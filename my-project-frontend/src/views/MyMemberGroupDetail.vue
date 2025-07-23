<template>
  <div class="member-group-container">
    <el-button @click="goBack" class="back-button">返回</el-button>

    <el-card class="member-group-card">
      <div class="header">
        <h2>{{ projectName }}</h2>
        <div class="header-actions">
          <el-button 
            v-if="isAdmin"
            type="primary" 
            @click="showAddMemberDialog"
          >
            <el-icon><Plus /></el-icon> 添加成员
          </el-button>
        </div>
      </div>

      <el-divider />

      <el-table 
        :data="memberList" 
        v-loading="loading"
        style="width: 100%"
      >
        <el-table-column label="头像" width="80">
          <template #default="{ row }">
            <el-avatar :src="row.avatarUrl || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
          </template>
        </el-table-column>

        <el-table-column prop="nickname" label="昵称" width="150" />
         <el-table-column prop="sexName" label="性别" width="100" />
        <el-table-column prop="statusName" label="状态" width="100" />

        <el-table-column label="角色" width="120">
          <template #default="{ row }">
            <el-tag :type="getRoleTagType(row.roleOfMemberGroup)">
              {{ formatRole(row.roleOfMemberGroup) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="email" label="邮箱" width="200"/>
        <el-table-column label="地区">
          <template #default="{ row }">
            {{ row.province || '' }}{{ row.city || '' }}
          </template>
        </el-table-column>

        <el-table-column label="加入时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="220" v-if="isAdmin">
          <template #default="{ row }">
            <el-button 
              v-if="row.roleOfMemberGroup !== 'CREATOR'"
              type="danger" 
              size="small"
              @click="handleRemoveMember(row)"
            >
              移除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="addMemberDialogVisible" title="添加成员" width="500px">
      <el-form :model="addMemberForm" label-width="80px">
        <el-form-item label="搜索用户">
          <el-input 
            v-model="addMemberForm.keyword" 
            placeholder="输入用户昵称或用户名"
            @keyup.enter="searchUser"
          >
            <template #append>
              <el-button @click="searchUser">
                <el-icon><Search /></el-icon>
              </el-button>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="选择角色" v-if="searchResult.length > 0">
          <el-select v-model="addMemberForm.role" placeholder="请选择角色">
            <el-option label="普通成员" value="0" />
            <el-option label="组长" value="1" />
          </el-select>
        </el-form-item>
      </el-form>

      <div class="search-result" v-if="searchResult.length > 0">
        <div class="result-title">搜索结果：</div>
        <div 
          v-for="user in searchResult" 
          :key="user.id" 
          class="user-item"
          @click="selectUser(user)"
          :class="{ selected: user.selected }"
        >
          <el-avatar :src="user.avatarUrl || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
          <div class="user-info">
            <div class="nickname">{{ user.nickname || '未设置昵称' }}</div>
            <div class="username">@{{ user.username }}</div>
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="addMemberDialogVisible = false">取消</el-button>
        <el-button 
          type="primary" 
          @click="confirmAddMember"
          :disabled="!selectedUser || !addMemberForm.role"
        >
          确认添加
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { get, post } from '@/net'

const route = useRoute()
const router = useRouter()

const projectName = ref('项目名称')
const memberList = ref([])
const loading = ref(false)
const currentUserRole = ref('')
const isAdmin = ref(false)
const addMemberDialogVisible = ref(false)
const addMemberForm = ref({
  keyword: '0',
  role: '普通成员'
})
const searchResult = ref([])
const selectedUser = ref(null)
const projectId = ref(null)

onMounted(() => {
  projectId.value = route.params.id
  loadProjectInfo()
  loadMemberList()
})

const loadProjectInfo = async () => {
  try {
    const res = await get(`/api/unauth/project/detail?projectId=${projectId.value}`)
    projectName.value = res.name || '项目名称'
  } catch (error) {
    ElMessage.error('加载项目信息失败')
  }
}

const loadMemberList = async () => {
  try {
    loading.value = true
    const res = await get(`/api/auth/projectMember/memberList?projectId=${projectId.value}`)
    memberList.value = res.members || []
    currentUserRole.value = res.currentUserRole || ''
    isAdmin.value = ['管理员', '组长'].includes(currentUserRole.value)

    console.log('当前用户角色：', currentUserRole.value)
console.log('是否管理员：', isAdmin.value)
  } catch (error) {
    ElMessage.error('加载成员列表失败')
  } finally {
    loading.value = false
  }
}

const formatRole = (role) => ({
  CREATOR: '创建者',
  ADMIN: '管理员',
  MEMBER: '成员'
}[role] || role)

const getRoleTagType = (role) => ({
  CREATOR: 'danger',
  ADMIN: 'warning',
  MEMBER: 'success'
}[role] || '')

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString()
}

const goBack = () => {
  router.go(-1)
}

const showAddMemberDialog = () => {
  addMemberDialogVisible.value = true
  addMemberForm.value = {
    keyword: '',
    role: '0'
  }
  searchResult.value = []
  selectedUser.value = null
}

const searchUser = async () => {
  if (!addMemberForm.value.keyword.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }
  try {
    const res = await post('/api/auth/user/searchUser', {
      username:addMemberForm.value.keyword
    })

    searchResult.value = res || []
    if (searchResult.value.length === 0) {
      ElMessage.info('未找到匹配的用户')
    }
  } catch (error) {
    ElMessage.error('搜索用户失败')
  }
}

const selectUser = (user) => {
  selectedUser.value = user
  searchResult.value = searchResult.value.map(u => ({
    ...u,
    selected: u.id === user.id
  }))
}

const confirmAddMember = async () => {
  if (!selectedUser.value) {
    ElMessage.warning('请先选择要添加的用户')
    return
  }
  try {
    await post('/api/auth/projectMember/addMemberByManager', {
      projectId: projectId.value,
      userId: selectedUser.value.id,
      role: addMemberForm.value.role
    })
    ElMessage.success('添加成员成功')
    addMemberDialogVisible.value = false
    loadMemberList()
  } catch (error) {
    ElMessage.error(error.message || '添加成员失败')
  }
}

const handleRemoveMember = (member) => {
  ElMessageBox.confirm(
    `确定要移除成员 ${member.nickname || member.username} 吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    await post('/api/auth/projectMember/removeMember', {
        projectId:projectId.value,
        memberId: member.id,
        status:1
      })
      ElMessage.success('移除成员成功')
      loadMemberList()
  })
}

</script>

<style scoped>
.member-group-container {
  padding: 30px;
  max-width: 1200px;
  margin: 0 auto;
  background: #f7f9fc;
}

.back-button {
  position: absolute;
  left: 30px;
  top: 30px;
  z-index: 1000;
}

.member-group-card {
  background-color: #fff;
  padding: 30px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  border-radius: 16px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.search-result {
  margin-top: 20px;
  max-height: 300px;
  overflow-y: auto;
}

.result-title {
  font-weight: bold;
  margin-bottom: 10px;
}

.user-item {
  display: flex;
  align-items: center;
  padding: 10px;
  border-radius: 4px;
  cursor: pointer;
  margin-bottom: 5px;
  transition: background-color 0.3s;
}

.user-item:hover {
  background-color: #f5f7fa;
}

.user-item.selected {
  background-color: #ecf5ff;
}

.user-info {
  margin-left: 10px;
}

.nickname {
  font-weight: 500;
}

.username {
  font-size: 12px;
  color: #909399;
}

@media screen and (max-width: 768px) {
  .member-group-container {
    padding: 15px;
  }

  .member-group-card {
    padding: 15px;
  }

  .header {
    flex-direction: column;
    align-items: flex-start;
  }

  .header-actions {
    margin-top: 10px;
    width: 100%;
  }
}
</style>
