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
        <el-button
          size="small"
          plain
          :disabled="!hasAttachment(d)"
          @click.stop="download(d)"
        >
          下载
          <el-tooltip v-if="!hasAttachment(d)" content="暂无附件" placement="top">
            <el-icon style="margin-left: 4px"><Warning /></el-icon>
          </el-tooltip>
        </el-button>
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
          <span v-if="current.updatedAt || current.createdAt">· 更新于 {{ fmtDate(current.updatedAt || current.createdAt) }}</span>
        </p>
      </header>
      <div class="viewer__body">
        <!--
          文档渲染 —— 按 docType 分发：
            - pdf     -> <iframe :src="current.url" /> 或 pdf.js（占位）
            - doc/docx-> mammoth.js / docx-preview（占位）
            - image   -> <img :src="current.url" />
            - link    -> 外链跳转 / 内嵌
            - rich    -> v-html 渲染后端返回的富文本 HTML
            - 其他    -> 提示"暂不支持预览，请下载"
          后端接口契约（待落表）：
            GET /api/document/{id} -> PortalDocumentVO {
              id, title, summary, docType, url, content(HTML),
              size, uploaderId, uploaderName, createdAt, updatedAt
            }
        -->
        <div v-if="renderState === 'pdf'" class="viewer__pdf">
          <iframe v-if="current.url" :src="current.url" frameborder="0" class="viewer__iframe" />
          <div v-else class="placeholder">
            <el-icon :size="48"><Document /></el-icon>
            <p class="placeholder__t">PDF 渲染待接入</p>
            <p class="placeholder__d">后端 <code>GET /api/document/{id}</code> 返回 url 后可内嵌 iframe。</p>
          </div>
        </div>

        <div v-else-if="renderState === 'image'" class="viewer__image">
          <img v-if="current.url" :src="current.url" :alt="current.title" class="viewer__img" />
          <div v-else class="placeholder">
            <el-icon :size="48"><Picture /></el-icon>
            <p class="placeholder__t">图片渲染待接入</p>
            <p class="placeholder__d">后端返回 url 后即可展示。</p>
          </div>
        </div>

        <div v-else-if="renderState === 'link'" class="viewer__link">
          <p>
            <el-icon :size="18"><Link /></el-icon>
            <a v-if="current.url" :href="current.url" target="_blank" rel="noopener">
              {{ current.url }}
            </a>
            <span v-else>外链地址待后端下发</span>
          </p>
        </div>

        <div v-else-if="renderState === 'rich'" class="viewer__rich">
          <!--
            富文本内容渲染 —— 后端 PortalDocumentVO.content 是 HTML 字符串。
            后端应负责 XSS 过滤，前端 v-html 仅做展示。
          -->
          <div v-if="current.content" class="rich-body" v-html="current.content"></div>
          <div v-else class="placeholder">
            <el-icon :size="48"><Document /></el-icon>
            <p class="placeholder__t">文档待编辑</p>
            <p class="placeholder__d">后端 <code>GET /api/document/{id}</code> 暂未返回内容,请联系管理员在 8081 后台补全。</p>
          </div>
        </div>

        <div v-else class="placeholder">
          <el-icon :size="48"><Document /></el-icon>
          <p class="placeholder__t">文档待编辑</p>
          <p class="placeholder__d">后端 <code>GET /api/document/{id}</code> 暂未返回内容,请联系管理员在 8081 后台补全。</p>
        </div>
      </div>
    </article>
  </section>
</template>

<script setup>
// 此文档内容由 8081 管理后台编辑，8082 前端仅做展示
import { ref, computed, onMounted } from 'vue'
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
  // 富文本 / 在线文档
  rich: Document,
  html: Document,
  other: Document
}
const TYPE_LABEL = {
  pdf: 'PDF', doc: 'DOC', docx: 'DOCX',
  image: '图片', png: '图片', jpg: '图片', jpeg: '图片',
  link: '外链', rich: '富文本', html: '富文本',
  other: '其他'
}
const iconOf = (t) => ICON_MAP[t] || ICON_MAP.other
const typeLabel = (t) => TYPE_LABEL[t] || TYPE_LABEL.other

// ============================================================
// 渲染分支：根据 docType 决定 viewer 渲染何种内容
//   pdf  -> iframe 内嵌
//   image-> <img> 直接展示
//   link -> 外链跳转
//   rich -> v-html 渲染后端 content(HTML)
//   兜底:若 content 存在则按富文本,否则提示"待接入"
// ============================================================
const renderState = computed(() => {
  const t = (current.value?.docType || '').toLowerCase()
  if (['pdf'].includes(t)) return 'pdf'
  if (['image', 'png', 'jpg', 'jpeg', 'gif', 'webp'].includes(t)) return 'image'
  if (['link'].includes(t)) return 'link'
  if (['rich', 'html'].includes(t)) return 'rich'
  // 兜底：content 字段存在则按富文本渲染
  if (current.value?.content) return 'rich'
  return 'placeholder'
})

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
//   8081 后台尚未提供 /document/me 时,silentError 会吞掉错误,
//   UI 自然落到「暂无内容」空态卡片,不报错、不白屏
// ============================================================
const load = async () => {
  loading.value = true
  loadError.value = false
  current.value = null
  try {
    const res = await listMyDocuments({ current: 1, size: 50 })
    const records = res?.records || res?.list || res?.items || []
    docs.value = Array.isArray(records) ? records : []
    // 默认选中第一条(若有),并触发 open() 拉详情
    if (docs.value.length) open(docs.value[0])
  } catch (e) {
    loadError.value = true
    docs.value = []
  } finally {
    loading.value = false
  }
}

// ============================================================
// 选中 / 下载
// ============================================================
const detailLoading = ref(false)
const detailError = ref(false)

const open = async (d) => {
  if (!d) return
  current.value = d
  // 后端接口未上线时静默吞错,UI 走「文档待编辑」占位
  if (!d.id || d.id === 'mock') return
  detailLoading.value = true
  detailError.value = false
  try {
    const detail = await getDocument(d.id)
    // 仍选中同一篇文档时才合并 —— 避免用户切换文档后写入过期详情
    if (detail && current.value && current.value.id === d.id) {
      current.value = { ...current.value, ...detail }
    }
  } catch (e) {
    if (current.value && current.value.id === d.id) detailError.value = true
  } finally {
    detailLoading.value = false
  }
}

// 触发浏览器 Blob 下载 —— 把 blob 转临时 <a> 标签下载,后释放 URL 防内存泄漏
const triggerBlobDownload = (blob, filename) => {
  const url = window.URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename || 'document'
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  // 释放 Blob URL —— 防止内存泄漏
  setTimeout(() => window.URL.revokeObjectURL(url), 100)
}

// 文档是否带可下载资源 —— 多种字段名都算,后端字段未定型时保持兼容
const hasAttachment = (d) => !!(d?.fileUrl || d?.attachment || d?.url)

const download = async (d) => {
  if (!d || !hasAttachment(d)) {
    ElMessage.warning('暂无附件')
    return
  }
  try {
    const blob = await downloadDocument(d.id)
    triggerBlobDownload(blob, d.title || 'document')
  } catch (e) {
    // /download 接口未就绪时回退到直链下载；直链也不可用时再提示
    const directUrl = d.fileUrl || d.attachment || d.url
    if (directUrl) {
      const link = document.createElement('a')
      link.href = directUrl
      link.download = d.title || 'document'
      link.target = '_blank'
      link.rel = 'noopener'
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
    } else {
      console.warn('功能开发中:文档下载接口未就绪', e?.message)
      ElMessage.warning('下载功能开发中,请稍后再试')
    }
  }
}

// ============================================================
// 进入页面:登录态校验 + 数据加载
// ============================================================
onMounted(() => {
  if (!userStore.isLogin) {
    router.replace({ path: '/login', query: { redirect: '/profile' } })
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
.viewer__body { padding: clamp(20px, 2.4vw, 28px); min-height: 240px; display: flex; align-items: flex-start; justify-content: center; }
.viewer__body > .placeholder { margin-top: 80px; }

.placeholder { display: flex; flex-direction: column; align-items: center; gap: 10px; color: var(--mute); text-align: center; max-width: 480px; }
.placeholder__t { margin: 0; font-family: var(--font-display); font-size: 16px; font-weight: 600; color: var(--ink); }
.placeholder__d { margin: 0; font-size: 13px; line-height: 1.85; }
.placeholder code { font-family: var(--font-mono); font-size: 12px; padding: 2px 6px; background: var(--surface-soft); border: 1px solid var(--line); color: var(--accent); }

/* ---- 不同渲染分支的容器 ---- */
.viewer__body > * { width: 100%; max-width: 880px; margin: 0 auto; }

/* PDF iframe */
.viewer__pdf { width: 100%; height: 70vh; min-height: 480px; }
.viewer__iframe { width: 100%; height: 100%; border: 1px solid var(--line-soft); }

/* 图片 */
.viewer__image { width: 100%; text-align: center; }
.viewer__img { max-width: 100%; max-height: 70vh; border: 1px solid var(--line-soft); }

/* 外链 */
.viewer__link { padding: 20px; background: var(--surface-soft); border: 1px solid var(--line-soft); display: flex; align-items: center; gap: 10px; }
.viewer__link a { color: var(--accent); word-break: break-all; }

/* 富文本渲染 */
.viewer__rich { padding: 4px 12px 24px; background: var(--surface); border: 1px solid var(--line-soft); }
.rich-body { font-family: var(--font-body); font-size: 14.5px; line-height: 1.85; color: var(--ink); word-break: break-word; }
.rich-body :deep(h1), .rich-body :deep(h2), .rich-body :deep(h3) {
  font-family: var(--font-display); color: var(--ink); margin: 24px 0 12px; line-height: 1.4;
}
.rich-body :deep(h1) { font-size: 22px; }
.rich-body :deep(h2) { font-size: 18px; }
.rich-body :deep(h3) { font-size: 16px; }
.rich-body :deep(p) { margin: 0 0 12px; }
.rich-body :deep(strong) { color: var(--ink); font-weight: 600; }
.rich-body :deep(em) { color: var(--accent); font-style: italic; }
.rich-body :deep(a) { color: var(--accent); text-decoration: underline; }
.rich-body :deep(ul), .rich-body :deep(ol) { padding-left: 22px; margin: 8px 0 16px; }
.rich-body :deep(li) { margin: 4px 0; }
.rich-body :deep(code) {
  background: var(--surface-soft); padding: 1px 6px; border: 1px solid var(--line-soft);
  font-family: var(--font-mono); font-size: 12.5px; color: var(--accent);
}
.rich-body :deep(pre) {
  background: #1f2329; color: #f1f2f3; padding: 14px 16px; overflow-x: auto; margin: 12px 0;
  font-family: var(--font-mono); font-size: 12.5px; line-height: 1.6;
}
.rich-body :deep(blockquote) {
  margin: 12px 0; padding: 8px 16px; border-left: 3px solid var(--accent); color: var(--ink-soft);
  background: var(--surface-soft);
}
.rich-body :deep(img) { max-width: 100%; height: auto; }
.rich-body :deep(table) { border-collapse: collapse; width: 100%; margin: 12px 0; }
.rich-body :deep(th), .rich-body :deep(td) { border: 1px solid var(--line); padding: 6px 10px; }

/* 当 viewer__body 渲染富文本时，让 placeholder 容器不再左右居中 */
.viewer__rich .placeholder { margin: 40px auto; }
</style>