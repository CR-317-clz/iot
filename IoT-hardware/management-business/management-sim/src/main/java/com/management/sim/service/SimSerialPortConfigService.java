package com.management.sim.service;

import com.management.common.core.domain.AjaxResult;
import com.management.sim.domain.SimSerialPortConfig;

import java.util.List;

public interface SimSerialPortConfigService {

    /**
     * 获取所有串口配置
     * @return
     */
    List<SimSerialPortConfig> getAllSerialPortConfig();

    /**
     * 保存串口配置
     * @param config
     * @return
     */
    AjaxResult saveSerialPortConfig(SimSerialPortConfig config);

    /**
     * 根据serialId获取串口配置
     * @param serialId
     * @return
     */
    SimSerialPortConfig getBySerialId(Long serialId);
}
