import axios from "axios";

//根据设备ID获取时序数据
export function getIotTelemetryByDeviceId(deviceId) {
    return axios.get(`/api/telemetry/device/${deviceId}`);
}

//获取设备遥测统计数据
export function getStatisticsTelemetry(deviceId, telemetryName) {
    return axios.post(`/api/statistics/telemetry`, {
        deviceId,
        telemetryName
    });
}

// 获取每日平均温度统计数据
export function getDailyAverageTemperature(deviceId) {
    return axios.get(`/api/statistics/temperature/${deviceId}`);
}

// 获取每日平均湿度统计数据
export function getDailyAverageHumidity(deviceId) {
    return axios.get(`/api/statistics/humidity/${deviceId}`);
}

// 获取每日总雨量统计数据
export function getDailyRainfall(deviceId) {
    return axios.get(`/api/statistics/rainfall/${deviceId}`);
}

// 获取最新气象数据（风力、风速、风向）
export function getLatestMeteorology(deviceId) {
    return axios.get(`/api/statistics/meteorology/latest/${deviceId}`);
}