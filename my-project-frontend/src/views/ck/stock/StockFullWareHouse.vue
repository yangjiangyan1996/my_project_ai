<!--全仓库存管理 -->
<template>
  <div class="stock-take-full-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">全库盘点任务</span>
          <div>
            <el-button v-if="!isView" type="primary" @click="handleSave">保存</el-button>
            <el-button v-if="!isView && form.id" type="success" @click="handleSubmit">提交审批</el-button>
            <el-button @click="goBack">返回</el-button>
          </div>
        </div>
      </template>

      <!-- 基本信息 -->
      <el-form :model="form" :disabled="isView" label-width="120px">
        <el-form-item label="盘点单号">
          <el-input v-model="form.stockTakeNo" disabled />
        </el-form-item>

        <el-form-item label="盘点名称">
          <el-input v-model="form.takeName" placeholder="请输入盘点任务名称" />
        </el-form-item>

        <el-form-item label="仓库" required>
          <el-select v-model="form.warehouseId" placeholder="请选择仓库" style="width: 100%">
            <el-option v-for="w in warehouseList" :key="w.id" :label="w.name" :value="w.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="盘点方式" required>
          <el-radio-group v-model="form.takeType">
            <el-radio :label="1">动态盘点</el-radio>
            <el-radio :label="2">静态盘点</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入备注信息" :rows="3" />
        </el-form-item>
      </el-form>

      <!-- 快照展示 -->
      <el-divider v-if="snapshot">库存快照</el-divider>

      <el-descriptions v-if="snapshot" :column="3" border class="snapshot-info">
        <el-descriptions-item label="快照时间">{{ formatTime(snapshot.snapshotTime) }}</el-descriptions-item>
        <el-descriptions-item label="SKU 数量">{{ snapshot.productCount }}</el-descriptions-item>
        <el-descriptions-item label="库存总量">{{ snapshot.totalQuantity }}</el-descriptions-item>
      </el-descriptions>

      <!-- 操作日志 -->
      <el-divider v-if="logs.length > 0">操作日志</el-divider>
      
      <el-timeline v-if="logs.length > 0" class="log-timeline">
        <el-timeline-item
          v-for="(log, index) in logs"
          :key="index"
          :timestamp="formatTime(log.createdAt)"
          placement="top"
        >
          <el-card>
            <div class="log-content">
              <div class="log-action">{{ log.actionDesc || log.actionType }}</div>
              <div class="log-user">操作人：{{ log.createdByName || '系统' }}</div>
            </div>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { get, post } from '@/net'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()

const isView = computed(() => route.query.view === 'true')

const form = reactive({
  id: null,
  stockTakeNo: '',
  takeName: '',
  warehouseId: null,
  takeType: 1,
  takeScope: 1, // 固定为全库盘点
  remark: ''
})

const strategy = reactive({
  countMode: 1,
  needSecondCount: true,
  secondThreshold: 5,
  autoAdjust: true
})

const warehouseList = ref([])
const snapshot = ref(null)
const logs = ref([])

const formatTime = (time) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

const loadWarehouses = async () => {
  try {
    const res = await get('/api/auth/warehouse/list')
    warehouseList.value = res || []
  } catch (error) {
    console.error('加载仓库列表失败:', error)
    ElMessage.error('加载仓库列表失败')
  }
}

const loadDetail = async (id) => {
  try {
    const res = await get(`/api/auth/stock-take/detail?id=${id}`)
    Object.assign(form, res)
    if (res.strategyJson) {
      try {
        Object.assign(strategy, JSON.parse(res.strategyJson))
      } catch (e) {
        console.error('解析策略配置失败:', e)
      }
    }
    snapshot.value = res.snapshot || null
    loadLogs(id)
  } catch (error) {
    console.error('加载盘点单详情失败:', error)
    ElMessage.error('加载盘点单详情失败')
  }
}

const loadLogs = async (stockTakeId) => {
  try {
    const res = await get(`/api/auth/stock-take/logs?stockTakeId=${stockTakeId}`)
    logs.value = res || []
  } catch (error) {
    console.error('加载操作日志失败:', error)
  }
}

const validateForm = () => {
  if (!form.takeName?.trim()) {
    ElMessage.warning('请输入盘点名称')
    return false
  }
  if (!form.warehouseId) {
    ElMessage.warning('请选择仓库')
    return false
  }
  return true
}

const handleSave = async () => {
  if (!validateForm()) return
  
  try {
    const saveData = {
      ...form,
      strategyJson: JSON.stringify(strategy)
    }
    
    if (form.id) {
      // 更新
      await post('/api/auth/stock-take/update', saveData)
      ElMessage.success('更新成功')
    } else {
      // 创建
      const res = await post('/api/auth/stock/createStockTake', {
        warehouseId: form.warehouseId,
        takeType: form.takeType,
        takeScope: form.takeScope,
        remark: form.remark,
        takeName: form.takeName
      })
      form.id = res
      ElMessage.success('创建成功')
      // 刷新详情
      await loadDetail(res)
    }
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error(error.message || '保存失败')
  }
}

const handleSubmit = async () => {
  if (!form.id) {
    ElMessage.warning('请先保存盘点单')
    return
  }
  
  try {
    await ElMessageBox.confirm('确定要提交审批吗？提交后将进入审批流程', '确认提交', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    
    await post('/api/auth/stock-take/submit', { id: form.id })
    ElMessage.success('提交成功')
    
    // 刷新状态
    await loadDetail(form.id)
  } catch (error) {
    if (error !== 'cancel') {
      console.error('提交失败:', error)
      ElMessage.error(error.message || '提交失败')
    }
  }
}

const goBack = () => {
  router.back()
}

onMounted(async () => {
  await loadWarehouses()
  
  const id = route.params.id || route.query.id
  if (id) {
    await loadDetail(id)
  } else {
    // 新建时生成初始盘点单号
    form.stockTakeNo = `PD${dayjs().format('YYYYMMDDHHmmss')}`
  }
})
</script>

<style scoped>
.stock-take-full-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.manage-card {
  border-radius: 8px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.form-section {
  margin-bottom: 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.section-actions {
  display: flex;
  gap: 8px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-left: 8px;
}

.range-filter {
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 16px;
}

.range-table {
  margin-top: 16px;
}

.product-table {
  width: 100%;
  margin-bottom: 16px;
}

.product-info {
  line-height: 1.4;
}

.product-name {
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.product-sku {
  font-size: 12px;
  color: #909399;
  margin-bottom: 2px;
}

.product-category {
  font-size: 12px;
  color: #909399;
}

.selected-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background-color: #f8f9fa;
  border-radius: 4px;
  border: 1px solid #ebeef5;
}

.selected-summary {
  display: flex;
  gap: 24px;
}

.selected-summary span {
  font-size: 14px;
  color: #606266;
}

.snapshot-info {
  margin-bottom: 16px;
}

.log-timeline {
  margin-top: 20px;
}

.log-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.log-action {
  font-weight: 500;
  color: #303133;
}

.log-user {
  font-size: 12px;
  color: #909399;
}

.low-stock-list {
  max-height: 200px;
  overflow-y: auto;
}

.low-stock-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.low-stock-item:last-child {
  border-bottom: none;
}

.stock-info {
  font-size: 12px;
  color: #f56c6c;
  font-weight: bold;
}

.snapshot-tips {
  margin-top: 16px;
}

.tip-list {
  margin: 0;
  padding-left: 20px;
  font-size: 14px;
  line-height: 1.6;
  color: #606266;
}

.tip-list li {
  margin-bottom: 4px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .stock-take-full-container {
    padding: 10px;
  }
  
  .card-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
  
  .header-actions {
    width: 100%;
    justify-content: flex-end;
  }
  
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .section-actions {
    width: 100%;
    justify-content: flex-end;
  }
  
  .selected-info {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
  
  .selected-summary {
    flex-direction: column;
    gap: 8px;
  }
  
  .log-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}

.back-btn {
  padding: 10px 16px;
  font-size: 16px;
  font-weight: 500;
  color: #409EFF;
}

.back-btn:hover {
  background-color: #ecf5ff;
  border-radius: 4px;
}

.back-btn i {
  margin-right: 6px;
  font-size: 18px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
}

:deep(.el-radio) {
  margin-right: 24px;
}

:deep(.el-divider__text) {
  background-color: #f5f7fa;
  font-weight: 500;
  color: #303133;
}
</style>