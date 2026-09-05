package com.management.io.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("jy_management.iot_meteorology_relay_status")
public class IotMeteorologyRelayStatus {

    /**
     * 主键ID
     * 对应字段：relay_id
     * 数据类型：int8（数据库中一般为 bigint）
     */
    private Long relayId;

    /**
     * 设备地址
     * 对应字段：device_adr
     * 数据类型：int8
     * 用于标识继电器所属的设备
     */
    private String deviceAdr;

    /**
     * 继电器编号
     * 对应字段：relay_no
     * 数据类型：int4（数据库中为 int）
     * 表示第几路继电器（如 1~8）
     */
    private Integer relayNo;

    /**
     * 继电器状态
     * 对应字段：relay_status
     * 数据类型：int4 或 varchar(10)
     * 说明：0=关闭，1=开启，其他值表示异常状态
     */
    private Integer relayStatus;

    /**
     * 数据采集时间
     * 对应字段：timestamp
     * 数据类型：timestamp(6)
     * 记录该状态采集的实际时间
     */
    private Long timestamp;

    /**
     * 创建时间
     * 对应字段：create_time
     * 数据类型：timestamp(6)
     * 记录数据入库时间
     */
    private Date createTime;

}
