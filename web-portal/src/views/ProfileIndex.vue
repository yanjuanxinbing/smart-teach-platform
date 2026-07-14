<template>
  <section class="prof">
    <header class="prof__head">
      <span class="eyebrow"><span class="eyebrow__rule"></span>Account</span>
      <h2 class="prof__title">我的<em>资料</em></h2>
      <p class="prof__lead">展示在个人空间与课程交互中的基本信息——可随时编辑以保持最新。</p>
    </header>

    <article class="card">
      <div class="card__head">
        <h3 class="card__title">基础信息</h3>
        <el-button v-if="!editing" type="primary" plain class="card__edit" @click="editing = true">编辑</el-button>
        <div v-else class="card__actions">
          <el-button @click="cancel">取消</el-button>
          <el-button type="primary" :loading="saving" @click="save">保存</el-button>
        </div>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" :disabled="!editing" class="card__form">
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="头像">
              <div class="avatar-row">
                <el-avatar :size="56" :src="form.avatar">{{ (form.realName || form.username || 'U').charAt(0) }}</el-avatar>
                <el-button size="small" plain :disabled="!editing">更换头像</el-button>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="12"><el-form-item label="角色">
            <el-tag>{{ userStore.roleLabel }}</el-tag>
          </el-form-item></el-col>

          <el-col :span="12"><el-form-item label="用户名" prop="username">
            <el-input v-model="form.username" disabled />
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="昵称 / 真实姓名" prop="realName">
            <el-input v-model="form.realName" placeholder="请输入" />
          </el-form-item></el-col>

          <el-col :span="12"><el-form-item label="邮箱" prop="email">
            <el-input v-model="form.email" placeholder="name@example.com" />
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="手机号" prop="phone">
            <el-input v-model="form.phone" placeholder="11 位手机号" maxlength="11" />
          </el-form-item></el-col>

          <el-col :span="12"><el-form-item label="所属部门 / 学院">
            <el-input v-model="form.deptName" disabled />
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="个人简介" prop="bio">
            <el-input v-model="form.bio" type="textarea" :rows="3" placeholder="一句话介绍自己..." maxlength="160" show-word-limit />
          </el-form-item></el-col>
        </el-row>
      </el-form>
    </article>

    <article class="card">
      <div class="card__head">
        <h3 class="card__title">登录与安全</h3>
        <el-button plain class="card__edit" @click="$router.push('/profile/security')">前往设置</el-button>
      </div>
      <ul class="quick">
        <li><span>最近登录时间</span><b>{{ userStore.userInfo?.lastLoginTime || '—' }}</b></li>
        <li><span>最近登录地址</span><b>{{ userStore.userInfo?.lastLoginIp || '—' }}</b></li>
        <li><span>当前会话数</span><b>1</b></li>
      </ul>
    </article>
  </section>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { updateMyProfile } from '@/api/profile'

const userStore = useUserStore()
const formRef = ref()
const editing = ref(false)
const saving = ref(false)
const form = reactive({
  username: '', realName: '', email: '', phone: '', bio: '', avatar: '', deptName: ''
})
const rules = {
  realName: [{ required: true, message: '请输入昵称/真实姓名', trigger: 'blur' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
  phone: [{ pattern: /^1\d{10}$/, message: '请输入 11 位手机号', trigger: 'blur' }]
}

const snapshot = () => JSON.parse(JSON.stringify(form))
const restore = (s) => Object.assign(form, s)

let initial = snapshot()
const cancel = () => { restore(initial); editing.value = false }
const save = async () => {
  await formRef.value.validate()
  saving.value = true
  try {
    await updateMyProfile({ realName: form.realName, email: form.email, phone: form.phone, bio: form.bio })
    ElMessage.success('资料已保存')
    initial = snapshot()
    editing.value = false
    await userStore.fetchUserInfo()
  } finally { saving.value = false }
}

const fill = () => {
  const u = userStore.userInfo || {}
  Object.assign(form, {
    username: u.username || u.userName || '',
    realName: u.realName || '',
    email: u.email || '',
    phone: u.phone || '',
    bio: u.bio || '',
    avatar: u.avatar || '',
    deptName: u.deptName || u.dept?.name || '—'
  })
  initial = snapshot()
}

onMounted(async () => {
  try { await userStore.fetchUserInfo() } catch (e) {}
  fill()
})
</script>

<style scoped>
.prof > .card { margin-top: var(--s-5); }
.prof__head { margin-bottom: var(--s-6); }
.eyebrow { display: inline-flex; align-items: center; gap: 8px; font-family: var(--font-mono); font-size: 11px; letter-spacing: 0.18em; text-transform: uppercase; color: var(--mute); }
.eyebrow__rule { display: inline-block; width: 26px; height: 1px; background: var(--accent); }
.prof__title { font-family: var(--font-display); font-size: clamp(26px, 3vw, 36px); font-weight: 600; color: var(--ink); margin: 6px 0 6px; letter-spacing: -0.018em; }
.prof__title em { font-family: var(--font-serif); font-style: italic; font-weight: 500; color: var(--accent); }
.prof__lead { color: var(--ink-soft); margin: 0; font-size: 14px; }

.card { background: var(--surface); border: 1px solid var(--line); padding: clamp(20px, 2.6vw, 32px); }
.card__head { display: flex; justify-content: space-between; align-items: center; padding-bottom: var(--s-5); margin-bottom: var(--s-5); border-bottom: 1px solid var(--line-soft); }
.card__title { font-family: var(--font-display); font-size: 18px; font-weight: 600; color: var(--ink); margin: 0; letter-spacing: -0.01em; }
.card__actions { display: flex; gap: 8px; }
.card__edit, .card__actions :deep(.el-button) { border-radius: 0; }

.card__form :deep(.el-form-item__label) { font-weight: 500; color: var(--ink); font-size: 13px; }
.card__form :deep(.el-input__wrapper), .card__form :deep(.el-textarea__inner) { border-radius: 0; }

.avatar-row { display: flex; align-items: center; gap: 12px; }

.quick { list-style: none; padding: 0; margin: 0; }
.quick li { display: flex; justify-content: space-between; padding: 12px 0; border-bottom: 1px solid var(--line-soft); font-family: var(--font-mono); font-size: 13px; }
.quick li:last-child { border-bottom: none; }
.quick span { color: var(--mute); letter-spacing: 0.08em; text-transform: uppercase; font-size: 11px; }
.quick b { color: var(--ink); font-weight: 500; }
.quick b.warn { color: var(--warn); }
</style>
