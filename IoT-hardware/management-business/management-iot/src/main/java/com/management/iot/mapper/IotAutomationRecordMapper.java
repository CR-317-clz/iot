package com.management.iot.mapper;

import com.management.iot.domain.IotAutomationRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 自动化记录数据访问层接口
 * 提供自动化操作记录的CRUD操作
 * @author iot
 */
@Mapper
public interface IotAutomationRecordMapper {
    
    /**
     * 新增自动化记录
     * 每次执行控制操作后都会记录到数据库
     * @param record 自动化记录对象
     * @return 插入记录数，成功返回1，失败返回0
     */
    int insertRecord(IotAutomationRecord record);
    
    /**
     * 查询自动化记录列表
     * 支持按条件查询控制记录，用于历史记录查看
     * @param record 查询条件对象
     * @return 符合条件的记录列表
     */
    List<IotAutomationRecord> selectRecordList(IotAutomationRecord record);
    
    /**
     * 根据ID查询自动化记录
     * 用于查看单条记录的详细信息
     * @param recordId 记录ID
     * @return 自动化记录对象
     */
    IotAutomationRecord selectRecordById(@Param("recordId") Long recordId);
    
    /**
     * 根据协议ID查询最新记录
     * 用于获取设备最近一次的控制记录
     * @param protocolId 协议ID
     * @return 最新的自动化记录
     */
    IotAutomationRecord selectLatestRecordByProtocolId(@Param("protocolId") Long protocolId);
    
    /**
     * 批量插入自动化记录
     * 用于批量记录操作，提高性能
     * @param records 记录列表
     * @return 插入记录数
     */
    int batchInsertRecord(@Param("records") List<IotAutomationRecord> records);
}