<template>
  <div class="dashboard">
    <!-- 1. 用户统计区域 -->
    <section class="stats-section">
      <h3>用户数据统计</h3>
      <div class="stats-card-group">
        <div class="stats-card">
          <p class="card-title">总注册用户</p>
          <p class="card-value">{{ statsStore.userStats.total || 0 }}</p>
        </div>
        <div class="stats-card">
          <p class="card-title">活跃用户比例</p>
          <p class="card-value">{{ (statsStore.userStats.activeRatio * 100).toFixed(2) || 0 }}%</p>
        </div>
      </div>
      <!-- 复用EChartComponent展示用户增长趋势 -->
      <div class="chart-container">
        <EChartComponent :chartOption="userGrowthOption" />
      </div>
    </section>

    <!-- 2. 功能使用统计区域 -->
    <section class="stats-section">
      <h3>功能使用统计</h3>
      <div class="stats-card-group">
        <div class="stats-card">
          <p class="card-title">名片创建次数</p>
          <p class="card-value">{{ statsStore.functionStats.cardCreateCount || 0 }}</p>
        </div>
        <div class="stats-card">
          <p class="card-title">平均分享频率</p>
          <p class="card-value">{{ statsStore.functionStats.shareFrequency || 0 }}次/用户</p>
        </div>
        <div class="stats-card">
          <p class="card-title">用户互动次数</p>
          <p class="card-value">{{ statsStore.functionStats.interactCount || 0 }}</p>
        </div>
      </div>
    </section>

    <!-- 3. 用户行为统计区域 -->
    <section class="stats-section">
      <h3>用户行为统计</h3>
      <div class="stats-card-group">
        <div class="stats-card">
          <p class="card-title">平均使用时长</p>
          <p class="card-value">{{ statsStore.behaviorStats.avgUseTime || 0 }}分钟</p>
        </div>
        <div class="stats-card">
          <p class="card-title">平均访问深度</p>
          <p class="card-value">{{ statsStore.behaviorStats.pageDepth || 0 }}页</p>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useStatsStore } from '@/stores/statsStore'
import EChartComponent from '@/components/EChartComponent.vue'
import { use } from 'echarts/core'
import { LineChart, Line } from 'echarts/charts'
import { 
  GridComponent, 
  TooltipComponent, 
  LegendComponent,
  XAxisComponent,
  YAxisComponent
} from 'echarts/components'

use([
  LineChart, 
  Line,
  GridComponent,
  TooltipComponent,
  LegendComponent,
  XAxisComponent,
  YAxisComponent
])

const statsStore = useStatsStore()

onMounted(() => {
  statsStore.loadAllStats()
})

const userGrowthOption = {
  title: { text: '用户增长趋势' },
  tooltip: { trigger: 'axis' },
  xAxis: {
    type: 'category',
    data: statsStore.userStats.growthTrend?.map(item => item.date) || []
  },
  yAxis: { type: 'value', name: '新增用户数' },
  series: [{
    name: '新增用户',
    type: 'line',
    data: statsStore.userStats.growthTrend?.map(item => item.count) || []
  }]
}
</script>

<style scoped>
/* 看板容器：底色白色 */
.dashboard {
  width: 100%;
  height: 100%;
  padding: 20px;
  box-sizing: border-box;
  background-color: #fff; /* 底色白色 */
}

/* 统计区域标题：配色#504657 */
.stats-section {
  margin-bottom: 24px;
}

.stats-section h3 {
  font-size: 16px;
  color: #504657; /* 标题色与选中色一致 */
  margin-bottom: 12px;
  font-weight: 600;
}

/* 卡片组布局 */
.stats-card-group {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  margin-bottom: 16px;
}

/* 统计卡片：基础样式 + 选中色#504657 */
.stats-card {
  flex: 1;
  min-width: 200px;
  padding: 16px;
  background: #f8f8f8; /* 卡片基础底色（浅灰更有层次） */
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

/* 选中/hover状态：配色#504657 */
.stats-card:hover {
  background-color: #504657; /* 选中色 */
  color: #fff;
}

.card-title {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.stats-card:hover .card-title {
  color: #f5f5f5; /* 选中后文字色 */
}

.card-value {
  font-size: 20px;
  font-weight: 700;
  color: #504657; /* 数值色与选中色一致 */
}

.stats-card:hover .card-value {
  color: #fff; /* 选中后数值色 */
}

/* 图表容器：底色白色 */
.chart-container {
  width: 100%;
  height: 300px;
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  box-sizing: border-box;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}
</style>