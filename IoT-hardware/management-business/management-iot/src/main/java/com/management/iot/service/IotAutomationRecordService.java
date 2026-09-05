package com.management.iot.service;

import com.management.iot.domain.IotAutomationRecord;

import java.util.List;

/**
 * 自动化记录业务逻辑层接口
 * 定义自动化操作记录相关的业务操作方法
 * @author iot
 */
public interface IotAutomationRecordService {
    
    /**
     * 新增自动化记录
     * 每次控制操作后调用此方法记录操作历史
     * @param record 自动化记录对象
     * @return 插入记录数
     */
    int addRecord(IotAutomationRecord record);
    
    /**
     * 查询自动化记录列表
     * 支持分页和条件查询，用于前端展示操作历史
     * @param record 查询条件对象
     * @return 记录列表
     */
    List<IotAutomationRecord> getRecordList(IotAutomationRecord record);
    
    /**
     * 根据ID查询自动化记录
     * @param recordId 记录ID
     * @return 自动化记录
     */
    IotAutomationRecord getRecordById(Long recordId);
    
    /**
     * 批量插入自动化记录
     * 用于高性能批量记录操作
     * @param records 记录列表
     * @return 插入记录数
     */
    int batchAddRecord(List<IotAutomationRecord> records);
    
    /**
     * 根据协议ID查询最新记录
     * 用于获取设备最近的操作状态
     * @param protocolId 协议ID
     * @return 最新的自动化记录
     */
    IotAutomationRecord getLatestRecordByProtocolId(Long protocolId);
}