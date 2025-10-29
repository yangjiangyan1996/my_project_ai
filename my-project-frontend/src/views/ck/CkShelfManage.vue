<template>
  <div class="shelf-manage-container">
    <el-card class="manage-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">货架管理</span>
          <div class="header-actions">
            <el-button 
              type="primary" 
              @click="handleCreate"
            >
              <el-icon><Plus /></el-icon>
              新建货架
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
          <el-form-item label="货架编码">
            <el-input
              v-model="filterForm.shelfCode"
              placeholder="请输入货架编码"
              clearable
              style="width: 200px"
            />
          </el-form-item>
          <el-form-item label="货架名称">
            <el-input
              v-model="filterForm.shelfName"
              placeholder="请输入货架名称"
              clearable
              style="width: 200px"
            />
          </el-form-item>
          <el-form-item label="仓库">
            <el-select
              v-model="filterForm.warehouseId"
              placeholder="全部仓库"
              clearable
              style="width: 150px"
            >
              <el-option
                v-for="warehouse in warehouseList"
                :key="warehouse.id"
                :label="warehouse.name"
                :value="warehouse.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="区域">
            <el-select
              v-model="filterForm.area"
              placeholder="全部区域"
              clearable
              style="width: 120px"
            >
              <el-option
                v-for="area in areaOptions"
                :key="area"
                :label="area"
                :value="area"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select
              v-model="filterForm.status"
              placeholder="全部状态"
              clearable
              style="width: 120px"
            >
              <el-option
                v-for="item in statusOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 货架列表 -->
      <div class="shelf-list-section">
        <el-table
          :data="shelfList"
          v-loading="loading"
          empty-text="暂无货架数据"
          class="shelf-table"
          row-key="id"
        >
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column label="货架编码" width="150" fixed="left">
            <template #default="{ row }">
              <span class="shelf-code">{{ row.shelfCode }}</span>
            </template>
          </el-table-column>
          <el-table-column label="货架名称" width="150">
            <template #default="{ row }">
              <span>{{ row.shelfName }}</span>
            </template>
          </el-table-column>
          <el-table-column label="仓库" width="120">
            <template #default="{ row }">
              <span>{{ row.warehouseName }}</span>
            </template>
          </el-table-column>
          <el-table-column label="位置信息" width="200">
            <template #default="{ row }">
              <div class="location-info">
                <div v-if="row.area" class="location-item">
                  <span class="label">区域:</span>
                  <span class="value">{{ row.area }}</span>
                </div>
                <div v-if="row.rowN" class="location-item">
                  <span class="label">排:</span>
                  <span class="value">{{ row.rowN }}</span>
                </div>
                <div v-if="row.columnN" class="location-item">
                  <span class="label">列:</span>
                  <span class="value">{{ row.columnN }}</span>
                </div>
                <div v-if="row.layer" class="location-item">
                  <span class="label">层:</span>
                  <span class="value">{{ row.layer }}</span>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="容量" width="120" align="center">
            <template #default="{ row }">
              <span v-if="row.capacity">
                {{ row.capacity }} {{ row.capacityUnit || '' }}
              </span>
              <span v-else class="no-data">--</span>
            </template>
          </el-table-column>
          <el-table-column label="使用率" width="100" align="center">
            <template #default="{ row }">
              <el-progress 
                :percentage="row.utilizationRate || 0" 
                :show-text="false"
                :color="getUtilizationColor(row.utilizationRate)"
              />
              <span class="utilization-text">{{ row.utilizationRate || 0 }}%</span>
            </template>
          </el-table-column>
          <el-table-column label="产品数量" width="100" align="center">
            <template #default="{ row }">
              <span>{{ row.productCount || 0 }}</span>
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
          <el-table-column label="排序" width="80" align="center">
            <template #default="{ row }">
              <span>{{ row.sortOrder || 0 }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right" align="center">
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
                <el-button
                  type="info"
                  link
                  size="small"
                  @click="handleManageLocation(row)"
                >
                  位置管理
                </el-button>
                <el-button
                  :type="row.status === 1 ? 'danger' : 'success'"
                  link
                  size="small"
                  @click="handleToggleStatus(row)"
                >
                  {{ row.status === 1 ? '禁用' : '启用' }}
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

    <!-- 货架表单对话框 -->
    <el-dialog
      v-model="formDialogVisible"
      :title="formTitle"
      width="600px"
      top="5vh"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="货架编码" prop="shelfCode">
          <el-input
            v-model="formData.shelfCode"
            placeholder="请输入货架编码"
            maxlength="50"
          />
        </el-form-item>
        <el-form-item label="货架名称" prop="shelfName">
          <el-input
            v-model="formData.shelfName"
            placeholder="请输入货架名称"
            maxlength="100"
          />
        </el-form-item>
        <el-form-item label="所属仓库" prop="warehouseId">
          <el-select
            v-model="formData.warehouseId"
            placeholder="请选择仓库"
            style="width: 100%"
          >
            <el-option
              v-for="warehouse in warehouseList"
              :key="warehouse.id"
              :label="warehouse.name"
              :value="warehouse.id"
            />
          </el-select>
        </el-form-item>
        <el-row>
          <el-col :span="12">
            <el-form-item label="区域" prop="area">
              <el-select
                v-model="formData.area"
                placeholder="请选择区域"
                style="width: 100%"
              >
                <el-option
                  v-for="area in areaOptions"
                  :key="area"
                  :label="area"
                  :value="area"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排" prop="row">
              <el-input
                v-model="formData.rowN"
                placeholder="请输入排号"
                maxlength="20"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="列" prop="column">
              <el-input
                v-model="formData.columnN"
                placeholder="请输入列号"
                maxlength="20"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="层" prop="layer">
              <el-input
                v-model="formData.layer"
                placeholder="请输入层号"
                maxlength="20"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="容量" prop="capacity">
              <el-input-number
                v-model="formData.capacity"
                placeholder="请输入容量"
                :min="0"
                :precision="2"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="容量单位" prop="capacityUnit">
              <el-select
                v-model="formData.capacityUnit"
                placeholder="请选择单位"
                style="width: 100%"
              >
                <el-option label="个" value="个" />
                <el-option label="箱" value="箱" />
                <el-option label="千克" value="千克" />
                <el-option label="立方米" value="立方米" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number
            v-model="formData.sortOrder"
            placeholder="请输入排序"
            :min="0"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="formData.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="formLoading">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Refresh } from '@element-plus/icons-vue';
import { post, get } from '@/net';

const loading = ref(false);
const formLoading = ref(false);
const formDialogVisible = ref(false);
const formRef = ref();
const isEdit = ref(false);

// 筛选表单
const filterForm = reactive({
  shelfCode: '',
  shelfName: '',
  warehouseId: '',
  area: '',
  status: ''
});

// 分页信息
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
});

// 表单数据
const formData = reactive({
  id: '',
  shelfCode: '',
  shelfName: '',
  warehouseId: '',
  area: '',
  row: '',
  column: '',
  layer: '',
  capacity: null,
  capacityUnit: '',
  sortOrder: 0,
  status: 1,
  remark: ''
});

// 表单验证规则
const formRules = {
  shelfCode: [
    { required: true, message: '请输入货架编码', trigger: 'blur' }
  ],
  shelfName: [
    { required: true, message: '请输入货架名称', trigger: 'blur' }
  ],
  warehouseId: [
    { required: true, message: '请选择仓库', trigger: 'change' }
  ]
};

// 货架列表
const shelfList = ref([]);
const warehouseList = ref([]);

// 选项数据
const areaOptions = ['A区', 'B区', 'C区', 'D区', 'E区', 'F区'];
const statusOptions = [
  { value: 1, label: '启用' },
  { value: 0, label: '禁用' }
];

// 计算属性
const formTitle = computed(() => {
  return isEdit.value ? '编辑货架' : '新建货架';
});

// 方法
const loadShelfList = async () => {
  loading.value = true;
  try {
    const params = {
      page: pagination.current,
      size: pagination.size,
      ...filterForm
    };
    
    const res = await post('/api/auth/shelf/pageList', params);
    if (res && res.records) {
      shelfList.value = res.records.map(shelf => ({
        id: shelf.id || '',
        shelfCode: shelf.shelfCode || '',
        shelfName: shelf.shelfName || '',
        warehouseId: shelf.warehouseId || '',
        warehouseName: shelf.warehouseName || '',
        area: shelf.area || '',
        row: shelf.rowN || '',
        column: shelf.columnN || '',
        layer: shelf.layer || '',
        capacity: shelf.capacity || null,
        capacityUnit: shelf.capacityUnit || '',
        utilizationRate: shelf.utilizationRate || 0,
        productCount: shelf.productCount || 0,
        sortOrder: shelf.sortOrder || 0,
        status: shelf.status || 0,
        remark: shelf.remark || '',
        createdAt: shelf.createdAt || new Date().toISOString()
      }));
      pagination.total = res.total || 0;
    } else {
      shelfList.value = [];
      pagination.total = 0;
    }
  } catch (error) {
    console.error('加载货架列表失败:', error);
    ElMessage.error('加载货架列表失败');
    shelfList.value = [];
  } finally {
    loading.value = false;
  }
};

const loadWarehouseList = async () => {
  try {
    const res = await get('/api/auth/warehouse/list');
    warehouseList.value = res || [];
  } catch (error) {
    console.error('加载仓库列表失败:', error);
    warehouseList.value = [];
  }
};

const refreshList = () => {
  pagination.current = 1;
  loadShelfList();
};

const handleSearch = () => {
  pagination.current = 1;
  loadShelfList();
};

const handleReset = () => {
  Object.assign(filterForm, {
    shelfCode: '',
    shelfName: '',
    warehouseId: '',
    area: '',
    status: ''
  });
  pagination.current = 1;
  loadShelfList();
};

const handleSizeChange = (size) => {
  pagination.size = size;
  pagination.current = 1;
  loadShelfList();
};

const handleCurrentChange = (page) => {
  pagination.current = page;
  loadShelfList();
};

const handleCreate = () => {
  isEdit.value = false;
  resetForm();
  formDialogVisible.value = true;
};

const handleEdit = (shelf) => {
  isEdit.value = true;
  resetForm();
  Object.assign(formData, {
    id: shelf.id,
    shelfCode: shelf.shelfCode,
    shelfName: shelf.shelfName,
    warehouseId: shelf.warehouseId,
    area: shelf.area,
    row: shelf.rowN,
    column: shelf.columnN,
    layer: shelf.layer,
    capacity: shelf.capacity,
    capacityUnit: shelf.capacityUnit,
    sortOrder: shelf.sortOrder,
    status: shelf.status,
    remark: shelf.remark
  });
  formDialogVisible.value = true;
};

const handleView = (shelf) => {
  // 查看货架详情逻辑
  ElMessage.info('查看货架详情');
};

const handleManageLocation = (shelf) => {
  // 管理货架位置逻辑
  ElMessage.info('管理货架位置');
};

const handleToggleStatus = async (shelf) => {
  try {
    const newStatus = shelf.status === 1 ? 0 : 1;
    const statusText = newStatus === 1 ? '启用' : '禁用';
    
    await ElMessageBox.confirm(
      `确定要${statusText}货架"${shelf.shelfName}"吗？`,
      `${statusText}确认`,
      { type: 'warning' }
    );
    
    const res = await post('/api/auth/shelf/updateStatus', {
      id: shelf.id,
      status: newStatus
    });
    
    if (res) {
      ElMessage.success(`${statusText}成功`);
      refreshList();
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败');
    }
  }
};

const resetForm = () => {
  Object.assign(formData, {
    id: '',
    shelfCode: '',
    shelfName: '',
    warehouseId: '',
    area: '',
    row: '',
    column: '',
    layer: '',
    capacity: null,
    capacityUnit: '',
    sortOrder: 0,
    status: 1,
    remark: ''
  });
  if (formRef.value) {
    formRef.value.clearValidate();
  }
};

const handleSubmit = async () => {
  if (!formRef.value) return;
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      formLoading.value = true;
      try {
        const url = isEdit.value ? '/api/auth/shelf/update' : '/api/auth/shelf/create';
        const res = await post(url, formData);
        
        if (res) {
          ElMessage.success(isEdit.value ? '更新成功' : '创建成功');
          formDialogVisible.value = false;
          refreshList();
        }
      } catch (error) {
        console.error('提交失败:', error);
      } finally {
        formLoading.value = false;
      }
    }
  });
};

const getUtilizationColor = (percentage) => {
  if (percentage < 70) {
    return '#67C23A';
  } else if (percentage < 90) {
    return '#E6A23C';
  } else {
    return '#F56C6C';
  }
};

onMounted(() => {
  loadShelfList();
  loadWarehouseList();
});
</script>

<style scoped>
.shelf-manage-container {
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

.shelf-list-section {
  margin-top: 20px;
}

.shelf-table {
  width: 100%;
}

.shelf-code {
  font-family: 'Courier New', monospace;
  font-weight: bold;
  color: #409EFF;
}

.location-info {
  font-size: 12px;
}

.location-item {
  display: flex;
  align-items: center;
  margin-bottom: 2px;
}

.location-item .label {
  color: #909399;
  margin-right: 4px;
  min-width: 30px;
}

.location-item .value {
  color: #606266;
  font-weight: 500;
}

.utilization-text {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  display: block;
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

.no-data {
  color: #909399;
  font-style: italic;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .shelf-manage-container {
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
  
  .action-buttons {
    flex-direction: column;
    gap: 4px;
  }
}

/* 动画效果 */
.shelf-table :deep(.el-table__row) {
  transition: all 0.3s;
}

.shelf-table :deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}
</style>