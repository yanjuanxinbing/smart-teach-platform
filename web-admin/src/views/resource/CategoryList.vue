<template>
  <div class="app-container">
    <el-card>
      <div class="toolbar">
        <div class="toolbar-left">
          <span style="color: #909399">资源分类管理</span>
        </div>
        <el-button type="primary" :icon="Plus" @click="openForm()">新增分类</el-button>
      </div>

      <el-table :data="tree" v-loading="loading" row-key="id" border default-expand-all
        :tree-props="{ children: 'children' }">
        <el-table-column prop="categoryName" label="分类名称" />
        <el-table-column prop="categoryCode" label="编码" width="160" />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button size="small" link @click="openForm(row)">编辑</el-button>
            <el-button size="small" link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="formDialog" :title="form.id ? '编辑分类' : '新增分类'" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="父分类">
          <el-tree-select v-model="form.parentId" :data="parentOptions" :props="{ value: 'id', label: 'categoryName' }" check-strictly clearable style="width:100%" />
        </el-form-item>
        <el-form-item label="分类名称" prop="categoryName"><el-input v-model="form.categoryName" /></el-form-item>
        <el-form-item label="分类编码"><el-input v-model="form.categoryCode" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formDialog = false">取消</el-button>
        <el-button type="primary" @click="submit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { resCategoryTree, resCategoryAdd, resCategoryEdit, resCategoryRemove } from '@/api/resource'

const tree = ref([])
const loading = ref(false)
const formDialog = ref(false)
const formRef = ref()
const form = reactive({ id: null, parentId: 0, categoryName: '', categoryCode: '', sort: 0, status: 1, description: '' })
const rules = { categoryName: [{ required: true, message: '请输入分类名称' }] }

const parentOptions = computed(() => [{ id: 0, categoryName: '顶级分类' }, ...tree.value])

const load = async () => {
  loading.value = true
  try { tree.value = await resCategoryTree() }
  finally { loading.value = false }
}

const openForm = (row) => {
  formDialog.value = true
  if (row) Object.assign(form, row)
  else Object.assign(form, { id: null, parentId: 0, categoryName: '', categoryCode: '', sort: 0, status: 1, description: '' })
}

const submit = async () => {
  await formRef.value.validate()
  if (form.id) await resCategoryEdit(form); else await resCategoryAdd(form)
  ElMessage.success('保存成功'); formDialog.value = false; load()
}

const remove = async (row) => {
  await ElMessageBox.confirm(`确定删除分类"${row.categoryName}"？`, '提示', { type: 'warning' })
  await resCategoryRemove(row.id)
  ElMessage.success('删除成功'); load()
}

onMounted(load)
</script>
