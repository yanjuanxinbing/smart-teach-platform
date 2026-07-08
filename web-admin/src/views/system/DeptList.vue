<template>
  <div class="app-container">
    <el-card>
      <div class="toolbar">
        <span style="color: #909399">部门管理</span>
        <el-button type="primary" :icon="Plus" @click="openForm()">新增部门</el-button>
      </div>

      <el-table :data="tree" v-loading="loading" row-key="id" border default-expand-all
        :tree-props="{ children: 'children' }">
        <el-table-column prop="deptName" label="部门名称" />
        <el-table-column prop="deptCode" label="编码" width="160" />
        <el-table-column prop="leader" label="负责人" width="120" />
        <el-table-column prop="phone" label="电话" width="140" />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button size="small" link @click="openForm({ ...row, id: null, deptName: '', parentId: row.id })">添加下级</el-button>
            <el-button size="small" link @click="openForm(row)">编辑</el-button>
            <el-button size="small" link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑部门' : '新增部门'" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="上级部门">
          <el-tree-select v-model="form.parentId" :data="parentOptions" :props="{ value: 'id', label: 'deptName' }" check-strictly clearable style="width:100%" />
        </el-form-item>
        <el-form-item label="部门名称" prop="deptName"><el-input v-model="form.deptName" /></el-form-item>
        <el-form-item label="部门编码"><el-input v-model="form.deptCode" /></el-form-item>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="负责人"><el-input v-model="form.leader" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="电话" prop="phone"><el-input v-model="form.phone" maxlength="11" placeholder="请输入11位手机号" @input="form.phone = (form.phone || '').replace(/\D/g, '').slice(0, 11)" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
        <el-form-item label="状态"><el-radio-group v-model="form.status"><el-radio :value="1">启用</el-radio><el-radio :value="0">禁用</el-radio></el-radio-group></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { deptTree, deptAdd, deptEdit, deptRemove } from '@/api/system'

const tree = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const formRef = ref()
const form = reactive({ id: null, parentId: 0, deptName: '', deptCode: '', leader: '', phone: '', email: '', sort: 0, status: 1 })
const rules = {
  deptName: [{ required: true, message: '请输入部门名称' }],
  phone: [{ pattern: /^\d{11}$/, message: '电话必须是11位数字', trigger: 'blur' }]
}
const parentOptions = computed(() => [{ id: 0, deptName: '顶级' }, ...tree.value])

const load = async () => { loading.value = true; try { tree.value = await deptTree() } finally { loading.value = false } }
const openForm = (row) => {
  dialogVisible.value = true
  if (row && row.id) Object.assign(form, row)
  else Object.assign(form, { id: null, parentId: row?.parentId || 0, deptName: '', deptCode: '', leader: '', phone: '', email: '', sort: 0, status: 1 })
}
const submit = async () => { await formRef.value.validate(); if (form.id) await deptEdit(form); else await deptAdd(form); ElMessage.success('保存成功'); dialogVisible.value = false; load() }
const remove = async (row) => { await ElMessageBox.confirm(`确定删除"${row.deptName}"？`, '提示', { type: 'warning' }); await deptRemove(row.id); ElMessage.success('删除成功'); load() }

onMounted(load)
</script>
