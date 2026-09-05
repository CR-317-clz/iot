package com.management.io.domain.vo;

import lombok.Data;

import java.util.List;

@Data
public class StatisticsOfTemperatureAndHumidityVo {

    //星期
    private String weekDay;

    //温度和湿度数据
    private String registerName;

    //平均值
    private double avgValue;
}
