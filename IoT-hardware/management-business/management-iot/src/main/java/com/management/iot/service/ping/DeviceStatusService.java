package com.management.iot.service.ping;

import java.util.Map;

/**
 * 设备状态服务接口
 */
public interface DeviceStatusService {
    
    /**
     * 检查所有设备的网络状态（定时任务调用）
     */
    void checkAllDevicesStatus();
    
    /**
     * 手动触发设备状态检查
     */
    void manualCheckDeviceStatus();
    
    /**
     * 获取设备统计信息
     * @return 统计信息Map
     */
    Map<String, Object> getDeviceStats();
    
    /**
     * 详细Ping检测指定主机
     * @param host 主机地址
     * @return 检测结果
     */
    Map<String, Object> detailedPingCheck(String host);
    
    /**
     * 检查并更新指定主机的设备状态
     * @param host 主机地址
     * @return 更新结果
     */
    Map<String, Object> checkAndUpdateHostStatus(String host);
    
    /**
     * 根据父设备ID更新所有子设备状态
     * @param deviceId 父设备ID
     * @param status 状态（ONLINE/OFFLINE）
     * @return 更新的设备数量
     */
    int updateChildDevicesStatus(Long deviceId, String status);
}