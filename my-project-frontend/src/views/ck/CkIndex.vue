<template>
  <div class="dashboard-container">
    <!-- 顶部导航栏 -->
    <div class="nav-header">
      <div class="nav-left">
        <h1 class="logo">出入库管理系统</h1>
        <el-menu 
          mode="horizontal"
          background-color="#fff"
          text-color="#2c3e50"
          active-text-color="#409EFF"
          class="nav-menu"
          :default-active="activeNav"
        >
          <!-- 工作台 -->
          <el-menu-item index="dashboard" @click="changeDisplayMode('dashboard')">工作台</el-menu-item>
          
          <!-- 库存管理 TODO yang 待开发-->
          <el-sub-menu index="inventory">
            <template #title>库存管理</template>
            <el-menu-item index="inventory-list" @click="changeDisplayMode('inventory-list')">库存查询</el-menu-item>
          </el-sub-menu>
          
          <!-- 业务管理 -->
          <el-sub-menu index="business">
            <template #title>出入库管理</template>
            <el-menu-item index="inbound" @click="changeDisplayMode('inbound')">入库管理</el-menu-item>
            <el-menu-item index="outbound" @click="changeDisplayMode('outbound')">出库管理</el-menu-item>
          </el-sub-menu>
          
          <!-- 基础数据 -->
          <el-sub-menu index="base">
            <template #title>基础数据</template>
            <el-menu-item index="supplier" @click="changeDisplayMode('supplier')">供应商管理</el-menu-item>
            <el-menu-item index="customer" @click="changeDisplayMode('customer')">客户管理</el-menu-item>
            <el-menu-item index="sku" @click="changeDisplayMode('sku')">SKU管理</el-menu-item>
            <el-menu-item index="ckUnit" @click="changeDisplayMode('ckUnit')">单位管理</el-menu-item>
            <el-menu-item index="warehouse" @click="changeDisplayMode('warehouse')">仓库管理</el-menu-item>
            <el-menu-item index="shelf" @click="changeDisplayMode('shelf')">货架管理</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="product">
            <template #title>产品管理</template>
            <el-menu-item index="product" @click="changeDisplayMode('product')">产品管理</el-menu-item>
          </el-sub-menu>
          
          <!-- 审批管理 -->
          <el-sub-menu index="approval">
            <template #title>审批管理</template>
            <el-menu-item index="approval-task" @click="changeDisplayMode('approval-task')">我的待办</el-menu-item>
            <el-menu-item index="approval-history" @click="changeDisplayMode('approval-history')">审批历史</el-menu-item>
            <el-menu-item index="approval-flow" @click="changeDisplayMode('approval-flow')">流程配置</el-menu-item>
          </el-sub-menu>
        </el-menu>
      </div>
      
      <div class="nav-right">
        <!-- 通知铃铛 -->
        <el-popover
          placement="bottom-end"
          trigger="click"
          width="350"
          v-model:visible="messageVisible"
        >
          <template #reference>
            <div class="message-bell" @click="handleMessageClick">
              <el-badge :value="unreadCount" :max="99" class="badge">
                <el-icon :size="20"><Bell /></el-icon>
              </el-badge>
            </div>
          </template>
          
          <el-tabs v-model="activeMessageTab" class="message-tabs">
            <div class="mark-all-read-container">
              <el-button 
                type="text" 
                size="small" 
                @click="markAllAsRead"
                :disabled="unreadCount === 0"
              >
                <el-icon><CircleCheck /></el-icon>
                一键已读
              </el-button>
            </div>
            <el-tab-pane label="待办审批" name="approval">
              <div class="message-list">
                <div 
                  v-for="item in approvalMessages" 
                  :key="item.id" 
                  class="message-item" 
                  @click="handleApprovalClick(item)"
                  :class="{ 'unread-message': item.isRead === 0 }"
                >
                  <div class="message-unread-dot" v-if="item.isRead === 0"></div>
                  <div class="message-content">
                    <div class="message-header">
                      <span class="message-time">{{ formatTime(item.createdAt) }}</span>
                    </div>
                    <div class="message-text">
                      <span class="sender-name">{{ item.applicantName }}</span>
                      <span class="message-content-text">提交了{{ getBizTypeText(item.bizType) }}</span>
                      <span class="related-words">"{{ item.bizNo }}"</span>
                    </div>
                  </div>
                </div>
              
                <div v-if="approvalMessages.length === 0" class="no-message">
                  <el-empty description="暂无待办审批" :image-size="80" />
                </div>
              </div>
            </el-tab-pane>
            
            <el-tab-pane label="系统通知" name="system">
              <div class="message-list">
                <div 
                  v-for="item in systemMessages" 
                  :key="item.id" 
                  class="message-item"
                  :class="{ 'unread-message': item.isRead === 0 }"
                >
                  <div class="message-unread-dot" v-if="item.isRead === 0"></div>
                  <div class="message-content">
                    <div class="message-header">
                      <span class="message-time">{{ formatTime(item.createdAt) }}</span>
                    </div>
                    <div class="message-text">
                      <span class="message-content-text">{{ item.content }}</span>
                    </div>
                  </div>
                </div>
                <div v-if="systemMessages.length === 0" class="no-message">
                  暂无系统通知
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </el-popover>

        <!-- 用户头像下拉菜单 -->
        <el-dropdown class="avatar-dropdown" trigger="click" @command="handleUserCommand">
          <div class="avatar-wrapper">
            <el-avatar :src="userInfo.avatarUrl || '/default-avatar.png'" />
            <span class="user-name">{{ userInfo.realName || userInfo.username }}</span>
            <el-icon><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">
                <el-icon><User /></el-icon>
                个人中心
              </el-dropdown-item>
              <el-dropdown-item command="settings">
                <el-icon><Setting /></el-icon>
                系统设置
              </el-dropdown-item>
              <el-dropdown-item divided command="logout">
                <el-icon><SwitchButton /></el-icon>
                退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <!-- 主内容区域 - 根据 displayMode 显示不同组件 -->
    <div class="main-content" v-if="displayMode === 'dashboard'">
      <!-- 数据概览 -->
       <div class="chart-stats">
          <div class="stat-item">
            <span class="stat-label">产品总数</span>
            <span class="stat-value">{{ indexPageStats.totalProducts || 0 }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">仓库数量</span>
            <span class="stat-value">{{ indexPageStats.totalWarehouses || 0 }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">今日入库</span>
            <span class="stat-value" style="color: #67C23A;">{{ indexPageStats.todayInbound || 0 }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">今日出库</span>
            <span class="stat-value" style="color: #F56C6C;">{{ indexPageStats.todayOutbound || 0 }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">入库待办</span>
            <span class="stat-value" style="color: #E6A23C;">{{ indexPageStats.todoInBoundApproval || 0 }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">出库待办</span>
            <span class="stat-value" style="color: #E6A23C;">{{ indexPageStats.todoOutBoundApproval || 0 }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">低库存</span>
            <span class="stat-value urgent">{{ indexPageStats.lowStock || 0 }}</span>
          </div>
        </div>

      <!-- 快捷操作 -->
      <el-card class="quick-actions-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span class="card-title">快捷操作</span>
          </div>
        </template>
        <div class="quick-actions">
          <el-button 
            type="primary" 
            size="large" 
            @click="handleQuickAction('inbound')"
            class="quick-action-btn"
          >
            <el-icon><Plus /></el-icon>
            新建入库单
          </el-button>
          <el-button 
            type="success" 
            size="large" 
            @click="handleQuickAction('outbound')"
            class="quick-action-btn"
          >
            <el-icon><Minus /></el-icon>
            新建出库单
          </el-button>
          <el-button 
            type="warning" 
            size="large" 
            @click="handleQuickAction('transfer')"
            class="quick-action-btn"
          >
            <el-icon><Refresh /></el-icon>
            新建调拨单
          </el-button>
          <el-button 
            type="info" 
            size="large" 
            @click="handleQuickAction('stock-take')"
            class="quick-action-btn"
          >
            <el-icon><DocumentChecked /></el-icon>
            库存盘点
          </el-button>
          <el-button 
            type="danger" 
            size="large" 
            @click="handleQuickAction('approval')"
            class="quick-action-btn"
          >
            <el-icon><Finished /></el-icon>
            审批待办
          </el-button>
        </div>
      </el-card>

      <!-- 库存预警监控 - 整行显示 -->
      <el-card class="low-stock-alert-card" shadow="never">
        <template #header>
          <div class="card-header">
            <div class="card-title-section">
              <span class="card-title">库存预警监控</span>
              <span class="card-subtitle">实时监控库存状态，及时预警</span>
            </div>
            <div class="chart-actions">
              <el-button type="primary" link @click="refreshLowStockChart">
                <el-icon><Refresh /></el-icon>
                刷新数据
              </el-button>
              <!-- <el-button type="primary" link @click="exportLowStockData">
                <el-icon><Download /></el-icon>
                导出数据
              </el-button> -->
              <el-select 
                v-model="alertFilter" 
                placeholder="预警级别" 
                style="width: 120px; margin-left: 10px;"
                @change="handleAlertFilterChange"
              >
                <el-option label="全部" value="all" />
                <el-option label="紧急预警" value="urgent" />
                <el-option label="一般预警" value="warning" />
                <el-option label="库存正常" value="normal" />
              </el-select>
              <el-select 
                v-model="selectedWarehouse" 
                placeholder="选择仓库" 
                style="width: 150px; margin-left: 10px;"
                @change="handleWarehouseChange"
              >
                <el-option
                  v-for="warehouse in warehouseList"
                  :key="warehouse.id"
                  :label="warehouse.name"
                  :value="warehouse.id"
                />
              </el-select>
            </div>
          </div>
        </template>
        <div class="chart-container-full">
          <div v-if="loading" class="chart-loading">
            <el-empty description="加载中..." :image-size="80" />
          </div>
          <div v-else-if="inventoryAlerts.length === 0" class="chart-empty">
            <el-empty description="暂无库存预警数据" :image-size="80" />
          </div>
          <div 
            v-show="!loading && inventoryAlerts.length > 0" 
            ref="lowStockChart" 
            class="chart-wrapper"
          ></div>
        </div>
        <div class="chart-stats">
          <div class="stat-item">
            <span class="stat-label">监控产品总数</span>
            <span class="stat-value">{{ chartStats.totalProducts || 0 }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">紧急预警</span>
            <span class="stat-value urgent">{{ chartStats.urgentAlerts || 0 }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">一般预警</span>
            <span class="stat-value warning">{{ chartStats.warningAlerts || 0 }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">库存正常</span>
            <span class="stat-value normal">{{ chartStats.normalProducts || 0 }}</span>
          </div>
        </div>
      </el-card>

      <!-- 其他图表区域 -->
      <el-row :gutter="20" class="chart-section">
        <el-col :xs="24" :lg="12">
          <el-card class="chart-card" shadow="never">
            <template #header>
              <div class="card-header">
                <span class="card-title">出入库统计</span>
              </div>
            </template>
            <div class="chart-container">
              <div class="chart-placeholder">
                <el-empty description="出入库统计图表" :image-size="100" />
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :xs="24" :lg="12">
          <el-card class="chart-card" shadow="never">
            <template #header>
              <div class="card-header">
                <span class="card-title">库存周转分析</span>
              </div>
            </template>
            <div class="chart-container">
              <div class="chart-placeholder">
                <el-empty description="库存周转分析图表" :image-size="100" />
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 最近操作记录 -->
      <el-card class="recent-actions-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span class="card-title">最近操作记录</span>
            <el-button type="text" @click="viewAllActions">查看全部</el-button>
          </div>
        </template>
        <el-table 
          :data="recentActions" 
          style="width: 100%"
          empty-text="暂无操作记录"
        >
          <el-table-column prop="operationTime" label="操作时间" width="180">
            <template #default="scope">
              {{ formatTime(scope.row.operationTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="module" label="操作模块" width="120" />
          <el-table-column prop="operation" label="操作类型" width="120" />
          <el-table-column prop="description" label="操作描述" />
          <el-table-column prop="operator" label="操作人" width="120" />
        </el-table>
      </el-card>
    </div>

    <!-- 其他页面的条件渲染 -->
    <CkWarehouseManage v-if="displayMode === 'warehouse'" />
    <CkApproveManager v-if="displayMode === 'approveManager'" />
    <CkSupplierManage v-if="displayMode === 'supplier'" />
    <CkCustomerManage v-if="displayMode === 'customer'" />
    <CkCkUnitManage v-if="displayMode === 'ckUnit'" />
    <CkSkuManage v-if="displayMode === 'sku'" />
    <CkProductManage v-if="displayMode === 'product'" />
    <CkInboundManage v-if="displayMode === 'inbound'" />
    <CkOutboundManage v-if="displayMode === 'outbound'" />
    <CkTransferManage v-if="displayMode === 'transfer'" />
    <CkInventoryList v-if="displayMode === 'inventory-list'" />
    <CkInventoryTransaction v-if="displayMode === 'inventory-transaction'" />
    <CkStockTake v-if="displayMode === 'stock-take'" />
    <CkShelfManage v-if="displayMode === 'shelf'" />
    <CkUser v-if="displayMode === 'profile'" />

    <!-- 添加快捷操作对应的组件 -->
    <CkInboundCreate v-if="displayMode === 'inbound-create'" />
    <CkOutboundCreate v-if="displayMode === 'outbound-create'" />
    <CkTransferCreate v-if="displayMode === 'transfer-create'" />
    <CkStockTakeCreate v-if="displayMode === 'stock-take-create'" />
    <CkApprovalTask v-if="displayMode === 'approval-task'" />

  </div>
</template>

<script setup>
import { ref, onMounted, computed, nextTick, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { post, get } from '@/net';
import { logout } from '@/net';
import * as echarts from 'echarts';
import { 
  Bell, CircleCheck, ArrowDown, User, Setting, SwitchButton,
  Box, OfficeBuilding, TrendCharts, Clock, Warning,
  Plus, Minus, Refresh, DocumentChecked, Finished, Download
} from '@element-plus/icons-vue';

// 导入组件
import CkWarehouseManage from '@/views/ck/CkWarehouseManage.vue';
import CkApproveManager from '@/views/ck/CkApproveManager.vue';
import CkSupplierManage from '@/views/ck/CkSupplierManage.vue';
import CkInboundManage from '@/views/ck/CkInboundManage.vue';
import CkProductManage from '@/views/ck/CkProductManage.vue';
import CkCustomerManage from '@/views/ck/CkCustomerManage.vue';
import CkOutboundManage from '@/views/ck/CkOutboundManage.vue';
import CkTransferManage from '@/views/ck/CkTransferManage.vue';
import CkInventoryList from '@/views/ck/CkInventoryList.vue';
import CkStockTake from '@/views/ck/CkStockTake.vue';
import CkInventoryTransaction from '@/views/ck/CkInventoryTransaction.vue';
import CkShelfManage from '@/views/ck/CkShelfManage.vue';
import CkUser from '@/views/ck/CkUser.vue';
import CkSkuManage from '@/views/ck/CkSkuManage.vue';
import CkCkUnitManage from '@/views/ck/CkUnitManage.vue';

// 导入快捷操作组件
import CkInboundCreate from '@/views/ck/CkInboundCreate.vue';
import CkOutboundCreate from '@/views/ck/CkOutboundCreate.vue';
import CkTransferCreate from '@/views/ck/CkTransferCreate.vue';
import CkStockTakeCreate from '@/views/ck/CkStockTakeCreate.vue';
import CkApprovalTask from '@/views/ck/CkApprovalTask.vue';

const router = useRouter();
const route = useRoute();

// 响应式数据
const activeNav = ref('dashboard');
const messageVisible = ref(false);
const activeMessageTab = ref('approval');
const unreadCount = ref(0);
const displayMode = ref('dashboard');
const lowStockChart = ref(null);
const alertFilter = ref('all');
const selectedWarehouse = ref('');
const warehouseList = ref([]);
const loading = ref(false);

// 库存预警数据
const inventoryAlerts = ref([]);

// 图表统计信息
const chartStats = ref({
  totalProducts: 0,
  urgentAlerts: 0,
  warningAlerts: 0,
  normalProducts: 0
});

// 用户信息
const userInfo = ref({
  id: null,
  username: '',
  realName: '',
  avatarUrl: '',
  role: ''
});

// 首页统计数据
const indexPageStats = ref({
  totalProducts: 0,
  totalWarehouses: 0,
  todayInbound: 0,
  todayOutbound: 0,
  todoInBoundApproval: 0,
  todoOutBoundApproval: 0,
  lowStock: 0
});

// 概览数据
const overviewData = ref({
  totalProducts: 0,
  totalWarehouses: 0,
  todayInbound: 0,
  todayOutbound: 0,
  pendingApprovals: 0,
  lowStockItems: 0
});

// 消息数据
const approvalMessages = ref([]);
const systemMessages = ref([]);

// 最近操作记录
const recentActions = ref([]);

// 图表实例
let chartInstance = null;

// 计算属性
const userInitial = computed(() => {
  return userInfo.value.realName ? userInfo.value.realName.charAt(0) : '用户';
});

// 监听路由变化，根据路由参数切换显示模式
// watch(
//   () => route.query.mode,
//   (newMode) => {
//     if (newMode) {
//       changeDisplayMode(newMode);
//     }
//   },
//   { immediate: true }
// );

// watch(displayMode, async (newMode) => {
//   if (newMode === 'dashboard') {
//     // 切换到工作台时，重新加载图表数据
//     await nextTick();
//     loadInventoryAlerts();
//   }
// });
watch(
  [() => route.query.mode, displayMode],
  async ([newQueryMode, newDisplayMode], [oldQueryMode, oldDisplayMode]) => {
    // 处理路由参数变化
    if (newQueryMode && newQueryMode !== oldQueryMode) {
      changeDisplayMode(newQueryMode);
    }
    
    // 处理显示模式变化
    if (newDisplayMode !== oldDisplayMode && newDisplayMode === 'dashboard') {
      // 切换到工作台时，重新加载图表数据
      await nextTick();
      loadInventoryAlerts();
    }
  },
  { immediate: true }
);

// 核心方法：切换显示模式
function changeDisplayMode(mode) {
  console.log('切换显示模式:', mode);
  displayMode.value = mode;
  activeNav.value = mode; // 更新导航激活状态
  
  // 更新URL参数，但不触发页面跳转
  router.replace({
    path: '/', // 使用当前路径
    query: { ...route.query, mode }
  });
}

// 快捷操作处理方法 - 使用模式切换
const handleQuickAction = (action) => {
  console.log('快捷操作:', action);
  
  const modeMap = {
    'inbound': 'inbound-create',
    'outbound': 'outbound-create', 
    'transfer': 'transfer-create',
    'stock-take': 'stock-take-create',
    'approval': 'approval-task'
  };
  
  const targetMode = modeMap[action];
  if (targetMode) {
    changeDisplayMode(targetMode);
  } else {
    ElMessage.warning('该功能暂未开放');
  }
};

// 加载首页统计数据
const loadIndexPageStats = async () => {
  try {
    const res = await get('/api/auth/inventory/countsOfIndexPage');
    if (res) {
      indexPageStats.value = {
        totalProducts: res.totalProducts || 0,
        totalWarehouses: res.totalWarehouses || 0,
        todayInbound: res.todayInbound || 0,
        todayOutbound: res.todayOutbound || 0,
        todoInBoundApproval: res.todoInBoundApproval || 0,
        todoOutBoundApproval: res.todoOutBoundApproval || 0,
        lowStock: res.lowStock || 0
      };
      
      // 同时更新概览数据
      overviewData.value.totalProducts = res.totalProducts || 0;
      overviewData.value.totalWarehouses = res.totalWarehouses || 0;
      overviewData.value.todayInbound = res.todayInbound || 0;
      overviewData.value.todayOutbound = res.todayOutbound || 0;
      overviewData.value.pendingApprovals = (res.todoInBoundApproval || 0) + (res.todoOutBoundApproval || 0);
      overviewData.value.lowStockItems = res.lowStock || 0;
    }
  } catch (error) {
    console.error('加载首页统计数据失败:', error);
  }
};

// 加载库存预警数据
const loadInventoryAlerts = async () => {
  try {
    loading.value = true;
    const req = {
      warehouseId: selectedWarehouse.value || undefined,
      alertLevel: alertFilter.value === 'all' ? undefined : alertFilter.value, // 添加预警级别参数
      page: 1,
      size: 100
    };

    // 如果选中的是"全部仓库"，可以移除 warehouseId 字段
    if (selectedWarehouse.value === '') {
      delete req.warehouseId;
    }

     // 如果是"全部"预警级别，移除 alertLevel 字段
    if (alertFilter.value === 'all') {
      delete req.alertLevel;
    }
    
    const res = await post('/api/auth/inventory/alerts', req);
    console.log('库存预警数据:', res);
    if (res && Array.isArray(res)) {
      inventoryAlerts.value = res;
      
      // 如果数据不为空，初始化图表
      if (inventoryAlerts.value.length > 0) {
        console.log('初始化图表数据:', inventoryAlerts.value);
        // 等待DOM更新后初始化图表
        await nextTick();
        initLowStockChart();
      }
      
      // 加载统计信息
      loadAlertStats();
    } else {
      console.warn('库存预警数据格式不正确:', res);
      inventoryAlerts.value = [];
    }
  } catch (error) {
    console.error('加载库存预警数据失败:', error);
    inventoryAlerts.value = [];
  } finally {
    loading.value = false;
  }
};

// 加载预警统计信息
const loadAlertStats = async () => {
  try {
    const res = await get('/api/auth/inventory/alertStats');
    if (res) {
      chartStats.value = res;
      overviewData.value.lowStockItems = (res.urgentAlerts || 0) + (res.warningAlerts || 0);
    }
  } catch (error) {
    console.error('加载预警统计信息失败:', error);
  }
};

// 库存预警图表相关方法
const initLowStockChart = () => {
  // 检查必要的条件
  if (!lowStockChart.value) {
    console.warn('图表容器未找到');
    return;
  }
  
  if (!inventoryAlerts.value || inventoryAlerts.value.length === 0) {
    console.warn('没有库存预警数据可显示');
    return;
  }

  try {
    // 销毁之前的图表实例
    if (chartInstance) {
      chartInstance.dispose();
      chartInstance = null;
    }
    
    // 强制设置容器尺寸
    lowStockChart.value.style.width = '100%';
    lowStockChart.value.style.height = '500px';
    
    // 使用 setTimeout 确保 DOM 已经完成渲染
    setTimeout(() => {
      chartInstance = echarts.init(lowStockChart.value);
      
      const productNames = inventoryAlerts.value.map(item => item.productName);
      const currentStocks = inventoryAlerts.value.map(item => Number(item.currentStock));
      const warningThresholds = inventoryAlerts.value.map(item => Number(item.warningThreshold));
      const urgentThresholds = inventoryAlerts.value.map(item => Number(item.urgentThreshold));
      
      const option = {
        title: {
          text: '库存预警监控',
          left: 'center',
          textStyle: {
            fontSize: 18,
            fontWeight: 'bold'
          }
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          },
          formatter: function(params) {
            const productIndex = params[0].dataIndex;
            const product = inventoryAlerts.value[productIndex];
            let result = `<div style="font-weight: bold; margin-bottom: 8px;">${product.productName}-${product.spec}-${product.color}</div>`;
            
            params.forEach(param => {
              const value = param.value;
              let status = '';
              let color = '#333';
              let icon = '';
              
              if (param.seriesName === '当前库存') {
                if (value <= product.urgentThreshold) {
                  status = '🔴 紧急缺货';
                  color = '#F56C6C';
                  icon = '🔴';
                } else if (value <= product.warningThreshold) {
                  status = '🟡 库存预警';
                  color = '#E6A23C';
                  icon = '🟡';
                } else {
                  status = '🟢 库存正常';
                  color = '#67C23A';
                  icon = '🟢';
                }
              }
              
              result += `<div style="display: flex; align-items: center; margin: 4px 0;">
                <span style="display:inline-block;margin-right:8px;border-radius:10px;width:12px;height:12px;background-color:${param.color}"></span>
                <span style="flex: 1;">${param.seriesName}:</span>
                <span style="color:${color};font-weight:bold; margin-right: 8px;">${value}</span>
                ${param.seriesName === '当前库存' ? `<span style="color:${color}">${icon} ${status}</span>` : ''}
              </div>`;
            });
            
            result += `<div style="margin-top: 8px; padding-top: 8px; border-top: 1px solid #eee;">
              <div style="color:#E6A23C">⚠️ 预警库存: ${product.warningThreshold}</div>
              <div style="color:#F56C6C">🚨 紧急库存: ${product.urgentThreshold}</div>
            </div>`;
            return result;
          }
        },
        legend: {
          data: ['当前库存', '预警库存线', '紧急库存线'],
          top: 40,
          textStyle: {
            fontSize: 12
          }
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '15%',
          top: '20%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: productNames,
          axisLabel: {
            interval: 0,
            rotate: 45,
            fontSize: 11,
            margin: 10
          },
          axisLine: {
            lineStyle: {
              color: '#999'
            }
          }
        },
        yAxis: {
          type: 'value',
          name: '库存数量',
          min: 0,
          axisLine: {
            lineStyle: {
              color: '#999'
            }
          },
          splitLine: {
            lineStyle: {
              type: 'dashed',
              color: '#eee'
            }
          }
        },
        series: [
          {
            name: '当前库存',
            type: 'bar',
            data: currentStocks,
            barWidth: '40%',
            itemStyle: {
              color: function(params) {
                const product = inventoryAlerts.value[params.dataIndex];
                const currentStock = Number(product.currentStock);
                const urgentThreshold = Number(product.urgentThreshold);
                const warningThreshold = Number(product.warningThreshold);
                
                if (currentStock <= urgentThreshold) {
                  return '#F56C6C'; // 红色：紧急缺货
                } else if (currentStock <= warningThreshold) {
                  return '#E6A23C'; // 黄色：库存预警
                } else {
                  return '#67C23A'; // 绿色：库存正常
                }
              }
            },
            label: {
              show: true,
              position: 'top',
              formatter: function(params) {
                const product = inventoryAlerts.value[params.dataIndex];
                const currentStock = Number(product.currentStock);
                const urgentThreshold = Number(product.urgentThreshold);
                const warningThreshold = Number(product.warningThreshold);
                
                if (currentStock <= urgentThreshold) {
                  return '🚨';
                } else if (currentStock <= warningThreshold) {
                  return '⚠️';
                }
                return params.value;
              },
              fontSize: 14
            }
          },
          {
            name: '预警库存线',
            type: 'line',
            data: warningThresholds,
            lineStyle: {
              type: 'dashed',
              width: 2,
              color: '#E6A23C'
            },
            symbol: 'none'
          },
          {
            name: '紧急库存线',
            type: 'line',
            data: urgentThresholds,
            lineStyle: {
              type: 'dashed',
              width: 2,
              color: '#F56C6C'
            },
            symbol: 'none'
          }
        ],
        dataZoom: [
          {
            type: 'inside',
            start: 0,
            end: 100
          },
          {
            type: 'slider',
            show: true,
            bottom: 20,
            start: 0,
            end: 100
          }
        ]
      };
      
      chartInstance.setOption(option);
      
      // 响应式调整
      window.addEventListener('resize', () => {
        chartInstance?.resize();
      });
      
    }, 100);
    
  } catch (error) {
    console.error('初始化图表失败:', error);
  }
};

const refreshLowStockChart = async () => {
  await loadInventoryAlerts();
  ElMessage.success('库存预警数据已刷新');
};

const exportLowStockData = () => {
  ElMessage.info('导出库存预警数据功能开发中...');
  loadInventoryAlerts();
};

const handleAlertFilterChange = (filter) => {
   alertFilter.value = filter;
    ElMessage.info(`已筛选预警级别: ${filter}`);
    // 筛选改变时重新加载数据
    loadInventoryAlerts();
};

const handleWarehouseChange = (warehouseId) => {
  selectedWarehouse.value = warehouseId;
  ElMessage.info(`已选择仓库: ${warehouseId}`);
  // 仓库改变时重新加载数据
  loadInventoryAlerts();
};

const loadWarehouseList = async () => {
  try {
    const res = await get('/api/auth/warehouse/listEnable');
    console.log('加载仓库列表:', res);
    if (res) {
      // 在数组开头插入"全部仓库"选项
      warehouseList.value = [
        {
          id: '', // 空值表示全部
          name: '全部仓库'
        },
        ...(res || [])
      ];
      
      // 设置默认选中全部仓库
      selectedWarehouse.value = '';
    }
  } catch (error) {
    console.error('加载仓库列表失败:', error);
    // 即使出错也设置默认选项
    warehouseList.value = [
      {
        id: '',
        name: '全部仓库'
      }
    ];
    selectedWarehouse.value = '';
  }
};

// 消息相关方法
const handleMessageClick = () => {
  if (!messageVisible.value) {
    loadApprovalMessages();
    loadSystemMessages();
  }
};

const loadApprovalMessages = async () => {
  try {
    const res = await post('/api/auth/approval/pendingTasks', {
      page: 1,
      size: 10
    });
    approvalMessages.value = res.records || [];
  } catch (e) {
    console.error('加载待办审批失败:', e);
  }
};

const loadSystemMessages = async () => {
  try {
    const res = await post('/api/auth/message/system', {
      page: 1,
      size: 10
    });
    systemMessages.value = res.records || [];
  } catch (e) {
    console.error('加载系统消息失败:', e);
  }
};


const markAllAsRead = async () => {
  try {
    await get('/api/auth/message/markAllAsRead');
    unreadCount.value = 0;
    approvalMessages.value.forEach(msg => msg.isRead = 1);
    systemMessages.value.forEach(msg => msg.isRead = 1);
    ElMessage.success('消息已标记为已读');
  } catch (e) {
    ElMessage.error('标记消息为已读失败');
  }
};

const handleApprovalClick = (item) => {
  messageVisible.value = false;
  changeDisplayMode('approval-task');
};

const getBizTypeText = (bizType) => {
  const types = {
    1: '入库单',
    2: '出库单',
    3: '调拨单',
    4: '盘点单'
  };
  return types[bizType] || '单据';
};

const formatTime = (timeString) => {
  if (!timeString) return '';
  const date = new Date(timeString);
  return `${date.getFullYear()}-${padZero(date.getMonth() + 1)}-${padZero(date.getDate())} ${padZero(date.getHours())}:${padZero(date.getMinutes())}`;
};

const padZero = (num) => {
  return num < 10 ? `0${num}` : num;
};

const handleUserCommand = (command) => {
  console.log('用户操作:', command);
  switch (command) {
    case 'profile':
      changeDisplayMode('profile');
      break;
    case 'settings':
      changeDisplayMode('settings');
      break;
    case 'logout':
      userLogout();
      break;
  }
};

const viewAllActions = () => {
  changeDisplayMode('operation-log');
};

const userLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '退出登录', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(() => {
    logout(() => {
      window.location.href = '/login';
    });
  });
};

// 初始化数据
const loadOverviewData = async () => {
  overviewData.value = {
    totalProducts: 156,
    totalWarehouses: 8,
    todayInbound: 24,
    todayOutbound: 18,
    pendingApprovals: 5,
    lowStockItems: 12
  };
};

const loadRecentActions = async () => {
  try {
    recentActions.value = [
      {
        operationTime: new Date(),
        module: '入库管理',
        operation: '新建入库单',
        description: '创建了入库单 IN20240115001',
        operator: '张三'
      },
      {
        operationTime: new Date(Date.now() - 30 * 60 * 1000),
        module: '出库管理',
        operation: '审核出库单',
        description: '审核通过了出库单 OUT20240115002',
        operator: '李四'
      }
    ];
  } catch (e) {
    console.error('加载操作记录失败:', e);
  }
};

const loadUserInfo = async () => {
  try {
    const res = await get('/api/auth/user/info');
    userInfo.value = res || {};
  } catch (e) {
    console.error('加载用户信息失败:', e);
  }
};

const fetchUnreadCount = async () => {
  try {
    unreadCount.value = 3;
  } catch (e) {
    console.error('获取未读消息数失败:', e);
  }
};

onMounted(() => {
  loadUserInfo();
  loadIndexPageStats(); // 新增这行
  loadOverviewData();
  loadRecentActions();
  fetchUnreadCount();
  loadWarehouseList();
  
  nextTick(() => {
    loadInventoryAlerts();
  });
});

// 组件卸载时清理图表
import { onUnmounted } from 'vue';
onUnmounted(() => {
  if (chartInstance) {
    chartInstance.dispose();
    chartInstance = null;
  }
});
</script>

<style scoped>
.dashboard-container {
  height: 100vh;
  background-color: #f5f7fa;
  display: flex;
  flex-direction: column;
}

.nav-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  background-color: #fff;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  height: 60px;
  z-index: 1000;
}

.nav-left {
  display: flex;
  align-items: center;
}

.logo {
  margin: 0 40px 0 0;
  font-size: 20px;
  font-weight: bold;
  color: #409EFF;
}

.nav-menu {
  border-bottom: none;
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.message-bell {
  cursor: pointer;
  padding: 8px;
  border-radius: 50%;
  transition: background-color 0.3s;
  color: #606266;
}

.message-bell:hover {
  background-color: #f0f0f0;
  color: #409EFF;
}

.avatar-dropdown {
  cursor: pointer;
}

.avatar-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.avatar-wrapper:hover {
  background-color: #f0f0f0;
}

.user-name {
  font-size: 14px;
  color: #606266;
}

.main-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  height: calc(100vh - 60px);
}

.overview-cards {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 8px;
  border: none;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.stat-icon .el-icon {
  font-size: 24px;
}

.stat-info {
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

.quick-actions-card {
  margin-bottom: 20px;
  border-radius: 8px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.chart-actions {
  display: flex;
  align-items: center;
}

.quick-actions {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.quick-action-btn {
  flex: 1;
  min-width: 160px;
  height: 80px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  font-size: 14px;
}

.quick-action-btn .el-icon {
  font-size: 24px;
}

/* 库存预警卡片样式 */
.low-stock-alert-card {
  margin-bottom: 20px;
  border-radius: 12px;
  border: 1px solid #e6ebf5;
}

.low-stock-alert-card .card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 16px 20px;
  border-bottom: 1px solid #e6ebf5;
}

.card-title-section {
  display: flex;
  flex-direction: column;
}

.card-title-section .card-title {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
}

.card-title-section .card-subtitle {
  font-size: 14px;
  color: #909399;
}

.chart-container-full {
  height: 500px;
  padding: 20px;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-radius: 8px;
  margin: 0 20px;
  position: relative;
}

.chart-wrapper {
  width: 100%;
  height: 100%;
  min-height: 500px;
}

.chart-stats {
  display: flex;
  justify-content: space-around;
  padding: 16px 20px;
  background-color: #f8fafc;
  border-top: 1px solid #e6ebf5;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.stat-label {
  font-size: 14px;
  color: #606266;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
}

.stat-value.urgent {
  color: #F56C6C;
}

.stat-value.warning {
  color: #E6A23C;
}

.stat-value.normal {
  color: #67C23A;
}

.chart-section {
  margin-bottom: 20px;
}

.chart-card {
  border-radius: 8px;
  height: 400px;
}

.chart-container {
  height: 320px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chart-placeholder {
  text-align: center;
}

.recent-actions-card {
  border-radius: 8px;
}

/* 消息样式 */
.message-tabs {
  padding: 0 10px;
  background-color: #f8fafc;
}

.message-list {
  max-height: 400px;
  overflow-y: auto;
}

.message-item {
  display: flex;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
  position: relative;
  transition: all 0.3s;
  cursor: pointer;
}

.unread-message {
  background-color: #f8fafc;
}

.message-unread-dot {
  position: absolute;
  left: 8px;
  top: 50%;
  transform: translateY(-50%);
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #f56c6c;
}

.message-content {
  flex: 1;
  margin-left: 15px;
}

.message-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 4px;
}

.message-time {
  font-size: 12px;
  color: #999;
}

.message-text {
  font-size: 14px;
  color: #666;
  line-height: 1.5;
}

.sender-name {
  color: #409EFF;
  font-weight: bold;
  margin-right: 5px;
}

.message-content-text {
  color: #666;
  margin-right: 5px;
}

.related-words {
  color: #67C23A;
  font-style: italic;
}

.no-message {
  padding: 20px 0;
  text-align: center;
  color: #999;
}

.mark-all-read-container {
  display: flex;
  justify-content: flex-end;
  padding: 8px 12px;
  border-bottom: 1px solid #f0f0f0;
}

.mark-all-read-container .el-button {
  color: #666;
}

.mark-all-read-container .el-button:hover {
  color: #409EFF;
}

.mark-all-read-container .el-button:disabled {
  color: #ccc;
  cursor: not-allowed;
}

.mark-all-read-container .el-icon {
  margin-right: 4px;
}

.chart-loading,
.chart-empty {
  height: 500px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .nav-header {
    padding: 0 10px;
  }
  
  .logo {
    font-size: 16px;
    margin-right: 20px;
  }
  
  .user-name {
    display: none;
  }
  
  .quick-actions {
    justify-content: center;
  }
  
  .quick-action-btn {
    min-width: 140px;
  }
  
  .main-content {
    padding: 10px;
  }
  
  .chart-actions {
    flex-direction: column;
    gap: 8px;
  }
  
  .low-stock-alert-card .card-header {
    flex-direction: column;
    gap: 16px;
  }
  
  .chart-container-full {
    height: 400px;
    margin: 0 10px;
    padding: 10px;
  }
  
  .chart-stats {
    flex-wrap: wrap;
    gap: 16px;
  }
}
</style>