package com.management.iot.mapper;

import com.management.iot.domain.IotCurtainMachineStatus;

public interface IotCurtainMachineStatusMapper {

    /**
     * 添加帘控设备状态
     * @param iotCurtainMachineStatus
     * @return
     */
    int insertCurtainMachineStatusService(IotCurtainMachineStatus iotCurtainMachineStatus);

    /**
     * 根据协议ID查询帘控设备状态
     * @param protocolId
     * @return
     */
    IotCurtainMachineStatus getCurtainMachineStatusByProtocol(Long protocolId);

}
