import Vue from "vue";
import App from "./App.vue";
import router from "./router";
import store from "./store";
import dataV from "@jiaminghi/data-view";
// 引入全局css
import "./assets/scss/style.scss";
// 按需引入vue-awesome图标
import Icon from "vue-awesome/components/Icon";
import "vue-awesome/icons/chart-bar.js";
import "vue-awesome/icons/chart-area.js";
import "vue-awesome/icons/chart-pie.js";
import "vue-awesome/icons/chart-line.js";
import "vue-awesome/icons/align-left.js";

// 引入elementUI - 完整引入方式
import ElementUI from "element-ui";
import "element-ui/lib/theme-chalk/index.css";

//引入echart
//4.x 引用方式
import * as echarts from 'echarts';
//5.x 引用方式为按需引用
//希望使用5.x版本的话,需要在package.json中更新版本号,并切换引用方式
//import * as echarts from 'echarts'
Vue.prototype.$echarts = echarts;
Vue.config.productionTip = false;
// Vue.config.devtools = true;

// 引入iconfont图标
import "./assets/iconfont/iconfont.css";


// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem("token");
  
  // 如果有 token 但 store 中没有数据，初始化 store
  if (token && !store.getters.getToken) {
    store.dispatch('initStore');
  }
  
  if (to.path !== "/login" && !token) {
    // 未登录，强制跳转到登录页
    next({ path: "/login" });
  } else if (to.path === "/login" && token) {
    // 已登录，访问登录页时跳转到首页
    next({ path: "/" });
  } else {
    next();
  }
});

// 使用ElementUI - 完整引入
Vue.use(ElementUI);
// 全局注册
Vue.component("icon", Icon);
Vue.use(dataV);

new Vue({
  router,
  store,
  created() {
    // 应用启动时初始化store
    this.$store.dispatch('initStore');
  },
  render: (h) => h(App),
}).$mount("#app");
