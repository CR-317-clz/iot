package com.management.sim.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * 传感器分类实体类
 * 
 * @author 
 * @since 
 */
@Data
@TableName("sim_sensor_genre")
public class SimSensorGenre {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long sensorId;

    /**
     * 父级ID
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long parentId;

    /**
     * 分类编码
     */
    private String sensorCode;

    /**
     * 分类名称
     */
    private String sensorName;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 状态：0禁用，1启用
     */
    private Integer status;

    /**
     * 描述
     */
    private String description;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /** 子分类 */
    @TableField(exist = false)
    private List<SimSensorGenre> children;

    /** 当前分类下的虚拟设备 */
    @TableField(exist = false)
    private List<SimVirtualDevice> devices;
}