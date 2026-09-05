package com.management.web.controller.business.io4G;

import com.management.common.core.page.TableDataInfo;
import com.management.io.domain.IotMeteorologyDevice;
import com.management.io.service.IotMeteorologyDeviceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/meteorology_device")
public class IotMeteorologyDeviceController {

    private static final Logger logger = LoggerFactory.getLogger(IotMeteorologyDeviceController.class);

    @Autowired
    private IotMeteorologyDeviceService iotMeteorologyDeviceService;

    /**
     * 获取气象站设备列表
     * @return
     */
    @GetMapping("/list")
    public TableDataInfo list() {
        TableDataInfo info = new TableDataInfo();
        try {
            List<IotMeteorologyDevice> meteorologyDevices = iotMeteorologyDeviceService.getAllMeteorologyDevice();
            info.setCode(200);
            info.setRows(meteorologyDevices);
            info.setMsg("获取气象站设备列表成功");
            info.setTotal(meteorologyDevices.size());
            logger.info("获取气象站设备列表成功，共 {} 个设备", meteorologyDevices.size());
            return info;
        } catch (Exception e) {
            info.setCode(500);
            info.setMsg("获取气象站设备列表失败: " + e.getMessage());
            logger.error("获取气象站设备列表失败", e);
            return info;
        }
    }

}
