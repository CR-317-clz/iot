<template>
  <div class="left-center-chart">
    <div class="chart-title">
      <i class="el-icon-s-grid chart-title-icon" />
      <span class="chart-title-text">十类传感器实时数据</span>
      <dv-decoration-1 style="width: 140px; height: 32px; margin-left: 10px" />
    </div>
    <div class="sensor-list">
      <div v-for="sensor in sensors" :key="sensor.name" class="sensor-item">
        <div class="sensor-icon-box"></div>
        <div class="sensor-info">
          <span class="sensor-name">{{ sensor.name }}</span>
          <span class="sensor-value">{{ sensor.value }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'LeftCenterSensorList',
  data() {
    return {
      sensors: []
    };
  },
  methods:{
    getSensorValue(cfg) {
      switch (cfg.name) {
        case '土壤湿度':
        case '空气湿度':
          return Math.floor(Math.random() * 40 + 40); // 40~80%
        case '土壤温度':
        case '温度':
          return (Math.random() * 15 + 10).toFixed(1); // 10~25℃
        case '土壤导电':
          return (Math.random() * 1500 + 500).toFixed(0); // 500~2000μS/cm
        case '紫外线':
          return (Math.random() * 5).toFixed(2); // 0~5
        case '二氧化碳':
          return (Math.random() * 600 + 400).toFixed(0); // 400~1000ppm
        case '光照':
          return (Math.random() * 80000 + 20000).toFixed(0); // 20000~100000lx
        case '一氧化碳':
          return (Math.random() * 10).toFixed(2); // 0~10ppm
        case 'PH':
          return (Math.random() * 4 + 4).toFixed(2); // 4~8
        default:
          return '--';
      }
    }
  },
  created() {
    // 图标可根据你的 iconfont 或 svg 库自定义
    // 传感器配置（名称、图标、单位）
    const sensorConfig = [
      { name: '土壤湿度', icon: 'default', unit: '%' },
      { name: '土壤温度', icon: 'default', unit: '℃' },
      { name: '紫外线', icon: 'default', unit: '' },
      { name: '二氧化碳', icon: 'default', unit: 'ppm' },
      { name: '光照', icon: 'default', unit: 'lx' },
      { name: '空气湿度', icon: 'default', unit: '%' },
      { name: '温度', icon: 'default', unit: '℃' },
      { name: '一氧化碳', icon: 'default', unit: 'ppm' },
    ];

    this.sensors = sensorConfig.map(cfg => ({
      name: cfg.name,
      icon: cfg.icon,
      value: this.getSensorValue(cfg) + (cfg.unit ? cfg.unit : ''),
    }));
  }
};
</script>

<style scoped>
.left-center-chart {
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
  font-size: 26px;
  color: rgb(37, 125, 255);
  margin-right: 8px;
  filter: drop-shadow(0 2px 8px rgba(37, 125, 255, 0.3));
}

.chart-title-text {
  font-size: 22px;
  font-weight: bold;
  letter-spacing: 2px;
  margin-right: 8px;
}

.sensor-list {
  width: 100%;
  min-height: 0;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px 22px;
  padding: 60px 28px 28px 28px;
  box-sizing: border-box;
}

.sensor-item {
  background: linear-gradient(
    135deg,
    rgba(37, 125, 255, 0.18) 0%,
    rgba(23, 84, 175, 0.514) 100%
  );
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 22px 12px 18px 12px;
  position: relative;
  transition: box-shadow 0.2s, transform 0.2s;
  min-width: 0;
  overflow: hidden;
}
.sensor-item:hover {
  box-shadow: 0 12px 36px rgba(37, 125, 255, 0.22),
    0 0 0 2px rgba(37, 125, 255, 0.18);
  transform: translateY(-4px) scale(1.06);
}
.sensor-icon-box {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: radial-gradient(
    circle at 60% 40%,
    rgba(37, 125, 255, 0.22) 0%,
    rgba(37, 125, 255, 0.08) 100%
  );
  border-radius: 50%;
  margin-bottom: 10px;
  box-shadow: 0 4px 16px rgba(37, 125, 255, 0.18);
  border: 1.5px solid rgba(37, 125, 255, 0.18);
}
.sensor-icon {
  font-size: 28px;
  color: rgb(37, 125, 255);
  filter: drop-shadow(0 2px 8px rgba(37, 125, 255, 0.3));
  transition: color 0.2s;
}
.sensor-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-width: 0;
}
.sensor-name {
  font-size: 15px;
  font-weight: bold;
  color: #fff;
  margin-bottom: 6px;
  letter-spacing: 1.5px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  text-shadow: 0 2px 8px rgba(37, 125, 255, 0.12);
}
.sensor-value {
  font-size: 18px;
  font-weight: bold;
  color: #fff;
  background: linear-gradient(90deg, rgb(37, 125, 255) 0%, #fff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  text-shadow: 0 2px 16px rgba(37, 125, 255, 0.18);
  word-break: break-all;
}
</style>
