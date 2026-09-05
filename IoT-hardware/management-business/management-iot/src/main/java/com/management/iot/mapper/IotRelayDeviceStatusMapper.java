package com.management.iot.mapper;

import com.management.iot.domain.IotRelayDeviceStatus;

import java.util.List;

public interface IotRelayDeviceStatusMapper {

    /**
     * 添加设备状态
     * @param deviceStatus
     * @return
     */
    int insertRelayDeviceStatus(IotRelayDeviceStatus deviceStatus);

    /**
     * 根据协议ID查询设备状态
     * @param protocolId
     * @return
     */
    List<IotRelayDeviceStatus> getRelayDeviceStatusByProtocol(Long protocolId);
}
