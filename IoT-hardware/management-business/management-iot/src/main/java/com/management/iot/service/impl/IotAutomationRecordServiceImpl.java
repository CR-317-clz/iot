package com.management.iot.service.impl;

import com.management.iot.domain.IotAutomationRecord;
import com.management.iot.mapper.IotAutomationRecordMapper;
import com.management.iot.service.IotAutomationRecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 自动化记录业务逻辑层实现类
 * 实现自动化操作记录相关的业务逻辑
 * @author iot
 */
@Service
public class IotAutomationRecordServiceImpl implements IotAutomationRecordService {

    private static final Logger logger = LoggerFactory.getLogger(IotAutomationRecordServiceImpl.class);

    @Autowired
    private IotAutomationRecordMapper automationRecordMapper;

    /**
     * 新增自动化记录
     * 记录每次控制操作的详细信息，用于审计和问题排查
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int addRecord(IotAutomationRecord record) {
        logger.debug("新增自动化记录，protocolId: {}, deviceType: {}", 
                   record.getProtocolId(), record.getDeviceType());
        
        // 参数校验
        if (record.getProtocolId() == null) {
            logger.warn("协议ID为空，无法记录自动化操作");
            return 0;
        }
        
        try {
            record.setCreateTime(new Date());
            record.setUpdateTime(new Date());
            
            int result = automationRecordMapper.insertRecord(record);
            if (result > 0) {
                logger.debug("成功记录自动化操作，recordId: {}, protocolId: {}", 
                           record.getRecordId(), record.getProtocolId());
            } else {
                logger.warn("记录自动化操作失败，protocolId: {}", record.getProtocolId());
            }
            return result;
        } catch (Exception e) {
            logger.error("新增自动化记录失败，protocolId: {}", record.getProtocolId(), e);
            // 记录失败不应该影响主要业务逻辑，所以只记录错误不抛出异常
            return 0;
        }
    }

    /**
     * 查询自动化记录列表
     * 支持多种条件组合查询，用于前端展示操作历史
     */
    @Override
    public List<IotAutomationRecord> getRecordList(IotAutomationRecord record) {
        logger.debug("查询自动化记录列表，protocolId: {}, controlType: {}", 
                   record.getProtocolId(), record.getControlType());
        
        try {
            List<IotAutomationRecord> records = automationRecordMapper.selectRecordList(record);
            logger.debug("成功查询到 {} 条自动化记录", records.size());
            return records;
        } catch (Exception e) {
            logger.error("查询自动化记录列表失败", e);
            throw new RuntimeException("查询操作记录失败", e);
        }
    }

    /**
     * 根据ID查询自动化记录
     * 用于查看单条记录的详细信息
     */
    @Override
    public IotAutomationRecord getRecordById(Long recordId) {
        logger.debug("根据ID查询自动化记录，recordId: {}", recordId);
        
        if (recordId == null) {
            throw new IllegalArgumentException("记录ID不能为空");
        }
        
        try {
            IotAutomationRecord record = automationRecordMapper.selectRecordById(recordId);
            if (record == null) {
                logger.warn("未找到ID为 {} 的自动化记录", recordId);
            }
            return record;
        } catch (Exception e) {
            logger.error("根据ID查询自动化记录失败，recordId: {}", recordId, e);
            throw new RuntimeException("查询记录详情失败", e);
        }
    }

    /**
     * 批量插入自动化记录
     * 用于高性能场景，减少数据库交互次数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchAddRecord(List<IotAutomationRecord> records) {
        logger.debug("批量插入自动化记录，记录数: {}", records.size());
        
        if (records == null || records.isEmpty()) {
            logger.warn("批量插入的记录列表为空");
            return 0;
        }
        
        try {
            // 设置创建时间
            Date now = new Date();
            for (IotAutomationRecord record : records) {
                record.setCreateTime(now);
                record.setUpdateTime(now);
            }
            
            int result = automationRecordMapper.batchInsertRecord(records);
            logger.debug("批量插入自动化记录成功，插入 {} 条记录", result);
            return result;
        } catch (Exception e) {
            logger.error("批量插入自动化记录失败", e);
            // 批量插入失败不影响主要业务
            return 0;
        }
    }

    /**
     * 根据协议ID查询最新记录
     * 用于获取设备最近一次的操作状态
     */
    @Override
    public IotAutomationRecord getLatestRecordByProtocolId(Long protocolId) {
        logger.debug("查询设备最新记录，protocolId: {}", protocolId);
        
        if (protocolId == null) {
            throw new IllegalArgumentException("协议ID不能为空");
        }
        
        try {
            IotAutomationRecord record = automationRecordMapper.selectLatestRecordByProtocolId(protocolId);
            if (record == null) {
                logger.debug("未找到协议ID为 {} 的最新记录", protocolId);
            }
            return record;
        } catch (Exception e) {
            logger.error("查询最新记录失败，protocolId: {}", protocolId, e);
            throw new RuntimeException("查询最新记录失败", e);
        }
    }
}