package com.management.iot.domain.dto;

import com.management.iot.domain.IotProtocol;
import com.management.iot.domain.IotTelemetry;
import lombok.Data;

import java.util.List;

@Data
public class KunLunDto {

    private List<IotTelemetry> telemetryList;

}
