package com.management.sim.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.management.sim.domain.SimVirtualDevice;

import java.util.List;

public interface SimVirtualDeviceMapper extends BaseMapper<SimVirtualDevice> {
    /**
     * 获取所有虚拟设备
     *
     * @return
     */
    List<SimVirtualDevice> getAllSimVirtualDevice();

    int removeByVirtualId(Long virtualId);
}
