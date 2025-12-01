<template>
  <div class="product-manage-container">
    <el-card class="manage-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">产品管理</span>
          <div class="header-actions">
            <el-button 
              type="primary" 
              @click="handleCreate"
            >
              <el-icon><Plus /></el-icon>
              新增产品
            </el-button>
            <el-button 
              @click="handleBatchImport"
            >
              <el-icon><Upload /></el-icon>
              批量导入
            </el-button>
            <el-button 
              @click="refreshList"
              :loading="loading"
            >
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </div>
      </template>

      <!-- 筛选条件 -->
      <div class="filter-section">
        <el-form :model="filterForm" inline>
          <el-form-item label="产品名称">
            <el-input
              v-model="filterForm.name"
              placeholder="请输入产品名称"
              clearable
              style="width: 200px"
            />
          </el-form-item>
          <el-form-item label="SKU编码">
            <el-input
              v-model="filterForm.sku"
              placeholder="请输入SKU编码"
              clearable
              style="width: 150px"
            />
          </el-form-item>
          <el-form-item label="英文名称">
            <el-input
              v-model="filterForm.englishName"
              placeholder="请输入英文名称"
              clearable
              style="width: 200px"
            />
          </el-form-item>
          <el-form-item label="产品分类">
            <el-cascader
              v-model="filterForm.categoryCode"
              :options="categoryTree"
              :props="categoryProps"
              placeholder="请选择产品分类"
              clearable
              style="width: 200px"
            />
          </el-form-item>
          <el-form-item label="状态">
            <el-select
              v-model="filterForm.status"
              placeholder="全部状态"
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
            <el-button @click="handleExport">导出</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 统计信息 -->
      <div class="stats-section">
        <el-row :gutter="20">
          <el-col :xs="12" :sm="6" :lg="3">
            <div class="stat-item total">
              <div class="stat-icon">
                <el-icon><Box /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ stats.total }}</div>
                <div class="stat-label">产品总数</div>
              </div>
            </div>
          </el-col>
          <el-col :xs="12" :sm="6" :lg="3">
            <div class="stat-item active">
              <div class="stat-icon">
                <el-icon><CircleCheck /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ stats.active }}</div>
                <div class="stat-label">启用产品</div>
              </div>
            </div>
          </el-col>
          <el-col :xs="12" :sm="6" :lg="3">
            <div class="stat-item low-stock">
              <div class="stat-icon">
                <el-icon><Warning /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ stats.lowStock }}</div>
                <div class="stat-label">库存预警</div>
              </div>
            </div>
          </el-col>
          <el-col :xs="12" :sm="6" :lg="3">
            <div class="stat-item categories">
              <div class="stat-icon">
                <el-icon><Collection /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ stats.categories }}</div>
                <div class="stat-label">产品分类</div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 产品列表 -->
      <div class="product-list-section">
        <el-table
          :data="productList"
          v-loading="loading"
          empty-text="暂无产品数据"
          class="product-table"
          row-key="id"
        >
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column label="产品图片" width="100" align="center">
            <template #default="{ row }">
              <div class="product-image">
                <el-image
                  v-if="row.imageUrl"
                  :src="row.imageUrl"
                  :preview-src-list="[row.imageUrl]"
                  fit="cover"
                  class="product-img"
                >
                  <template #error>
                    <div class="image-slot">
                      <el-icon><Picture /></el-icon>
                    </div>
                  </template>
                </el-image>
                <div v-else class="image-slot">
                  <el-icon><Picture /></el-icon>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="产品信息" min-width="220" fixed="left">
            <template #default="{ row }">
              <div class="product-info">
                <div class="product-name">{{ row.name }}</div>
                <div class="product-english-name" v-if="row.englishName">
                  {{ row.englishName }}
                </div>
                <div class="product-sku">SKU: {{ row.sku }}</div>
                <div class="product-spec" v-if="row.spec">规格: {{ row.spec }}</div>
                <div class="product-color" v-if="row.color">颜色: {{ row.color }}</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="条形码" width="140">
            <template #default="{ row }">
              <span>{{ row.barcode || '--' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="产品分类" width="150">
            <template #default="{ row }">
              <span>{{ row.categoryName || '--' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="单位信息" width="180">
            <template #default="{ row }">
              <div class="unit-info">
                <div>基础单位: {{ row.unitName }}</div>
                <div v-if="row.outUnitName">出货单位: {{ row.outUnitName }}</div>
                <div v-if="row.outUnitPerNum">转换率: 1{{ row.outUnitName }} = {{ row.outUnitPerNum }}{{ row.unitName }}</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="重量" width="100" align="center">
            <template #default="{ row }">
              <span>{{ row.weightPerUnit ? row.weightPerUnit + 'kg' : '--' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="最低库存" width="100" align="center">
            <template #default="{ row }">
              <span :class="getStockClass(row.minStock)">{{ row.minStock || 0 }}</span>
            </template>
          </el-table-column>
          <el-table-column label="当前库存" width="100" align="center">
            <template #default="{ row }">
              <el-button type="primary" link @click="handleViewInventory(row)">
                {{ row.currentStock || 0 }}
              </el-button>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="80" align="center">
            <template #default="{ row }">
              <el-tag 
                :type="row.status === 1 ? 'success' : 'danger'" 
                size="small"
              >
                {{ row.status === 1 ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" width="160">
            <template #default="{ row }">
              <span>{{ formatTime(row.createdAt) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="280" fixed="right" align="center">
            <template #default="{ row }">
              <div class="action-buttons">
                <el-button
                  type="primary"
                  link
                  size="small"
                  @click="handleView(row)"
                >
                  查看
                </el-button>
                <el-button
                  type="warning"
                  link
                  size="small"
                  @click="handleEdit(row)"
                >
                  编辑
                </el-button>
                <!-- 新增导入成品原材料按钮 -->
                <el-button
                  type="success"
                  link
                  size="small"
                  @click="handleImportBom(row)"
                >
                  导入原材料
                </el-button>
                <el-button
                  :type="row.status === 1 ? 'danger' : 'success'"
                  link
                  size="small"
                  @click="handleToggleStatus(row)"
                >
                  {{ row.status === 1 ? '禁用' : '启用' }}
                </el-button>
                <el-button
                  type="danger"
                  link
                  size="small"
                  @click="handleDelete(row)"
                >
                  删除
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-section">
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
      </div>
    </el-card>

    <!-- 新增/编辑产品对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      :title="`${isEdit ? '编辑' : '新增'}产品`"
      width="800px"
      top="5vh"
      :close-on-click-modal="false"
    >
      <ProductForm
        v-if="editDialogVisible"
        :form-data="currentProduct"
        :is-edit="isEdit"
        @success="handleFormSuccess"
        @cancel="editDialogVisible = false"
      />
    </el-dialog>

    <!-- 产品详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      :title="`产品详情 - ${currentProduct?.name}`"
      width="700px"
      top="5vh"
    >
      <ProductDetail
        v-if="detailDialogVisible && currentProduct"
        :product-data="currentProduct"
        @close="detailDialogVisible = false"
      />
    </el-dialog>

    <!-- 库存查看对话框 -->
    <el-dialog
      v-model="inventoryDialogVisible"
      :title="`库存查看 - ${currentProduct?.name}`"
      width="90%"
      top="5vh"
    >
      <ProductInventory
        v-if="inventoryDialogVisible && currentProduct"
        :product-id="currentProduct.id"
        @close="inventoryDialogVisible = false"
      />
    </el-dialog>

    <!-- 批量导入对话框 -->
    <el-dialog
      v-model="importDialogVisible"
      title="批量导入产品"
      width="600px"
    >
      <ProductImport
        @success="handleImportSuccess"
        @cancel="importDialogVisible = false"
      />
    </el-dialog>

    <!-- 导入成品原材料对话框 -->
    <el-dialog
      v-model="importBomDialogVisible"
      :title="`导入原材料 - ${currentProduct?.name}`"
      width="500px"
      :close-on-click-modal="false"
    >
      <div class="import-bom-dialog">
        <el-steps :active="importBomStep" align-center class="import-steps">
          <el-step title="下载模板" />
          <el-step title="上传文件" />
          <el-step title="导入结果" />
        </el-steps>

        <!-- 步骤1：下载模板 -->
        <div v-if="importBomStep === 0" class="step-content">
          <div class="step-description">
            <p>请先下载导入模板，按照模板格式填写原材料数据</p>
          </div>
          <div class="download-section">
            <el-button 
              type="primary" 
              @click="downloadBomTemplate"
              :loading="downloadLoading"
            >
              <el-icon><Download /></el-icon>
              下载导入模板
            </el-button>
          </div>
        </div>

        <!-- 步骤2：上传文件 -->
        <div v-if="importBomStep === 1" class="step-content">
          <div class="step-description">
            <p>请选择已填写好的Excel文件进行导入</p>
          </div>
          <div class="upload-section">
            <el-upload
              ref="bomUploadRef"
              class="upload-demo"
              :auto-upload="false"
              :show-file-list="true"
              :on-change="handleBomFileChange"
              accept=".xlsx,.xls"
            >
              <template #trigger>
                <el-button type="primary">选择文件</el-button>
              </template>
            </el-upload>
            <div class="upload-tips">
              <p>支持 .xlsx, .xls 格式文件，文件大小不超过10MB</p>
            </div>
          </div>
        </div>

        <!-- 步骤3：导入结果 -->
        <div v-if="importBomStep === 2" class="step-content">
          <div class="import-result">
            <el-result
              v-if="importResult.success"
              icon="success"
              :title="importResult.title"
              :sub-title="importResult.message"
            >
              <template #extra>
                <el-button type="primary" @click="handleImportBomSuccess">完成</el-button>
              </template>
            </el-result>
            <el-result
              v-else
              icon="error"
              :title="importResult.title"
              :sub-title="importResult.message"
            >
              <template #extra>
                <el-button @click="importBomStep = 1">重新上传</el-button>
                <el-button type="primary" @click="handleImportBomSuccess">关闭</el-button>
              </template>
            </el-result>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button 
            v-if="importBomStep > 0 && importBomStep < 2" 
            @click="importBomStep--"
          >
            上一步
          </el-button>
          <el-button 
            v-if="importBomStep === 0" 
            type="primary" 
            @click="importBomStep++"
          >
            下一步
          </el-button>
          <el-button 
            v-if="importBomStep === 1" 
            type="primary" 
            @click="handleBomImportSubmit"
            :loading="importLoading"
            :disabled="!currentBomFile"
          >
            开始导入
          </el-button>
          <el-button 
            v-if="importBomStep === 2" 
            @click="importBomDialogVisible = false"
          >
            关闭
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { 
  Plus, Refresh, Upload, Box, CircleCheck, Warning, Collection, Picture 
} from '@element-plus/icons-vue';
import { post, get } from '@/net';
import ProductForm from '@/components/ProductForm.vue';
import ProductDetail from '@/components/ProductDetail.vue';
import ProductInventory from '@/components/ProductInventory.vue';
import ProductImport from '@/components/ProductImport.vue';
import axios from 'axios';
import { accessHeader } from '@/net'; 

const loading = ref(false);
const editDialogVisible = ref(false);
const detailDialogVisible = ref(false);
const inventoryDialogVisible = ref(false);
const importDialogVisible = ref(false);
const isEdit = ref(false);
const currentProduct = ref(null);

// 导入成品原材料弹窗
const importBomDialogVisible = ref(false);
const importBomStep = ref(0);
const downloadLoading = ref(false);
const importLoading = ref(false);
const currentBomFile = ref(null);
const bomUploadRef = ref(null);

// 筛选表单
const filterForm = reactive({
  name: '',
  sku: '',
  englishName: '', // 新增英文名筛选
  categoryCode: '',
  status: ''
});

// 分页信息
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
});

// 统计信息
const stats = reactive({
  total: 0,
  active: 0,
  lowStock: 0,
  categories: 0
});

// 数据列表
const productList = ref([]);
const categoryTree = ref([]);
const unitList = ref([]);

// 分类级联选择器配置
const categoryProps = {
  value: 'categoryCode',
  label: 'categoryName',
  children: 'children',
  checkStrictly: true
};

// 导入结果
const importResult = reactive({
  success: false,
  title: '',
  message: ''
});


// 导入成品原材料方法
const handleImportBom = (product) => {
  currentProduct.value = product;
  importBomDialogVisible.value = true;
  importBomStep.value = 0;
  currentBomFile.value = null;
  if (bomUploadRef.value) {
    bomUploadRef.value.clearFiles();
  }
};

// 下载BOM模板
const downloadBomTemplate = async () => {
  downloadLoading.value = true;
  try {
    const response = await axios.get('/api/auth/product/bom/exportBomExcel', {
      headers: accessHeader(),
      responseType: 'blob',
    });

    const blob = new Blob([response.data], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    });

    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = '成品原材料导入模板.xlsx';
    document.body.appendChild(a);
    a.click();
    a.remove();
    window.URL.revokeObjectURL(url);

    ElMessage.success('模板下载成功');
  } catch (error) {
    console.error('下载模板失败', error);
    ElMessage.error('下载模板失败，请稍后重试');
  } finally {
    downloadLoading.value = false;
  }
};

// 处理文件选择
const handleBomFileChange = (file) => {
  currentBomFile.value = file;
};

// 提交BOM导入
const handleBomImportSubmit = async () => {
  if (!currentBomFile.value) {
    ElMessage.warning('请选择要上传的文件');
    return;
  }

  importLoading.value = true;
  try {
    const formData = new FormData();
    const realFile = currentBomFile.value.raw || currentBomFile.value;
    formData.append('file', realFile);
    // 传入当前产品的ID
    formData.append('productId', currentProduct.value.id);

    ElMessage.info('开始导入原材料数据，请稍候...');

    const result = await post('/api/auth/product/bom/importBomExcel', formData);
    console.log('BOM导入响应:', result);

    if (result) {
      importResult.success = true;
      importResult.title = '导入成功';
      importResult.message = `成功导入原材料数据`;
      importBomStep.value = 2;
    } else {
      throw new Error('导入失败');
    }
  } catch (error) {
    console.error('BOM导入失败:', error);
    importResult.success = false;
    importResult.title = '导入失败';
    importResult.message = error.response?.data?.message || '导入失败，请检查数据格式';
    importBomStep.value = 2;
  } finally {
    importLoading.value = false;
  }
};

// 处理导入成功
const handleImportBomSuccess = () => {
  importBomDialogVisible.value = false;
  // 可以刷新产品列表或BOM数据
  loadProductList();
};

// 计算属性
const activeProducts = computed(() => {
  return productList.value.filter(product => product.status === 1);
});

const lowStockProducts = computed(() => {
  return productList.value.filter(product => 
    product.minStock && product.currentStock <= product.minStock
  );
});

// 方法
const loadProductList = async () => {
  loading.value = true;
  try {
    const params = {
      page: pagination.current,
      size: pagination.size,
      ...filterForm
    };
    
    // 处理分类编码
    if (filterForm.categoryCode && filterForm.categoryCode.length > 0) {
      params.categoryCode = filterForm.categoryCode[filterForm.categoryCode.length - 1];
    }
    
    const res = await post('/api/auth/product/pageList', params);
    console.log('产品列表数据:', res);
    if (res && res.records) {
      productList.value = res.records.map(product => ({
        id: product.id || '',
        sku: product.sku || '',
        barcode: product.barcode || '',
        name: product.name || '',
        englishName: product.englishName || '', // 新增英文名字段
        spec: product.spec || '',
        categoryCode: product.categoryCode || '',
        categoryName: product.categoryName || '',
        unitCode: product.unitCode || '',
        unitName: product.unitName || '',
        outUnitCode: product.outUnitCode || '',
        outUnitName: product.outUnitName || '',
        outUnitPerNum: product.outUnitPerNum || 0,
        weightPerUnit: product.weightPerUnit || 0,
        outUnitLength: product.outUnitLength || 0,
        outUnitWidth: product.outUnitWidth || 0,
        outUnitHeight: product.outUnitHeight || 0,
        color: product.color || '',
        minStock: product.minStock || 0,
        currentStock: product.currentStock || 0,
        remark: product.remark || '',
        status: product.status ,
        imageUrl: product.imageUrl || '',
        createdAt: product.createdAt || new Date().toISOString(),
        updatedAt: product.updatedAt || new Date().toISOString(),
        bomData: product.bomData || [] // 确保 bomData 字段存在
      }));
      pagination.total = res.total || 0;
      
      // 更新统计信息
      updateStats();
    } else {
      productList.value = [];
      pagination.total = 0;
    }
  } catch (error) {
    console.error('加载产品列表失败:', error);
    ElMessage.error('加载产品列表失败');
    productList.value = [];
  } finally {
    loading.value = false;
  }
};

const loadCategoryTree = async () => {
  try {
    const res = await get('/api/auth/product/categoryList');
    if (res && Array.isArray(res)) {
      categoryTree.value = res;
      // 更新分类统计
      stats.categories = countCategories(res);
    }
  } catch (error) {
    console.error('加载分类树失败:', error);
    categoryTree.value = [];
  }
};

const countCategories = (categories) => {
  let count = 0;
  const countRecursive = (items) => {
    items.forEach(item => {
      count++;
      if (item.children && item.children.length > 0) {
        countRecursive(item.children);
      }
    });
  };
  countRecursive(categories);
  return count;
};

const loadUnitList = async () => {
  try {
    const res = await get('/api/auth/product/unitList');
    unitList.value = res || [];
  } catch (error) {
    console.error('加载单位列表失败:', error);
    unitList.value = [];
  }
};

const updateStats = () => {
  stats.total = productList.value.length;
  stats.active = activeProducts.value.length;
  stats.lowStock = lowStockProducts.value.length;
};

const refreshList = () => {
  pagination.current = 1;
  loadProductList();
};

const handleSearch = () => {
  pagination.current = 1;
  loadProductList();
};

const handleReset = () => {
  Object.assign(filterForm, {
    name: '',
    englishName: '', // 新增重置英文名
    sku: '',
    categoryCode: '',
    status: ''
  });
  pagination.current = 1;
  loadProductList();
};

const handleExport = () => {
  ElMessage.info('导出功能开发中');
};

const handleSizeChange = (size) => {
  pagination.size = size;
  pagination.current = 1;
  loadProductList();
};

const handleCurrentChange = (page) => {
  pagination.current = page;
  loadProductList();
};

const handleCreate = () => {
  isEdit.value = false;
  currentProduct.value = {
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
    color: '',
    minStock: 0,
    remark: '',
    status: 1,
    bomDetails: [] // 确保创建时也有 bomDetails 字段
  };
  editDialogVisible.value = true;
};

const handleEdit = (product) => {
  isEdit.value = true;
  
  // 转换 BOM 数据格式 - 关键修改
  const formData = { 
    ...product,
    // 将 bomData 转换为 bomDetails
    bomDetails: product.bomData && Array.isArray(product.bomData) ? product.bomData.map(item => ({
      componentProductId: item.componentProductId,
      componentProductName: item.componentProductName,
      componentProductSku: item.componentProductSku,
      componentProductSpec: item.componentProductSpec,
      componentProductUnit: item.componentProductUnit,
      quantity: item.quantity,
      type: item.type,
      lossRate: item.lossRate,
      remark: item.remark,
      sortOrder: item.sortOrder
    })) : [],
    // 确保出货单位体积字段有值
    outUnitLength: product.outUnitLength || 0,
    outUnitWidth: product.outUnitWidth || 0,
    outUnitHeight: product.outUnitHeight || 0
  };
  
  console.log('编辑产品数据转换:', {
    原始数据: product,
    转换后数据: formData
  });
  
  currentProduct.value = formData;
  editDialogVisible.value = true;
};

const handleView = (product) => {
  currentProduct.value = product;
  detailDialogVisible.value = true;
};

const handleViewInventory = (product) => {
  currentProduct.value = product;
  inventoryDialogVisible.value = true;
};

const handleBatchImport = () => {
  importDialogVisible.value = true;
};

const handleToggleStatus = async (product) => {
  const newStatus = product.status === 1 ? 0 : 1;
  const actionText = newStatus === 1 ? '启用' : '禁用';
  
  try {
    await ElMessageBox.confirm(
      `确定要${actionText}产品"${product.name}"吗？`,
      `${actionText}产品`,
      { type: 'warning' }
    );
    
    const res = await post('/api/auth/product/updateStatus', {
      id: product.id,
      status: newStatus
    });
    
    if (res) {
      ElMessage.success(`${actionText}产品成功`);
      loadProductList();
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(`${actionText}产品失败`);
    }
  }
};

const handleDelete = async (product) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除产品"${product.name}"吗？此操作不可恢复！`,
      '删除产品',
      { 
        type: 'error',
        confirmButtonText: '确定删除',
        cancelButtonText: '取消'
      }
    );
    
    const res = await post('/api/auth/product/delete', {
      id: product.id
    });
    
    if (res) {
      ElMessage.success('删除产品成功');
      loadProductList();
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除产品失败');
    }
  }
};

const handleFormSuccess = () => {
  editDialogVisible.value = false;
  ElMessage.success(`${isEdit.value ? '编辑' : '新增'}产品成功`);
  loadProductList();
};

const handleImportSuccess = () => {
  importDialogVisible.value = false;
  ElMessage.success('批量导入成功');
  loadProductList();
};

const getStockClass = (minStock) => {
  return minStock > 0 ? 'has-min-stock' : '';
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

onMounted(() => {
  loadProductList();
  loadCategoryTree();
  loadUnitList();
});
</script>

<style scoped>
.product-manage-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.manage-card {
  border-radius: 8px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.filter-section {
  padding: 20px 0;
  border-bottom: 1px solid #ebeef5;
}

.stats-section {
  padding: 20px 0;
}

.stat-item {
  display: flex;
  align-items: center;
  padding: 16px;
  border-radius: 8px;
  background: white;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  color: white;
  font-size: 24px;
}

.stat-item.total .stat-icon {
  background-color: #409EFF;
}

.stat-item.active .stat-icon {
  background-color: #67C23A;
}

.stat-item.low-stock .stat-icon {
  background-color: #E6A23C;
}

.stat-item.categories .stat-icon {
  background-color: #9b59b6;
}

.stat-content {
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

.product-list-section {
  margin-top: 20px;
}

.product-table {
  width: 100%;
}

.product-image {
  display: flex;
  justify-content: center;
  align-items: center;
}

.product-img {
  width: 50px;
  height: 50px;
  border-radius: 4px;
  object-fit: cover;
}

.image-slot {
  width: 50px;
  height: 50px;
  border-radius: 4px;
  background-color: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
}

.product-info {
  line-height: 1.4;
}

.product-name {
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.product-sku {
  font-size: 12px;
  color: #909399;
  margin-bottom: 2px;
}

.product-spec, .product-color {
  font-size: 12px;
  color: #606266;
}

.unit-info {
  font-size: 12px;
  line-height: 1.4;
  color: #606266;
}

.has-min-stock {
  color: #E6A23C;
  font-weight: bold;
}

.action-buttons {
  display: flex;
  gap: 8px;
  justify-content: center;
}

.pagination-section {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .product-manage-container {
    padding: 10px;
  }
  
  .card-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
  
  .header-actions {
    width: 100%;
    justify-content: flex-end;
  }
  
  .filter-section .el-form-item {
    margin-bottom: 12px;
  }
  
  .stats-section .el-col {
    margin-bottom: 12px;
  }
  
  .action-buttons {
    flex-direction: column;
    gap: 4px;
  }
}

/* 动画效果 */
.product-table :deep(.el-table__row) {
  transition: all 0.3s;
}

.product-table :deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}

.product-info {
  line-height: 1.4;
}

.product-name {
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.product-english-name {
  font-size: 12px;
  color: #409EFF;
  font-style: italic;
  margin-bottom: 4px;
}

.product-sku {
  font-size: 12px;
  color: #909399;
  margin-bottom: 2px;
}

.product-spec, .product-color {
  font-size: 12px;
  color: #606266;
}

/* 新增导入BOM对话框样式 */
.import-bom-dialog {
  padding: 20px 0;
}

.import-steps {
  margin-bottom: 30px;
}

.step-content {
  text-align: center;
  padding: 20px 0;
}

.step-description {
  margin-bottom: 20px;
  color: #606266;
}

.download-section, .upload-section {
  margin: 20px 0;
}

.upload-tips {
  margin-top: 10px;
  color: #909399;
  font-size: 12px;
}

.import-result {
  padding: 20px 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 调整操作按钮间距 */
.action-buttons {
  display: flex;
  gap: 6px;
  justify-content: center;
  flex-wrap: wrap;
}

.action-buttons .el-button {
  margin: 2px;
}

/* 新增导入BOM对话框样式 */
.import-bom-dialog {
  padding: 20px 0;
}

.import-steps {
  margin-bottom: 30px;
}

.step-content {
  text-align: center;
  padding: 20px 0;
}

.step-description {
  margin-bottom: 20px;
  color: #606266;
}

.download-section, .upload-section {
  margin: 20px 0;
}

.upload-tips {
  margin-top: 10px;
  color: #909399;
  font-size: 12px;
}

.import-result {
  padding: 20px 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 调整操作按钮间距 */
.action-buttons {
  display: flex;
  gap: 6px;
  justify-content: center;
  flex-wrap: wrap;
}

.action-buttons .el-button {
  margin: 2px;
}
</style>