<template>
  <div class="weather-monitor">
    <!-- 标题栏 -->  
    <!-- 主要内容 -->
    <main class="content">
      <!-- 风速风向展示区 -->
      <section class="wind-data">
        <!-- 风速 -->
        <div class="data-item">
          <h3 class="item-title">风速</h3>
          <div class="gauge-container">
            <echart :options="gaugeConfig" height="100%" width="100%" />
            <div class="gauge-overlay">
              <span class="speed-value">{{ windSpeed }}</span>
              <span class="unit">m/s</span>
            </div>
          </div>
          <div class="status-info">
            <span class="level">{{ windLevel }}</span>
            <span class="status" :class="getSpeedStatusClass()">{{ getSpeedStatus() }}</span>
          </div>
        </div>
        
        <!-- 风向 -->
        <div class="data-item">
          <h3 class="item-title">风向</h3>
          <div class="compass-container">
            <div class="compass-ring">
              <!-- 方向标记 -->
              <div class="cardinal-mark north">N</div>
              <div class="cardinal-mark east">E</div>
              <div class="cardinal-mark south">S</div>
              <div class="cardinal-mark west">W</div>
              
              <!-- 风向指针 -->
              <div class="wind-pointer" :style="{ transform: `rotate(${windDirection}deg)` }"></div>
              
              <!-- 中心点 -->
              <div class="center-dot"></div>
            </div>
          </div>
          <div class="direction-info">
            <span class="direction">{{ windDirectionText }}</span>
            <span class="angle">{{ windDirection }}°</span>
          </div>
        </div>
      </section>
      
      <!-- 统计数据 -->
      <section class="statistics">
        <div class="stat-item">
          <div class="stat-icon">💨</div>
          <div class="stat-content">
            <span class="label">当前风速</span>
            <span class="value">{{ windSpeed }} <small>m/s</small></span>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon">🏷️</div>
          <div class="stat-content">
            <span class="label">风力状态</span>
            <span class="value" :class="getWindStatusClass()">{{ windStatus }}</span>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon">🕐</div>
          <div class="stat-content">
            <span class="label">更新时间</span>
            <span class="value time">{{ updateTime }}</span>
          </div>
        </div>
      </section>
    </main>
  </div>
</template>

<script>
import echart from '@/common/echart/index.vue'
import { getLatestMeteorology } from '@/api/iot-telemetry'

// 风力等级配置
const WIND_LEVELS = [
  { max: 0.2, level: '0级', desc: '无风' },
  { max: 1.5, level: '1级', desc: '软风' },
  { max: 3.3, level: '2级', desc: '轻风' },
  { max: 5.4, level: '3级', desc: '微风' },
  { max: 7.9, level: '4级', desc: '和风' },
  { max: 10.7, level: '5级', desc: '清劲风' },
  { max: 13.8, level: '6级', desc: '强风' },
  { max: 17.1, level: '7级', desc: '疾风' },
  { max: 20.7, level: '8级', desc: '大风' },
  { max: 24.4, level: '9级', desc: '烈风' },
  { max: 28.4, level: '10级', desc: '狂风' },
  { max: 32.6, level: '11级', desc: '暴风' },
  { max: Infinity, level: '12级', desc: '飓风' }
]

// 风向配置（8个方向）- 支持带"风"和不带"风"两种格式
const WIND_DIRECTIONS = {
  '北': 0,
  '东北': 45,
  '东': 90,
  '东南': 135,
  '南': 180,
  '西南': 225,
  '西': 270,
  '西北': 315,
  '北风': 0,
  '东北风': 45,
  '东风': 90,
  '东南风': 135,
  '南风': 180,
  '西南风': 225,
  '西风': 270,
  '西北风': 315
}

export default {
  name: 'WeatherMonitor',
  components: { echart },
  
  data() {
    return {
      windSpeed: 0,
      windLevel: '0级',
      windDirection: 0,
      windDirectionText: '--',
      updateTime: '--',
      loading: false,
      error: null,
      updateTimer: null,
    }
  },
  
  computed: {
    deviceId() {
      const storeDeviceId = this.$store?.getters?.getDeviceId
      console.log('🔍 风向风速监测-当前设备ID:', storeDeviceId)
      return storeDeviceId || '374750517349453824'
    },
    
    windStatus() {
      if (this.windSpeed > 10.7) return '强风'
      if (this.windSpeed > 5.4) return '正常'
      if (this.windSpeed > 1.5) return '微风'
      return '无风'
    },
    
    gaugeConfig() {
      return {
        series: [{
          type: 'gauge',
          min: 0,
          max: 15,
          radius: '90%',
          center: ['50%', '50%'],
          startAngle: 225,
          endAngle: -45,
          axisLine: {
            lineStyle: {
              width: 6,
              color: [
                [0.3, '#00E676'],
                [0.6, '#FFC107'],
                [0.8, '#FF9800'],
                [1, '#F44336']
              ]
            }
          },
          axisTick: { show: false },
          axisLabel: { show: false },
          splitLine: { show: false },
          detail: { show: false },
          pointer: {
            length: '60%',
            width: 3,
            itemStyle: { color: '#fff' }
          },
          data: [{ value: this.windSpeed }]
        }]
      }
    }
  },
  
  mounted() {
    this.loadWindData()
    this.startDataUpdate()
  },
  
  beforeDestroy() {
    this.stopDataUpdate()
  },
  
  methods: {
    async loadWindData() {
      // 防止重复请求
      if (this.loading) {
        console.log('⏳ 正在加载中，跳过本次请求');
        return;
      }
      
      this.loading = true;
      this.error = null;
      
      try {
        const response = await getLatestMeteorology(this.deviceId);
        console.log('📡 最新气象数据原始响应:', JSON.stringify(response, null, 2));

        // 🔥 多种数据结构兼容
        let dataList = [];
        
        // 情况1: response.rows 是数组
        if (response && response.rows && Array.isArray(response.rows)) {
          dataList = response.rows;
          console.log('✅ 从 response.rows 获取数据，共', dataList.length, '条');
        } 
        // 情况2: response.data.rows
        else if (response && response.data && response.data.rows && Array.isArray(response.data.rows)) {
          dataList = response.data.rows;
          console.log('✅ 从 response.data.rows 获取数据，共', dataList.length, '条');
        }
        // 情况3: response 本身就是数组
        else if (Array.isArray(response)) {
          dataList = response;
          console.log('✅ response 本身就是数组，共', dataList.length, '条');
        }
        // 情况4: response.data 是数组
        else if (response && Array.isArray(response.data)) {
          dataList = response.data;
          console.log('✅ 从 response.data 获取数据，共', dataList.length, '条');
        }
        else {
          console.warn('⚠️ 无法识别的数据结构:', response);
          this.error = '数据格式错误';
          this.loading = false;
          return;
        }

        if (dataList.length === 0) {
          console.warn('⚠️ 数据列表为空');
          this.loading = false;
          return;
        }

        console.log('📋 数据列表详情:', dataList);

        // 🔥 查找风速和风向数据（多种 key 兼容）
        const windSpeedData = dataList.find(item => 
          item.dataKey === 'windspeed' || 
          item.dataKey === 'windSpeed' || 
          item.registerName === '风速'
        );
        
        const windDirectionData = dataList.find(item => 
          item.dataKey === 'winddirection' || 
          item.dataKey === 'windDirection' || 
          item.registerName === '风向'
        );

        console.log('🌬️ 风速数据:', windSpeedData);
        console.log('🧭 风向数据:', windDirectionData);

        // 🔥 设置风速
        let speedValue = 0;
        if (windSpeedData) {
          // dataValue 可能是字符串或数字
          const rawValue = windSpeedData.dataValue;
          speedValue = parseFloat(rawValue);
          if (isNaN(speedValue)) {
            speedValue = 0;
            console.warn('⚠️ 风速值转换失败:', rawValue);
          }
        }
        this.windSpeed = speedValue;
        console.log('🌬️ 设置风速:', this.windSpeed);

        // 🔥 计算风力等级
        const level = WIND_LEVELS.find(item => this.windSpeed <= item.max);
        this.windLevel = level?.level || '0级';
        console.log('💨 风力等级:', this.windLevel);

        // 🔥 设置风向
        let directionText = '--';
        let directionAngle = 0;
        
        if (windDirectionData) {
          directionText = windDirectionData.dataValue || '--';
          // 去除可能的空格
          directionText = directionText.trim();
          
          // 查找角度
          const angle = WIND_DIRECTIONS[directionText];
          if (angle !== undefined) {
            directionAngle = angle;
          } else {
            // 尝试去除"风"字再匹配
            const trimmed = directionText.replace('风', '');
            const angle2 = WIND_DIRECTIONS[trimmed];
            if (angle2 !== undefined) {
              directionAngle = angle2;
            } else {
              console.warn('⚠️ 未知风向:', directionText);
            }
          }
        }
        
        this.windDirectionText = directionText;
        this.windDirection = directionAngle;
        console.log('🧭 设置风向:', this.windDirectionText, this.windDirection + '°');

        // 🔥 更新数据时间
        const recordTime = windSpeedData?.recordTime || windDirectionData?.recordTime;
        if (recordTime) {
          const date = new Date(recordTime);
          this.updateTime = date.toLocaleString('zh-CN', {
            hour12: false,
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit'
          });
        } else {
          this.updateTime = new Date().toLocaleString('zh-CN', {
            hour12: false,
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit'
          });
        }

        console.log('✅ 最终解析结果:', {
          windSpeed: this.windSpeed,
          windLevel: this.windLevel,
          windDirection: this.windDirection,
          windDirectionText: this.windDirectionText,
          updateTime: this.updateTime
        });

      } catch (error) {
        console.error('❌ 加载气象数据失败:', error);
        this.error = error.message || '加载失败';
      } finally {
        this.loading = false;
      }
    },
    
    startDataUpdate() {
      // 先清除旧定时器
      this.stopDataUpdate();
      
      // 🔥 每5秒更新一次风力数据
      this.updateTimer = setInterval(() => {
        console.log('⏰ 定时器触发，每5秒更新一次数据');
        this.loadWindData();
      }, 5 * 1000); // 5秒 = 5000毫秒
      
      console.log('✅ 启动数据更新定时器，间隔5秒');
    },
    
    stopDataUpdate() {
      if (this.updateTimer) {
        clearInterval(this.updateTimer);
        this.updateTimer = null;
        console.log('⏹️ 停止数据更新定时器');
      }
    },
    
    refreshWindData() {
      console.log('🔄 手动刷新数据');
      this.loadWindData();
    },
    
    getSpeedStatus() {
      if (this.windSpeed > 10) return '危险'
      if (this.windSpeed > 6) return '警告'
      if (this.windSpeed > 3) return '正常'
      return '平稳'
    },
    
    getSpeedStatusClass() {
      if (this.windSpeed > 10) return 'danger'
      if (this.windSpeed > 6) return 'warning'
      if (this.windSpeed > 3) return 'normal'
      return 'calm'
    },
    
    getWindStatusClass() {
      if (this.windSpeed > 10.7) return 'danger'
      if (this.windSpeed > 5.4) return 'warning'
      if (this.windSpeed > 1.5) return 'normal'
      return 'calm'
    }
  },
  
  watch: {
    deviceId(newDeviceId, oldDeviceId) {
      if (newDeviceId && newDeviceId !== oldDeviceId) {
        console.log('🔄 检测到设备ID变化:', { 旧值: oldDeviceId, 新值: newDeviceId })
        // 重新加载数据
        this.loadWindData()
        // 重启定时器（使用新设备ID）
        this.startDataUpdate()
      }
    }
  }
}
</script>

<style lang="scss" scoped>
// 变量定义
$primary: #00ffff;
$warning: #ff9800;
$danger: #f44336;
$success: #00e676;
$text: #ffffff;
$border: rgba(37, 125, 255, 0.1);
$bg: rgba(37, 125, 255, 0.1);

.weather-monitor {
  height: 100%;
  border-radius: 8px;
  padding: 8px;
  display: flex;
  flex-direction: column;
}

.header {
  text-align: center;
  margin-bottom: 8px;
  
  .title {
    margin: 0;
    color: $primary;
    font-size: 14px;
    font-weight: bold;
  }
}

.content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 10px;
}

.wind-data {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  flex: 1;
}

.data-item {
  background: $bg;
  border: 1px solid $border;
  border-radius: 8px;
  padding: 8px;
  display: flex;
  flex-direction: column;
}

.item-title {
  margin: 0 0 8px 0;
  color: $text;
  font-size: 16px;
  text-align: center;
}

// 风速样式
.gauge-container {
  position: relative;
  flex: 1;
  min-height: 80px;
  
  .gauge-overlay {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    text-align: center;
    z-index: 10;
    
    .speed-value {
      font-size: 18px;
      font-weight: bold;
      color: $primary;
      display: block;
    }
    
    .unit {
      font-size: 10px;
      color: rgba(255, 255, 255, 0.7);
    }
  }
}

.status-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
  font-size: 16px;
  
  .level {
    color: $primary;
    font-weight: bold;
  }
  
  .status {
    padding: 2px 6px;
    border-radius: 4px;
    
    &.calm { background: rgba(0, 230, 118, 0.2); color: $success; }
    &.normal { background: rgba(0, 255, 255, 0.2); color: $primary; }
    &.warning { background: rgba(255, 152, 0, 0.2); color: $warning; }
    &.danger { background: rgba(244, 67, 54, 0.2); color: $danger; }
  }
}

// 风向样式
.compass-container {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 80px;
}

.compass-ring {
  position: relative;
  width: 80px;
  height: 80px;
  border: 2px solid $border;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(0, 255, 255, 0.1), transparent);
}

.cardinal-mark {
  position: absolute;
  font-size: 12px;
  font-weight: bold;
  color: $primary;
  
  &.north { top: -6px; left: 50%; transform: translateX(-50%); }
  &.east { right: -6px; top: 50%; transform: translateY(-50%); }
  &.south { bottom: -6px; left: 50%; transform: translateX(-50%); }
  &.west { left: -6px; top: 50%; transform: translateY(-50%); }
}

.wind-pointer {
  position: absolute;
  top: 50%;
  left: 50%;
  transform-origin: center;
  transition: transform 0.8s ease;
  width: 2px;
  height: 30px;
  background: linear-gradient(180deg, #ff6b35, rgba(255, 107, 53, 0.7));
  margin-left: -1px;
  margin-top: -15px;
  
  &::before {
    content: '';
    position: absolute;
    top: -6px;
    left: 50%;
    transform: translateX(-50%);
    width: 0;
    height: 0;
    border-left: 4px solid transparent;
    border-right: 4px solid transparent;
    border-bottom: 10px solid #ff6b35;
  }
}

.center-dot {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 6px;
  height: 6px;
  background: $primary;
  border-radius: 50%;
}

.direction-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
  font-size: 16px;
  
  .direction {
    color: $primary;
    font-weight: bold;
  }
  
  .angle {
    color: rgba(255, 255, 255, 0.7);
  }
}

// 统计样式 - 全新美观设计（修复溢出）
.statistics {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  margin-top: 4px;
  overflow: hidden;
}

.stat-item {
  background: linear-gradient(135deg, rgba(0, 255, 255, 0.08), rgba(0, 255, 255, 0.02));
  border: 1px solid rgba(0, 255, 255, 0.15);
  border-radius: 10px;
  padding: 10px 12px;
  display: flex;
  align-items: center;
  gap: 10px;
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
  overflow: hidden;
  min-width: 0;
  
  &:hover {
    background: linear-gradient(135deg, rgba(0, 255, 255, 0.15), rgba(0, 255, 255, 0.05));
    border-color: rgba(0, 255, 255, 0.3);
    transform: translateY(-2px);
    box-shadow: 0 4px 20px rgba(0, 255, 255, 0.1);
  }
  
  .stat-icon {
    font-size: 22px;
    line-height: 1;
    opacity: 0.9;
    flex-shrink: 0;
  }
  
  .stat-content {
    display: flex;
    flex-direction: column;
    flex: 1;
    min-width: 0;
    overflow: hidden;
  }
  
  .label {
    font-size: 11px;
    color: rgba(255, 255, 255, 0.5);
    letter-spacing: 0.5px;
    text-transform: uppercase;
    margin-bottom: 1px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
  
  .value {
    font-size: 17px;
    font-weight: 700;
    color: #ffffff;
    line-height: 1.2;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    
    small {
      font-size: 11px;
      font-weight: 400;
      color: rgba(255, 255, 255, 0.4);
      margin-left: 2px;
    }
    
    &.calm { color: #00e676; }
    &.normal { color: #00ffff; }
    &.warning { color: #ff9800; }
    &.danger { color: #f44336; }
    
    &.time {
      font-size: 13px;
      font-weight: 500;
      color: rgba(255, 255, 255, 0.8);
    }
  }
}

// 响应式
@media (max-width: 900px) {
  .wind-data {
    grid-template-columns: 1fr;
  }
  
  .statistics {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 600px) {
  .statistics {
    grid-template-columns: 1fr;
  }
  
  .stat-item {
    padding: 10px 12px;
    
    .stat-icon {
      font-size: 20px;
    }
    
    .value {
      font-size: 16px;
      
      &.time {
        font-size: 12px;
      }
    }
  }
}
</style>