<template>
  <el-dialog :visible.sync="dialogVisible" title="设备配置" width="600px" :close-on-click-modal="false"
    :close-on-press-escape="false" :show-close="false">
    <div class="device-config">
      <el-alert title="配置设备参数" type="info" description="请根据设备类型填写相关配置信息，配置完成后才能进行硬件接线仿真。" :closable="false"
        style="margin-bottom: 20px">
      </el-alert>

      <el-form :model="config" label-width="120px" size="small">
        <!-- Zigbee配置 -->
        <template v-if="deviceType.includes('zigbee') || deviceType.includes('Zigbee')">
          <el-form-item label="设备类型" required>
            <el-select v-model="config.deviceType" placeholder="请选择设备类型" style="width: 100%">
              <el-option v-for="option in deviceTypeOptions" :key="option.value" :label="option.label"
                :value="option.value">
              </el-option>
            </el-select>
          </el-form-item>

          <el-form-item label="PANID" required>
            <el-input v-model="config.panId" placeholder="请输入PANID(例如:0010)" maxlength="4">
            </el-input>
          </el-form-item>

          <el-form-item label="信道" required>
            <el-input v-model="config.channel" placeholder="请选择配置信道(例如:12)" style="width: 100%">
            </el-input>
          </el-form-item>
        </template>

        <!-- WiFi配置 -->
        <template v-else-if="
          deviceType.includes('wifi') ||
          deviceType.includes('WiFi') ||
          deviceType.includes('WIFI')
        ">
          <el-form-item label="WiFi名称" required>
            <el-input v-model="config.wifiName" placeholder="请输入WiFi名称">
            </el-input>
          </el-form-item>

          <el-form-item label="WiFi密码" required>
            <el-input v-model="config.wifiPassword" placeholder="请输入WiFi密码" type="password">
            </el-input>
          </el-form-item>

          <el-form-item label="传输协议" required>
            <el-select v-model="config.protocol" placeholder="请选择传输协议" style="width: 100%">
              <el-option label="TCP" value="tcp"></el-option>
              <el-option label="UDP" value="udp"></el-option>
            </el-select>
          </el-form-item>

          <el-form-item label="工作模式" required>
            <el-select v-model="config.workMode" placeholder="请选择工作模式" style="width: 100%">
              <el-option label="SERVER" value="server"></el-option>
              <el-option label="CLIENT" value="client"></el-option>
            </el-select>
          </el-form-item>

          <el-form-item label="服务器IP" required>
            <el-input v-model="config.serverIp" placeholder="请输入服务器IP地址">
            </el-input>
          </el-form-item>

          <el-form-item label="端口" required>
            <el-input v-model="config.serverPort" placeholder="请输入服务器端口" type="number">
            </el-input>
          </el-form-item>
        </template>

        <!-- 蓝牙或LORA配置 -->
        <template v-else-if="
          deviceType.includes('蓝牙') ||
          deviceType.includes('bluetooth') ||
          deviceType.includes('BLE') ||
          deviceType.includes('lora') ||
          deviceType.includes('LoRa') ||
          deviceType.includes('LORA')
        ">
          <el-empty description="该设备暂时不需要配置" :image-size="100">
            <el-button type="primary" @click="handleNoConfigDevice">确定</el-button>
          </el-empty>
        </template>

        <!-- 4G/5G配置 -->
        <template v-else-if="deviceType.includes('4G') || deviceType.includes('5G')">
          <el-form-item label="服务器地址" required>
            <el-input v-model="config.serverIp" placeholder="请输入服务器地址">
            </el-input>
          </el-form-item>

          <el-form-item label="端口" required>
            <el-input v-model="config.serverPort" placeholder="请输入服务器端口" type="number">
            </el-input>
          </el-form-item>

          <el-form-item label="连接类型" required>
            <el-select v-model="config.protocol" placeholder="请选择连接类型" style="width: 100%">
              <el-option label="TCP" value="tcp"></el-option>
              <el-option label="UDP" value="udp"></el-option>
            </el-select>
          </el-form-item>
        </template>
      </el-form>
    </div>

    <div slot="footer" class="dialog-footer" v-if="
      !(
        deviceType.includes('蓝牙') ||
        deviceType.includes('bluetooth') ||
        deviceType.includes('BLE') ||
        deviceType.includes('lora') ||
        deviceType.includes('LoRa') ||
        deviceType.includes('LORA')
      )
    ">
      <el-button @click="handleCancel" size="medium">
        {{ config.configured ? "取消" : "稍后配置" }}
      </el-button>
      <el-button type="primary" @click="handleConfirm" :loading="connecting" size="medium">
        <i v-if="!connecting" class="el-icon-link"></i>
        {{ connecting ? "连接中..." : "确认并连接" }}
      </el-button>
    </div>
  </el-dialog>
</template>

<script>
export default {
  name: "DeviceConfigDialog",
  props: {
    visible: {
      type: Boolean,
      default: false,
    },
    deviceType: {
      type: String,
      required: true,
    },
    deviceTypeOptions: {
      type: Array,
      default: () => [],
    },
    connecting: {
      type: Boolean,
      default: false,
    },
    existingConfig: {
      type: Object,
      default: null,
    },
    uniqueIdentification: {
      type: String,
      default: "",
    },
  },
  data() {
    return {
      config: {
        deviceType: "",
        panId: "",
        channel: "",
        wifiName: "",
        wifiPassword: "",
        protocol: "",
        workMode: "",
        serverIp: "",
        serverPort: "",
        configured: false,
        connected: false,
        timeout: 5000,
        autoReconnect: true,
      },
    };
  },
  computed: {
    dialogVisible: {
      get() {
        return this.visible;
      },
      set(value) {
        this.$emit("update:visible", value);
      },
    },
  },
  watch: {
    visible(newValue) {
      if (newValue) {
        // 每次打开对话框时，确保使用传入的配置数据
        if (this.existingConfig) {
          // 深度复制现有配置，确保完全独立
          this.config = this.deepCloneConfig(this.existingConfig);
          // 如果现有配置中有uniqueIdentification，则设置
          if (this.existingConfig.uniqueIdentification) {
            this.uniqueIdentification =
              this.existingConfig.uniqueIdentification;
          }
        } else {
          // 重置为全新的配置，确保所有字段都有默认值
          this.config = this.getDefaultConfig();
          // 重置uniqueIdentification
          this.uniqueIdentification = "";
        }

        // 调试日志：显示当前配置状态
        console.log("对话框打开，当前配置:", this.config);
        console.log("uniqueIdentification:", this.uniqueIdentification);
      }
    },
  },
  methods: {
    // 获取默认配置对象
    getDefaultConfig() {
      return {
        deviceType: "",
        panId: "",
        channel: "",
        wifiName: "",
        wifiPassword: "",
        protocol: "",
        workMode: "",
        serverIp: "",
        serverPort: "",
        configured: false,
        connected: false,
        timeout: 5000,
        autoReconnect: true,
      };
    },

    // 深度复制配置对象，确保完全独立
    deepCloneConfig(config) {
      return JSON.parse(
        JSON.stringify({
          ...this.getDefaultConfig(),
          ...config,
        }),
      );
    },

    handleCancel() {
      if (!this.config.configured) {
        this.$confirm(
          "未配置设备将无法进行硬件仿真，确定要稍后配置吗？",
          "提示",
          {
            confirmButtonText: "稍后配置",
            cancelButtonText: "继续配置",
            type: "warning",
          },
        )
          .then(() => {
            this.dialogVisible = false;
            this.$emit("cancel");
          })
          .catch(() => { });
      } else {
        this.dialogVisible = false;
        this.$emit("cancel");
      }
    },
    handleNoConfigDevice() {
      this.dialogVisible = false;
      this.$emit("cancel");
    },
    validateIp(ip) {
      const ipRegex =
        /^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/;
      return ipRegex.test(ip);
    },
    handleConfirm() {
      // 验证表单
      if (
        this.deviceType.includes("wifi") ||
        this.deviceType.includes("WiFi") ||
        this.deviceType.includes("WIFI")
      ) {
        if (!this.config.wifiName) {
          this.$message.error("请输入WiFi名称");
          return;
        }
        if (!this.config.wifiPassword) {
          this.$message.error("请输入WiFi密码");
          return;
        }
        if (!this.config.protocol) {
          this.$message.error("请选择传输协议");
          return;
        }
        if (!this.config.workMode) {
          this.$message.error("请选择工作模式");
          return;
        }
        if (!this.config.serverIp) {
          this.$message.error("请输入服务器IP");
          return;
        }
        if (!this.validateIp(this.config.serverIp)) {
          this.$message.error("请输入正确的IP地址格式");
          return;
        }
        if (!this.config.serverPort) {
          this.$message.error("请输入服务器端口");
          return;
        }
        if (!/^\d{4}$/.test(this.config.serverPort)) {
          this.$message.error("端口必须为4位数");
          return;
        }
      } else if (
        this.deviceType.includes("zigbee") ||
        this.deviceType.includes("Zigbee")
      ) {
        if (!this.config.deviceType) {
          this.$message.error("请选择设备类型");
          return;
        }
        if (!this.config.panId) {
          this.$message.error("请输入PANID");
          return;
        }
        if (!/^\d{4}$/.test(this.config.panId)) {
          this.$message.error("PANID必须为4位数");
          return;
        }
        if (parseInt(this.config.panId) < 0) {
          this.$message.error("PANID不允许为负数");
          return;
        }
        if (!this.config.channel) {
          this.$message.error("请输入信道");
          return;
        }
        if (!/^\d{2}$/.test(this.config.channel)) {
          this.$message.error("信道必须为2位数");
          return;
        }
        if (parseInt(this.config.channel) < 0) {
          this.$message.error("信道不允许为负数");
          return;
        }
      } else if (
        this.deviceType.includes("4G") ||
        this.deviceType.includes("5G")
      ) {
        if (!this.config.serverIp) {
          this.$message.error("请输入服务器地址");
          return;
        }
        if (!this.validateIp(this.config.serverIp)) {
          this.$message.error("请输入正确的IP地址格式");
          return;
        }
        if (!this.config.serverPort) {
          this.$message.error("请输入服务器端口");
          return;
        }
        if (!/^\d{4}$/.test(this.config.serverPort)) {
          this.$message.error("端口必须为4位数");
          return;
        }
        if (!this.config.protocol) {
          this.$message.error("请选择连接类型");
          return;
        }
      }

      // 如果是Zigbee设备，需要传递uniqueIdentification参数和完整设备类型
      const emitData = {
        config: { ...this.config }, // 确保传递配置的副本
        uniqueIdentification: this.uniqueIdentification,
        deviceTypeOptions: this.deviceTypeOptions, // 传递设备类型选项用于查找完整的中文字符串
      };
      this.$emit("confirm", emitData);
    },
  },
};
</script>

<style scoped>
.device-config {
  padding: 0 20px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-left: 10px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  padding: 10px 20px 20px;
}
</style>
