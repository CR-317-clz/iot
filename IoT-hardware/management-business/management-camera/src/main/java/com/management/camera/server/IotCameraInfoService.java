package com.management.camera.server;

import com.management.camera.domain.IotCameraInfo;

import java.util.List;

public interface IotCameraInfoService {

    /**
     * 获取所有摄像头信息
     * @return
     */
    List<IotCameraInfo> getAllCamera();

    /**
     * 添加摄像头信息
     * @param cameraInfo
     * @return
     */
    boolean addCamera(IotCameraInfo cameraInfo);

    /**
     * 根据摄像头ID获取摄像头信息
     * @param cameraId
     * @return
     */
    IotCameraInfo getCameraById(Long cameraId);

    /**
     * 根据设备ID获取摄像头信息
     * @param deviceId
     * @return
     */
    List<IotCameraInfo> getCameraByDeviceId(Long deviceId);

    /**
     * 更新摄像头信息
     * @param cameraInfo
     * @return
     */
    boolean updateCamera(IotCameraInfo cameraInfo);

    /**
     * 删除摄像头信息
     * @param cameraId
     * @return
     */
    boolean deleteCameraById(Long cameraId);

    /**
     * 根据设备ID获取摄像头信息
     * @param deviceId
     * @return
     */
    IotCameraInfo getCameraByDeviceIdData(Long deviceId);
}
