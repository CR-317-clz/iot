package com.management.iot.service.ping.impl;

import com.management.iot.domain.result.PingResult;
import com.management.iot.domain.result.PingStatistics;
import com.management.iot.service.ping.PingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Ping检测服务实现类
 */
@Service
@Slf4j
public class PingServiceImpl implements PingService {
    
    /**
     * 执行单个Ping检测
     */
    @Override
    public PingResult ping(String ipAddress, int timeout) {
        PingResult result = new PingResult();
        
        try {
            // 解析IP地址
            InetAddress inet = InetAddress.getByName(ipAddress);
            long startTime = System.currentTimeMillis();
            
            // 执行Ping检测
            boolean isReachable = inet.isReachable(timeout);
            long endTime = System.currentTimeMillis();
            
            // 设置结果
            result.setReachable(isReachable);
            result.setResponseTime(endTime - startTime);
            result.setHostAddress(inet.getHostAddress());
            
            log.debug("Ping检测完成: {} -> {}", ipAddress, isReachable ? "可达" : "不可达");
            
        } catch (Exception e) {
            // Ping失败处理
            result.setReachable(false);
            result.setErrorMessage(e.getMessage());
            log.error("Ping操作失败: {}, 错误: {}", ipAddress, e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 连续执行多次Ping检测
     */
    @Override
    public List<PingResult> multiplePing(String ipAddress, int count, int timeout) {
        List<PingResult> results = new ArrayList<>();
        
        log.info("正在 Ping {} 具有 32 字节的数据：", ipAddress);
        
        for (int i = 0; i < count; i++) {
            // 执行单次Ping
            PingResult result = ping(ipAddress, timeout);
            result.setSequence(i + 1);
            results.add(result);
            
            // 输出单次Ping结果
            log.info(result.toString());
            
            // 如果不是最后一次Ping，等待1秒
            if (i < count - 1) {
                try {
                    Thread.sleep(1000); // 间隔1秒
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("Ping操作被中断");
                    break;
                }
            }
        }
        
        return results;
    }
    
    /**
     * 生成Ping统计信息
     */
    @Override
    public PingStatistics generateStatistics(List<PingResult> results, String ipAddress) {
        int sent = results.size();
        int received = (int) results.stream().filter(PingResult::isReachable).count();
        int lost = sent - received;
        double lostPercent = (double) lost / sent * 100;
        
        // 计算响应时间统计（只计算成功的请求）
        List<Long> responseTimes = results.stream()
            .filter(PingResult::isReachable)
            .map(PingResult::getResponseTime)
            .collect(Collectors.toList());
        
        long min = responseTimes.stream().min(Long::compare).orElse(0L);
        long max = responseTimes.stream().max(Long::compare).orElse(0L);
        double average = responseTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);
        
        // 创建统计信息对象
        PingStatistics statistics = new PingStatistics();
        statistics.setHost(ipAddress);
        statistics.setSent(sent);
        statistics.setReceived(received);
        statistics.setLost(lost);
        statistics.setLostPercent(lostPercent);
        statistics.setMinResponseTime(min);
        statistics.setMaxResponseTime(max);
        statistics.setAvgResponseTime(average);
        statistics.setCheckTime(new java.util.Date());
        
        return statistics;
    }
    
    /**
     * 打印Ping统计信息
     */
    @Override
    public void printStatistics(List<PingResult> results, String ipAddress) {
        PingStatistics statistics = generateStatistics(results, ipAddress);
        log.info(statistics.toString());
    }
    
    /**
     * 根据统计信息判断设备是否在线
     */
    @Override
    public boolean isDeviceOnline(PingStatistics statistics) {
        // 判断逻辑：成功率大于50%且至少有一个成功响应
        double successRate = (double) statistics.getReceived() / statistics.getSent();
        boolean isOnline = successRate > 0.5 && statistics.getReceived() >= 1;
        
        log.debug("设备在线判断: 主机={}, 成功率={:.1f}%, 成功次数={}, 结果={}",
                statistics.getHost(), successRate * 100, statistics.getReceived(), 
                isOnline ? "在线" : "离线");
        
        return isOnline;
    }
    
    /**
     * 快速检查IP是否可达（用于定时任务）
     */
    @Override
    public boolean quickPingCheck(String ipAddress) {
        try {
            // 使用较短的超时时间进行快速检测
            PingResult result = ping(ipAddress, 3000); // 3秒超时
            boolean isReachable = result.isReachable();
            
            log.debug("快速Ping检查: {} -> {}", ipAddress, isReachable ? "可达" : "不可达");
            return isReachable;
            
        } catch (Exception e) {
            log.error("快速Ping检查失败: {}", ipAddress, e);
            return false;
        }
    }
}