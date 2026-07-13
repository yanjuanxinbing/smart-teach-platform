<template>
  <section class="msg">
    <header class="msg__head">
      <span class="eyebrow"><span class="eyebrow__rule"></span>Inbox</span>
      <h2 class="msg__title">我的<em>消息</em></h2>
      <p class="msg__lead">系统通知、课程动态与私信的聚合收件箱。</p>
      <div class="msg__toolbar">
        <el-radio-group v-model="tab" size="default">
          <el-radio-button value="all">全部</el-radio-button>
          <el-radio-button value="unread">未读 <em v-if="unread">({{ unread }})</em></el-radio-button>
          <el-radio-button value="system">系统</el-radio-button>
          <el-radio-button value="course">课程</el-radio-button>
          <el-radio-button value="private">私信</el-radio-button>
        </el-radio-group>
        <el-button :disabled="!list.some(i => !i.read)" @click="onMarkAll">全部标记已读</el-button>
      </div>
    </header>

    <article v-if="!list.length" class="empty">
      <el-icon :size="36"><Bell /></el-icon>
      <p>暂无消息</p>
    </article>
    <ul v-else class="messages">
      <li
        v-for="m in filtered"
        :key="m.id"
        class="row"
        :class="{ 'row--unread': !m.read }"
        @click="open(m)"
      >
        <span class="row__dot"></span>
        <span class="row__type" :style="{ color: TYPE_COLOR[m.type] }">{{ TYPE_LABEL[m.type] || m.type }}</span>
        <div class="row__body">
          <div class="row__title">{{ m.title }}</div>
          <p class="row__brief">{{ m.brief }}</p>
        </div>
        <span class="row__time">{{ shortTime(m.createTime) }}</span>
        <div class="row__actions">
          <el-button v-if="!m.read" size="small" plain @click.stop="markOne(m)">标已读</el-button>
          <el-button size="small" plain @click.stop="remove(m)">删除</el-button>
        </div>
      </li>
    </ul>

    <el-pagination
      v-if="list.length"
      v-model:current-page="page.current"
      v-model:page-size="page.size"
      :total="total"
      layout="prev, pager, next, jumper"
      background
      class="pager"
      @current-change="fetch"
    />

    <el-dialog v-model="dialogVisible" :title="current?.title" width="540px" class="msg-dialog">
      <p class="msg-dialog__meta" v-if="current">{{ TYPE_LABEL[current.type] }} · {{ shortTime(current.createTime) }}</p>
      <div class="msg-dialog__body">{{ current?.content || current?.brief }}</div>
    </el-dialog>
  </section>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Bell } from '@element-plus/icons-vue'
import { myMessages, markRead, markAllRead, unreadCount } from '@/api/profile'

const TAB_PARAMS = { all: '', unread: 'unread', system: 'system', course: 'course', private: 'private' }
const TYPE_LABEL = { system: '系统', course: '课程', code: '代码', assignment: '作业', private: '私信' }
const TYPE_COLOR = { system: '#5F86FF', course: '#3B82A8', code: '#A9C2FF', assignment: '#C2924A', private: '#2F5BFF' }

const list = ref([])
const total = ref(0)
const tab = ref('all')
const page = reactive({ current: 1, size: 10 })
const dialogVisible = ref(false)
const current = ref(null)
const unread = ref(0)

const filtered = computed(() => list.value)
const shortTime = (t) => t ? (typeof t === 'string' && t.length >= 16 ? t.slice(5, 16) : t) : ''

const fetch = async () => {
  try {
    const res = await myMessages({ current: page.current, size: page.size, tab: TAB_PARAMS[tab.value] })
    list.value = res?.records || res?.list || []
    total.value = Number(res?.total || list.value.length)
  } catch (e) { list.value = []; total.value = 0 }
}

const refreshUnread = async () => {
  try { unread.value = Number(await unreadCount() || 0) } catch (e) {}
}

const open = (m) => {
  current.value = m
  dialogVisible.value = true
  if (!m.read) markOne(m)
}
const markOne = async (m) => {
  m.read = true
  try { await markRead([m.id]) } catch (e) {}
  refreshUnread()
}
const onMarkAll = async () => {
  list.value.forEach(m => { m.read = true })
  try { await markAllRead() } catch (e) {}
  ElMessage.success('已全部标记为已读')
  unread.value = 0
}
const remove = async (m) => {
  list.value = list.value.filter(x => x.id !== m.id)
  ElMessage.success('已删除')
}

watch(tab, () => { page.current = 1; fetch() })
onMounted(async () => { refreshUnread(); fetch() })
</script>

<style scoped>
.msg__head { margin-bottom: var(--s-6); }
.eyebrow { display: inline-flex; align-items: center; gap: 8px; font-family: var(--font-mono); font-size: 11px; letter-spacing: 0.18em; text-transform: uppercase; color: var(--mute); }
.eyebrow__rule { display: inline-block; width: 26px; height: 1px; background: var(--accent); }
.msg__title { font-family: var(--font-display); font-size: clamp(26px, 3vw, 36px); font-weight: 600; color: var(--ink); margin: 6px 0 6px; letter-spacing: -0.018em; }
.msg__title em { font-family: var(--font-serif); font-style: italic; font-weight: 500; color: var(--accent); }
.msg__lead { color: var(--ink-soft); margin: 0 0 var(--s-5); font-size: 14px; }
.msg__toolbar { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 12px; }
.msg__toolbar :deep(.el-radio-button__inner) { border-radius: 0 !important; }
.msg__toolbar em { font-style: normal; color: var(--danger); margin-left: 4px; }

.empty { padding: clamp(60px, 10vw, 120px) 0; text-align: center; color: var(--mute); display: flex; flex-direction: column; align-items: center; gap: 14px; background: var(--surface); border: 1px dashed var(--line); }

.messages { list-style: none; margin: 0; padding: 0; background: var(--surface); border: 1px solid var(--line); }
.row {
  display: grid; grid-template-columns: 12px 60px 1fr 110px 110px;
  align-items: center; gap: 14px;
  padding: 16px 18px; cursor: pointer; border-bottom: 1px solid var(--line-soft);
  transition: background-color var(--t-fast) var(--ease);
}
.row:last-child { border-bottom: none; }
.row:hover { background: var(--surface-soft); }
.row__dot { width: 8px; height: 8px; border-radius: 50%; background: var(--mute-light); }
.row--unread .row__dot { background: var(--danger); }
.row__type { font-family: var(--font-mono); font-size: 11px; letter-spacing: 0.1em; text-transform: uppercase; }
.row__body { min-width: 0; }
.row__title { font-size: 14px; color: var(--ink); font-weight: 500; transition: color var(--t-fast) var(--ease); }
.row:hover .row__title { color: var(--accent); }
.row--unread .row__title { color: var(--ink); font-weight: 600; }
.row__brief { margin: 4px 0 0; font-size: 12.5px; line-height: 1.6; color: var(--ink-soft); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.row__time { font-family: var(--font-mono); font-size: 11.5px; color: var(--mute); }
.row__actions { display: flex; gap: 6px; justify-content: flex-end; }
.row__actions :deep(.el-button) { border-radius: 0; padding: 4px 10px; font-size: 11.5px; }

.pager { display: flex; justify-content: center; margin-top: var(--s-5); }
.pager :deep(.el-pager li), .pager :deep(.btn-prev), .pager :deep(.btn-next) { background: var(--surface) !important; color: var(--ink) !important; font-family: var(--font-mono) !important; }
.pager :deep(.el-pager li.is-active) { background: var(--ink) !important; color: #fff !important; }

.msg-dialog :deep(.el-dialog__title) { font-family: var(--font-display); font-weight: 600; }
.msg-dialog__meta { color: var(--mute); font-family: var(--font-mono); font-size: 12px; letter-spacing: 0.06em; margin: 0 0 var(--s-4); }
.msg-dialog__body { font-size: 14px; line-height: 1.85; color: var(--ink-soft); white-space: pre-wrap; }

@media (max-width: 880px) {
  .row { grid-template-columns: 12px 1fr; row-gap: 8px; }
  .row__type, .row__time, .row__actions { grid-column: 2; }
}
</style>
