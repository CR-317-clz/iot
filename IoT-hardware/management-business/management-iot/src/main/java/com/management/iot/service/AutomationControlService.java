package com.management.iot.service;

import com.management.common.utils.snowFlake.SnowflakeIdGenerator;
import com.management.iot.configuration.mqtt.MqttConnection;
import com.management.iot.configuration.mqtt.MqttManager;
import com.management.iot.configuration.tcp.TcpClientManager;
import com.management.iot.domain.*;
import com.management.iot.domain.dto.TelemetryDto;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 自动化控制核心服务
 * 负责定时执行自动化规则，管理自动化控制状态
 * @author iot
 */
@Service
public class AutomationControlService {
    
    private static final Logger logger = LoggerFactory.getLogger(AutomationControlService.class);
    
    // 设备类型常量
    private static final String DEVICE_TYPE_BLOWER = "1";           // 风扇
    private static final String DEVICE_TYPE_ULTRAVIOLET = "2";      // 紫外线灯
    private static final String DEVICE_TYPE_SPRAY = "3";            // 喷淋
    private static final String DEVICE_TYPE_WATER_CURTAIN = "4";    // 水帘
    private static final String DEVICE_TYPE_CURTAIN = "5";          // 窗帘机
    
    // 控制类型常量
    private static final String CONTROL_TYPE_AUTO = "2";            // 自动化控制
    
    // 执行结果常量
    private static final String EXECUTE_SUCCESS = "1";              // 执行成功
    private static final String EXECUTE_FAILED = "0";               // 执行失败
    
    // 操作状态常量
    private static final String OPERATION_ON = "1";                 // 开启
    private static final String OPERATION_OFF = "0";                // 关闭
    private static final String OPERATION_PAUSE = "2";              // 暂停
    
    @Autowired
    private IotAutomationConfigService automationConfigService;
    
    @Autowired
    private IotAutomationRecordService automationRecordService;
    
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
    
    // 自动化控制状态
    private volatile boolean automationEnabled = true;
    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> automationTask;
    
    // 状态缓存，避免频繁开关
    private final Map<Long, Boolean> lastCurtainState = new ConcurrentHashMap<>();
    private final Map<Long, Boolean> lastSprayState = new ConcurrentHashMap<>();
    private final Map<Long, Boolean> lastWaterCurtainState = new ConcurrentHashMap<>();
    private final Map<Long, Boolean> lastBlowerState = new ConcurrentHashMap<>();
    private final Map<Long, Boolean> lastUltravioletState = new ConcurrentHashMap<>();
    
    // 执行统计
    private final AtomicInteger totalExecutions = new AtomicInteger(0);
    private final AtomicInteger successExecutions = new AtomicInteger(0);
    private final AtomicInteger errorExecutions = new AtomicInteger(0);
    
    /**
     * 初始化方法 - 系统启动后自动调用
     * 启动自动化调度器，开始定时执行自动化控制
     */
    @PostConstruct
    public void init() {
        logger.info("自动化控制服务初始化开始...");
        try {
            startAutomationScheduler();
            logger.info("自动化控制服务初始化完成，调度器已启动");
        } catch (Exception e) {
            logger.error("自动化控制服务初始化失败", e);
        }
    }
    
    /**
     * 销毁方法 - 系统关闭时自动调用
     * 优雅关闭调度器，释放资源
     */
    @PreDestroy
    public void destroy() {
        logger.info("自动化控制服务开始关闭...");
        try {
            stopAutomationScheduler();
            clearStateCache();
            logger.info("自动化控制服务已关闭");
        } catch (Exception e) {
            logger.error("自动化控制服务关闭失败", e);
        }
    }
    
    /**
     * 启动自动化调度器
     * 创建定时任务，每30秒执行一次自动化控制逻辑
     */
    public synchronized void startAutomationScheduler() {
        logger.info("启动自动化调度器...");
        
        if (scheduler != null && !scheduler.isShutdown()) {
            logger.info("自动化调度器已在运行");
            return;
        }
        
        // 创建单线程调度器
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "Automation-Control-Thread");
            thread.setDaemon(true);
            thread.setPriority(Thread.NORM_PRIORITY);
            return thread;
        });
        
        // 创建定时任务
        automationTask = scheduler.scheduleAtFixedRate(() -> {
            try {
                totalExecutions.incrementAndGet();
                if (automationEnabled) {
                    executeAutomationControl();
                } else {
                    logger.debug("自动化控制已禁用，跳过本次执行");
                }
            } catch (Exception e) {
                errorExecutions.incrementAndGet();
                logger.error("自动化控制执行异常", e);
            }
        }, 5, 20, TimeUnit.SECONDS);
        
        logger.info("自动化调度器启动成功，执行间隔：20秒");
    }
    
    /**
     * 停止自动化调度器
     * 优雅停止定时任务，释放线程资源
     */
    public synchronized void stopAutomationScheduler() {
        logger.info("停止自动化调度器...");
        
        if (automationTask != null && !automationTask.isCancelled()) {
            automationTask.cancel(true);
            automationTask = null;
            logger.info("自动化定时任务已取消");
        }
        
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                    logger.warn("自动化调度器强制关闭");
                } else {
                    logger.info("自动化调度器已正常关闭");
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
                logger.warn("自动化调度器关闭被中断");
            }
        }
    }
    
    /**
     * 启用/禁用自动化控制
     */
    public void setAutomationEnabled(boolean enabled) {
        boolean oldStatus = this.automationEnabled;
        this.automationEnabled = enabled;
        
        if (oldStatus != enabled) {
            if (enabled) {
                logger.info("自动化控制已启用");
            } else {
                logger.info("自动化控制已禁用");
            }
        }
    }
    
    /**
     * 获取自动化控制状态
     */
    public boolean isAutomationEnabled() {
        return automationEnabled;
    }
    
    /**
     * 获取执行统计信息
     */
    public Map<String, Object> getExecutionStats() {
        return Map.of(
            "totalExecutions", totalExecutions.get(),
            "successExecutions", successExecutions.get(),
            "errorExecutions", errorExecutions.get(),
            "automationEnabled", automationEnabled
        );
    }
    
    /**
     * 清空状态缓存
     */
    public void clearStateCache() {
        lastCurtainState.clear();
        lastSprayState.clear();
        lastWaterCurtainState.clear();
        lastBlowerState.clear();
        lastUltravioletState.clear();
        logger.info("状态缓存已清空");
    }

    /**
     * 更新设备状态缓存
     * 手动控制时同步状态缓存，避免自动化控制因缓存未更新而跳过控制
     * @param protocolId 协议ID
     * @param deviceType 设备类型 (1-风扇, 2-紫外线灯, 3-喷淋, 4-水帘, 5-窗帘机)
     * @param state 设备状态 (true-开启, false-关闭)
     */
    public void updateDeviceStateCache(Long protocolId, String deviceType, boolean state) {
        Map<Long, Boolean> stateCache = getStateCacheByDeviceType(deviceType);
        if (stateCache == null) {
            logger.warn("无法更新状态缓存，未知设备类型: {}", deviceType);
            return;
        }
        stateCache.put(protocolId, state);
        logger.info("更新设备状态缓存，protocolId: {}, deviceType: {}, state: {}", protocolId, deviceType, state);
    }
    
    /**
     * 执行自动化控制逻辑
     */
    private void executeAutomationControl() {
        logger.debug("开始执行自动化控制...");
        long startTime = System.currentTimeMillis();
        
        try {
            List<IotAutomationConfig> configs = automationConfigService.getEnabledConfigs();
            logger.debug("找到 {} 个启用的自动化配置", configs.size());
            
            if (configs.isEmpty()) {
                logger.debug("没有启用的自动化配置，跳过本次执行");
                return;
            }
            
            int successCount = 0;
            int errorCount = 0;
            
            for (IotAutomationConfig config : configs) {
                try {
                    if (isValidConfig(config)) {
                        TelemetryDto telemetryData = getLatestTelemetryData(config.getProtocolId());
                        
                        if (telemetryData != null && isValidTelemetryData(telemetryData)) {
                            executeAutomationRules(config, telemetryData);
                            successCount++;
                        } else {
                            logger.warn("传感器数据无效，跳过配置 protocolId: {}", config.getProtocolId());
                            errorCount++;
                        }
                    } else {
                        logger.warn("自动化配置无效，跳过 protocolId: {}", config.getProtocolId());
                        errorCount++;
                    }
                } catch (Exception e) {
                    logger.error("执行自动化配置失败，protocolId: {}", config.getProtocolId(), e);
                    errorCount++;
                }
            }
            
            successExecutions.addAndGet(successCount);
            errorExecutions.addAndGet(errorCount);
            
            long costTime = System.currentTimeMillis() - startTime;
            logger.info("自动化控制执行完成，成功: {}个，失败: {}个，耗时: {}ms", 
                       successCount, errorCount, costTime);
            
        } catch (Exception e) {
            logger.error("自动化控制执行失败", e);
        }
    }
    
    /**
     * 验证配置有效性
     */
    private boolean isValidConfig(IotAutomationConfig config) {
        return config != null && 
               config.getProtocolId() != null && 
               "1".equals(config.getEnabled());
    }
    
    /**
     * 验证传感器数据有效性
     */
    private boolean isValidTelemetryData(TelemetryDto telemetry) {
        return telemetry != null && 
               (telemetry.getTemperature() != null || 
                telemetry.getHumidity() != null || 
                telemetry.getIlluminance() != null || 
                telemetry.getSoilMoisture() != null || 
                telemetry.getCarbonDioxide() != null || 
                telemetry.getUltravioletRay() != null);
    }
    
    /**
     * 执行自动化规则
     */
    private void executeAutomationRules(IotAutomationConfig config, TelemetryDto telemetry) {
        Long protocolId = config.getProtocolId();
        logger.debug("执行自动化规则，protocolId: {}", protocolId);
        
        try {
            String sensorDataJson = buildSensorDataJson(telemetry);
            String thresholdJson = buildThresholdJson(config);
            
            // 执行各设备控制规则
            // 仅执行土壤湿度控制喷淋（一号）规则，其余设备控制规则不执行
            // executeTemperatureAndIlluminanceRule(config, telemetry, sensorDataJson, thresholdJson);
            executeSoilMoistureRule(config, telemetry, sensorDataJson, thresholdJson);
            // executeHumidityRule(config, telemetry, sensorDataJson, thresholdJson);
            // executeCarbonDioxideRule(config, telemetry, sensorDataJson, thresholdJson);
            // executeUltravioletRule(config, telemetry, sensorDataJson, thresholdJson);
            
            logger.debug("自动化规则执行完成，protocolId: {}", protocolId);
            
        } catch (Exception e) {
            logger.error("执行自动化规则失败，protocolId: {}", protocolId, e);
            recordAutomationOperation(protocolId, "0", "0", "系统异常", null, null, false, e.getMessage());
        }
    }
    
    /**
     * 规则1: 温度/光照度控制窗帘
     */
    private void executeTemperatureAndIlluminanceRule(IotAutomationConfig config, TelemetryDto telemetry, 
                                                     String sensorDataJson, String thresholdJson) {
        Long protocolId = config.getProtocolId();
        
        boolean temperatureCondition = isConditionMet(telemetry.getTemperature(), config.getSetTemperature(), true);
        boolean illuminanceCondition = isConditionMet(telemetry.getIlluminance(), config.getSetIlluminance(), true);
        
        boolean shouldCloseCurtain = temperatureCondition || illuminanceCondition;
        Boolean lastState = lastCurtainState.get(protocolId);
        
        // 状态无变化则跳过
        if (lastState != null && lastState == shouldCloseCurtain) {
            logger.debug("窗帘状态未变化，跳过控制 protocolId: {}", protocolId);
            return;
        }
        
        try {
            if (shouldCloseCurtain) {
                controlCurtainMachine(protocolId, 0);
                String reason = buildDualControlReason("温度/光照度条件触发", 
                    telemetry.getTemperature(), config.getSetTemperature(),
                    telemetry.getIlluminance(), config.getSetIlluminance());
                recordAutomationOperation(protocolId, DEVICE_TYPE_CURTAIN, OPERATION_OFF, 
                    reason, sensorDataJson, thresholdJson, true, null);
            } else {
                controlCurtainMachine(protocolId, 1);
                recordAutomationOperation(protocolId, DEVICE_TYPE_CURTAIN, OPERATION_ON, 
                    "温度/光照度条件未触发", sensorDataJson, thresholdJson, true, null);
            }
            
            lastCurtainState.put(protocolId, shouldCloseCurtain);
            
        } catch (Exception e) {
            logger.error("控制窗帘失败，protocolId: {}", protocolId, e);
            recordAutomationOperation(protocolId, DEVICE_TYPE_CURTAIN, OPERATION_OFF, 
                "控制异常", sensorDataJson, thresholdJson, false, e.getMessage());
        }
    }
    
    /**
     * 规则2: 土壤湿度控制喷淋
     */
    
    private void executeSoilMoistureRule(IotAutomationConfig config, TelemetryDto telemetry, 
                                       String sensorDataJson, String thresholdJson) {

        Long protocolId = config.getProtocolId();
        //读取配置的土壤湿度阈值 shouldTurnOnSpray
        boolean shouldTurnOnSpray = isConditionMet(telemetry.getSoilMoisture(), config.getSetSoilMoisture(), false);
        Boolean lastState = lastSprayState.get(protocolId);
        //记录上一次设备状态 避免重复触发 lastState
        if (lastState != null && lastState == shouldTurnOnSpray) {
            return;
        }
 
        
        try {
            if (shouldTurnOnSpray) {
                //判断当前土壤湿度是否满足触发喷淋的条件，完成设备自动化 controlSpray
                controlSpray(protocolId, 1);
                String reason = buildControlReason("土壤湿度条件触发", telemetry.getSoilMoisture(), config.getSetSoilMoisture());
                recordAutomationOperation(protocolId, DEVICE_TYPE_SPRAY, OPERATION_ON, 
                    reason, sensorDataJson, thresholdJson, true, null);
            } else {
                controlSpray(protocolId, 0);
                recordAutomationOperation(protocolId, DEVICE_TYPE_SPRAY, OPERATION_OFF, 
                    "土壤湿度条件未触发", sensorDataJson, thresholdJson, true, null);
            }
            
            lastSprayState.put(protocolId, shouldTurnOnSpray);
            
        } catch (Exception e) {
            logger.error("控制喷淋失败，protocolId: {}", protocolId, e);
            recordAutomationOperation(protocolId, DEVICE_TYPE_SPRAY, OPERATION_OFF, 
                "控制异常", sensorDataJson, thresholdJson, false, e.getMessage());
        }
    }
    
    /**
     * 规则3: 空气湿度控制水帘
     */
    private void executeHumidityRule(IotAutomationConfig config, TelemetryDto telemetry, 
                                   String sensorDataJson, String thresholdJson) {
        Long protocolId = config.getProtocolId();
        
        boolean shouldTurnOnWaterCurtain = isConditionMet(telemetry.getHumidity(), config.getSetHumidity(), true);
        Boolean lastState = lastWaterCurtainState.get(protocolId);
        
        if (lastState != null && lastState == shouldTurnOnWaterCurtain) {
            return;
        }
        
        try {
            if (shouldTurnOnWaterCurtain) {
                controlWaterCurtain(protocolId, 1);
                String reason = buildControlReason("空气湿度条件触发", telemetry.getHumidity(), config.getSetHumidity());
                recordAutomationOperation(protocolId, DEVICE_TYPE_WATER_CURTAIN, OPERATION_ON, 
                    reason, sensorDataJson, thresholdJson, true, null);
            } else {
                controlWaterCurtain(protocolId, 0);
                recordAutomationOperation(protocolId, DEVICE_TYPE_WATER_CURTAIN, OPERATION_OFF, 
                    "空气湿度条件未触发", sensorDataJson, thresholdJson, true, null);
            }
            
            lastWaterCurtainState.put(protocolId, shouldTurnOnWaterCurtain);
            
        } catch (Exception e) {
            logger.error("控制水帘失败，protocolId: {}", protocolId, e);
            recordAutomationOperation(protocolId, DEVICE_TYPE_WATER_CURTAIN, OPERATION_OFF, 
                "控制异常", sensorDataJson, thresholdJson, false, e.getMessage());
        }
    }
    
    /**
     * 规则4: 二氧化碳控制风扇
     */
    private void executeCarbonDioxideRule(IotAutomationConfig config, TelemetryDto telemetry, 
                                        String sensorDataJson, String thresholdJson) {
        Long protocolId = config.getProtocolId();
        
        boolean shouldTurnOnBlower = isConditionMet(telemetry.getCarbonDioxide(), config.getSetCarbonDioxide(), true);
        Boolean lastState = lastBlowerState.get(protocolId);
        
        if (lastState != null && lastState == shouldTurnOnBlower) {
            return;
        }
        
        try {
            if (shouldTurnOnBlower) {
                controlBlower(protocolId, 1);
                String reason = buildControlReason("二氧化碳条件触发", telemetry.getCarbonDioxide(), config.getSetCarbonDioxide());
                recordAutomationOperation(protocolId, DEVICE_TYPE_BLOWER, OPERATION_ON, 
                    reason, sensorDataJson, thresholdJson, true, null);
            } else {
                controlBlower(protocolId, 0);
                recordAutomationOperation(protocolId, DEVICE_TYPE_BLOWER, OPERATION_OFF, 
                    "二氧化碳条件未触发", sensorDataJson, thresholdJson, true, null);
            }
            
            lastBlowerState.put(protocolId, shouldTurnOnBlower);
            
        } catch (Exception e) {
            logger.error("控制风扇失败，protocolId: {}", protocolId, e);
            recordAutomationOperation(protocolId, DEVICE_TYPE_BLOWER, OPERATION_OFF, 
                "控制异常", sensorDataJson, thresholdJson, false, e.getMessage());
        }
    }
    
    /**
     * 规则5: 紫外线控制紫外线灯
     */
    private void executeUltravioletRule(IotAutomationConfig config, TelemetryDto telemetry, 
                                      String sensorDataJson, String thresholdJson) {
        Long protocolId = config.getProtocolId();
        
        boolean shouldTurnOnUltraviolet = isConditionMet(telemetry.getUltravioletRay(), config.getSetUltravioletRay(), false);
        Boolean lastState = lastUltravioletState.get(protocolId);
        
        if (lastState != null && lastState == shouldTurnOnUltraviolet) {
            return;
        }
        
        try {
            if (shouldTurnOnUltraviolet) {
                controlUltravioletRadiator(protocolId, 1);
                String reason = buildControlReason("紫外线条件触发", telemetry.getUltravioletRay(), config.getSetUltravioletRay());
                recordAutomationOperation(protocolId, DEVICE_TYPE_ULTRAVIOLET, OPERATION_ON, 
                    reason, sensorDataJson, thresholdJson, true, null);
            } else {
                controlUltravioletRadiator(protocolId, 0);
                recordAutomationOperation(protocolId, DEVICE_TYPE_ULTRAVIOLET, OPERATION_OFF, 
                    "紫外线条件未触发", sensorDataJson, thresholdJson, true, null);
            }
            
            lastUltravioletState.put(protocolId, shouldTurnOnUltraviolet);
            
        } catch (Exception e) {
            logger.error("控制紫外线灯失败，protocolId: {}", protocolId, e);
            recordAutomationOperation(protocolId, DEVICE_TYPE_ULTRAVIOLET, OPERATION_OFF, 
                "控制异常", sensorDataJson, thresholdJson, false, e.getMessage());
        }
    }
    
    /**
     * 判断条件是否满足
     */
    private boolean isConditionMet(Double currentValue, Double setValue, boolean greaterThan) {
        if (currentValue == null || setValue == null) {
            return false;
        }
        return greaterThan ? currentValue > setValue : currentValue < setValue;
    }
    
    /**
     * 记录自动化操作
     */
    private void recordAutomationOperation(Long protocolId, String deviceType, String operationStatus, 
                                         String controlReason, String sensorData, String thresholdSettings, 
                                         boolean success, String errorMessage) {
        try {
            IotAutomationRecord record = new IotAutomationRecord();
            record.setRecordId(SnowflakeIdGenerator.nextId());
            record.setProtocolId(protocolId);
            record.setControlType(CONTROL_TYPE_AUTO);
            record.setDeviceType(deviceType);
            record.setOperationStatus(operationStatus);
            record.setSensorData(sensorData);
            record.setControlReason(controlReason);
            record.setThresholdSettings(thresholdSettings);
            record.setExecuteResult(success ? EXECUTE_SUCCESS : EXECUTE_FAILED);
            record.setErrorMessage(errorMessage);
            
            automationRecordService.addRecord(record);
            logger.debug("自动化操作记录已保存，protocolId: {}, deviceType: {}", protocolId, deviceType);
            
        } catch (Exception e) {
            logger.error("保存自动化操作记录失败，protocolId: {}", protocolId, e);
        }
    }
    
    /**
     * 构建控制原因描述
     */
    private String buildControlReason(String condition, Double currentValue, Double setValue) {
        return String.format("%s: 当前值%.2f > 设置值%.2f", condition, currentValue, setValue);
    }
    
    private String buildDualControlReason(String condition, Double tempCurrent, Double tempSet, 
                                       Double illumCurrent, Double illumSet) {
        if (tempCurrent > tempSet && illumCurrent > illumSet) {
            return String.format("%s: 温度(%.2f>%.2f) 且 光照度(%.2f>%.2f)", condition, tempCurrent, tempSet, illumCurrent, illumSet);
        } else if (tempCurrent > tempSet) {
            return String.format("%s: 温度(%.2f>%.2f)", condition, tempCurrent, tempSet);
        } else {
            return String.format("%s: 光照度(%.2f>%.2f)", condition, illumCurrent, illumSet);
        }
    }
    
    /**
     * 构建传感器数据JSON
     */
    private String buildSensorDataJson(TelemetryDto telemetry) {
        try {
            return String.format("{\"temperature\":%.2f,\"humidity\":%.2f,\"illuminance\":%.2f,\"soilMoisture\":%.2f,\"carbonDioxide\":%.2f,\"ultravioletRay\":%.2f}", 
                               telemetry.getTemperature() != null ? telemetry.getTemperature() : 0.0,
                               telemetry.getHumidity() != null ? telemetry.getHumidity() : 0.0,
                               telemetry.getIlluminance() != null ? telemetry.getIlluminance() : 0.0,
                               telemetry.getSoilMoisture() != null ? telemetry.getSoilMoisture() : 0.0,
                               telemetry.getCarbonDioxide() != null ? telemetry.getCarbonDioxide() : 0.0,
                               telemetry.getUltravioletRay() != null ? telemetry.getUltravioletRay() : 0.0);
        } catch (Exception e) {
            logger.error("构建传感器数据JSON失败", e);
            return "{}";
        }
    }
    
    /**
     * 构建阈值设置JSON
     */
    private String buildThresholdJson(IotAutomationConfig config) {
        try {
            return String.format("{\"setTemperature\":%.2f,\"setHumidity\":%.2f,\"setIlluminance\":%.2f,\"setSoilMoisture\":%.2f,\"setCarbonDioxide\":%.2f,\"setUltravioletRay\":%.2f}", 
                               config.getSetTemperature() != null ? config.getSetTemperature() : 0.0,
                               config.getSetHumidity() != null ? config.getSetHumidity() : 0.0,
                               config.getSetIlluminance() != null ? config.getSetIlluminance() : 0.0,
                               config.getSetSoilMoisture() != null ? config.getSetSoilMoisture() : 0.0,
                               config.getSetCarbonDioxide() != null ? config.getSetCarbonDioxide() : 0.0,
                               config.getSetUltravioletRay() != null ? config.getSetUltravioletRay() : 0.0);
        } catch (Exception e) {
            logger.error("构建阈值设置JSON失败", e);
            return "{}";
        }
    }
    
    /**
     * 控制风扇
     */
    private void controlBlower(Long protocolId, Integer status) {
        try {
            if (status == 0) {
                tcpClientManager.sendMessage(protocolId, "b2");
                logger.debug("自动化控制风扇关闭，protocolId: {}", protocolId);
            } else if (status == 1) {
                tcpClientManager.sendMessage(protocolId, "a2");
                logger.debug("自动化控制风扇开启，protocolId: {}", protocolId);
            }
        } catch (Exception e) {
            logger.error("控制风扇失败，protocolId: {}", protocolId, e);
            throw new RuntimeException("控制风扇失败", e);
        }
    }
    
    /**
     * 控制紫外线灯
     */
    private void controlUltravioletRadiator(Long protocolId, Integer status) {
        try {
            if (status == 0) {
                tcpClientManager.sendMessage(protocolId, "b3");
                logger.debug("自动化控制紫外线灯关闭，protocolId: {}", protocolId);
            } else if (status == 1) {
                tcpClientManager.sendMessage(protocolId, "a3");
                logger.debug("自动化控制紫外线灯开启，protocolId: {}", protocolId);
            }
        } catch (Exception e) {
            logger.error("控制紫外线灯失败，protocolId: {}", protocolId, e);
            throw new RuntimeException("控制紫外线灯失败", e);
        }
    }
    
    /**
     * 控制喷淋器
     */
    private void controlSpray(Long protocolId, Integer status) {
        try {
            if (status == 0) {
                tcpClientManager.sendMessage(protocolId, "b1");
                logger.debug("自动化控制喷淋器关闭，protocolId: {}", protocolId);
            } else if (status == 1) {
                tcpClientManager.sendMessage(protocolId, "a1");
                logger.debug("自动化控制喷淋器开启，protocolId: {}", protocolId);
            }
        } catch (Exception e) {
            logger.error("控制喷淋器失败，protocolId: {}", protocolId, e);
            throw new RuntimeException("控制喷淋器失败", e);
        }
    }
    
    /**
     * 控制水帘幕
     */
    private void controlWaterCurtain(Long protocolId, Integer status) {
        try {
            if (status == 0) {
                tcpClientManager.sendMessage(protocolId, "b4");
                logger.debug("自动化控制水帘幕关闭，protocolId: {}", protocolId);
            } else if (status == 1) {
                tcpClientManager.sendMessage(protocolId, "a4");
                logger.debug("自动化控制水帘幕开启，protocolId: {}", protocolId);
            }
        } catch (Exception e) {
            logger.error("控制水帘幕失败，protocolId: {}", protocolId, e);
            throw new RuntimeException("控制水帘幕失败", e);
        }
    }
    
    /**
     * 控制窗帘机
     */
    private void controlCurtainMachine(Long protocolId, Integer status) {
        try {
            IotDevice iotDevice = iotDeviceService.selectDeviceByRelayId(protocolId);
            if (iotDevice == null) {
                logger.error("设备不存在，protocolId: {}", protocolId);
                throw new RuntimeException("设备不存在");
            }
            
            IotProtocol protocol = iotProtocolService.getProtocolById(iotDevice.getProtocolId());
            if (protocol == null) {
                logger.error("MQTT设备协议不存在，protocolId: {}", protocolId);
                throw new RuntimeException("MQTT设备协议不存在");
            }
            
            IotCurtainMachineStatus machineStatus = new IotCurtainMachineStatus();
            machineStatus.setCurtainMachineId(SnowflakeIdGenerator.nextId());
            machineStatus.setCurtainMachineName("窗帘机");
            machineStatus.setProtocolId(protocol.getProtocolId());
            
            String message;
            if (status == 0) {
                message = "0";
                machineStatus.setStatus(OPERATION_OFF);
                logger.debug("自动化控制窗帘关闭，protocolId: {}", protocolId);
            } else if (status == 1) {
                message = "1";
                machineStatus.setStatus(OPERATION_ON);
                logger.debug("自动化控制窗帘开启，protocolId: {}", protocolId);
            } else {
                message = "2";
                machineStatus.setStatus(OPERATION_PAUSE);
                logger.debug("自动化控制窗帘暂停，protocolId: {}", protocolId);
            }
            
            int publish = mqttManager.publish(null, String.valueOf(protocol.getSendTopic()), message, 0, false);
            iotCurtainMachineStatusService.addCurtainMachineStatus(machineStatus);
            
            if (publish == 1) {
                logger.debug("✅ MQTT发布成功：{}", protocolId);
            } else {
                logger.warn("❌ MQTT发布失败：{}", protocolId);
                throw new RuntimeException("MQTT发布失败");
            }
            
        } catch (Exception e) {
            logger.error("控制窗帘机失败，protocolId: {}", protocolId, e);
            throw new RuntimeException("控制窗帘机失败", e);
        }
    }
    
    /**
     * 获取最新的传感器数据
     */
    private TelemetryDto getLatestTelemetryData(Long protocolId) {
        logger.debug("获取传感器数据，protocolId: {}", protocolId);
        
        try {
            IotDevice iotDevice = iotDeviceService.selectDeviceByRelayId(protocolId);
            if (iotDevice == null) {
                throw new IllegalArgumentException("设备不存在");
            }
            
            IotProtocol protocol = iotProtocolService.getProtocolById(iotDevice.getProtocolId());
            if (protocol == null) {
                throw new IllegalArgumentException("MQTT设备协议不存在");
            }
            
            Map<String, Map<String, Object>> allData = MqttConnection.getLatestMessages();
            Map<String, Object> message = allData.getOrDefault(protocol.getProtocolConfig().getTopic(), 
                Map.of("message", "No data for topic: " + protocol.getProtocolConfig().getTopic()));

            TelemetryDto data = new TelemetryDto();

            // 设置传感器数据，添加默认值处理
            data.setTemperature(getDoubleValue(message, "temperature", 25.0));
            data.setHumidity(getDoubleValue(message, "humidity", 60.0));
            data.setIlluminance(getDoubleValue(message, "illuminance", 800.0));
            data.setSoilMoisture(getDoubleValue(message, "soilmoisture", 40.0));
            data.setCarbonMonoxide(getDoubleValue(message, "carbonmonoxide", 20.0));
            data.setCarbonDioxide(getDoubleValue(message, "carbondioxide", 450.0));
            data.setUltravioletRay(getDoubleValue(message, "ultravioletray", 5.0));

            logger.debug("获取到传感器数据 - 温度: {}°C, 湿度: {}%, 光照度: {}lux, 紫外线: {}",
                    data.getTemperature(), data.getHumidity(), data.getIlluminance(), data.getUltravioletRay());

            return data;
            
        } catch (Exception e) {
            logger.error("获取传感器数据失败，protocolId: {}", protocolId, e);
            return null;
        }
    }

    private Double getDoubleValue(Map<String, Object> message, String key, Double defaultValue) {
        try {
            Object value = message.get(key);
            if (value != null) {
                if (value instanceof Number) {
                    return ((Number) value).doubleValue();
                } else if (value instanceof String) {
                    return Double.parseDouble((String) value);
                }
            }
            return defaultValue;
        } catch (Exception e) {
            logger.warn("无法解析{}的值，使用默认值: {}", key, defaultValue, e);
            return defaultValue;
        }
    }

    private Integer getIntegerValue(Map<String, Object> message, String key, Integer defaultValue) {
        try {
            Object value = message.get(key);
            if (value != null) {
                if (value instanceof Number) {
                    return ((Number) value).intValue();
                } else if (value instanceof String) {
                    return Integer.parseInt((String) value);
                }
            }
            return defaultValue;
        } catch (Exception e) {
            logger.warn("无法解析{}的值，使用默认值: {}", key, defaultValue, e);
            return defaultValue;
        }
    }

    /**
     * 监测设备控制模式（手动/自动）
     * 直接返回当前系统的控制模式，不查询数据库
     * @return 1-手动控制，2-自动控制
     */
    public String monitorControlMode() {
        // 直接返回当前自动化控制系统的启用状态
        return automationEnabled ? "2" : "1";
    }

    /**
     * 监测指定设备的控制模式（手动/自动）
     * 基于状态缓存判断，不查询数据库
     * @param protocolId 协议ID
     * @param deviceType 设备类型
     * @return 1-手动控制，2-自动控制
     */
    public String monitorControlMode(Long protocolId, String deviceType) {
        logger.debug("监测控制模式，protocolId: {}, deviceType: {}", protocolId, deviceType);

        // 如果系统级自动化控制被禁用，直接返回手动模式
        if (!automationEnabled) {
            logger.debug("系统自动化已禁用，返回手动控制模式");
            return "1";
        }

        try {
            // 检查该设备在状态缓存中是否有记录
            // 如果有记录说明最近被自动化系统控制过，返回自动模式
            // 如果没有记录说明可能是手动控制，返回手动模式
            Map<Long, Boolean> stateCache = getStateCacheByDeviceType(deviceType);

            if (stateCache != null && stateCache.containsKey(protocolId)) {
                logger.debug("设备在状态缓存中存在，返回自动控制模式，protocolId: {}, deviceType: {}", protocolId, deviceType);
                return "2";
            } else {
                logger.debug("设备在状态缓存中不存在，返回手动控制模式，protocolId: {}, deviceType: {}", protocolId, deviceType);
                return "1";
            }

        } catch (Exception e) {
            logger.error("监测控制模式失败，返回默认自动模式，protocolId: {}, deviceType: {}", protocolId, deviceType, e);
            return "2"; // 发生异常时返回自动模式
        }
    }

    /**
     * 获取所有设备的控制模式统计
     * @return 控制模式统计信息
     */
    public Map<String, Object> getControlModeStatistics() {
        Map<String, Object> stats = new ConcurrentHashMap<>();

        // 统计各设备类型的自动控制设备数量
        int autoCurtainCount = lastCurtainState.size();
        int autoSprayCount = lastSprayState.size();
        int autoWaterCurtainCount = lastWaterCurtainState.size();
        int autoBlowerCount = lastBlowerState.size();
        int autoUltravioletCount = lastUltravioletState.size();

        int totalAutoDevices = autoCurtainCount + autoSprayCount + autoWaterCurtainCount +
                autoBlowerCount + autoUltravioletCount;

        stats.put("systemAutomationEnabled", automationEnabled);
        stats.put("totalAutoDevices", totalAutoDevices);
        stats.put("autoCurtainCount", autoCurtainCount);
        stats.put("autoSprayCount", autoSprayCount);
        stats.put("autoWaterCurtainCount", autoWaterCurtainCount);
        stats.put("autoBlowerCount", autoBlowerCount);
        stats.put("autoUltravioletCount", autoUltravioletCount);
        stats.put("timestamp", System.currentTimeMillis());

        logger.debug("控制模式统计 - 系统自动化: {}, 自动控制设备总数: {}", automationEnabled, totalAutoDevices);
        return stats;
    }

    /**
     * 获取所有设备的详细控制模式列表
     * @return 包含所有设备控制模式的详细列表
     */
    public Map<String, Map<Long, String>> getAllDevicesControlMode() {
        Map<String, Map<Long, String>> result = new ConcurrentHashMap<>();

        // 获取各类型设备的控制模式
        result.put(DEVICE_TYPE_CURTAIN, convertStateCacheToControlMode(lastCurtainState));
        result.put(DEVICE_TYPE_SPRAY, convertStateCacheToControlMode(lastSprayState));
        result.put(DEVICE_TYPE_WATER_CURTAIN, convertStateCacheToControlMode(lastWaterCurtainState));
        result.put(DEVICE_TYPE_BLOWER, convertStateCacheToControlMode(lastBlowerState));
        result.put(DEVICE_TYPE_ULTRAVIOLET, convertStateCacheToControlMode(lastUltravioletState));

        logger.debug("获取所有设备控制模式完成，共{}种设备类型", result.size());
        return result;
    }

    /**
     * 将状态缓存转换为控制模式映射
     */
    private Map<Long, String> convertStateCacheToControlMode(Map<Long, Boolean> stateCache) {
        Map<Long, String> controlModeMap = new ConcurrentHashMap<>();
        if (stateCache != null) {
            for (Long protocolId : stateCache.keySet()) {
                controlModeMap.put(protocolId, "2"); // 在缓存中的设备都是自动控制
            }
        }
        return controlModeMap;
    }

    /**
     * 根据设备类型获取对应的状态缓存
     */
    private Map<Long, Boolean> getStateCacheByDeviceType(String deviceType) {
        switch (deviceType) {
            case DEVICE_TYPE_CURTAIN:
                return lastCurtainState;
            case DEVICE_TYPE_SPRAY:
                return lastSprayState;
            case DEVICE_TYPE_WATER_CURTAIN:
                return lastWaterCurtainState;
            case DEVICE_TYPE_BLOWER:
                return lastBlowerState;
            case DEVICE_TYPE_ULTRAVIOLET:
                return lastUltravioletState;
            default:
                logger.warn("未知的设备类型: {}", deviceType);
                return null;
        }
    }
}