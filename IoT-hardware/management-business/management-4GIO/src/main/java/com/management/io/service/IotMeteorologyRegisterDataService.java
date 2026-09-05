package com.management.io.service;

import com.management.io.domain.IotMeteorologyRegisterData;
import com.management.io.domain.dto.StatisticsOfTemperatureAndHumidityDto;

import java.util.List;

public interface IotMeteorologyRegisterDataService {

    /**
     * 插入数据
     *
     * @param registerData 数据
     */
    boolean insert(IotMeteorologyRegisterData registerData);

    /**
     * 根据设备ID获取测点数据
     *
     * @param deviceId 设备ID
     * @return 测点数据列表
     */
    List<IotMeteorologyRegisterData> getMeteorologyRegisterDataByDeviceId(Long deviceId);

    /**
     * 获取指定设备下的温度和湿度数据
     *
     * @param deviceId 设备ID
     * @return 测点数据列表
     */
    List<StatisticsOfTemperatureAndHumidityDto> getStatisticsOfTemperatureAndHumidity(String deviceId);

    /**
     * 更新气象站数据
     *
     * @return 是否更新成功
     */
    boolean updateMeteorologyApiData();

    /**
     * 根据地址查询数据
     * @param deviceAddr 地址
     * @return
     * */
    List<IotMeteorologyRegisterData> iotMeteorologyRegisterDataByAddress(String deviceAddr);

    /**
     * 根据地址查询数据
     * @param deviceAddr 地址
     * @return
     * */
    List<IotMeteorologyRegisterData> SelectIotMeteorologyRegisterDataByAddress(String deviceAddr);
}
