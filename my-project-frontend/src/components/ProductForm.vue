<template>
  <div class="product-form">
    <div class="form-header">
      <h3 class="form-title">{{ isEdit ? '编辑产品' : '创建产品' }}</h3>
      <p class="form-subtitle">请填写产品详细信息，带 <span class="required-mark">*</span> 的为必填项</p>
    </div>
    
    <el-form
      ref="formRef"
      :model="formModel"
      :rules="formRules"
      label-width="120px"
      label-position="left"
      class="advanced-form"
    >
      <!-- 基础信息卡片 -->
      <div class="form-card">
        <div class="card-header">
          <span class="card-title">基础信息</span>
        </div>
        <div class="card-content">
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="SKU编码" prop="sku" required>
                <div class="form-item-container">
                  <el-input
                    v-model="formModel.sku"
                    placeholder="请输入SKU编码"
                    :disabled="isEdit"
                  />
                  <div class="form-item-tip">产品的唯一编码标识</div>
                </div>
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

          <el-form-item label="产品名称" prop="name" required>
            <el-input
              v-model="formModel.name"
              placeholder="请输入产品名称"
              maxlength="50"
              show-word-limit
            />
          </el-form-item>

          <el-form-item label="英文名称" prop="englishName" required>
            <div class="form-item-container">
              <el-input
                v-model="formModel.englishName"
                placeholder="请输入产品英文名称"
                maxlength="100"
                show-word-limit
              />
              <div class="form-item-tip">用于国际业务和标识</div>
            </div>
          </el-form-item>

          <el-row :gutter="24">
            <el-col :span="12">
              <!-- 规格型号 -->
              <el-form-item label="规格型号" prop="spec" required>
                <el-input
                  v-model="formModel.spec"
                  placeholder="请输入规格型号"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <!-- 颜色 -->
              <el-form-item label="颜色" prop="color" required>
                <el-input
                  v-model="formModel.color"
                  placeholder="请输入颜色"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </div>
      </div>

      <!-- 分类与单位卡片 -->
      <div class="form-card">
        <div class="card-header">
          <span class="card-title">分类与单位</span>
        </div>
        <div class="card-content">
          <!-- 产品分类 -->
          <el-form-item label="产品分类" prop="categoryCode" required>
            <div class="form-item-with-action">
              <el-select
                v-model="formModel.categoryCode"
                placeholder="请选择产品分类"
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
                class="action-button"
              >
                + 新增分类
              </el-button>
            </div>
          </el-form-item>

          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="基础单位" prop="unitCode">
                <el-select
                  v-model="formModel.unitCode"
                  placeholder="请选择基础单位"
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

          <!-- 单品重量和出货单位包含数量 -->
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="单品重量（kg）">
                <div class="form-item-container">
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
                  <div class="form-item-tip">单个产品的重量</div>
                </div>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="出货单位包含数量">
                <div class="conversion-display">
                  <div class="conversion-wrapper">
                    <span class="conversion-label">1{{ getUnitName(formModel.outUnitCode) }} =</span>
                    <el-input-number
                      v-model="formModel.outUnitPerNum"
                      :min="1"
                      :max="1000"
                      controls-position="right"
                      style="width: 180px"
                      :disabled="!formModel.outUnitCode"
                    />
                    <span class="conversion-label">{{ getUnitName(formModel.unitCode) }}</span>
                  </div>
                  <div class="form-item-tip">出货单位包含的基础单位数量</div>
                </div>
              </el-form-item>
            </el-col>
          </el-row>

          <!-- 出货单位体积（长宽高） -->
          <div v-if="formModel.outUnitCode" class="dimensions-section">
            <div class="section-header">
              <span class="section-title">出货单位尺寸</span>
            </div>
            <el-row :gutter="24">
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
            <div v-if="calculateVolume > 0" class="volume-result">
              <div class="volume-card">
                <div class="volume-header">
                  <span class="volume-title">体积计算结果</span>
                </div>
                <div class="volume-content">
                  <div class="volume-value">
                    <span class="value">{{ calculateVolume.toFixed(2) }}</span>
                    <span class="unit">cm³</span>
                  </div>
                  <div class="volume-converted">
                    约 {{ formatVolume(calculateVolume) }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 库存与状态卡片 -->
      <div class="form-card">
        <div class="card-header">
          <span class="card-title">库存与状态</span>
        </div>
        <div class="card-content">
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="最低库存">
                <div class="form-item-container">
                  <el-input-number
                    v-model="formModel.minStock"
                    :min="0"
                    controls-position="right"
                    style="width: 100%"
                    placeholder="设置最低库存预警"
                  />
                  <div class="form-item-tip">库存低于此值将触发预警</div>
                </div>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="状态" prop="status">
                <el-radio-group v-model="formModel.status">
                  <el-radio :label="1" class="status-radio active">启用</el-radio>
                  <el-radio :label="0" class="status-radio inactive">禁用</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
          </el-row>
        </div>
      </div>

      <!-- BOM配方卡片 -->
      <div class="form-card">
        <div class="card-header">
          <span class="card-title">产品配方 (BOM)</span>
          <el-button 
            type="primary" 
            @click="handleAddComponent" 
            :icon="Plus" 
            size="small"
            class="header-button"
          >
            添加配件
          </el-button>
        </div>
        <div class="card-content">
          <div class="bom-section">
            <div v-if="formModel.bomDetails.length === 0" class="empty-bom">
              <div class="empty-icon">
                <el-icon><document /></el-icon>
              </div>
              <p>暂无配方配置</p>
              <p class="empty-tip">点击上方按钮添加配件产品</p>
            </div>
            
            <div v-else>
              <el-table
                :data="formModel.bomDetails"
                border
                class="bom-table"
                empty-text="暂无配件"
              >
                <el-table-column type="index" label="序号" width="80" align="center">
                  <template #default="{ $index }">
                    <div class="index-badge">{{ $index + 1 }}</div>
                  </template>
                </el-table-column>
                
                <el-table-column label="配件产品" min-width="240">
                  <template #default="{ row, $index }">
                    <div class="product-selector">
                      <el-select
                        v-model="row.componentProductId"
                        placeholder="选择配件产品"
                        style="width: 100%"
                        filterable
                        @change="(value) => handleComponentChange(value, $index)"
                      >
                        <el-option
                          v-for="product in availableComponentProducts"
                          :key="product.id"
                          :label="`${product.name}${product.sku ? ` (${product.sku})` : ''}`"
                          :value="product.id"
                          :disabled="isComponentSelected(product.id)"
                        />
                      </el-select>
                      <div v-if="row.componentProductSpec || row.componentProductColor" class="product-details">
                        <span v-if="row.componentProductSpec">{{ row.componentProductSpec }}</span>
                        <span v-if="row.componentProductColor" class="color-tag">{{ row.componentProductColor }}</span>
                      </div>
                    </div>
                  </template>
                </el-table-column>

                <el-table-column label="类型" width="120" align="center">
                  <template #default="{ row, $index }">
                    <el-select
                      v-model="row.type"
                      placeholder="选择类型"
                      style="width: 100%"
                      @change="(value) => handleTypeChange(value, $index)"
                    >
                      <el-option label="空白标签" :value="0" />
                      <el-option label="主料" :value="1" />
                      <el-option label="布料" :value="2" />
                      <el-option label="辅料" :value="10" />
                      <el-option label="五金" :value="20" />
                      <el-option label="包装" :value="100" />
                    </el-select>
                  </template>
                </el-table-column>

                <el-table-column label="包装比例" width="120" align="center">
                  <template #default="{ row, $index }">
                    <el-input-number
                      v-model="row.otherQuantity"
                      :min="0"
                      :precision="4"
                      :step="0.0001"
                      controls-position="right"
                      style="width: 100%"
                      placeholder="比例"
                      :disabled="row.type !== 100"
                      :class="{'disabled-input': row.type !== 100}"
                      @change="(value) => handlePackagingRatioChange(value, $index)"
                    />
                  </template>
                </el-table-column>
                
                <el-table-column label="所需数量" width="120" align="center">
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
                
                <el-table-column label="损耗率" width="120" align="center">
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
                
                
                <el-table-column label="操作" width="140" fixed="right" align="center">
                  <template #default="{ $index }">
                    <div class="action-buttons">
                      <el-tooltip
                        content="向上插入一行"
                        placement="top"
                        
                      >
                        <el-button
                          type="primary"
                          link
                          :icon="ArrowUp"
                          @click="handleInsertAbove($index)"
                          
                          class="action-btn insert-btn"
                        />
                      </el-tooltip>
                      
                      <el-tooltip
                        content="向下插入一行"
                        placement="top"
                        :disabled="$index === formModel.bomDetails.length - 1"
                      >
                        <el-button
                          type="primary"
                          link
                          :icon="ArrowDown"
                          @click="handleInsertBelow($index)"
                          :disabled="$index === formModel.bomDetails.length - 1"
                          class="action-btn insert-btn"
                        />
                      </el-tooltip>
                      
                      <el-tooltip
                        content="删除"
                        placement="top"
                      >
                        <el-button
                          type="danger"
                          link
                          :icon="Delete"
                          @click="handleRemoveComponent($index)"
                          class="action-btn delete-btn"
                        />
                      </el-tooltip>
                    </div>
                  </template>
                </el-table-column>
              </el-table>

              <!-- 配方统计 -->
              <div class="bom-summary">
                <el-row :gutter="24">
                  <el-col :span="8">
                    <div class="summary-card">
                      <div class="summary-icon">
                        <el-icon><collection /></el-icon>
                      </div>
                      <div class="summary-content">
                        <div class="summary-value">{{ formModel.bomDetails.length }}</div>
                        <div class="summary-label">配件种类</div>
                      </div>
                    </div>
                  </el-col>
                  <el-col :span="8">
                    <div class="summary-card">
                      <div class="summary-icon">
                        <el-icon><tickets /></el-icon>
                      </div>
                      <div class="summary-content">
                        <div class="summary-value">{{ totalComponentQuantity }}</div>
                        <div class="summary-label">总数量</div>
                      </div>
                    </div>
                  </el-col>
                  <el-col :span="8">
                    <div class="summary-card">
                      <div class="summary-icon">
                        <el-icon><trend-charts /></el-icon>
                      </div>
                      <div class="summary-content">
                        <div class="summary-value">{{ totalLossRate.toFixed(2) }}%</div>
                        <div class="summary-label">平均损耗率</div>
                      </div>
                    </div>
                  </el-col>
                </el-row>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 备注信息卡片 -->
      <div class="form-card">
        <div class="card-header">
          <span class="card-title">备注信息</span>
        </div>
        <div class="card-content">
          <el-form-item>
            <el-input
              v-model="formModel.remark"
              type="textarea"
              :rows="3"
              placeholder="请输入备注信息"
              maxlength="255"
              show-word-limit
            />
          </el-form-item>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="form-actions">
        <el-button @click="$emit('cancel')" class="cancel-btn">取消</el-button>
        <el-button 
          type="primary" 
          @click="handleSubmit" 
          :loading="loading"
          class="submit-btn"
        >
          {{ isEdit ? '更新产品' : '创建产品' }}
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
// ========== 重要：保持原版逻辑完全不变 ==========
// 注意：这里只修改样式，所有逻辑代码保持原样

import { ref, reactive, computed, onMounted, watch, watchEffect } from 'vue';
import { ElMessage } from 'element-plus';
import { Plus, Delete,ArrowUp, ArrowDown  } from '@element-plus/icons-vue';
import { Document, Collection, Tickets, TrendCharts } from '@element-plus/icons-vue';
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

// 表单相关 - 保持原样
const formRef = ref();
const loading = ref(false);

// 数据列表 - 保持原样
const categoryList = ref([]);
const unitList = ref([]);
const componentProductList = ref([]);

// 搜索相关 - 保持原样
const categorySearchText = ref('');
const parentCategorySearchText = ref('');

// 分类对话框相关 - 保持原样
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

// 响应式表单模型 - 保持原样
const formModel = reactive({
  id: '',
  sku: '',
  barcode: '',
  name: '',
  englishName: '',
  spec: '',
  categoryCode: '',
  unitCode: '',
  outUnitCode: '',
  outUnitPerNum: 1,
  weightPerUnit: 0,
  outUnitLength: 0,
  outUnitWidth: 0,
  outUnitHeight: 0,
  color: '',
  minStock: 0,
  remark: '',
  status: 1,
  bomDetails: []
});

// 监听 props.formData 的变化，更新 formModel - 保持原样
watchEffect(() => {
  if (props.formData) {
    const formData = { ...props.formData };
    console.log('原始 formData:', formData);
    // 统一处理 BOM 数据字段
    if (!formData.bomDetails) {
      if (formData.bomData && Array.isArray(formData.bomData)) {
        formData.bomDetails = formData.bomData.map(item => ({
          componentProductId: item.componentProductId,
          componentProductName: item.componentProductName,
          componentProductSku: item.componentProductSku,
          componentProductSpec: item.componentProductSpec,
          componentProductColor: item.componentProductColor,
          componentProductUnit: item.componentProductUnit,
          quantity: item.quantity,
          otherQuantity: item.otherQuantity,
          type: item.type || 1,
          lossRate: item.lossRate,
          remark: item.remark,
          sortOrder: item.sortOrder
        }));
      } else {
        formData.bomDetails = [];
      }
    }
    
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
  name: [
    { required: true, message: '请输入产品名称', trigger: 'blur' }
  ],
  englishName: [
    { required: true, message: '请输入英文名称', trigger: 'blur' },
    { pattern: /^[A-Za-z0-9_]+$/, message: '英文名称只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  spec: [
    { required: true, message: '请输入规格型号', trigger: 'blur' }
  ],
  color: [
    { required: true, message: '请输入颜色', trigger: 'blur' }
  ],
  categoryCode: [
    { required: true, message: '请选择产品分类', trigger: 'change' }
  ],
  unitCode: [
    { required: true, message: '请选择基础单位', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ],
  remark: [
    { required: true, message: '请输入备注信息', trigger: 'blur' },
    { min: 2, message: '备注长度至少为2个字符', trigger: 'blur' }
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

// 计算属性 - 保持原样
const filteredCategoryList = computed(() => {
  if (!categorySearchText.value) {
    return categoryList.value;
  }
  return categoryList.value.filter(category => 
    category.categoryName?.toLowerCase().includes(categorySearchText.value.toLowerCase()) ||
    category.categoryCode?.toLowerCase().includes(categorySearchText.value.toLowerCase())
  );
});

const filteredParentCategoryList = computed(() => {
  if (!parentCategorySearchText.value) {
    return categoryList.value;
  }
  return categoryList.value.filter(category => 
    category.categoryName?.toLowerCase().includes(parentCategorySearchText.value.toLowerCase()) ||
    category.categoryCode?.toLowerCase().includes(parentCategorySearchText.value.toLowerCase())
  );
});

const availableComponentProducts = computed(() => {
  return componentProductList.value.filter(product => 
    product.status === 1
  );
});

const totalComponentQuantity = computed(() => {
  return formModel.bomDetails.reduce((sum, item) => {
    return sum + (parseFloat(item.quantity) || 0);
  }, 0).toFixed(4);
});

// 新增：平均损耗率计算
const totalLossRate = computed(() => {
  if (formModel.bomDetails.length === 0) return 0;
  const total = formModel.bomDetails.reduce((sum, item) => {
    return sum + (parseFloat(item.lossRate) || 0);
  }, 0);
  return total / formModel.bomDetails.length;
});

const calculateVolume = computed(() => {
  const length = parseFloat(formModel.outUnitLength) || 0;
  const width = parseFloat(formModel.outUnitWidth) || 0;
  const height = parseFloat(formModel.outUnitHeight) || 0;
  return length * width * height;
});

const formatVolume = (volume) => {
  if (volume >= 1000000) {
    return `${(volume / 1000000).toFixed(2)} m³`;
  } else if (volume >= 1000) {
    return `${(volume / 1000).toFixed(2)} L`;
  }
  return `${volume.toFixed(2)} cm³`;
};

const getUnitName = (unitCode) => {
  const unit = unitList.value.find(item => item.unitCode === unitCode);
  return unit ? unit.unitName : '';
};

const isComponentSelected = (productId) => {
  return formModel.bomDetails.some(item => item.componentProductId === productId);
};

// 方法 - 保持原样
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


// 处理类型变化
const handleTypeChange = (newType, index) => {
  const row = formModel.bomDetails[index];
  
  if (newType === 100) {
    // 如果切换到包装类型，且当前包装比例为0，则设置为1
    if (row.otherQuantity === 0) {
      row.otherQuantity = 1;
    }
  } else {
    // 如果切换到非包装类型，将包装比例重置为0
    row.otherQuantity = 0;
  }
};


const getCategoryFullName = (category) => {
  return `${category.categoryName} (${category.categoryCode})`;
};

const filterCategory = (query) => {
  categorySearchText.value = query;
};

const filterParentCategory = (query) => {
  parentCategorySearchText.value = query;
};

const handleAddComponent = () => {
  formModel.bomDetails.push({
    componentProductId: null,
    componentProductName: '',
    componentProductSku: '',
    componentProductSpec: '',
    componentProductColor: '',
    componentProductUnit: '',
    quantity: 1,
    otherQuantity: 0,
    type: 1,
    lossRate: 0,
    remark: '',
    sortOrder: formModel.bomDetails.length // 确保sortOrder正确
  });
};

const handleRemoveComponent = (index) => {
  formModel.bomDetails.splice(index, 1);
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
    detail.componentProductColor = product.color;
    detail.componentProductUnit = product.unitName;
  }
};


// 在指定位置上方插入一行
const handleInsertAbove = (index) => {
  const newRow = {
    componentProductId: null,
    componentProductName: '',
    componentProductSku: '',
    componentProductSpec: '',
    componentProductColor: '',
    componentProductUnit: '',
    quantity: 1,
    otherQuantity: 0,
    type: 1,
    lossRate: 0,
    remark: '',
    sortOrder: index // 插入在当前位置
  };
  
  // 在指定位置插入新行
  formModel.bomDetails.splice(index, 0, newRow);
  
  // 重新排序所有行的sortOrder
  formModel.bomDetails.forEach((item, idx) => {
    item.sortOrder = idx;
  });
};

// 在指定位置下方插入一行
const handleInsertBelow = (index) => {
  const newRow = {
    componentProductId: null,
    componentProductName: '',
    componentProductSku: '',
    componentProductSpec: '',
    componentProductColor: '',
    componentProductUnit: '',
    quantity: 1,
    otherQuantity: 0,
    type: 1,
    lossRate: 0,
    remark: '',
    sortOrder: index + 1 // 插入在当前位置下方
  };
  
  // 在指定位置的下方插入新行
  formModel.bomDetails.splice(index + 1, 0, newRow);
  
  // 重新排序所有行的sortOrder
  formModel.bomDetails.forEach((item, idx) => {
    item.sortOrder = idx;
  });
};


// 在 methods 部分添加这个方法
const handlePackagingRatioChange = (value, index) => {
  const row = formModel.bomDetails[index];
  if (row.type === 100 && (!value || value <= 0)) {
    // 如果包装件且输入值无效，可以给出提示，但不要自动重置
    // 让用户自己修改或通过验证阻止提交
    console.warn('包装比例必须大于0');
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

// ========== 关键：确保 handleSubmit 方法完全正确 ==========
const handleSubmit = async () => {
  if (!formRef.value) return;
  
  console.log('提交前的BOM数据:', formModel.bomDetails);
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
        if (detail.type === undefined || detail.type === null) {
          ElMessage.warning(`请选择第 ${i + 1} 行配件的类型`);
          return;
        }

        // 新增：如果类型是包装件，包装比例必须大于0
        if (detail.type === 100 && (!detail.otherQuantity || detail.otherQuantity <= 0)) {
          ElMessage.warning(`第 ${i + 1} 行为包装件，请输入有效的包装比例`);
          return;
        }
      }
    }
    
    loading.value = true;
    console.log('提交数据:', formModel);
    
    const submitData = {
      id: formModel.id,
      sku: formModel.sku,
      barcode: formModel.barcode,
      name: formModel.name,
      englishName: formModel.englishName,
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
      outUnitLength: formModel.outUnitLength,
      outUnitWidth: formModel.outUnitWidth,
      outUnitHeight: formModel.outUnitHeight,
      bomData: formModel.bomDetails.length > 0 ? {
        bomCode: `${formModel.sku}_BOM`,
        version: 'V1.0',
        status: 1,
        remark: `${formModel.name}的默认配方`,
        details: formModel.bomDetails.map(detail => ({
          componentProductId: detail.componentProductId,
          otherQuantity: detail.otherQuantity,
          quantity: detail.quantity,
          lossRate: detail.lossRate,
          type: detail.type,
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

// 监听器 - 保持原样
watch(() => formModel.outUnitCode, (newVal) => {
  if (newVal && !formModel.outUnitPerNum) {
    formModel.outUnitPerNum = 1;
  }
});

// 生命周期 - 保持原样
onMounted(() => {
  loadCategoryList();
  loadUnitList();
  loadComponentProductList();
});
</script>

<style scoped>
.product-form {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100%;
}

.form-header {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e8e8e8;
}

.form-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
}

.form-subtitle {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.required-mark {
  color: #f56c6c;
  margin-right: 4px;
}

/* 卡片样式 */
.form-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
  border: 1px solid #ebeef5;
}

.card-header {
  padding: 16px 20px;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fafafa;
  border-radius: 8px 8px 0 0;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
}

.card-title::before {
  content: '';
  display: inline-block;
  width: 3px;
  height: 16px;
  background: #409eff;
  border-radius: 1.5px;
  margin-right: 8px;
}

.header-button {
  flex-shrink: 0;
}

.card-content {
  padding: 20px;
}

/* 表单项目样式 */
.form-item-container {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-item-tip {
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
}

.form-item-with-action {
  display: flex;
  gap: 12px;
  align-items: center;
}

.action-button {
  white-space: nowrap;
  flex-shrink: 0;
}

/* 单位转换显示 */
.conversion-display {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.conversion-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
}

.conversion-label {
  font-size: 14px;
  color: #606266;
  white-space: nowrap;
}

/* 尺寸区域 */
.dimensions-section {
  margin-top: 20px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 6px;
  border: 1px solid #e4e7ed;
}

.section-header {
  margin-bottom: 16px;
}

.section-title {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

/* 体积计算结果 */
.volume-result {
  margin-top: 16px;
}

.volume-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 6px;
  padding: 16px;
  color: white;
}

.volume-header {
  margin-bottom: 12px;
}

.volume-title {
  font-size: 14px;
  font-weight: 500;
  opacity: 0.9;
}

.volume-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.volume-value {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.volume-value .value {
  font-size: 24px;
  font-weight: 600;
}

.volume-value .unit {
  font-size: 14px;
  opacity: 0.9;
}

.volume-converted {
  font-size: 14px;
  opacity: 0.9;
  padding: 4px 8px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 4px;
}

/* 状态选择器 */
.status-radio {
  margin-right: 24px;
}

.status-radio.active :deep(.el-radio__inner) {
  border-color: #67c23a;
}

.status-radio.active :deep(.el-radio__inner.is-checked) {
  background-color: #67c23a;
}

.status-radio.inactive :deep(.el-radio__inner) {
  border-color: #f56c6c;
}

.status-radio.inactive :deep(.el-radio__inner.is-checked) {
  background-color: #f56c6c;
}

/* BOM区域 */
.bom-section {
  min-height: 100px;
}

.empty-bom {
  text-align: center;
  padding: 40px 20px;
  color: #c0c4cc;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
  color: #dcdfe6;
}

.empty-bom p {
  margin: 0;
  font-size: 14px;
}

.empty-tip {
  font-size: 12px;
  margin-top: 8px !important;
  color: #909399;
}

.bom-table {
  border-radius: 6px;
  overflow: hidden;
  margin-bottom: 16px;
}

.bom-table :deep(.el-table__header) {
  background: #fafafa;
}

.bom-table :deep(.el-table__header th) {
  background: #fafafa;
  font-weight: 600;
  color: #303133;
}

.index-badge {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #409eff;
  color: white;
  border-radius: 4px;
  font-weight: 500;
  font-size: 12px;
}

.product-selector {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.product-details {
  display: flex;
  gap: 8px;
  align-items: center;
  font-size: 12px;
  color: #909399;
}

.color-tag {
  padding: 2px 6px;
  background: #e8f4ff;
  color: #409eff;
  border-radius: 3px;
  font-size: 11px;
}

.delete-btn {
  color: #f56c6c;
  font-size: 16px;
}

.delete-btn:hover {
  color: #f78989;
}

/* BOM统计 */
.bom-summary {
  padding: 16px;
  background: #f8f9fa;
  border-radius: 6px;
  border: 1px solid #e4e7ed;
}

.summary-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: white;
  border-radius: 6px;
  border: 1px solid #ebeef5;
}

.summary-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  background: #409eff;
  color: white;
  font-size: 18px;
}

.summary-content {
  flex: 1;
}

.summary-value {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  line-height: 1.2;
}

.summary-label {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

/* 操作按钮 */
.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.cancel-btn {
  min-width: 80px;
}

.submit-btn {
  min-width: 100px;
  background: linear-gradient(135deg, #409eff 0%, #67c2ef 100%);
  border: none;
}

.submit-btn:hover {
  background: linear-gradient(135deg, #409eff 0%, #67c2ef 100%);
  opacity: 0.9;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .product-form {
    padding: 12px;
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .header-button {
    align-self: flex-end;
  }
  
  .card-content {
    padding: 16px;
  }
  
  .form-actions {
    flex-direction: column;
  }
  
  .cancel-btn,
  .submit-btn {
    width: 100%;
  }
}

@media (max-width: 576px) {
  .form-title {
    font-size: 18px;
  }
  
  .card-title {
    font-size: 15px;
  }
  
  .conversion-wrapper {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .conversion-wrapper :deep(.el-input-number) {
    width: 100% !important;
  }
}

/* 操作按钮样式 */
.action-buttons {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 4px;
}

.action-btn {
  padding: 4px;
  font-size: 14px;
  min-width: auto;
  height: auto;
}

.insert-btn {
  color: #409eff;
}

.insert-btn:hover {
  color: #66b1ff;
}

.insert-btn:disabled {
  color: #c0c4cc;
  cursor: not-allowed;
}

.delete-btn {
  color: #f56c6c;
}

.delete-btn:hover {
  color: #f78989;
}

/* 调整表格操作列宽度 */
.bom-table :deep(.el-table__cell) {
  padding: 8px 4px;
}

/* 禁用输入框样式 */
.disabled-input :deep(.el-input__wrapper) {
  background-color: #f5f7fa;
  cursor: not-allowed;
}

.disabled-input :deep(.el-input-number__decrease),
.disabled-input :deep(.el-input-number__increase) {
  background-color: #f5f7fa;
  cursor: not-allowed;
}
/* 必填项标记样式 */
:deep(.el-form-item.is-required .el-form-item__label::before) {
  content: '*';
  color: #f56c6c;
  margin-right: 4px;
}

/* 也可以为目标必填项添加特定样式 */
.required-field .el-form-item__label::before {
  content: '*';
  color: #f56c6c;
  margin-right: 4px;
}
/* 隐藏数字输入框的上下箭头 */
:deep(.el-input-number) {
  width: 100%;
}

:deep(.el-input-number .el-input-number__decrease),
:deep(.el-input-number .el-input-number__increase) {
  display: none;
}

:deep(.el-input-number .el-input__wrapper) {
  padding-right: 11px; /* 调整右边距 */
}
</style>