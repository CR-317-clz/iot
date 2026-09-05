<template>
  <div id="center">
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-overlay">
      <div class="loading-content">
        <div class="loading-spinner"></div>
        <span>正在加载数据...</span>
      </div>
    </div>

    <!-- 错误状态 -->
    <div v-if="error && !loading" class="error-overlay">
      <div class="error-content">
        <span>⚠️ 数据加载失败: {{ error }}</span>
        <div class="error-actions">
          <button @click="fetchData" class="retry-btn">重试</button>
          <button @click="goToDeviceSelection" class="device-btn" v-if="error.includes('设备配置')">选择设备</button>
        </div>
      </div>
    </div>

    <!-- 顶部：传感器数据卡片 - 4个 -->
    <div class="sensor-cards">
      <div class="sensor-card" v-for="sensor in sensors" :key="sensor.title">
        <div class="sensor-icon">{{ sensor.icon }}</div>
        <div class="sensor-info">
          <span class="sensor-title">{{ sensor.title }}</span>
          <dv-digital-flop class="sensor-value" :config="sensor.number" />
        </div>
        <span class="sensor-status" :class="{ online: sensor.status === '1', offline: sensor.status !== '1' }">
          {{ sensor.status === '1' ? '● 在线' : '○ 离线' }}
        </span>
      </div>
    </div>

    <!-- 底部：控制器 + 设备统计 + 环境指数 -->
    <div class="bottom-section">
      <!-- 控制器区域 -->
      <div class="controller-wrapper">
        <div class="section-header">
          <span class="header-icon">⚡</span>
          <span class="header-title">控制器状态</span>
          <span class="header-time">{{ lastUpdateTime ? `🕐 ${lastUpdateTime}` : '' }}</span>
        </div>
        <div class="controller-grid" ref="controllerList">
          <div class="controller-card" v-for="controller in controllers" :key="controller.name">
            <div class="controller-info">
              <span class="controller-name">{{ controller.name }}</span>
              <span class="controller-desc">{{ controller.desc || '继电器' }}</span>
            </div>
            <div class="controller-status-wrapper">
              <span class="controller-status" :class="controller.isOn ? 'status-on' : 'status-off'">
                {{ controller.isOn ? '● 开启' : '○ 关闭' }}
              </span>
            </div>
          </div>
          <div v-if="controllers.length === 0 && !loading" class="no-data">暂无控制器数据</div>
        </div>
      </div>

      <!-- 右侧：设备统计 + 环境指数 -->
      <div class="right-wrapper">
        <!-- 设备统计 -->
        <div class="stats-wrapper">
          <div class="stats-header">
            <span class="stats-icon">📊</span>
            <span class="stats-title">设备统计</span>
          </div>
          <div class="stats-body">
            <!-- 在线率 -->
            <div class="stats-item">
              <div class="stats-ring online-ring">
                <svg viewBox="0 0 120 120">
                  <circle class="ring-bg" cx="60" cy="60" r="50" />
                  <circle class="ring-progress online-progress" cx="60" cy="60" r="50"
                    :stroke-dasharray="circumference"
                    :stroke-dashoffset="onlineOffset" />
                  <text class="ring-number" x="60" y="42">{{ onlineRate }}%</text>
                  <text class="ring-label" x="60" y="62">在线率</text>
                </svg>
              </div>
              <div class="stats-info">
                <span class="stats-dot online-dot"></span>
                <span class="stats-count">{{ onlineCount }} 在线</span>
              </div>
            </div>

            <!-- 离线率 -->
            <div class="stats-item">
              <div class="stats-ring offline-ring">
                <svg viewBox="0 0 120 120">
                  <circle class="ring-bg" cx="60" cy="60" r="50" />
                  <circle class="ring-progress offline-progress" cx="60" cy="60" r="50"
                    :stroke-dasharray="circumference"
                    :stroke-dashoffset="offlineOffset" />
                  <text class="ring-number" x="60" y="42">{{ offlineRate }}%</text>
                  <text class="ring-label" x="60" y="62">离线率</text>
                </svg>
              </div>
              <div class="stats-info">
                <span class="stats-dot offline-dot"></span>
                <span class="stats-count">{{ offlineCount }} 离线</span>
              </div>
            </div>

            <!-- 设备总数 -->
            <div class="stats-total">
              <div class="total-number">{{ totalDevices }}</div>
              <div class="total-label">设备总数</div>
              <div class="total-detail">
                <span class="total-online">● {{ onlineCount }}</span>
                <span class="total-offline">● {{ offlineCount }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 环境质量指数 -->
        <div class="environment-wrapper">
          <div class="env-header">
            <span class="env-icon">🌿</span>
            <span class="env-title">环境质量指数</span>
            <span class="env-score" :style="{ color: envColor }">{{ envScore }}分</span>
          </div>
          <div class="env-body">
            <div class="env-ring">
              <svg viewBox="0 0 120 120">
                <circle class="env-ring-bg" cx="60" cy="60" r="50" />
                <circle class="env-ring-progress" cx="60" cy="60" r="50"
                  :stroke-dasharray="circumference"
                  :stroke-dashoffset="envOffset"
                  :style="{ stroke: envColor }" />
                <text class="env-number" x="60" y="42">{{ envScore }}</text>
                <text class="env-label" x="60" y="62">综合评分</text>
                <text class="env-star" x="60" y="84" :style="{ fill: envColor }">★</text>
              </svg>
            </div>
            <div class="env-details">
              <div class="env-item">
                <span class="env-dot" style="background: #ffa726;"></span>
                <span class="env-item-label">温度</span>
                <span class="env-item-value">{{ getSensorValue('温度') }}°C</span>
              </div>
              <div class="env-item">
                <span class="env-dot" style="background: #66bb6a;"></span>
                <span class="env-item-label">光照</span>
                <span class="env-item-value">{{ getSensorValue('光照度') }}lx</span>
              </div>
              <div class="env-item">
                <span class="env-dot" style="background: #42a5f5;"></span>
                <span class="env-item-label">湿度</span>
                <span class="env-item-value">{{ getSensorValue('湿度') }}%</span>
              </div>
              <div class="env-item">
                <span class="env-dot" style="background: #ab47bc;"></span>
                <span class="env-item-label">土壤</span>
                <span class="env-item-value">{{ getSensorValue('土壤温度') }}°C</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';
import { mapGetters } from "vuex";

export default {
  name: 'CenterBottom',
  data() {
    return {
      sensors: [],
      controllers: [],
      loading: false,
      error: null,
      lastUpdateTime: null,
      dataTimer: null,
      totalDevices: 0,
      onlineCount: 0,
      offlineCount: 0,
      onlineRate: 0,
      offlineRate: 0,
      envScore: 85,
      circumference: 2 * Math.PI * 50,
      iconMap: {
        '温度': '🌡️',
        '湿度': '💧',
        '光照度': '☀️',
        '土壤温度': '🌱',
      }
    };
  },
  computed: {
    ...mapGetters(['getConfigId', 'getDeviceId', 'getRelayProtocolId']),
    currentDeviceId() {
      const id = this.getDeviceId;
      return id || '374750517349453824';
    },
    currentRelayProtocolId() {
      const id = this.getRelayProtocolId;
      return id || '377282140729118720';
    },
    onlineOffset() {
      return this.circumference - (this.onlineRate / 100) * this.circumference;
    },
    offlineOffset() {
      return this.circumference - (this.offlineRate / 100) * this.circumference;
    },
    envOffset() {
      return this.circumference - (this.envScore / 100) * this.circumference;
    },
    envColor() {
      if (this.envScore >= 80) return '#4caf50';
      if (this.envScore >= 60) return '#ffa726';
      if (this.envScore >= 40) return '#ff6b6b';
      return '#ef5350';
    }
  },
  watch: {
    currentDeviceId(newId, oldId) {
      if (newId && newId !== oldId) this.fetchData();
    },
    currentRelayProtocolId(newId, oldId) {
      if (newId && newId !== oldId) this.fetchData();
    }
  },
  methods: {
    async fetchData() {
      this.loading = true;
      this.error = null;
      try {
        const [telemetryRes, controllerRes] = await Promise.all([
          axios.get(`/api/telemetry/device/${this.currentDeviceId}`),
          axios.get(`/api/install/sendTcp/${this.currentRelayProtocolId}`)
        ]);

        if (telemetryRes?.data?.rows) {
          this.loadRealSensorData(telemetryRes.data.rows);
        }
        if (controllerRes?.data?.data) {
          this.loadRealControllerData(controllerRes.data.data);
        } else {
          this.loadMockControllerData();
        }
        this.lastUpdateTime = new Date().toLocaleTimeString();
      } catch (error) {
        console.error('加载数据失败:', error);
        this.error = error.message || '加载失败';
        this.loadMockControllerData();
      } finally {
        this.loading = false;
      }
    },

    getSensorValue(name) {
      const sensor = this.sensors.find(s => s.title === name);
      if (sensor && sensor.number && sensor.number.number && sensor.number.number.length > 0) {
        return sensor.number.number[0];
      }
      return '--';
    },

    loadRealSensorData(data) {
      const displayKeys = [
        { key: 'temperature', name: '温度' },
        { key: 'humidity', name: '湿度' },
        { key: 'illuminance', name: '光照度' },
        { key: 'soiltemperature', name: '土壤温度' }
      ];
      
      const sensors = [];
      let total = 0, online = 0;
      
      displayKeys.forEach(({ key, name }) => {
        const item = data.find(d => d.dataKey === key);
        total++;
        if (item) {
          online++;
          sensors.push({
            deviceName: name,
            curValue: `${item.dataValue}${item.unit || ''}`,
            status: '1'
          });
        } else {
          sensors.push({
            deviceName: name,
            curValue: '--',
            status: '0'
          });
        }
      });
      
      while (sensors.length < 4) {
        const names = ['温度', '湿度', '光照度', '土壤温度'];
        const missing = names.find(n => !sensors.some(s => s.deviceName === n));
        if (missing) {
          const mockValues = {
            '温度': { value: (20 + Math.random() * 10).toFixed(1), unit: '℃' },
            '湿度': { value: (40 + Math.random() * 30).toFixed(1), unit: '%' },
            '光照度': { value: Math.floor(300 + Math.random() * 700), unit: 'lx' },
            '土壤温度': { value: (18 + Math.random() * 12).toFixed(1), unit: '℃' }
          };
          sensors.push({
            deviceName: missing,
            curValue: `${mockValues[missing].value}${mockValues[missing].unit}`,
            status: '1'
          });
          total++;
          online++;
        }
      }
      
      this.totalDevices = total;
      this.onlineCount = online;
      this.offlineCount = total - online;
      this.onlineRate = total > 0 ? Math.round((online / total) * 100) : 0;
      this.offlineRate = total > 0 ? Math.round(((total - online) / total) * 100) : 0;
      
      this.calculateEnvScore();
      this.processSensorData(sensors);
    },

    calculateEnvScore() {
      const temp = parseFloat(this.getSensorValue('温度')) || 22;
      const humidity = parseFloat(this.getSensorValue('湿度')) || 55;
      const light = parseFloat(this.getSensorValue('光照度')) || 600;
      
      let tempScore = 100 - Math.abs(temp - 23) * 5;
      tempScore = Math.max(0, Math.min(100, tempScore));
      
      let humidityScore = 100 - Math.min(Math.abs(humidity - 55) * 2, 100);
      humidityScore = Math.max(0, Math.min(100, humidityScore));
      
      let lightScore = 100 - Math.min(Math.abs(light - 750) / 10, 100);
      lightScore = Math.max(0, Math.min(100, lightScore));
      
      this.envScore = Math.round((tempScore + humidityScore + lightScore) / 3);
    },

    loadRealControllerData(data) {
      const controllers = data.map(item => ({
        deviceName: item.relayDeviceName,
        curValue: parseInt(item.status),
      }));
      this.processControllerData(controllers);
    },

    loadMockControllerData() {
      const mock = [
        { deviceName: '水泵', curValue: Math.random() > 0.5 ? 1 : 0 },
        { deviceName: '风扇', curValue: Math.random() > 0.5 ? 1 : 0 },
        { deviceName: '灯光', curValue: Math.random() > 0.5 ? 1 : 0 },
        { deviceName: '加热', curValue: Math.random() > 0.5 ? 1 : 0 },
        { deviceName: '灌溉', curValue: Math.random() > 0.5 ? 1 : 0 },
        { deviceName: '通风', curValue: Math.random() > 0.5 ? 1 : 0 },
      ];
      
      this.totalDevices = 8;
      this.onlineCount = Math.floor(Math.random() * 3) + 5;
      this.offlineCount = this.totalDevices - this.onlineCount;
      this.onlineRate = Math.round((this.onlineCount / this.totalDevices) * 100);
      this.offlineRate = Math.round((this.offlineCount / this.totalDevices) * 100);
      this.envScore = Math.floor(Math.random() * 30) + 60;
      
      this.processControllerData(mock);
    },

    processSensorData(list) {
      this.sensors = list.map(s => {
        const { value, unit } = this.parseValue(s.curValue, s.deviceName);
        const decimal = this.getDecimalPlaces(s.deviceName);
        const icon = this.iconMap[s.deviceName] || '📊';
        
        return {
          title: s.deviceName,
          icon: icon,
          status: s.status,
          number: {
            number: [parseFloat(value.toFixed(decimal))],
            toFixed: decimal,
            textAlign: "left",
            content: `{nt}${unit}`,
            style: { fontSize: 22, fontWeight: 'bold' },
          },
        };
      });
    },

    processControllerData(list) {
      this.controllers = list.map(r => ({
        name: r.deviceName,
        desc: '继电器',
        isOn: r.curValue === 1,
      }));
    },

    parseValue(curValue, deviceName) {
      let value = 0, unit = '';
      if (typeof curValue === 'string') {
        const match = curValue.match(/(-?\d+(?:\.\d+)?)\s*([a-zA-Z%°μ/²³]+)?/);
        if (match) { value = parseFloat(match[1]); unit = match[2] || ''; }
      } else if (typeof curValue === 'number') {
        value = curValue;
        if (deviceName.includes('温度')) unit = '℃';
        else if (deviceName.includes('湿度')) unit = '%';
        else if (deviceName.includes('光照')) unit = 'lx';
      }
      return { value, unit };
    },

    getDecimalPlaces(name) {
      const lower = name.toLowerCase();
      if (lower.includes('温度') || lower.includes('湿度')) return 1;
      if (lower.includes('光照')) return 0;
      return 1;
    },

    goToDeviceSelection() {
      this.$router.push('/layout/big-screen');
    }
  },
  created() {
    this.fetchData();
    this.dataTimer = setInterval(this.fetchData, 5000);
  },
  beforeDestroy() {
    if (this.dataTimer) clearInterval(this.dataTimer);
  }
};
</script>

<style lang="scss" scoped>
#center {
  width: 100%;
  height: 100%;
  padding: 10px 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  background: linear-gradient(145deg, rgba(255,255,255,0.03) 0%, rgba(255,255,255,0.01) 100%);
  border-radius: 14px;
  border: 1px solid rgba(37, 125, 255, 0.08);
  box-sizing: border-box;
  overflow: hidden;
  position: relative;
}

/* 加载/错误遮罩 */
.loading-overlay, .error-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.75);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 100;
  border-radius: 14px;
  backdrop-filter: blur(8px);
}

.loading-content, .error-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #25d2ff;
  font-size: 13px;
  gap: 8px;
}

.loading-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid rgba(37, 210, 255, 0.15);
  border-top: 3px solid #25d2ff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.error-content { color: #ff6b6b; }
.error-actions { display: flex; gap: 6px; }

.retry-btn, .device-btn {
  padding: 4px 14px;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 11px;
  transition: all 0.3s;
}
.retry-btn {
  background: linear-gradient(135deg, #25d2ff, #00897b);
  color: #fff;
}
.device-btn {
  background: linear-gradient(135deg, #ff9800, #f57c00);
  color: #fff;
}
.retry-btn:hover, .device-btn:hover {
  transform: scale(1.05);
}

/* ================= 传感器卡片 - 自适应 ================= */
.sensor-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
  flex-shrink: 0;
}

.sensor-card {
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(37, 210, 255, 0.08);
  border-radius: 10px;
  padding: 6px 10px;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s;
  position: relative;
  overflow: hidden;
  min-height: 56px;
  height: auto;
}
.sensor-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 3px;
  height: 100%;
  background: linear-gradient(180deg, #25d2ff, #4dd0e1);
}
.sensor-card:hover {
  border-color: rgba(37, 210, 255, 0.3);
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(37, 210, 255, 0.08);
}

.sensor-icon {
  font-size: 20px;
  flex-shrink: 0;
}

.sensor-info {
  flex: 1;
  min-width: 0;
}
.sensor-title {
  display: block;
  font-size: 11px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.5);
  margin-bottom: 1px;
}
.sensor-value {
  height: 24px;
}
.sensor-value :deep(.dv-digital-flop) {
  font-size: 16px !important;
}

.sensor-status {
  font-size: 9px;
  font-weight: 500;
  padding: 1px 8px;
  border-radius: 8px;
  flex-shrink: 0;
}
.sensor-status.online {
  color: #4caf50;
  background: rgba(76, 175, 80, 0.12);
}
.sensor-status.offline {
  color: #666;
  background: rgba(255, 255, 255, 0.05);
}

/* ================= 底部区域 ================= */
.bottom-section {
  flex: 1;
  display: flex;
  gap: 8px;
  min-height: 0;
}

/* ===== 控制器区域 ===== */
.controller-wrapper {
  flex: 0 0 48%;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(37, 210, 255, 0.06);
  border-radius: 10px;
  padding: 8px 12px;
  display: flex;
  flex-direction: column;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 6px;
  padding-bottom: 6px;
  border-bottom: 1px solid rgba(37, 210, 255, 0.08);
  flex-shrink: 0;
}
.header-icon {
  font-size: 14px;
}
.header-title {
  font-size: 13px;
  font-weight: 600;
  color: #25d2ff;
}
.header-time {
  margin-left: auto;
  font-size: 9px;
  color: rgba(255, 255, 255, 0.3);
}

.controller-grid {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 4px;
  padding-top: 4px;
  overflow-y: auto;
  min-height: 0;
}
.controller-grid::-webkit-scrollbar {
  width: 2px;
}
.controller-grid::-webkit-scrollbar-thumb {
  background: rgba(37, 210, 255, 0.15);
  border-radius: 2px;
}

.controller-card {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(37, 210, 255, 0.06);
  border-radius: 6px;
  padding: 5px 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: all 0.3s;
}
.controller-card:hover {
  background: rgba(255, 255, 255, 0.06);
  border-color: rgba(37, 210, 255, 0.15);
}

.controller-info {
  display: flex;
  flex-direction: column;
}
.controller-name {
  font-size: 12px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.85);
}
.controller-desc {
  font-size: 8px;
  color: rgba(255, 255, 255, 0.25);
}

.controller-status-wrapper {
  flex-shrink: 0;
}
.controller-status {
  font-size: 10px;
  font-weight: 600;
  padding: 2px 10px;
  border-radius: 8px;
}
.status-on {
  background: rgba(37, 210, 255, 0.15);
  color: #25d2ff;
  border: 1px solid rgba(37, 210, 255, 0.2);
}
.status-off {
  background: rgba(255, 255, 255, 0.05);
  color: rgba(255, 255, 255, 0.35);
  border: 1px solid rgba(255, 255, 255, 0.06);
}

.no-data {
  grid-column: 1 / -1;
  color: rgba(255, 255, 255, 0.3);
  text-align: center;
  padding: 12px 0;
  font-size: 12px;
}

/* ===== 右侧区域 ===== */
.right-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-height: 0;
}

/* ===== 设备统计 ===== */
.stats-wrapper {
  flex: 0 0 52%;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(37, 210, 255, 0.06);
  border-radius: 10px;
  padding: 8px 12px;
  display: flex;
  flex-direction: column;
}

.stats-header {
  display: flex;
  align-items: center;
  gap: 6px;
  padding-bottom: 4px;
  border-bottom: 1px solid rgba(37, 210, 255, 0.08);
  flex-shrink: 0;
}
.stats-icon {
  font-size: 14px;
}
.stats-title {
  font-size: 13px;
  font-weight: 600;
  color: #25d2ff;
}

.stats-body {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-around;
  padding-top: 2px;
  min-height: 0;
}

.stats-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stats-ring {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.stats-ring svg {
  width: 50px;
  height: 50px;
  transform: rotate(-90deg);
}

.ring-bg {
  fill: none;
  stroke: rgba(255, 255, 255, 0.06);
  stroke-width: 4;
}

.ring-progress {
  fill: none;
  stroke-width: 4;
  stroke-linecap: round;
  transition: stroke-dashoffset 1.5s ease;
}

.online-progress {
  stroke: #4caf50;
}
.offline-progress {
  stroke: #ff6b6b;
}

.ring-number {
  font-size: 13px;
  font-weight: 700;
  fill: #ffffff;
  text-anchor: middle;
  transform: rotate(90deg);
  dominant-baseline: central;
}

.ring-label {
  font-size: 7px;
  fill: rgba(255, 255, 255, 0.35);
  text-anchor: middle;
  transform: rotate(90deg);
  dominant-baseline: central;
}

.stats-info {
  display: flex;
  align-items: center;
  gap: 3px;
  margin-top: 1px;
}
.stats-dot {
  width: 4px;
  height: 4px;
  border-radius: 50%;
}
.online-dot {
  background: #4caf50;
}
.offline-dot {
  background: #ff6b6b;
}
.stats-count {
  font-size: 9px;
  color: rgba(255, 255, 255, 0.5);
}

/* 设备总数 */
.stats-total {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 2px 10px;
  background: rgba(255, 255, 255, 0.04);
  border-radius: 8px;
  border: 1px solid rgba(37, 210, 255, 0.08);
}

.total-number {
  font-size: 24px;
  font-weight: 800;
  color: #25d2ff;
  line-height: 1;
}

.total-label {
  font-size: 9px;
  color: rgba(255, 255, 255, 0.4);
  margin-top: 1px;
}

.total-detail {
  display: flex;
  gap: 8px;
  margin-top: 2px;
  font-size: 9px;
}
.total-online {
  color: #4caf50;
}
.total-offline {
  color: #ff6b6b;
}

/* ===== 环境质量指数 ===== */
.environment-wrapper {
  flex: 1;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(37, 210, 255, 0.06);
  border-radius: 10px;
  padding: 8px 12px;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.env-header {
  display: flex;
  align-items: center;
  gap: 6px;
  padding-bottom: 4px;
  border-bottom: 1px solid rgba(37, 210, 255, 0.08);
  flex-shrink: 0;
}
.env-icon {
  font-size: 14px;
}
.env-title {
  font-size: 13px;
  font-weight: 600;
  color: #25d2ff;
}
.env-score {
  margin-left: auto;
  font-size: 13px;
  font-weight: 700;
}

.env-body {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 10px;
  padding-top: 2px;
  min-height: 0;
}

.env-ring {
  flex-shrink: 0;
}
.env-ring svg {
  width: 54px;
  height: 54px;
  transform: rotate(-90deg);
}

.env-ring-bg {
  fill: none;
  stroke: rgba(255, 255, 255, 0.06);
  stroke-width: 4;
}

.env-ring-progress {
  fill: none;
  stroke-width: 4;
  stroke-linecap: round;
  transition: stroke-dashoffset 1.5s ease;
}

.env-number {
  font-size: 15px;
  font-weight: 800;
  fill: #ffffff;
  text-anchor: middle;
  transform: rotate(90deg);
  dominant-baseline: central;
}

.env-label {
  font-size: 7px;
  fill: rgba(255, 255, 255, 0.35);
  text-anchor: middle;
  transform: rotate(90deg);
  dominant-baseline: central;
}

.env-star {
  font-size: 10px;
  text-anchor: middle;
  transform: rotate(90deg);
  dominant-baseline: central;
}

.env-details {
  flex: 1;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1px 8px;
}

.env-item {
  display: flex;
  align-items: center;
  gap: 3px;
  font-size: 10px;
  color: rgba(255, 255, 255, 0.5);
}

.env-dot {
  width: 4px;
  height: 4px;
  border-radius: 50%;
  flex-shrink: 0;
}

.env-item-label {
  min-width: 22px;
}

.env-item-value {
  color: #ffffff;
  font-weight: 500;
}

/* ================= 响应式 - 自适应所有分辨率 ================= */
/* 大屏 1920x1080 */
@media (min-width: 1920px) {
  #center { padding: 16px 20px; gap: 12px; }
  .sensor-card { min-height: 72px; padding: 10px 16px; }
  .sensor-icon { font-size: 26px; }
  .sensor-title { font-size: 14px; }
  .sensor-value { height: 30px; }
  .stats-ring svg { width: 64px; height: 64px; }
  .env-ring svg { width: 68px; height: 68px; }
  .ring-number { font-size: 17px; }
  .env-number { font-size: 19px; }
  .total-number { font-size: 32px; }
  .controller-name { font-size: 14px; }
  .header-title { font-size: 15px; }
  .stats-title { font-size: 15px; }
  .env-title { font-size: 15px; }
}

/* 中屏 1440x900 */
@media (max-width: 1440px) {
  .sensor-card { min-height: 60px; padding: 6px 12px; }
  .sensor-icon { font-size: 20px; }
  .sensor-title { font-size: 11px; }
  .sensor-value { height: 24px; }
  .stats-ring svg { width: 48px; height: 48px; }
  .env-ring svg { width: 52px; height: 52px; }
  .ring-number { font-size: 12px; }
  .env-number { font-size: 14px; }
  .total-number { font-size: 22px; }
}

/* 小屏 1366x768 */
@media (max-width: 1366px) {
  #center { padding: 8px 10px; gap: 6px; }
  .sensor-card { min-height: 50px; padding: 4px 8px; gap: 6px; }
  .sensor-icon { font-size: 17px; }
  .sensor-title { font-size: 10px; }
  .sensor-value { height: 20px; }
  .sensor-value :deep(.dv-digital-flop) { font-size: 13px !important; }
  .sensor-status { font-size: 8px; padding: 1px 6px; }
  .stats-ring svg { width: 40px; height: 40px; }
  .env-ring svg { width: 44px; height: 44px; }
  .ring-number { font-size: 10px; }
  .env-number { font-size: 12px; }
  .total-number { font-size: 18px; }
  .controller-name { font-size: 10px; }
  .controller-status { font-size: 9px; padding: 1px 8px; }
  .header-title { font-size: 11px; }
  .stats-title { font-size: 11px; }
  .env-title { font-size: 11px; }
  .env-score { font-size: 11px; }
  .stats-count { font-size: 8px; }
  .env-item { font-size: 9px; }
  .env-item-label { min-width: 18px; }
  .stats-total { padding: 2px 8px; }
  .total-detail { font-size: 8px; gap: 6px; }
  .controller-wrapper { padding: 6px 8px; }
  .stats-wrapper { padding: 6px 8px; }
  .environment-wrapper { padding: 6px 8px; }
}

/* 超小屏 1280x720 */
@media (max-width: 1280px) {
  .sensor-card { min-height: 44px; padding: 3px 6px; gap: 4px; }
  .sensor-icon { font-size: 15px; }
  .sensor-title { font-size: 9px; }
  .sensor-value { height: 18px; }
  .sensor-value :deep(.dv-digital-flop) { font-size: 11px !important; }
  .sensor-status { font-size: 7px; padding: 0 5px; }
  .stats-ring svg { width: 36px; height: 36px; }
  .env-ring svg { width: 38px; height: 38px; }
  .ring-number { font-size: 9px; }
  .env-number { font-size: 10px; }
  .total-number { font-size: 16px; }
  .total-label { font-size: 8px; }
  .controller-name { font-size: 9px; }
  .controller-status { font-size: 8px; padding: 1px 6px; }
  .header-title { font-size: 10px; }
  .stats-title { font-size: 10px; }
  .env-title { font-size: 10px; }
  .env-score { font-size: 10px; }
  .stats-count { font-size: 7px; }
  .env-item { font-size: 8px; }
  .env-item-label { min-width: 16px; }
  .controller-card { padding: 3px 6px; }
  .controller-grid { gap: 3px; }
}

/* 移动端 */
@media (max-width: 768px) {
  .sensor-cards { grid-template-columns: repeat(2, 1fr); }
  .bottom-section { flex-direction: column; }
  .controller-wrapper { flex: none; height: 40%; }
  .right-wrapper { height: 60%; flex-direction: row; }
  .stats-wrapper { flex: 0 0 50%; }
  .environment-wrapper { flex: 1; }
  .stats-ring svg { width: 44px; height: 44px; }
  .env-ring svg { width: 48px; height: 48px; }
}

/* 极小屏 */
@media (max-width: 480px) {
  .sensor-cards { grid-template-columns: 1fr 1fr; gap: 4px; }
  .sensor-card { min-height: 40px; padding: 2px 6px; }
  .sensor-icon { font-size: 13px; }
  .sensor-title { font-size: 8px; }
  .sensor-value { height: 16px; }
  .sensor-value :deep(.dv-digital-flop) { font-size: 10px !important; }
  .right-wrapper { flex-direction: column; }
  .stats-wrapper { flex: 0 0 45%; }
  .environment-wrapper { flex: 1; }
}
</style>