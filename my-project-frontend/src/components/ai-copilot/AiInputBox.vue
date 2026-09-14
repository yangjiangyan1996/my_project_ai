<template>
  <div class="ai-input">
    <el-input
      ref="inputRef"
      v-model="local"
      type="textarea"
      :rows="2"
      :autosize="{ minRows: 2, maxRows: 5 }"
      placeholder="请输入你想查询的内容..."
      :disabled="disabled"
      @keydown="onKeydown"
    />
    <el-button
      type="primary"
      :loading="sending"
      :disabled="disabled || !local.trim()"
      @click="submit"
    >
      发送
    </el-button>
  </div>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue'

const props = defineProps({
  modelValue: { type: String, default: '' },
  sending: { type: Boolean, default: false },
  disabled: { type: Boolean, default: false }
})
const emit = defineEmits(['update:modelValue', 'send'])

const local = ref(props.modelValue)
const inputRef = ref(null)

watch(
  () => props.modelValue,
  (v) => {
    if (v !== local.value) local.value = v
  }
)
watch(local, (v) => emit('update:modelValue', v))

function onKeydown(e) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    submit()
  }
}

function submit() {
  const text = local.value.trim()
  if (!text || props.sending || props.disabled) return
  emit('send', text)
}

function focus() {
  nextTick(() => inputRef.value?.focus?.())
}

defineExpose({ focus, clear: () => { local.value = '' } })
</script>

<style scoped>
.ai-input {
  display: flex;
  gap: 8px;
  align-items: flex-end;
  padding: 12px 14px;
  border-top: 1px solid var(--el-border-color-lighter);
  background: var(--el-bg-color);
}
.ai-input :deep(.el-textarea) {
  flex: 1;
}
</style>
