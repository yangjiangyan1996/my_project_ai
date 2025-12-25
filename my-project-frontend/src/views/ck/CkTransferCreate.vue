<template>
  <div class="transfer-create-container">
    <el-card class="form-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">新建调拨单</span>
          <div class="header-actions">
            <el-button @click="handleReset">重置</el-button>
            <el-button type="primary" @click="handleSaveDraft" :loading="loading">
              保存草稿
            </el-button>
            <!-- <el-button type="primary" @click="handleSubmit" :loading="loading">
              提交审核
            </el-button> -->
          </div>
        </div>
      </template>

      <!-- 基本信息 -->
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
        class="transfer-form"
      >
        <el-row :gutter="24">
          <el-col :xs="24" :sm="12" :lg="8">
            <el-form-item label="调拨单号" prop="orderNo">
              <el-input v-model="formData.orderNo" placeholder="系统自动生成" disabled />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :lg="8">
            <el-form-item label="调拨类型" prop="transferType">
              <el-select
                v-model="formData.transferType"
                placeholder="请选择调拨类型"
                style="width: 100%"
              >
                <el-option
                  v-for="item in transferTypeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :lg="8">
            <el-form-item label="预计调拨日期" prop="expectedDate">
              <el-date-picker
                v-model="formData.expectedDate"
                type="date"
                placeholder="选择预计调拨日期"
                style="width: 100%"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 仓库信息 -->
        <div class="warehouse-section">
          <el-row :gutter="24">
            <el-col :xs="24" :sm="12">
              <el-card class="warehouse-card" shadow="never">
                <template #header>
                  <div class="warehouse-header">
                    <el-icon><Location /></el-icon>
                    <span>调出仓库</span>
                  </div>
                </template>
                <el-form-item label="调出仓库" prop="fromWarehouseId">
                  <el-select
                    v-model="formData.fromWarehouseId"
                    placeholder="请选择调出仓库"
                    style="width: 100%"
                    filterable
                    @change="handleFromWarehouseChange"
                  >
                    <el-option
                      v-for="warehouse in warehouseList"
                      :key="warehouse.id"
                      :label="warehouse.name"
                      :value="warehouse.id"
                    />
                  </el-select>
                </el-form-item>
                <div class="warehouse-info" v-if="fromWarehouseInfo">
                  <p><strong>仓库地址：</strong>{{ fromWarehouseInfo.address || '暂无地址' }}</p>
                  <p><strong>负责人：</strong>{{ fromWarehouseInfo.managerName || '未指定' }}</p>
                </div>
              </el-card>
            </el-col>
            
            <el-col :xs="24" :sm="12">
              <el-card class="warehouse-card" shadow="never">
                <template #header>
                  <div class="warehouse-header">
                    <el-icon><Location /></el-icon>
                    <span>调入仓库</span>
                  </div>
                </template>
                <el-form-item label="调入仓库" prop="toWarehouseId">
                  <el-select
                    v-model="formData.toWarehouseId"
                    placeholder="请选择调入仓库"
                    style="width: 100%"
                    filterable
                    @change="handleToWarehouseChange"
                  >
                    <el-option
                      v-for="warehouse in availableToWarehouses"
                      :key="warehouse.id"
                      :label="warehouse.name"
                      :value="warehouse.id"
                    />
                  </el-select>
                </el-form-item>
                <div class="warehouse-info" v-if="toWarehouseInfo">
                  <p><strong>仓库地址：</strong>{{ toWarehouseInfo.address || '暂无地址' }}</p>
                  <p><strong>负责人：</strong>{{ toWarehouseInfo.managerName || '未指定' }}</p>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>

        <el-form-item label="调拨原因" prop="reason">
          <el-input
            v-model="formData.reason"
            type="textarea"
            :rows="3"
            placeholder="请输入调拨原因"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="备注" prop="remark">
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

      <!-- 产品明细 -->
      <div class="product-section">
        <div class="section-header">
          <h3>调拨产品明细</h3>
          <div class="section-actions">
            <el-button @click="handleBatchAdd" :disabled="!formData.fromWarehouseId">
              <el-icon><Collection /></el-icon>
              批量添加
            </el-button>
            <el-button type="primary" @click="handleAddProduct" :disabled="!formData.fromWarehouseId">
              <el-icon><Plus /></el-icon>
              添加产品
            </el-button>
          </div>
        </div>

        <el-table
          :data="formData.items"
          border
          class="product-table"
          empty-text="请添加调拨产品"
        >
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column label="产品信息" min-width="220">
            <template #default="{ row, $index }">
              <el-select
                v-model="row.productId"
                placeholder="选择产品"
                style="width: 100%"
                filterable
                @change="(value) => handleProductChange(value, $index)"
                :disabled="!formData.fromWarehouseId"
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
          <el-table-column label="调出库存" width="100" align="center">
            <template #default="{ row }">
              <span :class="getStockClass(row.currentStock, row.quantity)">
                {{ row.currentStock || 0 }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="调拨数量" width="120">
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
          <el-table-column label="调入库存" width="100" align="center">
            <template #default="{ row }">
              <span :class="getToStockClass(row.toWarehouseStock)">
                {{ row.toWarehouseStock || 0 }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="产品备注" min-width="150">
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
                <span class="label">调拨状态：</span>
                <span class="value" :class="transferStatusClass">
                  {{ transferStatusText }}
                </span>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="summary-item">
                <span class="label">库存检查：</span>
                <span class="value" :class="stockCheckClass">
                  {{ stockCheckText }}
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
          <span class="tip">支持调拨相关文件，单个文件不超过10MB</span>
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
              支持 jpg, png, pdf, doc, docx 格式文件
            </div>
          </template>
        </el-upload>
      </div>
    </el-card>

    <!-- 批量添加对话框 -->
    <el-dialog
      v-model="batchDialogVisible"
      title="批量添加产品"
      width="70%"
      top="5vh"
    >
      <BatchProductSelector
        :warehouse-id="formData.fromWarehouseId"
        :selected-products="selectedProductIds"
        @confirm="handleBatchSelect"
        @cancel="batchDialogVisible = false"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Delete, Upload, Location, Collection } from '@element-plus/icons-vue';
import { post, get } from '@/net';

const router = useRouter();
const formRef = ref();
const loading = ref(false);
const batchDialogVisible = ref(false);

// 表单数据
const formData = reactive({
  orderNo: '',
  transferType: 1,
  fromWarehouseId: null,
  toWarehouseId: null,
  expectedDate: '',
  reason: '',
  remark: '',
  items: [],
  attachments: []
});

// 选项数据
const warehouseList = ref([]);
const fromWarehouseInfo = ref(null);
const toWarehouseInfo = ref(null);
const fromInventoryList = ref([]);
const toInventoryList = ref([]);
const fileList = ref([]);

// 调拨类型选项
const transferTypeOptions = [
  { value: 1, label: '仓库间调拨' },
  { value: 2, label: '部门间调拨' },
  { value: 3, label: '项目间调拨' },
  { value: 4, label: '紧急调拨' },
  { value: 5, label: '其他调拨' }
];

// 计算属性
const availableToWarehouses = computed(() => {
  if (!formData.fromWarehouseId) return warehouseList.value;
  return warehouseList.value.filter(warehouse => warehouse.id !== formData.fromWarehouseId);
});

const availableProducts = computed(() => {
  if (!formData.fromWarehouseId) return [];
  return fromInventoryList.value
    .filter(item => item.quantity > 0)
    .map(item => ({
      id: item.productId,
      sku: item.productSku,
      name: item.productName,
      spec: item.productSpec,
      unit: item.productUnit,
      quantity: item.quantity
    }));
});

const selectedProductIds = computed(() => {
  return formData.items.map(item => item.productId).filter(id => id);
});

const totalQuantity = computed(() => {
  return formData.items.reduce((sum, item) => sum + (item.quantity || 0), 0);
});

const hasInsufficientStock = computed(() => {
  return formData.items.some(item => (item.quantity || 0) > (item.currentStock || 0));
});

const hasSameWarehouse = computed(() => {
  return formData.fromWarehouseId && formData.toWarehouseId && 
         formData.fromWarehouseId === formData.toWarehouseId;
});

const transferStatusText = computed(() => {
  if (hasSameWarehouse.value) return '仓库相同';
  if (hasInsufficientStock.value) return '库存不足';
  return '可调拨';
});

const transferStatusClass = computed(() => {
  if (hasSameWarehouse.value) return 'status-error';
  if (hasInsufficientStock.value) return 'status-warning';
  return 'status-success';
});

const stockCheckText = computed(() => {
  if (!formData.fromWarehouseId) return '请选择调出仓库';
  if (!formData.toWarehouseId) return '请选择调入仓库';
  if (hasSameWarehouse.value) return '仓库不能相同';
  return '检查通过';
});

const stockCheckClass = computed(() => {
  if (!formData.fromWarehouseId || !formData.toWarehouseId) return 'status-info';
  if (hasSameWarehouse.value) return 'status-error';
  return 'status-success';
});

// 表单验证规则
const formRules = {
  transferType: [
    { required: true, message: '请选择调拨类型', trigger: 'change' }
  ],
  fromWarehouseId: [
    { required: true, message: '请选择调出仓库', trigger: 'change' }
  ],
  toWarehouseId: [
    { 
      required: true, 
      message: '请选择调入仓库', 
      trigger: 'change',
      validator: (rule, value, callback) => {
        if (!value) {
          callback(new Error('请选择调入仓库'));
        } else if (value === formData.fromWarehouseId) {
          callback(new Error('调入仓库不能与调出仓库相同'));
        } else {
          callback();
        }
      }
    }
  ],
  expectedDate: [
    { required: true, message: '请选择预计调拨日期', trigger: 'change' }
  ],
  reason: [
    { required: true, message: '请输入调拨原因', trigger: 'blur' }
  ]
};

// 方法
const generateOrderNo = () => {
  const date = new Date();
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const random = String(Math.random()).substr(2, 6);
  formData.orderNo = `DB${year}${month}${day}${random}`;
};

const handleFromWarehouseChange = async (warehouseId) => {
  if (warehouseId) {
    await loadFromInventoryData(warehouseId);
    fromWarehouseInfo.value = warehouseList.value.find(w => w.id === warehouseId);
    // 清空已选择的产品
    formData.items = [];
    
    // 如果调入仓库与调出仓库相同，清空调入仓库
    if (formData.toWarehouseId === warehouseId) {
      formData.toWarehouseId = null;
      toWarehouseInfo.value = null;
    }
  } else {
    fromWarehouseInfo.value = null;
    fromInventoryList.value = [];
  }
};

const handleToWarehouseChange = async (warehouseId) => {
  if (warehouseId) {
    await loadToInventoryData(warehouseId);
    toWarehouseInfo.value = warehouseList.value.find(w => w.id === warehouseId);
    // 更新调入库存信息
    updateToWarehouseStock();
  } else {
    toWarehouseInfo.value = null;
    toInventoryList.value = [];
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
    toWarehouseStock: 0,
    quantity: 1,
    batchNo: '',
    availableBatches: [],
    remark: ''
  });
};

const handleBatchAdd = () => {
  batchDialogVisible.value = true;
};

const handleBatchSelect = (selectedProducts) => {
  selectedProducts.forEach(product => {
    // 避免重复添加
    if (!formData.items.some(item => item.productId === product.id)) {
      formData.items.push({
        productId: product.id,
        productName: product.name,
        sku: product.sku,
        spec: product.spec,
        unit: product.unit,
        currentStock: product.quantity,
        toWarehouseStock: getToWarehouseStock(product.id),
        quantity: 1,
        batchNo: '',
        availableBatches: [],
        remark: ''
      });
      
      // 加载批次信息
      loadBatchInfo(product.id, formData.fromWarehouseId, formData.items.length - 1);
    }
  });
  batchDialogVisible.value = false;
  calculateTotal();
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
    item.toWarehouseStock = getToWarehouseStock(productId);
    item.quantity = 1;
    
    // 加载批次信息
    loadBatchInfo(productId, formData.fromWarehouseId, index);
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

const getToStockClass = (toStock) => {
  if (!toStock) return 'stock-none';
  if (toStock < 10) return 'stock-low';
  return 'stock-sufficient';
};

const getToWarehouseStock = (productId) => {
  if (!formData.toWarehouseId) return 0;
  const inventory = toInventoryList.value.find(item => 
    item.productId === productId && item.warehouseId === formData.toWarehouseId
  );
  return inventory ? inventory.quantity : 0;
};

const updateToWarehouseStock = () => {
  formData.items.forEach(item => {
    if (item.productId) {
      item.toWarehouseStock = getToWarehouseStock(item.productId);
    }
  });
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
    fromWarehouseInfo.value = null;
    toWarehouseInfo.value = null;
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
      totalQuantity: totalQuantity.value
    };
    
    const res = await post('/api/auth/transfer/saveDraft', submitData);
    if (res) {
      ElMessage.success('保存草稿成功');
      router.push('/transfer/list');
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
    ElMessage.warning('请至少添加一个调拨产品');
    return;
  }
  
  if (hasInsufficientStock.value) {
    ElMessage.warning('存在库存不足的产品，请调整调拨数量');
    return;
  }
  
  if (hasSameWarehouse.value) {
    ElMessage.warning('调出仓库和调入仓库不能相同');
    return;
  }
  
  loading.value = true;
  try {
    const submitData = {
      ...formData,
      status: 1, // 待审核状态
      totalQuantity: totalQuantity.value
    };
    
    const res = await post('/api/auth/transfer/submit', submitData);
    if (res) {
      ElMessage.success('提交成功，等待审核');
      router.push('/transfer/list');
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
        ElMessage.warning(`第 ${i + 1} 行产品调拨数量超过库存`);
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
    const res = await get('/api/auth/warehouse/list');
    warehouseList.value = res.records || [];
  } catch (error) {
    ElMessage.error('加载仓库列表失败');
  }
};

const loadFromInventoryData = async (warehouseId) => {
  try {
    const res = await get(`/api/auth/inventory/list?warehouseId=${warehouseId}`);
    fromInventoryList.value = res.records || [];
  } catch (error) {
    ElMessage.error('加载调出库存数据失败');
  }
};

const loadToInventoryData = async (warehouseId) => {
  try {
    const res = await get(`/api/auth/inventory/list?warehouseId=${warehouseId}`);
    toInventoryList.value = res.records || [];
  } catch (error) {
    ElMessage.error('加载调入库存数据失败');
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

// 监听调入仓库变化，更新调入库存
watch(() => formData.toWarehouseId, () => {
  updateToWarehouseStock();
});

onMounted(() => {
  generateOrderNo();
  loadWarehouseList();
});
</script>

<style scoped>
.transfer-create-container {
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

.transfer-form {
  margin-bottom: 30px;
}

.warehouse-section {
  margin: 20px 0;
}

.warehouse-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
}

.warehouse-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: bold;
  color: #409EFF;
}

.warehouse-info {
  margin-top: 12px;
  padding: 12px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.warehouse-info p {
  margin: 4px 0;
  font-size: 13px;
  color: #606266;
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

.section-actions {
  display: flex;
  gap: 12px;
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

/* 状态样式 */
.status-success {
  color: #67C23A;
}

.status-warning {
  color: #E6A23C;
}

.status-error {
  color: #F56C6C;
}

.status-info {
  color: #909399;
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
  .transfer-create-container {
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
  
  .section-actions {
    width: 100%;
    justify-content: flex-end;
  }
  
  .summary-info .el-col {
    margin-bottom: 8px;
  }
  
  .warehouse-section .el-col {
    margin-bottom: 16px;
  }
}
</style>