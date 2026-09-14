<script setup>
import { provide, reactive } from 'vue'
import { get } from '@/net'
import AiCopilotRoot from '@/components/ai-copilot/AiCopilotRoot.vue'

const userInfo = reactive({
  data: null,
  loading: false,
  loadUserInfo: async () => {
    const res = await get('/api/auth/user/getCurrentUserInfo')
    userInfo.data = res
  }
})

provide('userInfo', userInfo)
</script>

<template>
  <header>
    <div class="wrapper">
      <router-view />
      <AiCopilotRoot />
    </div>
  </header>
</template>

<style scoped>
header {
  line-height: 1.5;
}
</style>
