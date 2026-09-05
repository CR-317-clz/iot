<template>
  <div class="device-data-detail">
    <el-card class="page-card">
      <!-- 页面头部 -->
      <div slot="header" class="page-header">
        <div class="header-left">
          <el-button type="primary" icon="el-icon-arrow-left" @click="goBack" class="back-btn">
            返回
          </el-button>
          <span class="page-title">
            <i class="el-icon-monitor"></i>
            {{ areaInfo.deviceName || areaInfo.configName || '设备详情' }} - 区域详情
          </span>
        </div> 
      </div>

      <div class="content">
        <!-- 区域基本信息和摄像头监控 -->
        <el-row :gutter="20">
          <el-col :span="12">
            <!-- 区域信息卡片 -->
            <el-card class="info-card">
              <div class="area-info">
                <div class="area-icon">
                  <i class="el-icon-monitor"></i>
                  <div class="icon-badge" :class="areaInfo.status === '1' ? 'online' : 'offline'">
                    <i :class="areaInfo.status === '1' ? 'el-icon-check' : 'el-icon-close'"></i>
                  </div>
                </div>
                <div class="area-details">
                  <div class="area-title">
                    <h3>{{ areaInfo.deviceName || areaInfo.configName || '未知设备' }}</h3>
                    <el-tag 
                      :type="areaInfo.status === '1' ? 'success' : 'danger'" 
                      size="small"
                    >
                      {{ areaInfo.status === '1' ? '在线' : '离线' }}
                    </el-tag>
                  </div>

                  <div class="area-meta">
                    <div class="meta-row">
                      <div class="meta-item">
                        <i class="el-icon-document"></i>
                        <span class="meta-label">设备编号</span>
                        <span class="meta-value">{{ displayDeviceCode }}</span>
                      </div>
                      <div class="meta-item">
                        <i class="el-icon-collection-tag"></i>
                        <span class="meta-label">设备类型</span>
                        <span class="meta-value">{{ displayDeviceType }}</span>
                      </div>
                    </div>
                    <div class="meta-row">
                      <div class="meta-item">
                        <i class="el-icon-position"></i>
                        <span class="meta-label">设备位置</span>
                        <span class="meta-value">{{ displayLocation }}</span>
                      </div>
                      <div class="meta-item">
                        <i class="el-icon-cpu"></i>
                        <span class="meta-label">设备数量</span>
                        <span class="meta-value highlight">{{ deviceStats.total }}台</span>
                      </div>
                    </div>
                    <div class="meta-row">
                      <div class="meta-item">
                        <i class="el-icon-connection"></i>
                        <span class="meta-label">MQTT协议</span>
                        <span class="meta-value">{{ displayProtocolId }}</span>
                      </div>
                      <div class="meta-item">
                        <i class="el-icon-set-up"></i>
                        <span class="meta-label">TCP协议</span>
                        <span class="meta-value">{{ displayRelayProtocolId }}</span>
                      </div>
                    </div>
                    <div class="meta-row">
                      <div class="meta-item full-width">
                        <i class="el-icon-time"></i>
                        <span class="meta-label">最后更新</span>
                        <span class="meta-value">{{ lastUpdateTime || formatTime(areaInfo.updateTime) || '暂无数据' }}</span>
                      </div>
                    </div>
                  </div>

                  <div class="area-stats">
                    <div class="stats-item">
                      <div class="stats-number">{{ deviceStats.online }}</div>
                      <div class="stats-label">正常设备</div>
                    </div>
                    <div class="stats-item">
                      <div class="stats-number warning">{{ deviceStats.warning }}</div>
                      <div class="stats-label">警告设备</div>
                    </div>
                    <div class="stats-item">
                      <div class="stats-number danger">{{ deviceStats.offline }}</div>
                      <div class="stats-label">离线设备</div>
                    </div>
                  </div>
                </div>
              </div>
            </el-card>

            <!-- 趋势图卡片 -->
            <el-card class="chart-card mt-20">
              <div slot="header" class="trend-header">
                <span><i class="el-icon-data-line"></i> 设备状态趋势</span>
                <el-select
                  v-model="selectedSensor"
                  placeholder="选择传感器"
                  size="mini"
                  style="width: 180px"
                  @change="handleSensorChange"
                >
                  <el-option
                    v-for="sensor in sensorList"
                    :key="sensor"
                    :label="sensor"
                    :value="sensor"
                  />
                </el-select>
              </div>
              <div class="chart-container">
                <div v-if="trendLoading" class="chart-placeholder">
                  <i class="el-icon-loading"></i>
                  <p>加载中...</p>
                </div>
                <div v-else-if="trendData.length" style="width: 100%; height: 260px">
                  <trend-chart :data="trendData" />
                </div>
                <div v-else class="chart-placeholder">
                  <i class="el-icon-data-line"></i>
                  <p>暂无数据展示</p>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <!-- 摄像头监控卡片 -->
            <el-card class="camera-card">
              <div slot="header" class="camera-header">
                <span><i class="el-icon-video-camera"></i> 摄像头监控</span>
                <div class="camera-controls">
                  <el-button 
                    type="text" 
                    icon="el-icon-refresh" 
                    @click="reconnectVideo"
                    :disabled="!cameraInfo.streamUrl"
                    title="重新连接"
                  />
                  <el-button 
                    type="text" 
                    icon="el-icon-full-screen" 
                    @click="handleFullscreenCamera"
                    :disabled="!cameraInfo.streamUrl"
                    title="全屏"
                  />
                </div>
              </div>
              
              <div class="camera-content">
                <!-- 视频播放区域 -->
                <div class="video-player-section">
                  <div class="video-info-bar">
                    <div class="info-item">
                      <span class="label">位置：</span>
                      <span class="value">{{ cameraInfo.location || '-' }}</span>
                    </div>
                    <div class="info-item">
                      <span class="label">IP地址：</span>
                      <span class="value">{{ cameraInfo.ipAddress || '-' }}</span>
                    </div>
                    <div class="info-item">
                      <span class="label">状态：</span>
                      <el-tag 
                        :type="getCameraStatusType(cameraStatus)" 
                        size="mini"
                      >
                        {{ getCameraStatusText(cameraStatus) }}
                      </el-tag>
                    </div>
                    <div class="info-item webrtc-server-config">
                      <span class="label">WebRTC服务器：</span>
                      <el-input 
                        v-model="webRtcServerUrl" 
                        size="mini" 
                        style="width: 200px;"
                        placeholder="http://192.168.110.137:8000"
                      ></el-input>
                      <el-button 
                        type="primary" 
                        size="mini" 
                        @click="reconnectVideo"
                        icon="el-icon-refresh"
                        :disabled="!cameraInfo.streamUrl"
                      >
                        重新连接
                      </el-button>
                    </div>
                  </div>
                  
                  <div class="video-player-wrapper">
                    <div v-if="cameraStatus === 'online'" class="video-player">
                      <video 
                        v-if="cameraInfo.streamUrl"
                        ref="videoPlayer"
                        class="video-element"
                        preload="auto"
                        autoplay
                        muted
                        playsinline
                        controls
                        @dblclick="handleVideoDoubleClick"
                      >
                        您的浏览器不支持 video 标签。
                      </video>
                      <div v-else class="video-placeholder">
                        <i class="el-icon-video-camera-solid"></i>
                        <p>该摄像头暂无视频流地址</p>
                        <p class="hint-text">请配置 RTSP 地址后查看</p>
                      </div>
                    </div>
                    <div v-else class="video-offline">
                      <i class="el-icon-warning-outline"></i>
                      <p>摄像头离线</p>
                      <p class="hint-text">无法获取视频流</p>
                    </div>
                  </div>
                </div>
                
                <!-- 摄像头详细信息 -->
                <div class="camera-details">
                  <el-descriptions :column="2" size="small" border>
                    <el-descriptions-item label="摄像头名称">
                      {{ cameraInfo.cameraName || '-' }}
                    </el-descriptions-item>
                    <el-descriptions-item label="摄像头类型">
                      {{ cameraInfo.cameraType || '-' }}
                    </el-descriptions-item>
                    <el-descriptions-item label="厂商">
                      {{ cameraInfo.manufacturer || '-' }}
                    </el-descriptions-item>
                    <el-descriptions-item label="型号">
                      {{ cameraInfo.model || '-' }}
                    </el-descriptions-item>
                    <el-descriptions-item label="RTSP地址" :span="2">
                      <span class="rtsp-url">{{ cameraInfo.streamUrl || '-' }}</span>
                    </el-descriptions-item>
                  </el-descriptions>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 控制模式切换 -->
        <div class="mode-switch mt-20">
          <div class="mode-left">
            <span>控制模式：</span>
            <el-radio-group v-model="areaMode" size="small" @change="handleModeSwitch">
              <el-radio-button label="manual">手动控制</el-radio-button>
              <el-radio-button label="auto">自动控制</el-radio-button>
            </el-radio-group>
            <el-tag :type="currentModeConfig.type" size="mini" style="margin-left:12px">
              {{ currentModeConfig.label }}
            </el-tag>
          </div>
        </div>

        <!-- 设备控制面板 -->
        <el-row :gutter="16" class="mt-16">
          <el-col v-for="(item, index) in areaControls" :key="`${item.relayDeviceName}-${item.relayId || index}`" :span="12">
            <el-card class="chart-card control-card mb-16">
              <div slot="header">
                <span><i class="el-icon-setting"></i> {{ getControlDisplayName(item.relayDeviceName) }}控制</span>
              </div>
              <div class="control-panel">
                <i :class="getDeviceIconClass(item.relayDeviceName, item.status)"></i>
                
                <!-- 窗帘开关和遮阳帘专用三态控制 -->
                <div v-if="item.relayDeviceName === '窗帘开关' || item.relayDeviceName === '遮阳帘' || item.relayDeviceName === '窗帘机'" class="curtain-control">
                  <el-radio-group 
                    v-model="item.status" 
                    size="small"
                    :disabled="areaMode === 'auto'"
                    @change="(val) => handleCurtainChange(item, val)"
                  >
                    <el-radio-button :label="0">关闭</el-radio-button>
                    <el-radio-button :label="1">开启</el-radio-button>
                    <el-radio-button :label="2">暂停</el-radio-button>
                  </el-radio-group>
                  <div class="curtain-status">
                    <span :class="getCurtainStatusClass(item.status)">
                      {{ getCurtainStatusText(item.status) }}
                    </span>
                  </div>
                </div>
                
                <!-- 其他设备开关控制 -->
                <el-switch
                  v-else
                  v-model="item.status"
                  :active-value="1"
                  :inactive-value="0"
                  :disabled="areaMode === 'auto'"
                  active-text="开启"
                  inactive-text="关闭"
                  @change="(val) => handleRelayChange(item, val)"
                ></el-switch>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 设备列表 -->
        <el-row :gutter="20">
          <el-col :span="24">
            <el-card>
              <div slot="header">
                <span><i class="el-icon-data-analysis"></i> 数字农场环境数据 (共{{ deviceStats.total }}台设备)</span>
                <el-button type="primary" size="small" style="margin-left: 15px" @click="refreshDevices">
                  <i class="el-icon-refresh"></i> 刷新设备
                </el-button>
                <el-button
                  type="success"
                  size="small"
                  style="float: right; margin-right: 12px"
                  @click="handleConfirmSetValues"
                >
                  <i class="el-icon-check"></i> 确认修改
                </el-button>
              </div>
              <el-table :data="areaDevices" style="width: 100%" :header-cell-style="{background: '#f5f7fa'}">
                <el-table-column prop="deviceName" label="设备名称" />
                <el-table-column prop="status" label="运行状态">
                  <template slot-scope="scope">
                    <el-tag
                      :type="scope.row.status === '1' ? 'success' : scope.row.status === '离线' ? 'danger' : 'warning'"
                      size="small"
                    >
                      {{ scope.row.status === "1" ? "在线" : "离线" }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="curValue" label="当前数值" />
                <el-table-column prop="setValue" label="设置数值">
                  <template slot-scope="scope">
                    <el-input
                      v-model="scope.row.setValue"
                      style="width: 220px"
                      placeholder="请输入需要设置的值"
                      :disabled="areaMode === 'auto'"
                    />
                    <span style="margin-left: 8px; color: #909399; font-size: 14px">
                      {{ scope.row.unit || "" }}
                    </span>
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </el-card>
    
    <!-- 自动模式设置弹窗 -->
    <el-dialog
      title="自动控制模式设置"
      :visible.sync="showAutoDialog"
      width="680px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      custom-class="auto-mode-dialog-new"
      @close="handleAutoDialogCancel"
    >
      <div class="dialog-content">
        <!-- 提示信息 -->
        <div class="dialog-tip">
          <div class="tip-icon">
            <i class="el-icon-setting"></i>
          </div>
          <div class="tip-text">
            <div class="tip-title">智能自动控制</div>
            <div class="tip-desc">设置设备阈值后，系统将根据传感器数据自动调节设备运行状态</div>
          </div>
        </div>
        
        <!-- 表单 -->
        <el-form label-width="130px" label-position="left" class="auto-form">
          <el-form-item label="温度阈值">
            <el-input
              v-model="autoModeSettings.setTemperature"
              placeholder="例如：25"
              clearable
              @input="val => handleAutoModeInput('setTemperature', val)"
            >
              <template slot="prepend">
                <i class="el-icon-sunny" style="color: #ff6b6b;"></i>
              </template>
              <template slot="append">°C</template>
            </el-input>
          </el-form-item>
          
          <el-form-item label="湿度阈值">
            <el-input
              v-model="autoModeSettings.setHumidity"
              placeholder="例如：60"
              clearable
              @input="val => handleAutoModeInput('setHumidity', val)"
            >
              <template slot="prepend">
                <i class="el-icon-partly-cloudy" style="color: #4ecdc4;"></i>
              </template>
              <template slot="append">%</template>
            </el-input>
          </el-form-item>
          
          <el-form-item label="光照度阈值">
            <el-input
              v-model="autoModeSettings.setIlluminance"
              placeholder="例如：500"
              clearable
              @input="val => handleAutoModeInput('setIlluminance', val)"
            >
              <template slot="prepend">
                <i class="el-icon-light" style="color: #ffe66d;"></i>
              </template>
              <template slot="append">lux</template>
            </el-input>
          </el-form-item>
          
          <el-form-item label="土壤湿度阈值">
            <el-input
              v-model="autoModeSettings.setSoilMoisture"
              placeholder="例如：40"
              clearable
              @input="val => handleAutoModeInput('setSoilMoisture', val)"
            >
              <template slot="prepend">
                <i class="el-icon-drizzle" style="color: #95e1d3;"></i>
              </template>
              <template slot="append">%</template>
            </el-input>
          </el-form-item>
          
          <el-form-item label="一氧化碳阈值">
            <el-input
              v-model="autoModeSettings.setCarbonMonoxide"
              placeholder="例如：50"
              clearable
              @input="val => handleAutoModeInput('setCarbonMonoxide', val)"
            >
              <template slot="prepend">
                <i class="el-icon-warning-outline" style="color: #ff9ff3;"></i>
              </template>
              <template slot="append">ppm</template>
            </el-input>
          </el-form-item>
          
          <el-form-item label="二氧化碳阈值">
            <el-input
              v-model="autoModeSettings.setCarbonDioxide"
              placeholder="例如：800"
              clearable
              @input="val => handleAutoModeInput('setCarbonDioxide', val)"
            >
              <template slot="prepend">
                <i class="el-icon-warning" style="color: #feca57;"></i>
              </template>
              <template slot="append">ppm</template>
            </el-input>
          </el-form-item>
          
          <el-form-item label="紫外线阈值">
            <el-input
              v-model="autoModeSettings.setUltravioletRay"
              placeholder="例如：10"
              clearable
              @input="val => handleAutoModeInput('setUltravioletRay', val)"
            >
              <template slot="prepend">
                <i class="el-icon-sunrise" style="color: #ff6348;"></i>
              </template>
              <template slot="append">μW/cm²</template>
            </el-input>
          </el-form-item>
        </el-form>
      </div>
      
      <span slot="footer" class="dialog-footer">
        <el-button @click="handleAutoDialogCancel" size="medium">取消</el-button>
        <el-button type="primary" @click="handleAutoDialogConfirm" size="medium">确认切换</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
/* global WebRtcStreamer */
// ↑ 告诉 ESLint WebRtcStreamer 是全局变量（通过 public/webrtcstreamer.js 加载）

import {
  // getDeviceTrendApi, // 已注释：趋势数据接口暂停使用
  controlTcpApi,
} from "@/api/device";
// import { videoMonitoringApi } from "@/api/scream.js"; // 已注释：摄像头接口暂停使用
import TrendChart from "@/components/echart/TrendChart.vue";
// import videoPart from "../../components/echart/videoPart.vue"; // 已移除：未使用的组件
import { getIotTelemetryByDeviceId, getStatisticsTelemetry } from "@/api/iot-telemetry.js";
import { getRelayDeviceStatusByProtocol } from "@/api/iot-protocol.js";
import { getDeviceById } from "@/api/iot-device.js";
// import { getAllCameraList } from "@/api/iot-camera.js"; // 已废弃：摄像头数据现在从设备详情接口的 cameraInfo 字段获取
import { mapGetters } from "vuex";

// 常量配置
const MODE_CONFIG = {
  manual: { label: "手动模式", type: "success" },
  auto: { label: "自动模式", type: "info" }
};

// 已注释：趋势数据接口暂停使用
// const SENSOR_TYPE_MAP = {
//   土壤湿度传感器: 1,
//   土壤温度传感器: 2,
//   土壤导电传感器: 3,
//   紫外线传感器: 4,
//   二氧化碳传感器: 5,
//   光照传感器: 6,
//   空气湿度传感: 7,
//   温度传感器: 8,
//   一氧化碳传感器: 9,
//   PH传感器: 10,
// };

const DEVICE_ICON_MAP = {
  窗帘开关: "icon-chuanglian",
  遮阳帘: "icon-chuanglian",
  窗帘机: "icon-chuanglian", // 新增：窗帘机
  淋头: "icon-fangwupentou",
  外喷淋: "icon-youyanji",
  内喷淋: "icon-shower",
  喷淋: "icon-shower", // 新增：喷淋
  水帘: "icon-shuilian",
  风扇: "icon-paifeng-",
  紫外线灯: "icon-ziwaixiandeng",
  灯: "icon-ziwaixiandeng", // 新增：灯
  一号位: "icon-shower",
  四号位: "icon-ziwaixiandeng",
  滴灌: "icon-diguan",
  加热灯: "icon-tubiao-ditu-huozaibaojingqi",
  default: "icon-ziyuanxhdpi"
};

// 设备控制器映射配置 - 映射到新API参数名称
// 根据接口要求，使用具体的设备字段名
const DEVICE_CONTROL_PARAMS = {
  一号位: "spray",               // 喷淋（物理通道 a1，0-关 1-开）
  二号位: "blower",              // 风扇（物理通道 a2，0-关 1-开）
  三号位: "waterCurtain",        // 水帘幕（物理通道 a4，0-关 1-开）
  四号位: "ultravioletRadiator", // 紫外线灯/灯（物理通道 a3，0-关 1-开）
  淋头: "spray",                // 淋头（与喷淋相同）
  内喷淋: "spray",              // 内喷淋
  外喷淋: "spray",              // 外喷淋
  水帘: "waterCurtain",         // 水帘幕（0-关 1-开）
  窗帘机: "curtainMachine",     // 窗帘机器（0-关 1-开 2-暂停）
  窗帘开关: "curtainMachine",   // 窗帘开关（兼容）
  遮阳帘: "curtainMachine",     // 遮阳帘（兼容）
};

const CONTROL_DISPLAY_NAMES = {
  一号位: "喷淋",
  四号位: "灯光"
};

// 传感器设备映射 - 用于传感器控制和设备自动化控制
const SENSOR_DEVICE_MAP = {
  土壤湿度传感器: "setMoisture",
  土壤导电传感器: "setEc", 
  PH传感器: "setPh",
  土壤温度传感器: "setSoilTemperature",
  光照传感器: "setIllumination",
  空气湿度传感: "setHumidity",
  一氧化碳传感器: "setCo",
  二氧化碳传感器: "setCarbonDipxode",
  紫外线传感器: "setultravioletLight",
  温度传感器: "setTemperature"
};

export default {
  name: "AreaDataDetail",
  components: { 
    TrendChart
    // videoPart 已移除：未使用的组件
  },
  
  computed: {
    ...mapGetters({ configId: 'getConfigId' }),
    
    deviceStats() {
      const total = this.areaDevices.length;
      const online = this.areaDevices.filter(d => d.status === "1").length;
      const warning = this.areaDevices.filter(d => d.status === "警告").length;
      const offline = this.areaDevices.filter(d => d.status === "离线").length;
      return { total, online, warning, offline };
    },
    
    currentModeConfig() {
      return MODE_CONFIG[this.areaMode] || MODE_CONFIG.manual;
    },
    
    // 确保设备信息的响应性
    displayDeviceCode() {
      return this.areaInfo.deviceCode || '-';
    },
    
    displayDeviceType() {
      return this.areaInfo.deviceType || this.areaInfo.hardwareType || '-';
    },
    
    displayLocation() {
      return this.areaInfo.location || '-';
    },
    
    displayHost() {
      return this.areaInfo.host || '-';
    },
    
    displayTopic() {
      return this.areaInfo.topic || '-';
    },
    
    displayProtocolId() {
      return this.areaInfo.protocolId || '-';
    },
    
    displayRelayProtocolId() {
      return this.areaInfo.relayProtocolId || '-';
    }
  },
  
  data() {
    return {
      areaInfo: {
        id: "",
        deviceId: "",
        deviceName: "区域加载中...",
        configName: "",
        deviceCode: "",
        status: "1",
        deviceType: "",
        hardwareType: "",
        topic: "",
        host: "",
        location: "",
        protocolId: "",
        relayProtocolId: "",
        createTime: null,
        updateTime: null
      },
      lastUpdateTime: "",
      areaDevices: [],
      areaControls: [],
      areaMode: "manual",
      automation: "1",
      deviceId: null,
      autoModeSettings: {
        setTemperature: '',
        setHumidity: '',
        setIlluminance: '',
        setSoilMoisture: '',
        setCarbonMonoxide: '',
        setCarbonDioxide: '',
        setUltravioletRay: ''
      },
      showAutoDialog: false,
      autoDialogResolve: null,
      autoDialogReject: null,
      
      // 摄像头相关
      cameraUrl: "",
      cameraLoading: false,
      cameraRefreshing: false,
      cameraSelected: false,
      cameraPreviewUrl: "",
      cameraStatus: "offline",
      cameraInfo: {
        cameraName: "",
        location: "",
        ipAddress: "",
        cameraType: "",
        manufacturer: "",
        model: "",
        streamUrl: ""
      },
      webRtcServerUrl: 'http://192.168.110.137:8000',
      webRtcServer: null,
      selectedSensor: "",
      sensorList: [],
      telemetryList: [], // 存储从接口获取的遥测数据项（包含dataKey和dataKeyChinese）
      trendData: [],
      trendLoading: false,
      historyRecords: [],
      updateTimer: null
    };
  },
  
  watch: {
    '$route.query': {
      handler(newQuery, oldQuery) {
        // 当路由参数变化时，重新加载设备数据
        const newDeviceId = newQuery.deviceId || newQuery.configId;
        const oldDeviceId = oldQuery?.deviceId || oldQuery?.configId;
        
        if (newDeviceId && newDeviceId !== oldDeviceId) {
          console.log('路由参数变化，重新加载设备数据:', newDeviceId);
          this.deviceId = newDeviceId;
          
          if (newQuery.configId) {
            this.$store.commit('SET_CONFIG_ID', newQuery.configId);
          }
          
          this.loadAreaData();
        }
      },
      deep: true
    },
    
    configId: {
      handler(newId) {
        // configId 变化时，摄像头数据会在 loadAreaData 中自动更新
        console.log('configId 已变化:', newId);
      },
      immediate: false,
    }
  },
  
  mounted() {
    console.log('=== 设备详情页 mounted 开始 ===');
    console.log('路由 query:', this.$route.query);
    console.log('路由 params:', this.$route.params);
    
    // 从路由参数获取 deviceId
    const deviceId = this.$route.query.deviceId || this.$route.query.configId || this.$route.params.deviceId;
    
    if (deviceId) {
      this.deviceId = deviceId;
      console.log('✅ 获取到 deviceId:', this.deviceId);
    } else {
      console.error('❌ 未获取到 deviceId，无法加载设备详情');
      this.$message.error('缺少设备ID参数');
    }
    
    // 如果有 configId，设置到 store
    if (this.$route.query.configId) {
      this.$store.commit('SET_CONFIG_ID', this.$route.query.configId);
    }
    
    console.log('=== 设备详情页 mounted 结束 ===');
    
    this.initializeComponent();
  },
  
  beforeDestroy() {
    this.stopDataUpdate();
  },
  
  methods: {
    async initializeComponent() {
      await this.loadAreaData();
      this.startDataUpdate();
      
      // 等待摄像头数据加载完成后（已在 loadAreaData 中获取），如果有视频流则自动初始化
      this.$nextTick(() => {
        if (this.cameraInfo.streamUrl && this.cameraStatus === 'online') {
          this.initVideo();
        }
      });
      
      this.addHistoryRecord("进入设备详情页面");
    },
    
    async apiCall(apiFunc, ...args) {
      try {
        return await apiFunc(...args);
      } catch (error) {
        console.error('API调用失败:', error);
        throw error;
      }
    },
    
    detectDeviceConfig() {
      // 已不再需要检测设备配置，因为使用统一的参数映射
      console.log('当前设备列表:', this.areaControls.map(c => c.relayDeviceName));
    },
    
    getCurrentDeviceMap() {
      // 返回新的设备控制参数映射
      return DEVICE_CONTROL_PARAMS;
    },
    
    getTokenAndValidate() {
      const token = localStorage.getItem("token");
      if (!token) throw new Error('未登录或登录已过期');
      if (!this.areaInfo.topic) throw new Error('设备主题信息缺失');
      return token;
    },
    
    /**
     * 获取摄像头数据
     */
    // async fetchCameraData() {
    //   const deviceId = this.deviceId || this.$route.query.deviceId;
    //   
    //   if (!deviceId) {
    //     console.warn('fetchCameraData: deviceId 为空，跳过摄像头数据获取');
    //     return;
    //   }
    //   
    //   this.cameraLoading = true;
    //   console.log('开始获取摄像头数据，deviceId:', deviceId);
    //   
    //   try {
    //     const response = await this.apiCall(getAllCameraList);
    //     console.log('摄像头数据响应:', response);
    //     
    //     if (response?.data?.code === 200 && response.data.rows) {
    //       const cameras = response.data.rows || [];
    //       
    //       // 根据设备ID或位置信息匹配摄像头
    //       // 这里假设摄像头的location字段包含设备位置信息
    //       const matchedCamera = cameras.find(camera => 
    //         camera.location && this.areaInfo.location && 
    //         camera.location.includes(this.areaInfo.location)
    //       ) || cameras[0]; // 如果没有匹配到，使用第一个摄像头
    //       
    //       if (matchedCamera) {
    //         this.cameraInfo = {
    //           cameraName: matchedCamera.cameraName || '-',
    //           location: matchedCamera.location || '-',
    //           ipAddress: matchedCamera.ipAddress || '-',
    //           cameraType: matchedCamera.cameraType || '-',
    //           manufacturer: matchedCamera.manufacturer || '-',
    //           model: matchedCamera.model || '-',
    //           streamUrl: matchedCamera.streamUrl || ''
    //         };
    //         
    //         this.cameraStatus = matchedCamera.status || 'offline';
    //         this.cameraPreviewUrl = matchedCamera.previewUrl || '';
    //         
    //         console.log('✅ 摄像头信息获取成功:', this.cameraInfo);
    //       } else {
    //         console.warn('⚠️ 未找到匹配的摄像头');
    //         this.cameraStatus = 'offline';
    //       }
    //     } else {
    //       console.warn('摄像头数据获取失败:', response);
    //       this.cameraStatus = 'offline';
    //     }
    //   } catch (error) {
    //     console.error('获取摄像头数据异常:', error);
    //     this.cameraStatus = 'offline';
    //   } finally {
    //     this.cameraLoading = false;
    //   }
    // },
    
    // ============ 摄像头相关方法 ============
    
    /**
     * 初始化视频（WebRTC）
     */
    initVideo() {
      try {
        // 确保 WebRtcStreamer 已加载
        if (typeof WebRtcStreamer === 'undefined') {
          console.error('WebRtcStreamer 未加载');
          this.$message.error('视频播放器未加载，请刷新页面重试');
          return;
        }
        
        // 连接前先断开
        this.disconnectWebRTC();
        
        // 连接 WebRTC-streamer 服务
        this.webRtcServer = new WebRtcStreamer(
          this.$refs.videoPlayer,
          this.webRtcServerUrl
        );
        
        console.log('RTSP地址:', this.cameraInfo.streamUrl);
        
        // 向后端发送rtsp地址，强制使用TCP拉流
        this.webRtcServer.connect(
          this.cameraInfo.streamUrl,
          null,
          'setup=tcp&timeout=60'
        );
        
        this.$message.success('视频连接成功');
      } catch (error) {
        console.error('初始化视频失败:', error);
        this.$message.error('视频连接失败: ' + error.message);
      }
    },
    
    /**
     * 断开 WebRTC 连接
     */
    disconnectWebRTC() {
      if (this.webRtcServer) {
        try {
          this.webRtcServer.disconnect();
        } catch (error) {
          console.error('断开 WebRTC 连接失败:', error);
        }
        this.webRtcServer = null;
      }
    },
    
    /**
     * 重新连接视频
     */
    reconnectVideo() {
      if (!this.cameraInfo.streamUrl) {
        this.$message.warning('该摄像头未配置 RTSP 地址');
        return;
      }
      
      this.$message.info('正在重新连接...');
      this.initVideo();
    },
    
    /**
     * 处理视频双击事件（全屏）
     */
    handleVideoDoubleClick() {
      const video = this.$refs.videoPlayer;
      if (!video) return;
      
      if (video.requestFullscreen) {
        video.requestFullscreen();
      } else if (video.webkitRequestFullscreen) {
        video.webkitRequestFullscreen();
      } else if (video.mozRequestFullScreen) {
        video.mozRequestFullScreen();
      } else if (video.msRequestFullscreen) {
        video.msRequestFullscreen();
      }
    },
    
    /**
     * 获取摄像头状态文本
     */
    getCameraStatusText(status) {
      const statusMap = {
        online: '在线',
        offline: '离线',
        maintenance: '维护中'
      };
      return statusMap[status] || '未知';
    },
    
    /**
     * 获取摄像头状态类型
     */
    getCameraStatusType(status) {
      const typeMap = {
        online: 'success',
        offline: 'danger',
        maintenance: 'warning'
      };
      return typeMap[status] || 'info';
    },
    
    handleSelectVideo(id) {
      this.$emit("videoSelected", { id, configId: this.configId });
      this.cameraSelected = true;
    },

    handleRefreshCamera() {
      this.reconnectVideo();
    },

    handleFullscreenCamera() {
      this.handleVideoDoubleClick();
    },

    handleRetryCamera() {
      // 重新加载设备数据（包括摄像头信息）
      this.loadAreaData();
    },
    
    async fetchRelayDeviceStatus() {
      // 使用 deviceId 获取继电器状态及控制模式
      // 正确接口为 /iot/relay_device_status/device_protocol/{deviceId}：
      // 返回 relayDeviceStatusList 与 automation（1-手动 2-自动）。
      // 旧接口 /install/sendTcp/{id} 仅返回裸数组、无模式字段，会使前端每 30s 把模式重置为手动。
      const deviceId = this.deviceId;
      
      if (!deviceId) {
        console.warn('fetchRelayDeviceStatus: deviceId 为空，跳过继电器设备状态获取');
        return;
      }
      
      try {
        console.log('开始获取继电器设备状态，deviceId:', deviceId);
        const response = await this.apiCall(getRelayDeviceStatusByProtocol, deviceId);
        
        if (response.data && response.data.code === 200) {
          const data = response.data.data;
          
          // 处理两种可能的数据格式：
          // 1. 直接是数组：[{relayDeviceName, status, ...}]
          // 2. 包含 relayDeviceStatusList 的对象：{relayDeviceStatusList: [...], automation: "1"-手动 "2"-自动}
          let deviceList = [];
          let automationValue = null;
          
          if (Array.isArray(data)) {
            // 格式1：直接是数组
            deviceList = data;
            console.log('✅ 继电器设备状态为数组格式，设备数量:', deviceList.length);
          } else if (data && data.relayDeviceStatusList) {
            // 格式2：包含 relayDeviceStatusList
            deviceList = data.relayDeviceStatusList;
            automationValue = data.automation;
            console.log('✅ 继电器设备状态为对象格式，设备数量:', deviceList.length);
          }
          
          // 映射数据
          if (deviceList && deviceList.length > 0) {
            const hiddenControls = ['二号位', '窗帘机', '窗帘开关', '遮阳帘'];
            const mappedData = deviceList
              .filter(item => !hiddenControls.includes(item.relayDeviceName) && item.relayDeviceName !== '四号位')
              .map((item, index) => ({
                relayDeviceName: item.relayDeviceName === '三号位' ? '四号位' : item.relayDeviceName,
                status: Number(item.status),
                relayId: item.relayId || `auto-${index}`,
                protocolId: item.protocolId,
                createTime: item.createTime,
                _raw: item
              }));
            
            this.areaControls = mappedData;
            console.log('✅ 继电器设备状态映射完成:', this.areaControls);
            this.$forceUpdate();
          } else {
            console.warn('⚠️ 继电器设备列表为空');
            this.areaControls = [];
          }
          
          // 处理控制模式
          if (automationValue !== undefined && automationValue !== null) {
            const automationNum = Number(automationValue);
            this.automation = (automationNum === 1 || automationNum === 2) ? String(automationNum) : "1";
            this.areaMode = automationNum === 2 ? "auto" : "manual";
            console.log('✅ 控制模式设置为:', this.areaMode, '(automation:', this.automation, ')');
          } else {
            // 如果后端未返回automation，默认设置为手动模式
            this.automation = "1";
            this.areaMode = "manual";
            console.log('ℹ️ 未返回 automation，默认使用手动模式');
          }
          
          this.lastUpdateTime = new Date().toLocaleString("zh-CN");
          this.detectDeviceConfig();
        } else {
          console.error('❌ 继电器设备状态接口返回异常:', response);
          this.$message.warning('获取设备控制状态失败');
        }
      } catch (error) {
        console.error('❌ 获取继电器设备状态失败:', error);
        this.$message.error('获取设备控制状态失败: ' + error.message);
      }
    },
    
    clearTimer() {
      if (this.updateTimer) {
        clearInterval(this.updateTimer);
        this.updateTimer = null;
      }
    },
    
    startDataUpdate() {
      this.clearTimer();
      this.updateTimer = setInterval(async () => {
        try {
          // 使用 relayProtocolId 刷新继电器状态
          if (this.areaInfo.relayProtocolId || this.areaInfo.id) {
            await this.fetchRelayDeviceStatus();
          }
          await this.fetchTelemetryData();
        } catch (error) {
          console.error('❌ 定时刷新失败:', error);
        }
      }, 5000);
    },
    
    stopDataUpdate() {
      this.clearTimer();
    },
    
    async loadAreaData() {
      // 从路由获取 deviceId
      const deviceId = this.deviceId || this.$route.query.deviceId || this.$route.query.configId;
      const configId = this.$route.query.configId;
      
      if (configId) {
        this.$store.commit('SET_CONFIG_ID', configId);
      }
      
      if (!deviceId) {
        console.error('❌ 缺少设备ID，无法加载设备数据');
        this.$message.error('缺少设备ID参数');
        return;
      }
      
      try {
        console.log('📡 开始从接口获取设备信息，deviceId:', deviceId);
        
        // 直接调用接口获取设备详情
        const deviceRes = await this.apiCall(getDeviceById, deviceId);
        
        if (deviceRes.data && deviceRes.data.code === 200 && deviceRes.data.data) {
          const device = deviceRes.data.data;
          
          // 更新 areaInfo
          this.areaInfo = {
            ...this.areaInfo,
            id: device.deviceId,
            deviceId: device.deviceId,
            deviceName: device.deviceName,
            configName: device.deviceName,
            deviceCode: device.deviceCode,
            status: device.status === 'online' ? '1' : '0',
            deviceType: device.deviceType,
            hardwareType: device.deviceType,
            host: device.host,
            topic: device.topic || device.deviceId,
            location: device.location,
            protocolId: device.protocolId,
            relayProtocolId: device.relayProtocolId,
            createTime: device.createTime,
            updateTime: device.updateTime
          };
          
          this.deviceId = device.deviceId;
          console.log('✅ 成功从接口获取设备信息:', this.areaInfo);

          // 摄像头信息直接从 device.cameraInfo 获取
          if (device.cameraInfo) {
            this.cameraInfo = {
              cameraName: device.cameraInfo.cameraName || '-',
              location: device.cameraInfo.location || '-',
              ipAddress: device.cameraInfo.ipAddress || '-',
              cameraType: device.cameraInfo.cameraType || '-',
              manufacturer: device.cameraInfo.manufacturer || '-',
              model: device.cameraInfo.model || '-',
              streamUrl: device.cameraInfo.streamUrl || ''
            };
            this.cameraStatus = device.cameraInfo.status || 'offline';
            this.cameraPreviewUrl = device.cameraInfo.previewUrl || '';
            console.log('✅ 摄像头信息已从设备详情接口获取:', this.cameraInfo);
          } else {
            this.cameraInfo = {
              cameraName: '',
              location: '',
              ipAddress: '',
              cameraType: '',
              manufacturer: '',
              model: '',
              streamUrl: ''
            };
            this.cameraStatus = 'offline';
          }
          
          // 获取遥测数据
          await this.fetchTelemetryData();
          
          // 更新传感器列表（会自动加载第一个传感器的趋势数据）
          this.updateSensorList();
          
          // 获取继电器设备状态（使用 relayProtocolId）
          if (this.areaInfo.relayProtocolId || this.areaInfo.id) {
            await this.fetchRelayDeviceStatus();
          }
        } else {
          throw new Error(deviceRes.data?.msg || '设备信息返回格式错误');
        }
      } catch (error) {
        console.error('❌ 加载设备数据失败:', error);
        this.$message.error("设备详情数据获取失败: " + (error.message || "未知错误"));
      }
    },
    
    updateSensorList() {
      // 从telemetryList中提取传感器列表（使用dataKeyChinese作为显示名称）
      this.sensorList = this.telemetryList.map(item => item.dataKeyChinese);
      if (this.sensorList.length > 0) {
        this.selectedSensor = this.sensorList[0];
        // 自动加载第一个传感器的趋势数据
        this.fetchTrendData();
      }
    },
    
    async fetchTrendData() {
      const deviceId = this.deviceId || this.$route.query.deviceId;
      
      if (!deviceId || !this.selectedSensor) {
        console.warn('fetchTrendData: 缺少deviceId或selectedSensor，跳过数据获取');
        this.trendData = [];
        this.trendLoading = false;
        return;
      }

      // 根据selectedSensor（dataKeyChinese）找到对应的dataKey
      const telemetryItem = this.telemetryList.find(item => item.dataKeyChinese === this.selectedSensor);
      
      if (!telemetryItem || !telemetryItem.dataKey) {
        console.warn('fetchTrendData: 未找到对应的dataKey，跳过数据获取');
        this.trendData = [];
        this.trendLoading = false;
        return;
      }

      this.trendLoading = true;
      try {
        const response = await this.apiCall(
          getStatisticsTelemetry, 
          deviceId, 
          telemetryItem.dataKey
        );
        
        if (response.data && response.data.code === 200) {
          // 接口返回格式：{ total, rows: [...], code, msg }
          const statisticsData = response.data.rows || [];
          
          console.log(`✅ 获取到 ${statisticsData.length} 条趋势数据:`, statisticsData);
          
          // 将接口返回的数据转换为图表需要的格式
          // 接口返回字段：timeFormat (时间), hourlyAvgValue (平均值)
          this.trendData = statisticsData.map(item => ({
            hour: item.timeFormat,           // 时间格式：如 "00:00", "01:00"
            value: parseFloat(item.hourlyAvgValue) || 0,  // 小时平均值
          }));
          
          console.log('📊 趋势图数据已转换:', this.trendData);
        } else {
          throw new Error(response.data?.msg || '获取趋势数据失败');
        }
      } catch (error) {
        console.error('❌ 获取趋势数据失败:', error);
        this.$message.error(`趋势数据获取失败: ${error.message || "未知错误"}`);
        this.trendData = [];
      } finally {
        this.trendLoading = false;
      }
    },
    
    goBack() {
      this.$router.go(-1);
    },
    
    showAutoModeDialog() {
      this.showAutoDialog = true;
      return new Promise((resolve, reject) => {
        this.autoDialogResolve = resolve;
        this.autoDialogReject = reject;
      });
    },
    
    handleAutoDialogConfirm() {
      const hasValue = Object.values(this.autoModeSettings).some(val => val && val.trim() !== '');
      if (!hasValue) {
        this.$message.warning('请至少设置一个设备阈值');
        return;
      }
      
      this.showAutoDialog = false;
      if (this.autoDialogResolve) {
        this.autoDialogResolve();
      }
    },
    
    handleAutoDialogCancel() {
      this.showAutoDialog = false;
      if (this.autoDialogReject) {
        this.autoDialogReject();
      }
    },
    
    handleAutoModeInput(key, value) {
      const filtered = value.replace(/[^\d.]/g, '');
      this.$set(this.autoModeSettings, key, filtered);
    },
    
    async handleModeSwitch(mode) {
      const originalMode = this.areaMode;
      const originalAutomation = this.automation;
      
      if (mode === 'auto') {
        try {
          await this.showAutoModeDialog();
        } catch {
          this.$nextTick(() => {
            this.areaMode = originalMode;
          });
          return;
        }
      }
      
      try {
        const token = this.getTokenAndValidate();
        const automationValue = mode === 'auto' ? 2 : 1;  // Integer类型：1-手动 2-自动
        
        let relayProtocolId = this.areaInfo.relayProtocolId || this.$route.query.configId || this.$route.query.deviceId;
        
        if (!relayProtocolId && this.areaInfo.id && this.areaInfo.id !== "AREA001") {
          relayProtocolId = this.areaInfo.id;
        }
        
        if (!relayProtocolId) {
          throw new Error('缺少设备TCP协议ID（relayProtocolId），无法切换模式');
        }
        
        const requestData = {
          automation: automationValue,
          relayProtocolId: String(relayProtocolId)  // 使用 relayProtocolId 替代 protocolId
        };
        
        console.log('模式切换请求参数:', requestData);
        
        if (mode === 'auto') {
          const automationConfig = {};
          Object.keys(this.autoModeSettings).forEach(key => {
            const value = this.autoModeSettings[key];
            if (value && value.trim() !== '') {
              automationConfig[key] = String(value);
            }
          });
          
          if (Object.keys(automationConfig).length > 0) {
            requestData.automationConfig = automationConfig;
          }
        }
        
        await this.apiCall(controlTcpApi, requestData, token);
        
        this.areaMode = mode;
        this.automation = String(automationValue);  // 本地保持字符串格式用于显示
        
        const config = MODE_CONFIG[mode];
        this.$message.success(`切换至${config.label}成功`);
        this.addHistoryRecord(`切换至${config.label}`);
      } catch (error) {
        console.error('❌ 模式切换失败:', error);
        this.$message.error(`切换失败: ${error.message || "未知错误"}`);
        this.areaMode = originalMode;
        this.automation = originalAutomation;
      }
    },
    
    handleSensorChange(sensor) {
      this.selectedSensor = sensor;
      // 传感器切换时，重新加载趋势数据
      this.fetchTrendData();
    },
    
    async handleDeviceControl(item, value, isRelay = true) {
      const originalValue = item.status;
      const displayName = this.getControlDisplayName(item.relayDeviceName);
      
      try {
        if (isRelay) {
          await this.controlDevice(item.relayDeviceName, value);
        }
        
        item.status = value;
        
        const action = isRelay && item.relayDeviceName !== '窗帘开关'
          ? `${displayName}${value ? "开启" : "关闭"}`
          : `${displayName}${this.getCurtainStatusText(value)}`;
          
        this.$message.success(action);
        this.addHistoryRecord(action);
      } catch (error) {
        console.error('设备控制失败:', error);
        this.$message.error(`${displayName}控制失败: ${error.message || "未知错误"}`);
        item.status = originalValue;
      }
    },
    
    async handleRelayChange(item, value) {
      await this.handleDeviceControl(item, value, true);
    },
    
    async handleCurtainChange(item, value) {
      await this.handleDeviceControl(item, value, true);
    },
    
    async controlDevice(deviceName, value) {
      const token = this.getTokenAndValidate();
      const currentDeviceMap = this.getCurrentDeviceMap();
      const paramKey = currentDeviceMap[deviceName];
      
      if (!paramKey) {
        throw new Error(`未知的设备类型: ${deviceName}，请联系管理员添加设备映射`);
      }
      
      let relayProtocolId = this.areaInfo.relayProtocolId || this.$route.query.configId || this.$route.query.deviceId;
      
      if (!relayProtocolId && this.areaInfo.id && this.areaInfo.id !== "AREA001") {
        relayProtocolId = this.areaInfo.id;
      }
      
      if (!relayProtocolId) {
        throw new Error('缺少设备TCP协议ID（relayProtocolId），无法控制设备');
      }
      
      // 构建请求参数：只传递当前操作的继电器参数
      // 确保automation为1或2，不能为0（1-手动 2-自动）
      const automationValue = Number(this.automation) || 1;
      const requestData = {
        relayProtocolId: String(relayProtocolId),  // 使用 relayProtocolId 替代 protocolId
        automation: automationValue,  // 添加控制模式参数（1-手动 2-自动）- Integer类型
        [paramKey]: Number(value)  // 只传递当前操作的设备参数
      };
      
      console.log('设备控制请求参数:', requestData);
      console.log(`控制设备: ${deviceName} -> ${paramKey} = ${value}`);
      
      return await this.apiCall(controlTcpApi, requestData, token);
    },
    
    getCurtainStatusText(value) {
      const statusMap = { 0: "关闭", 1: "开启", 2: "暂停" };
      return statusMap[value] || "未知状态";
    },
    
    getCurtainStatusClass(value) {
      const classMap = { 0: "curtain-closed", 1: "curtain-opened", 2: "curtain-paused" };
      return `curtain-status-text ${classMap[value] || ""}`;
    },
    
    async fetchTelemetryData() {
      const deviceId = this.deviceId || this.$route.query.deviceId;
      
      if (!deviceId) return;

      try {
        const response = await this.apiCall(getIotTelemetryByDeviceId, deviceId);
        
        if (response.data && response.data.code === 200) {
          const telemetryData = response.data.rows || [];
          
          // 保存完整的遥测数据列表（包含dataKey和dataKeyChinese）
          this.telemetryList = telemetryData;
          
          const telemetryMap = {};
          telemetryData.forEach(item => {
            telemetryMap[item.dataKeyChinese] = {
              curValue: item.dataValue,
              unit: item.unit,
              recordTime: item.recordTime
            };
          });
          
          if (!this.areaDevices || this.areaDevices.length === 0) {
            this.areaDevices = telemetryData.map(item => ({
              deviceName: item.dataKeyChinese,
              status: "1",
              curValue: item.dataValue,
              unit: item.unit,
              setValue: undefined
            }));
          } else {
            this.areaDevices = this.areaDevices.map(device => {
              const telemetry = telemetryMap[device.deviceName];
              if (telemetry) {
                return {
                  ...device,
                  curValue: telemetry.curValue,
                  unit: telemetry.unit
                };
              }
              return device;
            });
          }
        } else {
          throw new Error(response.data?.msg || '获取遥测数据失败');
        }
      } catch (error) {
        console.error('获取遥测数据失败:', error);
        this.$message.error(`获取遥测数据失败: ${error.message || "未知错误"}`);
      }
    },
    
    async refreshDevices() {
      this.$message.success("设备列表已刷新");
      await this.loadAreaData();
      await this.fetchTelemetryData();
    },
    
    async handleConfirmSetValues() {
      try {
        const token = this.getTokenAndValidate();
        
        const deviceControls = this.areaDevices.filter(d => 
          d.setValue !== undefined && 
          d.setValue !== null && 
          d.setValue !== '' &&
          d.setValue !== d.curValue
        );
        
        if (deviceControls.length === 0) {
          this.$message.warning("没有设备需要设置数值");
          return;
        }
        
        let relayProtocolId = this.areaInfo.relayProtocolId || this.$route.query.configId || this.$route.query.deviceId;
        
        if (!relayProtocolId && this.areaInfo.id && this.areaInfo.id !== "AREA001") {
          relayProtocolId = this.areaInfo.id;
        }
        
        if (!relayProtocolId) {
          throw new Error('缺少设备TCP协议ID（relayProtocolId），无法设置传感器数值');
        }
        
        const sensorConfig = {};
        let validParamsCount = 0;
        
        deviceControls.forEach(device => {
          const paramKey = SENSOR_DEVICE_MAP[device.deviceName];
          if (paramKey) {
            sensorConfig[paramKey] = parseFloat(device.setValue) || device.setValue;
            validParamsCount++;
          }
        });
        
        if (validParamsCount === 0) {
          this.$message.warning("没有识别到有效的传感器设备设置");
          return;
        }
        
        const requestData = {
          relayProtocolId: String(relayProtocolId),  // 使用 relayProtocolId 替代 protocolId
          sensorConfig: sensorConfig
        };
        
        await this.apiCall(controlTcpApi, requestData, token);
        
        this.$message.success(`成功设置${validParamsCount}个设备数值`);
        this.addHistoryRecord(`批量设置${validParamsCount}个传感器设备数值`);
        this.refreshDevices();
      } catch (error) {
        console.error('❌ 设备设置失败:', error);
        this.$message.error("设备设置数值提交失败: " + (error.message || "未知错误"));
      }
    },
    
    addHistoryRecord(action) {
      const newRecord = {
        id: Date.now(),
        time: new Date().toLocaleTimeString("zh-CN", { hour: "2-digit", minute: "2-digit" }),
        action,
        user: "管理员",
      };
      
      this.historyRecords.unshift(newRecord);
      if (this.historyRecords.length > 10) {
        this.historyRecords.pop();
      }
    },
    
    getControlDisplayName(deviceName) {
      return CONTROL_DISPLAY_NAMES[deviceName] || deviceName;
    },

    getDeviceIconClass(deviceName, isActive) {
      const iconName = DEVICE_ICON_MAP[deviceName] || DEVICE_ICON_MAP.default;
      
      if (deviceName === '窗帘开关' || deviceName === '遮阳帘' || deviceName === '窗帘机') {
        const activeClassMap = { 1: 'icon-green', 2: 'icon-orange', 0: 'icon-gray' };
        const activeClass = activeClassMap[isActive] || 'icon-gray';
        return ['iconfont', iconName, 'control-img', activeClass];
      }
      
      const activeClass = isActive ? 'icon-green' : 'icon-gray';
      return ['iconfont', iconName, 'control-img', activeClass];
    },
    
    formatTime(timestamp) {
      if (!timestamp) return '';
      try {
        const date = new Date(timestamp);
        return date.toLocaleString('zh-CN', {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit',
          hour: '2-digit',
          minute: '2-digit',
          second: '2-digit'
        });
      } catch (error) {
        console.error('时间格式化失败:', error);
        return '';
      }
    },

  }
};
</script>

<style lang="scss" scoped> 
$primary-color: #409eff;
$success-color: #67c23a;
$warning-color: #e6a23c;
$danger-color: #f56c6c;
$border-radius: 8px;
$shadow: 0 1px 4px rgba(0, 0, 0, 0.06);

.mt-16 { margin-top: 16px; }
.mt-20 { margin-top: 20px; }
.mb-16 { margin-bottom: 16px; }

.device-data-detail {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 40px);
  
  .page-card {
    border-radius: $border-radius;
    box-shadow: $shadow;
    border: 1px solid #e8eaec;
    overflow: hidden;
    background: white;
    
    .page-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 20px 24px !important;
      background: $primary-color;
      color: white;
      border-radius: $border-radius $border-radius 0 0;
      
      .header-left {
        display: flex;
        align-items: center;
        gap: 16px;
        
        .back-btn {
          padding: 8px 16px;
          border-radius: 4px;
          background: rgba(255, 255, 255, 0.15);
          border: 1px solid rgba(255, 255, 255, 0.3);
          color: white;
          transition: all 0.2s;
          
          &:hover { 
            background: rgba(255, 255, 255, 0.25);
          }
        }
        
        .page-title {
          font-size: 18px;
          font-weight: 500;
          display: flex;
          align-items: center;
          gap: 10px;
          
          i {
            font-size: 24px;
            background: rgba(255, 255, 255, 0.15);
            width: 40px;
            height: 40px;
            display: flex;
            align-items: center;
            justify-content: center;
            border-radius: 50%;
          }
        }
      }
    }
    
    .content {
      padding: 20px;
    }
  }
  
  .info-card, .chart-card, .camera-card {
    height: 100%;
    box-shadow: $shadow;
    border-radius: $border-radius;
    border: 1px solid #e8eaec;
    overflow: hidden;
    transition: all 0.2s;
    
    ::v-deep .el-card__header {
      background: #fafbfc;
      border-bottom: 1px solid #ebeef5;
      padding: 16px 20px;
      font-weight: 500;
      color: #303133;
      font-size: 15px;
    }
  }

  .info-card {
    .area-info {
      display: flex;
      gap: 20px;
      padding: 20px;

      .area-icon {
        width: 80px;
        height: 80px;
        background: $primary-color;
        border-radius: $border-radius;
        display: flex;
        align-items: center;
        justify-content: center;
        flex-shrink: 0;
        position: relative;

        i {
          font-size: 36px;
          color: white;
        }

        .icon-badge {
          position: absolute;
          top: -8px;
          right: -8px;
          width: 28px;
          height: 28px;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          border: 3px solid white;
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.25);

          &.online { background: $success-color; }
          &.offline { background: $danger-color; }
          
          i { font-size: 14px; color: white; }
        }
      }

      .area-details {
        flex: 1;

        .area-title {
          display: flex;
          align-items: center;
          gap: 12px;
          margin-bottom: 25px;

          h3 {
            margin: 0;
            color: #202226;
            font-size: 28px;
            font-weight: 800;
          }
          
          .el-tag {
            font-size: 13px;
            padding: 4px 12px;
            font-weight: 500;
          }
        }

        .area-meta {
          margin-bottom: 25px;

          .meta-row {
            display: flex;
            margin-bottom: 20px;
            gap: 20px;
            flex-wrap: wrap;

            .meta-item {
              display: flex;
              align-items: center;
              gap: 10px;
              flex: 1;
              min-width: 200px;
              padding: 12px 16px;
              background: #f8f9fa;
              border-radius: $border-radius;

              &:hover { background: #e9ecef; }
              &.full-width { flex: 100%; }

              i {
                color: $primary-color;
                font-size: 18px;
              }

              .meta-label {
                color: #6c757d;
                font-size: 15px;
                font-weight: 600;
                min-width: 70px;
              }

              .meta-value {
                color: #202226;
                font-size: 16px;
                font-weight: 700;

                &.highlight {
                  color: $primary-color;
                  font-weight: 800;
                }
              }
            }
          }
        }

        .area-stats {
          display: flex;
          gap: 25px;
          padding-top: 25px;
          border-top: 2px solid #f0f0f0;
          flex-wrap: wrap;

          .stats-item {
            text-align: center;
            flex: 1;
            min-width: 120px;
            padding: 16px;
            border-radius: $border-radius;
            background: #f8f9fa;

            &:hover { background: #e9ecef; }

            .stats-number {
              font-size: 24px;
              font-weight: 700;
              color: $success-color;
              margin-bottom: 6px;

              &.warning { color: $warning-color; }
              &.danger { color: $danger-color; }
            }

            .stats-label {
              font-size: 14px;
              color: #6c757d;
              font-weight: 500;
            }
          }
        }
      }
    }
  }

  .chart-card {
    .chart-container {
      height: 260px;
      padding: 16px;
      
      .chart-placeholder {
        height: 100%;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        background: #fafbfc;
        border-radius: $border-radius;
        color: #909399;
        border: 1px solid #ebeef5;

        &:hover { background: #f5f7fa; }

        i { font-size: 44px; margin-bottom: 12px; color: #c0c4cc; }
        p { margin: 0; font-size: 14px; }
      }
    }

    &.control-card {
      min-height: 160px;
      box-shadow: $shadow;
      border: 1px solid #e8eaec;
      
      &:hover {
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        border-color: $primary-color;
      }
      
      .control-panel {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 12px;
        padding: 20px 15px;
        height: 130px;
        justify-content: center;
        
        .control-img {
          width: 40px;
          height: 40px;
          font-size: 28px !important;
          
          &.icon-green { color: #67c23a !important; }
          &.icon-orange { color: #e6a23c !important; }
          &.icon-gray { color: #c0c4cc !important; }
        }
        
        .curtain-control {
          display: flex;
          flex-direction: column;
          align-items: center;
          gap: 8px;
          
          .curtain-status .curtain-status-text {
            font-size: 13px;
            font-weight: 500;
            padding: 4px 10px;
            border-radius: $border-radius;
            
            &.curtain-closed { color: #909399; background: #f5f7fa; }
            &.curtain-paused { color: $warning-color; background: #fdf6ec; }
            &.curtain-opened { color: $success-color; background: #f0f9ff; }
          }
        }
      }
    }
  }

  .camera-card .camera-container {
    min-height: 350px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #000;
    border-radius: $border-radius;
    position: relative;
    overflow: hidden;
    border: 1px solid #ebeef5;
  }

  .camera-header {
    display: flex;
    align-items: center;
    justify-content: space-between;

    .camera-controls {
      display: flex;
      gap: 8px;

      .el-button {
        padding: 0;
        font-size: 18px;
        color: #606266;
        transition: all 0.3s;

        &:hover {
          color: $primary-color;
          transform: scale(1.1);
        }

        &.is-loading {
          color: $primary-color;
        }
      }
    }
  }

  .video-wrapper {
    width: 100%;
    height: 100%;
    min-height: 350px;
    position: relative;
    background: #000;

    .video-overlay {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      padding: 12px;
      background: linear-gradient(180deg, rgba(0,0,0,0.6) 0%, rgba(0,0,0,0) 100%);
      pointer-events: none;
      opacity: 0;
      transition: opacity 0.3s;
    }

    &:hover .video-overlay {
      opacity: 1;
    }

    .video-info {
      display: flex;
      align-items: center;
      justify-content: space-between;
      color: white;
      font-size: 13px;

      .video-status {
        display: flex;
        align-items: center;
        gap: 6px;
        font-weight: 500;

        i {
          font-size: 14px;
          animation: pulse 2s ease-in-out infinite;
        }
      }

      .video-tip {
        font-size: 12px;
        opacity: 0.8;
      }
    }
  }
  
  .camera-loading, .camera-placeholder {
    width: 100%;
    height: 350px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    border-radius: $border-radius;
    
    i { 
      font-size: 48px; 
      margin-bottom: 16px; 
    }
    p { 
      margin: 0; 
      font-size: 15px; 
      font-weight: 500; 
    }
  }
  
  .camera-loading {
    color: $primary-color;
    background: rgba(64, 158, 255, 0.05);
    
    i { 
      font-size: 52px;
    }
  }
  
  .camera-placeholder {
    color: #909399;
    background: #fafbfc;

    i {
      opacity: 0.5;
    }
  }

  .mode-switch {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16px 20px;
    background: white;
    border-radius: $border-radius;
    border: 1px solid #e8eaec;
    box-shadow: $shadow;
    
    .mode-left {
      display: flex;
      align-items: center;
      gap: 16px;
      
      span {
        font-size: 15px;
        font-weight: 500;
        color: #303133;
      }
    }
  }

  .trend-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    
    span {
      font-weight: 500;
      color: #303133;
      font-size: 15px;
    }
  }
}

// 摄像头卡片样式
.camera-card {
  .camera-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    
    .camera-controls {
      display: flex;
      gap: 8px;
      
      .el-button {
        padding: 8px;
        font-size: 16px;
        color: #606266;
        transition: all 0.3s;
        
        &:hover:not(:disabled) {
          color: $primary-color;
          background: rgba(64, 158, 255, 0.1);
        }
        
        &:disabled {
          opacity: 0.5;
          cursor: not-allowed;
        }
      }
    }
  }
  
  .camera-content {
    .video-player-section {
      .video-info-bar {
        display: flex;
        flex-wrap: wrap;
        gap: 16px;
        padding: 12px 16px;
        background: #f8f9fa;
        border-bottom: 1px solid #ebeef5;
        
        .info-item {
          display: flex;
          align-items: center;
          gap: 6px;
          font-size: 13px;
          
          .label {
            color: #909399;
            font-weight: 500;
          }
          
          .value {
            color: #303133;
          }
          
          &.webrtc-server-config {
            flex: 1 1 100%;
            display: flex;
            align-items: center;
            gap: 8px;
            padding-top: 8px;
            border-top: 1px dashed #e0e0e0;
            margin-top: 4px;
            
            .el-input {
              flex: 1;
              max-width: 300px;
            }
          }
        }
      }
      
      .video-player-wrapper {
        width: 100%;
        height: 400px;
        background: #000;
        position: relative;
        display: flex;
        align-items: center;
        justify-content: center;
        
        .video-player {
          width: 100%;
          height: 100%;
          
          .video-element {
            width: 100%;
            height: 100%;
            object-fit: contain;
            background: #000;
          }
          
          .video-placeholder {
            width: 100%;
            height: 100%;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            color: #909399;
            
            i {
              font-size: 64px;
              margin-bottom: 16px;
              opacity: 0.5;
            }
            
            p {
              font-size: 15px;
              margin: 6px 0;
              
              &.hint-text {
                font-size: 13px;
                color: #c0c4cc;
              }
            }
          }
        }
        
        .video-offline {
          width: 100%;
          height: 100%;
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          color: #f56c6c;
          
          i {
            font-size: 64px;
            margin-bottom: 16px;
          }
          
          p {
            font-size: 15px;
            margin: 6px 0;
            
            &.hint-text {
              font-size: 13px;
              color: #909399;
            }
          }
        }
      }
    }
    
    .camera-details {
      padding: 16px;
      background: #fafbfc;
      
      .rtsp-url {
        font-family: monospace;
        font-size: 12px;
        color: $primary-color;
        word-break: break-all;
      }
    }
  }
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

@keyframes loading-spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>
  overflow: hidden;
  
  .el-dialog__header {
    padding: 16px 20px;
    background: $primary-color;
    border-bottom: none;
    
    .el-dialog__title {
      font-size: 16px;
      font-weight: 500;
      color: white;
    }
    
    .el-dialog__headerbtn {
      top: 16px;
      right: 16px;
      
      .el-dialog__close {
        color: white;
        
        &:hover {
          color: rgba(255, 255, 255, 0.8);
        }
      }
    }
  }
  
  .el-dialog__body {
    padding: 0;
    max-height: 480px;
    overflow-y: auto;
  }
  
  .dialog-content {
    padding: 20px;
    background: white;
    
    .dialog-tip {
      display: flex;
      align-items: center;
      gap: 10px;
      padding: 12px 14px;
      background: #ecf5ff;
      border-radius: 6px;
      border-left: 3px solid $primary-color;
      margin-bottom: 16px;
      
      .tip-icon {
        width: 32px;
        height: 32px;
        background: $primary-color;
        border-radius: 6px;
        display: flex;
        align-items: center;
        justify-content: center;
        flex-shrink: 0;
        
        i {
          font-size: 16px;
          color: white;
        }
      }
      
      .tip-text {
        flex: 1;
        
        .tip-title {
          font-size: 13px;
          font-weight: 500;
          color: #303133;
          margin-bottom: 2px;
        }
        
        .tip-desc {
          font-size: 12px;
          color: #606266;
          line-height: 1.4;
        }
      }
    }
    
    .auto-form {
      .el-form-item {
        margin-bottom: 16px;
        
        .el-input-group__append {
          background: $primary-color;
          color: white;
          border: none;
          font-weight: 500;
        }
      }
    }
  }
  
  .el-dialog__footer {
    padding: 14px 20px;
    border-top: 1px solid #ebeef5;
    background: #fafafa;
  }
}

.video-container-dialog {
  .video-info-bar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px 16px;
    background: #f5f7fa;
    border-bottom: 1px solid #ebeef5;
    
    .info-group {
      display: flex;
      align-items: center;
      gap: 8px;
      
      .info-label {
        font-size: 14px;
        color: #606266;
        font-weight: 500;
      }
      
      .info-text {
        font-size: 14px;
        color: #303133;
        font-weight: 600;
      }
    }
  }
  
  .video-player-wrapper {
    position: relative;
    width: 100%;
    height: 400px;
    background: #000;
    border-radius: $border-radius;
    overflow: hidden;
    border: 1px solid #ebeef5;
    
    .video-element {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
    
    .video-placeholder {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      background: rgba(0, 0, 0, 0.8);
      color: white;
      font-size: 14px;
      text-align: center;
      
      i {
        font-size: 48px;
        margin-bottom: 12px;
        color: #ff6b6b;
      }
      
      .hint-text {
        font-size: 12px;
        color: #909399;
        margin-top: 4px;
      }
    }
    
    .video-offline {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      background: rgba(0, 0, 0, 0.8);
      color: #f56c6c;
      font-size: 14px;
      text-align: center;
      
      i {
        font-size: 48px;
        margin-bottom: 12px;
      }
      
      .hint-text {
        font-size: 12px;
        color: #909399;
        margin-top: 4px;
      }
    }
  }
}
