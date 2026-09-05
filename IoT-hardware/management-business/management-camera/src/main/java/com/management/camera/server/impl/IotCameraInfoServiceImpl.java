package com.management.camera.server.impl;

import com.management.camera.domain.IotCameraInfo;
import com.management.camera.mapper.IotCameraInfoMapper;
import com.management.camera.server.IotCameraInfoService;
import com.management.common.utils.snowFlake.SnowflakeIdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class IotCameraInfoServiceImpl implements IotCameraInfoService {

    @Autowired
    private IotCameraInfoMapper iotCameraInfoMapper;


    /**
     * 获取所有摄像头信息
     * @return
     */
    @Override
    public List<IotCameraInfo> getAllCamera() {
        try {
            return iotCameraInfoMapper.selectAllCamera();
        } catch (Exception e) {
            throw new RuntimeException("获取摄像头列表失败: " + e.getMessage(), e);
        }
    }

    /**
     * 添加新摄像头
     * @param cameraInfo 摄像头信息
     * @return 是否添加成功
     */
    @Override
    public boolean addCamera(IotCameraInfo cameraInfo) {
        if (cameraInfo == null) {
            throw new IllegalArgumentException("摄像头信息不能为空");
        }
        if (cameraInfo.getCameraName() == null || cameraInfo.getCameraName().trim().isEmpty()) {
            throw new IllegalArgumentException("摄像头名称不能为空");
        }
        if (cameraInfo.getCameraCode() == null || cameraInfo.getCameraCode().trim().isEmpty()) {
            throw new IllegalArgumentException("摄像头编码不能为空");
        }

        try {
            // 检查设备编码是否已存在
            IotCameraInfo existingDevice = iotCameraInfoMapper.selectCameraByCode(cameraInfo.getCameraCode());
            if (existingDevice != null) {
                throw new RuntimeException("摄像头编码已存在: " + cameraInfo.getCameraCode());
            }

            cameraInfo.setCameraId(SnowflakeIdGenerator.nextId());
            int result = iotCameraInfoMapper.insertCamera(cameraInfo);
            return result > 0;
        } catch (Exception e) {
            throw new RuntimeException("添加摄像头失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据摄像头ID获取摄像头信息
     * @param cameraId 摄像头ID
     * @return 摄像头信息
     */
    @Override
    public IotCameraInfo getCameraById(Long cameraId) {
        if (cameraId == null || cameraId <= 0) {
            throw new IllegalArgumentException("摄像头ID不能为空且必须大于0");
        }
        try {
            IotCameraInfo device = iotCameraInfoMapper.selectCameraById(cameraId);
            if (device == null) {
                throw new RuntimeException("摄像头不存在，设备ID: " + cameraId);
            }
            return device;
        } catch (Exception e) {
            throw new RuntimeException("获取摄像头信息失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据设备ID获取摄像头信息
     * @param deviceId 设备ID
     * @return 摄像头信息列表
     */
    @Override
    public List<IotCameraInfo> getCameraByDeviceId(Long deviceId) {
        if (deviceId == null || deviceId <= 0) {
            throw new IllegalArgumentException("设备ID不能为空且必须大于0");
        }
        try {
            return iotCameraInfoMapper.selectCameraByDeviceId(deviceId);
        } catch (Exception e) {
            log.error("根据设备ID获取摄像头数据失败，设备ID: {}", deviceId, e);
            throw new RuntimeException("根据设备ID获取摄像头数据失败: " + e.getMessage(), e);
        }
    }

    /**
     * 更新摄像头信息
     * @param cameraInfo 摄像头信息
     * @return 是否更新成功
     */
    @Override
    public boolean updateCamera(IotCameraInfo cameraInfo) {
        if (cameraInfo == null) {
            throw new IllegalArgumentException("摄像头信息不能为空");
        }
        if (cameraInfo.getCameraId() == null || cameraInfo.getCameraId() == 0) {
            throw new IllegalArgumentException("摄像头ID不能为空");
        }

        try {
            // 检查设备是否已存在
            IotCameraInfo existingDevice = iotCameraInfoMapper.selectCameraById(cameraInfo.getCameraId());
            if (existingDevice == null) {
                throw new RuntimeException("摄像头不存在: " + cameraInfo.getCameraId());
            }
            int result = iotCameraInfoMapper.updateCamere(cameraInfo);
            return result > 0;
        } catch (Exception e) {
            throw new RuntimeException("添加摄像头失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteCameraById(Long cameraId) {
        if (cameraId == null || cameraId <= 0){
            throw new IllegalArgumentException("摄像头ID不能为空且必须大于0");
        }

        try {
            IotCameraInfo existingDevice = iotCameraInfoMapper.selectCameraById(cameraId);
            if (existingDevice == null) {
                throw new RuntimeException("摄像头不存在: " + cameraId);
            }
            int result = iotCameraInfoMapper.deleteCameraById(cameraId);
            return result > 0;
        } catch (Exception e) {
            throw new RuntimeException("删除摄像头失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据设备ID获取摄像头信息
     * @param deviceId 设备ID
     * @return 摄像头信息
     */
    @Override
    public IotCameraInfo getCameraByDeviceIdData(Long deviceId) {
        if (deviceId == null || deviceId <= 0) {
            throw new IllegalArgumentException("设备ID不能为空且必须大于0");
        }
        try {
            return iotCameraInfoMapper.getCameraByDeviceIdData(deviceId);
        } catch (Exception e) {
            log.error("根据设备ID获取摄像头数据失败，设备ID: {}", deviceId, e);
            throw new RuntimeException("根据设备ID获取摄像头数据失败: " + e.getMessage(), e);
        }
    }

}
