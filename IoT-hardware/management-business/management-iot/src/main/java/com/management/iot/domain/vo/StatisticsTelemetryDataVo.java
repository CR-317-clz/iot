package com.management.iot.domain.vo;

import lombok.Data;

@Data
public class StatisticsTelemetryDataVo {

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 记录时间
     */
    private String recordDate;

    /**
     * 时间格式
     */
    private String timeFormat;

    /**
     * 数据键名
     */
    private String dataKey;

    /**
     * 数据键名中文
     */
    private String dataKeyChinese;

    /**
     * 每小时平均值
     */
    private String hourlyAvgValue;

    /**
     * 单位
     */
    private String unit;

    private String minuteCount;
}
