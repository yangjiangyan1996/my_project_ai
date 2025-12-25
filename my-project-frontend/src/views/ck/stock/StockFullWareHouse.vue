<!--全仓库存管理 -->
<template>
  <div class="stock-take-full-container">
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

    <el-card class="manage-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">全库盘点</span>
          <div class="header-actions">
            <el-button 
              type="primary" 
              @click="handleSave"
              :loading="saving"
              v-if="!isViewMode"
            >
              <el-icon><Document /></el-icon>
              保存草稿
            </el-button>
            <el-button 
              type="success" 
              @click="handleSubmit"
              :loading="submitting"
              v-if="!isViewMode && formData.id"
            >
              <el-icon><CircleCheck /></el-icon>
              提交审批
            </el-button>
            <el-button 
              type="warning" 
              @click="handleBack"
            >
              <el-icon><Back /></el-icon>
              返回列表
            </el-button>
          </div>
        </div>
      </template>

      <!-- 基本信息 -->
      <el-card class="form-section" shadow="never">
        <template #header>
          <div class="section-header">
            <span class="section-title">基本信息</span>
          </div>
        </template>
        <el-form 
          ref="baseFormRef" 
          :model="formData" 
          :rules="formRules" 
          label-width="120px"
          :disabled="isViewMode"
        >
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="盘点单号" prop="orderNo">
                <el-input 
                  v-model="formData.orderNo" 
                  placeholder="系统自动生成"
                  :disabled="true"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="盘点名称" prop="takeName">
                <el-input 
                  v-model="formData.takeName" 
                  placeholder="请输入盘点任务名称"
                  maxlength="100"
                  show-word-limit
                />
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="仓库" prop="warehouseId">
                <el-select
                  v-model="formData.warehouseId"
                  placeholder="请选择仓库"
                  style="width: 100%"
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
            <el-col :span="12">
              <el-form-item label="盘点方式" prop="takeType">
                <el-radio-group v-model="formData.takeType">
                  <el-radio :label="1">动态盘点（业务照常）</el-radio>
                  <el-radio :label="2">静态盘点（停止出入库）</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="允许差异率" prop="toleranceRate">
                <el-input-number
                  v-model="formData.toleranceRate"
                  :min="0"
                  :max="100"
                  :step="0.1"
                  :precision="2"
                  style="width: 100%"
                  placeholder="请输入允许差异率"
                >
                  <template #append>%</template>
                </el-input-number>
                <div class="form-tip">超过此差异率将触发复核</div>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="计划盘点时间" prop="planTimeRange">
                <el-date-picker
                  v-model="formData.planTimeRange"
                  type="datetimerange"
                  range-separator="至"
                  start-placeholder="开始时间"
                  end-placeholder="结束时间"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="24">
            <el-col :span="24">
              <el-form-item label="备注" prop="remark">
                <el-input
                  v-model="formData.remark"
                  type="textarea"
                  :rows="3"
                  placeholder="请输入盘点备注信息"
                  maxlength="500"
                  show-word-limit
                />
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </el-card>

      <!-- 盘点范围设置 -->
      <el-card class="form-section" shadow="never">
        <template #header>
          <div class="section-header">
            <span class="section-title">盘点范围设置</span>
            <div class="section-actions">
              <el-button 
                type="primary" 
                size="small"
                @click="handleSelectAll"
                v-if="!isViewMode"
              >
                全选
              </el-button>
              <el-button 
                type="info" 
                size="small"
                @click="handleClearAll"
                v-if="!isViewMode"
              >
                清空
              </el-button>
            </div>
          </div>
        </template>
        
        <div class="range-filter">
          <el-form :model="rangeFilter" inline>
            <el-form-item label="产品分类">
              <el-cascader
                v-model="rangeFilter.categoryCode"
                :options="categoryTree"
                :props="{ value: 'categoryCode', label: 'categoryName', children: 'children' }"
                placeholder="请选择分类"
                clearable
                style="width: 200px"
                @change="filterProductsByCategory"
              />
            </el-form-item>
            <el-form-item label="产品名称">
              <el-input
                v-model="rangeFilter.productName"
                placeholder="输入产品名称"
                clearable
                style="width: 180px"
                @keyup.enter="filterProducts"
              >
                <template #append>
                  <el-button @click="filterProducts">
                    <el-icon><Search /></el-icon>
                  </el-button>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item label="SKU">
              <el-input
                v-model="rangeFilter.sku"
                placeholder="输入SKU"
                clearable
                style="width: 180px"
                @keyup.enter="filterProducts"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="filterProducts">筛选</el-button>
              <el-button @click="resetFilter">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
        
        <div class="range-table">
          <el-table
            :data="filteredProducts"
            v-loading="productLoading"
            empty-text="暂无产品数据"
            class="product-table"
            row-key="id"
            @selection-change="handleSelectionChange"
          >
            <el-table-column 
              type="selection" 
              width="55" 
              align="center"
              v-if="!isViewMode"
            />
            <el-table-column type="index" label="序号" width="60" align="center" />
            <el-table-column label="产品信息" min-width="250">
              <template #default="{ row }">
                <div class="product-info">
                  <div class="product-name">{{ row.name }}</div>
                  <div class="product-sku">SKU: {{ row.sku }}</div>
                  <div class="product-category">分类: {{ row.categoryName || '未分类' }}</div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="规格型号" width="120" prop="spec" />
            <el-table-column label="单位" width="80" align="center" prop="unitCode" />
            <el-table-column label="当前库存" width="120" align="center">
              <template #default="{ row }">
                <span>{{ row.currentStock || 0 }}</span>
              </template>
            </el-table-column>
            <el-table-column label="库存价值" width="120" align="right">
              <template #default="{ row }">
                <span>¥{{ ((row.currentStock || 0) * (row.unitPrice || 0)).toFixed(2) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="批次管理" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="row.batchManage ? 'success' : 'info'" size="small">
                  {{ row.batchManage ? '需要' : '不需要' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" align="center" v-if="!isViewMode">
              <template #default="{ row }">
                <el-button
                  type="primary"
                  link
                  size="small"
                  @click="viewProductDetail(row)"
                >
                  详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <div class="selected-info">
            <el-tag type="info">
              已选择 {{ selectedProducts.length }} 个产品
            </el-tag>
            <div class="selected-summary">
              <span>预估盘点项数: {{ estimatedItems }} 项</span>
              <span>预估库存价值: ¥{{ estimatedValue.toFixed(2) }}</span>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 盘点策略设置 -->
      <el-card class="form-section" shadow="never">
        <template #header>
          <div class="section-header">
            <span class="section-title">盘点策略设置</span>
          </div>
        </template>
        
        <el-form 
          :model="strategyConfig" 
          label-width="120px"
          :disabled="isViewMode"
        >
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="盘点模式">
                <el-radio-group v-model="strategyConfig.countMode">
                  <el-radio :label="1">盲盘（不显示系统库存）</el-radio>
                  <el-radio :label="2">明盘（显示系统库存）</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="盘点顺序">
                <el-select 
                  v-model="strategyConfig.countOrder" 
                  placeholder="请选择盘点顺序"
                  style="width: 100%"
                >
                  <el-option :label="按货架顺序" :value="1" />
                  <el-option :label="按产品分类" :value="2" />
                  <el-option :label="按库存价值-高到低" :value="3" />
                  <el-option :label="随机顺序" :value="4" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="是否需要复盘">
                <el-switch
                  v-model="strategyConfig.needSecondCount"
                  active-text="需要"
                  inactive-text="不需要"
                />
                <div class="form-tip">开启后，差异超过允许范围将自动触发复盘</div>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="复盘阈值" v-if="strategyConfig.needSecondCount">
                <el-input-number
                  v-model="strategyConfig.secondCountThreshold"
                  :min="0.1"
                  :max="100"
                  :step="0.5"
                  :precision="2"
                  style="width: 100%"
                  placeholder="请输入复盘阈值"
                >
                  <template #append>%</template>
                </el-input-number>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="盘点人员配置">
                <el-select
                  v-model="strategyConfig.countPersonConfig"
                  placeholder="请选择人员配置"
                  style="width: 100%"
                >
                  <el-option :label="单人盘点" :value="1" />
                  <el-option :label="双人盘点-初盘+复盘" :value="2" />
                  <el-option :label="多人分区盘点" :value="3" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="盘点设备">
                <el-checkbox-group v-model="strategyConfig.devices">
                  <el-checkbox :label="1">PDA</el-checkbox>
                  <el-checkbox :label="2">RFID扫描枪</el-checkbox>
                  <el-checkbox :label="3">平板电脑</el-checkbox>
                  <el-checkbox :label="4">纸质盘点单</el-checkbox>
                </el-checkbox-group>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="自动生成调整单">
                <el-switch
                  v-model="strategyConfig.autoGenerateAdjust"
                  active-text="自动生成"
                  inactive-text="手动生成"
                />
                <div class="form-tip">开启后，盘点完成后自动生成库存调整单</div>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="调整单审批流程">
                <el-select
                  v-model="strategyConfig.adjustApprovalFlow"
                  placeholder="请选择审批流程"
                  style="width: 100%"
                  :disabled="!strategyConfig.autoGenerateAdjust"
                >
                  <el-option :label="无需审批" :value="0" />
                  <el-option :label="仓库主管审批" :value="1" />
                  <el-option :label="财务审批" :value="2" />
                  <el-option :label="主管+财务双审" :value="3" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </el-card>

      <!-- 库存快照 -->
      <el-card class="form-section" shadow="never" v-if="showStockSnapshot">
        <template #header>
          <div class="section-header">
            <span class="section-title">库存快照</span>
            <div class="section-actions">
              <el-button 
                type="primary" 
                size="small"
                @click="refreshStockSnapshot"
                :loading="snapshotLoading"
              >
                <el-icon><Refresh /></el-icon>
                刷新快照
              </el-button>
            </div>
          </div>
        </template>
        
        <div class="snapshot-info">
          <el-descriptions :column="4" border>
            <el-descriptions-item label="快照时间">{{ snapshotTime }}</el-descriptions-item>
            <el-descriptions-item label="产品数量">{{ snapshotSummary.productCount }} 个</el-descriptions-item>
            <el-descriptions-item label="总库存数量">{{ snapshotSummary.totalQuantity }}</el-descriptions-item>
            <el-descriptions-item label="总库存价值">¥{{ snapshotSummary.totalValue.toFixed(2) }}</el-descriptions-item>
            <el-descriptions-item label="批次商品数">{{ snapshotSummary.batchProductCount }} 个</el-descriptions-item>
            <el-descriptions-item label="货位商品数">{{ snapshotSummary.shelfProductCount }} 个</el-descriptions-item>
            <el-descriptions-item label="最低库存商品" :span="2">
              <span v-if="snapshotSummary.lowStockProducts.length > 0">
                {{ snapshotSummary.lowStockProducts.length }} 个
                <el-popover
                  placement="bottom"
                  title="低库存商品列表"
                  :width="300"
                  trigger="hover"
                >
                  <template #reference>
                    <el-button type="text" size="small">查看详情</el-button>
                  </template>
                  <div class="low-stock-list">
                    <div 
                      v-for="product in snapshotSummary.lowStockProducts" 
                      :key="product.id"
                      class="low-stock-item"
                    >
                      <span>{{ product.name }}</span>
                      <span class="stock-info">库存: {{ product.currentStock }}/{{ product.minStock }}</span>
                    </div>
                  </div>
                </el-popover>
              </span>
              <span v-else>无</span>
            </el-descriptions-item>
          </el-descriptions>
        </div>
        
        <div class="snapshot-tips">
          <el-alert
            title="快照说明"
            type="info"
            :closable="false"
          >
            <ul class="tip-list">
              <li>库存快照记录盘点开始时的库存状态，用于与盘点结果对比</li>
              <li>建议在盘点开始前刷新快照，确保数据准确性</li>
              <li>快照数据仅供参考，实际盘点以现场盘点结果为准</li>
            </ul>
          </el-alert>
        </div>
      </el-card>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Document, CircleCheck, Back, Search, Refresh } from '@element-plus/icons-vue';
import { post, get } from '@/net';

const route = useRoute();
const router = useRouter();
const baseFormRef = ref(null);

const saving = ref(false);
const submitting = ref(false);
const productLoading = ref(false);
const snapshotLoading = ref(false);
const showStockSnapshot = ref(false);

// 判断是否为查看模式
const isViewMode = computed(() => route.name === 'CkStockTakeFullView' || route.params.view === 'true');

// 表单数据
const formData = reactive({
  id: null,
  orderNo: '',
  takeName: '',
  takeType: 1, // 1-动态盘点, 2-静态盘点
  takeStrategy: 1, // 1-全库盘点
  warehouseId: null,
  toleranceRate: 0.5, // 默认0.5%
  planTimeRange: [],
  remark: '',
  
  // 额外字段
  planStartTime: '',
  planEndTime: '',
  status: 0,
  approvalStatus: 0,
  
  // 盘点范围
  productIds: [],
  categoryIds: [],
  includeAllProducts: false
});

// 表单验证规则
const formRules = {
  takeName: [
    { required: true, message: '请输入盘点名称', trigger: 'blur' },
    { max: 100, message: '盘点名称不能超过100个字符', trigger: 'blur' }
  ],
  warehouseId: [
    { required: true, message: '请选择仓库', trigger: 'change' }
  ],
  takeType: [
    { required: true, message: '请选择盘点方式', trigger: 'change' }
  ],
  toleranceRate: [
    { required: true, message: '请输入允许差异率', trigger: 'blur' },
    { type: 'number', min: 0, max: 100, message: '差异率必须在0-100之间', trigger: 'blur' }
  ],
  planTimeRange: [
    { required: true, message: '请选择计划盘点时间', trigger: 'change' }
  ]
};

// 盘点策略配置
const strategyConfig = reactive({
  countMode: 1, // 1-盲盘, 2-明盘
  countOrder: 1, // 1-按货架顺序, 2-按产品分类, 3-按库存价值, 4-随机顺序
  needSecondCount: true,
  secondCountThreshold: 5, // 默认5%
  countPersonConfig: 1, // 1-单人盘点, 2-双人盘点, 3-多人分区
  devices: [1, 4], // 默认PDA和纸质盘点单
  autoGenerateAdjust: true,
  adjustApprovalFlow: 1 // 默认仓库主管审批
});

// 范围筛选
const rangeFilter = reactive({
  categoryCode: null,
  productName: '',
  sku: ''
});

// 数据列表
const warehouseList = ref([]);
const productList = ref([]);
const filteredProducts = ref([]);
const selectedProducts = ref([]);
const categoryTree = ref([]);

// 库存快照
const snapshotTime = ref('');
const snapshotSummary = reactive({
  productCount: 0,
  totalQuantity: 0,
  totalValue: 0,
  batchProductCount: 0,
  shelfProductCount: 0,
  lowStockProducts: []
});

// 计算属性
const estimatedItems = computed(() => {
  return selectedProducts.value.length;
});

const estimatedValue = computed(() => {
  return selectedProducts.value.reduce((sum, product) => {
    const stock = product.currentStock || 0;
    const price = product.unitPrice || 0;
    return sum + (stock * price);
  }, 0);
});

// 方法
const loadWarehouseList = async () => {
  try {
    const res = await get('/api/auth/warehouse/list');
    warehouseList.value = res || [];
  } catch (error) {
    console.error('加载仓库列表失败:', error);
    warehouseList.value = [];
  }
};

const loadProductList = async () => {
  if (!formData.warehouseId) {
    ElMessage.warning('请先选择仓库');
    return;
  }
  
  productLoading.value = true;
  try {
    const params = {
      warehouseId: formData.warehouseId,
      status: 1, // 只加载启用的产品
      pageSize: 1000 // 加载所有产品
    };
    
    const res = await post('/api/auth/product/warehouse-stock-list', params);
    if (res && res.records) {
      productList.value = res.records.map(product => ({
        id: product.id,
        sku: product.sku,
        name: product.name,
        spec: product.spec,
        unitCode: product.unitCode,
        categoryCode: product.categoryCode,
        categoryName: product.categoryName,
        currentStock: product.quantity || 0,
        unitPrice: product.unitPrice || 0,
        minStock: product.minStock || 0,
        batchManage: product.batchManage || false,
        shelfManage: product.shelfManage || false,
        // 用于筛选
        searchText: `${product.sku}${product.name}${product.categoryName || ''}`.toLowerCase()
      }));
      
      filteredProducts.value = [...productList.value];
    } else {
      productList.value = [];
      filteredProducts.value = [];
    }
  } catch (error) {
    console.error('加载产品列表失败:', error);
    productList.value = [];
    filteredProducts.value = [];
    ElMessage.error('加载产品列表失败');
  } finally {
    productLoading.value = false;
  }
};

const loadCategoryTree = async () => {
  try {
    const res = await get('/api/auth/product-category/tree');
    categoryTree.value = res || [];
  } catch (error) {
    console.error('加载分类树失败:', error);
    categoryTree.value = [];
  }
};

const handleGoBack = () => {
  const hasUnsavedChanges = false;
  
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
      router.back();
    }).catch(() => {
    });
  } else {
    router.back();
  }
};

const loadStockTakeDetail = async (id) => {
  try {
    const res = await get(`/api/auth/stock-take/full-detail?id=${id}`);
    if (res) {
      // 填充表单数据
      Object.assign(formData, {
        id: res.id,
        orderNo: res.orderNo,
        takeName: res.takeName,
        takeType: res.takeType,
        warehouseId: res.warehouseId,
        toleranceRate: res.toleranceRate,
        planTimeRange: res.planStartTime && res.planEndTime ? 
          [res.planStartTime, res.planEndTime] : [],
        remark: res.remark,
        status: res.status,
        approvalStatus: res.approvalStatus
      });
      
      // 填充策略配置
      if (res.strategyConfig) {
        Object.assign(strategyConfig, JSON.parse(res.strategyConfig));
      }
      
      // 填充产品选择
      if (res.productIds && res.productIds.length > 0) {
        setTimeout(() => {
          const selectedIds = res.productIds;
          selectedProducts.value = productList.value.filter(p => selectedIds.includes(p.id));
        }, 500);
      }
      
      // 显示库存快照
      showStockSnapshot.value = true;
      if (res.snapshotTime) {
        snapshotTime.value = res.snapshotTime;
      }
      if (res.snapshotSummary) {
        Object.assign(snapshotSummary, res.snapshotSummary);
      }
      
      // 加载仓库产品
      loadProductList();
    }
  } catch (error) {
    console.error('加载盘点单详情失败:', error);
    ElMessage.error('加载数据失败');
  }
};

const handleWarehouseChange = () => {
  // 清空产品选择
  selectedProducts.value = [];
  // 加载产品列表
  loadProductList();
  // 隐藏库存快照
  showStockSnapshot.value = false;
};

const filterProducts = () => {
  if (!productList.value.length) return;
  
  let result = productList.value;
  
  // 按分类筛选
  if (rangeFilter.categoryCode && rangeFilter.categoryCode.length > 0) {
    const lastCategoryCode = rangeFilter.categoryCode[rangeFilter.categoryCode.length - 1];
    result = result.filter(product => product.categoryCode === lastCategoryCode);
  }
  
  // 按产品名称筛选
  if (rangeFilter.productName) {
    const keyword = rangeFilter.productName.toLowerCase();
    result = result.filter(product => 
      product.name.toLowerCase().includes(keyword) || 
      product.sku.toLowerCase().includes(keyword)
    );
  }
  
  // 按SKU筛选
  if (rangeFilter.sku) {
    const skuKeyword = rangeFilter.sku.toLowerCase();
    result = result.filter(product => product.sku.toLowerCase().includes(skuKeyword));
  }
  
  filteredProducts.value = result;
};

const filterProductsByCategory = () => {
  filterProducts();
};

const resetFilter = () => {
  rangeFilter.categoryCode = null;
  rangeFilter.productName = '';
  rangeFilter.sku = '';
  filteredProducts.value = [...productList.value];
};

const handleSelectionChange = (selection) => {
  selectedProducts.value = selection;
};

const handleSelectAll = () => {
  if (!filteredProducts.value.length) {
    ElMessage.warning('没有可选择的商品');
    return;
  }
  
  // 获取当前页所有商品的ID
  const currentPageIds = filteredProducts.value.map(p => p.id);
  const currentSelectedIds = selectedProducts.value.map(p => p.id);
  
  // 添加当前页所有未选择的商品
  const newSelections = [...selectedProducts.value];
  filteredProducts.value.forEach(product => {
    if (!currentSelectedIds.includes(product.id)) {
      newSelections.push(product);
    }
  });
  
  selectedProducts.value = newSelections;
  ElMessage.success(`已选择当前页所有商品`);
};

const handleClearAll = () => {
  selectedProducts.value = [];
  ElMessage.info('已清空所有选择');
};

const viewProductDetail = (product) => {
  // 打开产品详情弹窗
  console.log('查看产品详情:', product);
};

const refreshStockSnapshot = async () => {
  if (!formData.warehouseId) {
    ElMessage.warning('请先选择仓库');
    return;
  }
  
  snapshotLoading.value = true;
  try {
    const res = await post('/api/auth/stock-take/generate-snapshot', {
      warehouseId: formData.warehouseId,
      productIds: selectedProducts.value.map(p => p.id)
    });
    
    if (res) {
      snapshotTime.value = res.snapshotTime || new Date().toLocaleString();
      snapshotSummary.productCount = res.productCount || 0;
      snapshotSummary.totalQuantity = res.totalQuantity || 0;
      snapshotSummary.totalValue = res.totalValue || 0;
      snapshotSummary.batchProductCount = res.batchProductCount || 0;
      snapshotSummary.shelfProductCount = res.shelfProductCount || 0;
      snapshotSummary.lowStockProducts = res.lowStockProducts || [];
      
      ElMessage.success('库存快照已更新');
      showStockSnapshot.value = true;
    }
  } catch (error) {
    console.error('刷新库存快照失败:', error);
    ElMessage.error('刷新快照失败');
  } finally {
    snapshotLoading.value = false;
  }
};

const validateForm = async () => {
  if (!baseFormRef.value) return false;
  
  try {
    await baseFormRef.value.validate();
    
    // 检查是否有选择产品
    if (selectedProducts.value.length === 0) {
      ElMessage.warning('请至少选择一个盘点产品');
      return false;
    }
    
    // 检查时间范围
    if (formData.planTimeRange && formData.planTimeRange.length === 2) {
      const startTime = new Date(formData.planTimeRange[0]);
      const endTime = new Date(formData.planTimeRange[1]);
      
      if (startTime >= endTime) {
        ElMessage.warning('结束时间必须晚于开始时间');
        return false;
      }
    }
    
    return true;
  } catch (error) {
    console.error('表单验证失败:', error);
    return false;
  }
};

const handleSave = async () => {
  const isValid = await validateForm();
  if (!isValid) return;
  
  saving.value = true;
  try {
    // 处理时间范围
    const planStartTime = formData.planTimeRange && formData.planTimeRange[0] 
      ? formData.planTimeRange[0] 
      : null;
    const planEndTime = formData.planTimeRange && formData.planTimeRange[1] 
      ? formData.planTimeRange[1] 
      : null;
    
    const saveData = {
      ...formData,
      planStartTime,
      planEndTime,
      productIds: selectedProducts.value.map(p => p.id),
      strategyConfig: JSON.stringify(strategyConfig),
      takeStrategy: 1, // 全库盘点
      status: 0, // 草稿状态
      approvalStatus: 0 // 未提交审批
    };
    
    // 删除不需要的字段
    delete saveData.planTimeRange;
    
    const res = await post('/api/auth/stock-take/full/save', saveData);
    
    if (res) {
      formData.id = res.id;
      formData.orderNo = res.orderNo || formData.orderNo;
      ElMessage.success('保存成功');
      
      // 如果是新建保存，更新URL中的ID
      if (route.params.id && route.params.id !== res.id) {
        router.replace(`/index/ckStockTakeFullEdit/${res.id}`);
      }
    }
  } catch (error) {
    console.error('保存失败:', error);
    ElMessage.error('保存失败: ' + (error.message || '未知错误'));
  } finally {
    saving.value = false;
  }
};

const handleSubmit = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要提交盘点单"${formData.takeName}"审批吗？提交后不可修改。`,
      '提交审批确认',
      { 
        type: 'warning',
        confirmButtonText: '确定提交',
        cancelButtonText: '取消'
      }
    );
    
    const isValid = await validateForm();
    if (!isValid) return;
    
    submitting.value = true;
    
    const submitData = {
      id: formData.id,
      status: 1, // 提交后状态变为审批中
      approvalStatus: 1 // 审核中
    };
    
    const res = await post('/api/auth/stock-take/full/submit', submitData);
    
    if (res) {
      ElMessage.success('提交成功，已进入审批流程');
      router.push('/index/ckStockTakeManage');
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('提交失败:', error);
      ElMessage.error('提交失败');
    }
  } finally {
    submitting.value = false;
  }
};

const handleBack = () => {
  router.push('/index/ckStockTakeManage');
};

// 生成盘点单号
const generateOrderNo = () => {
  const prefix = 'PD';
  const timestamp = new Date().getTime().toString().slice(-6);
  const random = Math.floor(Math.random() * 1000).toString().padStart(3, '0');
  formData.orderNo = `${prefix}${timestamp}${random}`;
};

// 初始化页面
const initPage = async () => {
  await loadWarehouseList();
  await loadCategoryTree();
  
  // 检查是否是编辑或查看模式
  const id = route.params.id;
  if (id && id !== 'create') {
    await loadStockTakeDetail(id);
  } else {
    // 新建模式，生成单号
    generateOrderNo();
  }
};

// 监听仓库变化
watch(() => formData.warehouseId, (newVal) => {
  if (newVal) {
    // 重置产品选择
    selectedProducts.value = [];
  }
});

onMounted(() => {
  initPage();
});
</script>

<style scoped>
.stock-take-full-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.manage-card {
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

.form-section {
  margin-bottom: 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.section-actions {
  display: flex;
  gap: 8px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.range-filter {
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 16px;
}

.range-table {
  margin-top: 16px;
}

.product-table {
  width: 100%;
  margin-bottom: 16px;
}

.product-info {
  line-height: 1.4;
}

.product-name {
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.product-sku {
  font-size: 12px;
  color: #909399;
  margin-bottom: 2px;
}

.product-category {
  font-size: 12px;
  color: #909399;
}

.selected-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background-color: #f8f9fa;
  border-radius: 4px;
  border: 1px solid #ebeef5;
}

.selected-summary {
  display: flex;
  gap: 24px;
}

.selected-summary span {
  font-size: 14px;
  color: #606266;
}

.snapshot-info {
  margin-bottom: 16px;
}

.low-stock-list {
  max-height: 200px;
  overflow-y: auto;
}

.low-stock-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.low-stock-item:last-child {
  border-bottom: none;
}

.stock-info {
  font-size: 12px;
  color: #f56c6c;
  font-weight: bold;
}

.snapshot-tips {
  margin-top: 16px;
}

.tip-list {
  margin: 0;
  padding-left: 20px;
  font-size: 14px;
  line-height: 1.6;
  color: #606266;
}

.tip-list li {
  margin-bottom: 4px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .stock-take-full-container {
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
    align-items: flex-start;
    gap: 8px;
  }
  
  .section-actions {
    width: 100%;
    justify-content: flex-end;
  }
  
  .selected-info {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
  
  .selected-summary {
    flex-direction: column;
    gap: 8px;
  }
}

.back-btn {
  padding: 10px 16px;
  font-size: 16px;
  font-weight: 500;
  color: #409EFF;
}

.back-btn:hover {
  background-color: #ecf5ff;
  border-radius: 4px;
}

.back-btn i {
  margin-right: 6px;
  font-size: 18px;
}

</style>