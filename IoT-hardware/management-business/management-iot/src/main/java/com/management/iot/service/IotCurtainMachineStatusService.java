package com.management.iot.service;

import com.management.iot.domain.IotCurtainMachineStatus;

public interface IotCurtainMachineStatusService {

    /**
     * 添加帘控设备状态
     *
     * @param iotCurtainMachineStatus 帘控设备状态
     * @return 是否添加成功
     */
    boolean addCurtainMachineStatus(IotCurtainMachineStatus iotCurtainMachineStatus);

    /**
     * 根据协议ID获取帘控设备状态
     *
     * @param protocolId 协议ID
     * @return 帘控设备状态
     */
    IotCurtainMachineStatus getCurtainMachineStatusByProtocol(Long protocolId);
}
