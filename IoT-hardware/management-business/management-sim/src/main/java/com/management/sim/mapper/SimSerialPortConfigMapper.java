package com.management.sim.mapper;

import com.management.sim.domain.SimSerialPortConfig;

import java.util.List;

public interface SimSerialPortConfigMapper {

    /**
     * 获取所有串口配置
     * @return
     */
    List<SimSerialPortConfig> getAllSerialPortConfig();

    // 获取唯一标识符
    List<SimSerialPortConfig> getUniqueIdentification(String uniqueIdentification);

    int saveSerialPortConfig(SimSerialPortConfig config);

    /**
     * 获取所有协调器
     * @param equipmentConfigurationType
     * @return
     */
    List<SimSerialPortConfig> getAllCoordinators(String equipmentConfigurationType);

    /**
     * 通过串口ID获取串口配置
     * @param serialId
     * @return
     */
    SimSerialPortConfig getBySerialId(Long serialId);
}
