<template>
  <div class="adjust-order-manage-container">
    <el-card class="manage-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">库存调整单管理</span>
          <div class="header-actions">
            <el-button 
              type="primary" 
              @click="handleCreateNew"
              :disabled="!selectedWarehouseId"
            >
              <el-icon><Plus /></el-icon>
              新建调整单
            </el-button>
            <el-button 
              @click="refreshList"
              :loading="loading"
            >
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </div>
      </template>

      <!-- 仓库选择 -->
      <div class="warehouse-select-section">
        <el-form :model="warehouseForm" inline>
          <el-form-item label="选择仓库" required>
            <el-select
              v-model="warehouseForm.warehouseId"
              placeholder="请选择仓库"
              clearable
              @change="handleWarehouseSelect"
              style="width: 250px"
            >
              <el-option
                v-for="warehouse in warehouseList"
                :key="warehouse.id"
                :label="warehouse.name"
                :value="warehouse.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item v-if="selectedWarehouseName">
            <el-tag type="primary" size="large">
              <el-icon><OfficeBuilding /></el-icon>
              {{ selectedWarehouseName }}
            </el-tag>
          </el-form-item>
        </el-form>
      </div>

      <!-- 筛选条件 -->
      <div class="filter-section" v-if="selectedWarehouseId">
        <el-form :model="filterForm" inline>
          <el-form-item label="调整单号">
            <el-input
              v-model="filterForm.adjustNo"
              placeholder="请输入调整单号"
              clearable
              style="width: 180px"
            />
          </el-form-item>
          <el-form-item label="调整类型">
            <el-select
              v-model="filterForm.adjustType"
              placeholder="全部类型"
              clearable
              style="width: 120px"
            >
              <el-option
                v-for="type in adjustTypeOptions"
                :key="type.value"
                :label="type.label"
                :value="type.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="来源类型">
            <el-select
              v-model="filterForm.sourceType"
              placeholder="全部来源"
              clearable
              style="width: 120px"
            >
              <el-option
                v-for="source in sourceTypeOptions"
                :key="source.value"
                :label="source.label"
                :value="source.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="调整状态">
            <el-select
              v-model="filterForm.adjustStatus"
              placeholder="全部状态"
              clearable
              style="width: 120px"
            >
              <el-option
                v-for="status in adjustStatusOptions"
                :key="status.value"
                :label="status.label"
                :value="status.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="创建时间">
            <el-date-picker
              v-model="filterForm.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              style="width: 240px"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 调整单列表 -->
      <div class="adjust-order-list-section" v-if="selectedWarehouseId">
        <el-table
          :data="adjustOrderList"
          v-loading="loading"
          empty-text="请先选择仓库"
          class="adjust-order-table"
          row-key="id"
        >
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column label="调整单号" width="180" fixed="left">
            <template #default="{ row }">
              <span class="adjust-order-no">{{ row.adjustNo }}</span>
            </template>
          </el-table-column>
          <el-table-column label="调整类型" width="100" align="center">
            <template #default="{ row }">
              <el-tag 
                :type="getAdjustTypeTagType(row.adjustType)" 
                size="small"
              >
                {{ getAdjustTypeLabel(row.adjustType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="来源" width="120">
            <template #default="{ row }">
              <div v-if="row.sourceType === 1" class="source-info">
                <el-icon><Document /></el-icon>
                <span>盘点单：{{ row.sourceNo }}</span>
              </div>
              <div v-else-if="row.sourceType === 2" class="source-info">
                <el-icon><Edit /></el-icon>
                <span>手动创建</span>
              </div>
              <div v-else class="source-info">
                <el-icon><SetUp /></el-icon>
                <span>{{ getSourceTypeLabel(row.sourceType) }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="调整商品" width="100" align="center">
            <template #default="{ row }">
              <span class="detail-info">
                {{ row.totalItems || 0 }} 个
              </span>
            </template>
          </el-table-column>
          <el-table-column label="调整总量" width="120" align="center">
            <template #default="{ row }">
              <span :class="getQuantityClass(row.totalQuantity)">
                {{ formatNumber(row.totalQuantity) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="调整金额" width="120" align="right">
            <template #default="{ row }">
              <span :class="getAmountClass(row.totalAmount)">
                ¥{{ formatCurrency(row.totalAmount) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="调整状态" width="120" align="center">
            <template #default="{ row }">
              <el-tag 
                :type="getStatusTagType(row.adjustStatus)" 
                size="small"
              >
                {{ getStatusLabel(row.adjustStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="创建人" width="120">
            <template #default="{ row }">
              <span>{{ row.createdByName }}</span>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" width="160">
            <template #default="{ row }">
              <span>{{ formatDateTime(row.createdAt) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="300" fixed="right" align="center">
            <template #default="{ row }">
              <div class="action-buttons">
                <!-- 查看详情 -->
                <el-button
                  type="primary"
                  link
                  size="small"
                  @click="handleView(row)"
                >
                  查看
                </el-button>

                <!-- 待提交状态：可编辑 -->
                <template v-if="row.adjustStatus === 1">
                  <el-button
                    type="warning"
                    link
                    size="small"
                    @click="handleEdit(row)"
                  >
                    编辑
                  </el-button>
                  <el-button
                    type="success"
                    link
                    size="small"
                    @click="handleSubmit(row)"
                  >
                    提交审核
                  </el-button>
                  <el-button
                    type="danger"
                    link
                    size="small"
                    @click="handleDelete(row)"
                  >
                    删除
                  </el-button>
                </template>

                <!-- 待审核状态：可审核 -->
              <template v-if="row.adjustStatus === 1">
                <!-- 新增审核按钮 -->
                <el-button
                  type="primary"
                  link
                  size="small"
                  @click="openAuditDialog(row)"
                >
                  审核
                </el-button>
              </template>

              <!-- 审核通过状态：可执行 -->
              <template v-if="row.adjustStatus === 3">
                <el-button
                  type="primary"
                  link
                  size="small"
                  @click="handleExecute(row)"
                >
                  执行调整
                </el-button>
              </template>

                <!-- 审核通过状态：可执行 -->
                <template v-if="row.adjustStatus === 2">
                  <el-button
                    type="primary"
                    link
                    size="small"
                    @click="handleExecute(row)"
                  >
                    执行调整
                  </el-button>
                </template>

                <!-- 已执行状态：可查看执行结果 -->
                <template v-if="row.adjustStatus === 20 || row.adjustStatus === 29">
                  <el-button
                    type="info"
                    link
                    size="small"
                    @click="handleViewResult(row)"
                  >
                    执行结果
                  </el-button>
                </template>

                <!-- 已执行状态：可查看执行结果 -->
              <template v-if="row.adjustStatus === 5">
                <el-button
                  type="info"
                  link
                  size="small"
                  @click="handleViewResult(row)"
                >
                  执行结果
                </el-button>
              </template>


              </div>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-section" v-if="adjustOrderList.length > 0">
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

      <!-- 无仓库选择提示 -->
      <div class="no-warehouse-tip" v-else>
        <el-empty description="请先选择仓库以查看调整单">
          <template #image>
            <el-icon size="60"><OfficeBuilding /></el-icon>
          </template>
        </el-empty>
      </div>
    </el-card>
  </div>

  <!-- 审核对话框 -->
    <el-dialog
      v-model="auditDialogVisible"
      :title="`审核调整单 - ${currentAuditOrder?.adjustNo || ''}`"
      width="500px"
      :close-on-click-modal="false"
    >
      <div class="audit-dialog-content">
        <el-form 
          :model="auditForm" 
          ref="auditFormRef"
          label-width="100px"
          size="medium"
        >
          <el-form-item label="审核状态" required prop="approveStatus">
            <el-radio-group v-model="auditForm.approveStatus">
              <el-radio :label="2">审核通过</el-radio>
              <el-radio :label="4">审核拒绝</el-radio>
            </el-radio-group>
          </el-form-item>
          
          <el-form-item 
            label="审核备注" 
            required 
            prop="approveRemark"
            v-if="auditForm.approveStatus === 4"
          >
            <el-input
              v-model="auditForm.approveRemark"
              type="textarea"
              :rows="4"
              placeholder="请输入拒绝原因"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
          
          <el-form-item 
            label="审核备注" 
            prop="approveRemark"
            v-else
          >
            <el-input
              v-model="auditForm.approveRemark"
              type="textarea"
              :rows="4"
              placeholder="请输入审核备注（可选）"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
        </el-form>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="auditDialogVisible = false">取消</el-button>
          <el-button 
            type="primary" 
            @click="handleConfirmAudit"
            :loading="auditing"
          >
            确认审核
          </el-button>
        </div>
      </template>
    </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { 
  Plus, 
  Refresh, 
  Document,
  Edit,
  SetUp,
  OfficeBuilding
} from '@element-plus/icons-vue';
import { post, get } from '@/net';

const router = useRouter();
const loading = ref(false);
const auditing = ref(false); // 审核加载状态
const auditDialogVisible = ref(false); // 审核对话框显示状态
const auditFormRef = ref(); // 审核表单引用

// 审核表单
const auditForm = reactive({
  approveStatus: 2, // 2=审核通过, 4=审核拒绝
  approveRemark: ''
});

// 当前正在审核的调整单
const currentAuditOrder = ref(null);

// 仓库选择表单
const warehouseForm = reactive({
  warehouseId: ''
});

// 筛选表单
const filterForm = reactive({
  adjustNo: '',
  adjustType: '',
  sourceType: '',
  adjustStatus: '',
  dateRange: []
});

// 主分页信息
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
});

// 当前查看的调整单
const currentAdjustOrder = reactive({
  id: '',
  adjustNo: '',
  warehouseId: '',
  warehouseName: '',
  adjustType: 1,
  sourceType: 1,
  sourceId: '',
  sourceNo: '',
  adjustReason: '',
  adjustStatus: 1,
  approvalStatus: 0,
  totalItems: 0,
  totalQuantity: 0,
  totalAmount: 0,
  increaseQuantity: 0,
  decreaseQuantity: 0,
  isUrgent: false,
  isAffectCost: false,
  remark: '',
  createdByName: '',
  createdAt: '',
  modifiedAt: '',
  approverName: '',
  approveTime: '',
  executeByName: '',
  actualExecuteTime: ''
});

// 仓库列表
const warehouseList = ref([]);
// 调整单列表
const adjustOrderList = ref([]);

// 选项数据
const adjustTypeOptions = [
  { value: 1, label: '盘点调整' },
  { value: 2, label: '报损调整' },
  { value: 3, label: '报溢调整' },
  { value: 4, label: '成本调整' },
  { value: 5, label: '库存转移' },
  { value: 6, label: '其他调整' }
];

const sourceTypeOptions = [
  { value: 1, label: '盘点单' },
  { value: 2, label: '手动创建' },
  { value: 3, label: '异常处理' },
  { value: 4, label: '系统自动' }
];

const adjustStatusOptions = [
  { value: 1, label: '待提交' },
  { value: 2, label: '待审核' },
  { value: 3, label: '审核通过' },
  { value: 4, label: '审核拒绝' },
  { value: 5, label: '已执行' },
  { value: 6, label: '已取消' }
];

// 计算属性
const selectedWarehouseId = computed(() => warehouseForm.warehouseId);
const selectedWarehouseName = computed(() => {
  const warehouse = warehouseList.value.find(w => w.id === warehouseForm.warehouseId);
  return warehouse ? warehouse.name : '';
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


// 确认审核
const handleConfirmAudit = async () => {
  if (!currentAuditOrder.value) {
    ElMessage.error('未找到审核的调整单');
    return;
  }
  
  // 验证表单
  if (auditForm.approveStatus === 4 && !auditForm.approveRemark.trim()) {
    ElMessage.warning('审核拒绝时必须填写备注');
    return;
  }
  
  auditing.value = true;
  try {
    // 构造请求参数
    const params = {
      id: currentAuditOrder.value.id,
      approveStatus: auditForm.approveStatus,
      approveRemark: auditForm.approveRemark || '',
      // 注意：这里需要获取当前登录用户的userId和tenantId
      // 假设从localStorage或用户信息中获取
      
      
    };
    
    const res = await post('/api/auth/adjust/approveOk', params);
    if (res === true) {
      ElMessage.success(auditForm.approveStatus === 2 ? '审核通过成功' : '审核拒绝成功');
      auditDialogVisible.value = false;
      refreshList();
    }
    //  else {
    //   ElMessage.error('审核操作失败');
    // }
  } catch (error) {
    console.error('审核操作失败:', error);
    // ElMessage.error('审核操作失败');
  } finally {
    auditing.value = false;
  }
};

const loadAdjustOrderList = async () => {
  if (!selectedWarehouseId.value) {
    adjustOrderList.value = [];
    return;
  }
  
  loading.value = true;
  try {
    const params = {
      page: pagination.current,
      size: pagination.size,
      warehouseId: selectedWarehouseId.value,
      ...filterForm
    };
    
    if (filterForm.dateRange && filterForm.dateRange.length === 2) {
      params.startDate = filterForm.dateRange[0];
      params.endDate = filterForm.dateRange[1];
    }
    
    const res = await post('/api/auth/adjust/pageList', params);
    if (res && res.records) {
      adjustOrderList.value = res.records.map(item => ({
        id: item.id || '',
        adjustNo: item.adjustNo || '',
        warehouseId: item.warehouseId || '',
        warehouseName: item.warehouseName || '',
        adjustType: item.adjustType || 1,
        sourceType: item.sourceType || 1,
        sourceId: item.sourceId || '',
        sourceNo: item.sourceNo || '',
        adjustReason: item.adjustReason || '',
        adjustStatus: item.adjustStatus || 1,
        totalItems: item.totalItems || 0,
        totalQuantity: item.totalQuantity || 0,
        totalAmount: item.totalAmount || 0,
        isUrgent: item.isUrgent || false,
        isAffectCost: item.isAffectCost || false,
        createdByName: item.createdByName || '',
        createdAt: item.createdAt || '',
        modifiedAt: item.modifiedAt || ''
      }));
      pagination.total = res.total || 0;
    } else {
      adjustOrderList.value = [];
      pagination.total = 0;
    }
  } catch (error) {
    console.error('加载调整单列表失败:', error);
    ElMessage.error('加载调整单列表失败');
    adjustOrderList.value = [];
  } finally {
    loading.value = false;
  }
};

// 打开审核对话框
const openAuditDialog = (adjustOrder) => {
  // 重置审核表单
  Object.assign(auditForm, {
    approveStatus: 2,
    approveRemark: ''
  });
  
  currentAuditOrder.value = adjustOrder;
  auditDialogVisible.value = true;
};

const handleWarehouseSelect = () => {
  // 重置筛选条件
  Object.assign(filterForm, {
    adjustNo: '',
    adjustType: '',
    sourceType: '',
    adjustStatus: '',
    dateRange: []
  });
  
  // 重置分页
  pagination.current = 1;
  
  // 加载调整单列表
  if (selectedWarehouseId.value) {
    loadAdjustOrderList();
  } else {
    adjustOrderList.value = [];
  }
};

const handleSearch = () => {
  pagination.current = 1;
  loadAdjustOrderList();
};

const handleReset = () => {
  Object.assign(filterForm, {
    adjustNo: '',
    adjustType: '',
    sourceType: '',
    adjustStatus: '',
    dateRange: []
  });
  pagination.current = 1;
  loadAdjustOrderList();
};

const handleSizeChange = (size) => {
  pagination.size = size;
  pagination.current = 1;
  loadAdjustOrderList();
};

const handleCurrentChange = (page) => {
  pagination.current = page;
  loadAdjustOrderList();
};

// 新建调整单 - 跳转到调整单创建页面
const handleCreateNew = () => {
  if (!selectedWarehouseId.value) {
    ElMessage.warning('请先选择仓库');
    return;
  }
  
  // 跳转到调整单创建页面
  router.push({
    path: '/index/ckStockAdjustment/create',
    query: {
      createType: 'manual',
      warehouseId: selectedWarehouseId.value,
      warehouseName: selectedWarehouseName.value
    }
  });
};

// 查看调整单详情 - 跳转到调整单详情页面
const handleView = async (adjustOrder) => {
  try {
    // 跳转到调整单详情页面
    router.push({
      path: `/index/ckStockAdjustment/${adjustOrder.id}`,
      query: {
        viewMode: 'detail',
        // 如果调整单不是手动创建的，可能会有sourceType和sourceId
        sourceType: adjustOrder.sourceType,
        sourceId: adjustOrder.sourceId,
        sourceNo: adjustOrder.sourceNo,
        // 仓库信息用于页面显示
        warehouseId: adjustOrder.warehouseId,
        warehouseName: adjustOrder.warehouseName,
        // 创建类型：如果是盘点单创建的，需要标识
        createType: adjustOrder.sourceType === 1 ? 'stock_take' : 'adjust_detail'
      }
    });
  } catch (error) {
    console.error('跳转失败:', error);
    ElMessage.error('跳转失败');
  }
};

// 编辑调整单 - 跳转到调整单编辑页面
const handleEdit = async (adjustOrder) => {
  try {
    // 跳转到调整单编辑页面
    router.push({
      path: `/index/ckStockAdjustment/${adjustOrder.id}`,
      query: {
        editMode: 'true',
        warehouseId: adjustOrder.warehouseId,
        warehouseName: adjustOrder.warehouseName
      }
    });
  } catch (error) {
    console.error('跳转失败:', error);
    ElMessage.error('跳转失败');
  }
};

// 提交审核
const handleSubmit = async (adjustOrder) => {
  try {
    await ElMessageBox.confirm(
      '确定要提交审核吗？提交后不可再编辑。',
      '提交确认',
      { type: 'warning' }
    );
    
    const res = await get('/api/auth/adjust/submitApprove?id=' + adjustOrder.id);
    if (res) {
      ElMessage.success('提交成功');
      refreshList();
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('提交失败');
    }
  }
};

// 审核通过
const handleApprove = async (adjustOrder) => {
  try {
    await ElMessageBox.confirm(
      '确定要审核通过吗？',
      '审核确认',
      { type: 'warning' }
    );
    
    const res = await get('/api/auth/adjust/approve?id=' + adjustOrder.id);
    if (res) {
      ElMessage.success('审核通过成功');
      refreshList();
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('审核通过失败');
    }
  }
};

// 审核拒绝
const handleReject = async (adjustOrder) => {
  try {
    await ElMessageBox.prompt('请输入拒绝原因', '审核拒绝', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      inputType: 'textarea',
      inputPlaceholder: '请输入拒绝原因...',
      inputValidator: (value) => {
        if (!value || value.trim().length < 2) {
          return '拒绝原因不能少于2个字符';
        }
        return true;
      }
    }).then(async ({ value }) => {
      const res = await post('/api/auth/adjust/reject', {
        id: adjustOrder.id,
        rejectReason: value
      });
      
      if (res) {
        ElMessage.success('审核拒绝成功');
        refreshList();
      }
    }).catch(() => {
      // 用户取消
    });
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('审核拒绝失败');
    }
  }
};

// 执行调整
const handleExecute = async (adjustOrder) => {
  try {
    await ElMessageBox.confirm(
      '确定要执行调整吗？执行后库存数据将被更新。',
      '执行确认',
      { type: 'warning' }
    );
    
    const res = await get('/api/auth/adjust/execute?id=' + adjustOrder.id);
    if (res) {
      ElMessage.success('执行调整成功');
      refreshList();
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('执行调整失败');
    }
  }
};

// 查看执行结果
const handleViewResult = async (adjustOrder) => {
  try {
    // 跳转到调整单执行结果页面
    router.push({
      path: `/index/ckStockAdjustment/${adjustOrder.id}`,
      query: {
        viewMode: 'result',
        resultView: 'true'
      }
    });
  } catch (error) {
    console.error('跳转失败:', error);
    ElMessage.error('跳转失败');
  }
};

// 删除调整单
const handleDelete = async (adjustOrder) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除调整单"${adjustOrder.adjustNo}"吗？`,
      '删除确认',
      { type: 'warning' }
    );
    
    const res = await get('/api/auth/adjust/delete?id=' + adjustOrder.id);
    if (res) {
      ElMessage.success('删除成功');
      refreshList();
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败');
    }
  }
};

const refreshList = () => {
  if (selectedWarehouseId.value) {
    loadAdjustOrderList();
  }
};

// 工具方法
const getAdjustTypeLabel = (type) => {
  const mapping = {
    1: '盘点调整',
    2: '报损调整',
    3: '报溢调整',
    4: '成本调整',
    5: '库存转移',
    6: '其他调整'
  };
  return mapping[type] || '未知';
};

const getAdjustTypeTagType = (type) => {
  const mapping = {
    1: 'primary',
    2: 'danger',
    3: 'success',
    4: 'warning',
    5: 'info',
    6: ''
  };
  return mapping[type] || 'info';
};

const getSourceTypeLabel = (type) => {
  const mapping = {
    1: '盘点单',
    2: '手动创建',
    3: '异常处理',
    4: '系统自动'
  };
  return mapping[type] || '未知';
};

const getStatusLabel = (status) => {
  const mapping = {
    0: '待提交',
    1: '待审核',
    2: '审核通过',
    4: '审核拒绝',
    9: '已取消',
    //20-调整完成 29-调整失败
    20: '调整完成',
    29: '调整失败'
  };
  return mapping[status] || '未知';
};

const getStatusTagType = (status) => {
  const mapping = {
    1: 'info',
    2: 'warning',
    3: 'success',
    4: 'danger',
    5: 'primary',
    6: 'info'
  };
  return mapping[status] || 'info';
};

const getQuantityClass = (quantity) => {
  if (quantity > 0) return 'quantity-positive';
  if (quantity < 0) return 'quantity-negative';
  return 'quantity-zero';
};

const getAmountClass = (amount) => {
  if (amount > 0) return 'amount-positive';
  if (amount < 0) return 'amount-negative';
  return 'amount-zero';
};

const formatNumber = (num) => {
  if (num === null || num === undefined) return '--';
  const number = Number(num);
  if (isNaN(number)) return '--';
  return number.toLocaleString('zh-CN', {
    minimumFractionDigits: 0,
    maximumFractionDigits: 4
  });
};

const formatCurrency = (num) => {
  if (num === null || num === undefined) return '0.00';
  const number = Number(num);
  if (isNaN(number)) return '0.00';
  return number.toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',');
};

const formatDateTime = (dateStr) => {
  if (!dateStr) return '--';
  try {
    const date = new Date(dateStr);
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    });
  } catch (e) {
    return dateStr;
  }
};

onMounted(() => {
  loadWarehouseList();
});
</script>

<style scoped>
/* 使用与盘点页面相同的样式体系，保持一致性 */
.adjust-order-manage-container {
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

.warehouse-select-section {
  padding: 20px 0;
  border-bottom: 1px solid #ebeef5;
  background: linear-gradient(135deg, #f6f8ff 0%, #f0f7ff 100%);
  border-radius: 8px;
  margin-bottom: 20px;
}

.filter-section {
  padding: 20px 0;
  border-bottom: 1px solid #ebeef5;
}

.adjust-order-list-section {
  margin-top: 20px;
}

.adjust-order-table {
  width: 100%;
}

.adjust-order-no {
  font-family: 'Courier New', monospace;
  font-weight: bold;
  color: #409EFF;
}

.source-info {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #606266;
}

.source-info .el-icon {
  color: #909399;
}

.detail-info {
  color: #606266;
  font-size: 14px;
}

/* 数量相关样式 */
.quantity-positive, .after-positive {
  color: #67C23A;
  font-weight: bold;
}

.quantity-negative, .adjust-negative {
  color: #F56C6C;
  font-weight: bold;
}

.quantity-zero, .adjust-zero, .after-zero {
  color: #909399;
}

.adjust-positive {
  color: #409EFF;
  font-weight: bold;
}

/* 金额相关样式 */
.amount-positive {
  color: #67C23A;
  font-weight: bold;
}

.amount-negative {
  color: #F56C6C;
  font-weight: bold;
}

.amount-zero {
  color: #909399;
}

.action-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  justify-content: center;
}

.pagination-section {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.no-warehouse-tip {
  padding: 60px 0;
  text-align: center;
}

.no-warehouse-tip .el-empty__description {
  margin-top: 10px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .adjust-order-manage-container {
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
  
  .warehouse-select-section,
  .filter-section .el-form-item {
    margin-bottom: 12px;
  }
  
  .action-buttons {
    flex-direction: column;
    gap: 4px;
  }
}

/* 动画效果 */
.adjust-order-table :deep(.el-table__row) {
  transition: all 0.3s;
}

.adjust-order-table :deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}

/* 审核对话框样式 */
.audit-dialog-content {
  padding: 10px 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .adjust-order-manage-container {
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
  
  .warehouse-select-section,
  .filter-section .el-form-item {
    margin-bottom: 12px;
  }
  
  .action-buttons {
    flex-direction: column;
    gap: 4px;
  }
}

/* 动画效果 */
.adjust-order-table :deep(.el-table__row) {
  transition: all 0.3s;
}

.adjust-order-table :deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}
</style>