<template>
  <div class="ai-clarification">
    <div class="ai-clarification__hint">{{ title }}</div>
    <el-radio-group v-model="picked" class="ai-clarification__group">
      <el-radio
        v-for="(opt, idx) in options"
        :key="idx"
        :label="idx"
        border
        class="ai-clarification__opt"
      >
        {{ labelOf(opt) }}
      </el-radio>
    </el-radio-group>
    <el-button
      type="primary"
      size="small"
      :disabled="picked === null"
      @click="confirm"
    >
      确认选择
    </el-button>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  title: { type: String, default: '请选择：' },
  options: { type: Array, default: () => [] }
})
const emit = defineEmits(['select'])

const picked = ref(null)

function labelOf(opt) {
  if (opt == null) return '选项'
  if (typeof opt === 'string' || typeof opt === 'number') return String(opt)
  return (
    opt.name ||
    opt.productName ||
    opt.warehouseName ||
    opt.orderNo ||
    opt.stockTakeNo ||
    opt.sku ||
    opt.code ||
    opt.label ||
    JSON.stringify(opt)
  )
}

function confirm() {
  if (picked.value === null) return
  const opt = props.options[picked.value]
  emit('select', { option: opt, label: labelOf(opt) })
}
</script>

<style scoped>
.ai-clarification {
  margin-top: 8px;
  padding: 10px;
  background: var(--el-fill-color-light);
  border-radius: 8px;
}
.ai-clarification__hint {
  font-size: 13px;
  margin-bottom: 8px;
  font-weight: 500;
}
.ai-clarification__group {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  gap: 8px;
  margin-bottom: 10px;
  width: 100%;
}
.ai-clarification__opt {
  margin: 0 !important;
  height: auto !important;
  padding: 8px 12px !important;
}
</style>
