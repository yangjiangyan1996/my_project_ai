<template>
  <div class="outbound-create-container">

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
            <!-- <el-button 
              type="primary" 
              @click="handleSubmit" 
              :loading="loading"
              v-if="!isEditMode || (isEditMode && (formData.status === 0 || formData.status === 4))"
            >
              {{ isEditMode ? '更新提交' : '提交审核' }}
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
        class="outbound-form"
      >
        <el-row :gutter="24">
          <el-col :xs="24" :sm="12" :lg="8">
            <el-form-item label="关联单号" prop="relatedOrderNo">
              <el-input
                v-model="formData.relatedOrderNo"
                placeholder="请输入关联单号"
              />
            </el-form-item>
          </el-col>
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
        </el-row>

       <el-form-item label="备注" prop="remark">
        <el-input
          v-model="formData.remark"
          type="textarea"
          :rows="3"
          placeholder="请输入备注信息（备注将用于生产入库时的订单标题提示）"
          maxlength="500"
          show-word-limit
        />
        <div class="remark-tip">提示：此备注信息将作为生产入库单的标题提示，请填写清晰的生产要求或注意事项。</div>
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
          <el-table-column label="产品信息" min-width="150">
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
                  :label="`${product.sku}-${product.name}-${product.spec}-${product.color || '-'}`"
                  :value="product.id"
                />
              </el-select>
            </template>
          </el-table-column>

           <el-table-column label="名称" width="100">
            <template #default="{ row }">
              <span>{{ row.productName || '-' }}</span>
            </template>
          </el-table-column>

          <el-table-column label="规格" width="100">
            <template #default="{ row }">
              <span>{{ row.spec || '-' }}</span>
            </template>
          </el-table-column>
          <!-- 新增颜色列 -->
          <el-table-column label="颜色" width="100" align="center">
            <template #default="{ row }">
              <span>{{ row.color || '-' }}</span>
            </template>
          </el-table-column>
          
           <el-table-column label="sku" width="100">
            <template #default="{ row }">
              <span>{{ row.sku || '-' }}</span>
            </template>
          </el-table-column>

          <el-table-column label="单位" width="80" align="center">
            <template #default="{ row }">
              <span>{{ row.unit || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="领料数量" width="200">
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
          <el-table-column label="操作" width="100" fixed="right" align="center">
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
        <div class="bom-section" v-if="hasBomData && dataLoaded">
          <div class="section-header">
            <h3>原料分配</h3>
            <div class="header-right-actions">
              <span class="bom-tip">根据产品BOM自动计算所需原料</span>
              <el-button
                type="success"
                @click="handleAutoAllocateAll"
                :loading="autoAllocating"
                :disabled="!canAutoAllocate"
              >
                <el-icon><MagicStick /></el-icon>
                自动全部分配
              </el-button>
            </div>
          </div>
          
          <!-- 分配加载状态 -->
          <div v-if="isAllocating" class="allocation-loading">
            <div class="loading-content">
              <el-icon class="loading-icon"><Loading /></el-icon>
              <span class="loading-text">正在自动分配中，请稍候...</span>
              <div class="loading-progress">
                <div class="progress-bar" :style="{ width: allocationProgress + '%' }"></div>
              </div>
              <span class="progress-text">
                正在检查 {{ Math.min(currentCheckIndex, totalChecksCount) }}/{{ totalChecksCount }} 个分配记录
                ({{ Math.round(Math.min(allocationProgress, 100)) }}%)
              </span>
            </div>
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
              

              <!-- 修改原料信息列的模板 -->
              <el-table-column label="原料信息" min-width="150">
                <template #default="{ row }">
                  <div>
                    <div class="component-product-name">
                      {{ row.componentProductName }}
                      <!-- 如果有合并项，显示展开/收起按钮 -->
                      <el-button
                        v-if="row.usageDetailList && row.usageDetailList.length > 1"
                        type="primary"
                        link
                        size="small"
                        @click="toggleUsageDetails(row)"
                        class="toggle-usage-btn"
                      >
                        <el-icon v-if="row.showUsageDetails">
                          <ArrowUp />
                        </el-icon>
                        <el-icon v-else>
                          <ArrowDown />
                        </el-icon>
                        {{ row.showUsageDetails ? '收起' : '展开' }}详情
                      </el-button>
                    </div>
                    <div class="sku-text">{{ row.componentProductSku }}</div>
                    
                    <!-- 合并详情展开区域 -->
                    <el-collapse-transition>
                      <div 
                        v-if="row.showUsageDetails && row.usageDetailList && row.usageDetailList.length > 1"
                        class="usage-details-container"
                      >
                        <div class="usage-details-title">合并详情:</div>
                        <div 
                          v-for="(usageDetail, usageIndex) in row.usageDetailList" 
                          :key="usageDetail.bomDetailId || usageIndex"
                          class="usage-detail-item"
                        >
                          <div class="usage-detail-content">
                            <span class="usage-quantity">数量: {{ formatNumber(usageDetail.quantity) }}</span>
                            <span class="usage-type" v-if="getUsageTypeText(usageDetail.type)">用途: {{ getUsageTypeText(usageDetail.type) }}</span>
                            <span class="usage-remark" v-if="usageDetail.remark">备注: {{ usageDetail.remark }}</span>
                            <span class="usage-loss-rate" v-if="usageDetail.lossRate">损耗率: {{ formatNumber(usageDetail.lossRate) }}%</span>
                          </div>
                        </div>
                      </div>
                    </el-collapse-transition>
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
                <template #default="{ row, $index: bomIndex }">
                  <div class="allocation-container">
                    <!-- 批次维度展示 -->
                    <div v-for="batch in getBatchDataForComponent(row.componentProductId, item.productId)" 
                        :key="batch.batchNo" 
                        class="batch-allocation">
                      <div class="batch-info">
                        <strong>批次 {{ batch.batchNo }}</strong>
                        <span class="real-time-info">
                          批次实时可用: {{ getRealTimeAvailableInfo(item.productId, row.componentProductId, batch.batchNo) }}
                        </span>
                      </div>
                      <div class="shelf-allocation" v-if="batch.shelfList && batch.shelfList.length > 0">
                        <div v-for="shelf in batch.shelfList" 
                            :key="shelf.shelfId" 
                            class="shelf-item">
                          <div class="shelf-info">
                            <span>货架: {{ shelf.shelivesName }}</span>
                            <span>区域: {{ shelf.shelfName }}</span>
                            <span class="real-time-info">
                              货架实时可用: {{ getRealTimeAvailableInfo(item.productId, row.componentProductId, batch.batchNo, shelf.shelfId) }}
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
                            :disabled="loadingCheck || isAllocating"
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

        <!-- 加载状态提示 -->
        <div v-if="hasBomData && !dataLoaded" class="loading-section">
          <el-alert title="正在加载原料分配数据..." type="info" :closable="false" show-icon />
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
import { Plus, Delete, Upload,  ArrowUp, ArrowDown, MagicStick, Loading, ArrowLeft } from '@element-plus/icons-vue';
import { post, get } from '@/net';

const router = useRouter();
const route = useRoute();
const formRef = ref();
const loading = ref(false);
const loadingCheck = ref(false);
const dataLoaded = ref(false);
const autoAllocating = ref(false);
const isAllocating = ref(false); // 新增：分配中状态
const allocationProgress = ref(0); // 新增：分配进度
const currentCheckIndex = ref(0); // 新增：当前检查索引
const totalChecksCount = ref(0); // 新增：总检查数量

// 存储接口返回的最新分配数据
const latestAllocationData = ref({});
// 存储批次信息
const batchInfoCache = ref({});

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

const productTypeCount = computed(() => formData.items.length);
const totalQuantity = computed(() => {
  return formData.items.reduce((sum, item) => sum + (parseFloat(item.quantity) || 0), 0);
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

// 计算是否可以自动分配
const canAutoAllocate = computed(() => {
  return formData.warehouseId && 
         formData.items.length > 0 && 
         hasBomData.value && 
         dataLoaded.value &&
         !isAllocating.value; // 正在分配时禁用
});

// 生产领料可用产品
const productionProducts = computed(() => {
  return productionProductList.value.map(product => ({
    id: product.id,
    sku: product.sku,
    name: product.name,
    color: product.color,
    spec: product.spec,
    unit: product.unitName,
    bomData: product.bomData || []
  }));
});

// 表单验证规则
const formRules = {
  warehouseId: [
    { required: true, message: '请选择出库仓库', trigger: 'change' }
  ],
  expectedDate: [
    { required: true, message: '请选择预计出库日期', trigger: 'change' }
  ],
  remark: [
    { required: true, message: '请输入备注信息', trigger: 'blur' },
    { min: 2, message: '备注长度至少为2个字符', trigger: 'blur' }
  ]
};

// 添加格式数字的方法
const formatNumber = (value) => {
  if (!value) return '0';
  const num = parseFloat(value);
  return isNaN(num) ? '0' : num.toFixed(4);
};

// 切换使用详情显示状态
const toggleUsageDetails = (row) => {
  row.showUsageDetails = !row.showUsageDetails;
};

// 获取用途类型文本
const getUsageTypeText = (type) => {
  if (!type) return '';
  const typeMap = {
    0: '空',
    1: '主料', 
    2: '布料',
    10: '辅料',
    20:"五金",
    100:"包装"
  };
  return typeMap[type] || `类型${type}`;
};

// ==================== 自动分配相关方法 ====================

// 构建自动分配请求数据 - 调整为后端期望的格式
const buildAutoAllocationRequest = () => {
  // 构建 List<ShelfProductUsedAllReq> 格式
  const list = [];

  // 遍历每个成品
  formData.items.forEach((item, itemIndex) => {
    const productInfo = productionProductList.value.find(p => p.id === item.productId);
    
    // 遍历成品的每个原料
    if (item.bomData && item.bomData.length > 0) {
      item.bomData.forEach(bomItem => {
        // 获取该原料的总需求量
        const requiredQuantity = parseFloat(calculateRequiredQuantity(bomItem.quantity, item.quantity));
        
        if (requiredQuantity <= 0) return;
        
        // 为每个原料创建一个分配请求
        const allocationReq = {
          index: list.length + 1, // 分配一个序号
          productId: bomItem.componentProductId,
          sku: bomItem.componentProductSku,
          quantity: requiredQuantity,
          pcList: []
        };
        
        // 如果有批次信息，添加到请求中
        if (bomItem.batches && bomItem.batches.length > 0) {
          bomItem.batches.forEach(batch => {
            const batchReq = {
              batNo: batch.batchNo,
              createdAtOfBatch: batch.createdAtOfBatch || new Date(),
              shelfQuantityList: []
            };
            
            // 添加货架信息，使用实时可用数量
            if (batch.shelfList && batch.shelfList.length > 0) {
              batch.shelfList.forEach(shelf => {
                // 获取货架实时可用数量
                const realTimeAvailable = getRealTimeAvailableInfo(
                  item.productId,
                  bomItem.componentProductId,
                  batch.batchNo,
                  shelf.shelfId
                );
                
                batchReq.shelfQuantityList.push({
                  shelfId: shelf.shelfId,
                  shelfName: shelf.shelfName || `货架${shelf.shelfId}`,
                  quantity: parseFloat(realTimeAvailable) || 0
                });
              });
            }
            
            allocationReq.pcList.push(batchReq);
          });
        }
        
        list.push(allocationReq);
      });
    }
  });

  // 返回符合后端接口的格式
  return list; // 直接返回数组，后端期望接收的是数组
};

// 处理自动分配
const handleAutoAllocateAll = async () => {
  try {
    if (isEditMode.value) {
      const confirmed = await ElMessageBox.confirm(
        '编辑模式下进行自动分配会覆盖当前的分配数据，确定要继续吗？',
        '提示',
        {
          type: 'warning',
          confirmButtonText: '确定',
          cancelButtonText: '取消'
        }
      );
      
      if (!confirmed) {
        return;
      }
    }
    
    // 设置分配中状态
    isAllocating.value = true;
    autoAllocating.value = true;
    allocationProgress.value = 0;
    currentCheckIndex.value = 0;
    
    if (!formData.warehouseId) {
      ElMessage.warning('请先选择仓库');
      isAllocating.value = false;
      autoAllocating.value = false;
      return;
    }
    
    if (formData.items.length === 0) {
      ElMessage.warning('请先添加产品');
      isAllocating.value = false;
      autoAllocating.value = false;
      return;
    }
    
    for (const item of formData.items) {
      if (!item.quantity || item.quantity <= 0) {
        ElMessage.warning(`产品 ${item.productName || item.productId} 的数量必须大于0`);
        isAllocating.value = false;
        autoAllocating.value = false;
        return;
      }
    }
    
    // 确保有最新的库存数据
    console.log('自动分配前先进行库存检查...');
    await checkBatchAllocation(null, null, null);
    
    // 等待2秒，确保数据渲染完成
    await new Promise(resolve => setTimeout(resolve, 2000));
    
    // 构建请求数据
    const requestData = buildAutoAllocationRequest();
    
    // 验证请求数据
    if (requestData.length === 0) {
      ElMessage.warning('没有找到可分配的数据');
      isAllocating.value = false;
      autoAllocating.value = false;
      return;
    }
    
    console.log('自动分配请求数据:', JSON.stringify(requestData, null, 2));
    
    // 调用自动分配接口
    const result = await post('/api/auth/shelf/allocateIShelfnventoryQuantity', requestData);
    
    if (result && Array.isArray(result)) {
      console.log('自动分配接口返回结果:', result);
      await processAutoAllocationResult(result);
      ElMessage.success('自动分配成功');
      
    } else {
      ElMessage.error('自动分配失败，返回数据格式错误');
      isAllocating.value = false;
      autoAllocating.value = false;
    }
  } catch (error) {
    console.error('自动分配失败:', error);
    ElMessage.error('自动分配失败: ' + (error.message || '未知错误'));
    isAllocating.value = false;
    autoAllocating.value = false;
  }
};

// 处理自动分配结果
// 处理自动分配结果 - 修复版
const processAutoAllocationResult = async (result) => {
  // 备份原有的分配数据
  const backupAllocations = formData.items.map(item => 
    item.bomAllocations ? [...item.bomAllocations] : []
  );
  
  try {
    // 先清空所有分配数据
    formData.items.forEach(item => {
      if (item.bomAllocations) {
        item.bomAllocations = [];
      }
    });
    
    // 处理每个原料的分配结果
    result.forEach((allocationResp, index) => {
      const materialId = allocationResp.productId;
      
      // 找到这个原料对应的所有成品
      formData.items.forEach((item, itemIndex) => {
        const bomItem = item.bomData?.find(b => b.componentProductId == materialId);
        if (!bomItem) return;
        
        // 确保分配数组存在
        if (!item.bomAllocations) {
          item.bomAllocations = [];
        }
        
        // 处理批次分配结果
        allocationResp.batchAllocations?.forEach(batchAlloc => {
          const batchNo = batchAlloc.batchNo;
          
          batchAlloc.allocations?.forEach(shelfAlloc => {
            // 找到对应的批次和货架
            const batch = bomItem.batches?.find(b => b.batchNo === batchNo);
            if (!batch) return;
            
            const shelf = batch.shelfList?.find(s => s.shelfId == shelfAlloc.shelfId);
            if (!shelf) return;
            
            // 创建分配记录
            const allocationData = {
              componentProductId: materialId,
              componentProductName: bomItem.componentProductName,
              componentProductSku: bomItem.componentProductSku,
              batchNo: batchNo,
              shelfId: shelfAlloc.shelfId,
              shelfName: shelfAlloc.shelfName || shelf.shelfName || `货架${shelfAlloc.shelfId}`,
              quantity: parseFloat(shelfAlloc.quantity) || 0
            };
            
            // 添加到分配数组中
            item.bomAllocations.push(allocationData);
            
            console.log(`自动分配结果: 产品${item.productId} 原料${materialId} 批次${batchNo} 货架${shelfAlloc.shelfId} 数量${allocationData.quantity}`);
          });
        });
      });
    });
    
    // 重要：补充0分配的货架数据
    await supplementZeroAllocationShelves();
    
    // 等待DOM更新
    await nextTick();
    
    // 批量检查所有货架的实时数量（包括已分配和未分配的）
    const allShelvesToCheck = await collectAllShelvesToCheck();
    if (allShelvesToCheck.length > 0) {
      await batchCheckAllocations(allShelvesToCheck);
    } else {
      // 如果没有货架数据，触发一次整体检查
      await checkBatchAllocation(null, null, null);
    }
    
    // 分配完成后，等待2秒进行最终整体检查
    await new Promise(resolve => setTimeout(resolve, 2000));
    console.log('分配完成后进行最终整体检查...');
    await checkBatchAllocation(null, null, null);
    
    ElMessage.success('自动分配成功，已更新所有货架的实时数据');
    
  } catch (error) {
    console.error('处理分配结果失败:', error);
    
    // 恢复原有数据
    formData.items.forEach((item, index) => {
      item.bomAllocations = backupAllocations[index] || [];
    });
    
    if (isEditMode.value) {
      ElMessage.warning('自动分配失败，已恢复原始分配数据');
    } else {
      ElMessage.error('自动分配失败');
    }
  } finally {
    // 无论成功失败，都结束分配状态
    isAllocating.value = false;
    autoAllocating.value = false;
  }
};

// 补充0分配的货架数据
const supplementZeroAllocationShelves = async () => {
  console.log('开始补充0分配的货架数据...');
  
  formData.items.forEach((item, itemIndex) => {
    if (!item.bomAllocations) {
      item.bomAllocations = [];
    }
    
    if (item.bomData && item.bomData.length > 0) {
      item.bomData.forEach(bomItem => {
        if (bomItem.batches && bomItem.batches.length > 0) {
          bomItem.batches.forEach(batch => {
            if (batch.shelfList && batch.shelfList.length > 0) {
              batch.shelfList.forEach(shelf => {
                // 检查这个货架是否已经有分配数据
                const existingAllocation = item.bomAllocations.find(a => 
                  a.componentProductId === bomItem.componentProductId &&
                  a.batchNo === batch.batchNo &&
                  a.shelfId === shelf.shelfId
                );
                
                // 如果没有分配数据，添加一个0分配的记录
                if (!existingAllocation) {
                  const zeroAllocation = {
                    componentProductId: bomItem.componentProductId,
                    componentProductName: bomItem.componentProductName,
                    componentProductSku: bomItem.componentProductSku,
                    batchNo: batch.batchNo,
                    shelfId: shelf.shelfId,
                    shelfName: shelf.shelfName || `货架${shelf.shelfId}`,
                    quantity: 0
                  };
                  
                  item.bomAllocations.push(zeroAllocation);
                  console.log(`补充0分配数据: 产品${item.productId} 原料${bomItem.componentProductId} 批次${batch.batchNo} 货架${shelf.shelfId} 数量0`);
                }
              });
            }
          });
        }
      });
    }
  });
  
  console.log('0分配货架数据补充完成');
};

// 批量检查所有分配记录的实时数量
// 批量检查所有分配记录的实时数量 - 修改为检查所有货架
// 批量检查所有分配记录的实时数量 - 优化版
const batchCheckAllocations = async (checkRequests) => {
  if (!checkRequests || checkRequests.length === 0) {
    allocationProgress.value = 100;
    return;
  }
  
  console.log(`开始批量检查 ${checkRequests.length} 个分配记录`);
  
  // 设置总检查数量
  totalChecksCount.value = checkRequests.length;
  currentCheckIndex.value = 0;
  
  console.log(`总共需要检查 ${totalChecksCount.value} 个货架`);
  
  // 按原料-批次-货架分组，减少重复检查
  const groupedChecks = {};
  
  checkRequests.forEach(shelf => {
    const groupKey = `${shelf.productId}_${shelf.componentProductId}_${shelf.batchNo}_${shelf.shelfId}`;
    if (!groupedChecks[groupKey]) {
      groupedChecks[groupKey] = shelf;
    }
  });
  
  const uniqueChecks = Object.values(groupedChecks);
  console.log(`去重后需要检查 ${uniqueChecks.length} 个唯一货架`);
  
  // 按成品分组
  const checksByProduct = {};
  uniqueChecks.forEach(shelf => {
    const key = shelf.productId;
    if (!checksByProduct[key]) {
      checksByProduct[key] = [];
    }
    checksByProduct[key].push(shelf);
  });
  
  const productKeys = Object.keys(checksByProduct);
  
  for (let i = 0; i < productKeys.length; i++) {
    const productId = productKeys[i];
    const shelves = checksByProduct[productId];
    
    console.log(`检查成品 ${productId} 的 ${shelves.length} 个货架`);
    
    // 逐个检查每个货架
    for (let j = 0; j < shelves.length; j++) {
      const shelf = shelves[j];
      
      try {
        console.log(`检查货架 ${j+1}/${shelves.length}: 成品${shelf.productId} 原料${shelf.componentProductId} 批次${shelf.batchNo} 货架${shelf.shelfId}`);
        
        // 更新进度
        currentCheckIndex.value++;
        allocationProgress.value = Math.round((currentCheckIndex.value / totalChecksCount.value) * 100);
        
        // 调用检查接口
        const checkResult = await checkBatchAllocation(
          shelf.productId,
          shelf.batchNo,
          shelf.shelfId
        );
        
        if (checkResult && checkResult.success) {
          console.log(`√ 货架检查成功: ${shelf.productId}-${shelf.componentProductId}-${shelf.batchNo}-${shelf.shelfId}`);
        } else {
          console.warn(`× 货架检查失败: ${shelf.productId}-${shelf.componentProductId}-${shelf.batchNo}-${shelf.shelfId}`);
        }
        
        // 检查间隔缩短到500毫秒，提高效率
        if (j < shelves.length - 1) {
          await new Promise(resolve => setTimeout(resolve, 500));
        }
        
      } catch (error) {
        console.error(`货架检查异常:`, error);
      }
    }
    
    // 成品间检查间隔500毫秒
    if (i < productKeys.length - 1) {
      await new Promise(resolve => setTimeout(resolve, 500));
    }
  }
  
  // 检查完成
  allocationProgress.value = 100;
  console.log('批量检查完成');
};

// 收集所有需要检查的货架（包括已分配和未分配的）
const collectAllShelvesToCheck = async () => {
  const allShelves = [];
  
  // 遍历所有成品
  formData.items.forEach((item, itemIndex) => {
    if (item.bomData && item.bomData.length > 0) {
      item.bomData.forEach(bomItem => {
        if (bomItem.batches && bomItem.batches.length > 0) {
          bomItem.batches.forEach(batch => {
            if (batch.shelfList && batch.shelfList.length > 0) {
              batch.shelfList.forEach(shelf => {
                // 收集所有货架，不管是否分配了数量
                allShelves.push({
                  itemIndex,
                  productId: item.productId,
                  componentProductId: bomItem.componentProductId,
                  batchNo: batch.batchNo,
                  shelfId: shelf.shelfId,
                  shelfName: shelf.shelfName || `货架${shelf.shelfId}`,
                  // 查找该货架是否有分配数量
                  allocatedQuantity: getAllocationQuantity(itemIndex, bomItem.componentProductId, batch.batchNo, shelf.shelfId)
                });
              });
            }
          });
        }
      });
    }
  });
  
  console.log(`收集到 ${allShelves.length} 个货架需要检查（包括已分配和未分配的）`);
  
  // 为每个货架添加日志
  allShelves.forEach(shelf => {
    console.log(`货架检查列表: 成品${shelf.productId} 原料${shelf.componentProductId} 批次${shelf.batchNo} 货架${shelf.shelfId} 分配数量:${shelf.allocatedQuantity}`);
  });
  
  return allShelves;
};

// ==================== 自动分配相关方法结束 ====================

// 构建检查请求数据// 构建检查请求数据 - 强制包含所有货架
const buildAllocationCheckRequest = (currentProductId = null, currentBatchNo = null, currentShelfId = null) => {
  const productAllocations = formData.items.map((item, index) => {
    const productInfo = productionProductList.value.find(p => p.id === item.productId);
    
    // 收集所有可能的分配记录（包括数量为0的）
    const allPossibleAllocations = [];
    
    if (item.bomData && item.bomData.length > 0) {
      item.bomData.forEach(bomItem => {
        if (bomItem.batches && bomItem.batches.length > 0) {
          bomItem.batches.forEach(batch => {
            if (batch.shelfList && batch.shelfList.length > 0) {
              batch.shelfList.forEach(shelf => {
                // 查找现有的分配数量
                const existingAllocation = item.bomAllocations?.find(a => 
                  a.componentProductId === bomItem.componentProductId &&
                  a.batchNo === batch.batchNo &&
                  a.shelfId === shelf.shelfId
                );
                
                // 确保每个货架都有一条记录（即使没有分配数据）
                const allocationData = {
                  componentProductId: bomItem.componentProductId,
                  componentProductName: bomItem.componentProductName,
                  batchNo: batch.batchNo,
                  shelfId: shelf.shelfId,
                  shelfName: shelf.shelfName || `货架${shelf.shelfId}`,
                  quantity: existingAllocation ? parseFloat(existingAllocation.quantity) || 0 : 0,
                  totalBomQuantity: calculateRequiredQuantity(bomItem.quantity, item.quantity)
                };
                
                allPossibleAllocations.push(allocationData);
                
                console.log(`构建检查请求 - 货架${shelf.shelfId}:`, {
                  成品: item.productId,
                  原料: bomItem.componentProductId,
                  批次: batch.batchNo,
                  货架: shelf.shelfId,
                  数量: allocationData.quantity
                });
              });
            }
          });
        }
      });
    }
    
    return {
      productId: item.productId,
      productName: productInfo?.name || item.productName,
      productQuantity: parseFloat(item.quantity) || 0,
      bomAllocations: allPossibleAllocations
    };
  });

  // 确保参数正确传递
  const requestData = {
    warehouseId: formData.warehouseId,
    tenantId: 1,
    productAllocations: productAllocations
  };
  
  // 只有在有具体参数时才添加这些字段
  if (currentProductId !== null) {
    requestData.currentProductId = currentProductId;
  }
  
  if (currentBatchNo !== null) {
    requestData.currentBatchNo = currentBatchNo;
  }
  
  if (currentShelfId !== null) {
    requestData.currentShelfId = currentShelfId;
  }
  
  console.log('构建的检查请求数据 - 货架总数:', 
    productAllocations.reduce((sum, p) => sum + (p.bomAllocations?.length || 0), 0));
  
  return requestData;
};

// 检查分配数量
const checkBatchAllocation = async (currentProductId = null, currentBatchNo = null, currentShelfId = null) => {
  if (!formData.warehouseId || formData.items.length === 0) {
    return { success: true };
  }

  if (loadingCheck.value) {
    // 如果已经在检查，等待完成
    await new Promise(resolve => setTimeout(resolve, 100));
    return { success: true };
  }

  loadingCheck.value = true;
  try {
    const requestData = buildAllocationCheckRequest(currentProductId, currentBatchNo, currentShelfId);
    
    // 确保传递了具体的参数
    console.log('发送检查请求 - 参数详情:', {
      currentProductId,
      currentBatchNo,
      currentShelfId,
      productAllocationsCount: requestData.productAllocations.length,
      productAllocations: requestData.productAllocations.map(p => ({
        productId: p.productId,
        productName: p.productName,
        bomAllocationsCount: p.bomAllocations?.length || 0
      }))
    });
    
    if (requestData.productAllocations.length === 0) {
      console.log('没有产品分配数据，跳过检查');
      return { success: true };
    }
    
    const res = await post('/api/auth/inventory/checkBatchAllocation', requestData);
    
    if (res) {
      if (res.batchAllocatedList) {
        updateLatestAllocationData(res.batchAllocatedList);
        syncLocalAllocationsWithApi(res.batchAllocatedList);
      }
      
      allocationCheckResult.value = {
        success: res.success,
        message: res.message || ''
      };
      
      dataLoaded.value = true;
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

// 同步本地分配数据与接口返回数据
const syncLocalAllocationsWithApi = (batchAllocatedList) => {
  batchAllocatedList.forEach(parentDto => {
    const productId = parentDto.productParentId;
    
    const itemIndex = formData.items.findIndex(item => item.productId == productId);
    if (itemIndex === -1) return;
    
    const item = formData.items[itemIndex];
    
    parentDto.productSonDtoList?.forEach(sonDto => {
      const componentProductId = sonDto.productSonId;
      
      sonDto.batchList?.forEach(batch => {
        const batchNo = batch.batchNo;
        
        batch.shelfList?.forEach(shelf => {
          const shelfId = shelf.shelfId;
          const allocatedQuantity = shelf.allocatedQuantity || 0;
          
          if (allocatedQuantity > 0) {
            if (!item.bomAllocations) {
              item.bomAllocations = [];
            }
            
            const allocationIndex = item.bomAllocations.findIndex(a => 
              a.componentProductId == componentProductId &&
              a.batchNo === batchNo &&
              a.shelfId == shelfId
            );
            
            if (allocationIndex >= 0) {
              item.bomAllocations[allocationIndex].quantity = allocatedQuantity;
              console.log(`同步本地数据: 产品${productId} 原料${componentProductId} 批次${batchNo} 货架${shelfId} 数量${allocatedQuantity}`);
            } else {
              const bomItem = item.bomData?.find(b => b.componentProductId == componentProductId);
              if (bomItem) {
                item.bomAllocations.push({
                  componentProductId: componentProductId,
                  componentProductName: bomItem.componentProductName,
                  componentProductSku: bomItem.componentProductSku,
                  batchNo: batchNo,
                  shelfId: shelfId,
                  shelfName: shelf.shelfName || `货架${shelfId}`,
                  quantity: allocatedQuantity
                });
                console.log(`创建本地数据: 产品${productId} 原料${componentProductId} 批次${batchNo} 货架${shelfId} 数量${allocatedQuantity}`);
              }
            }
          }
        });
      });
    });
  });
  
  console.log('本地分配数据已与接口数据同步');
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
            allocatedQuantity: shelf.allocatedQuantity || 0
          };
          
          console.log(`接口返回分配数据 key=${key}:`, {
            allocatedQuantity: shelf.allocatedQuantity,
            shelfAvailableQuantity: shelf.shelfAvailableQuantity
          });
        });
      });
    });
  });
  
  latestAllocationData.value = newData;
  console.log('更新最新的分配数据（共', Object.keys(newData).length, '条）');
};

// 获取实时可用信息
const getRealTimeAvailableInfo = (parentProductId, componentProductId, batchNo, shelfId = null) => {
  for (const [key, data] of Object.entries(latestAllocationData.value)) {
    const parts = key.split('_');
    const productParentId = parts[0];
    const productSonId = parts[1];
    const dataBatchNo = parts[2];
    const dataShelfId = parts[3];
    
    if (productParentId == parentProductId && 
        productSonId == componentProductId && 
        dataBatchNo === batchNo) {
      if (shelfId && dataShelfId == shelfId) {
        return data.shelfAvailableQuantity?.toFixed(4) || '0.0000';
      } else if (!shelfId) {
        return data.availableBatchQuantity?.toFixed(4) || '0.0000';
      }
    }
  }
  
  return '0.0000';
};

// 获取分配数量 - 优先使用接口返回的allocatedQuantity
const getAllocationQuantity = (itemIndex, componentProductId, batchNo, shelfId) => {
  const item = formData.items[itemIndex];
  
  if (!item) {
    return 0;
  }
  
  const parentProductId = item.productId;
  const key = `${parentProductId}_${componentProductId}_${batchNo}_${shelfId}`;
  
  const apiData = latestAllocationData.value[key];
  if (apiData && apiData.allocatedQuantity !== undefined) {
    const apiQuantity = parseFloat(apiData.allocatedQuantity);
    console.log(`getAllocationQuantity [${key}]: 使用接口数据 ${apiQuantity}`);
    return !isNaN(apiQuantity) ? apiQuantity : 0;
  }
  
  if (item.bomAllocations && item.bomAllocations.length > 0) {
    const allocation = item.bomAllocations.find(a => 
      a.componentProductId === componentProductId &&
      a.batchNo === batchNo &&
      a.shelfId === shelfId
    );
    
    if (allocation) {
      const localQuantity = parseFloat(allocation.quantity) || 0;
      console.log(`getAllocationQuantity [${key}]: 使用本地数据 ${localQuantity}`);
      return localQuantity;
    }
  }
  
  return 0;
};

// 更新分配数量 - 只更新本地数据，不影响接口返回的数据
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

  const allocationData = {
    componentProductId: bomRow.componentProductId,
    componentProductName: bomRow.componentProductName,
    componentProductSku: bomRow.componentProductSku,
    batchNo: batch.batchNo,
    shelfId: shelf.shelfId,
    shelfName: shelf.shelfName || '默认货架',
    quantity: quantity
  };

  if (allocationIndex >= 0) {
    item.bomAllocations[allocationIndex] = allocationData;
  } else {
    item.bomAllocations.push(allocationData);
  }
};

// 在编辑态时保护现有分配数据
const protectEditModeData = () => {
  if (isEditMode.value) {
    console.log('编辑模式，保护现有分配数据不被覆盖');
    
    formData.items.forEach((item, index) => {
      if (!item.bomAllocations) {
        item.bomAllocations = [];
      }
      
      if (item.bomData && item.bomData.length > 0) {
        item.bomData.forEach(bomItem => {
          if (bomItem.batches) {
            bomItem.batches.forEach(batch => {
              if (batch.shelfList) {
                batch.shelfList.forEach(shelf => {
                  const key = `${item.productId}_${bomItem.componentProductId}_${batch.batchNo}_${shelf.shelfId}`;
                  const apiData = latestAllocationData.value[key];
                  
                  if (apiData && apiData.allocatedQuantity > 0) {
                    const existingAllocation = item.bomAllocations.find(a => 
                      a.componentProductId === bomItem.componentProductId &&
                      a.batchNo === batch.batchNo &&
                      a.shelfId === shelf.shelfId
                    );
                    
                    if (!existingAllocation) {
                      item.bomAllocations.push({
                        componentProductId: bomItem.componentProductId,
                        componentProductName: bomItem.componentProductName,
                        componentProductSku: bomItem.componentProductSku,
                        batchNo: batch.batchNo,
                        shelfId: shelf.shelfId,
                        shelfName: shelf.shelfName || '默认货架',
                        quantity: apiData.allocatedQuantity
                      });
                    }
                  }
                });
              }
            });
          }
        });
      }
    });
  }
};

// 原料分配输入框失去焦点处理
const handleAllocationBlur = async (itemIndex, bomRow, batch, shelf, productId) => {
  console.log('原料分配输入框失去焦点，调用检查接口');
  
  const currentInputValue = getAllocationQuantity(itemIndex, bomRow.componentProductId, batch.batchNo, shelf.shelfId);
  console.log('当前输入框的值:', currentInputValue);
  
  const result = await checkBatchAllocation(productId, batch.batchNo, shelf.shelfId);
  
  if (result && result.success) {
    console.log('接口调用成功，等待数据更新');
    
    await new Promise(resolve => setTimeout(resolve, 100));
    
    const key = `${productId}_${bomRow.componentProductId}_${batch.batchNo}_${shelf.shelfId}`;
    const apiData = latestAllocationData.value[key];
    
    if (apiData && apiData.allocatedQuantity !== undefined) {
      const apiQuantity = parseFloat(apiData.allocatedQuantity) || 0;
      console.log(`接口返回的allocatedQuantity: ${apiQuantity} (key: ${key})`);
      
      const item = formData.items[itemIndex];
      const allocation = item.bomAllocations?.find(a => 
        a.componentProductId === bomRow.componentProductId &&
        a.batchNo === batch.batchNo &&
        a.shelfId === shelf.shelfId
      );
      
      if (allocation && Math.abs(parseFloat(allocation.quantity) - apiQuantity) > 0.0001) {
        console.log(`本地数据未同步，手动更新: ${allocation.quantity} -> ${apiQuantity}`);
        allocation.quantity = apiQuantity;
      }
      
      await nextTick();
    } else {
      console.log(`没有找到接口返回的数据 key=${key}`);
    }
  }
};

// 领料数量输入框失去焦点处理
const handleQuantityBlur = async (index) => {
  const item = formData.items[index];
  await checkBatchAllocation(item.productId, null, null);
};

// 加载出库单详情 - 关键修复
const loadOutboundDetail = async (id) => {
  loading.value = true;
  dataLoaded.value = false;
  try {
    const res = await get(`/api/auth/outbound/detailNew?orderId=${id}`);
    
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
        formData.items = [];
        
        await nextTick();
        
        for (const itemData of detailData.items) {
          const newItem = {
            productId: itemData.productId,
            productName: itemData.productName || '',
            sku: itemData.sku || '',
            spec: itemData.spec || '',
            color: itemData.color || '',
            unit: itemData.unit || '',
            quantity: parseFloat(itemData.quantity) || 1,
            bomData: [],
            bomAllocations: []
          };
          
          if (itemData.bomComponents && itemData.bomComponents.length > 0) {
            const allocationsByComponent = {};
            if (itemData.materialAllocations) {
              itemData.materialAllocations.forEach(allocation => {
                const key = `${allocation.componentProductId}_${allocation.batchNo}_${allocation.shelfId}`;
                allocationsByComponent[key] = allocation;
              });
            }
            
            newItem.bomData = itemData.bomComponents.map(component => {
              const batches = [];
              if (component.availableBatches) {
                component.availableBatches.forEach(batch => {
                  const batchWithAllocations = {
                    batchNo: batch.batchNo,
                    quantity: parseFloat(batch.totalQuantity) || 0,
                    shelfList: []
                  };
                  
                  if (batch.shelves) {
                    batch.shelves.forEach(shelf => {
                      const allocationKey = `${component.componentProductId}_${batch.batchNo}_${shelf.shelfId}`;
                      const allocation = allocationsByComponent[allocationKey];
                      
                      batchWithAllocations.shelfList.push({
                        shelfId: shelf.shelfId,
                        shelfName: shelf.shelfName || `货架${shelf.shelfId}`,
                        quantity: parseFloat(shelf.availableQuantity) || 0,
                        allocatedQuantity: allocation ? parseFloat(allocation.allocatedQuantity) : 0
                      });
                      
                      if (allocation) {
                        newItem.bomAllocations.push({
                          componentProductId: component.componentProductId,
                          componentProductName: component.componentProductName,
                          componentProductSku: component.componentProductSku,
                          batchNo: batch.batchNo,
                          shelfId: shelf.shelfId,
                          shelfName: shelf.shelfName || `货架${shelf.shelfId}`,
                          quantity: parseFloat(allocation.allocatedQuantity) || 0
                        });
                      }
                    });
                  }
                  
                  batches.push(batchWithAllocations);
                });
              }
              
              return {
                componentProductId: component.componentProductId,
                componentProductName: component.componentProductName,
                componentProductSku: component.componentProductSku,
                componentProductSpec: component.componentProductSpec,
                componentProductUnit: component.componentProductUnit,
                quantity: parseFloat(component.unitUsage) || 0,
                usageDetailList: component.usageDetailList || [],
                showUsageDetails: false,
                batches: batches
              };
            });
          }
          
          formData.items.push(newItem);
        }
        
        await checkBatchAllocation(null, null, null);
        
        protectEditModeData();
      }
      
      ElMessage.success('数据加载成功');
    }
  } catch (error) {
    console.error('加载出库单详情失败:', error);
    ElMessage.error('加载数据失败');
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
    dataLoaded.value = false;
    batchInfoCache.value = {};
  }
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
    bomAllocations: [],
    bomData: []
  });
};

const handleRemoveProduct = async (index) => {
  formData.items.splice(index, 1);
  await checkBatchAllocation(null, null, null);
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

const handleProductChange = async (productId, index) => {
  const product = productionProducts.value.find(p => p.id === productId);
  
  if (product) {
    const item = formData.items[index];
    item.productId = product.id;
    item.productName = product.name;
    item.sku = product.sku;
    item.color = product.color;
    item.spec = product.spec;
    item.unit = product.unit;
    item.quantity = 1;
    
    item.bomAllocations = [];
    
    await loadBatchInfoForProduction(productId, index);

    if (item.bomData) {
      item.bomData = item.bomData.map(bomItem => ({
        ...bomItem,
        showUsageDetails: false,
        usageDetailList: bomItem.usageDetailList || []
      }));
    }
    
    await buildInitialAllocations(item, index);
    
    setTimeout(async () => {
      await checkBatchAllocation(productId, null, null);
    }, 300);
  }
};

// 构建初始分配数据
const buildInitialAllocations = async (item, itemIndex) => {
  if (!item.bomData || item.bomData.length === 0) {
    item.bomAllocations = [];
    return;
  }
  
  const initialAllocations = [];
  
  for (const bomItem of item.bomData) {
    const batches = bomItem.batches || [];
    
    for (const batch of batches) {
      const shelfList = batch.shelfList || [];
      
      if (shelfList.length > 0) {
        for (const shelf of shelfList) {
          initialAllocations.push({
            componentProductId: bomItem.componentProductId,
            componentProductName: bomItem.componentProductName,
            componentProductSku: bomItem.componentProductSku,
            batchNo: batch.batchNo,
            shelfId: shelf.shelfId,
            shelfName: shelf.shelfName || `货架${shelf.shelfId}`,
            quantity: 0
          });
        }
      } else {
        initialAllocations.push({
          componentProductId: bomItem.componentProductId,
          componentProductName: bomItem.componentProductName,
          componentProductSku: bomItem.componentProductSku,
          batchNo: batch.batchNo,
          shelfId: null,
          shelfName: '默认货架',
          quantity: 0
        });
      }
    }
  }
  
  item.bomAllocations = initialAllocations;
  
  console.log(`构建了 ${initialAllocations.length} 个初始分配记录`, initialAllocations);
};

// 修改BOM数据处理方法，确保usageDetailList存在
const getBomData = (productId) => {
  const itemWithBomData = formData.items.find(item => item.productId === productId && item.bomData);
  if (itemWithBomData && itemWithBomData.bomData.length > 0) {
    return itemWithBomData.bomData.map(item => ({
      ...item,
      showUsageDetails: item.showUsageDetails || false
    }));
  }
  
  const product = productionProductList.value.find(p => p.id === productId);
  if (product?.bomData) {
    return product.bomData.map(item => ({
      ...item,
      showUsageDetails: item.showUsageDetails || false
    }));
  }
  
  return [];
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
        const cacheKey = `${bomItem.componentProductId}_${formData.warehouseId}`;
        
        let batches = batchInfoCache.value[cacheKey];
        if (!batches) {
          batches = await loadBatchInfoForComponent(bomItem.componentProductId);
          if (batches && batches.length > 0) {
            batchInfoCache.value[cacheKey] = batches;
          } else {
            batches = [];
          }
        }

        return {
          ...bomItem,
          componentProductId: bomItem.componentProductId,
          componentProductName: bomItem.componentProductName,
          componentProductSku: bomItem.componentProductSku,
          componentProductSpec: bomItem.componentProductSpec,
          componentProductUnit: bomItem.componentProductUnit,
          quantity: parseFloat(bomItem.quantity) || 0,
          usageDetailList: bomItem.usageDetailList || [],
          showUsageDetails: false,
          batches: batches || []
        };
      })
    );
    
    item.bomData = bomDataWithBatches;
    
    return bomDataWithBatches;
    
  } catch (error) {
    console.error('加载生产领料批次信息失败:', error);
    formData.items[index].bomData = [];
    return [];
  }
};

// 获取原料组件的批次数据（关联特定产品）
const getBatchDataForComponent = (componentProductId, parentProductId) => {
  for (const item of formData.items) {
    if (item.productId === parentProductId && item.bomData) {
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
    
    if (res && Array.isArray(res)) {
      return res.map(batch => ({
        batchNo: batch.batchNo,
        quantity: parseFloat(batch.quantity) || 0,
        createdAtOfBatch: batch.createdAt || new Date(),
        shelfList: (batch.shelfList || []).map(shelf => ({
          shelfId: shelf.shelfId,
          shelfName: shelf.shelfName || `区域${shelf.shelfId}`,
          shelivesName: shelf.shelivesName || `货架${shelf.shelivesId}`,
          quantity: parseFloat(shelf.quantity) || 0
        }))
      }));
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
  
  let totalFromApi = 0;
  for (const [key, data] of Object.entries(latestAllocationData.value)) {
    const parts = key.split('_');
    const productParentId = parts[0];
    const productSonId = parts[1];
    
    if (productParentId == item.productId && productSonId == componentProductId) {
      totalFromApi += parseFloat(data.allocatedQuantity) || 0;
    }
  }
  
  if (totalFromApi > 0) {
    return totalFromApi.toFixed(4);
  }
  
  if (!item.bomAllocations) return '0.0000';
  
  const totalFromLocal = item.bomAllocations
    .filter(a => a.componentProductId === componentProductId)
    .reduce((sum, a) => sum + (parseFloat(a.quantity) || 0), 0);
  
  return totalFromLocal.toFixed(4);
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
      formData.remark = ''; // 确保备注也被清空
      fileList.value = [];
      latestAllocationData.value = {};
      allocationCheckResult.value = { success: true, message: '' };
      dataLoaded.value = false;
      batchInfoCache.value = {};
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
    
    const url = isEditMode.value ? '/api/auth/outbound/updateProductionPickingOutBound' : '/api/auth/outbound/createProductionPickingOutBound';
    const res = await post(url, submitData);
    if (res) {
      ElMessage.success(isEditMode.value ? '更新草稿成功' : '保存草稿成功');
       router.replace({
        path: '/',
        query: { mode: 'outbound' }
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
  
  const validationResult = validateBomAllocations();
  if (!validationResult.valid) {
    ElMessage.warning(validationResult.message);
    return;
  }
  
  const checkResult = await checkBatchAllocation(null, null, null);
  if (!checkResult.success) {
    ElMessage.warning(`库存分配存在问题: ${checkResult.message}`);
    return;
  }
  
  console.log('提交前分配数据检查:');
  formData.items.forEach((item, index) => {
    console.log(`产品 ${item.productName || '未命名'} (index: ${index}):`);
    console.log('- 原始分配记录数:', item.bomAllocations?.length || 0);
    console.log('- 非零分配记录数:', item.bomAllocations?.filter(a => parseFloat(a.quantity) > 0)?.length || 0);
    console.log('- 分配明细:', item.bomAllocations?.map(a => ({
      原料: a.componentProductName,
      批次: a.batchNo,
      货架: a.shelfName,
      数量: a.quantity
    })) || []);
  });
  
  loading.value = true;
  try {
    const submitData = prepareSubmitData();
    submitData.status = 1;
    
    console.log('提交数据检查:');
    submitData.items.forEach((item, index) => {
      console.log(`提交的产品 ${item.productName} 分配记录数:`, item.bomAllocations?.length || 0);
    });
    
    const url = isEditMode.value ? '/api/auth/outbound/updateProductionPickingOutBound' : '/api/auth/outbound/createProductionPickingOutBound';
    const res = await post(url, submitData);
    if (res) {
      ElMessage.success(isEditMode.value ? '更新成功' : '提交成功，等待审核');
      router.replace({
        path: '/',
        query: { mode: 'outbound' }
      });
    }
  } catch (error) {
    ElMessage.error(isEditMode.value ? '更新失败' : '提交失败');
  } finally {
    loading.value = false;
  }
};

// 准备提交数据
// 准备提交数据 - 修复多个产品共享原料的问题
const prepareSubmitData = () => {
  const items = formData.items.map((item, itemIndex) => {
    const productInfo = productionProductList.value.find(p => p.id === item.productId);
    
    // 修复：直接从当前产品的实际分配数据中获取，避免从全局数据中获取导致的重复计算
    const bomAllocations = [];
    
    if (item.bomAllocations && item.bomAllocations.length > 0) {
      // 只处理当前产品的分配数据
      const currentProductAllocations = item.bomAllocations.filter(
        allocation => parseFloat(allocation.quantity) > 0
      );
      
      // 使用Set去重，避免重复记录
      const uniqueAllocations = new Map();
      
      currentProductAllocations.forEach(allocation => {
        // 创建唯一键：原料ID + 批次 + 货架
        const uniqueKey = `${allocation.componentProductId}_${allocation.batchNo}_${allocation.shelfId}`;
        
        // 检查是否已经存在相同键的分配
        if (uniqueAllocations.has(uniqueKey)) {
          console.warn(`重复的分配记录: 产品${item.productId} 原料${allocation.componentProductId} 批次${allocation.batchNo} 货架${allocation.shelfId}`);
        } else {
          uniqueAllocations.set(uniqueKey, {
            componentProductId: allocation.componentProductId,
            componentProductName: allocation.componentProductName,
            componentProductSku: allocation.componentProductSku,
            batchNo: allocation.batchNo,
            shelfId: allocation.shelfId,
            shelfName: allocation.shelfName || `货架${allocation.shelfId}`,
            quantity: parseFloat(allocation.quantity) || 0
          });
        }
      });
      
      // 将Map转换为数组
      bomAllocations.push(...Array.from(uniqueAllocations.values()));
    }
    
    return {
      productId: item.productId,
      productName: productInfo?.name || item.productName,
      sku: item.sku,
      spec: item.spec,
      unit: item.unit,
      quantity: parseFloat(item.quantity) || 0,
      price: 0,
      priceTotal: 0,
      priceUnitUsd: 0,
      priceTotalUsd: 0,
      remark: '',
      bomAllocations: bomAllocations
    };
  });
  
  console.log('提交数据详细检查:');
  items.forEach((item, index) => {
    console.log(`\n=== 产品 ${item.productName} (ID: ${item.productId}) ===`);
    if (item.bomAllocations && item.bomAllocations.length > 0) {
      item.bomAllocations.forEach(alloc => {
        console.log(`  原料 ${alloc.componentProductName} (${alloc.componentProductId})`);
        console.log(`    批次: ${alloc.batchNo}, 货架: ${alloc.shelfName}, 数量: ${alloc.quantity}`);
      });
    } else {
      console.log('  无分配记录');
    }
  });
  
  return {
    id: formData.id,
    orderNo: formData.orderNo,
    orderType: formData.orderType,
    warehouseId: formData.warehouseId,
    expectedDate: formData.expectedDate,
    relatedOrderNo: formData.relatedOrderNo || '',
    remark: formData.remark || '',
    status: formData.status,
    totalQuantity: totalQuantity.value,
    totalAmount: 0,
    totalAmountUsd: 0,
    attachments: formData.attachments.map(att => att.filePath),
    items: items,
    tenantId: 1
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
    const res = await get('/api/auth/product/listEnableNoPackaging');
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
    dataLoaded.value = true;
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
      dataLoaded.value = false;
      batchInfoCache.value = {};
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

.loading-section {
  margin: 20px 0;
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

/* 分配加载状态样式 */
.allocation-loading {
  margin: 20px;
  padding: 24px;
  background-color: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #ebeef5;
  text-align: center;
}

.loading-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.loading-icon {
  font-size: 36px;
  color: #409eff;
  animation: rotate 2s linear infinite;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.loading-text {
  font-size: 16px;
  font-weight: bold;
  color: #409eff;
}

.loading-progress {
  width: 100%;
  height: 8px;
  background-color: #e4e7ed;
  border-radius: 4px;
  overflow: hidden;
  margin: 8px 0;
}

.progress-bar {
  height: 100%;
  background: linear-gradient(90deg, #409eff, #79bbff);
  transition: width 0.3s ease;
  border-radius: 4px;
}

.progress-text {
  font-size: 14px;
  color: #606266;
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
  
  /* 移动端加载状态优化 */
  .allocation-loading {
    margin: 12px;
    padding: 16px;
  }
  
  .loading-icon {
    font-size: 28px;
  }
  
  .loading-text {
    font-size: 14px;
  }
  
  .progress-text {
    font-size: 12px;
  }
}

/* 产品表格样式优化 */
:deep(.product-table) {
  font-size: 14px;
}

:deep(.product-table .el-table__body) {
  font-size: 13px;
}

/* 优化表格行高，确保内容可见 */
:deep(.product-table .el-table__row) {
  height: 60px !important;
}

:deep(.product-table .el-table__row td) {
  padding: 8px 4px !important;
  vertical-align: middle;
}

/* 优化选择器样式 */
:deep(.product-table .el-select) {
  width: 100%;
}

:deep(.product-table .el-select .el-input__inner) {
  height: 36px !important;
  line-height: 36px !important;
  font-size: 13px;
  padding: 0 8px;
}

:deep(.product-table .el-select .el-input__suffix) {
  display: flex;
  align-items: center;
}

/* 优化数字输入框样式 */
:deep(.product-table .el-input-number) {
  width: 100%;
}

:deep(.product-table .el-input-number .el-input__inner) {
  height: 36px !important;
  line-height: 36px !important;
  text-align: center;
  font-size: 13px;
  padding: 0 8px;
}

:deep(.product-table .el-input-number .el-input-number__decrease),
:deep(.product-table .el-input-number .el-input-number__increase) {
  width: 24px;
  height: 18px;
  line-height: 18px;
}

/* 确保表格内容不换行，使用省略号 */
:deep(.product-table .el-table__cell) {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 产品选择器下拉菜单优化 */
:deep(.product-table .el-select-dropdown__item) {
  height: 36px !important;
  line-height: 36px !important;
  padding: 0 12px;
  font-size: 13px;
}

/* 优化表格固定列样式 */
:deep(.product-table .el-table__fixed-right) {
  height: 100% !important;
}

/* 响应式优化 */
@media (max-width: 1200px) {
  :deep(.product-table) {
    font-size: 13px;
  }
  
  :deep(.product-table .el-table__row) {
    height: 55px !important;
  }
  
  :deep(.product-table .el-select .el-input__inner) {
    height: 32px !important;
    line-height: 32px !important;
    font-size: 12px;
  }
  
  :deep(.product-table .el-input-number .el-input__inner) {
    height: 32px !important;
    line-height: 32px !important;
    font-size: 12px;
  }
}

/* 鼠标悬停时显示完整内容 */
:deep(.product-table .el-table__cell) {
  position: relative;
}

:deep(.product-table .el-table__cell:hover::after) {
  content: attr(title);
  position: absolute;
  left: 0;
  top: 100%;
  background: rgba(0, 0, 0, 0.8);
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  white-space: normal;
  z-index: 9999;
  max-width: 300px;
  word-break: break-all;
}

/* 确保操作按钮可见 */
:deep(.product-table .el-button--link) {
  padding: 4px;
  font-size: 12px;
}

:deep(.product-table .el-button--link .el-icon) {
  font-size: 14px;
}

.product-details {
  margin-top: 8px;
  padding: 4px;
  background-color: #f8f9fa;
  border-radius: 4px;
  font-size: 12px;
}

/* 添加以下样式到style部分 */
.component-product-name {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 4px;
}

.toggle-usage-btn {
  padding: 0;
  height: auto;
  margin-left: 8px;
}

.usage-details-container {
  margin-top: 8px;
  padding: 8px;
  background-color: #f8f9fa;
  border-radius: 4px;
  border: 1px solid #ebeef5;
}

.usage-details-title {
  font-size: 12px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 6px;
  padding-bottom: 4px;
  border-bottom: 1px dashed #dcdfe6;
}

.usage-detail-item {
  margin-bottom: 4px;
  padding: 4px;
  background-color: white;
  border-radius: 2px;
}

.usage-detail-content {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  font-size: 12px;
  color: #606266;
}

.usage-quantity {
  color: #e6a23c;
  font-weight: bold;
}

.usage-type {
  color: #409eff;
}

.usage-remark {
  color: #909399;
  flex: 1;
  min-width: 100px;
}

.usage-loss-rate {
  color: #f56c6c;
}

.sku-text {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

/* 自动分配按钮样式 */
.bom-section .section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

@media (max-width: 768px) {
  .bom-section .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .bom-section .header-right-actions {
    width: 100%;
    justify-content: space-between;
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

/* 备注提示样式 */
:deep(.el-form-item .el-form-item__content) {
  position: relative;
}

.remark-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 6px;
  padding: 6px 10px;
  background-color: #f8f9fa;
  border-radius: 4px;
  border-left: 3px solid #409eff;
  line-height: 1.4;
}

/* 验证失败时的提示 */
:deep(.el-form-item.is-error .remark-tip) {
  color: #f56c6c;
  background-color: #fef0f0;
  border-left-color: #f56c6c;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .remark-tip {
    font-size: 11px;
    padding: 5px 8px;
  }
}
</style>