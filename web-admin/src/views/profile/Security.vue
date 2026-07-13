<template>
  <div class="ps">
    <header class="ps__head">
      <span class="ps__crumb">个人中心 · 安全</span>
      <h2 class="ps__title">账号安全</h2>
      <p class="ps__lead">修改密码，查看当前登录设备，保持账号安全。</p>
    </header>

    <el-row :gutter="16">
      <el-col :span="14">
        <el-card shadow="never">
          <template #header><span>修改密码</span></template>
          <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
            <el-form-item label="原密码" prop="oldPassword"><el-input v-model="form.oldPassword" type="password" show-password /></el-form-item>
            <el-form-item label="新密码" prop="newPassword"><el-input v-model="form.newPassword" type="password" show-password /></el-form-item>
            <el-form-item label="确认密码" prop="confirm">
              <el-input v-model="form.confirm" type="password" show-password />
            </el-form-item>
            <el-form-item><el-button type="primary" :loading="submitting" @click="submit">修改</el-button></el-form-item>
          </el-form>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="never">
          <template #header><span>两步验证</span></template>
          <p class="muted">开启两步验证后，登录时除密码外还需输入短信或邮箱验证码。</p>
          <el-switch v-model="tfa" />
          <p class="muted" style="margin-top: 12px">最近一次密码修改：{{ userStore.userInfo?.passwordUpdatedAt || '—' }}</p>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" style="margin-top: 16px">
      <template #header><span>登录设备</span></template>
      <el-table :data="devices" border>
        <el-table-column label="设备" prop="name" />
        <el-table-column label="系统" prop="os" width="120" />
        <el-table-column label="IP" prop="ip" width="140" />
        <el-table-column label="最后活跃" prop="last" width="160" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.current" type="success">当前</el-tag>
            <el-tag v-else type="info">已登录</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row, $index }">
            <el-button size="small" type="danger" :disabled="row.current" @click="kick($index)">退出</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { changeOwnPassword } from '@/api/profile'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const submitting = ref(false)
const tfa = ref(false)
const form = reactive({ oldPassword: '', newPassword: '', confirm: '' })
const rules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [{ required: true, min: 6, message: '至少 6 位', trigger: 'blur' }],
  confirm: [{ required: true, validator: (_, v, cb) => v === form.newPassword ? cb() : cb(new Error('两次密码不一致')) }]
}

const devices = ref([
  { name: 'Chrome / macOS', os: 'macOS 14', ip: '::1', last: '刚刚', current: true },
  { name: 'Safari / iOS', os: 'iOS 17', ip: '192.168.1.12', last: '昨天 21:34', current: false }
])

const submit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    await changeOwnPassword({ oldPassword: form.oldPassword, newPassword: form.newPassword })
    ElMessage.success('密码已修改，请重新登录')
    await userStore.logout()
    router.push('/login')
  } finally { submitting.value = false }
}

const kick = (i) => { devices.value.splice(i, 1); ElMessage.success('已退出该设备') }

onMounted(async () => { try { await userStore.fetchUserInfo() } catch (e) {} })
</script>

<style scoped>
.ps__head { margin-bottom: var(--s-4); }
.ps__crumb { font-family: var(--font-mono); font-size: 11px; letter-spacing: 0.18em; text-transform: uppercase; color: var(--mute); }
.ps__title { font-family: var(--font-display); font-size: 22px; font-weight: 600; color: var(--ink); margin: 6px 0 4px; }
.ps__lead { color: var(--ink-soft); margin: 0 0 var(--s-4); font-size: 13.5px; }
.muted { color: var(--mute); font-size: 13px; margin: 0; }
</style>
