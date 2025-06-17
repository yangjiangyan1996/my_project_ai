<template>
  <div class="index-container">
    <el-button @click="userLogout" style="position: absolute; right: 20px; top: 20px">退出登录</el-button>
    
    <el-card class="menu-card">
      <el-button 
        type="primary" 
        @click="router.push({ name: 'name-generator' })"
        style="width: 200px; margin: 10px">
        ✨ 生成
      </el-button>
      <!-- 预留其他功能按钮位置 -->
    </el-card>
  </div>
</template>

<script setup>
import { logout } from '@/net'
import router from "@/router";

function userLogout() {
  logout(() => router.push("/"))
}
</script>

<style scoped>
.index-container {
  padding: 20px;
  position: relative;
}
.menu-card {
  max-width: 800px;
  margin: 20px auto;
  text-align: center;
}
</style>
