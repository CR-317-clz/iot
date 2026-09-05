<template>
  <div class="camera-management">
    <div class="camera-container">
      <!-- 顶部标题栏 -->
      <div class="page-header">
        <div class="header-content">
          <div class="header-left">
            <span class="page-title">
              <i class="el-icon-video-camera"></i>
              摄像头管理
            </span>
            <p class="page-subtitle">管理系统中的摄像头设备信息</p>
          </div>
          <div class="header-actions">
            <el-button 
              class="add-btn" 
              type="primary" 
              icon="el-icon-plus" 
              @click="handleAdd"
            >
              新增摄像头
            </el-button>
            <el-button 
              class="refresh-btn" 
              type="primary" 
              icon="el-icon-refresh" 
              @click="refreshCameraList"
            >
              刷新列表
            </el-button>
          </div>
        </div>
      </div>

      <!-- 筛选和搜索区域 -->
      <div class="filter-search-container">
        <div class="camera-filter-container">
          <div class="filter-label">状态:</div>
          <div class="filter-buttons">
            <el-button 
              v-for="filter in statusFilters"
              :key="filter.value"
              :type="statusFilter === filter.value ? 'primary' : ''" 
              @click="filterByStatus(filter.value)"
              size="small"
            >
              {{ filter.label }}
            </el-button>
          </div>
        </div>

        <!-- 搜索区域 -->
        <div class="search-container">
          <el-input
            v-model="search"
            placeholder="请输入摄像头名称或位置进行搜索"
            clearable
            class="search-input"
            @keyup.enter.native="handleSearch"
          >
            <i slot="prefix" class="el-input__icon el-icon-search"></i>
          </el-input>
          <el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button>
        </div>
      </div>

      <!-- 摄像头卡片区域 -->
      <div class="cards-container">
        <el-row :gutter="24">
          <el-col 
            v-for="camera in filteredCameras" 
            :key="camera.cameraId" 
            :xs="24" 
            :sm="12" 
            :md="12" 
            :lg="8" 
            :xl="8"
            class="card-col"
          >
            <el-card class="camera-card" shadow="hover">
              <div slot="header" class="card-header">
                <div class="card-header-content">
                  <span class="camera-name">{{ camera.cameraName || '-' }}</span>
                  <el-tag 
                    :type="getStatusType(camera.status)" 
                    size="mini"
                    class="status-tag"
                  >
                    {{ getStatusText(camera.status) }}
                  </el-tag>
                </div>
                <div class="camera-location">{{ camera.location || '-' }}</div>
              </div>
              
              <div class="card-body">
                <div class="camera-preview" @click="handleView(camera)">
                  <div class="preview-wrapper">
                    <img 
                      :src="camera.previewUrl || 'https://img2.baidu.com/it/u=1815681536,3276907896&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=300'" 
                      :alt="camera.cameraName"
                      class="preview-image"
                      @error="handleImageError"
                    >
                    <div class="preview-overlay">
                      <div class="overlay-content">
                        <i class="el-icon-video-play"></i>
                        <span>点击查看实时画面</span>
                      </div>
                    </div>
                    <div class="preview-status" :class="`status-${camera.status}`">
                      <span class="status-dot"></span>
                      <span class="status-text">{{ getStatusText(camera.status) }}</span>
                    </div>
                  </div>
                </div>
                
                <div 
                  v-for="item in getCameraInfoItems(camera)" 
                  :key="item.label" 
                  class="info-item"
                >
                  <div class="info-label">{{ item.label }}：</div>
                  <div class="info-value" :class="item.class">{{ item.value }}</div>
                </div>
              </div>
              
              <div class="card-footer">
                <el-button 
                  type="primary" 
                  size="mini" 
                  @click="handleView(camera)"
                  icon="el-icon-view"
                  plain
                >
                  查看
                </el-button>
                <el-button 
                  type="primary" 
                  size="mini" 
                  @click="handleEdit(camera)"
                  icon="el-icon-edit"
                  plain
                >
                  编辑
                </el-button>
                <el-button 
                  type="danger" 
                  size="mini" 
                  @click="handleDelete(camera)"
                  icon="el-icon-delete"
                  plain
                >
                  删除
                </el-button>
              </div>
            </el-card>
          </el-col>
          
          <el-col v-if="filteredCameras.length === 0" :span="24">
            <div class="no-data">
              <i class="el-icon-video-camera"></i>
              <p>{{ loading ? '数据加载中...' : '暂无摄像头数据' }}</p>
              <el-button v-if="!loading" type="primary" @click="handleAdd" icon="el-icon-plus">新增摄像头</el-button>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 编辑对话框 -->
      <el-dialog
        :visible.sync="dialogFormVisible"
        :title="dialogTitle"
        width="650px"
        @close="handleDialogClose"
        class="camera-dialog"
        :modal-append-to-body="false"
      >
        <el-form 
          :model="cameraFormData" 
          :rules="formRules" 
          ref="cameraForm" 
          label-width="120px"
          class="camera-form"
        >
          <el-form-item 
            v-for="field in formFields" 
            :key="field.prop"
            :label="field.label" 
            :prop="field.prop"
          >
            <!-- 文本输入框 -->
            <el-input
              v-if="field.type === 'input'"
              v-model="cameraFormData[field.prop]"
              :placeholder="field.placeholder"
              clearable
              class="form-input"
            ></el-input>
            
            <!-- 数字输入框 -->
            <el-input
              v-else-if="field.type === 'number'"
              v-model="cameraFormData[field.prop]"
              type="number"
              :placeholder="field.placeholder"
              clearable
              class="form-input"
            ></el-input>
            
            <!-- 单选按钮组 -->
            <el-radio-group 
              v-else-if="field.type === 'radio'" 
              v-model="cameraFormData[field.prop]" 
              class="form-radio-group"
            >
              <el-radio 
                v-for="option in field.options" 
                :key="option.label"
                :label="option.label"
              >
                {{ option.text }}
              </el-radio>
            </el-radio-group>
            
            <!-- 文本域 -->
            <el-input
              v-else-if="field.type === 'textarea'"
              v-model="cameraFormData[field.prop]"
              type="textarea"
              :rows="field.rows || 2"
              :placeholder="field.placeholder"
              clearable
              class="form-textarea"
            ></el-input>
          </el-form-item>
        </el-form>

        <template #footer>
          <div class="dialog-footer">
            <el-button @click="handleDialogClose" class="cancel-btn">取消</el-button>
            <el-button type="primary" @click="handleConfirm" class="confirm-btn">确定</el-button>
          </div>
        </template>
      </el-dialog>

      <!-- 视频查看对话框 -->
      <el-dialog
        :visible.sync="videoDialogVisible"
        :title="currentCamera ? `${currentCamera.cameraName} - 实时监控` : '视频监控'"
        width="900px"
        @close="handleVideoDialogClose"
        class="video-dialog"
        :modal-append-to-body="false"
      >
        <div class="video-container" v-if="currentCamera">
          <div class="video-info-bar">
            <div class="info-group">
              <span class="info-label">位置：</span>
              <span class="info-text">{{ currentCamera.location || '-' }}</span>
            </div>
            <div class="info-group">
              <span class="info-label">IP地址：</span>
              <span class="info-text">{{ currentCamera.ipAddress || '-' }}</span>
            </div>
            <div class="info-group">
              <span class="info-label">状态：</span>
              <el-tag 
                :type="getStatusType(currentCamera.status)" 
                size="mini"
              >
                {{ getStatusText(currentCamera.status) }}
              </el-tag>
            </div>
            <div class="info-group">
              <span class="info-label">WebRTC服务器：</span>
              <el-input 
                v-model="webRtcServerUrl" 
                size="mini" 
                style="width: 220px;"
                placeholder="http://192.168.110.137:8000"
              ></el-input>
              <el-button 
                type="primary" 
                size="mini" 
                @click="reconnectVideo"
                icon="el-icon-refresh"
              >
                重新连接
              </el-button>
            </div>
          </div>
          
          <div class="video-player-wrapper">
            <div v-if="currentCamera.status === 'online'" class="video-player">
              <!-- WebRTC 视频播放器容器 -->
              <video 
                v-if="currentCamera.streamUrl"
                ref="videoPlayer"
                class="video-element"
                preload="auto"
                autoplay
                muted
                playsinline
                controls
                @dblclick="handleVideoDoubleClick"
                style="object-fit: cover; width: 100%; height: 100%;"
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

          <div class="video-details">
            <el-descriptions :column="2" border>
              <el-descriptions-item 
                v-for="field in videoDetailFields" 
                :key="field.prop"
                :label="field.label"
                :span="field.span"
              >
                <span :class="field.class">
                  {{ currentCamera[field.prop] || '-' }}
                </span>
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </div>

        <template #footer>
          <div class="dialog-footer">
            <el-button @click="handleVideoDialogClose" class="cancel-btn">关闭</el-button>
            <el-button type="primary" @click="handleEdit(currentCamera)" icon="el-icon-edit" class="confirm-btn">
              编辑信息
            </el-button>
          </div>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import WebRtcStreamer from '@/public/webrtcstreamer'
import { getAllCameraList, addCamera, updateCamera, deleteCamera } from '@/api/iot-camera';

// 常量定义
const STATUS = {
  ONLINE: 'online',
  OFFLINE: 'offline'
};

const STATUS_TEXT = {
  [STATUS.ONLINE]: '在线',
  [STATUS.OFFLINE]: '离线'
};

const STATUS_TYPE = {
  [STATUS.ONLINE]: 'success',
  [STATUS.OFFLINE]: 'danger'
};

const DEFAULT_CAMERA_FORM = {
  cameraId: undefined,
  cameraName: '',
  cameraCode: '',
  cameraType: '',
  location: '',
  ipAddress: '',
  port: 554,
  streamUrl: '',
  previewUrl: '',
  status: STATUS.ONLINE,
  manufacturer: '',
  model: '',
  resolution: '',
  remark: '',
  createTime: ''
};

// 表单字段配置
const FORM_FIELDS = [
  { label: '摄像头名称', prop: 'cameraName', placeholder: '请输入摄像头名称', type: 'input' },
  { label: '摄像头编号', prop: 'cameraCode', placeholder: '请输入摄像头编号', type: 'input' },
  { label: '摄像头类型', prop: 'cameraType', placeholder: '请输入摄像头类型', type: 'input' },
  { label: '位置描述', prop: 'location', placeholder: '请输入摄像头位置描述', type: 'input' },
  { label: 'IP地址', prop: 'ipAddress', placeholder: '请输入摄像头IP地址', type: 'input' },
  { label: '端口', prop: 'port', placeholder: '请输入端口号', type: 'number' },
  { label: 'RTSP地址', prop: 'streamUrl', placeholder: '请输入RTSP地址', type: 'input' },
  { label: '预览图片', prop: 'previewUrl', placeholder: '请输入预览图片URL', type: 'input' },
  { label: '状态', prop: 'status', type: 'radio', options: [
    { label: 'online', text: '在线' },
    { label: 'offline', text: '离线' }
  ]},
  { label: '厂商', prop: 'manufacturer', placeholder: '请输入厂商名称', type: 'input' },
  { label: '型号', prop: 'model', placeholder: '请输入设备型号', type: 'input' },
  { label: '分辨率', prop: 'resolution', placeholder: '请输入分辨率', type: 'input' },
  { label: '备注', prop: 'remark', placeholder: '请输入备注信息', type: 'textarea', rows: 2 }
];

// 视频详情字段配置
const VIDEO_DETAIL_FIELDS = [
  { label: '摄像头名称', prop: 'cameraName', span: 1 },
  { label: '摄像头编号', prop: 'cameraCode', span: 1 },
  { label: '厂商', prop: 'manufacturer', span: 1 },
  { label: '型号', prop: 'model', span: 1 },
  { label: '分辨率', prop: 'resolution', span: 1 },
  { label: '端口', prop: 'port', span: 1 },
  { label: 'RTSP地址', prop: 'streamUrl', span: 2, class: 'rtsp-text' }
];

// 状态筛选按钮配置
const STATUS_FILTERS = [
  { value: '', label: '全部' },
  { value: STATUS.ONLINE, label: STATUS_TEXT[STATUS.ONLINE] },
  { value: STATUS.OFFLINE, label: STATUS_TEXT[STATUS.OFFLINE] }
];

export default {
  name: "Camera",
  data() {
    return {
      // 搜索和筛选
      search: "",
      statusFilter: "",
      statusFilters: STATUS_FILTERS,
      
      // 摄像头列表
      cameras: [],
      loading: false,
      
      // 编辑对话框
      dialogFormVisible: false,
      dialogTitle: "编辑摄像头信息",
      isEdit: false,
      
      // 视频查看对话框
      videoDialogVisible: false,
      currentCamera: null,
      webRtcServer: null,
      webRtcServerUrl: 'http://192.168.110.137:8000',
      
      // 表单字段配置
      formFields: FORM_FIELDS,
      
      // 视频详情字段配置
      videoDetailFields: VIDEO_DETAIL_FIELDS,
      
      // 表单数据和验证规则
      cameraFormData: { ...DEFAULT_CAMERA_FORM },
      formRules: {
        cameraName: [
          { required: true, message: '请输入摄像头名称', trigger: 'blur' }
        ],
        location: [
          { required: true, message: '请输入位置描述', trigger: 'blur' }
        ],
        ipAddress: [
          { required: true, message: '请输入IP地址', trigger: 'blur' }
        ]
      }
    };
  },
  computed: {
    /**
     * 过滤后的摄像头列表
     */
    filteredCameras() {
      return this.cameras.filter(camera => {
        const matchesStatus = !this.statusFilter || camera.status === this.statusFilter;
        const matchesSearch = !this.search || 
          this.matchesKeyword(camera.cameraName, this.search.toLowerCase()) ||
          this.matchesKeyword(camera.location, this.search.toLowerCase());
        return matchesStatus && matchesSearch;
      });
    }
  },
  
  created() {
    this.loadCameraList();
  },

  beforeCreate() {
    window.onbeforeunload = () => {
      if (this.webRtcServer) {
        this.webRtcServer.disconnect();
      }
    };
  },

  beforeDestroy() {
    this.cleanup();
  },

  methods: {
    // ============ 工具方法 ============
    
    /**
     * 检查字符串是否匹配关键词
     */
    matchesKeyword(str, keyword) {
      return str && str.toLowerCase().includes(keyword);
    },
    
    /**
     * 格式化时间
     */
    formatTime(dateTimeArray) {
      if (!dateTimeArray || !Array.isArray(dateTimeArray)) return '-';
      
      try {
        const [year, month, day, hour, minute, second] = dateTimeArray;
        return new Date(year, month - 1, day, hour, minute, second).toLocaleString('zh-CN');
      } catch (error) {
        console.error('时间格式化失败:', error);
        return '-';
      }
    },
    
    /**
     * 重置表单数据
     */
    resetFormData() {
      this.cameraFormData = { ...DEFAULT_CAMERA_FORM };
    },
    
    /**
     * 清理资源
     */
    cleanup() {
      this.disconnectWebRTC();
      if (typeof window !== 'undefined') {
        window.onbeforeunload = null;
      }
    },
    
    /**
     * 获取状态文本
     */
    getStatusText(status) {
      return STATUS_TEXT[status] || '-';
    },
    
    /**
     * 获取状态类型
     */
    getStatusType(status) {
      return STATUS_TYPE[status] || 'info';
    },
    
    /**
     * 获取摄像头信息项列表（用于模板渲染）
     */
    getCameraInfoItems(camera) {
      return [
        {
          label: '摄像头类型',
          value: camera.cameraType || '-'
        },
        {
          label: 'IP地址',
          value: camera.ipAddress || '-'
        },
        {
          label: '厂商',
          value: camera.manufacturer || '-'
        },
        {
          label: '型号',
          value: camera.model || '-'
        },
        {
          label: 'RTSP地址',
          value: camera.streamUrl || '-',
          class: 'rtsp-url'
        },
        {
          label: '创建时间',
          value: this.formatTime(camera.createTime)
        }
      ];
    },
    
    // ============ 数据加载 ============
    
    /**
     * 加载摄像头列表
     */
    async loadCameraList() {
      this.loading = true;
      try {
        const response = await getAllCameraList();
        if (response.data.code === 200) {
          this.cameras = response.data.rows || [];
        } else {
          this.$message.error(response.data.msg || '获取摄像头列表失败');
        }
      } catch (error) {
        console.error('加载摄像头列表失败:', error);
        this.$message.error('获取摄像头列表失败: ' + (error.message || '网络错误'));
      } finally {
        this.loading = false;
      }
    },

    /**
     * 刷新列表
     */
    refreshCameraList() {
      this.search = "";
      this.statusFilter = "";
      this.loadCameraList();
      this.$message.success("摄像头列表已刷新");
    },

    // ============ 搜索和筛选 ============
    
    /**
     * 搜索处理（由计算属性实现）
     */
    handleSearch() {
      // 搜索逻辑已在 computed 中实现
    },

    /**
     * 根据状态筛选
     */
    filterByStatus(status) {
      this.statusFilter = status;
    },

    // ============ CRUD 操作 ============
    
    /**
     * 新增摄像头
     */
    handleAdd() {
      this.isEdit = false;
      this.dialogTitle = "新增摄像头";
      this.resetFormData();
      this.dialogFormVisible = true;
    },

    /**
     * 编辑摄像头
     */
    handleEdit(row) {
      this.isEdit = true;
      this.dialogTitle = "编辑摄像头信息";
      this.cameraFormData = { ...row };
      this.dialogFormVisible = true;
    },

    /**
     * 删除摄像头
     */
    handleDelete(row) {
      this.$confirm(
        `确定要删除摄像头 "${row.cameraName}" 吗？`, 
        '提示', 
        {
          type: 'warning',
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          confirmButtonClass: 'el-button--danger'
        }
      ).then(async () => {
        try {
          const response = await deleteCamera(row.cameraId);
          if (response.data.code === 200) {
            this.$message.success('删除成功');
            this.loadCameraList();
          } else {
            this.$message.error(response.data.msg || '删除失败');
          }
        } catch (error) {
          console.error('删除摄像头失败:', error);
          this.$message.error('删除失败: ' + (error.message || '网络错误'));
        }
      }).catch(() => {
        this.$message.info('已取消删除');
      });
    },

    /**
     * 确认保存（新增或编辑）
     */
    handleConfirm() {
      this.$refs.cameraForm.validate(async (valid) => {
        if (!valid) {
          this.$message.warning("请填写必填项");
          return false;
        }

        try {
          const requestData = { ...this.cameraFormData };
          let response;
          
          if (this.isEdit) {
            response = await updateCamera(this.cameraFormData.cameraId, requestData);
          } else {
            delete requestData.cameraId;
            response = await addCamera(requestData);
          }
          
          if (response.data.code === 200) {
            const successMsg = this.isEdit ? "摄像头信息修改成功" : "摄像头新增成功";
            this.$message.success(successMsg);
            this.dialogFormVisible = false;
            this.loadCameraList();
          } else {
            const errorMsg = this.isEdit ? "摄像头信息修改失败" : "摄像头新增失败";
            this.$message.error(response.data.msg || errorMsg);
          }
        } catch (error) {
          const errorMsg = this.isEdit ? "摄像头信息修改失败" : "摄像头新增失败";
          console.error(errorMsg + ':', error);
          this.$message.error(errorMsg + ": " + (error.message || "网络错误"));
        }
      });
    },

    // ============ 对话框处理 ============
    
    /**
     * 关闭编辑对话框
     */
    handleDialogClose() {
      this.dialogFormVisible = false;
      this.$refs.cameraForm && this.$refs.cameraForm.resetFields();
      this.resetFormData();
    },

    // ============ 视频查看功能 ============
    
    /**
     * 查看摄像头视频
     */
    handleView(row) {
      console.log('===== 查看摄像头视频 =====');
      console.log('摄像头名称:', row.cameraName);
      console.log('RTSP 地址:', row.streamUrl);
      
      this.currentCamera = row;
      this.videoDialogVisible = true;
      
      // 等待对话框渲染完成后初始化视频
      this.$nextTick(() => {
        if (row.streamUrl && row.status === STATUS.ONLINE) {
          this.initVideo();
        } else {
          this.showVideoWarnings(row);
        }
      });
    },
    
    /**
     * 显示视频警告信息
     */
    showVideoWarnings(camera) {
      if (!camera.streamUrl) {
        this.$message.warning('该摄像头未配置 RTSP 地址');
      }
      if (camera.status !== STATUS.ONLINE) {
        this.$message.warning('该摄像头当前离线');
      }
    },

    /**
     * 关闭视频查看对话框
     */
    handleVideoDialogClose() {
      this.videoDialogVisible = false;
      if (this.webRtcServer) {
        this.webRtcServer.disconnect();
      }
      this.currentCamera = null;
    },

    /**
     * 处理图片加载错误 - 生成占位图
     */
    handleImageError(e) {
      try {
        const canvas = document.createElement('canvas');
        canvas.width = 500;
        canvas.height = 300;
        const ctx = canvas.getContext('2d');
        
        // 绘制渐变背景
        const gradient = ctx.createLinearGradient(0, 0, 500, 300);
        gradient.addColorStop(0, '#34495e');
        gradient.addColorStop(1, '#2c3e50');
        ctx.fillStyle = gradient;
        ctx.fillRect(0, 0, 500, 300);
        
        // 绘制图标
        ctx.fillStyle = '#7f8c8d';
        ctx.font = 'bold 80px Arial';
        ctx.textAlign = 'center';
        ctx.textBaseline = 'middle';
        ctx.fillText('📷', 250, 120);
        
        // 绘制文字
        ctx.fillStyle = '#bdc3c7';
        ctx.font = 'bold 20px Arial';
        ctx.fillText('暂无预览图', 250, 200);
        
        ctx.font = '14px Arial';
        ctx.fillStyle = '#95a5a6';
        ctx.fillText('No Preview Available', 250, 230);
        
        e.target.src = canvas.toDataURL('image/png');
      } catch (error) {
        console.error('生成占位图失败:', error);
      }
    },

    // ============ WebRTC 视频流处理 ============
    
    /**
     * 初始化视频（参考 videoPart.vue）
     */
    initVideo() {
      try {
        // 连接前先断开
        if (this.webRtcServer) {
          this.webRtcServer.disconnect();
        }
        
        // 连接 WebRTC-streamer 服务
        this.webRtcServer = new WebRtcStreamer(
          this.$refs.videoPlayer,
          this.webRtcServerUrl
        );
        
        console.log('RTSP地址:', this.currentCamera.streamUrl);
        
        // 向后端发送rtsp地址，强制使用TCP拉流，兼容性更好
        this.webRtcServer.connect(
          this.currentCamera.streamUrl, 
          null, 
          'rtsp_transport=tcp'
        );
        
        this.$message.success('正在连接视频流...');
      } catch (error) {
        console.error('视频初始化失败:', error);
        this.$message.error('视频流初始化失败');
      }
    },
    
    /**
     * 断开 WebRTC 连接
     */
    disconnectWebRTC() {
      if (this.webRtcServer) {
        try {
          this.webRtcServer.disconnect();
          this.webRtcServer = null;
        } catch (error) {
          console.error('断开连接失败:', error);
        }
      }
    },

    /**
     * 重新连接视频
     */
    reconnectVideo() {
      if (!this.currentCamera || !this.currentCamera.streamUrl) {
        this.$message.warning('无法重新连接：缺少视频流地址');
        return;
      }
      
      this.$message.info('正在重新连接...');
      this.initVideo();
    },

    /**
     * 处理视频双击全屏
     */
    handleVideoDoubleClick() {
      const videoElement = this.$refs.videoPlayer;
      if (!videoElement) return;

      try {
        if (videoElement.webkitRequestFullScreen) {
          videoElement.webkitRequestFullScreen();
        } else if (videoElement.mozRequestFullScreen) {
          videoElement.mozRequestFullScreen();
        } else if (videoElement.requestFullscreen) {
          videoElement.requestFullscreen();
        }
      } catch (error) {
        console.error('全屏播放失败:', error);
        this.$message.warning('您的浏览器不支持全屏播放');
      }
    }
  }
};
</script>

<style scoped>
.camera-management {
  padding: 0;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
  min-height: calc(100vh - 60px);
}

.camera-container {
  padding: 24px;
}

.page-header {
  background: linear-gradient(120deg, #ffffff 0%, #f8f9fa 100%);
  border-radius: 16px;
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.06);
  overflow: hidden;
  margin-bottom: 24px;
  transition: all 0.3s ease;
  border: 1px solid rgba(0, 0, 0, 0.03);
}

.page-header:hover {
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32px;
}

.header-left {
  flex: 1;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #2c3e50;
  display: flex;
  align-items: center;
  gap: 14px;
  letter-spacing: 0.5px;
  margin-bottom: 8px;
}

.page-title i {
  font-size: 28px;
  color: #409eff;
  background: linear-gradient(135deg, #409eff, #3498db);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.page-subtitle {
  font-size: 14px;
  color: #7f8c8d;
  margin: 0;
  padding-left: 42px;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.add-btn, .refresh-btn {
  background: linear-gradient(135deg, #409eff, #3498db);
  border: none;
  border-radius: 8px;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
  transition: all 0.3s ease;
  padding: 12px 20px;
  font-size: 14px;
}

.add-btn:hover, .refresh-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.4);
}

.filter-search-container {
  background: linear-gradient(120deg, #ffffff 0%, #f8f9fa 100%);
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
  padding: 24px 32px;
  margin-bottom: 24px;
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  align-items: center;
  border: 1px solid rgba(0, 0, 0, 0.03);
}

.camera-filter-container {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 15px;
}

.filter-label {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
  white-space: nowrap;
}

.filter-buttons {
  display: flex;
  gap: 12px;
}

.filter-buttons .el-button {
  padding: 8px 16px;
  border-radius: 6px;
  transition: all 0.2s ease;
}

.search-container {
  display: flex;
  gap: 15px;
  margin-left: auto;
  align-items: center;
}

.search-input {
  width: 320px;
}

.search-input ::v-deep .el-input__inner {
  border-radius: 8px;
  border: 1px solid #dcdfe6;
  padding-left: 36px;
  height: 38px;
}

.cards-container {
  padding: 8px;
}

.card-col {
  display: flex;
  flex-direction: column;
}

.camera-card {
  margin-bottom: 24px;
  border-radius: 14px;
  transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
  border: none;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  height: 100%;
  display: flex;
  flex-direction: column;
}

.camera-card ::v-deep .el-card__body {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.camera-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.12);
}

.camera-card ::v-deep .el-card__header {
  padding: 0;
  border: none;
}

.card-header {
  padding: 20px 20px 16px;
  border-bottom: 1px solid #f0f2f5;
}

.card-header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.camera-name {
  font-size: 18px;
  font-weight: 700;
  color: #2c3e50;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.camera-location {
  font-size: 12px;
  color: #409eff;
  background-color: rgba(64, 158, 255, 0.1);
  padding: 4px 10px;
  border-radius: 12px;
  display: inline-block;
  font-weight: 500;
  width: fit-content;
  margin-top: 4px;
}

.status-tag {
  height: auto;
  padding: 3px 8px;
  border-radius: 12px;
  font-size: 12px;
}

.card-body {
  padding: 20px;
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.camera-preview {
  text-align: center;
  margin-bottom: 18px;
  cursor: pointer;
}

.preview-wrapper {
  position: relative;
  width: 100%;
  height: 180px;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.preview-wrapper:hover {
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
  transform: scale(1.02);
}

.preview-wrapper:hover .preview-overlay {
  opacity: 1;
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  background: linear-gradient(135deg, #2c3e50, #34495e);
  transition: transform 0.3s ease;
}

.preview-wrapper:hover .preview-image {
  transform: scale(1.05);
}

.preview-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.85), rgba(52, 152, 219, 0.85));
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.overlay-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  color: #ffffff;
  font-size: 14px;
  font-weight: 600;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.overlay-content i {
  font-size: 48px;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.1);
    opacity: 0.8;
  }
}

.preview-status {
  position: absolute;
  top: 12px;
  right: 12px;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  backdrop-filter: blur(10px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  z-index: 1;
}

.status-online {
  background: rgba(103, 194, 58, 0.9);
  color: #ffffff;
}

.status-offline {
  background: rgba(245, 108, 108, 0.9);
  color: #ffffff;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #ffffff;
  animation: blink 2s infinite;
}

@keyframes blink {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.3;
  }
}

.status-text {
  line-height: 1;
}

.info-item {
  margin-bottom: 18px;
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 40px;
}

.info-item:last-child {
  margin-bottom: 0;
}

.info-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
  font-weight: 500;
}

.info-value {
  font-size: 14px;
  color: #2c3e50;
  word-break: break-word;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  flex: 1;
}

.rtsp-url {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
  background: #f0f2f5;
  padding: 8px;
  border-radius: 4px;
  border-left: 2px solid #409eff;
  white-space: nowrap;
  overflow-x: auto;
}

.card-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 16px 20px;
  border-top: 1px solid #f0f2f5;
  background-color: #fafbfd;
  margin-top: auto;
}

.card-footer .el-button {
  border-radius: 6px;
  padding: 7px 15px;
  font-size: 13px;
}

.no-data {
  text-align: center;
  padding: 80px 20px;
  color: #95a5a6;
  background: linear-gradient(120deg, #ffffff 0%, #f8f9fa 100%);
  border-radius: 14px;
  margin: 10px;
}

.no-data i {
  font-size: 64px;
  margin-bottom: 20px;
  display: block;
  color: #c0c4cc;
}

.no-data p {
  font-size: 18px;
  margin: 0 0 24px;
  font-weight: 500;
}

/* 对话框样式 */
.camera-dialog ::v-deep .el-dialog {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

.camera-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(120deg, #f8f9fa 0%, #ffffff 100%);
  border-bottom: 1px solid #eee;
  padding: 24px;
}

.camera-dialog ::v-deep .el-dialog__title {
  font-weight: 700;
  font-size: 20px;
  color: #2c3e50;
}

.camera-dialog ::v-deep .el-dialog__body {
  padding: 30px;
}

.camera-dialog ::v-deep .el-dialog__footer {
  border-top: 1px solid #eee;
  padding: 18px 24px;
  background-color: #f8f9fa;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
}

.cancel-btn {
  border-color: #dcdfe6;
  color: #606266;
  background-color: #ffffff;
  padding: 11px 22px;
  border-radius: 8px;
  font-size: 14px;
}

.cancel-btn:hover {
  background-color: #f5f7fa;
  border-color: #c0c4cc;
  color: #606266;
}

.confirm-btn {
  background: linear-gradient(135deg, #409eff, #3498db);
  border: none;
  padding: 11px 22px;
  border-radius: 8px;
  font-size: 14px;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.confirm-btn:hover {
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.4);
  transform: translateY(-1px);
}

/* 视频查看对话框样式 */
.video-dialog ::v-deep .el-dialog {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

.video_dialog ::v-deep .el-dialog__header {
  background: linear-gradient(120deg, #2c3e50 0%, #34495e 100%);
  border-bottom: none;
  padding: 24px 30px;
}

.video_dialog ::v-deep .el-dialog__title {
  font-weight: 700;
  font-size: 20px;
  color: #ffffff;
}

.video_dialog ::v-deep .el-dialog__headerbtn .el-dialog__close {
  color: #ffffff;
  font-size: 20px;
}

.video_dialog ::v-deep .el-dialog__headerbtn .el-dialog__close:hover {
  color: #409eff;
}

.video_dialog ::v-deep .el-dialog__body {
  padding: 0;
  background-color: #f8f9fa;
}

.video_dialog ::v-deep .el-dialog__footer {
  border-top: 1px solid #eee;
  padding: 18px 24px;
  background-color: #ffffff;
}

.video-container {
  display: flex;
  flex-direction: column;
}

.video-info-bar {
  display: flex;
  justify-content: space-around;
  align-items: center;
  padding: 20px 30px;
  background: linear-gradient(120deg, #ffffff 0%, #f8f9fa 100%);
  border-bottom: 2px solid #e8eaed;
  flex-wrap: wrap;
  gap: 15px;
}

.info-group {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.info-label {
  font-size: 14px;
  color: #606266;
  font-weight: 600;
}

.info-text {
  font-size: 14px;
  color: #2c3e50;
  font-weight: 500;
}

.video-player-wrapper {
  padding: 30px;
  background-color: #1a1a1a;
  min-height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.video-player {
  width: 100%;
  max-width: 100%;
  position: relative;
  background-color: #000;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.5);
}

.video-element {
  width: 100%;
  height: auto;
  max-height: 500px;
  display: block;
  background-color: #000;
}

.video-placeholder,
.video-offline {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  color: #95a5a6;
  background: linear-gradient(135deg, #2c3e50 0%, #34495e 100%);
  padding: 40px;
  text-align: center;
}

.video-placeholder i,
.video-offline i {
  font-size: 80px;
  margin-bottom: 20px;
  color: #7f8c8d;
}

.video-placeholder p,
.video-offline p {
  margin: 8px 0;
  font-size: 18px;
  color: #bdc3c7;
  font-weight: 500;
}

.hint-text {
  font-size: 14px !important;
  color: #95a5a6 !important;
  font-weight: 400 !important;
}

.video-details {
  padding: 30px;
  background-color: #ffffff;
}

.video-details ::v-deep .el-descriptions {
  border-radius: 8px;
  overflow: hidden;
}

.video-details ::v-deep .el-descriptions-item__label {
  background-color: #f8f9fa;
  font-weight: 600;
  color: #2c3e50;
  width: 140px;
}

.video-details ::v-deep .el-descriptions-item__content {
  color: #5d6d7e;
}

.rtsp-text {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
  color: #409eff;
  word-break: break-all;
  background-color: #f0f2f5;
  padding: 8px 12px;
  border-radius: 4px;
  display: block;
  border-left: 3px solid #409eff;
}

/* 表单样式 */
.camera-form {
  padding: 10px 0;
}

.camera-form ::v-deep .el-form-item {
  margin-bottom: 26px;
}

.camera-form ::v-deep .el-form-item__label {
  font-weight: 500;
  color: #2c3e50;
  padding-right: 12px;
  font-size: 14px;
}

.form-input, .form-textarea {
  width: 100%;
  transition: all 0.3s ease;
}

.form-input ::v-deep .el-input__inner,
.form-textarea ::v-deep .el-textarea__inner {
  border-radius: 8px;
  border: 1px solid #dcdfe6;
  transition: border-color 0.3s ease, box-shadow 0.3s ease;
  height: 40px;
}

.form-input ::v-deep .el-input__inner:focus,
.form-textarea ::v-deep .el-textarea__inner:focus {
  border-color: #409eff;
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.2);
}

.form-textarea ::v-deep .el-textarea__inner {
  border-radius: 8px;
  background-color: #fff;
  min-height: 80px !important;
}

.form-radio-group {
  display: flex;
  gap: 25px;
  padding: 10px 0;
}

.form-radio-group ::v-deep .el-radio {
  font-weight: 500;
  color: #5d6d7e;
}

.form-radio-group ::v-deep .el-radio__input.is-checked+.el-radio__label {
  color: #409eff;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .camera-container {
    padding: 16px;
  }
  
  .header-content {
    padding: 24px;
    flex-direction: column;
    gap: 20px;
    align-items: flex-start;
  }
  
  .header-actions {
    width: 100%;
    justify-content: flex-end;
  }
  
  .filter-search-container {
    padding: 20px;
    flex-direction: column;
    align-items: flex-start;
  }
  
  .search-container {
    margin-left: 0;
    width: 100%;
  }
  
  .search-input {
    flex: 1;
    width: auto;
  }
}

@media (max-width: 768px) {
  .camera-container {
    padding: 12px;
  }
  
  .page-title {
    font-size: 22px;
  }
  
  .page-header,
  .filter-search-container {
    border-radius: 12px;
  }
  
  .header-content {
    padding: 20px;
  }
  
  .filter-search-container {
    padding: 16px;
  }
  
  .filter-buttons {
    width: 100%;
    flex-wrap: wrap;
  }
  
  .camera-card {
    border-radius: 12px;
  }
  
  .card-header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .camera-dialog ::v-deep .el-dialog {
    width: 95% !important;
    margin-top: 20px !important;
  }
  
  .camera-dialog ::v-deep .el-dialog__body {
    padding: 20px;
  }
  
  .video-dialog ::v-deep .el-dialog {
    width: 95% !important;
    margin-top: 20px !important;
  }
  
  .video-info-bar {
    padding: 16px;
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .video-player-wrapper {
    padding: 20px;
    min-height: 300px;
  }
  
  .video-placeholder,
  .video-offline {
    min-height: 300px;
    padding: 30px;
  }
  
  .video-placeholder i,
  .video-offline i {
    font-size: 60px;
  }
  
  .video-details {
    padding: 20px;
  }
  
  .video-details ::v-deep .el-descriptions-item__label {
    width: 100px;
    font-size: 13px;
  }
}
</style>