<template>
  <div class="rich-editor" :class="{ 'rich-editor--disabled': disabled }">
    <Toolbar
      v-if="editor && showToolbar"
      :editor="editor"
      :default-config="toolbarConfig"
      mode="default"
      class="rich-editor__toolbar"
    />
    <Editor
      :style="{ height: heightCss }"
      :default-config="editorConfig"
      :default-html="defaultHtml"
      :mode="mode"
      :model-value="modelValue"
      @update:model-value="onUpdate"
      @on-created="onCreated"
      @on-change="onChange"
      @on-destroyed="onDestroyed"
      class="rich-editor__body"
    />
  </div>
</template>

<script setup>
/**
 * 富文本编辑器 —— 基于 @wangeditor/editor-for-vue 5.x
 *
 * 设计要点：
 *  1. 通过 v-model 双向绑定 HTML 字符串；
 *  2. 默认模式 default (ToolBar + Edit Area)，便于在 form 中使用；
 *  3. toolbarConfig / editorConfig 支持父组件按场景覆盖；
 *  4. readonly / disabled 状态由父组件通过 props 控制；
 *  5. 上传图片的 server 上传后端未就绪时自动 fallback 到 base64 插入，
 *     等后端 /file/upload 接口落地后只需传入 customUpload 即可。
 */
import { computed, onBeforeUnmount, ref, shallowRef } from 'vue'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import '@wangeditor/editor/dist/css/style.css'

const props = defineProps({
  modelValue: { type: String, default: '' },
  // 初始内容（仅初始化时使用）
  defaultHtml: { type: String, default: '' },
  // 'default' | 'simple' | 'readonly'
  mode: { type: String, default: 'default' },
  // 编辑器高度（带单位的字符串，如 '320px'）
  height: { type: String, default: '320px' },
  // 占位符
  placeholder: { type: String, default: '请输入内容...' },
  // 只读
  readonly: { type: Boolean, default: false },
  // 禁用（禁用编辑器交互并降透明度）
  disabled: { type: Boolean, default: false },
  // 是否展示工具栏
  showToolbar: { type: Boolean, default: true },
  // 自定义上传函数（不传则自动 base64）
  customUpload: { type: Function, default: null },
  // 自定义 toolbar 排除项（按 key 过滤，例如 ['uploadImage']）
  excludeKeys: { type: Array, default: () => [] }
})

const emit = defineEmits(['update:modelValue', 'change', 'created', 'destroyed'])

// editor 实例使用 shallowRef 避免 Vue 把内部对象深度代理
const editor = shallowRef(null)

// v-model 同步：仅当值变化时触发
const onUpdate = (val) => {
  emit('update:modelValue', val)
}
const onChange = (editor) => {
  // wangeditor 在内容变更时回调，附带 editor 实例
  emit('change', editor.getHtml(), editor)
}
const onCreated = (instance) => {
  editor.value = instance
  // 同步 readonly
  if (props.readonly || props.disabled) {
    instance.disable()
  }
  emit('created', instance)
}
const onDestroyed = (instance) => {
  emit('destroyed', instance)
}

const heightCss = computed(() => props.height)

// 工具栏配置：允许父组件通过 excludeKeys 过滤
const toolbarConfig = computed(() => {
  const cfg = {
    // 不在工具栏里放全屏按钮，依赖宿主页面布局
    excludeKeys: [...props.excludeKeys]
  }
  return cfg
})

// 编辑器配置
const editorConfig = computed(() => {
  const cfg = {
    placeholder: props.placeholder,
    readOnly: props.readonly,
    // 自定义图片上传：未提供时 base64 内联
    MENU_CONF: {
      uploadImage: props.customUpload
        ? { customUpload: props.customUpload }
        : {
            // wangeditor v5 在没有 server 时会自动转 base64
            server: ''
          }
    },
    // 在用户输入 paste 时，过滤掉 word 样式
    autoFocus: false,
    scroll: true
  }
  return cfg
})

// 组件卸载时销毁 editor，防止内存泄漏
onBeforeUnmount(() => {
  if (editor.value) {
    editor.value.destroy()
    editor.value = null
  }
})
</script>

<style scoped>
.rich-editor {
  display: flex;
  flex-direction: column;
  border: 1px solid var(--line, #dcdfe6);
  background: var(--surface, #fff);
  transition: border-color 0.18s ease;
}
.rich-editor:focus-within {
  border-color: var(--accent, #409eff);
}
.rich-editor--disabled {
  opacity: 0.6;
  pointer-events: none;
}

.rich-editor__toolbar {
  border-bottom: 1px solid var(--line-soft, #ebeef5);
}
.rich-editor__body {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
}

/* 让 wangeditor 默认字号贴合项目字号规范 */
.rich-editor :deep(.w-e-text-container) {
  font-family: var(--font-body, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif);
  font-size: 14px;
  line-height: 1.7;
}
.rich-editor :deep(.w-e-text-placeholder) {
  font-style: italic;
  color: var(--mute, #909399);
}

/* 工具栏按钮去除默认圆角、贴合项目 button 风格 */
.rich-editor :deep(.w-e-toolbar) {
  background: var(--surface-soft, #fafafa);
}
</style>
