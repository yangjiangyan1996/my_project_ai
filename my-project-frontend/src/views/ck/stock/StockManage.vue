<template>
  <div class="stock-take-manage-container">
    <el-card class="manage-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">库存盘点管理</span>
          <div class="header-actions">
            <el-button 
              type="primary" 
              @click="handleCreate"
            >
              <el-icon><Plus /></el-icon>
              新建盘点单
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
          <el-form-item label="盘点单号">
            <el-input
              v-model="filterForm.stockTakeNo"
              placeholder="请输入盘点单号"
              clearable
              style="width: 180px"
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
          <el-form-item label="盘点类型">
            <el-select
              v-model="filterForm.takeType"
              placeholder="全部类型"
              clearable
              style="width: 120px"
            >
              <el-option
                v-for="type in takeTypeOptions"
                :key="type.value"
                :label="type.label"
                :value="type.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="盘点范围">
            <el-select
              v-model="filterForm.takeScope"
              placeholder="全部范围"
              clearable
              style="width: 120px"
            >
              <el-option
                v-for="scope in takeScopeOptions"
                :key="scope.value"
                :label="scope.label"
                :value="scope.value"
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
          <el-table-column label="盘点单号" width="180" fixed="left">
            <template #default="{ row }">
              <span class="stock-take-no">{{ row.stockTakeNo }}</span>
            </template>
          </el-table-column>
          <el-table-column label="仓库" width="150">
            <template #default="{ row }">
              <span>{{ row.warehouseName }}</span>
            </template>
          </el-table-column>
          <el-table-column label="盘点类型" width="100" align="center">
            <template #default="{ row }">
              <el-tag 
                :type="row.takeType === 1 ? 'primary' : 'warning'" 
                size="small"
              >
                {{ row.takeType === 1 ? '动态盘' : '静态盘' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="盘点范围" width="120" align="center">
            <template #default="{ row }">
              <span>{{ getScopeLabel(row.takeScope) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="盘点明细" width="120" align="center">
            <template #default="{ row }">
              <span class="detail-info">
                {{ row.itemCount || 0 }} 条
              </span>
            </template>
          </el-table-column>

          <el-table-column label="盘点完成" width="120" align="center">
            <template #default="{ row }">
              <span class="detail-info">
                {{ row.countedCount || 0 }} 条
              </span>
            </template>
          </el-table-column>

          <el-table-column label="差异数量" width="120" align="center">
            <template #default="{ row }">
              <span :class="{
                'diff-positive': row.totalDiff > 0,
                'diff-negative': row.totalDiff < 0,
                'diff-zero': row.totalDiff === 0
              }">
                {{ formatNumber(row.totalDiff) }}
              </span>
            </template>
          </el-table-column>

          <el-table-column label="审核状态" width="120" align="center">
            <template #default="{ row }">
              <el-tag 
                :type="getApproveStatusTagType(row.approvalStatus)" 
                size="small"
              >
                {{ getApproveStatusLabel(row.approvalStatus) }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column label="盘点状态" width="120" align="center">
            <template #default="{ row }">
              <el-tag 
                :type="getStatusTagType(row.takeStatus)" 
                size="small"
              >
                {{ getStatusLabel(row.takeStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="创建人" width="120">
            <template #default="{ row }">
              <span>{{ row.createdByName }}</span>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" width="160">
            <template #default="{ row }">
              <span>{{ formatDateTime(row.createdAt) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="450" fixed="right" align="center">
            <template #default="{ row }">
              <div class="action-buttons">
                <!-- 基础操作 -->
                <el-button
                  type="primary"
                  link
                  size="small"
                  @click="handleView(row)"
                >
                  查看
                </el-button>
                
                <!-- 差异调整 -->
                <el-button
                  type="warning"
                  link
                  size="small"
                  @click="handleAdjustDifference(row)"
                >
                  调整差异
                </el-button>
                
                <el-button
                  type="success"
                  link
                  size="small"
                  disabled
                >
                  已调整完成
                </el-button>
                
                <!-- 编辑相关 -->
                <el-button
                  type="warning"
                  link
                  size="small"
                  @click="handleEdit(row)"
                >
                  编辑
                </el-button>
                
                <el-button
                  type="success"
                  link
                  size="small"
                  @click="handleSubmit(row)"
                >
                  提交审批
                </el-button>
                
                <el-button
                  type="info"
                  link
                  size="small"
                  @click="handleApprove(row)"
                >
                  审批通过
                </el-button>
                
                <el-button
                  type="danger"
                  link
                  size="small"
                  @click="handleCancel(row)"
                >
                  删除
                </el-button>
                
                <!-- 盘点相关 -->
                <el-button
                  type="primary"
                  link
                  size="small"
                  @click="handleInitialize(row)"
                >
                  初始化盘点
                </el-button>
                
                <el-button
                  type="primary"
                  link
                  size="small"
                  @click="handleEnterData(row)"
                >
                  录入数据
                </el-button>
                
                <el-button
                  type="success"
                  link
                  size="small"
                  @click="handleComplete(row)"
                >
                  完成盘点
                </el-button>
                
                <!-- 复核确认 -->
                <el-button
                  type="warning"
                  link
                  size="small"
                  @click="handleReview(row)"
                >
                  提交复核
                </el-button>
                
                <el-button
                  type="success"
                  link
                  size="small"
                  @click="handleConfirm(row)"
                >
                  审核确认
                </el-button>
                
                <!-- 取消操作 -->
                <el-button
                  type="danger"
                  link
                  size="small"
                  @click="handleCancel(row)"
                >
                  取消
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

    <!-- 创建/编辑盘点单对话框 -->
    <el-dialog
      v-model="formDialogVisible"
      :title="formTitle"
      width="800px"
      top="5vh"
      class="stock-take-form-dialog"
    >
      <div class="stock-take-form-container">
        <el-form
          ref="formRef"
          :model="formData"
          :rules="formRules"
          label-width="120px"
          class="compact-form"
        >
          <!-- 基本信息 -->
          <el-card class="form-card" shadow="never">
            <template #header>
              <div class="card-header">
                <span class="card-title">基本信息</span>
              </div>
            </template>
            
            <div class="form-grid">
              <div class="form-group">
                <el-form-item label="盘点单号" prop="stockTakeNo">
                  <el-input
                    v-model="formData.stockTakeNo"
                    placeholder="系统自动生成"
                    disabled
                    class="form-input"
                  />
                </el-form-item>
              </div>
              
              <div class="form-group">
                <el-form-item label="仓库" prop="warehouseId">
                  <el-select
                    v-model="formData.warehouseId"
                    placeholder="请选择仓库"
                    class="form-select"
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
              </div>
              
              <div class="form-group">
                <el-form-item label="盘点类型" prop="takeType">
                  <el-radio-group v-model="formData.takeType">
                    <el-radio :label="1">动态盘</el-radio>
                    <el-radio :label="2">静态盘</el-radio>
                  </el-radio-group>
                  <div class="type-tips">
                    <el-icon><InfoFilled /></el-icon>
                    <span v-if="formData.takeType === 1">动态盘：盘点期间允许出入库</span>
                    <span v-if="formData.takeType === 2">静态盘：盘点期间锁定库存</span>
                  </div>
                </el-form-item>
              </div>
              
              <div class="form-group full-width">
                <el-form-item label="盘点范围" prop="takeScope">
                  <el-radio-group v-model="formData.takeScope">
                    <el-radio :label="1">全部</el-radio>
                    <el-radio :label="2">批次</el-radio>
                    <el-radio :label="3">货架</el-radio>
                    <el-radio :label="4">商品</el-radio>
                  </el-radio-group>
                </el-form-item>
              </div>
              

              <!-- 新增：阈值设置 -->
        <!-- <div class="form-group">
          <el-form-item label="差异阈值" prop="thresholdValue">
            <el-input-number
              v-model="formData.thresholdValue"
              :min="0"
              :step="1"
              :precision="0"
              placeholder="请输入阈值"
              class="form-input"
            />
            <div class="input-tips">当盘点差异超过此数值时提示</div>
          </el-form-item>
        </div>
        
        <div class="form-group">
          <el-form-item label="阈值单位" prop="thresholdUnit">
            <el-select
              v-model="formData.thresholdUnit"
              placeholder="请选择单位"
              class="form-select"
            >
              <el-option label="个" value="个" />
              <el-option label="件" value="件" />
              <el-option label="箱" value="箱" />
              <el-option label="百分比" value="%" />
            </el-select>
            <div class="input-tips">阈值的计量单位</div>
          </el-form-item>
        </div>
        
        <div class="form-group full-width">
          <el-form-item label="超阈值复盘" prop="recheckFlag">
            <el-radio-group v-model="formData.recheckFlag">
              <el-radio :label="true">是</el-radio>
              <el-radio :label="false">否</el-radio>
            </el-radio-group>
            <div class="type-tips">
              <el-icon><InfoFilled /></el-icon>
              <span>当差异超过阈值时，是否需要进行复盘确认</span>
            </div>
          </el-form-item>
        </div> -->
              <!-- 动态范围选择 -->
              <template v-if="formData.takeScope === 2 && formData.warehouseId">
                <div class="form-group full-width">
                  <el-form-item label="选择批次" prop="batchIds">
                    <el-select
                      v-model="formData.batchIds"
                      placeholder="请选择批次"
                      multiple
                      filterable
                      class="form-select"
                      @focus="loadBatchOptions"
                    >
                      <el-option
                        v-for="batch in batchOptions"
                        :key="batch.id"
                        :label="batch.name"
                        :value="batch.id"
                      />
                    </el-select>
                  </el-form-item>
                </div>
              </template>
              
              <template v-if="formData.takeScope === 3 && formData.warehouseId">
                <div class="form-group full-width">
                  <el-form-item label="选择货架" prop="shelfIds">
                    <el-select
                      v-model="formData.shelfIds"
                      placeholder="请选择货架"
                      multiple
                      filterable
                      class="form-select"
                      @focus="loadShelfOptions"
                    >
                      <el-option
                        v-for="shelf in shelfOptions"
                        :key="shelf.id"
                        :label="shelf.name"
                        :value="shelf.id"
                      />
                    </el-select>
                  </el-form-item>
                </div>
              </template>
              
              <template v-if="formData.takeScope === 4 && formData.warehouseId">
                <div class="form-group full-width">
                  <el-form-item label="选择商品" prop="productIds">
                    <el-select
                      v-model="formData.productIds"
                      placeholder="请选择商品"
                      multiple
                      filterable
                      class="form-select"
                      @focus="loadProductOptions"
                    >
                      <el-option
                        v-for="product in productOptions"
                        :key="product.id"
                        :label="product.name"
                        :value="product.id"
                      />
                    </el-select>
                  </el-form-item>
                </div>
              </template>
              
              <div class="form-group full-width">
                <el-form-item label="备注" prop="remark">
                  <el-input
                    v-model="formData.remark"
                    type="textarea"
                    :rows="3"
                    placeholder="请输入备注信息"
                    maxlength="200"
                    show-word-limit
                    resize="none"
                    class="remark-textarea"
                  />
                </el-form-item>
              </div>
            </div>
          </el-card>

          <!-- 盘点预览 -->
          <el-card class="form-card" shadow="never" v-if="showPreview">
            <template #header>
              <div class="card-header">
                <span class="card-title">盘点范围预览</span>
                <el-button
                  type="primary"
                  link
                  size="small"
                  @click="refreshPreview"
                >
                  <el-icon><Refresh /></el-icon>
                  刷新预览
                </el-button>
              </div>
            </template>
            
            <div class="preview-content">
              <div v-if="previewLoading" class="preview-loading">
                <el-icon class="loading-icon"><Loading /></el-icon>
                加载中...
              </div>
              
              <div v-else-if="previewData.length > 0" class="preview-list">
                <div class="preview-header">
                  <span class="preview-title">预估盘点项目：{{ previewData.length }} 个</span>
                  <span class="preview-total">总库存数量：{{ formatNumber(totalPreviewQuantity) }}</span>
                </div>
                
                <el-table
                  :data="previewData.slice(0, 5)"
                  height="200"
                  class="preview-table"
                >
                  <el-table-column label="SKU" width="120">
                    <template #default="{ row }">
                      {{ row.sku || '--' }}
                    </template>
                  </el-table-column>
                  <el-table-column label="产品名称" width="150">
                    <template #default="{ row }">
                      {{ row.productName || '--' }}
                    </template>
                  </el-table-column>
                  <el-table-column label="批次号" width="100">
                    <template #default="{ row }">
                      {{ row.batchNo || '--' }}
                    </template>
                  </el-table-column>
                  <el-table-column label="货架" width="100">
                    <template #default="{ row }">
                      {{ row.shelfCode || '--' }}
                    </template>
                  </el-table-column>
                  <el-table-column label="库位" width="100">
                    <template #default="{ row }">
                      {{ row.locationCode || '--' }}
                    </template>
                  </el-table-column>
                  <el-table-column label="系统库存" align="right" width="100">
                    <template #default="{ row }">
                      {{ formatNumber(row.quantity) }}
                    </template>
                  </el-table-column>
                </el-table>
                
                <div v-if="previewData.length > 5" class="preview-more">
                  ... 还有 {{ previewData.length - 5 }} 项
                </div>
              </div>
              
              <div v-else class="preview-empty">
                <el-icon><Box /></el-icon>
                <span>暂无库存数据</span>
              </div>
            </div>
          </el-card>
        </el-form>
      </div>
      
      <template #footer>
        <div class="form-dialog-footer">
          <el-button @click="formDialogVisible = false" class="cancel-btn">取消</el-button>
          <el-button 
            type="primary" 
            @click="handleFormSubmit" 
            :loading="formLoading"
            class="submit-btn"
          >
            保存
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 查看盘点单详情对话框 -->
    <el-dialog
      v-model="viewDialogVisible"
      title="盘点单详情"
      width="1100px"
      top="5vh"
      class="stock-take-view-dialog"
    >
      <div class="stock-take-view-container" v-loading="viewLoading">
        <!-- 基本信息卡片 -->
        <el-card class="basic-info-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span class="card-title">基本信息</span>
              <div class="header-status">
                <el-tag 
                  :type="getStatusTagType(currentStockTake.takeStatus)" 
                  size="large"
                >
                  {{ getStatusLabel(currentStockTake.takeStatus) }}
                </el-tag>
                <el-tag 
                  :type="currentStockTake.takeType === 1 ? 'primary' : 'warning'" 
                  size="large"
                >
                  {{ currentStockTake.takeType === 1 ? '动态盘' : '静态盘' }}
                </el-tag>
              </div>
            </div>
          </template>
          <div class="info-grid">
            <div class="info-item">
              <span class="info-label">盘点单号</span>
              <span class="info-value highlight">{{ currentStockTake.stockTakeNo }}</span>
            </div>

             <div class="info-item">
              <span class="info-label">审核状态</span>
              <span class="info-value highlight">{{ getApproveStatusLabel(currentStockTake.approvalStatus) }}</span>
            </div>

             <div class="info-item">
              <span class="info-label">盘点状态</span>
              <span class="info-value highlight">{{ getStatusLabel(currentStockTake.takeStatus) }}</span>
            </div>

            <div class="info-item">
              <span class="info-label">仓库</span>
              <span class="info-value">{{ currentStockTake.warehouseName }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">盘点范围</span>
              <span class="info-value">{{ getScopeLabel(currentStockTake.takeScope) }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">创建人</span>
              <span class="info-value">{{ currentStockTake.createdByName }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">创建时间</span>
              <span class="info-value">{{ formatDateTime(currentStockTake.createdAt) }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">修改时间</span>
              <span class="info-value">{{ formatDateTime(currentStockTake.modifiedAt) }}</span>
            </div>
            <div class="info-item full-width">
              <span class="info-label">备注</span>
              <span class="info-value">{{ currentStockTake.remark || '无' }}</span>
            </div>
          </div>
        </el-card>

        <!-- 统计信息卡片 -->
        <el-card class="stats-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span class="card-title">盘点统计</span>
            </div>
          </template>
          <div class="stats-grid">
            <div class="stat-item">
              <div class="stat-icon primary">
                <el-icon><Document /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ detailPagination.total || 0 }}</div>
                <div class="stat-label">盘点项总数</div>
              </div>
            </div>
            <div class="stat-item">
              <div class="stat-icon success">
                <el-icon><Check /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ countedItemCount }}</div>
                <div class="stat-label">已盘数量</div>
              </div>
            </div>
            <div class="stat-item">
              <div class="stat-icon warning">
                <el-icon><Clock /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ pendingItemCount }}</div>
                <div class="stat-label">未盘数量</div>
              </div>
            </div>
            <div class="stat-item">
              <div class="stat-icon" :class="getDiffClass(totalDiff)">
                <el-icon><TrendCharts /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ formatNumber(totalDiff) }}</div>
                <div class="stat-label">总差异数量</div>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 盘点明细表格 -->
        <el-card class="detail-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span class="card-title">盘点明细</span>
              <div class="header-actions">
                <el-button
                  v-if="currentStockTake.status === 3"
                  type="primary"
                  size="small"
                  @click="handleBatchEnter"
                >
                  批量录入
                </el-button>
                <el-button
                  v-if="currentStockTake.status === 3"
                  type="success"
                  size="small"
                  @click="handleExportData"
                >
                  导出数据
                </el-button>
              </div>
            </div>
          </template>
          
          <div class="detail-table-container">
            <el-table
              :data="detailList"
              v-loading="detailLoading"
              empty-text="暂无盘点明细"
              class="detail-table"
              height="400"
            >
              <el-table-column type="index" label="序号" width="60" align="center" />
              <el-table-column label="SKU" width="120">
                <template #default="{ row }">
                  {{ row.productSku || '--' }}
                </template>
              </el-table-column>
              <el-table-column label="产品名称" width="150">
                <template #default="{ row }">
                  {{ row.productName || '--' }}
                </template>
              </el-table-column>
              <el-table-column label="批次号" width="100">
                <template #default="{ row }">
                  {{ row.batchNo || '--' }}
                </template>
              </el-table-column>
              <el-table-column label="货架" width="100">
                <template #default="{ row }">
                  {{ row.shelfCode || '--' }}
                </template>
              </el-table-column>
              <el-table-column label="库位" width="100">
                <template #default="{ row }">
                  {{ row.locationCode || '--' }}
                </template>
              </el-table-column>
              <el-table-column label="系统数量" width="100" align="right">
                <template #default="{ row }">
                  {{ formatNumber(row.systemQuantity) }}
                </template>
              </el-table-column>
              <el-table-column label="实盘数量" width="120" align="center">
                <template #default="{ row }">
                  <template v-if="currentStockTake.status >= 3 && currentStockTake.status !== 5">
                    <el-input-number
                      v-if="row.status === 1 || row.status === 2"
                      v-model="row.countedQuantity"
                      :min="0"
                      :precision="4"
                      size="small"
                      style="width: 100px"
                      @change="handleQuantityChange(row)"
                    />
                    <span v-else>{{ row.countedQuantity !== null ? formatNumber(row.countedQuantity) : '--' }}</span>
                  </template>
                  <span v-else>{{ row.countedQuantity !== null ? formatNumber(row.countedQuantity) : '--' }}</span>
                </template>
              </el-table-column>
              <el-table-column label="差异数量" width="100" align="right">
                <template #default="{ row }">
                  <span :class="getDiffClass(row.diffQuantity)">
                    {{ formatNumber(row.diffQuantity) }}
                  </span>
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
              <el-table-column label="操作" width="180" align="center" fixed="right">
                <template #default="{ row }">
                  <div class="item-actions">
                    <el-button
                      v-if="currentStockTake.status === 3 && row.status !== 3 && row.status !== undefined"
                      type="primary"
                      link
                      size="small"
                      @click="handleSaveItem(row)"
                    >
                      保存
                    </el-button>
                    <el-button
                      v-if="currentStockTake.status === 3 && row.status === 2"
                      type="success"
                      link
                      size="small"
                      @click="handleConfirmItem(row)"
                    >
                      确认
                    </el-button>
                    <el-button
                      v-if="currentStockTake.status === 3 && row.status === 3"
                      type="warning"
                      link
                      size="small"
                      @click="handleRevertItem(row)"
                    >
                      撤销确认
                    </el-button>
                    
                  </div>
                </template>
              </el-table-column>
            </el-table>
            
            <div class="detail-pagination">
              <el-pagination
                v-model:current-page="detailPagination.current"
                v-model:page-size="detailPagination.size"
                :total="detailPagination.total"
                :page-sizes="[20, 50, 100]"
                layout="total, sizes, prev, pager, next"
                @size-change="handleDetailSizeChange"
                @current-change="handleDetailCurrentChange"
              />
            </div>
          </div>
        </el-card>

        <!-- 操作日志卡片 -->
        <el-card class="log-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span class="card-title">操作日志</span>
            </div>
          </template>
          
          <div class="log-list">
            <el-timeline>
              <el-timeline-item
                v-for="log in logList"
                :key="log.id"
                :timestamp="formatDateTime(log.createdAt)"
                placement="top"
              >
                <div class="log-item">
                  <div class="log-action">{{ log.actionType || '操作' }}</div>
                  <div class="log-desc">{{ log.actionDesc || '无描述' }}</div>
                  <div class="log-user">操作人：{{ log.createdByName || '系统' }}</div>
                </div>
              </el-timeline-item>
              <el-timeline-item v-if="logList.length === 0">
                <div class="log-item">
                  <div class="log-desc">暂无操作日志</div>
                </div>
              </el-timeline-item>
            </el-timeline>
          </div>
        </el-card>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="viewDialogVisible = false">关闭</el-button>
          <template v-if="currentStockTake.status === 1">
            <el-button type="warning" @click="handleEdit(currentStockTake)">编辑</el-button>
            <el-button type="success" @click="handleSubmit(currentStockTake)">提交审批</el-button>
            <el-button type="danger" @click="handleCancel(currentStockTake)">删除</el-button>
          </template>
          <template v-if="currentStockTake.status === 2">
            <el-button type="primary" @click="handleInitialize(currentStockTake)">初始化盘点</el-button>
            <el-button type="danger" @click="handleCancel(currentStockTake)">取消盘点</el-button>
          </template>
          <template v-if="currentStockTake.status === 3">
            <el-button type="success" @click="handleComplete(currentStockTake)">完成盘点</el-button>
            <el-button type="primary" @click="handleEnterData(currentStockTake)">录入数据</el-button>
          </template>
          <template v-if="currentStockTake.status === 4">
            <el-button type="warning" @click="handleReview(currentStockTake)">提交复核</el-button>
          </template>
          <template v-if="currentStockTake.status === 5">
            <el-button type="info" disabled>已完成</el-button>
          </template>
        </div>
      </template>
    </el-dialog>

    <!-- 批量录入对话框 -->
    <el-dialog
      v-model="batchDialogVisible"
      title="批量录入实盘数量"
      width="600px"
      top="10vh"
    >
      <div class="batch-enter-container">
        <el-alert
          title="操作说明"
          type="info"
          description="请下载模板，填写实盘数量后上传。系统将自动更新盘点明细。"
          show-icon
          closable
          class="batch-alert"
        />
        
        <div class="batch-actions">
          <el-button type="primary" @click="downloadTemplate">
            <el-icon><Download /></el-icon>
            下载模板
          </el-button>
          <el-upload
            class="batch-upload"
            action="#"
            :show-file-list="false"
            :before-upload="beforeUpload"
            :http-request="handleUpload"
          >
            <el-button type="success">
              <el-icon><Upload /></el-icon>
              上传数据
            </el-button>
          </el-upload>
        </div>
        
        <div class="batch-preview" v-if="uploadData.length > 0">
          <div class="preview-header">
            <span class="preview-title">数据预览（前10条）</span>
            <el-button type="primary" link @click="clearUploadData">
              清空
            </el-button>
          </div>
          <el-table :data="uploadData.slice(0, 10)" height="200" border>
            <el-table-column prop="sku" label="SKU" width="100" />
            <el-table-column prop="batchNo" label="批次号" width="100" />
            <el-table-column prop="countedQuantity" label="实盘数量" width="100" />
          </el-table>
        </div>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="batchDialogVisible = false">取消</el-button>
          <el-button 
            type="primary" 
            @click="handleBatchSave"
            :disabled="uploadData.length === 0"
            :loading="batchLoading"
          >
            确认保存
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { useRouter, useRoute } from 'vue-router';
import { ref, reactive, computed, onMounted, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';

import { 
  Plus, 
  Refresh, 
  InfoFilled,
  Loading,
  Box,
  Document,
  Check,
  Clock,
  TrendCharts,
  Download,
  Upload
} from '@element-plus/icons-vue';
import { post, get } from '@/net';

const loading = ref(false);
const formLoading = ref(false);
const formDialogVisible = ref(false);
const viewDialogVisible = ref(false);
const batchDialogVisible = ref(false);
const viewLoading = ref(false);
const detailLoading = ref(false);
const batchLoading = ref(false);
const productLoading = ref(false);
const previewLoading = ref(false);
const formRef = ref();
const isEdit = ref(false);
const showPreview = ref(false);
const router = useRouter();

// 筛选表单
const filterForm = reactive({
  stockTakeNo: '',
  warehouseId: '',
  takeType: '',
  takeScope: '',
  status: '',
  dateRange: []
});

// 主分页信息
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
});

// 明细分页信息
const detailPagination = reactive({
  current: 1,
  size: 20,
  total: 0
});

// 表单数据
const formData = reactive({
  id: '',
  stockTakeNo: '',
  warehouseId: '',
  takeType: 1,
  takeScope: 1,
  batchIds: [],  // 批次ID列表
  shelfIds: [],  // 货架ID列表
  productIds: [], // 商品ID列表
  remark: ''
});

// 预览数据
const previewData = ref([]);

// 选项数据
const batchOptions = ref([]);     // 批次选项：{id, name}
const shelfOptions = ref([]);     // 货架选项：{id, name}
const productOptions = ref([]);   // 商品选项：{id, name}

// 当前查看的盘点单
const currentStockTake = reactive({
  id: '',
  stockTakeNo: '',
  warehouseId: '',
  warehouseName: '',
  takeType: 1,
  takeScope: 1,
  status: 1,
  remark: '',
  itemCount: 0,
  countedCount: 0,
  pendingCount: 0,
  totalDiff: 0,
  createdByName: '',
  createdAt: '',
  modifiedAt: ''
});

// 明细列表
const detailList = ref([]);
// 日志列表
const logList = ref([]);
// 上传数据
const uploadData = ref([]);

// 仓库列表
const warehouseList = ref([]);
// 盘点单列表
const stockTakeList = ref([]);

// 选项数据
const takeTypeOptions = [
  { value: 1, label: '动态盘' },
  { value: 2, label: '静态盘' }
];
const takeScopeOptions = [
  { value: 1, label: '全部' },
  { value: 2, label: '批次' },
  { value: 3, label: '货架' },
  { value: 4, label: '商品' }
];
const statusOptions = [
  { value: 1, label: '新建' },
  { value: 2, label: '盘点中' },
  { value: 3, label: '待确认' },
  { value: 4, label: '已完成' },
  { value: 5, label: '已取消' }
];

// 表单验证规则
const formRules = {
  warehouseId: [
    { required: true, message: '请选择仓库', trigger: 'change' }
  ],
  takeType: [
    { required: true, message: '请选择盘点类型', trigger: 'change' }
  ],
  takeScope: [
    { required: true, message: '请选择盘点范围', trigger: 'change' }
  ],
  batchIds: [
    { 
      validator: (rule, value, callback) => {
        if (formData.takeScope === 2 && (!value || value.length === 0)) {
          callback(new Error('请选择批次'));
        } else {
          callback();
        }
      },
      trigger: 'change'
    }
  ],
  shelfIds: [
    { 
      validator: (rule, value, callback) => {
        if (formData.takeScope === 3 && (!value || value.length === 0)) {
          callback(new Error('请选择货架'));
        } else {
          callback();
        }
      },
      trigger: 'change'
    }
  ],
  productIds: [
    { 
      validator: (rule, value, callback) => {
        if (formData.takeScope === 4 && (!value || value.length === 0)) {
          callback(new Error('请选择商品'));
        } else {
          callback();
        }
      },
      trigger: 'change'
    }
  ]
};

// 计算属性
const formTitle = computed(() => {
  return isEdit.value ? '编辑盘点单' : '新建盘点单';
});

const totalPreviewQuantity = computed(() => {
  return previewData.value.reduce((sum, item) => sum + (item.quantity || 0), 0);
});

// 统计相关计算属性
const countedItemCount = computed(() => {
  return detailList.value.filter(item => item.status === 2 || item.status === 3).length;
});

const pendingItemCount = computed(() => {
  return detailList.value.filter(item => item.status === 1).length;
});

const totalDiff = computed(() => {
  return detailList.value.reduce((sum, item) => sum + (item.diffQuantity || 0), 0);
});

// 方法
const loadStockTakeList = async () => {
  loading.value = true;
  try {
    const params = {
      page: pagination.current,
      size: pagination.size,
      ...filterForm
    };
    
    if (filterForm.dateRange && filterForm.dateRange.length === 2) {
      params.startDate = filterForm.dateRange[0];
      params.endDate = filterForm.dateRange[1];
    }
    
    const res = await post('/api/auth/stock/pageList', params);
    if (res && res.records) {
      stockTakeList.value = res.records.map(item => ({
        id: item.id || '',
        approvalStatus: item.approvalStatus || 1,
        takeStatus: item.takeStatus || 1,
        stockTakeNo: item.stockTakeNo || '',
        warehouseId: item.warehouseId || '',
        warehouseName: item.warehouseName || '',
        takeType: item.takeType || 1,
        takeScope: item.takeScope || 1,
        status: item.status || 1,
        remark: item.remark || '',
        itemCount: item.itemCount || 0,
        countedCount: item.countedCount || 0,
        pendingCount: item.pendingCount || 0,
        totalDiff: item.totalDiff || 0,
        createdByName: item.createdByName || '',
        createdAt: item.createdAt || '',
        modifiedAt: item.modifiedAt || '',
        scopeValues: item.scopeValues || null  // 添加scopeValues字段
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

const loadBatchOptions = async () => {
  if (!formData.warehouseId) {
    ElMessage.warning('请先选择仓库');
    return;
  }
  
  try {
    const res = await get(`/api/auth/inventory/allBatchNoOfWareHouse?warehouseId=${formData.warehouseId}`);
    // 确保返回的是数组，并且每个对象都有id和name属性
    batchOptions.value = Array.isArray(res) ? res.map(item => ({
      id: item.id || '',
      name: item.name || ''
    })) : [];
    console.log('批次列表:', batchOptions.value);
  } catch (error) {
    console.error('加载批次列表失败:', error);
    batchOptions.value = [];
    ElMessage.error('加载批次列表失败');
  }
};

const loadShelfOptions = async () => {
  if (!formData.warehouseId) {
    ElMessage.warning('请先选择仓库');
    return;
  }
  
  try {
    const res = await get(`/api/auth/inventory/allShelfOfWareHouse?warehouseId=${formData.warehouseId}`);
     console.log('货架列表:', res);
    // 确保返回的是数组，并且每个对象都有id和name属性
    shelfOptions.value = Array.isArray(res) ? res.map(item => ({
      id: item.id || '',
      name: item.name || ''
    })) : [];
   
  } catch (error) {
    console.error('加载货架列表失败:', error);
    shelfOptions.value = [];
    ElMessage.error('加载货架列表失败');
  }
};

const loadProductOptions = async () => {
  if (!formData.warehouseId) {
    ElMessage.warning('请先选择仓库');
    return;
  }
  
  try {
    const res = await get(`/api/auth/inventory/allProductOfWareHouse?warehouseId=${formData.warehouseId}`);
    // 确保返回的是数组，并且每个对象都有id和name属性
    productOptions.value = Array.isArray(res) ? res.map(item => ({
      id: item.id || '',
      name: item.name || ''
    })) : [];
    console.log('商品列表:', productOptions.value);
  } catch (error) {
    console.error('加载商品列表失败:', error);
    productOptions.value = [];
    ElMessage.error('加载商品列表失败');
  }
};

const refreshPreview = async () => {
  // if (!formData.warehouseId) {
  //   ElMessage.warning('请先选择仓库');
  //   return;
  // }
  
  // previewLoading.value = true;
  // try {
  //   const params = {
  //     warehouseId: formData.warehouseId,
  //     takeScope: formData.takeScope
  //   };
    
  //   // 根据范围传递不同参数
  //   if (formData.takeScope === 2 && formData.batchIds.length > 0) {
  //     params.batchIds = formData.batchIds;
  //   } else if (formData.takeScope === 3 && formData.shelfIds.length > 0) {
  //     params.shelfIds = formData.shelfIds;
  //   } else if (formData.takeScope === 4 && formData.productIds.length > 0) {
  //     params.productIds = formData.productIds;
  //   }
    
  //   const res = await post('/api/auth/inventory/preview', params);
  //   previewData.value = res || [];
  // } catch (error) {
  //   console.error('加载预览数据失败:', error);
  //   ElMessage.error('加载预览数据失败');
  //   previewData.value = [];
  // } finally {
  //   previewLoading.value = false;
  // }
};

const loadDetailList = async (stockTakeId) => {
  detailLoading.value = true;
  try {
    const params = {
      stockTakeId,
      page: detailPagination.current,
      size: detailPagination.size
    };
    
    const res = await post('/api/auth/stock/itemPageList', params);
    if (res && res.records) {
      detailList.value = res.records.map(item => ({
        id: item.id || '',
        productId: item.productId || '',
        productSku: item.productSku || '',
        productName: item.productName || '',
        batchNo: item.batchNo || '',
        shelfId: item.shelfId || '',
        shelfCode: item.shelfCode || '',
        locationCode: item.locationCode || '',
        systemQuantity: item.systemQuantity || 0,
        countedQuantity: item.countedQuantity !== undefined ? item.countedQuantity : null,
        diffQuantity: item.diffQuantity || 0,
        status: item.status || 1
      }));
      detailPagination.total = res.total || 0;
    } else {
      detailList.value = [];
      detailPagination.total = 0;
    }
  } catch (error) {
    console.error('加载盘点明细失败:', error);
    ElMessage.error('加载盘点明细失败');
    detailList.value = [];
  } finally {
    detailLoading.value = false;
  }
};

const loadLogList = async (stockTakeId) => {
  try {
    const res = await get('/api/auth/stock/logList', { stockTakeId });
    logList.value = res || [];
  } catch (error) {
    console.error('加载操作日志失败:', error);
    logList.value = [];
  }
};

const refreshList = () => {
  pagination.current = 1;
  loadStockTakeList();
};

const handleSearch = () => {
  pagination.current = 1;
  loadStockTakeList();
};

const handleReset = () => {
  Object.assign(filterForm, {
    stockTakeNo: '',
    warehouseId: '',
    takeType: '',
    takeScope: '',
    status: '',
    dateRange: []
  });
  pagination.current = 1;
  loadStockTakeList();
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

const handleDetailSizeChange = (size) => {
  detailPagination.size = size;
  detailPagination.current = 1;
  loadDetailList(currentStockTake.id);
};

const handleDetailCurrentChange = (page) => {
  detailPagination.current = page;
  loadDetailList(currentStockTake.id);
};

const handleCreate = () => {
  isEdit.value = false;
  resetForm();
  formDialogVisible.value = true;
  showPreview.value = false;
};

const handleEdit = async (stockTake) => {
  isEdit.value = true;
  resetForm();
  
  // 先加载仓库数据
  if (!warehouseList.value.length) {
    await loadWarehouseList();
  }
  
  // 解析scopeValues
  let batchIds = [];
  let shelfIds = [];
  let productIds = [];
  
  if (stockTake.scopeValues) {
    try {
      const scopeValues = JSON.parse(stockTake.scopeValues);
      if (stockTake.takeScope === 2 && scopeValues.batchIds) {
        batchIds = scopeValues.batchIds;
      } else if (stockTake.takeScope === 3 && scopeValues.shelfIds) {
        shelfIds = scopeValues.shelfIds;
      } else if (stockTake.takeScope === 4 && scopeValues.productIds) {
        productIds = scopeValues.productIds;
      }
    } catch (e) {
      console.error('解析范围数据失败:', e);
    }
  }
  
  Object.assign(formData, {
    id: stockTake.id,
    stockTakeNo: stockTake.stockTakeNo,
    warehouseId: stockTake.warehouseId,
    takeType: stockTake.takeType,
    takeScope: stockTake.takeScope,
    batchIds,
    shelfIds,
    productIds,
    remark: stockTake.remark
  });
  
  formDialogVisible.value = true;
  showPreview.value = false;
  
  // 如果仓库已选择，加载对应的选项数据
  if (stockTake.warehouseId) {
    showPreview.value = true;
    
    // 根据盘点范围加载对应的选项数据
    if (stockTake.takeScope === 2) {
      await loadBatchOptions();
    } else if (stockTake.takeScope === 3) {
      await loadShelfOptions();
    } else if (stockTake.takeScope === 4) {
      await loadProductOptions();
    }
  }
};

const handleView = async (stockTake) => {
  try {
    viewLoading.value = true;
    Object.assign(currentStockTake, stockTake);
    
    // 重置分页
    detailPagination.current = 1;
    detailPagination.size = 20;
    
    // 加载明细和日志
    await Promise.all([
      loadDetailList(stockTake.id),
      loadLogList(stockTake.id)
    ]);
    
    viewDialogVisible.value = true;
  } catch (error) {
    console.error('加载盘点单详情失败:', error);
    ElMessage.error('加载盘点单详情失败');
  } finally {
    viewLoading.value = false;
  }
};

const handleSubmit = async (stockTake) => {
  try {
    await ElMessageBox.confirm(
      '确定要提交审批吗？提交后将进入盘点流程。',
      '提交确认',
      { type: 'warning' }
    );
    
    const res = await get('/api/auth/stock/submitApproval?id=' + stockTake.id);

    if (res) {
      ElMessage.success('提交成功');
      refreshList();
      if (viewDialogVisible.value) {
        viewDialogVisible.value = false;
      }
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('提交失败');
    }
  }
};

const handleApprove = async (stockTake) => {
  try {
    await ElMessageBox.confirm(
      '确定要审批通过吗？',
      '审批确认',
      { type: 'warning' }
    );
    
    const res = await get('/api/auth/stock/apprroveAndInitialize?id=' + stockTake.id);

    if (res) {
      ElMessage.success('审批通过并初始化成功');
      refreshList();
      if (viewDialogVisible.value) {
        viewDialogVisible.value = false;
      }
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('审批通过并初始化失败');
    }
  }
};

const handleInitialize = async (stockTake) => {
  try {
    await ElMessageBox.confirm(
      '确定要初始化盘点吗？系统将生成库存快照和盘点明细。',
      '初始化确认',
      { type: 'warning' }
    );
    
    const res = await get('/api/auth/stock/initialize?id=' + stockTake.id);
    if (res) {
      ElMessage.success('初始化成功');
      refreshList();
      if (viewDialogVisible.value) {
        await handleView(stockTake);
      }
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('初始化失败');
    }
  }
};

const handleAdjustDifference= async (stockTake) => {
  try {
    // 跳转到盘点数据录入页面
    router.push({
      name: 'ckStockAdjustment',
      params: {
        id: stockTake.id,
        stockTakeNo: stockTake.stockTakeNo
      }
    });
  } catch (error) {
    console.error('跳转页面失败:', error);
    ElMessage.error('跳转页面失败');
  }
};

const handleEnterData = async (stockTake) => {
  try {
    // 跳转到盘点数据录入页面
    router.push({
      name: 'ckStockOperateItem',
      params: {
        id: stockTake.id,
        stockTakeNo: stockTake.stockTakeNo
      }
    });
  } catch (error) {
    console.error('跳转页面失败:', error);
    ElMessage.error('跳转页面失败');
  }
};

const handleComplete = async (stockTake) => {
  try {
    // 检查是否有未盘点的项目
    if (pendingItemCount.value > 0) {
      await ElMessageBox.confirm(
        `还有 ${pendingItemCount.value} 项未盘点，确定要完成盘点吗？`,
        '完成确认',
        { type: 'warning' }
      );
    }
    
    const res = await get('/api/auth/stock/completeStockTake?id=' + stockTake.id);
    if (res) {
      ElMessage.success('完成盘点成功');
      refreshList();
      if (viewDialogVisible.value) {
        viewDialogVisible.value = false;
      }
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('完成盘点失败');
    }
  }
};

const handleReview = async (stockTake) => {
  try {
    await ElMessageBox.confirm(
      '确定要提交复核吗？提交后将进入审核阶段。',
      '复核确认',
      { type: 'warning' }
    );
    
    const res = await get('/api/auth/stock/reviewStockTake?id=' + stockTake.id);
    if (res) {
      ElMessage.success('提交复核成功');
      refreshList();
      if (viewDialogVisible.value) {
        viewDialogVisible.value = false;
      }
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('提交复核失败');
    }
  }
};

const handleConfirm = async (stockTake) => {
  try {
    await ElMessageBox.confirm(
      '确定要审核确认吗？系统将根据差异生成库存调整单。',
      '审核确认',
      { type: 'warning' }
    );
    
    const res = await get('/api/auth/stock/confirmStockTake?id=' + stockTake.id);
    if (res) {
      ElMessage.success('审核确认成功');
      refreshList();
      if (viewDialogVisible.value) {
        viewDialogVisible.value = false;
      }
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('审核确认失败');
    }
  }
};

const handleCancel = async (stockTake) => {
  try {
    const action = stockTake.status === 1 ? '删除' : '取消';
    await ElMessageBox.confirm(
      `确定要${action}盘点单"${stockTake.stockTakeNo}"吗？`,
      `${action}确认`,
      { type: 'warning' }
    );
    
    const res = await get('/api/auth/stock/cancelStockTake?id=' + stockTake.id);
    if (res) {
      ElMessage.success(`${action}成功`);
      refreshList();
      if (viewDialogVisible.value) {
        viewDialogVisible.value = false;
      }
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败');
    }
  }
};

const handleQuantityChange = (row) => {
  if (row.countedQuantity !== null && row.countedQuantity !== undefined) {
    row.diffQuantity = row.countedQuantity - row.systemQuantity;
  } else {
    row.diffQuantity = -row.systemQuantity;
  }
};

const handleSaveItem = async (row) => {
  try {
    if (row.countedQuantity === null || row.countedQuantity === undefined) {
      ElMessage.warning('请输入实盘数量');
      return;
    }
    
    const res = await post('/api/auth/stock/updateItem', {
      id: row.id,
      countedQuantity: row.countedQuantity,
      diffQuantity: row.diffQuantity,
      status: 2
    });
    
    if (res) {
      ElMessage.success('保存成功');
      row.status = 2;
      // 重新加载当前页的明细
      await loadDetailList(currentStockTake.id);
    }
  } catch (error) {
    ElMessage.error('保存失败');
  }
};

const handleConfirmItem = async (row) => {
  try {
    await ElMessageBox.confirm(
      '确认已盘点此项？',
      '确认盘点',
      { type: 'warning' }
    );
    
    const res = await post('/api/auth/stock/confirmItem', {
      id: row.id,
      status: 3
    });
    
    if (res) {
      ElMessage.success('确认成功');
      row.status = 3;
      // 重新加载当前页的明细
      await loadDetailList(currentStockTake.id);
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('确认失败');
    }
  }
};

const handleRevertItem = async (row) => {
  try {
    await ElMessageBox.confirm(
      '确定要撤销确认吗？',
      '撤销确认',
      { type: 'warning' }
    );
    
    const res = await post('/api/auth/stock/revertItem', {
      id: row.id,
      status: 2
    });
    
    if (res) {
      ElMessage.success('撤销成功');
      row.status = 2;
      // 重新加载当前页的明细
      await loadDetailList(currentStockTake.id);
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('撤销失败');
    }
  }
};

const handleBatchEnter = () => {
  uploadData.value = [];
  batchDialogVisible.value = true;
};

const handleExportData = async () => {
  try {
    const res = await post('/api/auth/stock/export', { 
      stockTakeId: currentStockTake.id 
    }, { responseType: 'blob' });
    
    const url = window.URL.createObjectURL(new Blob([res]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `${currentStockTake.stockTakeNo}_盘点数据.xlsx`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  } catch (error) {
    console.error('导出失败:', error);
    ElMessage.error('导出失败');
  }
};

const downloadTemplate = async () => {
  try {
    const res = await get('/api/auth/stock/template', {}, { responseType: 'blob' });
    
    const url = window.URL.createObjectURL(new Blob([res]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', '盘点数据模板.xlsx');
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  } catch (error) {
    console.error('下载模板失败:', error);
    ElMessage.error('下载模板失败');
  }
};

const beforeUpload = (file) => {
  const isExcel = file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' || 
                  file.name.endsWith('.xlsx');
  if (!isExcel) {
    ElMessage.error('只能上传Excel文件');
    return false;
  }
  return true;
};

const handleUpload = async ({ file }) => {
  const formData = new FormData();
  formData.append('file', file);
  
  try {
    const res = await post('/api/auth/stock/parseUpload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
    
    uploadData.value = res || [];
    ElMessage.success('解析成功，请确认数据');
  } catch (error) {
    console.error('解析文件失败:', error);
    ElMessage.error('解析文件失败');
    uploadData.value = [];
  }
};

const clearUploadData = () => {
  uploadData.value = [];
};

const handleBatchSave = async () => {
  batchLoading.value = true;
  try {
    const res = await post('/api/auth/stock/batchUpdate', {
      stockTakeId: currentStockTake.id,
      items: uploadData.value
    });
    
    if (res) {
      ElMessage.success('批量保存成功');
      batchDialogVisible.value = false;
      uploadData.value = [];
      // 重新加载明细
      await loadDetailList(currentStockTake.id);
    }
  } catch (error) {
    console.error('批量保存失败:', error);
    ElMessage.error('批量保存失败');
  } finally {
    batchLoading.value = false;
  }
};

const resetForm = () => {
  Object.assign(formData, {
    id: '',
    stockTakeNo: '',
    warehouseId: '',
    takeType: 1,
    takeScope: 1,
    batchIds: [],
    shelfIds: [],
    productIds: [],
    remark: ''
  });
  
  previewData.value = [];
  batchOptions.value = [];
  shelfOptions.value = [];
  productOptions.value = [];
  
  if (formRef.value) {
    formRef.value.clearValidate();
  }
};

const handleFormSubmit = async () => {
  if (!formRef.value) return;
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      formLoading.value = true;
      try {
        // 准备提交数据
        const submitData = {
          ...formData
        };
        
        // 根据盘点范围，构建scopeValues
        let scopeValues = {};
        if (formData.takeScope === 2 && formData.batchIds.length > 0) {
          scopeValues = { batchIds: formData.batchIds };
        } else if (formData.takeScope === 3 && formData.shelfIds.length > 0) {
          scopeValues = { shelfIds: formData.shelfIds };
        } else if (formData.takeScope === 4 && formData.productIds.length > 0) {
          scopeValues = { productIds: formData.productIds };
        }
        
        // 将scopeValues转换为JSON字符串
        if (Object.keys(scopeValues).length > 0) {
          submitData.scopeValues = JSON.stringify(scopeValues);
        }
        
        const url = isEdit.value ? '/api/auth/stock/update' : '/api/auth/stock/createStockTake';
        const res = await post(url, submitData);
        
        if (res) {
          ElMessage.success(isEdit.value ? '更新成功' : '创建成功');
          formDialogVisible.value = false;
          refreshList();
        }
      } catch (error) {
        console.error('提交失败:', error);
        ElMessage.error('提交失败: ' + (error.message || '未知错误'));
      } finally {
        formLoading.value = false;
      }
    }
  });
};

const handleWarehouseChange = (warehouseId) => {
  if (warehouseId) {
    showPreview.value = true;
    // 清空选项数据
    batchOptions.value = [];
    shelfOptions.value = [];
    productOptions.value = [];
    
    // 清空已选的范围值
    if (formData.takeScope !== 2) formData.batchIds = [];
    if (formData.takeScope !== 3) formData.shelfIds = [];
    if (formData.takeScope !== 4) formData.productIds = [];
    
    // 延迟刷新预览，等待用户选择范围
  } else {
    showPreview.value = false;
    previewData.value = [];
  }
};

// 监听盘点范围变化
watch(() => formData.takeScope, (newScope) => {
  // 当范围变化时，清空其他范围的数据
  if (newScope !== 2) formData.batchIds = [];
  if (newScope !== 3) formData.shelfIds = [];
  if (newScope !== 4) formData.productIds = [];
  
  // 如果已选择仓库，则刷新预览
  if (formData.warehouseId && showPreview.value) {
    // 防抖刷新预览
    clearTimeout(window.previewTimer);
    window.previewTimer = setTimeout(() => {
      refreshPreview();
    }, 500);
  }
});

// 监听范围选择值的变化
watch(() => [formData.batchIds, formData.shelfIds, formData.productIds], () => {
  if (formData.warehouseId && showPreview.value) {
    // 防抖刷新预览
    clearTimeout(window.previewTimer);
    window.previewTimer = setTimeout(() => {
      refreshPreview();
    }, 500);
  }
}, { deep: true });

const getScopeLabel = (scope) => {
  const mapping = {
    1: '全部',
    2: '批次',
    3: '货架',
    4: '商品'
  };
  return mapping[scope] || '未知';
};

const getApproveStatusLabel = (status) => {
  //1新建,2待审核,3通过,4拒绝
  const mapping = {
    0: '草稿',
    1: '待审核',
    2: '通过',
    3: '拒绝'
  };
  return mapping[status] || '未知';
};

const getApproveStatusTagType = (status) => {
  const mapping = {
    1: 'info',
    2: 'primary',
    3: 'success',
    4: 'danger'
  };
  return mapping[status] || 'info';
};

const getStatusLabel = (status) => {
  //盘点状态:0未开始,1盘点中,2待确认,3已完成,4已取消
  const mapping = {
    0: '新建',
    1: '盘点中',
    2: '待确认',
    3: '已完成',
    4: '已取消'
  };
  return mapping[status] || '未知';
};

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

const getItemStatusLabel = (status) => {
  const mapping = {
    1: '未盘',
    2: '已盘',
    3: '已确认'
  };
  return mapping[status] || '未知';
};

const getItemStatusTagType = (status) => {
  const mapping = {
    1: 'info',
    2: 'warning',
    3: 'success'
  };
  return mapping[status] || 'info';
};

const getDiffClass = (diff) => {
  if (diff > 0) return 'diff-positive';
  if (diff < 0) return 'diff-negative';
  return 'diff-zero';
};

const formatNumber = (num) => {
  if (num === null || num === undefined) return '--';
  const number = Number(num);
  if (isNaN(number)) return '--';
  return number.toLocaleString('zh-CN', {
    minimumFractionDigits: 0,
    maximumFractionDigits: 4
  });
};

const formatDateTime = (dateStr) => {
  if (!dateStr) return '--';
  try {
    const date = new Date(dateStr);
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    });
  } catch (e) {
    return dateStr;
  }
};

onMounted(() => {
  loadStockTakeList();
  loadWarehouseList();
});
</script>

<style scoped>
/* 样式保持不变，与原始文件相同 */
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

.stock-take-list-section {
  margin-top: 20px;
}

.stock-take-table {
  width: 100%;
}

.stock-take-no {
  font-family: 'Courier New', monospace;
  font-weight: bold;
  color: #409EFF;
}

.detail-info {
  color: #606266;
  font-size: 14px;
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

/* 表单对话框样式 */
.stock-take-form-dialog :deep(.el-dialog__header) {
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 15px;
}

.stock-take-form-container {
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

.compact-form {
  padding: 0;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  padding: 20px;
}

.form-group {
  margin-bottom: 0;
}

.form-group.full-width {
  grid-column: 1 / -1;
}

.form-group :deep(.el-form-item__label) {
  font-weight: 500;
  color: #606266;
}

.form-input, .form-select {
  width: 100%;
}

.type-tips {
  margin-top: 8px;
  padding: 8px 12px;
  background: #f0f7ff;
  border-radius: 4px;
  font-size: 12px;
  color: #409EFF;
}

.type-tips .el-icon {
  margin-right: 4px;
}

/* 预览区域样式 */
.preview-content {
  padding: 0 20px 20px;
}

.preview-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100px;
  color: #909399;
}

.loading-icon {
  margin-bottom: 8px;
  animation: rotate 1s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
}

.preview-title {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.preview-total {
  font-size: 14px;
  color: #409EFF;
  font-weight: bold;
}

.preview-table {
  margin-bottom: 12px;
}

.preview-more {
  text-align: center;
  padding: 8px;
  color: #909399;
  font-size: 12px;
  background: #f8f9fa;
  border-radius: 4px;
}

.preview-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100px;
  color: #909399;
}

.preview-empty .el-icon {
  font-size: 32px;
  margin-bottom: 8px;
}

/* 查看对话框样式 */
.stock-take-view-dialog :deep(.el-dialog__header) {
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 15px;
}

.stock-take-view-container {
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

.header-status {
  display: flex;
  gap: 8px;
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

/* 统计卡片 */
.stats-card {
  margin-bottom: 20px;
  border-radius: 8px;
}

.stats-card :deep(.el-card__header) {
  padding: 16px 20px;
  background: #f8f9fa;
  border-bottom: 1px solid #ebeef5;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  padding: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  transition: all 0.3s;
}

.stat-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  margin-right: 16px;
}

.stat-icon.primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-icon.success {
  background: linear-gradient(135deg, #36d1dc 0%, #5b86e5 100%);
}

.stat-icon.warning {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-icon.diff-positive {
  background: linear-gradient(135deg, #67C23A 0%, #85ce61 100%);
}

.stat-icon.diff-negative {
  background: linear-gradient(135deg, #F56C6C 0%, #f78989 100%);
}

.stat-icon.diff-zero {
  background: linear-gradient(135deg, #909399 0%, #c0c4cc 100%);
}

.stat-icon .el-icon {
  font-size: 24px;
  color: white;
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
  font-size: 12px;
  color: #909399;
}

/* 明细卡片 */
.detail-card {
  margin-bottom: 20px;
  border-radius: 8px;
}

.detail-card :deep(.el-card__header) {
  padding: 16px 20px;
  background: #f8f9fa;
  border-bottom: 1px solid #ebeef5;
}

.detail-card .card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.detail-table-container {
  padding: 0 20px 20px;
}

.detail-table {
  width: 100%;
  margin-bottom: 16px;
}

.detail-table .el-input-number {
  width: 100px;
}

.item-actions {
  display: flex;
  gap: 4px;
  justify-content: center;
  flex-wrap: wrap;
}

.detail-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

/* 日志卡片 */
.log-card {
  border-radius: 8px;
}

.log-card :deep(.el-card__header) {
  padding: 16px 20px;
  background: #f8f9fa;
  border-bottom: 1px solid #ebeef5;
}

.log-list {
  padding: 20px;
  max-height: 300px;
  overflow-y: auto;
}

.log-item {
  padding: 12px 16px;
  background: #f8f9fa;
  border-radius: 6px;
  margin-bottom: 12px;
  border-left: 4px solid #409EFF;
}

.log-action {
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
  font-size: 14px;
}

.log-desc {
  font-size: 13px;
  color: #606266;
  margin-bottom: 4px;
  line-height: 1.4;
}

.log-user {
  font-size: 12px;
  color: #909399;
}

/* 批量录入对话框 */
.batch-enter-container {
  padding: 20px;
}

.batch-alert {
  margin-bottom: 20px;
}

.batch-actions {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}

.batch-preview {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 16px;
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.preview-title {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

/* 对话框底部 */
.form-dialog-footer,
.dialog-footer {
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
@media (max-width: 1200px) {
  .stats-grid {
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
  
  .filter-section .el-form-item {
    margin-bottom: 12px;
  }
  
  .action-buttons {
    flex-direction: column;
    gap: 4px;
  }
  
  .stock-take-view-dialog,
  .stock-take-form-dialog {
    width: 95% !important;
  }
  
  .info-grid {
    grid-template-columns: 1fr;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .form-grid {
    grid-template-columns: 1fr;
  }
  
  .cancel-btn, .submit-btn {
    min-width: 80px;
  }
}

/* 动画效果 */
.stock-take-table :deep(.el-table__row) {
  transition: all 0.3s;
}

.stock-take-table :deep(.el-table__row:hover) {
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

.stock-take-view-container {
  animation: fadeIn 0.3s ease;
}

.form-card {
  animation: slideUp 0.3s ease;
}

/* 滚动条样式 */
.log-list::-webkit-scrollbar {
  width: 6px;
}

.log-list::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.log-list::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.log-list::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>