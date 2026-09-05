<template>
  <div class="component-management">
    <el-card class="page-card">
      <div slot="header" class="page-header">
        <span class="page-title">
          <i class="el-icon-box"></i>
          器件管理
        </span>
        <el-button type="primary" icon="el-icon-plus" @click="handleAdd">
          新增器件
        </el-button>
      </div>

      <!-- 搜索筛选 -->
      <div class="filter-section">
        <el-form :inline="true" :model="queryParams" class="filter-form">
          <el-form-item label="器件名称">
            <el-input v-model="queryParams.name" placeholder="请输入器件名称" clearable @keyup.enter.native="handleSearch">
            </el-input>
          </el-form-item>
          <el-form-item label="器件类型">
            <el-select v-model="queryParams.category" placeholder="请选择类型" clearable>
              <el-option v-for="item in categoryList" :key="item.sensorId" :label="item.sensorName"
                :value="item.sensorCode">
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="通信协议">
            <el-select v-model="queryParams.protocol" placeholder="请选择协议" clearable>
              <el-option label="I2C" value="I2C"></el-option>
              <el-option label="SPI" value="SPI"></el-option>
              <el-option label="UART" value="UART"></el-option>
              <el-option label="Analog" value="Analog"></el-option>
              <el-option label="Digital" value="Digital"></el-option>
              <el-option label="PWM" value="PWM"></el-option>
              <el-option label="Multiple" value="Multiple"></el-option>
              <el-option label="Power" value="Power"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" @click="handleSearch">
              搜索
            </el-button>
            <el-button icon="el-icon-refresh" @click="handleReset">
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 数据表格 -->
      <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%"
        :height="tableHeight" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center"></el-table-column>
        <el-table-column prop="id" label="codeID" width="180" show-overflow-tooltip></el-table-column>
        <el-table-column label="器件名称" width="150">
          <template slot-scope="scope">
            <div class="component-cell">
              <!-- 判断是否为图片URL -->
              <div v-if="scope.row.icon && scope.row.icon.startsWith('http')" class="component-icon-mini">
                <img :src="scope.row.icon" alt="组件图标" class="component-icon-img">
              </div>
              <!-- 否则显示默认图标 -->
              <div v-else class="component-icon-mini" :style="{ background: scope.row.color }">
                <i :class="scope.row.icon"></i>
              </div>
              <span>{{ scope.row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="器件类型" width="100">
          <template slot-scope="scope">
            <el-tag :type="getCategoryType(scope.row.category)" size="small">
              {{ scope.row.category }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="voltage" label="工作电压" width="100">
          <template slot-scope="scope">
            <span class="voltage-text">{{ scope.row.voltage }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="protocol" label="通信协议" width="100">
          <template slot-scope="scope">
            <el-tag size="small" effect="plain">{{ scope.row.protocol }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="引脚数量" width="100" align="center">
          <template slot-scope="scope">
            <el-badge :value="scope.row.pins.length" class="pin-badge">
              <i class="el-icon-connection" style="font-size: 18px; color: #409EFF;"></i>
            </el-badge>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="器件描述" min-width="200" show-overflow-tooltip></el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template slot-scope="scope">
            <el-button type="text" icon="el-icon-view" size="small" @click="handleView(scope.row)">
              详情
            </el-button>
            <el-button type="text" icon="el-icon-edit" size="small" @click="handleEdit(scope.row)">
              编辑
            </el-button>
            <el-button type="text" icon="el-icon-delete" size="small" style="color: #F56C6C;"
              @click="handleDelete(scope.row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 批量操作 -->
      <div class="batch-actions" v-if="selectedRows.length > 0">
        <span class="batch-info">已选择 {{ selectedRows.length }} 项</span>
        <el-button type="danger" icon="el-icon-delete" size="small" @click="handleBatchDelete">
          批量删除
        </el-button>
      </div>

      <!-- 分页 -->
      <el-pagination class="pagination" @size-change="handleSizeChange" @current-change="handleCurrentChange"
        :current-page="queryParams.pageNum" :page-sizes="[10, 20, 50, 100]" :page-size="queryParams.pageSize"
        layout="total, sizes, prev, pager, next, jumper" :total="total">
      </el-pagination>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="800px" :close-on-click-modal="false"
      @close="handleDialogClose">
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="codeID" prop="id">
              <el-input v-model="form.id" placeholder="请输入器件唯一标识" :disabled="form.mode === 'edit'">
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="器件名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入器件名称"></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="器件类型" prop="category">
              <el-select v-model="form.category" placeholder="请选择类型" style="width: 100%;">
                <el-option v-for="item in categoryList" :key="item.sensorId" :label="item.sensorName"
                  :value="item.sensorCode">
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <!-- 替换原来的图片选择 -->
            <el-form-item label="器件图片" prop="icon">
              <el-upload class="avatar-uploader" action="" :show-file-list="false" :on-change="handleImageChange"
                :auto-upload="false" :before-upload="beforeUpload" accept=".jpg,.jpeg,.png,.gif,.svg">
                <div v-if="tempImageUrl || form.imageUrl" class="image-container">
                  <img :src="tempImageUrl || form.imageUrl" class="avatar">
                  <div class="delete-btn" @click.stop="removeImage">
                    <i class="el-icon-close"></i>
                  </div>
                </div>
                <i v-else class="el-icon-plus avatar-uploader-icon"></i>
              </el-upload>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="工作电压" prop="voltage">
              <el-input v-model="form.voltage" placeholder="例如: 5V"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="协议" prop="agreement">
              <el-select v-model="form.agreement" placeholder="请选择协议" style="width: 100%;">
                <el-option label="I2C" value="I2C"></el-option>
                <el-option label="SPI" value="SPI"></el-option>
                <el-option label="UART" value="UART"></el-option>
                <el-option label="Analog" value="Analog"></el-option>
                <el-option label="Digital" value="Digital"></el-option>
                <el-option label="PWM" value="PWM"></el-option>
                <el-option label="Multiple" value="Multiple"></el-option>
                <el-option label="Power" value="Power"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="器件描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入器件描述信息">
          </el-input>
        </el-form-item>

        <el-form-item label="阈值配置" v-if="shouldShowThresholdFields">
          <div style="margin-bottom: 10px;">
            <el-button type="primary" size="small" icon="el-icon-plus" @click="handleAddThreshold">
              添加阈值
            </el-button>
          </div>

          <el-table :data="form.threshold" border size="small">
            <el-table-column label="阈值名称" width="200">
              <template slot-scope="scope">
                <el-input v-model="scope.row.thresholdName" placeholder="请输入阈值名称"></el-input>
              </template>
            </el-table-column>

            <el-table-column label="阈值数值" width="260">
              <template slot-scope="scope">
                <el-input v-model="scope.row.thresholdNum" placeholder="请输入阈值数值，格式：'最小值-最大值'，如：'-40-80'"
                  :class="{ 'input-error': hasThresholdError(scope.$index) }"></el-input>
                <!-- 显示特定阈值行的错误信息 -->
                <div v-if="hasThresholdError(scope.$index)" class="threshold-error-message">
                  {{ getThresholdErrorMessage(scope.$index) }}
                </div>
              </template>
            </el-table-column>

            <el-table-column label="操作" width="198" align="center">
              <template slot-scope="scope">
                <el-button type="text" icon="el-icon-delete" size="small" @click="handleDeleteThreshold(scope.$index)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 显示整体阈值错误信息 -->
          <div v-if="thresholdError && !thresholdErrorDetails.length" class="threshold-error-message">
            {{ thresholdError }}
          </div>
        </el-form-item>

        <el-form-item label="是否启用" prop="enabled" :rules="[{ required: true, message: '请选择启用状态', trigger: 'change' }]">
          <el-radio-group v-model="form.enabled">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">不启用</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="引脚配置" prop="pins">
          <div class="pins-config">
            <el-button type="primary" icon="el-icon-plus" size="small" @click="handleAddPin">
              添加引脚
            </el-button>

            <el-table :data="form.pins" border size="small" style="margin-top: 10px;" row-key="id" ref="pinTable">
              <el-table-column label="排序" width="60" align="center">
                <template >
                  <i class="el-icon-rank drag-handle" style="cursor: move; color: #909399; font-size: 16px;"></i>
                </template>
              </el-table-column>
              <el-table-column label="引脚ID" width="110">
                <template slot-scope="scope">
                  <el-input v-model="scope.row.id" size="small" placeholder="例如: vcc">
                  </el-input>
                </template>
              </el-table-column>
              <el-table-column label="引脚标签" width="110">
                <template slot-scope="scope">
                  <el-input v-model="scope.row.label" size="small" placeholder="例如: VCC">
                  </el-input>
                </template>
              </el-table-column>
              <el-table-column label="类型" width="110">
                <template slot-scope="scope">
                  <el-select v-model="scope.row.type" size="small">
                    <el-option label="输入" value="input"></el-option>
                    <el-option label="输出" value="output"></el-option>
                    <el-option label="输入/输出" value="IO"></el-option>
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="电压(V)" width="110">
                <template slot-scope="scope">
                  <el-input-number v-model="scope.row.voltage" size="small" :precision="1" :step="0.1" :min="0"
                    :max="50">
                  </el-input-number>
                </template>
              </el-table-column>
              <!-- 新增位置字段 -->
              <!-- 修改位置字段为输入框 -->
              <el-table-column label="位置" width="110">
                <template slot-scope="scope">
                  <el-input v-model="scope.row.location" size="small" placeholder="输入位置"></el-input>
                </template>
              </el-table-column>
              <!-- 新增颜色选择列 -->
               <!-- #FF404D 红色  #080808黑色 -->
              <el-table-column label="颜色" width="150">
                <template slot-scope="scope">
                  <el-color-picker v-model="scope.row.color" size="small"></el-color-picker>
                  <span style="margin-left: 5px; color: #909399; font-size: 12px;">{{ scope.row.color }}</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="105" align="center">
                <template slot-scope="scope">
                  <el-button type="text" icon="el-icon-delete" size="small" style="color: #F56C6C;"
                    @click="handleDeletePin(scope.$index)">
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-form-item>
      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
          确 定
        </el-button>
      </div>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog title="器件详情" :visible.sync="detailVisible" width="700px">
      <div v-if="currentComponent" class="detail-content">
        <div class="detail-header">
          <div class="detail-icon" v-if="currentComponent.images && currentComponent.images.startsWith('http')">
            <img :src="currentComponent.images" alt="组件图标" class="detail-icon-img">
          </div>
          <div class="detail-icon" :style="{ background: currentComponent.color }" v-else>
            <i :class="currentComponent.icon"></i>
          </div>
          <div class="detail-title">
            <h2>{{ currentComponent.name }}</h2>
            <el-tag :type="getCategoryType(currentComponent.category)" size="small">
              {{ currentComponent.category }}
            </el-tag>
          </div>
        </div>

        <el-descriptions :column="2" border>
          <el-descriptions-item label="codeID">{{ currentComponent.id }}</el-descriptions-item>
          <el-descriptions-item label="工作电压">
            <span class="voltage-text">{{ currentComponent.voltage }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="通信协议">
            <el-tag size="small" effect="plain">{{ currentComponent.protocol }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="颜色">
            <span class="color-preview" :style="{ background: currentComponent.color }"></span>
            {{ currentComponent.color }}
          </el-descriptions-item>
          <el-descriptions-item label="器件描述" :span="2">
            {{ currentComponent.description || '暂无描述' }}
          </el-descriptions-item>
        </el-descriptions>

        <div class="pins-section">
          <h3><i class="el-icon-connection"></i> 引脚配置 ({{ currentComponent.pins.length }})</h3>
          <div class="pins-grid">
            <div v-for="pin in currentComponent.pins" :key="pin.id" class="pin-card">
              <div class="pin-card-header">
                <span class="pin-dot" :class="pin.type"></span>
                <span class="pin-label">{{ pin.label }}</span>
                <el-tag :type="pin.type === 'output' ? 'warning' : 'success'" size="mini" effect="dark">
                  {{ pin.type === 'output' ? 'OUT' : 'IN' }}
                </el-tag>
              </div>
              <div class="pin-card-body">
                <span v-if="pin.voltage !== undefined">
                  <i class="el-icon-lightning"></i> {{ pin.voltage }}V
                </span>
                <span v-if="pin.protocol">
                  <i class="el-icon-connection"></i> {{ pin.protocol }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getSensorList, getSensorlist, addedcomponents, modifycomponents, deletecomponents, uploadImage } from '@/api/HardwareSimulation'
import Sortable from 'sortablejs'

export default {
  name: 'ComponentManagement',

  data() {
    return {
      loading: false,
      tableData: [],
      total: 0,
      selectedRows: [],
      tableHeight: 400, // 默认表格高度
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        name: '',
        category: '',
        protocol: ''
      },

      dialogVisible: false,
      dialogTitle: '',
      submitLoading: false,
      form: {
        mode: 'add',
        id: '',
        name: '',
        category: '',
        icon: 'el-icon-box',
        voltage: '',
        protocol: '',
        color: '#409EFF',
        description: '',
        pins: [],
        agreement: '', // 新增agreement字段
        images: '',    // 新增images字段用于存储图片路径
        threshold: [] // 新增threshold字段
      },
      rules: {
        id: [
          { required: true, message: '请输入codeID', trigger: 'blur' },
          { pattern: /^[a-zA-Z0-9_-]+$/, message: '只能包含字母、数字、下划线和中划线', trigger: 'blur' }
        ],
        name: [
          { required: true, message: '请输入器件名称', trigger: 'blur' }
        ],
        category: [
          { required: true, message: '请选择器件类型', trigger: 'change' }
        ],
        // voltage: [
        //   { required: true, message: '请输入工作电压', trigger: 'blur' }
        // ],
        protocol: [
          { required: true, message: '请选择通信协议', trigger: 'change' }
        ],
        agreement: [  // 新增agreement字段校验规则
          { required: true, message: '请选择协议', trigger: 'change' }
        ],
        pins: [
          { type: 'array', required: true, message: '请至少添加一个引脚', trigger: 'change' }
        ]
      },

      detailVisible: false,
      currentComponent: null,
      sensorList: [], // 新增：存储传感器类型列表
      categoryList: [], // 存储从后端获取的器件类型列表
      // 添加图片相关字段
      imageUrl: '', // 上传图片的URL
      tempImageUrl: '', // 临时存储本地预览图片
      uploadHeaders: {
        'Authorization': localStorage.getItem('token') // 如果需要认证头
      },
      uploadAction: '', // 临时不使用真实接口

      // 添加错误信息存储字段
      thresholdError: '', // 阈值错误信息
      thresholdErrorDetails: [], // 存储每个阈值行的具体错误信息
    }
  },

  mounted() {
    this.loadData()
    this.getCategoryList() // 新增：加载传感器类型列表
    this.calculateTableHeight()
    window.addEventListener('resize', this.calculateTableHeight)
  },
  
  beforeDestroy() {
    window.removeEventListener('resize', this.calculateTableHeight)
  },

  computed: {
    // 计算属性：判断是否应该显示阈值字段
    shouldShowThresholdFields() {
      // 定义需要显示阈值字段的类型
      const thresholdTypes = ['传感器', '执行器'];

      // 获取当前选择的类型名称
      const selectedCategory = this.categoryList.find(cat => cat.sensorCode === this.form.category);
      const categoryName = selectedCategory ? selectedCategory.sensorName : this.form.category;

      // 检查是否属于需要显示阈值字段的类型
      return thresholdTypes.some(type =>
        categoryName && (categoryName.includes(type) || categoryName === type)
      );
    }
  },

  methods: {

    // 计算表格高度
    calculateTableHeight() {
      const windowHeight = window.innerHeight
      const tableTop = this.$el ? this.$el.getBoundingClientRect().top : 300
      const padding = 100 // 底部留白
      
      // 计算可用高度
      const availableHeight = windowHeight - tableTop - padding
      
      // 设置最小和最大高度限制
      this.tableHeight = Math.max(300, Math.min(availableHeight, 800))
    },

    // 获取器件类型列表
    async getCategoryList() {
      try {
        console.log('Fetching category list...');
        const response = await getSensorlist();
        console.log('API response:', response);

        // 【关键修改】将sensorId转换为字符串以避免精度丢失
        this.categoryList = (response.data.rows || []).map(item => ({
          ...item,
          sensorId: String(item.sensorId)  // 将ID转换为字符串
        }));

        console.log('Updated categoryList:', this.categoryList);
      } catch (error) {
        console.error('获取器件类型列表失败:', error);
        this.$message.error('获取器件类型列表失败');
      }
    },


    // 加载数据
    loadData() {
      this.loading = true

      // 使用API请求获取数据
      getSensorList()
        .then(response => {
          const res = response.data

          // 筛选
          let filteredData = res.rows || []
          if (this.queryParams.name) {
            filteredData = filteredData.filter(item =>
              item.virtualName && item.virtualName.includes(this.queryParams.name)
            )
          }
          if (this.queryParams.category) {
            // 修改筛选条件：同时匹配categoryCode和sensorCode
            filteredData = filteredData.filter(item =>
              item.categoryCode === this.queryParams.category ||
              item.sensorCode === this.queryParams.category ||
              String(item.genreId) === this.queryParams.category
            )
          }
          if (this.queryParams.protocol) {
            filteredData = filteredData.filter(item =>
              item.agreement === this.queryParams.protocol ||
              item.protocol === this.queryParams.protocol
            )
          }

          // 分页
          this.total = filteredData.length
          const start = (this.queryParams.pageNum - 1) * this.queryParams.pageSize
          const end = start + this.queryParams.pageSize

          console.log('API返回的原始数据:', res.rows); // 添加调试信息

          this.tableData = filteredData.slice(start, end).map(item => {
            const transformed = this.transformApiDataToTableFormat(item);
            console.log('转换后的数据:', transformed); // 添加调试信息
            return transformed;
          });
        })
        .catch(error => {
          console.error('获取器件数据失败:', error)
          this.$message.error('获取器件数据失败')
        })
        .finally(() => {
          this.loading = false
        })
    },

    transformApiDataToTableFormat(apiItem) {
      // 根据后端返回的类型查找对应信息
      const category = apiItem.genreName || apiItem.category || '未知';
      const categoryCode = apiItem.sensorCode || '';

      // 默认图标映射（可根据实际需求调整）
      const categoryIconMap = {
        '电源': 'el-icon-box',
        '网关': 'el-icon-connection',
        '控制器': 'el-icon-cpu',
        '传感器': 'el-icon-thermometer',
        '执行器': 'el-icon-sunny',
        '通信模块': 'el-icon-service',
        'Io': 'el-icon-link'  // 为Io类型添加图标
      }

      // 默认颜色映射
      const categoryColorMap = {
        '电源': '#FFC312',
        '网关': '#A55EEA',
        '控制器': '#95E1D3',
        '传感器': '#FF6B6B',
        '执行器': '#F8B500',
        '通信模块': '#4ECDC4',
        'Io': '#67C23A'  // 为Io类型添加颜色
      }

      // 正确处理阈值数据
      let thresholdData = [];
      if (apiItem.threshold && Array.isArray(apiItem.threshold)) {
        thresholdData = apiItem.threshold.map(th => ({
          thresholdName: th.thresholdName || '',
          thresholdNum: th.thresholdNum !== undefined ? th.thresholdNum : null
        }));
      } else {
        thresholdData = [{ thresholdName: '', thresholdNum: null }];
      }

      return {
        id: apiItem.virtualCode || String(apiItem.virtualId), // 确保ID为字符串
        name: apiItem.virtualName,
        category: category,
        categoryCode: categoryCode,
        icon: apiItem.images ? apiItem.images : categoryIconMap[category] || 'el-icon-box',
        // 处理可能为空的电压值
        voltage: apiItem.voltage ? `${apiItem.voltage}V` : '',
        protocol: apiItem.agreement || apiItem.protocol || 'Unknown', // 优先使用agreement字段
        color: categoryColorMap[category] || '#409EFF',
        description: apiItem.description || `设备${apiItem.virtualName}的描述`,
        pins: (apiItem.pin || []).map(pin => ({
          id: String(pin.pinId),
          label: pin.pinName,
          type: pin.pinType || 'input',  // 直接使用后端返回的类型，而不是转换
          voltage: typeof pin.voltage !== 'undefined' && pin.voltage !== null ?
            parseFloat(pin.voltage) || 0 : 0,  // 将后端返回的电压字符串转为数字
          location: pin.location || '',  // 添加位置字段，从后端获取
          protocol: '',
          color: pin.pinColour || pin.color || '#409EFF'  // 添加颜色属性，兼容后端字段
        })),
        status: apiItem.status, // 添加状态字段
        enabled: apiItem.status, // 添加启用状态字段
        agreement: apiItem.agreement || apiItem.protocol, // 添加agreement字段
        images: apiItem.images, // 添加图片字段
        originalData: apiItem,
        threshold: thresholdData
      }
    },

    // 搜索
    handleSearch() {
      this.queryParams.pageNum = 1
      this.loadData()
    },

    // 重置
    handleReset() {
      this.queryParams = {
        pageNum: 1,
        pageSize: 10,
        name: '',
        category: '', // 确保重置时清空分类筛选
        protocol: ''
      }
      this.loadData()
    },

    // 分页
    handleSizeChange(val) {
      this.queryParams.pageSize = val
      this.loadData()
    },

    handleCurrentChange(val) {
      this.queryParams.pageNum = val
      this.loadData()
    },

    // 选择
    handleSelectionChange(val) {
      this.selectedRows = val
    },

    // 文件上传前的验证
    beforeUpload(file) {
      const isImg = file.type.startsWith('image/');
      const isLt2M = file.size / 1024 / 1024 < 2;

      if (!isImg) {
        this.$message.error('只能上传图片文件!');
        return false;
      }

      if (!isLt2M) {
        this.$message.error('图片大小不能超过 2MB!');
        return false;
      }

      return isImg && isLt2M;
    },


    // 处理图片选择变化
    handleImageChange(file) {
      // 先创建本地预览URL作为容错机制
      const localPreviewUrl = URL.createObjectURL(file.raw);
      this.tempImageUrl = localPreviewUrl;
      this.form.icon = localPreviewUrl; // 先使用本地预览

      // 调用上传接口
      const formData = new FormData();
      formData.append('file', file.raw);

      // 获取token并上传图片
      const token = localStorage.getItem('token');
      uploadImage(token, file.raw)
        .then(response => {
          if (response.data.code === 200) {
            // 上传成功，使用服务器返回的URL
            // 根据实际返回格式，URL在msg字段中
            const imageUrl = response.data.msg;
            this.tempImageUrl = imageUrl;
            this.form.icon = imageUrl;
            this.form.images = imageUrl; // 将图片路径保存到images字段
            this.$message.success('图片上传成功');
          } else {
            // 上传失败，保留本地预览并提示用户
            this.$message.warning('图片上传失败，使用本地预览');
          }
        })
        .catch(error => {
          console.error('图片上传失败:', error);
          // 出错时保留本地预览并提示用户
          this.$message.warning('图片上传失败，使用本地预览');
        });
    },

    // 重置上传图片
    resetImage() {
      this.tempImageUrl = '';
      this.imageUrl = '';
      // 在表单重置时也恢复默认图标
      if (this.form.mode !== 'edit') {
        this.form.icon = 'el-icon-box';
      }
    },

    // 删除已上传的图片
    removeImage() {
      this.$confirm('确定要删除当前图片吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.tempImageUrl = '';
        this.form.icon = 'el-icon-box'; // 恢复默认图标
        this.imageUrl = '';
      }).catch(() => {
        // 用户取消删除，不做任何操作
      });
    },

    //  新增操作
    handleAdd() {
      this.dialogTitle = '新增器件'
      this.form = {
        mode: 'add',
        id: '',
        name: '',
        category: '',
        icon: 'el-icon-box',
        voltage: '',
        protocol: '',
        color: '#409EFF',
        description: '',
        pins: [],
        agreement: '',
        images: '',
        threshold: [  // 修改：初始化阈值数组对象，使用正确的字段名
          {
            thresholdName: '',
            thresholdNum: null
          }
        ]
      }
      this.dialogVisible = true
    },

    // 编辑 处理编辑时的阈值字段
    handleEdit(row) {
      this.dialogTitle = '编辑器件'

      // 使用深拷贝确保完全独立的数据
      const clonedRow = JSON.parse(JSON.stringify(row));

      // 正确处理阈值数据
      let thresholdData = [];
      if (row.threshold && Array.isArray(row.threshold)) {
        // 后端返回的是数组格式，直接使用
        thresholdData = row.threshold.map(th => ({
          thresholdName: th.thresholdName || '',
          thresholdNum: th.thresholdNum !== undefined ? th.thresholdNum : null
        }));
      } else if (row.threshold && typeof row.threshold === 'object') {
        // 如果是单个对象的情况
        thresholdData = [{
          thresholdName: row.threshold.thresholdName || row.thresholdName || '',
          thresholdNum: row.threshold.thresholdNum !== undefined ? row.threshold.thresholdNum : (row.thresholdNum || null)
        }];
      } else {
        // 默认情况
        thresholdData = [{ thresholdName: '', thresholdNum: null }];
      }

      this.form = {
        mode: 'edit',
        ...clonedRow,
        threshold: thresholdData
      }

      // 特别处理引脚数据，确保数值正确
      this.form.pins = (row.pins || []).map(pin => ({
        id: String(pin.id || pin.pinId),
        label: pin.label || pin.pinName,
        type: pin.type || pin.pinType || 'input',  // 保持原有的类型值
        voltage: typeof pin.voltage !== 'undefined' && pin.voltage !== null ?
          parseFloat(pin.voltage) || 0 : 0,  // 将电压字符串转为数字
        location: pin.location || '',  // 添加位置字段
        protocol: pin.protocol || '',
        color: pin.color || pin.pinColour || '#409EFF'  // 添加颜色属性，兼容后端字段
      }));

      // 在编辑模式下，根据category查找对应的类型ID
      if (row.categoryCode) {
        const categoryInfo = this.categoryList.find(cat => cat.sensorCode === row.categoryCode);
        if (categoryInfo) {
          this.form.category = categoryInfo.sensorCode;
        }
      }

      // 处理图片回显
      if (row.images) {
        this.tempImageUrl = row.images;
        this.form.icon = row.images;
        this.form.images = row.images;
      } else if (row.icon && (row.icon.startsWith('http') || row.icon.startsWith('data:image'))) {
        this.tempImageUrl = row.icon;
        this.form.icon = row.icon;
      } else {
        this.tempImageUrl = '';
        this.form.icon = row.icon || 'el-icon-box';
      }

      // 设置协议字段
      this.form.agreement = row.agreement || row.protocol;

      if (!this.isValidProtocol(this.form.protocol)) {
        console.log('Backend protocol not in options, keeping as is:', this.form.protocol);
      }

      this.dialogVisible = true;
      this.$nextTick(() => {
        this.initPinTableDrag();
      });
    },

    // 查看详情
    handleView(row) {
      this.currentComponent = row
      this.detailVisible = true
    },

    // 删除
    handleDelete(row) {
      this.$confirm(`确定要删除器件"${row.name}"吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // 调用删除接口
        const virtualId = row.originalData.virtualId; // 获取原始数据中的virtualId
        deletecomponents(localStorage.getItem('token'), virtualId)
          .then(() => {
            this.$message.success('删除成功');
            this.loadData(); // 重新加载数据
          })
          .catch(error => {
            console.error('删除器件失败:', error);
            this.$message.error('删除失败');
          });
      }).catch(() => { })
    },

    // 批量删除
    handleBatchDelete() {
      if (this.selectedRows.length === 0) {
        this.$message.warning('请先选择要删除的器件');
        return;
      }

      this.$confirm(`确定要删除选中的 ${this.selectedRows.length} 个器件吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // 批量删除多个器件
        const deletePromises = this.selectedRows.map(row => {
          const virtualId = row.originalData.virtualId;
          return deletecomponents(localStorage.getItem('token'), virtualId);
        });

        Promise.all(deletePromises)
          .then(responses => {
            this.$message.success(`成功删除了 ${responses.length} 个器件`);
            this.loadData(); // 重新加载数据
          })
          .catch(error => {
            console.error('批量删除器件失败:', error);
            this.$message.error('批量删除失败');
          });
      }).catch(() => { })
    },

    // 添加引脚
    handleAddPin() {
      this.form.pins.push({
        id: '',
        label: '',
        type: 'input',
        voltage: 5,
        location: '',  // 默认位置为空
        protocol: '',
        color: '#409EFF'  // 默认颜色
      })
    },

    // 删除引脚
    handleDeletePin(index) {
      this.form.pins.splice(index, 1)
    },


    // 新增阈值
    handleAddThreshold() {
      this.form.threshold.push({
        thresholdName: '',
        thresholdNum: null
      })
    },

    // 删除阈值
    handleDeleteThreshold(index) {
      this.form.threshold.splice(index, 1)
    },

    // 提交表单
    handleSubmit() {
      this.$refs.form.validate(valid => {
        if (valid) {
          this.submitLoading = true

          // 清除之前的错误信息
          this.thresholdError = '';
          this.thresholdErrorDetails = [];

          // 根据选择的类型找到对应的类型ID
          const selectedCategory = this.categoryList.find(cat => cat.sensorCode === this.form.category);

          // 如果是编辑模式，确保我们有正确的genreId
          if (this.form.mode === 'edit' && !selectedCategory) {
            // 如果找不到匹配的类别，尝试通过原始数据恢复
            const originalCategory = this.categoryList.find(cat => cat.sensorId === this.form.originalData?.genreId);
            if (originalCategory) {
              this.form.category = originalCategory.sensorCode;
            }
          }

          // 检查是否选择了"不启用"选项
          if (this.form.enabled === 0) {
            this.$confirm('确定要禁用此器件吗？禁用后该器件将不可用！', '警告', {
              confirmButtonText: '确定',
              cancelButtonText: '取消',
              type: 'warning'
            }).then(() => {
              this.sendDeviceData(selectedCategory);
            }).catch(() => {
              this.submitLoading = false;
            });
          } else {
            this.sendDeviceData(selectedCategory);
          }
        }
      })
    },

    // 发送设备数据
    sendDeviceData(selectedCategory) {
      // 准备发送到后端的数据
      const deviceData = {
        virtualCode: this.form.id,
        virtualName: this.form.name,
        category: this.form.category,
        genreId: selectedCategory ? selectedCategory.sensorId : null,
        voltage: this.form.voltage ? parseFloat(this.form.voltage.replace('V', '')) : 0,
        protocol: this.form.protocol,
        description: this.form.description,
        status: this.form.enabled,
        agreement: this.form.agreement,
        images: this.form.images,
        // 修改：传递完整的阈值数组，过滤掉未填写的项
        threshold: this.shouldShowThresholdFields && Array.isArray(this.form.threshold)
          ? this.form.threshold
            .filter(threshold => threshold.thresholdName && (threshold.thresholdNum !== null && threshold.thresholdNum !== ''))
            .map(threshold => ({
              thresholdName: threshold.thresholdName,
              thresholdNum: String(threshold.thresholdNum)  // 转换为字符串
            }))
          : [],
        pin: this.form.pins.map(pin => ({
          pinId: pin.id,
          pinName: pin.label,
          pinType: pin.type,  // 这里发送用户在界面上选择的实际类型
          voltage: Number(pin.voltage),  // 使用小写的voltage字段
          location: pin.location,  // 添加位置字段
          pinColour: pin.color  // 添加引脚颜色字段，使用后端要求的字段名
        }))
      }

      // 如果是编辑模式，需要添加virtualId
      if (this.form.mode === 'edit') {
        deviceData.virtualId = this.form.originalData.virtualId;
      }

      console.log('Device data to send:', deviceData);  // 添加日志以便调试

      // 判断是新增还是编辑模式
      let apiCall;
      if (this.form.mode === 'edit') {
        // 编辑模式使用 modifycomponents 接口
        apiCall = modifycomponents(localStorage.getItem('token'), deviceData);
      } else {
        // 新增模式使用 addedcomponents 接口
        apiCall = addedcomponents(localStorage.getItem('token'), deviceData);
      }

      // 调用API
      apiCall
        .then(response => {
          console.log(response);

          this.submitLoading = false

          // 检查返回的状态码
          if (response.data && response.data.code === 200) {
            this.dialogVisible = false

            // 显示成功消息
            this.$message.success(this.form.mode === 'add' ? '新增成功' : '修改成功')

            // 重新加载数据以获取最新列表
            this.loadData()
          } else {
            // 如果返回的不是200，也当作错误处理
            this.handleError({ response: { data: response.data } });
          }
        })
        .catch(error => {
          this.handleError(error);
        })
    },

    // 统一错误处理方法
    handleError(error) {
      this.submitLoading = false;

      // 清除之前的错误信息
      this.thresholdError = '';
      this.thresholdErrorDetails = [];

      // 检查错误响应
      if (error.response && error.response.data) {
        const errorData = error.response.data;

        // 检查错误码是否为500
        if (errorData.code === 500) {
          // 检查是否是阈值格式错误
          if (errorData.msg && errorData.msg.includes('阈值格式错误')) {
            // 显示错误信息
            this.thresholdError = errorData.msg;

            // 如果错误信息包含具体哪一行的问题，可以进一步处理
            this.$message.error({
              message: errorData.msg,
              duration: 1000, // 不自动关闭
              showClose: true
            });

            // 保持对话框打开
            return;
          }

          // 如果是其他500错误
          this.$message.error({
            message: errorData.msg || '操作失败',
            duration: 1000, // 不自动关闭
            showClose: true
          });

          // 保持对话框打开
          return;
        }

        // 其他错误码
        this.$message.error({
          message: errorData.msg || '操作失败',
          duration: 1000, // 不自动关闭
          showClose: true
        });
      } else {
        // 网络错误或其他错误
        this.$message.error({
          message: '网络错误或服务不可用',
          duration: 1000, // 不自动关闭
          showClose: true
        });
      }

      console.error('提交器件数据失败:', error);
    },

    // 检查特定阈值索引是否有错误
    hasThresholdError(index) {
      if (!this.thresholdErrorDetails || !Array.isArray(this.thresholdErrorDetails)) {
        return false;
      }

      // 检查是否有针对特定索引的错误
      return this.thresholdErrorDetails.some(error => error.index === index);
    },

    // 获取特定阈值索引的错误信息
    getThresholdErrorMessage(index) {
      if (!this.thresholdErrorDetails || !Array.isArray(this.thresholdErrorDetails)) {
        return this.thresholdError || '';
      }

      const error = this.thresholdErrorDetails.find(err => err.index === index);
      return error ? error.message : this.thresholdError;
    },

    // 对话框关闭
    handleDialogClose() {
      this.$refs.form.resetFields()
      this.resetImage();

      // 重置阈值数组对象
      this.form.threshold = [
        {
          thresholdName: '',
          thresholdNum: null
        }
      ];

      // 清除错误信息
      this.thresholdError = '';
      this.thresholdErrorDetails = [];

      // 销毁拖拽实例
      this.destroyPinTableDrag();
    },

    // 获取类型标签
    getCategoryType(category) {
      const typeMap = {
        '电源': 'warning',
        '传感器': 'success',
        '控制器': 'primary',
        '执行器': 'danger'
      }
      return typeMap[category] || 'info'
    },

    // 新增：加载传感器类型列表
    loadSensorList() {
      getSensorlist()
        .then(response => {
          this.sensorList = response.data || []
        })
        .catch(error => {
          console.error('获取传感器类型列表失败:', error)
          this.$message.error('获取传感器类型列表失败')
        })
    },

    // 新增：根据传感器名称获取对应的sensorId
    getSensorIdByName(sensorName) {
      const sensor = this.sensorList.find(s => s.sensorName === sensorName || s.virtualName?.includes(sensorName))
      return sensor ? sensor.sensorId : null
    },

    // 新增方法：验证协议是否有效
    isValidProtocol(protocolValue) {
      const validProtocols = ['I2C', 'SPI', 'UART', 'Analog', 'Digital', 'PWM', 'Multiple', 'Power'];
      return validProtocols.includes(protocolValue);
    },

    // 初始化引脚表格拖拽功能
    initPinTableDrag() {
      const table = this.$refs.pinTable;
      if (!table || !table.$el) return;
      
      const tbody = table.$el.querySelector('.el-table__body-wrapper tbody');
      if (!tbody) return;
      
      // 销毁之前的实例
      this.destroyPinTableDrag();
      
      // 创建新的Sortable实例
      this.sortable = Sortable.create(tbody, {
        handle: '.drag-handle',
        animation: 150,
        ghostClass: 'sortable-ghost',
        chosenClass: 'sortable-chosen',
        dragClass: 'sortable-drag',
        onEnd: (evt) => {
          // 获取拖拽前后的索引
          const oldIndex = evt.oldIndex;
          const newIndex = evt.newIndex;
          
          // 更新数据顺序
          if (oldIndex !== newIndex) {
            const pin = this.form.pins.splice(oldIndex, 1)[0];
            this.form.pins.splice(newIndex, 0, pin);
            
            // 强制更新视图
            this.$forceUpdate();
          }
        }
      });
    },

    // 销毁引脚表格拖拽功能
    destroyPinTableDrag() {
      if (this.sortable) {
        this.sortable.destroy();
        this.sortable = null;
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.component-icon-img {
  width: 28px;
  height: 28px;
  border-radius: 6px;
  object-fit: cover;
  display: block;
}

.component-icon-mini {
  width: 28px;
  height: 28px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;

  i {
    font-size: 14px;
    color: white;
  }
}

/* 阈值错误信息样式 */
.threshold-error-message {
  color: #f56c6c;
  font-size: 12px;
  margin-top: 5px;
  margin-left: 100px;
  /* 与表单标签对齐 */
}

.input-error input {
  border-color: #f56c6c !important;
}

/* 弹窗不关闭时的样式 */
.dialog-not-closable {
  pointer-events: auto;
}

.component-management {
  height: 100%;

  .page-card {
    height: 100%;
    display: flex;
    flex-direction: column;

    ::v-deep .el-card__body {
      flex: 1;
      overflow: hidden;
      display: flex;
      flex-direction: column;
    }

    .page-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .page-title {
        font-size: 18px;
        font-weight: 600;
        color: #303133;
        display: flex;
        align-items: center;
        gap: 8px;

        i {
          font-size: 22px;
          color: #409EFF;
        }
      }
    }
  }

  .filter-section {
    padding: 20px 0;
    border-bottom: 1px solid #EBEEF5;

    .filter-form {
      ::v-deep .el-form-item {
        margin-bottom: 0;
      }
    }
  }

  .el-table {
    margin-top: 20px;
    flex: 1;
    
    // 美化滚动条
      ::v-deep .el-table__body-wrapper {
        // Webkit浏览器样式
        &::-webkit-scrollbar {
          width: 8px;
          height: 8px;
        }
        
        &::-webkit-scrollbar-track {
          background: #f5f5f5;
          border-radius: 4px;
        }
        
        &::-webkit-scrollbar-thumb {
          background: #c0c4cc;
          border-radius: 4px;
          transition: background 0.3s ease;
          
          &:hover {
            background: #909399;
          }
        }
        
        &::-webkit-scrollbar-corner {
          background: #f5f5f5;
        }
        
        // Firefox浏览器样式
        scrollbar-width: thin;
        scrollbar-color: #c0c4cc #f5f5f5;
      }

    .component-cell {
      display: flex;
      align-items: center;
      gap: 10px;

      .component-icon-mini {
        width: 28px;
        height: 28px;
        border-radius: 6px;
        display: flex;
        align-items: center;
        justify-content: center;

        i {
          font-size: 14px;
          color: white;
        }
      }
    }

    .voltage-text {
      padding: 2px 8px;
      background: linear-gradient(135deg, #FFC312 0%, #F79F1F 100%);
      color: white;
      border-radius: 10px;
      font-size: 11px;
      font-weight: 600;
    }

    .pin-badge {
      ::v-deep .el-badge__content {
        background-color: #409EFF;
        border: none;
      }

      ::v-deep .el-badge__content.is-fixed {
        position: absolute;
        top: 9px;
        right: -3px;
      }
    }
  }

  .batch-actions {
    padding: 15px 0;
    display: flex;
    align-items: center;
    gap: 15px;

    .batch-info {
      font-size: 13px;
      color: #606266;
    }
  }

  .pagination {
    margin-top: 20px;
    text-align: right;
  }

  // 对话框样式
  .pins-config {
    width: 100%;
  }

  // 详情对话框
  .detail-content {
    .detail-header {
      display: flex;
      align-items: center;
      padding: 20px;
      margin: -20px -20px 20px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);

      .detail-icon {
        width: 60px;
        height: 60px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        background: rgba(255, 255, 255, 0.2);
        backdrop-filter: blur(10px);

        i {
          font-size: 32px;
          color: white;
        }
      }

      .detail-title {
        flex: 1;
        margin-left: 20px;

        h2 {
          margin: 0 0 8px 0;
          font-size: 20px;
          color: white;
        }
      }
    }

    .color-preview {
      display: inline-block;
      width: 20px;
      height: 20px;
      border-radius: 4px;
      border: 1px solid #DCDFE6;
      vertical-align: middle;
      margin-right: 8px;
    }

    .pins-section {
      margin-top: 30px;

      h3 {
        margin: 0 0 15px 0;
        font-size: 15px;
        color: #606266;
        display: flex;
        align-items: center;

        i {
          margin-right: 8px;
          color: #409EFF;
        }
      }

      .pins-grid {
        display: grid;
        grid-template-columns: repeat(2, 1fr);
        gap: 12px;

        .pin-card {
          padding: 12px;
          background: #F5F7FA;
          border-radius: 8px;
          border-left: 3px solid #409EFF;

          .pin-card-header {
            display: flex;
            align-items: center;
            gap: 8px;
            margin-bottom: 8px;

            .pin-dot {
              width: 8px;
              height: 8px;
              border-radius: 50%;

              &.input {
                background: #67C23A;
                box-shadow: 0 0 6px rgba(103, 194, 58, 0.6);
              }

              &.output {
                background: #E6A23C;
                box-shadow: 0 0 6px rgba(230, 162, 60, 0.6);
              }
            }

            .pin-label {
              flex: 1;
              font-size: 13px;
              font-weight: 600;
              color: #303133;
            }
          }

          .pin-card-body {
            font-size: 11px;
            color: #909399;
            display: flex;
            gap: 12px;

            span {
              display: flex;
              align-items: center;

              i {
                margin-right: 4px;
              }
            }
          }
        }
      }
    }
  }
}

.avatar-uploader {
  .image-container {
    position: relative;
    display: inline-block;

    &:hover .delete-btn {
      display: block;
    }
  }

  .delete-btn {
    position: absolute;
    top: -8px;
    right: -8px;
    width: 20px;
    height: 20px;
    border-radius: 50%;
    background-color: #f56c6c;
    color: white;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    z-index: 10;
    border: 1px solid white;
    display: none;

    &:hover {
      opacity: 0.9;
    }
  }

  .avatar-uploader-icon {
    font-size: 28px;
    color: #8c939d;
    width: 50px;
    height: 50px;
    line-height: 50px;
    text-align: center;
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
    cursor: pointer;

    &:hover {
      border-color: #409EFF;
    }
  }

  .avatar {
    width: 50px;
    height: 50px;
    display: block;
    border-radius: 6px;
    object-fit: cover;
  }
}

// 详情对话框中的图标样式
.detail-icon-img {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  object-fit: cover;
}

::v-deep .el-icon-close {
  position: relative;
  bottom: 10px;
}

// 拖拽样式
.sortable-ghost {
  opacity: 0.5;
  background-color: #f5f7fa !important;
}

.sortable-chosen {
  background-color: #ecf5ff !important;
}

.sortable-drag {
  opacity: 0.8;
  transform: rotate(5deg);
}

.drag-handle {
  cursor: move;
  transition: all 0.3s ease;
  
  &:hover {
    color: #409EFF !important;
    transform: scale(1.1);
  }
  
  &:active {
    color: #67C23A !important;
  }
}

// 确保表格行可以拖拽
::v-deep .el-table__body-wrapper tbody tr {
  cursor: move;
}

// 拖拽时的视觉反馈
::v-deep .el-table__body-wrapper tbody tr.sortable-ghost {
  td {
    background-color: #f5f7fa !important;
    border: 2px dashed #409EFF !important;
  }
}
</style>
