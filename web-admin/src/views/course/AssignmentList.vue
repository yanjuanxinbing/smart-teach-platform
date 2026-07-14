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
        <el-table-column prop="title" label="作业标题" min-width="180" show-overflow-tooltip />
        <el-table-column label="所属章节" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">{{ chapterMap[row.chapterId] || ('#' + row.chapterId) }}</template>
        </el-table-column>
        <el-table-column label="目标班级" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.classIds && row.classIds.length">
              {{ row.classIds.map(id => classMap[id] || ('#'+id)).join('、') }}
            </span>
            <span v-else style="color:#bbb">未指定</span>
          </template>
        </el-table-column>
        <el-table-column prop="deadline" label="截止时间" width="170" />
        <el-table-column prop="totalScore" label="总分" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="['info','success','warning'][row.status]">{{ ['草稿','已发布','已截止'][row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="280" fixed="right">
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
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑作业' : '新增作业'" width="680px">
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
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="院系">
              <el-tree-select v-model="form.deptId" :data="deptOptions" :props="{ value: 'id', label: 'deptName', children: 'children' }"
                check-strictly clearable placeholder="选择院系（可选，用于过滤班级）" style="width:100%" @change="onFormDeptChange" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="目标班级">
              <el-select v-model="form.classIds" multiple filterable collapse-tags collapse-tags-tooltip
                :disabled="!form.deptId" placeholder="多选班级" style="width:100%">
                <el-option v-for="c in formClassOptions" :key="c.id" :label="c.className" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="作业标题" prop="title"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="作业说明">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="作业要求、提交方式等" />
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
import { classListByDept, classListAll } from '@/api/system'
import { deptTreeOptions } from '@/api/system'

const userStore = useUserStore()

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ pageNum: 1, pageSize: 10, courseId: null, chapterId: null, keyword: '', status: null, classId: null })

const courseOptions = ref([])
const chapterOptions = ref([])
const chapterMap = reactive({})
const classMap = reactive({})

// 表单内联动
const formChapterOptions = ref([])
const formClassOptions = ref([])
const deptOptions = ref([])
const deptTreeAll = ref([])

// 表单 & dialog 状态
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref()
const form = reactive({
  id: null, courseId: null, chapterId: null,
  deptId: null, classIds: [],
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
    // 收集缺失的章节标题 / 班级名
    const missingChapterIds = [...new Set(res.list.map(r => r.chapterId).filter(id => id && !chapterMap[id]))]
    if (missingChapterIds.length) {
      for (const c of courseOptions.value) {
        const chs = await chapterList(c.id)
        chs.forEach(ch => { if (!chapterMap[ch.id]) chapterMap[ch.id] = ch.chapterTitle })
      }
    }
    const missingClassIds = [...new Set(res.list.flatMap(r => r.classIds || []).filter(id => !classMap[id]))]
    if (missingClassIds.length) {
      const all = await classListAll()
      all.forEach(c => { classMap[c.id] = c.className })
    }
  } finally { loading.value = false }
}

const reset = () => {
  query.keyword = ''
  query.status = null
  query.courseId = null
  query.chapterId = null
  query.classId = null
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

const onFormDeptChange = async (deptId) => {
  form.classIds = []
  formClassOptions.value = deptId ? await classListByDept(deptId) : []
}

const openForm = (row) => {
  dialogVisible.value = true
  if (row) {
    Object.assign(form, {
      id: row.id, courseId: row.courseId, chapterId: row.chapterId,
      deptId: null, classIds: Array.isArray(row.classIds) ? [...row.classIds] : [],
      title: row.title, description: row.description, deadline: row.deadline,
      totalScore: row.totalScore, status: row.status
    })
    onFormCourseChange(row.courseId).then(() => {
      form.chapterId = row.chapterId
      // 编辑回填：班级可能跨多个系，这里从全量班级里反查所属 deptId，再加载该 dept 下拉
      if (form.classIds && form.classIds.length) {
        const allClasses = Object.values(classMap).length ? [] : null // 占位，下面异步查询
        // 简单做法：直接从 classListAll 里找 classIds[0] 对应的 deptId
        classListAll().then(all => {
          const first = all.find(c => c.id === form.classIds[0])
          if (first) {
            form.deptId = first.deptId
            return classListByDept(first.deptId)
          }
          return []
        }).then(list => {
          formClassOptions.value = list
        })
      }
    })
  } else {
    Object.assign(form, {
      id: null, courseId: null, chapterId: null,
      deptId: null, classIds: [],
      title: '', description: '', deadline: null, totalScore: 100, status: 0
    })
    formChapterOptions.value = []
    formClassOptions.value = []
  }
}

const submit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    const payload = { ...form, classIds: form.classIds || [] }
    if (form.id) await assignEdit(payload); else await assignAdd(payload)
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
  // 用轻量版部门树（不含 PII），避免非管理员账号 403
  deptTreeAll.value = await deptTreeOptions()
  // 仅展示二级部门（系）作为院系下拉
  const flatten = (nodes, depth = 0) => {
    let out = []
    for (const n of nodes) {
      if (depth === 1) out.push({ ...n, children: [] })
      else if (n.children && n.children.length) out = out.concat(flatten(n.children, depth + 1))
    }
    return out
  }
  deptOptions.value = flatten(deptTreeAll.value)
  // 顺手预热 classMap
  const allClasses = await classListAll()
  allClasses.forEach(c => { classMap[c.id] = c.className })
  load()
})
</script>