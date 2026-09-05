package com.management.iot.service.impl;

import com.management.iot.domain.IotDevice;
import com.management.iot.domain.IotEvent;
import com.management.iot.mapper.IotDeviceMapper;
import com.management.iot.mapper.IotEventMapper;
import com.management.iot.service.IotEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 设备事件记录服务实现类
 * 实现事件相关的业务逻辑
 */
@Service
public class IotEventServiceImpl implements IotEventService {

    private static final Logger logger = LoggerFactory.getLogger(IotEventServiceImpl.class);
    
    @Autowired
    private IotEventMapper eventMapper;
    
    @Autowired
    private IotDeviceMapper deviceMapper;

    /**
     * 获取所有事件记录
     * @return 事件列表
     */
    @Override
    public List<IotEvent> getAllEvents() {
        try {
            return eventMapper.selectAllEvents();
        } catch (Exception e) {
            logger.error("获取事件记录列表失败", e);
            throw new RuntimeException("获取事件记录列表失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据事件ID获取事件
     * @param eventId 事件ID
     * @return 事件信息
     */
    @Override
    public IotEvent getEventById(Long eventId) {
        if (eventId == null || eventId <= 0) {
            throw new IllegalArgumentException("事件ID不能为空且必须大于0");
        }
        try {
            IotEvent event = eventMapper.selectEventById(eventId);
            if (event == null) {
                throw new RuntimeException("事件记录不存在，事件ID: " + eventId);
            }
            return event;
        } catch (Exception e) {
            logger.error("获取事件记录失败，事件ID: {}", eventId, e);
            throw new RuntimeException("获取事件记录失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据设备ID获取事件列表
     * @param deviceId 设备ID
     * @return 事件列表
     */
    @Override
    public List<IotEvent> getEventsByDeviceId(Long deviceId) {
        if (deviceId == null || deviceId <= 0) {
            throw new IllegalArgumentException("设备ID不能为空且必须大于0");
        }
        try {
            return eventMapper.selectEventsByDeviceId(deviceId);
        } catch (Exception e) {
            logger.error("根据设备ID获取事件列表失败，设备ID: {}", deviceId, e);
            throw new RuntimeException("根据设备ID获取事件列表失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据事件类型获取事件列表
     * @param eventType 事件类型
     * @return 事件列表
     */
    @Override
    public List<IotEvent> getEventsByType(String eventType) {
        if (eventType == null || eventType.trim().isEmpty()) {
            throw new IllegalArgumentException("事件类型不能为空");
        }
        try {
            return eventMapper.selectEventsByType(eventType.trim());
        } catch (Exception e) {
            logger.error("根据事件类型获取事件列表失败，事件类型: {}", eventType, e);
            throw new RuntimeException("根据事件类型获取事件列表失败: " + e.getMessage(), e);
        }
    }

    /**
     * 保存事件记录
     * @param event 事件信息
     * @return 是否保存成功
     */
    @Override
    @Transactional
    public boolean saveEvent(IotEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("事件信息不能为空");
        }
        if (event.getDeviceId() == null || event.getDeviceId() <= 0) {
            throw new IllegalArgumentException("设备ID不能为空且必须大于0");
        }
        if (event.getEventType() == null || event.getEventType().trim().isEmpty()) {
            throw new IllegalArgumentException("事件类型不能为空");
        }
        
        try {
            // 检查设备是否存在
            IotDevice device = deviceMapper.selectDeviceById(event.getDeviceId());
            if (device == null) {
                throw new RuntimeException("设备不存在，设备ID: " + event.getDeviceId());
            }
            
            int result = eventMapper.insertEvent(event);
            boolean success = result > 0;
            
            if (success) {
                logger.info("成功保存事件记录，事件ID: {}, 设备ID: {}, 事件类型: {}", 
                           event.getEventId(), event.getDeviceId(), event.getEventType());
            }
            
            return success;
        } catch (Exception e) {
            logger.error("保存事件记录失败，设备ID: {}, 事件类型: {}", event.getDeviceId(), event.getEventType(), e);
            throw new RuntimeException("保存事件记录失败: " + e.getMessage(), e);
        }
    }

    /**
     * 批量保存事件记录
     * @param events 事件列表
     * @return 是否保存成功
     */
    @Override
    @Transactional
    public boolean batchSaveEvents(List<IotEvent> events) {
        if (events == null || events.isEmpty()) {
            throw new IllegalArgumentException("事件列表不能为空");
        }
        
        try {
            // 验证所有设备ID和事件类型
            for (IotEvent event : events) {
                if (event.getDeviceId() == null || event.getDeviceId() <= 0) {
                    throw new IllegalArgumentException("设备ID不能为空且必须大于0");
                }
                if (event.getEventType() == null || event.getEventType().trim().isEmpty()) {
                    throw new IllegalArgumentException("事件类型不能为空");
                }
                
                // 检查设备是否存在
                IotDevice device = deviceMapper.selectDeviceById(event.getDeviceId());
                if (device == null) {
                    throw new RuntimeException("设备不存在，设备ID: " + event.getDeviceId());
                }
            }
            
            int result = eventMapper.batchInsertEvents(events);
            boolean success = result > 0;
            
            if (success) {
                logger.info("批量保存事件记录成功，共 {} 条记录", events.size());
            }
            
            return success;
        } catch (Exception e) {
            logger.error("批量保存事件记录失败", e);
            throw new RuntimeException("批量保存事件记录失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据时间范围查询事件
     * @param deviceId 设备ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 事件列表
     */
    @Override
    public List<IotEvent> getEventsByTimeRange(Long deviceId, String startTime, String endTime) {
        if (deviceId == null || deviceId <= 0) {
            throw new IllegalArgumentException("设备ID不能为空且必须大于0");
        }
        if (startTime == null || startTime.trim().isEmpty()) {
            throw new IllegalArgumentException("开始时间不能为空");
        }
        if (endTime == null || endTime.trim().isEmpty()) {
            throw new IllegalArgumentException("结束时间不能为空");
        }
        
        try {
            return eventMapper.selectEventsByTimeRange(deviceId, startTime.trim(), endTime.trim());
        } catch (Exception e) {
            logger.error("根据时间范围查询事件失败，设备ID: {}, 时间范围: {} - {}", deviceId, startTime, endTime, e);
            throw new RuntimeException("根据时间范围查询事件失败: " + e.getMessage(), e);
        }
    }

    /**
     * 记录设备上线事件
     * @param deviceId 设备ID
     * @param deviceName 设备名称
     */
    @Override
    @Transactional
    public void recordDeviceOnlineEvent(Long deviceId, String deviceName) {
        if (deviceId == null || deviceId <= 0) {
            throw new IllegalArgumentException("设备ID不能为空且必须大于0");
        }
        
        try {
            IotEvent event = new IotEvent();
            event.setDeviceId(deviceId);
            event.setEventType("INFO");
            event.setDescription("设备上线: " + deviceName);
            
            boolean saved = saveEvent(event);
            if (saved) {
                logger.info("记录设备上线事件成功，设备ID: {}, 设备名称: {}", deviceId, deviceName);
            } else {
                logger.error("记录设备上线事件失败，设备ID: {}, 设备名称: {}", deviceId, deviceName);
            }
        } catch (Exception e) {
            logger.error("记录设备上线事件异常，设备ID: {}, 设备名称: {}", deviceId, deviceName, e);
            // 不抛出异常，避免影响主要业务流程
        }
    }

    /**
     * 记录设备离线事件
     * @param deviceId 设备ID
     * @param deviceName 设备名称
     */
    @Override
    @Transactional
    public void recordDeviceOfflineEvent(Long deviceId, String deviceName) {
        if (deviceId == null || deviceId <= 0) {
            throw new IllegalArgumentException("设备ID不能为空且必须大于0");
        }
        
        try {
            IotEvent event = new IotEvent();
            event.setDeviceId(deviceId);
            event.setEventType("WARNING");
            event.setDescription("设备离线: " + deviceName);
            
            boolean saved = saveEvent(event);
            if (saved) {
                logger.info("记录设备离线事件成功，设备ID: {}, 设备名称: {}", deviceId, deviceName);
            } else {
                logger.error("记录设备离线事件失败，设备ID: {}, 设备名称: {}", deviceId, deviceName);
            }
        } catch (Exception e) {
            logger.error("记录设备离线事件异常，设备ID: {}, 设备名称: {}", deviceId, deviceName, e);
            // 不抛出异常，避免影响主要业务流程
        }
    }

    /**
     * 记录设备告警事件
     * @param deviceId 设备ID
     * @param deviceName 设备名称
     * @param description 告警描述
     */
    @Override
    @Transactional
    public void recordDeviceAlarmEvent(Long deviceId, String deviceName, String description) {
        if (deviceId == null || deviceId <= 0) {
            throw new IllegalArgumentException("设备ID不能为空且必须大于0");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("告警描述不能为空");
        }
        
        try {
            IotEvent event = new IotEvent();
            event.setDeviceId(deviceId);
            event.setEventType("ALARM");
            event.setDescription("设备告警[" + deviceName + "]: " + description.trim());
            
            boolean saved = saveEvent(event);
            if (saved) {
                logger.warn("记录设备告警事件成功，设备ID: {}, 设备名称: {}, 告警描述: {}", deviceId, deviceName, description);
            } else {
                logger.error("记录设备告警事件失败，设备ID: {}, 设备名称: {}, 告警描述: {}", deviceId, deviceName, description);
            }
        } catch (Exception e) {
            logger.error("记录设备告警事件异常，设备ID: {}, 设备名称: {}, 告警描述: {}", deviceId, deviceName, description, e);
            // 不抛出异常，避免影响主要业务流程
        }
    }

    /**
     * 统计指定类型的事件数量
     * @param eventType 事件类型
     * @return 事件数量
     */
    @Override
    public int getEventCountByType(String eventType) {
        if (eventType == null || eventType.trim().isEmpty()) {
            throw new IllegalArgumentException("事件类型不能为空");
        }
        try {
            return eventMapper.countEventsByType(eventType.trim());
        } catch (Exception e) {
            logger.error("统计事件数量失败，事件类型: {}", eventType, e);
            throw new RuntimeException("统计事件数量失败: " + e.getMessage(), e);
        }
    }
}