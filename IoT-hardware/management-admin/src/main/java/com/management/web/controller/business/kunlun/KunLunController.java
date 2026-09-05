package com.management.web.controller.business.kunlun;

import com.management.common.core.domain.AjaxResult;
import com.management.io.domain.IotMeteorologyRegisterData;
import com.management.io.service.IotMeteorologyDeviceService;
import com.management.io.service.IotMeteorologyRegisterDataService;
import com.management.iot.configuration.mqtt.MqttConnection;
import com.management.iot.domain.IotDevice;
import com.management.iot.domain.IotProtocol;
import com.management.iot.domain.IotRelayDeviceStatus;
import com.management.iot.domain.IotTelemetry;
import com.management.iot.service.IotDeviceService;
import com.management.iot.service.IotProtocolService;
import com.management.iot.service.IotRelayDeviceStatusService;
import com.management.iot.service.IotTelemetryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class KunLunController {

    @Autowired
    private IotProtocolService iotProtocolService;

    @Autowired
    private IotDeviceService iotDeviceService;

    @Autowired
    private IotTelemetryService iotTelemetryService;

    @Autowired
    private IotRelayDeviceStatusService iotRelayDeviceStatusService;

    @Autowired
    private IotMeteorologyRegisterDataService iotMeteorologyRegisterDataService;

    @GetMapping("/api/kunlun")
    public Map<String, Object> setKunLun() {
        Map<String, Object> message = null;
        //查询所有设备信息
        List<IotDevice> devices = iotDeviceService.getAllDevices();
        for (IotDevice device : devices) {
            Map<String, Map<String, Object>> allData = MqttConnection.getLatestMessages();
            message = allData.getOrDefault(device.getTopic(), Map.of("message", "No data for topic: " + device.getTopic()));
        }

        return message;
    }

}
