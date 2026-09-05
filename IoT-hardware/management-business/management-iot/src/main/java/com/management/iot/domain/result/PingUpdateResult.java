package com.management.iot.domain.result;

import lombok.Data;

/**
 * Ping更新结果实体类
 * 用于存储Ping检测后的状态更新结果
 */
@Data
public class PingUpdateResult {
    /**
     * 目标主机地址
     */
    private String host;
    
    /**
     * 是否在线
     */
    private boolean online;
    
    /**
     * 状态是否发生变化
     */
    private boolean statusChanged;
    
    /**
     * 总设备数量
     */
    private int totalDevices;
    
    /**
     * 更新的设备数量
     */
    private int devicesUpdated;
    
    /**
     * 错误信息
     */
    private String error;
    
    public PingUpdateResult() {
    }
    
    public PingUpdateResult(String host) {
        this.host = host;
        this.online = false;
        this.statusChanged = false;
        this.totalDevices = 0;
        this.devicesUpdated = 0;
        this.error = null;
    }
}