<template>
  <div class="meteorology-realtime">
    <!-- 页面主卡片 -->
    <el-card class="page-card">
      <!-- 顶部标题栏 -->
      <div slot="header" class="page-header">
        <span class="page-title">
          <i class="el-icon-sunny"></i>
          气象站实时数据
        </span>
        <p class="page-desc">
          查看气象站设备实时监测数据
        </p>
      </div>

      <!-- 内容区 -->
      <div class="content">
        <el-row :gutter="20">
          <!-- 左侧树形结构 -->
          <el-col :span="6">
            <el-card class="tree-card">
              <div slot="header" class="tree-header">
                <span><i class="el-icon-monitor"></i> 设备列表</span>
                <el-button class="refresh-btn" type="text" @click="refreshTree">
                  <i class="el-icon-refresh"></i>
                </el-button>
              </div>
              <div class="tree-container">
                <el-tree
                  :data="deviceTree"
                  :props="treeProps"
                  node-key="id"
                  default-expand-all
                  highlight-current
                  :expand-on-click-node="false"
                  @node-click="handleNodeClick"
                  v-loading="treeLoading"
                  element-loading-text="加载中..."
                >
                  <span class="custom-tree-node" slot-scope="{ node, data }">
                    <span class="node-label">
                      <i :class="getNodeIcon(data.status)"></i>
                      {{ node.label }}
                    </span>
                    <span class="node-status">
                      <el-tag :type="data.status === '在线' ? 'success' : 'danger'" size="mini">
                        {{ data.status }}
                      </el-tag>
                    </span>
                  </span>
                </el-tree>
              </div>
            </el-card>
          </el-col>
          
          <!-- 右侧数据展示 -->
          <el-col :span="18">
            <div v-if="selectedDevice">
              <!-- 设备信息卡片 -->
              <el-card class="device-info-card mb-20">
                <div slot="header">
                  <span>{{ selectedDevice.name }} - 实时数据</span>
                  <el-button style="float: right; padding: 3px 0" type="text" @click="refreshDeviceData">刷新数据</el-button>
                </div>
                <div class="device-basic-info">
                  <el-row :gutter="20">
                    <el-col :span="8">
                      <div class="info-item">
                        <span class="label">设备ID：</span>
                        <span class="value">{{ selectedDevice.id }}</span>
                      </div>
                    </el-col>
                    <el-col :span="8">
                      <div class="info-item">
                        <span class="label">设备地址：</span>
                        <span class="value">{{ selectedDevice.address }}</span>
                      </div>
                    </el-col>
                    <el-col :span="8">
                      <div class="info-item center-item">
                        <span class="label">设备状态：</span>
                        <span class="value">
                          <el-tag :type="selectedDevice.status === '在线' ? 'success' : 'danger'">
                            {{ selectedDevice.status }}
                          </el-tag>
                        </span>
                      </div>
                    </el-col>
                  </el-row>
                </div>
              </el-card>
              
              <!-- 实时数据展示 -->
              <el-card class="data-display-card">
                <div slot="header">
                  <span>实时监测数据</span>
                </div>
                <div class="data-grid">
                  <div v-if="deviceRealtimeData && deviceRealtimeData.length > 0">
                    <el-row :gutter="20">
                      <el-col :span="8" v-for="data in deviceRealtimeData" :key="data.key">
                        <div class="data-item">
                          <div class="data-icon" :style="{ backgroundColor: data.color }">
                            <i :class="data.icon"></i>
                          </div>
                          <div class="data-content">
                            <div class="data-label">{{ data.label }}</div>
                            <div class="data-value">{{ data.value }}<span class="data-unit">{{ data.unit }}</span></div>
                            <div class="data-status" :class="data.status">
                              {{ data.statusText }}
                            </div>
                          </div>
                        </div>
                      </el-col>
                    </el-row>
                  </div>
                  <div v-else class="no-data">
                    <i class="el-icon-info"></i>
                    <p>暂无数据</p>
                  </div>
                </div>
              </el-card>
              
              <!-- 数据图表 -->
            </div>
            
            <!-- 未选择设备时的提示 -->
            <div v-else class="no-device-selected">
              <i class="el-icon-sunny empty-icon"></i>
              <p>请选择左侧设备查看实时数据</p>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-card>
  </div>
</template>

<script>
import { getAllMeteorologyDeviceList, getMeteorologyDeviceDetail } from "@/api/iot-meteorology.js";

export default {
  name: "MeteorologyRealtime",
  components: {
  },
  data() {
    return {
      // 树形结构相关
      deviceTree: [],
      treeProps: {
        label: "name",
        children: "children"
      },
      treeLoading: false,
      
      // 选中的设备
      selectedDevice: null,
      
      // 实时数据
      deviceRealtimeData: []
    };
  },
  created() {
    this.loadDeviceTree();
  },
  methods: {
    /** 加载设备树 */
    async loadDeviceTree() {
      this.treeLoading = true;
      try {
        const response = await getAllMeteorologyDeviceList();
        
        if (response.data && response.data.code === 200) {
          // 构建树形结构，使用环境监测站作为设备名称前缀
          this.deviceTree = response.data.rows.map((item, index) => {
            return {
              id: item.deviceId,
              name: `气象监测站${index + 1}`,
              address: item.deviceAddr,
              status: item.deviceStatus === "normal" ? "在线" : "离线",
              raw: item
            };
          });
          
          // 如果有设备，默认选中第一个
          if (this.deviceTree.length > 0) {
            this.handleNodeClick(this.deviceTree[0]);
          }
        } else {
          throw new Error(response.data.msg || "获取设备列表失败");
        }
      } catch (error) {
        console.error("获取设备列表出错:", error);
        this.$message.error(error.message || "设备数据加载失败");
      }
      this.treeLoading = false;
    },
    
    /** 刷新设备树 */
    refreshTree() {
      this.loadDeviceTree();
      this.$message.success("设备列表已刷新");
    },
    
    /** 处理节点点击 */
    async handleNodeClick(data) {
      this.selectedDevice = data;
      await this.loadDeviceRealtimeData();
    },
    
    /** 加载设备实时数据 */
    async loadDeviceRealtimeData() {
      if (!this.selectedDevice) return;
      
      try {
        // 使用设备地址而不是设备ID查询详细信息
        const response = await getMeteorologyDeviceDetail(this.selectedDevice.address);
        
        if (response.data && response.data.code === 200) {
          // 处理返回的数据
          const rawData = response.data.rows;
          
          if (rawData && rawData.length > 0) {
            // 将数据转换为展示格式
            this.deviceRealtimeData = this.transformRealtimeData(rawData);
          } else {
            // 没有数据时显示提示
            this.deviceRealtimeData = [];
          }
        } else {
          throw new Error(response.data.msg || "获取设备详细数据失败");
        }
      } catch (error) {
        console.error("获取设备详细数据出错:", error);
        this.$message.error(error.message || "设备详细数据加载失败");
        // 出错时使用模拟数据
        this.generateMockData();
      }
    },
    
    /** 转换实时数据格式 */
    transformRealtimeData(rawData) {
      // 定义数据映射关系
      const dataMapping = {
        '空气温度': { key: 'temperature', label: '环境温度', icon: 'el-icon-sunny', color: '#409EFF' },
        '空气湿度': { key: 'humidity', label: '环境湿度', icon: 'el-icon-water', color: '#67C23A' },
        '大气压': { key: 'pressure', label: '大气压力', icon: 'el-icon-guide', color: '#E6A23C' },
        '风速': { key: 'windSpeed', label: '风速', icon: 'el-icon-wind-power', color: '#F56C6C' },
        '瞬时雨量': { key: 'rainfall', label: '降雨量', icon: 'el-icon-heavy-rain', color: '#909399' },
        '光照': { key: 'illumination', label: '光照强度', icon: 'el-icon-sunny', color: '#FFD700' },
        '风向': { key: 'windDirection', label: '风向', icon: 'el-icon-location', color: '#409EFF' },
        '风力': { key: 'windPower', label: '风力等级', icon: 'el-icon-star-on', color: '#F56C6C' },
        '日雨量（昨日雨量）': { key: 'yesterdayRainfall', label: '昨日雨量', icon: 'el-icon-heavy-rain', color: '#909399' },
        '当前雨量（今日雨量）': { key: 'todayRainfall', label: '今日雨量', icon: 'el-icon-heavy-rain', color: '#909399' },
        '累计雨量': { key: 'totalRainfall', label: '累计雨量', icon: 'el-icon-heavy-rain', color: '#909399' }
      };
      
      // 转换数据
      return rawData.map(item => {
        const mapping = dataMapping[item.registerName] || { 
          key: item.registerName, 
          label: item.registerName, 
          icon: 'el-icon-info', 
          color: '#909399' 
        };
        
        return {
          key: mapping.key,
          label: mapping.label,
          value: item.dataValue,
          unit: item.unit || '',
          icon: mapping.icon,
          color: mapping.color,
          status: 'normal',
          statusText: '正常'
        };
      });
    },
    
    /** 生成模拟数据（仅在API调用失败时使用） */
    generateMockData() {
      this.deviceRealtimeData = [
        {
          key: 'temperature',
          label: '环境温度',
          value: (20 + Math.random() * 10).toFixed(1),
          unit: '℃',
          icon: 'el-icon-sunny',
          color: '#409EFF',
          status: 'normal',
          statusText: '正常'
        },
        {
          key: 'humidity',
          label: '环境湿度',
          value: (50 + Math.random() * 20).toFixed(1),
          unit: '%',
          icon: 'el-icon-water',
          color: '#67C23A',
          status: 'normal',
          statusText: '正常'
        },
        {
          key: 'pressure',
          label: '大气压力',
          value: (100 + Math.random() * 5).toFixed(1),
          unit: 'Kpa',
          icon: 'el-icon-guide',
          color: '#E6A23C',
          status: 'normal',
          statusText: '正常'
        },
        {
          key: 'windSpeed',
          label: '风速',
          value: (2 + Math.random() * 5).toFixed(1),
          unit: 'm/s',
          icon: 'el-icon-wind-power',
          color: '#F56C6C',
          status: 'warning',
          statusText: '偏高'
        },
        {
          key: 'rainfall',
          label: '降雨量',
          value: '0.0',
          unit: 'mm',
          icon: 'el-icon-heavy-rain',
          color: '#909399',
          status: 'normal',
          statusText: '正常'
        },
        {
          key: 'illumination',
          label: '光照强度',
          value: Math.floor(5000 + Math.random() * 5000),
          unit: 'Lux',
          icon: 'el-icon-sunny',
          color: '#FFD700',
          status: 'normal',
          statusText: '正常'
        },
        {
          key: 'windDirection',
          label: '风向',
          value: '东北风',
          unit: '方向',
          icon: 'el-icon-location',
          color: '#409EFF',
          status: 'normal',
          statusText: '正常'
        },
        {
          key: 'windPower',
          label: '风力等级',
          value: '0',
          unit: '级',
          icon: 'el-icon-star-on',
          color: '#F56C6C',
          status: 'normal',
          statusText: '正常'
        },
        {
          key: 'yesterdayRainfall',
          label: '昨日雨量',
          value: '0.0',
          unit: 'mm',
          icon: 'el-icon-heavy-rain',
          color: '#909399',
          status: 'normal',
          statusText: '正常'
        },
        {
          key: 'todayRainfall',
          label: '今日雨量',
          value: '0.0',
          unit: 'mm',
          icon: 'el-icon-heavy-rain',
          color: '#909399',
          status: 'normal',
          statusText: '正常'
        },
        {
          key: 'totalRainfall',
          label: '累计雨量',
          value: '0.2',
          unit: 'mm',
          icon: 'el-icon-heavy-rain',
          color: '#909399',
          status: 'normal',
          statusText: '正常'
        }
      ];
    },
    
    /** 刷新设备数据 */
    async refreshDeviceData() {
      if (this.selectedDevice) {
        await this.loadDeviceRealtimeData();
        this.$message.success("数据已刷新");
      }
    },
    
    /** 获取节点图标 */
    getNodeIcon(status) {
      if (status === '在线') {
        return 'el-icon-success';
      } else {
        return 'el-icon-warning-outline';
      }
    }
  }
};
</script>

<style lang="scss" scoped>
.meteorology-realtime {
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
          color: #409eff;
        }
      }

      .page-desc {
        margin-top: 8px;
        color: #909399;
        font-size: 14px;
      }
    }
  }

  .content {
    // 通用间距
    .mb-20 {
      margin-bottom: 20px;
    }
    
    .mt-20 {
      margin-top: 20px;
    }
    
    // 树形卡片
    .tree-card {
      height: calc(100vh - 200px);
      display: flex;
      flex-direction: column;
      
      .tree-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        
        .refresh-btn {
          font-size: 16px;
          padding: 0;
        }
      }
      
      .tree-container {
        flex: 1;
        overflow-y: auto;
        
        // 自定义树节点样式
        .custom-tree-node {
          flex: 1;
          display: flex;
          align-items: center;
          justify-content: space-between;
          padding: 8px 0;
          padding-right: 20px;
          
          .node-label {
            i {
              margin-right: 8px;
              color: #409EFF;
            }
          }
          
          .node-status {
            margin-left: 10px;
          }
        }
        
        // 调整树节点间距
        ::v-deep .el-tree-node {
          padding: 3px 0;
        }
        
        ::v-deep .el-tree-node__content {
          height: auto;
          padding: 8px 0;
        }
        
        ::v-deep .el-tree-node__children {
          padding-left: 16px;
        }
      }
    }
    
    // 设备信息卡片
    .device-info-card {
      .device-basic-info {
        .info-item {
          display: flex;
          margin-bottom: 10px;
          
          .label {
            color: #909399;
            width: 80px;
          }
          
          .value {
            color: #606266;
            flex: 1;
          }
        }
      }
    }
    
    // 数据展示卡片
    .data-display-card {
      .data-grid {
        .data-item {
          display: flex;
          align-items: center;
          padding: 20px;
          border-radius: 8px;
          background: #f8f9fa;
          margin-bottom: 20px;
          
          .data-icon {
            width: 50px;
            height: 50px;
            border-radius: 8px;
            display: flex;
            align-items: center;
            justify-content: center;
            margin-right: 15px;
            flex-shrink: 0;
            
            i {
              font-size: 24px;
              color: white;
            }
          }
          
          .data-content {
            flex: 1;
            
            .data-label {
              font-size: 14px;
              color: #909399;
              margin-bottom: 4px;
            }
            
            .data-value {
              font-size: 20px;
              font-weight: 600;
              color: #303133;
              margin-bottom: 4px;
              
              .data-unit {
                font-size: 14px;
                color: #909399;
                margin-left: 4px;
              }
            }
            
            .data-status {
              font-size: 12px;
              padding: 2px 8px;
              border-radius: 10px;
              display: inline-block;
              
              &.normal {
                background-color: #e1f3d8;
                color: #67c23a;
              }
              
              &.warning {
                background-color: #fdf6ec;
                color: #e6a23c;
              }
              
              &.danger {
                background-color: #fef0f0;
                color: #f56c6c;
              }
            }
          }
        }
        
        .no-data {
          height: 200px;
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          color: #909399;
          
          i {
            font-size: 48px;
            margin-bottom: 16px;
            color: #c0c4cc;
          }
          
          p {
            font-size: 16px;
            margin: 0;
          }
        }
      }
    }
    
    // 未选择设备提示
    .no-device-selected {
      height: 400px;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      color: #909399;
      
      .empty-icon {
        font-size: 64px;
        margin-bottom: 16px;
        color: #c0c4cc;
      }
      
      p {
        font-size: 16px;
        margin: 0;
      }
    }
  }
}
</style>