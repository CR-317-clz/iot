package com.management.iot.mapper;

import com.management.iot.domain.IotDevice;
import com.management.iot.domain.IotSubDevice;

import java.util.List;

public interface IotSubDeviceMapper {

    /**
     * 查询所有子设备
     * @return
     */
    List<IotSubDevice> selectAllSubDevices();

    /**
     * 根据设备id查询子设备
     * @param deviceId
     * @return
     */
    List<IotSubDevice> selectSubDeviceById(Long deviceId);

    /**
     * 根据设备编码查询子设备
     * @param subCode
     **/
    IotDevice selectSubDeviceByCode(String subCode);

    /**
     * 添加子设备
     * @param device
     * @return
     */
    int insertSubDevice(IotSubDevice device);

    /**
     * 查询所有子设备IP
     * @return
     */
    List<String> findDistinctHosts();

    /**
     * 根据IP查询子设备
     * @param host
     * @return
     */
    List<IotSubDevice> findByHost(String host);

    /**
     * 根据IP更新子设备状态
     * @param host
     * @param status
     * @return
     */
    int updateStatusByHost(String host, String status);

}
