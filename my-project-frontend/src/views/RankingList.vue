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
        :key="project.id"
        class="project-card"
        :class="{ 'is-flipped': project.isFlipped }"
        @click="toggleFlip(project)"
      >
        <div class="card-face card-front">
          <!-- 项目封面 -->
          <div class="project-cover" :style="{ backgroundImage: `url(${project.cover || defaultCover})` }">
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
                {{ project.industry }}
              </span>
              <span class="time">
                <el-icon><Clock /></el-icon>
                {{ formatDate(project.createdAt) }}
              </span>
            </div>
            
            <p class="project-desc">{{ truncate(project.description, 60) }}</p>
            
            <!-- 技术栈标签云 -->
            <div class="tech-tags">
              <el-tag
                v-for="tech in project.techStack.slice(0, 4)"
                :key="tech"
                size="small"
                :type="getRandomTagType()"
                effect="plain"
              >
                {{ tech }}
              </el-tag>
              <el-tag v-if="project.techStack.length > 4" size="small" type="info">
                +{{ project.techStack.length - 4 }}
              </el-tag>
            </div>
          </div>
          
          <!-- 项目数据 -->
          <div class="project-stats">
            <div class="stat-item">
              <el-icon><View /></el-icon>
              <span>{{ project.views | formatNumber }}</span>
            </div>
            <div class="stat-item">
              <el-icon><Star /></el-icon>
              <span>{{ project.likes | formatNumber }}</span>
            </div>
            <div class="stat-item">
              <el-icon><Connection /></el-icon>
              <span>{{ project.applicants | formatNumber }}</span>
            </div>
          </div>
        </div>
        
        <!-- 卡片背面 - 详细信息 -->
        <div class="card-face card-back">
          <div class="back-content">
            <h3 class="project-title">{{ project.name }}</h3>
            
            <div class="detail-section">
              <h4><el-icon><Document /></el-icon> 项目描述</h4>
              <p class="project-desc">{{ project.description }}</p>
            </div>
            
            <div class="detail-section">
              <h4><el-icon><List /></el-icon> 技术要求</h4>
              <div class="tech-stack">
                <div 
                  v-for="tech in project.techStack" 
                  :key="tech"
                  class="tech-item"
                >
                  <span class="tech-name">{{ tech }}</span>
                  <el-progress 
                    :percentage="getRandomSkillLevel()" 
                    :stroke-width="8" 
                    :show-text="false"
                  />
                </div>
              </div>
            </div>
            
            <div class="detail-section">
              <h4><el-icon><Opportunity /></el-icon> 项目优势</h4>
              <ul class="advantages">
                <li v-for="(adv, idx) in project.advantages" :key="idx">
                  {{ adv }}
                </li>
              </ul>
            </div>
            
            <div class="project-actions">
              <el-button type="primary" size="small" @click.stop="applyProject(project)">
                <el-icon><Position /></el-icon>
                立即申请
              </el-button>
              <el-button size="small" @click.stop="shareProject(project)">
                <el-icon><Share /></el-icon>
                分享项目
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
                v-for="pos in currentProject.positions"
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
  Position, Share, Loading,
  OfficeBuilding, Clock
} from '@element-plus/icons-vue'
import defaultCover from '@/assets/default-project-cover.jpg'

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
    loading.value = true
    // 模拟API调用
    const mockProjects = generateMockProjects(pageSize.value)
    projects.value = [...projects.value, ...mockProjects]
    
    // 实际项目中替换为:
    // const res = await post('/api/side-jobs', {
    //   ...filter.value,
    //   page: page.value,
    //   size: pageSize.value
    // })
    // projects.value = [...projects.value, ...res.data]
    // noMore.value = res.data.length < pageSize.value
  } catch (error) {
    ElMessage.error('获取项目列表失败')
  } finally {
    loading.value = false
  }
}

// 应用筛选
const applyFilters = () => {
  page.value = 1
  projects.value = []
  noMore.value = false
  fetchProjects()
}

// 翻转卡片
const toggleFlip = (project) => {
  project.isFlipped = !project.isFlipped
}

// 申请项目
const applyProject = (project) => {
  currentProject.value = project
  applyForm.value = {
    position: project.positions[0] || '',
    introduction: '',
    portfolio: []
  }
  applyDialogVisible.value = true
}

// 提交申请
const submitApplication = () => {
  ElNotification.success({
    title: '申请成功',
    message: `已成功申请项目 ${currentProject.value.name}`,
    duration: 3000
  })
  applyDialogVisible.value = false
}

// 分享项目
const shareProject = (project) => {
  ElMessage.success(`已复制项目 ${project.name} 的分享链接`)
  // 实际项目中实现分享逻辑
}

// 辅助函数
const formatDate = (dateStr) => {
  // 实现日期格式化
  return new Date(dateStr).toLocaleDateString()
}

const truncate = (text, length) => {
  return text.length > length ? text.substring(0, length) + '...' : text
}

const getStatusText = (status) => {
  const statusMap = {
    0: '招募中',
    1: '进行中',
    2: '已结束'
  }
  return statusMap[status] || '未知状态'
}

const getStatusClass = (status) => {
  const classMap = {
    0: 'recruiting',
    1: 'ongoing',
    2: 'ended'
  }
  return classMap[status] || ''
}

const getRandomTagType = () => {
  const types = ['', 'success', 'info', 'warning', 'danger']
  return types[Math.floor(Math.random() * types.length)]
}

const getRandomSkillLevel = () => {
  return Math.floor(Math.random() * 40) + 60 // 60-100之间的随机数
}

const handleExceed = () => {
  ElMessage.warning('最多只能上传3个文件')
}

// 模拟数据生成
const generateMockProjects = (count) => {
  const mockProjects = []
  const statuses = [0, 1, 2]
  const mockIndustries = ['互联网/IT', '金融', '教育', '医疗健康', '制造业']
  
  for (let i = 0; i < count; i++) {
    const techCount = Math.floor(Math.random() * 6) + 3
    const techStack = []
    for (let j = 0; j < techCount; j++) {
      const randomTech = techStacks.value[Math.floor(Math.random() * techStacks.value.length)]
      if (!techStack.includes(randomTech)) {
        techStack.push(randomTech)
      }
    }
    
    const positionCount = Math.floor(Math.random() * 3) + 1
    const positions = []
    for (let k = 0; k < positionCount; k++) {
      positions.push(`开发工程师${k+1}`)
    }
    
    const advantageCount = Math.floor(Math.random() * 3) + 2
    const advantages = []
    for (let l = 0; l < advantageCount; l++) {
      advantages.push(`项目优势示例${l+1}`)
    }
    
    mockProjects.push({
      id: `project-${page.value}-${i}`,
      name: `示例项目 ${page.value}-${i}`,
      description: '这是一个示例项目描述，展示项目的主要内容和目标。该项目旨在解决某一特定领域的问题，需要具备相关技术栈的开发者参与。',
      industry: mockIndustries[Math.floor(Math.random() * mockIndustries.length)],
      techStack,
      positions,
      advantages,
      status: statuses[Math.floor(Math.random() * statuses.length)],
      views: Math.floor(Math.random() * 1000),
      likes: Math.floor(Math.random() * 500),
      applicants: Math.floor(Math.random() * 100),
      createdAt: new Date(Date.now() - Math.floor(Math.random() * 30 * 24 * 60 * 60 * 1000)).toISOString(),
      isFlipped: false
    })
  }
  
  return mockProjects
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
  transition: all 0.3s ease;
  cursor: pointer;
  position: relative;
  transform-style: preserve-3d;
  height: 380px;
}

.project-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
}

.card-face {
  position: absolute;
  width: 100%;
  height: 100%;
  backface-visibility: hidden;
  border-radius: 10px;
  overflow: hidden;
}

.card-front {
  display: flex;
  flex-direction: column;
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

.project-badge.recruiting {
  background: var(--el-color-success);
}

.project-badge.ongoing {
  background: var(--el-color-warning);
}

.project-badge.ended {
  background: var(--el-color-info);
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

/* 技术标签 */
.tech-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 10px;
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
}

.back-content .project-title {
  font-size: 18px;
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

.tech-stack {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.tech-item {
  display: flex;
  align-items: center;
}

.tech-name {
  width: 80px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.advantages {
  margin: 0;
  padding-left: 18px;
  font-size: 13px;
  color: var(--el-text-color-regular);
}

.advantages li {
  margin-bottom: 5px;
}

.project-actions {
  display: flex;
  justify-content: center;
  gap: 10px;
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid var(--el-border-color-light);
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
}
</style>