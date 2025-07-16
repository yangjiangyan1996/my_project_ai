<template>
  <div class="side-job-container">
    <!-- 顶部筛选栏 -->
    <div class="filter-bar">
      <div class="filter-group">
        <el-select v-model="filter.industry" multiple placeholder="行业" clearable>
          <el-option
            v-for="item in industries"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
        
        <el-select v-model="filter.techStack" multiple placeholder="技术栈" filterable clearable>
          <el-option
            v-for="item in techStacks"
            :key="item"
            :label="item"
            :value="item"
          />
        </el-select>
        
        <el-select v-model="filter.sortBy" placeholder="排序方式" clearable>
          <el-option label="最新发布" value="newest" />
          <el-option label="最热项目" value="hottest" />
          <el-option label="最佳匹配" value="bestMatch" />
        </el-select>
      </div>
      
      <el-button type="primary" @click="applyFilters" :loading="loading">
        <el-icon><Search /></el-icon>
        <span>筛选</span>
      </el-button>
    </div>
    
    <!-- 项目展示区 -->
    <div class="project-showcase">
      <!-- 3D翻转卡片 -->
      <div 
        v-for="project in projects" 
        :key="project.projectId"
        class="project-card"
        
        :class="{ 'is-flipped': project.isFlipped }"
       @mouseenter="handleMouseEnter(project)"
        @mouseleave="project.isFlipped = false"
      >
        <div class="card-face card-front">
          <!-- 项目封面 -->
          <div class="project-cover" :style="{ backgroundImage: `url(${project.imageUrl || defaultCover})` }">
            <div class="project-badge" :class="getStatusClass(project.status)">
              {{ getStatusText(project.status) }}
            </div>
          </div>
          
          <!-- 项目基本信息 -->
          <div class="project-info">
            <h3 class="project-title">{{ project.name }}</h3>
            <div class="project-meta">
              <span class="industry">
                <el-icon><OfficeBuilding /></el-icon>
                {{ project.firstCategoryName }}
              </span>
              <span class="time">
                <el-icon><Clock /></el-icon>
                {{ formatDate(project.createdAt) }}
              </span>
            </div>
            
            <p class="project-desc">{{ truncate(project.description, 60) }}</p>
            
            <!-- 项目评分 -->
            <div class="project-score">
              <el-rate 
                v-model="project.score" 
                disabled 
                show-score 
                text-color="#ff9900" 
                score-template="{value}" 
                :max="1"
              />
            </div>
          </div>
          
          <!-- 项目数据 -->
          <div class="project-stats">
            <div class="stat-item">
              <el-icon><Star /></el-icon>
              <span>{{ project.likeCount || 0 }}</span>
            </div>
            <div class="stat-item">
              <el-icon><ChatDotRound /></el-icon>
              <span>{{ project.commentCount || 0 }}</span>
            </div>
            <div class="stat-item">
              <el-icon><Collection /></el-icon>
              <span>{{ project.favoriteCount || 0 }}</span>
            </div>
          </div>
        </div>
        
        <!-- 卡片背面 - 详细信息 -->
        <div class="card-face card-back">
          <div class="back-content" v-if="project.detailLoading">
            <div class="loading-detail">
              <el-icon class="is-loading"><Loading /></el-icon>
              加载中...
            </div>
          </div>
          <div class="back-content" v-else-if="project.detail">
            <h3 class="project-title">{{ project.name }}</h3>
            <p class="creator">创建者: {{ project.detail.creatorName }}</p>
            
            <div class="detail-section">
              <h4><el-icon><Document /></el-icon> 项目描述</h4>
              <p class="project-desc">{{ project.description }}</p>
            </div>
            
            <div class="detail-section">
              <h4><el-icon><List /></el-icon> 项目详情</h4>
              <div class="detail-grid">
                <div class="detail-item">
                  <label>目标人群</label>
                  <p>{{ project.detail.targetAudience || '无' }}</p>
                </div>
                <div class="detail-item">
                  <label>每日投入</label>
                  <p>{{ project.detail.timePerDay || '无' }}</p>
                </div>
                <div class="detail-item">
                  <label>收入预估</label>
                  <p>{{ project.detail.incomeEstimate || '无' }}</p>
                </div>
                <div class="detail-item">
                  <label>风险提示</label>
                  <p>{{ project.detail.riskWarning || '无' }}</p>
                </div>
              </div>
            </div>
            
            <div class="detail-section">
              <h4><el-icon><Tools /></el-icon> 所需工具</h4>
              <p>{{ project.detail.tools || '无' }}</p>
            </div>
            
            <div class="detail-section">
              <h4><el-icon><Guide /></el-icon> 操作步骤</h4>
              <pre class="steps-content">{{ project.detail.steps }}</pre>
            </div>
            
            <div class="project-actions">
              <el-button 
                type="primary" 
                size="small" 
                @click.stop="applyProject(project)"
                :disabled="project.detail.applyStatus !== null"
              >
                <el-icon><Position /></el-icon>
                {{ project.detail.applyStatus !== null ? '已申请' : '立即申请' }}
              </el-button>
              <el-button 
                size="small" 
                @click.stop="toggleLike(project)"
                :type="project.detail.myLike ? 'danger' : ''"
              >
                <el-icon><Star /></el-icon>
                {{ project.detail.myLike ? '已点赞' : '点赞' }}
              </el-button>
              <el-button 
                size="small" 
                @click.stop="toggleFavorite(project)"
                :type="project.detail.myFavorite ? 'warning' : ''"
              >
                <el-icon><Collection /></el-icon>
                {{ project.detail.myFavorite ? '已收藏' : '收藏' }}
              </el-button>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 加载更多 -->
      <div v-if="loading" class="loading-more">
        <el-icon class="is-loading"><Loading /></el-icon>
        加载中...
      </div>
      <div v-if="noMore" class="no-more">
        没有更多项目了
      </div>
    </div>
    
    <!-- 项目申请对话框 -->
    <el-dialog 
      v-model="applyDialogVisible" 
      :title="`申请项目 - ${currentProject?.name}`"
      width="50%"
    >
      <div v-if="currentProject" class="apply-dialog">
        <el-form :model="applyForm" label-width="100px">
          <el-form-item label="申请职位">
            <el-select v-model="applyForm.position" placeholder="请选择申请职位">
              <el-option
                v-for="pos in positions"
                :key="pos"
                :label="pos"
                :value="pos"
              />
            </el-select>
          </el-form-item>
          
          <el-form-item label="个人简介">
            <el-input
              v-model="applyForm.introduction"
              type="textarea"
              :rows="4"
              placeholder="请简要介绍你的相关经验和技能"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
          
          <el-form-item label="作品集">
            <el-upload
              action="#"
              multiple
              :limit="3"
              :file-list="applyForm.portfolio"
              :on-exceed="handleExceed"
              :auto-upload="false"
            >
              <el-button type="primary">点击上传</el-button>
              <template #tip>
                <div class="el-upload__tip">
                  可上传作品集(不超过3个文件)
                </div>
              </template>
            </el-upload>
          </el-form-item>
        </el-form>
        
        <div class="dialog-footer">
          <el-button @click="applyDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitApplication">提交申请</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElNotification } from 'element-plus'
import { 
  Search, Star, View, Connection, 
  Document, List, Opportunity, 
  Position, Share, Loading, Collection,
  OfficeBuilding, Clock, ChatDotRound,
  Tools, Guide
} from '@element-plus/icons-vue'
import defaultCover from '@/assets/default-project-cover.jpg'
import { get, post } from '@/net'

// 筛选条件
const filter = ref({
  industry: [],
  techStack: [],
  sortBy: 'hottest'
})

// 行业选项
const industries = ref([
  { value: 'it', label: '互联网/IT' },
  { value: 'finance', label: '金融' },
  { value: 'education', label: '教育' },
  { value: 'health', label: '医疗健康' },
  { value: 'manufacture', label: '制造业' },
  { value: 'entertainment', label: '文化娱乐' }
])

// 技术栈选项
const techStacks = ref([
  'Java', 'Python', 'JavaScript', 'Go', 'C++',
  'Vue', 'React', 'Angular', 'Spring Boot',
  'Django', 'Flask', 'Node.js', 'MySQL',
  'MongoDB', 'Redis', 'Docker', 'Kubernetes',
  'AWS', 'Azure', 'GCP', 'TensorFlow', 'PyTorch'
])

// 职位选项
const positions = ref([
  '开发工程师', '产品经理', 'UI设计师', '测试工程师', '运维工程师'
])

// 项目数据
const projects = ref([])
const loading = ref(false)
const noMore = ref(false)
const page = ref(1)
const pageSize = ref(12)

// 申请对话框
const applyDialogVisible = ref(false)
const currentProject = ref(null)
const applyForm = ref({
  position: '',
  introduction: '',
  portfolio: []
})

// 初始化加载项目
onMounted(() => {
  fetchProjects()
})

// 获取项目数据
const fetchProjects = async () => {
  try {
    loading.value = true;
    const response = await post('/api/auth/project/showHotProjectList', {
      page: page.value,
      size: pageSize.value
    });
    
    if (response && response.records) {
      // 转换API返回的数据格式
      const formattedProjects = response.records.map(project => ({
        ...project,
        projectId: project.projectId, // 添加projectId字段保持一致性
        isFlipped: false,
        detail: null, // 存储详情数据
        detailLoading: false // 详情加载状态
      }));
      
      // 如果是第一页，直接替换数据；否则追加数据
      if (page.value === 1) {
        projects.value = formattedProjects;
      } else {
        projects.value = [...projects.value, ...formattedProjects];
      }
      
      // 判断是否还有更多数据
      noMore.value = page.value >= response.pages || 
                     response.records.length < pageSize.value;
    } else {
      ElMessage.warning('获取的项目列表数据格式不正确');
    }
  } catch (error) {
    ElMessage.error('获取项目列表失败');
    console.error('获取项目列表失败:', error);
  } finally {
    loading.value = false;
  }
};

// 应用筛选
const applyFilters = () => {
  page.value = 1
  projects.value = []
  noMore.value = false
  fetchProjects()
}

// 翻转卡片时获取详情
const toggleFlip = async (project) => {
   
  project.isFlipped = !project.isFlipped
  
  if (project.isFlipped && !project.detail) {
    try {
      project.detailLoading = true
      
       const detail = await get(`/api/auth/project/detail?projectId=${project.projectId}`);
       console.log("toggleFlip", detail)
      project.detail = {
        ...detail,
        // 处理可能的多选标签
        tags: detail.tags ? detail.tags.split(',') : []
      }
    } catch (error) {
      ElMessage.error('获取项目详情失败')
      console.error('获取项目详情失败:', error)
    } finally {
      project.detailLoading = false
    }
  }
}

// 修改鼠标悬停处理
const handleMouseEnter = async (project) => {
  project.isFlipped = true;
  
  // 如果还没有加载详情，则加载
  if (!project.detail) {
    try {
      project.detailLoading = true;
      const detail = await get(`/api/auth/project/detail?projectId=${project.projectId}`);
      project.detail = {
        ...detail,
        tags: detail.tags ? detail.tags.split(',') : []
      };
    } catch (error) {
      ElMessage.error('获取项目详情失败');
    } finally {
      project.detailLoading = false;
    }
  }
};


// 点赞项目
const toggleLike = async (project) => {
  try {
    const action = project.detail.myLike ? 'unlike' : 'like'
    await post(`/api/auth/project/${action}`, { projectId: project.projectId })
    
    // 更新状态
    project.detail.myLike = !project.detail.myLike
    project.likeCount += project.detail.myLike ? 1 : -1
    
    ElMessage.success(project.detail.myLike ? '点赞成功' : '已取消点赞')
  } catch (error) {
    ElMessage.error('操作失败')
    console.error('点赞操作失败:', error)
  }
}

// 收藏项目
const toggleFavorite = async (project) => {
  try {
    const action = project.detail.myFavorite ? 'unfavorite' : 'favorite'
    await post(`/api/auth/project/${action}`, { projectId: project.projectId })
    
    // 更新状态
    project.detail.myFavorite = !project.detail.myFavorite
    project.favoriteCount += project.detail.myFavorite ? 1 : -1
    
    ElMessage.success(project.detail.myFavorite ? '收藏成功' : '已取消收藏')
  } catch (error) {
    ElMessage.error('操作失败')
    console.error('收藏操作失败:', error)
  }
}

// 申请项目
const applyProject = (project) => {
  currentProject.value = project
  applyForm.value = {
    position: positions.value[0] || '',
    introduction: '',
    portfolio: []
  }
  applyDialogVisible.value = true
}

// 提交申请
const submitApplication = async () => {
  try {
    // 这里调用申请API
    await post('/api/auth/project/apply', {
      projectId: currentProject.value.projectId,
      position: applyForm.value.position,
      introduction: applyForm.value.introduction
      // 文件上传需要额外处理
    })
    
    // 更新申请状态
    currentProject.value.detail.applyStatus = 1
    
    ElNotification.success({
      title: '申请成功',
      message: `已成功申请项目 ${currentProject.value.name}`,
      duration: 3000
    })
    applyDialogVisible.value = false
  } catch (error) {
    ElMessage.error('申请失败')
    console.error('申请失败:', error)
  }
}

// 分享项目
const shareProject = (project) => {
  ElMessage.success(`已复制项目 ${project.name} 的分享链接`)
  // 实际项目中实现分享逻辑
}

// 辅助函数
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`
}

const truncate = (text, length) => {
  return text && text.length > length ? text.substring(0, length) + '...' : text
}

const getStatusText = (status) => {
  const statusMap = {
    'published': '已发布',
    'draft': '草稿',
    'closed': '已关闭'
  }
  return statusMap[status] || status || '未知状态'
}

const getStatusClass = (status) => {
  const classMap = {
    'published': 'recruiting',
    'draft': 'info',
    'closed': 'ended'
  }
  return classMap[status] || ''
}

const handleExceed = () => {
  ElMessage.warning('最多只能上传3个文件')
}
</script>

<style scoped>
.side-job-container {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

/* 筛选栏样式 */
.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 15px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.filter-group {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

/* 项目展示区 */
.project-showcase {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

/* 3D卡片样式 */
.project-card {
  background: white;
  border-radius: 10px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  /* transition: all 0.3s ease; */
  cursor: pointer;
  position: relative;
  transform-style: preserve-3d;
  height: 380px;
  transition: transform 0.6s ease;
  /* transform-style: preserve-3d; */
  perspective: 1000px;
}

.project-card:hover {
  /* transform: translateY(-5px); */
  /* box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12); */

   transform: translateY(-5px) scale(1.02);
  box-shadow: 0 12px 20px rgba(0, 0, 0, 0.15);
  z-index: 10;
}

.card-face {
  position: absolute;
  width: 100%;
  height: 100%;
  backface-visibility: hidden;
  border-radius: 10px;
  overflow: hidden;
  transition: all 0.6s ease;
}

.card-front {
  display: flex;
  flex-direction: column;
  transform: rotateY(0deg);
}

.card-back {
  transform: rotateY(180deg);
  background: white;
  overflow-y: auto;
}

.is-flipped {
  transform: rotateY(180deg);
}

/* 项目封面 */
.project-cover {
  height: 150px;
  background-size: cover;
  background-position: center;
  position: relative;
}

.project-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  color: white;
  background: var(--el-color-primary);
}


.is-flipped .card-front {
  transform: rotateY(-180deg);
}

.is-flipped .card-back {
  transform: rotateY(0deg);
}

.project-badge.recruiting {
  background: var(--el-color-success);
}

.project-badge.info {
  background: var(--el-color-info);
}

.project-badge.ended {
  background: var(--el-color-warning);
}

/* 项目信息 */
.project-info {
  padding: 15px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.project-title {
  margin: 0 0 10px 0;
  font-size: 16px;
  color: var(--el-text-color-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.project-meta {
  display: flex;
  gap: 15px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-bottom: 10px;
}

.project-meta .el-icon {
  margin-right: 3px;
}

.project-desc {
  margin: 0 0 12px 0;
  font-size: 13px;
  color: var(--el-text-color-regular);
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  flex: 1;
}

.project-score {
  margin: 10px 0;
}

.project-score :deep(.el-rate) {
  display: inline-flex;
  align-items: center;
}

.project-score :deep(.el-rate__item) {
  margin-right: 2px;
}

/* 项目数据 */
.project-stats {
  display: flex;
  justify-content: space-around;
  padding: 10px 15px;
  border-top: 1px solid var(--el-border-color-light);
  background: var(--el-fill-color-lighter);
}

.stat-item {
  display: flex;
  align-items: center;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.stat-item .el-icon {
  margin-right: 5px;
  font-size: 14px;
}

/* 卡片背面内容 */
.back-content {
  padding: 15px;
  height: 100%;
  box-sizing: border-box;
}

.loading-detail {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  color: var(--el-text-color-secondary);
}

.back-content .project-title {
  font-size: 18px;
  margin-bottom: 5px;
  text-align: center;
}

.creator {
  color: var(--el-text-color-secondary);
  font-size: 14px;
  margin-bottom: 15px;
  text-align: center;
}

.detail-section {
  margin-bottom: 15px;
}

.detail-section h4 {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: var(--el-text-color-primary);
  display: flex;
  align-items: center;
}

.detail-section h4 .el-icon {
  margin-right: 5px;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
  margin-bottom: 15px;
}

.detail-item {
  margin-bottom: 10px;
}

.detail-item label {
  display: block;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-bottom: 5px;
}

.detail-item p {
  margin: 0;
  font-size: 14px;
  color: var(--el-text-color-regular);
}

.steps-content {
  font-size: 14px;
  line-height: 1.6;
  color: var(--el-text-color-regular);
  white-space: pre-wrap;
  background: var(--el-fill-color-light);
  padding: 10px;
  border-radius: 4px;
}

.project-actions {
  display: flex;
  justify-content: center;
  gap: 10px;
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid var(--el-border-color-light);
  flex-wrap: wrap;
}

/* 加载更多 */
.loading-more, .no-more {
  grid-column: 1 / -1;
  text-align: center;
  padding: 20px;
  color: var(--el-text-color-secondary);
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

/* 申请对话框 */
.apply-dialog {
  padding: 0 10px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .filter-bar {
    flex-direction: column;
    align-items: stretch;
    gap: 10px;
  }
  
  .project-showcase {
    grid-template-columns: 1fr;
  }
  
  .project-card {
    height: auto;
    min-height: 350px;
  }
  
  .detail-grid {
    grid-template-columns: 1fr;
  }
}
</style>