<template>
  <div class="app-container">
    <el-card>
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input v-model="query.keyword" placeholder="课程名 / 教师姓名" clearable style="width: 220px" @keyup.enter="load" />
          <el-select v-model="query.status" placeholder="状态" clearable style="width: 110px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
          <el-button type="primary" @click="load">搜索</el-button>
          <el-button @click="reset">重置</el-button>
        </div>
        <el-button type="primary" :icon="Plus" v-if="userStore.hasAuthority('teach:add')" @click="openAssignDialog()">分配授课</el-button>
      </div>

      <el-table :data="list" v-loading="loading" border>
        <el-table-column prop="courseName" label="课程" min-width="200" show-overflow-tooltip />
        <el-table-column prop="teacherName" label="教师" min-width="120" />
        <el-table-column label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="row.role === '助教' ? 'info' : 'success'" size="small">{{ row.role || '主讲' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-switch
              v-if="userStore.hasAuthority('teach:edit')"
              :model-value="row.status === 1"
              @change="(val) => changeStatus(row, val)"
            />
            <el-tag v-else :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="分配时间" width="170" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button size="small" link type="danger" v-if="userStore.hasAuthority('teach:remove')" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination v-model:page="query.pageNum" v-model:size="query.pageSize" :total="total" @change="load" />
    </el-card>

    <!-- 分配授课对话框：选课程 + 选多个教师（含主讲/助教） -->
    <el-dialog v-model="assignDialog" title="分配授课教师" width="780px" destroy-on-close>
      <el-form ref="assignFormRef" :model="assignForm" :rules="assignRules" label-width="100px">
        <el-form-item label="课程" prop="courseId">
          <el-select v-model="assignForm.courseId" filterable placeholder="选择课程" style="width: 100%" @change="onCourseChange">
            <el-option v-for="c in courseOptions" :key="c.id" :label="`${c.courseCode} ${c.courseName}`" :value="c.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="教师列表">
          <div class="teacher-toolbar">
            <el-input v-model="teacherKeyword" placeholder="搜索教师姓名/账号" clearable style="width: 200px" @input="filterTeachers" />
            <el-button @click="addAllVisible" :disabled="!filteredTeachers.length">加入</el-button>
          </div>
          <el-table :data="filteredTeachers" max-height="180" border @selection-change="onPickedTeachers">
            <el-table-column type="selection" width="44" />
            <el-table-column prop="realName" label="姓名" />
            <el-table-column prop="username" label="账号" />
          </el-table>
        </el-form-item>

        <el-form-item label="已选教师" prop="teachers">
          <el-table :data="assignForm.teachers" max-height="220" border>
            <el-table-column prop="realName" label="姓名" width="120" />
            <el-table-column prop="username" label="账号" width="120" />
            <el-table-column label="角色" width="140">
              <template #default="{ row }">
                <el-select v-model="row.role" size="small">
                  <el-option label="主讲" value="主讲" />
                  <el-option label="助教" value="助教" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80" fixed="right">
              <template #default="{ $index }">
                <el-button size="small" link type="danger" @click="assignForm.teachers.splice($index, 1)">移除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignDialog = false">取消</el-button>
        <el-button type="primary" :loading="assigning" @click="submitAssign">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import Pagination from '@/components/Pagination.vue'
import { useUserStore } from '@/store/user'
import { teachPage, teachAssign, teachChangeStatus, teachRemove, userListByRole } from '@/api/teaching'
import { listAllCourses } from '@/api/course'

const userStore = useUserStore()

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', status: null })

const load = async () => {
  loading.value = true
  try {
    const res = await teachPage(query)
    list.value = res.list
    total.value = res.total
  } finally { loading.value = false }
}

const reset = () => {
  query.keyword = ''
  query.status = null
  query.pageNum = 1
  load()
}

const changeStatus = async (row, val) => {
  await teachChangeStatus(row.id, val ? 1 : 0)
  ElMessage.success('已更新')
  load()
}

const remove = async (row) => {
  await ElMessageBox.confirm(`确定解除「${row.courseName}」的「${row.teacherName}」授课关系？`, '提示', { type: 'warning' })
  await teachRemove([row.id])
  ElMessage.success('已删除')
  load()
}

// 分配对话框
const assignDialog = ref(false)
const assigning = ref(false)
const courseOptions = ref([])
const teacherOptions = ref([])
const teacherKeyword = ref('')
const filteredTeachers = computed(() => {
  const kw = teacherKeyword.value.trim().toLowerCase()
  if (!kw) return teacherOptions.value
  return teacherOptions.value.filter(t =>
    (t.realName && t.realName.toLowerCase().includes(kw)) ||
    (t.username && t.username.toLowerCase().includes(kw)))
})
const assignFormRef = ref()
const assignForm = reactive({ courseId: null, teachers: [] })
const assignRules = {
  courseId: [{ required: true, message: '请选择课程' }],
  teachers: [{
    validator: (_, val, cb) => val && val.length > 0 ? cb() : cb(new Error('至少选择一位教师')),
    trigger: 'change'
  }]
}

const openAssignDialog = async () => {
  assignForm.courseId = null
  assignForm.teachers = []
  teacherKeyword.value = ''
  assignDialog.value = true
  // 拉课程 + 教师选项
  await Promise.all([
    (async () => { courseOptions.value = await listAllCourses() })(),
    (async () => { teacherOptions.value = await userListByRole('ROLE_TEACHER') })()
  ])
}

const onCourseChange = () => {
  // 切换课程时清空已选教师，避免跨课程残留
  assignForm.teachers = []
}

const filterTeachers = () => { /* computed 自动响应 */ }

const addAllVisible = () => {
  const existing = new Set(assignForm.teachers.map(t => t.id))
  for (const t of filteredTeachers.value) {
    if (!existing.has(t.id)) {
      assignForm.teachers.push({ id: t.id, realName: t.realName, username: t.username, role: '主讲', sort: assignForm.teachers.length })
      existing.add(t.id)
    }
  }
}

const onPickedTeachers = (rows) => {
  // 多选框勾选 → 整批添加（不去重已存在，由 addAllVisible 配合）
  const existing = new Set(assignForm.teachers.map(t => t.id))
  for (const t of rows) {
    if (!existing.has(t.id)) {
      assignForm.teachers.push({ id: t.id, realName: t.realName, username: t.username, role: '主讲', sort: assignForm.teachers.length })
      existing.add(t.id)
    }
  }
}

const submitAssign = async () => {
  await assignFormRef.value.validate()
  assigning.value = true
  try {
    const teachers = assignForm.teachers.map((t, idx) => ({
      teacherId: t.id,
      role: t.role || '主讲',
      sort: typeof t.sort === 'number' ? t.sort : idx
    }))
    await teachAssign({ courseId: assignForm.courseId, teachers })
    ElMessage.success('分配成功')
    assignDialog.value = false
    load()
  } finally { assigning.value = false }
}

onMounted(load)
</script>

<style scoped>
.toolbar { display: flex; align-items: center; justify-content: space-between; gap: 12px; margin-bottom: 8px; flex-wrap: wrap; }
.toolbar-left { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.teacher-toolbar { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
</style>