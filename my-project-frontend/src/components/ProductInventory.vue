<template>
  <div class="product-inventory">
    <!-- 产品基本信息 -->
    <el-card class="product-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>产品信息</span>
        </div>
      </template>
      <el-descriptions :column="3" size="small">
        <el-descriptions-item label="产品名称">{{ productData.name }}</el-descriptions-item>
        <el-descriptions-item label="SKU编码">{{ productData.sku }}</el-descriptions-item>
        <el-descriptions-item label="规格型号">{{ productData.spec }}</el-descriptions-item>
        <el-descriptions-item label="基础单位">{{ productData.unitName }}</el-descriptions-item>
        <el-descriptions-item label="最低库存">{{ productData.minStock || 0 }}</el-descriptions-item>
        <el-descriptions-item label="总库存">{{ totalInventory }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 筛选条件 -->
    <div class="filter-section">
      <el-form :model="filterForm" inline>
        <el-form-item label="仓库">
          <el-select
            v-model="filterForm.warehouseId"
            placeholder="全部仓库"
            clearable
            style="width: 200px"
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
        <el-table-column label="仓库信息" min-width="200">
          <template #default="{ row }">
            <div class="warehouse-info">
              <div class="warehouse-name">{{ row.warehouseName }}</div>
              <div class="warehouse-address" v-if="row.warehouseAddress">
                {{ row.warehouseAddress }}
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="库存数量" width="120" align="center">
          <template #default="{ row }">
            <span :class="getStockClass(row.quantity)">{{ row.quantity }}</span>
          </template>
        </el-table-column>
        <el-table-column label="锁定数量" width="120" align="center">
          <template #default="{ row }">
            <span class="locked-quantity">{{ row.lockedQuantity || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="可用数量" width="120" align="center">
          <template #default="{ row }">
            <span class="available-quantity">{{ row.availableQuantity || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="库存状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.quantity)" size="small">
              {{ getStatusText(row.quantity) }}
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
            <span class="amount">¥{{ ((row.avgCost || 0) * row.quantity).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="最后入库" width="160">
          <template #default="{ row }">
            <span>{{ formatTime(row.lastInboundTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="最后出库" width="160">
          <template #default="{ row }">
            <span>{{ formatTime(row.lastOutboundTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button
                type="primary"
                link
                size="small"
                @click="handleViewHistory(row)"
              >
                流水
              </el-button>
              <el-button
                type="warning"
                link
                size="small"
                @click="handleAdjustStock(row)"
              >
                调整
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

    <!-- 操作按钮 -->
    <div class="action-section">
      <el-button @click="$emit('close')">关闭</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { post, get } from '@/net';

const props = defineProps({
  productId: {
    type: String,
    required: true
  },
  productData: {
    type: Object,
    default: () => ({})
  }
});

const emit = defineEmits(['close']);

const loading = ref(false);
const warehouseList = ref([]);

// 筛选表单
const filterForm = reactive({
  warehouseId: '',
  stockStatus: ''
});

// 分页信息
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
});

// 库存列表
const inventoryList = ref([]);

// 计算属性
const totalInventory = computed(() => {
  return inventoryList.value.reduce((sum, item) => sum + item.quantity, 0);
});

// 方法
const loadInventoryData = async () => {
  loading.value = true;
  try {
    const params = {
      page: pagination.current,
      size: pagination.size,
      productId: props.productId,
      ...filterForm
    };
    
    const res = await post('/api/auth/inventory/listByProduct', params);
    if (res && res.records) {
      inventoryList.value = res.records;
      pagination.total = res.total || 0;
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

const loadWarehouseList = async () => {
  try {
    const res = await get('/api/auth/warehouse/list');
    warehouseList.value = res.records || [];
  } catch (error) {
    console.error('加载仓库列表失败:', error);
    warehouseList.value = [];
  }
};

const handleSearch = () => {
  pagination.current = 1;
  loadInventoryData();
};

const handleReset = () => {
  Object.assign(filterForm, {
    warehouseId: '',
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

const handleViewHistory = (inventory) => {
  ElMessage.info(`查看 ${inventory.warehouseName} 的库存流水`);
};

const handleAdjustStock = (inventory) => {
  ElMessage.info(`调整 ${inventory.warehouseName} 的库存`);
};

const getStockClass = (quantity) => {
  const minStock = props.productData.minStock || 0;
  if (quantity === 0) return 'stock-out';
  if (quantity <= minStock) return 'stock-low';
  return 'stock-normal';
};

const getStatusTagType = (quantity) => {
  const minStock = props.productData.minStock || 0;
  if (quantity === 0) return 'danger';
  if (quantity <= minStock) return 'warning';
  return 'success';
};

const getStatusText = (quantity) => {
  const minStock = props.productData.minStock || 0;
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

onMounted(() => {
  loadInventoryData();
  loadWarehouseList();
});
</script>

<style scoped>
.product-inventory {
  max-height: 80vh;
  overflow-y: auto;
}

.product-card {
  margin-bottom: 20px;
}

.card-header {
  font-weight: bold;
  color: #303133;
}

.filter-section {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
}

.inventory-table {
  width: 100%;
}

.warehouse-info {
  line-height: 1.4;
}

.warehouse-name {
  font-weight: 500;
  color: #303133;
}

.warehouse-address {
  font-size: 12px;
  color: #909399;
}

/* 库存状态样式 */
.stock-out {
  color: #F56C6C;
  font-weight: bold;
}

.stock-low {
  color: #E6A23C;
  font-weight: bold;
}

.stock-normal {
  color: #67C23A;
  font-weight: bold;
}

.locked-quantity {
  color: #909399;
}

.available-quantity {
  color: #409EFF;
  font-weight: bold;
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

.action-section {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

:deep(.el-descriptions) {
  margin-top: 0;
}
</style>