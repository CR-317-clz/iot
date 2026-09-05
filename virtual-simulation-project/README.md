
## 一、项目描述

这是一个基于 **Vue 2.x + DataV + ECharts + Element UI** 的物联网硬件监控数据大屏可视化项目。项目集成了设备管理、实时监控、视频监控、气象数据展示等功能模块，通过动态数据渲染实现设备状态的实时可视化展示。

### 技术栈

- **前端框架**: Vue 2.6.11
- **UI 组件库**: Element UI 2.15.14
- **数据可视化**: 
  - ECharts 5.6.0 (图表库)
  - @jiaminghi/data-view 2.7.3 (大屏组件)
- **状态管理**: Vuex 3.1.2
- **路由管理**: Vue Router 3.1.5
- **HTTP 请求**: Axios 1.8.3
- **图标库**: Vue-Awesome 4.0.2
- **样式处理**: Sass/SCSS

### 主要功能模块

1. **用户登录认证** - Token 鉴权机制
2. **设备管理** - 设备信息的增删改查
3. **设备监控** - 实时监控设备运行状态
4. **视频监控** - 支持 WebRTC 视频流播放
5. **数据大屏** - 数据可视化展示
6. **气象数据** - 气象站数据概览与实时监控
7. **设备协议管理** - IoT 设备协议配置

### 项目特点

- 响应式布局，支持不同分辨率屏幕
- 数据动态刷新，实时展示设备状态
- 图表组件可自由替换和定制
- 完整的路由守卫和权限控制
- 项目需要全屏展示以获得最佳体验（按 F11）

## 二、项目结构

```
IoT-hardware-ui/
├── public/                      # 静态资源目录
│   ├── index.html              # HTML 入口文件
│   ├── favicon.ico             # 网站图标
│   └── webrtcstreamer.js       # WebRTC 流媒体播放器
├── src/                         # 源代码目录
│   ├── App.vue                 # 根组件
│   ├── main.js                 # 入口文件，初始化 Vue 实例
│   ├── api/                    # API 接口目录
│   │   ├── login.js           # 登录相关接口
│   │   ├── device.js          # 设备管理接口
│   │   ├── iot-device.js      # IoT 设备接口
│   │   ├── iot-camera.js      # 摄像头设备接口
│   │   ├── iot-telemetry.js   # 设备遥测数据接口
│   │   ├── iot-meteorology.js # 气象数据接口
│   │   ├── iot-protocol.js    # 设备协议接口
│   │   └── scream.js          # 大屏配置接口
│   ├── assets/                 # 静态资源
│   │   ├── logo.png           # Logo 图片
│   │   ├── pageBg.png         # 页面背景图
│   │   ├── iconfont/          # 图标字体
│   │   ├── login/             # 登录页面资源
│   │   ├── pic/               # 图片资源
│   │   ├── svg/               # SVG 图标
│   │   └── scss/              # 全局样式文件
│   │       ├── style.scss     # 全局通用样式
│   │       ├── index.scss     # 首页样式
│   │       └── _variables.scss # SCSS 变量定义
│   ├── common/                 # 公共组件和工具
│   │   ├── echart/            # ECharts 封装
│   │   │   ├── index.vue     # ECharts 通用组件
│   │   │   └── theme.json    # ECharts 主题配置
│   │   └── map/               # 地图相关
│   │     
│   ├── components/             # 业务组件
│   │   └── echart/            # 图表组件
│   │       ├── TrendChart.vue # 趋势图表
│   │       ├── videoPart.vue  # 视频组件
│   │       ├── bottom/        # 底部图表
│   │       ├── center/        # 中心图表
│   │       ├── centerLeft/    # 左中图表
│   │       └── centerRight/   # 
│   ├── layout/                 # 布局组件
│   │   ├── index.vue          # 主布局框架
│   │   ├── login.vue          # 登录页面
│   │   └── page/              # 页面组件
│   │       ├── BigScreen.vue         # 数据大屏
│   │       ├── DeviceManagement.vue  # 设备管理
│   │       ├── DeviceMonitor.vue     # 设备监控
│   │       ├── VideoMonitor.vue      # 视频监控
│   │       ├── DeviceDataDetail.vue  # 设备数据详情
│   │       ├── iot/                  # IoT 功能模块
│   │       │   ├── Camera.vue       # 摄像头管理
│   │       │   └── Protocol.vue     # 协议管理
│   │       └── meteorology/          # 气象功能模块
│   │           ├── Overview.vue      # 气象概览
│   │           ├── OverviewData.vue  # 概览数据
│   │           ├── Realtime.vue      # 实时数据
│   │           └── History.vue       # 历史数据
│   ├── router/                 # 路由配置
│   │   └── index.js           # 路由定义
│   ├── store/                  # Vuex 状态管理
│   │   └── index.js           # 状态树定义
│   ├── utils/                  # 工具函数
│   │   ├── index.js           # 通用工具函数
│   │   ├── drawMixin.js       # 图表绘制混入
│   │   ├── resizeMixin.js     # 响应式混入
│   │   └── deviceDataService.js # 设备数据服务
│   └── views/                  # 视图组件
│       ├── index.vue          # 大屏主视图
│       └── newPart/           # 大屏各区域组件
│           ├── center/        # 中心区域
│           ├── left/          # 左侧区域
│           └── right/         # 右侧区域
├── babel.config.js             # Babel 配置
├── vue.config.js               # Vue CLI 配置
├── package.json                # 项目依赖配置
├── pnpm-lock.yaml             # PNPM 锁文件
├── LICENSE                     # 开源许可证
└── README.md                   # 项目说明文档
```

## 三、主要文件介绍

| 文件/目录 | 作用/功能 |
| :--- | :--- |
| `src/main.js` | 项目入口文件，初始化 Vue 实例，引入全局组件、插件、样式等 |
| `src/App.vue` | 根组件，包含路由视图出口 |
| `src/router/index.js` | 路由配置文件，定义所有页面路由和路由守卫 |
| `src/store/index.js` | Vuex 状态管理，管理用户信息、设备列表、大屏配置等全局状态 |
| `src/api/` | API 接口封装目录，按功能模块划分不同的接口文件 |
| `src/utils/index.js` | 通用工具函数，如时间格式化、数据处理等 |
| `src/utils/drawMixin.js` | 图表绘制混入，提供 ECharts 图表的通用绘制逻辑 |
| `src/utils/resizeMixin.js` | 响应式混入，监听窗口大小变化，自动调整图表尺寸 |
| `src/utils/deviceDataService.js` | 设备数据轮询服务，处理设备数据的定时更新 |
| `src/layout/index.vue` | 主布局组件，包含导航菜单、侧边栏等 |
| `src/layout/login.vue` | 登录页面组件，处理用户登录认证 |
| `src/views/index.vue` | 数据大屏主视图，整合各个可视化组件 |
| `src/common/echart/index.vue` | ECharts 图表封装组件，提供统一的图表创建和更新接口 |
| `src/common/echart/theme.json` | ECharts 主题配置文件，定义图表的默认样式 |
| `src/components/echart/` | 各类图表组件，按位置和功能分类（顶部、底部、左侧、右侧、中心等） |
| `src/assets/scss/style.scss` | 全局通用样式文件，定义项目统一的样式规范 |
| `src/assets/scss/index.scss` | 大屏首页专用样式文件 |
| `src/assets/scss/_variables.scss` | SCSS 变量定义文件，统一管理颜色、字体、间距等样式变量 |
| `src/assets/iconfont/` | 图标字体文件，提供项目所需的各种图标 |
| `public/webrtcstreamer.js` | WebRTC 流媒体播放器库，用于视频监控功能 |
| `vue.config.js` | Vue CLI 配置文件，配置代理、打包优化等 |
| `babel.config.js` | Babel 转译配置，兼容不同浏览器 |

## 四、使用介绍

### 环境要求

- **Node.js**: >= 12.x
- **包管理器**: PNPM / NPM / Yarn
- **浏览器**: 推荐使用 Chrome、Edge、Firefox 最新版本

### 安装依赖

```bash
# 使用 PNPM（推荐）
pnpm install

# 或使用 NPM
npm install

# 或使用 Yarn
yarn install
```

### 启动项目

```bash
# 开发环境启动
npm run serve

# 或使用 Vue CLI
vue-cli-service serve
```

启动成功后，在浏览器中访问 `http://localhost:8080`（默认端口），**按 F11 进入全屏模式以获得最佳展示效果**。

### 构建部署

```bash
# 生产环境构建
npm run build

# 构建完成后，dist 目录即为可部署的静态文件
```

### 常见问题

1. **缺少 DataV 依赖**
   ```bash
   npm install @jiaminghi/data-view
   # 或
   yarn add @jiaminghi/data-view
   ```

2. **端口被占用**
   - 修改 `vue.config.js` 中的 `devServer.port` 配置

3. **跨域问题**
   - 在 `vue.config.js` 中配置代理 `devServer.proxy`

## 五、技术实现

### ECharts 图表封装

所有的 ECharts 图表都基于 `src/common/echart/index.vue` 封装组件创建，该组件具有以下特性：

- **数据监听**: 自动监听数据变化，动态更新图表
- **响应式布局**: 监听窗口大小变化，自动调整图表尺寸
- **性能优化**: 使用防抖函数控制更新频率，减少不必要的重绘
- **主题定制**: 支持自定义主题配置（`common/echart/theme.json`）

### 状态管理

项目使用 Vuex 进行全局状态管理，主要管理：

- **用户信息**: userId、token、role
- **设备数据**: 设备列表、设备名称、配置 ID
- **大屏配置**: 选中的大屏信息、大屏列表
- **轮询状态**: 数据轮询定时器和状态

### 路由守卫

通过 `router.beforeEach` 实现路由守卫：

- 未登录用户自动跳转到登录页
- 已登录用户访问登录页自动跳转到首页
- 自动从 localStorage 恢复登录状态

### API 接口管理

API 接口按功能模块分类：

- **login.js**: 用户登录认证
- **device.js**: 设备基础管理
- **iot-device.js**: IoT 设备操作
- **iot-camera.js**: 摄像头设备管理
- **iot-telemetry.js**: 设备遥测数据
- **iot-meteorology.js**: 气象数据
- **iot-protocol.js**: 设备协议配置
- **scream.js**: 大屏配置管理

每个接口文件都基于 Axios 进行封装，统一处理请求和响应。

## 六、开发规范

### 组件命名

- 页面组件：大驼峰命名（如 `DeviceManagement.vue`）
- 业务组件：大驼峰命名（如 `TrendChart.vue`）
- 通用组件：小写+连字符（如 `echart/index.vue`）

### 样式规范

- 使用 SCSS 预处理器
- 统一使用 `_variables.scss` 中定义的样式变量
- 组件样式使用 `scoped` 避免污染全局

### API 调用

- 统一在 `src/api/` 目录下定义接口
- 使用 async/await 处理异步请求
- 统一错误处理和提示

## 七、项目截图

（此处可添加项目运行截图）

## 八、License

本项目遵循 LICENSE 文件中的开源协议。
