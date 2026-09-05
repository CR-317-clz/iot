package com.management.iot.service;

import com.management.iot.domain.IotAutomationConfig;

import java.util.List;

/**
 * 自动化配置业务逻辑层接口
 * 定义自动化配置相关的业务操作方法
 * @author iot
 */
public interface IotAutomationConfigService {
    
    /**
     * 查询所有启用的自动化配置
     * 自动化调度器调用此方法获取需要执行的配置
     * @return 启用的自动化配置列表
     */
    List<IotAutomationConfig> getEnabledConfigs();
    
    /**
     * 根据协议ID查询自动化配置
     * 用于检查设备是否已配置自动化规则
     * @param protocolId 设备协议ID
     * @return 自动化配置信息
     */
    IotAutomationConfig getConfigByProtocolId(Long protocolId);
    
    /**
     * 新增或更新自动化配置
     * 第一次传参时新增，后续传参时更新
     * 这是核心方法，处理配置的保存逻辑
     * @param config 自动化配置对象
     * @return 操作结果，成功返回1，失败返回0
     */
    int saveOrUpdateConfig(IotAutomationConfig config);
    
    /**
     * 新增自动化配置
     * @param config 自动化配置对象
     * @return 插入记录数
     */
    int addConfig(IotAutomationConfig config);
    
    /**
     * 修改自动化配置
     * @param config 自动化配置对象
     * @return 更新记录数
     */
    int updateConfig(IotAutomationConfig config);
    
    /**
     * 删除自动化配置
     * @param automationId 自动化配置ID
     * @return 删除记录数
     */
    int deleteConfig(Long automationId);
    
    /**
     * 根据协议ID删除自动化配置
     * @param protocolId 设备协议ID
     * @return 删除记录数
     */
    int deleteConfigByProtocolId(Long protocolId);
    
    /**
     * 初始化默认自动化配置
     * 为设备创建默认的自动化配置参数
     * @param protocolId 设备协议ID
     * @return 操作结果
     */
    int initDefaultConfig(Long protocolId);

    /**
     * 根据设备ID查询自动化配置
     * @param deviceId 设备ID
     * @return 自动化配置信息
     */
    IotAutomationConfig getConfigByDeviceId(Long deviceId);
}