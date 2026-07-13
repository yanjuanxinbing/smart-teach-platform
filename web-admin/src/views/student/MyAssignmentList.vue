<template>
  <div class="app-container">
    <el-card>
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input v-model="query.keyword" placeholder="作业标题关键字" clearable style="width: 220px" @keyup.enter="load" />
          <el-select v-model="query.status" placeholder="作业状态" clearable style="width: 140px">
            <el-option label="已发布" :value="1" />
            <el-option label="已截止" :value="2" />
          </el-select>
          <el-button type="primary" @click="load">搜索</el-button>
          <el-button @click="reset">重置</el-button>
        </div>
      </div>

      <el-table :data="list" v-loading="loading" border>
        <el-table-column prop="title" label="作业标题" min-width="220" show-overflow-tooltip />
        <el-table-column prop="description" label="作业说明" min-width="220" show-overflow-tooltip />
        <el-table-column prop="deadline" label="截止时间" width="170">
          <template #default="{ row }">
            <span :style="{ color: isPastDeadline(row.deadline) && row.status === 1 ? '#f56c6c' : '' }">{{ row.deadline }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalScore" label="总分" width="80" />
        <el-table-column label="作业状态" width="100">
          <template #default="{ row }">
            <el-tag :type="['info','success','warning'][row.status]">{{ ['草稿','已发布','已截止'][row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="我的状态" width="120">
          <template #default="{ row }">
            <el-tag v-if="myStatusMap[row.id] === 0" type="info">已存草稿</el-tag>
            <el-tag v-else-if="myStatusMap[row.id] === 1" type="success">已提交</el-tag>
            <el-tag v-else-if="myStatusMap[row.id] === 2" type="primary">已批改</el-tag>
            <el-tag v-else type="info" effect="plain">未提交</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="分数" width="80">
          <template #default="{ row }">
            {{ myScoreMap[row.id] ?? '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button size="small" link v-if="userStore.hasAuthority('assignment:submit') && row.status !== 2" @click="openSubmit(row.id)">
              {{ myStatusMap[row.id] === 2 ? '查看' : (myStatusMap[row.id] != null ? '继续编辑' : '提交作业') }}
            </el-button>
            <el-button size="small" link v-else-if="myStatusMap[row.id] === 2" @click="openSubmit(row.id)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination v-model:page="query.pageNum" v-model:size="query.pageSize" :total="total" @change="load" />
    </el-card>

    <el-dialog v-model="submitVisible" :title="assignment ? assignment.title : '提交作业'" width="800px" top="5vh" @closed="onDialogClosed">
      <template v-if="assignment">
        <el-descriptions :column="2" border style="margin-bottom: 16px">
          <el-descriptions-item label="作业状态">
            <el-tag :type="['info','success','warning'][assignment.status]">{{ ['草稿','已发布','已截止'][assignment.status] }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="总分">{{ assignment.totalScore }}</el-descriptions-item>
          <el-descriptions-item label="截止时间">
            <span :style="{ color: dialogPastDeadline ? '#f56c6c' : '' }">{{ assignment.deadline }}</span>
            <el-tag v-if="dialogPastDeadline" type="warning" size="small" style="margin-left:8px">已过截止</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="关联章节内容">#{{ assignment.contentId }}</el-descriptions-item>
          <el-descriptions-item label="作业说明" :span="2">
            <div style="white-space:pre-wrap">{{ assignment.description || '（老师没有补充说明）' }}</div>
          </el-descriptions-item>
        </el-descriptions>

        <!-- 已批改：只读分数+评语 -->
        <el-alert
          v-if="latest && latest.status === 2"
          type="success"
          :closable="false"
          style="margin-bottom: 16px"
        >
          <template #title>
            <span>已批改：成绩 {{ latest.score }} / {{ assignment.totalScore }}</span>
          </template>
          <div style="margin-top: 8px">评语：{{ latest.comment || '（老师没有留下评语）' }}</div>
          <div style="margin-top: 4px; color: #999; font-size: 12px">
            批改人：{{ latest.graderName || '-' }} ｜ 批改时间：{{ latest.gradeTime || '-' }}
          </div>
        </el-alert>

        <!-- 已提交待批改：只读 -->
        <el-alert
          v-else-if="latest && latest.status === 1"
          type="info"
          :closable="false"
          show-icon
          style="margin-bottom: 16px"
        >
          <template #title>
            <span>已提交，等待老师批改（提交时间：{{ latest.submitTime }}）</span>
          </template>
          <div v-if="latest.isLate === 1" style="margin-top: 4px; color: #e6a23c">本次提交为迟交</div>
        </el-alert>

        <!-- 表单：草稿 / 还没提过 / 已批改后想再交 -->
        <el-form
          v-if="canEdit"
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="100px"
        >
          <el-form-item label="提交正文" prop="submitText">
            <el-input v-model="form.submitText" type="textarea" :rows="8" placeholder="写正文、可附文件一起交" />
          </el-form-item>
          <el-form-item label="附件">
            <el-upload :http-request="upload" :show-file-list="false" :before-upload="beforeUpload">
              <el-button>{{ form.fileUrl ? '更换附件' : '选择文件' }}</el-button>
              <span v-if="form.fileUrl" style="margin-left:8px">{{ form.originalName }}</span>
            </el-upload>
            <div v-if="form.fileUrl" style="margin-top:8px">
              <el-button size="small" link @click="clearAttachment">清除附件</el-button>
            </div>
          </el-form-item>

          <!-- 附件预览 -->
          <el-form-item v-if="form.fileUrl" label="附件预览">
            <iframe v-if="previewType === 'pdf'" :src="form.fileUrl" class="preview-frame" />
            <img v-else-if="previewType === 'image'" :src="form.fileUrl" class="preview-image" />
            <video v-else-if="previewType === 'video'" :src="form.fileUrl" controls class="preview-media" />
            <audio v-else-if="previewType === 'audio'" :src="form.fileUrl" controls class="preview-media" />
            <div v-else style="color:#909399; font-size:12px">该类型不能在线预览，下载/提交即可</div>
          </el-form-item>
        </el-form>
      </template>
      <el-empty v-else description="作业不存在或已被删除" />

      <template #footer v-if="assignment && canEdit">
        <el-button type="primary" :loading="submitting" v-if="userStore.hasAuthority('assignment:save')" @click="saveDraft">保存草稿</el-button>
        <el-button type="success" :loading="submitting" v-if="userStore.hasAuthority('assignment:submit')" @click="onSubmit">提交作业</el-button>
        <el-button type="danger" v-if="canDeleteDraft" @click="removeDraft">删除草稿</el-button>
        <el-button @click="submitVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.preview-frame { width: 100%; max-width: 600px; height: 400px; border: 1px solid #ebeef5; }
.preview-image { max-width: 600px; max-height: 400px; object-fit: contain; border: 1px solid #ebeef5; }
.preview-media { max-width: 600px; }
</style>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import Pagination from '@/components/Pagination.vue'
import { useUserStore } from '@/store/user'
import { assignPage, assignDetail, submissionLatest, submissionSaveDraft, submissionSubmit, submissionRemove } from '@/api/assignment'
import { uploadFile } from '@/api/resource'

const userStore = useUserStore()

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', status: null, excludeDraft: true })
const myStatusMap = reactive({})
const myScoreMap = reactive({})

const isPastDeadline = (deadline) => {
  if (!deadline) return false
  return new Date(deadline.replace(/-/g, '/')).getTime() < Date.now()
}

const load = async () => {
  loading.value = true
  try {
    const res = await assignPage(query)
    list.value = res.list
    total.value = res.total
    // 后台并发拉每个作业的 latest 提交，把我的状态/分数缓存
    await Promise.all(res.list.map(async row => {
      try {
        const sub = await submissionLatest(row.id)
        myStatusMap[row.id] = sub?.status ?? null
        myScoreMap[row.id] = sub?.score ?? null
      } catch (e) {
        myStatusMap[row.id] = null
        myScoreMap[row.id] = null
      }
    }))
  } finally { loading.value = false }
}

const reset = () => {
  query.keyword = ''
  query.status = null
  load()
}

// ===== 提交作业（对话框，整合自原“提交作业”页面）=====
const submitVisible = ref(false)
const assignment = ref(null)
const latest = ref(null)
const formRef = ref()
const submitting = ref(false)
const currentId = ref(null)

const form = reactive({
  id: null, assignmentId: null, submitText: '',
  fileUrl: '', originalName: '', fileSuffix: '', fileSize: null
})
const rules = {
  submitText: [{ required: false }]   // 允许仅文件提交；如要必填在这里调
}

const dialogPastDeadline = computed(() => isPastDeadline(assignment.value?.deadline))

const canEdit = computed(() => {
  if (!assignment.value) return false
  // 已截止 → 不能改
  if (assignment.value.status === 2) return false
  // 已批改 → 默认不能再改
  if (latest.value && latest.value.status === 2) return false
  return true
})
const canDeleteDraft = computed(() => userStore.hasAuthority('assignment:my:remove') && latest.value && latest.value.status === 0)

const previewType = computed(() => {
  const name = (form.originalName || '').toLowerCase()
  const ext = name.includes('.') ? name.split('.').pop() : ''
  if (ext === 'pdf') return 'pdf'
  if (['png','jpg','jpeg','gif','bmp','webp','svg'].includes(ext)) return 'image'
  if (['mp4','webm','ogg','mov','avi','mkv'].includes(ext)) return 'video'
  if (['mp3','wav','ogg','flac','aac','m4a'].includes(ext)) return 'audio'
  return 'other'
})

const loadSubmit = async () => {
  let a = null
  let sub = null
  try {
    a = await assignDetail(currentId.value)
  } catch (e) {
    ElMessage.warning('作业已被删除或不可访问')
    assignment.value = null
    return
  }
  try {
    sub = await submissionLatest(currentId.value)
  } catch (_) {
    sub = null
  }
  assignment.value = a
  latest.value = sub
  if (sub) {
    form.id = sub.id
    form.assignmentId = sub.assignmentId
    form.submitText = sub.submitText || ''
    form.fileUrl = sub.fileUrl || ''
    form.originalName = sub.originalName || ''
    form.fileSuffix = sub.fileSuffix || ''
    form.fileSize = sub.fileSize
  } else {
    form.id = null
    form.assignmentId = currentId.value
    form.submitText = ''
    form.fileUrl = ''
    form.originalName = ''
    form.fileSuffix = ''
    form.fileSize = null
  }
}

const openSubmit = async (id) => {
  currentId.value = id
  assignment.value = null
  latest.value = null
  submitVisible.value = true
  await loadSubmit()
}

// 关闭对话框后刷新该作业在列表里的状态/分数
const onDialogClosed = async () => {
  if (!currentId.value) return
  try {
    const sub = await submissionLatest(currentId.value)
    myStatusMap[currentId.value] = sub?.status ?? null
    myScoreMap[currentId.value] = sub?.score ?? null
  } catch (_) {
    myStatusMap[currentId.value] = null
    myScoreMap[currentId.value] = null
  }
  currentId.value = null
}

const buildDto = () => ({
  id: form.id,
  assignmentId: currentId.value,
  submitText: form.submitText,
  fileUrl: form.fileUrl || null,
  originalName: form.originalName || null,
  fileSuffix: form.fileSuffix || null,
  fileSize: form.fileSize
})

const beforeUpload = (file) => {
  if (file.size > 50 * 1024 * 1024) {
    ElMessage.error('文件大小不能超过50MB')
    return false
  }
}

const upload = async (option) => {
  try {
    const res = await uploadFile(option.file)
    form.fileUrl = res.fileUrl
    form.originalName = res.originalName
    form.fileSuffix = res.fileSuffix
    form.fileSize = res.fileSize
    ElMessage.success('上传成功')
  } catch (e) {
    ElMessage.error('上传失败：' + (e.message || '请稍后重试'))
  }
}

const clearAttachment = () => {
  form.fileUrl = ''
  form.originalName = ''
  form.fileSuffix = ''
  form.fileSize = null
}

const saveDraft = async () => {
  submitting.value = true
  try {
    form.id = await submissionSaveDraft(buildDto())
    ElMessage.success('草稿已保存')
    await loadSubmit()
  } finally { submitting.value = false }
}

const onSubmit = async () => {
  await ElMessageBox.confirm('确认提交本次作业吗？提交后无法再修改，但可以重新提交一个新版本。', '提示', { type: 'warning' })
  submitting.value = true
  try {
    await submissionSubmit(buildDto())
    ElMessage.success('提交成功')
    try { await loadSubmit() } catch (_) { /* 静默：提交本身已成功 */ }
  } finally { submitting.value = false }
}

const removeDraft = async () => {
  await ElMessageBox.confirm('确定删除草稿？', '提示', { type: 'warning' })
  await submissionRemove([latest.value.id])
  ElMessage.success('草稿已删除')
  await loadSubmit()
}

onMounted(load)
</script>
