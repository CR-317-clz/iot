import axios from "axios";

// IoT设备信息控制器所有设备列表
export function getAllMeteorologyDeviceList() {
    return axios.get(`/api/meteorology_device/list`);
}

// 获取气象站历史数据
export function getMeteorologyHistoryData(queryParams) {
    return axios.get(`/api/meteorology_device/history`, { params: queryParams });
}

// 获取气象站设备详细数据 (使用设备地址作为参数)
export function getMeteorologyDeviceDetail(deviceAddr) {
    return axios.get(`/api/meteorology_data/meteorology_register_data/${deviceAddr}`);
}

// 统计气象站设备温湿度统计数据 (使用设备地址作为参数)
export function statisticsMeteorologyDeviceDetail(deviceAddr) {
    return axios.get(`/api/meteorology_data/statistics/${deviceAddr}`);
}