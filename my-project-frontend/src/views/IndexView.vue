<template>
  <div class="index-container">
    <el-popover
      placement="bottom-end"
      trigger="click"
      width="350"
      v-model:visible="messageVisible"
    >
      <template #reference>
        <div class="message-bell" @click="handleMessageClick">
          <el-badge :value="unreadCount" :max="99" class="badge">
            <el-icon :size="20"><bell /></el-icon>
          </el-badge>
        </div>
      </template>
      
      <el-tabs v-model="activeMessageTab" class="message-tabs">
        <el-tab-pane label="评论回复" name="comment">
          <div class="message-list">
            <div v-for="item in commentMessages" :key="item.id" class="message-item" @click="showCommentDetail(item)">
                    <!-- <div class="message-avatar">
                      <el-avatar :src="item.senderAvatar || '/images/default-avatar.png'" />
                    </div> -->
                    <div class="message-content">
                      <div class="message-header">
                        <span class="message-time">{{ formatTime(item.createdAt) }}</span>
                      </div>
                      <div class="message-text">
                        <span class="sender-name">{{ item.senderName }}</span>
                        <span class="message-content-text">{{ item.content }}</span>
                        <span class="related-words">"{{ item.relatedWords }}"</span>
                      </div>
                    </div>
                  </div>
            <!-- <div v-for="item in commentMessages" :key="item.id" class="message-item">
              <div class="message-avatar">
                <el-avatar :src="item.senderAvatar || '/images/default-avatar.png'" />
              </div>
              <div class="message-content">
                <div class="message-header">
                  <span class="message-user">{{ item.senderName }}</span>
                  <span class="message-time">{{ formatTime(item.createTime) }}</span>
                </div>
                <div class="message-text">回复了你的评论: {{ item.content }}</div>
              </div>
            </div> -->
            <div v-if="commentLoading" class="loading-more">
              <el-icon class="is-loading"><Loading /></el-icon>
            </div>
            <div v-else-if="commentHasMore" class="load-more" @click="loadMoreComments">
              加载更多
            </div>
            <div v-else-if="commentMessages.length === 0" class="no-message">
              暂无消息
            </div>
          </div>
        </el-tab-pane>

        <!-- 评论详情对话框 -->
          <el-dialog
            v-model="commentDialogVisible"
            title="评论详情"
            width="60%"
            top="5vh"
            @closed="handleDialogClosed"
          >
            <div class="comment-dialog-container">
              <div class="comment-list" ref="commentListRef">
                <div 
                  v-for="comment in allComments" 
                  :key="comment.id" 
                  class="comment-item"
                  :class="{ 'highlight-comment': comment.id === currentCommentId }"
                  ref="commentItems"
                >
                  <div class="comment-header">
                    <el-avatar :src="comment.senderAvatar || '/images/default-avatar.png'" size="small" />
                    <span class="comment-user">{{ comment.senderName }}</span>
                    <span class="comment-time">{{ formatTime(comment.createdAt) }}</span>
                  </div>
                  <div class="comment-content">{{ comment.content }}</div>
                  <div v-if="comment.relatedWords" class="comment-related">
                    相关项目: {{ comment.relatedWords }}
                  </div>
                </div>
              </div>
            </div>
            <template #footer>
              <el-button @click="commentDialogVisible = false">关闭</el-button>
            </template>
          </el-dialog>
        
        <el-tab-pane label="新增关注" name="follow">
          <div class="message-list">
            <div v-for="item in followMessages" :key="item.id" class="message-item">
              <div class="message-avatar">
                <el-avatar :src="item.senderAvatar || '/images/default-avatar.png'" />
              </div>
              <div class="message-content">
                <div class="message-header">
                  <span class="message-user">{{ item.senderName }}</span>
                  <span class="message-time">{{ formatTime(item.createTime) }}</span>
                </div>
                <div class="message-text">关注了你</div>
              </div>
            </div>
            <div v-if="followLoading" class="loading-more">
              <el-icon class="is-loading"><Loading /></el-icon>
            </div>
            <div v-else-if="followHasMore" class="load-more" @click="loadMoreFollows">
              加载更多
            </div>
            <div v-else-if="followMessages.length === 0" class="no-message">
              暂无消息
            </div>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="点赞通知" name="like">
          <div class="message-list">
            <div v-for="item in likeMessages" :key="item.id" class="message-item">
              <div class="message-avatar">
                <el-avatar :src="item.senderAvatar || '/images/default-avatar.png'" />
              </div>
              <div class="message-content">
                <div class="message-header">
                  <span class="message-user">{{ item.senderName }}</span>
                  <span class="message-time">{{ formatTime(item.createTime) }}</span>
                </div>
                <div class="message-text">点赞了你的{{ item.targetType === 'project' ? '项目' : '评论' }}</div>
              </div>
            </div>
            <div v-if="likeLoading" class="loading-more">
              <el-icon class="is-loading"><Loading /></el-icon>
            </div>
            <div v-else-if="likeHasMore" class="load-more" @click="loadMoreLikes">
              加载更多
            </div>
            <div v-else-if="likeMessages.length === 0" class="no-message">
              暂无消息
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-popover>

    <!-- 头像下拉菜单 -->
    <el-dropdown class="avatar-dropdown" trigger="click">
      <div class="avatar-wrapper">
        <el-avatar :src="state.data.avatarUrl || '/images/default-avatar.png'" />
      </div>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item @click="changeDisplayMode('myInfo')">
            <i class="el-icon-user"></i>我的
          </el-dropdown-item>
          <el-dropdown-item divided @click="userLogout">
            <i class="el-icon-switch-button"></i>退出登录
          </el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>

    <el-menu 
      mode="horizontal"
      background-color="#f8f9fa"
      text-color="#2c3e50"
      active-text-color="#409EFF"
      class="nav-menu"
    >
      <el-sub-menu index="1">
        <template #title><i class="el-icon-data-analysis"></i>项目展示</template>
        <el-menu-item index="fuye" @click="changeDisplayMode('project')">
          热门副业
        </el-menu-item>
        <el-menu-item index="ranking" @click="handleMenuClick('ranking')">
          副业榜单
        </el-menu-item>
      </el-sub-menu>

      <el-sub-menu index="2">
        <template #title><i class="el-icon-user"></i>找人合作</template>
        <el-menu-item index="partner-map" @click="router.push({ name: 'partner-map' })">合作地图</el-menu-item>
        <el-menu-item index="skill-match" @click="changeDisplayMode('skillMatch')">技能匹配</el-menu-item>
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

      
    </el-menu>

    <!-- 搜索区域 -->
    <!-- 搜索 + 项目展示：只在非技能匹配页面展示 -->
    <div v-if="displayMode === 'project'">
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
              <el-card class="project-card" shadow="hover" @click="goToDetail(project)">
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

    <!-- 技能匹配展示区域 -->
    <SkillMatch v-if="displayMode === 'skillMatch'" />

    <MyInfo v-if="displayMode === 'myInfo'" />

  </div>
</template>

<script setup>
import { ref, onMounted ,computed, watch} from 'vue';
import { Loading, ArrowDown,Bell } from '@element-plus/icons-vue';
import router from "@/router";
import { logout, post, get } from '@/net';
import { ElMessage } from 'element-plus';
import SkillMatch from '@/views/SkillMatch.vue';
import MyInfo from '@/views/My.vue';
import useUserInfo from '@/hooks/useUserInfo';

const { state, loadUserInfo } = useUserInfo();
const displayMode = ref('project')
const projectList = ref([]);
const loading = ref(false);
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);
const hasMore = ref(true);
const categories = ref([]);
const difficulties = ref([]);
const search = ref({ name: '', category: '', difficulties: [] });


// 消息相关状态
const messageVisible = ref(false);
const activeMessageTab = ref('comment');
const unreadCount = ref(0);
// 评论消息
const commentMessages = ref([]);
const commentPage = ref(1);
const commentSize = ref(10);
const commentHasMore = ref(true);
const commentLoading = ref(false);
// 关注消息
const followMessages = ref([]);
const followPage = ref(1);
const followHasMore = ref(true);
const followLoading = ref(false);
// 点赞消息
const likeMessages = ref([]);
const likePage = ref(1);
const likeHasMore = ref(true);
const likeLoading = ref(false);

//评论回复相关
const commentDialogVisible = ref(false);
const allComments = ref([]);
const currentCommentId = ref(null);
const commentListRef = ref(null);
const commentItems = ref([]);

// 显示评论详情
const showCommentDetail = async (item) => {
  try {
    // 获取所有评论
    const res = await get(`/api/auth/project/commentShow?projectId=${item.projectId}`);
    allComments.value = res || [];
    currentCommentId.value = item.commentId;
    commentDialogVisible.value = true;
    
    // 在下一个tick滚动到指定评论
    nextTick(() => {
      scrollToCurrentComment();
    });
  } catch (e) {
    console.error('获取评论详情失败:', e);
    ElMessage.error('加载评论详情失败');
  }
};

// 滚动到当前评论
const scrollToCurrentComment = () => {
  if (commentItems.value && currentCommentId.value) {
    const index = allComments.value.findIndex(c => c.id === currentCommentId.value);
    if (index !== -1 && commentItems.value[index]) {
      commentItems.value[index].scrollIntoView({
        behavior: 'smooth',
        block: 'center'
      });
    }
  }
};

// 对话框关闭时重置状态
const handleDialogClosed = () => {
  allComments.value = [];
  currentCommentId.value = null;
};


// 获取未读消息数
const fetchUnreadCount = async () => {
  try {
    const res = await get('/api/auth/msg/unreadMsgCount');
    unreadCount.value = res || 0;
  } catch (e) {
    console.error('获取未读消息数失败:', e);
  }
};

const formatTime = (timeString) => {
  if (!timeString) return '';
  const date = new Date(timeString);
  return `${date.getFullYear()}-${padZero(date.getMonth() + 1)}-${padZero(date.getDate())} ${padZero(date.getHours())}:${padZero(date.getMinutes())}`;
};

const padZero = (num) => {
  return num < 10 ? `0${num}` : num;
};

// 加载评论消息
const loadCommentMessages = async () => {
  if (commentLoading.value) return;
  commentLoading.value = true;
  try {
    const res = await post('/api/auth/msg/msgOfCommentList', {
      page: commentPage.value,
      size: commentSize.value
    });
    commentMessages.value = [...commentMessages.value, ...(res.records || [])];
    commentHasMore.value = commentMessages.value.length < (res.total || 0);
  } catch (e) {
    console.error('加载评论消息失败:', e);
  } finally {
    commentLoading.value = false;
  }
};

// 加载更多评论
const loadMoreComments = () => {
  commentPage.value += 1;
  loadCommentMessages();
};

// 加载关注消息
const loadFollowMessages = async () => {
  if (followLoading.value) return;
  followLoading.value = true;
  try {
    const res = await post('/api/auth/msg/msgOfFollowed', {
      page: followPage.value,
      size: commentSize.value
    });
    followMessages.value = [...followMessages.value, ...(res.records || [])];
    followHasMore.value = followMessages.value.length < (res.total || 0);
  } catch (e) {
    console.error('加载关注消息失败:', e);
  } finally {
    followLoading.value = false;
  }
};

// 加载更多关注
const loadMoreFollows = () => {
  followPage.value += 1;
  loadFollowMessages();
};

// 加载点赞消息
const loadLikeMessages = async () => {
  if (likeLoading.value) return;
  likeLoading.value = true;
  try {
    const res = await post('/api/auth/msg/msgOfLiked', {
      page: likePage.value,
      size: commentSize.value
    });
    likeMessages.value = [...likeMessages.value, ...(res.records || [])];
    likeHasMore.value = likeMessages.value.length < (res.total || 0);
  } catch (e) {
    console.error('加载点赞消息失败:', e);
  } finally {
    likeLoading.value = false;
  }
};

// 加载更多点赞
const loadMoreLikes = () => {
  likePage.value += 1;
  loadLikeMessages();
};

// 点击消息图标
const handleMessageClick = () => {
  if (!messageVisible.value) {
    // 每次打开时刷新当前tab数据
    switch (activeMessageTab.value) {
      case 'comment':
        commentMessages.value = [];
        commentPage.value = 1;
        loadCommentMessages();
        break;
      case 'follow':
        followMessages.value = [];
        followPage.value = 1;
        loadFollowMessages();
        break;
      case 'like':
        likeMessages.value = [];
        likePage.value = 1;
        loadLikeMessages();
        break;
    }
    // 如果有未读消息，标记为已读
    if (unreadCount.value > 0) {
      markMessagesAsRead();
    }
  }
};

// 标记消息为已读
const markMessagesAsRead = async () => {
  try {
    await post('/api/auth/project/markMsgAsRead');
    unreadCount.value = 0;
  } catch (e) {
    console.error('标记消息为已读失败:', e);
  }
};

// 监听tab切换
watch(activeMessageTab, (newVal) => {
  if (messageVisible.value) {
    switch (newVal) {
      case 'comment':
        if (commentMessages.value.length === 0) {
          loadCommentMessages();
        }
        break;
      case 'follow':
        if (followMessages.value.length === 0) {
          loadFollowMessages();
        }
        break;
      case 'like':
        if (likeMessages.value.length === 0) {
          loadLikeMessages();
        }
        break;
    }
  }
});




function changeDisplayMode(mode) {
  displayMode.value = mode;
}

//跳转到列表详情页
function goToDetail(project) {
  router.push({ name: 'project-detail', params: { id: project.id } });
}

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

    if (!res?.records) {
      console.warn("接口返回异常结构：", res);
      return;
    }

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

const loadProjects = async () => {
  fetchOptions();
  fetchProjectListData({
    category: search.value.category,
    difficulty: search.value.difficulties,
    projectName: search.value.name
  });
};

const onSearch = () => {
  currentPage.value = 1;
  projectList.value = [];
  hasMore.value = true;
  fetchProjectListData({
    category: search.value.category,
    difficulty: search.value.difficulties,
    projectName: search.value.name
  });
};

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

onMounted(() => {
  console.log("indexView页面的用户数据",state)
  if (!state.data.id) {
    loadUserInfo().then(() => {
      loadProjects();
      fetchUnreadCount();
    });
  } else {
    fetchUnreadCount();
    loadProjects();
  }
});

function userLogout() {
  logout(() => router.push("/"));
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

/* 新增头像下拉菜单样式 */
.avatar-dropdown {
  position: absolute;
  right: 20px;
  top: 10px;
  z-index: 1001;
}

.avatar-wrapper {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.avatar-wrapper .el-icon {
  margin-left: 5px;
  color: #666;
}

/* 新增消息通知样式 */
.message-bell {
  position: absolute;
  right: 70px;
  top: 15px;
  z-index: 1001;
  cursor: pointer;
  padding: 8px;
  border-radius: 50%;
  transition: background-color 0.3s;
}

.message-bell:hover {
  background-color: #f0f0f0;
}

.badge {
  display: flex;
  align-items: center;
  justify-content: center;
}

.message-tabs {
  padding: 0 10px;
}

.message-list {
  max-height: 400px;
  overflow-y: auto;
}

.message-item {
  display: flex;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.message-avatar {
  margin-right: 12px;
}

.message-content {
  flex: 1;
}

.message-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 4px;
}

.message-user {
  font-weight: bold;
  color: #333;
}

.message-time {
  font-size: 12px;
  color: #999;
}

.message-text {
  font-size: 14px;
  color: #666;
  line-height: 1.5;
}

.load-more, .no-message {
  text-align: center;
  padding: 10px;
  color: #999;
  cursor: pointer;
}

.load-more:hover {
  color: #409EFF;
}

.no-message {
  cursor: default;
}

.loading-more {
  text-align: center;
  padding: 10px;
}

.message-text {
  font-size: 14px;
  line-height: 1.5;
}

.sender-name {
  color: #409EFF; /* 蓝色显示发送者名字 */
  font-weight: bold;
  margin-right: 5px;
}

.message-content-text {
  color: #666; /* 灰色显示消息内容 */
  margin-right: 5px;
}

.related-words {
  color: #67C23A; /* 绿色显示相关词 */
  font-style: italic;
}
</style>