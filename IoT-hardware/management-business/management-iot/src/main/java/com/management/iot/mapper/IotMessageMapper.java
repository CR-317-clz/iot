package com.management.iot.mapper;

import com.management.iot.domain.IotMessage;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 设备通信消息数据访问层接口
 */
@Mapper
public interface IotMessageMapper {
    
    /**
     * 查询所有消息记录
     * @return 消息列表
     */
    List<IotMessage> selectAllMessages();
    
    /**
     * 根据消息ID查询消息
     * @param msgId 消息ID
     * @return 消息信息
     */
    IotMessage selectMessageById(@Param("msgId") Long msgId);
    
    /**
     * 根据设备ID查询消息列表
     * @param deviceId 设备ID
     * @return 消息列表
     */
    List<IotMessage> selectMessagesByDeviceId(@Param("deviceId") Long deviceId);
    
    /**
     * 根据主题查询消息列表
     * @param topic 消息主题
     * @return 消息列表
     */
    List<IotMessage> selectMessagesByTopic(@Param("topic") String topic);
    
    /**
     * 插入新消息记录
     * @param message 消息实体对象
     * @return 插入记录数
     */
    int insertMessage(IotMessage message);
    
    /**
     * 批量插入消息记录
     * @param messages 消息列表
     * @return 插入记录数
     */
    int batchInsertMessages(@Param("messages") List<IotMessage> messages);
    
    /**
     * 更新消息解析状态
     * @param msgId 消息ID
     * @param parsed 解析状态
     * @return 更新记录数
     */
    int updateMessageParsedStatus(@Param("msgId") Long msgId, @Param("parsed") Boolean parsed);
    
    /**
     * 根据设备ID删除消息记录
     * @param deviceId 设备ID
     * @return 删除记录数
     */
    int deleteMessagesByDeviceId(@Param("deviceId") Long deviceId);
    
    /**
     * 统计未解析的消息数量
     * @return 未解析消息数量
     */
    int countUnparsedMessages();
}