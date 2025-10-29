<template>
  <div class="inbound-detail">
    <!-- 基本信息 -->
    <el-card class="info-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>基本信息</span>
          <div class="header-actions">
            <el-tag 
              :type="getStatusTagType(inboundData.status)" 
              size="large"
            >
              {{ getStatusText(inboundData.status) }}
            </el-tag>
          </div>
        </div>
      </template>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="入库单号">{{ inboundData.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="入库类型">{{ getTypeText(inboundData.orderType) }}</el-descriptions-item>
        <el-descriptions-item label="仓库">{{ inboundData.warehouseName }}</el-descriptions-item>
        <el-descriptions-item label="供应商">{{ inboundData.supplierName || '--' }}</el-descriptions-item>
        <el-descriptions-item label="申请人">{{ inboundData.applicantName }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatTime(inboundData.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="总数量">{{ inboundData.totalQuantity }}</el-descriptions-item>
        <el-descriptions-item label="总金额">¥{{ (inboundData.totalAmount || 0).toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ inboundData.remark || '--' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 产品明细 -->
    <el-card class="product-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>产品明细</span>
          <span class="product-count">共 {{ productList.length }} 种产品</span>
        </div>
      </template>
      <el-table
        :data="productList"
        border
        class="product-table"
        empty-text="暂无产品明细"
      >
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column label="产品信息" min-width="200">
          <template #default="{ row }">
            <div class="product-info">
              <div class="product-name">{{ row.productName }}</div>
              <div class="product-sku">{{ row.sku }}</div>
              <div class="product-spec" v-if="row.spec">{{ row.spec }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="单位" width="80" align="center">
          <template #default="{ row }">
            <span>{{ row.unit }}</span>
          </template>
        </el-table-column>
        <el-table-column label="计划数量" width="100" align="center">
          <template #default="{ row }">
            <span>{{ row.quantity }}</span>
          </template>
        </el-table-column>
        <el-table-column label="实际数量" width="100" align="center">
          <template #default="{ row }">
            <span>{{ row.actualQuantity || row.quantity }}</span>
          </template>
        </el-table-column>
        <el-table-column label="单价" width="120" align="right">
          <template #default="{ row }">
            <span>¥{{ (row.price || 0).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="金额" width="120" align="right">
          <template #default="{ row }">
            <span class="amount">¥{{ ((row.price || 0) * (row.actualQuantity || row.quantity)).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="批次号" width="150">
          <template #default="{ row }">
            <span>{{ row.batchNo || '--' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="生产日期" width="120">
          <template #default="{ row }">
            <span>{{ row.productionDate || '--' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="保质期" width="100">
          <template #default="{ row }">
            <span>{{ row.shelfLife ? row.shelfLife + '天' : '--' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="备注" min-width="150">
          <template #default="{ row }">
            <span>{{ row.remark || '--' }}</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 审批流程 -->
    <el-card class="approval-card" shadow="never" v-if="inboundData.status !== 0">
      <template #header>
        <div class="card-header">
          <span>审批流程</span>
        </div>
      </template>
      <el-timeline>
        <el-timeline-item
          v-for="(step, index) in approvalSteps"
          :key="index"
          :timestamp="formatTime(step.time)"
          :type="getStepType(step.status)"
          :hollow="step.status === 'pending'"
        >
          <div class="step-content">
            <div class="step-title">{{ step.nodeName }}</div>
            <div class="step-info">
              <span class="approver">{{ step.approver || '待审批' }}</span>
              <span class="opinion" v-if="step.opinion"> - {{ step.opinion }}</span>
            </div>
          </div>
        </el-timeline-item>
      </el-timeline>
    </el-card>

    <!-- 操作按钮 -->
    <div class="action-section">
      <el-button @click="$emit('close')">关闭</el-button>
      <el-button
        type="primary"
        @click="handlePrint"
        v-if="inboundData.status === 3"
      >
        打印入库单
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { ElMessage } from 'element-plus';

const props = defineProps({
  inboundData: {
    type: Object,
    required: true
  }
});

const emit = defineEmits(['close', 'refresh']);

const productList = ref([]);
const approvalSteps = ref([]);

// 入库类型选项
const orderTypeOptions = [
  { value: 1, label: '采购入库' },
  { value: 2, label: '生产入库' },
  { value: 3, label: '退货入库' },
  { value: 4, label: '调拨入库' },
  { value: 5, label: '其他入库' }
];

const statusOptions = [
  { value: 0, label: '草稿' },
  { value: 1, label: '待审核' },
  { value: 2, label: '已通过' },
  { value: 3, label: '已完成' },
  { value: 4, label: '已拒绝' },
  { value: 9, label: '已取消' }
];

// 计算属性
const totalAmount = computed(() => {
  return productList.value.reduce((sum, item) => {
    return sum + (item.price || 0) * (item.actualQuantity || item.quantity);
  }, 0);
});

// 方法
const loadProductList = async () => {
  try {
    // 模拟加载产品明细数据
    productList.value = [
      {
        id: '1',
        productName: '笔记本电脑',
        sku: 'NB001',
        spec: '15寸 i7 16G',
        unit: '台',
        quantity: 10,
        actualQuantity: 10,
        price: 5999.00,
        batchNo: 'B202401001',
        productionDate: '2024-01-15',
        shelfLife: null,
        remark: ''
      },
      {
        id: '2',
        productName: '无线鼠标',
        sku: 'MS002',
        spec: '2.4G',
        unit: '个',
        quantity: 50,
        actualQuantity: 50,
        price: 89.00,
        batchNo: 'B202401002',
        productionDate: '2024-01-10',
        shelfLife: 365,
        remark: '黑色款'
      }
    ];
  } catch (error) {
    console.error('加载产品明细失败:', error);
    productList.value = [];
  }
};

const loadApprovalSteps = async () => {
  if (props.inboundData.status === 0) return;
  
  try {
    // 模拟加载审批流程数据
    approvalSteps.value = [
      {
        nodeName: '提交申请',
        approver: props.inboundData.applicantName,
        time: props.inboundData.createdAt,
        status: 'success',
        opinion: ''
      },
      {
        nodeName: '仓库主管审批',
        approver: '李主管',
        time: props.inboundData.updatedAt,
        status: props.inboundData.status >= 2 ? 'success' : 'pending',
        opinion: props.inboundData.status >= 2 ? '同意入库' : ''
      }
    ];
    
    if (props.inboundData.status === 3) {
      approvalSteps.value.push({
        nodeName: '入库完成',
        approver: '系统',
        time: props.inboundData.updatedAt,
        status: 'success',
        opinion: '入库操作已完成'
      });
    }
  } catch (error) {
    console.error('加载审批流程失败:', error);
    approvalSteps.value = [];
  }
};

const getTypeText = (orderType) => {
  const typeObj = orderTypeOptions.find(item => item.value === orderType);
  return typeObj ? typeObj.label : '未知';
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

const getStepType = (status) => {
  const types = {
    'success': 'success',
    'pending': 'primary',
    'error': 'danger'
  };
  return types[status] || 'info';
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

const handlePrint = () => {
  ElMessage.info('打印功能开发中');
};

onMounted(() => {
  loadProductList();
  loadApprovalSteps();
});
</script>

<style scoped>
.inbound-detail {
  max-height: 80vh;
  overflow-y: auto;
  padding-right: 10px;
}

.info-card, .product-card, .approval-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.product-count {
  color: #909399;
  font-size: 14px;
}

.product-table {
  width: 100%;
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

.product-spec {
  font-size: 12px;
  color: #606266;
}

.amount {
  font-weight: bold;
  color: #E6A23C;
}

.step-content {
  line-height: 1.4;
}

.step-title {
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.step-info {
  font-size: 14px;
  color: #606266;
}

.approver {
  color: #409EFF;
}

.opinion {
  color: #909399;
}

.action-section {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

:deep(.el-descriptions) {
  margin-top: 0;
}

:deep(.el-timeline) {
  padding-left: 0;
}
</style>