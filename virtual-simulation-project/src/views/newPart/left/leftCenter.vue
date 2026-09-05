<template>
  <div class="environment-monitor">
    <!-- 标题栏 -->
    <header class="header">
      <div class="title-container">
        <div class="icon-wrapper">
          <span class="env-icon">🌿</span>
        </div>
        <h2 class="title">环境监测</h2>
      </div>
      <div class="decoration-panel">
        <div class="data-stats">
          <div class="stat-item">
            <span class="stat-label">监测点</span>
            <span class="stat-value">5项</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">更新时间</span>
            <span class="stat-value">{{ updateTime }}</span>
          </div>
        </div>
        <div class="status-lights">
          <div class="light-item" :class="overallStatus.level">
            <div class="light-circle"></div>
            <span class="light-text">{{ overallStatus.text }}</span>
          </div>
        </div>
      </div>
    </header>

    <!-- 监测数据展示区 - 滚动列表 -->
    <main class="content">
      <div class="scroll-container" ref="scrollContainer">
        <div class="scroll-list" :style="{ transform: `translateY(${scrollOffset}px)` }">
          <!-- 温度 -->
          <div class="list-item temperature-item">
            <div class="item-left">
              <span class="item-icon">🌡️</span>
              <div class="item-info">
                <span class="item-title">温度</span>
                <span class="item-subtitle">Temperature</span>
              </div>
            </div>
            <div class="item-center">
              <span class="value-number">{{ formatValue(environmentData.temperature, 1) }}</span>
              <span class="value-unit">°C</span>
            </div>
            <div class="item-right">
              <div class="item-progress">
                <div class="progress-bar">
                  <div class="progress-fill temperature-progress" :style="{ width: getTemperaturePercentage() + '%' }"></div>
                </div>
              </div>
              <span class="status-tag" :class="getTemperatureStatus().level">{{ getTemperatureStatus().text }}</span>
            </div>
          </div>

          <!-- 湿度 -->
          <div class="list-item humidity-item">
            <div class="item-left">
              <span class="item-icon">💧</span>
              <div class="item-info">
                <span class="item-title">湿度</span>
                <span class="item-subtitle">Humidity</span>
              </div>
            </div>
            <div class="item-center">
              <span class="value-number">{{ formatValue(environmentData.humidity, 1) }}</span>
              <span class="value-unit">%</span>
            </div>
            <div class="item-right">
              <div class="item-progress">
                <div class="progress-bar">
                  <div class="progress-fill humidity-progress" :style="{ width: getHumidityPercentage() + '%' }"></div>
                </div>
              </div>
              <span class="status-tag" :class="getHumidityStatus().level">{{ getHumidityStatus().text }}</span>
            </div>
          </div>

          <!-- 光照度 -->
          <div class="list-item illuminance-item">
            <div class="item-left">
              <span class="item-icon">💡</span>
              <div class="item-info">
                <span class="item-title">光照度</span>
                <span class="item-subtitle">Illuminance</span>
              </div>
            </div>
            <div class="item-center">
              <span class="value-number">{{ formatValue(environmentData.illuminance, 0) }}</span>
              <span class="value-unit">lx</span>
            </div>
            <div class="item-right">
              <div class="item-progress">
                <div class="progress-bar">
                  <div class="progress-fill illuminance-progress" :style="{ width: getIlluminancePercentage() + '%' }"></div>
                </div>
              </div>
              <span class="status-tag" :class="getIlluminanceStatus().level">{{ getIlluminanceStatus().text }}</span>
            </div>
          </div>

          <!-- 风速 -->
          <div class="list-item windspeed-item">
            <div class="item-left">
              <span class="item-icon">🌬️</span>
              <div class="item-info">
                <span class="item-title">风速</span>
                <span class="item-subtitle">Wind Speed</span>
              </div>
            </div>
            <div class="item-center">
              <span class="value-number">{{ formatValue(environmentData.windspeed, 2) }}</span>
              <span class="value-unit">m/s</span>
            </div>
            <div class="item-right">
              <div class="item-progress">
                <div class="progress-bar">
                  <div class="progress-fill windspeed-progress" :style="{ width: getWindspeedPercentage() + '%' }"></div>
                </div>
              </div>
              <span class="status-tag" :class="getWindspeedStatus().level">{{ getWindspeedStatus().text }}</span>
            </div>
          </div>

          <!-- 风向 -->
          <div class="list-item winddirection-item">
            <div class="item-left">
              <span class="item-icon">🧭</span>
              <div class="item-info">
                <span class="item-title">风向</span>
                <span class="item-subtitle">Wind Direction</span>
              </div>
            </div>
            <div class="item-center direction-center">
              <span class="value-number direction-text">{{ environmentData.winddirection || '--' }}</span>
            </div>
            <div class="item-right">
              <div class="compass-mini">
                <div class="compass-ring-mini">
                  <span class="compass-n-mini">N</span>
                  <span class="compass-e-mini">E</span>
                  <span class="compass-s-mini">S</span>
                  <span class="compass-w-mini">W</span>
                  <div class="compass-pointer-mini" :style="{ transform: `rotate(${getWindDirectionAngle()}deg)` }">
                    <div class="pointer-arrow-mini"></div>
                  </div>
                  <div class="compass-center-mini"></div>
                </div>
              </div>
              <span class="status-tag" :class="getWindDirectionStatus().level">{{ getWindDirectionStatus().text }}</span>
            </div>
          </div>

          <!-- 复制所有项用于无缝滚动 -->
          <div class="list-item temperature-item">
            <div class="item-left">
              <span class="item-icon">🌡️</span>
              <div class="item-info">
                <span class="item-title">温度</span>
                <span class="item-subtitle">Temperature</span>
              </div>
            </div>
            <div class="item-center">
              <span class="value-number">{{ formatValue(environmentData.temperature, 1) }}</span>
              <span class="value-unit">°C</span>
            </div>
            <div class="item-right">
              <div class="item-progress">
                <div class="progress-bar">
                  <div class="progress-fill temperature-progress" :style="{ width: getTemperaturePercentage() + '%' }"></div>
                </div>
              </div>
              <span class="status-tag" :class="getTemperatureStatus().level">{{ getTemperatureStatus().text }}</span>
            </div>
          </div>

          <div class="list-item humidity-item">
            <div class="item-left">
              <span class="item-icon">💧</span>
              <div class="item-info">
                <span class="item-title">湿度</span>
                <span class="item-subtitle">Humidity</span>
              </div>
            </div>
            <div class="item-center">
              <span class="value-number">{{ formatValue(environmentData.humidity, 1) }}</span>
              <span class="value-unit">%</span>
            </div>
            <div class="item-right">
              <div class="item-progress">
                <div class="progress-bar">
                  <div class="progress-fill humidity-progress" :style="{ width: getHumidityPercentage() + '%' }"></div>
                </div>
              </div>
              <span class="status-tag" :class="getHumidityStatus().level">{{ getHumidityStatus().text }}</span>
            </div>
          </div>

          <div class="list-item illuminance-item">
            <div class="item-left">
              <span class="item-icon">💡</span>
              <div class="item-info">
                <span class="item-title">光照度</span>
                <span class="item-subtitle">Illuminance</span>
              </div>
            </div>
            <div class="item-center">
              <span class="value-number">{{ formatValue(environmentData.illuminance, 0) }}</span>
              <span class="value-unit">lx</span>
            </div>
            <div class="item-right">
              <div class="item-progress">
                <div class="progress-bar">
                  <div class="progress-fill illuminance-progress" :style="{ width: getIlluminancePercentage() + '%' }"></div>
                </div>
              </div>
              <span class="status-tag" :class="getIlluminanceStatus().level">{{ getIlluminanceStatus().text }}</span>
            </div>
          </div>

          <div class="list-item windspeed-item">
            <div class="item-left">
              <span class="item-icon">🌬️</span>
              <div class="item-info">
                <span class="item-title">风速</span>
                <span class="item-subtitle">Wind Speed</span>
              </div>
            </div>
            <div class="item-center">
              <span class="value-number">{{ formatValue(environmentData.windspeed, 2) }}</span>
              <span class="value-unit">m/s</span>
            </div>
            <div class="item-right">
              <div class="item-progress">
                <div class="progress-bar">
                  <div class="progress-fill windspeed-progress" :style="{ width: getWindspeedPercentage() + '%' }"></div>
                </div>
              </div>
              <span class="status-tag" :class="getWindspeedStatus().level">{{ getWindspeedStatus().text }}</span>
            </div>
          </div>

          <div class="list-item winddirection-item">
            <div class="item-left">
              <span class="item-icon">🧭</span>
              <div class="item-info">
                <span class="item-title">风向</span>
                <span class="item-subtitle">Wind Direction</span>
              </div>
            </div>
            <div class="item-center direction-center">
              <span class="value-number direction-text">{{ environmentData.winddirection || '--' }}</span>
            </div>
            <div class="item-right">
              <div class="compass-mini">
                <div class="compass-ring-mini">
                  <span class="compass-n-mini">N</span>
                  <span class="compass-e-mini">E</span>
                  <span class="compass-s-mini">S</span>
                  <span class="compass-w-mini">W</span>
                  <div class="compass-pointer-mini" :style="{ transform: `rotate(${getWindDirectionAngle()}deg)` }">
                    <div class="pointer-arrow-mini"></div>
                  </div>
                  <div class="compass-center-mini"></div>
                </div>
              </div>
              <span class="status-tag" :class="getWindDirectionStatus().level">{{ getWindDirectionStatus().text }}</span>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script>
import { reactive, computed, onMounted, onUnmounted, getCurrentInstance, watch, ref, nextTick } from "vue";
import axios from "axios";

export default {
  name: "LeftCenter",
  setup() {
    const { proxy } = getCurrentInstance();
    const scrollContainer = ref(null);
    
    const getDeviceId = () => {
      const storeDeviceId = proxy?.$store?.getters?.getDeviceId
      console.log('🔍 环境监测-当前设备ID:', storeDeviceId)
      return storeDeviceId || '374750517349453824'
    }

    // 环境监测数据
    const environmentData = reactive({
      temperature: 0,
      humidity: 0,
      illuminance: 0,
      windspeed: 0,
      winddirection: '--'
    });

    const updateTime = ref('--');
    const scrollOffset = ref(0);
    let scrollTimer = null;
    let isScrolling = true;

    const apiState = reactive({
      loading: false,
      error: null,
      lastUpdateTime: null
    });

    // API数据映射
    const apiKeyMap = {
      temperature: 'temperature',
      humidity: 'humidity',
      illuminance: 'illuminance',
      windspeed: 'windspeed',
      winddirection: 'winddirection'
    };

    // 风向角度映射
    const windDirectionAngles = {
      '北': 0, '北风': 0,
      '东北': 45, '东北风': 45,
      '东': 90, '东风': 90,
      '东南': 135, '东南风': 135,
      '南': 180, '南风': 180,
      '西南': 225, '西南风': 225,
      '西': 270, '西风': 270,
      '西北': 315, '西北风': 315
    };

    // 获取风向角度
    const getWindDirectionAngle = () => {
      const dir = environmentData.winddirection;
      if (!dir || dir === '--') return 0;
      return windDirectionAngles[dir] || 0;
    };

    // 加载真实环境监测数据
    const fetchEnvironmentData = async () => {
      if (apiState.loading) return;
      
      apiState.loading = true;
      apiState.error = null;
      
      try {
        const deviceId = getDeviceId();
        console.log('📡 环境监测-请求数据，设备ID:', deviceId)
        
        const response = await axios.get(`/api/telemetry/device/${deviceId}`);
        console.log('✅ 环境监测-API响应:', response.data);

        let dataList = [];
        const respData = response.data;
        
        if (respData && respData.code === 200 && respData.rows && Array.isArray(respData.rows)) {
          dataList = respData.rows;
        } else if (respData && Array.isArray(respData)) {
          dataList = respData;
        } else if (respData && respData.data && Array.isArray(respData.data)) {
          dataList = respData.data;
        } else {
          console.warn('⚠️ 环境监测-无法识别的数据结构')
          apiState.loading = false;
          return;
        }

        if (!dataList || dataList.length === 0) {
          console.warn('⚠️ 环境监测-数据列表为空')
          apiState.loading = false;
          return;
        }

        // 更新环境数据
        Object.entries(apiKeyMap).forEach(([key, apiKey]) => {
          const item = dataList.find(d => d.dataKey === apiKey);
          if (item) {
            if (key === 'winddirection') {
              environmentData.winddirection = item.dataValue || '--';
              console.log(`✅ 更新 winddirection: ${environmentData.winddirection}`);
            } else {
              const value = parseFloat(item.dataValue);
              if (!isNaN(value)) {
                environmentData[key] = value;
                console.log(`✅ 更新 ${key}: ${environmentData[key]}`);
              }
            }
          }
        });
        
        // 更新时间
        const firstItem = dataList[0];
        if (firstItem && firstItem.recordTime) {
          const date = new Date(firstItem.recordTime);
          updateTime.value = date.toLocaleTimeString('zh-CN', {
            hour12: false,
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit'
          });
        } else {
          updateTime.value = new Date().toLocaleTimeString('zh-CN', {
            hour12: false,
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit'
          });
        }
        
        apiState.lastUpdateTime = new Date();
        
      } catch (error) {
        console.error('❌ 环境监测-加载数据失败:', error);
        apiState.error = error.message || '加载失败';
      } finally {
        apiState.loading = false;
      }
    };

    // 温度百分比 (0-50°C)
    const getTemperaturePercentage = () => {
      const value = environmentData.temperature;
      return Math.min(100, Math.max(0, (value / 50) * 100));
    };

    // 湿度百分比 (0-100%)
    const getHumidityPercentage = () => {
      return Math.min(100, Math.max(0, environmentData.humidity));
    };

    // 光照度百分比 (0-2000 lx)
    const getIlluminancePercentage = () => {
      const value = environmentData.illuminance;
      return Math.min(100, Math.max(0, (value / 2000) * 100));
    };

    // 风速百分比 (0-20 m/s)
    const getWindspeedPercentage = () => {
      const value = environmentData.windspeed;
      return Math.min(100, Math.max(0, (value / 20) * 100));
    };

    // 温度状态
    const getTemperatureStatus = () => {
      const value = environmentData.temperature;
      if (value < 10) return { level: 'low', text: '偏低' };
      if (value > 35) return { level: 'high', text: '偏高' };
      return { level: 'normal', text: '适宜' };
    };

    // 湿度状态
    const getHumidityStatus = () => {
      const value = environmentData.humidity;
      if (value < 30) return { level: 'low', text: '干燥' };
      if (value > 70) return { level: 'high', text: '潮湿' };
      return { level: 'normal', text: '舒适' };
    };

    // 光照度状态
    const getIlluminanceStatus = () => {
      const value = environmentData.illuminance;
      if (value < 300) return { level: 'low', text: '昏暗' };
      if (value > 1200) return { level: 'high', text: '强光' };
      return { level: 'normal', text: '适宜' };
    };

    // 风速状态
    const getWindspeedStatus = () => {
      const value = environmentData.windspeed;
      if (value < 0.3) return { level: 'low', text: '无风' };
      if (value > 10) return { level: 'high', text: '强风' };
      return { level: 'normal', text: '正常' };
    };

    // 风向状态
    const getWindDirectionStatus = () => {
      const dir = environmentData.winddirection;
      if (!dir || dir === '--') return { level: 'low', text: '未知' };
      return { level: 'normal', text: '正常' };
    };

    // 总体状态评估
    const overallStatus = computed(() => {
      const statuses = [
        getTemperatureStatus().level,
        getHumidityStatus().level,
        getIlluminanceStatus().level,
        getWindspeedStatus().level
      ];
      
      const abnormalCount = statuses.filter(s => s !== 'normal').length;
      
      if (abnormalCount === 0) return { level: 'excellent', text: '优秀' };
      if (abnormalCount <= 1) return { level: 'good', text: '良好' };
      if (abnormalCount <= 2) return { level: 'warning', text: '注意' };
      return { level: 'danger', text: '警告' };
    });

    // 计算列表总高度
    const getTotalHeight = () => {
      const items = document.querySelectorAll('.list-item');
      if (items.length === 0) return 0;
      const totalItems = items.length / 2; // 一半是复制的
      const firstItemHeight = items[0].offsetHeight + 6; // 高度 + margin
      return firstItemHeight * totalItems;
    };

    // 启动滚动 - 使用 requestAnimationFrame 实现平滑滚动
    const startScroll = () => {
      if (scrollTimer) {
        cancelAnimationFrame(scrollTimer);
        scrollTimer = null;
      }
      
      let lastTime = 0;
      const speed = 0.8; // 滚动速度
        
      const scroll = (timestamp) => {
        if (!isScrolling) return;
        
        if (lastTime === 0) {
          lastTime = timestamp;
          scrollTimer = requestAnimationFrame(scroll);
          return;
        }
        
        const delta = timestamp - lastTime;
        lastTime = timestamp;
        
        // 根据时间增量计算滚动距离，保持匀速
        const step = (delta / 16) * speed;
        scrollOffset.value -= step;
        
        // 获取列表总高度
        const totalHeight = getTotalHeight();
        if (totalHeight > 0 && Math.abs(scrollOffset.value) >= totalHeight) {
          scrollOffset.value = 0;
        }
        
        scrollTimer = requestAnimationFrame(scroll);
      };
      
      scrollTimer = requestAnimationFrame(scroll);
    };

    // 停止滚动
    const stopScroll = () => {
      isScrolling = false;
      if (scrollTimer) {
        cancelAnimationFrame(scrollTimer);
        scrollTimer = null;
      }
    };

    // 恢复滚动
    const resumeScroll = () => {
      isScrolling = true;
      if (!scrollTimer) {
        startScroll();
      }
    };

    let dataTimer = null;

    const startDataUpdate = () => {
      if (dataTimer) clearInterval(dataTimer);
      dataTimer = setInterval(() => {
        console.log('⏰ 环境监测-定时刷新')
        fetchEnvironmentData();
      }, 10000);
    };

    // 鼠标悬停暂停滚动
    const pauseScroll = () => {
      stopScroll();
    };

    const handleMouseEnter = () => {
      pauseScroll();
    };

    const handleMouseLeave = () => {
      resumeScroll();
    };

    onMounted(() => {
      fetchEnvironmentData();
      startDataUpdate();
      
      // 等待DOM渲染完成后启动滚动
      nextTick(() => {
        // 绑定鼠标事件到滚动容器
        if (scrollContainer.value) {
          scrollContainer.value.addEventListener('mouseenter', handleMouseEnter);
          scrollContainer.value.addEventListener('mouseleave', handleMouseLeave);
        }
        startScroll();
      });
    });

    onUnmounted(() => {
      stopScroll();
      if (dataTimer) {
        clearInterval(dataTimer);
        dataTimer = null;
      }
      if (scrollContainer.value) {
        scrollContainer.value.removeEventListener('mouseenter', handleMouseEnter);
        scrollContainer.value.removeEventListener('mouseleave', handleMouseLeave);
      }
    });

    watch(() => getDeviceId(), (newDeviceId, oldDeviceId) => {
      if (newDeviceId && newDeviceId !== oldDeviceId) {
        console.log('🔄 环境监测-设备ID变化:', newDeviceId)
        fetchEnvironmentData();
      }
    });

    const formatValue = (value, precision) => {
      if (value === null || value === undefined || isNaN(value)) {
        return '--';
      }
      return Number(value).toFixed(precision);
    };

    return {
      environmentData,
      updateTime,
      overallStatus,
      scrollOffset,
      scrollContainer,
      fetchEnvironmentData,
      formatValue,
      getTemperaturePercentage,
      getHumidityPercentage,
      getIlluminancePercentage,
      getWindspeedPercentage,
      getTemperatureStatus,
      getHumidityStatus,
      getIlluminanceStatus,
      getWindspeedStatus,
      getWindDirectionStatus,
      getWindDirectionAngle
    };
  },
};
</script>

<style scoped>
.environment-monitor {
  width: 100%;
  height: 100%;
  padding: 12px 16px 16px 16px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  background: linear-gradient(145deg, rgba(255,255,255,0.03) 0%, rgba(255,255,255,0.01) 100%);
  overflow: hidden;
}

/* 标题栏 */
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 10px;
  border-bottom: 2px solid rgba(0, 188, 212, 0.2);
  position: relative;
  flex-shrink: 0;
}

.header::after {
  content: "";
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 60px;
  height: 3px;
  background: linear-gradient(90deg, #00bcd4, #4dd0e1, #80deea);
  border-radius: 2px;
  box-shadow: 0 0 20px rgba(0, 188, 212, 0.3);
}

.title-container {
  display: flex;
  align-items: center;
  gap: 10px;
}

.icon-wrapper {
  width: 38px;
  height: 38px;
  background: linear-gradient(135deg, #00bcd4, #00897b);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 16px rgba(0, 188, 212, 0.3);
  position: relative;
  overflow: hidden;
  flex-shrink: 0;
}

.icon-wrapper::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(255,255,255,0.2) 0%, transparent 50%);
}

.env-icon {
  font-size: 20px;
  position: relative;
  z-index: 1;
}

.title {
  color: #ffffff;
  font-size: 18px;
  font-weight: 700;
  margin: 0;
  letter-spacing: 1px;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
}

.decoration-panel {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 4px 14px;
  background: rgba(255, 255, 255, 0.06);
  border-radius: 10px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(12px);
  flex-shrink: 0;
}

.data-stats {
  display: flex;
  gap: 14px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1px;
}

.stat-label {
  color: rgba(255, 255, 255, 0.5);
  font-size: 9px;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.stat-value {
  color: #ffffff;
  font-size: 12px;
  font-weight: 600;
  background: linear-gradient(135deg, #4dd0e1, #80deea);
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.light-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 3px 10px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.08);
  transition: all 0.3s ease;
}

.light-circle {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  position: relative;
  animation: pulse 2s infinite;
  flex-shrink: 0;
}

.light-circle::after {
  content: '';
  position: absolute;
  inset: -3px;
  border-radius: 50%;
  border: 2px solid currentColor;
  opacity: 0.3;
  animation: ripple 2s infinite;
}

.light-text {
  color: #ffffff;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.light-item.excellent { color: #4dd0e1; border-color: rgba(77, 208, 225, 0.3); background: rgba(77, 208, 225, 0.1); }
.light-item.good { color: #81c784; border-color: rgba(129, 199, 132, 0.3); background: rgba(129, 199, 132, 0.1); }
.light-item.warning { color: #ffb74d; border-color: rgba(255, 183, 77, 0.3); background: rgba(255, 183, 77, 0.1); }
.light-item.danger { color: #ef5350; border-color: rgba(239, 83, 80, 0.3); background: rgba(239, 83, 80, 0.1); }

/* 内容区域 */
.content {
  flex: 1;
  overflow: hidden;
  min-height: 0;
  position: relative;
  display: flex;
  align-items: center;
}

.scroll-container {
  width: 100%;
  height: 100%;
  overflow: hidden;
  position: relative;
  mask-image: linear-gradient(to bottom, transparent 0%, black 5%, black 95%, transparent 100%);
  -webkit-mask-image: linear-gradient(to bottom, transparent 0%, black 5%, black 95%, transparent 100%);
}

.scroll-list {
  transition: none;
  will-change: transform;
}

/* 列表项 */
.list-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 14px;
  margin-bottom: 6px;
  background: rgba(255, 255, 255, 0.04);
  border-radius: 10px;
  border: 1px solid rgba(255, 255, 255, 0.06);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  flex-shrink: 0;
  height: 52px;
}

.list-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  border-radius: 0 2px 2px 0;
}

.list-item:hover {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(255, 255, 255, 0.15);
  transform: scale(1.01);
}

/* 列表项主题色 */
.temperature-item::before { background: #ffa726; }
.humidity-item::before { background: #42a5f5; }
.illuminance-item::before { background: #fbc02d; }
.windspeed-item::before { background: #66bb6a; }
.winddirection-item::before { background: #ab47bc; }

.temperature-item:hover { border-color: rgba(255, 167, 38, 0.3); }
.humidity-item:hover { border-color: rgba(66, 165, 245, 0.3); }
.illuminance-item:hover { border-color: rgba(251, 192, 45, 0.3); }
.windspeed-item:hover { border-color: rgba(102, 187, 106, 0.3); }
.winddirection-item:hover { border-color: rgba(171, 71, 188, 0.3); }

.item-left {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 0 0 140px;
  min-width: 120px;
}

.item-icon {
  font-size: 18px;
  flex-shrink: 0;
}

.item-info {
  display: flex;
  flex-direction: column;
  gap: 1px;
}

.item-title {
  color: #ffffff;
  font-size: 13px;
  font-weight: 600;
}

.item-subtitle {
  color: rgba(255, 255, 255, 0.3);
  font-size: 9px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.item-center {
  display: flex;
  align-items: baseline;
  gap: 3px;
  flex: 0 0 80px;
  justify-content: center;
}

.value-number {
  color: #ffffff;
  font-size: 18px;
  font-weight: 700;
}

.value-unit {
  color: rgba(255, 255, 255, 0.4);
  font-size: 11px;
  font-weight: 500;
}

.direction-center {
  flex: 0 0 100px;
}

.direction-text {
  font-size: 18px;
  background: linear-gradient(135deg, #ce93d8, #ba68c8);
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.item-right {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  justify-content: flex-end;
}

.item-progress {
  flex: 1;
  max-width: 120px;
  min-width: 60px;
}

.progress-bar {
  width: 100%;
  height: 4px;
  background: rgba(255, 255, 255, 0.06);
  border-radius: 2px;
  overflow: hidden;
  box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.1);
}

.progress-fill {
  height: 100%;
  border-radius: 2px;
  transition: width 1s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}

.progress-fill::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(90deg, transparent 0%, rgba(255,255,255,0.2) 50%, transparent 100%);
  animation: shimmer 2s infinite;
}

.temperature-progress { background: linear-gradient(90deg, #ff6f00, #ffa726); }
.humidity-progress { background: linear-gradient(90deg, #0d47a1, #42a5f5); }
.illuminance-progress { background: linear-gradient(90deg, #f57f17, #fbc02d); }
.windspeed-progress { background: linear-gradient(90deg, #1b5e20, #66bb6a); }

@keyframes shimmer {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}

.status-tag {
  font-size: 10px;
  font-weight: 600;
  padding: 2px 10px;
  border-radius: 10px;
  text-align: center;
  min-width: 36px;
  flex-shrink: 0;
}

.status-tag.normal {
  color: #81c784;
  background: rgba(76, 175, 80, 0.15);
  border: 1px solid rgba(76, 175, 80, 0.2);
}

.status-tag.low {
  color: #64b5f6;
  background: rgba(33, 150, 243, 0.15);
  border: 1px solid rgba(33, 150, 243, 0.2);
}

.status-tag.high {
  color: #ef5350;
  background: rgba(244, 67, 54, 0.15);
  border: 1px solid rgba(244, 67, 54, 0.2);
}

/* 风向罗盘 */
.compass-mini {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.compass-ring-mini {
  position: relative;
  width: 38px;
  height: 38px;
  border: 1.5px solid rgba(255, 255, 255, 0.12);
  border-radius: 50%;
  background: radial-gradient(circle, rgba(156, 39, 176, 0.06), transparent);
}

.compass-n-mini, .compass-e-mini, .compass-s-mini, .compass-w-mini {
  position: absolute;
  font-size: 6px;
  font-weight: 700;
  color: rgba(255, 255, 255, 0.3);
}

.compass-n-mini { top: 2px; left: 50%; transform: translateX(-50%); color: #ef5350; }
.compass-e-mini { right: 2px; top: 50%; transform: translateY(-50%); }
.compass-s-mini { bottom: 2px; left: 50%; transform: translateX(-50%); }
.compass-w-mini { left: 2px; top: 50%; transform: translateY(-50%); }

.compass-pointer-mini {
  position: absolute;
  top: 50%;
  left: 50%;
  transform-origin: center;
  transition: transform 0.8s cubic-bezier(0.4, 0, 0.2, 1);
  width: 1.5px;
  height: 14px;
  margin-left: -0.75px;
  margin-top: -14px;
}

.pointer-arrow-mini {
  width: 100%;
  height: 100%;
  background: linear-gradient(180deg, #ab47bc, rgba(171, 71, 188, 0.3));
  position: relative;
  border-radius: 1px;
}

.pointer-arrow-mini::before {
  content: '';
  position: absolute;
  top: -4px;
  left: 50%;
  transform: translateX(-50%);
  width: 0;
  height: 0;
  border-left: 4px solid transparent;
  border-right: 4px solid transparent;
  border-bottom: 7px solid #ab47bc;
}

.compass-center-mini {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 3.5px;
  height: 3.5px;
  background: #ab47bc;
  border-radius: 50%;
  box-shadow: 0 0 10px rgba(156, 39, 176, 0.4);
}

/* 动画 */
@keyframes pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.5; transform: scale(0.8); }
}

@keyframes ripple {
  0% { transform: scale(0.8); opacity: 0.4; }
  100% { transform: scale(1.6); opacity: 0; }
}

/* 响应式 */
@media (max-width: 1200px) {
  .item-left { flex: 0 0 110px; min-width: 90px; }
  .item-center { flex: 0 0 65px; }
  .direction-center { flex: 0 0 80px; }
  .value-number { font-size: 16px; }
  .item-title { font-size: 12px; }
  .item-icon { font-size: 16px; }
  .list-item { padding: 6px 12px; height: 48px; }
}

@media (max-width: 900px) {
  .item-left { flex: 0 0 90px; min-width: 70px; gap: 6px; }
  .item-center { flex: 0 0 55px; }
  .direction-center { flex: 0 0 70px; }
  .item-right { gap: 8px; }
  .value-number { font-size: 14px; }
  .item-title { font-size: 11px; }
  .item-subtitle { font-size: 8px; }
  .item-icon { font-size: 14px; }
  .status-tag { font-size: 9px; padding: 1px 8px; min-width: 30px; }
  .list-item { padding: 5px 10px; height: 44px; margin-bottom: 4px; }
  .item-progress { max-width: 80px; min-width: 40px; }
  .compass-ring-mini { width: 32px; height: 32px; }
  .compass-pointer-mini { height: 12px; margin-top: -12px; }
  .pointer-arrow-mini::before { border-bottom-width: 6px; border-left-width: 3px; border-right-width: 3px; top: -3px; }
}

@media (max-width: 768px) {
  .environment-monitor { padding: 10px 12px 12px 12px; }
  
  .header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .decoration-panel {
    width: 100%;
    justify-content: space-between;
  }
  
  .item-left { flex: 0 0 70px; min-width: 60px; gap: 4px; }
  .item-center { flex: 0 0 45px; }
  .direction-center { flex: 0 0 55px; }
  .value-number { font-size: 12px; }
  .value-unit { font-size: 9px; }
  .item-title { font-size: 10px; }
  .item-subtitle { font-size: 7px; }
  .item-icon { font-size: 12px; }
  .status-tag { font-size: 8px; padding: 1px 6px; min-width: 24px; }
  .list-item { padding: 4px 8px; height: 38px; margin-bottom: 3px; border-radius: 6px; }
  .item-progress { max-width: 50px; min-width: 25px; }
  .compass-ring-mini { width: 26px; height: 26px; border-width: 1px; }
  .compass-pointer-mini { height: 10px; margin-top: -10px; width: 1px; }
  .compass-n-mini, .compass-e-mini, .compass-s-mini, .compass-w-mini { font-size: 5px; }
  .pointer-arrow-mini::before { border-bottom-width: 5px; border-left-width: 2.5px; border-right-width: 2.5px; top: -2.5px; }
  .compass-center-mini { width: 2.5px; height: 2.5px; }
  .title { font-size: 16px; }
  .icon-wrapper { width: 32px; height: 32px; }
  .env-icon { font-size: 17px; }
  .scroll-container {
    mask-image: linear-gradient(to bottom, transparent 0%, black 3%, black 97%, transparent 100%);
    -webkit-mask-image: linear-gradient(to bottom, transparent 0%, black 3%, black 97%, transparent 100%);
  }
}

@media (max-height: 700px) {
  .list-item { padding: 5px 10px; height: 42px; margin-bottom: 4px; }
  .value-number { font-size: 14px; }
  .item-title { font-size: 11px; }
  .item-icon { font-size: 14px; }
  .item-left { flex: 0 0 90px; }
  .header { margin-bottom: 8px; padding-bottom: 8px; }
  .compass-ring-mini { width: 30px; height: 30px; }
  .compass-pointer-mini { height: 11px; margin-top: -11px; }
}

@media (max-height: 550px) {
  .list-item { padding: 3px 8px; height: 34px; margin-bottom: 3px; }
  .value-number { font-size: 12px; }
  .value-unit { font-size: 9px; }
  .item-title { font-size: 10px; }
  .item-subtitle { font-size: 7px; }
  .item-icon { font-size: 12px; }
  .item-left { flex: 0 0 70px; gap: 4px; }
  .item-center { flex: 0 0 40px; }
  .direction-center { flex: 0 0 50px; }
  .status-tag { font-size: 7px; padding: 0 5px; min-width: 20px; }
  .item-progress { max-width: 40px; min-width: 20px; }
  .progress-bar { height: 3px; }
  .compass-ring-mini { width: 22px; height: 22px; }
  .compass-pointer-mini { height: 8px; margin-top: -8px; }
  .compass-n-mini, .compass-e-mini, .compass-s-mini, .compass-w-mini { font-size: 4px; }
  .pointer-arrow-mini::before { border-bottom-width: 4px; border-left-width: 2px; border-right-width: 2px; top: -2px; }
  .compass-center-mini { width: 2px; height: 2px; }
  .header { margin-bottom: 6px; padding-bottom: 6px; }
  .title { font-size: 14px; }
  .icon-wrapper { width: 28px; height: 28px; }
  .env-icon { font-size: 15px; }
  .decoration-panel { padding: 3px 10px; gap: 8px; }
  .stat-label { font-size: 8px; }
  .stat-value { font-size: 10px; }
  .light-text { font-size: 9px; }
  .light-circle { width: 6px; height: 6px; }
  .item-right { gap: 6px; }
  .scroll-container {
    mask-image: linear-gradient(to bottom, transparent 0%, black 5%, black 95%, transparent 100%);
    -webkit-mask-image: linear-gradient(to bottom, transparent 0%, black 5%, black 95%, transparent 100%);
  }
}
</style>