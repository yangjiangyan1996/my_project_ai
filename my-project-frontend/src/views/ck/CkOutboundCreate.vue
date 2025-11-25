<template>
  <div class="outbound-create-container">
    <el-card class="form-card" shadow="never">
      <template #header>
        <!-- 透出废弃两个字 -->
        <span class="deprecated-tag">废弃</span>
        <div class="card-header">
          <span class="card-title">{{ isEditMode ? '编辑出库单' : '新建出库单' }}</span>
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
                @change="handleOrderTypeChange"
              >
                <el-option
                  v-for="item in orderTypeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
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
            <el-form-item label="客户" prop="customerId" v-if="showCustomer">
              <el-select
                v-model="formData.customerId"
                placeholder="请选择客户"
                style="width: 100%"
                filterable
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
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :lg="8">
            <el-form-item label="关联单号" prop="relatedOrderNo">
              <el-input
                v-model="formData.relatedOrderNo"
                placeholder="请输入关联单号"
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
          />
        </el-form-item>
      </el-form>

      <!-- 产品明细 -->
      <div class="product-section">
        <div class="section-header">
          <h3>产品明细</h3>
          <div class="header-right-actions">
            <el-button 
              type="primary" 
              @click="handleAddProduct"
              :disabled="!formData.warehouseId"
              v-if="formData.orderType !== 1"
            >
              <el-icon><Plus /></el-icon>
              添加产品
            </el-button>
            <template v-if="formData.orderType === 1">
              <el-button 
                type="success" 
                @click="handleDownloadTemplate"
                :loading="downloadLoading"
              >
                <el-icon><Download /></el-icon>
                下载模板
              </el-button>
              <el-button 
                type="warning" 
                @click="handleImportExcel"
              >
                <el-icon><Upload /></el-icon>
                导入模板
              </el-button>
            </template>
          </div>
        </div>

        <!-- 销售出库的产品表格 - 修改为展示所有商品 -->
        <el-table
          v-if="formData.orderType === 1 && formData.warehouseId"
          :data="allInventoryProducts"
          border
          class="product-table"
          empty-text="请先选择仓库"
        >
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column label="产品信息" min-width="200">
            <template #default="{ row }">
              <div>
                <div class="product-name">{{ row.productName }}</div>
                <div class="sku-text">{{ row.sku }}</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="规格型号" width="120">
            <template #default="{ row }">
              <span>{{ row.spec || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="颜色" width="60">
            <template #default="{ row }">
              <span>{{ row.color || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="当前库存" width="100" align="center">
            <template #default="{ row }">
              <span :class="getStockClass(row.availableQuantity, row.quantity)">
                {{ row.availableQuantity }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="出库数量" width="120">
            <template #default="{ row }">
              <el-input-number
                v-model="row.quantity"
                :min="0"
                :max="row.availableQuantity"
                controls-position="right"
                style="width: 100%"
                @change="() => handleQuantityChangeForAll(row)"
                placeholder="请输入数量"
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
                style="width: 100%"
                :disabled="!row.quantity || row.quantity <= 0"
              >
                <template #prefix>¥</template>
              </el-input-number>
            </template>
          </el-table-column>
          <el-table-column label="金额" width="120" align="right">
            <template #default="{ row }">
              <span v-if="row.quantity > 0">¥ {{ ((row.price || 0) * (row.quantity || 0)).toFixed(2) }}</span>
              <span v-else>-</span>
            </template>
          </el-table-column>

          <!-- 新增USD单价字段 -->
          <el-table-column label="USD单价" width="120">
            <template #default="{ row }">
              <el-input-number
                v-model="row.priceUnitUsd"
                :min="0"
                :precision="2"
                controls-position="right"
                style="width: 100%"
                :disabled="!row.quantity || row.quantity <= 0"
              >
                <template #prefix>$</template>
              </el-input-number>
            </template>
          </el-table-column>
          <!-- 新增USD总额字段 -->
          <el-table-column label="USD总额" width="120" align="right">
            <template #default="{ row }">
              <span v-if="row.quantity > 0">$ {{ ((row.priceUnitUsd || 0) * (row.quantity || 0)).toFixed(2) }}</span>
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
                  :disabled="!row.quantity || row.quantity <= 0"
                >
                  分配批次
                </el-button>
                <div v-if="row.batchAllocations && row.batchAllocations.length > 0" class="batch-summary">
                  <el-tag
                    v-for="allocation in row.batchAllocations"
                    :key="`${allocation.batchNo}-${allocation.shelfId}`"
                    size="small"
                    class="batch-tag"
                  >
                    {{ allocation.batchNo }}({{ allocation.shelfName }}): {{ allocation.quantity }}个
                  </el-tag>
                </div>
                <div v-else class="batch-empty">
                  <span class="empty-text" v-if="row.quantity > 0">未分配批次</span>
                  <span class="empty-text" v-else>-</span>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="备注" min-width="150">
            <template #default="{ row }">
              <el-input
                v-model="row.remark"
                placeholder="产品备注"
                maxlength="100"
                show-word-limit
                :disabled="!row.quantity || row.quantity <= 0"
              />
            </template>
          </el-table-column>
        </el-table>

        <!-- 生产领料的产品表格 -->
        <el-table
          v-if="formData.orderType === 2"
          :data="formData.items"
          border
          class="product-table"
          empty-text="请添加产品明细"
        >
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column label="产品信息" min-width="200">
            <template #default="{ row, $index }">
              <el-select
                v-model="row.productId"
                placeholder="选择产品"
                style="width: 100%"
                filterable
                @change="(value) => handleProductChange(value, $index)"
              >
                <el-option
                  v-for="product in productionProducts"
                  :key="product.id"
                  :label="`${product.sku} - ${product.name}`"
                  :value="product.id"
                />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="规格型号" width="120">
            <template #default="{ row }">
              <span>{{ row.spec || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="单位" width="80" align="center">
            <template #default="{ row }">
              <span>{{ row.unit || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="领料数量" width="120">
            <template #default="{ row, $index }">
              <el-input-number
                v-model="row.quantity"
                :min="1"
                controls-position="right"
                style="width: 100%"
                @change="() => handleQuantityChange($index)"
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
                style="width: 100%"
              >
                <template #prefix>¥</template>
              </el-input-number>
            </template>
          </el-table-column>
          <el-table-column label="金额" width="120" align="right">
            <template #default="{ row }">
              <span>¥ {{ ((row.price || 0) * (row.quantity || 0)).toFixed(2) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="备注" min-width="150">
            <template #default="{ row }">
              <el-input
                v-model="row.remark"
                placeholder="产品备注"
                maxlength="100"
                show-word-limit
              />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80" fixed="right" align="center">
            <template #default="{ $index }">
              <el-button
                type="danger"
                link
                @click="handleRemoveProduct($index)"
              >
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- BOM原料分配（仅生产领料显示） -->
       
        <!-- BOM原料分配部分 -->
  <div class="bom-section" v-if="formData.orderType === 2 && hasBomData">
    <div class="section-header">
      <h3>原料分配</h3>
      <span class="bom-tip">根据产品BOM自动计算所需原料</span>
    </div>
    
    <div class="bom-content" v-for="(item, itemIndex) in formData.items" :key="itemIndex">
      <div class="bom-item-header" v-if="getBomData(item.productId)?.length">
        <h4>{{ item.productName }} ({{ item.quantity || 0 }} {{ item.unit }}) 所需原料:</h4>
        <div class="allocation-summary">
          <span v-for="bomItem in getBomData(item.productId)" :key="bomItem.componentProductId" 
                class="summary-item" :class="{ 'insufficient': isInsufficient(itemIndex, bomItem) }">
            {{ bomItem.componentProductName }}: 
            已分配 {{ getAllocatedQuantityForComponent(itemIndex, bomItem.componentProductId) }} / 
            总需求 {{ calculateRequiredQuantity(bomItem.quantity, item.quantity) }}
            <span v-if="isInsufficient(itemIndex, bomItem)" class="insufficient-tip">
              (不足 {{ calculateShortage(itemIndex, bomItem) }})
            </span>
          </span>
        </div>
      </div>
      
      <el-table
        :data="getBomData(item.productId)"
        border
        class="bom-table"
        v-if="getBomData(item.productId)?.length"
      >
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column label="原料信息" min-width="100">
          <template #default="{ row }">
            <div>
              <div>{{ row.componentProductName }}</div>
              <div class="sku-text">{{ row.componentProductSku }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="规格" width="120">
          <template #default="{ row }">
            <span>{{ row.componentProductSpec || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="单位" width="80" align="center">
          <template #default="{ row }">
            <span>{{ row.componentProductUnit || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="单件用量" width="100" align="center">
          <template #default="{ row }">
            <span>{{ row.quantity }}</span>
          </template>
        </el-table-column>
        <el-table-column label="总需求量" width="120" align="center">
          <template #default="{ row }">
            <span class="required-quantity">{{ calculateRequiredQuantity(row.quantity, item.quantity) }}</span>
          </template>
        </el-table-column>

        <!-- 修改原料分配列，显示批次货架信息 -->
        <el-table-column label="原料分配" min-width="400">
          <template #default="{ row }">
            <div class="allocation-container">
              <!-- 改为批次维度展示 -->
              <div v-for="batch in getBatchDataForComponent(row.componentProductId)" 
                  :key="batch.batchNo" 
                  class="batch-allocation">
                <div class="batch-info">
                  <strong>批次 {{ batch.batchNo }}</strong>
                  <span>总可用: {{ batch.quantity }}</span>
                </div>
                <div class="shelf-allocation" v-if="batch.shelfList && batch.shelfList.length > 0">
                  <div v-for="shelf in batch.shelfList" 
                      :key="shelf.shelfId" 
                      class="shelf-item">
                    <div class="shelf-info">
                      <span>货架 {{ shelf.shelfName }}</span>
                      <span>可用: {{ shelf.quantity }}</span>
                    </div>
                    <el-input-number
                      :model-value="getAllocationQuantity(itemIndex, row.componentProductId, batch.batchNo, shelf.shelfId)"
                      @update:model-value="(value) => updateAllocationQuantity(value, itemIndex, row, batch, shelf)"
                      :min="0"
                      :max="getMaxAllocation(itemIndex, row, batch, shelf)"
                      :precision="4"
                      :step="1"
                      controls-position="right"
                      size="small"
                      placeholder="使用数量"
                      class="allocation-input"
                    />
                  </div>
                </div>
                <div v-else class="no-shelf-allocation">
                  <span class="no-shelf-text">无货架信息</span>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>

        <!-- 统计信息 -->
        <div class="summary-info" v-if="(formData.orderType === 1 && allInventoryProducts.some(p => p.quantity > 0)) || (formData.orderType !== 1 && formData.items.length > 0)">
          <el-row :gutter="20">
            <el-col :span="4">
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
                <span class="value">¥ {{ totalAmount.toFixed(2) }}</span>
              </div>
            </el-col>
            <el-col :span="4">
              <div class="summary-item">
                <span class="label">USD总额：</span>
                <span class="value">$ {{ totalAmountUsd.toFixed(2) }}</span>
              </div>
            </el-col>
            <el-col :span="4">
              <div class="summary-item">
                <span class="label">库存状态：</span>
                <span class="value" :class="stockStatusClass">
                  {{ stockStatusText }}
                </span>
              </div>
            </el-col>
          </el-row>
        </div>
      </div>

      <!-- 附件上传 -->
      <div class="attachment-section">
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
    </el-card>

    <!-- 批次分配对话框（销售出库使用） -->
    <el-dialog
      v-model="batchDialog.visible"
      :title="`批次分配 - ${batchDialog.productName}`"
      width="800px"
      destroy-on-close
      v-if="formData.orderType === 1"
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
        </div>

        <!-- 批次分配表格 -->
        <el-table :data="batchDialog.batches" border class="batch-table">
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
                      :disabled="shelf.quantity < 1 || row.quantity < 1"
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
          <el-table-column label="操作" width="100" fixed="right" align="center">
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

        <div class="batch-actions">
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


  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Delete, Upload, Download,  Document, Close  } from '@element-plus/icons-vue';
import { post, get } from '@/net';
import axios from 'axios';
import { accessHeader } from '@/net'; 

const router = useRouter();
const route = useRoute();
const formRef = ref();
const uploadRef = ref();
const loading = ref(false);
const downloadLoading = ref(false);

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
const productionProductList = ref([]);
const fileList = ref([]);
const allInventoryProducts = ref([]);

// 产品库存映射表
const productStockMap = ref({});

// 出库类型选项
const orderTypeOptions = [
  { value: 1, label: '销售出库' },
  { value: 2, label: '生产领料' },
  { value: 3, label: '退货出库' },
  { value: 4, label: '调拨出库' },
  { value: 5, label: '其他出库' }
];

// 计算属性
const showCustomer = computed(() => {
  return formData.orderType === 1;
});

// 所有库存产品（销售出库使用）
// const allInventoryProducts = computed(() => {
//   if (!formData.warehouseId || !inventoryList.value.length) return [];
  
//   return inventoryList.value.map(item => ({
//     ...item,
//     quantity: 0,
//     price: item.price || 0,
//     remark: item.remark || '',
//     batchAllocations: item.batchAllocations || [],
//     availableBatches: item.availableBatches || []
//   }));
// });

// 添加一个方法来更新库存产品数据
const updateInventoryProducts = () => {
  if (!formData.warehouseId || !inventoryList.value.length) {
    allInventoryProducts.value = [];
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
      price: displayPrice, // 默认展示接口中的price字段
      remark: existingProduct ? existingProduct.remark : '',
      batchAllocations: existingProduct ? existingProduct.batchAllocations : [],
      availableBatches: existingProduct ? existingProduct.availableBatches : [],
      // 保存接口原始价格，用于导入时的逻辑判断
      originalPrice: item.price || 0,
      originalPriceUnitUsd: item.priceUnitUsd || 0 // 新增：保存接口原始USD价格
    };
  });
};

// 销售出库可用产品（有库存的产品）
const availableProducts = computed(() => {
  if (!formData.warehouseId) return [];
  
  return inventoryList.value
    .filter(item => item.availableQuantity > 0)
    .map(item => ({
      id: item.productId,
      sku: item.sku,
      name: item.productName,
      spec: item.spec,
      color: item.color,
      unit: item.unitName,
      quantity: item.availableQuantity,
      price: item.price || 0
    }));
});

// 生产领料可用产品
const productionProducts = computed(() => {
  return productionProductList.value.map(product => ({
    id: product.id,
    sku: product.sku,
    name: product.name,
    spec: product.spec,
    unit: product.unitName,
    bomData: product.bomData || []
  }));
});

// 产品种类数量
const productTypeCount = computed(() => {
  if (formData.orderType === 1) {
    return allInventoryProducts.value.filter(p => p.quantity > 0).length;
  } else {
    return formData.items.length;
  }
});

const totalQuantity = computed(() => {
  if (formData.orderType === 1) {
    return allInventoryProducts.value.reduce((sum, item) => sum + (item.quantity || 0), 0);
  } else {
    return formData.items.reduce((sum, item) => sum + (item.quantity || 0), 0);
  }
});

const totalAmount = computed(() => {
  if (formData.orderType === 1) {
    return allInventoryProducts.value.reduce((sum, item) => {
      const price = item.price || 0;
      const quantity = item.quantity || 0;
      return sum + (price * quantity);
    }, 0);
  } else {
    return formData.items.reduce((sum, item) => {
      const price = item.price || 0;
      const quantity = item.quantity || 0;
      return sum + (price * quantity);
    }, 0);
  }
});

// 新增：是否有BOM数据
const hasBomData = computed(() => {
  return formData.items.some(item => {
    const product = productionProductList.value.find(p => p.id === item.productId);
    return product?.bomData?.length > 0;
  });
});

const hasInsufficientStock = computed(() => {
  // 销售出库检查批次分配
  if (formData.orderType === 1) {
    return allInventoryProducts.value.some(item => {
      if (item.quantity <= 0) return false;
      const allocatedQuantity = item.batchAllocations 
        ? item.batchAllocations.reduce((sum, alloc) => sum + (alloc.quantity || 0), 0)
        : 0;
      return (item.quantity || 0) !== allocatedQuantity;
    });
  }
  
  // 生产领料检查BOM分配
  if (formData.orderType === 2) {
    const validationResult = validateBomAllocations();
    return !validationResult.valid;
  }
  
  return false;
});

const stockStatusText = computed(() => {
  if (formData.orderType === 1) {
    return hasInsufficientStock.value ? '批次分配不足' : '分配完成';
  } else if (formData.orderType === 2) {
    return hasInsufficientStock.value ? '原料分配不足' : '分配完成';
  }
  return '库存充足';
});

const stockStatusClass = computed(() => {
  return hasInsufficientStock.value ? 'status-warning' : 'status-success';
});

// 上传相关数据
const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token') || sessionStorage.getItem('token');
  return {
    'Authorization': token ? `Bearer ${token}` : ''
  };
});

const uploadData = computed(() => {
  return {
    warehouseId: formData.warehouseId
  };
});

// 修改获取BOM数据的方法，加入批次数据
const getBomData = (productId) => {
  // 首先查找产品自身的bomData（从批次接口获取的）
  const itemWithBomData = formData.items.find(item => item.productId === productId && item.bomData);
  if (itemWithBomData && itemWithBomData.bomData.length > 0) {
    return itemWithBomData.bomData;
  }
  
  // 如果没有，再从生产产品列表中查找
  const product = productionProductList.value.find(p => p.id === productId);
  return product?.bomData || [];
};


// 新增：为生产领料加载批次信息的方法
const loadBatchInfoForProduction = async (productId, index) => {
  try {
    const item = formData.items[index];
    
    // 首先获取产品的BOM数据
    const product = productionProductList.value.find(p => p.id === productId);
    if (!product?.bomData) {
      item.bomData = [];
      return;
    }
    
    // 为每个BOM组件加载批次信息
    const bomDataWithBatches = await Promise.all(
      product.bomData.map(async (bomItem) => {
        const batches = await loadBatchInfoForComponent(bomItem.componentProductId);
        return {
          ...bomItem,
          batches: batches || []
        };
      })
    );
    
    item.bomData = bomDataWithBatches;
    console.log('生产领料批次信息加载完成:', item.bomData);
    
  } catch (error) {
    console.error('加载生产领料批次信息失败:', error);
    formData.items[index].bomData = [];
  }
};


// 修改：获取原料组件的批次数据
const getBatchDataForComponent = (componentProductId) => {
  // 在所有产品的BOM数据中查找该组件的批次信息
  for (const item of formData.items) {
    if (item.bomData) {
      const bomItem = item.bomData.find(b => b.componentProductId === componentProductId);
      if (bomItem && bomItem.batches) {
        return bomItem.batches;
      }
    }
  }
  return [];
};


// 计算总需求量
const calculateRequiredQuantity = (unitQuantity, productQuantity) => {
  const unitQty = parseFloat(unitQuantity) || 0;
  const productQty = parseFloat(productQuantity) || 0;
  return (unitQty * productQty).toFixed(4);
};


// 计算属性 - 添加USD总额
const totalAmountUsd = computed(() => {
  if (formData.orderType === 1) {
    return allInventoryProducts.value.reduce((sum, item) => {
      const priceUsd = item.priceUnitUsd || 0;
      const quantity = item.quantity || 0;
      return sum + (priceUsd * quantity);
    }, 0);
  } else {
    return formData.items.reduce((sum, item) => {
      const priceUsd = item.priceUnitUsd || 0;
      const quantity = item.quantity || 0;
      return sum + (priceUsd * quantity);
    }, 0);
  }
});

// 修改获取分配数量的方法，加入批次维度
const getAllocationQuantity = (itemIndex, componentProductId, batchNo, shelfId) => {
  const item = formData.items[itemIndex];
  if (!item.bomAllocations) return 0;
  
  const allocation = item.bomAllocations.find(a => 
    a.componentProductId === componentProductId &&
    a.batchNo === batchNo &&
    a.shelfId === shelfId
  );
  return allocation ? parseFloat(allocation.quantity) : 0;
};

// 获取某个原料的总分配数量 - 修改为从 item.bomAllocations 中获取
const getAllocatedQuantityForComponent = (itemIndex, componentProductId) => {
  const item = formData.items[itemIndex];
  if (!item.bomAllocations) return 0;
  
  return item.bomAllocations
    .filter(a => a.componentProductId === componentProductId)
    .reduce((sum, a) => sum + (parseFloat(a.quantity) || 0), 0)
    .toFixed(4);
};

// 检查原料是否不足
const isInsufficient = (itemIndex, bomItem) => {
  const requiredQuantity = parseFloat(calculateRequiredQuantity(bomItem.quantity, formData.items[itemIndex].quantity));
  const allocatedQuantity = parseFloat(getAllocatedQuantityForComponent(itemIndex, bomItem.componentProductId));
  return allocatedQuantity < requiredQuantity;
};

// 计算不足数量
const calculateShortage = (itemIndex, bomItem) => {
  const requiredQuantity = parseFloat(calculateRequiredQuantity(bomItem.quantity, formData.items[itemIndex].quantity));
  const allocatedQuantity = parseFloat(getAllocatedQuantityForComponent(itemIndex, bomItem.componentProductId));
  return (requiredQuantity - allocatedQuantity).toFixed(4);
};

// 获取最大分配数量（考虑总需求量和已分配数量）
const getMaxAllocation = (itemIndex, bomRow, batch, shelf) => {
  const requiredQuantity = parseFloat(calculateRequiredQuantity(bomRow.quantity, formData.items[itemIndex].quantity));
  const currentAllocated = parseFloat(getAllocatedQuantityForComponent(itemIndex, bomRow.componentProductId));
  const currentInputValue = getAllocationQuantity(itemIndex, bomRow.componentProductId, batch.batchNo, shelf.shelfId);
  
  // 剩余可分配数量 = 总需求量 - (当前已分配总量 - 当前输入框的值)
  const remainingAllocation = requiredQuantity - (currentAllocated - currentInputValue);
  
  // 物理库存限制 - 使用货架可用数量
  const physicalMax = parseFloat(shelf.quantity);
  
  // 取两者中的较小值
  return Math.min(remainingAllocation, physicalMax);
};



// 修改更新分配数量的方法，加入批次维度
const updateAllocationQuantity = (value, itemIndex, bomRow, batch, shelf) => {
  const quantity = parseFloat(value) || 0;
  const item = formData.items[itemIndex];
  
  // 确保 item.bomAllocations 存在
  if (!item.bomAllocations) {
    item.bomAllocations = [];
  }
  
  const allocationIndex = item.bomAllocations.findIndex(a => 
    a.componentProductId === bomRow.componentProductId &&
    a.batchNo === batch.batchNo &&
    a.shelfId === shelf.shelfId
  );

  // 检查是否超过总需求量
  const requiredQuantity = parseFloat(calculateRequiredQuantity(bomRow.quantity, formData.items[itemIndex].quantity));
  const currentAllocated = parseFloat(getAllocatedQuantityForComponent(itemIndex, bomRow.componentProductId));
  const currentInputValue = getAllocationQuantity(itemIndex, bomRow.componentProductId, batch.batchNo, shelf.shelfId);
  
  const newTotalAllocated = currentAllocated - currentInputValue + quantity;
  
  if (newTotalAllocated > requiredQuantity) {
    // 如果超过总需求量，自动调整为剩余可分配数量
    const adjustedQuantity = Math.max(0, requiredQuantity - (currentAllocated - currentInputValue));
    ElMessage.warning(`分配数量不能超过总需求量 ${requiredQuantity}，已自动调整为 ${adjustedQuantity}`);
    
    if (adjustedQuantity > 0) {
      if (allocationIndex >= 0) {
        item.bomAllocations[allocationIndex].quantity = adjustedQuantity;
      } else {
        item.bomAllocations.push({
          componentProductId: bomRow.componentProductId,
          componentProductName: bomRow.componentProductName,
          componentProductSku: bomRow.componentProductSku,
          batchNo: batch.batchNo,
          shelfId: shelf.shelfId,
          shelfName: shelf.shelfName,
          quantity: adjustedQuantity
        });
      }
    } else if (allocationIndex >= 0) {
      item.bomAllocations.splice(allocationIndex, 1);
    }
    return;
  }

  if (quantity > 0) {
    if (allocationIndex >= 0) {
      item.bomAllocations[allocationIndex].quantity = quantity;
    } else {
      item.bomAllocations.push({
        componentProductId: bomRow.componentProductId,
        componentProductName: bomRow.componentProductName,
        componentProductSku: bomRow.componentProductSku,
        batchNo: batch.batchNo,
        shelfId: shelf.shelfId,
        shelfName: shelf.shelfName,
        quantity: quantity
      });
    }
  } else if (allocationIndex >= 0) {
    item.bomAllocations.splice(allocationIndex, 1);
  }
};

// 验证BOM分配
const validateBomAllocations = () => {
  for (let i = 0; i < formData.items.length; i++) {
    const item = formData.items[i];
    const bomData = getBomData(item.productId);
    
    if (bomData.length > 0) {
      for (const bomItem of bomData) {
        const requiredQuantity = calculateRequiredQuantity(bomItem.quantity, item.quantity);
        const allocatedQuantity = getAllocatedQuantityForComponent(i, bomItem.componentProductId);
        
        if (parseFloat(allocatedQuantity) < parseFloat(requiredQuantity)) {
          return {
            valid: false,
            message: `${item.productName} 所需的原料 ${bomItem.componentProductName} 分配数量不足，需要 ${requiredQuantity}，已分配 ${allocatedQuantity}`
          };
        }
      }
    }
  }
  
  return { valid: true };
};

// 表单验证规则
const formRules = {
  orderType: [
    { required: true, message: '请选择出库类型', trigger: 'change' }
  ],
  warehouseId: [
    { required: true, message: '请选择出库仓库', trigger: 'change' }
  ],
  customerId: [
    { 
      required: true, 
      message: '请选择客户', 
      trigger: 'change',
      validator: (rule, value, callback) => {
        if (showCustomer.value && !value) {
          callback(new Error('请选择客户'));
        } else {
          callback();
        }
      }
    }
  ],
  expectedDate: [
    { required: true, message: '请选择预计出库日期', trigger: 'change' }
  ]
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
        if (detailData.orderType === 1) {
          await loadInventoryData(detailData.warehouseId);
        }
      }

      // 设置产品明细数据 - 修改为将 bomAllocations 放入每个 item 中
      if (detailData.items && detailData.items.length > 0) {
        if (detailData.orderType === 1) {
          // 销售出库：将数据映射到 allInventoryProducts
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
              if (product) {
                product.quantity = detailItem.quantity || 0;
                product.price = detailItem.price || 0;
                product.remark = detailItem.remark || '';
                product.batchAllocations = detailItem.batchAllocations || [];
              }
            });
          }, 500);
        } else {
          // 生产领料：保持原有逻辑
          formData.items = detailData.items.map(item => {
            let currentStock = 0;
            if (detailData.orderType === 1) {
              currentStock = productStockMap.value[item.productId] || item.currentStock || 0;
            }
            
            return {
              productId: item.productId,
              productName: item.productName || '',
              sku: item.sku || '',
              spec: item.spec || '',
              unit: item.unit || '',
              currentStock: currentStock,
              quantity: item.quantity || 1,
              price: item.price || 0,
              batchAllocations: item.batchAllocations || [],
              availableBatches: item.availableBatches || [],
              remark: item.remark || '',
              // 将 bomAllocations 放入每个 item 中
              bomAllocations: item.bomAllocations || []
            };
          });
        }
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

// 获取当前库存数量
const getCurrentStock = (row) => {
  if (row.productId && productStockMap.value[row.productId] !== undefined) {
    return productStockMap.value[row.productId];
  }
  return row.currentStock || 0;
};

// 获取最大可出库数量
const getMaxQuantity = (row) => {
  const currentStock = getCurrentStock(row);
  return currentStock > 0 ? currentStock : 1;
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

const handleOrderTypeChange = (value) => {
  if (value !== 1) {
    formData.customerId = null;
  }
  
  // 清空产品列表和分配数据
  formData.items = [];
  
  // 重新加载产品数据
  if (value === 1 && formData.warehouseId) {
    loadInventoryData(formData.warehouseId);
  } else if (value === 2) {
    loadProductionProducts();
    // 清空销售出库的产品数据
    allInventoryProducts.value = [];
  } else {
    // 其他出库类型也清空销售出库数据
    allInventoryProducts.value = [];
  }
};

const handleWarehouseChange = async (warehouseId) => {
  if (warehouseId) {
    if (formData.orderType === 1) {
      await loadInventoryData(warehouseId);
    }
    // 清空产品列表和分配数据
    formData.items = [];
    
    // 确保所有产品的数量为0
    if (formData.orderType === 1 && allInventoryProducts.value.length > 0) {
      allInventoryProducts.value.forEach(product => {
        product.quantity = 0;
        product.price = 0;
        product.remark = '';
        product.batchAllocations = [];
      });
    }
  }
};

const handleAddProduct = () => {
  formData.items.push({
    productId: null,
    productName: '',
    sku: '',
    spec: '',
    unit: '',
    currentStock: 0,
    quantity: 1,
    price: 0,
    batchAllocations: [],
    availableBatches: [],
    remark: '',
    // 新增：每个商品项的BOM分配数据
    bomAllocations: []
  });
};

const handleRemoveProduct = (index) => {
  formData.items.splice(index, 1);
};


const handleProductChange = async (productId, index) => {
  let product;
  if (formData.orderType === 1) {
    product = availableProducts.value.find(p => p.id === productId);
  } else {
    product = productionProducts.value.find(p => p.id === productId);
  }
  
  if (product) {
    const item = formData.items[index];
    item.productId = product.id;
    item.productName = product.name;
    item.sku = product.sku;
    item.spec = product.spec;
    item.unit = product.unit;
    
    if (formData.orderType === 1) {
      // 销售出库逻辑保持不变
      item.currentStock = productStockMap.value[productId] || 0;
      item.quantity = item.quantity || 1;
      item.price = item.price || 0;
      item.batchAllocations = [];
      
      await loadBatchInfo(productId, formData.warehouseId, index);
    } else {
      // 生产领料：初始化数据
      item.quantity = item.quantity || 1;
      item.price = item.price || 0;
      item.bomAllocations = item.bomAllocations || [];
      
      // 新增：为生产领料加载批次信息
      await loadBatchInfoForProduction(productId, index);
    }
  }
};

const handleQuantityChange = (index) => {
  const item = formData.items[index];
  
  if (formData.orderType === 1) {
    // 如果数量减少，需要调整批次分配
    if (item.batchAllocations && item.batchAllocations.length > 0) {
      const totalAllocated = item.batchAllocations.reduce((sum, alloc) => sum + alloc.quantity, 0);
      if (item.quantity < totalAllocated) {
        ElMessage.warning('出库数量小于已分配批次数量，请重新分配批次');
        item.batchAllocations = [];
      }
    }
  } else if (formData.orderType === 2) {
    // 生产领料时检查原料可用性
    checkMaterialAvailability(index);
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
    if (row.priceFromApi && row.priceFromApi > 0 && (!row.price || row.price === 0)) {
      row.price = row.priceFromApi;
    }
    // 新增：如果接口返回了USD价格，使用接口USD价格（仅在USD价格为空时自动填充）
    if (row.originalPriceUnitUsd && row.originalPriceUnitUsd > 0 && (!row.priceUnitUsd || row.priceUnitUsd === 0)) {
      row.priceUnitUsd = row.originalPriceUnitUsd;
    }
  } else {
    // 如果数量设为0，清空相关数据
    row.quantity = 0; // 确保数量为0
    row.price = 0;
    row.priceUnitUsd = 0; // 新增：清空USD价格
    row.remark = '';
    row.batchAllocations = [];
  }
};


// 检查原料可用性
const checkMaterialAvailability = (itemIndex) => {
  const item = formData.items[itemIndex];
  if (!item.productId || !item.quantity) return;
  
  const bomData = getBomData(item.productId);
  if (!bomData.length) return;
  
  let insufficientMaterials = [];
  
  bomData.forEach(bomItem => {
    const requiredQuantity = parseFloat(calculateRequiredQuantity(bomItem.quantity, item.quantity));
    const totalAvailable = bomItem.warehouseQuantityList.reduce((sum, warehouse) => {
      return sum + parseFloat(warehouse.warehouseAvailableQuantity);
    }, 0);
    
    if (totalAvailable < requiredQuantity) {
      const shortage = (requiredQuantity - totalAvailable).toFixed(4);
      insufficientMaterials.push({
        name: bomItem.componentProductName,
        required: requiredQuantity,
        available: totalAvailable,
        shortage: shortage
      });
    }
  });
  
  if (insufficientMaterials.length > 0) {
    const message = insufficientMaterials.map(m => 
      `原料 ${m.name} 不足：需要 ${m.required}，可用 ${m.available}，缺少 ${m.shortage}`
    ).join('；');
    
    ElMessage.warning({
      message: message,
      duration: 10000,
      showClose: true
    });
  }
};

const getStockClass = (currentStock, quantity) => {
  if (!currentStock || currentStock <= 0) return 'stock-none';
  if (quantity > currentStock) return 'stock-insufficient';
  if (currentStock < 10) return 'stock-low';
  return 'stock-sufficient';
};

// 修复批次分配相关方法
const getShelfMaxAllocation = (batch, shelf, batchIndex, shelfIndex) => {
  // 1. 如果批次总可用数量小于1，不能分配
  if (batch.quantity < 1) {
    return 0;
  }
  
  // 2. 如果货架可用数量小于1，不能分配
  if (shelf.quantity < 1) {
    return 0;
  }
  
  // 计算当前货架已分配数量
  const currentShelfAllocated = shelf.allocated || 0;
  
  // 3. 最大可分配数量 = 货架可用数量 和 剩余可分配数量的较小值
  const shelfMax = shelf.quantity;
  
  // 计算其他所有货架已分配的总数量（不包括当前货架）
  let otherAllocatedTotal = 0;
  batchDialog.batches.forEach((b, bIndex) => {
    if (b.shelfList && b.shelfList.length > 0) {
      b.shelfList.forEach((s, sIndex) => {
        // 排除当前货架
        if (!(bIndex === batchIndex && sIndex === shelfIndex)) {
          otherAllocatedTotal += s.allocated || 0;
        }
      });
    }
  });
  
  // 4. 剩余可分配数量 = 总出库数量 - 其他所有货架已分配数量
  const remainingForThisShelf = Math.max(0, batchDialog.totalQuantity - otherAllocatedTotal);
  
  // 取货架可用数量和剩余可分配数量的较小值
  const maxAllocation = Math.min(shelfMax, remainingForThisShelf);
  
  // 确保不会小于0
  return Math.max(0, maxAllocation);
};

const handleShelfAllocationChange = (batchIndex, shelfIndex, newValue) => {
  const batch = batchDialog.batches[batchIndex];
  const shelf = batch.shelfList[shelfIndex];
  
  // 1. 检查批次总可用数量是否小于1
  if (batch.quantity < 1) {
    ElMessage.warning('该批次总可用数量不足，无法分配');
    shelf.allocated = 0;
    updateBatchDialogCalculations();
    return;
  }
  
  // 2. 检查货架可用数量是否小于1
  if (shelf.quantity < 1) {
    ElMessage.warning('该货架可用数量不足，无法分配');
    shelf.allocated = 0;
    updateBatchDialogCalculations();
    return;
  }
  
  // 确保输入的是正整数
  if (newValue !== null && newValue !== undefined) {
    newValue = Math.max(0, Math.floor(newValue));
  } else {
    newValue = 0;
  }
  
  // 3. 获取当前货架的最大可分配数量
  const maxAllocation = getShelfMaxAllocation(batch, shelf, batchIndex, shelfIndex);
  
  // 如果输入值超过最大可分配数量，自动调整
  if (newValue > maxAllocation) {
    newValue = maxAllocation;
    if (maxAllocation > 0) {
      ElMessage.warning(`分配数量不能超过最大可分配数量 ${maxAllocation}`);
    } else {
      ElMessage.warning('当前无可分配数量');
      newValue = 0;
    }
  }
  
  // 4. 检查是否会导致已分配总数超过总出库数量
  const currentAllocated = batchDialog.allocatedQuantity;
  const otherAllocated = currentAllocated - (shelf.allocated || 0);
  const totalAllocated = otherAllocated + newValue;
  
  if (totalAllocated > batchDialog.totalQuantity) {
    const maxAllowed = Math.max(0, batchDialog.totalQuantity - otherAllocated);
    newValue = Math.max(0, maxAllowed);
    ElMessage.warning(`分配总数不能超过出库数量 ${batchDialog.totalQuantity}，当前最多可分配 ${maxAllowed}`);
  }
  
  // 5. 确保分配数量不会导致剩余数量小于0
  const remainingAfterAllocation = batchDialog.totalQuantity - (otherAllocated + newValue);
  if (remainingAfterAllocation < 0) {
    newValue = Math.max(0, batchDialog.totalQuantity - otherAllocated);
    ElMessage.warning('分配数量过多，已自动调整为最大可分配数量');
  }
  
  shelf.allocated = newValue;
  
  // 重新计算批次分配总数和剩余数量
  updateBatchDialogCalculations();
};

const getBatchAllocatedTotal = (batch) => {
  if (!batch.shelfList || batch.shelfList.length === 0) return 0;
  return batch.shelfList.reduce((sum, shelf) => {
    // 只计算可用数量>=1的货架
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
      // 只清空可用数量>=1的货架
      if (shelf.quantity >= 1) {
        shelf.allocated = 0;
      }
    });
  }
  updateBatchDialogCalculations();
};

const updateBatchDialogCalculations = () => {
  // 计算总分配数量
  batchDialog.allocatedQuantity = batchDialog.batches.reduce((sum, batch) => {
    if (batch.quantity >= 1 && batch.shelfList) {
      return sum + batch.shelfList.reduce((shelfSum, shelf) => {
        // 只计算可用数量>=1的货架
        return shelf.quantity >= 1 ? shelfSum + (shelf.allocated || 0) : shelfSum;
      }, 0);
    }
    return sum;
  }, 0);
  
  // 5. 确保剩余数量不会小于0
  batchDialog.remainingQuantity = Math.max(0, batchDialog.totalQuantity - batchDialog.allocatedQuantity);
  
  // 额外检查：如果已分配数量超过总出库数量，进行调整
  if (batchDialog.allocatedQuantity > batchDialog.totalQuantity) {
    console.warn('分配数量异常，进行自动修正');
    // 这里可以添加自动修正逻辑，但通常不应该发生
    batchDialog.allocatedQuantity = batchDialog.totalQuantity;
    batchDialog.remainingQuantity = 0;
  }
};

const autoAllocateBatches = () => {
  let remaining = batchDialog.remainingQuantity;
  
  if (remaining <= 0) {
    ElMessage.warning('已全部分配完成');
    return;
  }
  
  // 重置所有分配（只重置可用数量>=1的批次和货架）
  batchDialog.batches.forEach(batch => {
    if (batch.quantity >= 1 && batch.shelfList) {
      batch.shelfList.forEach(shelf => {
        if (shelf.quantity >= 1) {
          shelf.allocated = 0;
        }
      });
    }
  });
  
  updateBatchDialogCalculations();
  remaining = batchDialog.remainingQuantity;
  
  // 按批次和货架顺序自动分配，只处理可用数量>=1的批次和货架
  for (const batch of batchDialog.batches) {
    if (remaining <= 0) break;
    
    // 1. 跳过总可用数量小于1的批次
    if (batch.quantity < 1) continue;
    
    if (batch.shelfList && batch.shelfList.length > 0) {
      for (const shelf of batch.shelfList) {
        if (remaining <= 0) break;
        
        // 2. 跳过可用数量小于1的货架
        if (shelf.quantity < 1) continue;
        
        // 计算当前可分配的最大数量
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

// 批次分配相关方法（销售出库使用）- 修改为处理所有商品表格
const openBatchDialogForProduct = async (row) => {
  if (!row.productId) {
    ElMessage.warning('产品信息不完整');
    return;
  }

  batchDialog.productIndex = -1; // 不使用索引，直接操作row
  batchDialog.currentRow = row;
  batchDialog.productName = row.productName;
  batchDialog.totalQuantity = row.quantity;
  
  // 加载批次信息
  await loadBatchInfoForProduct(row.productId, formData.warehouseId, row);
  
  // 准备批次数据，包含货架信息
  batchDialog.batches = row.availableBatches.map(batch => ({
    ...batch,
    // 为每个货架添加分配数量字段
    shelfList: batch.shelfList ? batch.shelfList.map(shelf => ({
      ...shelf,
      allocated: 0,
      maxAllocatable: shelf.quantity
    })) : []
  }));

  // 恢复已分配的批次数据
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


const loadBatchInfoForComponent = async (componentProductId) => {
  try {
    const res = await get(`/api/auth/inventory/batches?productId=${componentProductId}&warehouseId=${formData.warehouseId}`);
    console.log('原料批次信息响应:', res);
    
    if (res && Array.isArray(res)) {
      return res;
    }
    return [];
  } catch (error) {
    console.error('加载原料批次信息失败:', error);
    return [];
  }
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

const confirmBatchAllocation = () => {
  // 验证分配数量是否与出库数量一致
  if (batchDialog.remainingQuantity !== 0) {
    ElMessage.warning(`分配数量 (${batchDialog.allocatedQuantity}) 与出库数量 (${batchDialog.totalQuantity}) 不一致，请完成分配`);
    return;
  }

  // 验证是否有无效的分配（分配数量大于可用数量）
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

  // 保存批次分配数据（包含货架信息）
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

// 修改批次信息加载方法
const loadBatchInfo = async (productId, warehouseId, index) => {
  try {
    const res = await get(`/api/auth/inventory/batches?productId=${productId}&warehouseId=${warehouseId}`);
    console.log('批次信息响应:', res);
    
    if (res && Array.isArray(res)) {
      formData.items[index].availableBatches = res;
    } else {
      formData.items[index].availableBatches = [];
    }
  } catch (error) {
    console.error('加载批次信息失败:', error);
    formData.items[index].availableBatches = [];
  }
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
      headers: accessHeader(), // 如果需要认证
      responseType: 'blob', // ⚠️ 必须加
    });

    // 创建 blob 对象
    const blob = new Blob([response.data], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    });

    // 创建 URL 对象
    const url = window.URL.createObjectURL(blob);

    // 创建 a 标签下载
    const a = document.createElement('a');
    a.href = url;
    a.download = '销售出库数量导入模版.xlsx'; // 可自定义文件名
    document.body.appendChild(a);
    a.click();
    a.remove();

    // 释放 URL
    window.URL.revokeObjectURL(url);

  } catch (error) {
    console.error('下载模板失败', error);
    ElMessage.error('下载模板失败，请稍后重试');
  }finally {
    downloadLoading.value = false;
  }

};


// 验证导入数据
const validateImportData = (importedData) => {
  const errors = [];
  
  importedData.forEach((item, index) => {
    // 验证数量
    if (!item.quantity || item.quantity <= 0) {
      errors.push(`第 ${index + 1} 行: 出库数量必须大于0`);
    }
    
    // 验证价格
    if (item.price && item.price < 0) {
      errors.push(`第 ${index + 1} 行: 价格不能为负数`);
    }
    
    // 验证库存是否足够
    const existingProduct = allInventoryProducts.value.find(p => 
      p.productId === item.productId || p.sku === item.sku
    );
    
    if (existingProduct && item.quantity > existingProduct.availableQuantity) {
      errors.push(`第 ${index + 1} 行: 出库数量 ${item.quantity} 超过可用库存 ${existingProduct.availableQuantity}`);
    }
  });
  
  return errors;
};

// 应用导入数据到表格
const applyImportedData = (importedData) => {
  let successCount = 0;
  let failCount = 0;
  
  console.log('导入数据:', importedData);
  
  importedData.forEach(importedItem => {
    // 通过 productId 或 sku 匹配现有产品
    const existingProductIndex = allInventoryProducts.value.findIndex(p => 
      p.productId === importedItem.productId || p.sku === importedItem.sku
    );
    
    if (existingProductIndex >= 0) {
      console.log('匹配产品:', allInventoryProducts.value[existingProductIndex]);
      
      // 直接修改响应式数组中的对象
      const product = allInventoryProducts.value[existingProductIndex];
      
      // 情况1：填写数量，没有填写价格 - 使用导入的数量，价格用接口中的originalPrice
      if (importedItem.quantity && importedItem.quantity > 0 && (!importedItem.price || importedItem.price === 0)) {
        console.log('情况1:', importedItem);
        product.quantity = importedItem.quantity || 0;
        product.price = product.originalPrice || 0; // 使用接口原始价格
        product.priceUnitUsd = product.originalPriceUnitUsd || 0; // 新增：使用接口原始USD价格
      }
      // 情况2：填写数量，填写价格 - 使用导入的数量和价格
      else if (importedItem.quantity && importedItem.quantity > 0 && importedItem.price && importedItem.price > 0) {
        console.log('情况2:', importedItem);
        product.quantity = importedItem.quantity || 0;
        product.price = importedItem.price || 0;
        // 新增：如果导入数据包含USD价格，使用导入的USD价格
        product.priceUnitUsd = importedItem.priceUnitUsd || product.originalPriceUnitUsd || 0;
      }
      // 情况3：没有填写数量，填写价格 - 数量为0，使用导入的价格
      else if ((!importedItem.quantity || importedItem.quantity === 0) && importedItem.price && importedItem.price > 0) {
        console.log('情况3:', importedItem);
        product.quantity = 0;
        product.price = importedItem.price || 0;
        product.priceUnitUsd = importedItem.priceUnitUsd || product.originalPriceUnitUsd || 0;
      }
      // 其他情况：默认处理
      else {
        console.log('情况4:', importedItem);
        product.quantity = importedItem.quantity || 0;
        product.price = importedItem.price || product.originalPrice || 0;
        product.priceUnitUsd = importedItem.priceUnitUsd || product.originalPriceUnitUsd || 0;
      }
      
      product.remark = importedItem.remark || '';
      // 清空之前的批次分配
      product.batchAllocations = [];
      
      console.log('更新后产品:', product);
      successCount++;
    } else {
      console.warn('未找到匹配的产品:', importedItem);
      failCount++;
    }
  });
  
  // 强制触发响应式更新
  allInventoryProducts.value = [...allInventoryProducts.value];
  
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
      // 编辑模式下重新加载数据
      loadOutboundDetail(route.params.id);
    } else {
      // 创建模式下清空表单
      formRef.value?.resetFields();
      formData.items = [];
      fileList.value = [];
      generateOrderNo();
      
      // 清空所有商品表格的数据
      allInventoryProducts.value.forEach(product => {
        product.quantity = 0;
        product.price = 0;
        product.priceUnitUsd = 0; // 新增：清空USD价格
        product.remark = '';
        product.batchAllocations = [];
      });
      
      // 强制更新
      allInventoryProducts.value = [...allInventoryProducts.value];
      
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
    
    const url = isEditMode.value ? '/api/auth/outbound/update' : '/api/auth/outbound/create';
    const res = await post(url, submitData);
    if (res) {
      ElMessage.success(isEditMode.value ? '更新草稿成功' : '保存草稿成功');
      router.push('/index/ckOutboundManage');
    }
  } catch (error) {
    ElMessage.error(isEditMode.value ? '更新草稿失败' : '保存草稿失败');
  } finally {
    loading.value = false;
  }
};

const handleSubmit = async () => {
  if (!await validateForm()) return;
  
  if (formData.orderType === 1) {
    const hasProducts = allInventoryProducts.value.some(p => p.quantity > 0);
    if (!hasProducts) {
      ElMessage.warning('请至少设置一个产品的出库数量');
      return;
    }
  } else {
    if (formData.items.length === 0) {
      ElMessage.warning('请至少添加一个产品');
      return;
    }
  }
  
  // 销售出库：检查批次分配
  if (formData.orderType === 1) {
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
  }
  
  // 生产领料：验证BOM分配
  if (formData.orderType === 2) {
    const validationResult = validateBomAllocations();
    if (!validationResult.valid) {
      ElMessage.warning(validationResult.message);
      return;
    }
  }
  
  loading.value = true;
  try {
    const submitData = prepareSubmitData();
    submitData.status = 1;
    
    const url = isEditMode.value ? '/api/auth/outbound/update' : '/api/auth/outbound/create';
    const res = await post(url, submitData);
    if (res) {
      ElMessage.success(isEditMode.value ? '更新成功' : '提交成功，等待审核');
      router.push('/index/ckOutboundManage');
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
  
  if (formData.orderType === 1) {
    // 销售出库：只提交数量大于0的产品
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
        priceTotal: item.price * (item.quantity || 0), // 新增：总额
        priceUnitUsd: item.priceUnitUsd || 0, // 新增：USD单价
        priceTotalUsd: (item.priceUnitUsd || 0) * (item.quantity || 0), // 新增：USD总额
        batchAllocations: item.batchAllocations || [],
        remark: item.remark || ''
      }));
  } else {
    // 生产领料：使用原有逻辑
    items = formData.items.map(item => ({
      productId: item.productId,
      productName: item.productName,
      sku: item.sku,
      spec: item.spec,
      unit: item.unit,
      currentStock: item.currentStock,
      quantity: item.quantity,
      price: item.price,
      priceUnitUsd: item.priceUnitUsd || 0, // 新增：USD单价
      priceTotalUsd: (item.priceUnitUsd || 0) * (item.quantity || 0), // 新增：USD总额
      batchAllocations: item.batchAllocations || [],
      remark: item.remark || '',
      bomAllocations: item.bomAllocations || []
    }));
  }
  
  return {
    ...formData,
    items: items,
    totalQuantity: totalQuantity.value,
    totalAmount: totalAmount.value,
    totalAmountUsd: totalAmountUsd.value // 新增：USD总额
  };
};


const validateForm = async () => {
  if (!formRef.value) return false;
  
  try {
    await formRef.value.validate();
    
    if (formData.orderType === 1) {
      // 销售出库：检查有数量的产品是否填写完整
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

      // 新增：检查USD价格是否为负数
      const invalidUsdProducts = productsWithQuantity.filter(p => p.priceUnitUsd < 0);
      if (invalidUsdProducts.length > 0) {
        ElMessage.warning('USD单价不能为负数');
        return false;
      }
    } else {
      // 生产领料：原有验证逻辑
      for (let i = 0; i < formData.items.length; i++) {
        const item = formData.items[i];
        if (!item.productId) {
          ElMessage.warning(`请选择第 ${i + 1} 行的产品`);
          return false;
        }
        if (!item.quantity || item.quantity <= 0) {
          ElMessage.warning(`请输入第 ${i + 1} 行产品的有效数量`);
          return false;
        }
        
        // 新增：检查USD价格是否为负数
        if (item.priceUnitUsd < 0) {
          ElMessage.warning(`第 ${i + 1} 行产品的USD单价不能为负数`);
          return false;
        }

        // 销售出库：验证库存
        if (formData.orderType === 1) {
          const currentStock = getCurrentStock(item);
          if (item.quantity > currentStock) {
            ElMessage.warning(`第 ${i + 1} 行产品出库数量超过库存 (当前库存: ${currentStock})`);
            return false;
          }
        }
      }
    }
    
    return true;
  } catch (error) {
    ElMessage.warning('请完善表单信息');
    return false;
  }
};

// 数据加载方法
const loadWarehouseList = async () => {
  try {
    const res = await get('/api/auth/warehouse/listEnable');
    warehouseList.value = res || [];
  } catch (error) {
    ElMessage.error('加载仓库列表失败');
  }
};


// 文件选择处理
const handleFileChange = (file) => {
  // console.log('选择的文件:', file.type);
  // const isExcel = file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' || 
  //                file.type === 'application/vnd.ms-excel';
  // if (!isExcel) {
  //   ElMessage.error('只能上传Excel文件!');
  //   return;
  // }
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
      
      // 验证导入数据
        if (result && Array.isArray(result)) {
          // const validationErrors = validateImportData(result);
          
          // if (validationErrors.length > 0) {
          //   ElMessage.warning({
          //     message: `导入数据存在以下问题：\n${validationErrors.join('\n')}`,
          //     duration: 10000,
          //     showClose: true
          //   });
          //   return;
          // }
          
          // 首先将所有产品的数量重置为0
          allInventoryProducts.value.forEach(product => {
            product.quantity = 0;
            product.price = 0;
            product.remark = '';
            product.batchAllocations = [];
          });
          
          // 应用导入数据
          const { successCount, failCount } = applyImportedData(result);
          
          if (successCount > 0) {
            ElMessage.success(`成功导入 ${successCount} 条产品记录`);
          }
          if (failCount > 0) {
            ElMessage.warning(`${failCount} 条记录未找到匹配的产品`);
          }
          
          // 关闭对话框
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
    
    // 构建产品库存映射
    productStockMap.value = {};
    inventoryList.value.forEach(item => {
      productStockMap.value[item.productId] = item.availableQuantity;
      // 保存接口返回的价格，用于自动填充
      if (item.price && item.price > 0) {
        item.priceFromApi = item.price;
      }
    });
    
    console.log('库存映射表:', productStockMap.value);
    
    // 更新库存产品数据
    updateInventoryProducts();
    
  } catch (error) {
    ElMessage.error('加载库存数据失败');
  }
};

const loadProductionProducts = async () => {
  try {
    const res = await get('/api/auth/product/listEnable');
    productionProductList.value = res?.data || res || [];
    console.log('生产产品列表:', productionProductList.value);
  } catch (error) {
    console.error('加载生产产品列表失败:', error);
    productionProductList.value = [];
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

onMounted(() => {
  if (isEditMode.value) {
    // 编辑模式，加载数据
    loadOutboundDetail(route.params.id);
  } else {
    // 创建模式，生成单号
    generateOrderNo();
  }
  loadWarehouseList();
  loadCustomerList();
  loadProductionProducts();
});

// 监听路由变化，处理直接通过URL进入的情况
watch(
  () => route.params.id,
  (newId) => {
    if (newId) {
      loadOutboundDetail(newId);
    } else {
      // 从编辑模式切换到创建模式
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
      generateOrderNo();
    }
  }
);

// 监听仓库变化，重新加载库存数据
watch(
  () => formData.warehouseId,
  (newWarehouseId) => {
    if (newWarehouseId && formData.orderType === 1) {
      loadInventoryData(newWarehouseId);
    }
  }
);

// 监听仓库变化，重新加载库存数据
watch(
  () => formData.warehouseId,
  (newWarehouseId) => {
    if (newWarehouseId && formData.orderType === 1) {
      loadInventoryData(newWarehouseId);
    } else {
      // 清空产品数据
      allInventoryProducts.value = [];
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

.outbound-form {
  margin-bottom: 30px;
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

.section-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.header-right-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.tip {
  font-size: 12px;
  color: #909399;
}

.bom-tip {
  font-size: 14px;
  color: #909399;
}

.product-table {
  margin-bottom: 16px;
}

.product-name {
  font-weight: 500;
  margin-bottom: 4px;
}

.sku-text {
  font-size: 12px;
  color: #909399;
}

/* BOM相关样式 */
.bom-section {
  margin: 30px 0;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.bom-content {
  padding: 16px;
}

.bom-item-header {
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px dashed #dcdfe6;
}

.bom-item-header h4 {
  margin: 0;
  color: #409eff;
  font-size: 14px;
  margin-bottom: 8px;
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

.allocation-summary {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 12px;
}

.summary-item {
  padding: 4px 8px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.summary-item.insufficient {
  background-color: #fef0f0;
  color: #f56c6c;
  border: 1px solid #fbc4c4;
}

.insufficient-tip {
  color: #f56c6c;
  font-weight: bold;
}

.bom-table {
  margin-bottom: 16px;
}

.allocation-container {
  max-height: 200px;
  overflow-y: auto;
}

.warehouse-allocation {
  margin-bottom: 12px;
  padding: 8px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.warehouse-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 13px;
}

.shelf-allocation {
  margin-left: 12px;
}

.shelf-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
  padding: 4px 8px;
  background-color: white;
  border-radius: 2px;
  gap: 12px;
}

.shelf-info {
  display: flex;
  justify-content: space-between;
  flex: 6;
  font-size: 12px;
}

.allocation-input {
  flex: 4;
  min-width: 100px;
}

.no-shelf-allocation {
  display: flex;
  justify-content: flex-end;
}

.required-quantity {
  color: #e6a23c;
  font-weight: bold;
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
}

.status-success {
  color: #67C23A;
}

.status-warning {
  color: #E6A23C;
}

/* 库存状态样式 */
.stock-none {
  color: #909399;
}

.stock-sufficient {
  color: #67C23A;
}

.stock-low {
  color: #E6A23C;
}

.stock-insufficient {
  color: #F56C6C;
  font-weight: bold;
}

.attachment-section {
  margin-top: 30px;
}

/* 批次分配样式 */
.batch-allocation {
  min-height: 40px;
}

.batch-summary {
  margin-top: 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.batch-tag {
  margin: 2px;
}

.batch-empty {
  margin-top: 8px;
}

.empty-text {
  color: #909399;
  font-size: 12px;
}

/* 批次对话框样式 */
.batch-dialog-content {
  padding: 0 10px;
}

.batch-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16px;
  padding: 12px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.info-item {
  display: flex;
  flex-direction: column;
  align-items: center;
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

.batch-table {
  margin: 16px 0;
}

/* 货架分配样式 - 调整为6:4比例 */
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
  flex: 6; /* 60% 宽度 */
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
  flex: 4; /* 40% 宽度 */
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

/* 分配数量样式 */
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

/* 导入对话框样式 */
.import-dialog-content {
  padding: 20px 0;
}

.upload-demo {
  margin-bottom: 20px;
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

/* 货架禁用状态 */
.shelf-disabled {
  background-color: #f5f7fa;
  opacity: 0.6;
}

.shelf-disabled .shelf-name,
.shelf-disabled .shelf-quantity {
  color: #c0c4cc;
}

/* 禁用状态文本 */
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

/* 批次分配对话框中的禁用状态 */
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
  
  .header-right-actions {
    width: 100%;
    justify-content: flex-start;
    flex-wrap: wrap;
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
  
  .allocation-input {
    width: 100%;
  }
  
  .allocation-summary {
    font-size: 11px;
  }
  
  .batch-info {
    flex-direction: column;
    gap: 8px;
  }
  
  .info-item {
    flex-direction: row;
    justify-content: space-between;
  }
}

/* 添加批次分配样式 */
.batch-allocation {
  margin-bottom: 12px;
  padding: 8px;
  background-color: #f8f9fa;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
}

.batch-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 13px;
  padding: 4px 0;
  border-bottom: 1px dashed #dcdfe6;
}

.batch-info strong {
  color: #409eff;
}

.shelf-allocation {
  margin-left: 8px;
}

.shelf-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
  padding: 4px 8px;
  background-color: white;
  border-radius: 2px;
  gap: 12px;
}

.shelf-info {
  display: flex;
  justify-content: space-between;
  flex: 6;
  font-size: 12px;
}

.allocation-input {
  flex: 4;
  min-width: 100px;
}

.no-shelf-allocation {
  text-align: center;
  padding: 8px;
}

.no-shelf-text {
  color: #909399;
  font-size: 12px;
}
</style>