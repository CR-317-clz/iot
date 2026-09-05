<template>
  <div class="device-management">
    <!-- 页面主卡片 -->
    <div class="page-header-container">
      <div class="page-header-wrapper">
        <div class="page-header-content">
          <div class="header-main">
            <i class="el-icon-monitor header-icon"></i>
            <h1 class="page-title">设备管理</h1>
          </div>
          <p class="page-desc">
            管理智慧农业园区的设备（共 {{ areaTotal }} 个区域）
          </p>
          <!-- 添加设备按钮 -->
          <div class="header-actions">
            <el-button
              type="primary"
              size="medium"
              @click="showAddDeviceDialog"
              icon="el-icon-circle-plus-outline"
              class="main-action-btn"
            >
              添加设备
            </el-button>
            <el-button
              type="success"
              size="medium"
              @click="loadAreaList"
              icon="el-icon-refresh"
              class="main-action-btn"
            >
              刷新
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 内容区 -->
    <div class="content-container">
      <div class="content-wrapper" v-loading="loading" element-loading-text="正在加载区域列表..." element-loading-spinner="el-icon-loading" element-loading-background="rgba(0, 0, 0, 0.8)">
        <!-- 区域卡片列表 -->
        <div class="cards-grid" v-if="!loading && areaTotal > 0">
          <div 
            class="area-card-wrapper"
            v-for="(area, index) in areaList" 
            :key="area.id"
          >
            <div class="area-card">
              <div class="card-header">
                <div class="area-icon">
                  <i class="el-icon-s-grid"></i>
                </div>
                <h3 class="area-name">{{ area.deviceName || '数智农业实训架(' + (index + 1) + ')' }}</h3>
                <!-- 设备操作按钮 -->
                <div class="device-actions">
                  <el-dropdown @command="handleDeviceAction">
                    <el-button class="action-dropdown-btn" size="mini">
                      操作<i class="el-icon-arrow-down el-icon--right"></i>
                    </el-button>
                    <el-dropdown-menu slot="dropdown">
                      <el-dropdown-item :command="{action: 'edit', data: area}">
                        编辑设备
                      </el-dropdown-item>
                      <el-dropdown-item :command="{action: 'detail', data: area}">
                        查看详情
                      </el-dropdown-item>
                      <el-dropdown-item :command="{action: 'configProtocol', data: area}">
                        配置协议
                      </el-dropdown-item>
                      <el-dropdown-item :command="{action: 'deviceDetail', data: area}">
                        详细设备
                      </el-dropdown-item>
                      <el-dropdown-item :command="{action: 'delete', data: area}" divided>
                        删除设备
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </el-dropdown>
                </div>
              </div>
              
              <div class="card-content" @click="viewAreaDetail(area)">
                <div class="area-details">
                  <div class="detail-item">
                    <span class="detail-label">类型：</span>
                    <span class="detail-value">{{ area.hardwareType || '-' }}</span>
                  </div>
                  <div class="detail-item">
                    <span class="detail-label">状态：</span>
                    <span 
                      :class="['status-badge', getStatusClass(area.status)]"
                    >
                      {{ getStatusText(area.status) }}
                    </span>
                  </div>
                  <div class="detail-item">
                    <span class="detail-label">设备接口：</span>
                    <span class="detail-value">{{ area.host || '-' }}</span>
                  </div>
                  <div class="detail-item">
                    <span class="detail-label">设备地址：</span>
                    <span class="detail-value">{{ area.location || '-' }}</span>
                  </div>
                  <div class="detail-item" v-if="area.protocolId || area.relayProtocolId">
                    <span class="detail-label">协议配置：</span>
                    <span class="detail-value">
                      <el-tag size="mini" type="primary" v-if="area.protocolId" style="margin-right: 5px;">
                        MQTT
                      </el-tag>
                      <el-tag size="mini" type="success" v-if="area.relayProtocolId">
                        TCP
                      </el-tag>
                      <span v-if="!area.protocolId && !area.relayProtocolId">-</span>
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-if="!loading && areaTotal === 0" class="empty-state">
          <div class="empty-content">
            <i class="el-icon-s-grid empty-icon"></i>
            <p class="empty-text">暂无可用的区域设备</p>
            <el-button 
              class="refresh-btn" 
              type="primary" 
              size="medium" 
              @click="loadAreaList"
            >
              <i class="el-icon-refresh"></i>
              重新加载
            </el-button>
          </div>
        </div>

        <!-- 错误状态 -->
        <div v-if="!loading && error" class="error-state">
          <div class="error-content">
            <i class="el-icon-warning-outline error-icon"></i>
            <p class="error-text">{{ error }}</p>
            <el-button 
              class="retry-btn" 
              type="primary" 
              size="medium" 
              @click="loadAreaList"
            >
              <i class="el-icon-refresh"></i>
              重试
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 协议配置对话框 (协议选择模式) -->
    <el-dialog
      :visible.sync="protocolDialogVisible"
      :title="protocolDialogTitle"
      width="1000px"
      :before-close="handleProtocolDialogClose"
      class="protocol-dialog modern-dialog"
    >
      <div v-loading="protocolLoading" class="protocol-dialog-content">
        <!-- 已选择协议展示区域 -->
        <div class="selected-protocol-display" v-if="selectedMqttProtocolId || selectedTcpProtocolId">
          <div class="display-header">
            <i class="el-icon-check"></i>
            <span>当前已选择的协议</span>
          </div>
          <div class="display-content">
            <!-- MQTT 协议展示 -->
            <div v-if="selectedMqttProtocolId" class="protocol-display-card mqtt">
              <div class="card-icon">
                <i class="el-icon-connection"></i>
              </div>
              <div class="card-info">
                <div class="card-title">
                  <span class="protocol-name">{{ getProtocolById(selectedMqttProtocolId)?.protocolName }}</span>
                  <el-tag size="mini" type="primary">MQTT</el-tag>
                </div>
                <div class="card-desc">{{ getProtocolById(selectedMqttProtocolId)?.description || '暂无描述' }}</div>
                <div class="card-config" v-if="getProtocolConfig(getProtocolById(selectedMqttProtocolId))">
                  <span class="config-item" v-if="getProtocolConfig(getProtocolById(selectedMqttProtocolId)).host">
                    <i class="el-icon-link"></i>
                    {{ getProtocolConfig(getProtocolById(selectedMqttProtocolId)).host }}
                  </span>
                  <span class="config-item" v-if="getProtocolConfig(getProtocolById(selectedMqttProtocolId)).topic">
                    <i class="el-icon-message"></i>
                    {{ getProtocolConfig(getProtocolById(selectedMqttProtocolId)).topic }}
                  </span>
                </div>
              </div>
              <div class="card-actions">
                <el-button 
                  type="text" 
                  size="small" 
                  icon="el-icon-close"
                  @click="clearProtocolSelection('mqtt')"
                  class="remove-btn"
                >
                  移除
                </el-button>
              </div>
            </div>
            
            <!-- TCP 协议展示 -->
            <div v-if="selectedTcpProtocolId" class="protocol-display-card tcp">
              <div class="card-icon">
                <i class="el-icon-link"></i>
              </div>
              <div class="card-info">
                <div class="card-title">
                  <span class="protocol-name">{{ getProtocolById(selectedTcpProtocolId)?.protocolName }}</span>
                  <el-tag size="mini" type="warning">TCP</el-tag>
                </div>
                <div class="card-desc">{{ getProtocolById(selectedTcpProtocolId)?.description || '暂无描述' }}</div>
                <div class="card-config" v-if="getProtocolConfig(getProtocolById(selectedTcpProtocolId))">
                  <span class="config-item" v-if="getProtocolConfig(getProtocolById(selectedTcpProtocolId)).host">
                    <i class="el-icon-link"></i>
                    {{ getProtocolConfig(getProtocolById(selectedTcpProtocolId)).host }}
                  </span>
                  <span class="config-item" v-if="getProtocolConfig(getProtocolById(selectedTcpProtocolId)).port">
                    <i class="el-icon-place"></i>
                    {{ getProtocolConfig(getProtocolById(selectedTcpProtocolId)).port }}
                  </span>
                </div>
              </div>
              <div class="card-actions">
                <el-button 
                  type="text" 
                  size="small" 
                  icon="el-icon-close"
                  @click="clearProtocolSelection('tcp')"
                  class="remove-btn"
                >
                  移除
                </el-button>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 空状态提示 -->
        <div class="no-selection-hint" v-else>
          <i class="el-icon-info"></i>
          <span>请从下方选择至少一个协议配置</span>
        </div>
        
        <el-tabs v-model="activeProtocolTab" type="card" class="protocol-tabs">
          <!-- MQTT协议Tab -->
          <el-tab-pane label="MQTT协议" name="mqtt">
            <div class="protocol-tab-content">
              <div class="protocol-list-container">
                <div class="section-header">
                  <h4>可用的MQTT协议</h4>
                  <el-button 
                    type="primary" 
                    size="small" 
                    icon="el-icon-plus"
                    @click="createNewProtocol('mqtt')"
                  >
                    新建MQTT协议
                  </el-button>
                </div>
                
                <div class="protocol-grid">
                  <div 
                    v-for="protocol in mqttProtocolList" 
                    :key="protocol.protocolId"
                    :class="['protocol-card', { 'selected': selectedMqttProtocolId === protocol.protocolId }]"
                  >
                    <div class="protocol-header">
                      <div class="protocol-title">
                        <i class="el-icon-connection"></i>
                        <span>{{ protocol.protocolName }}</span>
                      </div>
                      <div class="protocol-badges">
                        <el-tag size="mini" type="primary">{{ protocol.remark || protocol.protocolType || protocol.protocolName }}</el-tag>
                        <el-tag size="mini" :type="protocol.status === '1' ? 'success' : 'info'">
                          {{ protocol.status === '1' ? '已启用' : '未启用' }}
                        </el-tag>
                      </div>
                    </div>
                    
                    <div class="protocol-body">
                      <div class="protocol-info-row" v-if="protocol.description">
                        <span class="label">描述：</span>
                        <span class="value">{{ protocol.description || '-' }}</span>
                      </div>
                      
                      <!-- 解析并显示配置信息 -->
                      <div class="protocol-config" v-if="getProtocolConfig(protocol)">
                        <!-- MQTT 协议特有字段 -->
                        <div class="protocol-info-row" v-if="getProtocolConfig(protocol).brokerUrl">
                          <span class="label">Broker：</span>
                          <span class="value">{{ getProtocolConfig(protocol).brokerUrl }}</span>
                        </div>
                        <div class="protocol-info-row" v-if="getProtocolConfig(protocol).host">
                          <span class="label">主机：</span>
                          <span class="value">{{ getProtocolConfig(protocol).host }}</span>
                        </div>
                        <div class="protocol-info-row" v-if="getProtocolConfig(protocol).port">
                          <span class="label">端口：</span>
                          <span class="value">{{ getProtocolConfig(protocol).port }}</span>
                        </div>
                        <div class="protocol-info-row" v-if="getProtocolConfig(protocol).topic">
                          <span class="label">主题：</span>
                          <span class="value">{{ getProtocolConfig(protocol).topic }}</span>
                        </div>
                        <div class="protocol-info-row" v-if="getProtocolConfig(protocol).clientId">
                          <span class="label">客户端ID：</span>
                          <span class="value">{{ getProtocolConfig(protocol).clientId }}</span>
                        </div>
                        <div class="protocol-info-row" v-if="getProtocolConfig(protocol).username">
                          <span class="label">用户名：</span>
                          <span class="value">{{ getProtocolConfig(protocol).username }}</span>
                        </div>
                        <div class="protocol-info-row" v-if="getProtocolConfig(protocol).qos !== undefined && getProtocolConfig(protocol).qos !== null">
                          <span class="label">QoS：</span>
                          <span class="value">{{ getProtocolConfig(protocol).qos }}</span>
                        </div>
                      </div>
                    </div>
                    
                    <div class="protocol-actions">
                      <el-button 
                        type="primary" 
                        size="small"
                        :disabled="selectedMqttProtocolId === protocol.protocolId"
                        @click="selectProtocol('mqtt', protocol)"
                      >
                        {{ selectedMqttProtocolId === protocol.protocolId ? '已选择' : '选择协议' }}
                      </el-button>
                    </div>
                  </div>
                  
                  <div v-if="mqttProtocolList.length === 0" class="empty-protocol">
                    <div class="empty-content">
                      <i class="el-icon-info"></i>
                      <p>暂无MQTT协议</p>
                      <el-button 
                        type="primary" 
                        size="small"
                        @click="createNewProtocol('mqtt')"
                      >
                        立即新建
                      </el-button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>

          <!-- TCP协议Tab -->
          <el-tab-pane label="TCP协议" name="tcp">
            <div class="protocol-tab-content">
              <div class="protocol-list-container">
                <div class="section-header">
                  <h4>可用的TCP协议</h4>
                  <el-button 
                    type="primary" 
                    size="small" 
                    icon="el-icon-plus"
                    @click="createNewProtocol('tcp')"
                  >
                    新建TCP协议
                  </el-button>
                </div>
                
                <div class="protocol-grid">
                  <div 
                    v-for="protocol in tcpProtocolList" 
                    :key="protocol.protocolId"
                    :class="['protocol-card', { 'selected': selectedTcpProtocolId === protocol.protocolId }]"
                  >
                    <div class="protocol-header">
                      <div class="protocol-title">
                        <i class="el-icon-link"></i>
                        <span>{{ protocol.protocolName }}</span>
                      </div>
                      <div class="protocol-badges">
                        <el-tag size="mini" type="warning">{{ protocol.remark || protocol.protocolType || protocol.protocolName }}</el-tag>
                        <el-tag size="mini" :type="protocol.status === '1' ? 'success' : 'info'">
                          {{ protocol.status === '1' ? '已启用' : '未启用' }}
                        </el-tag>
                      </div>
                    </div>
                    
                    <div class="protocol-body">
                      <div class="protocol-info-row" v-if="protocol.description">
                        <span class="label">描述：</span>
                        <span class="value">{{ protocol.description || '-' }}</span>
                      </div>
                      
                      <!-- 解析并显示配置信息 -->
                      <div class="protocol-config" v-if="getProtocolConfig(protocol)">
                        <!-- TCP 协议特有字段 -->
                        <div class="protocol-info-row" v-if="getProtocolConfig(protocol).host">
                          <span class="label">主机：</span>
                          <span class="value">{{ getProtocolConfig(protocol).host }}</span>
                        </div>
                        <div class="protocol-info-row" v-if="getProtocolConfig(protocol).port">
                          <span class="label">端口：</span>
                          <span class="value">{{ getProtocolConfig(protocol).port }}</span>
                        </div>
                        <div class="protocol-info-row" v-if="getProtocolConfig(protocol).timeout">
                          <span class="label">超时：</span>
                          <span class="value">{{ getProtocolConfig(protocol).timeout }}ms</span>
                        </div>
                        <div class="protocol-info-row" v-if="getProtocolConfig(protocol).retryTimes">
                          <span class="label">重试次数：</span>
                          <span class="value">{{ getProtocolConfig(protocol).retryTimes }}</span>
                        </div>
                        <div class="protocol-info-row" v-if="getProtocolConfig(protocol).encoding">
                          <span class="label">编码：</span>
                          <span class="value">{{ getProtocolConfig(protocol).encoding }}</span>
                        </div>
                      </div>
                    </div>
                    
                    <div class="protocol-actions">
                      <el-button 
                        type="primary" 
                        size="small"
                        :disabled="selectedTcpProtocolId === protocol.protocolId"
                        @click="selectProtocol('tcp', protocol)"
                      >
                        {{ selectedTcpProtocolId === protocol.protocolId ? '已选择' : '选择协议' }}
                      </el-button>
                    </div>
                  </div>
                  
                  <div v-if="tcpProtocolList.length === 0" class="empty-protocol">
                    <div class="empty-content">
                      <i class="el-icon-info"></i>
                      <p>暂无TCP协议</p>
                      <el-button 
                        type="primary" 
                        size="small"
                        @click="createNewProtocol('tcp')"
                      >
                        立即新建
                      </el-button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
      
      <span slot="footer" class="dialog-footer">
        <div class="footer-info">
          <span v-if="selectedMqttProtocolId || selectedTcpProtocolId">
            已选择: 
            <el-tag v-if="selectedMqttProtocolId" type="primary" size="mini">
              MQTT: {{ getProtocolById(selectedMqttProtocolId)?.protocolName }}
            </el-tag>
            <el-tag v-if="selectedTcpProtocolId" type="warning" size="mini">
              TCP: {{ getProtocolById(selectedTcpProtocolId)?.protocolName }}
            </el-tag>
          </span>
          <span v-else class="no-selection">请至少选择一个协议</span>
        </div>
        <div class="footer-actions">
          <el-button 
            @click="handleProtocolDialogClose"
            class="cancel-btn"
          >
            取消
          </el-button>
          <el-button 
            type="primary"
            :disabled="!selectedMqttProtocolId && !selectedTcpProtocolId"
            @click="confirmProtocolSelection"
            class="confirm-btn"
          >
            确认配置
          </el-button>
        </div>
      </span>
    </el-dialog>

    <!-- 添加/编辑设备对话框 -->
    <el-dialog
      :visible.sync="deviceDialogVisible"
      :title="deviceDialogTitle"
      width="620px"
      :before-close="handleDeviceDialogClose"
      class="device-dialog modern-dialog"
    >
      <el-form 
        :model="deviceForm"
        :rules="deviceFormRules"
        ref="deviceForm"
        label-width="100px"
        class="device-form modern-form"
      >
        <el-form-item label="设备名称" prop="deviceName">
          <el-input 
            v-model="deviceForm.deviceName" 
            placeholder="请输入设备名称"
            class="modern-input"
          ></el-input>
        </el-form-item>
        <el-form-item label="设备编号" prop="deviceCode">
          <el-input 
            v-model="deviceForm.deviceCode" 
            placeholder="请输入设备编号"
            class="modern-input"
          ></el-input>
        </el-form-item>
        <el-form-item label="设备类型" prop="deviceType">
          <el-select 
            v-model="deviceForm.deviceType" 
            placeholder="请选择设备类型" 
            style="width: 100%"
            class="modern-select"
          >
            <el-option label="传感器" value="SENSOR"></el-option>
            <el-option label="执行器" value="ACTUATOR"></el-option>
            <el-option label="网关" value="GATEWAY"></el-option>
            <el-option label="其他" value="OTHER"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="设备地址" prop="location">
          <el-input 
            v-model="deviceForm.location" 
            placeholder="请输入设备地址"
            class="modern-input"
          ></el-input>
        </el-form-item>
        <el-form-item label="MQTT协议">
          <el-select 
            v-model="deviceForm.protocolId" 
            placeholder="请选择MQTT协议(可选)" 
            clearable
            style="width: 100%"
            class="modern-select"
          >
            <el-option 
              v-for="protocol in mqttProtocolList" 
              :key="protocol.protocolId" 
              :label="`${protocol.protocolName} (${protocol.remark || protocol.protocolType || 'MQTT'})`" 
              :value="protocol.protocolId"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="TCP协议">
          <el-select 
            v-model="deviceForm.relayProtocolId" 
            placeholder="请选择TCP协议(可选)" 
            clearable
            style="width: 100%"
            class="modern-select"
          >
            <el-option 
              v-for="protocol in tcpProtocolList" 
              :key="protocol.protocolId" 
              :label="`${protocol.protocolName} (${protocol.remark || protocol.protocolType || 'TCP'})`" 
              :value="protocol.protocolId"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group 
            v-model="deviceForm.status"
            class="modern-radio-group"
          >
            <el-radio label="ONLINE">在线</el-radio>
            <el-radio label="OFFLINE">离线</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="handleDeviceDialogClose" class="cancel-btn">取消</el-button>
        <el-button type="primary" @click="submitDeviceForm" class="confirm-btn">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getAllDeviceList, addDevice, updateDevice, deleteDevice } from "@/api/iot-device"
import { getAllDeviceProtocolList } from "@/api/iot-protocol"
import { mapGetters, mapActions } from "vuex";

export default {
  name: "DeviceManagement",
  data() {
    return {
      areaList: [],
      areaTotal: 0,
      loading: false,
      error: "",
      protocolDialogVisible: false,
      protocolDialogTitle: '',
      protocolLoading: false,
      currentDeviceForProtocol: null,
      activeProtocolTab: 'mqtt',
      protocolList: [],
      mqttProtocolList: [],
      tcpProtocolList: [],
      selectedMqttProtocolId: null,
      selectedTcpProtocolId: null,
      deviceDialogVisible: false,
      deviceDialogTitle: '',
      isEditDevice: false,
      currentDeviceId: null,
      deviceForm: {
        deviceName: '',
        deviceCode: '',
        deviceType: '',
        status: 'ONLINE',
        location: '',
        protocolId: '',
        relayProtocolId: '',
        delFlag: '0'
      },
      deviceFormRules: {
        deviceName: [{ required: true, message: '请输入设备名称', trigger: 'blur' }],
        deviceCode: [{ required: true, message: '请输入设备编号', trigger: 'blur' }],
        deviceType: [{ required: true, message: '请选择设备类型', trigger: 'change' }],
        location: [{ required: true, message: '请输入设备地址/位置', trigger: 'blur' }]
      }
    };
  },
  
  computed: {
    ...mapGetters(["getToken", "getUserId", "getDeviceList"])
  },
  
  created() {
    this.loadAreaList();
    this.loadProtocolList();
  },
  methods: {
    ...mapActions(["setDeviceList", "setDeviceById"]),

    normalizeStatus(status) {
      return (status || 'OFFLINE').toUpperCase();
    },

    getStatusClass(status) {
      return this.normalizeStatus(status) === 'ONLINE' ? 'status-online' : 'status-offline';
    },

    getStatusText(status) {
      return this.normalizeStatus(status) === 'ONLINE' ? '在线' : '离线';
    },

    async loadAreaList() {
      this.loading = true;
      this.error = "";
      try {
        const response = await getAllDeviceList();
        if (response.data?.code === 200) {
          const rows = response.data.rows || [];
          this.setDeviceList(rows);
          
          // 打印第一条数据，用于检查后端返回的字段结构
          if (rows.length > 0) {
            console.log("🔍 后端返回的第一条设备数据结构:", rows[0]);
            console.log("📋 可用字段列表:", Object.keys(rows[0]));
          }
          
          this.areaList = rows.map(item => ({
            id: item.deviceId,
            deviceId: item.deviceId,          // 添加 deviceId 字段
            deviceName: item.deviceName || `设备${item.deviceId}`,
            deviceCode: item.deviceCode,
            configName: item.deviceName || item.deviceCode || `设备${item.deviceId}`,
            deviceType: item.deviceType,      // 添加 deviceType 字段
            hardwareType: item.deviceType,
            status: this.normalizeStatus(item.status),
            host: item.host,
            topic: item.topic,                // 添加 topic 字段
            deviceAddr: item.deviceAddr,
            location: item.location,
            configId: item.deviceId,
            protocolId: item.protocolId,
            relayProtocolId: item.relayProtocolId,
            createTime: item.createTime,      // 添加 createTime 字段
            updateTime: item.updateTime,      // 添加 updateTime 字段
            delFlag: item.delFlag,
            rawData: item,
            registerDataList: item.registerDataList || []
          }));
          
          this.areaTotal = this.areaList.length;
          console.log("加载的区域数据:", this.areaList);
        } else {
          const errorMsg = response.data?.msg || response.data?.message || "获取区域列表失败";
          this.$message.warning(errorMsg);
          this.error = errorMsg;
        }
      } catch (error) {
        console.error("获取区域列表出错:", error);
        const errorMsg = error.response?.data?.msg || error.message || "网络错误";
        this.$message.error(`获取区域列表出错: ${errorMsg}`);
        this.error = "区域数据加载失败，请检查网络连接";
      } finally {
        this.loading = false;
      }
    },

    refreshAreaList() {
      this.loadAreaList();
      this.$message.success("区域列表已刷新");
    },

    viewAreaDetail(area) {
      // 设置 store 中的 configId（实际上是 deviceId）
      this.setDeviceById({ deviceName: area.deviceName, configId: area.deviceId });
      
      // 详细打印传递的数据
      console.log("====== 设备管理页面传递数据 ======");
      console.log("📦 选中设备完整信息:", area);
      console.log("🔑 关键字段检查:");
      console.log("  - deviceId:", area.deviceId);
      console.log("  - deviceCode:", area.deviceCode);
      console.log("  - deviceType:", area.deviceType);
      console.log("  - host:", area.host);
      console.log("  - topic:", area.topic);
      console.log("  - location:", area.location);
      console.log("  - protocolId:", area.protocolId);
      console.log("  - relayProtocolId:", area.relayProtocolId);
      console.log("  - updateTime:", area.updateTime);
      console.log("==================================");
      
      // 传递完整的设备信息到详情页
      // ⚠️ 关键修改：使用 name 而不是 path，这样 params 才能正确传递
      this.$router.push({
        name: "device-data-detail",  // 使用命名路由
        query: { 
          deviceId: area.deviceId,  // 用于接口调用
          deviceName: area.deviceName,
          configId: area.deviceId   // 兼容旧代码
        },
        params: {
          areaInfo: {
            // 基本信息
            id: area.deviceId,
            deviceId: area.deviceId,
            deviceName: area.deviceName,
            configName: area.deviceName,
            deviceCode: area.deviceCode,
            
            // 状态信息
            status: area.status === 'online' ? '1' : '0',
            
            // 设备类型
            deviceType: area.deviceType,
            hardwareType: area.deviceType || area.hardwareType,
            
            // 网络配置
            host: area.host,
            topic: area.topic,
            
            // 协议配置
            protocolId: area.protocolId,
            relayProtocolId: area.relayProtocolId,
            
            // 位置信息
            location: area.location,
            
            // 时间戳
            createTime: area.createTime,
            updateTime: area.updateTime
          }
        }
      });
      
      console.log("✅ 路由跳转完成，使用命名路由传递 params");
    },

    async loadProtocolList() {
      try {
        const response = await getAllDeviceProtocolList();
        console.log('协议列表接口原始响应:', response.data);
        
        if (response.data?.code === 200) {
          this.protocolList = response.data.rows || [];
          console.log('所有协议列表:', this.protocolList);
          
          const filterProtocolByType = (type) => this.protocolList.filter(p => {
            const protocolType = (p.remark || p.protocolName || p.protocolType || '').toUpperCase();
            return protocolType.includes(type);
          });
          
          this.mqttProtocolList = filterProtocolByType('MQTT');
          this.tcpProtocolList = filterProtocolByType('TCP');
          
          console.log('协议列表加载完成:', {
            total: this.protocolList.length,
            mqtt: this.mqttProtocolList.length,
            tcp: this.tcpProtocolList.length
          });
        } else {
          console.warn('协议列表接口返回异常:', response.data);
        }
      } catch (error) {
        console.error("获取协议列表出错:", error);
      }
    },

    async showProtocolConfig(area) {
      this.currentDeviceForProtocol = area;
      this.protocolDialogTitle = `配置设备协议 - ${area.deviceName || area.configName}`;
      this.protocolDialogVisible = true;
      this.protocolLoading = true;
      this.activeProtocolTab = 'mqtt';
      this.selectedMqttProtocolId = null;
      this.selectedTcpProtocolId = null;
      
      console.log("配置设备协议:", area, "设备当前协议ID:", { protocolId: area.protocolId, relayProtocolId: area.relayProtocolId });

      try {
        await this.loadProtocolList();
        console.log("开始匹配设备协议...");
        
        const matchProtocol = (protocolId, expectedType) => {
          const matched = this.protocolList.find(p => p.protocolId === protocolId);
          if (matched) {
            const protocolTypeStr = (matched.remark || matched.protocolName || matched.protocolType || '').toUpperCase();
            console.log(`找到设备${expectedType}协议:`, { protocolId: matched.protocolId, protocolName: matched.protocolName });
            return protocolTypeStr.includes(expectedType) ? matched.protocolId : null;
          }
          return null;
        };
        
        if (area.protocolId) {
          this.selectedMqttProtocolId = matchProtocol(area.protocolId, 'MQTT');
        }
        
        if (area.relayProtocolId) {
          this.selectedTcpProtocolId = matchProtocol(area.relayProtocolId, 'TCP');
        }
        
        console.log("协议匹配完成:", {
          selectedMqttProtocolId: this.selectedMqttProtocolId,
          selectedTcpProtocolId: this.selectedTcpProtocolId
        });
        
        if (this.selectedMqttProtocolId) {
          this.activeProtocolTab = 'mqtt';
        } else if (this.selectedTcpProtocolId) {
          this.activeProtocolTab = 'tcp';
        }
      } catch (error) {
        console.error("加载协议配置信息出错:", error);
        this.$message.warning('加载协议配置失败');
      } finally {
        this.protocolLoading = false;
      }
    },

    selectProtocol(type, protocol) {
      if (type === 'mqtt') {
        this.selectedMqttProtocolId = protocol.protocolId;
      } else if (type === 'tcp') {
        this.selectedTcpProtocolId = protocol.protocolId;
      }
      this.$message.success(`已选择${type.toUpperCase()}协议: ${protocol.protocolName}`);
    },

    clearProtocolSelection(type) {
      if (type === 'mqtt') {
        this.selectedMqttProtocolId = null;
      } else if (type === 'tcp') {
        this.selectedTcpProtocolId = null;
      }
      this.$message.info(`已移除${type.toUpperCase()}协议选择`);
    },

    createNewProtocol(type) {
      this.$message.info(`创建新${type.toUpperCase()}协议功能待开发`);
    },

    getProtocolConfig(protocol) {
      if (!protocol) return null;
      
      console.log('解析协议配置:', { id: protocol.protocolId, name: protocol.protocolName });
      
      if (protocol.protocolConfig && typeof protocol.protocolConfig === 'object') {
        console.log('使用 protocolConfig 对象:', protocol.protocolConfig);
        return protocol.protocolConfig;
      }
      
      if (protocol.configJson) {
        try {
          const parsed = JSON.parse(protocol.configJson);
          console.log('解析 configJson 成功:', parsed);
          return parsed.protocolConfig && typeof parsed.protocolConfig === 'object' ? parsed.protocolConfig : parsed;
        } catch (e) {
          console.warn('解析协议配置失败:', protocol.protocolName, e);
        }
      }
      
      console.log('没有找到有效的协议配置');
      return null;
    },

    getProtocolById(protocolId) {
      return this.protocolList.find(p => p.protocolId === protocolId);
    },

    async confirmProtocolSelection() {
      if (!this.selectedMqttProtocolId && !this.selectedTcpProtocolId) {
        this.$message.warning('请至少选择一个协议');
        return;
      }

      const loading = this.$loading({
        lock: true,
        text: '正在配置协议...',
        spinner: 'el-icon-loading',
        background: 'rgba(0, 0, 0, 0.7)'
      });

      try {
        if (this.currentDeviceForProtocol) {
          await this.updateDeviceProtocol(
            this.currentDeviceForProtocol.id, 
            this.selectedMqttProtocolId,
            this.selectedTcpProtocolId
          );
        }

        const selectedProtocols = [];
        if (this.selectedMqttProtocolId) {
          selectedProtocols.push(`MQTT: ${this.getProtocolById(this.selectedMqttProtocolId).protocolName}`);
        }
        if (this.selectedTcpProtocolId) {
          selectedProtocols.push(`TCP: ${this.getProtocolById(this.selectedTcpProtocolId).protocolName}`);
        }
        
        this.$message.success(`协议配置成功！已选择：${selectedProtocols.join(', ')}`);
        await this.loadAreaList();
        this.handleProtocolDialogClose();
      } catch (error) {
        console.error('配置协议出错:', error);
        this.$message.error(`配置失败: ${error.message || "网络错误"}`);
      } finally {
        loading.close();
      }
    },

    async updateDeviceProtocol(deviceId, mqttProtocolId, tcpProtocolId) {
      const device = this.areaList.find(d => d.id === deviceId);
      if (!device) throw new Error('设备不存在');

      const params = {
        deviceId: deviceId,
        deviceName: device.deviceName,
        deviceCode: device.deviceCode,
        deviceType: device.hardwareType,
        status: device.status,
        location: device.location,
        protocolId: mqttProtocolId || null,
        relayProtocolId: tcpProtocolId || null,
        delFlag: device.delFlag || '0'
      };

      console.log('更新设备协议参数:', { deviceId, deviceName: device.deviceName, ...params });
      const response = await updateDevice(this.getToken, deviceId, params);
      
      if (response.data?.code !== 200) {
        throw new Error(response.data?.msg || '更新协议失败');
      }
      console.log('设备协议更新成功');
    },

    handleProtocolDialogClose() {
      this.protocolDialogVisible = false;
      this.currentDeviceForProtocol = null;
      this.activeProtocolTab = 'mqtt';
      this.selectedMqttProtocolId = null;
      this.selectedTcpProtocolId = null;
    },

    formatDate(timestamp) {
      if (!timestamp) return '-';
      return new Date(timestamp).toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      });
    },

    viewDeviceDetail(area) {
      this.$router.push({ path: "/layout/device-list", query: { areaId: area.id } });
    },

    showAddDeviceDialog() {
      this.isEditDevice = false;
      this.deviceDialogTitle = '添加设备';
      this.deviceForm = {
        deviceName: '',
        deviceCode: '',
        deviceType: '',
        status: 'ONLINE',
        location: '',
        protocolId: '',
        relayProtocolId: '',
        delFlag: '0'
      };
      this.deviceDialogVisible = true;
    },

    showEditDeviceDialog(area) {
      this.isEditDevice = true;
      this.deviceDialogTitle = '编辑设备';
      this.currentDeviceId = area.id;
      
      this.deviceForm = {
        deviceName: area.deviceName || '',
        deviceCode: area.deviceCode || '',
        deviceType: area.hardwareType || '',
        status: this.normalizeStatus(area.status),
        location: area.location || '',
        protocolId: area.protocolId || '',
        relayProtocolId: area.relayProtocolId || '',
        delFlag: area.delFlag || '0'
      };
      
      console.log('编辑设备表单数据:', this.deviceForm);
      this.deviceDialogVisible = true;
      this.$nextTick(() => this.$refs.deviceForm?.clearValidate());
    },

    submitDeviceForm() {
      this.$refs.deviceForm.validate(async (valid) => {
        if (!valid) return;

        const loading = this.$loading({
          lock: true,
          text: this.isEditDevice ? '正在更新设备...' : '正在添加设备...',
          spinner: 'el-icon-loading',
          background: 'rgba(0, 0, 0, 0.7)'
        });

        try {
          const params = {
            deviceName: this.deviceForm.deviceName.trim(),
            deviceCode: this.deviceForm.deviceCode.trim(),
            deviceType: this.deviceForm.deviceType,
            status: this.deviceForm.status,
            location: this.deviceForm.location.trim(),
            protocolId: this.deviceForm.protocolId || null,
            relayProtocolId: this.deviceForm.relayProtocolId || null,
            delFlag: this.deviceForm.delFlag
          };
          
          // 如果是编辑模式，添加 deviceId
          if (this.isEditDevice) {
            params.deviceId = this.currentDeviceId;
          }
          
          console.log('提交设备参数:', params);

          const response = this.isEditDevice 
            ? await updateDevice(this.getToken, this.currentDeviceId, params)
            : await addDevice(this.getToken, params);

          if (response.data?.code === 200) {
            this.$message.success(this.isEditDevice ? '设备更新成功' : '设备添加成功');
            this.handleDeviceDialogClose();
            await this.loadAreaList();
          } else {
            const errorMsg = response.data?.msg || response.data?.message || 
                           (this.isEditDevice ? '设备更新失败' : '设备添加失败');
            this.$message.error(errorMsg);
          }
        } catch (error) {
          console.error('操作设备出错:', error);
          const errorMsg = error.response?.data?.msg || error.message || "网络错误";
          this.$message.error(`操作失败: ${errorMsg}`);
        } finally {
          loading.close();
        }
      });
    },

    handleDeviceDialogClose() {
      this.deviceDialogVisible = false;
      this.$refs.deviceForm?.resetFields();
    },

    async confirmDeleteDevice(area) {
      try {
        await this.$confirm(
          `确定要删除设备 "${area.deviceName || area.configName}" 吗？此操作不可恢复！`, 
          '删除确认', 
          {
            confirmButtonText: '确定删除',
            cancelButtonText: '取消',
            type: 'warning',
            customClass: 'modern-confirm-dialog'
          }
        );

        const loading = this.$loading({
          lock: true,
          text: '正在删除设备...',
          spinner: 'el-icon-loading',
          background: 'rgba(0, 0, 0, 0.7)'
        });

        try {
          const response = await deleteDevice(this.getToken, area.id);
          if (response.data?.code === 200) {
            this.$message.success('设备删除成功');
            await this.loadAreaList();
          } else {
            const errorMsg = response.data?.msg || response.data?.message || '设备删除失败';
            this.$message.error(errorMsg);
          }
        } catch (error) {
          console.error('删除设备出错:', error);
          this.$message.error(`删除失败: ${error.response?.data?.msg || error.message || "网络错误"}`);
        } finally {
          loading.close();
        }
      } catch {
        // 用户取消删除
      }
    },

    handleDeviceAction(command) {
      const actions = {
        edit: () => this.showEditDeviceDialog(command.data),
        detail: () => this.viewAreaDetail(command.data),
        configProtocol: () => this.showProtocolConfig(command.data),
        deviceDetail: () => this.viewDeviceDetail(command.data),
        delete: () => this.confirmDeleteDevice(command.data)
      };
      actions[command.action]?.();
    }
  },
};
</script>

<style lang="scss" scoped>
.device-management {
  width: 100%;
  height: 100%;
  padding: 24px;
  background: #f5f6f8;
  overflow-y: auto;
  color: #333333;
  
  // 自定义滚动条
  &::-webkit-scrollbar {
    width: 8px;
  }
  
  &::-webkit-scrollbar-track {
    background: #f1f1f1;
    border-radius: 4px;
  }
  
  &::-webkit-scrollbar-thumb {
    background: #c1c1c1;
    border-radius: 4px;
    
    &:hover {
      background: #a8a8a8;
    }
  }
}

.page-header-container {
  margin-bottom: 24px;
  
  .page-header-wrapper {
    background: #ffffff;
    border-radius: 12px;
    padding: 24px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
    border: 1px solid #edf0f5;
    
    .page-header-content {
      .header-main {
        display: flex;
        align-items: center;
        margin-bottom: 12px;
        
        .header-icon {
          font-size: 32px;
          color: #409EFF;
          margin-right: 16px;
        }
        
        .page-title {
          font-size: 28px;
          font-weight: 600;
          color: #2d3037;
          margin: 0;
        }
      }
      
      .page-desc {
        font-size: 16px;
        color: #666666;
        margin: 0 0 20px 0;
      }
      
      .header-actions {
        display: flex;
        gap: 12px;
        
        .main-action-btn {
          border-radius: 8px;
          padding: 10px 20px;
          font-size: 14px;
          transition: all 0.3s ease;
          
          &:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
          }
        }
      }
    }
  }
}

.content-container {
  .content-wrapper {
    min-height: 500px;
    
    .cards-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
      gap: 24px;
      padding: 8px;
    }
    
    .area-card-wrapper {
      transition: all 0.3s ease;
      
      &:hover {
        transform: translateY(-4px);
      }
    }
    
    .area-card {
      background: #ffffff;
      border-radius: 12px;
      overflow: hidden;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
      border: 1px solid #edf0f5;
      transition: all 0.3s ease;
      
      &:hover {
        box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1);
        border-color: #409EFF;
      }
      
      .card-header {
        display: flex;
        align-items: center;
        padding: 20px 20px 16px;
        border-bottom: 1px solid #edf0f5;
        
        .area-icon {
          width: 48px;
          height: 48px;
          background: linear-gradient(135deg, #409EFF, #1a7be0);
          border-radius: 12px;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-right: 16px;
          
          i {
            font-size: 24px;
            color: white;
          }
        }
        
        .area-name {
          margin: 0;
          font-size: 18px;
          font-weight: 600;
          color: #2d3037;
          flex: 1;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
        
        .device-actions {
          .action-dropdown-btn {
            background: #f0f8ff;
            border: 1px solid #409EFF;
            color: #409EFF;
            border-radius: 8px;
            padding: 6px 12px;
            font-size: 13px;
            transition: all 0.3s ease;
            
            &:hover {
              background: #409EFF;
              color: white;
            }
          }
        }
      }
      
      .card-content {
        padding: 16px 20px;
        cursor: pointer;
        transition: background-color 0.2s;
        
        &:hover {
          background-color: #f9fbfd;
        }
        
        .area-details {
          .detail-item {
            display: flex;
            margin-bottom: 14px;
            
            &:last-child {
              margin-bottom: 0;
            }
            
            .detail-label {
              width: 80px;
              font-size: 14px;
              color: #666666;
              flex-shrink: 0;
            }
            
            .detail-value {
              flex: 1;
              font-size: 14px;
              color: #2d3037;
              word-break: break-all;
            }
            
            .status-badge {
              padding: 4px 12px;
              border-radius: 20px;
              font-size: 12px;
              font-weight: 500;
              
              &.status-online {
                background: #e8f7f0;
                color: #0ac185;
                border: 1px solid #0ac185;
              }
              
              &.status-offline {
                background: #fff0f0;
                color: #f56c6c;
                border: 1px solid #f56c6c;
              }
            }
          }
        }
      }
    }
    
    .empty-state,
    .error-state {
      display: flex;
      justify-content: center;
      align-items: center;
      height: 400px;
      
      .empty-content,
      .error-content {
        text-align: center;
        padding: 40px;
        background: #ffffff;
        border-radius: 12px;
        border: 1px dashed #d9d9d9;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
        
        .empty-icon,
        .error-icon {
          font-size: 64px;
          margin-bottom: 20px;
        }
        
        .empty-icon {
          color: #409EFF;
        }
        
        .error-icon {
          color: #F56C6C;
        }
        
        .empty-text,
        .error-text {
          font-size: 18px;
          color: #666666;
          margin-bottom: 24px;
        }
        
        .refresh-btn,
        .retry-btn {
          background: linear-gradient(135deg, #409EFF, #1a7be0);
          border: none;
          border-radius: 8px;
          padding: 12px 28px;
          color: white;
          font-size: 16px;
          transition: all 0.3s ease;
          
          &:hover {
            background: linear-gradient(135deg, #66b1ff, #409EFF);
            transform: scale(1.05);
            box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
          }
        }
      }
    }
  }
}

// 现代化对话框样式
.modern-dialog {
  border-radius: 12px;
  overflow: hidden;
  
  ::v-deep .el-dialog {
    border-radius: 12px;
    background: #ffffff;
    border: 1px solid #edf0f5;
    box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
    
    .el-dialog__header {
      background: #f9fbfd;
      padding: 20px 24px;
      border-bottom: 1px solid #edf0f5;
      
      .el-dialog__title {
        color: #2d3037;
        font-size: 20px;
        font-weight: 600;
      }
      
      .el-dialog__headerbtn {
        .el-icon-close {
          color: #909399;
          font-size: 20px;
          
          &:hover {
            color: #333333;
            background: #f0f0f0;
            border-radius: 50%;
          }
        }
      }
    }
    
    .el-dialog__body {
      padding: 28px 24px;
    }
    
    .el-dialog__footer {
      padding: 16px 24px;
      background: #f9fbfd;
      border-top: 1px solid #edf0f5;
      text-align: right;
    }
  }
  
  .protocol-form,
  .device-form {
    .el-form-item {
      margin-bottom: 22px;
      
      .el-form-item__label {
        color: #555555;
        font-weight: 500;
        font-size: 14px;
      }
      
      .form-value {
        color: #2d3037;
        font-size: 14px;
        font-weight: 500;
      }
      
      .protocol-type-tag {
        font-weight: 500;
      }
      
      .status-tag {
        font-weight: 500;
      }
    }
  }
}

// 现代化表单元素
.modern-form {
  .modern-input {
    ::v-deep .el-input__inner {
      border-radius: 8px;
      border: 1px solid #dcdfe6;
      padding: 12px 15px;
      transition: all 0.3s ease;
      
      &:focus {
        border-color: #409EFF;
        box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.1);
      }
    }
  }
  
  .modern-select {
    ::v-deep .el-input__inner {
      border-radius: 8px;
      border: 1px solid #dcdfe6;
      padding: 12px 15px;
      transition: all 0.3s ease;
      
      &:focus {
        border-color: #409EFF;
        box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.1);
      }
    }
  }
  
  .modern-radio-group {
    ::v-deep .el-radio {
      margin-right: 20px;
      
      .el-radio__input.is-checked + .el-radio__label {
        color: #409EFF;
      }
      
      .el-radio__input.is-checked .el-radio__inner {
        border-color: #409EFF;
        background: #409EFF;
      }
    }
  }
}

// 操作按钮样式
.cancel-btn {
  background: #f5f7fa;
  border: 1px solid #dcdfe6;
  color: #606266;
  border-radius: 8px;
  padding: 9px 20px;
  font-size: 14px;
  transition: all 0.3s ease;
  
  &:hover {
    background: #f0f0f0;
    border-color: #c0c4cc;
    color: #333333;
  }
}

.confirm-btn {
  background: linear-gradient(135deg, #409EFF, #1a7be0);
  border: none;
  color: white;
  border-radius: 8px;
  padding: 9px 24px;
  font-size: 14px;
  transition: all 0.3s ease;
  
  &:hover {
    background: linear-gradient(135deg, #66b1ff, #409EFF);
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
  }
}

.close-btn {
  background: #f5f7fa;
  border: 1px solid #dcdfe6;
  color: #606266;
  border-radius: 8px;
  padding: 9px 20px;
  transition: all 0.3s ease;
  
  &:hover {
    background: #f0f0f0;
    border-color: #c0c4cc;
    color: #333333;
  }
}

// 协议配置对话框样式
::v-deep .protocol-dialog {
  .el-dialog {
    border-radius: 12px;
    box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
  }
  
  .protocol-dialog-content {
    min-height: 400px;
  }
  
  // 已选择协议展示区域
  .selected-protocol-display {
    background: linear-gradient(135deg, #f6f9fc 0%, #ffffff 100%);
    border: 2px solid #e8f4ff;
    border-radius: 12px;
    padding: 20px;
    margin-bottom: 24px;
    animation: slideDown 0.3s ease;
    
    .display-header {
      display: flex;
      align-items: center;
      margin-bottom: 16px;
      color: #409EFF;
      font-weight: 600;
      font-size: 15px;
      
      i {
        margin-right: 8px;
        font-size: 18px;
      }
    }
    
    .display-content {
      display: flex;
      flex-wrap: wrap;
      gap: 16px;
      
      .protocol-display-card {
        flex: 1;
        min-width: 280px;
        background: #ffffff;
        border-radius: 10px;
        padding: 16px;
        display: flex;
        gap: 12px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
        transition: all 0.3s ease;
        
        &:hover {
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
          transform: translateY(-2px);
        }
        
        &.mqtt {
          border-left: 4px solid #409EFF;
        }
        
        &.tcp {
          border-left: 4px solid #E6A23C;
        }
        
        .card-icon {
          width: 48px;
          height: 48px;
          border-radius: 10px;
          display: flex;
          align-items: center;
          justify-content: center;
          flex-shrink: 0;
          
          i {
            font-size: 24px;
          }
        }
        
        &.mqtt .card-icon {
          background: linear-gradient(135deg, #409EFF, #66b1ff);
          color: white;
        }
        
        &.tcp .card-icon {
          background: linear-gradient(135deg, #E6A23C, #f0b254);
          color: white;
        }
        
        .card-info {
          flex: 1;
          min-width: 0;
          
          .card-title {
            display: flex;
            align-items: center;
            gap: 8px;
            margin-bottom: 6px;
            
            .protocol-name {
              font-size: 16px;
              font-weight: 600;
              color: #2d3037;
              overflow: hidden;
              text-overflow: ellipsis;
              white-space: nowrap;
            }
          }
          
          .card-desc {
            font-size: 13px;
            color: #909399;
            margin-bottom: 8px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }
          
          .card-config {
            display: flex;
            flex-wrap: wrap;
            gap: 12px;
            
            .config-item {
              display: inline-flex;
              align-items: center;
              gap: 4px;
              font-size: 12px;
              color: #606266;
              background: #f5f7fa;
              padding: 4px 10px;
              border-radius: 4px;
              
              i {
                font-size: 12px;
                color: #909399;
              }
            }
          }
        }
        
        .card-actions {
          display: flex;
          align-items: flex-start;
          
          .remove-btn {
            color: #f56c6c;
            padding: 4px 8px;
            
            &:hover {
              background: #fef0f0;
              color: #f56c6c;
            }
          }
        }
      }
    }
  }
  
  // 空状态提示
  .no-selection-hint {
    background: #f8f9fa;
    border: 2px dashed #dcdfe6;
    border-radius: 8px;
    padding: 20px;
    margin-bottom: 24px;
    text-align: center;
    color: #909399;
    font-size: 14px;
    
    i {
      margin-right: 8px;
      font-size: 16px;
    }
  }
  
  @keyframes slideDown {
    from {
      opacity: 0;
      transform: translateY(-10px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }
  
  .protocol-tabs {
    .el-tabs__header {
      margin-bottom: 20px;
      
      .el-tabs__nav {
        border: 1px solid #e4e7ed;
        border-radius: 8px;
        overflow: hidden;
      }
      
      .el-tabs__item {
        height: 44px;
        line-height: 44px;
        padding: 0 30px;
        font-size: 15px;
        font-weight: 500;
        color: #606266;
        transition: all 0.3s ease;
        border-right: 1px solid #e4e7ed;
        
        &:last-child {
          border-right: none;
        }
        
        &:hover {
          color: #409EFF;
          background: #f5f7fa;
        }
        
        &.is-active {
          color: #ffffff;
          background: linear-gradient(135deg, #409EFF, #66b1ff);
        }
      }
    }
    
    .el-tabs__content {
      max-height: 500px;
      overflow-y: auto;
      padding: 0 10px;
      
      // 自定义滚动条
      &::-webkit-scrollbar {
        width: 6px;
      }
      
      &::-webkit-scrollbar-track {
        background: #f1f1f1;
        border-radius: 3px;
      }
      
      &::-webkit-scrollbar-thumb {
        background: #c1c1c1;
        border-radius: 3px;
        
        &:hover {
          background: #a8a8a8;
        }
      }
    }
  }
  
  .protocol-form {
    .el-divider__text {
      background-color: #fff;
      color: #409EFF;
      font-weight: 600;
      font-size: 14px;
    }
    
    .form-value {
      color: #606266;
      font-size: 14px;
    }
  }
  
  // 协议选择界面样式
  .protocol-tab-content {
    .protocol-list-container {
      .section-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 20px;
        padding-bottom: 12px;
        border-bottom: 2px solid #f0f2f5;
        
        h4 {
          margin: 0;
          color: #2d3037;
          font-size: 18px;
          font-weight: 600;
        }
      }
      
      .protocol-grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
        gap: 16px;
        max-height: 500px;
        overflow-y: auto;
        padding-right: 8px;
        
        // 自定义滚动条
        &::-webkit-scrollbar {
          width: 6px;
        }
        
        &::-webkit-scrollbar-track {
          background: #f1f1f1;
          border-radius: 3px;
        }
        
        &::-webkit-scrollbar-thumb {
          background: #c1c1c1;
          border-radius: 3px;
          
          &:hover {
            background: #a8a8a8;
          }
        }
      }
      
      .protocol-card {
        background: #ffffff;
        border: 2px solid #e4e7ed;
        border-radius: 12px;
        padding: 16px;
        transition: all 0.3s ease;
        cursor: pointer;
        
        &:hover {
          border-color: #409EFF;
          box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
          transform: translateY(-2px);
        }
        
        &.selected {
          border-color: #409EFF;
          background: #f0f8ff;
          box-shadow: 0 4px 16px rgba(64, 158, 255, 0.2);
        }
        
        .protocol-header {
          display: flex;
          justify-content: space-between;
          align-items: flex-start;
          margin-bottom: 12px;
          
          .protocol-title {
            display: flex;
            align-items: center;
            font-size: 16px;
            font-weight: 600;
            color: #2d3037;
            
            i {
              margin-right: 8px;
              font-size: 18px;
              color: #409EFF;
            }
          }
          
          .protocol-badges {
            display: flex;
            gap: 6px;
          }
        }
        
        .protocol-body {
          margin-bottom: 16px;
          
          .protocol-info-row {
            display: flex;
            margin-bottom: 8px;
            
            &:last-child {
              margin-bottom: 0;
            }
            
            .label {
              width: 60px;
              font-size: 13px;
              color: #909399;
              flex-shrink: 0;
            }
            
            .value {
              flex: 1;
              font-size: 13px;
              color: #606266;
              word-break: break-all;
            }
          }
          
          .protocol-config {
            background: #f8f9fa;
            border-radius: 6px;
            padding: 10px;
            margin-top: 8px;
          }
        }
        
        .protocol-actions {
          text-align: center;
          
          .el-button {
            width: 100%;
          }
        }
      }
      
      .empty-protocol {
        grid-column: 1 / -1;
        
        .empty-content {
          text-align: center;
          padding: 60px 20px;
          background: #fafafa;
          border: 2px dashed #d9d9d9;
          border-radius: 12px;
          
          i {
            font-size: 48px;
            color: #c0c4cc;
            margin-bottom: 16px;
          }
          
          p {
            margin: 0 0 20px;
            color: #909399;
            font-size: 16px;
          }
        }
      }
    }
  }
  
  // 对话框底部样式
  .dialog-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .footer-info {
      flex: 1;
      
      .no-selection {
        color: #909399;
        font-size: 14px;
      }
      
      .el-tag {
        margin-right: 8px;
      }
    }
    
    .footer-actions {
      display: flex;
      gap: 12px;
    }
  }
}

// 响应式设计
@media (max-width: 1200px) {
  .cards-grid {
    grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)) !important;
  }
}

@media (max-width: 768px) {
  .device-management {
    padding: 16px;
  }
  
  .page-header-container {
    margin-bottom: 16px;
    
    .page-header-wrapper {
      padding: 20px;
      
      .page-header-content {
        .header-main {
          .header-icon {
            font-size: 28px;
            margin-right: 12px;
          }
          
          .page-title {
            font-size: 24px;
          }
        }
        
        .page-desc {
          font-size: 15px;
          margin-bottom: 16px;
        }
      }
    }
  }
  
  .content-container {
    .content-wrapper {
      .cards-grid {
        grid-template-columns: 1fr !important;
        gap: 16px;
      }
    }
  }
  
  .area-card {
    .card-header {
      padding: 16px;
      
      .area-icon {
        width: 42px;
        height: 42px;
        
        i {
          font-size: 20px;
        }
      }
      
      .area-name {
        font-size: 16px;
      }
    }
    
    .card-content {
      padding: 16px;
    }
  }
}
</style>