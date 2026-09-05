package com.management.sim.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.management.sim.domain.SimWiringRule;
import com.management.sim.domain.dto.WiringCheckDTO;
import com.management.sim.domain.dto.WiringCheckResult;
import com.management.sim.mapper.SimWiringRuleMapper;
import com.management.sim.service.WiringRuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 接线规则服务实现类 - 支持双向匹配
 * 规则匹配逻辑：只要设备和引脚在规则中存在（无论是source还是target），都允许连接
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WiringRuleServiceImpl extends ServiceImpl<SimWiringRuleMapper, SimWiringRule>
        implements WiringRuleService {
    
    // 规则状态常量
    private static final String STATUS_ENABLED = "1";
    private static final String STATUS_DISABLED = "0";
    private static final String RULE_TYPE_CORRECT = "1";  // 正确规则
    private static final String RULE_TYPE_ERROR = "2";    // 错误规则
    private static final String DEL_FLAG_NORMAL = "0";    // 未删除
    
    @Override
    public WiringCheckResult checkWiring(WiringCheckDTO checkDTO) {
        log.info("开始检查接线: 源设备[{}]-引脚[{}] -> 目标设备[{}]-引脚[{}]",
                checkDTO.getSourceDeviceId(), checkDTO.getSourcePin(),
                checkDTO.getTargetDeviceId(), checkDTO.getTargetPin());
        
        // 1. 创建结果对象
        WiringCheckResult result = new WiringCheckResult()
                .setSourceDeviceId(checkDTO.getSourceDeviceId())
                .setSourcePin(checkDTO.getSourcePin())
                .setTargetDeviceId(checkDTO.getTargetDeviceId())
                .setTargetPin(checkDTO.getTargetPin());
        
        // 2. 查找匹配的规则 - 支持双向匹配
        List<SimWiringRule> matchingRules = findMatchingRules(checkDTO);
        
        // 3. 如果没有匹配规则，返回默认错误
        if (CollectionUtils.isEmpty(matchingRules)) {
            log.warn("未找到匹配的接线规则");
            return result.setValid(false)
                    .setMessage("未找到匹配的接线规则，请检查设备引脚配置")
                    .setRuleType(RULE_TYPE_ERROR)
                    .setSuggestion("请参考设备手册或联系管理员");
        }
        
        // 4. 查找允许连接的规则（is_allowed = 1）
        List<SimWiringRule> allowedRules = matchingRules.stream()
                .filter(rule -> Boolean.TRUE.equals(rule.getIsAllowed()))
                .collect(Collectors.toList());
        
        // 5. 查找禁止连接的规则（is_allowed = 0）
        List<SimWiringRule> forbiddenRules = matchingRules.stream()
                .filter(rule -> Boolean.FALSE.equals(rule.getIsAllowed()))
                .collect(Collectors.toList());
        
        // 6. 处理优先级：允许规则优先
        if (!allowedRules.isEmpty()) {
            SimWiringRule rule = allowedRules.get(0);
            log.info("找到允许规则: ruleId={}, description={}", rule.getRuleId(), rule.getDescription());
            
            // 检查电压匹配（如果需要）
            if (isVoltageCheckRequired(rule, checkDTO)) {
                boolean voltageValid = checkVoltageMatch(
                        checkDTO.getSourceVoltage(),
                        checkDTO.getTargetRequiredVoltage(),
                        rule.getMaxVoltageDiff()
                );
                
                if (!voltageValid) {
                    return result.setValid(false)
                            .setMessage("电压不匹配：" + rule.getDescription())
                            .setRuleType(RULE_TYPE_ERROR)
                            .setRuleId(rule.getRuleId())
                            .setSuggestion("请检查设备电压要求，允许最大电压差：" + rule.getMaxVoltageDiff() + "V");
                }
            }
            
            return result.setValid(true)
                    .setMessage(rule.getDescription())
                    .setRuleType(RULE_TYPE_CORRECT)
                    .setRuleId(rule.getRuleId())
                    .setSuggestion("接线正确，可以连接");
        }
        
        // 7. 如果只有禁止规则
        if (!forbiddenRules.isEmpty()) {
            SimWiringRule rule = forbiddenRules.get(0);
            log.warn("找到禁止规则: ruleId={}, description={}", rule.getRuleId(), rule.getDescription());
            
            return result.setValid(false)
                    .setMessage(rule.getDescription())
                    .setRuleType(RULE_TYPE_ERROR)
                    .setRuleId(rule.getRuleId())
                    .setSuggestion("禁止连接，请修改接线方案");
        }
        
        // 8. 默认返回错误
        return result.setValid(false)
                .setMessage("未知的接线规则状态")
                .setRuleType(RULE_TYPE_ERROR)
                .setSuggestion("请联系系统管理员");
    }
    
    /**
     * 查找匹配的规则 - 支持双向匹配
     * 逻辑说明：
     * 1. 首先尝试正向匹配：sourceDeviceId -> targetDeviceId
     * 2. 如果正向没有找到，尝试反向匹配：targetDeviceId -> sourceDeviceId
     * 3. 只要设备和引脚在规则中出现，都认为匹配
     */
    private List<SimWiringRule> findMatchingRules(WiringCheckDTO checkDTO) {
        List<SimWiringRule> allRules = this.listEnabledRules();
        
        if (CollectionUtils.isEmpty(allRules)) {
            return Collections.emptyList();
        }
        
        // 尝试正向匹配
        List<SimWiringRule> forwardRules = allRules.stream()
                .filter(rule -> isForwardMatch(rule, checkDTO))
                .collect(Collectors.toList());
        
        if (!forwardRules.isEmpty()) {
            return forwardRules;
        }
        
        // 正向没有找到，尝试反向匹配
        List<SimWiringRule> reverseRules = allRules.stream()
                .filter(rule -> isReverseMatch(rule, checkDTO))
                .collect(Collectors.toList());
        
        return reverseRules;
    }
    
    /**
     * 正向匹配：检查规则是否与接线方向一致
     * rule: source -> target
     * check: source -> target
     */
    private boolean isForwardMatch(SimWiringRule rule, WiringCheckDTO checkDTO) {
        // 1. 源设备ID匹配
        if (!rule.getVirtualDeviceId().equals(checkDTO.getSourceDeviceId())) {
            return false;
        }
        
        // 2. 源引脚类型匹配
        if (!rule.getSourcePinType().equals(checkDTO.getSourcePin())) {
            return false;
        }
        
        // 3. 目标设备ID匹配（检查目标设备是否在规则的目标设备列表中）
        if (!isTargetDeviceInList(rule.getTargetDeviceId(), checkDTO.getTargetDeviceId())) {
            return false;
        }
        
        // 4. 目标引脚类型匹配
        if (!rule.getTargetPinType().equals(checkDTO.getTargetPin())) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 反向匹配：检查规则是否反向匹配
     * rule: target -> source
     * check: source -> target
     * 注意：需要检查引脚类型的对应关系
     */
    private boolean isReverseMatch(SimWiringRule rule, WiringCheckDTO checkDTO) {
        // 1. 规则中的目标设备作为检查的源设备
        if (!isTargetDeviceInList(rule.getTargetDeviceId(), checkDTO.getSourceDeviceId())) {
            return false;
        }
        
        // 2. 规则中的目标引脚作为检查的源引脚
        // 注意：这里可能需要引脚类型映射，暂时直接比较
        if (!rule.getTargetPinType().equals(checkDTO.getSourcePin())) {
            return false;
        }
        
        // 3. 规则中的源设备作为检查的目标设备
        if (!rule.getVirtualDeviceId().equals(checkDTO.getTargetDeviceId())) {
            return false;
        }
        
        // 4. 规则中的源引脚作为检查的目标引脚
        if (!rule.getSourcePinType().equals(checkDTO.getTargetPin())) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 检查目标设备是否在规则的目标设备列表中
     */
    private boolean isTargetDeviceInList(String ruleTargetDevices, Long targetDeviceId) {
        if (!StringUtils.hasText(ruleTargetDevices) || targetDeviceId == null) {
            return false;
        }
        
        // 将逗号分隔的设备ID字符串转换为Set以提高性能
        String[] deviceIdArray = ruleTargetDevices.split(",");
        for (String deviceIdStr : deviceIdArray) {
            try {
                if (targetDeviceId.equals(Long.parseLong(deviceIdStr.trim()))) {
                    return true;
                }
            } catch (NumberFormatException e) {
                log.warn("规则中的设备ID格式错误: {}", deviceIdStr);
            }
        }
        
        return false;
    }
    
    /**
     * 获取所有启用的规则
     */
    private List<SimWiringRule> listEnabledRules() {
        LambdaQueryWrapper<SimWiringRule> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SimWiringRule::getStatus, STATUS_ENABLED)
                   .eq(SimWiringRule::getDelFlag, DEL_FLAG_NORMAL);
        
        return this.list(queryWrapper);
    }
    
    /**
     * 检查是否需要电压匹配
     */
    private boolean isVoltageCheckRequired(SimWiringRule rule, WiringCheckDTO checkDTO) {
        // 1. 规则要求电压匹配
        if (Boolean.TRUE.equals(rule.getVoltageMatchRequired())) {
            // 2. 检查DTO中是否有电压信息
            boolean hasSourceVoltage = checkDTO.getSourceVoltage() != null;
            boolean hasTargetVoltage = checkDTO.getTargetRequiredVoltage() != null;
            
            // 3. 如果缺少电压信息，记录警告但仍允许连接
            if (!hasSourceVoltage || !hasTargetVoltage) {
                log.warn("规则要求电压匹配，但缺少电压信息。源电压: {}, 目标电压: {}", 
                        hasSourceVoltage ? "已提供" : "未提供",
                        hasTargetVoltage ? "已提供" : "未提供");
                // 可以根据需要决定是否返回false
            }
            
            return hasSourceVoltage && hasTargetVoltage;
        }
        
        return false;
    }
    
    /**
     * 检查电压是否匹配
     */
    private boolean checkVoltageMatch(BigDecimal sourceVoltage, BigDecimal targetVoltage, BigDecimal maxDiff) {
        if (sourceVoltage == null || targetVoltage == null || maxDiff == null) {
            log.warn("电压匹配检查缺少参数，跳过检查");
            return true; // 跳过检查
        }
        
        BigDecimal diff = sourceVoltage.subtract(targetVoltage).abs();
        boolean isValid = diff.compareTo(maxDiff) <= 0;
        
        log.info("电压匹配检查: 源电压={}V, 目标电压={}V, 差值={}V, 允许最大差值={}V, 结果={}",
                sourceVoltage, targetVoltage, diff, maxDiff, isValid ? "通过" : "失败");
        
        return isValid;
    }
    
    /**
     * 批量检查接线规则 - 改进版
     */
    @Override
    public List<WiringCheckResult> batchCheckWiring(List<WiringCheckDTO> checkDTOs) {
        log.info("批量检查接线规则，共{}条记录", checkDTOs.size());
        
        // 预加载所有规则到内存，提高批量检查性能
        List<SimWiringRule> allRules = this.listEnabledRules();
        Map<Long, List<SimWiringRule>> sourceDeviceRules = new HashMap<>();
        
        // 按源设备ID分组规则
        for (SimWiringRule rule : allRules) {
            sourceDeviceRules
                    .computeIfAbsent(rule.getVirtualDeviceId(), k -> new ArrayList<>())
                    .add(rule);
        }
        
        List<WiringCheckResult> results = new ArrayList<>();
        
        for (WiringCheckDTO checkDTO : checkDTOs) {
            try {
                // 批量检查时使用缓存的规则
                WiringCheckResult result = checkWiringWithCachedRules(checkDTO, allRules);
                results.add(result);
            } catch (Exception e) {
                log.error("检查接线时发生异常: {}", checkDTO, e);
                
                WiringCheckResult errorResult = createErrorResult(checkDTO, e);
                results.add(errorResult);
            }
        }
        
        return results;
    }
    
    /**
     * 使用缓存的规则进行检查
     */
    private WiringCheckResult checkWiringWithCachedRules(WiringCheckDTO checkDTO, 
                                                        List<SimWiringRule> cachedRules) {
        WiringCheckResult result = new WiringCheckResult()
                .setSourceDeviceId(checkDTO.getSourceDeviceId())
                .setSourcePin(checkDTO.getSourcePin())
                .setTargetDeviceId(checkDTO.getTargetDeviceId())
                .setTargetPin(checkDTO.getTargetPin());
        
        // 从缓存规则中查找匹配的规则
        List<SimWiringRule> matchingRules = cachedRules.stream()
                .filter(rule -> isForwardMatch(rule, checkDTO) || isReverseMatch(rule, checkDTO))
                .collect(Collectors.toList());
        
        // 后续逻辑与单条检查相同...
        if (CollectionUtils.isEmpty(matchingRules)) {
            return result.setValid(false)
                    .setMessage("未找到匹配的接线规则")
                    .setRuleType(RULE_TYPE_ERROR)
                    .setSuggestion("请参考设备手册");
        }
        
        // 分离允许和禁止规则
        List<SimWiringRule> allowedRules = matchingRules.stream()
                .filter(rule -> Boolean.TRUE.equals(rule.getIsAllowed()))
                .collect(Collectors.toList());
        
        if (!allowedRules.isEmpty()) {
            SimWiringRule rule = allowedRules.get(0);
            
            // 检查电压匹配
            if (isVoltageCheckRequired(rule, checkDTO)) {
                boolean voltageValid = checkVoltageMatch(
                        checkDTO.getSourceVoltage(),
                        checkDTO.getTargetRequiredVoltage(),
                        rule.getMaxVoltageDiff()
                );
                
                if (!voltageValid) {
                    return result.setValid(false)
                            .setMessage("电压不匹配：" + rule.getDescription())
                            .setRuleType(RULE_TYPE_ERROR)
                            .setRuleId(rule.getRuleId())
                            .setSuggestion("电压超出允许范围");
                }
            }
            
            return result.setValid(true)
                    .setMessage(rule.getDescription())
                    .setRuleType(RULE_TYPE_CORRECT)
                    .setRuleId(rule.getRuleId())
                    .setSuggestion("接线正确");
        }
        
        // 禁止规则处理
        List<SimWiringRule> forbiddenRules = matchingRules.stream()
                .filter(rule -> Boolean.FALSE.equals(rule.getIsAllowed()))
                .collect(Collectors.toList());
        
        if (!forbiddenRules.isEmpty()) {
            SimWiringRule rule = forbiddenRules.get(0);
            return result.setValid(false)
                    .setMessage(rule.getDescription())
                    .setRuleType(RULE_TYPE_ERROR)
                    .setRuleId(rule.getRuleId())
                    .setSuggestion("禁止连接");
        }
        
        return result.setValid(false)
                .setMessage("未知规则状态")
                .setRuleType(RULE_TYPE_ERROR)
                .setSuggestion("请联系管理员");
    }
    
    /**
     * 创建错误结果
     */
    private WiringCheckResult createErrorResult(WiringCheckDTO checkDTO, Exception e) {
        return new WiringCheckResult()
                .setSourceDeviceId(checkDTO.getSourceDeviceId())
                .setSourcePin(checkDTO.getSourcePin())
                .setTargetDeviceId(checkDTO.getTargetDeviceId())
                .setTargetPin(checkDTO.getTargetPin())
                .setValid(false)
                .setMessage("检查过程中发生错误: " + e.getMessage())
                .setRuleType(RULE_TYPE_ERROR)
                .setSuggestion("请联系系统管理员");
    }
    
    // 其他方法保持不变...
    @Override
    public List<SimWiringRule> getRulesBySource(Long sourceDeviceId, String sourcePin) {
        LambdaQueryWrapper<SimWiringRule> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SimWiringRule::getVirtualDeviceId, sourceDeviceId)
                   .eq(StringUtils.hasText(sourcePin), SimWiringRule::getSourcePinType, sourcePin)
                   .eq(SimWiringRule::getStatus, STATUS_ENABLED)
                   .eq(SimWiringRule::getDelFlag, DEL_FLAG_NORMAL)
                   .orderByDesc(SimWiringRule::getIsAllowed);
                   
        return this.list(queryWrapper);
    }
    
    @Override
    public List<SimWiringRule> getDeviceRules(Long deviceId) {
        LambdaQueryWrapper<SimWiringRule> queryWrapper = new LambdaQueryWrapper<>();
        
        // 查询设备作为源设备
        queryWrapper.or(wrapper -> wrapper
                .eq(SimWiringRule::getVirtualDeviceId, deviceId));
        
        // 或者设备在目标设备列表中
        queryWrapper.or(wrapper -> wrapper
                .like(SimWiringRule::getTargetDeviceId, deviceId.toString()));
        
        queryWrapper.eq(SimWiringRule::getStatus, STATUS_ENABLED)
                   .eq(SimWiringRule::getDelFlag, DEL_FLAG_NORMAL);
        
        return this.list(queryWrapper);
    }
    
    @Override
    public List<SimWiringRule> getMatchingRules(Long sourceDeviceId, String sourcePin,
                                               Long targetDeviceId, String targetPin) {
        WiringCheckDTO checkDTO = new WiringCheckDTO()
                .setSourceDeviceId(sourceDeviceId)
                .setSourcePin(sourcePin)
                .setTargetDeviceId(targetDeviceId)
                .setTargetPin(targetPin);
        
        return findMatchingRules(checkDTO);
    }
}