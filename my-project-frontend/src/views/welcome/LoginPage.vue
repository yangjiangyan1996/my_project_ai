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
              <el-input v-model="form.code" :maxlength="6" type="text" placeholder="请输入密码" size="large">
                <template #prefix>
                  <el-icon><EditPen /></el-icon>
                </template>
              </el-input>
            </el-col>
          </el-row>
        </el-form-item>
      </el-form>
    </div>
    <div style="margin-top: 60px">
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
import {reactive, ref} from "vue";
import {ElMessage} from "element-plus";
import {get, post,login} from '@/net'

const formRef = ref()
const form = reactive({
  phone: '',
  code: ''
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
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ]
}

const isPhoneValid = ref(false)
const coldTime = ref(0)

const onValidate = (prop, isValid) => {
  if(prop === 'phone')
    isPhoneValid.value = isValid
}


function userLogin() {
  formRef.value.validate((isValid) => {
    if(isValid) {
      login(form.phone, form.code, form.remember, () => router.push("/"))
    }
  });
}

// function userLogin() {
//   formRef.value.validate((isValid) => {
//     if(isValid) {
//       post('/api/auth/login-by-code', {
//         phone: form.phone,
//         code: form.code
//       }, () => {
//         ElMessage.success('登录成功')
//         router.push("/")
//       })
//     }
//   });
// }

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
/* 添加容器最大宽度限制，在大屏幕上不会太宽 */
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
  max-width: 320px; /* 设置最大宽度，避免太宽 */
  height: 48px;
  font-size: 16px;
}

/* 确保文字在按钮内完全居中 */
:deep(.uniform-button span) {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  letter-spacing: 2px; /* 微调字间距 */
}

/* 如果还有问题，可以强制设置字体 */
:deep(.uniform-button) {
  font-family: "PingFang SC", "Microsoft YaHei", sans-serif;
  font-weight: 500;
}
</style>