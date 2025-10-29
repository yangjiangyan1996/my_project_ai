<template>
  <div class="stock-take-container">
    <el-card class="manage-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">库存盘点</span>
          <div class="header-actions">
            <el-button 
              type="primary" 
              @click="handleCreate"
            >
              <el-icon><Plus /></el-icon>
              新建盘点单
            </el-button>
            <el-button 
              @click="refreshList"
              :loading="loading"
            >
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </div>
      </template>

      <!-- 筛选条件 -->
      <div class="filter-section">
        <el-form :model="filterForm" inline>
          <el-form-item label="盘点单号">
            <el-input
              v-model="filterForm.orderNo"
              placeholder="请输入盘点单号"
              clearable
              style="width: 200px"
            />
          </el-form-item>
          <el-form-item label="仓库">
            <el-select
              v-model="filterForm.warehouseId"
              placeholder="全部仓库"
              clearable
              style="width: 150px"
            >
              <el-option
                v-for="warehouse in warehouseList"
                :key="warehouse.id"
                :label="warehouse.name"
                :value="warehouse.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="盘点类型">
            <el-select
              v-model="filterForm.takeType"
              placeholder="全部类型"
              clearable
              style="width: 120px"
            >
              <el-option
                v-for="item in takeTypeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select
              v-model="filterForm.status"
              placeholder="全部状态"
              clearable
              style="width: 120px"
            >
              <el-option
                v-for="item in statusOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="创建时间">
            <el-date-picker
              v-model="filterForm.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              style="width: 240px"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
            <el-button @click="handleExport">导出</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 统计信息 -->
      <div class="stats-section">
        <el-row :gutter="20">
          <el-col :xs="12" :sm="6" :lg="3">
            <div class="stat-item total">
              <div class="stat-icon">
                <el-icon><Document /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ stats.total }}</div>
                <div class="stat-label">总单数</div>
              </div>
            </div>
          </el-col>
          <el-col :xs="12" :sm="6" :lg="3">
            <div class="stat-item counting">
              <div class="stat-icon">
                <el-icon><Clock /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ stats.counting }}</div>
                <div class="stat-label">盘点中</div>
              </div>
            </div>
          </el-col>
          <el-col :xs="12" :sm="6" :lg="3">
            <div class="stat-item completed">
              <div class="stat-icon">
                <el-icon><CircleCheck /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ stats.completed }}</div>
                <div class="stat-label">已完成</div>
              </div>
            </div>
          </el-col>
          <el-col :xs="12" :sm="6" :lg="3">
            <div class="stat-item adjusted">
              <div class="stat-icon">
                <el-icon><Finished /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ stats.adjusted }}</div>
                <div class="stat-label">已调整</div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 盘点单列表 -->
      <div class="stock-take-list-section">
        <el-table
          :data="stockTakeList"
          v-loading="loading"
          empty-text="暂无盘点单数据"
          class="stock-take-table"
          row-key="id"
        >
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column label="盘点单号" width="180" fixed="left">
            <template #default="{ row }">
              <div class="order-info">
                <span class="order-no">{{ row.orderNo }}</span>
                <el-tag 
                  v-if="row.isUrgent" 
                  type="danger" 
                  size="small" 
                  class="urgent-tag"
                >
                  紧急
                </el-tag>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="盘点类型" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="getTypeTagType(row.takeType)" size="small">
                {{ getTypeText(row.takeType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="仓库" width="120">
            <template #default="{ row }">
              <span>{{ row.warehouseName }}</span>
            </template>
          </el-table-column>
          <el-table-column label="盘点范围" width="120">
            <template #default="{ row }">
              <span>{{ getRangeText(row.takeRange) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="产品数量" width="100" align="center">
            <template #default="{ row }">
              <span>{{ row.productCount }} 种</span>
            </template>
          </el-table-column>
          <el-table-column label="盘盈数量" width="100" align="center">
            <template #default="{ row }">
              <span class="profit">{{ row.profitQuantity || 0 }}</span>
            </template>
          </el-table-column>
          <el-table-column label="盘亏数量" width="100" align="center">
            <template #default="{ row }">
              <span class="loss">{{ row.lossQuantity || 0 }}</span>
            </template>
          </el-table-column>
          <el-table-column label="差异金额" width="120" align="right">
            <template #default="{ row }">
              <span :class="getAmountClass(row.differenceAmount)" class="amount">
                {{ row.differenceAmount > 0 ? '+' : '' }}¥{{ (row.differenceAmount || 0).toFixed(2) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="申请人" width="120">
            <template #default="{ row }">
              <div class="applicant-info">
                <el-avatar :size="24" :src="row.applicantAvatar" class="applicant-avatar" />
                <span class="applicant-name">{{ row.applicantName }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" width="160">
            <template #default="{ row }">
              <span>{{ formatTime(row.createdAt) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag 
                :type="getStatusTagType(row.status)" 
                size="small"
              >
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="250" fixed="right" align="center">
            <template #default="{ row }">
              <div class="action-buttons">
                <el-button
                  type="primary"
                  link
                  size="small"
                  @click="handleView(row)"
                >
                  查看
                </el-button>
                <el-button
                  type="warning"
                  link
                  size="small"
                  @click="handleEdit(row)"
                  v-if="row.status === 0 || row.status === 1"
                >
                  编辑
                </el-button>
                <el-button
                  type="success"
                  link
                  size="small"
                  @click="handleSubmit(row)"
                  v-if="row.status === 0"
                >
                  提交
                </el-button>
                <el-button
                  type="info"
                  link
                  size="small"
                  @click="handleCount(row)"
                  v-if="row.status === 1"
                >
                  盘点
                </el-button>
                <el-button
                  type="danger"
                  link
                  size="small"
                  @click="handleDelete(row)"
                  v-if="row.status === 0 || row.status === 1"
                >
                  删除
                </el-button>
                <el-button
                  type="success"
                  link
                  size="small"
                  @click="handleAdjust(row)"
                  v-if="row.status === 2"
                >
                  调整
                </el-button>
                <el-button
                  type="info"
                  link
                  size="small"
                  @click="handleCancel(row)"
                  v-if="row.status === 1 || row.status === 2"
                >
                  取消
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-section">
          <el-pagination
            v-model:current-page="pagination.current"
            v-model:page-size="pagination.size"
            :total="pagination.total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </el-card>

    <!-- 盘点单详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      :title="`盘点单详情 - ${currentStockTake?.orderNo || '未知单号'}`"
      width="90%"
      top="5vh"
    >
      <div v-if="currentStockTake">
        <StockTakeDetail
          :stock-take-data="currentStockTake"
          @close="detailDialogVisible = false"
          @refresh="refreshList"
        />
      </div>
      <div v-else class="no-data">
        <el-empty description="数据加载失败" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Refresh, Document, Clock, CircleCheck, Finished } from '@element-plus/icons-vue';
import { post, get } from '@/net';

const router = useRouter();
const loading = ref(false);
const detailDialogVisible = ref(false);
const currentStockTake = ref(null);

// 筛选表单
const filterForm = reactive({
  orderNo: '',
  warehouseId: '',
  takeType: '',
  status: '',
  dateRange: []
});

// 分页信息
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
});

// 统计信息
const stats = reactive({
  total: 0,
  counting: 0,
  completed: 0,
  adjusted: 0
});

// 盘点单列表
const stockTakeList = ref([]);
const warehouseList = ref([]);

// 选项数据
const takeTypeOptions = [
  { value: 1, label: '全盘' },
  { value: 2, label: '抽盘' },
  { value: 3, label: '循环盘点' },
  { value: 4, label: '定期盘点' },
  { value: 5, label: '临时盘点' }
];

const takeRangeOptions = [
  { value: 1, label: '全部产品' },
  { value: 2, label: '指定分类' },
  { value: 3, label: '指定产品' },
  { value: 4, label: '低库存产品' }
];

const statusOptions = [
  { value: 0, label: '草稿' },
  { value: 1, label: '盘点中' },
  { value: 2, label: '已完成' },
  { value: 3, label: '已调整' },
  { value: 4, label: '已取消' }
];

// 计算属性
const countingStockTakes = computed(() => {
  return stockTakeList.value.filter(item => item.status === 1);
});

const completedStockTakes = computed(() => {
  return stockTakeList.value.filter(item => item.status === 2);
});

const adjustedStockTakes = computed(() => {
  return stockTakeList.value.filter(item => item.status === 3);
});

// 方法
const loadStockTakeList = async () => {
  loading.value = true;
  try {
    const params = {
      page: pagination.current,
      size: pagination.size,
      ...filterForm
    };
    
    // 处理日期范围
    if (filterForm.dateRange && filterForm.dateRange.length === 2) {
      params.startDate = filterForm.dateRange[0];
      params.endDate = filterForm.dateRange[1];
    }
    
    const res = await post('/api/auth/stock-take/list', params);
    if (res && res.records) {
      stockTakeList.value = res.records.map(stockTake => ({
        id: stockTake.id || '',
        orderNo: stockTake.orderNo || '',
        takeType: stockTake.takeType || 1,
        takeRange: stockTake.takeRange || 1,
        warehouseId: stockTake.warehouseId || '',
        warehouseName: stockTake.warehouseName || '',
        productCount: stockTake.productCount || 0,
        profitQuantity: stockTake.profitQuantity || 0,
        lossQuantity: stockTake.lossQuantity || 0,
        differenceAmount: stockTake.differenceAmount || 0,
        applicantId: stockTake.applicantId || '',
        applicantName: stockTake.applicantName || '',
        applicantAvatar: stockTake.applicantAvatar || '/images/default-avatar.png',
        status: stockTake.status || 0,
        isUrgent: stockTake.isUrgent || false,
        remark: stockTake.remark || '',
        startTime: stockTake.startTime || '',
        endTime: stockTake.endTime || '',
        createdAt: stockTake.createdAt || new Date().toISOString(),
        updatedAt: stockTake.updatedAt || new Date().toISOString()
      }));
      pagination.total = res.total || 0;
      
      // 更新统计信息
      updateStats();
    } else {
      stockTakeList.value = [];
      pagination.total = 0;
    }
  } catch (error) {
    console.error('加载盘点单列表失败:', error);
    ElMessage.error('加载盘点单列表失败');
    stockTakeList.value = [];
  } finally {
    loading.value = false;
  }
};

const loadWarehouseList = async () => {
  try {
    const res = await get('/api/auth/warehouse/list');
    warehouseList.value = res.records || [];
  } catch (error) {
    console.error('加载仓库列表失败:', error);
    warehouseList.value = [];
  }
};

const updateStats = () => {
  stats.total = stockTakeList.value.length;
  stats.counting = countingStockTakes.value.length;
  stats.completed = completedStockTakes.value.length;
  stats.adjusted = adjustedStockTakes.value.length;
};

const refreshList = () => {
  pagination.current = 1;
  loadStockTakeList();
};

const handleSearch = () => {
  pagination.current = 1;
  loadStockTakeList();
};

const handleReset = () => {
  Object.assign(filterForm, {
    orderNo: '',
    warehouseId: '',
    takeType: '',
    status: '',
    dateRange: []
  });
  pagination.current = 1;
  loadStockTakeList();
};

const handleExport = () => {
  ElMessage.info('导出功能开发中');
};

const handleSizeChange = (size) => {
  pagination.size = size;
  pagination.current = 1;
  loadStockTakeList();
};

const handleCurrentChange = (page) => {
  pagination.current = page;
  loadStockTakeList();
};

const handleCreate = () => {
  router.push('/index/ckStockTakeCreate');
};

const handleView = (stockTake) => {
  currentStockTake.value = stockTake;
  detailDialogVisible.value = true;
};

const handleEdit = (stockTake) => {
  router.push(`/stock-take/edit/${stockTake.id}`);
};

const handleSubmit = async (stockTake) => {
  try {
    await ElMessageBox.confirm(
      `确定要提交盘点单"${stockTake.orderNo}"吗？提交后将开始盘点。`,
      '提交确认',
      { type: 'warning' }
    );
    
    const res = await post('/api/auth/stock-take/submit', {
      id: stockTake.id
    });
    
    if (res) {
      ElMessage.success('提交成功，开始盘点');
      refreshList();
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('提交失败');
    }
  }
};

const handleCount = (stockTake) => {
  router.push(`/stock-take/count/${stockTake.id}`);
};

const handleDelete = async (stockTake) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除盘点单"${stockTake.orderNo}"吗？此操作不可恢复！`,
      '删除确认',
      { 
        type: 'error',
        confirmButtonText: '确定删除',
        cancelButtonText: '取消'
      }
    );
    
    const res = await post('/api/auth/stock-take/delete', {
      id: stockTake.id
    });
    
    if (res) {
      ElMessage.success('删除成功');
      refreshList();
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败');
    }
  }
};

const handleAdjust = async (stockTake) => {
  try {
    await ElMessageBox.confirm(
      `确定要根据盘点结果调整库存吗？此操作将更新实际库存数量。`,
      '调整确认',
      { type: 'warning' }
    );
    
    const res = await post('/api/auth/stock-take/adjust', {
      id: stockTake.id
    });
    
    if (res) {
      ElMessage.success('库存调整成功');
      refreshList();
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('调整失败');
    }
  }
};

const handleCancel = async (stockTake) => {
  try {
    await ElMessageBox.confirm(
      `确定要取消盘点单"${stockTake.orderNo}"吗？`,
      '取消确认',
      { type: 'warning' }
    );
    
    const res = await post('/api/auth/stock-take/cancel', {
      id: stockTake.id
    });
    
    if (res) {
      ElMessage.success('取消成功');
      refreshList();
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消失败');
    }
  }
};

const getTypeText = (takeType) => {
  const typeObj = takeTypeOptions.find(item => item.value === takeType);
  return typeObj ? typeObj.label : '未知';
};

const getTypeTagType = (takeType) => {
  const types = {
    1: 'primary',
    2: 'success',
    3: 'warning',
    4: 'info',
    5: 'danger'
  };
  return types[takeType] || '';
};

const getRangeText = (takeRange) => {
  const rangeObj = takeRangeOptions.find(item => item.value === takeRange);
  return rangeObj ? rangeObj.label : '未知';
};

const getStatusText = (status) => {
  const statusObj = statusOptions.find(item => item.value === status);
  return statusObj ? statusObj.label : '未知';
};

const getStatusTagType = (status) => {
  const types = {
    0: 'info',      // 草稿
    1: 'warning',   // 盘点中
    2: 'success',   // 已完成
    3: '',          // 已调整
    4: 'danger'     // 已取消
  };
  return types[status] || '';
};

const getAmountClass = (amount) => {
  if (amount > 0) {
    return 'amount-profit';
  } else if (amount < 0) {
    return 'amount-loss';
  } else {
    return 'amount-zero';
  }
};

const formatTime = (timeString) => {
  if (!timeString) return '--';
  try {
    const date = new Date(timeString);
    return `${date.getFullYear()}-${padZero(date.getMonth() + 1)}-${padZero(date.getDate())} ${padZero(date.getHours())}:${padZero(date.getMinutes())}`;
  } catch {
    return '--';
  }
};

const padZero = (num) => {
  return num < 10 ? `0${num}` : num;
};

onMounted(() => {
  loadStockTakeList();
  loadWarehouseList();
});
</script>

<style scoped>
.stock-take-container {
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

.filter-section {
  padding: 20px 0;
  border-bottom: 1px solid #ebeef5;
}

.stats-section {
  padding: 20px 0;
}

.stat-item {
  display: flex;
  align-items: center;
  padding: 16px;
  border-radius: 8px;
  background: white;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  color: white;
  font-size: 24px;
}

.stat-item.total .stat-icon {
  background-color: #409EFF;
}

.stat-item.counting .stat-icon {
  background-color: #E6A23C;
}

.stat-item.completed .stat-icon {
  background-color: #67C23A;
}

.stat-item.adjusted .stat-icon {
  background-color: #909399;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.stock-take-list-section {
  margin-top: 20px;
}

.stock-take-table {
  width: 100%;
}

.order-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.order-no {
  font-family: 'Courier New', monospace;
  font-weight: bold;
  color: #409EFF;
}

.urgent-tag {
  margin-left: 4px;
}

.profit {
  color: #67C23A;
  font-weight: bold;
}

.loss {
  color: #F56C6C;
  font-weight: bold;
}

.amount {
  font-weight: bold;
}

.amount-profit {
  color: #67C23A;
}

.amount-loss {
  color: #F56C6C;
}

.amount-zero {
  color: #909399;
}

.applicant-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.applicant-avatar {
  flex-shrink: 0;
}

.applicant-name {
  font-weight: 500;
}

.action-buttons {
  display: flex;
  gap: 8px;
  justify-content: center;
  flex-wrap: wrap;
}

.pagination-section {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.no-data {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 200px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .stock-take-container {
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
  
  .filter-section .el-form-item {
    margin-bottom: 12px;
  }
  
  .stats-section .el-col {
    margin-bottom: 12px;
  }
  
  .action-buttons {
    flex-direction: column;
    gap: 4px;
  }
  
  .applicant-info {
    flex-direction: column;
    gap: 4px;
    text-align: center;
  }
}

/* 动画效果 */
.stock-take-table :deep(.el-table__row) {
  transition: all 0.3s;
}

.stock-take-table :deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}
</style>