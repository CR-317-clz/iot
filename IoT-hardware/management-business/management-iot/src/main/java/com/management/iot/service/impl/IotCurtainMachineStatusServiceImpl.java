package com.management.iot.service.impl;

import com.management.iot.domain.IotCurtainMachineStatus;
import com.management.iot.domain.IotDevice;
import com.management.iot.mapper.IotCurtainMachineStatusMapper;
import com.management.iot.service.IotCurtainMachineStatusService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class IotCurtainMachineStatusServiceImpl implements IotCurtainMachineStatusService {

    private static final Logger logger = LoggerFactory.getLogger(IotCurtainMachineStatusServiceImpl.class);

    @Autowired
    private IotCurtainMachineStatusMapper iotCurtainMachineStatusMapper;

    @Override
    public boolean addCurtainMachineStatus(IotCurtainMachineStatus iotCurtainMachineStatus) {
        if (iotCurtainMachineStatus == null) {
            throw new IllegalArgumentException("窗帘机信息不能为空");
        }
        if (iotCurtainMachineStatus.getProtocolId() == null || iotCurtainMachineStatus.getProtocolId() <= 0) {
            throw new IllegalArgumentException("协议ID不能为空且必须大于0");
        }

        try {
            int result = iotCurtainMachineStatusMapper.insertCurtainMachineStatusService(iotCurtainMachineStatus);
            boolean success = result > 0;

            if (success) {
                logger.info("成功保存消息记录，协议ID: {}", iotCurtainMachineStatus.getProtocolId());
            }

            return success;
        } catch (Exception e) {
            logger.error("保存消息记录失败，协议ID: {}", iotCurtainMachineStatus.getProtocolId(), e);
            throw new RuntimeException("保存窗帘机信息失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据协议ID查询窗帘机信息
     *
     * @param protocolId 协议ID
     * @return 窗帘机信息
     */
    @Override
    public IotCurtainMachineStatus getCurtainMachineStatusByProtocol(Long protocolId) {
        try {
            IotCurtainMachineStatus curtainMachineStatus = iotCurtainMachineStatusMapper.getCurtainMachineStatusByProtocol(protocolId);

            return curtainMachineStatus;
        } catch (Exception e) {
            logger.error("查询窗帘机消息记录失败");
            throw new RuntimeException("查询窗帘机消息记录失败: " + e.getMessage(), e);
        }
    }
}
