package com.management.sim.domain.dto;

import lombok.Data;

@Data
public class VirtualDevicePinDto {

    private String pinId;

    private String pinName;

    private String pinType;

    private String pinColour;

    //电压
    private String voltage;

    //协议
    private String agreement;

    //位置
    private String location;
}
