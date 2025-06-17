<template>
  <div class="container">
    <el-card class="form-card">
      <template #header>
        <div class="card-header">
          <h2>🌸 桃花运势分析</h2>
        </div>
      </template>

      <el-form ref="formRef" :model="formData" :rules="rules">
        <el-row :gutter="24">
          <!-- 表单字段 -->
          <el-col :md="12" :sm="24">
            <el-form-item label="姓名" prop="name">
              <el-input
                v-model="formData.name"
                placeholder="请输入您的姓名（可选）"
                clearable
              />
            </el-form-item>
          </el-col>

          <el-col :md="12" :sm="24">
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="formData.gender">
                <el-radio-button label="male">男</el-radio-button>
                <el-radio-button label="female">女</el-radio-button>
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
                style="width: 100%"
              />
            </el-form-item>
          </el-col>

          <el-col :md="12" :sm="24">
            <el-form-item label="出生时辰" prop="birthHour">
              <el-select
                v-model="formData.birthHour"
                placeholder="请选择时辰"
                style="width: 100%"
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
            <el-form-item label="感情状态" prop="relationship">
              <el-select
                v-model="formData.relationship"
                placeholder="请选择当前状态"
              >
                <el-option label="单身" value="single" />
                <el-option label="有目标" value="target" />
                <el-option label="有一群目标" value="targets" />
                <el-option label="恋爱中" value="in_relationship" />
                <el-option label="已婚" value="married" />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="24" v-if="formData.relationship === 'target'">
            <el-card class="target-form">
              <div class="sub-title">💘 目标对象信息</div>
              
              <el-row :gutter="24">
                <el-col :md="12" :sm="24">
                  <el-form-item label="对方姓名" prop="targetName">
                    <el-input
                      v-model="formData.targetName"
                      placeholder="请输入对方姓名"
                      clearable
                    />
                  </el-form-item>
                </el-col>
              
                <el-col :md="12" :sm="24">
                  <el-form-item label="对方性别" prop="targetGender">
                    <el-radio-group v-model="formData.targetGender">
                      <el-radio-button label="male">男</el-radio-button>
                      <el-radio-button label="female">女</el-radio-button>
                    </el-radio-group>
                  </el-form-item>
                </el-col>
              
                <el-col :md="12" :sm="24">
                  <el-form-item label="对方生日" prop="targetBirthDate">
                    <el-date-picker
                      v-model="formData.targetBirthDate"
                      type="date"
                      placeholder="选择日期"
                      value-format="YYYY-MM-DD"
                      style="width: 100%"
                    />
                  </el-form-item>
                </el-col>
              
                <el-col :span="24">
                  <el-form-item label="对方爱好">
                    <el-input
                      v-model="formData.targetHobbies"
                      type="textarea"
                      :rows="2"
                      placeholder="请输入对方的兴趣爱好（例如：阅读、运动）"
                    />
                  </el-form-item>
                </el-col>
              </el-row>
            </el-card>
          </el-col>

          <el-col :span="24">
            <el-form-item label="附加说明">
              <el-input
                v-model="formData.additionalInfo"
                type="textarea"
                :rows="3"
                placeholder="请输入您的问题（例如：我想知道何时脱单）"
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
          {{ loading ? '分析中...' : '立即分析桃花运' }}
        </el-button>
      </el-form>
    </el-card>

    <el-card class="result-card" v-if="analysisResult">
      <template #header>
        <div class="result-header">
          <h3>🔮 分析结果</h3>
        </div>
      </template>
      <div class="result-content">
        <h4 class="result-title">{{ analysisResult.analysis }}</h4>
        <div class="detail-item">
          <span class="label">📌 幸运方位：</span>
          <span class="value">{{ analysisResult.luckyDirection || '等待探索' }}</span>
        </div>
        <div class="detail-item">
          <span class="label">💡 提升建议：</span>
          <ul class="advice-list">
            <li v-for="(item, index) in analysisResult.advice" :key="index" class="advice-item">
              {{ item }}
            </li>
            <span v-if="!analysisResult.advice?.length">暂无特别建议</span>
          </ul>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const formData = reactive({
  name: '',
  gender: '',
  birthDate: '',
  birthHour: '',
  relationship: '',
  additionalInfo: ''
})

const rules = reactive({
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  birthDate: [{ required: true, message: '请选择出生日期', trigger: 'change' }],
  birthHour: [{ required: true, message: '请选择出生时辰', trigger: 'change' }],
  relationship: [{ required: true, message: '请选择感情状态', trigger: 'change' }],
  targetName: [{ required: true, message: '请输入对方姓名', trigger: 'blur' }],
  targetGender: [{ required: true, message: '请选择对方性别', trigger: 'change' }],
  targetBirthDate: [{ required: true, message: '请选择对方生日', trigger: 'change' }]
})

const loading = ref(false)
const analysisResult = ref('')
const formRef = ref(null)

const hourOptions = Array.from({ length: 24 }, (_, i) => ({
  value: i,
  label: `${i.toString().padStart(2, '0')}:00`
}))

const submitForm = async () => {
  try {
    await formRef.value.validate()
    loading.value = true
    
    const response = await axios.post('/api/auth/taohua/kanTaohua', {
      ...formData,
      birthDate: formData.birthDate ? new Date(formData.birthDate).toISOString() : ''
    })

    analysisResult.value = response.data
  } catch (error) {
    console.error(error)
    ElMessage.error(error.response?.data?.message || '分析失败，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.container {
  max-width: 800px;
  margin: 2rem auto;
  padding: 0 1rem;
}

.form-card {
  background: linear-gradient(135deg, #fff5f5 0%, #fff0f6 100%);
  border-radius: 16px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
  margin-bottom: 2rem;
}

.card-header {
  text-align: center;
  padding: 1.5rem 0;
}

.card-header h2 {
  color: #e83e8c;
  font-size: 1.8rem;
}

.submit-btn {
  width: 100%;
  padding: 1rem;
  font-size: 1.1rem;
  background: linear-gradient(45deg, #ff758c, #ff7eb3);
  border: none;
}

.result-card {
  background: linear-gradient(135deg, #fff0f6 0%, #fff5f5 100%);
  border-radius: 16px;
}

.result-content {
  padding: 1.5rem;
  background: #fff8f9;
  border-radius: 12px;

  .result-title {
    color: #e83e8c;
    margin-bottom: 1.2rem;
  }

  .detail-item {
    margin: 1rem 0;
    .label {
      color: #6d4c67;
      font-weight: 500;
    }
    .value {
      color: #9e6b8e;
    }
  }

  .advice-list {
    margin-top: 0.5rem;
    .advice-item {
      padding: 0.5rem 0;
      border-bottom: 1px dashed #f0d6e1;
    }
  }
}

@media (max-width: 768px) {
  .container {
    padding: 0;
  }
  
  .card-header h2 {
    font-size: 1.4rem;
  }
}

.target-form {
  margin-top: 1.5rem;
  background: linear-gradient(135deg, #fff0f9 0%, #ffeef6 100%);
  .sub-title {
    color: #e83e8c;
    font-size: 1.1rem;
    margin-bottom: 1rem;
  }
}
</style>