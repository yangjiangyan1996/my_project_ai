<template>
  <div class="progress-list-container">
    <el-page-header @click="goBack" title="返回个人中心">
      <template #content>
        <span class="page-title">我的申请进度</span>
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
            class="progress-item"
            v-for="(item, index) in list"
            :key="index"
            @mouseenter="hoveredItem = index"
            @mouseleave="hoveredItem = null"
            @click="handleItemClick(item.projectId, $event)"
          >
            <div class="progress-header">
              <div class="project-name">{{ item.projectName }}</div>
              <div class="apply-time">{{ formatTime(item.applyTime) }}</div>
            </div>

            <div class="progress-content">
              <div class="status-info">
                <span class="label">当前状态：</span>
                <el-tag :type="getStatusType(item.status)" size="small">
                  {{ getStatusText(item.status) }}
                </el-tag>
              </div>
            </div>

            <div class="progress-actions" v-if="item.status === 0">
              <el-button 
                type="danger" 
                size="small" 
                @click.stop="handleCancel(item.projectId, index)"
                :loading="cancelingId === item.id"
              >
                撤销申请
              </el-button>
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
const cancelingId = ref(null)

// 处理项目点击
const handleItemClick = (projectId, event) => {
  // 检查点击的是否是按钮或按钮的子元素
  const isButtonClick = event.target.closest('.el-button') !== null
  if (!isButtonClick) {
    goToDetail(projectId)
  }
}

const goToDetail = (projectId) => {
  router.push({ name: 'project-detail', params: { id: projectId } })
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
    const res = await post('/api/auth/project/myApplicationList', {
      page: page.value,
      size: size.value
    })

    if (page.value === 1) {
      list.value = res.records
    } else {
      list.value.push(...res.records)
    }
    
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

const handleCancel = async (id, index) => {
  try {
    await ElMessageBox.confirm('确定要撤销此申请吗？撤销后将无法恢复', '提示', {
      confirmButtonText: '确定撤销',
      cancelButtonText: '取消',
      type: 'warning',
      confirmButtonClass: 'el-button--danger'
    })

    cancelingId.value = id
    const res = await post('/api/auth/project/cancelApply', { projectId:id })
    if (res) {
      ElMessage.success('撤销成功')
      list.value[index].status = 3
      list.value[index].statusText = '已撤销'
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('撤销失败: ' + (error.message || '未知错误'))
    }
  } finally {
    cancelingId.value = null
  }
}

const goBack = () => {
  router.push('/index/my')
}

// 初始化加载
onMounted(() => {
  page.value = 1
  fetchList()
})
</script>

<style scoped>
.progress-list-container {
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

.progress-item {
  padding: 16px;
  border-bottom: 1px solid #f0f2f7;
  position: relative;
  cursor: pointer;
  transition: background-color 0.2s;
}

.progress-item:hover {
  background-color: #f8f9fa;
}

.progress-item:last-child {
  border-bottom: none;
}

.progress-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.project-name {
  font-size: 16px;
  font-weight: 600;
  color: #1f2d3d;
}

.apply-time {
  font-size: 12px;
  color: #8590a6;
}

.progress-content {
  margin-bottom: 12px;
}

.status-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.label {
  color: #606266;
  font-weight: 500;
}

.progress-actions {
  margin-top: 12px;
  text-align: right;
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

/* 按钮样式微调 */
.el-button--danger {
  background-color: #f56c6c;
  border-color: #f56c6c;
}

.el-button--danger:hover {
  background-color: #e05c5c;
  border-color: #e05c5c;
}
</style>