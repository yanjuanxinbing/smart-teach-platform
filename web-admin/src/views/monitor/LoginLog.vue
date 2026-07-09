<template>
  <div class="app-container">
    <el-card>
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input v-model="query.keyword" placeholder="账号" clearable style="width: 220px" @keyup.enter="search" />
          <el-button type="primary" @click="search">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </div>
        <div>
          <el-button type="danger" :disabled="!selection.length" @click="removeBatch">批量删除</el-button>
        </div>
      </div>

      <el-table :data="list" v-loading="loading" border @selection-change="(rows) => selection = rows">
        <el-table-column type="selection" width="50" />
        <el-table-column prop="username" label="账号" width="160" />
        <el-table-column prop="ip" label="IP" width="140" />
        <el-table-column prop="location" label="登录地" width="180" />
        <el-table-column prop="browser" label="浏览器" width="100" />
        <el-table-column prop="os" label="操作系统" width="120" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '成功' : '失败' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="message" label="消息" />
        <el-table-column prop="loginTime" label="登录时间" width="170" />
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button size="small" link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination v-model:page="query.pageNum" v-model:size="query.pageSize" :total="total" @change="load" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import Pagination from '@/components/Pagination.vue'
import { loginLogPage, loginLogRemove } from '@/api/monitor'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '' })
const selection = ref([])

const load = async () => {
  loading.value = true
  try {
    const res = await loginLogPage(query)
    // 防御性兜底：万一后端返回结构异常，分页器至少能渲染
    list.value = Array.isArray(res?.list) ? res.list : []
    total.value = Number(res?.total) || 0
  } catch (e) {
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}
// 搜索时统一从第 1 页开始，避免停留在无数据的页码
const search = () => { query.pageNum = 1; load() }
const resetQuery = () => {
  query.keyword = ''
  query.pageNum = 1
  load()
}

// 单条删除：复用批量删除接口，传一个 id 即可
const remove = async (row) => {
  await ElMessageBox.confirm(`确定删除"${row.username}"于 ${row.loginTime} 的登录日志？`, '提示', { type: 'warning' })
  await loginLogRemove([row.id])
  ElMessage.success('删除成功')
  // 删除当前页最后一条时回退一页，避免页码越界
  if (list.value.length === 1 && query.pageNum > 1) query.pageNum -= 1
  load()
}
const removeBatch = async () => {
  await ElMessageBox.confirm(`确定删除选中的 ${selection.value.length} 条登录日志？`, '提示', { type: 'warning' })
  await loginLogRemove(selection.value.map(s => s.id))
  ElMessage.success('删除成功')
  if (list.value.length === selection.value.length && query.pageNum > 1) query.pageNum -= 1
  selection.value = []
  load()
}

onMounted(load)
</script>
