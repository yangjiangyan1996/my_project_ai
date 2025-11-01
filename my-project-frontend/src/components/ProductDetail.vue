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
      <el-button type="primary" @click="handleEdit">编辑产品</el-button>
      <el-button @click="handlePrint">打印产品信息</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { post } from '@/net';

const props = defineProps({
  productData: {
    type: Object,
    required: true
  }
});

const emit = defineEmits(['close', 'edit']);

const warehouseInventory = ref([]);

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

.info-card, .unit-card, .inventory-card, .warehouse-card, .remark-card {
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

:deep(.el-descriptions) {
  margin-top: 0;
}
</style>