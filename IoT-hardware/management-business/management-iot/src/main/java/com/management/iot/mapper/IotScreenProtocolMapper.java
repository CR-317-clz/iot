package com.management.iot.mapper;

import com.management.iot.domain.IotScreenProtocol;

import java.util.List;

public interface IotScreenProtocolMapper {

    /**
     * 根据协议类型获取协议配置列表
     * @return 协议配置列表
     */
    List<IotScreenProtocol> getScreenProtocolsByMQTT();

    /**
     * 根据协议配置 JSON 获取协议配置
     * @param configJson 协议配置 JSON
     * @return 协议配置
     */
    IotScreenProtocol getScreenByConfigJson(String configJson);
}
