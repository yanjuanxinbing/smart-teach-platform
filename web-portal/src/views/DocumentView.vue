<template>
  <section class="doc">
    <header class="doc__head">
      <span class="eyebrow"><span class="eyebrow__rule"></span>Documents</span>
      <h2 class="doc__title">我的<em>文档</em></h2>
      <p class="doc__lead">集中查看管理员在 <code>localhost:8081</code> 后台为你发布的资料、附件与文档。</p>
    </header>

    <!-- 加载 / 空 / 错误 三态 -->
    <div v-if="loading" class="state">
      <el-icon :size="28" class="is-loading"><Loading /></el-icon>
      <p>文档加载中…</p>
    </div>

    <div v-else-if="loadError" class="state">
      <el-icon :size="32"><Warning /></el-icon>
      <p>文档暂时无法加载,请稍后再试</p>
      <el-button size="small" plain @click="load">重新加载</el-button>
    </div>

    <div v-else-if="!docs.length" class="state state--empty">
      <el-icon :size="56"><DocumentRemove /></el-icon>
      <h3>暂无内容</h3>
      <p>管理员暂未在 <code>localhost:8081</code> 后台为你发布文档。</p>
      <p class="state__hint">文档就绪后会自动显示在此处,无需刷新页面。</p>
    </div>

    <!-- 文档列表 -->
    <ul v-else class="doc-list">
      <li
        v-for="(d, idx) in docs"
        :key="d.id"
        class="doc-item"
        :class="{ 'doc-item--active': current && current.id === d.id }"
        @click="open(d)"
      >
        <span class="doc-item__idx">{{ String(idx + 1).padStart(2, '0') }}</span>
        <el-icon class="doc-item__ico" :size="18">
          <component :is="iconOf(d.docType)" />
        </el-icon>
        <div class="doc-item__main">
          <div class="doc-item__title">{{ d.title || '未命名文档' }}</div>
          <p class="doc-item__meta">
            <span>{{ typeLabel(d.docType) }}</span>
            <span v-if="d.size">· {{ humanSize(d.size) }}</span>
            <span v-if="d.uploaderName">· {{ d.uploaderName }}</span>
            <span>· {{ fmtDate(d.createdAt || d.updatedAt) }}</span>
          </p>
        </div>
        <el-button size="small" plain @click.stop="download(d)">下载</el-button>
      </li>
    </ul>

    <!-- 选中后的详情面板(暂用静态 mock 渲染) -->
    <article v-if="current && !loading && !loadError" class="viewer">
      <header class="viewer__head">
        <span class="eyebrow"><span class="eyebrow__rule"></span>Preview</span>
        <h3 class="viewer__title">{{ current.title }}</h3>
        <p class="viewer__meta">
          <span>{{ typeLabel(current.docType) }}</span>
          <span v-if="current.size">· {{ humanSize(current.size) }}</span>
          <span v-if="current.updatedAt">· 更新于 {{ fmtDate(current.updatedAt) }}</span>
        </p>
      </header>
      <div class="viewer__body">
        <!--
          TODO: [document 渲染] [P1]
          当前为占位文案。
          后端就绪后:
            - pdf  -> <iframe :src="current.url" /> 或 pdf.js 渲染
            - doc/docx -> mammoth.js / docx-preview
            - image -> <img :src="current.url" />
            - link  -> 跳外链
            - 其他  -> 提示"暂不支持预览,请下载"
        -->
        <div class="placeholder">
          <el-icon :size="48"><Document /></el-icon>
          <p class="placeholder__t">文档预览待接入</p>
          <p class="placeholder__d">
            当前为前端占位。后端 <code>GET /api/document/{id}</code> 接口就绪后,
            将按 <code>docType</code> 渲染对应预览器( PDF / DOCX / 图片 / 外链 )。
          </p>
        </div>
      </div>
    </article>
  </section>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Document, DocumentRemove, Picture, Link, Loading, Warning, Download
} from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { listMyDocuments, getDocument, downloadDocument } from '@/api/document'

const router = useRouter()
const userStore = useUserStore()

// ============================================================
// 文档状态
// ============================================================
const loading = ref(true)
const loadError = ref(false)
const docs = ref([])
const current = ref(null)

// ============================================================
// 图标 / 文案映射
// ============================================================
const ICON_MAP = {
  pdf: Document,
  doc: Document,
  docx: Document,
  image: Picture,
  png: Picture,
  jpg: Picture,
  jpeg: Picture,
  link: Link,
  other: Document
}
const TYPE_LABEL = {
  pdf: 'PDF', doc: 'DOC', docx: 'DOCX',
  image: '图片', png: '图片', jpg: '图片', jpeg: '图片',
  link: '外链', other: '其他'
}
const iconOf = (t) => ICON_MAP[t] || ICON_MAP.other
const typeLabel = (t) => TYPE_LABEL[t] || TYPE_LABEL.other

// ============================================================
// 工具
// ============================================================
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
  return `${(n / 1024 / 1024).toFixed(2)} MB`
}

// ============================================================
// 加载 —— 接口就绪时拉真数据,失败优雅降级到占位
// ============================================================
//
// TODO: [document 接口契约] [P1]
//   当前 GET /api/document/me 还没实现,document.js 已用 silentError:true
//   让失败静默,UI 直接走"暂无内容"分支;
//   后端补完后,这里会自动拉到数据。
//
const MOCK_DOC = {
  // 让 UI 看上去"有内容",方便联调;真接口就绪后整段会被覆盖。
  id: 'mock-1',
  title: '示例文档 · 学生学习手册',
  docType: 'pdf',
  size: 0,
  uploaderName: '管理员(占位)',
  createdAt: new Date().toISOString(),
  updatedAt: new Date().toISOString()
}

const load = async () => {
  loading.value = true
  loadError.value = false
  current.value = null
  try {
    // 静默调用 —— 后端未实现不会触发红色 toast
    const res = await listMyDocuments({ current: 1, size: 50 })
    const records = res?.records || res?.list || res?.items || []
    if (Array.isArray(records) && records.length) {
      docs.value = records
      current.value = records[0]
    } else {
      // 接口返回空数组 或 接口失败被 silentError 吞掉
      // → 用 mock 占位,保证 UI 可视
      docs.value = [MOCK_DOC]
      current.value = MOCK_DOC
    }
  } catch (e) {
    // 理论不会到这里(silentError),但兜底
    loadError.value = true
    docs.value = []
  } finally {
    loading.value = false
  }
}

// ============================================================
// 选中 / 下载
// ============================================================
const open = async (d) => {
  if (!d) return
  current.value = d
  // 真实接口就绪后,可以在这里再拉一次详情拿到 url:
  //   const detail = await getDocument(d.id)
  //   current.value = { ...d, ...detail }
  // TODO: [document 详情] [P1] 后端补 /api/document/{id} 后,改成上面那行;
  //   当前 list 接口返回的字段已够展示,无需额外请求。
}

const download = async (d) => {
  if (!d) return
  // 真接口就绪:
  //   const blob = await downloadDocument(d.id)
  //   const url = URL.createObjectURL(blob)
  //   const a = document.createElement('a')
  //   a.href = url; a.download = d.title || 'document'; a.click()
  //   URL.revokeObjectURL(url)
  // TODO: [document 下载] [P1] 取消上面这段注释即可启用;
  //   当前接口未就绪,提示用户去 8081 后台。
  ElMessage.info('下载接口尚未接入,请联系管理员在 8081 后台上传后查看')
}

// ============================================================
// 进入页面:登录态校验 + 数据加载
// ============================================================
onMounted(() => {
  if (!userStore.isLogin) {
    router.replace({ path: '/login', query: { redirect: '/profile/document' } })
    return
  }
  load()
})
</script>

<style scoped>
.doc { padding-bottom: var(--s-9); }
.doc__head { margin-bottom: var(--s-6); }
.eyebrow { display: inline-flex; align-items: center; gap: 8px; font-family: var(--font-mono); font-size: 11px; letter-spacing: 0.18em; text-transform: uppercase; color: var(--mute); }
.eyebrow__rule { display: inline-block; width: 26px; height: 1px; background: var(--accent); }
.doc__title { font-family: var(--font-display); font-size: clamp(26px, 3vw, 36px); font-weight: 600; color: var(--ink); margin: 6px 0 6px; letter-spacing: -0.018em; }
.doc__title em { font-family: var(--font-serif); font-style: italic; font-weight: 500; color: var(--accent); }
.doc__lead { color: var(--ink-soft); margin: 0; font-size: 14px; }
.doc__lead code { font-family: var(--font-mono); font-size: 12px; padding: 2px 6px; background: var(--surface); border: 1px solid var(--line); color: var(--accent); }

/* ---- 状态 ---- */
.state { padding: clamp(60px, 10vw, 120px) 0; text-align: center; color: var(--mute); display: flex; flex-direction: column; align-items: center; gap: 12px; background: var(--surface); border: 1px dashed var(--line); }
.state--empty h3 { margin: 0; font-family: var(--font-display); font-size: 18px; color: var(--ink); font-weight: 600; }
.state--empty p { margin: 0; font-size: 13px; }
.state--empty code { font-family: var(--font-mono); font-size: 12px; padding: 2px 6px; background: var(--surface); border: 1px solid var(--line); color: var(--accent); }
.state__hint { font-size: 11.5px; color: var(--mute); font-style: italic; }

/* ---- 列表 ---- */
.doc-list { list-style: none; margin: 0 0 var(--s-6); padding: 0; background: var(--surface); border: 1px solid var(--line); }
.doc-item {
  display: grid; grid-template-columns: 40px 24px 1fr auto;
  align-items: center; gap: var(--s-3);
  padding: 16px 20px; cursor: pointer;
  border-bottom: 1px solid var(--line-soft);
  transition: background-color var(--t-fast) var(--ease);
}
.doc-item:last-child { border-bottom: none; }
.doc-item:hover { background: var(--surface-soft); }
.doc-item--active { background: var(--ink); color: #fff; }
.doc-item--active .doc-item__meta { color: rgba(255,255,255,0.7); }
.doc-item__idx { font-family: var(--font-mono); font-size: 11px; color: var(--mute); }
.doc-item--active .doc-item__idx { color: rgba(255,255,255,0.5); }
.doc-item__ico { color: var(--accent); }
.doc-item--active .doc-item__ico { color: var(--accent); }
.doc-item__main { display: flex; flex-direction: column; gap: 4px; min-width: 0; }
.doc-item__title { font-size: 14px; color: var(--ink); font-weight: 500; }
.doc-item--active .doc-item__title { color: #fff; }
.doc-item__meta { margin: 0; font-family: var(--font-mono); font-size: 11.5px; color: var(--mute); letter-spacing: 0.04em; }

/* ---- 预览面板 ---- */
.viewer { background: var(--surface); border: 1px solid var(--line); }
.viewer__head { padding: clamp(20px, 2.4vw, 28px); border-bottom: 1px solid var(--line-soft); }
.viewer__title { margin: 6px 0 6px; font-family: var(--font-display); font-size: 22px; font-weight: 600; color: var(--ink); letter-spacing: -0.012em; }
.viewer__meta { margin: 0; font-family: var(--font-mono); font-size: 12px; color: var(--mute); display: flex; gap: 10px; flex-wrap: wrap; }
.viewer__body { padding: clamp(20px, 2.4vw, 28px); min-height: 240px; display: flex; align-items: center; justify-content: center; }

.placeholder { display: flex; flex-direction: column; align-items: center; gap: 10px; color: var(--mute); text-align: center; max-width: 480px; }
.placeholder__t { margin: 0; font-family: var(--font-display); font-size: 16px; font-weight: 600; color: var(--ink); }
.placeholder__d { margin: 0; font-size: 13px; line-height: 1.85; }
.placeholder code { font-family: var(--font-mono); font-size: 12px; padding: 2px 6px; background: var(--surface-soft); border: 1px solid var(--line); color: var(--accent); }
</style>