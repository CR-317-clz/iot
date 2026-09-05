<template>
  <div class="hardware-simulation-x6">
    <!-- 设备配置对话框 -->
    <DeviceConfigDialog :visible="deviceConfigDialogVisible" :device-type="selectedDeviceType"
      :device-type-options="deviceTypeOptions" :connecting="deviceConfigConnecting"
      :existing-config="deviceConfigs[selectedDeviceId]"
      @update:visible="(value) => { deviceConfigDialogVisible = value }" @confirm="handleDeviceConfigConfirm"
      @cancel="handleDeviceConfigCancel" />

    <!-- 节点引脚信息悬浮窗 -->
    <!-- <NodeTooltip ref="nodeTooltip" :graph="graph" :visible="tooltipVisible" :content="tooltipContent"
      :position="tooltipPosition" @update:visible="(value) => { tooltipVisible = value }"
      @update:content="(value) => { tooltipContent = value }"
      @update:position="(value) => { tooltipPosition = value }" /> -->

    <!-- 传感器阈值配置对话框 -->
    <SensorThresholdDialog :visible="sensorThresholdDialogVisible" :sensor-type="selectedSensorType"
      :sensor-name="selectedSensorName" :sensor-id="selectedSensorId" :connecting="sensorThresholdConnecting"
      :existing-threshold="sensorThresholds[selectedSensorId]"
      @update:visible="(value) => { sensorThresholdDialogVisible = value }" @confirm="handleSensorThresholdConfirm"
      @cancel="handleSensorThresholdCancel" />

    <el-card class="page-card">
      <div slot="header" class="page-header">
        <span class="page-title">
          <i class="el-icon-connection"></i>
          硬件接线仿真 (AntV X6)
        </span>
        <!-- <div class="page-header-right">
          <el-tag :type="serialPortConfig.connected ? 'success' : 'info'" size="small" style="margin-right: 10px;">
            <i :class="serialPortConfig.connected ? 'el-icon-link' : 'el-icon-remove-outline'"></i>
            {{ serialPortConfig.connected ?
              `已连接 (${serialPortConfig.deviceType === 'coordinator' ? '协调器' : serialPortConfig.deviceType === 'router' ?
                '路由器'
                : '终端节点'})` :
              '未连接' }}
          </el-tag>
          <el-button size="mini" type="primary" plain icon="el-icon-setting" @click="openSerialPortDialog">
            {{ serialPortConfig.configured ? '重新配置' : '配置设备' }}
          </el-button>
        </div> -->
        <p class="page-desc">拖拽器件到画布并进行接线，系统将自动验证连接的正确性</p>
      </div>

      <div class="content">
        <el-row :gutter="20">
          <!-- 左侧器件库 -->
          <el-col :span="5">
            <el-card class="component-panel">
              <div slot="header" class="panel-header">
                <i class="el-icon-box"></i>
                <span>器件库</span>
              </div>

              <div class="component-list">
                <el-collapse v-model="activeCategory" accordion>
                  <el-collapse-item v-for="category in componentCategories" :key="category.name" :name="category.name">
                    <template slot="title">
                      <i :class="category.icon"></i>
                      <span>{{ category.label }}</span>
                      <el-tag size="mini" type="info" style="margin-left: 20px; height: 18px; line-height: 18px;">
                        {{ category.count }}
                      </el-tag>
                    </template>

                    <div v-for="(component, index) in category.components" :key="component.id" class="component-item"
                      @mousedown="handleComponentDragStart($event, component)">
                      <div class="component-icon">
                        <img v-if="component.imageUrl" :src="component.imageUrl" alt="器件图片" />
                        <i v-else :class="component.icon"></i>
                      </div>
                      <div class="component-info">
                        <div class="component-name">{{ component.name }}</div>
                        <div class="component-desc">{{ component.voltage }}</div>
                      </div>
                      <!-- 在每个器件旁边显示序号 -->
                      <el-tag size="mini" type="info"
                        style="position: absolute; top: 5px; right: 5px; height: 16px; line-height: 16px; font-size: 10px;">
                        {{ index + 1 }}
                      </el-tag>
                    </div>
                  </el-collapse-item>
                </el-collapse>
              </div>

              <div class="panel-footer">
                <el-button type="danger" size="small" icon="el-icon-delete" @click="clearCanvas">
                  清空画布
                </el-button>
                <el-button type="warning" size="small" icon="el-icon-refresh-left" @click="clearAllConnections">
                  清空连线
                </el-button>
              </div>
            </el-card>
          </el-col>

          <!-- 中间画布区 -->
          <el-col :span="14">
            <CanvasArea ref="canvasArea" :show-grid="showGrid" :zoom-level="zoomLevel" :can-undo="canUndo"
              :can-redo="canRedo" :device-configs="deviceConfigs" @graph-ready="handleGraphReady"
              @node-selected="handleNodeSelected" @node-deselected="handleNodeDeselected"
              @node-mouseenter="handleNodeMouseEnter" @node-mouseleave="handleNodeMouseLeave"
              @node-moving="handleNodeMoving" @show-context-menu="showContextMenu" @history-change="handleHistoryChange"
              @zoom-change="handleZoomChange" @node-added="handleNodeAdded" @cells-deleted="handleCellsDeleted"
              @cells-copied="handleCellsCopied" @cells-pasted="handleCellsPasted" @validate-edge="validateEdge"
              @canvas-cleared="handleCanvasCleared" @connections-cleared="handleConnectionsCleared"
              @node-deleted="handleNodeDeleted" @open-serial-dialog="openDeviceConfigDialog"
              @open-threshold-dialog="openSensorThresholdDialog" />
          </el-col>

          <!-- 右侧属性面板 -->
          <el-col :span="5">
            <div class="right-panel-wrapper">
              <el-card class="property-panel">
                <div slot="header" class="panel-header">
                  <i class="el-icon-info"></i>
                  <span>属性面板</span>
                </div>

                <div v-if="selectedNode" class="property-content">
                  <div class="node-header">
                    <div class="node-icon" :style="{ background: selectedNode.color || '#409EFF' }">
                      <i :class="selectedNode.icon"></i>
                    </div>
                    <div class="node-title">
                      <h3>{{ selectedNode.name }}</h3>
                      <span class="node-category">{{ selectedNode.category }}</span>
                    </div>
                  </div>

                  <div class="property-section">
                    <h4><i class="el-icon-info"></i> 基本信息</h4>
                    <div class="property-item">
                      <label>器件名称:</label>
                      <span>{{ selectedNode.name }}</span>
                    </div>
                    <div class="property-item">
                      <label>器件类型:</label>
                      <el-tag size="mini" :type="getCategoryType(selectedNode.category)">
                        {{ selectedNode.category }}
                      </el-tag>
                    </div>
                    <div class="property-item">
                      <label>工作电压:</label>
                      <span class="voltage-badge">{{ selectedNode.voltage }}</span>
                    </div>
                    <div class="property-item">
                      <label>通信协议:</label>
                      <el-tag size="mini" effect="plain">
                        {{ selectedNode.protocol || '无' }}
                      </el-tag>
                    </div>
                  </div>

                  <div class="property-section">
                    <h4><i class="el-icon-connection"></i> 引脚配置 ({{ selectedNode.pins.length }})</h4>
                    <div v-for="pin in selectedNode.pins" :key="pin.id" class="pin-info">
                      <div class="pin-info-header">
                        <div class="pin-label-group">
                          <span class="pin-dot" :class="pin.type"></span>
                          <span class="pin-info-label">{{ pin.label }}</span>
                        </div>
                        <el-tag :type="pin.type === 'output' ? 'warning' : 'success'" size="mini" effect="dark">
                          {{ pin.type === 'output' ? 'OUT' : 'IN' }}
                        </el-tag>
                      </div>
                      <div class="pin-info-detail">
                        <span v-if="pin.voltage !== undefined">
                          <i class="el-icon-lightning"></i>
                          {{ pin.voltage }}V
                        </span>
                        <span v-if="pin.protocol">
                          <i class="el-icon-connection"></i>
                          {{ pin.protocol }}
                        </span>
                      </div>
                    </div>
                  </div>

                  <div class="property-section">
                    <el-button type="danger" size="small" icon="el-icon-delete" @click="deleteSelectedNode"
                      style="width: 100%;">
                      删除元器件
                    </el-button>
                  </div>
                </div>

                <div v-else class="property-empty">
                  <div class="empty-icon">
                    <i class="el-icon-info"></i>
                  </div>
                  <p class="empty-title">未选择器件</p>
                  <p class="empty-desc">点击画布上的器件查看详细属性</p>

                  <div class="quick-tips">
                    <div class="tip-item">
                      <i class="el-icon-mouse"></i>
                      <span>拖拽器件到画布</span>
                    </div>
                    <div class="tip-item">
                      <i class="el-icon-connection"></i>
                      <span>拖拽端口连接</span>
                    </div>
                    <div class="tip-item">
                      <i class="el-icon-delete"></i>
                      <span>Delete 删除</span>
                    </div>
                    <div class="tip-item">
                      <i class="el-icon-document-copy"></i>
                      <span>Ctrl+C/V 复制</span>
                    </div>
                  </div>
                </div>
              </el-card>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-card>
  </div>
</template>

<script>
/* eslint-disable no-unused-vars */
import { Graph } from '@antv/x6'
import { getSensorCategoryTree, checkWiringRule, getDeviceTypeList } from '@/api/HardwareSimulation.js'
import CanvasArea from '@/components/HardwareSimulation/CanvasArea.vue'
// import NodeTooltip from '@/components/HardwareSimulation/NodeTooltip.vue'
import { saveSerialPortConfigApi } from '@/api/DeviceConfig.js'
import DeviceConfigDialog from '@/components/DeviceConfigDialog.vue'
import SensorThresholdDialog from '@/components/SensorThresholdDialog.vue'

export default {
  name: 'HardwareSimulationX6',
  components: {
    CanvasArea,
    // NodeTooltip,
    DeviceConfigDialog,
    SensorThresholdDialog
  },
  data() {
    return {
      graph: null,
      showGrid: true,
      zoomLevel: 100,
      canUndo: false,
      canRedo: false,
      edgeCount: 0,
      allConnectionsValid: true,
      selectedNode: null,

      // 串口配置相关
      serialPortDialogVisible: false,
      serialPortConnecting: false,
      serialPortConfig: {
        deviceType: '',
        panId: '',
        channel: '',
        configured: false,
        connected: false,
        timeout: 5000,
        autoReconnect: true
      },

      deviceTypeOptions: [], // 设备类型下拉选项
      // 设备配置相关
      deviceConfigDialogVisible: false,
      deviceConfigConnecting: false,
      selectedDeviceType: '',
      selectedDeviceId: '',

      // 新增：存储每个设备的配置，key为deviceId
      deviceConfigs: {}, // 存储每个设备的配置，key为deviceId
      uniqueIdentifications: {}, // 存储每个设备的uniqueIdentification，key为deviceId
      // 移除共享的deviceConfig对象，改为使用deviceConfigs存储每个设备的独立配置
      activeCategory: 'sensor',
      componentCategories: [],
      sensorCategoryTree: [],

      // 悬浮窗相关状态
      tooltipVisible: false,
      tooltipContent: '',
      tooltipPosition: { x: 0, y: 0 },

      // 传感器阈值配置相关
      sensorThresholdDialogVisible: false,
      sensorThresholdConnecting: false,
      selectedSensorType: '',
      selectedSensorName: '',
      selectedSensorId: '',
      sensorThresholds: {} // 存储每个传感器的阈值配置，key为sensorId
    }
  },

  mounted() {
    this.$nextTick(() => {
      this.getSensorCategoryTree(); // 新增：组件挂载时获取器件分类数据
      // 首次进入时显示串口配置对话框
      this.serialPortDialogVisible = false

      // 新增：等待数据加载完成后，默认展开第一个分类
      setTimeout(() => {
        if (this.componentCategories.length > 0) {
          this.activeCategory = this.componentCategories[0].name;
        }
      }, 100);
    })

    const svgNS = "http://www.w3.org/2000/svg";
    let svg = document.querySelector('svg');
    if (svg && !document.getElementById('glow')) {
      const filter = document.createElementNS(svgNS, 'filter');
      filter.setAttribute('id', 'glow');
      filter.innerHTML = `
      <feGaussianBlur stdDeviation="2.5" result="coloredBlur"/>
      <feMerge>
        <feMergeNode in="coloredBlur"/>
        <feMergeNode in="SourceGraphic"/>
      </feMerge>
    `;
      svg.appendChild(filter);
    }
  },

  beforeDestroy() {
  },

  methods: {
    // 处理画布准备就绪
    handleGraphReady(graph) {
      this.graph = graph;
    },

    // 新增：获取器件分类树的方法
    async getSensorCategoryTree() {
      try {
        // 实际调用API接口
        const response = await getSensorCategoryTree();
        this.sensorCategoryTree = response.data;

        // 将API数据转换为组件所需的格式
        this.transformSensorData();
      } catch (error) {
        console.error('获取传感器分类树失败:', error);
        this.$message.error('获取器件库数据失败');
      }
    },

    // 修改：将API数据转换为组件所需格式
    transformSensorData() {
      this.componentCategories = this.sensorCategoryTree.map(category => {
        // 将pin字段映射为pins，并转换引脚类型
        const devicesWithPins = category.devices.map(device => {
          const pins = device.pin ? device.pin.map((pin, index) => {
            // 将pinType转换为type
            let type = 'input'; // 默认为input
            if (pin.pinType.toLowerCase() === 'vcc' || pin.pinType.toLowerCase() === 'gnd' || pin.pinType.toLowerCase() === 'power') {
              type = 'output'; // 电源引脚为输出
            } else if (pin.pinType.toLowerCase() === 'input' || pin.pinType.toLowerCase() === 'in') {
              type = 'input';
            } else if (pin.pinType.toLowerCase() === 'output' || pin.pinType.toLowerCase() === 'out') {
              type = 'output';
            }

            // 为重复的引脚 ID 添加索引以确保唯一性
            const uniqueId = `${pin.pinId}_${index}`;

            return {
              id: uniqueId,  // 使用唯一 ID
              label: pin.pinName || pin.pinId,
              type: type,
              location: pin.location || '未知', // 添加位置信息
              voltage: pin.voltage ? parseFloat(pin.voltage) || 0 : 0, // 确保电压是数字类型
              pinColour: pin.pinColour || '#409EFF' // 添加引脚颜色，默认为蓝色
            };
          }) : [];

          return {
            id: `${device.virtualId}_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`, // 确保每个设备实例都有唯一ID
            name: device.virtualName,
            category: category.sensorName,
            icon: this.getIconByCategory(category.sensorCode),
            voltage: `${device.voltage}V`,
            protocol: 'Unknown',
            color: this.getColorByCategory(category.sensorCode),
            pins: pins,       // 用于画布显示的引脚
            pin: device.pin,  // 保留原始引脚数据
            // 添加图片路径，如果有则使用
            imageUrl: device.images ? (Array.isArray(device.images) ? device.images[0] : device.images) : null
          };
        });

        return {
          name: category.sensorCode,
          label: category.sensorName,
          icon: this.getIconByCategory(category.sensorCode),
          count: devicesWithPins.length, // 添加器件数量
          components: devicesWithPins
        };
      });
    },

    // 辅助方法：根据类别获取图标
    getIconByCategory(sensorCode) {
      const iconMap = {
        'power_supply': 'el-icon-lightning',
        'gateway': 'el-icon-s-platform',
        'controller': 'el-icon-cpu',
        'sensor': 'el-icon-data-analysis',
        'actuator': 'el-icon-video-play',
        'communication_module': 'el-icon-connection'
      };
      return iconMap[sensorCode] || 'el-icon-box';
    },

    // 辅助方法：根据类别获取颜色
    getColorByCategory(sensorCode) {
      const colorMap = {
        'power_supply': '#FFC312',
        'gateway': '#F79F1F',
        'controller': '#95E1D3',
        'sensor': '#FF6B6B',
        'actuator': '#F8B500',
        'communication_module': '#786FA6'
      };
      return colorMap[sensorCode] || '#409EFF';
    },

    // 根据类别获取标签类型
    getCategoryType(category) {
      const typeMap = {
        '电源': 'warning',
        '传感器': 'success',
        '控制器': 'primary',
        '执行器': 'danger'
      };
      return typeMap[category] || 'info';
    },

    // 处理组件拖拽开始
    handleComponentDragStart(event, component) {
      // 阻止默认行为和事件冒泡
      event.preventDefault()
      event.stopPropagation()

      // 创建拖拽指示器
      const indicator = document.createElement('div')
      indicator.style.position = 'fixed'
      indicator.style.pointerEvents = 'none'
      indicator.style.zIndex = '9999'
      indicator.style.padding = '8px 12px'
      indicator.style.background = 'rgba(64, 158, 255, 0.9)'
      indicator.style.color = 'white'
      indicator.style.borderRadius = '4px'
      indicator.style.fontSize = '12px'
      indicator.style.boxShadow = '0 2px 12px rgba(0, 0, 0, 0.3)'
      indicator.textContent = `拖拽 ${component.name}`
      document.body.appendChild(indicator)

      // 记录初始位置
      let isDragging = false
      const startX = event.clientX
      const startY = event.clientY

      // 鼠标移动处理
      const handleMouseMove = (e) => {
        // 移动一定距离后才开始拖拽
        if (!isDragging) {
          const distance = Math.sqrt(
            Math.pow(e.clientX - startX, 2) +
            Math.pow(e.clientY - startY, 2)
          )
          if (distance > 5) {
            isDragging = true
          } else {
            return
          }
        }

        // 更新指示器位置
        indicator.style.left = (e.clientX + 15) + 'px'
        indicator.style.top = (e.clientY + 15) + 'px'

        const container = document.querySelector('.x6-graph-container')
        if (!container) return

        const rect = container.getBoundingClientRect()
        const x = e.clientX - rect.left
        const y = e.clientY - rect.top

        // 判断是否在画布区域
        if (x > 0 && x < rect.width && y > 0 && y < rect.height) {
          indicator.style.background = 'rgba(67, 207, 124, 0.9)'
          document.body.style.cursor = 'copy'
        } else {
          indicator.style.background = 'rgba(245, 108, 108, 0.9)'
          document.body.style.cursor = 'not-allowed'
        }
      }

      // 鼠标释放处理
      const handleMouseUp = (e) => {
        // 清理
        document.body.style.cursor = 'default'
        document.removeEventListener('mousemove', handleMouseMove)
        document.removeEventListener('mouseup', handleMouseUp)

        // 移除指示器
        if (indicator && indicator.parentNode) {
          indicator.parentNode.removeChild(indicator)
        }

        // 只有真正拖拽了才添加节点
        if (!isDragging) return

        // 检查是否在画布区域
        const container = document.querySelector('.x6-graph-container')
        if (!container) return

        const rect = container.getBoundingClientRect()
        const x = e.clientX - rect.left
        const y = e.clientY - rect.top

        if (x > 0 && x < rect.width && y > 0 && y < rect.height) {
          // 转换为画布坐标（考虑缩放）
          const graphPosition = this.graph.clientToLocal(e.clientX, e.clientY)

          // 为拖拽的设备生成唯一ID，确保每个设备实例都有独立配置
          const uniqueComponent = {
            ...component,
            id: `${component.id}_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
          }

          console.log('拖拽设备到画布:', {
            原始ID: component.id,
            新ID: uniqueComponent.id,
            设备名称: component.name
          })

          // 调用 CanvasArea 组件的方法添加节点
          this.$refs.canvasArea.addNodeToCanvas(uniqueComponent, graphPosition.x - 75, graphPosition.y - 50)
        }
      }

      // 添加事件监听
      document.addEventListener('mousemove', handleMouseMove)
      document.addEventListener('mouseup', handleMouseUp)

      // 初始化指示器位置
      indicator.style.left = (event.clientX + 15) + 'px'
      indicator.style.top = (event.clientY + 15) + 'px'
    },

    // 节点事件处理
    handleNodeSelected(nodeData) {
      this.selectedNode = nodeData
    },

    // 节点取消选择/删除
    handleNodeDeselected(nodeId) {
      if (this.selectedNode && this.selectedNode.id === nodeId) {
        this.selectedNode = null
      } else {
        this.selectedNode = null
      }
    },

    // // 显示节点引脚信息悬浮窗
    // handleNodeMouseEnter(node, event) {
    //   if (this.$refs.nodeTooltip) {
    //     this.$refs.nodeTooltip.handleNodeMouseEnter(node, event);
    //   }
    // },

    // handleNodeMouseLeave(node) {
    //   if (this.$refs.nodeTooltip) {
    //     this.$refs.nodeTooltip.handleNodeMouseLeave(node);
    //   }
    // },

    // // 节点移动时更新悬浮窗位置
    // handleNodeMoving(node) {
    //   if (this.$refs.nodeTooltip) {
    //     this.$refs.nodeTooltip.handleNodeMoving(node);
    //   }
    // },

    // 右键菜单
    showContextMenu(cell, type) {
      this.$confirm(`确定要删除这个${type === 'node' ? '器件' : '连线'}吗？`, '提示', {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.graph.removeCell(cell)
        this.$message.success(`已删除器件`)
        if (type === 'node') this.selectedNode = null
      }).catch(() => { })
    },

    // 历史变化
    handleHistoryChange({ canUndo, canRedo }) {
      this.canUndo = canUndo
      this.canRedo = canRedo
    },

    // 缩放变化
    handleZoomChange(value) {
      this.zoomLevel = value
    },

    // 工具栏方法
    undo() {
      if (this.graph?.canUndo()) this.graph.undo()
    },

    redo() {
      if (this.graph?.canRedo()) this.graph.redo()
    },

    fitView() {
      if (!this.graph) return
      this.graph.centerContent()
      this.graph.zoomToFit({ padding: 20, maxScale: 1 })
    },

    resetZoom() {
      if (!this.graph) return
      this.graph.zoomTo(1)
      this.zoomLevel = 100
    },

    // 清空画布
    clearCanvas() {
      if (!this.graph) return
      this.$confirm('确定要清空画布吗？', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.graph.clearCells()
        this.selectedNode = null
        this.$message.success('画布已清空')
      }).catch(() => { })
    },

    // 清空所有连线
    clearAllConnections() {
      if (!this.graph) return
      this.$confirm('确定要清空所有连线吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        const edges = this.graph.getEdges()
        this.graph.removeCells(edges)
        this.$message.success('已清空所有连线')
      }).catch(() => { })
    },

    // 删除选中的器件
    deleteSelectedNode() {
      if (!this.selectedNode) {
        this.$message.warning('请先选择要删除的元器件')
        return
      }

      // 通过事件通知CanvasArea组件删除选中的节点，传递节点ID
      this.$refs.canvasArea.deleteSelectedNode(this.selectedNode.id)
    },

    // 事件处理方法
    handleNodeAdded(componentName) {
      this.$message.success(`已添加 ${componentName}`)
    },

    handleCellsDeleted(count) {
      this.$message.success(`已删除 ${count} 个器件`)
    },

    handleCellsCopied() {
      this.$message.success('已复制到剪贴板')
    },

    handleCellsPasted() {
      this.$message.success('已粘贴')
    },

    handleCanvasCleared() {
      this.selectedNode = null
    },

    handleConnectionsCleared() {
      this.$message.success('已清空所有连线')
    },

    // 处理节点删除事件
    handleNodeDeleted(nodeData) {
      this.selectedNode = null
      // 清理已删除设备的配置
      if (nodeData && nodeData.id) {
        this.cleanupDeviceConfig(nodeData.id)
      }
    },

    // 清理已删除设备的配置
    cleanupDeviceConfig(deviceId) {
      if (this.deviceConfigs[deviceId]) {
        this.$delete(this.deviceConfigs, deviceId)
        console.log('已清理设备配置:', deviceId)
      }
      if (this.uniqueIdentifications[deviceId]) {
        this.$delete(this.uniqueIdentifications, deviceId)
      }
    },

    // 设备配置相关方法
    async openDeviceConfigDialog({ deviceType, deviceId }) {
      this.selectedDeviceType = deviceType
      this.selectedDeviceId = deviceId

      console.log('打开设备配置对话框:', { deviceType, deviceId })
      console.log('当前设备配置状态:', this.deviceConfigs)

      // 确保每个设备都有独立的配置对象
      // 如果设备配置不存在，初始化一个全新的默认配置
      if (!this.deviceConfigs[deviceId]) {
        console.log('初始化新设备配置:', deviceId)
        this.$set(this.deviceConfigs, deviceId, this.getDefaultDeviceConfig())
      } else {
        // 如果配置已存在，确保使用独立的配置对象（避免引用共享）
        console.log('使用现有设备配置:', deviceId, this.deviceConfigs[deviceId])
        this.deviceConfigs[deviceId] = { ...this.deviceConfigs[deviceId] }
      }

      // 如果是Zigbee设备，检查是否已经有协调器配置了uniqueIdentification
      if (deviceType.includes('zigbee') || deviceType.includes('Zigbee')) {
        // 查找已配置的协调器设备
        const coordinatorDevice = Object.values(this.deviceConfigs).find(config =>
          config.configured && config.deviceType === '0x00' && config.uniqueIdentification
        )

        if (coordinatorDevice && coordinatorDevice.uniqueIdentification) {
          // 如果当前设备不是协调器，则使用协调器的uniqueIdentification
          if (this.deviceConfigs[deviceId].deviceType !== '0x00') {
            this.deviceConfigs[deviceId].uniqueIdentification = coordinatorDevice.uniqueIdentification
            console.log('使用协调器的uniqueIdentification:', coordinatorDevice.uniqueIdentification)
          }
        }
      }

      // 传递设备自身的uniqueIdentification（如果有）
      if (this.uniqueIdentifications[deviceId]) {
        this.deviceConfigs[deviceId].uniqueIdentification = this.uniqueIdentifications[deviceId]
      }

      this.deviceConfigDialogVisible = true

      // 获取设备类型列表
      await this.getDeviceTypeOptions()
    },

    // 处理设备配置取消
    handleDeviceConfigCancel() {
      this.deviceConfigDialogVisible = false
    },

    // 处理设备配置确认 发送请求后台
    async handleDeviceConfigConfirm(emitData) {
      const { config, uniqueIdentification, deviceTypeOptions } = emitData

      // 模拟连接设备
      this.deviceConfigConnecting = true

      try {
        // 如果是Zigbee设备，发送配置请求
        if (this.selectedDeviceType.includes('zigbee') || this.selectedDeviceType.includes('Zigbee')) {
          // 获取token（这里需要根据实际情况获取）
          const token = localStorage.getItem('token') || ''

          // 获取完整的中文字符串设备类型
          const selectedOption = (deviceTypeOptions || this.deviceTypeOptions).find(option => option.value === config.deviceType)
          const fullDeviceType = selectedOption ? selectedOption.label : config.deviceType

          // 准备配置数据
          const configData = {
            equipmentConfigurationType: fullDeviceType, // 传递完整的中文字符串
            panId: config.panId,
            channel: config.channel,
            uniqueIdentification: uniqueIdentification || undefined // 如果已有uniqueIdentification则带上
          }

          // 发送配置请求
          const response = await saveSerialPortConfigApi(configData, token)

          if (response.data && response.data.code === 200) {
            // 保存返回的uniqueIdentification
            const newUniqueIdentification = response.data.data?.uniqueIdentification
            if (newUniqueIdentification && this.selectedDeviceId) {
              this.uniqueIdentifications[this.selectedDeviceId] = newUniqueIdentification
            }

            this.$message.success(response.data.msg || 'Zigbee设备配置成功！')
          } else {
            throw new Error(response.data?.msg || response.data?.message || '配置失败')
          }
        }

        else if (this.selectedDeviceType.includes('wifi') || this.selectedDeviceType.includes('WIFI')) {
          console.log('WIFI设备配置:', config)
        }

        else if (this.selectedDeviceType.includes('4G') || this.selectedDeviceType.includes('5G')
          || this.selectedDeviceType.includes('移动通讯节点')) {
          console.log('移动通讯节点设备配置:', config)
        }

        // 更新设备配置状态
        // 确保使用独立的配置对象（避免引用共享）
        const updatedConfig = {
          ...config,
          configured: true,
          connected: true,
          uniqueIdentification: uniqueIdentification || this.uniqueIdentifications[this.selectedDeviceId] || ''
        }

        // 保存配置到设备配置对象中，确保使用深度复制
        if (this.selectedDeviceId) {
          // 使用JSON序列化/反序列化确保完全独立的配置对象
          this.deviceConfigs[this.selectedDeviceId] = JSON.parse(JSON.stringify(updatedConfig))
        }

        this.deviceConfigDialogVisible = false

        // 记录配置信息到控制台
        console.log('设备配置信息:', updatedConfig)
        console.log('当前所有设备配置:', this.deviceConfigs)

      } catch (error) {
        console.error('设备配置失败:', error)
        this.$message.error('设备配置失败: ' + (error.response?.data?.message || error.message || '未知错误'))
      } finally {
        this.deviceConfigConnecting = false
      }
    },

    // 获取默认设备配置对象
    getDefaultDeviceConfig() {
      return {
        deviceType: '',
        panId: '',
        channel: '',
        wifiName: '',
        wifiPassword: '',
        protocol: '',
        workMode: '',
        serverIp: '',
        serverPort: '',
        configured: false,
        connected: false,
        timeout: 5000,
        autoReconnect: true,
        uniqueIdentification: ''
      }
    },

    // 这里开始传感器相关方法
    // 打开传感器阈值配置对话框
    openSensorThresholdDialog(sensorData) {
      // 支持两种参数格式：直接传递节点对象或传递参数对象
      let sensorId, sensorName, sensorType

      if (sensorData && sensorData.sensorId) {
        // 从CanvasArea传递的参数对象格式
        sensorId = sensorData.sensorId
        sensorName = sensorData.sensorName || '未知传感器'
        sensorType = sensorData.sensorType || '传感器'
      } else if (sensorData && sensorData.id) {
        // 从属性面板传递的节点对象格式
        sensorId = sensorData.id
        sensorName = sensorData.name || '未知传感器'
        sensorType = sensorData.category || '传感器'
      } else {
        this.$message.error('无法获取传感器信息')
        return
      }

      this.selectedSensorId = sensorId
      this.selectedSensorName = sensorName
      this.selectedSensorType = sensorType

      // 检查是否是传感器类型，只有传感器才需要配置阈值
      const isSensor = this.selectedSensorType.includes('传感器')
      if (!isSensor) {
        this.$message.warning('该设备类型不需要配置阈值')
        return
      }

      this.sensorThresholdDialogVisible = true
      console.log('打开传感器阈值配置对话框:', {
        sensorId: this.selectedSensorId,
        sensorName: this.selectedSensorName,
        sensorType: this.selectedSensorType
      })
    },

    // 处理传感器阈值配置确认
    // 这里根据设备类型 具体的类型 分if else 处理 调取后台接口
    handleSensorThresholdConfirm(thresholdData) {
      console.log('传感器阈值配置确认:', thresholdData)

      this.sensorThresholdConnecting = true

      try {
        // 保存阈值配置到对应的传感器ID
        this.sensorThresholds[thresholdData.sensorId] = thresholdData.thresholdConfig

        // 显示成功消息
        this.$message.success(`传感器 ${thresholdData.sensorName} 阈值配置保存成功`)

        // 关闭对话框
        this.sensorThresholdDialogVisible = false
        this.sensorThresholdConnecting = false

        // 调试：显示当前所有传感器的阈值配置
        console.log('当前所有传感器阈值配置:', this.sensorThresholds)

      } catch (error) {
        console.error('保存传感器阈值配置失败:', error)
        this.$message.error('保存传感器阈值配置失败')
        this.sensorThresholdConnecting = false
      }
    },

    // 处理传感器阈值配置取消
    handleSensorThresholdCancel() {
      console.log('传感器阈值配置取消')
      this.sensorThresholdDialogVisible = false
      this.sensorThresholdConnecting = false
    },

    // 获取传感器的阈值配置
    getSensorThreshold(sensorId) {
      return this.sensorThresholds[sensorId] || null
    },

    // 检查传感器是否已配置阈值
    isSensorThresholdConfigured(sensorId) {
      const threshold = this.sensorThresholds[sensorId]
      return threshold && threshold.configured === true
    },

    // 判断是否为传感器类型
    isSensorType(category) {
      if (!category) return false
      return category.includes('传感器')
    },

    // 新增：获取设备类型选项的方法
    async getDeviceTypeOptions() {
      try {
        const response = await getDeviceTypeList()

        // 根据接口返回的数据格式进行处理
        // 假设返回的数据结构为: { rows: ["协调器 0x00", "路由器 0x01", "终端器 0x02"] }
        if (response.data && response.data.rows) {
          this.deviceTypeOptions = response.data.rows.map(item => {
            // 将完整的设备类型字符串作为标签显示，如 "协调器 0x00"
            // 同时提取最后部分作为值
            const parts = item.split(' ')
            const label = item // 使用完整字符串作为标签
            const value = parts[parts.length - 1] // 最后部分作为值

            return {
              label: label,
              value: value
            }
          })
        } else {
          // 如果返回数据格式不符合预期，使用默认选项
          this.deviceTypeOptions = [
            { label: '协调器', value: '0x00' },
            { label: '路由器', value: '0x01' },
            { label: '终端器', value: '0x02' }
          ]
        }
      } catch (error) {
        console.error('获取设备类型列表失败:', error)
        this.$message.error('获取设备类型列表失败')

        // 出错时使用默认选项
        this.deviceTypeOptions = [
          { label: '协调器', value: '0x00' },
          { label: '路由器', value: '0x01' },
          { label: '终端器', value: '0x02' }
        ]
      }
    },
  }
}

</script>

<style lang="scss" scoped>
// 硬件模拟X6页面样式 单独的文件
@import './scss/hardware-simulation-x6.scss';
</style>