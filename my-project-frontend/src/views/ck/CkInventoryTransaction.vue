<template>
  <div class="inventory-transaction-container">
    <el-card class="manage-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">库存流水</span>
          <div class="header-actions">
            <el-button 
              @click="refreshList"
              :loading="loading"
            >
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
            <el-button 
              @click="handleExport"
            >
              <el-icon><Download /></el-icon>
              导出
            </el-button>
          </div>
        </div>
      </template>

      <!-- 筛选条件 -->
      <div class="filter-section">
        <el-form :model="filterForm" inline>
          <el-form-item label="产品名称">
            <el-input
              v-model="filterForm.productName"
              placeholder="请输入产品名称"
              clearable
              style="width: 200px"
            />
          </el-form-item>
          <el-form-item label="产品编码">
            <el-input
              v-model="filterForm.productCode"
              placeholder="请输入产品编码"
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
          <el-form-item label="业务类型">
            <el-select
              v-model="filterForm.businessType"
              placeholder="全部类型"
              clearable
              style="width: 150px"
            >
              <el-option
                v-for="item in businessTypeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="单据号">
            <el-input
              v-model="filterForm.orderNo"
              placeholder="请输入单据号"
              clearable
              style="width: 200px"
            />
          </el-form-item>
          <el-form-item label="操作时间">
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
                <div class="stat-value">{{ stats.totalRecords }}</div>
                <div class="stat-label">总记录数</div>
              </div>
            </div>
          </el-col>
          <el-col :xs="12" :sm="6" :lg="3">
            <div class="stat-item inbound">
              <div class="stat-icon">
                <el-icon><Top /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ stats.inboundCount }}</div>
                <div class="stat-label">入库记录</div>
              </div>
            </div>
          </el-col>
          <el-col :xs="12" :sm="6" :lg="3">
            <div class="stat-item outbound">
              <div class="stat-icon">
                <el-icon><Bottom /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ stats.outboundCount }}</div>
                <div class="stat-label">出库记录</div>
              </div>
            </div>
          </el-col>
          <el-col :xs="12" :sm="6" :lg="3">
            <div class="stat-item adjust">
              <div class="stat-icon">
                <el-icon><Refresh /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ stats.adjustCount }}</div>
                <div class="stat-label">调整记录</div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 库存流水列表 -->
      <div class="transaction-list-section">
        <el-table
          :data="transactionList"
          v-loading="loading"
          empty-text="暂无库存流水数据"
          class="transaction-table"
          row-key="id"
          @sort-change="handleSortChange"
        >
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column label="流水时间" width="160" sortable="custom" prop="transactionTime">
            <template #default="{ row }">
              <span>{{ formatTime(row.transactionTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="产品信息" width="280">
            <template #default="{ row }">
              <div class="product-info">
                <el-avatar :size="40" :src="row.productImage" class="product-image">
                  {{ row.productName?.charAt(0) || '产' }}
                </el-avatar>
                <div class="product-details">
                  <div class="product-name">{{ row.productName }}</div>
                  <div class="product-code">编码: {{ row.productCode }}</div>
                  <div class="product-spec">规格: {{ row.productSpec || '无' }}</div>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="仓库" width="120">
            <template #default="{ row }">
              <span>{{ row.warehouseName }}</span>
            </template>
          </el-table-column>
          <el-table-column label="业务类型" width="120" align="center">
            <template #default="{ row }">
              <el-tag :type="getBusinessTypeTagType(row.businessType)" size="small">
                {{ getBusinessTypeText(row.businessType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="单据号" width="180">
            <template #default="{ row }">
              <span class="order-no">{{ row.orderNo }}</span>
            </template>
          </el-table-column>
          <el-table-column label="变化数量" width="120" align="center" sortable="custom" prop="changeQuantity">
            <template #default="{ row }">
              <span :class="getQuantityClass(row.changeQuantity)">
                {{ row.changeQuantity > 0 ? '+' : '' }}{{ row.changeQuantity }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="变化前库存" width="120" align="center">
            <template #default="{ row }">
              <span>{{ row.beforeQuantity }}</span>
            </template>
          </el-table-column>
          <el-table-column label="变化后库存" width="120" align="center">
            <template #default="{ row }">
              <span>{{ row.afterQuantity }}</span>
            </template>
          </el-table-column>
          <el-table-column label="单位" width="80" align="center">
            <template #default="{ row }">
              <span>{{ row.unit }}</span>
            </template>
          </el-table-column>
          <el-table-column label="单价" width="100" align="right">
            <template #default="{ row }">
              <span>¥{{ (row.unitPrice || 0).toFixed(2) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="变化金额" width="120" align="right" sortable="custom" prop="changeAmount">
            <template #default="{ row }">
              <span :class="getAmountClass(row.changeAmount)" class="amount">
                {{ row.changeAmount > 0 ? '+' : '' }}¥{{ (row.changeAmount || 0).toFixed(2) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="操作人" width="120">
            <template #default="{ row }">
              <div class="operator-info">
                <el-avatar :size="24" :src="row.operatorAvatar" class="operator-avatar" />
                <span class="operator-name">{{ row.operatorName }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="备注" min-width="200" show-overflow-tooltip>
            <template #default="{ row }">
              <span>{{ row.remark || '--' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" fixed="right" align="center">
            <template #default="{ row }">
              <div class="action-buttons">
                <el-button
                  type="primary"
                  link
                  size="small"
                  @click="handleViewDetail(row)"
                >
                  详情
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

    <!-- 流水详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      :title="`库存流水详情`"
      width="60%"
      top="5vh"
    >
      <div v-if="currentTransaction">
        <TransactionDetail
          :transaction-data="currentTransaction"
          @close="detailDialogVisible = false"
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
import { ElMessage } from 'element-plus';
import { Refresh, Download, Document, Top, Bottom } from '@element-plus/icons-vue';
import { post, get } from '@/net';

const router = useRouter();
const loading = ref(false);
const detailDialogVisible = ref(false);
const currentTransaction = ref(null);

// 筛选表单
const filterForm = reactive({
  productName: '',
  productCode: '',
  warehouseId: '',
  businessType: '',
  orderNo: '',
  dateRange: []
});

// 分页信息
const pagination = reactive({
  current: 1,
  size: 20,
  total: 0
});

// 排序信息
const sortInfo = reactive({
  prop: '',
  order: ''
});

// 统计信息
const stats = reactive({
  totalRecords: 0,
  inboundCount: 0,
  outboundCount: 0,
  adjustCount: 0
});

// 库存流水列表
const transactionList = ref([]);
const warehouseList = ref([]);

// 选项数据
const businessTypeOptions = [
  { value: 1, label: '采购入库' },
  { value: 2, label: '生产入库' },
  { value: 3, label: '退货入库' },
  { value: 4, label: '调拨入库' },
  { value: 5, label: '其他入库' },
  { value: 6, label: '销售出库' },
  { value: 7, label: '退货出库' },
  { value: 8, label: '调拨出库' },
  { value: 9, label: '领用出库' },
  { value: 10, label: '其他出库' },
  { value: 11, label: '库存调整' },
  { value: 12, label: '库存盘点' }
];

// 计算属性
const inboundTransactions = computed(() => {
  return transactionList.value.filter(item => item.changeQuantity > 0 && item.businessType <= 5);
});

const outboundTransactions = computed(() => {
  return transactionList.value.filter(item => item.changeQuantity < 0 && item.businessType >= 6 && item.businessType <= 10);
});

const adjustTransactions = computed(() => {
  return transactionList.value.filter(item => item.businessType >= 11);
});

// 方法
const loadTransactionList = async () => {
  loading.value = true;
  try {
    const params = {
      page: pagination.current,
      size: pagination.size,
      ...filterForm,
      sortField: sortInfo.prop,
      sortOrder: sortInfo.order
    };
    
    // 处理日期范围
    if (filterForm.dateRange && filterForm.dateRange.length === 2) {
      params.startDate = filterForm.dateRange[0];
      params.endDate = filterForm.dateRange[1];
    }
    
    const res = await post('/api/auth/inventory/transaction/list', params);
    if (res && res.records) {
      transactionList.value = res.records.map(transaction => ({
        id: transaction.id || '',
        productId: transaction.productId || '',
        productName: transaction.productName || '',
        productCode: transaction.productCode || '',
        productSpec: transaction.productSpec || '',
        productImage: transaction.productImage || '/images/default-product.png',
        warehouseId: transaction.warehouseId || '',
        warehouseName: transaction.warehouseName || '',
        businessType: transaction.businessType || 0,
        orderNo: transaction.orderNo || '',
        orderId: transaction.orderId || '',
        changeQuantity: transaction.changeQuantity || 0,
        beforeQuantity: transaction.beforeQuantity || 0,
        afterQuantity: transaction.afterQuantity || 0,
        unit: transaction.unit || '个',
        unitPrice: transaction.unitPrice || 0,
        changeAmount: transaction.changeAmount || 0,
        operatorId: transaction.operatorId || '',
        operatorName: transaction.operatorName || '',
        operatorAvatar: transaction.operatorAvatar || '/images/default-avatar.png',
        remark: transaction.remark || '',
        transactionTime: transaction.transactionTime || new Date().toISOString(),
        createdAt: transaction.createdAt || new Date().toISOString()
      }));
      pagination.total = res.total || 0;
      
      // 更新统计信息
      updateStats();
    } else {
      transactionList.value = [];
      pagination.total = 0;
    }
  } catch (error) {
    console.error('加载库存流水列表失败:', error);
    ElMessage.error('加载库存流水列表失败');
    transactionList.value = [];
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
  stats.totalRecords = transactionList.value.length;
  stats.inboundCount = inboundTransactions.value.length;
  stats.outboundCount = outboundTransactions.value.length;
  stats.adjustCount = adjustTransactions.value.length;
};

const refreshList = () => {
  pagination.current = 1;
  loadTransactionList();
};

const handleSearch = () => {
  pagination.current = 1;
  loadTransactionList();
};

const handleReset = () => {
  Object.assign(filterForm, {
    productName: '',
    productCode: '',
    warehouseId: '',
    businessType: '',
    orderNo: '',
    dateRange: []
  });
  pagination.current = 1;
  loadTransactionList();
};

const handleExport = () => {
  ElMessage.info('导出功能开发中');
};

const handleSizeChange = (size) => {
  pagination.size = size;
  pagination.current = 1;
  loadTransactionList();
};

const handleCurrentChange = (page) => {
  pagination.current = page;
  loadTransactionList();
};

const handleSortChange = (sort) => {
  sortInfo.prop = sort.prop;
  sortInfo.order = sort.order === 'ascending' ? 'asc' : 
                   sort.order === 'descending' ? 'desc' : '';
  loadTransactionList();
};

const handleViewDetail = (transaction) => {
  currentTransaction.value = transaction;
  detailDialogVisible.value = true;
};

const getBusinessTypeText = (businessType) => {
  const typeObj = businessTypeOptions.find(item => item.value === businessType);
  return typeObj ? typeObj.label : '未知';
};

const getBusinessTypeTagType = (businessType) => {
  // 入库类型
  if (businessType <= 5) {
    return 'success';
  }
  // 出库类型
  else if (businessType <= 10) {
    return 'warning';
  }
  // 调整类型
  else {
    return 'info';
  }
};

const getQuantityClass = (quantity) => {
  if (quantity > 0) {
    return 'quantity-increase';
  } else if (quantity < 0) {
    return 'quantity-decrease';
  } else {
    return 'quantity-zero';
  }
};

const getAmountClass = (amount) => {
  if (amount > 0) {
    return 'amount-increase';
  } else if (amount < 0) {
    return 'amount-decrease';
  } else {
    return 'amount-zero';
  }
};

const formatTime = (timeString) => {
  if (!timeString) return '--';
  try {
    const date = new Date(timeString);
    return `${date.getFullYear()}-${padZero(date.getMonth() + 1)}-${padZero(date.getDate())} ${padZero(date.getHours())}:${padZero(date.getMinutes())}:${padZero(date.getSeconds())}`;
  } catch {
    return '--';
  }
};

const padZero = (num) => {
  return num < 10 ? `0${num}` : num;
};

onMounted(() => {
  loadTransactionList();
  loadWarehouseList();
});
</script>

<style scoped>
.inventory-transaction-container {
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

.stat-item.inbound .stat-icon {
  background-color: #67C23A;
}

.stat-item.outbound .stat-icon {
  background-color: #E6A23C;
}

.stat-item.adjust .stat-icon {
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

.transaction-list-section {
  margin-top: 20px;
}

.transaction-table {
  width: 100%;
}

.product-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.product-image {
  flex-shrink: 0;
  background-color: #409EFF;
  color: white;
  font-weight: bold;
}

.product-details {
  flex: 1;
  min-width: 0;
}

.product-name {
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.product-code {
  font-size: 12px;
  color: #909399;
  margin-bottom: 2px;
}

.product-spec {
  font-size: 12px;
  color: #909399;
}

.order-no {
  font-family: 'Courier New', monospace;
  font-weight: bold;
  color: #409EFF;
}

.operator-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.operator-avatar {
  flex-shrink: 0;
}

.operator-name {
  font-weight: 500;
}

.amount {
  font-weight: bold;
}

.action-buttons {
  display: flex;
  gap: 8px;
  justify-content: center;
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

/* 数量变化样式 */
.quantity-increase {
  color: #67C23A;
  font-weight: bold;
}

.quantity-decrease {
  color: #F56C6C;
  font-weight: bold;
}

.quantity-zero {
  color: #909399;
}

/* 金额变化样式 */
.amount-increase {
  color: #67C23A;
}

.amount-decrease {
  color: #F56C6C;
}

.amount-zero {
  color: #909399;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .inventory-transaction-container {
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
  
  .product-info {
    flex-direction: column;
    text-align: center;
    gap: 8px;
  }
  
  .operator-info {
    flex-direction: column;
    gap: 4px;
    text-align: center;
  }
}

/* 动画效果 */
.transaction-table :deep(.el-table__row) {
  transition: all 0.3s;
}

.transaction-table :deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}
</style>