<template>
  <div>
    <!-- 年度开工率 -->
    <Echart
      :options="options"
      id="bottomLeftChart"
      height="580px"
      width="100%"
    ></Echart>
  </div>
</template>

<script>
import Echart from "@/common/echart";
export default {
  data() {
    return {
      options: {},
      data:[5,6,6,8,15,13,16,12,14,13,15,16,13,15,16,16,14,15,6,5,6,3,5,5],
      data2:[25,26,26,20,24,23,22,59,58,54,52,56,52,30,41,30,42,25,63,63,63,55,64,56]
    };
  },
  components: {
    Echart,
  },
  props: {
    cdata: {
      type: Object,
      default: () => ({}),
    },
  },
  watch: {
    cdata: {
      handler(newData) {
        this.options = {
          tooltip: {
            trigger: "axis",
            backgroundColor: "rgba(255,255,255,0.1)",
            axisPointer: {
              type: "shadow",
              label: {
                show: true,
                backgroundColor: "#7B7DDC",
              },
            },
          },
          legend: {
            data: ["湿度", "温度"],
            textStyle: {
              color: "#B4B4B4",
            },
            top: "0%",
          },
          grid: {
            x: "8%",
            width: "88%",
            y: "4%",
          },
          xAxis: {
            data: newData.category,
            axisLine: {
              lineStyle: {
                color: "#B4B4B4",
              },
            },
            axisTick: {
              show: false,
            },
          },
          yAxis: [
            {
              splitLine: { show: false },
              axisLine: {
                lineStyle: {
                  color: "#B4B4B4",
                },
              },

              axisLabel: {
                formatter: "{value} ",
              },
            },
            {
              splitLine: { show: false },
              axisLine: {
                lineStyle: {
                  color: "#B4B4B4",
                },
              },
              axisLabel: {
                formatter: "{value} ",
              },
            },
          ],
          series: [
            {
              name: "温度",
              type: "bar",
              barWidth: 10,
              itemStyle: {
                normal: {
                  barBorderRadius: 5,
                  color: new this.$echarts.graphic.LinearGradient(0, 0, 0, 1, [
                    { offset: 0, color: "#956FD4" },
                    { offset: 1, color: "#3EACE5" },
                  ]),
                },
              },
              data: this.data,
            },
            {
              name: "湿度",
              type: "line",
              barWidth: 10,
              itemStyle: {
                normal: {
                  barBorderRadius: 5,
                  color: new this.$echarts.graphic.LinearGradient(0, 0, 0, 1, [
                    { offset: 0, color: "#956FD4" },
                    { offset: 1, color: "#3EACE5" },
                  ]),
                },
              },
              data: this.data2,
            },
          ],
        };
      },
      immediate: true,
      deep: true,
    },
  },
};
</script>
