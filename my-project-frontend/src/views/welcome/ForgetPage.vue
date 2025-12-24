<template>
    <div>
        <div style="margin: 30px 20px">
            <el-steps :active="active" finish-status="success" align-center>
                <el-step title="验证手机号码" />
                <el-step title="重新设定密码" />
            </el-steps>
        </div>
        <transition name="el-fade-in-linear" mode="out-in">
            <div style="text-align: center;margin: 0 20px;height: 100%" v-if="active === 0">
                <div style="margin-top: 80px">
                    <div style="font-size: 25px;font-weight: bold">重置密码</div>
                    <div style="font-size: 14px;color: grey">请输入需要重置密码的手机号码</div>
                </div>
                <div style="margin-top: 50px">
                    <el-form :model="form" :rules="rules" @validate="onValidate" ref="formRef">
                        <el-form-item prop="phone">
                            <el-input v-model="form.phone" type="tel" :maxlength="11" placeholder="手机号码">
                                <template #prefix>
                                    <el-icon><Iphone /></el-icon>
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
                                    <el-button type="success" @click="validatePhoneCode"
                                               :disabled="!isPhoneValid || coldTime > 0">
                                        {{coldTime > 0 ? '请稍后 ' + coldTime + ' 秒' : '获取验证码'}}
                                    </el-button>
                                </el-col>
                            </el-row>
                        </el-form-item>
                    </el-form>
                </div>
                <div style="margin-top: 70px">
                    <el-button @click="confirmReset()" style="width: 270px;" type="danger" plain>开始重置密码</el-button>
                </div>
                 <div style="margin-top: 20px">
                    <span style="font-size: 14px;line-height: 15px;color: grey">已有账号? </span>
                    <el-link type="primary" style="translate: 0 -2px" @click="router.push('/login')">立即登录</el-link>
                </div>
            </div>
        </transition>
        <transition name="el-fade-in-linear" mode="out-in">
            <div style="text-align: center;margin: 0 20px;height: 100%" v-if="active === 1">
                <div style="margin-top: 80px">
                    <div style="font-size: 25px;font-weight: bold">重置密码</div>
                    <div style="font-size: 14px;color: grey">请填写您的新密码，务必牢记，防止丢失</div>
                </div>
                <div style="margin-top: 50px">
                    <el-form :model="form" :rules="rules" @validate="onValidate" ref="formRef">
                        <el-form-item prop="password">
                            <el-input v-model="form.password" :maxlength="20" type="password" placeholder="新密码">
                                <template #prefix>
                                    <el-icon><Lock /></el-icon>
                                </template>
                            </el-input>
                        </el-form-item>
                        <el-form-item prop="password_repeat">
                            <el-input v-model="form.password_repeat" :maxlength="20" type="password" placeholder="重复新密码">
                                <template #prefix>
                                    <el-icon><Lock /></el-icon>
                                </template>
                            </el-input>
                        </el-form-item>
                    </el-form>
                </div>
                <div style="margin-top: 70px">
                    <el-button @click="doReset()" style="width: 270px;" type="danger" plain>立即重置密码</el-button>
                </div>

            </div>
        </transition>
    </div>
</template>

<script setup>
import {reactive, ref} from "vue";
import {EditPen, Lock, Iphone} from "@element-plus/icons-vue";
import {get, post} from "@/net";
import {ElMessage} from "element-plus";
import router from "@/router";

const active = ref(0)

const form = reactive({
    phone: '',
    code: '',
    password: '',
    password_repeat: '',
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

const validatePassword = (rule, value, callback) => {
    if (value === '') {
        callback(new Error('请再次输入密码'))
    } else if (value !== form.password) {
        callback(new Error("两次输入的密码不一致"))
    } else {
        callback()
    }
}

const rules = {
    phone: [
        { validator: validatePhone, trigger: ['blur', 'change'] }
    ],
    code: [
        { required: true, message: '请输入获取的验证码', trigger: 'blur' },
        { min: 6, max: 6, message: '验证码长度必须为6位', trigger: ['blur', 'change'] }
    ],
    password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, max: 20, message: '密码的长度必须在6-20个字符之间', trigger: ['blur'] }
    ],
    password_repeat: [
        { validator: validatePassword, trigger: ['blur', 'change'] },
    ],
}

const formRef = ref()
const isPhoneValid = ref(false)
const coldTime = ref(0)

const onValidate = (prop, isValid) => {
    if(prop === 'phone')
        isPhoneValid.value = isValid
}

const validatePhoneCode = () => {
    coldTime.value = 60
    get(`/api/unauth/project/askPhoneCode?phone=${form.phone}&type=reset`, () => {
        ElMessage.success(`验证码已发送到手机: ${form.phone}，请注意查收`)
        
        const handle = setInterval(() => {
            if(coldTime.value > 0) {
                coldTime.value--
            } else {
                clearInterval(handle)
            }
        }, 1000)
    }, (message) => {
        ElMessage.warning(message)
        coldTime.value = 0
    })
}

const confirmReset = () => {
    formRef.value.validate((isValid) => {
        if(isValid) {
            post('/api/unauth/login/reset-confirm', {
                phone: form.phone,
                code: form.code
            }, () => active.value++)
        }
    })
}

const doReset = () => {
    formRef.value.validate((isValid) => {
        if(isValid) {
            post('/api/unauth/login/reset-password', {
                phone: form.phone,
                code: form.code,
                password: form.password
            }, () => {
                ElMessage.success('密码重置成功，请重新登录')
                router.push('/')
            })
        }
    })
}

</script>

<style scoped>

</style>