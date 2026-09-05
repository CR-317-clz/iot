package com.management.web.controller.business.iot;

import com.management.common.core.domain.AjaxResult;
import com.management.common.core.page.TableDataInfo;
import com.management.iot.domain.IotProtocol;
import com.management.iot.service.IotProtocolService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通信协议配置控制器
 * 提供协议管理的RESTful API接口
 */
@RestController
@RequestMapping("/protocol")
public class IotProtocolController {

    private static final Logger logger = LoggerFactory.getLogger(IotProtocolController.class);
    
    @Autowired
    private IotProtocolService protocolService;

    /**
     * 获取所有协议配置
     * @return 协议列表和操作结果
     */
    @GetMapping("/list")
    public TableDataInfo getAllProtocols(String protocolType) {
        TableDataInfo info = new TableDataInfo();
        try {
            List<IotProtocol> protocols = protocolService.getAllProtocols(protocolType);
            info.setCode(200);
            info.setRows(protocols);
            info.setTotal(protocols.size());
            info.setMsg("获取协议列表成功");
            logger.info("获取协议列表成功，共 {} 个协议", protocols.size());
            return info;
        } catch (Exception e) {
            info.setCode(500);
            info.setMsg("获取协议列表失败");
            logger.error("获取协议列表失败", e);
            return info;
        }
    }

    /**
     * 根据协议ID获取协议配置
     * @param protocolId 协议ID
     * @return 协议信息和操作结果
     */
    @GetMapping("/info/{protocolId}")
    public AjaxResult getProtocolById(@PathVariable Long protocolId) {
        AjaxResult result = new AjaxResult();
        try {
            IotProtocol protocol = protocolService.getProtocolById(protocolId);
            result.put("code", 200);
            result.put("data", protocol);
            result.put("message", "获取协议信息成功");
            logger.info("获取协议信息成功，协议ID: {}", protocolId);
            return result;
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取协议信息失败: " + e.getMessage());
            logger.error("获取协议信息失败，协议ID: {}", protocolId, e);
            return result;
        }
    }

    /**
     * 根据协议名称获取协议配置
     * @param protocolName 协议名称
     * @return 协议信息和操作结果
     */
    @GetMapping("/name")
    public ResponseEntity<Map<String, Object>> getProtocolByName(String protocolName) {
        Map<String, Object> result = new HashMap<>();
        try {
            IotProtocol protocol = protocolService.getProtocolByName(protocolName);
            result.put("success", true);
            result.put("data", protocol);
            result.put("message", "获取协议信息成功");
            logger.info("获取协议信息成功，协议名称: {}", protocolName);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取协议信息失败: " + e.getMessage());
            logger.error("获取协议信息失败，协议名称: {}", protocolName, e);
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 添加新协议配置
     * @param protocol 协议配置信息
     * @return 操作结果
     */
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addProtocol(@RequestBody IotProtocol protocol) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 验证协议配置
            if (!protocolService.validateProtocolConfig(protocol)) {
                result.put("success", false);
                result.put("message", "协议配置无效");
                logger.warn("协议配置无效，协议名称: {}", protocol.getProtocolName());
                return ResponseEntity.badRequest().body(result);
            }
            
            boolean success = protocolService.addProtocol(protocol);
            if (success) {
                result.put("success", true);
                result.put("message", "添加协议配置成功");
                result.put("protocolId", protocol.getProtocolId());
                logger.info("添加协议配置成功，协议ID: {}, 协议名称: {}", protocol.getProtocolId(), protocol.getProtocolName());
                return ResponseEntity.ok(result);
            } else {
                result.put("success", false);
                result.put("message", "添加协议配置失败");
                logger.error("添加协议配置失败，协议名称: {}", protocol.getProtocolName());
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "添加协议配置失败: " + e.getMessage());
            logger.error("添加协议配置失败", e);
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 更新协议配置
     * @param protocol 协议配置信息
     * @return 操作结果
     */
    @PutMapping("/update")
    public ResponseEntity<Map<String, Object>> updateProtocol(@RequestBody IotProtocol protocol) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 验证协议配置
//            if (!protocolService.validateProtocolConfig(protocol)) {
//                result.put("success", false);
//                result.put("message", "协议配置无效");
//                logger.warn("协议配置无效，协议ID: {}", protocol.getProtocolId());
//                return ResponseEntity.badRequest().body(result);
//            }
            
            boolean success = protocolService.updateProtocol(protocol);
            if (success) {
                result.put("success", true);
                result.put("message", "更新协议配置成功");
                logger.info("更新协议配置成功，协议ID: {}", protocol.getProtocolId());
                return ResponseEntity.ok(result);
            } else {
                result.put("success", false);
                result.put("message", "更新协议配置失败");
                logger.error("更新协议配置失败，协议ID: {}", protocol.getProtocolId());
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "更新协议配置失败: " + e.getMessage());
            logger.error("更新协议配置失败，协议ID: {}", protocol.getProtocolId(), e);
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 删除协议配置
     * @param protocolId 协议ID
     * @return 操作结果
     */
    @DeleteMapping("/delete/{protocolId}")
    public ResponseEntity<Map<String, Object>> deleteProtocol(@PathVariable Long protocolId) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = protocolService.deleteProtocol(protocolId);
            if (success) {
                result.put("success", true);
                result.put("message", "删除协议配置成功");
                logger.info("删除协议配置成功，协议ID: {}", protocolId);
                return ResponseEntity.ok(result);
            } else {
                result.put("success", false);
                result.put("message", "删除协议配置失败");
                logger.error("删除协议配置失败，协议ID: {}", protocolId);
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "删除协议配置失败: " + e.getMessage());
            logger.error("删除协议配置失败，协议ID: {}", protocolId, e);
            return ResponseEntity.badRequest().body(result);
        }
    }
}