<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-title">智能化在线教学支持服务平台</div>
      <div class="login-subtitle">管理中心登录</div>
      <el-form ref="formRef" :model="form" :rules="rules" size="large">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="账号" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" show-password :prefix-icon="Lock" @keyup.enter="submit" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" style="width:100%" @click="submit">登录</el-button>
        </el-form-item>
      </el-form>
      <div class="login-tip">
        还没有账号？<el-button link type="primary" @click="$router.push('/register')">立即注册</el-button>
      </div>
      <div class="login-tip" style="margin-top: 4px">默认账号：admin / 密码：admin（首次登录后请到"个人信息"修改密码）</div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)
// TODO: [admin 默认账号] [P1] 原表单默认填充 `admin/admin` 方便本地开发,但生产环境暴露
//       默认凭据是严重问题.已清空;开发期如需预填,可改为只在 import.meta.env.DEV
//       且 localStorage.dev_autofill === '1' 才回填.
const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

/**
 * 登录成功后的目标地址：后台工作台首页（同域路由跳转）
 */
const ADMIN_HOME_URL = '/dashboard'

const submit = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    await userStore.loginAction(form)
    await userStore.fetchUserInfo()
    ElMessage.success('登录成功')
    router.push(ADMIN_HOME_URL)
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-container {
  height: 100vh;
  background: linear-gradient(135deg, #667eea, #764ba2);
  display: flex;
  align-items: center;
  justify-content: center;
}
.login-box {
  width: 420px;
  padding: 40px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.2);
}
.login-title { font-size: 22px; font-weight: bold; text-align: center; }
.login-subtitle { color: #999; text-align: center; margin: 8px 0 24px; }
.login-tip { color: #999; text-align: center; font-size: 12px; margin-top: 8px; }
</style>
