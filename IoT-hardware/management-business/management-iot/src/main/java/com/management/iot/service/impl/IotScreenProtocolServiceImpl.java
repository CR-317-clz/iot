package com.management.iot.service.impl;

import com.management.iot.domain.IotProtocol;
import com.management.iot.domain.IotScreenProtocol;
import com.management.iot.mapper.IotScreenProtocolMapper;
import com.management.iot.service.IotScreenProtocolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IotScreenProtocolServiceImpl implements IotScreenProtocolService {

    @Autowired
    private IotScreenProtocolMapper iotScreenProtocolMapper;

    @Override
    public List<IotScreenProtocol> getScreenProtocolsByMQTT() {
        try {
            List<IotScreenProtocol> protocol = iotScreenProtocolMapper.getScreenProtocolsByMQTT();
            if (protocol == null) {
                throw new RuntimeException("协议配置不存在");
            }
            return protocol;
        } catch (Exception e) {
            throw new RuntimeException("获取协议配置失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据配置JSON获取协议
     * @param configJson
     * @return
     */
    @Override
    public IotScreenProtocol getScreenByConfigJson(String configJson) {
        try {
            IotScreenProtocol protocol = iotScreenProtocolMapper.getScreenByConfigJson(configJson);
            if (protocol == null) {
                throw new RuntimeException("继电器编号不存在");
            }
            return protocol;
        } catch (Exception e) {
            throw new RuntimeException("获取继电器编号失败: " + e.getMessage(), e);
        }
    }
}
