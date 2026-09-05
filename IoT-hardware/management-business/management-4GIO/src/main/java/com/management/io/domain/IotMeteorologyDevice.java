package com.management.io.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@TableName("jy_management.iot_meteorology_device")
public class IotMeteorologyDevice {

    /**
     * 设备ID（主键）
     */
    private Long deviceId;

    /**
     * 系统编码
     */
    private String systemCode;

    /**
     * 设备唯一地址
     */
    private String deviceAddr;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 纬度
     */
    private Double lat;

    /**
     * 经度
     */
    private Double lng;

    /**
     * 设备状态
     */
    private String deviceStatus;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 时间戳
     */
    private Long timestamp;

    /**
     * 注册数据列表
     */
    @TableField(exist = false)
    private List<IotMeteorologyRegisterData> registerDataList;

}
