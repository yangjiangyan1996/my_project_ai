<template>
  <div class="adjust-order-manage-container">
    <el-card class="manage-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">库存调整单管理</span>
          <div class="header-actions">
            <el-button 
              type="primary" 
              @click="handleCreate"
              :disabled="!selectedWarehouseId"
            >
              <el-icon><Plus /></el-icon>
              新建调整单
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

      <!-- 仓库选择 -->
      <div class="warehouse-select-section">
        <el-form :model="warehouseForm" inline>
          <el-form-item label="选择仓库" required>
            <el-select
              v-model="warehouseForm.warehouseId"
              placeholder="请选择仓库"
              clearable
              @change="handleWarehouseSelect"
              style="width: 250px"
            >
              <el-option
                v-for="warehouse in warehouseList"
                :key="warehouse.id"
                :label="warehouse.name"
                :value="warehouse.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item v-if="selectedWarehouseName">
            <el-tag type="primary" size="large">
              <el-icon><OfficeBuilding /></el-icon>
              {{ selectedWarehouseName }}
            </el-tag>
          </el-form-item>
        </el-form>
      </div>

      <!-- 筛选条件 -->
      <div class="filter-section" v-if="selectedWarehouseId">
        <el-form :model="filterForm" inline>
          <el-form-item label="调整单号">
            <el-input
              v-model="filterForm.adjustNo"
              placeholder="请输入调整单号"
              clearable
              style="width: 180px"
            />
          </el-form-item>
          <el-form-item label="调整类型">
            <el-select
              v-model="filterForm.adjustType"
              placeholder="全部类型"
              clearable
              style="width: 120px"
            >
              <el-option
                v-for="type in adjustTypeOptions"
                :key="type.value"
                :label="type.label"
                :value="type.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="来源类型">
            <el-select
              v-model="filterForm.sourceType"
              placeholder="全部来源"
              clearable
              style="width: 120px"
            >
              <el-option
                v-for="source in sourceTypeOptions"
                :key="source.value"
                :label="source.label"
                :value="source.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="调整状态">
            <el-select
              v-model="filterForm.adjustStatus"
              placeholder="全部状态"
              clearable
              style="width: 120px"
            >
              <el-option
                v-for="status in adjustStatusOptions"
                :key="status.value"
                :label="status.label"
                :value="status.value"
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

      <!-- 调整单列表 -->
      <div class="adjust-order-list-section" v-if="selectedWarehouseId">
        <el-table
          :data="adjustOrderList"
          v-loading="loading"
          empty-text="请先选择仓库"
          class="adjust-order-table"
          row-key="id"
        >
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column label="调整单号" width="180" fixed="left">
            <template #default="{ row }">
              <span class="adjust-order-no">{{ row.adjustNo }}</span>
            </template>
          </el-table-column>
          <el-table-column label="调整类型" width="100" align="center">
            <template #default="{ row }">
              <el-tag 
                :type="getAdjustTypeTagType(row.adjustType)" 
                size="small"
              >
                {{ getAdjustTypeLabel(row.adjustType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="来源" width="120">
            <template #default="{ row }">
              <div v-if="row.sourceType === 1" class="source-info">
                <el-icon><Document /></el-icon>
                <span>盘点单：{{ row.sourceNo }}</span>
              </div>
              <div v-else-if="row.sourceType === 2" class="source-info">
                <el-icon><Edit /></el-icon>
                <span>手动创建</span>
              </div>
              <div v-else class="source-info">
                <el-icon><SetUp /></el-icon>
                <span>{{ getSourceTypeLabel(row.sourceType) }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="调整商品" width="100" align="center">
            <template #default="{ row }">
              <span class="detail-info">
                {{ row.totalItems || 0 }} 个
              </span>
            </template>
          </el-table-column>
          <el-table-column label="调整总量" width="120" align="center">
            <template #default="{ row }">
              <span :class="getQuantityClass(row.totalQuantity)">
                {{ formatNumber(row.totalQuantity) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="调整金额" width="120" align="right">
            <template #default="{ row }">
              <span :class="getAmountClass(row.totalAmount)">
                ¥{{ formatCurrency(row.totalAmount) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="调整状态" width="120" align="center">
            <template #default="{ row }">
              <el-tag 
                :type="getStatusTagType(row.adjustStatus)" 
                size="small"
              >
                {{ getStatusLabel(row.adjustStatus) }}
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
          <el-table-column label="操作" width="300" fixed="right" align="center">
            <template #default="{ row }">
              <div class="action-buttons">
                <!-- 查看详情 -->
                <el-button
                  type="primary"
                  link
                  size="small"
                  @click="handleView(row)"
                >
                  查看
                </el-button>

                <!-- 待提交状态：可编辑、提交、删除 -->
                <template v-if="row.adjustStatus === 1">
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
                    提交审核
                  </el-button>
                  <el-button
                    type="danger"
                    link
                    size="small"
                    @click="handleDelete(row)"
                  >
                    删除
                  </el-button>
                </template>

                <!-- 待审核状态：可审核通过/拒绝 -->
                <template v-if="row.adjustStatus === 2">
                  <el-button
                    type="success"
                    link
                    size="small"
                    @click="handleApprove(row)"
                  >
                    审核通过
                  </el-button>
                  <el-button
                    type="danger"
                    link
                    size="small"
                    @click="handleReject(row)"
                  >
                    审核拒绝
                  </el-button>
                </template>

                <!-- 审核通过状态：可执行 -->
                <template v-if="row.adjustStatus === 3">
                  <el-button
                    type="primary"
                    link
                    size="small"
                    @click="handleExecute(row)"
                  >
                    执行调整
                  </el-button>
                </template>

                <!-- 已执行状态：可查看执行结果 -->
                <template v-if="row.adjustStatus === 5">
                  <el-button
                    type="info"
                    link
                    size="small"
                    @click="handleViewResult(row)"
                  >
                    执行结果
                  </el-button>
                </template>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-section" v-if="adjustOrderList.length > 0">
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

      <!-- 无仓库选择提示 -->
      <div class="no-warehouse-tip" v-else>
        <el-empty description="请先选择仓库以查看调整单">
          <template #image>
            <el-icon size="60"><OfficeBuilding /></el-icon>
          </template>
        </el-empty>
      </div>
    </el-card>

    <!-- 创建/编辑调整单对话框 -->
    <el-dialog
      v-model="formDialogVisible"
      :title="formTitle"
      width="900px"
      top="5vh"
      class="adjust-order-form-dialog"
    >
      <div class="adjust-order-form-container">
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
                <el-form-item label="调整单号" prop="adjustNo">
                  <el-input
                    v-model="formData.adjustNo"
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
                    disabled
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
                <el-form-item label="调整类型" prop="adjustType">
                  <el-select
                    v-model="formData.adjustType"
                    placeholder="请选择调整类型"
                    class="form-select"
                  >
                    <el-option
                      v-for="type in adjustTypeOptions"
                      :key="type.value"
                      :label="type.label"
                      :value="type.value"
                    />
                  </el-select>
                </el-form-item>
              </div>
              
              <div class="form-group">
                <el-form-item label="来源类型" prop="sourceType">
                  <el-select
                    v-model="formData.sourceType"
                    placeholder="请选择来源类型"
                    class="form-select"
                    @change="handleSourceTypeChange"
                  >
                    <el-option
                      v-for="source in sourceTypeOptions"
                      :key="source.value"
                      :label="source.label"
                      :value="source.value"
                    />
                  </el-select>
                </el-form-item>
              </div>
              
              <!-- 来源单据选择 -->
              <template v-if="formData.sourceType === 1">
                <div class="form-group full-width">
                  <el-form-item label="选择盘点单" prop="sourceId">
                    <el-select
                      v-model="formData.sourceId"
                      placeholder="请选择盘点单"
                      filterable
                      class="form-select"
                      @change="handleStockTakeSelect"
                    >
                      <el-option
                        v-for="stockTake in stockTakeOptions"
                        :key="stockTake.id"
                        :label="`${stockTake.stockTakeNo}（${stockTake.warehouseName}）`"
                        :value="stockTake.id"
                      />
                    </el-select>
                  </el-form-item>
                </div>
              </template>
              
              <div class="form-group full-width">
                <el-form-item label="调整原因" prop="adjustReason">
                  <el-input
                    v-model="formData.adjustReason"
                    type="textarea"
                    :rows="3"
                    placeholder="请输入调整原因"
                    maxlength="200"
                    show-word-limit
                    resize="none"
                    class="reason-textarea"
                  />
                </el-form-item>
              </div>
              
              <div class="form-group">
                <el-form-item label="是否紧急" prop="isUrgent">
                  <el-switch
                    v-model="formData.isUrgent"
                    active-text="紧急"
                    inactive-text="普通"
                  />
                </el-form-item>
              </div>
              
              <div class="form-group">
                <el-form-item label="影响成本" prop="isAffectCost">
                  <el-switch
                    v-model="formData.isAffectCost"
                    active-text="是"
                    inactive-text="否"
                  />
                </el-form-item>
              </div>
              
              <div class="form-group full-width">
                <el-form-item label="备注" prop="remark">
                  <el-input
                    v-model="formData.remark"
                    type="textarea"
                    :rows="2"
                    placeholder="请输入备注信息"
                    maxlength="500"
                    show-word-limit
                    resize="none"
                    class="remark-textarea"
                  />
                </el-form-item>
              </div>
            </div>
          </el-card>

          <!-- 调整明细 -->
          <el-card class="form-card" shadow="never" v-if="showAdjustItems">
            <template #header>
              <div class="card-header">
                <span class="card-title">调整明细</span>
                <div class="header-actions">
                  <el-button
                    type="primary"
                    link
                    size="small"
                    @click="handleAddItem"
                  >
                    <el-icon><Plus /></el-icon>
                    添加商品
                  </el-button>
                </div>
              </div>
            </template>
            
            <div class="adjust-items-container">
              <div v-if="adjustItems.length === 0" class="no-items-tip">
                <el-empty description="暂无调整明细">
                  <template #image>
                    <el-icon><Box /></el-icon>
                  </template>
                  <el-button type="primary" @click="handleAddItem">添加商品</el-button>
                </el-empty>
              </div>
              
              <div v-else class="adjust-items-list">
                <el-table
                  :data="adjustItems"
                  border
                  class="adjust-items-table"
                  size="small"
                >
                  <el-table-column type="index" label="序号" width="60" align="center" />
                  <el-table-column label="商品信息" width="250">
                    <template #default="{ row }">
                      <div class="product-info">
                        <div class="product-name">{{ row.productName || '--' }}</div>
                        <div class="product-sku">{{ row.skuCode || '--' }}</div>
                        <div class="product-spec">{{ row.specification || '--' }}</div>
                      </div>
                    </template>
                  </el-table-column>
                  <el-table-column label="批次号" width="120">
                    <template #default="{ row }">
                      <el-input
                        v-model="row.batchNo"
                        placeholder="批次号"
                        size="small"
                        @change="handleBatchNoChange(row)"
                      />
                    </template>
                  </el-table-column>
                  <el-table-column label="货架" width="120">
                    <template #default="{ row }">
                      <el-select
                        v-model="row.shelfId"
                        placeholder="选择货架"
                        size="small"
                        clearable
                        @change="handleShelfChange(row)"
                      >
                        <el-option
                          v-for="shelf in shelfOptions"
                          :key="shelf.id"
                          :label="shelf.name"
                          :value="shelf.id"
                        />
                      </el-select>
                    </template>
                  </el-table-column>
                  <el-table-column label="调整前数量" width="120" align="right">
                    <template #default="{ row }">
                      <span class="before-quantity">{{ formatNumber(row.beforeQuantity) }}</span>
                    </template>
                  </el-table-column>
                  <el-table-column label="调整数量" width="150" align="center">
                    <template #default="{ row }">
                      <div class="adjust-quantity-cell">
                        <el-input-number
                          v-model="row.adjustQuantity"
                          :min="-999999"
                          :precision="4"
                          size="small"
                          controls-position="right"
                          style="width: 120px"
                          @change="handleAdjustQuantityChange(row)"
                        />
                        <div class="quantity-tips">
                          <span v-if="row.adjustQuantity > 0" class="positive">增加</span>
                          <span v-if="row.adjustQuantity < 0" class="negative">减少</span>
                          <span v-if="row.adjustQuantity === 0" class="zero">不变</span>
                        </div>
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
                  <el-table-column label="单位成本" width="120" align="right">
                    <template #default="{ row }">
                      <el-input-number
                        v-model="row.unitCost"
                        :min="0"
                        :precision="2"
                        size="small"
                        controls-position="right"
                        style="width: 100px"
                        @change="handleCostChange(row)"
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
                  <el-table-column label="操作" width="80" align="center" fixed="right">
                    <template #default="{ row }">
                      <el-button
                        type="danger"
                        link
                        size="small"
                        @click="handleRemoveItem(row)"
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
                    <span class="stat-value">{{ adjustItems.length }}</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-label">调整总量：</span>
                    <span :class="getQuantityClass(totalAdjustQuantity)">
                      {{ formatNumber(totalAdjustQuantity) }}
                    </span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-label">增加数量：</span>
                    <span class="stat-value positive">{{ formatNumber(totalIncreaseQuantity) }}</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-label">减少数量：</span>
                    <span class="stat-value negative">{{ formatNumber(totalDecreaseQuantity) }}</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-label">调整金额：</span>
                    <span :class="getAmountClass(totalAdjustAmount)">
                      ¥{{ formatCurrency(totalAdjustAmount) }}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </el-card>
          
          <!-- 选择商品对话框 -->
          <el-dialog
            v-model="productDialogVisible"
            title="选择商品"
            width="800px"
            top="10vh"
            append-to-body
          >
            <div class="product-select-dialog">
              <!-- 商品筛选 -->
              <div class="product-filter">
                <el-input
                  v-model="productFilter.keyword"
                  placeholder="搜索商品名称、SKU、编码"
                  clearable
                  style="width: 300px"
                  @keyup.enter="loadProductList"
                >
                  <template #append>
                    <el-button @click="loadProductList">
                      <el-icon><Search /></el-icon>
                    </el-button>
                  </template>
                </el-input>
              </div>
              
              <!-- 商品列表 -->
              <div class="product-list-container">
                <el-table
                  ref="productTableRef"
                  :data="productList"
                  v-loading="productLoading"
                  empty-text="暂无商品数据"
                  @selection-change="handleProductSelectionChange"
                  class="product-select-table"
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
                </el-table>
              </div>
              
              <div class="product-pagination">
                <el-pagination
                  v-model:current-page="productPagination.current"
                  v-model:page-size="productPagination.size"
                  :total="productPagination.total"
                  :page-sizes="[10, 20, 50]"
                  layout="total, sizes, prev, pager, next"
                  @size-change="handleProductSizeChange"
                  @current-change="handleProductCurrentChange"
                />
              </div>
            </div>
            
            <template #footer>
              <div class="dialog-footer">
                <el-button @click="productDialogVisible = false">取消</el-button>
                <el-button 
                  type="primary" 
                  @click="handleConfirmProducts"
                  :disabled="selectedProducts.length === 0"
                >
                  确认选择（{{ selectedProducts.length }}个商品）
                </el-button>
              </div>
            </template>
          </el-dialog>
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
            :disabled="adjustItems.length === 0"
          >
            保存调整单
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 查看调整单详情对话框 -->
    <el-dialog
      v-model="viewDialogVisible"
      :title="`调整单详情 - ${currentAdjustOrder.adjustNo}`"
      width="1000px"
      top="5vh"
      class="adjust-order-view-dialog"
    >
      <div class="adjust-order-view-container" v-loading="viewLoading">
        <!-- 基本信息卡片 -->
        <el-card class="basic-info-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span class="card-title">基本信息</span>
              <div class="header-status">
                <el-tag 
                  :type="getStatusTagType(currentAdjustOrder.adjustStatus)" 
                  size="large"
                >
                  {{ getStatusLabel(currentAdjustOrder.adjustStatus) }}
                </el-tag>
                <el-tag 
                  :type="getAdjustTypeTagType(currentAdjustOrder.adjustType)" 
                  size="large"
                >
                  {{ getAdjustTypeLabel(currentAdjustOrder.adjustType) }}
                </el-tag>
                <el-tag 
                  v-if="currentAdjustOrder.isUrgent"
                  type="danger"
                  size="large"
                >
                  紧急
                </el-tag>
              </div>
            </div>
          </template>
          <div class="info-grid">
            <div class="info-item">
              <span class="info-label">调整单号</span>
              <span class="info-value highlight">{{ currentAdjustOrder.adjustNo }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">仓库</span>
              <span class="info-value">{{ currentAdjustOrder.warehouseName }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">调整类型</span>
              <span class="info-value">{{ getAdjustTypeLabel(currentAdjustOrder.adjustType) }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">来源类型</span>
              <span class="info-value">{{ getSourceTypeLabel(currentAdjustOrder.sourceType) }}</span>
            </div>
            <div class="info-item" v-if="currentAdjustOrder.sourceNo">
              <span class="info-label">来源单号</span>
              <span class="info-value">{{ currentAdjustOrder.sourceNo }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">调整状态</span>
              <span class="info-value">{{ getStatusLabel(currentAdjustOrder.adjustStatus) }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">调整原因</span>
              <span class="info-value">{{ currentAdjustOrder.adjustReason }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">创建人</span>
              <span class="info-value">{{ currentAdjustOrder.createdByName }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">创建时间</span>
              <span class="info-value">{{ formatDateTime(currentAdjustOrder.createdAt) }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">修改时间</span>
              <span class="info-value">{{ formatDateTime(currentAdjustOrder.modifiedAt) }}</span>
            </div>
            <div class="info-item" v-if="currentAdjustOrder.approverName">
              <span class="info-label">审批人</span>
              <span class="info-value">{{ currentAdjustOrder.approverName }}</span>
            </div>
            <div class="info-item" v-if="currentAdjustOrder.approveTime">
              <span class="info-label">审批时间</span>
              <span class="info-value">{{ formatDateTime(currentAdjustOrder.approveTime) }}</span>
            </div>
            <div class="info-item" v-if="currentAdjustOrder.executeByName">
              <span class="info-label">执行人</span>
              <span class="info-value">{{ currentAdjustOrder.executeByName }}</span>
            </div>
            <div class="info-item" v-if="currentAdjustOrder.actualExecuteTime">
              <span class="info-label">执行时间</span>
              <span class="info-value">{{ formatDateTime(currentAdjustOrder.actualExecuteTime) }}</span>
            </div>
            <div class="info-item full-width">
              <span class="info-label">备注</span>
              <span class="info-value">{{ currentAdjustOrder.remark || '无' }}</span>
            </div>
          </div>
        </el-card>

        <!-- 统计信息卡片 -->
        <el-card class="stats-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span class="card-title">调整统计</span>
            </div>
          </template>
          <div class="stats-grid">
            <div class="stat-item">
              <div class="stat-icon primary">
                <el-icon><Document /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ currentAdjustOrder.totalItems || 0 }}</div>
                <div class="stat-label">调整商品数</div>
              </div>
            </div>
            <div class="stat-item">
              <div class="stat-icon" :class="getQuantityClass(currentAdjustOrder.totalQuantity)">
                <el-icon><TrendCharts /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ formatNumber(currentAdjustOrder.totalQuantity) }}</div>
                <div class="stat-label">调整总量</div>
              </div>
            </div>
            <div class="stat-item">
              <div class="stat-icon success">
                <el-icon><Top /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ formatNumber(currentAdjustOrder.increaseQuantity || 0) }}</div>
                <div class="stat-label">增加数量</div>
              </div>
            </div>
            <div class="stat-item">
              <div class="stat-icon warning">
                <el-icon><Bottom /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ formatNumber(currentAdjustOrder.decreaseQuantity || 0) }}</div>
                <div class="stat-label">减少数量</div>
              </div>
            </div>
            <div class="stat-item">
              <div class="stat-icon" :class="getAmountClass(currentAdjustOrder.totalAmount)">
                <el-icon><Money /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">¥{{ formatCurrency(currentAdjustOrder.totalAmount || 0) }}</div>
                <div class="stat-label">调整金额</div>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 调整明细表格 -->
        <el-card class="detail-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span class="card-title">调整明细</span>
              <div class="header-actions">
                <el-button
                  v-if="currentAdjustOrder.adjustStatus === 3"
                  type="primary"
                  size="small"
                  @click="handleExecute(currentAdjustOrder)"
                >
                  执行调整
                </el-button>
                <el-button
                  type="success"
                  size="small"
                  @click="handleExportAdjustData"
                >
                  导出明细
                </el-button>
              </div>
            </div>
          </template>
          
          <div class="detail-table-container">
            <el-table
              :data="adjustDetailList"
              v-loading="detailLoading"
              empty-text="暂无调整明细"
              class="detail-table"
              height="400"
            >
              <el-table-column type="index" label="序号" width="60" align="center" />
              <el-table-column label="SKU" width="120">
                <template #default="{ row }">
                  {{ row.skuCode || '--' }}
                </template>
              </el-table-column>
              <el-table-column label="商品名称" width="150">
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
              <el-table-column label="调整前数量" width="100" align="right">
                <template #default="{ row }">
                  {{ formatNumber(row.beforeQuantity) }}
                </template>
              </el-table-column>
              <el-table-column label="调整数量" width="120" align="center">
                <template #default="{ row }">
                  <span :class="getAdjustQuantityClass(row.adjustQuantity)">
                    {{ formatNumber(row.adjustQuantity) }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column label="调整后数量" width="100" align="right">
                <template #default="{ row }">
                  <span :class="getAfterQuantityClass(row.afterQuantity)">
                    {{ formatNumber(row.afterQuantity) }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column label="单位成本" width="100" align="right">
                <template #default="{ row }">
                  {{ formatCurrency(row.unitCost) }}
                </template>
              </el-table-column>
              <el-table-column label="调整金额" width="100" align="right">
                <template #default="{ row }">
                  <span :class="getAmountClass(row.adjustAmount)">
                    ¥{{ formatCurrency(row.adjustAmount) }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column label="调整原因" width="150">
                <template #default="{ row }">
                  {{ row.adjustReason || '--' }}
                </template>
              </el-table-column>
              <el-table-column label="明细状态" width="100" align="center">
                <template #default="{ row }">
                  <el-tag
                    :type="getItemStatusTagType(row.status)"
                    size="small"
                  >
                    {{ getItemStatusLabel(row.status) }}
                  </el-tag>
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
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="viewDialogVisible = false">关闭</el-button>
          <!-- 根据状态显示不同操作按钮 -->
          <template v-if="currentAdjustOrder.adjustStatus === 1">
            <el-button type="warning" @click="handleEdit(currentAdjustOrder)">编辑</el-button>
            <el-button type="success" @click="handleSubmit(currentAdjustOrder)">提交审核</el-button>
            <el-button type="danger" @click="handleDelete(currentAdjustOrder)">删除</el-button>
          </template>
          <template v-if="currentAdjustOrder.adjustStatus === 2">
            <el-button type="success" @click="handleApprove(currentAdjustOrder)">审核通过</el-button>
            <el-button type="danger" @click="handleReject(currentAdjustOrder)">审核拒绝</el-button>
          </template>
          <template v-if="currentAdjustOrder.adjustStatus === 3">
            <el-button type="primary" @click="handleExecute(currentAdjustOrder)">执行调整</el-button>
          </template>
          <template v-if="currentAdjustOrder.adjustStatus === 5">
            <el-button type="info" disabled>已执行</el-button>
          </template>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch, nextTick } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { 
  Plus, 
  Refresh, 
  Document,
  Edit,
  SetUp,
  Search,
  Box,
  TrendCharts,
  Top,
  Bottom,
  Money,
  OfficeBuilding
} from '@element-plus/icons-vue';
import { post, get } from '@/net';

const loading = ref(false);
const formLoading = ref(false);
const formDialogVisible = ref(false);
const viewDialogVisible = ref(false);
const productDialogVisible = ref(false);
const viewLoading = ref(false);
const detailLoading = ref(false);
const productLoading = ref(false);
const formRef = ref();
const productTableRef = ref();
const isEdit = ref(false);
const showAdjustItems = ref(false);

// 仓库选择表单
const warehouseForm = reactive({
  warehouseId: ''
});

// 筛选表单
const filterForm = reactive({
  adjustNo: '',
  adjustType: '',
  sourceType: '',
  adjustStatus: '',
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

// 商品选择分页
const productPagination = reactive({
  current: 1,
  size: 10,
  total: 0
});

// 商品筛选条件
const productFilter = reactive({
  keyword: '',
  warehouseId: ''
});

// 表单数据
const formData = reactive({
  id: '',
  adjustNo: '',
  warehouseId: '',
  warehouseName: '',
  adjustType: 1,
  sourceType: 1,
  sourceId: '',
  sourceNo: '',
  adjustReason: '',
  isUrgent: false,
  isAffectCost: false,
  remark: ''
});

// 调整明细数据
const adjustItems = ref([]);

// 当前查看的调整单
const currentAdjustOrder = reactive({
  id: '',
  adjustNo: '',
  warehouseId: '',
  warehouseName: '',
  adjustType: 1,
  sourceType: 1,
  sourceId: '',
  sourceNo: '',
  adjustReason: '',
  adjustStatus: 1,
  approvalStatus: 0,
  totalItems: 0,
  totalQuantity: 0,
  totalAmount: 0,
  increaseQuantity: 0,
  decreaseQuantity: 0,
  isUrgent: false,
  isAffectCost: false,
  remark: '',
  createdByName: '',
  createdAt: '',
  modifiedAt: '',
  approverName: '',
  approveTime: '',
  executeByName: '',
  actualExecuteTime: ''
});

// 调整明细列表
const adjustDetailList = ref([]);

// 仓库列表
const warehouseList = ref([]);
// 调整单列表
const adjustOrderList = ref([]);
// 盘点单选项
const stockTakeOptions = ref([]);
// 商品列表
const productList = ref([]);
// 货架选项
const shelfOptions = ref([]);
// 已选商品
const selectedProducts = ref([]);

// 选项数据
const adjustTypeOptions = [
  { value: 1, label: '盘点调整' },
  { value: 2, label: '报损调整' },
  { value: 3, label: '报溢调整' },
  { value: 4, label: '成本调整' },
  { value: 5, label: '库存转移' },
  { value: 6, label: '其他调整' }
];

const sourceTypeOptions = [
  { value: 1, label: '盘点单' },
  { value: 2, label: '手动创建' },
  { value: 3, label: '异常处理' },
  { value: 4, label: '系统自动' }
];

const adjustStatusOptions = [
  { value: 1, label: '待提交' },
  { value: 2, label: '待审核' },
  { value: 3, label: '审核通过' },
  { value: 4, label: '审核拒绝' },
  { value: 5, label: '已执行' },
  { value: 6, label: '已取消' }
];

// 表单验证规则
const formRules = {
  warehouseId: [
    { required: true, message: '请选择仓库', trigger: 'change' }
  ],
  adjustType: [
    { required: true, message: '请选择调整类型', trigger: 'change' }
  ],
  sourceType: [
    { required: true, message: '请选择来源类型', trigger: 'change' }
  ],
  adjustReason: [
    { required: true, message: '请输入调整原因', trigger: 'blur' },
    { min: 2, max: 200, message: '长度在2到200个字符', trigger: 'blur' }
  ],
  sourceId: [
    { 
      validator: (rule, value, callback) => {
        if (formData.sourceType === 1 && !value) {
          callback(new Error('请选择盘点单'));
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
  return isEdit.value ? '编辑调整单' : '新建调整单';
});

const selectedWarehouseId = computed(() => warehouseForm.warehouseId);
const selectedWarehouseName = computed(() => {
  const warehouse = warehouseList.value.find(w => w.id === warehouseForm.warehouseId);
  return warehouse ? warehouse.name : '';
});

const totalAdjustQuantity = computed(() => {
  return adjustItems.value.reduce((sum, item) => sum + Math.abs(item.adjustQuantity || 0), 0);
});

const totalIncreaseQuantity = computed(() => {
  return adjustItems.value.reduce((sum, item) => {
    return item.adjustQuantity > 0 ? sum + item.adjustQuantity : sum;
  }, 0);
});

const totalDecreaseQuantity = computed(() => {
  return adjustItems.value.reduce((sum, item) => {
    return item.adjustQuantity < 0 ? sum + Math.abs(item.adjustQuantity) : sum;
  }, 0);
});

const totalAdjustAmount = computed(() => {
  return adjustItems.value.reduce((sum, item) => sum + (item.adjustAmount || 0), 0);
});

// 方法
const loadWarehouseList = async () => {
  try {
    const res = await get('/api/auth/warehouse/list');
    warehouseList.value = res || [];
  } catch (error) {
    console.error('加载仓库列表失败:', error);
    warehouseList.value = [];
  }
};

const loadAdjustOrderList = async () => {
  if (!selectedWarehouseId.value) {
    adjustOrderList.value = [];
    return;
  }
  
  loading.value = true;
  try {
    const params = {
      page: pagination.current,
      size: pagination.size,
      warehouseId: selectedWarehouseId.value,
      ...filterForm
    };
    
    if (filterForm.dateRange && filterForm.dateRange.length === 2) {
      params.startDate = filterForm.dateRange[0];
      params.endDate = filterForm.dateRange[1];
    }
    
    const res = await post('/api/auth/adjust/pageList', params);
    if (res && res.records) {
      adjustOrderList.value = res.records.map(item => ({
        id: item.id || '',
        adjustNo: item.adjustNo || '',
        warehouseId: item.warehouseId || '',
        warehouseName: item.warehouseName || '',
        adjustType: item.adjustType || 1,
        sourceType: item.sourceType || 1,
        sourceId: item.sourceId || '',
        sourceNo: item.sourceNo || '',
        adjustReason: item.adjustReason || '',
        adjustStatus: item.adjustStatus || 1,
        totalItems: item.totalItems || 0,
        totalQuantity: item.totalQuantity || 0,
        totalAmount: item.totalAmount || 0,
        isUrgent: item.isUrgent || false,
        isAffectCost: item.isAffectCost || false,
        createdByName: item.createdByName || '',
        createdAt: item.createdAt || '',
        modifiedAt: item.modifiedAt || ''
      }));
      pagination.total = res.total || 0;
    } else {
      adjustOrderList.value = [];
      pagination.total = 0;
    }
  } catch (error) {
    console.error('加载调整单列表失败:', error);
    ElMessage.error('加载调整单列表失败');
    adjustOrderList.value = [];
  } finally {
    loading.value = false;
  }
};

const loadStockTakeOptions = async () => {
  if (!selectedWarehouseId.value) return;
  
  try {
    const res = await get(`/api/auth/stock/completedStockTakeList?warehouseId=${selectedWarehouseId.value}`);
    stockTakeOptions.value = res || [];
  } catch (error) {
    console.error('加载盘点单列表失败:', error);
    stockTakeOptions.value = [];
    ElMessage.error('加载盘点单列表失败');
  }
};

const loadProductList = async () => {
  if (!selectedWarehouseId.value) {
    ElMessage.warning('请先选择仓库');
    return;
  }
  
  productLoading.value = true;
  try {
    const params = {
      page: productPagination.current,
      size: productPagination.size,
      warehouseId: selectedWarehouseId.value,
      keyword: productFilter.keyword
    };
    
    const res = await post('/api/auth/product/listByWarehouse', params);
    if (res && res.records) {
      productList.value = res.records.map(item => ({
        id: item.id || '',
        productCode: item.productCode || '',
        skuCode: item.skuCode || '',
        productName: item.productName || '',
        specification: item.specification || '',
        unit: item.unit || '',
        currentQuantity: item.currentQuantity || 0
      }));
      productPagination.total = res.total || 0;
    } else {
      productList.value = [];
      productPagination.total = 0;
    }
  } catch (error) {
    console.error('加载商品列表失败:', error);
    ElMessage.error('加载商品列表失败');
    productList.value = [];
  } finally {
    productLoading.value = false;
  }
};

const loadShelfOptions = async () => {
  if (!selectedWarehouseId.value) return;
  
  try {
    const res = await get(`/api/auth/inventory/allShelfOfWareHouse?warehouseId=${selectedWarehouseId.value}`);
    shelfOptions.value = res || [];
  } catch (error) {
    console.error('加载货架列表失败:', error);
    shelfOptions.value = [];
  }
};

const loadAdjustDetailList = async (adjustOrderId) => {
  detailLoading.value = true;
  try {
    const params = {
      adjustOrderId,
      page: detailPagination.current,
      size: detailPagination.size
    };
    
    const res = await post('/api/auth/adjust/itemPageList', params);
    if (res && res.records) {
      adjustDetailList.value = res.records.map(item => ({
        id: item.id || '',
        productId: item.productId || '',
        skuCode: item.skuCode || '',
        productName: item.productName || '',
        batchNo: item.batchNo || '',
        shelfId: item.shelfId || '',
        shelfCode: item.shelfCode || '',
        beforeQuantity: item.beforeQuantity || 0,
        adjustQuantity: item.adjustQuantity || 0,
        afterQuantity: item.afterQuantity || 0,
        unitCost: item.unitCost || 0,
        adjustAmount: item.adjustAmount || 0,
        adjustReason: item.adjustReason || '',
        status: item.status || 1
      }));
      detailPagination.total = res.total || 0;
    } else {
      adjustDetailList.value = [];
      detailPagination.total = 0;
    }
  } catch (error) {
    console.error('加载调整明细失败:', error);
    ElMessage.error('加载调整明细失败');
    adjustDetailList.value = [];
  } finally {
    detailLoading.value = false;
  }
};

const handleWarehouseSelect = () => {
  // 重置筛选条件
  Object.assign(filterForm, {
    adjustNo: '',
    adjustType: '',
    sourceType: '',
    adjustStatus: '',
    dateRange: []
  });
  
  // 重置分页
  pagination.current = 1;
  
  // 加载调整单列表
  if (selectedWarehouseId.value) {
    loadAdjustOrderList();
    loadStockTakeOptions();
    loadShelfOptions();
  } else {
    adjustOrderList.value = [];
  }
};

const handleSearch = () => {
  pagination.current = 1;
  loadAdjustOrderList();
};

const handleReset = () => {
  Object.assign(filterForm, {
    adjustNo: '',
    adjustType: '',
    sourceType: '',
    adjustStatus: '',
    dateRange: []
  });
  pagination.current = 1;
  loadAdjustOrderList();
};

const handleSizeChange = (size) => {
  pagination.size = size;
  pagination.current = 1;
  loadAdjustOrderList();
};

const handleCurrentChange = (page) => {
  pagination.current = page;
  loadAdjustOrderList();
};

const handleDetailSizeChange = (size) => {
  detailPagination.size = size;
  detailPagination.current = 1;
  loadAdjustDetailList(currentAdjustOrder.id);
};

const handleDetailCurrentChange = (page) => {
  detailPagination.current = page;
  loadAdjustDetailList(currentAdjustOrder.id);
};

const handleProductSizeChange = (size) => {
  productPagination.size = size;
  productPagination.current = 1;
  loadProductList();
};

const handleProductCurrentChange = (page) => {
  productPagination.current = page;
  loadProductList();
};

const handleCreate = () => {
  isEdit.value = false;
  resetForm();
  
  // 设置当前选择的仓库
  formData.warehouseId = selectedWarehouseId.value;
  formData.warehouseName = selectedWarehouseName.value;
  
  formDialogVisible.value = true;
  showAdjustItems.value = true;
  
  // 加载盘点单选项
  loadStockTakeOptions();
};

const handleEdit = async (adjustOrder) => {
  isEdit.value = true;
  resetForm();
  
  // 加载数据
  try {
    const res = await get(`/api/auth/adjust/detail?id=${adjustOrder.id}`);
    if (res) {
      Object.assign(formData, {
        id: res.id || '',
        adjustNo: res.adjustNo || '',
        warehouseId: res.warehouseId || '',
        warehouseName: res.warehouseName || '',
        adjustType: res.adjustType || 1,
        sourceType: res.sourceType || 1,
        sourceId: res.sourceId || '',
        sourceNo: res.sourceNo || '',
        adjustReason: res.adjustReason || '',
        isUrgent: res.isUrgent || false,
        isAffectCost: res.isAffectCost || false,
        remark: res.remark || ''
      });
      
      // 加载调整明细
      const detailRes = await post('/api/auth/adjust/itemList', { adjustOrderId: adjustOrder.id });
      adjustItems.value = detailRes || [];
      
      formDialogVisible.value = true;
      showAdjustItems.value = true;
    }
  } catch (error) {
    console.error('加载调整单详情失败:', error);
    ElMessage.error('加载调整单详情失败');
  }
};

const handleView = async (adjustOrder) => {
  try {
    viewLoading.value = true;
    Object.assign(currentAdjustOrder, adjustOrder);
    
    // 重置分页
    detailPagination.current = 1;
    detailPagination.size = 20;
    
    // 加载明细
    await loadAdjustDetailList(adjustOrder.id);
    
    viewDialogVisible.value = true;
  } catch (error) {
    console.error('加载调整单详情失败:', error);
    ElMessage.error('加载调整单详情失败');
  } finally {
    viewLoading.value = false;
  }
};

const handleSubmit = async (adjustOrder) => {
  try {
    await ElMessageBox.confirm(
      '确定要提交审核吗？提交后不可再编辑。',
      '提交确认',
      { type: 'warning' }
    );
    
    const res = await get('/api/auth/adjust/submit?id=' + adjustOrder.id);
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

const handleApprove = async (adjustOrder) => {
  try {
    await ElMessageBox.confirm(
      '确定要审核通过吗？',
      '审核确认',
      { type: 'warning' }
    );
    
    const res = await get('/api/auth/adjust/approve?id=' + adjustOrder.id);
    if (res) {
      ElMessage.success('审核通过成功');
      refreshList();
      if (viewDialogVisible.value) {
        viewDialogVisible.value = false;
      }
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('审核通过失败');
    }
  }
};

const handleReject = async (adjustOrder) => {
  try {
    await ElMessageBox.prompt('请输入拒绝原因', '审核拒绝', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      inputType: 'textarea',
      inputPlaceholder: '请输入拒绝原因...',
      inputValidator: (value) => {
        if (!value || value.trim().length < 2) {
          return '拒绝原因不能少于2个字符';
        }
        return true;
      }
    }).then(async ({ value }) => {
      const res = await post('/api/auth/adjust/reject', {
        id: adjustOrder.id,
        rejectReason: value
      });
      
      if (res) {
        ElMessage.success('审核拒绝成功');
        refreshList();
        if (viewDialogVisible.value) {
          viewDialogVisible.value = false;
        }
      }
    }).catch(() => {
      // 用户取消
    });
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('审核拒绝失败');
    }
  }
};

const handleExecute = async (adjustOrder) => {
  try {
    await ElMessageBox.confirm(
      '确定要执行调整吗？执行后库存数据将被更新。',
      '执行确认',
      { type: 'warning' }
    );
    
    const res = await get('/api/auth/adjust/execute?id=' + adjustOrder.id);
    if (res) {
      ElMessage.success('执行调整成功');
      refreshList();
      if (viewDialogVisible.value) {
        viewDialogVisible.value = false;
      }
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('执行调整失败');
    }
  }
};

const handleDelete = async (adjustOrder) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除调整单"${adjustOrder.adjustNo}"吗？`,
      '删除确认',
      { type: 'warning' }
    );
    
    const res = await get('/api/auth/adjust/delete?id=' + adjustOrder.id);
    if (res) {
      ElMessage.success('删除成功');
      refreshList();
      if (viewDialogVisible.value) {
        viewDialogVisible.value = false;
      }
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败');
    }
  }
};

const handleSourceTypeChange = (sourceType) => {
  if (sourceType === 1) {
    // 来源为盘点单时，需要选择盘点单
    formData.sourceId = '';
    formData.sourceNo = '';
    showAdjustItems.value = false;
    
    // 如果是新建状态，清空调整明细
    if (!isEdit.value) {
      adjustItems.value = [];
    }
  } else {
    // 其他来源类型
    formData.sourceId = '';
    formData.sourceNo = '';
    showAdjustItems.value = true;
  }
};

const handleStockTakeSelect = async (stockTakeId) => {
  if (!stockTakeId) {
    adjustItems.value = [];
    showAdjustItems.value = false;
    return;
  }
  
  try {
   // 获取盘点单差异项
      const diffRes = await post('/api/auth/stock/itemList', { stockTakeId });
      if (diffRes && diffRes.length > 0) {
        // 将盘点差异转换为调整明细
        adjustItems.value = diffRes.map(item => ({
          productId: item.productId,
          productCode: item.productCode,
          skuCode: item.skuCode,
          productName: item.productName,
          specification: item.specification,
          unit: item.unit,
          batchNo: item.batchNo,
          shelfId: item.shelfId,
          shelfCode: item.shelfCode,
          beforeQuantity: item.systemQuantity || 0,
          adjustQuantity: item.diffQuantity || 0,
          afterQuantity: (item.systemQuantity || 0) + (item.diffQuantity || 0),
          unitCost: item.unitCost || 0,
          adjustAmount: Math.abs((item.diffQuantity || 0) * (item.unitCost || 0)),
          itemReason: `盘点差异调整 - ${item.diffQuantity > 0 ? '盘盈' : '盘亏'}`,
          sourceItemId: item.id
        }));
        
        showAdjustItems.value = true;
        
      }
  } catch (error) {
    console.error('加载盘点单详情失败:', error);
    ElMessage.error('加载盘点单详情失败');
    adjustItems.value = [];
    showAdjustItems.value = false;
  }
};

const handleAddItem = () => {
  // 清空已选商品
  selectedProducts.value = [];
  
  // 重置商品筛选
  productFilter.keyword = '';
  productPagination.current = 1;
  
  // 加载商品列表
  loadProductList();
  
  // 显示商品选择对话框
  productDialogVisible.value = true;
};

const handleProductSelectionChange = (selection) => {
  selectedProducts.value = selection;
};

const handleConfirmProducts = () => {
  // 将选中的商品添加到调整明细中
  selectedProducts.value.forEach(product => {
    // 检查是否已存在
    const exists = adjustItems.value.some(item => item.productId === product.id);
    if (!exists) {
      adjustItems.value.push({
        productId: product.id,
        productCode: product.productCode,
        skuCode: product.skuCode,
        productName: product.productName,
        specification: product.specification,
        unit: product.unit,
        batchNo: '',
        shelfId: '',
        shelfCode: '',
        beforeQuantity: product.currentQuantity || 0,
        adjustQuantity: 0,
        afterQuantity: product.currentQuantity || 0,
        unitCost: 0,
        adjustAmount: 0,
        itemReason: '',
        sourceItemId: null
      });
    }
  });
  
  productDialogVisible.value = false;
  selectedProducts.value = [];
};

const handleRemoveItem = (item) => {
  const index = adjustItems.value.indexOf(item);
  if (index > -1) {
    adjustItems.value.splice(index, 1);
  }
};

const handleBatchNoChange = (item) => {
  // 这里可以添加批次验证逻辑
  console.log('批次号变更:', item.batchNo);
};

const handleShelfChange = (item) => {
  const shelf = shelfOptions.value.find(s => s.id === item.shelfId);
  if (shelf) {
    item.shelfCode = shelf.code || shelf.name;
  } else {
    item.shelfCode = '';
  }
};

const handleAdjustQuantityChange = (item) => {
  // 计算调整后数量
  item.afterQuantity = (item.beforeQuantity || 0) + (item.adjustQuantity || 0);
  
  // 计算调整金额
  item.adjustAmount = Math.abs((item.adjustQuantity || 0) * (item.unitCost || 0));
};

const handleCostChange = (item) => {
  // 计算调整金额
  item.adjustAmount = Math.abs((item.adjustQuantity || 0) * (item.unitCost || 0));
};

const handleFormSubmit = async () => {
  if (!formRef.value) return;
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      // 验证调整明细
      if (adjustItems.value.length === 0) {
        ElMessage.warning('请至少添加一条调整明细');
        return;
      }
      
      formLoading.value = true;
      try {
        // 准备提交数据
        const submitData = {
          ...formData,
          items: adjustItems.value.map(item => ({
            productId: item.productId,
            productCode: item.productCode,
            skuCode: item.skuCode,
            productName: item.productName,
            specification: item.specification,
            unit: item.unit,
            batchNo: item.batchNo || null,
            shelfId: item.shelfId || null,
            shelfCode: item.shelfCode || null,
            beforeQuantity: item.beforeQuantity,
            adjustQuantity: item.adjustQuantity,
            afterQuantity: item.afterQuantity,
            unitCost: item.unitCost || 0,
            adjustAmount: item.adjustAmount || 0,
            itemReason: item.itemReason || formData.adjustReason,
            sourceItemId: item.sourceItemId || null
          }))
        };
        
        const url = isEdit.value ? '/api/auth/adjust/update' : '/api/auth/adjust/create';
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

const handleExportAdjustData = async () => {
  try {
    const res = await post('/api/auth/adjust/export', { 
      adjustOrderId: currentAdjustOrder.id 
    }, { responseType: 'blob' });
    
    const url = window.URL.createObjectURL(new Blob([res]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `${currentAdjustOrder.adjustNo}_调整明细.xlsx`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  } catch (error) {
    console.error('导出失败:', error);
    ElMessage.error('导出失败');
  }
};

const refreshList = () => {
  if (selectedWarehouseId.value) {
    loadAdjustOrderList();
  }
};

const resetForm = () => {
  Object.assign(formData, {
    id: '',
    adjustNo: '',
    warehouseId: '',
    warehouseName: '',
    adjustType: 1,
    sourceType: 1,
    sourceId: '',
    sourceNo: '',
    adjustReason: '',
    isUrgent: false,
    isAffectCost: false,
    remark: ''
  });
  
  adjustItems.value = [];
  showAdjustItems.value = false;
  
  if (formRef.value) {
    formRef.value.clearValidate();
  }
};

// 工具方法
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

const getSourceTypeLabel = (type) => {
  const mapping = {
    1: '盘点单',
    2: '手动创建',
    3: '异常处理',
    4: '系统自动'
  };
  return mapping[type] || '未知';
};

const getStatusLabel = (status) => {
  const mapping = {
    1: '待提交',
    2: '待审核',
    3: '审核通过',
    4: '审核拒绝',
    5: '已执行',
    6: '已取消'
  };
  return mapping[status] || '未知';
};

const getStatusTagType = (status) => {
  const mapping = {
    1: 'info',
    2: 'warning',
    3: 'success',
    4: 'danger',
    5: 'primary',
    6: 'info'
  };
  return mapping[status] || 'info';
};

const getItemStatusLabel = (status) => {
  const mapping = {
    1: '待执行',
    2: '已执行',
    3: '已取消'
  };
  return mapping[status] || '未知';
};

const getItemStatusTagType = (status) => {
  const mapping = {
    1: 'warning',
    2: 'success',
    3: 'danger'
  };
  return mapping[status] || 'info';
};

const getQuantityClass = (quantity) => {
  if (quantity > 0) return 'quantity-positive';
  if (quantity < 0) return 'quantity-negative';
  return 'quantity-zero';
};

const getAdjustQuantityClass = (quantity) => {
  if (quantity > 0) return 'adjust-positive';
  if (quantity < 0) return 'adjust-negative';
  return 'adjust-zero';
};

const getAfterQuantityClass = (quantity) => {
  if (quantity > 0) return 'after-positive';
  return 'after-zero';
};

const getAmountClass = (amount) => {
  if (amount > 0) return 'amount-positive';
  if (amount < 0) return 'amount-negative';
  return 'amount-zero';
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

const formatCurrency = (num) => {
  if (num === null || num === undefined) return '0.00';
  const number = Number(num);
  if (isNaN(number)) return '0.00';
  return number.toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',');
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
  loadWarehouseList();
});
</script>

<style scoped>
/* 使用与盘点页面相同的样式体系，保持一致性 */
.adjust-order-manage-container {
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

.warehouse-select-section {
  padding: 20px 0;
  border-bottom: 1px solid #ebeef5;
  background: linear-gradient(135deg, #f6f8ff 0%, #f0f7ff 100%);
  border-radius: 8px;
  margin-bottom: 20px;
}

.filter-section {
  padding: 20px 0;
  border-bottom: 1px solid #ebeef5;
}

.adjust-order-list-section {
  margin-top: 20px;
}

.adjust-order-table {
  width: 100%;
}

.adjust-order-no {
  font-family: 'Courier New', monospace;
  font-weight: bold;
  color: #409EFF;
}

.source-info {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #606266;
}

.source-info .el-icon {
  color: #909399;
}

.detail-info {
  color: #606266;
  font-size: 14px;
}

/* 数量相关样式 */
.quantity-positive, .after-positive {
  color: #67C23A;
  font-weight: bold;
}

.quantity-negative, .adjust-negative {
  color: #F56C6C;
  font-weight: bold;
}

.quantity-zero, .adjust-zero, .after-zero {
  color: #909399;
}

.adjust-positive {
  color: #409EFF;
  font-weight: bold;
}

/* 金额相关样式 */
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

.no-warehouse-tip {
  padding: 60px 0;
  text-align: center;
}

.no-warehouse-tip .el-empty__description {
  margin-top: 10px;
}

/* 表单对话框样式 */
.adjust-order-form-dialog :deep(.el-dialog__header) {
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 15px;
}

.adjust-order-form-container {
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

.reason-textarea, .remark-textarea {
  width: 100%;
}

/* 调整明细样式 */
.adjust-items-container {
  padding: 0 20px 20px;
}

.no-items-tip {
  padding: 40px 0;
  text-align: center;
}

.adjust-items-table {
  width: 100%;
  margin-bottom: 16px;
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

.product-spec {
  font-size: 12px;
  color: #606266;
}

.adjust-quantity-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.quantity-tips {
  font-size: 12px;
}

.quantity-tips .positive {
  color: #67C23A;
}

.quantity-tips .negative {
  color: #F56C6C;
}

.quantity-tips .zero {
  color: #909399;
}

.before-quantity {
  color: #606266;
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

.product-select-table {
  width: 100%;
}

.product-pagination {
  display: flex;
  justify-content: flex-end;
}

/* 查看对话框样式 */
.adjust-order-view-dialog :deep(.el-dialog__header) {
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 15px;
}

.adjust-order-view-container {
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
  grid-template-columns: repeat(5, 1fr);
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

.stat-icon.quantity-positive {
  background: linear-gradient(135deg, #67C23A 0%, #85ce61 100%);
}

.stat-icon.quantity-negative {
  background: linear-gradient(135deg, #F56C6C 0%, #f78989 100%);
}

.stat-icon.amount-positive {
  background: linear-gradient(135deg, #67C23A 0%, #85ce61 100%);
}

.stat-icon.amount-negative {
  background: linear-gradient(135deg, #F56C6C 0%, #f78989 100%);
}

.stat-icon .el-icon {
  font-size: 24px;
  color: white;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 20px;
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

.detail-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
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
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .adjust-order-manage-container {
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
  
  .warehouse-select-section,
  .filter-section .el-form-item {
    margin-bottom: 12px;
  }
  
  .action-buttons {
    flex-direction: column;
    gap: 4px;
  }
  
  .adjust-order-view-dialog,
  .adjust-order-form-dialog {
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
  
  .items-statistics {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .cancel-btn, .submit-btn {
    min-width: 80px;
  }
}

/* 动画效果 */
.adjust-order-table :deep(.el-table__row) {
  transition: all 0.3s;
}

.adjust-order-table :deep(.el-table__row:hover) {
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

.adjust-order-view-container {
  animation: fadeIn 0.3s ease;
}

.form-card {
  animation: slideUp 0.3s ease;
}
</style>