import axios from "axios";

// 获取所有单元的大体数据
export function getMqttConfigListApi(token, userId) {
  return axios.get(`/api/mqtt/config/getConfigListByUserId/${userId}`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
}

// 获取具体设备信息（旧接口，返回完整配置包括 topic）
export function getDeviceInfoApi(deviceId, token) {
  return axios.get(`/api/mqtt/config/getConfigList/${deviceId}`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
}

// 获取设备继电器状态（新接口，专门用于获取继电器设备状态）
export function getRelayDeviceStatusApi(deviceProtocol, token) {
  return axios.get(`/api/iot/relay_device_status/device_protocol/${deviceProtocol}`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
}

// 获取设备状态趋势折线图
export function getDeviceTrendApi(token, topicId, type) {
  return axios.post(
    "/api/mqtt/config/getAvgZeebeList",
    { topicId, type },
    {
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      },
    }
  );
}

//传感器控制和设备自动化控制
export function controlDeviceApi(token, data) {
  return axios.post(`/api/mqttSend/mqttSend`, data, {
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
  });
}

// 控制器控制
export function controlControllerApi(token, data) {
  return axios.post(`/api/mqttSend/switchSend`, data, {
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
  });
}


// 查询4G接口实时数据
export function getGroupList(token, configId) {
  return axios.get(`/api/group/ioLList`, {
    params: {
      configId: configId
    },
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
}

// 新增设备
export function addDevice(token, deviceData) {
  return axios.post('/api/device/add', deviceData, {
    headers: {
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json',
    },
  });
}

// 更新设备
export function updateDevice(token, deviceId, deviceData) {
  return axios.put(`/api/device/update/${deviceId}`, deviceData, {
    headers: {
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json',
    },
  });
}

// 删除设备
export function deleteDevice(token, deviceId) {
  return axios.delete(`/api/device/delete/${deviceId}`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
}

// TCP 控制接口 - 用于控制模式切换和设备控制
export function controlTcpApi(data, token) {
  return axios.post(`/api/control/tcp`, data, {
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
  });
}