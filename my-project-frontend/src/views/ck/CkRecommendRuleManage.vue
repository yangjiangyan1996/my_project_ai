<template>
  <div class="recommend-container">
    <!-- 页面标题和操作栏 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">智能推荐规则管理</h1>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>工作台</el-breadcrumb-item>
          <el-breadcrumb-item>智能配置</el-breadcrumb-item>
          <el-breadcrumb-item>推荐规则</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增规则
        </el-button>
        <el-button @click="handleRefresh">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
        <el-button @click="handleEnableBatch" :disabled="selectedRows.length === 0">
          <el-icon><CircleCheck /></el-icon>
          批量启用
        </el-button>
        <el-button @click="handleDisableBatch" :disabled="selectedRows.length === 0">
          <el-icon><CircleClose /></el-icon>
          批量禁用
        </el-button>
      </div>
    </div>

    <!-- 筛选条件 -->
    <div class="filter-container">
      <el-form :model="filterForm" inline>
        <el-form-item label="规则名称">
          <el-input
            v-model="filterForm.ruleName"
            placeholder="请输入规则名称"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="触发产品">
          <el-select
            v-model="filterForm.triggerProductId"
            placeholder="请选择触发产品"
            clearable
            filterable
            remote
            :remote-method="searchTriggerProducts"
            :loading="productLoading"
            style="width: 220px"
          >
            <el-option
              v-for="product in triggerProductOptions"
              :key="product.id"
              :label="`${product.name} (${product.sku})`"
              :value="product.id"
            >
              <span style="float: left">{{ product.name }}</span>
              <span style="float: right; color: #8492a6; font-size: 13px">{{ product.sku }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="客户">
          <el-select
            v-model="filterForm.customerId"
            placeholder="请选择客户"
            clearable
            filterable
            style="width: 180px"
          >
            <el-option label="所有客户" :value="null" />
            <el-option
              v-for="customer in customerList"
              :key="customer.id"
              :label="customer.customerName"
              :value="customer.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="应用场景">
          <el-select v-model="filterForm.applyScene" placeholder="请选择场景" clearable style="width: 140px">
            <el-option label="全部" :value="null" />
            <el-option label="出库推荐" :value="1" />
            <el-option label="采购建议" :value="2" />
            <el-option label="上架建议" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="规则类型">
          <el-select v-model="filterForm.ruleType" placeholder="请选择类型" clearable style="width: 140px">
            <el-option label="全部" :value="null" />
            <el-option label="手动规则" :value="1" />
            <el-option label="自动规则" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="请选择状态" clearable style="width: 120px">
            <el-option label="全部" :value="null" />
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 推荐规则列表 -->
    <el-card class="table-card">
      <el-table
        :data="ruleList"
        v-loading="loading"
        style="width: 100%"
        empty-text="暂无推荐规则数据"
        @selection-change="handleSelectionChange"
        row-key="id"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="ruleCode" label="规则编码" width="120" />
        <el-table-column prop="ruleName" label="规则名称" min-width="150" />
        <el-table-column label="触发产品" min-width="200">
          <template #default="scope">
            <div>{{ scope.row.triggerProductName }}</div>
            <div class="text-muted">sku: {{ scope.row.triggerProductSku }}</div>
            <div class="text-muted">spec: {{ scope.row.triggerProductSpec }}</div>
            <div class="text-muted">color: {{ scope.row.triggerProductColor }}</div>
          </template>
        </el-table-column>
        <el-table-column label="触发数量" width="100" align="center">
          <template #default="scope">
            {{ scope.row.triggerMinQuantity }}
            <span v-if="scope.row.triggerMaxQuantity"> - {{ scope.row.triggerMaxQuantity }}</span>
          </template>
        </el-table-column>
        <el-table-column label="客户" width="150">
          <template #default="scope">
            {{ scope.row.customerName || '所有客户' }}
          </template>
        </el-table-column>
        <el-table-column label="应用场景" width="100" align="center">
          <template #default="scope">
            <el-tag :type="getSceneTagType(scope.row.applyScene)" size="small">
              {{ getSceneText(scope.row.applyScene) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="规则类型" width="90" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.ruleType === 1 ? '' : 'success'" size="small">
              {{ scope.row.ruleType === 1 ? '手动' : '自动' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="推荐项" width="100" align="center">
          <template #default="scope">
            <el-button type="text" @click="viewRuleItems(scope.row)">
              {{ scope.row.itemCount || 0 }} 个
            </el-button>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="80" align="center" sortable>
          <template #default="scope">
            <el-tag size="small">{{ scope.row.priority }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="confidence" label="置信度" width="80" align="center" v-if="showConfidence">
          <template #default="scope">
            {{ (scope.row.confidence * 100).toFixed(1) }}%
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="scope">
            <el-switch
              v-model="scope.row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button type="primary" link @click="handleEdit(scope.row)">
              编辑
            </el-button>
            <el-button type="info" link @click="viewRuleDetail(scope.row)">
              详情
            </el-button>
            <el-button type="danger" link @click="handleDelete(scope.row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
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

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="1000px"
      :before-close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="120px"
        label-position="right"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="规则编码" prop="ruleCode">
              <el-input
                v-model="form.ruleCode"
                placeholder="自动生成或手动输入"
                :disabled="isEditMode"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="规则名称" prop="ruleName">
              <el-input
                v-model="form.ruleName"
                placeholder="请输入规则名称"
                maxlength="100"
                show-word-limit
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="触发产品" prop="triggerProductId">
              <el-select
                v-model="form.triggerProductId"
                placeholder="请选择触发产品"
                style="width: 100%"
                filterable
                remote
                :remote-method="searchTriggerProducts"
                :loading="productLoading"
                @change="handleTriggerProductChange"
              >
                <el-option
                  v-for="product in triggerProductOptions"
                  :key="product.id"
                  :label="`${product.name} (${product.sku})`"
                  :value="product.id"
                >
                  <span style="float: left">{{ product.name }}</span>
                  <span style="float: right; color: #8492a6; font-size: 13px">
                    {{ product.sku }} | {{ product.spec }} | {{ product.color }}
                  </span>
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="适用客户">
              <el-select
                v-model="form.customerId"
                placeholder="请选择客户（为空时适用所有客户）"
                style="width: 100%"
                filterable
                clearable
                @change="handleCustomerChange"
              >
                <el-option label="所有客户" :value="null" />
                <el-option
                  v-for="customer in customerList"
                  :key="customer.id"
                  :label="customer.customerName"
                  :value="customer.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="最小触发数量" prop="triggerMinQuantity">
              <el-input-number
                v-model="form.triggerMinQuantity"
                :min="0"
                :precision="2"
                :step="1"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="最大触发数量">
              <el-input-number
                v-model="form.triggerMaxQuantity"
                :min="0"
                :precision="2"
                :step="1"
                controls-position="right"
                style="width: 100%"
                placeholder="不限制"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="优先级" prop="priority">
              <el-input-number
                v-model="form.priority"
                :min="1"
                :max="999"
                controls-position="right"
                style="width: 100%"
              />
              <div class="form-tip">数字越小优先级越高</div>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="应用场景" prop="applyScene">
              <el-select v-model="form.applyScene" placeholder="请选择应用场景" style="width: 100%">
                <el-option label="出库推荐" :value="1" />
                <el-option label="采购建议" :value="2" />
                <el-option label="上架建议" :value="3" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="规则类型" prop="ruleType">
              <el-select v-model="form.ruleType" placeholder="请选择规则类型" style="width: 100%" @change="handleRuleTypeChange">
                <el-option label="手动规则" :value="1" />
                <el-option label="自动规则" :value="2" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20" v-if="form.ruleType === 2">
          <el-col :span="12">
            <el-form-item label="置信度" prop="confidence">
              <el-slider
                v-model="form.confidence"
                :min="0"
                :max="1"
                :step="0.01"
                :format-tooltip="formatConfidence"
                style="width: 100%"
              />
              <div class="slider-value">{{ (form.confidence * 100).toFixed(1) }}%</div>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 推荐产品配置 -->
        <el-divider content-position="left">推荐产品配置</el-divider>
        <div class="recommend-items-container">
          <div class="item-header">
            <span>推荐产品列表</span>
            <el-button type="primary" link @click="addRecommendItem">
              <el-icon><Plus /></el-icon>添加推荐项
            </el-button>
          </div>
          
          <div v-if="form.ruleItems.length === 0" class="empty-items">
            暂无推荐产品，请点击"添加推荐项"进行配置
          </div>
          
          <div v-else class="items-list">
            <div v-for="(item, index) in form.ruleItems" :key="index" class="recommend-item">
              <div class="item-header">
                <span>推荐项 {{ index + 1 }}</span>
                <el-button type="danger" link @click="removeRecommendItem(index)">
                  <el-icon><Delete /></el-icon>删除
                </el-button>
              </div>
              
              <el-row :gutter="20">
                <el-col :span="20">
                  <el-form-item 
                    :label="`产品 ${index + 1}`" 
                    :prop="`ruleItems.${index}.productId`"
                    :rules="{
                      required: true,
                      message: '请选择推荐产品',
                      trigger: 'change'
                    }"
                  >
                    <el-select
                      v-model="item.productId"
                      placeholder="请选择推荐产品"
                      style="width: 100%"
                      filterable
                      remote
                      :remote-method="(query) => searchRecommendProducts(query, index)"
                      @change="(value) => handleRecommendProductChange(value, index)"
                    >
                      <el-option
                        v-for="product in item.productOptions"
                        :key="product.id"
                        :label="`${product.name} (${product.sku})`"
                        :value="product.id"
                      >
                        <div style="display: flex; justify-content: space-between; align-items: center; width: 100%;">
                          <div style="text-align: left; flex: 1;">
                           <div style="font-weight: 500;">{{ product.name }}{{ product.spec ? ' - ' + product.spec : '-无规格' }}{{ product.color ? ' - ' + product.color : '-无颜色' }}</div>
                          </div>
                        </div>
                      </el-option>
                    </el-select>
                    <!-- 已选择的产品信息 -->
                    <div v-if="item.productName" class="selected-product-info">
                      <div class="product-info-row">
                        <span class="label">产品名称:</span>
                        <span class="value">{{ item.productName }}</span>
                      </div>
                      <div class="product-info-row">
                        <span class="label">SKU:</span>
                        <span class="value">{{ item.productSku }}</span>
                      </div>
                      <div v-if="item.productSpec" class="product-info-row">
                        <span class="label">规格:</span>
                        <span class="value">{{ item.productSpec }}</span>
                      </div>
                      <div v-if="item.productColor" class="product-info-row">
                        <span class="label">颜色:</span>
                        <span class="value">{{ item.productColor }}</span>
                      </div>
                    </div>
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item 
                    :label="`数量类型`" 
                    :prop="`ruleItems.${index}.quantityType`"
                    :rules="{
                      required: true,
                      message: '请选择数量类型',
                      trigger: 'change'
                    }"
                  >
                    <el-select 
                      v-model="item.quantityType" 
                      placeholder="请选择" 
                      style="width: 100%" 
                      @change="handleQuantityTypeChange(item)"
                    >
                      <el-option label="固定数量" :value="1" />
                      <el-option label="按比例" :value="2" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item 
                    :label="item.quantityType === 1 ? '固定数量' : '比例系数'" 
                    :prop="`ruleItems.${index}.quantityValue`"
                    :rules="{
                      required: true,
                      message: item.quantityType === 1 ? '请输入数量' : '请输入比例',
                      trigger: 'blur'
                    }"
                  >
                    <el-input-number
                      v-model="item.quantityValue"
                      :min="0.0001"
                      :precision="item.quantityType === 1 ? 2 : 4"
                      :step="item.quantityType === 1 ? 1 : 0.1"
                      controls-position="right"
                      style="width: 100%"
                      :placeholder="item.quantityType === 1 ? '如：10.00' : '如：1.5000'"
                    />
                  </el-form-item>
                </el-col>
              </el-row>
              
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item 
                    label="是否强制" 
                    :prop="`ruleItems.${index}.isRequired`"
                    :rules="{
                      required: true,
                      message: '请选择是否强制',
                      trigger: 'change'
                    }"
                  >
                    <el-radio-group v-model="item.isRequired" style="margin-top: 8px;">
                      <el-radio :label="1" border size="small">必选</el-radio>
                      <el-radio :label="0" border size="small">非必选</el-radio>
                    </el-radio-group>
                  </el-form-item>
                </el-col>
                <el-col :span="12" v-if="item.quantityType === 2">
                  <div class="quantity-tip">
                    <div style="font-weight: 500; margin-bottom: 4px;">计算示例:</div>
                    当触发产品数量为 {{ form.triggerMinQuantity || 1 }} 时，
                    推荐产品数量为：{{ calculateRecommendQuantity(item) }}
                  </div>
                </el-col>
              </el-row>
              
              <el-row :gutter="20">
                <el-col :span="24">
                  <el-form-item label="推荐说明" label-width="80px">
                    <el-input
                      v-model="item.remark"
                      placeholder="请输入推荐说明（可选）"
                      maxlength="200"
                      show-word-limit
                    />
                  </el-form-item>
                </el-col>
              </el-row>
              
              <el-divider v-if="index < form.ruleItems.length - 1" />
            </div>
          </div>
        </div>

        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入规则备注信息"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-switch
            v-model="form.status"
            :active-value="1"
            :inactive-value="0"
            active-text="启用"
            inactive-text="禁用"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="handleDialogClose">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
          {{ isEditMode ? '更新' : '创建' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 规则详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="规则详情"
      width="900px"
    >
      <div v-if="currentRule">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="规则编码">{{ currentRule.ruleCode || '-' }}</el-descriptions-item>
          <el-descriptions-item label="规则名称">{{ currentRule.ruleName }}</el-descriptions-item>
          <el-descriptions-item label="触发产品">
            {{ currentRule.triggerProductName }} ({{ currentRule.triggerProductSku }})
          </el-descriptions-item>
          <el-descriptions-item label="触发产品规格">{{ currentRule.triggerProductSpec || '-' }}</el-descriptions-item>
          <el-descriptions-item label="触发产品颜色">{{ currentRule.triggerProductColor || '-' }}</el-descriptions-item>
          <el-descriptions-item label="触发数量">
            {{ currentRule.triggerMinQuantity }}
            <span v-if="currentRule.triggerMaxQuantity"> - {{ currentRule.triggerMaxQuantity }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="适用客户">{{ currentRule.customerName || '所有客户' }}</el-descriptions-item>
          <el-descriptions-item label="应用场景">
            <el-tag :type="getSceneTagType(currentRule.applyScene)" size="small">
              {{ getSceneText(currentRule.applyScene) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="规则类型">
            <el-tag :type="currentRule.ruleType === 1 ? '' : 'success'" size="small">
              {{ currentRule.ruleType === 1 ? '手动' : '自动' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="优先级">
            <el-tag size="small">{{ currentRule.priority }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="置信度" v-if="currentRule.confidence">
            {{ (currentRule.confidence * 100).toFixed(1) }}%
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="currentRule.status === 1 ? 'success' : 'danger'" size="small">
              {{ currentRule.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatTime(currentRule.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ formatTime(currentRule.modifiedAt) }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ currentRule.remark || '-' }}</el-descriptions-item>
        </el-descriptions>

        <!-- 推荐项详情 -->
        <div v-if="ruleItemsDetail.length > 0" class="detail-items-section">
          <h4>推荐产品列表</h4>
          <el-table :data="ruleItemsDetail" style="width: 100%" border>
            <el-table-column type="index" label="序号" width="60" align="center" />
            <el-table-column label="推荐产品" min-width="200">
              <template #default="scope">
                <div style="font-weight: 500;">{{ scope.row.productName }}</div>
                <div class="text-muted">SKU: {{ scope.row.productSku }}</div>
                <div v-if="scope.row.productSpec" class="text-muted">规格: {{ scope.row.productSpec }}</div>
                <div v-if="scope.row.productColor" class="text-muted">颜色: {{ scope.row.productColor }}</div>
              </template>
            </el-table-column>
            <el-table-column label="数量类型" width="100" align="center">
              <template #default="scope">
                {{ scope.row.quantityType === 1 ? '固定数量' : '按比例' }}
              </template>
            </el-table-column>
            <el-table-column label="数量/比例" width="120" align="center">
              <template #default="scope">
                {{ scope.row.quantityValue }}
              </template>
            </el-table-column>
            <el-table-column label="计算示例" width="150" align="center" v-if="currentRule.triggerMinQuantity && ruleItemsDetail.some(item => item.quantityType === 2)">
              <template #default="scope">
                <span v-if="scope.row.quantityType === 2">
                  {{ calculateDetailQuantity(scope.row) }}
                </span>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column label="是否强制" width="100" align="center">
              <template #default="scope">
                <el-tag :type="scope.row.isRequired === 1 ? 'danger' : 'success'" size="small">
                  {{ scope.row.isRequired === 1 ? '必选' : '非必选' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="推荐说明" prop="remark" min-width="150" show-overflow-tooltip />
          </el-table>
        </div>
      </div>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Refresh, Delete, CircleCheck, CircleClose } from '@element-plus/icons-vue';
import { post, get } from '@/net';

// 响应式数据
const loading = ref(false);
const dialogVisible = ref(false);
const detailDialogVisible = ref(false);
const submitLoading = ref(false);
const productLoading = ref(false);
const isEditMode = ref(false);
const currentRule = ref(null);
const selectedRows = ref([]);
const ruleItemsDetail = ref([]);

// 筛选表单
const filterForm = reactive({
  ruleName: '',
  triggerProductId: null,
  customerId: null,
  applyScene: null,
  ruleType: null,
  status: null
});

// 分页
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
});

// 客户列表
const customerList = ref([]);
// 触发产品选项
const triggerProductOptions = ref([]);
// 规则列表
const ruleList = ref([]);

// 表单数据和验证规则
const formRef = ref();
const form = reactive({
  ruleCode: '',
  ruleName: '',
  customerId: null,
  triggerProductId: null,
  triggerMinQuantity: 1,
  triggerMaxQuantity: null,
  priority: 100,
  status: 1,
  applyScene: 1,
  ruleType: 1,
  confidence: 1,
  remark: '',
  ruleItems: []
});

const formRules = {
  ruleName: [
    { required: true, message: '请输入规则名称', trigger: 'blur' },
    { max: 100, message: '规则名称不能超过100个字符', trigger: 'blur' }
  ],
  triggerProductId: [
    { required: true, message: '请选择触发产品', trigger: 'change' }
  ],
  priority: [
    { required: true, message: '请输入优先级', trigger: 'blur' },
    { type: 'number', min: 1, max: 999, message: '优先级范围为1-999', trigger: 'blur' }
  ],
  applyScene: [
    { required: true, message: '请选择应用场景', trigger: 'change' }
  ],
  ruleType: [
    { required: true, message: '请选择规则类型', trigger: 'change' }
  ],
  triggerMinQuantity: [
    { required: true, message: '请输入最小触发数量', trigger: 'blur' },
    { type: 'number', min: 0, message: '最小触发数量不能小于0', trigger: 'blur' }
  ]
};

// 计算属性
const dialogTitle = computed(() => {
  return isEditMode.value ? '编辑推荐规则' : '新增推荐规则';
});

const showConfidence = computed(() => {
  return ruleList.value.some(rule => rule.ruleType === 2);
});

// 方法
const handleSearch = () => {
  pagination.current = 1;
  loadRuleList();
};

const handleReset = () => {
  Object.assign(filterForm, {
    ruleName: '',
    triggerProductId: null,
    customerId: null,
    applyScene: null,
    ruleType: null,
    status: null
  });
  pagination.current = 1;
  loadRuleList();
};

const handleSizeChange = (size) => {
  pagination.size = size;
  pagination.current = 1;
  loadRuleList();
};

const handleCurrentChange = (current) => {
  pagination.current = current;
  loadRuleList();
};

const handleRefresh = () => {
  loadRuleList();
};

const handleAdd = () => {
  resetForm();
  isEditMode.value = false;
  
  // 生成规则编码
  const timestamp = new Date().getTime();
  form.ruleCode = `R${timestamp.toString().slice(-8)}`;
  
  dialogVisible.value = true;
};

const handleEdit = async (row) => {
  try {
    loading.value = true;
    resetForm();
    isEditMode.value = true;
    currentRule.value = row;
    
    // 加载规则详情
    const res = await get(`/api/auth/recommend/detail?id=${row.id}`);
    if (res) {
      Object.assign(form, {
        id: res.id,
        ruleCode: res.ruleCode,
        ruleName: res.ruleName,
        customerId: res.customerId,
        triggerProductId: res.triggerProductId,
        triggerMinQuantity: res.triggerMinQuantity,
        triggerMaxQuantity: res.triggerMaxQuantity,
        priority: res.priority,
        status: res.status,
        applyScene: res.applyScene,
        ruleType: res.ruleType,
        confidence: res.confidence || 1,
        remark: res.remark,
        ruleItems: []
      });
      
      // 加载规则项
      if (res.ruleItems && res.ruleItems.length > 0) {
        form.ruleItems = res.ruleItems.map(item => ({
          id: item.id,
          productId: item.productId,
          productName: item.productName,
          productSku: item.productSku,
          productSpec: item.productSpec,
          productColor: item.productColor,
          productOptions: [{
            id: item.productId,
            name: item.productName,
            sku: item.productSku,
            spec: item.productSpec,
            color: item.productColor
          }],
          quantityType: item.quantityType,
          quantityValue: item.quantityValue,
          isRequired: item.isRequired || 0, // 确保有默认值
          confidence: item.confidence || 1,
          remark: item.remark,
          sequence: item.sequence
        }));
      }
      
      dialogVisible.value = true;
    }
  } catch (error) {
    console.error('加载规则详情失败:', error);
    ElMessage.error('加载规则详情失败');
  } finally {
    loading.value = false;
  }
};

const viewRuleDetail = async (row) => {
  try {
    loading.value = true;
    currentRule.value = row;
    
    // 加载规则项详情
    const res = await get(`/api/auth/recommend/items?ruleId=${row.id}`);
    if (res && res.length > 0) {
      ruleItemsDetail.value = res.map(item => ({
        ...item,
        productSpec: item.productSpec,
        productColor: item.productColor
      }));
    } else {
      ruleItemsDetail.value = [];
    }
    
    detailDialogVisible.value = true;
  } catch (error) {
    console.error('加载规则详情失败:', error);
    ElMessage.error('加载规则详情失败');
  } finally {
    loading.value = false;
  }
};

const viewRuleItems = (row) => {
  ElMessage.info(`规则"${row.ruleName}"有${row.itemCount || 0}个推荐项`);
};

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除规则"${row.ruleName}"吗？`,
      '删除确认',
      {
        type: 'warning'
      }
    );
    
    await post('/api/auth/recommend/delete', { id: row.id });
    ElMessage.success('删除成功');
    loadRuleList();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败');
    }
  }
};

const handleStatusChange = async (row) => {
  try {
    await post('/api/auth/recommend/updateStatus', {
      id: row.id,
      status: row.status
    });
    ElMessage.success('状态更新成功');
  } catch (error) {
    // 回滚状态
    row.status = row.status === 1 ? 0 : 1;
    ElMessage.error('状态更新失败');
  }
};

const handleEnableBatch = async () => {
  try {
    const ids = selectedRows.value.map(row => row.id);
    await post('/api/auth/recommend/batchEnable', { ids });
    ElMessage.success('批量启用成功');
    loadRuleList();
  } catch (error) {
    ElMessage.error('批量启用失败');
  }
};

const handleDisableBatch = async () => {
  try {
    const ids = selectedRows.value.map(row => row.id);
    await post('/api/auth/recommend/batchDisable', { ids });
    ElMessage.success('批量禁用成功');
    loadRuleList();
  } catch (error) {
    ElMessage.error('批量禁用失败');
  }
};

const handleSelectionChange = (selection) => {
  selectedRows.value = selection;
};

const handleCustomerChange = (customerId) => {
  if (customerId) {
    const customer = customerList.value.find(c => c.id === customerId);
    if (customer) {
      // 可以在这里处理客户相关的逻辑
    }
  }
};

const handleTriggerProductChange = (productId) => {
  const product = triggerProductOptions.value.find(p => p.id === productId);
  if (product) {
    // 可以在这里处理触发产品相关的逻辑
  }
};

const handleRuleTypeChange = (ruleType) => {
  if (ruleType === 2 && form.confidence === undefined) {
    form.confidence = 0.8; // 自动规则默认置信度
  }
};

const handleRecommendProductChange = (productId, index) => {
  const productOptions = form.ruleItems[index].productOptions;
  const product = productOptions.find(p => p.id === productId);
  if (product) {
    form.ruleItems[index].productName = product.name;
    form.ruleItems[index].productSku = product.sku;
    form.ruleItems[index].productSpec = product.spec;
    form.ruleItems[index].productColor = product.color;
  }
};

const handleQuantityTypeChange = (item) => {
  // 如果切换到按比例，设置默认比例系数为1
  if (item.quantityType === 2 && (!item.quantityValue || item.quantityValue === 0)) {
    item.quantityValue = 1;
  }
  // 更新精度设置
  if (item.quantityType === 1) {
    item.quantityValue = item.quantityValue ? parseFloat(item.quantityValue.toFixed(2)) : 1;
  } else {
    item.quantityValue = item.quantityValue ? parseFloat(item.quantityValue.toFixed(4)) : 1;
  }
};

// 搜索触发产品
const searchTriggerProducts = async (query) => {
  if (query) {
    productLoading.value = true;
    try {
      const res = await get(`/api/auth/product/search?keyword=${query}`);
      triggerProductOptions.value = (res || []).map(product => ({
        ...product,
        spec: product.spec || '',
        color: product.color || ''
      }));
    } catch (error) {
      console.error('搜索触发产品失败:', error);
      triggerProductOptions.value = [];
    } finally {
      productLoading.value = false;
    }
  } else {
    triggerProductOptions.value = [];
  }
};

// 搜索推荐产品
const searchRecommendProducts = async (query, index) => {
  if (query) {
    try {
      const res = await get(`/api/auth/product/search?keyword=${query}`);
      form.ruleItems[index].productOptions = (res || []).map(product => ({
        ...product,
        spec: product.spec || '',
        color: product.color || ''
      }));
    } catch (error) {
      console.error('搜索推荐产品失败:', error);
      form.ruleItems[index].productOptions = [];
    }
  } else {
    form.ruleItems[index].productOptions = [];
  }
};

const handleDialogClose = () => {
  ElMessageBox.confirm('确定要关闭吗？未保存的更改将会丢失。', '提示', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(() => {
    dialogVisible.value = false;
  }).catch(() => {
    // 用户取消关闭
  });
};

const handleSubmit = async () => {
  if (!formRef.value) return;
  
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) return;
  
  if (form.ruleItems.length === 0) {
    ElMessage.warning('请至少配置一个推荐产品');
    return;
  }
  
  submitLoading.value = true;
  
  try {
    // 准备提交数据
    const submitData = {
      ...form,
      ruleItems: form.ruleItems.map((item, index) => ({
        id: item.id,
        productId: item.productId,
        productName: item.productName,
        productSku: item.productSku,
        productSpec: item.productSpec,
        productColor: item.productColor,
        quantityType: item.quantityType,
        quantityValue: item.quantityValue,
        isRequired: item.isRequired,
        confidence: item.confidence || 1,
        remark: item.remark,
        sequence: index + 1
      }))
    };
    
    if (isEditMode.value) {
      await post('/api/auth/recommend/update', submitData);
      ElMessage.success('更新成功');
    } else {
      await post('/api/auth/recommend/create', submitData);
      ElMessage.success('创建成功');
    }
    
    dialogVisible.value = false;
    loadRuleList();
  } catch (error) {
    console.error('保存推荐规则失败:', error);
    ElMessage.error('保存失败');
  } finally {
    submitLoading.value = false;
  }
};

const addRecommendItem = () => {
  form.ruleItems.push({
    productId: null,
    productName: '',
    productSku: '',
    productSpec: '',
    productColor: '',
    productOptions: [],
    quantityType: 1,
    quantityValue: 1,
    isRequired: 0, // 默认非必选
    confidence: 1,
    remark: '',
    sequence: form.ruleItems.length + 1
  });
};

const removeRecommendItem = (index) => {
  form.ruleItems.splice(index, 1);
  // 更新序号
  form.ruleItems.forEach((item, idx) => {
    item.sequence = idx + 1;
  });
};

const calculateRecommendQuantity = (item) => {
  if (item.quantityType === 1) {
    return item.quantityValue;
  } else {
    const triggerQty = form.triggerMinQuantity || 1;
    const result = triggerQty * item.quantityValue;
    return result.toFixed(4);
  }
};

const calculateDetailQuantity = (item) => {
  const triggerQty = currentRule.value.triggerMinQuantity || 1;
  const result = triggerQty * item.quantityValue;
  return result.toFixed(4);
};

const formatConfidence = (value) => {
  return `${(value * 100).toFixed(1)}%`;
};

const getSceneText = (scene) => {
  const sceneMap = {
    1: '出库推荐',
    2: '采购建议',
    3: '上架建议'
  };
  return sceneMap[scene] || '未知';
};

const getSceneTagType = (scene) => {
  const typeMap = {
    1: 'primary',    // 出库推荐
    2: 'success',    // 采购建议
    3: 'warning'     // 上架建议
  };
  return typeMap[scene] || '';
};

const resetForm = () => {
  Object.assign(form, {
    id: null,
    ruleCode: '',
    ruleName: '',
    customerId: null,
    triggerProductId: null,
    triggerMinQuantity: 1,
    triggerMaxQuantity: null,
    priority: 100,
    status: 1,
    applyScene: 1,
    ruleType: 1,
    confidence: 1,
    remark: '',
    ruleItems: []
  });
  
  if (formRef.value) {
    formRef.value.clearValidate();
  }
};

// 工具方法
const formatTime = (timeString) => {
  if (!timeString) return '-';
  const date = new Date(timeString);
  return `${date.getFullYear()}-${padZero(date.getMonth() + 1)}-${padZero(date.getDate())} ${padZero(date.getHours())}:${padZero(date.getMinutes())}`;
};

const padZero = (num) => {
  return num < 10 ? `0${num}` : num;
};

// API 调用
const loadCustomerList = async () => {
  try {
    const res = await get('/api/auth/customer/listEnable');
    customerList.value = res || [];
  } catch (error) {
    console.error('加载客户列表失败:', error);
  }
};

const loadRuleList = async () => {
  loading.value = true;
  try {
    const params = {
      page: pagination.current,
      size: pagination.size,
      ...filterForm
    };
    
    // 过滤掉null值
    Object.keys(params).forEach(key => {
      if (params[key] === null || params[key] === undefined || params[key] === '') {
        delete params[key];
      }
    });
    
    const res = await post('/api/auth/recommend/pageList', params);
    ruleList.value = res.records || [];
    pagination.total = res.total || 0;
  } catch (error) {
    console.error('加载推荐规则列表失败:', error);
    ElMessage.error('加载推荐规则列表失败');
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  loadCustomerList();
  loadRuleList();
});
</script>

<style scoped>
.recommend-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.header-left .page-title {
  margin: 0 0 8px 0;
  font-size: 20px;
  font-weight: bold;
  color: #303133;
}

.filter-container {
  margin-bottom: 20px;
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.table-card {
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.recommend-items-container {
  margin: 20px 0;
  padding: 20px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  background-color: #f8f9fa;
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #e4e7ed;
}

.item-header span {
  font-weight: bold;
  color: #409eff;
}

.empty-items {
  padding: 40px;
  text-align: center;
  color: #909399;
  background-color: white;
  border-radius: 4px;
  border: 1px dashed #dcdfe6;
}

.items-list {
  margin-top: 10px;
}

.recommend-item {
  padding: 16px;
  background-color: white;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
  margin-bottom: 16px;
}

.recommend-item:last-child {
  margin-bottom: 0;
}

.quantity-tip {
  padding: 12px;
  background-color: #f0f9ff;
  border-radius: 4px;
  color: #409eff;
  font-size: 13px;
  margin-top: 8px;
  border-left: 4px solid #409eff;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.slider-value {
  text-align: center;
  font-weight: bold;
  color: #409eff;
  margin-top: 8px;
}

.text-muted {
  color: #909399;
  font-size: 12px;
}

.detail-items-section {
  margin-top: 20px;
}

.detail-items-section h4 {
  margin: 20px 0 10px 0;
  color: #303133;
  font-size: 16px;
  font-weight: bold;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
}

.selected-product-info {
  margin-top: 8px;
  padding: 12px;
  background-color: #f8f9fa;
  border-radius: 4px;
  border: 1px solid #ebeef5;
}

.product-info-row {
  display: flex;
  margin-bottom: 4px;
  font-size: 13px;
}

.product-info-row:last-child {
  margin-bottom: 0;
}

.product-info-row .label {
  color: #606266;
  min-width: 50px;
  margin-right: 8px;
}

.product-info-row .value {
  color: #303133;
  font-weight: 500;
}

:deep(.el-table) {
  border-radius: 6px;
  overflow: hidden;
}

:deep(.el-table .el-table__cell) {
  padding: 12px 0;
}

:deep(.el-button--link) {
  padding: 4px 8px;
}

:deep(.el-descriptions) {
  margin-top: 10px;
}

:deep(.el-descriptions__label) {
  width: 100px;
  font-weight: bold;
}

:deep(.el-select-dropdown__item) {
  display: flex;
  justify-content: space-between;
  padding: 8px 20px;
}

:deep(.el-divider__text) {
  color: #409eff;
  font-weight: bold;
}

:deep(.el-radio-group .el-radio) {
  margin-right: 8px;
}

:deep(.el-radio.is-bordered) {
  padding: 8px 15px;
}
</style>