<!-- ProductionPickingCreate.vue -->
<template>
  <div class="outbound-create-container">
    <el-card class="form-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">{{ isEditMode ? '编辑生产领料单' : '新建生产领料单' }}</span>
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
                  :value="2"
                  label="生产领料"
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
              type="primary" 
              @click="handleAddProduct"
              :disabled="!formData.warehouseId"
            >
              <el-icon><Plus /></el-icon>
              添加产品
            </el-button>
          </div>
        </div>

        <!-- 生产领料的产品表格 -->
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
                  v-for="product in productionProducts"
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
          <el-table-column label="领料数量" width="120">
            <template #default="{ row, $index }">
              <el-input-number
                v-model="row.quantity"
                :min="1"
                controls-position="right"
                style="width: 100%"
                @blur="() => handleQuantityBlur($index)"
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

        <!-- BOM原料分配部分 -->
        <div class="bom-section" v-if="hasBomData">
          <div class="section-header">
            <h3>原料分配</h3>
            <span class="bom-tip">根据产品BOM自动计算所需原料</span>
          </div>
          
          <div class="bom-content" v-for="(item, itemIndex) in formData.items" :key="itemIndex">
            <div class="bom-item-header" v-if="getBomData(item.productId)?.length">
              <h4>{{ item.productName }} ({{ item.quantity || 0 }} {{ item.unit }}) 所需原料:</h4>
              <div class="allocation-summary">
                <span v-for="bomItem in getBomData(item.productId)" 
                      :key="bomItem.componentProductId" 
                      class="summary-item" :class="{ 'insufficient': isInsufficient(itemIndex, bomItem) }">
                  {{ bomItem.componentProductName }}: 
                  已分配 {{ getAllocatedQuantityForComponent(itemIndex, bomItem.componentProductId) }} / 
                  总需求 {{ calculateRequiredQuantity(bomItem.quantity, item.quantity) }}
                  <span v-if="isInsufficient(itemIndex, bomItem)" class="insufficient-tip">
                    (不足 {{ calculateShortage(itemIndex, bomItem) }})
                  </span>
                </span>
              </div>
            </div>
            
            <el-table
              :data="getBomData(item.productId)"
              border
              class="bom-table"
              v-if="getBomData(item.productId)?.length"
            >
              <el-table-column type="index" label="序号" width="60" align="center" />
              <el-table-column label="原料信息" min-width="100">
                <template #default="{ row }">
                  <div>
                    <div>{{ row.componentProductName }}</div>
                    <div class="sku-text">{{ row.componentProductSku }}</div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="规格" width="120">
                <template #default="{ row }">
                  <span>{{ row.componentProductSpec || '-' }}</span>
                </template>
              </el-table-column>
              <el-table-column label="单位" width="80" align="center">
                <template #default="{ row }">
                  <span>{{ row.componentProductUnit || '-' }}</span>
                </template>
              </el-table-column>
              <el-table-column label="单件用量" width="100" align="center">
                <template #default="{ row }">
                  <span>{{ row.quantity }}</span>
                </template>
              </el-table-column>
              <el-table-column label="总需求量" width="120" align="center">
                <template #default="{ row }">
                  <span class="required-quantity">{{ calculateRequiredQuantity(row.quantity, item.quantity) }}</span>
                </template>
              </el-table-column>

              <!-- 原料分配列 -->
              <el-table-column label="原料分配" min-width="400">
                <template #default="{ row }">
                  <div class="allocation-container">
                    <!-- 批次维度展示 -->
                    <div v-for="batch in getBatchDataForComponent(row.componentProductId)" 
                        :key="batch.batchNo" 
                        class="batch-allocation">
                      <div class="batch-info">
                        <strong>批次 {{ batch.batchNo }}</strong>
                        <span class="real-time-info">
                          批次实时可用: {{ getRealTimeAvailableInfo(row.componentProductId, batch.batchNo) }}
                        </span>
                      </div>
                      <div class="shelf-allocation" v-if="batch.shelfList && batch.shelfList.length > 0">
                        <div v-for="shelf in batch.shelfList" 
                            :key="shelf.shelfId" 
                            class="shelf-item">
                          <div class="shelf-info">
                            <span>货架 {{ shelf.shelfName }}</span>
                            <span class="real-time-info">
                              货架实时可用: {{ getRealTimeAvailableInfo(row.componentProductId, batch.batchNo, shelf.shelfId) }}
                            </span>
                          </div>
                          <el-input-number
                            :model-value="getAllocationQuantity(itemIndex, row.componentProductId, batch.batchNo, shelf.shelfId)"
                            @update:model-value="(value) => updateAllocationQuantity(value, itemIndex, row, batch, shelf, item.productId)"
                            @blur="() => handleAllocationBlur(itemIndex, row, batch, shelf, item.productId)"
                            :min="0"
                            :max="999999"
                            :precision="4"
                            :step="1"
                            controls-position="right"
                            size="small"
                            placeholder="使用数量"
                            class="allocation-input"
                            :disabled="loadingCheck"
                          />
                        </div>
                      </div>
                      <div v-else class="no-shelf-allocation">
                        <span class="no-shelf-text">无货架信息</span>
                      </div>
                    </div>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>

        <!-- 统计信息 -->
        <div class="summary-info" v-if="formData.items.length > 0">
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
                <span class="label">库存状态：</span>
                <span class="value" :class="stockStatusClass">
                  {{ stockStatusText }}
                </span>
              </div>
            </el-col>
            <el-col :span="4">
              <div class="summary-item">
                <span class="label">实时检查：</span>
                <span class="value" :class="allocationCheckStatusClass">
                  {{ allocationCheckStatusText }}
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
import { ref, reactive, computed, onMounted, watch, nextTick } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Delete, Upload } from '@element-plus/icons-vue';
import { post, get } from '@/net';

const router = useRouter();
const route = useRoute();
const formRef = ref();
const loading = ref(false);
const loadingCheck = ref(false);

// 存储接口返回的最新分配数据
const latestAllocationData = ref({});

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
  relatedOrderNo: '',
  remark: '',
  status: 0,
  items: [],
  attachments: []
});

// 选项数据
const warehouseList = ref([]);
const productionProductList = ref([]);
const fileList = ref([]);

const allocationCheckResult = ref({ success: true, message: '' });

// 计算属性
const hasBomData = computed(() => {
  return formData.items.some(item => {
    const product = productionProductList.value.find(p => p.id === item.productId);
    return product?.bomData?.length > 0;
  });
});

const productTypeCount = computed(() => {
  return formData.items.length;
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

const totalAmountUsd = computed(() => {
  return formData.items.reduce((sum, item) => {
    const priceUsd = item.priceUnitUsd || 0;
    const quantity = item.quantity || 0;
    return sum + (priceUsd * quantity);
  }, 0);
});

const hasInsufficientStock = computed(() => {
  const validationResult = validateBomAllocations();
  return !validationResult.valid;
});

const stockStatusText = computed(() => {
  return hasInsufficientStock.value ? '原料分配不足' : '分配完成';
});

const stockStatusClass = computed(() => {
  return hasInsufficientStock.value ? 'status-warning' : 'status-success';
});

const allocationCheckStatusText = computed(() => {
  return allocationCheckResult.value.success ? '库存正常' : '库存不足';
});

const allocationCheckStatusClass = computed(() => {
  return allocationCheckResult.value.success ? 'status-success' : 'status-error';
});

// 生产领料可用产品
const productionProducts = computed(() => {
  return productionProductList.value.map(product => ({
    id: product.id,
    sku: product.sku,
    name: product.name,
    spec: product.spec,
    unit: product.unitName,
    bomData: product.bomData || []
  }));
});

// 表单验证规则
const formRules = {
  orderType: [
    { required: true, message: '请选择出库类型', trigger: 'change' }
  ],
  warehouseId: [
    { required: true, message: '请选择出库仓库', trigger: 'change' }
  ],
  expectedDate: [
    { required: true, message: '请选择预计出库日期', trigger: 'change' }
  ]
};

// 构建检查请求数据
const buildAllocationCheckRequest = (currentProductId = null) => {
  const productAllocations = formData.items.map((item, index) => {
    const productInfo = productionProductList.value.find(p => p.id === item.productId);
    
    const bomAllocationsWithTotal = (item.bomAllocations || []).map(allocation => {
      const bomItem = item.bomData?.find(b => b.componentProductId === allocation.componentProductId);
      const totalBomQuantity = bomItem ? calculateRequiredQuantity(bomItem.quantity, item.quantity) : 0;
      
      return {
        componentProductId: allocation.componentProductId,
        componentProductName: allocation.componentProductName,
        batchNo: allocation.batchNo,
        shelfId: allocation.shelfId,
        shelfName: allocation.shelfName,
        quantity: parseFloat(allocation.quantity) || 0,
        totalBomQuantity: parseFloat(totalBomQuantity) || 0
      };
    });

    return {
      productId: item.productId,
      productName: productInfo?.name || item.productName,
      productQuantity: item.quantity || 0,
      bomAllocations: bomAllocationsWithTotal
    };
  });

  return {
    warehouseId: formData.warehouseId,
    tenantId: 1,
    currentProductId: currentProductId,
    productAllocations: productAllocations
  };
};

// 检查分配数量 - 失去焦点时调用
const checkBatchAllocation = async (currentProductId = null) => {
  if (!formData.warehouseId || formData.items.length === 0) {
    return { success: true };
  }

  if (loadingCheck.value) {
    return { success: true };
  }

  loadingCheck.value = true;
  try {
    const requestData = buildAllocationCheckRequest(currentProductId);
    console.log('调用检查分配接口，请求数据:', requestData);
    
    const res = await post('/api/auth/inventory/checkBatchAllocation', requestData);
    
    if (res) {
      console.log('检查分配接口响应:', res);
      
      // 存储接口返回的最新数据
      if (res.batchAllocatedList) {
        updateLatestAllocationData(res.batchAllocatedList);
      }
      
      allocationCheckResult.value = {
        success: res.success,
        message: res.message || ''
      };
      
      return res;
    }
    return { success: true };
  } catch (error) {
    console.error('检查分配数量失败:', error);
    allocationCheckResult.value = { success: true, message: '' };
    return { success: true };
  } finally {
    loadingCheck.value = false;
  }
};

// 更新最新的分配数据
const updateLatestAllocationData = (batchAllocatedList) => {
  const newData = {};
  
  batchAllocatedList.forEach(parentDto => {
    parentDto.productSonDtoList?.forEach(sonDto => {
      sonDto.batchList?.forEach(batch => {
        batch.shelfList?.forEach(shelf => {
          const key = `${shelf.productParentId}_${shelf.productSonId}_${shelf.batchNo}_${shelf.shelfId}`;
          newData[key] = {
            shelfAvailableQuantity: shelf.shelfAvailableQuantity,
            availableBatchQuantity: batch.availableBatchQuantity,
            allocatedQuantity: shelf.allocatedQuantity
          };
        });
      });
    });
  });
  
  latestAllocationData.value = newData;
};

// 获取实时可用信息 - 直接从接口返回数据获取
const getRealTimeAvailableInfo = (componentProductId, batchNo, shelfId = null) => {
  // 查找对应的数据
  for (const [key, data] of Object.entries(latestAllocationData.value)) {
    const parts = key.split('_');
    const productSonId = parts[1];
    const dataBatchNo = parts[2];
    const dataShelfId = parts[3];
    
    if (productSonId == componentProductId && dataBatchNo === batchNo) {
      if (shelfId && dataShelfId == shelfId) {
        // 返回货架级别的可用数量
        return data.shelfAvailableQuantity?.toFixed(4) || '0.0000';
      } else if (!shelfId) {
        // 返回批次级别的可用数量
        return data.availableBatchQuantity?.toFixed(4) || '0.0000';
      }
    }
  }
  
  return '0.0000';
};

// 获取分配数量的方法 - 直接从接口返回数据获取
const getAllocationQuantity = (itemIndex, componentProductId, batchNo, shelfId) => {
  const item = formData.items[itemIndex];
  
  // 首先从接口返回数据中查找
  for (const [key, data] of Object.entries(latestAllocationData.value)) {
    const parts = key.split('_');
    const productParentId = parts[0];
    const productSonId = parts[1];
    const dataBatchNo = parts[2];
    const dataShelfId = parts[3];
    
    if (productParentId == item.productId && 
        productSonId == componentProductId && 
        dataBatchNo === batchNo && 
        dataShelfId == shelfId) {
      return parseFloat(data.allocatedQuantity) || 0;
    }
  }
  
  // 如果没有接口数据，从本地数据获取
  if (!item.bomAllocations) return 0;
  
  const allocation = item.bomAllocations.find(a => 
    a.componentProductId === componentProductId &&
    a.batchNo === batchNo &&
    a.shelfId === shelfId
  );
  
  return parseFloat(allocation?.quantity) || 0;
};

// 更新分配数量的方法 - 只更新本地数据
const updateAllocationQuantity = (value, itemIndex, bomRow, batch, shelf, productId) => {
  const quantity = parseFloat(value) || 0;
  const item = formData.items[itemIndex];
  
  if (!item.bomAllocations) {
    item.bomAllocations = [];
  }
  
  const allocationIndex = item.bomAllocations.findIndex(a => 
    a.componentProductId === bomRow.componentProductId &&
    a.batchNo === batch.batchNo &&
    a.shelfId === shelf.shelfId
  );

  if (quantity > 0) {
    if (allocationIndex >= 0) {
      item.bomAllocations[allocationIndex].quantity = quantity;
    } else {
      item.bomAllocations.push({
        componentProductId: bomRow.componentProductId,
        componentProductName: bomRow.componentProductName,
        componentProductSku: bomRow.componentProductSku,
        batchNo: batch.batchNo,
        shelfId: shelf.shelfId,
        shelfName: shelf.shelfName || '默认货架',
        quantity: quantity
      });
    }
  } else if (allocationIndex >= 0) {
    item.bomAllocations.splice(allocationIndex, 1);
  }
};

// 原料分配输入框失去焦点处理
const handleAllocationBlur = async (itemIndex, bomRow, batch, shelf, productId) => {
  await checkBatchAllocation(productId);
};

// 领料数量输入框失去焦点处理
const handleQuantityBlur = async (index) => {
  const item = formData.items[index];
  await checkBatchAllocation(item.productId);
};

// 加载出库单详情
const loadOutboundDetail = async (id) => {
  loading.value = true;
  try {
    const res = await get(`/api/auth/outbound/detail?orderId=${id}`);
    console.log('出库单详情响应:', res);
    
    if (res) {
      const detailData = res.data || res;
      
      Object.assign(formData, {
        id: detailData.id,
        orderNo: detailData.orderNo,
        orderType: detailData.orderType,
        warehouseId: detailData.warehouseId,
        expectedDate: detailData.expectedDate,
        relatedOrderNo: detailData.relatedOrderNo || '',
        remark: detailData.remark || '',
        status: detailData.status
      });

      if (detailData.items && detailData.items.length > 0) {
        formData.items = detailData.items.map(item => {
          return {
            productId: item.productId,
            productName: item.productName || '',
            sku: item.sku || '',
            spec: item.spec || '',
            unit: item.unit || '',
            currentStock: item.currentStock || 0,
            quantity: item.quantity || 1,
            price: item.price || 0,
            priceUnitUsd: item.priceUnitUsd || 0,
            batchAllocations: item.batchAllocations || [],
            availableBatches: item.availableBatches || [],
            remark: item.remark || '',
            bomAllocations: item.bomAllocations || [],
            bomData: item.bomData || []
          };
        });

        for (let i = 0; i < formData.items.length; i++) {
          const item = formData.items[i];
          if (item.productId) {
            await loadBatchInfoForProduction(item.productId, i);
          }
        }
        
        await checkBatchAllocation(null);
      } else {
        formData.items = [];
      }

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
    formData.items = [];
    latestAllocationData.value = {};
    allocationCheckResult.value = { success: true, message: '' };
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
    priceUnitUsd: 0,
    batchAllocations: [],
    availableBatches: [],
    remark: '',
    bomAllocations: [],
    bomData: []
  });
};

const handleRemoveProduct = async (index) => {
  formData.items.splice(index, 1);
  await checkBatchAllocation(null);
};

const handleProductChange = async (productId, index) => {
  const product = productionProducts.value.find(p => p.id === productId);
  
  if (product) {
    const item = formData.items[index];
    item.productId = product.id;
    item.productName = product.name;
    item.sku = product.sku;
    item.spec = product.spec;
    item.unit = product.unit;
    item.quantity = item.quantity || 1;
    item.price = item.price || 0;
    item.priceUnitUsd = item.priceUnitUsd || 0;
    item.bomAllocations = item.bomAllocations || [];
    
    await loadBatchInfoForProduction(productId, index);
    await initializeBomAllocations(item);
    await checkBatchAllocation(productId);
  }
};

// 初始化BOM分配数据
const initializeBomAllocations = async (item) => {
  if (!item.productId || !item.bomData || item.bomData.length === 0) {
    return;
  }
  
  // 清空现有分配
  item.bomAllocations = [];
  
  // 为每个BOM项创建空的分配记录
  item.bomData.forEach(bomItem => {
    if (bomItem.batches && bomItem.batches.length > 0) {
      bomItem.batches.forEach(batch => {
        if (batch.shelfList && batch.shelfList.length > 0) {
          batch.shelfList.forEach(shelf => {
            item.bomAllocations.push({
              componentProductId: bomItem.componentProductId,
              componentProductName: bomItem.componentProductName,
              componentProductSku: bomItem.componentProductSku,
              batchNo: batch.batchNo,
              shelfId: shelf.shelfId,
              shelfName: shelf.shelfName || `货架${shelf.shelfId}`,
              quantity: 0
            });
          });
        }
      });
    }
  });
};

// BOM相关方法
const getBomData = (productId) => {
  const itemWithBomData = formData.items.find(item => item.productId === productId && item.bomData);
  if (itemWithBomData && itemWithBomData.bomData.length > 0) {
    return itemWithBomData.bomData;
  }
  
  const product = productionProductList.value.find(p => p.id === productId);
  return product?.bomData || [];
};

// 为生产领料加载批次信息的方法
const loadBatchInfoForProduction = async (productId, index) => {
  try {
    const item = formData.items[index];
    const product = productionProductList.value.find(p => p.id === productId);
    if (!product?.bomData) {
      item.bomData = [];
      return;
    }
    
    const bomDataWithBatches = await Promise.all(
      product.bomData.map(async (bomItem) => {
        const batches = await loadBatchInfoForComponent(bomItem.componentProductId);

        const processedBatches = batches.map(batch => ({
          ...batch,
          shelfList: batch.shelfList?.map(shelf => ({
            ...shelf,
            shelfName: shelf.shelfName || `货架${shelf.shelfId}`
          })) || []
        }));

        return {
          ...bomItem,
          componentProductId: bomItem.componentProductId,
          componentProductName: bomItem.componentProductName,
          componentProductSku: bomItem.componentProductSku,
          batches: processedBatches || []
        };
      })
    );
    
    item.bomData = bomDataWithBatches;
    console.log('生产领料批次信息加载完成:', item.bomData);
    
  } catch (error) {
    console.error('加载生产领料批次信息失败:', error);
    formData.items[index].bomData = [];
  }
};

// 获取原料组件的批次数据
const getBatchDataForComponent = (componentProductId) => {
  for (const item of formData.items) {
    if (item.bomData) {
      const bomItem = item.bomData.find(b => b.componentProductId === componentProductId);
      if (bomItem && bomItem.batches) {
        return bomItem.batches;
      }
    }
  }
  return [];
};

// 加载原料批次信息
const loadBatchInfoForComponent = async (componentProductId) => {
  try {
    const res = await get(`/api/auth/inventory/batches?productId=${componentProductId}&warehouseId=${formData.warehouseId}`);
    console.log('原料批次信息响应:', res);
    
    if (res && Array.isArray(res)) {
      return res;
    }
    return [];
  } catch (error) {
    console.error('加载原料批次信息失败:', error);
    return [];
  }
};

// 计算总需求量
const calculateRequiredQuantity = (unitQuantity, productQuantity) => {
  const unitQty = parseFloat(unitQuantity) || 0;
  const productQty = parseFloat(productQuantity) || 0;
  return (unitQty * productQty).toFixed(4);
};

// 获取某个原料的总分配数量
const getAllocatedQuantityForComponent = (itemIndex, componentProductId) => {
  const item = formData.items[itemIndex];
  if (!item.bomAllocations) return 0;
  
  return item.bomAllocations
    .filter(a => a.componentProductId === componentProductId)
    .reduce((sum, a) => sum + (parseFloat(a.quantity) || 0), 0)
    .toFixed(4);
};

// 检查原料是否不足
const isInsufficient = (itemIndex, bomItem) => {
  const requiredQuantity = parseFloat(calculateRequiredQuantity(bomItem.quantity, formData.items[itemIndex].quantity));
  const allocatedQuantity = parseFloat(getAllocatedQuantityForComponent(itemIndex, bomItem.componentProductId));
  return allocatedQuantity < requiredQuantity;
};

// 计算不足数量
const calculateShortage = (itemIndex, bomItem) => {
  const requiredQuantity = parseFloat(calculateRequiredQuantity(bomItem.quantity, formData.items[itemIndex].quantity));
  const allocatedQuantity = parseFloat(getAllocatedQuantityForComponent(itemIndex, bomItem.componentProductId));
  return (requiredQuantity - allocatedQuantity).toFixed(4);
};

// 验证BOM分配
const validateBomAllocations = () => {
  for (let i = 0; i < formData.items.length; i++) {
    const item = formData.items[i];
    const bomData = getBomData(item.productId);
    
    if (bomData.length > 0) {
      for (const bomItem of bomData) {
        const requiredQuantity = calculateRequiredQuantity(bomItem.quantity, item.quantity);
        const allocatedQuantity = getAllocatedQuantityForComponent(i, bomItem.componentProductId);
        
        if (parseFloat(allocatedQuantity) < parseFloat(requiredQuantity)) {
          return {
            valid: false,
            message: `${item.productName} 所需的原料 ${bomItem.componentProductName} 分配数量不足，需要 ${requiredQuantity}，已分配 ${allocatedQuantity}`
          };
        }
      }
    }
  }
  
  return { valid: true };
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
      latestAllocationData.value = {};
      allocationCheckResult.value = { success: true, message: '' };
      generateOrderNo();
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
      router.push('/index/CkOutboundManage/');
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
  
  const validationResult = validateBomAllocations();
  if (!validationResult.valid) {
    ElMessage.warning(validationResult.message);
    return;
  }
  
  const checkResult = await checkBatchAllocation(null);
  if (!checkResult.success) {
    ElMessage.warning(`库存分配存在问题: ${checkResult.message}`);
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
      router.push('/index/productionPickingManage');
    }
  } catch (error) {
    ElMessage.error(isEditMode.value ? '更新失败' : '提交失败');
  } finally {
    loading.value = false;
  }
};

// 准备提交数据
const prepareSubmitData = () => {
  const items = formData.items.map(item => {
    const productInfo = productionProductList.value.find(p => p.id === item.productId);
    
    return {
      productId: item.productId,
      productName: productInfo?.name || item.productName,
      sku: item.sku,
      spec: item.spec,
      unit: item.unit,
      currentStock: item.currentStock,
      quantity: item.quantity,
      price: item.price,
      priceUnitUsd: item.priceUnitUsd || 0,
      priceTotalUsd: (item.priceUnitUsd || 0) * (item.quantity || 0),
      batchAllocations: item.batchAllocations || [],
      remark: item.remark || '',
      bomAllocations: item.bomAllocations?.map(allocation => ({
        componentProductId: allocation.componentProductId,
        componentProductName: allocation.componentProductName,
        componentProductSku: allocation.componentProductSku,
        batchNo: allocation.batchNo,
        shelfId: allocation.shelfId,
        shelfName: allocation.shelfName,
        quantity: allocation.quantity
      })) || []
    };
  });
  
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
      
      if (item.priceUnitUsd < 0) {
        ElMessage.warning(`第 ${i + 1} 行产品的USD单价不能为负数`);
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

const loadProductionProducts = async () => {
  try {
    const res = await get('/api/auth/product/listEnable');
    productionProductList.value = res?.data || res || [];
  } catch (error) {
    console.error('加载生产产品列表失败:', error);
    productionProductList.value = [];
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
  loadProductionProducts();
});

watch(
  () => route.params.id,
  (newId) => {
    if (newId) {
      loadOutboundDetail(newId);
    } else {
      Object.assign(formData, {
        id: null,
        orderNo: '',
        orderType: 2,
        warehouseId: null,
        expectedDate: '',
        relatedOrderNo: '',
        remark: '',
        status: 0,
        items: [],
        attachments: []
      });
      fileList.value = [];
      latestAllocationData.value = {};
      allocationCheckResult.value = { success: true, message: '' };
      generateOrderNo();
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

.bom-tip {
  font-size: 14px;
  color: #909399;
}

.product-table {
  margin-bottom: 16px;
}

/* BOM相关样式 */
.bom-section {
  margin: 30px 0;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.bom-content {
  padding: 16px;
}

.bom-item-header {
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px dashed #dcdfe6;
}

.bom-item-header h4 {
  margin: 0;
  color: #409eff;
  font-size: 14px;
  margin-bottom: 8px;
}

.allocation-summary {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 12px;
}

.summary-item {
  padding: 4px 8px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.summary-item.insufficient {
  background-color: #fef0f0;
  color: #f56c6c;
  border: 1px solid #fbc4c4;
}

.insufficient-tip {
  color: #f56c6c;
  font-weight: bold;
}

.bom-table {
  margin-bottom: 16px;
}

.allocation-container {
  max-height: 200px;
  overflow-y: auto;
}

.batch-allocation {
  margin-bottom: 12px;
  padding: 8px;
  background-color: #f8f9fa;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
}

.batch-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 13px;
  padding: 4px 0;
  border-bottom: 1px dashed #dcdfe6;
}

.batch-info strong {
  color: #409eff;
}

.real-time-info {
  color: #67c23a;
  font-size: 12px;
  font-weight: bold;
}

.shelf-allocation {
  margin-left: 8px;
}

.shelf-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
  padding: 4px 8px;
  background-color: white;
  border-radius: 2px;
  gap: 12px;
}

.shelf-info {
  display: flex;
  justify-content: space-between;
  flex: 6;
  font-size: 12px;
}

.allocation-input {
  flex: 4;
  min-width: 100px;
}

.no-shelf-allocation {
  text-align: center;
  padding: 8px;
}

.no-shelf-text {
  color: #909399;
  font-size: 12px;
}

.required-quantity {
  color: #e6a23c;
  font-weight: bold;
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

.status-error {
  color: #F56C6C;
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
  
  .allocation-input {
    width: 100%;
  }
  
  .allocation-summary {
    font-size: 11px;
  }
  
  .batch-info {
    flex-direction: column;
    gap: 4px;
  }
  
  .shelf-info {
    flex-direction: column;
    gap: 2px;
  }
}
</style>