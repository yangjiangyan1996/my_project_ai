<template>
  <div class="profile-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2 class="page-title">个人中心</h2>
      <div class="page-actions">
        <el-button type="primary" @click="handleEditProfile">编辑资料</el-button>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="profile-content">
      <el-row :gutter="20">
        <!-- 左侧：用户信息卡片 -->
        <el-col :xs="24" :lg="8">
          <!-- 用户信息卡片 -->
          <el-card class="user-card" shadow="never">
            <template #header>
              <div class="card-header">
                <span class="card-title">个人信息</span>
              </div>
            </template>
            
            <div class="user-info">
              <!-- 头像区域 -->
              <div class="avatar-section">
                <el-avatar 
                  :src="userInfo.avatarUrl || '/images/default-avatar.png'" 
                  :size="100"
                  class="user-avatar"
                />
                <div class="avatar-actions">
                  <el-button type="text" @click="handleAvatarEdit">更换头像</el-button>
                </div>
              </div>

              <!-- 基本信息 -->
              <div class="basic-info">
                <div class="info-item">
                  <span class="info-label">用户名：</span>
                  <span class="info-value">{{ userInfo.username }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">真实姓名：</span>
                  <span class="info-value">{{ userInfo.nickname || '未设置' }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">角色：</span>
                  <el-tag :type="getRoleType(userInfo.role)" class="role-tag">
                    {{ getRoleText(userInfo.role) }}
                  </el-tag>
                </div>
                <div class="info-item">
                  <span class="info-label">手机号：</span>
                  <span class="info-value">{{ userInfo.phone || '未设置' }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">邮箱：</span>
                  <span class="info-value">{{ userInfo.email || '未设置' }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">性别：</span>
                  <span class="info-value">{{ getGenderText(userInfo.sex) }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">地区：</span>
                  <span class="info-value">
                    {{ [userInfo.province, userInfo.city, userInfo.county].filter(Boolean).join(' ') || '未设置' }}
                  </span>
                </div>
                <div class="info-item">
                  <span class="info-label">注册时间：</span>
                  <span class="info-value">{{ formatTime(userInfo.registerTime) }}</span>
                </div>
              </div>
            </div>
          </el-card>

          <!-- 租户信息卡片 -->
          <el-card class="tenant-card" shadow="never" v-if="tenantInfo">
            <template #header>
              <div class="card-header">
                <span class="card-title">企业信息</span>
              </div>
            </template>
            
            <div class="tenant-info">
              <div class="info-item">
                <span class="info-label">企业名称：</span>
                <span class="info-value">{{ tenantInfo.name }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">联系人：</span>
                <span class="info-value">{{ tenantInfo.contactPerson || '未设置' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">联系电话：</span>
                <span class="info-value">{{ tenantInfo.contactPhone || '未设置' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">服务状态：</span>
                <el-tag :type="tenantInfo.status === 1 ? 'success' : 'danger'">
                  {{ tenantInfo.status === 1 ? '正常' : '禁用' }}
                </el-tag>
              </div>
              <div class="info-item" v-if="tenantInfo.expireAt">
                <span class="info-label">服务到期：</span>
                <span class="info-value" :class="{ 'expire-soon': isExpireSoon }">
                  {{ formatTime(tenantInfo.expireAt) }}
                </span>
              </div>
            </div>
          </el-card>
        </el-col>

        <!-- 右侧：操作日志和其他信息 -->
        <el-col :xs="24" :lg="16">
          <!-- 操作日志 -->
          <el-card class="operation-log-card" shadow="never">
            <template #header>
              <div class="card-header">
                <span class="card-title">最近操作记录</span>
                <el-button type="text" @click="viewAllOperations">查看全部</el-button>
              </div>
            </template>
            
            <el-table 
              :data="operationLogs" 
              style="width: 100%"
              empty-text="暂无操作记录"
              v-loading="loading"
            >
              <el-table-column prop="operationTime" label="操作时间" width="180">
                <template #default="scope">
                  {{ formatTime(scope.row.createdAt) }}
                </template>
              </el-table-column>
              <el-table-column prop="module" label="操作模块" width="120" />
              <el-table-column prop="operation" label="操作类型" width="120" />
              <el-table-column prop="description" label="操作描述" min-width="200" />
              <el-table-column prop="ipAddress" label="IP地址" width="130" />
            </el-table>
          </el-card>

          <!-- 统计信息 -->
          <el-card class="stats-card" shadow="never">
            <template #header>
              <div class="card-header">
                <span class="card-title">个人统计</span>
              </div>
            </template>
            
            <el-row :gutter="20">
              <el-col :xs="12" :sm="6" v-for="stat in userStats" :key="stat.label">
                <div class="stat-item">
                  <div class="stat-value">{{ stat.value }}</div>
                  <div class="stat-label">{{ stat.label }}</div>
                </div>
              </el-col>
            </el-row>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 编辑资料对话框 -->
    <el-dialog 
      v-model="editDialogVisible" 
      title="编辑资料" 
      width="500px"
      :before-close="handleEditDialogClose"
    >
      <el-form 
        :model="editForm" 
        :rules="editRules" 
        ref="editFormRef"
        label-width="80px"
      >
        <el-form-item label="真实姓名" prop="nickname">
          <el-input v-model="editForm.nickname" placeholder="请输入真实姓名" />
        </el-form-item>
        
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="editForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        
        <el-form-item label="性别" prop="sex">
          <el-radio-group v-model="editForm.sex">
            <el-radio :label="0">未知</el-radio>
            <el-radio :label="1">男</el-radio>
            <el-radio :label="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="地区" prop="region">
          <el-cascader
            v-model="editForm.region"
            :options="regionOptions"
            placeholder="请选择地区"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="handleEditDialogClose">取消</el-button>
        <el-button type="primary" @click="handleSaveProfile" :loading="saving">保存</el-button>
      </template>
    </el-dialog>

    <!-- 头像上传对话框 -->
    <el-dialog 
      v-model="avatarDialogVisible" 
      title="更换头像" 
      width="400px"
    >
      <div class="avatar-upload">
        <el-upload
          class="avatar-uploader"
          action="/api/auth/user/uploadAvatar"
          :show-file-list="false"
          :before-upload="beforeAvatarUpload"
          :on-success="handleAvatarSuccess"
          :on-error="handleAvatarError"
        >
          <img v-if="avatarPreview" :src="avatarPreview" class="avatar-preview" />
          <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
        </el-upload>
        <div class="upload-tips">
          <p>建议上传 1:1 比例的图片</p>
          <p>支持 JPG、PNG 格式，大小不超过 2MB</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { get, post } from '@/net'

// 响应式数据
const loading = ref(false)
const saving = ref(false)
const editDialogVisible = ref(false)
const avatarDialogVisible = ref(false)
const avatarPreview = ref('')

// 用户信息
const userInfo = ref({
  id: null,
  username: '',
  nickname: '',
  avatarUrl: '',
  phone: '',
  email: '',
  sex: 0,
  province: '',
  city: '',
  county: '',
  role: 'USER',
  registerTime: ''
})

// 租户信息
const tenantInfo = ref(null)

// 操作日志
const operationLogs = ref([])

// 编辑表单
const editForm = reactive({
  nickname: '',
  phone: '',
  email: '',
  sex: 0,
  region: []
})

const editFormRef = ref()
const editRules = {
  nickname: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

// 地区选项（简化版，实际项目中可以使用完整的地区数据）
const regionOptions = [
  {
    value: 'beijing',
    label: '北京市',
    children: [
      { value: 'dongcheng', label: '东城区' },
      { value: 'xicheng', label: '西城区' }
    ]
  }
  // 可以添加更多地区数据
]

// 计算属性
const userStats = computed(() => [
  { label: '本月操作', value: '128' },
  { label: '待办审批', value: '5' },
  { label: '已完成', value: '89' },
  { label: '登录次数', value: '156' }
])

const isExpireSoon = computed(() => {
  if (!tenantInfo.value?.expireAt) return false
  const expireTime = new Date(tenantInfo.value.expireAt).getTime()
  const now = new Date().getTime()
  return expireTime - now < 30 * 24 * 60 * 60 * 1000 // 30天内到期
})

// 方法
const getRoleType = (role) => {
  return role === 'ADMIN' ? 'danger' : 'primary'
}

const getRoleText = (role) => {
  return role === 'ADMIN' ? '管理员' : '普通用户'
}

const getGenderText = (sex) => {
  const genderMap = { 0: '未知', 1: '男', 2: '女' }
  return genderMap[sex] || '未知'
}

const formatTime = (timeString) => {
  if (!timeString) return ''
  const date = new Date(timeString)
  return `${date.getFullYear()}-${padZero(date.getMonth() + 1)}-${padZero(date.getDate())} ${padZero(date.getHours())}:${padZero(date.getMinutes())}`
}

const padZero = (num) => {
  return num < 10 ? `0${num}` : num
}

// 数据加载
const loadUserInfo = async () => {
  try {
    const res = await get('/api/auth/user/info')
    userInfo.value = res || {}
    // 初始化编辑表单
    Object.assign(editForm, {
      nickname: userInfo.value.nickname || '',
      phone: userInfo.value.phone || '',
      email: userInfo.value.email || '',
      sex: userInfo.value.sex || 0,
      region: [userInfo.value.province, userInfo.value.city, userInfo.value.county].filter(Boolean)
    })
  } catch (e) {
    console.error('加载用户信息失败:', e)
  }
}

const loadTenantInfo = async () => {
  try {
    const res = await get('/api/auth/tenant/info')
    tenantInfo.value = res || null
  } catch (e) {
    console.error('加载租户信息失败:', e)
  }
}

const loadOperationLogs = async () => {
  loading.value = true
  try {
    const res = await post('/api/auth/operation-log/user', {
      page: 1,
      size: 10
    })
    operationLogs.value = res.records || []
  } catch (e) {
    console.error('加载操作记录失败:', e)
  } finally {
    loading.value = false
  }
}

// 事件处理
const handleEditProfile = () => {
  editDialogVisible.value = true
}

const handleEditDialogClose = () => {
  editDialogVisible.value = false
  // 重置表单
  if (editFormRef.value) {
    editFormRef.value.resetFields()
  }
  // 重新加载用户信息以恢复原始数据
  Object.assign(editForm, {
    nickname: userInfo.value.nickname || '',
    phone: userInfo.value.phone || '',
    email: userInfo.value.email || '',
    sex: userInfo.value.sex || 0,
    region: [userInfo.value.province, userInfo.value.city, userInfo.value.county].filter(Boolean)
  })
}

const handleSaveProfile = async () => {
  if (!editFormRef.value) return
  
  try {
    await editFormRef.value.validate()
    saving.value = true
    
    const updateData = {
      nickname: editForm.nickname,
      phone: editForm.phone,
      email: editForm.email,
      sex: editForm.sex
    }
    
    // 处理地区数据
    if (editForm.region && editForm.region.length >= 2) {
      updateData.province = editForm.region[0] || ''
      updateData.city = editForm.region[1] || ''
      updateData.county = editForm.region[2] || ''
    }
    
    await post('/api/auth/user/updateProfile', updateData)
    ElMessage.success('资料更新成功')
    editDialogVisible.value = false
    // 重新加载用户信息
    await loadUserInfo()
  } catch (e) {
    if (e.errors) {
      // 表单验证错误，不显示消息
      return
    }
    ElMessage.error('资料更新失败')
  } finally {
    saving.value = false
  }
}

const handleAvatarEdit = () => {
  avatarDialogVisible.value = true
}

const beforeAvatarUpload = (file) => {
  const isJPGOrPNG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPGOrPNG) {
    ElMessage.error('头像只能是 JPG 或 PNG 格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('头像大小不能超过 2MB!')
    return false
  }
  
  // 预览图片
  const reader = new FileReader()
  reader.readAsDataURL(file)
  reader.onload = (e) => {
    avatarPreview.value = e.target.result
  }
  
  return false // 手动上传
}

const handleAvatarSuccess = (response) => {
  ElMessage.success('头像上传成功')
  avatarDialogVisible.value = false
  avatarPreview.value = ''
  // 重新加载用户信息
  loadUserInfo()
}

const handleAvatarError = () => {
  ElMessage.error('头像上传失败')
}

const viewAllOperations = () => {
  // 跳转到完整的操作日志页面
  // 这里可以根据需要实现跳转逻辑
  ElMessage.info('跳转到操作日志页面')
}

// 初始化
onMounted(() => {
  loadUserInfo()
  loadTenantInfo()
  loadOperationLogs()
})
</script>

<style scoped>
.profile-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 0 10px;
}

.page-title {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin: 0;
}

.profile-content {
  max-width: 1200px;
  margin: 0 auto;
}

.user-card,
.tenant-card,
.operation-log-card,
.stats-card {
  border-radius: 8px;
  margin-bottom: 20px;
  border: 1px solid #e6e6e6;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

/* 用户信息样式 */
.user-info {
  text-align: center;
}

.avatar-section {
  margin-bottom: 20px;
}

.user-avatar {
  border: 3px solid #f0f0f0;
  margin-bottom: 10px;
}

.avatar-actions {
  margin-top: 10px;
}

.basic-info {
  text-align: left;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f5f5f5;
}

.info-item:last-child {
  border-bottom: none;
}

.info-label {
  color: #909399;
  font-size: 14px;
}

.info-value {
  color: #303133;
  font-size: 14px;
  text-align: right;
}

.role-tag {
  margin-left: 8px;
}

/* 租户信息样式 */
.tenant-info .info-item {
  border-bottom: 1px solid #f5f5f5;
  padding: 10px 0;
}

.expire-soon {
  color: #e6a23c;
  font-weight: bold;
}

/* 统计信息样式 */
.stat-item {
  text-align: center;
  padding: 20px 0;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

/* 头像上传样式 */
.avatar-upload {
  text-align: center;
}

.avatar-uploader {
  border: 2px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: border-color 0.3s;
  width: 178px;
  height: 178px;
  margin: 0 auto 20px;
}

.avatar-uploader:hover {
  border-color: #409EFF;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  line-height: 178px;
  text-align: center;
}

.avatar-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.upload-tips {
  text-align: center;
  color: #909399;
  font-size: 12px;
}

.upload-tips p {
  margin: 4px 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .profile-container {
    padding: 10px;
  }
  
  .page-header {
    flex-direction: column;
    gap: 10px;
    align-items: flex-start;
  }
  
  .info-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
  
  .info-value {
    text-align: left;
  }
}
</style>