<template>
  <div class="apply-list-container">
    <el-page-header @click="goBack" title="返回个人中心">
      <template #content>
        <span class="page-title">管理员审核列表</span>
      </template>
    </el-page-header>

    <div class="list-container">
      <el-card class="list-card">
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
             @click="goToDetail(item)"
          >
            <div class="apply-header" >
              <el-avatar :src="item.avatar" size="small">
                {{ item.username?.charAt(0) }}
              </el-avatar>
              <div class="user-info">
                <div class="username">{{ item.username }}</div>
                <div class="apply-time">{{ formatTime(item.applyTime) }}</div>
              </div>
            </div>

            <div class="apply-content">
              <div class="project-info">
                <span class="label">申请项目：</span>
                <span class="value strong">{{ item.projectName }}</span>
              </div>
            </div>

            <div class="apply-actions" v-if="item.status === 0">
              <el-button type="success" size="small" @click.stop="handleApprove(item.id)">通过</el-button>
              <el-button type="danger" size="small" @click.stop="handleReject(item.id)">拒绝</el-button>
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
const hoveredItem = ref(null)

// 跳转到项目详情页
function goToDetail(item) {
  console.log("跳转项目详情", item)
  if (!item) {
    console.error("跳转失败：item参数为空")
    return
  }
  
  // 确保有可用的ID
  const projectId = item.projectId || item.id
  if (!projectId) {
    console.error("跳转失败：缺少项目ID")
    return
  }

  router.push({ 
    name: 'project-detail', 
    params: { id: projectId } 
  }).catch(err => {
    console.error("路由跳转失败:", err)
  })
}

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
    const res = await post('/api/auth/project/adminApproveList', {
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

    const res = await post('/api/auth/project/adminApprovePass', { projectId:id })
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
    const { value: reason } = await ElMessageBox.prompt(
      '请输入拒绝理由：',
      '拒绝申请',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPattern: /.+/,
        inputErrorMessage: '理由不能为空'
      }
    )

    const res = await post('/api/auth/project/adminApproveNo', { projectId:id, reason })
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
  position: relative;
  cursor: pointer;
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
  margin-left: 42px;
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

.strong {
  color: #1f2d3d;
  font-weight: 600;
}

.apply-actions {
  margin-top: 12px;
  margin-left: 42px;
}

.apply-status {
  margin-top: 8px;
  margin-left: 42px;
}

.hover-info {
  position: absolute;
  right: 20px;
  top: 16px;
  background: #fff;
  border: 1px solid #ebeef5;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 12px;
  border-radius: 8px;
  z-index: 100;
  width: 240px;
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
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