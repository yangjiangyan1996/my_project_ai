<!-- CkInventoryHistory.vue -->
<template>
  <div class="inventory-history-container">
    <el-card class="manage-card" shadow="never">
      <template #header>
        <div class="card-header">
          <div class="card-title-section">
            <el-button 
              type="primary" 
              link 
              @click="router.back()"
              class="back-button"
            >
              <el-icon><ArrowLeft /></el-icon>
              返回
            </el-button>
            <span class="card-title">库存流水详情 - {{ productInfo.productName || '未知产品' }}</span>
          </div>
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

      <!-- 产品基本信息 -->
      <div class="product-info-section">
        <el-descriptions :column="4" border>
          <el-descriptions-item label="产品名称">{{ productInfo.productName }}</el-descriptions-item>
          <el-descriptions-item label="SKU">{{ productInfo.sku }}</el-descriptions-item>
          <el-descriptions-item label="规格型号">{{ productInfo.spec || '--' }}</el-descriptions-item>
          <el-descriptions-item label="单位">{{ productInfo.unitName }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 筛选条件 -->
      <div class="filter-section">
        <el-form :model="filterForm" inline>
          <el-form-item label="业务类型">
            <el-select
              v-model="filterForm.orderType"
              placeholder="全部类型"
              clearable
              style="width: 120px"
            >
              <el-option
                v-for="item in orderTypeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
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
          <el-form-item label="业务时间">
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
            <div class="stat-item current">
              <div class="stat-icon">
                <el-icon><Box /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value" :class="getStockClass(inventoryStats.currentStock)">
                  {{ formatNumber(inventoryStats.currentStock) }}
                </div>
                <div class="stat-label">当前库存</div>
              </div>
            </div>
          </el-col>
          <el-col :xs="12" :sm="6" :lg="3">
            <div class="stat-item total">
              <div class="stat-icon">
                <el-icon><Document /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ formatNumber(inventoryStats.totalCount) }}</div>
                <div class="stat-label">总记录数</div>
              </div>
            </div>
          </el-col>
          <el-col :xs="12" :sm="6" :lg="3">
            <div class="stat-item in">
              <div class="stat-icon">
                <el-icon><Top /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ formatNumber(inventoryStats.inQuantity) }}</div>
                <div class="stat-label">总入库</div>
              </div>
            </div>
          </el-col>
          <el-col :xs="12" :sm="6" :lg="3">
            <div class="stat-item out">
              <div class="stat-icon">
                <el-icon><Bottom /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ formatNumber(inventoryStats.outQuantity) }}</div>
                <div class="stat-label">总出库</div>
              </div>
            </div>
          </el-col>
          <el-col :xs="12" :sm="6" :lg="3">
            <div class="stat-item balance">
              <div class="stat-icon">
                <el-icon><TrendCharts /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value" :class="getNetChangeClass(inventoryStats.netChange)">
                  {{ formatNumber(inventoryStats.netChange) }}
                </div>
                <div class="stat-label">净变动</div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 综合流水列表 -->
      <div class="history-list-section">
        <el-table
          :data="historyList"
          v-loading="loading"
          empty-text="暂无库存流水数据"
          class="history-table"
          row-key="id"
          @sort-change="handleSortChange"
        >
          <el-table-column type="index" label="序号" width="60" align="center" />
          
          <el-table-column label="业务时间" width="160" sortable="custom" prop="transactionTime">
            <template #default="{ row }">
              <span>{{ formatTime(row.transactionTime) }}</span>
            </template>
          </el-table-column>

          <el-table-column label="业务类型" width="120" align="center">
            <template #default="{ row }">
              <el-tag :type="getOrderTypeTagType(row.orderType)" size="small">
                {{ getOrderTypeText(row.orderType) }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column label="单据编号" width="180">
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

          <el-table-column label="仓库" width="120">
            <template #default="{ row }">
              <span>{{ row.warehouseName }}</span>
            </template>
          </el-table-column>

          <el-table-column label="批次号" width="150">
            <template #default="{ row }">
              <span>{{ row.batchNo || '--' }}</span>
            </template>
          </el-table-column>

          <el-table-column label="变前数量" width="120" align="right" sortable="custom" prop="beforBalanceQuantity">
            <template #default="{ row }">
              <span :class="getChangeQuantityClass(row.beforBalanceQuantity)">
                {{ formatNumber(row.beforBalanceQuantity) }}
              </span>
            </template>
          </el-table-column>

          <el-table-column label="变动数量" width="120" align="right" sortable="custom" prop="changeQuantity">
            <template #default="{ row }">
              <span :class="getChangeQuantityClass(row.changeQuantity)">
                {{ formatNumber(row.changeQuantity) }}
              </span>
            </template>
          </el-table-column>

          <el-table-column label="结存数量" width="120" align="right" sortable="custom" prop="balanceQuantity">
            <template #default="{ row }">
              <span :class="getStockClass(row.balanceQuantity)">
                {{ formatNumber(row.balanceQuantity) }}
              </span>
            </template>
          </el-table-column>

          <el-table-column label="单价(¥)" width="120" align="right">
            <template #default="{ row }">
              <span class="amount">{{ formatCurrency(row.priceUnit) }}</span>
            </template>
          </el-table-column>

          <el-table-column label="总价(¥)" width="120" align="right">
            <template #default="{ row }">
              <span class="amount">{{ formatCurrency(row.priceTotal) }}</span>
            </template>
          </el-table-column>

          <!-- 四张表关联数据展示 -->
          <!-- <el-table-column label="库存层级" width="300">
            <template #default="{ row }">
              <div class="inventory-hierarchy">
                <div class="hierarchy-item">
                  <span class="label">总库存:</span>
                  <span :class="getStockClass(row.totalInventory)">
                    {{ formatNumber(row.totalInventory) }}
                  </span>
                </div>
                <div class="hierarchy-item">
                  <span class="label">仓库库存:</span>
                  <span :class="getStockClass(row.warehouseInventory)">
                    {{ formatNumber(row.warehouseInventory) }}
                  </span>
                </div>
                <div class="hierarchy-item">
                  <span class="label">批次库存:</span>
                  <span :class="getStockClass(row.batchInventory)">
                    {{ formatNumber(row.batchInventory) }}
                  </span>
                </div>
              </div>
            </template>
          </el-table-column> -->

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

    <!-- 仓库批次详情对话框 -->
    <el-dialog
      v-model="warehouseDetailVisible"
      :title="`批次库存详情 - ${currentWarehouse?.warehouseName || ''}`"
      width="80%"
      top="5vh"
    >
      <div v-if="currentWarehouse">
        <el-table :data="currentWarehouse.batchDetails" empty-text="无批次数据">
          <el-table-column label="批次号" prop="batchNo" width="150" />
          <el-table-column label="生产日期" width="120" align="center">
            <template #default="{ row }">
              <span>{{ row.productionDate || '--' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="当前数量" width="120" align="center">
            <template #default="{ row }">
              <span :class="getStockClass(row.quantity)">{{ formatNumber(row.quantity) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="锁定数量" width="120" align="center">
            <template #default="{ row }">
              <span>{{ formatNumber(row.lockedQuantity) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="可用数量" width="120" align="center">
            <template #default="{ row }">
              <span :class="getStockClass(row.availableQuantity)">{{ formatNumber(row.availableQuantity) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="来源入库单" width="120" align="center">
            <template #default="{ row }">
              <span>{{ row.inboundOrderId ? `IN${row.inboundOrderId}` : '--' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" width="160">
            <template #default="{ row }">
              <span>{{ formatTime(row.createdAt) }}</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import { 
  Refresh, 
  Download, 
  Document, 
  Top, 
  Bottom, 
  TrendCharts,
  ArrowLeft,
  Box
} from '@element-plus/icons-vue';
import { post, get } from '@/net';

const router = useRouter();
const route = useRoute();
const loading = ref(false);
const warehouseDetailVisible = ref(false);
const currentWarehouse = ref(null);

// 产品信息
const productInfo = reactive({
  productId: null,
  productName: '',
  sku: '',
  spec: '',
  unitName: ''
});

// 库存统计信息
const inventoryStats = reactive({
  currentStock: 0,
  totalCount: 0,
  inQuantity: 0,
  outQuantity: 0,
  netChange: 0
});

// 筛选表单
const filterForm = reactive({
  orderType: '',
  warehouseId: '',
  changeType: '',
  dateRange: []
});

// 分页信息
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
});

// 排序信息
const sortInfo = reactive({
  prop: '',
  order: ''
});

// 数据列表
const historyList = ref([]);
const warehouseList = ref([]);
const warehouseSummary = ref([]);

// 选项数据
const orderTypeOptions = [
  { value: 1, label: '入库单' },
  { value: 2, label: '出库单' }
];

const changeTypeOptions = [
  { value: 'in', label: '入库' },
  { value: 'out', label: '出库' }
];

// 方法
const loadProductInfo = async () => {
  const productId = route.params.id;
  if (!productId) {
    ElMessage.error('产品ID不存在');
    router.back();
    return;
  }

  try {
    const res = await get(`/api/auth/product/detail?productId=${productId}`);
    if (res) {
      Object.assign(productInfo, {
        productId: res.id,
        productName: res.name,
        sku: res.sku,
        spec: res.spec,
        unitName: res.unitName
      });
    }
  } catch (error) {
    console.error('加载产品信息失败:', error);
    ElMessage.error('加载产品信息失败');
  }
};

const loadInventoryStats = async () => {
  const productId = route.params.id;
  if (!productId) return;

  try {
    const res = await get(`/api/auth/inventory/productInventoryDetail?productId=${productId}`);
    if (res) {
      Object.assign(inventoryStats, {
        currentStock: res.currentStock || 0,
        totalCount: res.totalCount || 0,
        inQuantity: res.inQuantity || 0,
        outQuantity: res.outQuantity || 0,
        netChange: res.netChange || 0
      });
    }
  } catch (error) {
    console.error('加载库存统计信息失败:', error);
    ElMessage.error('加载库存统计信息失败');
  }
};

const loadWarehouseSummary = async () => {
  try {
    const res = await get('/api/auth/warehouse/list');
    warehouseSummary.value = res || [];
  } catch (error) {
    console.error('加载仓库汇总失败:', error);
    warehouseSummary.value = [];
  }
};

const loadHistoryList = async () => {
  loading.value = true;
  try {
    const params = {
      page: pagination.current,
      size: pagination.size,
      productId: route.params.id,
      ...filterForm,
      sortField: sortInfo.prop,
      sortOrder: sortInfo.order
    };
    
    // 处理日期范围
    if (filterForm.dateRange && filterForm.dateRange.length === 2) {
      params.startDate = filterForm.dateRange[0];
      params.endDate = filterForm.dateRange[1];
    }
    
    // 处理变动方向
    if (filterForm.changeType === 'in') {
      params.minChangeQuantity = 0.0001; // 正数表示入库
    } else if (filterForm.changeType === 'out') {
      params.maxChangeQuantity = -0.0001; // 负数表示出库
    }
    
    const res = await post('/api/auth/inventory/transaction/pageList', params);
    if (res && res.records) {
      historyList.value = res.records.map(item => ({
        // 流水表数据
        id: item.id,
        orderType: item.orderType,
        orderNo: item.orderNo,
        warehouseId: item.warehouseId,
        warehouseName: item.warehouseName,
        changeQuantity: item.changeQuantity,
        beforBalanceQuantity: item.beforBalanceQuantity,
        balanceQuantity: item.balanceQuantity,
        priceUnit: item.priceUnit,
        priceTotal: item.priceTotal,
        remark: item.remark,
        transactionTime: item.transactionTime,
        operatorName: item.operatorName,
        operatorAvatar: item.operatorAvatar,
        batchNo: item.batchNo,
        
        // 关联的四张表数据
        totalInventory: item.totalInventory, // ck_inventory
        warehouseInventory: item.warehouseInventory, // ck_inventory_warehouse
        batchInventory: item.batchInventory, // ck_inventory_batch
        currentBatchQuantity: item.currentBatchQuantity,
        
        isUrgent: item.isUrgent || false
      }));
      pagination.total = res.total || 0;
    } else {
      historyList.value = [];
      pagination.total = 0;
    }
  } catch (error) {
    console.error('加载库存流水失败:', error);
    ElMessage.error('加载库存流水失败');
    historyList.value = [];
  } finally {
    loading.value = false;
  }
};

const loadWarehouseList = async () => {
  try {
    const res = await get('/api/auth/warehouse/list');
    warehouseList.value = res || [];
  } catch (error) {
    console.error('加载仓库列表失败:', error);
    warehouseList.value = [];
  }
};

const showWarehouseDetail = async (warehouse) => {
  try {
    const res = await get(`/api/auth/inventory/batch/details?productId=${route.params.id}&warehouseId=${warehouse.warehouseId}`);
    currentWarehouse.value = {
      ...warehouse,
      batchDetails: res || []
    };
    warehouseDetailVisible.value = true;
  } catch (error) {
    console.error('加载批次详情失败:', error);
    ElMessage.error('加载批次详情失败');
  }
};

const refreshList = () => {
  pagination.current = 1;
  loadHistoryList();
  loadInventoryStats();
  loadWarehouseSummary();
};

const handleSearch = () => {
  pagination.current = 1;
  loadHistoryList();
};

const handleReset = () => {
  Object.assign(filterForm, {
    orderType: '',
    warehouseId: '',
    changeType: '',
    dateRange: []
  });
  pagination.current = 1;
  loadHistoryList();
};

const handleExport = () => {
  ElMessage.info('导出功能开发中');
};

const handleSizeChange = (size) => {
  pagination.size = size;
  pagination.current = 1;
  loadHistoryList();
};

const handleCurrentChange = (page) => {
  pagination.current = page;
  loadHistoryList();
};

const handleSortChange = (sort) => {
  sortInfo.prop = sort.prop;
  sortInfo.order = sort.order === 'ascending' ? 'asc' : 
                   sort.order === 'descending' ? 'desc' : '';
  loadHistoryList();
};

// 格式化数字显示
const formatNumber = (value) => {
  if (value === null || value === undefined) return '0';
  const num = Number(value);
  if (isNaN(num)) return '0';
  // 如果是整数，不显示小数位
  if (Number.isInteger(num)) {
    return num.toString();
  }
  // 否则显示4位小数
  return num.toFixed(4).replace(/\.?0+$/, '');
};

// 格式化货币显示
const formatCurrency = (value) => {
  if (value === null || value === undefined) return '0.00';
  const num = Number(value);
  if (isNaN(num)) return '0.00';
  return num.toFixed(2);
};

// 格式化时间显示
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

const getStockClass = (quantity) => {
  const num = Number(quantity) || 0;
  if (num <= 0) {
    return 'stock-out';
  } else if (num <= 10) {
    return 'stock-low';
  }
  return 'stock-normal';
};

const getChangeQuantityClass = (quantity) => {
  const num = Number(quantity) || 0;
  if (num > 0) {
    return 'change-in';
  } else if (num < 0) {
    return 'change-out';
  }
  return 'change-zero';
};

const getNetChangeClass = (quantity) => {
  const num = Number(quantity) || 0;
  if (num > 0) {
    return 'net-in';
  } else if (num < 0) {
    return 'net-out';
  }
  return 'net-zero';
};

const getOrderTypeText = (orderType) => {
  const typeObj = orderTypeOptions.find(item => item.value === orderType);
  return typeObj ? typeObj.label : '未知';
};

const getOrderTypeTagType = (orderType) => {
  const types = {
    1: 'success', // 入库单
    2: 'warning'  // 出库单
  };
  return types[orderType] || '';
};

onMounted(() => {
  loadProductInfo();
  loadInventoryStats();
  loadWarehouseSummary();
  loadHistoryList();
  loadWarehouseList();
});
</script>

<style scoped>
.inventory-history-container {
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

.card-title-section {
  display: flex;
  align-items: center;
  gap: 12px;
}

.back-button {
  margin-right: 8px;
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

.product-info-section {
  margin-bottom: 20px;
  padding: 16px;
  background-color: #f8f9fa;
  border-radius: 4px;
  border: 1px solid #ebeef5;
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

.stat-item.current .stat-icon {
  background-color: #409EFF;
}

.stat-item.total .stat-icon {
  background-color: #909399;
}

.stat-item.in .stat-icon {
  background-color: #67C23A;
}

.stat-item.out .stat-icon {
  background-color: #E6A23C;
}

.stat-item.balance .stat-icon {
  background-color: #F56C6C;
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

.history-list-section {
  margin-top: 20px;
}

.history-table {
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
  color: #E6A23C;
}

.inventory-hierarchy {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.hierarchy-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
}

.hierarchy-item .label {
  color: #606266;
  min-width: 70px;
}

.pagination-section {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

/* 库存数量样式 */
.stock-normal {
  color: #67C23A;
  font-weight: bold;
}

.stock-low {
  color: #E6A23C;
  font-weight: bold;
}

.stock-out {
  color: #F56C6C;
  font-weight: bold;
}

/* 变动数量样式 */
.change-in {
  color: #67C23A;
  font-weight: bold;
}

.change-out {
  color: #F56C6C;
  font-weight: bold;
}

.change-zero {
  color: #909399;
}

/* 净变动样式 */
.net-in {
  color: #67C23A;
  font-weight: bold;
}

.net-out {
  color: #F56C6C;
  font-weight: bold;
}

.net-zero {
  color: #909399;
  font-weight: bold;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .inventory-history-container {
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
  
  .inventory-hierarchy {
    gap: 2px;
  }
  
  .hierarchy-item {
    font-size: 11px;
  }
}

/* 动画效果 */
.history-table :deep(.el-table__row) {
  transition: all 0.3s;
}

.history-table :deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}
</style>