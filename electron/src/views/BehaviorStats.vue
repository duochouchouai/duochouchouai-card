<template>
  <div class="stats-container">
    <h2>用户行为统计</h2>
    <!-- 保留卡片布局，数据改为动态变量 -->
    <div class="stats-card-group">
      <div class="stats-card">
        <p class="card-title">日均访问时长</p>
        <p class="card-value">{{ avgVisitTime || '加载中...' }}</p>
      </div>
      <div class="stats-card">
        <p class="card-title">周活跃用户</p>
        <p class="card-value">{{ weeklyActiveUser || '加载中...' }}</p>
      </div>
      <div class="stats-card">
        <p class="card-title">月留存率</p>
        <p class="card-value">{{ monthlyRetention || '加载中...' }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
// import request from '@/utils/request'

// 定义空变量，等待接口赋值（无假数据）
const avgVisitTime = ref('')
const weeklyActiveUser = ref('')
const monthlyRetention = ref('')

// 页面挂载请求数据（保留原逻辑结构）
onMounted(async () => {
  try {
    // 真实接口请求：
    // const res = await request.get('/api/stats/behavior')
    // avgVisitTime.value = res.data.avgVisitTime + '分钟'
    // weeklyActiveUser.value = res.data.weeklyActiveUser
    // monthlyRetention.value = res.data.monthlyRetention + '%'
  } catch (err) {
    avgVisitTime.value = '加载失败'
    weeklyActiveUser.value = '加载失败'
    monthlyRetention.value = '加载失败'
  }
})
</script>

<style scoped>
/* 样式完全保留，与其他统计页面一致 */
.stats-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  align-items: center;
  justify-content: flex-start;
  margin-top: 180px;
}

h2 {
  color: #504657;
  margin-bottom: 20px;
}

.stats-card-group {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.stats-card {
  padding: 20px 30px;
  background: #f8f8f8;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  min-width: 180px;
  text-align: center;
}

.card-title {
  font-size: 14px;
  color: #666;
  margin-bottom: 10px;
}

.card-value {
  font-size: 22px;
  font-weight: 700;
  color: #504657;
}
</style>