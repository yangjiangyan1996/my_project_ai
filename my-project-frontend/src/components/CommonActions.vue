<template>
  <div class="common-actions">
    <el-button @click="goBack" class="back-button">返回</el-button>
    <el-button @click="userLogout" class="logout-button">退出登录</el-button>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
const router = useRouter()

const goBack = () => router.go(-1)
const userLogout = () => {
  localStorage.removeItem('token')
  router.push('/login')
}
</script>

<style scoped>
.common-actions {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 999;
}
</style>