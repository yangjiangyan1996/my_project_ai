<!-- CkInboundProduction.vue -->
<template>
  <div class="inbound-create-container">
    <!-- 返回按钮行 -->
    <div class="back-header">
      <el-button 
        type="text" 
        @click="handleGoBack"
        :icon="ArrowLeft"
        class="back-btn"
      >
        返回
      </el-button>
    </div>

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
            <!-- <el-button 
              type="primary" 
              @click="handleSubmit" 
              :loading="loading"
              v-if="!isEditMode || (isEditMode && (formData.status === 0 || formData.status === 4))"
            >
              {{ isEditMode ? '更新提交' : '提交审核' }}
            </el-button> -->
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
              :label="`${order.relatedOrderNo} - ${order.remark} (${order.mainProductName || '多产品'})`"
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
                <el-table-column prop="sku" label="SKU" min-width="120" />
                <el-table-column prop="spec" label="规格" min-width="120" />
                <el-table-column prop="color" label="颜色" width="100" align="center" />
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
            <!-- 自动全部分配按钮 -->
            <el-button 
              type="success" 
              @click="handleAutoAllocateAll" 
              :loading="autoAllocating"
              :disabled="!canAutoAllocate"
              class="auto-allocate-btn"
              icon="CircleCheck"
              v-if="!isEditMode || (isEditMode && formData.status === 0)"
            >
              自动全部分配
            </el-button>
            <!-- 新增：批量更新批次号按钮 -->
            <el-button 
              type="info" 
              @click="handleBatchUpdateBatchNo"
              :disabled="formData.items.length === 0"
              icon="Refresh"
              class="batch-update-btn"
            >
              批量更新批次号
            </el-button>
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
            style="min-width: 1500px"
          >
            <el-table-column type="index" label="序号" width="60" align="center" fixed="left" />
            <el-table-column label="产品信息" min-width="220" fixed="left">
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
                    :label="`${product.name}-${product.sku}`"
                    :value="product.id"
                  />
                </el-select>
                 <div v-if="row.productName" class="product-details">
                  <div class="product-name">name:{{ row.productName }}</div>
                  <div class="product-sku">sku:{{ row.sku }}</div>
                 
                </div>
              </template>
            </el-table-column>
            <el-table-column label="规格" width="80">
              <template #default="{ row }">
                <span>{{ row.spec || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="颜色" width="80">
              <template #default="{ row }">
                <span>{{ row.color || '-' }}</span>
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
            <el-table-column label="货架分配" width="180" align="center">
              <template #default="{ row, $index }">
                <div v-if="row.shelfAllocations && row.shelfAllocations.length > 0" class="shelf-allocation-summary">
                  <div 
                    v-for="allocation in row.shelfAllocations" 
                    :key="allocation.shelfLocationId"
                    class="allocation-item"
                  >
                    <el-tooltip
                      :content="`${getShelfName(allocation.shelfLocationId)}: ${allocation.quantity}`"
                      placement="top"
                    >
                      <span class="allocation-text">
                        {{ getShelfName(allocation.shelfLocationId) }}: {{ allocation.quantity }}
                      </span>
                    </el-tooltip>
                  </div>
                </div>
                <div v-else class="allocation-empty">
                  <span class="empty-text">未分配</span>
                </div>
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
                <span class="label">自动分配状态：</span>
                <span class="value" :class="autoAllocationStatus.class">
                  {{ autoAllocationStatus.text }}
                </span>
              </div>
            </el-col>
          </el-row>
        </div>
      </div>
    </el-card>
  </div>

</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Delete, CircleCheck, ArrowLeft } from '@element-plus/icons-vue';
import { post, get } from '@/net';

const router = useRouter();
const route = useRoute();
const formRef = ref();
const loading = ref(false);
const importing = ref(false);
const autoAllocating = ref(false); // 自动分配加载状态

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

// 选项数据
const warehouseList = ref([]);
const productList = ref([]);
const shelfLocationList = ref([]);

// 计算属性
const totalActualQuantity = computed(() => {
  return formData.items.reduce((sum, item) => sum + (parseInt(item.actualQuantity) || 0), 0);
});

// 计算是否可以自动分配
const canAutoAllocate = computed(() => {
  if (!formData.warehouseId) return false;
  
  // 检查是否有已选择的产品并且有数量
  const validItems = formData.items.filter(item => 
    item.productId && item.actualQuantity > 0
  );
  
  return validItems.length > 0;
});

// 自动分配状态信息
const autoAllocationStatus = computed(() => {
  if (!formData.warehouseId) {
    return { text: '请先选择仓库', class: 'warning' };
  }
  
  const validItems = formData.items.filter(item => 
    item.productId && item.actualQuantity > 0
  );
  
  if (validItems.length === 0) {
    return { text: '请添加产品并输入数量', class: 'warning' };
  }
  
  const allocatedItems = formData.items.filter(item => 
    item.shelfAllocations && item.shelfAllocations.length > 0
  );
  
  if (allocatedItems.length === validItems.length) {
    return { text: '已全部分配', class: 'success' };
  }
  
  return { 
    text: `${allocatedItems.length}/${validItems.length} 已分配`, 
    class: 'info' 
  };
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

// ==================== 自动分配相关方法 ====================

// 返回上一页方法
const handleGoBack = () => {
  // 检查是否有未保存的更改
  const hasUnsavedChanges = formData.items.length > 0 || 
                           formData.warehouseId || 
                           formData.expectedDate || 
                           formData.remark;
  
  if (hasUnsavedChanges && !isEditMode.value) {
    ElMessageBox.confirm(
      '当前表单有未保存的更改，确定要返回吗？',
      '确认返回',
      {
        type: 'warning',
        confirmButtonText: '确定返回',
        cancelButtonText: '取消',
        distinguishCancelAndClose: true
      }
    ).then(() => {
      // 用户确认返回
      router.back();
    }).catch(() => {
      // 用户取消返回
    });
  } else {
    // 没有未保存的更改或处于编辑模式，直接返回
    router.back();
  }
};

// 自动全部分配方法
const handleAutoAllocateAll = async () => {
  if (!formData.warehouseId) {
    ElMessage.warning('请先选择仓库');
    return;
  }
  
  const validItems = formData.items.filter(item => 
    item.productId && item.actualQuantity > 0
  );
  
  if (validItems.length === 0) {
    ElMessage.warning('请先选择产品并输入数量');
    return;
  }
  
  autoAllocating.value = true;
  
  try {
    // 构建符合接口要求的参数
    const reqData = {
      warehouseId: formData.warehouseId,
      list: validItems.map(item => {
        // 找到产品信息以获取sku
        const product = productList.value.find(p => p.id === item.productId);
        return {
          productId: item.productId,
          sku: product ? product.sku : item.sku || '',
          quantity: parseFloat(item.actualQuantity) || 1
        };
      })
    };
    
    // 调用自动分配接口
    const res = await post('/api/auth/inbound/allocateIShelfnventoryQuantity', reqData);
    
    if (res && Array.isArray(res)) {
      // 处理分配结果
      processAutoAllocationResult(res, validItems);
      ElMessage.success('自动分配完成');
    } else {
      ElMessage.warning('获取分配结果失败');
    }
  } catch (error) {
    console.error('自动分配失败:', error);
    ElMessage.error('自动分配失败');
  } finally {
    autoAllocating.value = false;
  }
};

// 处理自动分配结果
const processAutoAllocationResult = (allocationResults, validItems) => {
  // 创建产品ID到分配结果的映射
  const allocationMap = new Map();
  allocationResults.forEach(result => {
    if (result.productId && result.shelfQuantityList) {
      allocationMap.set(result.productId, result.shelfQuantityList);
    }
  });
  
  // 更新每个产品的货架分配
  formData.items.forEach((item, index) => {
    if (!item.productId || item.actualQuantity <= 0) {
      // 清空无效产品的货架分配
      if (item.shelfLocationIds && item.shelfLocationIds.length > 0) {
        item.shelfLocationIds = [];
      }
      if (item.shelfAllocations && item.shelfAllocations.length > 0) {
        item.shelfAllocations = [];
      }
      return;
    }
    
    const shelfAllocation = allocationMap.get(item.productId);
    if (!shelfAllocation || !Array.isArray(shelfAllocation)) {
      // 如果没有分配结果，清空已有分配
      item.shelfLocationIds = [];
      item.shelfAllocations = [];
      return;
    }
    
    // 过滤出当前仓库中存在的货架
    const availableShelves = shelfAllocation.filter(shelf => 
      shelfLocationList.value.some(loc => loc.id === shelf.shelfId)
    );
    
    if (availableShelves.length === 0) {
      item.shelfLocationIds = [];
      item.shelfAllocations = [];
      return;
    }
    
    // 计算总分配数量
    const totalAllocated = availableShelves.reduce((sum, shelf) => 
      sum + (parseFloat(shelf.quantity) || 0), 0
    );
    
    // 如果总分配数量与实际数量不匹配，按比例调整
    const actualQuantity = parseFloat(item.actualQuantity) || 1;
    let adjustedShelves = [...availableShelves];
    
    if (totalAllocated !== actualQuantity && totalAllocated > 0) {
      // 按比例调整每个货架的分配数量
      const ratio = actualQuantity / totalAllocated;
      adjustedShelves = availableShelves.map(shelf => ({
        ...shelf,
        quantity: Math.round(parseFloat(shelf.quantity) * ratio)
      }));
      
      // 处理四舍五入可能导致的误差
      const adjustedTotal = adjustedShelves.reduce((sum, shelf) => sum + (shelf.quantity || 0), 0);
      if (adjustedTotal !== actualQuantity) {
        const diff = actualQuantity - adjustedTotal;
        if (diff !== 0 && adjustedShelves.length > 0) {
          // 将差异加到第一个货架上
          adjustedShelves[0].quantity = Math.max(0, (adjustedShelves[0].quantity || 0) + diff);
        }
      }
    }
    
    // 过滤掉数量为0的货架
    const validShelves = adjustedShelves.filter(shelf => shelf.quantity > 0);
    
    // 更新货架位置ID
    item.shelfLocationIds = validShelves.map(shelf => shelf.shelfId);
    
    // 更新货架分配
    item.shelfAllocations = validShelves.map(shelf => ({
      shelfLocationId: shelf.shelfId,
      quantity: shelf.quantity
    }));
  });
};

// ==================== 自动分配相关方法结束 ====================

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
    const res = await get(`/api/auth/outbound/simpleDetailOfProductionOutboundDetail?orderId=${orderId}`);
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
    
     // 生成默认批次号
    const defaultBatchNo = generateBatchNo();

    // 设置基本信息（如果是第一次导入）
    if (!formData.warehouseId && pickingOrder.warehouseId) {
      //formData.warehouseId = pickingOrder.warehouseId;
      await loadShelfLocationList(pickingOrder.warehouseId);
    }
    
    // 导入产品数据 - 基于BOM反向推导产成品
    const finishedProducts = deriveFinishedProducts(pickingOrder.items);
    console.log("finishedProducts:", finishedProducts);
    const newItems = finishedProducts.map(product => ({
      productId: product.id,
      productName: product.name,
      sku: product.sku,
      spec: product.spec,
      unit: product.unit,
      color: product.color,
      actualQuantity: product.quantity,
      relatedPickingOrderNo: pickingOrder.orderNo, // 关联单号放到产品明细
      shelfLocationIds: [],
      shelfAllocations: [],
      batchNo: defaultBatchNo,
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


// 批量更新批次号的方法
const handleBatchUpdateBatchNo = () => {
  if (formData.items.length === 0) {
    ElMessage.warning('没有产品需要更新批次号');
    return;
  }
  
  ElMessageBox.confirm(
    '确定要批量更新所有产品的批次号吗？现有批次号将被覆盖。',
    '批量更新确认',
    {
      type: 'warning',
      confirmButtonText: '确定更新',
      cancelButtonText: '取消'
    }
  ).then(() => {
    const newBatchNo = generateBatchNo();
    let updatedCount = 0;
    
    formData.items.forEach((item, index) => {
      if (item.productId) {
        item.batchNo = newBatchNo;
        validateBatchNo(newBatchNo, index);
        updatedCount++;
      }
    });
    
    ElMessage.success(`成功为 ${updatedCount} 个产品更新批次号: ${newBatchNo}`);
  }).catch(() => {
    // 用户取消
  });
};


// 根据领料数据推导产成品（简化逻辑）
const deriveFinishedProducts = (pickingItems) => {
  // 这里需要根据您的BOM逻辑来推导
  // 简化示例：假设领料单中的产品就是产成品
  return pickingItems.map(item => ({
    id: item.productId,
    name: item.productName,
    sku: item.sku,
    color: item.color,
    spec: item.spec,
    unit: item.unit,
    quantity: Math.floor(item.quantity) // 简化计算
  }));
};

// 生成批次号
// 生成批次号 - 按照指定规则
const generateBatchNo = () => {
  const date = new Date();
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  
  // 生成6位随机数字
  const random = Math.floor(Math.random() * 900000 + 100000).toString();
  
  // 方案1：只使用纯数字格式 - 年月日+随机数
  // 格式：production20251219123456
  let batchNo = `production${year}${month}${day}${random}`;
  
  // 如果有导入的领料单，取领料单号中的数字部分
  if (importedPickingOrders.value.length > 0) {
    // 取第一个已导入领料单的订单号
    const firstOrderNo = importedPickingOrders.value[0]?.orderNo || '';
    if (firstOrderNo) {
      // 提取采购单号中的数字部分
      const relatedNumbers = firstOrderNo.replace(/\D/g, '');
      if (relatedNumbers) {
        batchNo += relatedNumbers.slice(0, 6); // 最多取6位
      }
    }
  }
  
  // 如果有入库仓库ID，直接添加数字
  if (formData.warehouseId) {
    batchNo += formData.warehouseId.toString().slice(0, 4); // 最多取4位
  }
  
  // 确保批次号长度合理
  if (batchNo.length > 50) {
    batchNo = batchNo.substring(0, 50);
  }
  
  return batchNo;
};

// 生成默认批次号并应用到所有产品
const generateDefaultBatchNo = () => {
  const defaultBatchNo = generateBatchNo();
  
  // 将默认批次号应用到所有已有商品行
  formData.items.forEach((item, index) => {
    if (item.productId && (!item.batchNo || item.batchNo.trim() === '')) {
      item.batchNo = defaultBatchNo;
      validateBatchNo(defaultBatchNo, index);
    }
  });
  
  return defaultBatchNo;
};

// 获取货架名称
const getShelfName = (shelfLocationId) => {
  const location = shelfLocationList.value.find(loc => loc.id === shelfLocationId);
  return location ? location.shelfName : '未知货架';
};

// 其他方法
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
  const newBatchNo = generateBatchNo(); // 为新产品生成批次号
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
    batchNo: newBatchNo,
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
    console.log("111"+product);
    const item = formData.items[index];
    item.productName = product.name;
    item.sku = product.sku;
    item.spec = product.spec;
    item.color = product.color;
    item.unit = product.unitName;
  }
};

const validateBatchNo = (batchNo, index) => {
  if (!batchNo) return true;
  
  // 移除非字母数字字符，只保留字母和数字
  const cleanedBatchNo = batchNo.replace(/[^A-Za-z0-9]/g, '');
  
  // 更新为清理后的批次号
  if (cleanedBatchNo !== batchNo) {
    formData.items[index].batchNo = cleanedBatchNo;
    batchNo = cleanedBatchNo;
  }
  
  // 验证批次号格式
  if (!/^[A-Za-z0-9]+$/.test(batchNo)) {
    ElMessage.warning('批次号只能包含字母和数字');
    formData.items[index].batchNo = '';
    return false;
  }
  
  // 检查批次号长度
  if (batchNo.length < 5) {
    ElMessage.warning('批次号长度至少为5位');
    return false;
  }
  
  if (batchNo.length > 50) {
    ElMessage.warning('批次号长度不能超过50位');
    formData.items[index].batchNo = batchNo.substring(0, 50);
    return false;
  }
  
  return true;
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

// 提交相关方法
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
      router.replace({
        path: '/',
        query: { mode: 'inbound' } // 返回到入库管理页面
      });
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
      router.replace({
        path: '/',
        query: { mode: 'inbound' } // 返回到入库管理页面
      });
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
/* 返回按钮区域样式 */
.back-header {
  margin-bottom: 16px;
  padding: 0 4px;
}

.back-btn {
  padding: 10px 16px;  /* 增加内边距 */
  font-size: 16px;     /* 增大字体 */
  font-weight: 500;    /* 增加字重 */
  color: #409EFF;
}

.back-btn:hover {
  background-color: #ecf5ff;
  border-radius: 4px;
}

.back-btn i {
  margin-right: 6px;  /* 增加图标和文字间距 */
  font-size: 18px;    /* 增大图标 */
}

/* 调整整体容器，为返回按钮腾出空间 */
.inbound-create-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

/* 响应式调整 */
@media (max-width: 768px) {
  .back-header {
    margin-bottom: 12px;
  }
  
  .back-btn {
    padding: 8px 14px;
    font-size: 15px;
  }
  
  .back-btn i {
    font-size: 16px;
    margin-right: 4px;
  }
}

/* 自动分配按钮样式 */
.product-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.auto-allocate-btn {
  margin-right: 8px;
}

/* 货架分配状态样式 */
.shelf-allocation-summary {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.allocation-item {
  margin-bottom: 4px;
  padding: 2px 4px;
  background-color: #f0f7ff;
  border-radius: 2px;
  border-left: 2px solid #409eff;
}

.allocation-text {
  font-size: 12px;
  color: #409eff;
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.allocation-empty {
  text-align: center;
  padding: 8px 0;
}

.empty-text {
  color: #c0c4cc;
  font-size: 12px;
}

/* 统计信息样式调整 */
.summary-item .value.success {
  color: #67C23A;
}

.summary-item .value.warning {
  color: #E6A23C;
}

.summary-item .value.info {
  color: #409EFF;
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

.product-table-container {
  margin-bottom: 16px;
  width: 100%;
  overflow-x: auto;
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

.product-details {
  margin-top: 8px;
  padding: 4px;
  background-color: #f8f9fa;
  border-radius: 4px;
  font-size: 12px;
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
    flex-direction: column;
    width: 100%;
    gap: 8px;
  }
  
  .auto-allocate-btn,
  .product-actions .el-button {
    width: 100%;
  }
  
  .summary-info .el-col {
    margin-bottom: 8px;
  }
}
</style>