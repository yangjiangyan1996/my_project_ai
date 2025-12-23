<template>
    <div style="text-align: center;margin: 0 20px">
        <div style="margin-top: 100px">
            <div style="font-size: 25px;font-weight: bold">注册新用户</div>
            <div style="font-size: 14px;color: grey">欢迎注册我们的学习平台，请在下方填写相关信息</div>
        </div>
        <div style="margin-top: 50px">
            <el-form :model="form" :rules="rules" @validate="onValidate" ref="formRef">
                <!-- 租户选择 -->
                <el-form-item prop="tenantId">
                    <el-select
                        v-model="form.tenantId"
                        filterable
                        clearable
                        placeholder="请选择所属企业"
                        style="width: 100%"
                        :filter-method="filterTenants"
                        :popper-append-to-body="false"
                    >
                        <template #prefix>
                            <el-icon><OfficeBuilding /></el-icon>
                        </template>
                        
                        <el-option 
                            v-for="tenant in filteredTenants" 
                            :key="tenant.id"
                            :label="tenant.name"
                            :value="tenant.id"
                        >
                            <div style="display: flex; align-items: center; gap: 8px">
                                <el-avatar 
                                    v-if="tenant.image" 
                                    :src="tenant.image" 
                                    :size="24"
                                    shape="square"
                                />
                                <span>{{ tenant.name }}</span>
                            </div>
                        </el-option>
                        
                        <!-- 无数据时的显示 -->
                        <template #empty>
                            <div style="padding: 10px; color: #909399; text-align: center">
                                {{ searchQuery ? '未找到匹配的企业' : '暂无企业数据' }}
                            </div>
                        </template>
                    </el-select>
                </el-form-item>
                
                <el-form-item prop="username">
                    <el-input v-model="form.username" :maxlength="8" type="text" placeholder="用户名">
                        <template #prefix>
                            <el-icon><User /></el-icon>
                        </template>
                    </el-input>
                </el-form-item>
                <el-form-item prop="password">
                    <el-input v-model="form.password" :maxlength="16" type="password" placeholder="密码">
                        <template #prefix>
                            <el-icon><Lock /></el-icon>
                        </template>
                    </el-input>
                </el-form-item>
                <el-form-item prop="password_repeat">
                    <el-input v-model="form.password_repeat" :maxlength="16" type="password" placeholder="重复密码">
                        <template #prefix>
                            <el-icon><Lock /></el-icon>
                        </template>
                    </el-input>
                </el-form-item>
                <el-form-item prop="phone">
                    <el-input v-model="form.phone" type="tel" placeholder="手机号码">
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
                            <el-button type="success" @click="sendCode"
                                       :disabled="!isPhoneValid || coldTime > 0">
                                {{coldTime > 0 ? '请稍后 ' + coldTime + ' 秒' : '获取验证码'}}
                            </el-button>
                        </el-col>
                    </el-row>
                </el-form-item>
            </el-form>
        </div>
        <div style="margin-top: 80px">
            <el-button style="width: 270px" type="warning" @click="register" plain>立即注册</el-button>
        </div>
        <div style="margin-top: 20px">
            <span style="font-size: 14px;line-height: 15px;color: grey">已有账号? </span>
            <el-link type="primary" style="translate: 0 -2px" @click="router.push('/login')">立即登录</el-link>
        </div>
    </div>
</template>

<script setup>
import { EditPen, Lock, User, Iphone, OfficeBuilding } from "@element-plus/icons-vue";
import router from "@/router";
import { reactive, ref, onMounted, computed } from "vue";
import { ElMessage } from "element-plus";
import { get, post } from "@/net";

// 修改 form 对象，添加 tenantId 字段
const form = reactive({
    username: '',
    password: '',
    password_repeat: '',
    phone: '',
    code: '',
    tenantId: ''  // 添加 tenantId 字段
})

// 租户相关数据
const tenants = ref([]) // 租户列表
const searchQuery = ref('') // 搜索关键词

// 计算属性：过滤后的租户列表
const filteredTenants = computed(() => {
    if (!searchQuery.value.trim()) {
        return tenants.value
    }
    const query = searchQuery.value.toLowerCase()
    return tenants.value.filter(tenant => 
        tenant.name && tenant.name.toLowerCase().includes(query)
    )
})

// 过滤租户选项
const filterTenants = (query) => {
    searchQuery.value = query
}

// 加载租户列表
const loadTenants = async () => {
    const res = await get('/api/unauth/tenant/getTenantList')
    console.log('加载租户列表:', res)
    if (res) {
        tenants.value = res || []
    } else {
        console.error('加载租户列表失败')
        ElMessage.error('加载企业列表失败')
    }
}

// 组件挂载时加载租户列表
onMounted(() => {
    loadTenants()
})

const validateUsername = (rule, value, callback) => {
    if (value === '') {
        callback(new Error('请输入用户名'))
    } else if(!/^[a-zA-Z0-9\u4e00-\u9fa5]+$/.test(value)){
        callback(new Error('用户名不能包含特殊字符，只能是中文/英文'))
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

const validatePhone = (rule, value, callback) => {
    if (value === '') {
        callback(new Error('请输入手机号码'))
    } else if (!/^1[3-9]\d{9}$/.test(value)) {
        callback(new Error('请输入正确的手机号码格式'))
    } else {
        callback()
    }
}

// 添加租户验证规则
const validateTenant = (rule, value, callback) => {
    if (value === '') {
        callback(new Error('请选择所属企业'))
    } else {
        callback()
    }
}

const rules = {
    username: [
        { validator: validateUsername, trigger: ['blur', 'change'] },
        { min: 2, max: 8, message: '用户名的长度必须在2-8个字符之间', trigger: ['blur', 'change'] },
    ],
    password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, max: 16, message: '密码的长度必须在6-16个字符之间', trigger: ['blur', 'change'] }
    ],
    password_repeat: [
        { validator: validatePassword, trigger: ['blur', 'change'] },
    ],
    phone: [
        { validator: validatePhone, trigger: ['blur', 'change'] }
    ],
    code: [
        { required: true, message: '请输入获取的验证码', trigger: 'blur' },
    ],
    tenantId: [  // 注意这里改为 tenantId
        { validator: validateTenant, trigger: ['change', 'blur'] }
    ]
}

const formRef = ref()
const isPhoneValid = ref(false)
const coldTime = ref(0)

const onValidate = (prop, isValid) => {
    if(prop === 'phone')
        isPhoneValid.value = isValid
}

const register = () => {
    formRef.value.validate((isValid) => {
        if(isValid) {
            post('/api/unauth/project/register', {
                username: form.username,
                password: form.password,
                phone: form.phone,
                code: form.code,
                tenantId: form.tenantId  // 使用 form.tenantId
            }, () => {
                ElMessage.success('注册成功，欢迎加入我们')
                router.push("/")
            })
        } else {
            ElMessage.warning('请完整填写注册表单内容！')
        }
    })
}

const sendCode = () => {
    coldTime.value = 60
    get(`/api/unauth/project/askPhoneCode?phone=${form.phone}&type=register`, () => {
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