package com.management.iot.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 设备控制记录实体类
 * 对应数据库表：自动化控制记录表
 *
 */
@Data
@TableName(value = "jy_management.iot_automation_record") // 请根据实际表名调整
public class IotAutomationRecord {
    
    /**
     * 自动化记录主键
     */
    private Long recordId;
    
    /**
     * 设备协议ID
     */
    private Long protocolId;
    
    /**
     * 控制类型：1-手动 2-自动化
     */
    private String controlType;
    
    /**
     * 设备类型：1-风扇 2-紫外线灯 3-喷淋器 4-水帘幕 5-窗帘
     */
    private String deviceType;
    
    /**
     * 操作状态：0-关闭 1-开启 2-暂停
     */
    private String operationStatus;
    
    /**
     * 传感器数据跟踪
     */
    private String sensorData;
    
    /**
     * 控制原因
     */
    private String controlReason;
    
    /**
     * 设置阈值
     */
    private String thresholdSettings;
    
    /**
     * 执行结果：1-成功 0-失败
     */
    private String executeResult;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 修改时间
     */
    private Date updateTime;
    
    // 以下是一些实用的枚举定义，建议在实际项目中使用
    
    /**
     * 控制类型枚举
     */
    public enum ControlType {
        MANUAL("1", "手动"),
        AUTOMATION("2", "自动化");
        
        private final String code;
        private final String description;
        
        ControlType(String code, String description) {
            this.code = code;
            this.description = description;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 设备类型枚举
     */
    public enum DeviceType {
        FAN("1", "风扇"),
        UV_LIGHT("2", "紫外线灯"),
        SPRINKLER("3", "喷淋器"),
        WATER_CURTAIN("4", "水帘幕"),
        CURTAIN("5", "窗帘");
        
        private final String code;
        private final String description;
        
        DeviceType(String code, String description) {
            this.code = code;
            this.description = description;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 操作状态枚举
     */
    public enum OperationStatus {
        OFF("0", "关闭"),
        ON("1", "开启"),
        PAUSED("2", "暂停");
        
        private final String code;
        private final String description;
        
        OperationStatus(String code, String description) {
            this.code = code;
            this.description = description;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 执行结果枚举
     */
    public enum ExecuteResult {
        FAILED("0", "失败"),
        SUCCESS("1", "成功");
        
        private final String code;
        private final String description;
        
        ExecuteResult(String code, String description) {
            this.code = code;
            this.description = description;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getDescription() {
            return description;
        }
    }
}