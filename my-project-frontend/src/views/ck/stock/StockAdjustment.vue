<template>
  <div class="stock-adjustment-container">
    <!-- 页面标题和操作 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">
          <el-icon><Edit /></el-icon>
          {{ pageTitle }}
        </h1>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item to="/stock/take-list">库存管理</el-breadcrumb-item>
          <el-breadcrumb-item v-if="pageMode === 'view'">调整单详情</el-breadcrumb-item>
          <el-breadcrumb-item v-else-if="createType === 'manual'">调整单管理</el-breadcrumb-item>
          <el-breadcrumb-item v-else>盘点管理</el-breadcrumb-item>
          <el-breadcrumb-item>{{ createType === 'stock_take' ? '盘点差异调整' : '手动创建调整单' }}</el-breadcrumb-item>
          <el-breadcrumb-item v-if="createType === 'stock_take' && stockTakeInfo.stockTakeNo">
            {{ stockTakeInfo.stockTakeNo }}
          </el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-actions">
        <el-button type="info" @click="goBack">
          <el-icon><Back /></el-icon>
          {{ pageMode === 'view' ? '返回列表' : createType === 'stock_take' ? '返回列表' : '返回管理' }}
        </el-button>
        <el-button 
          type="primary" 
          @click="exportAdjustmentReport"
          :loading="exporting"
          v-if="createType === 'stock_take' && currentAdjustment?.id && pageMode !== 'view'"
        >
          <el-icon><Download /></el-icon>
          导出调整报告
        </el-button>
        
        <!-- 查看模式下的操作按钮 -->
        <template v-if="pageMode === 'view'">
          <el-button 
            type="primary" 
            @click="handleExecuteAdjustment"
            :loading="executing"
            v-if="currentAdjustment.adjustStatus === 2"
          >
            <el-icon><VideoPlay /></el-icon>
            执行调整
          </el-button>
          
          <el-button 
            type="warning" 
            @click="handleSubmitForReview"
            :loading="submitting"
            v-if="currentAdjustment.adjustStatus === 0"
          >
            <el-icon><Check /></el-icon>
            提交审核
          </el-button>
          
          <el-button 
            type="danger" 
            @click="handleReapplyAdjustment"
            :loading="reapplying"
            v-if="currentAdjustment.adjustStatus === 4"
          >
            <el-icon><Refresh /></el-icon>
            重新申请
          </el-button>
          
          <el-button 
            type="success" 
            @click="handlePrintAdjustment"
          >
            <el-icon><Printer /></el-icon>
            打印
          </el-button>
        </template>
      </div>
    </div>

    <!-- ========== 查看模式：调整单详情 ========== -->
    <div v-if="pageMode === 'view'" class="view-mode-container">
      <!-- 查看模式下的步骤条 -->
      <div class="process-steps view-mode-steps">
        <el-steps :active="currentStepView" finish-status="success" align-center>
          <el-step 
            title="创建调整单" 
            :description="getStepDescription(0)"
            :status="getStepStatus(0)"
          ></el-step>
          <el-step 
            title="审核调整" 
            :description="getStepDescription(1)"
            :status="getStepStatus(1)"
          ></el-step>
          <el-step 
            title="执行调整" 
            :description="getStepDescription(2)"
            :status="getStepStatus(2)"
          ></el-step>
          <el-step 
            title="调整完成" 
            :description="getStepDescription(3)"
            :status="getStepStatus(3)"
          ></el-step>
        </el-steps>
      </div>

      <!-- 调整单基本信息卡片 -->
      <el-card shadow="never" class="adjustment-basic-card">
        <template #header>
          <div class="card-header">
            <span class="card-title">调整单基本信息</span>
            <el-tag 
              :type="getStatusTagType(currentAdjustment.adjustStatus)"
              size="medium"
            >
              {{ getStatusLabel(currentAdjustment.adjustStatus) }}
            </el-tag>
          </div>
        </template>

        <div class="basic-info-grid">
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="调整单号">
              <span class="highlight-text">{{ currentAdjustment.adjustNo }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="调整类型">
              <el-tag :type="getAdjustTypeTagType(currentAdjustment.adjustType)">
                {{ getAdjustTypeLabel(currentAdjustment.adjustType) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="仓库">
              {{ currentAdjustment.warehouseName }}
            </el-descriptions-item>
            <el-descriptions-item label="来源单据">
              <div v-if="currentAdjustment.sourceType === 1">
                盘点单：{{ currentAdjustment.sourceNo }}
              </div>
              <div v-else-if="currentAdjustment.sourceType === 2">
                手动创建
              </div>
              <div v-else>
                {{ getSourceTypeLabel(currentAdjustment.sourceType) }}
              </div>
            </el-descriptions-item>
            <el-descriptions-item label="调整原因">
              {{ currentAdjustment.adjustReason }}
            </el-descriptions-item>
            <el-descriptions-item label="调整商品数">
              {{ currentAdjustment.totalItems }} 个
            </el-descriptions-item>
            <el-descriptions-item label="调整总量">
              <span :class="getQuantityClass(currentAdjustment.totalQuantity)">
                {{ formatNumber(currentAdjustment.totalQuantity) }}
              </span>
            </el-descriptions-item>
            <el-descriptions-item label="调整金额" v-if="currentAdjustment.totalAmount !== 0">
              <span :class="getAmountClass(currentAdjustment.totalAmount)">
                ¥{{ formatCurrency(currentAdjustment.totalAmount) }}
              </span>
            </el-descriptions-item>
            <el-descriptions-item label="期望完成时间">
              {{ formatDateTime(currentAdjustment.expectExecuteTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="实际执行时间">
              {{ formatDateTime(currentAdjustment.actualExecuteTime) || '--' }}
            </el-descriptions-item>
            <el-descriptions-item label="创建人">
              {{ currentAdjustment.createdByName }}
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">
              {{ formatDateTime(currentAdjustment.createdAt) }}
            </el-descriptions-item>
            <el-descriptions-item label="审核人">
              {{ currentAdjustment.approverName || '--' }}
            </el-descriptions-item>
            <el-descriptions-item label="审核时间">
              {{ formatDateTime(currentAdjustment.approveTime) || '--' }}
            </el-descriptions-item>
            <el-descriptions-item label="审核备注" :span="2" v-if="currentAdjustment.approveRemark">
              {{ currentAdjustment.approveRemark }}
            </el-descriptions-item>
            <el-descriptions-item label="备注" :span="2">
              {{ currentAdjustment.remark || '无' }}
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </el-card>

      <!-- 调整明细卡片 -->
      <el-card shadow="never" class="adjustment-items-card" style="margin-top: 20px;">
        <template #header>
          <div class="card-header">
            <span class="card-title">调整明细（共 {{ currentAdjustment.totalItems || 0 }} 项）</span>
            <div class="header-summary">
              <span class="summary-item">
                调整总量：<span :class="getQuantityClass(currentAdjustment.totalQuantity)">
                  {{ formatNumber(currentAdjustment.totalQuantity) }}
                </span>
              </span>
              <span class="summary-item" v-if="currentAdjustment.totalAmount !== 0">
                调整金额：<span :class="getAmountClass(currentAdjustment.totalAmount)">
                  ¥{{ formatCurrency(currentAdjustment.totalAmount) }}
                </span>
              </span>
            </div>
          </div>
        </template>

        <el-table
          :data="adjustmentDetailItems"
          border
          stripe
          size="small"
          class="adjustment-detail-table"
          empty-text="暂无明细数据"
        >
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column label="商品信息" min-width="250">
            <template #default="{ row }">
              <div class="product-info">
                <div class="product-sku">{{ row.skuCode || row.sku_code }}</div>
                <div class="product-name">{{ row.productName || row.product_name }}</div>
                <div v-if="row.specification" class="product-spec">
                  {{ row.specification }}
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="批次/货架" width="180">
            <template #default="{ row }">
              <div class="location-info">
                <div v-if="row.batchNo || row.batch_no" class="batch-info">
                  <el-tag size="mini">批次: {{ row.batchNo || row.batch_no }}</el-tag>
                </div>
                <div v-if="row.shelfCode || row.shelf_name" class="shelf-info">
                  <el-icon><Box /></el-icon>
                  货架: {{ row.shelfCode || row.shelf_name }}
                </div>
                <div v-if="row.locationCode || row.location_code" class="location-code">
                  库位: {{ row.locationCode || row.location_code }}
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="调整前" width="100" align="right">
            <template #default="{ row }">
              <span class="before-quantity">{{ formatNumber(row.beforeQuantity || row.before_quantity) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="调整数量" width="120" align="right">
            <template #default="{ row }">
              <span :class="getAdjustQuantityClass(row.adjustQuantity || row.adjust_quantity)">
                {{ (row.adjustQuantity || row.adjust_quantity) > 0 ? '+' : '' }}{{ formatNumber(row.adjustQuantity || row.adjust_quantity) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="调整后" width="100" align="right">
            <template #default="{ row }">
              <span class="after-quantity">{{ formatNumber(row.afterQuantity || row.after_quantity) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="单位成本" width="100" align="right" v-if="currentAdjustment.isAffectCost">
            <template #default="{ row }">
              <span v-if="row.unitCost || row.unit_cost">¥{{ formatCurrency(row.unitCost || row.unit_cost) }}</span>
              <span v-else>--</span>
            </template>
          </el-table-column>
          <el-table-column label="调整金额" width="120" align="right" v-if="currentAdjustment.isAffectCost">
            <template #default="{ row }">
              <span :class="getAmountClass(row.adjustAmount || row.adjust_amount)">
                ¥{{ formatCurrency(row.adjustAmount || row.adjust_amount) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="明细状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag 
                :type="(row.status || row.item_status) === 2 ? 'success' : 'info'"
                size="small"
              >
                {{ (row.status || row.item_status) === 2 ? '已执行' : '待执行' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="调整原因" width="150">
            <template #default="{ row }">
              {{ row.adjustReason || currentAdjustment.adjustReason }}
            </template>
          </el-table-column>
        </el-table>
        
        <!-- 统计信息 -->
        <div class="items-statistics" v-if="adjustmentDetailItems.length > 0">
          <div class="stat-item">
            <span class="stat-label">盘盈项数：</span>
            <span class="stat-value positive">{{ gainItemsCount }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">盘亏项数：</span>
            <span class="stat-value negative">{{ lossItemsCount }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">总调整量：</span>
            <span :class="getQuantityClass(totalAdjustQuantity)">
              {{ formatNumber(totalAdjustQuantity) }}
            </span>
          </div>
          <div class="stat-item" v-if="currentAdjustment.isAffectCost">
            <span class="stat-label">总调整金额：</span>
            <span :class="getAmountClass(totalAdjustAmount)">
              ¥{{ formatCurrency(totalAdjustAmount) }}
            </span>
          </div>
        </div>
      </el-card>
    </div>

    <!-- ========== 创建模式：原有的创建界面 ========== -->
    <div v-else>
      <!-- 盘点单概览（仅当基于盘点单创建时显示） -->
      <el-card shadow="never" class="overview-card" v-if="createType === 'stock_take' && stockTakeInfo.stockTakeNo">
        <template #header>
          <div class="card-header">
            <span class="card-title">盘点单概览</span>
            <el-tag 
              :type="getTakeStatusTagType(stockTakeInfo.takeStatus)"
              size="medium"
            >
              {{ getTakeStatusLabel(stockTakeInfo.takeStatus) }}
            </el-tag>
          </div>
        </template>

        <div class="overview-content">
          <el-row :gutter="24">
            <el-col :span="6">
              <div class="overview-item">
                <div class="overview-label">盘点单号</div>
                <div class="overview-value highlight">{{ stockTakeInfo.stockTakeNo }}</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="overview-item">
                <div class="overview-label">仓库</div>
                <div class="overview-value">{{ stockTakeInfo.warehouseName }}</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="overview-item">
                <div class="overview-label">盘点类型</div>
                <div class="overview-value">
                  <el-tag :type="stockTakeInfo.takeType === 1 ? 'primary' : 'warning'" size="small">
                    {{ stockTakeInfo.takeTypeName }}
                  </el-tag>
                </div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="overview-item">
                <div class="overview-label">盘点完成时间</div>
                <div class="overview-value">{{ formatDateTime(stockTakeInfo.actualEndTime) }}</div>
              </div>
            </el-col>
          </el-row>
          
          <!-- 保护期提示 -->
          <div v-if="stockTakeInfo.protectionEndTime" class="protection-notice">
            <el-alert 
              :title="getProtectionNoticeTitle" 
              :type="protectionStatus.type"
              :closable="false"
            >
              <div class="protection-details">
                <p>{{ protectionStatus.message }}</p>
                <p v-if="protectionStatus.timeLeft">剩余时间：{{ protectionStatus.timeLeft }}</p>
                <div v-if="protectionStatus.expired" class="expired-warning">
                  <el-icon><Warning /></el-icon>
                  保护期已过，建议尽快完成差异调整
                </div>
              </div>
            </el-alert>
          </div>
        </div>
      </el-card>

      <!-- 差异处理统计卡片（仅当基于盘点单创建时显示） -->
      <div class="stats-cards" v-if="createType === 'stock_take'">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-card shadow="hover" class="stat-card">
              <div class="stat-content">
                <el-icon class="stat-icon" color="#409EFF">
                  <TrendCharts />
                </el-icon>
                <div class="stat-info">
                  <div class="stat-number">{{ totalDiffItems }}</div>
                  <div class="stat-label">总差异项</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover" class="stat-card">
              <div class="stat-content">
                <el-icon class="stat-icon" color="#67C23A">
                  <Check />
                </el-icon>
                <div class="stat-info">
                  <div class="stat-number">{{ processedItems }}</div>
                  <div class="stat-label">已处理项</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover" class="stat-card">
              <div class="stat-content">
                <el-icon class="stat-icon" color="#E6A23C">
                  <Clock />
                </el-icon>
                <div class="stat-info">
                  <div class="stat-number">{{ pendingItems }}</div>
                  <div class="stat-label">待处理项</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover" class="stat-card">
              <div class="stat-content">
                <el-icon class="stat-icon" color="#909399">
                  <Document />
                </el-icon>
                <div class="stat-info">
                  <div class="stat-number">{{ adjustmentOrders.length }}</div>
                  <div class="stat-label">已创建调整单</div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 处理步骤指引 -->
      <div class="process-steps">
        <el-steps :active="currentStep" finish-status="success" align-center>
          <el-step 
            v-if="createType === 'stock_take'" 
            title="查看差异" 
            description="确认盘点产生的差异项"
          ></el-step>
          <el-step 
            title="创建调整单" 
            :description="createType === 'stock_take' ? '为差异项创建库存调整单' : '手动创建库存调整单'"
          ></el-step>
          <el-step title="审核调整" description="等待调整单审核通过"></el-step>
          <el-step title="执行调整" description="完成库存实际调整"></el-step>
          <el-step title="调整完成" description="所有差异处理完成"></el-step>
        </el-steps>
      </div>

      <!-- 主要操作区域 -->
      <div class="main-operation">
        <el-row :gutter="20">
          <!-- 左侧：差异项列表（仅当基于盘点单创建时显示） -->
          <el-col :span="16" v-if="createType === 'stock_take'">
            <el-card shadow="never" class="diff-list-card">
              <template #header>
                <div class="card-header">
                  <span class="card-title">差异项列表</span>
                  <div class="header-tabs">
                    <el-radio-group v-model="diffFilterStatus" size="small">
                      <el-radio-button label="all">全部 ({{ totalDiffItems }})</el-radio-button>
                      <el-radio-button label="pending">待处理 ({{ pendingItems }})</el-radio-button>
                      <el-radio-button label="processing">处理中 ({{ processingItems }})</el-radio-button>
                      <el-radio-button label="processed">已处理 ({{ processedItems }})</el-radio-button>
                    </el-radio-group>
                  </div>
                </div>
              </template>

              <!-- 批量操作 -->
              <div v-if="selectedDiffItems.length > 0" class="batch-actions-bar">
                <div class="batch-info">
                  已选择 <span class="selected-count">{{ selectedDiffItems.length }}</span> 项
                  <span class="selected-summary">
                    （盘盈: {{ selectedGainCount }} 项，盘亏: {{ selectedLossCount }} 项）
                  </span>
                </div>
                <div class="batch-buttons">
                  <el-button 
                    type="primary" 
                    size="small"
                    @click="handleCreateAdjustmentForSelected"
                    :loading="creatingAdjustment"
                  >
                    <el-icon><DocumentAdd /></el-icon>
                    为选中项创建调整单
                  </el-button>
                  <el-button 
                    type="success" 
                    size="small"
                    @click="handleBatchProcessAll"
                    :disabled="availableDiffItems.length === 0"
                  >
                    <el-icon><Check /></el-icon>
                    处理所有剩余项
                  </el-button>
                  <el-button @click="clearSelection" size="small">
                    清空选择
                  </el-button>
                </div>
              </div>

              <!-- 差异项表格 -->
              <el-table
                :data="filteredDiffItems"
                v-loading="loadingDiffItems"
                @selection-change="handleDiffSelectionChange"
                :row-class-name="diffTableRowClassName"
                border
                stripe
                height="500"
                class="diff-items-table"
              >
                <el-table-column type="selection" width="55" align="center" 
                  :selectable="isDiffItemSelectable" />
                
                <el-table-column label="处理状态" width="120" align="center">
                  <template #default="{ row }">
                    <el-tag 
                      :type="getAdjustStatusTagType(row.adjust_status)"
                      size="small"
                    >
                      {{ getAdjustStatusLabel(row.adjust_status) }}
                    </el-tag>
                  </template>
                </el-table-column>
                
                <el-table-column label="商品信息" min-width="200">
                  <template #default="{ row }">
                    <div class="product-info">
                      <div class="product-sku">{{ row.sku_code }}</div>
                      <div class="product-name">{{ row.product_name }}</div>
                      <div v-if="row.specification" class="product-spec">
                        {{ row.specification }}
                      </div>
                    </div>
                  </template>
                </el-table-column>
                
                <el-table-column label="批次/货位" width="180" align="center">
                  <template #default="{ row }">
                    <div class="location-info">
                      <div v-if="row.batch_no">
                        <el-tag size="small">批次: {{ row.batch_no }}</el-tag>
                      </div>
                      <div v-if="row.shelf_id" class="shelf-info">
                        <el-icon><Box /></el-icon>
                        货架: {{ row.shelf_name || row.shelf_id }}
                      </div>
                      <div v-if="row.location_code" class="location-code">
                        库位: {{ row.location_code }}
                      </div>
                    </div>
                  </template>
                </el-table-column>
                
               <el-table-column label="系统库存" width="120" align="right">
                <template #default="{ row }">
                  <span class="system-quantity">
                    {{ formatNumber(row.system_quantity) }}
                  </span>
                </template>
              </el-table-column>

              <el-table-column label="实盘库存" width="120" align="right">
                <template #default="{ row }">
                  <span class="counted-quantity">
                    {{ formatNumber(row.counted_quantity) }}
                  </span>
                </template>
              </el-table-column>

              <el-table-column label="差异数量" width="120" align="right">
                <template #default="{ row }">
                  <span :class="getDiffQuantityClass(row.diff_quantity)">
                    {{ row.diff_quantity > 0 ? '+' : '' }}{{ formatNumber(row.diff_quantity) }}
                  </span>
                </template>
              </el-table-column>

              <el-table-column label="调整类型" width="100" align="center">
                <template #default="{ row }">
                  <el-tag 
                    :type="row.diff_quantity > 0 ? 'success' : 'danger'"
                    size="small"
                  >
                    {{ row.diff_quantity > 0 ? '盘盈' : '盘亏' }}
                  </el-tag>
                </template>
              </el-table-column>

              <el-table-column label="关联调整单" width="180">
                <template #default="{ row }">
                  <div v-if="row.adjustment_no" class="adjustment-link">
                    <el-tooltip :content="`点击查看调整单详情`">
                      <el-link 
                        type="primary" 
                        :underline="false"
                        @click="viewAdjustmentDetail(row.adjustment_id)"
                      >
                        {{ formatAdjustmentNo(row.adjustment_no) }}
                      </el-link>
                    </el-tooltip>
                    <div class="adjustment-status">
                      <el-tag 
                        v-if="row.adjust_status === 1" 
                        type="warning" 
                        size="mini"
                      >
                        审核中
                      </el-tag>
                      <el-tag 
                        v-else-if="row.adjust_status === 2" 
                        type="success" 
                        size="mini"
                      >
                        已调整
                      </el-tag>
                    </div>
                  </div>
                  <span v-else class="no-adjustment">--</span>
                </template>
              </el-table-column>

              <el-table-column label="操作" width="150" fixed="right" align="center">
                <template #default="{ row }">
                  <div class="item-actions">
                    <template v-if="row.adjust_status === 0">
                      <el-button 
                        type="primary" 
                        size="small"
                        @click="handleCreateSingleAdjustment(row)"
                      >
                        单独创建调整单
                      </el-button>
                    </template>
                    <template v-else-if="row.adjust_status === 1">
                      <el-button 
                        type="text" 
                        size="small"
                        @click="viewAdjustmentDetail(row.adjustment_id)"
                      >
                        查看调整单
                      </el-button>
                    </template>
                    <template v-else>
                      <el-button 
                        type="text" 
                        size="small"
                        disabled
                      >
                        <el-icon><Check /></el-icon>
                        已处理
                      </el-button>
                    </template>
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
            </el-card>
          </el-col>

          <!-- 左侧：手动创建调整单表单（仅当手动创建时显示） -->
          <el-col :span="16" v-else>
            <el-card shadow="never" class="manual-create-card">
              <template #header>
                <div class="card-header">
                  <span class="card-title">手动创建调整单</span>
                </div>
              </template>

              <div class="manual-create-form">
                <el-form 
                  :model="manualAdjustmentForm" 
                  ref="manualAdjustmentFormRef"
                  label-width="120px"
                  size="medium"
                >
                  <!-- 仓库选择 -->
                  <el-form-item label="选择仓库" required prop="warehouseId">
                    <el-select
                      v-model="manualAdjustmentForm.warehouseId"
                      placeholder="请选择仓库"
                      style="width: 100%"
                      @change="handleWarehouseChange"
                    >
                      <el-option
                        v-for="warehouse in warehouseList"
                        :key="warehouse.id"
                        :label="warehouse.name"
                        :value="warehouse.id"
                      />
                    </el-select>
                  </el-form-item>
                  
                  <!-- 调整单类型 -->
                  <el-form-item label="调整单类型" required prop="adjustType">
                    <el-select
                      v-model="manualAdjustmentForm.adjustType"
                      placeholder="请选择调整单类型"
                      style="width: 100%"
                      @change="handleAdjustTypeChange"
                    >
                      <el-option :label="getAdjustTypeLabel(1)" :value="1" />
                      <el-option :label="getAdjustTypeLabel(2)" :value="2" />
                      <el-option :label="getAdjustTypeLabel(3)" :value="3" />
                      <el-option :label="getAdjustTypeLabel(4)" :value="4" />
                      <el-option :label="getAdjustTypeLabel(5)" :value="5" />
                      <el-option :label="getAdjustTypeLabel(6)" :value="6" />
                    </el-select>
                  </el-form-item>
                  
                  <!-- 盘点单选择（仅当调整类型为盘点调整时显示） -->
                  <el-form-item 
                    label="选择盘点单" 
                    prop="stockTakeId"
                    v-if="manualAdjustmentForm.adjustType === 1"
                  >
                    <el-select
                      v-model="manualAdjustmentForm.stockTakeId"
                      placeholder="请选择已完成盘点任务"
                      style="width: 100%"
                      @change="handleStockTakeSelect"
                      :loading="loadingStockTakeList"
                      clearable
                    >
                      <el-option
                        v-for="stockTake in completedStockTakeList"
                        :key="stockTake.id"
                        :label="`${stockTake.stockTakeNo} - ${stockTake.warehouseName}`"
                        :value="stockTake.id"
                      >
                        <div class="stock-take-option">
                          <div class="option-no">{{ stockTake.stockTakeNo }}</div>
                          <div class="option-info">
                            <span class="warehouse">{{ stockTake.warehouseName }}</span>
                            <span class="time">{{ formatDateTime(stockTake.actualEndTime) }}</span>
                          </div>
                        </div>
                      </el-option>
                    </el-select>
                  </el-form-item>
                  
                  <!-- 调整原因 -->
                  <el-form-item label="调整原因" required prop="adjustReason">
                    <el-select
                      v-model="manualAdjustmentForm.adjustReason"
                      placeholder="请选择调整原因"
                      style="width: 100%"
                    >
                      <el-option label="盘点差异" value="stock_take_diff" />
                      <el-option label="商品自然损耗" value="natural_loss" />
                      <el-option label="商品破损报废" value="damage_scrap" />
                      <el-option label="系统数据错误" value="system_error" />
                      <el-option label="操作失误" value="operation_mistake" />
                      <el-option label="其他原因" value="other" />
                    </el-select>
                  </el-form-item>
                  
                  <!-- 是否紧急 -->
                  <el-form-item label="是否紧急" prop="isUrgent">
                    <el-switch
                      v-model="manualAdjustmentForm.isUrgent"
                      active-text="紧急"
                      inactive-text="普通"
                    />
                  </el-form-item>
                  
                  <!-- 影响成本 -->
                  <el-form-item label="影响成本" prop="isAffectCost">
                    <el-switch
                      v-model="manualAdjustmentForm.isAffectCost"
                      active-text="是"
                      inactive-text="否"
                    />
                  </el-form-item>
                  
                  <!-- 期望完成时间 -->
                  <el-form-item label="期望完成时间" required prop="expectedCompleteTime">
                    <el-date-picker
                      v-model="manualAdjustmentForm.expectedCompleteTime"
                      type="datetime"
                      placeholder="选择期望完成时间"
                      style="width: 100%"
                      format="YYYY-MM-DD HH:mm"
                      value-format="YYYY-MM-DD HH:mm:ss"
                      :disabled-date="disabledPastDate"
                    />
                  </el-form-item>
                  
                  <!-- 审核人 -->
                  <el-form-item label="审核人" prop="reviewerId">
                    <el-select
                      v-model="manualAdjustmentForm.reviewerId"
                      placeholder="请选择审核人（可选）"
                      filterable
                      clearable
                      style="width: 100%"
                    >
                      <el-option
                        v-for="user in reviewerList"
                        :key="user.id"
                        :label="`${user.name} (${user.department})`"
                        :value="user.id"
                      />
                    </el-select>
                  </el-form-item>
                  
                  <!-- 调整说明 -->
                  <el-form-item label="调整说明" prop="remark">
                    <el-input
                      v-model="manualAdjustmentForm.remark"
                      type="textarea"
                      :rows="4"
                      placeholder="请输入调整说明"
                      maxlength="500"
                      show-word-limit
                    />
                  </el-form-item>
                </el-form>
              </div>
            </el-card>

            <!-- 调整明细列表 -->
            <el-card shadow="never" class="manual-items-card" style="margin-top: 20px;">
              <template #header>
                <div class="card-header">
                  <span class="card-title">调整明细</span>
                  <div class="header-actions">
                    <el-button 
                      v-if="manualAdjustmentForm.adjustType !== 1"
                      type="primary" 
                      size="small"
                      @click="openManualProductDialog"
                      :disabled="!manualAdjustmentForm.warehouseId"
                    >
                      <el-icon><Plus /></el-icon>
                      添加商品
                    </el-button>
                    <el-button 
                      v-if="manualAdjustmentForm.adjustType === 1"
                      type="success" 
                      size="small"
                      @click="loadStockTakeItems"
                      :disabled="!manualAdjustmentForm.stockTakeId"
                      :loading="loadingStockTakeItems"
                    >
                      <el-icon><Refresh /></el-icon>
                      加载盘点明细
                    </el-button>
                  </div>
                </div>
              </template>

              <div v-if="manualAdjustmentItems.length === 0" class="no-items">
                <el-empty :description="manualAdjustmentForm.adjustType === 1 ? '请选择盘点单并加载明细' : '请添加调整商品明细'">
                  <el-button 
                    v-if="manualAdjustmentForm.adjustType !== 1"
                    type="primary" 
                    @click="openManualProductDialog"
                    :disabled="!manualAdjustmentForm.warehouseId"
                  >
                    添加商品
                  </el-button>
                  <el-button 
                    v-if="manualAdjustmentForm.adjustType === 1"
                    type="success" 
                    @click="loadStockTakeItems"
                    :disabled="!manualAdjustmentForm.stockTakeId"
                    :loading="loadingStockTakeItems"
                  >
                    加载盘点明细
                  </el-button>
                </el-empty>
              </div>

              <div v-else class="manual-items-list">
                <el-table
                  :data="manualAdjustmentItems"
                  border
                  stripe
                  size="small"
                  class="manual-items-table"
                >
                  <el-table-column type="index" label="序号" width="60" align="center" />
                  <el-table-column label="商品信息" min-width="250">
                    <template #default="{ row }">
                      <div class="product-info">
                        <div class="product-sku">{{ row.skuCode }}</div>
                        <div class="product-name">{{ row.productName }}</div>
                        <div v-if="row.specification" class="product-spec">
                          {{ row.specification }}
                        </div>
                        <div v-if="row.color" class="product-color">
                          颜色: {{ row.color }}
                        </div>
                      </div>
                    </template>
                  </el-table-column>
                  <el-table-column label="批次号" width="120">
                    <template #default="{ row }">
                      <div v-if="row.batchNo">{{ row.batchNo }}</div>
                      <span v-else class="no-batch">--</span>
                    </template>
                  </el-table-column>
                  <el-table-column label="货架" width="120">
                    <template #default="{ row }">
                      <div v-if="row.shelfName">{{ row.shelfName }}</div>
                      <span v-else class="no-shelf">--</span>
                    </template>
                  </el-table-column>
                  <el-table-column label="系统库存" width="120" align="right">
                    <template #default="{ row }">
                      <span class="before-quantity">{{ formatNumber(row.beforeQuantity) }}</span>
                    </template>
                  </el-table-column>
                  <el-table-column label="实盘数量" width="120" align="right">
                    <template #default="{ row }">
                      <span class="counted-quantity">{{ formatNumber(row.countedQuantity) }}</span>
                    </template>
                  </el-table-column>
                  <el-table-column label="差异数量" width="120" align="right">
                    <template #default="{ row }">
                      <span :class="getDiffQuantityClass(row.diffQuantity)">
                        {{ row.diffQuantity > 0 ? '+' : '' }}{{ formatNumber(row.diffQuantity) }}
                      </span>
                    </template>
                  </el-table-column>
                  <el-table-column label="调整数量" width="150" align="center" v-if="manualAdjustmentForm.adjustType !== 1">
                    <template #default="{ row }">
                      <div class="adjust-quantity-cell">
                        <el-input-number
                          v-model="row.adjustQuantity"
                          :min="-999999"
                          :precision="4"
                          size="small"
                          controls-position="right"
                          style="width: 120px"
                          @change="handleManualAdjustQuantityChange(row)"
                        />
                        <div class="quantity-tips">
                          <span v-if="row.adjustQuantity > 0" class="positive">增加</span>
                          <span v-if="row.adjustQuantity < 0" class="negative">减少</span>
                          <span v-if="row.adjustQuantity === 0" class="zero">不变</span>
                        </div>
                      </div>
                    </template>
                  </el-table-column>
                  <el-table-column label="调整数量" width="150" align="center" v-else>
                    <template #default="{ row }">
                      <span :class="getDiffQuantityClass(row.diffQuantity)">
                        {{ row.diffQuantity > 0 ? '+' : '' }}{{ formatNumber(row.diffQuantity) }}
                      </span>
                      <div class="quantity-tips">
                        <span v-if="row.diffQuantity > 0" class="positive">盘盈</span>
                        <span v-if="row.diffQuantity < 0" class="negative">盘亏</span>
                        <span v-if="row.diffQuantity === 0" class="zero">无差异</span>
                      </div>
                    </template>
                  </el-table-column>
                  <el-table-column label="调整后数量" width="120" align="right">
                    <template #default="{ row }">
                      <span :class="getAfterQuantityClass(row.afterQuantity)">
                        {{ formatNumber(row.afterQuantity) }}
                      </span>
                    </template>
                  </el-table-column>
                  <el-table-column label="单位成本" width="120" align="right" v-if="manualAdjustmentForm.isAffectCost">
                    <template #default="{ row }">
                      <el-input-number
                        v-model="row.unitCost"
                        :min="0"
                        :precision="2"
                        size="small"
                        controls-position="right"
                        style="width: 100px"
                        @change="handleManualCostChange(row)"
                      />
                    </template>
                  </el-table-column>
                  <el-table-column label="调整原因" width="150">
                    <template #default="{ row }">
                      <el-input
                        v-model="row.itemReason"
                        placeholder="明细原因"
                        size="small"
                      />
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="80" align="center" fixed="right" v-if="manualAdjustmentForm.adjustType !== 1">
                    <template #default="{ row }">
                      <el-button
                        type="danger"
                        link
                        size="small"
                        @click="removeManualItem(row)"
                      >
                        删除
                      </el-button>
                    </template>
                  </el-table-column>
                </el-table>

                <!-- 统计信息 -->
                <div class="items-statistics">
                  <div class="stat-item">
                    <span class="stat-label">商品总数：</span>
                    <span class="stat-value">{{ manualAdjustmentItems.length }}</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-label">差异项数：</span>
                    <span class="stat-value">{{ totalManualDiffItems }}</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-label">盘盈项数：</span>
                    <span class="stat-value positive">{{ totalManualGainItems }}</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-label">盘亏项数：</span>
                    <span class="stat-value negative">{{ totalManualLossItems }}</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-label">调整总量：</span>
                    <span :class="getQuantityClass(totalManualAdjustQuantity)">
                      {{ formatNumber(totalManualAdjustQuantity) }}
                    </span>
                  </div>
                  <div class="stat-item" v-if="manualAdjustmentForm.isAffectCost">
                    <span class="stat-label">调整金额：</span>
                    <span :class="getAmountClass(totalManualAdjustAmount)">
                      ¥{{ formatCurrency(totalManualAdjustAmount) }}
                    </span>
                  </div>
                </div>
              </div>
            </el-card>
          </el-col>

          <!-- 右侧：调整单操作面板 -->
          <el-col :span="8">
            <!-- 创建调整单面板（基于盘点单） -->
            <el-card shadow="never" class="adjustment-create-card" v-if="createType === 'stock_take'">
              <template #header>
                <div class="card-header">
                  <span class="card-title">创建调整单</span>
                  <el-tag v-if="selectedDiffItems.length > 0" type="primary">
                    已选 {{ selectedDiffItems.length }} 项
                  </el-tag>
                </div>
              </template>

              <div v-if="selectedDiffItems.length > 0" class="create-form">
                <el-form 
                  :model="adjustmentForm" 
                  ref="adjustmentFormRef"
                  label-width="100px"
                  size="small"
                >
                  <el-form-item label="调整单类型" required>
                    <el-select
                      v-model="adjustmentForm.adjust_type"
                      placeholder="请选择调整类型"
                      style="width: 100%"
                    >
                      <el-option :label="getAdjustTypeLabel(1)" :value="1" />
                      <el-option :label="getAdjustTypeLabel(2)" :value="2" />
                      <el-option :label="getAdjustTypeLabel(3)" :value="3" />
                    </el-select>
                  </el-form-item>
                  
                  <el-form-item label="调整原因" required>
                    <el-select
                      v-model="adjustmentForm.adjust_reason"
                      placeholder="请选择调整原因"
                      style="width: 100%"
                    >
                      <el-option label="盘点差异" value="stock_take_diff" />
                      <el-option label="商品自然损耗" value="natural_loss" />
                      <el-option label="商品破损报废" value="damage_scrap" />
                      <el-option label="系统数据错误" value="system_error" />
                      <el-option label="操作失误" value="operation_mistake" />
                      <el-option label="其他原因" value="other" />
                    </el-select>
                  </el-form-item>
                  
                  <el-form-item label="期望完成时间" required>
                    <el-date-picker
                      v-model="adjustmentForm.expected_complete_time"
                      type="datetime"
                      placeholder="选择期望完成时间"
                      style="width: 100%"
                      format="YYYY-MM-DD HH:mm"
                      value-format="YYYY-MM-DD HH:mm:ss"
                      :disabled-date="disabledPastDate"
                    />
                  </el-form-item>
                  
                  <el-form-item label="审核人">
                    <el-select
                      v-model="adjustmentForm.reviewer_id"
                      placeholder="请选择审核人（可选）"
                      filterable
                      clearable
                      style="width: 100%"
                    >
                      <el-option
                        v-for="user in reviewerList"
                        :key="user.id"
                        :label="`${user.name} (${user.department})`"
                        :value="user.id"
                      />
                    </el-select>
                  </el-form-item>
                  
                  <el-form-item label="调整说明">
                    <el-input
                      v-model="adjustmentForm.remark"
                      type="textarea"
                      :rows="3"
                      placeholder="请输入调整说明"
                      maxlength="500"
                      show-word-limit
                    />
                  </el-form-item>
                  
                  <el-form-item>
                    <el-button 
                      type="primary" 
                      @click="handleCreateAdjustment"
                      :loading="creatingAdjustment"
                      style="width: 100%"
                    >
                      <el-icon><DocumentAdd /></el-icon>
                      创建库存调整单
                    </el-button>
                  </el-form-item>
                </el-form>
              </div>
              
              <div v-else class="no-selection">
                <el-empty description="请从左侧表格中选择需要调整的差异项">
                  <el-button type="primary" @click="selectAllPendingItems">
                    选择所有待处理项
                  </el-button>
                </el-empty>
              </div>
            </el-card>

            <!-- 创建调整单面板（手动创建） -->
            <el-card shadow="never" class="adjustment-create-card" v-else>
              <template #header>
                <div class="card-header">
                  <span class="card-title">创建调整单</span>
                  <el-tag v-if="manualAdjustmentItems.length > 0" type="primary">
                    已选 {{ manualAdjustmentItems.length }} 项
                  </el-tag>
              </div>
              </template>

              <div class="create-form">
                <el-form 
                  :model="manualAdjustmentForm" 
                  ref="manualAdjustmentFormRef"
                  label-width="100px"
                  size="small"
                >
                  <el-form-item>
                    <el-button 
                      type="primary" 
                      @click="handleCreateManualAdjustment"
                      :loading="creatingManualAdjustment"
                      :disabled="manualAdjustmentItems.length === 0"
                      style="width: 100%"
                    >
                      <el-icon><DocumentAdd /></el-icon>
                      创建库存调整单
                    </el-button>
                  </el-form-item>
                  
                  <div class="form-tips">
                    <el-alert type="info" :closable="false">
                      <p>请确保：</p>
                      <p v-if="manualAdjustmentForm.adjustType === 1">1. 已选择仓库和盘点单</p>
                      <p v-else>1. 已选择仓库</p>
                      <p v-if="manualAdjustmentForm.adjustType === 1">2. 已加载盘点明细</p>
                      <p v-else>2. 已添加调整商品明细</p>
                      <p>3. 调整数量已正确填写</p>
                    </el-alert>
                  </div>
                </el-form>
              </div>
            </el-card>

            <!-- 调整单历史 -->
            <el-card shadow="never" class="adjustment-history-card" style="margin-top: 20px;">
              <template #header>
                <div class="card-header">
                  <span class="card-title">已创建的调整单</span>
                  <el-button 
                    type="text" 
                    size="small"
                    @click="refreshAdjustmentOrders"
                  >
                    <el-icon><Refresh /></el-icon>
                    刷新
                  </el-button>
                </div>
              </template>

              <div v-if="adjustmentOrders.length > 0" class="history-list">
                <div 
                  v-for="order in adjustmentOrders" 
                  :key="order.id"
                  class="order-item"
                  :class="{ 'active': activeOrderId === order.id }"
                  @click="viewAdjustmentDetail(order.id)"
                >
                  <div class="order-header">
                    <span class="order-no">{{ formatAdjustmentNo(order.adjustment_no) }}</span>
                    <el-tag 
                      :type="getOrderStatusTagType(order.adjust_status)"
                      size="small"
                    >
                      {{ getOrderStatusLabel(order.adjust_status) }}
                    </el-tag>
                  </div>
                  <div class="order-info">
                    <div class="info-item">
                      <span class="info-label">调整项数：</span>
                      <span class="info-value">{{ order.total_items }}</span>
                    </div>
                    <div class="info-item">
                      <span class="info-label">创建时间：</span>
                      <span class="info-value">{{ formatDateTime(order.created_at) }}</span>
                    </div>
                    <div class="info-item">
                      <span class="info-label">创建人：</span>
                      <span class="info-value">{{ order.creator_name }}</span>
                    </div>
                  </div>
                </div>
              </div>
              
              <div v-else class="no-history">
                <el-empty description="暂无调整单记录" />
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </div>

    <!-- 商品选择对话框（手动创建时使用，仅当非盘点调整时） -->
    <el-dialog
      v-model="manualProductDialogVisible"
      title="选择商品"
      width="1000px"
      top="5vh"
      :close-on-click-modal="false"
    >
      <div class="product-select-dialog">
        <!-- 商品筛选 -->
        <div class="product-filter">
          <el-form :model="manualProductFilter" inline>
            <el-form-item>
              <el-input
                v-model="manualProductFilter.keyword"
                placeholder="搜索商品名称、SKU、编码"
                clearable
                style="width: 300px"
                @keyup.enter="loadManualProductList"
              >
                <template #append>
                  <el-button @click="loadManualProductList">
                    <el-icon><Search /></el-icon>
                  </el-button>
                </template>
              </el-input>
            </el-form-item>
          </el-form>
        </div>
        
        <!-- 商品列表 -->
        <div class="product-list-container">
          <el-table
            ref="manualProductTableRef"
            :data="manualProductList"
            v-loading="manualProductLoading"
            @selection-change="handleManualProductSelectionChange"
            empty-text="暂无商品数据"
            border
            stripe
            height="400"
          >
            <el-table-column type="selection" width="55" />
            <el-table-column label="商品编码" width="120">
              <template #default="{ row }">
                {{ row.productCode }}
              </template>
            </el-table-column>
            <el-table-column label="SKU编码" width="120">
              <template #default="{ row }">
                {{ row.skuCode }}
              </template>
            </el-table-column>
            <el-table-column label="商品名称" width="200">
              <template #default="{ row }">
                {{ row.productName }}
              </template>
            </el-table-column>
            <el-table-column label="规格" width="150">
              <template #default="{ row }">
                {{ row.specification }}
              </template>
            </el-table-column>
            <el-table-column label="单位" width="80" align="center">
              <template #default="{ row }">
                {{ row.unit }}
              </template>
            </el-table-column>
            <el-table-column label="当前库存" width="100" align="right">
              <template #default="{ row }">
                {{ formatNumber(row.currentQuantity) }}
              </template>
            </el-table-column>
            <el-table-column label="货架" width="120">
              <template #default="{ row }">
                {{ row.shelfName || '--' }}
              </template>
            </el-table-column>
            <el-table-column label="库位" width="120">
              <template #default="{ row }">
                {{ row.locationCode || '--' }}
              </template>
            </el-table-column>
          </el-table>
        </div>
        
        <!-- 分页 -->
        <div class="product-pagination">
          <el-pagination
            v-model:current-page="manualProductPagination.current"
            v-model:page-size="manualProductPagination.size"
            :total="manualProductPagination.total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next"
            @size-change="handleManualProductSizeChange"
            @current-change="handleManualProductCurrentChange"
          />
        </div>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="manualProductDialogVisible = false">取消</el-button>
          <el-button 
            type="primary" 
            @click="handleConfirmManualProducts"
            :disabled="selectedManualProducts.length === 0"
          >
            确认选择（{{ selectedManualProducts.length }}个商品）
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 批量处理确认对话框（仅当基于盘点单创建时显示） -->
    <el-dialog
      v-model="batchConfirmDialogVisible"
      title="批量创建调整单确认"
      width="500px"
      :close-on-click-modal="false"
    >
      <div class="batch-confirm-content">
        <el-alert type="info" :closable="false">
          <p>确定要为所有剩余差异项创建调整单吗？</p>
          <p>本次将创建 {{ availableDiffItems.length }} 个调整项</p>
        </el-alert>
        
        <div class="confirm-stats">
          <div class="stat-row">
            <span class="stat-label">盘盈项数：</span>
            <span class="stat-value positive">{{ gainItemsCount }}</span>
          </div>
          <div class="stat-row">
            <span class="stat-label">盘亏项数：</span>
            <span class="stat-value negative">{{ lossItemsCount }}</span>
          </div>
          <div class="stat-row">
            <span class="stat-label">总差异数量：</span>
            <span class="stat-value" :class="getTotalDiffClass">
              {{ formatNumber(totalDiffQuantity) }}
            </span>
          </div>
        </div>
        
        <el-form 
          :model="batchAdjustmentForm" 
          ref="batchAdjustmentFormRef"
          label-width="100px"
          style="margin-top: 20px;"
        >
          <el-form-item label="调整原因" required>
            <el-select
              v-model="batchAdjustmentForm.adjust_reason"
              placeholder="请选择调整原因"
              style="width: 100%"
            >
              <el-option label="盘点差异" value="stock_take_diff" />
              <el-option label="批量调整" value="batch_adjustment" />
              <el-option label="其他原因" value="other" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="调整说明">
            <el-input
              v-model="batchAdjustmentForm.remark"
              type="textarea"
              :rows="2"
              placeholder="请输入调整说明"
            />
          </el-form-item>
        </el-form>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="batchConfirmDialogVisible = false">取消</el-button>
          <el-button 
            type="primary" 
            @click="handleConfirmBatchProcess"
            :loading="creatingBatchAdjustment"
          >
            确认创建调整单
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch, nextTick } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  Edit, Back, Download, TrendCharts, Check, Clock, Document,
  DocumentAdd, Refresh, Warning, Box, Close, VideoPlay, Printer,
  Plus, Search
} from '@element-plus/icons-vue';
import { post, get } from '@/net';

const router = useRouter();
const route = useRoute();

// 页面模式：'view' 查看模式，'create' 创建模式
const pageMode = ref('create'); 
// 创建类型：'stock_take'（基于盘点单）或 'manual'（手动创建）
const createType = ref('stock_take');
// 查看模式的调整单ID
const viewAdjustmentId = ref(null);

// 状态定义
const loadingDiffItems = ref(false);
const creatingAdjustment = ref(false);
const creatingManualAdjustment = ref(false);
const creatingBatchAdjustment = ref(false);
const exporting = ref(false);
const batchConfirmDialogVisible = ref(false);
const reviewing = ref(false);
const executing = ref(false);
const reapplying = ref(false);
const submitting = ref(false);
const manualProductDialogVisible = ref(false);
const manualProductLoading = ref(false);
const loadingStockTakeList = ref(false);
const loadingStockTakeItems = ref(false);

// 查看模式的当前调整单数据
const currentAdjustment = reactive({
  id: '',
  adjustNo: '',
  tenantId: '',
  adjustType: 1,
  sourceType: 1,
  sourceId: '',
  sourceNo: '',
  warehouseId: '',
  warehouseName: '',
  adjustReason: '',
  adjustStatus: 1,
  totalItems: 0,
  totalQuantity: 0,
  totalCostAmount: 0,
  totalAmount: 0,
  isAffectCost: false,
  isUrgent: false,
  priority: 3,
  expectExecuteTime: '',
  actualExecuteTime: '',
  remark: '',
  attachmentUrls: '',
  extData: '',
  approverId: '',
  approverName: '',
  approveTime: '',
  approveRemark: '',
  createdBy: '',
  createdByName: '',
  createdAt: '',
  modifiedBy: '',
  modifiedByName: '',
  modifiedAt: ''
});

// 查看模式的调整单明细
const adjustmentDetailItems = ref([]);

// 创建模式的数据（保持不变）
const stockTakeInfo = reactive({
  id: '',
  stockTakeNo: '',
  warehouseId: '',
  warehouseName: '',
  takeType: 1,
  takeTypeName: '',
  takeStatus: 4,
  actualEndTime: '',
  protectionEndTime: '',
  totalItems: 0,
  countedItems: 0,
  diffItems: 0
});

const diffItems = ref([]);
const selectedDiffItems = ref([]);
const diffFilterStatus = ref('pending');

const pagination = reactive({
  current: 1,
  size: 20,
  total: 0
});

const adjustmentForm = reactive({
  adjust_type: 3,
  adjust_reason: 'stock_take_diff',
  expected_complete_time: null,
  reviewer_id: null,
  remark: ''
});

const manualAdjustmentForm = reactive({
  warehouseId: '',
  warehouseName: '',
  adjustType: 3,
  adjustReason: 'other',
  stockTakeId: null,
  stockTakeNo: '',
  isUrgent: false,
  isAffectCost: false,
  expectedCompleteTime: null,
  reviewerId: null,
  remark: ''
});

const batchAdjustmentForm = reactive({
  adjust_reason: 'stock_take_diff',
  remark: '批量处理所有剩余差异项'
});

const manualAdjustmentItems = ref([]);
const manualProductList = ref([]);
const selectedManualProducts = ref([]);
const manualProductFilter = reactive({
  keyword: '',
  warehouseId: ''
});
const manualProductPagination = reactive({
  current: 1,
  size: 10,
  total: 0
});

const adjustmentOrders = ref([]);
const activeOrderId = ref(null);

const warehouseList = ref([]);
const completedStockTakeList = ref([]);
const shelfOptions = ref([]);
const reviewerList = ref([]);

const adjustmentFormRef = ref();
const manualAdjustmentFormRef = ref();
const batchAdjustmentFormRef = ref();
const manualProductTableRef = ref();

// 计算属性
const pageTitle = computed(() => {
  if (pageMode.value === 'view') {
    return `调整单详情 - ${currentAdjustment.adjustNo || ''}`;
  }
  return createType.value === 'stock_take' ? '盘点差异调整处理' : '手动创建调整单';
});

// 查看模式的计算属性
const totalAdjustQuantity = computed(() => {
  return adjustmentDetailItems.value.reduce((sum, item) => {
    return sum + Math.abs(item.adjustQuantity || item.adjust_quantity || 0);
  }, 0);
});

const totalAdjustAmount = computed(() => {
  return adjustmentDetailItems.value.reduce((sum, item) => {
    return sum + (item.adjustAmount || item.adjust_amount || 0);
  }, 0);
});

const gainItemsCount = computed(() => {
  return adjustmentDetailItems.value.filter(item => {
    const quantity = item.adjustQuantity || item.adjust_quantity;
    return quantity > 0;
  }).length;
});

const lossItemsCount = computed(() => {
  return adjustmentDetailItems.value.filter(item => {
    const quantity = item.adjustQuantity || item.adjust_quantity;
    return quantity < 0;
  }).length;
});

// 查看模式的步骤状态计算
const currentStepView = computed(() => {
  const status = currentAdjustment.adjustStatus;
  
  // 状态映射到步骤索引（0-3）
  if (status === 0) return 0; // 待提交 -> 步骤1
  if (status === 1 || status === 2 || status === 4) return 1; // 待审核/审核通过/审核拒绝 -> 步骤2
  if (status === 11) return 2; // 开始执行 -> 步骤3
  if (status === 20 || status === 29) return 3; // 调整完成/调整失败 -> 步骤4
  
  return 0; // 默认
});

// 获取步骤描述
const getStepDescription = (stepIndex) => {
  const status = currentAdjustment.adjustStatus;
  
  if (stepIndex === 0) {
    if (status === 0) return '待提交';
    if (status >= 1) return '已提交';
    return '创建调整单';
  }
  
  if (stepIndex === 1) {
    if (status === 1) return '待审核';
    if (status === 2) return '审核通过';
    if (status === 4) return '审核拒绝';
    if (status >= 11) return '已审核';
    return '等待审核';
  }
  
  if (stepIndex === 2) {
    if (status === 11) return '开始执行';
    if (status >= 20) return '已执行';
    return '等待执行';
  }
  
  if (stepIndex === 3) {
    if (status === 20) return '调整完成';
    if (status === 29) return '调整失败';
    if (status === 9) return '已取消';
    return '等待完成';
  }
  
  return '';
};

// 获取步骤状态
const getStepStatus = (stepIndex) => {
  const currentStep = currentStepView.value;
  
  if (stepIndex < currentStep) return 'success'; // 已完成
  if (stepIndex === currentStep) return 'process'; // 进行中
  
  return 'wait'; // 等待中
};

// 创建模式的计算属性（保持不变）
const totalDiffItems = computed(() => diffItems.value.length);
const processedItems = computed(() => diffItems.value.filter(item => item.adjust_status === 2).length);
const pendingItems = computed(() => diffItems.value.filter(item => item.adjust_status === 0).length);
const processingItems = computed(() => diffItems.value.filter(item => item.adjust_status === 1).length);
const availableDiffItems = computed(() => diffItems.value.filter(item => item.adjust_status === 0));
const filteredDiffItems = computed(() => {
  if (diffFilterStatus.value === 'all') return diffItems.value;
  if (diffFilterStatus.value === 'pending') return availableDiffItems.value;
  if (diffFilterStatus.value === 'processing') return diffItems.value.filter(item => item.adjust_status === 1);
  if (diffFilterStatus.value === 'processed') return diffItems.value.filter(item => item.adjust_status === 2);
  return diffItems.value;
});
const selectedGainCount = computed(() => selectedDiffItems.value.filter(item => item.diff_quantity > 0).length);
const selectedLossCount = computed(() => selectedDiffItems.value.filter(item => item.diff_quantity < 0).length);
const gainItemsCountCreate = computed(() => availableDiffItems.value.filter(item => item.diff_quantity > 0).length);
const lossItemsCountCreate = computed(() => availableDiffItems.value.filter(item => item.diff_quantity < 0).length);
const totalDiffQuantity = computed(() => availableDiffItems.value.reduce((sum, item) => sum + item.diff_quantity, 0));
const getTotalDiffClass = computed(() => totalDiffQuantity.value >= 0 ? 'positive' : 'negative');

const totalManualAdjustQuantity = computed(() => {
  if (manualAdjustmentForm.adjustType === 1) {
    return manualAdjustmentItems.value.reduce((sum, item) => sum + Math.abs(item.diffQuantity || 0), 0);
  } else {
    return manualAdjustmentItems.value.reduce((sum, item) => sum + Math.abs(item.adjustQuantity || 0), 0);
  }
});

const totalManualAdjustAmount = computed(() => {
  if (manualAdjustmentForm.adjustType === 1) {
    return manualAdjustmentItems.value.reduce((sum, item) => {
      return sum + Math.abs((item.diffQuantity || 0) * (item.unitCost || 0));
    }, 0);
  } else {
    return manualAdjustmentItems.value.reduce((sum, item) => sum + (item.adjustAmount || 0), 0);
  }
});

const totalManualDiffItems = computed(() => manualAdjustmentItems.value.filter(item => item.diffQuantity !== 0).length);
const totalManualGainItems = computed(() => manualAdjustmentItems.value.filter(item => item.diffQuantity > 0).length);
const totalManualLossItems = computed(() => manualAdjustmentItems.value.filter(item => item.diffQuantity < 0).length);

const currentStep = computed(() => {
  if (pageMode.value === 'view') {
    // 查看模式下的步骤计算已经由 currentStepView 处理
    return currentStepView.value;
  } else if (createType.value === 'manual') {
    if (currentAdjustment.adjustStatus === 5) return 5;
    if (currentAdjustment.adjustStatus === 4) return 4;
    if (currentAdjustment.adjustStatus === 2) return 3;
    if (currentAdjustment.adjustStatus === 1) return 2;
    return 1;
  } else {
    if (pendingItems.value === 0 && processedItems.value > 0) return 5;
    if (processingItems.value > 0) return 3;
    if (selectedDiffItems.value.length > 0) return 2;
    return 1;
  }
});

const protectionStatus = computed(() => {
  if (!stockTakeInfo.protectionEndTime) {
    return { type: 'info', message: '无保护期限制' };
  }
  
  const now = new Date();
  const endTime = new Date(stockTakeInfo.protectionEndTime);
  const diff = endTime - now;
  
  if (diff <= 0) {
    return { type: 'error', message: '保护期已结束', expired: true };
  }
  
  const hours = Math.floor(diff / (1000 * 60 * 60));
  const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
  
  return {
    type: hours < 24 ? 'warning' : 'info',
    message: hours < 24 ? '保护期即将结束' : '处于保护期内',
    timeLeft: `${hours}小时${minutes}分钟`,
    expired: false
  };
});

const getProtectionNoticeTitle = computed(() => {
  if (!stockTakeInfo.protectionEndTime) return '无保护期';
  if (protectionStatus.value.expired) return '保护期已结束';
  return '盘点保护期';
});

// 方法定义
const initPage = async () => {
  const params = route.params;
  const query = route.query;
  
  // 检查是否是查看模式
  if (query.viewMode === 'detail' && params.id) {
    pageMode.value = 'view';
    viewAdjustmentId.value = params.id;
    
    // 加载调整单详情
    await loadAdjustmentDetailForView(params.id);
    return;
  }
  
  // 原有的创建逻辑
  pageMode.value = 'create';
  
  if (query.createType === 'manual') {
    createType.value = 'manual';
    
    await Promise.all([
      loadWarehouseList(),
      loadReviewerList()
    ]);
    
    if (query.warehouseId) {
      manualAdjustmentForm.warehouseId = query.warehouseId;
      const warehouse = warehouseList.value.find(w => w.id === query.warehouseId);
      if (warehouse) {
        manualAdjustmentForm.warehouseName = warehouse.name;
        loadShelfOptions(query.warehouseId);
      }
    }
    
  } else if (query.stockTakeId) {
    createType.value = 'stock_take';
    
    await Promise.all([
      loadStockTakeInfo(query.stockTakeId),
      loadDiffItems(query.stockTakeId),
      loadAdjustmentOrders(query.stockTakeId),
      loadReviewerList(),
      loadWarehouseList()
    ]);
    
  } else {
    ElMessage.error('缺少必要参数');
    goBack();
  }
};

// ========== 查看模式方法 ==========
const loadAdjustmentDetailForView = async (adjustmentId) => {
  try {
    const res = await get(`/api/auth/adjust/detail?id=${adjustmentId}`);
    console.log('查看模式调整单详情:', res);
    if (res) {
      // 将数据设置到当前调整单对象
      Object.assign(currentAdjustment, {
        id: res.id,
        adjustNo: res.adjustNo,
        tenantId: res.tenantId,
        adjustType: res.adjustType,
        sourceType: res.sourceType,
        sourceId: res.sourceId,
        sourceNo: res.sourceNo,
        warehouseId: res.warehouseId,
        warehouseName: res.warehouseName,
        adjustReason: res.adjustReason,
        adjustStatus: res.adjustStatus,
        totalItems: res.totalItems,
        totalQuantity: res.totalQuantity,
        totalCostAmount: res.totalCostAmount,
        totalAmount: res.totalAmount,
        isAffectCost: res.isAffectCost,
        isUrgent: res.isUrgent,
        priority: res.priority,
        expectExecuteTime: res.expectExecuteTime,
        actualExecuteTime: res.actualExecuteTime,
        remark: res.remark,
        attachmentUrls: res.attachmentUrls,
        extData: res.extData,
        approverId: res.approverId,
        approverName: res.approverName,
        approveTime: res.approveTime,
        approveRemark: res.approveRemark,
        createdBy: res.createdBy,
        createdByName: res.createdByName,
        createdAt: res.createdAt,
        modifiedBy: res.modifiedBy,
        modifiedByName: res.modifiedByName,
        modifiedAt: res.modifiedAt
      });
      
      // 设置明细数据
      if (res.items && Array.isArray(res.items)) {
        adjustmentDetailItems.value = res.items.map(item => ({
          id: item.id,
          productId: item.productId,
          skuCode: item.skuCode,
          productName: item.productName,
          specification: item.specification,
          unit: item.unit,
          batchNo: item.batchNo,
          shelfId: item.shelfId,
          shelfCode: item.shelfCode,
          locationCode: item.locationCode,
          beforeQuantity: item.beforeQuantity,
          adjustQuantity: item.adjustQuantity,
          afterQuantity: item.afterQuantity,
          unitCost: item.unitCost,
          adjustAmount: item.adjustAmount,
          adjustReason: item.adjustReason,
          itemRemark: item.itemRemark,
          status: item.status,
          executeTime: item.executeTime,
          executeBy: item.executeBy,
          executeByName: item.executeByName,
          isAffectCost: item.isAffectCost,
          costAdjustMethod: item.costAdjustMethod,
          sourceItemId: item.sourceItemId,
          extData: item.extData
        }));
      }
      
      // 如果是基于盘点单创建的，加载盘点单信息
      if (res.sourceType === 1 && res.sourceId) {
        await loadStockTakeInfoForView(res.sourceId);
      }
      
      // 加载调整单历史（如果是基于盘点单）
      if (res.sourceType === 1 && res.sourceId) {
        await loadAdjustmentOrders(res.sourceId);
      }
      
      ElMessage.success('调整单详情加载成功');
    } else {
      ElMessage.error('调整单不存在');
      goBack();
    }
  } catch (error) {
    console.error('加载调整单详情失败:', error);
    ElMessage.error('加载调整单详情失败');
    goBack();
  }
};

const loadStockTakeInfoForView = async (stockTakeId) => {
  try {
    const res = await get(`/api/auth/stock/stockDetail?id=${stockTakeId}`);
    if (res) {
      Object.assign(stockTakeInfo, {
        id: res.id,
        stockTakeNo: res.stockTakeNo,
        warehouseId: res.warehouseId,
        warehouseName: res.warehouseName,
        takeType: res.takeType,
        takeTypeName: res.takeTypeName,
        takeStatus: res.takeStatus,
        actualEndTime: res.actualEndTime,
        protectionEndTime: res.protectionEndTime,
        totalItems: res.totalItems,
        countedItems: res.countedItems,
        diffItems: res.diffItems
      });
    }
  } catch (error) {
    console.error('加载盘点单信息失败:', error);
  }
};

// ========== 创建模式方法（保持不变） ==========
const loadWarehouseList = async () => {
  try {
    const res = await get('/api/auth/warehouse/list');
    warehouseList.value = res || [];
  } catch (error) {
    console.error('加载仓库列表失败:', error);
    warehouseList.value = [];
  }
};

const loadStockTakeInfo = async (stockTakeId) => {
  if (!stockTakeId) return;
  
  try {
    const res = await get(`/api/auth/stock/stockDetail?id=${stockTakeId}`);
    if (res) {
      Object.assign(stockTakeInfo, {
        id: res.id,
        stockTakeNo: res.stockTakeNo,
        warehouseId: res.warehouseId,
        warehouseName: res.warehouseName,
        takeType: res.takeType,
        takeTypeName: res.takeTypeName,
        takeStatus: res.takeStatus,
        actualEndTime: res.actualEndTime,
        protectionEndTime: res.protectionEndTime,
        totalItems: res.totalItems,
        countedItems: res.countedItems,
        diffItems: res.diffItems
      });
    }
  } catch (error) {
    console.error('加载盘点单信息失败:', error);
    ElMessage.error('加载盘点单信息失败');
  }
};

const loadDiffItems = async (stockTakeId) => {
  loadingDiffItems.value = true;
  
  try {
    const params = {
      stockTakeId,
      page: pagination.current,
      size: pagination.size,
      adjustStatus: diffFilterStatus.value === 'all' ? null : 
                    diffFilterStatus.value === 'pending' ? 0 :
                    diffFilterStatus.value === 'processing' ? 1 : 2
    };
    
    const res = await post('/api/auth/stock/diffItems', params);
    if (res && res.records) {
      diffItems.value = res.records.map(item => ({
        id: item.id,
        product_id: item.productId,
        sku_code: item.skuCode,
        product_name: item.productName,
        specification: item.specification,
        batch_no: item.batchNo,
        shelf_id: item.shelfId,
        shelf_name: item.shelfName,
        location_code: item.locationCode,
        system_quantity: item.systemQuantity,
        counted_quantity: item.countedQuantity,
        diff_quantity: item.diffQuantity,
        adjust_status: item.adjustStatus || 0,
        adjustment_id: item.adjustmentId,
        adjustment_no: item.adjustmentNo
      }));
      pagination.total = res.total || 0;
    }
  } catch (error) {
    console.error('加载差异项失败:', error);
    ElMessage.error('加载差异项失败');
  } finally {
    loadingDiffItems.value = false;
  }
};

const loadAdjustmentOrders = async (stockTakeId) => {
  if (!stockTakeId) return;
  
  try {
    const res = await get(`/api/auth/adjust/list?sourceId=${stockTakeId}&sourceType=1`);
    if (res ) {
      adjustmentOrders.value = res.data.map(order => ({
        id: order.id,
        adjustment_no: order.adjustNo,
        adjust_type: order.adjustType,
        adjust_status: order.adjustStatus,
        total_items: order.totalItems,
        created_at: order.createdAt,
        creator_name: order.createdByName,
        warehouse_name: order.warehouseName,
        stock_take_no: order.sourceNo
      }));
    }
  } catch (error) {
    console.error('加载调整单失败:', error);
  }
};

const loadShelfOptions = async (warehouseId) => {
  if (!warehouseId) return;
  
  try {
    const res = await get(`/api/auth/inventory/allShelfOfWareHouse?warehouseId=${warehouseId}`);
    shelfOptions.value = res || [];
  } catch (error) {
    console.error('加载货架列表失败:', error);
    shelfOptions.value = [];
  }
};

const loadReviewerList = async () => {
  try {
    const res = await post('/api/auth/user/searchUser');
    if (res && Array.isArray(res)) {
      reviewerList.value = res.map(user => ({
        id: user.id,
        name: user.username || user.name,
        username: user.username,
        department: user.department || '未分配部门',
        avatarUrl: user.avatarUrl || '/default-avatar.png'
      }));
    }
  } catch (error) {
    console.error('加载用户列表失败:', error);
    ElMessage.error('加载用户列表失败');
    reviewerList.value = [];
  }
};

const loadCompletedStockTakeList = async () => {
  if (!manualAdjustmentForm.warehouseId) {
    ElMessage.warning('请先选择仓库');
    return;
  }
  
  loadingStockTakeList.value = true;
  try {
    const res = await get(`/api/auth/stock/completedStockTakeList?warehouseId=${manualAdjustmentForm.warehouseId}`);
    if (res && Array.isArray(res)) {
      completedStockTakeList.value = res.map(item => ({
        id: item.id,
        stockTakeNo: item.stockTakeNo,
        warehouseName: item.warehouseName,
        takeType: item.takeType,
        takeTypeName: item.takeTypeName,
        actualEndTime: item.actualEndTime,
        totalItems: item.totalItems,
        countedItems: item.countedItems,
        diffItems: item.diffItems
      }));
    } else {
      completedStockTakeList.value = [];
    }
  } catch (error) {
    console.error('加载已完成盘点单列表失败:', error);
    ElMessage.error('加载已完成盘点单列表失败');
    completedStockTakeList.value = [];
  } finally {
    loadingStockTakeList.value = false;
  }
};

const loadStockTakeItems = async () => {
  if (!manualAdjustmentForm.stockTakeId) {
    ElMessage.warning('请先选择盘点单');
    return;
  }
  
  loadingStockTakeItems.value = true;
  try {
    const params = {
      stockTakeId: manualAdjustmentForm.stockTakeId
    };
    
    const res = await post('/api/auth/stock/itemList', params);
    if (res && Array.isArray(res)) {
      manualAdjustmentItems.value = [];
      
      res.forEach(item => {
        if (item.diffQuantity !== 0) {
          manualAdjustmentItems.value.push({
            stockTakeItemId: item.stockTakeItemId,
            productId: item.productId,
            skuCode: item.sku,
            productName: item.productName,
            specification: item.spec,
            color: item.color,
            batchNo: item.batchNo,
            shelfId: item.shelfId,
            shelfName: item.shelfName,
            beforeQuantity: item.systemQuantity || 0,
            countedQuantity: item.countedQuantity || 0,
            diffQuantity: item.diffQuantity || 0,
            adjustQuantity: item.diffQuantity || 0,
            afterQuantity: item.countedQuantity || 0,
            unitCost: 0,
            adjustAmount: 0,
            itemReason: '',
            sourceItemId: item.stockTakeItemId
          });
        }
      });
      
      if (manualAdjustmentItems.value.length === 0) {
        ElMessage.info('该盘点单没有差异项');
      } else {
        ElMessage.success(`已加载 ${manualAdjustmentItems.value.length} 个差异项`);
      }
    } else {
      ElMessage.warning('该盘点单没有明细数据');
      manualAdjustmentItems.value = [];
    }
  } catch (error) {
    console.error('加载盘点单明细失败:', error);
    ElMessage.error('加载盘点单明细失败');
    manualAdjustmentItems.value = [];
  } finally {
    loadingStockTakeItems.value = false;
  }
};

const loadManualProductList = async () => {
  if (!manualAdjustmentForm.warehouseId) {
    ElMessage.warning('请先选择仓库');
    return;
  }
  
  manualProductLoading.value = true;
  try {
    const params = {
      page: manualProductPagination.current,
      size: manualProductPagination.size,
      warehouseId: manualAdjustmentForm.warehouseId,
      keyword: manualProductFilter.keyword
    };
    
    const res = await get('/api/auth/inventory/listOfWarehouse?warehouseId=' + manualAdjustmentForm.warehouseId);
    if (res ) {
      manualProductList.value = res.map(item => ({
        id: item.productId || '',
        productCode: item.sku || '',
        skuCode: item.sku || '',
        productName: item.productName || '',
        specification: item.spec || '',
        unit: item.unitName || '',
        currentQuantity: item.availableQuantity || 0,
        totalQuantity: item.quantity || 0,
        lockedQuantity: item.lockedQuantity || 0,
        shelfId: '',
        shelfName: '',
        locationCode: '',
        unitCost: item.price || 0,
        categoryName: item.categoryName || '',
        color: item.color || ''
      }));
      manualProductPagination.total = res.total || 0;
    } else {
      manualProductList.value = [];
      manualProductPagination.total = 0;
    }
  } catch (error) {
    console.error('加载商品列表失败:', error);
    ElMessage.error('加载商品列表失败');
    manualProductList.value = [];
  } finally {
    manualProductLoading.value = false;
  }
};

const handleDiffSelectionChange = (selection) => {
  selectedDiffItems.value = selection;
  
  if (selectedDiffItems.value.length > 0) {
    const hasGain = selectedDiffItems.value.some(item => item.diff_quantity > 0);
    const hasLoss = selectedDiffItems.value.some(item => item.diff_quantity < 0);
    
    if (hasGain && !hasLoss) {
      adjustmentForm.adjust_type = 1;
    } else if (!hasGain && hasLoss) {
      adjustmentForm.adjust_type = 2;
    } else {
      adjustmentForm.adjust_type = 3;
    }
  }
};

const isDiffItemSelectable = (row) => {
  return row.adjust_status === 0;
};

const diffTableRowClassName = ({ row }) => {
  if (row.adjust_status === 1) return 'row-processing';
  if (row.adjust_status === 2) return 'row-processed';
  return '';
};

const selectAllPendingItems = () => {
  selectedDiffItems.value = [...availableDiffItems.value];
};

const clearSelection = () => {
  selectedDiffItems.value = [];
};

const handleWarehouseChange = (warehouseId) => {
  const warehouse = warehouseList.value.find(w => w.id === warehouseId);
  if (warehouse) {
    manualAdjustmentForm.warehouseName = warehouse.name;
    loadShelfOptions(warehouseId);
    
    manualAdjustmentItems.value = [];
    
    manualAdjustmentForm.stockTakeId = null;
    manualAdjustmentForm.stockTakeNo = '';
    completedStockTakeList.value = [];
    
    if (manualAdjustmentForm.adjustType === 1) {
      loadCompletedStockTakeList();
    }
  }
};

const handleAdjustTypeChange = (type) => {
  manualAdjustmentItems.value = [];
  
  if (type === 1) {
    if (manualAdjustmentForm.warehouseId) {
      loadCompletedStockTakeList();
    }
  }
};

const handleStockTakeSelect = (stockTakeId) => {
  if (stockTakeId) {
    const stockTake = completedStockTakeList.value.find(item => item.id === stockTakeId);
    if (stockTake) {
      manualAdjustmentForm.stockTakeNo = stockTake.stockTakeNo;
      manualAdjustmentForm.stockTakeId = stockTakeId;
      manualAdjustmentForm.adjustReason = 'stock_take_diff';
    }
  } else {
    manualAdjustmentForm.stockTakeNo = '';
    manualAdjustmentForm.stockTakeId = null;
    manualAdjustmentItems.value = [];
  }
};

const openManualProductDialog = () => {
  if (!manualAdjustmentForm.warehouseId) {
    ElMessage.warning('请先选择仓库');
    return;
  }
  
  selectedManualProducts.value = [];
  
  manualProductFilter.keyword = '';
  manualProductPagination.current = 1;
  
  loadManualProductList();
  
  manualProductDialogVisible.value = true;
};

const handleManualProductSelectionChange = (selection) => {
  selectedManualProducts.value = selection;
};

const handleConfirmManualProducts = () => {
  selectedManualProducts.value.forEach(product => {
    const exists = manualAdjustmentItems.value.some(item => item.productId === product.id);
    if (!exists) {
      manualAdjustmentItems.value.push({
        productId: product.id,
        productCode: product.productCode,
        skuCode: product.skuCode,
        productName: product.productName,
        specification: product.specification,
        unit: product.unit,
        batchNo: '',
        shelfId: product.shelfId || '',
        shelfCode: product.shelfName || '',
        beforeQuantity: product.currentQuantity || 0,
        countedQuantity: 0,
        diffQuantity: 0,
        adjustQuantity: 0,
        afterQuantity: product.currentQuantity || 0,
        unitCost: product.unitCost || 0,
        adjustAmount: 0,
        itemReason: '',
        sourceItemId: null
      });
    }
  });
  
  manualProductDialogVisible.value = false;
  selectedManualProducts.value = [];
};

const removeManualItem = (item) => {
  const index = manualAdjustmentItems.value.indexOf(item);
  if (index > -1) {
    manualAdjustmentItems.value.splice(index, 1);
  }
};

const handleManualAdjustQuantityChange = (item) => {
  item.afterQuantity = (item.beforeQuantity || 0) + (item.adjustQuantity || 0);
  item.diffQuantity = item.adjustQuantity || 0;
  item.adjustAmount = Math.abs((item.adjustQuantity || 0) * (item.unitCost || 0));
};

const handleManualCostChange = (item) => {
  if (manualAdjustmentForm.adjustType === 1) {
    item.adjustAmount = Math.abs((item.diffQuantity || 0) * (item.unitCost || 0));
  } else {
    item.adjustAmount = Math.abs((item.adjustQuantity || 0) * (item.unitCost || 0));
  }
};

const handleManualProductSizeChange = (size) => {
  manualProductPagination.size = size;
  manualProductPagination.current = 1;
  loadManualProductList();
};

const handleManualProductCurrentChange = (page) => {
  manualProductPagination.current = page;
  loadManualProductList();
};

// 创建调整单（基于盘点单）
const handleCreateAdjustment = async () => {
  if (selectedDiffItems.value.length === 0) {
    ElMessage.warning('请先选择要调整的差异项');
    return;
  }
  
  if (!adjustmentForm.adjust_reason) {
    ElMessage.warning('请选择调整原因');
    return;
  }
  
  if (!adjustmentForm.expected_complete_time) {
    ElMessage.warning('请选择期望完成时间');
    return;
  }
  
  creatingAdjustment.value = true;
  
  try {
    const hasGain = selectedDiffItems.value.some(item => item.diff_quantity > 0);
    const hasLoss = selectedDiffItems.value.some(item => item.diff_quantity < 0);
    let adjustType;
    if (hasGain && !hasLoss) {
      adjustType = 3; // 报溢调整
    } else if (!hasGain && hasLoss) {
      adjustType = 2; // 报损调整
    } else {
      adjustType = 6; // 其他调整（混合）
    }
    
    const items = selectedDiffItems.value.map(item => {
      const adjustQuantity = item.diff_quantity;
      const afterQuantity = item.counted_quantity;
      const beforeQuantity = item.system_quantity;
      
      return {
        productId: item.product_id,
        productName: item.product_name,
        productCode: item.sku_code,
        skuCode: item.sku_code,
        spec: item.specification || '',
        unit: '',
        batchNo: item.batch_no || null,
        shelfId: item.shelf_id || null,
        shelfCode: item.shelf_name || '',
        locationCode: item.location_code || '',
        beforeQuantity: beforeQuantity,
        adjustQuantity: adjustQuantity,
        afterQuantity: afterQuantity,
        unitCost: 0,
        unitPrice: 0,
        adjustCostAmount: 0,
        adjustAmount: 0,
        itemReason: adjustmentForm.adjust_reason,
        sourceItemId: item.id
      };
    });
    
    const adjustData = {
      warehouseId: stockTakeInfo.warehouseId || '',
      warehouseName: stockTakeInfo.warehouseName,
      adjustType: adjustType,
      sourceType: 1,
      sourceId: stockTakeInfo.id,
      sourceNo: stockTakeInfo.stockTakeNo,
      adjustReason: adjustmentForm.adjust_reason,
      isAffectCost: false,
      isUrgent: false,
      priority: 3,
      remark: adjustmentForm.remark || '',
      expectExecuteTime: adjustmentForm.expected_complete_time,
      approvalUserId: adjustmentForm.reviewer_id,
      items: items
    };
    
    const res = await post('/api/auth/adjust/createManual', adjustData);
    if (res ) {
      ElMessage.success('调整单创建成功');
      
      if (adjustmentFormRef.value) {
        adjustmentFormRef.value.resetFields();
      }
      Object.assign(adjustmentForm, {
        adjust_type: 3,
        adjust_reason: 'stock_take_diff',
        expected_complete_time: null,
        reviewer_id: null,
        remark: ''
      });
      
      selectedDiffItems.value = [];
      
      await Promise.all([
        loadDiffItems(stockTakeInfo.id),
        loadAdjustmentOrders(stockTakeInfo.id),
        loadStockTakeInfo(stockTakeInfo.id)
      ]);
      
      if (res.data && res.data.id) {
        // 跳转到查看模式
        router.push({
          path: `/index/ckStockAdjustment/${res.data.id}`,
          query: {
            viewMode: 'detail'
          }
        });
      }
    } else {
      ElMessage.error(res.message || '创建调整单失败');
    }
  } catch (error) {
    console.error('创建调整单失败:', error);
    ElMessage.error('创建调整单失败');
  } finally {
    creatingAdjustment.value = false;
  }
};

// 创建调整单（手动创建）
const handleCreateManualAdjustment = async () => {
  if (manualAdjustmentItems.value.length === 0) {
    ElMessage.warning('请至少添加一条调整明细');
    return;
  }
  
  if (!manualAdjustmentForm.warehouseId) {
    ElMessage.warning('请选择仓库');
    return;
  }
  
  if (!manualAdjustmentForm.adjustReason) {
    ElMessage.warning('请选择调整原因');
    return;
  }
  
  if (!manualAdjustmentForm.expectedCompleteTime) {
    ElMessage.warning('请选择期望完成时间');
    return;
  }
  
  if (manualAdjustmentForm.adjustType === 1 && !manualAdjustmentForm.stockTakeId) {
    ElMessage.warning('请选择盘点单');
    return;
  }
  
  creatingManualAdjustment.value = true;
  
  try {
    const items = manualAdjustmentItems.value.map(item => {
      let adjustQuantity;
      if (manualAdjustmentForm.adjustType === 1) {
        adjustQuantity = item.diffQuantity;
      } else {
        adjustQuantity = item.adjustQuantity || 0;
      }
      
      const afterQuantity = item.afterQuantity;
      const beforeQuantity = item.beforeQuantity;
      
      return {
        productId: item.productId,
        productName: item.productName,
        productCode: item.skuCode,
        skuCode: item.skuCode,
        spec: item.specification || '',
        unit: item.unit || '',
        batchNo: item.batchNo || null,
        shelfId: item.shelfId || null,
        shelfCode: item.shelfCode || '',
        locationCode: item.locationCode || '',
        beforeQuantity: beforeQuantity,
        adjustQuantity: adjustQuantity,
        afterQuantity: afterQuantity,
        unitCost: item.unitCost || 0,
        unitPrice: 0,
        adjustCostAmount: 0,
        adjustAmount: 0,
        itemReason: item.itemReason || manualAdjustmentForm.adjustReason,
        sourceItemId: item.sourceItemId || null
      };
    });
    
    const adjustData = {
      warehouseId: manualAdjustmentForm.warehouseId,
      warehouseName: manualAdjustmentForm.warehouseName,
      adjustType: manualAdjustmentForm.adjustType,
      sourceType: manualAdjustmentForm.adjustType === 1 ? 1 : null,
      sourceId: manualAdjustmentForm.adjustType === 1 ? manualAdjustmentForm.stockTakeId : null,
      sourceNo: manualAdjustmentForm.adjustType === 1 ? manualAdjustmentForm.stockTakeNo : null,
      adjustReason: manualAdjustmentForm.adjustReason,
      isAffectCost: manualAdjustmentForm.isAffectCost,
      isUrgent: manualAdjustmentForm.isUrgent,
      priority: manualAdjustmentForm.isUrgent ? 1 : 3,
      remark: manualAdjustmentForm.remark || '',
      expectExecuteTime: manualAdjustmentForm.expectedCompleteTime,
      approvalUserId: manualAdjustmentForm.reviewerId,
      items: items
    };
    
    const res = await post('/api/auth/adjust/createManual', adjustData);
    if (res ) {
      ElMessage.success('调整单创建成功');
      
      if (manualAdjustmentFormRef.value) {
        manualAdjustmentFormRef.value.resetFields();
      }
      
      manualAdjustmentItems.value = [];
      
      if (createType.value === 'stock_take') {
        await loadAdjustmentOrders(stockTakeInfo.id);
      } else {
        await initPage();
      }
      
      if (res.data && res.data.id) {
        // 跳转到查看模式
        router.push({
          path: `/index/ckStockAdjustment/${res.data.id}`,
          query: {
            viewMode: 'detail'
          }
        });
      }
    } else {
      ElMessage.error(res.message || '创建调整单失败');
    }
  } catch (error) {
    console.error('创建调整单失败:', error);
    ElMessage.error('创建调整单失败');
  } finally {
    creatingManualAdjustment.value = false;
  }
};

const handleCreateSingleAdjustment = async (row) => {
  selectedDiffItems.value = [row];
  await handleCreateAdjustment();
};

const handleCreateAdjustmentForSelected = async () => {
  if (selectedDiffItems.value.length === 0) {
    ElMessage.warning('请先选择要调整的差异项');
    return;
  }
  await handleCreateAdjustment();
};

// 批量创建调整单
const handleBatchProcessAll = () => {
  if (availableDiffItems.value.length === 0) {
    ElMessage.warning('没有可处理的差异项');
    return;
  }
  batchConfirmDialogVisible.value = true;
};

const handleConfirmBatchProcess = async () => {
  if (!batchAdjustmentForm.adjust_reason) {
    ElMessage.warning('请选择调整原因');
    return;
  }
  
  creatingBatchAdjustment.value = true;
  
  try {
    const items = availableDiffItems.value.map(item => {
      const adjustQuantity = item.diff_quantity;
      const afterQuantity = item.counted_quantity;
      const beforeQuantity = item.system_quantity;
      
      return {
        productId: item.product_id,
        productName: item.product_name,
        productCode: item.sku_code,
        skuCode: item.sku_code,
        spec: item.specification || '',
        unit: '',
        batchNo: item.batch_no || null,
        shelfId: item.shelf_id || null,
        shelfCode: item.shelf_name || '',
        locationCode: item.location_code || '',
        beforeQuantity: beforeQuantity,
        adjustQuantity: adjustQuantity,
        afterQuantity: afterQuantity,
        unitCost: 0,
        unitPrice: 0,
        adjustCostAmount: 0,
        adjustAmount: 0,
        itemReason: batchAdjustmentForm.adjust_reason,
        sourceItemId: item.id
      };
    });
    
    const adjustData = {
      warehouseId: stockTakeInfo.warehouseId || '',
      warehouseName: stockTakeInfo.warehouseName,
      adjustType: 6,
      sourceType: 1,
      sourceId: stockTakeInfo.id,
      sourceNo: stockTakeInfo.stockTakeNo,
      adjustReason: batchAdjustmentForm.adjust_reason,
      isAffectCost: false,
      isUrgent: false,
      priority: 3,
      remark: batchAdjustmentForm.remark || '批量处理所有剩余差异项',
      expectExecuteTime: new Date().toISOString().replace('T', ' ').substring(0, 19),
      items: items
    };
    
    const res = await post('/api/auth/adjust/createManual', adjustData);
    if (res ) {
      ElMessage.success('批量调整单创建成功');
      batchConfirmDialogVisible.value = false;
      
      await Promise.all([
        loadDiffItems(stockTakeInfo.id),
        loadAdjustmentOrders(stockTakeInfo.id),
        loadStockTakeInfo(stockTakeInfo.id)
      ]);
      
      if (res.data && res.data.id) {
        // 跳转到查看模式
        router.push({
          path: `/index/ckStockAdjustment/${res.data.id}`,
          query: {
            viewMode: 'detail'
          }
        });
      }
    } else {
      ElMessage.error(res.message || '批量创建调整单失败');
    }
  } catch (error) {
    console.error('批量创建调整单失败:', error);
    ElMessage.error('批量创建调整单失败');
  } finally {
    creatingBatchAdjustment.value = false;
  }
};

const viewAdjustmentDetail = async (adjustmentId) => {
  router.push({
    path: `/index/ckStockAdjustment/${adjustmentId}`,
    query: {
      viewMode: 'detail'
    }
  });
};

// ========== 查看模式操作 ==========
const handleSubmitForReview = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要提交审核吗？提交后不可再编辑。',
      '提交审核确认',
      { type: 'warning' }
    );
    
    submitting.value = true;
    
    const res = await post('/api/auth/adjust/submitApprove', {
      id: currentAdjustment.id,
      userId: 0, // 从登录信息获取
      tenantId: currentAdjustment.tenantId
    });
    
    if (res && res.code === 200) {
      ElMessage.success('提交审核成功');
      // 重新加载详情
      await loadAdjustmentDetailForView(currentAdjustment.id);
    } else {
      ElMessage.error(res.message || '提交审核失败');
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('提交审核失败:', error);
    }
  } finally {
    submitting.value = false;
  }
};

const handleExecuteAdjustment = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要执行此调整单吗？执行后库存将被实际调整。',
      '执行调整确认',
      { type: 'warning' }
    );
    
    executing.value = true;
    
    const res = await post('/api/auth/adjust/execute', {
      id: currentAdjustment.id,
      userId: 0, // 从登录信息获取
      tenantId: currentAdjustment.tenantId
    });
    
    if (res && res.code === 200) {
      ElMessage.success('调整单执行成功');
      await loadAdjustmentDetailForView(currentAdjustment.id);
    } else {
      ElMessage.error(res.message || '执行调整单失败');
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('执行调整单失败:', error);
    }
  } finally {
    executing.value = false;
  }
};

const handleReapplyAdjustment = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要重新申请此调整单吗？',
      '重新申请确认'
    );
    
    reapplying.value = true;
    
    const res = await post('/api/auth/adjust/reapply', {
      id: currentAdjustment.id
    });
    
    if (res && res.code === 200) {
      ElMessage.success('重新申请成功');
      await loadAdjustmentDetailForView(currentAdjustment.id);
    } else {
      ElMessage.error(res.message || '重新申请失败');
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('重新申请失败:', error);
    }
  } finally {
    reapplying.value = false;
  }
};

const handlePrintAdjustment = () => {
  window.print();
};

const refreshAllData = async () => {
  if (pageMode.value === 'view') {
    await loadAdjustmentDetailForView(currentAdjustment.id);
  } else if (createType.value === 'stock_take' && stockTakeInfo.id) {
    await Promise.all([
      loadStockTakeInfo(stockTakeInfo.id),
      loadDiffItems(stockTakeInfo.id),
      loadAdjustmentOrders(stockTakeInfo.id)
    ]);
  }
};

const refreshAdjustmentOrders = async () => {
  if (createType.value === 'stock_take' && stockTakeInfo.id) {
    await loadAdjustmentOrders(stockTakeInfo.id);
  }
};

const handleSizeChange = (size) => {
  pagination.size = size;
  pagination.current = 1;
  if (stockTakeInfo.id) {
    loadDiffItems(stockTakeInfo.id);
  }
};

const handleCurrentChange = (page) => {
  pagination.current = page;
  if (stockTakeInfo.id) {
    loadDiffItems(stockTakeInfo.id);
  }
};

const exportAdjustmentReport = async () => {
  exporting.value = true;
  try {
    const stockTakeId = stockTakeInfo.id;
    const res = await post(
      '/api/auth/stock/exportAdjustmentReport',
      { stockTakeId },
      { responseType: 'blob' }
    );
    
    if (res) {
      const blob = new Blob([res], { type: 'application/vnd.ms-excel' });
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `盘点差异调整报告_${stockTakeInfo.stockTakeNo}_${new Date().getTime()}.xlsx`;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      window.URL.revokeObjectURL(url);
      ElMessage.success('导出成功');
    }
  } catch (error) {
    console.error('导出失败:', error);
    ElMessage.error('导出失败');
  } finally {
    exporting.value = false;
  }
};

const goBack = () => {
  if (pageMode.value === 'view') {
    // 查看模式返回调整单管理页面
    router.push('/stock/adjust-manage');
  } else if (createType.value === 'stock_take') {
    router.push('/stock/take-list');
  } else {
    router.push('/stock/adjust-manage');
  }
};

// 工具方法
const formatDateTime = (timestamp) => {
  if (!timestamp) return '--';
  const date = new Date(timestamp);
  return date.toLocaleString('zh-CN');
};

const formatNumber = (num) => {
  if (num === null || num === undefined) return '0';
  const number = Number(num);
  if (isNaN(number)) return '0';
  return number.toLocaleString('zh-CN', {
    minimumFractionDigits: 0,
    maximumFractionDigits: 4
  });
};

const formatCurrency = (amount) => {
  if (amount === null || amount === undefined) return '0.00';
  const number = Number(amount);
  if (isNaN(number)) return '0.00';
  return number.toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  });
};

const formatAdjustmentNo = (no) => {
  if (!no) return '--';
  if (no.length > 15) return no.substring(0, 12) + '...';
  return no;
};

const getDiffQuantityClass = (diff) => {
  if (diff > 0) return 'diff-positive';
  if (diff < 0) return 'diff-negative';
  return '';
};

const getAdjustQuantityClass = (adjust) => {
  if (adjust > 0) return 'positive';
  if (adjust < 0) return 'negative';
  return '';
};

const getTakeStatusTagType = (status) => {
  const mapping = { 3: 'primary', 4: 'warning', 6: 'success' };
  return mapping[status] || 'info';
};

const getTakeStatusLabel = (status) => {
  const mapping = {
    3: '待确认',
    4: '已确认（待调整）',
    6: '已完成'
  };
  return mapping[status] || '未知';
};

const getAdjustStatusTagType = (status) => {
  const mapping = { 0: 'info', 1: 'warning', 2: 'success' };
  return mapping[status] || 'info';
};

const getAdjustStatusLabel = (status) => {
  const mapping = {
    0: '未处理',
    1: '处理中',
    2: '已处理'
  };
  return mapping[status] || '未知';
};

const getOrderStatusTagType = (status) => {
  const mapping = {
    0: 'info',      // 待提交
    1: 'warning',   // 待审核
    2: 'primary',   // 审核通过
    4: 'danger',    // 审核拒绝
    9: 'info',      // 已取消
    11: 'warning',  // 开始执行
    20: 'success',  // 调整完成
    29: 'danger'    // 调整失败
  };
  return mapping[status] || 'info';
};

const getOrderStatusLabel = (status) => {
  const mapping = {
    0: '待提交',
    1: '待审核',
    2: '审核通过',
    4: '审核拒绝',
    9: '已取消',
    11: '开始执行',
    20: '调整完成',
    29: '调整失败'
  };
  return mapping[status] || '未知';
};

const getStatusTagType = (status) => {
  const mapping = {
    0: 'info',
    1: 'warning',
    2: 'primary',
    4: 'danger',
    9: 'info',
    11: 'warning',
    20: 'success',
    29: 'danger'
  };
  return mapping[status] || 'info';
};

const getStatusLabel = (status) => {
  const mapping = {
    0: '待提交',
    1: '待审核',
    2: '审核通过',
    4: '审核拒绝',
    9: '已取消',
    11: '开始执行',
    20: '调整完成',
    29: '调整失败'
  };
  return mapping[status] || '未知';
};

const getAdjustTypeTagType = (type) => {
  const mapping = {
    1: 'primary',
    2: 'danger',
    3: 'success',
    4: 'warning',
    5: 'info',
    6: ''
  };
  return mapping[type] || 'info';
};

const getAdjustTypeLabel = (type) => {
  const mapping = {
    1: '盘点调整',
    2: '报损调整',
    3: '报溢调整',
    4: '成本调整',
    5: '库存转移',
    6: '其他调整'
  };
  return mapping[type] || '未知';
};

const getSourceTypeLabel = (type) => {
  const mapping = {
    1: '盘点单',
    2: '手动创建',
    3: '异常处理',
    4: '系统自动'
  };
  return mapping[type] || '未知';
};

const getItemStatusTagType = (status) => {
  const mapping = { 1: 'warning', 2: 'success' };
  return mapping[status] || 'info';
};

const getItemStatusLabel = (status) => {
  const mapping = { 1: '待调整', 2: '已调整' };
  return mapping[status] || '未知';
};

const getQuantityClass = (quantity) => {
  if (quantity > 0) return 'quantity-positive';
  if (quantity < 0) return 'quantity-negative';
  return 'quantity-zero';
};

const getAmountClass = (amount) => {
  if (amount > 0) return 'amount-positive';
  if (amount < 0) return 'amount-negative';
  return 'amount-zero';
};

const getAfterQuantityClass = (quantity) => {
  if (quantity > 0) return 'after-positive';
  return 'after-zero';
};

const disabledPastDate = (time) => {
  return time.getTime() < Date.now() - 24 * 60 * 60 * 1000;
};

onMounted(() => {
  initPage();
});

watch(diffFilterStatus, () => {
  pagination.current = 1;
  if (stockTakeInfo.id) {
    loadDiffItems(stockTakeInfo.id);
  }
});

watch(() => manualAdjustmentForm.warehouseId, (newVal) => {
  if (newVal && manualAdjustmentForm.adjustType === 1) {
    loadCompletedStockTakeList();
  }
});

watch(() => manualAdjustmentForm.adjustType, (newVal) => {
  manualAdjustmentItems.value = [];
  
  if (newVal === 1 && manualAdjustmentForm.warehouseId) {
    loadCompletedStockTakeList();
  }
});
</script>

<style scoped>
.stock-adjustment-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 0 10px;
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.page-title {
  margin: 0;
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-actions {
  display: flex;
  gap: 12px;
}

/* ========== 查看模式样式 ========== */
.view-mode-container {
  padding: 0 10px;
}

/* 查看模式下的步骤条 */
.view-mode-steps {
  margin-bottom: 30px;
  padding: 20px;
  background: white;
  border-radius: 8px;
}

.adjustment-basic-card {
  margin-bottom: 20px;
}

.highlight-text {
  font-weight: bold;
  color: #409EFF;
  font-size: 16px;
}

.basic-info-grid {
  padding: 10px;
}

.adjustment-detail-table {
  width: 100%;
}

/* 产品信息样式 */
.product-info {
  display: flex;
  flex-direction: column;
}

.product-sku {
  font-family: 'Courier New', monospace;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 4px;
}

.product-name {
  font-size: 14px;
  color: #303133;
  margin-bottom: 2px;
}

.product-spec {
  font-size: 12px;
  color: #909399;
}

/* 位置信息样式 */
.location-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.batch-info {
  display: flex;
  align-items: center;
}

.shelf-info {
  font-size: 12px;
  color: #666;
  display: flex;
  align-items: center;
  gap: 4px;
}

.location-code {
  font-size: 11px;
  color: #999;
  font-family: 'Courier New', monospace;
}

/* 数量相关样式 */
.positive {
  color: #67C23A;
  font-weight: bold;
}

.negative {
  color: #F56C6C;
  font-weight: bold;
}

.before-quantity, .after-quantity {
  font-weight: 500;
}

/* 汇总信息样式 */
.header-summary {
  display: flex;
  gap: 20px;
  align-items: center;
}

.summary-item {
  font-size: 14px;
  color: #606266;
}

.summary-item span {
  font-weight: bold;
  font-size: 16px;
}

/* 统计信息 */
.items-statistics {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #f8f9fa;
  border-radius: 6px;
  border: 1px solid #ebeef5;
  margin-top: 15px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.stat-label {
  font-size: 14px;
  color: #606266;
}

.stat-value {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.stat-value.positive {
  color: #67C23A;
}

.stat-value.negative {
  color: #F56C6C;
}

/* ========== 创建模式样式（保持不变） ========== */
/* 概览卡片 */
.overview-card {
  margin-bottom: 20px;
}

.overview-content {
  padding: 10px 0;
}

.overview-item {
  margin-bottom: 15px;
}

.overview-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 6px;
}

.overview-value {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.overview-value.highlight {
  color: #409EFF;
  font-weight: bold;
}

.protection-notice {
  margin-top: 20px;
  padding: 15px;
  background-color: #f0f9ff;
  border-radius: 6px;
  border-left: 4px solid #409EFF;
}

.protection-details p {
  margin: 5px 0;
  font-size: 14px;
}

.expired-warning {
  margin-top: 10px;
  padding: 8px;
  background-color: #fef0f0;
  color: #f56c6c;
  border-radius: 4px;
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 统计卡片 */
.stats-cards {
  margin-bottom: 30px;
}

.stat-card {
  border-radius: 8px;
  border: none;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
}

.stat-icon {
  font-size: 40px;
  opacity: 0.8;
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

/* 处理步骤 */
.process-steps {
  margin-bottom: 30px;
  padding: 20px;
  background: white;
  border-radius: 8px;
}

/* 差异项列表 */
.diff-list-card {
  height: calc(100vh - 450px);
  display: flex;
  flex-direction: column;
}

.diff-list-card .card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-tabs {
  display: flex;
  align-items: center;
}

.batch-actions-bar {
  margin-bottom: 15px;
  padding: 12px 15px;
  background-color: #f0f9ff;
  border-radius: 6px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.batch-info {
  font-size: 14px;
}

.selected-count {
  font-weight: bold;
  color: #409EFF;
  font-size: 16px;
  margin: 0 4px;
}

.selected-summary {
  color: #666;
  font-size: 13px;
  margin-left: 10px;
}

.batch-buttons {
  display: flex;
  gap: 10px;
}

.diff-items-table {
  flex: 1;
  margin-bottom: 15px;
}

.diff-items-table :deep(.el-table__row.row-processing) {
  background-color: #fdf6ec !important;
}

.diff-items-table :deep(.el-table__row.row-processed) {
  background-color: #f0f9eb !important;
}

.product-color {
  font-size: 12px;
  color: #666;
  margin-top: 2px;
}

.diff-positive {
  color: #67C23A;
  font-weight: bold;
}

.diff-negative {
  color: #F56C6C;
  font-weight: bold;
}

.adjustment-link {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.adjustment-status {
  display: flex;
  justify-content: center;
}

.no-adjustment {
  color: #999;
  font-style: italic;
}

.item-actions {
  display: flex;
  justify-content: center;
}

.pagination-section {
  padding-top: 15px;
  border-top: 1px solid #ebeef5;
  display: flex;
  justify-content: center;
}

/* 手动创建调整单表单 */
.manual-create-card {
  margin-bottom: 20px;
}

.manual-create-form {
  padding: 20px;
}

.manual-items-card {
  margin-bottom: 20px;
}

.no-items {
  padding: 40px 20px;
  text-align: center;
}

.manual-items-table {
  width: 100%;
  margin-bottom: 15px;
}

.quantity-positive, .after-positive {
  color: #67C23A;
  font-weight: bold;
}

.quantity-negative {
  color: #F56C6C;
  font-weight: bold;
}

.quantity-zero, .after-zero {
  color: #909399;
}

.adjust-positive {
  color: #409EFF;
  font-weight: bold;
}

.adjust-negative {
  color: #F56C6C;
  font-weight: bold;
}

.adjust-zero {
  color: #909399;
}

.amount-positive {
  color: #67C23A;
  font-weight: bold;
}

.amount-negative {
  color: #F56C6C;
  font-weight: bold;
}

.amount-zero {
  color: #909399;
}

.no-batch, .no-shelf {
  color: #999;
  font-style: italic;
  font-size: 12px;
}

/* 调整明细统计 */
.items-statistics {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #f8f9fa;
  border-radius: 6px;
  border: 1px solid #ebeef5;
  margin-top: 15px;
}

/* 盘点单选项样式 */
.stock-take-option {
  display: flex;
  flex-direction: column;
  padding: 4px 0;
}

.option-no {
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 2px;
}

.option-info {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #666;
}

.option-info .warehouse {
  color: #67C23A;
}

.option-info .time {
  color: #999;
}

/* 调整单操作面板 */
.adjustment-create-card .card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.create-form {
  padding: 10px 0;
}

.no-selection {
  padding: 40px 20px;
  text-align: center;
}

.adjustment-history-card {
  max-height: 400px;
  overflow-y: auto;
}

.order-item {
  padding: 12px;
  margin-bottom: 10px;
  border: 1px solid #e6e6e6;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
}

.order-item:hover {
  background-color: #f5f7fa;
  border-color: #409EFF;
}

.order-item.active {
  background-color: #f0f9ff;
  border-color: #409EFF;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.order-no {
  font-family: 'Courier New', monospace;
  font-weight: bold;
  color: #409EFF;
  font-size: 14px;
}

.order-info {
  font-size: 12px;
  color: #666;
}

.info-item {
  margin-bottom: 4px;
  display: flex;
  justify-content: space-between;
}

.info-label {
  color: #999;
}

.info-value {
  font-weight: 500;
}

.no-history {
  padding: 30px 0;
  text-align: center;
}

/* 商品选择对话框 */
.product-select-dialog {
  padding: 10px;
}

.product-filter {
  margin-bottom: 16px;
}

.product-list-container {
  max-height: 400px;
  overflow-y: auto;
  margin-bottom: 16px;
}

.product-pagination {
  display: flex;
  justify-content: flex-end;
}

/* 批量确认对话框 */
.batch-confirm-content {
  padding: 10px 0;
}

.confirm-stats {
  margin: 20px 0;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 6px;
}

.stat-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  font-size: 14px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .main-operation .el-col {
    margin-bottom: 20px;
  }
  
  .main-operation .el-col-16,
  .main-operation .el-col-8 {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 15px;
    align-items: flex-start;
  }
  
  .header-actions {
    width: 100%;
    justify-content: flex-end;
  }
  
  .stats-cards .el-col {
    margin-bottom: 15px;
  }
  
  .batch-actions-bar {
    flex-direction: column;
    gap: 10px;
    align-items: flex-start;
  }
  
  .batch-buttons {
    width: 100%;
    justify-content: flex-end;
  }
  
  .items-statistics {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .header-summary {
    flex-direction: column;
    gap: 8px;
    align-items: flex-start;
    margin-top: 10px;
  }
}
</style>