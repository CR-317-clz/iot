package com.management.web.controller.business.statistics;

import com.management.common.core.page.TableDataInfo;
import com.management.io.domain.IotMeteorologyRegisterData;
import com.management.io.mapper.IotMeteorologyRegisterDataMapper;
import com.management.iot.domain.dto.StatisticsTelemetryDataDto;
import com.management.io.domain.vo.StatisticsRainfallVo;
import com.management.iot.domain.vo.StatisticsTelemetryDataVo;
import com.management.iot.domain.vo.StatisticsTemperatureDataVo;
import com.management.iot.service.IotTelemetryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    private static final Logger logger = LoggerFactory.getLogger(StatisticsController.class);

    @Autowired
    private IotTelemetryService iotTelemetryService;

    @Autowired
    private IotMeteorologyRegisterDataMapper iotMeteorologyRegisterDataMapper;

    /**
     * 获取传感器数据统计列表
     *
     * @param statisticsTelemetryDataDto
     * @return
     */
    @PostMapping("/telemetry")
    public TableDataInfo StatisticsTelemetryData(@RequestBody StatisticsTelemetryDataDto statisticsTelemetryDataDto) {
        TableDataInfo info = new TableDataInfo();
        try {
            List<StatisticsTelemetryDataVo> devices = iotTelemetryService.getStatisticsTelemetryData(statisticsTelemetryDataDto);
            info.setCode(200);
            info.setRows(devices);
            info.setMsg("获取传感器数据统计列表成功");
            info.setTotal(devices.size());
            logger.info("获取传感器数据统计列表成功，共 {} 个设备", devices.size());
            return info;
        } catch (Exception e) {
            info.setCode(500);
            info.setMsg("获取传感器数据统计列表失败: " + e.getMessage());
            logger.error("获取传感器数据统计列表失败", e);
            return info;
        }
    }

    /**
     * 获取传感器温度数据统计列表
     *
     * @param deviceId
     * @return
     */
    @GetMapping("/temperature/{deviceId}")
    public TableDataInfo StatisticsTemperature(@PathVariable("deviceId") Long deviceId){
        TableDataInfo info = new TableDataInfo();
        try {
            List<StatisticsTemperatureDataVo> devices = iotTelemetryService.getStatisticsTemperatureData(deviceId);
            info.setCode(200);
            info.setRows(devices);
            info.setMsg("获取传感器温度数据统计列表成功");
            info.setTotal(devices.size());
            logger.info("获取传感器温度数据统计列表成功，共 {} 个设备", devices.size());
            return info;
        } catch (Exception e) {
            info.setCode(500);
            info.setMsg("获取传感器温度数据统计列表失败: " + e.getMessage());
            logger.error("获取传感器温度数据统计列表失败", e);
            return info;
        }
    }

    /**
     * 获取传感器湿度数据统计列表
     *
     * @param deviceId
     * @return
     */
    @GetMapping("/humidity/{deviceId}")
    public TableDataInfo StatisticsHumidity(@PathVariable("deviceId") Long deviceId){
        TableDataInfo info = new TableDataInfo();
        try {
            List<StatisticsTemperatureDataVo> devices = iotTelemetryService.getStatisticsHumidityData(deviceId);
            info.setCode(200);
            info.setRows(devices);
            info.setMsg("获取传感器湿度数据统计列表成功");
            info.setTotal(devices.size());
            logger.info("获取传感器湿度数据统计列表成功，共 {} 个设备", devices.size());
            return info;
        } catch (Exception e) {
            info.setCode(500);
            info.setMsg("获取传感器湿度数据统计列表失败: " + e.getMessage());
            logger.error("获取传感器湿度数据统计列表失败", e);
            return info;
        }
    }

    /**
     * 获取风力数据
     *
     * @param deviceId
     * @return
     */
    @GetMapping("/meteorology/latest/{deviceId}")
    public TableDataInfo getLatestMeteorologyData(@PathVariable("deviceId") Long deviceId){
        TableDataInfo info = new TableDataInfo();
        try {
            List<IotMeteorologyRegisterData> devices = iotTelemetryService.getMeteorologyRegisterData(deviceId);
            info.setCode(200);
            info.setRows(devices);
            info.setMsg("获取风力数据统计列表成功");
            info.setTotal(devices.size());
            logger.info("获取风力数据统计列表成功，共 {} 个设备", devices.size());
            return info;
        } catch (Exception e) {
            info.setCode(500);
            info.setMsg("获取风力数据统计列表失败: " + e.getMessage());
            logger.error("获取风力数据统计列表失败", e);
            return info;
        }
    }

    /**
     * 获取雨量数据
     *
     * @param deviceId
     * @return
     */
    @GetMapping("/rainfall/{deviceId}")
    public TableDataInfo getRainfall(@PathVariable("deviceId") Long deviceId){
        TableDataInfo info = new TableDataInfo();
        try {
            List<StatisticsRainfallVo> devices = iotMeteorologyRegisterDataMapper.getRainfallDataByDeviceId(deviceId);
            info.setCode(200);
            info.setRows(devices);
            info.setMsg("获取雨量数据统计列表成功");
            info.setTotal(devices.size());
            logger.info("获取雨量数据统计列表成功");
            return info;
        }catch (Exception e){
            info.setCode(500);
            info.setMsg("获取雨量数据统计列表失败: " + e.getMessage());
            return info;
        }
    }

}
