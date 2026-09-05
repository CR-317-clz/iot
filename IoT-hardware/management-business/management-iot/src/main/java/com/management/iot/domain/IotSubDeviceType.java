package com.management.iot.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "iot_sub_device_type")
public class IotSubDeviceType {

    //子设备类型编号
    private Long subDeviceTypeId;

    //父级子设备类型编号
    private Long subDeviceParentId;

    //子设备类型名称
    private String subDeviceTypeName;

    //使用状态（0-未启用 1-已启用 2-已弃用）
    private Integer status;

    //逻辑删除
    private Integer delFlag;

    //创建时间
    private Date createTime;

    //修改时间
    private Date updateTime;

}
