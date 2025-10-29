<template>
  <div class="approval-form">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
    >
      <!-- 审批信息 -->
      <el-card class="info-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span>审批信息</span>
          </div>
        </template>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="审批单号">
              <span class="readonly-text">{{ taskData.bizNo }}</span>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="业务类型">
              <span class="readonly-text">{{ getBizTypeText(taskData.bizType) }}</span>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="申请人">
              <span class="readonly-text">{{ taskData.applicantName }}</span>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="申请时间">
              <span class="readonly-text">{{ formatTime(taskData.createdAt) }}</span>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="当前节点">
              <span class="readonly-text">{{ taskData.currentNodeName }}</span>
            </el-form-item>
          </el-col>
        </el-row>
      </el-card>

      <!-- 审批操作 -->
      <el-card class="action-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span>审批操作</span>
          </div>
        </template>
        <el-form-item label="审批结果" prop="approvalResult">
          <el-radio-group v-model="formData.approvalResult">
            <el-radio :label="1">通过</el-radio>
            <el-radio :label="2">拒绝</el-radio>
            <el-radio :label="3">退回</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="审批意见" prop="opinion">
          <el-input
            v-model="formData.opinion"
            type="textarea"
            :rows="4"
            placeholder="请输入审批意见"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="下一步处理" v-if="formData.approvalResult === 3">
          <el-radio-group v-model="formData.nextAction">
            <el-radio :label="1">退回申请人</el-radio>
            <el-radio :label="2">退回上一节点</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-card>

      <!-- 操作按钮 -->
      <div class="form-actions">
        <el-button @click="$emit('cancel')">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="loading">
          提交审批
        </el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { ElMessage } from 'element-plus';
import { post } from '@/net';

const props = defineProps({
  taskData: {
    type: Object,
    required: true
  }
});

const emit = defineEmits(['success', 'cancel']);

const formRef = ref();
const loading = ref(false);

const formData = reactive({
  approvalResult: 1,
  opinion: '',
  nextAction: 1
});

const formRules = {
  approvalResult: [
    { required: true, message: '请选择审批结果', trigger: 'change' }
  ],
  opinion: [
    { required: true, message: '请输入审批意见', trigger: 'blur' }
  ]
};

const getBizTypeText = (bizType) => {
  const types = {
    1: '入库单',
    2: '出库单',
    3: '调拨单',
    4: '盘点单'
  };
  return types[bizType] || '未知';
};

const formatTime = (timeString) => {
  if (!timeString) return '';
  const date = new Date(timeString);
  return `${date.getFullYear()}-${padZero(date.getMonth() + 1)}-${padZero(date.getDate())} ${padZero(date.getHours())}:${padZero(date.getMinutes())}`;
};

const padZero = (num) => {
  return num < 10 ? `0${num}` : num;
};

const handleSubmit = async () => {
  if (!formRef.value) return;
  
  try {
    await formRef.value.validate();
    
    loading.value = true;
    const submitData = {
      taskId: props.taskData.id,
      instanceId: props.taskData.instanceId,
      ...formData
    };
    
    const res = await post('/api/auth/approval/approve', submitData);
    if (res) {
      ElMessage.success('审批提交成功');
      emit('success');
    }
  } catch (error) {
    if (error instanceof Error) {
      ElMessage.error('表单验证失败');
    } else {
      ElMessage.error('审批提交失败');
    }
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.approval-form {
  max-height: 70vh;
  overflow-y: auto;
}

.info-card, .action-card {
  margin-bottom: 20px;
}

.card-header {
  font-weight: bold;
  color: #303133;
}

.readonly-text {
  color: #606266;
  font-weight: 500;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}
</style>