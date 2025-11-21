<!-- 调拨入库， ORDER_TYPE =4 -->
 <template>
  <div class="inbound-create-container">
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
            <el-form-item label="采购单号" prop="relatedOrderNo">
              <el-input
                v-model="formData.relatedOrderNo"
                placeholder="请输入采购单号"
              />
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

      <!-- 产品明细 - 与原来相同的代码 -->
      <div class="product-section">
        <div class="section-header">
          <h3>产品明细</h3>
          <el-button type="primary" @click="handleAddProduct" :icon="Plus">
            添加产品
          </el-button>
        </div>

        <el-table
          :data="formData.items"
          border
          class="product-table"
          empty-text="请添加产品明细"
        >
          <!-- 表格列定义与原来相同 -->
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
          <!-- 其他列保持不变 -->
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
          <el-table-column label="实际数量" width="120">
            <template #default="{ row, $index }">
              <el-input-number
                v-model="row.actualQuantity"
                :min="1"
                :precision="0"
                controls-position="right"
                style="width: 100%"
                @change="(value) => handleActualQuantityChange(value, $index)"
              />
            </template>
          </el-table-column>
          <el-table-column label="单价" width="120">
            <template #default="{ row, $index }">
              <el-input-number
                v-model="row.priceUnit"
                :min="0"
                :precision="4"
                :step="0.01"
                controls-position="right"
                style="width: 100%"
                @change="() => calculateItemTotal($index)"
              >
                <template #prefix>¥</template>
              </el-input-number>
            </template>
          </el-table-column>
          <el-table-column label="总价" width="120" align="right">
            <template #default="{ row }">
              <span class="price-total">¥ {{ (row.priceTotal || 0).toFixed(2) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="批次号" width="150">
            <template #default="{ row, $index }">
              <el-input
                v-model="row.batchNo"
                placeholder="批次号"
                @blur="() => validateBatchNo(row.batchNo, $index)"
              />
            </template>
          </el-table-column>
          <el-table-column label="货架位置" width="200">
            <template #default="{ row, $index }">
              <el-select
                v-model="row.shelfLocationIds"
                placeholder="选择位置"
                style="width: 100%"
                filterable
                multiple
                collapse-tags
                collapse-tags-tooltip
                :max-collapse-tags="2"
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
            </template>
          </el-table-column>
          <el-table-column label="货架分配" width="150">
            <template #default="{ row }">
              <div v-if="row.shelfAllocations && row.shelfAllocations.length > 0" class="shelf-allocation-summary">
                <el-tag
                  v-for="allocation in row.shelfAllocations"
                  :key="allocation.shelfLocationId"
                  size="small"
                  class="shelf-tag"
                >
                  {{ getShelfName(allocation.shelfLocationId) }}: {{ allocation.quantity }}
                </el-tag>
              </div>
              <div v-else class="allocation-empty">
                <span class="empty-text">未分配</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="备注" min-width="150">
            <template #default="{ row, $index }">
              <el-input
                v-model="row.remark"
                placeholder="产品备注"
                maxlength="100"
                show-word-limit
              />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80" fixed="right" align="center">
            <template #default="{ $index }">
              <el-button
                type="danger"
                link
                :icon="Delete"
                @click="handleRemoveProduct($index)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

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
import { ref, reactive, computed, onMounted, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Delete } from '@element-plus/icons-vue';
import { post, get } from '@/net';

const router = useRouter();
const route = useRoute();
const formRef = ref();
const loading = ref(false);


const ORDER_TYPE = 4;
const orderTypeText = '调拨入库';

// 判断是否是编辑模式
const isEditMode = computed(() => {
  return !!route.params.id;
});

// 表单数据
const formData = reactive({
  id: null,
  orderNo: '',
  orderType: ORDER_TYPE, 
  warehouseId: null,
  supplierId: null,
  relatedOrderNo: '',
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
const supplierList = ref([]);
const productList = ref([]);
const shelfLocationList = ref([]);

// 计算属性
const totalActualQuantity = computed(() => {
  return formData.items.reduce((sum, item) => sum + (parseInt(item.actualQuantity) || 0), 0);
});

// 打开货架分配对话框
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
  
  // 准备货架数据
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

// 处理货架分配数量变化
const handleShelfAllocationChange = (value, changedIndex) => {
  const changedShelf = shelfAllocationDialog.shelves[changedIndex];
  const oldValue = changedShelf.allocatedQuantity;
  const newValue = parseInt(value) || 0;
  
  // 计算其他货架的总和
  const otherShelvesTotal = shelfAllocationDialog.shelves.reduce((sum, shelf, index) => {
    if (index !== changedIndex) {
      return sum + (parseInt(shelf.allocatedQuantity) || 0);
    }
    return sum;
  }, 0);
  
  // 计算新的总和
  const newTotal = otherShelvesTotal + newValue;
  
  // 如果新总和超过总数量，需要调整其他货架的数量
  if (newTotal > shelfAllocationDialog.totalQuantity) {
    const excess = newTotal - shelfAllocationDialog.totalQuantity;
    
    // 将超出的部分从其他货架中扣除
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
    
    // 如果还有剩余的超额，说明无法分配，恢复原值
    if (remainingExcess > 0) {
      changedShelf.allocatedQuantity = oldValue;
      ElMessage.warning(`分配数量不能超过总入库数量 ${shelfAllocationDialog.totalQuantity}`);
      return;
    }
  }
  
  changedShelf.allocatedQuantity = newValue;
  updateShelfAllocationCalculations();
};

// 清空单个货架分配
const clearShelfAllocation = (shelfIndex) => {
  shelfAllocationDialog.shelves[shelfIndex].allocatedQuantity = 0;
  updateShelfAllocationCalculations();
};

// 更新货架分配计算
const updateShelfAllocationCalculations = () => {
  shelfAllocationDialog.allocatedQuantity = shelfAllocationDialog.shelves.reduce(
    (sum, shelf) => sum + (parseInt(shelf.allocatedQuantity) || 0), 0
  );
  shelfAllocationDialog.remainingQuantity = Math.max(0, shelfAllocationDialog.totalQuantity - shelfAllocationDialog.allocatedQuantity);
};

// 获取货架最大分配数量
const getMaxShelfAllocation = (index) => {
  const otherShelvesTotal = shelfAllocationDialog.shelves.reduce((sum, shelf, i) => {
    if (i !== index) {
      return sum + (parseInt(shelf.allocatedQuantity) || 0);
    }
    return sum;
  }, 0);
  
  return Math.max(0, shelfAllocationDialog.totalQuantity - otherShelvesTotal);
};

// 自动分配货架数量
const autoAllocateShelves = () => {
  const totalQuantity = shelfAllocationDialog.totalQuantity;
  const shelfCount = shelfAllocationDialog.shelves.length;
  
  if (shelfCount === 0) return;
  
  // 重置所有分配数量为0
  shelfAllocationDialog.shelves.forEach(shelf => {
    shelf.allocatedQuantity = 0;
  });
  
  // 平均分配整数部分
  const baseAllocation = Math.floor(totalQuantity / shelfCount);
  let remaining = totalQuantity - (baseAllocation * shelfCount);
  
  // 分配基础数量
  shelfAllocationDialog.shelves.forEach(shelf => {
    shelf.allocatedQuantity = baseAllocation;
  });
  
  // 分配剩余数量
  for (let i = 0; i < remaining; i++) {
    if (i < shelfAllocationDialog.shelves.length) {
      shelfAllocationDialog.shelves[i].allocatedQuantity += 1;
    }
  }
  
  updateShelfAllocationCalculations();
};

// 确认货架分配
const confirmShelfAllocation = () => {
  const allocatedQuantity = shelfAllocationDialog.allocatedQuantity;
  const totalQuantity = shelfAllocationDialog.totalQuantity;
  
  if (allocatedQuantity !== totalQuantity) {
    ElMessage.warning(`分配数量 (${allocatedQuantity}) 与入库数量 (${totalQuantity}) 不一致`);
    return;
  }

  // 保存货架分配
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

// 处理实际数量变化
const handleActualQuantityChange = (value, index) => {
  const item = formData.items[index];
  const newQuantity = parseInt(value) || 1;
  
  // 确保数量是有效数字
  item.actualQuantity = newQuantity;
  
  // 如果实际数量减少，需要重新验证货架分配
  if (item.shelfAllocations && item.shelfAllocations.length > 0) {
    const totalAllocated = item.shelfAllocations.reduce((sum, alloc) => sum + (parseInt(alloc.quantity) || 0), 0);
    if (newQuantity < totalAllocated) {
      ElMessage.warning('实际数量小于已分配货架数量，请重新分配货架');
      item.shelfAllocations = [];
    }
  }
  
  // 重新计算总价
  calculateItemTotal(index);
};

// 计算单个产品的总价
const calculateItemTotal = (index) => {
  const item = formData.items[index];
  const quantity = parseInt(item.actualQuantity) || 0;
  const priceUnit = parseFloat(item.priceUnit) || 0;
  item.priceTotal = quantity * priceUnit;
};

// 验证批次号
const validateBatchNo = (batchNo, index) => {
  if (batchNo && !/^[A-Za-z0-9_-]+$/.test(batchNo)) {
    ElMessage.warning('批次号只能包含字母、数字、下划线和横线');
    formData.items[index].batchNo = '';
  }
};

const totalAmount = computed(() => {
  return formData.items.reduce((sum, item) => {
    return sum + (parseFloat(item.priceTotal) || 0);
  }, 0);
});

// 表单验证规则
const formRules = {
  orderType: [
    { required: true, message: '请选择入库类型', trigger: 'change' }
  ],
  warehouseId: [
    { required: true, message: '请选择入库仓库', trigger: 'change' }
  ],
  supplierId: [
    { required: true, message: '请选择供应商', trigger: 'change' }
  ]
};

// 所有方法都从原CkInboundCreate.vue复制过来，保持不变
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
    unit: '',
    quantity: 1,
    actualQuantity: 1,
    priceUnit: 0,
    priceTotal: 0,
    shelfLocationIds: [],
    shelfAllocations: [],
    batchNo: '',
    remark: ''
  });
};

const handleRemoveProduct = (index) => {
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

// 其他所有方法（openShelfAllocationDialog, handleShelfAllocationChange, calculateItemTotal等）
// 都从原CkInboundCreate.vue完整复制过来

// 初始化数据
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
              quantity: parseInt(item.actualQuantity) || 0
            }];
          }

          shelfAllocations = shelfAllocations.map(allocation => ({
            ...allocation,
            quantity: parseInt(allocation.quantity) || 0
          }));

          return {
            productId: item.productId,
            productName: item.productName || '',
            sku: item.sku || '',
            spec: item.spec || '',
            unit: item.unit || '',
            quantity: item.quantity || 1,
            actualQuantity: parseInt(item.actualQuantity) || 1,
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
      
      ElMessage.success('数据加载成功');
    }
  } catch (error) {
    ElMessage.error('加载数据失败');
    router.back();
  } finally {
    loading.value = false;
  }
};

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
      ElMessage.success('表单已重置');
    }
  });
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
      router.push('/index/ckInboundManage');
    }
  } catch (error) {
    ElMessage.error(isEditMode.value ? '更新失败' : '提交失败');
  } finally {
    loading.value = false;
  }
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

onMounted(() => {
  if (isEditMode.value) {
    loadInboundDetail(route.params.id);
  } else {
    generateOrderNo();
  }
  loadWarehouseList();
  loadSupplierList();
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
        orderType: ORDER_TYPE,
        warehouseId: null,
        supplierId: null,
        relatedOrderNo: '',
        remark: '',
        status: 0,
        items: []
      });
      generateOrderNo();
    }
  }
);
</script>

<style scoped>
/* 所有样式从原CkInboundCreate.vue复制过来 */
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
</style>