package com.management.iot.kunlun;

import java.util.HashMap;
import java.util.Map;

public final class DeviceTopicConfig {

    private DeviceTopicConfig() {
        throw new UnsupportedOperationException("常量工具类，禁止实例化");
    }

    // 发布主题配置：设备topic数字到MQTT主题号的映射
    // 规则：device.topic = "N" → MQTT主题 = PUBLISH_BASE_TOPIC + N
    public static final int PUBLISH_BASE_TOPIC = 20;
    public static final int SUBSCRIBE_BASE_TOPIC = 30;
    public static final int MAX_TOPIC_NUMBER = 6;

    // 主题类型枚举
    public enum TopicType {
        PUBLISH,    // 发布主题（发送数据）
        SUBSCRIBE   // 订阅主题（接收控制指令）
    }

    /**
     * 根据设备topic和主题类型获取对应的MQTT主题
     */
    public static Integer getMqttTopicForDevice(String deviceTopic, TopicType type) {
        if (deviceTopic == null || deviceTopic.isEmpty()) {
            return null;
        }
        try {
            int topicNum = Integer.parseInt(deviceTopic);
            if (topicNum >= 1 && topicNum <= MAX_TOPIC_NUMBER) {
                int baseTopic = (type == TopicType.PUBLISH) ? PUBLISH_BASE_TOPIC : SUBSCRIBE_BASE_TOPIC;
                return baseTopic + topicNum;
            }
        } catch (NumberFormatException e) {
            // 不是数字，返回null
        }
        return null;
    }

    /**
     * 重载方法，默认使用发布主题（保持向后兼容）
     */
    public static Integer getMqttTopicForDevice(String deviceTopic) {
        return getMqttTopicForDevice(deviceTopic, TopicType.PUBLISH);
    }

    /**
     * 获取订阅主题
     */
    public static Integer getSubscribeTopicForDevice(String deviceTopic) {
        return getMqttTopicForDevice(deviceTopic, TopicType.SUBSCRIBE);
    }

    /**
     * 从MQTT主题反推设备topic
     */
    public static String getDeviceTopicFromMqttTopic(Integer mqttTopic, TopicType type) {
        if (mqttTopic == null) {
            return null;
        }
        int baseTopic = (type == TopicType.PUBLISH) ? PUBLISH_BASE_TOPIC : SUBSCRIBE_BASE_TOPIC;
        int deviceNumber = mqttTopic - baseTopic;
        if (deviceNumber >= 1 && deviceNumber <= MAX_TOPIC_NUMBER) {
            return String.valueOf(deviceNumber);
        }
        return null;
    }

    /**
     * 验证设备topic是否有效
     */
    public static boolean isValidDeviceTopic(String deviceTopic) {
        return getMqttTopicForDevice(deviceTopic) != null;
    }

    /**
     * 获取所有可能的主题映射
     */
    public static Map<String, Map<String, Integer>> getAllTopicMappings() {
        Map<String, Map<String, Integer>> allMappings = new HashMap<>();

        for (int i = 1; i <= MAX_TOPIC_NUMBER; i++) {
            String topicKey = String.valueOf(i);
            Map<String, Integer> mappings = new HashMap<>();
            mappings.put("publish", PUBLISH_BASE_TOPIC + i);
            mappings.put("subscribe", SUBSCRIBE_BASE_TOPIC + i);
            allMappings.put(topicKey, mappings);
        }

        return allMappings;
    }
}