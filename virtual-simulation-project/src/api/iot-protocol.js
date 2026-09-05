import axios from "axios";

// IoT设备信息控制器所有设备列表
export function getAllDeviceProtocolList() {
    return axios.get(`/api/protocol/list`);
}

// 修改设备信息控制器
export function updateDeviceProtocol(data) {
    return axios.put(`/api/protocol/update`,data);
}

// 添加设备信息控制器
export function addDeviceProtocol(data) {
    return axios.post(`/api/protocol/add`,data);
}

// 删除设备信息控制器
export function deleteDeviceProtocol(id) {
    return axios.delete(`/api/protocol/delete/${id}`);
}

export function getDeviceProtocolByProtocolId(protocolId) {
    return axios.get(`/api/protocol/info/${protocolId}`);
}

// 获取设备继电器状态 + 控制模式（通过设备ID）
// 该接口返回 { relayDeviceStatusList, automation }，其中 automation: 1-手动 2-自动
// （原 /api/install/sendTcp/{id} 仅返回裸数组、无模式字段，会导致前端每次轮询都重置为手动）
export function getRelayDeviceStatusByProtocol(deviceId) {
    return axios.get(`/api/iot/relay_device_status/device_protocol/${deviceId}`);
}