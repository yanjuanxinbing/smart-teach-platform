<template>
  <div class="login-container">
    <div class="login-box register-box">
      <div class="login-title">智能化在线教学支持服务平台</div>
      <div class="login-subtitle">注册账号</div>

      <el-form ref="formRef" :model="form" :rules="rules" size="large" @submit.prevent>
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="工号 / 学号（登录账号）" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码（6-20 位）" show-password :prefix-icon="Lock" />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="确认密码" show-password :prefix-icon="Lock" />
        </el-form-item>
        <el-form-item prop="realName">
          <el-input v-model="form.realName" placeholder="姓名" :prefix-icon="UserFilled" />
        </el-form-item>
        <el-form-item prop="phone">
          <el-input v-model="form.phone" placeholder="手机号（11 位数字）" :prefix-icon="Phone"
            :maxlength="11" @input="form.phone = (form.phone || '').replace(/\D/g, '').slice(0, 11)" />
        </el-form-item>
        <el-form-item prop="email">
          <el-input v-model="form.email" placeholder="邮箱" :prefix-icon="Message" />
        </el-form-item>
        <el-form-item prop="roleCode">
          <el-radio-group v-model="form.roleCode">
            <el-radio value="ROLE_TEACHER">教师</el-radio>
            <el-radio value="ROLE_STUDENT">学生</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" style="width:100%" @click="submit">注册并登录</el-button>
        </el-form-item>
      </el-form>

      <div class="login-tip">
        已有账号？<router-link to="/login">返回登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, UserFilled, Phone, Message } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  realName: '',
  phone: '',
  email: '',
  roleCode: 'ROLE_TEACHER'
})

const validateConfirm = (rule, value, cb) => {
  if (value !== form.password) cb(new Error('两次密码不一致'))
  else cb()
}

const rules = {
  username: [
    { required: true, message: '请输入工号/学号', trigger: 'blur' },
    { min: 3, max: 30, message: '账号长度 3-30 位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度 6-20 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^\d{11}$/, message: '手机号必须是 11 位数字', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  roleCode: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const submit = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    await userStore.registerAction(form)
    ElMessage.success('注册成功，正在进入...')
    router.push('/')
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
.register-box {
  width: 460px;
  padding: 32px 40px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.2);
}
.login-title { font-size: 22px; font-weight: bold; text-align: center; }
.login-subtitle { color: #999; text-align: center; margin: 8px 0 20px; }
.login-tip { color: #999; text-align: center; font-size: 12px; margin-top: 8px; }
.login-tip a { color: #5f86ff; text-decoration: none; }
.login-tip a:hover { text-decoration: underline; }
</style>