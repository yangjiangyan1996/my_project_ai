<template>
  <div class="ai-daily-summary">
    <div class="ai-daily-summary__label">AI 总结</div>
    <p class="ai-daily-summary__text" :class="{ muted: !available }">
      {{ text }}
    </p>
  </div>
</template>

<script setup>
defineProps({
  text: { type: String, default: 'AI 摘要暂时不可用' },
  available: { type: Boolean, default: false }
})
</script>

<style scoped>
.ai-daily-summary {
  margin: 12px 0 16px;
  padding: 12px 14px;
  background: var(--el-fill-color-light);
  border-radius: 8px;
}
.ai-daily-summary__label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-bottom: 6px;
}
.ai-daily-summary__text {
  margin: 0;
  font-size: 14px;
  line-height: 1.6;
  white-space: pre-wrap;
}
.ai-daily-summary__text.muted {
  color: var(--el-text-color-secondary);
}
</style>
