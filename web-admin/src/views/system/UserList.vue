<template>
  <div class="app-container">
    <el-card>
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input v-model="query.username" placeholder="账号" clearable style="width: 180px" @keyup.enter="load" />
          <el-input v-model="query.realName" placeholder="姓名" clearable style="width: 180px" @keyup.enter="load" />
          <el-select v-model="query.status" placeholder="状态" clearable style="width: 120px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
          <el-button type="primary" @click="load">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </div>
        <div class="toolbar-right">
          <el-button type="success" :icon="Upload" @click="openImportDialog">表格导入</el-button>
          <el-button type="primary" :icon="Plus" @click="openForm()">新增用户</el-button>
        </div>
      </div>

      <el-table :data="safeList" v-loading="loading" border>
        <el-table-column prop="username" label="账号" width="160" />
        <el-table-column prop="realName" label="姓名" width="120" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="email" label="邮箱" width="200" />
        <el-table-column label="角色" width="200">
          <template #default="{ row }">
            <template v-if="Array.isArray(row.roleNames) && row.roleNames.length">
              <el-tag v-for="r in row.roleNames" :key="r" style="margin-right: 4px">{{ r }}</el-tag>
            </template>
            <span v-else>-</span>
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
          <el-col :span="12">
            <el-form-item label="账号" prop="username">
              <el-input v-model="form.username" :disabled="!!form.id" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="realName">
              <el-input v-model="form.realName" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16" v-if="!form.id">
          <el-col :span="12">
            <el-form-item label="密码">
              <el-input v-model="form.password" placeholder="留空使用默认密码" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" maxlength="11" placeholder="请输入11位手机号"
                @input="form.phone = (form.phone || '').replace(/\D/g, '').slice(0, 11)" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="性别">
              <el-radio-group v-model="form.gender">
                <el-radio :value="1">男</el-radio>
                <el-radio :value="2">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-radio-group v-model="form.status">
                <el-radio :value="1">启用</el-radio>
                <el-radio :value="0">禁用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="分配角色" prop="roleIds">
          <el-radio-group v-model="form.roleIds">
            <el-radio v-for="r in roleOptions" :key="r.id" :value="r.id">{{ r.roleName }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 表格导入（CSV：姓名 / 手机号 / 邮箱 / 角色；账号 + 密码自动生成） -->
    <el-dialog v-model="importVisible" title="表格导入用户" width="900px" destroy-on-close>
      <el-alert
        type="info" :closable="false" show-icon style="margin-bottom: 12px"
        title="使用说明"
      >
        <template #default>
          1. 文件必须是 <b>CSV</b> 格式（Excel 可"另存为 CSV"导出）；<br>
          2. 表头必须包含：<b>姓名、手机号、邮箱、角色</b>（列顺序不限，可中文/英文表头）；<br>
          3. "角色"列填写 <b>教师</b> 或 <b>学生</b>；<br>
          4. <b>账号</b>自动按"角色首字母+手机号"生成，<b>密码</b>固定为初始值 <b>123456</b>；<br>
          5. 导入成功后，账号会在结果面板中显示，可下载导出留档。
        </template>
      </el-alert>

      <el-upload
        v-if="importRows.length === 0"
        :auto-upload="false"
        :show-file-list="false"
        accept=".csv"
        :on-change="handleCsvChange"
        drag
        style="margin-bottom: 12px"
      >
        <el-icon class="el-icon--upload"><Plus /></el-icon>
        <div class="el-upload__text">将 CSV 文件拖到此处，或<em>点击选择</em></div>
      </el-upload>

      <div v-else>
        <div class="import-summary">
          共 {{ importRows.length }} 行，
          <el-tag type="success" size="small">有效 {{ importRows.filter(r => r.valid).length }}</el-tag>
          <el-tag type="danger" size="small" style="margin-left: 6px">无效 {{ importRows.filter(r => !r.valid).length }}</el-tag>
          <el-button link type="primary" @click="resetImport" style="margin-left: 12px">重新选择文件</el-button>
        </div>

        <el-table :data="importRows" v-loading="importPreviewLoading" max-height="320" border>
          <el-table-column type="index" label="#" width="50" />
          <el-table-column prop="realName" label="姓名" width="120" />
          <el-table-column prop="phone" label="手机号" width="130" />
          <el-table-column prop="email" label="邮箱" min-width="180" />
          <el-table-column label="角色" width="80">
            <template #default="{ row }">
              <el-tag size="small" :type="row.roleCode === 'ROLE_TEACHER' ? 'primary' : 'success'">
                {{ row.roleCode === 'ROLE_TEACHER' ? '教师' : '学生' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="账号（自动生成）" width="180">
            <template #default="{ row }">{{ row.username || '-' }}</template>
          </el-table-column>
          <el-table-column label="密码" width="100">
            <template #default="{ row }">
              <span v-if="row.valid">123456</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="校验" min-width="160">
            <template #default="{ row }">
              <el-tag v-if="row.valid" type="success" size="small">通过</el-tag>
              <el-tag v-else type="danger" size="small">{{ row.error || '校验失败' }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <template #footer>
        <el-button @click="importVisible = false">取消</el-button>
        <el-button v-if="importRows.length && !importDone" type="primary" :loading="importSubmitting" :disabled="!hasValidRow" @click="submitImport">
          开始导入（{{ validRowCount }} 条）
        </el-button>
        <el-button v-if="importDone && importResult.length" @click="downloadResult">导出结果</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Upload } from '@element-plus/icons-vue'
import Pagination from '@/components/Pagination.vue'
import {
  userPage, userAdd, userEdit, userRemove,
  userResetPassword, userChangeStatus, roleList
} from '@/api/system'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ pageNum: 1, pageSize: 10, username: '', realName: '', status: null })
const roleOptions = ref([])
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref()
const form = reactive({
  id: null, username: '', password: '', realName: '',
  phone: '', email: '', gender: 1, status: 1, remark: '', roleIds: null
})
const rules = {
  username: [{ required: true, message: '请输入账号' }],
  realName: [{ required: true, message: '请输入姓名' }],
  email: [{ type: 'email', message: '邮箱格式不正确' }],
  roleIds: [{ required: true, message: '请选择角色', trigger: 'change' }],
  phone: [{ pattern: /^\d{11}$/, message: '手机号必须是11位数字', trigger: 'blur' }]
}

// 防御性 fallback：万一 list 是 null/undefined，表格也不会崩
const safeList = computed(() => Array.isArray(list.value) ? list.value : [])

const resetQuery = () => {
  query.username = ''
  query.realName = ''
  query.status = null
  query.pageNum = 1
  load()
}

const load = async () => {
  loading.value = true
  try {
    const res = await userPage(query)
    list.value = Array.isArray(res?.list) ? res.list : []
    total.value = Number(res?.total) || 0
  } catch (e) {
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const openForm = (row) => {
  dialogVisible.value = true
  // 重置表单
  Object.assign(form, {
    id: null, username: '', password: '', realName: '',
    phone: '', email: '', gender: 1, status: 1, remark: '', roleIds: null
  })
  if (row) {
    // 直接复用列表接口返回的 row（已包含 UserVO 全字段），不再调用详情接口
    // —— 详情接口需要 system:user:query 权限，而该权限未挂在任何角色上，
    // 调用会 403 触发全局 ElMessage 提示，这里复用列表数据可避免该问题
    Object.assign(form, row)
    // 兼容：列表接口若仍返回 roleIds 数组，取第一个作为单选值
    if (Array.isArray(form.roleIds)) form.roleIds = form.roleIds[0] ?? null
    form.password = ''
  }
}

const submit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    // 表单内 roleIds 为单值（null 或 Long），而后端 UserSaveDTO.roleIds 仍是 List<Long>，
    // 这里包装一下保证 Jackson 能正确反序列化；同时不为空时也能保留住单选语义
    const payload = {
      ...form,
      roleIds: form.roleIds == null ? [] : [form.roleIds]
    }
    if (form.id) await userEdit(payload)
    else await userAdd(payload)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } catch (e) {
    // ignore
  } finally {
    submitting.value = false
  }
}

const remove = async (row) => {
  await ElMessageBox.confirm(`确定删除用户"${row.username}"？`, '提示', { type: 'warning' })
  await userRemove([row.id])
  ElMessage.success('删除成功')
  load()
}

const resetPwd = async (row) => {
  const { value } = await ElMessageBox.prompt('请输入新密码（留空将重置为 123456）', '重置密码', { inputValue: '123456' })
  await userResetPassword(row.id, value || '123456')
  ElMessage.success('密码已重置')
}

const changeStatus = async (row, status) => {
  await userChangeStatus(row.id, status)
  ElMessage.success('操作成功')
  load()
}

// ===== 表格导入（CSV）=====
// 表头允许中英文；列名映射表
const HEADER_ALIASES = {
  realName: ['realName', 'real_name', '姓名', '名字', 'name'],
  phone:    ['phone',    '手机号', '手机', 'mobile'],
  email:    ['email',    '邮箱', 'mail', '电子邮件'],
  role:     ['role',     'roleCode', '角色', '职务', '身份']
}
const DEFAULT_PASSWORD = '123456'
const ROLE_CODE_MAP = {
  '教师': 'ROLE_TEACHER', '老师': 'ROLE_TEACHER', 'teacher': 'ROLE_TEACHER', 'ROLE_TEACHER': 'ROLE_TEACHER',
  '学生': 'ROLE_STUDENT', '学员': 'ROLE_STUDENT', 'student': 'ROLE_STUDENT', 'ROLE_STUDENT': 'ROLE_STUDENT'
}
const ROLE_ID_MAP = computed(() => {
  // 角色中文 → roleId
  const m = {}
  roleOptions.value.forEach(r => {
    m[r.roleName] = r.id
  })
  return m
})

const importVisible = ref(false)
const importRows = ref([])
const importResult = ref([])
const importPreviewLoading = ref(false)
const importSubmitting = ref(false)
const importDone = ref(false)

const validRowCount = computed(() => importRows.value.filter(r => r.valid).length)
const hasValidRow = computed(() => validRowCount.value > 0)

const openImportDialog = () => {
  importVisible.value = true
}

const resetImport = () => {
  importRows.value = []
  importResult.value = []
  importDone.value = false
}

const handleCsvChange = async (uploadFile) => {
  const raw = uploadFile?.raw
  if (!raw) return
  importPreviewLoading.value = true
  try {
    const text = await raw.text()
    const allLines = parseCSV(text)
    if (allLines.length === 0) {
      ElMessage.warning('文件为空')
      return
    }
    // 解析表头 → 字段映射
    const headers = allLines[0].map(h => (h || '').trim())
    const columnMap = resolveColumns(headers)
    const missing = ['realName', 'phone', 'email', 'role'].filter(k => columnMap[k] === -1)
    if (missing.length) {
      ElMessage.error(`CSV 缺少列：${missing.map(m => HEADER_ALIASES[m][0]).join(' / ')}`)
      importRows.value = []
      return
    }
    // 解析数据行（跳过空行）
    const dataLines = allLines.slice(1).filter(arr => arr.some(cell => (cell || '').trim() !== ''))
    const parsed = dataLines.map((arr, idx) => buildRow(arr, columnMap, idx))
    importRows.value = parsed
    importDone.value = false
  } finally {
    importPreviewLoading.value = false
  }
}

function parseCSV(text) {
  // 处理引号包裹 / 转义 / BOM
  if (text && text.charCodeAt(0) === 0xFEFF) text = text.slice(1)
  const rows = []
  let row = []
  let cell = ''
  let inQuotes = false
  for (let i = 0; i < text.length; i++) {
    const ch = text[i]
    if (inQuotes) {
      if (ch === '"') {
        if (text[i + 1] === '"') { cell += '"'; i++ }   // 转义双引号
        else inQuotes = false
      } else {
        cell += ch
      }
    } else {
      if (ch === '"') inQuotes = true
      else if (ch === ',') { row.push(cell); cell = '' }
      else if (ch === '\n' || ch === '\r') {
        // \r\n 一起处理：把紧跟的 \n 也吞掉
        if (ch === '\r' && text[i + 1] === '\n') i++
        row.push(cell); cell = ''
        rows.push(row); row = []
      } else {
        cell += ch
      }
    }
  }
  if (cell !== '' || row.length) { row.push(cell); rows.push(row) }
  return rows
}

function resolveColumns(headers) {
  const map = { realName: -1, phone: -1, email: -1, role: -1 }
  for (const field of Object.keys(map)) {
    for (const alias of HEADER_ALIASES[field]) {
      const idx = headers.indexOf(alias)
      if (idx !== -1) { map[field] = idx; break }
    }
  }
  return map
}

function buildRow(arr, columnMap, idx) {
  const realName = (arr[columnMap.realName] || '').trim()
  const phone    = (arr[columnMap.phone] || '').trim()
  const email    = (arr[columnMap.email] || '').trim()
  const roleRaw  = (arr[columnMap.role] || '').trim()
  const roleCode = ROLE_CODE_MAP[roleRaw] || (roleRaw.startsWith('ROLE_') ? roleRaw : null)
  const roleId   = roleCode ? ROLE_ID_MAP.value[roleCode === 'ROLE_TEACHER' ? '教师' : '学生'] : null

  // 校验
  const errors = []
  if (!realName) errors.push('姓名为空')
  if (!/^\d{11}$/.test(phone)) errors.push('手机号必须 11 位数字')
  if (!/^[\w.+-]+@[\w-]+\.[\w.-]+$/.test(email)) errors.push('邮箱格式错误')
  if (!roleCode) errors.push(`角色"${roleRaw}"无效`)
  else if (!roleId) errors.push(`角色"${roleRaw}"未配置`)

  // 账号：角色首字母 + 手机号
  const username = errors.length === 0
    ? `${roleCode === 'ROLE_TEACHER' ? 't' : 's'}${phone}`
    : ''

  return {
    rowIndex: idx,
    realName, phone, email, roleCode, roleId,
    username,
    password: errors.length === 0 ? DEFAULT_PASSWORD : '',
    valid: errors.length === 0,
    error: errors.join('；')
  }
}

const submitImport = async () => {
  importSubmitting.value = true
  const result = []
  try {
    // 顺序调用 userAdd；任一失败不会中断后续，最后汇总
    for (const row of importRows.value.filter(r => r.valid)) {
      const payload = {
        username: row.username,
        password: row.password,        // "123456"
        realName: row.realName,
        phone: row.phone,
        email: row.email,
        status: 1,
        roleIds: row.roleId == null ? [] : [row.roleId]
      }
      try {
        await userAdd(payload)
        result.push({ ...row, ok: true, msg: '成功' })
      } catch (e) {
        result.push({ ...row, ok: false, msg: e?.message || '失败' })
      }
    }
    importResult.value = result
    importDone.value = true
    const ok = result.filter(r => r.ok).length
    const fail = result.length - ok
    ElMessage[fail === 0 ? 'success' : 'warning'](`导入完成：成功 ${ok} 条，失败 ${fail} 条`)
    load()
  } finally {
    importSubmitting.value = false
  }
}

const downloadResult = () => {
  // 输出与导入格式对齐的 CSV：姓名、手机号、邮箱、角色、账号、结果
  const lines = ['姓名,手机号,邮箱,角色,账号,结果']
  importResult.value.forEach(r => {
    const role = r.roleCode === 'ROLE_TEACHER' ? '教师' : (r.roleCode === 'ROLE_STUDENT' ? '学生' : r.roleCode || '')
    lines.push([r.realName, r.phone, r.email, role, r.username, r.msg].map(csvEscape).join(','))
  })
  const blob = new Blob(['﻿' + lines.join('\n')], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url; a.download = `用户导入结果-${Date.now()}.csv`; a.click()
  URL.revokeObjectURL(url)
}

function csvEscape(v) {
  const s = v == null ? '' : String(v)
  return /[",\n\r]/.test(s) ? `"${s.replace(/"/g, '""')}"` : s
}

onMounted(async () => {
  try {
    const roles = await roleList()
    const list = Array.isArray(roles) ? roles : []
    // 兜底：保证学生角色一定可选（数据库配置若被误禁用，这里补一条）
    if (!list.some(r => r.roleName === '学生')) {
      list.push({ id: 4, roleName: '学生', roleCode: 'ROLE_STUDENT', sort: 4, status: 1 })
    }
    roleOptions.value = list
  } catch (e) {
    roleOptions.value = [{ id: 4, roleName: '学生', roleCode: 'ROLE_STUDENT', sort: 4, status: 1 }]
  }
  load()
})
</script>

<style scoped>
.toolbar-right { display: flex; align-items: center; gap: 8px; }
.import-summary { display: flex; align-items: center; gap: 4px; margin-bottom: 8px; color: var(--el-text-color-secondary); font-size: 13px; }
.el-upload-dragger { padding: 24px 0; }
</style>