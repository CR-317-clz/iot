package com.management.iot.domain.unity;

import com.management.iot.domain.dto.ProtocolConfigDto;
import lombok.Data;

@Data
public class InquireUnityDto {

    // 协议名称
    private String host;

    // 订阅号
    private String topic;

}
