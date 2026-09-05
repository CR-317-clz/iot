package com.management.io.service;

import com.management.io.domain.IotMeteorologyNode;
import org.apache.ibatis.annotations.Param;

public interface IotMeteorologyNodeService {

    /**
     * 根据设备地址查询设备信息
     * @param deviceAddr 设备地址
     * @return
     */
    IotMeteorologyNode selectByNodeId(String deviceAddr);

    /**
     * 新增设备信息
     * @param deviceNode
     * @return
     */
    boolean insert(IotMeteorologyNode deviceNode);

    /**
     * 修改设备信息
     * @param deviceNode
     */
    boolean update(IotMeteorologyNode deviceNode);

    /**
     * 根据设备地址和节点ID查询设备信息
     * @param valueOf
     * @param nodeId
     * @return
     */
    IotMeteorologyNode selectByDeviceAddrAndNodeId(String valueOf,Long nodeId);
}
