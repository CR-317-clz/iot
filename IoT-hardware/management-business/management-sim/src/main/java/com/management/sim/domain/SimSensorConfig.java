package com.management.sim.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.management.sim.domain.dto.VirtualDeviceThresholdDto;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@TableName(value = "sim_sensor_config") // 请替换为实际表名
public class SimSensorConfig {

    private Long sensorId;

    private String uniqueIdentification;

    private String sensorName;

    @TableField(value = "threshold", typeHandler = JacksonTypeHandler.class)
    private List<VirtualDeviceThresholdDto> threshold;

    private Date createTime;

    private Date updateTime;

}