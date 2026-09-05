package com.management.web.controller.business.iot;

import com.management.common.core.domain.AjaxResult;
import com.management.common.core.page.TableDataInfo;
import com.management.iot.configuration.tcp.TcpClientManager;
import com.management.iot.domain.IotCurtainMachineStatus;
import com.management.iot.domain.IotDevice;
import com.management.iot.domain.IotRelayDeviceStatus;
import com.management.iot.domain.unity.UnityDto;
import com.management.iot.service.AutomationControlService;
import com.management.iot.service.IotCurtainMachineStatusService;
import com.management.iot.service.IotDeviceService;
import com.management.iot.service.IotRelayDeviceStatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/iot/relay_device_status")
public class IotRelayDeviceStatusController {

    private static final Logger logger = LoggerFactory.getLogger(IotRelayDeviceStatusController.class);

    @Autowired
    private IotRelayDeviceStatusService iotRelayDeviceStatusService;

    @Autowired
    private AutomationControlService automationControlService;

    @Autowired
    private IotDeviceService iotDeviceService;

    @Autowired
    private IotCurtainMachineStatusService iotCurtainMachineStatusService;

    @Autowired
    private TcpClientManager tcpClientManager;


    /**
     * 根据协议ID获取设备列表
     * @param protocolId 协议ID
     * @return 设备列表和操作结果
     */
    @GetMapping("/device_protocol/{deviceId}")
    public AjaxResult getDeviceByProtocolId(@PathVariable("deviceId") Long deviceId) {
        AjaxResult result = new AjaxResult();
        try {
            UnityDto dto = new UnityDto();
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
            // 窗帘记录可能为空（设备无窗帘机时），需做空值保护，避免 NPE 导致整个状态接口 500
            if (iotCurtainMachineStatus != null) {
                IotRelayDeviceStatus curtainAsRelay = new IotRelayDeviceStatus();
                curtainAsRelay.setRelayId(iotCurtainMachineStatus.getCurtainMachineId());
                curtainAsRelay.setStatus(iotCurtainMachineStatus.getStatus());
                curtainAsRelay.setProtocolId(iotCurtainMachineStatus.getProtocolId());
                curtainAsRelay.setCreateTime(iotCurtainMachineStatus.getCreateTime());
                curtainAsRelay.setRelayDeviceName(iotCurtainMachineStatus.getCurtainMachineName());
                relayDeviceStatuses.add(curtainAsRelay);
            } else {
                logger.warn("设备 {} 暂无窗帘机状态记录，已跳过窗帘数据附加", device.getDeviceId());
            }
            dto.setRelayDeviceStatusList(relayDeviceStatuses);
            // 统一编码：1-手动 2-自动（与控制面 / 前端 / monitorControlMode() 保持一致）
            dto.setAutomation(automationControlService.isAutomationEnabled() ? "2" : "1");
            result.put("code", 200);
            result.put("total",relayDeviceStatuses.size());
            result.put("data",dto);
            result.put("message","获取设备列表成功");
            logger.info("获取继电器列表成功，共 {} 个设备", relayDeviceStatuses.size());
            return result;
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message","获取继电器信息列表失败: " + e.getMessage());
            logger.error("获取继电器列表失败", e);
            return result;
        }
    }

}
