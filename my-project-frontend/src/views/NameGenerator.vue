<template>
  <div class="container">
    <el-card class="form-card">
      <template #header>
        <div class="card-header">
          <h2>生成系统</h2>
        </div>
      </template>

      <el-form ref="formRef" :model="formData" :rules="rules">
        <el-row :gutter="24">
          <!-- 表单字段 -->
          <el-col :md="12" :sm="24">
            <el-form-item label="姓氏" prop="lastName">
              <el-input
                v-model="formData.lastName"
                placeholder="请输入家族姓氏"
                clearable
                class="input-item"
              />
            </el-form-item>
          </el-col>

          <el-col :md="12" :sm="24">
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="formData.gender">
                <el-radio-button label="male">男性</el-radio-button>
                <el-radio-button label="female">女性</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </el-col>

          <el-col :md="12" :sm="24">
            <el-form-item label="出生日期" prop="birthDate">
              <el-date-picker
                v-model="formData.birthDate"
                type="date"
                placeholder="选择日期"
                value-format="YYYY-MM-DD"
                class="input-item"
              />
            </el-form-item>
          </el-col>

          <el-col :md="12" :sm="24">
            <el-form-item label="出生时辰" prop="birthHour">
              <el-select
                v-model="formData.birthHour"
                placeholder="请选择时辰"
                class="input-item"
              >
                <el-option
                  v-for="item in hourOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="附加信息">
              <el-input
                v-model="formData.additionalInfo"
                type="textarea"
                :rows="3"
                placeholder="请输入其他要求（可选）"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-button
          type="primary"
          :loading="loading"
          @click="submitForm"
          class="submit-btn"
        >
          {{ loading ? '生成中...' : '立即生成名字' }}
        </el-button>
      </el-form>
    </el-card>

    <el-card class="result-card" v-if="resultData">
      <template #header>
        <div class="result-header">
          <h3>生成结果</h3>
          <el-tag type="success">推荐指数：{{ resultData.score }}/10</el-tag>
        </div>
      </template>

      <div class="name-result">
        <div class="main-name">{{ resultData.name }}</div>
        <div class="name-meaning">{{ resultData.meaning }}</div>
      </div>

      <el-divider />

      <div class="analysis-box">
        <h4>命名解析</h4>
        <p class="analysis-text">{{ resultData.analysis }}</p>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import axios from "axios";


const formData = reactive({
  lastName: '',
  gender: 'male',
  birthDate: '',
  birthHour: '',
  additionalInfo: ''
})

const rules = reactive({
  lastName: [{ required: true, message: '请输入姓氏', trigger: 'blur' }],
  birthDate: [{ required: true, message: '请选择出生日期', trigger: 'change' }],
  birthHour: [{ required: true, message: '请选择出生时辰', trigger: 'change' }]
})

const loading = ref(false)
const resultData = ref(null)
const formRef = ref(null)

const hourOptions = Array.from({ length: 24 }, (_, i) => ({
  value: i,
  label: `${i.toString().padStart(2, '0')}:00`
}))

const submitForm = async () => {
  console.log("立即生成名字方法调用", formRef.value)
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    loading.value = true

    const response = await axios.post('/api/auth/name/generateName', {
      ...formData,
      birthDate: formData.birthDate ? new Date(formData.birthDate).toISOString() : ''
    })

    resultData.value = response.data
  } catch (error) {
    console.log("失败",error)
    ElMessage.error(error.response?.data?.message || '生成失败，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.container {
  max-width: 800px; /* 调整最大宽度 */
  margin: 0 auto;
  padding: 2rem;
}

.form-card {
  width: 100%; /* 表单占据全部宽度 */
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
}

.card-header {
  text-align: center;
  padding: 1.5rem 0;
}

.card-header h2 {
  color: #2c3e50;
  margin-bottom: 0.5rem;
}

.subtitle {
  color: #7f8c8d;
  font-size: 0.9rem;
}

.input-item {
  width: 100%;
}

.submit-btn {
  width: 100%;
  padding: 1rem;
  font-size: 1.1rem;
  margin-top: 1.5rem;
}

.result-card {
  margin-top: 2rem;
  border-color: #e9ecef;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.name-result {
  text-align: center;
  padding: 2rem 0;
}

.main-name {
  font-size: 2.5rem;
  color: #2c3e50;
  font-weight: 600;
  letter-spacing: 2px;
}

.name-meaning {
  color: #7f8c8d;
  margin-top: 1rem;
  font-size: 1.1rem;
}

.analysis-box {
  background: #f8f9fa;
  padding: 1.5rem;
  border-radius: 8px;
}

.analysis-text {
  color: #495057;
  line-height: 1.8;
  white-space: pre-wrap;
}

@media (max-width: 768px) {
  .container {
    padding: 1rem;
  }

  .main-name {
    font-size: 1.8rem;
  }
}
</style>