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
          <el-form-item label="SKU">
            <el-input
              v-model="filterForm.sku"
              placeholder="请输入SKU"
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
                :label="category.categoryName"
                :value="category.id"
              />
            </el-select>
          </el-form-item>
          <!-- <el-form-item label="仓库">
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
          </el-form-item> -->
          <!-- <el-form-item label="库存状态">
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
          </el-form-item> -->
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
            <!-- <el-button @click="handleLowStockAlert">低库存预警</el-button> -->
          </el-form-item>
        </el-form>
      </div>

      <!-- 统计信息 -->
      <!-- <div class="stats-section">
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
      </div> -->

      <!-- 库存列表 -->
      <div class="inventory-list-section">
        <el-table
          :data="inventoryList"
          v-loading="loading"
          empty-text="暂无库存数据"
          class="inventory-table"
          row-key="productId"
          @sort-change="handleSortChange"
        >
          <el-table-column type="index" label="序号" width="60" align="center" />
          
          <!-- 产品信息 -->
          <el-table-column label="产品信息" width="300" fixed="left">
            <template #default="{ row }">
              <div class="product-info">
                <el-avatar :size="40" :src="row.productImage" class="product-image">
                  {{ row.productName?.charAt(0) || '产' }}
                </el-avatar>
                <div class="product-details">
                  <div class="product-name">{{ row.productName }}</div>
                  <div class="product-sku">SKU: {{ row.sku || '--' }}</div>
                  <div class="product-spec">
                    <span v-if="row.spec">规格: {{ row.spec }}</span>
                    <span v-if="row.color"> | 颜色: {{ row.color }}</span>
                  </div>
                  <div class="product-barcode" v-if="row.barcode">
                    条码: {{ row.barcode }}
                  </div>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="产品分类" width="120">
            <template #default="{ row }">
              <span>{{ row.categoryName }}</span>
            </template>
          </el-table-column>

          <el-table-column label="总库存" width="120" align="center">
            <template #default="{ row }">
              <span :class="getStockClass(row.remainingStockQuantityOfAllWarehouses)">
                {{ formatNumber(row.remainingStockQuantityOfAllWarehouses) }}
              </span>
            </template>
          </el-table-column>

          <!-- 总进货库存信息 -->
          <el-table-column label="总进货库存" width="120" align="center" sortable="custom" prop="totalInQuantityOfAllWarehouses">
            <template #default="{ row }">
              <span :class="getStockClass(row.totalInQuantityOfAllWarehouses)">
                {{ formatNumber(row.totalInQuantityOfAllWarehouses) }}
              </span>
            </template>
          </el-table-column>

          <el-table-column label="总出货数量" width="120" align="center" sortable="custom" prop="totalOutboundQuantityOfAllWarehouses">
            <template #default="{ row }">
              <span :class="getStockClass(row.totalOutboundQuantityOfAllWarehouses)">
                {{ formatNumber(row.totalOutboundQuantityOfAllWarehouses) }}
              </span>
            </template>
          </el-table-column>

          

          <!-- <el-table-column label="单位" width="80" align="center">
            <template #default="{ row }">
              <span>{{ row.unitName }}</span>
            </template>
          </el-table-column> -->

          <!-- 出货信息 -->
          <!-- <el-table-column label="出货单位" width="100" align="center">
            <template #default="{ row }">
              <span>{{ row.outUnitName || '--' }}</span>
            </template>
          </el-table-column> -->

          <!-- <el-table-column label="出货单位数量" width="100" align="center">
            <template #default="{ row }">
              <span>{{ row.outUnitPerNum || '--' }}</span>
            </template>
          </el-table-column> -->

          <!-- <el-table-column label="出货数量" width="120" align="center">
            <template #default="{ row }">
              <span>{{ formatNumber(row.outUnitTotalNum) }}</span>
            </template>
          </el-table-column> -->

          <!-- 价格信息 -->
          <!-- <el-table-column label="单价(¥)" width="120" align="right">
            <template #default="{ row }">
              <span class="amount">{{ formatCurrency(row.priceRmb) }}</span>
            </template>
          </el-table-column> -->

          <!-- <el-table-column label="总价(¥)" width="120" align="right" sortable="custom" prop="totalPriceRmb">
            <template #default="{ row }">
              <span class="amount">{{ formatCurrency(row.totalPriceRmb) }}</span>
            </template>
          </el-table-column> -->

          <!-- 物理信息 -->
          <!-- <el-table-column label="总体积" width="100" align="center">
            <template #default="{ row }">
              <span>{{ formatNumber(row.volume) }} m³</span>
            </template>
          </el-table-column> -->

          <!-- <el-table-column label="总重量" width="100" align="center">
            <template #default="{ row }">
              <span>{{ formatNumber(row.weightAll) }} kg</span>
            </template>
          </el-table-column> -->

          <!-- 仓库库存详情 -->
          <el-table-column label="仓库库存详情" width="200">
            <template #default="{ row }">
              <div class="warehouse-inventory-list">
                <div 
                  v-for="warehouse in row.warehouseInventoryList" 
                  :key="warehouse.warehouseId"
                  class="warehouse-item"
                >
                  <div class="warehouse-name">{{ warehouse.warehouseName }}:</div>
                  <div class="warehouse-quantity">
                    <span class="total">{{ formatNumber(warehouse.quantity) }}</span>
                    <span v-if="warehouse.lockedQuantity > 0" class="locked">
                      (锁:{{ formatNumber(warehouse.lockedQuantity) }})
                    </span>
                  </div>
                </div>
                <div v-if="!row.warehouseInventoryList || row.warehouseInventoryList.length === 0" class="no-warehouse">
                  无仓库库存
                </div>
              </div>
            </template>
          </el-table-column>

          <!-- 新增：货架库存详情 -->
          <el-table-column label="货架库存详情" width="220">
            <template #default="{ row }">
              <div class="shelf-inventory-list">
                <!-- 如果没有货架库存数据 -->
                <div v-if="!row.shelfInventoryList || row.shelfInventoryList.length === 0" class="no-shelf-data">
                  <span class="no-shelf-text">无货架库存数据</span>
                </div>
                
                <!-- 显示所有货架库存 -->
                <div 
                  v-for="shelf in row.shelfInventoryList" 
                  :key="shelf.shelfId"
                  class="shelf-item"
                >
                  <div class="shelf-info">
                    <span class="shelf-name">{{ shelf.shelfName || `货架${shelf.shelfId}` }}:</span>
                    <span class="shelf-quantity">{{ formatNumber(shelf.quantity) }}</span>
                  </div>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="库存状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag 
                :type="getStockStatusTagType(row.inventoryStatus)" 
                size="small"
              >
                {{ getStockStatusText(row.inventoryStatus) }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column label="操作" width="180" fixed="right" align="center">
            <template #default="{ row }">
              <div class="action-buttons">
                <!-- <el-button
                  type="primary"
                  link
                  size="small"
                  @click="handleViewDetail(row)"
                >
                  详情
                </el-button> -->
                <!-- <el-button
                  type="warning"
                  link
                  size="small"
                  @click="handleAdjustStock(row)"
                >
                  调整
                </el-button> -->
                <el-button
                  type="info"
                  link
                  size="small"
                  @click="handleViewHistory(row)"
                >
                  流水
                </el-button>
                <el-button
                  type="success"
                  link
                  size="small"
                  @click="handleWarehouseDetail(row)"
                >
                  仓库明细
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

    <!-- 仓库库存明细对话框 -->
    <el-dialog
      v-model="warehouseDetailVisible"
      :title="`仓库库存明细 - ${currentInventory?.productName || '未知产品'}`"
      width="60%"
    >
      <div v-if="currentInventory">
        <el-table :data="currentInventory.warehouseInventoryList" empty-text="无仓库库存数据">
          <el-table-column label="仓库名称" prop="warehouseName" width="150" />
          <el-table-column label="库存数量" align="center" width="120">
            <template #default="{ row }">
              <span :class="getStockClass(row.quantity)">{{ formatNumber(row.quantity) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="锁定数量" align="center" width="120">
            <template #default="{ row }">
              <span>{{ formatNumber(row.lockedQuantity) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="可用数量" align="center" width="120">
            <template #default="{ row }">
              <span :class="getStockClass(row.availableQuantity)">{{ formatNumber(row.availableQuantity) }}</span>
            </template>
          </el-table-column>
        </el-table>
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
const warehouseDetailVisible = ref(false);
const currentInventory = ref(null);

// 筛选表单
const filterForm = reactive({
  productName: '',
  sku: '',
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
    
    const res = await post('/api/auth/inventory/pageList', params);
    if (res && res.records) {
      inventoryList.value = res.records.map(inventory => ({
        ...inventory,
        // 确保数组字段不为空
        warehouseInventoryList: inventory.warehouseInventoryList || [],
        outboundQuantityList: inventory.outboundQuantityList || []
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
    const res = await get('/api/auth/product/categoryList');
    categoryList.value = res || [];
  } catch (error) {
    console.error('加载分类列表失败:', error);
    categoryList.value = [];
  }
};

const updateStats = () => {
  stats.totalProducts = inventoryList.value.length;
  stats.normalStock = inventoryList.value.filter(item => 
    item.remainingStockQuantityOfAllWarehouses > 10 // 可以根据业务需求调整阈值
  ).length;
  stats.lowStock = inventoryList.value.filter(item => 
    item.remainingStockQuantityOfAllWarehouses > 0 && item.totalQuaremainingStockQuantityOfAllWarehousesntityOfAllWarehouses <= 10
  ).length;
  stats.outOfStock = inventoryList.value.filter(item => 
    item.remainingStockQuantityOfAllWarehouses <= 0
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
    sku: '',
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

const handleWarehouseDetail = (inventory) => {
  currentInventory.value = inventory;
  warehouseDetailVisible.value = true;
};

const handleAdjustStock = (inventory) => {
  router.push(`/inventory/adjust/${inventory.productId}`);
};

const handleViewHistory = (inventory) => {
  console.log("查看历史记录", inventory.productId);
  router.push(`/index/ckInventoryHistory/${inventory.productId}`);
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

const getStockClass = (quantity) => {
  const num = Number(quantity) || 0;
  if (num <= 0) {
    return 'stock-out';
  } else if (num <= 10) { // 可以根据业务需求调整低库存阈值
    return 'stock-low';
  }
  return 'stock-normal';
};

const getStockStatusText = (quantity) => {
  const num = Number(quantity) || 0;
  if (num === 0) {
    return '缺货';
  } else if (num === 10) {
    return '低库存';
  } else {
    return '正常';
  }
};

const getStockStatusTagType = (quantity) => {
  const num = Number(quantity) || 0;
  if (num === 0) {
    return 'danger';
  } else if (num === 10) {
    return 'warning';
  } else {
    return 'success';
  }
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
  align-items: flex-start;
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

.product-sku, .product-spec, .product-barcode {
  font-size: 12px;
  color: #909399;
  margin-bottom: 2px;
  line-height: 1.4;
}

.warehouse-inventory-list {
  max-height: 120px;
  overflow-y: auto;
}

.warehouse-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4px 0;
  border-bottom: 1px solid #f0f0f0;
  font-size: 12px;
}

.warehouse-item:last-child {
  border-bottom: none;
}

.warehouse-name {
  color: #606266;
  flex-shrink: 0;
}

.warehouse-quantity {
  display: flex;
  align-items: center;
  gap: 4px;
}

.warehouse-quantity .total {
  font-weight: bold;
  color: #303133;
}

.warehouse-quantity .locked {
  color: #E6A23C;
  font-size: 11px;
}

.no-warehouse {
  color: #909399;
  font-style: italic;
  text-align: center;
  padding: 8px;
}

.amount {
  font-weight: bold;
  color: #E6A23C;
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
  
  .warehouse-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 2px;
  }
}

/* 动画效果 */
.inventory-table :deep(.el-table__row) {
  transition: all 0.3s;
}

.inventory-table :deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}

/* 货架库存列表样式 */
.shelf-inventory-list {
  max-height: 120px;
  overflow-y: auto;
  padding: 2px 0;
}

.shelf-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4px 0;
  border-bottom: 1px solid #f0f0f0;
  font-size: 12px;
  min-height: 24px;
}

.shelf-item:last-child {
  border-bottom: none;
}

.shelf-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.shelf-name {
  color: #606266;
  flex-shrink: 0;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.shelf-quantity {
  font-weight: bold;
  color: #409EFF;
  flex-shrink: 0;
}

.no-shelf-data {
  text-align: center;
  padding: 8px;
  color: #909399;
  font-style: italic;
}

.no-shelf-text {
  font-size: 12px;
}

/* 如果需要按仓库分组显示，可以使用以下样式 */
.shelf-warehouse-group {
  margin-bottom: 6px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 4px;
}

.warehouse-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 2px 4px;
  background-color: #f5f7fa;
  border-radius: 2px;
  margin-bottom: 4px;
  font-size: 11px;
}

.warehouse-header .warehouse-name {
  color: #409EFF;
  font-weight: bold;
}

.warehouse-header .warehouse-total {
  color: #E6A23C;
  font-size: 10px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .shelf-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 2px;
  }
  
  .shelf-info {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .shelf-quantity {
    margin-left: 8px;
  }
}
</style>