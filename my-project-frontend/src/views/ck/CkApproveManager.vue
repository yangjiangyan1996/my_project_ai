<template>
  <div class="approval-container">
    <!-- 页面标题和操作栏 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">审批管理</h1>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>工作台</el-breadcrumb-item>
          <el-breadcrumb-item>审批管理</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="handleRefresh">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <!-- 标签页 -->
    <el-tabs v-model="activeTab" class="approval-tabs" @tab-click="handleTabChange">
      <el-tab-pane label="我的待办" name="pending">
        <!-- 筛选条件 -->
        <div class="filter-container">
          <el-form :model="filterForm" inline>
            <el-form-item label="业务类型">
              <el-select v-model="filterForm.bizType" placeholder="请选择业务类型" clearable>
                <el-option label="全部" value="" />
                <el-option label="入库单" value="1" />
                <el-option label="出库单" value="2" />
                <el-option label="调拨单" value="3" />
                <el-option label="盘点单" value="4" />
              </el-select>
            </el-form-item>
            <el-form-item label="申请人">
              <el-input v-model="filterForm.applicantName" placeholder="请输入申请人" clearable />
            </el-form-item>
            <el-form-item label="申请时间">
              <el-date-picker
                v-model="filterForm.dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch">查询</el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 待办列表 -->
        <el-table
          :data="pendingList"
          v-loading="loading"
          style="width: 100%"
          empty-text="暂无待办审批"
        >
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column prop="bizNo" label="单据编号" width="180" />
          <el-table-column prop="bizType" label="业务类型" width="120">
            <template #default="scope">
              <el-tag :type="getBizTypeTagType(scope.row.bizType)">
                {{ getBizTypeText(scope.row.bizType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="applicantName" label="申请人" width="120" />
          <el-table-column prop="createdAt" label="申请时间" width="180">
            <template #default="scope">
              {{ formatTime(scope.row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column prop="currentNode" label="当前节点" />
          <el-table-column prop="urgency" label="紧急程度" width="100">
            <template #default="scope">
              <el-tag :type="getUrgencyTagType(scope.row.urgency)" effect="plain">
                {{ getUrgencyText(scope.row.urgency) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="scope">
              <el-button type="primary" link @click="handleViewDetail(scope.row)">
                查看详情
              </el-button>
              <el-button type="success" link @click="handleApprove(scope.row)">
                审批
              </el-button>
              <el-button type="danger" link @click="handleReject(scope.row)">
                驳回
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-container">
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
      </el-tab-pane>

      <el-tab-pane label="审批历史" name="history">
        <!-- 筛选条件 -->
        <div class="filter-container">
          <el-form :model="historyFilterForm" inline>
            <el-form-item label="审批状态">
              <el-select v-model="historyFilterForm.approvalStatus" placeholder="请选择审批状态" clearable>
                <el-option label="全部" value="" />
                <el-option label="已通过" value="1" />
                <el-option label="已驳回" value="2" />
                <el-option label="已撤回" value="3" />
              </el-select>
            </el-form-item>
            <el-form-item label="业务类型">
              <el-select v-model="historyFilterForm.bizType" placeholder="请选择业务类型" clearable>
                <el-option label="全部" value="" />
                <el-option label="入库单" value="1" />
                <el-option label="出库单" value="2" />
                <el-option label="调拨单" value="3" />
                <el-option label="盘点单" value="4" />
              </el-select>
            </el-form-item>
            <el-form-item label="审批时间">
              <el-date-picker
                v-model="historyFilterForm.dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleHistorySearch">查询</el-button>
              <el-button @click="handleHistoryReset">重置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 审批历史列表 -->
        <el-table
          :data="historyList"
          v-loading="historyLoading"
          style="width: 100%"
          empty-text="暂无审批历史"
        >
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column prop="bizNo" label="单据编号" width="180" />
          <el-table-column prop="bizType" label="业务类型" width="120">
            <template #default="scope">
              <el-tag :type="getBizTypeTagType(scope.row.bizType)">
                {{ getBizTypeText(scope.row.bizType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="applicantName" label="申请人" width="120" />
          <el-table-column prop="createdAt" label="申请时间" width="180">
            <template #default="scope">
              {{ formatTime(scope.row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column prop="approvalTime" label="审批时间" width="180">
            <template #default="scope">
              {{ formatTime(scope.row.approvalTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="approvalStatus" label="审批状态" width="100">
            <template #default="scope">
              <el-tag :type="getStatusTagType(scope.row.approvalStatus)">
                {{ getStatusText(scope.row.approvalStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="approvalOpinion" label="审批意见" show-overflow-tooltip />
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="scope">
              <el-button type="primary" link @click="handleViewHistoryDetail(scope.row)">
                查看详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="historyPagination.current"
            v-model:page-size="historyPagination.size"
            :total="historyPagination.total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleHistorySizeChange"
            @current-change="handleHistoryCurrentChange"
          />
        </div>
      </el-tab-pane>

      <el-tab-pane label="流程配置" name="config">
        <div class="config-container">
          <div class="config-header">
            <h3>审批流程配置</h3>
            <el-button type="primary" @click="handleAddConfig">
              <el-icon><Plus /></el-icon>
              新增配置
            </el-button>
          </div>

          <!-- 流程配置列表 -->
          <el-table :data="configList" style="width: 100%" empty-text="暂无流程配置">
            <el-table-column type="index" label="序号" width="60" align="center" />
            <el-table-column prop="bizType" label="业务类型" width="120">
              <template #default="scope">
                {{ getBizTypeText(scope.row.bizType) }}
              </template>
            </el-table-column>
            <el-table-column prop="processName" label="流程名称" />
            <el-table-column prop="nodeCount" label="节点数量" width="100" align="center" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-switch
                  v-model="scope.row.status"
                  :active-value="1"
                  :inactive-value="0"
                  @change="handleConfigStatusChange(scope.row)"
                />
              </template>
            </el-table-column>
            <el-table-column prop="updatedAt" label="更新时间" width="180">
              <template #default="scope">
                {{ formatTime(scope.row.updatedAt) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="scope">
                <el-button type="primary" link @click="handleEditConfig(scope.row)">
                  编辑
                </el-button>
                <el-button type="info" link @click="handleViewConfig(scope.row)">
                  查看详情
                </el-button>
                <el-button type="danger" link @click="handleDeleteConfig(scope.row)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 审批对话框 -->
    <el-dialog
      v-model="approvalDialogVisible"
      :title="`审批 - ${currentTask?.bizNo}`"
      width="600px"
    >
      <el-form :model="approvalForm" label-width="80px">
        <el-form-item label="审批意见">
          <el-input
            v-model="approvalForm.opinion"
            type="textarea"
            :rows="4"
            placeholder="请输入审批意见"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approvalDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmApprove">确认通过</el-button>
      </template>
    </el-dialog>

    <!-- 驳回对话框 -->
    <el-dialog
      v-model="rejectDialogVisible"
      :title="`驳回 - ${currentTask?.bizNo}`"
      width="600px"
    >
      <el-form :model="rejectForm" label-width="80px">
        <el-form-item label="驳回原因" required>
          <el-input
            v-model="rejectForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入驳回原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="handleConfirmReject">确认驳回</el-button>
      </template>
    </el-dialog>
  </div>

  <ApprovalConfigDialog ref="configDialogRef" @config-updated="loadConfigList" />

</template>

<script setup>
import { ref, onMounted, reactive } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Refresh, Plus } from '@element-plus/icons-vue';
import { post, get } from '@/net';
import ApprovalConfigDialog from '@/components/ApprovalConfigDialog.vue';


// 响应式数据
const activeTab = ref('pending');
const loading = ref(false);
const historyLoading = ref(false);
const configDialogRef = ref(null);


// 待办审批相关
const pendingList = ref([]);
const filterForm = reactive({
  bizType: '',
  applicantName: '',
  dateRange: []
});
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
});

// 审批历史相关
const historyList = ref([]);
const historyFilterForm = reactive({
  approvalStatus: '',
  bizType: '',
  dateRange: []
});
const historyPagination = reactive({
  current: 1,
  size: 10,
  total: 0
});

// 流程配置相关
const configList = ref([]);

// 对话框相关
const approvalDialogVisible = ref(false);
const rejectDialogVisible = ref(false);
const currentTask = ref(null);
const approvalForm = reactive({
  opinion: ''
});
const rejectForm = reactive({
  reason: ''
});

// 方法
const handleTabChange = (tab) => {
  if (tab.paneName === 'pending') {
    loadPendingList();
  } else if (tab.paneName === 'history') {
    loadHistoryList();
  } else if (tab.paneName === 'config') {
    loadConfigList();
  }
};

const handleSearch = () => {
  pagination.current = 1;
  loadPendingList();
};

const handleReset = () => {
  Object.assign(filterForm, {
    bizType: '',
    applicantName: '',
    dateRange: []
  });
  pagination.current = 1;
  loadPendingList();
};

const handleHistorySearch = () => {
  historyPagination.current = 1;
  loadHistoryList();
};

const handleHistoryReset = () => {
  Object.assign(historyFilterForm, {
    approvalStatus: '',
    bizType: '',
    dateRange: []
  });
  historyPagination.current = 1;
  loadHistoryList();
};

const handleSizeChange = (size) => {
  pagination.size = size;
  pagination.current = 1;
  loadPendingList();
};

const handleCurrentChange = (current) => {
  pagination.current = current;
  loadPendingList();
};

const handleHistorySizeChange = (size) => {
  historyPagination.size = size;
  historyPagination.current = 1;
  loadHistoryList();
};

const handleHistoryCurrentChange = (current) => {
  historyPagination.current = current;
  loadHistoryList();
};

const handleRefresh = () => {
  if (activeTab.value === 'pending') {
    loadPendingList();
  } else if (activeTab.value === 'history') {
    loadHistoryList();
  } else if (activeTab.value === 'config') {
    loadConfigList();
  }
};

const handleViewDetail = (row) => {
  // 查看详情逻辑
  ElMessage.info(`查看详情：${row.bizNo}`);
};

const handleApprove = (row) => {
  currentTask.value = row;
  approvalForm.opinion = '';
  approvalDialogVisible.value = true;
};

const handleReject = (row) => {
  currentTask.value = row;
  rejectForm.reason = '';
  rejectDialogVisible.value = true;
};

const handleConfirmApprove = async () => {
  if (!currentTask.value) return;

  try {
    await post('/api/auth/approval/approve', {
      taskId: currentTask.value.id,
      opinion: approvalForm.opinion
    });
    
    ElMessage.success('审批通过');
    approvalDialogVisible.value = false;
    loadPendingList();
  } catch (error) {
    ElMessage.error('审批操作失败');
  }
};

const handleConfirmReject = async () => {
  if (!currentTask.value) return;

  if (!rejectForm.reason) {
    ElMessage.warning('请输入驳回原因');
    return;
  }

  try {
    await post('/api/auth/approval/reject', {
      taskId: currentTask.value.id,
      reason: rejectForm.reason
    });
    
    ElMessage.success('已驳回');
    rejectDialogVisible.value = false;
    loadPendingList();
  } catch (error) {
    ElMessage.error('驳回操作失败');
  }
};

const handleViewHistoryDetail = (row) => {
  // 查看历史详情逻辑
  ElMessage.info(`查看历史详情：${row.bizNo}`);
};

const handleAddConfig = () => {
    configDialogRef.value.handleAddConfig();

};

const handleEditConfig = (row) => {
    configDialogRef.value.handleEditConfig(row);
};

const handleViewConfig = (row) => {
  ElMessage.info(`查看配置详情：${row.processName}`);
};

const handleDeleteConfig = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除流程配置"${row.processName}"吗？`,
      '删除确认',
      {
        type: 'warning'
      }
    );
    
    await post('/api/auth/approval/deleteConfig', { id: row.id });
    ElMessage.success('删除成功');
    loadConfigList();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败');
    }
  }
};

const handleConfigStatusChange = async (row) => {
  try {
    await post('/api/auth/approval/updateConfigStatus', {
      id: row.id,
      status: row.status
    });
    ElMessage.success('状态更新成功');
  } catch (error) {
    // 回滚状态
    row.status = row.status === 1 ? 0 : 1;
    ElMessage.error('状态更新失败');
  }
};

// 工具方法
const getBizTypeText = (bizType) => {
  const types = {
    1: '入库单',
    2: '出库单',
    3: '调拨单',
    4: '盘点单'
  };
  return types[bizType] || '未知类型';
};

const getBizTypeTagType = (bizType) => {
  const types = {
    1: 'primary',
    2: 'success',
    3: 'warning',
    4: 'info'
  };
  return types[bizType] || 'info';
};

const getUrgencyText = (urgency) => {
  const levels = {
    1: '普通',
    2: '紧急',
    3: '特急'
  };
  return levels[urgency] || '普通';
};

const getUrgencyTagType = (urgency) => {
  const types = {
    1: 'info',
    2: 'warning',
    3: 'danger'
  };
  return types[urgency] || 'info';
};

const getStatusText = (status) => {
  const statusMap = {
    1: '已通过',
    2: '已驳回',
    3: '已撤回'
  };
  return statusMap[status] || '未知状态';
};

const getStatusTagType = (status) => {
  const types = {
    1: 'success',
    2: 'danger',
    3: 'info'
  };
  return types[status] || 'info';
};

const formatTime = (timeString) => {
  if (!timeString) return '';
  const date = new Date(timeString);
  return `${date.getFullYear()}-${padZero(date.getMonth() + 1)}-${padZero(date.getDate())} ${padZero(date.getHours())}:${padZero(date.getMinutes())}`;
};

const padZero = (num) => {
  return num < 10 ? `0${num}` : num;
};

// API 调用
const loadPendingList = async () => {
  loading.value = true;
  try {
    const params = {
      page: pagination.current,
      size: pagination.size,
      ...filterForm
    };
    
    if (filterForm.dateRange && filterForm.dateRange.length === 2) {
      params.startDate = filterForm.dateRange[0];
      params.endDate = filterForm.dateRange[1];
    }
    
    const res = await post('/api/auth/approval/pendingTasks', params);
    pendingList.value = res.records || [];
    pagination.total = res.total || 0;
  } catch (error) {
    console.error('加载待办列表失败:', error);
    ElMessage.error('加载待办列表失败');
  } finally {
    loading.value = false;
  }
};

const loadHistoryList = async () => {
  historyLoading.value = true;
  try {
    const params = {
      page: historyPagination.current,
      size: historyPagination.size,
      ...historyFilterForm
    };
    
    if (historyFilterForm.dateRange && historyFilterForm.dateRange.length === 2) {
      params.startDate = historyFilterForm.dateRange[0];
      params.endDate = historyFilterForm.dateRange[1];
    }
    
    const res = await post('/api/auth/approval/history', params);
    historyList.value = res.records || [];
    historyPagination.total = res.total || 0;
  } catch (error) {
    console.error('加载审批历史失败:', error);
    ElMessage.error('加载审批历史失败');
  } finally {
    historyLoading.value = false;
  }
};

const loadConfigList = async () => {
  try {
    const res = await get('/api/auth/approval/configList');
    configList.value = res || [];
  } catch (error) {
    console.error('加载流程配置失败:', error);
    ElMessage.error('加载流程配置失败');
  }
};

onMounted(() => {
  loadPendingList();
});
</script>

<style scoped>
.approval-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.header-left .page-title {
  margin: 0 0 8px 0;
  font-size: 20px;
  font-weight: bold;
  color: #303133;
}

.approval-tabs {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.filter-container {
  margin-bottom: 20px;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 6px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.config-container {
  padding: 20px 0;
}

.config-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e8e8e8;
}

.config-header h3 {
  margin: 0;
  color: #303133;
}

:deep(.el-tabs__header) {
  margin-bottom: 0;
}

:deep(.el-table) {
  border-radius: 6px;
  overflow: hidden;
}

:deep(.el-table .el-table__cell) {
  padding: 12px 0;
}

:deep(.el-button--link) {
  padding: 4px 8px;
}
</style>