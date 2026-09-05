package com.management.web.controller.business.io4G;

import com.management.common.core.page.TableDataInfo;
import com.management.io.domain.IotMeteorologyRegisterData;
import com.management.io.domain.dto.StatisticsOfTemperatureAndHumidityDto;
import com.management.io.service.IotMeteorologyRegisterDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Description: 气象站数据控制层
 * @Author: xlsky
 * @Date: 2019/1/7 11:05
 * @param: null
 * @return:
 */
@RestController
@RequestMapping("/meteorology_data")
public class IotMeteorologyRegisterDataController {

    private static final Logger logger = LoggerFactory.getLogger(IotMeteorologyRegisterDataController.class);

    @Autowired
    private IotMeteorologyRegisterDataService iotMeteorologyRegisterDataService;


    /**
     * 获取指定设备下的温湿度测点数据
     *
     * @param deviceAddr 设备ID
     * @return 测点数据列表
     */
    @RequestMapping("/statistics/{deviceAddr}")
    public TableDataInfo statisticsOfTemperatureAndHumidity(@PathVariable String deviceAddr){
        TableDataInfo result = new TableDataInfo();
        try {
            List<StatisticsOfTemperatureAndHumidityDto> statisticsOfTemperatureAndHumidityDto = iotMeteorologyRegisterDataService.getStatisticsOfTemperatureAndHumidity(deviceAddr);
            result.setCode(200);
            result.setRows(statisticsOfTemperatureAndHumidityDto);
            result.setMsg("获取气象统计数据信息成功");
            result.setTotal(statisticsOfTemperatureAndHumidityDto.size());
            logger.info("获取气象统计数据信息成功，设备ID: {}", deviceAddr);
            return result;
        } catch (Exception e) {
            result.setCode(500);
            result.setMsg("获取气象统计数据失败");
            logger.error("获取气象统计数据失败，设备ID: {}", deviceAddr, e);
            return result;
        }
    }


    /**
     * 根据地址查询数据
     * @param deviceAddr 地址
     * @return
     * */
    @GetMapping("/meteorology_register_data/{deviceAddr}")
    public TableDataInfo meteorologyRegisterDataList(@PathVariable String deviceAddr){
        TableDataInfo result = new TableDataInfo();
        try {
            List<IotMeteorologyRegisterData> iotMeteorologyRegisterDataList = iotMeteorologyRegisterDataService.SelectIotMeteorologyRegisterDataByAddress(deviceAddr);
            result.setCode(200);
            result.setRows(iotMeteorologyRegisterDataList);
            result.setMsg("获取气象数据信息成功");
            result.setTotal(iotMeteorologyRegisterDataList.size());
            logger.info("获取气象数据信息成功，设备地址ID: {}", deviceAddr);
            return result;
        } catch (Exception e) {
            result.setCode(500);
            result.setMsg("获取气象数据失败");
            logger.error("获取气象数据失败，设备地址ID: {}", deviceAddr, e);
            return result;
        }
    }

}
