<template>
  <div class="tieba-container">
    <!-- 顶部信息栏 -->
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
              <el-tag size="small" effect="plain">{{ barInfo.description || '暂无描述' }}</el-tag>
          </div>
        </div>
        <el-button 
            :type="isFollowed ? 'success' : 'primary'"
            :class="['follow-btn', { 'followed': isFollowed }]"
            size="small" 
            round
            @click="toggleFollow"
        >
            <el-icon>
            <component :is="isFollowed ? 'CircleCheckFilled' : 'Plus'" />
            </el-icon>
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
          <!-- 新增图片展示区域 -->
 
          <div class="post-images" v-if="post.avatar && post.avatar.length > 0">
          <div 
            class="post-image-item" 
            v-for="(image, index) in post.avatar.slice(0, 9)" 
            :key="index"
          >
            <img :src="image" alt="帖子图片" />
          </div>
          <div class="image-more" v-if="post.avatar.length > 9">
            +{{ post.avatar.length - 9 }}
          </div>
        </div>

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
            <!-- <div class="action-btn">
              <el-icon><Share /></el-icon>
            </div> -->
          </div>
        </div>
        
                
        <el-pagination
        class="pagination"
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :pager-count="5"
        layout="prev, pager, next"
        :total="total"
        background
        hide-on-single-page
        @current-change="fetchPosts"
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
            clearable
            @clear="handleSearch" 
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
            <!-- <el-button type="text" size="small">更多</el-button> -->
          </div>
          <div class="info-item">
            <el-icon><UserFilled /></el-icon>
            <span>{{ barInfo.name }}圈共{{ barInfo.followerCount }}人</span>
          </div>
          <div class="info-item">
            <el-icon><Medal /></el-icon>
            <span>{{ barInfo.firstCategoryName }}</span>
          </div>
          <div class="info-item">
            <el-icon><Medal /></el-icon>
            <span>{{ barInfo.secondCategoryName }}</span>
          </div>
          <!-- <div class="info-item">
            <el-icon><Folder /></el-icon>
            <span>游戏主播及平台</span>
          </div> -->
          <!-- <el-button type="primary" size="small" class="apply-btn" round>
            申请吧主
          </el-button> -->
        </div>

        <div class="sidebar-section">
          <div class="section-header">
            <h3>相关圈子</h3>
            <el-button type="text" size="small" @click="openFriendBarDialog">更多</el-button>
          </div>
          <div 
            class="friend-bar" 
            v-for="bar in friendBars" 
            :key="bar.id" 
            @click="navigateToBar(bar.id)"
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
                <span>{{ topic.views | formatNumber }}阅读</span>
                <span>·</span>
                <span>{{ topic.comments | formatNumber }}评论</span>
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
         <el-form-item label="内容" prop="content">
        <div style="border: 1px solid #dcdfe6; border-radius: 4px;">
          <Toolbar
            style="border-bottom: 1px solid #dcdfe6"
            :editor="editorRef"
            :defaultConfig="toolbarConfig"
            mode="default"
          />
          <Editor
            style="height: 400px; overflow-y: hidden;"
            v-model="postForm.content"
            :defaultConfig="editorConfig"
            mode="default"
            @onCreated="handleEditorCreated"
          />
        </div>
      </el-form-item>
       
     <el-form-item label="图片" prop="images">
        <el-upload
          class="cover-uploader"
          action="http://localhost:8080/api/unauth/common/upload"
          list-type="picture-card"
          :on-success="handleImageSuccess"
          :before-upload="beforeImageUpload"
          :on-remove="handleRemoveImage"
          :limit="9"
          :on-exceed="handleExceed"
          :file-list="postForm.images"
          multiple
        >
          <el-icon class="cover-uploader-icon"><Plus /></el-icon>
          <template #file="{ file }">
            <div class="image-preview">
              <img :src="file.url" class="cover-image" />
            </div>
          </template>
          <template #tip>
            <div class="el-upload__tip">最多上传9张图片，单张不超过2MB</div>
          </template>
        </el-upload>
      </el-form-item>

      </el-form>
      <template #footer>
        <el-button @click="showPostDialog = false">取消</el-button>
        <el-button type="primary" @click="submitPost">发布</el-button>
      </template>
    </el-dialog>
  </div>


  <!-- x相关圈子弹窗 -->
  <el-dialog 
    v-model="friendBarDialogVisible" 
    title="相关圈子" 
    width="60%"
    top="5vh"
  >
    <div class="friend-bar-dialog-content">
      <div 
        class="friend-bar-item" 
        v-for="bar in dialogFriendBars" 
        :key="bar.id"
        @click="navigateToBar(bar.id)"
      >
        <div class="friend-bar-avatar">
          <el-avatar :size="48" :src="bar.avatar" shape="square" />
        </div>
        <div class="friend-bar-info">
          <h4 class="friend-bar-name">{{ bar.name }}</h4>
          <div class="friend-bar-stats">
            <span class="stat-item">
              <el-icon><User /></el-icon>
              <span>{{ bar.followerCount | formatNumber }}</span>
            </span>
            <span class="stat-item">
              <el-icon><Document /></el-icon>
              <span>{{ bar.postCount | formatNumber }}</span>
            </span>
          </div>
          <!-- <div class="friend-bar-desc">
            {{ bar.description || '暂无描述' }}
          </div>
          <div class="friend-bar-category">
            <el-tag size="small" effect="plain">{{ bar.firstCategoryName }}</el-tag>
            <el-tag size="small" effect="plain" v-if="bar.secondCategoryName">
              {{ bar.secondCategoryName }}
            </el-tag>
          </div> -->
        </div>
      </div>
    </div>
    
    <el-pagination
      small
      layout="prev, pager, next"
      :total="friendBarTotal"
      :page-size="dialogFriendBarSize"
      :current-page="dialogFriendBarPage"
      @current-change="handleFriendBarPageChange"
      class="dialog-pagination"
    />
  </el-dialog>
</template>

<script setup>
import { ref, computed,onMounted,shallowRef, onBeforeUnmount } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { get, post } from '@/net';
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import '@wangeditor/editor/dist/css/style.css'
import { 
  Search, User, Document, Plus, MoreFilled, View, 
  ChatDotRound, Star, Share, UserFilled, Medal, 
  Folder, EditPen ,CircleCheckFilled
} from '@element-plus/icons-vue';
import useUserInfo from '@/hooks/useUserInfo';
import { debounce } from 'lodash-es';

const route = useRoute();
const router = useRouter();
const { state: userInfo, loadUserInfo } = useUserInfo();

// 响应式数据
const activeNav = ref(0);
const hoverPost = ref(null);
const searchQuery = ref('');
const showPostDialog = ref(false);
const barId = route.params.id;
// 帖子数据和分页相关
const posts = ref([])
const currentPage = ref(1)
const pageSize = ref(5)
const total = ref(0)
// 提交状态
const submitting = ref(false)

// 今日热议数据
const hotTopics = ref([]);

// 相关圈子数据
const friendBars = ref([]);
const friendBarPage = ref(1);
const friendBarSize = ref(5);
const friendBarTotal = ref(0);

// 相关圈子弹窗相关
const friendBarDialogVisible = ref(false);
const dialogFriendBars = ref([]);
const dialogFriendBarPage = ref(1);
const dialogFriendBarSize = ref(10);

// 圈子信息
const barInfo = ref({
  id: 0,
  name: '',
  avatar: '',
  followerCount: 0,
  postCount: 0,
  description: '',
  firstCategoryName: '',
  secondCategoryName: '',
});
// 富文本编辑器相关
const editorRef = shallowRef()
const toolbarConfig = {}
const editorConfig = {
  placeholder: '请输入帖子内容...',
  MENU_CONF: {
    uploadImage: {
      server: 'http://localhost:8080/api/unauth/common/upload',
      fieldName: 'file',
      maxFileSize: 2 * 1024 * 1024, // 2M
      allowedFileTypes: ['image/*'],
      customInsert(res, insertFn) {
        if (res && res.data) {
          insertFn(res.data)
        }
      }
    }
  }
}
// 是否已关注
const isFollowed = ref(false);


onMounted(() => {
  fetchFriendBars();
    fetchHotTopics();

  if (!userInfo.data.id) {
    loadUserInfo().then(() => {
      console.log("当前用户", userInfo)
      fetchBarInfo().then(() => {
        fetchPosts()
      })
      checkFollowStatus()
    })
  } else {
    console.log("当前用户", userInfo)
    fetchBarInfo().then(() => {
      fetchPosts()
    })
    checkFollowStatus()
  }
})

onBeforeUnmount(() => {
  const editor = editorRef.value
  if (editor == null) return
  editor.destroy()
})

// 发帖表单数据
const postForm = ref({
  title: '',
  content: '',
  images: [] // 改为数组形式存储多图
})


// 获取今日热议
const fetchHotTopics = async () => {
  try {
    const response = await get('/api/unauth/quan/getTodayHotTie');
    
    if (response) {
      hotTopics.value = response.map((topic, index) => ({
        id: topic.id,
        rank: index + 1,
        title: topic.title,
        views: topic.views,
        comments: topic.comments
      }));
    }
  } catch (error) {
    console.error('获取今日热议失败:', error);
  }
};

// 获取相关圈子
const fetchFriendBars = async (page = 1, size = 5) => {
  try {
    const response = await post('/api/unauth/quan/getRelationBar', {
      page: page,
      size: size,
      barId: barId
    });
    
    if (response && response.records) {
      if (size === 5) {
        // 主列表数据
        friendBars.value = response.records.map(bar => ({
          id: bar.id,
          name: bar.name,
          avatar: bar.avatar,
          followerCount: bar.followerCount,
          postCount: bar.postCount,
          description: bar.description,
          firstCategoryName: bar.firstCategoryName,
          secondCategoryName: bar.secondCategoryName
        }));
        friendBarTotal.value = response.total || 0;
      } else {
        // 弹窗数据
        dialogFriendBars.value = response.records.map(bar => ({
          id: bar.id,
          name: bar.name,
          avatar: bar.avatar,
          followerCount: bar.followerCount,
          postCount: bar.postCount,
          description: bar.description,
          firstCategoryName: bar.firstCategoryName,
          secondCategoryName: bar.secondCategoryName
        }));
        friendBarTotal.value = response.total || 0;
      }
    }
  } catch (error) {
    console.error('获取相关圈子失败:', error);
  }
};

// 添加防抖的搜索处理函数
const debouncedSearch = debounce(() => {
  currentPage.value = 1;
  fetchPosts();
}, 500);

const handleSearch = () => {
  console.log('搜索:', searchQuery.value);
  currentPage.value = 1; // 重置为第一页
  fetchPosts(); // 重新获取帖子列表
  debouncedSearch();
};

// 数字格式化过滤器
const formatNumber = (value) => {
  if (!value) return '0';
  if (value >= 10000) {
    return (value / 10000).toFixed(1) + '万';
  }
  return value;
};

// 打开相关圈子弹窗
const openFriendBarDialog = () => {
  friendBarDialogVisible.value = true;
  fetchFriendBars(1, dialogFriendBarSize.value);
};

// 弹窗分页切换
const handleFriendBarPageChange = (page) => {
  dialogFriendBarPage.value = page;
  fetchFriendBars(page, dialogFriendBarSize.value);
};

// 图片上传成功处理
const handleImageSuccess = (response, file, fileList) => {
  if (response && response.data) {
    // 更新fileList中的url，确保预览能显示
    const updatedFileList = fileList.map(item => {
      if (item.uid === file.uid) {
        return {
          ...item,
          url: response.data
        }
      }
      return item
    })
    
    postForm.value.images = updatedFileList
    ElMessage.success('上传成功')
  }
}

// 图片上传前校验 (保持原有逻辑不变)
const beforeImageUpload = (file) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2
  const isSizeValid = new Promise((resolve) => {
    const img = new Image()
    img.src = URL.createObjectURL(file)
    img.onload = () => {
      const valid = img.width <= 2000 && img.height <= 2000
      if (!valid) {
        ElMessage.error('图片尺寸不能超过2000x2000像素')
      }
      resolve(valid)
    }
  })

  if (!isJPG) {
    ElMessage.error('图片只能是 JPG/PNG 格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  
  return isJPG && isLt2M && isSizeValid
}

// 新增方法：移除图片
const handleRemoveImage = (file, fileList) => {
  postForm.value.images = fileList
}

// 新增方法：超出限制提示
const handleExceed = () => {
  ElMessage.warning('最多只能上传9张图片')
}

const handleEditorCreated = (editor) => {
  editorRef.value = editor
}

const navigateToPost = (tieId) => {
  console.log("访问用户详情页",tieId)
  // router.push(`/index/user/${userId}`)
  window.open(`/index/quan/QuanTieDetail/${tieId}?barId=${barId}`, '_blank');
};


// 获取帖子列表
const fetchPosts = async (page = 1) => {
  try {
    const response = await post('/api/unauth/quan/getTiePageOfBar', {
      page: page,
      size: pageSize.value,
      barId: barId,
      keyword: searchQuery.value || undefined // 添加keyword参数
    })
    
    console.log("获取帖子列表", response.records)
    if (response && response.records) {
      posts.value = response.records.map(item => ({
        id: item.id,
        title: item.title,
        content: '', // 接口未返回内容，可以留空或后续添加
        userAvatar: item.avatar ,
        username: item.createdName,
        time: item.createdTime,
        views: item.views || 0, // 接口未返回，可以留空或后续添加
        comments: item.comments || 0, // 接口未返回，可以留空或后续添加
        likes: item.likes || 0, // 接口未返回，可以留空或后续添加
        avatar: item.avatar || [] // 帖子图片数组
      }))
      total.value = response.total || 0
      currentPage.value = page
    }
  } catch (error) {
    console.error('获取帖子列表失败:', error)
  }
}

// 检查用户是否已关注该圈子
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

// 获圈子信息
const fetchBarInfo = async () => {
  try {
    const response = await get(`/api/unauth/quan/getBarInfo?barId=${barId}`);
    
    if (response) {
      barInfo.value = {
        id: response.id,
        name: response.name,
        avatar: response.avatar,
        followerCount: response.followerCount,
        postCount: response.postCount,
        description: response.description,
        firstCategoryName: response.firstCategoryName,
        secondCategoryName: response.secondCategoryName,
      };
    }
  } catch (error) {
    console.error('获取圈子信息失败:', error);
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
        barId: barId, 
        userId: userInfo.data.id 
      });
      barInfo.value.followerCount--; // 更新关注数
    } else {
      // 调用关注API
      await post('/api/auth/quan/followBar', { 
        barId: barId, 
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


// 导航项
const navItems = ref([
//   '首页', '精华', '热门', '视频', '图片', '吧务', '活动'
]);


// 计算属性
const isMobile = computed(() => window.innerWidth < 768);

// 方法
const navigateToBar = (barId) => {
  console.log('跳转圈子:', barId);
   window.open(`/index/quan/QuanDetail/${barId}`, '_blank');
};



const showPostMenu = (postId) => {
  console.log('显示帖子菜单:', postId);
};


// 提交帖子方法 (只需修改images部分的处理)
const submitPost = async () => {
  if (!postForm.value.title.trim()) {
    ElMessage.error('请输入标题')
    return
  }
  
  if (!postForm.value.content.trim()) {
    ElMessage.error('请输入内容')
    return
  }

  submitting.value = true
  
  try {
    // 准备图片URL数组
    const imageUrls = postForm.value.images.map(img => img.url || img.response?.data)
    
    // 调用API
    await post('/api/auth/quan/createTie', {
      title: postForm.value.title,
      content: postForm.value.content,
      avatar: imageUrls[0] || '', // 保持原有avatar字段，取第一张图
      images: imageUrls,          // 新增images字段传递所有图片
      barId: barId
    })

    // 1. 关闭弹窗
    showPostDialog.value = false
    
    // 2. 重置表单
    resetPostForm()
    
    // 3. 提示成功
    ElMessage.success('发帖成功')
    
    // 4. 刷新列表数据
    await fetchPosts(currentPage.value) // 保持当前页码
    
    // 如果需要回到第一页，可以用下面这行代替
    // currentPage.value = 1
    // await fetchPosts()
    
  } catch (error) {
    console.error('发帖失败:', error)
    ElMessage.error(error.message || '发帖失败')
  } finally {
    submitting.value = false
  }
}

// 重置表单方法 (保持原有逻辑，只需修改images部分)
const resetPostForm = () => {
  postForm.value = {
    title: '',
    content: '',
    images: []
  }
  if (editorRef.value) {
    editorRef.value.clear()
  }
}

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

follow-btn {
  align-self: flex-start;
  padding: 8px 16px;
  font-weight: 500;
  transition: all 0.3s ease;
  border-width: 1px;
  border-style: solid;
  
  // 关注状态样式
  &:not(.followed) {
    background: linear-gradient(135deg, #409EFF 0%, #64B5FF 100%);
    border-color: #409EFF;
    box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
    
    &:hover {
      background: linear-gradient(135deg, #64B5FF 0%, #409EFF 100%);
      transform: translateY(-1px);
      box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
    }
    
    &:active {
      transform: translateY(0);
      box-shadow: 0 2px 6px rgba(64, 158, 255, 0.3);
    }
  }
  
  // 已关注状态样式
  &.followed {
    background: linear-gradient(135deg, #67C23A 0%, #85CE61 100%);
    border-color: #67C23A;
    box-shadow: 0 2px 8px rgba(103, 194, 58, 0.3);
    
    .el-icon {
      color: white;
    }
    
    &:hover {
      background: linear-gradient(135deg, #85CE61 0%, #67C23A 100%);
      transform: translateY(-1px);
      box-shadow: 0 4px 12px rgba(103, 194, 58, 0.4);
    }
    
    &:active {
      transform: translateY(0);
      box-shadow: 0 2px 6px rgba(103, 194, 58, 0.3);
    }
  }
  
  .el-icon {
    margin-right: 4px;
    font-size: 14px;
    transition: all 0.3s ease;
  }
}

/* 富文本编辑器样式调整 */
.w-e-toolbar {
  background-color: #f5f7fa !important;
  border-bottom: 1px solid #e4e7ed !important;
}

.w-e-text-container {
  background-color: #fff !important;
  border: none !important;
}
.friend-bar-dialog-content {
  max-height: 60vh;
  overflow-y: auto;
  padding: 10px;
}

.friend-bar-item {
  display: flex;
  gap: 16px;
  padding: 16px;
  margin-bottom: 12px;
  border-radius: 8px;
  background-color: #f9f9f9;
  cursor: pointer;
  transition: all 0.3s ease;
  
  &:hover {
    background-color: #f0f7ff;
    transform: translateX(5px);
  }
}

.friend-bar-avatar {
  flex-shrink: 0;
  
  :deep(.el-avatar) {
    border-radius: 6px;
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
  }
}

.friend-bar-info {
  flex: 1;
}

.friend-bar-name {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
}

.friend-bar-stats {
  display: flex;
  gap: 16px;
  margin-bottom: 8px;
  
  .stat-item {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    font-size: 13px;
    color: #666;
    
    .el-icon {
      font-size: 14px;
    }
  }
}

.friend-bar-desc {
  font-size: 13px;
  color: #666;
  margin-bottom: 8px;
  line-height: 1.5;
}

.friend-bar-category {
  display: flex;
  gap: 8px;
  
  .el-tag {
    background-color: rgba(0, 0, 0, 0.05);
    border: none;
    color: #666;
  }
}

.dialog-pagination {
  margin-top: 16px;
  justify-content: center;
}
.cover-uploader {
  :deep(.el-upload) {
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: var(--el-transition-duration-fast);
    width: 100px;
    height: 100px;
    
    &:hover {
      border-color: var(--el-color-primary);
    }
  }
  
  :deep(.el-upload-list--picture-card .el-upload-list__item) {
    width: 100px;
    height: 100px;
    margin-right: 10px;
  }
}

.cover-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100px;
  height: 100px;
  line-height: 100px;
  text-align: center;
}

.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-preview {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.el-upload__tip {
  margin-top: 7px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
.post-images {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin: 8px 0;
  
  .post-image-item {
    width: 80px;
    height: 80px;
    overflow: hidden;
    border-radius: 4px;
    background-color: #f5f5f5;
    position: relative;
    
    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }
  
  .image-more {
    width: 40px;
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: rgba(0, 0, 0, 0.5);
    color: white;
    font-size: 12px;
    border-radius: 4px;
  }
}
  
 
</style>