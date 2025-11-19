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
        <el-button type="primary" @click="handleDownloadTemplate"  :loading="downloadLoading">
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
          :auto-upload="false"
          :multiple="false"
          :file-list="fileList"
          :before-upload="beforeUpload"
          :on-change="handleFileChange"
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
        
        <!-- 手动上传按钮 -->
        <div class="upload-actions" v-if="fileList.length > 0">
          <el-button 
            type="primary" 
            @click="handleManualUpload" 
            :loading="uploading"
            size="large"
          >
            <el-icon><Upload /></el-icon>
            开始导入
          </el-button>
          <el-button @click="handleClear" :disabled="uploading">
            清空文件
          </el-button>
        </div>
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
        @click="handleManualUpload"
        :disabled="fileList.length === 0 || uploading"
        :loading="uploading"
      >
        {{ uploading ? '导入中...' : '开始导入' }}
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { ElMessage } from 'element-plus';
import { UploadFilled, Download, InfoFilled, Upload } from '@element-plus/icons-vue';
import { post } from '@/net';
import axios from 'axios';
import { accessHeader } from '@/net'; 

const emit = defineEmits(['success', 'cancel']);

const uploadRef = ref();
const fileList = ref([]);
const importResult = ref(null);
const uploading = ref(false);
const currentFile = ref(null);
const downloadLoading = ref(false);


// 方法
const handleDownloadTemplate = async () => {
  downloadLoading.value = true;

  try {
    const response = await axios.get('/api/auth/product/exportProductCreateExcel', {
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
    a.download = '产品导入模版.xlsx'; // 可自定义文件名
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

const beforeUpload = (file) => {
  const isExcel = file.type === 'application/vnd.ms-excel' || 
                  file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' ||
                  file.name.endsWith('.xlsx') || 
                  file.name.endsWith('.xls');
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

const handleFileChange = (file) => {
  // 这里 file 参数是 UploadFile 对象，我们需要获取原始文件
  currentFile.value = file.raw;
  // 更新文件列表显示，确保只有一个文件
  fileList.value = [file];
  console.log('文件已选择:', file.name, '文件列表:', fileList.value);
};

const handleManualUpload = async () => {
  if (!currentFile.value) {
    ElMessage.warning('请先选择要导入的文件');
    return;
  }
  
  uploading.value = true;
  
  try {
    const formData = new FormData();
    formData.append('file', currentFile.value);
    
    ElMessage.info('开始导入数据，请稍候...');
    //importProductCreateExcel
    const result = await post('/api/auth/product/importProductCreateExcel', formData, {
    //const result = await post('/api/auth/product/import', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
    
    console.log('导入结果:', result);
    // 处理导入结果
    if (result && result.code ===200) {
      importResult.value = result;
      ElMessage.success(`导入成功`);
    } else {
      ElMessage.error(result?.message || '导入失败，请检查数据格式');
    }
  } catch (error) {
    
  } finally {
    uploading.value = false;
  }
};

const handleRemove = (file) => {
  fileList.value = fileList.value.filter(item => item.uid !== file.uid);
  if (fileList.value.length === 0) {
    currentFile.value = null;
  }
  importResult.value = null;
};

const handleClear = () => {
  fileList.value = [];
  currentFile.value = null;
  importResult.value = null;
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

.upload-actions {
  margin-top: 16px;
  text-align: center;
  display: flex;
  justify-content: center;
  gap: 12px;
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

/* 响应式设计 */
@media (max-width: 768px) {
  .upload-actions {
    flex-direction: column;
  }
  
  .upload-actions .el-button {
    width: 100%;
  }
}
</style>