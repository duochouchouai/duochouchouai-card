<template>
  <div class="stats-container">
    <h2>用户数据统计</h2>
    <!-- 保留卡片布局，数据部分改为动态变量（等待接口赋值） -->
    <div class="stats-card-group">
      <div class="stats-card">
        <p class="card-title">总注册用户</p>
        <p class="card-value">{{ totalUser || '加载中...' }}</p> <!-- 无假数据，显示加载提示 -->
      </div>
      <div class="stats-card">
        <p class="card-title">日活用户</p>
        <p class="card-value">{{ dailyActiveUser || '加载中...' }}</p>
      </div>
      <div class="stats-card">
        <p class="card-title">活跃用户比例</p>
        <p class="card-value">{{ activeRate || '加载中...' }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
// 引入请求工具（后续对接后端用，当前仅定义结构）
// import request from '@/utils/request'

// 定义空的响应式变量（替代之前的假数据，等待接口赋值）
const totalUser = ref('')
const dailyActiveUser = ref('')
const activeRate = ref('')

// 页面挂载时请求数据（假数据前的核心逻辑，当前注释仅保留结构）
onMounted(async () => {
  try {
    // 真实项目中替换为后端接口：
    // const res = await request.get('/api/stats/user')
    // 赋值逻辑：
    // totalUser.value = res.data.totalUser
    // dailyActiveUser.value = res.data.dailyActiveUser
    // activeRate.value = res.data.activeRate + '%'
  } catch (err) {
    // 错误处理：数据加载失败时提示
    totalUser.value = '加载失败'
    dailyActiveUser.value = '加载失败'
    activeRate.value = '加载失败'
  }
})
</script>

<style scoped>
/* 保留原布局样式，无修改 */
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