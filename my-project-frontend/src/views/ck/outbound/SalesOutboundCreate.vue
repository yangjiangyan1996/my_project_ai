<!-- SalesOutboundCreate.vue -->
<template>
  <div class="outbound-create-container">
    <el-card class="form-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">{{ isEditMode ? '编辑销售出库单' : '新建销售出库单' }}</span>
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
                disabled
              >
                <el-option
                  :value="1"
                  label="销售出库"
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
            <el-form-item label="客户" prop="customerId">
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
          <div class="header-right-actions">
            <el-button 
              type="success" 
              @click="handleDownloadTemplate"
              :loading="downloadLoading"
            >
              <el-icon><Download /></el-icon>
              下载模板
            </el-button>
            <el-button 
              type="warning" 
              @click="handleImportExcel"
            >
              <el-icon><Upload /></el-icon>
              导入模板
            </el-button>
          </div>
        </div>

        <!-- 销售出库的产品表格 -->
        <el-table
          v-if="formData.warehouseId"
          :data="allInventoryProducts"
          border
          class="product-table"
          empty-text="请先选择仓库"
        >
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column label="产品信息" min-width="200">
            <template #default="{ row }">
              <div>
                <div class="product-name">{{ row.productName }}</div>
                <div class="sku-text">{{ row.sku }}</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="规格型号" width="120">
            <template #default="{ row }">
              <span>{{ row.spec || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="颜色" width="60">
            <template #default="{ row }">
              <span>{{ row.color || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="当前库存" width="100" align="center">
            <template #default="{ row }">
              <span :class="getStockClass(row.availableQuantity, row.quantity)">
                {{ row.availableQuantity }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="出库数量" width="120">
            <template #default="{ row }">
              <el-input-number
                v-model="row.quantity"
                :min="0"
                :max="row.availableQuantity"
                controls-position="right"
                style="width: 100%"
                @change="() => handleQuantityChangeForAll(row)"
                placeholder="请输入数量"
              />
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
                :disabled="!row.quantity || row.quantity <= 0"
              >
                <template #prefix>¥</template>
              </el-input-number>
            </template>
          </el-table-column>
          <el-table-column label="金额" width="120" align="right">
            <template #default="{ row }">
              <span v-if="row.quantity > 0">¥ {{ ((row.price || 0) * (row.quantity || 0)).toFixed(2) }}</span>
              <span v-else>-</span>
            </template>
          </el-table-column>

          <!-- USD单价字段 -->
          <el-table-column label="USD单价" width="120">
            <template #default="{ row }">
              <el-input-number
                v-model="row.priceUnitUsd"
                :min="0"
                :precision="2"
                controls-position="right"
                style="width: 100%"
                :disabled="!row.quantity || row.quantity <= 0"
              >
                <template #prefix>$</template>
              </el-input-number>
            </template>
          </el-table-column>
          <!-- USD总额字段 -->
          <el-table-column label="USD总额" width="120" align="right">
            <template #default="{ row }">
              <span v-if="row.quantity > 0">$ {{ ((row.priceUnitUsd || 0) * (row.quantity || 0)).toFixed(2) }}</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          
          <el-table-column label="批次分配" min-width="200">
            <template #default="{ row }">
              <div class="batch-allocation">
                <el-button 
                  type="primary" 
                  link 
                  @click="openBatchDialogForProduct(row)"
                  :disabled="!row.quantity || row.quantity <= 0"
                >
                  分配批次
                </el-button>
                <div v-if="row.batchAllocations && row.batchAllocations.length > 0" class="batch-summary">
                  <el-tag
                    v-for="allocation in row.batchAllocations"
                    :key="`${allocation.batchNo}-${allocation.shelfId}`"
                    size="small"
                    class="batch-tag"
                  >
                    {{ allocation.batchNo }}({{ allocation.shelfName }}): {{ allocation.quantity }}个
                  </el-tag>
                </div>
                <div v-else class="batch-empty">
                  <span class="empty-text" v-if="row.quantity > 0">未分配批次</span>
                  <span class="empty-text" v-else>-</span>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="备注" min-width="150">
            <template #default="{ row }">
              <el-input
                v-model="row.remark"
                placeholder="产品备注"
                maxlength="100"
                show-word-limit
                :disabled="!row.quantity || row.quantity <= 0"
              />
            </template>
          </el-table-column>
        </el-table>

        <!-- 统计信息 -->
        <div class="summary-info" v-if="allInventoryProducts.some(p => p.quantity > 0)">
          <el-row :gutter="20">
            <el-col :span="4">
              <div class="summary-item">
                <span class="label">产品种类：</span>
                <span class="value">{{ productTypeCount }} 种</span>
              </div>
            </el-col>
            <el-col :span="4">
              <div class="summary-item">
                <span class="label">总数量：</span>
                <span class="value">{{ totalQuantity }} 个</span>
              </div>
            </el-col>
            <el-col :span="4">
              <div class="summary-item">
                <span class="label">总金额：</span>
                <span class="value">¥ {{ totalAmount.toFixed(2) }}</span>
              </div>
            </el-col>
            <el-col :span="4">
              <div class="summary-item">
                <span class="label">USD总额：</span>
                <span class="value">$ {{ totalAmountUsd.toFixed(2) }}</span>
              </div>
            </el-col>
            <el-col :span="4">
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
      width="800px"
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
            <span class="value" :class="batchDialog.allocatedQuantity === batchDialog.totalQuantity ? 'success' : 'warning'">
              {{ batchDialog.allocatedQuantity }}
            </span>
          </div>
          <div class="info-item">
            <span class="label">剩余数量：</span>
            <span class="value">{{ batchDialog.remainingQuantity }}</span>
          </div>
        </div>

        <!-- 批次分配表格 -->
        <el-table :data="batchDialog.batches" border class="batch-table">
          <el-table-column label="批次号" prop="batchNo" width="120" fixed="left" />
          <el-table-column label="总可用数量" width="100" align="center">
            <template #default="{ row }">
              <span :class="row.quantity < 1 ? 'text-disabled' : ''">{{ row.quantity }}</span>
            </template>
          </el-table-column>
          <el-table-column label="货架分配" min-width="400">
            <template #default="{ row, $index: batchIndex }">
              <div class="shelf-allocation-container">
                <div v-if="row.shelfList && row.shelfList.length > 0" class="shelf-list">
                  <div 
                    v-for="(shelf, shelfIndex) in row.shelfList" 
                    :key="shelf.shelfId" 
                    class="shelf-item"
                    :class="{ 'shelf-disabled': shelf.quantity < 1 }"
                  >
                    <div class="shelf-info">
                      <span class="shelf-name">货架 {{ shelf.shelfName }}</span>
                      <span class="shelf-quantity" :class="shelf.quantity < 1 ? 'text-disabled' : ''">
                        可用: {{ shelf.quantity }}
                      </span>
                    </div>
                    <el-input-number
                      v-model="shelf.allocated"
                      :min="0"
                      :max="getShelfMaxAllocation(row, shelf, batchIndex, shelfIndex)"
                      :precision="0"
                      controls-position="right"
                      size="small"
                      placeholder="分配数量"
                      class="shelf-input"
                      @change="(value) => handleShelfAllocationChange(batchIndex, shelfIndex, value)"
                      :disabled="shelf.quantity < 1 || row.quantity < 1"
                    />
                  </div>
                </div>
                <div v-else class="no-shelf">
                  <span class="no-shelf-text">无货架信息</span>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="批次分配总数" width="120" align="center">
            <template #default="{ row }">
              <span :class="getBatchAllocationClass(row)">{{ getBatchAllocatedTotal(row) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" fixed="right" align="center">
            <template #default="{ row, $index }">
              <el-button
                type="danger"
                link
                @click="clearBatchAllocation($index)"
                :disabled="getBatchAllocatedTotal(row) === 0 || row.quantity < 1"
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
          <el-button type="primary" @click="confirmBatchAllocation" :disabled="batchDialog.remainingQuantity !== 0">
            确认分配
          </el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 导入Excel对话框 -->
    <el-dialog
      v-model="importDialog.visible"
      title="导入Excel模板"
      width="500px"
      destroy-on-close
    >
      <div class="import-dialog-content">
        <el-upload
          ref="uploadRef"
          class="upload-demo"
          :auto-upload="false"
          :show-file-list="false"
          accept=".xlsx,.xls"
          :on-change="handleFileChange"
        >
          <el-button type="primary">选择Excel文件</el-button>
          <template #tip>
            <div class="el-upload__tip">
              请选择.xlsx或.xls格式的Excel文件
            </div>
          </template>
        </el-upload>
        
        <div v-if="currentFile" class="selected-file">
          <el-icon><Document /></el-icon>
          <span>{{ currentFile.name }}</span>
          <el-button type="danger" link @click="clearSelectedFile">
            <el-icon><Close /></el-icon>
          </el-button>
        </div>
        
        <div class="import-actions" v-if="currentFile">
          <el-button 
            type="primary" 
            @click="handleImportSubmit"
            :loading="importLoading"
          >
            开始导入
          </el-button>
        </div>
        
        <div class="import-tips" v-if="importResult">
          <h4>导入结果：</h4>
          <div v-if="importResult.success" class="success-result">
            <p>导入成功！</p>
            <p>成功导入 {{ importResult.data?.length || 0 }} 条产品记录</p>
          </div>
          <div v-else class="error-result">
            <p>导入失败：{{ importResult.message }}</p>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Delete, Upload, Download,  Document, Close  } from '@element-plus/icons-vue';
import { post, get } from '@/net';
import axios from 'axios';
import { accessHeader } from '@/net'; 

const router = useRouter();
const route = useRoute();
const formRef = ref();
const uploadRef = ref();
const loading = ref(false);
const downloadLoading = ref(false);

const importDialog = reactive({
  visible: false
});

const currentFile = ref(null);
const importResult = ref(null);
const importLoading = ref(false);

// 判断是否是编辑模式
const isEditMode = computed(() => {
  return !!route.params.id;
});

// 表单数据
const formData = reactive({
  id: null,
  orderNo: '',
  orderType: 1, // 销售出库
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
  remainingQuantity: 0,
  currentRow: null
});

// 选项数据
const warehouseList = ref([]);
const customerList = ref([]);
const inventoryList = ref([]);
const fileList = ref([]);
const allInventoryProducts = ref([]);

// 产品库存映射表
const productStockMap = ref({});

// 计算属性
const productTypeCount = computed(() => {
  return allInventoryProducts.value.filter(p => p.quantity > 0).length;
});

const totalQuantity = computed(() => {
  return allInventoryProducts.value.reduce((sum, item) => sum + (item.quantity || 0), 0);
});

const totalAmount = computed(() => {
  return allInventoryProducts.value.reduce((sum, item) => {
    const price = item.price || 0;
    const quantity = item.quantity || 0;
    return sum + (price * quantity);
  }, 0);
});

const totalAmountUsd = computed(() => {
  return allInventoryProducts.value.reduce((sum, item) => {
    const priceUsd = item.priceUnitUsd || 0;
    const quantity = item.quantity || 0;
    return sum + (priceUsd * quantity);
  }, 0);
});

const hasInsufficientStock = computed(() => {
  return allInventoryProducts.value.some(item => {
    if (item.quantity <= 0) return false;
    const allocatedQuantity = item.batchAllocations 
      ? item.batchAllocations.reduce((sum, alloc) => sum + (alloc.quantity || 0), 0)
      : 0;
    return (item.quantity || 0) !== allocatedQuantity;
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
    { required: true, message: '请选择客户', trigger: 'change' }
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

      // 如果仓库有值，先加载对应的数据
      if (detailData.warehouseId) {
        await loadInventoryData(detailData.warehouseId);
      }

      // 设置产品明细数据
      if (detailData.items && detailData.items.length > 0) {
        await loadInventoryData(detailData.warehouseId);
        
        // 等待库存数据加载完成后，再设置产品数据
        setTimeout(() => {
          // 首先将所有产品的数量重置为0
          allInventoryProducts.value.forEach(product => {
            product.quantity = 0;
            product.price = 0;
            product.remark = '';
            product.batchAllocations = [];
          });
          
          // 然后设置编辑模式下的数据
          detailData.items.forEach(detailItem => {
            const product = allInventoryProducts.value.find(p => p.productId === detailItem.productId);
            console.log('找到产品:', product);
            console.log('明细数据:', detailItem);
            if (product) {
              product.quantity = detailItem.quantity || 0;
              product.price = detailItem.priceUnit || 0;
              product.remark = detailItem.remark || '';
              product.priceUnitUsd = detailItem.priceUnitUsd || 0;
              product.batchAllocations = detailItem.batchAllocations || [];
            }
          });
        }, 500);
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

// 更新库存产品数据
const updateInventoryProducts = () => {
  if (!formData.warehouseId || !inventoryList.value.length) {
    allInventoryProducts.value = [];
    return;
  }
  
  allInventoryProducts.value = inventoryList.value.map(item => {
    // 查找是否已经存在这个产品的数据（保留已输入的数量和价格）
    const existingProduct = allInventoryProducts.value.find(p => p.productId === item.productId);
    
    // 默认使用接口返回的price，如果已有数据则保留用户输入的价格
    const displayPrice = existingProduct ? existingProduct.price : (item.price || 0);
    
    return {
      ...item,
      quantity: existingProduct ? existingProduct.quantity : 0,
      price: displayPrice,
      remark: existingProduct ? existingProduct.remark : '',
      batchAllocations: existingProduct ? existingProduct.batchAllocations : [],
      availableBatches: existingProduct ? existingProduct.availableBatches : [],
      // 保存接口原始价格，用于导入时的逻辑判断
      originalPrice: item.price || 0,
      originalPriceUnitUsd: item.priceUnitUsd || 0
    };
  });
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

const handleWarehouseChange = async (warehouseId) => {
  if (warehouseId) {
    await loadInventoryData(warehouseId);
    
    // 确保所有产品的数量为0
    if (allInventoryProducts.value.length > 0) {
      allInventoryProducts.value.forEach(product => {
        product.quantity = 0;
        product.price = 0;
        product.remark = '';
        product.batchAllocations = [];
      });
    }
  }
};

// 销售出库：处理所有商品表格中的数量变化
const handleQuantityChangeForAll = (row) => {
  if (row.quantity > 0) {
    // 如果之前有批次分配，但数量减少了，需要清空批次分配
    if (row.batchAllocations && row.batchAllocations.length > 0) {
      const totalAllocated = row.batchAllocations.reduce((sum, alloc) => sum + alloc.quantity, 0);
      if (row.quantity < totalAllocated) {
        ElMessage.warning('出库数量小于已分配批次数量，请重新分配批次');
        row.batchAllocations = [];
      }
    }
    
    // 如果接口返回了价格，使用接口价格（仅在价格为空时自动填充）
    if (row.priceFromApi && row.priceFromApi > 0 && (!row.price || row.price === 0)) {
      row.price = row.priceFromApi;
    }
    // 如果接口返回了USD价格，使用接口USD价格（仅在USD价格为空时自动填充）
    if (row.originalPriceUnitUsd && row.originalPriceUnitUsd > 0 && (!row.priceUnitUsd || row.priceUnitUsd === 0)) {
      row.priceUnitUsd = row.originalPriceUnitUsd;
    }
  } else {
    // 如果数量设为0，清空相关数据
    row.quantity = 0;
    row.price = 0;
    row.priceUnitUsd = 0;
    row.remark = '';
    row.batchAllocations = [];
  }
};

const getStockClass = (currentStock, quantity) => {
  if (!currentStock || currentStock <= 0) return 'stock-none';
  if (quantity > currentStock) return 'stock-insufficient';
  if (currentStock < 10) return 'stock-low';
  return 'stock-sufficient';
};

// 批次分配相关方法
const getShelfMaxAllocation = (batch, shelf, batchIndex, shelfIndex) => {
  if (batch.quantity < 1) {
    return 0;
  }
  
  if (shelf.quantity < 1) {
    return 0;
  }
  
  const currentShelfAllocated = shelf.allocated || 0;
  const shelfMax = shelf.quantity;
  
  let otherAllocatedTotal = 0;
  batchDialog.batches.forEach((b, bIndex) => {
    if (b.shelfList && b.shelfList.length > 0) {
      b.shelfList.forEach((s, sIndex) => {
        if (!(bIndex === batchIndex && sIndex === shelfIndex)) {
          otherAllocatedTotal += s.allocated || 0;
        }
      });
    }
  });
  
  const remainingForThisShelf = Math.max(0, batchDialog.totalQuantity - otherAllocatedTotal);
  const maxAllocation = Math.min(shelfMax, remainingForThisShelf);
  
  return Math.max(0, maxAllocation);
};

const handleShelfAllocationChange = (batchIndex, shelfIndex, newValue) => {
  const batch = batchDialog.batches[batchIndex];
  const shelf = batch.shelfList[shelfIndex];
  
  if (batch.quantity < 1) {
    ElMessage.warning('该批次总可用数量不足，无法分配');
    shelf.allocated = 0;
    updateBatchDialogCalculations();
    return;
  }
  
  if (shelf.quantity < 1) {
    ElMessage.warning('该货架可用数量不足，无法分配');
    shelf.allocated = 0;
    updateBatchDialogCalculations();
    return;
  }
  
  if (newValue !== null && newValue !== undefined) {
    newValue = Math.max(0, Math.floor(newValue));
  } else {
    newValue = 0;
  }
  
  const maxAllocation = getShelfMaxAllocation(batch, shelf, batchIndex, shelfIndex);
  
  if (newValue > maxAllocation) {
    newValue = maxAllocation;
    if (maxAllocation > 0) {
      ElMessage.warning(`分配数量不能超过最大可分配数量 ${maxAllocation}`);
    } else {
      ElMessage.warning('当前无可分配数量');
      newValue = 0;
    }
  }
  
  const currentAllocated = batchDialog.allocatedQuantity;
  const otherAllocated = currentAllocated - (shelf.allocated || 0);
  const totalAllocated = otherAllocated + newValue;
  
  if (totalAllocated > batchDialog.totalQuantity) {
    const maxAllowed = Math.max(0, batchDialog.totalQuantity - otherAllocated);
    newValue = Math.max(0, maxAllowed);
    ElMessage.warning(`分配总数不能超过出库数量 ${batchDialog.totalQuantity}，当前最多可分配 ${maxAllowed}`);
  }
  
  const remainingAfterAllocation = batchDialog.totalQuantity - (otherAllocated + newValue);
  if (remainingAfterAllocation < 0) {
    newValue = Math.max(0, batchDialog.totalQuantity - otherAllocated);
    ElMessage.warning('分配数量过多，已自动调整为最大可分配数量');
  }
  
  shelf.allocated = newValue;
  updateBatchDialogCalculations();
};

const getBatchAllocatedTotal = (batch) => {
  if (!batch.shelfList || batch.shelfList.length === 0) return 0;
  return batch.shelfList.reduce((sum, shelf) => {
    return shelf.quantity >= 1 ? sum + (shelf.allocated || 0) : sum;
  }, 0);
};

const getBatchAllocationClass = (batch) => {
  const allocated = getBatchAllocatedTotal(batch);
  const batchTotal = batch.quantity;
  
  if (allocated === 0) return 'allocation-zero';
  if (allocated > batchTotal) return 'allocation-exceed';
  return 'allocation-normal';
};

const clearBatchAllocation = (batchIndex) => {
  const batch = batchDialog.batches[batchIndex];
  if (batch.shelfList) {
    batch.shelfList.forEach(shelf => {
      if (shelf.quantity >= 1) {
        shelf.allocated = 0;
      }
    });
  }
  updateBatchDialogCalculations();
};

const updateBatchDialogCalculations = () => {
  batchDialog.allocatedQuantity = batchDialog.batches.reduce((sum, batch) => {
    if (batch.quantity >= 1 && batch.shelfList) {
      return sum + batch.shelfList.reduce((shelfSum, shelf) => {
        return shelf.quantity >= 1 ? shelfSum + (shelf.allocated || 0) : shelfSum;
      }, 0);
    }
    return sum;
  }, 0);
  
  batchDialog.remainingQuantity = Math.max(0, batchDialog.totalQuantity - batchDialog.allocatedQuantity);
  
  if (batchDialog.allocatedQuantity > batchDialog.totalQuantity) {
    batchDialog.allocatedQuantity = batchDialog.totalQuantity;
    batchDialog.remainingQuantity = 0;
  }
};

const autoAllocateBatches = () => {
  let remaining = batchDialog.remainingQuantity;
  
  if (remaining <= 0) {
    ElMessage.warning('已全部分配完成');
    return;
  }
  
  batchDialog.batches.forEach(batch => {
    if (batch.quantity >= 1 && batch.shelfList) {
      batch.shelfList.forEach(shelf => {
        if (shelf.quantity >= 1) {
          shelf.allocated = 0;
        }
      });
    }
  });
  
  updateBatchDialogCalculations();
  remaining = batchDialog.remainingQuantity;
  
  for (const batch of batchDialog.batches) {
    if (remaining <= 0) break;
    
    if (batch.quantity < 1) continue;
    
    if (batch.shelfList && batch.shelfList.length > 0) {
      for (const shelf of batch.shelfList) {
        if (remaining <= 0) break;
        
        if (shelf.quantity < 1) continue;
        
        const maxAllocation = Math.min(shelf.quantity, remaining);
        const allocate = maxAllocation;
        
        if (allocate > 0) {
          shelf.allocated = allocate;
          remaining -= allocate;
        }
      }
    }
  }
  
  updateBatchDialogCalculations();
  
  if (remaining > 0) {
    ElMessage.warning(`库存不足，仍有 ${remaining} 个无法分配`);
  } else {
    ElMessage.success('自动分配完成');
  }
};

// 批次分配相关方法
const openBatchDialogForProduct = async (row) => {
  if (!row.productId) {
    ElMessage.warning('产品信息不完整');
    return;
  }

  batchDialog.currentRow = row;
  batchDialog.productName = row.productName;
  batchDialog.totalQuantity = row.quantity;
  
  await loadBatchInfoForProduct(row.productId, formData.warehouseId, row);
  
  batchDialog.batches = row.availableBatches.map(batch => ({
    ...batch,
    shelfList: batch.shelfList ? batch.shelfList.map(shelf => ({
      ...shelf,
      allocated: 0,
      maxAllocatable: shelf.quantity
    })) : []
  }));

  if (row.batchAllocations && row.batchAllocations.length > 0) {
    row.batchAllocations.forEach(allocation => {
      const batch = batchDialog.batches.find(b => b.batchNo === allocation.batchNo);
      if (batch && batch.shelfList) {
        const shelf = batch.shelfList.find(s => s.shelfId === allocation.shelfId);
        if (shelf) {
          shelf.allocated = allocation.quantity;
        }
      }
    });
  }

  updateBatchDialogCalculations();
  batchDialog.visible = true;
};

const loadBatchInfoForProduct = async (productId, warehouseId, row) => {
  try {
    const res = await get(`/api/auth/inventory/batches?productId=${productId}&warehouseId=${warehouseId}`);
    console.log('批次信息响应:', res);
    
    if (res && Array.isArray(res)) {
      row.availableBatches = res;
    } else {
      row.availableBatches = [];
    }
  } catch (error) {
    console.error('加载批次信息失败:', error);
    row.availableBatches = [];
  }
};

const confirmBatchAllocation = () => {
  if (batchDialog.remainingQuantity !== 0) {
    ElMessage.warning(`分配数量 (${batchDialog.allocatedQuantity}) 与出库数量 (${batchDialog.totalQuantity}) 不一致，请完成分配`);
    return;
  }

  let hasInvalidAllocation = false;
  batchDialog.batches.forEach(batch => {
    if (batch.shelfList) {
      batch.shelfList.forEach(shelf => {
        if (shelf.allocated > 0 && shelf.allocated > shelf.quantity) {
          hasInvalidAllocation = true;
          ElMessage.warning(`批次 ${batch.batchNo} 货架 ${shelf.shelfName} 的分配数量超过可用数量`);
        }
      });
    }
  });

  if (hasInvalidAllocation) {
    return;
  }

  const row = batchDialog.currentRow;
  row.batchAllocations = [];
  
  batchDialog.batches.forEach(batch => {
    if (batch.shelfList) {
      batch.shelfList.forEach(shelf => {
        if (shelf.allocated > 0) {
          row.batchAllocations.push({
            batchNo: batch.batchNo,
            shelfId: shelf.shelfId,
            shelfName: shelf.shelfName,
            quantity: shelf.allocated,
            price: row.price || 0
          });
        }
      });
    }
  });

  batchDialog.visible = false;
  ElMessage.success('批次分配完成');
};

// 下载模板
const handleDownloadTemplate = async () => {
  if (!formData.warehouseId) {
    ElMessage.warning('请先选择仓库');
    return;
  }
  
  downloadLoading.value = true;

  try {
    const response = await axios.get('/api/auth/outbound/exportExcel?warehouseId=' + formData.warehouseId, {
      headers: accessHeader(),
      responseType: 'blob',
    });

    const blob = new Blob([response.data], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    });

    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = '销售出库数量导入模版.xlsx';
    document.body.appendChild(a);
    a.click();
    a.remove();
    window.URL.revokeObjectURL(url);

  } catch (error) {
    console.error('下载模板失败', error);
    ElMessage.error('下载模板失败，请稍后重试');
  } finally {
    downloadLoading.value = false;
  }
};

// 应用导入数据到表格
const applyImportedData = (importedData) => {
  let successCount = 0;
  let failCount = 0;
  
  console.log('导入数据:', importedData);
  
  importedData.forEach(importedItem => {
    const existingProductIndex = allInventoryProducts.value.findIndex(p => 
      p.productId === importedItem.productId || p.sku === importedItem.sku
    );
    
    if (existingProductIndex >= 0) {
      console.log('匹配产品:', allInventoryProducts.value[existingProductIndex]);
      
      const product = allInventoryProducts.value[existingProductIndex];
      
      if (importedItem.quantity && importedItem.quantity > 0 && (!importedItem.price || importedItem.price === 0)) {
        console.log('情况1:', importedItem);
        product.quantity = importedItem.quantity || 0;
        product.price = product.originalPrice || 0;
        product.priceUnitUsd = product.originalPriceUnitUsd || 0;
      }
      else if (importedItem.quantity && importedItem.quantity > 0 && importedItem.price && importedItem.price > 0) {
        console.log('情况2:', importedItem);
        product.quantity = importedItem.quantity || 0;
        product.price = importedItem.price || 0;
        product.priceUnitUsd = importedItem.priceUnitUsd || product.originalPriceUnitUsd || 0;
      }
      else if ((!importedItem.quantity || importedItem.quantity === 0) && importedItem.price && importedItem.price > 0) {
        console.log('情况3:', importedItem);
        product.quantity = 0;
        product.price = importedItem.price || 0;
        product.priceUnitUsd = importedItem.priceUnitUsd || product.originalPriceUnitUsd || 0;
      }
      else {
        console.log('情况4:', importedItem);
        product.quantity = importedItem.quantity || 0;
        product.price = importedItem.price || product.originalPrice || 0;
        product.priceUnitUsd = importedItem.priceUnitUsd || product.originalPriceUnitUsd || 0;
      }
      
      product.remark = importedItem.remark || '';
      product.batchAllocations = [];
      
      console.log('更新后产品:', product);
      successCount++;
    } else {
      console.warn('未找到匹配的产品:', importedItem);
      failCount++;
    }
  });
  
  allInventoryProducts.value = [...allInventoryProducts.value];
  
  return { successCount, failCount };
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
      loadOutboundDetail(route.params.id);
    } else {
      formRef.value?.resetFields();
      formData.items = [];
      fileList.value = [];
      generateOrderNo();
      
      allInventoryProducts.value.forEach(product => {
        product.quantity = 0;
        product.price = 0;
        product.priceUnitUsd = 0;
        product.remark = '';
        product.batchAllocations = [];
      });
      
      allInventoryProducts.value = [...allInventoryProducts.value];
      
      ElMessage.success('表单已重置');
    }
  });
};

const handleSaveDraft = async () => {
  if (!await validateForm()) return;
  
  loading.value = true;
  try {
    const submitData = prepareSubmitData();
    submitData.status = 0;
    
    const url = isEditMode.value ? '/api/auth/outbound/update' : '/api/auth/outbound/create';
    const res = await post(url, submitData);
    if (res) {
      ElMessage.success(isEditMode.value ? '更新草稿成功' : '保存草稿成功');
      router.push('/index/CkOutboundManage');
    }
  } catch (error) {
    ElMessage.error(isEditMode.value ? '更新草稿失败' : '保存草稿失败');
  } finally {
    loading.value = false;
  }
};

const handleSubmit = async () => {
  if (!await validateForm()) return;
  
  const hasProducts = allInventoryProducts.value.some(p => p.quantity > 0);
  if (!hasProducts) {
    ElMessage.warning('请至少设置一个产品的出库数量');
    return;
  }
  
  const hasUnallocatedItems = allInventoryProducts.value.some(item => {
    if (item.quantity <= 0) return false;
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
    const submitData = prepareSubmitData();
    submitData.status = 1;
    
    const url = isEditMode.value ? '/api/auth/outbound/update' : '/api/auth/outbound/create';
    const res = await post(url, submitData);
    if (res) {
      ElMessage.success(isEditMode.value ? '更新成功' : '提交成功，等待审核');
      router.push('/index/salesOutboundManage');
    }
  } catch (error) {
    ElMessage.error(isEditMode.value ? '更新失败' : '提交失败');
  } finally {
    loading.value = false;
  }
};

// 准备提交数据
const prepareSubmitData = () => {
  let items = [];
  
  items = allInventoryProducts.value
    .filter(item => item.quantity > 0)
    .map(item => ({
      productId: item.productId,
      productName: item.productName,
      sku: item.sku,
      spec: item.spec,
      unit: item.unitName,
      color: item.color,
      currentStock: item.availableQuantity,
      quantity: item.quantity,
      price: item.price,
      priceTotal: item.price * (item.quantity || 0),
      priceUnitUsd: item.priceUnitUsd || 0,
      priceTotalUsd: (item.priceUnitUsd || 0) * (item.quantity || 0),
      batchAllocations: item.batchAllocations || [],
      remark: item.remark || ''
    }));
  
  return {
    ...formData,
    items: items,
    totalQuantity: totalQuantity.value,
    totalAmount: totalAmount.value,
    totalAmountUsd: totalAmountUsd.value
  };
};

const validateForm = async () => {
  if (!formRef.value) return false;
  
  try {
    await formRef.value.validate();
    
    const productsWithQuantity = allInventoryProducts.value.filter(p => p.quantity > 0);
    
    if (productsWithQuantity.length === 0) {
      ElMessage.warning('请至少设置一个产品的出库数量');
      return false;
    }
    
    const invalidProducts = productsWithQuantity.filter(p => (!p.price || p.price <= 0));
    if (invalidProducts.length > 0) {
      ElMessage.warning('请为所有出库数量大于0的产品设置有效的单价');
      return false;
    }

    const invalidUsdProducts = productsWithQuantity.filter(p => (!p.priceUnitUsd || p.priceUnitUsd <= 0));
    if (invalidUsdProducts.length > 0) {
      ElMessage.warning('请为所有出库数量大于0的产品设置有效的USD单价');
      return false;
    }
    
    return true;
  } catch (error) {
    ElMessage.warning('请完善表单信息');
    return false;
  }
};

// 文件选择处理
const handleFileChange = (file) => {
  const isLt10M = file.size / 1024 / 1024 < 10;
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过10MB!');
    return;
  }
  
  currentFile.value = file;
};

// 清空选择的文件
const clearSelectedFile = () => {
  currentFile.value = null;
  importResult.value = null;
};

// 导入提交处理
const handleImportSubmit = async () => {
  console.log("开始导入销售出库数量");
  if (!formData.warehouseId) {
    ElMessage.warning('请先选择仓库');
    return;
  }
  console.log("开始导入销售出库数量。仓库ID:", formData.warehouseId);
  if (!currentFile.value) {
    ElMessage.warning('请选择要上传的文件');
    return;
  }

  try {
    importLoading.value = true;
    const fd = new FormData();

    const realFile = currentFile.value.raw || currentFile.value;
    fd.append('file', realFile);
    fd.append('warehouseId', formData.warehouseId);

    console.log('FormData:');
    for (let [k, v] of fd.entries()) console.log(k, v);

    ElMessage.info('开始导入数据，请稍候...');

    const result = await post('/api/auth/outbound/importOutboundSaleQuantity', fd);
    console.log('导入响应:', result);

    if (result) {
      importResult.value = result;
      ElMessage.success(`导入成功！`);
      
      if (result && Array.isArray(result)) {
        allInventoryProducts.value.forEach(product => {
          product.quantity = 0;
          product.price = 0;
          product.remark = '';
          product.batchAllocations = [];
        });
        
        const { successCount, failCount } = applyImportedData(result);
        
        if (successCount > 0) {
          ElMessage.success(`成功导入 ${successCount} 条产品记录`);
        }
        if (failCount > 0) {
          ElMessage.warning(`${failCount} 条记录未找到匹配的产品`);
        }
        
        importDialog.visible = false;
        currentFile.value = null;
        importResult.value = null;
      }
    } else {
      ElMessage.error('导入失败，请检查数据格式');
    }
  } catch (error) {
    console.error('导入失败详情:', error);
    ElMessage.error("导入失败，请重试");
    importResult.value = {
      success: false,
      message: error.message || '导入失败，请重试'
    };
  } finally {
    importLoading.value = false;
  }
};

// 导入Excel
const handleImportExcel = () => {
  if (!formData.warehouseId) {
    ElMessage.warning('请先选择仓库');
    return;
  }
  
  importDialog.visible = true;
  currentFile.value = null;
  importResult.value = null;
};

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
    
    productStockMap.value = {};
    inventoryList.value.forEach(item => {
      productStockMap.value[item.productId] = item.availableQuantity;
      if (item.price && item.price > 0) {
        item.priceFromApi = item.price;
      }
    });
    
    console.log('库存映射表:', productStockMap.value);
    
    updateInventoryProducts();
    
  } catch (error) {
    ElMessage.error('加载库存数据失败');
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
    loadOutboundDetail(route.params.id);
  } else {
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
    } else {
      allInventoryProducts.value = [];
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

.header-right-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.tip {
  font-size: 12px;
  color: #909399;
}

.product-table {
  margin-bottom: 16px;
}

.product-name {
  font-weight: 500;
  margin-bottom: 4px;
}

.sku-text {
  font-size: 12px;
  color: #909399;
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

.shelf-allocation-container {
  padding: 8px 0;
}

.shelf-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.shelf-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 8px;
  background-color: #f8f9fa;
  border-radius: 4px;
  border: 1px solid #ebeef5;
  gap: 12px;
}

.shelf-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  flex: 6;
  min-width: 0;
}

.shelf-name {
  font-size: 13px;
  font-weight: 500;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.shelf-quantity {
  font-size: 12px;
  color: #909399;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.shelf-input {
  width: 100%;
  flex: 4;
  max-width: 120px;
}

.no-shelf {
  text-align: center;
  padding: 16px;
}

.no-shelf-text {
  color: #909399;
  font-size: 13px;
}

.allocation-zero {
  color: #909399;
}

.allocation-normal {
  color: #67C23A;
  font-weight: bold;
}

.allocation-exceed {
  color: #F56C6C;
  font-weight: bold;
}

.batch-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

.import-dialog-content {
  padding: 20px 0;
}

.upload-demo {
  margin-bottom: 20px;
}

.selected-file {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 16px 0;
  padding: 8px 12px;
  background-color: #f8f9fa;
  border-radius: 4px;
  border: 1px solid #ebeef5;
}

.import-actions {
  margin: 16px 0;
  display: flex;
  justify-content: flex-end;
}

.import-tips {
  margin-top: 20px;
  padding: 16px;
  border-radius: 4px;
}

.import-tips h4 {
  margin: 0 0 12px 0;
  color: #303133;
}

.success-result {
  color: #67C23A;
  background-color: #f0f9ff;
  padding: 12px;
  border-radius: 4px;
}

.error-result {
  color: #F56C6C;
  background-color: #fef0f0;
  padding: 12px;
  border-radius: 4px;
}

.shelf-disabled {
  background-color: #f5f7fa;
  opacity: 0.6;
}

.shelf-disabled .shelf-name,
.shelf-disabled .shelf-quantity {
  color: #c0c4cc;
}

.text-disabled {
  color: #c0c4cc;
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

:deep(.batch-table .el-input-number.is-disabled) {
  background-color: #f5f7fa;
  border-color: #e4e7ed;
}

:deep(.batch-table .el-input-number.is-disabled .el-input__inner) {
  color: #c0c4cc;
  background-color: #f5f7fa;
}

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
  
  .header-right-actions {
    width: 100%;
    justify-content: flex-start;
    flex-wrap: wrap;
  }
  
  .summary-info .el-col {
    margin-bottom: 8px;
  }
  
  .shelf-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .shelf-info {
    width: 100%;
    flex-direction: row;
    justify-content: space-between;
    flex: none;
  }
  
  .shelf-input {
    width: 100%;
    max-width: none;
    flex: none;
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