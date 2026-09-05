<template>
  <div class="soil-monitor">
    <!-- 标题栏 -->
    <header class="header">
      <div class="title-container">
        <div class="icon-wrapper">
          <span class="soil-icon">🌱</span>
        </div>
        <h2 class="title">土壤监测</h2>
      </div>
      <div class="decoration-panel">
        <div class="data-stats">
          <div class="stat-item">
            <span class="stat-label">监测点</span>
            <span class="stat-value">3项</span>
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

    <!-- 监测数据展示区 -->
    <main class="content">
      <div class="data-grid">
        <!-- 土壤温度 -->
        <div class="data-card temperature-card">
          <div class="card-header">
            <div class="card-icon">🌡️</div>
            <div class="card-info">
              <span class="card-title">土壤温度</span>
              <span class="card-subtitle">Soil Temperature</span>
            </div>
          </div>
          <div class="card-body">
            <div class="value-display">
              <span class="value">{{ formatValue(soilData.temperature, 1) }}</span>
              <span class="unit">°C</span>
            </div>
            <div class="progress-bar">
              <div 
                class="progress-fill temperature-progress" 
                :style="{ width: getTemperaturePercentage() + '%' }"
              ></div>
            </div>
            <div class="status-tag" :class="getTemperatureStatus().level">
              {{ getTemperatureStatus().text }}
            </div>
          </div>
        </div>

        <!-- 土壤水分 -->
        <div class="data-card moisture-card">
          <div class="card-header">
            <div class="card-icon">💧</div>
            <div class="card-info">
              <span class="card-title">土壤水分</span>
              <span class="card-subtitle">Soil Moisture</span>
            </div>
          </div>
          <div class="card-body">
            <div class="value-display">
              <span class="value">{{ formatValue(soilData.moisture, 0) }}</span>
              <span class="unit">%</span>
            </div>
            <div class="progress-bar">
              <div 
                class="progress-fill moisture-progress" 
                :style="{ width: getMoisturePercentage() + '%' }"
              ></div>
            </div>
            <div class="status-tag" :class="getMoistureStatus().level">
              {{ getMoistureStatus().text }}
            </div>
          </div>
        </div>

        <!-- 土壤电导率 -->
        <div class="data-card conductivity-card">
          <div class="card-header">
            <div class="card-icon">⚡</div>
            <div class="card-info">
              <span class="card-title">土壤电导率</span>
              <span class="card-subtitle">Conductivity</span>
            </div>
          </div>
          <div class="card-body">
            <div class="value-display">
              <span class="value">{{ formatValue(soilData.conductivity, 1) }}</span>
              <span class="unit">μS/cm</span>
            </div>
            <div class="progress-bar">
              <div 
                class="progress-fill conductivity-progress" 
                :style="{ width: getConductivityPercentage() + '%' }"
              ></div>
            </div>
            <div class="status-tag" :class="getConductivityStatus().level">
              {{ getConductivityStatus().text }}
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- 报警弹窗 -->
    <div v-if="showAlarm" class="alarm-overlay" @click.self="closeAlarm">
      <div class="alarm-modal">
        <div class="alarm-icon-wrapper">
          <span class="alarm-icon">⚠️</span>
        </div>
        <div class="alarm-content">
          <h3 class="alarm-title">水分过高预警</h3>
          <p class="alarm-message">
            设备 <span class="highlight">{{ alarmDeviceId }}</span>
            水分值 <span class="highlight">{{ alarmMoistureValue }}%</span>，超限！
            <br />
            请立即处理。
          </p>
        </div>
        <button class="alarm-btn" @click="closeAlarm">确 定</button>
      </div>
    </div>
  </div>
</template>

<script>
import { reactive, computed, onMounted, onUnmounted, getCurrentInstance, watch, ref } from 'vue'
import axios from 'axios'

export default {
  name: 'LeftTopSoilMonitor',
  setup() {
    const { proxy } = getCurrentInstance()
    
    const getDeviceId = () => {
      const storeDeviceId = proxy?.$store?.getters?.getDeviceId
      console.log('🔍 土壤监测-当前设备ID:', storeDeviceId)
      return storeDeviceId || '374750517349453824'
    }
    
    // 土壤监测数据
    const soilData = reactive({
      temperature: 0,
      moisture: 0,
      conductivity: 0
    })

    const apiState = reactive({
      loading: false,
      error: null,
      lastUpdateTime: null
    })

    const updateTime = ref('--')

    // API数据映射
    const apiKeyMap = {
      temperature: 'soiltemperature',
      moisture: 'soilmoisture',
      conductivity: 'soilelectricalconductivity'
    }

    // ===== 水分超限告警 =====
    const MOISTURE_THRESHOLD = 60
    const moistureAlertActive = ref(false)
    const showAlarm = ref(false)
    const alarmDeviceId = ref('')
    const alarmMoistureValue = ref(0)
    let audioContext = null
    let oscillatorNode = null
    let gainNode = null
    let alarmIntervalId = null

    // ===== 告警声音（Web Audio API） =====
    const playAlarmSound = () => {
      try {
        // 复用预创建的 AudioContext；若不存在才新建
        if (!audioContext) {
          audioContext = new (window.AudioContext || window['webkitAudioContext'])()
        }
        // 确保 context 处于 running 状态
        if (audioContext.state === 'suspended') {
          audioContext.resume()
        }
        // 清掉之前可能残留的 oscillator
        if (oscillatorNode) {
          try { oscillatorNode.stop() } catch (e) {}
          oscillatorNode = null
        }
        const playBeep = () => {
          oscillatorNode = audioContext.createOscillator()
          gainNode = audioContext.createGain()
          oscillatorNode.type = 'square'
          oscillatorNode.frequency.value = 880 // 高音蜂鸣
          gainNode.gain.setValueAtTime(0.3, audioContext.currentTime)
          oscillatorNode.connect(gainNode)
          gainNode.connect(audioContext.destination)
          oscillatorNode.start()
          oscillatorNode.stop(audioContext.currentTime + 0.2)
        }
        // 每 600ms 蜂鸣一次
        playBeep()
        alarmIntervalId = setInterval(playBeep, 600)
      } catch (e) {
        console.warn('AudioContext 不支持:', e)
      }
    }

    const stopAlarmSound = () => {
      if (alarmIntervalId) {
        clearInterval(alarmIntervalId)
        alarmIntervalId = null
      }
      if (oscillatorNode) {
        try { oscillatorNode.stop() } catch (e) {}
        oscillatorNode = null
      }
      if (audioContext) {
        try { audioContext.close() } catch (e) {}
        audioContext = null
      }
      gainNode = null
    }

    // ===== 关闭报警弹窗 =====
    const closeAlarm = () => {
      showAlarm.value = false
      stopAlarmSound()
    }

    // 检查水分是否超限并触发告警
    const checkMoistureAlert = () => {
      const moisture = soilData.moisture
      const deviceId = getDeviceId()

      if (moisture > MOISTURE_THRESHOLD && !moistureAlertActive.value) {
        moistureAlertActive.value = true
        alarmDeviceId.value = deviceId
        alarmMoistureValue.value = moisture
        showAlarm.value = true
        playAlarmSound()
      } else if (moisture <= MOISTURE_THRESHOLD && moistureAlertActive.value) {
        moistureAlertActive.value = false
        showAlarm.value = false
        stopAlarmSound()
      }
    }

    // 异步进行，数据->解析
    const fetchSoilData = async () => {
      if (apiState.loading) return
      
      apiState.loading = true
      apiState.error = null
      
      try {
        const deviceId = getDeviceId()
        console.log('📡 土壤监测-请求数据，设备ID:', deviceId)
        
        const response = await axios.get(`/api/telemetry/device/${deviceId}`)
        console.log('✅ 土壤监测-API响应:', response.data)

        let dataList = []
        const respData = response.data
        
        if (respData && respData.code === 200 && respData.rows && Array.isArray(respData.rows)) {
          dataList = respData.rows
        } else if (respData && Array.isArray(respData)) {
          dataList = respData
        } else if (respData && respData.data && Array.isArray(respData.data)) {
          dataList = respData.data
        } else {
          console.warn('⚠️ 土壤监测-无法识别的数据结构')
          apiState.loading = false
          return
        }

        if (!dataList || dataList.length === 0) {
          console.warn('⚠️ 土壤监测-数据列表为空')
          apiState.loading = false
          return
        }

        console.log('📋 土壤监测-数据列表:', dataList)

        // 更新土壤数据
        Object.entries(apiKeyMap).forEach(([key, apiKey]) => {
          const item = dataList.find(d => d.dataKey === apiKey)
          if (item) {
            const value = parseFloat(item.dataValue)
            if (!isNaN(value)) {
              soilData[key] = value
              console.log(`✅ 更新 ${key}: ${soilData[key]}`)
            }
          }
        })
        
        // 更新时间
        const firstItem = dataList[0]
        if (firstItem && firstItem.recordTime) {
          const date = new Date(firstItem.recordTime)
          updateTime.value = date.toLocaleTimeString('zh-CN', {
            hour12: false,
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit'
          })
        } else {
          updateTime.value = new Date().toLocaleTimeString('zh-CN', {
            hour12: false,
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit'
          })
        }
        
        apiState.lastUpdateTime = new Date()

        // 检查水分超限告警
        checkMoistureAlert()

      } catch (error) {
        console.error('❌ 土壤监测-加载数据失败:', error)
        apiState.error = error.message || '加载失败'
      } finally {
        apiState.loading = false
      }
    }

    // 获取温度百分比（0-40°C）
    const getTemperaturePercentage = () => {
      const value = soilData.temperature
      return Math.min(100, Math.max(0, ((value - 0) / 40) * 100))
    }

    // 获取水分百分比（0-100%）
    const getMoisturePercentage = () => {
      return Math.min(100, Math.max(0, soilData.moisture))
    }

    // 获取电导率百分比（0-3000 μS/cm）
    const getConductivityPercentage = () => {
      const value = soilData.conductivity
      return Math.min(100, Math.max(0, (value / 3000) * 100))
    }

    // 温度状态
    const getTemperatureStatus = () => {
      const value = soilData.temperature
      if (value < 15) return { level: 'low', text: '偏低' }
      if (value > 30) return { level: 'high', text: '偏高' }
      return { level: 'normal', text: '适宜' }
    }

    // 水分状态
    const getMoistureStatus = () => {
      const value = soilData.moisture
      if (value < 30) return { level: 'low', text: '缺水' }
      if (value > 70) return { level: 'high', text: '过湿' }
      return { level: 'normal', text: '适中' }
    }

    // 电导率状态
    const getConductivityStatus = () => {
      const value = soilData.conductivity
      if (value < 500) return { level: 'low', text: '偏低' }
      if (value > 2000) return { level: 'high', text: '偏高' }
      return { level: 'normal', text: '正常' }
    }

    // 总体状态评估
    const overallStatus = computed(() => {
      const statuses = [
        getTemperatureStatus().level,
        getMoistureStatus().level,
        getConductivityStatus().level
      ]

      const abnormalCount = statuses.filter(s => s !== 'normal').length

      if (abnormalCount === 0) return { level: 'excellent', text: '优秀' }
      if (abnormalCount <= 1) return { level: 'good', text: '良好' }
      if (abnormalCount <= 2) return { level: 'warning', text: '注意' }
      return { level: 'danger', text: '警告' }
    })

    let timer = null

    // 定时任务机制 定时刷新土壤数据 
    const startDataUpdate = () => {
      if (timer) clearInterval(timer)
      timer = setInterval(() => {
        console.log('⏰ 土壤监测-定时刷新')
        fetchSoilData()
      }, 1000)
    }

    onMounted(() => {
      fetchSoilData()
      startDataUpdate()
    })

    onUnmounted(() => {
      if (timer) {
        clearInterval(timer)
        timer = null
      }
      // 清理告警音频资源
      stopAlarmSound()
    })

    watch(() => getDeviceId(), (newDeviceId, oldDeviceId) => {
      if (newDeviceId && newDeviceId !== oldDeviceId) {
        console.log('🔄 土壤监测-设备ID变化:', newDeviceId)
        fetchSoilData()
      }
    })

    const formatValue = (value, precision) => {
      if (value === null || value === undefined || isNaN(value)) {
        return '--'
      }
      return Number(value).toFixed(precision)
    }

    return {
      soilData,
      updateTime,
      overallStatus,
      fetchSoilData,
      formatValue,
      showAlarm,
      alarmDeviceId,
      alarmMoistureValue,
      closeAlarm,
      getTemperaturePercentage,
      getMoisturePercentage,
      getConductivityPercentage,
      getTemperatureStatus,
      getMoistureStatus,
      getConductivityStatus
    }
  }
}
</script>

<style scoped>
.soil-monitor {
  width: 100%;
  height: 100%;
  padding: 16px;
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
  margin-bottom: 16px;
  padding-bottom: 12px;
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
  gap: 12px;
}

.icon-wrapper {
  width: 44px;
  height: 44px;
  background: linear-gradient(135deg, #00bcd4, #00897b);
  border-radius: 12px;
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

.soil-icon {
  font-size: 24px;
  position: relative;
  z-index: 1;
}

.title {
  color: #ffffff;
  font-size: 20px;
  font-weight: 700;
  margin: 0;
  letter-spacing: 1px;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
}

.decoration-panel {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 6px 16px;
  background: rgba(255, 255, 255, 0.06);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(12px);
  flex-shrink: 0;
}

.data-stats {
  display: flex;
  gap: 16px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1px;
}

.stat-label {
  color: rgba(255, 255, 255, 0.5);
  font-size: 10px;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.stat-value {
  color: #ffffff;
  font-size: 14px;
  font-weight: 600;
  background: linear-gradient(135deg, #4dd0e1, #80deea);
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.light-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 12px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.08);
  transition: all 0.3s ease;
}

.light-circle {
  width: 10px;
  height: 10px;
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
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

/* 状态颜色 */
.light-item.excellent { color: #4dd0e1; border-color: rgba(77, 208, 225, 0.3); background: rgba(77, 208, 225, 0.1); }
.light-item.good { color: #81c784; border-color: rgba(129, 199, 132, 0.3); background: rgba(129, 199, 132, 0.1); }
.light-item.warning { color: #ffb74d; border-color: rgba(255, 183, 77, 0.3); background: rgba(255, 183, 77, 0.1); }
.light-item.danger { color: #ef5350; border-color: rgba(239, 83, 80, 0.3); background: rgba(239, 83, 80, 0.1); }

/* 内容区域 */
.content {
  flex: 1;
  overflow: hidden;
  min-height: 0;
}

.data-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
  height: 100%;
}

/* 数据卡片 */
.data-card {
  background: linear-gradient(145deg, rgba(255,255,255,0.06), rgba(255,255,255,0.02));
  border-radius: 14px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  padding: 16px 18px;
  display: flex;
  flex-direction: column;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  backdrop-filter: blur(12px);
  position: relative;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  min-width: 0;
}

.data-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  opacity: 0.8;
  transition: all 0.4s ease;
}

.data-card::after {
  content: '';
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at 70% 20%, rgba(var(--card-color-rgb, 255,255,255), 0.05) 0%, transparent 60%);
  pointer-events: none;
}

.data-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
  border-color: rgba(255, 255, 255, 0.15);
}

.data-card:hover::before {
  height: 4px;
  opacity: 1;
  box-shadow: 0 0 30px rgba(var(--card-color-rgb), 0.4);
}

/* 卡片主题色 */
.temperature-card { --card-color-rgb: 255, 152, 0; }
.temperature-card::before { background: linear-gradient(90deg, #ff6f00, #ffa726, #ff6f00); background-size: 200% 100%; animation: gradientMove 3s ease infinite; }

.moisture-card { --card-color-rgb: 33, 150, 243; }
.moisture-card::before { background: linear-gradient(90deg, #0d47a1, #42a5f5, #0d47a1); background-size: 200% 100%; animation: gradientMove 3s ease infinite; }

.conductivity-card { --card-color-rgb: 76, 175, 80; }
.conductivity-card::before { background: linear-gradient(90deg, #1b5e20, #66bb6a, #1b5e20); background-size: 200% 100%; animation: gradientMove 3s ease infinite; }

@keyframes gradientMove {
  0%, 100% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
}

.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
  position: relative;
  z-index: 2;
  flex-shrink: 0;
}

.card-icon {
  font-size: 26px;
  line-height: 1;
  flex-shrink: 0;
}

.card-info {
  display: flex;
  flex-direction: column;
  gap: 1px;
  min-width: 0;
}

.card-title {
  color: #ffffff;
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 0.5px;
  white-space: nowrap;
}

.card-subtitle {
  color: rgba(255, 255, 255, 0.3);
  font-size: 10px;
  text-transform: uppercase;
  letter-spacing: 1px;
  white-space: nowrap;
}

.card-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  position: relative;
  z-index: 2;
  min-height: 0;
}

.value-display {
  display: flex;
  align-items: baseline;
  gap: 4px;
  margin-bottom: 10px;
  transition: transform 0.3s ease;
}

.data-card:hover .value-display {
  transform: scale(1.03);
}

.value {
  font-size: 30px;
  font-weight: 800;
  line-height: 1;
  background: linear-gradient(135deg, #ffffff 0%, rgba(255,255,255,0.7) 100%);
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  text-shadow: 0 2px 20px rgba(0, 0, 0, 0.2);
}

.unit {
  color: rgba(255, 255, 255, 0.4);
  font-size: 13px;
  font-weight: 500;
  margin-left: 2px;
}

.progress-bar {
  width: 100%;
  height: 5px;
  background: rgba(255, 255, 255, 0.06);
  border-radius: 3px;
  overflow: hidden;
  margin-bottom: 10px;
  box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.2);
}

.progress-fill {
  height: 100%;
  border-radius: 3px;
  transition: width 1s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}

.progress-fill::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(90deg, transparent 0%, rgba(255,255,255,0.3) 50%, transparent 100%);
  animation: shimmer 2s infinite;
}

.temperature-progress {
  background: linear-gradient(90deg, #ff6f00, #ffa726);
  box-shadow: 0 0 20px rgba(255, 152, 0, 0.3);
}

.moisture-progress {
  background: linear-gradient(90deg, #0d47a1, #42a5f5);
  box-shadow: 0 0 20px rgba(33, 150, 243, 0.3);
}

.conductivity-progress {
  background: linear-gradient(90deg, #1b5e20, #66bb6a);
  box-shadow: 0 0 20px rgba(76, 175, 80, 0.3);
}

@keyframes shimmer {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}

.status-tag {
  align-self: flex-start;
  padding: 3px 12px;
  border-radius: 10px;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.5px;
  transition: all 0.3s ease;
  text-transform: uppercase;
  flex-shrink: 0;
}

.status-tag.normal {
  background: rgba(76, 175, 80, 0.2);
  color: #81c784;
  border: 1px solid rgba(76, 175, 80, 0.3);
  box-shadow: 0 0 20px rgba(76, 175, 80, 0.1);
}

.status-tag.low {
  background: rgba(33, 150, 243, 0.2);
  color: #64b5f6;
  border: 1px solid rgba(33, 150, 243, 0.3);
  box-shadow: 0 0 20px rgba(33, 150, 243, 0.1);
}

.status-tag.high {
  background: rgba(244, 67, 54, 0.2);
  color: #ef5350;
  border: 1px solid rgba(244, 67, 54, 0.3);
  box-shadow: 0 0 20px rgba(244, 67, 54, 0.1);
}

/* 动画 */
@keyframes pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.5; transform: scale(0.8); }
}

@keyframes ripple {
  0% { transform: scale(0.8); opacity: 0.5; }
  100% { transform: scale(1.6); opacity: 0; }
}

/* 响应式 */
@media (max-width: 1024px) {
  .data-grid {
    grid-template-columns: 1fr 1fr;
    gap: 12px;
  }
}

@media (max-width: 768px) {
  .soil-monitor { padding: 12px; }
  
  .header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .decoration-panel {
    width: 100%;
    justify-content: space-between;
  }
  
  .data-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }
  
  .data-card {
    padding: 14px 16px;
  }
  
  .value {
    font-size: 26px;
  }
  
  .title {
    font-size: 18px;
  }
  
  .icon-wrapper {
    width: 38px;
    height: 38px;
  }
  
  .soil-icon {
    font-size: 20px;
  }
}

@media (max-height: 650px) {
  .data-card {
    padding: 12px 14px;
  }
  
  .value {
    font-size: 24px;
  }
  
  .card-icon {
    font-size: 22px;
  }
  
  .card-title {
    font-size: 14px;
  }
  
  .card-header {
    margin-bottom: 8px;
  }
  
  .value-display {
    margin-bottom: 6px;
  }
  
  .progress-bar {
    height: 4px;
    margin-bottom: 6px;
  }
}

/* ===== 报警弹窗样式 ===== */
.alarm-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  animation: alarmFadeIn 0.3s ease;
}

@keyframes alarmFadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.alarm-modal {
  background: linear-gradient(145deg, #1a1a2e 0%, #16213e 100%);
  border-radius: 16px;
  border: 1px solid rgba(239, 83, 80, 0.4);
  box-shadow: 0 0 40px rgba(239, 83, 80, 0.3), 0 20px 60px rgba(0, 0, 0, 0.5);
  padding: 32px 36px;
  min-width: 360px;
  max-width: 440px;
  display: flex;
  flex-direction: column;
  align-items: center;
  animation: alarmSlideIn 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
  position: relative;
  overflow: hidden;
}

.alarm-modal::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #ef5350, #ff7043, #ef5350);
  background-size: 200% 100%;
  animation: gradientMove 1.5s ease infinite;
}

@keyframes alarmSlideIn {
  from { transform: translateY(-30px) scale(0.95); opacity: 0; }
  to { transform: translateY(0) scale(1); opacity: 1; }
}

.alarm-icon-wrapper {
  width: 64px;
  height: 64px;
  background: rgba(239, 83, 80, 0.15);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
  animation: alarmPulse 1.2s ease infinite;
  border: 2px solid rgba(239, 83, 80, 0.3);
}

@keyframes alarmPulse {
  0%, 100% { box-shadow: 0 0 0 0 rgba(239, 83, 80, 0.5); }
  50% { box-shadow: 0 0 0 12px rgba(239, 83, 80, 0); }
}

.alarm-icon {
  font-size: 32px;
}

.alarm-content {
  text-align: center;
  margin-bottom: 24px;
}

.alarm-title {
  color: #ef5350;
  font-size: 20px;
  font-weight: 700;
  margin: 0 0 12px 0;
  letter-spacing: 1px;
  text-shadow: 0 0 20px rgba(239, 83, 80, 0.4);
}

.alarm-message {
  color: rgba(255, 255, 255, 0.85);
  font-size: 14px;
  line-height: 1.8;
  margin: 0;
}

.alarm-message .highlight {
  color: #ffca28;
  font-weight: 700;
  background: rgba(255, 202, 40, 0.15);
  padding: 2px 8px;
  border-radius: 4px;
  margin: 0 2px;
}

.alarm-btn {
  background: linear-gradient(135deg, #ef5350, #ff7043);
  color: #ffffff;
  border: none;
  padding: 12px 48px;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 2px;
  border-radius: 24px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 16px rgba(239, 83, 80, 0.4);
}

.alarm-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 24px rgba(239, 83, 80, 0.6);
}

.alarm-btn:active {
  transform: translateY(0);
}
</style>