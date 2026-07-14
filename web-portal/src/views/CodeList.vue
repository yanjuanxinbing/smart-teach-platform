<template>
  <div class="page">
    <header class="page__header">
      <div class="container">
        <div class="rubric page__rubric">
          <span>Section · 03 / Code Library</span>
          <span class="rubric__dot"></span>
          <span>{{ total }} snippets</span>
          <span class="rubric__dot"></span>
          <span>{{ activeLangLabel }}</span>
        </div>
        <h1 class="page__title">笔记库</h1>
        <p class="page__lead">
          笔记、模板——按语言和主题筛选，可一键复制或查阅上下文。
        </p>
      </div>
    </header>

    <section class="page__body">
      <div class="container">
        <header class="toolbar">
          <div class="langs">
            <button
              v-for="l in langs"
              :key="l.value"
              class="lang"
              :class="{ 'lang--active': filters.lang === l.value }"
              @click="setLang(l.value)"
            >
              <span class="lang__dot" :style="{ background: l.color }"></span>
              {{ l.label }}
              <em>{{ countByLang[l.value] || 0 }}</em>
            </button>
          </div>
          <el-input v-model="filters.q" placeholder="按标题、标签、作者搜索" clearable class="search">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
        </header>

        <div v-if="loading" class="loading-tip">笔记加载中…</div>
        <div v-else-if="!list.length" class="empty"><el-icon :size="32"><DocumentCopy /></el-icon><p>暂无符合条件的代码片段</p></div>

        <div v-else class="cards">
          <article
            v-for="(c, idx) in list"
            :key="c.id"
            class="card"
            data-reveal
            :style="{ transitionDelay: `${Math.min(idx, 8) * 50}ms` }"
            @click="goDetail(c)"
          >
            <header class="card__head">
              <span class="lang-chip">
                <span class="lang-chip__dot" :style="{ background: langColor(c.language) }"></span>
                {{ c.language || 'text' }}
              </span>
              <span class="copy">
                <el-icon :size="11"><View /></el-icon>
                {{ c.views || 0 }}
              </span>
            </header>
            <h3 class="card__title">{{ c.title }}</h3>
            <pre class="card__preview"><code>{{ c.preview || c.code?.slice(0, 240) }}</code></pre>
            <footer class="card__foot">
              <span v-for="t in (c.tags || []).slice(0, 3)" :key="t" class="tag">#{{ t }}</span>
              <span class="author">@{{ c.author || '匿名' }}</span>
            </footer>
          </article>
        </div>

        <div class="pager">
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
    </section>
  </div>
</template>

<script setup>
import { ref, computed, reactive, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Search, DocumentCopy, View } from '@element-plus/icons-vue'
import { listCode } from '@/api/codex'

const router = useRouter()
const loading = ref(true)
const list = ref([])
const total = ref(0)
const page = reactive({ current: 1, size: 12 })

const langs = [
  { value: '',         label: '全部',  color: '#5F86FF' },
  { value: 'java',     label: 'Java',  color: '#B07219' },
  { value: 'python',   label: 'Python', color: '#3572A5' },
  { value: 'js',       label: 'JavaScript', color: '#F7C52B' },
  { value: 'ts',       label: 'TypeScript', color: '#2F74C0' },
  { value: 'sql',      label: 'SQL',   color: '#3B82A8' },
  { value: 'html',     label: 'HTML',  color: '#E34C26' },
  { value: 'css',      label: 'CSS',   color: '#563D7C' },
  { value: 'shell',    label: 'Shell', color: '#89E051' }
]
const filters = reactive({ q: '', lang: '' })
const activeLangLabel = computed(() => langs.find(l => l.value === filters.lang)?.label || '全部语言')
const langColor = (v) => langs.find(l => l.value === v)?.color || '#5F86FF'
const countByLang = computed(() => {
  const out = {}
  for (const c of list.value) out[c.language || ''] = (out[c.language || ''] || 0) + 1
  return out
})

const setLang = (v) => { filters.lang = v; page.current = 1; fetch() }
const goDetail = (c) => router.push(`/codex/${c.id}`)

const fetch = async () => {
  loading.value = true
  try {
    const res = await listCode({ current: page.current, size: page.size, q: filters.q, lang: filters.lang })
    list.value = res?.records || res?.list || []
    total.value = Number(res?.total || list.value.length)
  } catch (e) { list.value = []; total.value = 0 }
  finally { loading.value = false }
}

let timer
watch(() => filters.q, () => { clearTimeout(timer); timer = setTimeout(() => { page.current = 1; fetch() }, 300) })

onMounted(fetch)
</script>

<style scoped>
.page__header { padding: clamp(64px, 9vw, 112px) 0 clamp(32px, 5vw, 64px); border-bottom: 1px solid var(--line); background: linear-gradient(180deg, #FFFFFF 0%, #F4F7FF 100%); }
.page__rubric { margin-bottom: var(--s-6); }
.rubric__dot { display: inline-block; width: 4px; height: 4px; background: var(--mute-soft); border-radius: 50%; margin: 0 10px; vertical-align: middle; }
.page__title { font-family: var(--font-display); font-size: clamp(40px, 6vw, 72px); font-weight: 600; line-height: 1.04; letter-spacing: -0.025em; margin: 0 0 var(--s-4); }
.page__title em { font-family: var(--font-serif); font-style: italic; font-weight: 500; color: var(--accent); }
.page__lead { max-width: 56ch; font-size: 15.5px; line-height: 1.75; color: var(--ink-soft); margin: 0; }
.page__body { padding: clamp(48px, 7vw, 88px) 0; }

.toolbar { display: flex; align-items: center; justify-content: space-between; gap: var(--s-5); flex-wrap: wrap; padding-bottom: var(--s-4); margin-bottom: var(--s-6); border-bottom: 1px solid var(--line); }
.langs { display: flex; flex-wrap: wrap; gap: 6px; }
.lang {
  display: inline-flex; align-items: center; gap: 8px;
  font-family: var(--font-mono); font-size: 11px; letter-spacing: 0.12em; text-transform: uppercase;
  padding: 9px 12px; background: var(--surface); border: 1px solid var(--line); color: var(--ink-soft);
  cursor: pointer; transition: border-color var(--t-fast) var(--ease), color var(--t-fast) var(--ease);
}
.lang em { font-style: normal; color: var(--mute); }
.lang__dot { width: 8px; height: 8px; border-radius: 50%; }
.lang:hover { color: var(--ink); border-color: var(--ink); }
.lang:hover em { color: var(--ink); }
.lang--active { background: var(--ink); color: #fff; border-color: var(--ink); }
.lang--active em { color: var(--accent); }
.search { width: 260px; }
.search :deep(.el-input__wrapper) { border-radius: 0; }

.loading-tip { color: var(--mute); font-size: 13px; letter-spacing: 0.06em; text-align: center; padding: 64px 0; }
.empty { padding: 88px 0; text-align: center; color: var(--mute); display: flex; flex-direction: column; align-items: center; gap: 14px; }

.cards {
  display: grid; grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: clamp(20px, 2.6vw, 32px);
}
.card {
  background: var(--surface); border: 1px solid var(--line);
  padding: 22px; display: flex; flex-direction: column; gap: 12px;
  cursor: pointer; overflow: hidden;
  transition: border-color var(--t-base) var(--ease), box-shadow var(--t-base) var(--ease), transform var(--t-base) var(--ease);
}
.card:hover { border-color: var(--accent); box-shadow: var(--shadow-blue); transform: translateY(-3px); }

.card__head { display: flex; justify-content: space-between; align-items: center; }
.lang-chip {
  display: inline-flex; align-items: center; gap: 6px;
  font-family: var(--font-mono); font-size: 11px; letter-spacing: 0.08em; text-transform: uppercase;
  padding: 4px 8px; background: var(--surface-cool); color: var(--ink-soft);
}
.lang-chip__dot { width: 8px; height: 8px; border-radius: 50%; }
.copy { display: inline-flex; align-items: center; gap: 4px; font-family: var(--font-mono); font-size: 11px; color: var(--mute); }

.card__title { font-family: var(--font-display); font-size: 20px; font-weight: 600; color: var(--ink); margin: 0; letter-spacing: -0.01em; transition: color var(--t-fast) var(--ease); }
.card:hover .card__title { color: var(--accent); }

.card__preview {
  margin: 0; padding: 14px 16px;
  font-family: var(--font-mono); font-size: 13px; line-height: 1.65;
  color: var(--ink-soft);
  background: var(--surface-cool);
  border: 1px solid var(--line);
  max-height: 144px; overflow: hidden;
  white-space: pre-wrap;
  position: relative;
}
.card__preview::after {
  content: ''; position: absolute; left: 0; right: 0; bottom: 0; height: 36px;
  background: linear-gradient(to bottom, transparent, var(--surface-cool));
  pointer-events: none;
}
.card__preview code { font-family: inherit; }

.card__foot { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; padding-top: 8px; border-top: 1px solid var(--line-soft); margin-top: auto; }
.tag { font-family: var(--font-mono); font-size: 11px; color: var(--accent); padding: 2px 6px; border: 1px solid var(--accent-tint); background: var(--brand-100); }
.author { margin-left: auto; font-family: var(--font-mono); font-size: 11px; color: var(--mute); }

.pager { display: flex; justify-content: center; margin-top: var(--s-9); }
.pager :deep(.el-pager li), .pager :deep(.btn-prev), .pager :deep(.btn-next) { background: var(--surface) !important; color: var(--ink) !important; font-family: var(--font-mono) !important; }
.pager :deep(.el-pager li.is-active) { background: var(--ink) !important; color: #fff !important; }

@media (max-width: 820px) { .cards { grid-template-columns: 1fr; } .search { width: 100%; } }
</style>
