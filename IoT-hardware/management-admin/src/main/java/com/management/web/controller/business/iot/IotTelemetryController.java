package com.management.web.controller.business.iot;

import com.management.common.core.page.TableDataInfo;
import com.management.iot.domain.IotTelemetry;
import com.management.iot.service.IotTelemetryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 设备时序数据控制器
 * 提供时序数据查询的RESTful API接口
 */
@RestController
@RequestMapping("/telemetry")
public class IotTelemetryController {

    private static final Logger logger = LoggerFactory.getLogger(IotTelemetryController.class);
    
    @Autowired
    private IotTelemetryService telemetryService;

    /**
     * 获取所有时序数据
     * @return 时序数据列表和操作结果
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getAllTelemetry() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<IotTelemetry> telemetryList = telemetryService.getAllTelemetry();
            result.put("success", true);
            result.put("data", telemetryList);
            result.put("message", "获取时序数据列表成功");
            logger.info("获取时序数据列表成功，共 {} 条记录", telemetryList.size());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取时序数据列表失败: " + e.getMessage());
            logger.error("获取时序数据列表失败", e);
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 根据设备ID获取时序数据
     * @param deviceId 设备ID
     * @return 时序数据列表和操作结果
     */
    @GetMapping("/device/{deviceId}")
    public TableDataInfo getTelemetryByDeviceId(@PathVariable Long deviceId) {
        TableDataInfo info = new TableDataInfo();
        try {
            List<IotTelemetry> telemetryList = telemetryService.getTelemetryByDeviceId(deviceId);
            info.setCode(200);
            info.setRows( telemetryList);
            info.setMsg("获取设备时序数据成功");
            info.setTotal(telemetryList.size());
            logger.info("获取设备时序数据成功，设备ID: {}, 数量: {}", deviceId, telemetryList.size());
            return info;
        } catch (Exception e) {
            info.setCode(500);
            info.setMsg("获取设备时序数据失败: " + e.getMessage());
            logger.error("获取设备时序数据失败，设备ID: {}", deviceId, e);
            return info;
        }
    }

    /**
     * 根据数据键名获取时序数据
     * @param dataKey 数据键名
     * @return 时序数据列表和操作结果
     */
    @GetMapping("/key/{dataKey}")
    public ResponseEntity<Map<String, Object>> getTelemetryByDataKey(@PathVariable String dataKey) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<IotTelemetry> telemetryList = telemetryService.getTelemetryByDataKey(dataKey);
            result.put("success", true);
            result.put("data", telemetryList);
            result.put("message", "根据数据键名获取时序数据成功");
            logger.info("根据数据键名获取时序数据成功，数据键: {}, 数量: {}", dataKey, telemetryList.size());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "根据数据键名获取时序数据失败: " + e.getMessage());
            logger.error("根据数据键名获取时序数据失败，数据键: {}", dataKey, e);
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 根据时间范围查询时序数据
     * @param deviceId 设备ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 时序数据列表和操作结果
     */
    @GetMapping("/range")
    public ResponseEntity<Map<String, Object>> getTelemetryByTimeRange(
            @RequestParam Long deviceId,
            @RequestParam String startTime,
            @RequestParam String endTime) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<IotTelemetry> telemetryList = telemetryService.getTelemetryByTimeRange(deviceId, startTime, endTime);
            result.put("success", true);
            result.put("data", telemetryList);
            result.put("message", "根据时间范围查询时序数据成功");
            logger.info("根据时间范围查询时序数据成功，设备ID: {}, 时间范围: {} - {}, 数量: {}", 
                       deviceId, startTime, endTime, telemetryList.size());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "根据时间范围查询时序数据失败: " + e.getMessage());
            logger.error("根据时间范围查询时序数据失败，设备ID: {}, 时间范围: {} - {}", deviceId, startTime, endTime, e);
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 获取设备的最新数据
     * @param deviceId 设备ID
     * @return 最新时序数据列表和操作结果
     */
    @GetMapping("/latest/{deviceId}")
    public ResponseEntity<Map<String, Object>> getLatestTelemetry(@PathVariable Long deviceId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<IotTelemetry> telemetryList = telemetryService.getLatestTelemetryByDeviceId(deviceId);
            result.put("success", true);
            result.put("data", telemetryList);
            result.put("message", "获取设备最新数据成功");
            logger.info("获取设备最新数据成功，设备ID: {}, 数量: {}", deviceId, telemetryList.size());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取设备最新数据失败: " + e.getMessage());
            logger.error("获取设备最新数据失败，设备ID: {}", deviceId, e);
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 保存时序数据记录
     * @param telemetry 时序数据信息
     * @return 操作结果
     */
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addTelemetry(@RequestBody IotTelemetry telemetry) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = telemetryService.saveTelemetry(telemetry);
            if (success) {
                result.put("success", true);
                result.put("message", "保存时序数据成功");
                result.put("telemetryId", telemetry.getTelemetryId());
                logger.info("保存时序数据成功，数据ID: {}, 设备ID: {}, 数据键: {}", 
                           telemetry.getTelemetryId(), telemetry.getDeviceId(), telemetry.getDataKey());
                return ResponseEntity.ok(result);
            } else {
                result.put("success", false);
                result.put("message", "保存时序数据失败");
                logger.error("保存时序数据失败，设备ID: {}, 数据键: {}", telemetry.getDeviceId(), telemetry.getDataKey());
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "保存时序数据失败: " + e.getMessage());
            logger.error("保存时序数据失败", e);
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 批量保存时序数据记录
     * @param telemetryList 时序数据列表
     * @return 操作结果
     */
    @PostMapping("/batch-add")
    public ResponseEntity<Map<String, Object>> batchAddTelemetry(@RequestBody List<IotTelemetry> telemetryList) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = telemetryService.batchSaveTelemetry(telemetryList);
            if (success) {
                result.put("success", true);
                result.put("message", "批量保存时序数据成功");
                logger.info("批量保存时序数据成功，共 {} 条记录", telemetryList.size());
                return ResponseEntity.ok(result);
            } else {
                result.put("success", false);
                result.put("message", "批量保存时序数据失败");
                logger.error("批量保存时序数据失败，数量: {}", telemetryList.size());
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "批量保存时序数据失败: " + e.getMessage());
            logger.error("批量保存时序数据失败", e);
            return ResponseEntity.badRequest().body(result);
        }
    }
}