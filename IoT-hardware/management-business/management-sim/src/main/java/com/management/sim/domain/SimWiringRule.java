package com.management.sim.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 接线规则实体类
 * 对应数据库表：sim_wiring_rule
 */
@Data
@Accessors(chain = true)
@TableName("sim_wiring_rule")
public class SimWiringRule {
    
    // 规则ID - 主键
    @TableId(value = "rule_id", type = IdType.ASSIGN_ID)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long ruleId;
    
    // 源设备ID（虚拟设备或物理设备）
    @TableField("virtual_device_id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long virtualDeviceId;
    
    // 源引脚类型：power(电源)/signal(信号)/data(数据)/GND(地线)/VCC(电源正)
    @TableField("source_pin_type")
    private String sourcePinType;
    
    // 目标设备ID - 支持多个设备，用逗号分隔
    // 例如："407095181041602560,407097085469528064"
    @TableField("target_device_id")
    private String targetDeviceId;
    
    // 目标引脚类型
    // 例如：GND/5V/3.3V/PB0/PB1/OUT/IN 等
    @TableField("target_pin_type")
    private String targetPinType;
    
    // 连接类型：默认为 signal
    @TableField("connection_type")
    private String connectionType = "signal";
    
    // 是否允许连接：1-允许，0-禁止
    @TableField("is_allowed")
    private Boolean isAllowed = true;
    
    // 是否需要电压匹配：1-需要，0-不需要
    @TableField("voltage_match_required")
    private Boolean voltageMatchRequired = true;
    
    // 是否需要协议匹配：1-需要，0-不需要
    @TableField("protocol_match_required")
    private Boolean protocolMatchRequired = false;
    
    // 最大允许电压差（单位：V）
    @TableField("max_voltage_diff")
    private BigDecimal maxVoltageDiff = new BigDecimal("0.50");
    
    // 规则描述信息
    @TableField("description")
    private String description;
    
    // 规则状态：1-启用，0-停用
    @TableField("status")
    private String status;
    
    // 规则类型：1-正确提示，2-错误警告
    @TableField("rule_type")
    private String ruleType;
    
    // 逻辑删除标志：0-正常，1-已删除
    @TableField("del_flag")
    private String delFlag = "0";
    
    // 创建时间 - 自动填充
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    // 更新时间 - 自动更新
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}