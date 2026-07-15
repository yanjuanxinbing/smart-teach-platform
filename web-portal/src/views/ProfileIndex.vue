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
                <el-button size="small" plain :disabled="!editing" :loading="avatarUploading" @click="pickAvatar">更换头像</el-button>
                <el-button v-if="editing && form.avatar" size="small" link type="danger" @click="resetAvatar">还原</el-button>
                <input
                  ref="avatarInputRef"
                  type="file"
                  accept="image/png,image/jpeg,image/jpg,image/webp,image/gif"
                  style="display:none"
                  @change="onAvatarSelected"
                />
              </div>
              <p class="avatar-hint">支持 JPG / PNG / WebP / GIF,单张不超过 2MB</p>
            </el-form-item>
          </el-col>
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
            <el-input
              v-model="form.phone"
              :placeholder="form.phone ? '11 位手机号' : '待完善'"
              maxlength="11"
            />
          </el-form-item></el-col>

          <el-col :span="12"><el-form-item label="所属部门 / 学院">
            <el-input v-model="form.deptName" disabled placeholder="待完善" />
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="个人简介" prop="bio">
            <el-input v-model="form.bio" type="textarea" :rows="3" placeholder="一句话介绍自己..." maxlength="160" show-word-limit />
          </el-form-item></el-col>
        </el-row>
      </el-form>
    </article>

    <!-- ============= 个人简介（独立卡片 + 专属切换） ============= -->
    <article class="card">
      <div class="card__head">
        <h3 class="card__title">个人简介</h3>
        <!--
          显示态:右上角一个"编辑简介"按钮,点击进入编辑态;
          编辑态:右侧一组"取消 / 保存"按钮,实时反馈保存结果。
          这是用户要求的"el-input / 富文本 的切换逻辑"。
        -->
        <el-button v-if="!bioEditing" type="primary" plain class="card__edit" @click="startBioEdit">编辑简介</el-button>
        <div v-else class="card__actions">
          <el-button @click="cancelBioEdit">取消</el-button>
          <el-button type="primary" :loading="bioSaving" @click="saveBio">保存</el-button>
        </div>
      </div>

      <!-- 显示态 -->
      <div v-if="!bioEditing" class="bio bio--display">
        <!--
          bio 现在可能保存为富文本 HTML；用 v-html 渲染。
          注意：后端应负责 XSS 过滤（白名单或转义），前端 v-html 仅做展示。
          若后端存储纯文本，仍会按文本正常显示（HTML 解析为纯文本节点）。
        -->
        <div v-if="bio" class="bio__text bio__text--html" v-html="bio"></div>
        <p v-else class="bio__text bio__text--empty">还没有填写个人简介,点击右上角"编辑简介"补充一下吧~</p>
        <!--
          "查看文件"按钮 —— 当前项目内路由跳转到 /profile/document。
          与 ProfileLayout 侧栏的"查看文档"共用同一路由;
          这里保留是让简介区也能独立触发(对应"个人文档/履历附件"场景)。
          之前是 window.open 跨域跳 8081,已纠正为内部路由跳转。
        -->
        <div class="bio__foot">
          <el-button size="small" plain @click="goDocuments">查看文件</el-button>
        </div>
      </div>

      <!-- 编辑态 —— 富文本编辑器（@wangeditor/editor-for-vue 5.x） -->
      <div v-else class="bio bio--edit">
        <RichEditor
          v-model="bioDraft"
          height="220px"
          placeholder="介绍自己,比如研究方向、兴趣爱好、近期目标..."
          :exclude-keys="['uploadImage', 'uploadVideo', 'insertVideo', 'insertTable', 'codeBlock', 'todo']"
          :readonly="bioSaving"
        />
        <p class="bio__hint">简介会在课程页、消息签名、个人空间公开区域展示,建议 1-3 行。</p>
      </div>
    </article>

    <!-- ============= 登录与安全 ============= -->
    <article class="card">
      <div class="card__head">
        <h3 class="card__title">登录与安全</h3>
        <el-button plain class="card__edit" @click="$router.push('/profile/security')">前往设置</el-button>
      </div>
      <ul class="quick">
        <li><span>当前会话数</span><b>1</b></li>
      </ul>
    </article>
  </section>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { updateMyProfile, uploadAvatar } from '@/api/profile'
import RichEditor from '@/components/RichEditor.vue'

const router = useRouter()
const userStore = useUserStore()

// ============================================================
// 跳转"我的文档"页 —— 当前项目内路由跳转,不跨域
// ============================================================
// 与 ProfileLayout 侧栏的"查看文档"按钮共用 /profile/document;
// 之前是 window.open 跨域跳 8081,已纠正为内部路由:
//   8082 是内容展示端,8081 是内容生产端,前台不直接打开后台;
//   文档内容通过 GET /api/document/me 拉到 8082 在 DocumentView 渲染。
const goDocuments = () => {
  router.push('/profile/document')
}

// ============================================================
// 基础信息表单
// ============================================================
const formRef = ref()
const editing = ref(false)
const saving = ref(false)
const isStudent = computed(() => userStore.roleCode === 'STUDENT')
const form = reactive({
  username: '', realName: '', email: '', phone: '', bio: '', avatar: '', deptName: '', className: ''
})
const rules = {
  realName: [{ required: true, message: '请输入昵称/真实姓名', trigger: 'blur' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
  phone: [{ pattern: /^1\d{10}$/, message: '请输入 11 位手机号', trigger: 'blur' }]
}

// ============================================================
// 头像 —— 选文件 → 立即上传,后端失败时回退到本地 dataURL 预览
// ============================================================
const avatarInputRef = ref()
const avatarUploading = ref(false)
// 缓存上一次「干净」的 avatar URL,方便 cancel 时还原
let avatarOriginal = ''

const pickAvatar = () => {
  if (!editing.value) return
  avatarInputRef.value?.click()
}

const resetAvatar = () => {
  // 仅在编辑态下可点击,把 avatar 还原到进入编辑态时的初值
  if (!editing.value) return
  form.avatar = avatarOriginal || ''
}

const readAsDataURL = (file) => new Promise((resolve, reject) => {
  const fr = new FileReader()
  fr.onload = () => resolve(fr.result)
  fr.onerror = reject
  fr.readAsDataURL(file)
})

const onAvatarSelected = async (e) => {
  const file = e.target?.files?.[0]
  // 每次选完重置 input.value,允许重复选同一张
  if (avatarInputRef.value) avatarInputRef.value.value = ''
  if (!file) return
  // 前端基本校验
  if (!/^image\//.test(file.type)) {
    ElMessage.warning('请选择图片文件')
    return
  }
  if (file.size > 2 * 1024 * 1024) {
    ElMessage.warning('头像大小不能超过 2MB')
    return
  }

  // 选完立刻本地预览(base64),保证 UI 不闪
  const localPreview = await readAsDataURL(file).catch(() => '')
  if (localPreview) form.avatar = localPreview

  avatarUploading.value = true
  try {
    // request.js 响应拦截器已经把 { code, data } 解包,这里拿到的是内层 payload
    const payload = await uploadAvatar(file)
    const remoteUrl = payload?.url || payload?.avatar
    if (remoteUrl) {
      form.avatar = remoteUrl
      userStore.userInfo = { ...(userStore.userInfo || {}), avatar: remoteUrl }
      ElMessage.success('头像已更新')
    } else {
      // 接口返回了 200 但 body 里没 url —— 视为部分成功,保留本地预览
      console.warn('功能开发中:头像上传接口未返回 url 字段,保留本地预览')
      ElMessage.warning('头像已选择本地预览,服务器尚未保存')
    }
  } catch (err) {
    // 接口不存在/失败 —— 保留本地 base64 预览,不报错不白屏
    console.warn('功能开发中:头像上传接口未就绪,使用本地预览', err?.message)
    ElMessage.warning('头像上传功能开发中,已使用本地预览')
  } finally {
    avatarUploading.value = false
  }
}

const snapshot = () => JSON.parse(JSON.stringify(form))
const restore = (s) => Object.assign(form, s)

let initial = snapshot()
const cancel = () => {
  restore(initial)
  form.avatar = avatarOriginal
  editing.value = false
}
const save = async () => {
  await formRef.value.validate()
  saving.value = true
  try {
    // 不携带 username(后端只读) 与 deptName(管理员专属字段)
    await updateMyProfile({ realName: form.realName, email: form.email, phone: form.phone })
    ElMessage.success('基础资料已保存')
    initial = snapshot()
    editing.value = false
    await userStore.fetchUserInfo()
  } catch (e) {
    console.warn('功能开发中:基础资料保存接口未就绪', e?.message)
    ElMessage.warning('基础资料保存功能开发中,请稍后重试')
  } finally { saving.value = false }
}

// ============================================================
// 个人简介 —— 独立编辑状态(不与基础信息表单的 editing 耦合)
// ============================================================
const bio = ref('')           // 显示态用的源数据(只读,跟 store 同步)
const bioDraft = ref('')      // 编辑态用的可写副本
const bioEditing = ref(false)
const bioSaving = ref(false)

const startBioEdit = async () => {
  bioDraft.value = bio.value || ''
  bioEditing.value = true
  // RichEditor 会在 onMounted 后自动获得焦点，无需手动 focus textarea
  await nextTick()
}
const cancelBioEdit = () => {
  bioDraft.value = ''
  bioEditing.value = false
}
const saveBio = async () => {
  // 富文本 bio 是 HTML 字符串；校验时剥离标签再判断字符长度
  const html = bioDraft.value || ''
  const textOnly = html.replace(/<[^>]*>/g, '').trim()
  if (textOnly.length > 600) {
    ElMessage.warning('简介不能超过 600 字（富文本按纯文本计算）')
    return
  }
  if (!textOnly) {
    ElMessage.warning('简介不能为空')
    return
  }
  bioSaving.value = true
  try {
    await updateMyProfile({ bio: html })
    ElMessage.success('简介已保存')
    bio.value = html
    // 同步 store.userInfo.bio,刷新时不会丢
    await userStore.fetchUserInfo()
    bioEditing.value = false
  } catch (e) {
    console.warn('功能开发中:简介保存接口未就绪', e?.message)
    ElMessage.warning('简介保存功能开发中,请稍后重试')
  } finally {
    bioSaving.value = false
  }
}

// ============================================================
// 数据装载 —— 把 store.userInfo 映射到本地 form / bio
// ============================================================
const fill = () => {
  const u = userStore.userInfo || {}
  // 字段缺失时统一落到空串,UI 端通过 placeholder / '—' 展示待完善
  Object.assign(form, {
    username:  u.username || u.userName || '',
    realName:  u.realName || '',
    email:     u.email    || '',
    phone:     u.phone    || '',
    bio:       u.bio      || '',
    avatar:    u.avatar   || '',
    deptName:  u.deptName || u.dept?.name || '',
    className: u.className || ''
  })
  bio.value = u.bio || ''
  initial = snapshot()
  avatarOriginal = form.avatar
}

// 进入编辑态时锁定原 avatar,方便 cancel 还原
watch(editing, (val) => {
  if (val) avatarOriginal = form.avatar
})

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
.avatar-hint { margin: 6px 0 0; font-family: var(--font-mono); font-size: 11px; color: var(--mute); letter-spacing: 0.04em; }

.quick { list-style: none; padding: 0; margin: 0; }
.quick li { display: flex; justify-content: space-between; padding: 12px 0; border-bottom: 1px solid var(--line-soft); font-family: var(--font-mono); font-size: 13px; }
.quick li:last-child { border-bottom: none; }
.quick span { color: var(--mute); letter-spacing: 0.08em; text-transform: uppercase; font-size: 11px; }
.quick b { color: var(--ink); font-weight: 500; }
.quick b.warn { color: var(--warn); }

/* 富文本简介显示态 —— 复用展示样式并允许内嵌 a/img/code 等元素 */
.bio__text--html { white-space: normal; line-height: 1.8; font-size: 14px; color: var(--ink); }
.bio__text--html :deep(p) { margin: 0 0 8px; }
.bio__text--html :deep(strong) { font-weight: 600; color: var(--ink); }
.bio__text--html :deep(em) { font-style: italic; color: var(--accent); }
.bio__text--html :deep(a) { color: var(--accent); text-decoration: underline; }
.bio__text--html :deep(ul), .bio__text--html :deep(ol) { padding-left: 20px; margin: 6px 0; }
.bio__text--html :deep(code) { background: var(--surface-soft); padding: 1px 6px; font-family: var(--font-mono); font-size: 12px; border: 1px solid var(--line-soft); }

/* 富文本编辑态的 toolbar 容器 —— 边角对齐项目 button 风格 */
.bio--edit :deep(.rich-editor) { border-radius: 0; }
.bio--edit :deep(.w-e-toolbar) { border-radius: 0; }
</style>
