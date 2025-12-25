<template>
  <div class="outbound-create-container">
    <!-- 返回按钮行 -->
    <div class="back-header">
      <el-button 
        type="text" 
        @click="handleGoBack"
        :icon="ArrowLeft"
        class="back-btn"
      >
        返回
      </el-button>
    </div>

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
              v-if="!isViewMode && (!isEditMode || (isEditMode && formData.status === 0))"
            >
              保存草稿
            </el-button>
            <!-- <el-button 
              type="primary" 
              @click="handleSubmit" 
              :loading="loading"
              v-if="!isViewMode && (!isEditMode || (isEditMode && (formData.status === 0 || formData.status === 4)))"
            >
              {{ isEditMode ? '更新提交' : '提交审核' }}
            </el-button> -->
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
            <el-form-item label="关联单号" prop="relatedOrderNo">
              <el-input
                v-model="formData.relatedOrderNo"
                placeholder="请输入关联单号"
                :disabled="isViewMode"
              />
            </el-form-item>
          </el-col>
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

      <!-- 产品添加区域 -->
      <div class="product-add-section" v-if="!isViewMode && formData.warehouseId && formData.customerId">
        <div class="section-header">
          <h3>添加产品</h3>
          <div class="header-actions">
            <el-input
              v-model="quickSearch"
              placeholder="输入产品名称/SKU搜索..."
              style="width: 300px; margin-right: 10px;"
              clearable
              @keyup.enter="handleQuickSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            
            <el-button 
              type="primary" 
              @click="showProductSelector = true"
              :disabled="!availableProducts.length"
            >
              <el-icon><Plus /></el-icon>
              批量选择产品
            </el-button>
          </div>
        </div>

        <!-- 快速搜索结果 -->
        <div v-if="searchResults.length > 0" class="search-results">
          <el-card class="search-results-card">
            <template #header>
              <div class="search-results-header">
                <span>搜索结果</span>
                <el-button type="text" @click="clearSearch">
                  <el-icon><Close /></el-icon>
                </el-button>
              </div>
            </template>
            <div class="search-results-list">
              <div 
                v-for="product in searchResults" 
                :key="product.productId"
                class="search-result-item"
              >
                <div class="product-info">
                  <span class="product-name">{{ product.productName }}</span>
                  <span class="sku-text">{{ product.sku }}</span>
                  <span class="stock-text">库存: {{ product.availableQuantity }}</span>
                </div>
                <el-button 
                  type="primary" 
                  size="small" 
                  @click="addProductToTable(product)"
                >
                  添加到出库单
                </el-button>
              </div>
            </div>
          </el-card>
        </div>
      </div>

      <!-- 推荐产品展示区域 -->
      <div 
        v-if="(!isViewMode && groupedRecommendations.length > 0) || (isViewMode && hasRecommendationDetails)" 
        class="recommendations-panel"
      >
        <div class="recommendations-header">
          <div class="header-left">
            <h3>
              <el-icon><MagicStick /></el-icon>
              智能推荐
            </h3>
            <span class="tip" v-if="!isViewMode">基于您添加的产品，系统为您推荐以下搭配产品</span>
            <span class="tip" v-else>创建时的智能推荐记录</span>
          </div>
          <div class="header-right" v-if="!isViewMode">
            <el-button type="text" @click="applyAllRecommendations" :disabled="!hasValidRecommendations">
              应用所有推荐
            </el-button>
            <el-button type="text" @click="closeRecommendations">
              <el-icon><Close /></el-icon>
            </el-button>
          </div>
        </div>

        <!-- 按触发产品分组的推荐 -->
        <div 
          v-for="group in groupedRecommendations" 
          :key="group.triggerProductId"
          class="recommendation-group"
        >
          <div class="group-header">
            <div class="trigger-info">
              <span class="trigger-product">
                {{ getProductName(group.triggerProductId) }}
                <el-tag size="small">触发产品</el-tag>
              </span>
              <span class="trigger-quantity">数量: {{ getProductQuantity(group.triggerProductId) }}</span>
              <el-button 
                v-if="!isViewMode"
                type="text" 
                size="small" 
                @click="refreshRecommendations(group.triggerProductId)"
                class="refresh-btn"
              >
                <el-icon><Refresh /></el-icon>
                重新获取推荐
              </el-button>
            </div>
            <el-button 
              v-if="!isViewMode"
              type="primary" 
              size="small" 
              @click="applyGroupRecommendations(group)"
              :disabled="!group.hasValidItems"
            >
              应用本组推荐
            </el-button>
          </div>

          <el-table :data="group.items" class="recommendation-items-table" border>
            <el-table-column label="推荐产品" min-width="150">
              <template #default="{ row }">
                <div class="recommended-product-info">
                  <div class="product-name">{{ row.productName }}</div>
                  <div class="sku-text">sku:{{ row.sku }}</div>
                  <div class="spec-text">规格:{{ row.spec || '-' }}</div>
                  <div class="spec-text">颜色: {{ row.color || '-' }}</div>
                  
                  <div v-if="row.selected" class="source-tip">
                    <el-tag size="mini" type="success">已添加</el-tag>
                    <span class="source-text">来自: {{ getProductName(row.triggerProductId) }}</span>
                  </div>
                </div>
              </template>
            </el-table-column>

            <el-table-column label="推荐类型" width="150">
              <template #default="{ row }">
                <el-tag :type="row.isRequired ? 'danger' : 'info'" size="small">
                  {{ row.isRequired ? '必选' : '可选' }}
                </el-tag>
                <div v-if="row.confidence" class="confidence">
                  置信度: {{ (row.confidence * 100).toFixed(0) }}%
                </div>
              </template>
            </el-table-column>

            <el-table-column label="推荐数量" width="180">
              <template #default="{ row }">
                <div class="recommended-quantity">
                  <div v-if="row.quantityType === 1">
                    <span class="quantity-type">固定数量</span>
                    <span class="quantity-value">{{ row.calculatedQuantity || row.quantityValue }}</span>
                  </div>
                  <div v-else>
                    <span class="quantity-type">比例: {{ row.quantityValue }}</span>
                    <span class="quantity-value">
                      = {{ getTriggerProductQuantity(row.triggerProductId) }} × {{ row.quantityValue }}
                      = {{ row.calculatedQuantity || calculateRecommendedQuantity(row, row.triggerProductId) }}
                    </span>
                  </div>
                </div>
              </template>
            </el-table-column>

            <el-table-column label="库存" width="150" align="center">
              <template #default="{ row }">
                <span :class="getStockClass(row.availableQuantity, row.calculatedQuantity || row.quantityValue)">
                  {{ row.availableQuantity }}
                </span>
              </template>
            </el-table-column>

            <el-table-column label="备注" width="200" align="center">
              <template #default="{ row }">
                <span class="stock-none">
                  {{ row.remark }}
                </span>
              </template>
            </el-table-column>

            <el-table-column label="是否添加" width="120" align="center" v-if="!isViewMode">
              <template #default="{ row }">
                <el-checkbox 
                  v-model="row.selected"
                  :disabled="row.isRequired || isViewMode"
                  @change="handleRecommendationToggle(row)"
                >
                  {{ row.selected ? '已添加' : '添加' }}
                </el-checkbox>
                <div v-if="row.isRequired" class="required-tip">必选</div>
              </template>
            </el-table-column>
            
            <el-table-column label="实际数量" width="100" align="center" v-if="isViewMode">
              <template #default="{ row }">
                <span v-if="row.selected" class="actual-quantity">
                  {{ row.actualQuantity || row.calculatedQuantity || 0 }}
                </span>
                <span v-else>-</span>
              </template>
            </el-table-column>
          </el-table>
        </div>
        
        <!-- 在查看模式下的推荐汇总 -->
        <div v-if="isViewMode && !groupedRecommendations.length && hasRecommendationDetails" class="view-mode-recommendations">
          <div class="recommendation-summary">
            <h4>推荐产品汇总</h4>
            <div class="recommendation-list">
              <div 
                v-for="(details, productId) in recommendationDetails" 
                :key="productId"
                class="recommendation-item"
              >
                <span class="product-name">{{ getProductName(productId) }}</span>
                <!-- 检查是否为包装件 -->
                <span v-if="details[0] && details[0].isPackage" class="total-recommended">
                  包装推荐: {{ details[0].recommendedQuantity ? details[0].recommendedQuantity.toFixed(2) : '0.00' }} 个/件
                  <span class="source-count" v-if="details[0].packageRatio">
                    (比例: 1:{{ details[0].packageRatio }})
                  </span>
                </span>
                <!-- 显示普通推荐商品 -->
                <span v-else class="total-recommended">
                  总推荐量: {{ getTotalRecommendedQuantity(productId) }} 个
                  <span class="source-count" v-if="details.length > 0">(来自 {{ details.length }} 个来源)</span>
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 产品明细表格 -->
      <div class="product-section">
        <div class="section-header">
          <div class="section-header-left">
            <h3>产品明细</h3>
          </div>
          <div class="header-right-actions" v-if="!isViewMode">
            <div v-if="isAllocating" class="auto-allocation-status">
              <el-progress 
                :percentage="allocationProgress" 
                :stroke-width="6"
                :show-text="false"
                color="#67C23A"
                class="allocation-progress"
              />
              <span class="progress-text">
                自动分配中: {{ Math.round(allocationProgress) }}%
                ({{ currentAllocationIndex }}/{{ totalAllocationCount }})
              </span>
            </div>
            
            <el-button 
              type="success" 
              @click="handleAutoAllocateAll"
              :loading="autoAllocating"
              :disabled="!canAutoAllocate || isViewMode"
              class="auto-allocate-btn"
            >
              <el-icon><MagicStick /></el-icon>
              自动全部分配
            </el-button>

            <!-- <el-button 
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
            </el-button> -->
          </div>
        </div>

        <!-- 统计信息 -->
        <div class="quick-stats" v-if="filteredProducts.length > 0">
          <el-row :gutter="10">
            <el-col :span="3">
              <div class="stat-item">
                <span class="stat-label">出库产品:</span>
                <span class="stat-value">{{ filteredProducts.length }} 项</span>
              </div>
            </el-col>
            <el-col :span="3">
              <div class="stat-item">
                <span class="stat-label">触发商品:</span>
                <span class="stat-value">{{ filteredProducts.filter(p => p.isTriggerProduct).length }} 项</span>
              </div>
            </el-col>
            <el-col :span="4">
              <div class="stat-item">
                <span class="stat-label">推荐商品:</span>
                <span class="stat-value">{{ filteredProducts.filter(p => p.isRecommend).length }} 项</span>
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

        <!-- 自动分配结果提示 -->
        <div v-if="autoAllocationResult" class="auto-allocation-result">
          <el-alert 
            :title="autoAllocationResult.success ? '自动分配成功' : '自动分配失败'" 
            :type="autoAllocationResult.success ? 'success' : 'error'"
            :description="autoAllocationResult.message"
            :closable="true"
            @close="autoAllocationResult = null"
          />
        </div>

        <!-- 销售出库的产品表格 -->
        <el-table
          v-if="formData.warehouseId"
          :data="filteredProducts"
          border
          class="product-table"
          empty-text="请先选择仓库和客户，然后添加产品"
          @selection-change="handleSelectionChange"
          @row-click="handleRowClick"
          row-key="productId"
          :row-class-name="tableRowClassName"
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
                  
                  <div class="product-tags">
                    <el-tag 
                      v-if="row.isTriggerProduct" 
                      size="small" 
                      type="success" 
                      class="trigger-tag"
                    >
                      触发商品
                    </el-tag>
                    <el-tag 
                      v-else-if="row.isRecommend" 
                      size="small" 
                      type="primary" 
                      class="recommend-tag"
                    >
                      推荐商品
                    </el-tag>
                    <el-tag 
                      v-else-if="row.isPackage" 
                      size="small" 
                      type="warning" 
                      class="package-tag"
                    >
                      包装件
                    </el-tag>
                    <el-tag 
                      v-else 
                      size="small" 
                      type="info" 
                      class="normal-tag"
                    >
                      普通商品
                    </el-tag>
                  </div>
                </div>
                <!-- 包装件来源信息 -->
                <div v-if="row.isPackage && row.parentProductId" class="package-source-row">
                  <el-tooltip
                    :content="getProductFullName(row.parentProductId)"
                    placement="top"
                  >
                    <span class="package-source-text">
                      <el-icon><Connection /></el-icon>
                      包装来源: {{ getTruncatedProductName(row.parentProductId) }}
                    </span>
                  </el-tooltip>
                  <span v-if="row.packageRatio" class="package-ratio">
                    比例: 1:{{ row.packageRatio }}
                  </span>
                </div>
                
                <div class="product-details">
                  <div class="sku-text">{{ row.sku }}</div>
                  <div class="spec-text">{{ row.spec || '-' }}</div>
                  <div class="color-text">{{ row.color || '-' }}</div>

                  <div v-if="row.triggerProductId" class="trigger-source-row">
                    <el-tooltip
                      :content="getProductFullName(row.triggerProductId)"
                      placement="top"
                    >
                      <span class="trigger-source-text">
                        <el-icon><Connection /></el-icon>
                        来自: {{ getTruncatedProductName(row.triggerProductId) }}
                      </span>
                    </el-tooltip>
                    <span class="recommend-quantity-info">
                      推荐: {{ row.quantity }} 个
                    </span>
                  </div>
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
          
          <!-- <el-table-column label="历史价格" width="140">
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
          </el-table-column> -->
          
          <!-- 在推荐数量列中 -->
          <!-- 推荐数量列 - 固定显示 -->
          <el-table-column label="推荐数量" width="180" align="center">
            <template #default="{ row }">
              <!-- 1. 推荐商品的显示 -->
              <el-popover
                v-if="row.isRecommend && getRecommendationDetails(row.productId, row.parentTriggerId).length > 0"
                placement="top-start"
                :width="280"
                trigger="click"
                :show-arrow="false"
              >
                <template #reference>
                  <div class="recommend-quantity-summary" @click.stop>
                    <div class="summary-badge">
                      <el-tag size="small" type="info" class="count-tag">
                        {{ getRecommendationDetails(row.productId, row.parentTriggerId).length }}
                      </el-tag>
                    </div>
                    <div class="total-quantity-display">
                      {{ getTotalRecommendedQuantity(row.productId, row.parentTriggerId) }} 个
                      <!-- 显示触发商品信息 -->
                      <span v-if="row.parentTriggerId" class="trigger-hint">
                        (来自: {{ getTruncatedProductName(row.parentTriggerId) }})
                      </span>
                    </div>
                    <el-icon class="expand-icon"><ArrowRight /></el-icon>
                  </div>
                </template>
                
                <div class="recommend-detail-popover">
                  <div class="popover-header">
                    <div class="product-header">
                      <el-icon><Connection /></el-icon>
                      <span class="product-name">{{ row.productName }}</span>
                    </div>
                    <div class="recommend-title">
                      推荐来源详情
                      <span class="trigger-source-badge" v-if="row.parentTriggerId">
                        (来自 {{ getTruncatedProductName(row.parentTriggerId) }})
                      </span>
                    </div>
                  </div>
                  
                  <div class="detail-scroll">
                    <!-- 显示特定触发商品的推荐 -->
                    <div 
                      v-for="(detail, index) in getRecommendationDetails(row.productId, row.parentTriggerId)" 
                      :key="`${detail.triggerProductId}_${index}`" 
                      class="detail-item"
                      :class="{'last-item': index === getRecommendationDetails(row.productId, row.parentTriggerId).length - 1}"
                    >
                      <div class="trigger-source">
                        <el-icon><Promotion /></el-icon>
                        <div class="trigger-info">
                          <div class="trigger-name">{{ getTriggerProductName(detail.triggerProductId) }}</div>
                          <div class="trigger-spec">
                            {{ getProductSpec(detail.triggerProductId) }}
                          </div>
                        </div>
                      </div>
                      <div class="recommend-quantity">
                        <span class="quantity-label">推荐数量</span>
                        <span class="quantity-value">{{ detail.recommendedQuantity }} 个</span>
                        <span v-if="detail.isRequired" class="required-badge">必选</span>
                      </div>
                    </div>
                  </div>
                  
                  <div class="popover-footer">
                    <div class="footer-summary">
                      <span class="total-label">推荐总数:</span>
                      <span class="total-number">{{ getTotalRecommendedQuantity(row.productId, row.parentTriggerId) }}</span>
                      <span class="total-unit">个</span>
                    </div>
                  </div>
                </div>
              </el-popover>
              
              <!-- 在推荐数量列中 -->
              <!-- 2. 包装件的显示 -->
              <div v-else-if="row.isPackage" class="package-recommended-quantity">
                <div class="recommended-value">
                  {{ (row.quantity || 0).toFixed(2) }} 个
                  <span v-if="row.recommendedQuantity" class="recommend-tip">
                    (推荐: {{ row.recommendedQuantity.toFixed(2) }} 个/件)
                  </span>
                </div>
                <div v-if="row.packageRatio" class="ratio-info">
                  比例: 1:{{ row.packageRatio }} (每{{ row.packageRatio }}个成品需要1个包装)
                </div>
                <div class="trigger-info" v-if="row.parentProductId">
                  <el-icon><Connection /></el-icon>
                  <span class="trigger-name">来自: {{ getTruncatedProductName(row.parentProductId) }}</span>
                </div>
              </div>
              
              <!-- 3. 触发商品的显示 -->
              <div v-else-if="row.isTriggerProduct && row.productLevel === 0" class="trigger-recommend-info">
                <el-tag size="small" type="success" class="trigger-tag">触发商品</el-tag>
                <div class="trigger-tip">点击"刷新推荐"获取推荐商品</div>
              </div>
              
              <!-- 4. 其他情况显示"-" -->
              <span v-else>-</span>
            </template>
          </el-table-column>
          
          <el-table-column label="出库数量" width="150">
            <template #default="{ row }">
              <div class="quantity-cell">
                <el-input-number
                  v-if="!isViewMode"
                  v-model="row.quantity"
                  :min="0"
                  :max="getQuantityMax(row)"
                  controls-position="right"
                  size="small"
                 @change="(value) => {
                    console.log(`Quantity change for  ${row.triggerProductId} ${row.productName}: ${row.quantity} -> ${value}`);
                    handleQuantityChange(row, value);
                  }"
                  @blur="(e) => {
                    console.log(`Quantity blur for ${row.productName}: ${row.quantity}`);
                  }"
                  placeholder="数量"
                  :disabled="row.availableQuantity <= 0"
                  class="quantity-input"
                />
                <span v-else class="quantity-view">
                  {{ row.quantity || 0 }}
                </span>
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
            <template #default="{ row, $index }">
              <div 
                class="remark-cell" 
                @click="openRemarkDialog(row, $index)"
                :class="{ 'remark-disabled': isViewMode || !row.quantity || row.quantity <= 0 }"
              >
                <div v-if="row.remark" class="remark-content">
                  <span class="remark-text">{{ row.remark }}</span>
                  <el-icon v-if="!isViewMode && row.quantity > 0" class="edit-icon"><Edit /></el-icon>
                </div>
                <div v-else class="remark-empty">
                  <span>点击添加备注</span>
                  <el-icon v-if="!isViewMode && row.quantity > 0" class="edit-icon"><Edit /></el-icon>
                </div>
                <div v-if="row.remark" class="remark-length">
                  {{ row.remark.length }}/200
                </div>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="操作" width="120" fixed="right" align="center" v-if="!isViewMode">
            <template #default="{ row, $index }">
              <el-button
                type="danger"
                link
                @click="removeProductFromTable(row, $index)"
              >
                删除
              </el-button>
              <el-button
                v-if="row.isTriggerProduct"
                type="text"
                link
                @click="refreshTriggerRecommendations(row.productId)"
                size="small"
              >
                刷新推荐
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 没有产品时的提示 -->
        <div v-if="!filteredProducts.length" class="empty-product-tip">
          <div class="empty-content">
            <el-icon class="empty-icon"><Goods /></el-icon>
            <p>请先选择仓库和客户，然后添加产品</p>
            <el-button 
              type="primary" 
              @click="showProductSelector = true"
              :disabled="!availableProducts.length"
            >
              <el-icon><Plus /></el-icon>
              添加产品
            </el-button>
          </div>
        </div>

        <!-- 底部统计信息 -->
        <div class="summary-info" v-if="filteredProducts.length > 0">
          <el-row :gutter="20">
            <el-col :span="3">
              <div class="summary-item">
                <span class="label">出库产品：</span>
                <span class="value">{{ filteredProducts.length }} 种</span>
              </div>
            </el-col>
            <el-col :span="4">
              <div class="summary-item">
                <span class="label">触发产品：</span>
                <span class="value">{{ triggerProductCount }} 个</span>
              </div>
            </el-col>
            <el-col :span="4">
              <div class="summary-item">
                <span class="label">推荐产品：</span>
                <span class="value">{{ recommendedProductCount }} 个</span>
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
            <el-col :span="5">
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

    <!-- 产品选择对话框 -->
    <el-dialog
      v-model="showProductSelector"
      title="选择产品"
      width="900px"
      destroy-on-close
      @closed="handleProductSelectorClosed"
    >
      <div class="product-selector-dialog">
        <!-- 搜索和筛选 -->
        <div class="selector-filter">
          <el-input
            v-model="selectorSearch"
            placeholder="搜索产品名称、SKU..."
            clearable
            style="width: 300px; margin-right: 10px;"
            @input="filterSelectorProducts"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          
          <el-button type="text" @click="clearSelectorSelection">
            清空选择
          </el-button>
        </div>

        <!-- 产品列表 -->
        <el-table 
          :data="filteredSelectorProducts" 
          @selection-change="handleProductSelection"
          class="selector-table"
          height="400"
          border
        >
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" width="60" align="center" />
          
          <el-table-column label="产品信息" min-width="250">
            <template #default="{ row }">
              <div class="product-info">
                <div class="product-name">{{ row.productName }}</div>
                <div class="sku-text">{{ row.sku }}</div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="规格" width="80" align="center">
            <template #default="{ row }">
              {{ row.spec || '-' }}
            </template>
          </el-table-column>

          <el-table-column label="颜色" width="80" align="center">
            <template #default="{ row }">
              {{ row.color || '-' }}
            </template>
          </el-table-column>
          
          <el-table-column label="库存" width="100" align="center">
            <template #default="{ row }">
              <span :class="row.availableQuantity <= 0 ? 'stock-none' : 'stock-ok'">
                {{ row.availableQuantity }}
              </span>
            </template>
          </el-table-column>
        </el-table>

        <div class="selector-actions">
          <div class="selected-count">
            已选择 {{ selectedProductsForSelector.length }} 个产品
          </div>
          <div class="action-buttons">
            <el-button @click="showProductSelector = false">取消</el-button>
            <el-button 
              type="primary" 
              @click="addSelectedProducts"
              :disabled="selectedProductsForSelector.length === 0"
            >
              添加并处理推荐
            </el-button>
          </div>
        </div>
      </div>
    </el-dialog>

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

    <!-- 备注编辑对话框 -->
    <el-dialog
      v-model="remarkDialog.visible"
      :title="`编辑备注 - ${remarkDialog.productName}`"
      width="500px"
      destroy-on-close
      @closed="handleRemarkDialogClosed"
      @keydown.enter="saveRemark"
      @keydown.esc="remarkDialog.visible = false"
    >
      <div class="remark-dialog-content">
        <el-input
          v-model="remarkDialog.remark"
          type="textarea"
          :rows="6"
          placeholder="请输入产品备注"
          maxlength="200"
          show-word-limit
          resize="none"
          class="remark-textarea"
        />
        
        <div class="remark-dialog-actions">
          <el-button @click="remarkDialog.visible = false">取消</el-button>
          <el-button type="primary" @click="saveRemark">保存</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import * as XLSX from 'xlsx';
import { saveAs } from 'file-saver';
import { ref, reactive, computed, onMounted, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { 
  Plus, Upload, Download, Document, Close, Search, 
  MagicStick, Warning, InfoFilled, Refresh, 
  SetUp, ArrowUp, Goods, Connection, ArrowLeft,
  ArrowRight, Promotion, Edit
} from '@element-plus/icons-vue';
import { post, get } from '@/net';

const router = useRouter();
const route = useRoute();
const formRef = ref();
const loading = ref(false);
const downloadLoading = ref(false);

const showProductSelector = ref(false);
const quickSearch = ref('');
const searchResults = ref([]);
const selectorSearch = ref('');
const selectedProductsForSelector = ref([]);

const autoAllocating = ref(false);
const isAllocating = ref(false);
const allocationProgress = ref(0);
const currentAllocationIndex = ref(0);
const totalAllocationCount = ref(0);
const autoAllocationResult = ref(null);

const availableProducts = ref([]);
const filteredSelectorProducts = ref([]);

const groupedRecommendations = ref([]);
const recommendationDetails = ref({});

const showAllocationStrategy = ref(false);
const allocationStrategy = ref('FIFO');
const applyingStrategy = ref(false);
const loadingBatches = ref(false);

const outboundProducts = ref([]);

const formData = reactive({
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

const warehouseList = ref([]);
const customerList = ref([]);
const fileList = ref([]);

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

const remarkDialog = reactive({
  visible: false,
  productId: null,
  productName: '',
  remark: '',
  rowIndex: -1
});

const loadingHistory = ref(false);

const importDialog = reactive({
  visible: false
});

const currentFile = ref(null);
const importResult = ref(null);
const importLoading = ref(false);

const isEditMode = computed(() => !!route.params.id);

const isViewMode = computed(() => {
  if (isEditMode.value) {
    return formData.status !== 0;
  }
  return false;
});

const filteredProducts = computed(() => {
  if (isViewMode.value) {
    return outboundProducts.value;
  }
  
  const sortedProducts = sortProductsByHierarchy();
  
  return sortedProducts.filter(p => {
    if (p.quantity > 0) return true;
    
    if (p.productLevel === 0) {
      return true;
    }
    
    if (p.productLevel === 1) {
      const triggerGroup = groupedRecommendations.value.find(g => 
        g.triggerProductId === p.parentTriggerId
      );
      if (triggerGroup) {
        const panelItem = triggerGroup.items.find(i => i.productId === p.productId);
        if (panelItem && panelItem.isRequired) {
          return true;
        }
      }
    }
    
    if (p.productLevel === 2) {
      return true;
    }
    
    return false;
  });
});

const totalQuantity = computed(() => {
  return filteredProducts.value
    .filter(p => p.quantity > 0)
    .reduce((sum, item) => sum + (item.quantity || 0), 0);
});

const totalAmount = computed(() => {
  return filteredProducts.value
    .filter(p => p.quantity > 0)
    .reduce((sum, item) => {
      const price = item.price || 0;
      const quantity = item.quantity || 0;
      return sum + (price * quantity);
    }, 0);
});

const totalAmountUsd = computed(() => {
  return filteredProducts.value
    .filter(p => p.quantity > 0)
    .reduce((sum, item) => {
      const priceUsd = item.priceUnitUsd || 0;
      const quantity = item.quantity || 0;
      return sum + (priceUsd * quantity);
    }, 0);
});


// 打开备注编辑对话框
const openRemarkDialog = (row, index) => {
  if (isViewMode.value || !row.quantity || row.quantity <= 0) {
    return;
  }
  
  remarkDialog.visible = true;
  remarkDialog.productId = row.productId;
  remarkDialog.productName = row.productName;
  remarkDialog.remark = row.remark || '';
  remarkDialog.rowIndex = index;
};

// 保存备注
const saveRemark = () => {
  if (remarkDialog.rowIndex >= 0 && remarkDialog.rowIndex < outboundProducts.value.length) {
    outboundProducts.value[remarkDialog.rowIndex].remark = remarkDialog.remark;
    remarkDialog.visible = false;
    ElMessage.success('备注保存成功');
  }
};

// 关闭备注对话框
const handleRemarkDialogClosed = () => {
  remarkDialog.productId = null;
  remarkDialog.productName = '';
  remarkDialog.remark = '';
  remarkDialog.rowIndex = -1;
};


const hasValidRecommendations = computed(() => {
  return groupedRecommendations.value.some(group => 
    group.items.some(item => !item.selected && item.availableQuantity > 0)
  );
});

const recommendedProductCount = computed(() => {
  return filteredProducts.value.filter(p => p.isRecommend && p.quantity > 0).length;
});

const triggerProductCount = computed(() => {
  return filteredProducts.value.filter(p => p.isTriggerProduct && p.quantity > 0).length;
});

const hasInsufficientStock = computed(() => {
  return filteredProducts.value.some(item => {
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

const hasRecommendationDetails = computed(() => {
  return Object.keys(recommendationDetails.value).length > 0;
});

const canAutoAllocate = computed(() => {
  return formData.warehouseId && 
         outboundProducts.value.length > 0 && 
         outboundProducts.value.some(item => item.quantity > 0) &&
         !isAllocating.value &&
         !isViewMode.value;
});

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

const tableRowClassName = ({ row }) => {
  let className = '';
  
  if (row.productLevel === 0) {
    className += 'product-level-0 ';
  } else if (row.productLevel === 1) {
    className += 'product-level-1 ';
  } else if (row.productLevel === 2) {
    className += 'product-level-2 ';
  }
  
  if (row.isTriggerProduct && !row.isPackage && !row.isRecommend) {
    className += 'trigger-product-row';
  } else if (row.isRecommend) {
    className += 'recommend-product-row';
  } else if (row.isPackage) {
    className += 'package-product-row';
  }
  
  return className.trim();
};

const getQuantityMax = (row) => {
  if (isViewMode.value) {
    return Number.MAX_SAFE_INTEGER;
  }
  return row.availableQuantity || 0;
};

const generateOrderNo = () => {
  const date = new Date();
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const random = String(Math.random()).substr(2, 6);
  formData.orderNo = `CK${year}${month}${day}${random}`;
};

const handleGoBack = () => {
  const hasUnsavedChanges = formData.items.length > 0 || 
                           formData.warehouseId || 
                           formData.customerId || 
                           formData.relatedOrderNo || 
                           formData.remark;
  
  if (hasUnsavedChanges && !isEditMode.value) {
    ElMessageBox.confirm(
      '当前表单有未保存的更改，确定要返回吗？',
      '确认返回',
      {
        type: 'warning',
        confirmButtonText: '确定返回',
        cancelButtonText: '取消',
        distinguishCancelAndClose: true
      }
    ).then(() => {
      router.back();
    }).catch(() => {
    });
  } else {
    router.back();
  }
};

const loadAvailableProducts = async () => {
  if (!formData.warehouseId) return;
  
  try {
    const res = await get(`/api/auth/inventory/listOfWarehouse?warehouseId=${formData.warehouseId}`);
    if (res && Array.isArray(res)) {
      availableProducts.value = res.map(item => ({
        ...item,
        originalPrice: item.price || 0,
        originalPriceUnitUsd: item.priceUnitUsd || 0
      }));
      filteredSelectorProducts.value = [...availableProducts.value];
      
      if (isEditMode.value && outboundProducts.value.length > 0) {
        outboundProducts.value.forEach(product => {
          const availableProduct = availableProducts.value.find(p => p.productId === product.productId);
          if (availableProduct) {
            product.availableQuantity = availableProduct.availableQuantity;
          }
        });
      }
    }
  } catch (error) {
    console.error('加载可用产品失败:', error);
  }
};

const getTriggerProductName = (productId) => {
  const product = outboundProducts.value.find(p => p.productId === productId) || 
                  availableProducts.value.find(p => p.productId === productId);
  return product ? product.productName : '未知产品';
};

const getProductSpec = (productId) => {
  const product = outboundProducts.value.find(p => p.productId === productId) || 
                  availableProducts.value.find(p => p.productId === productId);
  if (!product) return '';
  
  const parts = [];
  if (product.spec && product.spec !== '-') parts.push(product.spec);
  if (product.color && product.color !== '-') parts.push(product.color);
  
  return parts.join(' | ') || '无规格';
};

const loadOutboundDetail = async (id) => {
  loading.value = true;
  try {
    const res = await get(`/api/auth/outbound/detail?orderId=${id}`);

    if (res) {
      const detailData = res.data || res;
      
      Object.assign(formData, {
        id: detailData.id,
        orderNo: detailData.orderNo,
        orderType: detailData.orderType,
        warehouseId: detailData.warehouseId,
        customerId: detailData.customerId,
        expectedDate: detailData.expectedDate,
        relatedOrderNo: detailData.relatedOrderNo || '',
        remark: detailData.remark || '',
        status: detailData.status || 0
      });

      if (detailData.attachments && detailData.attachments.length > 0) {
        fileList.value = detailData.attachments.map(att => ({
          name: att.fileName,
          url: att.filePath,
          status: 'success'
        }));
        formData.attachments = detailData.attachments;
      }
      
      outboundProducts.value = [];
      groupedRecommendations.value = [];
      recommendationDetails.value = {};
      
      if (detailData.items && detailData.items.length > 0) {
        const itemsWithOrder = detailData.items.map((item, index) => {
          const extension = item.extension || {};
          const isRecommendProduct = extension.isRecommendProduct;
          
          let isTriggerProduct, isRecommend, isPackage;
          let productLevel = 0;
          let parentTriggerId = null;
          let packageRatioValue = null;
          let recommendedQuantityValue = 0;
          
          if (isRecommendProduct === 1) {
            isTriggerProduct = extension.isTriggerProduct === 0;
            isRecommend = true;
            isPackage = false;
            productLevel = 1;
            parentTriggerId = extension.triggerProductId || null;
          } else if (isRecommendProduct === 2) {
            isTriggerProduct = false;
            isRecommend = false;
            isPackage = true;
            productLevel = 2;
            parentTriggerId = extension.parentProductId || null;
            
            // 修复：使用正确的变量名
            packageRatioValue = extension.packageRatio || null;
            
            // 计算包装件推荐数量
            if (packageRatioValue && packageRatioValue > 0) {
              recommendedQuantityValue = 1 / packageRatioValue;
            }
          } else {
            isTriggerProduct = extension.isTriggerProduct === 0;
            isRecommend = false;
            isPackage = false;
            productLevel = 0;
          }

          return {
            productId: item.productId,
            productName: item.productName,
            sku: item.sku,
            spec: item.spec || '',
            color: item.color || '',
            unitName: item.unit || '',
            quantity: item.quantity || 0,
            price: item.priceUnit ? Number(item.priceUnit) : 0,
            priceUnitUsd: item.priceUnitUsd ? Number(item.priceUnitUsd) : 0,
            remark: item.remark || '',
            batchAllocations: item.batchAllocations || [],
            availableQuantity: item.quantity * 2,
            isTriggerProduct: isTriggerProduct,
            isRecommend: isRecommend,
            isPackage: isPackage,
            triggerProductId: extension.triggerProductId || null,
            parentProductId: extension.parentProductId || null,
            packageRatio: packageRatioValue, // 使用正确的变量名
            recommendedQuantity: recommendedQuantityValue, // 添加推荐数量
            extension: extension,
            originalPrice: item.priceUnit ? Number(item.priceUnit) : 0,
            originalPriceUnitUsd: item.priceUnitUsd ? Number(item.priceUnitUsd) : 0,
            historyPrice: null,
            lastQuantity: item.quantity ? Number(item.quantity) : 0,
            originalIndex: index,
            productLevel: productLevel,
            parentTriggerId: parentTriggerId,
            isRequired: extension.isRequired || false
          };
        });
        
        outboundProducts.value = itemsWithOrder;
      }
      
      if (formData.warehouseId) {
        await loadAvailableProducts();
      }
      
      if (formData.customerId && formData.warehouseId) {
        await loadRecommendationsForExistingProducts();
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

const loadRecommendationsForExistingProducts = async () => {
  if (!formData.customerId || !formData.warehouseId) return;
  
  groupedRecommendations.value = [];
  recommendationDetails.value = {};
  
  // 先处理包装件的推荐信息
  outboundProducts.value.forEach(product => {
    if (product.isPackage && product.packageRatio) {
      console.log(`处理包装件推荐信息: ${product.productName}`);
      
      if (!recommendationDetails.value[product.productId]) {
        recommendationDetails.value[product.productId] = [];
      }
      
      const recommendedQuantity = product.recommendedQuantity || (1 / product.packageRatio);
      
      recommendationDetails.value[product.productId].push({
        triggerProductId: product.parentProductId,
        recommendedQuantity: recommendedQuantity,
        productName: product.productName,
        isRequired: true,
        isPackage: true,
        packageRatio: product.packageRatio,
        calculatedQuantity: product.quantity || 0
      });
    }
  });
  
  // 再处理触发商品的推荐
  const triggerProducts = outboundProducts.value.filter(p => 
    p.isTriggerProduct && p.quantity > 0
  );
  
  for (const product of triggerProducts) {
    let quantityOfTriggerProducts = Number(product.quantity) || 0;
    
    if (quantityOfTriggerProducts > 0) {
      try {
        await loadRecommendationsForProduct(product.productId, quantityOfTriggerProducts);
      } catch (error) {
        console.error(`加载产品 ${product.productName} 的推荐失败:`, error);
      }
    }
  }
  
  // 更新推荐面板中的选中状态
  const recommendProducts = outboundProducts.value.filter(p => p.isRecommend && p.quantity > 0);
  
  recommendProducts.forEach(recommendProduct => {
    groupedRecommendations.value.forEach(group => {
      const item = group.items.find(i => i.productId === recommendProduct.productId);
      if (item) {
        item.selected = true;
        item.calculatedQuantity = recommendProduct.quantity;
        item.actualQuantity = recommendProduct.quantity;
      }
    });
  });
};

const handleWarehouseChange = async (warehouseId) => {
  if (warehouseId && formData.customerId) {
    await loadAvailableProducts();
  }
  
  if (!isEditMode.value) {
    outboundProducts.value = [];
    groupedRecommendations.value = [];
    recommendationDetails.value = {};
  }
};

const handleCustomerChange = async (customerId) => {
  if (customerId && formData.warehouseId) {
    await loadAvailableProducts();
  }
  
  if (!isEditMode.value) {
    outboundProducts.value = [];
    groupedRecommendations.value = [];
    recommendationDetails.value = {};
  }
};

const handleQuickSearch = () => {
  if (!quickSearch.value.trim()) {
    searchResults.value = [];
    return;
  }
  
  const searchText = quickSearch.value.toLowerCase().trim();
  searchResults.value = availableProducts.value.filter(product => {
    return (
      (product.productName && product.productName.toLowerCase().includes(searchText)) ||
      (product.sku && product.sku.toLowerCase().includes(searchText))
    );
  }).slice(0, 10);
};

const sortProductsByHierarchy = () => {
  const mainProducts = outboundProducts.value.filter(p => p.productLevel === 0);
  const recommendations = outboundProducts.value.filter(p => p.productLevel === 1);
  const packages = outboundProducts.value.filter(p => p.productLevel === 2);
  
  const sortedArray = [];
  
  mainProducts.forEach(mainProduct => {
    sortedArray.push(mainProduct);
    
    const triggerId = mainProduct.productId;
    
    const requiredRecommendations = recommendations.filter(r => 
      r.parentTriggerId === triggerId && r.isRequired && r.quantity > 0
    );
    
    const triggerGroup = groupedRecommendations.value.find(g => g.triggerProductId === triggerId);
    if (triggerGroup) {
      requiredRecommendations.sort((a, b) => {
        const aInPanel = triggerGroup.items.find(i => i.productId === a.productId);
        const bInPanel = triggerGroup.items.find(i => i.productId === b.productId);
        const aIndex = aInPanel ? triggerGroup.items.indexOf(aInPanel) : 999;
        const bIndex = bInPanel ? triggerGroup.items.indexOf(bInPanel) : 999;
        return aIndex - bIndex;
      });
    }
    
    sortedArray.push(...requiredRecommendations);
    
    const productPackages = packages.filter(p => p.parentProductId === triggerId);
    sortedArray.push(...productPackages);
    
    const optionalRecommendations = recommendations.filter(r => 
      r.parentTriggerId === triggerId && 
      !r.isRequired && 
      r.quantity > 0
    );
    
    if (triggerGroup) {
      optionalRecommendations.sort((a, b) => {
        const aInPanel = triggerGroup.items.find(i => i.productId === a.productId);
        const bInPanel = triggerGroup.items.find(i => i.productId === b.productId);
        const aIndex = aInPanel ? triggerGroup.items.indexOf(aInPanel) : 999;
        const bIndex = bInPanel ? triggerGroup.items.indexOf(bInPanel) : 999;
        return aIndex - bIndex;
      });
    }
    
    sortedArray.push(...optionalRecommendations);
  });
  
  const independentRecommendations = recommendations.filter(r => !r.parentTriggerId && r.quantity > 0);
  const independentPackages = packages.filter(p => !p.parentProductId && p.quantity > 0);
  
  sortedArray.push(...independentRecommendations);
  sortedArray.push(...independentPackages);
  
  return sortedArray;
};

const clearSearch = () => {
  quickSearch.value = '';
  searchResults.value = [];
};


// 清理错误关联的推荐商品
const cleanupIncorrectRecommendations = () => {
  // 清理推荐商品中的错误关联
  outboundProducts.value.forEach(product => {
    if (product.isRecommend && product.parentTriggerId) {
      // 检查父触发商品是否存在
      const triggerProduct = outboundProducts.value.find(p => 
        p.productId === product.parentTriggerId
      );
      
      if (!triggerProduct) {
        // 如果父触发商品不存在，清除关联
        product.parentTriggerId = null;
        product.isRecommend = false;
        product.productLevel = 0;
      }
    }
  });
  
  // 清理推荐面板中的错误分组
  const validTriggerIds = outboundProducts.value
    .filter(p => p.productLevel === 0 && p.quantity > 0)
    .map(p => p.productId);
  
  groupedRecommendations.value = groupedRecommendations.value.filter(group => 
    validTriggerIds.includes(group.triggerProductId)
  );
  
  // 重新计算推荐详情
  updateRecommendationDetails();
};


const addProductToTable = async (product) => {
  const existingProduct = outboundProducts.value.find(p => p.productId === product.productId && !p.isPackage);
  if (existingProduct) {
    if (existingProduct.quantity === 0) {
      existingProduct.quantity = 1;
      existingProduct.price = product.originalPrice || 0;
      existingProduct.priceUnitUsd = product.originalPriceUnitUsd || 0;
      existingProduct.isTriggerProduct = true;
      existingProduct.isRecommend = false;
      existingProduct.triggerProductId = null;
      existingProduct.productLevel = 0;
      existingProduct.parentTriggerId = null; // 明确设置为null
      
      existingProduct.extension = existingProduct.extension || {};
      existingProduct.extension.isTriggerProduct = 0;
      existingProduct.extension.isRecommendProduct = 0;
      existingProduct.extension.triggerProductId = null;
      
      // 加载推荐 - 确保只加载当前商品的推荐
      await loadRecommendationsForProduct(product.productId, 1);
      
      // 应用必选推荐 - 确保只应用当前商品的必选推荐
      await applyRequiredRecommendations(product.productId);
      
      await addPackageComponentsBatch(product.productId, 1);
      
      resortProducts();

      // 在添加完成后清理可能存在的错误关联
      cleanupIncorrectRecommendations();
      
      ElMessage.success(`已更新 ${product.productName} 数量为1`);
    } else {
      ElMessage.warning(`${product.productName} 已存在于出库单中`);
    }
    return;
  }
  
  const newProduct = {
    ...product,
    quantity: 1,
    price: product.originalPrice || 0,
    priceUnitUsd: product.originalPriceUnitUsd || 0,
    remark: '',
    batchAllocations: [],
    isTriggerProduct: true,
    isRecommend: false,
    isPackage: false,
    triggerProductId: null,
    parentTriggerId: null, // 明确设置为null
    productLevel: 0,
    extension: {
      productId: product.productId,
      isTriggerProduct: 0,
      isRecommendProduct: 0,
      triggerProductId: null,
      isPackageProduct: 1,
      packageRatio: 0
    },
    historyPrice: null,
    lastQuantity: 0
  };
  
  outboundProducts.value.push(newProduct);
  
  clearSearch();
  
  ElMessage.success(`已添加 ${product.productName} 到出库单`);
  
  // 加载推荐 - 确保只加载当前商品的推荐
  await loadRecommendationsForProduct(product.productId, 1);
  
  // 应用必选推荐 - 确保只应用当前商品的必选推荐
  await applyRequiredRecommendations(product.productId);
  
  await addPackageComponentsBatch(product.productId, 1);
  
  resortProducts();
};

const addPackageComponentsBatch = async (parentProductId, parentQuantity) => {
  try {
    const res = await get(`/api/auth/product/bomDetailOnlyPackageInfo?productId=${parentProductId}`);
    
    if (res && res.list && res.list.length > 0) {
      const parentIndex = outboundProducts.value.findIndex(p => 
        p.productId === parentProductId && !p.isPackage
      );
      
      if (parentIndex === -1) {
        return;
      }
      
      const parentProduct = outboundProducts.value[parentIndex];
      
      parentProduct.packageComponents = [];
      
      const packageItems = [];
      for (const component of res.list) {
        const packageItem = await createPackageComponent(component, parentProductId, parentQuantity);
        if (packageItem) {
          packageItems.push(packageItem);
          parentProduct.packageComponents.push(packageItem.productId);
        }
      }
      
      if (packageItems.length > 0) {
        const existingPackageIndices = [];
        outboundProducts.value.forEach((item, index) => {
          if (item.parentProductId === parentProductId && item.isPackage) {
            existingPackageIndices.push(index);
          }
        });
        
        existingPackageIndices.sort((a, b) => b - a).forEach(index => {
          outboundProducts.value.splice(index, 1);
        });
        
        packageItems.forEach((packageItem, index) => {
          outboundProducts.value.splice(parentIndex + 1 + index, 0, packageItem);
        });
      }
    }
  } catch (error) {
    console.error('批量添加包装件失败:', error);
  }
};

const createPackageComponent = async (component, parentProductId, parentQuantity) => {
  const availableProduct = availableProducts.value.find(p => 
    p.productId === component.componentProductId
  );
  
  if (!availableProduct) {
    return null;
  }
  
  // 修正包装件数量的计算逻辑
  // otherQuantity: 多少个成品用一个包装
  // parentQuantity: 触发产品的数量
  // 包装件数量 = parentQuantity ÷ otherQuantity
  const otherQuantity = component.otherQuantity ? Number(component.otherQuantity) : 1;
  
  let recommendedQuantity = 0;
  if (otherQuantity > 0 && parentQuantity > 0) {
    // 修正：包装件推荐数量 = 成品数量 ÷ 比例值
    recommendedQuantity = 1 / otherQuantity;
  }
  
  // 包装件总数量 = 成品数量 × 包装件推荐数量
  const packageQuantity = recommendedQuantity > 0 ? 
    (parentQuantity * recommendedQuantity) : 
    0;
  
  return {
    ...availableProduct,
    productId: component.componentProductId,
    productName: component.componentProductName || availableProduct.productName,
    sku: component.componentProductSku || availableProduct.sku,
    spec: component.componentProductSpec || availableProduct.spec,
    color: component.componentProductColor || availableProduct.color,
    quantity: packageQuantity,
    price: availableProduct.originalPrice || 0,
    priceUnitUsd: availableProduct.originalPriceUnitUsd || 0,
    remark: `包装件 - ${component.remark || ''}`,
    batchAllocations: [],
    isTriggerProduct: false,
    isRecommend: false,
    isPackage: true,
    productLevel: 2,
    parentProductId: parentProductId,
    packageRatio: otherQuantity,
    recommendedQuantity: recommendedQuantity, // 包装件推荐数量（比例值）
    triggerProductId: parentProductId,
    extension: {
      productId: component.componentProductId,
      isTriggerProduct: 1,
      isRecommendProduct: 2,
      parentProductId: parentProductId,
      triggerProductId: parentProductId,
      isPackageProduct: 0,
      packageRatio: otherQuantity,
      recommendedQuantity: recommendedQuantity
    },
    historyPrice: null,
    lastQuantity: packageQuantity,
    availableQuantity: availableProduct.availableQuantity || 0,
    unitName: availableProduct.unitName || '个'
  };
};

const calculateRecommendedQuantity = (recommendation, triggerProductId, triggerQuantity) => {
  console.log(`calculateRecommendedQuantity: 
    triggerProductId=${triggerProductId}, 
    product=${recommendation.productName}, 
    trigger=${triggerProductId}, 
    quantity=${triggerQuantity},
    type=${recommendation.quantityType},
    value=${recommendation.quantityValue}`);
  
  let quantity = triggerQuantity;
  
  // 如果triggerQuantity未提供，尝试从产品中获取
  if (quantity === undefined) {
    const product = outboundProducts.value.find(p => p.productId === triggerProductId);
    quantity = product ? product.quantity : 0;
  }
  
  if (quantity <= 0) return 0;
  
  if (recommendation.quantityType === 1) {
    // 固定数量
    return Number(recommendation.quantityValue) || 0;
  } else {
    // 比例计算
    const ratio = Number(recommendation.quantityValue) || 0;
    const calculated = quantity * ratio;
    const rounded = Math.round(calculated);
    
    console.log(`Ratio calculation: ${quantity} × ${ratio} = ${rounded}`);
    return rounded;
  }
};

const getTriggerProductQuantity = (triggerProductId) => {
  const product = outboundProducts.value.find(p => p.productId === triggerProductId);
  return product ? product.quantity : 0;
};

const refreshTriggerRecommendations = async (productId) => {
  const product = outboundProducts.value.find(p => p.productId === productId);
  if (product && product.quantity > 0) {
    await loadRecommendationsForProduct(productId, product.quantity);
    ElMessage.success('已刷新推荐');
  } else {
    ElMessage.warning('请先设置触发产品的数量');
  }
};

const refreshRecommendations = async (triggerProductId) => {
  const product = outboundProducts.value.find(p => p.productId === triggerProductId);
  if (product && product.quantity > 0) {
    await loadRecommendationsForProduct(triggerProductId, product.quantity);
    ElMessage.success('已重新获取推荐');
  } else {
    ElMessage.warning('触发产品数量为0，无法获取推荐');
  }
};


const loadRecommendationsForProduct = async (productId, quantity) => {
  console.log(`loadRecommendationsForProduct: product=${productId}, quantity=${quantity}`);
  
  if (!formData.customerId || !formData.warehouseId) {
    console.warn('Missing customer or warehouse info');
    return;
  }
  
  try {
    const requestData = {
      customerId: formData.customerId,
      warehouseId: formData.warehouseId,
      productId: productId,
      quantity: quantity,
      applyScene: 1
    };
    
    console.log('Requesting recommendations with:', requestData);
    
    const res = await post('/api/auth/recommend/queryRuleItems', requestData);
    
    if (res && res.recommendations && res.recommendations.length > 0) {
      console.log(`Received ${res.recommendations.length} recommendations for product ${productId}`);
      
      const recommendations = res.recommendations.map(item => {
        const productInfo = availableProducts.value.find(p => p.productId === item.productId);
        const availableQuantity = productInfo ? productInfo.availableQuantity : 0;
        
        // 创建唯一标识
        const uniqueId = `${item.productId}_${productId}`;
        
        // 使用唯一标识查找已存在的推荐商品
        const existingProduct = outboundProducts.value.find(p => 
          p.recommendationUniqueId === uniqueId
        );
        
        const isSelected = !!existingProduct;
        const actualQuantity = existingProduct ? existingProduct.quantity : 0;
        
        // 重新计算推荐数量
        const calculatedQuantity = calculateRecommendedQuantity({
          quantityType: item.quantityType,
          quantityValue: item.quantityValue
        }, productId, quantity);
        
        console.log(`Recommendation ${item.productName} for trigger ${productId}: 
          selected=${isSelected}, 
          calculated=${calculatedQuantity}, 
          actual=${actualQuantity},
          uniqueId=${uniqueId}`);
        
        return {
          productId: item.productId,
          productName: item.productName,
          sku: item.sku,
          spec: item.spec || '-',
          color: item.color || '-',
          quantityType: item.quantityType || 1,
          quantityValue: item.quantityValue ? Number(item.quantityValue) : 1,
          isRequired: item.isRequired || false,
          confidence: item.confidence ? Number(item.confidence) : 1,
          remark: item.remark || '',
          selected: isSelected,
          availableQuantity: availableQuantity,
          calculatedQuantity: calculatedQuantity,
          triggerProductId: productId,
          actualQuantity: actualQuantity,
          isCurrentTriggerRecommendation: true,
          uniqueId: uniqueId  // 使用一致的唯一标识
        };
      });
      
      if (recommendations.length > 0) {
        // 移除旧的推荐组（如果有）
        const existingGroupIndex = groupedRecommendations.value.findIndex(
          g => g.triggerProductId === productId
        );
        
        if (existingGroupIndex >= 0) {
          console.log(`Updating existing recommendation group for trigger ${productId}`);
          groupedRecommendations.value.splice(existingGroupIndex, 1);
        }
        
        console.log(`Creating new recommendation group for trigger ${productId}`);
        groupedRecommendations.value.push({
          triggerProductId: productId,
          items: recommendations,
          hasValidItems: recommendations.some(item => item.availableQuantity > 0),
          triggerProductName: getProductName(productId),
          triggerProductQuantity: quantity
        });
        
        // 更新推荐详情
        updateRecommendationDetails();
      }
    } else {
      console.log(`No recommendations received for product ${productId}`);
      
      // 清除该触发商品的推荐组
      const existingGroupIndex = groupedRecommendations.value.findIndex(
        g => g.triggerProductId === productId
      );
      if (existingGroupIndex >= 0) {
        groupedRecommendations.value.splice(existingGroupIndex, 1);
      }
    }
  } catch (error) {
    console.error(`加载产品 ${productId} 的推荐失败:`, error);
    if (!isEditMode.value) {
      ElMessage.error(`获取推荐失败: ${error.message || '未知错误'}`);
    }
  }
};

const updateRecommendationDetails = () => {
  console.log('开始更新推荐详情...');
  recommendationDetails.value = {};
  
  // 处理普通推荐商品
  groupedRecommendations.value.forEach(group => {
    group.items.forEach(item => {
      if (!recommendationDetails.value[item.productId]) {
        recommendationDetails.value[item.productId] = [];
      }
      
      // 计算推荐数量
      const recommendedQuantity = calculateRecommendedQuantity(item, group.triggerProductId);
      
      // 检查是否已存在相同的触发商品记录
      const existingIndex = recommendationDetails.value[item.productId].findIndex(
        detail => detail.triggerProductId === group.triggerProductId
      );
      
      if (existingIndex >= 0) {
        // 更新现有记录
        recommendationDetails.value[item.productId][existingIndex] = {
          triggerProductId: group.triggerProductId,
          recommendedQuantity: recommendedQuantity,
          productName: item.productName,
          isRequired: item.isRequired
        };
      } else {
        // 添加新记录
        recommendationDetails.value[item.productId].push({
          triggerProductId: group.triggerProductId,
          recommendedQuantity: recommendedQuantity,
          productName: item.productName,
          isRequired: item.isRequired
        });
      }
    });
  });
  
  // 处理包装件的推荐详情
  outboundProducts.value.forEach(product => {
    if (product.isPackage && product.packageRatio) {
      if (!recommendationDetails.value[product.productId]) {
        recommendationDetails.value[product.productId] = [];
      }
      
      // 包装件的推荐逻辑是固定的，基于包装比例
      const recommendedQuantity = product.recommendedQuantity || (product.packageRatio > 0 ? 1 / product.packageRatio : 0);
      
      recommendationDetails.value[product.productId].push({
        triggerProductId: product.parentProductId,
        recommendedQuantity: recommendedQuantity,
        productName: product.productName,
        isRequired: true,
        isPackage: true,
        packageRatio: product.packageRatio,
        calculatedQuantity: product.quantity || 0
      });
    }
  });
  
  console.log('推荐详情更新完成:', recommendationDetails.value);
};

// 获取推荐详情 - 只返回与指定产品相关的推荐详情
const getRecommendationDetails = (productId, triggerProductId = null) => {
  const allDetails = recommendationDetails.value[productId] || [];
  console.log(`获取推荐详情 - 产品 ${productId} 的所有详情:`, allDetails);
  
  // 如果提供了 triggerProductId，则返回该触发商品的特定推荐
  if (triggerProductId) {
    const specificDetails = allDetails.filter(detail => 
      detail.triggerProductId === triggerProductId
    );
    console.log(`获取特定触发商品 ${triggerProductId} 的推荐详情: ${specificDetails.length} 条记录`);
    return specificDetails;
  }
  
  // 找到产品信息
  const product = outboundProducts.value.find(p => p.productId === productId);
  
  if (product) {
    console.log(`获取推荐详情 - 产品信息:`, {
      productId,
      isRecommend: product.isRecommend,
      isTriggerProduct: product.isTriggerProduct,
      parentTriggerId: product.parentTriggerId,
      triggerProductId: product.triggerProductId
    });
    
    // 如果是推荐商品，并且有具体的触发商品ID，只返回该触发商品的推荐
    if (product.isRecommend && product.parentTriggerId) {
      const filteredDetails = allDetails.filter(detail => 
        detail.triggerProductId === product.parentTriggerId
      );
      console.log(`推荐商品 - 只显示来自父触发商品 ${product.parentTriggerId} 的推荐: ${filteredDetails.length} 条记录`);
      return filteredDetails;
    }
    
    // 如果是触发商品，显示所有相关的推荐
    if (product.isTriggerProduct) {
      console.log(`触发商品 - 显示所有关联推荐`);
      return allDetails;
    }
  }
  
  console.log(`返回所有详情: ${allDetails.length} 条记录`);
  return allDetails;
};


const getTotalRecommendedQuantity = (productId) => {
  const product = outboundProducts.value.find(p => p.productId === productId);
  if (!product) {
    return 0;
  }
  
  // 如果是包装件，返回包装件的推荐数量
  if (product.isPackage && product.recommendedQuantity) {
    return product.recommendedQuantity;
  }
  
  // 其他情况从推荐详情中获取
  const details = recommendationDetails.value[productId];
  if (!details || details.length === 0) {
    return 0;
  }
  
  // 计算所有推荐的总和
  return details.reduce((sum, detail) => {
    return sum + (detail.recommendedQuantity || 0);
  }, 0);
};


const applyRequiredRecommendations = async (triggerProductId) => {
  console.log(`applyRequiredRecommendations: trigger=${triggerProductId}`);
  
  const group = groupedRecommendations.value.find(g => g.triggerProductId === triggerProductId);
  if (!group) {
    console.warn(`No recommendation group found for ${triggerProductId}`);
    return;
  }
  
  const triggerProduct = outboundProducts.value.find(p => 
    p.productId === triggerProductId && p.productLevel === 0
  );
  
  if (!triggerProduct) {
    console.warn(`No trigger product found: ${triggerProductId}`);
    return;
  }
  
  const triggerQuantity = triggerProduct ? triggerProduct.quantity : 0;
  console.log(`Trigger product quantity: ${triggerQuantity}`);
  
  if (triggerQuantity <= 0) {
    console.log('Trigger product quantity is 0, skipping recommendations');
    return;
  }
  
  // 只处理当前触发商品的必选推荐
  const requiredRecommendations = group.items.filter(item => 
    item.isRequired && 
    item.availableQuantity > 0 &&
    item.triggerProductId === triggerProductId
  );
  
  console.log(`Found ${requiredRecommendations.length} required recommendations for ${triggerProductId}`);
  
  let addedCount = 0;
  let updatedCount = 0;
  
  for (const item of requiredRecommendations) {
    const calculatedQuantity = calculateRecommendedQuantity(item, triggerProductId, triggerQuantity);
    const actualQuantity = Math.min(calculatedQuantity, item.availableQuantity);
    
    console.log(`Required recommendation ${item.productName}: 
      calculated=${calculatedQuantity}, 
      available=${item.availableQuantity}, 
      actual=${actualQuantity}`);
    
    if (actualQuantity <= 0) {
      console.log(`Skipping ${item.productName}, actual quantity is 0`);
      continue;
    }
    
    // 查找已存在的推荐商品 - 确保来自同一触发商品
    // const existingProduct = outboundProducts.value.find(p => 
    //   p.productId === item.productId && 
    //   p.isRecommend &&
    //   p.parentTriggerId === triggerProductId  // 关键：确保来自同一触发商品
    // );

    const uniqueId = `${item.productId}_${triggerProductId}`;
    const existingProduct = outboundProducts.value.find(p => 
      p.recommendationUniqueId === uniqueId
    );

    console.log(`唯一标识: ${uniqueId}`);
    console.log(`查找结果: `,existingProduct);  
    
    if (!existingProduct) {
      const product = availableProducts.value.find(p => p.productId === item.productId);
      if (product) {
        const newRecommendation = {
          ...product,
          quantity: actualQuantity,
          price: product.originalPrice || 0,
          priceUnitUsd: product.originalPriceUnitUsd || 0,
          batchAllocations: [],
          isTriggerProduct: false,
          isRecommend: true,
          isPackage: false,
          productLevel: 1,
          parentTriggerId: triggerProductId,  // 正确设置父触发商品ID
          triggerProductId: triggerProductId, // 也设置 triggerProductId
          isRequired: item.isRequired,
          recommendationUniqueId: item.uniqueId,
          extension: {
            productId: item.productId,
            isTriggerProduct: 1,
            isRecommendProduct: 1,
            triggerProductId: triggerProductId,  // 正确设置触发商品ID
            isPackageProduct: 1
          },
          historyPrice: null,
          lastQuantity: 0
        };
        
        outboundProducts.value.push(newRecommendation);
        addedCount++;
        
        console.log(`Added new required recommendation: ${item.productName} (${actualQuantity}) for trigger ${triggerProductId}`);
      }
    } else if (existingProduct.isRecommend) {
      const newQuantity = Math.max(existingProduct.quantity, actualQuantity);
      
      if (newQuantity !== existingProduct.quantity) {
        existingProduct.quantity = newQuantity;
        updatedCount++;
        
        console.log(`Updated existing recommendation: ${item.productName} ${existingProduct.quantity} -> ${newQuantity} for trigger ${triggerProductId}`);
      }
      
      existingProduct.isRequired = item.isRequired;
      // 确保关联关系正确
      existingProduct.parentTriggerId = triggerProductId;
      existingProduct.triggerProductId = triggerProductId;
    }
    
    // 更新推荐面板中的选择状态
    item.selected = true;
    item.calculatedQuantity = actualQuantity;
    item.actualQuantity = actualQuantity;
  }
  
  if (addedCount > 0 || updatedCount > 0) {
    const message = [];
    if (addedCount > 0) message.push(`已自动添加 ${addedCount} 个必选推荐商品`);
    if (updatedCount > 0) message.push(`已更新 ${updatedCount} 个推荐商品数量`);
    
    const fullMessage = message.join('，');
    console.log(fullMessage);
    ElMessage.success(fullMessage);
    
    // 更新推荐详情
    updateRecommendationDetails();
    
    resortProducts();
  } else {
    console.log('No required recommendations to add or update');
  }
};


const addRecommendationToOrder = (recommendation) => {
  const product = availableProducts.value.find(p => p.productId === recommendation.productId);
  if (!product) {
    ElMessage.warning(`产品 ${recommendation.productName} 不在当前仓库中`);
    return;
  }
  
  const quantity = recommendation.calculatedQuantity || 1;
  const actualQuantity = Math.min(quantity, product.availableQuantity);
  
  if (actualQuantity <= 0) {
    ElMessage.warning(`${recommendation.productName} 库存不足，无法添加`);
    return;
  }
  
  // 查找触发商品是否存在
  const triggerProduct = outboundProducts.value.find(p => 
    p.productId === recommendation.triggerProductId && p.productLevel === 0
  );
  
  if (!triggerProduct) {
    ElMessage.warning('未找到对应的触发产品');
    return;
  }
  
  // 创建唯一标识：productId_triggerProductId
  const uniqueId = `${recommendation.productId}_${recommendation.triggerProductId}`;
  
  // 关键：查找已存在的推荐商品时，要同时匹配产品ID和触发商品ID
  const existingProduct = outboundProducts.value.find(p => 
    p.recommendationUniqueId === uniqueId
  );
  
  if (existingProduct) {
    existingProduct.quantity = Math.max(existingProduct.quantity, actualQuantity);
    existingProduct.isRequired = recommendation.isRequired;
    existingProduct.parentTriggerId = recommendation.triggerProductId;
    existingProduct.triggerProductId = recommendation.triggerProductId;
    
    ElMessage.success(`已更新 ${recommendation.productName} 数量为 ${existingProduct.quantity}`);
  } else {
    const newProduct = {
      ...product,
      quantity: actualQuantity,
      price: product.originalPrice || 0,
      priceUnitUsd: product.originalPriceUnitUsd || 0,
      batchAllocations: [],
      isTriggerProduct: false,
      isRecommend: true,
      isPackage: false,
      productLevel: 1,
      parentTriggerId: recommendation.triggerProductId,
      triggerProductId: recommendation.triggerProductId,
      isRequired: recommendation.isRequired,
      recommendationUniqueId: uniqueId, // 使用唯一标识
      extension: {
        productId: recommendation.productId,
        isTriggerProduct: 1,
        isRecommendProduct: 1,
        triggerProductId: recommendation.triggerProductId,
        isPackageProduct: 1
      },
      historyPrice: null
    };
    
    outboundProducts.value.push(newProduct);
    
    ElMessage.success(`已添加 ${recommendation.productName} 到出库单`);
  }
  
  // 更新推荐面板中的选中状态
  recommendation.selected = true;
  recommendation.actualQuantity = actualQuantity;
  
  // 更新推荐详情
  updateRecommendationDetails();
  
  // 重新排序
  resortProducts();
};


const handleRecommendationToggle = (recommendation) => {
  if (recommendation.selected) {
    addRecommendationToOrder(recommendation);
  } else {
    // 使用唯一标识查找并移除
    const uniqueId = `${recommendation.productId}_${recommendation.triggerProductId}`;
    const existingProduct = outboundProducts.value.find(p => 
      p.recommendationUniqueId === uniqueId
    );
    
    if (existingProduct) {
      const index = outboundProducts.value.findIndex(p => 
        p.recommendationUniqueId === uniqueId
      );
      if (index > -1) {
        outboundProducts.value.splice(index, 1);
      }
    }
    
    // 更新推荐面板中的选中状态
    updateRecommendationSelection(recommendation.productId, recommendation.triggerProductId, false);
  }
};

const removeRecommendationsForTrigger = (triggerProductId, deleteRecommendProducts = true) => {
  const index = groupedRecommendations.value.findIndex(g => g.triggerProductId === triggerProductId);
  if (index >= 0) {
    if (deleteRecommendProducts) {
      const indices = [];
      outboundProducts.value.forEach((item, idx) => {
        if (item.isRecommend && item.triggerProductId === triggerProductId) {
          indices.unshift(idx);
        }
      });
      
      indices.forEach(idx => {
        outboundProducts.value.splice(idx, 1);
      });
    }
    
    groupedRecommendations.value.splice(index, 1);
  }
  
  updateRecommendationDetails();
};


const updateRecommendationSelection = (productId, triggerProductId, isSelected) => {
  const group = groupedRecommendations.value.find(g => g.triggerProductId === triggerProductId);
  if (group) {
    const item = group.items.find(i => i.productId === productId);
    if (item) {
      item.selected = isSelected;
      
      if (isViewMode.value || isEditMode.value) {
        const existingProduct = outboundProducts.value.find(p => 
          p.productId === productId && 
          p.parentTriggerId === triggerProductId &&
          p.quantity > 0 && 
          p.isRecommend
        );
        if (existingProduct) {
          item.actualQuantity = existingProduct.quantity;
        } else if (!isSelected) {
          item.actualQuantity = 0;
        }
      }
    }
  }
};

const removeProductFromTableByProductId = (productId, triggerProductId) => {
  const uniqueId = `${productId}_${triggerProductId}`;
  
  const index = outboundProducts.value.findIndex(p => 
    p.recommendationUniqueId === uniqueId
  );
  
  if (index >= 0) {
    outboundProducts.value.splice(index, 1);
    
    // 只删除与该触发商品相关的推荐详情
    if (recommendationDetails.value[productId]) {
      recommendationDetails.value[productId] = recommendationDetails.value[productId]
        .filter(detail => detail.triggerProductId !== triggerProductId);
      
      if (recommendationDetails.value[productId].length === 0) {
        delete recommendationDetails.value[productId];
      }
    }
    
    updateRecommendationDetails();
  }
  
  updateRecommendationSelection(productId, triggerProductId, false);
};

const applyAllRecommendations = () => {
  let addedCount = 0;
  
  groupedRecommendations.value.forEach(group => {
    group.items.forEach(item => {
      if (!item.selected && item.availableQuantity > 0) {
        addRecommendationToOrder(item);
        addedCount++;
      }
    });
  });
  
  if (addedCount > 0) {
    ElMessage.success(`成功添加 ${addedCount} 个推荐产品`);
  } else {
    ElMessage.warning('没有可添加的推荐产品');
  }
};

const applyGroupRecommendations = (group) => {
  let addedCount = 0;
  
  group.items.forEach(item => {
    if (!item.selected && item.availableQuantity > 0) {
      addRecommendationToOrder(item);
      addedCount++;
    }
  });
  
  if (addedCount > 0) {
    ElMessage.success(`成功添加 ${addedCount} 个推荐产品`);
  } else {
    ElMessage.warning('本组没有可添加的推荐产品');
  }
};

const getProductFullName = (productId) => {
  const product = outboundProducts.value.find(p => p.productId === productId) || 
                  availableProducts.value.find(p => p.productId === productId);
  if (!product) return '未知产品';
  
  let name = product.productName || '';
  if (product.spec && product.spec !== '-') {
    name += ` (${product.spec})`;
  } else {
    name += ' - 无规格';
  }
  if (product.color && product.color !== '-') {
    name += ` [${product.color}]`;
  } else {
    name += ' - 无颜色';
  }
  return name;
};

const getTruncatedProductName = (productId) => {
  const fullName = getProductFullName(productId);
  if (fullName.length > 25) {
    return fullName.substring(0, 25) + '...';
  }
  return fullName;
};

const getProductName = (productId) => {
  const product = outboundProducts.value.find(p => p.productId === productId) || 
                  availableProducts.value.find(p => p.productId === productId);
  
  if (!product) {
    return '未知产品';
  }
  
  let name = product.productName || '';
  if (product.spec && product.spec !== '-') {
    name += ` - ${product.spec}`;
  }
  if (product.color && product.color !== '-') {
    name += ` - ${product.color}`;
  }
  
  return name;
};

const getProductQuantity = (productId) => {
  const product = outboundProducts.value.find(p => p.productId === productId);
  return product ? product.quantity : 0;
};

const closeRecommendations = () => {
  ElMessageBox.confirm(
    '关闭推荐面板将不再显示推荐信息，但已添加的推荐商品会保留。确定要关闭吗？',
    '确认关闭',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    groupedRecommendations.value = [];
    recommendationDetails.value = {};
    ElMessage.success('已关闭推荐面板');
  });
};

const filterSelectorProducts = () => {
  if (!selectorSearch.value.trim()) {
    filteredSelectorProducts.value = [...availableProducts.value];
    return;
  }
  
  const searchText = selectorSearch.value.toLowerCase().trim();
  filteredSelectorProducts.value = availableProducts.value.filter(product => {
    return (
      (product.productName && product.productName.toLowerCase().includes(searchText)) ||
      (product.sku && product.sku.toLowerCase().includes(searchText)) ||
      (product.spec && product.spec.toLowerCase().includes(searchText))
    );
  });
};

const handleProductSelection = (selection) => {
  selectedProductsForSelector.value = selection;
};

const clearSelectorSelection = () => {
  selectedProductsForSelector.value = [];
};

const handleProductSelectorClosed = () => {
  selectorSearch.value = '';
  selectedProductsForSelector.value = [];
  filteredSelectorProducts.value = [...availableProducts.value];
};

const addSelectedProducts = async () => {
  if (selectedProductsForSelector.value.length === 0) {
    ElMessage.warning('请先选择产品');
    return;
  }
  
  let addedCount = 0;
  const addedProducts = [];
  
  for (const product of selectedProductsForSelector.value) {
    const existingProduct = outboundProducts.value.find(p => 
      p.productId === product.productId && !p.isPackage
    );
    
    if (!existingProduct) {
      const newProduct = {
        ...product,
        quantity: 1,
        price: product.originalPrice || 0,
        priceUnitUsd: product.originalPriceUnitUsd || 0,
        remark: '',
        batchAllocations: [],
        isTriggerProduct: true,
        isRecommend: false,
        productLevel: 0,
        historyPrice: null,
        lastQuantity: 0,
        triggerProductId: null,
        isPackage: false,
        parentProductId: null,
        packageComponents: []
      };
      
      outboundProducts.value.push(newProduct);
      addedProducts.push(newProduct);
      addedCount++;
      
      await loadRecommendationsForProduct(product.productId, 1);
      
      await applyRequiredRecommendations(product.productId);
      
    } else if (existingProduct.quantity === 0) {
      existingProduct.quantity = 1;
      existingProduct.price = product.originalPrice || 0;
      existingProduct.priceUnitUsd = product.originalPriceUnitUsd || 0;
      existingProduct.isTriggerProduct = true;
      existingProduct.isRecommend = false;
      existingProduct.triggerProductId = null;
      existingProduct.productLevel = 0;
      addedProducts.push(existingProduct);
      addedCount++;
      
      await loadRecommendationsForProduct(product.productId, 1);
      
      await applyRequiredRecommendations(product.productId);
    }
  }
  
  for (const product of addedProducts) {
    await addPackageComponentsBatch(product.productId, product.quantity);
  }
  
  showProductSelector.value = false;
  
  resortProducts();
  
  if (addedCount > 0) {
    ElMessage.success(`成功添加 ${addedCount} 个产品到出库单`);
  }
};

const removeProductFromTable = (row, index) => {
  ElMessageBox.confirm(
    `确定要删除 ${row.productName} 吗？`,
    '确认删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    if (row.productLevel === 0) {
      const indicesToRemove = [];
      outboundProducts.value.forEach((item, idx) => {
        if ((item.parentProductId === row.productId && item.productLevel === 2) ||
            (item.parentTriggerId === row.productId && item.productLevel === 1)) {
          indicesToRemove.unshift(idx);
        }
      });
      
      indicesToRemove.forEach(idx => {
        outboundProducts.value.splice(idx, 1);
      });
      
      removeRecommendationsForTrigger(row.productId, false);
      
    } else if (row.productLevel === 1) {
      groupedRecommendations.value.forEach(group => {
        const itemIndex = group.items.findIndex(i => 
          i.uniqueId === row.recommendationUniqueId
        );
        if (itemIndex >= 0) {
          group.items.splice(itemIndex, 1);
        }
      });
    }
    
    outboundProducts.value.splice(index, 1);
    
    resortProducts();
    
    ElMessage.success('删除成功');
  });
};

const handleSelectionChange = (selection) => {
  selectedProducts.value = selection;
};

const handleRowClick = (row) => {
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

const checkAutoAllocationEligibility = () => {
  const productsWithQuantity = outboundProducts.value.filter(item => item.quantity > 0);
  
  if (productsWithQuantity.length === 0) {
    ElMessage.warning('请先设置产品的出库数量');
    return false;
  }
  
  const productsWithInsufficientStock = productsWithQuantity.filter(
    item => item.quantity > (item.availableQuantity || 0)
  );
  
  if (productsWithInsufficientStock.length > 0) {
    const productNames = productsWithInsufficientStock.map(item => item.productName).join(', ');
    ElMessage.warning(`以下产品库存不足: ${productNames}`);
    return false;
  }
  
  return true;
};

const handleAutoAllocateAll = async () => {
  try {
    if (!checkAutoAllocationEligibility()) {
      return;
    }
    
    if (isEditMode.value) {
      const confirmed = await ElMessageBox.confirm(
        '自动分配会覆盖当前的批次分配数据，确定要继续吗？',
        '提示',
        {
          type: 'warning',
          confirmButtonText: '确定',
          cancelButtonText: '取消'
        }
      );
      
      if (!confirmed) {
        return;
      }
    }
    
    autoAllocating.value = true;
    isAllocating.value = true;
    allocationProgress.value = 0;
    currentAllocationIndex.value = 0;
    autoAllocationResult.value = null;
    
    const productsToAllocate = outboundProducts.value.filter(item => 
      item.quantity > 0 && 
      (!item.batchAllocations || item.batchAllocations.length === 0)
    );
    
    totalAllocationCount.value = productsToAllocate.length;
    
    if (totalAllocationCount.value === 0) {
      ElMessage.info('所有产品已经分配完成');
      autoAllocating.value = false;
      isAllocating.value = false;
      return;
    }
    
    for (let i = 0; i < productsToAllocate.length; i++) {
      const product = productsToAllocate[i];
      currentAllocationIndex.value = i + 1;
      allocationProgress.value = Math.round((currentAllocationIndex.value / totalAllocationCount.value) * 100);
      
      try {
        await autoAllocateProduct(product);
      } catch (error) {
        ElMessage.error(`产品 ${product.productName} 分配失败: ${error.message || '未知错误'}`);
      }
      
      if (i < productsToAllocate.length - 1) {
        await new Promise(resolve => setTimeout(resolve, 300));
      }
    }
    
    autoAllocating.value = false;
    isAllocating.value = false;
    allocationProgress.value = 100;
    
    const allocatedProducts = outboundProducts.value.filter(item => 
      item.quantity > 0 && item.batchAllocations && item.batchAllocations.length > 0
    );
    
    const allocatedCount = allocatedProducts.reduce((sum, item) => 
      sum + (item.batchAllocations?.length || 0), 0
    );
    
    autoAllocationResult.value = {
      success: true,
      message: `自动分配完成，成功为 ${allocatedProducts.length} 个产品分配了 ${allocatedCount} 个批次`
    };
    
    ElMessage.success('自动分配完成');
    
  } catch (error) {
    console.error('自动分配失败:', error);
    autoAllocating.value = false;
    isAllocating.value = false;
    
    autoAllocationResult.value = {
      success: false,
      message: `自动分配失败: ${error.message || '未知错误'}`
    };
    
    ElMessage.error('自动分配失败');
  }
};

const autoAllocateProduct = async (product) => {
  if (!product.productId || !formData.warehouseId) {
    throw new Error('产品信息或仓库信息不完整');
  }
  
  await loadBatchInfoForProduct(product.productId, formData.warehouseId, product);
  
  if (!product.availableBatches || product.availableBatches.length === 0) {
    throw new Error('没有找到可用批次');
  }
  
  const targetQuantity = product.quantity;
  let remainingQuantity = targetQuantity;
  const allocations = [];
  
  const sortedBatches = [...product.availableBatches].sort((a, b) => {
    if (a.productionDate && b.productionDate) {
      return new Date(a.productionDate) - new Date(b.productionDate);
    }
    if (a.createdAt && b.createdAt) {
      return new Date(a.createdAt) - new Date(b.createdAt);
    }
    return 0;
  });
  
  for (const batch of sortedBatches) {
    if (remainingQuantity <= 0) break;
    
    const batchTotalAvailable = batch.quantity || 0;
    if (batchTotalAvailable <= 0) continue;
    
    if (batch.shelfList && batch.shelfList.length > 0) {
      const sortedShelves = [...batch.shelfList].sort((a, b) => b.quantity - a.quantity);
      
      for (const shelf of sortedShelves) {
        if (remainingQuantity <= 0) break;
        
        const shelfAvailable = shelf.quantity || 0;
        if (shelfAvailable <= 0) continue;
        
        const allocateQuantity = Math.min(shelfAvailable, remainingQuantity);
        
        allocations.push({
          batchNo: batch.batchNo,
          shelfId: shelf.shelfId,
          shelfName: shelf.shelfName || `货架${shelf.shelfId}`,
          quantity: allocateQuantity,
          price: product.price || 0
        });
        
        remainingQuantity -= allocateQuantity;
      }
    } else {
      const allocateQuantity = Math.min(batchTotalAvailable, remainingQuantity);
      
      allocations.push({
        batchNo: batch.batchNo,
        shelfId: null,
        shelfName: '默认货架',
        quantity: allocateQuantity,
        price: product.price || 0
      });
      
      remainingQuantity -= allocateQuantity;
    }
  }
  
  if (remainingQuantity > 0) {
    throw new Error(`库存不足，仍有 ${remainingQuantity} 个无法分配`);
  }
  
  product.batchAllocations = allocations;
  
  return allocations;
};

const handleBatchDialogClosed = () => {
  showAllocationStrategy.value = false;
  allocationStrategy.value = 'FIFO';
};

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

const applyAllocationStrategy = () => {
  applyingStrategy.value = true;
  
  batchDialog.batches.forEach(batch => {
    if (batch.shelfList) {
      batch.shelfList.forEach(shelf => {
        shelf.allocated = 0;
      });
    }
  });
  
  let remaining = batchDialog.totalQuantity;
  
  let sortedBatches = [...batchDialog.batches];
  
  switch (allocationStrategy.value) {
    case 'FIFO':
      sortedBatches.sort((a, b) => {
        if (!a.productionDate && !b.productionDate) return 0;
        if (!a.productionDate) return 1;
        if (!b.productionDate) return -1;
        return new Date(a.productionDate) - new Date(b.productionDate);
      });
      break;
      
    case 'NEAR_EXPIRE':
      sortedBatches.sort((a, b) => {
        if (!a.expiryDate && !b.expiryDate) return 0;
        if (!a.expiryDate) return 1;
        if (!b.expiryDate) return -1;
        return new Date(a.expiryDate) - new Date(b.expiryDate);
      });
      break;
      
    case 'MIN_SHELF':
      sortedBatches.sort((a, b) => {
        const aShelves = a.shelfList ? a.shelfList.length : 0;
        const bShelves = b.shelfList ? b.shelfList.length : 0;
        return aShelves - bShelves;
      });
      break;
  }
  
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
  
  batchDialog.batches.forEach(batch => {
    if (batch.shelfList) {
      batch.shelfList.forEach(shelf => {
        shelf.allocated = 0;
      });
    }
  });
  
  updateBatchDialogCalculations();
  remaining = batchDialog.remainingQuantity;
  
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
  } else {
    const mockAllocations = await calculateAutoAllocation(row.quantity, batchDialog.batches);
    mockAllocations.forEach(mockAlloc => {
      const batch = batchDialog.batches.find(b => b.batchNo === mockAlloc.batchNo);
      if (batch && batch.shelfList) {
        const shelf = batch.shelfList.find(s => s.shelfId === mockAlloc.shelfId);
        if (shelf) {
          shelf.allocated = mockAlloc.quantity;
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

const calculateAutoAllocation = async (targetQuantity, batches) => {
  let remainingQuantity = targetQuantity;
  const allocations = [];
  
  const sortedBatches = [...batches].sort((a, b) => {
    if (a.productionDate && b.productionDate) {
      return new Date(a.productionDate) - new Date(b.productionDate);
    }
    return 0;
  });
  
  for (const batch of sortedBatches) {
    if (remainingQuantity <= 0) break;
    
    const batchTotalAvailable = batch.quantity || 0;
    if (batchTotalAvailable <= 0) continue;
    
    if (batch.shelfList && batch.shelfList.length > 0) {
      const sortedShelves = [...batch.shelfList].sort((a, b) => b.quantity - a.quantity);
      
      for (const shelf of sortedShelves) {
        if (remainingQuantity <= 0) break;
        
        const shelfAvailable = shelf.quantity || 0;
        if (shelfAvailable <= 0) continue;
        
        const allocateQuantity = Math.min(shelfAvailable, remainingQuantity);
        
        allocations.push({
          batchNo: batch.batchNo,
          shelfId: shelf.shelfId,
          quantity: allocateQuantity
        });
        
        remainingQuantity -= allocateQuantity;
      }
    } else {
      const allocateQuantity = Math.min(batchTotalAvailable, remainingQuantity);
      
      allocations.push({
        batchNo: batch.batchNo,
        shelfId: null,
        quantity: allocateQuantity
      });
      
      remainingQuantity -= allocateQuantity;
    }
  }
  
  return allocations;
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

const checkAllocationStatus = () => {
  const unallocatedProducts = outboundProducts.value.filter(item => {
    if (item.quantity <= 0) return false;
    const allocatedQuantity = item.batchAllocations 
      ? item.batchAllocations.reduce((sum, alloc) => sum + (alloc.quantity || 0), 0)
      : 0;
    return (item.quantity || 0) !== allocatedQuantity;
  });
  
  if (unallocatedProducts.length > 0) {
    const productNames = unallocatedProducts.map(p => p.productName).join(', ');
    ElMessage.warning(`以下产品未完成批次分配: ${productNames}`);
    return false;
  }
  
  return true;
};

const handleDownloadTemplate = () => {
  downloadLoading.value = true;
  
  try {
    const exportProducts = filteredProducts.value
      .filter(product => product.productId && product.productName)
      .map(product => ({
        '产品ID': product.productId,
        '产品名称': product.productName,
        'SKU': product.sku || '',
        '规格': product.spec || '',
        '颜色': product.color || '',
        '库存数量': product.availableQuantity || 0,
        '当前数量': product.quantity || 0,
        '人民币单价': product.price || 0,
        '美元单价': product.priceUnitUsd || 0,
        '备注': product.remark || '',
      }));
    
    if (exportProducts.length === 0) {
      ElMessage.warning('没有产品可以导出');
      return;
    }
    
    const wb = XLSX.utils.book_new();
    
    const ws = XLSX.utils.json_to_sheet(exportProducts);
    
    const colWidths = [
      { wch: 15 },
      { wch: 30 },
      { wch: 20 },
      { wch: 15 },
      { wch: 10 },
      { wch: 8 },
      { wch: 12 },
      { wch: 12 },
      { wch: 15 },
      { wch: 15 },
      { wch: 30 },
    ];
    ws['!cols'] = colWidths;
    
    XLSX.utils.book_append_sheet(wb, ws, '销售出库模板');
    
    const instructionData = [
      ['使用说明'],
      ['1. 请在"当前数量"列填写出库数量'],
      ['2. 请在"人民币单价"列填写人民币单价'],
      ['3. 请在"美元单价"列填写美元单价'],
      ['4. "产品ID"和"SKU"是系统标识，请勿修改'],
      ['5. 请勿删除或修改表头'],
      ['6. 表格中已包含当前产品的初始数据，请直接修改数值'],
      ['7. 导入时系统会根据产品ID匹配并更新数据'],
      ['', ''],
      ['注意：'],
      ['- 只有"当前数量"大于0的产品才会被处理'],
      ['- 数量和价格不能为负数'],
      ['- 如果导入的产品不在当前列表中，将被忽略']
    ];
    
    const instructionWs = XLSX.utils.aoa_to_sheet(instructionData);
    const instructionColWidths = [{ wch: 50 }];
    instructionWs['!cols'] = instructionColWidths;
    XLSX.utils.book_append_sheet(wb, instructionWs, '使用说明');
    
    const wbout = XLSX.write(wb, { bookType: 'xlsx', type: 'array' });
    
    const blob = new Blob([wbout], { type: 'application/octet-stream' });
    const fileName = `销售出库单模板_${formData.orderNo || new Date().toISOString().slice(0, 10)}.xlsx`;
    
    saveAs(blob, fileName);
    
    ElMessage.success('模板下载成功');
  } catch (error) {
    console.error('下载模板失败:', error);
    ElMessage.error('下载模板失败');
  } finally {
    downloadLoading.value = false;
  }
};

const handleFileChange = (file) => {
  const isLt10M = file.size / 1024 / 1024 < 10;
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过10MB!');
    return;
  }
  
  currentFile.value = file;
  importResult.value = null;
  
  parseExcelFile(file);
};

const parseExcelFile = async (file) => {
  importLoading.value = true;
  
  try {
    const data = await readExcelFile(file);
    
    if (!data || data.length === 0) {
      importResult.value = {
        success: false,
        message: '文件内容为空或格式不正确'
      };
      return;
    }
    
    const processedData = processImportData(data);
    
    importResult.value = {
      success: true,
      data: processedData,
      message: `成功解析 ${processedData.length} 条记录`
    };
    
  } catch (error) {
    console.error('解析Excel文件失败:', error);
    importResult.value = {
      success: false,
      message: `解析失败: ${error.message || '未知错误'}`
    };
  } finally {
    importLoading.value = false;
  }
};

const readExcelFile = (file) => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    
    reader.onload = (e) => {
      try {
        const data = new Uint8Array(e.target.result);
        const workbook = XLSX.read(data, { type: 'array' });
        
        const firstSheetName = workbook.SheetNames[0];
        const worksheet = workbook.Sheets[firstSheetName];
        
        const jsonData = XLSX.utils.sheet_to_json(worksheet);
        resolve(jsonData);
      } catch (error) {
        reject(error);
      }
    };
    
    reader.onerror = () => {
      reject(new Error('读取文件失败'));
    };
    
    reader.readAsArrayBuffer(file.raw || file);
  });
};

const processImportData = (importData) => {
  const processedItems = [];
  let updatedCount = 0;
  let ignoredCount = 0;
  
  const productMap = new Map();
  outboundProducts.value.forEach(product => {
    productMap.set(product.productId, product);
    if (product.sku) {
      productMap.set(product.sku, product);
    }
  });
  
  importData.forEach((row, index) => {
    let product = null;
    let matchBy = '';
    
    if (row['产品ID']) {
      product = productMap.get(String(row['产品ID']));
      matchBy = '产品ID';
    }
    
    if (!product && row['SKU']) {
      product = productMap.get(String(row['SKU']));
      matchBy = 'SKU';
    }
    
    if (!product) {
      ignoredCount++;
      processedItems.push({
        index: index + 1,
        productId: row['产品ID'] || row['SKU'],
        productName: row['产品名称'],
        status: '忽略',
        reason: '未在当前产品列表中找到匹配的产品'
      });
      return;
    }
    
    let errors = [];
    
    const quantity = parseFloat(row['当前数量'] || row['数量'] || 0);
    if (isNaN(quantity) || quantity < 0) {
      errors.push('数量无效');
    }
    
    const price = parseFloat(row['人民币单价'] || row['单价'] || 0);
    if (isNaN(price) || price < 0) {
      errors.push('人民币单价无效');
    }
    
    const priceUnitUsd = parseFloat(row['美元单价'] || row['USD单价'] || 0);
    if (isNaN(priceUnitUsd) || priceUnitUsd < 0) {
      errors.push('美元单价无效');
    }
    
    if (errors.length > 0) {
      ignoredCount++;
      processedItems.push({
        index: index + 1,
        productId: product.productId,
        productName: product.productName,
        status: '错误',
        reason: errors.join(', ')
      });
      return;
    }
    
    if (quantity > 0) {
      const oldQuantity = product.quantity || 0;
      const oldPrice = product.price || 0;
      const oldPriceUsd = product.priceUnitUsd || 0;
      
      product.quantity = quantity;
      product.price = price;
      product.priceUnitUsd = priceUnitUsd;
      
      if (row['备注'] !== undefined) {
        product.remark = row['备注'];
      }
      
      if (product.isTriggerProduct && Math.abs(oldQuantity - quantity) > 0.001) {
        handleQuantityChange(product, quantity);
      }
      
      updatedCount++;
      
      processedItems.push({
        index: index + 1,
        productId: product.productId,
        productName: product.productName,
        status: '成功',
        matchBy: matchBy,
        changes: {
          数量: `${oldQuantity} → ${quantity}`,
          人民币单价: `${oldPrice} → ${price}`,
          美元单价: `${oldPriceUsd} → ${priceUnitUsd}`
        }
      });
    } else {
      ignoredCount++;
      processedItems.push({
        index: index + 1,
        productId: product.productId,
        productName: product.productName,
        status: '忽略',
        reason: '数量为0'
      });
    }
  });
  
  if (updatedCount > 0) {
    ElMessage.success(`成功更新 ${updatedCount} 个产品的数据`);
    
    resortProducts();
  }
  
  return {
    items: processedItems,
    summary: {
      total: importData.length,
      updated: updatedCount,
      ignored: ignoredCount
    }
  };
};

const handleImportSubmit = () => {
  if (!currentFile.value) {
    ElMessage.warning('请选择要上传的文件');
    return;
  }
  
  importDialog.visible = false;
  currentFile.value = null;
  
  if (importResult.value && importResult.value.success) {
    const result = importResult.value.data;
    ElMessage.success(`导入完成: 更新了 ${result.summary.updated} 个产品`);
  }
};

const handleImportExcel = () => {
  importDialog.visible = true;
};

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
      
      outboundProducts.value = [];
      groupedRecommendations.value = [];
      recommendationDetails.value = {};
      searchResults.value = [];
      
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
        query: { mode: 'outbound' }
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
  
  const hasProducts = outboundProducts.value.some(p => p.quantity > 0);
  if (!hasProducts) {
    ElMessage.warning('请至少设置一个产品的出库数量');
    return;
  }
  
  if (!checkAllocationStatus()) {
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
        query: { mode: 'outbound' }
      });
    }
  } catch (error) {
    ElMessage.error(isEditMode.value ? '更新失败' : '提交失败');
  } finally {
    loading.value = false;
  }
};

const prepareSubmitData = () => {
  const items = outboundProducts.value
    .filter(item => item.quantity > 0)
    .map(item => {
      // 修正数量计算 - 包装件需要特殊处理
      let quantity = item.quantity;
      
      // 如果是包装件，需要根据父产品数量计算
      if (item.isPackage && item.parentProductId && item.packageRatio) {
        const parentProduct = outboundProducts.value.find(p => 
          p.productId === item.parentProductId && !p.isPackage
        );
        
        if (parentProduct) {
          // 修正：包装件数量 = 父产品数量 / packageRatio
          // packageRatio 表示 "多少成品用1个包装"
          quantity = parentProduct.quantity / item.packageRatio;
          
          // 四舍五入保留4位小数
          quantity = Math.round(quantity * 10000) / 10000;
        }
      }
      
      // 确保数量有效
      quantity = quantity || 0;
      
      // 计算总金额 - 添加四舍五入
      const price = item.price || 0;
      const priceUnitUsd = item.priceUnitUsd || 0;
      
      // 使用精确计算
      const priceTotal = preciseCalculate.multiply(item.price, item.quantity, 2);
      const priceTotalUsd = preciseCalculate.multiply(item.priceUnitUsd, item.quantity, 2);
      
      // 确定产品类型
      let isRecommendProductValue;
      
      if (item.isPackage) {
        isRecommendProductValue = 2;
      } else if (item.isRecommend) {
        isRecommendProductValue = 1;
      } else {
        isRecommendProductValue = 0;
      }
      
      const extension = item.extension || {
        productId: item.productId,
        isTriggerProduct: item.isTriggerProduct ? 0 : 1,
        isRecommendProduct: isRecommendProductValue,
        triggerProductId: item.triggerProductId || 0,
        remark: item.remark || '',
        isPackageProduct: item.isPackage ? 0 : 1,
        packageRatio: item.packageRatio || 0,
      };
      
      if (item.extension) {
        extension.isTriggerProduct = item.isTriggerProduct ? 0 : 1;
        extension.isRecommendProduct = isRecommendProductValue;
        extension.triggerProductId = item.triggerProductId || 0;
      }
      
      return {
        productId: item.productId,
        productName: item.productName,
        sku: item.sku,
        spec: item.spec,
        unit: item.unitName,
        color: item.color,
        currentStock: item.availableQuantity,
        quantity: quantity,
        price: price,
        priceTotal: priceTotal,
        priceUnitUsd: priceUnitUsd,
        priceTotalUsd: priceTotalUsd,
        batchAllocations: (item.batchAllocations || []).map(allocation => ({
          batchNo: allocation.batchNo,
          shelfId: allocation.shelfId,
          shelfName: allocation.shelfName,
          quantity: allocation.quantity,
          price: allocation.price || price || 0
        })),
        remark: item.remark || '',
        isTriggerProduct: item.isTriggerProduct || false,
        isRecommend: item.isRecommend || false,
        isPackage: item.isPackage || false,
        triggerProductId: item.triggerProductId || null,
        extension: extension
      };
    });
  
  // 计算总计 - 使用精确计算
  const totalQuantity = items.reduce((sum, item) => {
    return Math.round((sum + (item.quantity || 0)) * 10000) / 10000;
  }, 0);
  
  const totalAmount = items.reduce((sum, item) => {
    return preciseCalculate.add(sum, item.priceTotal || 0, 2);
  }, 0);
  
  const totalAmountUsd = items.reduce((sum, item) => {
    return Math.round((sum + (item.priceTotalUsd || 0)) * 100) / 100;
  }, 0);
  
  return {
    ...formData,
    items: items,
    totalQuantity: totalQuantity,
    totalAmount: totalAmount,
    totalAmountUsd: totalAmountUsd
  };
};

const validateForm = async () => {
  if (!formRef.value) return false;
  
  try {
    await formRef.value.validate();
    
    const productsWithQuantity = outboundProducts.value.filter(p => p.quantity > 0);
    
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

const handlePreview = (file) => {
  if (file.url) {
    window.open(file.url, '_blank');
  }
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

const resortProducts = () => {
  const sorted = sortProductsByHierarchy();
  outboundProducts.value.splice(0, outboundProducts.value.length, ...sorted);
};

const handleQuantityChange = async (row, value) => {
  console.log(`handleQuantityChange: ${row.productName}, value: ${value}, lastQuantity: ${row.lastQuantity}`);
  
  if (value > 0) {
    // 修改这个条件：只要数量有变化就更新推荐，不只是从0到有值
    if (row.isTriggerProduct && row.productLevel === 0) {
      const quantityChanged = value !== row.lastQuantity;
      
      if (quantityChanged) {
        console.log(`Trigger product quantity changed: ${row.lastQuantity} -> ${value}`);
        
        // 更新当前产品的推荐
        await loadRecommendationsForProduct(row.productId, value);
        
        // 重新计算并应用必选推荐
        await applyRequiredRecommendations(row.productId);
        
        // 更新包装件
        if (!row.isPackage && !row.isRecommend) {
          await updatePackageComponentsBatch(row.productId, value);
        }
        
        // 更新已经存在的推荐商品数量
        await updateExistingRecommendationQuantities(row.productId, value);
      }
    }
    
    if (!row.extension) {
      row.extension = {
        productId: row.productId,
        isTriggerProduct: row.isTriggerProduct ? 0 : 1,
        isRecommendProduct: row.isRecommend ? 0 : 1,
        triggerProductId: row.triggerProductId || null
      };
    }
    
    if (!row.historyPrice && formData.customerId) {
      await loadProductHistoryPrice(row);
    }
  } else {
    row.quantity = 0;
    row.price = 0;
    row.priceUnitUsd = 0;
    row.remark = '';
    row.batchAllocations = [];
    
    if (row.isTriggerProduct && row.productLevel === 0) {
      removeRecommendationsForTrigger(row.productId, true);
      removePackageComponents(row.productId, true);
    }

     // 清理可能存在的错误关联
    if (value === 0) {
      cleanupIncorrectRecommendations();
    }
  }
  
  row.lastQuantity = value;
  
  // 确保触发重新排序
  resortProducts();
};

const updateExistingRecommendationQuantities = async (triggerProductId, newTriggerQuantity) => {
  if (!triggerProductId || !newTriggerQuantity) return;
  
  console.log(`updateExistingRecommendationQuantities: trigger=${triggerProductId}, quantity=${newTriggerQuantity}`);
  
  // 找到与该触发商品相关的推荐商品
  const relatedRecommendations = outboundProducts.value.filter(p => 
    p.parentTriggerId === triggerProductId && 
    p.isRecommend && 
    p.productLevel === 1 &&
    p.triggerProductId === triggerProductId  // 确保触发商品ID匹配
  );
  
  console.log(`找到 ${relatedRecommendations.length} 个相关的推荐商品`);
  
  if (relatedRecommendations.length === 0) return;
  
  // 找到对应的推荐规则组
  const triggerGroup = groupedRecommendations.value.find(g => g.triggerProductId === triggerProductId);
  if (!triggerGroup) return;
  
  let updatedCount = 0;
  
  for (const recommendation of relatedRecommendations) {
    // 在推荐规则中找到对应的项
    const ruleItem = triggerGroup.items.find(item => 
      item.productId === recommendation.productId && 
      item.triggerProductId === triggerProductId
    );
    
    if (!ruleItem) {
      console.log(`未找到推荐规则: ${recommendation.productName}`);
      continue;
    }
    
    // 计算新的推荐数量
    const calculatedQuantity = calculateRecommendedQuantity(ruleItem, triggerProductId, newTriggerQuantity);
    
    if (calculatedQuantity > 0 && calculatedQuantity !== recommendation.quantity) {
      const oldQuantity = recommendation.quantity;
      recommendation.quantity = calculatedQuantity;
      
      // 更新推荐面板中的数量
      ruleItem.calculatedQuantity = calculatedQuantity;
      ruleItem.actualQuantity = calculatedQuantity;
      
      console.log(`更新推荐商品 ${recommendation.productName}: ${oldQuantity} -> ${calculatedQuantity}`);
      updatedCount++;
    }
  }
  
  if (updatedCount > 0) {
    // 更新推荐详情
    updateRecommendationDetails();
    
    // 显示提示信息
    if (updatedCount > 0) {
      ElMessage.info(`已更新 ${updatedCount} 个推荐商品的数量`);
    }
  }
  
  return updatedCount;
};


// 精确计算函数
const preciseCalculate = {
  // 乘法，保留指定位数
  multiply: (a, b, precision = 2) => {
    const result = a * b;
    return Math.round(result * Math.pow(10, precision)) / Math.pow(10, precision);
  },
  
  // 加法，保留指定位数
  add: (a, b, precision = 2) => {
    const result = a + b;
    return Math.round(result * Math.pow(10, precision)) / Math.pow(10, precision);
  },
  
  // 除法，保留指定位数
  divide: (a, b, precision = 4) => {
    if (b === 0) return 0;
    const result = a / b;
    return Math.round(result * Math.pow(10, precision)) / Math.pow(10, precision);
  }
};


const updatePackageComponentsBatch = async (parentProductId, parentQuantity) => {
  try {
    const res = await get(`/api/auth/product/bomDetailOnlyPackageInfo?productId=${parentProductId}`);
    
    if (res && res.list && res.list.length > 0) {
      const parentIndex = outboundProducts.value.findIndex(p => 
        p.productId === parentProductId && !p.isPackage
      );
      
      if (parentIndex === -1) return;
      
      for (const component of res.list) {
        const availableProduct = availableProducts.value.find(p => 
          p.productId === component.componentProductId
        );
        
        if (!availableProduct) continue;
        
        // 修正包装件数量的计算逻辑
        const packageRatio = component.otherQuantity ? Number(component.otherQuantity) : 1;
        
        // 计算包装件数量
        // packageRatio = 5 表示 "5个成品用1个包装"
        // 包装件数量 = 成品数量 ÷ packageRatio
        let packageQuantity = 0;
        if (packageRatio > 0) {
          packageQuantity = preciseCalculate.divide(parentQuantity, packageRatio, 4);
          // 四舍五入保留4位小数
          packageQuantity = Math.round(packageQuantity * 10000) / 10000;
        }
        
        // 包装件推荐数量是比例值
        const recommendedQuantity = packageRatio > 0 ? (1 / packageRatio) : 0;
        
        const packageItem = {
          ...availableProduct,
          productId: component.componentProductId,
          productName: component.componentProductName || availableProduct.productName,
          sku: component.componentProductSku || availableProduct.sku,
          spec: component.componentProductSpec || availableProduct.spec,
          color: component.componentProductColor || availableProduct.color,
          quantity: packageQuantity,
          price: availableProduct.originalPrice || 0,
          priceUnitUsd: availableProduct.originalPriceUnitUsd || 0,
          remark: `包装件 - ${component.remark || ''}`,
          batchAllocations: [],
          isTriggerProduct: false,
          isRecommend: false,
          isPackage: true,
          productLevel: 2,
          parentProductId: parentProductId,
          packageRatio: packageRatio,
          recommendedQuantity: recommendedQuantity,
          triggerProductId: parentProductId,
          extension: {
            productId: component.componentProductId,
            isTriggerProduct: 1,
            isRecommendProduct: 2,
            parentProductId: parentProductId,
            triggerProductId: parentProductId,
            isPackageProduct: 0,
            packageRatio: packageRatio,
            recommendedQuantity: recommendedQuantity
          },
          historyPrice: null,
          lastQuantity: packageQuantity,
          availableQuantity: availableProduct.availableQuantity || 0,
          unitName: availableProduct.unitName || '个'
        };
        
        const existingPackageIndex = outboundProducts.value.findIndex(p => 
          p.isPackage && p.productId === packageItem.productId && p.parentProductId === parentProductId
        );
        
        if (existingPackageIndex === -1) {
          outboundProducts.value.splice(parentIndex + 1, 0, packageItem);
        } else {
          // 更新现有的包装件
          outboundProducts.value[existingPackageIndex] = {
            ...outboundProducts.value[existingPackageIndex],
            ...packageItem
          };
        }
      }
    }
  } catch (error) {
    console.error('编辑模式加载包装件失败:', error);
  }
};

const removePackageComponents = (parentProductId, deletePackages = false) => {
  if (deletePackages) {
    const indices = [];
    outboundProducts.value.forEach((item, index) => {
      if (item.parentProductId === parentProductId && item.isPackage) {
        indices.unshift(index);
      }
    });
    
    indices.forEach(index => {
      outboundProducts.value.splice(index, 1);
    });
  } else {
    outboundProducts.value.forEach(item => {
      if (item.parentProductId === parentProductId && item.isPackage) {
        item.quantity = 0;
        item.price = 0;
        item.priceUnitUsd = 0;
        item.remark = '';
        item.batchAllocations = [];
      }
    });
  }
};

const clearSelectedFile = () => {
  currentFile.value = null;
  importResult.value = null;
};

const selectedProducts = ref([]);

onMounted(() => {
  if (isEditMode.value) {
    loadOutboundDetail(route.params.id);
  } else {
    generateOrderNo();
  }
  loadWarehouseList();
  loadCustomerList();
});

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
      outboundProducts.value = [];
      groupedRecommendations.value = [];
      recommendationDetails.value = {};
      searchResults.value = [];
      quickSearch.value = '';
      generateOrderNo();
    }
  }
);

watch(
  [() => formData.warehouseId, () => formData.customerId],
  ([warehouseId, customerId]) => {
    if (warehouseId && customerId) {
      loadAvailableProducts();
    } else {
      availableProducts.value = [];
      filteredSelectorProducts.value = [];
    }
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

.auto-allocate-btn {
  background-color: #67C23A;
  border-color: #67C23A;
}

.auto-allocate-btn:hover {
  background-color: #5da737;
  border-color: #5da737;
}

.outbound-form {
  margin-bottom: 30px;
}

.product-add-section {
  margin: 20px 0;
  padding: 16px;
  background-color: #f8f9fa;
  border-radius: 6px;
  border: 1px solid #ebeef5;
}

.product-add-section .section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.product-add-section .header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.search-results {
  margin-top: 16px;
}

.search-results-card {
  border: 1px solid #e4e7ed;
}

.search-results-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-results-list {
  max-height: 300px;
  overflow-y: auto;
}

.search-result-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.search-result-item:last-child {
  border-bottom: none;
}

.search-result-item .product-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.search-result-item .product-name {
  font-weight: 500;
  color: #303133;
}

.search-result-item .sku-text {
  font-size: 12px;
  color: #909399;
}

.search-result-item .stock-text {
  font-size: 12px;
  color: #67c23a;
}

.recommendations-panel {
  margin: 20px 0;
  padding: 16px;
  background: linear-gradient(135deg, #f0f9ff 0%, #e6f7ff 100%);
  border: 1px solid #91d5ff;
  border-radius: 6px;
  box-shadow: 0 2px 8px rgba(145, 213, 255, 0.1);
}

.recommendations-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #91d5ff;
}

.recommendations-header .header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.recommendations-header .header-left h3 {
  margin: 0;
  font-size: 16px;
  font-weight: bold;
  color: #1890ff;
  display: flex;
  align-items: center;
  gap: 6px;
}

.recommendations-header .tip {
  font-size: 12px;
  color: #69c0ff;
}

.recommendations-header .header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.recommendation-group {
  margin-bottom: 20px;
  padding: 12px;
  background-color: #fafdff;
  border-radius: 4px;
  border: 1px solid #91d5ff;
}

.recommendation-group:last-child {
  margin-bottom: 0;
}

.group-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px dashed #91d5ff;
}

.trigger-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.trigger-product {
  font-weight: 500;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
}

.trigger-quantity {
  font-size: 12px;
  color: #606266;
}

.refresh-btn {
  margin-left: 8px;
}

.recommendation-items-table {
  margin-top: 12px;
}

.recommended-product-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.recommended-product-info .product-name {
  font-weight: 500;
  color: #303133;
}

.recommended-product-info .sku-text {
  font-size: 12px;
  color: #909399;
}

.recommended-product-info .spec-text {
  font-size: 12px;
  color: #67c23a;
}

.confidence {
  font-size: 11px;
  color: #69c0ff;
  margin-top: 2px;
}

.required-tip {
  font-size: 11px;
  color: #f56c6c;
  margin-top: 2px;
}

.recommended-quantity {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.quantity-type {
  font-size: 12px;
  color: #909399;
}

.quantity-value {
  font-weight: 500;
  color: #303133;
}

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
  gap: 20px;
}

.section-header-left h3 {
  margin: 0;
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.header-right-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.auto-allocation-status {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 4px 12px;
  background-color: #f0f9ff;
  border-radius: 4px;
  border: 1px solid #91d5ff;
}

.allocation-progress {
  width: 100px;
}

.progress-text {
  font-size: 12px;
  color: #1890ff;
  white-space: nowrap;
}

.auto-allocation-result {
  margin: 16px 0;
}

.product-table {
  margin-bottom: 16px;
}

.product-info-cell {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.product-info-cell .product-name {
  font-weight: 500;
  color: #303133;
  cursor: pointer;
  display: flex;
  align-items: flex-start;
  gap: 6px;
  transition: color 0.2s;
}

.product-info-cell .product-name:hover {
  color: #1890ff;
}

.product-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin-top: 4px;
}

.trigger-tag {
  height: 20px;
  line-height: 18px;
  background-color: #f0f9ff;
  border-color: #91d5ff;
  color: #1890ff;
}

.recommend-tag {
  height: 20px;
  line-height: 18px;
  background-color: #f9f0ff;
  border-color: #d6adff;
  color: #722ed1;
}

.package-tag {
  height: 20px;
  line-height: 18px;
  background-color: #fdf6ec;
  border-color: #fac858;
  color: #e6a23c;
}

.normal-tag {
  height: 20px;
  line-height: 18px;
  background-color: #f5f5f5;
  border-color: #dcdfe6;
  color: #606266;
}

.package-source-row {
  margin-top: 4px;
  padding: 3px 6px;
  background-color: #fdf6ec;
  border-radius: 3px;
  border: 1px solid #fac858;
  max-width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.package-source-text {
  font-size: 11px;
  color: #e6a23c;
  display: flex;
  align-items: center;
  gap: 3px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
  min-width: 0;
}

.package-ratio {
  font-size: 11px;
  color: #909399;
  white-space: nowrap;
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

.trigger-source-row {
  margin-top: 4px;
  padding: 3px 6px;
  background-color: #f6ffed;
  border-radius: 3px;
  border: 1px solid #b7eb8f;
  max-width: 100%;
}

.trigger-source-text {
  font-size: 11px;
  color: #52c41a;
  display: flex;
  align-items: center;
  gap: 3px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.trigger-source-text .el-icon {
  font-size: 10px;
}

.recommend-quantity-info {
  font-size: 11px;
  color: #909399;
  white-space: nowrap;
}

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

.stock-ok {
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
  color: #606266;
}

.price-value {
  color: #303133;
  font-weight: 500;
}

.recommend-quantity-summary {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  border: 1px solid #ebeef5;
  transition: background-color 0.2s;
}

.recommend-quantity-summary:hover {
  background-color: #f5f7fa;
}

.summary-badge {
  display: flex;
  align-items: center;
}

.total-quantity-display {
  font-weight: 500;
  color: #1890ff;
}

.expand-icon {
  font-size: 12px;
  color: #909399;
  margin-left: auto;
}

.recommend-detail-popover {
  padding: 8px;
  max-height: 300px;
  overflow-y: auto;
}

.popover-header {
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
}

.product-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 4px;
}

.product-header .product-name {
  font-weight: 500;
  color: #303133;
}

.recommend-title {
  font-size: 12px;
  color: #909399;
}

.detail-scroll {
  max-height: 200px;
  overflow-y: auto;
}

.detail-item {
  padding: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.detail-item.last-item {
  border-bottom: none;
}

.trigger-source {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.trigger-info {
  flex: 1;
}

.trigger-name {
  font-size: 13px;
  font-weight: 500;
  color: #303133;
}

.trigger-spec {
  font-size: 11px;
  color: #909399;
}

.recommend-quantity {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 4px;
}

.quantity-label {
  font-size: 12px;
  color: #606266;
}

.quantity-value {
  font-size: 13px;
  font-weight: 500;
  color: #1890ff;
}

.popover-footer {
  margin-top: 12px;
  padding-top: 8px;
  border-top: 1px solid #ebeef5;
}

.footer-summary {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 4px;
}

.total-label {
  font-size: 12px;
  color: #606266;
}

.total-number {
  font-size: 16px;
  font-weight: bold;
  color: #1890ff;
}

.total-unit {
  font-size: 12px;
  color: #909399;
}

.quantity-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.quantity-input {
  flex: 1;
}

.max-btn {
  flex-shrink: 0;
}

.quantity-view {
  display: inline-block;
  width: 100%;
  height: 32px;
  line-height: 32px;
  text-align: center;
  font-weight: 500;
  color: #303133;
}

.price-input {
  width: 100%;
}

.amount-text {
  font-weight: 500;
  color: #303133;
}

.batch-allocation {
  display: flex;
  align-items: center;
  gap: 8px;
}

.batch-btn {
  white-space: nowrap;
}

.batch-summary {
  display: flex;
  align-items: center;
  gap: 8px;
}

.batch-tag {
  cursor: pointer;
}

.batch-detail {
  padding: 8px;
}

.batch-detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4px 0;
  border-bottom: 1px solid #f0f0f0;
}

.batch-detail-item:last-child {
  border-bottom: none;
}

.batch-no {
  font-weight: 500;
  color: #303133;
  min-width: 80px;
}

.shelf-name {
  color: #606266;
  flex: 1;
  margin: 0 8px;
}

.batch-quantity {
  color: #67c23a;
  font-weight: 500;
  min-width: 50px;
  text-align: right;
}

.allocation-total {
  font-size: 12px;
  color: #67c23a;
  font-weight: 500;
}

.batch-empty {
  display: flex;
  align-items: center;
}

.empty-text {
  color: #909399;
  font-size: 12px;
}

.empty-product-tip {
  padding: 60px 20px;
  text-align: center;
  background-color: #f8f9fa;
  border-radius: 6px;
  border: 1px dashed #dcdfe6;
}

.empty-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.empty-icon {
  font-size: 48px;
  color: #c0c4cc;
}

.empty-content p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

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

.product-selector-dialog {
  padding: 10px 0;
}

.selector-filter {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.selector-table {
  margin-bottom: 20px;
}

.selector-table .product-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.selector-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

.selected-count {
  font-size: 14px;
  color: #606266;
}

.action-buttons {
  display: flex;
  gap: 12px;
}

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

.view-mode-recommendations {
  padding: 16px;
  background-color: #fafdff;
  border-radius: 4px;
  border: 1px solid #91d5ff;
}

.recommendation-summary h4 {
  margin: 0 0 12px 0;
  color: #1890ff;
}

.recommendation-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.recommendation-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background-color: #ffffff;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
}

.recommendation-item .product-name {
  font-weight: 500;
  color: #303133;
}

.recommendation-item .total-recommended {
  color: #1890ff;
  font-weight: 500;
}

.actual-quantity {
  color: #67c23a;
  font-weight: bold;
}

.source-tip {
  margin-top: 4px;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
}

.source-text {
  color: #67c23a;
}

.back-header {
  margin-bottom: 16px;
  padding: 0 4px;
}

.back-btn {
  padding: 10px 16px;
  font-size: 16px;
  font-weight: 500;
  color: #409EFF;
}

.back-btn:hover {
  background-color: #ecf5ff;
  border-radius: 4px;
}

.back-btn i {
  margin-right: 6px;
  font-size: 18px;
}

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
    flex-wrap: wrap;
    gap: 8px;
  }
  
  .product-add-section .section-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
  
  .product-add-section .header-actions {
    width: 100%;
  }
  
  .recommendations-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
  
  .group-header {
    flex-direction: column;
    gap: 8px;
    align-items: flex-start;
  }
  
  .section-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
  
  .section-header-left {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .header-right-actions {
    width: 100%;
    justify-content: flex-start;
    flex-wrap: wrap;
    gap: 8px;
  }
  
  .auto-allocation-status {
    flex-direction: column;
    align-items: stretch;
    gap: 8px;
  }
  
  .allocation-progress {
    width: 100%;
  }
  
  .progress-text {
    font-size: 11px;
    text-align: center;
  }
  
  .quick-stats .el-col {
    margin-bottom: 8px;
  }
  
  .summary-info .el-col {
    margin-bottom: 8px;
  }
  
  .selector-filter {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
  
  .selector-actions {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
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
  .product-add-section .header-actions {
    flex-direction: column;
    align-items: stretch;
  }
  
  .product-add-section .header-actions .el-input {
    width: 100%;
    margin-right: 0;
  }
  
  .product-details {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
  
  .product-tags {
    flex-direction: column;
    align-items: flex-start;
  }
}

/* 添加新的样式 */
.trigger-hint {
  font-size: 11px;
  color: #69c0ff;
  margin-left: 4px;
  font-style: italic;
}

.count-tag {
  background-color: #e6f7ff;
  border-color: #91d5ff;
  color: #1890ff;
}

.trigger-source-badge {
  font-size: 11px;
  color: #69c0ff;
  margin-left: 4px;
  font-style: italic;
}

.source-count {
  font-size: 11px;
  color: #909399;
  margin-left: 4px;
}

.package-recommended-quantity {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
  padding: 4px 8px;
  background-color: #fdf6ec;
  border-radius: 4px;
  border: 1px solid #fac858;
}

.package-recommended-quantity .package-tag {
  margin: 0;
}

.recommended-value {
  font-weight: 500;
  color: #e6a23c;
  font-size: 14px;
}

.ratio-info {
  font-size: 11px;
  color: #909399;
  font-style: italic;
}

/* 包装件推荐数量样式 */
.package-recommended-quantity {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
  padding: 6px 8px;
  background-color: #fdf6ec;
  border-radius: 4px;
  border: 1px solid #fac858;
}

.package-recommended-quantity .recommended-value {
  font-weight: 500;
  color: #e6a23c;
  font-size: 14px;
}

.package-recommended-quantity .ratio-info {
  font-size: 12px;
  color: #909399;
}

.package-recommended-quantity .trigger-info {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: #67c23a;
  margin-top: 2px;
}

.package-recommended-quantity .trigger-info .el-icon {
  font-size: 10px;
}

.package-recommended-quantity .trigger-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 120px;
}

/* 触发商品推荐信息样式 */
.trigger-recommend-info {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 6px;
  padding: 6px 8px;
  background-color: #f0f9ff;
  border-radius: 4px;
  border: 1px solid #91d5ff;
}

.trigger-recommend-info .trigger-tag {
  height: 20px;
  line-height: 18px;
  background-color: #f0f9ff;
  border-color: #91d5ff;
  color: #1890ff;
}

.trigger-recommend-info .trigger-tip {
  font-size: 11px;
  color: #69c0ff;
  font-style: italic;
}

/* 包装件推荐提示 */
.recommend-tip {
  font-size: 11px;
  color: #67c23a;
  font-style: italic;
  margin-left: 4px;
}

/* 包装件推荐数量显示 */
.package-recommended-quantity {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
  padding: 6px 8px;
  background-color: #fdf6ec;
  border-radius: 4px;
  border: 1px solid #fac858;
}

.package-recommended-quantity .recommended-value {
  font-weight: 500;
  color: #e6a23c;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.package-recommended-quantity .ratio-info {
  font-size: 12px;
  color: #909399;
}

.package-recommended-quantity .trigger-info {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: #67c23a;
  margin-top: 2px;
}

.package-recommended-quantity .trigger-info .el-icon {
  font-size: 10px;
}

/* 包装件推荐提示 */
.recommend-tip {
  font-size: 11px;
  color: #67c23a;
  font-style: italic;
  margin-left: 4px;
}

/* 包装件推荐数量显示 */
.package-recommended-quantity {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
  padding: 6px 8px;
  background-color: #fdf6ec;
  border-radius: 4px;
  border: 1px solid #fac858;
}

.package-recommended-quantity .recommended-value {
  font-weight: 500;
  color: #e6a23c;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.package-recommended-quantity .ratio-info {
  font-size: 12px;
  color: #909399;
}

.package-recommended-quantity .trigger-info {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: #67c23a;
  margin-top: 2px;
}

.package-recommended-quantity .trigger-info .el-icon {
  font-size: 10px;
}

/* 包装件推荐提示 */
.recommend-tip {
  font-size: 11px;
  color: #67c23a;
  font-style: italic;
  margin-left: 4px;
}

/* 包装件推荐数量显示 */
.package-recommended-quantity {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
  padding: 6px 8px;
  background-color: #fdf6ec;
  border-radius: 4px;
  border: 1px solid #fac858;
}

.package-recommended-quantity .recommended-value {
  font-weight: 500;
  color: #e6a23c;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.package-recommended-quantity .ratio-info {
  font-size: 12px;
  color: #909399;
}

.package-recommended-quantity .trigger-info {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: #67c23a;
  margin-top: 2px;
}

.package-recommended-quantity .trigger-info .el-icon {
  font-size: 10px;
}

/* 备注单元格样式 */
.remark-cell {
  padding: 8px;
  border-radius: 4px;
  cursor: pointer;
  border: 1px solid #ebeef5;
  transition: all 0.2s;
  background-color: #fafafa;
  min-height: 60px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.remark-cell:hover {
  border-color: #409eff;
  background-color: #ecf5ff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
}

.remark-disabled {
  cursor: not-allowed;
  background-color: #f5f7fa;
  opacity: 0.6;
}

.remark-disabled:hover {
  border-color: #ebeef5;
  background-color: #f5f7fa;
  box-shadow: none;
}

.remark-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 8px;
}

.remark-text {
  flex: 1;
  color: #303133;
  font-size: 13px;
  line-height: 1.4;
  word-break: break-word;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  max-height: 4.2em;
}

.remark-empty {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #909399;
  font-size: 13px;
}

.edit-icon {
  color: #409eff;
  font-size: 14px;
  flex-shrink: 0;
  transition: transform 0.2s;
}

.remark-cell:hover .edit-icon {
  transform: scale(1.1);
}

.remark-length {
  font-size: 11px;
  color: #909399;
  text-align: right;
  margin-top: 4px;
}

/* 备注对话框样式 */
.remark-dialog-content {
  padding: 10px 0;
}

.remark-textarea {
  margin-bottom: 20px;
}

.remark-textarea :deep(.el-textarea__inner) {
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  line-height: 1.5;
}

.remark-dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .remark-cell {
    min-height: 50px;
  }
  
  .remark-text {
    font-size: 12px;
    -webkit-line-clamp: 2;
    max-height: 2.8em;
  }
}

.remark-cell:active {
  transform: translateY(1px);
}
</style>