package com.management.sim.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.management.sim.domain.SimSensorGenre;

import java.util.List;

public interface SimSensorGenreService extends IService<SimSensorGenre> {

    /**
     * 获取所有虚拟仿真设备类别
     * @return
     */
    List<SimSensorGenre> getAllSimSensorCategory();

    List<SimSensorGenre> getGenreDeviceTree();

}
