package com.management.iot.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.management.iot.domain.IotAutomationConfig;
import lombok.Data;

import java.util.List;

/**
 * 继电器数据传输对象
 */
@Data
public class RelayControlDto {

    //自动化模式（1-手动 2-自动化）
    private Integer automation;

    //继电器编号
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long relayProtocolId;

    //风扇（0-关 1-开）
    private Integer blower;

    //紫外线灯（0-关 1-开）
    private Integer ultravioletRadiator;

    //喷淋器（0-关 1-开）
    private Integer spray;

    //水帘幕（0-关 1-开）
    private Integer waterCurtain;

    //窗帘机器（0-关 1-开）
    private Integer curtainMachine;

    /**
     * 自动化配置信息，第一次传参时保存配置
     */
    private IotAutomationConfig automationConfig;
}
