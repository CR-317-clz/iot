package com.management.iot.service.impl;

import com.management.common.utils.snowFlake.SnowflakeIdGenerator;
import com.management.iot.domain.IotAutomationConfig;
import com.management.iot.domain.IotDevice;
import com.management.iot.mapper.IotAutomationConfigMapper;
import com.management.iot.service.IotAutomationConfigService;
import com.management.iot.service.IotDeviceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 自动化配置业务逻辑层实现类
 * 实现自动化配置相关的业务逻辑
 * @author iot
 */
@Service
public class IotAutomationConfigServiceImpl implements IotAutomationConfigService {

    private static final Logger logger = LoggerFactory.getLogger(IotAutomationConfigServiceImpl.class);

    @Autowired
    private IotAutomationConfigMapper automationConfigMapper;

    @Autowired
    private IotDeviceService  iotDeviceService;

    /**
     * 查询所有启用的自动化配置
     * 自动化调度器定期调用此方法获取需要执行的配置
     */
    @Override
    public List<IotAutomationConfig> getEnabledConfigs() {
        logger.debug("查询所有启用的自动化配置");
        try {
            List<IotAutomationConfig> configs = automationConfigMapper.selectEnabledConfigs();
            logger.info("成功查询到 {} 个启用的自动化配置", configs.size());
            return configs;
        } catch (Exception e) {
            logger.error("查询启用的自动化配置失败", e);
            throw new RuntimeException("查询自动化配置失败", e);
        }
    }

    /**
     * 根据协议ID查询自动化配置
     * 用于检查设备是否已存在自动化配置
     */
    @Override
    public IotAutomationConfig getConfigByProtocolId(Long protocolId) {
        logger.debug("根据协议ID查询自动化配置，protocolId: {}", protocolId);
        if (protocolId == null) {
            logger.warn("协议ID为空，无法查询自动化配置");
            return null;
        }
        
        try {
            IotAutomationConfig config = automationConfigMapper.selectConfigByProtocolId(protocolId);
            if (config == null) {
                logger.debug("未找到协议ID为 {} 的自动化配置", protocolId);
            } else {
                logger.debug("成功找到协议ID为 {} 的自动化配置", protocolId);
            }
            return config;
        } catch (Exception e) {
            logger.error("根据协议ID查询自动化配置失败，protocolId: {}", protocolId, e);
            throw new RuntimeException("查询自动化配置失败", e);
        }
    }

    /**
     * 新增或更新自动化配置（核心方法）
     * 第一次传参时新增配置，后续传参时更新配置
     * 这个方法实现了配置的智能保存逻辑
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int saveOrUpdateConfig(IotAutomationConfig config) {
        logger.info("开始保存或更新自动化配置，protocolId: {}", config.getProtocolId());
        
        // 参数校验
        if (config.getProtocolId() == null) {
            throw new IllegalArgumentException("设备协议ID不能为空");
        }
        
        try {
            // 检查是否已存在配置
            IotAutomationConfig existingConfig = automationConfigMapper.selectConfigByProtocolId(config.getProtocolId());
            
            if (existingConfig == null) {
                // 第一次传参，新增配置
                logger.info("第一次传参，新增自动化配置，protocolId: {}", config.getProtocolId());
                config.setCreateTime(new Date());
                config.setUpdateTime(new Date());
                // 如果未设置启用状态，默认启用
                if (config.getEnabled() == null) {
                    config.setEnabled("1");
                }
                config.setAutomationId(SnowflakeIdGenerator.nextId());
                int result = automationConfigMapper.insertConfig(config);
                logger.info("新增自动化配置成功，protocolId: {}, automationId: {}", 
                           config.getProtocolId(), config.getAutomationId());
                return result;
            } else {
                // 后续传参，更新配置
                logger.info("更新已存在的自动化配置，protocolId: {}, automationId: {}", 
                           config.getProtocolId(), existingConfig.getAutomationId());
                config.setAutomationId(existingConfig.getAutomationId());
                config.setUpdateTime(new Date());
                int result = automationConfigMapper.updateConfig(config);
                logger.info("更新自动化配置成功，protocolId: {}", config.getProtocolId());
                return result;
            }
        } catch (Exception e) {
            logger.error("保存或更新自动化配置失败，protocolId: {}", config.getProtocolId(), e);
            throw new RuntimeException("保存自动化配置失败", e);
        }
    }

    /**
     * 新增自动化配置
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int addConfig(IotAutomationConfig config) {
        logger.info("新增自动化配置，protocolId: {}", config.getProtocolId());
        
        // 参数校验
        if (config.getProtocolId() == null) {
            throw new IllegalArgumentException("设备协议ID不能为空");
        }
        
        try {
            config.setCreateTime(new Date());
            config.setUpdateTime(new Date());
            // 默认启用
            if (config.getEnabled() == null) {
                config.setEnabled("1");
            }
            
            int result = automationConfigMapper.insertConfig(config);
            logger.info("新增自动化配置成功，protocolId: {}, automationId: {}", 
                       config.getProtocolId(), config.getAutomationId());
            return result;
        } catch (Exception e) {
            logger.error("新增自动化配置失败，protocolId: {}", config.getProtocolId(), e);
            throw new RuntimeException("新增自动化配置失败", e);
        }
    }

    /**
     * 修改自动化配置
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateConfig(IotAutomationConfig config) {
        logger.info("修改自动化配置，automationId: {}", config.getAutomationId());
        
        if (config.getAutomationId() == null) {
            throw new IllegalArgumentException("自动化配置ID不能为空");
        }
        
        try {
            config.setUpdateTime(new Date());
            int result = automationConfigMapper.updateConfig(config);
            logger.info("修改自动化配置成功，automationId: {}", config.getAutomationId());
            return result;
        } catch (Exception e) {
            logger.error("修改自动化配置失败，automationId: {}", config.getAutomationId(), e);
            throw new RuntimeException("修改自动化配置失败", e);
        }
    }

    /**
     * 删除自动化配置
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteConfig(Long automationId) {
        logger.info("删除自动化配置，automationId: {}", automationId);
        
        if (automationId == null) {
            throw new IllegalArgumentException("自动化配置ID不能为空");
        }
        
        try {
            int result = automationConfigMapper.deleteConfig(automationId);
            logger.info("删除自动化配置成功，automationId: {}", automationId);
            return result;
        } catch (Exception e) {
            logger.error("删除自动化配置失败，automationId: {}", automationId, e);
            throw new RuntimeException("删除自动化配置失败", e);
        }
    }

    /**
     * 根据协议ID删除自动化配置
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteConfigByProtocolId(Long protocolId) {
        logger.info("根据协议ID删除自动化配置，protocolId: {}", protocolId);
        
        if (protocolId == null) {
            throw new IllegalArgumentException("设备协议ID不能为空");
        }
        
        try {
            int result = automationConfigMapper.deleteConfigByProtocolId(protocolId);
            logger.info("根据协议ID删除自动化配置成功，protocolId: {}", protocolId);
            return result;
        } catch (Exception e) {
            logger.error("根据协议ID删除自动化配置失败，protocolId: {}", protocolId, e);
            throw new RuntimeException("删除自动化配置失败", e);
        }
    }

    /**
     * 初始化默认自动化配置
     * 为设备创建默认的阈值参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int initDefaultConfig(Long protocolId) {
        logger.info("初始化默认自动化配置，protocolId: {}", protocolId);
        
        if (protocolId == null) {
            throw new IllegalArgumentException("设备协议ID不能为空");
        }
        
        try {
            // 检查是否已存在配置
            IotAutomationConfig existingConfig = automationConfigMapper.selectConfigByProtocolId(protocolId);
            if (existingConfig != null) {
                logger.info("设备已存在自动化配置，无需初始化，protocolId: {}", protocolId);
                return 1;
            }
            
            // 创建默认配置
            IotAutomationConfig defaultConfig = new IotAutomationConfig();
            defaultConfig.setProtocolId(protocolId);
            defaultConfig.setSetTemperature(26.0);      // 默认温度阈值26°C
            defaultConfig.setSetHumidity(65.0);         // 默认湿度阈值65%
            defaultConfig.setSetIlluminance(750.0);     // 默认光照度阈值750lux
            defaultConfig.setSetSoilMoisture(50.0);     // 默认土壤湿度阈值50%
            defaultConfig.setSetCarbonDioxide(500.0);   // 默认二氧化碳阈值500ppm
            defaultConfig.setSetUltravioletRay(6.0);    // 默认紫外线阈值6
            defaultConfig.setEnabled("1");              // 默认启用
            defaultConfig.setCreateTime(new Date());
            defaultConfig.setUpdateTime(new Date());
            
            int result = automationConfigMapper.insertConfig(defaultConfig);
            logger.info("初始化默认自动化配置成功，protocolId: {}", protocolId);
            return result;
        } catch (Exception e) {
            logger.error("初始化默认自动化配置失败，protocolId: {}", protocolId, e);
            throw new RuntimeException("初始化默认配置失败", e);
        }
    }

    /**
     * 根据设备ID查询自动化配置
     */
    @Override
    public IotAutomationConfig getConfigByDeviceId(Long deviceId) {
        logger.debug("根据设备ID查询自动化配置，protocolId: {}", deviceId);
        if (deviceId == null) {
            logger.warn("设备ID为空，无法查询自动化配置");
            return null;
        }

        try {

            IotDevice device = iotDeviceService.getDeviceById(deviceId);
            if (device == null){
                logger.warn("未找到设备ID为 {} 的设备信息", deviceId);
            }

            IotAutomationConfig config = automationConfigMapper.selectConfigByProtocolId(device.getRelayProtocolId());
            if (config == null) {
                logger.debug("未找到协议ID为 {} 的自动化配置", config.getProtocolId());
            } else {
                logger.debug("成功找到协议ID为 {} 的自动化配置", config.getProtocolId());
            }
            return config;
        } catch (Exception e) {
            logger.error("根据设备ID查询自动化配置失败，deviceId: {}", deviceId, e);
            throw new RuntimeException("查询自动化配置失败", e);
        }
    }
}