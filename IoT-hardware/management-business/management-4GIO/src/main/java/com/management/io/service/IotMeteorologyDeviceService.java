package com.management.io.service;

import com.management.io.domain.IotMeteorologyDevice;

import java.util.List;

public interface IotMeteorologyDeviceService {

    /**
     * 根据气象站设备名称查询设备信息
     * @param deviceName 设备名称
     * @return
     */
    IotMeteorologyDevice getMeteorologyDeviceByDeviceName(String deviceName);

    /**
     * 新增气象站设备信息
     * @param iotMeteorologyDevice 设备信息
     */
    boolean insert(IotMeteorologyDevice iotMeteorologyDevice);

    /**
     * 修改气象站设备信息
     * @param meteorologyDevice 设备信息
     */
    boolean update(IotMeteorologyDevice meteorologyDevice);

    /**
     * 查询所有气象站设备信息
     * @return
     */
    List<IotMeteorologyDevice> getAllMeteorologyDevice();

}
