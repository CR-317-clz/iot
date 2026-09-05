package com.management.iot.service.impl;

import com.alibaba.fastjson2.JSON;
import com.management.camera.domain.IotCameraInfo;
import com.management.common.core.domain.AjaxResult;
import com.management.common.utils.snowFlake.SnowflakeIdGenerator;
import com.management.io.domain.IotMeteorologyDevice;
import com.management.io.domain.IotMeteorologyRegisterData;
import com.management.iot.configuration.tcp.TcpClientManager;
import com.management.iot.domain.*;
import com.management.iot.domain.unity.UnityDto;
import com.management.iot.mapper.IotRelayDeviceStatusMapper;
import com.management.iot.service.IotCurtainMachineStatusService;
import com.management.iot.service.IotDeviceService;
import com.management.iot.service.IotRelayDeviceStatusService;
import org.hibernate.validator.internal.util.stereotypes.Lazy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class IotRelayDeviceStatusServiceImpl implements IotRelayDeviceStatusService {

    private static final Logger logger = LoggerFactory.getLogger(IotRelayDeviceStatusServiceImpl.class);

    @Autowired
    private IotRelayDeviceStatusMapper iotRelayDeviceStatusMapper;

    @Autowired
    private IotCurtainMachineStatusService iotCurtainMachineStatusService;

    @Autowired
    private IotDeviceService iotDeviceService;

    private TcpClientManager tcpClientManager;

    @Override
    public boolean insert(IotRelayDeviceStatus deviceStatus) {
        if (deviceStatus == null) {
            throw new IllegalArgumentException("继电器信息不能为空");
        }

        try {

            deviceStatus.setRelayId(SnowflakeIdGenerator.nextId());
            int result = iotRelayDeviceStatusMapper.insertRelayDeviceStatus(deviceStatus);
            return result > 0;
        } catch (Exception e) {
            throw new RuntimeException("添加继电器信息失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据协议ID查询继电器信息
     * @param deviceId
     * @return
     */
    @Override
    public List<IotRelayDeviceStatus> getRelayDeviceStatusByProtocol(Long deviceId) {
        if (deviceId == null) {
            throw new IllegalArgumentException("设备ID不能为空");
        }
        try {

            IotDevice device = iotDeviceService.getDeviceById(deviceId);
            if (device == null){
                throw new IllegalArgumentException("设备不存在");
            }

            // 获取设备下的窗帘数据
            IotCurtainMachineStatus iotCurtainMachineStatus = iotCurtainMachineStatusService.getCurtainMachineStatusByProtocol(device.getProtocolId());

            // 获取设备下的继电器数据
            logger.info("开始发送 TCP 命令到协议: {}", device.getRelayProtocolId());

            // 使用新的等待机制，确保第一次请求获取最新数据
            List<IotRelayDeviceStatus> relayDeviceStatuses =
                    tcpClientManager.sendMessageAndWaitResponse(device.getRelayProtocolId(), "qa");

            // 创建新的继电器状态对象，将窗帘数据添加进去
            IotRelayDeviceStatus curtainAsRelay = new IotRelayDeviceStatus();
            curtainAsRelay.setRelayId(iotCurtainMachineStatus.getCurtainMachineId());
            curtainAsRelay.setStatus(iotCurtainMachineStatus.getStatus());
            curtainAsRelay.setProtocolId(iotCurtainMachineStatus.getProtocolId());
            curtainAsRelay.setCreateTime(iotCurtainMachineStatus.getCreateTime());
            curtainAsRelay.setRelayDeviceName(iotCurtainMachineStatus.getCurtainMachineName());

            relayDeviceStatuses.add(curtainAsRelay);

            return relayDeviceStatuses;
        } catch (Exception e) {
            throw new RuntimeException("获取继电器信息失败: " + e.getMessage(), e);
        }
    }
}
