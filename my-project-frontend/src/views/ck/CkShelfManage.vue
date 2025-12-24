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
          <el-table-column label="货架名称" width="200">
            <template #default="{ row }">
              <span>{{ row.shelfName }}</span>
            </template>
          </el-table-column>
          <el-table-column label="仓库" width="150">
            <template #default="{ row }">
              <span>{{ row.warehouseName }}</span>
            </template>
          </el-table-column>
          <!-- <el-table-column label="位置信息" width="200">
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
          </el-table-column> -->
          <el-table-column label="容量" width="150" align="center">
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
          <el-table-column label="产品数量" width="200" align="center">
            <template #default="{ row }">
              <span>{{ row.productCount || 0 }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag 
                :type="row.status === 1 ? 'success' : 'danger'" 
                size="small"
              >
                {{ row.status === 1 ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="排序" width="100" align="center">
            <template #default="{ row }">
              <span>{{ row.sortOrder || 0 }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="250" fixed="right" align="center">
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
                  :type="row.status === 1 ? 'danger' : 'success'"
                  link
                  size="small"
                  @click="handleToggleStatus(row)"
                >
                  {{ row.status === 1 ? '禁用' : '启用' }}
                </el-button>
                <el-button type="danger" link @click="handleDelete(row)">
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

    <!-- 货架编辑/创建对话框 -->
    <el-dialog
      v-model="formDialogVisible"
      :title="formTitle"
      width="800px"
      top="5vh"
      class="shelf-form-dialog"
    >
      <div class="shelf-form-container">
        <!-- 基本信息卡片 -->
        <el-card class="form-card basic-info-form" shadow="never">
          <template #header>
            <div class="card-header">
              <span class="card-title">基本信息</span>
              <el-tag 
                v-if="isEdit"
                :type="formData.status === 1 ? 'success' : 'danger'" 
                size="large"
              >
                {{ formData.status === 1 ? '启用' : '禁用' }}
              </el-tag>
            </div>
          </template>
          
          <el-form
            ref="formRef"
            :model="formData"
            :rules="formRules"
            label-width="100px"
            class="compact-form"
          >
            <div class="form-grid">
              <div class="form-group">
                <el-form-item label="货架编码" prop="shelfCode">
                  <el-input
                    v-model="formData.shelfCode"
                    placeholder="请输入货架编码"
                    maxlength="50"
                    class="form-input"
                  />
                </el-form-item>
              </div>
              
              <div class="form-group">
                <el-form-item label="货架名称" prop="shelfName">
                  <el-input
                    v-model="formData.shelfName"
                    placeholder="请输入货架名称"
                    maxlength="100"
                    class="form-input"
                  />
                </el-form-item>
              </div>
              
              <div class="form-group">
                <el-form-item label="所属仓库" prop="warehouseId">
                  <el-select
                    v-model="formData.warehouseId"
                    placeholder="请选择仓库"
                    class="form-select"
                  >
                    <el-option
                      v-for="warehouse in warehouseList"
                      :key="warehouse.id"
                      :label="warehouse.name"
                      :value="warehouse.id"
                    />
                  </el-select>
                </el-form-item>
              </div>
              
              <div class="form-group">
                <el-form-item label="区域" prop="area">
                  <el-select
                    v-model="formData.area"
                    placeholder="请选择区域"
                    class="form-select"
                  >
                    <el-option
                      v-for="area in areaOptions"
                      :key="area"
                      :label="area"
                      :value="area"
                    />
                  </el-select>
                </el-form-item>
              </div>
              
              <div class="form-group">
                <el-form-item label="容量" prop="capacity">
                  <div class="capacity-input-group">
                    <el-input-number
                      v-model="formData.capacity"
                      placeholder="请输入容量"
                      :min="0"
                      :precision="2"
                      class="capacity-input"
                    />
                    <el-select
                      v-model="formData.capacityUnit"
                      placeholder="单位"
                      class="capacity-unit"
                    >
                      <el-option label="个" value="个" />
                      <el-option label="箱" value="箱" />
                      <el-option label="千克" value="千克" />
                      <el-option label="立方米" value="立方米" />
                    </el-select>
                  </div>
                </el-form-item>
              </div>
              
              <div class="form-group">
                <el-form-item label="排序" prop="sortOrder">
                  <el-input-number
                    v-model="formData.sortOrder"
                    placeholder="请输入排序"
                    :min="0"
                    class="form-input"
                  />
                </el-form-item>
              </div>
            </div>
          </el-form>
        </el-card>

        <!-- 位置信息卡片 -->
        <el-card class="form-card location-form" shadow="never">
          <template #header>
            <div class="card-header">
              <span class="card-title">位置信息</span>
            </div>
          </template>
          
          <div class="location-form-grid">
            <el-form :model="formData" label-width="80px">
              <el-form-item label="排" prop="rowN" class="location-form-item">
                <el-input
                  v-model="formData.rowN"
                  placeholder="请输入排号"
                  maxlength="20"
                  class="location-input"
                />
              </el-form-item>
              
              <el-form-item label="列" prop="columnN" class="location-form-item">
                <el-input
                  v-model="formData.columnN"
                  placeholder="请输入列号"
                  maxlength="20"
                  class="location-input"
                />
              </el-form-item>
              
              <el-form-item label="层" prop="layer" class="location-form-item">
                <el-input
                  v-model="formData.layer"
                  placeholder="请输入层号"
                  maxlength="20"
                  class="location-input"
                />
              </el-form-item>
            </el-form>
          </div>
          
          <div class="location-preview">
            <div class="preview-label">位置预览：</div>
            <div class="preview-value">
              <el-tag type="info" size="large">
                {{ generateLocationPreview(formData) }}
              </el-tag>
            </div>
          </div>
        </el-card>

        <!-- 状态与备注卡片 -->
        <el-card class="form-card status-form" shadow="never">
          <template #header>
            <div class="card-header">
              <span class="card-title">状态与备注</span>
            </div>
          </template>
          
          <div class="status-form-content">
            <el-form :model="formData" label-width="100px">
              <el-form-item label="状态" prop="status" class="status-item">
                <el-radio-group v-model="formData.status" class="status-radio-group">
                  <el-radio :label="1" class="status-radio">
                    <span class="status-label">启用</span>
                  </el-radio>
                  <el-radio :label="0" class="status-radio">
                    <span class="status-label">禁用</span>
                  </el-radio>
                </el-radio-group>
              </el-form-item>
              
              <el-form-item label="备注" prop="remark" class="remark-item">
                <el-input
                  v-model="formData.remark"
                  type="textarea"
                  :rows="3"
                  placeholder="请输入备注信息"
                  maxlength="500"
                  show-word-limit
                  resize="none"
                  class="remark-textarea"
                />
              </el-form-item>
            </el-form>
          </div>
        </el-card>
      </div>
      
      <template #footer>
        <div class="form-dialog-footer">
          <el-button @click="formDialogVisible = false" class="cancel-btn">取消</el-button>
          <el-button 
            type="primary" 
            @click="handleSubmit" 
            :loading="formLoading"
            class="submit-btn"
          >
            确定
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 查看货架详情对话框 -->
    <el-dialog
      v-model="viewDialogVisible"
      title="货架详情"
      width="800px"
      top="5vh"
      class="shelf-view-dialog"
    >
      <div class="shelf-view-container" v-loading="viewLoading">
        <!-- 基本信息卡片 -->
        <el-card class="basic-info-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span class="card-title">基本信息</span>
              <el-tag 
                :type="currentShelf.status === 1 ? 'success' : 'danger'" 
                size="large"
              >
                {{ currentShelf.status === 1 ? '启用' : '禁用' }}
              </el-tag>
            </div>
          </template>
          <div class="info-grid">
            <div class="info-item">
              <span class="info-label">货架编码</span>
              <span class="info-value highlight">{{ currentShelf.shelfCode }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">货架名称</span>
              <span class="info-value">{{ currentShelf.shelfName }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">所属仓库</span>
              <span class="info-value">{{ currentShelf.warehouseName }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">区域位置</span>
              <span class="info-value">
                {{ formatLocation(currentShelf) }}
              </span>
            </div>
            <div class="info-item">
              <span class="info-label">容量</span>
              <span class="info-value">{{ currentShelf.capacity }} {{ currentShelf.capacityUnit }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">排序</span>
              <span class="info-value">{{ currentShelf.sortOrder }}</span>
            </div>
            <div class="info-item full-width">
              <span class="info-label">备注</span>
              <span class="info-value">{{ currentShelf.remark || '无' }}</span>
            </div>
          </div>
        </el-card>

        <!-- 使用情况卡片 -->
        <el-card class="usage-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span class="card-title">使用情况</span>
            </div>
          </template>
          <div class="usage-content">
            <!-- 容量进度条 -->
            <div class="capacity-progress">
              <div class="progress-header">
                <span class="progress-title">容量使用率</span>
                <span class="progress-value">{{ currentShelf.utilizationRate || 0 }}%</span>
              </div>
              <el-progress 
                :percentage="currentShelf.utilizationRate || 0" 
                :stroke-width="16"
                :color="getUtilizationColor(currentShelf.utilizationRate)"
                :show-text="false"
              />
              <div class="capacity-details">
                <div class="detail-item">
                  <span class="detail-label">总容量</span>
                  <span class="detail-value">{{ currentShelf.capacity }} {{ currentShelf.capacityUnit }}</span>
                </div>
                <div class="detail-item">
                  <span class="detail-label">已使用</span>
                  <span class="detail-value">{{ currentShelf.productCount || 0 }} {{ currentShelf.capacityUnit }}</span>
                </div>
                <div class="detail-item">
                  <span class="detail-label">可用容量</span>
                  <span class="detail-value success">{{ currentShelf.availableCapacity || currentShelf.capacity }} {{ currentShelf.capacityUnit }}</span>
                </div>
              </div>
            </div>

            <!-- 使用情况图表 -->
            <div class="usage-chart">
              <div class="chart-container">
                <div class="chart-bar" :style="{ width: `${currentShelf.utilizationRate || 0}%` }"></div>
              </div>
              <div class="chart-legend">
                <div class="legend-item">
                  <span class="legend-color used"></span>
                  <span class="legend-text">已使用 ({{ currentShelf.utilizationRate || 0 }}%)</span>
                </div>
                <div class="legend-item">
                  <span class="legend-color available"></span>
                  <span class="legend-text">可用 ({{ 100 - (currentShelf.utilizationRate || 0) }}%)</span>
                </div>
              </div>
            </div>

            <!-- 关键指标 -->
            <div class="key-metrics">
              <div class="metric-card">
                <div class="metric-icon">
                  <el-icon><Box /></el-icon>
                </div>
                <div class="metric-content">
                  <div class="metric-value">{{ currentShelf.productCount || 0 }}</div>
                  <div class="metric-label">产品种类</div>
                </div>
              </div>
              <div class="metric-card">
                <div class="metric-icon warning">
                  <el-icon><TrendCharts /></el-icon>
                </div>
                <div class="metric-content">
                  <div class="metric-value">{{ currentShelf.utilizationRate || 0 }}%</div>
                  <div class="metric-label">使用率</div>
                </div>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 位置信息卡片 -->
        <el-card class="location-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span class="card-title">位置信息</span>
            </div>
          </template>
          <div class="location-details">
            <div class="location-grid">
              <div class="location-item">
                <div class="location-icon">
                  <el-icon><Location /></el-icon>
                </div>
                <div class="location-content">
                  <div class="location-title">区域</div>
                  <div class="location-value">{{ currentShelf.area || '未设置' }}</div>
                </div>
              </div>
              <div class="location-item">
                <div class="location-icon">
                  <el-icon><Grid /></el-icon>
                </div>
                <div class="location-content">
                  <div class="location-title">排</div>
                  <div class="location-value">{{ currentShelf.rowN || '--' }}</div>
                </div>
              </div>
              <div class="location-item">
                <div class="location-icon">
                  <el-icon><Menu /></el-icon>
                </div>
                <div class="location-content">
                  <div class="location-title">列</div>
                  <div class="location-value">{{ currentShelf.columnN || '--' }}</div>
                </div>
              </div>
              <div class="location-item">
                <div class="location-icon">
                  <el-icon><Histogram /></el-icon>
                </div>
                <div class="location-content">
                  <div class="location-title">层</div>
                  <div class="location-value">{{ currentShelf.layer || '--' }}</div>
                </div>
              </div>
            </div>
            <div class="location-code">
              <span class="code-label">完整位置编码：</span>
              <span class="code-value">{{ generateLocationCode(currentShelf) }}</span>
            </div>
          </div>
        </el-card>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="viewDialogVisible = false">关闭</el-button>
          <el-button type="primary" @click="handleViewEdit">编辑货架</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { 
  Plus, 
  Refresh, 
  Box, 
  Location, 
  Grid, 
  Menu, 
  Histogram,
  TrendCharts 
} from '@element-plus/icons-vue';
import { post, get } from '@/net';

const loading = ref(false);
const formLoading = ref(false);
const formDialogVisible = ref(false);
const viewDialogVisible = ref(false);
const viewLoading = ref(false);
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
  rowN: '',
  columnN: '',
  layer: '',
  capacity: null,
  capacityUnit: '',
  sortOrder: 0,
  status: 1,
  remark: ''
});

// 当前查看的货架
const currentShelf = reactive({
  id: '',
  shelfCode: '',
  shelfName: '',
  warehouseId: '',
  warehouseName: '',
  area: '',
  rowN: '',
  columnN: '',
  layer: '',
  capacity: 0,
  availableCapacity: 0,
  capacityUnit: '',
  status: 0,
  sortOrder: 0,
  remark: '',
  utilizationRate: 0,
  productCount: 0
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
        rowN: shelf.rowN || '',
        columnN: shelf.columnN || '',
        layer: shelf.layer || '',
        capacity: shelf.capacity || 0,
        availableCapacity: shelf.availableCapacity || shelf.capacity || 0,
        capacityUnit: shelf.capacityUnit || '',
        utilizationRate: shelf.utilizationRate || 0,
        productCount: shelf.productCount || 0,
        sortOrder: shelf.sortOrder || 0,
        status: shelf.status || 0,
        remark: shelf.remark || ''
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

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除货架"${row.shelfName}"吗？`,
      '删除确认',
      {
        type: 'warning'
      }
    );
    
    await post('/api/auth/shelf/delete', { id: row.id });
    ElMessage.success('删除成功');
    loadShelfList();
  } catch (error) {
    // 用户取消删除
  }
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
    rowN: shelf.rowN,
    columnN: shelf.columnN,
    layer: shelf.layer,
    capacity: shelf.capacity,
    capacityUnit: shelf.capacityUnit,
    sortOrder: shelf.sortOrder,
    status: shelf.status,
    remark: shelf.remark
  });
  formDialogVisible.value = true;
};

const handleView = async (shelf) => {
  try {
    viewLoading.value = true;
    // 使用列表中的完整数据
    Object.assign(currentShelf, shelf);
    viewDialogVisible.value = true;
  } catch (error) {
    console.error('加载货架详情失败:', error);
    ElMessage.error('加载货架详情失败');
  } finally {
    viewLoading.value = false;
  }
};

const handleViewEdit = () => {
  viewDialogVisible.value = false;
  handleEdit(currentShelf);
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
    rowN: '',
    columnN: '',
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

const formatLocation = (shelf) => {
  const parts = [];
  if (shelf.area) parts.push(shelf.area);
  if (shelf.rowN) parts.push(`排${shelf.rowN}`);
  if (shelf.columnN) parts.push(`列${shelf.columnN}`);
  if (shelf.layer) parts.push(`层${shelf.layer}`);
  return parts.join(' / ') || '未设置位置';
};

const generateLocationCode = (shelf) => {
  const parts = [];
  if (shelf.area) parts.push(shelf.area);
  if (shelf.rowN) parts.push(`R${shelf.rowN}`);
  if (shelf.columnN) parts.push(`C${shelf.columnN}`);
  if (shelf.layer) parts.push(`L${shelf.layer}`);
  return parts.join('-') || '未设置';
};

const generateLocationPreview = (formData) => {
  const parts = [];
  if (formData.area) parts.push(formData.area);
  if (formData.rowN) parts.push(`R${formData.rowN}`);
  if (formData.columnN) parts.push(`C${formData.columnN}`);
  if (formData.layer) parts.push(`L${formData.layer}`);
  return parts.join('-') || '未设置位置';
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

/* 查看对话框样式 */
.shelf-view-dialog :deep(.el-dialog__header) {
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 15px;
}

.shelf-view-container {
  padding: 0;
}

/* 基本信息卡片 */
.basic-info-card {
  margin-bottom: 20px;
  border-radius: 8px;
}

.basic-info-card :deep(.el-card__header) {
  padding: 16px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 8px 8px 0 0;
}

.basic-info-card .card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.basic-info-card .card-title {
  color: white;
  font-size: 16px;
  font-weight: 600;
}

.basic-info-card :deep(.el-tag) {
  font-weight: bold;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 16px;
  padding: 20px;
}

.info-item {
  display: flex;
  flex-direction: column;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 6px;
  border-left: 4px solid #409EFF;
}

.info-item.full-width {
  grid-column: 1 / -1;
}

.info-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.info-value {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

.info-value.highlight {
  color: #409EFF;
  font-weight: bold;
}

/* 使用情况卡片 */
.usage-card {
  margin-bottom: 20px;
  border-radius: 8px;
}

.usage-card :deep(.el-card__header) {
  padding: 16px 20px;
  background: #f8f9fa;
  border-bottom: 1px solid #ebeef5;
}

.usage-content {
  padding: 20px;
}

.capacity-progress {
  margin-bottom: 30px;
}

.progress-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.progress-title {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

.progress-value {
  font-size: 18px;
  font-weight: bold;
  color: #409EFF;
}

.capacity-details {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-top: 16px;
}

.detail-item {
  text-align: center;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 6px;
}

.detail-label {
  display: block;
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.detail-value {
  display: block;
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.detail-value.success {
  color: #67C23A;
}

.usage-chart {
  margin: 30px 0;
}

.chart-container {
  height: 24px;
  background: #f0f2f5;
  border-radius: 12px;
  overflow: hidden;
  position: relative;
}

.chart-bar {
  height: 100%;
  background: linear-gradient(90deg, #409EFF, #67C23A);
  border-radius: 12px;
  transition: width 1s ease;
}

.chart-legend {
  display: flex;
  justify-content: center;
  gap: 24px;
  margin-top: 12px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.legend-color {
  width: 16px;
  height: 16px;
  border-radius: 4px;
}

.legend-color.used {
  background: linear-gradient(90deg, #409EFF, #67C23A);
}

.legend-color.available {
  background: #f0f2f5;
}

.legend-text {
  font-size: 12px;
  color: #606266;
}

.key-metrics {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-top: 24px;
}

.metric-card {
  display: flex;
  align-items: center;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
  transition: all 0.3s;
}

.metric-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.metric-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  margin-right: 16px;
}

.metric-icon.warning {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.metric-icon .el-icon {
  font-size: 24px;
  color: white;
}

.metric-content {
  flex: 1;
}

.metric-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
}

.metric-label {
  font-size: 12px;
  color: #909399;
}

/* 位置信息卡片 */
.location-card {
  border-radius: 8px;
}

.location-card :deep(.el-card__header) {
  padding: 16px 20px;
  background: #f8f9fa;
  border-bottom: 1px solid #ebeef5;
}

.location-details {
  padding: 20px;
}

.location-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.location-item {
  display: flex;
  align-items: center;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  transition: all 0.3s;
}

.location-item:hover {
  background: #e6f7ff;
  border-color: #91d5ff;
}

.location-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #e6f7ff;
  border-radius: 50%;
  margin-right: 12px;
}

.location-icon .el-icon {
  font-size: 20px;
  color: #409EFF;
}

.location-content {
  flex: 1;
}

.location-title {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.location-value {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.location-code {
  padding: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 8px;
  text-align: center;
}

.code-label {
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
}

.code-value {
  color: white;
  font-size: 18px;
  font-weight: bold;
  font-family: 'Courier New', monospace;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

/* 编辑对话框样式 */
.shelf-form-dialog :deep(.el-dialog__header) {
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 15px;
}

.shelf-form-container {
  padding: 0;
}

.form-card {
  margin-bottom: 20px;
  border-radius: 8px;
  border: 1px solid #ebeef5;
}

.form-card:last-child {
  margin-bottom: 0;
}

.form-card :deep(.el-card__header) {
  padding: 16px 20px;
  background: #f8f9fa;
  border-bottom: 1px solid #ebeef5;
}

.form-card .card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.form-card .card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

/* 基本信息表单 */
.basic-info-form :deep(.el-card__header) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.basic-info-form .card-title {
  color: white;
}

.basic-info-form :deep(.el-tag) {
  font-weight: bold;
  color: white;
  border: none;
}

.compact-form {
  padding: 20px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.form-group {
  margin-bottom: 0;
}

.form-group :deep(.el-form-item__label) {
  font-weight: 500;
  color: #606266;
}

.form-input, .form-select {
  width: 100%;
}

.capacity-input-group {
  display: flex;
  gap: 8px;
  align-items: center;
}

.capacity-input {
  flex: 1;
}

.capacity-unit {
  width: 100px;
}

/* 位置信息表单 */
.location-form :deep(.el-card__header) {
  background: linear-gradient(135deg, #36d1dc 0%, #5b86e5 100%);
}

.location-form .card-title {
  color: white;
}

.location-form-grid {
  padding: 20px;
}

.location-form-item {
  margin-bottom: 16px;
}

.location-form-item :deep(.el-form-item__label) {
  font-weight: 500;
  color: #606266;
}

.location-input {
  width: 100%;
}

.location-preview {
  display: flex;
  align-items: center;
  padding: 16px 20px;
  background: #f8f9fa;
  border-top: 1px solid #ebeef5;
  margin-top: 10px;
}

.preview-label {
  font-size: 14px;
  color: #606266;
  margin-right: 12px;
  font-weight: 500;
}

.preview-value :deep(.el-tag) {
  font-size: 16px;
  font-weight: bold;
  padding: 8px 16px;
  border-radius: 6px;
  background: #f0f2f5;
  border-color: #dcdfe6;
}

/* 状态与备注表单 */
.status-form :deep(.el-card__header) {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.status-form .card-title {
  color: white;
}

.status-form-content {
  padding: 20px;
}

.status-item {
  margin-bottom: 20px;
}

.status-item :deep(.el-form-item__label) {
  font-weight: 500;
  color: #606266;
}

.status-radio-group {
  display: flex;
  gap: 20px;
}

.status-radio {
  padding: 8px 16px;
  border-radius: 6px;
  border: 1px solid #dcdfe6;
  transition: all 0.3s;
}

.status-radio:hover {
  border-color: #409EFF;
  background: #f0f7ff;
}

:deep(.el-radio__input.is-checked + .el-radio__label .status-label) {
  color: #409EFF;
  font-weight: 500;
}

.remark-item {
  margin-bottom: 0;
}

.remark-item :deep(.el-form-item__label) {
  font-weight: 500;
  color: #606266;
}

.remark-textarea :deep(.el-textarea__inner) {
  resize: none;
  border-radius: 6px;
  border: 1px solid #dcdfe6;
  transition: border-color 0.3s;
}

.remark-textarea :deep(.el-textarea__inner:focus) {
  border-color: #409EFF;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
}

/* 对话框底部 */
.form-dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.cancel-btn {
  min-width: 100px;
}

.submit-btn {
  min-width: 100px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

.submit-btn:hover {
  background: linear-gradient(135deg, #5a6fd8 0%, #6a3f9b 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
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
  
  .shelf-view-dialog,
  .shelf-form-dialog {
    width: 95% !important;
  }
  
  .info-grid {
    grid-template-columns: 1fr;
  }
  
  .capacity-details {
    grid-template-columns: 1fr;
  }
  
  .location-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .key-metrics {
    grid-template-columns: 1fr;
  }
  
  .form-grid {
    grid-template-columns: 1fr;
  }
  
  .capacity-input-group {
    flex-direction: column;
  }
  
  .capacity-unit {
    width: 100%;
  }
  
  .status-radio-group {
    flex-direction: column;
    gap: 8px;
  }
  
  .cancel-btn, .submit-btn {
    min-width: 80px;
  }
}

/* 动画效果 */
.shelf-table :deep(.el-table__row) {
  transition: all 0.3s;
}

.shelf-table :deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.shelf-view-container {
  animation: fadeIn 0.3s ease;
}

.form-card {
  animation: slideUp 0.3s ease;
}

.location-preview {
  animation: fadeIn 0.5s ease;
}
</style>