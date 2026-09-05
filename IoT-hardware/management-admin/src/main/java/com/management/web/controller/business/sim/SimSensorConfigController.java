package com.management.web.controller.business.sim;

import com.management.common.core.page.TableDataInfo;
import com.management.sim.domain.SimSensorConfig;
import com.management.sim.domain.SimSerialPortConfig;
import com.management.sim.service.SimSensorConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/sensor_config")
public class SimSensorConfigController {

    private static final Logger logger = LoggerFactory.getLogger(SimSensorConfigController.class);

    @Autowired
    private SimSensorConfigService simSensorConfigService;

    @GetMapping("/list_all")
    public TableDataInfo getAllSerialPortConfig() {
        TableDataInfo info = new TableDataInfo();
        try {
            List<SimSensorConfig> devices = simSensorConfigService.getAllSimSensorConfig();
            info.setCode(200);
            info.setRows(devices);
            info.setMsg("获取传感器阈值配置列表成功");
            info.setTotal(devices.size());
            logger.info("获取传感器阈值配置列表成功，共 {} 个", devices.size());
            return info;
        } catch (Exception e) {
            info.setCode(500);
            info.setMsg("获取传感器阈值配置列表失败: " + e.getMessage());
            logger.error("获取传感器阈值配置列表失败", e);
            return info;
        }
    }

}
