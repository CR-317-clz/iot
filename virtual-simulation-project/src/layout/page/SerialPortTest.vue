<template>
  <div class="serial-port-test">
    <el-card class="page-card">
      <div slot="header" class="page-header">
        <span class="page-title">
          <i class="el-icon-connection"></i>
          串口通信测试
        </span>
        <p class="page-desc">使用 Web Serial API 进行串口设备通信测试</p>
      </div>
      
      <div class="content">
        <el-row :gutter="20">
          <!-- 左侧连接配置 -->
          <el-col :span="6">
            <el-card class="config-panel">
              <div slot="header" class="panel-header">
                <i class="el-icon-setting"></i>
                <span>连接配置</span>
              </div>
              
              <div class="config-content">
                <!-- 连接按钮 -->
                <el-button 
                  v-if="!isConnected"
                  type="primary" 
                  icon="el-icon-link"
                  @click="connectSerial"
                  :loading="connecting"
                  style="width: 100%; margin-bottom: 15px;">
                  {{ connecting ? '连接中...' : '连接串口' }}
                </el-button>
                
                <el-button 
                  v-else
                  type="danger" 
                  icon="el-icon-switch-button"
                  @click="disconnectSerial"
                  style="width: 100%; margin-bottom: 15px;">
                  断开连接
                </el-button>
                
                <!-- 串口配置 -->
                <el-divider>串口配置</el-divider>
                
                <el-form label-width="70px" size="small">
                  <el-form-item label="波特率">
                    <el-select v-model="serialConfig.baudRate" :disabled="isConnected" style="width: 100%;">
                      <el-option label="9600" :value="9600"></el-option>
                      <el-option label="19200" :value="19200"></el-option>
                      <el-option label="38400" :value="38400"></el-option>
                      <el-option label="57600" :value="57600"></el-option>
                      <el-option label="115200" :value="115200"></el-option>
                    </el-select>
                  </el-form-item>
                  
                  <el-form-item label="数据位">
                    <el-select v-model="serialConfig.dataBits" :disabled="isConnected" style="width: 100%;">
                      <el-option label="7" :value="7"></el-option>
                      <el-option label="8" :value="8"></el-option>
                    </el-select>
                  </el-form-item>
                  
                  <el-form-item label="停止位">
                    <el-select v-model="serialConfig.stopBits" :disabled="isConnected" style="width: 100%;">
                      <el-option label="1" :value="1"></el-option>
                      <el-option label="2" :value="2"></el-option>
                    </el-select>
                  </el-form-item>
                  
                  <el-form-item label="校验位">
                    <el-select v-model="serialConfig.parity" :disabled="isConnected" style="width: 100%;">
                      <el-option label="无" value="none"></el-option>
                      <el-option label="奇校验" value="odd"></el-option>
                      <el-option label="偶校验" value="even"></el-option>
                    </el-select>
                  </el-form-item>
                </el-form>
                
                <!-- 状态 -->
                <el-divider>连接状态</el-divider>
                <el-tag v-if="isConnected" type="success" style="width: 100%; justify-content: center;">
                  <i class="el-icon-success"></i> 已连接
                </el-tag>
                <el-tag v-else type="info" style="width: 100%; justify-content: center;">
                  <i class="el-icon-info"></i> 未连接
                </el-tag>
                
                <!-- 网络环境检测 -->
                <el-divider>网络环境</el-divider>
                <div class="network-info">
                  <div class="info-item">
                    <span class="label">访问协议:</span>
                    <el-tag :type="isSecureContext ? 'success' : 'danger'" size="small">
                      {{ protocol }}
                    </el-tag>
                  </div>
                  <div class="info-item">
                    <span class="label">当前地址:</span>
                    <span class="value">{{ currentHost }}</span>
                  </div>
                  <div class="info-item">
                    <span class="label">API 支持:</span>
                    <el-tag :type="isSerialSupported ? 'success' : 'danger'" size="small">
                      {{ isSerialSupported ? '✓ 支持' : '✗ 不支持' }}
                    </el-tag>
                  </div>
                </div>
                
                <!-- 浏览器兼容性提示 -->
                <el-alert
                  v-if="!isSerialSupported"
                  title="浏览器不支持 Web Serial API"
                  type="error"
                  :closable="false"
                  show-icon
                  style="margin-top: 15px;">
                  <template slot="default">
                    <div style="font-size: 12px; line-height: 1.8;">
                      <p style="margin: 5px 0; font-weight: 600; color: #F56C6C;">
                        ⚠️ 当前浏览器：{{ browserInfo }}
                      </p>
                      <p style="margin: 5px 0;">
                        <strong>问题原因：</strong>
                      </p>
                      <ul style="margin: 5px 0; padding-left: 20px;">
                        <li>浏览器版本不支持</li>
                        <li>未启用实验性功能</li>
                      </ul>
                    </div>
                  </template>
                </el-alert>
                
                <!-- HTTPS 环境提示 -->
                <el-alert
                  v-if="isSerialSupported && !isSecureContext"
                  title="需要 HTTPS 安全环境"
                  type="warning"
                  :closable="false"
                  show-icon
                  style="margin-top: 15px;">
                  <template slot="default">
                    <div style="font-size: 12px; line-height: 1.8;">
                      <p style="margin: 5px 0; font-weight: 600; color: #E6A23C;">
                        🔒 当前使用 HTTP 协议，Web Serial API 需要安全上下文
                      </p>
                      <p style="margin: 10px 0 5px 0;">
                        <strong>📋 解决方案（按推荐顺序）：</strong>
                      </p>
                      
                      <div style="margin: 10px 0; padding: 10px; background: #FDF6EC; border-radius: 4px;">
                        <p style="margin: 0 0 5px 0; font-weight: 600;">方案 1：配置 HTTPS（生产环境推荐）</p>
                        <ol style="margin: 5px 0; padding-left: 20px;">
                          <li>申请 SSL 证书（Let's Encrypt 免费）</li>
                          <li>在 vue.config.js 配置 HTTPS</li>
                          <li>重启开发服务器</li>
                        </ol>
                        <pre style="background: #f5f5f5; padding: 8px; border-radius: 4px; margin: 5px 0; overflow-x: auto;"><code>// vue.config.js
module.exports = {
  devServer: {
    https: true,
    host: '0.0.0.0',
    port: 8080
  }
}</code></pre>
                      </div>
                      
                      <div style="margin: 10px 0; padding: 10px; background: #FDF6EC; border-radius: 4px;">
                        <p style="margin: 0 0 5px 0; font-weight: 600;">方案 2：SSH 隧道转发（开发环境推荐）</p>
                        <p style="margin: 5px 0;">将远程服务映射到本地 localhost：</p>
                        <pre style="background: #f5f5f5; padding: 8px; border-radius: 4px; margin: 5px 0; overflow-x: auto;"><code># 在本地电脑执行
ssh -L 8080:localhost:8080 user@远程服务器IP</code></pre>
                        <p style="margin: 5px 0;">然后访问：<code style="background: #f0f0f0; padding: 2px 6px; border-radius: 3px;">http://localhost:8080</code></p>
                      </div>
                      
                      <div style="margin: 10px 0; padding: 10px; background: #FDF6EC; border-radius: 4px;">
                        <p style="margin: 0 0 5px 0; font-weight: 600;">方案 3：自签名证书（临时测试）</p>
                        <pre style="background: #f5f5f5; padding: 8px; border-radius: 4px; margin: 5px 0; overflow-x: auto;"><code># 生成自签名证书
openssl req -x509 -newkey rsa:4096 -nodes \\
  -keyout key.pem -out cert.pem -days 365

# vue.config.js 配置
module.exports = {
  devServer: {
    https: {
      key: fs.readFileSync('./key.pem'),
      cert: fs.readFileSync('./cert.pem')
    },
    host: '0.0.0.0'
  }
}</code></pre>
                        <p style="margin: 5px 0; color: #E6A23C;">⚠️ 浏览器会提示不安全，需手动信任证书</p>
                      </div>
                      
                      <div style="margin: 10px 0; padding: 10px; background: #FDF6EC; border-radius: 4px;">
                        <p style="margin: 0 0 5px 0; font-weight: 600;">方案 4：Nginx 反向代理</p>
                        <pre style="background: #f5f5f5; padding: 8px; border-radius: 4px; margin: 5px 0; overflow-x: auto;"><code>server {
    listen 443 ssl;
    server_name your-domain.com;
    
    ssl_certificate /path/to/cert.pem;
    ssl_certificate_key /path/to/key.pem;
    
    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
    }
}</code></pre>
                      </div>
                    </div>
                  </template>
                </el-alert>
                
                <!-- 成功提示 -->
                <el-alert
                  v-if="isSerialSupported && isSecureContext"
                  title="✓ 环境配置正确"
                  type="success"
                  :closable="false"
                  show-icon
                  style="margin-top: 15px;">
                  <div style="font-size: 12px;">
                    当前环境支持 Web Serial API，可以正常使用串口功能
                  </div>
                </el-alert>
              </div>
            </el-card>
          </el-col>
          
          <!-- 中间数据监控 -->
          <el-col :span="12">
            <el-card class="monitor-panel">
              <div slot="header" class="panel-header">
                <div>
                  <i class="el-icon-monitor"></i>
                  <span>数据监控</span>
                </div>
                <div>
                  <el-button-group size="mini">
                    <el-button 
                      icon="el-icon-delete"
                      @click="clearLog">
                      清空
                    </el-button>
                    <el-button 
                      :icon="autoScroll ? 'el-icon-bottom' : 'el-icon-top'"
                      @click="autoScroll = !autoScroll">
                      {{ autoScroll ? '自动滚动' : '锁定' }}
                    </el-button>
                  </el-button-group>
                </div>
              </div>
              
              <div class="monitor-content" ref="monitorContent">
                <div 
                  v-for="(log, index) in logs"
                  :key="index"
                  class="log-item"
                  :class="log.type">
                  <span class="log-time">{{ log.time }}</span>
                  <span class="log-direction">{{ log.direction }}</span>
                  <span class="log-data">{{ log.data }}</span>
                  <span v-if="log.hex" class="log-hex">{{ log.hex }}</span>
                </div>
                
                <div v-if="logs.length === 0" class="empty-log">
                  <i class="el-icon-info"></i>
                  <p>暂无数据，连接串口后将显示通信记录</p>
                </div>
              </div>
            </el-card>
            
            <!-- 发送数据区 -->
            <el-card class="send-panel" style="margin-top: 20px;">
              <div slot="header" class="panel-header">
                <i class="el-icon-s-promotion"></i>
                <span>发送数据</span>
              </div>
              
              <div class="send-content">
                <el-input
                  v-model="sendData"
                  type="textarea"
                  :rows="4"
                  placeholder="输入要发送的数据... (Ctrl+Enter 快速发送)"
                  :disabled="!isConnected"
                  @keydown.ctrl.enter.native="sendMessage">
                </el-input>
                
                <div class="send-options">
                  <el-radio-group v-model="sendFormat" size="small">
                    <el-radio-button label="text">文本</el-radio-button>
                    <el-radio-button label="hex">十六进制</el-radio-button>
                  </el-radio-group>
                  
                  <el-checkbox v-model="addNewLine" size="small">
                    添加换行符
                  </el-checkbox>
                </div>
                
                <el-button 
                  type="primary" 
                  icon="el-icon-s-promotion"
                  @click="sendMessage"
                  :disabled="!isConnected || !sendData"
                  style="width: 100%; margin-top: 10px;">
                  发送数据
                </el-button>
                
                <!-- 快捷命令 -->
                <el-divider content-position="left" style="margin: 20px 0 15px 0;">
                  <span style="font-size: 12px; color: #909399;">快捷命令</span>
                </el-divider>
                
                <div class="quick-commands">
                  <el-button
                    v-for="(cmd, index) in quickCommands"
                    :key="index"
                    size="small"
                    @click="sendQuickCommand(cmd.value)"
                    :disabled="!isConnected"
                    style="margin: 5px;">
                    {{ cmd.label }}
                  </el-button>
                </div>
              </div>
            </el-card>
          </el-col>
          
          <!-- 右侧统计信息 -->
          <el-col :span="6">
            <el-card class="stats-panel">
              <div slot="header" class="panel-header">
                <i class="el-icon-data-analysis"></i>
                <span>统计信息</span>
              </div>
              
              <div class="stats-content">
                <div class="stat-item">
                  <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
                    <i class="el-icon-upload2"></i>
                  </div>
                  <div class="stat-info">
                    <p class="stat-label">发送字节</p>
                    <p class="stat-value">{{ formatBytes(stats.bytesSent) }}</p>
                  </div>
                </div>
                
                <div class="stat-item">
                  <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
                    <i class="el-icon-download"></i>
                  </div>
                  <div class="stat-info">
                    <p class="stat-label">接收字节</p>
                    <p class="stat-value">{{ formatBytes(stats.bytesReceived) }}</p>
                  </div>
                </div>
                
                <div class="stat-item">
                  <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
                    <i class="el-icon-s-promotion"></i>
                  </div>
                  <div class="stat-info">
                    <p class="stat-label">消息数量</p>
                    <p class="stat-value">{{ stats.messageCount }}</p>
                  </div>
                </div>
                
                <div class="stat-item">
                  <div class="stat-icon" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);">
                    <i class="el-icon-time"></i>
                  </div>
                  <div class="stat-info">
                    <p class="stat-label">连接时长</p>
                    <p class="stat-value">{{ formatDuration(stats.connectedTime) }}</p>
                  </div>
                </div>
              </div>
            </el-card>
            
            <!-- 数据解析 -->
            <el-card class="parse-panel" style="margin-top: 20px;">
              <div slot="header" class="panel-header">
                <i class="el-icon-document"></i>
                <span>数据解析</span>
              </div>
              
              <div class="parse-content">
                <el-tabs v-model="activeParseTab" type="card">
                  <el-tab-pane label="JSON" name="json">
                    <div class="parse-result">
                      <pre v-if="parsedData.json">{{ parsedData.json }}</pre>
                      <div v-else class="empty-parse">
                        <i class="el-icon-document-copy"></i>
                        <p>接收到 JSON 数据后将自动解析</p>
                      </div>
                    </div>
                  </el-tab-pane>
                  
                  <el-tab-pane label="HEX" name="hex">
                    <div class="parse-result">
                      <pre v-if="parsedData.hex">{{ parsedData.hex }}</pre>
                      <div v-else class="empty-parse">
                        <i class="el-icon-document-copy"></i>
                        <p>接收到数据后将显示十六进制</p>
                      </div>
                    </div>
                  </el-tab-pane>
                  
                  <el-tab-pane label="ASCII" name="ascii">
                    <div class="parse-result">
                      <pre v-if="parsedData.ascii">{{ parsedData.ascii }}</pre>
                      <div v-else class="empty-parse">
                        <i class="el-icon-document-copy"></i>
                        <p>接收到数据后将显示 ASCII 码</p>
                      </div>
                    </div>
                  </el-tab-pane>
                </el-tabs>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'SerialPortTest',
  
  data() {
    return {
      // Web Serial API 对象
      port: null,
      reader: null,
      writer: null,
      readableStreamClosed: null,
      writableStreamClosed: null,
      
      // 连接状态
      isConnected: false,
      connecting: false,
      isSerialSupported: false,
      portInfo: null,
      browserInfo: '',
      
      // 网络环境
      protocol: window.location.protocol.replace(':', ''),
      currentHost: window.location.host,
      isSecureContext: window.isSecureContext,
      
      // 串口配置
      serialConfig: {
        baudRate: 115200,
        dataBits: 8,
        stopBits: 1,
        parity: 'none',
        flowControl: 'none'
      },
      
      // 日志数据
      logs: [],
      autoScroll: true,
      
      // 发送数据
      sendData: '',
      sendFormat: 'text',
      addNewLine: true,
      
      // 统计信息
      stats: {
        bytesSent: 0,
        bytesReceived: 0,
        messageCount: 0,
        connectedTime: 0,
        startTime: null
      },
      
      // 定时器
      statsTimer: null,
      
      // 解析数据
      parsedData: {
        json: '',
        hex: '',
        ascii: ''
      },
      activeParseTab: 'json',
      
      // 快速命令
      quickCommands: [
        { label: 'AT', value: 'AT' },
        { label: 'AT+GMR (版本)', value: 'AT+GMR' },
        { label: 'AT+RST (重启)', value: 'AT+RST' },
        { label: 'Hello World', value: 'Hello World' },
        { label: '测试中文', value: '测试中文' }
      ]
    }
  },
  
  mounted() {
    // 检测浏览器信息
    this.detectBrowser()
    
    // 检查浏览器是否支持 Web Serial API
    this.isSerialSupported = 'serial' in navigator
    
    if (!this.isSerialSupported) {
      console.error('Web Serial API is not supported in this browser')
      console.log('Browser Info:', this.browserInfo)
      console.log('Protocol:', this.protocol)
      console.log('Host:', this.currentHost)
      console.log('Secure Context:', this.isSecureContext)
      console.log('解决方案：')
      console.log('1. 使用 Chrome 89+ 或 Edge 89+ 浏览器')
      console.log('2. 配置 HTTPS 或使用 localhost 访问')
      console.log('3. 使用 SSH 隧道转发到本地')
    } else if (!this.isSecureContext) {
      console.warn('⚠️ 当前不是安全上下文（需要 HTTPS 或 localhost）')
      console.log('当前协议:', this.protocol)
      console.log('建议使用 HTTPS 或通过 SSH 隧道访问')
    } else {
      console.log('✓ Web Serial API 已支持')
      console.log('✓ 安全上下文已满足')
      this.$message.success('环境检测通过，可以使用串口功能')
    }
  },
  
  beforeDestroy() {
    // 断开连接
    if (this.isConnected) {
      this.disconnectSerial()
    }
    
    // 清理定时器
    if (this.statsTimer) {
      clearInterval(this.statsTimer)
    }
  },
  
  methods: {
    // 检测浏览器信息
    detectBrowser() {
      const ua = navigator.userAgent
      let browserName = '未知浏览器'
      let browserVersion = ''
      
      if (ua.indexOf('Chrome') > -1 && ua.indexOf('Edg') === -1) {
        browserName = 'Chrome'
        const match = ua.match(/Chrome\/(\d+)/)
        if (match) {
          browserVersion = match[1]
        }
      } else if (ua.indexOf('Edg') > -1) {
        browserName = 'Edge'
        const match = ua.match(/Edg\/(\d+)/)
        if (match) {
          browserVersion = match[1]
        }
      } else if (ua.indexOf('Firefox') > -1) {
        browserName = 'Firefox'
        const match = ua.match(/Firefox\/(\d+)/)
        if (match) {
          browserVersion = match[1]
        }
      } else if (ua.indexOf('Safari') > -1 && ua.indexOf('Chrome') === -1) {
        browserName = 'Safari'
        const match = ua.match(/Version\/(\d+)/)
        if (match) {
          browserVersion = match[1]
        }
      }
      
      this.browserInfo = `${browserName} ${browserVersion}`
      
      // 检查是否需要提示升级
      if (browserName === 'Chrome' && parseInt(browserVersion) < 89) {
        this.browserInfo += ' (需要 89+)'
      } else if (browserName === 'Edge' && parseInt(browserVersion) < 89) {
        this.browserInfo += ' (需要 89+)'
      } else if (browserName === 'Firefox' || browserName === 'Safari') {
        this.browserInfo += ' (不支持 Web Serial API)'
      }
    },
    
    // 连接串口
    async connectSerial() {
      if (!this.isSerialSupported) {
        this.$message.error('浏览器不支持 Web Serial API')
        return
      }
      
      if (!this.isSecureContext) {
        this.$message.error('需要 HTTPS 安全环境才能使用串口功能')
        return
      }
      
      try {
        this.connecting = true
        
        // 请求串口访问
        this.port = await navigator.serial.requestPort()
        
        // 获取端口信息
        const info = this.port.getInfo()
        this.portInfo = info
        
        // 打开串口
        await this.port.open(this.serialConfig)
        
        this.isConnected = true
        this.stats.startTime = Date.now()
        
        // 设置写入流（立即获取并保持）
        this.setupWriter()
        
        // 开始读取数据
        this.startReading()
        
        // 开始统计计时
        this.startStatsTimer()
        
        this.addLog('系统', `串口已连接 (波特率: ${this.serialConfig.baudRate})`, 'success')
        this.$message.success('串口连接成功')
        
      } catch (error) {
        console.error('连接串口失败:', error)
        
        let errorMsg = error.message
        if (error.name === 'NotFoundError') {
          errorMsg = '未选择串口设备'
        } else if (error.name === 'SecurityError') {
          errorMsg = '没有访问串口的权限'
        } else if (error.name === 'InvalidStateError') {
          errorMsg = '串口已被其他程序占用'
        }
        
        this.$message.error(`连接失败: ${errorMsg}`)
        this.addLog('系统', `连接失败: ${errorMsg}`, 'error')
      } finally {
        this.connecting = false
      }
    },
    
    // 设置写入流
    setupWriter() {
      if (this.port && this.port.writable) {
        // eslint-disable-next-line no-undef
        const textEncoderStream = new TextEncoderStream()
        this.writableStreamClosed = textEncoderStream.readable.pipeTo(this.port.writable)
        this.writer = textEncoderStream.writable.getWriter()
      }
    },
    
    // 断开串口
    async disconnectSerial() {
      try {
        this.isConnected = false
        
        // 关闭写入流
        if (this.writer) {
          try {
            await this.writer.close()
            this.writer = null
          } catch (e) {
            console.warn('关闭 writer 失败:', e)
          }
        }
        
        // 等待写入流关闭
        if (this.writableStreamClosed) {
          try {
            await this.writableStreamClosed
          } catch (e) {
            console.warn('写入流关闭失败:', e)
          }
          this.writableStreamClosed = null
        }
        
        // 取消读取
        if (this.reader) {
          try {
            await this.reader.cancel()
          } catch (e) {
            console.warn('取消 reader 失败:', e)
          }
          this.reader = null
        }
        
        // 等待读取流关闭
        if (this.readableStreamClosed) {
          try {
            await this.readableStreamClosed
          } catch (e) {
            console.warn('读取流关闭失败:', e)
          }
          this.readableStreamClosed = null
        }
        
        // 关闭串口
        if (this.port) {
          try {
            await this.port.close()
          } catch (e) {
            console.warn('关闭串口失败:', e)
          }
          this.port = null
        }
        
        this.portInfo = null
        
        // 停止统计计时
        if (this.statsTimer) {
          clearInterval(this.statsTimer)
          this.statsTimer = null
        }
        
        this.addLog('系统', '串口已断开', 'warning')
        this.$message.info('串口已断开')
        
      } catch (error) {
        console.error('断开串口失败:', error)
        this.$message.error(`断开失败: ${error.message}`)
      }
    },
    
    // 开始读取数据
    async startReading() {
      try {
        // eslint-disable-next-line no-undef
        const textDecoder = new TextDecoderStream()
        this.readableStreamClosed = this.port.readable.pipeTo(textDecoder.writable)
        this.reader = textDecoder.readable.getReader()
        
        // 持续读取数据
        // eslint-disable-next-line no-constant-condition
        while (true) {
          const { value, done } = await this.reader.read()
          
          if (done) {
            console.log('读取流已关闭')
            break
          }
          
          if (value) {
            // 更新统计
            this.stats.bytesReceived += new TextEncoder().encode(value).length
            this.stats.messageCount++
            
            // 添加日志
            this.addLog('接收', value, 'receive', new TextEncoder().encode(value))
            
            // 解析数据
            this.parseData(value, new TextEncoder().encode(value))
          }
        }
      } catch (error) {
        console.error('读取数据错误:', error)
        if (this.isConnected) {
          this.$message.error(`读取数据失败: ${error.message}`)
          this.addLog('系统', `读取错误: ${error.message}`, 'error')
        }
      } finally {
        if (this.reader) {
          try {
            this.reader.releaseLock()
          } catch (e) {
            console.warn('释放 reader 锁失败:', e)
          }
        }
      }
    },
    
    // 发送消息
    async sendMessage() {
      if (!this.isConnected || !this.sendData) {
        if (!this.isConnected) {
          this.$message.warning('请先连接串口')
        } else {
          this.$message.warning('请输入要发送的数据')
        }
        return
      }
      
      try {
        let data = this.sendData.trim()
        
        if (!data) {
          this.$message.warning('发送内容不能为空')
          return
        }
        
        // 处理十六进制格式
        if (this.sendFormat === 'hex') {
          // 移除空格和特殊字符
          const hexString = data.replace(/[^0-9A-Fa-f]/g, '')
          
          if (hexString.length === 0) {
            this.$message.warning('请输入有效的十六进制数据')
            return
          }
          
          if (hexString.length % 2 !== 0) {
            this.$message.warning('十六进制数据长度必须是偶数')
            return
          }
          
          // 转换为字节数组
          const bytes = new Uint8Array(hexString.length / 2)
          for (let i = 0; i < hexString.length; i += 2) {
            bytes[i / 2] = parseInt(hexString.substr(i, 2), 16)
          }
          
          // 发送原始字节数据
          const writer = this.port.writable.getWriter()
          await writer.write(bytes)
          writer.releaseLock()
          
          this.stats.bytesSent += bytes.length
          this.addLog('发送', `HEX: ${data}`, 'send', bytes)
          this.$message.success(`已发送 ${bytes.length} 字节 (HEX)`)
          
        } else {
          // 文本格式
          if (this.addNewLine) {
            data += '\r\n'
          }
          
          // 使用保持的 writer
          if (!this.writer) {
            this.$message.error('写入流未就绪，请重新连接')
            return
          }
          
          await this.writer.write(data)
          
          const encoded = new TextEncoder().encode(data)
          this.stats.bytesSent += encoded.length
          
          // 显示时去掉换行符
          const displayData = data.replace(/\r\n$/, '')
          this.addLog('发送', displayData, 'send', encoded)
          this.$message.success(`已发送 ${encoded.length} 字节`)
        }
        
        // 清空输入框
        this.sendData = ''
        
      } catch (error) {
        console.error('发送数据失败:', error)
        
        let errorMsg = error.message
        if (error.name === 'NetworkError') {
          errorMsg = '串口连接已断开'
          this.isConnected = false
        } else if (error.name === 'InvalidStateError') {
          errorMsg = '串口状态异常，请重新连接'
          this.isConnected = false
        }
        
        this.$message.error(`发送失败: ${errorMsg}`)
        this.addLog('系统', `发送失败: ${errorMsg}`, 'error')
      }
    },
    
    // 发送快速命令
    async sendQuickCommand(command) {
      if (!this.isConnected) {
        this.$message.warning('请先连接串口')
        return
      }
      
      this.sendData = command
      await this.sendMessage()
    },
    
    // 添加日志
    addLog(direction, data, type, rawData = null) {
      const time = new Date().toLocaleTimeString('zh-CN', { 
        hour12: false,
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
        fractionalSecondDigits: 3
      })
      
      let hex = ''
      if (rawData) {
        hex = Array.from(rawData)
          .map(b => b.toString(16).padStart(2, '0').toUpperCase())
          .join(' ')
      }
      
      this.logs.push({
        time,
        direction,
        data,
        type,
        hex
      })
      
      // 限制日志数量
      if (this.logs.length > 1000) {
        this.logs.shift()
      }
      
      // 自动滚动到底部
      if (this.autoScroll) {
        this.$nextTick(() => {
          const container = this.$refs.monitorContent
          if (container) {
            container.scrollTop = container.scrollHeight
          }
        })
      }
    },
    
    // 清空日志
    clearLog() {
      this.logs = []
      this.$message.success('日志已清空')
    },
    
    // 解析数据
    parseData(text, rawData) {
      // JSON 解析
      try {
        const json = JSON.parse(text)
        this.parsedData.json = JSON.stringify(json, null, 2)
      } catch (e) {
        // 不是有效的 JSON，保留原文本
        this.parsedData.json = text
      }
      
      // 十六进制
      if (rawData) {
        this.parsedData.hex = Array.from(rawData)
          .map(b => b.toString(16).padStart(2, '0').toUpperCase())
          .join(' ')
      }
      
      // ASCII
      if (rawData) {
        this.parsedData.ascii = Array.from(rawData)
          .map(b => `${String.fromCharCode(b)} (${b})`)
          .join('\n')
      }
    },
    
    // 开始统计计时
    startStatsTimer() {
      this.statsTimer = setInterval(() => {
        if (this.stats.startTime) {
          this.stats.connectedTime = Date.now() - this.stats.startTime
        }
      }, 1000)
    },
    
    // 格式化字节数
    formatBytes(bytes) {
      if (bytes === 0) return '0 B'
      const k = 1024
      const sizes = ['B', 'KB', 'MB', 'GB']
      const i = Math.floor(Math.log(bytes) / Math.log(k))
      return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
    },
    
    // 格式化时长
    formatDuration(ms) {
      if (ms === 0) return '0秒'
      const seconds = Math.floor(ms / 1000)
      const minutes = Math.floor(seconds / 60)
      const hours = Math.floor(minutes / 60)
      
      if (hours > 0) {
        return `${hours}时${minutes % 60}分${seconds % 60}秒`
      } else if (minutes > 0) {
        return `${minutes}分${seconds % 60}秒`
      } else {
        return `${seconds}秒`
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.serial-port-test {
  height: 100%;
  display: flex;
  flex-direction: column;
  
  .page-card {
    height: 100%;
    display: flex;
    flex-direction: column;
    
    ::v-deep .el-card__body {
      flex: 1;
      overflow: hidden;
      display: flex;
      flex-direction: column;
    }
    
    .page-header {
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
    }
  }
  
  .content {
    flex: 1;
    overflow: hidden;
    
    ::v-deep .el-row {
      height: 100%;
    }
    
    ::v-deep .el-col {
      height: 100%;
      display: flex;
      flex-direction: column;
    }
    
    .panel-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-size: 14px;
      font-weight: 500;
      
      i {
        margin-right: 6px;
        color: #409EFF;
      }
    }
    
    // 配置面板
    .config-panel {
      .config-content {
        .network-info {
          padding: 12px;
          background: #F5F7FA;
          border-radius: 6px;
          margin-bottom: 15px;
          
          .info-item {
            display: flex;
            align-items: center;
            justify-content: space-between;
            margin-bottom: 8px;
            
            &:last-child {
              margin-bottom: 0;
            }
            
            .label {
              font-size: 13px;
              color: #606266;
              font-weight: 500;
            }
            
            .value {
              font-size: 12px;
              color: #909399;
              font-family: 'Consolas', 'Monaco', monospace;
            }
          }
        }
        
        .action-buttons-top {
          margin-bottom: 20px;
          padding: 15px;
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          border-radius: 12px;
          box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
          
          .el-button {
            font-weight: 600;
            letter-spacing: 2px;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
            
            &:hover {
              transform: translateY(-2px);
              box-shadow: 0 6px 20px rgba(0, 0, 0, 0.25);
            }
            
            &:active {
              transform: translateY(0);
            }
          }
        }
        
        .status-section {
          margin-bottom: 20px;
          
          .status-indicator {
            display: flex;
            align-items: center;
            padding: 15px;
            background: linear-gradient(135deg, #F56C6C 0%, #E74C3C 100%);
            border-radius: 8px;
            color: white;
            font-weight: 500;
            
            &.connected {
              background: linear-gradient(135deg, #67C23A 0%, #27AE60 100%);
            }
            
            i {
              font-size: 24px;
              margin-right: 10px;
            }
            
            span {
              font-size: 16px;
            }
          }
          
          .port-info {
            margin-top: 10px;
            padding: 10px;
            background: #F5F7FA;
            border-radius: 6px;
            font-size: 12px;
            color: #606266;
            
            p {
              margin: 5px 0;
            }
            
            strong {
              color: #303133;
            }
          }
        }
        
        .action-buttons {
          margin-top: 20px;
        }
      }
    }
    
    // 监控面板
    .monitor-panel {
      height: calc(100% - 240px);
      display: flex;
      flex-direction: column;
      
      ::v-deep .el-card__body {
        flex: 1;
        overflow: hidden;
        display: flex;
        flex-direction: column;
        padding: 0;
      }
      
      .monitor-content {
        flex: 1;
        overflow-y: auto;
        padding: 15px;
        background: #1E1E1E;
        font-family: 'Consolas', 'Monaco', monospace;
        font-size: 12px;
        
        .log-item {
          display: flex;
          align-items: flex-start;
          margin-bottom: 8px;
          padding: 8px;
          border-radius: 4px;
          background: rgba(255, 255, 255, 0.05);
          
          &.receive {
            border-left: 3px solid #67C23A;
          }
          
          &.send {
            border-left: 3px solid #409EFF;
          }
          
          &.error {
            border-left: 3px solid #F56C6C;
            background: rgba(245, 108, 108, 0.1);
          }
          
          &.success {
            border-left: 3px solid #67C23A;
            background: rgba(103, 194, 58, 0.1);
          }
          
          &.warning {
            border-left: 3px solid #E6A23C;
            background: rgba(230, 162, 60, 0.1);
          }
          
          .log-time {
            color: #909399;
            margin-right: 10px;
            min-width: 80px;
          }
          
          .log-direction {
            color: #409EFF;
            margin-right: 10px;
            min-width: 50px;
            font-weight: 600;
          }
          
          .log-data {
            color: #E5E5E5;
            flex: 1;
            word-break: break-all;
          }
          
          .log-hex {
            color: #67C23A;
            margin-left: 10px;
            font-size: 11px;
            opacity: 0.7;
          }
        }
        
        .empty-log {
          height: 100%;
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          color: #909399;
          
          i {
            font-size: 48px;
            margin-bottom: 15px;
            opacity: 0.5;
          }
          
          p {
            font-size: 14px;
          }
        }
      }
    }
    
    // 发送面板
    .send-panel {
      height: 220px;
      
      .send-content {
        .send-options {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-top: 10px;
        }
      }
    }
    
    // 统计面板
    .stats-panel {
      .stats-content {
        padding: 10px;
        
        .stat-item {
          display: flex;
          align-items: center;
          padding: 15px;
          margin-bottom: 15px;
          background: #F5F7FA;
          border-radius: 8px;
          transition: all 0.3s;
          
          &:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
          }
          
          .stat-icon {
            width: 50px;
            height: 50px;
            border-radius: 10px;
            display: flex;
            align-items: center;
            justify-content: center;
            margin-right: 15px;
            
            i {
              font-size: 24px;
              color: white;
            }
          }
          
          .stat-info {
            flex: 1;
            
            .stat-label {
              margin: 0 0 5px 0;
              font-size: 12px;
              color: #909399;
            }
            
            .stat-value {
              margin: 0;
              font-size: 20px;
              font-weight: 600;
              color: #303133;
            }
          }
        }
      }
    }
    
    // 解析面板
    .parse-panel {
      height: calc(100% - 440px);
      display: flex;
      flex-direction: column;
      
      ::v-deep .el-card__body {
        flex: 1;
        overflow: hidden;
        padding: 0;
      }
      
      .parse-content {
        height: 100%;
        
        ::v-deep .el-tabs {
          height: 100%;
          display: flex;
          flex-direction: column;
          
          .el-tabs__content {
            flex: 1;
            overflow: hidden;
          }
          
          .el-tab-pane {
            height: 100%;
          }
        }
        
        .parse-result {
          height: 100%;
          overflow-y: auto;
          padding: 15px;
          background: #1E1E1E;
          
          pre {
            margin: 0;
            color: #E5E5E5;
            font-family: 'Consolas', 'Monaco', monospace;
            font-size: 12px;
            white-space: pre-wrap;
            word-break: break-all;
          }
          
          .empty-parse {
            height: 100%;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            color: #909399;
            
            i {
              font-size: 36px;
              margin-bottom: 10px;
              opacity: 0.5;
            }
            
            p {
              font-size: 12px;
            }
          }
        }
      }
    }
    
    // 快捷命令样式
    .quick-commands {
      display: flex;
      flex-wrap: wrap;
      gap: 5px;
      
      .el-button {
        flex: 0 0 calc(50% - 5px);
        margin: 0 !important;
        
        &:hover:not(:disabled) {
          transform: translateY(-2px);
          box-shadow: 0 4px 8px rgba(64, 158, 255, 0.3);
        }
        
        &:active:not(:disabled) {
          transform: translateY(0);
        }
      }
    }
  }
}
</style>
