package com.management.web.controller.business.unity;

import com.alibaba.fastjson2.JSON;
import com.management.camera.domain.IotCameraInfo;
import com.management.camera.server.IotCameraInfoService;
import com.management.common.core.domain.AjaxResult;
import com.management.common.core.page.TableDataInfo;
import com.management.io.domain.IotMeteorologyRegisterData;
import com.management.io.mapper.IotMeteorologyRegisterDataMapper;
import com.management.io.service.IotMeteorologyRegisterDataService;
import com.management.iot.configuration.tcp.TcpClientManager;
import com.management.iot.domain.*;
import com.management.iot.domain.unity.InquireUnityDto;
import com.management.iot.domain.unity.UnityDto;
import com.management.iot.service.*;
import com.management.web.controller.business.iot.IotDeviceController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/unity")
public class UnityController {

    private static final Logger logger = LoggerFactory.getLogger(UnityController.class);

    @Autowired
    private IotProtocolService iotProtocolService;

    @Autowired
    private IotDeviceService iotDeviceService;

    @Autowired
    private IotTelemetryService iotTelemetryService;

    @Autowired
    private IotCameraInfoService iotCameraInfoService;

    @Autowired
    private IotMeteorologyRegisterDataService iotMeteorologyRegisterDataService;

    @Autowired
    private IotRelayDeviceStatusService iotRelayDeviceStatusService;

    @Autowired
    private AutomationControlService automationControlService;

    @Autowired
    private IotCurtainMachineStatusService iotCurtainMachineStatusService;

    @Autowired
    private TcpClientManager tcpClientManager;

    @PostMapping("/list")
    public AjaxResult list(@RequestBody InquireUnityDto inquireUnityDto){

        AjaxResult result = new AjaxResult();

        try {
            if (inquireUnityDto.getHost() == null && inquireUnityDto.getTopic() == null){
                throw new RuntimeException("参数错误");
            }

            // 校验协议 + 是否存在
            IotProtocol iotProtocol = iotProtocolService.validateProtocolConfigIsNo(JSON.toJSONString(inquireUnityDto));
            if (iotProtocol == null){
                throw new RuntimeException("协议不存在");
            }

            // 获取协议下的设备列表
            IotDevice iotDevice = iotDeviceService.getDevicesByProtocolIdOrStatus(iotProtocol.getProtocolId());

            if (iotDevice == null){
                throw new RuntimeException("设备不存在");
            }

            if (iotDevice.getStatus().equals("ERROR")){
                return AjaxResult.error("设备不在线");
            }

            UnityDto dto = new UnityDto();

            // 获取协议下的设备列表
            List<IotDevice> devices = iotDeviceService.getDevicesByProtocolId(iotProtocol.getProtocolId());
            dto.setDeviceList( devices);

            // 获取设备下的数据
            for (IotDevice device : devices) {

                // 获取设备下的传感器数据
                List<IotTelemetry> iotTelemetries = iotTelemetryService.getTelemetryByDeviceId(device.getDeviceId());

                dto.setTelemetryList(iotTelemetries);

                // 获取设备下的摄像头数据
                List<IotCameraInfo> cameraInfos = iotCameraInfoService.getCameraByDeviceId(device.getDeviceId());

                dto.setCameraList(cameraInfos);

                // 获取设备下的继电器数据
                List<IotRelayDeviceStatus> relayDeviceStatus = tcpClientManager.sendMessageAndWaitResponse(device.getRelayProtocolId(), "qa");

                dto.setRelayDeviceStatusList(relayDeviceStatus);
            }
            // 统一编码：1-手动 2-自动（与控制面 / 前端 / monitorControlMode() 保持一致）
            dto.setAutomation(automationControlService.isAutomationEnabled() ? "2" : "1");
            result.put("code", 200);
            result.put("data",dto);
            result.put("message","获取设备列表成功");
            logger.info("获取设备列表成功，共 {} 个设备", devices.size());
            return result;
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message","获取设备信息列表失败: " + e.getMessage());
            logger.error("获取设备信息列表失败", e);
            return result;
        }
    }


}
