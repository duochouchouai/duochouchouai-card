import { createRouter, createWebHashHistory } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'

// 路由配置（修复路径+权限标记）
const routes = [
  // 登录页（无需权限）
  { 
    path: '/login', 
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  },
  // 用户数据统计（需登录）
  { 
    path: '/user-stats', 
    component: () => import('@/views/UserStats.vue'),
    meta: { requiresAuth: true }
  },
  // 功能使用统计（需登录）
  { 
    path: '/function-stats', 
    component: () => import('@/views/FunctionStats.vue'),
    meta: { requiresAuth: true }
  },
  // 用户行为统计（需登录）
  { 
    path: '/behavior-stats', 
    component: () => import('@/views/BehaviorStats.vue'),
    meta: { requiresAuth: true }
  },
  // 默认重定向到登录页
  { path: '/', redirect: '/login' }
]

// 创建路由实例
const router = createRouter({
  history: createWebHashHistory(),
  routes
})

// 修复：路由守卫（拦截未登录请求）
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  // 若页面需要权限且未登录→ 跳转到登录页
  if (to.meta.requiresAuth && !authStore.isLogin) {
    next('/login')
  } else {
    next() // 已登录/无需权限→ 正常跳转
  }
})

export default router