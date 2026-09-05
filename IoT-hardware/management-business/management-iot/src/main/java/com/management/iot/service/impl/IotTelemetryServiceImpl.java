package com.management.iot.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.management.common.utils.DataUnitUtil;
import com.management.io.domain.IotMeteorologyRegisterData;
import com.management.iot.domain.IotDevice;
import com.management.iot.domain.IotMessage;
import com.management.iot.domain.IotTelemetry;
import com.management.iot.domain.dto.StatisticsTelemetryDataDto;
import com.management.iot.domain.vo.StatisticsTelemetryDataVo;
import com.management.iot.domain.vo.StatisticsTemperatureDataVo;
import com.management.iot.mapper.IotDeviceMapper;
import com.management.iot.mapper.IotTelemetryMapper;
import com.management.iot.service.IotTelemetryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 设备时序数据服务实现类
 * 实现时序数据相关的业务逻辑
 */
@Service
public class IotTelemetryServiceImpl implements IotTelemetryService {

    private static final Logger logger = LoggerFactory.getLogger(IotTelemetryServiceImpl.class);
    
    @Autowired
    private IotTelemetryMapper telemetryMapper;
    
    @Autowired
    private IotDeviceMapper deviceMapper;
    
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 获取所有时序数据
     * @return 时序数据列表
     */
    @Override
    public List<IotTelemetry> getAllTelemetry() {
        try {
            return telemetryMapper.selectAllTelemetry();
        } catch (Exception e) {
            logger.error("获取时序数据列表失败", e);
            throw new RuntimeException("获取时序数据列表失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据设备ID获取时序数据
     * @param deviceId 设备ID
     * @return 时序数据列表
     */
    @Override
    public List<IotTelemetry> getTelemetryByDeviceId(Long deviceId) {
        if (deviceId == null || deviceId <= 0) {
            throw new IllegalArgumentException("设备ID不能为空且必须大于0");
        }
        try {
            List<IotTelemetry> iotTelemetries = telemetryMapper.selectTelemetryByDeviceId(deviceId);
            return iotTelemetries;
        } catch (Exception e) {
            logger.error("根据设备ID获取时序数据失败，设备ID: {}", deviceId, e);
            throw new RuntimeException("根据设备ID获取时序数据失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据数据键名获取时序数据
     * @param dataKey 数据键名
     * @return 时序数据列表
     */
    @Override
    public List<IotTelemetry> getTelemetryByDataKey(String dataKey) {
        if (dataKey == null || dataKey.trim().isEmpty()) {
            throw new IllegalArgumentException("数据键名不能为空");
        }
        try {
            return telemetryMapper.selectTelemetryByDataKey(dataKey.trim());
        } catch (Exception e) {
            logger.error("根据数据键名获取时序数据失败，数据键名: {}", dataKey, e);
            throw new RuntimeException("根据数据键名获取时序数据失败: " + e.getMessage(), e);
        }
    }

    /**
     * 保存时序数据记录
     * @param telemetry 时序数据信息
     * @return 是否保存成功
     */
    @Override
    @Transactional
    public boolean saveTelemetry(IotTelemetry telemetry) {
        if (telemetry == null) {
            throw new IllegalArgumentException("时序数据信息不能为空");
        }
        if (telemetry.getDeviceId() == null || telemetry.getDeviceId() <= 0) {
            throw new IllegalArgumentException("设备ID不能为空且必须大于0");
        }
        if (telemetry.getDataKey() == null || telemetry.getDataKey().trim().isEmpty()) {
            throw new IllegalArgumentException("数据键名不能为空");
        }
        
        try {
            // 检查设备是否存在
            IotDevice device = deviceMapper.selectDeviceById(telemetry.getDeviceId());
            if (device == null) {
                throw new RuntimeException("设备不存在，设备ID: " + telemetry.getDeviceId());
            }
            
            int result = telemetryMapper.insertTelemetry(telemetry);
            boolean success = result > 0;
            
            if (success) {
                logger.debug("成功保存时序数据记录，数据ID: {}, 设备ID: {}, 数据键: {}", 
                           telemetry.getTelemetryId(), telemetry.getDeviceId(), telemetry.getDataKey());
            }
            
            return success;
        } catch (Exception e) {
            logger.error("保存时序数据记录失败，设备ID: {}, 数据键: {}", telemetry.getDeviceId(), telemetry.getDataKey(), e);
            throw new RuntimeException("保存时序数据记录失败: " + e.getMessage(), e);
        }
    }

    /**
     * 批量保存时序数据记录
     * @param telemetryList 时序数据列表
     * @return 是否保存成功
     */
    @Override
    @Transactional
    public boolean batchSaveTelemetry(List<IotTelemetry> telemetryList) {
        if (telemetryList == null || telemetryList.isEmpty()) {
            throw new IllegalArgumentException("时序数据列表不能为空");
        }
        
        try {
            // 验证所有设备ID和数据键名
            for (IotTelemetry telemetry : telemetryList) {
                if (telemetry.getDeviceId() == null || telemetry.getDeviceId() <= 0) {
                    throw new IllegalArgumentException("设备ID不能为空且必须大于0");
                }
                if (telemetry.getDataKey() == null || telemetry.getDataKey().trim().isEmpty()) {
                    throw new IllegalArgumentException("数据键名不能为空");
                }
                
                // 检查设备是否存在
                IotDevice device = deviceMapper.selectDeviceById(telemetry.getDeviceId());
                if (device == null) {
                    throw new RuntimeException("设备不存在，设备ID: " + telemetry.getDeviceId());
                }
            }
            
            int result = telemetryMapper.batchInsertTelemetry(telemetryList);
            boolean success = result > 0;
            
            if (success) {
                logger.info("批量保存时序数据记录成功，共 {} 条记录", telemetryList.size());
            }
            
            return success;
        } catch (Exception e) {
            logger.error("批量保存时序数据记录失败", e);
            throw new RuntimeException("批量保存时序数据记录失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据时间范围查询时序数据
     * @param deviceId 设备ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 时序数据列表
     */
    @Override
    public List<IotTelemetry> getTelemetryByTimeRange(Long deviceId, String startTime, String endTime) {
        if (deviceId == null || deviceId <= 0) {
            throw new IllegalArgumentException("设备ID不能为空且必须大于0");
        }
        if (startTime == null || startTime.trim().isEmpty()) {
            throw new IllegalArgumentException("开始时间不能为空");
        }
        if (endTime == null || endTime.trim().isEmpty()) {
            throw new IllegalArgumentException("结束时间不能为空");
        }
        
        try {
            return telemetryMapper.selectTelemetryByTimeRange(deviceId, startTime.trim(), endTime.trim());
        } catch (Exception e) {
            logger.error("根据时间范围查询时序数据失败，设备ID: {}, 时间范围: {} - {}", deviceId, startTime, endTime, e);
            throw new RuntimeException("根据时间范围查询时序数据失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取设备的最新数据
     * @param deviceId 设备ID
     * @return 最新时序数据列表
     */
    @Override
    public List<IotTelemetry> getLatestTelemetryByDeviceId(Long deviceId) {
        if (deviceId == null || deviceId <= 0) {
            throw new IllegalArgumentException("设备ID不能为空且必须大于0");
        }
        try {
            return telemetryMapper.selectLatestTelemetryByDeviceId(deviceId);
        } catch (Exception e) {
            logger.error("获取设备最新数据失败，设备ID: {}", deviceId, e);
            throw new RuntimeException("获取设备最新数据失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析消息并保存时序数据
     * @param message 原始消息
     * @return 是否解析成功
     */
    @Override
    @Transactional
    public boolean parseAndSaveTelemetry(IotMessage message) {
        if (message == null) {
            throw new IllegalArgumentException("消息不能为空");
        }
        if (message.getPayload() == null || message.getPayload().trim().isEmpty()) {
            logger.warn("消息内容为空，无法解析，消息ID: {}", message.getMsgId());
            return false;
        }
        
        try {
            String payload = message.getPayload().trim();
            List<IotTelemetry> telemetryList = new ArrayList<>();
            
            // 尝试解析JSON格式的消息
            if (payload.startsWith("{") && payload.endsWith("}")) {
                telemetryList = parseJsonPayload(message.getDeviceId(), payload);
            } else {
                // 可以扩展其他格式的解析，如CSV、XML等
                logger.warn("不支持的消息格式，消息ID: {}", message.getMsgId());
                return false;
            }
            
            if (!telemetryList.isEmpty()) {
                return batchSaveTelemetry(telemetryList);
            } else {
                logger.warn("解析消息未产生时序数据，消息ID: {}", message.getMsgId());
                return false;
            }
            
        } catch (Exception e) {
            logger.error("解析消息失败，消息ID: {}", message.getMsgId(), e);
            return false;
        }
    }

    /**
     * 根据传感器数据统计数据
     * @param statisticsTelemetryDataDto 统计数据参数
     * @return 统计数据列表
     */
    @Override
    public List<StatisticsTelemetryDataVo> getStatisticsTelemetryData(StatisticsTelemetryDataDto statisticsTelemetryDataDto) {
        try {
            return telemetryMapper.getStatisticsTelemetryData(statisticsTelemetryDataDto);
        } catch (Exception e) {
            throw new RuntimeException("根据传感器数据统计数据失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据传感器数据统计温度数据
     * @param deviceId 设备ID
     * @return 统计数据列表
     */
    @Override
    public List<StatisticsTemperatureDataVo> getStatisticsTemperatureData(Long deviceId) {
        try {
            return telemetryMapper.getStatisticsTemperatureData(deviceId);
        } catch (Exception e) {
            throw new RuntimeException("根据传感器数据统计数据失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据传感器数据统计湿度数据
     * @param deviceId 设备ID
     * @return 统计数据列表
     */
    @Override
    public List<StatisticsTemperatureDataVo> getStatisticsHumidityData(Long deviceId) {
        try {
            return telemetryMapper.getStatisticsHumidityData(deviceId);
        } catch (Exception e) {
            throw new RuntimeException("根据传感器数据统计数据失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<IotMeteorologyRegisterData> getMeteorologyRegisterData(Long deviceId) {
        try {
            return telemetryMapper.getMeteorologyRegisterData(deviceId);
        } catch (Exception e) {
            throw new RuntimeException("根据传感器数据统计数据失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析JSON格式的消息负载
     * @param deviceId 设备ID
     * @param payload JSON消息内容
     * @return 时序数据列表
     */
    private List<IotTelemetry> parseJsonPayload(Long deviceId, String payload) {
        List<IotTelemetry> telemetryList = new ArrayList<>();
        
        try {
            JsonNode rootNode = objectMapper.readTree(payload);
            
            // 遍历JSON中的所有字段
            rootNode.fields().forEachRemaining(entry -> {
                String dataKey = entry.getKey();
                JsonNode valueNode = entry.getValue();
                
                // 根据值类型创建时序数据
                if (valueNode.isNumber()) {
                    IotTelemetry telemetry = new IotTelemetry();
                    telemetry.setDeviceId(deviceId);
                    telemetry.setDataKey(dataKey);
                    telemetry.setDataValue(valueNode.asDouble());
                    telemetry.setUnit(DataUnitUtil.getUnit(dataKey));
                    telemetryList.add(telemetry);
                } else if (valueNode.isBoolean()) {
                    // 布尔值转换为数字 (true=1, false=0)
                    IotTelemetry telemetry = new IotTelemetry();
                    telemetry.setDeviceId(deviceId);
                    telemetry.setDataKey(dataKey);
                    telemetry.setDataValue(valueNode.asBoolean() ? 1.0 : 0.0);
                    telemetry.setUnit("bool");
                    telemetryList.add(telemetry);
                }
                // 可以扩展其他数据类型的处理
            });
            
            logger.debug("解析JSON负载成功，设备ID: {}, 生成 {} 条时序数据", deviceId, telemetryList.size());
            
        } catch (Exception e) {
            logger.error("解析JSON负载失败，设备ID: {}, 负载: {}", deviceId, payload, e);
            throw new RuntimeException("解析JSON负载失败: " + e.getMessage(), e);
        }
        
        return telemetryList;
    }
}