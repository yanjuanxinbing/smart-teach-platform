<template>
  <div class="page auth">
    <div class="auth__panel">
      <header class="auth__head">
        <router-link to="/" class="auth__back">
          <el-icon :size="13"><Back /></el-icon>
          <span>返回门户首页</span>
        </router-link>
        <span class="eyebrow"><span class="eyebrow__rule"></span>Sign in</span>
        <h1 class="auth__title">账号<em>登录</em></h1>
        <p class="auth__lead">登录后可查看个人中心、收藏的课程与笔记；老师/管理员可一键进入管理中心。</p>
      </header>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="auth__form" @submit.prevent="onSubmit">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" :prefix-icon="User" autofocus />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" @keyup.enter="onSubmit" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitting" class="auth__submit" @click="onSubmit">登 录</el-button>
        </el-form-item>
        <p class="auth__hint">
          还没有账号？
          <router-link to="/register" class="auth__link">立即注册</router-link>
        </p>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Back, User } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const formRef = ref()
const submitting = ref(false)
const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const onSubmit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    await userStore.loginAction({ username: form.username.trim(), password: form.password })
    // 拉到完整用户信息，给顶部「个人姓名」等展示使用
    try { await userStore.fetchUserInfo() } catch (e) { /* swallow */ }

    const intent = String(route.query.intent || '')
    const redirect = String(route.query.redirect || '')

    // 从「管理中心」触发的登录：教师/管理员 -> 后台；学生 -> 拒绝
    if (intent === 'admin') {
      const r = userStore.roleCode
      if (r === 'ADMIN' || r === 'TEACHER') {
        ElMessage.success('登录成功，正在进入管理中心')
        window.location.href = 'http://localhost:8081/'
        return
      }
      // 学生身份：拒绝访问后台
      ElMessage.error('您没有管理员权限')
      await userStore.logout()
      router.replace({ path: '/login', query: { ...route.query, intent: '' } })
      return
    }

    ElMessage.success(`欢迎回来，${userStore.userInfo?.realName || userStore.userInfo?.username || form.username}`)
    router.replace(redirect || '/')
  } catch (e) {
    // request.js 已提示过错误
  } finally { submitting.value = false }
}
</script>

<style scoped>
.auth {
  min-height: calc(100vh - var(--header-h));
  display: flex; align-items: center; justify-content: center;
  padding: clamp(32px, 6vw, 88px) 24px;
  background: linear-gradient(180deg, #FFFFFF 0%, #F4F7FF 100%);
}
.auth__panel {
  width: min(420px, 100%);
  background: var(--surface);
  border: 1px solid var(--line);
  padding: clamp(28px, 4vw, 44px);
}
.auth__head { margin-bottom: var(--s-6); }
.auth__back { display: inline-flex; align-items: center; gap: 6px; font-family: var(--font-mono); font-size: 11px; letter-spacing: 0.18em; text-transform: uppercase; color: var(--mute); margin-bottom: var(--s-4); }
.auth__back:hover { color: var(--accent); }
.eyebrow { display: inline-flex; align-items: center; gap: 8px; font-family: var(--font-mono); font-size: 11px; letter-spacing: 0.18em; text-transform: uppercase; color: var(--mute); }
.eyebrow__rule { display: inline-block; width: 26px; height: 1px; background: var(--accent); }
.auth__title { font-family: var(--font-display); font-size: clamp(28px, 3.4vw, 40px); font-weight: 600; color: var(--ink); margin: 6px 0 6px; letter-spacing: -0.018em; }
.auth__title em { font-family: var(--font-serif); font-style: italic; font-weight: 500; color: var(--accent); }
.auth__lead { color: var(--ink-soft); margin: 0 0 var(--s-5); font-size: 14px; line-height: 1.7; }

.auth__form :deep(.el-form-item__label) { font-weight: 500; color: var(--ink); font-size: 13px; }
.auth__form :deep(.el-input__wrapper) { border-radius: 0; }
.auth__submit { width: 100%; height: 42px; border-radius: 0; background: var(--ink); border-color: var(--ink); }
.auth__submit, .auth__submit:hover { background: var(--ink); border-color: var(--ink); color: #fff; }
.auth__hint { font-size: 13px; color: var(--ink-soft); margin: 4px 0 0; text-align: center; }
.auth__link { color: var(--accent); text-decoration: none; }
.auth__link:hover { text-decoration: underline; }
</style>
