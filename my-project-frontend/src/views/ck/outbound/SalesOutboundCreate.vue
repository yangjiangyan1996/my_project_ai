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
            <el-button @click="handleReset" >重置</el-button>
            
            <!-- 新增：自动全部分配按钮 -->
            <!-- <el-button 
              type="success" 
              @click="handleAutoAllocateAll"
              :loading="autoAllocating"
              :disabled="!canAutoAllocate || isViewMode"
              v-if="!isViewMode"
              class="auto-allocate-btn"
            >
              <el-icon><MagicStick /></el-icon>
              自动全部分配
            </el-button> -->
            
            <el-button 
              type="primary" 
              @click="handleSaveDraft" 
              :loading="loading"
              v-if="!isViewMode && (!isEditMode || (isEditMode && formData.status === 0))"
            >
              保存草稿
            </el-button>
            <el-button 
              type="primary" 
              @click="handleSubmit" 
              :loading="loading"
              v-if="!isViewMode && (!isEditMode || (isEditMode && (formData.status === 0 || formData.status === 4)))"
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
            <!-- 快速搜索添加 -->
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
                  
                  <!-- 新增：来源提示 -->
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
                      = {{ getTriggerProductQuantity(getTriggerIdForRow(row)) }} × {{ row.quantityValue }}
                      = {{ row.calculatedQuantity || calculateRecommendedQuantity(row, getTriggerIdForRow(row)) }}
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
                <span :class="stock-none">
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
            
            <!-- 查看模式下显示实际添加数量 -->
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
        
        <!-- 查看模式下显示推荐详情 -->
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
                <span class="total-recommended">推荐总量: {{ getTotalRecommendedQuantity(productId) }}</span>
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
            <!-- 自动分配加载状态 -->
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
            
             <!-- 新增：自动全部分配按钮 -->
            <el-button 
              type="success" 
              @click="handleAutoAllocateAll"
              :loading="autoAllocating"
              :disabled="!canAutoAllocate || isViewMode"
              v-if="!isViewMode"
              class="auto-allocate-btn"
            >
              <el-icon><MagicStick /></el-icon>
              自动全部分配
            </el-button>

            <!-- 批量操作 -->
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
                  </div>
                </div>
                <div class="product-details">
                  <div class="sku-text">{{ row.sku }}</div>
                  <div class="spec-text">{{ row.spec || '-' }}</div>
                  <div class="color-text">{{ row.color || '-' }}</div>

                   <!-- 新增：触发产品来源信息（单独一行） -->
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
          
          <!-- 新增：推荐数量列 -->
          <!-- 产品明细表格中修改推荐数量列 -->
          <el-table-column label="推荐数量" width="180" align="center" v-if="hasRecommendationDetails">
            <template #default="{ row }">
              <!-- 使用Popover显示完整信息 -->
              <el-popover
                v-if="row.isRecommend && getRecommendationDetails(row.productId).length > 0"
                placement="top-start"
                :width="280"
                trigger="click"
                :show-arrow="false"
              >
                <template #reference>
                  <div class="recommend-quantity-summary" @click.stop>
                    <div class="summary-badge">
                      <el-tag size="small" type="info" class="count-tag">
                        {{ getRecommendationDetails(row.productId).length }}
                      </el-tag>
                    </div>
                    <div class="total-quantity-display">
                      {{ getTotalRecommendedQuantity(row.productId) }} 个
                    </div>
                    <el-icon class="expand-icon"><ArrowRight /></el-icon>
                  </div>
                </template>
                
                <!-- Popover内容 -->
                <div class="recommend-detail-popover">
                  <div class="popover-header">
                    <div class="product-header">
                      <el-icon><Connection /></el-icon>
                      <span class="product-name">{{ row.productName }}</span>
                    </div>
                    <div class="recommend-title">推荐来源详情</div>
                  </div>
                  
                  <div class="detail-scroll">
                    <div 
                      v-for="(detail, index) in getRecommendationDetails(row.productId)" 
                      :key="detail.triggerProductId" 
                      class="detail-item"
                      :class="{'last-item': index === getRecommendationDetails(row.productId).length - 1}"
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
                      </div>
                    </div>
                  </div>
                  
                  <div class="popover-footer">
                    <div class="footer-summary">
                      <span class="total-label">推荐总数:</span>
                      <span class="total-number">{{ getTotalRecommendedQuantity(row.productId) }}</span>
                      <span class="total-unit">个</span>
                    </div>
                  </div>
                </div>
              </el-popover>
              
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
                  @change="(value) => handleQuantityChange(row, value)"
                  placeholder="数量"
                  :disabled="row.availableQuantity <= 0"
                  class="quantity-input"
                />
                <!-- 查看模式下只显示数值 -->
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
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch, nextTick } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { 
  Plus, Delete, Upload, Download, Document, Close, Search, 
  Operation, MagicStick, Warning, InfoFilled, Refresh, 
  CloseBold, SetUp, ArrowUp, Goods, Connection, Loading, ArrowLeft
} from '@element-plus/icons-vue';
import { post, get } from '@/net';
import axios from 'axios';
import { accessHeader } from '@/net'; 

const router = useRouter();
const route = useRoute();
const formRef = ref();
const loading = ref(false);
const downloadLoading = ref(false);

// 产品选择相关
const showProductSelector = ref(false);
const quickSearch = ref('');
const searchResults = ref([]);
const selectorSearch = ref('');
const selectedProductsForSelector = ref([]);

// 新增：自动分配相关状态
const autoAllocating = ref(false);
const isAllocating = ref(false);
const allocationProgress = ref(0);
const currentAllocationIndex = ref(0);
const totalAllocationCount = ref(0);
const autoAllocationResult = ref(null);

// 可用产品列表
const availableProducts = ref([]);
const filteredSelectorProducts = ref([]);

// 推荐相关
const autoApplyRequired = ref(true);
const groupedRecommendations = ref([]);

// 新增：推荐详情数据存储
const recommendationDetails = ref({}); // 存储产品推荐详情 {productId: [{triggerProductId, recommendedQuantity}, ...]}

// 批次分配相关
const showAllocationStrategy = ref(false);
const allocationStrategy = ref('FIFO');
const applyingStrategy = ref(false);
const loadingBatches = ref(false);

// 表单数据
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

// 出库产品数据
const outboundProducts = ref([]);

// 产品类型筛选
const productTypeFilter = ref(['trigger', 'recommend']);

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
const fileList = ref([]);

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

// 导入对话框
const importDialog = reactive({
  visible: false
});
const currentFile = ref(null);
const importResult = ref(null);
const importLoading = ref(false);

// 计算属性
const isEditMode = computed(() => !!route.params.id);

const isViewMode = computed(() => {
  // 如果是编辑模式但状态不可编辑，则进入查看模式
  if (isEditMode.value) {
    // 假设状态 0=草稿, 1=待审核, 2=已审核, 3=已出库, 4=已取消
    return formData.status > 1; // 只有草稿和待审核状态可编辑
  }
  return false; // 新建模式肯定不是查看模式
});

const filteredProducts = computed(() => {
  return outboundProducts.value.filter(p => {
    if (p.quantity > 0) return true; // 总是显示有数量的产品
    if (!productTypeFilter.value.length) return false;
    
    const isTrigger = p.isTriggerProduct && productTypeFilter.value.includes('trigger');
    const isRecommend = p.isRecommend && productTypeFilter.value.includes('recommend');
    return isTrigger || isRecommend;
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

const selectedProductCount = computed(() => {
  return 0;
});

const hasProductsWithQuantity = computed(() => {
  return outboundProducts.value.some(p => p.quantity > 0);
});

const hasBatchAllocations = computed(() => {
  return outboundProducts.value.some(item => 
    item.batchAllocations && item.batchAllocations.length > 0
  );
});

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

// 新增：是否有推荐详情
const hasRecommendationDetails = computed(() => {
  return Object.keys(recommendationDetails.value).length > 0;
});

// 新增：计算是否可以自动分配
const canAutoAllocate = computed(() => {
  return formData.warehouseId && 
         outboundProducts.value.length > 0 && 
         outboundProducts.value.some(item => item.quantity > 0) &&
         !isAllocating.value &&
         !isViewMode.value;
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

// 表格行样式
const tableRowClassName = ({ row }) => {
  if (row.isTriggerProduct) {
    return 'trigger-product-row';
  } else if (row.isRecommend) {
    return 'recommend-product-row';
  }
  return '';
};

// 获取出库数量的最大值
const getQuantityMax = (row) => {
  if (isViewMode.value) {
    return Number.MAX_SAFE_INTEGER;
  }
  return row.availableQuantity || 0;
};

// 生成出库单号
const generateOrderNo = () => {
  const date = new Date();
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const random = String(Math.random()).substr(2, 6);
  formData.orderNo = `CK${year}${month}${day}${random}`;
};



// 返回上一页方法
const handleGoBack = () => {
  // 检查是否有未保存的更改
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
      // 用户确认返回
      router.back();
    }).catch(() => {
      // 用户取消返回
    });
  } else {
    // 没有未保存的更改或处于编辑模式，直接返回
    router.back();
  }
};

// 加载可用产品列表
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
      
      // 编辑模式下，更新已有产品的可用库存信息
      if (isEditMode.value && outboundProducts.value.length > 0) {
        outboundProducts.value.forEach(product => {
          const availableProduct = availableProducts.value.find(p => p.productId === product.productId);
          if (availableProduct) {
            console.log('availableProduct', availableProduct);
            product.availableQuantity = availableProduct.availableQuantity;
          }
        });
      }
    }
  } catch (error) {
    console.error('加载可用产品失败:', error);
  }
};


// 新增方法：获取触发产品名称（简化版）
const getTriggerProductName = (productId) => {
  const product = outboundProducts.value.find(p => p.productId === productId) || 
                  availableProducts.value.find(p => p.productId === productId);
  return product ? product.productName : '未知产品';
};

// 新增方法：获取产品规格信息
const getProductSpec = (productId) => {
  const product = outboundProducts.value.find(p => p.productId === productId) || 
                  availableProducts.value.find(p => p.productId === productId);
  if (!product) return '';
  
  const parts = [];
  if (product.spec && product.spec !== '-') parts.push(product.spec);
  if (product.color && product.color !== '-') parts.push(product.color);
  
  return parts.join(' | ') || '无规格';
};

// 加载出库单详情
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

      // 设置附件数据
      if (detailData.attachments && detailData.attachments.length > 0) {
        fileList.value = detailData.attachments.map(att => ({
          name: att.fileName,
          url: att.filePath,
          status: 'success'
        }));
        formData.attachments = detailData.attachments;
      }
      
      // 设置产品数据（关键修改点）
      if (detailData.items && detailData.items.length > 0) {
        outboundProducts.value = detailData.items.map(item => {
          // 从 extension 中获取扩展信息
          const extension = item.extension || {};
          const isTriggerProduct = extension.isTriggerProduct === 0; // 0=是触发产品
          const isRecommend = extension.isRecommendProduct === 0; // 0=是推荐产品
          const triggerProductId = extension.triggerProductId || null;

          // 构建产品对象
          const productData = {
            productId: item.productId,
            productName: item.productName,
            sku: item.sku,
            spec: item.spec || '',
            color: item.color || '',
            unitName: item.unit || '',
            // 关键：确保 quantity 字段正确设置
            quantity: item.quantity,
            price: item.priceUnit ? Number(item.priceUnit) : 0,
            priceUnitUsd: item.priceUnitUsd ? Number(item.priceUnitUsd) : 0,
            remark: item.remark || '',
            batchAllocations: item.batchAllocations || [],
            // 从 availableBatches 中获取可用库存，如果没有则设为0
            // availableQuantity: item.availableBatches ? 
            //   item.availableBatches.reduce((sum, batch) => sum + Number(batch.quantity), 0) : 0,
            availableQuantity: item.quantity * 2,// TODO yang 这里最好是实时库存 如果这里的数量小于 item.quantity，就会导致页面回显出库数量为0
            // 扩展信息
            isTriggerProduct: isTriggerProduct,
            isRecommend: isRecommend,
            triggerProductId: triggerProductId,
            extension: extension,
            // 保存原始价格用于重置
            originalPrice: item.priceUnit ? Number(item.priceUnit) : 0,
            originalPriceUnitUsd: item.priceUnitUsd ? Number(item.priceUnitUsd) : 0,
            historyPrice: null,
            // 记录上次数量用于比较
            lastQuantity: item.quantity ? Number(item.quantity) : 0
          };
          
          console.log(`加载产品 ${item.productName}:`, {
            quantity: productData.quantity,
            price: productData.price,
            isTrigger: productData.isTriggerProduct,
            isRecommend: productData.isRecommend,
            triggerId: productData.triggerProductId
          });
          
          return productData;
        });
      }

      console.log('最终加载的产品数据:', outboundProducts.value);
      
      // 编辑模式下，加载仓库对应的可用产品
      if (formData.warehouseId) {
        await loadAvailableProducts();
      }
      
      // 编辑模式下，为已有的触发产品加载推荐
      await loadRecommendationsForExistingProducts();
      
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

// 为编辑模式下的现有产品加载推荐
const loadRecommendationsForExistingProducts = async () => {
  if (!formData.customerId || !formData.warehouseId) return;
  
  // 重置推荐数据
  groupedRecommendations.value = [];
  recommendationDetails.value = {};
  
  // 为每个触发产品加载推荐
  const triggerProducts = outboundProducts.value.filter(p => p.isTriggerProduct );
  //计算p.quantity.batchAllocations中对象的quantity总和
  //const quantityOfTriggerProducts = triggerProducts.reduce((sum, p) => sum + p.quantity, 0);
  // console.log('触发产品:', triggerProducts);
  console.log('为以下触发产品加载推荐:', triggerProducts.map(p => ({
    name: p.productName,
    quantity: p.quantity,
    id: p.productId
  })));
  
  for (const product of triggerProducts) {
    console.log('触发产品:', product);
    
    // 如果产品有批次分配，从批次分配中计算总数
    let quantityOfTriggerProducts;
    
    if (product.batchAllocations && product.batchAllocations.length > 0) {
        // 从批次分配中计算总数量
        quantityOfTriggerProducts = product.batchAllocations.reduce((sum, allocation) => {
            return sum + (Number(allocation.quantity) || 0);
        }, 0);
        console.log(`触发产品 ${product.productName} 从批次分配计算数量: ${quantityOfTriggerProducts}`);
    } else {
        // 如果没有批次分配，直接使用 quantity 属性
        quantityOfTriggerProducts = Number(product.quantity) || 0;
        console.log(`触发产品 ${product.productName} 从quantity属性获取数量: ${quantityOfTriggerProducts}`);
    }
    console.log(`触发产品 ${product.productName} 最终计算数量: ${quantityOfTriggerProducts}`);
    
    await loadRecommendationsForProduct(product.productId, quantityOfTriggerProducts);
}
  
  // 标记已添加的推荐产品为选中状态
  const recommendProducts = outboundProducts.value.filter(p => p.isRecommend && p.quantity > 0);
  console.log('已添加的推荐产品:', recommendProducts.map(p => ({
    name: p.productName,
    quantity: p.quantity,
    id: p.productId
  })));
  
  // 更新推荐详情数据
  updateRecommendationDetails();
  
  // 在推荐面板中标记已添加的推荐产品
  recommendProducts.forEach(recommendProduct => {
    groupedRecommendations.value.forEach(group => {
      const item = group.items.find(i => i.productId === recommendProduct.productId);
      if (item) {
        item.selected = true;
        // 更新已添加的数量
        item.calculatedQuantity = recommendProduct.quantity;
      }
    });
  });
};


// 仓库变化处理
const handleWarehouseChange = async (warehouseId) => {
  if (warehouseId && formData.customerId) {
    await loadAvailableProducts();
  }
  
  // 编辑模式下不清空已有产品，除非是新建模式
  if (!isEditMode.value) {
    outboundProducts.value = [];
    groupedRecommendations.value = [];
    recommendationDetails.value = {};
  }
};

// 客户变化处理
const handleCustomerChange = async (customerId) => {
  if (customerId && formData.warehouseId) {
    await loadAvailableProducts();
  }
  
  // 编辑模式下不清空已有产品，除非是新建模式
  if (!isEditMode.value) {
    outboundProducts.value = [];
    groupedRecommendations.value = [];
    recommendationDetails.value = {};
  }
};



// 快速搜索处理
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

const clearSearch = () => {
  quickSearch.value = '';
  searchResults.value = [];
};

// 添加产品到表格
const addProductToTable = async (product) => {
  // 检查是否已存在
  const existingProduct = outboundProducts.value.find(p => p.productId === product.productId);
  if (existingProduct) {
    // 如果已存在且数量为0，更新数量为1
    if (existingProduct.quantity === 0) {
      existingProduct.quantity = 1;
      existingProduct.price = product.originalPrice || 0;
      existingProduct.priceUnitUsd = product.originalPriceUnitUsd || 0;
      existingProduct.isTriggerProduct = true;
      existingProduct.isRecommend = false;
      existingProduct.triggerProductId = null;
      
      // 确保有 extension 信息
      existingProduct.extension = existingProduct.extension || {};
      existingProduct.extension.isTriggerProduct = 0; // 0=是触发产品
      existingProduct.extension.isRecommendProduct = 1; // 1=不是推荐产品
      existingProduct.extension.triggerProductId = null;
      
      // 加载推荐
      await loadRecommendationsForProduct(product.productId, 1);
      ElMessage.success(`已更新 ${product.productName} 数量为1`);
    } else {
      ElMessage.warning(`${product.productName} 已存在于出库单中`);
    }
    return;
  }
  
  // 新增产品
  const newProduct = {
    ...product,
    quantity: 1,
    price: product.originalPrice || 0,
    priceUnitUsd: product.originalPriceUnitUsd || 0,
    remark: '',
    batchAllocations: [],
    isTriggerProduct: true,
    isRecommend: false,
    triggerProductId: null,
    // 添加扩展信息
    extension: {
      productId: product.productId,
      isTriggerProduct: 0, // 0=是触发产品
      isRecommendProduct: 1, // 1=不是推荐产品
      triggerProductId: null
    },
    historyPrice: null,
    lastQuantity: 0
  };
  
  outboundProducts.value.push(newProduct);
  
  clearSearch();
  
  ElMessage.success(`已添加 ${product.productName} 到出库单`);
  
  // 加载推荐
  await loadRecommendationsForProduct(product.productId, 1);
  

  // 如果有推荐产品被添加，更新推荐面板中的选择状态
    if (outboundProducts.value.some(p => p.isRecommend && p.quantity > 0)) {
      updateRecommendationDetails();
    }


  nextTick(() => {
    const element = document.querySelector('.product-table');
    if (element) {
      element.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  });
};

// 获取行的触发产品ID
const getTriggerIdForRow = (row) => {
  for (const group of groupedRecommendations.value) {
    if (group.items.includes(row)) {
      return group.triggerProductId;
    }
  }
  return null;
};

// 产品数量变化处理
const handleQuantityChange = async (row, value) => {
  if (value > 0) {
    // 如果是触发产品且数量从0变成有值，查询推荐
    if (row.isTriggerProduct && value > 0 && (!row.lastQuantity || row.lastQuantity === 0)) {
      await loadRecommendationsForProduct(row.productId, value);
    }
    
    // 更新价格
    if (!row.price || row.price === 0) {
      row.price = row.originalPrice || 0;
    }
    if (!row.priceUnitUsd || row.priceUnitUsd === 0) {
      row.priceUnitUsd = row.originalPriceUnitUsd || 0;
    }
    
    // 确保有 extension 信息
    if (!row.extension) {
      row.extension = {
        productId: row.productId,
        isTriggerProduct: row.isTriggerProduct ? 0 : 1,
        isRecommendProduct: row.isRecommend ? 0 : 1,
        triggerProductId: row.triggerProductId || null
      };
    }
    
    // 加载历史价格
    if (!row.historyPrice && formData.customerId) {
      await loadProductHistoryPrice(row);
    }
    
    // 如果是触发产品，更新相关推荐的数量
    if (row.isTriggerProduct) {
      updateRecommendationQuantities(row.productId, value);
    }

    // 更新推荐面板中相关产品的选择状态
    if (row.isRecommend) {
      updateRecommendationSelection(row.productId, value > 0);
    }
  } else {
    // 如果数量设为0，清空相关数据
    row.quantity = 0;
    row.price = 0;
    row.priceUnitUsd = 0;
    row.remark = '';
    row.batchAllocations = [];
    
    // 如果是触发产品，移除相关推荐（但不删除推荐商品）
    if (row.isTriggerProduct) {
      removeRecommendationsForTrigger(row.productId, false); // 不删除推荐商品
    }
  }
  
  row.lastQuantity = value;
};

// 更新推荐产品的数量
const updateRecommendationQuantities = (triggerProductId, quantity) => {
  const group = groupedRecommendations.value.find(g => g.triggerProductId === triggerProductId);
  if (group) {
    group.items.forEach(item => {
      if (item.quantityType === 2) {
        item.calculatedQuantity = calculateRecommendedQuantity(item, triggerProductId, quantity);
      }
    });
  }
  
  // 更新推荐详情
  updateRecommendationDetails();
};

// 修改 calculateRecommendedQuantity 方法
const calculateRecommendedQuantity = (recommendation, triggerProductId, triggerQuantity) => {
  const quantity = triggerQuantity !== undefined 
    ? triggerQuantity 
    : getProductQuantity(triggerProductId);
  
  if (quantity <= 0) return 0;
  
  if (recommendation.quantityType === 1) {
    return Number(recommendation.quantityValue) || 0;
  } else {
    const ratio = Number(recommendation.quantityValue) || 0;
    const calculated = quantity * ratio;
    return Math.round(calculated);
  }
};

// 添加辅助方法获取触发产品数量
const getTriggerProductQuantity = (triggerProductId) => {
  const product = outboundProducts.value.find(p => p.productId === triggerProductId);
  return product ? product.quantity : 0;
};

// 刷新触发产品的推荐
const refreshTriggerRecommendations = async (productId) => {
  const product = outboundProducts.value.find(p => p.productId === productId);
  if (product && product.quantity > 0) {
    await loadRecommendationsForProduct(productId, product.quantity);
    ElMessage.success('已刷新推荐');
  } else {
    ElMessage.warning('请先设置触发产品的数量');
  }
};

// 刷新推荐
const refreshRecommendations = async (triggerProductId) => {
  const product = outboundProducts.value.find(p => p.productId === triggerProductId);
  if (product && product.quantity > 0) {
    await loadRecommendationsForProduct(triggerProductId, product.quantity);
    ElMessage.success('已重新获取推荐');
  } else {
    ElMessage.warning('触发产品数量为0，无法获取推荐');
  }
};

// 加载推荐
const loadRecommendationsForProduct = async (productId, quantity) => {
  console.log('加载推荐,loadRecommendationsForProduct触发产品ID:', productId, '数量:', quantity, 'formData:', formData);
  if (!formData.customerId || !formData.warehouseId) return;
  
  try {
    const requestData = {
      customerId: formData.customerId,
      warehouseId: formData.warehouseId,
      productId: productId,
      quantity: quantity,
      applyScene: 1
    };
    
    console.log('推荐接口响应,请求参数:', requestData);
    const res = await post('/api/auth/recommend/queryRuleItems', requestData);
    
    console.log('推荐接口响应:', res);
    
    if (res && res.recommendations && res.recommendations.length > 0) {
      // 过滤掉已经在出库单中且数量大于0的产品
      const existingProductIds = outboundProducts.value
        .filter(p => p.quantity > 0 && p.productId !== productId) // 排除当前触发产品本身
        .map(p => p.productId);
      
      const recommendations = res.recommendations.map(item => {
        const productInfo = availableProducts.value.find(p => p.productId === item.productId);
        const availableQuantity = productInfo ? productInfo.availableQuantity : 0;
        
        // 检查是否已添加（编辑模式下，已存在的推荐产品应该被标记为已选中）
        const existingProduct = outboundProducts.value.find(p => 
          p.productId === item.productId && p.quantity > 0 && p.isRecommend
        );
        const isSelected = !!existingProduct;
        const actualQuantity = isSelected ? existingProduct.quantity : 0;
        
        const calculatedQuantity = calculateRecommendedQuantity({
          quantityType: item.quantityType,
          quantityValue: item.quantityValue
        }, productId, quantity);
        
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
          triggerProductId: productId, // 记录触发产品ID
          // 如果是编辑模式且已存在，使用实际数量
          actualQuantity: actualQuantity,
          // 记录这个推荐是否为当前触发产品的推荐
          isCurrentTriggerRecommendation: true
        };
      });

      // 重要：不移除过滤逻辑，保留所有推荐产品
      // .filter(item => !existingProductIds.includes(item.productId));
      
      console.log('处理后的推荐数据:', recommendations);
      
      if (recommendations.length > 0) {
        const existingGroupIndex = groupedRecommendations.value.findIndex(
          g => g.triggerProductId === productId
        );
        
        if (existingGroupIndex >= 0) {
          groupedRecommendations.value[existingGroupIndex].items = recommendations;
          groupedRecommendations.value[existingGroupIndex].hasValidItems = recommendations.some(item => item.availableQuantity > 0);
        } else {
          groupedRecommendations.value.push({
            triggerProductId: productId,
            items: recommendations,
            hasValidItems: recommendations.some(item => item.availableQuantity > 0)
          });
        }
        
        // 更新推荐详情
        updateRecommendationDetails();
        
        // 编辑模式下不自动应用推荐，只展示
        if (!isEditMode.value) {
          applyRequiredRecommendations(productId);
        }
        
        ElMessage.success(`发现 ${recommendations.length} 条推荐规则`);
      }
    } else {
      console.log('未找到推荐规则或推荐列表为空', res);
    }
  } catch (error) {
    console.error('加载推荐失败:', error);
    ElMessage.error('获取推荐规则失败');
  }
};

// 新增：更新推荐详情数据
const updateRecommendationDetails = () => {
  recommendationDetails.value = {};
  
  groupedRecommendations.value.forEach(group => {
    group.items.forEach(item => {
      if (!recommendationDetails.value[item.productId]) {
        recommendationDetails.value[item.productId] = [];
      }
      
      const recommendedQuantity = calculateRecommendedQuantity(item, group.triggerProductId);
      
      // 检查是否已存在相同的触发产品
      const existingDetail = recommendationDetails.value[item.productId].find(
        detail => detail.triggerProductId === group.triggerProductId
      );
      
      if (existingDetail) {
        existingDetail.recommendedQuantity = recommendedQuantity;
      } else {
        recommendationDetails.value[item.productId].push({
          triggerProductId: group.triggerProductId,
          recommendedQuantity: recommendedQuantity
        });
      }
    });
  });
  
  console.log('推荐详情数据:', recommendationDetails.value);
};

// 新增：获取产品推荐详情
const getRecommendationDetails = (productId) => {
  return recommendationDetails.value[productId] || [];
};

// 新增：获取产品总推荐数量
const getTotalRecommendedQuantity = (productId) => {
  const details = recommendationDetails.value[productId];
  if (!details || details.length === 0) return 0;
  
  return details.reduce((sum, detail) => sum + (detail.recommendedQuantity || 0), 0);
};

// 获取产品可用数量
const getProductAvailableQuantity = (productId) => {
  const product = availableProducts.value.find(p => p.productId === productId);
  return product ? product.availableQuantity : 0;
};

// 应用必选推荐
const applyRequiredRecommendations = (triggerProductId) => {
  const group = groupedRecommendations.value.find(g => g.triggerProductId === triggerProductId);
  if (!group) return;
  
  let addedCount = 0;
  group.items.forEach(item => {
    // 只添加必选且未选中（不在出库单中）的产品
    if (item.isRequired && !item.selected && item.availableQuantity > 0) {
      // 先检查产品是否已经在出库单中（可能来自其他触发产品的推荐）
      const existingProduct = outboundProducts.value.find(p => 
        p.productId === item.productId && p.quantity > 0 && p.isRecommend
      );
      
      if (!existingProduct) {
        addRecommendationToOrder(item);
        addedCount++;
      } else {
        // 如果已经存在，只需标记为已选中
        item.selected = true;
        // 更新实际数量
        item.actualQuantity = existingProduct.quantity;
      }
    }
  });
  
  if (addedCount > 0) {
    ElMessage.success(`已自动添加 ${addedCount} 个必选推荐商品`);
  }
};

// 添加推荐产品到订单
const addRecommendationToOrder = (recommendation) => {
  const product = availableProducts.value.find(p => p.productId === recommendation.productId);
  if (!product) {
    ElMessage.warning(`产品 ${recommendation.productName} 不在当前仓库中`);
    return;
  }
  
  const existingProduct = outboundProducts.value.find(p => p.productId === recommendation.productId);
  if (existingProduct) {
    if (existingProduct.quantity === 0) {
      const quantity = recommendation.calculatedQuantity || 1;
      existingProduct.quantity = Math.min(quantity, product.availableQuantity);
      existingProduct.price = product.originalPrice || 0;
      existingProduct.priceUnitUsd = product.originalPriceUnitUsd || 0;
      existingProduct.isRecommend = true;
      existingProduct.isTriggerProduct = false;
      existingProduct.triggerProductId = recommendation.triggerProductId;
      
      // 确保有 extension 信息
      existingProduct.extension = existingProduct.extension || {};
      existingProduct.extension.isTriggerProduct = 1; // 1=不是触发产品
      existingProduct.extension.isRecommendProduct = 0; // 0=是推荐产品
      existingProduct.extension.triggerProductId = recommendation.triggerProductId || 0;
      
      recommendation.selected = true;
      
      ElMessage.success(`已添加 ${recommendation.productName} 到出库单`);
    } else {
      ElMessage.warning(`${recommendation.productName} 已存在于出库单中`);
    }
    return;
  }
  
  const quantity = recommendation.calculatedQuantity || 1;
  const actualQuantity = Math.min(quantity, product.availableQuantity);
  
  if (actualQuantity <= 0) {
    ElMessage.warning(`${recommendation.productName} 库存不足，无法添加`);
    return;
  }
  
  const newProduct = {
    ...product,
    quantity: actualQuantity,
    price: product.originalPrice || 0,
    priceUnitUsd: product.originalPriceUnitUsd || 0,
    // remark: recommendation.remark || '',
    batchAllocations: [],
    isTriggerProduct: false,
    isRecommend: true,
    triggerProductId: recommendation.triggerProductId,
    // 添加扩展信息
    extension: {
      productId: recommendation.productId,
      isTriggerProduct: 1, // 1=不是触发产品
      isRecommendProduct: 0, // 0=是推荐产品
      triggerProductId: recommendation.triggerProductId || 0
    },
    historyPrice: null
  };
  
  outboundProducts.value.push(newProduct);
  
  recommendation.selected = true;
  
  ElMessage.success(`已添加 ${recommendation.productName} 到出库单`);
  
  // 更新所有相关的推荐项状态
  updateRecommendationSelection(recommendation.productId, true);
};

// 处理推荐切换
const handleRecommendationToggle = (recommendation) => {
  if (recommendation.selected) {
    addRecommendationToOrder(recommendation);
  } else {
    // 只从出库单中移除，但不从推荐面板中移除
    const existingProduct = outboundProducts.value.find(p => 
      p.productId === recommendation.productId && p.quantity > 0 && p.isRecommend
    );
    
    if (existingProduct) {
      // 如果是来自多个触发产品的推荐，只设置数量为0，不删除
      const triggerCount = Object.values(recommendationDetails.value)
        .filter(details => details.some(d => d.triggerProductId === recommendation.triggerProductId))
        .length;
      
      if (triggerCount > 1) {
        // 来自多个触发产品，只清空数量
        existingProduct.quantity = 0;
        existingProduct.price = 0;
        existingProduct.priceUnitUsd = 0;
        existingProduct.remark = '';
        existingProduct.batchAllocations = [];
        
        ElMessage.success(`已清空 ${recommendation.productName} 的数量`);
        
        // 更新推荐面板中的选择状态
        updateRecommendationSelection(recommendation.productId, false);
      } else {
        // 只来自当前触发产品，可以删除
        removeProductFromTableByProductId(recommendation.productId);
      }
    }
  }
};

// 移除触发产品的推荐（可选是否删除推荐商品）
const removeRecommendationsForTrigger = (triggerProductId, deleteRecommendProducts = false) => {
  const index = groupedRecommendations.value.findIndex(g => g.triggerProductId === triggerProductId);
  if (index >= 0) {
    if (deleteRecommendProducts) {
      // 删除推荐商品
      groupedRecommendations.value[index].items.forEach(item => {
        if (item.selected) {
          removeProductFromTableByProductId(item.productId);
        }
      });
    }
    
    groupedRecommendations.value.splice(index, 1);
  }
  
  // 更新推荐详情
  updateRecommendationDetails();
};


// 更新指定产品的所有推荐项的选择状态
const updateRecommendationSelection = (productId, isSelected) => {
  groupedRecommendations.value.forEach(group => {
    const item = group.items.find(i => i.productId === productId);
    if (item) {
      item.selected = isSelected;
      
      // 如果是查看模式或者编辑模式，更新实际数量
      if (isViewMode.value || isEditMode.value) {
        const existingProduct = outboundProducts.value.find(p => 
          p.productId === productId && p.quantity > 0 && p.isRecommend
        );
        if (existingProduct) {
          item.actualQuantity = existingProduct.quantity;
        } else if (!isSelected) {
          item.actualQuantity = 0;
        }
      }
    }
  });
};

// 通过产品ID移除产品
const removeProductFromTableByProductId = (productId) => {
  const index = outboundProducts.value.findIndex(p => p.productId === productId && p.isRecommend);
  if (index >= 0) {
    outboundProducts.value.splice(index, 1);
    
    // 从推荐详情中移除
    delete recommendationDetails.value[productId];
    
    // 重新计算推荐详情
    updateRecommendationDetails();
  }
  
  // 更新所有相关的推荐项状态
  updateRecommendationSelection(productId, false);
};

// 应用所有推荐
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

// 应用组推荐
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


// 在组件中使用
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

// 获取截断后的名称（最多25个字符）
const getTruncatedProductName = (productId) => {
  const fullName = getProductFullName(productId);
  if (fullName.length > 25) {
    return fullName.substring(0, 25) + '...';
  }
  return fullName;
};


// 获取产品名称
const getProductName = (productId) => {
  const product = outboundProducts.value.find(p => p.productId === productId) || 
                  availableProducts.value.find(p => p.productId === productId);
                  console.log("dsjnfdskanksf",product);
  return product ? `${product.productName} - ${product.spec ? product.spec : '无规格'} - ${product.color ? product.color : '无颜色'}` : '未知产品';
};

// 获取产品数量
const getProductQuantity = (productId) => {
  const product = outboundProducts.value.find(p => p.productId === productId);
  return product ? product.quantity : 0;
};

// 关闭推荐面板
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

// 产品选择对话框相关
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
  nextTick(() => {
    filteredSelectorProducts.value = [...filteredSelectorProducts.value];
  });
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
  for (const product of selectedProductsForSelector.value) {
    const existingProduct = outboundProducts.value.find(p => p.productId === product.productId);
    
    if (!existingProduct) {
      // 新增产品
      const newProduct = {
        ...product,
        quantity: 1,
        price: product.originalPrice || 0,
        priceUnitUsd: product.originalPriceUnitUsd || 0,
        remark: '',
        batchAllocations: [],
        isTriggerProduct: true,
        isRecommend: false,
        historyPrice: null,
        lastQuantity: 0,
        triggerProductId: null
      };
      
      outboundProducts.value.push(newProduct);
      addedCount++;
      
      // 加载推荐
      await loadRecommendationsForProduct(product.productId, 1);
    } else if (existingProduct.quantity === 0) {
      // 已存在但数量为0，更新数量
      existingProduct.quantity = 1;
      existingProduct.price = product.originalPrice || 0;
      existingProduct.priceUnitUsd = product.originalPriceUnitUsd || 0;
      existingProduct.isTriggerProduct = true;
      existingProduct.isRecommend = false;
      existingProduct.triggerProductId = null;
      addedCount++;
      
      // 加载推荐
      await loadRecommendationsForProduct(product.productId, 1);
    }
  }
  
  showProductSelector.value = false;
  
  if (addedCount > 0) {
    ElMessage.success(`成功添加 ${addedCount} 个产品到出库单`);
    
    nextTick(() => {
      const element = document.querySelector('.product-table');
      if (element) {
        element.scrollIntoView({ behavior: 'smooth', block: 'start' });
      }
    });
  }
};

// 从表格中删除产品
const removeProductFromTable = (row, index) => {
  ElMessageBox.confirm(
    `确定要删除 ${row.productName} 吗？`,
    '确认删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    if (row.isTriggerProduct) {
      removeRecommendationsForTrigger(row.productId, true);
    } else if (row.isRecommend) {
      // 从推荐详情中移除
      delete recommendationDetails.value[row.productId];
    }
    
    outboundProducts.value.splice(index, 1);
    ElMessage.success('删除成功');
  });
};

// 批量操作
const handleBatchCommand = async (command) => {
  switch (command) {
    case 'clearAll':
      if (selectedProducts.value.length > 0) {
        selectedProducts.value.forEach(row => {
          row.quantity = 0;
          row.price = 0;
          row.priceUnitUsd = 0;
          row.remark = '';
          row.batchAllocations = [];
          if (row.isRecommend) {
            row.isRecommend = false;
            // 从推荐详情中移除
            delete recommendationDetails.value[row.productId];
          }
        });
        ElMessage.success(`已清空 ${selectedProducts.value.length} 个产品的数量`);
      } else {
        const count = outboundProducts.value.filter(p => p.quantity > 0).length;
        outboundProducts.value.forEach(product => {
          product.quantity = 0;
          product.price = 0;
          product.priceUnitUsd = 0;
          product.remark = '';
          product.batchAllocations = [];
          if (product.isRecommend) {
            product.isRecommend = false;
            // 从推荐详情中移除
            delete recommendationDetails.value[product.productId];
          }
        });
        if (count > 0) {
          ElMessage.success(`已清空 ${count} 个产品的数量`);
        }
      }
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
        outboundProducts.value.forEach(product => {
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
        outboundProducts.value.forEach(product => {
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

const handleRowClick = (row) => {
  // 行点击事件
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

// 加载产品历史价格
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

// ==================== 自动分配相关方法 ====================

// 检查是否可以自动分配
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

// 自动全部分配
const handleAutoAllocateAll = async () => {
  try {
    // 检查分配资格
    if (!checkAutoAllocationEligibility()) {
      return;
    }
    
    // 编辑模式下确认
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
    
    // 设置分配状态
    autoAllocating.value = true;
    isAllocating.value = true;
    allocationProgress.value = 0;
    currentAllocationIndex.value = 0;
    autoAllocationResult.value = null;
    
    // 获取需要分配的产品
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
    
    console.log(`开始为 ${totalAllocationCount.value} 个产品进行自动分配`);
    
    // 依次为每个产品分配批次
    for (let i = 0; i < productsToAllocate.length; i++) {
      const product = productsToAllocate[i];
      currentAllocationIndex.value = i + 1;
      allocationProgress.value = Math.round((currentAllocationIndex.value / totalAllocationCount.value) * 100);
      
      console.log(`分配产品 ${i+1}/${totalAllocationCount.value}: ${product.productName} (数量: ${product.quantity})`);
      
      try {
        await autoAllocateProduct(product);
        console.log(`产品 ${product.productName} 分配成功`);
      } catch (error) {
        console.error(`产品 ${product.productName} 分配失败:`, error);
        ElMessage.error(`产品 ${product.productName} 分配失败: ${error.message || '未知错误'}`);
      }
      
      // 每个产品分配后稍作延迟，避免请求过快
      if (i < productsToAllocate.length - 1) {
        await new Promise(resolve => setTimeout(resolve, 300));
      }
    }
    
    // 分配完成
    autoAllocating.value = false;
    isAllocating.value = false;
    allocationProgress.value = 100;
    
    // 显示分配结果
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

// 为单个产品自动分配批次
const autoAllocateProduct = async (product) => {
  if (!product.productId || !formData.warehouseId) {
    throw new Error('产品信息或仓库信息不完整');
  }
  
  // 加载批次信息
  await loadBatchInfoForProduct(product.productId, formData.warehouseId, product);
  
  if (!product.availableBatches || product.availableBatches.length === 0) {
    throw new Error('没有找到可用批次');
  }
  
  const targetQuantity = product.quantity;
  let remainingQuantity = targetQuantity;
  const allocations = [];
  
  // 按先进先出（FIFO）排序批次
  const sortedBatches = [...product.availableBatches].sort((a, b) => {
    if (a.productionDate && b.productionDate) {
      return new Date(a.productionDate) - new Date(b.productionDate);
    }
    if (a.createdAt && b.createdAt) {
      return new Date(a.createdAt) - new Date(b.createdAt);
    }
    return 0;
  });
  
  // 遍历批次进行分配
  for (const batch of sortedBatches) {
    if (remainingQuantity <= 0) break;
    
    const batchTotalAvailable = batch.quantity || 0;
    if (batchTotalAvailable <= 0) continue;
    
    // 如果有货架信息，按货架分配
    if (batch.shelfList && batch.shelfList.length > 0) {
      // 按货架可用数量排序
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
      // 没有货架信息，直接按批次分配
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
  
  // 检查是否分配完成
  if (remainingQuantity > 0) {
    throw new Error(`库存不足，仍有 ${remainingQuantity} 个无法分配`);
  }
  
  // 更新产品的批次分配
  product.batchAllocations = allocations;
  
  // 记录分配详情
  console.log(`产品 ${product.productName} 分配结果:`, {
    需要数量: targetQuantity,
    分配批次数: allocations.length,
    分配明细: allocations.map(a => ({
      批次: a.batchNo,
      货架: a.shelfName,
      数量: a.quantity
    }))
  });
  
  return allocations;
};

// 批量自动分配
const batchAutoAllocate = async (products) => {
  const results = {
    success: 0,
    failed: 0,
    details: []
  };
  
  for (const product of products) {
    try {
      await autoAllocateProduct(product);
      results.success++;
      results.details.push({
        productId: product.productId,
        productName: product.productName,
        success: true,
        message: '分配成功'
      });
    } catch (error) {
      results.failed++;
      results.details.push({
        productId: product.productId,
        productName: product.productName,
        success: false,
        message: error.message || '分配失败'
      });
    }
  }
  
  return results;
};

// ==================== 自动分配相关方法结束 ====================

// 批次分配相关方法
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

// 打开批次分配对话框 - 修改以支持自动分配
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

  // 如果已经有分配数据，使用现有数据
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
    // 尝试自动分配（仅填充对话框，不保存）
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

// 加载批次信息
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

// 计算自动分配方案（用于对话框预览）
const calculateAutoAllocation = async (targetQuantity, batches) => {
  let remainingQuantity = targetQuantity;
  const allocations = [];
  
  // 按先进先出排序
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

// 确认批次分配
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

// 提交前检查分配状态
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

// 导出当前数据
const exportCurrentData = () => {
  const exportData = {
    orderInfo: {
      orderNo: formData.orderNo,
      warehouseId: formData.warehouseId,
      customerId: formData.customerId,
      expectedDate: formData.expectedDate
    },
    products: filteredProducts.value
      .filter(p => p.quantity > 0)
      .map(p => ({
        sku: p.sku,
        productName: p.productName,
        spec: p.spec,
        quantity: p.quantity,
        price: p.price,
        priceUnitUsd: p.priceUnitUsd,
        isTriggerProduct: p.isTriggerProduct,
        isRecommend: p.isRecommend,
        triggerProductId: p.triggerProductId,
        remark: p.remark,
        recommendationDetails: recommendationDetails.value[p.productId] || []
      })),
    summary: {
      productCount: filteredProducts.value.filter(p => p.quantity > 0).length,
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
    // 获取产品ID列表
    const productIds = filteredProducts.value
      .filter(p => p.quantity > 0)
      .map(p => p.productId)
      .filter(id => id);

    const response = await axios.post('/api/auth/outbound/exportExcel', {
      warehouseId: formData.warehouseId,
      productIds: productIds
    }, {
      headers: {
        ...accessHeader(),
        'Content-Type': 'application/json'
      },
      responseType: 'blob'
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

    ElMessage.info('开始导入数据，请稍候...');

    const result = await post('/api/auth/outbound/importOutboundSaleQuantity', fd);

    if (result) {
      importResult.value = result;
      ElMessage.success(`导入成功！`);
      
      if (result && Array.isArray(result)) {
        result.forEach(importedItem => {
          const product = availableProducts.value.find(p => 
            p.productId === importedItem.productId || p.sku === importedItem.sku
          );
          
          if (product) {
            const existingProduct = outboundProducts.value.find(p => p.productId === product.productId);
            
            if (existingProduct) {
              existingProduct.quantity = importedItem.quantity || 0;
              existingProduct.price = importedItem.price || product.originalPrice || 0;
              existingProduct.priceUnitUsd = importedItem.priceUnitUsd || product.originalPriceUnitUsd || 0;
              existingProduct.remark = importedItem.remark || '';
            } else {
              outboundProducts.value.push({
                ...product,
                quantity: importedItem.quantity || 0,
                price: importedItem.price || product.originalPrice || 0,
                priceUnitUsd: importedItem.priceUnitUsd || product.originalPriceUnitUsd || 0,
                remark: importedItem.remark || '',
                batchAllocations: [],
                isTriggerProduct: true,
                isRecommend: false,
                historyPrice: null,
                triggerProductId: null
              });
            }
            
            // 加载推荐
            if (importedItem.quantity > 0) {
              loadRecommendationsForProduct(product.productId, importedItem.quantity || 0);
            }
          }
        });
        
        importDialog.visible = false;
        currentFile.value = null;
        importResult.value = null;
        
        ElMessage.success('导入完成');
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

// 查看历史价格
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
  
  // 检查批次分配
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

// 准备提交数据
const prepareSubmitData = () => {
  const items = outboundProducts.value
    .filter(item => item.quantity > 0)
    .map(item => {
      // 构建扩展信息
      // 优先使用已存在的 extension，否则新建
      const extension = item.extension || {
        productId: item.productId,
        isTriggerProduct: item.isTriggerProduct ? 0 : 1, // 0=是触发产品, 1=不是触发产品
        isRecommendProduct: item.isRecommend ? 0 : 1, // 0=是推荐产品, 1=不是推荐产品
        triggerProductId: item.triggerProductId || 0, // 关联的触发产品ID，没有则为0
        remark: item.remark || ''
      };
      
      // 如果 extension 存在但需要更新字段
      if (item.extension) {
        extension.isTriggerProduct = item.isTriggerProduct ? 0 : 1;
        extension.isRecommendProduct = item.isRecommend ? 0 : 1;
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
        remark: item.remark || '',
        // 保留原来的字段，用于前端显示
        isTriggerProduct: item.isTriggerProduct || false,
        isRecommend: item.isRecommend || false,
        triggerProductId: item.triggerProductId || null,
        // 扩展信息
        extension: extension
      };
    });
  
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


onMounted(() => {
  if (isEditMode.value) {
    loadOutboundDetail(route.params.id);
  } else {
    generateOrderNo();
  }
  loadWarehouseList();
  loadCustomerList();
});

// 监听路由变化
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

// 监听仓库和客户变化
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

/* 产品添加区域 */
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

/* 搜索结果显示 */
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

/* 推荐面板 */
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

/* 推荐分组 */
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

/* 推荐产品表格 */
.recommendation-items-table {
  margin-top: 12px;
}

.recommendation-items-table :deep(.el-table__header-wrapper) {
  background-color: #e6f7ff;
}

.recommendation-items-table :deep(.el-table__body-wrapper) {
  background-color: #ffffff;
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
  gap: 20px;
}

.section-header-left h3 {
  margin: 0;
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.product-type-filter {
  display: flex;
  align-items: center;
  gap: 10px;
}

.product-type-filter :deep(.el-checkbox) {
  margin-right: 15px;
}

.header-right-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

/* 自动分配状态样式 */
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

/* 自动分配结果提示 */
.auto-allocation-result {
  margin: 16px 0;
}

/* 产品表格样式 */
.product-table {
  margin-bottom: 16px;
}

:deep(.trigger-product-row) {
  background-color: #f0f9ff !important;
}

:deep(.trigger-product-row:hover > td) {
  background-color: #e6f7ff !important;
}

:deep(.recommend-product-row) {
  background-color: #f9f0ff !important;
}

:deep(.recommend-product-row:hover > td) {
  background-color: #f2e6ff !important;
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

.recommend-source-tag {
  height: 20px;
  line-height: 18px;
  background-color: #f6ffed;
  border-color: #b7eb8f;
  color: #52c41a;
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

/* 库存样式 */
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

/* 新增：推荐数量样式 */
.recommendation-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
  align-items: flex-start;
}

.recommendation-detail-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
}

.trigger-product-name {
  color: #606266;
  min-width: 60px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.recommended-quantity {
  color: #1890ff;
  font-weight: 500;
}

.recommendation-total {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 4px;
  padding-top: 4px;
  border-top: 1px dashed #ebeef5;
  font-size: 12px;
  font-weight: bold;
}

.total-quantity {
  color: #f56c6c;
}

/* 空产品提示 */
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

/* 产品选择对话框 */
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

.selector-table .usd-price {
  font-size: 11px;
  color: #909399;
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

/* 统计信息 */
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

/* 底部统计 */
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

/* 批次分配对话框样式 */
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

/* 触发产品来源行样式 */
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

/* 查看模式下的推荐样式 */
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

.quantity-view {
  display: inline-block;
  width: 100%;
  height: 32px;
  line-height: 32px;
  text-align: center;
  font-weight: 500;
  color: #303133;
}

/* 推荐产品来源提示 */
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

/* 已添加的推荐行样式 */
:deep(.recommendation-items-table .row-added) {
  background-color: #f0f9eb !important;
}

:deep(.recommendation-items-table .row-added:hover > td) {
  background-color: #e6f7e6 !important;
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
  
  .recommendation-detail-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 2px;
  }
}


/* 返回按钮区域样式 */
.back-header {
  margin-bottom: 16px;
  padding: 0 4px;
}

.back-btn {
  padding: 10px 16px;  /* 增加内边距 */
  font-size: 16px;     /* 增大字体 */
  font-weight: 500;    /* 增加字重 */
  color: #409EFF;
}

.back-btn:hover {
  background-color: #ecf5ff;
  border-radius: 4px;
}

.back-btn i {
  margin-right: 6px;  /* 增加图标和文字间距 */
  font-size: 18px;    /* 增大图标 */
}

/* 调整整体容器，为返回按钮腾出空间 */
.inbound-create-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

/* 响应式调整 */
@media (max-width: 768px) {
  .back-header {
    margin-bottom: 12px;
  }
  
  .back-btn {
    padding: 8px 14px;
    font-size: 15px;
  }
  
  .back-btn i {
    font-size: 16px;
    margin-right: 4px;
  }
}
</style>