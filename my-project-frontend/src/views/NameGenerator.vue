<template>
  <div class="container">
    <el-card class="form-card">
      <template #header>
        <div class="card-header">
          <h2>智能名字生成系统</h2>
        </div>
      </template>

      <el-form ref="formRef" :model="formData" :rules="rules">
        <el-row :gutter="24">
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

          <el-col :md="12" :sm="24">
            <el-form-item label="名字风格" prop="style">
              <el-select
                v-model="formData.style"
                placeholder="选择风格"
                class="input-item"
              >
                <el-option label="传统" value="traditional" />
                <el-option label="洋气" value="modern" />
                <el-option label="诗意" value="poetic" />
                <el-option label="简约" value="simple" />
                <el-option label="霸气" value="powerful" />
                <el-option label="佛系" value="buddha" />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :md="12" :sm="24">
            <el-form-item label="喜欢的字">
              <el-input
                v-model="formData.preferredChars"
                placeholder="请输入你喜欢的字（逗号分隔）"
                clearable
                class="input-item"
              />
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

    <el-card class="result-card" v-if="resultList.length">
      <template #header>
        <div class="result-header">
          <h3>生成结果</h3>
        </div>
      </template>

      <div v-for="(item, index) in resultList" :key="index" class="name-block">
        <div class="main-name">{{ item.name }}</div>
        <el-tag type="success">推荐指数：{{ item.score }}/10</el-tag>
        <div class="name-meaning">{{ item.meaning }}</div>
        <div class="wuxing">
          <strong>五行解析：</strong>
          <div v-for="(val, key) in item.wuxing" :key="key">{{ key }}：{{ val }}</div>
        </div>
        <el-button type="success" size="small" @click="likeName(item)">👍 收藏</el-button>
        <el-divider />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const formData = reactive({
  lastName: '',
  gender: 'male',
  birthDate: '',
  birthHour: '',
  style: '',
  preferredChars: '',
  additionalInfo: ''
})

const rules = reactive({
  lastName: [{ required: true, message: '请输入姓氏', trigger: 'blur' }],
  birthDate: [{ required: true, message: '请选择出生日期', trigger: 'change' }],
  birthHour: [{ required: true, message: '请选择出生时辰', trigger: 'change' }],
  style: [{ required: true, message: '请选择名字风格', trigger: 'change' }]
})

const loading = ref(false)
const resultList = ref([])
const formRef = ref(null)

const hourOptions = Array.from({ length: 24 }, (_, i) => ({
  value: i,
  label: `${i.toString().padStart(2, '0')}:00`
}))

const submitForm = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    loading.value = true
    const response = await axios.post('/api/auth/name/generateName', {
      ...formData,
      birthDate: formData.birthDate ? new Date(formData.birthDate).toISOString() : ''
    })
    resultList.value = response.data || []
  } catch (error) {
    // console.error(error)
    ElMessage.error(error.response?.data?.message || '生成失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const likeName = (nameItem) => {
  ElMessage.success(`已收藏：${nameItem.name}`)
}
</script>

<style scoped>
.container {
  max-width: 800px;
  margin: 0 auto;
  padding: 2rem;
}
.form-card {
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
}
.card-header {
  text-align: center;
  padding: 1.5rem 0;
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
}
.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.name-block {
  margin-bottom: 2rem;
}
.main-name {
  font-size: 2rem;
  font-weight: bold;
}
.name-meaning {
  margin-top: 0.5rem;
  color: #7f8c8d;
}
.wuxing {
  margin-top: 0.5rem;
  color: #34495e;
  font-size: 0.95rem;
}
@media (max-width: 768px) {
  .container {
    padding: 1rem;
  }
}
</style>
