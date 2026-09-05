<template>
  <div class="device-monitor">
    <el-card class="page-card">
      <div slot="header" class="page-header">
        <span class="page-title">
          <i class="el-icon-pie-chart"></i>
          区域监控
        </span>
        <p class="page-desc">实时监控智慧农业园区各区域的运行状态，提供数据可视化展示</p>
      </div>
      
      <div class="content">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-icon online">
                  <i class="el-icon-success"></i>
                </div>
                <div class="stat-info">
                  <h3>{{ deviceStats.online }}</h3>
                  <p>在线区域</p>
                </div>
              </div>
            </el-card>
          </el-col>
          
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-icon offline">
                  <i class="el-icon-error"></i>
                </div>
                <div class="stat-info">
                  <h3>{{ deviceStats.offline }}</h3>
                  <p>离线设备</p>
                </div>
              </div>
            </el-card>
          </el-col>
          
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-icon warning">
                  <i class="el-icon-warning"></i>
                </div>
                <div class="stat-info">
                  <h3>{{ deviceStats.warning }}</h3>
                  <p>警告设备</p>
                </div>
              </div>
            </el-card>
          </el-col>
          
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-icon total">
                  <i class="el-icon-monitor"></i>
                </div>
                <div class="stat-info">
                  <h3>{{ deviceStats.total }}</h3>
                  <p>总设备数</p>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
        
        <el-row :gutter="20" style="margin-top: 20px;">
          <el-col :span="12">
            <el-card>
              <div slot="header">
                <span>设备状态分布</span>
              </div>
              <div class="chart-placeholder">
                <i class="el-icon-pie-chart"></i>
                <p>饼图展示区域</p>
              </div>
            </el-card>
          </el-col>
          
          <el-col :span="12">
            <el-card>
              <div slot="header">
                <span>实时数据趋势</span>
              </div>
              <div class="chart-placeholder">
                <i class="el-icon-data-line"></i>
                <p>折线图展示区域</p>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </el-card>
  </div>
</template>

<script>
export default {
  data() {
    return {
      deviceStats: {
        online: 13,
        offline: 3,
        warning: 2,
        total: 16
      }
    };
  },
  computed: {
    onlineRate() {
      return ((this.deviceStats.online / this.deviceStats.total) * 100).toFixed(1);
    }
  },
  mounted() {
    // 模拟实时数据更新
    this.startStatsUpdate();
  },
  beforeDestroy() {
    if (this.statsTimer) {
      clearInterval(this.statsTimer);
    }
  },
  methods: {
    startStatsUpdate() {
      this.statsTimer = setInterval(() => {
        // 模拟设备状态变化
        const randomChange = Math.random();
        if (randomChange > 0.9) {
          // 10% 概率状态发生变化
          if (this.deviceStats.offline > 0 && Math.random() > 0.5) {
            // 设备上线
            this.deviceStats.offline--;
            this.deviceStats.online++;
          } else if (this.deviceStats.online > 1 && Math.random() > 0.8) {
            // 设备离线
            this.deviceStats.online--;
            this.deviceStats.offline++;
          }
        }
      }, 10000); // 每10秒更新一次
    }
  }
};
</script>

<style lang="scss" scoped>
.device-monitor {
  .page-card {
    .page-header {
      .page-title {
        font-size: 20px;
        font-weight: 600;
        color: #303133;
        display: flex;
        align-items: center;
        gap: 8px;
        
        i {
          font-size: 24px;
          color: #409EFF;
        }
      }
      
      .page-desc {
        margin: 8px 0 0 0;
        color: #909399;
        font-size: 14px;
      }
    }
  }
  
  .content {
    .stat-card {
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
          
          i {
            font-size: 24px;
            color: white;
          }
          
          &.online {
            background: linear-gradient(45deg, #67C23A, #85CE61);
          }
          
          &.offline {
            background: linear-gradient(45deg, #F56C6C, #F78989);
          }
          
          &.warning {
            background: linear-gradient(45deg, #E6A23C, #EEBE77);
          }
          
          &.total {
            background: linear-gradient(45deg, #409EFF, #66B1FF);
          }
        }
        
        .stat-info {
          h3 {
            margin: 0;
            font-size: 28px;
            font-weight: 600;
            color: #303133;
          }
          
          p {
            margin: 5px 0 0 0;
            color: #909399;
            font-size: 14px;
          }
        }
      }
    }
    
    .chart-placeholder {
      height: 200px;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      background: #f8f9fa;
      border-radius: 8px;
      color: #909399;
      
      i {
        font-size: 48px;
        margin-bottom: 10px;
      }
      
      p {
        margin: 0;
        font-size: 14px;
      }
    }
  }
}
</style>