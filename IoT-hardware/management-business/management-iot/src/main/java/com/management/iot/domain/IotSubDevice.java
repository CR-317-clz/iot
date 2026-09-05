package com.management.iot.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName(value = "jy_management.iot_sub_device") // 请替换为实际的表名
public class IotSubDevice {

    // 主键
    private Long subId;

    // 设备名称
    private String subName;

    // 设备编号
    private String subCode;

    // 设备类型
    private String subType;

    // 设备状态
    private String status;

    // 设备所属设备ID
    private Long deviceId;

    // 设备IP
    private String host;

    // 创建时间
    private Date createTime;

    // 更新时间
    private Date updateTime;

    // 备注
    private String remark;
}