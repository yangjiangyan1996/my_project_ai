<template>
  <div class="stocktake-create-container">
    <el-card class="form-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">新建库存盘点</span>
          <div class="header-actions">
            <el-button @click="handleReset">重置</el-button>
            <el-button type="primary" @click="handleSaveDraft" :loading="loading">
              保存草稿
            </el-button>
            <el-button type="success" @click="handleStartCounting" :loading="loading" 
                      v-if="formData.status === 0" :disabled="!canStartCounting">
              开始盘点
            </el-button>
            <el-button type="primary" @click="handleSubmit" :loading="loading"
                      v-if="formData.status === 1">
              完成盘点
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
        class="stocktake-form"
      >
        <el-row :gutter="24">
          <el-col :xs="24" :sm="12" :lg="8">
            <el-form-item label="盘点单号" prop="orderNo">
              <el-input v-model="formData.orderNo" placeholder="系统自动生成" disabled />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :lg="8">
            <el-form-item label="盘点类型" prop="stocktakeType">
              <el-select
                v-model="formData.stocktakeType"
                placeholder="请选择盘点类型"
                style="width: 100%"
                @change="handleStocktakeTypeChange"
              >
                <el-option
                  v-for="item in stocktakeTypeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :lg="8">
            <el-form-item label="盘点仓库" prop="warehouseId">
              <el-select
                v-model="formData.warehouseId"
                placeholder="请选择盘点仓库"
                style="width: 100%"
                filterable
                @change="handleWarehouseChange"
                :disabled="formData.status > 0"
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
            <el-form-item label="盘点方式" prop="countingMethod">
              <el-select
                v-model="formData.countingMethod"
                placeholder="请选择盘点方式"
                style="width: 100%"
                :disabled="formData.status > 0"
              >
                <el-option
                  v-for="item in countingMethodOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :lg="8">
            <el-form-item label="计划盘点日期" prop="plannedDate">
              <el-date-picker
                v-model="formData.plannedDate"
                type="date"
                placeholder="选择计划盘点日期"
                style="width: 100%"
                value-format="YYYY-MM-DD"
                :disabled="formData.status > 0"
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :lg="8">
            <el-form-item label="盘点负责人" prop="responsiblePerson">
              <el-input
                v-model="formData.responsiblePerson"
                placeholder="请输入盘点负责人"
                :disabled="formData.status > 0"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="盘点范围" prop="scope">
          <el-radio-group v-model="formData.scope" :disabled="formData.status > 0">
            <el-radio :label="1">全库盘点</el-radio>
            <el-radio :label="2">按分类盘点</el-radio>
            <el-radio :label="3">按产品盘点</el-radio>
            <el-radio :label="4">指定范围盘点</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="盘点说明" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入盘点说明"
            maxlength="500"
            show-word-limit
            :disabled="formData.status > 0"
          />
        </el-form-item>

        <!-- 盘点进度 -->
        <div class="progress-section" v-if="formData.status > 0">
          <el-alert
            :title="progressTitle"
            :type="progressType"
            :closable="false"
            show-icon
          >
            <template #default>
              <div class="progress-stats">
                <span>已盘点: {{ countedItems }}/{{ totalItems }} 种产品</span>
                <span class="progress-percent">({{ progressPercent }}%)</span>
              </div>
            </template>
          </el-alert>
          
          <el-progress 
            :percentage="progressPercent" 
            :status="progressStatus"
            class="progress-bar"
          />
        </div>
      </el-form>

      <!-- 盘点产品明细 -->
      <div class="product-section">
        <div class="section-header">
          <h3>盘点产品明细</h3>
          <div class="section-actions">
            <el-button 
              @click="handleImportInventory" 
              :disabled="!formData.warehouseId || formData.status > 0"
            >
              <el-icon><Upload /></el-icon>
              导入库存数据
            </el-button>
            <el-button 
              type="primary" 
              @click="handleAddProduct" 
              :disabled="formData.status > 1"
            >
              <el-icon><Plus /></el-icon>
              添加产品
            </el-button>
            <el-button 
              type="success" 
              @click="handleQuickCountAll" 
              v-if="formData.status === 1"
              :disabled="allCounted"
            >
              <el-icon><Check /></el-icon>
              快速盘点全部
            </el-button>
          </div>
        </div>

        <el-table
          :data="formData.items"
          border
          class="product-table"
          empty-text="请导入库存数据或添加盘点产品"
          v-loading="tableLoading"
        >
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column label="产品信息" min-width="220" fixed="left">
            <template #default="{ row }">
              <div class="product-info">
                <div class="product-name">{{ row.productName }}</div>
                <div class="product-sku">{{ row.sku }}</div>
                <div class="product-spec" v-if="row.spec">{{ row.spec }}</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="单位" width="80" align="center">
            <template #default="{ row }">
              <span>{{ row.unit || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="系统库存" width="120" align="center">
            <template #default="{ row }">
              <span class="system-quantity">{{ row.systemQuantity || 0 }}</span>
            </template>
          </el-table-column>
          <el-table-column label="实际数量" width="150">
            <template #default="{ row, $index }">
              <el-input-number
                v-model="row.actualQuantity"
                :min="0"
                :max="999999"
                controls-position="right"
                style="width: 100%"
                @change="() => handleQuantityChange(row, $index)"
                :disabled="formData.status !== 1"
                placeholder="输入实际数量"
              />
            </template>
          </el-table-column>
          <el-table-column label="差异数量" width="120" align="center">
            <template #default="{ row }">
              <span :class="getDifferenceClass(row.difference)">
                {{ row.difference || 0 }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="差异率" width="100" align="center">
            <template #default="{ row }">
              <span :class="getDifferenceRateClass(row.differenceRate)">
                {{ row.differenceRate || '0%' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="批次号" width="150">
            <template #default="{ row }">
              <el-input
                v-model="row.batchNo"
                placeholder="批次号"
                :disabled="formData.status !== 1"
              />
            </template>
          </el-table-column>
          <el-table-column label="盘点状态" width="120" align="center">
            <template #default="{ row }">
              <el-tag 
                :type="getCountStatusType(row.countStatus)" 
                size="small"
              >
                {{ getCountStatusText(row.countStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="备注" min-width="150">
            <template #default="{ row }">
              <el-input
                v-model="row.remark"
                placeholder="盘点备注"
                maxlength="100"
                show-word-limit
                :disabled="formData.status !== 1"
              />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right" align="center">
            <template #default="{ row, $index }">
              <div class="action-buttons">
                <el-button
                  type="success"
                  link
                  @click="handleQuickCount(row, $index)"
                  v-if="formData.status === 1 && row.countStatus !== 1"
                >
                  <el-icon><Check /></el-icon>
                  盘点
                </el-button>
                <el-button
                  type="danger"
                  link
                  @click="handleRemoveProduct($index)"
                  :disabled="formData.status > 1"
                >
                  <el-icon><Delete /></el-icon>
                  删除
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <!-- 盘点统计 -->
        <div class="count-summary" v-if="formData.items.length > 0">
          <el-row :gutter="20">
            <el-col :xs="12" :sm="6" :lg="3">
              <div class="summary-card">
                <div class="summary-value">{{ formData.items.length }}</div>
                <div class="summary-label">盘点产品</div>
              </div>
            </el-col>
            <el-col :xs="12" :sm="6" :lg="3">
              <div class="summary-card">
                <div class="summary-value" :class="getTotalDifferenceClass">
                  {{ totalDifference }}
                </div>
                <div class="summary-label">总差异</div>
              </div>
            </el-col>
            <el-col :xs="12" :sm="6" :lg="3">
              <div class="summary-card">
                <div class="summary-value">{{ countedItems }}</div>
                <div class="summary-label">已盘点</div>
              </div>
            </el-col>
            <el-col :xs="12" :sm="6" :lg="3">
              <div class="summary-card">
                <div class="summary-value">{{ pendingItems }}</div>
                <div class="summary-label">待盘点</div>
              </div>
            </el-col>
            <el-col :xs="12" :sm="6" :lg="3">
              <div class="summary-card">
                <div class="summary-value" :class="getAccuracyClass">
                  {{ accuracyRate }}%
                </div>
                <div class="summary-label">准确率</div>
              </div>
            </el-col>
            <el-col :xs="12" :sm="6" :lg="3">
              <div class="summary-card">
                <div class="summary-value">{{ differenceItems }}</div>
                <div class="summary-label">差异产品</div>
              </div>
            </el-col>
          </el-row>
        </div>

        <!-- 差异分析 -->
        <div class="difference-analysis" v-if="differenceItems > 0">
          <el-card shadow="never">
            <template #header>
              <div class="analysis-header">
                <span>差异分析</span>
                <el-button type="text" @click="showDifferenceDetails = !showDifferenceDetails">
                  {{ showDifferenceDetails ? '收起' : '展开' }}
                </el-button>
              </div>
            </template>
            <div v-if="showDifferenceDetails">
              <el-table :data="differenceItemsList" size="small" border>
                <el-table-column prop="productName" label="产品名称" min-width="200" />
                <el-table-column prop="sku" label="SKU" width="120" />
                <el-table-column prop="systemQuantity" label="系统库存" width="100" align="center" />
                <el-table-column prop="actualQuantity" label="实际数量" width="100" align="center" />
                <el-table-column prop="difference" label="差异数量" width="100" align="center">
                  <template #default="{ row }">
                    <span :class="getDifferenceClass(row.difference)">
                      {{ row.difference }}
                    </span>
                  </template>
                </el-table-column>
                <el-table-column prop="differenceRate" label="差异率" width="100" align="center">
                  <template #default="{ row }">
                    <span :class="getDifferenceRateClass(row.differenceRate)">
                      {{ row.differenceRate }}
                    </span>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-card>
        </div>
      </div>

      <!-- 附件上传 -->
      <div class="attachment-section">
        <div class="section-header">
          <h3>附件上传</h3>
          <span class="tip">支持盘点记录表、照片等，单个文件不超过10MB</span>
        </div>
        <el-upload
          v-model:file-list="fileList"
          action="/api/auth/file/upload"
          multiple
          :limit="10"
          :on-exceed="handleExceed"
          :before-upload="beforeUpload"
          :on-success="handleUploadSuccess"
          :on-remove="handleRemoveFile"
          list-type="picture-card"
          :file-list="fileList"
        >
          <el-icon><Plus /></el-icon>
        </el-upload>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Delete, Upload, Check, Picture } from '@element-plus/icons-vue';
import { post, get } from '@/net';

const router = useRouter();
const formRef = ref();
const loading = ref(false);
const tableLoading = ref(false);
const showDifferenceDetails = ref(false);

// 表单数据
const formData = reactive({
  orderNo: '',
  stocktakeType: 1,
  warehouseId: null,
  countingMethod: 1,
  plannedDate: '',
  responsiblePerson: '',
  scope: 1,
  description: '',
  status: 0, // 0:草稿 1:盘点中 2:已完成
  startTime: '',
  endTime: '',
  items: [],
  attachments: []
});

// 选项数据
const warehouseList = ref([]);
const fileList = ref([]);

// 盘点类型选项
const stocktakeTypeOptions = [
  { value: 1, label: '定期盘点' },
  { value: 2, label: '循环盘点' },
  { value: 3, label: '重点盘点' },
  { value: 4, label: '随机盘点' },
  { value: 5, label: '全面盘点' }
];

// 盘点方式选项
const countingMethodOptions = [
  { value: 1, label: '手工盘点' },
  { value: 2, label: 'PDA盘点' },
  { value: 3, label: 'RFID盘点' },
  { value: 4, label: '混合盘点' }
];

// 计算属性
const canStartCounting = computed(() => {
  return formData.warehouseId && formData.items.length > 0;
});

const totalItems = computed(() => {
  return formData.items.length;
});

const countedItems = computed(() => {
  return formData.items.filter(item => item.countStatus === 1).length;
});

const pendingItems = computed(() => {
  return formData.items.filter(item => item.countStatus !== 1).length;
});

const progressPercent = computed(() => {
  if (totalItems.value === 0) return 0;
  return Math.round((countedItems.value / totalItems.value) * 100);
});

const progressTitle = computed(() => {
  if (formData.status === 1) {
    return `盘点进行中 - 已完成 ${countedItems.value}/${totalItems.value} 种产品`;
  } else if (formData.status === 2) {
    return `盘点已完成 - 准确率 ${accuracyRate.value}%`;
  }
  return '盘点未开始';
});

const progressType = computed(() => {
  if (formData.status === 1) return 'info';
  if (formData.status === 2) return 'success';
  return 'warning';
});

const progressStatus = computed(() => {
  if (progressPercent.value === 100) return 'success';
  return undefined;
});

const totalDifference = computed(() => {
  return formData.items.reduce((sum, item) => sum + Math.abs(item.difference || 0), 0);
});

const differenceItems = computed(() => {
  return formData.items.filter(item => item.difference !== 0).length;
});

const accuracyRate = computed(() => {
  if (totalItems.value === 0) return 100;
  const accurateItems = formData.items.filter(item => item.difference === 0).length;
  return Math.round((accurateItems / totalItems.value) * 100);
});

const differenceItemsList = computed(() => {
  return formData.items.filter(item => item.difference !== 0);
});

const allCounted = computed(() => {
  return countedItems.value === totalItems.value;
});

const getTotalDifferenceClass = computed(() => {
  return totalDifference.value === 0 ? 'difference-zero' : 'difference-have';
});

const getAccuracyClass = computed(() => {
  if (accuracyRate.value >= 95) return 'accuracy-high';
  if (accuracyRate.value >= 90) return 'accuracy-medium';
  return 'accuracy-low';
});

// 表单验证规则
const formRules = {
  stocktakeType: [
    { required: true, message: '请选择盘点类型', trigger: 'change' }
  ],
  warehouseId: [
    { required: true, message: '请选择盘点仓库', trigger: 'change' }
  ],
  countingMethod: [
    { required: true, message: '请选择盘点方式', trigger: 'change' }
  ],
  plannedDate: [
    { required: true, message: '请选择计划盘点日期', trigger: 'change' }
  ],
  responsiblePerson: [
    { required: true, message: '请输入盘点负责人', trigger: 'blur' }
  ]
};

// 方法
const generateOrderNo = () => {
  const date = new Date();
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const random = String(Math.random()).substr(2, 6);
  formData.orderNo = `PD${year}${month}${day}${random}`;
};

const handleStocktakeTypeChange = (value) => {
  // 根据盘点类型调整默认设置
};

const handleWarehouseChange = async (warehouseId) => {
  if (warehouseId) {
    // 清空现有产品
    formData.items = [];
  }
};

const handleImportInventory = async () => {
  if (!formData.warehouseId) {
    ElMessage.warning('请先选择盘点仓库');
    return;
  }

  tableLoading.value = true;
  try {
    const res = await get(`/api/auth/inventory/list?warehouseId=${formData.warehouseId}`);
    const inventoryData = res.records || [];
    
    formData.items = inventoryData.map(item => ({
      productId: item.productId,
      productName: item.productName,
      sku: item.productSku,
      spec: item.productSpec,
      unit: item.productUnit,
      systemQuantity: item.quantity,
      actualQuantity: item.quantity, // 默认与实际相同
      difference: 0,
      differenceRate: '0%',
      countStatus: 0, // 0:未盘点 1:已盘点
      batchNo: '',
      remark: ''
    }));
    
    ElMessage.success(`成功导入 ${formData.items.length} 个库存产品`);
  } catch (error) {
    ElMessage.error('导入库存数据失败');
  } finally {
    tableLoading.value = false;
  }
};

const handleAddProduct = () => {
  formData.items.push({
    productId: null,
    productName: '',
    sku: '',
    spec: '',
    unit: '',
    systemQuantity: 0,
    actualQuantity: 0,
    difference: 0,
    differenceRate: '0%',
    countStatus: 0,
    batchNo: '',
    remark: ''
  });
};

const handleRemoveProduct = (index) => {
  formData.items.splice(index, 1);
};

const handleQuantityChange = (row, index) => {
  // 计算差异
  row.difference = (row.actualQuantity || 0) - (row.systemQuantity || 0);
  
  // 计算差异率
  if (row.systemQuantity > 0) {
    const rate = (Math.abs(row.difference) / row.systemQuantity) * 100;
    row.differenceRate = `${rate.toFixed(1)}%`;
  } else {
    row.differenceRate = row.actualQuantity > 0 ? '100%' : '0%';
  }
  
  // 更新盘点状态
  row.countStatus = row.actualQuantity !== null && row.actualQuantity !== undefined ? 1 : 0;
};

const handleQuickCount = (row, index) => {
  // 快速盘点：实际数量等于系统数量
  row.actualQuantity = row.systemQuantity;
  handleQuantityChange(row, index);
  ElMessage.success('产品已盘点');
};

const handleQuickCountAll = () => {
  ElMessageBox.confirm('确定要快速盘点所有产品吗？这将把所有产品的实际数量设置为系统数量。', '快速盘点确认', {
    type: 'warning'
  }).then(() => {
    formData.items.forEach(item => {
      item.actualQuantity = item.systemQuantity;
      handleQuantityChange(item);
    });
    ElMessage.success('所有产品已快速盘点完成');
  });
};

const getDifferenceClass = (difference) => {
  if (difference === 0) return 'difference-zero';
  return difference > 0 ? 'difference-positive' : 'difference-negative';
};

const getDifferenceRateClass = (differenceRate) => {
  const rate = parseFloat(differenceRate);
  if (rate === 0) return 'difference-rate-zero';
  if (rate <= 5) return 'difference-rate-low';
  if (rate <= 10) return 'difference-rate-medium';
  return 'difference-rate-high';
};

const getCountStatusType = (status) => {
  const types = { 0: 'info', 1: 'success' };
  return types[status] || 'info';
};

const getCountStatusText = (status) => {
  const texts = { 0: '未盘点', 1: '已盘点' };
  return texts[status] || '未知';
};

const handleReset = () => {
  ElMessageBox.confirm('确定要重置表单吗？所有输入的数据将会丢失。', '重置确认', {
    type: 'warning'
  }).then(() => {
    formRef.value?.resetFields();
    formData.items = [];
    fileList.value = [];
    formData.status = 0;
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
      totalItems: totalItems.value,
      countedItems: countedItems.value,
      totalDifference: totalDifference.value,
      accuracyRate: accuracyRate.value
    };
    
    const res = await post('/api/auth/stocktake/saveDraft', submitData);
    if (res) {
      ElMessage.success('保存草稿成功');
      router.push('/stocktake/list');
    }
  } catch (error) {
    ElMessage.error('保存草稿失败');
  } finally {
    loading.value = false;
  }
};

const handleStartCounting = async () => {
  if (!await validateForm()) return;
  
  if (formData.items.length === 0) {
    ElMessage.warning('请先导入库存数据或添加盘点产品');
    return;
  }
  
  loading.value = true;
  try {
    formData.status = 1;
    formData.startTime = new Date().toISOString();
    
    const submitData = {
      ...formData,
      status: 1, // 盘点中状态
      startTime: formData.startTime
    };
    
    const res = await post('/api/auth/stocktake/startCounting', submitData);
    if (res) {
      ElMessage.success('盘点已开始');
    }
  } catch (error) {
    formData.status = 0;
    ElMessage.error('开始盘点失败');
  } finally {
    loading.value = false;
  }
};

const handleSubmit = async () => {
  if (!await validateForm()) return;
  
  if (countedItems.value !== totalItems.value) {
    ElMessage.warning('请先完成所有产品的盘点');
    return;
  }
  
  loading.value = true;
  try {
    formData.status = 2;
    formData.endTime = new Date().toISOString();
    
    const submitData = {
      ...formData,
      status: 2, // 已完成状态
      endTime: formData.endTime,
      totalItems: totalItems.value,
      countedItems: countedItems.value,
      totalDifference: totalDifference.value,
      accuracyRate: accuracyRate.value,
      differenceItems: differenceItems.value
    };
    
    const res = await post('/api/auth/stocktake/complete', submitData);
    if (res) {
      ElMessage.success('盘点完成');
      router.push('/stocktake/list');
    }
  } catch (error) {
    formData.status = 1;
    ElMessage.error('完成盘点失败');
  } finally {
    loading.value = false;
  }
};

const validateForm = async () => {
  if (!formRef.value) return false;
  
  try {
    await formRef.value.validate();
    return true;
  } catch (error) {
    ElMessage.warning('请完善表单信息');
    return false;
  }
};

// 文件上传相关
const handleExceed = () => {
  ElMessage.warning('最多只能上传10个文件');
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

// 数据加载方法
const loadWarehouseList = async () => {
  try {
    const res = await get('/api/auth/warehouse/list');
    warehouseList.value = res.records || [];
  } catch (error) {
    ElMessage.error('加载仓库列表失败');
  }
};

onMounted(() => {
  generateOrderNo();
  loadWarehouseList();
});
</script>

<style scoped>
.stocktake-create-container {
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

.stocktake-form {
  margin-bottom: 30px;
}

.progress-section {
  margin: 20px 0;
}

.progress-stats {
  display: flex;
  align-items: center;
  gap: 8px;
}

.progress-percent {
  color: #409EFF;
  font-weight: bold;
}

.progress-bar {
  margin-top: 10px;
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

.product-info {
  line-height: 1.4;
}

.product-name {
  font-weight: 500;
  color: #303133;
}

.product-sku {
  font-size: 12px;
  color: #909399;
}

.product-spec {
  font-size: 12px;
  color: #606266;
}

.system-quantity {
  font-weight: bold;
  color: #409EFF;
}

/* 差异样式 */
.difference-zero {
  color: #67C23A;
}

.difference-positive {
  color: #E6A23C;
  font-weight: bold;
}

.difference-negative {
  color: #F56C6C;
  font-weight: bold;
}

.difference-have {
  color: #E6A23C;
  font-weight: bold;
}

.difference-rate-zero {
  color: #67C23A;
}

.difference-rate-low {
  color: #E6A23C;
}

.difference-rate-medium {
  color: #E6A23C;
  font-weight: bold;
}

.difference-rate-high {
  color: #F56C6C;
  font-weight: bold;
}

.accuracy-high {
  color: #67C23A;
}

.accuracy-medium {
  color: #E6A23C;
}

.accuracy-low {
  color: #F56C6C;
}

.action-buttons {
  display: flex;
  gap: 8px;
  justify-content: center;
}

.count-summary {
  margin: 20px 0;
}

.summary-card {
  text-align: center;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #ebeef5;
}

.summary-value {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 8px;
}

.summary-label {
  font-size: 14px;
  color: #606266;
}

.difference-analysis {
  margin-top: 20px;
}

.analysis-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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
  .stocktake-create-container {
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
  
  .count-summary .el-col {
    margin-bottom: 12px;
  }
}
</style>