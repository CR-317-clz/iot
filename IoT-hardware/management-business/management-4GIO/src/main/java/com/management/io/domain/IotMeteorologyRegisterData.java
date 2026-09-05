package com.management.io.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("jy_management.iot_meteorology_register_data")
public class IotMeteorologyRegisterData {

    /**
     * 主键ID
     * 对应字段：data_id
     * 数据类型：int8（bigint）
     * 自动递增主键
     */
    private Long dataId;

    /**
     * 设备地址
     * 对应字段：device_addr
     * 数据类型：varchar(50)
     * 表示该数据所属的气象设备地址
     */
    private String deviceAddr;

    /**
     * 节点ID
     * 对应字段：node_id
     * 数据类型：int4（int）
     * 表示设备中的节点编号或通道号
     */
    private Long nodeId;

    /**
     * 寄存器ID
     * 对应字段：register_id
     * 数据类型：int4（int）
     * 用于标识该数据来自哪个寄存器
     */
    private Integer registerId;

    /**
     * 原始字符串值
     * 对应字段：data_value
     * 数据类型：varchar(100)
     * 原始设备上报的字符串值（如 “23.4℃”、“ON”、“ALARM” 等）
     */
    private String dataValue;

    /**
     * 数值数据
     * 对应字段：value_num
     * 数据类型：float8（double）
     * 转换后的数值类型（便于数值运算和趋势分析）
     */
    private Double valueNum;

    /**
     * 报警等级
     * 对应字段：alarm_level
     * 数据类型：int4（int）
     * 报警级别（0=无报警，1=一级报警，2=二级报警 等）
     */
    private Integer alarmLevel;

    /**
     * 报警颜色
     * 对应字段：alarm_color
     * 数据类型：varchar(20)
     * 用于前端展示报警颜色（如 “#FF0000”）
     */
    private String alarmColor;

    /**
     * 报警信息
     * 对应字段：alarm_info
     * 数据类型：varchar(255)
     * 报警详情描述（如 “风速过高”、“湿度异常”等）
     */
    private String alarmInfo;

    /**
     * 单位
     * 对应字段：unit
     * 数据类型：varchar(20)
     * 数据单位（如 “℃”、“m/s”、“%”）
     */
    private String unit;

    /**
     * 创建时间
     * 对应字段：create_time
     * 数据类型：timestamp(6)
     * 记录该条数据入库的时间
     */
    private Date createTime;

    /**
     * 寄存器名称
     * 对应字段：register_name
     * 数据类型：varchar(50)
     * 寄存器名称（如 “温度”、“湿度”、“风速”等）
     */
    private String registerName;

}
