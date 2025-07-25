<template>
  <div class="tieba-container">
    <!-- 顶部贴吧信息栏 -->
<div class="bar-header">
      <div class="bar-info">
        <div class="bar-avatar">
          <el-avatar :size="60" :src="barInfo.avatar" shape="square" />
        </div>
        <div class="bar-meta">
          <h1 class="bar-name" @click="navigateToBar(barInfo.name)">{{ barInfo.name }}</h1>
          <div class="bar-stats">
            <span class="stat-item">
              <el-icon><User /></el-icon>
              <span class="stat-value">{{ barInfo.followerCount | formatNumber }}</span>
            </span>
            <span class="stat-item">
              <el-icon><Document /></el-icon>
              <span class="stat-value">{{ barInfo.postCount | formatNumber }}</span>
            </span>
          </div>
          <div class="bar-category">
            <el-tag size="small" effect="plain">游戏主播及平台</el-tag>
          </div>
        </div>
        <el-button 
          type="primary" 
          class="follow-btn" 
          size="small" 
          round
          @click="toggleFollow"
        >
          <el-icon><Plus /></el-icon>
          <span>{{ isFollowed ? '已关注' : '关注' }}</span>
        </el-button>
      </div>
    </div>

    <!-- 功能导航栏 -->
    <div class="bar-nav">
      <el-scrollbar>
        <div class="nav-items">
          <el-button 
            v-for="(item, index) in navItems" 
            :key="index" 
            :class="['nav-btn', { 'active': activeNav === index }]"
            @click="activeNav = index"
          >
            {{ item }}
          </el-button>
        </div>
        </el-scrollbar>
    </div>

    <!-- 主内容区 -->
    <div class="main-content">
      <!-- 帖子列表 -->
      <div class="post-list">
        <div 
          class="post-item" 
          v-for="post in posts" 
          :key="post.id" 
          @click="navigateToPost(post.id)"
          @mouseenter="hoverPost = post.id"
          @mouseleave="hoverPost = null"
        >
          <div class="post-header">
            <el-avatar :size="40" :src="post.userAvatar" class="user-avatar"></el-avatar>
            <div class="user-info">
              <span class="username">{{ post.username }}</span>
              <span class="post-time">{{ post.time }}</span>
            </div>
            <el-button 
              v-show="hoverPost === post.id"
              class="more-btn" 
              type="text" 
              size="small" 
              @click.stop="showPostMenu(post.id)"
            >
              <el-icon><MoreFilled /></el-icon>
            </el-button>
          </div>
          <h3 class="post-title">{{ post.title }}</h3>
          <p class="post-content">{{ post.content }}</p>
          <div class="post-footer">
            <div class="action-btn">
              <el-icon><View /></el-icon>
              <span>{{ post.views }}</span>
            </div>
            <div class="action-btn">
              <el-icon><ChatDotRound /></el-icon>
              <span>{{ post.comments }}</span>
            </div>
            <div class="action-btn">
              <el-icon><Star /></el-icon>
              <span>{{ post.likes || 0 }}</span>
            </div>
            <div class="action-btn">
              <el-icon><Share /></el-icon>
            </div>
          </div>
        </div>
        
        <el-pagination
          class="pagination"
          :page-size="10"
          :pager-count="5"
          layout="prev, pager, next"
          :total="100"
          background
          hide-on-single-page
        />
      </div>

      <!-- 右侧边栏 -->
      <div class="sidebar">
        <div class="search-box">
          <el-input 
            placeholder="吧内搜索" 
            size="large"
            v-model="searchQuery"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
            <template #append>
              <el-button :icon="Search" @click="handleSearch" />
            </template>
          </el-input>
        </div>

        <div class="sidebar-section">
          <div class="section-header">
            <h3>本吧信息</h3>
            <el-button type="text" size="small">更多</el-button>
          </div>
          <div class="info-item">
            <el-icon><UserFilled /></el-icon>
            <span>小吧主共46人</span>
          </div>
          <div class="info-item">
            <el-icon><Medal /></el-icon>
            <span>会员</span>
          </div>
          <div class="info-item">
            <el-icon><Folder /></el-icon>
            <span>游戏主播及平台</span>
          </div>
          <el-button type="primary" size="small" class="apply-btn" round>
            申请吧主
          </el-button>
        </div>

        <div class="sidebar-section">
          <div class="section-header">
            <h3>友情贴吧</h3>
            <el-button type="text" size="small">更多</el-button>
          </div>
          <div 
            class="friend-bar" 
            v-for="bar in friendBars" 
            :key="bar.id" 
            @click="navigateToBar(bar.name)"
          >
            <el-avatar :size="24" :src="bar.avatar" />
            <span>{{ bar.name }}</span>
          </div>
        </div>
        
        <div class="sidebar-section">
          <div class="section-header">
            <h3>今日热议</h3>
          </div>
          <div 
            class="hot-topic" 
            v-for="topic in hotTopics" 
            :key="topic.id"
            @click="navigateToPost(topic.id)"
          >
            <div class="topic-rank" :class="{'top-rank': topic.rank <= 3}">
              {{ topic.rank }}
            </div>
            <div class="topic-content">
              <div class="topic-title">{{ topic.title }}</div>
              <div class="topic-meta">
                <span>{{ topic.views }}阅读</span>
                <span>·</span>
                <span>{{ topic.comments }}评论</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 发帖按钮 -->
    <el-button 
      class="post-create-btn" 
      type="primary" 
      size="large" 
      round
      @click="showPostDialog = true"
    >
      <el-icon><EditPen /></el-icon>
      <span>发帖</span>
    </el-button>
    
    <!-- 发帖对话框 -->
    <el-dialog 
      v-model="showPostDialog" 
      title="发表新帖" 
      width="80%"
      :fullscreen="isMobile"
    >
      <el-form :model="postForm" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="postForm.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input 
            v-model="postForm.content" 
            type="textarea" 
            :rows="8" 
            placeholder="请输入内容"
            resize="none"
          />
        </el-form-item>
        <el-form-item label="图片">
          <el-upload
            action="#"
            list-type="picture-card"
            :auto-upload="false"
            :limit="9"
            multiple
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPostDialog = false">取消</el-button>
        <el-button type="primary" @click="submitPost">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed,onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { get, post } from '@/net';
import { 
  Search, User, Document, Plus, MoreFilled, View, 
  ChatDotRound, Star, Share, UserFilled, Medal, 
  Folder, EditPen 
} from '@element-plus/icons-vue';
import useUserInfo from '@/hooks/useUserInfo';

const route = useRoute();
const router = useRouter();
const { state: userInfo, loadUserInfo } = useUserInfo();

// 响应式数据
const activeNav = ref(0);
const hoverPost = ref(null);
const searchQuery = ref('');
const showPostDialog = ref(false);
const barId = route.params.id;
const postForm = ref({
  title: '',
  content: ''
});

// 贴吧信息
const barInfo = ref({
  id: 0,
  name: '',
  avatar: '',
  followerCount: 0,
  postCount: 0
});

// 是否已关注
const isFollowed = ref(false);


onMounted(() => {
  if (!userInfo.data.id) {
      loadUserInfo().then(() => {
        console.log("当前用户", userInfo)
        fetchBarInfo();
        checkFollowStatus();
      });
    } else {
        console.log("当前用户", userInfo)
        fetchBarInfo();
        checkFollowStatus();
    }
});

// 检查用户是否已关注该贴吧
const checkFollowStatus = async () => {
  try {
    if (!userInfo.data.id) return; // 未登录不检查
    
    const response = await post('/api/auth/quan/isFollowBar', { 
      barId: barId, 
      userId: userInfo.data.id 
    });
    console.log("检查关注状态", response)
    isFollowed.value = response; // 假设返回的data字段为布尔值
  } catch (error) {
    console.error('检查关注状态失败:', error);
    isFollowed.value = false; // 出错时默认未关注
  }
};

// 获取贴吧信息
const fetchBarInfo = async () => {
  try {
    const response = await get(`/api/unauth/quan/getBarInfo?barId=${barId}`);
    
    if (response) {
      barInfo.value = {
        id: response.id,
        name: response.name,
        avatar: response.avatar,
        followerCount: response.followerCount,
        postCount: response.postCount
      };
    }
  } catch (error) {
    console.error('获取贴吧信息失败:', error);
  }
};


// 关注/取消关注
// 关注/取消关注
const toggleFollow = async () => {
  try {
    if (!userInfo.data.id) {
      // 未登录处理，可以跳转到登录页
      router.push('/login');
      return;
    }

    if (isFollowed.value) {
      // 调用取消关注API
      await post('/api/auth/quan/unFollowBar', { 
        barId: barInfo.value.id, 
        userId: userInfo.data.id 
      });
      barInfo.value.followerCount--; // 更新关注数
    } else {
      // 调用关注API
      await post('/api/auth/quan/followBar', { 
        barId: barInfo.value.id, 
        userId: userInfo.data.id 
      });
      barInfo.value.followerCount++; // 更新关注数
    }
    isFollowed.value = !isFollowed.value;
  } catch (error) {
    console.error('操作失败:', error);
    ElMessage.error(isFollowed.value ? '取消关注失败' : '关注失败');
  }
};


// 数字格式化过滤器
const formatNumber = (value) => {
  if (value >= 10000) {
    return (value / 10000).toFixed(1) + '万';
  }
  return value;
};

// 导航项
const navItems = ref([
  '首页', '精华', '热门', '视频', '图片', '吧务', '活动'
]);

// 帖子数据
const posts = ref([
  {
    id: 1,
    title: '抖音被小红书入侵了是吧？',
    content: '拳师的角度越来越新奇了',
    userAvatar: 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100',
    username: '易炎嘉',
    time: '07-24 10:30',
    views: '7',
    comments: '3'
  },
  {
    id: 2,
    title: '为什么没木鼠找我',
    content: '剪完发感觉自己变帅了，为什么还没木薯找我',
    userAvatar: 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100',
    username: '被现实打垮',
    time: '07-24 09:15',
    views: '194',
    comments: '42'
  },
  {
    id: 3,
    title: '把黑猴看成流浪地球的话，明末现在算不算上海堡垒？',
    content: '经典刷开门又把门带上',
    userAvatar: 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100',
    username: '暴雨之夜',
    time: '07-24 08:45',
    views: '62',
    comments: '18'
  },
  {
    id: 4,
    title: '给哥们谈到绘梨衣了',
    content: '上辈子修来的福气谈到这么好的女朋友，鼠鼠必不放手',
    userAvatar: 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100',
    username: '上辈子修来的福气',
    time: '07-23 22:10',
    views: '4422',
    comments: '521'
  }
]);

// 友情贴吧
const friendBars = ref([
  { id: 1, name: '焕得醉夕霞', avatar: 'https://via.placeholder.com/24' },
  { id: 2, name: '新世界的...', avatar: 'https://via.placeholder.com/24' }
]);

// 今日热议
const hotTopics = ref([
  { id: 1, rank: 1, title: '如何看待最近的明星事件', views: '12.5万', comments: '3.2万' },
  { id: 2, rank: 2, title: '游戏圈最新动态讨论', views: '8.7万', comments: '1.9万' },
  { id: 3, rank: 3, title: '技术分享：前端开发新趋势', views: '6.3万', comments: '1.2万' },
  { id: 4, rank: 4, title: '生活小技巧分享', views: '5.1万', comments: '0.8万' },
  { id: 5, rank: 5, title: '美食探店合集', views: '4.2万', comments: '0.7万' }
]);

// 计算属性
const isMobile = computed(() => window.innerWidth < 768);

// 方法
const navigateToBar = (barName) => {
  console.log('跳转到贴吧:', barName);
  // router.push({ name: 'bar', params: { name: barName } });
};

const navigateToPost = (postId) => {
  console.log('查看帖子:', postId);
  // router.push({ name: 'post', params: { id: postId } });
};

const showPostMenu = (postId) => {
  console.log('显示帖子菜单:', postId);
};

const handleSearch = () => {
  console.log('搜索:', searchQuery.value);
};

const submitPost = () => {
  console.log('提交帖子:', postForm.value);
  showPostDialog.value = false;
  postForm.value = { title: '', content: '' };
};
</script>

<style scoped lang="scss">
.tieba-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px 40px;
  position: relative;
}

.bar-header {
  margin: 20px 0 30px;
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8eb 100%);
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.bar-info {
  display: flex;
  align-items: center;
  gap: 20px;
}

.bar-avatar {
  flex-shrink: 0;
  :deep(.el-avatar) {
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }
}

.bar-meta {
  flex: 1;
}

.bar-name {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: #1a1a1a;
  cursor: pointer;
  transition: color 0.2s;
  
  &:hover {
    color: var(--el-color-primary);
  }
}

.bar-stats {
  display: flex;
  gap: 20px;
  margin-bottom: 12px;
  
  .stat-item {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    font-size: 14px;
    color: #666;
    
    .el-icon {
      font-size: 16px;
    }
    
    .stat-value {
      font-weight: 500;
    }
  }
}

.bar-category {
  .el-tag {
    background-color: rgba(0, 0, 0, 0.05);
    border: none;
    color: #666;
  }
}

.follow-btn {
  align-self: flex-start;
  padding: 8px 16px;
  font-weight: 500;
  
  .el-icon {
    margin-right: 4px;
  }
}

.bar-nav {
  margin-bottom: 24px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
  
  :deep(.el-scrollbar__bar) {
    display: none;
  }
  
  .nav-items {
    display: flex;
    gap: 4px;
    white-space: nowrap;
  }
  
  .nav-btn {
    padding: 8px 16px;
    font-size: 15px;
    color: #666;
    border-radius: 6px;
    transition: all 0.2s;
    
    &:hover {
      color: var(--el-color-primary);
      background-color: rgba(64, 158, 255, 0.1);
    }
    
    &.active {
      color: var(--el-color-primary);
      background-color: rgba(64, 158, 255, 0.1);
      font-weight: 500;
    }
  }
}

.main-content {
  display: flex;
  gap: 24px;
}

.post-list {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.post-item {
  padding: 20px;
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.03);
  transition: all 0.3s ease;
  cursor: pointer;
  position: relative;
  
  &:hover {
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
    transform: translateY(-2px);
  }
}

.post-header {
  display: flex;
  align-items: center;
  margin-bottom: 14px;
  gap: 12px;
}

.user-avatar {
  flex-shrink: 0;
}

.user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
  
  .username {
    font-size: 15px;
    font-weight: 500;
    color: #1a1a1a;
  }
  
  .post-time {
    font-size: 12px;
    color: #999;
  }
}

.more-btn {
  position: absolute;
  top: 16px;
  right: 16px;
  color: #999;
  
  &:hover {
    color: #666;
  }
}

.post-title {
  margin: 0 0 12px 0;
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  line-height: 1.4;
}

.post-content {
  margin: 0 0 16px 0;
  font-size: 15px;
  color: #666;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.post-footer {
  display: flex;
  align-items: center;
  gap: 20px;
  
  .action-btn {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    font-size: 13px;
    color: #999;
    transition: color 0.2s;
    
    .el-icon {
      font-size: 16px;
    }
    
    &:hover {
      color: var(--el-color-primary);
    }
  }
}

.pagination {
  margin-top: 24px;
  justify-content: center;
}

.sidebar {
  width: 300px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 20px;
  
  @media (max-width: 992px) {
    display: none;
  }
}

.search-box {
  :deep(.el-input-group__append) {
    background-color: var(--el-color-primary);
    color: white;
    
    &:hover {
      opacity: 0.9;
    }
  }
}

.sidebar-section {
  background-color: #fff;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.03);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  
  h3 {
    margin: 0;
    font-size: 16px;
    font-weight: 600;
    color: #1a1a1a;
  }
  
  .el-button {
    padding: 0;
    color: #999;
    
    &:hover {
      color: var(--el-color-primary);
    }
  }
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 0;
  font-size: 14px;
  color: #666;
  border-bottom: 1px solid #f5f5f5;
  
  .el-icon {
    color: #999;
  }
  
  &:last-child {
    border-bottom: none;
  }
}

.apply-btn {
  width: 100%;
  margin-top: 12px;
  font-weight: 500;
}

.friend-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 0;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  transition: color 0.2s;
  border-bottom: 1px solid #f5f5f5;
  
  &:hover {
    color: var(--el-color-primary);
  }
  
  .el-avatar {
    flex-shrink: 0;
  }
  
  &:last-child {
    border-bottom: none;
  }
}

.hot-topic {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  cursor: pointer;
  
  &:hover .topic-title {
    color: var(--el-color-primary);
  }
}

.topic-rank {
  width: 24px;
  height: 24px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: #999;
  background-color: #f5f5f5;
  border-radius: 4px;
  
  &.top-rank {
    color: white;
    background-color: var(--el-color-primary);
    font-weight: 500;
  }
}

.topic-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.topic-title {
  font-size: 14px;
  color: #333;
  transition: color 0.2s;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.topic-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #999;
}

.post-create-btn {
  position: fixed;
  right: 40px;
  bottom: 40px;
  padding: 12px 24px;
  font-weight: 500;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
  
  .el-icon {
    margin-right: 6px;
  }
  
  @media (max-width: 768px) {
    right: 20px;
    bottom: 20px;
    padding: 12px 16px;
    
    span {
      display: none;
    }
  }
}

@media (max-width: 768px) {
  .tieba-container {
    padding: 0 12px 20px;
  }
  
  .bar-header {
    padding: 16px;
    margin: 12px 0 20px;
  }
  
  .bar-name {
    font-size: 20px;
  }
  
  .post-item {
    padding: 16px;
  }
  
  .post-title {
    font-size: 16px;
  }
  
  .post-content {
    font-size: 14px;
  }
}
</style>