package com.management.iot.service;


import com.management.iot.domain.IotMessage;

import java.util.List;

/**
 * 设备通信消息服务接口
 * 定义消息相关的业务逻辑方法
 */
public interface IotMessageService {
    
    /**
     * 获取所有消息记录
     * @return 消息列表
     */
    List<IotMessage> getAllMessages();
    
    /**
     * 根据消息ID获取消息
     * @param msgId 消息ID
     * @return 消息信息
     */
    IotMessage getMessageById(Long msgId);
    
    /**
     * 根据设备ID获取消息列表
     * @param deviceId 设备ID
     * @return 消息列表
     */
    List<IotMessage> getMessagesByDeviceId(Long deviceId);
    
    /**
     * 根据主题获取消息列表
     * @param topic 消息主题
     * @return 消息列表
     */
    List<IotMessage> getMessagesByTopic(String topic);
    
    /**
     * 保存消息记录
     * @param message 消息信息
     * @return 是否保存成功
     */
    boolean saveMessage(IotMessage message);
    
    /**
     * 批量保存消息记录
     * @param messages 消息列表
     * @return 是否保存成功
     */
    boolean batchSaveMessages(List<IotMessage> messages);
    
    /**
     * 更新消息解析状态
     * @param msgId 消息ID
     * @param parsed 解析状态
     * @return 是否更新成功
     */
    boolean updateMessageParsedStatus(Long msgId, Boolean parsed);
    
    /**
     * 获取未解析的消息数量
     * @return 未解析消息数量
     */
    int getUnparsedMessageCount();
    
    /**
     * 处理MQTT消息
     * @param topic 消息主题
     * @param payload 消息内容
     */
    void processMqttMessage(String topic, String payload);
}