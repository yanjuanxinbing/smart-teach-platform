<template>
  <div class="page auth">
    <div class="auth__panel">
      <header class="auth__head">
        <router-link to="/" class="auth__back">
          <el-icon :size="13"><Back /></el-icon>
          <span>返回门户首页</span>
        </router-link>
        <span class="eyebrow"><span class="eyebrow__rule"></span>Sign up</span>
        <h1 class="auth__title">账号<em>注册</em></h1>
        <p class="auth__lead">教师 / 学生身份自助注册，注册完成即可登录门户浏览与登录后受保护内容。</p>
      </header>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="auth__form" @submit.prevent="onSubmit">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="form.username" placeholder="3-20 位字母/数字" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="角色">
              <el-select v-model="form.roleCode" placeholder="选择身份">
                <el-option label="教师" value="TEACHER" />
                <el-option label="学生" value="STUDENT" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入您的姓名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="name@example.com" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="11 位手机号（选填）" maxlength="11" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="6 位以上" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" show-password placeholder="再次输入密码" @keyup.enter="onSubmit" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitting" class="auth__submit" @click="onSubmit">注 册</el-button>
        </el-form-item>
        <p class="auth__hint">
          已有账号？
          <router-link to="/login" class="auth__link">直接登录</router-link>
        </p>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Back } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref()
const submitting = ref(false)
const form = reactive({
  username: '',
  roleCode: 'TEACHER',
  realName: '',
  email: '',
  phone: '',
  password: '',
  confirmPassword: ''
})
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { pattern: /^[A-Za-z0-9_]{3,20}$/, message: '3-20 位字母/数字/下划线', trigger: 'blur' }
  ],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
  phone: [{ pattern: /^$|^1\d{10}$/, message: '请输入 11 位手机号', trigger: 'blur' }],
  password: [{ required: true, min: 6, message: '至少 6 位', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: (_, v, cb) => v === form.password ? cb() : cb(new Error('两次密码不一致')) }
  ]
}

const onSubmit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    await userStore.registerAction({
      username: form.username.trim(),
      password: form.password,
      confirmPassword: form.confirmPassword,
      roleCode: form.roleCode,
      realName: form.realName.trim(),
      email: form.email.trim(),
      phone: form.phone.trim()
    })
    try { await userStore.fetchUserInfo() } catch (e) {}
    ElMessage.success('注册成功，欢迎加入')
    router.replace('/')
  } catch (e) {
    // request.js 已提示错误信息
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
  width: min(560px, 100%);
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
.auth__form :deep(.el-input__wrapper), .auth__form :deep(.el-select__wrapper) { border-radius: 0; }
.auth__submit { width: 100%; height: 42px; border-radius: 0; background: var(--ink); border-color: var(--ink); }
.auth__submit, .auth__submit:hover { background: var(--ink); border-color: var(--ink); color: #fff; }
.auth__hint { font-size: 13px; color: var(--ink-soft); margin: 4px 0 0; text-align: center; }
.auth__link { color: var(--accent); text-decoration: none; }
.auth__link:hover { text-decoration: underline; }
</style>
