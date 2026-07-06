<template>
  <div class="app-container">
    <el-card>
      <div class="toolbar">
        <span style="color: #909399">菜单/按钮权限维护</span>
        <el-button type="primary" :icon="Plus" @click="openForm()">新增</el-button>
      </div>

      <el-table :data="tree" v-loading="loading" row-key="id" border default-expand-all
        :tree-props="{ children: 'children' }">
        <el-table-column prop="menuName" label="名称" />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="['primary','success','info'][row.menuType - 1]">{{ ['目录','菜单','按钮'][row.menuType - 1] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="路由" width="200" />
        <el-table-column prop="component" label="组件路径" width="200" />
        <el-table-column prop="permission" label="权限标识" width="200" />
        <el-table-column prop="icon" label="图标" width="100" />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button size="small" link @click="openForm({ parentId: row.id, menuType: 2 })">添加子项</el-button>
            <el-button size="small" link @click="openForm(row)">编辑</el-button>
            <el-button size="small" link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑菜单' : '新增菜单'" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="类型">
          <el-radio-group v-model="form.menuType">
            <el-radio :value="1">目录</el-radio>
            <el-radio :value="2">菜单</el-radio>
            <el-radio :value="3">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="父级">
          <el-tree-select v-model="form.parentId" :data="parentOptions" :props="{ value: 'id', label: 'menuName' }" check-strictly clearable style="width:100%" />
        </el-form-item>
        <el-form-item label="名称" prop="menuName"><el-input v-model="form.menuName" /></el-form-item>
        <el-form-item label="路由" v-if="form.menuType !== 3"><el-input v-model="form.path" /></el-form-item>
        <el-form-item label="组件路径" v-if="form.menuType === 2"><el-input v-model="form.component" /></el-form-item>
        <el-form-item label="权限标识" v-if="form.menuType === 3" prop="permission"><el-input v-model="form.permission" /></el-form-item>
        <el-form-item label="图标" v-if="form.menuType !== 3"><el-input v-model="form.icon" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
        <el-form-item label="显示" v-if="form.menuType !== 3">
          <el-radio-group v-model="form.visible"><el-radio :value="1">显示</el-radio><el-radio :value="0">隐藏</el-radio></el-radio-group>
        </el-form-item>
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
import { menuTree, menuAdd, menuEdit, menuRemove } from '@/api/system'

const tree = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const formRef = ref()
const form = reactive({ id: null, parentId: 0, menuName: '', menuType: 2, path: '', component: '', icon: '', permission: '', sort: 0, visible: 1, status: 1 })
const rules = { menuName: [{ required: true, message: '请输入名称' }], permission: [{ required: true, message: '请输入权限标识' }] }

const parentOptions = computed(() => [{ id: 0, menuName: '顶级' }, ...tree.value])

const load = async () => { loading.value = true; try { tree.value = await menuTree() } finally { loading.value = false } }
const openForm = (row) => {
  dialogVisible.value = true
  if (row && row.id) Object.assign(form, row)
  else if (row) Object.assign(form, { id: null, parentId: row.parentId, menuName: '', menuType: row.menuType, path: '', component: '', icon: '', permission: '', sort: 0, visible: 1, status: 1 })
  else Object.assign(form, { id: null, parentId: 0, menuName: '', menuType: 2, path: '', component: '', icon: '', permission: '', sort: 0, visible: 1, status: 1 })
}
const submit = async () => {
  await formRef.value.validate()
  if (form.id) await menuEdit(form); else await menuAdd(form)
  ElMessage.success('保存成功'); dialogVisible.value = false; load()
}
const remove = async (row) => { await ElMessageBox.confirm(`确定删除"${row.menuName}"？`, '提示', { type: 'warning' }); await menuRemove(row.id); ElMessage.success('删除成功'); load() }

onMounted(load)
</script>
