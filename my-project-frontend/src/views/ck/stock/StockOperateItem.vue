<template>
  <div class="stock-take-manage-container">
    <el-card class="manage-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">盘点数据录入</span>
          <div class="header-actions">
            <el-button 
              @click="refreshData"
              :loading="loading"
            >
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
            <el-button 
              type="primary" 
              @click="handleCompleteStock"
              :loading="completing"
            >
              <el-icon><Check /></el-icon>
              完成盘点
            </el-button>
          </div>
        </div>
      </template>

      <!-- 盘点单信息 -->
      <!-- 修改模板中的盘点单信息部分 -->
        <div class="stock-take-info-section">
        <el-card shadow="never" class="info-card">
            <div class="info-grid">
            <div class="info-item">
                <span class="info-label">盘点单号：</span>
                <span class="info-value">{{ stockTakeInfo.stockTakeNo }}</span>
            </div>
            <div class="info-item">
                <span class="info-label">仓库：</span>
                <span class="info-value">{{ stockTakeInfo.warehouseName }}</span>
            </div>
            <div class="info-item">
                <span class="info-label">盘点类型：</span>
                <!-- 直接显示名称，不需要再映射 -->
                <el-tag type="primary" size="small">
                {{ stockTakeInfo.takeTypeName }}
                </el-tag>
            </div>
            <div class="info-item">
                <span class="info-label">盘点状态：</span>
                <!-- 使用新的状态名称字段 -->
                <el-tag 
                :type="getStatusTagTypeByName(stockTakeInfo.takeStatusName)" 
                size="small"
                >
                {{ stockTakeInfo.takeStatusName }}
                </el-tag>
            </div>
            <div class="info-item">
                <span class="info-label">审核状态：</span>
                <!-- 新增审核状态显示 -->
                <el-tag 
                :type="getApprovalStatusTagType(stockTakeInfo.approvalStatusName)" 
                size="small"
                >
                {{ stockTakeInfo.approvalStatusName }}
                </el-tag>
            </div>
            <div class="info-item full-width">
                <span class="info-label">备注：</span>
                <span class="info-value">{{ stockTakeInfo.remark || '无' }}</span>
            </div>
            </div>
        </el-card>
        </div>

      <!-- 统计信息 -->
      <div class="stats-section">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-card shadow="never" class="stat-card">
              <div class="stat-content">
                <div class="stat-number">{{ totalItems }}</div>
                <div class="stat-label">总盘点项</div>
              </div>
              <el-icon class="stat-icon" color="#409EFF"><Document /></el-icon>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="never" class="stat-card">
              <div class="stat-content">
                <div class="stat-number">{{ countedItems }}</div>
                <div class="stat-label">已盘点</div>
              </div>
              <el-icon class="stat-icon" color="#67C23A"><Check /></el-icon>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="never" class="stat-card">
              <div class="stat-content">
                <div class="stat-number">{{ pendingItems }}</div>
                <div class="stat-label">未盘点</div>
              </div>
              <el-icon class="stat-icon" color="#E6A23C"><Clock /></el-icon>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="never" class="stat-card">
              <div class="stat-content">
                <div class="stat-number" :class="getDiffClass(totalDiff)">
                  {{ formatNumber(totalDiff) }}
                </div>
                <div class="stat-label">总差异数量</div>
              </div>
              <el-icon class="stat-icon" color="#909399"><TrendCharts /></el-icon>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 盘点明细表格 -->
      <div class="stock-take-list-section">
        <!-- 搜索和筛选 -->
        <div class="filter-section">
          <el-form :model="filterForm" inline>
            <el-form-item label="SKU">
              <el-input
                v-model="filterForm.sku"
                placeholder="请输入SKU"
                clearable
                style="width: 180px"
                @keyup.enter="handleSearch"
              />
            </el-form-item>
            <el-form-item label="产品名称">
              <el-input
                v-model="filterForm.productName"
                placeholder="请输入产品名称"
                clearable
                style="width: 180px"
                @keyup.enter="handleSearch"
              />
            </el-form-item>
            <el-form-item label="批次号">
              <el-input
                v-model="filterForm.batchNo"
                placeholder="请输入批次号"
                clearable
                style="width: 180px"
                @keyup.enter="handleSearch"
              />
            </el-form-item>
            <el-form-item label="盘点状态">
              <el-select
                v-model="filterForm.status"
                placeholder="全部状态"
                clearable
                style="width: 120px"
              >
                <el-option label="未盘" :value="1" />
                <el-option label="已盘" :value="2" />
                <el-option label="已确认" :value="3" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch">查询</el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <el-table
          :data="stockTakeItems"
          v-loading="loading"
          empty-text="暂无盘点明细数据"
          class="stock-take-table"
          row-key="id"
          border
        >
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column label="SKU" width="140">
            <template #default="{ row }">
              <span class="sku-text">{{ row.productSku }}</span>
            </template>
          </el-table-column>
          <el-table-column label="产品名称" width="200">
            <template #default="{ row }">
              <div class="product-info">
                <div class="product-name">{{ row.productName }}</div>
                <div v-if="row.spec" class="product-spec">{{ row.spec }}</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="批次号" width="120" align="center">
            <template #default="{ row }">
              <span>{{ row.batchNo || '--' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="货架/库位" width="150" align="center">
            <template #default="{ row }">
              <div v-if="row.shelfCode || row.locationCode">
                <div>{{ row.shelfCode || '--' }}</div>
                <div class="location-code">{{ row.locationCode || '--' }}</div>
              </div>
              <span v-else>--</span>
            </template>
          </el-table-column>
          <el-table-column label="系统库存" width="120" align="right">
            <template #default="{ row }">
              <span class="system-quantity">{{ formatNumber(row.systemQuantity) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="实盘数量" width="150" align="center">
            <template #default="{ row }">
              <el-input-number
                v-model="row.countedQuantity"
                :min="0"
                :precision="row.precision || 0"
                :controls="false"
                size="small"
                style="width: 120px"
                placeholder="请输入"
                @change="handleQuantityChange(row)"
                :disabled="row.status === 3"
              />
            </template>
          </el-table-column>
          <el-table-column label="差异数量" width="120" align="right">
            <template #default="{ row }">
              <span :class="getDiffClass(row.diffQuantity)">
                {{ formatNumber(row.diffQuantity) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag
                :type="getItemStatusTagType(row.status)"
                size="small"
              >
                {{ getItemStatusLabel(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" align="center" fixed="right">
            <template #default="{ row }">
              <div class="action-buttons">
                <el-button
                  type="primary"
                  size="small"
                  @click="handleSaveItem(row)"
                  :loading="row.saving"
                  :disabled="!row.countedQuantity || row.countedQuantity < 0 || row.status === 3"
                >
                  保存
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-section">
          <el-pagination
            v-model:current-page="pagination.current"
            v-model:page-size="pagination.size"
            :total="pagination.total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </el-card>

    <!-- 批量操作区域 -->
    <div v-if="selectedItems.length > 0" class="batch-actions-footer">
      <el-card shadow="never" class="batch-actions-card">
        <div class="batch-actions-content">
          <div class="selected-info">
            已选择 <span class="selected-count">{{ selectedItems.length }}</span> 项
          </div>
          <div class="batch-buttons">
            <el-button type="primary" size="small" @click="handleBatchSave">
              批量保存
            </el-button>
            <el-button type="success" size="small" @click="handleBatchConfirm">
              批量确认
            </el-button>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { useRouter, useRoute } from 'vue-router';
import { ref, reactive, computed, onMounted, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { 
  Refresh, 
  Check, 
  Document, 
  Clock,
  TrendCharts
} from '@element-plus/icons-vue';
import { post, get } from '@/net';

const router = useRouter();
const route = useRoute();

// 加载状态
const loading = ref(false);
const completing = ref(false);

// 分页信息
const pagination = reactive({
  current: 1,
  size: 20,
  total: 0
});

// 筛选表单
const filterForm = reactive({
  sku: '',
  productName: '',
  batchNo: '',
  status: ''
});

// 盘点单信息
const stockTakeInfo = reactive({
  id: '',
  stockTakeNo: '',
  warehouseName: '',
  takeType: 1,
  status: 1,
  remark: ''
});

// 盘点明细列表
const stockTakeItems = ref([]);
// 选中项
const selectedItems = ref([]);

// 计算属性
const totalItems = computed(() => {
  return pagination.total;
});

const countedItems = computed(() => {
  return stockTakeItems.value.filter(item => item.status === 2 || item.status === 3).length;
});

const pendingItems = computed(() => {
  return stockTakeItems.value.filter(item => item.status === 1).length;
});

const totalDiff = computed(() => {
  return stockTakeItems.value.reduce((sum, item) => sum + (item.diffQuantity || 0), 0);
});

const canComplete = computed(() => {
  // 可以根据业务需求调整完成条件
  return stockTakeInfo.status === 3 && pendingItems.value === 0;
});

// 从URL获取盘点单ID
const getStockTakeIdFromUrl = () => {
  var id = route.params.id;
  console.log('路由参数:', id);
  return id;
};

// 保存盘点单ID到URL和本地存储
const saveStockTakeId = (id) => {
  const url = new URL(window.location);
  url.searchParams.set('id', id);
  window.history.replaceState({}, '', url);
  localStorage.setItem('currentStockTakeId', id);
};

// 加载盘点单信息
// 修改后的 loadStockTakeInfo 方法
const loadStockTakeInfo = async () => {
  const stockTakeId = getStockTakeIdFromUrl();
  if (!stockTakeId) {
    ElMessage.error('未找到盘点单ID');
    return;
  }

  try {
    const res = await get('/api/auth/stock/stockDetail?id=' + route.params.id);
    if (res) {
      // 使用新的数据结构
      Object.assign(stockTakeInfo, {
        id: res.id || '',
        stockTakeNo: res.stockTakeNo || '',
        warehouseName: res.warehouseName || '',
        takeTypeName: res.takeTypeName || '', // 改为使用名称
        approvalStatusName: res.approvalStatusName || '',
        takeStatusName: res.takeStatusName || '', // 使用新的盘点状态名称
        remark: res.remark || ''
      });
      
      // 根据新的状态名称映射到原来的状态值
      // 如果你的业务逻辑需要数字状态值，可以在这里进行映射
      // 例如：
      // stockTakeInfo.status = mapStatusToValue(res.takeStatusName);
    }
  } catch (error) {
    console.error('加载盘点单信息失败:', error);
    ElMessage.error('加载盘点单信息失败');
  }
};

// 加载盘点明细
const loadStockTakeItems = async () => {
  const stockTakeId = getStockTakeIdFromUrl();
  if (!stockTakeId) {
    ElMessage.error('未找到盘点单ID');
    return;
  }

  loading.value = true;
  try {
    const params = {
      stockTakeId,
      page: pagination.current,
      size: pagination.size,
      ...filterForm
    };

    const res = await post('/api/auth/stock/itemPageList', params);
    if (res && res.records) {
      // 为每个项添加状态标记
      stockTakeItems.value = res.records.map(item => ({
        stockTakeItemId: item.stockTakeItemId || '',
        stockTakeId: stockTakeId || '',
        productId: item.productId || '',
        productSku: item.productSku || '',
        productName: item.productName || '',
        spec: item.spec || '',
        batchNo: item.batchNo || '',
        shelfCode: item.shelfCode || '',
        locationCode: item.locationCode || '',
        systemQuantity: item.systemQuantity || 0,
        countedQuantity: item.countedQuantity !== undefined ? item.countedQuantity : null,
        diffQuantity: item.diffQuantity || 0,
        status: item.status || 1,
        precision: item.precision || 0,
        saving: false,
        confirming: false
      }));
      pagination.total = res.total || 0;
    } else {
      stockTakeItems.value = [];
      pagination.total = 0;
    }
  } catch (error) {
    console.error('加载盘点明细失败:', error);
    ElMessage.error('加载盘点明细失败');
    stockTakeItems.value = [];
  } finally {
    loading.value = false;
  }
};

// 刷新数据
const refreshData = () => {
  loadStockTakeInfo();
  loadStockTakeItems();
};

// 搜索
const handleSearch = () => {
  pagination.current = 1;
  loadStockTakeItems();
};

// 重置搜索
const handleReset = () => {
  Object.assign(filterForm, {
    sku: '',
    productName: '',
    batchNo: '',
    status: ''
  });
  pagination.current = 1;
  loadStockTakeItems();
};

// 分页大小变化
const handleSizeChange = (size) => {
  pagination.size = size;
  pagination.current = 1;
  loadStockTakeItems();
};

// 页码变化
const handleCurrentChange = (page) => {
  pagination.current = page;
  loadStockTakeItems();
};

// 数量变化处理
const handleQuantityChange = (row) => {
  if (row.countedQuantity !== null && row.countedQuantity !== undefined) {
    row.diffQuantity = row.countedQuantity - row.systemQuantity;
  } else {
    row.diffQuantity = -row.systemQuantity;
  }
};

// 保存单条数据
const handleSaveItem = async (row) => {
  if (row.countedQuantity === null || row.countedQuantity === undefined) {
    ElMessage.warning('请输入实盘数量');
    return;
  }

  if (row.countedQuantity < 0) {
    ElMessage.warning('实盘数量不能为负数');
    return;
  }

  row.saving = true;
  try {
    const res = await post('/api/auth/stock/executeStockTakeItem', {
      stockTakeItemId: row.stockTakeItemId,
      stockTakeId: row.stockTakeId,
      productId: row.productId,
      countedQuantity: row.countedQuantity,
    });
    
    if (res) {
      ElMessage.success('保存成功');
      row.status = 2;
      // 重新加载当前页数据
      await loadStockTakeItems();
    }
  } catch (error) {
    console.error('保存失败:', error);
    ElMessage.error('保存失败');
  } finally {
    row.saving = false;
  }
};

// 确认单条数据
const handleConfirmItem = async (row) => {
  try {
    await ElMessageBox.confirm(
      '确认已盘点此项？确认后将不可修改。',
      '确认盘点',
      { type: 'warning' }
    );
    
    row.confirming = true;
    const res = await post('/api/auth/stock/confirmItem', {
      id: row.id,
      status: 3  // 确认后状态改为"已确认"
    });
    
    if (res) {
      ElMessage.success('确认成功');
      row.status = 3;
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('确认失败');
    }
  } finally {
    row.confirming = false;
  }
};

// 批量保存
const handleBatchSave = async () => {
  // 获取所有已修改但未保存的项
  const itemsToSave = stockTakeItems.value.filter(item => 
    item.countedQuantity !== null && 
    item.countedQuantity !== undefined && 
    item.status === 1
  );

  if (itemsToSave.length === 0) {
    ElMessage.warning('没有需要保存的项');
    return;
  }

  try {
    await ElMessageBox.confirm(
      `确定要批量保存 ${itemsToSave.length} 项数据吗？`,
      '批量保存确认',
      { type: 'warning' }
    );

    const res = await post('/api/auth/stock/batchSaveStockItem', {
      items: itemsToSave.map(item => ({
        id: item.id,
        countedQuantity: item.countedQuantity,
        diffQuantity: item.diffQuantity,
        status: 2
      }))
    });

    if (res) {
      ElMessage.success(`成功保存 ${itemsToSave.length} 项数据`);
      await loadStockTakeItems();
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量保存失败');
    }
  }
};

// 批量确认
const handleBatchConfirm = async () => {
  // 获取所有已保存但未确认的项
  const itemsToConfirm = stockTakeItems.value.filter(item => 
    item.status === 2
  );

  if (itemsToConfirm.length === 0) {
    ElMessage.warning('没有需要确认的项');
    return;
  }

  try {
    await ElMessageBox.confirm(
      `确定要批量确认 ${itemsToConfirm.length} 项数据吗？确认后将不可修改。`,
      '批量确认确认',
      { type: 'warning' }
    );

    const res = await post('/api/auth/stock/batchConfirmItem', {
      items: itemsToConfirm.map(item => ({
        id: item.id,
        status: 3
      }))
    });

    if (res) {
      ElMessage.success(`成功确认 ${itemsToConfirm.length} 项数据`);
      await loadStockTakeItems();
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量确认失败');
    }
  }
};

// 完成盘点
const handleCompleteStock = async () => {
  const stockTakeId = getStockTakeIdFromUrl();
  if (!stockTakeId) {
    ElMessage.error('未找到盘点单ID');
    return;
  }

  try {
    await ElMessageBox.confirm(
      '确定要完成盘点吗？完成盘点后将提交复核。',
      '完成盘点确认',
      { type: 'warning' }
    );
    completing.value = true;
    const res = await get('/api/auth/stock/completedStock?id=' + route.params.id);

    if (res) {
      ElMessage.success('完成盘点成功');
      // 更新盘点单状态
      stockTakeInfo.status = 4; // 已完成状态
      // 重新加载数据
      await loadStockTakeItems();
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('完成盘点失败:', error);
      ElMessage.error('完成盘点失败');
    }
  } finally {
    completing.value = false;
  }
};


// 根据状态名称获取标签类型
const getStatusTagTypeByName = (statusName) => {
  const mapping = {
    '新建': 'info',
    '盘点中': 'warning',
    '待确认': 'primary',
    '已完成': 'success',
    '已取消': 'danger'
  };
  return mapping[statusName] || 'info';
};

// 根据审核状态名称获取标签类型
const getApprovalStatusTagType = (approvalStatus) => {
  const mapping = {
    '待审批': 'warning',
    '已批准': 'success',
    '已拒绝': 'danger',
    '审批中': 'primary'
  };
  return mapping[approvalStatus] || 'info';
};

// 状态标签类型
const getStatusTagType = (status) => {
  const mapping = {
    1: 'info',
    2: 'warning',
    3: 'primary',
    4: 'success',
    5: 'danger'
  };
  return mapping[status] || 'info';
};

// 状态标签文本
const getStatusLabel = (status) => {
  const mapping = {
    1: '新建',
    2: '盘点中',
    3: '待确认',
    4: '已完成',
    5: '已取消'
  };
  return mapping[status] || '未知';
};

// 盘点项状态标签类型
const getItemStatusTagType = (status) => {
  const mapping = {
    1: 'info',
    2: 'warning',
    3: 'success'
  };
  return mapping[status] || 'info';
};

// 盘点项状态标签文本
const getItemStatusLabel = (status) => {
  const mapping = {
    1: '未盘',
    2: '已盘',
    3: '已确认'
  };
  return mapping[status] || '未知';
};

// 差异数量样式
const getDiffClass = (diff) => {
  if (diff > 0) return 'diff-positive';
  if (diff < 0) return 'diff-negative';
  return 'diff-zero';
};

// 数字格式化
const formatNumber = (num) => {
  if (num === null || num === undefined) return '0';
  const number = Number(num);
  if (isNaN(number)) return '0';
  return number.toLocaleString('zh-CN', {
    minimumFractionDigits: 0,
    maximumFractionDigits: 4
  });
};

// 监听选中的项
watch(stockTakeItems, (newItems) => {
  selectedItems.value = newItems.filter(item => item.checked);
}, { deep: true });

// 初始化加载
onMounted(() => {
  // 获取盘点单ID
  const stockTakeId = getStockTakeIdFromUrl();
  if (stockTakeId) {
    loadStockTakeInfo();
    loadStockTakeItems();
  } else {
    ElMessage.warning('请从盘点列表页面进入数据录入');
  }
});
</script>

<style scoped>
.stock-take-manage-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.manage-card {
  border-radius: 8px;
  min-height: calc(100vh - 100px);
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

/* 盘点单信息区域 */
.stock-take-info-section {
  margin-bottom: 20px;
}

.info-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  padding: 20px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 6px;
  border-left: 4px solid rgba(255, 255, 255, 0.3);
}

.info-item.full-width {
  grid-column: 1 / -1;
}

.info-label {
  font-size: 14px;
  font-weight: 500;
  opacity: 0.9;
}

.info-value {
  font-size: 14px;
  font-weight: bold;
}

/* 统计区域 */
.stats-section {
  margin-bottom: 20px;
}

.stat-card {
  position: relative;
  border-radius: 8px;
  border: none;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  flex-direction: column;
}

.stat-number {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
}

.stat-number.diff-positive {
  color: #67C23A;
}

.stat-number.diff-negative {
  color: #F56C6C;
}

.stat-number.diff-zero {
  color: #909399;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.stat-icon {
  position: absolute;
  top: 20px;
  right: 20px;
  font-size: 48px;
  opacity: 0.3;
}

/* 筛选区域 */
.filter-section {
  padding: 20px 0;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 20px;
}

/* 表格区域 */
.stock-take-list-section {
  margin-top: 20px;
}

.stock-take-table {
  width: 100%;
  margin-bottom: 20px;
}

.sku-text {
  font-family: 'Courier New', monospace;
  font-weight: bold;
  color: #409EFF;
}

.product-info {
  display: flex;
  flex-direction: column;
}

.product-name {
  font-size: 14px;
  color: #303133;
  margin-bottom: 4px;
}

.product-spec {
  font-size: 12px;
  color: #909399;
}

.location-code {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.system-quantity {
  font-weight: bold;
  color: #303133;
}

.diff-positive {
  color: #67C23A;
  font-weight: bold;
}

.diff-negative {
  color: #F56C6C;
  font-weight: bold;
}

.diff-zero {
  color: #909399;
  font-weight: bold;
}

.action-buttons {
  display: flex;
  gap: 8px;
  justify-content: center;
}

/* 分页区域 */
.pagination-section {
  display: flex;
  justify-content: flex-end;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

/* 批量操作底部区域 */
.batch-actions-footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20px;
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 -2px 12px 0 rgba(0, 0, 0, 0.1);
  z-index: 1000;
}

.batch-actions-card {
  border: none;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.batch-actions-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
}

.selected-info {
  font-size: 14px;
  color: #303133;
}

.selected-count {
  font-weight: bold;
  color: #409EFF;
  font-size: 16px;
  margin: 0 4px;
}

.batch-buttons {
  display: flex;
  gap: 12px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .info-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .stock-take-manage-container {
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
  
  .info-grid {
    grid-template-columns: 1fr;
  }
  
  .stat-card {
    margin-bottom: 16px;
  }
  
  .filter-section .el-form-item {
    margin-bottom: 12px;
  }
  
  .action-buttons {
    flex-direction: column;
    gap: 4px;
  }
  
  .batch-actions-footer {
    padding: 10px;
  }
  
  .batch-actions-content {
    flex-direction: column;
    gap: 12px;
    text-align: center;
  }
}

/* 动画效果 */
.stock-take-table :deep(.el-table__row) {
  transition: all 0.3s;
}

.stock-take-table :deep(.el-table__row:hover) {
  background-color: #f5f7fa;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.stat-card {
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.manage-card {
  animation: fadeIn 0.3s ease;
}
</style>