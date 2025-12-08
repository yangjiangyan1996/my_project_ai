<!-- SalesOutboundCreate.vue -->
<template>
  <div class="outbound-create-container">
    <el-card class="form-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">{{ isEditMode ? '编辑销售出库单' : '新建销售出库单' }}</span>
          <div class="header-actions">
            <el-button @click="handleReset">重置</el-button>
            <el-button 
              type="primary" 
              @click="handleSaveDraft" 
              :loading="loading"
              v-if="!isEditMode || (isEditMode && formData.status === 0)"
            >
              保存草稿
            </el-button>
            <el-button 
              type="primary" 
              @click="handleSubmit" 
              :loading="loading"
              v-if="!isEditMode || (isEditMode && (formData.status === 0 || formData.status === 4))"
            >
              {{ isEditMode ? '更新提交' : '提交审核' }}
            </el-button>
          </div>
        </div>
      </template>

      <!-- 基本信息 -->
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
        class="outbound-form"
      >
        <el-row :gutter="24">
          <el-col :xs="24" :sm="12" :lg="8">
            <el-form-item label="出库单号" prop="orderNo">
              <el-input v-model="formData.orderNo" placeholder="系统自动生成" disabled />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :lg="8">
            <el-form-item label="出库类型" prop="orderType">
              <el-select
                v-model="formData.orderType"
                placeholder="请选择出库类型"
                style="width: 100%"
                disabled
              >
                <el-option
                  :value="1"
                  label="销售出库"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :lg="8">
            <el-form-item label="出库仓库" prop="warehouseId">
              <el-select
                v-model="formData.warehouseId"
                placeholder="请选择仓库"
                style="width: 100%"
                filterable
                @change="handleWarehouseChange"
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
        </el-row>

        <el-row :gutter="24">
          <el-col :xs="24" :sm="12" :lg="8">
            <el-form-item label="客户" prop="customerId">
              <el-select
                v-model="formData.customerId"
                placeholder="请选择客户"
                style="width: 100%"
                filterable
                :disabled="isViewMode"
                @change="handleCustomerChange"
              >
                <el-option
                  v-for="customer in customerList"
                  :key="customer.id"
                  :label="customer.customerName"
                  :value="customer.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :lg="8">
            <el-form-item label="预计出库日期" prop="expectedDate">
              <el-date-picker
                v-model="formData.expectedDate"
                type="date"
                placeholder="选择预计出库日期"
                style="width: 100%"
                value-format="YYYY-MM-DD"
                :disabled="isViewMode"
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :lg="8">
            <el-form-item label="关联单号" prop="relatedOrderNo">
              <el-input
                v-model="formData.relatedOrderNo"
                placeholder="请输入关联单号"
                :disabled="isViewMode"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="formData.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
            maxlength="500"
            show-word-limit
            :disabled="isViewMode"
          />
        </el-form-item>
      </el-form>

      <!-- 智能推荐区域 -->
      <div class="recommendation-section" v-if="!isViewMode && showRecommendations && recommendations.length > 0">
        <div class="section-header">
          <div class="recommendation-header-left">
            <h3>
              <el-icon><MagicStick /></el-icon>
              智能推荐
            </h3>
            <span class="tip">基于该客户的购买习惯，系统为您推荐以下产品</span>
          </div>
          <div class="recommendation-header-right">
            <el-button type="text" @click="addAllRecommendations" :disabled="!hasValidRecommendations">
              一键添加全部
            </el-button>
            <el-button type="text" @click="closeRecommendations">
              <el-icon><Close /></el-icon>
            </el-button>
          </div>
        </div>
        
        <el-table :data="recommendations" border class="recommendation-table" v-loading="loadingRecommendations">
          <el-table-column label="推荐产品" min-width="200">
            <template #default="{ row }">
              <div class="product-info">
                <div class="product-name">{{ row.productName }}</div>
                <div class="sku-text">{{ row.sku }}</div>
                <div class="spec-text">{{ row.spec || '-' }}</div>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="推荐理由" width="140">
            <template #default="{ row }">
              <div class="recommend-reason">
                <el-tag size="small" :type="row.reasonType || 'info'">
                  {{ row.reasonText }}
                </el-tag>
                <div class="confidence" v-if="row.confidence">
                  置信度: {{ (row.confidence * 100).toFixed(0) }}%
                </div>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="通常搭配" width="120">
            <template #default="{ row }">
              <div class="trigger-products">
                <el-popover
                  placement="top"
                  :width="200"
                  trigger="hover"
                  v-if="row.triggerProducts && row.triggerProducts.length > 0"
                >
                  <template #reference>
                    <el-tag size="small" type="success">
                      搭配{{ row.triggerProducts.length }}个产品
                    </el-tag>
                  </template>
                  <div class="trigger-list">
                    <div v-for="trigger in row.triggerProducts" :key="trigger.productId" class="trigger-item">
                      {{ trigger.productName }}
                    </div>
                  </div>
                </el-popover>
                <span v-else class="no-trigger">通用推荐</span>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="库存" width="80" align="center">
            <template #default="{ row }">
              <span :class="getStockClass(row.availableQuantity, row.recommendQuantity)">
                {{ row.availableQuantity }}
              </span>
            </template>
          </el-table-column>
          
          <el-table-column label="推荐数量" width="120">
            <template #default="{ row }">
              <el-input-number
                v-model="row.recommendQuantity"
                :min="0"
                :max="row.availableQuantity"
                controls-position="right"
                size="small"
                @change="() => handleRecommendQuantityChange(row)"
                :disabled="row.availableQuantity <= 0"
              />
            </template>
          </el-table-column>
          
          <el-table-column label="单价" width="120">
            <template #default="{ row }">
              <el-input-number
                v-model="row.price"
                :min="0"
                :precision="2"
                controls-position="right"
                size="small"
                :disabled="row.recommendQuantity <= 0 || row.availableQuantity <= 0"
              >
                <template #prefix>¥</template>
              </el-input-number>
            </template>
          </el-table-column>
          
          <el-table-column label="USD单价" width="120">
            <template #default="{ row }">
              <el-input-number
                v-model="row.priceUnitUsd"
                :min="0"
                :precision="2"
                controls-position="right"
                size="small"
                :disabled="row.recommendQuantity <= 0 || row.availableQuantity <= 0"
              >
                <template #prefix>$</template>
              </el-input-number>
            </template>
          </el-table-column>
          
          <el-table-column label="操作" width="120" align="center" fixed="right">
            <template #default="{ row }">
              <div class="recommend-actions">
                <el-button
                  type="primary"
                  link
                  @click="addRecommendToOrder(row)"
                  :disabled="row.recommendQuantity <= 0 || row.availableQuantity <= 0 || row.added"
                >
                  {{ row.added ? '已添加' : '添加' }}
                </el-button>
                <el-button
                  type="text"
                  size="small"
                  @click="ignoreRecommendation(row)"
                >
                  忽略
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 产品明细 -->
      <div class="product-section">
        <div class="section-header">
          <div class="section-header-left">
            <h3>产品明细</h3>
            <el-button 
              type="success" 
              @click="loadRecommendations"
              :loading="loadingRecommendations"
              v-if="!isViewMode && formData.customerId && formData.warehouseId"
              class="recommend-btn"
            >
              <el-icon><MagicStick /></el-icon>
              智能推荐
            </el-button>
          </div>
          <div class="header-right-actions" v-if="!isViewMode">
            <!-- 产品搜索 -->
            <el-input
              v-model="productSearch"
              placeholder="搜索产品..."
              clearable
              style="width: 200px; margin-right: 10px;"
              @input="filterProducts"
              size="small"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            
            <el-dropdown @command="handleBatchCommand" trigger="click">
              <el-button type="primary" size="small">
                <el-icon><Operation /></el-icon>
                批量操作
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="clearAll" :disabled="!hasProductsWithQuantity">
                    <el-icon><Delete /></el-icon>
                    清空所有数量
                  </el-dropdown-item>
                  <el-dropdown-item command="resetPrices" :disabled="!hasProductsWithQuantity">
                    <el-icon><Refresh /></el-icon>
                    重置价格
                  </el-dropdown-item>
                  <el-dropdown-item command="clearBatches" :disabled="!hasBatchAllocations">
                    <el-icon><CloseBold /></el-icon>
                    清空批次分配
                  </el-dropdown-item>
                  <el-dropdown-item command="exportData">
                    <el-icon><Download /></el-icon>
                    导出当前数据
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            
            <el-button 
              type="success" 
              @click="handleDownloadTemplate"
              :loading="downloadLoading"
              size="small"
            >
              <el-icon><Download /></el-icon>
              下载模板
            </el-button>
            <el-button 
              type="warning" 
              @click="handleImportExcel"
              size="small"
            >
              <el-icon><Upload /></el-icon>
              导入模板
            </el-button>
          </div>
        </div>

        <!-- 统计信息 -->
        <div class="quick-stats" v-if="filteredProducts.length > 0">
          <el-row :gutter="10">
            <el-col :span="3">
              <div class="stat-item">
                <span class="stat-label">筛选:</span>
                <span class="stat-value">{{ filteredProducts.length }} 项</span>
              </div>
            </el-col>
            <el-col :span="3">
              <div class="stat-item">
                <span class="stat-label">已选:</span>
                <span class="stat-value" :class="selectedProductCount > 0 ? 'stat-active' : ''">
                  {{ selectedProductCount }} 项
                </span>
              </div>
            </el-col>
            <el-col :span="4">
              <div class="stat-item">
                <span class="stat-label">总数量:</span>
                <span class="stat-value">{{ totalQuantity }} 个</span>
              </div>
            </el-col>
            <el-col :span="4">
              <div class="stat-item">
                <span class="stat-label">总金额:</span>
                <span class="stat-value">¥ {{ totalAmount.toFixed(2) }}</span>
              </div>
            </el-col>
            <el-col :span="4">
              <div class="stat-item">
                <span class="stat-label">USD总额:</span>
                <span class="stat-value">$ {{ totalAmountUsd.toFixed(2) }}</span>
              </div>
            </el-col>
            <el-col :span="4">
              <div class="stat-item">
                <span class="stat-label">批次状态:</span>
                <span class="stat-value" :class="batchStatusClass">
                  {{ batchStatusText }}
                </span>
              </div>
            </el-col>
          </el-row>
        </div>

        <!-- 销售出库的产品表格 -->
        <el-table
          v-if="formData.warehouseId"
          :data="filteredProducts"
          border
          class="product-table"
          empty-text="请先选择仓库"
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" width="60" align="center" />
          
          <el-table-column label="产品信息" min-width="200" fixed="left">
            <template #header>
              <div class="table-header">
                <span>产品信息</span>
                <el-tooltip content="点击产品名称查看历史价格" placement="top">
                  <el-icon class="header-icon"><InfoFilled /></el-icon>
                </el-tooltip>
              </div>
            </template>
            <template #default="{ row }">
              <div class="product-info-cell">
                <div class="product-name" @click="showProductHistory(row)">
                  {{ row.productName }}
                  <el-tag v-if="row.isRecommend" size="small" type="success" class="recommend-tag">
                    推荐
                  </el-tag>
                </div>
                <div class="product-details">
                  <div class="sku-text">{{ row.sku }}</div>
                  <div class="spec-text">{{ row.spec || '-' }}</div>
                  <div class="color-text">{{ row.color || '-' }}</div>
                </div>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="单位" width="60" align="center">
            <template #default="{ row }">
              <span>{{ row.unitName || '-' }}</span>
            </template>
          </el-table-column>
          
          <el-table-column label="库存" width="100" align="center">
            <template #default="{ row }">
              <div class="stock-info">
                <span :class="getStockClass(row.availableQuantity, row.quantity)">
                  {{ row.availableQuantity }}
                </span>
                <el-progress 
                  v-if="row.availableQuantity > 0 && row.quantity > 0" 
                  :percentage="Math.min(100, (row.quantity / row.availableQuantity) * 100)" 
                  :stroke-width="6"
                  :show-text="false"
                  :color="getStockColor(row.availableQuantity, row.quantity)"
                  class="stock-progress"
                />
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="历史价格" width="140">
            <template #default="{ row }">
              <div class="price-history" v-if="row.historyPrice">
                <div class="history-price-item">
                  <span class="price-label">最近:</span>
                  <span class="price-value">¥ {{ row.historyPrice.lastPrice?.toFixed(2) || '-' }}</span>
                </div>
                <div class="history-price-item">
                  <span class="price-label">平均:</span>
                  <span class="price-value">¥ {{ row.historyPrice.avgPrice?.toFixed(2) || '-' }}</span>
                </div>
              </div>
              <span v-else>-</span>
            </template>
          </el-table-column>
          
          <el-table-column label="出库数量" width="130">
            <template #default="{ row }">
              <div class="quantity-cell">
                <el-input-number
                  v-model="row.quantity"
                  :min="0"
                  :max="getQuantityMax(row)"
                  controls-position="right"
                  size="small"
                  @change="() => handleQuantityChangeForAll(row)"
                  placeholder="数量"
                  :disabled="isViewMode || row.availableQuantity <= 0"
                  class="quantity-input"
                />
                <el-button
                  v-if="!isViewMode && row.availableQuantity > 0"
                  type="text"
                  size="small"
                  @click="setMaxQuantity(row)"
                  class="max-btn"
                >
                  最大
                </el-button>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="单价" width="130">
            <template #default="{ row }">
              <el-input-number
                v-model="row.price"
                :min="0"
                :precision="2"
                controls-position="right"
                size="small"
                :disabled="!row.quantity || row.quantity <= 0 || isViewMode"
                class="price-input"
              >
                <template #prefix>¥</template>
              </el-input-number>
            </template>
          </el-table-column>
          
          <el-table-column label="金额" width="120" align="right">
            <template #default="{ row }">
              <span v-if="row.quantity > 0" class="amount-text">
                ¥ {{ ((row.price || 0) * (row.quantity || 0)).toFixed(2) }}
              </span>
              <span v-else>-</span>
            </template>
          </el-table-column>

          <el-table-column label="USD单价" width="130">
            <template #default="{ row }">
              <el-input-number
                v-model="row.priceUnitUsd"
                :min="0"
                :precision="2"
                controls-position="right"
                size="small"
                :disabled="!row.quantity || row.quantity <= 0 || isViewMode"
                class="price-input"
              >
                <template #prefix>$</template>
              </el-input-number>
            </template>
          </el-table-column>
          
          <el-table-column label="USD总额" width="120" align="right">
            <template #default="{ row }">
              <span v-if="row.quantity > 0" class="amount-text">
                $ {{ ((row.priceUnitUsd || 0) * (row.quantity || 0)).toFixed(2) }}
              </span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          
          <el-table-column label="批次分配" min-width="200">
            <template #default="{ row }">
              <div class="batch-allocation">
                <el-button 
                  type="primary" 
                  link 
                  @click="openBatchDialogForProduct(row)"
                  :disabled="!row.quantity || row.quantity <= 0 || isViewMode || row.availableQuantity <= 0"
                  class="batch-btn"
                >
                  分配批次
                </el-button>
                <div v-if="row.batchAllocations && row.batchAllocations.length > 0" class="batch-summary">
                  <el-popover
                    placement="top"
                    :width="300"
                    trigger="hover"
                  >
                    <template #reference>
                      <el-tag size="small" type="success" class="batch-tag">
                        {{ row.batchAllocations.length }}个批次
                      </el-tag>
                    </template>
                    <div class="batch-detail">
                      <div v-for="allocation in row.batchAllocations" :key="`${allocation.batchNo}-${allocation.shelfId}`" class="batch-detail-item">
                        <span class="batch-no">{{ allocation.batchNo }}</span>
                        <span class="shelf-name">{{ allocation.shelfName }}</span>
                        <span class="batch-quantity">{{ allocation.quantity }}个</span>
                      </div>
                    </div>
                  </el-popover>
                  <span class="allocation-total">
                    已分配: {{ row.batchAllocations.reduce((sum, alloc) => sum + (alloc.quantity || 0), 0) }}个
                  </span>
                </div>
                <div v-else class="batch-empty">
                  <span class="empty-text" v-if="row.quantity > 0 && row.availableQuantity > 0">未分配批次</span>
                  <span class="empty-text" v-else>-</span>
                </div>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="备注" min-width="150" fixed="right">
            <template #default="{ row }">
              <el-input
                v-model="row.remark"
                placeholder="产品备注"
                maxlength="100"
                show-word-limit
                :disabled="!row.quantity || row.quantity <= 0 || isViewMode"
                size="small"
              />
            </template>
          </el-table-column>
          
          <el-table-column label="操作" width="80" fixed="right" align="center" v-if="!isViewMode">
            <template #default="{ row }">
              <el-button
                type="danger"
                link
                @click="clearProductRow(row)"
                :disabled="row.quantity <= 0"
              >
                清空
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 底部统计信息 -->
        <div class="summary-info" v-if="hasProductsWithQuantity">
          <el-row :gutter="20">
            <el-col :span="3">
              <div class="summary-item">
                <span class="label">产品种类：</span>
                <span class="value">{{ productTypeCount }} 种</span>
              </div>
            </el-col>
            <el-col :span="4">
              <div class="summary-item">
                <span class="label">总数量：</span>
                <span class="value">{{ totalQuantity }} 个</span>
              </div>
            </el-col>
            <el-col :span="4">
              <div class="summary-item">
                <span class="label">总金额：</span>
                <span class="value highlight">¥ {{ totalAmount.toFixed(2) }}</span>
              </div>
            </el-col>
            <el-col :span="4">
              <div class="summary-item">
                <span class="label">USD总额：</span>
                <span class="value highlight">$ {{ totalAmountUsd.toFixed(2) }}</span>
              </div>
            </el-col>
            <el-col :span="4">
              <div class="summary-item">
                <span class="label">批次状态：</span>
                <span class="value" :class="batchStatusClass">
                  {{ batchStatusText }}
                  <el-tooltip v-if="hasInsufficientStock" content="存在批次分配不足的产品" placement="top">
                    <el-icon class="status-icon"><Warning /></el-icon>
                  </el-tooltip>
                </span>
              </div>
            </el-col>
            <el-col :span="5">
              <div class="summary-item">
                <span class="label">推荐产品：</span>
                <span class="value">{{ recommendedProductCount }} 个</span>
              </div>
            </el-col>
          </el-row>
        </div>
      </div>

      <!-- 附件上传 -->
      <div class="attachment-section" v-if="!isViewMode">
        <div class="section-header">
          <h3>附件上传</h3>
          <span class="tip">支持图片、文档等格式，单个文件不超过10MB</span>
        </div>
        <el-upload
          v-model:file-list="fileList"
          action="/api/auth/file/upload"
          multiple
          :limit="5"
          :on-exceed="handleExceed"
          :before-upload="beforeUpload"
          :on-success="handleUploadSuccess"
          :on-remove="handleRemoveFile"
          list-type="text"
        >
          <el-button type="primary">
            <el-icon><Upload /></el-icon>
            上传文件
          </el-button>
          <template #tip>
            <div class="el-upload__tip">
              支持 jpg, png, pdf, doc, docx, xls, xlsx 格式文件
            </div>
          </template>
        </el-upload>
      </div>

      <!-- 查看模式下的附件显示 -->
      <div class="attachment-section" v-else>
        <div class="section-header">
          <h3>附件</h3>
        </div>
        <div v-if="fileList.length > 0" class="attachment-list">
          <div v-for="file in fileList" :key="file.name" class="attachment-item">
            <el-icon><Document /></el-icon>
            <span class="file-name">{{ file.name }}</span>
            <el-button type="primary" link @click="handlePreview(file)">
              预览
            </el-button>
          </div>
        </div>
        <div v-else class="no-attachment">
          <span>暂无附件</span>
        </div>
      </div>
    </el-card>

    <!-- 批次分配对话框 -->
    <el-dialog
      v-model="batchDialog.visible"
      :title="`批次分配 - ${batchDialog.productName}`"
      width="900px"
      destroy-on-close
      @closed="handleBatchDialogClosed"
    >
      <div class="batch-dialog-content">
        <div class="batch-info">
          <div class="info-item">
            <span class="label">总出库数量：</span>
            <span class="value">{{ batchDialog.totalQuantity }}</span>
          </div>
          <div class="info-item">
            <span class="label">已分配数量：</span>
            <span class="value" :class="batchDialog.allocatedQuantity === batchDialog.totalQuantity ? 'success' : 'warning'">
              {{ batchDialog.allocatedQuantity }}
            </span>
          </div>
          <div class="info-item">
            <span class="label">剩余数量：</span>
            <span class="value">{{ batchDialog.remainingQuantity }}</span>
          </div>
          <div class="info-item">
            <el-button 
              type="primary" 
              link 
              @click="showAllocationStrategy = !showAllocationStrategy"
              size="small"
            >
              <el-icon><SetUp /></el-icon>
              分配策略
            </el-button>
          </div>
        </div>

        <!-- 分配策略面板 -->
        <div class="allocation-strategy" v-if="showAllocationStrategy">
          <div class="strategy-header">
            <span>分配策略</span>
            <el-button type="text" @click="showAllocationStrategy = false">
              <el-icon><ArrowUp /></el-icon>
            </el-button>
          </div>
          <div class="strategy-options">
            <el-radio-group v-model="allocationStrategy" size="small">
              <el-radio :label="'FIFO'">先进先出</el-radio>
              <el-radio :label="'NEAR_EXPIRE'">近效期优先</el-radio>
              <el-radio :label="'MIN_SHELF'">最少货架优先</el-radio>
              <el-radio :label="'MANUAL'">手动分配</el-radio>
            </el-radio-group>
            <el-button 
              type="primary" 
              size="small" 
              @click="applyAllocationStrategy"
              :loading="applyingStrategy"
            >
              应用策略
            </el-button>
          </div>
        </div>

        <!-- 批次分配表格 -->
        <el-table 
          :data="batchDialog.batches" 
          border 
          class="batch-table"
          v-loading="loadingBatches"
        >
          <el-table-column label="批次号" prop="batchNo" width="120" fixed="left" />
          <el-table-column label="生产日期" width="100">
            <template #default="{ row }">
              <span>{{ row.productionDate || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="有效期" width="100">
            <template #default="{ row }">
              <span :class="getExpiryClass(row.expiryDate)">{{ formatDate(row.expiryDate) || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="总可用数量" width="100" align="center">
            <template #default="{ row }">
              <span :class="row.quantity < 1 ? 'text-disabled' : ''">{{ row.quantity }}</span>
            </template>
          </el-table-column>
          <el-table-column label="货架分配" min-width="400">
            <template #default="{ row, $index: batchIndex }">
              <div class="shelf-allocation-container">
                <div v-if="row.shelfList && row.shelfList.length > 0" class="shelf-list">
                  <div 
                    v-for="(shelf, shelfIndex) in row.shelfList" 
                    :key="shelf.shelfId" 
                    class="shelf-item"
                    :class="{ 'shelf-disabled': shelf.quantity < 1 }"
                  >
                    <div class="shelf-info">
                      <span class="shelf-name">货架 {{ shelf.shelfName }}</span>
                      <span class="shelf-quantity" :class="shelf.quantity < 1 ? 'text-disabled' : ''">
                        可用: {{ shelf.quantity }}
                      </span>
                    </div>
                    <el-input-number
                      v-model="shelf.allocated"
                      :min="0"
                      :max="getShelfMaxAllocation(row, shelf, batchIndex, shelfIndex)"
                      :precision="0"
                      controls-position="right"
                      size="small"
                      placeholder="分配数量"
                      class="shelf-input"
                      @change="(value) => handleShelfAllocationChange(batchIndex, shelfIndex, value)"
                      :disabled="shelf.quantity < 1 || row.quantity < 1 || isViewMode"
                    />
                  </div>
                </div>
                <div v-else class="no-shelf">
                  <span class="no-shelf-text">无货架信息</span>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="批次分配总数" width="120" align="center">
            <template #default="{ row }">
              <span :class="getBatchAllocationClass(row)">{{ getBatchAllocatedTotal(row) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" fixed="right" align="center" v-if="!isViewMode">
            <template #default="{ row, $index }">
              <el-button
                type="danger"
                link
                @click="clearBatchAllocation($index)"
                :disabled="getBatchAllocatedTotal(row) === 0 || row.quantity < 1"
              >
                清空
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="batch-actions" v-if="!isViewMode">
          <el-button @click="autoAllocateBatches" :disabled="batchDialog.remainingQuantity <= 0">
            自动分配
          </el-button>
          <el-button type="primary" @click="confirmBatchAllocation" :disabled="batchDialog.remainingQuantity !== 0">
            确认分配
          </el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 导入Excel对话框 -->
    <el-dialog
      v-model="importDialog.visible"
      title="导入Excel模板"
      width="500px"
      destroy-on-close
    >
      <div class="import-dialog-content">
        <el-upload
          ref="uploadRef"
          class="upload-demo"
          :auto-upload="false"
          :show-file-list="false"
          accept=".xlsx,.xls"
          :on-change="handleFileChange"
        >
          <el-button type="primary">选择Excel文件</el-button>
          <template #tip>
            <div class="el-upload__tip">
              请选择.xlsx或.xls格式的Excel文件
            </div>
          </template>
        </el-upload>
        
        <div v-if="currentFile" class="selected-file">
          <el-icon><Document /></el-icon>
          <span>{{ currentFile.name }}</span>
          <el-button type="danger" link @click="clearSelectedFile">
            <el-icon><Close /></el-icon>
          </el-button>
        </div>
        
        <div class="import-actions" v-if="currentFile">
          <el-button 
            type="primary" 
            @click="handleImportSubmit"
            :loading="importLoading"
          >
            开始导入
          </el-button>
        </div>
        
        <div class="import-tips" v-if="importResult">
          <h4>导入结果：</h4>
          <div v-if="importResult.success" class="success-result">
            <p>导入成功！</p>
            <p>成功导入 {{ importResult.data?.length || 0 }} 条产品记录</p>
          </div>
          <div v-else class="error-result">
            <p>导入失败：{{ importResult.message }}</p>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 产品历史价格对话框 -->
    <el-dialog
      v-model="historyDialog.visible"
      :title="`价格历史 - ${historyDialog.productName}`"
      width="600px"
    >
      <div class="history-dialog-content" v-loading="loadingHistory">
        <div class="history-summary">
          <div class="summary-item">
            <span class="label">最近价格：</span>
            <span class="value">¥ {{ historyDialog.lastPrice?.toFixed(2) || '-' }}</span>
          </div>
          <div class="summary-item">
            <span class="label">平均价格：</span>
            <span class="value">¥ {{ historyDialog.avgPrice?.toFixed(2) || '-' }}</span>
          </div>
          <div class="summary-item">
            <span class="label">最高价格：</span>
            <span class="value">¥ {{ historyDialog.maxPrice?.toFixed(2) || '-' }}</span>
          </div>
          <div class="summary-item">
            <span class="label">最低价格：</span>
            <span class="value">¥ {{ historyDialog.minPrice?.toFixed(2) || '-' }}</span>
          </div>
        </div>
        
        <el-table :data="historyDialog.priceList" border class="history-table">
          <el-table-column label="日期" prop="date" width="120" />
          <el-table-column label="客户" prop="customerName" width="150" />
          <el-table-column label="单价" prop="price" width="100" align="right">
            <template #default="{ row }">
              ¥ {{ row.price?.toFixed(2) }}
            </template>
          </el-table-column>
          <el-table-column label="数量" prop="quantity" width="80" align="center" />
          <el-table-column label="总金额" width="120" align="right">
            <template #default="{ row }">
              ¥ {{ ((row.price || 0) * (row.quantity || 0)).toFixed(2) }}
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch, nextTick } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { 
  Plus, Delete, Upload, Download, Document, Close, Search, 
  Operation, MagicStick, Warning, InfoFilled, Refresh, 
  CloseBold, SetUp, ArrowUp 
} from '@element-plus/icons-vue';
import { post, get } from '@/net';
import axios from 'axios';
import { accessHeader } from '@/net'; 

const router = useRouter();
const route = useRoute();
const formRef = ref();
const uploadRef = ref();
const loading = ref(false);
const downloadLoading = ref(false);

// 智能推荐相关
const showRecommendations = ref(false);
const loadingRecommendations = ref(false);
const recommendations = ref([]);

// 产品搜索和筛选
const productSearch = ref('');
const filteredProducts = ref([]);
const selectedProducts = ref([]);

// 批次分配策略
const showAllocationStrategy = ref(false);
const allocationStrategy = ref('FIFO');
const applyingStrategy = ref(false);
const loadingBatches = ref(false);

// 历史价格对话框
const historyDialog = reactive({
  visible: false,
  productName: '',
  productId: null,
  lastPrice: null,
  avgPrice: null,
  maxPrice: null,
  minPrice: null,
  priceList: []
});
const loadingHistory = ref(false);

const importDialog = reactive({
  visible: false
});

const currentFile = ref(null);
const importResult = ref(null);
const importLoading = ref(false);

// 判断是否是编辑模式
const isEditMode = computed(() => {
  return !!route.params.id;
});

// 判断是否是查看模式（已审核、已完成等状态）
const isViewMode = computed(() => {
  return formData.status > 1; // 状态大于1表示已审核、已完成等不可编辑状态
});

// 表单数据
const formData = reactive({
  id: null,
  orderNo: '',
  orderType: 1, // 销售出库
  warehouseId: null,
  customerId: null,
  expectedDate: '',
  relatedOrderNo: '',
  remark: '',
  status: 0,
  items: [],
  attachments: []
});

// 批次分配对话框数据
const batchDialog = reactive({
  visible: false,
  productIndex: -1,
  productName: '',
  totalQuantity: 0,
  batches: [],
  allocatedQuantity: 0,
  remainingQuantity: 0,
  currentRow: null
});

// 选项数据
const warehouseList = ref([]);
const customerList = ref([]);
const inventoryList = ref([]);
const fileList = ref([]);
const allInventoryProducts = ref([]);

// 产品库存映射表
const productStockMap = ref({});

// 计算属性
const productTypeCount = computed(() => {
  return allInventoryProducts.value.filter(p => p.quantity > 0).length;
});

const totalQuantity = computed(() => {
  return allInventoryProducts.value.reduce((sum, item) => sum + (item.quantity || 0), 0);
});

const totalAmount = computed(() => {
  return allInventoryProducts.value.reduce((sum, item) => {
    const price = item.price || 0;
    const quantity = item.quantity || 0;
    return sum + (price * quantity);
  }, 0);
});

const totalAmountUsd = computed(() => {
  return allInventoryProducts.value.reduce((sum, item) => {
    const priceUsd = item.priceUnitUsd || 0;
    const quantity = item.quantity || 0;
    return sum + (priceUsd * quantity);
  }, 0);
});

const hasInsufficientStock = computed(() => {
  return allInventoryProducts.value.some(item => {
    if (item.quantity <= 0) return false;
    const allocatedQuantity = item.batchAllocations 
      ? item.batchAllocations.reduce((sum, alloc) => sum + (alloc.quantity || 0), 0)
      : 0;
    return (item.quantity || 0) !== allocatedQuantity;
  });
});

const batchStatusText = computed(() => {
  return hasInsufficientStock.value ? '批次分配不足' : '分配完成';
});

const batchStatusClass = computed(() => {
  return hasInsufficientStock.value ? 'status-warning' : 'status-success';
});

const selectedProductCount = computed(() => {
  return selectedProducts.value.length;
});

const hasProductsWithQuantity = computed(() => {
  return allInventoryProducts.value.some(p => p.quantity > 0);
});

const hasBatchAllocations = computed(() => {
  return allInventoryProducts.value.some(item => 
    item.batchAllocations && item.batchAllocations.length > 0
  );
});

const hasValidRecommendations = computed(() => {
  return recommendations.value.some(r => 
    r.recommendQuantity > 0 && 
    r.availableQuantity > 0 && 
    !r.added
  );
});

const recommendedProductCount = computed(() => {
  return allInventoryProducts.value.filter(p => p.isRecommend).length;
});

// 表单验证规则
const formRules = {
  orderType: [
    { required: true, message: '请选择出库类型', trigger: 'change' }
  ],
  warehouseId: [
    { required: true, message: '请选择出库仓库', trigger: 'change' }
  ],
  customerId: [
    { required: true, message: '请选择客户', trigger: 'change' }
  ],
  expectedDate: [
    { required: true, message: '请选择预计出库日期', trigger: 'change' }
  ]
};

// 获取出库数量的最大值
const getQuantityMax = (row) => {
  // 如果是查看模式，不限制最大值，确保能显示历史数据
  if (isViewMode.value) {
    return Number.MAX_SAFE_INTEGER;
  }
  
  // 编辑模式下，限制不能超过当前库存
  return row.availableQuantity || 0;
};

// 加载出库单详情
const loadOutboundDetail = async (id) => {
  loading.value = true;
  try {
    const res = await get(`/api/auth/outbound/detail?orderId=${id}`);
    console.log('出库单详情响应:', res);
    
    if (res) {
      const detailData = res.data || res;
      
      // 设置基本数据
      Object.assign(formData, {
        id: detailData.id,
        orderNo: detailData.orderNo,
        orderType: detailData.orderType,
        warehouseId: detailData.warehouseId,
        customerId: detailData.customerId,
        expectedDate: detailData.expectedDate,
        relatedOrderNo: detailData.relatedOrderNo || '',
        remark: detailData.remark || '',
        status: detailData.status
      });

      // 如果仓库有值，先加载对应的数据
      if (detailData.warehouseId) {
        await loadInventoryData(detailData.warehouseId);
      }

      // 设置产品明细数据
      if (detailData.items && detailData.items.length > 0) {
        await loadInventoryData(detailData.warehouseId);
        
        // 等待库存数据加载完成后，再设置产品数据
        setTimeout(() => {
          // 首先将所有产品的数量重置为0
          allInventoryProducts.value.forEach(product => {
            product.quantity = 0;
            product.price = 0;
            product.remark = '';
            product.batchAllocations = [];
          });
          
          // 然后设置编辑模式下的数据
          detailData.items.forEach(detailItem => {
            const product = allInventoryProducts.value.find(p => p.productId === detailItem.productId);
            console.log('找到产品:', product);
            console.log('明细数据:', detailItem);
            if (product) {
              product.quantity = detailItem.quantity || 0;
              product.price = detailItem.priceUnit || 0;
              product.remark = detailItem.remark || '';
              product.priceUnitUsd = detailItem.priceUnitUsd || 0;
              product.batchAllocations = detailItem.batchAllocations || [];
            }
          });
        }, 500);
      } else {
        formData.items = [];
      }

      // 设置附件数据
      if (detailData.attachments && detailData.attachments.length > 0) {
        fileList.value = detailData.attachments.map(att => ({
          name: att.fileName,
          url: att.filePath,
          status: 'success'
        }));
        formData.attachments = detailData.attachments;
      }
      
      ElMessage.success('数据加载成功');
    }
  } catch (error) {
    console.error('加载出库单详情失败:', error);
    ElMessage.error('加载数据失败');
    router.back();
  } finally {
    loading.value = false;
  }
};

// 更新库存产品数据
const updateInventoryProducts = () => {
  if (!formData.warehouseId || !inventoryList.value.length) {
    allInventoryProducts.value = [];
    filteredProducts.value = [];
    return;
  }
  
  allInventoryProducts.value = inventoryList.value.map(item => {
    // 查找是否已经存在这个产品的数据（保留已输入的数量和价格）
    const existingProduct = allInventoryProducts.value.find(p => p.productId === item.productId);
    
    // 默认使用接口返回的price，如果已有数据则保留用户输入的价格
    const displayPrice = existingProduct ? existingProduct.price : (item.price || 0);
    
    return {
      ...item,
      quantity: existingProduct ? existingProduct.quantity : 0,
      price: displayPrice,
      priceUnitUsd: existingProduct ? existingProduct.priceUnitUsd : (item.priceUnitUsd || 0),
      remark: existingProduct ? existingProduct.remark : '',
      batchAllocations: existingProduct ? existingProduct.batchAllocations : [],
      availableBatches: existingProduct ? existingProduct.availableBatches : [],
      // 保存接口原始价格，用于导入时的逻辑判断
      originalPrice: item.price || 0,
      originalPriceUnitUsd: item.priceUnitUsd || 0,
      // 是否为推荐产品
      isRecommend: existingProduct ? existingProduct.isRecommend : false,
      // 历史价格
      historyPrice: existingProduct ? existingProduct.historyPrice : null
    };
  });
  
  // 初始化筛选结果
  filterProducts();
};

// 方法
const generateOrderNo = () => {
  const date = new Date();
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const random = String(Math.random()).substr(2, 6);
  formData.orderNo = `CK${year}${month}${day}${random}`;
};

const handleWarehouseChange = async (warehouseId) => {
  if (warehouseId) {
    await loadInventoryData(warehouseId);
    
    // 确保所有产品的数量为0
    if (allInventoryProducts.value.length > 0) {
      allInventoryProducts.value.forEach(product => {
        product.quantity = 0;
        product.price = 0;
        product.priceUnitUsd = 0;
        product.remark = '';
        product.batchAllocations = [];
        product.isRecommend = false;
      });
    }
    
    // 清空推荐
    showRecommendations.value = false;
    recommendations.value = [];
  }
};

const handleCustomerChange = (customerId) => {
  // 客户变化时清空推荐
  showRecommendations.value = false;
  recommendations.value = [];
  
  // 如果已有仓库，可以预加载推荐
  if (customerId && formData.warehouseId) {
    // 延迟加载推荐，避免频繁请求
    setTimeout(() => {
      if (formData.customerId === customerId) {
        loadRecommendations();
      }
    }, 500);
  }
};

// 销售出库：处理所有商品表格中的数量变化
const handleQuantityChangeForAll = (row) => {
  if (row.quantity > 0) {
    // 如果之前有批次分配，但数量减少了，需要清空批次分配
    if (row.batchAllocations && row.batchAllocations.length > 0) {
      const totalAllocated = row.batchAllocations.reduce((sum, alloc) => sum + alloc.quantity, 0);
      if (row.quantity < totalAllocated) {
        ElMessage.warning('出库数量小于已分配批次数量，请重新分配批次');
        row.batchAllocations = [];
      }
    }
    
    // 如果接口返回了价格，使用接口价格（仅在价格为空时自动填充）
    if (row.originalPrice && row.originalPrice > 0 && (!row.price || row.price === 0)) {
      row.price = row.originalPrice;
    }
    // 如果接口返回了USD价格，使用接口USD价格（仅在USD价格为空时自动填充）
    if (row.originalPriceUnitUsd && row.originalPriceUnitUsd > 0 && (!row.priceUnitUsd || row.priceUnitUsd === 0)) {
      row.priceUnitUsd = row.originalPriceUnitUsd;
    }
    
    // 加载历史价格
    if (!row.historyPrice && formData.customerId) {
      loadProductHistoryPrice(row);
    }
  } else {
    // 如果数量设为0，清空相关数据
    row.quantity = 0;
    row.price = 0;
    row.priceUnitUsd = 0;
    row.remark = '';
    row.batchAllocations = [];
  }
  
  // 更新筛选列表
  filterProducts();
};

// 智能推荐相关方法
const loadRecommendations = async () => {
  if (!formData.customerId || !formData.warehouseId) {
    ElMessage.warning('请先选择客户和仓库');
    return;
  }
  
  loadingRecommendations.value = true;
  try {
    const res = await get(
      `/api/auth/recommend/sales?customerId=${formData.customerId}&warehouseId=${formData.warehouseId}`
    );
    
    if (res && res.data && res.data.length > 0) {
      // 过滤掉已经在出库单中的产品
      const existingProductIds = allInventoryProducts.value
        .filter(p => p.quantity > 0)
        .map(p => p.productId);
      
      recommendations.value = res.data
        .filter(item => !existingProductIds.includes(item.productId))
        .map(item => ({
          ...item,
          recommendQuantity: item.recommendQuantity || 1,
          price: item.price || 0,
          priceUnitUsd: item.priceUnitUsd || 0,
          originalPrice: item.price || 0,
          triggerProducts: item.triggerProducts || [],
          reasonText: getRecommendReasonText(item),
          reasonType: getRecommendReasonType(item),
          added: false,
          availableQuantity: productStockMap.value[item.productId] || 0
        }));
      
      if (recommendations.value.length > 0) {
        showRecommendations.value = true;
        ElMessage.success(`发现 ${recommendations.value.length} 条推荐`);
        
        // 滚动到推荐区域
        nextTick(() => {
          const element = document.querySelector('.recommendation-section');
          if (element) {
            element.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
          }
        });
      } else {
        ElMessage.info('所有推荐产品已存在于出库单中');
        showRecommendations.value = false;
      }
    } else {
      ElMessage.info('暂无推荐产品');
      showRecommendations.value = false;
    }
  } catch (error) {
    console.error('加载推荐失败:', error);
    ElMessage.error('获取推荐失败');
    showRecommendations.value = false;
  } finally {
    loadingRecommendations.value = false;
  }
};

const getRecommendReasonText = (item) => {
  if (item.ruleType === 1) return '配置规则';
  if (item.isRequired === 1) return '强制搭配';
  if (item.confidence > 0.9) return '高频搭配';
  if (item.confidence > 0.7) return '通常搭配';
  return '建议搭配';
};

const getRecommendReasonType = (item) => {
  if (item.isRequired === 1) return 'danger'; // 强制推荐
  if (item.confidence > 0.9) return 'success'; // 高置信度
  if (item.confidence > 0.7) return 'warning'; // 中置信度
  return 'info'; // 普通建议
};

const handleRecommendQuantityChange = (row) => {
  if (row.recommendQuantity > row.availableQuantity) {
    row.recommendQuantity = row.availableQuantity;
    ElMessage.warning(`推荐数量不能超过库存数量 ${row.availableQuantity}`);
  }
};

const addRecommendToOrder = (recommend) => {
  // 查找是否已在产品列表中
  let product = allInventoryProducts.value.find(p => p.productId === recommend.productId);
  
  if (product) {
    // 如果产品已存在，更新数量
    if (product.quantity === 0) {
      product.quantity = recommend.recommendQuantity;
      product.price = recommend.price;
      product.priceUnitUsd = recommend.priceUnitUsd;
      product.isRecommend = true;
      ElMessage.success(`已添加 ${recommend.productName} 到出库单`);
    } else {
      // 如果已有数量，询问是否覆盖
      ElMessageBox.confirm(
        `产品 ${recommend.productName} 已有数量 ${product.quantity}，是否替换为推荐数量 ${recommend.recommendQuantity}？`,
        '确认替换',
        {
          confirmButtonText: '替换',
          cancelButtonText: '取消',
          type: 'warning'
        }
      ).then(() => {
        product.quantity = recommend.recommendQuantity;
        product.price = recommend.price;
        product.priceUnitUsd = recommend.priceUnitUsd;
        product.isRecommend = true;
        ElMessage.success('已替换数量');
      });
    }
  } else {
    // 如果产品不存在（可能不在当前仓库），提示用户
    ElMessage.warning(`产品 ${recommend.productName} 不在当前仓库库存中`);
  }
  
  // 标记为已添加
  recommend.added = true;
  
  // 更新筛选列表
  filterProducts();
};

const addAllRecommendations = () => {
  const validRecommendations = recommendations.value.filter(r => 
    r.recommendQuantity > 0 && 
    r.availableQuantity > 0 && 
    !r.added
  );
  
  if (validRecommendations.length === 0) {
    ElMessage.warning('没有可添加的推荐产品');
    return;
  }
  
  let addedCount = 0;
  validRecommendations.forEach(recommend => {
    const product = allInventoryProducts.value.find(p => p.productId === recommend.productId);
    if (product && product.availableQuantity >= recommend.recommendQuantity) {
      // 如果产品已有数量，累加
      product.quantity = (product.quantity || 0) + recommend.recommendQuantity;
      product.price = recommend.price;
      product.priceUnitUsd = recommend.priceUnitUsd;
      product.isRecommend = true;
      recommend.added = true;
      addedCount++;
    }
  });
  
  if (addedCount > 0) {
    ElMessage.success(`成功添加 ${addedCount} 个推荐产品`);
    filterProducts();
  } else {
    ElMessage.warning('未能添加任何推荐产品');
  }
};

const ignoreRecommendation = (recommend) => {
  const index = recommendations.value.indexOf(recommend);
  if (index > -1) {
    recommendations.value.splice(index, 1);
  }
  
  if (recommendations.value.length === 0) {
    showRecommendations.value = false;
  }
};

const closeRecommendations = () => {
  showRecommendations.value = false;
};

// 产品搜索和筛选
const filterProducts = () => {
  if (!productSearch.value.trim()) {
    filteredProducts.value = [...allInventoryProducts.value];
    return;
  }
  
  const searchText = productSearch.value.toLowerCase().trim();
  filteredProducts.value = allInventoryProducts.value.filter(product => {
    return (
      (product.productName && product.productName.toLowerCase().includes(searchText)) ||
      (product.sku && product.sku.toLowerCase().includes(searchText)) ||
      (product.spec && product.spec.toLowerCase().includes(searchText)) ||
      (product.color && product.color.toLowerCase().includes(searchText))
    );
  });
};

// 批量操作
const handleBatchCommand = async (command) => {
  switch (command) {
    case 'clearAll':
      if (selectedProducts.value.length > 0) {
        // 只清空选中的产品
        selectedProducts.value.forEach(row => {
          row.quantity = 0;
          row.price = 0;
          row.priceUnitUsd = 0;
          row.remark = '';
          row.batchAllocations = [];
          row.isRecommend = false;
        });
        ElMessage.success(`已清空 ${selectedProducts.value.length} 个产品的数量`);
      } else {
        // 清空所有有数量的产品
        const count = allInventoryProducts.value.filter(p => p.quantity > 0).length;
        allInventoryProducts.value.forEach(product => {
          product.quantity = 0;
          product.price = 0;
          product.priceUnitUsd = 0;
          product.remark = '';
          product.batchAllocations = [];
          product.isRecommend = false;
        });
        if (count > 0) {
          ElMessage.success(`已清空 ${count} 个产品的数量`);
        }
      }
      filterProducts();
      break;
      
    case 'resetPrices':
      if (selectedProducts.value.length > 0) {
        selectedProducts.value.forEach(row => {
          if (row.quantity > 0) {
            row.price = row.originalPrice || 0;
            row.priceUnitUsd = row.originalPriceUnitUsd || 0;
          }
        });
        ElMessage.success(`已重置 ${selectedProducts.value.length} 个产品的价格`);
      } else {
        allInventoryProducts.value.forEach(product => {
          if (product.quantity > 0) {
            product.price = product.originalPrice || 0;
            product.priceUnitUsd = product.originalPriceUnitUsd || 0;
          }
        });
        ElMessage.success('已重置所有产品的价格');
      }
      break;
      
    case 'clearBatches':
      if (selectedProducts.value.length > 0) {
        selectedProducts.value.forEach(row => {
          row.batchAllocations = [];
        });
        ElMessage.success(`已清空 ${selectedProducts.value.length} 个产品的批次分配`);
      } else {
        allInventoryProducts.value.forEach(product => {
          product.batchAllocations = [];
        });
        ElMessage.success('已清空所有产品的批次分配');
      }
      break;
      
    case 'exportData':
      exportCurrentData();
      break;
  }
};

const handleSelectionChange = (selection) => {
  selectedProducts.value = selection;
};

const clearProductRow = (row) => {
  row.quantity = 0;
  row.price = 0;
  row.priceUnitUsd = 0;
  row.remark = '';
  row.batchAllocations = [];
  row.isRecommend = false;
  ElMessage.success('已清空该产品数据');
  filterProducts();
};

const setMaxQuantity = (row) => {
  row.quantity = row.availableQuantity || 0;
  if (row.quantity > 0) {
    row.price = row.originalPrice || 0;
    row.priceUnitUsd = row.originalPriceUnitUsd || 0;
  }
  ElMessage.success('已设置为最大库存数量');
};

const getStockClass = (currentStock, quantity) => {
  if (!currentStock || currentStock <= 0) return 'stock-none';
  if (quantity > currentStock) return 'stock-insufficient';
  if (currentStock < 10) return 'stock-low';
  return 'stock-sufficient';
};

const getStockColor = (currentStock, quantity) => {
  if (!currentStock || currentStock <= 0) return '#909399';
  if (quantity > currentStock) return '#f56c6c';
  const percentage = (quantity / currentStock) * 100;
  if (percentage > 80) return '#f56c6c';
  if (percentage > 50) return '#e6a23c';
  return '#67c23a';
};

// 产品历史价格
const showProductHistory = async (row) => {
  if (!row.productId) return;
  
  historyDialog.visible = true;
  historyDialog.productName = row.productName;
  historyDialog.productId = row.productId;
  
  if (!row.historyPrice) {
    await loadProductHistoryPrice(row, true);
  }
  
  if (row.historyPrice) {
    historyDialog.lastPrice = row.historyPrice.lastPrice;
    historyDialog.avgPrice = row.historyPrice.avgPrice;
    historyDialog.maxPrice = row.historyPrice.maxPrice;
    historyDialog.minPrice = row.historyPrice.minPrice;
    historyDialog.priceList = row.historyPrice.priceList || [];
  }
};

const loadProductHistoryPrice = async (row, force = false) => {
  if (!row.productId || (!force && row.historyPrice)) return;
  
  try {
    const params = {
      productId: row.productId,
      customerId: formData.customerId || undefined,
      limit: 10
    };
    
    const queryString = Object.entries(params)
      .filter(([_, value]) => value !== undefined)
      .map(([key, value]) => `${key}=${value}`)
      .join('&');
    
    const res = await get(`/api/auth/price/history?${queryString}`);
    
    if (res && res.data) {
      row.historyPrice = {
        lastPrice: res.data.lastPrice,
        avgPrice: res.data.avgPrice,
        maxPrice: res.data.maxPrice,
        minPrice: res.data.minPrice,
        priceList: res.data.priceList || []
      };
    }
  } catch (error) {
    console.error('加载历史价格失败:', error);
  }
};

// 批次分配相关方法
const getShelfMaxAllocation = (batch, shelf, batchIndex, shelfIndex) => {
  if (batch.quantity < 1) {
    return 0;
  }
  
  if (shelf.quantity < 1) {
    return 0;
  }
  
  const currentShelfAllocated = shelf.allocated || 0;
  const shelfMax = shelf.quantity;
  
  let otherAllocatedTotal = 0;
  batchDialog.batches.forEach((b, bIndex) => {
    if (b.shelfList && b.shelfList.length > 0) {
      b.shelfList.forEach((s, sIndex) => {
        if (!(bIndex === batchIndex && sIndex === shelfIndex)) {
          otherAllocatedTotal += s.allocated || 0;
        }
      });
    }
  });
  
  const remainingForThisShelf = Math.max(0, batchDialog.totalQuantity - otherAllocatedTotal);
  const maxAllocation = Math.min(shelfMax, remainingForThisShelf);
  
  return Math.max(0, maxAllocation);
};

const handleShelfAllocationChange = (batchIndex, shelfIndex, newValue) => {
  const batch = batchDialog.batches[batchIndex];
  const shelf = batch.shelfList[shelfIndex];
  
  if (batch.quantity < 1) {
    ElMessage.warning('该批次总可用数量不足，无法分配');
    shelf.allocated = 0;
    updateBatchDialogCalculations();
    return;
  }
  
  if (shelf.quantity < 1) {
    ElMessage.warning('该货架可用数量不足，无法分配');
    shelf.allocated = 0;
    updateBatchDialogCalculations();
    return;
  }
  
  if (newValue !== null && newValue !== undefined) {
    newValue = Math.max(0, Math.floor(newValue));
  } else {
    newValue = 0;
  }
  
  const maxAllocation = getShelfMaxAllocation(batch, shelf, batchIndex, shelfIndex);
  
  if (newValue > maxAllocation) {
    newValue = maxAllocation;
    if (maxAllocation > 0) {
      ElMessage.warning(`分配数量不能超过最大可分配数量 ${maxAllocation}`);
    } else {
      ElMessage.warning('当前无可分配数量');
      newValue = 0;
    }
  }
  
  const currentAllocated = batchDialog.allocatedQuantity;
  const otherAllocated = currentAllocated - (shelf.allocated || 0);
  const totalAllocated = otherAllocated + newValue;
  
  if (totalAllocated > batchDialog.totalQuantity) {
    const maxAllowed = Math.max(0, batchDialog.totalQuantity - otherAllocated);
    newValue = Math.max(0, maxAllowed);
    ElMessage.warning(`分配总数不能超过出库数量 ${batchDialog.totalQuantity}，当前最多可分配 ${maxAllowed}`);
  }
  
  const remainingAfterAllocation = batchDialog.totalQuantity - (otherAllocated + newValue);
  if (remainingAfterAllocation < 0) {
    newValue = Math.max(0, batchDialog.totalQuantity - otherAllocated);
    ElMessage.warning('分配数量过多，已自动调整为最大可分配数量');
  }
  
  shelf.allocated = newValue;
  updateBatchDialogCalculations();
};

const getBatchAllocatedTotal = (batch) => {
  if (!batch.shelfList || batch.shelfList.length === 0) return 0;
  return batch.shelfList.reduce((sum, shelf) => {
    return shelf.quantity >= 1 ? sum + (shelf.allocated || 0) : sum;
  }, 0);
};

const getBatchAllocationClass = (batch) => {
  const allocated = getBatchAllocatedTotal(batch);
  const batchTotal = batch.quantity;
  
  if (allocated === 0) return 'allocation-zero';
  if (allocated > batchTotal) return 'allocation-exceed';
  return 'allocation-normal';
};

const clearBatchAllocation = (batchIndex) => {
  const batch = batchDialog.batches[batchIndex];
  if (batch.shelfList) {
    batch.shelfList.forEach(shelf => {
      if (shelf.quantity >= 1) {
        shelf.allocated = 0;
      }
    });
  }
  updateBatchDialogCalculations();
  ElMessage.success('已清空该批次分配');
};

const updateBatchDialogCalculations = () => {
  batchDialog.allocatedQuantity = batchDialog.batches.reduce((sum, batch) => {
    if (batch.quantity >= 1 && batch.shelfList) {
      return sum + batch.shelfList.reduce((shelfSum, shelf) => {
        return shelf.quantity >= 1 ? shelfSum + (shelf.allocated || 0) : shelfSum;
      }, 0);
    }
    return sum;
  }, 0);
  
  batchDialog.remainingQuantity = Math.max(0, batchDialog.totalQuantity - batchDialog.allocatedQuantity);
  
  if (batchDialog.allocatedQuantity > batchDialog.totalQuantity) {
    batchDialog.allocatedQuantity = batchDialog.totalQuantity;
    batchDialog.remainingQuantity = 0;
  }
};

const getExpiryClass = (expiryDate) => {
  if (!expiryDate) return '';
  
  const expiry = new Date(expiryDate);
  const now = new Date();
  const daysDiff = Math.ceil((expiry - now) / (1000 * 60 * 60 * 24));
  
  if (daysDiff < 0) return 'expiry-expired';
  if (daysDiff < 30) return 'expiry-soon';
  if (daysDiff < 90) return 'expiry-near';
  return '';
};

const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toISOString().split('T')[0];
};

const applyAllocationStrategy = () => {
  applyingStrategy.value = true;
  
  // 清空现有分配
  batchDialog.batches.forEach(batch => {
    if (batch.shelfList) {
      batch.shelfList.forEach(shelf => {
        shelf.allocated = 0;
      });
    }
  });
  
  let remaining = batchDialog.totalQuantity;
  
  // 根据策略排序批次
  let sortedBatches = [...batchDialog.batches];
  
  switch (allocationStrategy.value) {
    case 'FIFO':
      // 先进先出：按生产日期排序
      sortedBatches.sort((a, b) => {
        if (!a.productionDate && !b.productionDate) return 0;
        if (!a.productionDate) return 1;
        if (!b.productionDate) return -1;
        return new Date(a.productionDate) - new Date(b.productionDate);
      });
      break;
      
    case 'NEAR_EXPIRE':
      // 近效期优先：按有效期排序
      sortedBatches.sort((a, b) => {
        if (!a.expiryDate && !b.expiryDate) return 0;
        if (!a.expiryDate) return 1;
        if (!b.expiryDate) return -1;
        return new Date(a.expiryDate) - new Date(b.expiryDate);
      });
      break;
      
    case 'MIN_SHELF':
      // 最少货架优先：按货架数量排序
      sortedBatches.sort((a, b) => {
        const aShelves = a.shelfList ? a.shelfList.length : 0;
        const bShelves = b.shelfList ? b.shelfList.length : 0;
        return aShelves - bShelves;
      });
      break;
  }
  
  // 应用分配
  for (const batch of sortedBatches) {
    if (remaining <= 0) break;
    
    if (batch.quantity < 1 || !batch.shelfList) continue;
    
    for (const shelf of batch.shelfList) {
      if (remaining <= 0) break;
      
      if (shelf.quantity < 1) continue;
      
      const allocate = Math.min(shelf.quantity, remaining);
      if (allocate > 0) {
        shelf.allocated = allocate;
        remaining -= allocate;
      }
    }
  }
  
  updateBatchDialogCalculations();
  
  setTimeout(() => {
    applyingStrategy.value = false;
    if (remaining === 0) {
      ElMessage.success('分配策略应用完成');
    } else {
      ElMessage.warning(`分配策略应用完成，仍有 ${remaining} 个无法分配`);
    }
  }, 300);
};

const autoAllocateBatches = () => {
  let remaining = batchDialog.remainingQuantity;
  
  if (remaining <= 0) {
    ElMessage.warning('已全部分配完成');
    return;
  }
  
  // 清空现有分配
  batchDialog.batches.forEach(batch => {
    if (batch.shelfList) {
      batch.shelfList.forEach(shelf => {
        shelf.allocated = 0;
      });
    }
  });
  
  updateBatchDialogCalculations();
  remaining = batchDialog.remainingQuantity;
  
  // 按有效期排序分配（默认策略）
  const sortedBatches = [...batchDialog.batches].sort((a, b) => {
    if (!a.expiryDate && !b.expiryDate) return 0;
    if (!a.expiryDate) return 1;
    if (!b.expiryDate) return -1;
    return new Date(a.expiryDate) - new Date(b.expiryDate);
  });
  
  for (const batch of sortedBatches) {
    if (remaining <= 0) break;
    
    if (batch.quantity < 1) continue;
    
    if (batch.shelfList && batch.shelfList.length > 0) {
      for (const shelf of batch.shelfList) {
        if (remaining <= 0) break;
        
        if (shelf.quantity < 1) continue;
        
        const maxAllocation = Math.min(shelf.quantity, remaining);
        const allocate = maxAllocation;
        
        if (allocate > 0) {
          shelf.allocated = allocate;
          remaining -= allocate;
        }
      }
    }
  }
  
  updateBatchDialogCalculations();
  
  if (remaining > 0) {
    ElMessage.warning(`库存不足，仍有 ${remaining} 个无法分配`);
  } else {
    ElMessage.success('自动分配完成');
  }
};

// 批次分配相关方法
const openBatchDialogForProduct = async (row) => {
  if (!row.productId) {
    ElMessage.warning('产品信息不完整');
    return;
  }

  batchDialog.currentRow = row;
  batchDialog.productName = row.productName;
  batchDialog.totalQuantity = row.quantity;
  
  loadingBatches.value = true;
  await loadBatchInfoForProduct(row.productId, formData.warehouseId, row);
  loadingBatches.value = false;
  
  batchDialog.batches = row.availableBatches.map(batch => ({
    ...batch,
    shelfList: batch.shelfList ? batch.shelfList.map(shelf => ({
      ...shelf,
      allocated: 0,
      maxAllocatable: shelf.quantity
    })) : []
  }));

  if (row.batchAllocations && row.batchAllocations.length > 0) {
    row.batchAllocations.forEach(allocation => {
      const batch = batchDialog.batches.find(b => b.batchNo === allocation.batchNo);
      if (batch && batch.shelfList) {
        const shelf = batch.shelfList.find(s => s.shelfId === allocation.shelfId);
        if (shelf) {
          shelf.allocated = allocation.quantity;
        }
      }
    });
  }

  updateBatchDialogCalculations();
  batchDialog.visible = true;
};

const loadBatchInfoForProduct = async (productId, warehouseId, row) => {
  try {
    const res = await get(`/api/auth/inventory/batches?productId=${productId}&warehouseId=${warehouseId}`);
    console.log('批次信息响应:', res);
    
    if (res && Array.isArray(res)) {
      row.availableBatches = res;
    } else {
      row.availableBatches = [];
    }
  } catch (error) {
    console.error('加载批次信息失败:', error);
    row.availableBatches = [];
  }
};

const handleBatchDialogClosed = () => {
  showAllocationStrategy.value = false;
  allocationStrategy.value = 'FIFO';
};

const confirmBatchAllocation = () => {
  if (batchDialog.remainingQuantity !== 0) {
    ElMessage.warning(`分配数量 (${batchDialog.allocatedQuantity}) 与出库数量 (${batchDialog.totalQuantity}) 不一致，请完成分配`);
    return;
  }

  let hasInvalidAllocation = false;
  batchDialog.batches.forEach(batch => {
    if (batch.shelfList) {
      batch.shelfList.forEach(shelf => {
        if (shelf.allocated > 0 && shelf.allocated > shelf.quantity) {
          hasInvalidAllocation = true;
          ElMessage.warning(`批次 ${batch.batchNo} 货架 ${shelf.shelfName} 的分配数量超过可用数量`);
        }
      });
    }
  });

  if (hasInvalidAllocation) {
    return;
  }

  const row = batchDialog.currentRow;
  row.batchAllocations = [];
  
  batchDialog.batches.forEach(batch => {
    if (batch.shelfList) {
      batch.shelfList.forEach(shelf => {
        if (shelf.allocated > 0) {
          row.batchAllocations.push({
            batchNo: batch.batchNo,
            shelfId: shelf.shelfId,
            shelfName: shelf.shelfName,
            quantity: shelf.allocated,
            price: row.price || 0
          });
        }
      });
    }
  });

  batchDialog.visible = false;
  ElMessage.success('批次分配完成');
};

// 导出当前数据
const exportCurrentData = () => {
  const exportData = {
    orderInfo: {
      orderNo: formData.orderNo,
      warehouseId: formData.warehouseId,
      customerId: formData.customerId,
      expectedDate: formData.expectedDate
    },
    products: allInventoryProducts.value
      .filter(p => p.quantity > 0)
      .map(p => ({
        sku: p.sku,
        productName: p.productName,
        spec: p.spec,
        quantity: p.quantity,
        price: p.price,
        priceUnitUsd: p.priceUnitUsd,
        remark: p.remark
      })),
    summary: {
      productCount: productTypeCount.value,
      totalQuantity: totalQuantity.value,
      totalAmount: totalAmount.value,
      totalAmountUsd: totalAmountUsd.value
    }
  };
  
  const dataStr = JSON.stringify(exportData, null, 2);
  const dataUri = 'data:application/json;charset=utf-8,'+ encodeURIComponent(dataStr);
  
  const exportFileDefaultName = `出库单_${formData.orderNo}_${new Date().toISOString().split('T')[0]}.json`;
  
  const linkElement = document.createElement('a');
  linkElement.setAttribute('href', dataUri);
  linkElement.setAttribute('download', exportFileDefaultName);
  linkElement.click();
  
  ElMessage.success('数据导出成功');
};

// 下载模板
const handleDownloadTemplate = async () => {
  if (!formData.warehouseId) {
    ElMessage.warning('请先选择仓库');
    return;
  }
  
  downloadLoading.value = true;

  try {
    const response = await axios.get('/api/auth/outbound/exportExcel?warehouseId=' + formData.warehouseId, {
      headers: accessHeader(),
      responseType: 'blob',
    });

    const blob = new Blob([response.data], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    });

    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = '销售出库数量导入模版.xlsx';
    document.body.appendChild(a);
    a.click();
    a.remove();
    window.URL.revokeObjectURL(url);

  } catch (error) {
    console.error('下载模板失败', error);
    ElMessage.error('下载模板失败，请稍后重试');
  } finally {
    downloadLoading.value = false;
  }
};

// 应用导入数据到表格
const applyImportedData = (importedData) => {
  let successCount = 0;
  let failCount = 0;
  
  console.log('导入数据:', importedData);
  
  importedData.forEach(importedItem => {
    const existingProductIndex = allInventoryProducts.value.findIndex(p => 
      p.productId === importedItem.productId || p.sku === importedItem.sku
    );
    
    if (existingProductIndex >= 0) {
      console.log('匹配产品:', allInventoryProducts.value[existingProductIndex]);
      
      const product = allInventoryProducts.value[existingProductIndex];
      
      if (importedItem.quantity && importedItem.quantity > 0 && (!importedItem.price || importedItem.price === 0)) {
        console.log('情况1:', importedItem);
        product.quantity = importedItem.quantity || 0;
        product.price = product.originalPrice || 0;
        product.priceUnitUsd = product.originalPriceUnitUsd || 0;
      }
      else if (importedItem.quantity && importedItem.quantity > 0 && importedItem.price && importedItem.price > 0) {
        console.log('情况2:', importedItem);
        product.quantity = importedItem.quantity || 0;
        product.price = importedItem.price || 0;
        product.priceUnitUsd = importedItem.priceUnitUsd || product.originalPriceUnitUsd || 0;
      }
      else if ((!importedItem.quantity || importedItem.quantity === 0) && importedItem.price && importedItem.price > 0) {
        console.log('情况3:', importedItem);
        product.quantity = 0;
        product.price = importedItem.price || 0;
        product.priceUnitUsd = importedItem.priceUnitUsd || product.originalPriceUnitUsd || 0;
      }
      else {
        console.log('情况4:', importedItem);
        product.quantity = importedItem.quantity || 0;
        product.price = importedItem.price || product.originalPrice || 0;
        product.priceUnitUsd = importedItem.priceUnitUsd || product.originalPriceUnitUsd || 0;
      }
      
      product.remark = importedItem.remark || '';
      product.batchAllocations = [];
      product.isRecommend = false;
      
      console.log('更新后产品:', product);
      successCount++;
    } else {
      console.warn('未找到匹配的产品:', importedItem);
      failCount++;
    }
  });
  
  allInventoryProducts.value = [...allInventoryProducts.value];
  filterProducts();
  
  return { successCount, failCount };
};

const handleReset = () => {
  ElMessageBox.confirm(
    `确定要${isEditMode.value ? '重置' : '清空'}表单吗？所有输入的数据将会丢失。`, 
    `${isEditMode.value ? '重置' : '清空'}确认`, 
    {
      type: 'warning'
    }
  ).then(() => {
    if (isEditMode.value) {
      loadOutboundDetail(route.params.id);
    } else {
      formRef.value?.resetFields();
      formData.items = [];
      fileList.value = [];
      generateOrderNo();
      
      allInventoryProducts.value.forEach(product => {
        product.quantity = 0;
        product.price = 0;
        product.priceUnitUsd = 0;
        product.remark = '';
        product.batchAllocations = [];
        product.isRecommend = false;
      });
      
      allInventoryProducts.value = [...allInventoryProducts.value];
      filterProducts();
      
      // 清空推荐
      showRecommendations.value = false;
      recommendations.value = [];
      
      ElMessage.success('表单已重置');
    }
  });
};

const handleSaveDraft = async () => {
  if (!await validateForm()) return;
  
  loading.value = true;
  try {
    const submitData = prepareSubmitData();
    submitData.status = 0;
    
    const url = isEditMode.value ? '/api/auth/outbound/updateProductionSaleOutBound' : '/api/auth/outbound/createProductionSaleOutBound';
    const res = await post(url, submitData);
    if (res) {
      ElMessage.success(isEditMode.value ? '更新草稿成功' : '保存草稿成功');
      router.replace({
        path: '/',
        query: { mode: 'outbound' } // 返回到出库管理页面
      });
    }
  } catch (error) {
    ElMessage.error(isEditMode.value ? '更新草稿失败' : '保存草稿失败');
  } finally {
    loading.value = false;
  }
};

const handleSubmit = async () => {
  if (!await validateForm()) return;
  
  const hasProducts = allInventoryProducts.value.some(p => p.quantity > 0);
  if (!hasProducts) {
    ElMessage.warning('请至少设置一个产品的出库数量');
    return;
  }
  
  const hasUnallocatedItems = allInventoryProducts.value.some(item => {
    if (item.quantity <= 0) return false;
    const allocatedQuantity = item.batchAllocations 
      ? item.batchAllocations.reduce((sum, alloc) => sum + (alloc.quantity || 0), 0)
      : 0;
    return (item.quantity || 0) !== allocatedQuantity;
  });
  
  if (hasUnallocatedItems) {
    ElMessage.warning('存在未完成批次分配的产品，请完成批次分配后再提交');
    return;
  }
  
  loading.value = true;
  try {
    const submitData = prepareSubmitData();
    submitData.status = 1;
    
    const url = isEditMode.value ? '/api/auth/outbound/updateProductionSaleOutBound' : '/api/auth/outbound/createProductionSaleOutBound';
    const res = await post(url, submitData);
    if (res) {
      ElMessage.success(isEditMode.value ? '更新成功' : '提交成功，等待审核');
      router.replace({
        path: '/',
        query: { mode: 'outbound' } // 返回到出库管理页面
      });
    }
  } catch (error) {
    ElMessage.error(isEditMode.value ? '更新失败' : '提交失败');
  } finally {
    loading.value = false;
  }
};

// 准备提交数据
const prepareSubmitData = () => {
  let items = [];
  
  items = allInventoryProducts.value
    .filter(item => item.quantity > 0)
    .map(item => ({
      productId: item.productId,
      productName: item.productName,
      sku: item.sku,
      spec: item.spec,
      unit: item.unitName,
      color: item.color,
      currentStock: item.availableQuantity,
      quantity: item.quantity,
      price: item.price,
      priceTotal: item.price * (item.quantity || 0),
      priceUnitUsd: item.priceUnitUsd || 0,
      priceTotalUsd: (item.priceUnitUsd || 0) * (item.quantity || 0),
      batchAllocations: (item.batchAllocations || []).map(allocation => ({
        batchNo: allocation.batchNo,
        shelfId: allocation.shelfId,
        shelfName: allocation.shelfName,
        quantity: allocation.quantity,
        price: allocation.price || item.price || 0
      })),
      remark: item.remark || ''
    }));
  
  return {
    ...formData,
    items: items,
    totalQuantity: totalQuantity.value,
    totalAmount: totalAmount.value,
    totalAmountUsd: totalAmountUsd.value
  };
};

const validateForm = async () => {
  if (!formRef.value) return false;
  
  try {
    await formRef.value.validate();
    
    const productsWithQuantity = allInventoryProducts.value.filter(p => p.quantity > 0);
    
    if (productsWithQuantity.length === 0) {
      ElMessage.warning('请至少设置一个产品的出库数量');
      return false;
    }
    
    const invalidProducts = productsWithQuantity.filter(p => (!p.price || p.price <= 0));
    if (invalidProducts.length > 0) {
      ElMessage.warning('请为所有出库数量大于0的产品设置有效的单价');
      return false;
    }

    const invalidUsdProducts = productsWithQuantity.filter(p => (!p.priceUnitUsd || p.priceUnitUsd <= 0));
    if (invalidUsdProducts.length > 0) {
      ElMessage.warning('请为所有出库数量大于0的产品设置有效的USD单价');
      return false;
    }
    
    // 检查库存是否足够
    const insufficientStockProducts = productsWithQuantity.filter(p => p.quantity > p.availableQuantity);
    if (insufficientStockProducts.length > 0) {
      const productNames = insufficientStockProducts.map(p => p.productName).join(', ');
      ElMessage.warning(`以下产品库存不足: ${productNames}`);
      return false;
    }
    
    return true;
  } catch (error) {
    ElMessage.warning('请完善表单信息');
    return false;
  }
};

// 文件选择处理
const handleFileChange = (file) => {
  const isLt10M = file.size / 1024 / 1024 < 10;
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过10MB!');
    return;
  }
  
  currentFile.value = file;
};

// 清空选择的文件
const clearSelectedFile = () => {
  currentFile.value = null;
  importResult.value = null;
};

// 导入提交处理
const handleImportSubmit = async () => {
  console.log("开始导入销售出库数量");
  if (!formData.warehouseId) {
    ElMessage.warning('请先选择仓库');
    return;
  }
  console.log("开始导入销售出库数量。仓库ID:", formData.warehouseId);
  if (!currentFile.value) {
    ElMessage.warning('请选择要上传的文件');
    return;
  }

  try {
    importLoading.value = true;
    const fd = new FormData();

    const realFile = currentFile.value.raw || currentFile.value;
    fd.append('file', realFile);
    fd.append('warehouseId', formData.warehouseId);

    console.log('FormData:');
    for (let [k, v] of fd.entries()) console.log(k, v);

    ElMessage.info('开始导入数据，请稍候...');

    const result = await post('/api/auth/outbound/importOutboundSaleQuantity', fd);
    console.log('导入响应:', result);

    if (result) {
      importResult.value = result;
      ElMessage.success(`导入成功！`);
      
      if (result && Array.isArray(result)) {
        allInventoryProducts.value.forEach(product => {
          product.quantity = 0;
          product.price = 0;
          product.remark = '';
          product.batchAllocations = [];
          product.isRecommend = false;
        });
        
        const { successCount, failCount } = applyImportedData(result);
        
        if (successCount > 0) {
          ElMessage.success(`成功导入 ${successCount} 条产品记录`);
        }
        if (failCount > 0) {
          ElMessage.warning(`${failCount} 条记录未找到匹配的产品`);
        }
        
        importDialog.visible = false;
        currentFile.value = null;
        importResult.value = null;
      }
    } else {
      ElMessage.error('导入失败，请检查数据格式');
    }
  } catch (error) {
    console.error('导入失败详情:', error);
    ElMessage.error("导入失败，请重试");
    importResult.value = {
      success: false,
      message: error.message || '导入失败，请重试'
    };
  } finally {
    importLoading.value = false;
  }
};

// 导入Excel
const handleImportExcel = () => {
  if (!formData.warehouseId) {
    ElMessage.warning('请先选择仓库');
    return;
  }
  
  importDialog.visible = true;
  currentFile.value = null;
  importResult.value = null;
};

const loadWarehouseList = async () => {
  try {
    const res = await get('/api/auth/warehouse/listEnable');
    warehouseList.value = res || [];
  } catch (error) {
    ElMessage.error('加载仓库列表失败');
  }
};

const loadCustomerList = async () => {
  try {
    const res = await get('/api/auth/customer/listEnable');
    customerList.value = res || [];
  } catch (error) {
    ElMessage.error('加载客户列表失败');
  }
};

const loadInventoryData = async (warehouseId) => {
  try {
    const res = await get(`/api/auth/inventory/listOfWarehouse?warehouseId=${warehouseId}`);
    inventoryList.value = res || [];
    
    productStockMap.value = {};
    inventoryList.value.forEach(item => {
      productStockMap.value[item.productId] = item.availableQuantity;
      if (item.price && item.price > 0) {
        item.priceFromApi = item.price;
      }
    });
    
    console.log('库存映射表:', productStockMap.value);
    
    updateInventoryProducts();
    
  } catch (error) {
    ElMessage.error('加载库存数据失败');
  }
};

// 文件上传相关方法
const handleExceed = () => {
  ElMessage.warning('最多只能上传5个文件');
};

const beforeUpload = (file) => {
  const isLt10M = file.size / 1024 / 1024 < 10;
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过10MB');
    return false;
  }
  return true;
};

const handleUploadSuccess = (response, file) => {
  formData.attachments.push({
    fileName: file.name,
    filePath: response.data,
    fileSize: file.size,
    fileType: file.type
  });
};

const handleRemoveFile = (file) => {
  const index = formData.attachments.findIndex(att => att.fileName === file.name);
  if (index > -1) {
    formData.attachments.splice(index, 1);
  }
};

// 附件预览
const handlePreview = (file) => {
  if (file.url) {
    window.open(file.url, '_blank');
  }
};

onMounted(() => {
  if (isEditMode.value) {
    loadOutboundDetail(route.params.id);
  } else {
    generateOrderNo();
  }
  loadWarehouseList();
  loadCustomerList();
});

// 监听路由变化，处理直接通过URL进入的情况
watch(
  () => route.params.id,
  (newId) => {
    if (newId) {
      loadOutboundDetail(newId);
    } else {
      Object.assign(formData, {
        id: null,
        orderNo: '',
        orderType: 1,
        warehouseId: null,
        customerId: null,
        expectedDate: '',
        relatedOrderNo: '',
        remark: '',
        status: 0,
        items: [],
        attachments: []
      });
      fileList.value = [];
      productStockMap.value = {};
      allInventoryProducts.value = [];
      filteredProducts.value = [];
      showRecommendations.value = false;
      recommendations.value = [];
      productSearch.value = '';
      generateOrderNo();
    }
  }
);

// 监听仓库变化，重新加载库存数据
watch(
  () => formData.warehouseId,
  (newWarehouseId) => {
    if (newWarehouseId) {
      loadInventoryData(newWarehouseId);
    } else {
      allInventoryProducts.value = [];
      filteredProducts.value = [];
    }
  }
);

// 监听产品搜索变化
watch(
  () => productSearch.value,
  () => {
    filterProducts();
  }
);
</script>

<style scoped>
.outbound-create-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.form-card {
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

.outbound-form {
  margin-bottom: 30px;
}

/* 智能推荐区域 */
.recommendation-section {
  margin: 20px 0;
  padding: 16px;
  background: linear-gradient(135deg, #f0f9ff 0%, #e6f7ff 100%);
  border: 1px solid #91d5ff;
  border-radius: 6px;
  box-shadow: 0 2px 8px rgba(145, 213, 255, 0.1);
}

.recommendation-header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.recommendation-header-left h3 {
  margin: 0;
  font-size: 16px;
  font-weight: bold;
  color: #1890ff;
  display: flex;
  align-items: center;
  gap: 6px;
}

.recommendation-header-left .tip {
  font-size: 12px;
  color: #69c0ff;
}

.recommendation-header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.recommendation-table {
  margin-top: 12px;
  border: 1px solid #91d5ff;
}

.recommendation-table :deep(.el-table__header-wrapper) {
  background-color: #e6f7ff;
}

.recommendation-table :deep(.el-table__body-wrapper) {
  background-color: #fafdff;
}

.product-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.product-name {
  font-weight: 500;
  color: #303133;
}

.sku-text {
  font-size: 12px;
  color: #909399;
}

.spec-text {
  font-size: 12px;
  color: #67c23a;
}

.recommend-reason {
  display: flex;
  flex-direction: column;
  gap: 4px;
  align-items: flex-start;
}

.confidence {
  font-size: 11px;
  color: #69c0ff;
}

.trigger-products {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.trigger-list {
  max-height: 200px;
  overflow-y: auto;
}

.trigger-item {
  padding: 4px 8px;
  font-size: 12px;
  color: #606266;
  border-bottom: 1px solid #f0f0f0;
}

.trigger-item:last-child {
  border-bottom: none;
}

.no-trigger {
  font-size: 12px;
  color: #909399;
}

.recommend-actions {
  display: flex;
  flex-direction: column;
  gap: 4px;
  align-items: center;
}

/* 产品明细区域 */
.product-section {
  margin: 30px 0;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
}

.section-header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.section-header-left h3 {
  margin: 0;
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.recommend-btn {
  display: flex;
  align-items: center;
  gap: 6px;
}

.header-right-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.tip {
  font-size: 12px;
  color: #909399;
}

/* 快速统计 */
.quick-stats {
  margin-bottom: 16px;
  padding: 12px 16px;
  background-color: #f8f9fa;
  border-radius: 6px;
  border: 1px solid #ebeef5;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.stat-label {
  font-size: 12px;
  color: #606266;
  white-space: nowrap;
}

.stat-value {
  font-size: 13px;
  font-weight: 500;
  color: #303133;
}

.stat-active {
  color: #1890ff;
  font-weight: bold;
}

/* 产品表格 */
.product-table {
  margin-bottom: 16px;
}

.table-header {
  display: flex;
  align-items: center;
  gap: 4px;
}

.header-icon {
  color: #909399;
  cursor: help;
}

.product-info-cell {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.product-name {
  font-weight: 500;
  color: #303133;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: color 0.2s;
}

.product-name:hover {
  color: #1890ff;
}

.recommend-tag {
  height: 20px;
  line-height: 18px;
}

.product-details {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.sku-text, .spec-text, .color-text {
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 3px;
}

.sku-text {
  color: #909399;
  background-color: #f5f5f5;
}

.spec-text {
  color: #67c23a;
  background-color: #f0f9eb;
}

.color-text {
  color: #e6a23c;
  background-color: #fdf6ec;
}

/* 库存信息 */
.stock-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  align-items: center;
}

.stock-progress {
  width: 60px;
}

.stock-none {
  color: #909399;
  font-weight: normal;
}

.stock-sufficient {
  color: #67C23A;
  font-weight: 500;
}

.stock-low {
  color: #E6A23C;
  font-weight: 500;
}

.stock-insufficient {
  color: #F56C6C;
  font-weight: bold;
}

/* 价格历史 */
.price-history {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.history-price-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
}

.price-label {
  color: #909399;
}

.price-value {
  color: #303133;
  font-weight: 500;
}

/* 数量单元格 */
.quantity-cell {
  display: flex;
  align-items: center;
  gap: 4px;
}

.quantity-input {
  flex: 1;
}

.max-btn {
  flex-shrink: 0;
  padding: 0 8px;
  height: 28px;
}

.price-input {
  width: 100%;
}

.amount-text {
  font-weight: 500;
  color: #303133;
}

/* 批次分配 */
.batch-allocation {
  min-height: 40px;
}

.batch-btn {
  margin-bottom: 8px;
}

.batch-summary {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.batch-tag {
  cursor: help;
}

.allocation-total {
  font-size: 12px;
  color: #67c23a;
  font-weight: 500;
}

.batch-detail {
  max-height: 300px;
  overflow-y: auto;
}

.batch-detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px;
  border-bottom: 1px solid #f0f0f0;
  font-size: 12px;
}

.batch-detail-item:last-child {
  border-bottom: none;
}

.batch-no {
  color: #303133;
  font-weight: 500;
  flex: 2;
}

.shelf-name {
  color: #606266;
  flex: 2;
}

.batch-quantity {
  color: #67c23a;
  font-weight: 500;
  flex: 1;
  text-align: right;
}

.batch-empty {
  margin-top: 8px;
}

.empty-text {
  color: #909399;
  font-size: 12px;
}

/* 统计信息 */
.summary-info {
  padding: 16px;
  background-color: #f8f9fa;
  border-radius: 4px;
  border: 1px solid #ebeef5;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.summary-item .label {
  color: #606266;
  font-size: 14px;
}

.summary-item .value {
  color: #303133;
  font-weight: bold;
  font-size: 16px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.summary-item .value.highlight {
  color: #1890ff;
}

.status-success {
  color: #67C23A;
}

.status-warning {
  color: #E6A23C;
}

.status-icon {
  font-size: 14px;
}

/* 附件区域 */
.attachment-section {
  margin-top: 30px;
}

.attachment-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.attachment-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background-color: #f8f9fa;
  border-radius: 4px;
  border: 1px solid #ebeef5;
}

.file-name {
  flex: 1;
  color: #303133;
}

.no-attachment {
  padding: 20px;
  text-align: center;
  color: #909399;
  background-color: #f8f9fa;
  border-radius: 4px;
  border: 1px dashed #dcdfe6;
}

/* 批次分配对话框 */
.batch-dialog-content {
  padding: 0 10px;
}

.batch-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 12px;
  background-color: #f8f9fa;
  border-radius: 4px;
  flex-wrap: wrap;
  gap: 12px;
}

.info-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 100px;
}

.info-item .label {
  font-size: 12px;
  color: #606266;
  margin-bottom: 4px;
}

.info-item .value {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.info-item .value.success {
  color: #67C23A;
}

.info-item .value.warning {
  color: #E6A23C;
}

/* 分配策略 */
.allocation-strategy {
  margin-bottom: 16px;
  padding: 12px;
  background-color: #f0f9ff;
  border: 1px solid #91d5ff;
  border-radius: 4px;
}

.strategy-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  font-weight: 500;
  color: #1890ff;
}

.strategy-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.expiry-expired {
  color: #f56c6c;
  font-weight: bold;
}

.expiry-soon {
  color: #e6a23c;
  font-weight: 500;
}

.expiry-near {
  color: #67c23a;
}

.batch-table {
  margin: 16px 0;
}

.shelf-allocation-container {
  padding: 8px 0;
}

.shelf-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.shelf-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 8px;
  background-color: #f8f9fa;
  border-radius: 4px;
  border: 1px solid #ebeef5;
  gap: 12px;
}

.shelf-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  flex: 6;
  min-width: 0;
}

.shelf-name {
  font-size: 13px;
  font-weight: 500;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.shelf-quantity {
  font-size: 12px;
  color: #909399;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.shelf-input {
  width: 100%;
  flex: 4;
  max-width: 120px;
}

.no-shelf {
  text-align: center;
  padding: 16px;
}

.no-shelf-text {
  color: #909399;
  font-size: 13px;
}

.allocation-zero {
  color: #909399;
}

.allocation-normal {
  color: #67C23A;
  font-weight: bold;
}

.allocation-exceed {
  color: #F56C6C;
  font-weight: bold;
}

.batch-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

/* 导入对话框 */
.import-dialog-content {
  padding: 20px 0;
}

.upload-demo {
  margin-bottom: 20px;
}

.selected-file {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 16px 0;
  padding: 8px 12px;
  background-color: #f8f9fa;
  border-radius: 4px;
  border: 1px solid #ebeef5;
}

.import-actions {
  margin: 16px 0;
  display: flex;
  justify-content: flex-end;
}

.import-tips {
  margin-top: 20px;
  padding: 16px;
  border-radius: 4px;
}

.import-tips h4 {
  margin: 0 0 12px 0;
  color: #303133;
}

.success-result {
  color: #67C23A;
  background-color: #f0f9ff;
  padding: 12px;
  border-radius: 4px;
}

.error-result {
  color: #F56C6C;
  background-color: #fef0f0;
  padding: 12px;
  border-radius: 4px;
}

/* 历史价格对话框 */
.history-dialog-content {
  min-height: 400px;
}

.history-summary {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 20px;
  padding: 16px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.history-summary .summary-item {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
}

.history-summary .label {
  font-size: 12px;
  color: #606266;
}

.history-summary .value {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.history-table {
  max-height: 300px;
  overflow-y: auto;
}

/* 通用样式 */
.shelf-disabled {
  background-color: #f5f7fa;
  opacity: 0.6;
}

.shelf-disabled .shelf-name,
.shelf-disabled .shelf-quantity {
  color: #c0c4cc;
}

.text-disabled {
  color: #c0c4cc;
}

:deep(.el-upload) {
  margin-right: 12px;
}

:deep(.el-upload-list) {
  margin-top: 12px;
}

:deep(.el-table) {
  margin-top: 0;
}

:deep(.el-table .el-input-number) {
  width: 100%;
}

:deep(.el-table .el-input-number .el-input__inner) {
  text-align: center;
}

:deep(.batch-table .el-input-number.is-disabled) {
  background-color: #f5f7fa;
  border-color: #e4e7ed;
}

:deep(.batch-table .el-input-number.is-disabled .el-input__inner) {
  color: #c0c4cc;
  background-color: #f5f7fa;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .outbound-create-container {
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
  
  .section-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
  
  .section-header-left {
    width: 100%;
    justify-content: space-between;
  }
  
  .header-right-actions {
    width: 100%;
    justify-content: flex-start;
    flex-wrap: wrap;
    gap: 8px;
  }
  
  .recommendation-header-left,
  .recommendation-header-right {
    width: 100%;
    justify-content: space-between;
  }
  
  .quick-stats .el-col {
    margin-bottom: 8px;
  }
  
  .summary-info .el-col {
    margin-bottom: 8px;
  }
  
  .shelf-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .shelf-info {
    width: 100%;
    flex-direction: row;
    justify-content: space-between;
    flex: none;
  }
  
  .shelf-input {
    width: 100%;
    max-width: none;
    flex: none;
  }
  
  .batch-info {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .info-item {
    flex-direction: row;
    justify-content: space-between;
    width: 100%;
    min-width: auto;
  }
  
  .history-summary {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 576px) {
  .product-details {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
  
  .header-right-actions {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .header-right-actions .el-input {
    width: 100%;
    margin-right: 0;
    margin-bottom: 8px;
  }
}
</style>