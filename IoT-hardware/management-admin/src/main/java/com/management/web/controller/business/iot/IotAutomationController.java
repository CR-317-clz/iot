package com.management.web.controller.business.iot;

import com.management.common.core.controller.BaseController;
import com.management.common.core.domain.AjaxResult;
import com.management.common.core.page.TableDataInfo;
import com.management.iot.domain.IotAutomationConfig;
import com.management.iot.domain.IotAutomationRecord;
import com.management.iot.service.IotAutomationConfigService;
import com.management.iot.service.IotAutomationRecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 自动化配置管理控制器
 * 提供自动化配置的增删改查接口
 * @author iot
 */
@RestController
@RequestMapping("/automation")
public class IotAutomationController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(IotAutomationController.class);

    @Autowired
    private IotAutomationConfigService automationConfigService;

    @Autowired
    private IotAutomationRecordService automationRecordService;

    /**
     * 获取所有启用的自动化配置列表
     * 用于前端展示当前启用的自动化规则
     * @return 启用的自动化配置列表
     */
    @GetMapping("/configs/enabled")
    public AjaxResult getEnabledConfigs() {
        logger.info("获取所有启用的自动化配置");
        try {
            List<IotAutomationConfig> configs = automationConfigService.getEnabledConfigs();
            return AjaxResult.success("获取成功", configs);
        } catch (Exception e) {
            logger.error("获取启用的自动化配置失败", e);
            return AjaxResult.error("获取配置失败: " + e.getMessage());
        }
    }

    /**
     * 根据协议ID获取自动化配置
     * 用于查看或编辑特定设备的自动化配置
     * @param protocolId 设备协议ID
     * @return 自动化配置信息
     */
    @GetMapping("/configs/{protocolId}")
    public AjaxResult getConfigByProtocolId(@PathVariable Long protocolId) {
        logger.info("根据协议ID获取自动化配置，protocolId: {}", protocolId);
        
        if (protocolId == null) {
            return AjaxResult.error("协议ID不能为空");
        }
        
        try {
            IotAutomationConfig config = automationConfigService.getConfigByProtocolId(protocolId);
            if (config == null) {
                return AjaxResult.error("未找到该设备的自动化配置");
            }
            return AjaxResult.success("获取成功", config);
        } catch (Exception e) {
            logger.error("根据协议ID获取自动化配置失败，protocolId: {}", protocolId, e);
            return AjaxResult.error("获取配置失败: " + e.getMessage());
        }
    }

    /**
     * 新增自动化配置
     * 第一次传参时调用此接口保存配置
     * @param config 自动化配置对象
     * @return 操作结果
     */
    @PostMapping("/configs")
    public AjaxResult addConfig(@RequestBody IotAutomationConfig config) {
        logger.info("新增自动化配置，protocolId: {}", config.getProtocolId());
        
        // 参数校验
        if (config.getProtocolId() == null) {
            return AjaxResult.error("设备协议ID不能为空");
        }
        
        try {
            int result = automationConfigService.addConfig(config);
            if (result > 0) {
                logger.info("新增自动化配置成功，protocolId: {}", config.getProtocolId());
                return AjaxResult.success("新增配置成功");
            } else {
                logger.warn("新增自动化配置失败，protocolId: {}", config.getProtocolId());
                return AjaxResult.error("新增配置失败");
            }
        } catch (Exception e) {
            logger.error("新增自动化配置异常，protocolId: {}", config.getProtocolId(), e);
            return AjaxResult.error("新增配置失败: " + e.getMessage());
        }
    }

    /**
     * 修改自动化配置
     * 用于更新已存在的自动化配置
     * @param config 自动化配置对象
     * @return 操作结果
     */
    @PutMapping("/configs")
    public AjaxResult updateConfig(@RequestBody IotAutomationConfig config) {
        logger.info("修改自动化配置，automationId: {}", config.getAutomationId());
        
        if (config.getAutomationId() == null) {
            return AjaxResult.error("自动化配置ID不能为空");
        }
        
        try {
            int result = automationConfigService.updateConfig(config);
            if (result > 0) {
                logger.info("修改自动化配置成功，automationId: {}", config.getAutomationId());
                return AjaxResult.success("修改配置成功");
            } else {
                logger.warn("修改自动化配置失败，automationId: {}", config.getAutomationId());
                return AjaxResult.error("修改配置失败");
            }
        } catch (Exception e) {
            logger.error("修改自动化配置异常，automationId: {}", config.getAutomationId(), e);
            return AjaxResult.error("修改配置失败: " + e.getMessage());
        }
    }

    /**
     * 保存或更新自动化配置（智能保存）
     * 第一次传参时新增，后续传参时更新
     * 这是推荐使用的方法，自动处理配置的保存逻辑
     * @param config 自动化配置对象
     * @return 操作结果
     */
    @PostMapping("/configs/saveOrUpdate")
    public AjaxResult saveOrUpdateConfig(@RequestBody IotAutomationConfig config) {
        logger.info("保存或更新自动化配置，protocolId: {}", config.getProtocolId());
        
        if (config.getProtocolId() == null) {
            return AjaxResult.error("设备协议ID不能为空");
        }
        
        try {
            int result = automationConfigService.saveOrUpdateConfig(config);
            if (result > 0) {
                logger.info("保存自动化配置成功，protocolId: {}", config.getProtocolId());
                return AjaxResult.success("保存配置成功");
            } else {
                logger.warn("保存自动化配置失败，protocolId: {}", config.getProtocolId());
                return AjaxResult.error("保存配置失败");
            }
        } catch (Exception e) {
            logger.error("保存自动化配置异常，protocolId: {}", config.getProtocolId(), e);
            return AjaxResult.error("保存配置失败: " + e.getMessage());
        }
    }

    /**
     * 删除自动化配置
     * @param automationId 自动化配置ID
     * @return 操作结果
     */
    @DeleteMapping("/configs/{automationId}")
    public AjaxResult deleteConfig(@PathVariable Long automationId) {
        logger.info("删除自动化配置，automationId: {}", automationId);
        
        if (automationId == null) {
            return AjaxResult.error("自动化配置ID不能为空");
        }
        
        try {
            int result = automationConfigService.deleteConfig(automationId);
            if (result > 0) {
                logger.info("删除自动化配置成功，automationId: {}", automationId);
                return AjaxResult.success("删除配置成功");
            } else {
                logger.warn("删除自动化配置失败，automationId: {}", automationId);
                return AjaxResult.error("删除配置失败");
            }
        } catch (Exception e) {
            logger.error("删除自动化配置异常，automationId: {}", automationId, e);
            return AjaxResult.error("删除配置失败: " + e.getMessage());
        }
    }

    /**
     * 初始化默认自动化配置
     * 为新设备创建默认的自动化配置
     * @param protocolId 设备协议ID
     * @return 操作结果
     */
    @PostMapping("/configs/init/{protocolId}")
    public AjaxResult initDefaultConfig(@PathVariable Long protocolId) {
        logger.info("初始化默认自动化配置，protocolId: {}", protocolId);
        
        if (protocolId == null) {
            return AjaxResult.error("设备协议ID不能为空");
        }
        
        try {
            int result = automationConfigService.initDefaultConfig(protocolId);
            if (result > 0) {
                logger.info("初始化默认自动化配置成功，protocolId: {}", protocolId);
                return AjaxResult.success("初始化配置成功");
            } else {
                logger.warn("初始化默认自动化配置失败，protocolId: {}", protocolId);
                return AjaxResult.error("初始化配置失败");
            }
        } catch (Exception e) {
            logger.error("初始化默认自动化配置异常，protocolId: {}", protocolId, e);
            return AjaxResult.error("初始化配置失败: " + e.getMessage());
        }
    }

    /**
     * 获取自动化记录列表（分页）
     * 支持按条件查询操作历史记录
     * @param record 查询条件
     * @return 分页数据
     */
    @GetMapping("/records")
    public TableDataInfo getRecordList(IotAutomationRecord record) {
        logger.info("获取自动化记录列表，protocolId: {}, controlType: {}", 
                   record.getProtocolId(), record.getControlType());
        
        try {
            startPage(); // 开始分页
            List<IotAutomationRecord> records = automationRecordService.getRecordList(record);
            return getDataTable(records); // 返回分页数据
        } catch (Exception e) {
            logger.error("获取自动化记录列表失败", e);
            return getDataTable(null);
        }
    }

    /**
     * 根据ID获取自动化记录详情
     * @param recordId 记录ID
     * @return 记录详情
     */
    @GetMapping("/records/{recordId}")
    public AjaxResult getRecordById(@PathVariable Long recordId) {
        logger.info("根据ID获取自动化记录详情，recordId: {}", recordId);
        
        if (recordId == null) {
            return AjaxResult.error("记录ID不能为空");
        }
        
        try {
            IotAutomationRecord record = automationRecordService.getRecordById(recordId);
            if (record == null) {
                return AjaxResult.error("未找到该记录");
            }
            return AjaxResult.success("获取成功", record);
        } catch (Exception e) {
            logger.error("获取自动化记录详情失败，recordId: {}", recordId, e);
            return AjaxResult.error("获取记录失败: " + e.getMessage());
        }
    }

    /**
     * 根据协议ID获取最新记录
     * 用于获取设备最近的操作状态
     * @param protocolId 协议ID
     * @return 最新记录
     */
    @GetMapping("/records/latest/{protocolId}")
    public AjaxResult getLatestRecordByProtocolId(@PathVariable Long protocolId) {
        logger.info("获取设备最新记录，protocolId: {}", protocolId);
        
        if (protocolId == null) {
            return AjaxResult.error("协议ID不能为空");
        }
        
        try {
            IotAutomationRecord record = automationRecordService.getLatestRecordByProtocolId(protocolId);
            return AjaxResult.success("获取成功", record);
        } catch (Exception e) {
            logger.error("获取最新记录失败，protocolId: {}", protocolId, e);
            return AjaxResult.error("获取最新记录失败: " + e.getMessage());
        }
    }

    /**
     * 根据设备ID获取自动化配置
     * @param deviceId 设备ID
     * @return 配置详情
     */
    @GetMapping("/configs_device_id/{deviceId}")
    public AjaxResult getConfigByDeviceId(@PathVariable Long deviceId) {

        logger.info("根据设备ID获取自动化配置，protocolId: {}", deviceId);

        if (deviceId == null) {
            return AjaxResult.error("设备ID不能为空");
        }

        try {
            IotAutomationConfig config = automationConfigService.getConfigByDeviceId(deviceId);
            if (config == null) {
                return AjaxResult.error("未找到该设备的自动化配置");
            }
            return AjaxResult.success("获取成功", config);
        } catch (Exception e) {
            logger.error("根据协议ID获取自动化配置失败，protocolId: {}", deviceId, e);
            return AjaxResult.error("获取配置失败: " + e.getMessage());
        }
    }
}