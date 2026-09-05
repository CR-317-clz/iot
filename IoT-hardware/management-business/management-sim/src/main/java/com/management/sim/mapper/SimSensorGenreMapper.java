package com.management.sim.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.management.sim.domain.SimSensorGenre;

import java.util.List;

public interface SimSensorGenreMapper extends BaseMapper<SimSensorGenre> {

    /**
     * 查询所有虚拟仿真传感器类别
     *
     * @return
     */
    List<SimSensorGenre> selectAllSimSensorCategory();

}
