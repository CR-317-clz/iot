package com.management.io.mapper;

import com.management.io.domain.IotMeteorologyDevice;

import java.util.List;

public interface IotMeteorologyDeviceMapper {

    /**
     * 根据气象站设备名称查询设备信息
     * @param deviceName 设备名称
     * @return
     */
    IotMeteorologyDevice selectMeteorologyDeviceByDeviceName(String deviceName);

    /**
     * 新增气象站设备信息
     * @param iotMeteorologyDevice 设备信息
     * @return
     */
    int insertMeteorologyDevice(IotMeteorologyDevice iotMeteorologyDevice);

    /**
     * 修改气象站设备信息
     * @param meteorologyDevice 设备信息
     * @return
     */
    int updateMeteorologyDevice(IotMeteorologyDevice meteorologyDevice);

    /**
     * 获取所有气象站设备信息
     * @return
     */
    List<IotMeteorologyDevice> getAllMeteorologyDevice();

}
