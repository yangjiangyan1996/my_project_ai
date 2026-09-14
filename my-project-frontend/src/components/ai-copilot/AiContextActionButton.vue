<template>
  <el-button
    :type="type"
    :size="size"
    :text="text"
    :link="link"
    @click="onClick"
  >
    <el-icon v-if="showIcon" style="margin-right: 4px"><MagicStick /></el-icon>
    {{ label }}
  </el-button>
</template>

<script setup>
import { MagicStick } from '@element-plus/icons-vue'
import { useRoute } from 'vue-router'
import { useAiCopilot } from '@/composables/useAiCopilot'
import { buildPageContext } from './pageContext'

const props = defineProps({
  label: { type: String, default: 'AI 分析' },
  prefill: { type: String, default: '' },
  autoSend: { type: Boolean, default: false },
  context: { type: Object, default: null },
  type: { type: String, default: 'primary' },
  size: { type: String, default: 'small' },
  text: { type: Boolean, default: false },
  link: { type: Boolean, default: false },
  showIcon: { type: Boolean, default: true }
})

const route = useRoute()
const { open } = useAiCopilot()

function onClick() {
  const ctx = {
    ...buildPageContext(route),
    ...(props.context || {})
  }
  open({
    prefill: props.prefill || '帮我分析一下当前页面的业务数据。',
    autoSend: props.autoSend,
    context: ctx
  })
}
</script>
