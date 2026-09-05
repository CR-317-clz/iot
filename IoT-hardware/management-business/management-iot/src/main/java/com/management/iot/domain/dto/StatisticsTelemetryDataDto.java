package com.management.iot.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

@Data
public class StatisticsTelemetryDataDto {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long deviceId;

    private String telemetryName;

}
