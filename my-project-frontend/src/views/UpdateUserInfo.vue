<template>
  <div class="update-user-info-container">
    <el-page-header @back="goBack" title="返回个人中心">
      <template #content>
        <span class="page-title">编辑个人资料</span>
      </template>
    </el-page-header>

    <el-card class="user-form-card" v-loading="loading">
      <el-form 
        :model="userForm" 
        label-width="100px"
        label-position="left"
        :rules="rules"
        ref="userFormRef"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" disabled></el-input>
        </el-form-item>

        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="userForm.nickname" placeholder="请输入昵称"></el-input>
        </el-form-item>

        <el-form-item label="性别" prop="sex">
          <el-radio-group v-model="userForm.sex">
            <el-radio :label="0">保密</el-radio>
            <el-radio :label="1">男</el-radio>
            <el-radio :label="2">女</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" placeholder="请输入邮箱"></el-input>
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input v-model="userForm.phone" placeholder="请输入手机号"></el-input>
        </el-form-item>

        <el-form-item label="头像" prop="avatarUrl">
          <el-upload
            class="avatar-uploader"
            action="http://localhost:8080/api/unauth/common/upload"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :before-upload="beforeAvatarUpload"
            :headers="uploadHeaders">
            <img v-if="userForm.avatarUrl" :src="userForm.avatarUrl" class="avatar">
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
            <div class="upload-tip" v-if="!userForm.avatarUrl">点击上传头像</div>
          </el-upload>
        </el-form-item>

        <el-form-item label="省份" prop="province">
          <el-input v-model="userForm.province" placeholder="请输入省份"></el-input>
        </el-form-item>

        <el-form-item label="城市" prop="city">
          <el-input v-model="userForm.city" placeholder="请输入城市"></el-input>
        </el-form-item>

        <el-form-item label="区县" prop="county">
          <el-input v-model="userForm.county" placeholder="请输入区县"></el-input>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="submitForm" :loading="submitting">保存</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { get, post } from '@/net'

const router = useRouter()

// 表单数据
const userForm = ref({
  id: '',
  username: '',
  sex: '',
  nickname: '',
  avatarUrl: '',
  email: '',
  phone: '',
  province: '',
  city: '',
  county: ''
})

// 表单验证规则
const rules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: ['blur', 'change'] }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

const loading = ref(false)
const submitting = ref(false)
const userFormRef = ref()

// 获取用户信息
const fetchUserInfo = async () => {
  try {
    loading.value = true
    const res = await get('/api/auth/user/getUserAllInfo')
    userForm.value = {
      id: res.id,
      username: res.username,
      sex: res.sex || 0,
      nickname: res.nickname || '',
      avatarUrl: res.avatarUrl || '',
      email: res.email || '',
      phone: res.phone || '',
      province: res.province || '',
      city: res.city || '',
      county: res.county || ''
    }
  } catch (error) {
    ElMessage.error('获取用户信息失败')
    router.back()
  } finally {
    loading.value = false
  }
}

// 头像上传成功
const handleAvatarSuccess = (response) => {
    console.log("上传成功",response)
    console.log("上传成功-userForm",userForm.avatarUrl)
  userForm.value.avatarUrl = response.data
  console.log("上传成功-userForm",userForm.avatarUrl)
  ElMessage.success('上传成功')
}

// 头像上传前校验
const beforeAvatarUpload = (file) => {
  const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJpgOrPng) {
    ElMessage.error('头像图片只能是 JPG/PNG 格式!')
  }
  

  return isJpgOrPng && isLt2M
}

// 提交表单
const submitForm = async () => {
  try {
    await userFormRef.value.validate()
    submitting.value = true
    
    await post('/api/auth/user/updateUserInfo', userForm.value)
    
    ElMessage.success('个人资料更新成功')
    router.back()
  } catch (error) {
    if (error !== 'validate') {
      ElMessage.error('更新失败: ' + (error.message || '未知错误'))
    }
  } finally {
    submitting.value = false
  }
}

// 重置表单
const resetForm = () => {
  userFormRef.value.resetFields()
  fetchUserInfo()
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 初始化加载用户数据
onMounted(() => {
  fetchUserInfo()
})
</script>

<style scoped>
.update-user-info-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.page-title {
  font-size: 18px;
  font-weight: 500;
}

.user-form-card {
  margin-top: 20px;
  padding: 20px;
}

.avatar-uploader {
  display: flex;
  align-items: center;
}

.avatar-uploader :deep(.el-upload) {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.avatar-uploader :deep(.el-upload:hover) {
  border-color: var(--el-color-primary);
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 120px;
  height: 120px;
  line-height: 120px;
  text-align: center;
}

.avatar {
  width: 120px;
  height: 120px;
  display: block;
  object-fit: cover;
}
</style>