package com.management.sim.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Accessors(chain = true)
public class WiringCheckDTO {
    
    // 源设备ID
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long sourceDeviceId;
    
    // 源引脚类型
    private String sourcePin;
    
    // 目标设备ID
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long targetDeviceId;
    
    // 目标引脚类型
    private String targetPin;
    
    // 可选：源设备电压（用于电压匹配检查）
    private BigDecimal sourceVoltage;
    
    // 可选：目标设备电压要求
    private BigDecimal targetRequiredVoltage;
    
    // 可选：连接协议类型
    private String protocolType;
}
