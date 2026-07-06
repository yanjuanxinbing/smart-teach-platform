<template>
  <div class="app-container">
    <el-card>
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input v-model="query.keyword" placeholder="角色名/编码" clearable style="width: 220px" @keyup.enter="load" />
          <el-button type="primary" @click="load">搜索</el-button>
        </div>
        <el-button type="primary" :icon="Plus" @click="openForm()">新增角色</el-button>
      </div>

      <el-table :data="list" v-loading="loading" border>
        <el-table-column prop="roleName" label="角色名称" />
        <el-table-column prop="roleCode" label="角色编码" width="200" />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" />
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" link @click="openForm(row)">编辑</el-button>
            <el-button size="small" link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination v-model:page="query.pageNum" v-model:size="query.pageSize" :total="total" @change="load" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑角色' : '新增角色'" width="700px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="角色名称" prop="roleName"><el-input v-model="form.roleName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="角色编码" prop="roleCode"><el-input v-model="form.roleCode" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="状态">
            <el-radio-group v-model="form.status"><el-radio :value="1">启用</el-radio><el-radio :value="0">禁用</el-radio></el-radio-group>
          </el-form-item></el-col>
        </el-row>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="分配权限">
          <el-tree ref="menuTreeRef" :data="menuTreeData" :props="{ label: 'menuName', children: 'children' }" show-checkbox node-key="id" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import Pagination from '@/components/Pagination.vue'
import { rolePage, roleAdd, roleEdit, roleRemove, roleMenuIds } from '@/api/system'
import { menuTree } from '@/api/system'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '' })
const dialogVisible = ref(false)
const formRef = ref()
const menuTreeRef = ref()
const menuTreeData = ref([])
const form = reactive({ id: null, roleName: '', roleCode: '', sort: 0, status: 1, remark: '', menuIds: [] })
const rules = { roleName: [{ required: true, message: '请输入角色名称' }], roleCode: [{ required: true, message: '请输入角色编码' }] }

const load = async () => {
  loading.value = true
  try { const res = await rolePage(query); list.value = res.list; total.value = res.total }
  finally { loading.value = false }
}

const openForm = async (row) => {
  dialogVisible.value = true
  menuTreeData.value = await menuTree()
  if (row) {
    const d = row
    Object.assign(form, d)
    const ids = await roleMenuIds(row.id)
    await nextTick()
    ids.forEach(id => { const node = menuTreeRef.value.getNode(id); if (node) menuTreeRef.value.setChecked(id, true, false) })
  } else {
    Object.assign(form, { id: null, roleName: '', roleCode: '', sort: 0, status: 1, remark: '', menuIds: [] })
    await nextTick(); menuTreeRef.value.setCheckedKeys([])
  }
}

const submit = async () => {
  await formRef.value.validate()
  const checked = menuTreeRef.value.getCheckedKeys().concat(menuTreeRef.value.getHalfCheckedKeys())
  form.menuIds = [...new Set(checked)]
  if (form.id) await roleEdit(form); else await roleAdd(form)
  ElMessage.success('保存成功'); dialogVisible.value = false; load()
}

const remove = async (row) => { await ElMessageBox.confirm(`确定删除角色"${row.roleName}"？`, '提示', { type: 'warning' }); await roleRemove([row.id]); ElMessage.success('删除成功'); load() }

onMounted(load)
</script>
