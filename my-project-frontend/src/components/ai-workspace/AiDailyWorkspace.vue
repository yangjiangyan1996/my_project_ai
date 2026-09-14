<template>
  <el-card class="ai-daily-workspace" shadow="never">
    <template #header>
      <div class="ai-daily-workspace__header">
        <div>
          <div class="ai-daily-workspace__greeting">
            {{ greeting }}
          </div>
          <div class="ai-daily-workspace__date">{{ data?.date || '' }}</div>
        </div>
        <el-button size="small" :loading="loading" @click="load">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </template>

    <AiWorkspaceSkeleton v-if="loading && !data" />
    <AiWorkspaceError v-else-if="error" :message="error" @retry="load" />
    <template v-else-if="data">
      <AiDailySummary
        :text="summaryText"
        :available="aiSummaryAvailable"
      />

      <div class="ai-daily-workspace__cards">
        <AiPendingCard
          label="待入库"
          :count="data.inbound?.pendingCount ?? 0"
          color="#E6A23C"
          @click="navList('inbound')"
        />
        <AiPendingCard
          label="待出库"
          :count="data.outbound?.pendingCount ?? 0"
          color="#F56C6C"
          @click="navList('outbound')"
        />
        <AiPendingCard
          label="待盘点"
          :count="data.stocktake?.pendingCount ?? 0"
          color="#409EFF"
          @click="navList('stocktake')"
        />
        <AiPendingCard
          label="库存风险"
          :count="data.inventory?.riskCount ?? 0"
          color="#F56C6C"
          @click="navList('inventory')"
        />
      </div>

      <AiPriorityList :items="data.priorityItems || []" @select="onPriority" />
    </template>
  </el-card>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Refresh } from '@element-plus/icons-vue'
import { getDailyWorkspace } from '@/api/ai'
import { resolveAction } from '@/components/ai-copilot/actionAllowlist'
import AiWorkspaceSkeleton from './AiWorkspaceSkeleton.vue'
import AiWorkspaceError from './AiWorkspaceError.vue'
import AiDailySummary from './AiDailySummary.vue'
import AiPendingCard from './AiPendingCard.vue'
import AiPriorityList from './AiPriorityList.vue'

const router = useRouter()
const loading = ref(false)
const error = ref('')
const payload = ref(null)

const data = computed(() => payload.value?.data || null)
const aiSummaryAvailable = computed(() => !!payload.value?.aiSummaryAvailable)
const summaryText = computed(() =>
  payload.value?.aiSummary || 'AI 摘要暂时不可用'
)

const greeting = computed(() => {
  const total = data.value?.totalActionable ?? 0
  return `早上好，今天有 ${total} 项需要关注`
})

async function load() {
  loading.value = true
  error.value = ''
  try {
    const res = await getDailyWorkspace()
    payload.value = res
  } catch (e) {
    error.value = e?.message && String(e.message).length < 80
      ? String(e.message)
      : '今日工作台加载失败，请重试'
  } finally {
    loading.value = false
  }
}

function navList(kind) {
  const map = {
    inbound: { name: 'ckInboundManage' },
    outbound: { name: 'ckOutboundManage' },
    stocktake: { name: 'ckStockTake' },
    inventory: { name: 'ckInventoryList' }
  }
  const loc = map[kind]
  if (loc) router.push(loc).catch(() => {})
}

function onPriority(item) {
  const loc = resolveAction({
    actionType: item.actionType,
    entityId: item.entityId
  })
  if (loc) router.push(loc).catch(() => {})
  else if (item.kind === 'INBOUND') navList('inbound')
  else if (item.kind === 'OUTBOUND') navList('outbound')
  else if (item.kind === 'STOCKTAKE') navList('stocktake')
  else if (item.kind === 'INVENTORY_RISK') navList('inventory')
}

onMounted(load)
defineExpose({ load })
</script>

<style scoped>
.ai-daily-workspace {
  margin-bottom: 16px;
}
.ai-daily-workspace__header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}
.ai-daily-workspace__greeting {
  font-size: 18px;
  font-weight: 700;
}
.ai-daily-workspace__date {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-top: 4px;
}
.ai-daily-workspace__cards {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}
@media (max-width: 768px) {
  .ai-daily-workspace__cards {
    flex-direction: column;
  }
}
</style>
