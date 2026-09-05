package com.management.iot.service;

import com.management.iot.domain.IotSubDevice;

import java.util.List;

public interface IotSubDeviceService {

    /**
     * 获取所有子设备列表
     * @return
     */
    List<IotSubDevice> getAllSubDevices();

    /**
     * 根据设备ID获取子设备列表
     * @param deviceId
     * @return
     */
    List<IotSubDevice> getSubDeviceById(Long deviceId);

    /**
     * 添加子设备
     * @param device
     * @return
     */
    boolean addSubDevice(IotSubDevice device);

    /**
     * 查询所有子设备IP
     * @return
     */
    List<String> findDistinctHosts();


    /**
     * 根据主机地址查询子设备
     * @param host
     * @return
     */
    List<IotSubDevice> findByHost(String host);

    /**
     * 根据主机地址更新子设备状态
     * @param host
     * @param status
     * @return
     */
    int updateStatusByHost(String host, String status);
}
