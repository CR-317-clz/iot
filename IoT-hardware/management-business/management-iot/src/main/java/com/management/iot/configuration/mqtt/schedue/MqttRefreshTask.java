package com.management.iot.configuration.mqtt.schedue;

import com.management.iot.configuration.mqtt.MqttManager;
import com.management.iot.service.IotProtocolService;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时检查数据库配置是否变化
 * 若变化则重连对应 MQTT 客户端
 */
@Component
public class MqttRefreshTask {

    @Resource
    private IotProtocolService protocolConfigService;

    @Resource
    private MqttManager mqttManager;

    /**
     * 每30秒检查一次数据库
     */
    @Scheduled(fixedRate = 30000)
    public void refreshConnections() {
        System.out.println("🕒 检查 MQTT 配置变化...");
        mqttManager.initializeConnections(protocolConfigService.getProtocolsByMQTT());
    }
}
