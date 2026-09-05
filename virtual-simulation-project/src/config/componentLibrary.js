/**
 * 硬件器材库配置文件
 * 用于定义可用的硬件器材及其属性
 */

export const componentLibrary = [
  {
    id: 'arduino',
    name: 'Arduino UNO',
    type: 'mcu',
    voltage: '5V',
    protocol: 'UART/I2C/SPI',
    icon: 'el-icon-cpu',
    color: '#00979D',
    description: 'Arduino UNO开发板，基于ATmega328P微控制器',
    pins: [
      { id: 'vcc', name: '5V', type: 'power', voltage: 5, description: '5V电源输出' },
      { id: '3v3', name: '3.3V', type: 'power', voltage: 3.3, description: '3.3V电源输出' },
      { id: 'gnd', name: 'GND', type: 'ground', voltage: 0, description: '接地引脚' },
      { id: 'd0', name: 'D0/RX', type: 'digital', voltage: 5, description: '数字引脚0/串口接收' },
      { id: 'd1', name: 'D1/TX', type: 'digital', voltage: 5, description: '数字引脚1/串口发送' },
      { id: 'd2', name: 'D2', type: 'digital', voltage: 5, description: '数字引脚2' },
      { id: 'd3', name: 'D3', type: 'digital', voltage: 5, description: '数字引脚3/PWM' },
      { id: 'd4', name: 'D4', type: 'digital', voltage: 5, description: '数字引脚4' },
      { id: 'd5', name: 'D5', type: 'digital', voltage: 5, description: '数字引脚5/PWM' },
      { id: 'd6', name: 'D6', type: 'digital', voltage: 5, description: '数字引脚6/PWM' },
      { id: 'd7', name: 'D7', type: 'digital', voltage: 5, description: '数字引脚7' },
      { id: 'a0', name: 'A0', type: 'analog', voltage: 5, description: '模拟引脚0' },
      { id: 'a1', name: 'A1', type: 'analog', voltage: 5, description: '模拟引脚1' },
      { id: 'a2', name: 'A2', type: 'analog', voltage: 5, description: '模拟引脚2' },
      { id: 'a3', name: 'A3', type: 'analog', voltage: 5, description: '模拟引脚3' }
    ]
  },
  {
    id: 'esp32',
    name: 'ESP32',
    type: 'mcu',
    voltage: '3.3V',
    protocol: 'UART/I2C/SPI/WiFi/BLE',
    icon: 'el-icon-connection',
    color: '#FF6347',
    description: 'ESP32开发板，支持WiFi和蓝牙',
    pins: [
      { id: '3v3', name: '3V3', type: 'power', voltage: 3.3, description: '3.3V电源输出' },
      { id: 'gnd', name: 'GND', type: 'ground', voltage: 0, description: '接地引脚' },
      { id: 'gpio0', name: 'GPIO0', type: 'digital', voltage: 3.3, description: 'GPIO0/Boot' },
      { id: 'gpio2', name: 'GPIO2', type: 'digital', voltage: 3.3, description: 'GPIO2/内置LED' },
      { id: 'gpio4', name: 'GPIO4', type: 'digital', voltage: 3.3, description: 'GPIO4' },
      { id: 'gpio5', name: 'GPIO5', type: 'digital', voltage: 3.3, description: 'GPIO5' },
      { id: 'gpio12', name: 'GPIO12', type: 'digital', voltage: 3.3, description: 'GPIO12' },
      { id: 'gpio13', name: 'GPIO13', type: 'digital', voltage: 3.3, description: 'GPIO13' },
      { id: 'gpio14', name: 'GPIO14', type: 'digital', voltage: 3.3, description: 'GPIO14' },
      { id: 'gpio15', name: 'GPIO15', type: 'digital', voltage: 3.3, description: 'GPIO15' }
    ]
  },
  {
    id: 'led',
    name: 'LED灯',
    type: 'output',
    voltage: '2-3V',
    protocol: 'None',
    icon: 'el-icon-sunny',
    color: '#FFD700',
    description: '发光二极管，用于指示或照明',
    pins: [
      { id: 'anode', name: '正极(+)', type: 'power', voltage: 2.5, description: 'LED正极，较长引脚' },
      { id: 'cathode', name: '负极(-)', type: 'ground', voltage: 0, description: 'LED负极，较短引脚' }
    ]
  },
  {
    id: 'resistor',
    name: '电阻',
    type: 'passive',
    voltage: '任意',
    protocol: 'None',
    icon: 'el-icon-minus',
    color: '#8B4513',
    description: '限流电阻，常用于保护LED等器件',
    pins: [
      { id: 'pin1', name: '引脚1', type: 'passive', voltage: null, description: '电阻引脚1' },
      { id: 'pin2', name: '引脚2', type: 'passive', voltage: null, description: '电阻引脚2' }
    ]
  },
  {
    id: 'dht11',
    name: 'DHT11温湿度',
    type: 'sensor',
    voltage: '3.3-5V',
    protocol: 'OneWire',
    icon: 'el-icon-partly-cloudy',
    color: '#4169E1',
    description: 'DHT11温湿度传感器',
    pins: [
      { id: 'vcc', name: 'VCC', type: 'power', voltage: 5, description: '电源正极，3.3-5V' },
      { id: 'data', name: 'DATA', type: 'digital', voltage: 5, description: '数据引脚' },
      { id: 'nc', name: 'NC', type: 'none', voltage: null, description: '未连接' },
      { id: 'gnd', name: 'GND', type: 'ground', voltage: 0, description: '接地引脚' }
    ]
  },
  {
    id: 'dht22',
    name: 'DHT22温湿度',
    type: 'sensor',
    voltage: '3.3-5V',
    protocol: 'OneWire',
    icon: 'el-icon-partly-cloudy',
    color: '#1E90FF',
    description: 'DHT22温湿度传感器，精度高于DHT11',
    pins: [
      { id: 'vcc', name: 'VCC', type: 'power', voltage: 5, description: '电源正极，3.3-5V' },
      { id: 'data', name: 'DATA', type: 'digital', voltage: 5, description: '数据引脚' },
      { id: 'nc', name: 'NC', type: 'none', voltage: null, description: '未连接' },
      { id: 'gnd', name: 'GND', type: 'ground', voltage: 0, description: '接地引脚' }
    ]
  },
  {
    id: 'servo',
    name: '舵机',
    type: 'actuator',
    voltage: '4.8-6V',
    protocol: 'PWM',
    icon: 'el-icon-setting',
    color: '#32CD32',
    description: 'SG90舵机，180度旋转',
    pins: [
      { id: 'vcc', name: 'VCC', type: 'power', voltage: 5, description: '电源正极，红线' },
      { id: 'signal', name: 'Signal', type: 'digital', voltage: 5, description: 'PWM信号，橙/黄线' },
      { id: 'gnd', name: 'GND', type: 'ground', voltage: 0, description: '接地，棕线' }
    ]
  },
  {
    id: 'ultrasonic',
    name: '超声波传感器',
    type: 'sensor',
    voltage: '5V',
    protocol: 'Digital',
    icon: 'el-icon-help',
    color: '#20B2AA',
    description: 'HC-SR04超声波测距传感器',
    pins: [
      { id: 'vcc', name: 'VCC', type: 'power', voltage: 5, description: '5V电源' },
      { id: 'trig', name: 'Trig', type: 'digital', voltage: 5, description: '触发引脚' },
      { id: 'echo', name: 'Echo', type: 'digital', voltage: 5, description: '回响引脚' },
      { id: 'gnd', name: 'GND', type: 'ground', voltage: 0, description: '接地引脚' }
    ]
  },
  {
    id: 'button',
    name: '按钮',
    type: 'input',
    voltage: '任意',
    protocol: 'None',
    icon: 'el-icon-circle-check',
    color: '#FF69B4',
    description: '轻触按钮开关',
    pins: [
      { id: 'pin1', name: '引脚1', type: 'passive', voltage: null, description: '按钮引脚1' },
      { id: 'pin2', name: '引脚2', type: 'passive', voltage: null, description: '按钮引脚2' },
      { id: 'pin3', name: '引脚3', type: 'passive', voltage: null, description: '按钮引脚3' },
      { id: 'pin4', name: '引脚4', type: 'passive', voltage: null, description: '按钮引脚4' }
    ]
  },
  {
    id: 'lcd1602',
    name: 'LCD1602显示屏',
    type: 'output',
    voltage: '5V',
    protocol: 'I2C/Parallel',
    icon: 'el-icon-picture-outline',
    color: '#9370DB',
    description: '16x2字符液晶显示屏',
    pins: [
      { id: 'vcc', name: 'VCC', type: 'power', voltage: 5, description: '5V电源' },
      { id: 'gnd', name: 'GND', type: 'ground', voltage: 0, description: '接地' },
      { id: 'sda', name: 'SDA', type: 'digital', voltage: 5, description: 'I2C数据线' },
      { id: 'scl', name: 'SCL', type: 'digital', voltage: 5, description: 'I2C时钟线' }
    ]
  },
  {
    id: 'relay',
    name: '继电器模块',
    type: 'actuator',
    voltage: '5V',
    protocol: 'Digital',
    icon: 'el-icon-switch-button',
    color: '#DC143C',
    description: '5V单路继电器模块',
    pins: [
      { id: 'vcc', name: 'VCC', type: 'power', voltage: 5, description: '5V电源' },
      { id: 'in', name: 'IN', type: 'digital', voltage: 5, description: '控制输入' },
      { id: 'gnd', name: 'GND', type: 'ground', voltage: 0, description: '接地' }
    ]
  },
  {
    id: 'buzzer',
    name: '蜂鸣器',
    type: 'output',
    voltage: '3-5V',
    protocol: 'Digital',
    icon: 'el-icon-bell',
    color: '#FFB6C1',
    description: '有源蜂鸣器',
    pins: [
      { id: 'positive', name: '+', type: 'power', voltage: 5, description: '正极' },
      { id: 'negative', name: '-', type: 'ground', voltage: 0, description: '负极' }
    ]
  }
];

/**
 * 引脚类型定义
 */
export const pinTypes = {
  power: {
    name: '电源',
    color: '#f56c6c',
    description: '电源输出引脚'
  },
  ground: {
    name: '接地',
    color: '#303133',
    description: '接地引脚'
  },
  digital: {
    name: '数字',
    color: '#409eff',
    description: '数字I/O引脚'
  },
  analog: {
    name: '模拟',
    color: '#67c23a',
    description: '模拟输入引脚'
  },
  passive: {
    name: '无源',
    color: '#909399',
    description: '无极性引脚'
  },
  none: {
    name: '未连接',
    color: '#c0c4cc',
    description: '不使用的引脚'
  }
};

/**
 * 连接验证规则
 */
export const validationRules = {
  // 电压兼容性检查
  voltageCompatibility: (pin1, pin2) => {
    if (pin1.voltage === null || pin2.voltage === null) {
      return { valid: true };
    }
    
    const diff = Math.abs(pin1.voltage - pin2.voltage);
    if (diff > 0.5) {
      return {
        valid: false,
        error: `电压不匹配：${pin1.name}(${pin1.voltage}V) 与 ${pin2.name}(${pin2.voltage}V)`,
        level: 'error'
      };
    }
    
    return { valid: true };
  },
  
  // 引脚类型兼容性检查
  typeCompatibility: (pin1, pin2) => {
    // 无源引脚可以连接任何引脚
    if (pin1.type === 'passive' || pin2.type === 'passive') {
      return { valid: true };
    }
    
    // 电源不能直连电源
    if (pin1.type === 'power' && pin2.type === 'power') {
      return {
        valid: false,
        error: '不能将两个电源引脚直接连接',
        level: 'error'
      };
    }
    
    // 地线可以互连（虽然不推荐）
    if (pin1.type === 'ground' && pin2.type === 'ground') {
      return {
        valid: true,
        warning: '地线互连是允许的，但通常不必要',
        level: 'warning'
      };
    }
    
    return { valid: true };
  },
  
  // 协议兼容性检查（扩展功能）
  protocolCompatibility: (component1, component2, pin1, pin2) => {
    // TODO: 实现协议兼容性检查
    return { valid: true };
  }
};

export default {
  componentLibrary,
  pinTypes,
  validationRules
};
