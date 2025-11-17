<template>
  <div class="sku-container">
    <!-- 页面标题和操作栏 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">客户SKU映射管理</h1>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>工作台</el-breadcrumb-item>
          <el-breadcrumb-item>基础数据</el-breadcrumb-item>
          <el-breadcrumb-item>客户SKU映射</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增映射
        </el-button>
        <el-button @click="handleRefresh">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
        <el-button @click="handleImport">
          <el-icon><Upload /></el-icon>
          导入
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
        <el-form-item label="客户">
          <el-select
            v-model="filterForm.customerId"
            placeholder="请选择客户"
            clearable
            filterable
            style="width: 200px"
            @change="handleCustomerChange"
          >
            <el-option
              v-for="customer in customerList"
              :key="customer.id"
              :label="customer.customerName"
              :value="customer.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="产品SKU">
          <el-input
            v-model="filterForm.productSku"
            placeholder="请输入产品SKU"
            clearable
            style="width: 180px"
          />
        </el-form-item>
        <el-form-item label="客户SKU">
          <el-input
            v-model="filterForm.customerSku"
            placeholder="请输入客户SKU"
            clearable
            style="width: 180px"
          />
        </el-form-item>
        <el-form-item label="产品名称">
          <el-input
            v-model="filterForm.productName"
            placeholder="请输入产品名称"
            clearable
            style="width: 180px"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="请选择状态" clearable style="width: 120px">
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

    <!-- SKU映射列表 -->
    <el-card class="table-card">
      <el-table
        :data="skuMappingList"
        v-loading="loading"
        style="width: 100%"
        empty-text="暂无SKU映射数据"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="customerName" label="客户名称" min-width="150" />
        <el-table-column prop="productSku" label="产品SKU" width="140" />
        <el-table-column prop="productName" label="产品名称" min-width="180" />
        <el-table-column prop="spec" label="规格" width="120" />
        <el-table-column prop="color" label="颜色" width="100" />
        <el-table-column prop="customerSku" label="客户SKU" width="140" />
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
        <el-table-column prop="remark" label="备注" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="scope">
            {{ formatTime(scope.row.createdAt) }}
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
            <el-form-item label="客户" prop="customerId">
              <el-select
                v-model="form.customerId"
                placeholder="请选择客户"
                style="width: 100%"
                filterable
                @change="handleCustomerSelectChange"
              >
                <el-option
                  v-for="customer in customerList"
                  :key="customer.id"
                  :label="customer.customerName"
                  :value="customer.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="产品" prop="productSku">
              <el-select
                v-model="form.productSku"
                placeholder="请选择产品"
                style="width: 100%"
                filterable
                remote
                :remote-method="searchProducts"
                :loading="productLoading"
                @change="handleProductSelectChange"
              >
                <el-option
                  v-for="product in productOptions"
                  :key="product.sku"
                  :label="`${product.name} (${product.sku})`"
                  :value="product.sku"
                >
                  <span style="float: left">{{ product.name }}</span>
                  <span style="float: right; color: #8492a6; font-size: 13px">
                    {{ product.sku }} | {{ product.spec }} | {{ product.color }}
                  </span>
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="产品名称">
              <el-input v-model="form.productName" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="规格型号">
              <el-input v-model="form.spec" disabled />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="颜色">
              <el-input v-model="form.color" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="客户SKU" prop="customerSku">
              <el-input
                v-model="form.customerSku"
                placeholder="请输入客户SKU编码"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="3"
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
      title="SKU映射详情"
      width="600px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="客户名称">{{ currentRow?.customerName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="产品SKU">{{ currentRow?.productSku || '-' }}</el-descriptions-item>
        <el-descriptions-item label="产品名称">{{ currentRow?.productName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="规格型号">{{ currentRow?.spec || '-' }}</el-descriptions-item>
        <el-descriptions-item label="颜色">{{ currentRow?.color || '-' }}</el-descriptions-item>
        <el-descriptions-item label="客户SKU">{{ currentRow?.customerSku || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态" :span="2">
          <el-tag :type="currentRow?.status === 1 ? 'success' : 'danger'">
            {{ currentRow?.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentRow?.remark || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatTime(currentRow?.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ formatTime(currentRow?.updatedAt) }}</el-descriptions-item>
      </el-descriptions>

      <template #footer>
        <el-button @click="viewDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 导入对话框 -->
    <el-dialog
      v-model="importDialogVisible"
      title="导入SKU映射"
      width="500px"
      :before-close="handleImportCancel"
    >
      <div class="import-tips">
        <el-alert
          title="导入说明"
          type="info"
          description="请先选择客户，然后上传对应的SKU映射文件。文件格式请下载模板参考。"
          :closable="false"
        />
      </div>

      <!-- 客户选择 -->
      <el-form :model="importForm" label-width="80px" style="margin-top: 20px;">
        <el-form-item label="客户" prop="customerId" required>
          <el-select
            v-model="importForm.customerId"
            placeholder="请选择客户"
            style="width: 100%"
            filterable
            clearable
          >
            <el-option
              v-for="customer in customerList"
              :key="customer.id"
              :label="customer.customerName"
              :value="customer.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      
      <el-upload
        class="upload-demo"
        drag
        action=""
        :auto-upload="false"
        :on-change="handleFileChange"
        :on-remove="handleFileRemove"
        :show-file-list="true"
        :file-list="fileList"
        accept=".xlsx,.xls"
        :disabled="!importForm.customerId"
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">
          将文件拖到此处，或<em>点击上传</em>
        </div>
        <div class="el-upload__tip" v-if="!importForm.customerId">
          请先选择客户
        </div>
        <template #tip>
          <div class="el-upload__tip">
            只能上传 xlsx/xls 文件，且不超过10MB
          </div>
        </template>
      </el-upload>

      <div class="download-template">
        <el-button type="text" @click="downloadTemplate">
          <el-icon><Download /></el-icon>
          下载导入模板
        </el-button>
      </div>

      <template #footer>
        <el-button @click="handleImportCancel">取消</el-button>
        <el-button 
          type="primary" 
          @click="handleImportSubmit" 
          :loading="importLoading"
          :disabled="!importForm.customerId || fileList.length === 0"
        >
          开始导入
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Refresh, Download, Upload, UploadFilled } from '@element-plus/icons-vue';
import { post, get } from '@/net';
import axios from 'axios';
import { accessHeader } from '@/net'; 




// 响应式数据
const loading = ref(false);
const dialogVisible = ref(false);
const viewDialogVisible = ref(false);
const importDialogVisible = ref(false);
const submitLoading = ref(false);
const importLoading = ref(false);
const productLoading = ref(false);
const isEditMode = ref(false);
const currentRow = ref(null);
const selectedRows = ref([]);

// 筛选表单
const filterForm = reactive({
  customerId: '',
  productSku: '',
  customerSku: '',
  productName: '',
  status: ''
});

// 导入表单
const importForm = reactive({
  customerId: ''
});
const fileList = ref([]);
const currentFile = ref(null);

// 分页
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
});

// 客户列表
const customerList = ref([]);
// 产品选项
const productOptions = ref([]);
// SKU映射列表
const skuMappingList = ref([]);

// 表单数据和验证规则
const formRef = ref();
const form = reactive({
  customerId: '',
  productSku: '',
  customerSku: '',
  remark: '',
  status: 1,
  // 显示字段
  productName: '',
  spec: '',
  color: '',
  customerName: ''
});

const formRules = {
  customerId: [
    { required: true, message: '请选择客户', trigger: 'change' }
  ],
  productSku: [
    { required: true, message: '请选择产品', trigger: 'change' }
  ],
  customerSku: [
    { required: true, message: '请输入客户SKU', trigger: 'blur' },
    { min: 1, max: 100, message: '客户SKU长度在 1 到 100 个字符', trigger: 'blur' }
  ]
};

// 计算属性
const dialogTitle = computed(() => {
  return isEditMode.value ? '编辑SKU映射' : '新增SKU映射';
});

// 方法
const handleSearch = () => {
  pagination.current = 1;
  loadSkuMappingList();
};

const handleReset = () => {
  Object.assign(filterForm, {
    customerId: '',
    productSku: '',
    customerSku: '',
    productName: '',
    status: ''
  });
  pagination.current = 1;
  loadSkuMappingList();
};

const handleSizeChange = (size) => {
  pagination.size = size;
  pagination.current = 1;
  loadSkuMappingList();
};

const handleCurrentChange = (current) => {
  pagination.current = current;
  loadSkuMappingList();
};

const handleRefresh = () => {
  loadSkuMappingList();
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
    id: row.id,
    customerId: row.customerId,
    productSku: row.productSku,
    customerSku: row.customerSku,
    remark: row.remark,
    status: row.status,
    productName: row.productName,
    spec: row.spec,
    color: row.color,
    customerName: row.customerName
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
      `确定要删除"${row.customerName}"的SKU映射吗？`,
      '删除确认',
      {
        type: 'warning'
      }
    );
    
    await post('/api/auth/sku/delete', { id: row.id });
    ElMessage.success('删除成功');
    loadSkuMappingList();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败');
    }
  }
};

const handleStatusChange = async (row) => {
  try {
    await post('/api/auth/sku/updateStatus', {
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

const handleImport = () => {
  // 重置导入表单
  importForm.customerId = '';
  fileList.value = [];
  currentFile.value = null;
  importDialogVisible.value = true;
};

const handleExport = () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要导出的SKU映射');
    return;
  }
  
  ElMessage.info('导出功能开发中...');
};

const handleCustomerChange = (customerId) => {
  console.log('customerId:', customerId);
  if (customerId) {
    loadSkuMappingList();
  }
};

const handleCustomerSelectChange = (customerId) => {
  const customer = customerList.value.find(c => c.id === customerId);
  if (customer) {
    form.customerName = customer.customerName;
  }
};

const handleProductSelectChange = (productSku) => {
  const product = productOptions.value.find(p => p.sku === productSku);
  if (product) {
    form.productName = product.name;
    form.spec = product.spec;
    form.color = product.color;
  }
};

const searchProducts = async (query) => {
  if (query) {
    productLoading.value = true;
    try {
      const res = await get('/api/auth/product/search?keyword=' + query);
      productOptions.value = res || [];
    } catch (error) {
      console.error('搜索产品失败:', error);
    } finally {
      productLoading.value = false;
    }
  } else {
    productOptions.value = [];
  }
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
      await post('/api/auth/sku/update', {
        ...form,
        id: currentRow.value.id
      });
      ElMessage.success('更新成功');
    } else {
      await post('/api/auth/sku/create', form);
      ElMessage.success('创建成功');
    }
    
    dialogVisible.value = false;
    loadSkuMappingList();
  } catch (error) {
    console.error('保存SKU映射失败:', error);
    ElMessage.error('保存失败');
  } finally {
    submitLoading.value = false;
  }
};

const handleFileChange = (file) => {
  currentFile.value = file;
  fileList.value = [file];
};

const handleFileRemove = () => {
  fileList.value = [];
  currentFile.value = null;
};

const handleImportCancel = () => {
  importDialogVisible.value = false;
  importForm.customerId = '';
  fileList.value = [];
  currentFile.value = null;
};

// const downloadTemplate = () => {
// //   if (!importForm.customerId) {
// //     ElMessage.warning('请先选择客户');
// //     return;
// //   }
  
// //   try {
// //     // 下载模板时也传递客户ID
// //     window.open(`/api/auth/sku/downloadTemplate?customerId=${importForm.customerId}`, '_blank');
// //     ElMessage.success('模板下载开始');
// //   } catch (error) {
// //     console.error('下载模板失败:', error);
// //     ElMessage.error('下载模板失败');
// //   }
// // 使用制表符\t分隔，Excel能更好识别
//   const tsvContent = "名称\t规格\t颜色\t用户SKU\t客户SKU\n" +
//                     "示例产品\t标准规格\t黑色\tSYSTEM_SKU_001\tCUSTOMER_SKU_001\n" +
//                     "测试商品\t大号\t红色\tSYSTEM_SKU_002\tCUSTOMER_SKU_002\n";
  
//   const blob = new Blob([tsvContent], { 
//     type: 'text/tab-separated-values;charset=utf-8' 
//   });
  
//   const url = window.URL.createObjectURL(blob);
//   const a = document.createElement('a');
//   a.href = url;
//   a.download = 'SKU导入模板.xlsx';
  
//   document.body.appendChild(a);
//   a.click();
//   document.body.removeChild(a);
//   window.URL.revokeObjectURL(url);
  
//   ElMessage.success('模板下载成功');
// };

const downloadTemplate = async () => {
  try {
    const response = await axios.get('/api/auth/sku/exportExcel', {
      headers: accessHeader(), // 如果需要认证
      responseType: 'blob', // ⚠️ 必须加
    });

    // 创建 blob 对象
    const blob = new Blob([response.data], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    });

    // 创建 URL 对象
    const url = window.URL.createObjectURL(blob);

    // 创建 a 标签下载
    const a = document.createElement('a');
    a.href = url;
    a.download = 'sku导入模版.xlsx'; // 可自定义文件名
    document.body.appendChild(a);
    a.click();
    a.remove();

    // 释放 URL
    window.URL.revokeObjectURL(url);

  } catch (error) {
    console.error('下载模板失败', error);
    ElMessage.error('下载模板失败，请稍后重试');
  }
};

const handleImportSubmit = async () => {
  if (!importForm.customerId) {
    ElMessage.warning('请选择客户');
    return;
  }

  if (!currentFile.value) {
    ElMessage.warning('请选择要上传的文件');
    return;
  }

  try {
    const formData = new FormData();

    // Element-Plus 上传组件通常文件在 raw 上
    const realFile = currentFile.value.raw || currentFile.value;
    formData.append('file', realFile);

    // 如果需要传 customerId，也加上
    formData.append('customerId', importForm.customerId);

    console.log('FormData:');
    for (let [key, val] of formData.entries()) {
      console.log(key, val);
    }

    ElMessage.info('开始导入数据，请稍候...');

    const result = await post('/api/auth/sku/import', formData);
    console.log('导入响应:', result);

    if (result) {
      ElMessage.success(`导入成功！`);
      handleSearch();
    } else {
      ElMessage.error('导入失败，请检查数据格式');
    }
  } catch (error) {
    console.error('导入失败详情:', error);
    ElMessage.error("导入失败，请重试");
  }
};


const resetForm = () => {
  Object.assign(form, {
    customerId: '',
    productSku: '',
    customerSku: '',
    remark: '',
    status: 1,
    productName: '',
    spec: '',
    color: '',
    customerName: ''
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
const loadCustomerList = async () => {
  try {
    const res = await get('/api/auth/customer/listEnable');
    customerList.value = res|| [];
  } catch (error) {
    console.error('加载客户列表失败:', error);
  }
};



const loadSkuMappingList = async () => {
  loading.value = true;
  try {
    const params = {
      page: pagination.current,
      size: pagination.size,
      ...filterForm
    };
    
    const res = await post('/api/auth/sku/pageList', params);
    skuMappingList.value = res.records || [];
    pagination.total = res.total || 0;
  } catch (error) {
    console.error('加载SKU映射列表失败:', error);
    ElMessage.error('加载SKU映射列表失败');
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  loadCustomerList();
  loadSkuMappingList();
});
</script>

<style scoped>
.sku-container {
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

.import-tips {
  margin-bottom: 20px;
}

.upload-demo {
  margin: 20px 0;
}

.download-template {
  text-align: center;
  margin-top: 10px;
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

:deep(.el-select-dropdown__item) {
  display: flex;
  justify-content: space-between;
}

:deep(.el-upload-dragger) {
  width: 100%;
}
</style>