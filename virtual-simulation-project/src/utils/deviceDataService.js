// 智慧农渔设备数据服务
const deviceDataService = {
  // 农渔区域基础信息
  deviceInfo: {
    'AREA001': {
      id: 'AREA001',
      name: '一号仓库',
      type: '一号种植区',
      location: '一号园区',
      status: '在线',
      icon: 'el-icon-sunny',
      deviceCount: 8,
      params: [
        { name: '土壤温度', value: 24.5, unit: '°C', icon: 'el-icon-light-rain', status: 'normal', statusText: '正常' },
        { name: '土壤湿度', value: 65, unit: '%', icon: 'el-icon-light-rain', status: 'normal', statusText: '正常' },
        { name: '空气温度', value: 26.2, unit: '°C', icon: 'el-icon-sunny', status: 'normal', statusText: '正常' },
        { name: '光照强度', value: 45000, unit: 'lux', icon: 'el-icon-view', status: 'normal', statusText: '正常' }
      ]
    },
    'AREA002': {
      id: 'AREA002',
      name: '二号仓库',
      type: '二号养殖区',
      location: '二号园区',
      status: '在线',
      icon: 'el-icon-umbrella',
      deviceCount: 12,
      params: [
        { name: '水温', value: 22.8, unit: '°C', icon: 'el-icon-light-rain', status: 'normal', statusText: '正常' },
        { name: '溶氧量', value: 8.5, unit: 'mg/L', icon: 'el-icon-lightning', status: 'normal', statusText: '正常' },
        { name: 'pH值', value: 7.2, unit: '', icon: 'el-icon-data-analysis', status: 'normal', statusText: '正常' },
        { name: '浊度', value: 15, unit: 'NTU', icon: 'el-icon-view', status: 'warning', statusText: '偏高' }
      ]
    },
    'AREA003': {
      id: 'AREA003',
      name: '三号仓库',
      type: '三号养殖区',
      location: '三号园区',
      status: '离线',
      icon: 'el-icon-house',
      deviceCount: 6,
      params: [
        { name: '舍内温度', value: 18.5, unit: '°C', icon: 'el-icon-light-rain', status: 'danger', statusText: '过低' },
        { name: '舍内湿度', value: 45, unit: '%', icon: 'el-icon-light-rain', status: 'normal', statusText: '正常' },
        { name: '氨气浓度', value: 25, unit: 'ppm', icon: 'el-icon-warning', status: 'danger', statusText: '超标' },
        { name: '通风量', value: 0, unit: 'm³/h', icon: 'el-icon-refresh', status: 'danger', statusText: '设备离线' }
      ]
    }
  },

  // 获取区域信息
  getDeviceInfo(areaId) {
    return this.deviceInfo[areaId] || this.getDefaultAreaInfo(areaId);
  },

  // 获取默认区域信息（用于其他区域）
  getDefaultAreaInfo(areaId) {
    const areaTypes = {
      'AREA004': { name: '四号仓库', type: '四号种植区', location: '四号园区', icon: 'el-icon-apple', deviceCount: 10 },
      'AREA005': { name: '五号仓库', type: '五号种植区', location: '五号园区', icon: 'el-icon-grape', deviceCount: 7 },
      'AREA006': { name: '六号仓库', type: '六号养殖区', location: '六号园区', icon: 'el-icon-light-rain', deviceCount: 8 },
      'AREA007': { name: '七号仓库', type: '七号种植区', location: '七号园区', icon: 'el-icon-sunny', deviceCount: 15 },
      'AREA008': { name: '八号仓库', type: '八号种植区', location: '八号园区', icon: 'el-icon-star-on', deviceCount: 9 },
      'AREA009': { name: '九号仓库', type: '九号培育区', location: '九号园区', icon: 'el-icon-upload', deviceCount: 5 },
      'AREA010': { name: '十号仓库', type: '十号养殖区', location: '十号园区', icon: 'el-icon-light-rain', deviceCount: 6 },
      'AREA011': { name: '十一号仓库', type: '十一号种植区', location: '十一号园区', icon: 'el-icon-umbrella', deviceCount: 4 },
      'AREA012': { name: '十二号仓库', type: '十二号存储区', location: '十二号园区', icon: 'el-icon-box', deviceCount: 3 },
      'AREA013': { name: '十三号仓库', type: '十三号控制区', location: '十三号园区', icon: 'el-icon-refresh', deviceCount: 12 },
      'AREA014': { name: '十四号仓库', type: '十四号加工区', location: '十四号园区', icon: 'el-icon-setting', deviceCount: 8 },
      'AREA015': { name: '十五号仓库', type: '十五号存储区', location: '十五号园区', icon: 'el-icon-ice-cream', deviceCount: 6 },
      'AREA016': { name: '十六号仓库', type: '十六号监测区', location: '十六号园区', icon: 'el-icon-cloudy', deviceCount: 4 }
    };

    const area = areaTypes[areaId] || { name: '未知区域', type: '未知', location: '未知', icon: 'el-icon-monitor', deviceCount: 0 };
    
    return {
      id: areaId,
      ...area,
      status: Math.random() > 0.8 ? '离线' : '在线',
      params: this.generateRandomParams(area.type)
    };
  },

  // 生成随机参数数据
  generateRandomParams(areaType) {
    const commonParams = [
      { name: '运行时长', value: Math.floor(Math.random() * 200 + 50), unit: '小时', icon: 'el-icon-time', status: 'normal', statusText: '正常' },
      { name: '电力消耗', value: (Math.random() * 5 + 2).toFixed(1), unit: 'kW', icon: 'el-icon-lightning', status: Math.random() > 0.7 ? 'warning' : 'normal', statusText: Math.random() > 0.7 ? '偏高' : '正常' }
    ];

    switch (areaType) {
      case '一号种植区':
      case '四号种植区':
      case '五号种植区':
      case '七号种植区':
      case '八号种植区':
      case '十一号种植区':
        return [
          { name: '土壤温度', value: (Math.random() * 10 + 18).toFixed(1), unit: '°C', icon: 'el-icon-light-rain', status: 'normal', statusText: '正常' },
          { name: '土壤湿度', value: Math.floor(Math.random() * 40 + 50), unit: '%', icon: 'el-icon-water', status: 'normal', statusText: '正常' },
          { name: '光照强度', value: Math.floor(Math.random() * 20000 + 30000), unit: 'lux', icon: 'el-icon-view', status: 'normal', statusText: '正常' },
          ...commonParams
        ];
      
      case '二号养殖区':
      case '六号养殖区':
      case '十号养殖区':
        return [
          { name: '水温', value: (Math.random() * 8 + 18).toFixed(1), unit: '°C', icon: 'el-icon-light-rain', status: 'normal', statusText: '正常' },
          { name: '溶氧量', value: (Math.random() * 4 + 6).toFixed(1), unit: 'mg/L', icon: 'el-icon-lightning', status: 'normal', statusText: '正常' },
          { name: 'pH值', value: (Math.random() * 2 + 6.5).toFixed(1), unit: '', icon: 'el-icon-data-analysis', status: 'normal', statusText: '正常' },
          { name: '浊度', value: Math.floor(Math.random() * 20 + 10), unit: 'NTU', icon: 'el-icon-view', status: 'normal', statusText: '正常' }
        ];
      
      case '三号养殖区':
        return [
          { name: '舍内温度', value: (Math.random() * 10 + 15).toFixed(1), unit: '°C', icon: 'el-icon-light-rain', status: 'normal', statusText: '正常' },
          { name: '舍内湿度', value: Math.floor(Math.random() * 30 + 40), unit: '%', icon: 'el-icon-water', status: 'normal', statusText: '正常' },
          { name: '氨气浓度', value: Math.floor(Math.random() * 15 + 5), unit: 'ppm', icon: 'el-icon-warning', status: 'normal', statusText: '正常' },
          { name: '通风量', value: Math.floor(Math.random() * 500 + 200), unit: 'm³/h', icon: 'el-icon-refresh', status: 'normal', statusText: '正常' }
        ];
      
      case '九号培育区':
        return [
          { name: '培育温度', value: (Math.random() * 8 + 20).toFixed(1), unit: '°C', icon: 'el-icon-light-rain', status: 'normal', statusText: '正常' },
          { name: '培育湿度', value: Math.floor(Math.random() * 30 + 60), unit: '%', icon: 'el-icon-water', status: 'normal', statusText: '正常' },
          { name: '生长速度', value: (Math.random() * 3 + 1).toFixed(1), unit: 'cm/天', icon: 'el-icon-upload', status: 'normal', statusText: '正常' },
          ...commonParams
        ];
      
      case '十二号存储区':
      case '十五号存储区':
        return [
          { name: '存储温度', value: (Math.random() * 10 - 5).toFixed(1), unit: '°C', icon: 'el-icon-ice-cream', status: 'normal', statusText: '正常' },
          { name: '存储湿度', value: Math.floor(Math.random() * 20 + 70), unit: '%', icon: 'el-icon-water', status: 'normal', statusText: '正常' },
          { name: '存储率', value: Math.floor(Math.random() * 40 + 60), unit: '%', icon: 'el-icon-box', status: 'normal', statusText: '正常' },
          ...commonParams
        ];
      
      case '十三号控制区':
        return [
          { name: '系统温度', value: (Math.random() * 15 + 20).toFixed(1), unit: '°C', icon: 'el-icon-light-rain', status: 'normal', statusText: '正常' },
          { name: '系统负载', value: Math.floor(Math.random() * 40 + 30), unit: '%', icon: 'el-icon-cpu', status: 'normal', statusText: '正常' },
          { name: '网络状态', value: Math.floor(Math.random() * 30 + 70), unit: '%', icon: 'el-icon-connection', status: 'normal', statusText: '正常' },
          ...commonParams
        ];
      
      case '十四号加工区':
        return [
          { name: '加工温度', value: (Math.random() * 20 + 25).toFixed(1), unit: '°C', icon: 'el-icon-light-rain', status: 'normal', statusText: '正常' },
          { name: '加工湿度', value: Math.floor(Math.random() * 30 + 50), unit: '%', icon: 'el-icon-water', status: 'normal', statusText: '正常' },
          { name: '生产效率', value: Math.floor(Math.random() * 30 + 70), unit: '%', icon: 'el-icon-setting', status: 'normal', statusText: '正常' },
          ...commonParams
        ];
      
      case '十六号监测区':
        return [
          { name: '环境温度', value: (Math.random() * 15 + 10).toFixed(1), unit: '°C', icon: 'el-icon-light-rain', status: 'normal', statusText: '正常' },
          { name: '环境湿度', value: Math.floor(Math.random() * 40 + 50), unit: '%', icon: 'el-icon-water', status: 'normal', statusText: '正常' },
          { name: '风速', value: (Math.random() * 10 + 2).toFixed(1), unit: 'm/s', icon: 'el-icon-refresh', status: 'normal', statusText: '正常' },
          { name: '降雨量', value: (Math.random() * 20).toFixed(1), unit: 'mm', icon: 'el-icon-cloudy', status: 'normal', statusText: '正常' }
        ];
      
      default:
        return [
          { name: '设备状态', value: '正常', unit: '', icon: 'el-icon-check', status: 'normal', statusText: '正常' },
          { name: '信号强度', value: Math.floor(Math.random() * 30 + 70), unit: '%', icon: 'el-icon-connection', status: 'normal', statusText: '正常' },
          ...commonParams
        ];
    }
  }
};

export default deviceDataService;
