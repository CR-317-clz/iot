package com.management.sim.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.management.sim.domain.SimWiringRule;
import com.management.sim.domain.dto.WiringCheckDTO;
import com.management.sim.domain.dto.WiringCheckResult;

import java.util.List;

/**
 * 接线规则服务接口
 * 提供接线规则验证相关业务方法
 */
public interface WiringRuleService extends IService<SimWiringRule> {
    
    /**
     * 检查单个接线是否合法
     * @param checkDTO 接线检查请求参数
     * @return 检查结果
     */
    WiringCheckResult checkWiring(WiringCheckDTO checkDTO);
    
    /**
     * 批量检查接线规则
     * @param checkDTOs 多个接线检查请求
     * @return 批量检查结果
     */
    List<WiringCheckResult> batchCheckWiring(List<WiringCheckDTO> checkDTOs);
    
    /**
     * 根据源设备和引脚获取所有可能的连接规则
     * @param sourceDeviceId 源设备ID
     * @param sourcePin 源引脚类型
     * @return 匹配的规则列表
     */
    List<SimWiringRule> getRulesBySource(Long sourceDeviceId, String sourcePin);
    
    /**
     * 根据设备和引脚获取精确匹配的规则
     * @param sourceDeviceId 源设备ID
     * @param sourcePin 源引脚
     * @param targetDeviceId 目标设备ID
     * @param targetPin 目标引脚
     * @return 匹配的规则列表
     */
    List<SimWiringRule> getMatchingRules(Long sourceDeviceId, String sourcePin,
                                         Long targetDeviceId, String targetPin);
    
    /**
     * 获取设备的所有有效连接规则
     * @param deviceId 设备ID
     * @return 设备相关的规则
     */
    List<SimWiringRule> getDeviceRules(Long deviceId);
}