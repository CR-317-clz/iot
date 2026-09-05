package com.management.web.controller.business.camera;

import com.management.camera.domain.IotCameraInfo;
import com.management.camera.server.IotCameraInfoService;
import com.management.common.core.domain.AjaxResult;
import com.management.common.core.page.TableDataInfo;
import com.management.iot.domain.IotDevice;
import com.management.iot.domain.IotTelemetry;
import com.management.web.controller.business.iot.IotDeviceController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/camera")
public class  IotCameraInfoController {

    private static final Logger logger = LoggerFactory.getLogger(IotCameraInfoController.class);

    @Autowired
    private IotCameraInfoService iotCameraInfoService;

    /**
     * 获取所有摄像头列表
     * @return
     */
    @GetMapping("/list")
    public TableDataInfo getAllCamera() {
        TableDataInfo info = new TableDataInfo();
        try {
            List<IotCameraInfo> camera = iotCameraInfoService.getAllCamera();
            info.setCode(200);
            info.setRows(camera);
            info.setMsg("获取摄像头列表成功");
            info.setTotal(camera.size());
            logger.info("获取摄像头列表成功，共 {} 个设备", camera.size());
            return info;
        } catch (Exception e) {
            info.setCode(500);
            info.setMsg("获取摄像头列表失败: " + e.getMessage());
            logger.error("获取摄像头列表失败", e);
            return info;
        }
    }

    /**
     * 获取指定摄像头信息
     * @param cameraId 摄像头ID
     * @return 摄像头信息
     */
    @GetMapping("/info/{cameraId}")
    public AjaxResult getCameraById(@PathVariable Long cameraId) {
        AjaxResult result = new AjaxResult();
        try {
            IotCameraInfo camera = iotCameraInfoService.getCameraById(cameraId);
                result.put("code", 200);
                result.put("data", camera);
                result.put("message", "获取摄像头信息成功");
                logger.info("获取摄像头信息成功，设备ID: {}", cameraId);
                return result;
            } catch (Exception e) {
                result.put("code", 500);
                result.put("message", "获取摄像头信息失败: " + e.getMessage());
                logger.error("获取摄像头信息失败，设备ID: {}", cameraId, e);
                return result;
            }
    }

    /**
     * 获取指定设备下的所有摄像头信息
     * @param deviceId 设备ID
     * @return 摄像头信息列表
     */
    @GetMapping("/device/{deviceId}")
    public AjaxResult getTelemetryByDeviceId(@PathVariable Long deviceId) {
        AjaxResult result = new AjaxResult();
        try {
            List<IotCameraInfo> cameraInfos = iotCameraInfoService.getCameraByDeviceId(deviceId);
            result.put("code", 200);
            result.put("data", cameraInfos);
            result.put("message", "获取摄像头数据成功");
            logger.info("获取摄像头数据成功，设备ID: {}, 数量: {}", deviceId, cameraInfos.size());
            return result;
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取摄像头数据失败: " + e.getMessage());
            logger.error("获取摄像头数据失败，设备ID: {}", deviceId, e);
            return result;
        }
    }

    /**
     * 添加新摄像头
     * @param cameraInfo 摄像头信息
     * @return 是否添加成功
     */
    @PostMapping("/add")
    public AjaxResult addCamera(@RequestBody IotCameraInfo cameraInfo) {
        AjaxResult result = new AjaxResult();
        try {
            boolean success = iotCameraInfoService.addCamera(cameraInfo);
            if (success) {
                result.put("code", 200);
                result.put("message", "添加设备成功");
                result.put("deviceId", cameraInfo.getDeviceId());
                logger.info("添加摄像头成功，摄像头ID: {}, 摄像头名称: {}", cameraInfo.getCameraId(), cameraInfo.getCameraName());
                return result;
            } else {
                result.put("code", 500);
                result.put("message", "添加摄像头失败");
                logger.error("添加摄像头失败，摄像头名称: {}", cameraInfo.getCameraName());
                return result;
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "添加摄像头失败: " + e.getMessage());
            logger.error("添加摄像头失败", e);
            return result;
        }
    }

    /**
     * 修改摄像头信息
     * @param cameraInfo 摄像头信息
     * @return 是否修改成功
     */
    @PutMapping("/update")
    public AjaxResult updateCamera(@RequestBody IotCameraInfo cameraInfo) {
        AjaxResult result = new AjaxResult();
        try {
            boolean success = iotCameraInfoService.updateCamera(cameraInfo);
            if (success) {
                result.put("code", 200);
                result.put("message", "修改设备成功");
                result.put("deviceId", cameraInfo.getDeviceId());
                logger.info("修改摄像头成功，摄像头ID: {}, 摄像头名称: {}", cameraInfo.getCameraId(), cameraInfo.getCameraName());
                return result;
            } else {
                result.put("code", 500);
                result.put("message", "修改摄像头失败");
                logger.error("修改摄像头失败，摄像头名称: {}", cameraInfo.getCameraName());
                return result;
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "修改摄像头失败: " + e.getMessage());
            logger.error("修改摄像头失败", e);
            return result;
        }
    }

    @DeleteMapping("/delete/{cameraId}")
    public AjaxResult deleteCamera(@PathVariable Long cameraId) {
        AjaxResult result = new AjaxResult();
        try {
            boolean success = iotCameraInfoService.deleteCameraById(cameraId);
            if (success) {
                result.put("code", 200);
                result.put("message", "删除摄像头成功");
                logger.info("删除摄像头成功，摄像头ID: {}", cameraId);
                return result;
            } else {
                result.put("code", 500);
                result.put("message", "删除摄像头失败");
                logger.error("删除摄像头失败，摄像头ID: {}", cameraId);
                return result;
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "删除摄像头失败: " + e.getMessage());
            logger.error("删除摄像头失败，摄像头ID: {}", cameraId, e);
            return result;
        }
    }

}
