package com.management.iot.domain.unity;

import com.management.camera.domain.IotCameraInfo;
import com.management.io.domain.IotMeteorologyRegisterData;
import com.management.iot.domain.IotDevice;
import com.management.iot.domain.IotRelayDeviceStatus;
import com.management.iot.domain.IotTelemetry;
import lombok.Data;

import java.util.List;

@Data
public class UnityDto {

    //设备列表
    private List<IotDevice> deviceList;

    //设备数据
    private List<IotTelemetry> telemetryList;

    //摄像头数据
    private List<IotCameraInfo> cameraList;

    //气象数据
    private List<IotMeteorologyRegisterData> meteorologyRegisterDataList;

    //继电器数据
    private List<IotRelayDeviceStatus> relayDeviceStatusList;

    private String automation;
}
