package com.management.iot.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
@TableName(value = "jy_management.iot_relay_device_status")
public class IotRelayDeviceStatus {

    /**
     * 继电器ID（主键）
     * 对应数据库字段：relay_id
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long relayId;

    /**
     * 继电器设备名称
     * 对应数据库字段：relay_device_name
     */
    private String relayDeviceName;

    /**
     * 继电器状态
     * 对应数据库字段：status
     * 说明：
     *   0 - 关闭
     *   1 - 开启
     *   2 - 异常
     */
    private String status;

    /**
     * 协议编号
     * 对应数据库字段：protocol_id
     * 用于区分不同通信协议（如TCP、MQTT等）
     */
    private Long protocolId;

    /**
     * 创建时间
     * 对应数据库字段：create_time
     * 数据入库时间（自动生成）
     */
    private Date createTime;


}
