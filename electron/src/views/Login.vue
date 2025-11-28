<template>
  <div class="login-container">
    <div class="login-card">
      <h2 class="login-title">多瞅瞅名片运管后台</h2>
      
      <!-- 账号输入 -->
      <div class="form-group">
        <label>账号</label>
        <input 
          type="text" 
          v-model="username" 
          placeholder="请输入账号"
          class="form-input"
          value="admin"
        >
      </div>
      
      <!-- 密码输入 -->
      <div class="form-group">
        <label>密码</label>
        <input 
          type="password" 
          v-model="password" 
          placeholder="请输入密码"
          class="form-input"
          value="123456"
        >
      </div>
      
      <!-- 验证码输入区域（默认填充1234） -->
      <div class="form-group code-group">
        <label>验证码</label>
        <input 
          type="text" 
          v-model="code" 
          placeholder="请输入短信验证码"
          class="form-input code-input"
        >
        <!-- 获取验证码按钮（带倒计时） -->
        <button 
          @click="getCode" 
          class="get-code-btn"
          :disabled="isSending || countdown > 0"
        >
          {{ countdown > 0 ? `${countdown}秒后重发` : '获取验证码' }}
        </button>
      </div>
      
      <!-- 登录按钮 -->
      <button @click="handleLogin" class="login-btn">登录</button>
      <p class="error-tip" v-if="errorMsg">{{ errorMsg }}</p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'

// 表单数据
const username = ref('admin')
const password = ref('123456')
const code = ref('1234') // 核心：默认验证码设为1234
const errorMsg = ref('')

// 验证码倒计时状态
const countdown = ref(0)
const isSending = ref(false)

const router = useRouter()
const authStore = useAuthStore()

// 获取短信验证码（保留原功能）
const getCode = async () => {
  if (!username.value.trim()) {
    errorMsg.value = '请先输入账号'
    return
  }

  try {
    isSending.value = true
    // 调用后端发送验证码接口（实际项目替换为真实接口）
    // const res = await request.post('/api/send-code', { username: username.value })
    // if (res.code !== 200) throw new Error(res.msg)

    // 启动60秒倒计时
    countdown.value = 60
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (err) {
    errorMsg.value = err.message || '获取验证码失败，请重试'
  } finally {
    isSending.value = false
  }
}

// 登录逻辑（保留验证码校验）
const handleLogin = async () => {
  if (!username.value.trim() || !password.value.trim() || !code.value.trim()) {
    errorMsg.value = '账号、密码、验证码不能为空'
    return
  }

  try {
    // 调用登录接口（需后端支持验证码验证）
    await authStore.login(username.value, password.value, code.value)
    router.push('/user-stats')
  } catch (err) {
    errorMsg.value = err.message || '登录失败，请重试'
  }
}
</script>

<style scoped>
.login-container {
  width: 100vw;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f8f8f8;
}
.login-card {
  width: 380px;
  padding: 30px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}
.login-title {
  text-align: center;
  color: #504657;
  margin-bottom: 20px;
}
.form-group {
  margin-bottom: 15px;
}
.code-group {
  display: flex;
  align-items: flex-end;
  gap: 10px;
}
.code-input {
  flex: 1;
}
.get-code-btn {
  padding: 10px 12px;
  background-color: #504657;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}
.get-code-btn:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}
.form-group label {
  display: block;
  margin-bottom: 5px;
  color: #666;
}
.form-input {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  box-sizing: border-box;
  background-color: #f5f0e6;
  color: #333;
}
.login-btn {
  width: 100%;
  padding: 12px;
  background-color: #504657;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
}
.login-btn:hover {
  background-color: #63576b;
}
.error-tip {
  color: #ff4d4f;
  text-align: center;
  margin-top: 10px;
  font-size: 14px;
}
</style>