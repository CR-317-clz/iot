import Vue from "vue";
import VueRouter from "vue-router";

Vue.use(VueRouter);

const routes = [
  {
    path: "/",
    name: "index",
    component: () => import("../views/index.vue"),
  },
  {
    path: "/layout",
    name: "layout",
    component: () => import("../layout/index.vue"),
    redirect: "/layout/device-monitor", // 默认重定向到设备监控
    children: [
      {
        path: "device-management",
        name: "device-management",
        component: () => import("../layout/page/DeviceManagement.vue"),
      },
      {
        path: "big-screen",
        name: "big-screen",
        component: () => import("../layout/page/BigScreen.vue"),
      },
      {
        path: "device-monitor",
        name: "device-monitor",
        component: () => import("../layout/page/DeviceMonitor.vue"),
      },
      {
        path: "video-monitor",
        name: "video-monitor",
        component: () => import("../layout/page/VideoMonitor.vue"),
      },
        {
        path: "device-camera",
        name: "device-camera",
        component: () => import("../layout/page/iot/Camera.vue"),
      },
      {
        path: "device-data-detail",
        name: "device-data-detail",
        component: () => import("../layout/page/DeviceDataDetail.vue"),
      },
      // {
      //   path: "hardware-simulation",
      //   name: "hardware-simulation",
      //   component: () => import("../layout/page/HardwareSimulationX6.vue"),
      // },
      // {
      //   path: "component-management",
      //   name: "component-management",
      //   component: () => import("../layout/page/ComponentManagement.vue"),
      // },
      // {
      //   path: "serial-port-test",
      //   name: "serial-port-test",
      //   component: () => import("../layout/page/SerialPortTest.vue"),
      // },
        {
            path: 'meteorology-overview',
            name: 'meteorology-overview', component: () => import("../layout/page/meteorology/Overview.vue")
        },
        {
            path: 'meteorology-realtime',
            name: 'meteorology-realtime', component: () => import("../layout/page/meteorology/Realtime.vue")
        },
      
        {
            path: 'meteorology/overview-data',
            name: 'meteorology-overview-data', component: () => import("../layout/page/meteorology/OverviewData.vue")
        },
        {
            path: 'device-protocol',
            name: 'device-protocol', component: () => import("../layout/page/iot/Protocol.vue")
        },
    ],
  },
  {
    path: "/login",
    name: "login",
    component: () => import("../layout/login.vue"),
  },
];
const router = new VueRouter({
  routes,
});

// 解决Vue Router冗余导航的警告
const originalPush = VueRouter.prototype.push;
VueRouter.prototype.push = function push(location) {
  return originalPush.call(this, location).catch((err) => {
    if (err.name !== "NavigationDuplicated") {
      throw err;
    }
  });
};

export default router;