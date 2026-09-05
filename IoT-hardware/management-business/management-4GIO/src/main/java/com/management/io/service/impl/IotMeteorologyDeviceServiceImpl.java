package com.management.io.service.impl;

import com.management.common.utils.snowFlake.SnowflakeIdGenerator;
import com.management.io.domain.IotMeteorologyDevice;
import com.management.io.mapper.IotMeteorologyDeviceMapper;
import com.management.io.service.IotMeteorologyDeviceService;
import com.management.io.service.IotMeteorologyRegisterDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IotMeteorologyDeviceServiceImpl implements IotMeteorologyDeviceService {

    @Autowired
    private IotMeteorologyDeviceMapper iotMeteorologyDeviceMapper;

    @Autowired
    private IotMeteorologyRegisterDataService iotMeteorologyRegisterDataService;

    /**
     * 根据气象站设备名称查询设备信息
     * @param deviceName 设备名称
     * @return
     */
    @Override
    public IotMeteorologyDevice getMeteorologyDeviceByDeviceName(String deviceName) {
        if (deviceName == null) {
            throw new IllegalArgumentException("设备信息不能为空");
        }
        try {
            IotMeteorologyDevice device = iotMeteorologyDeviceMapper.selectMeteorologyDeviceByDeviceName(deviceName);

            return device;
        } catch (Exception e) {
            throw new RuntimeException("获取气象站设备信息失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean insert(IotMeteorologyDevice iotMeteorologyDevice) {
        try {
            // 检查设备编码是否已存在
            IotMeteorologyDevice existingDevice = iotMeteorologyDeviceMapper.selectMeteorologyDeviceByDeviceName(iotMeteorologyDevice.getDeviceName());
            if (existingDevice != null) {
                throw new RuntimeException("设备名称已存在: " + iotMeteorologyDevice.getDeviceName());
            }

            iotMeteorologyDevice.setDeviceId(SnowflakeIdGenerator.nextId());
            int result = iotMeteorologyDeviceMapper.insertMeteorologyDevice(iotMeteorologyDevice);
            return result > 0;
        } catch (Exception e) {
            throw new RuntimeException("添加气象站设备失败: " + e.getMessage(), e);
        }
    }

    /**
     * 修改气象站设备信息
     * @param meteorologyDevice 设备信息
     * @return
     */
    @Override
    public boolean update(IotMeteorologyDevice meteorologyDevice) {
        try {
            int result = iotMeteorologyDeviceMapper.updateMeteorologyDevice(meteorologyDevice);
            return result > 0;
        } catch (Exception e) {
            throw new RuntimeException("修改气象站设备失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取所有气象站设备信息
     * @return
     */
    @Override
    public List<IotMeteorologyDevice> getAllMeteorologyDevice() {
        try {

            List<IotMeteorologyDevice> meteorologyDevices = iotMeteorologyDeviceMapper.getAllMeteorologyDevice();

            for (IotMeteorologyDevice meteorologyDevice : meteorologyDevices) {
                if (meteorologyDevice.getDeviceStatus().equals("offline")){
                    meteorologyDevice.setRegisterDataList(null);
                }else if (meteorologyDevice.getDeviceStatus().equals("online")){

                    meteorologyDevice.setRegisterDataList(iotMeteorologyRegisterDataService.iotMeteorologyRegisterDataByAddress(meteorologyDevice.getDeviceAddr()));
                }
            }

            return meteorologyDevices;
        } catch (Exception e) {
            throw new RuntimeException("获取气象站设备列表失败: " + e.getMessage(), e);
        }
    }
}
