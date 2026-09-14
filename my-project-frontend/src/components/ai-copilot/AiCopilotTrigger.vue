<template>
  <button
    v-if="visible"
    type="button"
    class="ai-fab"
    title="AI 仓储助手"
    aria-label="打开 AI 仓储助手"
    @click="$emit('click')"
  >
    <el-icon :size="22"><ChatDotRound /></el-icon>
    <span class="ai-fab__label">AI 助手</span>
  </button>
</template>

<script setup>
import { ChatDotRound } from '@element-plus/icons-vue'
defineProps({
  visible: { type: Boolean, default: true }
})
defineEmits(['click'])
</script>

<style scoped>
.ai-fab {
  position: fixed;
  right: 24px;
  bottom: 28px;
  z-index: 3000;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 12px 16px;
  border: none;
  border-radius: 999px;
  background: var(--el-color-primary);
  color: #fff;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.2);
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
}
.ai-fab:hover {
  filter: brightness(1.05);
}
@media (max-width: 768px) {
  .ai-fab {
    right: 16px;
    bottom: 20px;
    padding: 12px;
  }
  .ai-fab__label {
    display: none;
  }
}
</style>
