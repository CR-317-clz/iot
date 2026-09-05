<template>
  <div class="left-bottom-bar-chart">
    <div class="chart-title">
      <svg class="chart-title-icon" width="24px" height="24px" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg">
        <path fill="#25d2ff" d="M516.075043 664.814099V475.033542c0-28.525298-22.703809-51.229107-51.229108-51.229108s-51.229107 22.703809-51.229107 51.229108v189.780557c-33.764639 19.210915-55.886299 56.468448-52.393406 98.383172 3.492894 49.482661 43.079022 89.650938 91.979534 95.472428 62.872086 6.985787 115.847641-41.914724 115.847641-103.622513 0-38.421831-21.539511-72.18647-52.975554-90.233087z" />
        <path fill="#25d2ff" d="M676.166003 214.812962c0-57.050597-22.12166-110.026151-62.289937-150.194429C574.872086 25.614554 523.642979 4.075043 468.920978 2.328596 413.034679 1.164298 359.476976 22.12166 318.1444 62.289937 275.065378 104.204662 250.615122 161.837408 250.615122 221.2166v367.335987c-44.24332 57.050597-64.036384 129.819215-54.139852 202.587834 16.300171 121.086981 116.42979 217.723707 238.09892 230.530983 9.314383 1.164298 18.628766 1.746447 27.943149 1.746447 66.36498 0 131.565662-24.450256 181.048323-69.275725 56.468448-51.229107 89.068789-123.997726 89.068789-200.259238 0-59.961342-19.793064-118.176236-56.468448-165.330301V214.812962zM574.872086 877.880614c-35.511086 32.018192-81.500853 46.571916-129.237066 41.914724-75.097214-7.567936-136.805003-67.529278-147.283684-142.044343-6.403638-48.900512 6.985787-96.054576 39.00398-132.72996 9.896532-11.642979 15.718022-26.778852 15.718021-41.914725V221.2166c0-32.018192 13.389426-62.872086 36.675384-85.575895 20.957362-20.375213 46.571916-30.853894 73.932916-30.853894h2.328596c28.525298 0.582149 54.722001 12.225128 75.097214 32.018192 20.957362 20.957362 32.018192 48.318363 32.018193 78.007959v388.293348c0 15.718022 5.821489 30.271745 15.718021 41.914725 26.196703 30.271745 40.750426 68.693576 40.750427 108.861853 0.582149 47.154065-19.793064 92.561683-54.722002 123.997726zM714.587834 306.792496h118.176236v102.458215H714.587834zM714.587834 157.180216h118.176236v102.458215H714.587834z" />
        <path fill="#25d2ff" d="M714.587834 456.404775h118.176236V558.86299H714.587834z" />
      </svg>
      <span class="chart-title-text">每周平均温度</span>
      <dv-decoration-1 style="width: 120px; height: 28px; margin-left: 8px" />
    </div>
    <div class="chart-content">
      <v-chart
        v-if="chartOptions"
        :option="chartOptions"
        autoresize
        style="height: 100%; width: 100%"
      />
    </div>
  </div>
</template>

<script>
import VChart from "vue-echarts";
import { getDailyAverageTemperature } from '@/api/iot-telemetry';

export default {
  name: "LeftBottomBarChart",
  components: { VChart },
  data() {
    return {
      deviceId: '374750517349453824', // 摄像头设备ID
      updateTimer: null,
      autoRefresh: true,
      updateInterval: 5 * 60 * 1000, // 5分钟刷新一次
      categories: [],
      temperatureData: [],
      loading: false,
      error: null,
    };
  },
  mounted() {
    this.loadTemperatureData();
    this.toggleAutoUpdate(true);
  },
  beforeDestroy() {
    this.stopAutoUpdate();
  },
  computed: {
    chartOptions() {
      if (!this.temperatureData.length) return null;
      return {
        backgroundColor: "rgba(0,0,0,0)",
        tooltip: {
          trigger: "axis",
          axisPointer: { type: "shadow" },
          backgroundColor: "rgba(10,24,50,0.85)",
          borderColor: "rgb(37,125,255)",
          textStyle: { color: "#fff" },
          formatter: params => {
            const value = params[0].value;
            const displayValue = value !== null && value !== undefined ? `${value}°C` : '暂无数据';
            return `<div style="padding: 8px;">
              <div style="color: #25d2ff; font-weight: bold; margin-bottom: 4px;">${params[0].name}</div>
              <div style="color: #fff;">
                <span style="display: inline-block; width: 10px; height: 10px; background: #25d2ff; border-radius: 50%; margin-right: 6px;"></span>
                温度: ${displayValue}
              </div>
            </div>`;
          }
        },
        grid: {
          left: "8%",
          right: "8%",
          top: "20%",
          bottom: "5%",
          containLabel: true,
        },
        xAxis: {
          type: "category",
          data: this.categories,
          axisLine: {
            lineStyle: {
              color: "rgb(37,125,255)",
            },
          },
          axisTick: { show: false },
          axisLabel: {
            color: "#b7e3ff",
            fontWeight: "bold",
            fontSize: 14,
          },
        },
        yAxis: {
          type: "value",
          name: "温度(°C)",
          nameTextStyle: { 
            color: "#b7e3ff", 
            fontSize: 14, 
            padding: [0, 0, 16, 0] 
          },
          axisLine: {
            lineStyle: {
              color: "rgb(37,125,255)",
            },
          },
          splitLine: {
            lineStyle: {
              color: "rgba(37,125,255,0.12)",
            },
          },
          axisLabel: {
            color: "#b7e3ff",
            fontWeight: "bold",
            fontSize: 14,
          },
        },
        series: [
          {
            name: "每日平均温度",
            type: "bar",
            data: this.temperatureData,
            barWidth: "40%",
            itemStyle: {
              color: {
                type: "linear",
                x: 0,
                y: 0,
                x2: 0,
                y2: 1,
                colorStops: [
                  { offset: 0, color: "#25d2ff" },
                  { offset: 1, color: "#1a5cd7" },
                ],
              },
              borderRadius: [8, 8, 0, 0],
              shadowColor: "rgba(37,125,255,0.18)",
              shadowBlur: 12,
            },
            label: {
              show: true,
              position: "top",
              color: "#fff",
              fontWeight: "bold",
              fontSize: 14,
              textShadowColor: "#25d2ff",
              textShadowBlur: 8,
              formatter: '{c}°C'
            },
          },
        ],
      };
    },
  },
  methods: {
    // 加载真实温度数据
    async loadTemperatureData() {
      this.loading = true;
      this.error = null;
      
      try {
        console.log('正在获取每日平均温度数据，设备ID:', this.deviceId);
        
        const response = await getDailyAverageTemperature(this.deviceId);
        
        console.log('温度API响应:', response);
        
        if (response?.data?.code === 200 && response?.data?.rows) {
          const rows = response.data.rows;
          
          // 提取日期标签和温度数据
          this.categories = rows.map(item => item.dayChinese);
          this.temperatureData = rows.map(item => {
            const temp = parseFloat(item.avgTemperature);
            // 如果温度为0且记录数为0，则显示为null（不显示柱子）
            return parseInt(item.recordCount) > 0 ? parseFloat(temp.toFixed(1)) : null;
          });
          
          console.log('解析后的温度数据:', {
            categories: this.categories,
            temperatureData: this.temperatureData
          });
          
        } else {
          throw new Error(response?.data?.msg || '获取温度数据失败');
        }
      } catch (error) {
        console.error('加载温度数据失败:', error);
        this.error = error.message || '获取温度数据失败';
        
        // 使用模拟数据作为后备
        this.loadMockTemperatureData();
      } finally {
        this.loading = false;
      }
    },
    
    // 模拟数据（作为后备）
    loadMockTemperatureData() {
      console.log('使用模拟温度数据');
      this.categories = ['周一', '周二', '周三', '周四', '周五', '周六', '周日'];
      this.temperatureData = this.categories.map(() => +(Math.random() * 10 + 18).toFixed(1));
    },
    
    toggleAutoUpdate(start = true) {
      this.stopAutoUpdate();
      if (start && this.autoRefresh) {
        this.updateTimer = setInterval(this.loadTemperatureData, this.updateInterval);
      }
    },
    
    stopAutoUpdate() {
      if (this.updateTimer) {
        clearInterval(this.updateTimer);
        this.updateTimer = null;
      }
    }
  }
};
</script>

<style scoped>
.left-bottom-bar-chart {
  width: 100%;
  height: 100%;
  border-radius: 18px;
  padding: 0;
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.chart-title {
  position: absolute;
  top: 18px;
  left: 24px;
  color: #fff;
  font-size: 20px;
  font-weight: bold;
  letter-spacing: 2px;
  z-index: 2;
  pointer-events: none;
  line-height: 1.2;
  display: flex;
  align-items: center;
  padding-right: 12px;
  border-left: 4px solid #25d2ff;
  border-radius: 8px;
}

.chart-title-icon {
  width: 24px;
  height: 24px;
  margin-right: 8px;
  filter: drop-shadow(0 2px 8px #25d2ff55);
}

.chart-title-text {
  font-size: 20px;
  font-weight: bold;
  letter-spacing: 2px;
  margin-right: 8px;
}

.chart-content {
  width: 100%;
  height: 100%;
  flex: 1;
  box-sizing: border-box;
  padding: 50px 10px 8px 10px; /* 减少顶部内边距 */
}
</style>
