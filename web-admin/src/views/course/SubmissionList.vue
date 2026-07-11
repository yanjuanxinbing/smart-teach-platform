<template>
  <div class="app-container">
    <el-card>
      <div class="toolbar">
        <div class="toolbar-left">
          <el-select v-model="query.assignmentId" placeholder="选择作业" clearable filterable style="width: 320px">
            <el-option v-for="a in assignmentOptions" :key="a.id" :label="a.title" :value="a.id" />
          </el-select>
          <el-input v-model="query.studentName" placeholder="学生姓名" clearable style="width: 160px" @keyup.enter="load" />
          <el-input v-model="query.className" placeholder="班级" clearable style="width: 160px" @keyup.enter="load" />
          <el-select v-model="query.status" placeholder="状态" clearable style="width: 120px">
            <el-option label="草稿" :value="0" />
            <el-option label="已提交" :value="1" />
            <el-option label="已批改" :value="2" />
          </el-select>
          <el-select v-model="query.isLate" placeholder="是否迟交" clearable style="width: 120px">
            <el-option label="否" :value="0" />
            <el-option label="是" :value="1" />
          </el-select>
          <el-button type="primary" @click="load">搜索</el-button>
        </div>
      </div>

      <el-table :data="list" v-loading="loading" border>
        <el-table-column prop="assignmentTitle" label="作业标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="studentName" label="学生" width="100" />
        <el-table-column prop="className" label="班级" width="120" />
        <el-table-column prop="submitTime" label="提交时间" width="170" />
        <el-table-column label="是否迟交" width="90">
          <template #default="{ row }">
            <el-tag v-if="row.isLate === 1" type="warning" size="small">迟交</el-tag>
            <el-tag v-else type="info" size="small">按时</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="成绩" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="['info','success','primary'][row.status] || 'info'">{{ ['草稿','已提交','已批改'][row.status] || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="附件" width="100">
          <template #default="{ row }">
            <el-button size="small" link v-if="row.fileUrl" @click="openAttachment(row)">查看附件</el-button>
            <span v-else style="color:#bbb">无</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" link v-if="userStore.hasAuthority('assignment:grade') && row.status === 1" @click="grade(row)">批改</el-button>
            <el-button size="small" link v-if="userStore.hasAuthority('assignment:grade') && row.status === 2" @click="viewGrade(row)">查看评分</el-button>
            <el-button size="small" link type="danger" v-if="userStore.hasAuthority('assignment:remove')" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination v-model:page="query.pageNum" v-model:size="query.pageSize" :total="total" @change="load" />
    </el-card>

    <!-- 批改弹窗 -->
    <el-dialog v-model="gradeDialog" :title="`批改 - ${currentRow?.studentName || ''}`" width="500px">
      <el-form :model="gradeForm" :rules="gradeRules" ref="gradeFormRef" label-width="80px">
        <el-form-item label="作业标题">
          <span>{{ currentRow?.assignmentTitle }}</span>
        </el-form-item>
        <el-form-item label="提交正文">
          <el-input :model-value="currentRow?.submitText" type="textarea" :rows="4" readonly />
        </el-form-item>
        <el-form-item label="成绩" prop="score">
          <el-input-number v-model="gradeForm.score" :min="0" :max="100" :precision="2" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="评语">
          <el-input v-model="gradeForm.comment" type="textarea" :rows="3" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="gradeDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitGrade">提交</el-button>
      </template>
    </el-dialog>

    <!-- 附件预览弹窗 -->
    <el-dialog v-model="attachmentDialog" :title="`附件 - ${currentRow?.originalName || ''}`" width="80%" top="5vh" destroy-on-close>
      <div class="preview-box">
        <iframe v-if="previewType === 'pdf'" :src="currentRow?.fileUrl" class="preview-frame" />
        <img v-else-if="previewType === 'image'" :src="currentRow?.fileUrl" class="preview-image" />
        <video v-else-if="previewType === 'video'" :src="currentRow?.fileUrl" controls class="preview-media" />
        <audio v-else-if="previewType === 'audio'" :src="currentRow?.fileUrl" controls class="preview-media" />
        <div v-else class="preview-unsupported">
          <p>该文件类型暂不支持在线预览，可点击下方按钮下载</p>
          <el-button type="primary" @click="downloadAttachment">下载附件</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.preview-box { min-height: 400px; max-height: 70vh; overflow: auto; display: flex; justify-content: center; align-items: center; }
.preview-frame { width: 100%; height: 70vh; border: 0; }
.preview-image { max-width: 100%; max-height: 70vh; object-fit: contain; }
.preview-media { max-width: 100%; }
.preview-unsupported { text-align: center; color: #909399; padding: 32px; }
</style>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRoute } from 'vue-router'
import Pagination from '@/components/Pagination.vue'
import { useUserStore } from '@/store/user'
import { submissionPage, submissionGrade, submissionRemove, assignPage } from '@/api/assignment'

const userStore = useUserStore()
const route = useRoute()

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ pageNum: 1, pageSize: 10, assignmentId: null, studentName: '', className: '', status: null, isLate: null })
const assignmentOptions = ref([])

// 批改弹窗
const gradeDialog = ref(false)
const gradeFormRef = ref()
const gradeForm = reactive({ score: null, comment: '' })
const submitting = ref(false)
const currentRow = ref(null)
const gradeRules = {
  score: [{ required: true, message: '请输入成绩', trigger: 'blur' }]
}

// 附件预览
const attachmentDialog = ref(false)
const previewType = ref('')

const detectPreviewType = (row) => {
  const name = (row?.originalName || '').toLowerCase()
  const ext = name.includes('.') ? name.split('.').pop() : ''
  if (ext === 'pdf') return 'pdf'
  if (['png','jpg','jpeg','gif','bmp','webp','svg'].includes(ext)) return 'image'
  if (['mp4','webm','ogg','mov','avi','mkv'].includes(ext)) return 'video'
  if (['mp3','wav','ogg','flac','aac','m4a'].includes(ext)) return 'audio'
  return 'other'
}

const load = async () => {
  loading.value = true
  try {
    const res = await submissionPage(query)
    list.value = res.list
    total.value = res.total
  } finally { loading.value = false }
}

const grade = (row) => {
  currentRow.value = row
  gradeForm.score = null
  gradeForm.comment = ''
  gradeDialog.value = true
}

const submitGrade = async () => {
  await gradeFormRef.value.validate()
  submitting.value = true
  try {
    await submissionGrade(currentRow.value.id, { score: gradeForm.score, comment: gradeForm.comment })
    ElMessage.success('批改成功')
    gradeDialog.value = false
    load()
  } catch (e) {
    // 静默：submissionGrade 走 silentError，弹窗里直接给老师一个友好提示并刷新列表
    ElMessage.warning('该提交记录已被删除或不可批改，列表已刷新')
    gradeDialog.value = false
    load()
  } finally { submitting.value = false }
}

const viewGrade = async (row) => {
  await ElMessageBox.alert(
    `成绩：${row.score ?? '-'}\n\n评语：${row.comment || '（无评语）'}\n\n批改人：${row.graderName || '-'}\n批改时间：${row.gradeTime || '-'}`,
    '查看评分',
    { confirmButtonText: '关闭' }
  )
}

const remove = async (row) => {
  await ElMessageBox.confirm(`确定删除该提交记录？`, '提示', { type: 'warning' })
  await submissionRemove([row.id])
  ElMessage.success('删除成功')
  load()
}

const openAttachment = (row) => {
  currentRow.value = row
  previewType.value = detectPreviewType(row)
  attachmentDialog.value = true
}

const downloadAttachment = () => {
  if (!currentRow.value?.fileUrl) return
  // 通过 a[download] 触发浏览器下载（静态映射已经走通）
  const a = document.createElement('a')
  a.href = currentRow.value.fileUrl
  a.download = currentRow.value.originalName || 'download'
  a.style.display = 'none'
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
}

onMounted(async () => {
  // 支持从"作业列表"页传 assignmentId 过来
  if (route.query.assignmentId) {
    query.assignmentId = String(route.query.assignmentId);
  }
  // 顶部作业下拉一次性拉完（数据规模小）
  const res = await assignPage({ pageNum: 1, pageSize: 1000, status: null })
  assignmentOptions.value = res.list
  load()
})
</script>
