package com.management.web.controller.business.sim;

import com.management.common.core.domain.AjaxResult;
import com.management.common.core.page.TableDataInfo;
import com.management.sim.domain.SimSensorConfig;
import com.management.sim.domain.SimSensorGenre;
import com.management.sim.domain.SimSerialPortConfig;
import com.management.sim.service.SimSensorConfigService;
import com.management.sim.service.SimSerialPortConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/serial_port_config")
public class SimSerialPortConfigController {

    private static final Logger logger = LoggerFactory.getLogger(SimSerialPortConfigController.class);

    @Autowired
    private SimSerialPortConfigService simSerialPortConfigService;


    /**
     * 获取串口通信配置列表
     *
     * @return
     */
    @GetMapping("/list_all")
    public TableDataInfo getAllSerialPortConfig() {
        TableDataInfo info = new TableDataInfo();
        try {
            List<SimSerialPortConfig> devices = simSerialPortConfigService.getAllSerialPortConfig();
            info.setCode(200);
            info.setRows(devices);
            info.setMsg("获取串口通信配置列表成功");
            info.setTotal(devices.size());
            logger.info("获取串口通信配置列表成功，共 {} 个类型", devices.size());
            return info;
        } catch (Exception e) {
            info.setCode(500);
            info.setMsg("获取串口通讯配置列表失败: " + e.getMessage());
            logger.error("获取串口通讯配置列表失败", e);
            return info;
        }
    }

    /**
     * 保存串口配置
     *
     * @param config
     * @return
     */
    @PostMapping("/save")
    public AjaxResult saveSerialPortConfig(@RequestBody SimSerialPortConfig config) {
        try {

            if (config == null){
                return AjaxResult.error("保存串口配置失败: 参数错误");
            }

            return simSerialPortConfigService.saveSerialPortConfig(config);

        } catch (Exception e) {
            logger.error("保存串口配置失败", e);
            return AjaxResult.error("保存串口配置失败: " + e.getMessage());
        }
    }

    @GetMapping("/info/{serialId}")
    public AjaxResult getUniqueIdentification(@PathVariable("serialId") Long serialId) {

        try {
            SimSerialPortConfig config = simSerialPortConfigService.getBySerialId(serialId);
            if (config == null) {
                return AjaxResult.error("获取串口配置失败: 串口配置不存在");
            }
            return AjaxResult.success(config.getUniqueIdentification());
        } catch (Exception e) {
            logger.error("获取串口配置失败", e);
            return AjaxResult.error("获取串口配置失败: " + e.getMessage());
        }

    }

}
