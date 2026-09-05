// src/api/HardwareSimulation.js
import axios from "axios";

// 查看所有设备信息 硬件接线仿真设备
export function getSensorCategoryTree(){
    return axios.get('/api/sensor/genre/sensor_category_tree');
}

// 获取虚拟仿真列表
export function getSensorlist(){
    return axios.get('/api/sensor/genre/list');
}


// 查询元器件管理接口
export function getSensorList(){
    return axios.get('/api/virtual_device/list');
}

// 添加元器件管理接口
export function addedcomponents(token, deviceData) {
  return axios.post('/api/virtual_device/save', deviceData, {
    headers: {
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json',
    },
  });
}

// 修改元器件管理接口
export function modifycomponents(token, deviceData) {
  return axios.put('/api/virtual_device/update', deviceData, {
    headers: {
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json',
    },
  });
}

// 删除元器件管理接口
export function deletecomponents(token, virtualId) {
  return axios.delete(`/api/virtual_device/delete/${virtualId}`, {
    headers: {
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json',
    },
  });
}

// 图片上传接口
export function uploadImage(token, file) {
  const formData = new FormData();
  formData.append('file', file);

  return axios.post('/api/upload_oss', formData, {
    headers: {
      Authorization: `Bearer ${token}`,
      'Content-Type': 'multipart/form-data',
    },
  });
}

// 仿真接线规则检查接口
export function checkWiringRule(token, data) {
  return axios.post('/api/wiring/rules/check', data, {
    headers: {
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json',
    },
  });
}

// 配置设备类型下拉框
export function getDeviceTypeList() { 
    return axios.get('/api/equipment_configuration/constants/list');
}
