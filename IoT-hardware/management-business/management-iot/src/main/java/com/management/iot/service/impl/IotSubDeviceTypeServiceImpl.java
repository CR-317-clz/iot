package com.management.iot.service.impl;

import com.management.common.utils.snowFlake.SnowflakeIdGenerator;
import com.management.iot.domain.IotDevice;
import com.management.iot.domain.IotSubDeviceType;
import com.management.iot.mapper.IotSubDeviceTypeMapper;
import com.management.iot.service.IotSubDeviceTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IotSubDeviceTypeServiceImpl implements IotSubDeviceTypeService {

    @Autowired
    private IotSubDeviceTypeMapper subDeviceTypeMapper;

    /**
     * 获取子设备类型列表
     * @return 子设备类型列表和操作结果
     */
    @Override
    public List<IotSubDeviceType> getAllSubDevicesType() {
        try {
            List<IotSubDeviceType> subDeviceTypes = subDeviceTypeMapper.selectAllSubDevicesType();
            return subDeviceTypes;
        } catch (Exception e) {
            throw new RuntimeException("获取子设备类型列表失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据子设备类型ID获取设备信息
     * @param subDeviceTypeId 子设备类型ID
     * @return 设备信息和操作结果
     */
    @Override
    public IotSubDeviceType getSubDeviceTypeInfo(Long subDeviceTypeId) {
        try {
            IotSubDeviceType subDeviceType = subDeviceTypeMapper.getSubDeviceTypeInfo(subDeviceTypeId);
            return subDeviceType;
        } catch (Exception e) {
            throw new RuntimeException("获取子设备类型信息失败: " + e.getMessage(), e);
        }
    }
    /**
     * 添加子设备类型
     * @param subDeviceType 子设备类型
     * @return 操作结果
     */
    @Override
    public boolean saveSubDeviceType(IotSubDeviceType subDeviceType) {
        if (subDeviceType == null) {
            throw new IllegalArgumentException("子设备类型信息不能为空");
        }
        if (subDeviceType.getSubDeviceTypeName() == null || subDeviceType.getSubDeviceTypeName().trim().isEmpty()) {
            throw new IllegalArgumentException("子设备类型名称不能为空");
        }
        try {
            subDeviceType.setSubDeviceTypeId(SnowflakeIdGenerator.nextId());
            if (subDeviceType.getSubDeviceParentId() == null){
                subDeviceType.setSubDeviceParentId(0L);
            }
            int result = subDeviceTypeMapper.saveSubDeviceType(subDeviceType);
            return result > 0;
        } catch (Exception e) {
            throw new RuntimeException("添加子设备类型信息失败: " + e.getMessage(), e);
        }
    }

    /**
     * 修改子设备类型
     * @param subDeviceType 子设备类型
     * @return 操作结果
     */
    @Override
    public boolean updateSubDeviceType(IotSubDeviceType subDeviceType) {
        if (subDeviceType == null) {
            throw new IllegalArgumentException("子设备类型信息不能为空");
        }
        try {
            int result = subDeviceTypeMapper.updateSubDeviceType(subDeviceType);
            return result > 0;
        } catch (Exception e) {
            throw new RuntimeException("修改子设备类型信息失败: " + e.getMessage(), e);
        }
    }
}
