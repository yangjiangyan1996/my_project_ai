<script setup>
import { provide, reactive } from 'vue'
import { get } from '@/net'

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
      <router-view/>
    </div>
  </header>
</template>

<style scoped>
header {
  line-height: 1.5;
}
</style>
