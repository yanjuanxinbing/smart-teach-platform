<template>
  <div class="app-container">
    <el-card>
      <div class="toolbar">
        <div class="toolbar-left">
          <el-select v-model="query.courseId" placeholder="课程" clearable filterable style="width: 200px" @change="onCourseFilterChange">
            <el-option v-for="c in courseOptions" :key="c.id" :label="`${c.courseCode} ${c.courseName}`" :value="c.id" />
          </el-select>
          <el-select v-model="query.chapterId" placeholder="章节" clearable filterable style="width: 200px" :disabled="!query.courseId">
            <el-option v-for="ch in chapterOptions" :key="ch.id" :label="ch.chapterTitle" :value="ch.id" />
          </el-select>
          <el-input v-model="query.keyword" placeholder="标题关键字" clearable style="width: 180px" @keyup.enter="load" />
          <el-select v-model="query.status" placeholder="状态" clearable style="width: 120px">
            <el-option label="草稿" :value="0" />
            <el-option label="已发布" :value="1" />
            <el-option label="已截止" :value="2" />
          </el-select>
          <el-button type="primary" @click="load">搜索</el-button>
          <el-button @click="reset">重置</el-button>
        </div>
        <el-button type="primary" :icon="Plus" v-if="userStore.hasAuthority('assignment:add')" @click="openForm()">新增作业</el-button>
      </div>

      <el-table :data="list" v-loading="loading" border>
        <el-table-column prop="title" label="作业标题" min-width="200" show-overflow-tooltip />
        <el-table-column label="所属章节" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">{{ chapterMap[row.chapterId] || ('#' + row.chapterId) }}</template>
        </el-table-column>
        <el-table-column prop="deadline" label="截止时间" width="170" />
        <el-table-column prop="totalScore" label="总分" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="['info','success','warning'][row.status]">{{ ['草稿','已发布','已截止'][row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="320" fixed="right">
          <template #default="{ row }">
            <el-button size="small" link v-if="userStore.hasAuthority('assignment:edit')" @click="openForm(row)">编辑</el-button>
            <el-button size="small" link v-if="userStore.hasAuthority('assignment:publish') && row.status === 0" @click="changeStatus(row, 'publish')">发布</el-button>
            <el-button size="small" link v-if="userStore.hasAuthority('assignment:close') && row.status === 1" @click="changeStatus(row, 'close')">截止</el-button>
            <el-button size="small" link type="danger" v-if="userStore.hasAuthority('assignment:remove')" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination v-model:page="query.pageNum" v-model:size="query.pageSize" :total="total" @change="load" />
    </el-card>

    <!-- 新增/编辑作业 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑作业' : '新增作业'" width="640px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="关联课程" prop="courseId">
          <el-select v-model="form.courseId" filterable style="width:100%" @change="onFormCourseChange">
            <el-option v-for="c in courseOptions" :key="c.id" :label="`${c.courseCode} ${c.courseName}`" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属章节" prop="chapterId">
          <el-select v-model="form.chapterId" filterable style="width:100%" :disabled="!form.courseId">
            <el-option v-for="ch in formChapterOptions" :key="ch.id" :label="ch.chapterTitle" :value="ch.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="作业标题" prop="title"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="作业说明">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="作业要求、提交方式等" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="截止时间" prop="deadline">
              <el-date-picker v-model="form.deadline" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" placeholder="选择日期时间" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="总分" prop="totalScore">
              <el-input-number v-model="form.totalScore" :min="0" :max="100" :precision="2" controls-position="right" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
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
import { useUserStore } from '@/store/user'
import { myCourses, chapterList } from '@/api/course'
import {
  assignPage, assignAdd, assignEdit, assignRemove, assignPublish, assignClose
} from '@/api/assignment'

const userStore = useUserStore()

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ pageNum: 1, pageSize: 10, courseId: null, chapterId: null, keyword: '', status: null })

const courseOptions = ref([])
const chapterOptions = ref([])
const chapterMap = reactive({})

// 表单内联动
const formChapterOptions = ref([])

// 表单 & dialog 状态
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref()
const form = reactive({
  id: null, courseId: null, chapterId: null,
  title: '', description: '', deadline: null, totalScore: 100, status: 0
})
const rules = {
  courseId: [{ required: true, message: '请选择课程' }],
  chapterId: [{ required: true, message: '请选择章节' }],
  title: [{ required: true, message: '请输入作业标题' }],
  deadline: [{ required: true, message: '请选择截止时间' }]
}

const load = async () => {
  loading.value = true
  try {
    const res = await assignPage(query)
    list.value = res.list
    total.value = res.total
    // 把作业列表里出现的章节标题缓存进 chapterMap（id → title），列表"所属章节"列直接查
    const missingIds = [...new Set(res.list.map(r => r.chapterId).filter(id => id && !chapterMap[id]))]
    if (missingIds.length) {
      // 对每个未命中的 chapterId，反查它所在课程 → 拉该课全部章节补齐
      // 简单做法：对每个课程拉一次 chapterList
      for (const c of courseOptions.value) {
        const chs = await chapterList(c.id)
        chs.forEach(ch => { if (!chapterMap[ch.id]) chapterMap[ch.id] = ch.chapterTitle })
      }
    }
  } finally { loading.value = false }
}

const reset = () => {
  query.keyword = ''
  query.status = null
  query.courseId = null
  query.chapterId = null
  chapterOptions.value = []
  load()
}

const onCourseFilterChange = async (courseId) => {
  query.chapterId = null
  chapterOptions.value = courseId ? await chapterList(courseId) : []
}

const onFormCourseChange = async (courseId) => {
  form.chapterId = null
  formChapterOptions.value = courseId ? await chapterList(courseId) : []
}

const openForm = (row) => {
  dialogVisible.value = true
  if (row) {
    Object.assign(form, {
      id: row.id, courseId: row.courseId, chapterId: row.chapterId,
      title: row.title, description: row.description, deadline: row.deadline,
      totalScore: row.totalScore, status: row.status
    })
    // 回填联动：先加载该课程的章节，再把 form.chapterId 设回去
    onFormCourseChange(row.courseId).then(() => {
      form.chapterId = row.chapterId
    })
  } else {
    Object.assign(form, {
      id: null, courseId: null, chapterId: null,
      title: '', description: '', deadline: null, totalScore: 100, status: 0
    })
    formChapterOptions.value = []
  }
}

const submit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (form.id) await assignEdit(form); else await assignAdd(form)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } finally { submitting.value = false }
}

const remove = async (row) => {
  await ElMessageBox.confirm(`确定删除作业"${row.title}"吗？`, '提示', { type: 'warning' })
  await assignRemove([row.id])
  ElMessage.success('删除成功')
  load()
}

const changeStatus = async (row, action) => {
  const verb = action === 'publish' ? '发布' : '截止'
  await ElMessageBox.confirm(`确定${verb}作业"${row.title}"？`, '提示', { type: 'warning' })
  if (action === 'publish') await assignPublish(row.id); else await assignClose(row.id)
  ElMessage.success(`${verb}成功`)
  load()
}

onMounted(async () => {
  courseOptions.value = await myCourses()
  load()
})
</script>
