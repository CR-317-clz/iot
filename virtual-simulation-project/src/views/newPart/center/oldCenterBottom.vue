<template>
  <div id="center">
    <div class="up">
      <div
        class="bg-color-black item"
        v-for="item in titleItem"
        :key="item.title"
      >
        <p class="ml-3 colorBlue fw-b fs-xl">{{ item.title }}</p>
        <div>
          <dv-digital-flop
            class="dv-dig-flop ml-1 mt-2 pl-3"
            :config="item.number"
          />
        </div>
      </div>
    </div>
    <div class="down">
      <div class="ranking bg-color-black">
        <span>
          <icon name="chart-pie" class="text-icon"></icon>
        </span>
        <span class="fs-xl text mx-2 mb-1 pl-3">农业区域环境异常次数</span>
        <dv-scroll-ranking-board
          class="dv-scr-rank-board mt-1"
          :config="ranking"
        />
      </div>
      <div class="percent">
        <div class="item bg-color-black">
          <span>今日作物成活率</span>
          <CenterChart
            :id="rate[0].id"
            :tips="rate[0].tips"
            :colorObj="rate[0].colorData"
          />
        </div>
        <div class="item bg-color-black">
          <span>今日水质达标率</span>
          <CenterChart
            :id="rate[1].id"
            :tips="rate[1].tips"
            :colorObj="rate[1].colorData"
          />
        </div>
        <div class="water">
          <dv-water-level-pond class="dv-wa-le-po" :config="water" />
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import CenterChart from "@/components/echart/center/centerChartRate";
// import axios from "axios";
export default {
  data() {
    return {
      titleItem: [
        {
          title: "今日平均土壤温度（℃）",
          number: {
            number: [23.5],
            toFixed: 1,
            textAlign: "left",
            content: "{nt}",
            style: {
              fontSize: 26,
            },
          },
        },
        {
          title: "今日平均水温（℃）",
          number: {
            number: [20.8],
            toFixed: 1,
            textAlign: "left",
            content: "{nt}",
            style: {
              fontSize: 26,
            },
          },
        },
        {
          title: "今日平均湿度(%)",
          number: {
            number: [68],
            toFixed: 1,
            textAlign: "left",
            content: "{nt}",
            style: {
              fontSize: 26,
            },
          },
        },
        {
          title: "今日农产品产量(kg)",
          number: {
            number: [1250],
            toFixed: 0,
            textAlign: "left",
            content: "{nt}",
            style: {
              fontSize: 26,
            },
          },
        },
        {
          title: "今日累计报警总数",
          number: {
            number: [3],
            toFixed: 0,
            textAlign: "left",
            content: "{nt}",
            style: {
              fontSize: 26,
            },
          },
        },
        {
          title: "传感器在线总数",
          number: {
            number: [142],
            toFixed: 0,
            textAlign: "left",
            content: "{nt}",
            style: {
              fontSize: 26,
            },
          },
        },
      ],
      ranking: {
        data: [
          {
            name: "一号仓库",
            value: 2,
          },
          {
            name: "三号仓库",
            value: 5,
          },
          {
            name: "八号仓库",
            value: 3,
          },
          {
            name: "十四号仓库",
            value: 1,
          },
          {
            name: "六号仓库",
            value: 4,
          },
          {
            name: "十二号仓库",
            value: 2,
          },
          {
            name: "十五号仓库",
            value: 1,
          },
        ],
        carousel: "single",
        unit: "次",
      },
      water: {
        data: [24, 45],
        shape: "roundRect",
        formatter: "{value}%",
        waveNum: 3,
      },
      // 通过率和达标率的组件复用数据
      rate: [
        {
          id: "centerRate1",
          tips: 60,
          colorData: {
            textStyle: "#3fc0fb",
            series: {
              color: ["#00bcd44a", "transparent"],
              dataColor: {
                normal: "#03a9f4",
                shadowColor: "#97e2f5",
              },
            },
          },
        },
        {
          id: "centerRate2",
          tips: 40,
          colorData: {
            textStyle: "#67e0e3",
            series: {
              color: ["#faf3a378", "transparent"],
              dataColor: {
                normal: "#ff9800",
                shadowColor: "#fcebad",
              },
            },
          },
        },
      ],
      weatherData: null, // 存储天气数据
      loading: false, // 加载状态
      error: null, // 错误信息
      dataTimer: null, // 数据更新定时器
    };
  },
  components: {
    CenterChart,
  },
  methods: {
    // 模拟实时数据更新
    updateRealTimeData() {
      // 更新土壤温度 (20-28度)
      const soilTemp = (Math.random() * 8 + 20).toFixed(1);
      this.titleItem[0].number.number = [parseFloat(soilTemp)];
      
      // 更新水温 (18-25度)
      const waterTemp = (Math.random() * 7 + 18).toFixed(1);
      this.titleItem[1].number.number = [parseFloat(waterTemp)];
      
      // 更新湿度 (50-80%)
      const humidity = Math.floor(Math.random() * 30 + 50);
      this.titleItem[2].number.number = [humidity];
      
      // 更新产量 (随机增加 0-5kg)
      const currentProduction = this.titleItem[3].number.number[0];
      const newProduction = currentProduction + Math.floor(Math.random() * 5);
      this.titleItem[3].number.number = [newProduction];
      
      // 更新传感器在线数 (135-150)
      const onlineSensors = Math.floor(Math.random() * 15 + 135);
      this.titleItem[5].number.number = [onlineSensors];
      
      // 更新水位数据
      this.water.data = [
        Math.floor(Math.random() * 30 + 20), // 20-50%
        Math.floor(Math.random() * 20 + 40)  // 40-60%
      ];
    },
    
    // 启动定时器
    startDataUpdate() {
      this.dataTimer = setInterval(() => {
        this.updateRealTimeData();
      }, 8000); // 每8秒更新一次
    },
    
    // 停止定时器
    stopDataUpdate() {
      if (this.dataTimer) {
        clearInterval(this.dataTimer);
        this.dataTimer = null;
      }
    }
  },
  created() {
    // 页面加载时启动数据更新
    this.startDataUpdate();
  },
  
  beforeDestroy() {
    // 页面销毁时停止定时器
    this.stopDataUpdate();
  },
};
</script>

<style lang="scss" scoped>
#center {
  display: flex;
  flex-direction: column;
  padding: 16px 20px; // 增加内边距，防止内容贴边
  .up {
    width: 100%;
    display: flex;
    flex-wrap: wrap;
    justify-content: space-around;
    .item {
      border-radius: 6px;
      padding-top: 8px;
      margin-top: 8px;
      width: 32%;
      height: 70px;
      .dv-dig-flop {
        width: 150px;
        height: 30px;
      }
    }
  }
  .down {
    padding: 6px 4px;
    padding-bottom: 0;
    width: 100%;
    display: flex;
    height: 255px;
    justify-content: space-between;
    .bg-color-black {
      border-radius: 5px;
    }
    .ranking {
      padding: 10px;
      width: 59%;
      .dv-scr-rank-board {
        height: 225px;
      }
    }
    .percent {
      width: 40%;
      display: flex;
      flex-wrap: wrap;
      .item {
        width: 50%;
        height: 120px;
        span {
          margin-top: 8px;
          font-size: 14px;
          display: flex;
          justify-content: center;
        }
      }
      .water {
        width: 100%;
        .dv-wa-le-po {
          height: 120px;
        }
      }
    }
  }
}
</style>
