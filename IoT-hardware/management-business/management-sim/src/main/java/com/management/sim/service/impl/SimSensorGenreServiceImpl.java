package com.management.sim.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.management.sim.domain.SimSensorGenre;
import com.management.sim.domain.SimVirtualDevice;
import com.management.sim.mapper.SimSensorGenreMapper;
import com.management.sim.mapper.SimVirtualDeviceMapper;
import com.management.sim.service.SimSensorGenreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SimSensorGenreServiceImpl extends ServiceImpl<SimSensorGenreMapper,SimSensorGenre> implements SimSensorGenreService {

    @Autowired
    private SimSensorGenreMapper simSensorCategoryMapper;

    @Autowired
    private SimVirtualDeviceMapper simVirtualDeviceMapper;

    /**
     * 获取所有虚拟仿真传感器类别
     * @return
     */
    @Override
    public List<SimSensorGenre> getAllSimSensorCategory() {
        try {
            return this.list(new LambdaQueryWrapper<SimSensorGenre>()
                    .eq(SimSensorGenre::getStatus, "1"));
        } catch (Exception e) {
            throw new RuntimeException("获取虚拟仿真类型列表失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<SimSensorGenre> getGenreDeviceTree() {
        // 1. 查询所有启用分类
        List<SimSensorGenre> genres = simSensorCategoryMapper.selectList(
                new QueryWrapper<SimSensorGenre>()
                        .eq("status", 1)
                        .orderByAsc("sort_order")
        );

        if (genres.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 查询所有已启用设备
        List<SimVirtualDevice> devices = simVirtualDeviceMapper.selectList(
                new QueryWrapper<SimVirtualDevice>()
                        .eq("status", 1)
                        .orderByAsc("sort")
        );

        // 3. 设备按 genreId 分组
        Map<Long, List<SimVirtualDevice>> deviceMap = devices.stream()
                .collect(Collectors.groupingBy(SimVirtualDevice::getGenreId));

        // 4. 分类转 Map
        Map<Long, SimSensorGenre> genreMap = genres.stream()
                .collect(Collectors.toMap(SimSensorGenre::getSensorId, g -> g));

        // 5. 构建树
        List<SimSensorGenre> rootList = new ArrayList<>();

        for (SimSensorGenre genre : genres) {

            // 挂设备
            genre.setDevices(deviceMap.getOrDefault(genre.getSensorId(), new ArrayList<>()));

            // 父子关系
            if (genre.getParentId() == null || genre.getParentId() == 0) {
                rootList.add(genre);
            } else {
                SimSensorGenre parent = genreMap.get(genre.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(genre);
                }
            }
        }

        return rootList;
    }
}
