<template>
  <div class="app-container">
    <el-card>
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input v-model="query.keyword" placeholder="资源名称" clearable style="width: 220px" @keyup.enter="load" />
          <el-select v-model="query.categoryId" placeholder="分类" clearable style="width: 160px" @change="load">
            <el-option v-for="c in categoryOptions" :key="c.id" :label="c.categoryName" :value="c.id" />
          </el-select>
          <el-select v-model="query.resourceType" placeholder="类型" clearable style="width: 120px" @change="load">
            <el-option v-for="d in dict.resource_type" :key="d.value" :label="d.label" :value="+d.value" />
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
        <el-table-column prop="originalName" label="原始文件" width="200" show-overflow-tooltip />
        <el-table-column label="大小" width="100">
          <template #default="{ row }">{{ formatSize(row.fileSize) }}</template>
        </el-table-column>
        <el-table-column prop="viewCount" label="浏览" width="70" />
        <el-table-column prop="downloadCount" label="下载" width="70" />
        <el-table-column prop="uploadName" label="上传人" width="100" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '上架' : '下架' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button size="small" link @click="preview(row)">预览</el-button>
            <el-button size="small" link @click="download(row)">下载</el-button>
            <el-button size="small" link @click="openEdit(row)">编辑</el-button>
            <el-button size="small" link @click="changeStatus(row, row.status === 1 ? 0 : 1)">{{ row.status === 1 ? '下架' : '上架' }}</el-button>
            <el-button size="small" link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination v-model:page="query.pageNum" v-model:size="query.pageSize" :total="total" @change="load" />
    </el-card>

    <el-dialog v-model="formDialog" :title="form.id ? '编辑资源' : '上传资源'" width="640px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="资源名称" prop="resourceName"><el-input v-model="form.resourceName" /></el-form-item>
        <el-form-item label="资源类型" prop="resourceType">
          <el-select v-model="form.resourceType" style="width:100%">
            <el-option v-for="d in dict.resource_type" :key="d.value" :label="d.label" :value="+d.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="资源分类" prop="categoryId">
          <el-tree-select v-model="form.categoryId" :data="categoryOptions" :props="{ value: 'id', label: 'categoryName' }" check-strictly style="width:100%" />
        </el-form-item>
        <el-form-item label="所属课程">
          <el-select v-model="form.courseId" placeholder="（不关联任何课程）" clearable filterable style="width:100%">
            <el-option v-for="c in courseOptions" :key="c.id" :label="c.courseName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="!form.id" label="上传文件">
          <el-upload :http-request="upload" :show-file-list="false" :before-upload="beforeUpload">
            <el-button>选择文件</el-button>
            <span v-if="form.fileUrl" style="margin-left: 8px">{{ form.originalName }}</span>
          </el-upload>
        </el-form-item>
        <el-form-item v-else label="已上传文件">
          <span>{{ form.originalName || '-' }}</span>
        </el-form-item>
        <el-form-item label="标签"><el-input v-model="form.tags" placeholder="多个标签用逗号分隔" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import Pagination from '@/components/Pagination.vue'
import {
  resPage, resAdd, resEdit, resRemove, resChangeStatus, resDownload, resDetail,
  resCategoryTree, uploadFile
} from '@/api/resource'
import { dictDataList } from '@/api/system'
import { listAllCourses } from '@/api/course'

const dict = reactive({ resource_type: [] })
const loadDict = async () => {
  try {
    const list = await dictDataList('resource_type')
    dict.resource_type = list.map(d => ({ value: d.dictValue, label: d.dictLabel, raw: d }))
  } catch (e) { dict.resource_type = [] }
}
const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', categoryId: null, resourceType: null })
const categoryOptions = ref([])
const courseOptions = ref([])
const formDialog = ref(false)
const submitting = ref(false)
const formRef = ref()
const form = reactive({
  id: null, resourceName: '', resourceType: 1, categoryId: null, courseId: null,
  courseName: '', originalName: '', filePath: '', fileUrl: '', fileSize: 0,
  fileSuffix: '', contentType: '', tags: '', description: '', status: 1
})
const rules = {
  resourceName: [{ required: true, message: '请输入资源名称' }],
  resourceType: [{ required: true, message: '请选择资源类型' }]
}

const load = async () => {
  loading.value = true
  try {
    const res = await resPage({
      pageNum: query.pageNum, pageSize: query.pageSize,
      resourceName: query.keyword || undefined,
      categoryId: query.categoryId || undefined,
      resourceType: query.resourceType || undefined
    })
    list.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

const openForm = async () => {
  formDialog.value = true
  const [cats, courses] = await Promise.all([resCategoryTree(), listAllCourses()])
  categoryOptions.value = cats
  courseOptions.value = courses
  Object.assign(form, {
    id: null, resourceName: '', resourceType: 1, categoryId: null, courseId: null,
    courseName: '', originalName: '', filePath: '', fileUrl: '', fileSize: 0,
    fileSuffix: '', contentType: '', tags: '', description: '', status: 1
  })
}

const openEdit = async (row) => {
  formDialog.value = true
  const [cats, courses] = await Promise.all([resCategoryTree(), listAllCourses()])
  categoryOptions.value = cats
  courseOptions.value = courses
  Object.assign(form, row)
}

const beforeUpload = (file) => {
  if (file.size > 100 * 1024 * 1024) { ElMessage.error('文件大小不能超过100MB'); return false }
}

const upload = async (option) => {
  const res = await uploadFile(option.file)
  form.fileUrl = res.fileUrl
  form.filePath = res.storageKey || ''   // 关键：后端用 storageKey 删文件
  form.originalName = res.originalName
  form.fileSize = res.fileSize
  form.fileSuffix = res.fileSuffix
  if (!form.resourceName) form.resourceName = res.originalName
  ElMessage.success('上传成功')
}

const submit = async () => {
  await formRef.value.validate()
  // 把 courseId 同步成 courseName 给后端冗余存
  const c = courseOptions.value.find(x => x.id === form.courseId)
  form.courseName = c ? c.courseName : ''
  submitting.value = true
  try {
    if (form.id) {
      await resEdit(form)
      ElMessage.success('保存成功')
    } else {
      await resAdd(form)
      ElMessage.success('保存成功')
    }
    formDialog.value = false
    load()
  } finally {
    submitting.value = false
  }
}

const remove = async (row) => {
  await ElMessageBox.confirm(`确定删除"${row.resourceName}"？`, '提示', { type: 'warning' })
  await resRemove([row.id])
  ElMessage.success('删除成功')
  load()
}

const changeStatus = async (row, status) => {
  await resChangeStatus(row.id, status)
  ElMessage.success('操作成功')
  load()
}

// 预览：先调详情接口让后端 +1 view_count，再开窗口
const openInNewTab = (url) => {
  const a = document.createElement('a')
  a.href = url
  a.target = '_blank'
  a.rel = 'noopener noreferrer'
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
}

const preview = async (row) => {
  if (!row.fileUrl) { ElMessage.error('no file to preview'); return }
  openInNewTab(row.fileUrl)
  resDetail(row.id).catch(() => {})
  row.viewCount = (row.viewCount || 0) + 1
}

// 下载：先调后端 +1 download_count，再开窗口
const download = async (row) => {
  if (!row.fileUrl) { ElMessage.error('no file to download'); return }
  openInNewTab(row.fileUrl)
  resDownload(row.id).catch(() => {})
  row.downloadCount = (row.downloadCount || 0) + 1
}

const formatSize = (bytes) => {
  if (!bytes) return '-'
  if (bytes < 1024) return bytes + 'B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + 'KB'
  if (bytes < 1024 * 1024 * 1024) return (bytes / 1024 / 1024).toFixed(1) + 'MB'
  return (bytes / 1024 / 1024 / 1024).toFixed(2) + 'GB'
}

onMounted(() => { load(); loadDict() })
</script>