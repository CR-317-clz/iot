package com.management.iot.kunlun;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.management.iot.configuration.mqtt.MqttConnection;
import com.management.iot.configuration.mqtt.MqttManager;
import com.management.iot.configuration.tcp.TcpClientManager;
import com.management.iot.domain.enumeration.DeviceEnum;
import com.management.iot.service.IotDeviceService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * MQTT继电器控制订阅服务
 * 订阅主题: 5
 * 接收格式: {"一号位": "false", "二号位": "0", "三号位": "true"}
 * 解析规则: true/1 -> 开启 (a), false/0/flase -> 关闭 (b)
 * 注：只处理一号位、二号位、三号位，四号位忽略
 * 命令间隔: 500ms
 */
@Service
public class MqttRelayControlService {

    @Autowired
    private MqttManager mqttManager;

    @Autowired
    private TcpClientManager tcpClientManager;

    @Autowired
    private IotDeviceService iotDeviceService;

    // ========== 常量定义 ==========

    /** 控制协议ID（固定） */
    private static final Long CONTROL_PROTOCOL_ID = 377282140729118720L;

    /** MQTT订阅主题 */
    private static final String CONTROL_TOPIC = "5";

    /** 命令间隔延迟（毫秒） */
    private static final long COMMAND_DELAY_MS = 500;

    /** 支持的设备列表（只处理一号位、二号位、三号位） */
    private static final Set<String> SUPPORTED_DEVICES = new HashSet<>(Arrays.asList("一号位", "二号位", "三号位"));

    /** 设备名称到设备类型的映射 */
    private static final Map<String, DeviceEnum> DEVICE_NAME_MAP = new ConcurrentHashMap<>();

    // ========== 状态变量 ==========

    private int messageCount = 0;
    private boolean subscribed = false;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private int retryCount = 0;
    private static final int MAX_RETRIES = 10;

    @PostConstruct
    public void init() {
        System.out.println("=".repeat(60));
        System.out.println("🚀 初始化MQTT继电器控制服务");
        System.out.println("=".repeat(60));
        System.out.println("📌 固定控制协议ID: " + CONTROL_PROTOCOL_ID);

        initDeviceNameMapping();

        System.out.println("✅ MQTT继电器控制服务初始化完成");
        System.out.println("📡 目标订阅主题: " + CONTROL_TOPIC);
        System.out.println("📋 支持的设备: " + SUPPORTED_DEVICES);
        System.out.println("📋 命令规则: true/1=开启(a), false/0=关闭(b)");
        System.out.println("⏳ 命令间隔: " + COMMAND_DELAY_MS + "ms");
        System.out.println("=".repeat(60));
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        System.out.println("🔄 应用启动完成，开始订阅MQTT主题...");
        scheduler.schedule(() -> subscribeToControlTopic(), 3, TimeUnit.SECONDS);
    }

    private void initDeviceNameMapping() {
        for (DeviceEnum device : DeviceEnum.values()) {
            DEVICE_NAME_MAP.put(device.getName(), device);
            System.out.println("   ✅ 加载设备: " + device.getName() + " -> " + device.name());
        }
    }

    private void subscribeToControlTopic() {
        try {
            boolean hasConnections = mqttManager.hasConnections();
            int connectionCount = mqttManager.getConnectionCount();
            System.out.println("📡 当前MQTT连接数量: " + connectionCount);

            if (!hasConnections) {
                retryCount++;
                if (retryCount > MAX_RETRIES) {
                    System.err.println("❌ 订阅失败: 已达到最大重试次数 " + MAX_RETRIES + " 次");
                    return;
                }
                System.out.println("⚠️ 没有MQTT连接，5秒后重试... (第" + retryCount + "次)");
                scheduler.schedule(() -> subscribeToControlTopic(), 5, TimeUnit.SECONDS);
                return;
            }

            if (subscribed) {
                System.out.println("✅ 已订阅主题: " + CONTROL_TOPIC);
                return;
            }

            System.out.println("📡 尝试订阅主题: " + CONTROL_TOPIC);

            mqttManager.subscribe(CONTROL_TOPIC, new MqttConnection.MessageCallback() {
                @Override
                public void onMessage(String topic, String message) {
                    messageCount++;
                    System.out.println("\n" + "=".repeat(60));
                    System.out.println("📨 收到控制指令! (第" + messageCount + "条)");
                    System.out.println("   📡 主题: " + topic);
                    System.out.println("   📝 消息: " + message);
                    System.out.println("   🕐 时间: " + new Date());
                    System.out.println("=".repeat(60));
                    handleControlCommand(message);
                }
            });

            subscribed = true;
            retryCount = 0;
            System.out.println("✅ 成功订阅主题: " + CONTROL_TOPIC);

        } catch (Exception e) {
            System.err.println("❌ 订阅主题失败: " + e.getMessage());
            e.printStackTrace();
            retryCount++;
            if (retryCount <= MAX_RETRIES) {
                System.out.println("🔄 5秒后重试订阅... (第" + retryCount + "次)");
                scheduler.schedule(() -> subscribeToControlTopic(), 5, TimeUnit.SECONDS);
            } else {
                System.err.println("❌ 订阅失败: 已达到最大重试次数 " + MAX_RETRIES + " 次");
            }
        }
    }

    /**
     * 解析字符串值为布尔值
     * 支持: true/false, 1/0, yes/no, on/off, 以及常见拼写错误 flase
     */
    private boolean parseBooleanValue(String value) {
        if (value == null) {
            return false;
        }
        String trimmed = value.trim().toLowerCase();

        if ("true".equals(trimmed) || "1".equals(trimmed) ||
            "yes".equals(trimmed) || "on".equals(trimmed)) {
            return true;
        }

        if ("false".equals(trimmed) || "0".equals(trimmed) ||
            "no".equals(trimmed) || "off".equals(trimmed) ||
            "flase".equals(trimmed)) {
            return false;
        }

        return !trimmed.isEmpty();
    }

    private void handleControlCommand(String message) {
        try {
            String cleanedMessage = message
                    .replaceAll("\\r\\n", "")
                    .replaceAll("\\n", "")
                    .replaceAll("\\r", "")
                    .replaceAll("\\s+", " ")
                    .trim();

            System.out.println("📝 清洗后消息: " + cleanedMessage);

            JSONObject jsonObject = JSON.parseObject(cleanedMessage);

            Map<String, Boolean> relayStatus = new LinkedHashMap<>();
            for (String key : jsonObject.keySet()) {
                if (!SUPPORTED_DEVICES.contains(key)) {
                    System.out.println("⏭️ 跳过不支持设备: " + key);
                    continue;
                }

                Object rawValue = jsonObject.get(key);
                Boolean value = null;

                if (rawValue instanceof Boolean) {
                    value = (Boolean) rawValue;
                } else if (rawValue instanceof String) {
                    value = parseBooleanValue((String) rawValue);
                } else if (rawValue instanceof Number) {
                    value = ((Number) rawValue).intValue() != 0;
                } else {
                    value = parseBooleanValue(String.valueOf(rawValue));
                }

                if (value != null) {
                    relayStatus.put(key, value);
                }
            }

            if (relayStatus.isEmpty()) {
                System.out.println("⚠️ 控制指令为空，忽略");
                return;
            }

            System.out.println("🔧 解析到控制指令:");
            for (Map.Entry<String, Boolean> entry : relayStatus.entrySet()) {
                String deviceName = entry.getKey();
                Boolean targetState = entry.getValue();
                String stateDesc = targetState ? "开启 (a)" : "关闭 (b)";
                System.out.println("   " + deviceName + " -> " + stateDesc + " (" + targetState + ")");
            }

            executeControl(relayStatus);

        } catch (Exception e) {
            System.err.println("❌ 解析控制指令失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 执行控制指令（使用固定协议ID）
     */
    private void executeControl(Map<String, Boolean> relayStatus) {
        System.out.println("\n🔌 开始执行控制指令...");
        System.out.println("📌 使用固定协议ID: " + CONTROL_PROTOCOL_ID);

        int index = 0;
        int total = relayStatus.size();

        for (Map.Entry<String, Boolean> entry : relayStatus.entrySet()) {
            String deviceName = entry.getKey();
            Boolean targetState = entry.getValue();

            DeviceEnum deviceEnum = DEVICE_NAME_MAP.get(deviceName);
            if (deviceEnum == null) {
                System.out.println("⚠️ 未知设备: " + deviceName + "，跳过");
                continue;
            }

            String command = buildControlCommand(deviceEnum, targetState);
            if (command == null) {
                System.out.println("⚠️ 设备 " + deviceName + " 不支持控制，跳过");
                continue;
            }

            String stateDesc = targetState ? "开启 (a)" : "关闭 (b)";
            System.out.println("📤 发送控制命令 [" + (index + 1) + "/" + total + "]:");
            System.out.println("   设备: " + deviceName);
            System.out.println("   协议ID: " + CONTROL_PROTOCOL_ID);
            System.out.println("   命令: " + command);
            System.out.println("   目标状态: " + stateDesc);

            boolean success = tcpClientManager.sendMessage(CONTROL_PROTOCOL_ID, command);

            if (success) {
                System.out.println("✅ 控制命令发送成功: " + deviceName + " -> " + stateDesc);
            } else {
                System.err.println("❌ 控制命令发送失败: " + deviceName);
            }

            index++;

            if (index < total) {
                try {
                    System.out.println("⏳ 等待 " + COMMAND_DELAY_MS + "ms 后发送下一条命令...");
                    Thread.sleep(COMMAND_DELAY_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        System.out.println("✅ 控制指令执行完成");
    }

    /**
     * 构建控制命令
     * true (开启): a + 设备编号 (如 a1 = 开启一号位)
     * false (关闭): b + 设备编号 (如 b1 = 关闭一号位)
     */
    private String buildControlCommand(DeviceEnum deviceEnum, Boolean targetState) {
        int deviceNumber = getDeviceNumber(deviceEnum);
        if (deviceNumber == 0) {
            return null;
        }
        String prefix = targetState ? "a" : "b";
        return prefix + deviceNumber;
    }

    private int getDeviceNumber(DeviceEnum deviceEnum) {
        switch (deviceEnum) {
            case FAN:
                return 1;
            case WATER_CURTAIN:
                return 2;
            case SPRINKLER:
                return 3;
            case LIGHT:
                return 0;
            default:
                return 0;
        }
    }

    public boolean sendControlCommand(String deviceName, Boolean targetState) {
        Map<String, Boolean> command = new LinkedHashMap<>();
        command.put(deviceName, targetState);
        return sendBatchControlCommand(command);
    }

    public boolean sendBatchControlCommand(Map<String, Boolean> commands) {
        try {
            String jsonMessage = JSON.toJSONString(commands);
            System.out.println("📤 发送批量控制指令到MQTT主题" + CONTROL_TOPIC + ": " + jsonMessage);

            int result = mqttManager.publish(null, CONTROL_TOPIC, jsonMessage, 1, false);
            System.out.println("   发布结果: " + result);
            return result > 0;
        } catch (Exception e) {
            System.err.println("❌ 发送控制指令失败: " + e.getMessage());
            return false;
        }
    }

    public Map<String, Object> getSubscriptionStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("topic", CONTROL_TOPIC);
        status.put("subscribed", subscribed);
        status.put("messageCount", messageCount);
        status.put("mqttConnections", mqttManager.getConnectionCount());
        status.put("controlProtocolId", CONTROL_PROTOCOL_ID);
        status.put("supportedDevices", SUPPORTED_DEVICES);
        status.put("commandRule", "true/1=开启(a), false/0=关闭(b)");
        status.put("commandDelayMs", COMMAND_DELAY_MS);
        return status;
    }
}