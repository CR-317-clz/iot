package com.management.iot.service;

import com.management.iot.domain.IotSubDeviceType;

import java.util.List;

public interface IotSubDeviceTypeService {

    /**
     * 获取子设备类型列表
     * @return
     */
    List<IotSubDeviceType> getAllSubDevicesType();

    /**
     * 根据子设备类型ID获取设备信息
     * @param subDeviceTypeId
     * @return
     */
    IotSubDeviceType getSubDeviceTypeInfo(Long subDeviceTypeId);

    /**
     * 保存子设备类型
     * @param subDeviceType
     * @return
     */
    boolean saveSubDeviceType(IotSubDeviceType subDeviceType);

    /**
     * 修改子设备类型
     * @param subDeviceType 子设备类型
     * @return 操作结果
     */
    boolean updateSubDeviceType(IotSubDeviceType subDeviceType);
}
