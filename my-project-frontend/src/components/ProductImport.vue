<template>
  <div class="product-import">
    <el-alert
      title="批量导入产品"
      type="info"
      description="支持Excel格式文件导入，请下载模板文件并按格式填写数据"
      :closable="false"
      class="alert-message"
    />

    <!-- 下载模板 -->
    <el-card class="template-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>下载模板</span>
        </div>
      </template>
      <div class="template-content">
        <p>请下载产品导入模板，按照模板格式填写数据后上传</p>
        <el-button type="primary" @click="handleDownloadTemplate">
          <el-icon><Download /></el-icon>
          下载导入模板
        </el-button>
      </div>
    </el-card>

    <!-- 文件上传 -->
    <el-card class="upload-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>上传文件</span>
        </div>
      </template>
      <div class="upload-content">
        <el-upload
          ref="uploadRef"
          class="upload-demo"
          drag
          action="/api/auth/product/import"
          :multiple="false"
          :file-list="fileList"
          :before-upload="beforeUpload"
          :on-success="handleSuccess"
          :on-error="handleError"
          :on-remove="handleRemove"
          accept=".xlsx,.xls"
        >
          <el-icon class="el-icon--upload"><upload-filled /></el-icon>
          <div class="el-upload__text">
            将文件拖到此处，或<em>点击上传</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">
              只能上传 xlsx/xls 文件，且不超过10MB
            </div>
          </template>
        </el-upload>
      </div>
    </el-card>

    <!-- 导入说明 -->
    <el-card class="instruction-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>导入说明</span>
        </div>
      </template>
      <div class="instruction-content">
        <el-alert
          title="重要提示"
          type="warning"
          :closable="false"
          class="warning-alert"
        />
        <div class="instruction-list">
          <div class="instruction-item">
            <el-icon><InfoFilled /></el-icon>
            <span>SKU编码必须唯一，不能与现有产品重复</span>
          </div>
          <div class="instruction-item">
            <el-icon><InfoFilled /></el-icon>
            <span>产品名称、规格型号、基础单位为必填项</span>
          </div>
          <div class="instruction-item">
            <el-icon><InfoFilled /></el-icon>
            <span>分类编码必须存在于系统中</span>
          </div>
          <div class="instruction-item">
            <el-icon><InfoFilled /></el-icon>
            <span>单位编码必须存在于系统中</span>
          </div>
          <div class="instruction-item">
            <el-icon><InfoFilled /></el-icon>
            <span>状态字段：1-启用，0-禁用</span>
          </div>
          <div class="instruction-item">
            <el-icon><InfoFilled /></el-icon>
            <span>数值字段请填写数字，不要包含单位</span>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 导入结果 -->
    <el-card class="result-card" shadow="never" v-if="importResult">
      <template #header>
        <div class="card-header">
          <span>导入结果</span>
          <el-tag :type="importResult.success ? 'success' : 'error'">
            {{ importResult.success ? '导入成功' : '导入失败' }}
          </el-tag>
        </div>
      </template>
      <div class="result-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="总记录数">
            {{ importResult.total || 0 }}
          </el-descriptions-item>
          <el-descriptions-item label="成功数量">
            <span class="success-count">{{ importResult.successCount || 0 }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="失败数量">
            <span class="error-count">{{ importResult.errorCount || 0 }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="导入时间">
            {{ formatTime(importResult.importTime) }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- 错误详情 -->
        <div class="error-details" v-if="importResult.errors && importResult.errors.length > 0">
          <h4>错误详情：</h4>
          <el-table
            :data="importResult.errors"
            size="small"
            border
            class="error-table"
          >
            <el-table-column prop="row" label="行号" width="80" align="center" />
            <el-table-column prop="sku" label="SKU编码" width="120" />
            <el-table-column prop="message" label="错误信息" min-width="200" />
          </el-table>
        </div>
      </div>
    </el-card>

    <!-- 操作按钮 -->
    <div class="action-section">
      <el-button @click="$emit('cancel')">取消</el-button>
      <el-button 
        type="primary" 
        @click="handleConfirm"
        :disabled="!fileList.length"
      >
        开始导入
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { ElMessage } from 'element-plus';
import { UploadFilled, Download, InfoFilled } from '@element-plus/icons-vue';

const emit = defineEmits(['success', 'cancel']);

const uploadRef = ref();
const fileList = ref([]);
const importResult = ref(null);
const loading = ref(false);

// 方法
const handleDownloadTemplate = () => {
  ElMessage.info('模板下载功能开发中');
  // 实际项目中这里应该调用下载接口
  // window.open('/api/auth/product/import/template', '_blank');
};

const beforeUpload = (file) => {
  const isExcel = file.type === 'application/vnd.ms-excel' || 
                  file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet';
  const isLt10M = file.size / 1024 / 1024 < 10;

  if (!isExcel) {
    ElMessage.error('只能上传Excel文件!');
    return false;
  }
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过10MB!');
    return false;
  }
  return true;
};

const handleSuccess = (response, file) => {
  loading.value = false;
  importResult.value = response;
  
  if (response.success) {
    ElMessage.success(`导入成功！成功${response.successCount}条，失败${response.errorCount}条`);
    emit('success');
  } else {
    ElMessage.error('导入失败，请检查数据格式');
  }
};

const handleError = (error, file) => {
  loading.value = false;
  ElMessage.error('文件上传失败');
  console.error('上传失败:', error);
};

const handleRemove = (file) => {
  fileList.value = fileList.value.filter(item => item.uid !== file.uid);
  importResult.value = null;
};

const handleConfirm = () => {
  if (fileList.value.length === 0) {
    ElMessage.warning('请先选择要导入的文件');
    return;
  }
  
  loading.value = true;
  ElMessage.info('开始导入数据...');
  
  // 在实际项目中，这里会触发上传组件的提交
  // uploadRef.value.submit();
  
  // 模拟导入成功
  setTimeout(() => {
    loading.value = false;
    importResult.value = {
      success: true,
      total: 25,
      successCount: 23,
      errorCount: 2,
      importTime: new Date().toISOString(),
      errors: [
        { row: 3, sku: 'SKU003', message: 'SKU编码已存在' },
        { row: 15, sku: 'SKU015', message: '分类编码不存在' }
      ]
    };
    ElMessage.success(`导入成功！成功23条，失败2条`);
    emit('success');
  }, 2000);
};

const formatTime = (timeString) => {
  if (!timeString) return '--';
  try {
    const date = new Date(timeString);
    return `${date.getFullYear()}-${padZero(date.getMonth() + 1)}-${padZero(date.getDate())} ${padZero(date.getHours())}:${padZero(date.getMinutes())}`;
  } catch {
    return '--';
  }
};

const padZero = (num) => {
  return num < 10 ? `0${num}` : num;
};
</script>

<style scoped>
.product-import {
  max-height: 80vh;
  overflow-y: auto;
  padding-right: 10px;
}

.alert-message {
  margin-bottom: 20px;
}

.template-card, .upload-card, .instruction-card, .result-card {
  margin-bottom: 20px;
}

.card-header {
  font-weight: bold;
  color: #303133;
}

.template-content {
  text-align: center;
  padding: 20px;
}

.template-content p {
  margin-bottom: 16px;
  color: #606266;
}

.upload-content {
  padding: 20px;
}

.instruction-content {
  padding: 0 20px 20px;
}

.warning-alert {
  margin-bottom: 16px;
}

.instruction-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.instruction-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #606266;
}

.instruction-item .el-icon {
  color: #E6A23C;
}

.result-content {
  padding: 0 20px;
}

.success-count {
  color: #67C23A;
  font-weight: bold;
}

.error-count {
  color: #F56C6C;
  font-weight: bold;
}

.error-details {
  margin-top: 20px;
}

.error-details h4 {
  margin-bottom: 12px;
  color: #303133;
}

.error-table {
  margin-top: 12px;
}

.action-section {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

:deep(.el-upload-dragger) {
  width: 100%;
}

:deep(.el-descriptions) {
  margin-top: 0;
}
</style>