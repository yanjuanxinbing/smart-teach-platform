<template>
  <div class="app-container">
    <el-card>
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input v-model="query.keyword" placeholder="标题" clearable style="width: 220px" @keyup.enter="load" />
        </div>
        <el-button type="primary" :icon="Plus" @click="openForm()">新增{{ pageTitle }}</el-button>
      </div>

      <el-table :data="list" v-loading="loading" border>
        <el-table-column v-if="type === 1" label="封面" width="160">
          <template #default="{ row }">
            <el-image v-if="row.coverImage" :src="row.coverImage" style="width: 120px; height: 60px" fit="cover" />
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column label="置顶" width="80">
          <template #default="{ row }"><el-tag v-if="row.top === 1" type="danger">是</el-tag><span v-else>-</span></template>
        </el-table-column>
        <el-table-column prop="publishTime" label="发布时间" width="170" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="['info','success','warning'][row.status]">{{ ['草稿','已发布','已下线'][row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button size="small" link @click="openForm(row)">编辑</el-button>
            <el-button size="small" link v-if="row.status === 0" @click="publish(row)">发布</el-button>
            <el-button size="small" link v-if="row.status === 1" @click="offline(row)">下线</el-button>
            <el-button size="small" link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination v-model:page="query.pageNum" v-model:size="query.pageSize" :total="total" @change="load" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? `编辑${pageTitle}` : `新增${pageTitle}`" width="640px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="标题" prop="title"><el-input v-model="form.title" /></el-form-item>
        <el-form-item v-if="type === 1" label="封面图" prop="coverImage">
          <el-upload
            :http-request="uploadCover"
            :show-file-list="false"
            :before-upload="beforeUploadCover"
            accept="image/*"
            class="banner-uploader"
          >
            <div class="banner-uploader__area" :class="{ 'is-loaded': !!form.coverImage, 'is-loading': coverUploading }">
              <el-image
                v-if="form.coverImage"
                :src="form.coverImage"
                class="banner-uploader__preview"
                fit="cover"
                @click.stop
              />
              <div v-else class="banner-uploader__placeholder">
                <el-icon class="banner-uploader__icon" v-if="!coverUploading"><Plus /></el-icon>
                <el-icon class="banner-uploader__icon is-loading" v-else><Loading /></el-icon>
                <span class="banner-uploader__text">{{ coverUploading ? '上传中...' : '点击上传封面图' }}</span>
                <span class="banner-uploader__hint">支持 jpg/png/webp/gif</span>
              </div>
              <div v-if="form.coverImage && !coverUploading" class="banner-uploader__mask">
                <el-icon><Refresh /></el-icon>
                <span>点击更换</span>
              </div>
            </div>
          </el-upload>
        </el-form-item>
        <el-form-item v-if="type === 1" label="跳转链接"><el-input v-model="form.linkUrl" placeholder="留空则跳转详情页" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
        <el-form-item label="置顶">
          <el-radio-group v-model="form.top"><el-radio :value="1">是</el-radio><el-radio :value="0">否</el-radio></el-radio-group>
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" :rows="6" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Loading } from '@element-plus/icons-vue'
import Pagination from '@/components/Pagination.vue'
import { portalPage, portalAdd, portalEdit, portalRemove, portalPublish, portalOffline } from '@/api/portal'

// 通用门户内容管理页：1轮播图 2通知公告 3新闻资讯
// 通过 type prop 切换栏目，UI 行为统一，仅在封面图 / 跳转链接 / 文案上有差异。
const props = defineProps({
  type: { type: Number, default: 1 }
})

const TYPE_LABELS = { 1: '轮播图', 2: '通知公告', 3: '新闻资讯' }
const pageTitle = computed(() => TYPE_LABELS[props.type] || '内容')

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ pageNum: 1, pageSize: 10, type: props.type, keyword: '' })
const dialogVisible = ref(false)
const formRef = ref()
const form = reactive({ id: null, type: props.type, title: '', coverImage: '', linkUrl: '', content: '', sort: 0, top: 0, status: 0 })
// 校验规则：仅当 type=1（轮播图）才要求封面图；通知 / 新闻不强求封面图
const rules = computed(() => {
  const base = { title: [{ required: true, message: '请输入标题' }] }
  if (props.type === 1) base.coverImage = [{ required: true, message: '请上传封面图' }]
  return base
})
const coverUploading = ref(false)

const beforeUploadCover = (file) => {
  if (!file.type.startsWith('image/')) { ElMessage.error('请选择图片文件'); return false }
  // 受 MySQL max_allowed_packet 默认 4MB 限制，原始文件限制 2MB（base64 后 ~2.7MB）比较稳妥
  if (file.size > 2 * 1024 * 1024) { ElMessage.error('图片大小不能超过2MB'); return false }
}

const readFileAsDataURL = (file) => new Promise((resolve, reject) => {
  const reader = new FileReader()
  reader.onload = () => resolve(reader.result)
  reader.onerror = () => reject(reader.error)
  reader.readAsDataURL(file)
})

// el-upload 自定义请求：用 FileReader 在浏览器内读取为 dataURL，
// 不再走 /biz/resource/upload，绕开磁盘路径 / 跨域 / 静态资源映射等问题。
// 选项 onSuccess 仍然要调用，否则 el-upload 内部会一直处于 loading 状态。
const uploadCover = async (option) => {
  coverUploading.value = true
  try {
    const dataUrl = await readFileAsDataURL(option.file)
    form.coverImage = dataUrl
    ElMessage.success('读取成功')
    option.onSuccess?.({ fileUrl: dataUrl })
  } catch (e) {
    ElMessage.error('图片读取失败：' + (e?.message || e))
    option.onError?.(e)
  } finally {
    coverUploading.value = false
  }
}

const load = async () => {
  loading.value = true
  try { const res = await portalPage(query); list.value = res.list; total.value = res.total }
  finally { loading.value = false }
}

const openForm = (row) => {
  dialogVisible.value = true
  if (row) Object.assign(form, row)
  else Object.assign(form, { id: null, type: props.type, title: '', coverImage: '', linkUrl: '', content: '', sort: 0, top: 0, status: 0 })
}

const submit = async () => {
  try {
    await formRef.value.validate()
    if (form.id) await portalEdit(form); else await portalAdd(form)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } catch (e) {
    // 拦截器里已经 ElMessage.error 过，这里只吞掉，避免 Vue 把它当成未处理的 component event 报错
    console.debug('保存失败：', e?.message || e)
  }
}
const publish = async (row) => { await portalPublish(row.id); ElMessage.success('已发布'); load() }
const offline = async (row) => { await portalOffline(row.id); ElMessage.success('已下线'); load() }
const remove = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除"${row.title}"？`, '提示', { type: 'warning' })
    await portalRemove([row.id])
    ElMessage.success('删除成功')
    load()
  } catch (e) {
    // 用户点取消时 ElMessageBox.confirm 会 reject，这里吞掉，避免 Vue 警告
    if (e !== 'cancel' && e?.message !== 'cancel') console.debug('删除失败：', e?.message || e)
  }
}

onMounted(load)
</script>

<style scoped>
.banner-uploader {
  display: inline-block;
}
.banner-uploader__area {
  position: relative;
  width: 320px;
  height: 180px;
  border: 1px dashed #dcdfe6;
  border-radius: 6px;
  background: #fafafa;
  cursor: pointer;
  overflow: hidden;
  transition: border-color 0.2s;
}
.banner-uploader__area:hover {
  border-color: #409eff;
}
.banner-uploader__area.is-loading {
  cursor: wait;
}
.banner-uploader__area.is-loaded {
  border-style: solid;
  border-color: transparent;
}
.banner-uploader__placeholder {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
}
.banner-uploader__icon {
  font-size: 32px;
  margin-bottom: 8px;
}
.banner-uploader__icon.is-loading {
  animation: rotating 1.4s linear infinite;
}
.banner-uploader__text {
  font-size: 14px;
  color: #606266;
}
.banner-uploader__hint {
  margin-top: 4px;
  font-size: 12px;
  color: #c0c4cc;
}
.banner-uploader__preview {
  width: 100%;
  height: 100%;
  display: block;
}
.banner-uploader__mask {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  background: rgba(0, 0, 0, 0.5);
  color: #fff;
  font-size: 14px;
  opacity: 0;
  transition: opacity 0.2s;
}
.banner-uploader__area:hover .banner-uploader__mask {
  opacity: 1;
}
@keyframes rotating {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>