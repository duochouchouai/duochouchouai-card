<template>
  <div class="stats-container">
    <h2>功能使用统计</h2>
    <!-- 保留卡片布局，数据改为动态变量 -->
    <div class="stats-card-group">
      <div class="stats-card">
        <p class="card-title">卡片创建数</p>
        <p class="card-value">{{ cardCreateCount || '加载中...' }}</p>
      </div>
      <div class="stats-card">
        <p class="card-title">卡片分享数</p>
        <p class="card-value">{{ cardShareCount || '加载中...' }}</p>
      </div>
      <div class="stats-card">
        <p class="card-title">卡片查看数</p>
        <p class="card-value">{{ cardViewCount || '加载中...' }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
// import request from '@/utils/request'

// 定义空变量，等待接口赋值（无假数据）
const cardCreateCount = ref('')
const cardShareCount = ref('')
const cardViewCount = ref('')

// 页面挂载请求数据（保留原逻辑结构）
onMounted(async () => {
  try {
    // 真实接口请求：
    // const res = await request.get('/api/stats/function')
    // cardCreateCount.value = res.data.cardCreateCount
    // cardShareCount.value = res.data.cardShareCount
    // cardViewCount.value = res.data.cardViewCount
  } catch (err) {
    cardCreateCount.value = '加载失败'
    cardShareCount.value = '加载失败'
    cardViewCount.value = '加载失败'
  }
})
</script>

<style scoped>
/* 样式完全保留，与用户数据统计一致 */
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