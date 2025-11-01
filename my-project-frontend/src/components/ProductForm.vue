<template>
  <div class="product-form">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
    >
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="SKU编码" prop="sku">
            <el-input
              v-model="formData.sku"
              placeholder="请输入SKU编码"
              :disabled="isEdit"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="条形码">
            <el-input
              v-model="formData.barcode"
              placeholder="请输入条形码"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="产品名称" prop="name">
        <el-input
          v-model="formData.name"
          placeholder="请输入产品名称"
          maxlength="50"
          show-word-limit
        />
      </el-form-item>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="规格型号" prop="spec">
            <el-input
              v-model="formData.spec"
              placeholder="请输入规格型号"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="颜色">
            <el-input
              v-model="formData.color"
              placeholder="请输入颜色"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="产品分类" prop="categoryCode">
        <el-select
          v-model="formData.categoryCode"
          placeholder="请选择产品分类"
          style="width: 100%"
          filterable
          clearable
          :filter-method="filterCategory"
          @focus="loadCategoryList"
        >
          <el-option
            v-for="category in filteredCategoryList"
            :key="category.categoryCode"
            :label="getCategoryFullName(category)"
            :value="category.categoryCode"
          />
        </el-select>
        <el-button 
          type="text" 
          @click="handleAddCategory"
          style="margin-left: 10px;"
        >
          新增分类
        </el-button>
      </el-form-item>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="基础单位" prop="unitCode">
            <el-select
              v-model="formData.unitCode"
              placeholder="请选择基础单位"
              style="width: 100%"
              filterable
            >
              <el-option
                v-for="unit in unitList"
                :key="unit.unitCode"
                :label="`${unit.unitName} (${unit.unitCode})`"
                :value="unit.unitCode"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="出货单位">
            <el-select
              v-model="formData.outUnitCode"
              placeholder="请选择出货单位"
              style="width: 100%"
              clearable
              filterable
            >
              <el-option
                v-for="unit in unitList"
                :key="unit.unitCode"
                :label="`${unit.unitName} (${unit.unitCode})`"
                :value="unit.unitCode"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20" v-if="formData.outUnitCode">
        <el-col :span="12">
          <el-form-item label="单品重量">
            <el-input-number
              v-model="formData.weightPerUnit"
              :min="0"
              :precision="2"
              controls-position="right"
              style="width: 100%"
            >
              <template #append>kg</template>
            </el-input-number>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="出货单位包含基础单位数量">
            <el-input-number
              v-model="formData.outUnitPerNum"
              :min="1"
              :max="1000"
              controls-position="right"
              style="width: 100%"
            >
              <template #prepend>1{{ getUnitName(formData.outUnitCode) }} =</template>
              <template #append>{{ getUnitName(formData.unitCode) }}</template>
            </el-input-number>
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="最低库存">
        <el-input-number
          v-model="formData.minStock"
          :min="0"
          controls-position="right"
          style="width: 100%"
          placeholder="设置最低库存预警"
        />
      </el-form-item>

      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="formData.status">
          <el-radio :label="1">启用</el-radio>
          <el-radio :label="0">禁用</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="备注">
        <el-input
          v-model="formData.remark"
          type="textarea"
          :rows="3"
          placeholder="请输入备注信息"
          maxlength="255"
          show-word-limit
        />
      </el-form-item>

      <div class="form-actions">
        <el-button @click="$emit('cancel')">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="loading">
          {{ isEdit ? '更新' : '创建' }}
        </el-button>
      </div>
    </el-form>

    <!-- 新增分类对话框 -->
    <el-dialog
      v-model="categoryDialogVisible"
      :title="`新增产品分类`"
      width="500px"
      @close="handleCategoryDialogClose"
    >
      <el-form
        ref="categoryFormRef"
        :model="categoryForm"
        :rules="categoryFormRules"
        label-width="100px"
      >
        <el-form-item label="分类编码" prop="categoryCode">
          <el-input
            v-model="categoryForm.categoryCode"
            placeholder="请输入分类编码"
          />
        </el-form-item>
        <el-form-item label="分类名称" prop="categoryName">
          <el-input
            v-model="categoryForm.categoryName"
            placeholder="请输入分类名称"
          />
        </el-form-item>
        <el-form-item label="父级分类">
          <el-select
            v-model="categoryForm.parentCode"
            placeholder="请选择父级分类"
            style="width: 100%"
            filterable
            clearable
            :filter-method="filterParentCategory"
            @focus="loadCategoryList"
          >
            <el-option
              v-for="category in filteredParentCategoryList"
              :key="category.categoryCode"
              :label="getCategoryFullName(category)"
              :value="category.categoryCode"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="排序序号">
          <el-input-number
            v-model="categoryForm.sortOrder"
            :min="0"
            controls-position="right"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="categoryForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="categoryDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCategorySubmit" :loading="categoryLoading">
          确认
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { get, post } from '@/net';

const props = defineProps({
  formData: {
    type: Object,
    required: true
  },
  isEdit: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['success', 'cancel']);

// 表单相关
const formRef = ref();
const loading = ref(false);

// 数据列表
const categoryList = ref([]); // 平铺的分类列表
const unitList = ref([]);

// 搜索相关
const categorySearchText = ref('');
const parentCategorySearchText = ref('');

// 分类对话框相关
const categoryDialogVisible = ref(false);
const categoryFormRef = ref();
const categoryLoading = ref(false);
const categoryForm = ref({
  categoryCode: '',
  categoryName: '',
  parentCode: '',
  sortOrder: 0,
  status: 1
});

const formRules = {
  sku: [
    { required: true, message: '请输入SKU编码', trigger: 'blur' },
    { pattern: /^[A-Za-z0-9_-]+$/, message: 'SKU编码只能包含字母、数字、下划线和横线', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入产品名称', trigger: 'blur' }
  ],
  spec: [
    { required: true, message: '请输入规格型号', trigger: 'blur' }
  ],
  categoryCode: [
    { required: true, message: '请选择产品分类', trigger: 'change' }
  ],
  unitCode: [
    { required: true, message: '请选择基础单位', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
};

const categoryFormRules = {
  categoryCode: [
    { required: true, message: '请输入分类编码', trigger: 'blur' }
  ],
  categoryName: [
    { required: true, message: '请输入分类名称', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
};

// 计算属性 - 过滤后的分类列表
const filteredCategoryList = computed(() => {
  if (!categorySearchText.value) {
    return categoryList.value;
  }
  return categoryList.value.filter(category => 
    category.categoryName?.toLowerCase().includes(categorySearchText.value.toLowerCase()) ||
    category.categoryCode?.toLowerCase().includes(categorySearchText.value.toLowerCase())
  );
});

// 计算属性 - 过滤后的父级分类列表
const filteredParentCategoryList = computed(() => {
  if (!parentCategorySearchText.value) {
    return categoryList.value;
  }
  return categoryList.value.filter(category => 
    category.categoryName?.toLowerCase().includes(parentCategorySearchText.value.toLowerCase()) ||
    category.categoryCode?.toLowerCase().includes(parentCategorySearchText.value.toLowerCase())
  );
});

// 计算属性
const getUnitName = (unitCode) => {
  const unit = unitList.value.find(item => item.unitCode === unitCode);
  return unit ? unit.unitName : '';
};

// 方法
const loadCategoryList = async () => {
  try {
    const res = await get('/api/auth/product/categoryList');
    console.log('分类列表数据:', res);
    if (res && Array.isArray(res)) {
      categoryList.value = res;
    }
  } catch (error) {
    ElMessage.error('加载产品分类失败');
    console.error('加载产品分类失败:', error);
  }
};

const loadUnitList = async () => {
  try {
    const res = await get('/api/auth/product/unitList');
    if (res && Array.isArray(res)) {
      unitList.value = res;
    }
  } catch (error) {
    ElMessage.error('加载单位列表失败');
    console.error('加载单位列表失败:', error);
  }
};

// 获取分类完整名称（包含编码）
const getCategoryFullName = (category) => {
  return `${category.categoryName} (${category.categoryCode})`;
};

// 分类搜索方法
const filterCategory = (query) => {
  categorySearchText.value = query;
};

// 父级分类搜索方法
const filterParentCategory = (query) => {
  parentCategorySearchText.value = query;
};

const handleAddCategory = () => {
  categoryDialogVisible.value = true;
};

const handleCategoryDialogClose = () => {
  categoryFormRef.value?.resetFields();
  categoryForm.value = {
    categoryCode: '',
    categoryName: '',
    parentCode: '',
    sortOrder: 0,
    status: 1
  };
  categorySearchText.value = '';
  parentCategorySearchText.value = '';
};

const handleCategorySubmit = async () => {
  if (!categoryFormRef.value) return;
  
  try {
    await categoryFormRef.value.validate();
    
    categoryLoading.value = true;
    
    const submitData = {
      ...categoryForm.value
    };
    
    const res = await post('/api/auth/product/category/create', submitData);
    
    if (res) {
      ElMessage.success('新增分类成功');
      categoryDialogVisible.value = false;
      // 重新加载分类列表
      await loadCategoryList();
    }
  } catch (error) {
    if (error instanceof Error) {
      ElMessage.error('分类表单验证失败');
    } else {
      ElMessage.error('新增分类失败');
    }
  } finally {
    categoryLoading.value = false;
  }
};

const handleSubmit = async () => {
  if (!formRef.value) return;
  
  try {
    await formRef.value.validate();
    
    loading.value = true;
    
    const submitData = {
      ...props.formData
    };
    
    const url = props.isEdit ? '/api/auth/product/update' : '/api/auth/product/create';
    const res = await post(url, submitData);
    
    if (res) {
      ElMessage.success(props.isEdit ? '更新产品成功' : '创建产品成功');
      emit('success');
    }
  } catch (error) {
    if (error instanceof Error) {
      ElMessage.error('表单验证失败');
    } else {
      ElMessage.error(props.isEdit ? '更新产品失败' : '创建产品失败');
    }
  } finally {
    loading.value = false;
  }
};

// 监听器
watch(() => props.formData.outUnitCode, (newVal) => {
  if (newVal && !props.formData.outUnitPerNum) {
    props.formData.outUnitPerNum = 1;
  }
});

// 生命周期
onMounted(() => {
  loadCategoryList();
  loadUnitList();
});
</script>

<style scoped>
.product-form {
  max-height: 70vh;
  overflow-y: auto;
  padding-right: 10px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

:deep(.el-input-number) {
  width: 100%;
}

:deep(.el-select) {
  width: 100%;
}
</style>