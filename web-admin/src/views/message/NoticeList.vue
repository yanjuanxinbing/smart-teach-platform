<template>
  <div class="mn">
    <header class="mn__head">
      <span class="mn__crumb">消息 · 系统通知</span>
      <h2 class="mn__title">系统通知 <el-badge v-if="unread" :value="unread" :max="99" /></h2>
      <p class="mn__lead">来自平台、运维、审核等系统级消息。</p>
    </header>

    <el-card shadow="never">
      <div class="bar">
        <el-radio-group v-model="tab">
          <el-radio-button value="all">全部</el-radio-button>
          <el-radio-button value="unread">未读</el-radio-button>
          <el-radio-button value="system">系统</el-radio-button>
          <el-radio-button value="audit">审核</el-radio-button>
        </el-radio-group>
        <el-button :disabled="!list.some(i => !i.read)" @click="readAll">全部标已读</el-button>
      </div>

      <ul v-if="list.length" class="list">
        <li v-for="m in list" :key="m.id" class="row" :class="{ 'row--unread': !m.read }" @click="open(m)">
          <el-icon class="row__ico"><BellFilled /></el-icon>
          <div class="row__body">
            <div class="row__title">{{ m.title }}</div>
            <p class="row__brief">{{ m.brief }}</p>
            <div class="row__meta">
              <el-tag :type="(m.level || 'info') === 'warn' ? 'warning' : (m.level === 'danger' ? 'danger' : 'info')" size="small">{{ m.levelLabel }}</el-tag>
              <span>{{ shortTime(m.createTime) }}</span>
            </div>
          </div>
          <span class="row__dot"></span>
        </li>
      </ul>
      <el-empty v-else description="暂无系统通知" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="current?.title" width="540px">
      <p style="color: var(--mute); font-family: var(--font-mono); font-size: 12px">{{ shortTime(current?.createTime) }}</p>
      <div style="margin-top: 12px; line-height: 1.85; color: var(--ink-soft)">{{ current?.content || current?.brief }}</div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { BellFilled } from '@element-plus/icons-vue'
import { myMessages, markRead, markAllRead, unreadCount } from '@/api/profile'

const list = ref([])
const tab = ref('all')
const dialogVisible = ref(false)
const current = ref(null)
const unread = ref(0)
const TAB = { all: '', unread: 'unread', system: 'system', audit: 'audit' }

const shortTime = (t) => t ? t.slice(5, 16) : ''

const fetch = async () => {
  try {
    const res = await myMessages({ tab: TAB[tab.value], size: 20 })
    list.value = res?.records || res?.list || []
  } catch (e) {
    list.value = [
      { id: 1, title: '系统升级通知', brief: '平台将于本周日凌晨 02:00-04:00 进行例行维护升级，请合理安排作业提交时间。', content: '平台将于本周日凌晨 02:00-04:00 进行例行维护升级，请合理安排作业提交时间。', level: 'info', levelLabel: '提示', read: false, createTime: '2025-11-14 09:30' },
      { id: 2, title: '课程资源审核通过', brief: '您提交的《Java 并发编程》课程已通过审核并发布到门户。', level: 'success', levelLabel: '已通过', read: false, createTime: '2025-11-13 17:21' },
      { id: 3, title: '异常登录提醒', brief: '检测到您的账号在新设备登录，如非本人操作请及时修改密码。', level: 'warn', levelLabel: '注意', read: true, createTime: '2025-11-12 08:11' }
    ]
  }
}
const refreshUnread = async () => { try { unread.value = Number((await unreadCount()) || 0) } catch (e) {} }

const open = (m) => { current.value = m; dialogVisible.value = true; if (!m.read) { m.read = true; markRead([m.id]).catch(()=>{}); refreshUnread() } }
const readAll = async () => { list.value.forEach(i => i.read = true); await markAllRead().catch(()=>{}); unread.value = 0; ElMessage.success('已全部标为已读') }

watch(tab, fetch)
onMounted(async () => { await fetch(); await refreshUnread() })
</script>

<style scoped>
.mn__head { margin-bottom: var(--s-4); }
.mn__crumb { font-family: var(--font-mono); font-size: 11px; letter-spacing: 0.18em; text-transform: uppercase; color: var(--mute); }
.mn__title { font-family: var(--font-display); font-size: 22px; font-weight: 600; color: var(--ink); margin: 6px 0 4px; display: inline-flex; align-items: center; gap: 10px; }
.mn__lead { color: var(--ink-soft); margin: 0 0 var(--s-4); font-size: 13.5px; }

.bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--s-4); }
.list { list-style: none; padding: 0; margin: 0; }
.row {
  display: grid; grid-template-columns: 24px 1fr 16px; gap: 14px; align-items: flex-start;
  padding: 14px 8px; border-bottom: 1px solid var(--line-soft); cursor: pointer;
  transition: background-color var(--t-fast) var(--ease);
}
.row:last-child { border-bottom: none; }
.row:hover { background: var(--surface-soft); }
.row__ico { font-size: 18px; color: var(--accent); margin-top: 2px; }
.row__title { font-size: 14px; font-weight: 500; color: var(--ink); }
.row--unread .row__title { color: var(--ink); font-weight: 600; }
.row__brief { margin: 6px 0; font-size: 13px; line-height: 1.7; color: var(--ink-soft); }
.row__meta { display: flex; align-items: center; gap: 10px; font-family: var(--font-mono); font-size: 11.5px; color: var(--mute); }
.row__dot { width: 8px; height: 8px; background: var(--mute-light); border-radius: 50%; margin-top: 6px; }
.row--unread .row__dot { background: var(--danger); }
</style>
