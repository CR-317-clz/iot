package com.management.web.controller.business.sim;

import com.alibaba.fastjson2.JSON;
import com.management.common.core.domain.AjaxResult;
import com.management.common.core.page.TableDataInfo;
import com.management.common.utils.snowFlake.SnowflakeIdGenerator;
import com.management.sim.domain.SimSensorGenre;
import com.management.sim.domain.SimVirtualDevice;
import com.management.sim.domain.dto.VirtualDeviceThresholdDto;
import com.management.sim.service.SimVirtualDeviceService;
import com.management.sim.utils.SensorThresholdValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/virtual_device")
public class SimVirtualDeviceController {

    private static final Logger logger = LoggerFactory.getLogger(SimVirtualDeviceController.class);

    @Autowired
    private SimVirtualDeviceService simVirtualDeviceService;

    /**
     * 获取虚拟仿真设备列表
     *
     * @return
     */
    @GetMapping("/list")
    public TableDataInfo list() {
        TableDataInfo info = new TableDataInfo();
        try {
            List<SimVirtualDevice> devices = simVirtualDeviceService.getAllSimVirtualDevice();
            info.setCode(200);
            info.setRows(devices);
            info.setMsg("获取虚拟仿真设备列表成功");
            info.setTotal(devices.size());
            logger.info("获取虚拟仿真设备列表成功，共 {} 个设备", devices.size());
            return info;
        } catch (Exception e) {
            info.setCode(500);
            info.setMsg("获取虚拟仿真设备列表失败: " + e.getMessage());
            logger.error("获取虚拟仿真设备列表失败", e);
            return info;
        }
    }

    /**
     * 新增虚拟仿真设备
     *
     * @param device
     * @return
     */
    @PostMapping("/save")
    public AjaxResult save(@RequestBody SimVirtualDevice device) {
        // 参数校验
        if (device.getVirtualCode() == null) {
            return AjaxResult.error("虚拟设备编码不能为空");
        }

        try {

            // 阈值校验逻辑
            List<VirtualDeviceThresholdDto> threshold = device.getThreshold();

            // 检查threshold是否为null（根据业务需求决定是否允许为空）
            if (threshold != null) {
                List<String> validationErrors = new ArrayList<>();

                for (VirtualDeviceThresholdDto virtualDeviceThresholdDto : threshold) {
                    // 快速判断是否正确
                    boolean isValid = SensorThresholdValidator.isThresholdValid(
                            virtualDeviceThresholdDto.getThresholdName(),
                            virtualDeviceThresholdDto.getThresholdNum()
                    );

                    if (isValid) {
                        // 数据正确
                        logger.info("阈值校验通过 - 名称: {}, 值: {}",
                                virtualDeviceThresholdDto.getThresholdName(),
                                virtualDeviceThresholdDto.getThresholdNum());
                    } else {
                        // 数据不正确
                        String errorInfo = SensorThresholdValidator.getValidationInfo(
                                virtualDeviceThresholdDto.getThresholdName(),
                                virtualDeviceThresholdDto.getThresholdNum()
                        );
                        logger.error("阈值校验失败: {}", errorInfo);
                        validationErrors.add(errorInfo);
                    }
                }

                // 如果有校验失败的错误，直接返回
                if (!validationErrors.isEmpty()) {
                    return AjaxResult.error("阈值校验失败: " + String.join("; ", validationErrors));
                }
            } else {
                logger.info("设备阈值列表为空，跳过阈值校验");
            }

            device.setVirtualId(SnowflakeIdGenerator.nextId());
            device.setCreateTime(new Date());
            boolean result = simVirtualDeviceService.save(device);
            if (result == true) {
                logger.info("新增虚拟仿真设备成功，virtualCode: {}", device.getVirtualCode());
                return AjaxResult.success("新增虚拟仿真类型成功");
            } else {
                logger.warn("新增虚拟仿真设备失败，virtualCode: {}", device.getVirtualCode());
                return AjaxResult.error("新增虚拟仿真设备失败");
            }
        } catch (Exception e) {
            logger.error("新增虚拟仿真设备异常，virtualCode: {}", device.getVirtualCode(), e);
            return AjaxResult.error("新增虚拟仿真设备异常: " + e.getMessage());
        }
    }

    /**
     * 修改虚拟仿真设备
     *
     * @param device
     * @return
     */
    @PutMapping("/update")
    public AjaxResult update(@RequestBody SimVirtualDevice device) {
        // 增加详细的调试日志
        logger.info("开始修改虚拟设备，接收到的数据：{}", device);

        // 参数校验
        if (device == null) {
            logger.error("修改设备失败：设备对象为null");
            return AjaxResult.error("虚拟设备不能为空");
        }

        logger.info("设备ID: {}, 设备编码: {}", device.getVirtualId(), device.getVirtualCode());

        if (device.getVirtualCode() == null || device.getVirtualCode().trim().isEmpty()) {
            logger.error("修改设备失败：设备编码为空");
            return AjaxResult.error("虚拟设备编码不能为空");
        }

        if (device.getVirtualId() == null) {
            logger.error("修改设备失败：设备ID为空");
            return AjaxResult.error("设备ID不能为空");
        }

        try {


            // 阈值校验逻辑
            List<VirtualDeviceThresholdDto> threshold = device.getThreshold();

            // 检查threshold是否为null（根据业务需求决定是否允许为空）
            if (threshold != null) {
                List<String> validationErrors = new ArrayList<>();

                for (VirtualDeviceThresholdDto virtualDeviceThresholdDto : threshold) {
                    // 快速判断是否正确
                    boolean isValid = SensorThresholdValidator.isThresholdValid(
                            virtualDeviceThresholdDto.getThresholdName(),
                            virtualDeviceThresholdDto.getThresholdNum()
                    );

                    if (isValid) {
                        // 数据正确
                        logger.info("阈值校验通过 - 名称: {}, 值: {}",
                                virtualDeviceThresholdDto.getThresholdName(),
                                virtualDeviceThresholdDto.getThresholdNum());
                    } else {
                        // 数据不正确
                        String errorInfo = SensorThresholdValidator.getValidationInfo(
                                virtualDeviceThresholdDto.getThresholdName(),
                                virtualDeviceThresholdDto.getThresholdNum()
                        );
                        logger.error("阈值校验失败: {}", errorInfo);
                        validationErrors.add(errorInfo);
                    }
                }

                // 如果有校验失败的错误，直接返回
                if (!validationErrors.isEmpty()) {
                    return AjaxResult.error("阈值校验失败: " + String.join("; ", validationErrors));
                }
            } else {
                logger.info("设备阈值列表为空，跳过阈值校验");
            }


            // 准备更新数据
            device.setUpdateTime(new Date());

            // 记录更新前的数据状态
            logger.info("更新后数据: {}", device);

            // 尝试更新设备
            logger.info("开始更新设备到数据库...");
            boolean result = simVirtualDeviceService.updateBySimVirtualDevice(device);
            if (result == true) {
                logger.info("修改虚拟仿真设备成功，virtualCode: {}", device.getVirtualCode());
                return AjaxResult.success("修改虚拟仿真类型成功");
            } else {
                logger.warn("修改虚拟仿真设备失败，virtualCode: {}", device.getVirtualCode());
                return AjaxResult.error("修改虚拟仿真设备失败");
            }
        } catch (DataIntegrityViolationException e) {
            logger.error("数据完整性异常，virtualCode: {}, 异常信息: {}",
                    device.getVirtualCode(), e.getMessage(), e);
            return AjaxResult.error("数据保存失败，请检查数据是否合法或重复");
        } catch (Exception e) {
            logger.error("修改虚拟仿真设备异常，virtualCode: {}, 异常信息: {}",
                    device.getVirtualCode(), e.getMessage(), e);
            return AjaxResult.error("修改虚拟仿真设备异常: " + e.getMessage());
        }
    }

    /**
     * 删除虚拟仿真设备
     *
     * @param virtualId
     * @return
     */
    @DeleteMapping("/delete/{virtualId}")
    public AjaxResult delete(@PathVariable("virtualId") Long virtualId) {
        try {
            boolean result = simVirtualDeviceService.removeByVirtualId(virtualId);
            if (result == true) {
                logger.info("删除虚拟仿真设备成功，virtualId: {}", virtualId);
                return AjaxResult.success("删除虚拟仿真设备成功");
            } else {
                logger.warn("删除虚拟仿真设备失败，virtualId: {}", virtualId);
                return AjaxResult.error("删除虚拟仿真设备失败");
            }
        } catch (Exception e) {
            logger.error("删除虚拟仿真设备异常，virtualId: {}", virtualId, e);
            return AjaxResult.error("删除虚拟仿真设备异常: " + e.getMessage());
        }
    }
}
