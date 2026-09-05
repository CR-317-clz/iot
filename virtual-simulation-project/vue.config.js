const path = require("path");
const resolve = (dir) => path.join(__dirname, dir);

module.exports = {
  publicPath: "./",

  devServer: {
    // HTTPS 配置（自动生成证书）
    https: true,
    host: '0.0.0.0',
    port: 8080,
    
    proxy: {
      // IoT 设备接口代理
      "/iot-api": {
        target: "http://192.168.4.100:9902",
        changeOrigin: true,
        pathRewrite: { "^/iot-api": "" },
      },
      // 业务接口代理
      "/api": {
        target: "http://192.168.110.137:10012",
        changeOrigin: true,
        pathRewrite: { "^/api": "" },
      },
    },
  },

  /**
   * vue-echarts 使用了现代 JS 语法（例如 ?.）
   * 需要 Babel 转译
   */
  transpileDependencies: [
    "vue-echarts",
    "resize-detector"
  ],

  chainWebpack: (config) => {
    config.resolve.alias.set("_c", resolve("src/components"));
  },
};
