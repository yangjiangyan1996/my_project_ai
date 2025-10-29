<template>
  <div class="warehouse-form">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
    >
      <el-form-item label="仓库编码" prop="code">
        <el-input
          v-model="formData.code"
          placeholder="请输入仓库编码"
          :disabled="isEdit"
        />
      </el-form-item>

      <el-form-item label="仓库名称" prop="name">
        <el-input
          v-model="formData.name"
          placeholder="请输入仓库名称"
          maxlength="50"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="仓库类型" prop="type">
        <el-select
          v-model="formData.type"
          placeholder="请选择仓库类型"
          style="width: 100%"
        >
          <el-option
            v-for="item in typeOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="仓库地址" prop="address">
        <el-input
          v-model="formData.address"
          type="textarea"
          :rows="2"
          placeholder="请输入仓库地址"
          maxlength="200"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="负责人" prop="managerId">
        <el-select
          v-model="formData.managerId"
          placeholder="请选择负责人"
          style="width: 100%"
          filterable
          clearable
        >
          <el-option
            v-for="user in userList"
            :key="user.id"
            :label="user.username"
            :value="user.id"
          />
        </el-select>
      </el-form-item>

      <!-- <el-form-item label="联系电话">
        <el-input
          v-model="formData.contactPhone"
          placeholder="请输入联系电话"
          maxlength="20"
        />
      </el-form-item> -->

      <el-form-item label="仓库面积">
        <el-input-number
          v-model="formData.area"
          :min="0"
          :precision="2"
          controls-position="right"
          style="width: 100%"
        >
          <template #append>㎡</template>
        </el-input-number>
      </el-form-item>

      <el-form-item label="存储容量">
        <el-input-number
          v-model="formData.capacity"
          :min="0"
          controls-position="right"
          style="width: 100%"
        >
          <template #append>立方米</template>
        </el-input-number>
      </el-form-item>

      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="formData.status">
          <el-radio :label="1">启用</el-radio>
          <el-radio :label="0">停用</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="备注">
        <el-input
          v-model="formData.remark"
          type="textarea"
          :rows="3"
          placeholder="请输入备注信息"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>

      <div class="form-actions">
        <el-button @click="$emit('cancel')">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="loading">
          {{ isEdit ? '更新' : '创建' }}
        </el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { post, get } from '@/net';

const props = defineProps({
  formData: {
    type: Object,
    required: true
  },
  isEdit: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['success', 'cancel']);

const formRef = ref();
const loading = ref(false);
const userList = ref([]);

const typeOptions = [
  { value: 1, label: '普通仓库' },
  { value: 2, label: '冷链仓库' },
  { value: 3, label: '危险品仓库' },
  { value: 4, label: '保税仓库' },
  { value: 5, label: '立体仓库' }
];

const formRules = {
  code: [
    { required: true, message: '请输入仓库编码', trigger: 'blur' },
    { pattern: /^[A-Za-z0-9_-]+$/, message: '仓库编码只能包含字母、数字、下划线和横线', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入仓库名称', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择仓库类型', trigger: 'change' }
  ],
  address: [
    { required: true, message: '请输入仓库地址', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
};

const loadUserList = async () => {
  try {
    const res = await post('/api/auth/user/searchUser');
    console.log('用户列表:', res);
    userList.value = res || [];
  } catch (error) {
    console.error('加载用户列表失败:', error);
    userList.value = [];
  }
};

const handleSubmit = async () => {
  if (!formRef.value) return;
  
  try {
    await formRef.value.validate();
    
    loading.value = true;
    const url = props.isEdit ? '/api/auth/warehouse/update' : '/api/auth/warehouse/create';
    const res = await post(url, props.formData);
    
    if (res) {
      ElMessage.success(props.isEdit ? '更新仓库成功' : '创建仓库成功');
      emit('success');
    }
  } catch (error) {
    if (error instanceof Error) {
      ElMessage.error('表单验证失败');
    } else {
      ElMessage.error(props.isEdit ? '更新仓库失败' : '创建仓库失败');
    }
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  loadUserList();
});
</script>

<style scoped>
.warehouse-form {
  max-height: 70vh;
  overflow-y: auto;
  padding-right: 10px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

:deep(.el-input-number) {
  width: 100%;
}
</style>