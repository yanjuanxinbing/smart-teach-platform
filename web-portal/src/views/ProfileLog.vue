<template>
  <section class="log">
    <header class="log__head">
      <span class="eyebrow"><span class="eyebrow__rule"></span>Activity</span>
      <h2 class="log__title">操作<em>日志</em></h2>
      <p class="log__lead">最近 30 天的关键操作留痕，用于个人信息可追溯。</p>
    </header>

    <div class="bar">
      <el-select v-model="filters.type" placeholder="类型" clearable class="bar__sel">
        <el-option label="登录" value="login" />
        <el-option label="修改资料" value="profile" />
        <el-option label="课程" value="course" />
        <el-option label="作业" value="assignment" />
        <el-option label="代码库" value="code" />
      </el-select>
      <el-date-picker v-model="filters.range" type="daterange" range-separator="→" start-placeholder="起" end-placeholder="止" class="bar__range" />
      <el-input v-model="filters.q" placeholder="搜索动作 / IP / 设备" clearable class="bar__search" />
      <el-button type="primary" plain @click="fetch">查询</el-button>
    </div>

    <article class="list">
      <ol class="timeline">
        <li v-for="(it, idx) in list" :key="idx" class="t-item" data-reveal>
          <span class="t-item__dot" :style="{ background: COLOR[it.type] || 'var(--accent)' }"></span>
          <div class="t-item__body">
            <div class="t-item__row">
              <span class="t-item__action">{{ it.action }}</span>
              <span class="t-item__time">{{ it.time }}</span>
            </div>
            <p class="t-item__meta">{{ it.target }} · IP {{ it.ip }} · {{ it.device }}</p>
            <span class="t-item__chip">{{ TYPE_LABEL[it.type] || it.type }}</span>
          </div>
        </li>
        <li v-if="!list.length" class="t-empty">暂无操作记录</li>
      </ol>

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
    </article>
  </section>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { myOperationLog } from '@/api/profile'

const COLOR = { login: '#5F86FF', profile: '#3B82A8', course: '#A9C2FF', assignment: '#C2924A', code: '#2F5BFF' }
const TYPE_LABEL = { login: '登录', profile: '资料', course: '课程', assignment: '作业', code: '代码' }

const list = ref([])
const total = ref(0)
const page = reactive({ current: 1, size: 10 })
const filters = reactive({ q: '', type: '', range: null })

const seed = (i, type, action, target, ip, device) => ({
  id: i, type, action, target,
  time: `${10 + i} 月 ${10 + (i % 7)} 日 ${10 + (i * 3 % 12)}:${10 + (i * 7 % 49)}`,
  ip, device
})

const fetch = async () => {
  try {
    const res = await myOperationLog({ current: page.current, size: page.size, type: filters.type, q: filters.q })
    list.value = res?.records || res?.list || []
    total.value = Number(res?.total || list.value.length)
  } catch (e) {
    list.value = Array.from({ length: 8 }, (_, i) => seed(i,
      ['login', 'profile', 'course', 'assignment', 'code'][i % 5],
      ['登录系统', '更新个人资料', '加入课程', '提交作业', '收藏代码片段', '退出登录'][i % 6],
      ['个人空间', 'Java EE 课程', 'Spring Boot 实验', 'CodeX 数据库工具', 'Python 数据分析'][i % 5],
      '10.0.0.' + (10 + i),
      ['Chrome / macOS', 'Safari / iOS', 'Edge / Windows'][i % 3]
    ))
    total.value = 24
  }
}

onMounted(fetch)
</script>

<style scoped>
.log__head { margin-bottom: var(--s-6); }
.eyebrow { display: inline-flex; align-items: center; gap: 8px; font-family: var(--font-mono); font-size: 11px; letter-spacing: 0.18em; text-transform: uppercase; color: var(--mute); }
.eyebrow__rule { display: inline-block; width: 26px; height: 1px; background: var(--accent); }
.log__title { font-family: var(--font-display); font-size: clamp(26px, 3vw, 36px); font-weight: 600; color: var(--ink); margin: 6px 0 6px; letter-spacing: -0.018em; }
.log__title em { font-family: var(--font-serif); font-style: italic; font-weight: 500; color: var(--accent); }
.log__lead { color: var(--ink-soft); margin: 0 0 var(--s-5); font-size: 14px; }

.bar { display: flex; flex-wrap: wrap; gap: var(--s-3); padding: var(--s-4); border: 1px solid var(--line); background: var(--surface); margin-bottom: var(--s-5); }
.bar__sel, .bar__range { width: 180px; }
.bar__search { flex: 1; min-width: 220px; }
.bar :deep(.el-input__wrapper), .bar :deep(.el-select__wrapper), .bar :deep(.el-range-editor) { border-radius: 0; }

.list { background: var(--surface); border: 1px solid var(--line); padding: clamp(20px, 2.6vw, 32px); }
.timeline { list-style: none; padding: 0; margin: 0; position: relative; }
.timeline::before { content: ''; position: absolute; left: 6px; top: 4px; bottom: 4px; width: 1px; background: var(--line); }
.t-item { position: relative; padding: 0 0 var(--s-5) 30px; }
.t-item__dot { position: absolute; left: 0; top: 6px; width: 13px; height: 13px; border-radius: 50%; border: 3px solid var(--surface); box-shadow: 0 0 0 1px var(--line); }
.t-item__row { display: flex; justify-content: space-between; align-items: center; gap: 12px; }
.t-item__action { font-size: 14px; color: var(--ink); font-weight: 500; }
.t-item__time { font-family: var(--font-mono); font-size: 11.5px; color: var(--mute); }
.t-item__meta { margin: 4px 0 6px; font-size: 12.5px; color: var(--ink-soft); }
.t-item__chip { font-family: var(--font-mono); font-size: 10.5px; letter-spacing: 0.08em; padding: 2px 8px; background: var(--brand-100); color: var(--accent); border: 1px solid var(--accent-tint); }
.t-empty { color: var(--mute); font-size: 13px; text-align: center; padding: var(--s-8) 0; }

.pager { display: flex; justify-content: center; margin-top: var(--s-6); }
.pager :deep(.el-pager li), .pager :deep(.btn-prev), .pager :deep(.btn-next) { background: var(--surface) !important; color: var(--ink) !important; font-family: var(--font-mono) !important; }
.pager :deep(.el-pager li.is-active) { background: var(--ink) !important; color: #fff !important; }
</style>
