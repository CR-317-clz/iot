import Vue from "vue";
import Vuex from "vuex";
// import axios from "axios";

Vue.use(Vuex);

export default new Vuex.Store({
  state: {
    userId: '',
    role: '',
    deviceName: '',
    deviceId: '', // 添加设备ID
    relayProtocolId: '', // 添加继电器协议ID
    configId: '',
    token: '',
    selectedScreen: null, // 选中的大屏信息
    screenList: [], // 大屏列表
    deviceList: [], // 设备列表
    pollingTimer: null, // 轮询定时器
    isPolling: false // 轮询状态
  },
  mutations: {
    SET_USER_ID(state, userId) {
      state.userId = userId;
      localStorage.setItem('userId', userId);
    },
    SET_DEVICE_NAME(state, deviceName) {
      state.deviceName = deviceName;
      localStorage.setItem('deviceName', deviceName);
    },
    SET_DEVICE_ID(state, deviceId) {
      state.deviceId = deviceId;
      localStorage.setItem('deviceId', deviceId);
    },
    SET_RELAY_PROTOCOL_ID(state, relayProtocolId) {
      state.relayProtocolId = relayProtocolId;
      localStorage.setItem('relayProtocolId', relayProtocolId);
    },
    SET_CONFIG_ID(state, configId) {
      state.configId = configId;
      localStorage.setItem('configId', configId);
    },
    SET_TOKEN(state, token) {
      state.token = token;
      localStorage.setItem('token', token);
    },
    SET_SELECTED_SCREEN(state, screen) {
      state.selectedScreen = screen;
      if (screen) {
        state.deviceName = screen.deviceName || '';
        state.deviceId = screen.deviceId || screen.id || '';
        state.relayProtocolId = screen.relayProtocolId || '';
        state.configId = screen.configId || '';
        localStorage.setItem('deviceName', state.deviceName);
        localStorage.setItem('deviceId', state.deviceId);
        localStorage.setItem('relayProtocolId', state.relayProtocolId);
        localStorage.setItem('configId', state.configId);
      }
    },
    SET_SCREEN_LIST(state, list) {
      state.screenList = list;
    },
    SET_DEVICE_LIST(state, list) {
      state.deviceList = list;
    },
    SET_DEVICE_BY_ID(state, { deviceName, deviceId, relayProtocolId, configId }) {
      state.deviceName = deviceName;
      state.deviceId = deviceId || '';
      state.relayProtocolId = relayProtocolId || '';
      state.configId = configId;
      localStorage.setItem('deviceName', deviceName);
      localStorage.setItem('deviceId', deviceId || '');
      localStorage.setItem('relayProtocolId', relayProtocolId || '');
      localStorage.setItem('configId', configId);
    },
    INIT_FROM_STORAGE(state) {
      state.userId = localStorage.getItem('userId') || '';
      state.deviceName = localStorage.getItem('deviceName') || '';
      state.deviceId = localStorage.getItem('deviceId') || '';
      state.relayProtocolId = localStorage.getItem('relayProtocolId') || '';
      state.configId = localStorage.getItem('configId') || '';
      state.token = localStorage.getItem('token') || '';
    },
    CLEAR_LOGIN_DATA(state) {
      state.userId = '';
      state.deviceName = '';
      state.deviceId = '';
      state.relayProtocolId = '';
      state.configId = '';
      state.token = '';
      state.selectedScreen = null;
      localStorage.removeItem('userId');
      localStorage.removeItem('deviceName');
      localStorage.removeItem('deviceId');
      localStorage.removeItem('relayProtocolId');
      localStorage.removeItem('configId');
      localStorage.removeItem('token');
      localStorage.removeItem('role');
    },
    SET_POLLING_TIMER(state, timer) {
      state.pollingTimer = timer;
    },
    SET_POLLING_STATUS(state, status) {
      state.isPolling = status;
    }
  },
  actions: {
    setUserId({ commit }, userId) {
      commit('SET_USER_ID', userId);
    },
    setSelectedScreen({ commit }, screen) {
      commit('SET_SELECTED_SCREEN', screen);
    },
    setScreenList({ commit }, list) {
      commit('SET_SCREEN_LIST', list);
    },
      setDeviceList({ commit }, apiResponse) {
          // 处理API返回的数据，提取deviceName和id(作为configId)
          const list = apiResponse.rows || [];  // 用 rows 而不是 config

          const deviceList = list.map(item => ({
              deviceName: item.deviceName || item.configName,
              configId: item.deviceId || item.id,  // 根据你的实际字段
              configName: item.configName || item.deviceName,
              hardwareType: item.deviceType || item.hardwareType,
              host: item.host || item.host,
              topic: item.topic || item.deviceAddr,
              status: item.deviceStatus || item.status,
          }));

          commit('SET_DEVICE_LIST', deviceList);
      },
    setDeviceById({ commit }, { deviceName, deviceId, relayProtocolId, configId }) {
      commit('SET_DEVICE_BY_ID', { deviceName, deviceId, relayProtocolId, configId });
    },
    initStore({ commit }) {
      commit('INIT_FROM_STORAGE');
    },
    logout({ commit, dispatch }) {
      // 停止轮询
      dispatch('stopPolling');
      // 清除登录数据
      commit('CLEAR_LOGIN_DATA');
    },
    // 轮询管理
    initPolling({ commit, dispatch, state }) {
      if (state.isPolling) return;
      
      commit('SET_POLLING_STATUS', true);
      const timer = setInterval(() => dispatch('fetchRealTimeData'), 5000);
      commit('SET_POLLING_TIMER', timer);
    },
    
    stopPolling({ commit, state }) {
      if (state.pollingTimer) {
        clearInterval(state.pollingTimer);
        commit('SET_POLLING_TIMER', null);
      }
      commit('SET_POLLING_STATUS', false);
    },
    
    // 获取实时数据
    async fetchRealTimeData({ state }) {
      if (!state.deviceName) return;
      
      try {
        // TODO: 实现实际的API调用
        console.log('Fetching data for device:', state.deviceName);
        // const response = await axios.get(`/api/realtime/${state.deviceName}`);
        // commit('UPDATE_REAL_TIME_DATA', response.data);
      } catch (error) {
        console.error('Error fetching real-time data:', error);
      }
    }
  },
  getters: {
    getUserId: state => state.userId,
    getDeviceName: state => state.deviceName,
    getDeviceId: state => state.deviceId,
    getRelayProtocolId: state => state.relayProtocolId,
    getConfigId: state => state.configId,
    getToken: state => state.token,
    getSelectedScreen: state => state.selectedScreen,
    getScreenList: state => state.screenList,
    getDeviceList: state => state.deviceList,
    isLoggedIn: state => !!state.token,
    isPolling: state => state.isPolling
  },
  modules: {},
});
