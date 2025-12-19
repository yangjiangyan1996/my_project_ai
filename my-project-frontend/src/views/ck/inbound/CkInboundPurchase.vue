<!-- 采购入库， ORDER_TYPE =1 -->
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
          <span class="card-title">{{ isEditMode ? '编辑采购入库单' : '新建采购入库单' }}</span>
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

      <!-- 基本信息 -->
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
        class="inbound-form"
      >
       
        <el-row :gutter="24">
          <el-col :xs="24" :sm="12" :lg="8">
            <el-form-item label="采购单号" prop="relatedOrderNo">
              <el-input
                v-model="formData.relatedOrderNo"
                placeholder="请输入采购单号"
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :lg="8">
            <el-form-item label="入库单号" prop="orderNo">
              <el-input v-model="formData.orderNo" placeholder="系统自动生成" disabled />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :lg="8">
            <el-form-item label="入库类型" prop="orderType">
              <el-input v-model="orderTypeText" disabled />
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
            <el-form-item label="供应商" prop="supplierId">
              <el-select
                v-model="formData.supplierId"
                placeholder="请选择供应商"
                style="width: 100%"
                filterable
                @change="handleSupplierChange"
              >
                <el-option
                  v-for="supplier in supplierList"
                  :key="supplier.id"
                  :label="supplier.supplierName"
                  :value="supplier.id"
                />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :xs="24" :sm="12" :lg="8">
            <el-form-item label="默认批次号" prop="defaultBatchNo">
              <el-tooltip :content="defaultBatchNo || '未生成'" placement="top">
                <el-input 
                  v-model="defaultBatchNo" 
                  placeholder="生成中..." 
                  disabled
                  style="width: 100%"
                />
              </el-tooltip>
            </el-form-item>
          </el-col>
        </el-row>


        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="formData.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <!-- 产品明细 -->
      <div class="product-section">
        <div class="section-header">
          <h3>产品明细</h3>
          <div class="product-actions">
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
            <el-button type="primary" @click="handleAddProduct" :icon="Plus">
              添加产品
            </el-button>
          </div>
        </div>

        <!-- 外层容器添加水平滚动 -->
        <div class="table-container">
          <el-table
            :data="formData.items"
            border
            class="product-table"
            empty-text="请添加产品明细"
            style="min-width: 1200px"  
          >
            <!-- 序号列 -->
            <el-table-column type="index" label="序号" width="60" align="center" fixed="left" />
            
            <!-- 产品信息列 -->
            <el-table-column label="产品信息" min-width="220" fixed="left">
              <template #default="{ row, $index }">
                <div class="product-info-cell">
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
                      :label="getProductOptionLabel(product)"
                      :value="product.id"
                      :disabled="isProductDisabled(product.id, $index)"
                    />
                  </el-select>
                  <div v-if="row.productName" class="product-details">
                    <div class="product-name">name:{{ row.productName }}</div>
                    <!-- <div class="product-spec">spec:{{ row.spec }}</div> -->
                    <!-- <div class="product-color">color:{{ row.color }}</div> -->
                    <div class="product-sku">sku:{{ row.sku }}</div>
                  </div>
                </div>
              </template>
            </el-table-column>

            <!-- 规格型号 -->
            <el-table-column label="规格" min-width="80" show-overflow-tooltip>
              <template #default="{ row }">
                <span class="cell-content">{{ row.spec || '-' }}</span>
              </template>
            </el-table-column>

            <!-- 颜色 -->
            <el-table-column label="颜色" min-width="60" show-overflow-tooltip>
              <template #default="{ row }">
                <span class="cell-content">{{ row.color || '-' }}</span>
              </template>
            </el-table-column>

            <!-- 单位 -->
            <el-table-column label="单位" width="60" align="center">
              <template #default="{ row }">
                <span class="cell-content">{{ row.unit || '-' }}</span>
              </template>
            </el-table-column>

            <!-- 实际数量列 -->
            <el-table-column label="实际数量" width="130" align="center">
              <template #default="{ row, $index }">
                <el-input
                  v-model="row.actualQuantity"
                  placeholder="数量"
                  size="small"
                  type="number"
                  min="1"
                  style="width: 100%"
                  @blur="handleActualQuantityBlur($index)"
                />
              </template>
            </el-table-column>

            <!-- 单价列 -->
            <el-table-column label="单价" width="130" align="center">
              <template #default="{ row, $index }">
                <el-input
                  v-model="row.priceUnit"
                  placeholder="单价"
                  size="small"
                  type="number"
                  min="0"
                  step="0.0001"
                  style="width: 100%"
                  @blur="handlePriceUnitBlur($index)"
                >
                  <template #prefix>¥</template>
                </el-input>
              </template>
            </el-table-column>

            <!-- 总价 -->
            <el-table-column label="总价" width="120" align="right">
              <template #default="{ row }">
                <span class="price-total">¥ {{ (row.priceTotal || 0).toFixed(2) }}</span>
              </template>
            </el-table-column>

            <!-- 批次号 -->
            <el-table-column label="批次号" min-width="180">
              <template #default="{ row, $index }">
                <div class="batch-no-cell">
                  <el-input
                    v-model="row.batchNo"
                    placeholder="批次号"
                    size="small"
                    @blur="() => validateBatchNo(row.batchNo, $index)"
                    class="batch-input"
                  />
                  <el-button
                    v-if="row.batchNo"
                    type="primary"
                    link
                    size="small"
                    @click="copyBatchNoToAll(row.batchNo)"
                    class="copy-batch-btn"
                    title="复制此批次号到所有产品"
                  >
                    复制到所有
                  </el-button>
                </div>
              </template>
            </el-table-column>

            <!-- 货架位置 -->
            <el-table-column label="货架位置" min-width="180">
              <template #default="{ row, $index }">
                <div class="shelf-location-cell">
                  <el-select
                    v-model="row.shelfLocationIds"
                    placeholder="选择位置"
                    style="width: 100%"
                    filterable
                    multiple
                    collapse-tags
                    collapse-tags-tooltip
                    :max-collapse-tags="1"
                    size="small"
                    @change="() => handleShelfLocationChange($index)"
                  >
                    <el-option
                      v-for="location in shelfLocationList"
                      :key="location.id"
                      :label="getShelfLocationLabel(location)"
                      :value="location.id"
                    />
                  </el-select>
                  <el-button 
                    v-if="row.shelfLocationIds && row.shelfLocationIds.length > 0"
                    type="primary" 
                    link 
                    size="small"
                    @click="openShelfAllocationDialog($index)"
                    class="allocation-btn"
                  >
                    分配数量
                  </el-button>
                </div>
              </template>
            </el-table-column>

            <!-- 货架分配 -->
            <el-table-column label="货架分配" min-width="160">
              <template #default="{ row }">
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

            <!-- 备注 -->
            <el-table-column label="备注" min-width="180">
              <template #default="{ row, $index }">
                <el-input
                  v-model="row.remark"
                  placeholder="产品备注"
                  maxlength="100"
                  show-word-limit
                  size="small"
                  type="textarea"
                  :rows="2"
                  resize="none"
                />
              </template>
            </el-table-column>

            <!-- 操作列 -->
            <el-table-column label="操作" width="80" fixed="right" align="center">
              <template #default="{ $index }">
                <el-button
                  type="danger"
                  link
                  :icon="Delete"
                  @click="handleRemoveProduct($index)"
                  size="small"
                >
                  删除
                </el-button>
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
                <span class="label">实际总数：</span>
                <span class="value">{{ totalActualQuantity }} </span>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="summary-item">
                <span class="label">总金额：</span>
                <span class="value">¥ {{ totalAmount.toFixed(2) }}</span>
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
            <span class="label">产品名称：</span>
            <span class="value">{{ shelfAllocationDialog.productName }}</span>
          </div>
          <div class="info-item">
            <span class="label">总入库数量：</span>
            <span class="value">{{ shelfAllocationDialog.totalQuantity }}</span>
          </div>
          <div class="info-item">
            <span class="label">已分配数量：</span>
            <span class="value" :class="shelfAllocationDialog.allocatedQuantity >= shelfAllocationDialog.totalQuantity ? 'success' : 'warning'">
              {{ shelfAllocationDialog.allocatedQuantity }}
            </span>
          </div>
          <div class="info-item">
            <span class="label">剩余数量：</span>
            <span class="value">{{ shelfAllocationDialog.remainingQuantity }}</span>
          </div>
        </div>

        <el-divider>货架分配</el-divider>

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
              <el-button
                type="danger"
                link
                size="small"
                @click="clearShelfAllocation(index)"
                :disabled="!shelf.allocatedQuantity || shelf.allocatedQuantity === 0"
                class="clear-btn"
              >
                清空
              </el-button>
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
import { ref, reactive, computed, onMounted, watch, nextTick } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Delete,  CircleCheck, ArrowLeft } from '@element-plus/icons-vue';
import { post, get } from '@/net';

const router = useRouter();
const route = useRoute();
const formRef = ref();
const loading = ref(false);
const autoAllocating = ref(false);

// 采购入库类型固定为1
const ORDER_TYPE = 1;
const orderTypeText = '采购入库';

// 判断是否是编辑模式
const isEditMode = computed(() => {
  return !!route.params.id;
});

// 表单数据
const formData = reactive({
  id: null,
  orderNo: '',
  orderType: ORDER_TYPE, // 固定为采购入库
  warehouseId: null,
  supplierId: null,
  relatedOrderNo: '',
  remark: '',
  status: 0,
  items: [],
});

// 默认批次号
const defaultBatchNo = ref('');

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
const supplierList = ref([]);
const productList = ref([]);
const shelfLocationList = ref([]);

// 性能优化：缓存已选产品ID
const selectedProductIdsCache = ref(new Set());

// 计算属性
const totalActualQuantity = computed(() => {
  return formData.items.reduce((sum, item) => sum + (parseInt(item.actualQuantity) || 0), 0);
});

const totalAmount = computed(() => {
  return formData.items.reduce((sum, item) => {
    return sum + (parseFloat(item.priceTotal) || 0);
  }, 0);
});

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
    
    // 注意：接口是 @GetMapping 但有 @RequestBody，可能需要特殊处理
    // 尝试使用 post 方法传递参数
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

// 修改相关的数量处理方法，确保正确处理浮点数
const handleActualQuantityBlur = (index) => {
  const item = formData.items[index];
  const quantity = parseFloat(item.actualQuantity) || 1;  // 改为 parseFloat
  item.actualQuantity = Math.max(1, quantity);
  handleActualQuantityChange(quantity, index);
};

const handlePriceUnitBlur = (index) => {
  const item = formData.items[index];
  const price = parseFloat(item.priceUnit) || 0;
  item.priceUnit = Math.max(0, price);
  calculateItemTotal(index);
};

const handleActualQuantityChange = (value, index) => {
  const item = formData.items[index];
  const newQuantity = parseFloat(value) || 1;  // 改为 parseFloat
  item.actualQuantity = newQuantity;
  
  // 如果实际数量减少，需要重新验证货架分配
  if (item.shelfAllocations && item.shelfAllocations.length > 0) {
    const totalAllocated = item.shelfAllocations.reduce((sum, alloc) => sum + (parseFloat(alloc.quantity) || 0), 0);
    if (newQuantity < totalAllocated) {
      ElMessage.warning('实际数量小于已分配货架数量，请重新分配货架');
      item.shelfAllocations = [];
    }
  }
  
  calculateItemTotal(index);
};


// 返回上一页方法
const handleGoBack = () => {
  // 检查是否有未保存的更改
  const hasUnsavedChanges = formData.items.length > 0 || 
                           formData.warehouseId || 
                           formData.supplierId || 
                           formData.relatedOrderNo || 
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

// 复制批次号方法
const copyBatchNoToAll = (batchNo) => {
  if (!batchNo || batchNo.trim() === '') {
    ElMessage.warning('请先输入有效的批次号');
    return;
  }
  
  // 验证批次号格式
  if (!/^[A-Za-z0-9-]+$/.test(batchNo)) {
    ElMessage.warning('批次号只能包含字母、数字和连字符（-）');
    return;
  }
  
  ElMessageBox.confirm(
    `确定要将批次号 "${batchNo}" 复制到所有产品的批次号吗？`,
    '批量复制批次号',
    {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    }
  ).then(() => {
    // 更新默认批次号为用户输入的批次号
    defaultBatchNo.value = batchNo;
    
    // 复制到所有有产品的行
    formData.items.forEach((item, index) => {
      if (item.productId) {
        // 对每个产品验证批次号
        validateBatchNo(batchNo, index);
        item.batchNo = batchNo;
      }
    });
    ElMessage.success(`已成功将批次号 "${batchNo}" 复制到所有产品`);
  }).catch(() => {
    // 用户取消操作
  });
};

// 复制当前行的批次号到所有产品
const copyCurrentBatchToAll = (index) => {
  const currentItem = formData.items[index];
  if (!currentItem.productId) {
    ElMessage.warning('请先选择当前行的产品');
    return;
  }
  
  if (!currentItem.batchNo || currentItem.batchNo.trim() === '') {
    ElMessage.warning('请先输入当前行的批次号');
    return;
  }
  
  copyBatchNoToAll(currentItem.batchNo);
};

// 生成默认批次号
// 生成默认批次号 - 修复版（只允许字母和数字）
const generateDefaultBatchNo = () => {
  const date = new Date();
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  
  // 生成6位随机数字
  const random = Math.floor(Math.random() * 900000 + 100000).toString();
  
  // 方案1：只使用纯数字格式 - 年月日+随机数
  // 格式：purchase20251219123456
  let batchNo = `purchase${year}${month}${day}${random}`;
  
  // 如果有采购单号，只取其中的数字部分
  if (formData.relatedOrderNo) {
    // 提取采购单号中的数字部分
    const relatedNumbers = formData.relatedOrderNo.replace(/\D/g, '');
    if (relatedNumbers) {
      batchNo += relatedNumbers.slice(0, 6); // 最多取6位
    }
  }
  
  // 如果有供应商ID，直接添加数字
  if (formData.supplierId) {
    batchNo += formData.supplierId.toString().slice(0, 4); // 最多取4位
  }
  
  // 确保批次号长度合理
  if (batchNo.length > 50) {
    batchNo = batchNo.substring(0, 50);
  }
  
  defaultBatchNo.value = batchNo;
  
  // 将默认批次号应用到所有已有商品行
  formData.items.forEach((item, index) => {
    if (item.productId && (!item.batchNo || item.batchNo.trim() === '')) {
      item.batchNo = batchNo;
      validateBatchNo(batchNo, index);
    }
  });
};

const calculateItemTotal = (index) => {
  const item = formData.items[index];
  const quantity = parseFloat(item.actualQuantity) || 0;  // 改为 parseFloat
  const priceUnit = parseFloat(item.priceUnit) || 0;
  item.priceTotal = parseFloat((quantity * priceUnit).toFixed(2));
};

// 修改加载详情的方法中的数据处理
const loadInboundDetail = async (id) => {
  loading.value = true;
  try {
    const res = await get(`/api/auth/inbound/detail?orderId=${id}`);
    if (res) {
      Object.assign(formData, {
        id: res.id,
        orderNo: res.orderNo,
        orderType: res.orderType,
        warehouseId: res.warehouseId,
        supplierId: res.supplierId,
        relatedOrderNo: res.relatedOrderNo || '',
        remark: res.remark || '',
        status: res.status
      });

      if (res.warehouseId) {
        await loadShelfLocationList(res.warehouseId);
      }

      if (res.items && res.items.length > 0) {
        formData.items = res.items.map(item => {
          const shelfLocationIds = item.shelfLocationIds || 
                                (item.shelfLocationId ? [item.shelfLocationId] : []);
          
          let shelfAllocations = item.shelfAllocations || [];
          if (!shelfAllocations.length && item.shelfLocationId && item.actualQuantity) {
            shelfAllocations = [{
              shelfLocationId: item.shelfLocationId,
              quantity: parseFloat(item.actualQuantity) || 0
            }];
          }

          // 处理 quantity 字段（BigDecimal 转换为数字）
          shelfAllocations = shelfAllocations.map(allocation => ({
            ...allocation,
            quantity: parseFloat(allocation.quantity) || 0
          }));

          return {
            productId: item.productId,
            productName: item.productName || '',
            sku: item.sku || '',
            spec: item.spec || '',
            color: item.color || '',
            unit: item.unit || '',
            quantity: item.quantity || 1,
            actualQuantity: parseFloat(item.actualQuantity) || 1,
            priceUnit: parseFloat(item.priceUnit) || 0,
            priceTotal: parseFloat(item.priceTotal) || 0,
            shelfLocationIds: shelfLocationIds,
            shelfAllocations: shelfAllocations,
            batchNo: item.batchNo || '',
            remark: item.remark || ''
          };
        });
      } else {
        formData.items = [];
      }
      
      // 初始化缓存
      updateSelectedProductIdsCache();
      
      ElMessage.success('数据加载成功');
    }
  } catch (error) {
    ElMessage.error('加载数据失败');
    router.back();
  } finally {
    loading.value = false;
  }
};

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

// 缓存管理方法
const updateSelectedProductIdsCache = () => {
  const newCache = new Set();
  formData.items.forEach(item => {
    if (item.productId) {
      newCache.add(item.productId);
    }
  });
  selectedProductIdsCache.value = newCache;
};

// 获取产品选项标签（添加已选标识）
const getProductOptionLabel = (product) => {
  const baseLabel = `${product.name}-${product.spec || ''}-${product.color === null ? '' : product.color}-${product.sku} `;
  
  // 如果产品已被其他行选中，添加标识
  if (selectedProductIdsCache.value.has(product.id)) {
    const isCurrentSelection = formData.items.some(item => item.productId === product.id);
    if (!isCurrentSelection) {
      return `${baseLabel} (已选择)`;
    }
  }
  
  return baseLabel;
};

// 判断产品是否禁用
const isProductDisabled = (productId, currentIndex) => {
  const currentProductId = formData.items[currentIndex]?.productId;
  
  // 当前行已选中的产品不禁用（允许用户取消选择）
  if (currentProductId === productId) {
    return false;
  }
  
  // 其他行已选中的产品禁用
  return selectedProductIdsCache.value.has(productId);
};

// 表单验证规则
const formRules = {
  warehouseId: [
    { required: true, message: '请选择入库仓库', trigger: 'change' }
  ],
  supplierId: [
    { required: true, message: '请选择供应商', trigger: 'change' }
  ]
};

// 业务方法
const generateOrderNo = () => {
  const date = new Date();
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const random = String(Math.random()).substr(2, 6);
  formData.orderNo = `RK${year}${month}${day}${random}`;
};

const handleWarehouseChange = async (value) => {
  if (value) {
    await loadShelfLocationList(value);
    formData.items.forEach(item => {
      item.shelfLocationIds = [];
      item.shelfAllocations = [];
    });
  } else {
    shelfLocationList.value = [];
    formData.items.forEach(item => {
      item.shelfLocationIds = [];
      item.shelfAllocations = [];
    });
  }
};

const handleSupplierChange = (value) => {
  console.log('选择供应商:', value);
};

const handleAddProduct = () => {
  formData.items.push({
    productId: null,
    productName: '',
    sku: '',
    spec: '',
    color: '',
    unit: '',
    quantity: 1,
    actualQuantity: 1,
    priceUnit: 0,
    priceTotal: 0,
    shelfLocationIds: [],
    shelfAllocations: [],
    batchNo: defaultBatchNo.value || '',
    remark: ''
  });
};

const handleRemoveProduct = (index) => {
  formData.items.splice(index, 1);
  // 更新缓存
  nextTick(() => {
    updateSelectedProductIdsCache();
  });
};

const handleProductChange = (productId, index) => {
  const product = productList.value.find(p => p.id === productId);
  const item = formData.items[index];
  
  if (product) {
    item.productName = product.name;
    item.color = product.color;
    item.sku = product.sku;
    item.spec = product.spec;
    item.unit = product.unitName;
    if (!item.batchNo || item.batchNo.trim() === '') {
      item.batchNo = defaultBatchNo.value || '';
      if (item.batchNo) {
        validateBatchNo(item.batchNo, index);
      }
    }
  } else {
    // 清空产品信息
    item.productName = '';
    item.color = '';
    item.sku = '';
    item.spec = '';
    item.unit = '';
    item.shelfLocationIds = [];
    item.shelfAllocations = [];
  }
  
  // 更新缓存
  nextTick(() => {
    updateSelectedProductIdsCache();
  });
  
  // 重新计算总价
  calculateItemTotal(index);
};


const validateBatchNo = (batchNo, index) => {
  if (!batchNo || batchNo.trim() === '') {
    return; // 空批次号不验证
  }
  
  // 更严格的验证：只允许字母、数字和连字符
  if (!/^[A-Za-z0-9-]+$/.test(batchNo)) {
    ElMessage.warning('批次号只能包含字母、数字和连字符（-）');
    formData.items[index].batchNo = '';
    return;
  }
  
  // 可选：验证长度
  if (batchNo.length > 50) {
    ElMessage.warning('批次号长度不能超过50个字符');
    formData.items[index].batchNo = batchNo.substring(0, 50);
  }
  
  // 可选：验证不能以特殊字符开头或结尾
  if (batchNo.startsWith('-') || batchNo.endsWith('-')) {
    ElMessage.warning('批次号不能以连字符开头或结尾');
    formData.items[index].batchNo = batchNo.replace(/^-|-$/g, '');
  }
  
  // 可选：验证不能有连续的连字符
  if (batchNo.includes('--')) {
    ElMessage.warning('批次号不能有连续的连字符');
    formData.items[index].batchNo = batchNo.replace(/--+/g, '-');
  }
};

const handleShelfLocationChange = (index) => {
  const item = formData.items[index];
  
  if (item.shelfAllocations) {
    item.shelfAllocations = item.shelfAllocations.filter(allocation => 
      item.shelfLocationIds.includes(allocation.shelfLocationId)
    );
  } else {
    item.shelfAllocations = [];
  }
  
  item.shelfLocationIds.forEach(shelfId => {
    if (!item.shelfAllocations.some(allocation => allocation.shelfLocationId === shelfId)) {
      item.shelfAllocations.push({
        shelfLocationId: shelfId,
        quantity: 0
      });
    }
  });
};

const getShelfLocationLabel = (location) => {
  if (!location) return '';
  return `${location.shelfName} (${location.shelfCode})`;
};

const getShelfName = (shelfLocationId) => {
  const location = shelfLocationList.value.find(loc => loc.id === shelfLocationId);
  return location ? location.shelfName : '未知货架';
};

// 货架分配对话框相关方法
const openShelfAllocationDialog = (index) => {
  const item = formData.items[index];
  if (!item.productId) {
    ElMessage.warning('请先选择产品');
    return;
  }

  if (!item.shelfLocationIds || item.shelfLocationIds.length === 0) {
    ElMessage.warning('请先选择货架位置');
    return;
  }

  shelfAllocationDialog.productIndex = index;
  shelfAllocationDialog.productName = item.productName;
  shelfAllocationDialog.totalQuantity = parseInt(item.actualQuantity) || 1;
  
  shelfAllocationDialog.shelves = item.shelfLocationIds.map(shelfId => {
    const location = shelfLocationList.value.find(loc => loc.id === shelfId);
    const existingAllocation = item.shelfAllocations.find(
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
  const oldValue = changedShelf.allocatedQuantity;
  const newValue = parseInt(value) || 0;
  
  const otherShelvesTotal = shelfAllocationDialog.shelves.reduce((sum, shelf, index) => {
    if (index !== changedIndex) {
      return sum + (parseInt(shelf.allocatedQuantity) || 0);
    }
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
      changedShelf.allocatedQuantity = oldValue;
      ElMessage.warning(`分配数量不能超过总入库数量 ${shelfAllocationDialog.totalQuantity}`);
      return;
    }
  }
  
  changedShelf.allocatedQuantity = newValue;
  updateShelfAllocationCalculations();
};

const clearShelfAllocation = (shelfIndex) => {
  shelfAllocationDialog.shelves[shelfIndex].allocatedQuantity = 0;
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
    if (i !== index) {
      return sum + (parseInt(shelf.allocatedQuantity) || 0);
    }
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

// 数据加载方法
const loadWarehouseList = async () => {
  try {
    const res = await get('/api/auth/warehouse/listEnable');
    warehouseList.value = res || [];
  } catch (error) {
    ElMessage.error('加载仓库列表失败');
  }
};

const loadSupplierList = async () => {
  try {
    const res = await get('/api/auth/supplier/listEnable');
    supplierList.value = res || [];
  } catch (error) {
    ElMessage.error('加载供应商列表失败');
  }
};

const loadProductList = async () => {
  try {
    const res = await get('/api/auth/product/listEnableNotBom');
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

// 表单操作
const handleReset = () => {
  ElMessageBox.confirm(
    `确定要${isEditMode.value ? '重置' : '清空'}表单吗？所有输入的数据将会丢失。`, 
    `${isEditMode.value ? '重置' : '清空'}确认`, 
    {
      type: 'warning'
    }
  ).then(() => {
    if (isEditMode.value) {
      loadInboundDetail(route.params.id);
    } else {
      formRef.value?.resetFields();
      formData.items = [];
      generateOrderNo();
      // 清除缓存
      selectedProductIdsCache.value.clear();
      defaultBatchNo.value = ''
      ElMessage.success('表单已重置');
    }
  });
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
      if (item.priceUnit < 0) {
        ElMessage.warning(`第 ${i + 1} 行产品的单价不能为负数`);
        return false;
      }
    }
    
    return true;
  } catch (error) {
    ElMessage.warning('请完善表单信息');
    return false;
  }
};

const handleSaveDraft = async () => {
  if (!await validateForm()) return;
  
  loading.value = true;
  try {
    const submitData = {
      ...formData,
      status: 0,
      totalQuantity: parseInt(totalActualQuantity.value),
      totalAmount: totalAmount.value
    };
    
    const url = isEditMode.value ? '/api/auth/inbound/update' : '/api/auth/inbound/create';
    const res = await post(url, submitData);
    if (res) {
      ElMessage.success(isEditMode.value ? '更新草稿成功' : '保存草稿成功');
      router.replace({
        path: '/',
        query: { mode: 'inbound' }
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
    if (!item.shelfLocationIds || item.shelfLocationIds.length === 0) {
      return true;
    }
    
    const allocatedQuantity = item.shelfAllocations 
      ? item.shelfAllocations.reduce((sum, alloc) => sum + (parseInt(alloc.quantity) || 0), 0)
      : 0;
    const actualQuantity = parseInt(item.actualQuantity) || 0;
    
    return allocatedQuantity !== actualQuantity;
  });
  
  if (hasUnallocatedItems) {
    ElMessage.warning('存在未完成货架分配的产品，请完成货架分配后再提交');
    return;
  }
  
  loading.value = true;
  try {
    const submitData = {
      ...formData,
      status: 2,
      totalQuantity: parseInt(totalActualQuantity.value),
      totalAmount: totalAmount.value
    };
    
    const url = isEditMode.value ? '/api/auth/inbound/update' : '/api/auth/inbound/create';
    const res = await post(url, submitData);
    if (res) {
      ElMessage.success(isEditMode.value ? '更新成功' : '提交成功，等待审核');
      router.replace({
        path: '/',
        query: { mode: 'inbound' }
      });
    }
  } catch (error) {
    ElMessage.error(isEditMode.value ? '更新失败' : '提交失败');
  } finally {
    loading.value = false;
  }
};

// 生命周期和监听
onMounted(() => {
  if (isEditMode.value) {
    loadInboundDetail(route.params.id);
  } else {
    generateOrderNo();
    generateDefaultBatchNo();
  }
  loadWarehouseList();
  loadSupplierList();
  loadProductList();
});

// 监听表单数据变化，更新缓存
watch(
  () => formData.items,
  () => {
    nextTick(() => {
      updateSelectedProductIdsCache();
    });
  },
  { deep: true }
);

watch(
  () => route.params.id,
  (newId) => {
    if (newId) {
      loadInboundDetail(newId);
    } else {
      Object.assign(formData, {
        id: null,
        orderNo: '',
        orderType: ORDER_TYPE,
        warehouseId: null,
        supplierId: null,
        relatedOrderNo: '',
        remark: '',
        status: 0,
        items: []
      });
      generateOrderNo();
      // 清除缓存
      selectedProductIdsCache.value.clear();
      generateDefaultBatchNo();
    }
  }
);

// 监听相关字段变化，重新生成批次号
watch(() => formData.relatedOrderNo, (newVal) => {
  if (newVal) {
    generateDefaultBatchNo();
  }
});

watch(() => formData.supplierId, (newVal) => {
  if (newVal) {
    generateDefaultBatchNo();
  }
});
</script>

<style scoped>
/* 样式保持不变，与之前相同 */
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

.inbound-form {
  margin-bottom: 30px;
}

.product-section {
  margin: 30px 0;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
}

.section-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.product-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.auto-allocate-btn {
  margin-right: 8px;
}

.product-table {
  margin-bottom: 16px;
}

.allocation-btn {
  margin-top: 4px;
  font-size: 12px;
}

.shelf-allocation-summary {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.allocation-empty {
  margin-top: 8px;
}

.empty-text {
  color: #909399;
  font-size: 12px;
}

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

.summary-item .value.success {
  color: #67C23A;
}

.summary-item .value.warning {
  color: #E6A23C;
}

.summary-item .value.info {
  color: #409EFF;
}

.price-total {
  font-weight: bold;
  color: #409eff;
}

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

.clear-btn {
  margin-left: 8px;
}

.allocation-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 16px;
}

.table-container {
  width: 100%;
  overflow-x: auto;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.product-info-cell {
  min-height: 60px;
}

.product-details {
  margin-top: 8px;
  padding: 4px;
  background-color: #f8f9fa;
  border-radius: 4px;
  font-size: 12px;
}

.product-name {
  font-weight: 500;
  color: #303133;
  margin-bottom: 2px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-sku {
  color: #909399;
  font-size: 11px;
}

.cell-content {
  display: block;
  padding: 4px 0;
  word-break: break-word;
}

.shelf-location-cell {
  min-height: 70px;
}

.shelf-allocation-summary {
  max-height: 80px;
  overflow-y: auto;
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
}

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

.inbound-form {
  margin-bottom: 30px;
}

.product-section {
  margin: 30px 0;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
}

.section-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.product-table {
  margin-bottom: 16px;
}

.allocation-btn {
  margin-top: 4px;
  font-size: 12px;
}

.shelf-allocation-summary {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.shelf-tag {
  margin: 1px;
  font-size: 11px;
}

.allocation-empty {
  margin-top: 8px;
}

.empty-text {
  color: #909399;
  font-size: 12px;
}

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

.price-total {
  font-weight: bold;
  color: #409eff;
}

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

.clear-btn {
  margin-left: 8px;
}

.allocation-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 16px;
}

:deep(.el-table) {
  margin-top: 0;
}

:deep(.el-table .el-input-number) {
  width: 100%;
}

:deep(.el-table .el-input-number .el-input__inner) {
  text-align: center;
}

:deep(.el-select .el-tag) {
  margin: 2px;
}

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

.table-container {
  width: 100%;
  overflow-x: auto;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.product-info-cell {
  min-height: 60px;
}

.product-details {
  margin-top: 8px;
  padding: 4px;
  background-color: #f8f9fa;
  border-radius: 4px;
  font-size: 12px;
}

.product-name {
  font-weight: 500;
  color: #303133;
  margin-bottom: 2px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-sku {
  color: #909399;
  font-size: 11px;
}

.cell-content {
  display: block;
  padding: 4px 0;
  word-break: break-word;
}

.shelf-location-cell {
  min-height: 70px;
}

.shelf-allocation-summary {
  max-height: 80px;
  overflow-y: auto;
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

@media (max-width: 1200px) {
  .table-container {
    font-size: 12px;
  }
  
  .product-info-cell {
    min-height: 50px;
  }
}

@media (max-width: 768px) {
  .table-container {
    border: none;
  }
  
  .product-table {
    min-width: 1400px;
  }
  
  .section-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
}

:deep(.el-table .el-input .el-input__inner) {
  height: 32px;
  line-height: 32px;
}

:deep(.el-table .el-input-number) {
  width: 100%;
}

:deep(.el-table .el-input-number .el-input__inner) {
  text-align: center;
  padding-left: 8px;
  padding-right: 40px;
}

:deep(.el-table .el-textarea .el-textarea__inner) {
  min-height: 60px;
  font-size: 12px;
  line-height: 1.4;
}

:deep(.el-table .el-select .el-tag) {
  margin: 1px;
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
}

:deep(.el-table .el-table__fixed-right) {
  box-shadow: -2px 0 8px rgba(0, 0, 0, 0.1);
}

:deep(.el-table .el-table__fixed-left) {
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
}

:deep(.el-table .el-input .el-input__inner) {
  height: 32px;
  line-height: 32px;
  text-align: center;
  padding: 0 8px;
  width: 100%;
}

:deep(.el-table input[type="number"]::-webkit-outer-spin-button),
:deep(.el-table input[type="number"]::-webkit-inner-spin-button) {
  -webkit-appearance: none;
  margin: 0;
}

:deep(.el-table input[type="number"]) {
  -moz-appearance: textfield;
}

:deep(.el-table .el-input__prefix) {
  display: flex;
  align-items: center;
  height: 100%;
  left: 8px;
  pointer-events: none;
}


/* 批次号单元格样式 */
.batch-no-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.batch-input {
  margin-bottom: 4px;
}

.copy-batch-btn {
  align-self: flex-start;
  font-size: 12px;
  padding: 0;
  height: 20px;
}

/* 批量操作样式 */
.batch-operations {
  display: flex;
  flex-direction: column;
  gap: 4px;
  align-items: center;
}

/* 响应式调整 */
@media (max-width: 1200px) {
  .batch-operations {
    flex-direction: row;
    justify-content: center;
  }
  
  .copy-batch-btn {
    font-size: 11px;
  }
}

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

</style>