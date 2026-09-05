package com.management.sim.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class WiringCheckResult {
    
    // 源设备ID
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long sourceDeviceId;
    
    // 源引脚
    private String sourcePin;
    
    // 目标设备ID
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long targetDeviceId;
    
    // 目标引脚
    private String targetPin;
    
    // 检查是否有效：true-允许连接，false-禁止连接
    private Boolean valid;
    
    // 结果消息（显示给用户）
    private String message;
    
    // 规则类型：1-正确，2-错误
    private String ruleType;
    
    // 匹配的规则ID
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long ruleId;
    
    // 建议操作（前端可根据此字段显示不同图标/颜色）
    private String suggestion;
    
    // 检查时间戳
    private Long timestamp = System.currentTimeMillis();
}