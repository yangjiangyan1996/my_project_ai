<template>
  <div class="apply-list-container">
    <el-page-header @back="goBack" title="返回个人中心">
      <template #content>
        <span class="page-title">我的审核列表</span>
      </template>
    </el-page-header>

    <div class="list-container">
      <el-card class="list-card">
        <div class="infinite-list" v-infinite-scroll="loadMore" :infinite-scroll-disabled="noMore">
          <div class="apply-item" v-for="(item, index) in list" :key="index">
            <div class="apply-header">
              <el-avatar :src="item.avatar" size="small">{{ item.username?.charAt(0) }}</el-avatar>
              <div class="user-info">
                <div class="username">{{ item.username }}</div>
                <div class="apply-time">{{ formatTime(item.applyTime) }}</div>
              </div>
            </div>
            
            <div class="apply-content">
              <div class="project-info">
                <span class="label">申请项目：</span>
                <span class="value">{{ item.projectName }}</span>
              </div>
              <div class="message">
                <span class="label">申请留言：</span>
                <span class="value">{{ item.message || '无留言' }}</span>
              </div>
            </div>
            
            <div class="apply-actions" v-if="item.status === 0">
              <el-button type="success" size="small" @click="handleApprove(item.id)">通过</el-button>
              <el-button type="danger" size="small" @click="handleReject(item.id)">拒绝</el-button>
            </div>
            
            <div class="apply-status">
              <el-tag :type="getStatusType(item.status)" size="small">
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
import { Loading } from '@element-plus/icons-vue'

const router = useRouter()

const list = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const loading = ref(false)
const noMore = ref(false)

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
    const res = await post('/api/auth/project/getApplyList', {
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
      // 更新本地状态
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
      // 更新本地状态
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
  router.push('/my')
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.apply-list-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.page-title {
  font-size: 18px;
  font-weight: 500;
}

.list-container {
  margin-top: 20px;
}

.list-card {
  border-radius: 8px;
}

.apply-item {
  padding: 16px;
  border-bottom: 1px solid #f0f2f7;
}

.apply-item:last-child {
  border-bottom: none;
}

.apply-header {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.user-info {
  margin-left: 10px;
}

.username {
  font-size: 14px;
  font-weight: 500;
}

.apply-time {
  font-size: 12px;
  color: #8590a6;
}

.apply-content {
  margin-left: 42px; /* 头像宽度 + 边距 */
}

.project-info, .message {
  margin-bottom: 8px;
  font-size: 14px;
}

.label {
  color: #606266;
  font-weight: 500;
}

.value {
  color: #303133;
}

.apply-actions {
  margin-top: 12px;
  margin-left: 42px;
}

.apply-status {
  margin-top: 8px;
  margin-left: 42px;
}

.loading-more, .no-more {
  text-align: center;
  padding: 10px 0;
  color: #8590a6;
  font-size: 14px;
}

.loading-more .el-icon {
  margin-right: 5px;
  animation: rotating 2s linear infinite;
}

@keyframes rotating {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>