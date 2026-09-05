<template>
  <div class="center-top-video">
    <!-- 视频播放器 -->
    <div v-if="cameraInfo.streamUrl && cameraStatus !== 'offline'" class="video-player-wrapper">
      <video 
        ref="videoPlayer"
        class="video-element"
        autoplay
        muted
        playsinline
        @dblclick="handleVideoDoubleClick"
      >
        您的浏览器不支持 video 标签。
      </video>
      
      <!-- 视频控制栏 -->
      <div class="video-controls">
        <div class="control-left">
          <el-tag 
            :type="getCameraStatusType(cameraStatus)" 
            size="mini"
          >
            {{ getCameraStatusText(cameraStatus) }}
          </el-tag>
          <span class="camera-location" v-if="cameraInfo.location">
            <i class="el-icon-location"></i>
            {{ cameraInfo.location }}
          </span>
        </div>
        <div class="control-right">
          <el-button 
            type="text" 
            icon="el-icon-refresh" 
            size="mini"
            @click="reconnectVideo"
            title="重新连接"
          />
          <el-button 
            type="text" 
            icon="el-icon-full-screen" 
            size="mini"
            @click="handleFullscreen"
            title="全屏"
          />
        </div>
      </div>
    </div>
    
    <!-- 无视频流提示 -->
    <div v-else-if="!loading && !cameraInfo.streamUrl" class="video-placeholder">
      <i class="el-icon-video-camera-solid"></i>
      <p>该摄像头暂无视频流地址</p>
      <p class="hint-text">请在设备管理中配置 RTSP 地址</p>
    </div>
    
    <!-- 离线提示 -->
    <div v-else-if="cameraStatus === 'offline'" class="video-offline">
      <i class="el-icon-warning-outline"></i>
      <p>摄像头离线</p>
      <p class="hint-text">无法获取视频流</p>
    </div>
    
    <!-- 加载中 -->
    <div v-else-if="loading" class="loading-container">
      <i class="el-icon-loading"></i>
      <div class="loading-text">视频加载中...</div>
    </div>
  </div>
</template>

<script>
/* global WebRtcStreamer */

import { getDeviceById } from '@/api/iot-device';
import { mapGetters } from "vuex";

export default {
  name: "centerTop",
  data() {
    return {
      loading: false,
      cameraInfo: {
        streamUrl: '',
        location: '',
        ipAddress: '',
        cameraName: '',
      },
      cameraStatus: 'online',
      webRtcServer: null,
      webRtcServerUrl: 'http://192.168.110.137:8000',
      error: null,
    };
  },
  computed: {
    ...mapGetters({
      configId: 'getConfigId',
      deviceId: 'getDeviceId'
    })
  },
  mounted() {
    this.loadCameraData();
  },
  beforeDestroy() {
    this.disconnectVideo();
  },
  watch: {
    configId: {
      handler(newId, oldId) {
        if (newId && newId !== oldId) {
          this.loadCameraData();
        }
      },
      immediate: false,
    },
    deviceId: {
      handler(newId, oldId) {
        if (newId && newId !== oldId) {
          console.log('🔄 摄像头-检测到deviceId变化:', { 旧值: oldId, 新值: newId })
          this.loadCameraData();
        }
      },
      immediate: false,
    },
  },
  methods: {
    // 加载摄像头数据
    async loadCameraData() {
      this.loading = true;
      this.error = null;
      
      try {
        const currentDeviceId = this.deviceId || '374750517349453800';
        console.log('🔍 摄像头-正在获取摄像头信息，deviceId:', currentDeviceId);
        
        const response = await getDeviceById(currentDeviceId);
        
        console.log('✅ 摄像头-API完整响应:', response);
        
        // 🔥 根据实际数据结构解析
        // 结构: response.data.data.cameraInfo
        let cameraData = null;
        
        if (response?.data?.code === 200 && response?.data?.data) {
          const deviceData = response.data.data;
          console.log('📦 设备数据:', deviceData);
          
          // 获取 cameraInfo 对象
          cameraData = deviceData.cameraInfo || deviceData;
          console.log('📷 摄像头数据:', cameraData);
          
          // 🔥 提取 streamUrl
          const streamUrl = cameraData.streamUrl || '';
          console.log('🎥 视频流地址:', streamUrl);
          
          // 更新摄像头信息
          this.cameraInfo = {
            streamUrl: streamUrl,
            location: cameraData.location || deviceData.location || '未知位置',
            ipAddress: cameraData.ipAddress || '',
            cameraName: cameraData.cameraName || deviceData.cameraName || '主摄像头',
            cameraCode: cameraData.cameraCode || '',
            cameraType: cameraData.cameraType || '',
            manufacturer: cameraData.manufacturer || '海康威视',
            model: cameraData.model || '',
            port: cameraData.port || 554,
          };
          
          // 更新摄像头状态
          this.cameraStatus = cameraData.status || 'online';
          
          console.log('✅ 解析后的摄像头信息:', this.cameraInfo);
          console.log('✅ 视频流地址:', this.cameraInfo.streamUrl);
          
          // 如果有视频流地址，初始化视频连接
          if (this.cameraInfo.streamUrl) {
            this.$nextTick(() => {
              this.initVideo();
            });
          } else {
            console.warn('⚠️ 未获取到视频流地址');
            this.cameraStatus = 'offline';
          }
        } else {
          throw new Error(response?.data?.message || '获取摄像头信息失败');
        }
      } catch (error) {
        console.error('❌ 加载摄像头数据失败:', error);
        this.error = error.message || '获取摄像头信息失败';
        this.cameraStatus = 'offline';
      } finally {
        this.loading = false;
      }
    },

    // 初始化视频连接
    initVideo() {
      try {
        if (typeof WebRtcStreamer === 'undefined') {
          console.error('WebRtcStreamer 未加载，请检查 public/webrtcstreamer.js');
          this.$message?.error('视频组件加载失败');
          return;
        }

        if (!this.$refs.videoPlayer) {
          console.warn('视频元素未找到');
          return;
        }

        this.disconnectVideo();
        
        this.cameraStatus = 'connecting';
        this.webRtcServer = new WebRtcStreamer(
          this.$refs.videoPlayer,
          this.webRtcServerUrl
        );
        
        console.log('🔗 正在连接RTSP流:', this.cameraInfo.streamUrl);
        
        this.webRtcServer.connect(
          this.cameraInfo.streamUrl, 
          null, 
          'rtsp_transport=tcp'
        );
        
        this.$refs.videoPlayer.onloadeddata = () => {
          this.cameraStatus = 'online';
          console.log('✅ 视频流连接成功');
        };
        
        this.$refs.videoPlayer.onerror = (error) => {
          console.error('❌ 视频播放错误:', error);
          this.cameraStatus = 'offline';
        };
        
      } catch (error) {
        console.error('❌ 初始化视频失败:', error);
        this.cameraStatus = 'offline';
        this.$message?.error('视频连接失败: ' + error.message);
      }
    },

    // 断开视频连接
    disconnectVideo() {
      if (this.webRtcServer) {
        try {
          this.webRtcServer.disconnect();
          this.webRtcServer = null;
        } catch (error) {
          console.error('断开视频连接失败:', error);
        }
      }
    },

    // 重新连接视频
    reconnectVideo() {
      if (!this.cameraInfo.streamUrl) {
        this.$message?.warning('该摄像头未配置 RTSP 地址');
        return;
      }
      
      this.$message?.info('正在重新连接...');
      this.disconnectVideo();
      this.$nextTick(() => {
        this.initVideo();
      });
    },

    // 全屏播放
    handleFullscreen() {
      const videoElement = this.$refs.videoPlayer;
      if (!videoElement) return;

      if (videoElement.requestFullscreen) {
        videoElement.requestFullscreen();
      } else if (videoElement.webkitRequestFullScreen) {
        videoElement.webkitRequestFullScreen();
      } else if (videoElement.mozRequestFullScreen) {
        videoElement.mozRequestFullScreen();
      } else if (videoElement.msRequestFullscreen) {
        videoElement.msRequestFullscreen();
      }
    },

    // 双击全屏
    handleVideoDoubleClick() {
      this.handleFullscreen();
    },

    // 获取摄像头状态类型
    getCameraStatusType(status) {
      const statusMap = {
        online: 'success',
        offline: 'danger',
        connecting: 'warning'
      };
      return statusMap[status] || 'info';
    },

    // 获取摄像头状态文本
    getCameraStatusText(status) {
      const textMap = {
        online: '在线',
        offline: '离线',
        connecting: '连接中'
      };
      return textMap[status] || '未知';
    },
  },
};
</script>

<style scoped lang="scss">
.center-top-video {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 8px;
  box-sizing: border-box;
  position: relative;
  background: rgba(0, 0, 0, 0.3);
  border-radius: 4px;
}

/* 视频播放器容器 */
.video-player-wrapper {
  position: relative;
  width: 100%;
  height: 100%;
  background: #000;
  border-radius: 4px;
  overflow: hidden;
}

/* 视频元素 */
.video-element {
  width: 100%;
  height: 100%;
  object-fit: contain;
  background: #000;
  display: block;
}

/* 视频控制栏 */
.video-controls {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.7), transparent);
  opacity: 0;
  transition: opacity 0.3s ease;
  
  .control-left {
    display: flex;
    align-items: center;
    gap: 12px;
    
    .camera-location {
      color: #fff;
      font-size: 12px;
      display: flex;
      align-items: center;
      gap: 4px;
      
      i {
        font-size: 14px;
      }
    }
  }
  
  .control-right {
    display: flex;
    gap: 4px;
    
    ::v-deep .el-button {
      color: #fff;
      padding: 4px 8px;
      
      &:hover {
        background: rgba(255, 255, 255, 0.2);
      }
    }
  }
}

/* 鼠标悬停显示控制栏 */
.video-player-wrapper:hover .video-controls {
  opacity: 1;
}

/* 无视频流提示 */
.video-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.6);
  color: #409EFF;
  border-radius: 4px;
  
  i {
    font-size: 64px;
    margin-bottom: 16px;
    opacity: 0.8;
  }
  
  p {
    margin: 8px 0;
    font-size: 16px;
    font-weight: 500;
  }
  
  .hint-text {
    font-size: 13px;
    color: rgba(255, 255, 255, 0.6);
    font-weight: normal;
  }
}

/* 离线提示 */
.video-offline {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.6);
  color: #F56C6C;
  border-radius: 4px;
  
  i {
    font-size: 64px;
    margin-bottom: 16px;
    opacity: 0.8;
  }
  
  p {
    margin: 8px 0;
    font-size: 16px;
    font-weight: 500;
  }
  
  .hint-text {
    font-size: 13px;
    color: rgba(255, 255, 255, 0.6);
    font-weight: normal;
  }
}

/* 加载中 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.6);
  color: #409EFF;
  border-radius: 4px;
  
  i {
    font-size: 48px;
    margin-bottom: 16px;
    animation: rotate 1.5s linear infinite;
  }
  
  .loading-text {
    font-size: 14px;
    color: rgba(255, 255, 255, 0.8);
  }
}

/* 旋转动画 */
@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .video-controls {
    padding: 6px 10px;
    
    .control-left {
      gap: 8px;
      
      .camera-location {
        font-size: 11px;
      }
    }
  }
  
  .video-placeholder,
  .video-offline {
    i {
      font-size: 48px;
    }
    
    p {
      font-size: 14px;
    }
    
    .hint-text {
      font-size: 12px;
    }
  }
}
</style>