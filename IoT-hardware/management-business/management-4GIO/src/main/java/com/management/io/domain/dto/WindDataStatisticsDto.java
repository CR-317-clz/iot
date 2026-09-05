package com.management.io.domain.dto;

import lombok.Data;

@Data
public class WindDataStatisticsDto {

    private Double avgWindSpeed;
    private Double maxWindSpeed;
    private Double currentWindSpeed;
    private String currentWindDirection;
    private Double currentWindPower;
}
