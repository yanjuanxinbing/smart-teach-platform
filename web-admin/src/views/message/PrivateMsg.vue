<template>
  <div class="pm">
    <header class="pm__head">
      <span class="pm__crumb">消息 · 私信</span>
      <h2 class="pm__title">站内私信</h2>
      <p class="pm__lead">与同事 / 教师 / 学生的实时会话。</p>
    </header>

    <el-card shadow="never" class="pm__chat">
      <div class="pm__chat-inner">
        <aside class="pm__list">
          <header class="pm__list-head">
            <span>会话</span>
            <el-button size="small" @click="dialogVisible = true">发起会话</el-button>
          </header>
          <ul>
            <li v-for="s in sessions" :key="s.id"
                :class="{ 'is-active': activeId === s.id }" @click="activeId = s.id">
              <el-avatar :size="32">{{ s.name.charAt(0) }}</el-avatar>
              <div class="pm__list-body">
                <div class="pm__list-row">
                  <span class="pm__list-name">{{ s.name }}</span>
                  <span class="pm__list-time">{{ s.lastTime }}</span>
                </div>
                <p class="pm__list-brief">{{ s.lastMsg }}</p>
              </div>
              <el-badge v-if="s.unread" :value="s.unread" :max="99" />
            </li>
          </ul>
        </aside>

        <section class="pm__msgs">
          <header class="pm__msgs-head">
            <span class="pm__msgs-name">{{ activeSession?.name }}</span>
            <span class="pm__msgs-online">{{ activeSession?.online ? '在线' : '离线' }}</span>
          </header>
          <div class="pm__msgs-list" ref="listRef">
            <div v-for="(m, i) in activeMsgs" :key="i"
                 class="msg" :class="{ 'msg--mine': m.mine }">
              <el-avatar v-if="!m.mine" :size="28">{{ m.from.charAt(0) }}</el-avatar>
              <div class="msg__body">
                <div class="msg__bubble">{{ m.text }}</div>
                <span class="msg__time">{{ m.time }}</span>
              </div>
            </div>
          </div>
          <footer class="pm__input">
            <el-input v-model="text" type="textarea" :rows="3" placeholder="按 Ctrl + Enter 发送" @keydown="onKey" />
            <el-button type="primary" :disabled="!text.trim()" @click="send">发送</el-button>
          </footer>
        </section>
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" title="发起会话" width="420px">
      <el-form :model="form">
        <el-form-item label="对象">
          <el-input v-model="form.to" placeholder="用户名 / 工号 / 学号" />
        </el-form-item>
        <el-form-item label="内容"><el-input v-model="form.text" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="startNew">发起</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, nextTick } from 'vue'
import { ElMessage } from 'element-plus'

const sessions = ref([
  { id: 1, name: '张老师', online: true, lastTime: '12:08', lastMsg: '下次课的时间已调整到周四下午。', unread: 2 },
  { id: 2, name: '王同学', online: false, lastTime: '昨天', lastMsg: '作业已经提交，请查收。', unread: 0 },
  { id: 3, name: '陈老师', online: true, lastTime: '周三', lastMsg: '课件已上传到资源中心。', unread: 0 }
])

const allMsgs = reactive({
  1: [
    { from: '张老师', mine: false, text: '课程大纲已更新，请查收。', time: '今天 09:14' },
    { from: 'me',     mine: true,  text: '收到，感谢张老师！', time: '今天 09:15' },
    { from: '张老师', mine: false, text: '下次课的时间已调整到周四下午。', time: '今天 12:08' }
  ],
  2: [
    { from: '王同学', mine: false, text: '作业已经提交，请查收。', time: '昨天 22:31' }
  ],
  3: [
    { from: '陈老师', mine: false, text: '课件已上传到资源中心。', time: '周三 10:00' }
  ]
})

const activeId = ref(1)
const text = ref('')
const listRef = ref()
const dialogVisible = ref(false)
const form = reactive({ to: '', text: '' })

const activeSession = computed(() => sessions.value.find(s => s.id === activeId.value))
const activeMsgs = computed(() => allMsgs[activeId.value] || [])

const send = () => {
  const t = text.value.trim()
  if (!t) return
  ;(allMsgs[activeId.value] = allMsgs[activeId.value] || []).push({ from: 'me', mine: true, text: t, time: nowTime() })
  text.value = ''
  nextTick(() => { if (listRef.value) listRef.value.scrollTop = listRef.value.scrollHeight })
}
const onKey = (e) => { if (e.ctrlKey && e.key === 'Enter') send() }
const nowTime = () => { const d = new Date(); return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}` }

const startNew = () => {
  if (!form.to.trim() || !form.text.trim()) return ElMessage.warning('请填写对象与内容')
  const id = Date.now()
  sessions.value.unshift({ id, name: form.to.trim(), online: false, lastTime: nowTime(), lastMsg: form.text.trim(), unread: 0 })
  allMsgs[id] = [{ from: 'me', mine: true, text: form.text, time: nowTime() }]
  dialogVisible.value = false
  activeId.value = id
  ElMessage.success('会话已发起')
  form.to = ''; form.text = ''
}
</script>

<style scoped>
.pm__head { margin-bottom: var(--s-4); }
.pm__crumb { font-family: var(--font-mono); font-size: 11px; letter-spacing: 0.18em; text-transform: uppercase; color: var(--mute); }
.pm__title { font-family: var(--font-display); font-size: 22px; font-weight: 600; color: var(--ink); margin: 6px 0 4px; }
.pm__lead { color: var(--ink-soft); margin: 0 0 var(--s-4); font-size: 13.5px; }

.pm__chat-inner { display: grid; grid-template-columns: 280px 1fr; height: 560px; }
.pm__list { border-right: 1px solid var(--line); background: var(--surface-soft); }
.pm__list-head { display: flex; justify-content: space-between; align-items: center; padding: 12px 16px; border-bottom: 1px solid var(--line); }
.pm__list ul { list-style: none; margin: 0; padding: 0; }
.pm__list li { display: flex; gap: 10px; padding: 12px 16px; cursor: pointer; transition: background-color var(--t-fast) var(--ease); }
.pm__list li:hover { background: var(--surface); }
.pm__list li.is-active { background: var(--brand-100); }
.pm__list-body { flex: 1; min-width: 0; }
.pm__list-row { display: flex; justify-content: space-between; }
.pm__list-name { font-size: 14px; color: var(--ink); font-weight: 500; }
.pm__list-time { font-family: var(--font-mono); font-size: 11px; color: var(--mute); }
.pm__list-brief { margin: 4px 0 0; font-size: 12.5px; color: var(--ink-soft); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

.pm__msgs { display: flex; flex-direction: column; }
.pm__msgs-head { padding: 12px 18px; border-bottom: 1px solid var(--line); display: flex; align-items: center; justify-content: space-between; }
.pm__msgs-name { font-size: 14.5px; color: var(--ink); font-weight: 600; }
.pm__msgs-online { font-family: var(--font-mono); font-size: 11px; color: var(--positive); letter-spacing: 0.12em; text-transform: uppercase; }
.pm__msgs-list { flex: 1; overflow-y: auto; padding: var(--s-5) var(--s-6); display: flex; flex-direction: column; gap: var(--s-3); }
.msg { display: flex; align-items: flex-start; gap: 8px; max-width: 76%; }
.msg--mine { align-self: flex-end; flex-direction: row-reverse; }
.msg__bubble { padding: 10px 14px; background: var(--surface-cool); border: 1px solid var(--line); font-size: 13.5px; line-height: 1.6; color: var(--ink); }
.msg--mine .msg__bubble { background: var(--accent); border-color: var(--accent); color: #fff; }
.msg__time { display: block; margin-top: 4px; font-family: var(--font-mono); font-size: 11px; color: var(--mute); }
.msg--mine .msg__time { text-align: right; }

.pm__input { display: flex; gap: 10px; padding: 12px 18px; border-top: 1px solid var(--line); align-items: flex-end; background: var(--surface); }
.pm__input :deep(.el-textarea__inner) { border-radius: 2px; }
</style>
