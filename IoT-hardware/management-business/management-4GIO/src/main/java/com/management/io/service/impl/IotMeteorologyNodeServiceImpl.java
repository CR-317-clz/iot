package com.management.io.service.impl;

import com.management.common.utils.snowFlake.SnowflakeIdGenerator;
import com.management.io.domain.IotMeteorologyNode;
import com.management.io.domain.IotMeteorologyRelayStatus;
import com.management.io.mapper.IotMeteorologyNodeMapper;
import com.management.io.service.IotMeteorologyNodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IotMeteorologyNodeServiceImpl implements IotMeteorologyNodeService {

    @Autowired
    private IotMeteorologyNodeMapper iotMeteorologyNodeMapper;

    @Override
    public IotMeteorologyNode selectByNodeId(String deviceAddr) {
        if (deviceAddr == null) {
            throw new IllegalArgumentException("设备地址不能为空");
        }
        try {
            IotMeteorologyNode node = iotMeteorologyNodeMapper.selectLatestByDeviceAdr(deviceAddr);

            return node;
        } catch (Exception e) {
            throw new RuntimeException("获取气象站设备信息失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean insert(IotMeteorologyNode deviceNode) {
        try {
            deviceNode.setNodeId(SnowflakeIdGenerator.nextId());
            int result = iotMeteorologyNodeMapper.insertMeteorologyNode(deviceNode);
            return result > 0;
        } catch (Exception e) {
            throw new RuntimeException("添加气象站设备失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(IotMeteorologyNode deviceNode) {
        try {
            int result = iotMeteorologyNodeMapper.updateMeteorologyNode(deviceNode);
            return result > 0;
        } catch (Exception e) {
            throw new RuntimeException("添加气象站设备失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据设备地址和节点ID查询设备信息
     * @param valueOf
     * @param nodeId
     * @return
     */
    @Override
    public IotMeteorologyNode selectByDeviceAddrAndNodeId(String valueOf, Long nodeId) {

        System.out.println("valueOf: " + valueOf);
        System.out.println("nodeId: " + nodeId);

        return iotMeteorologyNodeMapper.selectByDeviceAddrAndNodeId(valueOf, nodeId);
    }
}
