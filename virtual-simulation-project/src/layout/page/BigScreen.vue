<template>
  <div class="big-screen-entrance">
    <el-card class="page-card">
      <div slot="header" class="page-header">
        <span class="page-title">
          <i class="el-icon-data-analysis"></i>
          数字大屏
        </span>
        <p class="page-desc">点击进入对应区域的数字大屏</p>
        <div class="search-bar">
          <el-input
            v-model="search"
            placeholder="搜索大屏名称"
            clearable
            prefix-icon="el-icon-search"
            size="small"
            style="width: 220px"
          />
        </div>
      </div>
      <div class="content" v-loading="loading" element-loading-text="正在加载设备列表...">
        <el-row :gutter="20" v-if="!loading && screens.length > 0">
          <el-col :span="6" v-for="(screen) in filteredScreens" :key="screen.id">
            <el-card class="screen-card" shadow="hover">
              <div class="screen-content">
                <div class="screen-icon" :class="screen.status">
                  <i :class="screen.icon"></i>
                </div>
                <el-tag
                  :type="screen.status === 'online' ? 'success' : 'danger'"
                  class="screen-status"
                  size="mini"
                >
                  {{ screen.status === 'online' ? '在线' : '离线' }}
                </el-tag>
                <div class="screen-info">
                  <h4>{{ '数智农业驾驶舱(' + (screen.deviceCode) + ')' }}</h4>
                  <div class="screen-desc">{{ screen.desc }}</div>
                  <el-button
                    type="primary"
                    size="mini"
                    :disabled="screen.status !== 'online'"
                    @click="enterScreen(screen.id)"
                    class="enter-btn"
                  >
                    进入大屏
                  </el-button>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
        
        <!-- 空状态 -->
        <div v-if="!loading && screens.length === 0" class="empty-state">
          <i class="el-icon-data-analysis empty-icon"></i>
          <p>暂无可用的数字农业沙盘驾驶舱设备</p>
          <el-button type="primary" size="small" @click="fetchScreenList">
            <i class="el-icon-refresh"></i>
            重新加载
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import { getAllDeviceList } from "@/api/iot-device";
import { mapGetters, mapActions } from 'vuex';

export default {
  name: "BigScreenEntrance",
  data() {
    return {
      search: "",
      screens: [],
      loading: false
    };
  },
  computed: {
    ...mapGetters(['getToken', 'getUserId', 'getDeviceList']),
    filteredScreens() {
      if (!this.search) return this.screens;
      return this.screens.filter(s =>
        s.name.includes(this.search) || s.desc.includes(this.search)
      );
    },
  },
  mounted() {
    this.fetchScreenList();
  },
  methods: {
    ...mapActions(['setSelectedScreen', 'setScreenList', 'setDeviceList', 'setDeviceById']),
    
    // 获取大屏列表数据（与设备管理页使用相同接口）
    async fetchScreenList() {
      this.loading = true;
      try {
        const response = await getAllDeviceList();
        if (response.data?.code === 200) {
          const rows = response.data.rows || [];
          
          // 保存原始设备数据到store（与设备管理页格式一致）
          this.setDeviceList(rows);
          
          // 打印第一条数据，用于检查后端返回的字段结构
          if (rows.length > 0) {
            console.log("🔍 数字大屏-后端返回的第一条设备数据结构:", rows[0]);
            console.log("📋 数字大屏-可用字段列表:", Object.keys(rows[0]));
          }
          
          // 将API数据转换为页面需要的格式（字段映射与设备管理页一致）
          this.screens = rows.map(item => ({
            id: item.deviceId,
            name: item.deviceName || `设备${item.deviceId}`,
            desc: `${item.deviceName || item.deviceCode || '未知设备'} - ${item.deviceType || '未知类型'}`,
            icon: this.getDeviceIcon(item.deviceType),
            status: this.normalizeStatus(item.status),
            deviceName: item.deviceName,
            deviceCode: item.deviceCode,
            deviceId: item.deviceId,
            configId: item.deviceId,
            host: item.host,
            topic: item.topic,
            hardwareType: item.deviceType,
            deviceType: item.deviceType,
            deviceAddr: item.deviceAddr,
            location: item.location,
            protocolId: item.protocolId,
            relayProtocolId: item.relayProtocolId,
            createTime: item.createTime,
            updateTime: item.updateTime,
            rawData: item
          }));
          
          this.setScreenList(this.screens);
          console.log('📊 数字大屏-加载的设备数据:', this.screens);
        } else {
          const errorMsg = response.data?.msg || response.data?.message || "获取设备列表失败";
          this.$message.warning(errorMsg);
          this.generateMockData();
        }
      } catch (error) {
        console.error('获取设备列表出错:', error);
        const errorMsg = error.response?.data?.msg || error.message || "网络错误";
        this.$message.error(`获取设备列表出错: ${errorMsg}`);
        this.generateMockData();
      } finally {
        this.loading = false;
      }
    },
    
    // 状态标准化（与设备管理页一致）
    normalizeStatus(status) {
      if (status === 1 || status === "1" || status === "online") {
        return "online";
      } else if (status === 0 || status === "0" || status === "offline") {
        return "offline";
      }
      return "offline"; // 默认离线
    },
    
    // 根据设备类型获取图标
    getDeviceIcon(hardwareType) {
      const iconMap = {
        mqtt01: 'el-icon-monitor',
        mqtt02: 'el-icon-cpu',
        mqtt03: 'el-icon-thermometer',
        mqtt04: 'el-icon-heavy-rain',
        mqtt05: 'el-icon-camera-solid',
        mqtt06: 'el-icon-data-analysis',
        mqtt07: 'el-icon-position',
        mqtt08: 'el-icon-warning',
        mqtt09: 'el-icon-cpu',
        mqtt10: 'el-icon-monitor',
        mqtt11: 'el-icon-thermometer',
        mqtt12: 'el-icon-heavy-rain',
        mqtt13: 'el-icon-camera-solid',
        mqtt14: 'el-icon-data-analysis'
      };
      return iconMap[hardwareType] || 'el-icon-data-analysis';
    },
    
    // 生成模拟数据（API失败时使用）
    generateMockData() {
      this.screens = [
        { id: '1', name: '智慧社区A区', desc: '温湿度环境监控大屏', icon: 'el-icon-data-analysis', status: 'online', deviceName: 'Community_A_Monitor', configId: 'config_001' },
        { id: '2', name: '智慧社区B区', desc: '安防监控大屏', icon: 'el-icon-monitor', status: 'online', deviceName: 'Community_B_Security', configId: 'config_002' },
        { id: '3', name: '智慧社区C区', desc: '环境质量监测大屏', icon: 'el-icon-cpu', status: 'offline', deviceName: 'Community_C_Environment', configId: 'config_003' },
        { id: '4', name: '智慧社区D区', desc: '综合管理大屏', icon: 'el-icon-data-analysis', status: 'online', deviceName: 'Community_D_Management', configId: 'config_004' }
      ];
    },
    
    enterScreen(screenId) {
      const selectedScreen = this.screens.find(screen => screen.id === screenId);
      
      if (!selectedScreen) {
        this.$message.error('未找到对应的大屏信息');
        return;
      }

      // 保存选中的大屏信息到store（包括deviceId和relayProtocolId）
      this.setSelectedScreen(selectedScreen);
      this.setDeviceById({
        deviceName: selectedScreen.deviceName,
        deviceId: selectedScreen.deviceId,
        relayProtocolId: selectedScreen.relayProtocolId,
        configId: selectedScreen.configId
      });
      
      console.log('✅ 选中大屏信息（包含deviceId和relayProtocolId）:', {
        deviceId: selectedScreen.deviceId,
        deviceName: selectedScreen.deviceName,
        relayProtocolId: selectedScreen.relayProtocolId,
        configId: selectedScreen.configId,
        hardwareType: selectedScreen.hardwareType,
        host: selectedScreen.host
      });
      
      // 跳转到数字农业沙盘驾驶舱页面，传递deviceId和relayProtocolId
      this.$router.push({ 
        path: `/`, 
        query: { 
          screenId,
          deviceId: selectedScreen.deviceId,
          deviceName: selectedScreen.deviceName,
          relayProtocolId: selectedScreen.relayProtocolId,
          configId: selectedScreen.configId
        }
      });
    },
  },
};
</script>

<style lang="scss" scoped>
.big-screen-entrance {
  .page-card {
    .page-header {
      position: relative;
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
      .search-bar {
        position: absolute;
        right: 0;
        top: 10px;
      }
    }
  }
  .content {
    .screen-card {
      margin-bottom: 28px; // 卡片下间距
      border-radius: 14px;
      transition: box-shadow 0.2s;
      &:hover {
        box-shadow: 0 4px 18px rgba(64, 158, 255, 0.18);
      }
      .screen-content {
        display: flex;
        align-items: center;
        gap: 16px;
        position: relative;
        .screen-icon {
          width: 50px;
          height: 50px;
          border-radius: 10px;
          display: flex;
          align-items: center;
          justify-content: center;
          background: linear-gradient(45deg, #409EFF, #67C23A);
          &.offline {
            background: linear-gradient(45deg, #F56C6C, #E6A23C);
          }
          i {
            font-size: 28px;
            color: #fff;
          }
        }
        .screen-status {
          position: absolute;
          top: 0;
          right: 0;
          z-index: 2;
        }
        .screen-info {
          flex: 1;
          h4 {
            margin: 0 0 8px 0;
            color: #303133;
            font-size: 16px;
            font-weight: 600;
          }
          .screen-desc {
            color: #909399;
            font-size: 13px;
            margin-bottom: 8px;
          }
          .enter-btn {
            transition: box-shadow 0.2s, transform 0.2s;
            &:hover {
              box-shadow: 0 2px 8px #409eff44;
              transform: translateY(-2px) scale(1.05);
            }
          }
        }
      }
    }
    
    // 空状态样式
    .empty-state {
      text-align: center;
      padding: 60px 20px;
      color: #909399;
      .empty-icon {
        font-size: 64px;
        color: #C0C4CC;
        margin-bottom: 16px;
      }
      p {
        font-size: 16px;
        margin-bottom: 20px;
      }
    }
  }
}
</style>