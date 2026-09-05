package com.management.iot.kunlun;

import java.util.Map;

/**
 * 继电器控制指令
 */
public class RelayControlCommand {
    
    // 设备名称 -> 目标状态 (0=关闭, 1=开启)
    private Map<String, Integer> relayStatus;
    
    // 设备ID（可选）
    private Long deviceId;
    
    // 时间戳
    private Long timestamp;
    
    public Map<String, Integer> getRelayStatus() {
        return relayStatus;
    }
    
    public void setRelayStatus(Map<String, Integer> relayStatus) {
        this.relayStatus = relayStatus;
    }
    
    public Long getDeviceId() {
        return deviceId;
    }
    
    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }
    
    public Long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
    
    @Override
    public String toString() {
        return "RelayControlCommand{" +
                "relayStatus=" + relayStatus +
                ", deviceId=" + deviceId +
                ", timestamp=" + timestamp +
                '}';
    }
}