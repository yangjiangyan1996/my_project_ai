<template>
  <div class="supplier-container">
    <!-- 页面标题和操作栏 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">供应商管理</h1>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>工作台</el-breadcrumb-item>
          <el-breadcrumb-item>基础数据</el-breadcrumb-item>
          <el-breadcrumb-item>供应商管理</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增供应商
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
        <el-form-item label="供应商名称">
          <el-input
            v-model="filterForm.supplierName"
            placeholder="请输入供应商名称"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="供应商编码">
          <el-input
            v-model="filterForm.supplierCode"
            placeholder="请输入供应商编码"
            clearable
            style="width: 180px"
          />
        </el-form-item>
        <el-form-item label="联系人">
          <el-input
            v-model="filterForm.contactPerson"
            placeholder="请输入联系人"
            clearable
            style="width: 150px"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select 
            v-model="filterForm.status" 
            placeholder="请选择状态" 
            clearable
            style="width: 120px"  
          >
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

    <!-- 供应商列表 -->
    <el-card class="table-card">
      <el-table
        :data="supplierList"
        v-loading="loading"
        style="width: 100%"
        empty-text="暂无供应商数据"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="supplierCode" label="供应商编码" width="140" />
        <el-table-column prop="supplierName" label="供应商名称" min-width="180" />
        <el-table-column prop="contactPerson" label="联系人" width="120" />
        <el-table-column prop="contactPhone" label="联系电话" width="140" />
        <el-table-column prop="email" label="邮箱" width="180" show-overflow-tooltip />
        <el-table-column prop="address" label="地址" min-width="200" show-overflow-tooltip />
        <el-table-column prop="supplierType" label="供应商类型" width="120">
          <template #default="scope">
            <el-tag :type="getSupplierTypeTagType(scope.row.supplierType)">
              {{ getSupplierTypeText(scope.row.supplierType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="cooperationStatus" label="合作状态" width="100">
          <template #default="scope">
            <el-tag :type="getCooperationStatusTagType(scope.row.cooperationStatus)">
              {{ getCooperationStatusText(scope.row.cooperationStatus) }}
            </el-tag>
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
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button type="primary" link @click="handleEdit(scope.row)">
              编辑
            </el-button>
            <el-button type="info" link @click="handleView(scope.row)">
              查看
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
      width="600px"
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
            <el-form-item label="供应商编码" prop="supplierCode">
              <el-input
                v-model="form.supplierCode"
                placeholder="请输入供应商编码"
                :disabled="isEditMode"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="供应商名称" prop="supplierName">
              <el-input
                v-model="form.supplierName"
                placeholder="请输入供应商名称"
              />
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
            <el-form-item label="供应商类型" prop="supplierType">
              <el-select v-model="form.supplierType" placeholder="请选择供应商类型" style="width: 100%">
                <el-option label="生产商" value="1" />
                <el-option label="代理商" value="2" />
                <el-option label="经销商" value="3" />
                <el-option label="批发商" value="4" />
                <el-option label="其他" value="5" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="合作状态" prop="cooperationStatus">
              <el-select v-model="form.cooperationStatus" placeholder="请选择合作状态" style="width: 100%">
                <el-option label="合作中" value="1" />
                <el-option label="暂停合作" value="2" />
                <el-option label="终止合作" value="3" />
                <el-option label="待审核" value="4" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="营业执照" prop="businessLicense">
          <el-input
            v-model="form.businessLicense"
            placeholder="请输入营业执照号"
          />
        </el-form-item>

        <el-form-item label="开户银行" prop="bankName">
          <el-input
            v-model="form.bankName"
            placeholder="请输入开户银行"
          />
        </el-form-item>

        <el-form-item label="银行账号" prop="bankAccount">
          <el-input
            v-model="form.bankAccount"
            placeholder="请输入银行账号"
          />
        </el-form-item>

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
      title="供应商详情"
      width="600px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="供应商编码">{{ currentRow?.supplierCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="供应商名称">{{ currentRow?.supplierName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="联系人">{{ currentRow?.contactPerson || '-' }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentRow?.contactPhone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ currentRow?.email || '-' }}</el-descriptions-item>
        <el-descriptions-item label="供应商类型">
          {{ getSupplierTypeText(currentRow?.supplierType) }}
        </el-descriptions-item>
        <el-descriptions-item label="合作状态">
          <el-tag :type="getCooperationStatusTagType(currentRow?.cooperationStatus)">
            {{ getCooperationStatusText(currentRow?.cooperationStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="营业执照">{{ currentRow?.businessLicense || '-' }}</el-descriptions-item>
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
        <el-descriptions-item label="更新时间">{{ formatTime(currentRow?.modifiedAt) }}</el-descriptions-item>
      </el-descriptions>

      <template #footer>
        <el-button @click="viewDialogVisible = false">关闭</el-button>
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
const submitLoading = ref(false);
const isEditMode = ref(false);
const currentRow = ref(null);
const selectedRows = ref([]);

// 筛选表单
const filterForm = reactive({
  supplierName: '',
  supplierCode: '',
  contactPerson: '',
  status: ''
});

// 分页
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
});

// 供应商列表
const supplierList = ref([]);

// 表单数据和验证规则
const formRef = ref();
const form = reactive({
  supplierCode: '',
  supplierName: '',
  contactPerson: '',
  contactPhone: '',
  email: '',
  address: '',
  supplierType: '',
  cooperationStatus: '1',
  businessLicense: '',
  bankName: '',
  bankAccount: '',
  remark: '',
  status: 1
});

const formRules = {
  supplierCode: [
    { required: true, message: '请输入供应商编码', trigger: 'blur' },
    { min: 2, max: 50, message: '供应商编码长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  supplierName: [
    { required: true, message: '请输入供应商名称', trigger: 'blur' },
    { min: 2, max: 100, message: '供应商名称长度在 2 到 100 个字符', trigger: 'blur' }
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
  ],
  supplierType: [
    { required: true, message: '请选择供应商类型', trigger: 'change' }
  ]
};

// 计算属性
const dialogTitle = computed(() => {
  return isEditMode.value ? '编辑供应商' : '新增供应商';
});

// 方法
const handleSearch = () => {
  pagination.current = 1;
  loadSupplierList();
};

const handleReset = () => {
  Object.assign(filterForm, {
    supplierName: '',
    supplierCode: '',
    contactPerson: '',
    status: ''
  });
  pagination.current = 1;
  loadSupplierList();
};

const handleSizeChange = (size) => {
  pagination.size = size;
  pagination.current = 1;
  loadSupplierList();
};

const handleCurrentChange = (current) => {
  pagination.current = current;
  loadSupplierList();
};

const handleRefresh = () => {
  loadSupplierList();
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
    supplierCode: row.supplierCode,
    supplierName: row.supplierName,
    contactPerson: row.contactPerson,
    contactPhone: row.contactPhone,
    email: row.email,
    address: row.address,
    supplierType: String(row.supplierType || ''),  // 确保是字符串
    cooperationStatus: row.cooperationStatus,
    businessLicense: row.businessLicense,
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

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除供应商"${row.supplierName}"吗？`,
      '删除确认',
      {
        type: 'warning'
      }
    );
    
    await post('/api/auth/supplier/delete', { id: row.id });
    ElMessage.success('删除成功');
    loadSupplierList();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败');
    }
  }
};

const handleStatusChange = async (row) => {
  try {
    await post('/api/auth/supplier/updateStatus', {
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
    ElMessage.warning('请选择要导出的供应商');
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
      await post('/api/auth/supplier/update', {
        ...form,
        id: currentRow.value.id
      });
      ElMessage.success('更新成功');
    } else {
      await post('/api/auth/supplier/create', form);
      ElMessage.success('创建成功');
    }
    
    dialogVisible.value = false;
    loadSupplierList();
  } catch (error) {
    console.error('保存供应商失败:', error);
    ElMessage.error('保存失败');
  } finally {
    submitLoading.value = false;
  }
};

const resetForm = () => {
  Object.assign(form, {
    supplierCode: '',
    supplierName: '',
    contactPerson: '',
    contactPhone: '',
    email: '',
    address: '',
    supplierType: '',
    cooperationStatus: '1',
    businessLicense: '',
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
const getSupplierTypeText = (type) => {
  const types = {
    '1': '生产商',
    '2': '代理商',
    '3': '经销商',
    '4': '批发商',
    '5': '其他'
  };
  return types[type] || '未知类型';
};

const getSupplierTypeTagType = (type) => {
  const types = {
    '1': 'primary',
    '2': 'success',
    '3': 'warning',
    '4': 'info',
    '5': 'default'
  };
  return types[type] || 'default';
};

const getCooperationStatusText = (status) => {
  const statusMap = {
    '1': '合作中',
    '2': '暂停合作',
    '3': '终止合作',
    '4': '待审核'
  };
  return statusMap[status] || '未知状态';
};

const getCooperationStatusTagType = (status) => {
  const types = {
    '1': 'success',
    '2': 'warning',
    '3': 'danger',
    '4': 'info'
  };
  return types[status] || 'default';
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
const loadSupplierList = async () => {
  loading.value = true;
  try {
    const params = {
      page: pagination.current,
      size: pagination.size,
      ...filterForm
    };
    
    const res = await post('/api/auth/supplier/pageList', params);
    supplierList.value = res.records || [];
    pagination.total = res.total || 0;
  } catch (error) {
    console.error('加载供应商列表失败:', error);
    ElMessage.error('加载供应商列表失败');
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  loadSupplierList();
});
</script>

<style scoped>
.supplier-container {
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