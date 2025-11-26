[file name]: ProductDetail.vue
[file content begin]
<template>
  <div class="product-detail">
    <el-alert
      title="产品详细信息"
      type="info"
      :closable="false"
      class="alert-message"
    />
    
    <!-- 基本信息 -->
    <el-card class="info-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>基本信息</span>
          <el-tag 
            :type="productData.status === 1 ? 'success' : 'danger'" 
            size="large"
          >
            {{ productData.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </div>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="产品ID">{{ productData.id }}</el-descriptions-item>
        <el-descriptions-item label="SKU编码">{{ productData.sku }}</el-descriptions-item>
        <el-descriptions-item label="产品名称">{{ productData.name }}</el-descriptions-item>
        <el-descriptions-item label="条形码">{{ productData.barcode || '--' }}</el-descriptions-item>
        <el-descriptions-item label="规格型号">{{ productData.spec }}</el-descriptions-item>
        <el-descriptions-item label="颜色">{{ productData.color || '--' }}</el-descriptions-item>
        <el-descriptions-item label="产品分类">{{ productData.categoryName || '--' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatTime(productData.createdAt) }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 单位信息 -->
    <el-card class="unit-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>单位信息</span>
        </div>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="基础单位">
          {{ productData.unitName }} ({{ productData.unitCode }})
        </el-descriptions-item>
        <el-descriptions-item label="出货单位">
          {{ productData.outUnitName ? `${productData.outUnitName} (${productData.outUnitCode})` : '--' }}
        </el-descriptions-item>
        <el-descriptions-item label="单位转换率" v-if="productData.outUnitName">
          {{ `1${productData.outUnitName} = ${productData.outUnitPerNum}${productData.unitName}` }}
        </el-descriptions-item>
        <el-descriptions-item label="单品重量">
          {{ productData.weightPerUnit ? `${productData.weightPerUnit} kg` : '--' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 库存信息 -->
    <el-card class="inventory-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>库存信息</span>
          <el-button type="primary" link @click="handleViewAllInventory">
            查看所有仓库库存
          </el-button>
        </div>
      </template>
      <el-row :gutter="20">
        <el-col :span="8">
          <div class="inventory-stat">
            <div class="stat-label">当前总库存</div>
            <div class="stat-value">{{ productData.currentStock || 0 }}</div>
            <div class="stat-unit">{{ productData.unitName }}</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="inventory-stat">
            <div class="stat-label">最低库存</div>
            <div class="stat-value" :class="getMinStockClass">
              {{ productData.minStock || 0 }}
            </div>
            <div class="stat-unit">{{ productData.unitName }}</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="inventory-stat">
            <div class="stat-label">库存状态</div>
            <div class="stat-value">
              <el-tag :type="getInventoryStatusType" size="large">
                {{ getInventoryStatusText }}
              </el-tag>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 仓库库存分布 -->
    <el-card class="warehouse-card" shadow="never" v-if="warehouseInventory.length > 0">
      <template #header>
        <div class="card-header">
          <span>仓库库存分布</span>
        </div>
      </template>
      <el-table
        :data="warehouseInventory"
        border
        size="small"
        empty-text="暂无库存数据"
      >
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="warehouseName" label="仓库名称" min-width="150" />
        <el-table-column prop="quantity" label="库存数量" width="120" align="center">
          <template #default="{ row }">
            <span :class="getStockLevelClass(row.quantity)">{{ row.quantity }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="lockedQuantity" label="锁定数量" width="120" align="center">
          <template #default="{ row }">
            <span>{{ row.lockedQuantity || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="availableQuantity" label="可用数量" width="120" align="center">
          <template #default="{ row }">
            <span class="available-quantity">{{ row.availableQuantity || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="库存状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getWarehouseStockType(row.quantity)" size="small">
              {{ getWarehouseStockText(row.quantity) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastUpdated" label="最后更新" width="160">
          <template #default="{ row }">
            <span>{{ formatTime(row.lastUpdated) }}</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 原料信息（BOM信息） -->
    <el-card class="bom-card" shadow="never" v-if="bomData && bomData.bomId">
      <template #header>
        <div class="card-header">
          <span>原料信息 (BOM)</span>
          <el-tag :type="bomData.status === 1 ? 'success' : 'danger'" size="small">
            {{ bomData.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </div>
      </template>

      <!-- BOM基本信息 -->
      <div class="bom-basic-info">
        <el-descriptions :column="3" border size="small">
          <el-descriptions-item label="BOM ID">{{ bomData.bomId || '--' }}</el-descriptions-item>
          <el-descriptions-item label="BOM编号">{{ bomData.bomCode || '--' }}</el-descriptions-item>
          <el-descriptions-item label="版本号">{{ bomData.version || '--' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="bomData.status === 1 ? 'success' : 'danger'" size="small">
              {{ bomData.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ bomData.remark || '--' }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 原料明细表格 -->
      <el-table
        :data="bomData.list"
        border
        size="small"
        empty-text="暂无原料数据"
        class="bom-table"
        v-loading="loadingBom"
      >
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column label="原料信息" min-width="200">
          <template #default="{ row }">
            <div class="component-info">
              <div class="component-name">{{ row.componentProductName }}</div>
              <div class="component-id">ID: {{ row.componentProductId }}</div>
              <div class="bom-detail-id">明细ID: {{ row.bomDetailId }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.type === 1 ? 'primary' : 'success'" size="small">
              {{ row.typeName || (row.type === 1 ? '主料' : '辅料') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="用量信息" width="200" align="center">
          <template #default="{ row }">
            <div class="quantity-info">
              <div class="quantity-item">
                <span class="label">原料数量：</span>
                <span class="value">{{ formatNumber(row.quantity) }}</span>
              </div>
              <div class="quantity-item" v-if="row.quantityBeforeLoss !== null && row.quantityBeforeLoss !== undefined">
                <span class="label">净用量：</span>
                <span class="value">{{ formatNumber(row.quantityBeforeLoss) }}</span>
              </div>
              <div class="quantity-item" v-if="row.lossRate !== null && row.lossRate !== undefined">
                <span class="label">损耗率：</span>
                <span class="value loss-rate">{{ formatNumber(row.lossRate) }}%</span>
              </div>
              <div class="quantity-item" v-if="row.otherQuantity !== null && row.otherQuantity !== undefined">
                <span class="label">其他数量：</span>
                <span class="value">{{ formatNumber(row.otherQuantity) }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="排序" width="80" align="center">
          <template #default="{ row }">
            <span>{{ row.sortOrder || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="备注" min-width="150">
          <template #default="{ row }">
            <div class="component-remark">
              {{ row.remark || '--' }}
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 统计信息 -->
      <div class="bom-summary" v-if="bomData.list && bomData.list.length > 0">
        <el-row :gutter="20">
          <el-col :span="5">
            <div class="summary-item">
              <span class="label">原料总数：</span>
              <span class="value">{{ bomData.list.length }} 种</span>
            </div>
          </el-col>
          <el-col :span="5">
            <div class="summary-item">
              <span class="label">主料：</span>
              <span class="value">{{ mainMaterialsCount }} 种</span>
            </div>
          </el-col>
          <el-col :span="5">
            <div class="summary-item">
              <span class="label">辅料：</span>
              <span class="value">{{ auxiliaryMaterialsCount }} 种</span>
            </div>
          </el-col>
          <el-col :span="5">
            <div class="summary-item">
              <span class="label">有损耗：</span>
              <span class="value">{{ hasLossRateCount }} 种</span>
            </div>
          </el-col>
          <!-- <el-col :span="4">
            <div class="summary-item">
              <span class="label">有净用量：</span>
              <span class="value">{{ hasQuantityBeforeLossCount }} 种</span>
            </div>
          </el-col>
          <el-col :span="4">
            <div class="summary-item">
              <span class="label">有其他数量：</span>
              <span class="value">{{ hasOtherQuantityCount }} 种</span>
            </div>
          </el-col> -->
        </el-row>
      </div>
    </el-card>

    <!-- 无BOM数据提示 -->
    <el-card class="bom-card" shadow="never" v-else-if="!loadingBom">
      <template #header>
        <div class="card-header">
          <span>原料信息 (BOM)</span>
        </div>
      </template>
      <div class="no-bom-data">
        <el-empty description="暂无BOM数据" />
      </div>
    </el-card>

    <!-- 备注信息 -->
    <el-card class="remark-card" shadow="never" v-if="productData.remark">
      <template #header>
        <div class="card-header">
          <span>备注信息</span>
        </div>
      </template>
      <div class="remark-content">
        {{ productData.remark }}
      </div>
    </el-card>

    <!-- 操作按钮 -->
    <div class="action-section">
      <el-button @click="$emit('close')">关闭</el-button>
      <!-- <el-button type="primary" @click="handleEdit">编辑产品</el-button> -->
      <el-button @click="handlePrint">打印产品信息</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { post, get } from '@/net';

const props = defineProps({
  productData: {
    type: Object,
    required: true
  }
});

const emit = defineEmits(['close', 'edit']);

const warehouseInventory = ref([]);
const bomData = ref({}); // BOM数据改为对象
const loadingBom = ref(false); // BOM加载状态

// 计算属性
const getMinStockClass = computed(() => {
  return props.productData.minStock > 0 ? 'has-min-stock' : '';
});

const getInventoryStatusType = computed(() => {
  const currentStock = props.productData.currentStock || 0;
  const minStock = props.productData.minStock || 0;
  
  if (currentStock === 0) return 'danger';
  if (currentStock <= minStock) return 'warning';
  return 'success';
});

const getInventoryStatusText = computed(() => {
  const currentStock = props.productData.currentStock || 0;
  const minStock = props.productData.minStock || 0;
  
  if (currentStock === 0) return '缺货';
  if (currentStock <= minStock) return '低库存';
  return '库存充足';
});

// BOM相关计算属性
const mainMaterialsCount = computed(() => {
  return bomData.value.list ? bomData.value.list.filter(item => item.type === 1).length : 0;
});

const auxiliaryMaterialsCount = computed(() => {
  return bomData.value.list ? bomData.value.list.filter(item => item.type === 2).length : 0;
});

const hasLossRateCount = computed(() => {
  return bomData.value.list ? bomData.value.list.filter(item => 
    item.lossRate !== null && item.lossRate !== undefined && item.lossRate > 0
  ).length : 0;
});

const hasQuantityBeforeLossCount = computed(() => {
  return bomData.value.list ? bomData.value.list.filter(item => 
    item.quantityBeforeLoss !== null && item.quantityBeforeLoss !== undefined && item.quantityBeforeLoss > 0
  ).length : 0;
});

const hasOtherQuantityCount = computed(() => {
  return bomData.value.list ? bomData.value.list.filter(item => 
    item.otherQuantity !== null && item.otherQuantity !== undefined && item.otherQuantity > 0
  ).length : 0;
});

// 方法
const loadWarehouseInventory = async () => {
  try {
    const res = await post('/api/auth/inventory/listByProduct', {
      productId: props.productData.id
    });
    warehouseInventory.value = res.records || [];
  } catch (error) {
    console.error('加载仓库库存失败:', error);
    warehouseInventory.value = [];
  }
};

// 加载BOM信息
const loadBomData = async () => {
  if (!props.productData.id) return;
  
  loadingBom.value = true;
  try {
    const res = await get(`/api/auth/product/bomDetailWholeInfo?productId=${props.productData.id}`);
    console.log('BOM数据响应:', res);
    
    if (res && res.bomId) {
      bomData.value = res;
    } else {
      bomData.value = {};
    }
  } catch (error) {
    console.error('加载BOM数据失败:', error);
    bomData.value = {};
  } finally {
    loadingBom.value = false;
  }
};

const getStockLevelClass = (quantity) => {
  const minStock = props.productData.minStock || 0;
  if (quantity === 0) return 'stock-out';
  if (quantity <= minStock) return 'stock-low';
  return 'stock-normal';
};

const getWarehouseStockType = (quantity) => {
  const minStock = props.productData.minStock || 0;
  if (quantity === 0) return 'danger';
  if (quantity <= minStock) return 'warning';
  return 'success';
};

const getWarehouseStockText = (quantity) => {
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

const formatNumber = (value) => {
  if (value === null || value === undefined) return '--';
  // 如果是数字，保留2位小数
  const num = Number(value);
  return isNaN(num) ? value : num.toFixed(2);
};

const padZero = (num) => {
  return num < 10 ? `0${num}` : num;
};

const handleEdit = () => {
  emit('edit');
  emit('close');
};

const handleViewAllInventory = () => {
  ElMessage.info('查看所有仓库库存功能开发中');
};

const handlePrint = () => {
  ElMessage.info('打印功能开发中');
};

onMounted(() => {
  loadWarehouseInventory();
  loadBomData(); // 加载BOM数据
});
</script>

<style scoped>
.product-detail {
  max-height: 80vh;
  overflow-y: auto;
  padding-right: 10px;
}

.alert-message {
  margin-bottom: 20px;
}

.info-card, .unit-card, .inventory-card, .warehouse-card, .bom-card, .remark-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.inventory-stat {
  text-align: center;
  padding: 20px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  background: #f8f9fa;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
}

.has-min-stock {
  color: #E6A23C;
}

.stat-unit {
  font-size: 12px;
  color: #909399;
}

.remark-content {
  line-height: 1.6;
  color: #606266;
  padding: 10px;
  background: #f8f9fa;
  border-radius: 4px;
}

.action-section {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
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

.available-quantity {
  color: #409EFF;
  font-weight: bold;
}

/* BOM相关样式 */
.bom-basic-info {
  margin-bottom: 16px;
}

.bom-table {
  margin-bottom: 16px;
}

.component-info {
  line-height: 1.4;
}

.component-name {
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.component-id, .bom-detail-id {
  font-size: 12px;
  color: #909399;
}

.quantity-info {
  line-height: 1.6;
}

.quantity-item {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  margin-bottom: 2px;
}

.quantity-item:last-child {
  margin-bottom: 0;
}

.quantity-item .label {
  color: #606266;
}

.quantity-item .value {
  color: #303133;
  font-weight: 500;
}

.loss-rate {
  color: #E6A23C;
}

.component-remark {
  font-size: 12px;
  color: #606266;
  line-height: 1.4;
}

.bom-summary {
  padding: 12px;
  background-color: #f8f9fa;
  border-radius: 4px;
  border: 1px solid #ebeef5;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.summary-item .label {
  color: #606266;
  font-size: 14px;
}

.summary-item .value {
  color: #303133;
  font-weight: bold;
  font-size: 14px;
}

.no-bom-data {
  padding: 40px 0;
  text-align: center;
}

:deep(.el-descriptions) {
  margin-top: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .bom-summary .el-col {
    margin-bottom: 8px;
  }
  
  .summary-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
  
  .quantity-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 2px;
  }
}
</style>
[file content end]