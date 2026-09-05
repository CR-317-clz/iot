package com.management.iot.mapper;

import com.management.iot.domain.IotEvent;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 设备事件记录数据访问层接口
 */
@Mapper
public interface IotEventMapper {
    
    /**
     * 查询所有事件记录
     * @return 事件列表
     */
    List<IotEvent> selectAllEvents();
    
    /**
     * 根据事件ID查询事件
     * @param eventId 事件ID
     * @return 事件信息
     */
    IotEvent selectEventById(@Param("eventId") Long eventId);
    
    /**
     * 根据设备ID查询事件列表
     * @param deviceId 设备ID
     * @return 事件列表
     */
    List<IotEvent> selectEventsByDeviceId(@Param("deviceId") Long deviceId);
    
    /**
     * 根据事件类型查询事件列表
     * @param eventType 事件类型
     * @return 事件列表
     */
    List<IotEvent> selectEventsByType(@Param("eventType") String eventType);
    
    /**
     * 插入新事件记录
     * @param event 事件实体对象
     * @return 插入记录数
     */
    int insertEvent(IotEvent event);
    
    /**
     * 批量插入事件记录
     * @param events 事件列表
     * @return 插入记录数
     */
    int batchInsertEvents(@Param("events") List<IotEvent> events);
    
    /**
     * 根据设备ID和时间范围查询事件
     * @param deviceId 设备ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 事件列表
     */
    List<IotEvent> selectEventsByTimeRange(@Param("deviceId") Long deviceId,
                                          @Param("startTime") String startTime, 
                                          @Param("endTime") String endTime);
    
    /**
     * 根据事件ID删除事件记录
     * @param eventId 事件ID
     * @return 删除记录数
     */
    int deleteEventById(@Param("eventId") Integer eventId);
    
    /**
     * 统计指定类型的事件数量
     * @param eventType 事件类型
     * @return 事件数量
     */
    int countEventsByType(@Param("eventType") String eventType);
}