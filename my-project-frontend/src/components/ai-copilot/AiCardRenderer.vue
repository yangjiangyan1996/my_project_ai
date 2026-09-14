<template>
  <div class="ai-card-renderer">
    <template v-if="clarificationOptions.length">
      <AiClarificationCard
        :title="clarificationTitle"
        :options="clarificationOptions"
        @select="$emit('clarify', $event)"
      />
    </template>
    <template v-else-if="normalizedCards.length">
      <AiToolResultCard
        v-for="(card, idx) in normalizedCards"
        :key="idx"
        :title="card.title"
        :subtitle="card.subtitle"
        :fields="card.fields"
        :actions="card.actions"
        @action="$emit('action', $event)"
      />
    </template>
    <AiToolResultCard
      v-else-if="fallbackFields.length"
      title="查询结果"
      :fields="fallbackFields"
      :actions="messageActions"
      @action="$emit('action', $event)"
    />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import AiClarificationCard from './AiClarificationCard.vue'
import AiToolResultCard from './AiToolResultCard.vue'

const props = defineProps({
  message: { type: Object, required: true }
})
defineEmits(['clarify', 'action'])

const clarificationTitle = computed(() => {
  const c = (props.message.cards || []).find(x => x?.type === 'clarification')
  return c?.title || props.message.content || '请选择：'
})

const clarificationOptions = computed(() => {
  if (props.message.type !== 'NEED_CLARIFICATION') return []
  const c = (props.message.cards || []).find(x => x?.type === 'clarification')
  if (c?.options?.length) return c.options
  return []
})

const messageActions = computed(() => props.message.actions || [])

const normalizedCards = computed(() => {
  return (props.message.cards || [])
    .filter(c => c && c.type !== 'clarification')
    .map(normalizeCard)
})

const fallbackFields = computed(() => {
  // If TOOL_RESULT without cards, show nothing extra (text already shown)
  return []
})

function normalizeCard(card) {
  if (card.fields && Array.isArray(card.fields)) {
    return {
      title: card.title || '结果',
      subtitle: card.subtitle || '',
      fields: card.fields,
      actions: card.actions || []
    }
  }
  // Generic object → key/value fields (skip internal)
  const skip = new Set(['type', 'title', 'subtitle', 'options', 'actions', 'items'])
  const fields = []
  Object.keys(card || {}).forEach(k => {
    if (skip.has(k)) return
    const v = card[k]
    if (v == null || typeof v === 'object') return
    fields.push({ label: k, value: String(v) })
  })
  // Inventory-ish flat
  if (card.sku || card.warehouseName || card.quantity != null) {
    return {
      title: card.sku || card.productName || card.title || '库存',
      subtitle: card.warehouseName || '',
      fields: [
        card.quantity != null && { label: '库存', value: String(card.quantity) },
        card.lockedQuantity != null && { label: '锁定', value: String(card.lockedQuantity) },
        card.availableQuantity != null && { label: '可用', value: String(card.availableQuantity) }
      ].filter(Boolean),
      actions: card.actions || []
    }
  }
  return {
    title: card.title || card.orderNo || card.name || '结果',
    subtitle: card.statusName || card.warehouseName || '',
    fields,
    actions: card.actions || []
  }
}
</script>
