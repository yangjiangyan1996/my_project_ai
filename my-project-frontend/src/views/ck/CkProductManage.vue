<template>
  <div class="product-container">
    <!-- 页面标题和操作栏 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">产品管理</h1>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>工作台</el-breadcrumb-item>
          <el-breadcrumb-item>基础数据</el-breadcrumb-item>
          <el-breadcrumb-item>产品管理</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增产品
        </el-button>
        <el-button @click="handleRefresh">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
        <el-button @click="handleImport">
          <el-icon><Upload /></el-icon>
          导入
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
        <el-form-item label="产品名称">
          <el-input
            v-model="filterForm.productName"
            placeholder="请输入产品名称"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="产品编码">
          <el-input
            v-model="filterForm.productCode"
            placeholder="请输入产品编码"
            clearable
            style="width: 180px"
          />
        </el-form-item>
        <el-form-item label="产品分类">
          <el-cascader
            v-model="filterForm.categoryId"
            :options="categoryOptions"
            :props="categoryProps"
            placeholder="请选择产品分类"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="产品型号">
          <el-input
            v-model="filterForm.model"
            placeholder="请输入产品型号"
            clearable
            style="width: 150px"
          />
        </el-form-item>
        <el-form-item label="品牌">
          <el-input
            v-model="filterForm.brand"
            placeholder="请输入品牌"
            clearable
            style="width: 150px"
          />
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

    <!-- 产品列表 -->
    <el-card class="table-card">
      <el-table
        :data="productList"
        v-loading="loading"
        style="width: 100%"
        empty-text="暂无产品数据"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="productCode" label="产品编码" width="140" />
        <el-table-column prop="productName" label="产品名称" min-width="180" />
        <el-table-column prop="categoryName" label="产品分类" width="120" />
        <el-table-column prop="model" label="产品型号" width="120" />
        <el-table-column prop="brand" label="品牌" width="100" />
        <el-table-column prop="specification" label="规格" width="120" />
        <el-table-column prop="unit" label="单位" width="80" align="center" />
        <el-table-column prop="purchasePrice" label="采购价" width="100" align="right">
          <template #default="scope">
            {{ formatCurrency(scope.row.purchasePrice) }}
          </template>
        </el-table-column>
        <el-table-column prop="salePrice" label="销售价" width="100" align="right">
          <template #default="scope">
            {{ formatCurrency(scope.row.salePrice) }}
          </template>
        </el-table-column>
        <el-table-column prop="currentStock" label="当前库存" width="100" align="center">
          <template #default="scope">
            <span :class="getStockClass(scope.row.currentStock, scope.row.minStock)">
              {{ scope.row.currentStock }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="minStock" label="最低库存" width="100" align="center" />
        <el-table-column prop="maxStock" label="最高库存" width="100" align="center" />
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
            <el-button type="warning" link @click="handleStock(scope.row)">
              库存
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
      width="800px"
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
            <el-form-item label="产品编码" prop="productCode">
              <el-input
                v-model="form.productCode"
                placeholder="请输入产品编码"
                :disabled="isEditMode"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="产品名称" prop="productName">
              <el-input
                v-model="form.productName"
                placeholder="请输入产品名称"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="产品分类" prop="categoryId">
              <el-cascader
                v-model="form.categoryId"
                :options="categoryOptions"
                :props="categoryProps"
                placeholder="请选择产品分类"
                style="width: 100%"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="品牌" prop="brand">
              <el-input
                v-model="form.brand"
                placeholder="请输入品牌"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="产品型号" prop="model">
              <el-input
                v-model="form.model"
                placeholder="请输入产品型号"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="规格" prop="specification">
              <el-input
                v-model="form.specification"
                placeholder="请输入规格"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="单位" prop="unit">
              <el-select v-model="form.unit" placeholder="请选择单位" style="width: 100%">
                <el-option label="个" value="个" />
                <el-option label="台" value="台" />
                <el-option label="件" value="件" />
                <el-option label="套" value="套" />
                <el-option label="箱" value="箱" />
                <el-option label="千克" value="千克" />
                <el-option label="米" value="米" />
                <el-option label="升" value="升" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="颜色" prop="color">
              <el-input
                v-model="form.color"
                placeholder="请输入颜色"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="采购价" prop="purchasePrice">
              <el-input-number
                v-model="form.purchasePrice"
                placeholder="请输入采购价"
                :min="0"
                :precision="2"
                :step="0.01"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="销售价" prop="salePrice">
              <el-input-number
                v-model="form.salePrice"
                placeholder="请输入销售价"
                :min="0"
                :precision="2"
                :step="0.01"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="最低库存" prop="minStock">
              <el-input-number
                v-model="form.minStock"
                placeholder="请输入最低库存"
                :min="0"
                :step="1"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最高库存" prop="maxStock">
              <el-input-number
                v-model="form.maxStock"
                placeholder="请输入最高库存"
                :min="0"
                :step="1"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="产品描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入产品描述"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="2"
            placeholder="请输入备注信息"
            maxlength="200"
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
      title="产品详情"
      width="700px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="产品编码">{{ currentRow?.productCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="产品名称">{{ currentRow?.productName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="产品分类">{{ currentRow?.categoryName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="品牌">{{ currentRow?.brand || '-' }}</el-descriptions-item>
        <el-descriptions-item label="产品型号">{{ currentRow?.model || '-' }}</el-descriptions-item>
        <el-descriptions-item label="规格">{{ currentRow?.specification || '-' }}</el-descriptions-item>
        <el-descriptions-item label="单位">{{ currentRow?.unit || '-' }}</el-descriptions-item>
        <el-descriptions-item label="颜色">{{ currentRow?.color || '-' }}</el-descriptions-item>
        <el-descriptions-item label="采购价">{{ formatCurrency(currentRow?.purchasePrice) }}</el-descriptions-item>
        <el-descriptions-item label="销售价">{{ formatCurrency(currentRow?.salePrice) }}</el-descriptions-item>
        <el-descriptions-item label="当前库存">
          <span :class="getStockClass(currentRow?.currentStock, currentRow?.minStock)">
            {{ currentRow?.currentStock || 0 }}
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="最低库存">{{ currentRow?.minStock || 0 }}</el-descriptions-item>
        <el-descriptions-item label="最高库存">{{ currentRow?.maxStock || 0 }}</el-descriptions-item>
        <el-descriptions-item label="状态" :span="2">
          <el-tag :type="currentRow?.status === 1 ? 'success' : 'danger'">
            {{ currentRow?.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="产品描述" :span="2">{{ currentRow?.description || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentRow?.remark || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatTime(currentRow?.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ formatTime(currentRow?.updatedAt) }}</el-descriptions-item>
      </el-descriptions>

      <template #footer>
        <el-button @click="viewDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 库存管理对话框 -->
    <el-dialog
      v-model="stockDialogVisible"
      title="产品库存管理"
      width="600px"
    >
      <el-form :model="stockForm" label-width="100px">
        <el-form-item label="产品名称">
          <el-input :value="currentRow?.productName" disabled />
        </el-form-item>
        <el-form-item label="当前库存">
          <el-input :value="currentRow?.currentStock" disabled />
        </el-form-item>
        <el-form-item label="操作类型" prop="operationType">
          <el-radio-group v-model="stockForm.operationType">
            <el-radio label="adjust">库存调整</el-radio>
            <el-radio label="set">设置库存</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="调整数量" prop="quantity" v-if="stockForm.operationType === 'adjust'">
          <el-input-number
            v-model="stockForm.quantity"
            placeholder="请输入调整数量"
            :min="-999999"
            :max="999999"
            style="width: 100%"
          />
          <div class="form-tip">正数表示增加库存，负数表示减少库存</div>
        </el-form-item>
        <el-form-item label="设置库存" prop="newStock" v-if="stockForm.operationType === 'set'">
          <el-input-number
            v-model="stockForm.newStock"
            placeholder="请输入新的库存数量"
            :min="0"
            :max="999999"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="调整原因" prop="reason">
          <el-input
            v-model="stockForm.reason"
            type="textarea"
            :rows="3"
            placeholder="请输入库存调整原因"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="stockDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleUpdateStock">确认调整</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Refresh, Upload, Download } from '@element-plus/icons-vue';
import { post, get } from '@/net';

// 响应式数据
const loading = ref(false);
const dialogVisible = ref(false);
const viewDialogVisible = ref(false);
const stockDialogVisible = ref(false);
const submitLoading = ref(false);
const isEditMode = ref(false);
const currentRow = ref(null);
const selectedRows = ref([]);

// 筛选表单
const filterForm = reactive({
  productName: '',
  productCode: '',
  categoryId: [],
  model: '',
  brand: '',
  status: ''
});

// 分页
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
});

// 产品列表
const productList = ref([]);

// 分类选项
const categoryOptions = ref([
  {
    value: '1',
    label: '电子产品',
    children: [
      { value: '11', label: '手机' },
      { value: '12', label: '电脑' },
      { value: '13', label: '平板' }
    ]
  },
  {
    value: '2',
    label: '办公用品',
    children: [
      { value: '21', label: '文具' },
      { value: '22', label: '打印设备' },
      { value: '23', label: '办公家具' }
    ]
  },
  {
    value: '3',
    label: '工业原料',
    children: [
      { value: '31', label: '金属材料' },
      { value: '32', label: '化工原料' },
      { value: '33', label: '塑料原料' }
    ]
  }
]);

const categoryProps = {
  value: 'value',
  label: 'label',
  children: 'children',
  checkStrictly: true
};

// 表单数据和验证规则
const formRef = ref();
const form = reactive({
  productCode: '',
  productName: '',
  categoryId: [],
  brand: '',
  model: '',
  specification: '',
  unit: '个',
  color: '',
  purchasePrice: 0,
  salePrice: 0,
  minStock: 0,
  maxStock: 0,
  description: '',
  remark: '',
  status: 1
});

const formRules = {
  productCode: [
    { required: true, message: '请输入产品编码', trigger: 'blur' },
    { min: 2, max: 50, message: '产品编码长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  productName: [
    { required: true, message: '请输入产品名称', trigger: 'blur' },
    { min: 2, max: 100, message: '产品名称长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择产品分类', trigger: 'change' }
  ],
  unit: [
    { required: true, message: '请选择单位', trigger: 'change' }
  ],
  purchasePrice: [
    { required: true, message: '请输入采购价', trigger: 'blur' },
    { type: 'number', min: 0, message: '采购价不能为负数', trigger: 'blur' }
  ],
  salePrice: [
    { required: true, message: '请输入销售价', trigger: 'blur' },
    { type: 'number', min: 0, message: '销售价不能为负数', trigger: 'blur' }
  ]
};

// 库存表单
const stockForm = reactive({
  operationType: 'adjust',
  quantity: 0,
  newStock: 0,
  reason: ''
});

// 计算属性
const dialogTitle = computed(() => {
  return isEditMode.value ? '编辑产品' : '新增产品';
});

// 方法
const handleSearch = () => {
  pagination.current = 1;
  loadProductList();
};

const handleReset = () => {
  Object.assign(filterForm, {
    productName: '',
    productCode: '',
    categoryId: [],
    model: '',
    brand: '',
    status: ''
  });
  pagination.current = 1;
  loadProductList();
};

const handleSizeChange = (size) => {
  pagination.size = size;
  pagination.current = 1;
  loadProductList();
};

const handleCurrentChange = (current) => {
  pagination.current = current;
  loadProductList();
};

const handleRefresh = () => {
  loadProductList();
};

const handleImport = () => {
  ElMessage.info('导入功能开发中...');
};

const handleExport = () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要导出的产品');
    return;
  }
  
  ElMessage.info('导出功能开发中...');
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
    productCode: row.productCode,
    productName: row.productName,
    categoryId: row.categoryId ? row.categoryId.split(',') : [],
    brand: row.brand,
    model: row.model,
    specification: row.specification,
    unit: row.unit,
    color: row.color,
    purchasePrice: row.purchasePrice,
    salePrice: row.salePrice,
    minStock: row.minStock,
    maxStock: row.maxStock,
    description: row.description,
    remark: row.remark,
    status: row.status
  });
  
  dialogVisible.value = true;
};

const handleView = (row) => {
  currentRow.value = row;
  viewDialogVisible.value = true;
};

const handleStock = (row) => {
  currentRow.value = row;
  stockForm.operationType = 'adjust';
  stockForm.quantity = 0;
  stockForm.newStock = row.currentStock;
  stockForm.reason = '';
  stockDialogVisible.value = true;
};

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除产品"${row.productName}"吗？`,
      '删除确认',
      {
        type: 'warning'
      }
    );
    
    await post('/api/auth/product/delete', { id: row.id });
    ElMessage.success('删除成功');
    loadProductList();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败');
    }
  }
};

const handleStatusChange = async (row) => {
  try {
    await post('/api/auth/product/updateStatus', {
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
    const submitData = {
      ...form,
      categoryId: form.categoryId.join(',')
    };
    
    if (isEditMode.value) {
      await post('/api/auth/product/update', {
        ...submitData,
        id: currentRow.value.id
      });
      ElMessage.success('更新成功');
    } else {
      await post('/api/auth/product/create', submitData);
      ElMessage.success('创建成功');
    }
    
    dialogVisible.value = false;
    loadProductList();
  } catch (error) {
    console.error('保存产品失败:', error);
    ElMessage.error('保存失败');
  } finally {
    submitLoading.value = false;
  }
};

const handleUpdateStock = async () => {
  if (!stockForm.reason) {
    ElMessage.warning('请输入调整原因');
    return;
  }

  try {
    const params = {
      productId: currentRow.value.id,
      reason: stockForm.reason
    };

    if (stockForm.operationType === 'adjust') {
      params.quantity = stockForm.quantity;
      params.operationType = 'adjust';
    } else {
      params.newStock = stockForm.newStock;
      params.operationType = 'set';
    }

    await post('/api/auth/product/adjustStock', params);
    
    ElMessage.success('库存调整成功');
    stockDialogVisible.value = false;
    loadProductList();
  } catch (error) {
    console.error('调整库存失败:', error);
    ElMessage.error('调整失败');
  }
};

const resetForm = () => {
  Object.assign(form, {
    productCode: '',
    productName: '',
    categoryId: [],
    brand: '',
    model: '',
    specification: '',
    unit: '个',
    color: '',
    purchasePrice: 0,
    salePrice: 0,
    minStock: 0,
    maxStock: 0,
    description: '',
    remark: '',
    status: 1
  });
  
  if (formRef.value) {
    formRef.value.clearValidate();
  }
};

// 工具方法
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

const getStockClass = (currentStock, minStock) => {
  if (currentStock <= 0) {
    return 'text-danger';
  } else if (currentStock <= minStock) {
    return 'text-warning';
  }
  return '';
};

// API 调用
const loadProductList = async () => {
  loading.value = true;
  try {
    const params = {
      page: pagination.current,
      size: pagination.size,
      ...filterForm,
      categoryId: filterForm.categoryId.length > 0 ? filterForm.categoryId[filterForm.categoryId.length - 1] : ''
    };
    
    const res = await post('/api/auth/product/list', params);
    productList.value = res.records || [];
    pagination.total = res.total || 0;
  } catch (error) {
    console.error('加载产品列表失败:', error);
    ElMessage.error('加载产品列表失败');
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  loadProductList();
});
</script>

<style scoped>
.product-container {
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

.text-warning {
  color: #e6a23c;
  font-weight: bold;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
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