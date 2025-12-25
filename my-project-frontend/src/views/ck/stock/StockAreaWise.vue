<!-- 区域盘点 -->
 <template>
  <div class="stock-take-area-container">
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

    <el-card class="manage-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">区域盘点</span>
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

      <!-- 区域选择 -->
      <el-card class="form-section" shadow="never">
        <template #header>
          <div class="section-header">
            <span class="section-title">区域选择</span>
            <div class="section-actions">
              <el-button 
                type="primary" 
                size="small"
                @click="handleSelectAllAreas"
                v-if="!isViewMode && areaList.length > 0"
              >
                全选区域
              </el-button>
              <el-button 
                type="info" 
                size="small"
                @click="handleClearAreas"
                v-if="!isViewMode"
              >
                清空选择
              </el-button>
            </div>
          </div>
        </template>
        
        <div class="warehouse-info" v-if="currentWarehouse">
          <el-descriptions :column="4" border>
            <el-descriptions-item label="仓库名称">{{ currentWarehouse.name }}</el-descriptions-item>
            <el-descriptions-item label="仓库编码">{{ currentWarehouse.code }}</el-descriptions-item>
            <el-descriptions-item label="仓库类型">{{ getWarehouseTypeText(currentWarehouse.type) }}</el-descriptions-item>
            <el-descriptions-item label="负责人">{{ currentWarehouse.managerName || '未设置' }}</el-descriptions-item>
            <el-descriptions-item label="仓库面积">{{ currentWarehouse.area || '未设置' }} ㎡</el-descriptions-item>
            <el-descriptions-item label="总货架数">{{ totalShelves }} 个</el-descriptions-item>
            <el-descriptions-item label="总区域数">{{ areaList.length }} 个</el-descriptions-item>
            <el-descriptions-item label="已选区域">{{ selectedAreas.length }} 个</el-descriptions-item>
          </el-descriptions>
        </div>
        
        <div class="area-selector" v-if="areaList.length > 0">
          <div class="area-filter">
            <el-input
              v-model="areaFilter.keyword"
              placeholder="搜索区域名称"
              clearable
              style="width: 300px"
              @keyup.enter="filterAreas"
              @clear="filterAreas"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </div>
          
          <div class="area-grid">
            <div 
              v-for="area in filteredAreas" 
              :key="area.areaCode"
              class="area-card"
              :class="{ 'selected': isAreaSelected(area.areaCode) }"
              @click="toggleAreaSelection(area)"
            >
              <div class="area-card-header">
                <div class="area-name">{{ area.areaName }}</div>
                <div class="area-code">{{ area.areaCode }}</div>
              </div>
              <div class="area-card-content">
                <div class="area-stats">
                  <div class="stat-item">
                    <div class="stat-label">货架数</div>
                    <div class="stat-value">{{ area.shelfCount || 0 }}</div>
                  </div>
                  <div class="stat-item">
                    <div class="stat-label">产品数</div>
                    <div class="stat-value">{{ area.productCount || 0 }}</div>
                  </div>
                  <div class="stat-item">
                    <div class="stat-label">库存数量</div>
                    <div class="stat-value">{{ area.stockQuantity || 0 }}</div>
                  </div>
                </div>
                <div class="area-shelves">
                  <div class="shelves-label">主要货架：</div>
                  <div class="shelves-list">
                    <el-tag 
                      v-for="shelf in area.mainShelves" 
                      :key="shelf.shelfCode"
                      size="small"
                      type="info"
                      class="shelf-tag"
                    >
                      {{ shelf.shelfName }}
                    </el-tag>
                  </div>
                </div>
              </div>
              <div class="area-card-footer">
                <el-checkbox 
                  v-model="area.selected" 
                  :label="area.areaName"
                  @click.stop
                  @change="onAreaCheckboxChange(area)"
                >
                  <span class="checkbox-label">选择此区域</span>
                </el-checkbox>
              </div>
            </div>
          </div>
        </div>
        
        <div class="no-area" v-else>
          <el-empty description="请先选择仓库，或该仓库暂无区域数据">
            <template v-if="formData.warehouseId && !isViewMode">
              <el-button type="primary" @click="initWarehouseAreas">初始化区域数据</el-button>
            </template>
          </el-empty>
        </div>
        
        <div class="selected-areas-summary" v-if="selectedAreas.length > 0">
          <div class="summary-header">
            <span class="summary-title">已选区域汇总</span>
            <el-button 
              type="text" 
              size="small"
              @click="showSelectedAreasDetail"
            >
              查看详情
            </el-button>
          </div>
          <div class="summary-content">
            <el-row :gutter="20">
              <el-col :span="6">
                <div class="summary-item">
                  <div class="item-label">区域数量</div>
                  <div class="item-value">{{ selectedAreas.length }} 个</div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="summary-item">
                  <div class="item-label">总货架数</div>
                  <div class="item-value">{{ summary.totalShelves }} 个</div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="summary-item">
                  <div class="item-label">总产品数</div>
                  <div class="item-value">{{ summary.totalProducts }} 个</div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="summary-item">
                  <div class="item-label">预估库存价值</div>
                  <div class="item-value">¥{{ summary.totalValue.toFixed(2) }}</div>
                </div>
              </el-col>
            </el-row>
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
              <el-form-item label="区域盘点顺序">
                <el-select 
                  v-model="strategyConfig.areaOrder" 
                  placeholder="请选择区域盘点顺序"
                  style="width: 100%"
                >
                  <el-option :label="按区域编号顺序" :value="1" />
                  <el-option :label="按货架数量-多到少" :value="2" />
                  <el-option :label="按库存价值-高到低" :value="3" />
                  <el-option :label="随机顺序" :value="4" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="区域并行盘点">
                <el-switch
                  v-model="strategyConfig.parallelCount"
                  active-text="允许"
                  inactive-text="不允许"
                />
                <div class="form-tip">开启后，多个区域可同时进行盘点</div>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="最大并行区域数" v-if="strategyConfig.parallelCount">
                <el-input-number
                  v-model="strategyConfig.maxParallelAreas"
                  :min="1"
                  :max="10"
                  :step="1"
                  style="width: 100%"
                  placeholder="请输入最大并行区域数"
                />
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="24">
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
          </el-row>
          
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="区域负责人分配">
                <el-select
                  v-model="strategyConfig.areaPersonConfig"
                  placeholder="请选择人员分配方式"
                  style="width: 100%"
                >
                  <el-option :label="单人负责单个区域" :value="1" />
                  <el-option :label="团队负责多个区域" :value="2" />
                  <el-option :label="按区域轮转盘点" :value="3" />
                </el-select>
              </el-form-item>
            </el-col>
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
              <el-form-item label="区域报告生成">
                <el-switch
                  v-model="strategyConfig.generateAreaReport"
                  active-text="生成"
                  inactive-text="不生成"
                />
                <div class="form-tip">开启后，为每个区域生成单独的盘点报告</div>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </el-card>

      <!-- 任务分配预览 -->
      <el-card class="form-section" shadow="never" v-if="selectedAreas.length > 0 && strategyConfig.areaPersonConfig">
        <template #header>
          <div class="section-header">
            <span class="section-title">任务分配预览</span>
            <div class="section-actions">
              <el-button 
                type="primary" 
                size="small"
                @click="refreshTaskPreview"
                :loading="previewLoading"
                v-if="!isViewMode"
              >
                <el-icon><Refresh /></el-icon>
                刷新预览
              </el-button>
            </div>
          </div>
        </template>
        
        <div class="task-preview">
          <div class="preview-tabs">
            <el-tabs v-model="previewTab">
              <el-tab-pane label="按区域分配" name="byArea">
                <el-table :data="areaTasks" border style="width: 100%">
                  <el-table-column type="index" label="序号" width="60" align="center" />
                  <el-table-column label="区域信息" min-width="200">
                    <template #default="{ row }">
                      <div class="task-area-info">
                        <div class="area-name">{{ row.areaName }}</div>
                        <div class="area-code">编码: {{ row.areaCode }}</div>
                      </div>
                    </template>
                  </el-table-column>
                  <el-table-column label="货架数" width="100" align="center">
                    <template #default="{ row }">
                      {{ row.shelfCount }}
                    </template>
                  </el-table-column>
                  <el-table-column label="产品数" width="100" align="center">
                    <template #default="{ row }">
                      {{ row.productCount }}
                    </template>
                  </el-table-column>
                  <el-table-column label="预估耗时" width="120" align="center">
                    <template #default="{ row }">
                      {{ row.estimatedTime }} 分钟
                    </template>
                  </el-table-column>
                  <el-table-column label="分配人员" width="150">
                    <template #default="{ row }">
                      <el-select
                        v-model="row.assignedPerson"
                        placeholder="选择人员"
                        size="small"
                        style="width: 100%"
                        :disabled="isViewMode"
                      >
                        <el-option
                          v-for="person in availablePersons"
                          :key="person.id"
                          :label="person.name"
                          :value="person.id"
                        />
                      </el-select>
                    </template>
                  </el-table-column>
                  <el-table-column label="计划时间" width="200">
                    <template #default="{ row }">
                      <el-time-picker
                        v-model="row.plannedStartTime"
                        placeholder="开始时间"
                        style="width: 100%"
                        :disabled="isViewMode"
                      />
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="120" align="center" v-if="!isViewMode">
                    <template #default="{ row }">
                      <el-button
                        type="primary"
                        link
                        size="small"
                        @click="viewAreaTaskDetail(row)"
                      >
                        详情
                      </el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </el-tab-pane>
              <el-tab-pane label="按人员分配" name="byPerson">
                <el-table :data="personTasks" border style="width: 100%">
                  <el-table-column type="index" label="序号" width="60" align="center" />
                  <el-table-column label="人员信息" min-width="200">
                    <template #default="{ row }">
                      <div class="task-person-info">
                        <div class="person-name">{{ row.personName }}</div>
                        <div class="person-role">角色: {{ row.roleName }}</div>
                      </div>
                    </template>
                  </el-table-column>
                  <el-table-column label="负责区域数" width="100" align="center">
                    <template #default="{ row }">
                      {{ row.areaCount }}
                    </template>
                  </el-table-column>
                  <el-table-column label="总货架数" width="100" align="center">
                    <template #default="{ row }">
                      {{ row.totalShelves }}
                    </template>
                  </el-table-column>
                  <el-table-column label="总产品数" width="100" align="center">
                    <template #default="{ row }">
                      {{ row.totalProducts }}
                    </template>
                  </el-table-column>
                  <el-table-column label="预估总耗时" width="120" align="center">
                    <template #default="{ row }">
                      {{ row.totalEstimatedTime }} 分钟
                    </template>
                  </el-table-column>
                  <el-table-column label="工作负荷" width="120" align="center">
                    <template #default="{ row }">
                      <el-progress 
                        :percentage="row.workloadPercentage" 
                        :stroke-width="6"
                        :show-text="false"
                      />
                      <span class="workload-text">{{ row.workloadPercentage }}%</span>
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="120" align="center" v-if="!isViewMode">
                    <template #default="{ row }">
                      <el-button
                        type="primary"
                        link
                        size="small"
                        @click="viewPersonTaskDetail(row)"
                      >
                        详情
                      </el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </el-tab-pane>
            </el-tabs>
          </div>
          
          <div class="preview-summary">
            <el-descriptions :column="4" border>
              <el-descriptions-item label="总区域数">{{ selectedAreas.length }} 个</el-descriptions-item>
              <el-descriptions-item label="总货架数">{{ summary.totalShelves }} 个</el-descriptions-item>
              <el-descriptions-item label="总产品数">{{ summary.totalProducts }} 个</el-descriptions-item>
              <el-descriptions-item label="预估总耗时">{{ summary.totalEstimatedTime }} 分钟</el-descriptions-item>
              <el-descriptions-item label="所需人员">{{ summary.requiredPersons }} 人</el-descriptions-item>
              <el-descriptions-item label="平均工作量">{{ summary.avgWorkload }}%</el-descriptions-item>
              <el-descriptions-item label="最忙人员" :span="2">{{ summary.busiestPerson || '未分配' }}</el-descriptions-item>
            </el-descriptions>
          </div>
        </div>
      </el-card>
    </el-card>
    
    <!-- 已选区域详情对话框 -->
    <el-dialog
      v-model="areasDetailDialogVisible"
      title="已选区域详情"
      width="800px"
      class="areas-detail-dialog"
    >
      <div class="areas-detail-content">
        <el-table :data="selectedAreas" border style="width: 100%">
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column label="区域编码" width="120" prop="areaCode" />
          <el-table-column label="区域名称" width="150" prop="areaName" />
          <el-table-column label="货架数" width="80" align="center">
            <template #default="{ row }">
              {{ row.shelfCount || 0 }}
            </template>
          </el-table-column>
          <el-table-column label="产品数" width="80" align="center">
            <template #default="{ row }">
              {{ row.productCount || 0 }}
            </template>
          </el-table-column>
          <el-table-column label="库存数量" width="100" align="center">
            <template #default="{ row }">
              {{ row.stockQuantity || 0 }}
            </template>
          </el-table-column>
          <el-table-column label="库存价值" width="120" align="right">
            <template #default="{ row }">
              ¥{{ (row.stockValue || 0).toFixed(2) }}
            </template>
          </el-table-column>
          <el-table-column label="主要货架" min-width="200">
            <template #default="{ row }">
              <div class="shelf-list">
                <el-tag 
                  v-for="shelf in row.mainShelves" 
                  :key="shelf.shelfCode"
                  size="small"
                  type="info"
                  class="shelf-tag"
                >
                  {{ shelf.shelfName }}
                </el-tag>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="areasDetailDialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Document, CircleCheck, Back, Search, Refresh } from '@element-plus/icons-vue';
import { post, get } from '@/net';

const route = useRoute();
const router = useRouter();
const baseFormRef = ref(null);

const saving = ref(false);
const submitting = ref(false);
const previewLoading = ref(false);
const areasDetailDialogVisible = ref(false);

// 判断是否为查看模式
const isViewMode = computed(() => route.name === 'CkStockTakeAreaView' || route.params.view === 'true');

// 表单数据
const formData = reactive({
  id: null,
  orderNo: '',
  takeName: '',
  takeType: 1, // 1-动态盘点, 2-静态盘点
  takeStrategy: 2, // 2-区域盘点
  warehouseId: null,
  toleranceRate: 0.5, // 默认0.5%
  planTimeRange: [],
  remark: '',
  
  // 额外字段
  planStartTime: '',
  planEndTime: '',
  status: 0,
  approvalStatus: 0,
  
  // 区域选择
  areaCodes: [],
  includeAllAreas: false
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
  areaCodes: [
    { required: true, message: '请至少选择一个盘点区域', trigger: 'change' }
  ]
};

// 盘点策略配置
const strategyConfig = reactive({
  countMode: 1, // 1-盲盘, 2-明盘
  areaOrder: 1, // 1-按区域编号, 2-按货架数量, 3-按库存价值, 4-随机顺序
  parallelCount: false,
  maxParallelAreas: 3,
  needSecondCount: true,
  secondCountThreshold: 5, // 默认5%
  areaPersonConfig: 1, // 1-单人负责单个区域, 2-团队负责多个区域, 3-按区域轮转盘点
  devices: [1, 4], // 默认PDA和纸质盘点单
  autoGenerateAdjust: true,
  generateAreaReport: true
});

// 区域筛选
const areaFilter = reactive({
  keyword: ''
});

// 任务预览
const previewTab = ref('byArea');
const areaTasks = ref([]);
const personTasks = ref([]);
const availablePersons = ref([]);

// 数据列表
const warehouseList = ref([]);
const areaList = ref([]);
const filteredAreas = ref([]);
const selectedAreas = ref([]);

// 计算属性
const currentWarehouse = computed(() => {
  return warehouseList.value.find(w => w.id === formData.warehouseId) || null;
});

const totalShelves = computed(() => {
  return areaList.value.reduce((sum, area) => sum + (area.shelfCount || 0), 0);
});

const summary = computed(() => {
  const result = {
    totalShelves: 0,
    totalProducts: 0,
    totalValue: 0,
    totalEstimatedTime: 0,
    requiredPersons: 0,
    avgWorkload: 0,
    busiestPerson: ''
  };
  
  selectedAreas.value.forEach(area => {
    result.totalShelves += area.shelfCount || 0;
    result.totalProducts += area.productCount || 0;
    result.totalValue += area.stockValue || 0;
  });
  
  // 估算耗时：每个货架约5分钟，每个产品约1分钟
  result.totalEstimatedTime = Math.round(
    (result.totalShelves * 5 + result.totalProducts * 1) / selectedAreas.value.length
  );
  
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


const handleGoBack = () => {
  const hasUnsavedChanges = false;
  
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


const loadWarehouseAreas = async () => {
  if (!formData.warehouseId) {
    areaList.value = [];
    filteredAreas.value = [];
    return;
  }
  
  try {
    const res = await get(`/api/auth/warehouse/areas?warehouseId=${formData.warehouseId}`);
    if (res && res.length > 0) {
      areaList.value = res.map(area => ({
        ...area,
        selected: false,
        areaCode: area.areaCode || area.code,
        areaName: area.areaName || area.name,
        mainShelves: area.mainShelves || [],
        stockValue: area.stockValue || 0
      }));
      
      filteredAreas.value = [...areaList.value];
    } else {
      areaList.value = [];
      filteredAreas.value = [];
    }
  } catch (error) {
    console.error('加载仓库区域失败:', error);
    areaList.value = [];
    filteredAreas.value = [];
  }
};

const initWarehouseAreas = async () => {
  try {
    const res = await post('/api/auth/warehouse/init-areas', {
      warehouseId: formData.warehouseId
    });
    
    if (res) {
      ElMessage.success('区域数据初始化成功');
      await loadWarehouseAreas();
    }
  } catch (error) {
    console.error('初始化区域数据失败:', error);
    ElMessage.error('初始化失败');
  }
};

const loadAvailablePersons = async () => {
  try {
    const res = await get('/api/auth/user/warehouse-persons', {
      warehouseId: formData.warehouseId
    });
    availablePersons.value = res || [];
  } catch (error) {
    console.error('加载可用人员失败:', error);
    availablePersons.value = [];
  }
};

const loadStockTakeDetail = async (id) => {
  try {
    const res = await get(`/api/auth/stock-take/area-detail?id=${id}`);
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
        areaCodes: res.areaCodes || []
      });
      
      // 填充策略配置
      if (res.strategyConfig) {
        Object.assign(strategyConfig, JSON.parse(res.strategyConfig));
      }
      
      // 加载仓库区域
      await loadWarehouseAreas();
      
      // 填充区域选择
      if (res.areaCodes && res.areaCodes.length > 0) {
        setTimeout(() => {
          const selectedCodes = res.areaCodes;
          areaList.value.forEach(area => {
            area.selected = selectedCodes.includes(area.areaCode);
          });
          selectedAreas.value = areaList.value.filter(area => area.selected);
        }, 500);
      }
      
      // 加载可用人员
      await loadAvailablePersons();
      
      // 刷新任务预览
      await refreshTaskPreview();
    }
  } catch (error) {
    console.error('加载盘点单详情失败:', error);
    ElMessage.error('加载数据失败');
  }
};

const handleWarehouseChange = () => {
  // 清空区域选择
  selectedAreas.value = [];
  // 加载仓库区域
  loadWarehouseAreas();
  // 加载可用人员
  loadAvailablePersons();
};

const filterAreas = () => {
  if (!areaList.value.length) return;
  
  let result = areaList.value;
  
  // 按关键词筛选
  if (areaFilter.keyword) {
    const keyword = areaFilter.keyword.toLowerCase();
    result = result.filter(area => 
      area.areaName.toLowerCase().includes(keyword) || 
      area.areaCode.toLowerCase().includes(keyword)
    );
  }
  
  filteredAreas.value = result;
};

const isAreaSelected = (areaCode) => {
  return selectedAreas.value.some(area => area.areaCode === areaCode);
};

const toggleAreaSelection = (area) => {
  if (isViewMode) return;
  
  area.selected = !area.selected;
  
  if (area.selected) {
    if (!selectedAreas.value.some(a => a.areaCode === area.areaCode)) {
      selectedAreas.value.push(area);
    }
  } else {
    const index = selectedAreas.value.findIndex(a => a.areaCode === area.areaCode);
    if (index !== -1) {
      selectedAreas.value.splice(index, 1);
    }
  }
  
  // 更新表单数据
  formData.areaCodes = selectedAreas.value.map(area => area.areaCode);
};

const onAreaCheckboxChange = (area) => {
  toggleAreaSelection(area);
};

const handleSelectAllAreas = () => {
  if (isViewMode) return;
  
  areaList.value.forEach(area => {
    area.selected = true;
    if (!selectedAreas.value.some(a => a.areaCode === area.areaCode)) {
      selectedAreas.value.push(area);
    }
  });
  
  formData.areaCodes = selectedAreas.value.map(area => area.areaCode);
  ElMessage.success('已选择所有区域');
};

const handleClearAreas = () => {
  if (isViewMode) return;
  
  areaList.value.forEach(area => {
    area.selected = false;
  });
  
  selectedAreas.value = [];
  formData.areaCodes = [];
  ElMessage.info('已清空区域选择');
};

const showSelectedAreasDetail = () => {
  if (selectedAreas.value.length === 0) {
    ElMessage.warning('请先选择区域');
    return;
  }
  
  areasDetailDialogVisible.value = true;
};

const refreshTaskPreview = async () => {
  if (selectedAreas.value.length === 0) {
    ElMessage.warning('请先选择区域');
    return;
  }
  
  previewLoading.value = true;
  try {
    // 生成区域任务预览
    areaTasks.value = selectedAreas.value.map((area, index) => ({
      areaCode: area.areaCode,
      areaName: area.areaName,
      shelfCount: area.shelfCount || 0,
      productCount: area.productCount || 0,
      estimatedTime: Math.round((area.shelfCount || 0) * 5 + (area.productCount || 0) * 1),
      assignedPerson: null,
      plannedStartTime: '',
      sequence: index + 1
    }));
    
    // 根据人员分配方式生成人员任务
    if (strategyConfig.areaPersonConfig === 1) {
      // 单人负责单个区域
      personTasks.value = selectedAreas.value.slice(0, availablePersons.value.length).map((area, index) => {
        const person = availablePersons.value[index];
        return {
          personId: person?.id || null,
          personName: person?.name || `人员${index + 1}`,
          roleName: person?.roleName || '盘点员',
          areaCount: 1,
          totalShelves: area.shelfCount || 0,
          totalProducts: area.productCount || 0,
          totalEstimatedTime: Math.round((area.shelfCount || 0) * 5 + (area.productCount || 0) * 1),
          workloadPercentage: 100,
          areas: [area.areaName]
        };
      });
    } else if (strategyConfig.areaPersonConfig === 2) {
      // 团队负责多个区域
      const personsCount = availablePersons.value.length;
      const areasPerPerson = Math.ceil(selectedAreas.value.length / personsCount);
      
      personTasks.value = availablePersons.value.map((person, index) => {
        const startIndex = index * areasPerPerson;
        const endIndex = Math.min(startIndex + areasPerPerson, selectedAreas.value.length);
        const assignedAreas = selectedAreas.value.slice(startIndex, endIndex);
        
        const totalShelves = assignedAreas.reduce((sum, area) => sum + (area.shelfCount || 0), 0);
        const totalProducts = assignedAreas.reduce((sum, area) => sum + (area.productCount || 0), 0);
        
        return {
          personId: person.id,
          personName: person.name,
          roleName: person.roleName || '盘点员',
          areaCount: assignedAreas.length,
          totalShelves,
          totalProducts,
          totalEstimatedTime: Math.round(totalShelves * 5 + totalProducts * 1),
          workloadPercentage: Math.round((assignedAreas.length / areasPerPerson) * 100),
          areas: assignedAreas.map(area => area.areaName)
        };
      });
    }
    
    // 更新汇总信息
    summary.value.requiredPersons = personTasks.value.length;
    summary.value.avgWorkload = Math.round(
      personTasks.value.reduce((sum, task) => sum + task.workloadPercentage, 0) / personTasks.value.length
    );
    
    if (personTasks.value.length > 0) {
      const busiest = personTasks.value.reduce((max, task) => 
        task.workloadPercentage > max.workloadPercentage ? task : max
      );
      summary.value.busiestPerson = busiest.personName;
    }
    
  } catch (error) {
    console.error('刷新任务预览失败:', error);
    ElMessage.error('刷新预览失败');
  } finally {
    previewLoading.value = false;
  }
};

const viewAreaTaskDetail = (task) => {
  console.log('查看区域任务详情:', task);
  // 这里可以打开任务详情弹窗
};

const viewPersonTaskDetail = (task) => {
  console.log('查看人员任务详情:', task);
  // 这里可以打开任务详情弹窗
};

const getWarehouseTypeText = (type) => {
  const typeMap = {
    1: '普通仓库',
    2: '冷库',
    3: '危险品仓库',
    4: '自动化仓库'
  };
  return typeMap[type] || '未知类型';
};

const validateForm = async () => {
  if (!baseFormRef.value) return false;
  
  try {
    await baseFormRef.value.validate();
    
    // 检查是否有选择区域
    if (selectedAreas.value.length === 0) {
      ElMessage.warning('请至少选择一个盘点区域');
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
      areaCodes: selectedAreas.value.map(area => area.areaCode),
      areaNames: selectedAreas.value.map(area => area.areaName),
      strategyConfig: JSON.stringify(strategyConfig),
      takeStrategy: 2, // 区域盘点
      status: 0, // 草稿状态
      approvalStatus: 0 // 未提交审批
    };
    
    // 删除不需要的字段
    delete saveData.planTimeRange;
    
    const res = await post('/api/auth/stock-take/area/save', saveData);
    
    if (res) {
      formData.id = res.id;
      formData.orderNo = res.orderNo || formData.orderNo;
      ElMessage.success('保存成功');
      
      // 如果是新建保存，更新URL中的ID
      if (route.params.id && route.params.id !== res.id) {
        router.replace(`/index/ckStockTakeAreaEdit/${res.id}`);
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
      `确定要提交区域盘点单"${formData.takeName}"审批吗？提交后不可修改。`,
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
    
    const res = await post('/api/auth/stock-take/area/submit', submitData);
    
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
  formData.orderNo = `${prefix}A${timestamp}${random}`;
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
    loadWarehouseAreas();
    loadAvailablePersons();
  }
});

// 监听区域选择变化
watch(() => selectedAreas.value.length, () => {
  if (selectedAreas.value.length > 0) {
    refreshTaskPreview();
  }
});

onMounted(() => {
  initPage();
});
</script>

<style scoped>
.stock-take-area-container {
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

.warehouse-info {
  margin-bottom: 20px;
}

.area-selector {
  margin-top: 20px;
}

.area-filter {
  margin-bottom: 20px;
}

.area-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}

.area-card {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 16px;
  background: white;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  flex-direction: column;
}

.area-card:hover {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.area-card.selected {
  border-color: #409eff;
  background-color: #f0f7ff;
}

.area-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.area-name {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.area-code {
  font-size: 12px;
  color: #909399;
  background: #f5f7fa;
  padding: 2px 8px;
  border-radius: 4px;
}

.area-card-content {
  flex: 1;
  margin-bottom: 12px;
}

.area-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-bottom: 12px;
}

.stat-item {
  text-align: center;
}

.stat-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 18px;
  font-weight: bold;
  color: #409eff;
}

.area-shelves {
  margin-top: 12px;
}

.shelves-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.shelves-list {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.shelf-tag {
  margin-bottom: 4px;
}

.area-card-footer {
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.checkbox-label {
  font-size: 14px;
  color: #606266;
}

.no-area {
  padding: 40px 0;
  text-align: center;
}

.selected-areas-summary {
  margin-top: 20px;
  padding: 16px;
  background-color: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #ebeef5;
}

.summary-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.summary-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.summary-content {
  margin-top: 12px;
}

.summary-item {
  text-align: center;
  padding: 8px;
  background: white;
  border-radius: 4px;
  border: 1px solid #ebeef5;
}

.item-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.item-value {
  font-size: 18px;
  font-weight: bold;
  color: #409eff;
}

.task-preview {
  margin-top: 20px;
}

.preview-tabs {
  margin-bottom: 20px;
}

.task-area-info {
  line-height: 1.4;
}

.area-name {
  font-weight: 500;
  color: #303133;
  margin-bottom: 2px;
}

.area-code {
  font-size: 12px;
  color: #909399;
}

.task-person-info {
  line-height: 1.4;
}

.person-name {
  font-weight: 500;
  color: #303133;
  margin-bottom: 2px;
}

.person-role {
  font-size: 12px;
  color: #909399;
}

.workload-text {
  font-size: 12px;
  color: #606266;
  margin-left: 8px;
}

.preview-summary {
  margin-top: 20px;
}

.shelf-list {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.areas-detail-content {
  max-height: 500px;
  overflow-y: auto;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .stock-take-area-container {
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
  
  .area-grid {
    grid-template-columns: 1fr;
  }
  
  .area-card {
    margin-bottom: 12px;
  }
  
  .summary-content .el-col {
    margin-bottom: 12px;
  }
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

</style>