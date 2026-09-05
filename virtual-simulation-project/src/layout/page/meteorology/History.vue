<template>
  <div class="meteorology-history">
    <!-- 页面主卡片 -->
    <el-card class="page-card">
      <!-- 顶部标题栏 -->
      <div slot="header" class="page-header">
        <span class="page-title">
          <i class="el-icon-data-line"></i>
          气象站历史数据
        </span>
        <p class="page-desc">
          查看气象站设备历史监测数据
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
                  <span>{{ selectedDevice.name }} - 历史数据</span>
                  <div class="header-actions">
                    <el-date-picker
                      v-model="dateRange"
                      type="daterange"
                      range-separator="至"
                      start-placeholder="开始日期"
                      end-placeholder="结束日期"
                      value-format="yyyy-MM-dd"
                      @change="loadDeviceHistoryData"
                      class="date-picker">
                    </el-date-picker>
                    <el-button type="primary" @click="loadDeviceHistoryData" class="refresh-btn">查询</el-button>
                  </div>
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
              
              <!-- 图表展示区域 -->
              <el-card class="chart-card mb-20">
                <div slot="header">
                  <span>数据趋势图</span>
                  <div class="chart-controls">
                    <el-select 
                      v-model="selectedDataType" 
                      placeholder="选择数据类型" 
                      size="small"
                      @change="updateChartData"
                    >
                      <el-option
                        v-for="type in dataTypes"
                        :key="type"
                        :label="type"
                        :value="type">
                      </el-option>
                    </el-select>
                  </div>
                </div>
                <div class="chart-container">
                  <div ref="chartContainer" class="chart-wrapper" v-show="chartData.length > 0"></div>
                  <div v-show="chartData.length === 0" class="no-chart-data">
                    <i class="el-icon-info"></i>
                    <p>暂无图表数据</p>
                  </div>
                </div>
              </el-card>
              
              <!-- 关键指标展示 -->
              <el-card class="kpi-card mb-20">
                <div slot="header">
                  <span>关键指标</span>
                </div>
                <div class="kpi-container">
                  <el-row :gutter="20">
                    <el-col :span="6" v-for="kpi in kpiData" :key="kpi.key">
                      <div class="kpi-item">
                        <div class="kpi-icon" :style="{ backgroundColor: kpi.color }">
                          <i :class="kpi.icon"></i>
                        </div>
                        <div class="kpi-content">
                          <div class="kpi-value">{{ kpi.value }}<span class="kpi-unit">{{ kpi.unit }}</span></div>
                          <div class="kpi-label">{{ kpi.label }}</div>
                        </div>
                      </div>
                    </el-col>
                  </el-row>
                </div>
              </el-card>
              
              <!-- 历史数据列表 -->
              <el-card class="history-data-card">
                <div slot="header">
                  <span>历史数据详情</span>
                </div>
                
                <div class="history-data-container" v-loading="dataLoading">
                  <div v-if="historyData && historyData.length > 0" class="data-list">
                    <div 
                      v-for="(group, date) in groupedHistoryData" 
                      :key="date" 
                      class="data-group"
                    >
                      <div class="group-header">
                        <i class="el-icon-date"></i>
                        <strong>{{ formatDate(date) }}</strong>
                      </div>
                      
                      <div class="data-items">
                        <div 
                          v-for="item in group" 
                          :key="item.dataId" 
                          class="data-item"
                        >
                          <div class="item-header">
                            <span class="time">{{ formatTime(item.createTime) }}</span>
                            <el-tag :type="getStatusType(item)" size="mini">
                              {{ item.registerName }}
                            </el-tag>
                          </div>
                          
                          <div class="item-content">
                            <div class="data-value">
                              <span class="label">{{ item.registerName }}:</span>
                              <span class="value">{{ item.dataValue }} {{ item.unit }}</span>
                            </div>
                            
                            <div class="data-details">
                              <span class="detail-item">
                                <span class="detail-label">数值:</span>
                                <span class="detail-value">{{ item.valueNum }}</span>
                              </span>
                              <span class="detail-item">
                                <span class="detail-label">寄存器ID:</span>
                                <span class="detail-value">{{ item.registerId }}</span>
                              </span>
                              <span class="detail-item">
                                <span class="detail-label">节点ID:</span>
                                <span class="detail-value">{{ item.nodeId }}</span>
                              </span>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                  
                  <div v-else class="no-data">
                    <i class="el-icon-info"></i>
                    <p v-if="!dataLoading">暂无历史数据</p>
                    <p v-else>数据加载中...</p>
                  </div>
                </div>
              </el-card>
            </div>
            
            <!-- 未选择设备时的提示 -->
            <div v-else class="no-device-selected">
              <i class="el-icon-data-line empty-icon"></i>
              <p>请选择左侧设备查看历史数据</p>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-card>
  </div>
</template>

<script>
// 引入 ECharts 主模块
import * as echarts from 'echarts/lib/echarts';
// 引入折线图
import 'echarts/lib/chart/line';
// 引入提示框和标题组件
import 'echarts/lib/component/tooltip';
import 'echarts/lib/component/title';
import 'echarts/lib/component/legend';

import { getAllMeteorologyDeviceList } from "@/api/iot-meteorology.js";
// 注意：这里需要一个获取历史数据的API，暂时注释掉
// import { getMeteorologyDeviceHistory } from "@/api/iot-meteorology.js";

export default {
  name: "MeteorologyHistory",
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
      
      // 历史数据
      historyData: [],
      dataLoading: false,
      
      // 查询条件
      dateRange: [],
      
      // 图表相关
      chart: null,
      chartData: [],
      selectedDataType: '空气温度',
      dataTypes: ['空气温度', '空气湿度', '大气压', '风速', '光照', '风力'],
      
      // KPI数据
      kpiData: []
    };
  },
  computed: {
    groupedHistoryData() {
      if (!this.historyData || this.historyData.length === 0) return {};
      
      // 按日期分组数据
      const groups = {};
      this.historyData.forEach(item => {
        const date = this.formatDateOnly(item.createTime);
        if (!groups[date]) {
          groups[date] = [];
        }
        groups[date].push(item);
      });
      
      // 按时间倒序排列每组内的数据
      Object.keys(groups).forEach(date => {
        groups[date].sort((a, b) => b.createTime - a.createTime);
      });
      
      // 按日期倒序排序
      const sortedGroups = {};
      Object.keys(groups)
        .sort((a, b) => new Date(b) - new Date(a))
        .forEach(date => {
          sortedGroups[date] = groups[date];
        });
      
      return sortedGroups;
    }
  },
  mounted() {
    this.initChart();
  },
  beforeDestroy() {
    if (this.chart) {
      this.chart.dispose();
    }
  },
  created() {
    this.loadDeviceTree();
  },
  methods: {
    /** 初始化图表 */
    initChart() {
      this.$nextTick(() => {
        if (this.$refs.chartContainer) {
          this.chart = echarts.init(this.$refs.chartContainer);
          this.updateChart();
        }
      });
    },
    
    /** 更新图表 */
    updateChart() {
      if (!this.chart) return;
      
      const option = {
        title: {
          text: `${this.selectedDevice ? this.selectedDevice.name : ''} - ${this.selectedDataType}趋势图`,
          left: 'center'
        },
        tooltip: {
          trigger: 'axis'
        },
        xAxis: {
          type: 'category',
          data: this.chartData.map(item => this.formatTime(item.createTime))
        },
        yAxis: {
          type: 'value',
          name: this.dataTypes.includes(this.selectedDataType) ? 
                this.getUnitByDataType(this.selectedDataType) : ''
        },
        series: [{
          data: this.chartData.map(item => item.valueNum),
          type: 'line',
          smooth: true,
          areaStyle: {}
        }]
      };
      
      this.chart.setOption(option, true);
    },
    
    /** 根据数据类型获取单位 */
    getUnitByDataType(dataType) {
      const unitMap = {
        '空气温度': '℃',
        '空气湿度': '%',
        '大气压': 'Kpa',
        '风速': 'm/s',
        '光照': 'Lux',
        '风力': '级'
      };
      return unitMap[dataType] || '';
    },
    
    /** 更新图表数据 */
    updateChartData() {
      if (!this.historyData || this.historyData.length === 0) {
        this.chartData = [];
        this.updateChart();
        return;
      }
      
      // 筛选指定类型的数据
      this.chartData = this.historyData
        .filter(item => item.registerName === this.selectedDataType)
        .sort((a, b) => a.createTime - b.createTime);
      
      this.updateChart();
      this.updateKpiData();
    },
    
    /** 更新KPI数据 */
    updateKpiData() {
      if (!this.historyData || this.historyData.length === 0) {
        this.kpiData = [];
        return;
      }
      
      // 计算各种数据类型的最大值、最小值和平均值
      const dataStats = {};
      
      this.historyData.forEach(item => {
        if (!dataStats[item.registerName]) {
          dataStats[item.registerName] = {
            values: [],
            unit: item.unit
          };
        }
        dataStats[item.registerName].values.push(item.valueNum);
      });
      
      // 生成KPI数据
      this.kpiData = Object.keys(dataStats).slice(0, 4).map(key => {
        const stats = dataStats[key];
        const values = stats.values;
        const max = Math.max(...values);
        const min = Math.min(...values);
        const avg = values.reduce((a, b) => a + b, 0) / values.length;
        
        return {
          key: key,
          label: key,
          value: avg.toFixed(1),
          unit: stats.unit,
          icon: this.getIconByDataType(key),
          color: this.getColorByDataType(key)
        };
      });
    },
    
    /** 根据数据类型获取图标 */
    getIconByDataType(dataType) {
      const iconMap = {
        '空气温度': 'el-icon-sunny',
        '空气湿度': 'el-icon-water',
        '大气压': 'el-icon-guide',
        '风速': 'el-icon-wind-power',
        '光照': 'el-icon-sunny',
        '风力': 'el-icon-star-on'
      };
      return iconMap[dataType] || 'el-icon-info';
    },
    
    /** 根据数据类型获取颜色 */
    getColorByDataType(dataType) {
      const colorMap = {
        '空气温度': '#409EFF',
        '空气湿度': '#67C23A',
        '大气压': '#E6A23C',
        '风速': '#F56C6C',
        '光照': '#FFD700',
        '风力': '#909399'
      };
      return colorMap[dataType] || '#909399';
    },
    
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
    handleNodeClick(data) {
      this.selectedDevice = data;
      this.loadDeviceHistoryData();
    },
    
    /** 加载设备历史数据 */
    async loadDeviceHistoryData() {
      if (!this.selectedDevice) return;
      
      this.dataLoading = true;
      try {
        // 这里应该调用实际的API获取历史数据
        // 由于没有提供具体的API，我们使用模拟数据
        await new Promise(resolve => setTimeout(resolve, 800));
        
        // 生成模拟历史数据
        this.generateMockHistoryData();
        
        // 更新图表数据
        this.updateChartData();
        
        /*
         * 实际应该使用下面的代码：
         *
         * const params = {
         *   deviceAddr: this.selectedDevice.address,
         *   startDate: this.dateRange && this.dateRange[0] ? this.dateRange[0] : null,
         *   endDate: this.dateRange && this.dateRange[1] ? this.dateRange[1] : null
         * };
         * 
         * const response = await getMeteorologyDeviceHistory(params);
         * 
         * if (response.data && response.data.code === 200) {
         *   this.historyData = response.data.rows || [];
         *   this.updateChartData();
         * } else {
         *   throw new Error(response.data.msg || "获取设备历史数据失败");
         * }
         */
      } catch (error) {
        console.error("获取设备历史数据出错:", error);
        this.$message.error(error.message || "设备历史数据加载失败");
        this.historyData = [];
      }
      this.dataLoading = false;
    },
    
    /** 生成模拟历史数据 */
    generateMockHistoryData() {
      const dataTypes = [
        { name: '空气温度', unit: '℃' },
        { name: '空气湿度', unit: '%' },
        { name: '大气压', unit: 'Kpa' },
        { name: '风速', unit: 'm/s' },
        { name: '瞬时雨量', unit: 'mm' },
        { name: '光照', unit: 'Lux' },
        { name: '风向', unit: '方向' },
        { name: '风力', unit: '级' },
        { name: '日雨量（昨日雨量）', unit: 'mm' },
        { name: '当前雨量（今日雨量）', unit: 'mm' },
        { name: '累计雨量', unit: 'mm' }
      ];
      
      this.historyData = [];
      const now = new Date();
      
      // 生成最近7天的数据
      for (let day = 0; day < 7; day++) {
        const date = new Date(now);
        date.setDate(date.getDate() - day);
        
        // 每天生成10-20条记录
        const recordsCount = 10 + Math.floor(Math.random() * 11);
        for (let i = 0; i < recordsCount; i++) {
          const dataType = dataTypes[Math.floor(Math.random() * dataTypes.length)];
          const hoursAgo = Math.floor(Math.random() * 24);
          const minutesAgo = Math.floor(Math.random() * 60);
          
          const recordTime = new Date(date);
          recordTime.setHours(recordTime.getHours() - hoursAgo);
          recordTime.setMinutes(recordTime.getMinutes() - minutesAgo);
          
          // 根据数据类型生成合理的数值范围
          let value;
          switch (dataType.name) {
            case '空气温度':
              value = (15 + Math.random() * 15).toFixed(1); // 15-30度
              break;
            case '空气湿度':
              value = (40 + Math.random() * 40).toFixed(1); // 40-80%
              break;
            case '大气压':
              value = (95 + Math.random() * 10).toFixed(1); // 95-105 Kpa
              break;
            case '风速':
              value = (0 + Math.random() * 10).toFixed(1); // 0-10 m/s
              break;
            case '光照':
              value = Math.floor(0 + Math.random() * 10000); // 0-10000 Lux
              break;
            case '风力':
              value = Math.floor(Math.random() * 12); // 0-12级
              break;
            default:
              value = (Math.random() * 100).toFixed(1);
          }
          
          this.historyData.push({
            dataId: `${day}-${i}`,
            deviceAddr: this.selectedDevice.address,
            nodeId: Math.floor(Math.random() * 20),
            registerId: Math.floor(Math.random() * 10),
            dataValue: value,
            valueNum: parseFloat(value),
            alarmLevel: 0,
            alarmColor: "00ff00",
            unit: dataType.unit,
            createTime: recordTime.getTime(),
            registerName: dataType.name
          });
        }
      }
    },
    
    /** 获取节点图标 */
    getNodeIcon(status) {
      if (status === '在线') {
        return 'el-icon-success';
      } else {
        return 'el-icon-warning-outline';
      }
    },
    
    /** 格式化完整日期时间 */
    formatTime(timestamp) {
      const date = new Date(timestamp);
      return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}:${date.getSeconds().toString().padStart(2, '0')}`;
    },
    
    /** 只格式化日期部分 */
    formatDateOnly(timestamp) {
      const date = new Date(timestamp);
      return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`;
    },
    
    /** 格式化日期显示 */
    formatDate(dateStr) {
      const today = new Date();
      const date = new Date(dateStr);
      
      if (date.toDateString() === today.toDateString()) {
        return '今天';
      }
      
      const yesterday = new Date(today);
      yesterday.setDate(yesterday.getDate() - 1);
      
      if (date.toDateString() === yesterday.toDateString()) {
        return '昨天';
      }
      
      return dateStr;
    },
    
    /** 获取状态类型 */
    getStatusType(item) {
      const warningItems = ['空气温度', '风速'];
      const dangerItems = ['空气湿度', '大气压'];
      
      if (warningItems.includes(item.registerName)) {
        return 'warning';
      } else if (dangerItems.includes(item.registerName)) {
        return 'danger';
      }
      return 'info';
    }
  }
};
</script>

<style lang="scss" scoped>
.meteorology-history {
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
      .header-actions {
        float: right;
        display: flex;
        gap: 10px;
        align-items: center;
        
        .date-picker {
          width: 260px;
        }
        
        .refresh-btn {
          margin-left: 10px;
        }
      }
      
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
        
        .center-item {
          display: flex;
          align-items: center;
        }
      }
    }
    
    // 图表卡片
    .chart-card {
      .chart-controls {
        float: right;
      }
      
      .chart-container {
        height: 300px;
        position: relative;
        
        .chart-wrapper {
          width: 100%;
          height: 100%;
        }
        
        .no-chart-data {
          position: absolute;
          top: 0;
          left: 0;
          width: 100%;
          height: 100%;
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
    
    // KPI卡片
    .kpi-card {
      .kpi-container {
        .kpi-item {
          display: flex;
          align-items: center;
          padding: 15px;
          border-radius: 8px;
          background: #f8f9fa;
          margin-bottom: 15px;
          
          .kpi-icon {
            width: 45px;
            height: 45px;
            border-radius: 8px;
            display: flex;
            align-items: center;
            justify-content: center;
            margin-right: 15px;
            flex-shrink: 0;
            
            i {
              font-size: 20px;
              color: white;
            }
          }
          
          .kpi-content {
            .kpi-value {
              font-size: 20px;
              font-weight: 600;
              color: #303133;
              margin-bottom: 4px;
              
              .kpi-unit {
                font-size: 14px;
                color: #909399;
                margin-left: 4px;
              }
            }
            
            .kpi-label {
              font-size: 14px;
              color: #909399;
            }
          }
        }
      }
    }
    
    // 历史数据卡片
    .history-data-card {
      .history-data-container {
        min-height: 400px;
        
        .data-list {
          max-height: calc(100vh - 350px);
          overflow-y: auto;
          
          .data-group {
            margin-bottom: 20px;
            
            .group-header {
              padding: 10px 15px;
              background-color: #f5f7fa;
              border-left: 4px solid #409eff;
              margin-bottom: 10px;
              display: flex;
              align-items: center;
              gap: 8px;
              
              strong {
                font-size: 16px;
                color: #303133;
              }
            }
            
            .data-items {
              .data-item {
                border: 1px solid #ebeef5;
                border-radius: 4px;
                margin-bottom: 10px;
                transition: box-shadow 0.3s;
                
                &:hover {
                  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
                }
                
                .item-header {
                  padding: 10px 15px;
                  border-bottom: 1px solid #ebeef5;
                  display: flex;
                  justify-content: space-between;
                  align-items: center;
                  
                  .time {
                    font-weight: 500;
                    color: #606266;
                  }
                }
                
                .item-content {
                  padding: 15px;
                  
                  .data-value {
                    margin-bottom: 10px;
                    
                    .label {
                      font-weight: 500;
                      color: #303133;
                      margin-right: 10px;
                    }
                    
                    .value {
                      font-size: 16px;
                      font-weight: 600;
                      color: #409eff;
                    }
                  }
                  
                  .data-details {
                    display: flex;
                    flex-wrap: wrap;
                    gap: 15px;
                    
                    .detail-item {
                      .detail-label {
                        color: #909399;
                        margin-right: 5px;
                      }
                      
                      .detail-value {
                        color: #606266;
                      }
                    }
                  }
                }
              }
            }
          }
        }
        
        .no-data {
          height: 300px;
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