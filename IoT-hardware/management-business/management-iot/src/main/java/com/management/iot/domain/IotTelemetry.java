package com.management.iot.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 设备采集的时序数据实体类
 * 对应表: iot_telemetry
 */
@Data
@TableName(value = "jy_management.iot_telemetry")
public class IotTelemetry {

    // 数据记录ID
    private Long telemetryId;

    // 来源设备ID
    private Long deviceId;

    // 数据键名（如 temperature, humidity）
    private String dataKey;

    // 数值型数据
    private Object dataValue;

    // 单位（如 ℃、%）
    private String unit;

    // 数据时间
    private Date recordTime;

    // 数据键名中文名称
    @TableField(exist = false)
    private String dataKeyChinese;

    @TableField(exist = false)
    private String windDirectionName;
}