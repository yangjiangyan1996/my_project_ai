<template>
  <div class="warehouse-manage-container">
    <el-card class="manage-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">仓库管理</span>
          <div class="header-actions">
            <el-button 
              type="primary" 
              @click="handleCreate"
              v-permission="['warehouse:create']"
            >
              <el-icon><Plus /></el-icon>
              新增仓库
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
          <el-form-item label="仓库名称">
            <el-input
              v-model="filterForm.name"
              placeholder="请输入仓库名称"
              clearable
              style="width: 200px"
            />
          </el-form-item>
          <el-form-item label="仓库编码">
            <el-input
              v-model="filterForm.code"
              placeholder="请输入仓库编码"
              clearable
              style="width: 150px"
            />
          </el-form-item>
          <el-form-item label="状态">
            <el-select
              v-model="filterForm.status"
              placeholder="全部状态"
              clearable
              style="width: 120px"
            >
              <el-option label="启用" :value="1" />
              <el-option label="停用" :value="0" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 仓库列表 -->
      <div class="warehouse-list-section">
        <el-table
          :data="warehouseList"
          v-loading="loading"
          empty-text="暂无仓库数据"
          class="warehouse-table"
          row-key="id"
        >
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column label="仓库编码" width="120">
            <template #default="{ row }">
              <span class="warehouse-code">{{ row.code }}</span>
            </template>
          </el-table-column>
          <el-table-column label="仓库名称" min-width="150">
            <template #default="{ row }">
              <div class="warehouse-name">
                <span>{{ row.name }}</span>
                <el-tag 
                  v-if="row.isDefault" 
                  type="success" 
                  size="small" 
                  class="default-tag"
                >
                  默认
                </el-tag>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="仓库类型" width="120">
            <template #default="{ row }">
              <el-tag :type="getTypeTagType(row.type)" size="small">
                {{ getTypeText(row.type) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="地址" min-width="200">
            <template #default="{ row }">
              <span>{{ row.address || '--' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="负责人" width="120">
            <template #default="{ row }">
              <div class="manager-info">
                <el-avatar :size="24" :src="row.managerAvatar" class="manager-avatar" />
                <span class="manager-name">{{ row.managerName || '--' }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="联系电话" width="130">
            <template #default="{ row }">
              <span>{{ row.contactPhone || '--' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="库存数量" width="100" align="center">
            <template #default="{ row }">
              <el-button type="primary" link @click="handleViewInventory(row)">
                {{ row.productCount || 0 }}
              </el-button>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="80" align="center">
            <template #default="{ row }">
              <el-tag 
                :type="row.status === 1 ? 'success' : 'danger'" 
                size="small"
              >
                {{ row.status === 1 ? '启用' : '停用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" width="160">
            <template #default="{ row }">
              <span>{{ formatTime(row.createdAt) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right" align="center">
            <template #default="{ row }">
              <div class="action-buttons">
                <el-button
                  type="primary"
                  link
                  size="small"
                  @click="handleEdit(row)"
                  v-permission="['warehouse:update']"
                >
                  编辑
                </el-button>
                <el-button
                  type="success"
                  link
                  size="small"
                  @click="handleSetDefault(row)"
                  v-if="!row.isDefault"
                  v-permission="['warehouse:setDefault']"
                >
                  设默认
                </el-button>
                <el-button
                  :type="row.status === 1 ? 'warning' : 'success'"
                  link
                  size="small"
                  @click="handleToggleStatus(row)"
                  v-permission="['warehouse:update']"
                >
                  {{ row.status === 1 ? '停用' : '启用' }}
                </el-button>
                <el-button
                  type="danger"
                  link
                  size="small"
                  @click="handleDelete(row)"
                  v-permission="['warehouse:delete']"
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

    <!-- 新增/编辑仓库对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      :title="`${isEdit ? '编辑' : '新增'}仓库`"
      width="600px"
      top="5vh"
      :close-on-click-modal="false"
    >
      <WarehouseForm
        v-if="editDialogVisible"
        :form-data="currentWarehouse"
        :is-edit="isEdit"
        @success="handleFormSuccess"
        @cancel="editDialogVisible = false"
      />
    </el-dialog>

    <!-- 库存查看对话框 -->
    <el-dialog
      v-model="inventoryDialogVisible"
      :title="`库存查看 - ${currentWarehouse?.name}`"
      width="90%"
      top="5vh"
    >
      <WarehouseInventory
        v-if="inventoryDialogVisible && currentWarehouse"
        :warehouse-id="currentWarehouse.id"
        @close="inventoryDialogVisible = false"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Refresh } from '@element-plus/icons-vue';
import { post, get } from '@/net';
import WarehouseForm from '@/components/WarehouseForm.vue';
import WarehouseInventory from '@/components/WarehouseInventory.vue';


const loading = ref(false);
const editDialogVisible = ref(false);
const inventoryDialogVisible = ref(false);
const isEdit = ref(false);
const currentWarehouse = ref(null);

// 筛选表单
const filterForm = reactive({
  name: '',
  code: '',
  status: ''
});

// 分页信息
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
});

// 仓库列表
const warehouseList = ref([]);

// 仓库类型选项
const warehouseTypeOptions = [
  { value: 1, label: '普通仓库' },
  { value: 2, label: '冷链仓库' },
  { value: 3, label: '危险品仓库' },
  { value: 4, label: '保税仓库' },
  { value: 5, label: '立体仓库' }
];

// 方法
const loadWarehouseList = async () => {
  loading.value = true;
  try {
    const params = {
      page: pagination.current,
      size: pagination.size,
      ...filterForm
    };
    
    const res = await post('/api/auth/warehouse/list', params);
    if (res && res.records) {
      warehouseList.value = res.records.map(warehouse => ({
        id: warehouse.id || '',
        code: warehouse.code || '',
        name: warehouse.name || '',
        type: warehouse.type || 1,
        address: warehouse.address || '',
        managerId: warehouse.managerId || '',
        managerName: warehouse.managerName || '',
        managerAvatar: warehouse.managerAvatar || '/images/default-avatar.png',
        contactPhone: warehouse.contactPhone || '',
        area: warehouse.area || 0,
        capacity: warehouse.capacity || 0,
        productCount: warehouse.productCount || 0,
        isDefault: warehouse.isDefault || false,
        status: warehouse.status || 1,
        remark: warehouse.remark || '',
        createdAt: warehouse.createdAt || new Date().toISOString()
      }));
      pagination.total = res.total || 0;
    } else {
      warehouseList.value = [];
      pagination.total = 0;
    }
  } catch (error) {
    console.error('加载仓库列表失败:', error);
    ElMessage.error('加载仓库列表失败');
    warehouseList.value = [];
  } finally {
    loading.value = false;
  }
};

const refreshList = () => {
  pagination.current = 1;
  loadWarehouseList();
};

const handleSearch = () => {
  pagination.current = 1;
  loadWarehouseList();
};

const handleReset = () => {
  Object.assign(filterForm, {
    name: '',
    code: '',
    status: ''
  });
  pagination.current = 1;
  loadWarehouseList();
};

const handleSizeChange = (size) => {
  pagination.size = size;
  pagination.current = 1;
  loadWarehouseList();
};

const handleCurrentChange = (page) => {
  pagination.current = page;
  loadWarehouseList();
};

const handleCreate = () => {
  isEdit.value = false;
  currentWarehouse.value = {
    code: '',
    name: '',
    type: 1,
    address: '',
    managerId: '',
    contactPhone: '',
    area: 0,
    capacity: 0,
    status: 1,
    remark: ''
  };
  editDialogVisible.value = true;
};

const handleEdit = (warehouse) => {
  isEdit.value = true;
  currentWarehouse.value = { ...warehouse };
  editDialogVisible.value = true;
};

const handleSetDefault = async (warehouse) => {
  try {
    await ElMessageBox.confirm(
      `确定要将仓库"${warehouse.name}"设为默认仓库吗？`,
      '设置默认仓库',
      { type: 'warning' }
    );
    
    const res = await post('/api/auth/warehouse/setDefault', {
      id: warehouse.id
    });
    
    if (res) {
      ElMessage.success('设置默认仓库成功');
      loadWarehouseList();
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('设置默认仓库失败');
    }
  }
};

const handleToggleStatus = async (warehouse) => {
  const newStatus = warehouse.status === 1 ? 0 : 1;
  const actionText = newStatus === 1 ? '启用' : '停用';
  
  try {
    await ElMessageBox.confirm(
      `确定要${actionText}仓库"${warehouse.name}"吗？`,
      `${actionText}仓库`,
      { type: 'warning' }
    );
    
    const res = await post('/api/auth/warehouse/updateStatus', {
      id: warehouse.id,
      status: newStatus
    });
    
    if (res) {
      ElMessage.success(`${actionText}仓库成功`);
      loadWarehouseList();
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(`${actionText}仓库失败`);
    }
  }
};

const handleDelete = async (warehouse) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除仓库"${warehouse.name}"吗？此操作不可恢复！`,
      '删除仓库',
      { 
        type: 'error',
        confirmButtonText: '确定删除',
        cancelButtonText: '取消'
      }
    );
    
    const res = await post('/api/auth/warehouse/delete', {
      id: warehouse.id
    });
    
    if (res) {
      ElMessage.success('删除仓库成功');
      loadWarehouseList();
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除仓库失败');
    }
  }
};

const handleViewInventory = (warehouse) => {
  currentWarehouse.value = warehouse;
  inventoryDialogVisible.value = true;
};

const handleFormSuccess = () => {
  editDialogVisible.value = false;
  ElMessage.success(`${isEdit.value ? '编辑' : '新增'}仓库成功`);
  loadWarehouseList();
};

const getTypeText = (type) => {
  const typeObj = warehouseTypeOptions.find(item => item.value === type);
  return typeObj ? typeObj.label : '未知';
};

const getTypeTagType = (type) => {
  const types = {
    1: '',
    2: 'success',
    3: 'danger',
    4: 'warning',
    5: 'info'
  };
  return types[type] || '';
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
  loadWarehouseList();
});
</script>

<style scoped>
.warehouse-manage-container {
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

.warehouse-list-section {
  margin-top: 20px;
}

.warehouse-table {
  width: 100%;
}

.warehouse-code {
  font-family: 'Courier New', monospace;
  font-weight: bold;
  color: #409EFF;
}

.warehouse-name {
  display: flex;
  align-items: center;
  gap: 8px;
}

.default-tag {
  margin-left: 4px;
}

.manager-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.manager-avatar {
  flex-shrink: 0;
}

.manager-name {
  font-weight: 500;
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
  .warehouse-manage-container {
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
  
  .manager-info {
    flex-direction: column;
    gap: 4px;
    text-align: center;
  }
}

/* 动画效果 */
.warehouse-table :deep(.el-table__row) {
  transition: all 0.3s;
}

.warehouse-table :deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}
</style>