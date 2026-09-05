import axios from "axios";

/**
 * 保存串口配置
 * @param {Object} configData - 配置数据
 * @param {string} configData.equipmentConfigurationType - 设备配置类型（如：路由器 0x01）
 * @param {string} configData.panId - PAN ID（如：0011）
 * @param {string} configData.channel - 信道（如：11）
 * @param {string} [configData.uniqueIdentification] - 唯一标识（可选）
 * @param {string} token - 认证令牌
 * @returns {Promise} axios响应对象
 */
export function saveSerialPortConfigApi(configData, token) {
  return axios.post(`api/serial_port_config/save`, configData, {
    headers: {
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json'
    }
  });
}