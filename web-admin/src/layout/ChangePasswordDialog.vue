<template>
  <el-dialog v-model="visible" title="修改密码" width="400px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
      <el-form-item label="原密码" prop="oldPassword">
        <el-input v-model="form.oldPassword" type="password" show-password />
      </el-form-item>
      <el-form-item label="新密码" prop="newPassword">
        <el-input v-model="form.newPassword" type="password" show-password />
      </el-form-item>
      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input v-model="form.confirmPassword" type="password" show-password />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="submit">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { changeOwnPassword } from '@/api/system'

const props = defineProps({ modelValue: Boolean })
const emit = defineEmits(['update:modelValue'])

const visible = ref(props.modelValue)
watch(() => props.modelValue, v => visible.value = v)
watch(visible, v => emit('update:modelValue', v))

const formRef = ref()
const form = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const rules = {
  oldPassword: [{ required: true, message: '请输入原密码' }],
  newPassword: [{ required: true, min: 6, message: '密码至少6位' }],
  confirmPassword: [
    { required: true, message: '请确认新密码' },
    { validator: (rule, val, cb) => val === form.newPassword ? cb() : cb(new Error('两次密码不一致')) }
  ]
}

const submit = async () => {
  await formRef.value.validate()
  await changeOwnPassword({ oldPassword: form.oldPassword, newPassword: form.newPassword })
  ElMessage.success('密码修改成功，请重新登录')
  visible.value = false
}
</script>
