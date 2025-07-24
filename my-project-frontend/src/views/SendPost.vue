<template>
  <div class="tieba-layout">
    <!-- 左侧分类导航 -->
    <div class="left-sidebar">
      <div class="sidebar-header">
        <h3>贴吧分类</h3>
        <el-button type="text" @click="refreshCategories">
          <el-icon><Refresh /></el-icon>
        </el-button>
      </div>
      
      <div class="category-list">
        <div 
          v-for="group in categoryGroups" 
          :key="group.title"
          class="category-group"
        >
          <div class="group-header" @click="toggleGroup(group)">
            <span>{{ group.title }}</span>
            <el-icon :class="{ 'rotate-180': group.expanded }">
              <ArrowDown />
            </el-icon>
          </div>
          
          <el-collapse-transition>
            <div v-show="group.expanded" class="group-tags">
              <el-tag
                v-for="tag in group.tags"
                :key="tag"
                class="category-tag"
                :effect="activeTag === tag ? 'dark' : 'plain'"
                @click="handleTagClick(tag)"
              >
                {{ tag }}
              </el-tag>
            </div>
          </el-collapse-transition>
        </div>
      </div>
      
      <div class="my-bars">
        <h4>我关注的吧</h4>
        <div v-if="followedBars.length > 0" class="followed-list">
          <div 
            v-for="bar in followedBars"
            :key="bar.id"
            class="followed-bar"
            @click="navigateToBar(bar.id)"
          >
            <el-avatar :size="32" :src="bar.avatar" />
            <span class="bar-name">{{ bar.name }}</span>
            <el-badge :value="bar.unread" :max="99" class="unread-count" />
          </div>
        </div>
        <el-empty v-else description="暂无关注贴吧" :image-size="80" />
      </div>
    </div>
    
    <!-- 中间帖子列表 -->
    <div class="main-content">
      <div class="content-header">
        <h3>{{ currentCategory || '全部贴吧' }}</h3>
        <el-input
          v-model="searchQuery"
          placeholder="搜索帖子"
          class="search-input"
          clearable
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>
      
      <div class="post-tabs">
        <el-tabs v-model="activePostTab" @tab-click="handleTabChange">
          <el-tab-pane label="最新" name="latest"></el-tab-pane>
          <el-tab-pane label="热门" name="hot"></el-tab-pane>
          <el-tab-pane label="精华" name="featured"></el-tab-pane>
        </el-tabs>
      </div>
      
      <div 
        class="post-list"
        v-infinite-scroll="loadMorePosts"
        :infinite-scroll-disabled="loadingPosts"
        :infinite-scroll-distance="100"
      >
        <el-card
          v-for="post in filteredPosts"
          :key="post.id"
          class="post-card"
          shadow="hover"
          @click="navigateToPost(post.id)"
        >
          <div class="post-header">
            <el-avatar :size="40" :src="post.userAvatar" />
            <div class="user-info">
              <span class="username">{{ post.username }}</span>
              <span class="post-time">{{ post.time }}</span>
            </div>
            <el-tag size="small">{{ post.barName }}</el-tag>
          </div>
          
          <div class="post-content">
            <h4>{{ post.title }}</h4>
            <p class="content">{{ post.content }}</p>
            
            <div v-if="post.images && post.images.length > 0" class="post-images">
              <el-image
                v-for="(img, index) in post.images.slice(0, 3)"
                :key="index"
                :src="img"
                :preview-src-list="post.images"
                fit="cover"
                class="post-image"
                :class="{ 'last-image-more': index === 2 && post.images.length > 3 }"
              >
                <template #error>
                  <div class="image-error">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
                <div v-if="index === 2 && post.images.length > 3" class="image-more">
                  +{{ post.images.length - 3 }}
                </div>
              </el-image>
            </div>
          </div>
          
          <div class="post-footer">
            <div class="post-stats">
              <span><el-icon><View /></el-icon> {{ post.views }}</span>
              <span><el-icon><ChatDotRound /></el-icon> {{ post.comments }}</span>
              <span><el-icon><Star /></el-icon> {{ post.likes }}</span>
            </div>
            <el-button 
              type="text" 
              size="small" 
              @click.stop="toggleLike(post)"
              :icon="post.liked ? StarFilled : Star"
              :class="{ 'liked': post.liked }"
            >
              {{ post.liked ? '已赞' : '点赞' }}
            </el-button>
          </div>
        </el-card>
        
        <div v-if="loadingPosts" class="loading-more">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>加载中...</span>
        </div>
        <div v-if="!hasMorePosts" class="no-more">
          没有更多帖子了
        </div>
      </div>
    </div>
    
    <!-- 右侧边栏 -->
    <div class="right-sidebar">
      <div class="sidebar-section">
        <div class="section-header">
          <h4>热门吧</h4>
          <el-button type="text" @click="refreshHotBars">
            <el-icon><Refresh /></el-icon>
            换一换
          </el-button>
        </div>
        
        <div class="hot-bar-list">
          <div 
            v-for="bar in hotBars"
            :key="bar.id"
            class="hot-bar-item"
            @click="navigateToBar(bar.id)"
          >
            <div class="bar-rank" :class="getRankClass(bar.rank)">
              {{ bar.rank }}
            </div>
            <el-avatar :size="40" :src="bar.avatar" />
            <div class="bar-info">
              <span class="bar-name">{{ bar.name }}</span>
              <span class="bar-stats">{{ bar.followers }}关注 · {{ bar.posts }}帖子</span>
            </div>
            <el-button 
              type="text" 
              size="small" 
              @click.stop="toggleFollowBar(bar)"
              :icon="bar.followed ? CircleCheckFilled : Plus"
            >
              {{ bar.followed ? '已关注' : '关注' }}
            </el-button>
          </div>
        </div>
      </div>
      
      <div class="sidebar-section">
        <div class="section-header">
          <h4>贴吧热议榜</h4>
        </div>
        
        <div class="hot-topics">
          <div 
            v-for="(topic, index) in hotTopics"
            :key="topic.id"
            class="topic-item"
            @click="navigateToTopic(topic.id)"
          >
            <div class="topic-rank" :class="getRankClass(index + 1)">
              {{ index + 1 }}
            </div>
            <div class="topic-content">
              <p class="topic-title">{{ topic.title }}</p>
              <p class="topic-stats">{{ topic.views }}阅读 · {{ topic.comments }}讨论</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { 
  ArrowDown, Refresh, Search, View, 
  ChatDotRound, Star, StarFilled, 
  Plus, CircleCheckFilled, Picture,
  Loading
} from '@element-plus/icons-vue'

const router = useRouter()

// 左侧分类数据
const categoryGroups = ref([
  {
    title: '娱乐明星',
    tags: ['导演', '时尚人物', '明星', '粉丝组织', '网络红人', '选秀选手', 'CP'],
    expanded: true
  },
  {
    title: '体育',
    tags: ['足球', '篮球', 'NBA', 'CBA', '乒乓球', '网球', '舞蹈', '健身'],
    expanded: true
  },
  {
    title: '小说',
    tags: ['奇幻', '首饰', '男导', '穿越', '连载', '修真', '历史', '架空文'],
    expanded: false
  },
  {
    title: '生活家',
    tags: ['小而美', 'DIY', '美食', '摄影', '旅行', '变美', '留学移民', '文玩'],
    expanded: false
  },
  {
    title: '闲·趣',
    tags: ['萌宠', '萝莉', '重口味', '吐槽', '恐怖', '星座', '爆料', '喵星人'],
    expanded: false
  },
  {
    title: '游戏',
    tags: ['游戏主播及平台', '游戏交易及功能', '游戏角色', '电子竞技及选手'],
    expanded: false
  },
  {
    title: '动漫宅',
    tags: ['日本动漫', '国产动漫', '欧美动漫', '搞笑漫画', '热血动漫', '推理', '声优'],
    expanded: false
  },
  {
    title: '地区',
    tags: ['国内地区', '海外地区'],
    expanded: false
  }
])

// 右侧热门吧数据
const hotBars = ref([
  {
    id: 1,
    name: '抗压背锅',
    followers: '858.1W',
    posts: '17257.7W',
    avatar: 'https://via.placeholder.com/60?text=抗压背锅',
    rank: 1,
    followed: false
  },
  {
    id: 2,
    name: 'bilibili',
    followers: '458.2W',
    posts: '14367.2W',
    avatar: 'https://via.placeholder.com/60?text=bilibili',
    rank: 2,
    followed: true
  },
  {
    id: 3,
    name: '崩坏星穹铁...',
    followers: '36.1W',
    posts: '1764.8W',
    avatar: 'https://via.placeholder.com/60?text=崩坏星穹铁',
    rank: 3,
    followed: false
  },
  {
    id: 4,
    name: '孙笑川',
    followers: '708.2W',
    posts: '20683.4W',
    avatar: 'https://via.placeholder.com/60?text=孙笑川',
    rank: 4,
    followed: false
  },
  {
    id: 5,
    name: '第五人格交易',
    followers: '155.7W',
    posts: '14492.8W',
    avatar: 'https://via.placeholder.com/60?text=第五人格交易',
    rank: 5,
    followed: false
  }
])

// 热议榜数据
const hotTopics = ref([
  {
    id: 1,
    title: '6名大学生参观矿企室遇难',
    views: '2113980',
    comments: '52345'
  },
  {
    id: 2,
    title: 'T1道歉是在酷舔苗斯吗',
    views: '2078455',
    comments: '48762'
  },
  {
    id: 3,
    title: '坚决不走！石破龙舌...',
    views: '1646655',
    comments: '35678'
  },
  {
    id: 4,
    title: '游戏开发中的设计模式应用',
    views: '1456723',
    comments: '28765'
  },
  {
    id: 5,
    title: '如何看待最新动漫剧情走向',
    views: '1324567',
    comments: '25678'
  }
])

// 我关注的吧
const followedBars = ref([
  {
    id: 2,
    name: 'bilibili',
    avatar: 'https://via.placeholder.com/60?text=bilibili',
    unread: 5
  },
  {
    id: 6,
    name: '游戏开发吧',
    avatar: 'https://via.placeholder.com/60?text=游戏开发',
    unread: 0
  },
  {
    id: 7,
    name: '前端技术',
    avatar: 'https://via.placeholder.com/60?text=前端',
    unread: 2
  }
])

// 帖子数据
const posts = ref([
  {
    id: 1,
    title: '大一遇到的奇葩舍友',
    content: '深夜宿舍小动作，舍友都看出来他丫丫一下跪倒个女号逗他',
    userAvatar: 'https://via.placeholder.com/40?text=用户',
    username: '孙笑川吧',
    time: '2分钟之前',
    views: '2289',
    comments: '124',
    likes: '56',
    liked: false,
    barName: '孙笑川吧',
    images: [
      'https://via.placeholder.com/300x200?text=图片1',
      'https://via.placeholder.com/300x200?text=图片2',
      'https://via.placeholder.com/300x200?text=图片3',
      'https://via.placeholder.com/300x200?text=图片4'
    ]
  },
  {
    id: 2,
    title: '山上看的人生居然是这样的啊',
    content: '父亲是寒门出身，考上京都大学，成了高材生，后面当了建筑工程师，娶了社长千金并生下了三个孩子。他是老二，他有一个哥哥，一个妹妹但是由于门第差距加上工作压力，父亲承受不住，自杀了当时他母亲还怀着他妹妹...',
    userAvatar: 'https://via.placeholder.com/40?text=用户',
    username: '2ch吧',
    time: '1小时前',
    views: '1895',
    comments: '256',
    likes: '189',
    liked: true,
    barName: '2ch吧',
    images: []
  },
  {
    id: 3,
    title: '游戏开发经验分享：状态机模式的应用',
    content: '在游戏开发中，状态机模式是管理游戏角色行为非常有效的方式。它可以将复杂的行为逻辑分解为多个独立的状态，每个状态处理特定的行为逻辑...',
    userAvatar: 'https://via.placeholder.com/40?text=用户',
    username: '游戏开发者',
    time: '3小时前',
    views: '1562',
    comments: '89',
    likes: '342',
    liked: false,
    barName: '游戏开发吧',
    images: [
      'https://via.placeholder.com/300x200?text=状态机图',
      'https://via.placeholder.com/300x200?text=代码示例'
    ]
  },
  {
    id: 4,
    title: '前端框架性能对比：Vue vs React vs Angular',
    content: '最近做了一个全面的前端框架性能测试，对比了Vue3、React18和Angular15在不同场景下的表现。测试包括首次加载时间、运行时性能、内存占用等多个维度...',
    userAvatar: 'https://via.placeholder.com/40?text=用户',
    username: '前端工程师',
    time: '5小时前',
    views: '2897',
    comments: '156',
    likes: '421',
    liked: false,
    barName: '前端技术吧',
    images: [
      'https://via.placeholder.com/300x200?text=性能图表'
    ]
  }
])

// 状态管理
const activeTag = ref(null)
const currentCategory = ref(null)
const activePostTab = ref('latest')
const searchQuery = ref('')
const loadingPosts = ref(false)
const hasMorePosts = ref(true)
const page = ref(1)
const pageSize = ref(10)

// 计算属性
const filteredPosts = computed(() => {
  return posts.value.filter(post => {
    const matchesSearch = searchQuery.value === '' || 
      post.title.includes(searchQuery.value) || 
      post.content.includes(searchQuery.value)
    const matchesCategory = !currentCategory.value || 
      post.barName.includes(currentCategory.value)
    return matchesSearch && matchesCategory
  })
})

// 方法
const toggleGroup = (group) => {
  group.expanded = !group.expanded
}

const handleTagClick = (tag) => {
  activeTag.value = activeTag.value === tag ? null : tag
  currentCategory.value = activeTag.value
  resetPosts()
  fetchPosts()
}

const handleTabChange = () => {
  resetPosts()
  fetchPosts()
}


const toggleLike = (post) => {
  post.liked = !post.liked
  post.likes = post.liked ? (parseInt(post.likes) + 1 + '') : (parseInt(post.likes) - 1 + '')
}

const toggleFollowBar = (bar) => {
  bar.followed = !bar.followed
  if (bar.followed) {
    followedBars.value.unshift({
      id: bar.id,
      name: bar.name,
      avatar: bar.avatar,
      unread: 0
    })
  } else {
    followedBars.value = followedBars.value.filter(b => b.id !== bar.id)
  }
}

const refreshHotBars = () => {
  // 模拟刷新热门吧
  hotBars.value = [...hotBars.value].sort(() => Math.random() - 0.5)
  hotBars.value.forEach((bar, index) => {
    bar.rank = index + 1
  })
}

const refreshCategories = () => {
  // 模拟刷新分类
  categoryGroups.value.forEach(group => {
    group.expanded = false
  })
  setTimeout(() => {
    categoryGroups.value.forEach(group => {
      group.expanded = true
    })
  }, 100)
}

const fetchPosts = () => {
  // 模拟API请求
  loadingPosts.value = true
  setTimeout(() => {
    loadingPosts.value = false
    // 这里应该有判断是否还有更多数据的逻辑
    hasMorePosts.value = page.value < 3 // 假设只有3页数据
  }, 800)
}

const loadMorePosts = () => {
  if (loadingPosts.value || !hasMorePosts.value) return
  page.value += 1
  fetchPosts()
}

const resetPosts = () => {
  page.value = 1
  hasMorePosts.value = true
}

const navigateToBar = (id) => {
  router.push({ name: 'bar', params: { id } })
}

const navigateToPost = (id) => {
  router.push({ name: 'post', params: { id } })
}

const navigateToTopic = (id) => {
  console.log('查看话题:', id)
}

const getRankClass = (rank) => {
  if (rank <= 3) return `rank-${rank}`
  return ''
}

// 初始化
onMounted(() => {
  fetchPosts()
})
</script>

<style scoped>
.tieba-layout {
  display: flex;
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
  gap: 20px;
}

.left-sidebar {
  width: 240px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.main-content {
  flex: 1;
  min-width: 0;
}

.right-sidebar {
  width: 300px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.sidebar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #ebeef5;
}

.sidebar-header h3 {
  margin: 0;
  font-size: 16px;
  color: #303133;
}

.category-list {
  background: #fff;
  border-radius: 8px;
  padding: 15px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.category-group {
  margin-bottom: 15px;
}

.group-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  cursor: pointer;
  user-select: none;
  transition: all 0.3s;
}

.group-header:hover {
  color: #409eff;
}

.group-header .el-icon {
  transition: transform 0.3s;
}

.group-header .rotate-180 {
  transform: rotate(180deg);
}

.group-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 8px 0;
}

.category-tag {
  cursor: pointer;
  transition: all 0.3s;
}

.category-tag:hover {
  transform: translateY(-2px);
}

.my-bars {
  background: #fff;
  border-radius: 8px;
  padding: 15px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.my-bars h4 {
  margin: 0 0 15px 0;
  font-size: 15px;
  color: #303133;
}

.followed-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.followed-bar {
  display: flex;
  align-items: center;
  padding: 8px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.followed-bar:hover {
  background-color: #f5f7fa;
}

.bar-name {
  flex: 1;
  margin-left: 10px;
  font-size: 14px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.unread-count {
  margin-left: 10px;
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.content-header h3 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.search-input {
  width: 200px;
}

.post-tabs {
  margin-bottom: 15px;
}

.post-tabs :deep(.el-tabs__header) {
  margin: 0;
}

.post-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.post-card {
  cursor: pointer;
  transition: all 0.3s;
}

.post-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
}

.post-header {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.user-info {
  flex: 1;
  margin-left: 10px;
}

.username {
  display: block;
  font-size: 14px;
  color: #303133;
}

.post-time {
  font-size: 12px;
  color: #909399;
}

.post-content {
  margin-bottom: 12px;
}

.post-content h4 {
  margin: 0 0 8px 0;
  font-size: 16px;
  color: #303133;
  line-height: 1.4;
}

.post-content .content {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.post-images {
  display: flex;
  gap: 8px;
  margin-top: 12px;
}

.post-image {
  width: 120px;
  height: 80px;
  border-radius: 4px;
  object-fit: cover;
  position: relative;
  transition: all 0.3s;
}

.post-image:hover {
  transform: scale(1.03);
}

.last-image-more {
  position: relative;
}

.image-more {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: bold;
  border-radius: 4px;
}

.image-error {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  color: #c0c4cc;
}

.post-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 10px;
  border-top: 1px solid #f5f7fa;
}

.post-stats {
  display: flex;
  gap: 15px;
  font-size: 12px;
  color: #909399;
}

.post-stats .el-icon {
  margin-right: 3px;
}

.liked {
  color: #f56c6c;
}

.loading-more, .no-more {
  text-align: center;
  padding: 15px;
  color: #909399;
}

.loading-more .el-icon {
  margin-right: 5px;
  animation: rotating 2s linear infinite;
}

.sidebar-section {
  background: #fff;
  border-radius: 8px;
  padding: 15px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.section-header h4 {
  margin: 0;
  font-size: 15px;
  color: #303133;
}

.hot-bar-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.hot-bar-item {
  display: flex;
  align-items: center;
  padding: 8px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.hot-bar-item:hover {
  background-color: #f5f7fa;
}

.bar-rank {
  width: 20px;
  height: 20px;
  border-radius: 4px;
  background: #f5f7fa;
  color: #909399;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 10px;
  flex-shrink: 0;
}

.bar-rank.rank-1 {
  background: #f56c6c;
  color: white;
}

.bar-rank.rank-2 {
  background: #e6a23c;
  color: white;
}

.bar-rank.rank-3 {
  background: #409eff;
  color: white;
}

.bar-info {
  flex: 1;
  margin-left: 10px;
  min-width: 0;
}

.bar-name {
  display: block;
  font-size: 14px;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.bar-stats {
  font-size: 12px;
  color: #909399;
}

.hot-topics {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.topic-item {
  display: flex;
  padding: 8px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.topic-item:hover {
  background-color: #f5f7fa;
}

.topic-rank {
  width: 20px;
  height: 20px;
  border-radius: 4px;
  background: #f5f7fa;
  color: #909399;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 10px;
  flex-shrink: 0;
}

.topic-rank.rank-1 {
  background: #f56c6c;
  color: white;
}

.topic-rank.rank-2 {
  background: #e6a23c;
  color: white;
}

.topic-rank.rank-3 {
  background: #409eff;
  color: white;
}

.topic-content {
  flex: 1;
  min-width: 0;
}

.topic-title {
  margin: 0 0 4px 0;
  font-size: 14px;
  color: #303133;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.topic-stats {
  margin: 0;
  font-size: 12px;
  color: #909399;
}

@keyframes rotating {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

/* 响应式布局 */
@media (max-width: 1200px) {
  .right-sidebar {
    width: 260px;
  }
}

@media (max-width: 992px) {
  .tieba-layout {
    flex-direction: column;
  }
  
  .left-sidebar, .right-sidebar {
    width: 100%;
  }
  
  .left-sidebar {
    order: 1;
  }
  
  .main-content {
    order: 3;
  }
  
  .right-sidebar {
    order: 2;
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 20px;
  }
}

@media (max-width: 768px) {
  .right-sidebar {
    grid-template-columns: 1fr;
  }
  
  .post-images {
    flex-wrap: wrap;
  }
  
  .post-image {
    width: calc(50% - 4px);
    height: 100px;
  }
}
</style>