<template>
  <div class="customer-container">
    <!-- 页面标题和操作栏 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">客户管理</h1>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>工作台</el-breadcrumb-item>
          <el-breadcrumb-item>基础数据</el-breadcrumb-item>
          <el-breadcrumb-item>客户管理</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增客户
        </el-button>
        <el-button @click="handleRefresh">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
        <el-button @click="handleExport">
          <el-icon><Download /></el-icon>
          导出
        </el-button>
      </div>
    </div>

    <!-- 筛选条件 -->
    <div class="filter-container">
      <el-form :model="filterForm" inline>
        <el-form-item label="客户名称">
          <el-input
            v-model="filterForm.customerName"
            placeholder="请输入客户名称"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="客户编码">
          <el-input
            v-model="filterForm.customerCode"
            placeholder="请输入客户编码"
            clearable
            style="width: 180px"
          />
        </el-form-item>
        <el-form-item label="客户类型">
          <el-select v-model="filterForm.customerType" placeholder="请选择客户类型" clearable>
            <el-option label="全部" value="" />
            <el-option label="企业客户" value="1" />
            <el-option label="个人客户" value="2" />
            <el-option label="代理商" value="3" />
            <el-option label="经销商" value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="客户等级">
          <el-select v-model="filterForm.customerLevel" placeholder="请选择客户等级" clearable>
            <el-option label="全部" value="" />
            <el-option label="普通客户" value="1" />
            <el-option label="VIP客户" value="2" />
            <el-option label="战略客户" value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="请选择状态" clearable>
            <el-option label="全部" value="" />
            <el-option label="启用" value="1" />
            <el-option label="禁用" value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 客户列表 -->
    <el-card class="table-card">
      <el-table
        :data="customerList"
        v-loading="loading"
        style="width: 100%"
        empty-text="暂无客户数据"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="customerCode" label="客户编码" width="140" />
        <el-table-column prop="customerName" label="客户名称" min-width="180" />
        <el-table-column prop="customerType" label="客户类型" width="100">
          <template #default="scope">
            <el-tag :type="getCustomerTypeTagType(scope.row.customerType)">
              {{ getCustomerTypeText(scope.row.customerType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="customerLevel" label="客户等级" width="100">
          <template #default="scope">
            <el-tag :type="getCustomerLevelTagType(scope.row.customerLevel)" effect="plain">
              {{ getCustomerLevelText(scope.row.customerLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="contactPerson" label="联系人" width="120" />
        <el-table-column prop="contactPhone" label="联系电话" width="140" />
        <el-table-column prop="email" label="邮箱" width="180" show-overflow-tooltip />
        <el-table-column prop="address" label="地址" min-width="200" show-overflow-tooltip />
        <el-table-column prop="creditLimit" label="信用额度" width="120" align="right">
          <template #default="scope">
            {{ formatCurrency(scope.row.creditLimit) }}
          </template>
        </el-table-column>
        <el-table-column prop="arrearsAmount" label="欠款金额" width="120" align="right">
          <template #default="scope">
            <span :class="{ 'text-danger': scope.row.arrearsAmount > 0 }">
              {{ formatCurrency(scope.row.arrearsAmount) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="scope">
            <el-switch
              v-model="scope.row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="scope">
            {{ formatTime(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="scope">
            <el-button type="primary" link @click="handleEdit(scope.row)">
              编辑
            </el-button>
            <el-button type="info" link @click="handleView(scope.row)">
              查看
            </el-button>
            <el-button type="warning" link @click="handleCredit(scope.row)">
              信用
            </el-button>
            <el-button type="danger" link @click="handleDelete(scope.row)">
              删除
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
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="700px"
      :before-close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="100px"
        label-position="right"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="客户编码" prop="customerCode">
              <el-input
                v-model="form.customerCode"
                placeholder="请输入客户编码"
                :disabled="isEditMode"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="客户名称" prop="customerName">
              <el-input
                v-model="form.customerName"
                placeholder="请输入客户名称"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="客户类型" prop="customerType">
              <el-select v-model="form.customerType" placeholder="请选择客户类型" style="width: 100%">
                <el-option label="企业客户" value="1" />
                <el-option label="个人客户" value="2" />
                <el-option label="代理商" value="3" />
                <el-option label="经销商" value="4" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="客户等级" prop="customerLevel">
              <el-select v-model="form.customerLevel" placeholder="请选择客户等级" style="width: 100%">
                <el-option label="普通客户" value="1" />
                <el-option label="VIP客户" value="2" />
                <el-option label="战略客户" value="3" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="联系人" prop="contactPerson">
              <el-input
                v-model="form.contactPerson"
                placeholder="请输入联系人"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="contactPhone">
              <el-input
                v-model="form.contactPhone"
                placeholder="请输入联系电话"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="form.email"
            placeholder="请输入邮箱"
          />
        </el-form-item>

        <el-form-item label="地址" prop="address">
          <el-input
            v-model="form.address"
            type="textarea"
            :rows="2"
            placeholder="请输入详细地址"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="信用额度" prop="creditLimit">
              <el-input-number
                v-model="form.creditLimit"
                placeholder="请输入信用额度"
                :min="0"
                :precision="2"
                :step="1000"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="付款期限" prop="paymentTerm">
              <el-input-number
                v-model="form.paymentTerm"
                placeholder="请输入付款期限"
                :min="0"
                :max="365"
                style="width: 100%"
              >
                <template #append>天</template>
              </el-input-number>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="营业执照" prop="businessLicense">
              <el-input
                v-model="form.businessLicense"
                placeholder="请输入营业执照号"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="税号" prop="taxNumber">
              <el-input
                v-model="form.taxNumber"
                placeholder="请输入税号"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开户银行" prop="bankName">
              <el-input
                v-model="form.bankName"
                placeholder="请输入开户银行"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="银行账号" prop="bankAccount">
              <el-input
                v-model="form.bankAccount"
                placeholder="请输入银行账号"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="2"
            placeholder="请输入备注信息"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-switch
            v-model="form.status"
            :active-value="1"
            :inactive-value="0"
            active-text="启用"
            inactive-text="禁用"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="handleDialogClose">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
          {{ isEditMode ? '更新' : '创建' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog
      v-model="viewDialogVisible"
      title="客户详情"
      width="700px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="客户编码">{{ currentRow?.customerCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="客户名称">{{ currentRow?.customerName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="客户类型">
          <el-tag :type="getCustomerTypeTagType(currentRow?.customerType)">
            {{ getCustomerTypeText(currentRow?.customerType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="客户等级">
          <el-tag :type="getCustomerLevelTagType(currentRow?.customerLevel)" effect="plain">
            {{ getCustomerLevelText(currentRow?.customerLevel) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="联系人">{{ currentRow?.contactPerson || '-' }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentRow?.contactPhone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ currentRow?.email || '-' }}</el-descriptions-item>
        <el-descriptions-item label="信用额度">{{ formatCurrency(currentRow?.creditLimit) }}</el-descriptions-item>
        <el-descriptions-item label="欠款金额">
          <span :class="{ 'text-danger': currentRow?.arrearsAmount > 0 }">
            {{ formatCurrency(currentRow?.arrearsAmount) }}
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="付款期限">{{ currentRow?.paymentTerm || 0 }} 天</el-descriptions-item>
        <el-descriptions-item label="营业执照">{{ currentRow?.businessLicense || '-' }}</el-descriptions-item>
        <el-descriptions-item label="税号">{{ currentRow?.taxNumber || '-' }}</el-descriptions-item>
        <el-descriptions-item label="开户银行">{{ currentRow?.bankName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="银行账号">{{ currentRow?.bankAccount || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态" :span="2">
          <el-tag :type="currentRow?.status === 1 ? 'success' : 'danger'">
            {{ currentRow?.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="地址" :span="2">{{ currentRow?.address || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentRow?.remark || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatTime(currentRow?.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ formatTime(currentRow?.updatedAt) }}</el-descriptions-item>
      </el-descriptions>

      <template #footer>
        <el-button @click="viewDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 信用管理对话框 -->
    <el-dialog
      v-model="creditDialogVisible"
      title="客户信用管理"
      width="500px"
    >
      <el-form :model="creditForm" label-width="100px">
        <el-form-item label="客户名称">
          <el-input :value="currentRow?.customerName" disabled />
        </el-form-item>
        <el-form-item label="当前信用额度">
          <el-input :value="formatCurrency(currentRow?.creditLimit)" disabled />
        </el-form-item>
        <el-form-item label="当前欠款">
          <el-input :value="formatCurrency(currentRow?.arrearsAmount)" disabled />
        </el-form-item>
        <el-form-item label="新信用额度" prop="newCreditLimit">
          <el-input-number
            v-model="creditForm.newCreditLimit"
            placeholder="请输入新的信用额度"
            :min="0"
            :precision="2"
            :step="1000"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="调整原因" prop="reason">
          <el-input
            v-model="creditForm.reason"
            type="textarea"
            :rows="3"
            placeholder="请输入信用额度调整原因"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="creditDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleUpdateCredit">确认调整</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Refresh, Download } from '@element-plus/icons-vue';
import { post, get } from '@/net';

// 响应式数据
const loading = ref(false);
const dialogVisible = ref(false);
const viewDialogVisible = ref(false);
const creditDialogVisible = ref(false);
const submitLoading = ref(false);
const isEditMode = ref(false);
const currentRow = ref(null);
const selectedRows = ref([]);

// 筛选表单
const filterForm = reactive({
  customerName: '',
  customerCode: '',
  customerType: '',
  customerLevel: '',
  status: ''
});

// 分页
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
});

// 客户列表
const customerList = ref([]);

// 表单数据和验证规则
const formRef = ref();
const form = reactive({
  customerCode: '',
  customerName: '',
  customerType: '',
  customerLevel: '1',
  contactPerson: '',
  contactPhone: '',
  email: '',
  address: '',
  creditLimit: 0,
  paymentTerm: 30,
  businessLicense: '',
  taxNumber: '',
  bankName: '',
  bankAccount: '',
  remark: '',
  status: 1
});

const formRules = {
  customerCode: [
    { required: true, message: '请输入客户编码', trigger: 'blur' },
    { min: 2, max: 50, message: '客户编码长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  customerName: [
    { required: true, message: '请输入客户名称', trigger: 'blur' },
    { min: 2, max: 100, message: '客户名称长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  customerType: [
    { required: true, message: '请选择客户类型', trigger: 'change' }
  ],
  contactPerson: [
    { required: true, message: '请输入联系人', trigger: 'blur' }
  ],
  contactPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$|^0\d{2,3}-?\d{7,8}$/, message: '请输入正确的电话号码', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
};

// 信用表单
const creditForm = reactive({
  newCreditLimit: 0,
  reason: ''
});

// 计算属性
const dialogTitle = computed(() => {
  return isEditMode.value ? '编辑客户' : '新增客户';
});

// 方法
const handleSearch = () => {
  pagination.current = 1;
  loadCustomerList();
};

const handleReset = () => {
  Object.assign(filterForm, {
    customerName: '',
    customerCode: '',
    customerType: '',
    customerLevel: '',
    status: ''
  });
  pagination.current = 1;
  loadCustomerList();
};

const handleSizeChange = (size) => {
  pagination.size = size;
  pagination.current = 1;
  loadCustomerList();
};

const handleCurrentChange = (current) => {
  pagination.current = current;
  loadCustomerList();
};

const handleRefresh = () => {
  loadCustomerList();
};

const handleAdd = () => {
  resetForm();
  isEditMode.value = false;
  dialogVisible.value = true;
};

const handleEdit = (row) => {
  resetForm();
  isEditMode.value = true;
  currentRow.value = row;
  
  // 填充表单数据
  Object.assign(form, {
    customerCode: row.customerCode,
    customerName: row.customerName,
    customerType: row.customerType,
    customerLevel: row.customerLevel,
    contactPerson: row.contactPerson,
    contactPhone: row.contactPhone,
    email: row.email,
    address: row.address,
    creditLimit: row.creditLimit,
    paymentTerm: row.paymentTerm,
    businessLicense: row.businessLicense,
    taxNumber: row.taxNumber,
    bankName: row.bankName,
    bankAccount: row.bankAccount,
    remark: row.remark,
    status: row.status
  });
  
  dialogVisible.value = true;
};

const handleView = (row) => {
  currentRow.value = row;
  viewDialogVisible.value = true;
};

const handleCredit = (row) => {
  currentRow.value = row;
  creditForm.newCreditLimit = row.creditLimit;
  creditForm.reason = '';
  creditDialogVisible.value = true;
};

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除客户"${row.customerName}"吗？`,
      '删除确认',
      {
        type: 'warning'
      }
    );
    
    await post('/api/auth/customer/delete', { id: row.id });
    ElMessage.success('删除成功');
    loadCustomerList();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败');
    }
  }
};

const handleStatusChange = async (row) => {
  try {
    await post('/api/auth/customer/updateStatus', {
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

const handleSelectionChange = (selection) => {
  selectedRows.value = selection;
};

const handleExport = () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要导出的客户');
    return;
  }
  
  ElMessage.info('导出功能开发中...');
};

const handleDialogClose = () => {
  ElMessageBox.confirm('确定要关闭吗？未保存的更改将会丢失。', '提示', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(() => {
    dialogVisible.value = false;
  }).catch(() => {
    // 用户取消关闭
  });
};

const handleSubmit = async () => {
  if (!formRef.value) return;
  
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) return;
  
  submitLoading.value = true;
  
  try {
    if (isEditMode.value) {
      await post('/api/auth/customer/update', {
        ...form,
        id: currentRow.value.id
      });
      ElMessage.success('更新成功');
    } else {
      await post('/api/auth/customer/create', form);
      ElMessage.success('创建成功');
    }
    
    dialogVisible.value = false;
    loadCustomerList();
  } catch (error) {
    console.error('保存客户失败:', error);
    ElMessage.error('保存失败');
  } finally {
    submitLoading.value = false;
  }
};

const handleUpdateCredit = async () => {
  if (!creditForm.newCreditLimit && creditForm.newCreditLimit !== 0) {
    ElMessage.warning('请输入新的信用额度');
    return;
  }

  try {
    await post('/api/auth/customer/updateCredit', {
      id: currentRow.value.id,
      newCreditLimit: creditForm.newCreditLimit,
      reason: creditForm.reason
    });
    
    ElMessage.success('信用额度更新成功');
    creditDialogVisible.value = false;
    loadCustomerList();
  } catch (error) {
    console.error('更新信用额度失败:', error);
    ElMessage.error('更新失败');
  }
};

const resetForm = () => {
  Object.assign(form, {
    customerCode: '',
    customerName: '',
    customerType: '',
    customerLevel: '1',
    contactPerson: '',
    contactPhone: '',
    email: '',
    address: '',
    creditLimit: 0,
    paymentTerm: 30,
    businessLicense: '',
    taxNumber: '',
    bankName: '',
    bankAccount: '',
    remark: '',
    status: 1
  });
  
  if (formRef.value) {
    formRef.value.clearValidate();
  }
};

// 工具方法
const getCustomerTypeText = (type) => {
  const types = {
    '1': '企业客户',
    '2': '个人客户',
    '3': '代理商',
    '4': '经销商'
  };
  return types[type] || '未知类型';
};

const getCustomerTypeTagType = (type) => {
  const types = {
    '1': 'primary',
    '2': 'success',
    '3': 'warning',
    '4': 'info'
  };
  return types[type] || 'default';
};

const getCustomerLevelText = (level) => {
  const levels = {
    '1': '普通客户',
    '2': 'VIP客户',
    '3': '战略客户'
  };
  return levels[level] || '未知等级';
};

const getCustomerLevelTagType = (level) => {
  const types = {
    '1': 'info',
    '2': 'warning',
    '3': 'danger'
  };
  return types[level] || 'default';
};

const formatCurrency = (amount) => {
  if (amount === null || amount === undefined) return '¥0.00';
  return `¥${Number(amount).toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',')}`;
};

const formatTime = (timeString) => {
  if (!timeString) return '-';
  const date = new Date(timeString);
  return `${date.getFullYear()}-${padZero(date.getMonth() + 1)}-${padZero(date.getDate())} ${padZero(date.getHours())}:${padZero(date.getMinutes())}`;
};

const padZero = (num) => {
  return num < 10 ? `0${num}` : num;
};

// API 调用
const loadCustomerList = async () => {
  loading.value = true;
  try {
    const params = {
      page: pagination.current,
      size: pagination.size,
      ...filterForm
    };
    
    const res = await post('/api/auth/customer/list', params);
    customerList.value = res.records || [];
    pagination.total = res.total || 0;
  } catch (error) {
    console.error('加载客户列表失败:', error);
    ElMessage.error('加载客户列表失败');
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  loadCustomerList();
});
</script>

<style scoped>
.customer-container {
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

.filter-container {
  margin-bottom: 20px;
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.table-card {
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.text-danger {
  color: #f56c6c;
  font-weight: bold;
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

:deep(.el-descriptions) {
  margin-top: 10px;
}

:deep(.el-descriptions__label) {
  width: 100px;
  font-weight: bold;
}
</style>