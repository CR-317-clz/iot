package com.management.iot.service.impl;

import com.management.common.utils.snowFlake.SnowflakeIdGenerator;
import com.management.iot.domain.IotDevice;
import com.management.iot.domain.IotSubDevice;
import com.management.iot.mapper.IotSubDeviceMapper;
import com.management.iot.service.IotSubDeviceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class IotSubDeviceServiceImpl implements IotSubDeviceService {

    @Autowired
    private IotSubDeviceMapper iotSubDeviceMapper;

    /**
     * 获取所有子设备列表
     *
     * @return
     */
    @Override
    public List<IotSubDevice> getAllSubDevices() {
        try {
            return iotSubDeviceMapper.selectAllSubDevices();
        } catch (Exception e) {
            throw new RuntimeException("获取子设备列表失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<IotSubDevice> getSubDeviceById(Long deviceId) {
        if (deviceId == null || deviceId <= 0) {
            throw new IllegalArgumentException("父设备ID不能为空且必须大于0");
        }
        try {
            List<IotSubDevice> device = iotSubDeviceMapper.selectSubDeviceById(deviceId);
            if (device == null) {
                throw new RuntimeException("子设备不存在，设备ID: " + deviceId);
            }
            return device;
        } catch (Exception e) {
            throw new RuntimeException("获取子设备信息失败: " + e.getMessage(), e);
        }
    }

    /**
     * 添加新子设备
     * @param device
     * @return
     */
    @Override
    public boolean addSubDevice(IotSubDevice device) {
        if (device == null) {
            throw new IllegalArgumentException("子设备信息不能为空");
        }
        if (device.getSubName() == null || device.getSubName().trim().isEmpty()) {
            throw new IllegalArgumentException("子设备名称不能为空");
        }
        if (device.getSubCode() == null || device.getSubCode().trim().isEmpty()) {
            throw new IllegalArgumentException("子设备编码不能为空");
        }
        try {
            // 检查设备编码是否已存在
            IotDevice existingDevice = iotSubDeviceMapper.selectSubDeviceByCode(device.getSubCode());
            if (existingDevice != null) {
                throw new RuntimeException("子设备编码已存在: " + device.getSubCode());
            }

            device.setSubId(SnowflakeIdGenerator.nextId());
            int result = iotSubDeviceMapper.insertSubDevice(device);
            return result > 0;
        } catch (Exception e) {
            throw new RuntimeException("添加子设备失败: " + e.getMessage(), e);
        }
    }

    /**
     * 查询所有子设备IP地址
     * @return
     */
    @Override
    public List<String> findDistinctHosts() {
        try {
            return iotSubDeviceMapper.findDistinctHosts();
        } catch (Exception e) {
            throw new RuntimeException("获取子设备IP地址列表失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据IP地址查询子设备列表
     * @param host
     * @return
     */
    @Override
    public List<IotSubDevice> findByHost(String host) {
        try {
            return iotSubDeviceMapper.findByHost(host);
        } catch (Exception e) {
            throw new RuntimeException("获取子设备IP地址列表失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据IP地址更新子设备状态
     * @param host
     * @param status
     * @return
     */
    @Override
    public int updateStatusByHost(String host, String status) {

        try {
            return iotSubDeviceMapper.updateStatusByHost(host, status);
        } catch (Exception e) {
            throw new RuntimeException("更新子设备状态失败: " + e.getMessage(), e);
        }
    }

}
