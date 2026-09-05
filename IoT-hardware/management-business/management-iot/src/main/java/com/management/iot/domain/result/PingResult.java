package com.management.iot.domain.result;

import lombok.Data;

/**
 * Ping检测结果实体类
 * 用于存储单次Ping操作的详细结果
 */
@Data
public class PingResult {
    /**
     * Ping序列号（第几次Ping）
     */
    private int sequence;
    
    /**
     * 是否可达
     */
    private boolean reachable;
    
    /**
     * 响应时间（毫秒）
     */
    private long responseTime;
    
    /**
     * 主机地址
     */
    private String hostAddress;
    
    /**
     * 错误信息（如果Ping失败）
     */
    private String errorMessage;
    
    @Override
    public String toString() {
        if (reachable) {
            return String.format("来自 %s 的回复：字节=32 时间=%dms TTL=128", 
                hostAddress, responseTime);
        } else {
            return String.format("请求超时。错误：%s", errorMessage);
        }
    }
}