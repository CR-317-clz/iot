package com.management.iot.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 帘控设备实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "iot_curtain_machine_status")
public class IotCurtainMachineStatus {

    /**
     * 帘控设备ID
     */
    private Long curtainMachineId;

    /**
     * 帘控设备名称
     */
    private String curtainMachineName;

    /**
     * 帘控设备状态
     */
    private String status;

    /**
     * 设备协议ID
     */
    private Long protocolId;

    /**
     * 帘控设备创建时间
     */
    private Date createTime;
}
