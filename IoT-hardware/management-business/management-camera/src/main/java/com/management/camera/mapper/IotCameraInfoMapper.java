package com.management.camera.mapper;

import com.management.camera.domain.IotCameraInfo;

import java.util.List;

public interface IotCameraInfoMapper {

    /**
     * 查询所有摄像头信息
     * @return 摄像头信息列表
     */
    List<IotCameraInfo> selectAllCamera();

    /**
     * 根据摄像头编码查询摄像头信息
     * @param cameraCode 摄像头编码
     * @return 摄像头信息
     */
    IotCameraInfo selectCameraByCode(String cameraCode);

    /**
     * 添加摄像头信息
     * @param cameraInfo 摄像头信息
     * @return 添加结果
     */
    int insertCamera(IotCameraInfo cameraInfo);

    /**
     * 根据摄像头ID查询摄像头信息
     * @param cameraId 摄像头ID
     * @return 摄像头信息
     */
    IotCameraInfo selectCameraById(Long cameraId);

    /**
     * 根据设备ID查询摄像头信息
     * @param deviceId 设备ID
     * @return 摄像头信息列表
     */
    List<IotCameraInfo> selectCameraByDeviceId(Long deviceId);

    /**
     * 更新摄像头信息
     * @param cameraInfo 摄像头信息
     * @return 更新结果
     */
    int updateCamere(IotCameraInfo cameraInfo);

    /**
     * 删除摄像头信息
     * @param cameraId 摄像头ID
     * @return 删除结果
     */
    int deleteCameraById(Long cameraId);

    /**
     * 根据设备ID查询摄像头信息
     * @param deviceId 设备ID
     * @return 摄像头信息
     */
    IotCameraInfo getCameraByDeviceIdData(Long deviceId);
}
