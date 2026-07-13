<template>
  <div class="app-container">
    <el-card>
      <div class="toolbar">
        <span style="color: #909399">班级管理（隶属部门）</span>
        <div class="toolbar-left">
          <el-cascader
            v-model="cascaderValue"
            :options="deptCascaderOptions"
            :props="{ value: 'id', label: 'deptName', children: 'children', checkStrictly: true, emitPath: false }"
            clearable
            placeholder="按院系筛选"
            style="width: 240px"
            @change="onCascaderChange"
          />
          <el-input v-model="query.keyword" placeholder="班级名称" clearable style="width: 200px" @keyup.enter="load" />
          <el-button type="primary" @click="load">搜索</el-button>
        </div>
        <el-button type="primary" :icon="Plus" v-if="userStore.hasAuthority('class:add')" @click="openForm()">新增班级</el-button>
      </div>

      <el-table :data="list" v-loading="loading" border>
        <el-table-column prop="className" label="班级名称" min-width="160" />
        <el-table-column prop="grade" label="年级" width="120" />
        <el-table-column prop="deptName" label="所属部门" min-width="160" />
        <el-table-column prop="memberCount" label="成员数" width="100" align="center" />
        <el-table-column prop="sort" label="排序" width="80" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button size="small" link v-if="userStore.hasAuthority('class:edit')" @click="openForm(row)">编辑</el-button>
            <el-button size="small" link v-if="userStore.hasAuthority('class:member:assign')" @click="openMembersDialog(row)">分配成员</el-button>
            <el-button size="small" link type="danger" v-if="userStore.hasAuthority('class:remove')" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination v-model:page="query.pageNum" v-model:size="query.pageSize" :total="total" @change="load" />
    </el-card>

    <!-- 新增 / 编辑班级 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑班级' : '新增班级'" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="班级名称" prop="className"><el-input v-model="form.className" placeholder="如 计科2201班" /></el-form-item>
        <el-form-item label="年级"><el-input v-model="form.grade" placeholder="如 2022级" /></el-form-item>
        <el-form-item label="所属部门" prop="deptId">
          <el-tree-select v-model="form.deptId" :data="deptOptions" :props="{ value: 'id', label: 'deptName', children: 'children' }"
            check-strictly clearable placeholder="选择院系" style="width:100%" />
        </el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status"><el-radio :value="1">启用</el-radio><el-radio :value="0">禁用</el-radio></el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 分配成员 dialog：左未分配 / 右已分配 -->
    <el-dialog v-model="membersDialog" :title="`分配成员 - ${currentRow?.className || ''}`" width="1100px" destroy-on-close>
      <div class="members-toolbar">
        <el-radio-group v-model="memberRole" @change="loadMembers">
          <el-radio-button label="教师">教师</el-radio-button>
          <el-radio-button label="学生">学生</el-radio-button>
        </el-radio-group>
        <el-input v-model="memberKeyword" placeholder="搜索用户名/姓名" clearable style="width: 240px" @input="loadAvailableUsers" />
      </div>

      <div class="members-grid">
        <!-- 左：可分配 -->
        <div class="members-col">
          <div class="members-col-title">可选用户</div>
          <el-table :data="availableUsers" v-loading="availableLoading" max-height="380" @selection-change="onAvailableSelect">
            <el-table-column type="selection" width="44" />
            <el-table-column prop="username" label="账号" width="100" />
            <el-table-column prop="realName" label="姓名" />
            <el-table-column label="角色" width="180">
              <template #default="{ row }">
                <el-tag v-for="r in row.roleNames || []" :key="r" size="small" style="margin-right:4px">{{ r }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 中间箭头 -->
        <div class="members-arrow">
          <el-button type="primary" :icon="ArrowRight" :disabled="!pickedAvailable.length" @click="addToClass" />
        </div>

        <!-- 右：已分配 -->
        <div class="members-col">
          <div class="members-col-title">已分配用户</div>
          <el-table :data="assignedUsers" v-loading="assignedLoading" max-height="380">
            <el-table-column prop="username" label="账号" width="100" />
            <el-table-column prop="realName" label="姓名" />
            <el-table-column label="角色" width="180">
              <template #default="{ row }">
                <el-tag v-for="r in row.roleNames || []" :key="r" size="small" style="margin-right:4px">{{ r }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80" fixed="right">
              <template #default="{ row }">
                <el-button size="small" link type="danger" @click="removeFromClass(row)">移除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <template #footer>
        <el-button @click="membersDialog = false">取消</el-button>
        <el-button type="primary" :loading="assignSubmitting" @click="saveMembers">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, ArrowRight } from '@element-plus/icons-vue'
import Pagination from '@/components/Pagination.vue'
import { useUserStore } from '@/store/user'
import { useDict } from '@/hooks/useDict'
import {
  classPage, classAdd, classEdit, classRemove,
  classListMembers, classAssignMembers
} from '@/api/system'
import { deptTree } from '@/api/system'
import { userPage } from '@/api/system'

const userStore = useUserStore()

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ pageNum: 1, pageSize: 10, deptId: null, keyword: '' })
const cascaderValue = ref(null)
const deptTreeAll = ref([])

const deptOptions = computed(() => deptTreeAll.value)
// el-cascader 选项：每个节点都保留 children=[] 才能正确渲染
const deptCascaderOptions = computed(() => {
  const walk = (nodes) => nodes.map(n => ({
    ...n,
    children: n.children && n.children.length ? walk(n.children) : []
  }))
  return walk(deptTreeAll.value)
})
const onCascaderChange = (val) => {
  query.deptId = val || null
  load()
}

const load = async () => {
  loading.value = true
  try {
    const res = await classPage(query)
    list.value = res.list
    total.value = res.total
  } finally { loading.value = false }
}

const loadDeptTree = async () => {
  deptTreeAll.value = await deptTree()
}

// CRUD
const dialogVisible = ref(false)
const formRef = ref()
const form = reactive({ id: null, className: '', grade: '', deptId: null, sort: 0, status: 1 })
const rules = {
  className: [{ required: true, message: '请输入班级名称' }],
  deptId: [{ required: true, message: '请选择所属部门' }]
}
const openForm = (row) => {
  dialogVisible.value = true
  if (row && row.id) {
    Object.assign(form, row)
  } else {
    Object.assign(form, { id: null, className: '', grade: '', deptId: null, sort: 0, status: 1 })
  }
}
const submit = async () => {
  await formRef.value.validate()
  if (form.id) await classEdit(form); else await classAdd(form)
  ElMessage.success('保存成功')
  dialogVisible.value = false
  load()
}
const remove = async (row) => {
  await ElMessageBox.confirm(`确定删除班级"${row.className}"？`, '提示', { type: 'warning' })
  await classRemove([row.id])
  ElMessage.success('删除成功')
  load()
}

// 分配成员
const membersDialog = ref(false)
const currentRow = ref(null)
const memberRole = ref('学生')
const memberKeyword = ref('')
const availableUsers = ref([])
const assignedUsers = ref([])
const availableLoading = ref(false)
const assignedLoading = ref(false)
const assignSubmitting = ref(false)
const pickedAvailable = ref([])

const openMembersDialog = async (row) => {
  currentRow.value = row
  membersDialog.value = true
  memberKeyword.value = ''
  memberRole.value = '学生'
  await Promise.all([loadAssignedUsers(), loadAvailableUsers()])
}

// radio 切换时同时刷新两栏（已分配按 role 过滤，可选按 role + 关键字过滤）
const loadMembers = async () => {
  await Promise.all([loadAssignedUsers(), loadAvailableUsers()])
}

const loadAssignedUsers = async () => {
  if (!currentRow.value) return
  assignedLoading.value = true
  try {
    assignedUsers.value = await classListMembers(currentRow.value.id, memberRole.value || null)
  } finally { assignedLoading.value = false }
}

const loadAvailableUsers = async () => {
  if (!currentRow.value) return
  availableLoading.value = true
  try {
    // 拉所有用户（前端再做角色 + 关键字 + 已分配 三重过滤）
    const res = await userPage({ pageNum: 1, pageSize: 500, keyword: '' })
    let all = res.list || []
    // 角色过滤：UserVO.roleNames 里是中文名（"教师"/"学生"）
    if (memberRole.value) {
      all = all.filter(u => (u.roleNames || []).includes(memberRole.value))
    }
    // 关键字过滤
    if (memberKeyword.value) {
      const kw = memberKeyword.value.toLowerCase()
      all = all.filter(u =>
        (u.username && u.username.toLowerCase().includes(kw)) ||
        (u.realName && u.realName.toLowerCase().includes(kw)))
    }
    // 排除已分配的
    const assignedIds = new Set(assignedUsers.value.map(u => u.id))
    availableUsers.value = all.filter(u => !assignedIds.has(u.id))
  } finally { availableLoading.value = false }
}

const onAvailableSelect = (rows) => { pickedAvailable.value = rows }

const addToClass = () => {
  // 把勾选的用户移到右栏
  const ids = new Set(assignedUsers.value.map(u => u.id))
  for (const u of pickedAvailable.value) {
    if (!ids.has(u.id)) {
      assignedUsers.value.push(u)
      ids.add(u.id)
    }
  }
  availableUsers.value = availableUsers.value.filter(u => !pickedAvailable.value.find(p => p.id === u.id))
  pickedAvailable.value = []
}

const removeFromClass = (row) => {
  assignedUsers.value = assignedUsers.value.filter(u => u.id !== row.id)
  // 同时回到可分配栏，方便继续分配
  const inAvailable = new Set(availableUsers.value.map(u => u.id))
  if (!inAvailable.has(row.id)) {
    availableUsers.value.push(row)
  }
}

const saveMembers = async () => {
  if (!currentRow.value) return
  assignSubmitting.value = true
  try {
    await classAssignMembers({
      classId: currentRow.value.id,
      userIds: assignedUsers.value.map(u => u.id),
      roleName: null
    })
    ElMessage.success('成员分配成功')
    membersDialog.value = false
    load()
  } finally { assignSubmitting.value = false }
}

onMounted(async () => {
  await loadDeptTree()
  load()
})
</script>

<style scoped>
.toolbar { display: flex; align-items: center; justify-content: space-between; gap: 12px; margin-bottom: 8px; flex-wrap: wrap; }
.toolbar-left { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }

.members-toolbar { display: flex; align-items: center; justify-content: space-between; gap: 12px; margin-bottom: 12px; }
.members-grid { display: grid; grid-template-columns: minmax(0, 1fr) 56px minmax(0, 1fr); gap: 12px; align-items: stretch; }
.members-col { border: 1px solid var(--el-border-color-lighter); border-radius: 6px; padding: 8px; background: #fff; min-width: 0; overflow-x: auto; }
.members-col-title { font-size: 13px; color: #909399; padding: 4px 6px 8px; border-bottom: 1px solid var(--el-border-color-lighter); margin-bottom: 6px; }
.members-arrow { display: flex; align-items: center; justify-content: center; }
</style>