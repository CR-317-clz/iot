package com.management.iot.domain.dto;

import lombok.Data;

@Data
public class MqttTelemetryDto {

    //土壤湿度
    private Long soilMoisture;

    //二氧化碳
    private String carbonDioxide;

    //光照度
    private Long illuminance;

    //土壤PH
    private String soilPh;

    //土壤电导率
    private String soilElectricalConductivity;

    //一氧化碳
    private String carbonMonoxide;

    //温度
    private String temperature;

    //湿度
    private String humidity;

    //土壤温度
    private String soilTemperature;

    //紫外线
    private String ultravioletRay;
}
