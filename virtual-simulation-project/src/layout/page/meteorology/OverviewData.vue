<template>
  <div class="meteorology-overview-data">
    <!-- 页面头部 -->
    <el-card class="page-header-card">
      <div slot="header" class="page-header">
        <div class="header-left">
          <el-button type="text" icon="el-icon-arrow-left" @click="goBack" class="back-btn">
            返回
          </el-button>
          <span class="page-title">
            <i class="el-icon-sunny"></i>
            {{ deviceName }} - 气象数据总览
          </span>
          <p class="page-description">实时监测气象环境数据</p>
        </div>
        <div class="header-right">
          <el-button type="primary" icon="el-icon-refresh" @click="refreshData">
            刷新数据
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 统计概览 -->
    <el-row :gutter="20" class="mb-20">
      <el-col :span="6" v-for="stat in statistics" :key="stat.key">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" :style="{ backgroundColor: stat.color }">
              <i :class="stat.icon"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stat.value }}<span class="stat-unit">{{ stat.unit }}</span></div>
              <div class="stat-label">{{ stat.label }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 环境数据图表 -->
    <el-row :gutter="20" class="mb-20">
      <el-col :span="24">
        <el-card class="chart-card">
          <div slot="header" class="chart-header">
            <span>环境数据趋势</span>
          </div>
          <div class="chart-container">
            <div class="chart-placeholder">
              <i class="el-icon-data-line"></i>
              <p>暂无数据</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 实时气象数据 -->
    <el-row :gutter="20" class="mb-20">
      <el-col :span="24">
        <el-card class="data-card">
          <div slot="header">
            <span>实时气象数据</span>
          </div>
          <div class="data-grid">
            <div v-for="item in realTimeData" :key="item.registerId + '_' + item.nodeId" class="data-grid-item">
              <div class="data-card-inner">
                <div class="data-icon" :class="getItemStatusClass(item)">
                  <i :class="getItemIcon(item.registerName)"></i>
                </div>
                <div class="data-info">
                  <div class="data-label">{{ item.registerName }}</div>
                  <div class="data-value">
                    {{ formatValue(item.valueNum, item.dataValue) }}<span class="data-unit">{{ item.unit }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { getMeteorologyDeviceDetail } from "@/api/iot-meteorology.js";

export default {
  name: 'MeteorologyOverviewData',
  components: {
  },
  data() {
    return {
      deviceName: '',
      deviceId: '',
      deviceAddr: '',
      
      // 统计数据
      statistics: [
        {
          key: 'temperature',
          label: '环境温度',
          value: '-',
          unit: '°C',
          icon: 'el-icon-sunny',
          color: '#409EFF'
        },
        {
          key: 'humidity',
          label: '环境湿度',
          value: '-',
          unit: '%RH',
          icon: 'el-icon-water',
          color: '#67C23A'
        },
        {
          key: 'pressure',
          label: '大气压力',
          value: '-',
          unit: 'kPa',
          icon: 'el-icon-guide',
          color: '#E6A23C'
        },
        {
          key: 'wind',
          label: '风速',
          value: '-',
          unit: 'm/s',
          icon: 'el-icon-wind-power',
          color: '#F56C6C'
        }
      ],
      
      // 时间范围
      timeRange: 'week',
      
      // 实时数据
      realTimeData: []
    }
  },
  
  created() {
    this.deviceName = this.$route.query.deviceName || '气象站';
    this.deviceId = this.$route.query.deviceId || '';
    this.deviceAddr = this.$route.query.deviceAddr || '';
    if (this.deviceAddr) {
      this.loadDeviceData();
    }
  },
  
  methods: {
    // 返回上一页
    goBack() {
      this.$router.go(-1);
    },
    
    // 刷新数据
    refreshData() {
      if (this.deviceAddr) {
        this.loadDeviceData();
      }
    },

    // 加载设备数据
    async loadDeviceData() {
      try {
        const response = await getMeteorologyDeviceDetail(this.deviceAddr);
        if (response.data.code === 200) {
          const deviceData = response.data.rows;
          
          // 更新统计数据
          this.updateStatistics(deviceData);
          
          // 更新实时数据
          this.realTimeData = deviceData;
          
          this.$message.success('数据加载成功');
        } else {
          this.$message.error('数据加载失败: ' + response.data.msg);
        }
      } catch (error) {
        console.error('加载设备数据出错:', error);
        this.$message.error('数据加载失败: ' + (error.message || '网络错误'));
      }
    },

    // 更新统计数据
    updateStatistics(deviceData) {
      // 查找温度数据
      const temperatureData = deviceData.find(item => item.registerName.includes('空气温度'));
      if (temperatureData) {
        const tempStat = this.statistics.find(stat => stat.key === 'temperature');
        if (tempStat) {
          tempStat.value = this.formatValue(temperatureData.valueNum, temperatureData.dataValue);
          tempStat.unit = temperatureData.unit;
        }
      }
      
      // 查找湿度数据
      const humidityData = deviceData.find(item => item.registerName.includes('空气湿度'));
      if (humidityData) {
        const humidityStat = this.statistics.find(stat => stat.key === 'humidity');
        if (humidityStat) {
          humidityStat.value = this.formatValue(humidityData.valueNum, humidityData.dataValue);
          humidityStat.unit = humidityData.unit;
        }
      }
      
      // 查找大气压数据
      const pressureData = deviceData.find(item => item.registerName.includes('大气压'));
      if (pressureData) {
        const pressureStat = this.statistics.find(stat => stat.key === 'pressure');
        if (pressureStat) {
          pressureStat.value = this.formatValue(pressureData.valueNum, pressureData.dataValue);
          pressureStat.unit = pressureData.unit;
        }
      }
      
      // 查找风速数据
      const windSpeedData = deviceData.find(item => item.registerName.includes('风速'));
      if (windSpeedData) {
        const windStat = this.statistics.find(stat => stat.key === 'wind');
        if (windStat) {
          windStat.value = this.formatValue(windSpeedData.valueNum, windSpeedData.dataValue);
          windStat.unit = windSpeedData.unit;
        }
      }
    },

    // 获取项目图标
    getItemIcon(registerName) {
      if (registerName.includes('温度')) {
        return 'el-icon-sunny';
      } else if (registerName.includes('湿度')) {
        return 'el-icon-water';
      } else if (registerName.includes('大气压')) {
        return 'el-icon-guide';
      } else if (registerName.includes('风')) {
        return 'el-icon-wind-power';
      } else if (registerName.includes('雨量')) {
        return 'el-icon-heavy-rain';
      } else if (registerName.includes('光照')) {
        return 'el-icon-sunny';
      } else {
        return 'el-icon-info';
      }
    },

    // 获取项目状态类
    getItemStatusClass(item) {
      // 风速较高时显示警告
      if (item.registerName.includes('风速') && item.valueNum > 10) {
        return 'warning';
      }
      return 'normal';
    },

    // 格式化数值
    formatValue(value, dataValue) {
      // 对于风向等文本类型数据，直接返回原始值
      if (dataValue && typeof dataValue === 'string' && 
          (dataValue.includes('风') || !isNaN(parseFloat(dataValue)))) {
        // 如果dataValue包含"风"字或者本身就是数字，则优先使用dataValue
        if (dataValue.includes('风')) {
          return dataValue;
        }
      }
      
      if (value === undefined || value === null) return '-';
      const num = parseFloat(value);
      if (isNaN(num)) return value;
      
      // 整数保持原样，小数保留一位
      if (num % 1 === 0) {
        return num.toString();
      } else {
        return num.toFixed(1);
      }
    },

    // 格式化时间
    formatTime(timestamp) {
      if (!timestamp) return '-';
      const date = new Date(timestamp);
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      }).replace(/\//g, '-');
    },
    
    // 时间范围变化
    handleTimeRangeChange(value) {
      console.log('切换时间范围:', value);
    }
  }
}
</script>

<style lang="scss" scoped>
.meteorology-overview-data {
  padding: 20px;
  
  // 页面头部卡片
  .page-header-card {
    margin-bottom: 20px;
    
    .page-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .header-left {
        display: flex;
        align-items: center;
        gap: 15px;
        
        .back-btn {
          font-size: 16px;
          padding: 8px 15px;
          
          &:hover {
            color: #409EFF;
          }
        }
        
        .page-title {
          font-size: 22px;
          font-weight: 600;
          color: #303133;
          display: flex;
          align-items: center;
          gap: 10px;
          
          i {
            font-size: 26px;
            color: #409EFF;
          }
        }
        
        .page-description {
          margin-top: 8px;
          color: #909399;
          font-size: 14px;
        }
      }
    }
  }
  
  // 通用间距
  .mb-20 {
    margin-bottom: 20px;
  }
  
  // 统计卡片
  .stat-card {
    border-radius: 12px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
    
    .stat-content {
      display: flex;
      align-items: center;
      gap: 15px;
      
      .stat-icon {
        width: 50px;
        height: 50px;
        border-radius: 10px;
        display: flex;
        align-items: center;
        justify-content: center;
        flex-shrink: 0;
        
        i {
          font-size: 24px;
          color: white;
        }
      }
      
      .stat-info {
        .stat-value {
          font-size: 24px;
          font-weight: 600;
          color: #303133;
          
          .stat-unit {
            font-size: 14px;
            margin-left: 4px;
            color: #909399;
          }
        }
        
        .stat-label {
          font-size: 14px;
          color: #909399;
          margin-top: 4px;
        }
      }
    }
  }
  
  // 图表卡片和视频卡片
  .chart-card, .data-card {
    border-radius: 12px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  }
  
  .chart-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .chart-container {
    height: 300px;
    
    .chart-placeholder {
      height: 100%;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      color: #909399;
      
      i {
        font-size: 36px;
        margin-bottom: 10px;
      }
      
      p {
        margin: 0;
        font-size: 14px;
      }
    }
  }
  
  // 数据网格布局
  .data-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
    gap: 20px;
    padding: 10px;
    
    .data-grid-item {
      .data-card-inner {
        display: flex;
        align-items: center;
        padding: 20px;
        border-radius: 10px;
        background-color: #f8f9fa;
        transition: all 0.3s ease;
        border: 1px solid #ebeef5;
        
        &:hover {
          transform: translateY(-3px);
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
          border-color: #dcdfe6;
        }
        
        .data-icon {
          width: 50px;
          height: 50px;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          flex-shrink: 0;
          margin-right: 15px;
          
          i {
            font-size: 24px;
            color: white;
          }
          
          &.normal {
            background-color: #409EFF;
          }
          
          &.warning {
            background-color: #E6A23C;
          }
        }
        
        .data-info {
          flex: 1;
          
          .data-label {
            font-size: 14px;
            color: #606266;
            margin-bottom: 5px;
          }
          
          .data-value {
            font-size: 20px;
            font-weight: 600;
            color: #303133;
            
            .data-unit {
              font-size: 14px;
              color: #909399;
              margin-left: 4px;
            }
          }
        }
      }
    }
  }
}
</style>