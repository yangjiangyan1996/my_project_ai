<template>
  <div class="ai-priority">
    <div class="ai-priority__title">优先处理</div>
    <ol v-if="items.length" class="ai-priority__list">
      <li v-for="(it, idx) in items" :key="idx">
        <button type="button" class="ai-priority__item" @click="$emit('select', it)">
          <span class="ai-priority__name">{{ it.title }}</span>
          <span class="ai-priority__detail">{{ it.detail }}</span>
        </button>
      </li>
    </ol>
    <div v-else class="ai-priority__empty">暂无优先事项</div>
  </div>
</template>

<script setup>
defineProps({
  items: { type: Array, default: () => [] }
})
defineEmits(['select'])
</script>

<style scoped>
.ai-priority {
  margin-top: 16px;
}
.ai-priority__title {
  font-weight: 600;
  margin-bottom: 8px;
}
.ai-priority__list {
  margin: 0;
  padding-left: 20px;
}
.ai-priority__item {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  width: 100%;
  text-align: left;
  border: none;
  background: transparent;
  padding: 6px 0;
  cursor: pointer;
  color: inherit;
}
.ai-priority__item:hover .ai-priority__name {
  color: var(--el-color-primary);
}
.ai-priority__name {
  font-weight: 500;
}
.ai-priority__detail {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
.ai-priority__empty {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}
</style>
