package com.management.iot.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "iot_sub_type")
public class IotSubType {

    //子设备编号
    private Long subDeviceId;

    //子设备类型编号
    private Long subDeviceTypeId;

}
