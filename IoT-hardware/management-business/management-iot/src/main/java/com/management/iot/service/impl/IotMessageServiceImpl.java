package com.management.iot.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.management.iot.domain.IotDevice;
import com.management.iot.domain.IotMessage;
import com.management.iot.mapper.IotDeviceMapper;
import com.management.iot.mapper.IotMessageMapper;
import com.management.iot.service.IotEventService;
import com.management.iot.service.IotMessageService;
import com.management.iot.service.IotTelemetryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 设备通信消息服务实现类
 * 实现消息相关的业务逻辑，包括MQTT消息处理
 */
@Service
public class IotMessageServiceImpl implements IotMessageService {

    private static final Logger logger = LoggerFactory.getLogger(IotMessageServiceImpl.class);
    
    @Autowired
    private IotMessageMapper messageMapper;
    
    @Autowired
    private IotDeviceMapper deviceMapper;
    
    @Autowired
    private IotTelemetryService telemetryService;
    
    @Autowired
    private IotEventService eventService;
    
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 获取所有消息记录
     * @return 消息列表
     */
    @Override
    public List<IotMessage> getAllMessages() {
        try {
            return messageMapper.selectAllMessages();
        } catch (Exception e) {
            logger.error("获取消息记录列表失败", e);
            throw new RuntimeException("获取消息记录列表失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据消息ID获取消息
     * @param msgId 消息ID
     * @return 消息信息
     */
    @Override
    public IotMessage getMessageById(Long msgId) {
        if (msgId == null || msgId <= 0) {
            throw new IllegalArgumentException("消息ID不能为空且必须大于0");
        }
        try {
            IotMessage message = messageMapper.selectMessageById(msgId);
            if (message == null) {
                throw new RuntimeException("消息记录不存在，消息ID: " + msgId);
            }
            return message;
        } catch (Exception e) {
            logger.error("获取消息记录失败，消息ID: {}", msgId, e);
            throw new RuntimeException("获取消息记录失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据设备ID获取消息列表
     * @param deviceId 设备ID
     * @return 消息列表
     */
    @Override
    public List<IotMessage> getMessagesByDeviceId(Long deviceId) {
        if (deviceId == null || deviceId <= 0) {
            throw new IllegalArgumentException("设备ID不能为空且必须大于0");
        }
        try {
            return messageMapper.selectMessagesByDeviceId(deviceId);
        } catch (Exception e) {
            logger.error("根据设备ID获取消息列表失败，设备ID: {}", deviceId, e);
            throw new RuntimeException("根据设备ID获取消息列表失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据主题获取消息列表
     * @param topic 消息主题
     * @return 消息列表
     */
    @Override
    public List<IotMessage> getMessagesByTopic(String topic) {
        if (topic == null || topic.trim().isEmpty()) {
            throw new IllegalArgumentException("消息主题不能为空");
        }
        try {
            return messageMapper.selectMessagesByTopic(topic.trim());
        } catch (Exception e) {
            logger.error("根据主题获取消息列表失败，主题: {}", topic, e);
            throw new RuntimeException("根据主题获取消息列表失败: " + e.getMessage(), e);
        }
    }

    /**
     * 保存消息记录
     * @param message 消息信息
     * @return 是否保存成功
     */
    @Override
    @Transactional
    public boolean saveMessage(IotMessage message) {
        if (message == null) {
            throw new IllegalArgumentException("消息信息不能为空");
        }
        if (message.getDeviceId() == null || message.getDeviceId() <= 0) {
            throw new IllegalArgumentException("设备ID不能为空且必须大于0");
        }
        
        try {
            // 检查设备是否存在
            IotDevice device = deviceMapper.selectDeviceById(message.getDeviceId());
            if (device == null) {
                throw new RuntimeException("设备不存在，设备ID: " + message.getDeviceId());
            }
            
            int result = messageMapper.insertMessage(message);
            boolean success = result > 0;
            
            if (success) {
                logger.info("成功保存消息记录，消息ID: {}, 设备ID: {}", message.getMsgId(), message.getDeviceId());
            }
            
            return success;
        } catch (Exception e) {
            logger.error("保存消息记录失败，设备ID: {}", message.getDeviceId(), e);
            throw new RuntimeException("保存消息记录失败: " + e.getMessage(), e);
        }
    }

    /**
     * 批量保存消息记录
     * @param messages 消息列表
     * @return 是否保存成功
     */
    @Override
    @Transactional
    public boolean batchSaveMessages(List<IotMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            throw new IllegalArgumentException("消息列表不能为空");
        }
        
        try {
            // 验证所有设备ID是否存在
            for (IotMessage message : messages) {
                if (message.getDeviceId() == null || message.getDeviceId() <= 0) {
                    throw new IllegalArgumentException("设备ID不能为空且必须大于0");
                }
                IotDevice device = deviceMapper.selectDeviceById(message.getDeviceId());
                if (device == null) {
                    throw new RuntimeException("设备不存在，设备ID: " + message.getDeviceId());
                }
            }
            
            int result = messageMapper.batchInsertMessages(messages);
            boolean success = result > 0;
            
            if (success) {
                logger.info("批量保存消息记录成功，共 {} 条记录", messages.size());
            }
            
            return success;
        } catch (Exception e) {
            logger.error("批量保存消息记录失败", e);
            throw new RuntimeException("批量保存消息记录失败: " + e.getMessage(), e);
        }
    }

    /**
     * 更新消息解析状态
     * @param msgId 消息ID
     * @param parsed 解析状态
     * @return 是否更新成功
     */
    @Override
    @Transactional
    public boolean updateMessageParsedStatus(Long msgId, Boolean parsed) {
        if (msgId == null || msgId <= 0) {
            throw new IllegalArgumentException("消息ID不能为空且必须大于0");
        }
        if (parsed == null) {
            throw new IllegalArgumentException("解析状态不能为空");
        }
        
        try {
            int result = messageMapper.updateMessageParsedStatus(msgId, parsed);
            boolean success = result > 0;
            
            if (success) {
                logger.debug("更新消息解析状态成功，消息ID: {}, 解析状态: {}", msgId, parsed);
            }
            
            return success;
        } catch (Exception e) {
            logger.error("更新消息解析状态失败，消息ID: {}", msgId, e);
            throw new RuntimeException("更新消息解析状态失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取未解析的消息数量
     * @return 未解析消息数量
     */
    @Override
    public int getUnparsedMessageCount() {
        try {
            return messageMapper.countUnparsedMessages();
        } catch (Exception e) {
            logger.error("获取未解析消息数量失败", e);
            throw new RuntimeException("获取未解析消息数量失败: " + e.getMessage(), e);
        }
    }

    /**
     * 处理MQTT消息
     * @param topic 消息主题
     * @param payload 消息内容
     */
    @Override
    @Transactional
    public void processMqttMessage(String topic, String payload) {
        if (topic == null || topic.trim().isEmpty()) {
            logger.warn("MQTT消息主题为空，忽略处理");
            return;
        }
        if (payload == null || payload.trim().isEmpty()) {
            logger.warn("MQTT消息内容为空，主题: {}", topic);
            return;
        }
        
        try {
            logger.info("开始处理MQTT消息，主题: {}, 内容: {}", topic, payload);
            
            // 从主题中提取设备编码
            String deviceCode = extractDeviceCodeFromTopic(topic);
            if (deviceCode == null) {
                logger.warn("无法从主题中提取设备编码，主题: {}", topic);
                return;
            }
            
            // 根据设备编码查找设备
            IotDevice device = deviceMapper.selectDeviceByCode(deviceCode);
            if (device == null) {
                logger.warn("设备不存在，设备编码: {}", deviceCode);
                return;
            }
            
            // 保存原始消息
            IotMessage message = new IotMessage();
            message.setDeviceId(device.getDeviceId());
            message.setTopic(topic);
            message.setPayload(payload);
            message.setParsed(false);
            
            boolean saved = saveMessage(message);
            if (!saved) {
                logger.error("保存MQTT消息失败，设备编码: {}", deviceCode);
                return;
            }
            
            // 解析消息内容并保存时序数据
            boolean parsed = telemetryService.parseAndSaveTelemetry(message);
            if (parsed) {
                // 更新消息解析状态
                updateMessageParsedStatus(message.getMsgId(), true);
                logger.info("MQTT消息解析成功，消息ID: {}, 设备ID: {}", message.getMsgId(), device.getDeviceId());
                
                // 更新设备状态为在线
                IotDevice iotDevice = new IotDevice();
                iotDevice.setDeviceId(device.getDeviceId());
                iotDevice.setStatus("ONLINE");
                deviceMapper.updateDeviceStatus(iotDevice);
                
                // 记录设备在线事件
                eventService.recordDeviceOnlineEvent(device.getDeviceId(), device.getDeviceName());
            } else {
                logger.warn("MQTT消息解析失败，消息ID: {}, 设备ID: {}", message.getMsgId(), device.getDeviceId());
            }
            
        } catch (Exception e) {
            logger.error("处理MQTT消息失败，主题: {}, 内容: {}", topic, payload, e);
            throw new RuntimeException("处理MQTT消息失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 从主题中提取设备编码
     * @param topic MQTT主题
     * @return 设备编码
     */
    private String extractDeviceCodeFromTopic(String topic) {
        if (topic == null || topic.trim().isEmpty()) {
            return null;
        }
        
        // 假设主题格式为: sensor/{deviceCode}/data 或 device/{deviceCode}/status
        String[] parts = topic.split("/");
        if (parts.length >= 2) {
            return parts[1]; // 返回设备编码部分
        }
        
        return null;
    }
}