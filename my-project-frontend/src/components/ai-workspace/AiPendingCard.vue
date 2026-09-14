<template>
  <button type="button" class="ai-pending-card" @click="$emit('click')">
    <div class="ai-pending-card__label">{{ label }}</div>
    <div class="ai-pending-card__value" :style="{ color }">{{ count }}</div>
  </button>
</template>

<script setup>
defineProps({
  label: { type: String, required: true },
  count: { type: [Number, String], default: 0 },
  color: { type: String, default: 'var(--el-color-primary)' }
})
defineEmits(['click'])
</script>

<style scoped>
.ai-pending-card {
  flex: 1;
  min-width: 120px;
  text-align: left;
  border: 1px solid var(--el-border-color);
  border-radius: 10px;
  padding: 14px 16px;
  background: var(--el-bg-color);
  cursor: pointer;
}
.ai-pending-card:hover {
  border-color: var(--el-color-primary);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}
.ai-pending-card__label {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}
.ai-pending-card__value {
  margin-top: 6px;
  font-size: 28px;
  font-weight: 700;
}
</style>
