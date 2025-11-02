<template>
  <div class="inbound-create-container">
    <el-card class="form-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">{{ isEditMode ? '编辑入库单' : '新建入库单' }}</span>
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
                  :key="location.id"
                  :label="getShelfLocationLabel(location)"
                  :value="location.id"
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
            <el-col :span="6">
              <div class="summary-item">
                <span class="label">实际总数：</span>
                <span class="value">{{ totalActualQuantity }} </span>
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
import { Plus, Delete } from '@element-plus/icons-vue';
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
  supplierId: null,
  relatedOrderNo: '',
  remark: '',
  status: 0,
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

const totalActualQuantity = computed(() => {
  return formData.items.reduce((sum, item) => sum + (parseFloat(item.actualQuantity) || 0), 0).toFixed(4);
});

// 获取货架位置显示标签
const getShelfLocationLabel = (location) => {
  if (!location) return '';
  return `${location.shelfName} (${location.shelfCode})`;
};

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
    // 当仓库变更时，清空所有产品的货架位置选择
    formData.items.forEach(item => {
      item.shelfLocationId = null;
    });
  } else {
    shelfLocationList.value = [];
    // 清空所有货架位置选择
    formData.items.forEach(item => {
      item.shelfLocationId = null;
    });
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

// 加载入库单详情
const loadInboundDetail = async (id) => {
  loading.value = true;
  try {
    const res = await get(`/api/auth/inbound/detail?orderId=${id}`);
    if (res) {
      // 先设置基本数据
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

      // 如果仓库有值，先加载对应的货架位置
      if (res.warehouseId) {
        await loadShelfLocationList(res.warehouseId);
      }

      // 然后设置明细数据，确保货架位置列表已加载
      if (res.items && res.items.length > 0) {
        formData.items = res.items.map(item => ({
          productId: item.productId,
          productName: item.productName || '',
          sku: item.sku || '',
          spec: item.spec || '',
          unit: item.unit || '',
          quantity: item.quantity || 1,
          actualQuantity: item.actualQuantity || 0,
          shelfLocationId: item.shelfLocationId, // 这里使用数字ID，与下拉框value对应
          batchNo: item.batchNo || '',
          remark: item.remark || ''
        }));
      } else {
        formData.items = [];
      }

      // 设置总数量
      if (res.totalQuantity) {
        formData.totalQuantity = res.totalQuantity;
      }
      
      ElMessage.success('数据加载成功');
    }
  } catch (error) {
    console.error('加载入库单详情失败:', error);
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
      // 编辑模式下重新加载数据
      loadInboundDetail(route.params.id);
    } else {
      // 创建模式下清空表单
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
      status: 0, // 待提交状态
      totalQuantity: parseFloat(totalActualQuantity.value)
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
  
  loading.value = true;
  try {
    const submitData = {
      ...formData,
      // status: 1, // 审核中状态 TODO yang 等审核流程加了后
      status: 2, // 已通过状态
      totalQuantity: parseFloat(totalActualQuantity.value)
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
    
    // 验证产品明细
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
    console.log('加载货架位置列表:', shelfLocationList.value);
  } catch (error) {
    console.error('加载货架位置列表失败:', error);
    shelfLocationList.value = [];
  }
};

onMounted(() => {
  if (isEditMode.value) {
    // 编辑模式，加载数据
    loadInboundDetail(route.params.id);
  } else {
    // 创建模式，生成单号
    generateOrderNo();
  }
  loadWarehouseList();
  loadSupplierList();
  loadProductList();
});

// 监听路由变化，处理直接通过URL进入的情况
watch(
  () => route.params.id,
  (newId) => {
    if (newId) {
      loadInboundDetail(newId);
    } else {
      // 从编辑模式切换到创建模式
      Object.assign(formData, {
        id: null,
        orderNo: '',
        orderType: 1,
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