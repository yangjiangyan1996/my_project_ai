<template>
  <div class="inventory-list-container">
    <el-card class="manage-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">库存查询</span>
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
          <el-form-item label="产品分类">
            <el-select
              v-model="filterForm.categoryId"
              placeholder="全部分类"
              clearable
              style="width: 150px"
            >
              <el-option
                v-for="category in categoryList"
                :key="category.id"
                :label="category.name"
                :value="category.id"
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
          <el-form-item label="库存状态">
            <el-select
              v-model="filterForm.stockStatus"
              placeholder="全部状态"
              clearable
              style="width: 120px"
            >
              <el-option
                v-for="item in stockStatusOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
            <el-button @click="handleLowStockAlert">低库存预警</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 统计信息 -->
      <div class="stats-section">
        <el-row :gutter="20">
          <el-col :xs="12" :sm="6" :lg="3">
            <div class="stat-item total">
              <div class="stat-icon">
                <el-icon><Box /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ stats.totalProducts }}</div>
                <div class="stat-label">产品总数</div>
              </div>
            </div>
          </el-col>
          <el-col :xs="12" :sm="6" :lg="3">
            <div class="stat-item normal">
              <div class="stat-icon">
                <el-icon><CircleCheck /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ stats.normalStock }}</div>
                <div class="stat-label">正常库存</div>
              </div>
            </div>
          </el-col>
          <el-col :xs="12" :sm="6" :lg="3">
            <div class="stat-item low">
              <div class="stat-icon">
                <el-icon><Warning /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ stats.lowStock }}</div>
                <div class="stat-label">低库存</div>
              </div>
            </div>
          </el-col>
          <el-col :xs="12" :sm="6" :lg="3">
            <div class="stat-item out">
              <div class="stat-icon">
                <el-icon><CloseBold /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ stats.outOfStock }}</div>
                <div class="stat-label">缺货</div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 库存列表 -->
      <div class="inventory-list-section">
        <el-table
          :data="inventoryList"
          v-loading="loading"
          empty-text="暂无库存数据"
          class="inventory-table"
          row-key="id"
          @sort-change="handleSortChange"
        >
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column label="产品信息" width="280" fixed="left">
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
          <el-table-column label="产品分类" width="120">
            <template #default="{ row }">
              <span>{{ row.categoryName }}</span>
            </template>
          </el-table-column>
          <el-table-column label="仓库" width="120">
            <template #default="{ row }">
              <span>{{ row.warehouseName }}</span>
            </template>
          </el-table-column>
          <el-table-column label="当前库存" width="120" align="center" sortable="custom" prop="currentStock">
            <template #default="{ row }">
              <span :class="getStockClass(row.currentStock, row.minStock, row.maxStock)">
                {{ row.currentStock }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="可用库存" width="120" align="center" sortable="custom" prop="availableStock">
            <template #default="{ row }">
              <span>{{ row.availableStock }}</span>
            </template>
          </el-table-column>
          <el-table-column label="锁定库存" width="120" align="center">
            <template #default="{ row }">
              <span>{{ row.lockedStock || 0 }}</span>
            </template>
          </el-table-column>
          <el-table-column label="单位" width="80" align="center">
            <template #default="{ row }">
              <span>{{ row.unit }}</span>
            </template>
          </el-table-column>
          <el-table-column label="最低库存" width="120" align="center">
            <template #default="{ row }">
              <span>{{ row.minStock || 0 }}</span>
            </template>
          </el-table-column>
          <el-table-column label="最高库存" width="120" align="center">
            <template #default="{ row }">
              <span>{{ row.maxStock || '--' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="库存状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag 
                :type="getStockStatusTagType(row.currentStock, row.minStock)" 
                size="small"
              >
                {{ getStockStatusText(row.currentStock, row.minStock) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="库存金额" width="120" align="right" sortable="custom" prop="stockAmount">
            <template #default="{ row }">
              <span class="amount">¥{{ (row.stockAmount || 0).toFixed(2) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="最后更新" width="160">
            <template #default="{ row }">
              <span>{{ formatTime(row.updatedAt) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right" align="center">
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
                <el-button
                  type="warning"
                  link
                  size="small"
                  @click="handleAdjustStock(row)"
                >
                  调整
                </el-button>
                <el-button
                  type="info"
                  link
                  size="small"
                  @click="handleViewHistory(row)"
                >
                  流水
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

    <!-- 库存详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      :title="`库存详情 - ${currentInventory?.productName || '未知产品'}`"
      width="80%"
      top="5vh"
    >
      <div v-if="currentInventory">
        <InventoryDetail
          :inventory-data="currentInventory"
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
import { ElMessage, ElMessageBox } from 'element-plus';
import { Refresh, Download, Box, CircleCheck, Warning, CloseBold } from '@element-plus/icons-vue';
import { post, get } from '@/net';

const router = useRouter();
const loading = ref(false);
const detailDialogVisible = ref(false);
const currentInventory = ref(null);

// 筛选表单
const filterForm = reactive({
  productName: '',
  productCode: '',
  categoryId: '',
  warehouseId: '',
  stockStatus: ''
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

// 统计信息
const stats = reactive({
  totalProducts: 0,
  normalStock: 0,
  lowStock: 0,
  outOfStock: 0
});

// 库存列表
const inventoryList = ref([]);
const warehouseList = ref([]);
const categoryList = ref([]);

// 选项数据
const stockStatusOptions = [
  { value: 'normal', label: '正常' },
  { value: 'low', label: '低库存' },
  { value: 'out', label: '缺货' }
];

// 方法
const loadInventoryList = async () => {
  loading.value = true;
  try {
    const params = {
      page: pagination.current,
      size: pagination.size,
      ...filterForm,
      sortField: sortInfo.prop,
      sortOrder: sortInfo.order
    };
    
    const res = await post('/api/auth/inventory/list', params);
    if (res && res.records) {
      inventoryList.value = res.records.map(inventory => ({
        id: inventory.id || '',
        productId: inventory.productId || '',
        productName: inventory.productName || '',
        productCode: inventory.productCode || '',
        productSpec: inventory.productSpec || '',
        productImage: inventory.productImage || '/images/default-product.png',
        categoryId: inventory.categoryId || '',
        categoryName: inventory.categoryName || '',
        warehouseId: inventory.warehouseId || '',
        warehouseName: inventory.warehouseName || '',
        currentStock: inventory.currentStock || 0,
        availableStock: inventory.availableStock || 0,
        lockedStock: inventory.lockedStock || 0,
        unit: inventory.unit || '个',
        minStock: inventory.minStock || 0,
        maxStock: inventory.maxStock || null,
        stockAmount: inventory.stockAmount || 0,
        avgCost: inventory.avgCost || 0,
        lastInboundTime: inventory.lastInboundTime || '',
        lastOutboundTime: inventory.lastOutboundTime || '',
        createdAt: inventory.createdAt || new Date().toISOString(),
        updatedAt: inventory.updatedAt || new Date().toISOString()
      }));
      pagination.total = res.total || 0;
      
      // 更新统计信息
      updateStats();
    } else {
      inventoryList.value = [];
      pagination.total = 0;
    }
  } catch (error) {
    console.error('加载库存列表失败:', error);
    ElMessage.error('加载库存列表失败');
    inventoryList.value = [];
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

const loadCategoryList = async () => {
  try {
    const res = await get('/api/auth/category/list');
    categoryList.value = res.records || [];
  } catch (error) {
    console.error('加载分类列表失败:', error);
    categoryList.value = [];
  }
};

const updateStats = () => {
  stats.totalProducts = inventoryList.value.length;
  stats.normalStock = inventoryList.value.filter(item => 
    item.currentStock > (item.minStock || 0) * 1.2
  ).length;
  stats.lowStock = inventoryList.value.filter(item => 
    item.currentStock > 0 && item.currentStock <= (item.minStock || 0) * 1.2
  ).length;
  stats.outOfStock = inventoryList.value.filter(item => 
    item.currentStock <= 0
  ).length;
};

const refreshList = () => {
  pagination.current = 1;
  loadInventoryList();
};

const handleSearch = () => {
  pagination.current = 1;
  loadInventoryList();
};

const handleReset = () => {
  Object.assign(filterForm, {
    productName: '',
    productCode: '',
    categoryId: '',
    warehouseId: '',
    stockStatus: ''
  });
  pagination.current = 1;
  loadInventoryList();
};

const handleExport = () => {
  ElMessage.info('导出功能开发中');
};

const handleLowStockAlert = () => {
  filterForm.stockStatus = 'low';
  handleSearch();
};

const handleSizeChange = (size) => {
  pagination.size = size;
  pagination.current = 1;
  loadInventoryList();
};

const handleCurrentChange = (page) => {
  pagination.current = page;
  loadInventoryList();
};

const handleSortChange = (sort) => {
  sortInfo.prop = sort.prop;
  sortInfo.order = sort.order === 'ascending' ? 'asc' : 
                   sort.order === 'descending' ? 'desc' : '';
  loadInventoryList();
};

const handleViewDetail = (inventory) => {
  currentInventory.value = inventory;
  detailDialogVisible.value = true;
};

const handleAdjustStock = (inventory) => {
  router.push(`/inventory/adjust/${inventory.id}`);
};

const handleViewHistory = (inventory) => {
  router.push(`/inventory/history/${inventory.id}`);
};

const getStockClass = (currentStock, minStock, maxStock) => {
  if (currentStock <= 0) {
    return 'stock-out';
  } else if (currentStock <= (minStock || 0)) {
    return 'stock-low';
  } else if (maxStock && currentStock > maxStock) {
    return 'stock-over';
  }
  return 'stock-normal';
};

const getStockStatusText = (currentStock, minStock) => {
  if (currentStock <= 0) {
    return '缺货';
  } else if (currentStock <= (minStock || 0)) {
    return '低库存';
  } else {
    return '正常';
  }
};

const getStockStatusTagType = (currentStock, minStock) => {
  if (currentStock <= 0) {
    return 'danger';
  } else if (currentStock <= (minStock || 0)) {
    return 'warning';
  } else {
    return 'success';
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
  loadInventoryList();
  loadWarehouseList();
  loadCategoryList();
});
</script>

<style scoped>
.inventory-list-container {
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

.stat-item.normal .stat-icon {
  background-color: #67C23A;
}

.stat-item.low .stat-icon {
  background-color: #E6A23C;
}

.stat-item.out .stat-icon {
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

.inventory-list-section {
  margin-top: 20px;
}

.inventory-table {
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

.amount {
  font-weight: bold;
  color: #E6A23C;
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

.stock-over {
  color: #9b59b6;
  font-weight: bold;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .inventory-list-container {
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
}

/* 动画效果 */
.inventory-table :deep(.el-table__row) {
  transition: all 0.3s;
}

.inventory-table :deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}
</style>