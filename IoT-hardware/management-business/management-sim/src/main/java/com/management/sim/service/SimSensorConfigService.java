package com.management.sim.service;

import com.management.sim.domain.SimSensorConfig;
import com.management.sim.domain.SimSerialPortConfig;

import java.util.List;

public interface SimSensorConfigService {

    /**
     * 获取所有传感器配置
     * @return
     */
    List<SimSensorConfig> getAllSimSensorConfig();

}
