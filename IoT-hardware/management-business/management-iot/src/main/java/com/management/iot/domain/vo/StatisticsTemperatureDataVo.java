package com.management.iot.domain.vo;

import lombok.Data;

@Data
public class StatisticsTemperatureDataVo {

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 记录时间
     */
    private String recordDate;

    /**
     * 周
     */
    private String dayChinese;

    /**
     * 星期几
     */
    private String weekdayOrder;

    /**
     * 数据值
     */
    private String dataKeyChinese;

    /**
     * 平均温度
     */
    private String avgTemperature;

    /**
     * 单位
     */
    private String unit;

    /**
     * 记录数量
     */
    private String recordCount;

    /**
     * 周数
     */
    private String weekNumber;
}
