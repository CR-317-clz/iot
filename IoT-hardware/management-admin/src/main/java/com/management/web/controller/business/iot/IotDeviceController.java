package com.management.web.controller.business.iot;

import com.management.camera.domain.IotCameraInfo;
import com.management.camera.server.IotCameraInfoService;
import com.management.common.core.domain.AjaxResult;
import com.management.common.core.page.TableDataInfo;
import com.management.iot.domain.IotDevice;
import com.management.iot.domain.dto.DeviceProtocolDto;
import com.management.iot.service.IotDeviceService;
import net.sf.jsqlparser.schema.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * IoT设备信息控制器
 * 提供设备管理的RESTful API接口
 */
@RestController
@RequestMapping("/device")
public class IotDeviceController {

    private static final Logger logger = LoggerFactory.getLogger(IotDeviceController.class);
    
    @Autowired
    private IotDeviceService deviceService;

    @Autowired
    private IotCameraInfoService cameraService;

    /**
     * 获取所有设备列表
     * @return 设备列表和操作结果
     */
    @GetMapping("/list")
    public TableDataInfo getAllDevices() {
        TableDataInfo info = new TableDataInfo();
        try {
            List<IotDevice> devices = deviceService.getAllDevices();
            info.setCode(200);
            info.setRows(devices);
            info.setMsg("获取设备列表成功");
            info.setTotal(devices.size());
            logger.info("获取设备列表成功，共 {} 个设备", devices.size());
            return info;
        } catch (Exception e) {
            info.setCode(500);
            info.setMsg("获取设备列表失败: " + e.getMessage());
            logger.error("获取设备列表失败", e);
            return info;
        }
    }

    /**
     * 根据设备ID获取设备信息
     * @param deviceId 设备ID
     * @return 设备信息和操作结果
     */
    @GetMapping("/{deviceId}")
    public AjaxResult getDeviceById(@PathVariable Long deviceId) {
        AjaxResult result = new AjaxResult();
        try {
            IotDevice device = deviceService.getDeviceById(deviceId);
            IotCameraInfo cameraByDeviceId = cameraService.getCameraByDeviceIdData(device.getDeviceId());
            device.setCameraInfo(cameraByDeviceId);
            result.put("code", 200);
            result.put("data", device);
            result.put("message", "获取设备信息成功");
            logger.info("获取设备信息成功，设备ID: {}", deviceId);
            return result;
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取设备信息失败: " + e.getMessage());
            logger.error("获取设备信息失败，设备ID: {}", deviceId, e);
            return result;
        }
    }

    /**
     * 根据设备编码获取设备信息
     * @param deviceCode 设备编码
     * @return 设备信息和操作结果
     */
    @GetMapping("/code/{deviceCode}")
    public AjaxResult getDeviceByCode(@PathVariable String deviceCode) {
        AjaxResult result = new AjaxResult();
        try {
            IotDevice device = deviceService.getDeviceByCode(deviceCode);
            result.put("code", 200);
            result.put("data", device);
            result.put("message", "获取设备信息成功");
            logger.info("获取设备信息成功，设备编码: {}", deviceCode);
            return result;
        } catch (Exception e) {
            result.put("success", 500);
            result.put("message", "获取设备信息失败: " + e.getMessage());
            logger.error("获取设备信息失败，设备编码: {}", deviceCode, e);
            return result;
        }
    }

    /**
     * 根据设备状态获取设备列表
     * @param status 设备状态
     * @return 设备列表和操作结果
     */
    @GetMapping("/status/{status}")
    public TableDataInfo getDevicesByStatus(@PathVariable String status) {
        TableDataInfo info = new TableDataInfo();
        try {
            List<IotDevice> devices = deviceService.getDevicesByStatus(status);
            info.setCode(200);
            info.setRows(devices);
            info.setMsg("根据状态获取设备列表成功");
            logger.info("根据状态获取设备列表成功，状态: {}, 数量: {}", status, devices.size());
            return info;
        } catch (Exception e) {
            info.setCode(500);
            info.setMsg("根据状态获取设备列表失败: " + e.getMessage());
            logger.error("根据状态获取设备列表失败，状态: {}", status, e);
            return info;
        }
    }

    /**
     * 添加新设备
     * @param device 设备信息
     * @return 操作结果
     */
    @PostMapping("/add")
    public AjaxResult addDevice(@RequestBody IotDevice device) {
        AjaxResult result = new AjaxResult();
        try {
            boolean success = deviceService.addDevice(device);
            if (success) {
                result.put("code", 200);
                result.put("message", "添加设备成功");
                result.put("deviceId", device.getDeviceId());
                logger.info("添加设备成功，设备ID: {}, 设备名称: {}", device.getDeviceId(), device.getDeviceName());
                return result;
            } else {
                result.put("code", 500);
                result.put("message", "添加设备失败");
                logger.error("添加设备失败，设备名称: {}", device.getDeviceName());
                return result;
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "添加设备失败: " + e.getMessage());
            logger.error("添加设备失败", e);
            return result;
        }
    }

    /**
     * 更新设备信息
     * @param device 设备信息
     * @return 操作结果
     */
    @PutMapping("/update")
    public AjaxResult updateDevice(@RequestBody IotDevice device) {
        AjaxResult result = new AjaxResult();
        try {
            boolean success = deviceService.updateDevice(device);
            if (success) {
                result.put("code", 200);
                result.put("message", "更新设备信息成功");
                logger.info("更新设备信息成功，设备ID: {}", device.getDeviceId());
                return result;
            } else {
                result.put("success", 500);
                result.put("message", "更新设备信息失败");
                logger.error("更新设备信息失败，设备ID: {}", device.getDeviceId());
                return result;
            }
        } catch (Exception e) {
            result.put("success", 500);
            result.put("message", "更新设备信息失败: " + e.getMessage());
            logger.error("更新设备信息失败，设备ID: {}", device.getDeviceId(), e);
            return result;
        }
    }

    /**
     * 更新设备状态
     * @param device 设备实体对象
     * @return 操作结果
     */
    @PutMapping("/updateStatus")
    public AjaxResult updateDeviceStatus(@RequestBody IotDevice device) {
        AjaxResult result = new AjaxResult();
        try {
            boolean success = deviceService.updateDeviceStatus(device);
            if (success) {
                result.put("code", 200);
                result.put("message", "更新设备状态成功");
                logger.info("更新设备状态成功，设备ID: {}, 状态: {}", device.getDeviceId(), device.getStatus());
                return result;
            } else {
                result.put("code", 500);
                result.put("message", "更新设备状态失败");
                logger.error("更新设备状态失败，设备ID: {}, 状态: {}", device.getDeviceId(), device.getStatus());
                return result;
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "更新设备状态失败: " + e.getMessage());
            logger.error("更新设备状态失败，设备ID: {}, 状态: {}", device.getDeviceId(), device.getStatus(), e);
            return result;
        }
    }

    /**
     * 删除设备
     * @param deviceId 设备ID
     * @return 操作结果
     */
    @DeleteMapping("/{deviceId}")
    public AjaxResult deleteDevice(@PathVariable Long deviceId) {
        AjaxResult result = new AjaxResult();
        try {
            boolean success = deviceService.deleteDevice(deviceId);
            if (success) {
                result.put("code", 200);
                result.put("message", "删除设备成功");
                logger.info("删除设备成功，设备ID: {}", deviceId);
                return result;
            } else {
                result.put("code", 500);
                result.put("message", "删除设备失败");
                logger.error("删除设备失败，设备ID: {}", deviceId);
                return result;
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "删除设备失败: " + e.getMessage());
            logger.error("删除设备失败，设备ID: {}", deviceId, e);
            return result;
        }
    }

    /**
     * 获取设备统计信息
     * @return 统计信息和操作结果
     */
    @GetMapping("/stats")
    public AjaxResult getDeviceStats() {
        AjaxResult result = new AjaxResult();
        try {
            int totalDevices = deviceService.getDeviceCount();
            List<IotDevice> onlineDevices = deviceService.getDevicesByStatus("ONLINE");
            List<IotDevice> offlineDevices = deviceService.getDevicesByStatus("OFFLINE");
            List<IotDevice> faultDevices = deviceService.getDevicesByStatus("FAULT");
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("total", totalDevices);
            stats.put("online", onlineDevices.size());
            stats.put("offline", offlineDevices.size());
            stats.put("fault", faultDevices.size());
            
            result.put("code", 200);
            result.put("data", stats);
            result.put("message", "获取设备统计信息成功");
            logger.info("获取设备统计信息成功，总数: {}", totalDevices);
            return result;
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取设备统计信息失败: " + e.getMessage());
            logger.error("获取设备统计信息失败", e);
            return result;
        }
    }

    /**
     * 更新设备协议
     * @param dto 设备实体对象
     * @return 操作结果
     */
    @PutMapping("/updateDeviceProtocol")
    public AjaxResult updateDeviceProtocol(@RequestBody DeviceProtocolDto dto){
        AjaxResult result = new AjaxResult();
        try {
            boolean success = deviceService.updateDeviceProtocol(dto);
            if (success) {
                result.put("code", 200);
                result.put("message", "更新设备协议成功");
                logger.info("更新设备协议成功，设备ID: {}, 协议ID: {}", dto.getDeviceId(), dto.getProtocolId());
                return result;
            } else {
                result.put("code", 500);
                result.put("message", "更新设备协议失败");
                logger.error("更新设备协议失败，设备ID: {}, 协议ID: {}", dto.getDeviceId(), dto.getProtocolId());
                return result;
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "更新设备协议失败: " + e.getMessage());
            logger.error("更新设备协议失败，设备ID: {}, 协议ID: {}", dto.getDeviceId(), dto.getProtocolId(), e);
            return result;
        }
    }
}