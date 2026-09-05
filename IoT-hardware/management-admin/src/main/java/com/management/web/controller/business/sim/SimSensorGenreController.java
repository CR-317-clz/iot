package com.management.web.controller.business.sim;

import com.management.common.core.domain.AjaxResult;
import com.management.common.core.page.TableDataInfo;
import com.management.common.utils.snowFlake.SnowflakeIdGenerator;
import com.management.sim.domain.SimSensorGenre;
import com.management.sim.service.SimSensorGenreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/sensor/genre")
public class SimSensorGenreController {

    private static final Logger logger = LoggerFactory.getLogger(SimSensorGenreController.class);

    @Autowired
    private SimSensorGenreService simSensorCategoryService;

    /**
     * 获取虚拟仿真类型列表
     *
     * @return
     */
    @GetMapping("/list")
    public TableDataInfo getAllDevices() {
        TableDataInfo info = new TableDataInfo();
        try {
            List<SimSensorGenre> devices = simSensorCategoryService.getAllSimSensorCategory();
            info.setCode(200);
            info.setRows(devices);
            info.setMsg("获取虚拟仿真类型列表成功");
            info.setTotal(devices.size());
            logger.info("获取虚拟仿真类型列表成功，共 {} 个类型", devices.size());
            return info;
        } catch (Exception e) {
            info.setCode(500);
            info.setMsg("获取虚拟仿真类型列表失败: " + e.getMessage());
            logger.error("获取虚拟仿真类型列表失败", e);
            return info;
        }
    }

    @GetMapping("/sensor_category_tree")
    public List<SimSensorGenre> getTree() {
        return simSensorCategoryService.getGenreDeviceTree();
    }

    /**
     * 新增虚拟仿真类型
     *
     * @param sensorGenre
     * @return
     */
    @PostMapping("/save")
    public AjaxResult saveSimSensorGenre(@RequestBody SimSensorGenre sensorGenre) {
        // 参数校验
        if (sensorGenre.getSensorCode() == null) {
            return AjaxResult.error("分类编码不能为空");
        }

        try {
            if (sensorGenre.getParentId() == null){
                sensorGenre.setParentId(0L);
            }
            sensorGenre.setSensorId(SnowflakeIdGenerator.nextId());
            sensorGenre.setCreateTime(new Date());
            boolean result = simSensorCategoryService.save(sensorGenre);
            if (result == true) {
                logger.info("新增虚拟仿真类型成功，sensorCode: {}", sensorGenre.getSensorCode());
                return AjaxResult.success("新增虚拟仿真类型成功");
            } else {
                logger.warn("新增虚拟仿真类置失败，sensorCode: {}", sensorGenre.getSensorCode());
                return AjaxResult.error("新增虚拟仿真类型失败");
            }
        } catch (Exception e) {
            logger.error("新增虚拟仿真类型异常，sensorCode: {}", sensorGenre.getSensorCode(), e);
            return AjaxResult.error("新增虚拟仿真类型失败: " + e.getMessage());
        }
    }

    /**
     * 修改虚拟仿真类型
     *
     * @param sensorGenre
     * @return
     */
    @PutMapping("/update")
    public AjaxResult updateSimSensorGenre(@RequestBody SimSensorGenre sensorGenre) {
        try {
            boolean result = simSensorCategoryService.updateById(sensorGenre);
            if (result == true) {
                logger.info("修改虚拟仿真类型成功，sensorCode: {}", sensorGenre.getSensorCode());
                return AjaxResult.success("修改虚拟仿真类型成功");
            } else {
                logger.warn("修改虚拟仿真类置失败，sensorCode: {}", sensorGenre.getSensorCode());
                return AjaxResult.error("修改虚拟仿真类型失败");
            }

        } catch (Exception e) {
            logger.error("修改虚拟仿真类型异常，sensorCode: {}", sensorGenre.getSensorCode(), e);
            return AjaxResult.error("修改虚拟仿真类型失败: " + e.getMessage());
        }
    }

    /**
     * 删除虚拟仿真类型
     *
     * @param sensorId
     * @return
     */
    @DeleteMapping("/delete/{sensorId}")
    public AjaxResult deleteSimSensorGenre(@PathVariable Long sensorId) {
        try {
            boolean result = simSensorCategoryService.removeById(sensorId);
            if (result == true) {
                logger.info("删除虚拟仿真类型成功，sensorId: {}", sensorId);
                return AjaxResult.success("删除虚拟仿真类型成功");
            } else {
                logger.warn("删除虚拟仿真类置失败，sensorId: {}", sensorId);
                return AjaxResult.error("删除虚拟仿真类型失败");
            }
        } catch (Exception e) {
            logger.error("删除虚拟仿真类型异常，sensorId: {}", sensorId, e);
            return AjaxResult.error("删除虚拟仿真类型失败: " + e.getMessage());
        }
    }
}
