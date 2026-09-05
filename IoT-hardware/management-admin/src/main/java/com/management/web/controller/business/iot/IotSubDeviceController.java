package com.management.web.controller.business.iot;

import com.management.common.core.controller.BaseController;
import com.management.common.core.domain.AjaxResult;
import com.management.common.core.page.TableDataInfo;
import com.management.iot.domain.IotDevice;
import com.management.iot.domain.IotSubDevice;
import com.management.iot.service.IotSubDeviceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/subDevice")
public class IotSubDeviceController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(IotSubDeviceController.class);

    @Autowired
    private IotSubDeviceService iotSubDeviceService;


    /**
     * 获取所有子设备列表
     * @return 设备列表和操作结果
     */
    @GetMapping("/list")
    public TableDataInfo getAllSubDevices() {
        TableDataInfo info = new TableDataInfo();
        try {
            List<IotSubDevice> devices = iotSubDeviceService.getAllSubDevices();
            info.setCode(200);
            info.setRows(devices);
            info.setMsg("获取子设备列表成功");
            info.setTotal(devices.size());
            logger.info("获取子设备列表成功，共 {} 个设备", devices.size());
            return info;
        } catch (Exception e) {
            info.setCode(500);
            info.setMsg("获取子设备列表失败: " + e.getMessage());
            logger.error("获取子设备列表失败", e);
            return info;
        }
    }

    /**
     * 根据父设备ID获取设备信息
     * @param deviceId 设备ID
     * @return 子设备信息和操作结果
     */
    @GetMapping("/{deviceId}")
    public TableDataInfo getDeviceById(@PathVariable Long deviceId) {
        TableDataInfo info = new TableDataInfo();
        try {
            List<IotSubDevice> device = iotSubDeviceService.getSubDeviceById(deviceId);
            info.setCode(200);
            info.setRows(device);
            info.setMsg("获取子设备列表成功");
            info.setTotal(device.size());
            logger.info("获取子设备列表成功，共 {} 个设备", device.size());
            return info;
        } catch (Exception e) {
            info.setCode(500);
            info.setMsg("获取子设备列表失败: " + e.getMessage());
            logger.error("获取设子备信息失败，设备ID: {}", deviceId, e);
            return info;
        }
    }

    /**
     * 添加新子设备
     * @param device 设备信息
     * @return 操作结果
     */
    @PostMapping("/add")
    public AjaxResult addDevice(@RequestBody IotSubDevice device) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = iotSubDeviceService.addSubDevice(device);
            if (success) {
                logger.info("添加子设备成功，设备ID: {}, 子设备名称: {}", device.getDeviceId(), device.getSubName());
                return AjaxResult.success("添加设备成功",device.getDeviceId());
            } else {
                logger.error("添加子设备失败，子设备名称: {}", device.getSubName());
                return AjaxResult.error("添加设备失败");
            }
        } catch (Exception e) {
            logger.error("添加子设备失败", e);
            return AjaxResult.error("添加子设备失败"+e.getMessage());
        }
    }
}
