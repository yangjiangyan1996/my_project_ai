<template>
  <div class="tieba-layout">
    <!-- 左侧分类导航 -->
    <div class="left-sidebar">
      <div class="sidebar-header">
        <h3>圈分类</h3>
        <el-button type="text" @click="refreshCategories">
          <el-icon><Refresh /></el-icon>
        </el-button>
      </div>
      
      <div class="category-list">
        <div 
          v-for="category in firstLevelCategories" 
          :key="category.code"
          class="category-group"
        >
          <div class="group-header" @click="toggleCategory(category)">
            <span>{{ category.desc }}</span>
            <el-icon :class="{ 'rotate-180': category.expanded }">
              <ArrowDown />
            </el-icon>
          </div>
          
            <el-collapse-transition>
            <div v-show="category.expanded" class="group-tags">
              <div v-if="loadingBars[category.code]" class="loading-bars">
                <el-icon class="is-loading"><Loading /></el-icon>
              </div>
              <template v-else>
                <div 
                  v-for="bar in categoryBars[category.code]"
                  :key="bar.id"
                  class="bar-item"
                  :class="{ 'active': activeBar === bar.id }"
                  @click="goToUserProfile(bar.id)" 
                >
                  <el-avatar :size="40" :src="bar.avatar" class="bar-avatar" />
                  <div class="bar-info">
                    <div class="bar-name">{{ bar.name }}</div>
                    <div class="bar-stats">
                      <span class="follower-count">
                        <el-icon><User /></el-icon>
                        {{ bar.followerCount }}
                      </span>
                      <span class="post-count">
                        <el-icon><Document /></el-icon>
                        {{ bar.postCount }}
                      </span>
                    </div>
                  </div>
                </div>
              </template>
            </div>
          </el-collapse-transition>
        </div>
      </div>
      
      <div class="my-bars">
        <h4>我关注的</h4>
        <div v-if="followedBars.length > 0" class="followed-list">
          <div 
            v-for="bar in followedBars"
            :key="bar.id"
            class="followed-bar"
             @click="goToUserProfile(bar.id)"
          >
            <el-avatar :size="32" :src="bar.avatar" />
            <div class="bar-info">
              <span class="bar-name">{{ bar.name }}</span>
              <div class="bar-stats">
                <span><el-icon><User /></el-icon> {{ bar.followerCount }}</span>
                <span><el-icon><Document /></el-icon> {{ bar.postCount }}</span>
              </div>
            </div>
            <el-badge :value="bar.unread" :max="99" class="unread-count" />
          </div>
        </div>
        <el-empty v-else description="暂无关注圈" :image-size="80" />

        <el-button 
          v-if="followedBars.length < followedTotal"
          type="text" 
          size="small" 
          @click="loadMoreFollowed"
          class="load-more-btn"
        >
          加载更多
        </el-button>
      </div>
    


    </div>
    
    <!-- 中间帖子列表 -->
    <div class="main-content" style="width: 800px;">
      <div class="content-header">
        <h3>{{ currentCategory || '全部圈' }}</h3>
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
          <!-- <el-tab-pane label="热门" name="hot"></el-tab-pane>
          <el-tab-pane label="精华" name="featured"></el-tab-pane> -->
        </el-tabs>
      </div>
      
      <div 
        class="post-list"
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
            
            <div v-if="post.avatar && post.avatar.length > 0" class="post-avatar">
              <el-image
                v-for="(img, index) in post.avatar.slice(0, 3)"
                :key="index"
                :src="img"
                :preview-src-list="post.avatar"
                fit="cover"
                class="post-image"
                :class="{ 'last-image-more': index === 2 && post.avatar.length > 3 }"
              >
                <template #error>
                  <div class="image-error">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
                <div v-if="index === 2 && post.avatar.length > 3" class="avatar-more">
                  +{{ post.avatar.length - 3 }}
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
      </div>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          @current-change="handlePageChange"
        />
      </div>
    </div>
    
    <!-- 右侧边栏 -->
    <div class="right-sidebar">
      <div class="sidebar-section">
        <el-button 
          type="primary" 
          @click="showCreateDialog"
          class="create-bar-btn"
          style="width: 100%"
        >
          <el-icon><Plus /></el-icon>
          新建圈
        </el-button>
      </div>

      <!-- 右侧边栏 - 热门部分修改为相关圈子 -->
        <div class="sidebar-section">
          <div class="section-header">
            <h4>热门</h4>
            <el-button type="text" @click="refreshFriendBars">
              <el-icon><Refresh /></el-icon>
              换一换
            </el-button>
          </div>
          
          <div class="hot-bar-list">
            <div 
              v-for="bar in friendBars"
              :key="bar.id"
              class="hot-bar-item"
              @click="goToUserProfile(bar.id)"
            >
              <el-avatar :size="40" :src="bar.avatar" />
              <div class="bar-info">
                <span class="bar-name">{{ bar.name }}</span>
                <span class="bar-stats">{{ bar.followerCount }}关注 · {{ bar.postCount }}帖子</span>
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
          
          <div class="pagination-info">
            第 {{ friendBarCurrentPage }} 页 / 共 {{ friendBarTotalPages }} 页
          </div>
        </div>
      
      <div class="sidebar-section">
        <div class="section-header">
          <h4>圈热议榜</h4>
          <!-- <el-button type="text" @click="refreshHotTopics">
            <el-icon><Refresh /></el-icon>
            换一换
          </el-button> -->
        </div>
        
        <div class="hot-topics">
          <div 
            v-for="topic in hotTopics"
            :key="topic.id"
            class="topic-item"
            @click="navigateToPost(topic.id)"
          >
            <div class="topic-rank" :class="getRankClass(topic.rank)">
              {{ topic.rank }}
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

  <!-- 创建圈对话框 -->
  <el-dialog
    v-model="createDialogVisible"
    title="新建圈"
    width="600px"
    :before-close="handleClose"
  >
    <el-form 
      ref="createFormRef" 
      :model="createForm" 
      :rules="createRules"
      label-width="100px"
    >
      <el-form-item label="圈名称" prop="name">
        <el-input 
          v-model="createForm.name" 
          placeholder="请输入圈名称"
          clearable
        />
      </el-form-item>
      
    <el-form-item label="圈分类" required>
      <div class="category-selectors">
        <el-select
          v-model="createForm.firstCategory"
          placeholder="请选择一级分类"
          style="width: 48%; margin-right: 4%"
          clearable
          @change="handleFirstCategoryChange"
        >
          <el-option
            v-for="item in categoryOptions"
            :key="item.code"
            :label="item.desc"
            :value="item.code"
          />
        </el-select>
        
        <el-select
          v-model="createForm.secondCategory"
          placeholder="请选择二级分类"
          style="width: 48%"
          clearable
          :disabled="!createForm.firstCategory"
        >
          <el-option
            v-for="sub in secondCategoryOptions"
            :key="sub.code"
            :label="sub.desc"
            :value="sub.code"
          />
        </el-select>
      </div>
    </el-form-item>
      


      <!-- 1. 修改头像上传组件部分 -->
      <el-form-item label="圈图标" prop="avatar">
        <el-upload
          class="avatar-uploader"
          :action="uploadAction"
          :headers="uploadHeaders"
          :show-file-list="false"
          :on-success="handleAvatarSuccess"
          :before-upload="beforeAvatarUpload"
        >
          <img v-if="createForm.avatar" :src="createForm.avatar" class="avatar">
          <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
        </el-upload>
        <div class="upload-tip">建议尺寸 200×200px，支持 JPG/PNG 格式</div>
      </el-form-item>

      <el-form-item label="圈简介" prop="description">
        <el-input
          v-model="createForm.description"
          type="textarea"
          :rows="4"
          placeholder="请输入圈简介"
          maxlength="200"
          show-word-limit
        />
      </el-form-item>
    </el-form>
    
    <template #footer>
      <el-button @click="createDialogVisible = false">取消</el-button>
      <el-button 
        type="primary" 
        @click="submitCreateForm"
        :loading="submitting"
      >
        创建
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, onMounted,getCurrentInstance } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { 
  ArrowDown, Refresh, Search, View, 
  ChatDotRound, Star, StarFilled, 
  Plus, CircleCheckFilled, Picture,
  Loading, User, Document
} from '@element-plus/icons-vue'
import { logout, post, get,takeAccessToken } from '@/net';
import useUserInfo from '@/hooks/useUserInfo';

//上传接口，vue中添加 computed,getCurrentInstance
const { proxy } = getCurrentInstance();
const uploadAction = computed(() => proxy.$uploadAction());
// 创建计算属性来获取上传headers
const uploadHeaders = computed(() => {
  const token = takeAccessToken();
  return {
    'Authorization': token ? `Bearer ${token}` : ''
  };
});


const router = useRouter()
const { state: userInfo, loadUserInfo } = useUserInfo();



// 获取我关注的圈子
const fetchFollowedBars = async (page = 1, size = 5) => {
  try {
     if(!userInfo.data.id) {
         console.log("未登录，不获取我关注的圈子")
         return;
     }
    const response = await post('/api/auth/quan/myFavoriteBar', {
      page: page,
      size: size
    });
    
    if (response && response.records) {
      followedBars.value = response.records.map(bar => ({
        id: bar.id,
        name: bar.name,
        avatar: bar.avatar,
        followerCount: bar.followerCount,
        postCount: bar.postCount,
        unread: 0 // 初始未读消息数为0
      }));
    }
  } catch (error) {
    console.error('获取关注的圈子失败:', error);
    ElMessage.error('获取关注的圈子失败，请稍后重试');
  }
};



// 热议榜数据
 const hotTopics = ref([])
// 分页相关状态
const followedPage = ref(1);
const followedPageSize = ref(5);
const followedTotal = ref(0);



// 加载更多
const loadMoreFollowed = () => {
  if (followedBars.value.length < followedTotal.value) {
    fetchFollowedBars(followedPage.value + 1, followedPageSize.value);
  }
};

// 我关注的
const followedBars = ref([])

// 状态管理
const activeTag = ref(null)
const currentCategory = ref(null)
const activePostTab = ref('latest')
const searchQuery = ref('')
const loadingPosts = ref(false)
const page = ref(1)
const pageSize = ref(5)

// 帖子相关状态
const posts = ref([])
const total = ref(0)
const currentPage = ref(1)
const barId = ref(null) // null表示全部圈

// 创建圈相关状态
const createDialogVisible = ref(false)
const submitting = ref(false)
const createFormRef = ref(null)


// 状态管理
const firstLevelCategories = ref([]); // 一级分类列表
const categoryBars = ref({}); // 各分类下的圈子列表 {分类code: [圈子列表]}
const loadingBars = ref({}); // 各分类的加载状态 {分类code: boolean}
const activeBar = ref(null); // 当前选中的圈子ID

const createForm = ref({
  name: '',
  firstCategory: null,
  secondCategory: null,
  avatar: '',
  description: ''
})

const createRules = ref({
  name: [
    { required: true, message: '请输入圈名称', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  firstCategory: [
    { required: true, message: '请选择一级分类', trigger: 'change' }
  ],
  secondCategory: [
    { required: true, message: '请选择二级分类', trigger: 'change' }
  ],
  avatar: [
    { required: true, message: '请上传圈图标', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请输入圈简介', trigger: 'blur' },
    { min: 10, max: 200, message: '长度在 10 到 200 个字符', trigger: 'blur' }
  ]
})

const categoryOptions = ref([])
const secondCategoryOptions = ref([])

// 热门相关状态
const friendBars = ref([])
const friendBarCurrentPage = ref(1)
const friendBarPageSize = ref(5)
const friendBarTotal = ref(0)

const categoryProps = ref({
  value: 'code',
  label: 'desc',
  children: 'subs'
})


// 获取帖子列表
const fetchPosts = async (page = 1) => {
  loadingPosts.value = true;
  try {
    const params = {
      page,
      size: pageSize.value,
      barId: barId.value
    };

    if (searchQuery.value) params.keyword = searchQuery.value;
    if (userInfo?.data?.id) params.userId = userInfo.data.id;

    const response = await post('/api/unauth/quan/getTiePageOfBar', params);

    if (response?.records) {
      // 直接替换数据，不再区分第一页和其他页
      posts.value = response.records.map(item => ({
        id: item.id,
        title: item.title,
        content: item.content || '',
        userAvatar: item.avatar || 'https://via.placeholder.com/40?text=用户',
        username: item.createdName || '匿名用户',
        time: formatTime(item.createdTime),
        views: item.views || 0,
        comments: item.comments || 0,
        likes: item.likes || 0,
        liked: item.liked || false,
        barName: item.barName || '未知圈子',
        avatar: item.avatar || []
      }));
      
      total.value = response.total || 0;
      currentPage.value = page; // 确保当前页码同步
    }
  } catch (error) {
    console.error('获取帖子列表失败:', error);
    ElMessage.error('获取帖子失败，请稍后重试');
  } finally {
    loadingPosts.value = false;
  }
};

// 新增分页切换方法
const handlePageChange = (newPage) => {
  fetchPosts(newPage);
};




// 获取相关圈子
const fetchFriendBars = async (page = 1) => {
  try {
    const params = {
      page: page,
      size: friendBarPageSize.value,
      barId: null
    }

    if (userInfo?.data?.id) {
      params.userId = userInfo.data.id
    }

    const response = await post('/api/unauth/quan/getRelationBar',params)
    
    if (response && response.records) {
      friendBars.value = response.records.map(bar => ({
        id: bar.id,
        name: bar.name,
        avatar: bar.avatar,
        followerCount: bar.followerCount,
        postCount: bar.postCount,
        description: bar.description,
        followed: bar.followed // 初始化为未关注
      }))
      friendBarTotal.value = response.total || 0
      friendBarCurrentPage.value = page
    }
  } catch (error) {
    console.error('获取相关圈子失败:', error)
    ElMessage.error('获取相关圈子失败，请稍后重试')
  }
}


// 时间格式化函数
const formatTime = (timeStr) => {
  if (!timeStr) return '未知时间'
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now - date
  
  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour
  
  if (diff < minute) {
    return '刚刚'
  } else if (diff < hour) {
    return `${Math.floor(diff / minute)}分钟前`
  } else if (diff < day) {
    return `${Math.floor(diff / hour)}小时前`
  } else if (diff < 7 * day) {
    return `${Math.floor(diff / day)}天前`
  } else {
    return date.toLocaleDateString()
  }
}

const goToUserProfile = (barId) => {
  console.log("访问帖子详情页",barId)
  // router.push(`/index/user/${userId}`)
  window.open(`/index/quan/QuanDetail/${barId}`, '_blank');
}

// 获取一级分类
const fetchFirstLevelCategories = async () => {
  try {
    const response = await get('/api/unauth/common/category');
    if (response && response.length > 0) {
      firstLevelCategories.value = response.map(item => ({
        code: item.code,
        desc: item.desc,
        expanded: false // 默认折叠
      }));
    } else {
      ElMessage.warning('暂无分类数据');
      firstLevelCategories.value = [];
    }
  } catch (error) {
    console.error('获取分类数据失败:', error);
    ElMessage.error('获取分类数据失败，请稍后重试');
    firstLevelCategories.value = [];
  }
};

// 切换分类展开状态
const toggleCategory = async (category) => {
  category.expanded = !category.expanded;
  
  // 如果展开且尚未加载过该分类的圈子，则加载
  if (category.expanded && !categoryBars.value[category.code]) {
    await fetchBarsByCategory(category.code);
  }
};

// 根据分类获取圈子列表
const fetchBarsByCategory = async (categoryCode) => {
  loadingBars.value[categoryCode] = true;
  try {
    const response = await get(`/api/unauth/quan/getBarsByCategory?categoryCode=${categoryCode}`);
    categoryBars.value[categoryCode] = response || [];
  } catch (error) {
    console.error(`获取分类${categoryCode}的圈子列表失败:`, error);
    ElMessage.error('获取圈子列表失败，请稍后重试');
    categoryBars.value[categoryCode] = [];
  } finally {
    loadingBars.value[categoryCode] = false;
  }
};

const navigateToPost = (tieId) => {
  console.log("访问帖子详情页",tieId)
  // router.push(`/index/user/${userId}`)
  window.open(`/index/quan/QuanTieDetail/${tieId}`, '_blank');
};

// 获取今日热议
const fetchHotTopics = async () => {
  try {
    const response = await get('/api/unauth/quan/getTodayHotTie')
    
    if (response) {
      hotTopics.value = response.map((topic, index) => ({
        id: topic.id,
        rank: index + 1,
        title: topic.title,
        views: topic.views,
        comments: topic.comments
      }))
    }
  } catch (error) {
    console.error('获取今日热议失败:', error)
    ElMessage.error('获取热议话题失败，请稍后重试')
  }
}

// 刷新热议榜
const refreshHotTopics = () => {
  fetchHotTopics()
}


// 计算总页数
const friendBarTotalPages = computed(() => {
  return Math.ceil(friendBarTotal.value / friendBarPageSize.value)
})


// 换一换功能 - 翻页
const refreshFriendBars = () => {
  const nextPage = friendBarCurrentPage.value < friendBarTotalPages.value 
    ? friendBarCurrentPage.value + 1 
    : 1
  fetchFriendBars(nextPage)
}

// 关注/取消关注圈子
const toggleFollowBar = async (bar) => {
  console.log("关注/取消关注圈子", bar);
  try {
    // if (!userInfo?.data?.id) {
    //   // 未登录处理，可以跳转到登录页
    //   router.push('/login');
    //   return;
    // }


    if (bar.followed) {
      // 调用取消关注API
      await post('/api/auth/quan/unFollowBar', { 
        barId: bar.id, 
        userId: userInfo.data.id 
      });
      // 从已关注列表移除
      followedBars.value = followedBars.value.filter(b => b.id !== bar.id);
      bar.followerCount--;
    } else {
      // 调用关注API
      await post('/api/auth/quan/followBar', { 
        barId: bar.id, 
        userId: userInfo.data.id 
      });
      // 添加到已关注列表
      followedBars.value.unshift({
        id: bar.id,
        name: bar.name,
        avatar: bar.avatar,
        followerCount: bar.followerCount + 1,
        postCount: bar.postCount,
        unread: 0
      });
      bar.followerCount++;
    }
    
    // 更新关注状态
    bar.followed = !bar.followed;
    
    ElMessage.success(bar.followed ? '关注成功' : '已取消关注');
  } catch (error) {
    console.error('操作失败:', error);
    ElMessage.error(bar.followed ? '关注失败' : '取消关注失败');
  }
};

// 点击圈子标签
// const handleBarClick = (bar) => {
//   activeBar.value = bar.id;
//   // 这里可以根据需要跳转到圈子页面或过滤帖子
//   navigateToBar(bar.id);
// };

// 刷新分类
const refreshCategories = () => {
  fetchFirstLevelCategories();
  // 清空已加载的圈子数据
  categoryBars.value = {};
};

// 获取分类数据
const fetchCategories = async () => {
  try {
    const response = await get('/api/unauth/common/category');
    console.log("分类数据:", response); // 调试用
    
    if (response && response.length > 0) {
      categoryOptions.value = response.map(item => ({
        code: item.code,
        desc: item.desc,
        subs: item.subs || [] // 确保 subs 有默认值
      }));
      console.log("处理后的分类选项:", categoryOptions.value); // 调试用
    } else {
      ElMessage.warning('暂无分类数据');
      categoryOptions.value = [];
    }
  } catch (error) {
    console.error('获取分类数据失败:', error);
    ElMessage.error('获取分类数据失败，请稍后重试');
    categoryOptions.value = [];
  }
}

// 显示创建对话框
const showCreateDialog = async () => {
  createDialogVisible.value = true;
  if (createFormRef.value) {
    createFormRef.value.resetFields();
  }
  
  // 确保分类数据已加载
  if (categoryOptions.value.length === 0) {
    try {
      await fetchCategories();
    } catch (error) {
      console.error('加载分类数据失败:', error);
      ElMessage.error('加载分类数据失败');
    }
  }
};

const beforeAvatarUpload = (file) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 10

  if (!isJPG) {
    ElMessage.error('头像图片只能是 JPG/PNG 格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('头像图片大小不能超过 10MB!')
    return false
  }
  return true
}

// 提交创建表单
const submitCreateForm = () => {
  createFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    // 检查是否已上传图片
    console.log("createForm.value.avatar:", createForm.value)
    if (!createForm.value.avatar) {
      ElMessage.error('请上传圈图标')
      return
    }
    
    submitting.value = true
    
    try {
      const response = await post('/api/auth/quan/createBar', {
        name: createForm.value.name,
        firstCategory: createForm.value.firstCategory,
        secondCategory: createForm.value.secondCategory,
        avatar: createForm.value.avatar,
        description: createForm.value.description
      })
      
      ElMessage.success('圈创建成功!')
      createDialogVisible.value = false
      refreshHotBars()
    } catch (error) {
      console.error('创建圈失败:', error)
    } finally {
      submitting.value = false
    }
  })
}

const handleFirstCategoryChange = (value) => {
  createForm.value.secondCategory = null
  if (value) {
    const selectedCategory = categoryOptions.value.find(item => item.code === value)
    secondCategoryOptions.value = selectedCategory?.subs || []
  } else {
    secondCategoryOptions.value = []
  }
}

// 头像上传成功处理
const handleAvatarSuccess = (response) => {
  createForm.value.avatar = response.data
  ElMessage.success('头像上传成功')
}

// 封面图片上传成功处理 (保持不变)
const handleCoverSuccess = (response) => {
  form.imageUrl = response.data
  ElMessage.success('上传成功')
}

// 关闭对话框前的确认
const handleClose = (done) => {
  if (createFormRef.value && createFormRef.value.isClean?.()) {
    done()
    return
  }
  
  ElMessageBox.confirm('确定要放弃创建吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    done()
  }).catch(() => {})
}

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
  fetchPosts()
}


const handleTabChange = () => {
  currentPage.value = 1
  fetchPosts()
}




const toggleLike = async (post) => {
  console.log("post:", post)
   const targetState = !post.liked;
    const result = await get(`/api/auth/quan/favoriteTie?tieId=${post.id}&favorited=${targetState}`);
    if(result) {
      ElMessage.success('操作成功')
      post.liked = !post.liked
  post.likes = post.liked ? (parseInt(post.likes) + 1 + '') : (parseInt(post.likes) - 1 + '')
    }
    
  
}


const refreshHotBars = () => {
  // 模拟刷新热门
  hotBars.value = [...hotBars.value].sort(() => Math.random() - 0.5)
  hotBars.value.forEach((bar, index) => {
    bar.rank = index + 1
  })
}



const navigateToBar = (id) => {
  router.push({ name: 'bar', params: { id } })
}

const getRankClass = (rank) => {
  if (rank <= 3) return `rank-${rank}`
  return ''
}

// 初始化
onMounted(() => {
    fetchFirstLevelCategories()
  fetchFriendBars()
  fetchPosts()
    fetchHotTopics()
fetchFollowedBars();
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
.pagination-info {
  font-size: 12px;
  color: #909399;
  text-align: center;
  margin-top: 10px;
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

/* 添加分页样式 */
.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: center;
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

.post-avatar {
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
  
  .post-avatar {
    flex-wrap: wrap;
  }
  
  .post-image {
    width: calc(50% - 4px);
    height: 100px;
  }
}

.avatar-uploader {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 120px;
  height: 120px;
  transition: var(--el-transition-duration-fast);
}

.avatar-uploader:hover {
  border-color: var(--el-color-primary);
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 120px;
  height: 120px;
  text-align: center;
  line-height: 120px;
}

.avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.upload-tip {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-top: 8px;
}

.create-bar-btn {
  margin-bottom: 15px;
}

.avatar-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 120px;
  height: 120px;
}

.avatar-uploader:hover {
  border-color: #409eff;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 120px;
  height: 120px;
  line-height: 120px;
  text-align: center;
}

.avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.upload-tip {
  font-size: 12px;
  color: #8c939d;
  margin-top: 8px;
}

.category-selectors {
  display: flex;
  justify-content: space-between;
  width: 100%;
}

.group-tags {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 8px 0;
}

.bar-item {
  display: flex;
  align-items: center;
  padding: 8px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
  background-color: #f9f9f9;
}

.bar-item:hover {
  background-color: #f0f7ff;
}

.bar-item.active {
  background-color: #e6f1ff;
}

.bar-avatar {
  flex-shrink: 0;
  margin-right: 12px;
}

.bar-info {
  flex: 1;
  min-width: 0;
}

.bar-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.bar-stats {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #909399;
}

.bar-stats .el-icon {
  margin-right: 4px;
  font-size: 12px;
}

.loading-bars {
  display: flex;
  justify-content: center;
  padding: 10px;
}
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.hot-topics {
  animation: fadeIn 0.5s ease-in-out;
}

.followed-bar {
  display: flex;
  align-items: center;
  padding: 8px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
  gap: 10px;
}

.followed-bar:hover {
  background-color: #f5f7fa;
}

.bar-info {
  flex: 1;
  min-width: 0;
}

.bar-name {
  display: block;
  font-size: 14px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.bar-stats {
  display: flex;
  gap: 10px;
  font-size: 12px;
  color: #909399;
}

.bar-stats .el-icon {
  margin-right: 2px;
  font-size: 12px;
}

.unread-count {
  margin-left: auto;
}
</style>