package com.management.iot.mapper;

import com.management.io.domain.IotMeteorologyRegisterData;
import com.management.iot.domain.IotTelemetry;
import com.management.iot.domain.dto.StatisticsTelemetryDataDto;
import com.management.iot.domain.vo.StatisticsTelemetryDataVo;
import com.management.iot.domain.vo.StatisticsTemperatureDataVo;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 设备时序数据数据访问层接口
 */
@Mapper
public interface IotTelemetryMapper {
    
    /**
     * 查询所有时序数据
     * @return 时序数据列表
     */
    List<IotTelemetry> selectAllTelemetry();
    
    /**
     * 根据设备ID查询时序数据
     * @param deviceId 设备ID
     * @return 时序数据列表
     */
    List<IotTelemetry> selectTelemetryByDeviceId(@Param("deviceId") Long deviceId);
    
    /**
     * 根据数据键名查询时序数据
     * @param dataKey 数据键名
     * @return 时序数据列表
     */
    List<IotTelemetry> selectTelemetryByDataKey(@Param("dataKey") String dataKey);
    
    /**
     * 插入新时序数据记录
     * @param telemetry 时序数据实体对象
     * @return 插入记录数
     */
    int insertTelemetry(IotTelemetry telemetry);
    
    /**
     * 批量插入时序数据记录
     * @param telemetryList 时序数据列表
     * @return 插入记录数
     */
    int batchInsertTelemetry(@Param("telemetryList") List<IotTelemetry> telemetryList);
    
    /**
     * 根据设备ID和时间范围查询时序数据
     * @param deviceId 设备ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 时序数据列表
     */
    List<IotTelemetry> selectTelemetryByTimeRange(@Param("deviceId") Long deviceId,
                                                 @Param("startTime") String startTime, 
                                                 @Param("endTime") String endTime);
    
    /**
     * 根据设备ID删除时序数据
     * @param deviceId 设备ID
     * @return 删除记录数
     */
    int deleteTelemetryByDeviceId(@Param("deviceId") Integer deviceId);
    
    /**
     * 获取设备的最新数据
     * @param deviceId 设备ID
     * @return 最新时序数据列表
     */
    List<IotTelemetry> selectLatestTelemetryByDeviceId(@Param("deviceId") Long deviceId);

    /**
     * 获取设备数据统计信息
     * @param statisticsTelemetryDataDto 统计参数
     * @return 统计结果列表
     */
    List<StatisticsTelemetryDataVo> getStatisticsTelemetryData(StatisticsTelemetryDataDto statisticsTelemetryDataDto);

    /**
     * 获取设备温度数据统计信息
     * @param deviceId 设备ID
     * @return 统计结果列表
     */
    List<StatisticsTemperatureDataVo> getStatisticsTemperatureData(Long deviceId);

    /**
     * 获取设备湿度数据统计信息
     * @param deviceId 设备ID
     * @return 统计结果列表
     */
    List<StatisticsTemperatureDataVo> getStatisticsHumidityData(Long deviceId);

    List<IotMeteorologyRegisterData> getMeteorologyRegisterData(Long deviceId);

}