<template>
  <div class="app-container">
    <el-card>
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input v-model="query.keyword" placeholder="作业标题关键字" clearable style="width: 220px" @keyup.enter="load" />
          <el-select v-model="query.status" placeholder="作业状态" clearable style="width: 140px">
            <el-option label="已发布" :value="1" />
            <el-option label="已截止" :value="2" />
          </el-select>
          <el-button type="primary" @click="load">搜索</el-button>
          <el-button @click="reset">重置</el-button>
        </div>
      </div>

      <el-table :data="list" v-loading="loading" border>
        <el-table-column prop="title" label="作业标题" min-width="220" show-overflow-tooltip />
        <el-table-column prop="description" label="作业说明" min-width="220" show-overflow-tooltip />
        <el-table-column prop="deadline" label="截止时间" width="170">
          <template #default="{ row }">
            <span :style="{ color: isPastDeadline(row.deadline) && row.status === 1 ? '#f56c6c' : '' }">{{ row.deadline }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalScore" label="总分" width="80" />
        <el-table-column label="作业状态" width="100">
          <template #default="{ row }">
            <el-tag :type="['info','success','warning'][row.status]">{{ ['草稿','已发布','已截止'][row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="我的状态" width="120">
          <template #default="{ row }">
            <el-tag v-if="myStatusMap[row.id] === 0" type="info">已存草稿</el-tag>
            <el-tag v-else-if="myStatusMap[row.id] === 1" type="success">已提交</el-tag>
            <el-tag v-else-if="myStatusMap[row.id] === 2" type="primary">已批改</el-tag>
            <el-tag v-else type="info" effect="plain">未提交</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="分数" width="80">
          <template #default="{ row }">
            {{ myScoreMap[row.id] ?? '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button size="small" link v-if="userStore.hasAuthority('assignment:submit') && row.status !== 2" @click="goSubmit(row.id)">
              {{ myStatusMap[row.id] === 2 ? '查看' : (myStatusMap[row.id] != null ? '继续编辑' : '提交作业') }}
            </el-button>
            <el-button size="small" link v-else-if="myStatusMap[row.id] === 2" @click="goSubmit(row.id)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination v-model:page="query.pageNum" v-model:size="query.pageSize" :total="total" @change="load" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import Pagination from '@/components/Pagination.vue'
import { useUserStore } from '@/store/user'
import { assignPage } from '@/api/assignment'
import { submissionLatest } from '@/api/assignment'

const router = useRouter()
const userStore = useUserStore()

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', status: null })
const myStatusMap = reactive({})
const myScoreMap = reactive({})

const isPastDeadline = (deadline) => {
  if (!deadline) return false
  return new Date(deadline.replace(/-/g, '/')).getTime() < Date.now()
}

const load = async () => {
  loading.value = true
  try {
    const res = await assignPage(query)
    list.value = res.list
    total.value = res.total
    // 后台并发拉每个作业的 latest 提交，把我的状态/分数缓存
    await Promise.all(res.list.map(async row => {
      try {
        const sub = await submissionLatest(row.id)
        myStatusMap[row.id] = sub?.status ?? null
        myScoreMap[row.id] = sub?.score ?? null
      } catch (e) {
        myStatusMap[row.id] = null
        myScoreMap[row.id] = null
      }
    }))
  } finally { loading.value = false }
}

const reset = () => {
  query.keyword = ''
  query.status = null
  load()
}

const goSubmit = (id) => {
  router.push(`/student/assignment/submit/${id}`)
}

onMounted(load)
</script>
