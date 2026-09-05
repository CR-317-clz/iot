<template>
  <el-card class="canvas-panel">
    <div slot="header" class="panel-header">
      <div>
        <i class="el-icon-view"></i>
        <span class="iconview-font">接线画布</span>
        <span class="iconview-font1">温馨提醒:选择器件有传感器类型或者通信模块类型时,请先双击器件进行配置哦！</span>
      </div>

      <!-- 画布操作按钮 -->
      <div style="margin-top: 10px;">
        <el-button-group size="mini">
          <el-tooltip content="撤销 (Ctrl+Z)" placement="top">
            <el-button icon="el-icon-back" :disabled="!canUndo" @click="undo">
            </el-button>
          </el-tooltip>
          <el-tooltip content="重做 (Ctrl+Y)" placement="top">
            <el-button icon="el-icon-right" :disabled="!canRedo" @click="redo">
            </el-button>
          </el-tooltip>
          <el-tooltip content="适应画布" placement="top">
            <el-button icon="el-icon-full-screen" @click="fitView">
            </el-button>
          </el-tooltip>
          <el-tooltip content="重置缩放" placement="top">
            <el-button icon="el-icon-refresh" @click="resetZoom">
            </el-button>
          </el-tooltip>
        </el-button-group>

      </div>
    </div>

    <!-- X6 画布容器 -->
    <div ref="container" class="x6-graph-container"></div>

    <div class="canvas-tools">
      <el-checkbox v-model="showGrid" @change="toggleGrid">显示网格</el-checkbox>

      <el-slider v-model="zoomLevel" :min="50" :max="200" :step="10" :show-tooltip="false"
        style="width: 150px; margin: 0 15px;" @input="handleZoomChange">
      </el-slider>
      <span style="font-size: 12px; color: #909399; min-width: 45px;">{{ zoomLevel }}%</span>

      <el-button type="success" size="mini" icon="el-icon-caret-right" :disabled="edgeCount < 1"
        @click="toggleSimulateCircuit" style="margin-left: 8px;">
        {{ circuitSimulating ? '停止线路' : '启动线路' }}
      </el-button>

      <el-tag v-if="edgeCount > 0" :type="allConnectionsValid ? 'success' : 'warning'" size="small"
        style="margin-left: auto;">
        连线: {{ edgeCount }} 条
      </el-tag>
    </div>
  </el-card>
</template>

<script>
import { Graph } from '@antv/x6'
import { checkWiringRule } from '@/api/HardwareSimulation'

export default {
  name: 'CanvasArea',
  props: {
    showGrid: {
      type: Boolean,
      default: true
    },
    zoomLevel: {
      type: Number,
      default: 100
    },
    canUndo: {
      type: Boolean,
      default: false
    },
    canRedo: {
      type: Boolean,
      default: false
    },
    deviceConfigs: {
      type: Object,
      default: () => ({})
    }
  },
  data() {
    return {
      graph: null,
      resizeObserver: null,
      circuitSimulating: false, // 电路模拟器是否正在运行
      simulateInterval: null, // 电路模拟器定时器
      edgeCount: 0, // 连线数量
      allConnectionsValid: true // 所有连接是否有效
    }
  },
  mounted() {
    this.initGraph()
    this.initResizeObserver()
  },
  beforeDestroy() {
    if (this.graph) {
      this.graph.dispose()
      this.graph = null
    }
    if (this.resizeObserver) {
      this.resizeObserver.disconnect()
      this.resizeObserver = null
    }
    if (this.simulateInterval) {
      clearInterval(this.simulateInterval)
      this.simulateInterval = null
    }
  },
  methods: {
    initGraph() {
      const container = this.$refs.container
      if (!container) return

      // 创建 X6 画布
      this.graph = new Graph({
        container: container,
        width: container.clientWidth,
        height: container.clientHeight,
        background: {
          color: '#F5F5F5'
        },
        grid: {
          visible: this.showGrid,
          type: 'dot',
          args: {
            color: '#DCDFE6',
            thickness: 2
          }
        },
        panning: {
          enabled: true,
          modifiers: 'shift'
        },
        mousewheel: {
          enabled: true,
          modifiers: ['ctrl', 'meta'],
          minScale: 0.5,
          maxScale: 2
        },
        connecting: {
          snap: true,
          allowBlank: false,
          allowMulti: true,
          allowLoop: true,
          highlight: true,
          connector: 'rounded',
          connectionPoint: 'boundary',
          router: {
            name: 'manhattan',
            args: {
              padding: 10
            }
          },
          validateConnection({ sourceView, targetView }) {
            // 不能连接自己
            if (sourceView === targetView) {
              return false
            }

            // 获取源节点和目标节点的数据
            const sourceNode = sourceView.cell
            const targetNode = targetView.cell

            // 检查是否为相同的元器件类型（比较器件ID或类型）
            if (sourceNode && targetNode) {
              const sourceData = sourceNode.getData()
              const targetData = targetNode.getData()

              // 如果两个节点的器件类型相同，则不允许连接
              if (sourceData && targetData && sourceData.category === targetData.category) {
                return false
              }
            }

            // 移除端口有效性检查，允许任意连接（除了相同类型的器件）
            return true
          }
        },
        highlighting: {
          magnetAdsorbed: {
            name: 'stroke',
            args: {
              attrs: {
                fill: '#fff',
                stroke: '#31d0c6',
                strokeWidth: 4,
              },
            },
          },
        },
        selecting: {
          enabled: true,
          rubberband: true,
          showNodeSelectionBox: true,
        },
        keyboard: {
          enabled: true,
        },
        clipboard: {
          enabled: true,
        },
        history: {
          enabled: true,
        }
      })

      // 注册事件
      this.registerEvents()

      // 绑定快捷键
      this.bindShortcuts()

      // 暴露 graph 实例给父组件
      this.$emit('graph-ready', this.graph)
    },

    initResizeObserver() {
      const container = this.$refs.container
      if (!container) return

      this.resizeObserver = new ResizeObserver(entries => {
        for (const entry of entries) {
          if (this.graph) {
            this.graph.resize(entry.contentRect.width, entry.contentRect.height)
          }
        }
      })

      this.resizeObserver.observe(container)
    },

    registerEvents() {
      // 空白处点击 - 取消选中
      this.graph.on('blank:click', () => {
        this.graph.cleanSelection()
        this.$emit('node-deselected')
      })

      // 节点点击 - 选中并显示属性
      this.graph.on('node:click', ({ node, e }) => {
        e.stopPropagation()
        this.graph.cleanSelection()
        this.graph.select(node)
        this.$emit('node-selected', { ...node.getData() })
      })

      // 节点选中事件（备用）
      this.graph.on('node:selected', ({ node }) => {
        this.$emit('node-selected', { ...node.getData() })
      })

      // 节点取消选中
      this.graph.on('node:unselected', ({ node }) => {
        const nodeData = node.getData()
        this.$emit('node-deselected', nodeData.id)
      })

      // 节点双击
      this.graph.on('node:dblclick', ({ node }) => {
        const nodeData = node.getData()

        // 判断是否为通信模块（打开设备配置对话框）
        if (nodeData.category === '通信模块') {
          // 根据设备名称判断设备类型
          let deviceType = ''
          const deviceName = nodeData.name.toLowerCase()

          if (deviceName.includes('zigbee')) {
            deviceType = 'zigbee'
          } else if (deviceName.includes('wifi') || deviceName.includes('无线')) {
            deviceType = 'wifi'
          } else if (deviceName.includes('蓝牙') || deviceName.includes('bluetooth') || deviceName.includes('ble')) {
            deviceType = '蓝牙'
          } else if (deviceName.includes('lora')) {
            deviceType = 'lora'
          } else if (deviceName.includes('4g')) {
            deviceType = '4G'
          } else if (deviceName.includes('5g')) {
            deviceType = '5G'
          } else {
            deviceType = 'zigbee' // 默认设备类型
          }

          this.$emit('open-serial-dialog', { deviceType, deviceId: nodeData.id })
        }
        // 判断是否为传感器类型（打开阈值配置对话框）
        else if (nodeData.category && nodeData.category.includes('传感器')) {
          this.$emit('open-threshold-dialog', {
            sensorType: nodeData.category,
            sensorName: nodeData.name,
            sensorId: nodeData.id
          })
        }
      })

      // 节点悬停事件 - 显示引脚信息悬浮窗
      this.graph.on('node:mouseenter', ({ node, e }) => {
        this.$emit('node-mouseenter', node, e)
      })

      this.graph.on('node:mouseleave', ({ node, e }) => {
        this.$emit('node-mouseleave', node, e)
      })

      // 节点移动时更新悬浮窗位置
      this.graph.on('node:moving', ({ node, e }) => {
        this.$emit('node-moving', node, e)
      })

      // 连线创建与删除
      this.graph.on('edge:connected', ({ edge }) => {
        this.edgeCount = this.graph.getEdges().length
        this.validateEdge(edge)
      })

      this.graph.on('edge:removed', () => {
        this.edgeCount = this.graph.getEdges().length
        this.updateAllConnectionsValid()
      })

      // 右键菜单
      this.graph.on('node:contextmenu', ({ e, node }) => {
        e.preventDefault()
        this.$emit('show-context-menu', node, 'node')
      })

      this.graph.on('edge:contextmenu', ({ e, edge }) => {
        e.preventDefault()
        this.$emit('show-context-menu', edge, 'edge')
      })

      // 历史变化
      this.graph.on('history:change', () => {
        this.$emit('history-change', {
          canUndo: this.graph.canUndo(),
          canRedo: this.graph.canRedo()
        })
      })

      // 缩放变化
      this.graph.on('scale', ({ sx }) => {
        this.$emit('zoom-change', Math.round(sx * 100))
      })
    },

    bindShortcuts() {
      // 删除
      this.graph.bindKey(['delete', 'backspace'], () => {
        const cells = this.graph.getSelectedCells()
        if (cells.length) {
          // 获取被删除节点的数据
          const deletedNodes = cells.filter(cell => cell.isNode()).map(cell => cell.getData())
          this.graph.removeCells(cells)
          this.$emit('cells-deleted', cells.length)

          // 通知父组件节点被删除
          if (deletedNodes.length > 0) {
            this.$emit('node-deleted', deletedNodes[0])
          }
        }
      })

      // 撤销/重做
      this.graph.bindKey('ctrl+z', () => this.undo())
      this.graph.bindKey('ctrl+y', () => this.redo())

      // 复制/粘贴
      this.graph.bindKey('ctrl+c', () => {
        const cells = this.graph.getSelectedCells()
        if (cells.length) {
          this.graph.copy(cells)
          this.$emit('cells-copied')
        }
      })

      this.graph.bindKey('ctrl+v', () => {
        if (!this.graph.isClipboardEmpty()) {
          const cells = this.graph.paste({ offset: 32 })
          this.graph.cleanSelection()
          this.graph.select(cells)
          this.$emit('cells-pasted')
        }
      })

      // 全选
      this.graph.bindKey('ctrl+a', () => {
        const nodes = this.graph.getNodes()
        if (nodes.length) this.graph.select(nodes)
      })
    },

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
      this.$emit('zoom-change', 100)
    },

    handleZoomChange(value) {
      this.graph?.zoomTo(value / 100)
    },

    toggleGrid(value) {
      this.graph?.setGridVisible(value)
    },

    // 线路模拟
    toggleSimulateCircuit() {
      if (this.circuitSimulating) {
        this.stopSimulateCircuit();
      } else {
        this.startSimulateCircuit();
      }
    },

    // 检查是否有未配置的通信模块设备
    checkUnconfiguredDevices() {
      if (!this.graph) return false;

      const nodes = this.graph.getNodes();
      let hasUnconfiguredDevice = false;
      const unconfiguredDevices = [];

      nodes.forEach(node => {
        const nodeData = node.getData();
        // 检查是否为通信模块且需要配置
        if (nodeData.category === '通信模块') {
          const deviceId = nodeData.id;
          const deviceConfig = this.deviceConfigs[deviceId];

          // 判断设备是否需要配置
          const deviceName = nodeData.name.toLowerCase();
          const needsConfig = deviceName.includes('zigbee') ||
            deviceName.includes('wifi') ||
            deviceName.includes('无线') ||
            deviceName.includes('4g') ||
            deviceName.includes('5g');

          // 如果需要配置但未配置，则添加到未配置设备列表
          if (needsConfig && (!deviceConfig || !deviceConfig.configured)) {
            hasUnconfiguredDevice = true;
            unconfiguredDevices.push(nodeData.name);
          }
        }
      });

      return { hasUnconfiguredDevice, unconfiguredDevices };
    },

    // 启动线路模拟
    startSimulateCircuit() {
      if (this.edgeCount < 1) {
        this.$message.warning('请先连线后再启动线路');
        return;
      }

      // 检查是否有未配置的通信模块设备
      const { hasUnconfiguredDevice, unconfiguredDevices } = this.checkUnconfiguredDevices();
      if (hasUnconfiguredDevice) {
        this.$confirm(
          `检测到以下通信模块设备未配置：${unconfiguredDevices.join(', ')}。未配置的设备可能无法正常工作，是否继续启动线路？`,
          '设备配置警告',
          {
            confirmButtonText: '继续启动',
            cancelButtonText: '取消',
            type: 'warning'
          }
        ).then(() => {
          this.startCircuitSimulation();
        }).catch(() => {
          // 用户取消操作
        });
        return;
      }

      this.startCircuitSimulation();
    },

    // 实际的线路模拟启动逻辑
    startCircuitSimulation() {
      this.circuitSimulating = true;
      const edges = this.graph.getEdges();

      // 添加SVG滤镜定义（如果不存在）
      this.addSVGFilters();

      // 初始化所有连线为起始状态
      edges.forEach((edge, index) => {
        // 为每条连线设置不同的动画延迟，创建波浪效果
        const delay = index * 200; // 每条连线延迟200ms

        setTimeout(() => {
          this.startEdgeAnimation(edge);
        }, delay);
      });

      // 创建连续动画循环
      this.simulateInterval = setInterval(() => {
        edges.forEach((edge, index) => {
          // 为每条连线设置不同的动画延迟，创建波浪效果
          const delay = index * 200;

          setTimeout(() => {
            this.startEdgeAnimation(edge);
          }, delay);
        });
      }, 2000); // 每2秒重新开始一次波浪动画

      this.$message.success('模拟线路已启动！');
    },

    // 为单条连线启动动画
    startEdgeAnimation(edge) {
      if (!this.circuitSimulating) return;

      // 第一阶段：从起点到终点的流动效果
      edge.setAttrs({
        line: {
          stroke: '#67C23A',
          strokeWidth: 3,
          filter: 'url(#softGlow)',
          strokeDasharray: '8, 4',
          strokeDashoffset: 0,
          targetMarker: {
            name: 'path',
            d: 'M 0 -4 L 8 0 L 0 4 z',
            fill: '#67C23A',
            stroke: '#67C23A',
            strokeWidth: 1
          }
        }
      });

      // 使用requestAnimationFrame实现流畅的动画
      const animationDuration = 1800; // 稍微延长动画时间
      const startTime = Date.now();

      const animate = () => {
        if (!this.circuitSimulating) return;

        const elapsed = Date.now() - startTime;
        const progress = Math.min(elapsed / animationDuration, 1);

        // 使用缓动函数创建更自然的动画
        const easeProgress = this.easeInOutCubic(progress);

        // 计算颜色渐变 - 更平滑的过渡
        const colorProgress = Math.sin(progress * Math.PI * 2);
        const strokeColor = this.interpolateColor(
          '#67C23A',
          '#a0e475',
          (colorProgress + 1) / 2
        );

        // 计算线宽变化 - 更细微的变化
        const strokeWidth = 2.5 + Math.sin(progress * Math.PI * 3) * 0.8;

        // 计算虚线偏移，创建流动效果
        const dashOffset = -easeProgress * 12;

        // 计算透明度变化 - 更柔和的闪烁
        const opacity = 0.85 + 0.15 * Math.sin(progress * Math.PI * 2);

        // 计算发光强度变化并应用到滤镜
        const glowIntensity = 0.5 + 0.5 * Math.sin(progress * Math.PI);
        const filterId = glowIntensity > 0.7 ? 'url(#softGlow)' : '';

        edge.setAttrs({
          line: {
            stroke: strokeColor,
            strokeWidth: strokeWidth,
            filter: filterId,
            strokeDasharray: '8, 4',
            strokeDashoffset: dashOffset,
            opacity: opacity,
            targetMarker: {
              name: 'path',
              d: 'M 0 -4 L 8 0 L 0 4 z',
              fill: strokeColor,
              stroke: strokeColor,
              strokeWidth: 1
            }
          }
        });

        if (progress < 1) {
          requestAnimationFrame(animate);
        }
      };

      requestAnimationFrame(animate);
    },


    // 缓动函数 - 三次缓动
    easeInOutCubic(t) {
      return t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2;
    },

    // 颜色插值函数
    interpolateColor(color1, color2, factor) {
      const hex1 = color1.replace('#', '');
      const hex2 = color2.replace('#', '');

      const r1 = parseInt(hex1.substring(0, 2), 16);
      const g1 = parseInt(hex1.substring(2, 4), 16);
      const b1 = parseInt(hex1.substring(4, 6), 16);

      const r2 = parseInt(hex2.substring(0, 2), 16);
      const g2 = parseInt(hex2.substring(2, 4), 16);
      const b2 = parseInt(hex2.substring(4, 6), 16);

      const r = Math.round(r1 + (r2 - r1) * factor);
      const g = Math.round(g1 + (g2 - g1) * factor);
      const b = Math.round(b1 + (b2 - b1) * factor);

      return `#${((1 << 24) + (r << 16) + (g << 8) + b).toString(16).slice(1)}`;
    },

    // 添加SVG滤镜定义
    addSVGFilters() {
      const defs = this.graph?.container?.querySelector('defs');
      if (!defs) return;

      // 检查是否已存在滤镜
      if (defs.querySelector('#softGlow')) return;

      // 创建柔和的发光滤镜
      const softGlowFilter = document.createElementNS('http://www.w3.org/2000/svg', 'filter');
      softGlowFilter.setAttribute('id', 'softGlow');
      softGlowFilter.setAttribute('x', '-50%');
      softGlowFilter.setAttribute('y', '-50%');
      softGlowFilter.setAttribute('width', '200%');
      softGlowFilter.setAttribute('height', '200%');

      // 高斯模糊
      const feGaussianBlur = document.createElementNS('http://www.w3.org/2000/svg', 'feGaussianBlur');
      feGaussianBlur.setAttribute('stdDeviation', '3');
      feGaussianBlur.setAttribute('result', 'coloredBlur');

      // 合并原始图像和模糊效果
      const feMerge = document.createElementNS('http://www.w3.org/2000/svg', 'feMerge');
      const feMergeNode1 = document.createElementNS('http://www.w3.org/2000/svg', 'feMergeNode');
      feMergeNode1.setAttribute('in', 'coloredBlur');
      const feMergeNode2 = document.createElementNS('http://www.w3.org/2000/svg', 'feMergeNode');
      feMergeNode2.setAttribute('in', 'SourceGraphic');

      softGlowFilter.appendChild(feGaussianBlur);
      feMerge.appendChild(feMergeNode1);
      feMerge.appendChild(feMergeNode2);
      softGlowFilter.appendChild(feMerge);

      defs.appendChild(softGlowFilter);
    },

    // 停止线路模拟
    stopSimulateCircuit() {
      this.circuitSimulating = false;
      if (this.simulateInterval) {
        clearInterval(this.simulateInterval);
        this.simulateInterval = null;
      }
      // 恢复连线样式，使用原始的线颜色
      const edges = this.graph.getEdges();
      edges.forEach(edge => {
        const edgeData = edge.getData();
        const wireColor = edgeData.wireColor || '#409EFF';
        edge.setAttrs({
          line: {
            stroke: wireColor,
            strokeWidth: 2,
            filter: '',
            opacity: 1,
            strokeDasharray: '',
            strokeDashoffset: 0,
            targetMarker: 'classic'
          }
        });
      });
      this.$message.info('模拟线路已停止');
    },

    // 暴露给父组件的方法
    addNodeToCanvas(component, x, y) {
      if (!this.graph || !component) return

      // 确保 pins 存在且有效
      const pins = component.pins || []

      // 检查组件是否有图片以及引脚是否有具体位置信息
      const hasImage = component.imageUrl
      const hasLocationPins = component.pin && component.pin.some(pin =>
        pin.location && pin.location !== '未知' && pin.location !== ''
      )

      let ports

      if (hasImage && hasLocationPins) {
        // 如果有图片且有具体位置信息，按照左右两侧排列引脚
        const leftPins = []
        const rightPins = []
        const topPins = []
        const bottomPins = []

        component.pin.forEach((pin, index) => {
          if (pin.location && pin.location !== '未知' && pin.location !== '') {
            const pinData = {
              id: `${pin.pinId}_${index}`,
              attrs: {
                text: {
                  text: pin.pinName || pin.pinId,  // 中文引脚名称
                  fontSize: 12,  // 字体大小14
                  fill: '#000000',  // 黑色文字
                  fontFamily: 'Microsoft YaHei, sans-serif'  // 中文字体
                }
              }
            }

            // 根据引脚位置分配到不同的数组
            if (pin.location === '左' || pin.location.toLowerCase() === 'left') {
              leftPins.push(pinData)
            } else if (pin.location === '右' || pin.location.toLowerCase() === 'right') {
              rightPins.push(pinData)
            } else if (pin.location === '上' || pin.location.toLowerCase() === 'top') {
              topPins.push(pinData)
            } else if (pin.location === '下' || pin.location.toLowerCase() === 'bottom') {
              bottomPins.push(pinData)
            }
          }
        })

        // 创建各个方向的引脚配置
        ports = {
          groups: {
            left: {
              position: 'left',
              attrs: {
                circle: {
                  r: 6,
                  magnet: true,
                  stroke: '#67C23A', // 输入引脚颜色
                  strokeWidth: 2,
                  fill: '#fff'
                }
              },
              // 左侧标签放在圆点左侧
              label: {
                position: {
                  name: 'left',  // 标签在圆点左侧
                  args: {
                    offset: 8  // 与圆点的距离，稍微大一点避免重叠
                  }
                }
              }
            },
            right: {
              position: 'right',
              attrs: {
                circle: {
                  r: 6,
                  magnet: true,
                  stroke: '#E6A23C', // 输出引脚颜色
                  strokeWidth: 2,
                  fill: '#fff'
                }
              },
              // 右侧标签放在圆点右侧
              label: {
                position: {
                  name: 'right',  // 标签在圆点右侧
                  args: {
                    offset: 8  // 与圆点的距离
                  }
                }
              }
            },
            top: {
              position: 'top',
              attrs: {
                circle: {
                  r: 6,
                  magnet: true,
                  stroke: '#67C23A',
                  strokeWidth: 2,
                  fill: '#fff'
                }
              },
              // 上方标签放在圆点上方
              label: {
                position: {
                  name: 'top',
                  args: {
                    offset: 8
                  }
                }
              }
            },
            bottom: {
              position: 'bottom',
              attrs: {
                circle: {
                  r: 6,
                  magnet: true,
                  stroke: '#67C23A',
                  strokeWidth: 2,
                  fill: '#fff'
                }
              },
              // 下方标签放在圆点下方
              label: {
                position: {
                  name: 'bottom',
                  args: {
                    offset: 8
                  }
                }
              }
            }
          },
          items: [
            // 左侧引脚
            ...leftPins.map(pin => ({
              ...pin,
              group: 'left'
            })),
            // 右侧引脚
            ...rightPins.map(pin => ({
              ...pin,
              group: 'right'
            })),
            // 上侧引脚
            ...topPins.map(pin => ({
              ...pin,
              group: 'top'
            })),
            // 下侧引脚
            ...bottomPins.map(pin => ({
              ...pin,
              group: 'bottom'
            }))
          ]
        }
      }

      else {
        // 原来的逻辑：创建端口配置 - 所有引脚都放在底部
        ports = {
          groups: {
            bottom: {
              position: 'bottom',
              attrs: {
                circle: {
                  r: 6,
                  magnet: true,
                  stroke: '#67C23A', // 输入引脚颜色
                  strokeWidth: 2,
                  fill: '#fff'
                }
              },
              // 设置引脚标签在上方显示
              label: {
                position: 'top',
                attrs: {
                  text: {
                    fontSize: 10,
                    fill: '#606266'
                  }
                }
              }
            }
          },
          items: pins.map((pin) => ({
            id: pin.id,
            group: 'bottom', // 所有引脚都使用底部组
            label: { text: pin.label },
            attrs: { text: { fontSize: 10, fill: '#606266' } }
          }))
        }
      }

      let nodeConfig

      if (hasImage) {
        // 如果有图片，使用图片节点
        nodeConfig = {
          x, y,
          width: 150,
          height: 100,
          shape: 'image',
          image: component.imageUrl,
          attrs: {
            image: {
              'xlink:href': component.imageUrl,
              width: 150,
              height: 100,
              x: 0,
              y: 0,
              preserveAspectRatio: 'xMidYMid meet' // 保持图片比例并居中
            },
            // 添加一个透明的边框用于选中效果
            body: {
              fill: 'transparent',
              stroke: 'transparent',
              strokeWidth: 0,
              cursor: 'pointer'
            }
          },
          ports,
          data: { id: component.id || `device_${Date.now()}`, ...component }
        }
      } else {
        // 如果没有图片，使用原来的矩形节点
        nodeConfig = {
          x, y,
          width: 150,
          height: 100,
          shape: 'rect',
          attrs: {
            body: {
              stroke: component.color || '#409EFF',
              strokeWidth: 2,
              fill: '#ffffff',
              rx: 8,
              ry: 8,
              cursor: 'pointer'
            },
            label: {
              text: component.name,
              fontSize: 13,
              fontWeight: 'bold',
              fill: '#303133',
              pointerEvents: 'none'
            }
          },
          ports,
          data: { id: component.id || `device_${Date.now()}`, ...component }
        }
      }

      // 添加节点
      const node = this.graph.addNode(nodeConfig)

      // 选中新添加的节点
      this.graph.cleanSelection()
      this.graph.select(node)
      this.$emit('node-added', component.name)
    },

    // 验证连线
    validateEdge(edge) {
      const source = edge.getSource();
      const target = edge.getTarget();

      // 获取源节点和目标节点的数据
      let sourceNode, targetNode;
      let sourcePortId, targetPortId;

      // 源端信息
      if (source.cell) {
        sourceNode = this.graph.getCellById(source.cell);
        sourcePortId = source.port; // 端口 ID
      } else {
        console.warn("无法获取源节点信息");
        return;
      }

      // 目标端信息
      if (target.cell) {
        targetNode = this.graph.getCellById(target.cell);
        targetPortId = target.port; // 端口 ID
      } else {
        console.warn("无法获取目标节点信息");
        return;
      }

      const sourceData = sourceNode.getData();
      const targetData = targetNode.getData();

      // 从端口ID中提取基础的pinId（去掉索引后缀）
      const extractPinId = (portId) => {
        if (!portId) return '';
        // 如果端口ID包含下划线和数字后缀（如 "VCC_0"），则提取前面的部分
        const match = portId.match(/^([^_]+)_\d+$/);
        return match ? match[1] : portId; // 返回基础pinId或原值
      };

      const payload = {
        sourceDeviceId: sourceData.id,
        sourcePin: extractPinId(sourcePortId),  // 提取基础pinId，如 "VCC"
        targetDeviceId: targetData.id,
        targetPin: extractPinId(targetPortId)   // 提取基础pinId，如 "GND"
      };

      // 查找源引脚的颜色
      let wireColor = '#409EFF'; // 默认蓝色
      if (sourceData && sourceData.pin) {
        const sourcePinId = extractPinId(sourcePortId);
        const sourcePin = sourceData.pin.find(pin => pin.pinId === sourcePinId);
        if (sourcePin && sourcePin.pinColour) {
          wireColor = sourcePin.pinColour;
        }
      }

      // 调用接口验证
      checkWiringRule(this.$store.state.token, payload)
        .then(response => {
          const { code, data } = response.data;

          if (code === 200 && data.valid) {
            // 合法连接，使用源引脚的颜色设置边样式
            edge.setAttrs({
              line: {
                stroke: wireColor,
                strokeWidth: 2,
                strokeDasharray: '',
                targetMarker: 'classic',
                style: { animation: 'ant-line-flow 30s linear infinite' }
              }
            });
            edge.setData({ isValid: true, wireColor: wireColor });

            // 检查是否有警告消息
            if (data.message && data.message.includes('警告')) {
              this.$message.warning(data.message);
            }
          }

          else {
            // 连接不合法，检查是否为警告级别
            if (data.level === 'warning') {
              // 警告级别的连接，允许连接但显示警告消息
              edge.setAttrs({
                line: {
                  stroke: '#E6A23C',
                  strokeWidth: 2,
                  strokeDasharray: '',
                  targetMarker: 'classic'
                }
              });
              edge.setData({ isValid: false, wireColor: '#E6A23C', warning: true });
              this.$message.warning(data.message || '连接存在警告');
            } else {
              // 错误级别的连接，移除连线
              this.$message.error(data.message || '连接不合法');
              this.graph.removeCells([edge]);
            }
          }
          this.updateAllConnectionsValid();
        })
        .catch(err => {
          console.error('验证失败:', err);
          this.$message.error('网络异常，请重试');
          this.graph.removeCells([edge]); // 移除非法连线
          this.updateAllConnectionsValid();
        });
    },

    // 更新所有连接是否有效
    updateAllConnectionsValid() {
      const edges = this.graph.getEdges();
      this.allConnectionsValid = edges.every(edge => {
        const data = edge.getData();
        return data.isValid !== false;
      });
    },

    // 清空画布
    clearCanvas() {
      if (!this.graph) return
      this.graph.clearCells()
      this.$emit('canvas-cleared')
    },

    // 清空所有连线
    clearAllConnections() {
      if (!this.graph) return
      const edges = this.graph.getEdges()
      this.graph.removeCells(edges)
      this.$emit('connections-cleared')
    },

    // 删除选中节点
    deleteSelectedNode(nodeId = null) {
      if (!this.graph) return
      let cellsToDelete = []

      // 如果提供了节点ID，则根据ID查找节点
      if (nodeId) {
        const node = this.graph.getNodes().find(n => n.getData().id === nodeId)
        if (node) {
          cellsToDelete = [node]
        }
      } else {
        // 否则使用当前选中的节点
        cellsToDelete = this.graph.getSelectedCells()
      }

      if (cellsToDelete.length) {
        // 获取被删除节点的数据
        const deletedNodes = cellsToDelete.filter(cell => cell.isNode()).map(cell => cell.getData())
        this.graph.removeCells(cellsToDelete)
        this.$emit('cells-deleted', cellsToDelete.length)

        // 通知父组件节点被删除
        if (deletedNodes.length > 0) {
          this.$emit('node-deleted', deletedNodes[0])
        }
      } else {
        this.$message.warning('请先选择要删除的元器件')
      }
    },
  }
}
</script>

<style lang="scss" scoped>
.canvas-panel {
  height: 100%;
  display: flex;
  flex-direction: column;

  ::v-deep .el-card__body {
    flex: 1;
    padding: 0;
    display: flex;
    flex-direction: column;
    overflow: hidden;
  }

  .x6-graph-container {
    flex: 1;
    position: relative;
    background: #FAFAFA;
    outline: none;
    cursor: default;

    &:focus {
      outline: none;
    }
  }

  .canvas-tools {
    padding: 12px 20px;
    border-top: 1px solid #EBEEF5;
    background: #F5F7FA;
    display: flex;
    align-items: center;
    gap: 15px;
  }
}

.iconview-font {
  letter-spacing: 2px;
  margin-left: 5px;
}

.iconview-font1 {
  letter-spacing: 2px;
  margin-left: 5px;
  margin-top: 20px;
  color: red;
  font-size: 14px;
  position: relative;
  left: 25%;
}
</style>