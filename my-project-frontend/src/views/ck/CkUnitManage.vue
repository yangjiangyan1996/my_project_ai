<template>
  <div class="unit-container">
    <!-- 页面标题和操作栏 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">产品单位管理</h1>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>工作台</el-breadcrumb-item>
          <el-breadcrumb-item>基础数据</el-breadcrumb-item>
          <el-breadcrumb-item>产品单位管理</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增单位
        </el-button>
        <el-button @click="handleRefresh">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
        <!-- <el-button @click="handleExport">
          <el-icon><Download /></el-icon>
          导出
        </el-button> -->
      </div>
    </div>

    <!-- 筛选条件 -->
    <div class="filter-container">
      <el-form :model="filterForm" inline>
        <el-form-item label="单位编码">
          <el-input
            v-model="filterForm.unitCode"
            placeholder="请输入单位编码"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="单位名称">
          <el-input
            v-model="filterForm.unitName"
            placeholder="请输入单位名称"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="请选择状态" clearable style="width: 120px"  >
            <el-option label="全部" :value="undefined" />
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 单位列表 -->
    <el-card class="table-card">
      <el-table
        :data="unitList"
        v-loading="loading"
        style="width: 100%"
        empty-text="暂无单位数据"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="unitCode" label="单位编码" width="140" />
        <el-table-column prop="unitName" label="单位名称" width="140" />
        <el-table-column prop="remark" label="备注" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
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
        <el-table-column prop="modifiedAt" label="更新时间" width="180">
          <template #default="scope">
            {{ formatTime(scope.row.modifiedAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
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
      width="500px"
      :before-close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="100px"
        label-position="right"
      >
        <el-form-item label="单位编码" prop="unitCode">
          <el-input
            v-model="form.unitCode"
            placeholder="请输入单位编码，如：PCS、BOX、TON"
            :disabled="isEditMode"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="单位名称" prop="unitName">
          <el-input
            v-model="form.unitName"
            placeholder="请输入单位名称，如：个、箱、吨"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
            maxlength="255"
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
      title="单位详情"
      width="500px"
    >
      <el-descriptions :column="1" border>
        <el-descriptions-item label="单位编码">{{ currentRow?.unitCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="单位名称">{{ currentRow?.unitName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentRow?.status === 1 ? 'success' : 'danger'">
            {{ currentRow?.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="备注">{{ currentRow?.remark || '-' }}</el-descriptions-item>
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
  unitCode: '',
  unitName: '',
  status: undefined
});

// 分页
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
});

// 单位列表
const unitList = ref([]);

// 表单数据和验证规则
const formRef = ref();
const form = reactive({
  unitCode: '',
  unitName: '',
  remark: '',
  status: 1
});

const formRules = {
  unitCode: [
    { required: true, message: '请输入单位编码', trigger: 'blur' },
    { min: 1, max: 50, message: '单位编码长度在 1 到 50 个字符', trigger: 'blur' },
    { pattern: /^[A-Za-z0-9_]+$/, message: '单位编码只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  unitName: [
    { required: true, message: '请输入单位名称', trigger: 'blur' },
    { min: 1, max: 50, message: '单位名称长度在 1 到 50 个字符', trigger: 'blur' }
  ]
};

// 计算属性
const dialogTitle = computed(() => {
  return isEditMode.value ? '编辑单位' : '新增单位';
});

// 方法
const handleSearch = () => {
  pagination.current = 1;
  loadUnitList();
};

const handleReset = () => {
  Object.assign(filterForm, {
    unitCode: '',
    unitName: '',
    status: undefined
  });
  pagination.current = 1;
  loadUnitList();
};

const handleSizeChange = (size) => {
  pagination.size = size;
  pagination.current = 1;
  loadUnitList();
};

const handleCurrentChange = (current) => {
  pagination.current = current;
  loadUnitList();
};

const handleRefresh = () => {
  loadUnitList();
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
    unitCode: row.unitCode,
    unitName: row.unitName,
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
      `确定要删除单位"${row.unitName}(${row.unitCode})"吗？`,
      '删除确认',
      {
        type: 'warning'
      }
    );
    
    await post('/api/auth/unit/delete', { id: row.id });
    ElMessage.success('删除成功');
    loadUnitList();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败');
    }
  }
};

const handleStatusChange = async (row) => {
  try {
    await post('/api/auth/unit/updateStatus', {
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
    ElMessage.warning('请选择要导出的单位');
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
      await post('/api/auth/unit/update', {
        ...form,
        id: currentRow.value.id
      });
      ElMessage.success('更新成功');
    } else {
      await post('/api/auth/unit/create', form);
      ElMessage.success('创建成功');
    }
    
    dialogVisible.value = false;
    loadUnitList();
  } catch (error) {
    console.error('保存单位失败:', error);
    ElMessage.error('保存失败');
  } finally {
    submitLoading.value = false;
  }
};

const resetForm = () => {
  Object.assign(form, {
    unitCode: '',
    unitName: '',
    remark: '',
    status: 1
  });
  
  if (formRef.value) {
    formRef.value.clearValidate();
  }
};

// 工具方法
const formatTime = (timeString) => {
  if (!timeString) return '-';
  const date = new Date(timeString);
  return `${date.getFullYear()}-${padZero(date.getMonth() + 1)}-${padZero(date.getDate())} ${padZero(date.getHours())}:${padZero(date.getMinutes())}`;
};

const padZero = (num) => {
  return num < 10 ? `0${num}` : num;
};

// API 调用
const loadUnitList = async () => {
  loading.value = true;
  try {
    const params = {
      page: pagination.current,
      size: pagination.size,
      ...filterForm
    };
    
    // 清理空值参数
    Object.keys(params).forEach(key => {
      if (params[key] === '' || params[key] === undefined || params[key] === null) {
        delete params[key];
      }
    });
    
    const res = await post('/api/auth/unit/pageList', params);
    unitList.value = res.records || [];
    pagination.total = res.total || 0;
  } catch (error) {
    console.error('加载单位列表失败:', error);
    ElMessage.error('加载单位列表失败');
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  loadUnitList();
});
</script>

<style scoped>
.unit-container {
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