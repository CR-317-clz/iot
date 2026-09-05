package com.management.iot.service;

import com.management.iot.domain.IotProtocol;
import com.management.iot.domain.IotScreenProtocol;

import java.util.List;

public interface IotScreenProtocolService {

    /**
     * 获取所有 MQTT 配置的协议
     * @return
     */
    List<IotScreenProtocol> getScreenProtocolsByMQTT();

    /**
     * 根据配置 JSON 获取协议
     * @param configJson
     * @return
     */
    IotScreenProtocol getScreenByConfigJson(String configJson);
}
