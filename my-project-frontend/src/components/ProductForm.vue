<template>
  <div class="product-form">
    <el-form
      ref="formRef"
      :model="formModel"
      :rules="formRules"
      label-width="100px"
    >
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="SKU编码" prop="sku">
            <el-input
              v-model="formModel.sku"
              placeholder="请输入SKU编码"
              :disabled="isEdit"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="条形码">
            <el-input
              v-model="formModel.barcode"
              placeholder="请输入条形码"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="产品名称" prop="name">
        <el-input
          v-model="formModel.name"
          placeholder="请输入产品名称"
          maxlength="50"
          show-word-limit
        />
      </el-form-item>

            <!-- 添加英文名字段 -->
      <el-form-item label="英文名称">
        <el-input
          v-model="formModel.englishName"
          placeholder="请输入产品英文名称"
          maxlength="100"
          show-word-limit
        />
      </el-form-item>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="规格型号" prop="spec">
            <el-input
              v-model="formModel.spec"
              placeholder="请输入规格型号"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="颜色">
            <el-input
              v-model="formModel.color"
              placeholder="请输入颜色"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="产品分类" prop="categoryCode">
        <el-select
          v-model="formModel.categoryCode"
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
              v-model="formModel.unitCode"
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
              v-model="formModel.outUnitCode"
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

      <!-- 单品重量和出货单位包含数量 - 一直显示 -->
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="单品重量（kg）">
            <el-input-number
              v-model="formModel.weightPerUnit"
              :min="0"
              :precision="2"
              controls-position="right"
              style="width: 100%"
              placeholder="请输入单品重量"
            >
              <template #append>kg</template>
            </el-input-number>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="出货单位包含基础单位数量">
            <el-input-number
              v-model="formModel.outUnitPerNum"
              :min="1"
              :max="1000"
              controls-position="right"
              style="width: 100%"
              :disabled="!formModel.outUnitCode"
            >
              <template #prepend>1{{ getUnitName(formModel.outUnitCode) }} =</template>
              <template #append>{{ getUnitName(formModel.unitCode) }}</template>
            </el-input-number>
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 出货单位体积（长宽高） -->
      <el-row :gutter="20" v-if="formModel.outUnitCode">
        <el-col :span="8">
          <el-form-item label="长度(cm)">
            <el-input-number
              v-model="formModel.outUnitLength"
              :min="0"
              :precision="2"
              controls-position="right"
              style="width: 100%"
              placeholder="长度"
            >
              <template #append>cm</template>
            </el-input-number>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="宽度(cm)">
            <el-input-number
              v-model="formModel.outUnitWidth"
              :min="0"
              :precision="2"
              controls-position="right"
              style="width: 100%"
              placeholder="宽度"
            >
              <template #append>cm</template>
            </el-input-number>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="高度(cm)">
            <el-input-number
              v-model="formModel.outUnitHeight"
              :min="0"
              :precision="2"
              controls-position="right"
              style="width: 100%"
              placeholder="高度"
            >
              <template #append>cm</template>
            </el-input-number>
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 体积计算结果 -->
      <el-row :gutter="20" v-if="formModel.outUnitCode && (formModel.outUnitLength || formModel.outUnitWidth || formModel.outUnitHeight)">
        <el-col :span="24">
          <el-form-item label="总体积">
            <el-input
              :value="calculateVolume"
              readonly
              style="width: 100%"
            >
              <template #append>cm³</template>
            </el-input>
            <div class="volume-tip" v-if="calculateVolume > 0">
              <span class="tip-text">约 {{ formatVolume(calculateVolume) }}</span>
            </div>
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="最低库存">
        <el-input-number
          v-model="formModel.minStock"
          :min="0"
          controls-position="right"
          style="width: 100%"
          placeholder="设置最低库存预警"
        />
      </el-form-item>

      <!-- BOM配方设置 -->
      <el-form-item label="产品配方">
        <div class="bom-section">
          <div class="bom-header">
            <span>设置产品组成配方</span>
            <el-button type="primary" @click="handleAddComponent" :icon="Plus" size="small">
              添加配件
            </el-button>
          </div>
          
          <el-table
            :data="formModel.bomDetails"
            border
            class="bom-table"
            empty-text="暂无配件，请添加"
          >
            <el-table-column type="index" label="序号" width="60" align="center" />
            <el-table-column label="配件产品" min-width="200">
              <template #default="{ row, $index }">
                <el-select
                  v-model="row.componentProductId"
                  placeholder="选择配件产品"
                  style="width: 100%"
                  filterable
                  @change="(value) => handleComponentChange(value, $index)"
                  :disabled="isEdit"
                >
                  <el-option
                    v-for="product in availableComponentProducts"
                    :key="product.id"
                    :label="`${product.sku} - ${product.name}`"
                    :value="product.id"
                    :disabled="isComponentSelected(product.id)"
                  />
                </el-select>
              </template>
            </el-table-column>

            <el-table-column label="类型" width="120" align="center">
              <template #default="{ row, $index }">
                <el-select
                  v-model="row.type"
                  placeholder="选择类型"
                  style="width: 100%"
                  
                >
                  <el-option label="空白标签" :value="0" />
                  <el-option label="主料" :value="1" />
                  <el-option label="布料" :value="2" />
                  <el-option label="辅料" :value="10" />
                  <el-option label="五金" :value="20" />
                  <el-option label="包装" :value="999" />
                </el-select>
              </template>
            </el-table-column>

            <el-table-column label="规格型号" width="120">
              <template #default="{ row }">
                {{ row.componentProductSpec || '-' }}
              </template>
            </el-table-column>
            <el-table-column label="单位" width="80" align="center">
              <template #default="{ row }">
                {{ row.componentProductUnit || '-' }}
              </template>
            </el-table-column>
            
            <el-table-column label="所需数量" width="120">
              <template #default="{ row, $index }">
                <el-input-number
                  v-model="row.quantity"
                  :min="0.0001"
                  :precision="4"
                  :step="0.0001"
                  controls-position="right"
                  style="width: 100%"
                  placeholder="数量"
                />
              </template>
            </el-table-column>
            <el-table-column label="损耗率" width="120">
              <template #default="{ row, $index }">
                <el-input-number
                  v-model="row.lossRate"
                  :min="0"
                  :max="100"
                  :precision="2"
                  controls-position="right"
                  style="width: 100%"
                >
                  <template #append>%</template>
                </el-input-number>
              </template>
            </el-table-column>
            <el-table-column label="备注" min-width="150">
              <template #default="{ row, $index }">
                <el-input
                  v-model="row.remark"
                  placeholder="配件备注"
                  maxlength="255"
                />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80" fixed="right" align="center" v-if="!isEdit">
              <template #default="{ $index }">
                <el-button
                  type="danger"
                  link
                  :icon="Delete"
                  @click="handleRemoveComponent($index)"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 配方统计 -->
          <div class="bom-summary" v-if="formModel.bomDetails.length > 0">
            <el-row :gutter="20">
              <el-col :span="8">
                <div class="summary-item">
                  <span class="label">配件种类：</span>
                  <span class="value">{{ formModel.bomDetails.length }} 种</span>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="summary-item">
                  <span class="label">总数量：</span>
                  <span class="value">{{ totalComponentQuantity }}</span>
                </div>
              </el-col>
            </el-row>
          </div>
        </div>
      </el-form-item>

      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="formModel.status">
          <el-radio :label="1">启用</el-radio>
          <el-radio :label="0">禁用</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="备注">
        <el-input
          v-model="formModel.remark"
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
import { ref, reactive, computed, onMounted, watch, watchEffect } from 'vue';
import { ElMessage } from 'element-plus';
import { Plus, Delete } from '@element-plus/icons-vue';
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
const categoryList = ref([]);
const unitList = ref([]);
const componentProductList = ref([]); // 所有可用作配件的产品

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

// 响应式表单模型
const formModel = reactive({
  id: '',
  sku: '',
  barcode: '',
  name: '',
  englishName: '', // 新增英文名字段
  spec: '',
  categoryCode: '',
  unitCode: '',
  outUnitCode: '',
  outUnitPerNum: 1,
  weightPerUnit: 0,
  // 出货单位体积字段
  outUnitLength: 0,
  outUnitWidth: 0,
  outUnitHeight: 0,
  color: '',
  minStock: 0,
  remark: '',
  status: 1,
  bomDetails: []
});

// 监听 props.formData 的变化，更新 formModel
watchEffect(() => {
  if (props.formData) {
    const formData = { ...props.formData };
    console.log('原始 formData:', formData);
    // 统一处理 BOM 数据字段
    if (!formData.bomDetails) {
      if (formData.bomData && Array.isArray(formData.bomData)) {
        // 将 bomData 转换为 bomDetails 格式
        formData.bomDetails = formData.bomData.map(item => ({
          componentProductId: item.componentProductId,
          componentProductName: item.componentProductName,
          componentProductSku: item.componentProductSku,
          componentProductSpec: item.componentProductSpec,
          componentProductUnit: item.componentProductUnit,
          quantity: item.quantity,
          type: item.type || 1, // 确保类型字段有值
          lossRate: item.lossRate,
          remark: item.remark,
          sortOrder: item.sortOrder
        }));
      } else {
        formData.bomDetails = [];
      }
    }
    
    // 更新 formModel
    Object.keys(formModel).forEach(key => {
      if (formData[key] !== undefined) {
        formModel[key] = formData[key];
      }
    });
    
    console.log('表单数据初始化:', formModel);
  }
});

const formRules = {
  sku: [
    { required: true, message: '请输入SKU编码', trigger: 'blur' },
    { pattern: /^[A-Za-z0-9_-]+$/, message: 'SKU编码只能包含字母、数字、下划线和横线', trigger: 'blur' }
  ],
    englishName: [
    { required: true, message: '请输入英文名称', trigger: 'blur' },
    { pattern: /^[A-Za-z0-9_]+$/, message: '英文名称只能包含字母、数字和下划线', trigger: 'blur' }
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

// 计算属性 - 可用作配件的产品列表（排除自身）
const availableComponentProducts = computed(() => {
  return componentProductList.value.filter(product => 
    product.status === 1 // 只显示启用状态的产品
  );
});

// 计算属性 - 配件总数量
const totalComponentQuantity = computed(() => {
  return formModel.bomDetails.reduce((sum, item) => {
    return sum + (parseFloat(item.quantity) || 0);
  }, 0).toFixed(4);
});

// 计算属性 - 计算体积
const calculateVolume = computed(() => {
  const length = parseFloat(formModel.outUnitLength) || 0;
  const width = parseFloat(formModel.outUnitWidth) || 0;
  const height = parseFloat(formModel.outUnitHeight) || 0;
  return length * width * height;
});

// 方法 - 格式化体积显示
const formatVolume = (volume) => {
  if (volume >= 1000000) {
    return `${(volume / 1000000).toFixed(2)} m³`;
  } else if (volume >= 1000) {
    return `${(volume / 1000).toFixed(2)} L`;
  }
  return `${volume.toFixed(2)} cm³`;
};

// 计算属性
const getUnitName = (unitCode) => {
  const unit = unitList.value.find(item => item.unitCode === unitCode);
  return unit ? unit.unitName : '';
};

// 方法 - 检查配件是否已被选择
const isComponentSelected = (productId) => {
  return formModel.bomDetails.some(item => item.componentProductId === productId);
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

const loadComponentProductList = async () => {
  try {
    const res = await get('/api/auth/product/listEnable');
    componentProductList.value = res || [];
  } catch (error) {
    console.error('加载配件产品列表失败:', error);
    componentProductList.value = [];
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

// BOM相关方法
const handleAddComponent = () => {
  formModel.bomDetails.push({
    componentProductId: null,
    componentProductName: '',
    componentProductSku: '',
    componentProductSpec: '',
    componentProductUnit: '',
    quantity: 1,
    type: 1, // 默认选择主料
    lossRate: 0,
    remark: '',
    sortOrder: formModel.bomDetails.length
  });
};

const handleRemoveComponent = (index) => {
  formModel.bomDetails.splice(index, 1);
  // 重新排序
  formModel.bomDetails.forEach((item, idx) => {
    item.sortOrder = idx;
  });
};

const handleComponentChange = (productId, index) => {
  const product = componentProductList.value.find(p => p.id === productId);
  if (product) {
    const detail = formModel.bomDetails[index];
    detail.componentProductName = product.name;
    detail.componentProductSku = product.sku;
    detail.componentProductSpec = product.spec;
    detail.componentProductUnit = product.unitName;
  }
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
    
    // 验证BOM数据
    if (formModel.bomDetails.length > 0) {
      for (let i = 0; i < formModel.bomDetails.length; i++) {
        const detail = formModel.bomDetails[i];
        if (!detail.componentProductId) {
          ElMessage.warning(`请选择第 ${i + 1} 行的配件产品`);
          return;
        }
        if (!detail.quantity || detail.quantity <= 0) {
          ElMessage.warning(`请输入第 ${i + 1} 行配件的有效数量`);
          return;
        }
         // 新增类型验证
        if (detail.type === undefined || detail.type === null) {
          ElMessage.warning(`请选择第 ${i + 1} 行配件的类型`);
          return;
        }
      }
    }
    
    loading.value = true;
    console.log('提交数据:', formModel);
    
    const submitData = {
      // 编辑时带上 ID
      id: formModel.id,
      sku: formModel.sku,
      barcode: formModel.barcode,
      name: formModel.name,
      englishName: formModel.englishName, // 新增英文名
      spec: formModel.spec,
      categoryCode: formModel.categoryCode,
      color: formModel.color,
      minStock: formModel.minStock,
      remark: formModel.remark,
      status: formModel.status,
      unitCode: formModel.unitCode,
      outUnitCode: formModel.outUnitCode,
      outUnitPerNum: formModel.outUnitPerNum,
      weightPerUnit: formModel.weightPerUnit,
      // 出货单位体积字段
      outUnitLength: formModel.outUnitLength,
      outUnitWidth: formModel.outUnitWidth,
      outUnitHeight: formModel.outUnitHeight,
      // ✅ 只传 bomData
      bomData: formModel.bomDetails.length > 0 ? {
        bomCode: `${formModel.sku}_BOM`,
        version: 'V1.0',
        status: 1,
        remark: `${formModel.name}的默认配方`,
        details: formModel.bomDetails.map(detail => ({
          componentProductId: detail.componentProductId,
          quantity: detail.quantity,
          lossRate: detail.lossRate,
          type: detail.type, // 新增类型字段
          remark: detail.remark,
          sortOrder: detail.sortOrder
        }))
      } : null
    };

    console.log('最终提交数据:', submitData);
    
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
watch(() => formModel.outUnitCode, (newVal) => {
  if (newVal && !formModel.outUnitPerNum) {
    formModel.outUnitPerNum = 1;
  }
});

// 生命周期
onMounted(() => {
  loadCategoryList();
  loadUnitList();
  loadComponentProductList();
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

/* BOM配方样式 */
.bom-section {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 16px;
  background-color: #f8f9fa;
}

.bom-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.bom-header span {
  font-weight: 500;
  color: #303133;
}

.bom-table {
  margin-bottom: 12px;
}

.bom-summary {
  padding: 12px;
  background-color: #fff;
  border-radius: 4px;
  border: 1px solid #ebeef5;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.summary-item .label {
  color: #606266;
  font-size: 14px;
}

.summary-item .value {
  color: #303133;
  font-weight: bold;
  font-size: 14px;
}

/* 体积提示样式 */
.volume-tip {
  margin-top: 4px;
}

.tip-text {
  font-size: 12px;
  color: #909399;
  font-style: italic;
}

:deep(.el-input-number) {
  width: 100%;
}

:deep(.el-select) {
  width: 100%;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .bom-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
  
  .bom-header .el-button {
    align-self: flex-end;
  }
}
</style>