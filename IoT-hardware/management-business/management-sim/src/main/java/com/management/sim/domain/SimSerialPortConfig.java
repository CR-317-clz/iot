package com.management.sim.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName(value = "sim_serial_port_config")
public class SimSerialPortConfig {

    private Long serialId; // 主键

    private String equipmentConfigurationType; // 串口类型

    private String panId; // 产品ID

    private String channel; // 信道

    private String uniqueIdentification; // 唯一标识

    private Integer timeoutMs; // 超时时间

    private String autoReconnect; // 是否自动重连 0-否 1-是

    private Integer status; // 配置状态

    private Date createTime; // 创建时间

    private Date updateTime; // 修改时间
}