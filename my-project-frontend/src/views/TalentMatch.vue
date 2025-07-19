<template>
  <div class="match-container">
    <!-- 左侧项目列表 -->
    <div class="project-list-container">
      <div class="header">
        <h2>我的项目</h2>
        <el-button type="primary" @click="refreshProjects">刷新</el-button>
      </div>
      <div 
        class="project-list"
        v-infinite-scroll="loadMoreProjects"
        :infinite-scroll-disabled="loadingProjects || noMoreProjects"
        :infinite-scroll-distance="100"
      >
        <div 
          v-for="project in projects" 
          :key="project.id"
          class="project-card"
          :class="{ active: activeProjectId === project.id }"
          @click="selectProject(project)"
        >
          <div class="project-badge" v-if="project.status === 0">审核中</div>
          <div class="project-badge success" v-else-if="project.status === 1">已发布</div>
          <div class="project-badge danger" v-else>已拒绝</div>
          
          <h3>{{ project.name }}</h3>
          <p class="category">
            <span>{{ project.firstCategoryName }}</span>
            <span v-if="project.secondCategoryName"> / {{ project.secondCategoryName }}</span>
          </p>
          <p class="desc">{{ project.description }}</p>
          <div class="meta">
            <span><i class="el-icon-time"></i> {{ formatDate(project.createdAt) }}</span>
            <span><i class="el-icon-star-off"></i> {{ project.likeCount || 0 }}</span>
          </div>
        </div>
        
        <div v-if="loadingProjects" class="loading-more">
          <el-icon class="is-loading"><Loading /></el-icon>
          加载中...
        </div>
        <div v-if="noMoreProjects" class="no-more">
          没有更多项目了
        </div>
      </div>
    </div>

    <!-- 中间项目详情 -->
    <div class="project-detail-container" v-if="activeProject">
      <div class="project-header">
        <h2>{{ activeProject.name }}</h2>
        <el-tag :type="getStatusTagType(activeProject.status)">
          {{ getStatusText(activeProject.status) }}
        </el-tag>
      </div>
      
      <div class="project-meta">
        <div class="meta-item">
          <label>行业分类</label>
          <p>{{ activeProject.firstCategoryName }} / {{ activeProject.secondCategoryName }}</p>
        </div>
        <div class="meta-item">
          <label>创建时间</label>
          <p>{{ formatDate(activeProject.createdAt, 'YYYY-MM-DD HH:mm') }}</p>
        </div>
        <div class="meta-item">
          <label>项目状态</label>
          <p>{{ getStatusText(activeProject.status) }}</p>
        </div>
      </div>
      
      <div class="project-content">
        <h3>项目描述</h3>
        <p>{{ activeProject.description }}</p>
      </div>
      
      <div class="match-controls">
        <el-button 
          type="primary" 
          @click="fetchMatchedUsers"
          :loading="loadingUsers"
        >
          查找匹配用户
        </el-button>
        <!-- <el-button @click="resetMatch">重置筛选</el-button> -->
      </div>
    </div>

    <!-- 右侧匹配用户列表 -->
    <div class="user-list-container" v-if="activeProject">
      <div class="user-list-header">
        <h2>匹配用户 <span v-if="totalUsers">(共 {{ totalUsers }} 人)</span></h2>
        <el-pagination
          small
          layout="prev, pager, next"
          :page-size="pageSize"
          :total="totalUsers"
          @current-change="handlePageChange"
        />
      </div>
      
      <div class="user-list" v-loading="loadingUsers">
        <div 
          v-for="user in matchedUsers" 
          :key="user.id"
          class="user-card"
        >
          <div class="user-avatar">
            <el-avatar :size="60" :src="user.avatarUrl || defaultAvatar">
              {{ user.nickname?.charAt(0) || 'U' }}
            </el-avatar>
            <div class="match-rate">
              <el-progress 
                type="circle" 
                :percentage="calculateMatchRate(user)" 
                :width="60"
                :stroke-width="8"
                :color="getMatchRateColor(calculateMatchRate(user))"
              />
            </div>
          </div>
          
          <div class="user-info">
            <h3>{{ user.nickname || user.username }}</h3>
            <p class="meta">
              <span><i class="el-icon-user"></i> {{ getGenderText(user.sex) }}</span>
              <span><i class="el-icon-location-outline"></i> {{ user.province }}{{ user.city }}</span>
            </p>
            
            <div class="skills">
              <el-tag 
                v-for="skill in user.skillNames?.split(',')" 
                :key="skill"
                size="small"
                type="info"
              >
                {{ skill }}
              </el-tag>
            </div>
            
            <div class="user-actions">
              <el-button 
                size="small" 
                type="primary" 
                @click="showUserDetail(user)"
              >
                查看详情
              </el-button>
              <el-button 
                size="small" 
                type="success" 
                @click="inviteUser(user)"
                :disabled="user.statusOfUserInProject === 0"
              >
                {{ user.statusOfUserInProject === 0 ? '已邀请' : '邀请加入' }}
              </el-button>
            </div>
          </div>
        </div>
        
        <div class="empty-tip" v-if="!loadingUsers && matchedUsers.length === 0">
          <el-empty description="暂无匹配用户" />
        </div>
      </div>
    </div>

    <!-- 用户详情对话框 -->
        <el-dialog 
        v-model="userDetailVisible" 
        title="用户详情" 
        width="60%"
        top="5vh"
        >
        <div class="user-detail-container" v-if="currentUser">
            <div class="user-basic-info">
            <el-avatar :size="100" :src="currentUser.avatarUrl || defaultAvatar" />
            <div class="user-meta">
                <h2>{{ currentUser.nickname || currentUser.username }}</h2>
                <p class="meta-item">
                <span><i class="el-icon-user"></i> {{ getGenderText(currentUser.sex) }}</span>
                <span><i class="el-icon-location-outline"></i> {{ currentUser.province }}{{ currentUser.city }}{{ currentUser.county }}</span>
                </p>
                <p class="meta-item">
                <span><i class="el-icon-star"></i> 匹配度: {{ currentUser.matchScore }}%</span>
                <span><i class="el-icon-time"></i> 每日可投入时间: {{ currentUser.timePerDay }}</span>
                </p>
            </div>
            </div>

            <el-divider />

            <div class="detail-section">
            <h3>基本信息</h3>
            <div class="detail-grid">
                <div class="detail-item">
                <label>用户类型</label>
                <p>{{ currentUser.audienceName || '未知' }}</p>
                </div>
                <div class="detail-item">
                <label>资源</label>
                <p>{{ currentUser.resources || '无' }}</p>
                </div>
            </div>
            </div>

            <el-divider />

            <div class="detail-section">
            <h3>技能标签</h3>
            <div class="skills-container">
                <el-tag 
                v-for="skill in currentUser.skillNames?.split(',')" 
                :key="skill"
                type="info"
                size="medium"
                >
                {{ skill }}
                </el-tag>
            </div>
            </div>

            <el-divider />

            <div class="detail-section">
            <h3>项目参与状态</h3>
            <el-tag :type="currentUser.statusOfUserInProject === 0 ? 'success' : 'info'">
                {{ currentUser.statusOfUserInProject === 0 ? '已邀请' : '可邀请' }}
            </el-tag>
            </div>
        </div>
        </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { post } from '@/net'
import { Loading } from '@element-plus/icons-vue'
// import UserDetailPanel from '@/components/UserDetailPanel.vue'
import defaultAvatar from '@/assets/default-avatar.png'

// 数据状态
const projects = ref([])
const activeProjectId = ref(null)
const activeProject = ref(null)
const matchedUsers = ref([])
const totalUsers = ref(0)
const loadingUsers = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)

// 项目列表分页相关
const projectPage = ref(1)
const projectPageSize = ref(10)
const loadingProjects = ref(false)
const noMoreProjects = ref(false)

// 用户详情相关
const userDetailVisible = ref(false)
const currentUser = ref(null)

// 初始化加载项目列表
onMounted(() => {
  fetchMyProjects(true)
})

// 获取我的项目列表
const fetchMyProjects = async (reset = false) => {
  if (reset) {
    projectPage.value = 1
    noMoreProjects.value = false
  }
  
  try {
    loadingProjects.value = true
    const res = await post('/api/auth/my/myPublished', {
      page: projectPage.value,
      size: projectPageSize.value
    })
    
    console.log("项目列表", res)
    if (reset) {
      projects.value = res.records || []
    } else {
      projects.value = [...projects.value, ...(res.records || [])]
    }
    
    if (res.records?.length < projectPageSize.value) {
      noMoreProjects.value = true
    }
    
    if (projects.value.length > 0 && !activeProjectId.value) {
      selectProject(projects.value[0])
    }
  } catch (error) {
    ElMessage.error('获取项目列表失败')
  } finally {
    loadingProjects.value = false
  }
}

// 加载更多项目
const loadMoreProjects = async () => {
  if (loadingProjects.value || noMoreProjects.value) return

  loadingProjects.value = true
  projectPage.value += 1

  try {
    const res = await post('/api/auth/my/myPublished', {
      page: projectPage.value,
      size: projectPageSize.value
    })

    if (res.list?.length > 0) {
      projects.value.push(...res.list)
    }

    if (!res.list || res.list.length < projectPageSize.value) {
      noMoreProjects.value = true
    }
  } catch (e) {
    ElMessage.error('加载更多项目失败')
  } finally {
    loadingProjects.value = false
  }
}


// 选择项目
const selectProject = (project) => {
  activeProjectId.value = project.id
  activeProject.value = project
  matchedUsers.value = []
  totalUsers.value = 0
  currentPage.value = 1
}

// 获取匹配用户
const fetchMatchedUsers = async () => {
  if (!activeProjectId.value) return
  
  try {
    loadingUsers.value = true
    const res = await post('/api/unauth/project/matchUser', {
      projectId: activeProjectId.value,
      page: currentPage.value,
      size: pageSize.value
    })
    
    matchedUsers.value = res.records || []
    totalUsers.value = res.total || 0
  } catch (error) {
    ElMessage.error('获取匹配用户失败')
  } finally {
    loadingUsers.value = false
  }
}

// 计算匹配率 (示例逻辑，根据实际需求调整)
const calculateMatchRate = (user) => {
  // 这里可以根据项目需求和用户属性计算匹配率
  // 示例：随机生成70-95%的匹配率
console.log("user",user)
  return Math.floor(user.matchScore);
}

// 分页变化
const handlePageChange = (page) => {
  currentPage.value = page
  fetchMatchedUsers()
}

// 查看用户详情
const showUserDetail = (user) => {
  currentUser.value = user
  userDetailVisible.value = true
}

// 邀请用户
const inviteUser = async (user) => {
  try {
    await post('/api/auth/projectMember/addMemberByManager', {
      projectId: activeProjectId.value,
      role:0,
      userId: user.userId
    })
    ElMessage.success('邀请已发送')
    user.statusOfUserInProject = 0 // 标记为已邀请
  } catch (error) {
    console.log("邀请失败",error)
    // ElMessage.error('邀请失败')
  }
}

// 辅助函数
const formatDate = (dateStr, format = 'YYYY-MM-DD') => {
  // 实现日期格式化
  return dateStr // 实际项目中可以使用day.js等库
}

const getStatusText = (status) => {
  const statusMap = {
    0: '审核中',
    1: '已发布',
    2: '已拒绝'
  }
  return statusMap[status] || '未知状态'
}

const getStatusTagType = (status) => {
  const typeMap = {
    0: 'warning',
    1: 'success',
    2: 'danger'
  }
  return typeMap[status] || 'info'
}

const getGenderText = (sex) => {
  return sex === 1 ? '男' : sex === 2 ? '女' : '未知'
}

const getMatchRateColor = (rate) => {
  if (rate >= 90) return '#67C23A'
  if (rate >= 70) return '#409EFF'
  if (rate >= 50) return '#E6A23C'
  return '#F56C6C'
}

const refreshProjects = () => {
  fetchMyProjects(true)
  if (activeProjectId.value) {
    fetchMatchedUsers()
  }
}

const resetMatch = () => {
  currentPage.value = 1
  fetchMatchedUsers()
}
</script>

<style scoped>
.match-container {
  display: flex;
  height: calc(100vh - 60px);
  background-color: #f5f7fa;
  padding: 20px;
  gap: 20px;
}

/* 左侧项目列表 */
.project-list-container {
  width: 320px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  height: calc(100vh - 60px); /* 保证有滚动空间 */
  overflow: hidden;
  
}

.project-list-container .header {
  padding: 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #ebeef5;
}

.project-list {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
  height: 100%; /* 原来是 calc(100% - 60px)，但不一定有父高度，改成 100% */
  max-height: calc(100vh - 160px); /* 适配页面视口 */
}

.project-card {
    min-height: 120px;
  padding: 15px;
  margin-bottom: 10px;
  border-radius: 6px;
  border: 1px solid #ebeef5;
  cursor: pointer;
  position: relative;
  transition: all 0.3s;
}

.project-card:hover {
  border-color: #409eff;
  box-shadow: 0 2px 12px 0 rgba(64, 158, 255, 0.1);
}

.project-card.active {
  border-color: #409eff;
  background-color: #f0f7ff;
}

.project-badge {
  position: absolute;
  right: 10px;
  top: 10px;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 10px;
  background: #909399;
  color: white;
}

.project-badge.success {
  background: #67c23a;
}

.project-badge.danger {
  background: #f56c6c;
}

.project-card h3 {
  margin: 0 0 8px 0;
  font-size: 16px;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.project-card .category {
  margin: 0 0 8px 0;
  font-size: 12px;
  color: #909399;
}

.project-card .desc {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #606266;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.project-card .meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #c0c4cc;
}

.project-card .meta i {
  margin-right: 3px;
}

/* 中间项目详情 */
.project-detail-container {
  flex: 1;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  padding: 20px;
  overflow-y: auto;
}

.project-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.project-header h2 {
  margin: 0 15px 0 0;
}

.project-meta {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 15px;
  margin-bottom: 20px;
}

.meta-item label {
  display: block;
  font-size: 12px;
  color: #909399;
  margin-bottom: 5px;
}

.meta-item p {
  margin: 0;
  font-size: 14px;
  color: #606266;
}

.project-content {
  margin-bottom: 30px;
}

.project-content h3 {
  margin: 0 0 15px 0;
  font-size: 16px;
  color: #303133;
}

.project-content p {
  margin: 0;
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
}

.match-controls {
  margin-top: 30px;
  text-align: center;
}

/* 右侧用户列表 */
.user-list-container {
  width: 380px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
}

.user-list-header {
  padding: 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #ebeef5;
}

.user-list-header h2 {
  margin: 0;
  font-size: 16px;
}

.user-list {
  flex: 1;
  overflow-y: auto;
  padding: 15px;
}

.user-card {
  padding: 15px;
  margin-bottom: 15px;
  border-radius: 8px;
  border: 1px solid #ebeef5;
  display: flex;
  transition: all 0.3s;
}

.user-card:hover {
  border-color: #409eff;
  box-shadow: 0 2px 12px 0 rgba(64, 158, 255, 0.1);
}

.user-avatar {
  position: relative;
  margin-right: 15px;
}

.match-rate {
  position: absolute;
  bottom: -10px;
  right: -10px;
  background: white;
  border-radius: 50%;
  padding: 3px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.user-info {
  flex: 1;
}

.user-info h3 {
  margin: 0 0 8px 0;
  font-size: 16px;
  color: #303133;
}

.user-info .meta {
  margin: 0 0 10px 0;
  font-size: 12px;
  color: #909399;
  display: flex;
  gap: 10px;
}

.user-info .meta i {
  margin-right: 3px;
}

.skills {
  margin-bottom: 10px;
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
}

.user-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.empty-tip {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
}

/* 新增样式 */
.loading-more, .no-more {
  text-align: center;
  padding: 10px;
  color: #999;
  font-size: 14px;
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

/* 用户详情样式 */
.user-detail-container {
  padding: 0 20px;
}

.user-basic-info {
  display: flex;
  align-items: center;
  gap: 30px;
  margin-bottom: 20px;
}

.user-meta h2 {
  margin: 0 0 10px 0;
  font-size: 24px;
  color: #303133;
}

.meta-item {
  margin: 8px 0;
  color: #606266;
  display: flex;
  gap: 20px;
}

.meta-item i {
  margin-right: 5px;
  color: #909399;
}

.detail-section {
  margin: 20px 0;
}

.detail-section h3 {
  margin: 0 0 15px 0;
  font-size: 18px;
  color: #303133;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
}

.detail-item {
  margin-bottom: 15px;
}

.detail-item label {
  display: block;
  font-size: 14px;
  color: #909399;
  margin-bottom: 5px;
}

.detail-item p {
  margin: 0;
  font-size: 16px;
  color: #606266;
}

.skills-container {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
</style>