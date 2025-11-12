<template>
  <div class="outbound-create-container">
    <el-card class="form-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">{{ isEditMode ? '编辑出库单' : '新建出库单' }}</span>
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
        class="outbound-form"
      >
        <el-row :gutter="24">
          <el-col :xs="24" :sm="12" :lg="8">
            <el-form-item label="出库单号" prop="orderNo">
              <el-input v-model="formData.orderNo" placeholder="系统自动生成" disabled />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :lg="8">
            <el-form-item label="出库类型" prop="orderType">
              <el-select
                v-model="formData.orderType"
                placeholder="请选择出库类型"
                style="width: 100%"
                @change="handleOrderTypeChange"
              >
                <el-option
                  v-for="item in orderTypeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :lg="8">
            <el-form-item label="出库仓库" prop="warehouseId">
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
            <el-form-item label="客户" prop="customerId" v-if="showCustomer">
              <el-select
                v-model="formData.customerId"
                placeholder="请选择客户"
                style="width: 100%"
                filterable
              >
                <el-option
                  v-for="customer in customerList"
                  :key="customer.id"
                  :label="customer.customerName"
                  :value="customer.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :lg="8">
            <el-form-item label="预计出库日期" prop="expectedDate">
              <el-date-picker
                v-model="formData.expectedDate"
                type="date"
                placeholder="选择预计出库日期"
                style="width: 100%"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :lg="8">
            <el-form-item label="关联单号" prop="relatedOrderNo">
              <el-input
                v-model="formData.relatedOrderNo"
                placeholder="请输入关联单号"
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

      <!-- 产品明细 -->
      <div class="product-section">
        <div class="section-header">
          <h3>产品明细</h3>
          <el-button 
            type="primary" 
            @click="handleAddProduct"
            :disabled="!formData.warehouseId"
          >
            <el-icon><Plus /></el-icon>
            添加产品
          </el-button>
        </div>

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
                :disabled="!formData.warehouseId"
              >
                <el-option
                  v-for="product in availableProducts"
                  :key="product.id"
                  :label="`${product.sku} - ${product.name} (库存: ${productStockMap[product.id] || 0})`"
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
          <el-table-column label="当前库存" width="100" align="center">
            <template #default="{ row }">
              <span :class="getStockClass(getCurrentStock(row), row.quantity)">
                {{ getCurrentStock(row) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="出库数量" width="120">
            <template #default="{ row, $index }">
              <el-input-number
                v-model="row.quantity"
                :min="1"
                :max="getMaxQuantity(row)"
                controls-position="right"
                style="width: 100%"
                @change="() => handleQuantityChange($index)"
                :disabled="!getCurrentStock(row) || getCurrentStock(row) <= 0"
              />
            </template>
          </el-table-column>
          <el-table-column label="批次分配" min-width="200">
            <template #default="{ row, $index }">
              <div class="batch-allocation">
                <el-button 
                  type="primary" 
                  link 
                  @click="openBatchDialog($index)"
                  :disabled="!row.productId"
                >
                  分配批次
                </el-button>
                <div v-if="row.batchAllocations && row.batchAllocations.length > 0" class="batch-summary">
                  <el-tag
                    v-for="allocation in row.batchAllocations"
                    :key="allocation.batchNo"
                    size="small"
                    class="batch-tag"
                  >
                    {{ allocation.batchNo }}: {{ allocation.quantity }}个
                  </el-tag>
                </div>
                <div v-else class="batch-empty">
                  <span class="empty-text">未分配批次</span>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="单价" width="120">
            <template #default="{ row }">
              <el-input-number
                v-model="row.price"
                :min="0"
                :precision="2"
                controls-position="right"
                style="width: 100%"
              >
                <template #prefix>¥</template>
              </el-input-number>
            </template>
          </el-table-column>
          <el-table-column label="金额" width="120" align="right">
            <template #default="{ row }">
              <span>¥ {{ ((row.price || 0) * (row.quantity || 0)).toFixed(2) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="备注" min-width="150">
            <template #default="{ row }">
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
                @click="handleRemoveProduct($index)"
              >
                <el-icon><Delete /></el-icon>
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
                <span class="label">总数量：</span>
                <span class="value">{{ totalQuantity }} 个</span>
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
                <span class="label">库存状态：</span>
                <span class="value" :class="stockStatusClass">
                  {{ stockStatusText }}
                </span>
              </div>
            </el-col>
          </el-row>
        </div>
      </div>

      <!-- 附件上传 -->
      <div class="attachment-section">
        <div class="section-header">
          <h3>附件上传</h3>
          <span class="tip">支持图片、文档等格式，单个文件不超过10MB</span>
        </div>
        <el-upload
          v-model:file-list="fileList"
          action="/api/auth/file/upload"
          multiple
          :limit="5"
          :on-exceed="handleExceed"
          :before-upload="beforeUpload"
          :on-success="handleUploadSuccess"
          :on-remove="handleRemoveFile"
          list-type="text"
        >
          <el-button type="primary">
            <el-icon><Upload /></el-icon>
            上传文件
          </el-button>
          <template #tip>
            <div class="el-upload__tip">
              支持 jpg, png, pdf, doc, docx, xls, xlsx 格式文件
            </div>
          </template>
        </el-upload>
      </div>
    </el-card>

    <!-- 批次分配对话框 -->
    <el-dialog
      v-model="batchDialog.visible"
      :title="`批次分配 - ${batchDialog.productName}`"
      width="600px"
      destroy-on-close
    >
      <div class="batch-dialog-content">
        <div class="batch-info">
          <div class="info-item">
            <span class="label">总出库数量：</span>
            <span class="value">{{ batchDialog.totalQuantity }}</span>
          </div>
          <div class="info-item">
            <span class="label">已分配数量：</span>
            <span class="value" :class="batchDialog.allocatedQuantity >= batchDialog.totalQuantity ? 'success' : 'warning'">
              {{ batchDialog.allocatedQuantity }}
            </span>
          </div>
          <div class="info-item">
            <span class="label">剩余数量：</span>
            <span class="value">{{ batchDialog.remainingQuantity }}</span>
          </div>
        </div>

        <el-table :data="batchDialog.batches" border class="batch-table">
          <el-table-column label="批次号" prop="batchNo" width="150" />
          <el-table-column label="可用数量" width="100" align="center">
            <template #default="{ row }">
              <span>{{ row.quantity }}</span>
            </template>
          </el-table-column>
          <el-table-column label="分配数量" width="150">
            <template #default="{ row, $index }">
              <el-input-number
                v-model="row.allocated"
                :min="0"
                :max="row.maxAllocatable"
                controls-position="right"
                style="width: 100%"
                @change="() => handleBatchAllocationChange($index)"
              />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80" align="center">
            <template #default="{ row, $index }">
              <el-button
                type="danger"
                link
                @click="clearBatchAllocation($index)"
                :disabled="!row.allocated"
              >
                清空
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="batch-actions">
          <el-button @click="autoAllocateBatches" :disabled="batchDialog.remainingQuantity <= 0">
            自动分配
          </el-button>
          <el-button type="primary" @click="confirmBatchAllocation">
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
import { Plus, Delete, Upload } from '@element-plus/icons-vue';
import { post, get } from '@/net';

const router = useRouter();
const route = useRoute();
const formRef = ref();
const loading = ref(false);

// 判断是否是编辑模式
const isEditMode = computed(() => {
  return !!route.params.id;
});

// 表单数据
const formData = reactive({
  id: null,
  orderNo: '',
  orderType: 1,
  warehouseId: null,
  customerId: null,
  expectedDate: '',
  relatedOrderNo: '',
  remark: '',
  status: 0,
  items: [],
  attachments: []
});

// 批次分配对话框数据
const batchDialog = reactive({
  visible: false,
  productIndex: -1,
  productName: '',
  totalQuantity: 0,
  batches: [],
  allocatedQuantity: 0,
  remainingQuantity: 0
});

// 选项数据
const warehouseList = ref([]);
const customerList = ref([]);
const inventoryList = ref([]);
const fileList = ref([]);

// 产品库存映射表
const productStockMap = ref({});

// 出库类型选项
const orderTypeOptions = [
  { value: 1, label: '销售出库' },
  { value: 2, label: '生产领料' },
  { value: 3, label: '退货出库' },
  { value: 4, label: '调拨出库' },
  { value: 5, label: '其他出库' }
];

// 计算属性
const showCustomer = computed(() => {
  return formData.orderType === 1;
});

const availableProducts = computed(() => {
  if (!formData.warehouseId) return [];
  
  return inventoryList.value
    .filter(item => item.availableQuantity > 0)
    .map(item => ({
      id: item.productId,
      sku: item.sku,
      name: item.productName,
      spec: item.spec,
      unit: item.unitName,
      quantity: item.availableQuantity
    }));
});

const totalQuantity = computed(() => {
  return formData.items.reduce((sum, item) => sum + (item.quantity || 0), 0);
});

const totalAmount = computed(() => {
  return formData.items.reduce((sum, item) => {
    const price = item.price || 0;
    const quantity = item.quantity || 0;
    return sum + (price * quantity);
  }, 0);
});

const hasInsufficientStock = computed(() => {
  return formData.items.some(item => {
    const allocatedQuantity = item.batchAllocations 
      ? item.batchAllocations.reduce((sum, alloc) => sum + (alloc.quantity || 0), 0)
      : 0;
    return (item.quantity || 0) > allocatedQuantity;
  });
});

const stockStatusText = computed(() => {
  return hasInsufficientStock.value ? '批次分配不足' : '分配完成';
});

const stockStatusClass = computed(() => {
  return hasInsufficientStock.value ? 'status-warning' : 'status-success';
});

// 表单验证规则
const formRules = {
  orderType: [
    { required: true, message: '请选择出库类型', trigger: 'change' }
  ],
  warehouseId: [
    { required: true, message: '请选择出库仓库', trigger: 'change' }
  ],
  customerId: [
    { 
      required: true, 
      message: '请选择客户', 
      trigger: 'change',
      validator: (rule, value, callback) => {
        if (showCustomer.value && !value) {
          callback(new Error('请选择客户'));
        } else {
          callback();
        }
      }
    }
  ],
  expectedDate: [
    { required: true, message: '请选择预计出库日期', trigger: 'change' }
  ]
};

// 加载出库单详情
const loadOutboundDetail = async (id) => {
  loading.value = true;
  try {
    const res = await get(`/api/auth/outbound/detail?orderId=${id}`);
    console.log('出库单详情响应:', res);
    
    if (res) {
      const detailData = res.data || res;
      
      // 设置基本数据
      Object.assign(formData, {
        id: detailData.id,
        orderNo: detailData.orderNo,
        orderType: detailData.orderType,
        warehouseId: detailData.warehouseId,
        customerId: detailData.customerId,
        expectedDate: detailData.expectedDate,
        relatedOrderNo: detailData.relatedOrderNo || '',
        remark: detailData.remark || '',
        status: detailData.status
      });

      // 如果仓库有值，先加载对应的库存数据
      if (detailData.warehouseId) {
        await loadInventoryData(detailData.warehouseId);
      }

      // 设置产品明细数据
      if (detailData.items && detailData.items.length > 0) {
        formData.items = detailData.items.map(item => {
          // 使用库存映射中的数据，如果不存在则使用原始数据
          const currentStock = productStockMap.value[item.productId] || item.currentStock || 0;
          
          return {
            productId: item.productId,
            productName: item.productName || '',
            sku: item.sku || '',
            spec: item.spec || '',
            unit: item.unit || '',
            currentStock: currentStock, // 使用正确的库存数据
            quantity: item.quantity || 1,
            price: item.price || 0,
            batchAllocations: item.batchAllocations || [],
            availableBatches: item.availableBatches || [],
            remark: item.remark || ''
          };
        });

        // 为每个产品加载批次信息
        for (let i = 0; i < formData.items.length; i++) {
          const item = formData.items[i];
          if (item.productId && formData.warehouseId) {
            await loadBatchInfo(item.productId, formData.warehouseId, i);
          }
        }
      } else {
        formData.items = [];
      }

      // 设置附件数据
      if (detailData.attachments && detailData.attachments.length > 0) {
        fileList.value = detailData.attachments.map(att => ({
          name: att.fileName,
          url: att.filePath,
          status: 'success'
        }));
        formData.attachments = detailData.attachments;
      }
      
      ElMessage.success('数据加载成功');
    }
  } catch (error) {
    console.error('加载出库单详情失败:', error);
    ElMessage.error('加载数据失败');
    router.back();
  } finally {
    loading.value = false;
  }
};

// 获取当前库存数量
const getCurrentStock = (row) => {
  if (row.productId && productStockMap.value[row.productId] !== undefined) {
    return productStockMap.value[row.productId];
  }
  return row.currentStock || 0;
};

// 获取最大可出库数量
const getMaxQuantity = (row) => {
  const currentStock = getCurrentStock(row);
  return currentStock > 0 ? currentStock : 1;
};

// 方法
const generateOrderNo = () => {
  const date = new Date();
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const random = String(Math.random()).substr(2, 6);
  formData.orderNo = `CK${year}${month}${day}${random}`;
};

const handleOrderTypeChange = (value) => {
  if (value !== 1) {
    formData.customerId = null;
  }
};

const handleWarehouseChange = async (warehouseId) => {
  if (warehouseId) {
    await loadInventoryData(warehouseId);
    // 如果不是编辑模式，清空产品列表
    if (!isEditMode.value) {
      formData.items = [];
    }
  }
};

const handleAddProduct = () => {
  formData.items.push({
    productId: null,
    productName: '',
    sku: '',
    spec: '',
    unit: '',
    currentStock: 0,
    quantity: 1,
    price: 0,
    batchAllocations: [],
    availableBatches: [],
    remark: ''
  });
};

const handleRemoveProduct = (index) => {
  formData.items.splice(index, 1);
};

const handleProductChange = async (productId, index) => {
  const product = availableProducts.value.find(p => p.id === productId);
  if (product) {
    const item = formData.items[index];
    item.productId = product.id;
    item.productName = product.name;
    item.sku = product.sku;
    item.spec = product.spec;
    item.unit = product.unit;
    
    // 统一从库存映射中获取库存数量
    item.currentStock = productStockMap.value[productId] || 0;
    item.quantity = item.quantity || 1;
    item.price = item.price || 0;
    item.batchAllocations = [];
    
    // 加载批次信息
    await loadBatchInfo(productId, formData.warehouseId, index);
  }
};

const handleQuantityChange = (index) => {
  const item = formData.items[index];
  // 如果数量减少，需要调整批次分配
  if (item.batchAllocations && item.batchAllocations.length > 0) {
    const totalAllocated = item.batchAllocations.reduce((sum, alloc) => sum + alloc.quantity, 0);
    if (item.quantity < totalAllocated) {
      ElMessage.warning('出库数量小于已分配批次数量，请重新分配批次');
      item.batchAllocations = [];
    }
  }
};

const getStockClass = (currentStock, quantity) => {
  if (!currentStock || currentStock <= 0) return 'stock-none';
  if (quantity > currentStock) return 'stock-insufficient';
  if (currentStock < 10) return 'stock-low';
  return 'stock-sufficient';
};

// 批次分配相关方法
const openBatchDialog = async (index) => {
  const item = formData.items[index];
  if (!item.productId) {
    ElMessage.warning('请先选择产品');
    return;
  }

  batchDialog.productIndex = index;
  batchDialog.productName = item.productName;
  batchDialog.totalQuantity = item.quantity;
  
  // 准备批次数据
  batchDialog.batches = item.availableBatches.map(batch => ({
    ...batch,
    allocated: 0,
    maxAllocatable: batch.quantity
  }));

  // 恢复已分配的批次数据
  if (item.batchAllocations && item.batchAllocations.length > 0) {
    item.batchAllocations.forEach(allocation => {
      const batch = batchDialog.batches.find(b => b.batchNo === allocation.batchNo);
      if (batch) {
        batch.allocated = allocation.quantity;
      }
    });
  }

  updateBatchDialogCalculations();
  batchDialog.visible = true;
};

const handleBatchAllocationChange = () => {
  updateBatchDialogCalculations();
};

const clearBatchAllocation = (batchIndex) => {
  batchDialog.batches[batchIndex].allocated = 0;
  updateBatchDialogCalculations();
};

const updateBatchDialogCalculations = () => {
  batchDialog.allocatedQuantity = batchDialog.batches.reduce((sum, batch) => sum + (batch.allocated || 0), 0);
  batchDialog.remainingQuantity = batchDialog.totalQuantity - batchDialog.allocatedQuantity;
};

const autoAllocateBatches = () => {
  let remaining = batchDialog.remainingQuantity;
  
  // 按批次顺序自动分配
  for (const batch of batchDialog.batches) {
    if (remaining <= 0) break;
    
    const available = batch.maxAllocatable - (batch.allocated || 0);
    const allocate = Math.min(available, remaining);
    
    if (allocate > 0) {
      batch.allocated = (batch.allocated || 0) + allocate;
      remaining -= allocate;
    }
  }
  
  updateBatchDialogCalculations();
  
  if (remaining > 0) {
    ElMessage.warning(`库存不足，仍有 ${remaining} 个无法分配`);
  }
};

const confirmBatchAllocation = () => {
  if (batchDialog.allocatedQuantity !== batchDialog.totalQuantity) {
    ElMessage.warning(`分配数量 (${batchDialog.allocatedQuantity}) 与出库数量 (${batchDialog.totalQuantity}) 不一致`);
    return;
  }

  // 保存批次分配
  const item = formData.items[batchDialog.productIndex];
  item.batchAllocations = batchDialog.batches
    .filter(batch => batch.allocated > 0)
    .map(batch => ({
      batchNo: batch.batchNo,
      quantity: batch.allocated,
      price: item.price || 0
    }));

  batchDialog.visible = false;
  ElMessage.success('批次分配完成');
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
      // 编辑模式下重新加载数据
      loadOutboundDetail(route.params.id);
    } else {
      // 创建模式下清空表单
      formRef.value?.resetFields();
      formData.items = [];
      fileList.value = [];
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
      totalQuantity: totalQuantity.value,
      totalAmount: totalAmount.value
    };
    
    const url = isEditMode.value ? '/api/auth/outbound/update' : '/api/auth/outbound/create';
    const res = await post(url, submitData);
    if (res) {
      ElMessage.success(isEditMode.value ? '更新草稿成功' : '保存草稿成功');
      router.push('/index/ckOutboundManage');
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
  
  // 检查批次分配
  const hasUnallocatedItems = formData.items.some(item => {
    const allocatedQuantity = item.batchAllocations 
      ? item.batchAllocations.reduce((sum, alloc) => sum + (alloc.quantity || 0), 0)
      : 0;
    return (item.quantity || 0) !== allocatedQuantity;
  });
  
  if (hasUnallocatedItems) {
    ElMessage.warning('存在未完成批次分配的产品，请完成批次分配后再提交');
    return;
  }
  
  loading.value = true;
  try {
    const submitData = {
      ...formData,
      status: 1,
      totalQuantity: totalQuantity.value,
      totalAmount: totalAmount.value
    };
    
    const url = isEditMode.value ? '/api/auth/outbound/update' : '/api/auth/outbound/create';
    const res = await post(url, submitData);
    if (res) {
      ElMessage.success(isEditMode.value ? '更新成功' : '提交成功，等待审核');
      router.push('/index/ckOutboundManage');
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
      if (!item.quantity || item.quantity <= 0) {
        ElMessage.warning(`请输入第 ${i + 1} 行产品的有效数量`);
        return false;
      }
      
      const currentStock = getCurrentStock(item);
      if (item.quantity > currentStock) {
        ElMessage.warning(`第 ${i + 1} 行产品出库数量超过库存 (当前库存: ${currentStock})`);
        return false;
      }
    }
    
    return true;
  } catch (error) {
    ElMessage.warning('请完善表单信息');
    return false;
  }
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

const loadCustomerList = async () => {
  try {
    const res = await get('/api/auth/customer/listEnable');
    customerList.value = res || [];
  } catch (error) {
    ElMessage.error('加载客户列表失败');
  }
};

const loadInventoryData = async (warehouseId) => {
  try {
    const res = await get(`/api/auth/inventory/listOfWarehouse?warehouseId=${warehouseId}`);
    inventoryList.value = res || [];
    
    // 构建产品库存映射
    productStockMap.value = {};
    inventoryList.value.forEach(item => {
      productStockMap.value[item.productId] = item.availableQuantity;
    });
    
    console.log('库存映射表:', productStockMap.value);
  } catch (error) {
    ElMessage.error('加载库存数据失败');
  }
};

const loadBatchInfo = async (productId, warehouseId, index) => {
  try {
    const res = await get(`/api/auth/inventory/batches?productId=${productId}&warehouseId=${warehouseId}`);
    formData.items[index].availableBatches = res || [];
  } catch (error) {
    console.error('加载批次信息失败:', error);
    formData.items[index].availableBatches = [];
  }
};

// 文件上传相关方法
const handleExceed = () => {
  ElMessage.warning('最多只能上传5个文件');
};

const beforeUpload = (file) => {
  const isLt10M = file.size / 1024 / 1024 < 10;
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过10MB');
    return false;
  }
  return true;
};

const handleUploadSuccess = (response, file) => {
  formData.attachments.push({
    fileName: file.name,
    filePath: response.data,
    fileSize: file.size,
    fileType: file.type
  });
};

const handleRemoveFile = (file) => {
  const index = formData.attachments.findIndex(att => att.fileName === file.name);
  if (index > -1) {
    formData.attachments.splice(index, 1);
  }
};

onMounted(() => {
  if (isEditMode.value) {
    // 编辑模式，加载数据
    loadOutboundDetail(route.params.id);
  } else {
    // 创建模式，生成单号
    generateOrderNo();
  }
  loadWarehouseList();
  loadCustomerList();
});

// 监听路由变化，处理直接通过URL进入的情况
watch(
  () => route.params.id,
  (newId) => {
    if (newId) {
      loadOutboundDetail(newId);
    } else {
      // 从编辑模式切换到创建模式
      Object.assign(formData, {
        id: null,
        orderNo: '',
        orderType: 1,
        warehouseId: null,
        customerId: null,
        expectedDate: '',
        relatedOrderNo: '',
        remark: '',
        status: 0,
        items: [],
        attachments: []
      });
      fileList.value = [];
      productStockMap.value = {};
      generateOrderNo();
    }
  }
);

// 监听仓库变化，重新加载库存数据
watch(
  () => formData.warehouseId,
  (newWarehouseId) => {
    if (newWarehouseId) {
      loadInventoryData(newWarehouseId);
    }
  }
);
</script>

<style scoped>
.outbound-create-container {
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

.outbound-form {
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

.tip {
  font-size: 12px;
  color: #909399;
}

.product-table {
  margin-bottom: 16px;
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

.status-success {
  color: #67C23A;
}

.status-warning {
  color: #E6A23C;
}

/* 库存状态样式 */
.stock-none {
  color: #909399;
}

.stock-sufficient {
  color: #67C23A;
}

.stock-low {
  color: #E6A23C;
}

.stock-insufficient {
  color: #F56C6C;
  font-weight: bold;
}

.attachment-section {
  margin-top: 30px;
}

/* 批次分配样式 */
.batch-allocation {
  min-height: 40px;
}

.batch-summary {
  margin-top: 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.batch-tag {
  margin: 2px;
}

.batch-empty {
  margin-top: 8px;
}

.empty-text {
  color: #909399;
  font-size: 12px;
}

/* 批次对话框样式 */
.batch-dialog-content {
  padding: 0 10px;
}

.batch-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16px;
  padding: 12px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.info-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.info-item .label {
  font-size: 12px;
  color: #606266;
  margin-bottom: 4px;
}

.info-item .value {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.info-item .value.success {
  color: #67C23A;
}

.info-item .value.warning {
  color: #E6A23C;
}

.batch-table {
  margin: 16px 0;
}

.batch-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 16px;
}

:deep(.el-upload) {
  margin-right: 12px;
}

:deep(.el-upload-list) {
  margin-top: 12px;
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

/* 响应式设计 */
@media (max-width: 768px) {
  .outbound-create-container {
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
  
  .batch-info {
    flex-direction: column;
    gap: 8px;
  }
  
  .info-item {
    flex-direction: row;
    justify-content: space-between;
  }
}
</style>