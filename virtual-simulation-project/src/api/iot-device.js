import axios from "axios";

// IoT设备信息控制器所有设备列表
export function getAllDeviceList() {
    return axios.get(`/api/device/list`);
}

// 添加设备
export function addDevice(token, deviceData) {
    return axios.post(`/api/device/add`, deviceData, {
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        }
    });
}

// 更新设备
export function updateDevice(token, deviceId, deviceData) {
    return axios.put(`/api/device/update`, deviceData, {
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        }
    });
}

// 删除设备
export function deleteDevice(token, deviceId) {
    return axios.delete(`/api/device/${deviceId}`, {
        headers: {
            'Authorization': `Bearer ${token}`
        }
    });
}

// 获取单个设备详情
export function getDeviceById(deviceId) {
    return axios.get(`/api/device/${deviceId}`);
}

// 获取摄像头设备详情
export function getTelemetryByDeviceId(deviceId) {
    return axios.get(`/api/camera/device/${deviceId}`);
}