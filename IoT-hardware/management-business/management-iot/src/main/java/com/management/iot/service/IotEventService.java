package com.management.iot.service;


import com.management.iot.domain.IotEvent;

import java.util.List;

/**
 * 设备事件记录服务接口
 * 定义事件相关的业务逻辑方法
 */
public interface IotEventService {
    
    /**
     * 获取所有事件记录
     * @return 事件列表
     */
    List<IotEvent> getAllEvents();
    
    /**
     * 根据事件ID获取事件
     * @param eventId 事件ID
     * @return 事件信息
     */
    IotEvent getEventById(Long eventId);
    
    /**
     * 根据设备ID获取事件列表
     * @param deviceId 设备ID
     * @return 事件列表
     */
    List<IotEvent> getEventsByDeviceId(Long deviceId);
    
    /**
     * 根据事件类型获取事件列表
     * @param eventType 事件类型
     * @return 事件列表
     */
    List<IotEvent> getEventsByType(String eventType);
    
    /**
     * 保存事件记录
     * @param event 事件信息
     * @return 是否保存成功
     */
    boolean saveEvent(IotEvent event);
    
    /**
     * 批量保存事件记录
     * @param events 事件列表
     * @return 是否保存成功
     */
    boolean batchSaveEvents(List<IotEvent> events);
    
    /**
     * 根据时间范围查询事件
     * @param deviceId 设备ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 事件列表
     */
    List<IotEvent> getEventsByTimeRange(Long deviceId, String startTime, String endTime);
    
    /**
     * 记录设备上线事件
     * @param deviceId 设备ID
     * @param deviceName 设备名称
     */
    void recordDeviceOnlineEvent(Long deviceId, String deviceName);
    
    /**
     * 记录设备离线事件
     * @param deviceId 设备ID
     * @param deviceName 设备名称
     */
    void recordDeviceOfflineEvent(Long deviceId, String deviceName);
    
    /**
     * 记录设备告警事件
     * @param deviceId 设备ID
     * @param deviceName 设备名称
     * @param description 告警描述
     */
    void recordDeviceAlarmEvent(Long deviceId, String deviceName, String description);
    
    /**
     * 统计指定类型的事件数量
     * @param eventType 事件类型
     * @return 事件数量
     */
    int getEventCountByType(String eventType);
}