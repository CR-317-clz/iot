package com.management.web.controller.business.sim;

import com.management.common.core.domain.AjaxResult;
import com.management.sim.domain.SimWiringRule;
import com.management.sim.domain.dto.WiringCheckDTO;
import com.management.sim.domain.dto.WiringCheckResult;
import com.management.sim.service.WiringRuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 接线规则控制器
 * 提供接线规则验证的REST API接口
 */
@Validated
@RestController
@RequestMapping("/wiring/rules")
@RequiredArgsConstructor
public class WiringRuleController {
    
    private final WiringRuleService wiringRuleService;
    
    /**
     * 检查单个接线
     * POST /api/wiring/rules/check
     */
    @PostMapping("/check")
    public AjaxResult checkWiring(@Valid @RequestBody WiringCheckDTO checkDTO) {
        WiringCheckResult result = wiringRuleService.checkWiring(checkDTO);
        return AjaxResult.success(result);
    }
    
    /**
     * 批量检查接线
     * POST /api/wiring/rules/batch-check
     */
    @PostMapping("/batch-check")
    public AjaxResult batchCheckWiring(@Valid @RequestBody List<WiringCheckDTO> checkDTOs) {
        List<WiringCheckResult> results = wiringRuleService.batchCheckWiring(checkDTOs);
        return AjaxResult.success(results);
    }
    
    /**
     * 获取设备的接线规则
     * GET /api/wiring/rules/device/{deviceId}
     */
    @GetMapping("/device/{deviceId}")
    public AjaxResult getDeviceRules(@PathVariable Long deviceId) {
        List<SimWiringRule> rules = wiringRuleService.getDeviceRules(deviceId);
        return AjaxResult.success(rules);
    }
    
    /**
     * 获取源设备的可能连接规则
     * GET /api/wiring/rules/source/{sourceDeviceId}
     */
    @GetMapping("/source/{sourceDeviceId}")
    public AjaxResult getRulesBySource(
            @PathVariable Long sourceDeviceId,
            @RequestParam(required = false) String sourcePin) {
        List<SimWiringRule> rules = wiringRuleService.getRulesBySource(sourceDeviceId, sourcePin);
        return AjaxResult.success(rules);
    }
    
    /**
     * 新增接线规则
     * POST /api/wiring/rules
     */
    @PostMapping
    public AjaxResult addRule(@Valid @RequestBody SimWiringRule rule) {
        boolean saved = wiringRuleService.save(rule);
        return saved ? AjaxResult.success( "规则添加成功")
                    : AjaxResult.error("规则添加失败");
    }
    
    /**
     * 更新接线规则
     * PUT /api/wiring/rules/{ruleId}
     */
    @PutMapping("/{ruleId}")
    public AjaxResult updateRule(@PathVariable Long ruleId,
                                        @Valid @RequestBody SimWiringRule rule) {
        rule.setRuleId(ruleId);
        boolean updated = wiringRuleService.updateById(rule);
        return updated ? AjaxResult.success( "规则更新成功")
                      : AjaxResult.error("规则更新失败");
    }
    
    /**
     * 删除接线规则（逻辑删除）
     * DELETE /api/wiring/rules/{ruleId}
     */
    @DeleteMapping("/{ruleId}")
    public AjaxResult deleteRule(@PathVariable Long ruleId) {
        SimWiringRule rule = new SimWiringRule();
        rule.setRuleId(ruleId);
        rule.setDelFlag("1"); // 逻辑删除
        
        boolean deleted = wiringRuleService.updateById(rule);
        return deleted ? AjaxResult.success( "规则删除成功")
                      : AjaxResult.error("规则删除失败");
    }
    
    /**
     * 启用/停用规则
     * PUT /api/wiring/rules/{ruleId}/status/{status}
     */
    @PutMapping("/{ruleId}/status/{status}")
    public AjaxResult updateRuleStatus(@PathVariable Long ruleId,
                                              @PathVariable String status) {
        SimWiringRule rule = new SimWiringRule();
        rule.setRuleId(ruleId);
        rule.setStatus(status);
        
        boolean updated = wiringRuleService.updateById(rule);
        return updated ? AjaxResult.success( "规则状态更新成功")
                      : AjaxResult.error("规则状态更新失败");
    }
}