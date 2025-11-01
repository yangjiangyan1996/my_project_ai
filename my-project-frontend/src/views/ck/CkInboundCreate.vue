<template>
  <div class="inbound-create-container">
    <el-card class="form-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">新建入库单</span>
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
              <el-select
                v-model="formData.orderType"
                placeholder="请选择入库类型"
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
            <el-form-item label="供应商" prop="supplierId" v-if="showSupplier">
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
          <!-- <el-table-column label="计划数量" width="120">
            <template #default="{ row, $index }">
              <el-input-number
                v-model="row.quantity"
                :min="0.0001"
                :precision="4"
                :step="1"
                controls-position="right"
                style="width: 100%"
                @change="() => calculateTotal()"
              />
            </template>
          </el-table-column> -->
          <el-table-column label="实际数量" width="120">
            <template #default="{ row, $index }">
              <el-input-number
                v-model="row.actualQuantity"
                :min="0"
                :precision="4"
                :step="1"
                controls-position="right"
                style="width: 100%"
              />
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
          <el-table-column label="货架位置" width="150">
            <template #default="{ row, $index }">
              <el-select
                v-model="row.shelfLocationId"
                placeholder="选择位置"
                style="width: 100%"
                filterable
                clearable
              >
                <el-option
                  v-for="location in shelfLocationList"
                  :key="location.shelfCode"
                  :label="location.shelfName"
                  :value="location.shelfCode"
                />
              </el-select>
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
            <!-- <el-col :span="6">
              <div class="summary-item">
                <span class="label">总数量：</span>
                <span class="value">{{ totalQuantity }} </span>
              </div>
            </el-col> -->
            <el-col :span="6">
              <div class="summary-item">
                <span class="label">实际总数：</span>
                <span class="value">{{ totalActualQuantity }} </span>
              </div>
            </el-col>
            <!-- <el-col :span="6">
              <div class="summary-item">
                <span class="label">差异数量：</span>
                <span class="value" :class="quantityDiffClass">{{ quantityDiff }} </span>
              </div>
            </el-col> -->
          </el-row>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Delete } from '@element-plus/icons-vue';
import { post, get } from '@/net';

const router = useRouter();
const formRef = ref();
const loading = ref(false);

// 表单数据
const formData = reactive({
  orderNo: '',
  orderType: 1,
  warehouseId: null,
  supplierId: null,
  relatedOrderNo: '',
  remark: '',
  items: []
});

// 选项数据
const warehouseList = ref([]);
const supplierList = ref([]);
const productList = ref([]);
const shelfLocationList = ref([]);

// 入库类型选项
const orderTypeOptions = [
  { value: 1, label: '采购入库' },
  { value: 2, label: '生产入库' },
  { value: 3, label: '退货入库' },
  { value: 4, label: '调拨入库' }
];

// 计算属性
const showSupplier = computed(() => {
  return formData.orderType === 1; // 只有采购入库显示供应商
});

const selectedProductIds = computed(() => {
  return formData.items.map(item => item.productId).filter(id => id);
});

const totalQuantity = computed(() => {
  return formData.items.reduce((sum, item) => sum + (parseFloat(item.quantity) || 0), 0).toFixed(4);
});

const totalActualQuantity = computed(() => {
  return formData.items.reduce((sum, item) => sum + (parseFloat(item.actualQuantity) || 0), 0).toFixed(4);
});

const quantityDiff = computed(() => {
  return (parseFloat(totalActualQuantity.value) - parseFloat(totalQuantity.value)).toFixed(4);
});

const quantityDiffClass = computed(() => {
  const diff = parseFloat(quantityDiff.value);
  if (diff > 0) return 'positive';
  if (diff < 0) return 'negative';
  return '';
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
    { 
      required: true, 
      message: '请选择供应商', 
      trigger: 'change',
      validator: (rule, value, callback) => {
        if (showSupplier.value && !value) {
          callback(new Error('请选择供应商'));
        } else {
          callback();
        }
      }
    }
  ]
};

// 方法
const generateOrderNo = () => {
  const date = new Date();
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const random = String(Math.random()).substr(2, 6);
  formData.orderNo = `RK${year}${month}${day}${random}`;
};

const handleOrderTypeChange = (value) => {
  if (value !== 1) {
    formData.supplierId = null;
  }
};

const handleWarehouseChange = async (value) => {
  if (value) {
    await loadShelfLocationList(value);
  } else {
    shelfLocationList.value = [];
  }
};

const handleSupplierChange = (value) => {
  // 供应商变更逻辑
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
    actualQuantity: 0,
    shelfLocationId: null,
    batchNo: '',
    remark: ''
  });
};

const handleRemoveProduct = (index) => {
  formData.items.splice(index, 1);
  calculateTotal();
};

const handleProductChange = (productId, index) => {
  const product = productList.value.find(p => p.id === productId);
  if (product) {
    const item = formData.items[index];
    item.productName = product.name;
    item.sku = product.sku;
    item.spec = product.spec;
    item.unit = product.unitCode;
  }
};

const calculateTotal = () => {
  // 触发响应式更新
};

const validateBatchNo = (batchNo, index) => {
  if (batchNo && !/^[A-Za-z0-9_-]+$/.test(batchNo)) {
    ElMessage.warning('批次号只能包含字母、数字、下划线和横线');
    formData.items[index].batchNo = '';
  }
};

const handleReset = () => {
  ElMessageBox.confirm('确定要重置表单吗？所有输入的数据将会丢失。', '重置确认', {
    type: 'warning'
  }).then(() => {
    formRef.value?.resetFields();
    formData.items = [];
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
      status: 0, // 待提交状态
      totalQuantity: totalActualQuantity.value
    };
    
    const res = await post('/api/auth/inbound/create', submitData);
    if (res) {
      ElMessage.success('保存草稿成功');
      router.push('/index/ckInboundManage');
    }
  } catch (error) {
    // ElMessage.error('保存草稿失败');
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
  
  loading.value = true;
  try {
    const submitData = {
      ...formData,
      // status: 1, // 审核中状态 TODO yang 等审核流程加了后
      status: 2, // 已通过状态
      totalQuantity: totalActualQuantity.value
    };
    
    const res = await post('/api/auth/inbound/create', submitData);
    if (res) {
      ElMessage.success('提交成功，等待审核');
      router.push('/index/ckInboundManage');
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
    }
    
    return true;
  } catch (error) {
    ElMessage.warning('请完善表单信息');
    return false;
  }
};

// 初始化数据
const loadWarehouseList = async () => {
  try {
    const res = await get('/api/auth/warehouse/listEnable');
    console.log('加载仓库列表:', res);
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
    console.error('加载货架位置列表失败:', error);
    shelfLocationList.value = [];
  }
};

onMounted(() => {
  generateOrderNo();
  loadWarehouseList();
  loadSupplierList();
  loadProductList();
});
</script>

<style scoped>
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

.summary-item .value.positive {
  color: #67c23a;
}

.summary-item .value.negative {
  color: #f56c6c;
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
}
</style>