<template>
  <div>
    <Chart :cdata="cdata" />
  </div>
</template>

<script>
import Chart from './chart.vue'
export default {
  data () {
    return {
      drawTiming: null,
      cdata: {
        year: null,
        weekCategory: [],
        radarData: [],
        radarDataAvg: [],
        maxData: 80,
        weekMaxData: [],
        weekLineData: []
      }
    }
  },
  components: {
    Chart,
  },
  mounted () {
    // this.drawTimingFn();
    this.setData();
  },
  beforeDestroy () {
    clearInterval(this.drawTiming);
  },
  methods: {
    // drawTimingFn () {
    //   this.setData();
    //   this.drawTiming = setInterval(() => {
    //     this.setData();
    //   }, 6000);
    // },
    setData () {
      // 清空轮询数据
      this.cdata.weekCategory = [];
      this.cdata.weekMaxData = [];
      this.cdata.weekLineData = [30,40,21,35,61,30,50];
      this.cdata.radarData = [];
      this.cdata.radarDataAvg = [];

      let dateBase = new Date();
      this.cdata.year = dateBase.getFullYear();
      // 周数据
      for (let i = 0; i < 7; i++) {
        // 日期
        let date = new Date();
        this.cdata.weekCategory.unshift([date.getMonth() + 1, date.getDate()-i].join("/"));

        // 折线图数据
        // this.cdata.weekMaxData.push(this.cdata.maxData);
        // let distance = Math.round(Math.random() * 110 + 5);
        // this.cdata.weekLineData.push(distance);

        // 雷达图数据
        // 我的指标
        let averageSpeed = 9;
        let maxSpeed = 4;
        let hour = 9;
        let radarDayData = [8, averageSpeed, maxSpeed, hour];
        this.cdata.radarData.unshift(radarDayData);

        // 平均指标
        let distanceAvg = 7;
        let averageSpeedAvg = 8;
        let maxSpeedAvg = 5;
        let hourAvg = 9;
        let radarDayDataAvg = [
          distanceAvg,
          averageSpeedAvg,
          maxSpeedAvg,
          hourAvg
        ];
        this.cdata.radarDataAvg.unshift(radarDayDataAvg);
      }

    }
  }
};
</script>

<style lang="scss" scoped>
</style>