package com.management.io.mapper;

import com.management.io.domain.IotMeteorologyNode;
import org.apache.ibatis.annotations.Param;
import org.springframework.security.core.parameters.P;

public interface IotMeteorologyNodeMapper {

    /**
     * 根据设备地址查询最新数据
     * @param deviceAddr 设备地址
     * @return
     */
    IotMeteorologyNode selectLatestByDeviceAdr(String deviceAddr);

    /**
     * 添加气象站设备信息
     * @param deviceNode
     * @return
     */
    int insertMeteorologyNode(IotMeteorologyNode deviceNode);

    /**
     * 修改气象站设备信息
     * @param deviceNode
     * @return
     */
    int updateMeteorologyNode(IotMeteorologyNode deviceNode);

    IotMeteorologyNode selectByDeviceAddrAndNodeId(@Param("valueOf") String valueOf,@Param("nodeId") Long nodeId);
}
