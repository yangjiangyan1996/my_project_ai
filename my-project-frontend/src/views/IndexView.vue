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
        <el-menu-item index="fuye" @click="handleMenuClick('fuye')">
          热门副业
        </el-menu-item>
        <el-menu-item index="ranking" @click="handleMenuClick('ranking')">
          副业榜单
        </el-menu-item>
      </el-sub-menu>

      <el-sub-menu index="2">
        <template #title><i class="el-icon-user"></i>找人合作</template>
        <el-menu-item index="partner-map" @click="router.push({ name: 'partner-map' })">合作地图</el-menu-item>
        <el-menu-item index="skill-match" @click="router.push({ name: 'skill-match' })">技能匹配</el-menu-item>
      </el-sub-menu>

      <el-sub-menu index="3">
        <template #title><i class="el-icon-guide"></i>导航工具</template>
        <el-menu-item index="fuyeceping" @click="router.push({ name: 'survey' })">副业测评</el-menu-item>
        <el-menu-item index="gongjuxiang" @click="router.push({ name: 'toolbox' })">工具箱</el-menu-item>
        <el-menu-item index="ziyuandaohang" @click="router.push({ name: 'resources' })">资源导航</el-menu-item>
      </el-sub-menu>

      <el-sub-menu index="4">
        <template #title><i class="el-icon-chat-dot-round"></i>社区互动</template>
        <el-menu-item index="forum" @click="router.push({ name: 'forum' })">圈子论坛</el-menu-item>
        <el-menu-item index="qa" @click="router.push({ name: 'qa' })">问答专区</el-menu-item>
      </el-sub-menu>

      <el-sub-menu index="5">
        <template #title><i class="el-icon-s-custom"></i>AI推荐</template>
        <el-menu-item index="ai-assistant" @click="router.push({ name: 'ai-assistant' })">副业推荐助手</el-menu-item>
      </el-sub-menu>

      <el-menu-item index="wodefuye" @click="router.push({ name: 'my-projects' })">
        <i class="el-icon-folder-opened"></i>我的副业
      </el-menu-item>

      <div class="flex-grow" />

      <el-menu-item index="logout" @click="userLogout">
        <i class="el-icon-switch-button"></i>退出登录
      </el-menu-item>
    </el-menu>

     <!-- 搜索区域 -->
     <div class="search-bar">
      <el-input v-model="search.name" placeholder="搜索副业名称" style="width: 200px; margin-right: 10px" />
      <el-select v-model="search.category" placeholder="选择分类" style="width: 180px; margin-right: 10px">
        <el-option
          v-for="item in categories"
          :key="item.code"
          :label="item.desc"
          :value="item.code"
        />
      </el-select>
      <el-select
        v-model="search.difficulties"
        placeholder="选择难度"
        multiple
        style="width: 180px; margin-right: 10px"
      >
        <el-option
          v-for="item in difficulties"
          :key="item.code"
          :label="item.desc"
          :value="item.code"
        />
      </el-select>
      <el-button type="primary" @click="onSearch">搜索</el-button>
    </div>

    <!-- 项目展示区域 -->
    <div class="main-content">
      <!-- 项目列表容器，添加滚动监听 -->
      <div class="project-container" @scroll="handleScroll">
        <el-row :gutter="20" class="project-list">
          <el-col 
            v-for="project in projectList" 
            :key="project.id" 
            :xs="24" :sm="12" :md="8" :lg="6"
          >
            <el-card class="project-card" shadow="hover">
              <img 
                :src="project.imageUrl" 
                class="project-image"
                alt="项目封面"
              />
              <div class="project-content">
                <h3 class="project-title">{{ project.name }}</h3>
                <div class="project-meta">
                  <el-tag size="small">{{ project.category }}</el-tag>
                  <span class="project-difficulty">{{ project.difficulty }}</span>
                </div>
                <p class="project-description">{{ project.description }}</p>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 加载更多提示 -->
        <div v-if="loading" class="loading-more">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>加载中...</span>
        </div>
        <div v-else-if="!hasMore" class="no-more">
          没有更多数据了
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { Loading } from '@element-plus/icons-vue';
import router from "@/router";
import { logout, post, get } from '@/net';
import { ElMessage } from 'element-plus';

const projectList = ref([]);
const loading = ref(false);
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);
const hasMore = ref(true);
const categories = ref([]);
const difficulties = ref([]);
const search = ref({ name: '', category: '', difficulties: [] });

//加载下拉框的数据
const fetchOptions = async () => {
  try {
    const res = await get('/api/auth/common/category');

    const [catRes, diffRes] = await Promise.all([
      get('/api/auth/common/category'),
      get('/api/auth/common/difficulty')
    ]);
    categories.value = catRes || [];
    difficulties.value = diffRes || [];

    console.log("categories",categories)
    console.log("difficulties",difficulties)

  } catch (e) {
    ElMessage.error('加载搜索选项失败');
  }
};


/**
 * 副业的列表数据
 */
const fetchProjectListData = async (params = {}) => {
  if (loading.value || !hasMore.value) {
      return;
  }
  
  loading.value = true;
  try {
    const res = await post('/api/auth/project/show', { 
      page: currentPage.value, 
      size: pageSize.value,
      category: params?.category,
      difficulty: params?.difficulty,
      projectName: params?.projectName
    });

    console.log("接口返回数据:", res);
    
    if (!res?.records) {
      console.warn("接口返回异常结构：", res);
      return;
    }

    // 合并新数据
    projectList.value = [
      ...projectList.value,
      ...res.records.map(item => ({
        id: item.id,
        name: item.name,
        category: item.categoryName,
        description: item.description,
        difficulty: '★'.repeat(Number(item.difficulty || 1)),
        imageUrl: (item.imageUrl?.replace(/["]/g, '') || '/images/default-project.png')
      }))
    ];

    total.value = res.total || 0;
    hasMore.value = projectList.value.length < total.value;
    currentPage.value += 1;
  } catch (error) {
    console.error("加载失败：", error);
    ElMessage.error('数据加载失败');
  } finally {
    loading.value = false;
  }
}
// 加载项目数据
const loadProjects = async () => {
  fetchOptions();
  
  // 初始加载带上空条件
  fetchProjectListData({
    category: search.value.category,
    difficulty: search.value.difficulties,
    projectName: search.value.name
  });
};

const onSearch = () => {
  currentPage.value = 1;         // 重置页码
  projectList.value = [];        // 清空旧数据
  hasMore.value = true;          // 恢复为“还有更多”

  fetchProjectListData({
    category: search.value.category,
    difficulty: search.value.difficulties,
    projectName: search.value.name
  });
};


// 滚动到底部加载更多
const handleScroll = (e) => {
  const { scrollTop, scrollHeight, clientHeight } = e.target;
  if (scrollHeight - scrollTop - clientHeight < 100 && !loading.value && hasMore.value) {
    fetchProjectListData({
      category: search.value.category,
      difficulty: search.value.difficulties,
      projectName: search.value.name
    });
  }
};

// 初始化加载
onMounted(() => {
  loadProjects();
});

function userLogout() {
  logout(() => router.push("/"));
}

function handleMenuClick(panel) {
  activePanel.value = panel;
}
</script>

<style scoped>
.index-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
}

.nav-menu {
  position: sticky;
  top: 0;
  z-index: 1000;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.main-content {
  flex: 1;
  overflow: hidden;
  padding: 20px;
}

.project-container {
  height: 100%;
  overflow-y: auto;
  padding: 0 10px;
}

.project-list {
  margin-bottom: 20px;
}

.project-card {
  margin-bottom: 20px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.project-image {
  width: 100%;
  height: 180px;
  object-fit: cover;
  border-radius: 4px 4px 0 0;
}

.project-content {
  padding: 15px;
  flex: 1;
}

.project-title {
  margin: 0 0 10px;
  font-size: 16px;
  color: #333;
}

.project-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.project-difficulty {
  color: #ff9900;
  font-weight: bold;
}

.project-description {
  color: #666;
  font-size: 14px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.loading-more, .no-more {
  text-align: center;
  padding: 20px;
  color: #999;
}

.loading-more .el-icon {
  margin-right: 5px;
  animation: rotating 2s linear infinite;
}

@keyframes rotating {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.flex-grow {
  flex-grow: 1;
}

.search-bar {
  margin-top: 20px;
  display: flex;
  align-items: center;
  padding: 10px 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}
</style>