<template>
  <div class="pi">
    <header class="pi__head">
      <span class="pi__crumb">个人中心 · 我的资料</span>
      <h2 class="pi__title">基础信息</h2>
      <p class="pi__lead">展示在协作、评论、消息中的个人身份——可随时更新。</p>
    </header>

    <el-card shadow="never" class="pi__card">
      <template #header>
        <div class="card-head">
          <span>个人资料</span>
          <div>
            <el-button v-if="!editing" type="primary" plain @click="editing = true">编辑</el-button>
            <template v-else>
              <el-button @click="cancel">取消</el-button>
              <el-button type="primary" :loading="saving" @click="save">保存</el-button>
            </template>
          </div>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" :disabled="!editing" label-width="100px">
        <el-row :gutter="24">
          <el-col :span="8">
            <el-form-item label="头像">
              <div class="avatar">
                <el-avatar :size="64" :src="form.avatar">{{ (form.realName || form.username || 'U').charAt(0) }}</el-avatar>
                <el-upload :show-file-list="false" :auto-upload="false" :on-change="onPick" class="avatar__pick">
                  <el-button size="small" plain :disabled="!editing">更换</el-button>
                </el-upload>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="8"><el-form-item label="账号">{{ form.username }}</el-form-item></el-col>

          <el-col :span="12"><el-form-item label="真实姓名" prop="realName"><el-input v-model="form.realName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="昵称" prop="nickname"><el-input v-model="form.nickname" /></el-form-item></el-col>

          <el-col :span="12"><el-form-item label="邮箱" prop="email"><el-input v-model="form.email" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="手机" prop="phone"><el-input v-model="form.phone" maxlength="11" /></el-form-item></el-col>

          <el-col :span="12"><el-form-item :label="isStudent ? '班级' : '部门'">{{ (isStudent ? form.className : form.deptName) || '—' }}</el-form-item></el-col>

          <el-col :span="24"><el-form-item label="个人简介">
            <el-input v-model="form.bio" type="textarea" :rows="3" maxlength="160" show-word-limit placeholder="一句话介绍自己..." />
          </el-form-item></el-col>
        </el-row>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { updateMyProfile } from '@/api/profile'

const userStore = useUserStore()
const formRef = ref()
const editing = ref(false)
const saving = ref(false)
// roleNames 里存的是角色编码（如 ROLE_STUDENT），学生显示"班级"而非"部门"
const isStudent = computed(() => (userStore.roles || []).includes('ROLE_STUDENT'))
const form = reactive({
  username: '', realName: '', nickname: '', email: '', phone: '', bio: '', avatar: '', deptName: '', className: ''
})
const rules = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
  phone: [{ pattern: /^1\d{10}$/, message: '请输入 11 位手机号', trigger: 'blur' }]
}

let initial = JSON.parse(JSON.stringify(form))
const cancel = () => { Object.assign(form, initial); editing.value = false }
const save = async () => {
  await formRef.value.validate()
  saving.value = true
  try {
    await updateMyProfile({ realName: form.realName, nickname: form.nickname, email: form.email, phone: form.phone, bio: form.bio })
    ElMessage.success('资料已保存')
    initial = JSON.parse(JSON.stringify(form))
    editing.value = false
    await userStore.fetchUserInfo()
  } finally { saving.value = false }
}

const onPick = (file) => {
  const reader = new FileReader()
  reader.onload = () => { form.avatar = reader.result }
  reader.readAsDataURL(file.raw)
}

const fill = () => {
  const u = userStore.userInfo || {}
  Object.assign(form, {
    username: u.username || u.userName || '',
    realName: u.realName || '', nickname: u.nickname || '',
    email: u.email || '', phone: u.phone || '',
    bio: u.bio || '', avatar: u.avatar || '',
    deptName: u.deptName || u.dept?.name || '',
    className: u.className || ''
  })
  initial = JSON.parse(JSON.stringify(form))
}

onMounted(async () => {
  try { await userStore.fetchUserInfo() } catch (e) {}
  fill()
})
</script>

<style scoped>
.pi__head { margin-bottom: var(--s-4); }
.pi__crumb { font-family: var(--font-mono); font-size: 11px; letter-spacing: 0.18em; text-transform: uppercase; color: var(--mute); }
.pi__title { font-family: var(--font-display); font-size: 22px; font-weight: 600; color: var(--ink); margin: 6px 0 4px; letter-spacing: -0.012em; }
.pi__lead { color: var(--ink-soft); margin: 0; font-size: 13.5px; }

.pi__card :deep(.el-card__header) { padding: 14px 18px; }
.card-head { display: flex; justify-content: space-between; align-items: center; }

.avatar { display: flex; align-items: center; gap: 14px; }
</style>
