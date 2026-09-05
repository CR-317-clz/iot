<template>
  <div id="video-contianer">
    <video
      class="video"
      ref="video"
      preload="auto"
      autoplay
      muted
      playsinline
      width="100%"
      height="100%"
      style="object-fit: cover; width: 100%; height: 100%;"
    />
    <div
      class="mask"
      :class="{ 'active-video-border': selectStatus }"
      @click="handleClickVideo"
    ></div>
  </div>
</template>
 
<script>
import WebRtcStreamer from '@/public/webrtcstreamer'
 
export default {
  name: 'videoCom',
  props: {
    rtsp: {
      type: String,
      required: true,
    },
    isOn: {
      type: Boolean,
      default: false,
    },
    spareId: {
      type: Number,
    },
    selectStatus: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      socket: null,
      result: null, // 返回值
      pic: null,
      webRtcServer: null,
      clickCount: 0, // 用来计数点击次数
    }
  },
  watch: {
    rtsp() {
      // do something
      // console.log(this.rtsp)
      this.webRtcServer.disconnect()
      this.initVideo()
    },
  },
  beforeDestroy() {
    if (this.webRtcServer) {
      this.webRtcServer.disconnect()
    }
  },
  beforeCreate() {
    window.onbeforeunload = () => {
      if (this.webRtcServer) {
        this.webRtcServer.disconnect()
      }
    }
  },
  created() {},
  mounted() {
    this.initVideo()
  },
  methods: {
    initVideo() {
      try {
        // 连接前先断开
        if (this.webRtcServer) {
          this.webRtcServer.disconnect()
        }
        // 连接后端的IP地址和端口
        this.webRtcServer = new WebRtcStreamer(
          this.$refs.video,
          `http://192.168.110.137:8000` // 请确保此地址为你的 WebRTC-streamer 服务地址
        )
        console.log('rtsp地址', this.rtsp)
        // 向后端发送rtsp地址，强制使用TCP拉流，兼容性更好
        this.webRtcServer.connect(this.rtsp, null, 'rtsp_transport=tcp')
      } catch (error) {
        // console.log(error)
      }
    },
    /* 处理双击 单机 */
    dbClick() {
      this.clickCount++
      if (this.clickCount === 2) {
        this.btnFull() // 双击全屏
        this.clickCount = 0
      }
      setTimeout(() => {
        if (this.clickCount === 1) {
          this.clickCount = 0
        }
      }, 250)
    },
    /* 视频全屏 */
    btnFull() {
      const elVideo = this.$refs.video
      if (elVideo.webkitRequestFullScreen) {
        elVideo.webkitRequestFullScreen()
      } else if (elVideo.mozRequestFullScreen) {
        elVideo.mozRequestFullScreen()
      } else if (elVideo.requestFullscreen) {
        elVideo.requestFullscreen()
      }
    },
    /* 
    ison用来判断是否需要更换视频流
    dbclick函数用来双击放大全屏方法
    */
    handleClickVideo() {
      if (this.isOn) {
        this.$emit('selectVideo', this.spareId)
        this.dbClick()
      } else {
        this.btnFull()
      }
    },
  },
}
</script>
 
<style scoped lang="scss">
.active-video-border {
  border: 2px solid #409eff;
  box-shadow: 0 0 10px rgba(64, 158, 255, 0.5);
}
#video-contianer {
  position: relative;
  width: 100%;
  height: 100%;
  .video {
    width: 100%;
    height: 100%;
    object-fit: cover;
    background: #000;
  }
  .mask {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    cursor: pointer;
  }
}
</style>