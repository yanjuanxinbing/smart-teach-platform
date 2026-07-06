<template>
  <div class="app-container">
    <el-card>
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input v-model="query.keyword" placeholder="参数名/键" clearable style="width: 220px" @keyup.enter="load" />
          <el-button type="primary" @click="load">搜索</el-button>
        </div>
        <el-button type="primary" :icon="Plus" @click="openForm()">新增参数</el-button>
      </div>

      <el-table :data="list" v-loading="loading" border>
        <el-table-column prop="configName" label="参数名称" />
        <el-table-column prop="configKey" label="参数键" width="200" />
        <el-table-column prop="configValue" label="参数值" />
        <el-table-column prop="configType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.configType === 1 ? 'danger' : 'info'">{{ row.configType === 1 ? '系统内置' : '业务参数' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" link @click="openForm(row)">编辑</el-button>
            <el-button size="small" link type="danger" :disabled="row.configType === 1" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination v-model:page="query.pageNum" v-model:size="query.pageSize" :total="total" @change="load" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑参数' : '新增参数'" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="参数名称" prop="configName"><el-input v-model="form.configName" /></el-form-item>
        <el-form-item label="参数键名" prop="configKey"><el-input v-model="form.configKey" /></el-form-item>
        <el-form-item label="参数值"><el-input v-model="form.configValue" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="类型"><el-radio-group v-model="form.configType"><el-radio :value="0">业务参数</el-radio><el-radio :value="1">系统内置</el-radio></el-radio-group></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import Pagination from '@/components/Pagination.vue'
import { configPage, configAdd, configEdit, configRemove } from '@/api/system'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '' })
const dialogVisible = ref(false)
const formRef = ref()
const form = reactive({ id: null, configName: '', configKey: '', configValue: '', configType: 0, remark: '' })
const rules = { configName: [{ required: true, message: '请输入参数名称' }], configKey: [{ required: true, message: '请输入参数键名' }] }

const load = async () => { loading.value = true; try { const res = await configPage(query); list.value = res.list; total.value = res.total } finally { loading.value = false } }
const openForm = (row) => {
  dialogVisible.value = true
  if (row) Object.assign(form, row)
  else Object.assign(form, { id: null, configName: '', configKey: '', configValue: '', configType: 0, remark: '' })
}
const submit = async () => { await formRef.value.validate(); if (form.id) await configEdit(form); else await configAdd(form); ElMessage.success('保存成功'); dialogVisible.value = false; load() }
const remove = async (row) => { await ElMessageBox.confirm(`确定删除"${row.configName}"？`, '提示', { type: 'warning' }); await configRemove([row.id]); ElMessage.success('删除成功'); load() }

onMounted(load)
</script>
