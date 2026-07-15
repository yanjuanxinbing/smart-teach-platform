<template>
  <div class="page">
    <header class="page__header">
      <div class="container">
        <div class="rubric page__rubric">
          <span>Section · 07 / AI Tutor</span>
          <span class="rubric__dot"></span>
          <span>本地 Ollama · gemma3:1b</span>
        </div>
        <h1 class="page__title">AI 解题<em>助手</em></h1>
        <p class="page__lead">
          由本地大模型逐字给出解答，仅供学习参考，不保证准确性。
        </p>
      </div>
    </header>

    <section class="page__body">
      <div class="container ai">
        <!-- 左侧：会话抽屉 -->
        <aside class="ai__sidebar">
          <button class="ai__newbtn" type="button" @click="newSession">
            <el-icon><Plus /></el-icon>
            新会话
          </button>
          <div v-if="!sessions.length" class="ai__empty">暂无历史会话</div>
          <ul v-else class="ai__sessions">
            <li
              v-for="s in sessions"
              :key="s.id"
              :class="['ai__session', currentSessionId === s.id && 'is-active']"
              @click="loadSession(s.id)"
            >
              <div class="ai__session-title">{{ s.title || '新会话' }}</div>
              <div class="ai__session-time">{{ formatTime(s.updateTime) }}</div>
              <el-icon class="ai__session-del" @click.stop="onRemoveSession(s)"><Close /></el-icon>
            </li>
          </ul>
        </aside>

        <!-- 主对话区 -->
        <main class="ai__main">
          <div ref="scroller" class="ai__list">
            <div v-if="!messages.length" class="ai__intro">
              <el-icon :size="48"><ChatLineSquare /></el-icon>
              <h3>开始你的提问</h3>
              <p>支持编程 / 数学 / 概念类问题，建议说明上下文与期望输出格式。</p>
            </div>

            <article
              v-for="(m, i) in messages"
              :key="i"
              :class="['ai__msg', 'ai__msg--' + m.role]"
            >
              <header class="ai__bubble-header">
                <strong>{{ m.role === 'user' ? '我' : 'AI' }}</strong>
                <small>{{ formatTime(m.createdAt) }}</small>
              </header>
              <pre class="ai__bubble">{{ m.content }}</pre>
              <button
                v-if="m.role === 'assistant' && m.content"
                class="ai__copy"
                type="button"
                @click="copy(m.content)"
              >
                <el-icon><CopyDocument /></el-icon>
                复制
              </button>
            </article>

            <div v-if="streaming" class="ai__typing">
              <span></span><span></span><span></span>
            </div>

            <div v-if="errorMsg" class="ai__error">{{ errorMsg }}</div>
          </div>

          <footer class="ai__composer">
            <el-input
              v-model="prompt"
              type="textarea"
              :autosize="{ minRows: 2, maxRows: 6 }"
              :maxlength="2000"
              show-word-limit
              placeholder="输入问题后按 Ctrl/⌘ + Enter 提交"
              @keydown.ctrl.enter="submit"
              @keydown.meta.enter="submit"
            />
            <div class="ai__composer-actions">
              <el-button v-if="streaming" text @click="abort">
                <el-icon><CircleClose /></el-icon>
                停止
              </el-button>
              <el-button type="primary" :loading="streaming" :disabled="!canSubmit" @click="submit">
                <el-icon><Promotion /></el-icon>
                提问
              </el-button>
            </div>
          </footer>
          <small class="ai__hint">AI 生成内容仅供参考，不保证准确性；遇到重要决策请咨询任课教师。</small>
        </main>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Plus, Close, ChatLineSquare, CopyDocument, Promotion, CircleClose
} from '@element-plus/icons-vue'
import {
  streamQuestion, listHistory, sessionMessages, removeSession
} from '@/api/aiassistant'
import dayjs from 'dayjs'

const scroller = ref(null)
const prompt = ref('')
const messages = ref([])      // { role, content, createdAt }
const sessions = ref([])      // [{id, title, updateTime}]
const currentSessionId = ref(null)
const streaming = ref(false)
const errorMsg = ref('')
let abortCtl = null

const canSubmit = computed(() => prompt.value.trim().length >= 2 && prompt.value.length <= 2000 && !streaming.value)

// 会话 + 历史加载
async function fetchSessions() {
  try {
    const res = await listHistory({ pageNum: 1, pageSize: 50 })
    const records = res?.records || res?.list || []
    sessions.value = records
  } catch (_) { /* 静默失败 */ }
}

async function loadSession(id) {
  if (!id) return
  errorMsg.value = ''
  streaming.value = false
  if (abortCtl) { abortCtl.abort(); abortCtl = null }
  currentSessionId.value = id
  messages.value = []
  try {
    const rows = await sessionMessages(id)
    messages.value = (rows || []).map(r => ({
      role: r.role,
      content: r.content || '',
      createdAt: r.createTime ? new Date(r.createTime).getTime() : Date.now()
    }))
    await nextTick(); scrollToBottom()
  } catch (e) {
    errorMsg.value = e?.message || '加载会话失败'
  }
}

function newSession() {
  if (streaming.value && abortCtl) { abortCtl.abort(); abortCtl = null }
  currentSessionId.value = null
  messages.value = []
  errorMsg.value = ''
}

async function onRemoveSession(s) {
  try {
    await removeSession(s.id)
    if (currentSessionId.value === s.id) newSession()
    sessions.value = sessions.value.filter(x => x.id !== s.id)
    ElMessage.success('已删除')
  } catch (e) {
    ElMessage.error(e?.message || '删除失败')
  }
}

// 提交
async function submit() {
  if (!canSubmit.value) return
  const text = prompt.value.trim()
  prompt.value = ''
  errorMsg.value = ''

  // 立刻把用户消息渲染出来
  const userMsg = { role: 'user', content: text, createdAt: Date.now() }
  messages.value.push(userMsg)
  // assistant 占位
  const placeholder = { role: 'assistant', content: '', createdAt: Date.now() }
  messages.value.push(placeholder)

  streaming.value = true
  abortCtl = new AbortController()

  try {
    await streamQuestion({
      prompt: text,
      sessionId: currentSessionId.value,
      signal: abortCtl.signal,
      onChunk: (payload) => {
        if (payload.sessionId) {
          currentSessionId.value = payload.sessionId
          // 把新会话刷到列表
          if (!sessions.value.find(x => x.id === payload.sessionId)) {
            fetchSessions()
          }
        }
        if (payload.delta) {
          placeholder.content += payload.delta
          nextTick(scrollToBottom)
        }
      }
    })
    // 流完成
    if (!placeholder.content) {
      placeholder.content = '[未返回任何内容]'
    }
    fetchSessions()
  } catch (e) {
    errorMsg.value = e?.message || '调用失败，请稍后重试'
    placeholder.content = placeholder.content || `[错误] ${errorMsg.value}`
  } finally {
    streaming.value = false
    abortCtl = null
    nextTick(scrollToBottom)
  }
}

function abort() {
  if (abortCtl) {
    abortCtl.abort()
    abortCtl = null
    streaming.value = false
    ElMessage.info('已停止当前回答')
  }
}

// 工具
function scrollToBottom() {
  const el = scroller.value
  if (el) el.scrollTop = el.scrollHeight
}
function formatTime(t) {
  if (!t) return ''
  return dayjs(t).format('YYYY-MM-DD HH:mm')
}
async function copy(text) {
  if (!text) return
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success('已复制到剪贴板')
  } catch (_) {
    ElMessage.error('复制失败，请手动选择')
  }
}

onMounted(() => {
  fetchSessions()
})
onBeforeUnmount(() => {
  if (abortCtl) abortCtl.abort()
})
</script>

<style scoped>
.page__header {
  padding: clamp(64px, 9vw, 112px) 0 clamp(32px, 5vw, 64px);
  border-bottom: 1px solid var(--line);
  background: linear-gradient(180deg, #FFFFFF 0%, #F4F7FF 100%);
}
.page__rubric { margin-bottom: var(--s-6); }
.rubric__dot { display: inline-block; width: 4px; height: 4px; background: var(--mute-soft); border-radius: 50%; margin: 0 10px; vertical-align: middle; }
.page__title { font-family: var(--font-display); font-size: clamp(40px, 6vw, 72px); font-weight: 600; line-height: 1.04; letter-spacing: -0.025em; margin: 0 0 var(--s-4); }
.page__title em { font-family: var(--font-serif); font-style: italic; font-weight: 500; color: var(--accent); }
.page__lead { max-width: 56ch; font-size: 15.5px; line-height: 1.75; color: var(--ink-soft); margin: 0; }
.page__body { padding: clamp(48px, 7vw, 88px) 0; }

.ai {
  display: grid;
  grid-template-columns: 260px 1fr;
  gap: 24px;
  min-height: 70vh;
}
@media (max-width: 880px) {
  .ai { grid-template-columns: 1fr; }
  .ai__sidebar { order: 2; }
  .ai__main { order: 1; }
}

/* ============== sidebar ============== */
.ai__sidebar {
  border: 1px solid var(--line);
  background: var(--surface);
  padding: 16px;
  align-self: start;
  position: sticky;
  top: 80px;
}
.ai__newbtn {
  display: inline-flex; align-items: center; gap: 6px;
  width: 100%; justify-content: center;
  font-family: var(--font-mono); font-size: 11px; letter-spacing: 0.16em; text-transform: uppercase;
  padding: 10px 14px; background: var(--ink); color: #fff;
  border: 1px solid var(--ink); cursor: pointer;
  transition: background-color var(--t-fast) var(--ease);
}
.ai__newbtn:hover { background: var(--accent); border-color: var(--accent); }
.ai__empty {
  padding: 24px 0; text-align: center;
  font-size: 12px; color: var(--mute); letter-spacing: 0.06em;
}
.ai__sessions { list-style: none; padding: 0; margin: 12px 0 0; max-height: calc(100vh - 220px); overflow-y: auto; }
.ai__session {
  position: relative;
  padding: 10px 30px 10px 12px;
  border-bottom: 1px solid var(--line-soft);
  cursor: pointer;
  transition: background-color var(--t-fast) var(--ease);
}
.ai__session:hover { background: var(--surface-soft, #f7f8fc); }
.ai__session.is-active { background: var(--surface-soft, #eef3ff); border-left: 3px solid var(--accent); padding-left: 9px; }
.ai__session-title {
  font-size: 13.5px; color: var(--ink); line-height: 1.4;
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}
.ai__session-time { font-size: 11px; color: var(--mute); margin-top: 2px; }
.ai__session-del {
  position: absolute; right: 8px; top: 50%; transform: translateY(-50%);
  font-size: 14px; color: var(--mute); padding: 4px;
  opacity: 0; transition: opacity var(--t-fast) var(--ease);
}
.ai__session:hover .ai__session-del { opacity: 1; }

/* ============== main ============== */
.ai__main {
  display: flex; flex-direction: column;
  border: 1px solid var(--line); background: var(--surface);
  min-height: 70vh;
}
.ai__list {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  display: flex; flex-direction: column; gap: 18px;
}
.ai__intro {
  margin: auto; text-align: center;
  color: var(--mute); display: flex; flex-direction: column; align-items: center; gap: 12px;
}
.ai__intro h3 { margin: 0; font-size: 18px; color: var(--ink-soft); }
.ai__intro p { margin: 0; max-width: 36ch; font-size: 13px; line-height: 1.6; }

.ai__msg {
  display: flex; flex-direction: column; gap: 6px;
  max-width: 100%;
}
.ai__msg--user { align-items: flex-end; }
.ai__msg--assistant { align-items: flex-start; }
.ai__bubble-header {
  display: inline-flex; gap: 8px; align-items: baseline;
  font-family: var(--font-mono); font-size: 11px; letter-spacing: 0.06em;
  color: var(--mute);
}
.ai__msg--user .ai__bubble-header { color: var(--accent); }
.ai__bubble {
  max-width: 78%;
  margin: 0; padding: 12px 16px;
  font-size: 14px; line-height: 1.75;
  white-space: pre-wrap; word-break: break-word;
  border: 1px solid var(--line); background: var(--surface-soft, #fafbff);
  font-family: var(--font-sans, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif);
}
.ai__msg--user .ai__bubble {
  background: var(--ink); color: #fff;
  border-color: var(--ink);
}
.ai__msg--assistant .ai__bubble {
  background: var(--surface-soft, #f4f7ff);
}
.ai__copy {
  display: inline-flex; align-items: center; gap: 4px;
  background: none; border: 0;
  font-size: 11px; letter-spacing: 0.08em;
  color: var(--mute); cursor: pointer;
  padding: 2px 0;
}
.ai__copy:hover { color: var(--accent); }

.ai__typing {
  display: inline-flex; gap: 4px;
  padding: 12px 16px;
  align-self: flex-start;
  background: var(--surface-soft, #f4f7ff);
  border: 1px solid var(--line);
}
.ai__typing span {
  width: 6px; height: 6px; background: var(--accent); border-radius: 50%;
  animation: typing 1s infinite;
}
.ai__typing span:nth-child(2) { animation-delay: 0.15s; }
.ai__typing span:nth-child(3) { animation-delay: 0.30s; }
@keyframes typing {
  0%, 60%, 100% { transform: translateY(0); opacity: 0.4; }
  30% { transform: translateY(-4px); opacity: 1; }
}

.ai__error {
  margin-top: 8px; padding: 10px 14px;
  border: 1px solid #f5c6cb; background: #f8d7da; color: #721c24;
  font-size: 13px;
}

.ai__composer {
  border-top: 1px solid var(--line);
  padding: 16px;
  display: flex; flex-direction: column; gap: 10px;
}
.ai__composer :deep(.el-textarea__inner) { border-radius: 0; }
.ai__composer-actions { display: flex; justify-content: flex-end; gap: 8px; }
.ai__hint {
  display: block; padding: 0 16px 12px;
  font-size: 11px; color: var(--mute); letter-spacing: 0.06em;
}
</style>
