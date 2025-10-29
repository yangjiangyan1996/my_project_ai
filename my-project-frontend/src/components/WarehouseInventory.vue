<template>
  <div class="warehouse-inventory">
    <!-- 库存统计 -->
    <div class="inventory-stats">
      <el-row :gutter="20">
        <el-col :xs="12" :sm="6">
          <div class="stat-card">
            <div class="stat-icon total">
              <el-icon><Box /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.totalProducts }}</div>
              <div class="stat-label">产品种类</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6">
          <div class="stat-card">
            <div class="stat-icon quantity">
              <el-icon><TrendCharts /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.totalQuantity }}</div>
              <div class="stat-label">库存总量</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6">
          <div class="stat-card">
            <div class="stat-icon value">
              <el-icon><Money /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">¥{{ stats.totalValue }}</div>
              <div class="stat-label">库存总值</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6">
          <div class="stat-card">
            <div class="stat-icon warning">
              <el-icon><Warning /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.lowStockCount }}</div>
              <div class="stat-label">低库存预警</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

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
        <el-form-item label="产品SKU">
          <el-input
            v-model="filterForm.sku"
            placeholder="请输入产品SKU"
            clearable
            style="width: 150px"
          />
        </el-form-item>
        <el-form-item label="库存状态">
          <el-select
            v-model="filterForm.stockStatus"
            placeholder="全部状态"
            clearable
            style="width: 120px"
          >
            <el-option label="正常" value="normal" />
            <el-option label="低库存" value="low" />
            <el-option label="缺货" value="out" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button @click="handleExport">导出</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 库存列表 -->
    <div class="inventory-list">
      <el-table
        :data="inventoryList"
        v-loading="loading"
        empty-text="暂无库存数据"
        class="inventory-table"
      >
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column label="产品信息" min-width="200">
          <template #default="{ row }">
            <div class="product-info">
              <div class="product-name">{{ row.productName }}</div>
              <div class="product-sku">{{ row.sku }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="规格型号" width="120">
          <template #default="{ row }">
            <span>{{ row.spec || '--' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="单位" width="80" align="center">
          <template #default="{ row }">
            <span>{{ row.unit }}</span>
          </template>
        </el-table-column>
        <el-table-column label="当前库存" width="120" align="center">
          <template #default="{ row }">
            <span :class="getStockClass(row.quantity, row.minStock)">
              {{ row.quantity }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="最低库存" width="100" align="center">
          <template #default="{ row }">
            <span>{{ row.minStock || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="最高库存" width="100" align="center">
          <template #default="{ row }">
            <span>{{ row.maxStock || '--' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="库存状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStockStatusType(row.quantity, row.minStock)" size="small">
              {{ getStockStatusText(row.quantity, row.minStock) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="平均成本" width="120" align="right">
          <template #default="{ row }">
            <span>¥{{ (row.avgCost || 0).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="库存金额" width="120" align="right">
          <template #default="{ row }">
            <span>¥{{ ((row.avgCost || 0) * row.quantity).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="最后更新" width="160">
          <template #default="{ row }">
            <span>{{ formatTime(row.updatedAt) }}</span>
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { Box, TrendCharts, Money, Warning } from '@element-plus/icons-vue';
import { post } from '@/net';

const props = defineProps({
  warehouseId: {
    type: String,
    required: true
  }
});

const emit = defineEmits(['close']);

const loading = ref(false);

// 筛选表单
const filterForm = reactive({
  productName: '',
  sku: '',
  stockStatus: ''
});

// 分页信息
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
});

// 统计信息
const stats = reactive({
  totalProducts: 0,
  totalQuantity: 0,
  totalValue: 0,
  lowStockCount: 0
});

// 库存列表
const inventoryList = ref([]);

// 方法
const loadInventoryData = async () => {
  if (!props.warehouseId) return;
  
  loading.value = true;
  try {
    const params = {
      page: pagination.current,
      size: pagination.size,
      warehouseId: props.warehouseId,
      ...filterForm
    };
    
    const res = await post('/api/auth/inventory/listByWarehouse', params);
    if (res && res.records) {
      inventoryList.value = res.records;
      pagination.total = res.total || 0;
      
      // 更新统计信息
      updateStats();
    } else {
      inventoryList.value = [];
      pagination.total = 0;
    }
  } catch (error) {
    console.error('加载库存数据失败:', error);
    ElMessage.error('加载库存数据失败');
    inventoryList.value = [];
  } finally {
    loading.value = false;
  }
};

const updateStats = () => {
  stats.totalProducts = inventoryList.value.length;
  stats.totalQuantity = inventoryList.value.reduce((sum, item) => sum + item.quantity, 0);
  stats.totalValue = inventoryList.value.reduce((sum, item) => sum + (item.avgCost || 0) * item.quantity, 0);
  stats.lowStockCount = inventoryList.value.filter(item => 
    item.minStock && item.quantity <= item.minStock
  ).length;
};

const handleSearch = () => {
  pagination.current = 1;
  loadInventoryData();
};

const handleReset = () => {
  Object.assign(filterForm, {
    productName: '',
    sku: '',
    stockStatus: ''
  });
  pagination.current = 1;
  loadInventoryData();
};

const handleExport = () => {
  ElMessage.info('导出功能开发中');
};

const handleSizeChange = (size) => {
  pagination.size = size;
  pagination.current = 1;
  loadInventoryData();
};

const handleCurrentChange = (page) => {
  pagination.current = page;
  loadInventoryData();
};

const getStockClass = (quantity, minStock) => {
  if (!minStock) return 'stock-normal';
  if (quantity === 0) return 'stock-out';
  if (quantity <= minStock) return 'stock-low';
  return 'stock-normal';
};

const getStockStatusType = (quantity, minStock) => {
  if (!minStock) return '';
  if (quantity === 0) return 'danger';
  if (quantity <= minStock) return 'warning';
  return 'success';
};

const getStockStatusText = (quantity, minStock) => {
  if (!minStock) return '正常';
  if (quantity === 0) return '缺货';
  if (quantity <= minStock) return '低库存';
  return '正常';
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

// 监听仓库ID变化
watch(() => props.warehouseId, () => {
  if (props.warehouseId) {
    loadInventoryData();
  }
});

onMounted(() => {
  if (props.warehouseId) {
    loadInventoryData();
  }
});
</script>

<style scoped>
.warehouse-inventory {
  max-height: 70vh;
  overflow-y: auto;
}

.inventory-stats {
  margin-bottom: 20px;
}

.stat-card {
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

.stat-icon.total {
  background-color: #409EFF;
}

.stat-icon.quantity {
  background-color: #67C23A;
}

.stat-icon.value {
  background-color: #E6A23C;
}

.stat-icon.warning {
  background-color: #F56C6C;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.filter-section {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
}

.inventory-table {
  width: 100%;
}

.product-info {
  line-height: 1.4;
}

.product-name {
  font-weight: 500;
  color: #303133;
}

.product-sku {
  font-size: 12px;
  color: #909399;
}

/* 库存状态样式 */
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

.pagination-section {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .stat-card {
    margin-bottom: 12px;
  }
  
  .filter-section .el-form-item {
    margin-bottom: 12px;
  }
}
</style>