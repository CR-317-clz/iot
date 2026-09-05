<template>
  <div class="sensor-list-wrapper">
    <div class="sensor-list-header">
      <span class="header-name">名称</span>
      <span class="header-value">数值</span>
      <span class="header-status">状态</span>
    </div>
    <div class="sensor-list-scroll">
      <ul class="sensor-list" :style="scrollStyle" ref="scrollList">
        <li v-for="item in sensors" :key="item.id" class="sensor-item">
          <div class="sensor-name">{{ item.name }}</div>
          <div class="sensor-value">{{ item.value }}</div>
          <div class="sensor-status" :class="item.status">
            {{ item.status === "true" ? "正常" : "异常" }}
          </div>
        </li>
        <!-- 追加一组数据用于无缝循环 -->
        <li
          v-for="item in sensors"
          :key="'copy-' + item.id"
          class="sensor-item"
        >
          <div class="sensor-name">{{ item.name }}</div>
          <div class="sensor-value">{{ item.value }}</div>
          <div class="sensor-status" :class="item.status">
            {{ item.status === "true" ? "正常" : "异常" }}
          </div>
        </li>
      </ul>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      sensors: [
        { id: 1, name: "温度传感器", value: "26°C", status: "true" },
        { id: 2, name: "湿度传感器", value: "60%", status: "true" },
        { id: 3, name: "烟雾传感器", value: "无", status: "true" },
        { id: 4, name: "水浸传感器", value: "无", status: "false" },
        { id: 5, name: "门磁传感器", value: "关闭", status: "true" },
        { id: 6, name: "红外传感器", value: "无", status: "true" },
      ],
      scrollTop: 0,
      itemHeight: 0,
      timer: null,
      duration: 2000,
    };
  },
  computed: {
    scrollStyle() {
      return {
        transform: `translateY(-${this.scrollTop}px)`,
        transition: "transform 0.5s",
      };
    },
  },
  mounted() {
    this.$nextTick(() => {
      const el = this.$refs.scrollList.querySelector(".sensor-item");
      this.itemHeight = el ? el.offsetHeight : 40;
      this.startScroll();
    });
  },
  beforeDestroy() {
    clearInterval(this.timer);
  },
  methods: {
    startScroll() {
      this.timer = setInterval(() => {
        this.scrollTop += this.itemHeight;
        if (this.scrollTop >= this.itemHeight * this.sensors.length) {
          this.scrollTop = 0;
        }
      }, this.duration);
    },
  },
};
</script>

<style lang="scss" scoped>
.sensor-list-wrapper {
  width: 100%;
  height: 100%;
  min-height: 0;
  overflow: hidden;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(35, 52, 92, 0.12);
  border: 1.5px solid #23345c;
  position: relative;
  display: flex;
  flex-direction: column;
  background: none;
  padding: 12px;
  box-sizing: border-box;
}
.sensor-list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
  color: #fff;
  font-size: 24px;
  padding: 10px 16px 10px 16px;
  border-bottom: 2px solid #257dff;
  background: none;
  letter-spacing: 2px;
}
.header-name,
.header-value,
.header-status {
  flex: 1;
  text-align: left;
  font-size: 18px;
}

.header-value {
  text-align: center;
}

.header-status {
  text-align: right;
}

.sensor-list-scroll {
  flex: 1;
  overflow: hidden;
  position: relative;
}
.sensor-list {
  list-style: none;
  margin: 0;
  padding: 10px 16px 10px 16px;
}
.sensor-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 17px;
  background: none;
  border-bottom: 1px solid #23345c;
  border-radius: 6px;
  margin-bottom: 4px;
  height: 40px;
  color: #257dff;
  letter-spacing: 1px;
  position: relative;
  .sensor-value {
    color: rgb(51, 187, 167)
  }
}
.sensor-item:last-child {
  border-bottom: none;
  margin-bottom: 0;
}
.sensor-status {
  padding: 3px 14px;
  border-radius: 14px;
  font-size: 16px;
  font-weight: bold;
  letter-spacing: 1px;
  text-align: right;
  min-width: 70px;
}
.sensor-status.true {
  color: #4CAF50;
}
.sensor-status.false {
  color: #e74c3c;
}
</style>
