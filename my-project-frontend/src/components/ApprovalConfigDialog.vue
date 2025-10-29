<template>
  <!-- 新增配置按钮 -->
  <el-button type="primary" @click="handleAddConfig">
    <el-icon><Plus /></el-icon>
    新增配置
  </el-button>

  <!-- 新增/编辑流程配置对话框 -->
  <el-dialog
    v-model="configDialogVisible"
    :title="configDialogTitle"
    width="800px"
    :before-close="handleConfigDialogClose"
  >
    <el-form
      ref="configFormRef"
      :model="configForm"
      :rules="configFormRules"
      label-width="120px"
      label-position="right"
    >
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="业务类型" prop="bizType">
            <el-select
              v-model="configForm.bizType"
              placeholder="请选择业务类型"
              style="width: 100%"
            >
              <el-option label="入库单" value="1" />
              <el-option label="出库单" value="2" />
              <el-option label="调拨单" value="3" />
              <el-option label="盘点单" value="4" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="流程名称" prop="processName">
            <el-input
              v-model="configForm.processName"
              placeholder="请输入流程名称"
              maxlength="50"
              show-word-limit
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="流程描述" prop="description">
        <el-input
          v-model="configForm.description"
          type="textarea"
          :rows="3"
          placeholder="请输入流程描述"
          maxlength="200"
          show-word-limit
        />
      </el-form-item>

      <!-- 审批节点配置 -->
      <el-form-item label="审批节点">
        <div class="node-config-container">
          <div class="node-list">
            <div
              v-for="(node, index) in configForm.nodes"
              :key="index"
              class="node-item"
              :class="{ 'active-node': activeNodeIndex === index }"
              @click="activeNodeIndex = index"
            >
              <div class="node-header">
                <span class="node-title">节点 {{ index + 1 }}</span>
                <div class="node-actions">
                  <el-button
                    type="danger"
                    link
                    size="small"
                    @click.stop="removeNode(index)"
                    :disabled="configForm.nodes.length <= 1"
                  >
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </div>
              </div>
              <div class="node-content">
                <div class="node-info">
                  <span class="node-type">{{ getNodeTypeText(node.nodeType) }}</span>
                  <span class="node-approver" v-if="node.nodeType === 1">
                    {{ node.approverName || '未选择' }}
                  </span>
                </div>
              </div>
            </div>
          </div>

          <div class="node-actions-bar">
            <el-button type="primary" link @click="addNode">
              <el-icon><Plus /></el-icon>
              添加节点
            </el-button>
            <el-button type="info" link @click="clearAllNodes">
              <el-icon><Delete /></el-icon>
              清空节点
            </el-button>
          </div>

          <!-- 节点配置表单 -->
          <div class="node-config-form" v-if="activeNodeIndex !== -1">
            <el-divider>节点配置</el-divider>
            <el-form
              :model="configForm.nodes[activeNodeIndex]"
              label-width="100px"
            >
              <el-form-item label="节点类型">
                <el-radio-group v-model="configForm.nodes[activeNodeIndex].nodeType">
                  <el-radio :label="1">指定人员</el-radio>
                  <el-radio :label="2">角色审批</el-radio>
                  <el-radio :label="3">部门负责人</el-radio>
                </el-radio-group>
              </el-form-item>

              <el-form-item
                label="审批人"
                v-if="configForm.nodes[activeNodeIndex].nodeType === 1"
              >
                <el-select
                  v-model="configForm.nodes[activeNodeIndex].approverId"
                  placeholder="请选择审批人"
                  style="width: 100%"
                  filterable
                  @change="handleApproverChange(activeNodeIndex, $event)"
                >
                  <el-option
                    v-for="user in userList"
                    :key="user.id"
                    :label="user.realName"
                    :value="user.id"
                  >
                    <span>{{ user.realName }}</span>
                    <span style="float: right; color: #8492a6; font-size: 13px">
                      {{ user.department }}
                    </span>
                  </el-option>
                </el-select>
              </el-form-item>

              <el-form-item
                label="审批角色"
                v-if="configForm.nodes[activeNodeIndex].nodeType === 2"
              >
                <el-select
                  v-model="configForm.nodes[activeNodeIndex].roleId"
                  placeholder="请选择审批角色"
                  style="width: 100%"
                >
                  <el-option label="部门经理" value="1" />
                  <el-option label="仓库主管" value="2" />
                  <el-option label="财务审核" value="3" />
                  <el-option label="系统管理员" value="4" />
                </el-select>
              </el-form-item>

              <el-form-item label="节点条件">
                <el-checkbox-group v-model="configForm.nodes[activeNodeIndex].conditions">
                  <el-checkbox label="金额大于10000元">金额大于10000元</el-checkbox>
                  <el-checkbox label="特殊商品类型">特殊商品类型</el-checkbox>
                  <el-checkbox label="紧急订单">紧急订单</el-checkbox>
                </el-checkbox-group>
              </el-form-item>

              <el-form-item label="审批方式">
                <el-radio-group v-model="configForm.nodes[activeNodeIndex].approvalMethod">
                  <el-radio :label="1">任意一人审批</el-radio>
                  <el-radio :label="2">全部人员审批</el-radio>
                </el-radio-group>
              </el-form-item>

              <el-form-item label="是否必过">
                <el-switch
                  v-model="configForm.nodes[activeNodeIndex].isRequired"
                  :active-value="1"
                  :inactive-value="0"
                />
                <span style="margin-left: 8px; color: #909399; font-size: 12px">
                  {{ configForm.nodes[activeNodeIndex].isRequired ? '必须通过' : '可跳过' }}
                </span>
              </el-form-item>
            </el-form>
          </div>
        </div>
      </el-form-item>

      <el-form-item label="状态">
        <el-switch
          v-model="configForm.status"
          :active-value="1"
          :inactive-value="0"
          active-text="启用"
          inactive-text="禁用"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleConfigDialogClose">取消</el-button>
      <el-button type="primary" @click="handleSubmitConfig" :loading="submitLoading">
        {{ isEditMode ? '更新' : '创建' }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, nextTick } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Delete } from '@element-plus/icons-vue';
import { post, get } from '@/net';

// 响应式数据
const configDialogVisible = ref(false);
const configFormRef = ref();
const submitLoading = ref(false);
const activeNodeIndex = ref(-1);
const isEditMode = ref(false);
const editingConfigId = ref(null);

// 用户列表
const userList = ref([
  { id: 1, realName: '张三', department: '仓储部' },
  { id: 2, realName: '李四', department: '财务部' },
  { id: 3, realName: '王五', department: '采购部' },
  { id: 4, realName: '赵六', department: '销售部' }
]);

// 表单数据
const configForm = reactive({
  bizType: '',
  processName: '',
  description: '',
  status: 1,
  nodes: [
    {
      nodeType: 1,
      approverId: '',
      approverName: '',
      roleId: '',
      conditions: [],
      approvalMethod: 1,
      isRequired: 1,
      sortOrder: 1
    }
  ]
});

// 表单验证规则
const configFormRules = {
  bizType: [
    { required: true, message: '请选择业务类型', trigger: 'change' }
  ],
  processName: [
    { required: true, message: '请输入流程名称', trigger: 'blur' },
    { min: 2, max: 50, message: '流程名称长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入流程描述', trigger: 'blur' }
  ]
};

// 计算属性
const configDialogTitle = computed(() => {
  return isEditMode.value ? '编辑流程配置' : '新增流程配置';
});

// 方法
const handleAddConfig = () => {
  resetConfigForm();
  isEditMode.value = false;
  configDialogVisible.value = true;
  activeNodeIndex.value = 0;
};

const handleEditConfig = (row) => {
  resetConfigForm();
  isEditMode.value = true;
  editingConfigId.value = row.id;
  
  // 填充表单数据（模拟数据）
  Object.assign(configForm, {
    bizType: row.bizType,
    processName: row.processName,
    description: row.description || '',
    status: row.status,
    nodes: row.nodes || [
      {
        nodeType: 1,
        approverId: 1,
        approverName: '张三',
        roleId: '',
        conditions: ['金额大于10000元'],
        approvalMethod: 1,
        isRequired: 1,
        sortOrder: 1
      }
    ]
  });
  
  configDialogVisible.value = true;
  activeNodeIndex.value = 0;
};

const resetConfigForm = () => {
  Object.assign(configForm, {
    bizType: '',
    processName: '',
    description: '',
    status: 1,
    nodes: [
      {
        nodeType: 1,
        approverId: '',
        approverName: '',
        roleId: '',
        conditions: [],
        approvalMethod: 1,
        isRequired: 1,
        sortOrder: 1
      }
    ]
  });
  activeNodeIndex.value = 0;
  
  if (configFormRef.value) {
    configFormRef.value.clearValidate();
  }
};

const handleConfigDialogClose = () => {
  ElMessageBox.confirm('确定要关闭吗？未保存的更改将会丢失。', '提示', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(() => {
    configDialogVisible.value = false;
  }).catch(() => {
    // 用户取消关闭
  });
};

const addNode = () => {
  const newNode = {
    nodeType: 1,
    approverId: '',
    approverName: '',
    roleId: '',
    conditions: [],
    approvalMethod: 1,
    isRequired: 1,
    sortOrder: configForm.nodes.length + 1
  };
  configForm.nodes.push(newNode);
  activeNodeIndex.value = configForm.nodes.length - 1;
};

const removeNode = (index) => {
  if (configForm.nodes.length <= 1) {
    ElMessage.warning('至少需要保留一个审批节点');
    return;
  }
  
  configForm.nodes.splice(index, 1);
  
  // 更新排序
  configForm.nodes.forEach((node, idx) => {
    node.sortOrder = idx + 1;
  });
  
  if (activeNodeIndex.value >= index) {
    activeNodeIndex.value = Math.max(0, activeNodeIndex.value - 1);
  }
};

const clearAllNodes = () => {
  if (configForm.nodes.length > 0) {
    ElMessageBox.confirm('确定要清空所有节点吗？', '清空确认', {
      type: 'warning'
    }).then(() => {
      configForm.nodes = [
        {
          nodeType: 1,
          approverId: '',
          approverName: '',
          roleId: '',
          conditions: [],
          approvalMethod: 1,
          isRequired: 1,
          sortOrder: 1
        }
      ];
      activeNodeIndex.value = 0;
    });
  }
};

const handleApproverChange = (index, userId) => {
  const user = userList.value.find(u => u.id === userId);
  if (user) {
    configForm.nodes[index].approverName = user.realName;
  }
};

const getNodeTypeText = (nodeType) => {
  const types = {
    1: '指定人员',
    2: '角色审批',
    3: '部门负责人'
  };
  return types[nodeType] || '未知类型';
};

const validateNodes = () => {
  for (let i = 0; i < configForm.nodes.length; i++) {
    const node = configForm.nodes[i];
    
    if (node.nodeType === 1 && !node.approverId) {
      ElMessage.warning(`请为节点 ${i + 1} 选择审批人`);
      activeNodeIndex.value = i;
      return false;
    }
    
    if (node.nodeType === 2 && !node.roleId) {
      ElMessage.warning(`请为节点 ${i + 1} 选择审批角色`);
      activeNodeIndex.value = i;
      return false;
    }
  }
  return true;
};

const handleSubmitConfig = async () => {
  // 表单验证
  if (!configFormRef.value) return;
  
  const valid = await configFormRef.value.validate().catch(() => false);
  if (!valid) return;
  
  // 节点验证
  if (!validateNodes()) return;
  
  submitLoading.value = true;
  
  try {
    const submitData = {
      ...configForm,
      nodeCount: configForm.nodes.length
    };
    
    if (isEditMode.value) {
      submitData.id = editingConfigId.value;
      await post('/api/auth/approval/updateConfig', submitData);
      ElMessage.success('流程配置更新成功');
    } else {
      await post('/api/auth/approval/createConfig', submitData);
      ElMessage.success('流程配置创建成功');
    }
    
    configDialogVisible.value = false;
    
    // 触发父组件刷新
    emit('config-updated');
    
  } catch (error) {
    console.error('保存流程配置失败:', error);
    ElMessage.error('保存失败');
  } finally {
    submitLoading.value = false;
  }
};

// 暴露方法给父组件
defineExpose({
     handleAddConfig,  // 确保这行存在
  handleEditConfig
});

// 定义事件
const emit = defineEmits(['config-updated']);
</script>

<style scoped>
.node-config-container {
  border: 1px solid #e8e8e8;
  border-radius: 6px;
  padding: 16px;
  background-color: #fafafa;
}

.node-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
}

.node-item {
  width: 180px;
  border: 2px solid #e8e8e8;
  border-radius: 6px;
  padding: 12px;
  background-color: #fff;
  cursor: pointer;
  transition: all 0.3s;
}

.node-item:hover {
  border-color: #409EFF;
}

.node-item.active-node {
  border-color: #409EFF;
  background-color: #f0f7ff;
}

.node-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.node-title {
  font-weight: bold;
  color: #303133;
}

.node-actions .el-button {
  padding: 0;
  height: auto;
}

.node-content {
  font-size: 12px;
}

.node-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.node-type {
  color: #409EFF;
  font-weight: 500;
}

.node-approver {
  color: #67C23A;
}

.node-actions-bar {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e8e8e8;
}

.node-config-form {
  background-color: #fff;
  padding: 16px;
  border-radius: 6px;
  border: 1px solid #e8e8e8;
}

:deep(.el-divider) {
  margin: 16px 0;
}

:deep(.el-form-item) {
  margin-bottom: 16px;
}

:deep(.el-checkbox-group) {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
</style>