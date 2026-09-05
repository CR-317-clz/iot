package com.management.iot.mapper;

import com.management.iot.domain.IotSubDeviceType;

import java.util.List;

public interface IotSubDeviceTypeMapper {

    /**
     * 获取子设备类型列表
     * @return
     */
    List<IotSubDeviceType> selectAllSubDevicesType();

    /**
     * 根据子设备类型ID获取设备信息
     * @param subDeviceTypeId 子设备类型ID
     * @return 设备信息和操作结果
     */
    IotSubDeviceType getSubDeviceTypeInfo(Long subDeviceTypeId);

    /**
     * 保存子设备类型
     * @param subDeviceType 子设备类型
     * @return 保存结果
     */
    int saveSubDeviceType(IotSubDeviceType subDeviceType);

    /**
     * 修改子设备类型
     * @param subDeviceType 子设备类型
     * @return 保存结果
     */
    int updateSubDeviceType(IotSubDeviceType subDeviceType);
}
