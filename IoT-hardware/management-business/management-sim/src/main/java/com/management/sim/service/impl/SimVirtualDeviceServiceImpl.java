package com.management.sim.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.management.sim.domain.SimSensorGenre;
import com.management.sim.domain.SimVirtualDevice;
import com.management.sim.mapper.SimVirtualDeviceMapper;
import com.management.sim.service.SimVirtualDeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class SimVirtualDeviceServiceImpl extends ServiceImpl<SimVirtualDeviceMapper, SimVirtualDevice> implements SimVirtualDeviceService {

    @Autowired
    private SimVirtualDeviceMapper simVirtualDeviceMapper;

    /**
     * 获取所有虚拟设备
     *
     * @return
     */
    @Override
    public List<SimVirtualDevice> getAllSimVirtualDevice() {
            try {
                List<SimVirtualDevice> virtualDevice = simVirtualDeviceMapper.getAllSimVirtualDevice();

                System.out.println("virtualDevice: " + virtualDevice);

                return virtualDevice;
            } catch (Exception e) {
                throw new RuntimeException("获取虚拟仿真类型列表失败: " + e.getMessage(), e);
            }


    }

    /**
     * 修改虚拟仿真设备
     *
     * @param device
     * @return
     */
    @Override
    public boolean updateBySimVirtualDevice(SimVirtualDevice device) {
        if (device == null || device.getVirtualId() == null) {
            throw new IllegalArgumentException("虚拟设备ID不能为空");
        }

        try {
            UpdateWrapper<SimVirtualDevice> wrapper = new UpdateWrapper<>();
            wrapper.eq("virtual_id", device.getVirtualId());

            boolean hasUpdate = false;

            if (device.getVirtualName() != null) {
                wrapper.set("virtual_name", device.getVirtualName());
                hasUpdate = true;
            }

            if (device.getVirtualCode() != null) {
                wrapper.set("virtual_code", device.getVirtualCode());
                hasUpdate = true;
            }

            if (device.getGenreId() != null) {
                wrapper.set("genre_id", device.getGenreId());
                hasUpdate = true;
            }

            if (device.getVoltage() != null) {
                wrapper.set("voltage", device.getVoltage());
                hasUpdate = true;
            }

            if (device.getAgreement() != null) {
                wrapper.set("agreement", device.getAgreement());
                hasUpdate = true;
            }

            if (device.getThreshold() != null) {
                wrapper.set(
                        "threshold",
                        JSON.toJSONString(device.getThreshold())
                );
                hasUpdate = true;
            }

            if (device.getImages() != null) {
                wrapper.set("images", device.getImages());
                hasUpdate = true;
            }

            /**
             * ⭐ 关键修复点：JSON 字段必须转 String
             */
            if (device.getPin() != null) {
                wrapper.set(
                        "pin",
                        JSON.toJSONString(device.getPin())
                );
                hasUpdate = true;
            }

            if (device.getStatus() != null) {
                wrapper.set("status", device.getStatus());
                hasUpdate = true;
            }

            if (device.getSort() != null) {
                wrapper.set("sort", device.getSort());
                hasUpdate = true;
            }

            if (!hasUpdate) {
                return false;
            }

            wrapper.set("update_time", new Date());

            return this.update(wrapper);

        } catch (Exception e) {
            throw new RuntimeException("修改虚拟仿真类型失败", e);
        }
    }

    @Override
    public boolean removeByVirtualId(Long virtualId) {

        try {
            int i = simVirtualDeviceMapper.removeByVirtualId(virtualId);
            return i > 0;
        }catch (Exception e){
            throw new RuntimeException("删除虚拟仿真设备失败", e);
        }
    }

    @Override
    public SimVirtualDevice getByVirtualCode(String virtualCode) {
        try {
            LambdaQueryWrapper<SimVirtualDevice> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SimVirtualDevice::getVirtualCode, virtualCode);
            return this.getOne(wrapper);
        } catch (Exception e) {
            throw new RuntimeException("获取虚拟仿真设备失败", e);
        }
    }

}
