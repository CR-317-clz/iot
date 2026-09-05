<template>
  <div class="meteorology-overview">
    <!-- 页面主卡片 -->
    <el-card class="page-card">
      <!-- 顶部标题栏 -->
      <div slot="header" class="page-header">
        <span class="page-title">
          <i class="el-icon-sunny"></i>
          气象站设备管理
        </span>
        <p class="page-desc">
          管理气象站设备（共 {{ deviceList.length }} 台设备）
        </p>
      </div>

      <!-- 内容区 -->
      <div class="content" v-loading="loading" element-loading-text="正在加载设备列表...">
        <!-- 设备卡片列表 -->
        <el-row :gutter="20" v-if="!loading && deviceList.length > 0">
          <el-col :span="8" v-for="device in deviceList" :key="device.id">
            <el-card class="device-card" @click.native="viewDeviceDetail(device)">
              <div class="device-info">
                <!-- 图标 -->
                <div class="device-icon">
                  <i :class="getDeviceIcon(device.type)"></i>
                </div>

                <!-- 设备信息 -->
                <div class="device-details">
                  <h4>{{ device.name }}</h4>
                  <p>类型：{{ device.type }}</p>
                  <p>
                    状态：
                    <span :class="['status', device.status === '在线' ? 'online' : 'offline']">
                      {{ device.status }}
                    </span>
                  </p>
                  <p>设备地址：{{ device.ip }}</p>

                  <!-- 操作按钮 -->
                  <div class="device-actions">
                    <el-button
                      type="info"
                      size="small"
                      @click.stop="viewDeviceDetail(device)"
                    >
                      详情
                    </el-button>
                    <el-button
                      type="info"
                      size="small"
                      @click.stop="viewVideoMonitor(device)"
                    >
                      视频监控
                    </el-button>
                    <el-button
                      type="info"
                      size="small"
                      @click.stop="viewDeviceConfig(device)"
                    >
                      设备配置
                    </el-button>
                  </div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 空状态 -->
        <div v-if="!loading && deviceList.length === 0" class="empty-state">
          <i class="el-icon-sunny empty-icon"></i>
          <p>暂无气象站设备</p>
          <el-button type="primary" size="small" @click="loadDeviceList">
            <i class="el-icon-refresh"></i>
            重新加载
          </el-button>
        </div>

        <!-- 错误状态 -->
        <div v-if="!loading && error" class="error-state">
          <i class="el-icon-warning-outline error-icon"></i>
          <p>{{ error }}</p>
          <el-button type="primary" size="small" @click="loadDeviceList">
            <i class="el-icon-refresh"></i>
            重试
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import { getAllMeteorologyDeviceList } from "@/api/iot-meteorology.js";

export default {
  name: "MeteorologyOverview",
  data() {
    return {
      deviceList: [], // 设备列表
      loading: false, // 加载状态
      error: "", // 错误信息
    };
  },
  created() {
    this.loadDeviceList();
  },
  methods: {
    /** 加载设备列表 */
    async loadDeviceList() {
      this.loading = true;
      this.error = "";
      try {
        const response = await getAllMeteorologyDeviceList();
        
        if (response.data && response.data.code === 200) {
          // 处理返回的数据，所有设备都使用"环境监测站"作为前缀
          this.deviceList = response.data.rows.map((item, index) => {
            return {
              id: item.deviceId,
              name: `气象监测站${index + 1}`,
              type: "气象监测站",
              status: item.deviceStatus === "normal" ? "在线" : "离线",
              ip: `${item.deviceAddr}`,
              location: "位置待配置",
              raw: item // 保存原始数据
            };
          });
        } else {
          throw new Error(response.data.msg || "获取设备列表失败");
        }
      } catch (error) {
        console.error("获取设备列表出错:", error);
        this.error = error.message || "设备数据加载失败";
      }
      this.loading = false;
    },

    /** 获取设备图标 */
    getDeviceIcon(type) {
      const iconMap = {
        "环境监测站": "el-icon-sunny",
        "气象站": "el-icon-sunny",
        default: "el-icon-monitor"
      };
      return iconMap[type] || iconMap.default;
    },

    /** 查看设备详情 */
    viewDeviceDetail(device) {
      this.$router.push({
        path: "/layout/meteorology/overview-data",
        query: {
          deviceId: device.id,
          deviceName: device.name,
          deviceAddr: device.raw.deviceAddr
        },
      });
    },

    /** 查看视频监控 */
    viewVideoMonitor(device) {
      this.$router.push({
        path: "/layout/video-monitor",
        query: { 
          deviceId: device.id,
          deviceName: device.name 
        },
      });
    },

    /** 查看设备配置 */
    viewDeviceConfig(device) {
      this.$router.push({
        path: "/layout/device-config",
        query: { 
          deviceId: device.id,
          deviceName: device.name 
        },
      });
    },

    /** 重新刷新设备列表 */
    refreshDeviceList() {
      this.loadDeviceList();
      this.$message.success("设备列表已刷新");
    },
  },
};
</script>

<style lang="scss" scoped>
.meteorology-overview {
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
    .device-card {
      margin-bottom: 28px;
      border-radius: 16px;
      border: none;
      background: linear-gradient(120deg, #f8fbff 0%, #f0f9f4 100%);
      box-shadow: 0 2px 12px rgba(64, 158, 255, 0.08);
      transition: all 0.25s ease;
      cursor: pointer;
      position: relative;

      &:hover {
        box-shadow: 0 8px 32px rgba(64, 158, 255, 0.18);
        transform: translateY(-4px) scale(1.03);
      }

      .device-info {
        display: flex;
        align-items: center;
        gap: 18px;
        padding: 18px 10px 10px 10px;

        .device-icon {
          width: 64px;
          height: 64px;
          min-width: 64px;
          background: linear-gradient(135deg, #409eff 0%, #67c23a 100%);
          border-radius: 14px;
          display: flex;
          align-items: center;
          justify-content: center;
          box-shadow: 0 4px 16px #409eff22;
          flex-shrink: 0;

          i {
            font-size: 36px;
            color: #fff;
          }
        }

        .device-details {
          flex: 1;
          justify-content: center;

          h4 {
            margin: 0 0 10px 0;
            color: #303133;
            font-size: 18px;
            font-weight: 700;
          }

          p {
            margin: 0 0 10px 0;
            color: #606266;
            font-size: 15px;

            .status {
              padding: 2px 10px;
              border-radius: 6px;
              font-size: 13px;
              font-weight: 600;

              &.online {
                background: #e1f3d8;
                color: #67c23a;
                border: 1px solid #67c23a;
              }

              &.offline {
                background: #fde2e2;
                color: #f56c6c;
                border: 1px solid #f56c6c;
              }
            }
          }

          .device-actions {
            display: flex;
            gap: 10px;
            justify-content: center;
            align-items: center;
            margin-top: 12px;
            transform: translateX(-45px);

            .el-button {
              width: 80px;
              height: 36px;
              border-radius: 8px;
              font-size: 14px;
              background-color: #409eff;
              color: #fff;
              border: none;
              box-shadow: 0 2px 8px #409eff22;
              transition: box-shadow 0.2s, transform 0.2s, background 0.2s;

              &:hover {
                box-shadow: 0 4px 16px #409eff44;
                transform: translateY(-2px) scale(1.08);
                color: #fff;
                opacity: 0.95;
              }

              &:focus {
                outline: none;
                box-shadow: 0 0 0 2px #409eff44;
              }
            }
          }
        }
      }

      /* 卡片右上角装饰圆点 */
      &::after {
        content: "";
        position: absolute;
        top: 12px;
        right: 18px;
        width: 18px;
        height: 18px;
        border-radius: 50%;
        background: linear-gradient(135deg, #409eff 60%, #67c23a 100%);
        opacity: 0.12;
      }
    }

    /* 空状态 & 错误状态样式 */
    .empty-state,
    .error-state {
      text-align: center;
      padding: 60px 20px;
      color: #909399;

      .empty-icon,
      .error-icon {
        font-size: 64px;
        color: #c0c4cc;
        margin-bottom: 16px;
      }

      .error-icon {
        color: #f56c6c;
      }

      p {
        font-size: 16px;
        margin-bottom: 20px;
      }
    }
  }
}
</style>