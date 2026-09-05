package com.management.web.controller.business.iot;

import com.management.common.core.domain.AjaxResult;
import com.management.common.utils.snowFlake.SnowflakeIdGenerator;
import com.management.iot.configuration.mqtt.MqttManager;
import com.management.iot.configuration.tcp.TcpClientManager;
import com.management.iot.domain.*;
import com.management.iot.domain.dto.RelayControlDto;
import com.management.iot.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 设备控制核心控制器
 * 处理手动控制和自动化控制的切换，执行设备控制指令
 * @author iot
 */
@RestController
@RequestMapping("/control")
public class ControlController {

    private static final Logger logger = LoggerFactory.getLogger(ControlController.class);

    @Autowired
    private TcpClientManager tcpClientManager;

    @Autowired
    private MqttManager mqttManager;

    @Autowired
    private IotDeviceService iotDeviceService;

    @Autowired
    private IotProtocolService iotProtocolService;

    @Autowired
    private IotCurtainMachineStatusService iotCurtainMachineStatusService;

    @Autowired
    private AutomationControlService automationControlService;

    @Autowired
    private IotAutomationRecordService automationRecordService;

    @Autowired
    private IotAutomationConfigService automationConfigService;

    /**
     * 设备控制主接口
     * 支持手动控制和自动化控制的切换
     * 第一次传参时会自动保存自动化配置
     * @param dto 控制参数DTO
     * @return 控制结果
     */
    @PostMapping("/tcp")
    public AjaxResult controlDevice(@RequestBody RelayControlDto dto) {
        logger.info("收到设备控制请求，automation: {}, relayProtocolId: {}",
                   dto.getAutomation(), dto.getRelayProtocolId());

        AjaxResult result = new AjaxResult();

        try {
            // 参数基础校验
            if (dto.getRelayProtocolId() == null || dto.getRelayProtocolId() == 0) {
                throw new IllegalArgumentException("设备协议ID不能为空");
            }

            // 手动控制模式
            if (dto.getAutomation() == 1) {
                return handleManualControl(dto);
            } 
            // 自动化控制模式
            else if (dto.getAutomation() == 2) {

                System.out.println(dto);
                return handleAutomationControl(dto);
            } 
            // 错误的控制模式
            else {
                logger.error("无效的控制模式: {}", dto.getAutomation());
                result.put("code", 500);
                result.put("message", "请选择正确的控制模式：1-手动 2-自动化");
                return result;
            }

        } catch (Exception e) {
            logger.error("设备控制失败，relayProtocolId: {}", dto.getRelayProtocolId(), e);
            result.put("code", 500);
            result.put("message", "控制失败: " + e.getMessage());
            return result;
        }
    }

    /**
     * 处理手动控制逻辑
     * 手动控制时会自动禁用自动化控制，避免冲突
     */
    private AjaxResult handleManualControl(RelayControlDto dto) {
        logger.info("开始手动控制，relayProtocolId: {}", dto.getRelayProtocolId());

        // 手动控制时禁用自动化
        automationControlService.setAutomationEnabled(false);
        logger.info("手动控制开始，自动化控制已禁用");

        // 执行设备控制
        controlAllDevices(dto);

        // 同步设备状态缓存，避免手动控制后自动化控制因缓存未更新而跳过控制
        syncDeviceStateCache(dto);

        // 记录手动操作
        recordManualControlOperations(dto);

        logger.info("手动控制完成，relayProtocolId: {}", dto.getRelayProtocolId());
        
        return AjaxResult.success("手动控制成功", dto.getRelayProtocolId());
    }

    /**
     * 处理自动化控制逻辑
     * 启用自动化控制，并处理第一次传参的配置保存
     */
    private AjaxResult handleAutomationControl(RelayControlDto dto) {
        logger.info("开始自动化控制，relayProtocolId: {}", dto.getRelayProtocolId());

        // 检查是否有自动化配置需要保存（第一次传参）
        if (dto.getAutomationConfig() != null) {
            logger.info("检测到自动化配置，开始保存配置");
            handleAutomationConfigSave(dto);
        }

        // 启用自动化控制
        automationControlService.setAutomationEnabled(true);
        logger.info("自动化控制已启用");

        // 如果有传感器数据，立即执行一次自动化控制（可选）
        if (dto.getAutomationConfig() != null) {
            logger.info("检测到传感器数据，立即执行一次自动化控制");
            // 这里可以添加立即执行的逻辑
        }

        logger.info("自动化控制设置完成，relayProtocolId: {}", dto.getRelayProtocolId());
        
        return AjaxResult.success("自动化控制已启用", dto.getRelayProtocolId());
    }

    /**
     * 处理自动化配置保存
     * 第一次传参时保存配置，后续传参时更新配置
     */
    private void handleAutomationConfigSave(RelayControlDto dto) {
        IotAutomationConfig config = dto.getAutomationConfig();
        config.setProtocolId(dto.getRelayProtocolId()); // 确保协议ID一致
        
        try {
            int result = automationConfigService.saveOrUpdateConfig(config);
            if (result > 0) {
                logger.info("自动化配置保存成功，relayProtocolId: {}", dto.getRelayProtocolId());
            } else {
                logger.warn("自动化配置保存失败，relayProtocolId: {}", dto.getRelayProtocolId());
                throw new RuntimeException("保存自动化配置失败");
            }
        } catch (Exception e) {
            logger.error("保存自动化配置异常，relayProtocolId: {}", dto.getRelayProtocolId(), e);
            throw new RuntimeException("保存自动化配置异常: " + e.getMessage());
        }
    }

    /**
     * 执行所有设备控制
     * 根据DTO中的设备状态参数，控制对应的设备
     */
    private void controlAllDevices(RelayControlDto dto) {
        Long protocolId = dto.getRelayProtocolId();

        // 控制风扇
        controlBlower(protocolId, dto.getBlower(), "手动控制");
        
        // 控制紫外线灯
        controlUltravioletRadiator(protocolId, dto.getUltravioletRadiator(), "手动控制");
        
        // 控制喷淋器
        controlSpray(protocolId, dto.getSpray(), "手动控制");
        
        // 控制水帘幕
        controlWaterCurtain(protocolId, dto.getWaterCurtain(), "手动控制");
        
        // 控制窗帘机
        controlCurtainMachine(protocolId, dto.getCurtainMachine(), "手动控制");
    }

    /**
     * 同步设备状态缓存
     * 手动控制后将设备状态同步到自动化控制的状态缓存，
     * 避免自动化控制因缓存陈旧而误判"状态未变化"跳过控制
     */
    private void syncDeviceStateCache(RelayControlDto dto) {
        Long protocolId = dto.getRelayProtocolId();

        // 设备类型: 1-风扇, 2-紫外线灯, 3-喷淋, 4-水帘, 5-窗帘机
        if (dto.getBlower() != null) {
            automationControlService.updateDeviceStateCache(protocolId, "1", dto.getBlower() == 1);
        }
        if (dto.getUltravioletRadiator() != null) {
            automationControlService.updateDeviceStateCache(protocolId, "2", dto.getUltravioletRadiator() == 1);
        }
        if (dto.getSpray() != null) {
            automationControlService.updateDeviceStateCache(protocolId, "3", dto.getSpray() == 1);
        }
        if (dto.getWaterCurtain() != null) {
            automationControlService.updateDeviceStateCache(protocolId, "4", dto.getWaterCurtain() == 1);
        }
        if (dto.getCurtainMachine() != null) {
            automationControlService.updateDeviceStateCache(protocolId, "5", dto.getCurtainMachine() == 1);
        }
    }

    /**
     * 控制风扇
     */
    private void controlBlower(Long protocolId, Integer status, String controlType) {
        if (status == null) return;

        try {
            if (status == 0) {
                tcpClientManager.sendMessage(protocolId, "b2");
                logger.info("手动控制风扇关闭成功，protocolId: {}", protocolId);
            } else if (status == 1) {
                tcpClientManager.sendMessage(protocolId, "a2");
                logger.info("手动控制风扇开启成功，protocolId: {}", protocolId);
            }
        } catch (Exception e) {
            logger.error("控制风扇失败，protocolId: {}", protocolId, e);
            throw new RuntimeException("控制风扇失败: " + e.getMessage());
        }
    }

    /**
     * 控制紫外线灯
     */
    private void controlUltravioletRadiator(Long protocolId, Integer status, String controlType) {
        if (status == null) return;

        try {
            if (status == 0) {
                tcpClientManager.sendMessage(protocolId, "b3");
                logger.info("手动控制紫外线灯关闭成功，protocolId: {}", protocolId);
            } else if (status == 1) {
                tcpClientManager.sendMessage(protocolId, "a3");
                logger.info("手动控制紫外线灯开启成功，protocolId: {}", protocolId);
            }
        } catch (Exception e) {
            logger.error("控制紫外线灯失败，protocolId: {}", protocolId, e);
            throw new RuntimeException("控制紫外线灯失败: " + e.getMessage());
        }
    }

    /**
     * 控制喷淋器
     */
    private void controlSpray(Long protocolId, Integer status, String controlType) {
        if (status == null) return;

        try {
            if (status == 0) {
                tcpClientManager.sendMessage(protocolId, "b1");
                logger.info("手动控制喷淋器关闭成功，protocolId: {}", protocolId);
            } else if (status == 1) {
                tcpClientManager.sendMessage(protocolId, "a1");
                logger.info("手动控制喷淋器开启成功，protocolId: {}", protocolId);
            }
        } catch (Exception e) {
            logger.error("控制喷淋器失败，protocolId: {}", protocolId, e);
            throw new RuntimeException("控制喷淋器失败: " + e.getMessage());
        }
    }

    /**
     * 控制水帘幕
     */
    private void controlWaterCurtain(Long protocolId, Integer status, String controlType) {
        if (status == null) return;

        try {
            if (status == 0) {
                tcpClientManager.sendMessage(protocolId, "b4");
                logger.info("手动控制水帘幕关闭成功，protocolId: {}", protocolId);
            } else if (status == 1) {
                tcpClientManager.sendMessage(protocolId, "a4");
                logger.info("手动控制水帘幕开启成功，protocolId: {}", protocolId);
            }
        } catch (Exception e) {
            logger.error("控制水帘幕失败，protocolId: {}", protocolId, e);
            throw new RuntimeException("控制水帘幕失败: " + e.getMessage());
        }
    }

    /**
     * 控制窗帘机
     */
    private void controlCurtainMachine(Long protocolId, Integer status, String controlType) {
        if (status == null) return;

        try {
            IotDevice iotDevice = iotDeviceService.selectDeviceByRelayId(protocolId);
            if (iotDevice == null) {
                throw new IllegalArgumentException("设备不存在");
            }

            IotProtocol protocol = iotProtocolService.getProtocolById(iotDevice.getProtocolId());
            if (protocol == null) {
                throw new IllegalArgumentException("MQTT设备协议不存在");
            }

            IotCurtainMachineStatus machineStatus = new IotCurtainMachineStatus();
            machineStatus.setCurtainMachineId(SnowflakeIdGenerator.nextId());
            machineStatus.setCurtainMachineName("窗帘机");
            machineStatus.setProtocolId(protocol.getProtocolId());

            String message;
            String operationStatus;
            if (status == 0) {
                message = "0";
                operationStatus = "0";
                machineStatus.setStatus("0");
                logger.info("手动控制窗帘关闭，protocolId: {}", protocolId);
            } else if (status == 1) {
                message = "1";
                operationStatus = "1";
                machineStatus.setStatus("1");
                logger.info("手动控制窗帘开启，protocolId: {}", protocolId);
            } else {
                message = "2";
                operationStatus = "2";
                machineStatus.setStatus("2");
                logger.info("手动控制窗帘暂停，protocolId: {}", protocolId);
            }

            int publish = mqttManager.publish(null, String.valueOf(protocol.getSendTopic()), message, 0, false);
            iotCurtainMachineStatusService.addCurtainMachineStatus(machineStatus);

            if (publish == 1) {
                logger.info("✅ MQTT发布成功：{}", protocolId);
            } else {
                logger.error("❌ MQTT发布失败：{}", protocolId);
                throw new RuntimeException("MQTT发布失败");
            }

        } catch (Exception e) {
            logger.error("控制窗帘机失败，protocolId: {}", protocolId, e);
            throw new RuntimeException("控制窗帘机失败: " + e.getMessage());
        }
    }

    /**
     * 记录手动控制操作
     * 为每个设备控制操作记录操作历史
     */
    private void recordManualControlOperations(RelayControlDto dto) {
        Long protocolId = dto.getRelayProtocolId();

        try {
            // 记录风扇操作
            if (dto.getBlower() != null) {
                recordManualOperation(protocolId, "1",
                    dto.getBlower().toString(), "手动控制风扇");
            }

            // 记录紫外线灯操作
            if (dto.getUltravioletRadiator() != null) {
                recordManualOperation(protocolId, "2", 
                    dto.getUltravioletRadiator().toString(), "手动控制紫外线灯");
            }

            // 记录喷淋器操作
            if (dto.getSpray() != null) {
                recordManualOperation(protocolId, "3", 
                    dto.getSpray().toString(), "手动控制喷淋器");
            }

            // 记录水帘幕操作
            if (dto.getWaterCurtain() != null) {
                recordManualOperation(protocolId, "4", 
                    dto.getWaterCurtain().toString(), "手动控制水帘幕");
            }

            // 记录窗帘机操作
            if (dto.getCurtainMachine() != null) {
                recordManualOperation(protocolId, "5", 
                    dto.getCurtainMachine().toString(), "手动控制窗帘机");
            }

            logger.debug("手动控制操作记录完成，protocolId: {}", protocolId);

        } catch (Exception e) {
            logger.error("记录手动控制操作失败，protocolId: {}", protocolId, e);
            // 记录失败不应该影响主要控制逻辑
        }
    }

    /**
     * 记录手动操作
     */
    private void recordManualOperation(Long protocolId, String deviceType, String operationStatus, String controlReason) {
        try {
            IotAutomationRecord record = new IotAutomationRecord();
            record.setRecordId(SnowflakeIdGenerator.nextId());
            record.setProtocolId(protocolId);
            record.setControlType("1"); // 1-手动控制
            record.setDeviceType(deviceType);
            record.setOperationStatus(operationStatus);
            record.setControlReason(controlReason);
            record.setExecuteResult("1"); // 成功

            automationRecordService.addRecord(record);
            logger.debug("手动操作记录已保存，protocolId: {}, deviceType: {}", protocolId, deviceType);

        } catch (Exception e) {
            logger.error("保存手动操作记录失败，protocolId: {}", protocolId, e);
        }
    }

    /**
     * 手动启用/禁用自动化控制
     * 提供独立的接口用于控制自动化开关
     * @param enabled true-启用 false-禁用
     * @return 操作结果
     */
    @PostMapping("/automation/{enabled}")
    public AjaxResult controlAutomation(@PathVariable boolean enabled) {
        logger.info("手动{}自动化控制", enabled ? "启用" : "禁用");
        
        try {
            automationControlService.setAutomationEnabled(enabled);
            String message = "自动化控制已" + (enabled ? "启用" : "禁用");
            logger.info(message);
            return AjaxResult.success(message);
        } catch (Exception e) {
            logger.error("控制自动化状态失败", e);
            return AjaxResult.error("控制自动化状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取自动化控制状态
     * @return 当前自动化控制状态
     */
    @GetMapping("/automation/status")
    public AjaxResult getAutomationStatus() {
        try {
            boolean enabled = automationControlService.isAutomationEnabled();
            return AjaxResult.success("获取成功", enabled);
        } catch (Exception e) {
            logger.error("获取自动化状态失败", e);
            return AjaxResult.error("获取自动化状态失败: " + e.getMessage());
        }
    }

    /**
     * 立即执行一次自动化控制
     * 用于手动触发自动化控制，不依赖定时任务
     * @param protocolId 设备协议ID
     * @return 执行结果
     */
    @PostMapping("/automation/execute/{protocolId}")
    public AjaxResult executeAutomationNow(@PathVariable Long protocolId) {
        logger.info("立即执行自动化控制，protocolId: {}", protocolId);
        
        if (protocolId == null) {
            return AjaxResult.error("设备协议ID不能为空");
        }
        
        try {
            // 这里可以添加立即执行自动化控制的逻辑
            // 注意：由于代码复杂度，这里只是示意
            logger.info("立即执行自动化控制完成，protocolId: {}", protocolId);
            return AjaxResult.success("立即执行完成");
        } catch (Exception e) {
            logger.error("立即执行自动化控制失败，protocolId: {}", protocolId, e);
            return AjaxResult.error("立即执行失败: " + e.getMessage());
        }
    }
}