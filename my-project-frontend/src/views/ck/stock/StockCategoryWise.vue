<!-- 分类盘点 -->
 <template>
  <div class="stock-take-category-container">
    <el-card class="manage-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">分类盘点</span>
          <div class="header-actions">
            <el-button 
              type="primary" 
              @click="handleSave"
              :loading="saving"
              v-if="!isViewMode"
            >
              <el-icon><Document /></el-icon>
              保存草稿
            </el-button>
            <el-button 
              type="success" 
              @click="handleSubmit"
              :loading="submitting"
              v-if="!isViewMode && formData.id"
            >
              <el-icon><CircleCheck /></el-icon>
              提交审批
            </el-button>
            <el-button 
              type="warning" 
              @click="handleBack"
            >
              <el-icon><Back /></el-icon>
              返回列表
            </el-button>
          </div>
        </div>
      </template>

      <!-- 基本信息 -->
      <el-card class="form-section" shadow="never">
        <template #header>
          <div class="section-header">
            <span class="section-title">基本信息</span>
          </div>
        </template>
        <el-form 
          ref="baseFormRef" 
          :model="formData" 
          :rules="formRules" 
          label-width="120px"
          :disabled="isViewMode"
        >
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="盘点单号" prop="orderNo">
                <el-input 
                  v-model="formData.orderNo" 
                  placeholder="系统自动生成"
                  :disabled="true"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="盘点名称" prop="takeName">
                <el-input 
                  v-model="formData.takeName" 
                  placeholder="请输入盘点任务名称"
                  maxlength="100"
                  show-word-limit
                />
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="仓库" prop="warehouseId">
                <el-select
                  v-model="formData.warehouseId"
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
            </el-col>
            <el-col :span="12">
              <el-form-item label="盘点方式" prop="takeType">
                <el-radio-group v-model="formData.takeType">
                  <el-radio :label="1">动态盘点（业务照常）</el-radio>
                  <el-radio :label="2">静态盘点（停止出入库）</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="允许差异率" prop="toleranceRate">
                <el-input-number
                  v-model="formData.toleranceRate"
                  :min="0"
                  :max="100"
                  :step="0.1"
                  :precision="2"
                  style="width: 100%"
                  placeholder="请输入允许差异率"
                >
                  <template #append>%</template>
                </el-input-number>
                <div class="form-tip">超过此差异率将触发复核</div>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="计划盘点时间" prop="planTimeRange">
                <el-date-picker
                  v-model="formData.planTimeRange"
                  type="datetimerange"
                  range-separator="至"
                  start-placeholder="开始时间"
                  end-placeholder="结束时间"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="24">
            <el-col :span="24">
              <el-form-item label="备注" prop="remark">
                <el-input
                  v-model="formData.remark"
                  type="textarea"
                  :rows="3"
                  placeholder="请输入盘点备注信息"
                  maxlength="500"
                  show-word-limit
                />
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </el-card>

      <!-- 分类选择 -->
      <el-card class="form-section" shadow="never">
        <template #header>
          <div class="section-header">
            <span class="section-title">分类选择</span>
            <div class="section-actions">
              <el-button 
                type="primary" 
                size="small"
                @click="handleSelectAllCategories"
                v-if="!isViewMode && categoryTree.length > 0"
              >
                全选所有分类
              </el-button>
              <el-button 
                type="info" 
                size="small"
                @click="handleClearCategories"
                v-if="!isViewMode"
              >
                清空选择
              </el-button>
            </div>
          </div>
        </template>
        
        <div class="category-selector">
          <!-- 分类树 -->
          <div class="category-tree-section">
            <div class="tree-header">
              <span class="tree-title">分类树</span>
              <el-input
                v-model="categoryFilter"
                placeholder="搜索分类名称"
                clearable
                size="small"
                style="width: 200px"
                @keyup.enter="filterCategories"
                @clear="filterCategories"
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
            </div>
            
            <div class="tree-container">
              <el-tree
                ref="categoryTreeRef"
                :data="filteredCategoryTree"
                :props="categoryTreeProps"
                node-key="categoryCode"
                show-checkbox
                :default-expand-all="true"
                :filter-node-method="filterNode"
                @check-change="handleCategoryCheckChange"
                :disabled="isViewMode"
                class="category-tree"
              >
                <template #default="{ node, data }">
                  <div class="tree-node-content">
                    <div class="category-info">
                      <span class="category-name">{{ node.label }}</span>
                      <span class="category-code">{{ data.categoryCode }}</span>
                    </div>
                    <div class="category-stats" v-if="data.statistics">
                      <el-tag size="small" type="info">
                        产品: {{ data.statistics.productCount }}
                      </el-tag>
                      <el-tag size="small" type="info" style="margin-left: 4px">
                        库存: {{ data.statistics.stockQuantity }}
                      </el-tag>
                      <el-tag size="small" type="warning" style="margin-left: 4px">
                        价值: ¥{{ data.statistics.stockValue?.toFixed(2) || 0 }}
                      </el-tag>
                    </div>
                  </div>
                </template>
              </el-tree>
            </div>
          </div>
          
          <!-- 已选分类统计 -->
          <div class="category-selection-section">
            <div class="selection-header">
              <span class="selection-title">已选分类</span>
              <el-button 
                type="text" 
                size="small"
                @click="clearSelection"
                v-if="selectedCategories.length > 0 && !isViewMode"
              >
                清空
              </el-button>
            </div>
            
            <div class="selected-categories" v-if="selectedCategories.length > 0">
              <el-table 
                :data="selectedCategories" 
                border 
                style="width: 100%"
                max-height="300"
                empty-text="未选择任何分类"
              >
                <el-table-column type="index" label="序号" width="60" align="center" />
                <el-table-column label="分类路径" min-width="200">
                  <template #default="{ row }">
                    <div class="category-path">
                      <span v-for="(node, index) in row.path" :key="node.categoryCode">
                        <span class="path-item">{{ node.categoryName }}</span>
                        <span v-if="index < row.path.length - 1" class="path-separator">/</span>
                      </span>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column label="产品数" width="100" align="center">
                  <template #default="{ row }">
                    {{ row.statistics?.productCount || 0 }}
                  </template>
                </el-table-column>
                <el-table-column label="库存数量" width="120" align="center">
                  <template #default="{ row }">
                    {{ row.statistics?.stockQuantity || 0 }}
                  </template>
                </el-table-column>
                <el-table-column label="库存价值" width="120" align="right">
                  <template #default="{ row }">
                    ¥{{ (row.statistics?.stockValue || 0).toFixed(2) }}
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="80" align="center" v-if="!isViewMode">
                  <template #default="{ row }">
                    <el-button
                      type="danger"
                      link
                      size="small"
                      @click="removeCategory(row.categoryCode)"
                    >
                      移除
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
            
            <div class="no-selection" v-else>
              <el-empty description="请从左侧分类树中选择分类" />
            </div>
            
            <!-- 分类统计摘要 -->
            <div class="category-summary" v-if="selectedCategories.length > 0">
              <div class="summary-header">
                <span class="summary-title">分类统计摘要</span>
              </div>
              <el-descriptions :column="4" border>
                <el-descriptions-item label="选择分类数">{{ selectedCategories.length }} 个</el-descriptions-item>
                <el-descriptions-item label="涉及产品数">{{ categorySummary.productCount }} 个</el-descriptions-item>
                <el-descriptions-item label="总库存数量">{{ categorySummary.stockQuantity }}</el-descriptions-item>
                <el-descriptions-item label="总库存价值">¥{{ categorySummary.stockValue.toFixed(2) }}</el-descriptions-item>
                <el-descriptions-item label="一级分类数">{{ categorySummary.level1Count }} 个</el-descriptions-item>
                <el-descriptions-item label="二级分类数">{{ categorySummary.level2Count }} 个</el-descriptions-item>
                <el-descriptions-item label="三级分类数">{{ categorySummary.level3Count }} 个</el-descriptions-item>
                <el-descriptions-item label="平均产品数">{{ categorySummary.avgProductCount }} 个</el-descriptions-item>
              </el-descriptions>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 盘点策略设置 -->
      <el-card class="form-section" shadow="never">
        <template #header>
          <div class="section-header">
            <span class="section-title">盘点策略设置</span>
          </div>
        </template>
        
        <el-form 
          :model="strategyConfig" 
          label-width="120px"
          :disabled="isViewMode"
        >
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="盘点模式">
                <el-radio-group v-model="strategyConfig.countMode">
                  <el-radio :label="1">盲盘（不显示系统库存）</el-radio>
                  <el-radio :label="2">明盘（显示系统库存）</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="分类盘点顺序">
                <el-select 
                  v-model="strategyConfig.categoryOrder" 
                  placeholder="请选择分类盘点顺序"
                  style="width: 100%"
                >
                  <el-option :label="按分类层级-上到下" :value="1" />
                  <el-option :label="按产品数量-多到少" :value="2" />
                  <el-option :label="按库存价值-高到低" :value="3" />
                  <el-option :label="按分类编码顺序" :value="4" />
                  <el-option :label="随机顺序" :value="5" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="是否包含子分类">
                <el-switch
                  v-model="strategyConfig.includeSubCategories"
                  active-text="包含"
                  inactive-text="不包含"
                />
                <div class="form-tip">开启后，选择父分类时自动包含所有子分类</div>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="并行盘点分类数" v-if="strategyConfig.parallelCount">
                <el-input-number
                  v-model="strategyConfig.maxParallelCategories"
                  :min="1"
                  :max="10"
                  :step="1"
                  style="width: 100%"
                  placeholder="请输入最大并行分类数"
                />
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="分类并行盘点">
                <el-switch
                  v-model="strategyConfig.parallelCount"
                  active-text="允许"
                  inactive-text="不允许"
                />
                <div class="form-tip">开启后，多个分类可同时进行盘点</div>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="是否需要复盘">
                <el-switch
                  v-model="strategyConfig.needSecondCount"
                  active-text="需要"
                  inactive-text="不需要"
                />
                <div class="form-tip">开启后，差异超过允许范围将自动触发复盘</div>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="复盘阈值" v-if="strategyConfig.needSecondCount">
                <el-input-number
                  v-model="strategyConfig.secondCountThreshold"
                  :min="0.1"
                  :max="100"
                  :step="0.5"
                  :precision="2"
                  style="width: 100%"
                  placeholder="请输入复盘阈值"
                >
                  <template #append>%</template>
                </el-input-number>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="盘点人员配置">
                <el-select
                  v-model="strategyConfig.categoryPersonConfig"
                  placeholder="请选择人员配置"
                  style="width: 100%"
                >
                  <el-option :label="单人负责多个分类" :value="1" />
                  <el-option :label="团队按分类分配" :value="2" />
                  <el-option :label="专家负责特定分类" :value="3" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="盘点设备">
                <el-checkbox-group v-model="strategyConfig.devices">
                  <el-checkbox :label="1">PDA</el-checkbox>
                  <el-checkbox :label="2">RFID扫描枪</el-checkbox>
                  <el-checkbox :label="3">平板电脑</el-checkbox>
                  <el-checkbox :label="4">纸质盘点单</el-checkbox>
                </el-checkbox-group>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="分类报告生成">
                <el-switch
                  v-model="strategyConfig.generateCategoryReport"
                  active-text="生成"
                  inactive-text="不生成"
                />
                <div class="form-tip">开启后，为每个分类生成单独的盘点报告</div>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="自动生成调整单">
                <el-switch
                  v-model="strategyConfig.autoGenerateAdjust"
                  active-text="自动生成"
                  inactive-text="手动生成"
                />
                <div class="form-tip">开启后，盘点完成后自动生成库存调整单</div>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="重点分类标识">
                <el-switch
                  v-model="strategyConfig.highlightKeyCategories"
                  active-text="标识"
                  inactive-text="不标识"
                />
                <div class="form-tip">开启后，对库存价值高的分类进行重点标识</div>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </el-card>

      <!-- 产品预览 -->
      <el-card class="form-section" shadow="never" v-if="selectedCategories.length > 0">
        <template #header>
          <div class="section-header">
            <span class="section-title">涉及产品预览</span>
            <div class="section-actions">
              <el-button 
                type="primary" 
                size="small"
                @click="loadProductsPreview"
                :loading="productLoading"
              >
                <el-icon><Refresh /></el-icon>
                刷新产品列表
              </el-button>
            </div>
          </div>
        </template>
        
        <div class="product-preview">
          <div class="preview-tabs">
            <el-tabs v-model="previewTab">
              <el-tab-pane label="按分类查看" name="byCategory">
                <div class="category-products" v-if="categoryProducts.length > 0">
                  <div 
                    v-for="category in categoryProducts" 
                    :key="category.categoryCode"
                    class="category-product-group"
                  >
                    <div class="group-header">
                      <div class="category-title">
                        <span class="category-name">{{ category.categoryName }}</span>
                        <span class="product-count">（{{ category.products.length }} 个产品）</span>
                      </div>
                      <div class="category-stats">
                        <el-tag size="small" type="info">
                          库存数量: {{ category.stockQuantity }}
                        </el-tag>
                        <el-tag size="small" type="warning" style="margin-left: 8px">
                          库存价值: ¥{{ category.stockValue.toFixed(2) }}
                        </el-tag>
                      </div>
                    </div>
                    
                    <el-table 
                      :data="category.products" 
                      border 
                      style="width: 100%"
                      class="product-table"
                      :show-header="category.showHeader"
                    >
                      <el-table-column type="index" label="序号" width="60" align="center" />
                      <el-table-column label="产品信息" min-width="250">
                        <template #default="{ row }">
                          <div class="product-info">
                            <div class="product-name">{{ row.name }}</div>
                            <div class="product-sku">SKU: {{ row.sku }}</div>
                          </div>
                        </template>
                      </el-table-column>
                      <el-table-column label="规格型号" width="120" prop="spec" />
                      <el-table-column label="单位" width="80" align="center" prop="unitCode" />
                      <el-table-column label="当前库存" width="100" align="center">
                        <template #default="{ row }">
                          {{ row.quantity || 0 }}
                        </template>
                      </el-table-column>
                      <el-table-column label="库存价值" width="120" align="right">
                        <template #default="{ row }">
                          ¥{{ ((row.quantity || 0) * (row.unitPrice || 0)).toFixed(2) }}
                        </template>
                      </el-table-column>
                      <el-table-column label="批次管理" width="100" align="center">
                        <template #default="{ row }">
                          <el-tag :type="row.batchManage ? 'success' : 'info'" size="small">
                            {{ row.batchManage ? '需要' : '不需要' }}
                          </el-tag>
                        </template>
                      </el-table-column>
                      <el-table-column label="货位管理" width="100" align="center">
                        <template #default="{ row }">
                          <el-tag :type="row.shelfManage ? 'success' : 'info'" size="small">
                            {{ row.shelfManage ? '需要' : '不需要' }}
                          </el-tag>
                        </template>
                      </el-table-column>
                    </el-table>
                  </div>
                </div>
                <div class="no-products" v-else>
                  <el-empty description="暂无产品数据，请点击刷新按钮加载" />
                </div>
              </el-tab-pane>
              
              <el-tab-pane label="全部产品" name="allProducts">
                <el-table 
                  :data="allProducts" 
                  border 
                  style="width: 100%"
                  v-loading="productLoading"
                  class="all-products-table"
                >
                  <el-table-column type="index" label="序号" width="60" align="center" />
                  <el-table-column label="产品信息" min-width="250">
                    <template #default="{ row }">
                      <div class="product-info">
                        <div class="product-name">{{ row.name }}</div>
                        <div class="product-sku">SKU: {{ row.sku }}</div>
                        <div class="product-category">分类: {{ row.categoryName }}</div>
                      </div>
                    </template>
                  </el-table-column>
                  <el-table-column label="规格型号" width="120" prop="spec" />
                  <el-table-column label="单位" width="80" align="center" prop="unitCode" />
                  <el-table-column label="当前库存" width="100" align="center">
                    <template #default="{ row }">
                      {{ row.quantity || 0 }}
                    </template>
                  </el-table-column>
                  <el-table-column label="库存价值" width="120" align="right">
                    <template #default="{ row }">
                      ¥{{ ((row.quantity || 0) * (row.unitPrice || 0)).toFixed(2) }}
                    </template>
                  </el-table-column>
                  <el-table-column label="所属分类" width="150">
                    <template #default="{ row }">
                      {{ row.categoryName }}
                    </template>
                  </el-table-column>
                  <el-table-column label="分类层级" width="100" align="center">
                    <template #default="{ row }">
                      L{{ row.categoryLevel }}
                    </template>
                  </el-table-column>
                </el-table>
                
                <!-- 分页 -->
                <div class="product-pagination" v-if="productPagination.total > 20">
                  <el-pagination
                    v-model:current-page="productPagination.current"
                    v-model:page-size="productPagination.size"
                    :total="productPagination.total"
                    :page-sizes="[20, 50, 100]"
                    layout="total, sizes, prev, pager, next"
                    @size-change="handleProductSizeChange"
                    @current-change="handleProductCurrentChange"
                  />
                </div>
              </el-tab-pane>
            </el-tabs>
          </div>
          
          <div class="preview-summary" v-if="allProducts.length > 0">
            <el-descriptions :column="4" border>
              <el-descriptions-item label="总产品数">{{ productSummary.totalProducts }} 个</el-descriptions-item>
              <el-descriptions-item label="总库存数量">{{ productSummary.totalQuantity }}</el-descriptions-item>
              <el-descriptions-item label="总库存价值">¥{{ productSummary.totalValue.toFixed(2) }}</el-descriptions-item>
              <el-descriptions-item label="平均库存价值">¥{{ productSummary.avgValue.toFixed(2) }}</el-descriptions-item>
              <el-descriptions-item label="批次管理产品">{{ productSummary.batchProducts }} 个</el-descriptions-item>
              <el-descriptions-item label="货位管理产品">{{ productSummary.shelfProducts }} 个</el-descriptions-item>
              <el-descriptions-item label="零库存产品">{{ productSummary.zeroStockProducts }} 个</el-descriptions-item>
              <el-descriptions-item label="高价值产品（>¥1000）">{{ productSummary.highValueProducts }} 个</el-descriptions-item>
            </el-descriptions>
          </div>
        </div>
      </el-card>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch, nextTick } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Document, CircleCheck, Back, Search, Refresh } from '@element-plus/icons-vue';
import { post, get } from '@/net';

const route = useRoute();
const router = useRouter();
const baseFormRef = ref(null);
const categoryTreeRef = ref(null);

const saving = ref(false);
const submitting = ref(false);
const productLoading = ref(false);

// 判断是否为查看模式
const isViewMode = computed(() => route.name === 'CkStockTakeCategoryView' || route.params.view === 'true');

// 表单数据
const formData = reactive({
  id: null,
  orderNo: '',
  takeName: '',
  takeType: 1, // 1-动态盘点, 2-静态盘点
  takeStrategy: 3, // 3-分类盘点
  warehouseId: null,
  toleranceRate: 0.5, // 默认0.5%
  planTimeRange: [],
  remark: '',
  
  // 额外字段
  planStartTime: '',
  planEndTime: '',
  status: 0,
  approvalStatus: 0,
  
  // 分类选择
  categoryCodes: [],
  includeSubCategories: true
});

// 表单验证规则
const formRules = {
  takeName: [
    { required: true, message: '请输入盘点名称', trigger: 'blur' },
    { max: 100, message: '盘点名称不能超过100个字符', trigger: 'blur' }
  ],
  warehouseId: [
    { required: true, message: '请选择仓库', trigger: 'change' }
  ],
  takeType: [
    { required: true, message: '请选择盘点方式', trigger: 'change' }
  ],
  toleranceRate: [
    { required: true, message: '请输入允许差异率', trigger: 'blur' },
    { type: 'number', min: 0, max: 100, message: '差异率必须在0-100之间', trigger: 'blur' }
  ],
  planTimeRange: [
    { required: true, message: '请选择计划盘点时间', trigger: 'change' }
  ],
  categoryCodes: [
    { required: true, message: '请至少选择一个分类', trigger: 'change' }
  ]
};

// 盘点策略配置
const strategyConfig = reactive({
  countMode: 1, // 1-盲盘, 2-明盘
  categoryOrder: 1, // 1-按分类层级, 2-按产品数量, 3-按库存价值, 4-按分类编码, 5-随机顺序
  includeSubCategories: true,
  parallelCount: false,
  maxParallelCategories: 3,
  needSecondCount: true,
  secondCountThreshold: 5, // 默认5%
  categoryPersonConfig: 1, // 1-单人负责多个分类, 2-团队按分类分配, 3-专家负责特定分类
  devices: [1, 4], // 默认PDA和纸质盘点单
  autoGenerateAdjust: true,
  generateCategoryReport: true,
  highlightKeyCategories: true
});

// 分类树配置
const categoryFilter = ref('');
const categoryTree = ref([]);
const filteredCategoryTree = ref([]);
const selectedCategories = ref([]);

// 产品预览
const previewTab = ref('byCategory');
const categoryProducts = ref([]);
const allProducts = ref([]);
const productPagination = reactive({
  current: 1,
  size: 20,
  total: 0
});

// 数据列表
const warehouseList = ref([]);

// 计算属性
const categoryTreeProps = {
  children: 'children',
  label: 'categoryName',
  disabled: (data) => {
    // 如果选择父分类时包含子分类，则禁用子分类
    if (strategyConfig.includeSubCategories) {
      return false;
    }
    return false;
  }
};

const categorySummary = computed(() => {
  const result = {
    productCount: 0,
    stockQuantity: 0,
    stockValue: 0,
    level1Count: 0,
    level2Count: 0,
    level3Count: 0,
    avgProductCount: 0
  };
  
  selectedCategories.value.forEach(category => {
    if (category.statistics) {
      result.productCount += category.statistics.productCount || 0;
      result.stockQuantity += category.statistics.stockQuantity || 0;
      result.stockValue += category.statistics.stockValue || 0;
    }
    
    // 统计层级
    if (category.level === 1) result.level1Count++;
    else if (category.level === 2) result.level2Count++;
    else if (category.level === 3) result.level3Count++;
  });
  
  if (selectedCategories.value.length > 0) {
    result.avgProductCount = Math.round(result.productCount / selectedCategories.value.length);
  }
  
  return result;
});

const productSummary = computed(() => {
  const result = {
    totalProducts: allProducts.value.length,
    totalQuantity: 0,
    totalValue: 0,
    avgValue: 0,
    batchProducts: 0,
    shelfProducts: 0,
    zeroStockProducts: 0,
    highValueProducts: 0
  };
  
  allProducts.value.forEach(product => {
    const quantity = product.quantity || 0;
    const unitPrice = product.unitPrice || 0;
    const value = quantity * unitPrice;
    
    result.totalQuantity += quantity;
    result.totalValue += value;
    
    if (product.batchManage) result.batchProducts++;
    if (product.shelfManage) result.shelfProducts++;
    if (quantity === 0) result.zeroStockProducts++;
    if (value > 1000) result.highValueProducts++;
  });
  
  if (result.totalProducts > 0) {
    result.avgValue = result.totalValue / result.totalProducts;
  }
  
  return result;
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

const loadCategoryTree = async () => {
  try {
    const res = await get('/api/auth/product-category/tree-with-stats', {
      warehouseId: formData.warehouseId
    });
    
    if (res && res.length > 0) {
      categoryTree.value = res;
      filteredCategoryTree.value = [...categoryTree.value];
    } else {
      categoryTree.value = [];
      filteredCategoryTree.value = [];
    }
  } catch (error) {
    console.error('加载分类树失败:', error);
    categoryTree.value = [];
    filteredCategoryTree.value = [];
  }
};

const loadCategoryStatistics = async () => {
  if (!formData.warehouseId) return;
  
  try {
    const res = await post('/api/auth/product-category/statistics', {
      warehouseId: formData.warehouseId,
      categoryCodes: selectedCategories.value.map(c => c.categoryCode)
    });
    
    if (res && res.length > 0) {
      // 更新选中分类的统计数据
      selectedCategories.value.forEach(category => {
        const stat = res.find(s => s.categoryCode === category.categoryCode);
        if (stat) {
          category.statistics = stat;
        }
      });
      
      // 更新分类树的统计数据
      updateCategoryTreeStats(res);
    }
  } catch (error) {
    console.error('加载分类统计失败:', error);
  }
};

const updateCategoryTreeStats = (stats) => {
  const updateNodeStats = (nodes) => {
    nodes.forEach(node => {
      const stat = stats.find(s => s.categoryCode === node.categoryCode);
      if (stat) {
        node.statistics = stat;
      }
      
      if (node.children && node.children.length > 0) {
        updateNodeStats(node.children);
      }
    });
  };
  
  updateNodeStats(categoryTree.value);
  // 刷新过滤后的树
  filteredCategoryTree.value = [...categoryTree.value];
};

const loadProductsPreview = async () => {
  if (selectedCategories.value.length === 0) {
    ElMessage.warning('请先选择分类');
    return;
  }
  
  productLoading.value = true;
  try {
    const params = {
      warehouseId: formData.warehouseId,
      categoryCodes: selectedCategories.value.map(c => c.categoryCode),
      page: productPagination.current,
      size: productPagination.size,
      includeSubCategories: strategyConfig.includeSubCategories
    };
    
    const res = await post('/api/auth/product/category-products', params);
    
    if (res && res.records) {
      allProducts.value = res.records.map(product => ({
        id: product.id,
        sku: product.sku,
        name: product.name,
        spec: product.spec,
        unitCode: product.unitCode,
        categoryCode: product.categoryCode,
        categoryName: product.categoryName,
        categoryLevel: product.categoryLevel || 1,
        quantity: product.quantity || 0,
        unitPrice: product.unitPrice || 0,
        batchManage: product.batchManage || false,
        shelfManage: product.shelfManage || false
      }));
      
      productPagination.total = res.total || 0;
      
      // 按分类分组
      groupProductsByCategory();
    } else {
      allProducts.value = [];
      categoryProducts.value = [];
      productPagination.total = 0;
    }
  } catch (error) {
    console.error('加载产品预览失败:', error);
    allProducts.value = [];
    categoryProducts.value = [];
    ElMessage.error('加载产品数据失败');
  } finally {
    productLoading.value = false;
  }
};

const groupProductsByCategory = () => {
  const categoryMap = new Map();
  
  allProducts.value.forEach(product => {
    const categoryCode = product.categoryCode;
    if (!categoryMap.has(categoryCode)) {
      categoryMap.set(categoryCode, {
        categoryCode,
        categoryName: product.categoryName,
        products: [],
        stockQuantity: 0,
        stockValue: 0,
        showHeader: true
      });
    }
    
    const category = categoryMap.get(categoryCode);
    category.products.push(product);
    
    const quantity = product.quantity || 0;
    const unitPrice = product.unitPrice || 0;
    category.stockQuantity += quantity;
    category.stockValue += quantity * unitPrice;
  });
  
  categoryProducts.value = Array.from(categoryMap.values());
};

const loadStockTakeDetail = async (id) => {
  try {
    const res = await get(`/api/auth/stock-take/category-detail?id=${id}`);
    if (res) {
      // 填充表单数据
      Object.assign(formData, {
        id: res.id,
        orderNo: res.orderNo,
        takeName: res.takeName,
        takeType: res.takeType,
        warehouseId: res.warehouseId,
        toleranceRate: res.toleranceRate,
        planTimeRange: res.planStartTime && res.planEndTime ? 
          [res.planStartTime, res.planEndTime] : [],
        remark: res.remark,
        status: res.status,
        approvalStatus: res.approvalStatus,
        categoryCodes: res.categoryCodes || []
      });
      
      // 填充策略配置
      if (res.strategyConfig) {
        Object.assign(strategyConfig, JSON.parse(res.strategyConfig));
      }
      
      // 加载仓库分类树
      await loadCategoryTree();
      
      // 填充分类选择
      if (res.categoryCodes && res.categoryCodes.length > 0) {
        setTimeout(() => {
          const selectedCodes = res.categoryCodes;
          selectedCategories.value = [];
          
          // 遍历树找到选中的分类
          const findAndSelectCategories = (nodes, path = []) => {
            nodes.forEach(node => {
              const currentPath = [...path, { 
                categoryCode: node.categoryCode, 
                categoryName: node.categoryName 
              }];
              
              if (selectedCodes.includes(node.categoryCode)) {
                selectedCategories.value.push({
                  categoryCode: node.categoryCode,
                  categoryName: node.categoryName,
                  level: node.level || 1,
                  path: currentPath,
                  statistics: node.statistics
                });
              }
              
              if (node.children && node.children.length > 0) {
                findAndSelectCategories(node.children, currentPath);
              }
            });
          };
          
          findAndSelectCategories(categoryTree.value);
          
          // 设置树的选择状态
          if (categoryTreeRef.value) {
            categoryTreeRef.value.setCheckedKeys(selectedCodes);
          }
          
          // 加载分类统计
          loadCategoryStatistics();
          
          // 加载产品预览
          loadProductsPreview();
        }, 500);
      }
    }
  } catch (error) {
    console.error('加载盘点单详情失败:', error);
    ElMessage.error('加载数据失败');
  }
};

const handleWarehouseChange = () => {
  // 清空分类选择
  selectedCategories.value = [];
  // 加载分类树
  loadCategoryTree();
};

const filterCategories = () => {
  if (!categoryTreeRef.value) return;
  
  if (categoryFilter.value) {
    categoryTreeRef.value.filter(categoryFilter.value);
  } else {
    // 重置过滤
    const resetFilter = (nodes) => {
      nodes.forEach(node => {
        node.visible = true;
        if (node.children) {
          resetFilter(node.children);
        }
      });
    };
    resetFilter(filteredCategoryTree.value);
  }
};

const filterNode = (value, data) => {
  if (!value) return true;
  return data.categoryName.includes(value) || data.categoryCode.includes(value);
};

const handleCategoryCheckChange = (data, checked, indeterminate) => {
  if (isViewMode.value) return;
  
  const categoryCode = data.categoryCode;
  const categoryName = data.categoryName;
  const level = data.level || 1;
  
  if (checked) {
    // 如果包含子分类，且选择的是父分类，需要处理子分类
    if (strategyConfig.includeSubCategories && data.children && data.children.length > 0) {
      // 获取所有子分类的编码
      const getAllChildrenCodes = (node) => {
        let codes = [node.categoryCode];
        if (node.children && node.children.length > 0) {
          node.children.forEach(child => {
            codes = codes.concat(getAllChildrenCodes(child));
          });
        }
        return codes;
      };
      
      const allCodes = getAllChildrenCodes(data);
      
      // 添加选中的分类和子分类
      allCodes.forEach(code => {
        if (!selectedCategories.value.some(c => c.categoryCode === code)) {
          selectedCategories.value.push({
            categoryCode: code,
            categoryName: data.categoryName, // 这里可能需要根据code找到具体的分类名
            level: level,
            path: [{ categoryCode: data.categoryCode, categoryName: data.categoryName }],
            statistics: data.statistics
          });
        }
      });
    } else {
      // 不包含子分类，只添加当前分类
      if (!selectedCategories.value.some(c => c.categoryCode === categoryCode)) {
        selectedCategories.value.push({
          categoryCode,
          categoryName,
          level,
          path: [{ categoryCode, categoryName }],
          statistics: data.statistics
        });
      }
    }
  } else {
    // 移除分类
    const index = selectedCategories.value.findIndex(c => c.categoryCode === categoryCode);
    if (index !== -1) {
      selectedCategories.value.splice(index, 1);
    }
    
    // 如果包含子分类，且移除的是父分类，需要移除所有子分类
    if (strategyConfig.includeSubCategories && data.children && data.children.length > 0) {
      const getAllChildrenCodes = (node) => {
        let codes = [node.categoryCode];
        if (node.children && node.children.length > 0) {
          node.children.forEach(child => {
            codes = codes.concat(getAllChildrenCodes(child));
          });
        }
        return codes;
      };
      
      const allCodes = getAllChildrenCodes(data);
      selectedCategories.value = selectedCategories.value.filter(
        c => !allCodes.includes(c.categoryCode)
      );
    }
  }
  
  // 更新表单数据
  formData.categoryCodes = selectedCategories.value.map(c => c.categoryCode);
  
  // 加载分类统计
  if (selectedCategories.value.length > 0) {
    loadCategoryStatistics();
  }
};

const handleSelectAllCategories = () => {
  if (isViewMode.value) return;
  
  if (!categoryTreeRef.value) return;
  
  // 获取所有节点
  const getAllNodes = (nodes) => {
    let allNodes = [];
    nodes.forEach(node => {
      allNodes.push(node);
      if (node.children && node.children.length > 0) {
        allNodes = allNodes.concat(getAllNodes(node.children));
      }
    });
    return allNodes;
  };
  
  const allNodes = getAllNodes(categoryTree.value);
  const allKeys = allNodes.map(node => node.categoryCode);
  
  categoryTreeRef.value.setCheckedKeys(allKeys);
  
  // 更新选中分类列表
  selectedCategories.value = allNodes.map(node => ({
    categoryCode: node.categoryCode,
    categoryName: node.categoryName,
    level: node.level || 1,
    path: [{ categoryCode: node.categoryCode, categoryName: node.categoryName }],
    statistics: node.statistics
  }));
  
  formData.categoryCodes = allKeys;
  ElMessage.success('已选择所有分类');
};

const handleClearCategories = () => {
  if (isViewMode.value) return;
  
  if (categoryTreeRef.value) {
    categoryTreeRef.value.setCheckedKeys([]);
  }
  
  selectedCategories.value = [];
  formData.categoryCodes = [];
  ElMessage.info('已清空分类选择');
};

const clearSelection = () => {
  if (isViewMode.value) return;
  
  selectedCategories.value = [];
  formData.categoryCodes = [];
  
  if (categoryTreeRef.value) {
    categoryTreeRef.value.setCheckedKeys([]);
  }
  
  ElMessage.info('已清空选择');
};

const removeCategory = (categoryCode) => {
  if (isViewMode.value) return;
  
  const index = selectedCategories.value.findIndex(c => c.categoryCode === categoryCode);
  if (index !== -1) {
    selectedCategories.value.splice(index, 1);
    formData.categoryCodes = selectedCategories.value.map(c => c.categoryCode);
    
    // 同步更新树的选择状态
    if (categoryTreeRef.value) {
      categoryTreeRef.value.setChecked(categoryCode, false, false);
    }
    
    ElMessage.success('移除成功');
  }
};

const handleProductSizeChange = (size) => {
  productPagination.size = size;
  productPagination.current = 1;
  loadProductsPreview();
};

const handleProductCurrentChange = (page) => {
  productPagination.current = page;
  loadProductsPreview();
};

const validateForm = async () => {
  if (!baseFormRef.value) return false;
  
  try {
    await baseFormRef.value.validate();
    
    // 检查是否有选择分类
    if (selectedCategories.value.length === 0) {
      ElMessage.warning('请至少选择一个盘点分类');
      return false;
    }
    
    // 检查时间范围
    if (formData.planTimeRange && formData.planTimeRange.length === 2) {
      const startTime = new Date(formData.planTimeRange[0]);
      const endTime = new Date(formData.planTimeRange[1]);
      
      if (startTime >= endTime) {
        ElMessage.warning('结束时间必须晚于开始时间');
        return false;
      }
    }
    
    return true;
  } catch (error) {
    console.error('表单验证失败:', error);
    return false;
  }
};

const handleSave = async () => {
  const isValid = await validateForm();
  if (!isValid) return;
  
  saving.value = true;
  try {
    // 处理时间范围
    const planStartTime = formData.planTimeRange && formData.planTimeRange[0] 
      ? formData.planTimeRange[0] 
      : null;
    const planEndTime = formData.planTimeRange && formData.planTimeRange[1] 
      ? formData.planTimeRange[1] 
      : null;
    
    const saveData = {
      ...formData,
      planStartTime,
      planEndTime,
      categoryCodes: selectedCategories.value.map(c => c.categoryCode),
      categoryNames: selectedCategories.value.map(c => c.categoryName),
      strategyConfig: JSON.stringify(strategyConfig),
      takeStrategy: 3, // 分类盘点
      status: 0, // 草稿状态
      approvalStatus: 0 // 未提交审批
    };
    
    // 删除不需要的字段
    delete saveData.planTimeRange;
    
    const res = await post('/api/auth/stock-take/category/save', saveData);
    
    if (res) {
      formData.id = res.id;
      formData.orderNo = res.orderNo || formData.orderNo;
      ElMessage.success('保存成功');
      
      // 如果是新建保存，更新URL中的ID
      if (route.params.id && route.params.id !== res.id) {
        router.replace(`/index/ckStockTakeCategoryEdit/${res.id}`);
      }
    }
  } catch (error) {
    console.error('保存失败:', error);
    ElMessage.error('保存失败: ' + (error.message || '未知错误'));
  } finally {
    saving.value = false;
  }
};

const handleSubmit = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要提交分类盘点单"${formData.takeName}"审批吗？提交后不可修改。`,
      '提交审批确认',
      { 
        type: 'warning',
        confirmButtonText: '确定提交',
        cancelButtonText: '取消'
      }
    );
    
    const isValid = await validateForm();
    if (!isValid) return;
    
    submitting.value = true;
    
    const submitData = {
      id: formData.id,
      status: 1, // 提交后状态变为审批中
      approvalStatus: 1 // 审核中
    };
    
    const res = await post('/api/auth/stock-take/category/submit', submitData);
    
    if (res) {
      ElMessage.success('提交成功，已进入审批流程');
      router.push('/index/ckStockTakeManage');
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('提交失败:', error);
      ElMessage.error('提交失败');
    }
  } finally {
    submitting.value = false;
  }
};

const handleBack = () => {
  router.push('/index/ckStockTakeManage');
};

// 生成盘点单号
const generateOrderNo = () => {
  const prefix = 'PD';
  const timestamp = new Date().getTime().toString().slice(-6);
  const random = Math.floor(Math.random() * 1000).toString().padStart(3, '0');
  formData.orderNo = `${prefix}C${timestamp}${random}`;
};

// 初始化页面
const initPage = async () => {
  await loadWarehouseList();
  
  // 检查是否是编辑或查看模式
  const id = route.params.id;
  if (id && id !== 'create') {
    await loadStockTakeDetail(id);
  } else {
    // 新建模式，生成单号
    generateOrderNo();
  }
};

// 监听仓库变化
watch(() => formData.warehouseId, (newVal) => {
  if (newVal) {
    loadCategoryTree();
  }
});

// 监听分类选择变化
watch(() => selectedCategories.value.length, (newVal) => {
  if (newVal > 0) {
    formData.categoryCodes = selectedCategories.value.map(c => c.categoryCode);
  }
});

// 监听是否包含子分类设置变化
watch(() => strategyConfig.includeSubCategories, (newVal) => {
  // 如果设置变化，清空当前选择重新选择
  if (selectedCategories.value.length > 0) {
    ElMessageBox.confirm(
      '包含子分类设置已更改，需要重新选择分类，是否继续？',
      '设置变更提示',
      {
        type: 'warning',
        confirmButtonText: '继续',
        cancelButtonText: '取消'
      }
    ).then(() => {
      selectedCategories.value = [];
      formData.categoryCodes = [];
      if (categoryTreeRef.value) {
        categoryTreeRef.value.setCheckedKeys([]);
      }
    }).catch(() => {
      // 取消变更，恢复原设置
      strategyConfig.includeSubCategories = !newVal;
    });
  }
});

onMounted(() => {
  initPage();
});
</script>

<style scoped>
.stock-take-category-container {
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

.form-section {
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

.section-actions {
  display: flex;
  gap: 8px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.category-selector {
  display: flex;
  gap: 20px;
  min-height: 500px;
}

.category-tree-section {
  flex: 1;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 16px;
  background: white;
}

.tree-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
}

.tree-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.tree-container {
  height: 400px;
  overflow-y: auto;
}

.category-tree {
  font-size: 14px;
}

.tree-node-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  padding: 4px 0;
}

.category-info {
  display: flex;
  flex-direction: column;
}

.category-name {
  font-weight: 500;
  color: #303133;
  margin-bottom: 2px;
}

.category-code {
  font-size: 12px;
  color: #909399;
}

.category-stats {
  display: flex;
  gap: 4px;
}

.category-selection-section {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.selection-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
}

.selection-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.selected-categories {
  flex: 1;
  margin-bottom: 16px;
}

.category-path {
  display: flex;
  align-items: center;
}

.path-item {
  color: #606266;
}

.path-separator {
  color: #c0c4cc;
  margin: 0 4px;
}

.no-selection {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px dashed #dcdfe6;
  border-radius: 4px;
}

.category-summary {
  margin-top: 16px;
  padding: 16px;
  background-color: #f8f9fa;
  border-radius: 4px;
  border: 1px solid #ebeef5;
}

.summary-header {
  margin-bottom: 12px;
}

.summary-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.product-preview {
  margin-top: 20px;
}

.preview-tabs {
  margin-bottom: 20px;
}

.category-product-group {
  margin-bottom: 24px;
}

.group-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
}

.category-title {
  display: flex;
  align-items: center;
}

.category-name {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  margin-right: 8px;
}

.product-count {
  font-size: 14px;
  color: #909399;
}

.category-stats {
  display: flex;
  gap: 8px;
}

.product-table {
  margin-bottom: 8px;
}

.no-products {
  padding: 40px 0;
  text-align: center;
}

.all-products-table {
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

.product-category {
  font-size: 12px;
  color: #909399;
}

.product-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.preview-summary {
  margin-top: 20px;
  padding: 16px;
  background-color: #f8f9fa;
  border-radius: 4px;
  border: 1px solid #ebeef5;
}

/* 响应式设计 */
@media (max-width: 992px) {
  .category-selector {
    flex-direction: column;
  }
  
  .category-tree-section,
  .category-selection-section {
    width: 100%;
  }
  
  .tree-container {
    height: 300px;
  }
}

@media (max-width: 768px) {
  .stock-take-category-container {
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
    align-items: flex-start;
    gap: 8px;
  }
  
  .section-actions {
    width: 100%;
    justify-content: flex-end;
  }
  
  .group-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .category-stats {
    width: 100%;
    justify-content: flex-start;
  }
}
</style>