<template>
  <div class="shelf-manage-container">
    <el-card class="manage-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">货架管理</span>
          <div class="header-actions">
            <el-button 
              type="primary" 
              @click="handleCreateShelf"
              class="responsive-btn"
            >
              <el-icon><Plus /></el-icon>
              <span class="btn-text">新建货架</span>
            </el-button>
            <el-button 
              @click="refreshList"
              :loading="loading"
              class="responsive-btn"
            >
              <el-icon><Refresh /></el-icon>
              <span class="btn-text">刷新</span>
            </el-button>
          </div>
        </div>
      </template>

      <!-- 筛选条件 -->
      <div class="filter-section">
        <el-form :model="filterForm" inline class="responsive-form">
          <el-form-item label="货架编码">
            <el-input
              v-model="filterForm.shelfCode"
              placeholder="请输入货架编码"
              clearable
              class="responsive-input"
            />
          </el-form-item>
          <el-form-item label="货架名称">
            <el-input
              v-model="filterForm.shelfName"
              placeholder="请输入货架名称"
              clearable
              class="responsive-input"
            />
          </el-form-item>
          <el-form-item label="仓库">
            <el-select
              v-model="filterForm.warehouseId"
              placeholder="全部仓库"
              clearable
              class="responsive-select"
              style="min-width: 120px;"
            >
              <el-option
                v-for="warehouse in warehouseList"
                :key="warehouse.id"
                :label="warehouse.name"
                :value="warehouse.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="类型">
            <el-select
              v-model="filterForm.shelfType"
              placeholder="全部类型"
              clearable
              class="responsive-select"
              style="min-width: 120px;"
            >
              <el-option
                v-for="type in shelfTypeOptions"
                :key="type.value"
                :label="type.label"
                :value="type.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select
              v-model="filterForm.status"
              placeholder="全部状态"
              clearable
              class="responsive-select"
              style="min-width: 120px;"
            >
              <el-option
                v-for="item in statusOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item class="form-buttons">
            <el-button type="primary" @click="handleSearch" class="responsive-btn">查询</el-button>
            <el-button @click="handleReset" class="responsive-btn">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 货架列表 -->
      <div class="shelf-list-section">
        <el-table
          :data="shelfList"
          v-loading="loading"
          empty-text="暂无货架数据"
          class="shelf-table responsive-table"
          row-key="id"
        >
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column label="货架编码" min-width="120" fixed="left">
            <template #default="{ row }">
              <span class="shelf-code">{{ row.shelfCode }}</span>
            </template>
          </el-table-column>
          <el-table-column label="货架名称" min-width="120">
            <template #default="{ row }">
              <span>{{ row.shelfName }}</span>
            </template>
          </el-table-column>
          <el-table-column label="仓库" min-width="100">
            <template #default="{ row }">
              <span>{{ row.warehouseName }}</span>
            </template>
          </el-table-column>
          <el-table-column label="货架类型" min-width="90">
            <template #default="{ row }">
              <el-tag
                :type="getShelfTypeTagType(row.shelfType)"
                size="small"
                class="responsive-tag"
              >
                {{ getShelfTypeLabel(row.shelfType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="区域数量" width="90" align="center">
            <template #default="{ row }">
              <el-tooltip
                :content="`查看${row.shelfName}的区域`"
                placement="top"
              >
                <el-button
                  type="primary"
                  link
                  @click="handleViewZones(row)"
                  class="zone-count-btn"
                >
                  {{ row.zoneCount || 0 }}
                </el-button>
              </el-tooltip>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag
              :type="row.status === 1 ? 'danger' : 'success'"
              size="small"
              class="responsive-tag"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-tag>
          </template>
        </el-table-column>
          <el-table-column label="排序" width="80" align="center">
            <template #default="{ row }">
              <span>{{ row.sortOrder || 0 }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" min-width="280" fixed="right" align="center">
            <template #default="{ row }">
              <div class="action-buttons">
                <!-- 新增查看按钮 -->
                <el-button
                  type="info"
                  link
                  size="small"
                  @click="handleViewShelf(row)"
                  class="action-btn view-btn"
                >
                  <el-icon><View /></el-icon>
                  <span class="btn-text">查看</span>
                </el-button>
                <el-button
                  type="primary"
                  link
                  size="small"
                  @click="handleEditShelf(row)"
                  class="action-btn edit-btn"
                >
                  <el-icon><Edit /></el-icon>
                  <span class="btn-text">编辑</span>
                </el-button>
                <!-- <el-button
                  type="warning"
                  link
                  size="small"
                  @click="handleManageZones(row)"
                  class="action-btn manage-btn"
                >
                  <el-icon><Setting /></el-icon>
                  <span class="btn-text">管理区域</span>
                </el-button> -->
                <el-button
                  :type="row.status === 0 ? 'danger' : 'success'"
                  link
                  size="small"
                  @click="handleToggleShelfStatus(row)"
                  class="action-btn status-btn"
                >
                  <el-icon v-if="row.status === 0"><Close /></el-icon>
                  <el-icon v-else><Check /></el-icon>
                  <span class="btn-text">{{ row.status === 0 ? '禁用' : '启用' }}</span>
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
            class="responsive-pagination"
          />
        </div>
      </div>
    </el-card>

    <!-- 货架表单对话框（包含区域管理） -->
    <el-dialog
      v-model="shelfFormDialogVisible"
      :title="shelfFormTitle"
      width="900px"
      top="3vh"
      @close="handleFormDialogClose"
      class="responsive-dialog"
    >
      <!-- 货架基本信息 -->
      <el-form
        ref="shelfFormRef"
        :model="shelfFormData"
        :rules="shelfFormRules"
        label-width="100px"
        class="shelf-base-form responsive-form"
      >
        <el-row :gutter="20" class="form-row">
          <el-col :xs="24" :sm="12">
            <el-form-item label="货架编码" prop="shelfCode">
              <el-input
                v-model="shelfFormData.shelfCode"
                placeholder="请输入货架编码"
                maxlength="50"
                class="responsive-input"
                :disabled="isViewMode"
                :readonly="isViewMode"
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="货架名称" prop="shelfName">
              <el-input
                v-model="shelfFormData.shelfName"
                placeholder="请输入货架名称"
                maxlength="100"
                class="responsive-input"
                :disabled="isViewMode"
                :readonly="isViewMode"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20" class="form-row">
          <el-col :xs="24" :sm="12">
            <el-form-item label="所属仓库" prop="warehouseId">
              <el-select
                v-model="shelfFormData.warehouseId"
                placeholder="请选择仓库"
                class="responsive-select"
                :disabled="isViewMode"
              >
                <el-option
                  v-for="warehouse in warehouseList"
                  :key="warehouse.id"
                  :label="warehouse.name"
                  :value="warehouse.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="货架类型" prop="shelfType">
              <el-select
                v-model="shelfFormData.shelfType"
                placeholder="请选择货架类型"
                class="responsive-select"
                :disabled="isViewMode"
              >
                <el-option
                  v-for="type in shelfTypeOptions"
                  :key="type.value"
                  :label="type.label"
                  :value="type.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20" class="form-row">
          <el-col :xs="24" :sm="12">
            <el-form-item label="排序" prop="sortOrder">
              <el-input-number
                v-model="shelfFormData.sortOrder"
                placeholder="请输入排序"
                :min="0"
                class="responsive-input-number"
                :disabled="isViewMode"
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="shelfFormData.status" class="responsive-radio" :disabled="isViewMode">
                <el-radio :label="0">启用</el-radio>
                <el-radio :label="1">禁用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="shelfFormData.remark"
            type="textarea"
            :rows="2"
            placeholder="请输入备注"
            maxlength="500"
            show-word-limit
            class="responsive-textarea"
            :disabled="isViewMode"
            :readonly="isViewMode"
          />
        </el-form-item>
      </el-form>

      <!-- 货架区域管理 -->
      <div class="zone-management-section">
        <div class="section-header">
          <h4>货架区域管理</h4>
          <el-button
            type="primary"
            size="small"
            @click="handleAddZone"
            class="responsive-btn"
            :disabled="isViewMode"
          >
            <el-icon><Plus /></el-icon>
            <span class="btn-text">添加区域</span>
          </el-button>
        </div>

        <el-table
          :data="zoneList"
          empty-text="暂无区域数据"
          class="zone-table responsive-table"
          size="small"
        >
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column label="区域编码" min-width="120">
            <template #default="{ row, $index }">
              <el-input
                v-model="row.zoneCode"
                placeholder="区域编码"
                size="small"
                @blur="validateZoneCode($index)"
                class="zone-input"
                :disabled="isViewMode"
                :readonly="isViewMode"
              />
            </template>
          </el-table-column>
          <el-table-column label="区域名称" min-width="120">
            <template #default="{ row }">
              <el-input
                v-model="row.zoneName"
                placeholder="区域名称"
                size="small"
                class="zone-input"
                :disabled="isViewMode"
                :readonly="isViewMode"
              />
            </template>
          </el-table-column>
          <el-table-column label="位置信息" min-width="180">
            <template #default="{ row }">
              <div class="location-inputs">
                <el-select
                  v-model="row.area"
                  placeholder="区域"
                  size="small"
                  class="location-select"
                  :disabled="isViewMode"
                >
                  <el-option
                    v-for="area in areaOptions"
                    :key="area"
                    :label="area"
                    :value="area"
                  />
                </el-select>
                <el-input
                  v-model="row.rowN"
                  placeholder="排"
                  size="small"
                  class="location-input"
                  :disabled="isViewMode"
                  :readonly="isViewMode"
                />
                <el-input
                  v-model="row.columnN"
                  placeholder="列"
                  size="small"
                  class="location-input"
                  :disabled="isViewMode"
                  :readonly="isViewMode"
                />
                <el-input
                  v-model="row.layer"
                  placeholder="层"
                  size="small"
                  class="location-input"
                  :disabled="isViewMode"
                  :readonly="isViewMode"
                />
              </div>
            </template>
          </el-table-column>
          <el-table-column label="容量" min-width="120">
            <template #default="{ row }">
              <div class="capacity-inputs">
                <el-input-number
                  v-model="row.capacity"
                  placeholder="容量"
                  :min="0"
                  :precision="2"
                  size="small"
                  class="capacity-input"
                  :disabled="isViewMode"
                />
                <el-select
                  v-model="row.capacityUnit"
                  placeholder="单位"
                  size="small"
                  class="capacity-select"
                  :disabled="isViewMode"
                >
                  <el-option label="个" value="个" />
                  <el-option label="箱" value="箱" />
                  <el-option label="千克" value="千克" />
                  <el-option label="立方米" value="立方米" />
                </el-select>
              </div>
            </template>
          </el-table-column>
          <!-- <el-table-column label="状态1" width="80" align="center">
            <template #default="{ row }">
              <el-switch
                v-model="row.status"
                :active-value=1
                :inactive-value=0
                size="small"
                :disabled="isViewMode"
              />
            </template>
          </el-table-column> -->
          <!-- <el-table-column label="排序1" width="80">
            <template #default="{ row }">
              <el-input-number
                v-model="row.sortOrder"
                :min="0"
                size="small"
                class="sort-input"
                :disabled="isViewMode"
              />
            </template>
          </el-table-column> -->
          <el-table-column label="操作" width="100" align="center" v-if="!isViewMode">
            <template #default="{ $index }">
              <el-button
                type="danger"
                link
                size="small"
                @click="handleRemoveZone($index)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
          <!-- <el-table-column label="备注1" min-width="120" v-if="isViewMode">
            <template #default="{ row }">
              <span class="zone-remark">{{ row.remark || '--' }}</span>
            </template>
          </el-table-column> -->
        </el-table>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="shelfFormDialogVisible = false" class="responsive-btn">关闭</el-button>
          <el-button
            v-if="!isViewMode"
            type="primary"
            @click="handleShelfSubmit"
            :loading="shelfFormLoading"
            class="responsive-btn"
          >
            保存
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 查看货架详情对话框（复用货架表单对话框） -->
    <!-- 这个对话框现在通过 isViewMode 变量控制为查看模式 -->

    <!-- 查看区域详情对话框（单独的，只显示区域） -->
    <el-dialog
      v-model="zoneViewDialogVisible"
      title="货架区域详情"
      width="800px"
      top="5vh"
      class="responsive-dialog"
    >
      <el-table
        :data="currentZones"
        empty-text="暂无区域数据"
        class="zone-view-table responsive-table"
      >
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column label="区域编码" min-width="100">
          <template #default="{ row }">
            <span class="zone-code">{{ row.zoneCode }}</span>
          </template>
        </el-table-column>
        <el-table-column label="区域名称" min-width="100">
          <template #default="{ row }">
            {{ row.zoneName }}
          </template>
        </el-table-column>
        <el-table-column label="位置信息" min-width="150">
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
        <el-table-column label="容量" width="100" align="center">
          <template #default="{ row }">
            <span v-if="row.capacity">
              {{ row.capacity }} {{ row.capacityUnit || '' }}
            </span>
            <span v-else class="no-data">--</span>
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
            {{ row.sortOrder || 0 }}
          </template>
        </el-table-column>
        <el-table-column label="备注" min-width="120">
          <template #default="{ row }">
            <span class="zone-remark">{{ row.remark || '--' }}</span>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="zoneViewDialogVisible = false" class="responsive-btn">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Refresh, View, Edit, Setting, Close, Check } from '@element-plus/icons-vue';
import { post, get } from '@/net';

const loading = ref(false);
const shelfFormLoading = ref(false);
const shelfFormDialogVisible = ref(false);
const isViewMode = ref(false); // 新增：用于区分查看模式和编辑模式
const zoneViewDialogVisible = ref(false);
const shelfFormRef = ref();
const isShelfEdit = ref(false);

// 筛选表单
const filterForm = reactive({
  shelfCode: '',
  shelfName: '',
  warehouseId: '',
  shelfType: '',
  status: ''
});

// 分页信息
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
});

// 货架表单数据
const shelfFormData = reactive({
  id: '',
  shelfCode: '',
  shelfName: '',
  warehouseId: '',
  shelfType: 0,
  sortOrder: 0,
  status: 1,
  remark: ''
});

// 货架区域列表（用于表单中的区域管理）
const zoneList = ref([]);

// 当前查看的区域列表（用于单独的查看区域对话框）
const currentZones = ref([]);

// 表单验证规则
const shelfFormRules = {
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

const shelfTypeOptions = [
  { value: 0, label: '普通货架' },
  { value: 1, label: '自动化货架' },
  { value: 2, label: '流利式货架' },
  { value: 3, label: '阁楼式货架' }
];

// 计算属性
const shelfFormTitle = computed(() => {
  if (isViewMode.value) return '查看货架详情';
  return isShelfEdit.value ? '编辑货架' : '新建货架';
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
        shelfType: shelf.shelfType || 0,
        zoneCount: shelf.zoneCount || 0,
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

const loadShelfZones = async (shelfId) => {
  try {
    const res = await get(`/api/auth/shelf/shelfZoneList?parentId=${shelfId}`);
    return res || [];
  } catch (error) {
    console.error('加载货架区域失败:', error);
    return [];
  }
};

// 新增：查看货架详情（包含区域）
const handleViewShelf = async (shelf) => {
  isViewMode.value = true;
  isShelfEdit.value = false;
  resetShelfForm();

  // 加载货架基本信息
  Object.assign(shelfFormData, {
    id: shelf.id,
    shelfCode: shelf.shelfCode,
    shelfName: shelf.shelfName,
    warehouseId: shelf.warehouseId,
    shelfType: shelf.shelfType,
    sortOrder: shelf.sortOrder,
    status: shelf.status,
    remark: shelf.remark
  });

  // 加载货架区域
  const zones = await loadShelfZones(shelf.id);
  zoneList.value = zones.map(zone => ({
    id: zone.id || '',
    zoneCode: zone.zoneCode || '',
    zoneName: zone.zoneName || '',
    area: zone.area || '',
    rowN: zone.rowN || '',
    columnN: zone.columnN || '',
    layer: zone.layer || '',
    capacity: zone.capacity || null,
    capacityUnit: zone.capacityUnit || '',
    sortOrder: zone.sortOrder || 0,
    status: zone.status || 1, // 保持原值：1为启用，0为禁用
    remark: zone.remark || ''
  }));

  shelfFormDialogVisible.value = true;
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
    shelfType: '',
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

const handleCreateShelf = () => {
  isViewMode.value = false;
  isShelfEdit.value = false;
  resetShelfForm();
  // 初始化一个默认区域
  zoneList.value = [createDefaultZone()];
  shelfFormDialogVisible.value = true;
};

const handleEditShelf = async (shelf) => {
  isViewMode.value = false;
  isShelfEdit.value = true;
  resetShelfForm();

  // 加载货架基本信息
  Object.assign(shelfFormData, {
    id: shelf.id,
    shelfCode: shelf.shelfCode,
    shelfName: shelf.shelfName,
    warehouseId: shelf.warehouseId,
    shelfType: shelf.shelfType,
    sortOrder: shelf.sortOrder,
    status: shelf.status,
    remark: shelf.remark
  });

  // 加载货架区域
  const zones = await loadShelfZones(shelf.id);
  zoneList.value = zones.map(zone => ({
    id: zone.id || '',
    zoneCode: zone.zoneCode || '',
    zoneName: zone.zoneName || '',
    area: zone.area || '',
    rowN: zone.rowN || '',
    columnN: zone.columnN || '',
    layer: zone.layer || '',
    capacity: zone.capacity || null,
    capacityUnit: zone.capacityUnit || '',
    sortOrder: zone.sortOrder || 0,
    status: zone.status || 1, // 保持原值：1为启用，0为禁用
    remark: zone.remark || ''
  }));

  shelfFormDialogVisible.value = true;
};

const handleViewZones = async (shelf) => {
  const zones = await loadShelfZones(shelf.id);
  currentZones.value = zones.map(zone => ({
    id: zone.id || '',
    zoneCode: zone.zoneCode || '',
    zoneName: zone.zoneName || '',
    area: zone.area || '',
    rowN: zone.rowN || '',
    columnN: zone.columnN || '',
    layer: zone.layer || '',
    capacity: zone.capacity || null,
    capacityUnit: zone.capacityUnit || '',
    sortOrder: zone.sortOrder || 0,
    status: zone.status || 0, // 修正：保持原值，1为启用，0为禁用
    remark: zone.remark || ''
  }));
  zoneViewDialogVisible.value = true;
};

const handleManageZones = async (shelf) => {
  // 管理区域 - 打开编辑对话框
  await handleEditShelf(shelf);
};

const handleToggleShelfStatus = async (shelf) => {
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
      ElMessage.success(`成功`);
      // 修复1：操作完成后刷新列表
      await loadShelfList();
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败');
    }
  }
};

const createDefaultZone = () => {
  return {
    id: '',
    zoneCode: '',
    zoneName: '',
    area: '',
    rowN: '',
    columnN: '',
    layer: '',
    capacity: null,
    capacityUnit: '个',
    sortOrder: 0,
    status: 1, // 默认启用
    remark: ''
  };
};

const handleAddZone = () => {
  zoneList.value.push(createDefaultZone());
};

const handleRemoveZone = (index) => {
  if (zoneList.value.length > 1) {
    zoneList.value.splice(index, 1);
  } else {
    ElMessage.warning('至少需要保留一个区域');
  }
};

const validateZoneCode = (index) => {
  const zoneCode = zoneList.value[index].zoneCode;
  if (!zoneCode) return;

  // 检查是否有重复的区域编码
  const duplicates = zoneList.value.filter((zone, i) =>
    i !== index && zone.zoneCode === zoneCode
  );

  if (duplicates.length > 0) {
    ElMessage.warning('区域编码不能重复');
    zoneList.value[index].zoneCode = '';
  }
};

const resetShelfForm = () => {
  Object.assign(shelfFormData, {
    id: '',
    shelfCode: '',
    shelfName: '',
    warehouseId: '',
    shelfType: 0,
    sortOrder: 0,
    status: 1,
    remark: ''
  });
  zoneList.value = [];
  if (shelfFormRef.value) {
    shelfFormRef.value.clearValidate();
  }
};

const handleFormDialogClose = () => {
  // 清空区域列表
  zoneList.value = [];
  // 重置查看模式
  isViewMode.value = false;
};

const handleShelfSubmit = async () => {
  if (!shelfFormRef.value) return;
  
  await shelfFormRef.value.validate(async (valid) => {
    if (valid) {
      // 验证区域数据
      let hasError = false;
      for (let i = 0; i < zoneList.value.length; i++) {
        const zone = zoneList.value[i];
        if (!zone.zoneCode) {
          ElMessage.warning(`第${i + 1}个区域编码不能为空`);
          hasError = true;
          break;
        }
        if (!zone.zoneName) {
          ElMessage.warning(`第${i + 1}个区域名称不能为空`);
          hasError = true;
          break;
        }
      }

      if (hasError) return;

      // 检查区域编码重复
      const zoneCodes = zoneList.value.map(zone => zone.zoneCode);
      const uniqueZoneCodes = new Set(zoneCodes);
      if (zoneCodes.length !== uniqueZoneCodes.size) {
        ElMessage.warning('区域编码不能重复');
        return;
      }

      shelfFormLoading.value = true;
      try {
        const requestData = {
          shelf: shelfFormData,
          zones: zoneList.value
        };

        const url = isShelfEdit.value ? '/api/auth/shelf/updateWithZones' : '/api/auth/shelf/createWithZones';
        const res = await post(url, requestData);
        
        if (res) {
          ElMessage.success(isShelfEdit.value ? '更新成功' : '创建成功');
          shelfFormDialogVisible.value = false;
          // 修复1：保存成功后刷新列表
          await loadShelfList();
        }
      } catch (error) {
        console.error('提交失败:', error);
      } finally {
        shelfFormLoading.value = false;
      }
    }
  });
};

const getShelfTypeLabel = (type) => {
  const option = shelfTypeOptions.find(opt => opt.value === type);
  return option ? option.label : '未知';
};

const getShelfTypeTagType = (type) => {
  switch (type) {
    case 0: return ''; // 普通
    case 1: return 'success'; // 自动化
    case 2: return 'warning'; // 流利式
    case 3: return 'info'; // 阁楼式
    default: return '';
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
  box-sizing: border-box;
}

.manage-card {
  border-radius: 8px;
  overflow: hidden;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.card-title {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
  line-height: 1;
}

.header-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
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
  overflow-x: auto;
}

.shelf-code {
  font-family: 'Courier New', monospace;
  font-weight: bold;
  color: #409EFF;
}

.zone-management-section {
  margin-top: 20px;
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 4px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  flex-wrap: wrap;
  gap: 10px;
}

.section-header h4 {
  margin: 0;
  color: #303133;
  font-size: 16px;
}

.zone-table {
  width: 100%;
  background-color: white;
  border-radius: 4px;
  overflow-x: auto;
}

.location-inputs {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 5px;
}

.capacity-inputs {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 5px;
}

.zone-view-table {
  width: 100%;
  overflow-x: auto;
}

.zone-code {
  font-family: 'Courier New', monospace;
  font-weight: bold;
  color: #67C23A;
}

.zone-remark {
  color: #909399;
  font-size: 12px;
  font-style: italic;
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

.action-buttons {
  display: flex;
  gap: 8px;
  justify-content: center;
  flex-wrap: wrap;
}

.action-btn {
  min-width: 60px;
}

.view-btn { color: #909399; }
.edit-btn { color: #409EFF; }
.manage-btn { color: #E6A23C; }
.status-btn { color: #F56C6C; }

.zone-count-btn {
  padding: 0;
  min-height: auto;
}

.btn-text {
  margin-left: 4px;
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

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* 查看模式样式 */
:deep(.view-mode .el-input .el-input__wrapper) {
  background-color: #f5f7fa;
  border-color: transparent;
}

:deep(.view-mode .el-input.is-disabled .el-input__wrapper) {
  background-color: #f5f7fa;
}

:deep(.view-mode .el-select .el-input .el-input__wrapper) {
  background-color: #f5f7fa;
}

:deep(.view-mode .el-input-number .el-input__wrapper) {
  background-color: #f5f7fa;
}

:deep(.view-mode .el-radio-group) {
  pointer-events: none;
}

:deep(.view-mode .el-switch.is-disabled) {
  opacity: 1;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .manage-card {
    margin: 0 -10px;
  }
}

@media (max-width: 992px) {
  .responsive-form {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .responsive-form .el-form-item {
    margin-bottom: 0;
    width: 100%;
  }

  .form-row {
    margin: 0 !important;
  }

  .form-row .el-col {
    width: 100%;
    margin-bottom: 12px;
  }
}

@media (max-width: 768px) {
  .shelf-manage-container {
    padding: 10px;
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .header-actions {
    width: 100%;
    justify-content: flex-start;
  }
  
  .responsive-btn .btn-text {
    display: inline;
  }
  
  .action-buttons {
    flex-direction: row;
    gap: 4px;
  }

  .action-btn .btn-text {
    display: none;
  }

  .location-inputs,
  .capacity-inputs {
    flex-direction: column;
    gap: 5px;
  }

  .location-input,
  .location-select,
  .capacity-input,
  .capacity-select {
    width: 100% !important;
  }

  .responsive-dialog {
    width: 95% !important;
    margin: 2vh auto;
  }

  .responsive-table {
    font-size: 12px;
  }

  .responsive-tag {
    font-size: 10px;
    padding: 2px 6px;
  }

  .responsive-pagination {
    font-size: 12px;
  }
}

@media (max-width: 576px) {
  .card-header {
    flex-direction: column;
    gap: 10px;
  }

  .header-actions {
    justify-content: space-between;
    width: 100%;
  }

  .header-actions .el-button {
    flex: 1;
    min-width: 0;
  }

  .filter-section .el-form-item {
    width: 100%;
    margin-bottom: 8px;
  }

  .form-buttons {
    width: 100%;
    display: flex;
    gap: 10px;
  }

  .form-buttons .el-button {
    flex: 1;
  }

  .responsive-input,
  .responsive-select {
    width: 100% !important;
  }

  .responsive-input-number {
    width: 100% !important;
  }

  .responsive-radio {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
  }

  .responsive-textarea {
    width: 100%;
  }

  .action-buttons {
    justify-content: space-around;
  }

  .action-btn {
    min-width: 40px;
  }
}

/* 小屏幕设备 */
@media (max-width: 375px) {
  .btn-text {
    display: none;
  }

  .header-actions .el-button span:not(.btn-text) {
    display: none;
  }

  .action-buttons {
    justify-content: center;
  }

  .action-btn {
    padding: 4px;
  }
}

/* 动画效果 */
.shelf-table :deep(.el-table__row) {
  transition: all 0.3s;
}

.shelf-table :deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}

/* 滚动条优化 */
.responsive-table :deep(.el-table__body-wrapper) {
  overflow-x: auto;
}

.responsive-table :deep(.el-table__header-wrapper) {
  overflow-x: hidden;
}

/* 确保表格在小屏幕上可以滚动 */
@media (max-width: 768px) {
  .responsive-table {
    display: block;
    width: 100%;
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
  }

  .responsive-table :deep(table) {
    min-width: 600px;
  }
}

/* 对话框响应式 */
:deep(.el-dialog) {
  max-width: 95vw;
}

@media (max-width: 768px) {
  :deep(.el-dialog) {
    width: 95% !important;
    margin: 2vh auto;
  }

  :deep(.el-dialog__body) {
    padding: 10px 15px;
  }

  :deep(.el-dialog__footer) {
    padding: 10px 15px;
  }
}
</style>