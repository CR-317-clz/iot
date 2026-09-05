package com.management.io.mapper;

import com.management.io.domain.IotMeteorologyRegisterData;
import com.management.io.domain.dto.StatisticsOfTemperatureAndHumidityDto;
import com.management.io.domain.vo.StatisticsOfTemperatureAndHumidityVo;
import com.management.io.domain.vo.StatisticsRainfallVo;

import java.util.List;

public interface IotMeteorologyRegisterDataMapper {

    /**
     * 插入测点数据
     *
     * @param registerData
     * @return
     */
    int insertMeteorologyRegisterData(IotMeteorologyRegisterData registerData);

    /**
     * 根据设备ID查询测点数据
     *
     * @param deviceId
     * @return
     */
    List<IotMeteorologyRegisterData> getMeteorologyRegisterDataByDeviceId(Long deviceId);

    /**
     * 获取指定设备下的温湿度测点数据
     *
     * @param deviceId 设备ID
     * @return 测点数据列表
     */
    List<StatisticsOfTemperatureAndHumidityVo> getStatisticsOfTemperatureAndHumidity(String deviceId);

    /**
     * 根据设备地址查询测点数据
     *
     * @param deviceAddr 设备地址
     * @return 测点数据列表
     */
    List<IotMeteorologyRegisterData> iotMeteorologyRegisterDataByAddress(String deviceAddr);

    /**
     * 根据设备地址查询测点数据
     *
     * @param deviceAddr 设备地址
     * @return 测点数据列表
     */
    List<IotMeteorologyRegisterData> SelectIotMeteorologyRegisterDataByAddress(String deviceAddr);

    /**
     * 根据设备ID查询测点数据
     *
     * @param deviceId 设备ID
     * @return 测点数据列表
     */
    List<IotMeteorologyRegisterData> getMeteorologyRegisterData(Long deviceId);

    /**
     * 根据设备ID查询测点数据
     *
     * @param deviceId 设备ID
     * @return 测点数据列表
     */
    List<StatisticsRainfallVo> getRainfallDataByDeviceId(Long deviceId);
}
