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
          <el-tree-select v-model="form.categoryId" :data="categoryOptions" :props="{ value: 'id', label: 'categoryName' }" check-strictly style="width:100%" />
        </el-form-item>
        <el-form-item label="上传文件">
          <el-upload :http-request="upload" :show-file-list="false" :before-upload="beforeUpload">
            <el-button>选择文件</el-button>
            <span v-if="form.fileUrl" style="margin-left: 8px">{{ form.originalName }}</span>
          </el-upload>
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
import { useDict } from '@/hooks/useDict'
import { resPage, resAdd, resRemove, resChangeStatus, resDownload, resCategoryTree, uploadFile } from '@/api/resource'

const dict = useDict('resource_type')
const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', categoryId: null, resourceType: null })
const categoryOptions = ref([])
const formDialog = ref(false)
const submitting = ref(false)
const formRef = ref()
const form = reactive({ resourceName: '', resourceType: 1, categoryId: null, originalName: '', filePath: '', fileUrl: '', fileSize: 0, fileSuffix: '', contentType: '', tags: '', description: '' })
const rules = { resourceName: [{ required: true, message: '请输入资源名称' }] }

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

const openForm = async () => {
  formDialog.value = true
  if (!categoryOptions.value.length) await loadCategories()
  Object.assign(form, { resourceName: '', resourceType: 1, categoryId: null, originalName: '', filePath: '', fileUrl: '', fileSize: 0, fileSuffix: '', contentType: '', tags: '', description: '' })
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
  await resDownload(row.id)
  window.open(row.fileUrl, '_blank')
}
const preview = (row) => { window.open(row.fileUrl, '_blank') }

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
