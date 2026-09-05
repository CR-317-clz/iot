package com.management.iot.service.ping.impl;

import com.management.iot.domain.IotSubDevice;
import com.management.iot.domain.result.PingResult;
import com.management.iot.domain.result.PingStatistics;
import com.management.iot.service.ping.DeviceStatusService;
import com.management.iot.service.IotSubDeviceService;
import com.management.iot.service.ping.PingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 设备状态服务实现类
 */
@Service
@Slf4j
public class DeviceStatusServiceImpl implements DeviceStatusService {
    
    @Autowired
    private IotSubDeviceService iotSubDeviceService;
    
    @Autowired
    private PingService pingService;
    
    // Ping配置参数
    private static final int PING_COUNT = 4;
    private static final int PING_TIMEOUT = 5000;
    private static final int QUICK_PING_TIMEOUT = 3000;
    
    /**
     * 检查所有设备的网络状态（定时任务调用）
     */
    @Override
    @Transactional
    public void checkAllDevicesStatus() {
        log.info("🕒 === 开始执行设备状态检查定时任务 ===");
        long startTime = System.currentTimeMillis();
        
        try {
            // 获取所有不重复的主机地址
            List<String> distinctHosts = iotSubDeviceService.findDistinctHosts();
            log.info("发现 {} 个不同的主机地址需要检查: {}", 
                    distinctHosts.size(), distinctHosts);
            
            int onlineHosts = 0;
            int offlineHosts = 0;
            int totalDevicesUpdated = 0;
            
            // 对每个主机地址执行Ping检测和状态更新
            for (String host : distinctHosts) {
                Map<String, Object> result = checkAndUpdateHostStatus(host);
                boolean isOnline = (boolean) result.get("online");
                int devicesUpdated = (int) result.get("devicesUpdated");
                
                if (isOnline) {
                    onlineHosts++;
                } else {
                    offlineHosts++;
                }
                totalDevicesUpdated += devicesUpdated;
            }
            
            long endTime = System.currentTimeMillis();
            log.info("✅ === 设备状态检查完成 ===");
            log.info("📊 检查结果: 在线主机={}, 离线主机={}, 更新设备={}, 耗时={}ms",
                    onlineHosts, offlineHosts, totalDevicesUpdated, (endTime - startTime));
            
        } catch (Exception e) {
            log.error("❌ === 设备状态检查任务执行失败 ===", e);
            throw new RuntimeException("设备状态检查失败", e);
        }
    }
    
    /**
     * 手动触发设备状态检查
     */
    @Override
    public void manualCheckDeviceStatus() {
        log.info("🔄 手动触发设备状态检查");
        checkAllDevicesStatus();
    }
    
    /**
     * 获取设备统计信息
     */
    @Override
    public Map<String, Object> getDeviceStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            List<IotSubDevice> allDevices = iotSubDeviceService.getAllSubDevices();
            List<String> distinctHosts = iotSubDeviceService.findDistinctHosts();
            
            // 统计在线/离线设备数量
            long onlineDevices = allDevices.stream()
                .filter(device -> "ONLINE".equals(device.getStatus()))
                .count();
            long offlineDevices = allDevices.size() - onlineDevices;
            
            // 按设备类型统计
            Map<String, Long> typeStats = allDevices.stream()
                .collect(Collectors.groupingBy(IotSubDevice::getSubType, Collectors.counting()));
            
            // 构建统计信息
            stats.put("totalDevices", allDevices.size());
            stats.put("onlineDevices", onlineDevices);
            stats.put("offlineDevices", offlineDevices);
            stats.put("totalHosts", distinctHosts.size());
            stats.put("typeStatistics", typeStats);
            stats.put("checkTime", new Date());
            stats.put("lastCheck", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            
            log.debug("设备统计信息生成完成: 总数={}, 在线={}, 离线={}", 
                    allDevices.size(), onlineDevices, offlineDevices);
            
        } catch (Exception e) {
            log.error("获取设备统计信息失败", e);
            stats.put("error", "获取统计信息失败: " + e.getMessage());
        }
        
        return stats;
    }
    
    /**
     * 详细Ping检测指定主机
     */
    @Override
    public Map<String, Object> detailedPingCheck(String host) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            log.info("开始详细Ping检查: {}", host);
            
            // 执行多次Ping检测
            List<PingResult> pingResults = pingService.multiplePing(host, PING_COUNT, PING_TIMEOUT);
            PingStatistics statistics = pingService.generateStatistics(pingResults, host);
            
            // 判断是否在线
            boolean isOnline = pingService.isDeviceOnline(statistics);
            String newStatus = isOnline ? "ONLINE" : "OFFLINE";
            
            // 更新设备状态
            int devicesUpdated = updateDevicesStatusByHost(host, newStatus);
            
            // 构建返回结果
            result.put("success", true);
            result.put("host", host);
            result.put("online", isOnline);
            result.put("status", newStatus);
            result.put("statistics", statistics);
            result.put("devicesUpdated", devicesUpdated);
            result.put("pingResults", pingResults);
            result.put("checkTime", new Date());
            
            log.info("详细Ping检查完成: {} -> {}, 更新设备: {}个", 
                    host, newStatus, devicesUpdated);
            
        } catch (Exception e) {
            log.error("详细Ping检查失败: {}", host, e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 检查并更新指定主机的设备状态
     */
    @Override
    public Map<String, Object> checkAndUpdateHostStatus(String host) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            log.debug("正在检查主机状态: {}", host);
            
            // 执行快速Ping检测
            boolean isReachable = pingService.quickPingCheck(host);
            String newStatus = isReachable ? "ONLINE" : "OFFLINE";
            
            // 查询该主机下的所有设备
            List<IotSubDevice> devices = iotSubDeviceService.findByHost(host);
            int totalDevices = devices.size();
            
            // 检查是否需要更新状态
            boolean needUpdate = devices.stream()
                .anyMatch(device -> !newStatus.equals(device.getStatus()));
            
            int devicesUpdated = 0;
            boolean statusChanged = false;
            
            if (needUpdate) {
                // 更新设备状态
                devicesUpdated = updateDevicesStatusByHost(host, newStatus);
                statusChanged = true;
                
                if (isReachable) {
                    log.info("✅ 主机 {} 在线，更新了 {}/{} 个设备状态为 ONLINE", 
                            host, devicesUpdated, totalDevices);
                } else {
                    log.warn("❌ 主机 {} 离线，更新了 {}/{} 个设备状态为 OFFLINE", 
                            host, devicesUpdated, totalDevices);
                }
            } else {
                log.debug("主机 {} 状态未变化，仍为 {}", host, newStatus);
            }
            
            // 构建返回结果
            result.put("host", host);
            result.put("online", isReachable);
            result.put("statusChanged", statusChanged);
            result.put("totalDevices", totalDevices);
            result.put("devicesUpdated", devicesUpdated);
            result.put("status", newStatus);
            result.put("success", true);
            
        } catch (Exception e) {
            log.error("检查主机状态失败: {}", host, e);
            result.put("success", false);
            result.put("error", e.getMessage());
            
            // 检查失败时，将设备状态设置为离线
            try {
                int updated = updateDevicesStatusByHost(host, "OFFLINE");
                result.put("devicesUpdated", updated);
                result.put("status", "OFFLINE");
            } catch (Exception ex) {
                log.error("更新设备状态失败: {}", host, ex);
            }
        }
        
        return result;
    }
    
    /**
     * 根据父设备ID更新所有子设备状态
     */
    @Override
    @Transactional
    public int updateChildDevicesStatus(Long deviceId, String status) {
        try {
            // 查询所有子设备
            List<IotSubDevice> childDevices = iotSubDeviceService.getSubDeviceById(deviceId);
            
            if (childDevices.isEmpty()) {
                log.debug("未找到设备ID为 {} 的子设备", deviceId);
                return 0;
            }
            
            // 获取子设备的主机地址（去重）
            List<String> distinctHosts = childDevices.stream()
                .map(IotSubDevice::getHost)
                .distinct()
                .collect(Collectors.toList());
            
            int totalUpdated = 0;
            
            // 更新每个主机的设备状态
            for (String host : distinctHosts) {
                int updated = iotSubDeviceService.updateStatusByHost(host, status);
                totalUpdated += updated;
            }
            
            log.info("更新子设备状态完成: 父设备ID={}, 状态={}, 更新设备数={}", 
                    deviceId, status, totalUpdated);
            
            return totalUpdated;
            
        } catch (Exception e) {
            log.error("更新子设备状态失败: deviceId={}, status={}", deviceId, status, e);
            throw new RuntimeException("更新子设备状态失败", e);
        }
    }
    
    /**
     * 根据主机地址更新设备状态（私有方法）
     */
    private int updateDevicesStatusByHost(String host, String status) {
        try {
            int updatedCount = iotSubDeviceService.updateStatusByHost(host, status);
            
            // 如果更新成功，记录日志
            if (updatedCount > 0) {
                log.debug("更新设备状态: 主机={}, 状态={}, 数量={}", host, status, updatedCount);
            }
            
            return updatedCount;
            
        } catch (Exception e) {
            log.error("更新设备状态失败: host={}, status={}", host, status, e);
            throw new RuntimeException("更新设备状态失败", e);
        }
    }
}