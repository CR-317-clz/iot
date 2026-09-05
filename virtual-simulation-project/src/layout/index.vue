<template>
  <div class="layout-container">
    <el-container class="main-container">
      <el-aside :width="isCollapse ? '64px' : '200px'" :class="['sidebar', { 'sidebar-collapsed': isCollapse }]">
        <div class="logo-area">
          <h3>数字大屏</h3>
        </div>
        <el-menu :default-active="activeMenu" class="el-menu-vertical-demo" @open="handleOpen" @close="handleClose"
          @select="handleMenuSelect" :collapse="isCollapse" background-color="transparent" text-color="#b3c0d1"
          active-text-color="#409EFF">
          <!-- 设备管理：控制设备的开关、设置设备的参数等等 -->
          <el-submenu index="device-management">
            <template slot="title">
              <i class="el-icon-monitor"></i>
              <span>设备管理</span>
            </template>

            <el-menu-item index="device-management">
              <i class="el-icon-data-analysis"></i>
              <span>设备信息管理</span>
            </el-menu-item>

            <el-menu-item index="device-camera">
              <i class="el-icon-data-analysis"></i>
              <span>摄像头管理</span>
            </el-menu-item>
          </el-submenu>

          
          <!-- 协议配置 -->
          <el-menu-item index="device-protocol">
            <i class="el-icon-time"></i>
            <span>协议配置</span>
          </el-menu-item>
        

          <!-- 报错记录：查看设备的报错信息，进行故障排查 -->
          <el-menu-item index="big-screen">
            <i class="el-icon-data-analysis"></i>
            <span slot="title">数字大屏</span>
          </el-menu-item>
      
          <!-- 视频监控：实时监控设备周围环境的情况 -->
          <!-- <el-menu-item index="video-monitor">
            <i class="el-icon-picture-outline"></i>
            <span slot="title">视频监控</span>
          </el-menu-item> -->
        </el-menu>

        <div class="collapse-btn" @click="toggleCollapse">
          <i :class="isCollapse ? 'el-icon-s-unfold' : 'el-icon-s-fold'"></i>
        </div>
      </el-aside>

      <el-container class="content-container">
        <el-header class="header">
          <div class="header-left">
            <h2>智慧物联网监测平台</h2>
          </div>
          <div class="header-right">
            <span class="time">{{ currentTime }}</span>
            <el-dropdown>
              <span class="user-info">
                <i class="el-icon-user-solid"></i>
                {{ userRole }}
                <i class="el-icon-arrow-down el-icon--right"></i>
              </span>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item @click.native="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </div>
        </el-header>

        <el-main class="main-content">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>
<script>
export default {
  data() {
    return {
      isCollapse: false,
      currentTime: "",
      timer: null,
      activeMenu: "device-monitor", // 默认激活设备监控
      userRole: localStorage.getItem('role') || 'guest', // 获取用户角色
    };
  },
  mounted() {
    this.updateTime();
    this.timer = setInterval(this.updateTime, 1000);
    // 根据当前路由设置激活菜单
    this.setActiveMenu();
  },
  beforeDestroy() {
    if (this.timer) {
      clearInterval(this.timer);
    }
  },
  watch: {
    // 监听路由变化，更新激活菜单
    $route(to) {
      console.log(to)
      this.setActiveMenu();
    },
    // 监听用户角色变化
    '$store.getters.isLoggedIn'(newVal) {
      if (!newVal) {
        // 用户已退出登录，重置用户角色
        this.userRole = 'guest';
      } else {
        // 用户已登录，从 localStorage 获取角色
        this.userRole = localStorage.getItem('role') || 'guest';
      }
    }
  },
  methods: {
    handleOpen(key, keyPath) {
      console.log(key, keyPath);
    },
    handleClose(key, keyPath) {
      console.log(key, keyPath);
    },
    toggleCollapse() {
      this.isCollapse = !this.isCollapse;
    },
    updateTime() {
      const now = new Date();
      this.currentTime = now.toLocaleString("zh-CN", {
        year: "numeric",
        month: "2-digit",
        day: "2-digit",
        hour: "2-digit",
        minute: "2-digit",
        second: "2-digit",
      });
    },
    // 处理菜单选择
    handleMenuSelect(key) {
      this.activeMenu = key;
      // 特殊处理系统设置子菜单
      if (key.startsWith('5-')) {
        this.$message.info('系统设置功能开发中...');
        return;
      }
      this.$router.push(`/layout/${key}`);
    },
    // 根据当前路由设置激活菜单
    setActiveMenu() {
      const routeName = this.$route.name;
      if (routeName) {
        // 如果是设备详情页面，保持设备管理菜单激活
        if (routeName === 'device-data-detail') {
          this.activeMenu = 'device-management';
        } else {
          this.activeMenu = routeName;
        }
      }
    },
    // 处理退出登录
    handleLogout() {
      this.$confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // 调用 store 的 logout action 清除登录数据
        this.$store.dispatch('logout');

        // 显示退出成功消息
        this.$message.success('退出登录成功');

        // 跳转到登录页
        this.$router.push('/login');
      }).catch(() => {
        // 用户取消退出
      });
    },
  },
};
</script>

<style lang="scss" scoped>
.layout-container {
  width: 100vw;
  height: 100vh;
  background: #ffffff;
  overflow: hidden;

  .main-container {
    height: 100%;

    .sidebar {
      background: #f5f7fa;
      border-right: 1px solid #e4e7ed;
      position: relative;
      transition: width 0.3s ease;

      &.sidebar-collapsed {
        .logo-area h3 {
          opacity: 0;
        }

        .el-menu-vertical-demo {

          .el-menu-item,
          .el-submenu__title {
            margin: 5px 5px;
            padding: 0 !important;
            text-align: center;

            i {
              margin-right: 0;
              font-size: 18px;
            }
          }
        }
      }

      .logo-area {
        padding: 20px 15px;
        text-align: center;
        border-bottom: 1px solid #e4e7ed;
        overflow: hidden;
        white-space: nowrap;

        h3 {
          color: #303133;
          margin: 0;
          font-size: 18px;
          font-weight: 600;
          transition: opacity 0.3s ease;
        }
      }

      .el-menu-vertical-demo {
        border: none;
        padding-top: 10px;

        .el-menu-item,
        .el-submenu__title {
          height: 50px;
          line-height: 50px;
          margin: 5px 10px;
          border-radius: 8px;
          transition: all 0.3s ease;

          &:hover {
            background: rgba(64, 158, 255, 0.1) !important;
            color: #409eff !important;
          }

          &.is-active {
            background: rgba(64, 158, 255, 0.15) !important;
            color: #409eff !important;
            box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
          }

          i {
            margin-right: 10px;
            font-size: 16px;
            width: 20px;
            text-align: center;
          }
        }
      }

      .collapse-btn {
        position: absolute;
        bottom: 20px;
        left: 50%;
        transform: translateX(-50%);
        width: 40px;
        height: 40px;
        background: #f0f2f5;
        border: 1px solid #d9d9d9;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        transition: all 0.3s ease;

        &:hover {
          background: #e6f7ff;
          border-color: #409eff;
        }

        i {
          color: #606266;
          font-size: 18px;
        }
      }
    }

    .content-container {
      .header {
        background: #ffffff;
        border-bottom: 1px solid #e4e7ed;
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 0 30px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);

        .header-left {
          h2 {
            margin: 0;
            color: #333;
            font-size: 24px;
            font-weight: 600;
            background: linear-gradient(45deg, #667eea, #764ba2);
            background-clip: text;
            -webkit-background-clip: text;
            color: transparent;
          }
        }

        .header-right {
          display: flex;
          align-items: center;
          gap: 20px;

          .time {
            color: #666;
            font-size: 14px;
            font-family: "Courier New", monospace;
          }

          .user-info {
            cursor: pointer;
            color: #333;
            font-size: 14px;
            display: flex;
            align-items: center;
            gap: 5px;
            padding: 8px 12px;
            border-radius: 6px;
            transition: all 0.3s ease;

            &:hover {
              background: rgba(102, 126, 234, 0.1);
              color: #667eea;
            }

            .el-icon-user-solid {
              font-size: 16px;
            }
          }
        }
      }

      .main-content {
        background: #f5f7fa;
        padding: 20px;
        overflow: auto;

        &::-webkit-scrollbar {
          width: 6px;
        }

        &::-webkit-scrollbar-track {
          background: #000;
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
  }
}

// 响应式设计
@media (max-width: 768px) {
  .layout-container {
    .main-container {
      .sidebar {
        width: 60px !important;

        .logo-area h3 {
          font-size: 12px;
        }
      }

      .header {
        padding: 0 15px;

        .header-left h2 {
          font-size: 18px;
        }

        .header-right {
          gap: 10px;

          .time {
            display: none;
          }
        }
      }

      .main-content {
        padding: 10px;
      }
    }
  }
}
</style>
