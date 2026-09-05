package com.management.iot.domain.result;

import lombok.Data;
import java.util.Date;

/**
 * Ping统计信息实体类
 * 用于存储多次Ping操作的统计结果
 */
@Data
public class PingStatistics {
    /**
     * 目标主机地址
     */
    private String host;
    
    /**
     * 发送的数据包数量
     */
    private int sent;
    
    /**
     * 接收的数据包数量
     */
    private int received;
    
    /**
     * 丢失的数据包数量
     */
    private int lost;
    
    /**
     * 丢包率（百分比）
     */
    private double lostPercent;
    
    /**
     * 最小响应时间（毫秒）
     */
    private long minResponseTime;
    
    /**
     * 最大响应时间（毫秒）
     */
    private long maxResponseTime;
    
    /**
     * 平均响应时间（毫秒）
     */
    private double avgResponseTime;
    
    /**
     * 统计时间
     */
    private Date checkTime;
    
    @Override
    public String toString() {
        return String.format(
            "%s 的 Ping 统计信息：\n" +
            "    数据包：已发送 = %d，已接收 = %d，丢失 = %d (%.0f%% 丢失)\n" +
            "往返行程的估计时间（以毫秒为单位）：\n" +
            "    最短 = %dms，最长 = %dms，平均 = %.0fms",
            host, sent, received, lost, lostPercent, minResponseTime, maxResponseTime, avgResponseTime
        );
    }
}