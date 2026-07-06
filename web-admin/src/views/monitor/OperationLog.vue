<template>
  <div class="app-container">
    <el-card>
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input v-model="query.module" placeholder="模块" clearable style="width: 160px" />
          <el-input v-model="query.username" placeholder="操作人" clearable style="width: 160px" />
          <el-select v-model="query.status" placeholder="状态" clearable style="width: 120px">
            <el-option label="成功" :value="1" /><el-option label="失败" :value="0" />
          </el-select>
          <el-button type="primary" @click="load">搜索</el-button>
        </div>
        <div>
          <el-button type="warning" @click="clean">清理30天前日志</el-button>
          <el-button type="danger" :disabled="!selection.length" @click="removeBatch">批量删除</el-button>
        </div>
      </div>

      <el-table :data="list" v-loading="loading" border @selection-change="(rows) => selection = rows">
        <el-table-column type="selection" width="50" />
        <el-table-column prop="module" label="模块" width="140" />
        <el-table-column prop="action" label="操作" />
        <el-table-column prop="requestUri" label="请求路径" width="200" show-overflow-tooltip />
        <el-table-column prop="httpMethod" label="方法" width="80" />
        <el-table-column prop="username" label="操作人" width="100" />
        <el-table-column prop="ip" label="IP" width="120" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '成功' : '失败' }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="costTime" label="耗时(ms)" width="100" />
        <el-table-column prop="operationTime" label="时间" width="170" />
        <el-table-column label="详情" width="80" fixed="right">
          <template #default="{ row }"><el-button size="small" link @click="showDetail(row)">查看</el-button></template>
        </el-table-column>
      </el-table>

      <Pagination v-model:page="query.pageNum" v-model:size="query.pageSize" :total="total" @change="load" />
    </el-card>

    <el-dialog v-model="detailDialog" title="操作日志详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="模块">{{ current.module }}</el-descriptions-item>
        <el-descriptions-item label="操作">{{ current.action }}</el-descriptions-item>
        <el-descriptions-item label="请求路径" :span="2">{{ current.requestUri }}</el-descriptions-item>
        <el-descriptions-item label="HTTP方法">{{ current.httpMethod }}</el-descriptions-item>
        <el-descriptions-item label="耗时">{{ current.costTime }} ms</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ current.username }}</el-descriptions-item>
        <el-descriptions-item label="IP">{{ current.ip }}</el-descriptions-item>
        <el-descriptions-item label="状态" :span="2">
          <el-tag :type="current.status === 1 ? 'success' : 'danger'">{{ current.status === 1 ? '成功' : '失败' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item v-if="current.errorMsg" label="错误信息" :span="2">{{ current.errorMsg }}</el-descriptions-item>
      </el-descriptions>
      <h4 style="margin-top: 16px">请求参数</h4>
      <pre style="background: #f5f7fa; padding: 8px; border-radius: 4px; max-height: 200px; overflow: auto">{{ current.params || '-' }}</pre>
      <h4>返回结果</h4>
      <pre style="background: #f5f7fa; padding: 8px; border-radius: 4px; max-height: 200px; overflow: auto">{{ current.result || '-' }}</pre>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import Pagination from '@/components/Pagination.vue'
import { operationLogPage, operationLogClean, operationLogRemove } from '@/api/monitor'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ pageNum: 1, pageSize: 10, module: '', username: '', status: null })
const selection = ref([])
const detailDialog = ref(false)
const current = ref({})

const load = async () => { loading.value = true; try { const res = await operationLogPage(query); list.value = res.list; total.value = res.total } finally { loading.value = false } }
const showDetail = (row) => { current.value = row; detailDialog.value = true }
const clean = async () => { await ElMessageBox.confirm('确定清理30天前的操作日志？', '提示', { type: 'warning' }); await operationLogClean(30); ElMessage.success('已清理'); load() }
const removeBatch = async () => { await ElMessageBox.confirm(`确定删除选中的 ${selection.value.length} 条日志？`, '提示', { type: 'warning' }); await operationLogRemove(selection.value.map(s => s.id)); ElMessage.success('删除成功'); load() }

onMounted(load)
</script>
