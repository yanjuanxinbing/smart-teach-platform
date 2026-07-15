<template>
  <div class="page">
    <header class="toolbar">
      <div class="filters">
        <button
          v-for="t in typeOptions"
          :key="t.value"
          class="tag"
          :class="{ 'tag--active': filters.type === t.value }"
          @click="setType(t.value)"
        >{{ t.label }}</button>
      </div>
      <div class="toolbar__right">
        <el-input v-model="filters.q" placeholder="搜索资源名 / 标签" clearable class="search">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
      </div>
    </header>

    <div v-if="state === 'loading'" class="loading-tip">资源加载中…</div>
    <div v-else-if="state === 'empty'" class="empty">
      <el-icon :size="32"><FolderOpened /></el-icon>
      <p>暂无资源</p>
      <p class="empty__hint">管理员在 8081 后台上传后,资源会自动出现在这里</p>
    </div>
    <div v-else-if="state === 'error'" class="empty">
      <el-icon :size="32"><Warning /></el-icon>
      <p>资源数据暂时不可用，请稍后再试</p>
      <el-button class="empty__cta" size="small" plain @click="fetch">重新加载</el-button>
    </div>

    <ul v-else class="rlist">
      <li
        v-for="(r, idx) in list"
        :key="r.id"
        class="rrow"
        :style="{ transitionDelay: `${Math.min(idx, 8) * 40}ms` }"
        @click="goDetail(r)"
      >
        <span class="rrow__idx">{{ String(idx + 1).padStart(2, '0') }}</span>
        <div class="rrow__ico" :class="`rrow__ico--t${r.resourceType || 6}`">
          <el-icon :size="22"><component :is="typeIcon(r.resourceType)" /></el-icon>
        </div>
        <div class="rrow__main">
          <div class="rrow__top">
            <span class="rrow__type">{{ typeLabel(r.resourceType) }}</span>
            <span v-if="r.categoryName" class="rrow__cat">{{ r.categoryName }}</span>
            <span v-if="r.courseName" class="rrow__course">· {{ r.courseName }}</span>
          </div>
          <h3 class="rrow__title">{{ r.resourceName || '未命名资源' }}</h3>
          <div class="rrow__meta">
            <span>{{ r.uploadName || '—' }}</span>
            <span>· {{ fmtDate(r.createTime) }}</span>
            <span v-if="r.fileSize">· {{ humanSize(r.fileSize) }}</span>
            <span v-if="r.tags">· {{ r.tags }}</span>
          </div>
        </div>
        <div class="rrow__stat">
          <span class="rrow__count" :title="`浏览 ${r.viewCount || 0} 次`">
            <el-icon :size="13"><View /></el-icon>{{ r.viewCount || 0 }}
          </span>
          <span class="rrow__count" :title="`下载 ${r.downloadCount || 0} 次`">
            <el-icon :size="13"><Download /></el-icon>{{ r.downloadCount || 0 }}
          </span>
        </div>
        <el-button plain size="small" :loading="downloadingId === r.id" @click.stop="onDownload(r)">下载</el-button>
      </li>
    </ul>

    <div v-if="state === 'ok' && list.length" class="pager">
      <el-pagination
        v-model:current-page="page.current"
        v-model:page-size="page.size"
        :total="total"
        layout="prev, pager, next, jumper"
        background
        @current-change="fetch"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Search, FolderOpened, Warning,
  Document, Picture, VideoCamera, Headset, Files, MoreFilled,
  View, Download
} from '@element-plus/icons-vue'
import { listPortalResources, getPortalResource, downloadPortalResource } from '@/api/resource'

const router = useRouter()

// 资源类型映射 —— 1文档 2图片 3视频 4音频 5压缩包 6其他
const TYPE_LABEL = { 1: '文档', 2: '图片', 3: '视频', 4: '音频', 5: '压缩包', 6: '其他' }
const TYPE_ICON = {
  1: Document, 2: Picture, 3: VideoCamera, 4: Headset, 5: Files, 6: MoreFilled
}
const typeLabel = (t) => TYPE_LABEL[t] || TYPE_LABEL[6]
const typeIcon = (t) => TYPE_ICON[t] || TYPE_ICON[6]

// 顶部类型 tag 过滤 —— 「全部」+ 1-6
const typeOptions = [
  { value: '', label: '全部' },
  { value: 1, label: '文档' },
  { value: 2, label: '图片' },
  { value: 3, label: '视频' },
  { value: 4, label: '音频' },
  { value: 5, label: '压缩包' },
  { value: 6, label: '其他' }
]

const state = ref('loading') // loading | empty | error | ok
const list = ref([])
const total = ref(0)
const page = reactive({ current: 1, size: 12 })
const filters = reactive({ q: '', type: '' })

const setType = (v) => { filters.type = v; page.current = 1; fetch() }

const fmtDate = (iso) => {
  if (!iso) return '—'
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return iso
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}
const humanSize = (n) => {
  if (!n || n <= 0) return ''
  if (n < 1024) return `${n} B`
  if (n < 1024 * 1024) return `${(n / 1024).toFixed(1)} KB`
  if (n < 1024 * 1024 * 1024) return `${(n / 1024 / 1024).toFixed(2)} MB`
  return `${(n / 1024 / 1024 / 1024).toFixed(2)} GB`
}

// 跳详情(暂时复用 DocumentView 渲染富文本/HTML,纯文件直接走下载)
// DocumentView 期望 docType + content,资源没有 content 所以会显示"文档待编辑"占位,
// 用户可点右上角「下载」按钮下载原始文件 —— 这是 v1 的合理降级。
const goDetail = async (r) => {
  if (!r?.id) return
  try {
    await getPortalResource(r.id)
  } catch (e) { /* 静默,详情浏览数自增只是锦上添花 */ }
  router.push({
    path: '/profile/document',
    query: { resourceId: r.id }
  })
}

// 触发浏览器 Blob 下载 —— 把 blob 转临时 <a> 标签下载,后释放 URL 防内存泄漏
const triggerBlobDownload = (blob, filename) => {
  const url = window.URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename || 'resource'
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  setTimeout(() => window.URL.revokeObjectURL(url), 100)
}

const downloadingId = ref(null)
const onDownload = async (r) => {
  if (!r?.id) return
  downloadingId.value = r.id
  try {
    const blob = await downloadPortalResource(r.id)
    triggerBlobDownload(blob, r.originalName || r.resourceName || 'resource')
    ElMessage.success('下载已开始')
  } catch (e) {
    console.warn('功能开发中:资源下载接口未就绪', e?.message)
    ElMessage.warning('资源下载功能开发中,请稍后重试')
  } finally {
    downloadingId.value = null
  }
}

const fetch = async () => {
  state.value = 'loading'
  try {
    const res = await listPortalResources({
      current: page.current,
      size: page.size,
      resourceName: filters.q,
      resourceType: filters.type || undefined
    })
    const records = res?.records || res?.list || []
    list.value = records
    total.value = Number(res?.total ?? records.length)
    state.value = records.length ? 'ok' : 'empty'
  } catch (e) {
    state.value = 'error'
    list.value = []
    total.value = 0
  }
}

let timer
watch(() => filters.q, () => {
  clearTimeout(timer); timer = setTimeout(() => { page.current = 1; fetch() }, 300)
})

onMounted(fetch)
</script>

<style scoped>
.toolbar {
  display: flex; align-items: center; justify-content: space-between;
  gap: var(--s-5); flex-wrap: wrap;
  padding-bottom: var(--s-4); margin-bottom: var(--s-6);
  border-bottom: 1px solid var(--line);
}
.filters { display: flex; gap: 6px; flex-wrap: wrap; }
.tag {
  font-family: var(--font-mono); font-size: 11px; letter-spacing: 0.16em; text-transform: uppercase;
  padding: 9px 14px; background: var(--surface); border: 1px solid var(--line); color: var(--ink-soft);
  cursor: pointer; transition: border-color var(--t-fast) var(--ease), color var(--t-fast) var(--ease), background-color var(--t-fast) var(--ease);
}
.tag:hover { color: var(--ink); border-color: var(--ink); }
.tag--active { background: var(--ink); color: #fff; border-color: var(--ink); }
.toolbar__right { display: flex; gap: var(--s-3); }
.search { width: 240px; }
.search :deep(.el-input__wrapper) { border-radius: 0; }

.loading-tip { color: var(--mute); font-size: 13px; letter-spacing: 0.06em; text-align: center; padding: 64px 0; }
.empty { padding: 88px 0; text-align: center; color: var(--mute); display: flex; flex-direction: column; align-items: center; gap: 14px; }
.empty__hint { font-size: 12px; color: var(--mute); margin: 0; }
.empty__cta { color: var(--accent); text-decoration: none; font-size: 13px; }
.empty__cta:hover { text-decoration: underline; }

.rlist { list-style: none; margin: 0; padding: 0; background: var(--surface); border: 1px solid var(--line); }
.rrow {
  display: grid;
  grid-template-columns: 40px 44px minmax(0, 1fr) auto auto;
  align-items: center; gap: var(--s-4);
  padding: 16px 20px;
  border-bottom: 1px solid var(--line-soft);
  cursor: pointer;
  transition: background-color var(--t-fast) var(--ease);
}
.rrow:last-child { border-bottom: none; }
.rrow:hover { background: var(--surface-soft); }

.rrow__idx { font-family: var(--font-mono); font-size: 11px; color: var(--mute); }
.rrow__ico {
  width: 44px; height: 44px;
  display: flex; align-items: center; justify-content: center;
  background: var(--accent-tint); color: var(--accent);
}
.rrow__ico--t1 { background: #E8F0FF; color: #3B6BFF; }
.rrow__ico--t2 { background: #F0E8FF; color: #7C3BFF; }
.rrow__ico--t3 { background: #FFE8EC; color: #D63B5C; }
.rrow__ico--t4 { background: #FFEEDB; color: #C77A1F; }
.rrow__ico--t5 { background: #E6F4EA; color: #2F8E4F; }
.rrow__ico--t6 { background: #ECEFF4; color: #5C6779; }

.rrow__main { min-width: 0; display: flex; flex-direction: column; gap: 4px; }
.rrow__top {
  display: flex; gap: 10px; flex-wrap: wrap;
  font-family: var(--font-mono); font-size: 11px; letter-spacing: 0.08em; text-transform: uppercase;
  color: var(--mute);
}
.rrow__type { color: var(--accent); }
.rrow__cat { color: var(--ink-soft); }
.rrow__course { color: var(--ink-soft); }
.rrow__title {
  font-size: 15px; font-weight: 500; color: var(--ink); margin: 0;
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
  letter-spacing: -0.005em;
}
.rrow__meta {
  font-family: var(--font-mono); font-size: 11.5px; color: var(--mute);
  display: flex; gap: 0; flex-wrap: wrap;
}

.rrow__stat { display: flex; gap: 12px; padding-right: 12px; }
.rrow__count {
  font-family: var(--font-mono); font-size: 11.5px; color: var(--mute);
  display: inline-flex; align-items: center; gap: 4px;
}

.pager { display: flex; justify-content: center; margin-top: var(--s-9); }
.pager :deep(.el-pager li), .pager :deep(.btn-prev), .pager :deep(.btn-next) { background: var(--surface) !important; color: var(--ink) !important; font-family: var(--font-mono) !important; }
.pager :deep(.el-pager li.is-active) { background: var(--ink) !important; color: #fff !important; }

@media (max-width: 720px) {
  .rrow { grid-template-columns: 32px 36px minmax(0, 1fr) auto; padding: 14px 14px; }
  .rrow__stat { display: none; }
  .rrow__ico { width: 36px; height: 36px; }
}
</style>
