package com.management.iot.kunlun;
import com.alibaba.fastjson2.JSONObject;
import com.management.common.core.domain.AjaxResult;
import com.management.io.domain.IotMeteorologyRegisterData;
import com.management.io.service.IotMeteorologyRegisterDataService;
import com.management.iot.configuration.mqtt.MqttConnection;
import com.management.iot.configuration.mqtt.MqttManager;
import com.management.iot.configuration.tcp.TcpClientManager;
import com.management.iot.domain.IotDevice;
import com.management.iot.domain.IotRelayDeviceStatus;
import com.management.iot.service.IotDeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DeviceDataCollectService {

    @Autowired
    private IotDeviceService iotDeviceService;

    @Autowired
    private TcpClientManager tcpClientManager;

    @Autowired
    private MqttManager mqttService;

    @Autowired
    private IotMeteorologyRegisterDataService meteorologyRegisterDataService;


    /**
     * 收集并发送所有设备数据
     */
    public AjaxResult collectAndSendDeviceData() {
        List<Map<String, Object>> results = new ArrayList<>();
        int processedCount = 0;
        int successCount = 0;
        
        System.out.println("\n" + "=".repeat(50));
        System.out.println("📊 开始收集设备数据");
        System.out.println("=".repeat(50));
        
        List<IotDevice> devices = iotDeviceService.getAllDevices();
        
        for (IotDevice device : devices) {
            processedCount++;
            System.out.println("\n处理设备 [" + processedCount + "/" + devices.size() + "]: " + 
                             device.getDeviceName() + " (topic=" + device.getTopic() + ")");
            
            Map<String, Object> deviceResult = new HashMap<>();
            deviceResult.put("deviceId", device.getDeviceId());
            deviceResult.put("deviceName", device.getDeviceName());
            deviceResult.put("deviceCode", device.getDeviceCode());
            deviceResult.put("topic", device.getTopic());

            try {
                // 1. 收集继电器状态
                Map<String, Integer> relayStatus = collectRelayStatus(device);
                deviceResult.put("relayStatus", relayStatus);

                // 2. 发送数据到MQTT（固定主题2）
                boolean sent = sendDataToMqtt(device, relayStatus);
                deviceResult.put("mqttSent", sent);

                if (sent) {
                    successCount++;
                    System.out.println("✅ 数据发送成功");
                } else {
                    System.out.println("❌ 数据发送失败");
                }

                deviceResult.put("success", true);

            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("❌ 处理异常: " + e.getMessage());
                deviceResult.put("success", false);
                deviceResult.put("error", e.getMessage());
            }

            results.add(deviceResult);
        }

        System.out.println("\n" + "=".repeat(50));
        System.out.println("✅ 数据收集完成");
        System.out.println("   处理设备: " + processedCount + "台");
        System.out.println("   成功发送: " + successCount + "台");
        System.out.println("=".repeat(50));

        return AjaxResult.success("设备数据收集完成", Map.of(
            "processedCount", processedCount,
            "successCount", successCount,
            "totalDevices", devices.size(),
            "devices", results,
            "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * 收集继电器状态（只收集继电器设备，不包括窗帘）
     */
    private Map<String, Integer> collectRelayStatus(IotDevice device) {
        Map<String, Integer> relayRegisterData = new LinkedHashMap<>();

        if (device.getRelayProtocolId() == null) {
            System.out.println("⚠️ 设备无继电器协议");
            return relayRegisterData;
        }

        try {
            System.out.println("🔌 查询继电器状态...");

            // 使用TCP客户端查询继电器状态
            List<IotRelayDeviceStatus> relayDeviceStatuses =
                    tcpClientManager.sendMessageAndWaitResponse(device.getRelayProtocolId(), "qa");

            // 只添加继电器设备
            if (relayDeviceStatuses != null && !relayDeviceStatuses.isEmpty()) {
                for (IotRelayDeviceStatus status : relayDeviceStatuses) {
                    String deviceName = status.getRelayDeviceName();
                    Integer statusText = Integer.valueOf("1".equals(status.getStatus()) ? "1" : "0");
                    relayRegisterData.put(deviceName, statusText);
                    System.out.println("   [继电器] " + deviceName + ": " + statusText);
                }
            } else {
                System.out.println("⚠️ 继电器状态查询失败");
            }

            System.out.println("✅ 状态查询完成，共 " + relayRegisterData.size() + " 个继电器");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ 设备状态查询异常: " + e.getMessage());
        }

        return relayRegisterData;
    }

    /**
     * 发送数据到MQTT（固定使用主题2）
     */
    private boolean sendDataToMqtt(IotDevice device, Map<String, Integer> relayStatus) {

        // 固定使用主题 2
        String mqttTopic = "2";

        if (relayStatus == null || relayStatus.isEmpty()) {
            System.out.println("⚠️ 无继电器状态数据，跳过发送");
            return false;
        }

        try {
            // 直接发送继电器状态 JSON
            String message = JSONObject.toJSONString(relayStatus);

            int result = mqttService.publish(
                null,
                mqttTopic,                 // 固定主题 "2"
                message,
                1,
                false
            );

            boolean success = result > 0;
            if (success) {
                System.out.println("📨 继电器状态发送到MQTT主题: " + mqttTopic);
                System.out.println("   " + message);
            } else {
                System.out.println("❌ MQTT发送失败，主题: " + mqttTopic);
            }

            return success;

        } catch (Exception e) {
            System.err.println("❌ MQTT发送异常: " + e.getMessage());
            return false;
        }
    }

    /**
     * 查询单个设备数据（不发送MQTT）
     */
    public Map<String, Object> getDeviceData(Long deviceId) {
        try {
            IotDevice device = iotDeviceService.getDeviceById(deviceId);
            if (device == null) {
                return Map.of("error", "设备不存在");
            }

            Map<String, Object> result = new HashMap<>();
            result.put("deviceInfo", getDeviceInfo(device));
            result.put("relayStatus", collectRelayStatus(device));
            result.put("timestamp", System.currentTimeMillis());
            result.put("success", true);

            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("error", e.getMessage(), "success", false);
        }
    }

    /**
     * 获取设备基本信息
     */
    private Map<String, Object> getDeviceInfo(IotDevice device) {
        Map<String, Object> info = new HashMap<>();
        info.put("deviceId", device.getDeviceId());
        info.put("deviceName", device.getDeviceName());
        info.put("deviceCode", device.getDeviceCode());
        info.put("deviceType", device.getDeviceType());
        info.put("status", device.getStatus());
        info.put("location", device.getLocation());
        info.put("topic", device.getTopic());
        info.put("host", device.getHost());
        info.put("protocolId", device.getProtocolId());
        info.put("relayProtocolId", device.getRelayProtocolId());
        info.put("createTime", device.getCreateTime());
        info.put("updateTime", device.getUpdateTime());
        return info;
    }

    /**
     * 获取所有设备的MQTT主题映射
     */
    public Map<String, Integer> getDeviceMqttMappings() {
        Map<String, Integer> mappings = new HashMap<>();

        List<IotDevice> devices = iotDeviceService.getAllDevices();
        for (IotDevice device : devices) {
            // 固定主题2
            mappings.put(device.getDeviceId() + ":" + device.getDeviceName(), 2);
        }

        return mappings;
    }
}