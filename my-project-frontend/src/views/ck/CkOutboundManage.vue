<template>
  <div class="outbound-manage-container">
    <el-card class="manage-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">出库管理</span>
          <div class="header-actions">
            <el-dropdown @command="handleCreate" trigger="click">
              <el-button type="primary">
                <el-icon><Plus /></el-icon>
                新建出库单
                <el-icon class="el-icon--right"><arrow-down /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="1">销售出库</el-dropdown-item>
                  <el-dropdown-item command="2">生产领料</el-dropdown-item>
                  <el-dropdown-item command="3">退货出库</el-dropdown-item>
                  <el-dropdown-item command="4">调拨出库</el-dropdown-item>
                  <el-dropdown-item command="5">其他出库</el-dropdown-item>
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
          <el-form-item label="出库单号">
            <el-input
              v-model="filterForm.orderNo"
              placeholder="请输入出库单号"
              clearable
              style="width: 200px"
            />
          </el-form-item>
          <el-form-item label="出库类型">
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
          <el-form-item label="客户">
            <el-select
              v-model="filterForm.customerId"
              placeholder="全部客户"
              clearable
              style="width: 150px"
            >
              <el-option
                v-for="customer in customerList"
                :key="customer.id"
                :label="customer.customerName"
                :value="customer.id"
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
            <el-button @click="handleExport">导出</el-button>
            <!-- 新增：组合导出按钮 -->
            <el-button 
              type="success" 
              @click="handleBatchExport"
              :disabled="selectedRows.length === 0"
              :loading="batchExportLoading"
            >
              <el-icon><Download /></el-icon>
              批量导出（{{ selectedRows.length }}）
            </el-button>
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

      <!-- 出库单列表 -->
      <div class="outbound-list-section">
        <el-table
          :data="outboundList"
          v-loading="loading"
          empty-text="暂无出库单数据"
          class="outbound-table"
          row-key="id"
           @selection-change="handleSelectionChange"
        >
          <!-- 新增：选择列 -->
          <el-table-column 
            type="selection" 
            width="55" 
            align="center"
            :reserve-selection="false"
          />
          
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column label="出库单号" width="180" fixed="left">
            <template #default="{ row }">
              <div class="order-info">
                <span class="order-no">{{ row.orderNo }}</span>
                <el-tag 
                  v-if="row.isUrgent" 
                  type="danger" 
                  size="small" 
                  class="urgent-tag"
                >
                  紧急
                </el-tag>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="出库类型" width="100" align="center">
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
          <el-table-column label="客户" width="120">
            <template #default="{ row }">
              <span>{{ row.customerName || '--' }}</span>
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
          <el-table-column label="总金额" width="120" align="right">
            <template #default="{ row }">
              <span class="amount">¥{{ (row.totalAmount || 0).toFixed(2) }}</span>
            </template>
          </el-table-column>
           <!-- 新增USD总额列 -->
          <el-table-column label="USD总额" width="120" align="right">
            <template #default="{ row }">
              <span class="amount-usd">${{ (row.totalAmountUsd || 0).toFixed(2) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="申请人" width="120">
            <template #default="{ row }">
              <div class="applicant-info">
                <el-avatar :size="24" :src="row.applicantAvatar" class="applicant-avatar" />
                <span class="applicant-name">{{ row.applicantName }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" width="160">
            <template #default="{ row }">
              <span>{{ formatTime(row.createdAt) }}</span>
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
          <el-table-column label="操作" width="250" fixed="right" align="center">
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
                <!-- 新增导出按钮 -->
                <el-button
                  type="info"
                  link
                  size="small"
                  @click="handleDownloadTemplate(row)"
                  :loading="row.exportLoading"
                  v-if="row.orderType === 1 && row.status === 2"
                >
                  导出
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
                <!-- <el-button
                  type="success"
                  link
                  size="small"
                  @click="handleComplete(row)"
                  v-if="row.status === 2"
                >
                  完成
                </el-button> -->
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

    <!-- 出库单详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      :title="`出库单详情 - ${currentOutbound?.orderNo || '未知单号'}`"
      width="95%"
      top="5vh"
      class="detail-dialog"
    >
      <div v-if="currentOutbound" class="detail-content">
        <!-- 基本信息 -->
        <el-card class="detail-section" shadow="never">
          <template #header>
            <div class="section-header">
              <span class="section-title">基本信息</span>
            </div>
          </template>
          <el-descriptions :column="4" border>
            <el-descriptions-item label="出库单号">{{ currentOutbound.orderNo }}</el-descriptions-item>
            <el-descriptions-item label="出库类型">{{ getTypeText(currentOutbound.orderType) }}</el-descriptions-item>
            <el-descriptions-item label="仓库">{{ currentOutbound.warehouseName }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="getStatusTagType(currentOutbound.status)" size="small">
                {{ getStatusText(currentOutbound.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="客户">{{ currentOutbound.customerName || '--' }}</el-descriptions-item>
            <el-descriptions-item label="关联单号">{{ currentOutbound.relatedOrderNo || '--' }}</el-descriptions-item>
            <el-descriptions-item label="预计出库日期">{{ currentOutbound.expectedDate || '--' }}</el-descriptions-item>
            <el-descriptions-item label="产品种类">{{ currentOutbound.itemCount }} 种</el-descriptions-item>
            <el-descriptions-item label="总数量">{{ currentOutbound.totalQuantity }}</el-descriptions-item>
            <el-descriptions-item label="总金额">¥{{ (currentOutbound.totalAmount || 0).toFixed(2) }}</el-descriptions-item>
            <!-- 新增USD总额 -->
            <el-descriptions-item label="USD总额">${{ (currentOutbound.totalAmountUsd || 0).toFixed(2) }}</el-descriptions-item>
            <el-descriptions-item label="申请人">{{ currentOutbound.applicantName || '--' }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ formatTime(currentOutbound.createdAt) }}</el-descriptions-item>
            <el-descriptions-item label="更新时间">{{ formatTime(currentOutbound.updatedAt) }}</el-descriptions-item>
            <el-descriptions-item label="备注" :span="2">{{ currentOutbound.remark || '--' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 产品明细 -->
        <el-card class="detail-section" shadow="never" v-if="currentOutbound.items && currentOutbound.items.length > 0">
          <template #header>
            <div class="section-header">
              <span class="section-title">产品明细</span>
              <span class="section-subtitle">共 {{ currentOutbound.items.length }} 个产品</span>
            </div>
          </template>
          <el-table :data="currentOutbound.items" border style="width: 100%">
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
            <el-table-column label="出库数量" width="100" align="center">
              <template #default="{ row }">
                <span>{{ row.quantity }}</span>
              </template>
            </el-table-column>
            <el-table-column label="单价" width="120" align="right">
              <template #default="{ row }">
                <span>¥{{ (row.priceUnit || 0).toFixed(4) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="金额" width="120" align="right">
              <template #default="{ row }">
                <span class="price-total">¥{{ (row.priceTotal).toFixed(2) }}</span>
              </template>
            </el-table-column>
            <!-- 新增USD单价 -->
            <el-table-column label="USD单价" width="120" align="right">
              <template #default="{ row }">
                <span>${{ (row.priceUnitUsd || 0).toFixed(4) }}</span>
              </template>
            </el-table-column>
            <!-- 新增USD总额 -->
            <el-table-column label="USD总额" width="120" align="right">
              <template #default="{ row }">
                <span class="price-total-usd">${{ (row.priceTotalUsd || 0).toFixed(2) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="批次分配" min-width="200">
                <template #default="{ row }">
                  
                      <div v-if="row.batchAllocations && row.batchAllocations.length > 0" class="batch-summary">
                        <el-tag
                          v-for="allocation in row.batchAllocations"
                          :key="allocation.batchNo"
                          size="small"
                          class="batch-tag"
                        >
                          {{ allocation.batchNo }}: {{ allocation.quantity }}个
                        </el-tag>
                      </div>
                      <div v-else class="batch-empty">
                        <span class="empty-text">未分配批次</span>
                      </div>
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
              <el-col :span="4">
                <div class="summary-item">
                  <span class="label">产品种类：</span>
                  <span class="value">{{ currentOutbound.items.length }} 种</span>
                </div>
              </el-col>
              <el-col :span="4">
                <div class="summary-item">
                  <span class="label">总数量：</span>
                  <span class="value">{{ currentOutbound.totalQuantity }}</span>
                </div>
              </el-col>
              <el-col :span="4">
                <div class="summary-item">
                  <span class="label">总金额：</span>
                  <span class="value">¥{{ (currentOutbound.totalAmount || 0).toFixed(2) }}</span>
                </div>
              </el-col>
              <el-col :span="4">
                <div class="summary-item">
                  <span class="label">USD总额：</span>
                  <span class="value">${{ (currentOutbound.totalAmountUsd || 0).toFixed(2) }}</span>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-card>

        <!-- 附件信息 -->
        <el-card class="detail-section" shadow="never" v-if="currentOutbound.attachments && currentOutbound.attachments.length > 0">
          <template #header>
            <div class="section-header">
              <span class="section-title">附件信息</span>
              <span class="section-subtitle">共 {{ currentOutbound.attachments.length }} 个文件</span>
            </div>
          </template>
          <el-table :data="currentOutbound.attachments" border style="width: 100%">
            <el-table-column type="index" label="序号" width="60" align="center" />
            <el-table-column label="文件名称" prop="fileName" />
            <el-table-column label="文件大小" width="120" align="center">
              <template #default="{ row }">
                <span>{{ formatFileSize(row.fileSize) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="文件类型" width="120" align="center" prop="fileType" />
            <el-table-column label="操作" width="120" align="center">
              <template #default="{ row }">
                <el-button type="primary" link @click="handleDownload(row)">下载</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <!-- 原始数据展示（用于调试） -->
        <!-- <el-card class="detail-section" shadow="never" v-if="showRawData">
          <template #header>
            <div class="section-header">
              <span class="section-title">原始数据</span>
              <el-button type="text" @click="showRawData = !showRawData">
                {{ showRawData ? '隐藏' : '显示' }}原始数据
              </el-button>
            </div>
          </template>
          <pre class="raw-data">{{ JSON.stringify(currentOutbound, null, 2) }}</pre>
        </el-card> -->
      </div>
      <div v-else class="no-data">
        <el-empty description="数据加载失败" />
      </div>

      <template #footer>
        <div class="dialog-footer">
          <!-- <el-button @click="showRawData = !showRawData" type="info" link>
            {{ showRawData ? '隐藏原始数据' : '显示原始数据' }}
          </el-button> -->
          <el-button @click="detailDialogVisible = false">关闭</el-button>
          <el-button 
            type="primary" 
            @click="handleEdit(currentOutbound)"
            v-if="currentOutbound && (currentOutbound.status === 0 || currentOutbound.status === 4)"
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
import { Plus, Refresh, Document, Clock, CircleCheck, Finished, ArrowDown, Download } from '@element-plus/icons-vue';
import { post, get } from '@/net';
import axios from 'axios';
import { accessHeader } from '@/net'; 

const router = useRouter();
const loading = ref(false);
const detailDialogVisible = ref(false);
const currentOutbound = ref(null);
const showRawData = ref(false);

// 筛选表单
const filterForm = reactive({
  orderNo: '',
  orderType: '',
  warehouseId: '',
  customerId: '',
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

// 新增：选中的行数据
const selectedRows = ref([]);
const batchExportLoading = ref(false);

// 出库单列表
const outboundList = ref([]);
const warehouseList = ref([]);
const customerList = ref([]);

// 选项数据
const orderTypeOptions = [
  { value: 1, label: '销售出库' },
  { value: 2, label: '生产领料' },
  { value: 3, label: '退货出库' },
  { value: 4, label: '调拨出库' },
  { value: 5, label: '其他出库' }
];

const statusOptions = [
  { value: 0, label: '草稿' },
  { value: 1, label: '待审核' },
  { value: 2, label: '已通过' },
  { value: 3, label: '已完成' },
  { value: 4, label: '已拒绝' },
  { value: 9, label: '已取消' }
];

// 新增：获取出库类型图标
const getTypeIcon = (orderType) => {
  const icons = {
    1: 'ShoppingCart',
    2: 'Box',
    3: 'Refresh',
    4: 'Switch',
    5: 'More'
  };
  return icons[orderType] || 'Document';
};

// 新增：根据出库类型获取路由路径
const getRoutePathByType = (orderType, id = null, isEdit = false) => {
  const basePaths = {
    1: '/index/salesOutboundCreate',      // 销售出库
    2: '/index/productionPickingCreate',  // 生产领料
    // 3: '/index/ckOutboundCreateReturn',    // 退货出库 // 新建时候名称注意
    // 4: '/index/ckOutboundCreateTransfer',  // 调拨出库
    // 5: '/index/ckOutboundCreateOther'      // 其他出库
  };
  
  let path = basePaths[orderType] || '/index/ckOutboundCreate';
  
  // 如果有ID，则添加到路径中（编辑/查看模式）
  if (id) {
    path += `/${id}`;
  }
  
  return path;
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
    
    const res = await post('/api/auth/outbound/countsOfManagePage', params);
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

// 新增：处理选择变化
const handleSelectionChange = (selection) => {
  selectedRows.value = selection;
};

// 新增：批量导出方法
const handleBatchExport = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请先选择要导出的出库单');
    return;
  }
  
  try {
    // 检查是否都是相同的出库类型
    const orderTypes = [...new Set(selectedRows.value.map(row => row.orderType))];
    if (orderTypes.length > 1) {
      ElMessage.warning('请选择相同出库类型的订单进行批量导出');
      return;
    }
    
    batchExportLoading.value = true;
    
    const orderIds = selectedRows.value.map(row => row.id);
    
    const response = await axios.get(
      '/api/auth/outbound/exportOutboundOrderExcel',
      {
        params: {
          orderIds: orderIds.join(',')  // 将数组转换为逗号分隔的字符串
        },
        headers: accessHeader(),
        responseType: 'blob',
      }
    );
    
    // 从响应头读取文件名
    let fileName = '批量出库单.xlsx';
    const disposition = response.headers['content-disposition'];
    
    if (disposition) {
      const fileNameMatch = disposition.match(/filename=([^;]+)/);
      if (fileNameMatch && fileNameMatch[1]) {
        fileName = decodeURIComponent(fileNameMatch[1]);
      }
    }
    
    // 创建Blob并触发下载
    const blob = new Blob([response.data], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    });
    
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = fileName;
    document.body.appendChild(a);
    a.click();
    
    // 清理
    window.URL.revokeObjectURL(url);
    document.body.removeChild(a);
    
    ElMessage.success(`成功导出 ${selectedRows.value.length} 个出库单`);
    
    // 清空选择
    selectedRows.value = [];
    
  } catch (error) {
    console.error('批量导出失败', error);
    ElMessage.error('批量导出失败，请稍后重试');
  } finally {
    // 无论成功或失败，都清空选择并关闭loading
    selectedRows.value = [];
    batchExportLoading.value = false;
  }
};

// 计算属性
const pendingOutbounds = computed(() => {
  return outboundList.value.filter(item => item.status === 1);
});

const approvedOutbounds = computed(() => {
  return outboundList.value.filter(item => item.status === 2);
});

const completedOutbounds = computed(() => {
  return outboundList.value.filter(item => item.status === 3);
});

// 方法
const loadOutboundList = async () => {
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
    
    const res = await post('/api/auth/outbound/pageList', params);
    if (res && res.records) {
      outboundList.value = res.records.map(outbound => ({
        id: outbound.id || '',
        orderNo: outbound.orderNo || '',
        orderType: outbound.orderType || 1,
        warehouseId: outbound.warehouseId || '',
        warehouseName: outbound.warehouseName || '',
        customerId: outbound.customerId || '',
        customerName: outbound.customerName || '',
        itemCount: outbound.itemCount || 0,
        totalQuantity: outbound.totalQuantity || 0,
        totalAmount: outbound.totalAmount || 0,
        totalAmountUsd: outbound.totalAmountUsd || 0, // 新增：USD总额
        applicantId: outbound.applicantId || '',
        applicantName: outbound.applicantName || '',
        applicantAvatar: outbound.applicantAvatar || '/images/default-avatar.png',
        status: outbound.status || 0,
        isUrgent: outbound.isUrgent || false,
        remark: outbound.remark || '',
        createdAt: outbound.createdAt || new Date().toISOString(),
        updatedAt: outbound.updatedAt || new Date().toISOString()
      }));
      pagination.total = res.total || 0;
      
    
    } else {
      outboundList.value = [];
      pagination.total = 0;
    }
  } catch (error) {
    console.error('加载出库单列表失败:', error);
    ElMessage.error('加载出库单列表失败');
    outboundList.value = [];
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

const loadCustomerList = async () => {
  try {
    const res = await get('/api/auth/customer/listEnable');
    customerList.value = res || [];
  } catch (error) {
    console.error('加载客户列表失败:', error);
    customerList.value = [];
  }
};

const refreshList = () => {
  pagination.current = 1;
  loadOutboundList();
  loadStats(); // 新增：刷新统计信息
};

const handleSearch = () => {
  pagination.current = 1;
  loadOutboundList();
  loadStats(); // 新增：刷新统计信息
};

const handleReset = () => {
  Object.assign(filterForm, {
    orderNo: '',
    orderType: '',
    warehouseId: '',
    customerId: '',
    status: '',
    dateRange: []
  });
  pagination.current = 1;
  loadOutboundList();
  loadStats(); // 新增：重置时更新统计信息
};

const handleExport = () => {
  ElMessage.info('导出功能开发中');
};

const handleSizeChange = (size) => {
  pagination.size = size;
  pagination.current = 1;
  loadOutboundList();
};

const handleCurrentChange = (page) => {
  pagination.current = page;
  loadOutboundList();
};

// 修改：新建出库单处理
// 修改：新建出库单处理
const handleCreate = (orderType) => {
  const routeMap = {
    1: '/index/salesOutboundCreate',      // 销售出库
    2: '/index/productionPickingCreate',  // 生产领料
    // 3: '/index/ckOutboundCreateReturn',    // 退货出库
    // 4: '/index/ckOutboundCreateTransfer',  // 调拨出库
    // 5: '/index/ckOutboundCreateOther'      // 其他出库
  };
  
  const targetRoute = routeMap[orderType] || '/index/ckOutboundCreate';
  if (targetRoute) {
    router.push(targetRoute);
  }
};


const handleView = (outbound) => {
  const path = getRoutePathByType(outbound.orderType, outbound.id);
  router.push(path);
};

// 修改：编辑出库单
const handleEdit = (outbound) => {
  const path = getRoutePathByType(outbound.orderType, outbound.id);
  router.push(path);
};

const handleSubmit = async (outbound) => {
  try {
    await ElMessageBox.confirm(
      `确定要提交出库单"${outbound.orderNo}"吗？`,
      '提交确认',
      { type: 'warning' }
    );
    
    const res = await post('/api/auth/outbound/approveOk', {
      id: outbound.id
    });
    
    if (res) {
      ElMessage.success('提交成功');
      refreshList();
      loadStats(); // 新增：操作成功后更新统计信息
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('提交失败');
    }
  }
};

const handleDelete = async (outbound) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除出库单"${outbound.orderNo}"吗？此操作不可恢复！`,
      '删除确认',
      { 
        type: 'error',
        confirmButtonText: '确定删除',
        cancelButtonText: '取消'
      }
    );
    
    const res = await post('/api/auth/outbound/delete', {
      id: outbound.id
    });
    
    if (res) {
      ElMessage.success('删除成功');
      refreshList();
      loadStats(); // 新增：操作成功后更新统计信息
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败');
    }
  }
};

const handleCancel = async (outbound) => {
  try {
    //有审核后，应该是只有审核过程的才能取消，其他状态的都不能取消
    ElMessage.success('功能还待开发');
    return;
    await ElMessageBox.confirm(
      `确定要取消出库单"${outbound.orderNo}"吗？`,
      '取消确认',
      { type: 'warning' }
    );
    
    const res = await post('/api/auth/outbound/cancel', {
      id: outbound.id
    });
    
    if (res) {
      ElMessage.success('取消成功');
      refreshList();
      loadStats(); // 新增：操作成功后更新统计信息
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消失败');
    }
  }
};

const handleComplete = async (outbound) => {
  try {
    ElMessage.success('功能还待开发');
    return;
    await ElMessageBox.confirm(
      `确定要完成出库单"${outbound.orderNo}"吗？完成后将更新库存数量。`,
      '完成确认',
      { type: 'warning' }
    );
    
    const res = await post('/api/auth/outbound/complete', {
      id: outbound.id
    });
    
    if (res) {
      ElMessage.success('出库完成');
      refreshList();
      loadStats(); // 新增：操作成功后更新统计信息
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('完成失败');
    }
  }
};

const handleDownload = (file) => {
  // 文件下载逻辑
  ElMessage.info(`下载文件: ${file.fileName}`);
};

const formatFileSize = (bytes) => {
  if (!bytes) return '0 B';
  const k = 1024;
  const sizes = ['B', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
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
    4: 'primary',
    5: ''
  };
  return types[orderType] || '';
};

const getStatusText = (status) => {
  const statusObj = statusOptions.find(item => item.value === status);
  return statusObj ? statusObj.label : '未知';
};

const getStatusTagType = (status) => {
  const types = {
    0: 'info',      // 草稿
    1: 'warning',   // 待审核
    2: 'success',   // 已通过
    3: '',          // 已完成
    4: 'danger',    // 已拒绝
    9: 'info'       // 已取消
  };
  return types[status] || '';
};

const handleDownloadTemplate = async (outbound) => {
  try {
    outbound.exportLoading = true;

    const response = await axios.get(
      `/api/auth/outbound/exportOutboundOrderExcel?orderId=${outbound.id}`,
      {
        headers: accessHeader(),
        responseType: 'blob',
      }
    );

    // ===== 1️⃣ 从响应头读取文件名（注意小写） =====
    let fileName = '出库单.xlsx';
    const disposition = response.headers['content-disposition'];

    if (disposition) {
      // attachment;filename=xxxx.xlsx
      const fileNameMatch = disposition.match(/filename=([^;]+)/);
      if (fileNameMatch && fileNameMatch[1]) {
        fileName = decodeURIComponent(fileNameMatch[1]);
      }
    }

    // ===== 2️⃣ 创建 Blob =====
    const blob = new Blob([response.data], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    });

    // ===== 3️⃣ 触发浏览器下载 =====
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = fileName;
    document.body.appendChild(a);
    a.click();

    // ===== 4️⃣ 清理 =====
    window.URL.revokeObjectURL(url);
    document.body.removeChild(a);

    ElMessage.success('导出成功');
  } catch (error) {
    console.error('导出出库单失败', error);
    ElMessage.error('导出失败，请稍后重试');
  } finally {
    outbound.exportLoading = false;
  }
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
  loadOutboundList();
  loadWarehouseList();
  loadCustomerList();
  loadStats(); // 新增：组件挂载时加载统计信息
});
</script>

<style scoped>
.outbound-manage-container {
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

.outbound-list-section {
  margin-top: 20px;
}

.outbound-table {
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

.urgent-tag {
  margin-left: 4px;
}

.amount {
  font-weight: bold;
  color: #E6A23C;
}

.applicant-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.applicant-avatar {
  flex-shrink: 0;
}

.applicant-name {
  font-weight: 500;
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

.batch-summary {
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

.raw-data {
  background: #f5f5f5;
  padding: 16px;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
  font-size: 12px;
  line-height: 1.4;
  max-height: 400px;
  overflow: auto;
  white-space: pre-wrap;
  word-break: break-all;
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
  .outbound-manage-container {
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
  
  .applicant-info {
    flex-direction: column;
    gap: 4px;
    text-align: center;
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
.outbound-table :deep(.el-table__row) {
  transition: all 0.3s;
}

.outbound-table :deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}
/* USD金额样式 */
.amount-usd {
  font-weight: bold;
  color: #67C23A;
}

.price-total-usd {
  font-weight: bold;
  color: #67C23A;
}

/* 响应式设计调整 */
@media (max-width: 768px) {
  .product-summary .el-col {
    margin-bottom: 8px;
  }
}

/* 原有样式保持不变，新增下拉菜单样式 */
.outbound-manage-container {
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

/* 下拉菜单样式调整 */
:deep(.el-dropdown) {
  margin-right: 12px;
}

/* 其他样式保持不变... */
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

.outbound-list-section {
  margin-top: 20px;
}

.outbound-table {
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

.urgent-tag {
  margin-left: 4px;
}

.amount {
  font-weight: bold;
  color: #E6A23C;
}

.amount-usd {
  font-weight: bold;
  color: #67C23A;
}

.applicant-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.applicant-avatar {
  flex-shrink: 0;
}

.applicant-name {
  font-weight: 500;
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

/* 响应式设计 */
@media (max-width: 768px) {
  .outbound-manage-container {
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
  
  .applicant-info {
    flex-direction: column;
    gap: 4px;
    text-align: center;
  }
}

/* 动画效果 */
.outbound-table :deep(.el-table__row) {
  transition: all 0.3s;
}

.outbound-table :deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}

/* 调整下拉菜单样式 */
:deep(.el-dropdown-menu) {
  min-width: 120px !important;
  text-align: center;
}

:deep(.el-dropdown-menu__item) {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 8px 16px;
}

:deep(.el-dropdown-menu__item .el-icon) {
  margin-right: 8px;
}

/* 批量导出按钮样式 */
:deep(.el-button--success) {
  background-color: #67c23a;
  border-color: #67c23a;
}

:deep(.el-button--success:hover) {
  background-color: #5daf34;
  border-color: #5daf34;
}

:deep(.el-button--success.is-disabled) {
  background-color: #b3e19d;
  border-color: #b3e19d;
}

/* 选中行高亮样式 */
:deep(.el-table__row.selected-row) {
  background-color: #f0f9ff !important;
}

:deep(.el-table__row.selected-row:hover) {
  background-color: #e0f2ff !important;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .filter-section .el-form-item {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }
  
  .filter-section .el-button {
    margin-bottom: 8px;
  }
}
</style>