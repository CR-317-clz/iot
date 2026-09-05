<template>
  <div ref="chart" style="width: 100%; height: 220px;"></div>
</template>

<script>
import * as echarts from 'echarts';
export default {
  name: 'TrendChart',
  props: {
    data: {
      type: Array,
      default: () => []
    }
  },
  mounted() {
    this.renderChart();
  },
  watch: {
    data() {
      this.renderChart();
    }
  },
  methods: {
    renderChart() {
      if (!this.$refs.chart) return;
      const chart = echarts.init(this.$refs.chart);
      const hours = this.data.map(item => item.hour);
      const values = this.data.map(item => item.value);
      const unit = this.$parent.getSensorUnit ? this.$parent.getSensorUnit(this.$parent.selectedSensor) : '';
      chart.setOption({
        tooltip: {
          trigger: 'axis',
          backgroundColor: '#fff',
          borderColor: '#409eff',
          borderWidth: 1,
          textStyle: { color: '#303133' },
          padding: 8,
          formatter: params => {
            const val = params[0].value;
            return `${params[0].axisValue}<br/>数值: <b style='color:#409eff'>${val}${unit}</b>`;
          }
        },
        grid: {
          left: '5%',
          right: '5%',
          top: 24,
          bottom: 24
        },
        xAxis: {
          type: 'category',
          data: hours,
          axisLabel: { color: '#909399', fontSize: 13 },
          axisLine: { lineStyle: { color: '#e4e7ed' } },
        },
        yAxis: {
          type: 'value',
          axisLabel: {
            color: '#909399',
            fontSize: 13,
            formatter: value => `${value}${unit}`
          },
          splitLine: { lineStyle: { color: '#f0f2f5', type: 'dashed' } },
          axisLine: { show: false },
        },
        series: [{
          data: values,
          type: 'line',
          smooth: true,
          symbol: 'circle',
          symbolSize: 6,
          lineStyle: { color: '#409eff', width: 2 },
          itemStyle: { color: '#409eff', borderColor: '#fff', borderWidth: 2 },
          areaStyle: { color: 'rgba(64,158,255,0.10)' },
          label: {
            show: false
          }
        }]
      });
    }
  }
};
</script>

<style scoped>
</style>
