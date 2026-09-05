import axios from "axios";

// 获取所有摄像头列表
export function getAllCameraList() {
  return axios.get('/api/camera/list');
}

// 新增摄像头
export function addCamera(data) {
  return axios.post('/api/camera/add', data);
}

// 更新摄像头
export function updateCamera(cameraId, data) {
  return axios.put(`/api/camera/update`, data);
}

// 删除摄像头
export function deleteCamera(cameraId) {
  return axios.delete(`/api/camera/delete/${cameraId}`);
}