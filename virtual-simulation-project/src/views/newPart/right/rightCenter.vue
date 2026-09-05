<template>
  <div class="left-bottom-bar-chart">
    <!-- 标题栏 -->
    <header class="header">
      <div class="title-container">
        <div class="icon-wrapper">
          <svg class="header-icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg">
            <path fill="#25d2ff" d="M772.338983 138.847458a251.661017 251.661017 0 0 0-37.402034 2.776949A321.084746 321.084746 0 0 0 156.20339 247.842712 199.59322 199.59322 0 0 0 0 442.576271a199.59322 199.59322 0 0 0 199.59322 199.593221h572.745763a251.661017 251.661017 0 0 0 0-503.322034zM489.524068 513.041356a174.600678 174.600678 0 0 1-174.08-174.08 89.64339 89.64339 0 0 1 2.169491-23.951187l43.389831 43.389831a129.214915 129.214915 0 0 0 128.520678 111.077966 130.169492 130.169492 0 0 0 0-261.12 134.248136 134.248136 0 0 0-126.264407 91.465763l-15.186441-15.273221L271.880678 208.271186h60.745763l21.781695 21.781695a173.559322 173.559322 0 1 1 134.942372 282.901695z m65.258305-126.177627l-30.459661 30.459661-56.580339-56.580339V251.661017h43.38983v91.639322zM112.813559 694.237288h69.423729v190.915254h-69.423729zM295.050847 754.983051h69.423729v190.915254h-69.423729zM477.288136 833.084746h69.423728v190.915254h-69.423728zM659.525424 763.661017h69.423729v190.915254h-69.423729zM841.762712 694.237288h69.423729v190.915254h-69.423729z" />
          </svg>
        </div>
        <h2 class="title">环境监测数据</h2>
        <div class="title-decoration"></div>
      </div>
      <div class="header-actions">
        <div class="refresh-btn" @click="loadEnvironmentData" :class="{ loading: loading }">
          <i class="el-icon-refresh" :class="{ rotating: loading }"></i>
        </div>
      </div>
    </header>

    <div v-if="error && !loading" class="error-tip">
      <i class="el-icon-warning"></i>
      <span>{{ error }}</span>
    </div>

    <!-- 图表区域 -->
    <div class="chart-wrapper" ref="chartWrapper">
      <v-chart
        ref="chartRef"
        :option="chartOptions"
        autoresize
      />
    </div>

    <!-- 底部滚动Banner面板 -->
    <div class="bottom-banner">
      <div class="banner-track" ref="bannerTrack">
        <div class="banner-item" v-for="(item, index) in stockData" :key="index">
          <span class="banner-label">{{ item.label }}</span>
          <span class="banner-value" :style="{ color: item.color }">
            {{ item.value }}
            <span class="banner-unit">{{ item.unit }}</span>
          </span>
          <span class="banner-change" :class="item.change >= 0 ? 'up' : 'down'">
            <span v-if="item.change !== null">
              {{ item.change >= 0 ? '▲' : '▼' }} {{ Math.abs(item.change).toFixed(1) }}%
            </span>
            <span v-else style="color: rgba(255,255,255,0.2);">--</span>
          </span>
          <span class="banner-divider">|</span>
        </div>
        <!-- 复制一份用于无缝滚动 -->
        <div class="banner-item" v-for="(item, index) in stockData" :key="'copy-' + index">
          <span class="banner-label">{{ item.label }}</span>
          <span class="banner-value" :style="{ color: item.color }">
            {{ item.value }}
            <span class="banner-unit">{{ item.unit }}</span>
          </span>
          <span class="banner-change" :class="item.change >= 0 ? 'up' : 'down'">
            <span v-if="item.change !== null">
              {{ item.change >= 0 ? '▲' : '▼' }} {{ Math.abs(item.change).toFixed(1) }}%
            </span>
            <span v-else style="color: rgba(255,255,255,0.2);">--</span>
          </span>
          <span class="banner-divider">|</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import VChart from "vue-echarts";
import axios from "axios";

export default {
  name: "RightCenterBarChart",
  components: { VChart },
  props: {
    autoRefresh: { type: Boolean, default: true },
  },
  data() {
    return {
      categories: [],
      values: [],
      loading: false,
      error: null,
      updateTimer: null,
      previousValues: {},
      stockData: [],
      bannerTimer: null,
      colorMap: {
        '温度': '#ffa726',
        '湿度': '#42a5f5',
        '光照度': '#fbc02d',
        '风速': '#66bb6a',
        '风向': '#ab47bc',
        '土壤温度': '#ff7043',
        '土壤水分': '#26c6da',
        '土壤电导率': '#8d6e63'
      }
    };
  },

  computed: {
    deviceId() {
      const storeDeviceId = this.$store?.getters?.getDeviceId
      return storeDeviceId || '374750517349453824'
    },
    
    chartOptions() {
      const colors = this.categories.map(key => this.colorMap[key] || '#25d2ff');
      
      return {
        backgroundColor: "rgba(0,0,0,0)",
        tooltip: {
          trigger: "axis",
          backgroundColor: "rgba(10,24,50,0.92)",
          borderColor: "rgba(37,210,255,0.4)",
          borderWidth: 1,
          textStyle: { color: "#fff", fontSize: 13 },
          formatter: (params) => {
            const p = params[0];
            const value = p.value;
            const displayValue = value !== null && value !== undefined ? value : '暂无数据';
            const unit = this.getUnit(p.name);
            const change = this.getChange(p.name, value);
            const changeText = change !== null ? (change >= 0 ? `▲ +${change.toFixed(1)}%` : `▼ ${Math.abs(change).toFixed(1)}%`) : '--';
            const changeColor = change >= 0 ? '#ef5350' : '#26c6da';
            return `
              <div style="padding: 10px 14px; min-width: 160px;">
                <div style="color: #25d2ff; font-weight: bold; font-size: 15px; margin-bottom: 6px;">${p.name}</div>
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 4px;">
                  <span style="color: rgba(255,255,255,0.6);">当前值</span>
                  <span style="color: #fff; font-weight: bold; font-size: 18px;">${displayValue} <span style="font-size: 13px; color: rgba(255,255,255,0.4);">${unit}</span></span>
                </div>
                <div style="display: flex; justify-content: space-between; align-items: center; border-top: 1px solid rgba(255,255,255,0.06); padding-top: 4px;">
                  <span style="color: rgba(255,255,255,0.6);">涨跌幅</span>
                  <span style="color: ${changeColor}; font-weight: bold; font-size: 14px;">${changeText}</span>
                </div>
              </div>`;
          },
        },
        grid: {
          left: "6%",
          right: "6%",
          top: "10%",
          bottom: "10%",
          containLabel: true,
        },
        xAxis: {
          type: "category",
          data: this.categories,
          axisLine: { lineStyle: { color: "rgba(37,125,255,0.12)" } },
          axisLabel: { 
            color: "rgba(255,255,255,0.5)", 
            fontWeight: "500", 
            fontSize: 12,
            interval: 0,
            rotate: 0
          },
          axisTick: { show: false },
        },
        yAxis: {
          type: "value",
          name: "数值",
          nameTextStyle: { color: "rgba(255,255,255,0.25)", fontSize: 11 },
          axisLine: { show: false },
          splitLine: { lineStyle: { color: "rgba(37,125,255,0.05)" } },
          axisLabel: { color: "rgba(255,255,255,0.35)", fontSize: 11 },
        },
        series: [
          {
            name: "环境数据",
            type: "line",
            data: this.values,
            smooth: true,
            symbol: 'circle',
            symbolSize: 8,
            lineStyle: {
              width: 3,
              color: '#25d2ff',
              shadowColor: 'rgba(37,210,255,0.25)',
              shadowBlur: 12,
            },
            itemStyle: {
              color: (params) => {
                return colors[params.dataIndex] || '#25d2ff';
              },
              borderColor: 'rgba(255,255,255,0.8)',
              borderWidth: 2,
              shadowColor: 'rgba(37,210,255,0.2)',
              shadowBlur: 8,
            },
            areaStyle: {
              color: {
                type: 'linear',
                x: 0,
                y: 0,
                x2: 0,
                y2: 1,
                colorStops: [
                  { offset: 0, color: 'rgba(37,210,255,0.25)' },
                  { offset: 0.5, color: 'rgba(37,210,255,0.08)' },
                  { offset: 1, color: 'rgba(37,210,255,0.01)' }
                ]
              }
            },
            label: {
              show: true,
              position: "top",
              color: "rgba(255,255,255,0.6)",
              fontWeight: "600",
              fontSize: 10,
              textShadowColor: "rgba(0,0,0,0.6)",
              textShadowBlur: 6,
              formatter: (params) => {
                return params.value !== null && params.value !== undefined ? `${params.value}` : '';
              },
            },
            animationDelay: (idx) => idx * 100,
            animationEasing: "smoothOut",
            animationDuration: 1200,
          }
        ],
        ...(this.loading && {
          graphic: {
            elements: [
              {
                type: "text",
                left: "center",
                top: "center",
                style: {
                  text: "数据加载中...",
                  fontSize: 16,
                  fontWeight: "bold",
                  fill: "#25d2ff",
                },
              },
            ],
          },
        }),
      };
    },
  },

  methods: {
    getUnit(key) {
      const unitMap = {
        '温度': '°C',
        '湿度': '%',
        '光照度': 'lx',
        '风速': 'm/s',
        '风向': '',
        '土壤温度': '°C',
        '土壤水分': '%',
        '土壤电导率': 'μS/cm'
      };
      return unitMap[key] || '';
    },

    getChange(key, currentValue) {
      const prev = this.previousValues[key];
      if (prev !== undefined && prev !== null && prev !== 0 && currentValue !== null && currentValue !== 0) {
        return ((currentValue - prev) / Math.abs(prev)) * 100;
      }
      return null;
    },

    updateStockData() {
      const displayKeys = ['温度', '湿度', '光照度', '风速', '土壤温度', '土壤水分', '土壤电导率'];
      this.stockData = displayKeys.map((key, index) => {
        const value = this.values[index] || 0;
        const prev = this.previousValues[key];
        let change = null;
        if (prev !== undefined && prev !== null && prev !== 0 && value !== 0) {
          change = ((value - prev) / Math.abs(prev)) * 100;
        }
        const color = this.colorMap[key] || '#25d2ff';
        return {
          label: key,
          value: typeof value === 'number' ? value.toFixed(1) : value,
          unit: this.getUnit(key),
          change: change,
          color: color
        };
      });
    },

    // 启动Banner滚动
    startBannerScroll() {
      this.stopBannerScroll();
      const track = this.$refs.bannerTrack;
      if (!track) return;
      
      // 计算滚动速度
      let scrollPos = 0;
      const speed = 0.8;
      
      this.bannerTimer = setInterval(() => {
        scrollPos -= speed;
        // 当滚动到一半时重置
        const totalWidth = track.scrollWidth / 2;
        if (Math.abs(scrollPos) >= totalWidth) {
          scrollPos = 0;
        }
        track.style.transform = `translateX(${scrollPos}px)`;
      }, 30);
    },

    stopBannerScroll() {
      if (this.bannerTimer) {
        clearInterval(this.bannerTimer);
        this.bannerTimer = null;
      }
    },

    async loadEnvironmentData() {
      if (this.loading) return;
      
      this.loading = true;
      this.error = null;

      try {
        const deviceId = this.deviceId;
        console.log('📡 环境监测-请求数据，设备ID:', deviceId);
        
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
          console.warn('⚠️ 环境监测-无法识别的数据结构');
          this.generateMockData();
          this.loading = false;
          return;
        }

        if (!dataList || dataList.length === 0) {
          console.warn('⚠️ 环境监测-数据列表为空');
          this.generateMockData();
          this.loading = false;
          return;
        }

        const newPreviousValues = {};
        this.categories.forEach((key, index) => {
          newPreviousValues[key] = this.values[index];
        });
        this.previousValues = newPreviousValues;

        const displayKeys = ['温度', '湿度', '光照度', '风速', '土壤温度', '土壤水分', '土壤电导率'];
        const keyMap = {
          '温度': 'temperature',
          '湿度': 'humidity',
          '光照度': 'illuminance',
          '风速': 'windspeed',
          '土壤温度': 'soiltemperature',
          '土壤水分': 'soilmoisture',
          '土壤电导率': 'soilelectricalconductivity'
        };

        this.categories = [];
        this.values = [];

        displayKeys.forEach(key => {
          const apiKey = keyMap[key];
          const item = dataList.find(d => d.dataKey === apiKey);
          
          this.categories.push(key);
          
          if (item && item.dataValue !== undefined) {
            const value = parseFloat(item.dataValue);
            if (!isNaN(value)) {
              this.values.push(parseFloat(value.toFixed(2)));
            } else {
              this.values.push(0);
            }
          } else {
            this.values.push(0);
          }
        });

        this.updateStockData();
        
        // 数据更新后重启滚动
        this.$nextTick(() => {
          this.startBannerScroll();
        });

      } catch (error) {
        console.error('❌ 环境监测-加载数据失败:', error);
        this.error = error.message || '加载失败';
        this.generateMockData();
      } finally {
        this.loading = false;
      }
    },

    generateMockData() {
      const keys = ['温度', '湿度', '光照度', '风速', '土壤温度', '土壤水分', '土壤电导率'];
      
      const newPreviousValues = {};
      this.categories.forEach((key, index) => {
        newPreviousValues[key] = this.values[index];
      });
      this.previousValues = newPreviousValues;
      
      this.categories = keys;
      this.values = [
        parseFloat((20 + Math.random() * 10).toFixed(1)),
        parseFloat((40 + Math.random() * 30).toFixed(1)),
        parseFloat((300 + Math.random() * 700).toFixed(0)),
        parseFloat((0.5 + Math.random() * 2).toFixed(2)),
        parseFloat((18 + Math.random() * 12).toFixed(1)),
        parseFloat((30 + Math.random() * 40).toFixed(1)),
        parseFloat((500 + Math.random() * 1000).toFixed(0))
      ];
      
      this.updateStockData();
      
      this.$nextTick(() => {
        this.startBannerScroll();
      });
    },

    toggleAutoUpdate(start = true) {
      this.stopAutoUpdate();
      if (start && this.autoRefresh) {
        this.updateTimer = setInterval(this.loadEnvironmentData, 5000);
      }
    },

    stopAutoUpdate() {
      if (this.updateTimer) {
        clearInterval(this.updateTimer);
        this.updateTimer = null;
      }
    },
  },

  mounted() {
    this.loadEnvironmentData();
    this.toggleAutoUpdate(true);
  },

  beforeDestroy() {
    this.stopAutoUpdate();
    this.stopBannerScroll();
  },
  
  watch: {
    deviceId(newDeviceId, oldDeviceId) {
      if (newDeviceId && newDeviceId !== oldDeviceId) {
        this.loadEnvironmentData();
      }
    }
  }
};
</script>

<style scoped>
.left-bottom-bar-chart {
  width: 100%;
  height: 100%;
  position: relative;
  background: rgba(255,255,255,0.02);
  border-radius: 12px;
  border: 1px solid rgba(37, 125, 255, 0.06);
  overflow: hidden;
}

/* 标题栏 */
.header {
  height: 48px;
  min-height: 48px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  border-bottom: 1px solid rgba(37, 125, 255, 0.06);
  background: rgba(255, 255, 255, 0.01);
  position: relative;
}

.header::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 16px;
  width: 60px;
  height: 2px;
  background: linear-gradient(90deg, #25d2ff, rgba(37, 210, 255, 0.1));
  border-radius: 1px;
}

.title-container {
  display: flex;
  align-items: center;
  gap: 10px;
}

.icon-wrapper {
  width: 32px;
  height: 32px;
  background: rgba(37, 210, 255, 0.1);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.header-icon {
  width: 18px;
  height: 18px;
  flex-shrink: 0;
}

.title {
  font-size: 16px;
  font-weight: 700;
  color: #ffffff;
  margin: 0;
  letter-spacing: 0.5px;
  background: linear-gradient(135deg, #25d2ff, #4dd0e1);
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.title-decoration {
  width: 4px;
  height: 16px;
  background: linear-gradient(180deg, #25d2ff, transparent);
  border-radius: 2px;
  margin-left: 2px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.refresh-btn {
  width: 28px;
  height: 28px;
  flex-shrink: 0;
  background: rgba(37, 210, 255, 0.06);
  border: 1px solid rgba(37, 210, 255, 0.1);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.refresh-btn:hover {
  background: rgba(37, 210, 255, 0.15);
  transform: scale(1.05);
  border-color: rgba(37, 210, 255, 0.25);
}

.refresh-btn.loading {
  cursor: not-allowed;
  opacity: 0.5;
}

.refresh-btn i {
  font-size: 13px;
  color: #25d2ff;
}

.refresh-btn i.rotating {
  animation: rotate 1s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.error-tip {
  position: absolute;
  top: 54px;
  left: 12px;
  right: 12px;
  background: rgba(255, 77, 77, 0.08);
  border: 1px solid rgba(255, 77, 77, 0.12);
  border-radius: 4px;
  padding: 2px 10px;
  color: #ff6b6b;
  font-size: 11px;
  display: flex;
  align-items: center;
  gap: 4px;
  z-index: 3;
  height: 24px;
}

/* 图表区域 */
.chart-wrapper {
  position: absolute;
  top: 48px;
  left: 0;
  right: 0;
  bottom: 34px;
  padding: 4px 6px;
}

.chart-wrapper .v-chart {
  width: 100% !important;
  height: 100% !important;
}

/* 底部Banner - 滚动面板 */
.bottom-banner {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 34px;
  min-height: 34px;
  overflow: hidden;
  border-top: 1px solid rgba(37, 125, 255, 0.04);
  background: rgba(255, 255, 255, 0.015);
}

.banner-track {
  display: flex;
  align-items: center;
  height: 100%;
  white-space: nowrap;
  will-change: transform;
  padding: 0 4px;
}

.banner-item {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 0 8px;
  flex-shrink: 0;
  font-size: 12px;
}

.banner-label {
  color: rgba(255, 255, 255, 0.35);
  font-weight: 500;
  font-size: 10px;
}

.banner-value {
  font-size: 13px;
  font-weight: 700;
  color: #fff;
}

.banner-unit {
  font-size: 8px;
  font-weight: 400;
  color: rgba(255, 255, 255, 0.2);
  margin-left: 1px;
}

.banner-change {
  font-size: 10px;
  font-weight: 600;
  min-width: 28px;
}

.banner-change.up {
  color: #ef5350;
}
.banner-change.down {
  color: #26c6da;
}

.banner-divider {
  color: rgba(255, 255, 255, 0.06);
  font-size: 14px;
  margin-left: 4px;
}

/* 响应式 */
@media (max-width: 768px) {
  .header {
    height: 40px;
    min-height: 40px;
    padding: 0 12px;
  }
  .header::after {
    left: 12px;
    width: 40px;
  }
  .title {
    font-size: 14px;
  }
  .icon-wrapper {
    width: 26px;
    height: 26px;
  }
  .header-icon {
    width: 14px;
    height: 14px;
  }
  .title-decoration {
    width: 3px;
    height: 12px;
  }
  .bottom-banner {
    height: 28px;
    min-height: 28px;
  }
  .chart-wrapper {
    top: 40px;
    bottom: 28px;
  }
  .refresh-btn {
    width: 24px;
    height: 24px;
  }
  .refresh-btn i {
    font-size: 11px;
  }
  .banner-item {
    font-size: 10px;
    padding: 0 5px;
    gap: 3px;
  }
  .banner-value {
    font-size: 11px;
  }
  .banner-label {
    font-size: 8px;
  }
  .banner-change {
    font-size: 8px;
    min-width: 22px;
  }
  .banner-divider {
    font-size: 10px;
  }
  .error-tip {
    top: 46px;
  }
}

@media (max-height: 500px) {
  .header {
    height: 32px;
    min-height: 32px;
  }
  .title {
    font-size: 12px;
  }
  .icon-wrapper {
    width: 22px;
    height: 22px;
  }
  .header-icon {
    width: 12px;
    height: 12px;
  }
  .title-decoration {
    display: none;
  }
  .bottom-banner {
    height: 24px;
    min-height: 24px;
  }
  .chart-wrapper {
    top: 32px;
    bottom: 24px;
  }
  .banner-item {
    font-size: 9px;
    padding: 0 4px;
    gap: 2px;
  }
  .banner-value {
    font-size: 10px;
  }
  .banner-label {
    font-size: 7px;
  }
  .banner-change {
    font-size: 7px;
    min-width: 18px;
  }
  .banner-divider {
    font-size: 8px;
  }
  .refresh-btn {
    width: 20px;
    height: 20px;
  }
  .refresh-btn i {
    font-size: 10px;
  }
  .error-tip {
    top: 38px;
    height: 20px;
    font-size: 10px;
  }
}
</style>