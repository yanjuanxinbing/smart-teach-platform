<template>
  <div class="app-container">
    <el-card>
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input v-model="query.keyword" placeholder="课程编号/名称" clearable style="width: 220px" @keyup.enter="load" />
          <el-select v-model="query.courseType" placeholder="课程性质" clearable style="width: 140px">
            <el-option v-for="d in courseTypeOptions" :key="d.value" :label="d.label" :value="d.value" />
          </el-select>
          <el-select v-model="query.status" placeholder="状态" clearable style="width: 120px">
            <el-option label="未发布" :value="0" />
            <el-option label="已发布" :value="1" />
            <el-option label="已结课" :value="2" />
          </el-select>
          <el-button type="primary" @click="load">搜索</el-button>
          <el-button @click="reset">重置</el-button>
        </div>
        <div>
          <el-button type="primary" :icon="Plus" @click="openForm()">新增课程</el-button>
        </div>
      </div>

      <el-table :data="list" v-loading="loading" border>
        <el-table-column prop="courseCode" label="课程编号" width="140" />
        <el-table-column prop="courseName" label="课程名称" />
        <el-table-column prop="credit" label="学分" width="80" />
        <el-table-column prop="totalHours" label="学时" width="80" />
        <el-table-column label="课程性质" width="100">
          <template #default="{ row }">
            <el-tag :type="getCourseTypeTagType(row.courseType)">
              {{ getCourseTypeLabel(row.courseType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="['info','success','warning'][row.status]">{{ ['未发布','已发布','已结课'][row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" link @click="openForm(row)">编辑</el-button>
            <el-button size="small" link @click="changeStatus(row, 1)" v-if="row.status === 0">发布</el-button>
            <el-button size="small" link @click="changeStatus(row, 2)" v-if="row.status === 1">结课</el-button>
            <el-button size="small" link @click="changeStatus(row, 0)" v-if="row.status === 1">下架</el-button>
            <el-button size="small" link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination v-model:page="query.pageNum" v-model:size="query.pageSize" :total="total" @change="load" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑课程' : '新增课程'" width="640px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="课程编号" prop="courseCode"><el-input v-model="form.courseCode" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="课程名称" prop="courseName"><el-input v-model="form.courseName" /></el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="学分" prop="credit"><el-input-number v-model="form.credit" :precision="1" :min="0" controls-position="right" style="width: 100%" /></el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="总学时" prop="totalHours"><el-input-number v-model="form.totalHours" :min="0" controls-position="right" style="width: 100%" /></el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="课程性质" prop="courseType">
              <el-select v-model="form.courseType" style="width:100%" placeholder="请选择课程性质">
                <el-option v-for="d in courseTypeOptions" :key="d.value" :label="d.label" :value="d.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="课程简介">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import Pagination from '@/components/Pagination.vue'
import { useDict } from '@/hooks/useDict'
import { coursePage, courseAdd, courseEdit, courseRemove, courseChangeStatus } from '@/api/course'

const dict = useDict('course_type')
const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', courseType: null, status: null })

// 课程性质的内置兜底映射（必修 / 选修），与字典同步，字典未加载完成时也能正常显示
const COURSE_TYPE_FALLBACK = [
  { value: 1, label: '必修' },
  { value: 2, label: '选修' }
]
const COURSE_TYPE_TAG_TYPE = { 1: 'primary', 2: 'success' }

// 课程性质下拉数据：优先用字典，没有就用兜底（避免表单与筛选出现 NaN）
const courseTypeOptions = computed(() => {
  const fromDict = (dict.course_type && dict.course_type.value) || []
  if (fromDict.length) {
    return fromDict.map(d => ({
      value: Number(d.value) || 0,
      label: d.label
    })).filter(o => o.value)
  }
  return COURSE_TYPE_FALLBACK
})

const getCourseTypeLabel = (type) => {
  if (type === null || type === undefined || Number.isNaN(Number(type))) return '-'
  const t = Number(type)
  // 优先匹配字典（字典加载后会有更准确的中文标签）
  const fromDict = (dict.course_type && dict.course_type.value || []).find(d => Number(d.value) === t)
  if (fromDict) return fromDict.label
  const fallback = COURSE_TYPE_FALLBACK.find(o => o.value === t)
  return (fallback && fallback.label) || '-'
}
const getCourseTypeTagType = (type) => {
  const t = Number(type)
  if (COURSE_TYPE_TAG_TYPE[t]) return COURSE_TYPE_TAG_TYPE[t]
  const fromDict = (dict.course_type && dict.course_type.value || []).find(d => Number(d.value) === t)
  return fromDict && fromDict.raw && fromDict.raw.listClass ? fromDict.raw.listClass : 'info'
}
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref()
const form = reactive({ id: null, courseCode: '', courseName: '', credit: 0, totalHours: 0, courseType: 1, status: 0, description: '' })
const rules = {
  courseCode: [{ required: true, message: '请输入课程编号' }],
  courseName: [{ required: true, message: '请输入课程名称' }],
  courseType: [{ required: true, message: '请选择课程性质' }]
}

const load = async () => {
  loading.value = true
  try {
    const res = await coursePage(query)
    list.value = res.list
    total.value = res.total
  } finally { loading.value = false }
}

const reset = () => { query.keyword = ''; query.courseType = null; query.status = null; load() }

const openForm = (row) => {
  dialogVisible.value = true
  if (row) {
    Object.assign(form, row)
  } else {
    Object.assign(form, { id: null, courseCode: '', courseName: '', credit: 0, totalHours: 0, courseType: 1, status: 0, description: '' })
  }
}

const submit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (form.id) await courseEdit(form); else await courseAdd(form)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } finally { submitting.value = false }
}

const remove = async (row) => {
  await ElMessageBox.confirm(`确定删除课程"${row.courseName}"吗？`, '提示', { type: 'warning' })
  await courseRemove([row.id])
  ElMessage.success('删除成功')
  load()
}

const changeStatus = async (row, status) => {
  await courseChangeStatus(row.id, status)
  ElMessage.success('操作成功')
  load()
}

onMounted(load)
</script>
