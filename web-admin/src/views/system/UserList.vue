<template>
  <div class="app-container">
    <el-card>
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input v-model="query.username" placeholder="账号" clearable style="width: 180px" @keyup.enter="load" />
          <el-input v-model="query.realName" placeholder="姓名" clearable style="width: 180px" @keyup.enter="load" />
          <el-select v-model="query.status" placeholder="状态" clearable style="width: 120px">
            <el-option label="启用" :value="1" /><el-option label="禁用" :value="0" />
          </el-select>
          <el-button type="primary" @click="load">搜索</el-button>
        </div>
        <el-button type="primary" :icon="Plus" @click="openForm()">新增用户</el-button>
      </div>

      <el-table :data="list" v-loading="loading" border>
        <el-table-column prop="username" label="账号" width="160" />
        <el-table-column prop="realName" label="姓名" width="120" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="email" label="邮箱" width="200" />
        <el-table-column prop="roleNames" label="角色" width="200">
          <template #default="{ row }">
            <el-tag v-for="r in row.roleNames" :key="r" style="margin-right: 4px">{{ r }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-switch :model-value="row.status === 1" @change="(v) => changeStatus(row, v ? 1 : 0)" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button size="small" link @click="openForm(row)">编辑</el-button>
            <el-button size="small" link @click="resetPwd(row)">重置密码</el-button>
            <el-button size="small" link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination v-model:page="query.pageNum" v-model:size="query.pageSize" :total="total" @change="load" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑用户' : '新增用户'" width="640px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="账号" prop="username"><el-input v-model="form.username" :disabled="!!form.id" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="姓名" prop="realName"><el-input v-model="form.realName" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16" v-if="!form.id">
          <el-col :span="12"><el-form-item label="密码"><el-input v-model="form.password" placeholder="留空使用默认密码" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="邮箱" prop="email"><el-input v-model="form.email" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="性别">
            <el-radio-group v-model="form.gender"><el-radio :value="1">男</el-radio><el-radio :value="2">女</el-radio><el-radio :value="0">未知</el-radio></el-radio-group>
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="状态">
            <el-radio-group v-model="form.status"><el-radio :value="1">启用</el-radio><el-radio :value="0">禁用</el-radio></el-radio-group>
          </el-form-item></el-col>
        </el-row>
        <el-form-item label="分配角色" prop="roleIds">
          <el-checkbox-group v-model="form.roleIds">
            <el-checkbox v-for="r in roleList" :key="r.id" :value="r.id">{{ r.roleName }}</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import Pagination from '@/components/Pagination.vue'
import { userPage, userDetail, userAdd, userEdit, userRemove, userResetPassword, userChangeStatus } from '@/api/system'
import { roleList } from '@/api/system'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ pageNum: 1, pageSize: 10, username: '', realName: '', status: null })
const roleList_ = ref([])
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref()
const form = reactive({ id: null, username: '', password: '', realName: '', phone: '', email: '', gender: 0, status: 1, remark: '', roleIds: [] })
const rules = { username: [{ required: true, message: '请输入账号' }], realName: [{ required: true, message: '请输入姓名' }], email: [{ type: 'email', message: '邮箱格式不正确' }], roleIds: [{ type: 'array', required: true, message: '请选择角色' }] }

const load = async () => {
  loading.value = true
  try { const res = await userPage(query); list.value = res.list; total.value = res.total }
  finally { loading.value = false }
}

const openForm = async (row) => {
  dialogVisible.value = true
  if (row) { const d = await userDetail(row.id); Object.assign(form, d); form.password = '' }
  else { Object.assign(form, { id: null, username: '', password: '', realName: '', phone: '', email: '', gender: 0, status: 1, remark: '', roleIds: [] }) }
}

const submit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try { if (form.id) await userEdit(form); else await userAdd(form); ElMessage.success('保存成功'); dialogVisible.value = false; load() }
  finally { submitting.value = false }
}

const remove = async (row) => { await ElMessageBox.confirm(`确定删除用户"${row.username}"？`, '提示', { type: 'warning' }); await userRemove([row.id]); ElMessage.success('删除成功'); load() }
const resetPwd = async (row) => {
  const { value } = await ElMessageBox.prompt('请输入新密码（留空将重置为 123456）', '重置密码', { inputValue: '123456' })
  await userResetPassword(row.id, value || '123456')
  ElMessage.success('密码已重置')
}
const changeStatus = async (row, status) => { await userChangeStatus(row.id, status); ElMessage.success('操作成功'); load() }

onMounted(async () => { roleList_.value = await roleList(); load() })
</script>
