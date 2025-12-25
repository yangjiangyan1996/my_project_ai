<!-- 循环盘点 -->
 <template>
  <div class="stock-take-cycle-container">
    <el-card class="manage-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">循环盘点</span>
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
              <el-form-item label="计划开始时间" prop="planStartTime">
                <el-date-picker
                  v-model="formData.planStartTime"
                  type="datetime"
                  placeholder="选择开始时间"
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

      <!-- 循环配置 -->
      <el-card class="form-section" shadow="never">
        <template #header>
          <div class="section-header">
            <span class="section-title">循环配置</span>
          </div>
        </template>
        
        <el-form 
          :model="cycleConfig" 
          label-width="120px"
          :disabled="isViewMode"
        >
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="循环周期类型" prop="cycleType">
                <el-select
                  v-model="cycleConfig.cycleType"
                  placeholder="请选择循环周期类型"
                  style="width: 100%"
                  @change="handleCycleTypeChange"
                >
                  <el-option label="按天循环" :value="1" />
                  <el-option label="按周循环" :value="2" />
                  <el-option label="按月循环" :value="3" />
                  <el-option label="按季循环" :value="4" />
                  <el-option label="按年循环" :value="5" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="循环频率" prop="frequency">
                <el-input-number
                  v-model="cycleConfig.frequency"
                  :min="1"
                  :max="maxFrequency"
                  :step="1"
                  style="width: 100%"
                  placeholder="请输入循环频率"
                >
                  <template #append>{{ frequencyUnit }}</template>
                </el-input-number>
                <div class="form-tip">每{{ frequencyText }}执行一次</div>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="每次盘点数量" prop="itemsPerCycle">
                <el-input-number
                  v-model="cycleConfig.itemsPerCycle"
                  :min="1"
                  :max="10000"
                  :step="10"
                  style="width: 100%"
                  placeholder="请输入每次盘点数量"
                />
                <div class="form-tip">每次循环盘点的产品数量</div>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="执行时间" prop="executeTime">
                <el-time-picker
                  v-model="cycleConfig.executeTime"
                  placeholder="选择执行时间"
                  value-format="HH:mm:ss"
                  style="width: 100%"
                />
                <div class="form-tip">每天的执行时间</div>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="24" v-if="cycleConfig.cycleType === 2">
            <el-col :span="24">
              <el-form-item label="执行星期" prop="weekDays">
                <el-checkbox-group v-model="cycleConfig.weekDays">
                  <el-checkbox :label="1">周一</el-checkbox>
                  <el-checkbox :label="2">周二</el-checkbox>
                  <el-checkbox :label="3">周三</el-checkbox>
                  <el-checkbox :label="4">周四</el-checkbox>
                  <el-checkbox :label="5">周五</el-checkbox>
                  <el-checkbox :label="6">周六</el-checkbox>
                  <el-checkbox :label="7">周日</el-checkbox>
                </el-checkbox-group>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="24" v-if="cycleConfig.cycleType === 3">
            <el-col :span="12">
              <el-form-item label="执行日期" prop="monthDays">
                <el-select
                  v-model="cycleConfig.monthDays"
                  multiple
                  placeholder="选择执行日期"
                  style="width: 100%"
                >
                  <el-option
                    v-for="day in 31"
                    :key="day"
                    :label="`每月${day}号`"
                    :value="day"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="月末处理" prop="endOfMonth">
                <el-radio-group v-model="cycleConfig.endOfMonth">
                  <el-radio :label="1">包含月末</el-radio>
                  <el-radio :label="2">不包含月末</el-radio>
                  <el-radio :label="3">仅月末</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="循环结束条件" prop="endCondition">
                <el-select
                  v-model="cycleConfig.endCondition"
                  placeholder="请选择结束条件"
                  style="width: 100%"
                >
                  <el-option label="指定结束时间" :value="1" />
                  <el-option label="指定循环次数" :value="2" />
                  <el-option label="无限循环" :value="3" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item 
                label="结束时间" 
                prop="endTime"
                v-if="cycleConfig.endCondition === 1"
              >
                <el-date-picker
                  v-model="cycleConfig.endTime"
                  type="datetime"
                  placeholder="选择结束时间"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  style="width: 100%"
                />
              </el-form-item>
              <el-form-item 
                label="循环次数" 
                prop="maxCycles"
                v-else-if="cycleConfig.endCondition === 2"
              >
                <el-input-number
                  v-model="cycleConfig.maxCycles"
                  :min="1"
                  :max="1000"
                  :step="1"
                  style="width: 100%"
                  placeholder="请输入循环次数"
                />
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="优先级计算方式" prop="priorityMethod">
                <el-select
                  v-model="cycleConfig.priorityMethod"
                  placeholder="请选择优先级计算方式"
                  style="width: 100%"
                >
                  <el-option label="ABC分类法" :value="1" />
                  <el-option label="库存价值法" :value="2" />
                  <el-option label="周转率法" :value="3" />
                  <el-option label="综合评分法" :value="4" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="优先级更新频率" prop="priorityUpdateFreq">
                <el-input-number
                  v-model="cycleConfig.priorityUpdateFreq"
                  :min="1"
                  :max="30"
                  :step="1"
                  style="width: 100%"
                  placeholder="请输入更新频率"
                >
                  <template #append>天</template>
                </el-input-number>
                <div class="form-tip">每多少天更新一次优先级</div>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
        
        <div class="cycle-summary">
          <div class="summary-header">
            <span class="summary-title">循环计划预览</span>
            <el-button 
              type="primary" 
              size="small"
              @click="generateCyclePreview"
              :loading="previewLoading"
              v-if="!isViewMode"
            >
              生成预览
            </el-button>
          </div>
          
          <div class="preview-content" v-if="cyclePreview.length > 0">
            <el-table 
              :data="cyclePreview" 
              border 
              style="width: 100%"
              max-height="300"
            >
              <el-table-column type="index" label="序号" width="60" align="center" />
              <el-table-column label="循环批次" width="120">
                <template #default="{ row }">
                  第{{ row.cycleNumber }}批
                </template>
              </el-table-column>
              <el-table-column label="预计执行时间" width="180">
                <template #default="{ row }">
                  {{ row.expectedTime }}
                </template>
              </el-table-column>
              <el-table-column label="预计盘点产品" width="120" align="center">
                <template #default="{ row }">
                  {{ row.estimatedProducts }} 个
                </template>
              </el-table-column>
              <el-table-column label="累计盘点产品" width="120" align="center">
                <template #default="{ row }">
                  {{ row.cumulativeProducts }} 个
                </template>
              </el-table-column>
              <el-table-column label="累计覆盖率" width="120" align="center">
                <template #default="{ row }">
                  {{ row.coverageRate }}%
                </template>
              </el-table-column>
              <el-table-column label="状态" width="100" align="center">
                <template #default="{ row }">
                  <el-tag :type="getPreviewStatusTag(row.status)" size="small">
                    {{ getPreviewStatusText(row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="备注" min-width="200">
                <template #default="{ row }">
                  {{ row.remark || '--' }}
                </template>
              </el-table-column>
            </el-table>
            
            <div class="preview-stats">
              <el-descriptions :column="4" border>
                <el-descriptions-item label="总循环批次">{{ previewStats.totalCycles }} 批</el-descriptions-item>
                <el-descriptions-item label="总盘点产品">{{ previewStats.totalProducts }} 个</el-descriptions-item>
                <el-descriptions-item label="预计完成时间">{{ previewStats.expectedEndTime || '--' }}</el-descriptions-item>
                <el-descriptions-item label="平均覆盖率">{{ previewStats.avgCoverageRate }}%</el-descriptions-item>
              </el-descriptions>
            </div>
          </div>
          <div class="no-preview" v-else>
            <el-empty description="请先配置循环参数并生成预览" />
          </div>
        </div>
      </el-card>

      <!-- 产品选择策略 -->
      <el-card class="form-section" shadow="never">
        <template #header>
          <div class="section-header">
            <span class="section-title">产品选择策略</span>
          </div>
        </template>
        
        <el-form 
          :model="selectionStrategy" 
          label-width="120px"
          :disabled="isViewMode"
        >
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="选择范围" prop="selectionScope">
                <el-radio-group v-model="selectionStrategy.selectionScope">
                  <el-radio :label="1">全仓库产品</el-radio>
                  <el-radio :label="2">按分类选择</el-radio>
                  <el-radio :label="3">按区域选择</el-radio>
                  <el-radio :label="4">按供应商选择</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="选择算法" prop="selectionAlgorithm">
                <el-select
                  v-model="selectionStrategy.selectionAlgorithm"
                  placeholder="请选择选择算法"
                  style="width: 100%"
                >
                  <el-option label="随机选择" :value="1" />
                  <el-option label="按优先级顺序" :value="2" />
                  <el-option label="按上次盘点时间" :value="3" />
                  <el-option label="按库存变动频率" :value="4" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="ABC分类设置">
                <el-input-number
                  v-model="selectionStrategy.abcClassA"
                  :min="0"
                  :max="100"
                  :step="1"
                  style="width: 30%"
                  placeholder="A类"
                >
                  <template #append>%</template>
                </el-input-number>
                <span class="input-separator">-</span>
                <el-input-number
                  v-model="selectionStrategy.abcClassB"
                  :min="0"
                  :max="100"
                  :step="1"
                  style="width: 30%"
                  placeholder="B类"
                >
                  <template #append>%</template>
                </el-input-number>
                <span class="input-separator">-</span>
                <el-input-number
                  v-model="selectionStrategy.abcClassC"
                  :min="0"
                  :max="100"
                  :step="1"
                  style="width: 30%"
                  placeholder="C类"
                >
                  <template #append>%</template>
                </el-input-number>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="重复盘点间隔" prop="repeatInterval">
                <el-input-number
                  v-model="selectionStrategy.repeatInterval"
                  :min="1"
                  :max="365"
                  :step="1"
                  style="width: 100%"
                  placeholder="请输入重复盘点间隔"
                >
                  <template #append>天</template>
                </el-input-number>
                <div class="form-tip">同一产品两次盘点之间的最小间隔天数</div>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="排除零库存产品" prop="excludeZeroStock">
                <el-switch
                  v-model="selectionStrategy.excludeZeroStock"
                  active-text="排除"
                  inactive-text="不排除"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="排除近效期产品" prop="excludeNearExpiry">
                <el-switch
                  v-model="selectionStrategy.excludeNearExpiry"
                  active-text="排除"
                  inactive-text="不排除"
                />
                <div class="form-tip" v-if="selectionStrategy.excludeNearExpiry">
                  排除距离有效期小于{{ selectionStrategy.expiryThreshold || 30 }}天的产品
                </div>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="24" v-if="selectionStrategy.excludeNearExpiry">
            <el-col :span="12">
              <el-form-item label="效期阈值" prop="expiryThreshold">
                <el-input-number
                  v-model="selectionStrategy.expiryThreshold"
                  :min="1"
                  :max="365"
                  :step="1"
                  style="width: 100%"
                  placeholder="请输入效期阈值"
                >
                  <template #append>天</template>
                </el-input-number>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="重点关注产品" prop="keyProducts">
                <el-select
                  v-model="selectionStrategy.keyProducts"
                  multiple
                  placeholder="选择重点关注产品"
                  style="width: 100%"
                  :loading="keyProductsLoading"
                >
                  <el-option
                    v-for="product in keyProductsList"
                    :key="product.id"
                    :label="`${product.sku} - ${product.name}`"
                    :value="product.id"
                  />
                </el-select>
                <div class="form-tip">选中的产品将在每次循环中都被盘点</div>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="重点关注比例" prop="keyProductRatio">
                <el-input-number
                  v-model="selectionStrategy.keyProductRatio"
                  :min="0"
                  :max="100"
                  :step="1"
                  style="width: 100%"
                  placeholder="请输入重点关注比例"
                >
                  <template #append>%</template>
                </el-input-number>
                <div class="form-tip">A类产品在每次循环中的占比</div>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </el-card>

      <!-- 盘点策略设置 -->
      <el-card class="form-section" shadow="never">
        <template #header>
          <div class="section-header">
            <span class="section-title">盘点策略设置</span>
          </div>
        </template>
        
        <el-form 
          :model="takeStrategy" 
          label-width="120px"
          :disabled="isViewMode"
        >
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="盘点模式" prop="countMode">
                <el-radio-group v-model="takeStrategy.countMode">
                  <el-radio :label="1">盲盘（不显示系统库存）</el-radio>
                  <el-radio :label="2">明盘（显示系统库存）</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="是否需要复盘" prop="needSecondCount">
                <el-switch
                  v-model="takeStrategy.needSecondCount"
                  active-text="需要"
                  inactive-text="不需要"
                />
                <div class="form-tip">开启后，差异超过允许范围将自动触发复盘</div>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="复盘阈值" v-if="takeStrategy.needSecondCount">
                <el-input-number
                  v-model="takeStrategy.secondCountThreshold"
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
              <el-form-item label="自动生成调整单" prop="autoGenerateAdjust">
                <el-switch
                  v-model="takeStrategy.autoGenerateAdjust"
                  active-text="自动生成"
                  inactive-text="手动生成"
                />
                <div class="form-tip">开启后，盘点完成后自动生成库存调整单</div>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="异常处理策略" prop="exceptionStrategy">
                <el-select
                  v-model="takeStrategy.exceptionStrategy"
                  placeholder="请选择异常处理策略"
                  style="width: 100%"
                >
                  <el-option label="自动重试" :value="1" />
                  <el-option label="跳过继续" :value="2" />
                  <el-option label="暂停盘点" :value="3" />
                  <el-option label="通知处理" :value="4" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="盘点报告生成" prop="generateReport">
                <el-switch
                  v-model="takeStrategy.generateReport"
                  active-text="生成"
                  inactive-text="不生成"
                />
                <div class="form-tip">开启后，每次循环生成盘点报告</div>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="盘点提醒设置" prop="reminderSettings">
                <el-checkbox-group v-model="takeStrategy.reminderSettings">
                  <el-checkbox :label="1">开始前提醒</el-checkbox>
                  <el-checkbox :label="2">逾期未开始提醒</el-checkbox>
                  <el-checkbox :label="3">盘点异常提醒</el-checkbox>
                  <el-checkbox :label="4">盘点完成提醒</el-checkbox>
                </el-checkbox-group>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="提醒提前量" v-if="takeStrategy.reminderSettings.includes(1)">
                <el-input-number
                  v-model="takeStrategy.reminderLeadTime"
                  :min="1"
                  :max="24"
                  :step="1"
                  style="width: 100%"
                  placeholder="请输入提醒提前量"
                >
                  <template #append>小时</template>
                </el-input-number>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </el-card>

      <!-- 历史执行记录 -->
      <el-card class="form-section" shadow="never" v-if="formData.id && executionHistory.length > 0">
        <template #header>
          <div class="section-header">
            <span class="section-title">历史执行记录</span>
            <div class="section-actions">
              <el-button 
                type="primary" 
                size="small"
                @click="loadExecutionHistory"
              >
                <el-icon><Refresh /></el-icon>
                刷新记录
              </el-button>
            </div>
          </div>
        </template>
        
        <div class="execution-history">
          <el-table :data="executionHistory" border style="width: 100%">
            <el-table-column type="index" label="序号" width="60" align="center" />
            <el-table-column label="循环批次" width="120" align="center">
              <template #default="{ row }">
                第{{ row.cycleNumber }}批
              </template>
            </el-table-column>
            <el-table-column label="计划执行时间" width="180">
              <template #default="{ row }">
                {{ row.planTime }}
              </template>
            </el-table-column>
            <el-table-column label="实际执行时间" width="180">
              <template #default="{ row }">
                {{ row.actualTime || '--' }}
              </template>
            </el-table-column>
            <el-table-column label="盘点产品数" width="120" align="center">
              <template #default="{ row }">
                {{ row.productCount || 0 }} 个
              </template>
            </el-table-column>
            <el-table-column label="准确率" width="100" align="center">
              <template #default="{ row }">
                <span :class="getAccuracyClass(row.accuracyRate)">
                  {{ row.accuracyRate || 0 }}%
                </span>
              </template>
            </el-table-column>
            <el-table-column label="差异率" width="100" align="center">
              <template #default="{ row }">
                <span :class="getDifferenceClass(row.differenceRate)">
                  {{ row.differenceRate || 0 }}%
                </span>
              </template>
            </el-table-column>
            <el-table-column label="执行状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="getExecutionStatusTag(row.status)" size="small">
                  {{ getExecutionStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" align="center">
              <template #default="{ row }">
                <el-button
                  type="primary"
                  link
                  size="small"
                  @click="viewExecutionDetail(row)"
                  v-if="row.status === 2"
                >
                  查看详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <div class="history-summary">
            <el-descriptions :column="4" border>
              <el-descriptions-item label="总执行次数">{{ historySummary.totalExecutions }} 次</el-descriptions-item>
              <el-descriptions-item label="已完成次数">{{ historySummary.completedExecutions }} 次</el-descriptions-item>
              <el-descriptions-item label="平均准确率">{{ historySummary.avgAccuracyRate }}%</el-descriptions-item>
              <el-descriptions-item label="平均差异率">{{ historySummary.avgDifferenceRate }}%</el-descriptions-item>
              <el-descriptions-item label="总盘点产品数">{{ historySummary.totalProducts }} 个</el-descriptions-item>
              <el-descriptions-item label="覆盖率">{{ historySummary.coverageRate }}%</el-descriptions-item>
              <el-descriptions-item label="最近执行时间">{{ historySummary.lastExecutionTime || '--' }}</el-descriptions-item>
              <el-descriptions-item label="下次计划时间">{{ historySummary.nextPlanTime || '--' }}</el-descriptions-item>
            </el-descriptions>
          </div>
        </div>
      </el-card>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Document, CircleCheck, Back, Refresh } from '@element-plus/icons-vue';
import { post, get } from '@/net';

const route = useRoute();
const router = useRouter();
const baseFormRef = ref(null);

const saving = ref(false);
const submitting = ref(false);
const previewLoading = ref(false);
const keyProductsLoading = ref(false);

// 判断是否为查看模式
const isViewMode = computed(() => route.name === 'CkStockTakeCycleView' || route.params.view === 'true');

// 表单数据
const formData = reactive({
  id: null,
  orderNo: '',
  takeName: '',
  takeType: 1, // 1-动态盘点, 2-静态盘点
  takeStrategy: 4, // 4-循环盘点
  warehouseId: null,
  toleranceRate: 0.5, // 默认0.5%
  planStartTime: '',
  remark: '',
  
  // 额外字段
  status: 0,
  approvalStatus: 0,
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
  planStartTime: [
    { required: true, message: '请选择计划开始时间', trigger: 'change' }
  ]
};

// 循环配置
const cycleConfig = reactive({
  cycleType: 1, // 1-天, 2-周, 3-月, 4-季, 5-年
  frequency: 1,
  itemsPerCycle: 100,
  executeTime: '09:00:00',
  weekDays: [1, 2, 3, 4, 5], // 默认周一到周五
  monthDays: [],
  endOfMonth: 1,
  endCondition: 1, // 1-结束时间, 2-循环次数, 3-无限循环
  endTime: '',
  maxCycles: 12,
  priorityMethod: 1, // 1-ABC分类法, 2-库存价值法, 3-周转率法, 4-综合评分法
  priorityUpdateFreq: 7
});

// 产品选择策略
const selectionStrategy = reactive({
  selectionScope: 1, // 1-全仓库, 2-按分类, 3-按区域, 4-按供应商
  selectionAlgorithm: 2, // 1-随机, 2-优先级顺序, 3-上次盘点时间, 4-库存变动频率
  abcClassA: 70,
  abcClassB: 20,
  abcClassC: 10,
  repeatInterval: 30,
  excludeZeroStock: true,
  excludeNearExpiry: false,
  expiryThreshold: 30,
  keyProducts: [],
  keyProductRatio: 20
});

// 盘点策略
const takeStrategy = reactive({
  countMode: 1, // 1-盲盘, 2-明盘
  needSecondCount: true,
  secondCountThreshold: 5,
  autoGenerateAdjust: true,
  exceptionStrategy: 4, // 1-自动重试, 2-跳过继续, 3-暂停盘点, 4-通知处理
  generateReport: true,
  reminderSettings: [1, 2, 4], // 默认开始前、逾期、完成提醒
  reminderLeadTime: 2
});

// 循环预览
const cyclePreview = ref([]);
const previewStats = reactive({
  totalCycles: 0,
  totalProducts: 0,
  expectedEndTime: '',
  avgCoverageRate: 0
});

// 历史执行记录
const executionHistory = ref([]);
const historySummary = reactive({
  totalExecutions: 0,
  completedExecutions: 0,
  avgAccuracyRate: 0,
  avgDifferenceRate: 0,
  totalProducts: 0,
  coverageRate: 0,
  lastExecutionTime: '',
  nextPlanTime: ''
});

// 数据列表
const warehouseList = ref([]);
const keyProductsList = ref([]);

// 计算属性
const maxFrequency = computed(() => {
  const maxMap = {
    1: 30, // 天: 最多30天
    2: 4,  // 周: 最多4周
    3: 12, // 月: 最多12个月
    4: 4,  // 季: 最多4个季度
    5: 1   // 年: 最多1年
  };
  return maxMap[cycleConfig.cycleType] || 30;
});

const frequencyUnit = computed(() => {
  const unitMap = {
    1: '天',
    2: '周',
    3: '月',
    4: '季',
    5: '年'
  };
  return unitMap[cycleConfig.cycleType] || '天';
});

const frequencyText = computed(() => {
  if (cycleConfig.frequency === 1) {
    return frequencyUnit.value;
  }
  return `${cycleConfig.frequency}${frequencyUnit.value}`;
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

const loadKeyProducts = async () => {
  if (!formData.warehouseId) return;
  
  keyProductsLoading.value = true;
  try {
    const res = await get('/api/auth/product/key-products', {
      warehouseId: formData.warehouseId,
      limit: 100
    });
    keyProductsList.value = res || [];
  } catch (error) {
    console.error('加载重点关注产品失败:', error);
    keyProductsList.value = [];
  } finally {
    keyProductsLoading.value = false;
  }
};

const loadExecutionHistory = async () => {
  if (!formData.id) return;
  
  try {
    const res = await get(`/api/auth/stock-take/cycle-execution-history?id=${formData.id}`);
    if (res) {
      executionHistory.value = res.history || [];
      Object.assign(historySummary, res.summary || {});
    }
  } catch (error) {
    console.error('加载执行历史失败:', error);
    executionHistory.value = [];
  }
};

const loadStockTakeDetail = async (id) => {
  try {
    const res = await get(`/api/auth/stock-take/cycle-detail?id=${id}`);
    if (res) {
      // 填充表单数据
      Object.assign(formData, {
        id: res.id,
        orderNo: res.orderNo,
        takeName: res.takeName,
        takeType: res.takeType,
        warehouseId: res.warehouseId,
        toleranceRate: res.toleranceRate,
        planStartTime: res.planStartTime,
        remark: res.remark,
        status: res.status,
        approvalStatus: res.approvalStatus
      });
      
      // 填充配置
      if (res.cycleConfig) {
        Object.assign(cycleConfig, JSON.parse(res.cycleConfig));
      }
      
      if (res.selectionStrategy) {
        Object.assign(selectionStrategy, JSON.parse(res.selectionStrategy));
      }
      
      if (res.takeStrategy) {
        Object.assign(takeStrategy, JSON.parse(res.takeStrategy));
      }
      
      // 加载重点关注产品
      await loadKeyProducts();
      
      // 加载执行历史
      if (formData.id) {
        await loadExecutionHistory();
      }
    }
  } catch (error) {
    console.error('加载盘点单详情失败:', error);
    ElMessage.error('加载数据失败');
  }
};

const handleWarehouseChange = () => {
  // 加载重点关注产品
  loadKeyProducts();
};

const handleCycleTypeChange = () => {
  // 重置频率到最大值
  if (cycleConfig.frequency > maxFrequency.value) {
    cycleConfig.frequency = maxFrequency.value;
  }
};

const generateCyclePreview = async () => {
  if (!formData.warehouseId) {
    ElMessage.warning('请先选择仓库');
    return;
  }
  
  previewLoading.value = true;
  try {
    const params = {
      warehouseId: formData.warehouseId,
      cycleConfig: cycleConfig,
      selectionStrategy: selectionStrategy,
      planStartTime: formData.planStartTime
    };
    
    const res = await post('/api/auth/stock-take/cycle-preview', params);
    
    if (res && res.preview) {
      cyclePreview.value = res.preview;
      Object.assign(previewStats, res.stats || {});
      
      ElMessage.success('循环预览已生成');
    }
  } catch (error) {
    console.error('生成循环预览失败:', error);
    ElMessage.error('生成预览失败');
  } finally {
    previewLoading.value = false;
  }
};

const getPreviewStatusTag = (status) => {
  const tagMap = {
    0: 'info',
    1: 'warning',
    2: 'success',
    3: 'danger'
  };
  return tagMap[status] || 'info';
};

const getPreviewStatusText = (status) => {
  const textMap = {
    0: '未开始',
    1: '待执行',
    2: '已完成',
    3: '已取消'
  };
  return textMap[status] || '未知';
};

const getExecutionStatusTag = (status) => {
  const tagMap = {
    0: 'info',
    1: 'warning',
    2: 'success',
    3: 'danger',
    4: 'info'
  };
  return tagMap[status] || 'info';
};

const getExecutionStatusText = (status) => {
  const textMap = {
    0: '未开始',
    1: '进行中',
    2: '已完成',
    3: '异常',
    4: '已取消'
  };
  return textMap[status] || '未知';
};

const getAccuracyClass = (rate) => {
  if (rate >= 99.5) return 'text-success';
  if (rate >= 98) return 'text-warning';
  return 'text-danger';
};

const getDifferenceClass = (rate) => {
  if (rate <= 0.5) return 'text-success';
  if (rate <= 2) return 'text-warning';
  return 'text-danger';
};

const viewExecutionDetail = (row) => {
  console.log('查看执行详情:', row);
  // 这里可以打开执行详情弹窗
};

const validateForm = async () => {
  if (!baseFormRef.value) return false;
  
  try {
    await baseFormRef.value.validate();
    
    // 验证循环配置
    if (!cycleConfig.executeTime) {
      ElMessage.warning('请选择执行时间');
      return false;
    }
    
    if (cycleConfig.endCondition === 1 && !cycleConfig.endTime) {
      ElMessage.warning('请选择结束时间');
      return false;
    }
    
    if (cycleConfig.endCondition === 2 && (!cycleConfig.maxCycles || cycleConfig.maxCycles < 1)) {
      ElMessage.warning('请输入有效的循环次数');
      return false;
    }
    
    // 验证ABC分类设置
    const abcTotal = selectionStrategy.abcClassA + selectionStrategy.abcClassB + selectionStrategy.abcClassC;
    if (abcTotal !== 100) {
      ElMessage.warning('ABC分类比例之和必须为100%');
      return false;
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
    const saveData = {
      ...formData,
      cycleConfig: JSON.stringify(cycleConfig),
      selectionStrategy: JSON.stringify(selectionStrategy),
      takeStrategy: JSON.stringify(takeStrategy),
      takeStrategy: 4, // 循环盘点
      status: 0, // 草稿状态
      approvalStatus: 0 // 未提交审批
    };
    
    const res = await post('/api/auth/stock-take/cycle/save', saveData);
    
    if (res) {
      formData.id = res.id;
      formData.orderNo = res.orderNo || formData.orderNo;
      ElMessage.success('保存成功');
      
      // 如果是新建保存，更新URL中的ID
      if (route.params.id && route.params.id !== res.id) {
        router.replace(`/index/ckStockTakeCycleEdit/${res.id}`);
      }
      
      // 生成循环预览
      generateCyclePreview();
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
      `确定要提交循环盘点单"${formData.takeName}"审批吗？提交后不可修改。`,
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
    
    const res = await post('/api/auth/stock-take/cycle/submit', submitData);
    
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
  formData.orderNo = `${prefix}Y${timestamp}${random}`;
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
    
    // 设置默认结束时间为3个月后
    const defaultEndTime = new Date();
    defaultEndTime.setMonth(defaultEndTime.getMonth() + 3);
    cycleConfig.endTime = defaultEndTime.toISOString().slice(0, 19).replace('T', ' ');
  }
};

// 监听仓库变化
watch(() => formData.warehouseId, (newVal) => {
  if (newVal) {
    loadKeyProducts();
  }
});

onMounted(() => {
  initPage();
});
</script>

<style scoped>
.stock-take-cycle-container {
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

.input-separator {
  margin: 0 8px;
  color: #c0c4cc;
}

.cycle-summary {
  margin-top: 20px;
  padding: 16px;
  background-color: #f8f9fa;
  border-radius: 4px;
  border: 1px solid #ebeef5;
}

.summary-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
}

.summary-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.preview-content {
  margin-top: 16px;
}

.preview-stats {
  margin-top: 16px;
}

.no-preview {
  padding: 40px 0;
  text-align: center;
}

.execution-history {
  margin-top: 16px;
}

.history-summary {
  margin-top: 20px;
}

.text-success {
  color: #67C23A;
  font-weight: bold;
}

.text-warning {
  color: #E6A23C;
  font-weight: bold;
}

.text-danger {
  color: #F56C6C;
  font-weight: bold;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .stock-take-cycle-container {
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
  
  .input-separator {
    margin: 0 4px;
  }
}
</style>