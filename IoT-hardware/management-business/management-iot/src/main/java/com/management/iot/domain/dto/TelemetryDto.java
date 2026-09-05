package com.management.iot.domain.dto;

import lombok.Data;

/**
 * 传感器数据DTO
 * @author iot
 */
@Data
public class TelemetryDto {
    /**
     * 温度
     */
    private Double temperature;
    
    /**
     * 湿度
     */
    private Double humidity;
    
    /**
     * 光照度
     */
    private Double illuminance;
    
    /**
     * 土壤湿度
     */
    private Double soilMoisture;
    
    /**
     * 一氧化碳
     */
    private Double carbonMonoxide;
    
    /**
     * 二氧化碳
     */
    private Double carbonDioxide;
    
    /**
     * 紫外线
     */
    private Double ultravioletRay;
}