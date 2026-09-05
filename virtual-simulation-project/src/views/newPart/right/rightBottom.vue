<template>
  <div class="right-bottom-chart-container">
    <div class="chart-title">
      <i class="el-icon-s-data chart-title-icon" />
      <span class="chart-title-text">每周平均湿度</span>
      <dv-decoration-1 style="width: 120px; height: 28px; margin-left: 8px" />
    </div>
    <div class="chart-content">
      <v-chart
        v-if="option"
        :option="option"
        autoresize
        style="height: 100%; width: 100%"
      />
    </div>
  </div>
</template>

<script>
import VChart from "vue-echarts";
import { getDailyAverageHumidity } from "@/api/iot-telemetry";

export default {
  name: "RightBottomChart",
  components: { VChart },
  data() {
    return {
      option: null,
      updateTimer: null,
      autoRefresh: true, // 是否自动刷新
      updateInterval: 5 * 60 * 1000, // 刷新间隔，默认5分钟
      categories: [],
      humidityData: [],
      loading: false,
      error: null,
    };
  },
  
  computed: {
    // 从 store 获取 deviceId
    deviceId() {
      const storeDeviceId = this.$store?.getters?.getDeviceId
      console.log('🔍 每日平均湿度-当前设备ID:', storeDeviceId)
      return storeDeviceId || '374750517349453824' // 使用默认值作为兜底
    }
  },
  
  mounted() {
    this.loadHumidityData();
    this.toggleAutoUpdate(true);
  },

  beforeDestroy() {
    this.stopAutoUpdate();
  },
  
  watch: {
    // 监听 deviceId 变化，重新加载数据
    deviceId(newDeviceId, oldDeviceId) {
      if (newDeviceId && newDeviceId !== oldDeviceId) {
        console.log('🔄 每日平均湿度-检测到设备ID变化:', { 旧值: oldDeviceId, 新值: newDeviceId })
        this.loadHumidityData() // 重新获取数据
      }
    }
  },

  methods: {
    // 加载真实湿度数据
    async loadHumidityData() {
      this.loading = true;
      this.error = null;

      try {
        const response = await getDailyAverageHumidity(this.deviceId);
        console.log("每日平均湿度数据:", response);

        // 检查响应结构: response.data.rows
        if (response && response.data && response.data.rows && Array.isArray(response.data.rows)) {
          const data = response.data.rows;

          // 提取日期和湿度数据
          // dayChinese: 星期一, 星期二等
          // avgTemperature: 湿度值(虽然字段名是avgTemperature,但实际存储的是湿度值)
          this.categories = data.map((item) => item.dayChinese || "未知");
          this.humidityData = data.map((item) => {
            const value = parseFloat(item.avgTemperature);
            // 显示所有有效数字，包括0（湿度为0%是有效值）
            return !isNaN(value) && value >= 0 ? parseFloat(value.toFixed(1)) : null;
          });

          console.log("解析的日期:", this.categories);
          console.log("解析的湿度数据:", this.humidityData);
        } else {
          console.warn("湿度数据格式异常:", response);
          this.error = "数据格式错误";
          this.categories = [];
          this.humidityData = [];
        }
      } catch (error) {
        console.error("加载湿度数据失败:", error);
        this.error = error.message || "加载失败";
        this.categories = [];
        this.humidityData = [];
      } finally {
        this.loading = false;
        this.updateChart();
      }
    },

    // 启动/停止定时更新
    toggleAutoUpdate(start = true) {
      this.stopAutoUpdate();
      if (start && this.autoRefresh) {
        this.updateTimer = setInterval(
          this.loadHumidityData,
          this.updateInterval
        );
      }
    },
    stopAutoUpdate() {
      if (this.updateTimer) {
        clearInterval(this.updateTimer);
        this.updateTimer = null;
      }
    },
    updateChart() {
      this.option = {
        tooltip: {
          trigger: "axis",
          axisPointer: { type: "shadow" },
          backgroundColor: "rgba(10,24,50,0.85)",
          borderColor: "#25d2ff",
          borderWidth: 1,
          textStyle: { color: "#fff", fontWeight: "bold" },
          formatter: (params) => {
            const value = params[0].value;
            const displayValue = value !== null && value !== undefined ? `${value}%` : '暂无数据';
            let result = `<div style="padding: 8px;">`;
            result += `<div style="color: #25d2ff; font-weight: bold; margin-bottom: 4px;">${params[0].name}</div>`;
            result += `<div style="color: #fff; margin: 2px 0;">`;
            result += `<span style="display: inline-block; width: 10px; height: 10px; background: #25d2ff; border-radius: 50%; margin-right: 6px;"></span>`;
            result += `湿度: ${displayValue}`;
            result += `</div>`;
            result += `</div>`;
            return result;
          },
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
          axisLine: { lineStyle: { color: "#25d2ff" } },
          axisTick: { show: false },
          axisLabel: { color: "#b7e3ff", fontWeight: "bold", fontSize: 14 },
        },
        yAxis: {
          type: "value",
          name: "湿度(%)",
          nameTextStyle: {
            color: "#b7e3ff",
            fontSize: 14,
            padding: [0, 0, 16, 0],
          },
          axisLine: { lineStyle: { color: "#25d2ff" } },
          splitLine: { lineStyle: { color: "rgba(37,125,255,0.12)" } },
          axisLabel: { color: "#b7e3ff", fontWeight: "bold", fontSize: 14 },
        },
        series: [
          {
            name: "每日平均湿度",
            type: "line",
            smooth: true,
            showAllSymbol: true,
            symbol: "circle",
            symbolSize: 12,
            lineStyle: { color: "#25d2ff", width: 4 },
            itemStyle: {
              color: "#fff",
              borderColor: "#25d2ff",
              borderWidth: 3,
            },
            label: {
              show: true,
              position: "top",
              color: "#fff",
              fontWeight: "bold",
              fontSize: 14,
              textShadowColor: "#25d2ff",
              textShadowBlur: 8,
              formatter: "{c}%",
            },
            data: this.humidityData,
            animationDelay: (idx) => idx * 100,
            animationDuration: 1000,
            animationEasing: "elasticOut",
          },
        ],
      };
    },
  },
};
</script>

<style scoped>
.right-bottom-chart-container {
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
  font-size: 24px;
  color: #25d2ff;
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
