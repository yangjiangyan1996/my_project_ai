<template>
  <div class="stock-take-manage-container">
    <el-card class="manage-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">盘点数据录入</span>
          <div class="header-actions">
            <el-button 
              @click="refreshData"
              :loading="loading"
            >
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
            <el-button 
              type="warning" 
              @click="showAssignDialog"
              :disabled="stockTakeInfo.takeStatusName !== '盘点中'"
            >
              <el-icon><User /></el-icon>
              分配任务
            </el-button>
            <el-button 
              type="primary" 
              @click="handleCompleteStock"
              :loading="completing"
            >
              <el-icon><Check /></el-icon>
              完成盘点
            </el-button>
          </div>
        </div>
      </template>

      <!-- 盘点单信息 -->
      <div class="stock-take-info-section">
        <el-card shadow="never" class="info-card">
            <div class="info-grid">
            <div class="info-item">
                <span class="info-label">盘点单号：</span>
                <span class="info-value">{{ stockTakeInfo.stockTakeNo }}</span>
            </div>
            <div class="info-item">
                <span class="info-label">仓库：</span>
                <span class="info-value">{{ stockTakeInfo.warehouseName }}</span>
            </div>
            <div class="info-item">
                <span class="info-label">盘点类型：</span>
                <el-tag type="primary" size="small">
                {{ stockTakeInfo.takeTypeName }}
                </el-tag>
            </div>
            <div class="info-item">
                <span class="info-label">盘点状态：</span>
                <el-tag 
                :type="getStatusTagTypeByName(stockTakeInfo.takeStatusName)" 
                size="small"
                >
                {{ stockTakeInfo.takeStatusName }}
                </el-tag>
            </div>
            <div class="info-item">
                <span class="info-label">审核状态：</span>
                <el-tag 
                :type="getApprovalStatusTagType(stockTakeInfo.approvalStatusName)" 
                size="small"
                >
                {{ stockTakeInfo.approvalStatusName }}
                </el-tag>
            </div>
            <div class="info-item full-width">
                <span class="info-label">备注：</span>
                <span class="info-value">{{ stockTakeInfo.remark || '无' }}</span>
            </div>
            </div>
        </el-card>
        </div>

      <!-- 分配任务区域 -->
      <div class="assignment-section" v-if="assignments.length > 0">
        <el-card shadow="never" class="assignment-card">
          <template #header>
            <div class="assignment-header">
              <span class="assignment-title">
                <el-icon><User /></el-icon>
                已分配任务
              </span>
              <el-button 
                type="text" 
                size="small"
                @click="toggleAssignmentList"
              >
                {{ showAssignmentList ? '收起' : '展开' }}
              </el-button>
            </div>
          </template>
          
          <div v-show="showAssignmentList">
            <el-table
              :data="assignments"
              size="small"
              border
              class="assignment-table"
              empty-text="暂无分配信息"
            >
              <el-table-column label="分配方式" width="120">
                <template #default="{ row }">
                  <el-tag size="small" :type="row.assignType === 1 ? 'primary' : 'success'">
                    {{ row.assignType === 1 ? '按人分配' : '按条件分配' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="负责人" width="120">
                <template #default="{ row }">
                  {{ row.assigneeName || '--' }}
                </template>
              </el-table-column>
              <el-table-column label="优先级" width="100">
                <template #default="{ row }">
                  <el-tag 
                    size="small"
                    :type="getPriorityTagType(row.priority)"
                  >
                    {{ getPriorityLabel(row.priority) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="计划时间" width="240">
                <template #default="{ row }">
                  <div v-if="row.planStartTime || row.planEndTime">
                    <div>开始: {{ formatDateTime(row.planStartTime) }}</div>
                    <div>结束: {{ formatDateTime(row.planEndTime) }}</div>
                  </div>
                  <span v-else>--</span>
                </template>
              </el-table-column>
              <el-table-column label="分配条件">
                <template #default="{ row }">
                  <div v-if="row.assignType === 2">
                    <el-tag 
                      v-for="(condition, index) in row.conditions" 
                      :key="index"
                      size="small"
                      class="condition-tag"
                    >
                      {{ getConditionLabel(condition) }}
                    </el-tag>
                  </div>
                  <span v-else>--</span>
                </template>
              </el-table-column>
              <el-table-column label="分配时间" width="180">
                <template #default="{ row }">
                  {{ formatDateTime(row.createTime) }}
                </template>
              </el-table-column>
              <el-table-column label="状态" width="100">
                <template #default="{ row }">
                  <el-tag 
                    :type="row.status === 1 ? 'success' : row.status === 2 ? 'warning' : 'info'"
                    size="small"
                  >
                    {{ row.status === 1 ? '进行中' : row.status === 2 ? '已完成' : '未开始' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="进度" width="150">
                <template #default="{ row }">
                  <div class="progress-cell">
                    <el-progress 
                      :percentage="row.progressPercentage || 0" 
                      :show-text="false"
                      :stroke-width="8"
                      :color="row.progressPercentage === 100 ? '#67C23A' : '#409EFF'"
                    />
                    <span class="progress-text">
                      {{ row.completedCount || 0 }}/{{ row.totalCount || 0 }}
                    </span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="120" fixed="right">
                <template #default="{ row }">
                  <el-button 
                    type="text" 
                    size="small"
                    @click="viewAssignmentDetails(row)"
                  >
                    查看详情
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-card>
      </div>

      <!-- 统计信息 -->
      <div class="stats-section">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-card shadow="never" class="stat-card">
              <div class="stat-content">
                <div class="stat-number">{{ totalItems }}</div>
                <div class="stat-label">总盘点项</div>
              </div>
              <el-icon class="stat-icon" color="#409EFF"><Document /></el-icon>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="never" class="stat-card">
              <div class="stat-content">
                <div class="stat-number">{{ countedItems }}</div>
                <div class="stat-label">已盘点</div>
              </div>
              <el-icon class="stat-icon" color="#67C23A"><Check /></el-icon>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="never" class="stat-card">
              <div class="stat-content">
                <div class="stat-number">{{ pendingItems }}</div>
                <div class="stat-label">未盘点</div>
              </div>
              <el-icon class="stat-icon" color="#E6A23C"><Clock /></el-icon>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="never" class="stat-card">
              <div class="stat-content">
                <div class="stat-number" :class="getDiffClass(totalDiff)">
                  {{ formatNumber(totalDiff) }}
                </div>
                <div class="stat-label">总差异数量</div>
              </div>
              <el-icon class="stat-icon" color="#909399"><TrendCharts /></el-icon>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 盘点明细表格 -->
      <div class="stock-take-list-section">
        <!-- 搜索和筛选 -->
        <div class="filter-section">
          <el-form :model="filterForm" inline>
            <el-form-item label="SKU">
              <el-input
                v-model="filterForm.sku"
                placeholder="请输入SKU"
                clearable
                style="width: 180px"
                @keyup.enter="handleSearch"
              />
            </el-form-item>
            <el-form-item label="产品名称">
              <el-input
                v-model="filterForm.productName"
                placeholder="请输入产品名称"
                clearable
                style="width: 180px"
                @keyup.enter="handleSearch"
              />
            </el-form-item>
            <el-form-item label="批次号">
              <el-input
                v-model="filterForm.batchNo"
                placeholder="请输入批次号"
                clearable
                style="width: 180px"
                @keyup.enter="handleSearch"
              />
            </el-form-item>
            <el-form-item label="盘点状态">
              <el-select
                v-model="filterForm.status"
                placeholder="全部状态"
                clearable
                style="width: 120px"
              >
                <el-option label="未盘" :value="1" />
                <el-option label="已盘" :value="2" />
                <el-option label="已确认" :value="3" />
              </el-select>
            </el-form-item>
            <el-form-item label="分配状态">
              <el-select
                v-model="filterForm.assignStatus"
                placeholder="全部"
                clearable
                style="width: 120px"
              >
                <el-option label="已分配" :value="1" />
                <el-option label="未分配" :value="0" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch">查询</el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <el-table
          :data="stockTakeItems"
          v-loading="loading"
          empty-text="暂无盘点明细数据"
          class="stock-take-table"
          row-key="stockTakeItemId"
          border
          @selection-change="handleSelectionChange"
          :row-class-name="tableRowClassName"
        >
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column label="SKU" width="140">
            <template #default="{ row }">
              <span class="sku-text">{{ row.productSku || '--' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="产品名称" width="200">
            <template #default="{ row }">
              <div class="product-info">
                <div class="product-name">{{ row.productName || '--' }}</div>
                <div v-if="row.spec" class="product-spec">{{ row.spec }}</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="颜色" width="80" align="center">
            <template #default="{ row }">
              <span>{{ row.color || '--' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="货架" width="120" align="center">
            <template #default="{ row }">
              <span>{{ row.shelfName || '--' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="批次号" width="120" align="center">
            <template #default="{ row }">
              <span>{{ row.batchNo || '--' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="货架/库位" width="150" align="center">
            <template #default="{ row }">
              <div v-if="row.shelfCode || row.locationCode">
                <div>{{ row.shelfCode || '--' }}</div>
                <div class="location-code">{{ row.locationCode || '--' }}</div>
              </div>
              <span v-else>--</span>
            </template>
          </el-table-column>
          <el-table-column label="系统库存" width="120" align="right">
            <template #default="{ row }">
              <span class="system-quantity">{{ formatNumber(row.systemQuantity) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="实盘数量" width="150" align="center">
            <template #default="{ row }">
              <el-input-number
                v-model="row.countedQuantity"
                :min="0"
                :precision="row.precision || 0"
                :controls="false"
                size="small"
                style="width: 120px"
                placeholder="请输入"
                @change="handleQuantityChange(row)"
                :disabled="row.status === 3 || !canEditRow(row) || !row.hasStockItemTaskPermission"
              />
            </template>
          </el-table-column>
          <el-table-column label="差异数量" width="120" align="right">
            <template #default="{ row }">
              <span :class="getDiffClass(row.diffQuantity)">
                {{ formatNumber(row.diffQuantity) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="分配状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag
                v-if="row.assigneeName"
                type="primary"
                size="small"
              >
                {{ row.assigneeName }}
              </el-tag>
              <el-tag
                v-else
                type="info"
                size="small"
              >
                未分配
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag
                :type="getItemStatusTagType(row.status)"
                size="small"
              >
                {{ getItemStatusLabel(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" align="center" fixed="right">
            <template #default="{ row }">
              <div class="action-buttons">
                <el-button
                  type="primary"
                  size="small"
                  @click="handleSaveItem(row)"
                  :loading="row.saving"
                  :disabled="!row.countedQuantity || row.countedQuantity < 0 || row.status === 3 || !canEditRow(row) || !row.hasStockItemTaskPermission"
                >
                  保存
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

    <!-- 批量操作区域 -->
    <div v-if="selectedItems.length > 0" class="batch-actions-footer">
      <el-card shadow="never" class="batch-actions-card">
        <div class="batch-actions-content">
          <div class="selected-info">
            已选择 <span class="selected-count">{{ selectedItems.length }}</span> 项
            <span v-if="selectedItemsWithoutPermission > 0" class="no-permission-warning">
              (其中 {{ selectedItemsWithoutPermission }} 项无操作权限)
            </span>
          </div>
          <div class="batch-buttons">
            <el-button type="primary" size="small" @click="handleBatchSave"
            :disabled="selectedItemsWithoutPermission === selectedItems.length"
            >
              批量保存
            </el-button>
            <el-button type="success" size="small" @click="handleBatchConfirm"
            :disabled="selectedItemsWithoutPermission === selectedItems.length"
            >
              批量确认
            </el-button>
            <el-button 
              type="warning" 
              size="small" 
              @click="showAssignSelectedDialog"
              :disabled="!canAssignSelected || selectedItemsWithoutPermission === selectedItems.length"
            >
              分配选中项
            </el-button>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 分配任务对话框 -->
    <el-dialog
      v-model="assignDialogVisible"
      :title="assignDialogTitle"
      width="900px"
      :close-on-click-modal="false"
      @closed="handleAssignDialogClosed"
    >
      <el-form :model="assignForm" ref="assignFormRef" label-width="120px">
        <el-form-item label="分配方式" prop="assignType" required>
          <el-radio-group v-model="assignForm.assignType" @change="handleAssignTypeChange">
            <el-radio :label="1">分配给一个人</el-radio>
            <el-radio :label="2">分配给多个人</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 新增：优先级和计划时间 -->
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="优先级" prop="priority" required>
              <el-select
                v-model="assignForm.priority"
                placeholder="请选择优先级"
                style="width: 100%"
              >
                <el-option label="紧急" :value="1" />
                <el-option label="高" :value="2" />
                <el-option label="中" :value="3" />
                <el-option label="低" :value="4" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="计划开始时间" prop="planStartTime" required>
              <el-date-picker
                v-model="assignForm.planStartTime"
                type="datetime"
                placeholder="选择计划开始时间"
                style="width: 100%"
                format="YYYY-MM-DD HH:mm"
                value-format="YYYY-MM-DD HH:mm:ss"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="计划结束时间" prop="planEndTime" required>
          <el-date-picker
            v-model="assignForm.planEndTime"
            type="datetime"
            placeholder="选择计划结束时间"
            style="width: 100%"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm:ss"
            :disabled-date="disabledEndDate"
          />
        </el-form-item>

        <!-- 单人选人 -->
        <el-form-item 
          v-if="assignForm.assignType === 1" 
          label="选择人员" 
          prop="singleAssignee"
          :rules="[{ required: true, message: '请选择至少一个人员', trigger: 'change' }]"
        >
          <el-select
            v-model="assignForm.singleAssignee"
            placeholder="请选择负责人（可多选）"
            filterable
            multiple
            collapse-tags
            collapse-tags-tooltip
            style="width: 100%"
          >
            <el-option
              v-for="user in userList"
              :key="user.id"
              :label="user.name || user.username"
              :value="user.id"
            >
              <div class="user-option">
                <span>{{ user.name || user.username }}</span>
                <span class="user-department">{{ user.department || '--' }}</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>

        <!-- 多人分配 -->
        <div v-if="assignForm.assignType === 2">
          <el-form-item label="分配维度" required>
            <el-radio-group v-model="assignForm.assignDimension">
              <el-radio label="batchNo">按批次分配</el-radio>
              <el-radio label="shelfCode">按货架分配</el-radio>
              <el-radio label="product">按商品分配</el-radio>
            </el-radio-group>
          </el-form-item>

          <div class="multiple-assignment-section">
            <!-- 按批次分配 -->
            <div v-if="assignForm.assignDimension === 'batchNo'" class="dimension-section">
              <h4>按批次分配</h4>
              <el-table
                :data="batchList"
                size="small"
                border
                class="dimension-table"
                empty-text="暂无批次数据"
              >
                <el-table-column label="批次号" prop="batchNo" width="150" />
                <el-table-column label="商品数量" prop="itemCount" width="100" align="center">
                  <template #default="{ row }">
                    {{ formatNumber(row.quantity) }}
                  </template>
                </el-table-column>
                
                <el-table-column label="选择负责人" width="250">
                  <template #default="{ row }">
                    <el-select
                      v-model="row.assigneeIds"
                      placeholder="请选择（可多选）"
                      multiple
                      collapse-tags
                      collapse-tags-tooltip
                      size="small"
                      style="width: 230px"
                    >
                      <el-option
                        v-for="user in userList"
                        :key="user.id"
                        :label="user.name || user.username"
                        :value="user.id"
                      />
                    </el-select>
                  </template>
                </el-table-column>

              </el-table>
            </div>

            <!-- 按货架分配 -->
            <div v-if="assignForm.assignDimension === 'shelfCode'" class="dimension-section">
              <h4>按货架分配</h4>
              <el-table
                :data="shelfList"
                size="small"
                border
                class="dimension-table"
                empty-text="暂无货架数据"
              >
                <el-table-column label="货架号" prop="shelfCode" width="150" />
                <el-table-column label="商品数量" prop="itemCount" width="100" align="center">
                  <template #default="{ row }">
                    {{ formatNumber(row.quantity) }}
                  </template>
                </el-table-column>
                
                
              <el-table-column label="选择负责人" width="250">
                <template #default="{ row }">
                  <el-select
                    v-model="row.assigneeIds"
                    placeholder="请选择（可多选）"
                    multiple
                    collapse-tags
                    collapse-tags-tooltip
                    size="small"
                    style="width: 230px"
                  >
                    <el-option
                      v-for="user in userList"
                      :key="user.id"
                      :label="user.name || user.username"
                      :value="user.id"
                    />
                  </el-select>
                </template>
              </el-table-column>
              </el-table>
            </div>

            <!-- 按商品分配 -->
            <div v-if="assignForm.assignDimension === 'product'" class="dimension-section">
              <h4>按商品分配</h4>
              <el-table
                :data="productList"
                size="small"
                border
                class="dimension-table"
                empty-text="暂无商品数据"
              >
                <el-table-column label="商品名称" prop="productName" width="200" />
                <el-table-column label="商品数量" prop="itemCount" width="100" align="center">
                  <template #default="{ row }">
                    {{ formatNumber(row.quantity) }}
                  </template>
                </el-table-column>
                
                <el-table-column label="选择负责人" width="250">
                  <template #default="{ row }">
                    <el-select
                      v-model="row.assigneeIds"
                      placeholder="请选择（可多选）"
                      multiple
                      collapse-tags
                      collapse-tags-tooltip
                      size="small"
                      style="width: 230px"
                    >
                      <el-option
                        v-for="user in userList"
                        :key="user.id"
                        :label="user.name || user.username"
                        :value="user.id"
                      />
                    </el-select>
                  </template>
                </el-table-column>
              </el-table>
            </div>

            <!-- 快速分配 -->
            <div class="quick-assign-section">
              <h4>快速分配</h4>
              <el-form :inline="true" class="quick-assign-form">
                
                <el-form-item label="批量选择负责人">
                  <el-select
                    v-model="quickAssign.assigneeIds"
                    placeholder="选择负责人（可多选）"
                    multiple
                    collapse-tags
                    collapse-tags-tooltip
                    size="small"
                    style="width: 230px"
                  >
                    <el-option
                      v-for="user in userList"
                      :key="user.id"
                      :label="user.name"
                      :value="user.id"
                    />
                  </el-select>
                </el-form-item>
                <el-form-item>
                  <el-button
                    type="primary"
                    size="small"
                    @click="quickAssignAll"
                    :disabled="!quickAssign.assigneeIds || quickAssign.assigneeIds.length === 0"
                  >
                    批量分配全部未分配项
                  </el-button>
                </el-form-item>
              </el-form>
            </div>
          </div>
        </div>

        <el-form-item label="分配说明" prop="remark">
          <el-input
            v-model="assignForm.remark"
            type="textarea"
            placeholder="请输入分配说明（可选）"
            :rows="3"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="assignDialogVisible = false">取消</el-button>
          <el-button 
            type="primary" 
            @click="handleAssignSubmit"
            :loading="assigning"
          >
            确认分配
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 分配详情对话框 -->
    <el-dialog
      v-model="assignmentDetailVisible"
      title="分配详情"
      width="700px"
    >
      <div v-if="currentAssignment" class="assignment-detail">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="分配方式">
            <el-tag :type="currentAssignment.assignType === 1 ? 'primary' : 'success'">
              {{ currentAssignment.assignType === 1 ? '按人分配' : '按条件分配' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="负责人">
            {{ currentAssignment.assigneeName }}
          </el-descriptions-item>
          <el-descriptions-item label="优先级">
            <el-tag :type="getPriorityTagType(currentAssignment.priority)">
              {{ getPriorityLabel(currentAssignment.priority) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="计划开始时间">
            {{ formatDateTime(currentAssignment.planStartTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="计划结束时间">
            {{ formatDateTime(currentAssignment.planEndTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="分配时间">
            {{ formatDateTime(currentAssignment.createTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="分配状态">
            <el-tag 
              :type="currentAssignment.status === 1 ? 'success' : currentAssignment.status === 2 ? 'warning' : 'info'"
            >
              {{ currentAssignment.status === 1 ? '进行中' : currentAssignment.status === 2 ? '已完成' : '未开始' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="分配说明" :span="2">
            {{ currentAssignment.remark || '无' }}
          </el-descriptions-item>
          <el-descriptions-item label="分配项数" :span="2">
            <el-progress 
              :percentage="currentAssignment.progressPercentage || 0" 
              :text-inside="true"
              :stroke-width="20"
              :color="currentAssignment.progressPercentage === 100 ? '#67C23A' : '#409EFF'"
            />
            <div class="progress-info">
              已完成 {{ currentAssignment.completedCount || 0 }} / 总计 {{ currentAssignment.totalCount || 0 }}
            </div>
          </el-descriptions-item>
        </el-descriptions>

        <!-- 分配条件详情 -->
        <div v-if="currentAssignment.conditions && currentAssignment.conditions.length > 0" class="condition-detail">
          <h4>分配条件：</h4>
          <div class="condition-tags">
            <el-tag 
              v-for="(condition, index) in currentAssignment.conditions" 
              :key="index"
              type="info"
              size="small"
              class="condition-tag"
            >
              {{ getConditionLabel(condition) }}
            </el-tag>
          </div>
        </div>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="assignmentDetailVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { useRouter, useRoute } from 'vue-router';
import { ref, reactive, computed, onMounted, watch, nextTick } from 'vue';
import { ElMessage, ElMessageBox, ElForm } from 'element-plus';
import { 
  Refresh, 
  Check, 
  Document, 
  Clock,
  TrendCharts,
  User
} from '@element-plus/icons-vue';
import { post, get } from '@/net';

const router = useRouter();
const route = useRoute();

// 加载状态
const loading = ref(false);
const completing = ref(false);
const assigning = ref(false);

// 分页信息
const pagination = reactive({
  current: 1,
  size: 20,
  total: 0
});

// 筛选表单
const filterForm = reactive({
  sku: '',
  productName: '',
  batchNo: '',
  status: '',
  assignStatus: ''
});

// 盘点单信息
const stockTakeInfo = reactive({
  id: '',
  stockTakeNo: '',
  warehouseName: '',
  takeType: 1,
  status: 1,
  remark: ''
});

// 盘点明细列表
const stockTakeItems = ref([]);
// 选中项
const selectedItems = ref([]);

// 分配相关数据
const assignments = ref([]);
const showAssignmentList = ref(true);
const assignDialogVisible = ref(false);
const assignmentDetailVisible = ref(false);
const assignFormRef = ref();
const currentAssignment = ref(null);

// 分配表单
const assignForm = reactive({
  assignType: 1, // 1: 单人分配, 2: 多人分配
  singleAssignee: [], // 改为数组，支持多选
  assignDimension: 'batchNo', // 分配维度（改为单选，值对应：batchNo=1, shelfCode=2, product=3）
  priority: 3, // 默认优先级为"中"
  planStartTime: null, // 计划开始时间
  planEndTime: null, // 计划结束时间
  remark: ''
});


// 获取分配维度对应的数字值
const getDimensionValue = (dimension) => {
  const mapping = {
    'batchNo': 1,   // 批次 = 1
    'shelfCode': 2, // 货架 = 2
    'product': 3    // 商品 = 3
  };
  return mapping[dimension] || 1;
};

// 快速分配
const quickAssign = reactive({
  assigneeIds: []
});

// 用户列表
const userList = ref([]);

// 维度数据列表
const batchList = ref([]);
const shelfList = ref([]);
const productList = ref([]);

// 计算属性
const totalItems = computed(() => {
  return pagination.total;
});

const countedItems = computed(() => {
  return stockTakeItems.value.filter(item => item.status === 2 || item.status === 3).length;
});

const pendingItems = computed(() => {
  return stockTakeItems.value.filter(item => item.status === 1).length;
});

const totalDiff = computed(() => {
  return stockTakeItems.value.reduce((sum, item) => sum + (item.diffQuantity || 0), 0);
});

const canComplete = computed(() => {
  return stockTakeInfo.status === 3 && pendingItems.value === 0;
});

const assignDialogTitle = computed(() => {
  if (selectedItems.value.length > 0) {
    return `分配任务（已选择 ${selectedItems.value.length} 项）`;
  }
  return '分配任务';
});

const canAssignSelected = computed(() => {
  return selectedItems.value.length > 0 && stockTakeInfo.takeStatusName === '盘点中';
});

const selectedItemsWithoutPermission = computed(() => {
  return selectedItems.value.filter(item => !item.hasStockItemTaskPermission).length;
});

// 从URL获取盘点单ID
const getStockTakeIdFromUrl = () => {
  var id = route.params.id;
  console.log('路由参数:', id);
  return id;
};

// 保存盘点单ID到URL和本地存储
const saveStockTakeId = (id) => {
  const url = new URL(window.location);
  url.searchParams.set('id', id);
  window.history.replaceState({}, '', url);
  localStorage.setItem('currentStockTakeId', id);
};

// 加载盘点单信息
const loadStockTakeInfo = async () => {
  const stockTakeId = getStockTakeIdFromUrl();
  if (!stockTakeId) {
    ElMessage.error('未找到盘点单ID');
    return;
  }

  try {
    const res = await get('/api/auth/stock/stockDetail?id=' + route.params.id);
    if (res) {
      Object.assign(stockTakeInfo, {
        id: res.id || '',
        stockTakeNo: res.stockTakeNo || '',
        warehouseName: res.warehouseName || '',
        takeTypeName: res.takeTypeName || '',
        approvalStatusName: res.approvalStatusName || '',
        takeStatusName: res.takeStatusName || '',
        remark: res.remark || ''
      });
    }
  } catch (error) {
    console.error('加载盘点单信息失败:', error);
    ElMessage.error('加载盘点单信息失败');
  }
};

// 加载盘点明细
const loadStockTakeItems = async () => {
  const stockTakeId = getStockTakeIdFromUrl();
  if (!stockTakeId) {
    ElMessage.error('未找到盘点单ID');
    return;
  }

  loading.value = true;
  try {
    const params = {
      stockTakeId,
      page: pagination.current,
      size: pagination.size,
      ...filterForm
    };

    const res = await post('/api/auth/stock/itemPageList', params);
    if (res && res.records) {
      stockTakeItems.value = res.records.map(item => ({
        stockTakeItemId: item.stockTakeItemId || '',
        stockTakeId: item.stockTakeId || '',
        productId: item.productId || '',
        productSku: item.sku || '', // 使用新的sku字段
        productName: item.productName || '',
        spec: item.spec || '',
        color: item.color || '', // 新增颜色字段
        batchNo: item.batchNo || '',
        shelfId: item.shelfId || '', // 新增货架ID
        shelfName: item.shelfName || '', // 新增货架名称
        locationCode: item.locationCode || '',
        systemQuantity: item.systemQuantity || 0,
        countedQuantity: item.countedQuantity !== undefined ? item.countedQuantity : null,
        diffQuantity: item.diffQuantity || 0,
        status: item.status || 1,
        precision: item.precision || 0,
        saving: false,
        confirming: false,
        assigneeId: item.assigneeId || null,
        assigneeName: item.assigneeName || null,
        assignmentId: item.assignmentId || null,
        hasStockItemTaskPermission: item.hasStockItemTaskPermission || false // 新增权限字段
      }));
      pagination.total = res.total || 0;
    } else {
      stockTakeItems.value = [];
      pagination.total = 0;
    }
  } catch (error) {
    console.error('加载盘点明细失败:', error);
    ElMessage.error('加载盘点明细失败');
    stockTakeItems.value = [];
  } finally {
    loading.value = false;
  }
};

// 加载分配任务
const loadAssignments = async () => {
  const stockTakeId = getStockTakeIdFromUrl();
  if (!stockTakeId) return;

  try {
    const res = await get(`/api/auth/stock/assignment/list?stockTakeId=${stockTakeId}`);
    if (res) {
      assignments.value = res;
    }
  } catch (error) {
    console.error('加载分配任务失败:', error);
  }
};

// 加载用户列表
const loadUserList = async () => {
  try {
    const res = await post('/api/auth/user/searchUser');
    if (res) {
      userList.value = res.map(user => ({
        id: user.id,
        name: user.username || user.name, // 适配不同字段名
        username: user.username,
        department: user.department,
        avatarUrl: user.avatarUrl || '/default-avatar.png'
      }));
    }
  } catch (error) {
    console.error('加载用户列表失败:', error);
    ElMessage.error('加载用户列表失败');
  }
};

// 加载维度数据
const loadDimensionData = async () => {
  const stockTakeId = getStockTakeIdFromUrl();
  if (!stockTakeId) return;

  try {
    // 获取批次列表 (type = 2)
    const batchRes = await get('/api/auth/stock/getListOfStockTakeToBeAssigned?stockTakeId=' + stockTakeId +"&type=2");
    if (batchRes) {
      batchList.value = batchRes.map(item => ({
        batchNo: item.id,
        name: item.name,
        quantity: item.quantity,
        itemCount: item.quantity || 0, // 使用quantity作为商品数量
        assigneeIds: []
      }));
    }

    // 获取货架列表 (type = 3)
    const shelfRes = await get('/api/auth/stock/getListOfStockTakeToBeAssigned?stockTakeId=' + stockTakeId +"&type=3");
    if (shelfRes) {
      shelfList.value = shelfRes.map(item => ({
        shelfCode: item.id,
        name: item.name,
        quantity: item.quantity,
        itemCount: item.quantity || 0, // 使用quantity作为商品数量
        assigneeIds: []
      }));
    }

    // 获取商品列表 (type = 4)
    const productRes = await get('/api/auth/stock/getListOfStockTakeToBeAssigned?stockTakeId=' + stockTakeId +"&type=4");
    if (productRes) {
      productList.value = productRes.map(item => ({
        productId: item.id,
        productName: item.name,
        productSku: item.id, // 假设id就是SKU，根据实际调整
        quantity: item.quantity,
        itemCount: item.quantity || 0, // 使用quantity作为商品数量
        assigneeIds: []
      }));
    }
  } catch (error) {
    console.error('加载维度数据失败:', error);
    ElMessage.error('加载维度数据失败');
  }
};

// 刷新数据
const refreshData = () => {
  loadStockTakeInfo();
  loadStockTakeItems();
  loadAssignments();
};

// 搜索
const handleSearch = () => {
  pagination.current = 1;
  loadStockTakeItems();
};

// 重置搜索
const handleReset = () => {
  Object.assign(filterForm, {
    sku: '',
    productName: '',
    batchNo: '',
    status: '',
    assignStatus: ''
  });
  pagination.current = 1;
  loadStockTakeItems();
};

// 分页大小变化
const handleSizeChange = (size) => {
  pagination.size = size;
  pagination.current = 1;
  loadStockTakeItems();
};

// 页码变化
const handleCurrentChange = (page) => {
  pagination.current = page;
  loadStockTakeItems();
};

// 数量变化处理
const handleQuantityChange = (row) => {
  if (row.countedQuantity !== null && row.countedQuantity !== undefined) {
    row.diffQuantity = row.countedQuantity - row.systemQuantity;
  } else {
    row.diffQuantity = -row.systemQuantity;
  }
};

// 保存单条数据
const handleSaveItem = async (row) => {
  if (row.countedQuantity === null || row.countedQuantity === undefined) {
    ElMessage.warning('请输入实盘数量');
    return;
  }

  if (row.countedQuantity < 0) {
    ElMessage.warning('实盘数量不能为负数');
    return;
  }

  row.saving = true;
  try {
    const res = await post('/api/auth/stock/executeStockTakeItem', {
      stockTakeItemId: row.stockTakeItemId,
      stockTakeId: row.stockTakeId,
      productId: row.productId,
      countedQuantity: row.countedQuantity,
    });
    
    if (res) {
      ElMessage.success('保存成功');
      row.status = 2;
      await loadStockTakeItems();
      await loadAssignments(); // 刷新分配进度
    }
  } catch (error) {
    console.error('保存失败:', error);
    ElMessage.error('保存失败');
  } finally {
    row.saving = false;
  }
};

// 批量保存
const handleBatchSave = async () => {
  // 过滤有权限的项
  const itemsToSave = stockTakeItems.value.filter(item => 
    selectedItems.value.includes(item) &&
    item.hasStockItemTaskPermission &&
    item.countedQuantity !== null && 
    item.countedQuantity !== undefined && 
    item.status === 1
  );

  if (itemsToSave.length === 0) {
    ElMessage.warning('没有需要保存的项或无操作权限');
    return;
  }

  try {
    await ElMessageBox.confirm(
      `确定要批量保存 ${itemsToSave.length} 项数据吗？`,
      '批量保存确认',
      { type: 'warning' }
    );

    const res = await post('/api/auth/stock/batchSaveStockItem', {
      items: itemsToSave.map(item => ({
        id: item.stockTakeItemId,
        countedQuantity: item.countedQuantity,
        diffQuantity: item.diffQuantity,
        status: 2
      }))
    });

    if (res) {
      ElMessage.success(`成功保存 ${itemsToSave.length} 项数据`);
      await loadStockTakeItems();
      await loadAssignments(); // 刷新分配进度
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量保存失败');
    }
  }
};

// 批量确认
const handleBatchConfirm = async () => {
  // 过滤有权限的项
  const itemsToConfirm = stockTakeItems.value.filter(item => 
    selectedItems.value.includes(item) &&
    item.hasStockItemTaskPermission &&
    item.status === 2
  );

  if (itemsToConfirm.length === 0) {
    ElMessage.warning('没有需要确认的项或无操作权限');
    return;
  }

  try {
    await ElMessageBox.confirm(
      `确定要批量确认 ${itemsToConfirm.length} 项数据吗？确认后将不可修改。`,
      '批量确认确认',
      { type: 'warning' }
    );

    const res = await post('/api/auth/stock/batchConfirmItem', {
      items: itemsToConfirm.map(item => ({
        id: item.stockTakeItemId,
        status: 3
      }))
    });

    if (res) {
      ElMessage.success(`成功确认 ${itemsToConfirm.length} 项数据`);
      await loadStockTakeItems();
      await loadAssignments(); // 刷新分配进度
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量确认失败');
    }
  }
};

// 完成盘点
const handleCompleteStock = async () => {
  const stockTakeId = getStockTakeIdFromUrl();
  if (!stockTakeId) {
    ElMessage.error('未找到盘点单ID');
    return;
  }

  try {
    await ElMessageBox.confirm(
      '确定要完成盘点吗？完成盘点后将提交复核。',
      '完成盘点确认',
      { type: 'warning' }
    );
    completing.value = true;
    const res = await get('/api/auth/stock/completedStock?id=' + route.params.id);

    if (res) {
      ElMessage.success('完成盘点成功');
      stockTakeInfo.status = 4;
      await loadStockTakeItems();
      await loadAssignments();
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('完成盘点失败:', error);
      ElMessage.error('完成盘点失败');
    }
  } finally {
    completing.value = false;
  }
};

// 表格选择变化
const handleSelectionChange = (selection) => {
  selectedItems.value = selection;
};

// 显示分配对话框
const showAssignDialog = async () => {
  if (stockTakeInfo.takeStatusName !== '盘点中') {
    ElMessage.warning('只有在盘点中的盘点单才能分配任务');
    return;
  }

  // 加载必要数据
  await loadUserList();
  await loadDimensionData();
  
  assignDialogVisible.value = true;
};

// 显示选中项分配对话框
const showAssignSelectedDialog = async () => {
  if (selectedItems.value.length === 0) {
    ElMessage.warning('请先选择要分配的项');
    return;
  }

  await loadUserList();
  assignDialogVisible.value = true;
};

// 处理分配方式变化
const handleAssignTypeChange = (type) => {
  // 重置相关数据
  if (type === 1) {
    assignForm.singleAssignee = [];
    assignForm.assignDimension = 'batchNo';
  } else {
    // 重置维度分配数据
    batchList.value.forEach(item => item.assigneeIds = []);
    shelfList.value.forEach(item => item.assigneeIds = []);
    productList.value.forEach(item => item.assigneeIds = []);
  }
};

// 快速分配全部未分配项
const quickAssignAll = async () => {
  if (!quickAssign.assigneeIds || quickAssign.assigneeIds.length === 0) {
    ElMessage.warning('请先选择负责人');
    return;
  }

  try {
    const stockTakeId = getStockTakeIdFromUrl();
    
    // 找出所有未分配的盘点项
    const unassignedItems = stockTakeItems.value.filter(item => !item.assigneeId);
    
    if (unassignedItems.length === 0) {
      ElMessage.warning('当前页面没有未分配的盘点项');
      return;
    }
    
    // 获取这些盘点项的ID
    const itemIds = unassignedItems.map(item => item.stockTakeItemId);
    
    // 调用API进行快速分配
    const res = await post('/api/auth/stock/quickAssignTask', {
      stockTakeId,
      assigneeIds: quickAssign.assigneeIds, // 使用数组
      itemIds,
      remark: '快速分配'
    });
    
    if (res) {
      ElMessage.success(`已为 ${itemIds.length} 个未分配项分配负责人`);
      assignDialogVisible.value = false;
      await loadStockTakeItems();
      await loadAssignments();
    }
  } catch (error) {
    console.error('快速分配失败:', error);
    ElMessage.error('快速分配失败');
  }
};

// 提交分配
const handleAssignSubmit = async () => {
  if (!assignFormRef.value) return;

  await assignFormRef.value.validate(async (valid) => {
    if (!valid) return;

    // 检查是否选择了人员
    if (assignForm.assignType === 1 && assignForm.singleAssignee.length === 0) {
      ElMessage.warning('请选择至少一个负责人');
      return;
    }

    // 验证计划时间
    if (!assignForm.planStartTime) {
      ElMessage.warning('请选择计划开始时间');
      return;
    }

    if (!assignForm.planEndTime) {
      ElMessage.warning('请选择计划结束时间');
      return;
    }

    // 验证结束时间不能早于开始时间
    const startTime = new Date(assignForm.planStartTime);
    const endTime = new Date(assignForm.planEndTime);
    if (endTime <= startTime) {
      ElMessage.warning('计划结束时间必须晚于计划开始时间');
      return;
    }

    // 验证优先级
    if (!assignForm.priority) {
      ElMessage.warning('请选择优先级');
      return;
    }

    assigning.value = true;
    try {
      const stockTakeId = getStockTakeIdFromUrl();
      
      // 构建分配数据
      const assignmentData = {
        stockTakeId,
        assignType: assignForm.assignType,
        priority: assignForm.priority, // 新增：优先级
        planStartTime: assignForm.planStartTime, // 新增：计划开始时间
        planEndTime: assignForm.planEndTime, // 新增：计划结束时间
        remark: assignForm.remark
      };

      // 处理时间格式
      if (assignForm.planStartTime) {
        // 将格式转换为ISO格式：YYYY-MM-DDTHH:mm:ss
        assignmentData.planStartTime = assignForm.planStartTime.replace(' ', 'T');
      }
      
      if (assignForm.planEndTime) {
        assignmentData.planEndTime = assignForm.planEndTime.replace(' ', 'T');
      }

      // 构建分配数据
      if (assignForm.assignType === 1) {
        // 单人分配（现在支持多人）
        // 将选中的用户ID数组传递给后端
        assignmentData.assigneeIds = assignForm.singleAssignee;
        
        // 获取负责人名称（用于显示）
        const assigneeNames = assignForm.singleAssignee.map(id => 
          userList.value.find(user => user.id === id)?.name || '未知'
        );
        assignmentData.assigneeNames = assigneeNames;
        
        if (selectedItems.value.length > 0) {
          // 分配选中项给多个人
          assignmentData.items = selectedItems.value.map(item => ({
            stockTakeItemId: item.stockTakeItemId,
            assigneeIds: assignForm.singleAssignee // 传递给所有负责人
          }));
        }
        // 如果未选择具体项，则由后端处理分配所有未分配项
      } else {
          // 多人分配 - 按维度分配
          
          // 添加分配维度值
          assignmentData.assignDimension = getDimensionValue(assignForm.assignDimension);
          
          const conditions = [];
          
          // 根据选择的维度获取对应的数据
          let dimensionData = [];
          let dimensionField = '';
          
          switch (assignForm.assignDimension) {
            case 'batchNo':
              dimensionData = batchList.value;
              dimensionField = 'batchNo';
              break;
            case 'shelfCode':
              dimensionData = shelfList.value;
              dimensionField = 'shelfCode';
              break;
            case 'product':
              dimensionData = productList.value;
              dimensionField = 'productId';
              break;
          }
          
          // 收集已分配的条件
          dimensionData.forEach(item => {
            if (item.assigneeIds && item.assigneeIds.length > 0) {
              // 每个条件可以有多个负责人
              conditions.push({
                dimension: dimensionField,
                dimensionValue: item[dimensionField],
                assigneeIds: item.assigneeIds, // 改为数组
                assigneeNames: item.assigneeIds.map(id => 
                  userList.value.find(user => user.id === id)?.name || '未知'
                )
              });
            }
          });
          
          if (conditions.length === 0) {
            ElMessage.warning(`请在${assignForm.assignDimension === 'batchNo' ? '批次' : assignForm.assignDimension === 'shelfCode' ? '货架' : '商品'}表格中至少选择一项进行分配`);
            assigning.value = false;
            return;
          }
          
          assignmentData.conditions = conditions;
      }

      // 调用分配API
      const res = await post('/api/auth/stock/assignTask', assignmentData);
      if (res) {
        ElMessage.success('分配成功');
        assignDialogVisible.value = false;
        await loadStockTakeItems();
        await loadAssignments();
      }
    } catch (error) {
      console.error('分配失败:', error);
      ElMessage.error('分配失败');
    } finally {
      assigning.value = false;
    }
  });
};

// 分配对话框关闭
const handleAssignDialogClosed = () => {
  // 重置表单
  if (assignFormRef.value) {
    assignFormRef.value.resetFields();
  }
  Object.assign(assignForm, {
    assignType: 1,
    singleAssignee: [], // 重置为空数组
    assignDimension: 'batchNo',
    priority: 3, // 重置为默认值
    planStartTime: null, // 重置时间
    planEndTime: null, // 重置时间
    remark: ''
  });
  quickAssign.assigneeIds = [];
  
  // 重置维度数据中的负责人选择
  batchList.value.forEach(item => item.assigneeIds = []);
  shelfList.value.forEach(item => item.assigneeIds = []);
  productList.value.forEach(item => item.assigneeIds = []);
};

// 切换分配列表显示
const toggleAssignmentList = () => {
  showAssignmentList.value = !showAssignmentList.value;
};

// 查看分配详情
const viewAssignmentDetails = (assignment) => {
  currentAssignment.value = assignment;
  assignmentDetailVisible.value = true;
};

// 检查是否可以编辑行
const canEditRow = (row) => {
  // 检查权限字段
  if (!row.hasStockItemTaskPermission) {
    return false;
  }
  
  // 这里可以根据用户权限和分配状态来判断
  // 示例：只有分配给自己或未分配的项可以编辑
  return !row.assigneeId || row.assigneeId === currentUserId; // currentUserId需要从用户信息中获取
};

// 表格行类名函数
const tableRowClassName = ({ row }) => {
  if (!row.hasStockItemTaskPermission) {
    return 'no-permission';
  }
  return '';
};

// 根据状态名称获取标签类型
const getStatusTagTypeByName = (statusName) => {
  const mapping = {
    '新建': 'info',
    '盘点中': 'warning',
    '待确认': 'primary',
    '已完成': 'success',
    '已取消': 'danger'
  };
  return mapping[statusName] || 'info';
};

// 根据审核状态名称获取标签类型
const getApprovalStatusTagType = (approvalStatus) => {
  const mapping = {
    '待审批': 'warning',
    '已批准': 'success',
    '已拒绝': 'danger',
    '审批中': 'primary'
  };
  return mapping[approvalStatus] || 'info';
};

// 状态标签类型
const getStatusTagType = (status) => {
  const mapping = {
    1: 'info',
    2: 'warning',
    3: 'primary',
    4: 'success',
    5: 'danger'
  };
  return mapping[status] || 'info';
};

// 状态标签文本
const getStatusLabel = (status) => {
  const mapping = {
    1: '新建',
    2: '盘点中',
    3: '待确认',
    4: '已完成',
    5: '已取消'
  };
  return mapping[status] || '未知';
};

// 盘点项状态标签类型
const getItemStatusTagType = (status) => {
  const mapping = {
    1: 'info',
    2: 'warning',
    3: 'success'
  };
  return mapping[status] || 'info';
};

// 盘点项状态标签文本
const getItemStatusLabel = (status) => {
  const mapping = {
    1: '未盘',
    2: '已盘',
    3: '已确认'
  };
  return mapping[status] || '未知';
};

// 获取分配条件标签
const getConditionLabel = (condition) => {
  const { dimension, dimensionValue } = condition;
  const labels = {
    'batchNo': `批次: ${dimensionValue}`,
    'shelfCode': `货架: ${dimensionValue}`,
    'productId': `商品ID: ${dimensionValue}`
  };
  return labels[dimension] || `${dimension}: ${dimensionValue}`;
};

// 获取优先级标签类型
const getPriorityTagType = (priority) => {
  const mapping = {
    1: 'danger',     // 紧急 - 红色
    2: 'warning',    // 高 - 橙色
    3: 'primary',    // 中 - 蓝色
    4: 'info'        // 低 - 灰色
  };
  return mapping[priority] || 'info';
};

// 获取优先级标签文本
const getPriorityLabel = (priority) => {
  const mapping = {
    1: '紧急',
    2: '高',
    3: '中',
    4: '低'
  };
  return mapping[priority] || '未知';
};

// 格式化日期时间
const formatDateTime = (timestamp) => {
  if (!timestamp) return '--';
  // 如果是字符串格式的时间，直接返回
  if (typeof timestamp === 'string' && timestamp.includes('-')) {
    return timestamp;
  }
  const date = new Date(timestamp);
  return date.toLocaleString('zh-CN');
};

// 差异数量样式
const getDiffClass = (diff) => {
  if (diff > 0) return 'diff-positive';
  if (diff < 0) return 'diff-negative';
  return 'diff-zero';
};

// 数字格式化
const formatNumber = (num) => {
  if (num === null || num === undefined) return '0';
  const number = Number(num);
  if (isNaN(number)) return '0';
  return number.toLocaleString('zh-CN', {
    minimumFractionDigits: 0,
    maximumFractionDigits: 4
  });
};

// 禁用结束时间早于开始时间
const disabledEndDate = (time) => {
  if (!assignForm.planStartTime) return false;
  const startTime = new Date(assignForm.planStartTime);
  return time.getTime() <= startTime.getTime();
};

// 监听选中的项
watch(stockTakeItems, (newItems) => {
  selectedItems.value = newItems.filter(item => item.checked);
}, { deep: true });

// 初始化加载
onMounted(() => {
  const stockTakeId = getStockTakeIdFromUrl();
  if (stockTakeId) {
    loadStockTakeInfo();
    loadStockTakeItems();
    loadAssignments();
    loadUserList();
  } else {
    ElMessage.warning('请从盘点列表页面进入数据录入');
  }
});
</script>

<style scoped>
.stock-take-manage-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.manage-card {
  border-radius: 8px;
  min-height: calc(100vh - 100px);
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

/* 盘点单信息区域 */
.stock-take-info-section {
  margin-bottom: 20px;
}

.info-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  padding: 20px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 6px;
  border-left: 4px solid rgba(255, 255, 255, 0.3);
}

.info-item.full-width {
  grid-column: 1 / -1;
}

.info-label {
  font-size: 14px;
  font-weight: 500;
  opacity: 0.9;
}

.info-value {
  font-size: 14px;
  font-weight: bold;
}

/* 分配任务区域 */
.assignment-section {
  margin-bottom: 20px;
}

.assignment-card {
  border: 1px solid #e6e6e6;
}

.assignment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.assignment-title {
  font-size: 16px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 8px;
}

.assignment-table {
  width: 100%;
  margin-top: 10px;
}

.condition-tag {
  margin-right: 5px;
  margin-bottom: 5px;
}

.progress-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.progress-text {
  font-size: 12px;
  color: #666;
  min-width: 40px;
}

/* 统计区域 */
.stats-section {
  margin-bottom: 20px;
}

.stat-card {
  position: relative;
  border-radius: 8px;
  border: none;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  flex-direction: column;
}

.stat-number {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
}

.stat-number.diff-positive {
  color: #67C23A;
}

.stat-number.diff-negative {
  color: #F56C6C;
}

.stat-number.diff-zero {
  color: #909399;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.stat-icon {
  position: absolute;
  top: 20px;
  right: 20px;
  font-size: 48px;
  opacity: 0.3;
}

/* 筛选区域 */
.filter-section {
  padding: 20px 0;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 20px;
}

/* 表格区域 */
.stock-take-list-section {
  margin-top: 20px;
}

.stock-take-table {
  width: 100%;
  margin-bottom: 20px;
}

.sku-text {
  font-family: 'Courier New', monospace;
  font-weight: bold;
  color: #409EFF;
}

.product-info {
  display: flex;
  flex-direction: column;
}

.product-name {
  font-size: 14px;
  color: #303133;
  margin-bottom: 4px;
}

.product-spec {
  font-size: 12px;
  color: #909399;
}

.location-code {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.system-quantity {
  font-weight: bold;
  color: #303133;
}

.diff-positive {
  color: #67C23A;
  font-weight: bold;
}

.diff-negative {
  color: #F56C6C;
  font-weight: bold;
}

.diff-zero {
  color: #909399;
  font-weight: bold;
}

.action-buttons {
  display: flex;
  gap: 8px;
  justify-content: center;
}

/* 分配对话框样式 */
.multiple-assignment-section {
  margin-top: 15px;
}

.dimension-section {
  margin-bottom: 20px;
}

.dimension-section h4 {
  margin: 0 0 10px 0;
  color: #333;
  font-size: 14px;
  font-weight: 500;
}

.dimension-table {
  margin-bottom: 15px;
}

.quick-assign-section {
  margin-top: 20px;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.quick-assign-section h4 {
  margin: 0 0 10px 0;
  color: #333;
  font-size: 14px;
  font-weight: 500;
}

.quick-assign-form {
  margin-bottom: 0;
}

.user-option {
  display: flex;
  justify-content: space-between;
}

.user-department {
  font-size: 12px;
  color: #999;
  margin-left: 8px;
}

/* 分配详情对话框 */
.assignment-detail {
  padding: 10px 0;
}

.condition-detail {
  margin-top: 20px;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.condition-detail h4 {
  margin: 0 0 10px 0;
  color: #333;
  font-size: 14px;
  font-weight: 500;
}

.condition-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.progress-info {
  text-align: center;
  margin-top: 5px;
  font-size: 12px;
  color: #666;
}

/* 分页区域 */
.pagination-section {
  display: flex;
  justify-content: flex-end;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

/* 批量操作底部区域 */
.batch-actions-footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20px;
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 -2px 12px 0 rgba(0, 0, 0, 0.1);
  z-index: 1000;
}

.batch-actions-card {
  border: none;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.batch-actions-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
}

.selected-info {
  font-size: 14px;
  color: #303133;
}

.selected-count {
  font-weight: bold;
  color: #409EFF;
  font-size: 16px;
  margin: 0 4px;
}

.batch-buttons {
  display: flex;
  gap: 12px;
}

.no-permission-warning {
  color: #f56c6c;
  font-size: 12px;
  margin-left: 8px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .info-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .stock-take-manage-container {
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
  
  .info-grid {
    grid-template-columns: 1fr;
  }
  
  .stat-card {
    margin-bottom: 16px;
  }
  
  .filter-section .el-form-item {
    margin-bottom: 12px;
  }
  
  .action-buttons {
    flex-direction: column;
    gap: 4px;
  }
  
  .batch-actions-footer {
    padding: 10px;
  }
  
  .batch-actions-content {
    flex-direction: column;
    gap: 12px;
    text-align: center;
  }
  
  .assignment-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
}

/* 动画效果 */
.stock-take-table :deep(.el-table__row) {
  transition: all 0.3s;
}

.stock-take-table :deep(.el-table__row:hover) {
  background-color: #f5f7fa;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.stat-card {
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
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

.manage-card {
  animation: fadeIn 0.3s ease;
}

.user-avatar {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  margin-left: 8px;
}

/* 多选下拉框样式 */
:deep(.el-select__tags) {
  max-width: 100%;
}

:deep(.el-tag--small) {
  margin: 2px;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.user-department {
  font-size: 12px;
  color: #999;
  margin-left: 8px;
}

/* 多选下拉框样式 */
:deep(.el-select__tags) {
  max-width: 100%;
}

:deep(.el-tag--small) {
  margin: 2px;
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dimension-table :deep(.el-select) {
  width: 100%;
}

.dimension-table :deep(.el-select__tags) {
  flex-wrap: nowrap;
  overflow: hidden;
}

/* 无权限行样式 */
.stock-take-table :deep(.el-table__row.no-permission) {
  opacity: 0.6;
  background-color: #f5f5f5 !important;
}

.stock-take-table :deep(.el-table__row.no-permission:hover) {
  background-color: #eee !important;
}
</style>