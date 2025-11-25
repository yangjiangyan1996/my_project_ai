<!-- CkInboundProduction.vue -->
<template>
  <div class="inbound-create-container">
    <el-card class="form-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">{{ isEditMode ? '编辑生产入库单' : '新建生产入库单' }}</span>
          <div class="header-actions">
            <el-button @click="handleReset">重置</el-button>
            <el-button 
              type="primary" 
              @click="handleSaveDraft" 
              :loading="loading"
              v-if="!isEditMode || (isEditMode && formData.status === 0)"
            >
              保存草稿
            </el-button>
            <el-button 
              type="primary" 
              @click="handleSubmit" 
              :loading="loading"
              v-if="!isEditMode || (isEditMode && (formData.status === 0 || formData.status === 4))"
            >
              {{ isEditMode ? '更新提交' : '提交审核' }}
            </el-button>
          </div>
        </div>
      </template>

      <!-- 数据来源选择 -->
      <div class="data-source-section">
        <div class="section-header">
          <h3>数据来源</h3>
        </div>
        <div class="source-options">
          <el-radio-group v-model="dataSource" @change="handleDataSourceChange">
            <el-radio label="manual">手动创建</el-radio>
            <el-radio label="picking">从生产领料单导入</el-radio>
          </el-radio-group>
        </div>

        <!-- 领料单选择 -->
        <div class="picking-selector" v-if="dataSource === 'picking'">
          <el-select
            v-model="selectedPickingOrderId"
            placeholder="请选择生产领料单"
            filterable
            style="width: 100%"
            @change="handlePickingOrderChange"
          >
            <el-option
              v-for="order in availablePickingOrders"
              :key="order.id"
              :label="`${order.orderNo} - ${order.mainProductName || '多产品'} (${order.remark})`"
              :value="order.id"
            />
          </el-select>
          
          <!-- 领料单信息预览 -->
          <div class="picking-preview" v-if="selectedPickingOrder">
            <el-descriptions :column="2" border size="small">
              <el-descriptions-item label="领料单号">{{ selectedPickingOrder.orderNo }}</el-descriptions-item>
              <el-descriptions-item label="领料仓库">{{ selectedPickingOrder.warehouseName }}</el-descriptions-item>
              <el-descriptions-item label="领料日期">{{ selectedPickingOrder.expectedDate }}</el-descriptions-item>
              <el-descriptions-item label="备注">{{ selectedPickingOrder.remark }}</el-descriptions-item>
            </el-descriptions>
            
            <div class="picking-products">
              <h4>领料产品明细</h4>
              <el-table :data="selectedPickingOrder.items" size="small" border>
                <el-table-column prop="productName" label="产品名称" min-width="120" />
                <el-table-column prop="quantity" label="领料数量" width="100" align="center" />
                <el-table-column prop="unit" label="单位" width="80" align="center" />
              </el-table>
            </div>
            
            <div class="import-actions">
              <el-button type="primary" @click="importPickingData" :loading="importing">
                导入到入库单
              </el-button>
            </div>
          </div>

          <!-- 已导入的领料单列表 -->
          <div class="imported-picking-orders" v-if="importedPickingOrders.length > 0">
            <h4>已导入的领料单</h4>
            <div class="imported-list">
              <el-tag
                v-for="order in importedPickingOrders"
                :key="order.id"
                closable
                @close="removeImportedPickingOrder(order.id)"
                type="success"
                style="margin-right: 8px; margin-bottom: 8px;"
              >
                {{ order.orderNo }}
              </el-tag>
            </div>
          </div>
        </div>
      </div>

      <!-- 基本信息 -->
      <div class="basic-info-section">
        <div class="section-header">
          <h3>基本信息</h3>
        </div>
        <el-form
          ref="formRef"
          :model="formData"
          :rules="formRules"
          label-width="120px"
          class="inbound-form"
        >
          <el-row :gutter="24">
            <el-col :xs="24" :sm="12" :lg="8">
              <el-form-item label="入库单号" prop="orderNo">
                <el-input v-model="formData.orderNo" placeholder="系统自动生成" disabled />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="8">
              <el-form-item label="入库类型">
                <el-input value="生产入库" disabled />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="8">
              <el-form-item label="入库仓库" prop="warehouseId">
                <el-select
                  v-model="formData.warehouseId"
                  placeholder="请选择仓库"
                  style="width: 100%"
                  filterable
                  @change="handleWarehouseChange"
                >
                  <el-option
                    v-for="warehouse in warehouseList"
                    :key="warehouse.id"
                    :label="warehouse.name"
                    :value="warehouse.id"
                  />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="24">
            <el-col :xs="24" :sm="12" :lg="8">
              <el-form-item label="预计入库日期" prop="expectedDate">
                <el-date-picker
                  v-model="formData.expectedDate"
                  type="date"
                  placeholder="选择日期"
                  style="width: 100%"
                  value-format="YYYY-MM-DD"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="备注">
            <el-input
              v-model="formData.remark"
              type="textarea"
              :rows="2"
              placeholder="请输入备注信息"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>
        </el-form>
      </div>

      <!-- 产品明细 -->
      <div class="product-section">
        <div class="section-header">
          <h3>产品明细</h3>
          <div class="product-actions">
            <el-button type="primary" @click="handleAddProduct" :icon="Plus" :disabled="!formData.warehouseId">
              添加产品
            </el-button>
            <el-button @click="clearAllProducts" :disabled="formData.items.length === 0">
              清空所有
            </el-button>
          </div>
        </div>

        <div class="product-table-container">
          <el-table
            :data="formData.items"
            border
            class="product-table"
            empty-text="请添加产品明细"
          >
            <el-table-column type="index" label="序号" width="60" align="center" />
            <el-table-column label="产品信息" min-width="200">
              <template #default="{ row, $index }">
                <el-select
                  v-model="row.productId"
                  placeholder="选择产品"
                  style="width: 100%"
                  filterable
                  @change="(value) => handleProductChange(value, $index)"
                >
                  <el-option
                    v-for="product in productList"
                    :key="product.id"
                    :label="`${product.sku} - ${product.name}`"
                    :value="product.id"
                  />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="规格型号" width="120">
              <template #default="{ row }">
                <span>{{ row.spec || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="单位" width="80" align="center">
              <template #default="{ row }">
                <span>{{ row.unit || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="入库数量" width="120">
              <template #default="{ row, $index }">
                <el-input-number
                  v-model="row.actualQuantity"
                  :min="1"
                  :precision="0"
                  controls-position="right"
                  style="width: 100%"
                />
              </template>
            </el-table-column>
            <el-table-column label="关联领料单" width="150">
              <template #default="{ row }">
                <span>{{ row.relatedPickingOrderNo || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="批次号" width="150">
              <template #default="{ row, $index }">
                <el-input
                  v-model="row.batchNo"
                  placeholder="输入批次号"
                  @blur="() => validateBatchNo(row.batchNo, $index)"
                />
              </template>
            </el-table-column> 
            <el-table-column label="货架位置" width="180">
              <template #default="{ row, $index }">
                <el-select
                  v-model="row.shelfLocationIds"
                  placeholder="选择位置"
                  style="width: 100%"
                  filterable
                  multiple
                  collapse-tags
                  collapse-tags-tooltip
                  :max-collapse-tags="1"
                >
                  <el-option
                    v-for="location in shelfLocationList"
                    :key="location.id"
                    :label="getShelfLocationLabel(location)"
                    :value="location.id"
                  />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="货架分配" width="120" align="center">
              <template #default="{ row, $index }">
                <el-button 
                  type="primary" 
                  link 
                  @click="openShelfAllocationDialog($index)"
                  :disabled="!row.shelfLocationIds || row.shelfLocationIds.length === 0"
                >
                  {{ getShelfAllocationStatus(row) }}
                </el-button>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80" fixed="right" align="center">
              <template #default="{ $index }">
                <el-button
                  type="danger"
                  link
                  :icon="Delete"
                  @click="handleRemoveProduct($index)"
                />
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 统计信息 -->
        <div class="summary-info" v-if="formData.items.length > 0">
          <el-row :gutter="20">
            <el-col :span="6">
              <div class="summary-item">
                <span class="label">产品种类：</span>
                <span class="value">{{ formData.items.length }} 种</span>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="summary-item">
                <span class="label">入库总数：</span>
                <span class="value">{{ totalActualQuantity }}</span>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="summary-item">
                <span class="label">关联领料单：</span>
                <span class="value">{{ importedPickingOrders.length }} 个</span>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="summary-item">
                <span class="label">分配状态：</span>
                <span class="value" :class="allocationStatusClass">
                  {{ allocationStatusText }}
                </span>
              </div>
            </el-col>
          </el-row>
        </div>
      </div>
    </el-card>

    <!-- 货架分配对话框 -->
    <el-dialog
      v-model="shelfAllocationDialog.visible"
      :title="`货架数量分配 - ${shelfAllocationDialog.productName}`"
      width="500px"
      destroy-on-close
    >
      <div class="shelf-allocation-dialog-content">
        <div class="allocation-info">
          <div class="info-item">
            <span class="label">产品：</span>
            <span class="value">{{ shelfAllocationDialog.productName }}</span>
          </div>
          <div class="info-item">
            <span class="label">入库数量：</span>
            <span class="value">{{ shelfAllocationDialog.totalQuantity }}</span>
          </div>
          <div class="info-item">
            <span class="label">已分配：</span>
            <span class="value" :class="shelfAllocationDialog.allocatedQuantity >= shelfAllocationDialog.totalQuantity ? 'success' : 'warning'">
              {{ shelfAllocationDialog.allocatedQuantity }}
            </span>
          </div>
          <div class="info-item">
            <span class="label">剩余：</span>
            <span class="value">{{ shelfAllocationDialog.remainingQuantity }}</span>
          </div>
        </div>

        <el-divider />

        <div class="shelf-allocation-list">
          <div 
            v-for="(shelf, index) in shelfAllocationDialog.shelves" 
            :key="shelf.id"
            class="shelf-allocation-item"
          >
            <div class="shelf-info">
              <span class="shelf-name">{{ getShelfLocationLabel(shelf) }}</span>
            </div>
            <div class="allocation-control">
              <el-input-number
                v-model="shelf.allocatedQuantity"
                :min="0"
                :max="getMaxShelfAllocation(index)"
                :precision="0"
                controls-position="right"
                size="small"
                placeholder="分配数量"
                style="width: 120px"
                @change="(value) => handleShelfAllocationChange(value, index)"
              />
            </div>
          </div>
        </div>

        <div class="allocation-actions">
          <el-button @click="autoAllocateShelves" :disabled="shelfAllocationDialog.remainingQuantity <= 0">
            自动分配
          </el-button>
          <el-button type="primary" @click="confirmShelfAllocation">
            确认分配
          </el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Delete } from '@element-plus/icons-vue';
import { post, get } from '@/net';

const router = useRouter();
const route = useRoute();
const formRef = ref();
const loading = ref(false);
const importing = ref(false);

// 数据来源选择
const dataSource = ref('manual');
const selectedPickingOrderId = ref('');
const selectedPickingOrder = ref(null);
const availablePickingOrders = ref([]);
const importedPickingOrders = ref([]); // 已导入的领料单列表

// 判断是否是编辑模式
const isEditMode = computed(() => {
  return !!route.params.id;
});

// 表单数据
const formData = reactive({
  id: null,
  orderNo: '',
  orderType: 2,
  warehouseId: null,
  expectedDate: '',
  remark: '',
  status: 0,
  items: [],
});

// 货架分配对话框数据
const shelfAllocationDialog = reactive({
  visible: false,
  productIndex: -1,
  productName: '',
  totalQuantity: 0,
  shelves: [],
  allocatedQuantity: 0,
  remainingQuantity: 0
});

// 选项数据
const warehouseList = ref([]);
const productList = ref([]);
const shelfLocationList = ref([]);

// 计算属性
const totalActualQuantity = computed(() => {
  return formData.items.reduce((sum, item) => sum + (parseInt(item.actualQuantity) || 0), 0);
});

const allocationStatusText = computed(() => {
  const unallocatedItems = formData.items.filter(item => {
    if (!item.shelfLocationIds || item.shelfLocationIds.length === 0) return true;
    const allocated = item.shelfAllocations?.reduce((sum, alloc) => sum + (parseInt(alloc.quantity) || 0), 0) || 0;
    return allocated !== (parseInt(item.actualQuantity) || 0);
  });
  return unallocatedItems.length === 0 ? '已完成' : `${unallocatedItems.length}项待分配`;
});

const allocationStatusClass = computed(() => {
  const unallocatedItems = formData.items.filter(item => {
    if (!item.shelfLocationIds || item.shelfLocationIds.length === 0) return true;
    const allocated = item.shelfAllocations?.reduce((sum, alloc) => sum + (parseInt(alloc.quantity) || 0), 0) || 0;
    return allocated !== (parseInt(item.actualQuantity) || 0);
  });
  return unallocatedItems.length === 0 ? 'status-success' : 'status-warning';
});

// 表单验证规则
const formRules = {
  warehouseId: [
    { required: true, message: '请选择入库仓库', trigger: 'change' }
  ],
  expectedDate: [
    { required: true, message: '请选择预计入库日期', trigger: 'change' }
  ]
};

// 数据来源变更处理
const handleDataSourceChange = (value) => {
  if (value === 'picking') {
    loadAvailablePickingOrders();
  } else {
    selectedPickingOrderId.value = '';
    selectedPickingOrder.value = null;
  }
};

// 加载可用的生产领料单
const loadAvailablePickingOrders = async () => {
  try {
    const res = await get('/api/auth/outbound/listCompletedOutBoundProduction?orderType=2');
    console.log('加载生产领料单成功:', res);
    availablePickingOrders.value =  res || [];
  } catch (error) {
    console.error('加载生产领料单失败:', error);
    availablePickingOrders.value = [];
  }
};

// 选择领料单
const handlePickingOrderChange = async (orderId) => {
  if (!orderId) {
    selectedPickingOrder.value = null;
    return;
  }

  try {
    const res = await get(`/api/auth/outbound/detailNew?orderId=${orderId}`);
    if (res) {
      selectedPickingOrder.value = res.data || res;
    }
  } catch (error) {
    console.error('加载领料单详情失败:', error);
    ElMessage.error('加载领料单详情失败');
  }
};

// 导入领料数据
const importPickingData = async () => {
  if (!selectedPickingOrder.value) {
    ElMessage.warning('请先选择生产领料单');
    return;
  }

  // 检查是否已导入过该领料单
  if (importedPickingOrders.value.some(order => order.id === selectedPickingOrder.value.id)) {
    ElMessage.warning('该领料单已导入');
    return;
  }

  importing.value = true;
  try {
    const pickingOrder = selectedPickingOrder.value;
    
    // 设置基本信息（如果是第一次导入）
    if (!formData.warehouseId && pickingOrder.warehouseId) {
      formData.warehouseId = pickingOrder.warehouseId;
      await loadShelfLocationList(pickingOrder.warehouseId);
    }
    
    // 导入产品数据 - 基于BOM反向推导产成品
    const finishedProducts = deriveFinishedProducts(pickingOrder.items);
    const newItems = finishedProducts.map(product => ({
      productId: product.id,
      productName: product.name,
      sku: product.sku,
      spec: product.spec,
      unit: product.unit,
      actualQuantity: product.quantity,
      relatedPickingOrderNo: pickingOrder.orderNo, // 关联单号放到产品明细
      shelfLocationIds: [],
      shelfAllocations: [],
      batchNo: generateBatchNo(),
      remark: ''
    }));
    
    // 添加到现有产品列表
    formData.items.push(...newItems);
    
    // 添加到已导入领料单列表
    importedPickingOrders.value.push({
      id: pickingOrder.id,
      orderNo: pickingOrder.orderNo
    });
    
    ElMessage.success(`成功导入 ${newItems.length} 个产品`);
    
    // 清空选择，允许继续导入其他领料单
    selectedPickingOrderId.value = '';
    selectedPickingOrder.value = null;
    
  } catch (error) {
    console.error('导入数据失败:', error);
    ElMessage.error('导入数据失败');
  } finally {
    importing.value = false;
  }
};

// 移除已导入的领料单
const removeImportedPickingOrder = (orderId) => {
  const orderIndex = importedPickingOrders.value.findIndex(order => order.id === orderId);
  if (orderIndex > -1) {
    const orderNo = importedPickingOrders.value[orderIndex].orderNo;
    
    // 移除该领料单对应的产品
    formData.items = formData.items.filter(item => item.relatedPickingOrderNo !== orderNo);
    
    // 从已导入列表中移除
    importedPickingOrders.value.splice(orderIndex, 1);
    
    ElMessage.success(`已移除领料单 ${orderNo} 及其相关产品`);
  }
};

// 根据领料数据推导产成品（简化逻辑）
const deriveFinishedProducts = (pickingItems) => {
  // 这里需要根据您的BOM逻辑来推导
  // 简化示例：假设领料单中的产品就是产成品
  return pickingItems.map(item => ({
    id: item.productId,
    name: item.productName,
    sku: item.sku,
    spec: item.spec,
    unit: item.unit,
    quantity: Math.floor(item.quantity) // 简化计算
  }));
};

// 生成批次号
const generateBatchNo = () => {
  const date = new Date();
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const random = Math.random().toString(36).substr(2, 6).toUpperCase();
  return `B${year}${month}${day}${random}`;
};

// 获取货架分配状态文本
const getShelfAllocationStatus = (row) => {
  if (!row.shelfLocationIds || row.shelfLocationIds.length === 0) {
    return '未选择货架';
  }
  
  const allocated = row.shelfAllocations?.reduce((sum, alloc) => sum + (parseInt(alloc.quantity) || 0), 0) || 0;
  const total = parseInt(row.actualQuantity) || 0;
  
  if (allocated === 0) return '未分配';
  if (allocated < total) return `部分分配 (${allocated}/${total})`;
  if (allocated === total) return '已分配';
  return '超额分配';
};

// 货架分配相关方法（保持原有逻辑）
const openShelfAllocationDialog = (index) => {
  const item = formData.items[index];
  shelfAllocationDialog.productIndex = index;
  shelfAllocationDialog.productName = item.productName;
  shelfAllocationDialog.totalQuantity = parseInt(item.actualQuantity) || 1;
  
  shelfAllocationDialog.shelves = item.shelfLocationIds.map(shelfId => {
    const location = shelfLocationList.value.find(loc => loc.id === shelfId);
    const existingAllocation = item.shelfAllocations?.find(
      allocation => allocation.shelfLocationId === shelfId
    );
    
    return {
      id: shelfId,
      shelfName: location ? location.shelfName : '未知货架',
      shelfCode: location ? location.shelfCode : '',
      allocatedQuantity: existingAllocation ? parseInt(existingAllocation.quantity) : 0
    };
  });

  updateShelfAllocationCalculations();
  shelfAllocationDialog.visible = true;
};

const handleShelfAllocationChange = (value, changedIndex) => {
  const changedShelf = shelfAllocationDialog.shelves[changedIndex];
  const newValue = parseInt(value) || 0;
  
  const otherShelvesTotal = shelfAllocationDialog.shelves.reduce((sum, shelf, index) => {
    if (index !== changedIndex) return sum + (parseInt(shelf.allocatedQuantity) || 0);
    return sum;
  }, 0);
  
  const newTotal = otherShelvesTotal + newValue;
  
  if (newTotal > shelfAllocationDialog.totalQuantity) {
    const excess = newTotal - shelfAllocationDialog.totalQuantity;
    let remainingExcess = excess;
    
    for (let i = 0; i < shelfAllocationDialog.shelves.length; i++) {
      if (i !== changedIndex && remainingExcess > 0) {
        const shelf = shelfAllocationDialog.shelves[i];
        const currentValue = parseInt(shelf.allocatedQuantity) || 0;
        const deduction = Math.min(currentValue, remainingExcess);
        shelf.allocatedQuantity = currentValue - deduction;
        remainingExcess -= deduction;
      }
    }
    
    if (remainingExcess > 0) {
      changedShelf.allocatedQuantity = Math.max(0, shelfAllocationDialog.totalQuantity - otherShelvesTotal);
      ElMessage.warning(`分配数量不能超过总入库数量 ${shelfAllocationDialog.totalQuantity}`);
    }
  }
  
  changedShelf.allocatedQuantity = newValue;
  updateShelfAllocationCalculations();
};

const updateShelfAllocationCalculations = () => {
  shelfAllocationDialog.allocatedQuantity = shelfAllocationDialog.shelves.reduce(
    (sum, shelf) => sum + (parseInt(shelf.allocatedQuantity) || 0), 0
  );
  shelfAllocationDialog.remainingQuantity = Math.max(0, shelfAllocationDialog.totalQuantity - shelfAllocationDialog.allocatedQuantity);
};

const getMaxShelfAllocation = (index) => {
  const otherShelvesTotal = shelfAllocationDialog.shelves.reduce((sum, shelf, i) => {
    if (i !== index) return sum + (parseInt(shelf.allocatedQuantity) || 0);
    return sum;
  }, 0);
  return Math.max(0, shelfAllocationDialog.totalQuantity - otherShelvesTotal);
};

const autoAllocateShelves = () => {
  const totalQuantity = shelfAllocationDialog.totalQuantity;
  const shelfCount = shelfAllocationDialog.shelves.length;
  if (shelfCount === 0) return;
  
  shelfAllocationDialog.shelves.forEach(shelf => {
    shelf.allocatedQuantity = 0;
  });
  
  const baseAllocation = Math.floor(totalQuantity / shelfCount);
  let remaining = totalQuantity - (baseAllocation * shelfCount);
  
  shelfAllocationDialog.shelves.forEach(shelf => {
    shelf.allocatedQuantity = baseAllocation;
  });
  
  for (let i = 0; i < remaining; i++) {
    if (i < shelfAllocationDialog.shelves.length) {
      shelfAllocationDialog.shelves[i].allocatedQuantity += 1;
    }
  }
  
  updateShelfAllocationCalculations();
};

const confirmShelfAllocation = () => {
  const allocatedQuantity = shelfAllocationDialog.allocatedQuantity;
  const totalQuantity = shelfAllocationDialog.totalQuantity;
  
  if (allocatedQuantity !== totalQuantity) {
    ElMessage.warning(`分配数量 (${allocatedQuantity}) 与入库数量 (${totalQuantity}) 不一致`);
    return;
  }

  const item = formData.items[shelfAllocationDialog.productIndex];
  item.shelfAllocations = shelfAllocationDialog.shelves
    .filter(shelf => shelf.allocatedQuantity > 0)
    .map(shelf => ({
      shelfLocationId: shelf.id,
      quantity: parseInt(shelf.allocatedQuantity)
    }));

  shelfAllocationDialog.visible = false;
  ElMessage.success('货架分配完成');
};

// 其他方法（保持原有逻辑）
const generateOrderNo = () => {
  const date = new Date();
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const random = String(Math.random()).substr(2, 6);
  formData.orderNo = `SCRK${year}${month}${day}${random}`;
};

const handleWarehouseChange = async (value) => {
  if (value) {
    await loadShelfLocationList(value);
  } else {
    shelfLocationList.value = [];
  }
};

const handleAddProduct = () => {
  formData.items.push({
    productId: null,
    productName: '',
    sku: '',
    spec: '',
    unit: '',
    actualQuantity: 1,
    relatedPickingOrderNo: '', // 手动添加的产品没有关联领料单
    shelfLocationIds: [],
    shelfAllocations: [],
    batchNo: generateBatchNo(),
    remark: ''
  });
};

const clearAllProducts = () => {
  if (formData.items.length === 0) return;
  
  ElMessageBox.confirm('确定要清空所有产品吗？', '清空确认', {
    type: 'warning'
  }).then(() => {
    formData.items = [];
    importedPickingOrders.value = []; // 同时清空已导入的领料单
    ElMessage.success('已清空所有产品');
  });
};

const handleRemoveProduct = (index) => {
  const item = formData.items[index];
  
  // 如果删除的是导入的产品，检查是否需要从已导入列表中移除
  if (item.relatedPickingOrderNo) {
    const remainingItemsFromOrder = formData.items.filter(
      product => product.relatedPickingOrderNo === item.relatedPickingOrderNo && product !== item
    );
    
    if (remainingItemsFromOrder.length === 0) {
      // 该领料单没有其他产品了，从已导入列表中移除
      const orderIndex = importedPickingOrders.value.findIndex(
        order => order.orderNo === item.relatedPickingOrderNo
      );
      if (orderIndex > -1) {
        importedPickingOrders.value.splice(orderIndex, 1);
      }
    }
  }
  
  formData.items.splice(index, 1);
};

const handleProductChange = (productId, index) => {
  const product = productList.value.find(p => p.id === productId);
  if (product) {
    const item = formData.items[index];
    item.productName = product.name;
    item.sku = product.sku;
    item.spec = product.spec;
    item.unit = product.unitName;
  }
};

const validateBatchNo = (batchNo, index) => {
  if (batchNo && !/^[A-Za-z0-9_-]+$/.test(batchNo)) {
    ElMessage.warning('批次号只能包含字母、数字、下划线和横线');
    formData.items[index].batchNo = '';
  }
};

const getShelfLocationLabel = (location) => {
  if (!location) return '';
  return `${location.shelfName} (${location.shelfCode})`;
};

// 数据加载方法
const loadWarehouseList = async () => {
  try {
    const res = await get('/api/auth/warehouse/listEnable');
    warehouseList.value = res || [];
  } catch (error) {
    ElMessage.error('加载仓库列表失败');
  }
};

const loadProductList = async () => {
  try {
    const res = await get('/api/auth/product/listEnable');
    productList.value = res || [];
  } catch (error) {
    ElMessage.error('加载产品列表失败');
  }
};

const loadShelfLocationList = async (warehouseId) => {
  try {
    const res = await get(`/api/auth/shelf/listEnable?warehouseId=${warehouseId}`);
    shelfLocationList.value = res || [];
  } catch (error) {
    shelfLocationList.value = [];
  }
};

// 提交相关方法（保持原有逻辑）
const handleReset = () => {
  ElMessageBox.confirm(
    `确定要${isEditMode.value ? '重置' : '清空'}表单吗？所有输入的数据将会丢失。`, 
    `${isEditMode.value ? '重置' : '清空'}确认`, 
    { type: 'warning' }
  ).then(() => {
    if (isEditMode.value) {
      loadInboundDetail(route.params.id);
    } else {
      formRef.value?.resetFields();
      formData.items = [];
      importedPickingOrders.value = [];
      dataSource.value = 'manual';
      selectedPickingOrderId.value = '';
      selectedPickingOrder.value = null;
      generateOrderNo();
      ElMessage.success('表单已重置');
    }
  });
};

const handleSaveDraft = async () => {
  if (!await validateForm()) return;
  
  loading.value = true;
  try {
    const submitData = prepareSubmitData(0);
    const url = isEditMode.value ? '/api/auth/inbound/update' : '/api/auth/inbound/create';
    const res = await post(url, submitData);
    if (res) {
      ElMessage.success(isEditMode.value ? '更新草稿成功' : '保存草稿成功');
      router.push('/index/ckInboundManage');
    }
  } catch (error) {
    ElMessage.error(isEditMode.value ? '更新草稿失败' : '保存草稿失败');
  } finally {
    loading.value = false;
  }
};

const handleSubmit = async () => {
  if (!await validateForm()) return;
  
  if (formData.items.length === 0) {
    ElMessage.warning('请至少添加一个产品');
    return;
  }
  
  const hasUnallocatedItems = formData.items.some(item => {
    if (!item.shelfLocationIds || item.shelfLocationIds.length === 0) return true;
    const allocated = item.shelfAllocations?.reduce((sum, alloc) => sum + (parseInt(alloc.quantity) || 0), 0) || 0;
    return allocated !== (parseInt(item.actualQuantity) || 0);
  });
  
  if (hasUnallocatedItems) {
    ElMessage.warning('存在未完成货架分配的产品，请完成货架分配后再提交');
    return;
  }
  
  loading.value = true;
  try {
    const submitData = prepareSubmitData(2);
    const url = isEditMode.value ? '/api/auth/inbound/update' : '/api/auth/inbound/create';
    const res = await post(url, submitData);
    if (res) {
      ElMessage.success(isEditMode.value ? '更新成功' : '提交成功，等待审核');
      router.push('/index/ckInboundManage');
    }
  } catch (error) {
    ElMessage.error(isEditMode.value ? '更新失败' : '提交失败');
  } finally {
    loading.value = false;
  }
};

const prepareSubmitData = (status) => {
  const items = formData.items.map(item => {
    const shelfAllocations = Array.isArray(item.shelfAllocations) 
      ? item.shelfAllocations
          .filter(alloc => alloc && alloc.shelfLocationId && alloc.quantity >= 0)
          .map(alloc => ({
            shelfLocationId: alloc.shelfLocationId,
            quantity: parseInt(alloc.quantity) || 0
          }))
      : [];
    
    return {
      ...item,
      shelfAllocations: shelfAllocations,
      shelfLocationId: item.shelfLocationIds && item.shelfLocationIds.length > 0 
        ? item.shelfLocationIds[0] 
        : null
    };
  });

  return {
    ...formData,
    status: status,
    totalQuantity: parseInt(totalActualQuantity.value),
    items: items
  };
};

const validateForm = async () => {
  if (!formRef.value) return false;
  
  try {
    await formRef.value.validate();
    
    for (let i = 0; i < formData.items.length; i++) {
      const item = formData.items[i];
      if (!item.productId) {
        ElMessage.warning(`请选择第 ${i + 1} 行的产品`);
        return false;
      }
      if (!item.actualQuantity || item.actualQuantity <= 0) {
        ElMessage.warning(`请输入第 ${i + 1} 行产品的有效数量`);
        return false;
      }
    }
    
    return true;
  } catch (error) {
    ElMessage.warning('请完善表单信息');
    return false;
  }
};

// 编辑模式加载详情（简化版）
const loadInboundDetail = async (id) => {
  loading.value = true;
  try {
    const res = await get(`/api/auth/inbound/detailOfProductionInbound?orderId=${id}`);
    if (res) {
      Object.assign(formData, {
        id: res.id,
        orderNo: res.orderNo,
        orderType: res.orderType,
        warehouseId: res.warehouseId,
        expectedDate: res.expectedDate || '',
        remark: res.remark || '',
        status: res.status,
        items: []
      });

      if (res.warehouseId) {
        await loadShelfLocationList(res.warehouseId);
      }

      if (res.items && res.items.length > 0) {
        setTimeout(() => {
          formData.items = res.items.map(item => {
            const shelfLocationIds = item.shelfLocationIds || [];
            let shelfAllocations = [];
            
            if (item.shelfAllocations && item.shelfAllocations.length > 0) {
              shelfAllocations = item.shelfAllocations.map(allocation => ({
                shelfLocationId: allocation.shelfLocationId,
                quantity: parseInt(allocation.quantity) || 0
              }));
            }
            
            if (shelfLocationIds.length > 0 && shelfAllocations.length === 0) {
              shelfAllocations = shelfLocationIds.map(shelfId => ({
                shelfLocationId: shelfId,
                quantity: 0
              }));
            }

            // 重建已导入领料单列表
            if (item.relatedPickingOrderNo && !importedPickingOrders.value.some(order => order.orderNo === item.relatedPickingOrderNo)) {
              importedPickingOrders.value.push({
                id: item.relatedPickingOrderId || Date.now(), // 如果没有id，使用时间戳
                orderNo: item.relatedPickingOrderNo
              });
            }

            return {
              productId: item.productId,
              productName: item.productName || '',
              sku: item.sku || '',
              spec: item.spec || '',
              unit: item.unit || '',
              actualQuantity: parseInt(item.actualQuantity) || 1,
              relatedPickingOrderNo: item.relatedPickingOrderNo || '', // 加载关联领料单号
              shelfLocationIds: shelfLocationIds,
              shelfAllocations: shelfAllocations,
              batchNo: item.batchNo || generateBatchNo(),
              remark: item.remark || ''
            };
          });
        }, 100);
      }
      
      ElMessage.success('数据加载成功');
    }
  } catch (error) {
    ElMessage.error('加载数据失败');
    router.back();
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  if (isEditMode.value) {
    loadInboundDetail(route.params.id);
  } else {
    generateOrderNo();
  }
  loadWarehouseList();
  loadProductList();
});

watch(
  () => route.params.id,
  (newId) => {
    if (newId) {
      loadInboundDetail(newId);
    } else {
      Object.assign(formData, {
        id: null,
        orderNo: '',
        orderType: 2,
        warehouseId: null,
        expectedDate: '',
        remark: '',
        status: 0,
        items: []
      });
      importedPickingOrders.value = [];
      dataSource.value = 'manual';
      selectedPickingOrderId.value = '';
      selectedPickingOrder.value = null;
      generateOrderNo();
    }
  }
);
</script>

<style scoped>
/* 原有样式保持不变，只添加新样式 */
.imported-picking-orders {
  margin-top: 16px;
  padding: 12px;
  background-color: #f0f9ff;
  border-radius: 4px;
  border: 1px solid #e1f5fe;
}

.imported-picking-orders h4 {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #0288d1;
}

.imported-list {
  display: flex;
  flex-wrap: wrap;
}

/* 其他样式保持不变 */
.inbound-create-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.form-card {
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

/* 数据来源区域 */
.data-source-section {
  margin-bottom: 24px;
  padding: 16px;
  background-color: #f8f9fa;
  border-radius: 6px;
  border: 1px solid #ebeef5;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.source-options {
  margin-bottom: 16px;
}

.picking-selector {
  margin-top: 12px;
}

.picking-preview {
  margin-top: 16px;
  padding: 16px;
  background-color: white;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
}

.picking-products {
  margin-top: 16px;
}

.picking-products h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #606266;
}

.import-actions {
  margin-top: 16px;
  text-align: center;
}

/* 基本信息区域 */
.basic-info-section {
  margin-bottom: 24px;
}

.inbound-form {
  margin-bottom: 0;
}

/* 产品明细区域 */
.product-section {
  margin: 24px 0;
}

.product-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.product-table-container {
  margin-bottom: 16px;
}

.product-table {
  margin-bottom: 0;
}

/* 统计信息 */
.summary-info {
  padding: 16px;
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
  font-size: 16px;
}

.status-success {
  color: #67C23A;
}

.status-warning {
  color: #E6A23C;
}

/* 货架分配对话框 */
.shelf-allocation-dialog-content {
  padding: 0 10px;
}

.allocation-info {
  margin-bottom: 16px;
  padding: 12px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.info-item:last-child {
  margin-bottom: 0;
}

.info-item .label {
  color: #606266;
  font-size: 14px;
}

.info-item .value {
  color: #303133;
  font-weight: bold;
}

.info-item .value.success {
  color: #67C23A;
}

.info-item .value.warning {
  color: #E6A23C;
}

.shelf-allocation-list {
  max-height: 300px;
  overflow-y: auto;
  margin: 16px 0;
}

.shelf-allocation-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  margin-bottom: 8px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  background-color: #f8f9fa;
}

.shelf-info {
  flex: 1;
}

.shelf-name {
  font-weight: 500;
  color: #303133;
}

.allocation-control {
  display: flex;
  align-items: center;
  gap: 8px;
}

.allocation-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 16px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .inbound-create-container {
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
    gap: 12px;
    align-items: flex-start;
  }
  
  .product-actions {
    width: 100%;
    justify-content: flex-start;
    flex-wrap: wrap;
  }
  
  .summary-info .el-col {
    margin-bottom: 8px;
  }
  
  .shelf-allocation-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .allocation-control {
    width: 100%;
    justify-content: space-between;
  }
}
</style>