package com.management.camera.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 摄像头信息实体类
 */
@Data
@TableName(value = "iot_camera_info")
public class IotCameraInfo {
    
    /**
     * 主键ID
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long cameraId;
    
    /**
     * 摄像头名称
     */
    private String cameraName;
    
    /**
     * 摄像头编码
     */
    private String cameraCode;
    
    /**
     * 摄像头类型（球机，枪机等）
     */
    private String cameraType;
    
    /**
     * IP地址
     */
    private String ipAddress;
    
    /**
     * 端口号
     */
    private Integer port;
    
    /**
     * 安装位置
     */
    private String location;
    
    /**
     * 状态（ONLINE/OFFLINE/FAULT）
     */
    private String status;
    
    /**
     * 厂商
     */
    private String manufacturer;
    
    /**
     * 型号
     */
    private String model;
    
    /**
     * 分辨率
     */
    private String resolution;
    
    /**
     * 视频流地址
     */
    private String streamUrl;
    
    /**
     * 主设备ID
     */
    private Long deviceId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 修改时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 备注
     */
    private String remark;
}