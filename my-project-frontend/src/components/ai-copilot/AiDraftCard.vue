<template>
  <div class="ai-draft-card">
    <div class="ai-draft-card__title">
      {{ typeLabel }}草稿
      <el-tag size="small" type="warning">{{ draft.status || 'OPEN' }}</el-tag>
    </div>

    <div class="ai-draft-card__fields">
      <div v-for="row in fieldRows" :key="row.label" class="ai-draft-card__row">
        <span class="ai-draft-card__label">{{ row.label }}</span>
        <span class="ai-draft-card__value">{{ row.value }}</span>
      </div>
    </div>

    <div v-if="items.length" class="ai-draft-card__items">
      <div class="ai-draft-card__section">商品明细</div>
      <div v-for="(it, idx) in items" :key="idx" class="ai-draft-card__item">
        {{ itemLabel(it) }}
      </div>
    </div>

    <div v-if="warnings.length" class="ai-draft-card__warn">
      <div v-for="(w, i) in warnings" :key="i">⚠ {{ w }}</div>
    </div>

    <div v-if="validation" class="ai-draft-card__validation">
      校验：{{ validation }}
    </div>

    <div v-if="lastError" class="ai-draft-card__error">
      {{ lastError }}
    </div>

    <div class="ai-draft-card__actions">
      <el-button size="small" :disabled="busy || !canEdit" @click="$emit('edit', draft)">
        修改
      </el-button>
      <el-button size="small" :disabled="busy || !canCancel" @click="$emit('cancel', draft)">
        取消
      </el-button>
      <el-button
        type="primary"
        size="small"
        :loading="busy"
        :disabled="busy || !canConfirm"
        @click="onConfirmClick"
      >
        确认创建
      </el-button>
    </div>

    <el-dialog
      v-model="dialogVisible"
      title="确认创建单据？"
      width="420px"
      append-to-body
    >
      <div class="ai-draft-card__dialog">
        <p>{{ typeLabel }}</p>
        <p v-for="row in summaryRows" :key="row.label">{{ row.label }}：{{ row.value }}</p>
      </div>
      <template #footer>
        <el-button @click="dialogVisible = false" :disabled="busy">取消</el-button>
        <el-button type="primary" :loading="busy" @click="doConfirm">确认创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'

const props = defineProps({
  draft: { type: Object, required: true },
  busy: { type: Boolean, default: false }
})
const emit = defineEmits(['edit', 'cancel', 'confirm'])

const dialogVisible = ref(false)

const fields = computed(() => props.draft?.fields || {})
const items = computed(() => {
  if (Array.isArray(props.draft?.items) && props.draft.items.length) return props.draft.items
  if (Array.isArray(fields.value.items)) return fields.value.items
  return []
})
const warnings = computed(() => props.draft?.warnings || [])
const validation = computed(() => props.draft?.validation || '')
const lastError = computed(() => props.draft?.lastConfirmError || '')
const status = computed(() => props.draft?.status || 'OPEN')

const typeLabel = computed(() => {
  const t = props.draft?.draftType || fields.value.draftTypeLabel
  if (t === 'INBOUND' || t === '入库单') return '入库单'
  if (t === 'STOCKTAKE' || t === '盘点单') return '盘点单'
  if (t === 'OUTBOUND' || t === '出库单') return '出库单'
  return fields.value.draftTypeLabel || '单据'
})

const canConfirm = computed(
  () => status.value === 'OPEN' && validation.value !== 'BLOCK'
)
const canCancel = computed(() => status.value === 'OPEN')
const canEdit = computed(() => status.value === 'OPEN')

const fieldRows = computed(() => {
  const f = fields.value
  const rows = []
  if (f.customerName) rows.push({ label: '客户', value: f.customerName })
  if (f.supplierName) rows.push({ label: '供应商', value: f.supplierName })
  if (f.warehouseName) rows.push({ label: '仓库', value: f.warehouseName })
  if (f.takeScopeLabel) rows.push({ label: '盘点范围', value: f.takeScopeLabel })
  if (f.totalQuantity != null) rows.push({ label: '总数量', value: String(f.totalQuantity) })
  if (f.orderNoPreview) rows.push({ label: '预览单号', value: f.orderNoPreview })
  return rows
})

const summaryRows = computed(() => {
  const rows = [...fieldRows.value]
  rows.push({ label: '商品行数', value: String(items.value.length || 0) })
  return rows
})

function itemLabel(it) {
  if (!it || typeof it !== 'object') return String(it)
  const name = it.productName || it.name || it.sku || '商品'
  const sku = it.sku ? ` (${it.sku})` : ''
  const qty = it.quantity != null ? ` × ${it.quantity}` : ''
  return `${name}${sku}${qty}`
}

function onConfirmClick() {
  if (!canConfirm.value) return
  dialogVisible.value = true
}

function doConfirm() {
  dialogVisible.value = false
  emit('confirm', props.draft)
}
</script>

<style scoped>
.ai-draft-card {
  margin-top: 8px;
  padding: 12px;
  border: 1px solid var(--el-border-color);
  border-radius: 8px;
  background: var(--el-fill-color-blank);
}
.ai-draft-card__title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  margin-bottom: 8px;
}
.ai-draft-card__row {
  display: flex;
  gap: 8px;
  font-size: 13px;
  margin-bottom: 4px;
}
.ai-draft-card__label {
  color: var(--el-text-color-secondary);
  min-width: 64px;
}
.ai-draft-card__section {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin: 8px 0 4px;
}
.ai-draft-card__item {
  font-size: 13px;
  padding: 2px 0;
}
.ai-draft-card__warn {
  margin-top: 8px;
  font-size: 12px;
  color: var(--el-color-warning);
}
.ai-draft-card__validation {
  margin-top: 6px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
.ai-draft-card__error {
  margin-top: 6px;
  font-size: 12px;
  color: var(--el-color-danger);
}
.ai-draft-card__actions {
  display: flex;
  gap: 8px;
  margin-top: 12px;
  flex-wrap: wrap;
}
.ai-draft-card__dialog p {
  margin: 4px 0;
  font-size: 14px;
}
</style>
