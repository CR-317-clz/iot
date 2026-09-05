package com.management.iot.mapper;

import com.management.iot.domain.IotAutomationConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 自动化配置数据访问层接口
 * 提供自动化配置表的CRUD操作
 * @author iot
 */
@Mapper
public interface IotAutomationConfigMapper {
    
    /**
     * 查询所有启用的自动化配置
     * 自动化调度器会定期查询这些配置来执行控制逻辑
     * @return 启用的自动化配置列表
     */
    List<IotAutomationConfig> selectEnabledConfigs();
    
    /**
     * 根据协议ID查询自动化配置
     * 用于获取特定设备的自动化配置信息
     * @param protocolId 设备协议ID
     * @return 自动化配置信息，如果不存在返回null
     */
    IotAutomationConfig selectConfigByProtocolId(@Param("protocolId") Long protocolId);
    
    /**
     * 新增自动化配置
     * 第一次传参时会调用此方法保存配置
     * @param config 自动化配置对象
     * @return 插入记录数，成功返回1，失败返回0
     */
    int insertConfig(IotAutomationConfig config);
    
    /**
     * 更新自动化配置
     * 用于修改已存在的自动化配置
     * @param config 自动化配置对象
     * @return 更新记录数，成功返回1，失败返回0
     */
    int updateConfig(IotAutomationConfig config);
    
    /**
     * 删除自动化配置
     * 根据配置ID删除自动化配置
     * @param automationId 自动化配置ID
     * @return 删除记录数，成功返回1，失败返回0
     */
    int deleteConfig(@Param("automationId") Long automationId);
    
    /**
     * 根据协议ID删除自动化配置
     * 当设备删除时，同步删除对应的自动化配置
     * @param protocolId 设备协议ID
     * @return 删除记录数
     */
    int deleteConfigByProtocolId(@Param("protocolId") Long protocolId);
}