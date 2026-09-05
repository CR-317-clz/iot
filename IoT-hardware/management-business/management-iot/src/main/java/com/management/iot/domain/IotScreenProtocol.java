package com.management.iot.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 昆仑实体类
 */
@Data
@TableName("iot_screen_protocol")
public class IotScreenProtocol {

    /**
     * 屏幕id
     */
    public Long screenId;

    /**
     * 屏幕名称
     */
    private String screenName;

    /**
     * 协议说明
     */
    private String description;

    /**
     * 协议配置内容（JSON格式）
     */
    public String configJson;

    /**
     * 继电器协议编号
     */
    private Long relayProtocolId;
}
