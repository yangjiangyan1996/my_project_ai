<template>
  <div style="text-align: center;margin: 0 20px">
    <div style="margin-top: 100px">
      <div style="font-size: 25px;font-weight: bold">登录</div>
      <div style="font-size: 14px;color: grey;margin-top: 8px">在进入系统之前请先输入手机号和密码进行登录</div>
    </div>
    <div style="margin-top: 50px">
      <el-form :model="form" :rules="rules" @validate="onValidate" ref="formRef">
        <el-form-item prop="phone">
          <el-input v-model="form.phone" maxlength="11" type="tel" placeholder="手机号码" size="large">
            <template #prefix>
              <el-icon>
                <Iphone/>
              </el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="code">
          <el-row :gutter="10" style="width: 100%">
            <el-col>
              <el-input v-model="form.code" :maxlength="20" type="password" placeholder="请输入密码" size="large">
                <template #prefix>
                  <el-icon><EditPen /></el-icon>
                </template>
              </el-input>
            </el-col>
          </el-row>
        </el-form-item>
        
        <!-- 新增：记住我和忘记密码选项 -->
        <el-form-item style="margin-top: -10px; margin-bottom: 10px">
          <div style="display: flex; justify-content: space-between; align-items: center; width: 100%; padding: 0 5px">
            <el-checkbox v-model="form.remember" label="记住我" size="large" />
            <el-link 
              type="primary" 
              :underline="false" 
              @click="router.push('/welcome/forget')" 
              style="font-size: 14px"
            >
              忘记密码？
            </el-link>
          </div>
        </el-form-item>
      </el-form>
    </div>
    <div style="margin-top: 40px">
      <el-button @click="userLogin()" style="width: 100%;height: 48px;font-size: 16px" type="primary" round>立即登录</el-button>
    </div>
    <el-divider style="margin: 40px 0">
      <span style="color: grey;font-size: 14px;background: white;padding: 0 15px">没有账号</span>
    </el-divider>
    <!-- 注册和忘记密码按钮 -->
    <div style="padding: 0 20px;display: flex; flex-direction: column; gap: 16px; margin-bottom: 50px">
      <div class="button-wrapper">
        <el-button 
          @click="router.push('/welcome/register')" 
          class="uniform-button"
          type="default" 
          plain 
          round
        >
          注册账号
        </el-button>
      </div>
      <div class="button-wrapper">
        <el-button 
          @click="router.push('/welcome/forget')" 
          class="uniform-button"
          type="default" 
          plain 
          round
        >
          忘记密码
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import {Iphone, EditPen} from '@element-plus/icons-vue'
import router from "@/router";
import {reactive, ref, onMounted} from "vue";
import {ElMessage} from "element-plus";
import {get, login} from '@/net'

const formRef = ref()
const form = reactive({
  phone: '',
  code: '',
  remember: false  // 新增：记住我选项
})

const validatePhone = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入手机号码'))
  } else if (!/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('请输入正确的手机号码格式'))
  } else {
    callback()
  }
}

const rules = {
  phone: [
    { validator: validatePhone, trigger: ['blur', 'change'] }
  ],
  code: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: ['blur', 'change'] }
  ]
}

const isPhoneValid = ref(false)
const coldTime = ref(0)

const onValidate = (prop, isValid) => {
  if(prop === 'phone')
    isPhoneValid.value = isValid
}

// 新增：从localStorage加载记住的账号
const loadRememberedAccount = () => {
  try {
    const savedAccount = localStorage.getItem('remembered_account')
    if (savedAccount) {
      const account = JSON.parse(savedAccount)
      if (account.phone && account.code) {
        form.phone = account.phone
        form.code = account.code
        form.remember = true
      }
    }
  } catch (e) {
    console.error('加载记住的账号失败:', e)
    localStorage.removeItem('remembered_account')
  }
}

// 新增：保存账号到localStorage
const saveAccountToLocalStorage = () => {
  if (form.remember && form.phone && form.code) {
    const account = {
      phone: form.phone,
      code: form.code,
      timestamp: new Date().getTime()
    }
    localStorage.setItem('remembered_account', JSON.stringify(account))
  } else {
    localStorage.removeItem('remembered_account')
  }
}

// 新增：页面加载时恢复账号
onMounted(() => {
  loadRememberedAccount()
})

function userLogin() {
  formRef.value.validate((isValid) => {
    if(isValid) {
      // 新增：保存账号信息
      saveAccountToLocalStorage()
      
      login(form.phone, form.code, form.remember, () => router.push("/"))
    }
  });
}

const sendCode = () => {
  coldTime.value = 60
  get(`/api/unauth/project/askPhoneCode?phone=${form.phone}&type=login`, () => {
    ElMessage.success(`验证码已发送到手机: ${form.phone}，请注意查收`)
    
    const handle = setInterval(() => {
      if(coldTime.value > 0) {
        coldTime.value--
      } else {
        clearInterval(handle)
      } 
    }, 1000)
  }, undefined, (message) => {
    ElMessage.warning(message)
    coldTime.value = 0
  })
}
</script>

<style scoped>
/* 保持原有样式不变，只新增必要的样式 */
.login-container {
  max-width: 400px;
  margin: 0 auto;
}

/* 输入框聚焦效果增强 */
:deep(.el-input) {
  --el-input-focus-border-color: var(--el-color-primary);
}

/* 按钮悬停效果 */
.el-button {
  transition: all 0.3s ease;
}

.el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

/* 分割线样式优化 */
:deep(.el-divider__text) {
  background-color: #f5f7fa;
  padding: 0 15px;
}

.button-wrapper {
  width: 100%;
  display: flex;
  justify-content: center;
}

.uniform-button {
  width: 100%;
  max-width: 320px;
  height: 48px;
  font-size: 16px;
}

/* 确保文字在按钮内完全居中 */
:deep(.uniform-button span) {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  letter-spacing: 2px;
}

/* 如果还有问题，可以强制设置字体 */
:deep(.uniform-button) {
  font-family: "PingFang SC", "Microsoft YaHei", sans-serif;
  font-weight: 500;
}

/* 新增：记住我复选框样式（保持与原风格一致） */
:deep(.el-checkbox) {
  margin-right: 0;
}

:deep(.el-checkbox__label) {
  font-size: 14px;
  color: #606266;
  font-weight: normal;
}

/* 新增：忘记密码链接样式 */
:deep(.el-link) {
  font-size: 14px;
  font-weight: normal;
}
</style>