<template>
  <div>
    <div class="login-bg"></div>
    <div class="login-container">
      <div class="login-card">
        <div class="login-header">
          <!-- <img
            src="https://img.icons8.com/fluency/48/000000/lock-2.png"
            alt="logo"
            class="login-logo"
          /> -->
          <h1>欢迎登录</h1>
          <p class="login-desc">数字农业沙盘驾驶舱</p>
        </div>
        <el-form
          ref="loginForm"
          :model="loginForm"
          :rules="rules"
          class="login-form"
          @submit.native.prevent
        >
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="用户名"
              prefix-icon="el-icon-user"
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              type="password"
              v-model="loginForm.password"
              placeholder="密码"
              prefix-icon="el-icon-lock"
              show-password
            />
          </el-form-item>
          <el-button type="primary" @click="handleLogin" class="login-btn"
            >登录</el-button
          >
        </el-form>
      </div>
    </div>
  </div>
</template>

<script>
import { loginApi } from "@/api/login";
export default {
  data() {
    return {
      loginForm: {
        username: "admin",
        password: "admin123",
      },
      rules: {
        username: [
          { required: true, message: "请输入用户名", trigger: "blur" },
        ],
        password: [{ required: true, message: "请输入密码", trigger: "blur" }],
      },
    };
  },
  created() {
    // 如果已登录，自动跳转到首页
    const token = localStorage.getItem("token");
    if (token) {
      this.$router.replace({ path: "/" });
    }
  },
  methods: {
    handleLogin() {
      this.$refs.loginForm.validate((valid) => {
        if (valid) {
          loginApi({
            username: this.loginForm.username,
            password: this.loginForm.password,
          })
            .then((res) => {
              if (res.data.code === 200) {
                this.$message.success(res.data.msg || "登录成功");
                // 保存到 localStorage
                localStorage.setItem("token", res.data.token);
                localStorage.setItem('role', res.data.role);
                localStorage.setItem('userId', res.data.userId);
                
                // 同步更新 Vuex store
                this.$store.commit('SET_TOKEN', res.data.token);
                this.$store.commit('SET_USER_ID', res.data.userId);
                
                this.$router.push({ path: "/layout" });
              } else {
                this.$message.error(res.data.msg || "登录失败");
              }
            })
            .catch((err) => {
              this.$message.error(
                "登录失败：" + (err.response?.data?.message || "服务器错误")
              );
            });
        } else {
          this.$message.error("请检查输入的用户名和密码");
          return false;
        }
      });
    },
  },
};
</script>

<style scoped lang="scss">
.login-bg {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url("~@/assets/login/bg.png") no-repeat center center;
  background-size: cover; // 让背景图铺满
  background-attachment: fixed; // 固定背景
  z-index: 0;
}

.login-container {
  position: relative;
  z-index: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
}

.login-card {
  width: 350px;
  padding: 40px 32px 32px 32px;
  background: rgba(255, 255, 255, 0.10); /* 提高透明度 */
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(102, 126, 234, 0.18);
  display: flex;
  flex-direction: column;
  align-items: center;
  animation: fadeInDown 0.7s;
  backdrop-filter: blur(12px); /* 毛玻璃效果 */
  border: 1px solid rgba(255, 255, 255, 0.18);

  @keyframes fadeInDown {
    from {
      opacity: 0;
      transform: translateY(-40px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }
}

.login-header {
  text-align: center;
  margin-bottom: 28px;

  .login-logo {
    width: 48px;
    height: 48px;
    margin-bottom: 8px;
  }
  h1 {
    font-size: 26px;
    color: #333;
    margin: 0;
    font-weight: 700;
    letter-spacing: 2px;
  }
  .login-desc {
    color: #888;
    font-size: 14px;
    margin-top: 10px;
    margin-bottom: 0;
    letter-spacing: 1px;
  }
}

.login-form {
  width: 100%;
  .el-form-item {
    margin-bottom: 22px;
  }
  .el-input__inner {
    height: 44px;
    font-size: 15px;
    border-radius: 8px;
  }
  .login-btn {
    width: 100%;
    height: 44px;
    font-size: 16px;
    border-radius: 8px;
    letter-spacing: 2px;
    background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
    border: none;
    box-shadow: 0 2px 8px rgba(102, 126, 234, 0.15);
    transition: background 0.3s;
    &:hover {
      background: linear-gradient(90deg, #764ba2 0%, #667eea 100%);
    }
  }
}
</style>
