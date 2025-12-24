<template>
  <div class="profile-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-left">
        <div class="header-icon">
          <el-icon size="24"><UserFilled /></el-icon>
        </div>
        <div>
          <h2 class="page-title">个人中心</h2>
          <p class="page-subtitle">管理您的个人信息和操作记录</p>
        </div>
      </div>
      <div class="page-actions">
        <el-button type="primary" @click="handleEditProfile" :icon="Edit">
          编辑个人资料
        </el-button>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="profile-content">
      <!-- 第一行：企业信息和个人信息并排 -->
      <div class="info-row">
        <!-- 左侧：企业信息 -->
        <el-col :xs="24" :md="12" class="info-col">
          <el-card class="tenant-card" shadow="hover" v-if="tenantInfo">
            <template #header>
              <div class="card-header">
                <div class="header-content">
                  <el-icon class="header-icon"><OfficeBuilding /></el-icon>
                  <span class="card-title">企业信息</span>
                </div>
                <div class="card-header-right">
                  <el-tag :type="tenantInfo.status === 1 ? 'success' : 'danger'">
                    {{ getTenantStatusText(tenantInfo.status) }}
                  </el-tag>
                  <el-button 
                    v-if="tenantInfo.bossAuth"
                    type="text" 
                    @click="handleEditTenant"
                    :icon="Edit"
                    class="edit-tenant-btn"
                  >
                    编辑
                  </el-button>
                </div>
              </div>
            </template>
            
            <div class="tenant-info">
              <div class="company-header">
                <div class="company-logo">
                  <el-image 
                    v-if="tenantInfo.image"
                    :src="tenantInfo.image" 
                    :preview-src-list="[tenantInfo.image]"
                    :initial-index="0"
                    fit="cover"
                    class="company-image"
                  >
                    <template #error>
                      <el-icon size="48"><OfficeBuilding /></el-icon>
                    </template>
                  </el-image>
                  <el-icon v-else size="48"><OfficeBuilding /></el-icon>
                </div>
                <div class="company-details">
                  <h4 class="company-name">{{ tenantInfo.name || '未设置企业名称' }}</h4>
                  <p class="company-contact">{{ tenantInfo.contactPerson || '未设置董事长' }}</p>
                  <el-tag v-if="tenantInfo.bossAuth" type="success" size="small" class="boss-tag">
                    老板权限
                  </el-tag>
                </div>
              </div>
              
              <div class="company-info-grid">
                <div class="info-item">
                  <div class="item-label">
                    <el-icon><User /></el-icon>
                    <span>董事长</span>
                  </div>
                  <div class="item-value">{{ tenantInfo.contactPerson || '未设置' }}</div>
                </div>
                <div class="info-item">
                  <div class="item-label">
                    <el-icon><Phone /></el-icon>
                    <span>联系电话</span>
                  </div>
                  <div class="item-value">{{ tenantInfo.contactPhone || '未设置' }}</div>
                </div>
                <div class="info-item">
                  <div class="item-label">
                    <el-icon><CircleCheck /></el-icon>
                    <span>服务状态</span>
                  </div>
                  <div class="item-value">
                    <el-tag :type="tenantInfo.status === 1 ? 'success' : 'danger'" size="small">
                      {{ getTenantStatusText(tenantInfo.status) }}
                    </el-tag>
                  </div>
                </div>


                <!-- <div class="info-item" v-if="tenantInfo.expireAt">
                  <div class="item-label">
                    <el-icon><Clock /></el-icon>
                    <span>服务到期时间</span>
                  </div>
                  <div class="item-value" :class="{ 'expire-soon': isExpireSoon }">
                    {{ formatTime(tenantInfo.expireAt) }}
                  </div>
                </div>
                 -->
              </div>
            </div>
          </el-card>
        </el-col>

        <!-- 右侧：个人信息 -->
        <el-col :xs="24" :md="12" class="info-col">
          <el-card class="user-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <div class="header-content">
                  <el-icon class="header-icon"><User /></el-icon>
                  <span class="card-title">个人信息</span>
                </div>
                <el-tag :type="getRoleType(userInfo.role)" class="role-tag">
                  {{ getRoleText(userInfo.role) }}
                </el-tag>
              </div>
            </template>
            
            <div class="user-info">
              <!-- 头像区域 -->
              <div class="avatar-section">
                <div class="avatar-wrapper">
                  <el-avatar 
                    :src="userInfo.avatarUrl || '/images/default-avatar.png'" 
                    :size="100"
                    class="user-avatar"
                  />
                  <div class="avatar-status">
                    <span class="status-dot"></span>
                    <span class="status-text">在线</span>
                  </div>
                </div>
                <div class="user-names">
                  <h3 class="user-name">{{ userInfo.nickname || '未设置昵称' }}</h3>
                  <p class="user-realname">{{ userInfo.username || '未设置真实姓名' }}</p>
                </div>
              </div>

              <!-- 基本信息 -->
              <div class="basic-info">
                <div class="info-grid">
                  <div class="info-item">
                    <div class="item-label">
                      <el-icon><Iphone /></el-icon>
                      <span>手机号</span>
                    </div>
                    <div class="item-value">{{ userInfo.phone || '未设置' }}</div>
                  </div>
                  <div class="info-item">
                    <div class="item-label">
                      <el-icon><Message /></el-icon>
                      <span>邮箱</span>
                    </div>
                    <div class="item-value">{{ userInfo.email || '未设置' }}</div>
                  </div>
                  <div class="info-item">
                    <div class="item-label">
                      <el-icon><Male /></el-icon>
                      <span>性别</span>
                    </div>
                    <div class="item-value">{{ getGenderText(userInfo.sex) }}</div>
                  </div>
                  <div class="info-item">
                    <div class="item-label">
                      <el-icon><MapLocation /></el-icon>
                      <span>地区</span>
                    </div>
                    <div class="item-value">
                      {{ [userInfo.province, userInfo.city, userInfo.county].filter(Boolean).join(' ') || '未设置' }}
                    </div>
                  </div>
                  <div class="info-item">
                    <div class="item-label">
                      <el-icon><Calendar /></el-icon>
                      <span>注册时间</span>
                    </div>
                    <div class="item-value">{{ formatTime(userInfo.registerTime) || '未设置' }}</div>
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </div>

      <!-- 第二行：操作日志 -->
      <div class="log-row">
        <el-col :xs="24" class="log-col">
          <el-card class="operation-log-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <div class="header-content">
                  <el-icon class="header-icon"><Histogram /></el-icon>
                  <span class="card-title">最近操作记录</span>
                </div>
                <el-button type="primary" text @click="viewAllOperations">
                  查看全部
                  <el-icon><ArrowRight /></el-icon>
                </el-button>
              </div>
            </template>
            
            <!-- 操作日志表格 -->
            <div class="log-table-container">
              <el-table 
                :data="operationLogs" 
                style="width: 100%"
                empty-text="暂无操作记录"
                v-loading="loading"
                @sort-change="handleSortChange"
                class="log-table"
                :row-class-name="tableRowClassName"
              >
                <el-table-column prop="createdAt" label="操作时间" width="200" sortable="custom">
                  <template #default="scope">
                    <div class="time-cell">
                      <div class="time-date">{{ formatDate(scope.row.createdAt) }}</div>
                      <div class="time-time">{{ formatTimeOnly(scope.row.createdAt) }}</div>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column prop="module" label="操作模块" width="160">
                  <template #default="scope">
                    <el-tag size="small" :type="getModuleType(scope.row.module)">
                      {{ scope.row.module }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="operation" label="操作类型" width="200">
                  <template #default="scope">
                    <span class="operation-type" :class="getOperationClass(scope.row.operation)">
                      {{ scope.row.operation }}
                    </span>
                  </template>
                </el-table-column>
                <el-table-column prop="description" label="操作描述" min-width="250">
                  <template #default="scope">
                    <div class="description-cell">
                      {{ scope.row.description }}
                      <div v-if="scope.row.targetId" class="target-id">
                        ID: {{ scope.row.targetId }}
                      </div>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column prop="ipAddress" label="IP地址" width="130">
                  <template #default="scope">
                    <span class="ip-address">{{ scope.row.ipAddress || '-' }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="80" align="center">
                  <template #default="scope">
                    <el-button 
                      type="text" 
                      size="small"
                      class="detail-btn"
                      @click="viewLogDetail(scope.row)"
                      v-if="scope.row.requestParams"
                    >
                      <el-icon><View /></el-icon>
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
            
            <!-- 分页 -->
            <div class="pagination-container" v-if="pagination.total > 0">
              <el-pagination
                v-model:current-page="pagination.current"
                v-model:page-size="pagination.size"
                :page-sizes="[5, 10, 20, 50]"
                :total="pagination.total"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
                class="custom-pagination"
              />
            </div>
          </el-card>
        </el-col>
      </div>
    </div>

    <!-- 编辑个人资料对话框 -->
    <el-dialog 
      v-model="editUserDialogVisible" 
      title="编辑个人资料" 
      width="600px"
      :before-close="handleEditUserDialogClose"
      class="edit-dialog"
    >
      <el-form 
        :model="editUserForm" 
        :rules="editUserRules" 
        ref="editUserFormRef"
        label-width="100px"
        label-position="left"
        class="edit-form"
      >
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="头像" prop="avatarUrl">
              <div class="avatar-upload-container">
                <el-upload
                  class="avatar-uploader"
                  :action="uploadAction"
                  :show-file-list="false"
                  :on-success="(res) => handleUploadSuccess(res, 'avatarUrl', editUserForm)"
                  :before-upload="beforeImageUpload"
                  :headers="uploadHeaders">
                  <div class="upload-content">
                    <img v-if="editUserForm.avatarUrl" :src="editUserForm.avatarUrl" class="avatar">
                    <div v-else class="upload-placeholder">
                      <el-icon size="40"><CameraFilled /></el-icon>
                      <div>点击上传头像</div>
                    </div>
                  </div>
                </el-upload>
                <div class="upload-tips">
                  <p>建议尺寸：200×200像素</p>
                  <p>支持 JPG/PNG 格式，不超过2MB</p>
                </div>
              </div>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="基础信息">
          <div class="form-section-divider"></div>
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="昵称" prop="nickname">
              <el-input 
                v-model="editUserForm.nickname" 
                placeholder="请输入您的网名"
                size="large"
                :prefix-icon="User"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="真实姓名" prop="username">
              <el-input 
                v-model="editUserForm.username" 
                placeholder="请输入真实姓名"
                size="large"
                :prefix-icon="Avatar"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input 
                v-model="editUserForm.phone" 
                placeholder="请输入手机号"
                size="large"
                :prefix-icon="Iphone"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input 
                v-model="editUserForm.email" 
                placeholder="请输入邮箱"
                size="large"
                :prefix-icon="Message"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="其他信息">
          <div class="form-section-divider"></div>
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="性别" prop="sex">
              <el-radio-group v-model="editUserForm.sex" class="gender-group">
                <el-radio-button :label="0">
                  <el-icon><User /></el-icon>
                  <span>未知</span>
                </el-radio-button>
                <el-radio-button :label="1">
                  <el-icon><Male /></el-icon>
                  <span>男</span>
                </el-radio-button>
                <el-radio-button :label="2">
                  <el-icon><Female /></el-icon>
                  <span>女</span>
                </el-radio-button>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="省份" prop="province">
              <el-input 
                v-model="editUserForm.province" 
                placeholder="请输入省份"
                size="large"
                :prefix-icon="Location"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="城市" prop="city">
              <el-input 
                v-model="editUserForm.city" 
                placeholder="请输入城市"
                size="large"
                :prefix-icon="MapLocation"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="区县" prop="county">
              <el-input 
                v-model="editUserForm.county" 
                placeholder="请输入区县"
                size="large"
                :prefix-icon="Place"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handleEditUserDialogClose" size="large">取消</el-button>
          <el-button type="primary" @click="handleSaveUserProfile" :loading="savingUser" size="large">
            保存更改
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 编辑企业信息对话框 -->
    <el-dialog 
      v-model="editTenantDialogVisible" 
      title="编辑企业信息" 
      width="600px"
      :before-close="handleEditTenantDialogClose"
      class="edit-dialog"
    >
      <el-form 
        :model="editTenantForm" 
        :rules="editTenantRules" 
        ref="editTenantFormRef"
        label-width="120px"
        label-position="left"
        class="edit-form"
      >
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="企业图片" prop="image">
              <div class="avatar-upload-container">
                <el-upload
                  class="avatar-uploader"
                  :action="uploadAction"
                  :show-file-list="false"
                  :on-success="(res) => handleUploadSuccess(res, 'image', editTenantForm)"
                  :before-upload="beforeImageUpload"
                  :headers="uploadHeaders">
                  <div class="upload-content">
                    <img v-if="editTenantForm.image" :src="editTenantForm.image" class="avatar">
                    <div v-else class="upload-placeholder">
                      <el-icon size="40"><CameraFilled /></el-icon>
                      <div>点击上传企业图片</div>
                    </div>
                  </div>
                </el-upload>
                <div class="upload-tips">
                  <p>建议尺寸：200×200像素</p>
                  <p>支持 JPG/PNG 格式，不超过2MB</p>
                </div>
              </div>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="基本信息">
          <div class="form-section-divider"></div>
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="企业名称" prop="name">
              <el-input 
                v-model="editTenantForm.name" 
                placeholder="请输入企业名称"
                size="large"
                :prefix-icon="OfficeBuilding"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="联系人" prop="contactPerson">
              <el-input 
                v-model="editTenantForm.contactPerson" 
                placeholder="请输入联系人姓名"
                size="large"
                :prefix-icon="User"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="contactPhone">
              <el-input 
                v-model="editTenantForm.contactPhone" 
                placeholder="请输入联系电话"
                size="large"
                :prefix-icon="Phone"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="服务状态" prop="status">
              <el-select 
                v-model="editTenantForm.status" 
                placeholder="请选择服务状态"
                size="large"
                style="width: 100%"
              >
                <el-option label="启用" :value="1" />
                <el-option label="禁用" :value="0" />
              </el-select>
            </el-form-item>
          </el-col>
          <!-- <el-col :span="12" v-if="editTenantForm.expireAt">
            <el-form-item label="到期时间">
              <el-input 
                :value="formatTime(editTenantForm.expireAt)"
                disabled
                size="large"
                :prefix-icon="Clock"
              />
            </el-form-item>
          </el-col> -->
        </el-row>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handleEditTenantDialogClose" size="large">取消</el-button>
          <el-button type="primary" @click="handleSaveTenant" :loading="savingTenant" size="large">
            保存更改
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 日志详情对话框 -->
    <el-dialog 
      v-model="logDetailDialogVisible" 
      title="操作日志详情" 
      width="600px"
      class="log-detail-dialog"
    >
      <el-descriptions 
        :column="1" 
        border
        v-if="currentLogDetail"
        class="log-descriptions"
      >
        <el-descriptions-item label="操作时间">
          <div class="detail-time">
            <div>{{ formatDate(currentLogDetail.createdAt) }}</div>
            <div class="time-secondary">{{ formatTimeOnly(currentLogDetail.createdAt) }}</div>
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="操作模块">
          <el-tag :type="getModuleType(currentLogDetail.module)" size="small">
            {{ currentLogDetail.module || '无' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="操作类型">
          <span class="operation-type" :class="getOperationClass(currentLogDetail.operation)">
            {{ currentLogDetail.operation || '无' }}
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="操作描述">
          {{ currentLogDetail.description || '无' }}
        </el-descriptions-item>
        <el-descriptions-item label="目标ID">
          <span class="target-id-detail">{{ currentLogDetail.targetId || '无' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="IP地址">
          <span class="ip-address-detail">{{ currentLogDetail.ipAddress || '无' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="用户代理">
          <div class="user-agent">{{ currentLogDetail.userAgent || '无' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="请求参数" v-if="currentLogDetail.requestParams">
          <div class="json-container">
            <pre>{{ formatJson(currentLogDetail.requestParams) }}</pre>
          </div>
        </el-descriptions-item>
      </el-descriptions>
      
      <template #footer>
        <el-button @click="logDetailDialogVisible = false" size="large">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, getCurrentInstance } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  UserFilled, Edit, User, Phone, Iphone, Message, Location, 
  Male, Female, MapLocation, Calendar, OfficeBuilding, 
  CircleCheck, Histogram, ArrowRight, View, CameraFilled,
  Avatar, Place, Clock
} from '@element-plus/icons-vue'
import { get, post, takeAccessToken } from '@/net'

// 获取全局实例
const { proxy } = getCurrentInstance()

// 响应式数据
const loading = ref(false)
const savingUser = ref(false)
const savingTenant = ref(false)
const editUserDialogVisible = ref(false)
const editTenantDialogVisible = ref(false)
const logDetailDialogVisible = ref(false)

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

// 操作日志相关
const operationLogs = ref([])
const currentLogDetail = ref(null)

// 分页数据
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 排序数据
const sortData = reactive({
  prop: 'createdAt',
  order: 'descending'
})

// 编辑个人表单
const editUserForm = reactive({
  id: null,
  username: '',
  nickname: '',
  avatarUrl: '',
  phone: '',
  email: '',
  sex: 0,
  province: '',
  city: '',
  county: ''
})

// 编辑企业表单
const editTenantForm = reactive({
  id: null,
  name: '',
  image: '',
  status: 1,
  contactPerson: '',
  contactPhone: '',
  expireAt: null
})

// 表单引用
const editUserFormRef = ref()
const editTenantFormRef = ref()

// 表单验证规则
const editUserRules = {
  username: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ]
}

const editTenantRules = {
  name: [
    { required: true, message: '请输入企业名称', trigger: 'blur' }
  ],
  contactPerson: [
    { required: true, message: '请输入联系人', trigger: 'blur' }
  ],
  contactPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择服务状态', trigger: 'change' }
  ]
}

// 使用全局的 uploadAction
const uploadAction = computed(() => {
  return proxy.$uploadAction ? proxy.$uploadAction() : '/api/auth/common/upload'
})

// 上传 headers
const uploadHeaders = computed(() => {
  const token = takeAccessToken()
  return {
    'Authorization': token ? `Bearer ${token}` : ''
  }
})

// 计算属性
const isExpireSoon = computed(() => {
  if (!tenantInfo.value?.expireAt) return false
  const expireTime = new Date(tenantInfo.value.expireAt).getTime()
  const now = new Date().getTime()
  return expireTime - now < 30 * 24 * 60 * 60 * 1000 // 30天内到期
})

// 方法
const getRoleType = (role) => {
  const roleMap = {
    'ADMIN': 'danger',
    'SUPER_ADMIN': 'warning',
    'USER': 'primary'
  }
  return roleMap[role] || 'primary'
}

const getRoleText = (role) => {
  const roleMap = {
    'ADMIN': '管理员',
    'USER': '普通用户',
    'SUPER_ADMIN': '超级管理员'
  }
  return roleMap[role] || '普通用户'
}

const getGenderText = (sex) => {
  const genderMap = { 0: '未知', 1: '男', 2: '女' }
  return genderMap[sex] || '未知'
}

const getTenantStatusText = (status) => {
  return status === 1 ? '正常' : '禁用'
}

const getModuleType = (module) => {
  const moduleMap = {
    '用户管理': 'primary',
    '系统设置': 'success',
    '权限管理': 'warning',
    '日志管理': 'info'
  }
  return moduleMap[module] || 'info'
}

const getOperationClass = (operation) => {
  const classMap = {
    '新增': 'operation-create',
    '修改': 'operation-update',
    '删除': 'operation-delete',
    '查询': 'operation-query',
    '登录': 'operation-login',
    '登出': 'operation-logout'
  }
  return classMap[operation] || ''
}

const formatTime = (timeString) => {
  if (!timeString) return ''
  try {
    const date = new Date(timeString)
    if (isNaN(date.getTime())) return timeString
    return `${date.getFullYear()}-${padZero(date.getMonth() + 1)}-${padZero(date.getDate())} ${padZero(date.getHours())}:${padZero(date.getMinutes())}`
  } catch (e) {
    return timeString
  }
}

const formatDate = (timeString) => {
  if (!timeString) return ''
  try {
    const date = new Date(timeString)
    if (isNaN(date.getTime())) return ''
    return `${date.getFullYear()}-${padZero(date.getMonth() + 1)}-${padZero(date.getDate())}`
  } catch (e) {
    return ''
  }
}

const formatTimeOnly = (timeString) => {
  if (!timeString) return ''
  try {
    const date = new Date(timeString)
    if (isNaN(date.getTime())) return ''
    return `${padZero(date.getHours())}:${padZero(date.getMinutes())}:${padZero(date.getSeconds())}`
  } catch (e) {
    return ''
  }
}

const padZero = (num) => {
  return num < 10 ? `0${num}` : num
}

// 格式化JSON字符串
const formatJson = (jsonString) => {
  try {
    const jsonObj = JSON.parse(jsonString)
    return JSON.stringify(jsonObj, null, 2)
  } catch (e) {
    return jsonString
  }
}

// 表格行样式
const tableRowClassName = ({ rowIndex }) => {
  return rowIndex % 2 === 1 ? 'even-row' : ''
}

// ============ 通用上传方法 ============
// 图片上传成功（通用方法）
const handleUploadSuccess = (response, fieldName, form) => {
  if (response && response.code === 0) {
    form[fieldName] = response.data || ''
    ElMessage.success('图片上传成功')
  } else if (response && response.data) {
    // 如果接口直接返回图片URL
    form[fieldName] = response.data
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(response?.message || '图片上传失败')
  }
}

// 图片上传前校验（通用方法）
const beforeImageUpload = (file) => {
  const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 100

  if (!isJpgOrPng) {
    ElMessage.error('图片只能是 JPG/PNG 格式!')
  }
  
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 100MB!')
  }

  return isJpgOrPng && isLt2M
}

// ============ 数据加载 ============
// 加载用户信息
const loadUserInfo = async () => {
  try {
    const res = await get('/api/auth/user/info')
    if (res) {
      userInfo.value = {
        id: res.id || null,
        username: res.username || '',
        nickname: res.nickname || '',
        avatarUrl: res.avatarUrl || '',
        phone: res.phone || '',
        email: res.email || '',
        sex: res.sex || 0,
        province: res.province || '',
        city: res.city || '',
        county: res.county || '',
        role: res.role || 'USER',
        registerTime: res.registerTime || ''
      }
    }
  } catch (e) {
    console.error('加载用户信息失败:', e)
    ElMessage.error('加载用户信息失败')
  }
}

// 加载租户信息
const loadTenantInfo = async () => {
  try {
    const res = await get('/api/auth/tenant/info')
    tenantInfo.value = res || null
  } catch (e) {
    console.error('加载租户信息失败:', e)
  }
}

// 加载操作日志
const loadOperationLogs = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.current,
      size: pagination.size
    }
    
    const res = await post('/api/auth/operationLog/pageList', params)
    
    operationLogs.value = res.records || []
    pagination.total = res.total || 0
   
  } catch (e) {
    console.error('加载操作记录失败:', e)
    ElMessage.error('加载操作记录失败')
    operationLogs.value = []
  } finally {
    loading.value = false
  }
}

// ============ 分页处理 ============
const handleSizeChange = (size) => {
  pagination.size = size
  pagination.current = 1 // 重置到第一页
  loadOperationLogs()
}

const handleCurrentChange = (current) => {
  pagination.current = current
  loadOperationLogs()
}

// 排序处理
const handleSortChange = ({ prop, order }) => {
  if (prop && order) {
    sortData.prop = prop
    sortData.order = order
  }
}

// 查看日志详情
const viewLogDetail = (log) => {
  currentLogDetail.value = log
  logDetailDialogVisible.value = true
}

// ============ 个人信息编辑 ============
const handleEditProfile = () => {
  // 填充编辑表单数据
  Object.assign(editUserForm, {
    id: userInfo.value.id || null,
    username: userInfo.value.username || '',
    nickname: userInfo.value.nickname || '',
    avatarUrl: userInfo.value.avatarUrl || '',
    phone: userInfo.value.phone || '',
    email: userInfo.value.email || '',
    sex: userInfo.value.sex || 0,
    province: userInfo.value.province || '',
    city: userInfo.value.city || '',
    county: userInfo.value.county || ''
  })
  editUserDialogVisible.value = true
}

const handleEditUserDialogClose = () => {
  editUserDialogVisible.value = false
  if (editUserFormRef.value) {
    editUserFormRef.value.resetFields()
  }
}

const handleSaveUserProfile = async () => {
  if (!editUserFormRef.value) return
  
  try {
    await editUserFormRef.value.validate()
    savingUser.value = true
    
    const updateData = {
      id: editUserForm.id,
      username: editUserForm.username || '',
      nickname: editUserForm.nickname || '',
      phone: editUserForm.phone || '',
      email: editUserForm.email || '',
      sex: editUserForm.sex || 0,
      avatarUrl: editUserForm.avatarUrl || ''
    }
    
    if (editUserForm.province || editUserForm.city || editUserForm.county) {
      updateData.province = editUserForm.province || ''
      updateData.city = editUserForm.city || ''
      updateData.county = editUserForm.county || ''
    }
    
    const res = await post('/api/auth/user/updateUserInfo', updateData)
    console.log('更新用户信息响应:', res)
    if (res) {
      ElMessage.success('个人资料更新成功')
      editUserDialogVisible.value = false
      await loadUserInfo()
    } else {
      ElMessage.error(res.message || '个人资料更新失败')
    }
  } catch (e) {
    if (e.errors) {
      return
    }
    ElMessage.error('个人资料更新失败: ' + (e.message || '未知错误'))
  } finally {
    savingUser.value = false
  }
}

// ============ 企业信息编辑 ============
const handleEditTenant = () => {
  if (!tenantInfo.value || !tenantInfo.value.bossAuth) {
    ElMessage.warning('您没有权限编辑企业信息')
    return
  }
  
  // 填充企业编辑表单数据
  Object.assign(editTenantForm, {
    id: tenantInfo.value.id || null,
    name: tenantInfo.value.name || '',
    image: tenantInfo.value.image || '',
    status: tenantInfo.value.status || 1,
    contactPerson: tenantInfo.value.contactPerson || '',
    contactPhone: tenantInfo.value.contactPhone || '',
    expireAt: tenantInfo.value.expireAt || null
  })
  editTenantDialogVisible.value = true
}

const handleEditTenantDialogClose = () => {
  editTenantDialogVisible.value = false
  if (editTenantFormRef.value) {
    editTenantFormRef.value.resetFields()
  }
}

const handleSaveTenant = async () => {
  if (!editTenantFormRef.value) return
  
  try {
    await editTenantFormRef.value.validate()
    savingTenant.value = true
    
    const updateData = {
      id: editTenantForm.id,
      name: editTenantForm.name || '',
      image: editTenantForm.image || '',
      status: editTenantForm.status || 1,
      contactPerson: editTenantForm.contactPerson || '',
      contactPhone: editTenantForm.contactPhone || ''
    }
    
    const res = await post('/api/auth/tenant/update', updateData)
    console.log('更新企业信息响应:', res)
    if (res) {
      ElMessage.success('企业信息更新成功')
      editTenantDialogVisible.value = false
      await loadTenantInfo()
    } else {
      ElMessage.error(res.message || '企业信息更新失败')
    }
  } catch (e) {
    if (e.errors) {
      return
    }
    ElMessage.error('企业信息更新失败: ' + (e.message || '未知错误'))
  } finally {
    savingTenant.value = false
  }
}

const viewAllOperations = () => {
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
  padding: 24px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
  min-height: calc(100vh - 60px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
  padding: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #409EFF 0%, #67C23A 100%);
  border-radius: 12px;
  color: white;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  margin: 0;
  background: linear-gradient(135deg, #303133 0%, #606266 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.page-subtitle {
  font-size: 14px;
  color: #909399;
  margin: 4px 0 0;
}

.page-actions .el-button {
  height: 44px;
  padding: 0 24px;
  border-radius: 8px;
  font-weight: 500;
}

.profile-content {
  max-width: 1400px;
  margin: 0 auto;
}

.info-row {
  display: flex;
  flex-wrap: wrap;
  margin: 0 -12px 24px;
}

.info-col {
  padding: 0 12px;
  margin-bottom: 24px;
}

.log-row {
  margin: 0 -12px;
}

.log-col {
  padding: 0 12px;
}

.user-card,
.tenant-card,
.operation-log-card {
  border-radius: 12px;
  border: none;
  height: 100%;
  transition: all 0.3s ease;
}

.user-card:hover,
.tenant-card:hover,
.operation-log-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.12) !important;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px !important;
  border-bottom: 1px solid #f0f0f0;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-icon {
  color: #409EFF;
  font-size: 20px;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.card-header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.edit-tenant-btn {
  padding: 4px 8px;
  font-size: 12px;
}

.boss-tag {
  margin-top: 4px;
}

/* 企业信息样式 */
.tenant-info {
  padding: 20px;
}

.company-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
  padding: 20px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 12px;
}

.company-logo {
  width: 80px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #409EFF 0%, #67C23A 100%);
  border-radius: 16px;
  color: white;
  flex-shrink: 0;
  overflow: hidden;
}

.company-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.company-details {
  flex: 1;
}

.company-name {
  font-size: 22px;
  font-weight: 700;
  color: #303133;
  margin: 0 0 8px;
}

.company-contact {
  font-size: 16px;
  color: #606266;
  margin: 0;
}

.company-info-grid {
  display: grid;
  gap: 16px;
}

.expire-soon {
  color: #E6A23C;
  font-weight: 500;
}

/* 用户信息样式 */
.user-info {
  padding: 20px;
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px;
  margin-bottom: 24px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 12px;
}

.avatar-wrapper {
  position: relative;
  flex-shrink: 0;
}

.user-avatar {
  border: 4px solid #fff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
}

.user-avatar:hover {
  transform: scale(1.05);
}

.avatar-status {
  position: absolute;
  bottom: 0;
  right: 0;
  background: white;
  padding: 4px 8px;
  border-radius: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 4px;
}

.status-dot {
  width: 8px;
  height: 8px;
  background: #67C23A;
  border-radius: 50%;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% { opacity: 1; }
  50% { opacity: 0.5; }
  100% { opacity: 1; }
}

.status-text {
  font-size: 12px;
  color: #67C23A;
  font-weight: 500;
}

.user-names {
  flex: 1;
}

.user-name {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
  margin: 0 0 8px;
}

.user-realname {
  font-size: 18px;
  color: #606266;
  margin: 0;
}

.basic-info {
  padding: 0 8px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

@media (max-width: 992px) {
  .info-grid {
    grid-template-columns: 1fr;
  }
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 10px;
  transition: all 0.3s ease;
}

.info-item:hover {
  background: #edf2f7;
  transform: translateX(4px);
}

.item-label {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #606266;
  font-size: 14px;
  font-weight: 500;
}

.item-label .el-icon {
  color: #409EFF;
}

.item-value {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  text-align: right;
}

/* 操作日志样式 */
.log-table-container {
  border-radius: 8px;
  overflow: hidden;
}

.log-table {
  --el-table-border-color: #f0f0f0;
  --el-table-header-bg-color: #f8f9fa;
  --el-table-row-hover-bg-color: #f8f9fa;
}

:deep(.log-table .el-table__row.even-row) {
  background-color: #fafbfc;
}

.time-cell {
  display: flex;
  flex-direction: column;
}

.time-date {
  font-weight: 500;
  color: #303133;
}

.time-time {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.operation-type {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.operation-create {
  background: #e7f4e9;
  color: #67C23A;
}

.operation-update {
  background: #e8f4fd;
  color: #409EFF;
}

.operation-delete {
  background: #fdeeee;
  color: #F56C6C;
}

.operation-query {
  background: #f0f9ff;
  color: #409EFF;
}

.operation-login {
  background: #e8f4fd;
  color: #409EFF;
}

.operation-logout {
  background: #f0f9ff;
  color: #909399;
}

.description-cell {
  line-height: 1.6;
}

.target-id {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.ip-address {
  font-family: 'Courier New', monospace;
  font-size: 13px;
  color: #606266;
}

.detail-btn {
  color: #409EFF;
  transition: all 0.3s ease;
}

.detail-btn:hover {
  color: #67C23A;
  transform: scale(1.1);
}

/* 分页样式 */
.pagination-container {
  margin-top: 24px;
  padding: 20px 0 0;
  border-top: 1px solid #f0f0f0;
}

.custom-pagination {
  justify-content: center;
}

/* 编辑对话框样式 */
.edit-dialog :deep(.el-dialog__header) {
  padding: 24px 24px 16px;
  border-bottom: 1px solid #f0f0f0;
  margin: 0;
}

.edit-dialog :deep(.el-dialog__body) {
  padding: 24px;
}

.edit-dialog :deep(.el-dialog__footer) {
  padding: 16px 24px 24px;
  border-top: 1px solid #f0f0f0;
}

.avatar-upload-container {
  display: flex;
  align-items: flex-start;
  gap: 24px;
}

.avatar-uploader {
  flex-shrink: 0;
}

.avatar-uploader :deep(.el-upload) {
  border: 2px dashed var(--el-border-color);
  border-radius: 12px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
  width: 140px;
  height: 140px;
}

.avatar-uploader :deep(.el-upload:hover) {
  border-color: var(--el-color-primary);
  transform: translateY(-2px);
}

.upload-content {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.upload-placeholder {
  text-align: center;
  color: #909399;
}

.upload-placeholder .el-icon {
  margin-bottom: 8px;
  color: #c0c4cc;
}

.upload-tips p {
  margin: 4px 0;
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
}

.form-section-divider {
  height: 1px;
  background: linear-gradient(90deg, transparent, #e0e0e0, transparent);
  margin: 12px 0;
}

.gender-group {
  width: 100%;
}

.gender-group :deep(.el-radio-button) {
  flex: 1;
}

.gender-group :deep(.el-radio-button__inner) {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 12px 8px;
  width: 100%;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 日志详情对话框 */
.log-detail-dialog :deep(.el-dialog__body) {
  padding: 20px 24px;
}

.log-descriptions :deep(.el-descriptions__label) {
  font-weight: 500;
  color: #606266;
  width: 100px;
}

.log-descriptions :deep(.el-descriptions__content) {
  color: #303133;
}

.detail-time {
  display: flex;
  flex-direction: column;
}

.time-secondary {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.target-id-detail {
  font-family: 'Courier New', monospace;
  font-weight: 500;
}

.ip-address-detail {
  font-family: 'Courier New', monospace;
  color: #606266;
}

.user-agent {
  word-break: break-all;
  font-size: 13px;
  color: #606266;
}

.json-container {
  background: #f8f9fa;
  border-radius: 6px;
  padding: 12px;
  max-height: 300px;
  overflow-y: auto;
  border: 1px solid #e4e7ed;
}

.json-container pre {
  margin: 0;
  white-space: pre-wrap;
  word-wrap: break-word;
  font-family: 'SF Mono', Monaco, Consolas, monospace;
  font-size: 12px;
  line-height: 1.5;
  color: #2c3e50;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .profile-container {
    padding: 16px;
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .page-actions {
    width: 100%;
  }
  
  .page-actions .el-button {
    width: 100%;
  }
  
  .info-col {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .avatar-upload-container {
    flex-direction: column;
  }
  
  .upload-tips {
    text-align: center;
  }
  
  .company-header {
    flex-direction: column;
    text-align: center;
  }
  
  .avatar-section {
    flex-direction: column;
    text-align: center;
  }
  
  .info-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .item-value {
    text-align: left;
  }
  
  .gender-group :deep(.el-radio-button) {
    margin-bottom: 8px;
  }
  
  .card-header-right {
    flex-direction: column;
    align-items: flex-end;
  }
}
</style>