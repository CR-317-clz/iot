<template>
  <div v-show="visible" class="node-tooltip" :style="{ left: position.x + 'px', top: position.y + 'px' }">
    <div v-html="content"></div>
  </div>
</template>

<script>
export default {
  name: 'NodeTooltip',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    content: {
      type: String,
      default: ''
    },
    position: {
      type: Object,
      default: () => ({ x: 0, y: 0 })
    },
    graph: {
      type: Object,
      default: null
    }
  },
  data() {
    return {
      currentHoveredNode: null, // 当前悬停的节点
      tooltipWidth: 180 // 悬浮窗宽度
    }
  },
  methods: {
    // 显示节点引脚信息悬浮窗
    showNodeTooltip(node, event) {
      this.hideNodeTooltip();

      const nodeData = node.getData();
      if (!nodeData || !nodeData.pins || nodeData.pins.length === 0) {
        return;
      }

      // 获取节点的所有端口（引脚）
      const ports = node.getPorts();
      if (!ports || ports.length === 0) return;

      // 获取节点的包围盒
      const nodeBBox = node.getBBox();
      // 获取画布容器的位置信息
      const containerRect = document.querySelector('.x6-graph-container').getBoundingClientRect();

      // 找到鼠标最接近的引脚
      let closestPin = null;
      let minDistance = Infinity;
      let closestPort = null;
      let closestPortPosition = { x: 0, y: 0 };

      ports.forEach(port => {
        // 计算端口的绝对坐标（相对于页面）
        let portX, portY;
        // 只处理左右两侧
        if (port.group === 'left') {
          portX = nodeBBox.x;
          portY = nodeBBox.y + nodeBBox.height / (ports.filter(p => p.group === 'left').length + 1) * (ports.filter(p => p.group === 'left').findIndex(p => p.id === port.id) + 1);
        } else if (port.group === 'right') {
          portX = nodeBBox.x + nodeBBox.width;
          portY = nodeBBox.y + nodeBBox.height / (ports.filter(p => p.group === 'right').length + 1) * (ports.filter(p => p.group === 'right').findIndex(p => p.id === port.id) + 1);
        } else {
          // 跳过非左右引脚
          return;
        }

        // 转换为页面坐标
        const absX = containerRect.left + portX * this.graph.zoom();
        const absY = containerRect.top + portY * this.graph.zoom();

        const distance = Math.sqrt(
          Math.pow(absX - event.clientX, 2) +
          Math.pow(absY - event.clientY, 2)
        );

        if (distance < minDistance) {
          minDistance = distance;
          closestPin = nodeData.pins.find(p => p.id === port.id);
          closestPort = port;
          closestPortPosition = { x: absX, y: absY };
        }
      });

      // 阈值范围内才显示
      const distanceThreshold = 30;
      if (closestPin && minDistance <= distanceThreshold) {
        this.displaySinglePinInfo(nodeData, closestPin);
        this.updateTooltipPositionByPort(closestPort, closestPortPosition);
        this.$emit('update:visible', true);
        document.addEventListener('mousemove', this.handleMouseMove);
      }
    },

    // 悬浮窗显示在引脚同侧且不遮挡元器件图片
    updateTooltipPositionByPort(port, portAbsPosition) {
      let offsetX = 0, offsetY = -10;

      if (port.group === 'left') {
        offsetX = -this.tooltipWidth - 16; // 左侧引脚，悬浮窗显示在左边
      } else if (port.group === 'right') {
        offsetX = 16; // 右侧引脚，悬浮窗显示在右边
      }
      this.$emit('update:position', {
        x: portAbsPosition.x + offsetX,
        y: portAbsPosition.y + offsetY
      });
    },

    // 辅助方法：显示单个引脚信息
    displaySinglePinInfo(nodeData, pin) {
      const typeClass = pin.type === 'output' ? 'output-pin' : 'input-pin';
      const typeLabel = pin.type === 'output' ? '输出' : '输入';

      let pinInfoHtml = `<div class="node-tooltip-content">
<div class="tooltip-header">
  <strong>${nodeData.name}</strong>
  <span>引脚详情</span>
</div>
<div class="tooltip-pins">
  <ul>
    <li class="pin-item ${typeClass}">
      <div class="pin-info">
        <span class="pin-name">${pin.label}</span>
        <span class="pin-type">${typeLabel}</span>
        ${pin.voltage ? `<span class="pin-voltage">${pin.voltage}V</span>` : ''}
        ${pin.protocol ? `<span class="pin-protocol">${pin.protocol}</span>` : ''}
      </div>
    </li>
  </ul>
</div></div>`;

      this.$emit('update:content', pinInfoHtml);
    },

    // 隐藏节点引脚信息悬浮窗
    hideNodeTooltip() {
      this.$emit('update:visible', false);
      this.$emit('update:content', '');
      document.removeEventListener('mousemove', this.handleMouseMove);
    },

    // 处理鼠标移动事件
    handleMouseMove(event) {
      if (this.currentHoveredNode) {
        // 重新计算最近的引脚并更新悬浮窗
        this.showNodeTooltip(this.currentHoveredNode, event);
      }
    },

    // 处理节点鼠标进入事件
    handleNodeMouseEnter(node, event) {
      this.currentHoveredNode = node;
      this.showNodeTooltip(node, event);
    },

    // 处理节点鼠标离开事件
    handleNodeMouseLeave(node) {
      // 只有当鼠标离开当前悬停的节点时才隐藏悬浮窗
      if (this.currentHoveredNode === node) {
        this.hideNodeTooltip();
        this.currentHoveredNode = null;
      }
    },

    // 处理节点移动事件
    handleNodeMoving(node) {
      if (this.currentHoveredNode === node) {
        // 在节点移动过程中隐藏悬浮窗
        this.hideNodeTooltip();
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.node-tooltip {
  position: fixed;
  z-index: 9999;
  background: rgba(0, 0, 0, 0.9);
  border-radius: 8px;
  padding: 12px 16px;
  max-width: 300px;
  color: white;
  font-size: 12px;
  pointer-events: none;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(8px);

  .node-tooltip-content {
    min-width: 180px;

    .tooltip-header {
      margin-bottom: 10px;
      padding-bottom: 6px;
      border-bottom: 1px solid rgba(255, 255, 255, 0.2);

      strong {
        display: block;
        font-size: 14px;
        margin-bottom: 2px;
      }

      span {
        font-size: 11px;
        color: #aaa;
      }
    }

    .tooltip-pins {
      ul {
        list-style: none;
        margin: 0;
        padding: 0;

        .pin-item {
          padding: 5px 0;
          font-size: 11px;

          &:not(:last-child) {
            border-bottom: 1px dotted rgba(255, 255, 255, 0.1);
          }

          &.input-pin {
            .pin-type {
              background: rgba(103, 194, 58, 0.2);
              color: #a0d911;
            }
          }

          &.output-pin {
            .pin-type {
              background: rgba(230, 162, 60, 0.2);
              color: #faad14;
            }
          }
        }

        .pin-info {
          display: flex;
          justify-content: space-between;
          align-items: center;

          .pin-name {
            flex: 1;
            font-weight: 500;
          }

          .pin-type {
            margin-left: 8px;
            padding: 2px 6px;
            border-radius: 4px;
            font-size: 10px;
            font-weight: bold;
          }

          .pin-voltage,
          .pin-protocol {
            margin-left: 8px;
            color: #bbb;
            font-size: 10px;
          }
        }
      }
    }
  }
}
</style>