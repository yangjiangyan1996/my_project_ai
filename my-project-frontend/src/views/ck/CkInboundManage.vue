<template>
  <div class="inbound-manage-container">
    <el-card class="manage-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">入库管理</span>
          <div class="header-actions">
            <el-dropdown @command="handleCreate" trigger="click">
              <el-button type="primary">
                <el-icon><Plus /></el-icon>
                新建入库单
                <el-icon class="el-icon--right"><arrow-down /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="purchase">采购入库</el-dropdown-item>
                  <el-dropdown-item command="production">生产入库</el-dropdown-item>
                  <el-dropdown-item command="return">退货入库</el-dropdown-item>
                  <el-dropdown-item command="transfer">调拨入库</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
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

      <!-- 筛选条件 -->
      <div class="filter-section">
        <el-form :model="filterForm" inline>
          <el-form-item label="入库单号">
            <el-input
              v-model="filterForm.relatedOrderNo"
              placeholder="请输入入库单号"
              clearable
              style="width: 200px"
            />
          </el-form-item>
          <el-form-item label="入库类型">
            <el-select
              v-model="filterForm.orderType"
              placeholder="全部类型"
              clearable
              style="width: 120px"
            >
              <el-option
                v-for="item in orderTypeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="仓库">
            <el-select
              v-model="filterForm.warehouseId"
              placeholder="全部仓库"
              clearable
              style="width: 150px"
            >
              <el-option
                v-for="warehouse in warehouseList"
                :key="warehouse.id"
                :label="warehouse.name"
                :value="warehouse.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="供应商">
            <el-select
              v-model="filterForm.supplierId"
              placeholder="全部供应商"
              clearable
              style="width: 150px"
            >
              <el-option
                v-for="supplier in supplierList"
                :key="supplier.id"
                :label="supplier.supplierName"
                :value="supplier.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select
              v-model="filterForm.status"
              placeholder="全部状态"
              clearable
              style="width: 120px"
            >
              <el-option
                v-for="item in statusOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
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

      <!-- 统计信息 -->
      <div class="stats-section">
        <el-row :gutter="20">
          <el-col :xs="12" :sm="6" :lg="3">
            <div class="stat-item total">
              <div class="stat-icon">
                <el-icon><Document /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ stats.total }}</div>
                <div class="stat-label">总单数</div>
              </div>
            </div>
          </el-col>
          <el-col :xs="12" :sm="6" :lg="3">
            <div class="stat-item pending">
              <div class="stat-icon">
                <el-icon><Clock /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ stats.pending }}</div>
                <div class="stat-label">待审核</div>
              </div>
            </div>
          </el-col>
          <el-col :xs="12" :sm="6" :lg="3">
            <div class="stat-item approved">
              <div class="stat-icon">
                <el-icon><CircleCheck /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ stats.approved }}</div>
                <div class="stat-label">已通过</div>
              </div>
            </div>
          </el-col>
          <el-col :xs="12" :sm="6" :lg="3">
            <div class="stat-item completed">
              <div class="stat-icon">
                <el-icon><Finished /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ stats.completed }}</div>
                <div class="stat-label">已拒绝</div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 入库单列表 -->
      <div class="inbound-list-section">
        <el-table
          :data="inboundList"
          v-loading="loading"
          empty-text="暂无入库单数据"
          class="inbound-table"
          row-key="id"
        >
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column label="入库单号" width="180" fixed="left">
            <template #default="{ row }">
              <div class="order-info">
                <span class="order-no">{{ row.orderNo }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="入库类型" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="getTypeTagType(row.orderType)" size="small">
                {{ getTypeText(row.orderType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="仓库" width="120">
            <template #default="{ row }">
              <span>{{ row.warehouseName }}</span>
            </template>
          </el-table-column>
          <el-table-column label="供应商" width="120">
            <template #default="{ row }">
              <span>{{ row.supplierName || '--' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="产品数量" width="100" align="center">
            <template #default="{ row }">
              <span>{{ row.itemCount }} 种</span>
            </template>
          </el-table-column>
          <el-table-column label="总数量" width="100" align="center">
            <template #default="{ row }">
              <span>{{ row.totalQuantity }}</span>
            </template>
          </el-table-column>
          <el-table-column label="总金额" width="120" align="center">
            <template #default="{ row }">
              <span>¥{{ (row.totalAmount || 0).toFixed(2) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag 
                :type="getStatusTagType(row.status)" 
                size="small"
              >
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" width="160">
            <template #default="{ row }">
              <span>{{ formatTime(row.createdAt) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="250" fixed="right" align="center" min-width="250">
            <template #default="{ row }">
              <div class="action-buttons">
                <el-button
                  type="primary"
                  link
                  size="small"
                  @click="handleView(row)"
                >
                  查看
                </el-button>
                <el-button
                  type="warning"
                  link
                  size="small"
                  @click="handleEdit(row)"
                  v-if="row.status === 0 || row.status === 4"
                >
                  编辑
                </el-button>
                <el-button
                  type="success"
                  link
                  size="small"
                  @click="handleApprove(row)"
                  v-if="row.status === 0"
                >
                  提交审核
                </el-button>
                <el-button
                  type="success"
                  link
                  size="small"
                  @click="handleSubmit(row)"
                  v-if="row.status === 0"
                >
                  提交
                </el-button>
                <el-button
                  type="danger"
                  link
                  size="small"
                  @click="handleDelete(row)"
                  v-if="row.status === 0 || row.status === 4"
                >
                  删除
                </el-button>
                <el-button
                  type="info"
                  link
                  size="small"
                  @click="handleCancel(row)"
                  v-if="row.status === 1 || row.status === 2"
                >
                  取消
                </el-button>
                <el-button
                  type="success"
                  link
                  size="small"
                  @click="handleComplete(row)"
                  v-if="row.status === 2"
                >
                  完成
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

    <!-- 入库单详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      :title="`入库单详情 - ${currentInbound?.orderNo || '未知单号'}`"
      width="95%"
      top="5vh"
      class="detail-dialog"
    >
      <div v-if="currentInbound" class="detail-content">
        <!-- 基本信息 -->
        <el-card class="detail-section" shadow="never">
          <template #header>
            <div class="section-header">
              <span class="section-title">基本信息</span>
            </div>
          </template>
          <el-descriptions :column="4" border>
            <el-descriptions-item label="入库单号">{{ currentInbound.orderNo }}</el-descriptions-item>
            <el-descriptions-item label="入库类型">{{ getTypeText(currentInbound.orderType) }}</el-descriptions-item>
            <el-descriptions-item label="仓库">{{ currentInbound.warehouseName }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="getStatusTagType(currentInbound.status)" size="small">
                {{ getStatusText(currentInbound.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="供应商">{{ currentInbound.supplierName || '--' }}</el-descriptions-item>
            <el-descriptions-item label="关联单号">{{ currentInbound.relatedOrderNo || '--' }}</el-descriptions-item>
            <el-descriptions-item label="产品种类">{{ currentInbound.itemCount }} 种</el-descriptions-item>
            <el-descriptions-item label="总数量">{{ currentInbound.totalQuantity }}</el-descriptions-item>
            <el-descriptions-item label="总金额">¥{{ (currentInbound.totalAmount || 0).toFixed(2) }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ formatTime(currentInbound.createdAt) }}</el-descriptions-item>
            <el-descriptions-item label="更新时间">{{ formatTime(currentInbound.modifiedAt) }}</el-descriptions-item>
            <el-descriptions-item label="备注" :span="2">{{ currentInbound.remark || '--' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 产品明细 -->
        <el-card class="detail-section" shadow="never" v-if="currentInbound.items && currentInbound.items.length > 0">
          <template #header>
            <div class="section-header">
              <span class="section-title">产品明细</span>
              <span class="section-subtitle">共 {{ currentInbound.items.length }} 个产品</span>
            </div>
          </template>
          <el-table :data="currentInbound.items" border style="width: 100%">
            <el-table-column type="index" label="序号" width="60" align="center" />
            <el-table-column label="产品信息" min-width="200">
              <template #default="{ row }">
                <div class="product-info">
                  <div class="product-name">{{ row.productName }}</div>
                  <div class="product-sku">SKU: {{ row.sku }}</div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="规格型号" width="120" prop="spec" />
            <el-table-column label="单位" width="80" align="center" prop="unit" />
            <el-table-column label="实际数量" width="100" align="center">
              <template #default="{ row }">
                <span>{{ row.actualQuantity }}</span>
              </template>
            </el-table-column>
            <el-table-column label="单价" width="120" align="right">
              <template #default="{ row }">
                <span>¥{{ (row.priceUnit || 0).toFixed(4) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="总价" width="120" align="right">
              <template #default="{ row }">
                <span class="price-total">¥{{ (row.priceTotal || 0).toFixed(2) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="批次号" width="150" prop="batchNo">
              <template #default="{ row }">
                <span>{{ row.batchNo || '--' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="货架分配" min-width="200">
              <template #default="{ row }">
                <div v-if="row.shelfAllocations && row.shelfAllocations.length > 0" class="shelf-allocation-info">
                  <div 
                    v-for="allocation in row.shelfAllocations" 
                    :key="allocation.shelfLocationId"
                    class="shelf-allocation-item"
                  >
                    <el-tag size="small" type="info">
                      {{ allocation.shelfLocationName }}: {{ allocation.quantity }}
                    </el-tag>
                  </div>
                </div>
                <span v-else class="no-allocation">--</span>
              </template>
            </el-table-column>
            <el-table-column label="备注" min-width="150" prop="remark">
              <template #default="{ row }">
                <span>{{ row.remark || '--' }}</span>
              </template>
            </el-table-column>
          </el-table>

          <!-- 产品统计 -->
          <div class="product-summary">
            <el-row :gutter="20">
              <el-col :span="6">
                <div class="summary-item">
                  <span class="label">产品种类：</span>
                  <span class="value">{{ currentInbound.items.length }} 种</span>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="summary-item">
                  <span class="label">总数量：</span>
                  <span class="value">{{ currentInbound.totalQuantity }}</span>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="summary-item">
                  <span class="label">总金额：</span>
                  <span class="value">¥{{ (currentInbound.totalAmount || 0).toFixed(2) }}</span>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-card>
      </div>
      <div v-else class="no-data">
        <el-empty description="数据加载失败" />
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
          <el-button 
            type="primary" 
            @click="handleEdit(currentInbound)"
            v-if="currentInbound && (currentInbound.status === 0 || currentInbound.status === 4)"
          >
            编辑
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Refresh, Document, Clock, CircleCheck, Finished } from '@element-plus/icons-vue';
import { post, get } from '@/net';

const router = useRouter();
const loading = ref(false);
const detailDialogVisible = ref(false);
const currentInbound = ref(null);

// 添加入库类型映射
const inboundTypeMap = {
  1: 'purchase',
  2: 'production', 
  3: 'return',
  4: 'transfer'
};


// 修改新建处理方法
const handleCreate = (command) => {
  const routeMap = {
    'purchase': '/index/ckInboundPurchase',
    'production': '/index/ckInboundProduction',
    'return': '/index/ckInboundReturn',
    'transfer': '/index/ckInboundTransfer'
  };
  
  const targetRoute = routeMap[command];
  if (targetRoute) {
    router.push(targetRoute);
  }
};


// 筛选表单
const filterForm = reactive({
  relatedOrderNo: '',
  orderType: '',
  warehouseId: '',
  supplierId: '',
  status: '',
  dateRange: []
});

// 分页信息
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
});

// 统计信息
const stats = reactive({
  total: 0,
  pending: 0,
  approved: 0,
  completed: 0
});

// 入库单列表
const inboundList = ref([]);
const warehouseList = ref([]);
const supplierList = ref([]);

// 选项数据
const orderTypeOptions = [
  { value: 1, label: '采购入库' },
  { value: 2, label: '生产入库' },
  { value: 3, label: '退货入库' },
  { value: 4, label: '调拨入库' }
];

const statusOptions = [
  { value: 0, label: '待提交' },
  { value: 1, label: '审核中' },
  { value: 2, label: '已通过' },
  { value: 3, label: '已完成' },
  { value: 4, label: '已拒绝' },
  { value: 9, label: '已取消' }
];

// 加载入库单详情 - 根据新的API响应结构调整
const loadInboundDetail = async (id) => {
  try {
    const res = await get(`/api/auth/inbound/detail?orderId=${id}`);
    console.log('入库单详情响应:', res);
    
    if (res) {
      const detailData = res;
      return {
        // 基本信息
        id: detailData.id,
        orderNo: detailData.orderNo,
        orderType: detailData.orderType,
        warehouseId: detailData.warehouseId,
        warehouseName: detailData.warehouseName,
        supplierId: detailData.supplierId,
        supplierName: detailData.supplierName,
        relatedOrderNo: detailData.relatedOrderNo,
        remark: detailData.remark,
        status: detailData.status,
        itemCount: detailData.itemCount,
        totalQuantity: detailData.totalQuantity,
        totalAmount: detailData.totalAmount,
        createdAt: detailData.createdAt,
        modifiedAt: detailData.modifiedAt,
        
        // 产品明细 - 根据新的数据结构调整
        items: detailData.items ? detailData.items.map(item => ({
          id: item.itemId,
          productId: item.productId,
          productName: item.productName,
          sku: item.sku,
          spec: item.spec,
          unit: item.unit,
          actualQuantity: item.actualQuantity,
          priceUnit: item.priceUnit,
          priceTotal: item.priceTotal,
          batchNo: item.batchNo,
          remark: item.remark,
          // 货架分配信息
          shelfAllocations: item.shelfAllocations || []
        })) : []
      };
    } else {
      console.error('API返回数据格式异常:', res);
      return null;
    }
  } catch (error) {
    console.error('加载入库单详情失败:', error);
    ElMessage.error('加载详情失败: ' + (error.message || '未知错误'));
    return null;
  }
};

// 新增：加载统计信息的方法
const loadStats = async () => {
  try {

    const params = {
      page: pagination.current,
      size: pagination.size,
      ...filterForm
    };
    
    // 处理日期范围
    if (filterForm.dateRange && filterForm.dateRange.length === 2) {
      params.startDate = filterForm.dateRange[0];
      params.endDate = filterForm.dateRange[1];
    }
    
    const res = await post('/api/auth/inbound/countsOfManagePage', params);

    if (res) {
      stats.total = res.totalCount || 0;
      stats.pending = res.waitApproveCount || 0;
      stats.approved = res.approvePassCount || 0;
      stats.completed = res.approveRejectCount || 0;
    }
  } catch (error) {
    console.error('加载统计信息失败:', error);
    // 失败时重置统计信息
    stats.total = 0;
    stats.pending = 0;
    stats.approved = 0;
    stats.completed = 0;
  }
};

// 方法
const loadInboundList = async () => {
  loading.value = true;
  try {
    const params = {
      page: pagination.current,
      size: pagination.size,
      ...filterForm
    };
    
    // 处理日期范围
    if (filterForm.dateRange && filterForm.dateRange.length === 2) {
      params.startDate = filterForm.dateRange[0];
      params.endDate = filterForm.dateRange[1];
    }
    
    const res = await post('/api/auth/inbound/pageList', params);
    if (res && res.records) {
      inboundList.value = res.records.map(inbound => ({
        id: inbound.id || '',
        orderNo: inbound.orderNo || '',
        orderType: inbound.orderType || 1,
        warehouseId: inbound.warehouseId || '',
        warehouseName: inbound.warehouseName || '',
        supplierId: inbound.supplierId || '',
        supplierName: inbound.supplierName || '',
        itemCount: inbound.itemCount || 0,
        totalQuantity: inbound.totalQuantity || 0,
        totalAmount: inbound.totalAmount || 0,
        status: inbound.status || 0,
        remark: inbound.remark || '',
        createdAt: inbound.createdAt || new Date().toISOString(),
        modifiedAt: inbound.modifiedAt || new Date().toISOString()
      }));
      pagination.total = res.total || 0;
      
      
    } else {
      inboundList.value = [];
      pagination.total = 0;
    }
  } catch (error) {
    console.error('加载入库单列表失败:', error);
    ElMessage.error('加载入库单列表失败');
    inboundList.value = [];
  } finally {
    loading.value = false;
  }
};

const loadWarehouseList = async () => {
  try {
    const res = await get('/api/auth/warehouse/list');
    warehouseList.value = res || [];
  } catch (error) {
    console.error('加载仓库列表失败:', error);
    warehouseList.value = [];
  }
};

const loadSupplierList = async () => {
  try {
    const res = await get('/api/auth/supplier/listEnable');
    supplierList.value = res || [];
  } catch (error) {
    console.error('加载供应商列表失败:', error);
    supplierList.value = [];
  }
};



const refreshList = () => {
  pagination.current = 1;
  loadInboundList();
  loadStats(); // 新增：刷新统计信息
};

const handleSearch = () => {
  pagination.current = 1;
  loadInboundList();
  loadStats(); // 新增：搜索时更新统计信息
};

const handleReset = () => {
  Object.assign(filterForm, {
    relatedOrderNo: '',
    orderType: '',
    warehouseId: '',
    supplierId: '',
    status: '',
    dateRange: []
  });
  pagination.current = 1;
  loadInboundList();
  loadStats(); // 新增：重置时更新统计信息
};

const handleSizeChange = (size) => {
  pagination.size = size;
  pagination.current = 1;
  loadInboundList();
};

const handleCurrentChange = (page) => {
  pagination.current = page;
  loadInboundList();
};

// const handleCreate = () => {
//   router.push('/index/ckInboundCreate');
// };

// 修改查看和编辑方法
const handleView = (inbound) => {
  console.log("查看",inbound);
  const typeKey = inboundTypeMap[inbound.orderType];
  if (typeKey) {
    router.push(`/index/ckInbound${typeKey.charAt(0).toUpperCase() + typeKey.slice(1)}Edit/${inbound.id}`);
  } else {
    ElMessage.error('未知的入库类型');
  }
};

const handleEdit = (inbound) => {
  console.log("编辑",inbound);
  const typeKey = inboundTypeMap[inbound.orderType];
  console.log("typeKey",typeKey.charAt(0).toUpperCase() + typeKey.slice(1) +"Edit");
  if (typeKey) {
    router.push(`/index/ckInbound${typeKey.charAt(0).toUpperCase() + typeKey.slice(1)}Edit/${inbound.id}`);
  } else {
    ElMessage.error('未知的入库类型');
  }
};
const handleSubmit = async (inbound) => {
  try {
    await ElMessageBox.confirm(
      `确定要提交入库单"${inbound.orderNo}"吗？`,
      '提交确认',
      { type: 'warning' }
    );
    
    const res = await post('/api/auth/inbound/approveOk', {
      orderId: inbound.id,
      status: 2
    });
    
    if (res) {
      ElMessage.success('提交成功');
      refreshList();
      loadStats(); // 新增：组件挂载时加载统计信息
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('提交失败');
    }
  }
};

const handleApprove = async (inbound) => {
  ElMessage.success('审核功能暂未开发');
};

const handleDelete = async (inbound) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除入库单"${inbound.orderNo}"吗？此操作不可恢复！`,
      '删除确认',
      { 
        type: 'error',
        confirmButtonText: '确定删除',
        cancelButtonText: '取消'
      }
    );
    
    const res = await post('/api/auth/inbound/delete', {
      id: inbound.id
    });
    
    if (res) {
      ElMessage.success('删除成功');
      refreshList();
      loadStats(); // 新增：组件挂载时加载统计信息
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败');
    }
  }
};

const handleCancel = async (inbound) => {
  try {
    await ElMessageBox.confirm(
      `确定要取消入库单"${inbound.orderNo}"吗？`,
      '取消确认',
      { type: 'warning' }
    );
    
    const res = await post('/api/auth/inbound/updateStatus', {
      id: inbound.id,
      status: 9
    });
    
    if (res) {
      ElMessage.success('取消成功');
      refreshList();
      loadStats(); // 新增：组件挂载时加载统计信息
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消失败');
    }
  }
};

const handleComplete = async (inbound) => {
  try {
    await ElMessageBox.confirm(
      `确定要完成入库单"${inbound.orderNo}"吗？完成后将更新库存数量。`,
      '完成确认',
      { type: 'warning' }
    );
    
    const res = await post('/api/auth/inbound/updateStatus', {
      id: inbound.id,
      status: 3
    });
    
    if (res) {
      ElMessage.success('入库完成');
      refreshList();
      loadStats(); // 新增：组件挂载时加载统计信息
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('完成失败');
    }
  }
};

const getTypeText = (orderType) => {
  const typeObj = orderTypeOptions.find(item => item.value === orderType);
  return typeObj ? typeObj.label : '未知';
};

const getTypeTagType = (orderType) => {
  const types = {
    1: 'success',
    2: 'warning',
    3: 'info',
    4: 'primary'
  };
  return types[orderType] || '';
};

const getStatusText = (status) => {
  const statusObj = statusOptions.find(item => item.value === status);
  return statusObj ? statusObj.label : '未知';
};

const getStatusTagType = (status) => {
  const types = {
    0: 'info',
    1: 'warning',
    2: 'success',
    3: '',
    4: 'danger',
    9: 'info'
  };
  return types[status] || '';
};

const formatTime = (timeString) => {
  if (!timeString) return '--';
  try {
    const date = new Date(timeString);
    return `${date.getFullYear()}-${padZero(date.getMonth() + 1)}-${padZero(date.getDate())} ${padZero(date.getHours())}:${padZero(date.getMinutes())}`;
  } catch {
    return '--';
  }
};

const padZero = (num) => {
  return num < 10 ? `0${num}` : num;
};

onMounted(() => {
  loadInboundList();
  loadWarehouseList();
  loadSupplierList();
  loadStats(); // 新增：组件挂载时加载统计信息
});
</script>

<style scoped>
.inbound-manage-container {
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

.filter-section {
  padding: 20px 0;
  border-bottom: 1px solid #ebeef5;
}

.stats-section {
  padding: 20px 0;
}

.stat-item {
  display: flex;
  align-items: center;
  padding: 16px;
  border-radius: 8px;
  background: white;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  color: white;
  font-size: 24px;
}

.stat-item.total .stat-icon {
  background-color: #409EFF;
}

.stat-item.pending .stat-icon {
  background-color: #E6A23C;
}

.stat-item.approved .stat-icon {
  background-color: #67C23A;
}

.stat-item.completed .stat-icon {
  background-color: #909399;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.inbound-list-section {
  margin-top: 20px;
}

.inbound-table {
  width: 100%;
}

.order-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.order-no {
  font-family: 'Courier New', monospace;
  font-weight: bold;
  color: #409EFF;
}

.action-buttons {
  display: flex;
  gap: 8px;
  justify-content: center;
}

.pagination-section {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

/* 详情对话框样式 */
.detail-dialog {
  max-width: 1200px;
}

.detail-content {
  max-height: 70vh;
  overflow-y: auto;
}

.detail-section {
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

.section-subtitle {
  font-size: 14px;
  color: #909399;
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

.price-total {
  font-weight: bold;
  color: #409eff;
}

.shelf-allocation-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.shelf-allocation-item {
  display: flex;
}

.no-allocation {
  color: #909399;
  font-style: italic;
}

.product-summary {
  margin-top: 16px;
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

.dialog-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.no-data {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 200px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .inbound-manage-container {
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
  
  .filter-section .el-form-item {
    margin-bottom: 12px;
  }
  
  .stats-section .el-col {
    margin-bottom: 12px;
  }
  
  .action-buttons {
    flex-direction: column;
    gap: 4px;
  }
  
  .detail-dialog {
    width: 95% !important;
  }
  
  .dialog-footer {
    flex-direction: column;
    gap: 12px;
  }
}

/* 动画效果 */
.inbound-table :deep(.el-table__row) {
  transition: all 0.3s;
}

.inbound-table :deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}
</style>