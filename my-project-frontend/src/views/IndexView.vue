<template>
  <div class="index-container">
    <el-button @click="userLogout" style="position: absolute; right: 20px; top: 20px">退出登录</el-button>

    <el-menu 
      mode="horizontal"
      background-color="#f8f9fa"
      text-color="#2c3e50"
      active-text-color="#409EFF"
      class="nav-menu"
    >
      <el-sub-menu index="1">
        <template #title><i class="el-icon-data-analysis"></i>项目展示</template>
        <el-menu-item @click="activePanel = 'project-showcase'">热门副业</el-menu-item>
        <el-menu-item @click="router.push({ name: 'project-ranking' })">副业榜单</el-menu-item>
      </el-sub-menu>

      <el-sub-menu index="2">
        <template #title><i class="el-icon-user"></i>找人合作</template>
        <el-menu-item @click="router.push({ name: 'partner-map' })">合作地图</el-menu-item>
        <el-menu-item @click="router.push({ name: 'skill-match' })">技能匹配</el-menu-item>
      </el-sub-menu>

      <el-sub-menu index="3">
        <template #title><i class="el-icon-guide"></i>导航工具</template>
        <el-menu-item @click="router.push({ name: 'survey' })">副业测评</el-menu-item>
        <el-menu-item @click="router.push({ name: 'toolbox' })">工具箱</el-menu-item>
        <el-menu-item @click="router.push({ name: 'resources' })">资源导航</el-menu-item>
      </el-sub-menu>

      <el-sub-menu index="4">
        <template #title><i class="el-icon-chat-dot-round"></i>社区互动</template>
        <el-menu-item @click="router.push({ name: 'forum' })">圈子论坛</el-menu-item>
        <el-menu-item @click="router.push({ name: 'qa' })">问答专区</el-menu-item>
      </el-sub-menu>

      <el-sub-menu index="5">
        <template #title><i class="el-icon-s-custom"></i>AI推荐</template>
        <el-menu-item @click="router.push({ name: 'ai-assistant' })">副业推荐助手</el-menu-item>
      </el-sub-menu>

      <el-menu-item @click="router.push({ name: 'my-projects' })">
        <i class="el-icon-folder-opened"></i>我的副业
      </el-menu-item>

      <div class="flex-grow" />

      <el-menu-item index="logout" @click="userLogout">
        <i class="el-icon-switch-button"></i>退出登录
      </el-menu-item>
    </el-menu>

    <el-main class="content-panel">
      <component :is="currentComponent" />

      <el-card v-if="activePanel === 'project-showcase'">
        <el-table :data="projects">
          <el-table-column prop="name" label="项目名称" />
          <el-table-column prop="category" label="分类" />
          <el-table-column prop="difficulty" label="操作难度" />
        </el-table>
      </el-card>

      <!-- 后续可扩展其他面板 -->

    </el-main>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import router from "@/router";
import { logout } from '@/net';

const activePanel = ref('project-showcase');
const currentComponent = ref(null); // 预留动态组件位

const projects = ref([
  { name: '电商代运营', category: '电商', difficulty: '★★★' },
  { name: '短视频制作', category: '新媒体', difficulty: '★★☆' },
  { name: '摆摊卖小吃', category: '线下创业', difficulty: '★☆☆' },
  { name: 'AI提示词训练', category: 'AI相关', difficulty: '★★★' },
]);

function userLogout() {
  logout(() => router.push("/"))
}
</script>

<style scoped>
.index-container {
  padding: 20px;
  position: relative;
}
.nav-menu {
  margin-bottom: 20px;
}
.content-panel {
  margin-top: 20px;
}
.flex-grow {
  flex-grow: 1;
}
</style>
