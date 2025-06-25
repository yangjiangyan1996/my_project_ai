<template>
  <div class="profile-container">
    <div class="profile-header">
      <div class="profile-info">
        <h1 class="username">用户名</h1>
        <div class="industry">行业</div>
        <!-- <div class="profile-actions">
          <el-button type="text" size="small">查看详细资料</el-button>
          <el-button type="text" size="small">编辑个人资料</el-button>

          <el-button type="primary" size="small" @click="goToCreateSidejob">
            💼 找副业
          </el-button>
          <el-button type="default" size="small" @click="goToJoinSidejob">
            🤝 找搭子
          </el-button>
        </div> -->
        <div class="sidejob-entry-card">
          <div class="sidejob-header">
            <el-icon size="22"><Suitcase /></el-icon>
            <span class="sidejob-title">开启副业，赚外快</span>
          </div>
          <div class="sidejob-description">选择你的路径：发起副业项目，或加入有趣团队</div>
          <div class="sidejob-buttons">
            <el-button type="primary" size="large" @click="goToCreateSidejob">
              💼 我要发起副业
            </el-button>
            <el-button type="success" size="large" @click="goToJoinSidejob">
              🤝 我想找团队
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <div class="stats-container">
      <div class="stats-item">
        <div class="stats-count">0</div>
        <div class="stats-label">动态</div>
      </div>
      <div class="stats-item">
        <div class="stats-count">0</div>
        <div class="stats-label">回答</div>
      </div>
      <div class="stats-item">
        <div class="stats-count">0</div>
        <div class="stats-label">视频</div>
      </div>
      <div class="stats-item">
        <div class="stats-count">0</div>
        <div class="stats-label">提问</div>
      </div>
      <div class="stats-item">
        <div class="stats-count">0</div>
        <div class="stats-label">文章</div>
      </div>
      <div class="stats-item">
        <div class="stats-count">0</div>
        <div class="stats-label">专栏</div>
      </div>
      <div class="stats-item">
        <div class="stats-count">0</div>
        <div class="stats-label">想法</div>
      </div>
      <div class="stats-item">
        <div class="stats-count">0</div>
        <div class="stats-label">收藏</div>
      </div>
      <div class="stats-item">
        <div class="stats-count">0</div>
        <div class="stats-label">关注</div>
      </div>
    </div>

    <div class="content-tabs">
      <el-tabs v-model="activeTab" @tab-click="handleTabChange">
        <el-tab-pane label="我发布的" name="myPublish" v-loading="loading">
          <div class="infinite-list" v-infinite-scroll="loadMore" :infinite-scroll-disabled="noMorePublish">
            <div class="activity-item" v-for="(item, index) in publishList" :key="'publish-'+index" @click="goToDetail(item)">
              <div class="activity-type">{{ item.categoryName }}</div>
              <div class="activity-time">{{ item.createdAt.slice(0,10) }}</div>
              <div class="activity-content">
                <h3 class="activity-title">{{ item.name }}</h3>
                <div class="activity-detail">{{ item.description }}</div>
                <div class="activity-meta">
                  <span>已赞同 {{ item.likeCount }}</span>
                  <span>{{ item.commentCount }} 条评论</span>
                  <span>收藏 {{ item.favoriteCount }}</span>
                  <el-button type="text" size="small">分享</el-button>
                  <el-button type="text" size="small">喜欢</el-button>
                </div>
              </div>
            </div>
            <div v-if="noMorePublish" class="no-more">没有更多内容了</div>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="我收藏的" name="myFavorites" v-loading="favoritesLoading">
          <div class="infinite-list" v-infinite-scroll="loadMoreFavorites" :infinite-scroll-disabled="noMoreFavorites">
            <div class="activity-item" v-for="(item, index) in favoritesList" :key="'favorites-'+index">
              <div class="activity-type">{{ item.categoryName }}</div>
              <div class="activity-time">{{ item.createdAt.slice(0,10) }}</div>
              <div class="activity-content">
                <h3 class="activity-title">{{ item.name }}</h3>
                <div class="activity-detail">{{ item.description }}</div>
                <div class="activity-meta">
                  <span>已赞同 {{ item.likeCount }}</span>
                  <span>{{ item.commentCount }} 条评论</span>
                  <span>收藏 {{ item.favoriteCount }}</span>
                  <el-button type="text" size="small">分享</el-button>
                  <el-button type="text" size="small">喜欢</el-button>
                </div>
              </div>
            </div>
            <div v-if="noMoreFavorites" class="no-more">没有更多内容了</div>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="我点赞的" name="myLike" v-loading="likeLoading">
          <div class="infinite-list" v-infinite-scroll="loadMoreLike" :infinite-scroll-disabled="noMoreLike">
            <div class="activity-item" v-for="(item, index) in likeList" :key="'like-'+index">
              <div class="activity-type">{{ item.categoryName }}</div>
              <div class="activity-time">{{ item.createdAt.slice(0,10) }}</div>
              <div class="activity-content">
                <h3 class="activity-title">{{ item.name }}</h3>
                <div class="activity-detail">{{ item.description }}</div>
                <div class="activity-meta">
                  <span>已赞同 {{ item.likeCount }}</span>
                  <span>{{ item.commentCount }} 条评论</span>
                  <span>收藏 {{ item.favoriteCount }}</span>
                  <el-button type="text" size="small">分享</el-button>
                  <el-button type="text" size="small">喜欢</el-button>
                </div>
              </div>
            </div>
            <div v-if="noMoreLike" class="no-more">没有更多内容了</div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <div class="sidebar">
      <div class="sidebar-section">
        <h3 class="sidebar-title">关注了</h3>
        <div class="sidebar-count">{{ followerCount }}</div>
      </div>
      <div class="sidebar-section">
        <h3 class="sidebar-title">关注者</h3>
        <div class="sidebar-count">{{ followeeCount }}</div>
      </div>
      <div class="sidebar-section">
        <h3 class="sidebar-title">赞助的 Live</h3>
        <div class="empty-placeholder">暂无内容</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const goToDetail = (project) => {
  // router.push(`/project/${project.id}`)
    router.push({ name: 'project-detail', params: { id: project.id } });

}
import { post } from '@/net'

const activeTab = ref('myPublish')

// 关注数状态
const followerCount = ref(0)  // 关注者数量（被关注数）
const followeeCount = ref(0)  // 关注了数量（关注数）

// 我发布的相关状态
const publishList = ref([])
const publishPage = ref(1)
const publishSize = ref(10)
const publishTotal = ref(0)
const loading = ref(false)
const noMorePublish = ref(false)

// 我收藏的相关状态
const favoritesList = ref([])
const favoritesPage = ref(1)
const favoritesSize = ref(10)
const favoritesTotal = ref(0)
const favoritesLoading = ref(false)
const noMoreFavorites = ref(false)

// 我点赞的相关状态
const likeList = ref([])
const likePage = ref(1)
const likeSize = ref(10)
const likeTotal = ref(0)
const likeLoading = ref(false)
const noMoreLike = ref(false)

const goToCreateSidejob = () => {
  router.push({ name: 'createOfFindColleague' }) // 创建副业页面
}

const goToJoinSidejob = () => {
  router.push({ name: 'createOfFindJob' }) // 加入副业列表页
}

// 获取关注数
const fetchFollowCount = async () => {
  try {
    const res = await post('/api/auth/my/myFollowCount')
    followerCount.value = res.followerCount || 0
    followeeCount.value = res.followeeCount || 0
  } catch (error) {
    console.error('获取关注数失败:', error)
  }
}

// 加载更多我发布的内容
const loadMore = () => {
  if (!loading.value && publishPage.value * publishSize.value < publishTotal.value) {
    publishPage.value++
    fetchPublishData()
  } else {
    noMorePublish.value = true
  }
}

// 加载更多我收藏的内容
const loadMoreFavorites = () => {
  if (!favoritesLoading.value && favoritesPage.value * favoritesSize.value < favoritesTotal.value) {
    favoritesPage.value++
    fetchFavoritesData()
  } else {
    noMoreFavorites.value = true
  }
}

// 加载更多我点赞的内容
const loadMoreLike = () => {
  if (!likeLoading.value && likePage.value * likeSize.value < likeTotal.value) {
    likePage.value++
    fetchLikeData()
  } else {
    noMoreLike.value = true
  }
}

// 获取我发布的数据
const fetchPublishData = async () => {
  try {
    loading.value = true
    const res = await post('/api/auth/my/myPublished', {
      page: publishPage.value,
      size: publishSize.value
    })
    publishList.value.push(...res.records)
    publishTotal.value = res.total
    noMorePublish.value = publishPage.value * publishSize.value >= res.total
  } finally {
    loading.value = false
  }
}

// 获取我收藏的数据
const fetchFavoritesData = async () => {
  try {
    favoritesLoading.value = true
    const res = await post('/api/auth/my/myFavorites', {
      page: favoritesPage.value,
      size: favoritesSize.value
    })
    favoritesList.value.push(...res.records)
    favoritesTotal.value = res.total
    noMoreFavorites.value = favoritesPage.value * favoritesSize.value >= res.total
  } finally {
    favoritesLoading.value = false
  }
}

// 获取我点赞的数据
const fetchLikeData = async () => {
  try {
    likeLoading.value = true
    const res = await post('/api/auth/my/myLike', {
      page: likePage.value,
      size: likeSize.value
    })
    likeList.value.push(...res.records)
    likeTotal.value = res.total
    noMoreLike.value = likePage.value * likeSize.value >= res.total
  } finally {
    likeLoading.value = false
  }
}

// 切换标签页
const handleTabChange = (tab) => {
  if (tab.paneName === 'myFavorites' && favoritesList.value.length === 0) {
    fetchFavoritesData()
  } else if (tab.paneName === 'myLike' && likeList.value.length === 0) {
    fetchLikeData()
  }
}

onMounted(() => {
  fetchPublishData()
  fetchFollowCount()  // 页面加载时获取关注数
})
</script>

<style scoped>
.profile-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
  display: grid;
  grid-template-columns: 1fr 250px;
  gap: 20px;
}

.profile-header {
  grid-column: 1 / -1;
  margin-bottom: 20px;
}

.profile-info {
  padding: 20px 0;
}

.username {
  font-size: 24px;
  margin: 0 0 5px 0;
}

.industry {
  color: #8590a6;
  margin-bottom: 10px;
}

.profile-actions {
  margin-top: 10px;
}

.stats-container {
  grid-column: 1 / -1;
  display: flex;
  border-bottom: 1px solid #f0f2f7;
  padding-bottom: 15px;
  margin-bottom: 20px;
}

.stats-item {
  flex: 1;
  text-align: center;
  padding: 10px;
}

.stats-count {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 5px;
}

.stats-label {
  color: #8590a6;
  font-size: 14px;
}

.content-tabs {
  grid-column: 1;
}

.activity-item {
  padding: 15px 0;
  border-bottom: 1px solid #f0f2f7;
}

.activity-type {
  color: #8590a6;
  font-size: 14px;
}

.activity-time {
  color: #8590a6;
  font-size: 14px;
  margin: 5px 0;
}

.activity-title {
  font-size: 16px;
  margin: 10px 0;
}

.activity-detail {
  color: #1a1a1a;
  margin: 10px 0;
}

.activity-meta {
  color: #8590a6;
  font-size: 14px;
}

.activity-meta > * {
  margin-right: 15px;
}

.achievements-container {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}

.achievement-item {
  width: calc(25% - 15px);
  text-align: center;
  padding: 15px;
  background: #f7f8fa;
  border-radius: 4px;
}

.achievement-count {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 5px;
}

.achievement-label {
  color: #8590a6;
  font-size: 14px;
}

.sidebar {
  grid-column: 2;
}

.sidebar-section {
  margin-bottom: 25px;
}

.sidebar-title {
  font-size: 16px;
  color: #444;
  margin-bottom: 10px;
}

.sidebar-count {
  font-size: 18px;
  font-weight: 600;
}

.empty-placeholder {
  color: #8590a6;
  font-size: 14px;
  padding: 10px 0;
}

.no-more {
  text-align: center;
  color: #8590a6;
  padding: 10px 0;
  font-size: 14px;
}

.infinite-list {
  max-height: 600px;
  overflow-y: auto;
}

.sidejob-entry-card {
  background-color: #fefce8;
  border: 1px solid #fde68a;
  padding: 20px;
  border-radius: 12px;
  margin-top: 20px;
  max-width: 600px;
}

.sidejob-header {
  display: flex;
  align-items: center;
  font-size: 18px;
  font-weight: 600;
  color: #b45309;
  margin-bottom: 8px;
}

.sidejob-header .el-icon {
  margin-right: 8px;
}

.sidejob-title {
  font-size: 20px;
}

.sidejob-description {
  color: #92400e;
  font-size: 14px;
  margin-bottom: 16px;
}

.sidejob-buttons {
  display: flex;
  gap: 10px;
}

</style>