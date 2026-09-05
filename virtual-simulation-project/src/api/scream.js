import axios from "axios";

// 每日总雨量接口
export function dailyRainfallApi(deviceName) {
  return axios.get(`/api/group/getWeekRain/${deviceName}`);
}

// 每日平均温度、湿度接口
export function dailyWeatherApi(deviceName, rgisterName) {
  return axios.post(`/api/group/getWeekAvgData`, {
    deviceName,
    rgisterName
  });
}

// 今日风力数据
export function todayWindApi(deviceName) {
  return axios.get(`/api/group/getTodayWindDataStatistics/${deviceName}`);
}

// 获取土壤信息
export function soilInfoApi(configId) {
  return axios.get(`/api/mqtt/zeebe/getRealTimeZeebData/${configId}`);
}

// 获取环境监测数据
export function environmentMonitoringApi(configId) {
  return axios.get(`/api/mqtt/adim/getRealTimeAdimData/${configId}`);
}

// 获取传感器和控制器的数据
export function sensorControllerDataApi(configId) {
  return axios.get(`/api/mqtt/config/getConfigList/${configId}`);
}

// 获取视频监控数据
export function videoMonitoringApi(configId) {
  return axios.get(`/api/hik/getHikList/${configId}`);
}