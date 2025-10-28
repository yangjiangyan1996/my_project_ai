<template>
  <div style="text-align: center;margin: 0 20px">
    <div style="margin-top: 100px">
      <div style="font-size: 25px;font-weight: bold">登录</div>
      <div style="font-size: 14px;color: grey">在进入系统之前请先输入手机号和验证码进行登录</div>
    </div>
    <div style="margin-top: 50px">
      <el-form :model="form" :rules="rules" @validate="onValidate" ref="formRef">
        <el-form-item prop="phone">
          <el-input v-model="form.phone" maxlength="11" type="tel" placeholder="手机号码">
            <template #prefix>
              <el-icon>
                <Iphone/>
              </el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="code">
          <el-row :gutter="10" style="width: 100%">
            <el-col :span="17">
              <el-input v-model="form.code" :maxlength="6" type="text" placeholder="请输入验证码">
                <template #prefix>
                  <el-icon><EditPen /></el-icon>
                </template>
              </el-input>
            </el-col>
            <el-col :span="5">
              <el-button type="success" @click="sendCode"
                         :disabled="!isPhoneValid || coldTime > 0">
                {{coldTime > 0 ? '请稍后 ' + coldTime + ' 秒' : '获取验证码'}}
              </el-button>
            </el-col>
          </el-row>
        </el-form-item>
      </el-form>
    </div>
    <div style="margin-top: 40px">
      <el-button @click="userLogin()" style="width: 270px" type="success" plain>立即登录</el-button>
    </div>
    <el-divider>
      <span style="color: grey;font-size: 13px">没有账号</span>
    </el-divider>
    <div>
      <el-button style="width: 270px" @click="router.push('/welcome/register')" type="warning" plain>注册账号</el-button>
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

</style>