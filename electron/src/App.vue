<template>
  <div class="app-container">
    <!-- 登录页：仅显示登录内容 -->
    <template v-if="$route.path === '/login'">
      <main class="login-only-content">
        <router-view />
      </main>
    </template>

    <!-- 后台页：完整布局 -->
    <template v-else>
      <!-- 顶部导航栏（变宽+字体变大） -->
      <header class="app-header">
        <div class="header-title">多瞅瞅名片运管后台</div>
        <div class="header-right">
          <div class="header-user">当前：用户运营管理员</div>
          <button @click="handleLogout" class="logout-btn">退出登录</button>
        </div>
      </header>

      <!-- 主体区域（侧边栏+内容） -->
      <div class="app-body">
        <!-- 左侧导航栏（取消居中+上移+增大间距） -->
        <aside class="app-sidebar">
          <nav class="sidebar-nav">
            <router-link to="/user-stats" class="nav-item" active-class="nav-item-active">用户数据统计</router-link>
            <router-link to="/function-stats" class="nav-item" active-class="nav-item-active">功能使用统计</router-link>
            <router-link to="/behavior-stats" class="nav-item" active-class="nav-item-active">用户行为统计</router-link>
          </nav>
        </aside>

        <!-- 内容区 -->
        <main class="app-content">
          <router-view />
        </main>
      </div>
    </template>
  </div>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'

// 路由实例
const route = useRoute()
const router = useRouter()
// 登录状态实例
const authStore = useAuthStore()

// 退出登录逻辑
const handleLogout = () => {
  authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
/* 全局容器 */
.app-container {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background-color: #fff;
}

/* 登录页样式：全屏居中 */
.login-only-content {
  width: 100vw;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f8f8f8;
}

/* 顶部导航栏：变宽+字体变大 */
.app-header {
  height: 80px;
  padding: 0 25px;
  background-color: #504657;
  color: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 导航栏标题 */
.header-title {
  font-size: 22px;
  font-weight: bold;
}

/* 导航栏右侧（用户信息+退出按钮） */
.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

/* 用户信息 */
.header-user {
  font-size: 16px;
  font-weight: bold;
}

/* 退出按钮 */
.logout-btn {
  padding: 8px 16px;
  background-color: #d9534f;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  font-weight: bold;
}

.logout-btn:hover {
  background-color: #c9302c;
}

/* 主体区域 */
.app-body {
  flex: 1;
  display: flex;
  height: calc(100% - 80px);
}

/* 左侧导航栏容器：移除Flex居中样式，改为块级元素 */
.app-sidebar {
  width: 200px;
  background-color: #f8f8f8;
  border-right: 1px solid #eee;
  height: 100%;
  /* 移除以下2行Flex居中样式，彻底取消竖直/水平居中 */
  /* display: flex;
  align-items: center; */
  padding-top: 200px; /* 顶部留少量间距，避免按钮贴边 */
}

/* 导航组：靠上排列+增大间距 */
.sidebar-nav {
  display: flex;
  flex-direction: column;
  justify-content: flex-start; /* 强制靠上排列 */
  gap: 50px; /* 导航项间距 */
  width: 100%;
}

/* 导航项：水平居中（仅文字居中，不影响整体竖直排列） */
.nav-item {
  padding: 16px 20px;
  color: #504657;
  text-decoration: none;
  text-align: center; /* 仅文字水平居中 */
  transition: background-color 0.3s;
  border-radius: 4px;
}

/* 导航项hover/选中态 */
.nav-item:hover,
.nav-item-active {
  background-color: #D5C3DE;
  color: #504657;
  font-weight: 500;
}

/* 内容区 */
.app-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background-color: #fff;
}
</style>