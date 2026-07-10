<template>
  <div class="app-container">
    <el-card>
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input v-model="query.keyword" placeholder="资源名称" clearable style="width: 220px" @keyup.enter="load" />
          <!-- 筛选区：把普通 el-select 换成 el-tree-select，才能展示子分类 -->
          <el-tree-select
            v-model="query.categoryId"
            :data="categoryOptions"
            :props="{ value: 'id', label: 'categoryName' }"
            check-strictly
            clearable
            placeholder="分类"
            style="width: 160px"
          />
          <el-select v-model="query.resourceType" placeholder="类型" clearable style="width: 120px">
            <el-option v-for="d in dict.resource_type.value" :key="d.value" :label="d.label" :value="+d.value" />
          </el-select>
          <el-button type="primary" @click="load">搜索</el-button>
        </div>
        <el-button type="primary" :icon="Plus" @click="openForm()">上传资源</el-button>
      </div>

      <el-table :data="list" v-loading="loading" border>
        <el-table-column prop="resourceName" label="资源名称" />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag>{{ ['文档','图片','视频','音频','压缩包','其他'][row.resourceType - 1] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="分类" width="140" />
        <el-table-column prop="courseName" label="所属课程" width="160" />
        <el-table-column prop="originalName" label="原始文件名" width="200" show-overflow-tooltip />
        <el-table-column label="大小" width="100">
          <template #default="{ row }">{{ formatSize(row.fileSize) }}</template>
        </el-table-column>
        <el-table-column prop="downloadCount" label="下载次数" width="100" />
        <el-table-column prop="uploadName" label="上传人" width="100" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '上架' : '下架' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" link @click="preview(row)">预览</el-button>
            <el-button size="small" link @click="download(row)">下载</el-button>
            <el-button size="small" link @click="changeStatus(row, row.status === 1 ? 0 : 1)">{{ row.status === 1 ? '下架' : '上架' }}</el-button>
            <el-button size="small" link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination v-model:page="query.pageNum" v-model:size="query.pageSize" :total="total" @change="load" />
    </el-card>

    <el-dialog v-model="formDialog" title="上传资源" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="资源名称" prop="resourceName"><el-input v-model="form.resourceName" /></el-form-item>
        <el-form-item label="资源类型" v-if="form.fileSuffix">
          <el-tag>{{ ['文档','图片','视频','音频','压缩包','其他'][form.resourceType - 1] }}</el-tag>
          <span style="margin-left:8px;color:#999;font-size:12px">已根据文件后缀自动识别，无需手动选择</span>
        </el-form-item>
        <el-form-item label="资源分类" prop="categoryId">
          <el-tree-select v-model="form.categoryId" :data="categoryOptions" :props="{ value: 'id', label: 'categoryName' }" check-strictly style="width:100%" @change="onCategoryChange" />
        </el-form-item>
        <el-form-item label="所属课程">
          <el-select v-model="form.courseId" placeholder="选择关联课程（可空）" clearable filterable style="width:100%" @change="onCourseChange">
            <el-option v-for="c in courseOptions" :key="c.id" :label="c.courseName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="上传文件" prop="fileUrl">
          <el-upload :http-request="upload" :show-file-list="false" :before-upload="beforeUpload">
            <el-button>选择文件</el-button>
            <span v-if="form.fileUrl" style="margin-left: 8px">{{ form.originalName }}</span>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 预览弹窗：按文件类型分情况渲染（txt / pdf / 图片 / 视频 / 不支持） -->
    <el-dialog v-model="previewDialog" :title="`预览：${previewRow?.resourceName || ''}`" width="80%" top="5vh" destroy-on-close>
      <div v-loading="previewLoading" class="preview-box">
        <pre v-if="previewType === 'txt'" class="preview-txt">{{ previewText }}</pre>
        <div v-else-if="previewType === 'docx'" class="preview-docx" v-html="previewHtml"></div>
        <iframe v-else-if="previewType === 'pdf'" :src="previewRow?.fileUrl" class="preview-frame" />
        <img v-else-if="previewType === 'image'" :src="previewRow?.fileUrl" class="preview-image" />
        <video v-else-if="previewType === 'video'" :src="previewRow?.fileUrl" controls class="preview-media" />
        <audio v-else-if="previewType === 'audio'" :src="previewRow?.fileUrl" controls class="preview-media" />
        <div v-else class="preview-unsupported">
          <el-icon :size="48" color="#909399"><Document /></el-icon>
          <p class="title">该文件类型暂不支持在线预览</p>
          <p class="hint">可下载后用本地应用打开</p>
          <el-button type="primary" :icon="Download" @click="download(previewRow)">下载文件</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.preview-box { min-height: 400px; max-height: 70vh; overflow: auto; display: flex; justify-content: center; align-items: center; }
.preview-txt { width: 100%; max-height: 70vh; overflow: auto; padding: 16px; background: #fafafa; border-radius: 4px; font-family: Consolas, 'Courier New', monospace; font-size: 13px; line-height: 1.6; white-space: pre-wrap; word-break: break-all; margin: 0; }
.preview-frame { width: 100%; height: 70vh; border: 0; }
.preview-image { max-width: 100%; max-height: 70vh; object-fit: contain; }
.preview-media { max-width: 100%; }
.preview-docx { width: 100%; max-height: 70vh; overflow: auto; padding: 24px 32px; background: #fff; border: 1px solid #ebeef5; border-radius: 4px; line-height: 1.8; font-size: 14px; }
.preview-docx :deep(h1) { font-size: 22px; margin: 0.6em 0; }
.preview-docx :deep(h2) { font-size: 18px; margin: 0.5em 0; }
.preview-docx :deep(h3) { font-size: 16px; margin: 0.4em 0; }
.preview-docx :deep(p) { margin: 0.6em 0; }
.preview-docx :deep(table) { border-collapse: collapse; margin: 0.6em 0; }
.preview-docx :deep(table td),
.preview-docx :deep(table th) { border: 1px solid #dcdfe6; padding: 4px 8px; }
.preview-docx :deep(img) { max-width: 100%; }
.preview-unsupported { text-align: center; color: #909399; }
.preview-unsupported .title { font-size: 16px; color: #606266; margin: 12px 0 4px; }
.preview-unsupported .hint { font-size: 13px; margin: 0 0 16px; }
</style>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Document, Download } from '@element-plus/icons-vue'
import Pagination from '@/components/Pagination.vue'
import { useDict } from '@/hooks/useDict'
import { useUserStore } from '@/store/user'
import { resPage, resAdd, resRemove, resChangeStatus, resCategoryTree, uploadFile } from '@/api/resource'
import { listAllCourses } from '@/api/course'
import * as mammoth from 'mammoth'

const dict = useDict('resource_type')
const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', categoryId: null, resourceType: null })
const categoryOptions = ref([])
const courseOptions = ref([])

// 预览弹窗状态
const previewDialog = ref(false)
const previewLoading = ref(false)
const previewRow = ref(null)
const previewType = ref('')   // 'txt' | 'docx' | 'pdf' | 'image' | 'video' | 'audio' | 'other'
const previewText = ref('')   // 文本类文件读取后的内容
const previewHtml = ref('')   // docx 转换后的 HTML
const formDialog = ref(false)
const submitting = ref(false)
const formRef = ref()
const form = reactive({ resourceName: '', resourceType: 1, categoryId: null, courseId: null, courseName: '', originalName: '', filePath: '', fileUrl: '', fileSize: 0, fileSuffix: '', contentType: '' })
const rules = {
  resourceName: [{ required: true, message: '请输入资源名称' }],
  // 自定义上传组件不走 v-model，必须用 validator 校验；trigger 用 change，
  // 再在 upload() 成功后手动 validateField('fileUrl') 清掉提示
  fileUrl: [{
    required: true,
    validator: (rule, value, callback) => {
      if (!value) callback(new Error('请上传文件'))
      else callback()
    },
    trigger: 'change'
  }]
}

// 根据文件后缀自动判断资源类型，与 sys_dict_data.resource_type 保持一致
const EXT_TYPE_MAP = {
  doc: 1, docx: 1, pdf: 1, txt: 1, xls: 1, xlsx: 1, ppt: 1, pptx: 1, md: 1, csv: 1,
  jpg: 2, jpeg: 2, png: 2, gif: 2, bmp: 2, webp: 2, svg: 2,
  mp4: 3, avi: 3, mov: 3, wmv: 3, flv: 3, mkv: 3,
  mp3: 4, wav: 4, flac: 4, aac: 4, m4a: 4,
  zip: 5, rar: 5, '7z': 5, tar: 5, gz: 5
}
const guessResourceType = (suffix) => EXT_TYPE_MAP[(suffix || '').toLowerCase()] || 6

const load = async () => {
  loading.value = true
  try { const res = await resPage(query); list.value = res.list; total.value = res.total }
  finally { loading.value = false }
}

const loadCategories = async () => {
  categoryOptions.value = await resCategoryTree()
}

const loadCourses = async () => {
  courseOptions.value = await listAllCourses()
}

// 课程下拉变化时，把 courseName 一并写入 form，便于后端冗余存储
const onCourseChange = (courseId) => {
  const c = courseOptions.value.find(item => item.id === courseId)
  form.courseName = c ? c.courseName : ''
}

// 分类树选中后，把 categoryName 一并写入 form；树结构需要递归查找
const findInCategoryTree = (nodes, id) => {
  for (const n of nodes) {
    if (n.id === id) return n
    if (n.children && n.children.length) {
      const hit = findInCategoryTree(n.children, id)
      if (hit) return hit
    }
  }
  return null
}
const onCategoryChange = (categoryId) => {
  if (!categoryId) { form.categoryName = ''; return }
  const node = findInCategoryTree(categoryOptions.value, categoryId)
  form.categoryName = node ? node.categoryName : ''
}

const openForm = async () => {
  formDialog.value = true
  if (!categoryOptions.value.length) await loadCategories()
  if (!courseOptions.value.length) await loadCourses()
  Object.assign(form, { resourceName: '', resourceType: 1, categoryId: null, courseId: null, courseName: '', originalName: '', filePath: '', fileUrl: '', fileSize: 0, fileSuffix: '', contentType: '' })
}

const beforeUpload = (file) => {
  if (file.size > 100 * 1024 * 1024) { ElMessage.error('文件大小不能超过100MB'); return false }
}

const upload = async (option) => {
  const res = await uploadFile(option.file)
  form.fileUrl = res.fileUrl
  form.originalName = res.originalName
  form.fileSize = res.fileSize
  form.fileSuffix = res.fileSuffix
  form.resourceType = guessResourceType(res.fileSuffix)   // 新增：自动识别类型
  if (!form.resourceName) form.resourceName = res.originalName
  ElMessage.success('上传成功')
  // 文件上传成功后立即校验 fileUrl，清掉"请上传文件"的红色提示
  formRef.value?.validateField('fileUrl').catch(() => {})
}

const submit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try { await resAdd(form); ElMessage.success('保存成功'); formDialog.value = false; load() }
  finally { submitting.value = false }
}

const remove = async (row) => { await ElMessageBox.confirm(`确定删除"${row.resourceName}"？`, '提示', { type: 'warning' }); await resRemove([row.id]); ElMessage.success('删除成功'); load() }
const changeStatus = async (row, status) => { await resChangeStatus(row.id, status); ElMessage.success('操作成功'); load() }
const download = async (row) => {
  // 走 fetch + Authorization 头，保证请求能被 JwtAuthenticationFilter 识别；
  // 否则浏览器直接 <a href> 不会带 Bearer，会被 401，结果是浏览器把 {"code":401,...} 当文件下载。
  const userStore = useUserStore()
  let response
  try {
    response = await fetch(`/api/biz/resource/${row.id}/file`, {
      headers: { 'Authorization': 'Bearer ' + userStore.token }
    })
  } catch (e) {
    ElMessage.error('网络异常：' + (e.message || '请稍后重试'))
    return
  }

  if (!response.ok) {
    // 尝试解析后端返回的 JSON 错误信息
    let msg = `下载失败（HTTP ${response.status}）`
    try {
      const data = await response.json()
      if (data && data.message) msg = data.message
    } catch (_) { /* 不是 JSON 就用默认 msg */ }
    ElMessage.error(msg)
    return
  }

  // 从响应头取真实文件名（处理中文），回退到资源原始名
  let filename = row.originalName || row.resourceName || 'download'
  const disposition = response.headers.get('Content-Disposition') || ''
  const utf8Match = disposition.match(/filename\*=UTF-8''([^;]+)/i)
  if (utf8Match) {
    try { filename = decodeURIComponent(utf8Match[1]) } catch (_) {}
  }

  // 用 blob URL 触发下载，浏览器看到 attachment 头会直接保存，不打开新标签页
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
const preview = async (row) => {
  previewRow.value = row
  previewType.value = detectPreviewType(row)
  previewDialog.value = true
  previewText.value = ''
  previewHtml.value = ''

  // 文本类自己 fetch 内容渲染，避免浏览器按本地 GBK 解析导致乱码
  if (previewType.value === 'txt') {
    previewLoading.value = true
    try {
      const resp = await fetch(row.fileUrl)
      previewText.value = await resp.text()
    } catch (e) {
      previewText.value = '文件读取失败：' + (e.message || '网络异常')
    } finally {
      previewLoading.value = false
    }
  }

  // docx 用 mammoth 转 HTML 渲染；doc 是二进制 CFB，浏览器侧无法解析，仍走 unsupported 提示
  if (previewType.value === 'docx') {
    previewLoading.value = true
    try {
      const resp = await fetch(row.fileUrl)
      const buffer = await resp.arrayBuffer()
      const result = await mammoth.convertToHtml({ arrayBuffer: buffer })
      previewHtml.value = result.value
      if (result.messages && result.messages.length) {
        console.warn('mammoth warnings:', result.messages)
      }
    } catch (e) {
      previewHtml.value = `<p style="color:#f56c6c">文档解析失败：${e.message || '未知错误'}</p>`
    } finally {
      previewLoading.value = false
    }
  }
}

// 根据文件名/后缀判断预览类型
const detectPreviewType = (row) => {
  const name = (row.originalName || row.resourceName || '').toLowerCase()
  const ext = name.includes('.') ? name.split('.').pop() : ''
  if (['txt', 'md', 'log', 'csv', 'json', 'xml', 'yml', 'yaml', 'ini'].includes(ext)) return 'txt'
  if (ext === 'pdf') return 'pdf'
  if (['png', 'jpg', 'jpeg', 'gif', 'bmp', 'webp', 'svg'].includes(ext)) return 'image'
  if (['mp4', 'webm', 'ogg', 'mov', 'avi', 'mkv'].includes(ext)) return 'video'
  if (['mp3', 'wav', 'ogg', 'flac', 'aac', 'm4a'].includes(ext)) return 'audio'
  if (ext === 'docx') return 'docx'    // mammoth 在浏览器里把 docx 转 HTML
  return 'other'   // doc/xls/xlsx/ppt/pptx/zip 等浏览器原生无法渲染
}

const formatSize = (bytes) => {
  if (!bytes) return '-'
  if (bytes < 1024) return bytes + 'B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + 'KB'
  if (bytes < 1024 * 1024 * 1024) return (bytes / 1024 / 1024).toFixed(1) + 'MB'
  return (bytes / 1024 / 1024 / 1024).toFixed(2) + 'GB'
}

onMounted(() => {
  load()
  loadCategories()   // 页面一进来就加载分类树，供筛选区使用
})
</script>
