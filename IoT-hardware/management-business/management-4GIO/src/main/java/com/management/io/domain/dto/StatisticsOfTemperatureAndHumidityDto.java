package com.management.io.domain.dto;

import lombok.Data;

import java.util.List;

@Data
public class StatisticsOfTemperatureAndHumidityDto {

    //星期
    private String weekDay;

    //温度和湿度数据
    private List<TemperatureAndHumidity> temperatureAndHumidityList;

    @Data
    public class TemperatureAndHumidity {
        private String registerName;
        private double avgValue;
    }

}
