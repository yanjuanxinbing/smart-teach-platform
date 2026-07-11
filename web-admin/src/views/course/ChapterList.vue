<template>
  <div class="app-container">
    <el-card>
      <div class="toolbar">
        <div class="toolbar-left">
          <el-select v-model="courseId" placeholder="选择课程" filterable @change="load" style="width: 280px">
            <el-option v-for="c in courseList" :key="c.id" :label="`${c.courseCode} ${c.courseName}`" :value="c.id" />
          </el-select>
          <el-button @click="load" :disabled="!courseId">刷新</el-button>
        </div>
        <el-button type="primary" :icon="Plus" :disabled="!courseId" @click="openForm()">新增章节</el-button>
      </div>

      <el-table :data="chapters" v-loading="loading" row-key="id" border default-expand-all
        :tree-props="{ children: 'children' }">
        <el-table-column prop="chapterTitle" label="章节标题" />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column prop="hours" label="学时" width="80" />
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button size="small" link @click="openForm(row)">编辑</el-button>
            <el-button size="small" link @click="openContent(row)">内容</el-button>
            <el-button size="small" link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑章节' : '新增章节'" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="所属课程">
          <el-input :value="courseId" disabled />
        </el-form-item>
        <el-form-item label="父章节" prop="parentId">
          <el-tree-select v-model="form.parentId" :data="chapterOptions" :props="{ value: 'id', label: 'chapterTitle' }"
            check-strictly clearable placeholder="顶级章节" style="width:100%" />
        </el-form-item>
        <el-form-item label="章节标题" prop="chapterTitle"><el-input v-model="form.chapterTitle" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
        <el-form-item label="学时"><el-input-number v-model="form.hours" :min="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="contentDialog" :title="`章节内容 - ${currentChapter?.chapterTitle || ''}`" width="700px">
      <div class="toolbar">
        <el-button type="primary" :icon="Plus" @click="openContentForm()">新增内容</el-button>
      </div>
      <el-table :data="contents" v-loading="contentLoading" border>
        <el-table-column prop="contentTitle" label="标题" />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag>{{ ['PPT','视频','文档','富文本'][row.contentType - 1] || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="70" />
        <el-table-column prop="hours" label="学时" width="70" />
        <el-table-column label="操作" width="260">
          <template #default="{ row }">
            <el-button size="small" link v-if="row.resourceUrl || row.contentType === 4" @click="previewContent(row)">预览</el-button>
            <el-button size="small" link v-if="canDownloadContent(row)" @click="downloadContent(row)">下载</el-button>
            <el-button size="small" link @click="openContentForm(row)">编辑</el-button>
            <el-button size="small" link type="danger" @click="removeContent(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 内容预览弹窗（按类型走：iframe/img/video/audio/富文本/其他） -->
    <el-dialog v-model="contentPreviewDialog" :title="contentPreviewRow?.contentTitle ? `预览 - ${contentPreviewRow.contentTitle}` : '预览'" width="80%" top="5vh" destroy-on-close>
      <div class="preview-box">
        <iframe v-if="contentPreviewType === 'pdf'" :src="contentPreviewRow?.resourceUrl" class="preview-frame" />
        <img v-else-if="contentPreviewType === 'image'" :src="contentPreviewRow?.resourceUrl" class="preview-image" />
        <video v-else-if="contentPreviewType === 'video'" :src="contentPreviewRow?.resourceUrl" controls class="preview-media" />
        <audio v-else-if="contentPreviewType === 'audio'" :src="contentPreviewRow?.resourceUrl" controls class="preview-media" />
        <pre v-else-if="contentPreviewType === 'txt'" class="preview-txt">{{ contentPreviewText }}</pre>
        <div v-else-if="contentPreviewType === 'richtext'" class="preview-richtext" v-html="contentPreviewRow?.richText" />
        <div v-else class="preview-unsupported">
          <el-icon :size="48" color="#909399"><Document /></el-icon>
          <p class="title">该类型暂不支持在线预览</p>
          <p class="hint">请尝试下载后用本地应用打开</p>
          <el-button type="primary" @click="downloadContent(contentPreviewRow)">下载文件</el-button>
        </div>
      </div>
    </el-dialog>

    <el-dialog v-model="contentFormDialog" :title="contentForm.id ? '编辑内容' : '新增内容'" width="560px">
      <el-form ref="contentFormRef" :model="contentForm" :rules="contentRules" label-width="100px">
        <el-form-item label="标题" prop="contentTitle"><el-input v-model="contentForm.contentTitle" /></el-form-item>
        <el-form-item label="类型" prop="contentType">
          <el-select v-model="contentForm.contentType" style="width:100%">
            <el-option label="课件PPT" :value="1" />
            <el-option label="视频" :value="2" />
            <el-option label="文档" :value="3" />
            <el-option label="富文本" :value="4" />
          </el-select>
        </el-form-item>
        <!--
          课件/视频/文档（type 1-3）：上传文件即可，URL 自动写入 contentForm.resourceUrl、不在 UI 上展示
          富文本（type=4）：不依赖 URL/上传，直接填富文本框
        -->
        <el-form-item v-if="contentForm.contentType !== 4" label="课件文件">
          <el-upload :http-request="uploadContent" :show-file-list="false" :before-upload="beforeContentUpload">
            <el-button>{{ uploadedFile ? '更换文件' : '上传文件' }}</el-button>
            <span v-if="uploadedFile" style="margin-left:8px">{{ uploadedFile.originalName }}</span>
          </el-upload>
        </el-form-item>
        <el-form-item v-if="contentForm.contentType === 4" label="富文本内容">
          <el-input v-model="contentForm.richText" type="textarea" :rows="6" />
        </el-form-item>
        <el-form-item label="排序"><el-input-number v-model="contentForm.sort" :min="0" /></el-form-item>
        <el-form-item label="学时"><el-input-number v-model="contentForm.hours" :min="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="contentFormDialog = false">取消</el-button>
        <el-button type="primary" @click="submitContent">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
/* 内容预览弹窗的样式，与 ResourceList.vue 保持一致 */
.preview-box { min-height: 400px; max-height: 70vh; overflow: auto; display: flex; justify-content: center; align-items: center; }
.preview-frame { width: 100%; height: 70vh; border: 0; }
.preview-image { max-width: 100%; max-height: 70vh; object-fit: contain; }
.preview-media { max-width: 100%; }
.preview-txt { width: 100%; max-height: 70vh; overflow: auto; padding: 16px; background: #fafafa; border-radius: 4px; font-family: Consolas, 'Courier New', monospace; font-size: 13px; line-height: 1.6; white-space: pre-wrap; word-break: break-all; margin: 0; }
.preview-richtext { width: 100%; max-height: 70vh; overflow: auto; padding: 24px 32px; background: #fff; border: 1px solid #ebeef5; border-radius: 4px; line-height: 1.8; font-size: 14px; }
.preview-unsupported { text-align: center; color: #909399; padding: 24px; }
.preview-unsupported .title { font-size: 16px; color: #606266; margin: 12px 0 4px; }
.preview-unsupported .hint { font-size: 13px; margin: 0 0 16px; color: #909399; }
</style>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Document } from '@element-plus/icons-vue'
import { chapterList, chapterAdd, chapterEdit, chapterRemove, myCourses } from '@/api/course'
import { contentByChapter, contentAdd, contentEdit, contentRemove } from '@/api/course'
import { uploadFile } from '@/api/resource'

const courseList = ref([])
const courseId = ref(null)
const chapters = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const formRef = ref()
const form = reactive({ id: null, courseId: null, parentId: 0, chapterTitle: '', sort: 0, hours: 0 })
const rules = { chapterTitle: [{ required: true, message: '请输入章节标题' }] }

const contentDialog = ref(false)
const currentChapter = ref(null)
const contents = ref([])
const contentLoading = ref(false)
const contentFormDialog = ref(false)
const contentFormRef = ref()
const contentForm = reactive({ id: null, courseId: null, chapterId: null, contentTitle: '', contentType: 1, resourceUrl: '', richText: '', sort: 0, hours: 0, status: 1 })
// 仅本地上传文件弹窗用，用于展示文件元数据（不发给后端，CourseContent 没对应字段）
const uploadedFile = ref(null)
const contentRules = {
  contentTitle: [{ required: true, message: '请输入内容标题' }],
  contentType: [{ required: true, message: '请选择类型' }]
}

const chapterOptions = computed(() => [{ id: 0, chapterTitle: '顶级章节' }, ...chapters.value])

const load = async () => {
  if (!courseId.value) return
  loading.value = true
  try {
    chapters.value = await chapterList(courseId.value)
  } finally { loading.value = false }
}

const openForm = (row) => {
  dialogVisible.value = true
  if (row) Object.assign(form, row)
  else Object.assign(form, { id: null, courseId: courseId.value, parentId: 0, chapterTitle: '', sort: 0, hours: 0 })
}

const submit = async () => {
  await formRef.value.validate()
  form.courseId = courseId.value
  if (form.id) await chapterEdit(form); else await chapterAdd(form)
  ElMessage.success('保存成功')
  dialogVisible.value = false
  load()
}

const remove = async (row) => {
  await ElMessageBox.confirm(`确定删除章节"${row.chapterTitle}"吗？`, '提示', { type: 'warning' })
  await chapterRemove(row.id)
  ElMessage.success('删除成功')
  load()
}

const openContent = async (row) => {
  currentChapter.value = row
  contentDialog.value = true
  contentLoading.value = true
  try { contents.value = await contentByChapter(row.id) }
  finally { contentLoading.value = false }
}

const openContentForm = (row) => {
  contentFormDialog.value = true
  if (row) {
    Object.assign(contentForm, row)
    // 编辑态时，如果已有 resourceUrl 就模拟一个上传文件条目，让 UI 显示"当前文件：xxx.xxx"
    uploadedFile.value = row.resourceUrl
      ? { fileUrl: row.resourceUrl, originalName: '（已上传文件）', fileSize: null, fileSuffix: '' }
      : null
  } else {
    Object.assign(contentForm, { id: null, courseId: courseId.value, chapterId: currentChapter.value.id, contentTitle: '', contentType: 1, resourceUrl: '', richText: '', sort: 0, hours: 0, status: 1 })
    uploadedFile.value = null
  }
}

const beforeContentUpload = (file) => {
  if (file.size > 100 * 1024 * 1024) { ElMessage.error('文件大小不能超过100MB'); return false }
}

const uploadContent = async (option) => {
  try {
    const res = await uploadFile(option.file)
    uploadedFile.value = {
      fileUrl: res.fileUrl,
      originalName: res.originalName,
      fileSize: res.fileSize,
      fileSuffix: res.fileSuffix
    }
    contentForm.resourceUrl = res.fileUrl
    ElMessage.success('上传成功')
  } catch (e) {
    ElMessage.error('上传失败：' + (e.message || '请稍后重试'))
  }
}

const submitContent = async () => {
  await contentFormRef.value.validate()
  if (contentForm.id) await contentEdit(contentForm); else await contentAdd(contentForm)
  ElMessage.success('保存成功')
  contentFormDialog.value = false
  contents.value = await contentByChapter(currentChapter.value.id)
}

const removeContent = async (row) => {
  await ElMessageBox.confirm('确定删除该内容吗？', '提示', { type: 'warning' })
  await contentRemove([row.id])
  contents.value = await contentByChapter(currentChapter.value.id)
}

// ===== 内容预览/下载（与 ResourceList 保持一致） =====

// 预览弹窗状态
const contentPreviewDialog = ref(false)
const contentPreviewRow = ref(null)
const contentPreviewType = ref('')   // 'pdf' | 'image' | 'video' | 'audio' | 'txt' | 'richtext' | 'external' | 'other'
const contentPreviewText = ref('')

// 根据内容类型 + URL 后缀判断预览走哪一支
const detectContentPreviewType = (row) => {
  if (!row) return 'other'
  // 富文本
  if (row.contentType === 4) return 'richtext'
  // 其余（上传的文件）按 URL 后缀判断
  const url = (row.resourceUrl || '').toLowerCase().split('?')[0]
  // 用 /api/files/yyyy/MM/xxx.ext 的最后一段做后缀
  const last = url.substring(url.lastIndexOf('/') + 1)
  const dot = last.lastIndexOf('.')
  const ext = dot >= 0 ? last.substring(dot + 1) : ''
  if (!ext) return 'other'
  if (['png','jpg','jpeg','gif','bmp','webp','svg'].includes(ext)) return 'image'
  if (ext === 'pdf') return 'pdf'
  if (['mp4','webm','ogg','mov','avi','mkv'].includes(ext)) return 'video'
  if (['mp3','wav','ogg','flac','aac','m4a'].includes(ext)) return 'audio'
  if (['txt','md','log','csv','json','xml','yml','yaml','ini'].includes(ext)) return 'txt'
  return 'other'
}

// 只有上传文件（contentType 1-3）能下载；富文本（4）没文件可下载
const canDownloadContent = (row) => {
  if (!row?.resourceUrl) return false
  if (row.contentType === 4) return false
  return true
}

const previewContent = async (row) => {
  contentPreviewRow.value = row
  contentPreviewType.value = detectContentPreviewType(row)
  contentPreviewDialog.value = true
  contentPreviewText.value = ''
  // 文本类自己 fetch 内容渲染（避免浏览器按本地 GBK 解析导致乱码）
  if (contentPreviewType.value === 'txt') {
    try {
      const resp = await fetch(row.resourceUrl)
      contentPreviewText.value = await resp.text()
    } catch (e) {
      contentPreviewText.value = '文件读取失败：' + (e.message || '网络异常')
    }
  }
}

const downloadContent = async (row) => {
  if (!row?.resourceUrl) return
  // 静态资源路径 /api/files/yyyy/MM/<uuid>.<ext> 直接 fetch 即可下载
  const url = row.resourceUrl
  // 从 URL 末段拿文件名（优先走 ? 后缀之前的部分）
  const filename = url.split('?')[0].split('/').pop() || 'download'

  let response
  try {
    response = await fetch(url)
  } catch (e) {
    ElMessage.error('下载失败：' + (e.message || '网络异常'))
    return
  }
  if (!response.ok) {
    ElMessage.error(`下载失败（HTTP ${response.status}）`)
    return
  }

  // 用 blob URL 触发下载：浏览器看到浏览器默认行为 → 有响应头 disposition 就保存，无则打开
  // 对静态资源没有 Content-Disposition，所以用一个不可见 <a download> 强制保存
  const blob = await response.blob()
  const blobUrl = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = blobUrl
  a.download = filename
  a.style.display = 'none'
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(blobUrl)
}

onMounted(async () => {
  courseList.value = await myCourses()
  if (courseList.value.length) { courseId.value = courseList.value[0].id; load() }
})
</script>
