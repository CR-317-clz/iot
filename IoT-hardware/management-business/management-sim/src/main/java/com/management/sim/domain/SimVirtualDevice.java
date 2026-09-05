package com.management.sim.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.management.sim.domain.dto.VirtualDevicePinDto;
import com.management.sim.domain.dto.VirtualDeviceThresholdDto;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 虚拟设备实体类
 */
@Data
@TableName(value = "sim_virtual_device",autoResultMap = true) // 请根据实际表名修改
public class SimVirtualDevice {
    
    /**
     * 主键编号
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long virtualId;
    
    /**
     * 虚拟设备名称
     */
    private String virtualName;
    
    /**
     * 虚拟设备编码
     */
    private String virtualCode;

    /**
     * 虚拟设备图片
     */
    private String images;
    
    /**
     * 虚拟设备类型
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long genreId;
    
    /**
     * 电压/伏特数
     */
    private Integer voltage;
    
    /**
     * 通讯协议
     */
    private String agreement;
    
    /**
     * 阈值
     */
    @TableField(value = "threshold", typeHandler = JacksonTypeHandler.class)
    private List<VirtualDeviceThresholdDto> threshold;
    
    /**
     * 引脚
     */
    @TableField(value = "pin", typeHandler = JacksonTypeHandler.class)
    private List<VirtualDevicePinDto> pin;
    
    /**
     * 状态（0-未使用 1-已使用）
     */
    private Integer status;
    
    /**
     * 排序
     */
    private Integer sort;

    private Integer isNode;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 修改时间
     */
    private Date updateTime;

    @TableField(exist = false)
    private String genreName;

    /**
     * 传感器类型编码（关联查询字段，非数据库字段）
     */
    @TableField(exist = false)
    private String sensorCode;

}