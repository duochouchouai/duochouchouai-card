import { defineStore } from 'pinia'
// 保留原有请求工具（若无需后端请求可注释）
import request from '@/utils/request'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    // 从本地存储初始化状态
    token: localStorage.getItem('electron_auth_token') || '',
    userInfo: JSON.parse(localStorage.getItem('electron_user_info')) || {},
    isLogin: localStorage.getItem('electron_isLogin') === 'true' || false
  }),
  actions: {
    // 修复：异步登录（支持默认账号+后端请求）
    async login(username, password) {
      // 分支1：默认账号（admin/123456）→ 本地模拟登录
      if (username === 'admin' && password === '123456') {
        const mockRes = {
          data: {
            token: 'mock_token_' + Date.now(),
            user: { id: 1, name: '运营管理员' }
          }
        }
        // 更新状态
        this.token = mockRes.data.token
        this.userInfo = mockRes.data.user
        this.isLogin = true
        // 保存到本地存储
        localStorage.setItem('electron_auth_token', this.token)
        localStorage.setItem('electron_user_info', JSON.stringify(mockRes.data.user))
        localStorage.setItem('electron_isLogin', 'true')
        return
      }

      // 分支2：非默认账号→ 后端请求登录（保留原有逻辑）
      try {
        const res = await request({
          url: '/user/login',
          method: 'post',
          data: { username, password }
        })
        // 更新状态
        this.token = res.data.token
        this.userInfo = res.data.user
        this.isLogin = true
        localStorage.setItem('electron_auth_token', res.data.token)
        localStorage.setItem('electron_user_info', JSON.stringify(res.data.user))
        localStorage.setItem('electron_isLogin', 'true')
      } catch (err) {
        throw err // 抛出错误给Login.vue捕获
      }
    },

    // 修复：退出登录（清空状态+本地存储）
    logout() {
      this.token = ''
      this.userInfo = {}
      this.isLogin = false
      localStorage.removeItem('electron_auth_token')
      localStorage.removeItem('electron_user_info')
      localStorage.removeItem('electron_isLogin')
    }
  }
})