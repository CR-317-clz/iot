package com.management.sim.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.management.sim.domain.SimVirtualDevice;

import java.util.List;

public interface SimVirtualDeviceService extends IService<SimVirtualDevice> {

    /**
     * 获取所有虚拟设备
     *
     * @return
     */
    List<SimVirtualDevice> getAllSimVirtualDevice();

    boolean updateBySimVirtualDevice(SimVirtualDevice device);

    boolean removeByVirtualId(Long virtualId);

    SimVirtualDevice getByVirtualCode(String virtualCode);
}
