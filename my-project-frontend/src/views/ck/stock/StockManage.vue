<template>
  <div class="stock-take-manage-container">
    <el-card class="manage-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">库存盘点管理</span>
          <div class="header-actions">
            <el-dropdown @command="handleCreate" trigger="click">
              <el-button type="primary">
                <el-icon><Plus /></el-icon>
                新建盘点任务
                <el-icon class="el-icon--right"><arrow-down /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="1">全库盘点</el-dropdown-item>
                  <el-dropdown-item command="2">区域盘点</el-dropdown-item>
                  <el-dropdown-item command="3">分类盘点</el-dropdown-item>
                  <el-dropdown-item command="4">循环盘点</el-dropdown-item>
                  <el-dropdown-item command="5">随机抽盘</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
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
          <el-form-item label="盘点单号">
            <el-input
              v-model="filterForm.orderNo"
              placeholder="请输入盘点单号"
              clearable
              style="width: 200px"
            />
          </el-form-item>
          <el-form-item label="盘点类型">
            <el-select
              v-model="filterForm.takeType"
              placeholder="全部类型"
              clearable
              style="width: 120px"
            >
              <el-option
                v-for="item in takeTypeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="盘点策略">
            <el-select
              v-model="filterForm.takeStrategy"
              placeholder="全部策略"
              clearable
              style="width: 120px"
            >
              <el-option
                v-for="item in takeStrategyOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
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
          <el-form-item label="执行状态">
            <el-select
              v-model="filterForm.executeStatus"
              placeholder="全部状态"
              clearable
              style="width: 120px"
            >
              <el-option
                v-for="item in executeStatusOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="审批状态">
            <el-select
              v-model="filterForm.approvalStatus"
              placeholder="审批状态"
              clearable
              style="width: 120px"
            >
              <el-option
                v-for="item in approvalStatusOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="创建时间">
            <el-date-picker
              v-model="filterForm.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              style="width: 240px"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 统计信息 -->
      <div class="stats-section">
        <el-row :gutter="20">
          <el-col :xs="12" :sm="6" :lg="3">
            <div class="stat-item total">
              <div class="stat-icon">
                <el-icon><Document /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ stats.total }}</div>
                <div class="stat-label">总单数</div>
              </div>
            </div>
          </el-col>
          <el-col :xs="12" :sm="6" :lg="3">
            <div class="stat-item pending">
              <div class="stat-icon">
                <el-icon><Clock /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ stats.inProgress }}</div>
                <div class="stat-label">进行中</div>
              </div>
            </div>
          </el-col>
          <el-col :xs="12" :sm="6" :lg="3">
            <div class="stat-item approved">
              <div class="stat-icon">
                <el-icon><CircleCheck /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ stats.completed }}</div>
                <div class="stat-label">已完成</div>
              </div>
            </div>
          </el-col>
          <el-col :xs="12" :sm="6" :lg="3">
            <div class="stat-item difference">
              <div class="stat-icon">
                <el-icon><Warning /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ stats.difference }}</div>
                <div class="stat-label">有差异</div>
              </div>
            </div>
          </el-col>
          <el-col :xs="12" :sm="6" :lg="3">
            <div class="stat-item accuracy">
              <div class="stat-icon">
                <el-icon><TrendCharts /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ stats.accuracyRate }}%</div>
                <div class="stat-label">准确率</div>
              </div>
            </div>
          </el-col>
          <el-col :xs="12" :sm="6" :lg="3">
            <div class="stat-item adjusted">
              <div class="stat-icon">
                <el-icon><Finished /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ stats.adjusted }}</div>
                <div class="stat-label">已调整</div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 盘点单列表 -->
      <div class="stock-take-list-section">
        <el-table
          :data="stockTakeList"
          v-loading="loading"
          empty-text="暂无盘点单数据"
          class="stock-take-table"
          row-key="id"
        >
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column label="盘点单号" width="200" fixed="left">
            <template #default="{ row }">
              <div class="order-info">
                <span class="order-no">{{ row.orderNo }}</span>
                <div class="order-name">{{ row.takeName }}</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="盘点方式" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="getTakeTypeTagType(row.takeType)" size="small">
                {{ getTakeTypeText(row.takeType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="盘点策略" width="120" align="center">
            <template #default="{ row }">
              <el-tag :type="getStrategyTagType(row.takeStrategy)" size="small">
                {{ getStrategyText(row.takeStrategy) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="仓库" width="120">
            <template #default="{ row }">
              <span>{{ row.warehouseName }}</span>
            </template>
          </el-table-column>
          <el-table-column label="盘点项数" width="100" align="center">
            <template #default="{ row }">
              <div class="count-info">
                <span class="completed">{{ row.completedItems }}/{{ row.totalItems }}</span>
                <el-progress 
                  v-if="row.totalItems > 0"
                  :percentage="Math.round((row.completedItems / row.totalItems) * 100)"
                  :show-text="false"
                  :stroke-width="4"
                  class="progress-bar"
                />
              </div>
            </template>
          </el-table-column>
          <el-table-column label="准确率" width="100" align="center">
            <template #default="{ row }">
              <div class="accuracy-info">
                <span :class="getAccuracyClass(row.accuracyRate)">
                  {{ row.accuracyRate }}%
                </span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="差异项数" width="100" align="center">
            <template #default="{ row }">
              <span :class="{ 'has-difference': row.differItems > 0 }">
                {{ row.differItems }} 项
              </span>
            </template>
          </el-table-column>
          <el-table-column label="总差异金额" width="120" align="right">
            <template #default="{ row }">
              <span v-if="row.totalDifferenceAmount > 0" class="difference-amount negative">
                ¥-{{ row.totalDifferenceAmount.toFixed(2) }}
              </span>
              <span v-else-if="row.totalDifferenceAmount < 0" class="difference-amount positive">
                ¥+{{ Math.abs(row.totalDifferenceAmount).toFixed(2) }}
              </span>
              <span v-else>¥0.00</span>
            </template>
          </el-table-column>
          <el-table-column label="执行状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag 
                :type="getExecuteStatusTagType(row.executeStatus)" 
                size="small"
              >
                {{ getExecuteStatusText(row.executeStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="审批状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag 
                :type="getApprovalStatusTagType(row.approvalStatus)" 
                size="small"
              >
                {{ getApprovalStatusText(row.approvalStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" width="160">
            <template #default="{ row }">
              <span>{{ formatTime(row.createdAt) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="300" fixed="right" align="center" min-width="300">
            <template #default="{ row }">
              <div class="action-buttons">
                <el-button
                  type="primary"
                  link
                  size="small"
                  @click="handleView(row)"
                >
                  详情
                </el-button>
                <el-button
                  type="warning"
                  link
                  size="small"
                  @click="handleEdit(row)"
                  v-if="[0, 4].includes(row.executeStatus) && [0, 1].includes(row.approvalStatus)"
                >
                  编辑
                </el-button>
                <el-button
                  type="success"
                  link
                  size="small"
                  @click="handleStartTake(row)"
                  v-if="row.executeStatus === 0 && row.approvalStatus === 2"
                >
                  开始盘点
                </el-button>
                <el-button
                  type="success"
                  link
                  size="small"
                  @click="handleSubmitApproval(row)"
                  v-if="row.approvalStatus === 0"
                >
                  提交审批
                </el-button>
                <el-button
                  type="info"
                  link
                  size="small"
                  @click="handleAssignTasks(row)"
                  v-if="row.executeStatus === 1 && row.approvalStatus === 2"
                >
                  分配任务
                </el-button>
                <el-button
                  type="danger"
                  link
                  size="small"
                  @click="handleDelete(row)"
                  v-if="[0, 4, 9].includes(row.executeStatus)"
                >
                  删除
                </el-button>
                <el-button
                  type="info"
                  link
                  size="small"
                  @click="handleCancel(row)"
                  v-if="[0, 1].includes(row.executeStatus)"
                >
                  取消
                </el-button>
                <el-button
                  type="success"
                  link
                  size="small"
                  @click="handleComplete(row)"
                  v-if="row.executeStatus === 1"
                >
                  完成
                </el-button>
                <el-button
                  type="warning"
                  link
                  size="small"
                  @click="handleAdjust(row)"
                  v-if="row.executeStatus === 2 && row.differItems > 0"
                >
                  调整
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

    <!-- 盘点单详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      :title="`盘点单详情 - ${currentStockTake?.orderNo || '未知单号'}`"
      width="95%"
      top="5vh"
      class="detail-dialog"
    >
      <div v-if="currentStockTake" class="detail-content">
        <!-- 基本信息 -->
        <el-card class="detail-section" shadow="never">
          <template #header>
            <div class="section-header">
              <span class="section-title">盘点基本信息</span>
              <div class="section-actions">
                <el-button 
                  size="small" 
                  type="primary"
                  @click="exportReport(currentStockTake)"
                >
                  导出报告
                </el-button>
              </div>
            </div>
          </template>
          <el-descriptions :column="4" border>
            <el-descriptions-item label="盘点单号">{{ currentStockTake.orderNo }}</el-descriptions-item>
            <el-descriptions-item label="盘点名称">{{ currentStockTake.takeName }}</el-descriptions-item>
            <el-descriptions-item label="盘点方式">{{ getTakeTypeText(currentStockTake.takeType) }}</el-descriptions-item>
            <el-descriptions-item label="盘点策略">{{ getStrategyText(currentStockTake.takeStrategy) }}</el-descriptions-item>
            <el-descriptions-item label="仓库">{{ currentStockTake.warehouseName }}</el-descriptions-item>
            <el-descriptions-item label="允许差异率">{{ currentStockTake.toleranceRate }}%</el-descriptions-item>
            <el-descriptions-item label="总盘点项数">{{ currentStockTake.totalItems }} 项</el-descriptions-item>
            <el-descriptions-item label="已完成项数">{{ currentStockTake.completedItems }} 项</el-descriptions-item>
            <el-descriptions-item label="准确项数">{{ currentStockTake.accuracyItems }} 项</el-descriptions-item>
            <el-descriptions-item label="差异项数">
              <span :class="{ 'text-danger': currentStockTake.differItems > 0 }">
                {{ currentStockTake.differItems }} 项
              </span>
            </el-descriptions-item>
            <el-descriptions-item label="准确率">
              <span :class="getAccuracyClass(currentStockTake.accuracyRate)">
                {{ currentStockTake.accuracyRate }}%
              </span>
            </el-descriptions-item>
            <el-descriptions-item label="执行状态">
              <el-tag :type="getExecuteStatusTagType(currentStockTake.executeStatus)" size="small">
                {{ getExecuteStatusText(currentStockTake.executeStatus) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="审批状态">
              <el-tag :type="getApprovalStatusTagType(currentStockTake.approvalStatus)" size="small">
                {{ getApprovalStatusText(currentStockTake.approvalStatus) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="调整状态">
              <el-tag :type="getAdjustStatusTagType(currentStockTake.adjustStatus)" size="small">
                {{ getAdjustStatusText(currentStockTake.adjustStatus) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="计划时间">
              {{ formatTime(currentStockTake.planStartTime) }} ~ {{ formatTime(currentStockTake.planEndTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="实际时间" v-if="currentStockTake.actualStartTime">
              {{ formatTime(currentStockTake.actualStartTime) }} ~ {{ formatTime(currentStockTake.actualEndTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="总差异金额">
              <span :class="currentStockTake.totalDifferenceAmount > 0 ? 'text-danger' : 'text-success'">
                ¥{{ Math.abs(currentStockTake.totalDifferenceAmount).toFixed(2) }}
                {{ currentStockTake.totalDifferenceAmount > 0 ? '盘亏' : '盘盈' }}
              </span>
            </el-descriptions-item>
            <el-descriptions-item label="备注" :span="4">{{ currentStockTake.remark || '--' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 盘点任务分配 -->
        <el-card class="detail-section" shadow="never" v-if="currentStockTake.tasks && currentStockTake.tasks.length > 0">
          <template #header>
            <div class="section-header">
              <span class="section-title">盘点任务分配</span>
              <span class="section-subtitle">共 {{ currentStockTake.tasks.length }} 个任务</span>
            </div>
          </template>
          <el-table :data="currentStockTake.tasks" border style="width: 100%">
            <el-table-column type="index" label="序号" width="60" align="center" />
            <el-table-column label="任务编号" width="150" prop="taskNo" />
            <el-table-column label="执行人" width="120">
              <template #default="{ row }">
                <div>{{ row.assigneeName }}</div>
                <div class="text-muted small">{{ row.assigneeId }}</div>
              </template>
            </el-table-column>
            <el-table-column label="负责区域" min-width="200">
              <template #default="{ row }">
                <div v-if="row.areaCodes && row.areaCodes.length > 0">
                  <el-tag 
                    v-for="area in row.areaCodes" 
                    :key="area"
                    size="small"
                    type="info"
                    class="area-tag"
                  >
                    {{ area }}
                  </el-tag>
                </div>
                <span v-else>--</span>
              </template>
            </el-table-column>
            <el-table-column label="盘点项数" width="120" align="center">
              <template #default="{ row }">
                <div>{{ row.actualItems || 0 }} 项</div>
              </template>
            </el-table-column>
            <el-table-column label="进度" width="150" align="center">
              <template #default="{ row }">
                <div class="task-progress">
                  <el-progress 
                    :percentage="row.progressRate || 0" 
                    :stroke-width="8"
                    :show-text="false"
                  />
                  <span class="progress-text">{{ row.progressRate || 0 }}%</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="准确率" width="100" align="center">
              <template #default="{ row }">
                <span :class="getTaskAccuracyClass(row.accuracyRate)">
                  {{ row.accuracyRate || 0 }}%
                </span>
              </template>
            </el-table-column>
            <el-table-column label="任务状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="getTaskStatusTagType(row.status)" size="small">
                  {{ getTaskStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="耗时" width="100" align="center">
              <template #default="{ row }">
                <span>{{ row.durationMinutes || 0 }}分钟</span>
              </template>
            </el-table-column>
            <el-table-column label="质量评分" width="100" align="center">
              <template #default="{ row }">
                <el-rate 
                  v-model="row.qualityScore" 
                  disabled
                  :max="10"
                  :allow-half="true"
                  show-score
                  text-color="#ff9900"
                  score-template="{value}"
                />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" align="center" fixed="right">
              <template #default="{ row }">
                <el-button
                  type="primary"
                  link
                  size="small"
                  @click="viewTaskDetails(row)"
                >
                  详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <!-- 差异分析汇总 -->
        <el-card class="detail-section" shadow="never" v-if="currentStockTake.differItems > 0">
          <template #header>
            <div class="section-header">
              <span class="section-title">差异分析</span>
              <el-button 
                size="small" 
                type="warning"
                @click="handleAdjust(currentStockTake)"
                v-if="currentStockTake.adjustStatus === 0"
              >
                生成调整单
              </el-button>
            </div>
          </template>
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="difference-summary">
                <div class="summary-title">差异分布</div>
                <div class="summary-content">
                  <div v-for="item in currentStockTake.differenceSummary" :key="item.reason" class="summary-item">
                    <div class="reason">{{ getDifferenceReasonText(item.reason) }}</div>
                    <div class="count">{{ item.count }} 项</div>
                    <div class="percentage">{{ item.percentage }}%</div>
                  </div>
                </div>
              </div>
            </el-col>
            <el-col :span="16">
              <div class="difference-chart">
                <div class="chart-title">差异金额TOP 10</div>
                <el-table :data="currentStockTake.topDifferences" border style="width: 100%">
                  <el-table-column type="index" label="序号" width="60" align="center" />
                  <el-table-column label="产品名称" min-width="200" prop="productName" />
                  <el-table-column label="SKU" width="120" prop="sku" />
                  <el-table-column label="系统数量" width="100" align="center">
                    <template #default="{ row }">
                      {{ row.systemQuantity }}
                    </template>
                  </el-table-column>
                  <el-table-column label="实际数量" width="100" align="center">
                    <template #default="{ row }">
                      {{ row.actualQuantity }}
                    </template>
                  </el-table-column>
                  <el-table-column label="差异数量" width="100" align="center">
                    <template #default="{ row }">
                      <span :class="row.quantityDifference > 0 ? 'text-success' : 'text-danger'">
                        {{ row.quantityDifference > 0 ? '+' : '' }}{{ row.quantityDifference }}
                      </span>
                    </template>
                  </el-table-column>
                  <el-table-column label="差异金额" width="120" align="right">
                    <template #default="{ row }">
                      <span :class="row.differenceAmount > 0 ? 'text-success' : 'text-danger'">
                        ¥{{ Math.abs(row.differenceAmount).toFixed(2) }}
                        {{ row.differenceAmount > 0 ? '盘盈' : '盘亏' }}
                      </span>
                    </template>
                  </el-table-column>
                  <el-table-column label="差异原因" width="150">
                    <template #default="{ row }">
                      {{ getDifferenceReasonText(row.differenceReason) }}
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </el-col>
          </el-row>
        </el-card>

        <!-- 盘点明细 -->
        <el-card class="detail-section" shadow="never">
          <template #header>
            <div class="section-header">
              <span class="section-title">盘点明细</span>
              <el-input
                v-model="detailFilter.keyword"
                placeholder="搜索产品名称/SKU"
                style="width: 200px"
                clearable
                @clear="loadStockTakeDetail(currentStockTake.id)"
                @keyup.enter="filterDetailItems"
              >
                <template #append>
                  <el-button @click="filterDetailItems">
                    <el-icon><Search /></el-icon>
                  </el-button>
                </template>
              </el-input>
            </div>
          </template>
          
          <el-tabs v-model="detailTab" @tab-change="handleDetailTabChange">
            <el-tab-pane label="全部明细" name="all">
              <el-table 
                :data="currentStockTake.items" 
                border 
                style="width: 100%"
                v-loading="detailLoading"
              >
                <el-table-column type="index" label="序号" width="60" align="center" />
                <el-table-column label="产品信息" min-width="250" fixed="left">
                  <template #default="{ row }">
                    <div class="product-info">
                      <div class="product-name">{{ row.productName }}</div>
                      <div class="product-sku">SKU: {{ row.sku }}</div>
                      <div class="product-location" v-if="row.locationCode">
                        <el-tag size="small" type="info">
                          {{ row.locationCode }}
                        </el-tag>
                      </div>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column label="批次号" width="120" prop="batchNo">
                  <template #default="{ row }">
                    <span>{{ row.batchNo || '--' }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="系统数量" width="100" align="center">
                  <template #default="{ row }">
                    {{ row.systemQuantity }}
                  </template>
                </el-table-column>
                <el-table-column label="实际数量" width="100" align="center">
                  <template #default="{ row }">
                    {{ row.finalCountQuantity }}
                  </template>
                </el-table-column>
                <el-table-column label="差异数量" width="100" align="center">
                  <template #default="{ row }">
                    <span :class="row.quantityDifference > 0 ? 'text-success' : 'text-danger'">
                      {{ row.quantityDifference > 0 ? '+' : '' }}{{ row.quantityDifference }}
                    </span>
                  </template>
                </el-table-column>
                <el-table-column label="差异率" width="100" align="center">
                  <template #default="{ row }">
                    <span :class="Math.abs(row.differenceRate) > (currentStockTake.toleranceRate || 0.5) ? 'text-danger' : ''">
                      {{ row.differenceRate }}%
                    </span>
                  </template>
                </el-table-column>
                <el-table-column label="盘点状态" width="100" align="center">
                  <template #default="{ row }">
                    <el-tag :type="getCountStatusTagType(row.countStatus)" size="small">
                      {{ getCountStatusText(row.countStatus) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="盘点方式" width="100" align="center">
                  <template #default="{ row }">
                    {{ getCountMethodText(row.countMethod) }}
                  </template>
                </el-table-column>
                <el-table-column label="差异原因" width="150">
                  <template #default="{ row }">
                    <span v-if="row.differenceReason">
                      {{ getDifferenceReasonText(row.differenceReason) }}
                    </span>
                    <span v-else>--</span>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="120" align="center" fixed="right">
                  <template #default="{ row }">
                    <el-button
                      type="primary"
                      link
                      size="small"
                      @click="viewItemDetail(row)"
                      v-if="row.countStatus !== 0"
                    >
                      详情
                    </el-button>
                    <el-button
                      type="warning"
                      link
                      size="small"
                      @click="recountItem(row)"
                      v-if="currentStockTake.executeStatus === 1 && row.countStatus === 3"
                    >
                      重盘
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
              
              <!-- 明细分页 -->
              <div class="detail-pagination" v-if="currentStockTake.totalDetailItems > 20">
                <el-pagination
                  v-model:current-page="detailPagination.current"
                  v-model:page-size="detailPagination.size"
                  :total="currentStockTake.totalDetailItems"
                  :page-sizes="[20, 50, 100]"
                  layout="total, sizes, prev, pager, next"
                  @size-change="handleDetailSizeChange"
                  @current-change="handleDetailCurrentChange"
                />
              </div>
            </el-tab-pane>
            <el-tab-pane label="有差异" name="difference">
              <el-table :data="differenceItems" border style="width: 100%">
                <!-- 同全部明细的列配置，省略重复代码 -->
                <el-table-column type="index" label="序号" width="60" align="center" />
                <el-table-column label="产品信息" min-width="250" prop="productName" />
                <el-table-column label="差异数量" width="100" align="center">
                  <template #default="{ row }">
                    <span :class="row.quantityDifference > 0 ? 'text-success' : 'text-danger'">
                      {{ row.quantityDifference > 0 ? '+' : '' }}{{ row.quantityDifference }}
                    </span>
                  </template>
                </el-table-column>
                <el-table-column label="差异金额" width="120" align="right">
                  <template #default="{ row }">
                    <span :class="row.differenceAmount > 0 ? 'text-success' : 'text-danger'">
                      ¥{{ Math.abs(row.differenceAmount).toFixed(2) }}
                    </span>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
            <el-tab-pane label="未盘点" name="pending">
              <el-table :data="pendingItems" border style="width: 100%">
                <!-- 列配置 -->
              </el-table>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </div>
      <div v-else class="no-data">
        <el-empty description="数据加载失败" />
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
          <el-button 
            type="primary" 
            @click="handleEdit(currentStockTake)"
            v-if="currentStockTake && [0, 4].includes(currentStockTake.executeStatus) && [0, 1].includes(currentStockTake.approvalStatus)"
          >
            编辑
          </el-button>
          <el-button 
            type="success" 
            @click="handleStartTake(currentStockTake)"
            v-if="currentStockTake && currentStockTake.executeStatus === 0 && currentStockTake.approvalStatus === 2"
          >
            开始盘点
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Refresh, Document, Clock, CircleCheck, Finished, Warning, TrendCharts, Search } from '@element-plus/icons-vue';
import { post, get } from '@/net';

const router = useRouter();
const loading = ref(false);
const detailDialogVisible = ref(false);
const currentStockTake = ref(null);
const detailLoading = ref(false);
const detailTab = ref('all');

// 盘点类型映射
const stockTakeTypeMap = {
  'full': 'full',
  'area': 'area',
  'category': 'category',
  'cycle': 'cycle',
  'random': 'random'
};

// 筛选表单
const filterForm = reactive({
  orderNo: '',
  takeType: '',
  takeStrategy: '',
  warehouseId: '',
  executeStatus: '',
  approvalStatus: '',
  dateRange: []
});

// 详情筛选
const detailFilter = reactive({
  keyword: '',
  countStatus: '',
  differenceReason: ''
});

// 分页信息
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
});

// 详情分页
const detailPagination = reactive({
  current: 1,
  size: 20,
  total: 0
});

// 统计信息
const stats = reactive({
  total: 0,
  inProgress: 0,
  completed: 0,
  difference: 0,
  accuracyRate: 0,
  adjusted: 0
});

// 盘点单列表
const stockTakeList = ref([]);
const warehouseList = ref([]);

// 选项数据
const takeTypeOptions = [
  { value: 1, label: '动态盘点' },
  { value: 2, label: '静态盘点' }
];

const takeStrategyOptions = [
  { value: 1, label: '全库盘点' },
  { value: 2, label: '区域盘点' },
  { value: 3, label: '分类盘点' },
  { value: 4, label: '循环盘点' },
  { value: 5, label: '随机抽盘' }
];

const executeStatusOptions = [
  { value: 0, label: '未开始' },
  { value: 1, label: '进行中' },
  { value: 2, label: '已完成' },
  { value: 3, label: '已暂停' },
  { value: 9, label: '已取消' }
];

const approvalStatusOptions = [
  { value: 0, label: '未提交' },
  { value: 1, label: '审核中' },
  { value: 2, label: '已批准' },
  { value: 3, label: '已驳回' }
];

const countStatusOptions = [
  { value: 0, label: '待盘点' },
  { value: 1, label: '已初盘' },
  { value: 2, label: '已复盘' },
  { value: 3, label: '已确认' },
  { value: 4, label: '差异待处理' },
  { value: 5, label: '已调整' }
];

const countMethodOptions = [
  { value: 1, label: '人工盘点' },
  { value: 2, label: 'RFID盘点' },
  { value: 3, label: '视觉识别' },
  { value: 4, label: '自动化设备' }
];

// 加载盘点单详情
const loadStockTakeDetail = async (id) => {
  try {
    const res = await get(`/api/auth/stock-take/detail?takeId=${id}`);
    console.log('盘点单详情响应:', res);
    
    if (res) {
      const detailData = res;
      return {
        // 基本信息
        id: detailData.id,
        orderNo: detailData.orderNo,
        takeName: detailData.takeName,
        takeType: detailData.takeType,
        takeStrategy: detailData.takeStrategy,
        warehouseId: detailData.warehouseId,
        warehouseName: detailData.warehouseName,
        toleranceRate: detailData.toleranceRate,
        remark: detailData.remark,
        executeStatus: detailData.executeStatus,
        approvalStatus: detailData.approvalStatus,
        adjustStatus: detailData.adjustStatus,
        
        // 统计信息
        totalItems: detailData.totalItems || 0,
        completedItems: detailData.completedItems || 0,
        accuracyItems: detailData.accuracyItems || 0,
        differItems: detailData.differItems || 0,
        accuracyRate: detailData.accuracyRate || 0,
        totalDifferenceAmount: detailData.totalDifferenceAmount || 0,
        
        // 时间信息
        planStartTime: detailData.planStartTime,
        planEndTime: detailData.planEndTime,
        actualStartTime: detailData.actualStartTime,
        actualEndTime: detailData.actualEndTime,
        createdAt: detailData.createdAt,
        modifiedAt: detailData.modifiedAt,
        
        // 明细数据
        items: detailData.items || [],
        totalDetailItems: detailData.totalDetailItems || 0,
        
        // 任务分配
        tasks: detailData.tasks || [],
        
        // 差异分析
        differenceSummary: detailData.differenceSummary || [],
        topDifferences: detailData.topDifferences || []
      };
    } else {
      console.error('API返回数据格式异常:', res);
      return null;
    }
  } catch (error) {
    console.error('加载盘点单详情失败:', error);
    ElMessage.error('加载详情失败: ' + (error.message || '未知错误'));
    return null;
  }
};

// 加载统计信息
const loadStats = async () => {
  try {
    const res = await post('/api/auth/stock-take/counts');
    if (res) {
      stats.total = res.totalCount || 0;
      stats.inProgress = res.inProgressCount || 0;
      stats.completed = res.completedCount || 0;
      stats.difference = res.differenceCount || 0;
      stats.accuracyRate = res.accuracyRate || 0;
      stats.adjusted = res.adjustedCount || 0;
    }
  } catch (error) {
    console.error('加载统计信息失败:', error);
    stats.total = 0;
    stats.inProgress = 0;
    stats.completed = 0;
    stats.difference = 0;
    stats.accuracyRate = 0;
    stats.adjusted = 0;
  }
};

// 方法
const loadStockTakeList = async () => {
  loading.value = true;
  try {
    const params = {
      page: pagination.current,
      size: pagination.size,
      ...filterForm
    };
    
    // 处理日期范围
    if (filterForm.dateRange && filterForm.dateRange.length === 2) {
      params.startDate = filterForm.dateRange[0];
      params.endDate = filterForm.dateRange[1];
    }
    
    const res = await post('/api/auth/stock-take/pageList', params);
    if (res && res.records) {
      stockTakeList.value = res.records.map(take => ({
        id: take.id || '',
        orderNo: take.orderNo || '',
        takeName: take.takeName || '',
        takeType: take.takeType || 1,
        takeStrategy: take.takeStrategy || 1,
        warehouseId: take.warehouseId || '',
        warehouseName: take.warehouseName || '',
        totalItems: take.totalItems || 0,
        completedItems: take.completedItems || 0,
        accuracyItems: take.accuracyItems || 0,
        differItems: take.differItems || 0,
        accuracyRate: take.accuracyRate || 0,
        totalDifferenceAmount: take.totalDifferenceAmount || 0,
        executeStatus: take.executeStatus || 0,
        approvalStatus: take.approvalStatus || 0,
        adjustStatus: take.adjustStatus || 0,
        planStartTime: take.planStartTime,
        planEndTime: take.planEndTime,
        remark: take.remark || '',
        createdAt: take.createdAt || new Date().toISOString(),
        modifiedAt: take.modifiedAt || new Date().toISOString()
      }));
      pagination.total = res.total || 0;
    } else {
      stockTakeList.value = [];
      pagination.total = 0;
    }
  } catch (error) {
    console.error('加载盘点单列表失败:', error);
    ElMessage.error('加载盘点单列表失败');
    stockTakeList.value = [];
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
  loadStockTakeList();
  loadStats();
};

const handleSearch = () => {
  pagination.current = 1;
  loadStockTakeList();
  loadStats();
};

const handleReset = () => {
  Object.assign(filterForm, {
    orderNo: '',
    takeType: '',
    takeStrategy: '',
    warehouseId: '',
    executeStatus: '',
    approvalStatus: '',
    dateRange: []
  });
  pagination.current = 1;
  loadStockTakeList();
  loadStats();
};

const handleSizeChange = (size) => {
  pagination.size = size;
  pagination.current = 1;
  loadStockTakeList();
};

const handleCurrentChange = (page) => {
  pagination.current = page;
  loadStockTakeList();
};

const handleCreate = (command) => {
    const routeMap = {
        1: '/index/ckStockFullWareHouse',      // 全库盘点
        2: '/index/ckStockAreaWise',  // 区域盘点
        3: '/index/ckStockCategoryWise',  // 分类盘点
        4: '/index/ckStockCycleWise',  // 循环盘点
        5: '/index/ckStockRandomWise'  // 随机抽盘
    };
    
    const targetRoute = routeMap[command] || '/index/ckStockTakeFullCreate';
    if (targetRoute) {
        router.push(targetRoute);
    }
};

const handleView = async (take) => {
  detailLoading.value = true;
  try {
    const detail = await loadStockTakeDetail(take.id);
    if (detail) {
      currentStockTake.value = detail;
      detailDialogVisible.value = true;
    }
  } finally {
    detailLoading.value = false;
  }
};

const handleEdit = (take) => {
  const typeMap = {
    1: 'Full',
    2: 'Area',
    3: 'Category',
    4: 'Cycle',
    5: 'Random'
  };
  
  const routeName = typeMap[take.takeStrategy] || 'Full';
  router.push(`/index/ckStockTake${routeName}Edit/${take.id}`);
};

const handleStartTake = async (take) => {
  try {
    await ElMessageBox.confirm(
      `确定要开始盘点"${take.takeName}"吗？`,
      '开始盘点确认',
      { type: 'warning' }
    );
    
    const res = await post('/api/auth/stock-take/start', {
      takeId: take.id
    });
    
    if (res) {
      ElMessage.success('盘点已开始');
      refreshList();
      if (detailDialogVisible.value) {
        handleView(take);
      }
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('开始盘点失败');
    }
  }
};

const handleSubmitApproval = async (take) => {
  try {
    await ElMessageBox.confirm(
      `确定要提交盘点单"${take.takeName}"审批吗？`,
      '提交审批确认',
      { type: 'warning' }
    );
    
    const res = await post('/api/auth/stock-take/submit-approval', {
      takeId: take.id
    });
    
    if (res) {
      ElMessage.success('已提交审批');
      refreshList();
      if (detailDialogVisible.value) {
        handleView(take);
      }
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('提交审批失败');
    }
  }
};

const handleAssignTasks = (take) => {
  router.push(`/index/ckStockTakeAssign/${take.id}`);
};

const handleDelete = async (take) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除盘点单"${take.takeName}"吗？此操作不可恢复！`,
      '删除确认',
      { 
        type: 'error',
        confirmButtonText: '确定删除',
        cancelButtonText: '取消'
      }
    );
    
    const res = await post('/api/auth/stock-take/delete', {
      id: take.id
    });
    
    if (res) {
      ElMessage.success('删除成功');
      refreshList();
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败');
    }
  }
};

const handleCancel = async (take) => {
  try {
    await ElMessageBox.confirm(
      `确定要取消盘点单"${take.takeName}"吗？`,
      '取消确认',
      { type: 'warning' }
    );
    
    const res = await post('/api/auth/stock-take/cancel', {
      id: take.id
    });
    
    if (res) {
      ElMessage.success('已取消');
      refreshList();
      if (detailDialogVisible.value) {
        handleView(take);
      }
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消失败');
    }
  }
};

const handleComplete = async (take) => {
  try {
    await ElMessageBox.confirm(
      `确定要完成盘点单"${take.takeName}"吗？完成前请确保所有项目已盘点完毕。`,
      '完成确认',
      { type: 'warning' }
    );
    
    const res = await post('/api/auth/stock-take/complete', {
      id: take.id
    });
    
    if (res) {
      ElMessage.success('盘点完成');
      refreshList();
      if (detailDialogVisible.value) {
        handleView(take);
      }
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('完成失败');
    }
  }
};

const handleAdjust = async (take) => {
  try {
    await ElMessageBox.confirm(
      `确定要为盘点单"${take.takeName}"生成调整单吗？`,
      '生成调整单确认',
      { type: 'warning' }
    );
    
    const res = await post('/api/auth/stock-take/generate-adjust-order', {
      takeId: take.id
    });
    
    if (res) {
      ElMessage.success('调整单已生成');
      refreshList();
      if (detailDialogVisible.value) {
        handleView(take);
      }
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('生成调整单失败');
    }
  }
};

// 查看任务详情
const viewTaskDetails = (task) => {
  console.log('查看任务详情:', task);
  // 这里可以打开任务详情弹窗
};

// 查看明细详情
const viewItemDetail = (item) => {
  console.log('查看明细详情:', item);
  // 这里可以打开明细详情弹窗
};

// 重新盘点
const recountItem = async (item) => {
  try {
    await ElMessageBox.confirm(
      `确定要重新盘点"${item.productName}"吗？`,
      '重新盘点确认',
      { type: 'warning' }
    );
    
    const res = await post('/api/auth/stock-take-item/recount', {
      itemId: item.id
    });
    
    if (res) {
      ElMessage.success('已设置为重新盘点');
      if (currentStockTake.value) {
        handleView(currentStockTake.value);
      }
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败');
    }
  }
};

// 导出报告
const exportReport = async (take) => {
  try {
    loading.value = true;
    // 这里实现导出逻辑
    ElMessage.success('导出功能开发中');
  } finally {
    loading.value = false;
  }
};

// 过滤明细项目
const filterDetailItems = () => {
  // 实现过滤逻辑
  console.log('过滤明细项目:', detailFilter.keyword);
};

// 处理详情标签页切换
const handleDetailTabChange = (tab) => {
  console.log('切换到标签页:', tab);
  // 加载对应标签页的数据
};

// 处理详情分页大小变化
const handleDetailSizeChange = (size) => {
  detailPagination.size = size;
  detailPagination.current = 1;
  // 重新加载明细数据
  if (currentStockTake.value) {
    handleView(currentStockTake.value);
  }
};

// 处理详情分页变化
const handleDetailCurrentChange = (page) => {
  detailPagination.current = page;
  // 重新加载明细数据
  if (currentStockTake.value) {
    handleView(currentStockTake.value);
  }
};

// 计算属性：有差异的项目
const differenceItems = computed(() => {
  if (!currentStockTake.value || !currentStockTake.value.items) return [];
  return currentStockTake.value.items.filter(item => item.quantityDifference !== 0);
});

// 计算属性：未盘点的项目
const pendingItems = computed(() => {
  if (!currentStockTake.value || !currentStockTake.value.items) return [];
  return currentStockTake.value.items.filter(item => item.countStatus === 0);
});

// 文本转换方法
const getTakeTypeText = (type) => {
  const typeObj = takeTypeOptions.find(item => item.value === type);
  return typeObj ? typeObj.label : '未知';
};

const getTakeTypeTagType = (type) => {
  return type === 1 ? 'success' : 'warning';
};

const getStrategyText = (strategy) => {
  const strategyObj = takeStrategyOptions.find(item => item.value === strategy);
  return strategyObj ? strategyObj.label : '未知';
};

const getStrategyTagType = (strategy) => {
  const types = {
    1: 'danger',
    2: 'warning',
    3: 'success',
    4: 'info',
    5: 'primary'
  };
  return types[strategy] || '';
};

const getExecuteStatusText = (status) => {
  const statusObj = executeStatusOptions.find(item => item.value === status);
  return statusObj ? statusObj.label : '未知';
};

const getExecuteStatusTagType = (status) => {
  const types = {
    0: 'info',
    1: 'warning',
    2: 'success',
    3: 'danger',
    9: 'info'
  };
  return types[status] || '';
};

const getApprovalStatusText = (status) => {
  const statusObj = approvalStatusOptions.find(item => item.value === status);
  return statusObj ? statusObj.label : '未知';
};

const getApprovalStatusTagType = (status) => {
  const types = {
    0: 'info',
    1: 'warning',
    2: 'success',
    3: 'danger'
  };
  return types[status] || '';
};

const getAdjustStatusTagType = (status) => {
  const types = {
    0: 'info',
    1: 'warning',
    2: 'success'
  };
  return types[status] || '';
};

const getAdjustStatusText = (status) => {
  const texts = {
    0: '未调整',
    1: '调整中',
    2: '已调整'
  };
  return texts[status] || '未知';
};

const getCountStatusText = (status) => {
  const statusObj = countStatusOptions.find(item => item.value === status);
  return statusObj ? statusObj.label : '未知';
};

const getCountStatusTagType = (status) => {
  const types = {
    0: 'info',
    1: 'warning',
    2: 'warning',
    3: 'success',
    4: 'danger',
    5: 'success'
  };
  return types[status] || '';
};

const getCountMethodText = (method) => {
  const methodObj = countMethodOptions.find(item => item.value === method);
  return methodObj ? methodObj.label : '未知';
};

const getTaskStatusText = (status) => {
  const texts = {
    0: '待开始',
    1: '进行中',
    2: '已完成',
    3: '异常中断',
    4: '已取消'
  };
  return texts[status] || '未知';
};

const getTaskStatusTagType = (status) => {
  const types = {
    0: 'info',
    1: 'warning',
    2: 'success',
    3: 'danger',
    4: 'info'
  };
  return types[status] || '';
};

// 获取差异原因文本（需要根据你的枚举类来实现）
const getDifferenceReasonText = (reasonCode) => {
  const reasonMap = {
    1: '录入错误',
    2: '漏盘',
    3: '偷盗丢失',
    4: '损坏未报',
    5: '自然损耗',
    6: '系统错误',
    7: '多盘重复',
    8: '单位换算错误',
    9: '盘点时在途',
    99: '其他原因'
  };
  return reasonMap[reasonCode] || '未知原因';
};

// 准确率样式 TODO yang
const getAccuracyClass1 = (rate) => { 
  if (rate >= 99.5) return 'accuracy-high';
  if (rate >= 98) return 'accuracy-medium';
  return 'accuracy-low';
};

const getTaskAccuracyClass = (rate) => {
  if (rate >= 99.5) return 'text-success';
  if (rate >= 98) return 'text-warning';
  return 'text-danger';
};
//TODO yang
const getAccuracyClass2 = (rate) => {
  if (rate >= 99.5) return 'text-success';
  if (rate >= 98) return 'text-warning';
  return 'text-danger';
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
  loadStockTakeList();
  loadWarehouseList();
  loadStats();
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
  height: 100%;
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

.stat-item.pending .stat-icon {
  background-color: #E6A23C;
}

.stat-item.approved .stat-icon {
  background-color: #67C23A;
}

.stat-item.difference .stat-icon {
  background-color: #F56C6C;
}

.stat-item.accuracy .stat-icon {
  background-color: #909399;
}

.stat-item.adjusted .stat-icon {
  background-color: #9c27b0;
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

.stock-take-list-section {
  margin-top: 20px;
}

.stock-take-table {
  width: 100%;
}

.order-info {
  line-height: 1.4;
}

.order-no {
  font-family: 'Courier New', monospace;
  font-weight: bold;
  color: #409EFF;
  display: block;
}

.order-name {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.count-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.progress-bar {
  width: 80px;
}

.accuracy-info {
  display: flex;
  justify-content: center;
  align-items: center;
}

.accuracy-high {
  color: #67C23A;
  font-weight: bold;
}

.accuracy-medium {
  color: #E6A23C;
  font-weight: bold;
}

.accuracy-low {
  color: #F56C6C;
  font-weight: bold;
}

.has-difference {
  color: #F56C6C;
  font-weight: bold;
}

.difference-amount {
  font-weight: bold;
}

.difference-amount.positive {
  color: #67C23A;
}

.difference-amount.negative {
  color: #F56C6C;
}

.action-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  justify-content: center;
}

.pagination-section {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

/* 详情对话框样式 */
.detail-dialog {
  max-width: 1200px;
}

.detail-content {
  max-height: 70vh;
  overflow-y: auto;
}

.detail-section {
  margin-bottom: 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.section-subtitle {
  font-size: 14px;
  color: #909399;
}

.section-actions {
  display: flex;
  gap: 8px;
}

.area-tag {
  margin-right: 4px;
  margin-bottom: 4px;
}

.task-progress {
  display: flex;
  align-items: center;
  gap: 8px;
}

.progress-text {
  min-width: 40px;
  text-align: right;
}

.difference-summary {
  padding: 16px;
  background-color: #f8f9fa;
  border-radius: 4px;
  border: 1px solid #ebeef5;
}

.summary-title {
  font-weight: bold;
  margin-bottom: 12px;
  color: #303133;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #eee;
}

.summary-item:last-child {
  border-bottom: none;
}

.reason {
  flex: 1;
  color: #606266;
}

.count {
  width: 80px;
  text-align: right;
  color: #303133;
  font-weight: 500;
}

.percentage {
  width: 60px;
  text-align: right;
  color: #909399;
  font-size: 14px;
}

.chart-title {
  font-weight: bold;
  margin-bottom: 12px;
  color: #303133;
}

.product-info {
  line-height: 1.4;
}

.product-name {
  font-weight: 500;
  color: #303133;
}

.product-sku {
  font-size: 12px;
  color: #909399;
}

.product-location {
  margin-top: 4px;
}

.text-success {
  color: #67C23A;
}

.text-danger {
  color: #F56C6C;
}

.text-warning {
  color: #E6A23C;
}

.text-info {
  color: #909399;
}

.text-muted {
  color: #909399;
}

.small {
  font-size: 12px;
}

.detail-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

.dialog-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.no-data {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 200px;
}

/* 响应式设计 */
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
  
  .detail-dialog {
    width: 95% !important;
  }
  
  .dialog-footer {
    flex-direction: column;
    gap: 12px;
  }
  
  .detail-section {
    margin-bottom: 12px;
  }
  
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}

/* 动画效果 */
.stock-take-table :deep(.el-table__row) {
  transition: all 0.3s;
}

.stock-take-table :deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}
</style>