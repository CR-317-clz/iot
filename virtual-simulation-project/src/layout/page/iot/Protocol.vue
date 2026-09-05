<template>
  <div class="device-protocol">
    <div class="protocol-container">
      <!-- 顶部标题栏 -->
      <div class="page-header">
        <div class="header-content">
          <div class="header-left">
            <span class="page-title">
              <i class="el-icon-setting"></i>
              协议配置管理
            </span>
            <p class="page-subtitle">管理系统中各类设备的通信协议配置</p>
          </div>
          <div class="header-actions">
            <el-button 
              class="add-btn" 
              type="primary" 
              icon="el-icon-plus" 
              @click="handleAdd"
            >
              新增协议
            </el-button>
            <el-button 
              class="refresh-btn" 
              type="primary" 
              icon="el-icon-refresh" 
              @click="refreshAreaList"
            >
              刷新列表
            </el-button>
          </div>
        </div>
      </div>

      <!-- 筛选和搜索区域 -->
      <div class="filter-search-container">
        <div class="protocol-filter-container">
          <div class="filter-label">协议类型:</div>
          <div class="filter-buttons">
            <el-button 
              :type="protocolTypeFilter === '' ? 'primary' : ''" 
              @click="filterByProtocolType('')"
              size="small"
            >
              全部
            </el-button>
            <el-button 
              :type="protocolTypeFilter === 'MQTT' ? 'primary' : ''" 
              @click="filterByProtocolType('MQTT')"
              size="small"
            >
              MQTT
            </el-button>
            <el-button 
              :type="protocolTypeFilter === 'TCP' ? 'primary' : ''" 
              @click="filterByProtocolType('TCP')"
              size="small"
            >
              TCP
            </el-button>
          </div>
        </div>

        <!-- 搜索区域 -->
        <div class="search-container">
          <el-input
            v-model="search"
            placeholder="请输入协议名称或说明进行搜索"
            clearable
            class="search-input"
            @keyup.enter.native="handleSearch"
          >
            <i slot="prefix" class="el-input__icon el-icon-search"></i>
          </el-input>
          <el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button>
        </div>
      </div>

      <!-- 协议卡片区域 -->
      <div class="cards-container">
        <el-row :gutter="24">
          <el-col 
            v-for="item in filteredAreaList" 
            :key="item.protocolId" 
            :xs="24" 
            :sm="12" 
            :md="12" 
            :lg="8" 
            :xl="6"
            class="card-col"
          >
            <el-card class="protocol-card" shadow="hover">
              <div slot="header" class="card-header">
                <div class="card-header-top">
                  <div class="protocol-icon" :class="`protocol-icon-${getProtocolTypeLabel(item).toLowerCase()}`">
                    <i :class="getProtocolTypeLabel(item) === 'MQTT' ? 'el-icon-connection' : 'el-icon-link'"></i>
                  </div>
                  <div class="card-header-info">
                    <div class="protocol-name" :title="item.protocolName">
                      {{ item.protocolName || '-' }}
                    </div>
                    <div class="protocol-type-label">
                      {{ getProtocolTypeLabel(item) }} 协议
                    </div>
                  </div>
                  <el-tag 
                    :type="item.status === '1' ? 'success' : 'info'" 
                    size="small"
                    class="status-tag"
                    effect="dark"
                  >
                    <i :class="item.status === '1' ? 'el-icon-check' : 'el-icon-minus'"></i>
                    {{ item.status === '1' ? '启用' : '停用' }}
                  </el-tag>
                </div>
              </div>
              
              <div class="card-body">
                <div class="info-item">
                  <div class="info-header">
                    <i class="el-icon-document"></i>
                    <span class="info-label">协议说明</span>
                  </div>
                  <div class="info-value" :title="item.description">
                    {{ item.description || '-' }}
                  </div>
                </div>
                
                <div class="info-item">
                  <div class="info-header">
                    <i class="el-icon-setting"></i>
                    <span class="info-label">配置内容</span>
                  </div>
                  <div class="info-value config-content">
                    {{ formatConfig(item.configJson) }}
                  </div>
                </div>
                
                <div class="info-item" v-if="item.remark">
                  <div class="info-header">
                    <i class="el-icon-edit-outline"></i>
                    <span class="info-label">备注信息</span>
                  </div>
                  <div class="info-value" :title="item.remark">
                    {{ item.remark }}
                  </div>
                </div>
                
                <div class="info-item info-item-time">
                  <i class="el-icon-time"></i>
                  <span class="info-time-text">{{ formatTime(item.createTime) || '-' }}</span>
                </div>
              </div>
              
              <div class="card-footer">
                <el-button 
                  type="primary" 
                  size="small" 
                  @click="handleEdit(item)"
                  icon="el-icon-edit"
                  class="footer-btn"
                >
                  编辑
                </el-button>
                <el-button 
                  type="danger" 
                  size="small" 
                  @click="handleDelete(item)"
                  icon="el-icon-delete"
                  class="footer-btn"
                >
                  删除
                </el-button>
              </div>
            </el-card>
          </el-col>
          
          <el-col v-if="filteredAreaList.length === 0" :span="24">
            <div class="no-data">
              <div class="no-data-icon">
                <i class="el-icon-box"></i>
              </div>
              <p class="no-data-text">{{ loading ? '数据加载中...' : '暂无协议配置数据' }}</p>
              <p class="no-data-hint" v-if="!loading">点击下方按钮创建您的第一个协议配置</p>
              <el-button 
                v-if="!loading" 
                type="primary" 
                @click="handleAdd" 
                icon="el-icon-plus"
                size="medium"
                class="no-data-btn"
              >
                新增协议
              </el-button>
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
            class="protocol-dialog"
            :modal-append-to-body="false"
        >
          <el-form 
            :model="protocolFormData" 
            :rules="formRules" 
            ref="protocolForm" 
            label-width="120px"
            class="protocol-form"
          >
            <el-form-item label="协议名称" prop="protocolName">
              <el-input
                  v-model="protocolFormData.protocolName"
                  placeholder="请输入协议名称"
                  clearable
                  class="form-input"
              />
            </el-form-item>

            <el-form-item label="协议说明" prop="description">
              <el-input
                  v-model="protocolFormData.description"
                  type="textarea"
                  :rows="3"
                  placeholder="请输入协议说明"
                  clearable
                  class="form-textarea"
              />
            </el-form-item>

            <el-form-item label="协议类型" prop="protocolType">
              <el-select 
                v-model="protocolFormData.protocolType" 
                placeholder="请选择协议类型" 
                class="form-input"
                @change="handleProtocolTypeChange"
              >
                <el-option label="MQTT" value="MQTT"></el-option>
                <el-option label="TCP" value="TCP"></el-option>
              </el-select>
            </el-form-item>

            <el-form-item label="配置内容" class="protocol-config-section">
              <div class="config-form">
                <el-form-item label="主机地址" prop="protocolConfig.host" class="config-item">
                  <el-input
                      v-model="protocolFormData.protocolConfig.host"
                      placeholder="例如: tcp://192.168.1.109:1883"
                      clearable
                      class="form-input config-input"
                  />
                </el-form-item>

                <el-form-item 
                  label="主题" 
                  prop="protocolConfig.topic" 
                  class="config-item"
                >
                  <el-input
                      v-model="protocolFormData.protocolConfig.topic"
                      placeholder="例如: 1"
                      clearable
                      class="form-input config-input"
                  />
                </el-form-item>
              </div>
            </el-form-item>

            <el-form-item label="使用状态" prop="status">
              <el-radio-group v-model="protocolFormData.status" class="form-radio-group">
                <el-radio label="1">已启用</el-radio>
                <el-radio label="2">未启用</el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="备注" prop="remark">
              <el-input
                  v-model="protocolFormData.remark"
                  type="textarea"
                  :rows="2"
                  placeholder="请输入备注信息"
                  clearable
                  class="form-textarea"
              />
            </el-form-item>
          </el-form>

          <template #footer>
            <div class="dialog-footer">
              <el-button @click="handleDialogClose" class="cancel-btn">取消</el-button>
              <el-button type="primary" @click="handleConfirm" class="confirm-btn">确定</el-button>
            </div>
          </template>
        </el-dialog>
    </div>
  </div>
</template>

<script>
import {mapActions} from "vuex";
import {getAllDeviceProtocolList, updateDeviceProtocol, addDeviceProtocol, deleteDeviceProtocol} from "@/api/iot-protocol";

export default {
  name: "Protocol",
  data() {
    return {
      search: "",
      protocolTypeFilter: "", // 协议类型筛选
      screens: [],
      loading: false,
      areaList: [],
      listTotal: 0,
      error: "",

      dialogFormVisible: false,
      dialogTitle: "编辑协议配置",
      isEdit: false,
      protocolFormData: {
        protocolName: '',
        description: '',
        protocolType: 'MQTT', // 默认协议类型
        configJson: '',
        protocolConfig: {
          host: '',
          topic: ''
        },
        status: '1',
        remark: ''
      },
      formRules: {
        protocolName: [
          { required: true, message: '请输入协议名称', trigger: 'blur' }
        ],
        description: [
          { required: true, message: '请输入协议说明', trigger: 'blur' }
        ],
        protocolType: [
          { required: true, message: '请选择协议类型', trigger: 'change' }
        ],
        protocolConfig: {
          host: [
            { required: true, message: '请输入主机地址', trigger: 'blur' }
          ],
          topic: [
            { required: true, message: '请输入主题', trigger: 'blur' }
          ]
        }
      }
    };
  },
  computed: {
    filteredAreaList() {
      // 先根据协议类型筛选
      let filteredList = this.areaList;
      if (this.protocolTypeFilter) {
        filteredList = this.areaList.filter(item => {
          // 优先使用 protocolType 字段
          if (item.protocolType) {
            return item.protocolType === this.protocolTypeFilter;
          }
          // 如果没有 protocolType 字段，默认显示在所有类型中
          return true;
        });
      }

      // 再根据搜索关键词筛选
      if (!this.search) {
        return filteredList;
      }
      const searchLower = this.search.toLowerCase();
      return filteredList.filter(item => 
        (item.protocolName && item.protocolName.toLowerCase().includes(searchLower)) ||
        (item.description && item.description.toLowerCase().includes(searchLower))
      );
    }
  },
  created() {
    this.loadAreaList();
  },

  methods: {
    ...mapActions(["setDeviceList", "setDeviceById"]),

    /** 加载区域设备列表 */
    async loadAreaList() {
      this.loading = true;
      this.error = "";
      try {
        const response = await getAllDeviceProtocolList();

        if (response.data?.code === 200) {
          this.areaList = response.data.rows || [];
          this.listTotal = this.areaList.length;
          
          // 调试输出：查看返回的第一条数据结构
          if (this.areaList.length > 0) {
            console.log('协议列表第一条数据:', this.areaList[0]);
            console.log('configJson 类型:', typeof this.areaList[0].configJson);
            console.log('configJson 内容:', this.areaList[0].configJson);
          }
        } else {
          this.$message.warning("获取协议列表失败");
          this.error = "协议数据加载失败";
        }
      } catch (error) {
        console.error("获取协议列表出错:", error);
        this.$message.error(`获取协议列表出错: ${error.message || "网络错误"}`);
        this.error = "协议数据加载失败";
      }
      this.loading = false;
    },

    /** 搜索处理 */
    handleSearch() {
      // 搜索逻辑已在 computed 中实现
    },

        /** 格式化时间 */
    formatTime(timestamp) {
      if (!timestamp) return '-';
      const date = new Date(timestamp);
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      });
    },

    /** 格式化配置内容 */
    formatConfig(configJson) {
      if (!configJson) return '-';
      try {
        // 如果已经是对象，直接使用
        let config = configJson;
        if (typeof configJson === 'string') {
          config = JSON.parse(configJson);
        }
        
        // 检查多种可能的数据结构
        let protocolConfig = null;
        if (config.protocolConfig) {
          protocolConfig = config.protocolConfig;
        } else if (config.host || config.topic) {
          // 配置直接在根级别
          protocolConfig = config;
        }
        
        if (protocolConfig) {
          return `host: ${protocolConfig.host || '-'}\ntopic: ${protocolConfig.topic || '-'}`;
        }
        
        // 如果无法解析，返回原始JSON
        return typeof configJson === 'string' ? configJson : JSON.stringify(configJson);
      } catch (e) {
        return typeof configJson === 'string' ? configJson : JSON.stringify(configJson);
      }
    },

    /** 解析协议配置 */
    parseProtocolConfig(configJson) {
      const protocolConfig = { host: '', topic: '' };
      
      console.log('parseProtocolConfig 输入:', typeof configJson, configJson);
      
      if (!configJson) {
        console.warn('configJson 为空');
        return protocolConfig;
      }

      try {
        // 如果 configJson 已经是对象，直接使用
        let config = configJson;
        if (typeof configJson === 'string') {
          config = JSON.parse(configJson);
          console.log('解析后的 config:', config);
        }
        
        // 检查多种可能的数据结构
        if (config && config.protocolConfig) {
          console.log('找到 config.protocolConfig:', config.protocolConfig);
          return {
            host: config.protocolConfig.host || '',
            topic: config.protocolConfig.topic || ''
          };
        } else if (config && (config.host || config.topic)) {
          // 如果配置直接在根级别
          console.log('配置在根级别:', config);
          return {
            host: config.host || '',
            topic: config.topic || ''
          };
        } else {
          console.warn('未找到有效的协议配置结构，config:', config);
        }
      } catch (e) {
        console.error('解析协议配置失败:', e, 'configJson:', configJson);
      }
      
      return protocolConfig;
    },

    /** 编辑操作 */
    handleEdit(row) {
      this.isEdit = true;
      this.dialogTitle = "编辑协议配置";
      
      // 解析配置内容
      const protocolConfig = this.parseProtocolConfig(row.configJson);

      // 确定协议类型
      let protocolType = row.protocolType || 'MQTT'; // 优先使用 protocolType 字段，默认为MQTT

      console.log('编辑协议 - 原始数据:', row);
      console.log('解析后的 protocolConfig:', protocolConfig);
      console.log('协议类型:', protocolType);

      // 重置表单数据
      this.protocolFormData = {
        protocolId: row.protocolId,
        protocolName: row.protocolName || '',
        description: row.description || '',
        protocolType: protocolType,
        configJson: row.configJson || '',
        protocolConfig: {
          host: protocolConfig.host || '',
          topic: protocolConfig.topic || ''
        },
        status: row.status && (row.status === '1' || row.status === '2') ? row.status : '1',
        remark: row.remark || ''
      };

      console.log('设置后的 protocolFormData:', JSON.parse(JSON.stringify(this.protocolFormData)));

      // 使用 $nextTick 确保数据更新后再打开对话框
      this.$nextTick(() => {
        this.dialogFormVisible = true;
        // 再次确认表单数据，并清除验证状态
        this.$nextTick(() => {
          if (this.$refs.protocolForm) {
            this.$refs.protocolForm.clearValidate();
          }
          console.log('对话框打开后的 protocolFormData:', JSON.parse(JSON.stringify(this.protocolFormData)));
        });
      });
    },

    /** 新增操作 */
    handleAdd() {
      this.isEdit = false;
      this.dialogTitle = "新增协议配置";
      
      // 重置表单数据
      this.protocolFormData = {
        protocolId: undefined,
        protocolName: '',
        description: '',
        protocolType: 'MQTT',
        configJson: '',
        protocolConfig: {
          host: '',
          topic: ''
        },
        status: '1',
        remark: ''
      };
      
      this.$nextTick(() => {
        this.dialogFormVisible = true;
        // 清除验证状态
        this.$nextTick(() => {
          if (this.$refs.protocolForm) {
            this.$refs.protocolForm.clearValidate();
          }
        });
      });
    },

    /** 删除操作 */
    handleDelete(row) {
      this.$confirm(`确定要删除协议 "${row.protocolName}" 吗？`, '提示', {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        confirmButtonClass: 'el-button--danger'
      }).then(async () => {
        try {
          // 调用删除接口
          const response = await deleteDeviceProtocol(row.protocolId);
          
          // 判断响应成功 - 兼容两种格式：success 和 code
          const isSuccess = response.data?.success === true || response.data?.code === 200;
          
          if (isSuccess) {
            this.$message.success(
              response.data?.message || 
              response.data?.msg || 
              '删除成功'
            );
            this.loadAreaList(); // 重新加载数据
          } else {
            this.$message.error(
              response.data?.message || 
              response.data?.msg || 
              '删除失败'
            );
          }
        } catch (error) {
          this.$message.error(
            `删除失败: ${error.response?.data?.message || error.message || "网络错误"}`
          );
        }
      }).catch(() => {
        this.$message.info('已取消删除');
      });
    },

    /** 对话框关闭处理 */
    handleDialogClose() {
      this.dialogFormVisible = false;
      this.$refs.protocolForm.resetFields();
      // 重置表单数据
      this.protocolFormData = {
        protocolId: undefined,
        protocolName: '',
        description: '',
        protocolType: 'MQTT',
        configJson: '',
        protocolConfig: {
          host: '',
          topic: ''
        },
        status: '1',
        remark: ''
      };
    },

    /** 确认保存 */
    handleConfirm() {
      this.$refs.protocolForm.validate(async (valid) => {
        if (valid) {
          try {
            // 构造配置内容 - 统一使用 topic
            const protocolConfig = {
              host: this.protocolFormData.protocolConfig.host,
              topic: this.protocolFormData.protocolConfig.topic
            };

            // 构造 configJson 字符串
            const configData = {
              protocolConfig: protocolConfig
            };
            const configJson = JSON.stringify(configData);

            let response;
            if (this.isEdit) {
              // 编辑操作 - 同时传递 configJson 和 protocolConfig
              response = await updateDeviceProtocol({
                protocolId: this.protocolFormData.protocolId,
                protocolName: this.protocolFormData.protocolName,
                description: this.protocolFormData.description,
                configJson: configJson,
                protocolConfig: protocolConfig,
                status: this.protocolFormData.status,
                protocolType: this.protocolFormData.protocolType,
                remark: this.protocolFormData.remark
              });
            } else {
              // 新增操作 - 同时传递 configJson 和 protocolConfig
              response = await addDeviceProtocol({
                protocolName: this.protocolFormData.protocolName,
                description: this.protocolFormData.description,
                configJson: configJson,
                protocolConfig: protocolConfig,
                status: this.protocolFormData.status,
                protocolType: this.protocolFormData.protocolType,
                remark: this.protocolFormData.remark
              });
            }

            // 判断响应成功 - 统一使用 success 字段
            const isSuccess = response.data?.success === true;
            
            if (isSuccess) {
              this.$message.success(
                response.data?.message || 
                response.data?.msg || 
                (this.isEdit ? "协议修改成功" : "协议新增成功")
              );
              this.dialogFormVisible = false;
              this.loadAreaList(); // 重新加载数据
            } else {
              this.$message.error(
                response.data?.message || 
                response.data?.msg || 
                (this.isEdit ? "协议修改失败" : "协议新增失败")
              );
            }
          } catch (err) {
            this.$message.error(
              (this.isEdit ? "协议修改失败" : "协议新增失败") + 
              "：" + (err.response?.data?.message || "服务器错误")
            );
          }
        } else {
          this.$message.warning("请填写必填项");
          return false;
        }
      });
    },

    /** 重新刷新区域 */
    refreshAreaList() {
      this.search = "";
      this.protocolTypeFilter = "";
      this.loadAreaList();
      this.$message.success("协议列表已刷新");
    },

    /** 协议类型变更处理 */
    handleProtocolTypeChange() {
      // 协议类型变更时不需要清空 topic
      // 因为统一使用 topic 字段
    },

    /** 根据协议类型筛选 */
    filterByProtocolType(protocolType) {
      this.protocolTypeFilter = protocolType;
    },

    /** 根据协议配置获取协议类型标签 */
    getProtocolTypeLabel(item) {
      // 优先使用 protocolType 字段
      if (item.protocolType) {
        return item.protocolType;
      }
      
      // 默认返回 MQTT
      return 'MQTT';
    }
  },
};
</script>

<style scoped>
.device-protocol {
  padding: 0;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
  min-height: calc(100vh - 60px);
}

.protocol-container {
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

.protocol-filter-container {
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
  animation: fadeIn 0.5s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.card-col {
  display: flex;
  margin-bottom: 24px;
}

.protocol-card {
  border-radius: 16px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: none;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #ffffff;
  min-height: 480px;
}

.protocol-card ::v-deep .el-card__body {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 0;
  min-height: 0;
}

.protocol-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.15);
}

.protocol-card:active {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.protocol-card ::v-deep .el-card__header {
  padding: 0;
  border: none;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.card-header {
  padding: 20px;
  position: relative;
  overflow: hidden;
}

.card-header::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 70%);
  pointer-events: none;
}

.card-header-top {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  position: relative;
  z-index: 1;
}

.protocol-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #ffffff;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  flex-shrink: 0;
  transition: all 0.3s ease;
}

.protocol-card:hover .protocol-icon {
  transform: rotate(360deg) scale(1.1);
}

.protocol-icon-mqtt {
  background: linear-gradient(135deg, rgba(52, 211, 153, 0.3), rgba(16, 185, 129, 0.3));
}

.protocol-icon-tcp {
  background: linear-gradient(135deg, rgba(96, 165, 250, 0.3), rgba(59, 130, 246, 0.3));
}

.card-header-info {
  flex: 1;
  min-width: 0;
}

.protocol-name {
  font-size: 18px;
  font-weight: 700;
  color: #ffffff;
  margin-bottom: 6px;
  line-height: 1.3;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.protocol-card:hover .protocol-name {
  text-shadow: 0 3px 6px rgba(0, 0, 0, 0.15);
  letter-spacing: 0.3px;
}

.protocol-type-label {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.85);
  font-weight: 500;
  line-height: 1.4;
  margin-top: 4px;
  display: flex;
  align-items: center;
  gap: 4px;
  letter-spacing: 0.5px;
}

.status-tag {
  height: auto;
  padding: 5px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  border: 2px solid rgba(255, 255, 255, 0.3);
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}

.status-tag i {
  font-size: 12px;
  font-weight: bold;
}

.card-body {
  padding: 20px;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 14px;
  background: #ffffff;
  min-height: 0;
  overflow: hidden;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 0 0 auto;
  transition: all 0.2s ease;
}

.info-item:hover {
  transform: translateX(2px);
}

.info-item:not(.info-item-time) {
  min-height: 56px;
}

.info-header {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #909399;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.info-header i {
  font-size: 14px;
  color: #409eff;
}

.info-label {
  font-size: 12px;
}

.info-value {
  font-size: 14px;
  color: #303133;
  line-height: 1.6;
  word-break: break-word;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  line-clamp: 3;
  -webkit-box-orient: vertical;
}

.config-content {
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  background: linear-gradient(135deg, #f6f8fb 0%, #f1f3f6 100%);
  padding: 12px;
  border-radius: 8px;
  font-size: 12px;
  border-left: 3px solid #409eff;
  white-space: pre-wrap;
  line-height: 1.8;
  color: #1f2937;
  box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.05);
  max-height: 120px;
  overflow: auto;
  word-break: break-all;
  transition: all 0.3s ease;
}

.config-content:hover {
  background: linear-gradient(135deg, #f1f3f6 0%, #e5e9ef 100%);
  box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.08);
}

.config-content::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

.config-content::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.05);
  border-radius: 3px;
}

.config-content::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 3px;
  transition: all 0.3s ease;
}

.config-content::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(135deg, #764ba2 0%, #667eea 100%);
}

.info-item-time {
  flex-direction: row;
  align-items: center;
  gap: 6px;
  padding-top: 8px;
  margin-top: auto;
  border-top: 1px solid #f0f2f5;
  font-size: 12px;
  color: #909399;
  flex-shrink: 0;
  transition: color 0.2s ease;
}

.info-item-time:hover {
  color: #606266;
}

.info-item-time i {
  font-size: 14px;
  color: #c0c4cc;
}

.info-time-text {
  font-size: 12px;
  color: #909399;
}

.card-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 16px 20px;
  border-top: 1px solid #f0f2f5;
  background: linear-gradient(to bottom, #fafbfc 0%, #f5f7fa 100%);
  margin-top: auto;
  flex-shrink: 0;
}

.footer-btn {
  border-radius: 8px;
  padding: 9px 16px;
  font-size: 13px;
  font-weight: 600;
  transition: all 0.3s ease;
  border: none;
}

.footer-btn.el-button--primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
}

.footer-btn.el-button--primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.footer-btn.el-button--danger {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: #fff;
  box-shadow: 0 2px 8px rgba(245, 87, 108, 0.3);
}

.footer-btn.el-button--danger:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(245, 87, 108, 0.4);
}

.no-data {
  text-align: center;
  padding: 100px 40px;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border-radius: 20px;
  margin: 10px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
}

.no-data-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 120px;
  height: 120px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  margin: 0 auto 24px;
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.3);
}

.no-data-icon i {
  font-size: 60px;
  color: #ffffff;
}

.no-data-text {
  font-size: 20px;
  margin: 0 0 12px;
  font-weight: 600;
  color: #2c3e50;
}

.no-data-hint {
  font-size: 14px;
  color: #7f8c8d;
  margin: 0 0 32px;
}

.no-data-btn {
  padding: 12px 32px;
  font-size: 15px;
  border-radius: 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.4);
  transition: all 0.3s ease;
}

.no-data-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.5);
}

.protocol-config-section ::v-deep .el-form-item__content {
  display: block;
}

.config-form {
  border: 1px solid #ebeef5;
  border-radius: 10px;
  padding: 24px;
  background: linear-gradient(120deg, #f9fafc 0%, #f0f2f5 100%);
}

.config-item {
  margin-bottom: 20px;
}

.config-item ::v-deep .el-form-item__label {
  font-weight: 500;
  color: #5d6d7e;
}

.config-input ::v-deep .el-input__inner {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
}

/* 对话框样式 */
.protocol-dialog ::v-deep .el-dialog {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

.protocol-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(120deg, #f8f9fa 0%, #ffffff 100%);
  border-bottom: 1px solid #eee;
  padding: 24px;
}

.protocol-dialog ::v-deep .el-dialog__title {
  font-weight: 700;
  font-size: 20px;
  color: #2c3e50;
}

.protocol-dialog ::v-deep .el-dialog__body {
  padding: 30px;
}

.protocol-dialog ::v-deep .el-dialog__footer {
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

/* 表单样式 */
.protocol-form {
  padding: 10px 0;
}

.protocol-form ::v-deep .el-form-item {
  margin-bottom: 26px;
}

.protocol-form ::v-deep .el-form-item__label {
  font-weight: 500;
  color: #2c3e50;
  padding-right: 12px;
  font-size: 14px;
}

.form-input, .form-textarea {
  width: 100%;
  transition: all 0.3s ease;
}

.protocol-form ::v-deep .el-select {
  width: 100%;
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
  .protocol-container {
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
  .protocol-container {
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
  
  .protocol-card {
    border-radius: 12px;
    min-height: 420px;
  }
  
  .card-header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .protocol-dialog ::v-deep .el-dialog {
    width: 95% !important;
    margin-top: 20px !important;
  }
}

/* 卡片网格对齐优化 */
@media (min-width: 1920px) {
  .cards-container {
    max-width: 1800px;
    margin: 0 auto;
  }
}

/* 卡片加载动画 */
@keyframes cardFadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.protocol-card {
  animation: cardFadeIn 0.5s ease-out;
}

.card-col:nth-child(1) .protocol-card { animation-delay: 0.05s; }
.card-col:nth-child(2) .protocol-card { animation-delay: 0.1s; }
.card-col:nth-child(3) .protocol-card { animation-delay: 0.15s; }
.card-col:nth-child(4) .protocol-card { animation-delay: 0.2s; }
.card-col:nth-child(5) .protocol-card { animation-delay: 0.25s; }
.card-col:nth-child(6) .protocol-card { animation-delay: 0.3s; }

/* 滚动条全局优化 */
* {
  scrollbar-width: thin;
  scrollbar-color: rgba(102, 126, 234, 0.5) rgba(0, 0, 0, 0.05);
}

*::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

*::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.03);
  border-radius: 4px;
}

*::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 4px;
  transition: all 0.3s ease;
}

*::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(135deg, #764ba2 0%, #667eea 100%);
}
</style>
