
<!-- 随机抽盘 -->
 <template>
  <div class="stock-take-random-container">
    <el-card class="manage-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">随机抽盘</span>
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
              <el-form-item label="计划盘点时间" prop="planTime">
                <el-date-picker
                  v-model="formData.planTime"
                  type="datetime"
                  placeholder="选择盘点时间"
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

      <!-- 抽样配置 -->
      <el-card class="form-section" shadow="never">
        <template #header>
          <div class="section-header">
            <span class="section-title">抽样配置</span>
          </div>
        </template>
        
        <el-form 
          :model="sampleConfig" 
          label-width="120px"
          :disabled="isViewMode"
        >
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="抽样方式" prop="sampleMethod">
                <el-select
                  v-model="sampleConfig.sampleMethod"
                  placeholder="请选择抽样方式"
                  style="width: 100%"
                  @change="handleSampleMethodChange"
                >
                  <el-option label="按数量抽样" :value="1" />
                  <el-option label="按比例抽样" :value="2" />
                  <el-option label="分层抽样" :value="3" />
                  <el-option label="整群抽样" :value="4" />
                  <el-option label="系统抽样" :value="5" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item 
                label="抽样数量" 
                prop="sampleCount"
                v-if="sampleConfig.sampleMethod === 1"
              >
                <el-input-number
                  v-model="sampleConfig.sampleCount"
                  :min="1"
                  :max="maxSampleCount"
                  :step="1"
                  style="width: 100%"
                  placeholder="请输入抽样数量"
                  @change="updateSampleCount"
                />
              </el-form-item>
              <el-form-item 
                label="抽样比例" 
                prop="sampleRatio"
                v-else-if="sampleConfig.sampleMethod === 2"
              >
                <el-input-number
                  v-model="sampleConfig.sampleRatio"
                  :min="0.1"
                  :max="100"
                  :step="0.1"
                  :precision="2"
                  style="width: 100%"
                  placeholder="请输入抽样比例"
                  @change="updateSampleRatio"
                >
                  <template #append>%</template>
                </el-input-number>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="24" v-if="sampleConfig.sampleMethod === 3">
            <el-col :span="24">
              <el-form-item label="分层配置" prop="stratumConfig">
                <div class="stratum-config">
                  <div class="stratum-header">
                    <span class="stratum-title">分层规则</span>
                    <el-button 
                      type="primary" 
                      size="small"
                      @click="addStratum"
                      v-if="!isViewMode"
                    >
                      添加分层
                    </el-button>
                  </div>
                  
                  <div class="stratum-list" v-if="sampleConfig.stratumRules.length > 0">
                    <div 
                      v-for="(rule, index) in sampleConfig.stratumRules" 
                      :key="index"
                      class="stratum-item"
                    >
                      <div class="stratum-info">
                        <div class="stratum-name">分层 {{ index + 1 }}</div>
                        <el-button 
                          type="danger" 
                          link
                          size="small"
                          @click="removeStratum(index)"
                          v-if="!isViewMode"
                        >
                          删除
                        </el-button>
                      </div>
                      
                      <div class="stratum-fields">
                        <el-form-item label="分层名称" style="margin-bottom: 12px">
                          <el-input
                            v-model="rule.name"
                            placeholder="请输入分层名称"
                            style="width: 200px"
                          />
                        </el-form-item>
                        
                        <el-form-item label="条件类型" style="margin-bottom: 12px">
                          <el-select
                            v-model="rule.conditionType"
                            placeholder="选择条件类型"
                            style="width: 150px"
                            @change="handleConditionTypeChange(rule)"
                          >
                            <el-option label="按库存价值" :value="1" />
                            <el-option label="按产品分类" :value="2" />
                            <el-option label="按周转率" :value="3" />
                            <el-option label="按批次" :value="4" />
                            <el-option label="自定义" :value="5" />
                          </el-select>
                        </el-form-item>
                        
                        <div class="condition-config">
                          <div v-if="rule.conditionType === 1" class="condition-item">
                            <span>库存价值范围：</span>
                            <el-input-number
                              v-model="rule.minValue"
                              :min="0"
                              :step="100"
                              placeholder="最小值"
                              style="width: 120px"
                            />
                            <span class="range-separator">-</span>
                            <el-input-number
                              v-model="rule.maxValue"
                              :min="rule.minValue || 0"
                              :step="100"
                              placeholder="最大值"
                              style="width: 120px"
                            />
                            <span>元</span>
                          </div>
                          
                          <div v-if="rule.conditionType === 2" class="condition-item">
                            <span>产品分类：</span>
                            <el-cascader
                              v-model="rule.categoryCodes"
                              :options="categoryTree"
                              :props="{ value: 'categoryCode', label: 'categoryName', children: 'children' }"
                              placeholder="选择分类"
                              clearable
                              style="width: 300px"
                            />
                          </div>
                          
                          <div v-if="rule.conditionType === 3" class="condition-item">
                            <span>周转率范围：</span>
                            <el-input-number
                              v-model="rule.minTurnover"
                              :min="0"
                              :step="0.1"
                              :precision="2"
                              placeholder="最小周转率"
                              style="width: 120px"
                            />
                            <span class="range-separator">-</span>
                            <el-input-number
                              v-model="rule.maxTurnover"
                              :min="rule.minTurnover || 0"
                              :step="0.1"
                              :precision="2"
                              placeholder="最大周转率"
                              style="width: 120px"
                            />
                            <span>次/月</span>
                          </div>
                          
                          <div v-if="rule.conditionType === 4" class="condition-item">
                            <span>批次要求：</span>
                            <el-checkbox-group v-model="rule.batchRequirements">
                              <el-checkbox label="1">需要批次管理</el-checkbox>
                              <el-checkbox label="2">有批次库存</el-checkbox>
                              <el-checkbox label="3">近效期产品</el-checkbox>
                            </el-checkbox-group>
                          </div>
                          
                          <div v-if="rule.conditionType === 5" class="condition-item">
                            <span>自定义条件：</span>
                            <el-input
                              v-model="rule.customCondition"
                              placeholder="请输入自定义条件表达式"
                              style="width: 300px"
                            />
                          </div>
                        </div>
                        
                        <div class="sample-setting">
                          <el-form-item label="抽样数量" style="margin-bottom: 12px">
                            <el-input-number
                              v-model="rule.sampleCount"
                              :min="1"
                              :max="1000"
                              :step="1"
                              placeholder="抽样数量"
                              style="width: 150px"
                            />
                          </el-form-item>
                          
                          <el-form-item label="抽样比例" style="margin-bottom: 12px; margin-left: 16px">
                            <el-input-number
                              v-model="rule.sampleRatio"
                              :min="0.1"
                              :max="100"
                              :step="0.1"
                              :precision="2"
                              placeholder="抽样比例"
                              style="width: 150px"
                            >
                              <template #append>%</template>
                            </el-input-number>
                          </el-form-item>
                        </div>
                      </div>
                    </div>
                  </div>
                  
                  <div class="no-stratum" v-else>
                    <el-empty description="暂无分层配置，请添加分层规则" />
                  </div>
                </div>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="24" v-if="sampleConfig.sampleMethod === 4">
            <el-col :span="12">
              <el-form-item label="群组类型" prop="groupType">
                <el-select
                  v-model="sampleConfig.groupType"
                  placeholder="请选择群组类型"
                  style="width: 100%"
                >
                  <el-option label="按货架" :value="1" />
                  <el-option label="按区域" :value="2" />
                  <el-option label="按分类" :value="3" />
                  <el-option label="按供应商" :value="4" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="抽样群组数" prop="groupSampleCount">
                <el-input-number
                  v-model="sampleConfig.groupSampleCount"
                  :min="1"
                  :max="100"
                  :step="1"
                  style="width: 100%"
                  placeholder="请输入抽样群组数"
                />
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="24" v-if="sampleConfig.sampleMethod === 5">
            <el-col :span="12">
              <el-form-item label="抽样间隔" prop="systemInterval">
                <el-input-number
                  v-model="sampleConfig.systemInterval"
                  :min="1"
                  :max="1000"
                  :step="1"
                  style="width: 100%"
                  placeholder="请输入抽样间隔"
                />
                <div class="form-tip">每隔多少个产品抽取一个样本</div>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="起始位置" prop="startPosition">
                <el-input-number
                  v-model="sampleConfig.startPosition"
                  :min="1"
                  :max="10000"
                  :step="1"
                  style="width: 100%"
                  placeholder="请输入起始位置"
                />
                <div class="form-tip">从第几个产品开始抽样</div>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="随机种子" prop="randomSeed">
                <el-input
                  v-model="sampleConfig.randomSeed"
                  placeholder="请输入随机种子"
                  style="width: 100%"
                >
                  <template #append>
                    <el-button @click="generateRandomSeed" v-if="!isViewMode">
                      随机生成
                    </el-button>
                  </template>
                </el-input>
                <div class="form-tip">相同的随机种子可保证抽样结果可重现</div>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="置信水平" prop="confidenceLevel">
                <el-input-number
                  v-model="sampleConfig.confidenceLevel"
                  :min="80"
                  :max="99.9"
                  :step="0.1"
                  :precision="1"
                  style="width: 100%"
                  placeholder="请输入置信水平"
                >
                  <template #append>%</template>
                </el-input-number>
                <div class="form-tip">抽样结果的可信程度，建议95%以上</div>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="允许误差" prop="allowableError">
                <el-input-number
                  v-model="sampleConfig.allowableError"
                  :min="0.1"
                  :max="10"
                  :step="0.1"
                  :precision="2"
                  style="width: 100%"
                  placeholder="请输入允许误差"
                >
                  <template #append>%</template>
                </el-input-number>
                <div class="form-tip">抽样估计值与实际值的最大允许误差</div>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="最小样本量" prop="minSampleSize">
                <el-input-number
                  v-model="sampleConfig.minSampleSize"
                  :min="1"
                  :max="1000"
                  :step="1"
                  style="width: 100%"
                  placeholder="请输入最小样本量"
                />
                <div class="form-tip">保证抽样有效性的最小样本数量</div>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="排除条件" prop="excludeConditions">
                <el-checkbox-group v-model="sampleConfig.excludeConditions">
                  <el-checkbox label="1">零库存产品</el-checkbox>
                  <el-checkbox label="2">近效期产品</el-checkbox>
                  <el-checkbox label="3">贵重产品</el-checkbox>
                  <el-checkbox label="4">样品</el-checkbox>
                </el-checkbox-group>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="近效期阈值" v-if="sampleConfig.excludeConditions.includes('2')">
                <el-input-number
                  v-model="sampleConfig.expiryThreshold"
                  :min="1"
                  :max="365"
                  :step="1"
                  style="width: 100%"
                  placeholder="请输入近效期阈值"
                >
                  <template #append>天</template>
                </el-input-number>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </el-card>

      <!-- 抽样预览 -->
      <el-card class="form-section" shadow="never">
        <template #header>
          <div class="section-header">
            <span class="section-title">抽样预览</span>
            <div class="section-actions">
              <el-button 
                type="primary" 
                size="small"
                @click="generateSamplePreview"
                :loading="previewLoading"
                v-if="!isViewMode"
              >
                生成预览
              </el-button>
              <el-button 
                type="success" 
                size="small"
                @click="refreshSample"
                :loading="refreshLoading"
                v-if="!isViewMode && samplePreview.length > 0"
              >
                重新抽样
              </el-button>
            </div>
          </div>
        </template>
        
        <div class="sample-preview">
          <div class="preview-stats" v-if="sampleStats.totalProducts > 0">
            <el-descriptions :column="4" border>
              <el-descriptions-item label="仓库总产品数">{{ sampleStats.totalProducts }} 个</el-descriptions-item>
              <el-descriptions-item label="抽样产品数">{{ sampleStats.sampleProducts }} 个</el-descriptions-item>
              <el-descriptions-item label="抽样比例">{{ sampleStats.sampleRatio }}%</el-descriptions-item>
              <el-descriptions-item label="置信区间">[{{ sampleStats.confidenceInterval[0] }}%, {{ sampleStats.confidenceInterval[1] }}%]</el-descriptions-item>
              <el-descriptions-item label="总库存价值">¥{{ sampleStats.totalValue.toFixed(2) }}</el-descriptions-item>
              <el-descriptions-item label="抽样库存价值">¥{{ sampleStats.sampleValue.toFixed(2) }}</el-descriptions-item>
              <el-descriptions-item label="覆盖率">{{ sampleStats.coverageRate }}%</el-descriptions-item>
              <el-descriptions-item label="抽样误差">{{ sampleStats.samplingError }}%</el-descriptions-item>
            </el-descriptions>
          </div>
          
          <div class="preview-content" v-if="samplePreview.length > 0">
            <el-table 
              :data="samplePreview" 
              border 
              style="width: 100%"
              max-height="400"
              v-loading="previewLoading"
            >
              <el-table-column type="index" label="序号" width="60" align="center" />
              <el-table-column label="产品信息" min-width="250" fixed="left">
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
              <el-table-column label="周转率" width="100" align="center">
                <template #default="{ row }">
                  {{ row.turnoverRate || 0 }}
                </template>
              </el-table-column>
              <el-table-column label="ABC分类" width="100" align="center">
                <template #default="{ row }">
                  <el-tag :type="getAbcTagType(row.abcClass)" size="small">
                    {{ row.abcClass || 'C' }}类
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="批次管理" width="100" align="center">
                <template #default="{ row }">
                  <el-tag :type="row.batchManage ? 'success' : 'info'" size="small">
                    {{ row.batchManage ? '是' : '否' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="抽中概率" width="120" align="center">
                <template #default="{ row }">
                  {{ row.sampleProbability || 0 }}%
                </template>
              </el-table-column>
              <el-table-column label="操作" width="80" align="center" fixed="right" v-if="!isViewMode">
                <template #default="{ row }">
                  <el-button
                    type="danger"
                    link
                    size="small"
                    @click="removeFromSample(row)"
                  >
                    移除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
            
            <div class="preview-actions" v-if="samplePreview.length > 0 && !isViewMode">
              <el-button 
                type="primary" 
                @click="addManualSample"
              >
                手动添加产品
              </el-button>
              <el-button 
                type="success" 
                @click="exportSampleList"
              >
                导出抽样清单
              </el-button>
            </div>
          </div>
          <div class="no-preview" v-else>
            <el-empty description="请先配置抽样参数并生成预览" />
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
              <el-form-item label="抽样检查方式" prop="sampleCheckMethod">
                <el-select
                  v-model="takeStrategy.sampleCheckMethod"
                  placeholder="请选择检查方式"
                  style="width: 100%"
                >
                  <el-option label="全量检查" :value="1" />
                  <el-option label="交叉检查" :value="2" />
                  <el-option label="抽样复核" :value="3" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="是否需要复核" prop="needReview">
                <el-switch
                  v-model="takeStrategy.needReview"
                  active-text="需要"
                  inactive-text="不需要"
                />
                <div class="form-tip">开启后，对差异产品进行复核</div>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="复核阈值" v-if="takeStrategy.needReview">
                <el-input-number
                  v-model="takeStrategy.reviewThreshold"
                  :min="0.1"
                  :max="100"
                  :step="0.5"
                  :precision="2"
                  style="width: 100%"
                  placeholder="请输入复核阈值"
                >
                  <template #append>%</template>
                </el-input-number>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="24">
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
            <el-col :span="12">
              <el-form-item label="抽样报告生成" prop="generateReport">
                <el-switch
                  v-model="takeStrategy.generateReport"
                  active-text="生成"
                  inactive-text="不生成"
                />
                <div class="form-tip">开启后，生成详细的抽样盘点报告</div>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="结果推断方式" prop="inferenceMethod">
                <el-select
                  v-model="takeStrategy.inferenceMethod"
                  placeholder="请选择推断方式"
                  style="width: 100%"
                >
                  <el-option label="点估计" :value="1" />
                  <el-option label="区间估计" :value="2" />
                  <el-option label="假设检验" :value="3" />
                </el-select>
                <div class="form-tip">根据抽样结果推断整体库存状况</div>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="允许推断误差" prop="inferenceError">
                <el-input-number
                  v-model="takeStrategy.inferenceError"
                  :min="0.1"
                  :max="10"
                  :step="0.1"
                  :precision="2"
                  style="width: 100%"
                  placeholder="请输入允许推断误差"
                >
                  <template #append>%</template>
                </el-input-number>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="抽查质量标准" prop="qualityStandard">
                <el-input-number
                  v-model="takeStrategy.qualityStandard"
                  :min="80"
                  :max="100"
                  :step="0.1"
                  :precision="1"
                  style="width: 100%"
                  placeholder="请输入质量标准"
                >
                  <template #append>%</template>
                </el-input-number>
                <div class="form-tip">抽样盘点结果的最低质量标准</div>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="异常处理策略" prop="exceptionStrategy">
                <el-select
                  v-model="takeStrategy.exceptionStrategy"
                  placeholder="请选择异常处理策略"
                  style="width: 100%"
                >
                  <el-option label="扩大抽样" :value="1" />
                  <el-option label="全库盘点" :value="2" />
                  <el-option label="暂停处理" :value="3" />
                  <el-option label="报警通知" :value="4" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </el-card>

      <!-- 结果预测 -->
      <el-card class="form-section" shadow="never" v-if="samplePreview.length > 0 && sampleStats.totalProducts > 0">
        <template #header>
          <div class="section-header">
            <span class="section-title">结果预测</span>
          </div>
        </template>
        
        <div class="result-prediction">
          <div class="prediction-charts">
            <div class="chart-container">
              <div class="chart-title">抽样分布预测</div>
              <div class="chart-placeholder">
                <!-- 这里可以集成图表库，如ECharts -->
                <div class="mock-chart">
                  <div class="mock-chart-title">ABC类产品抽样分布</div>
                  <div class="mock-chart-content">
                    <div class="mock-bar-chart">
                      <div class="bar-group">
                        <div class="bar-label">A类</div>
                        <div class="bar-container">
                          <div class="bar" :style="{ width: predictionStats.abcDistribution.A + '%' }">
                            <span class="bar-value">{{ predictionStats.abcDistribution.A }}%</span>
                          </div>
                        </div>
                      </div>
                      <div class="bar-group">
                        <div class="bar-label">B类</div>
                        <div class="bar-container">
                          <div class="bar" :style="{ width: predictionStats.abcDistribution.B + '%' }">
                            <span class="bar-value">{{ predictionStats.abcDistribution.B }}%</span>
                          </div>
                        </div>
                      </div>
                      <div class="bar-group">
                        <div class="bar-label">C类</div>
                        <div class="bar-container">
                          <div class="bar" :style="{ width: predictionStats.abcDistribution.C + '%' }">
                            <span class="bar-value">{{ predictionStats.abcDistribution.C }}%</span>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            
            <div class="chart-container">
              <div class="chart-title">准确率预测</div>
              <div class="chart-placeholder">
                <div class="mock-chart">
                  <div class="mock-chart-title">准确率置信区间</div>
                  <div class="mock-chart-content">
                    <div class="accuracy-prediction">
                      <div class="prediction-value">
                        预计准确率: {{ predictionStats.expectedAccuracy }}%
                      </div>
                      <div class="confidence-interval">
                        置信区间: [{{ predictionStats.confidenceInterval[0] }}%, {{ predictionStats.confidenceInterval[1] }}%]
                      </div>
                      <div class="quality-assessment" :class="getQualityClass(predictionStats.qualityScore)">
                        质量评分: {{ predictionStats.qualityScore }}/100
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <div class="prediction-summary">
            <el-descriptions :column="3" border>
              <el-descriptions-item label="预测差异率">{{ predictionStats.expectedDifferenceRate }}%</el-descriptions-item>
              <el-descriptions-item label="预测调整金额">¥{{ predictionStats.expectedAdjustAmount.toFixed(2) }}</el-descriptions-item>
              <el-descriptions-item label="预测覆盖率">{{ predictionStats.expectedCoverage }}%</el-descriptions-item>
              <el-descriptions-item label="质量评估">{{ predictionStats.qualityAssessment }}</el-descriptions-item>
              <el-descriptions-item label="风险等级">
                <el-tag :type="getRiskTagType(predictionStats.riskLevel)" size="small">
                  {{ getRiskText(predictionStats.riskLevel) }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="建议措施">{{ predictionStats.recommendation }}</el-descriptions-item>
            </el-descriptions>
          </div>
        </div>
      </el-card>
    </el-card>
    
    <!-- 手动添加产品对话框 -->
    <el-dialog
      v-model="addProductDialogVisible"
      title="手动添加产品"
      width="800px"
      class="add-product-dialog"
    >
      <div class="add-product-content">
        <div class="product-search">
          <el-input
            v-model="productSearch.keyword"
            placeholder="搜索产品名称、SKU"
            clearable
            style="width: 300px"
            @keyup.enter="searchProducts"
          >
            <template #append>
              <el-button @click="searchProducts">
                <el-icon><Search /></el-icon>
              </el-button>
            </template>
          </el-input>
          
          <el-select
            v-model="productSearch.categoryCode"
            placeholder="按分类筛选"
            clearable
            style="width: 200px; margin-left: 12px"
          >
            <el-option
              v-for="category in categoryTree"
              :key="category.categoryCode"
              :label="category.categoryName"
              :value="category.categoryCode"
            />
          </el-select>
        </div>
        
        <div class="product-list">
          <el-table 
            :data="filteredProductList" 
            border 
            style="width: 100%"
            max-height="300"
            v-loading="productListLoading"
            @selection-change="handleManualSelectionChange"
          >
            <el-table-column type="selection" width="55" align="center" />
            <el-table-column type="index" label="序号" width="60" align="center" />
            <el-table-column label="产品信息" min-width="250">
              <template #default="{ row }">
                <div class="product-info">
                  <div class="product-name">{{ row.name }}</div>
                  <div class="product-sku">SKU: {{ row.sku }}</div>
                </div>
              </template>
            </el-table-column>
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
            <el-table-column label="ABC分类" width="100" align="center">
              <template #default="{ row }">
                {{ row.abcClass || 'C' }}类
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="addProductDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmAddProducts" :loading="addingProducts">
            添加选中产品
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Document, CircleCheck, Back, Search } from '@element-plus/icons-vue';
import { post, get } from '@/net';

const route = useRoute();
const router = useRouter();
const baseFormRef = ref(null);

const saving = ref(false);
const submitting = ref(false);
const previewLoading = ref(false);
const refreshLoading = ref(false);
const productListLoading = ref(false);
const addingProducts = ref(false);
const addProductDialogVisible = ref(false);

// 判断是否为查看模式
const isViewMode = computed(() => route.name === 'CkStockTakeRandomView' || route.params.view === 'true');

// 表单数据
const formData = reactive({
  id: null,
  orderNo: '',
  takeName: '',
  takeType: 1, // 1-动态盘点, 2-静态盘点
  takeStrategy: 5, // 5-随机抽盘
  warehouseId: null,
  toleranceRate: 0.5, // 默认0.5%
  planTime: '',
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
  planTime: [
    { required: true, message: '请选择计划盘点时间', trigger: 'change' }
  ]
};

// 抽样配置
const sampleConfig = reactive({
  sampleMethod: 1, // 1-按数量, 2-按比例, 3-分层, 4-整群, 5-系统
  sampleCount: 50,
  sampleRatio: 5,
  stratumRules: [],
  groupType: 1,
  groupSampleCount: 5,
  systemInterval: 10,
  startPosition: 1,
  randomSeed: '',
  confidenceLevel: 95,
  allowableError: 2,
  minSampleSize: 30,
  excludeConditions: ['1'], // 默认排除零库存
  expiryThreshold: 30
});

// 盘点策略
const takeStrategy = reactive({
  countMode: 1, // 1-盲盘, 2-明盘
  sampleCheckMethod: 1, // 1-全量检查, 2-交叉检查, 3-抽样复核
  needReview: true,
  reviewThreshold: 2,
  autoGenerateAdjust: true,
  generateReport: true,
  inferenceMethod: 2, // 1-点估计, 2-区间估计, 3-假设检验
  inferenceError: 1,
  qualityStandard: 95,
  exceptionStrategy: 1 // 1-扩大抽样, 2-全库盘点, 3-暂停处理, 4-报警通知
});

// 抽样预览
const samplePreview = ref([]);
const sampleStats = reactive({
  totalProducts: 0,
  sampleProducts: 0,
  sampleRatio: 0,
  confidenceInterval: [0, 0],
  totalValue: 0,
  sampleValue: 0,
  coverageRate: 0,
  samplingError: 0
});

// 结果预测
const predictionStats = reactive({
  abcDistribution: {
    A: 30,
    B: 40,
    C: 30
  },
  expectedAccuracy: 98.5,
  confidenceInterval: [97.8, 99.2],
  qualityScore: 88,
  expectedDifferenceRate: 1.2,
  expectedAdjustAmount: 1250.50,
  expectedCoverage: 85.3,
  qualityAssessment: '良好',
  riskLevel: 2,
  recommendation: '按计划执行抽样盘点'
});

// 手动添加产品
const productSearch = reactive({
  keyword: '',
  categoryCode: ''
});
const allProductList = ref([]);
const filteredProductList = ref([]);
const manualSelectedProducts = ref([]);
const categoryTree = ref([]);

// 数据列表
const warehouseList = ref([]);

// 计算属性
const maxSampleCount = computed(() => {
  // 最大抽样数量不超过仓库总产品数的80%
  return Math.floor(sampleStats.totalProducts * 0.8) || 1000;
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
    const res = await get('/api/auth/product-category/tree');
    categoryTree.value = res || [];
  } catch (error) {
    console.error('加载分类树失败:', error);
    categoryTree.value = [];
  }
};

const loadProductList = async () => {
  if (!formData.warehouseId) return;
  
  productListLoading.value = true;
  try {
    const res = await get('/api/auth/product/warehouse-list', {
      warehouseId: formData.warehouseId,
      pageSize: 1000
    });
    
    if (res && res.records) {
      allProductList.value = res.records.map(product => ({
        id: product.id,
        sku: product.sku,
        name: product.name,
        spec: product.spec,
        unitCode: product.unitCode,
        categoryCode: product.categoryCode,
        quantity: product.quantity || 0,
        unitPrice: product.unitPrice || 0,
        abcClass: product.abcClass || 'C',
        turnoverRate: product.turnoverRate || 0
      }));
      
      filteredProductList.value = [...allProductList.value];
    } else {
      allProductList.value = [];
      filteredProductList.value = [];
    }
  } catch (error) {
    console.error('加载产品列表失败:', error);
    allProductList.value = [];
    filteredProductList.value = [];
  } finally {
    productListLoading.value = false;
  }
};

const loadStockTakeDetail = async (id) => {
  try {
    const res = await get(`/api/auth/stock-take/random-detail?id=${id}`);
    if (res) {
      // 填充表单数据
      Object.assign(formData, {
        id: res.id,
        orderNo: res.orderNo,
        takeName: res.takeName,
        takeType: res.takeType,
        warehouseId: res.warehouseId,
        toleranceRate: res.toleranceRate,
        planTime: res.planTime,
        remark: res.remark,
        status: res.status,
        approvalStatus: res.approvalStatus
      });
      
      // 填充配置
      if (res.sampleConfig) {
        Object.assign(sampleConfig, JSON.parse(res.sampleConfig));
      }
      
      if (res.takeStrategy) {
        Object.assign(takeStrategy, JSON.parse(res.takeStrategy));
      }
      
      // 加载分类树
      await loadCategoryTree();
      
      // 加载产品列表
      await loadProductList();
      
      // 如果有抽样数据，加载预览
      if (res.samplePreview && res.samplePreview.length > 0) {
        samplePreview.value = res.samplePreview;
        Object.assign(sampleStats, res.sampleStats || {});
      }
    }
  } catch (error) {
    console.error('加载盘点单详情失败:', error);
    ElMessage.error('加载数据失败');
  }
};

const handleWarehouseChange = () => {
  // 清空抽样预览
  samplePreview.value = [];
  // 加载产品列表
  loadProductList();
  // 更新仓库统计
  updateWarehouseStats();
};

const updateWarehouseStats = async () => {
  if (!formData.warehouseId) return;
  
  try {
    const res = await get('/api/auth/warehouse/stats', {
      warehouseId: formData.warehouseId
    });
    
    if (res) {
      sampleStats.totalProducts = res.productCount || 0;
      sampleStats.totalValue = res.totalValue || 0;
      
      // 更新最大抽样数量
      if (sampleConfig.sampleCount > maxSampleCount.value) {
        sampleConfig.sampleCount = maxSampleCount.value;
      }
    }
  } catch (error) {
    console.error('更新仓库统计失败:', error);
  }
};

const handleSampleMethodChange = () => {
  // 重置相关配置
  if (sampleConfig.sampleMethod !== 3) {
    sampleConfig.stratumRules = [];
  }
};

const updateSampleCount = () => {
  // 计算抽样比例
  if (sampleStats.totalProducts > 0) {
    sampleConfig.sampleRatio = Math.round((sampleConfig.sampleCount / sampleStats.totalProducts) * 10000) / 100;
  }
};

const updateSampleRatio = () => {
  // 计算抽样数量
  if (sampleStats.totalProducts > 0) {
    const count = Math.round((sampleConfig.sampleRatio / 100) * sampleStats.totalProducts);
    sampleConfig.sampleCount = Math.min(Math.max(count, 1), maxSampleCount.value);
  }
};

const addStratum = () => {
  sampleConfig.stratumRules.push({
    name: `分层 ${sampleConfig.stratumRules.length + 1}`,
    conditionType: 1,
    minValue: 0,
    maxValue: 1000,
    categoryCodes: [],
    minTurnover: 0,
    maxTurnover: 5,
    batchRequirements: [],
    customCondition: '',
    sampleCount: 10,
    sampleRatio: 10
  });
};

const removeStratum = (index) => {
  sampleConfig.stratumRules.splice(index, 1);
};

const handleConditionTypeChange = (rule) => {
  // 根据条件类型重置相关字段
  rule.minValue = 0;
  rule.maxValue = 1000;
  rule.categoryCodes = [];
  rule.minTurnover = 0;
  rule.maxTurnover = 5;
  rule.batchRequirements = [];
  rule.customCondition = '';
};

const generateRandomSeed = () => {
  sampleConfig.randomSeed = Math.random().toString(36).substring(2, 15);
};

const generateSamplePreview = async () => {
  if (!formData.warehouseId) {
    ElMessage.warning('请先选择仓库');
    return;
  }
  
  if (sampleStats.totalProducts === 0) {
    await updateWarehouseStats();
  }
  
  previewLoading.value = true;
  try {
    const params = {
      warehouseId: formData.warehouseId,
      sampleConfig: sampleConfig,
      warehouseStats: sampleStats
    };
    
    const res = await post('/api/auth/stock-take/random-preview', params);
    
    if (res && res.samples) {
      samplePreview.value = res.samples.map(product => ({
        id: product.id,
        sku: product.sku,
        name: product.name,
        spec: product.spec,
        unitCode: product.unitCode,
        categoryName: product.categoryName,
        quantity: product.quantity || 0,
        unitPrice: product.unitPrice || 0,
        turnoverRate: product.turnoverRate || 0,
        abcClass: product.abcClass || 'C',
        batchManage: product.batchManage || false,
        sampleProbability: product.sampleProbability || 0
      }));
      
      Object.assign(sampleStats, res.stats || {});
      
      // 更新预测统计
      updatePredictionStats();
      
      ElMessage.success('抽样预览已生成');
    }
  } catch (error) {
    console.error('生成抽样预览失败:', error);
    ElMessage.error('生成预览失败');
  } finally {
    previewLoading.value = false;
  }
};

const refreshSample = async () => {
  refreshLoading.value = true;
  try {
    // 生成新的随机种子
    generateRandomSeed();
    
    // 重新生成预览
    await generateSamplePreview();
    
    ElMessage.success('已重新抽样');
  } catch (error) {
    console.error('重新抽样失败:', error);
    ElMessage.error('重新抽样失败');
  } finally {
    refreshLoading.value = false;
  }
};

const removeFromSample = (row) => {
  const index = samplePreview.value.findIndex(item => item.id === row.id);
  if (index !== -1) {
    samplePreview.value.splice(index, 1);
    sampleStats.sampleProducts--;
    sampleStats.sampleRatio = Math.round((sampleStats.sampleProducts / sampleStats.totalProducts) * 10000) / 100;
    ElMessage.success('已从抽样中移除');
  }
};

const addManualSample = async () => {
  await loadProductList();
  addProductDialogVisible.value = true;
};

const searchProducts = () => {
  if (!allProductList.value.length) return;
  
  let result = allProductList.value;
  
  // 按关键词筛选
  if (productSearch.keyword) {
    const keyword = productSearch.keyword.toLowerCase();
    result = result.filter(product => 
      product.name.toLowerCase().includes(keyword) || 
      product.sku.toLowerCase().includes(keyword)
    );
  }
  
  // 按分类筛选
  if (productSearch.categoryCode) {
    result = result.filter(product => product.categoryCode === productSearch.categoryCode);
  }
  
  filteredProductList.value = result;
};

const handleManualSelectionChange = (selection) => {
  manualSelectedProducts.value = selection;
};

const confirmAddProducts = () => {
  if (manualSelectedProducts.value.length === 0) {
    ElMessage.warning('请选择要添加的产品');
    return;
  }
  
  addingProducts.value = true;
  
  try {
    // 添加选中的产品到抽样列表
    manualSelectedProducts.value.forEach(product => {
      // 检查是否已存在
      const exists = samplePreview.value.some(item => item.id === product.id);
      if (!exists) {
        samplePreview.value.push({
          ...product,
          sampleProbability: 100, // 手动添加的概率为100%
          categoryName: product.categoryCode // 这里应该查询分类名称
        });
      }
    });
    
    // 更新统计
    sampleStats.sampleProducts = samplePreview.value.length;
    sampleStats.sampleRatio = Math.round((sampleStats.sampleProducts / sampleStats.totalProducts) * 10000) / 100;
    
    // 计算抽样价值
    sampleStats.sampleValue = samplePreview.value.reduce((sum, item) => {
      return sum + ((item.quantity || 0) * (item.unitPrice || 0));
    }, 0);
    
    addProductDialogVisible.value = false;
    manualSelectedProducts.value = [];
    
    ElMessage.success(`已添加${manualSelectedProducts.value.length}个产品到抽样列表`);
  } catch (error) {
    console.error('添加产品失败:', error);
    ElMessage.error('添加产品失败');
  } finally {
    addingProducts.value = false;
  }
};

const exportSampleList = () => {
  // 导出抽样清单逻辑
  console.log('导出抽样清单:', samplePreview.value);
  ElMessage.success('导出功能开发中');
};

const updatePredictionStats = () => {
  // 根据抽样数据更新预测统计
  if (samplePreview.value.length === 0) return;
  
  // 计算ABC分布
  const abcCounts = { A: 0, B: 0, C: 0 };
  samplePreview.value.forEach(item => {
    const abcClass = item.abcClass || 'C';
    if (abcClass in abcCounts) {
      abcCounts[abcClass]++;
    }
  });
  
  const total = samplePreview.value.length;
  predictionStats.abcDistribution = {
    A: Math.round((abcCounts.A / total) * 100),
    B: Math.round((abcCounts.B / total) * 100),
    C: Math.round((abcCounts.C / total) * 100)
  };
  
  // 根据置信水平和允许误差计算置信区间
  const confidence = sampleConfig.confidenceLevel / 100;
  const zScore = confidence === 0.95 ? 1.96 : confidence === 0.99 ? 2.576 : 1.645;
  const margin = zScore * Math.sqrt((0.5 * 0.5) / total);
  
  predictionStats.expectedAccuracy = 100 - sampleStats.samplingError;
  predictionStats.confidenceInterval = [
    Math.max(0, Math.round((predictionStats.expectedAccuracy - margin * 100) * 10) / 10),
    Math.min(100, Math.round((predictionStats.expectedAccuracy + margin * 100) * 10) / 10)
  ];
  
  // 计算质量评分
  predictionStats.qualityScore = Math.round(
    (sampleConfig.confidenceLevel / 100) * 30 + 
    (1 - sampleConfig.allowableError / 10) * 30 + 
    (sampleStats.sampleRatio / 10) * 40
  );
  
  // 质量评估
  if (predictionStats.qualityScore >= 90) {
    predictionStats.qualityAssessment = '优秀';
    predictionStats.riskLevel = 1;
  } else if (predictionStats.qualityScore >= 80) {
    predictionStats.qualityAssessment = '良好';
    predictionStats.riskLevel = 2;
  } else if (predictionStats.qualityScore >= 70) {
    predictionStats.qualityAssessment = '一般';
    predictionStats.riskLevel = 3;
  } else {
    predictionStats.qualityAssessment = '较差';
    predictionStats.riskLevel = 4;
  }
  
  // 建议措施
  const recommendations = [
    '按计划执行抽样盘点',
    '建议增加抽样数量以提高置信度',
    '建议检查排除条件设置',
    '建议重新设计抽样方案'
  ];
  predictionStats.recommendation = recommendations[predictionStats.riskLevel - 1] || '按计划执行抽样盘点';
};

const getAbcTagType = (abcClass) => {
  const typeMap = {
    'A': 'danger',
    'B': 'warning',
    'C': 'success'
  };
  return typeMap[abcClass] || 'info';
};

const getQualityClass = (score) => {
  if (score >= 90) return 'text-success';
  if (score >= 80) return 'text-warning';
  return 'text-danger';
};

const getRiskTagType = (level) => {
  const typeMap = {
    1: 'success',
    2: 'warning',
    3: 'danger',
    4: 'danger'
  };
  return typeMap[level] || 'info';
};

const getRiskText = (level) => {
  const textMap = {
    1: '低风险',
    2: '中风险',
    3: '高风险',
    4: '极高风险'
  };
  return textMap[level] || '未知';
};

const validateForm = async () => {
  if (!baseFormRef.value) return false;
  
  try {
    await baseFormRef.value.validate();
    
    // 验证抽样配置
    if (sampleConfig.sampleMethod === 1 && (!sampleConfig.sampleCount || sampleConfig.sampleCount < 1)) {
      ElMessage.warning('请输入有效的抽样数量');
      return false;
    }
    
    if (sampleConfig.sampleMethod === 2 && (!sampleConfig.sampleRatio || sampleConfig.sampleRatio < 0.1 || sampleConfig.sampleRatio > 100)) {
      ElMessage.warning('抽样比例必须在0.1%-100%之间');
      return false;
    }
    
    if (sampleConfig.sampleMethod === 3 && sampleConfig.stratumRules.length === 0) {
      ElMessage.warning('请至少配置一个分层规则');
      return false;
    }
    
    if (sampleConfig.sampleMethod === 4 && (!sampleConfig.groupSampleCount || sampleConfig.groupSampleCount < 1)) {
      ElMessage.warning('请输入有效的抽样群组数');
      return false;
    }
    
    if (sampleConfig.sampleMethod === 5 && (!sampleConfig.systemInterval || sampleConfig.systemInterval < 1)) {
      ElMessage.warning('请输入有效的抽样间隔');
      return false;
    }
    
    // 验证抽样样本
    if (samplePreview.value.length === 0) {
      ElMessage.warning('请先生成抽样预览');
      return false;
    }
    
    if (samplePreview.value.length < sampleConfig.minSampleSize) {
      ElMessage.warning(`抽样样本量(${samplePreview.value.length})低于最小样本量要求(${sampleConfig.minSampleSize})`);
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
      sampleConfig: JSON.stringify(sampleConfig),
      takeStrategy: JSON.stringify(takeStrategy),
      takeStrategy: 5, // 随机抽盘
      status: 0, // 草稿状态
      approvalStatus: 0, // 未提交审批
      samplePreview: samplePreview.value,
      sampleStats: sampleStats
    };
    
    const res = await post('/api/auth/stock-take/random/save', saveData);
    
    if (res) {
      formData.id = res.id;
      formData.orderNo = res.orderNo || formData.orderNo;
      ElMessage.success('保存成功');
      
      // 如果是新建保存，更新URL中的ID
      if (route.params.id && route.params.id !== res.id) {
        router.replace(`/index/ckStockTakeRandomEdit/${res.id}`);
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
      `确定要提交随机抽盘单"${formData.takeName}"审批吗？提交后不可修改。`,
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
    
    const res = await post('/api/auth/stock-take/random/submit', submitData);
    
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
  formData.orderNo = `${prefix}R${timestamp}${random}`;
};

// 初始化页面
const initPage = async () => {
  await loadWarehouseList();
  await loadCategoryTree();
  
  // 检查是否是编辑或查看模式
  const id = route.params.id;
  if (id && id !== 'create') {
    await loadStockTakeDetail(id);
  } else {
    // 新建模式，生成单号
    generateOrderNo();
    generateRandomSeed();
  }
};

// 监听仓库变化
watch(() => formData.warehouseId, (newVal) => {
  if (newVal) {
    updateWarehouseStats();
  }
});

onMounted(() => {
  initPage();
});
</script>

<style scoped>
.stock-take-random-container {
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

.stratum-config {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 16px;
  background: white;
}

.stratum-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
}

.stratum-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.stratum-list {
  margin-top: 16px;
}

.stratum-item {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 16px;
  margin-bottom: 12px;
  background: #f8f9fa;
}

.stratum-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
}

.stratum-name {
  font-weight: bold;
  color: #303133;
}

.stratum-fields {
  margin-top: 12px;
}

.condition-config {
  margin-top: 12px;
  padding: 12px;
  background: white;
  border-radius: 4px;
  border: 1px solid #f0f0f0;
}

.condition-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.condition-item:last-child {
  margin-bottom: 0;
}

.range-separator {
  margin: 0 8px;
  color: #c0c4cc;
}

.sample-setting {
  display: flex;
  align-items: center;
  margin-top: 12px;
}

.no-stratum {
  padding: 40px 0;
  text-align: center;
}

.sample-preview {
  margin-top: 16px;
}

.preview-stats {
  margin-bottom: 20px;
}

.preview-content {
  margin-top: 16px;
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

.preview-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
  gap: 12px;
}

.no-preview {
  padding: 40px 0;
  text-align: center;
}

.result-prediction {
  margin-top: 16px;
}

.prediction-charts {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.chart-container {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 16px;
  background: white;
}

.chart-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
}

.chart-placeholder {
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.mock-chart {
  width: 100%;
  text-align: center;
}

.mock-chart-title {
  font-size: 14px;
  font-weight: bold;
  color: #606266;
  margin-bottom: 12px;
}

.mock-chart-content {
  padding: 12px;
}

.mock-bar-chart {
  margin-top: 20px;
}

.bar-group {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.bar-label {
  width: 80px;
  text-align: right;
  margin-right: 12px;
  font-size: 14px;
  color: #606266;
}

.bar-container {
  flex: 1;
  height: 30px;
  background: #f0f0f0;
  border-radius: 4px;
  overflow: hidden;
  position: relative;
}

.bar {
  height: 100%;
  background: linear-gradient(90deg, #409eff, #79bbff);
  transition: width 0.5s ease;
  position: relative;
}

.bar-value {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  color: white;
  font-size: 12px;
  font-weight: bold;
  text-shadow: 1px 1px 1px rgba(0,0,0,0.3);
}

.accuracy-prediction {
  text-align: center;
  padding: 20px;
}

.prediction-value {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 8px;
}

.confidence-interval {
  font-size: 16px;
  color: #606266;
  margin-bottom: 12px;
}

.quality-assessment {
  font-size: 18px;
  font-weight: bold;
  margin-top: 16px;
}

.prediction-summary {
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

.add-product-content {
  min-height: 400px;
}

.product-search {
  margin-bottom: 16px;
}

.product-list {
  margin-top: 16px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .stock-take-random-container {
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
  
  .prediction-charts {
    grid-template-columns: 1fr;
  }
  
  .condition-item {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .range-separator {
    margin: 8px 0;
  }
  
  .sample-setting {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>