package com.management.iot.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 设备事件记录实体类
 * 对应表: iot_event
 */
@Data
@TableName(value = "jy_management.iot_event")
public class IotEvent {

    // 事件ID
    private Long eventId;

    // 关联设备
    private Long deviceId;

    // 事件类型（ALARM, WARNING, INFO等）
    private String eventType;

    // 事件描述
    private String description;

    // 事件发生时间
    private Date eventTime;
    

}