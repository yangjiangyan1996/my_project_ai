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
          <!-- <el-sub-menu index="inventory">
            <template #title>库存管理</template>
            <el-menu-item index="inventory-list" @click="changeDisplayMode('inventory-list')">库存查询</el-menu-item>
            <el-menu-item index="inventory-transaction" @click="changeDisplayMode('inventory-transaction')">库存流水</el-menu-item>
            <el-menu-item index="stock-take" @click="changeDisplayMode('stock-take')">库存盘点</el-menu-item>
          </el-sub-menu> -->
          
          <!-- 业务管理 -->
          <el-sub-menu index="business">
            <template #title>出入库管理</template>
            <el-menu-item index="inbound" @click="changeDisplayMode('inbound')">入库管理</el-menu-item>
            <el-menu-item index="outbound" @click="changeDisplayMode('outbound')">出库管理</el-menu-item>
            <!-- <el-menu-item index="transfer" @click="changeDisplayMode('transfer')">调拨管理</el-menu-item>  TODO yang 待开发 -->
          </el-sub-menu>
          
          <!-- 基础数据 -->
          <el-sub-menu index="base">
            <template #title>基础数据</template>
            <!-- <el-menu-item index="product" @click="changeDisplayMode('product')">产品管理</el-menu-item> -->
            <!-- <el-menu-item index="warehouse" @click="changeDisplayMode('warehouse')">仓库管理</el-menu-item> -->
            <el-menu-item index="supplier" @click="changeDisplayMode('supplier')">供应商管理</el-menu-item>
            <el-menu-item index="customer" @click="changeDisplayMode('customer')">客户管理</el-menu-item>
            <!-- <el-menu-item index="approveManager" @click="changeDisplayMode('approveManager')">审批管理</el-menu-item> TODO yang 待开发 -->
          </el-sub-menu>

          <el-sub-menu index="product">
            <template #title>产品管理</template>
            <el-menu-item index="product" @click="changeDisplayMode('product')">产品管理</el-menu-item>
            <el-menu-item index="warehouse" @click="changeDisplayMode('warehouse')">仓库管理</el-menu-item>
            <!-- <el-menu-item index="supplier" @click="changeDisplayMode('supplier')">供应商管理</el-menu-item> -->
            <!-- <el-menu-item index="customer" @click="changeDisplayMode('customer')">客户管理</el-menu-item> -->
            <el-menu-item index="shelf" @click="changeDisplayMode('shelf')">货架管理</el-menu-item>
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
            <el-avatar :src="userInfo.avatarUrl || '/images/default-avatar.png'" />
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
      <div class="overview-cards">
        <el-row :gutter="20">
          <el-col :xs="12" :sm="6" :lg="3">
            <el-card class="stat-card" shadow="hover">
              <div class="stat-content">
                <div class="stat-icon" style="background-color: #409EFF;">
                  <el-icon><Box /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ overviewData.totalProducts }}</div>
                  <div class="stat-label">产品总数</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :xs="12" :sm="6" :lg="3">
            <el-card class="stat-card" shadow="hover">
              <div class="stat-content">
                <div class="stat-icon" style="background-color: #67C23A;">
                  <el-icon><OfficeBuilding /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ overviewData.totalWarehouses }}</div>
                  <div class="stat-label">仓库数量</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :xs="12" :sm="6" :lg="3">
            <el-card class="stat-card" shadow="hover">
              <div class="stat-content">
                <div class="stat-icon" style="background-color: #E6A23C;">
                  <el-icon><TrendCharts /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ overviewData.todayInbound }}</div>
                  <div class="stat-label">今日入库</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :xs="12" :sm="6" :lg="3">
            <el-card class="stat-card" shadow="hover">
              <div class="stat-content">
                <div class="stat-icon" style="background-color: #F56C6C;">
                  <el-icon><TrendCharts /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ overviewData.todayOutbound }}</div>
                  <div class="stat-label">今日出库</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :xs="12" :sm="6" :lg="3">
            <el-card class="stat-card" shadow="hover">
              <div class="stat-content">
                <div class="stat-icon" style="background-color: #909399;">
                  <el-icon><Clock /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ overviewData.pendingApprovals }}</div>
                  <div class="stat-label">待办审批</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :xs="12" :sm="6" :lg="3">
            <el-card class="stat-card" shadow="hover">
              <div class="stat-content">
                <div class="stat-icon" style="background-color: #9b59b6;">
                  <el-icon><Warning /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ overviewData.lowStockItems }}</div>
                  <div class="stat-label">低库存预警</div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
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

      <!-- 图表和表格区域 -->
      <el-row :gutter="20" class="chart-section">
        <el-col :xs="24" :lg="12">
          <el-card class="chart-card" shadow="never">
            <template #header>
              <div class="card-header">
                <span class="card-title">库存趋势</span>
              </div>
            </template>
            <div class="chart-container">
              <!-- 这里可以接入 ECharts 图表 -->
              <div class="chart-placeholder">
                <el-empty description="库存趋势图表" :image-size="100" />
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :xs="24" :lg="12">
          <el-card class="chart-card" shadow="never">
            <template #header>
              <div class="card-header">
                <span class="card-title">出入库统计</span>
              </div>
            </template>
            <div class="chart-container">
              <!-- 这里可以接入 ECharts 图表 -->
              <div class="chart-placeholder">
                <el-empty description="出入库统计图表" :image-size="100" />
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
    <CkProductManage v-if="displayMode === 'product'" />
    <CkInboundManage v-if="displayMode === 'inbound'" />
    <CkOutboundManage v-if="displayMode === 'outbound'" />
    <CkTransferManage v-if="displayMode === 'transfer'" />
    <CkInventoryList v-if="displayMode === 'inventory-list'" />
    <CkInventoryTransaction v-if="displayMode === 'inventory-transaction'" />
    <CkStockTake v-if="displayMode === 'stock-take'" />
    <CkShelfManage v-if="displayMode === 'shelf'" />
    <CkUser v-if="displayMode === 'profile'" />

  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { post, get } from '@/net';
import { logout } from '@/net';
import { 
  Bell, CircleCheck, ArrowDown, User, Setting, SwitchButton,
  Box, OfficeBuilding,   TrendCharts, Clock, Warning,
  Plus, Minus, Refresh, DocumentChecked, Finished
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



const router = useRouter();

// 响应式数据
const activeNav = ref('dashboard');
const messageVisible = ref(false);
const activeMessageTab = ref('approval');
const unreadCount = ref(0);
const displayMode = ref('dashboard');

// 用户信息
const userInfo = ref({
  id: null,
  username: '',
  realName: '',
  avatarUrl: '',
  role: ''
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

// 计算属性
const userInitial = computed(() => {
  return userInfo.value.realName ? userInfo.value.realName.charAt(0) : '用户';
});

// 核心方法：切换显示模式
function changeDisplayMode(mode) {
  console.log('切换显示模式:', mode);
  displayMode.value = mode;
  activeNav.value = mode; // 更新导航激活状态
}

// 快捷操作处理方法 - 使用路由跳转
const handleQuickAction = (action) => {
  console.log('快捷操作:', action);
  
  const routeMap = {
    'inbound': '/index/ckInboundCreate',
    'outbound': '/index/ckOutboundCreate', 
    'transfer': '/index/ckTransferCreate',
    'stock-take': '/index/ckStockTakeCreate',
    'approval': '/index/ckApprovalTask'
  };
  
  const targetRoute = routeMap[action];
  if (targetRoute) {
    // 使用路由跳转到具体页面
    router.push(targetRoute);
  } else {
    ElMessage.warning('该功能暂未开放');
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
      // 退出登录后的处理
      window.location.href = '/login';
    });
  });
};

// 初始化数据
const loadOverviewData = async () => {
  try {
    const res = await get('/api/auth/dashboard/overview');
    overviewData.value = res || {};
  } catch (e) {
    console.error('加载概览数据失败:', e);
  }
};

const loadRecentActions = async () => {
  try {
    const res = await post('/api/auth/operation-log/recent', {
      page: 1,
      size: 10
    });
    recentActions.value = res.records || [];
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
    const res = await get('/api/auth/message/unreadCount');
    unreadCount.value = res || 0;
  } catch (e) {
    console.error('获取未读消息数失败:', e);
  }
};

onMounted(() => {
  loadUserInfo();
  loadOverviewData();
  loadRecentActions();
  fetchUnreadCount();
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
}
</style>