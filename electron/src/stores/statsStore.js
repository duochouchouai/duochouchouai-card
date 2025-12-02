import { defineStore } from 'pinia'
import request from '@/utils/request'

export const useStatsStore = defineStore('stats', {
  state: () => ({
    // 用户统计数据
    userStats: {
      total: 0, // 总注册用户
      activeRatio: 0, // 活跃用户比例
      growthTrend: [] // 用户增长趋势（按天/周）
    },
    // 功能使用数据
    functionStats: {
      cardCreateCount: 0, // 名片创建次数
      shareFrequency: 0, // 分享频率
      interactCount: 0 // 用户互动次数
    },
    // 用户行为数据
    behaviorStats: {
      avgUseTime: 0, // 平均使用时长（分钟）
      pageDepth: 0, // 访问页面深度
      clickRate: {} // 关键功能点击率
    }
  }),
  actions: {
    // 加载所有统计数据（需后端提供统计接口，参考Swagger文档：http://localhost:3000/api-docs）
    async loadAllStats() {
      // 1. 获取用户统计
      const userRes = await request.get('/stats/users')
      this.userStats = userRes.data

      // 2. 获取功能使用统计
      const funcRes = await request.get('/stats/functions')
      this.functionStats = funcRes.data

      // 3. 获取用户行为统计
      const behaviorRes = await request.get('/stats/behaviors')
      this.behaviorStats = behaviorRes.data
    }
  }
})