<template>
  <div id="index" ref="appRef">
    <div class="bg">
      <dv-loading v-if="loading">{{ LOADING_TEXT }}</dv-loading>
      <div v-else class="host-body">
        <!-- 标题栏 -->
        <header class="d-flex jc-center">
          <dv-decoration-10 class="dv-dec-10" />
          <div class="d-flex jc-center">
            <dv-decoration-8 class="dv-dec-8" :color="DECORATION_COLOR" />
            <div class="title">
              <span class="title-text">{{ SCREEN_TITLE }}</span>
              <dv-decoration-6
                class="dv-dec-6"
                :reverse="true"
                :color="TITLE_DECORATION_COLOR"
              />
            </div>
            <dv-decoration-8
              class="dv-dec-8"
              :reverse="true"
              :color="DECORATION_COLOR"
            />
          </div>
          <dv-decoration-10 class="dv-dec-10-s" />
        </header>
        
        <!-- 时间信息栏 -->
        <nav class="d-flex jc-between px-2">
          <div class="d-flex aside-width">
            <div class="react-left ml-4 react-l-s">
              <span class="react-left"></span>
              <span class="text"></span>
            </div>
            <div class="react-left ml-3 bg-blue">
              <span class="text"></span>
            </div>
          </div>
          <div class="d-flex aside-width">
            <div class="react-right bg-color-blue mr-3">
              <span class="text fw-b"></span>
            </div>
            <div class="react-right mr-4 react-l-s">
              <span class="react-after"></span>
              <span class="text">{{ currentDateTime }}</span>
              <button class="back-btn" @click="goToBigScreen">返回大屏</button>
            </div>
          </div>
        </nav>

        <!-- 主体内容区域 -->
        <main class="body-box">
          <!-- 左侧列 -->
          <section class="column">
            <dv-border-box-13>
              <left-top />
            </dv-border-box-13>
            <dv-border-box-13>
              <left-center />
            </dv-border-box-13>
            <dv-border-box-13>
              <left-bottom />
            </dv-border-box-13>
          </section>
          
          <!-- 中间列 -->
          <section class="column">
            <dv-border-box-12>
              <center-top />
            </dv-border-box-12>
            <dv-border-box-12>
              <center-bottom />
            </dv-border-box-12>
          </section>
          
          <!-- 右侧列 -->
          <section class="column">
            <dv-border-box-13>
              <right-top />
            </dv-border-box-13>
            <dv-border-box-13>
              <right-center />
            </dv-border-box-13>
            <dv-border-box-13>
              <right-bottom />
            </dv-border-box-13>
          </section>
        </main>
      </div>
    </div>
  </div>
</template>

<script>
import drawMixin from "../utils/drawMixin";
import { formatTime } from "../utils/index.js";
import { mapActions } from "vuex";

// 导入子组件
import CenterTop from "./newPart/center/centerTop";
import LeftTop from "./newPart/left/leftTop";
import LeftCenter from "./newPart/left/leftCenter";
import LeftBottom from "./newPart/left/leftBottom1";
import CenterBottom from './newPart/center/centerBottom';
import RightTop from './newPart/right/rightTop';
import RightCenter from './newPart/right/rightCenter';
import RightBottom from './newPart/right/rightBottom';

// 常量定义
const WEEKDAYS = ["周日", "周一", "周二", "周三", "周四", "周五", "周六"];
const DECORATION_COLOR = ["#568aea", "#000000"];
const TITLE_DECORATION_COLOR = ["#50e3c2", "#67a1e5"];
const LOADING_DELAY = 500;
const TIME_UPDATE_INTERVAL = 1000;

export default {
  name: 'BigScreenIndex',
  
  mixins: [drawMixin],
  
  components: {
    CenterTop,
    LeftTop,
    LeftCenter,
    LeftBottom,
    CenterBottom,
    RightTop,
    RightCenter,
    RightBottom,
  },
  
  data() {
    return {
      timeTimer: null,
      loading: true,
      currentTime: new Date(),
      currentScreenId: null,
      LOADING_TEXT: 'Loading...',
      SCREEN_TITLE: '数智农业驾驶舱',
      DECORATION_COLOR,
      TITLE_DECORATION_COLOR,
    };
  },
  
  computed: {
    currentDateTime() {
      const date = this.currentTime;
      const year = formatTime(date, "yyyy-MM-dd");
      const time = formatTime(date, "HH:mm:ss");
      const weekday = WEEKDAYS[date.getDay()];
      return `${year} ${weekday} ${time}`;
    },
  },
  
  watch: {
    '$route.query': {
      handler(newQuery) {
        const { screenId, deviceId, deviceName, relayProtocolId, configId } = newQuery;
        
        if (screenId && screenId !== this.currentScreenId) {
          this.currentScreenId = screenId;
          this.loadScreenData({ screenId, deviceId, deviceName, relayProtocolId, configId });
        } else if (deviceId && deviceId !== this.$store.getters.getDeviceId) {
          this.loadScreenData({ screenId, deviceId, deviceName, relayProtocolId, configId });
        }
      },
      immediate: true,
      deep: true
    }
  },
  
  mounted() {
    this.initializeScreen();
  },
  
  beforeDestroy() {
    this.cleanup();
  },
  
  methods: {
    // 🔥 修复：只映射存在的 actions
    ...mapActions({
      setSelectedScreen: 'setSelectedScreen',
      setDeviceById: 'setDeviceById'
    }),
    
    goToBigScreen() {
      this.$router.push({ name: 'big-screen' });
    },
    
    loadScreenData({ screenId, deviceId, deviceName, relayProtocolId, configId }) {
      console.log('🎯 加载大屏数据，参数:', { screenId, deviceId, deviceName, relayProtocolId, configId });
      
      if (deviceId || deviceName || relayProtocolId || configId) {
        this.setDeviceById({ 
          deviceName: deviceName || `device_${screenId}`, 
          deviceId: deviceId || screenId,
          relayProtocolId: relayProtocolId || '',
          configId: configId || screenId 
        });
      } else if (screenId) {
        this.setDeviceById({ 
          deviceName: `device_${screenId}`, 
          deviceId: screenId,
          relayProtocolId: '',
          configId: screenId 
        });
      }
      
      this.setSelectedScreen({
        id: screenId || deviceId,
        deviceId: deviceId,
        deviceName: deviceName,
        relayProtocolId: relayProtocolId,
        configId: configId,
        title: '数智农业驾驶舱',
        theme: 'default'
      });
      
      console.log('✅ 大屏数据加载完成，当前deviceId:', this.$store.getters.getDeviceId);
    },
    
    initializeScreen() {
      this.startTimeUpdater();
      this.hideLoadingWithDelay();
    },
    
    startTimeUpdater() {
      this.updateCurrentTime();
      this.timeTimer = setInterval(this.updateCurrentTime, TIME_UPDATE_INTERVAL);
    },
    
    updateCurrentTime() {
      this.currentTime = new Date();
    },
    
    hideLoadingWithDelay() {
      setTimeout(() => {
        this.loading = false;
      }, LOADING_DELAY);
    },
    
    cleanup() {
      if (this.timeTimer) {
        clearInterval(this.timeTimer);
        this.timeTimer = null;
      }
    },
  },
};
</script>

<style lang="scss" scoped>
@import "../assets/scss/index.scss";

.bg-blue {
  background-color: #1a5cd7;
}

.back-btn {
  margin-left: 16px;
  margin-right: 30px;
  padding: 4px 18px;
  font-size: 15px;
  font-weight: 600;
  border: none;
  border-radius: 20px;
  background: linear-gradient(90deg, #162447 0%, #1f4068 100%);
  color: #fff;
  box-shadow: 0 2px 8px rgba(22, 36, 71, 0.15);
  cursor: pointer;
  transition: all 0.2s ease;
  letter-spacing: 1px;
  transform: skewX(45deg);
  
  &:hover {
    background: linear-gradient(90deg, #1f4068 0%, #162447 100%);
    box-shadow: 0 4px 16px rgba(22, 36, 71, 0.22);
    transform: translateY(-2px);
  }
  
  &:active {
    transform: scale(0.98);
  }
}

@media (max-width: 768px) {
  .body-box {
    flex-direction: column;
  }
  
  .column {
    width: 100%;
    margin-bottom: 1rem;
  }
}
</style>