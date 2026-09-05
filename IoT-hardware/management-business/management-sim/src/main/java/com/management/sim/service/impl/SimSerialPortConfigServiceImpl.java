package com.management.sim.service.impl;

import com.management.common.core.domain.AjaxResult;
import com.management.common.utils.snowFlake.SnowflakeIdGenerator;
import com.management.sim.constant.EquipmentConfigurationConstant;
import com.management.sim.domain.SimSerialPortConfig;
import com.management.sim.domain.dto.ValidationResult;
import com.management.sim.mapper.SimSerialPortConfigMapper;
import com.management.sim.service.SimSerialPortConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SimSerialPortConfigServiceImpl implements SimSerialPortConfigService {

    @Autowired
    private SimSerialPortConfigMapper simSerialPortConfigMapper;

    /**
     * 获取所有串口配置
     *
     * @return
     */
    @Override
    public List<SimSerialPortConfig> getAllSerialPortConfig() {

        try {
            List<SimSerialPortConfig> simSerialPortConfigs = simSerialPortConfigMapper.getAllSerialPortConfig();

            System.out.println("simSerialPortConfigs: " + simSerialPortConfigs);

            return simSerialPortConfigs;
        } catch (Exception e) {
            throw new RuntimeException("获取串口配置类型列表失败: " + e.getMessage(), e);
        }

    }

    @Override
    public AjaxResult saveSerialPortConfig(SimSerialPortConfig config) {

        AjaxResult result = new AjaxResult();

        try {

            ValidationResult channelValidation = validateChannelValue(config.getChannel());
            if (!channelValidation.isValid()) {
                return AjaxResult.error("保存串口配置失败: " + channelValidation.getMessage());
            }
            if (!channelValidation.getWarnings().isEmpty()) {
                log.warn("信道配置警告: {}", channelValidation.getMessage());
            }

            // 如果是路由器或终端，检查是否有对应的协调器
            if (!EquipmentConfigurationConstant.COORDINATOR.equals(config.getEquipmentConfigurationType())) {
                // 如果uniqueIdentification为空，尝试查找可用的协调器
                if (config.getUniqueIdentification() == null || config.getUniqueIdentification().trim().isEmpty()) {
                    // 获取所有协调器配置
                    List<SimSerialPortConfig> allCoordinators = simSerialPortConfigMapper.getAllCoordinators(config.getEquipmentConfigurationType());

                    if (allCoordinators.isEmpty()) {
                        return AjaxResult.error("保存串口配置失败: 没有可用的协调器，请先添加协调器");
                    }

                    // 根据panId查找匹配的协调器
                    SimSerialPortConfig matchedCoordinator = allCoordinators.stream()
                            .filter(c -> c.getPanId().equals(config.getPanId())
                                    && c.getChannel().equals(String.valueOf(config.getChannel())))
                            .findFirst()
                            .orElse(null);

                    if (matchedCoordinator == null) {
                        // 如果没有匹配的协调器，列出所有可用的协调器
                        String availableCoordinators = allCoordinators.stream()
                                .map(c -> String.format("PAN ID: %s, 信道: %s", c.getPanId(), c.getChannel()))
                                .collect(Collectors.joining("; "));

                        return AjaxResult.error(String.format("保存串口配置失败: 没有找到PAN ID为%s、信道为%s的协调器，可用协调器: %s",
                                config.getPanId(), config.getChannel(), availableCoordinators));
                    }

                    // 使用找到的协调器的uniqueIdentification
                    config.setUniqueIdentification(matchedCoordinator.getUniqueIdentification());
                    log.info("路由器/终端自动关联到协调器，uniqueIdentification: {}", matchedCoordinator.getUniqueIdentification());
                }
            } else {
                // 如果是协调器且uniqueIdentification为空，生成新的
                if (config.getUniqueIdentification() == null || config.getUniqueIdentification().trim().isEmpty()) {
                    config.setUniqueIdentification(UUID.randomUUID().toString());
                }
            }

            // 获取现有配置（使用确定的uniqueIdentification）
            List<SimSerialPortConfig> existingConfigs =
                    simSerialPortConfigMapper.getUniqueIdentification(config.getUniqueIdentification());

            // 执行Zigbee组网验证
            ValidationResult validationResult = validateZigbeeConfig(config, existingConfigs);

            if (!validationResult.isValid()) {
                return AjaxResult.error("保存串口配置失败: " + validationResult.getMessage());
            }

            // 如果有警告，可以记录日志
            if (!validationResult.getWarnings().isEmpty()) {
                log.warn("Zigbee配置警告: {}", validationResult.getMessage());
            }

            config.setSerialId(SnowflakeIdGenerator.nextId());
            int rows = simSerialPortConfigMapper.saveSerialPortConfig(config);
            if (rows > 0){
                result.put("msg", "保存串口配置成功");
                result.put("data", config);
                result.put("code", 200);
                return result;
            }else {
                return AjaxResult.error("保存串口配置失败");
            }

        } catch (Exception e) {
            log.error("保存串口配置失败", e);
            return AjaxResult.error("保存串口配置失败: " + e.getMessage());
        }

    }

    /**
     * 根据serialId获取串口配置
     */
    @Override
    public SimSerialPortConfig getBySerialId(Long serialId) {

        try {

            if (serialId == null){
                return null;
            }

            SimSerialPortConfig simSerialPortConfig = simSerialPortConfigMapper.getBySerialId(serialId);
            return simSerialPortConfig;
        } catch (Exception e) {
            throw new RuntimeException("获取串口配置失败: " + e.getMessage(), e);
        }

    }

    /**
     * 校验Zigbee配置是否符合组网规则
     */
    public ValidationResult validateZigbeeConfig(SimSerialPortConfig newConfig,
                                                 List<SimSerialPortConfig> existingConfigs) {
        ValidationResult result = new ValidationResult();

        // 1. 协调器唯一性检查
        if (EquipmentConfigurationConstant.COORDINATOR.equals(newConfig.getEquipmentConfigurationType())) {
            checkCoordinatorUniqueness(newConfig, existingConfigs, result);
        }

        // 2. 组网一致性检查
        checkNetworkConsistency(newConfig, existingConfigs, result);

        return result;
    }

    /**
     * 检查协调器唯一性
     * Zigbee网络中只能有一个协调器具有相同的PAN ID和信道
     */
    private void checkCoordinatorUniqueness(SimSerialPortConfig newConfig,
                                            List<SimSerialPortConfig> existingConfigs,
                                            ValidationResult result) {
        // 只检查同一唯一标识下的设备
        List<SimSerialPortConfig> sameGroupDevices = existingConfigs.stream()
                .filter(c -> c.getUniqueIdentification().equals(newConfig.getUniqueIdentification()))
                .collect(Collectors.toList());

        // 如果新配置是协调器
        if (EquipmentConfigurationConstant.COORDINATOR.equals(newConfig.getEquipmentConfigurationType())) {

            // 查找同组内是否有相同PAN ID的其他协调器
            for (SimSerialPortConfig existing : sameGroupDevices) {
                if (EquipmentConfigurationConstant.COORDINATOR.equals(existing.getEquipmentConfigurationType())
                        && existing.getPanId().equals(newConfig.getPanId())) {

                    // 检查是否是同一设备（更新场景）
                    boolean isSelf = existing.getSerialId() != null
                            && existing.getSerialId().equals(newConfig.getSerialId());

                    if (!isSelf) {
                        result.addError(String.format("同一个唯一标识下不能有相同PAN ID(%s)的多个协调器", newConfig.getPanId()));
                        return;
                    }
                }
            }

            // 可选：记录不同PAN ID的协调器（仅用于日志）
            long differentPanIdCoordinators = sameGroupDevices.stream()
                    .filter(c -> EquipmentConfigurationConstant.COORDINATOR.equals(c.getEquipmentConfigurationType())
                            && !c.getPanId().equals(newConfig.getPanId()))
                    .count();

            if (differentPanIdCoordinators > 0) {
                log.info("同一唯一标识下存在多个不同PAN ID的协调器，当前PAN ID: {}", newConfig.getPanId());
            }
        }
    }

    /**
     * 检查组网一致性
     * 同一组网下的设备：
     * 1. 如果都是协调器，只要PAN ID不一致，信道不做要求
     * 2. 路由器/终端可以连接到任何一个协调器（只要PAN ID和信道与某个协调器匹配即可）
     */
    private void checkNetworkConsistency(SimSerialPortConfig newConfig,
                                         List<SimSerialPortConfig> existingConfigs,
                                         ValidationResult result) {

        // 获取同组设备（相同唯一标识）
        List<SimSerialPortConfig> sameGroupDevices = existingConfigs.stream()
                .filter(c -> c.getUniqueIdentification().equals(newConfig.getUniqueIdentification()))
                .collect(Collectors.toList());

        if (!sameGroupDevices.isEmpty()) {

            // 统计同组内的协调器
            List<SimSerialPortConfig> coordinators = sameGroupDevices.stream()
                    .filter(c -> EquipmentConfigurationConstant.COORDINATOR.equals(c.getEquipmentConfigurationType()))
                    .collect(Collectors.toList());

            // 统计同组内的非协调器（路由器/终端）
            List<SimSerialPortConfig> nonCoordinators = sameGroupDevices.stream()
                    .filter(c -> !EquipmentConfigurationConstant.COORDINATOR.equals(c.getEquipmentConfigurationType()))
                    .collect(Collectors.toList());

            boolean isNewConfigCoordinator = EquipmentConfigurationConstant.COORDINATOR.equals(newConfig.getEquipmentConfigurationType());

            // 场景1：所有设备都是协调器（包括新配置）
            if (isNewConfigCoordinator && nonCoordinators.isEmpty()) {
                // 检查是否所有协调器的PAN ID都不相同
                Set<String> panIds = new HashSet<>();
                panIds.add(newConfig.getPanId());

                for (SimSerialPortConfig coordinator : coordinators) {
                    // 排除自身（更新场景）
                    boolean isSelf = coordinator.getSerialId() != null
                            && coordinator.getSerialId().equals(newConfig.getSerialId());

                    if (!isSelf) {
                        if (panIds.contains(coordinator.getPanId())) {
                            result.addError("同一组网下多个协调器的PAN ID不能相同，冲突PAN ID: " + coordinator.getPanId());
                            return;
                        }
                        panIds.add(coordinator.getPanId());
                    }
                }

                // 信道不做要求，只记录日志
                if (!coordinators.isEmpty()) {
                    Set<String> channels = new HashSet<>();
                    channels.add(newConfig.getChannel());
                    for (SimSerialPortConfig coordinator : coordinators) {
                        channels.add(coordinator.getChannel());
                    }
                    if (channels.size() > 1) {
                        log.info("同一组网下多个协调器使用不同信道: {}", channels);
                    }
                }
            }
            // 场景2：存在协调器和路由器/终端混合
            else if (coordinators.size() > 0) {

                // 收集所有协调器的PAN ID和信道
                Set<String> coordinatorPanIds = coordinators.stream()
                        .map(SimSerialPortConfig::getPanId)
                        .collect(Collectors.toSet());

                Set<String> coordinatorChannels = coordinators.stream()
                        .map(SimSerialPortConfig::getChannel)
                        .collect(Collectors.toSet());

                // 新设备如果是协调器，必须与现有协调器PAN ID不同（除非是更新自身）
                if (isNewConfigCoordinator) {
                    boolean isSelf = false;
                    for (SimSerialPortConfig coordinator : coordinators) {
                        if (coordinator.getSerialId() != null && coordinator.getSerialId().equals(newConfig.getSerialId())) {
                            isSelf = true;
                            break;
                        }
                    }

                    if (!isSelf && coordinatorPanIds.contains(newConfig.getPanId())) {
                        result.addError("协调器的PAN ID不能与现有协调器相同，除非是同一设备");
                        return;
                    }
                }

                // 新设备如果是路由器/终端，必须与至少一个协调器的PAN ID和信道匹配
                if (!isNewConfigCoordinator) {
                    boolean panIdMatch = coordinatorPanIds.contains(newConfig.getPanId());
                    boolean channelMatch = coordinatorChannels.contains(newConfig.getChannel());

                    if (!panIdMatch) {
                        result.addError(String.format("路由器/终端的PAN ID(%s)必须与某个协调器的PAN ID一致，可用PAN ID: %s",
                                newConfig.getPanId(), coordinatorPanIds));
                    }
                    if (!channelMatch) {
                        result.addError(String.format("路由器/终端的信道(%s)必须与某个协调器的信道一致，可用信道: %s",
                                newConfig.getChannel(), coordinatorChannels));
                    }
                }

                // 检查已存在的路由器/终端是否与至少一个协调器匹配
                for (SimSerialPortConfig nonCoordinator : nonCoordinators) {
                    boolean panIdMatch = coordinatorPanIds.contains(nonCoordinator.getPanId());
                    boolean channelMatch = coordinatorChannels.contains(nonCoordinator.getChannel());

                    if (!panIdMatch) {
                        result.addError(String.format("路由器/终端的PAN ID(%s)必须与某个协调器的PAN ID一致，可用PAN ID: %s",
                                nonCoordinator.getPanId(), coordinatorPanIds));
                    }
                    if (!channelMatch) {
                        result.addError(String.format("路由器/终端的信道(%s)必须与某个协调器的信道一致，可用信道: %s",
                                nonCoordinator.getChannel(), coordinatorChannels));
                    }
                }
            }
            // 场景3：没有协调器，只有路由器/终端（这应该是错误配置）
            else if (nonCoordinators.size() > 0 && isNewConfigCoordinator) {
                // 新配置是协调器，但已有设备都是路由器/终端，这是允许的
                log.info("为现有的路由器/终端组添加协调器");
            }
            else if (nonCoordinators.size() > 0 && !isNewConfigCoordinator) {
                // 没有协调器，只有路由器/终端，应该报错
                result.addError("存在路由器/终端但没有协调器，请先添加协调器");
            }
        }

        // 检查跨组干扰
        checkCrossGroupInterference(newConfig, existingConfigs, result);
    }

    /**
     * 检查不同组网间的干扰
     * 不同组网可以通过不同PAN ID或不同信道来区分
     */
    private void checkCrossGroupInterference(SimSerialPortConfig newConfig,
                                             List<SimSerialPortConfig> existingConfigs,
                                             ValidationResult result) {

        for (SimSerialPortConfig existing : existingConfigs) {
            // 不同组的设备
            if (!existing.getUniqueIdentification().equals(newConfig.getUniqueIdentification())) {

                // 如果PAN ID和信道都相同，则会产生干扰
                if (existing.getPanId().equals(newConfig.getPanId())
                        && existing.getChannel().equals(newConfig.getChannel())) {

                    result.addWarning("不同组网设备使用了相同的PAN ID(" + newConfig.getPanId()
                            + ")和信道(" + newConfig.getChannel() + ")，可能会产生干扰");
                }
            }
        }
    }

    /**
     * 校验信道值 - 支持Integer和String类型
     */
    private ValidationResult validateChannelValue(Object channelObj) {
        ValidationResult result = new ValidationResult();

        if (channelObj == null) {
            result.addError("信道不能为空");
            return result;
        }

        // 处理Integer类型（十进制数字）
        if (channelObj instanceof Integer) {
            Integer channel = (Integer) channelObj;
            return validateChannel(channel);
        }

        // 处理String类型（可能是不带0x的十六进制字符串）
        if (channelObj instanceof String) {
            String channelStr = (String) channelObj;
            return validateHexString(channelStr);
        }

        result.addError("信道格式错误，请输入数字或十六进制字符串");
        return result;
    }

    /**
     * 校验十进制数字信道
     */
    private ValidationResult validateChannel(Integer channel) {
        ValidationResult result = new ValidationResult();

        // Zigbee有效信道范围：11-26
        int minChannel = 11;
        int maxChannel = 26;

        if (channel < minChannel || channel > maxChannel) {
            String hexValue = String.format("%02X", channel); // 格式化为两位十六进制，不带0x
            result.addError(String.format("信道必须在 %d-%d 范围内，当前值: %d (十六进制: %s)",
                    minChannel, maxChannel, channel, hexValue));
            return result;
        }

        // 记录转换信息
        log.debug("十进制信道校验通过: {} (十六进制: {})", channel, String.format("%02X", channel));

        return result;
    }

    /**
     * 校验不带0x的十六进制字符串
     * 如: "0B" -> 11, "0C" -> 12, ..., "1A" -> 26
     */
    private ValidationResult validateHexString(String hexStr) {
        ValidationResult result = new ValidationResult();

        if (hexStr == null || hexStr.trim().isEmpty()) {
            result.addError("信道不能为空");
            return result;
        }

        String trimmed = hexStr.trim().toUpperCase();

        // 验证是否是有效的十六进制格式（只允许0-9 A-F，长度1-2位）
        if (!trimmed.matches("^[0-9A-F]{1,2}$")) {
            result.addError("十六进制格式错误，请输入1-2位十六进制数（如：0B、0C、1A）");
            return result;
        }

        try {
            // 解析十六进制字符串为十进制数字
            Integer channel = Integer.parseInt(trimmed, 16);

            // Zigbee有效信道范围：11-26
            int minChannel = 11;
            int maxChannel = 26;

            if (channel < minChannel || channel > maxChannel) {
                result.addError(String.format("信道必须在 %d-%d 范围内，当前十六进制值: %s (十进制: %d)",
                        minChannel, maxChannel, trimmed, channel));
                return result;
            }

            // 将转换后的十进制数字设置回config
            // 注意：这里需要根据实际情况设置，可能需要在调用前处理
            log.debug("十六进制信道校验通过: {} -> 十进制: {}", trimmed, channel);

            // 添加信道使用建议
            if (channel == 26) {
                result.addWarning("信道26是Zigbee规范定义的最高信道，请确保设备支持");
            }

            return result;

        } catch (NumberFormatException e) {
            result.addError("无效的十六进制数: " + trimmed);
            return result;
        }
    }

}
