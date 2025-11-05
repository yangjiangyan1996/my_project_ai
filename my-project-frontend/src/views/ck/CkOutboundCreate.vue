<template>
  <div class="outbound-create-container">
    <el-card class="form-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">新建出库单</span>
          <div class="header-actions">
            <el-button @click="handleReset">重置</el-button>
            <el-button type="primary" @click="handleSaveDraft" :loading="loading">
              保存草稿
            </el-button>
            <el-button type="primary" @click="handleSubmit" :loading="loading">
              提交审核
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
          <el-button type="primary" @click="handleAddProduct">
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
                  :label="`${product.sku} - ${product.name} (库存: ${product.quantity})`"
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
              <span :class="getStockClass(row.currentStock, row.quantity)">
                {{ row.currentStock || 0 }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="出库数量" width="120">
            <template #default="{ row, $index }">
              <el-input-number
                v-model="row.quantity"
                :min="1"
                :max="row.currentStock || 0"
                controls-position="right"
                style="width: 100%"
                @change="() => calculateTotal()"
                :disabled="!row.currentStock"
              />
            </template>
          </el-table-column>
          <el-table-column label="批次号" width="150">
            <template #default="{ row }">
              <el-select
                v-model="row.batchNo"
                placeholder="选择批次"
                style="width: 100%"
                :disabled="!row.productId"
                @change="(value) => handleBatchChange(value, $index)"
              >
                <el-option
                  v-for="batch in row.availableBatches"
                  :key="batch.batchNo"
                  :label="`${batch.batchNo} (${batch.quantity}个)`"
                  :value="batch.batchNo"
                />
              </el-select>
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
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Delete, Upload } from '@element-plus/icons-vue';
import { post, get } from '@/net';

const router = useRouter();
const formRef = ref();
const loading = ref(false);

// 表单数据
const formData = reactive({
  orderNo: '',
  orderType: 1,
  warehouseId: null,
  customerId: null,
  expectedDate: '',
  relatedOrderNo: '',
  remark: '',
  items: [],
  attachments: []
});

// 选项数据
const warehouseList = ref([]);
const customerList = ref([]);
const inventoryList = ref([]);
const fileList = ref([]);

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
  return formData.orderType === 1; // 只有销售出库显示客户
});

const availableProducts = computed(() => {
  if (!formData.warehouseId) return [];
  return inventoryList.value
    .filter(item => item.warehouseId === formData.warehouseId && item.quantity > 0)
    .map(item => ({
      id: item.productId,
      sku: item.productSku,
      name: item.productName,
      spec: item.productSpec,
      unit: item.productUnit,
      quantity: item.quantity
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
  return formData.items.some(item => (item.quantity || 0) > (item.currentStock || 0));
});

const stockStatusText = computed(() => {
  return hasInsufficientStock.value ? '库存不足' : '库存充足';
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
    // 清空已选择的产品
    formData.items = [];
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
    batchNo: '',
    availableBatches: [],
    remark: ''
  });
};

const handleRemoveProduct = (index) => {
  formData.items.splice(index, 1);
  calculateTotal();
};

const handleProductChange = (productId, index) => {
  const product = availableProducts.value.find(p => p.id === productId);
  if (product) {
    const item = formData.items[index];
    item.productName = product.name;
    item.sku = product.sku;
    item.spec = product.spec;
    item.unit = product.unit;
    item.currentStock = product.quantity;
    item.quantity = 1;
    item.price = 0;
    
    // 加载批次信息
    loadBatchInfo(productId, formData.warehouseId, index);
  }
};

const handleBatchChange = (batchNo, index) => {
  const item = formData.items[index];
  const batch = item.availableBatches.find(b => b.batchNo === batchNo);
  if (batch) {
    item.currentStock = batch.quantity;
    // 如果当前数量超过批次库存，自动调整
    if (item.quantity > batch.quantity) {
      item.quantity = batch.quantity;
    }
  }
};

const getStockClass = (currentStock, quantity) => {
  if (!currentStock) return 'stock-none';
  if (quantity > currentStock) return 'stock-insufficient';
  if (currentStock < 10) return 'stock-low';
  return 'stock-sufficient';
};

const calculateTotal = () => {
  // 触发响应式更新
};

const handleReset = () => {
  ElMessageBox.confirm('确定要重置表单吗？所有输入的数据将会丢失。', '重置确认', {
    type: 'warning'
  }).then(() => {
    formRef.value?.resetFields();
    formData.items = [];
    fileList.value = [];
    generateOrderNo();
    ElMessage.success('表单已重置');
  });
};

const handleSaveDraft = async () => {
  if (!await validateForm()) return;
  
  loading.value = true;
  try {
    const submitData = {
      ...formData,
      status: 0, // 草稿状态
      totalQuantity: totalQuantity.value,
      totalAmount: totalAmount.value
    };
    
    const res = await post('/api/auth/outbound/saveDraft', submitData);
    if (res) {
      ElMessage.success('保存草稿成功');
      router.push('/outbound/list');
    }
  } catch (error) {
    ElMessage.error('保存草稿失败');
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
  
  if (hasInsufficientStock.value) {
    ElMessage.warning('存在库存不足的产品，请调整出库数量');
    return;
  }
  
  loading.value = true;
  try {
    const submitData = {
      ...formData,
      status: 1, // 待审核状态
      totalQuantity: totalQuantity.value,
      totalAmount: totalAmount.value
    };
    
    const res = await post('/api/auth/outbound/submit', submitData);
    if (res) {
      ElMessage.success('提交成功，等待审核');
      router.push('/outbound/list');
    }
  } catch (error) {
    ElMessage.error('提交失败');
  } finally {
    loading.value = false;
  }
};

const validateForm = async () => {
  if (!formRef.value) return false;
  
  try {
    await formRef.value.validate();
    
    // 验证产品明细
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
      if (item.quantity > item.currentStock) {
        ElMessage.warning(`第 ${i + 1} 行产品出库数量超过库存`);
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
  } catch (error) {
    ElMessage.error('加载库存数据失败');
  }
};

const loadBatchInfo = async (productId, warehouseId, index) => {
  try {
    const res = await get(`/api/auth/inventory/batches?productId=${productId}&warehouseId=${warehouseId}`);
    formData.items[index].availableBatches = res.records || [];
  } catch (error) {
    console.error('加载批次信息失败:', error);
    formData.items[index].availableBatches = [];
  }
};

// 文件上传相关
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
  generateOrderNo();
  loadWarehouseList();
  loadCustomerList();
});
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
}
</style>