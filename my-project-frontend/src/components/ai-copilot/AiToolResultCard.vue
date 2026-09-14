<template>
  <div class="ai-tool-card">
    <div class="ai-tool-card__title">{{ title }}</div>
    <div v-if="subtitle" class="ai-tool-card__sub">{{ subtitle }}</div>
    <dl v-if="fields.length" class="ai-tool-card__fields">
      <template v-for="(f, i) in fields" :key="i">
        <dt>{{ f.label }}</dt>
        <dd>{{ f.value }}</dd>
      </template>
    </dl>
    <div v-if="actions.length" class="ai-tool-card__actions">
      <el-button
        v-for="(a, i) in actions"
        :key="i"
        size="small"
        type="primary"
        link
        @click="$emit('action', a)"
      >
        {{ a.label || a.actionType }}
      </el-button>
    </div>
  </div>
</template>

<script setup>
defineProps({
  title: { type: String, default: '查询结果' },
  subtitle: { type: String, default: '' },
  fields: { type: Array, default: () => [] },
  actions: { type: Array, default: () => [] }
})
defineEmits(['action'])
</script>

<style scoped>
.ai-tool-card {
  margin-top: 8px;
  padding: 12px;
  border: 1px solid var(--el-border-color);
  border-radius: 8px;
  background: var(--el-bg-color);
}
.ai-tool-card__title {
  font-weight: 600;
  font-size: 14px;
}
.ai-tool-card__sub {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-top: 2px;
}
.ai-tool-card__fields {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 4px 12px;
  margin: 10px 0 0;
  font-size: 13px;
}
.ai-tool-card__fields dt {
  color: var(--el-text-color-secondary);
}
.ai-tool-card__fields dd {
  margin: 0;
}
.ai-tool-card__actions {
  margin-top: 8px;
}
</style>
