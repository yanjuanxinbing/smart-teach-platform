<template>
  <div class="page asubmit">
    <header class="asubmit__head">
      <div class="head">
        <router-link to="/my/assignments" class="head__back">
          <el-icon :size="13"><Back /></el-icon>
          <span>返回我的作业</span>
        </router-link>
        <h1 class="head__title">{{ assignment?.title || '提交作业' }}</h1>
        <p v-if="assignment" class="head__meta">
          <span class="meta__item">课程：{{ assignment.courseName || '—' }}</span>
          <span class="meta__item">截止：{{ fmtDate(assignment.deadline) }}</span>
          <span :class="['meta__item', isOverdue ? 'meta__item--danger' : '']">
            {{ isOverdue ? '已超过截止时间' : `剩余 ${daysLeft} 天` }}
          </span>
        </p>
      </div>
    </header>

    <section class="asubmit__body">
      <div v-if="loading" class="loading-tip">作业加载中…</div>
      <div v-else-if="loadError" class="empty">
        <el-icon :size="32"><Warning /></el-icon>
        <p>作业信息暂时不可用</p>
        <el-button size="small" plain @click="load">重新加载</el-button>
      </div>
      <el-form v-else ref="formRef" :model="form" class="form" label-position="top">
        <div class="form__block">
          <h2 class="form__title">作业描述</h2>
          <p class="form__desc">{{ assignment?.description || '（后端 PortalMyAssignmentVO 暂未返回 description 字段，当前为占位说明）' }}</p>
        </div>

        <el-form-item label="提交内容" prop="submitText">
          <!--
            富文本编辑器 —— 走 @wangeditor/editor-for-vue (封装的 RichEditor.vue 复用组件)
            form.submitText 保存为 HTML 字符串（wangeditor 默认输出格式）。
            后端期望字段：
              - assignment_submission.submit_text  (MEDIUMTEXT) 接收 HTML
              - 等同 submitText，payload { text } 已带此字段
          -->
          <RichEditor
            v-model="form.submitText"
            height="360px"
            placeholder="请输入作业正文（支持富文本 / HTML，可粘贴 Word 内容）"
            :disabled="submitting || savingDraft"
          />
          <!--
            TODO: [P1] 后端 /api/portal/my/assignments/{id}/submit 接口就绪后，
            当前 buildPayload() 把 form.submitText 当 HTML 发送给后端即可，
            后端需在 PortalMySubmissionVO.submitText 上落 MEDIUMTEXT。
            若后端仅支持纯文本，可在 onSubmit 前加一步 html2text 转换。
          -->
        </el-form-item>

        <el-form-item label="附件">
          <el-upload
            v-model:file-list="fileList"
            :auto-upload="false"
            :limit="1"
            :on-exceed="onExceed"
          >
            <el-button plain>
              <el-icon :size="14"><Upload /></el-icon><span style="margin-left:4px">选择文件</span>
            </el-button>
            <template #tip>
              <div class="form__tip">单个附件最大 100MB（与后台 sys_config.file.upload.max-mb 一致）</div>
            </template>
          </el-upload>
        </el-form-item>

        <div class="form__actions">
          <el-button @click="router.back()">取消</el-button>
          <el-button plain :loading="savingDraft" @click="onSaveDraft">保存草稿</el-button>
          <el-button type="primary" :loading="submitting" @click="onSubmit">提交作业</el-button>
        </div>
      </el-form>
    </section>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Back, Upload, Warning } from '@element-plus/icons-vue'
import { myAssignmentDetail, myLatestSubmission, saveAssignmentDraft, submitAssignment } from '@/api/my'
import { myAssignments } from '@/api/my'
import { useUserStore } from '@/store/user'
import RichEditor from '@/components/RichEditor.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const loadError = ref(false)
const assignment = ref(null)
const latest = ref(null)
const form = reactive({ submitText: '' })
const fileList = ref([])
const savingDraft = ref(false)
const submitting = ref(false)
const formRef = ref()

const fmtDate = (iso) => {
  if (!iso) return '—'
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return iso
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}
const isOverdue = computed(() => {
  if (!assignment.value?.deadline) return false
  return new Date(assignment.value.deadline).getTime() < Date.now()
})
const daysLeft = computed(() => {
  if (!assignment.value?.deadline) return 0
  const diff = new Date(assignment.value.deadline).getTime() - Date.now()
  return Math.max(0, Math.ceil(diff / 86400000))
})

const onExceed = () => {
  ElMessage.warning('仅支持上传 1 个附件，如需替换请先删除')
}

const load = async () => {
  loading.value = true
  loadError.value = false
  const id = String(route.params.id || '')
  try {
    const detail = await myAssignmentDetail(id)
    if (detail && detail.assignmentId) {
      assignment.value = detail
    } else {
      // 回退：从 myAssignments 列表过滤
      const res = await myAssignments({ current: 1, size: 200 })
      const hit = (res?.records || res?.list || []).find(a => String(a.assignmentId) === id)
      if (hit) assignment.value = hit
    }
    // 读最新一次提交，用于「继续编辑」场景（草稿 / 重新提交前展示旧内容）
    if (assignment.value) {
      try {
        const sub = await myLatestSubmission(id)
        if (sub && (sub.submitText || sub.fileUrl)) {
          latest.value = sub
          form.submitText = sub.submitText || ''
        }
      } catch (e) { /* 静默：可能是首次提交 */ }
    }
  } catch (e) {
    loadError.value = true
  } finally {
    loading.value = false
  }
}

const buildPayload = () => {
  // 后端若使用 multipart 接收，File 对象直接放；纯文本场景可走 JSON。
  // form.submitText 当前是富文本 HTML（来自 RichEditor 的 v-model）。
  // 后端接口期望字段：
  //   POST /api/portal/my/assignments/{id}/submit
  //     Body (multipart 或 JSON): { text: string(HTML), file?: Blob, draft?: boolean }
  //   对应落库字段: assignment_submission.submit_text (MEDIUMTEXT)
  if (fileList.value.length) {
    const fd = new FormData()
    fd.append('text', form.submitText || '')
    fd.append('file', fileList.value[0].raw)
    return fd
  }
  return { text: form.submitText || '' }
}

const onSaveDraft = async () => {
  savingDraft.value = true
  try {
    const payload = buildPayload()
    if (payload instanceof FormData) {
      // 多文件接口可能要求不同字段名，stub 阶段先传 text+file
      payload.append('draft', '1')
    } else {
      payload.draft = true
    }
    await saveAssignmentDraft(String(route.params.id), payload)
    ElMessage.success('草稿已保存（后端就绪后才会真正落库）')
  } catch (e) {
    ElMessage.error('保存草稿失败，请稍后再试')
  } finally {
    savingDraft.value = false
  }
}

const onSubmit = async () => {
  // 富文本提交，校验时先剥离 HTML 标签再判断空白
  const stripped = (form.submitText || '').replace(/<[^>]*>/g, '').trim()
  if (!stripped && !fileList.value.length) {
    ElMessage.warning('请填写提交内容或上传附件')
    return
  }
  try {
    await ElMessageBox.confirm('提交后无法再次修改，确定要提交作业吗？', '提交确认', {
      confirmButtonText: '确定提交',
      cancelButtonText: '再看看',
      type: 'warning'
    })
  } catch (e) { return }
  submitting.value = true
  try {
    await submitAssignment(String(route.params.id), buildPayload())
    ElMessage.success('作业已提交（后端就绪后才会真正落库）')
    router.replace('/my/assignments')
  } catch (e) {
    ElMessage.error('提交失败，请稍后再试')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  if (!userStore.isLogin) {
    router.replace({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  load()
})
</script>

<style scoped>
.asubmit { padding-bottom: var(--s-9); }
.asubmit__head { padding: var(--s-7) 0; border-bottom: 1px solid var(--line); background: linear-gradient(180deg, #fff 0%, #F4F7FF 100%); }
.head { display: flex; flex-direction: column; gap: 8px; }
.head__back { display: inline-flex; align-items: center; gap: 6px; font-family: var(--font-mono); font-size: 11px; letter-spacing: 0.18em; text-transform: uppercase; color: var(--mute); text-decoration: none; align-self: flex-start; }
.head__back:hover { color: var(--accent); }
.head__title { margin: 0; font-family: var(--font-display); font-size: 28px; font-weight: 600; color: var(--ink); letter-spacing: -0.012em; }
.head__meta { margin: 0; display: flex; gap: 18px; flex-wrap: wrap; font-family: var(--font-mono); font-size: 12px; color: var(--ink-soft); }
.meta__item--danger { color: var(--danger); }

.asubmit__body { padding: var(--s-7) 0 0; }
.form { max-width: 880px; display: flex; flex-direction: column; gap: 18px; }
.form__block { background: var(--surface-soft); border: 1px solid var(--line); padding: 18px 20px; }
.form__title { margin: 0 0 8px; font-family: var(--font-display); font-size: 14px; font-weight: 600; color: var(--ink); }
.form__desc { margin: 0; font-size: 13px; color: var(--ink-soft); line-height: 1.8; white-space: pre-wrap; }
.form__tip { font-family: var(--font-mono); font-size: 11px; color: var(--mute); margin-top: 4px; }
.form__actions { display: flex; gap: 12px; justify-content: flex-end; padding-top: 8px; border-top: 1px solid var(--line-soft); }

.loading-tip { color: var(--mute); font-size: 13px; letter-spacing: 0.06em; padding: 48px 0; text-align: center; }
.empty { padding: 48px 0; text-align: center; color: var(--mute); display: flex; flex-direction: column; align-items: center; gap: 14px; }

:deep(.el-textarea__inner) { border-radius: 0; font-family: var(--font-mono); font-size: 13px; line-height: 1.7; }

/* 富文本编辑器外层贴合表单样式 —— 边角与边框对齐项目规范 */
.form :deep(.rich-editor) { border-radius: 0; }
.form :deep(.w-e-toolbar) { border-radius: 0; }
</style>
