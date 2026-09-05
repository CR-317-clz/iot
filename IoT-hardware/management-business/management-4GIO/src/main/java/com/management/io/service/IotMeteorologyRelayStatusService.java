package com.management.io.service;

import com.management.io.domain.IotMeteorologyRelayStatus;

public interface IotMeteorologyRelayStatusService {

    /**
     * 根据设备地址查询最新数据
     * @param deviceAddr 设备地址
     * @return
     */
    IotMeteorologyRelayStatus selectLatestByDeviceAdr(String deviceAddr);

    /**
     * 插入数据
     * @param relay
     */
    boolean insert(IotMeteorologyRelayStatus relay);

    /**
     * 更新数据
     * @param relay
     */
    boolean update(IotMeteorologyRelayStatus relay);

    /**
     * 根据设备地址和继电器编号查询最新数据
     * @param valueOf
     * @param relayNo
     * @return
     */
    IotMeteorologyRelayStatus selectLatestByDeviceAdrAndRelayNo(String valueOf, Integer relayNo);
}
