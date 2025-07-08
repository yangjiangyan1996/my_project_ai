<template>
  <div class="apply-list-container">
    <el-page-header @click="goBack" title="返回个人中心" class="page-header">
      <template #content>
        <span class="page-title">我的审核列表</span>
        <div class="page-subtitle">查看和管理团队成员申请</div>
      </template>
    </el-page-header>

    <div class="list-container">
      <el-card class="list-card" shadow="hover">
        <div
          class="infinite-list"
          v-infinite-scroll="loadMore"
          :infinite-scroll-disabled="noMore"
        >
          <div
            class="apply-item"
            v-for="(item, index) in list"
            :key="index"
            @mouseenter="hoveredItem = index"
            @mouseleave="hoveredItem = null"
          >
            <div class="apply-header">
              <el-avatar 
                :src="item.avatar" 
                size="medium"
                :style="{
                  backgroundColor: item.avatar ? 'transparent' : '#409EFF',
                  color: 'white',
                  fontSize: '18px'
                }"
              >
                {{ item.username?.charAt(0) || '用' }}
              </el-avatar>
              <div class="user-info">
                <div class="username">{{ item.username || '用户名' }}</div>
                <div class="apply-time">{{ formatTime(item.applyTime) }}</div>
              </div>
            </div>

            <div class="apply-content">
              <div class="project-info">
                <span class="label">申请项目：</span>
                <span class="value strong">{{ item.projectName || '未命名项目' }}</span>
              </div>
              <div class="project-desc">
                <span class="label">项目简介：</span>
                <span class="value">{{ item.description || '暂无描述' }}</span>
              </div>
              <div class="message">
                <span class="label">申请留言：</span>
                <span class="value">{{ item.message || '无留言' }}</span>
              </div>
              <div class="applyUser">
                <span class="label">申请人：</span>
                <span class="value strong">{{ item.userName || '匿名用户' }}</span>
              </div>
            </div>

            <div
              class="hover-info"
              v-if="hoveredItem === index"
            >
              <p><b>身份：</b>{{ item.audience || '未填写' }}</p>
              <p><b>可投入时间：</b>{{ item.timePerDay || '未填写' }}</p>
              <p><b>技能：</b>{{ item.skills || '未填写' }}</p>
              <p><b>资源：</b>{{ item.resources || '未填写' }}</p>
            </div>

            <div class="apply-actions" v-if="item.status === 0">
              <el-button 
                type="success" 
                size="small" 
                @click="handleApprove(item.id)"
                plain
                round
              >
                <el-icon><CircleCheck /></el-icon>
                <span>通过</span>
              </el-button>
              <el-button 
                type="danger" 
                size="small" 
                @click="handleReject(item.id)"
                plain
                round
              >
                <el-icon><CircleClose /></el-icon>
                <span>拒绝</span>
              </el-button>
            </div>

            <div class="apply-status">
              <el-tag 
                :type="getStatusType(item.status)" 
                size="small"
                :effect="item.status === 1 ? 'dark' : 'plain'"
              >
                {{ getStatusText(item.status) }}
              </el-tag>
            </div>
          </div>

          <div v-if="loading" class="loading-more">
            <el-icon class="is-loading"><Loading /></el-icon>
            加载中...
          </div>
          <div v-if="noMore" class="no-more">没有更多数据了</div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { post } from '@/net'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading, CircleCheck, CircleClose } from '@element-plus/icons-vue'

const router = useRouter()

const list = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const loading = ref(false)
const noMore = ref(false)
const hoveredItem = ref(null)

const formatTime = (timeString) => {
  if (!timeString) return ''
  return new Date(timeString).toLocaleString()
}

const getStatusText = (status) => {
  switch(status) {
    case 0: return '待审核'
    case 1: return '已通过'
    case 2: return '已拒绝'
    case 3: return '已撤销'
    default: return '未知状态'
  }
}

const getStatusType = (status) => {
  switch(status) {
    case 0: return 'warning'
    case 1: return 'success'
    case 2: return 'danger'
    case 3: return 'info'
    default: return ''
  }
}

const fetchList = async () => {
  try {
    loading.value = true
    const res = await post('/api/auth/project/myApplyList', {
      page: page.value,
      size: size.value
    })

    list.value.push(...res.records)
    total.value = res.total
    noMore.value = page.value * size.value >= res.total
  } finally {
    loading.value = false
  }
}

const loadMore = () => {
  if (!loading.value && !noMore.value) {
    page.value++
    fetchList()
  }
}

const handleApprove = async (id) => {
  try {
    await ElMessageBox.confirm('确定要通过此申请吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const res = await post('/api/auth/project/approveApply', { id })
    if (res) {
      ElMessage.success('操作成功')
      const item = list.value.find(item => item.id === id)
      if (item) item.status = 1
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const handleReject = async (id) => {
  try {
    await ElMessageBox.confirm('确定要拒绝此申请吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const res = await post('/api/auth/project/rejectApply', { id })
    if (res) {
      ElMessage.success('操作成功')
      const item = list.value.find(item => item.id === id)
      if (item) item.status = 2
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const goBack = () => {
  router.push('/index/my')
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.apply-list-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 24px;
  background-color: #fff;
  min-height: 100vh;
}

.page-header {
  padding-bottom: 20px;
  border-bottom: 1px solid #ebebeb;
  margin-bottom: 24px;
  cursor: pointer;
}

.page-header :deep(.el-page-header__content) {
  font-size: 0;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #1a1a1a;
  display: block;
}

.page-subtitle {
  font-size: 14px;
  color: #606266;
  margin-top: 8px;
  display: block;
}

.list-container {
  margin-top: 20px;
}

.list-card {
  border-radius: 8px;
  border: none;
  background-color: #f6f6f6;
}

.apply-item {
  padding: 20px;
  border-bottom: 1px solid #ebebeb;
  position: relative;
  background-color: #fff;
  border-radius: 6px;
  margin-bottom: 16px;
  transition: all 0.3s ease;
}

.apply-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.apply-header {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.user-info {
  margin-left: 12px;
}

.username {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.apply-time {
  font-size: 13px;
  color: #8590a6;
  margin-top: 4px;
}

.apply-content {
  margin-left: 52px;
}

.project-info, 
.project-desc, 
.message, 
.applyUser {
  margin-bottom: 10px;
  font-size: 14px;
  display: flex;
}

.label {
  color: #606266;
  font-weight: 500;
  min-width: 80px;
}

.value {
  color: #303133;
  flex: 1;
}

.strong {
  color: #1a1a1a;
  font-weight: 600;
}

.apply-actions {
  margin-top: 16px;
  margin-left: 52px;
  display: flex;
  gap: 12px;
}

.apply-status {
  position: absolute;
  top: 20px;
  right: 20px;
}

.hover-info {
  background-color: #f8f9fa;
  border-radius: 6px;
  padding: 12px;
  margin-top: 12px;
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
  border-left: 3px solid #409EFF;
}

.hover-info p {
  margin-bottom: 6px;
}

.hover-info b {
  color: #303133;
  font-weight: 500;
  min-width: 80px;
  display: inline-block;
}

.loading-more, 
.no-more {
  text-align: center;
  padding: 20px 0;
  color: #8590a6;
  font-size: 14px;
}

.loading-more .el-icon {
  margin-right: 8px;
  animation: rotating 2s linear infinite;
}

@keyframes rotating {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .apply-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .user-info {
    margin-left: 0;
    margin-top: 10px;
  }
  
  .apply-content {
    margin-left: 0;
  }
  
  .apply-actions {
    margin-left: 0;
    flex-wrap: wrap;
  }
  
  .apply-status {
    position: static;
    margin-top: 10px;
  }
  
  .label {
    min-width: 70px;
  }
  
  .hover-info {
    position: static;
    width: auto;
  }
}
</style>