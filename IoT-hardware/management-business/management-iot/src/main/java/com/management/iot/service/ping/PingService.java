package com.management.iot.service.ping;


import com.management.iot.domain.result.PingResult;
import com.management.iot.domain.result.PingStatistics;

import java.util.List;

/**
 * Ping检测服务接口
 */
public interface PingService {
    
    /**
     * 执行单个Ping检测
     * @param ipAddress IP地址
     * @param timeout 超时时间（毫秒）
     * @return Ping检测结果
     */
    PingResult ping(String ipAddress, int timeout);
    
    /**
     * 连续执行多次Ping检测
     * @param ipAddress IP地址
     * @param count Ping次数
     * @param timeout 超时时间（毫秒）
     * @return Ping结果列表
     */
    List<PingResult> multiplePing(String ipAddress, int count, int timeout);
    
    /**
     * 生成Ping统计信息
     * @param results Ping结果列表
     * @param ipAddress IP地址
     * @return 统计信息
     */
    PingStatistics generateStatistics(List<PingResult> results, String ipAddress);
    
    /**
     * 打印Ping统计信息
     * @param results Ping结果列表
     * @param ipAddress IP地址
     */
    void printStatistics(List<PingResult> results, String ipAddress);
    
    /**
     * 根据统计信息判断设备是否在线
     * @param statistics Ping统计信息
     * @return true-在线，false-离线
     */
    boolean isDeviceOnline(PingStatistics statistics);
    
    /**
     * 快速检查IP是否可达（用于定时任务）
     * @param ipAddress IP地址
     * @return true-可达，false-不可达
     */
    boolean quickPingCheck(String ipAddress);
}