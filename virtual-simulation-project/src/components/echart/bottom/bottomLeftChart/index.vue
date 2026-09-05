<template>
  <div>
    <Chart :cdata="cdata" />
  </div>
</template>

<script>
import Chart from "./chart.vue";
import { mapState } from "vuex";
export default {
  data() {
    return {
      cdata: {
        category: [
          "1时",
          "2时",
          "3时",
          "4时",
          "5时",
          "6时",
          "7时",
          "8时",
          "9时",
          "10时",
          "11时",
          "12时",
          "13时",
          "14时",
          "15时",
          "16时",
          "17时",
          "18时",
          "19时",
          "20时",
          "21时",
          "22时",
          "23时",
          "24时",
        ],
        barData: [6, 1, 2, 2, 2, 3, 3, 3, 4, 4, 5, 3, 3, 4, 4, 5, 5],
        rateData: [],
      },
    };
  },
  components: {
    Chart,
  },
  mounted() {},
  methods: {
    reloadData(data) {
      const processData = data.map((item) => item[1]);
      this.cdata.barData = processData.map((item) =>
        parseInt(item.replace(/kWh$/, ""), 10)
      );
      console.log("柱状图更新", processData);
    },
  },
  computed: {
    ...mapState(["processedData"]),
  },
  watch: {
    processedData: {
      immediate: true, // 立即触发一次
      deep: true, // 深度监听
      handler(newVal) {
        this.reloadData(newVal);
      },
    },
  },
};
</script>

<style lang="scss" scoped></style>
