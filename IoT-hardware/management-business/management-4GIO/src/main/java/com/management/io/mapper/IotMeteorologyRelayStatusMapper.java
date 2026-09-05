package com.management.io.mapper;

import com.management.io.domain.IotMeteorologyRelayStatus;
import org.apache.ibatis.annotations.Param;

public interface IotMeteorologyRelayStatusMapper {

    /**
     * 根据设备地址查询最新的数据
     * @param deviceAddr 设备地址
     * @return
     */
    IotMeteorologyRelayStatus selectLatestByDeviceAdr(String deviceAddr);

    /**
     * 插入数据
     * @param relay
     * @return
     */
    int insertMeteorologyRelayStatus(IotMeteorologyRelayStatus relay);

    /**
     * 更新数据
     * @param relay
     * @return
     */
    int updateMeteorologyRelayStatus(IotMeteorologyRelayStatus relay);

    /**
     * 根据设备地址和继电器编号查询最新的数据
     * @param valueOf 设备地址
     * @param relayNo 继电器编号
     * @return
     */
    IotMeteorologyRelayStatus selectLatestByDeviceAdrAndRelayNo(@Param("valueOf") String valueOf, @Param("relayNo") Integer relayNo);
}
