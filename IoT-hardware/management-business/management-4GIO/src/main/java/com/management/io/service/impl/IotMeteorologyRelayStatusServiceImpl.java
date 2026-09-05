package com.management.io.service.impl;

import com.management.common.utils.snowFlake.SnowflakeIdGenerator;
import com.management.io.domain.IotMeteorologyDevice;
import com.management.io.domain.IotMeteorologyRelayStatus;
import com.management.io.mapper.IotMeteorologyRelayStatusMapper;
import com.management.io.service.IotMeteorologyRelayStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IotMeteorologyRelayStatusServiceImpl implements IotMeteorologyRelayStatusService {

    @Autowired
    private IotMeteorologyRelayStatusMapper iotMeteorologyRelayStatusMapper;


    @Override
    public IotMeteorologyRelayStatus selectLatestByDeviceAdr(String deviceAddr) {
        if (deviceAddr == null) {
            throw new IllegalArgumentException("设备地址不能为空");
        }
        try {
            IotMeteorologyRelayStatus relayStatus = iotMeteorologyRelayStatusMapper.selectLatestByDeviceAdr(deviceAddr);

            return relayStatus;
        } catch (Exception e) {
            throw new RuntimeException("获取气象站设备信息失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean insert(IotMeteorologyRelayStatus relay) {
        try {
            relay.setRelayId(SnowflakeIdGenerator.nextId());
            int result = iotMeteorologyRelayStatusMapper.insertMeteorologyRelayStatus(relay);
            return result > 0;
        } catch (Exception e) {
            throw new RuntimeException("添加气象站设备失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(IotMeteorologyRelayStatus relay) {
        try {
            int result = iotMeteorologyRelayStatusMapper.updateMeteorologyRelayStatus(relay);
            return result > 0;
        } catch (Exception e) {
            throw new RuntimeException("添加气象站设备失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据设备地址和继电器编号查询最新数据
     * @param valueOf 设备地址
     * @param relayNo 继电器编号
     * @return
     */
    @Override
    public IotMeteorologyRelayStatus selectLatestByDeviceAdrAndRelayNo(String valueOf, Integer relayNo) {

        return iotMeteorologyRelayStatusMapper.selectLatestByDeviceAdrAndRelayNo(valueOf, relayNo);
    }
}
