package com.management.web.controller.business.iot;

import com.management.common.core.domain.AjaxResult;
import com.management.common.core.page.TableDataInfo;
import com.management.iot.domain.IotDevice;
import com.management.iot.domain.IotSubDeviceType;
import com.management.iot.service.IotSubDeviceTypeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sub_device_type")
public class IotSubDeviceTypeController {

    private static final Logger logger = LoggerFactory.getLogger(IotSubDeviceTypeController.class);

    @Autowired
    private IotSubDeviceTypeService subDeviceTypeService;

    /**
     * 获取子设备类型列表
     * @return 子设备类型列表和操作结果
     */
    @GetMapping("/list")
    public TableDataInfo getAllSubDevicesType(){
        TableDataInfo info = new TableDataInfo();
        try {
            List<IotSubDeviceType> devices = subDeviceTypeService.getAllSubDevicesType();
            info.setCode(200);
            info.setRows(devices);
            info.setMsg("获取子设备类型列表成功");
            info.setTotal(devices.size());
            logger.info("获取子设备类型列表成功，共 {} 个设备", devices.size());
            return info;
        } catch (Exception e) {
            info.setCode(500);
            info.setMsg("获取子设备类型列表失败: " + e.getMessage());
            logger.error("获取子设备类型列表失败", e);
            return info;
        }
    }

    /**
     * 根据子设备类型ID获取设备信息
     * @param subDeviceTypeId 子设备类型ID
     * @return 设备信息和操作结果
     */
    @GetMapping("/info/{subDeviceTypeId}")
    public AjaxResult getSubDeviceTypeInfo(@PathVariable(value = "subDeviceTypeId") Long subDeviceTypeId){
        AjaxResult result = new AjaxResult();
        try {
            if (subDeviceTypeId == null){
                throw new RuntimeException("子设备编号不可为空");
            }
            IotSubDeviceType subDeviceType = subDeviceTypeService.getSubDeviceTypeInfo(subDeviceTypeId);

            result.put("code",200);
            result.put("message","获取子设备类型信息成功");
            result.put("data",subDeviceType);
            return result;
        }catch (Exception e){
            result.put("code",500);
            result.put("message","获取子设备类型信息失败");
            return result;
        }
    }

    /**
     * 添加子设备类型
     * @param subDeviceType 子设备类型
     * @return 操作结果
     */
    @PostMapping("/save")
    public AjaxResult saveSubDeviceType(@RequestBody IotSubDeviceType subDeviceType){
        AjaxResult result = new AjaxResult();
        try {
            if (subDeviceType == null){
                throw new RuntimeException("子设备类型不可为空");
            }
            subDeviceTypeService.saveSubDeviceType(subDeviceType);

            result.put("code",200);
            result.put("message","添加子设备类型成功");
            return result;
        }catch (Exception e){
            result.put("code",500);
            result.put("message","添加子设备类型失败");
            return result;
        }
    }

    /**
     * 添加子设备类型
     * @param subDeviceType 子设备类型
     * @return 操作结果
     */
    @PutMapping("/update")
    public AjaxResult updateSubDeviceType(@RequestBody IotSubDeviceType subDeviceType){
        AjaxResult result = new AjaxResult();
        try {
            if (subDeviceType == null){
                throw new RuntimeException("子设备类型不可为空");
            }
            subDeviceTypeService.updateSubDeviceType(subDeviceType);

            result.put("code",200);
            result.put("message","修改子设备类型成功");
            return result;
        }catch (Exception e){
            result.put("code",500);
            result.put("message","修改子设备类型失败");
            return result;
        }
    }
}
