<template>
  <div class="video-monitor">
    <el-card class="page-card">
      <div slot="header" class="page-header">
        <span class="page-title">
          <i class="el-icon-video-camera"></i>
          视频监控
        </span>
        <p class="page-desc">实时查看各区域摄像头画面及状态</p>
      </div>
      <div class="content">
        <el-row :gutter="20">
          <el-col :span="8" v-for="camera in cameras" :key="camera.id">
            <el-card class="camera-card" :body-style="{padding: '10px'}">
              <div class="camera-video">
                <!-- 占位图或视频流，实际项目可替换为 <video> 标签 -->
                <img
                  v-if="!camera.stream"
                  :src="camera.placeholder"
                  alt="摄像头画面"
                  class="camera-img"
                />
                <video
                  v-else
                  :src="camera.stream"
                  controls
                  autoplay
                  muted
                  class="camera-video-player"
                ></video>
                <el-tag
                  :type="camera.status === '在线' ? 'success' : 'danger'"
                  class="camera-status"
                  size="mini"
                >
                  {{ camera.status }}
                </el-tag>
              </div>
              <div class="camera-info">
                <div class="camera-name">{{ camera.name }}</div>
                <div class="camera-location">
                  <i class="el-icon-location"></i> {{ camera.location }}
                </div>
                <div class="camera-time">
                  <i class="el-icon-time"></i> {{ camera.lastUpdate }}
                </div>
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
  name: "VideoMonitor",
  data() {
    return {
      cameras: [
        {
          id: 1,
          name: "1号摄像头",
          location: "1号楼入口",
          status: "在线",
          lastUpdate: "2025-07-16 10:20:01",
          placeholder: "https://img2.baidu.com/it/u=1815681536,3276907896&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=300",
          stream: "", // 可填写视频流地址
        },
        {
          id: 2,
          name: "2号摄像头",
          location: "2号楼走廊",
          status: "离线",
          lastUpdate: "2025-07-16 09:58:12",
          placeholder: "https://img2.baidu.com/it/u=1815681536,3276907896&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=300",
          stream: "",
        },
        {
          id: 3,
          name: "3号摄像头",
          location: "3号楼停车场",
          status: "在线",
          lastUpdate: "2025-07-16 10:18:45",
          placeholder: "https://img2.baidu.com/it/u=1815681536,3276907896&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=300",
          stream: "",
        },
        {
          id: 4,
          name: "4号摄像头",
          location: "4号楼后门",
          status: "在线",
          lastUpdate: "2025-07-16 10:19:30",
          placeholder: "https://img2.baidu.com/it/u=1815681536,3276907896&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=300",
          stream: "",
        },
        {
          id: 5,
          name: "5号摄像头",
          location: "园区大门",
          status: "离线",
          lastUpdate: "2025-07-16 09:40:10",
          placeholder: "https://img2.baidu.com/it/u=1815681536,3276907896&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=300",
          stream: "",
        },
        {
          id: 6,
          name: "6号摄像头",
          location: "仓库内",
          status: "在线",
          lastUpdate: "2025-07-16 10:17:55",
          placeholder: "https://img2.baidu.com/it/u=1815681536,3276907896&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=300",
          stream: "",
        },
      ],
    };
  },
};
</script>

<style lang="scss" scoped>
.video-monitor {
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
    .camera-card {
      margin-bottom: 18px;
      .camera-video {
        position: relative;
        .camera-img,
        .camera-video-player {
          width: 100%;
          height: 180px;
          object-fit: cover;
          border-radius: 8px;
          background: #222;
        }
        .camera-status {
          position: absolute;
          top: 10px;
          left: 10px;
        }
      }
      .camera-info {
        margin-top: 10px;
        .camera-name {
          font-size: 16px;
          font-weight: 600;
          color: #303133;
        }
        .camera-location,
        .camera-time {
          font-size: 13px;
          color: #909399;
          margin-top: 2px;
          i {
            margin-right: 2px;
          }
        }
      }
    }
  }
}
</style>