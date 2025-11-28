<template>
  <div class="chart-wrapper">
    <v-chart :option="chartOption" style="width: 100%; height: 100%" />
  </div>
</template>

<script setup>
// 1. 正确导入vue-echarts（默认导出）
import VChart from 'vue-echarts'
// 2. 导入echarts核心及正确的组件/图表
import { use } from 'echarts/core'
// 导入图表类型（如折线图）
import { LineChart } from 'echarts/charts'
// 导入组件（修正名称：去掉Component后缀）
import {
  Grid,       // 原GridComponent → 改为Grid
  Tooltip,    // 原TooltipComponent → 改为Tooltip
  Legend,     // 原LegendComponent → 改为Legend
  XAxis,      // 原XAxisComponent → 改为XAxis（当前错误的根源）
  YAxis       // 原YAxisComponent → 改为YAxis
} from 'echarts/components'

// 3. 注册ECharts所需模块（使用修正后的名称）
use([
  LineChart,
  Grid,
  Tooltip,
  Legend,
  XAxis,
  YAxis
])

// 接收父组件传的图表配置
const props = defineProps({
  chartOption: {
    type: Object,
    required: true,
    default: () => ({})
  }
})
</script>

<style scoped>
.chart-wrapper {
  width: 100%;
  height: 100%;
  min-height: 200px;
}
</style>