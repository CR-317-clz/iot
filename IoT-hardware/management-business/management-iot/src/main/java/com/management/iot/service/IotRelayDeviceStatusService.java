package com.management.iot.service;

import com.management.iot.domain.IotRelayDeviceStatus;

import java.util.List;

public interface IotRelayDeviceStatusService {

    /**
     * 添加设备状态
     * @param deviceStatus
     */
    boolean insert(IotRelayDeviceStatus deviceStatus);

    /**
     * 根据协议ID获取设备状态
     * @param protocolId
     * @return
     */
    List<IotRelayDeviceStatus> getRelayDeviceStatusByProtocol(Long deviceId);
}
