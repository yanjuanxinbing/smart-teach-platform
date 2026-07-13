<template>
  <div class="page">
    <header class="page__header">
      <div class="container">
        <router-link to="/codex" class="back">
          <el-icon :size="13"><Back /></el-icon>
          <span>返回代码库</span>
        </router-link>
        <div class="head">
          <span class="lang-chip">
            <span class="lang-chip__dot" :style="{ background: langColor }"></span>
            {{ snippet?.language || 'text' }}
          </span>
          <h1 class="title">{{ snippet?.title || '代码片段' }}</h1>
          <div class="meta">
            <span><el-icon :size="12"><View /></el-icon>{{ snippet?.views || 0 }} 次浏览</span>
            <span><el-icon :size="12"><Calendar /></el-icon>{{ snippet?.createTime || '—' }}</span>
            <span><el-icon :size="12"><User /></el-icon>@{{ snippet?.author || '匿名' }}</span>
          </div>
        </div>
      </div>
    </header>

    <section class="page__body">
      <div class="container two">
        <article class="code">
          <header class="code__bar">
            <div class="code__dots" aria-hidden="true"><span></span><span></span><span></span></div>
            <span class="code__file">{{ snippet?.filename || 'snippet.txt' }}</span>
            <el-button size="small" :icon="DocumentCopy" @click="onCopy" :loading="copying" plain class="code__copy">复制</el-button>
          </header>
          <pre class="code__pre"><code>{{ snippet?.code || '' }}</code></pre>
          <p v-if="snippet?.description" class="code__desc">{{ snippet.description }}</p>
        </article>

        <aside class="side">
          <section class="card">
            <header class="card__head">
              <span class="eyebrow"><span class="eyebrow__rule"></span>代码信息</span>
              <h3 class="card__title">关于这段<em>代码</em></h3>
            </header>
            <ul class="kv">
              <li><span>语言</span><b>{{ snippet?.language || '—' }}</b></li>
              <li><span>行数</span><b>{{ lineCount }} 行</b></li>
              <li><span>字数</span><b>{{ snippet?.code?.length || 0 }}</b></li>
              <li><span>作者</span><b>{{ snippet?.author || '匿名' }}</b></li>
              <li><span>许可</span><b>{{ snippet?.license || 'MIT' }}</b></li>
              <li><span>更新</span><b>{{ snippet?.updateTime || '—' }}</b></li>
            </ul>
          </section>

          <section class="card">
            <header class="card__head">
              <span class="eyebrow"><span class="eyebrow__rule"></span>标签</span>
              <h3 class="card__title">相关<em>主题</em></h3>
            </header>
            <div class="tags">
              <span v-for="t in snippet?.tags || []" :key="t" class="tag">#{{ t }}</span>
              <span v-if="!snippet?.tags?.length" class="muted">暂无标签</span>
            </div>
          </section>

          <section v-if="related.length" class="card">
            <header class="card__head">
              <span class="eyebrow"><span class="eyebrow__rule"></span>相关片段</span>
              <h3 class="card__title">也许<em>你会用</em>到</h3>
            </header>
            <ul class="related">
              <li v-for="r in related" :key="r.id" @click="goRelated(r)">
                <span class="dot" :style="{ background: langColorOf(r.language) }"></span>
                <div class="rel__body">
                  <span class="rel__title">{{ r.title }}</span>
                  <span class="rel__meta">{{ r.language }} · {{ r.views || 0 }} 浏览</span>
                </div>
              </li>
            </ul>
          </section>
        </aside>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Back, View, Calendar, User, DocumentCopy } from '@element-plus/icons-vue'
import { codeDetail, listCode } from '@/api/codex'

const route = useRoute()
const router = useRouter()
const snippet = ref(null)
const related = ref([])
const copying = ref(false)

const langColorMap = {
  java: '#B07219', python: '#3572A5', js: '#F7C52B', ts: '#2F74C0',
  sql: '#3B82A8', html: '#E34C26', css: '#563D7C', shell: '#89E051'
}
const langColor = computed(() => langColorMap[snippet.value?.language] || '#5F86FF')
const langColorOf = (v) => langColorMap[v] || '#5F86FF'

const lineCount = computed(() => (snippet.value?.code?.match(/\n/g)?.length ?? 0) + 1)

const onCopy = async () => {
  copying.value = true
  try {
    await navigator.clipboard.writeText(snippet.value?.code || '')
    ElMessage.success('代码已复制到剪贴板')
  } catch (e) {
    ElMessage.error('复制失败，请手动选择')
  } finally { copying.value = false }
}

const goRelated = (r) => router.push(`/codex/${r.id}`)

const load = async (id) => {
  try {
    snippet.value = await codeDetail(id)
  } catch (e) {
    snippet.value = {
      id, title: '代码片段',
      language: 'text',
      code: '/* 该片段暂无内容 */',
      author: '匿名', views: 0, license: 'MIT', tags: [], createTime: '—'
    }
  }
  try {
    const list = await listCode({ page: 1, size: 4, lang: snippet.value?.language })
    related.value = (list?.records || list?.list || []).filter(c => c.id !== snippet.value?.id).slice(0, 4)
  } catch (e) { related.value = [] }
}

onMounted(() => load(route.params.id))
watch(() => route.params.id, (id) => load(id))
</script>

<style scoped>
.page__header { padding: clamp(48px, 7vw, 96px) 0 clamp(36px, 5vw, 64px); border-bottom: 1px solid var(--line); background: linear-gradient(180deg, #fff 0%, #F4F7FF 100%); }
.back { display: inline-flex; align-items: center; gap: 6px; font-family: var(--font-mono); font-size: 11px; letter-spacing: 0.18em; text-transform: uppercase; color: var(--mute); margin-bottom: var(--s-6); }
.back:hover { color: var(--accent); }
.head { display: flex; flex-direction: column; gap: 14px; }
.lang-chip { display: inline-flex; align-items: center; gap: 6px; align-self: flex-start; font-family: var(--font-mono); font-size: 11px; letter-spacing: 0.08em; text-transform: uppercase; padding: 5px 10px; background: var(--surface); border: 1px solid var(--line); color: var(--ink-soft); }
.lang-chip__dot { width: 9px; height: 9px; border-radius: 50%; }
.title { font-family: var(--font-display); font-size: clamp(30px, 4vw, 48px); font-weight: 600; color: var(--ink); margin: 0; letter-spacing: -0.02em; line-height: 1.15; }
.meta { display: flex; gap: 18px; flex-wrap: wrap; font-family: var(--font-mono); font-size: 12px; color: var(--mute); }
.meta span { display: inline-flex; align-items: center; gap: 5px; }
.page__body { padding: clamp(48px, 7vw, 88px) 0; }

.two { display: grid; grid-template-columns: minmax(0, 1.7fr) minmax(0, 1fr); gap: clamp(20px, 3vw, 36px); align-items: start; }

.code { background: #13192C; border: 1px solid var(--surface-deep); color: #E8EDF7; }
.code__bar {
  display: flex; align-items: center; gap: 14px;
  padding: 12px 16px; border-bottom: 1px solid #1B2440;
  background: linear-gradient(180deg, #13192C, #0A0E1A);
}
.code__dots { display: inline-flex; gap: 6px; }
.code__dots span { width: 9px; height: 9px; border-radius: 50%; background: #2F3E66; }
.code__dots span:nth-child(1) { background: #5F86FF; }
.code__dots span:nth-child(2) { background: #A9C2FF; }
.code__dots span:nth-child(3) { background: #2F3E66; }
.code__file { font-family: var(--font-mono); font-size: 12px; color: #98A2BC; }
.code__copy { margin-left: auto; }
.code__copy :deep(.el-button) { background: rgba(95,134,255,0.16); color: #fff; border-color: rgba(95,134,255,0.4); border-radius: 0; }
.code__copy :deep(.el-button):hover { background: rgba(95,134,255,0.28); }
.code__pre { margin: 0; padding: clamp(16px, 2.4vw, 28px); font-family: var(--font-mono); font-size: 13.5px; line-height: 1.75; color: #E8EDF7; white-space: pre-wrap; word-break: break-word; max-height: 60vh; overflow: auto; }
.code__pre code { font-family: inherit; }
.code__desc { margin: 0; padding: 16px clamp(16px, 2.4vw, 28px); border-top: 1px solid #1B2440; color: #C0C8DD; font-size: 13.5px; line-height: 1.7; }

.side .card { background: var(--surface); border: 1px solid var(--line); padding: 22px; margin-bottom: var(--s-5); transition: border-color var(--t-base) var(--ease); }
.side .card:hover { border-color: var(--accent); }
.card__head { margin-bottom: var(--s-4); }
.card__title { font-family: var(--font-display); font-size: 18px; font-weight: 600; color: var(--ink); margin: 4px 0 0; letter-spacing: -0.012em; }
.card__title em { font-family: var(--font-serif); font-style: italic; font-weight: 500; color: var(--accent); }
.eyebrow { display: inline-flex; align-items: center; gap: 8px; font-family: var(--font-mono); font-size: 11px; letter-spacing: 0.18em; text-transform: uppercase; color: var(--mute); }
.eyebrow__rule { display: inline-block; width: 26px; height: 1px; background: var(--accent); }

.kv { list-style: none; padding: 0; margin: 0; }
.kv li { display: flex; justify-content: space-between; padding: 10px 0; border-bottom: 1px solid var(--line-soft); font-family: var(--font-mono); font-size: 12.5px; }
.kv li:last-child { border-bottom: none; }
.kv span { color: var(--mute); letter-spacing: 0.08em; text-transform: uppercase; font-size: 11px; }
.kv b { color: var(--ink); font-weight: 500; }

.tags { display: flex; flex-wrap: wrap; gap: 6px; }
.tag { font-family: var(--font-mono); font-size: 11px; color: var(--accent); padding: 3px 8px; border: 1px solid var(--accent-tint); background: var(--brand-100); }
.muted { color: var(--mute); font-size: 12px; }

.related { list-style: none; padding: 0; margin: 0; }
.related li { display: flex; gap: 10px; padding: 12px 0; border-bottom: 1px solid var(--line-soft); cursor: pointer; transition: color var(--t-fast) var(--ease); }
.related li:hover .rel__title { color: var(--accent); }
.related li:last-child { border-bottom: none; }
.dot { width: 10px; height: 10px; border-radius: 50%; margin-top: 6px; }
.rel__body { display: flex; flex-direction: column; gap: 4px; min-width: 0; }
.rel__title { font-family: var(--font-display); font-size: 14.5px; color: var(--ink); }
.rel__meta { font-family: var(--font-mono); font-size: 11px; color: var(--mute); }

@media (max-width: 1024px) { .two { grid-template-columns: 1fr; } }
</style>
