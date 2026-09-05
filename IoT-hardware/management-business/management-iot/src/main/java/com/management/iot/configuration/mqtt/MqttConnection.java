package com.management.iot.configuration.mqtt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.management.common.utils.DataUnitUtil;
import com.management.common.utils.snowFlake.SnowflakeIdGenerator;
import com.management.iot.domain.IotDevice;
import com.management.iot.domain.IotTelemetry;
import com.management.iot.mapper.IotTelemetryMapper;
import com.management.iot.service.IotDeviceService;
import com.management.iot.service.IotTelemetryService;
import com.management.iot.utils.WindDirectionUtil;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 封装单个 MQTT 客户端连接与监听逻辑
 */
public class MqttConnection {

    /** MQTT 客户端实例 */
    private MqttClient client;

    /** MQTT 配置 JSON（来自数据库） */
    private final String configJson;

    /** 协议名称（用于日志输出） */
    private final String protocolName;

    /** 对应设备ID（来自协议-设备关联表） */
    private final Long deviceId;

    /** 是否正在运行检测线程 */
    private volatile boolean running = false;

    /** 最新消息缓存（key=topic, value=数据Map） */
    private static final Map<String, Map<String, Object>> latestMessageMap = new ConcurrentHashMap<>();

    /** MyBatis Mapper（写入数据库） */

    private final IotTelemetryService iotTelemetryService;

    private final IotDeviceService iotDeviceService;

    public MqttConnection(String protocolName, String configJson, Long deviceId,
                          IotTelemetryService iotTelemetryService, IotDeviceService iotDeviceService) {
        this.protocolName = protocolName;
        this.configJson = configJson;
        this.deviceId = deviceId;
        this.iotTelemetryService = iotTelemetryService;
        this.iotDeviceService = iotDeviceService;
    }

    /**
     * 启动 MQTT 连接与订阅 逻辑
     */
    public synchronized void connect() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(configJson);

            String broker = node.get("host").asText();   // MQTT服务器地址
            String topic = node.get("topic").asText();   // 订阅主题
            String username = node.has("username") ? node.get("username").asText() : null;
            String password = node.has("password") ? node.get("password").asText() : null;

            System.out.println("🔗 [" + protocolName + "] 正在连接 Broker: " + broker);
            System.out.println("📡 [" + protocolName + "] 订阅主题: " + topic);

            String clientId = "mqtt_" + protocolName + "_" + System.currentTimeMillis();
            client = new MqttClient(broker, clientId, new MemoryPersistence());

            // 设置连接选项
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setAutomaticReconnect(true);
            options.setConnectionTimeout(10);
            if (username != null) options.setUserName(username);
            if (password != null) options.setPassword(password.toCharArray());

            // 设置回调函数
            client.setCallback(new MqttCallbackExtended() {
                @Override
                public void connectionLost(Throwable cause) {
                    System.err.println("❌ [" + protocolName + "] 连接丢失: " + cause.getMessage());
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    String payload = new String(message.getPayload());
                    System.out.println("📩 [" + protocolName + "] 收到消息: topic=" + topic + " payload=" + payload);

                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        Map<String, Object> dataMap = mapper.readValue(payload, Map.class);
                        dataMap.put("recvTime", LocalDateTime.now());
                        latestMessageMap.put(topic, dataMap); // ✅ 缓存最新数据
                    } catch (Exception e) {
                        System.err.println("🚫 [" + protocolName + "] 消息解析失败: " + e.getMessage());
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    System.out.println("✅ [" + protocolName + "] 消息发送完成");
                }

                @Override
                public void connectComplete(boolean reconnect, String serverURI) {
                    System.out.println("🔗 [" + protocolName + "] 已连接到: " + serverURI + (reconnect ? "（重连）" : ""));
                    try {
                        client.subscribe(topic, 1);
                        System.out.println("📡 [" + protocolName + "] 已订阅主题: " + topic);

                        // ✅ 更新设备状态为在线
                        if (iotDeviceService != null){
                            IotDevice device = new IotDevice();
                            device.setDeviceId(deviceId);
                            device.setStatus("online");
                            iotDeviceService.updateDeviceStatus(device);
                            System.out.println("🟢 [" + protocolName + "] 设备已标记为在线");
                        }
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }
            });

            // 执行连接
            client.connect(options);

            // 启动后台连接监控
            startConnectionMonitor();

        } catch (Exception e) {
            // ✅ 更新设备状态为异常
            if (iotDeviceService != null){
                IotDevice device = new IotDevice();
                device.setDeviceId(deviceId);
                device.setStatus("ERROR");
                iotDeviceService.updateDeviceStatus(device);
                System.out.println("🔴 [" + protocolName + "] 设备已标记为异常");
            }
            System.err.println("🚫 [" + protocolName + "] 连接异常: " + e.getMessage());
        }
    }

    /**
     * 后台连接状态检测（每10秒检测一次）
     */
    private void startConnectionMonitor() {
        if (running) return;
        running = true;

        Thread monitorThread = new Thread(() -> {
            while (running) {
                try {
                    Thread.sleep(10000);
                    if (client == null) continue;

                    if (!client.isConnected()) {
                        System.err.println("⚠️ [" + protocolName + "] 检测到断线，尝试重连...");
                        reconnect();
                    }
                } catch (Exception e) {
                    System.err.println("🚫 [" + protocolName + "] 监控线程异常: " + e.getMessage());
                }
            }
        });

        monitorThread.setDaemon(true);
        monitorThread.start();
    }

    /** 手动重连逻辑 */
    private synchronized void reconnect() {
        try {
            if (client != null && !client.isConnected()) {
                client.reconnect();
                System.out.println("🔁 [" + protocolName + "] 已尝试重新连接");
            }
        } catch (MqttException e) {
            System.err.println("🚫 [" + protocolName + "] 重连失败: " + e.getMessage());
        }
    }

    /** 发布消息 */
    public int publish(String topic, String payload, int qos, boolean retained) {
        try {
            if (client == null || !client.isConnected()) {
                System.err.println("⚠️ [" + protocolName + "] 未连接，无法发布消息");
                return 0;
            }

            if (topic == null || topic.isEmpty()) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.readTree(configJson);
                topic = node.has("topic") ? node.get("topic").asText() : null;
            }

            if (topic == null || topic.isEmpty()) {
                System.err.println("🚫 [" + protocolName + "] 无效的 topic");
                return 0;
            }

            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(qos);
            message.setRetained(retained);
            client.publish(topic, message);

            System.out.println("📤 [" + protocolName + "] 发布成功 -> topic=" + topic + " payload=" + payload);
            return 1;
        } catch (Exception e) {
            System.err.println("🚫 [" + protocolName + "] 发布异常: " + e.getMessage());
            return 0;
        }
    }

    /** 断开连接 */
    public synchronized void disconnect() {
        running = false;
        try {
            if (client != null && client.isConnected()) {
                client.disconnect();
                System.out.println("🔌 [" + protocolName + "] 已断开连接");

                // ✅ 更新设备状态为离线
                if (iotDeviceService != null) {
                    IotDevice device = new IotDevice();
                    device.setDeviceId(deviceId);
                    device.setStatus("OFFLINE");
                    iotDeviceService.updateDeviceStatus(device);
                    System.out.println("🔴 [" + protocolName + "] 设备已标记为离线");
                }
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /** 保存缓存中的最新数据到数据库 */
    public void saveLatestData() {
        if (latestMessageMap.isEmpty()) return;

        try {
            List<IotTelemetry> telemetryList = new ArrayList<>();

            for (Map.Entry<String, Map<String, Object>> entry : latestMessageMap.entrySet()) {
                String topic = entry.getKey();
                Map<String, Object> record = entry.getValue();
                LocalDateTime time = (LocalDateTime) record.get("recvTime");

                for (Map.Entry<String, Object> kv : record.entrySet()) {
                    if ("recvTime".equals(kv.getKey())) continue;

                    IotTelemetry telemetry = new IotTelemetry();
                    telemetry.setTelemetryId(SnowflakeIdGenerator.nextId());
                    telemetry.setDeviceId(deviceId);
                    telemetry.setDataKey(kv.getKey());

                    String rawValue = kv.getValue().toString();

                    // 🔥 特殊处理：如果是风向字段，转换角度为中文方位
                    if ("winddirection".equalsIgnoreCase(kv.getKey())) {
                        double angle = Double.parseDouble(rawValue);
                        telemetry.setDataValue(WindDirectionUtil.angleToDirectionName(angle)); // 存中文名称
                        telemetry.setUnit(""); // 风向单位留空
                    } else {
                        telemetry.setDataValue(Double.parseDouble(rawValue));
                        telemetry.setUnit(DataUnitUtil.getUnit(kv.getKey()));
                    }

                    telemetryList.add(telemetry);

                    System.out.println("💾 [" + protocolName + "] 保存数据 -> deviceId=" + deviceId +
                            " topic=" + topic + " key=" + kv.getKey() + " value=" + kv.getValue());
                }
            }

            if (!telemetryList.isEmpty()) {

                System.out.println("💾 [" + telemetryList + "] 批量写入数据开始...");
                iotTelemetryService.batchSaveTelemetry(telemetryList); // ✅ 批量写入


                System.out.println("✅ [" + protocolName + "] 批量写入 " + telemetryList.size() + " 条数据到数据库");
            }

        } catch (Exception e) {
            System.err.println("🚫 [" + protocolName + "] 数据写入异常: " + e.getMessage());
        }
    }

    public String getProtocolName() {
        return protocolName;
    }

    public MqttClient getClient() {
        return client;
    }

    /**
     * 获取所有最新消息的快照（线程安全）
     */
    public static Map<String, Map<String, Object>> getLatestMessages() {
        // 返回不可修改副本，避免外部修改
        return Collections.unmodifiableMap(new HashMap<>(latestMessageMap));
    }

    // 在 MqttConnection.java 中添加订阅方法

    /**
     * 订阅主题并设置回调
     */
    public void subscribe(String topic, MessageCallback callback) {
        if (client != null && client.isConnected()) {
            try {
                client.subscribe(topic, (topic1, message) -> {
                    String payload = new String(message.getPayload());
                    callback.onMessage(topic1, payload);
                });
                System.out.println("✅ 订阅主题成功: " + topic);
            } catch (Exception e) {
                System.err.println("❌ 订阅主题失败: " + e.getMessage());
            }
        }
    }

    /**
     * 订阅主题（带QoS）
     */
    public void subscribe(String topic, int qos, MessageCallback callback) {
        if (client != null && client.isConnected()) {
            try {
                client.subscribe(topic, qos, (topic1, message) -> {
                    String payload = new String(message.getPayload());
                    callback.onMessage(topic1, payload);
                });
                System.out.println("✅ 订阅主题成功: " + topic + " (QoS: " + qos + ")");
            } catch (Exception e) {
                System.err.println("❌ 订阅主题失败: " + e.getMessage());
            }
        }
    }

    /**
     * 消息回调接口
     */
    public interface MessageCallback {
        void onMessage(String topic, String message);
    }
}
