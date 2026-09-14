<template>
  <div class="ai-empty">
    <div class="ai-empty__title">你好，我是 AI 仓储助手。</div>
    <p class="ai-empty__desc">你可以问我：</p>
    <ul class="ai-empty__list">
      <li v-for="q in examples" :key="q" @click="$emit('pick', q)">{{ q }}</li>
    </ul>
    <div class="ai-empty__quick">
      <el-button
        v-for="q in quickPrompts"
        :key="q.label"
        size="small"
        round
        @click="$emit('pick', q.text)"
      >
        {{ q.label }}
      </el-button>
    </div>
  </div>
</template>

<script setup>
defineEmits(['pick'])

const examples = [
  'A001 还有多少库存？',
  '今天有哪些待出库单？',
  '杭州仓有哪些待入库？',
  '帮我查一下待盘点任务'
]

const quickPrompts = [
  { label: '查询库存', text: '帮我查一下库存情况' },
  { label: '待入库', text: '当前有哪些待处理入库单？' },
  { label: '待出库', text: '当前有哪些待处理出库单？' },
  { label: '待盘点', text: '当前有哪些待审核盘点单？' }
]
</script>

<style scoped>
.ai-empty {
  padding: 24px 16px;
  color: var(--el-text-color-regular);
}
.ai-empty__title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 8px;
  color: var(--el-text-color-primary);
}
.ai-empty__desc {
  margin: 0 0 8px;
  font-size: 13px;
}
.ai-empty__list {
  margin: 0 0 16px;
  padding-left: 18px;
  font-size: 13px;
  line-height: 1.8;
}
.ai-empty__list li {
  cursor: pointer;
  color: var(--el-color-primary);
}
.ai-empty__list li:hover {
  text-decoration: underline;
}
.ai-empty__quick {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
</style>
