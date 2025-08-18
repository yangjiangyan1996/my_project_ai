<template>
  <div class="member-group-container">
    <el-button @click="goBack" class="back-button">返回</el-button>

    <el-card class="member-group-card">
      <div class="header-card">
        <div class="header-content">
          <h2 class="project-title">{{ projectName }}</h2>
          <div class="meta-info">
            <span class="member-count">
              <el-icon><User /></el-icon> {{ memberList.length }} 位成员
            </span>
          </div>
        </div>
        <div class="header-actions">
            <el-button 
              v-if="isAdmin && !projectFinished"
              type="warning" 
              @click="confirmFinishProject"
              class="action-button finish-button"
            >
              <el-icon><Finished /></el-icon> 完结项目
            </el-button>

          <el-button 
            v-if="isAdmin"
            type="primary" 
            @click="showAddMemberDialog"
            class="action-button"
          >
            <el-icon><Plus /></el-icon> 添加成员
          </el-button>

          <!-- 新增群聊按钮 -->
          <el-button 
            type="success" 
            @click="showGroupChat"
            class="action-button chat-button"
          >
            <el-icon><ChatDotRound /></el-icon> 群聊
          </el-button>
        </div>
      </div>

      <el-divider />

      <el-table 
        :data="memberList" 
        v-loading="loading"
        style="width: 100%"
        class="modern-table"
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

       <el-table-column label="操作" width="220" v-if="isAdmin || projectFinished">
          <template #default="{ row }">
            <el-button 
              v-if="projectFinished && (isAdmin || row.roleOfMemberGroup === 'ADMIN' || row.roleOfMemberGroup === 'CREATOR')"
              type="info" 
              size="small"
              @click="showEvaluationDialog(row)"
              class="evaluation-button"
            >
              <el-icon><Edit /></el-icon> 评价
            </el-button>
            
            <el-button 
              v-if="isAdmin && !projectFinished && row.roleOfMemberGroup !== 'CREATOR'"
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

    
    <el-dialog 
      v-model="addMemberDialogVisible" 
      title="添加成员" 
      width="500px"
      class="modern-dialog"
      >
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



<!-- 评价对话框 -->
<el-dialog 
  v-model="evaluationDialogVisible" 
  :title="`评价 ${evaluationTarget.nickname || evaluationTarget.username}`"
  width="700px"
  class="modern-dialog evaluation-dialog"
>
  <div class="evaluation-content">
    <div class="evaluation-header">
      <el-avatar :size="60" :src="evaluationTarget.avatarUrl" />
      <div class="evaluation-user-info">
        <h3>{{ evaluationTarget.nickname || evaluationTarget.username }}</h3>
        <div class="user-role">
          <el-tag :type="getRoleTagType(evaluationTarget.roleOfMemberGroup)">
            {{ formatRole(evaluationTarget.roleOfMemberGroup) }}
          </el-tag>
        </div>
      </div>
    </div>
    
    <el-divider />
    
    <el-form :model="evaluationForm" label-width="100px">
      <el-form-item label="评分">
        <el-rate
          v-model="evaluationForm.rating"
          :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
          :max="5"
          show-text
          text-color="#ff9900"
          :texts="['差', '一般', '不错', '很好', '优秀']"
        />
      </el-form-item>
      
      <el-form-item label="评价内容">
        <el-input
          v-model="evaluationForm.comment"
          type="textarea"
          :rows="4"
          placeholder="请输入详细评价内容"
          maxlength="300"
          show-word-limit
        />
      </el-form-item>
      
      <el-form-item label="匿名评价" v-if="!isAdmin">
        <el-switch v-model="evaluationForm.anonymous" />
      </el-form-item>
    </el-form>
  </div>
  
  <template #footer>
    <el-button @click="evaluationDialogVisible = false">取消</el-button>
    <el-button 
      type="primary" 
      @click="submitEvaluation"
      :loading="submittingEvaluation"
    >
      提交评价
    </el-button>
  </template>
</el-dialog>
    
      <ChatDialog 
        v-model="groupChatVisible"
        :current-user="currentUser"
        :target-user="targetUser"
        :chat-conversation-id="currentChatId"
        :chat-type="chatType"
      />

  </div>


  <el-dialog
    v-model="evaluationPromptVisible"
    title="项目评价提醒"
    width="500px"
    class="modern-dialog"
    :close-on-click-modal="false"
  >
    <div class="evaluation-prompt-content">
      <el-icon class="prompt-icon"><MessageBox /></el-icon>
      <h3>该项目已完结，请对团队成员进行评价</h3>
      <p>您的评价将帮助团队成员更好地成长和改进</p>
    </div>
    <template #footer>
      <el-button @click="handleLaterEvaluation">稍后前往</el-button>
      <el-button 
        type="primary" 
        @click="handleGoEvaluation"
        class="go-evaluate-btn"
      >
        立即前往
      </el-button>
    </template>
  </el-dialog>

  <div 
    v-if="showEvaluationReminder && !evaluationPromptVisible"
    class="evaluation-reminder"
    @click="handleGoEvaluation"
  >
    <el-icon><Bell /></el-icon>
    <span>您有待完成的项目评价</span>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search,ChatDotRound,Finished ,Edit,MessageBox, Bell} from '@element-plus/icons-vue'

import { get, post } from '@/net'
import ChatDialog from '@/components/ChatDialog.vue'

import useUserInfo from '@/hooks/useUserInfo';
const { state: userInfo, loadUserInfo } = useUserInfo();


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

// 新增群聊相关状态
const chatVisible = ref(false);
const showTooltip = ref(false);
const groupChatVisible = ref(false);
const currentChatId = ref(null); // 修改为从API获取
const chatType = ref(2); // 2表示群聊类型
const currentUser = ref({});
const targetUser = ref({});


// 完结评价
const projectFinished = ref(false)
const finishProjectDialogVisible = ref(false)
const finishingProject = ref(false)
const finishProjectForm = ref({
  summary: ''
})

const evaluationDialogVisible = ref(false)
const evaluationTarget = ref({})
const evaluationForm = ref({
  rating: 5,
  comment: '',
  anonymous: false
})
const submittingEvaluation = ref(false)

// 新增状态变量
const evaluationPromptVisible = ref(false)
const showEvaluationReminder = ref(false)
const hasEvaluated = ref(false)

onMounted(() => {
    projectId.value = route.params.id
    if (!userInfo.data.id) {
      loadUserInfo().then(() => {
        getConversationId()
        loadProjectInfo()
        loadMemberList()
      });
    } else {
      getConversationId()
      loadProjectInfo()
      loadMemberList()
    }
 
})

// 添加完结项目方法
const confirmFinishProject = () => {
  ElMessageBox.confirm(
    '确定要完结该项目吗? 完结后将无法再进行项目操作',
    '完结项目确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      finishingProject.value = true
      await post('/api/auth/project/closed', {
        projectId: projectId.value
      })
      ElMessage.success('项目已成功完结')
      // 重新加载项目信息以更新状态
      loadProjectInfo()
    } catch (error) {
      ElMessage.error(error.message || '完结项目失败')
    } finally {
      finishingProject.value = false
    }
  }).catch(() => {
    // 用户取消操作
  })
}

const showEvaluationDialog = (member) => {
  evaluationTarget.value = member
  evaluationForm.value = {
    rating: 5,
    comment: '',
    anonymous: false
  }
  evaluationDialogVisible.value = true
}


// 修改loadProjectInfo方法
const loadProjectInfo = async () => {
  try {
    const res = await get(`/api/unauth/project/detail?projectId=${projectId.value}`)
    projectName.value = res.name || '项目名称'
    projectFinished.value = res.status === 30 // 根据新接口返回的status判断
    
    // 如果项目已完结，检查用户是否已完成评价
    if (projectFinished.value) {
      checkEvaluationStatus()
    }
  } catch (error) {
    ElMessage.error('加载项目信息失败')
  }
}



// 新增方法：处理立即前往评价
const handleGoEvaluation = () => {
  evaluationPromptVisible.value = false
  // 找到第一个未评价的成员（这里简化处理，实际可以根据业务需求调整）
  const firstMember = memberList.value.find(member => 
    member.id !== userInfo.data?.id // 通常不需要评价自己
  )
  
  if (firstMember) {
    showEvaluationDialog(firstMember)
  } else {
    ElMessage.warning('没有可评价的成员')
  }
}

// 新增方法：处理稍后前往
const handleLaterEvaluation = () => {
  evaluationPromptVisible.value = false
  showEvaluationReminder.value = true
}

// 修改submitEvaluation方法
const submitEvaluation = async () => {
  try {
    console.log('评价目标:', evaluationTarget.value)
    submittingEvaluation.value = true
    const success = await post('/api/auth/project/evaluate', {
      projectId: projectId.value,
      toUserId: evaluationTarget.value.userId,
      comment: evaluationForm.value.comment,
      score: evaluationForm.value.rating
    })
    
    if (success) {
      ElMessage.success('评价提交成功')
      evaluationDialogVisible.value = false
      hasEvaluated.value = true
      showEvaluationReminder.value = false
      
      // 检查是否还有未评价的成员
      await checkEvaluationStatus()
    } else {
      ElMessage.error('评价提交失败')
    }
  } catch (error) {
    ElMessage.error(error.message || '提交评价失败')
  } finally {
    submittingEvaluation.value = false
  }
}

// 新增方法：检查评价状态
const checkEvaluationStatus = async () => {
  try {
    const res = await get(`/api/auth/project/hasEvaluate?projectId=${projectId.value}`)
    hasEvaluated.value = res
    
    // 如果未评价，显示提示框
    if (!hasEvaluated.value) {
      evaluationPromptVisible.value = true
    }
  } catch (error) {
    ElMessage.error('检查评价状态失败')
  }
}

const getConversationId = async () => {
  try {
    const res = await get(`/api/auth/chat/getChatIdByProjectId?projectId=${projectId.value}`)
    if (res) {
      console.log('获取到的会话ID:', res)
      currentChatId.value = res
    } else {
      ElMessage.warning('获取会话ID失败，将使用项目ID作为替代')
      currentChatId.value = projectId.value
    }
  } catch (error) {
    console.error('获取会话ID失败:', error)
  }
}


const showGroupChat = () => {
  if (!currentChatId.value) {
    ElMessage.warning('正在获取会话信息，请稍后...')
    return
  }
  // 设置用户信息
  currentUser.value = {
    id: userInfo.data?.id,
    username: userInfo.data?.username,
    avatarUrl: userInfo.data?.avatarUrl
  }

  targetUser.value = {};
  chatVisible.value = true;
  groupChatVisible.value = true
  
  // 添加打开动画效果
  setTimeout(() => {
    const chatBtn = document.querySelector('.chat-button')
    if (chatBtn) {
      chatBtn.classList.add('animate-pulse')
      setTimeout(() => chatBtn.classList.remove('animate-pulse'), 1000)
    }
  }, 50)
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

/* 现代卡片样式 */
.header-card {
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 24px;
  border-radius: 12px;
  margin-bottom: 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.project-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: #2d3748;
  margin: 0;
}

.meta-info {
  display: flex;
  gap: 16px;
  margin-top: 8px;
  color: #4a5568;
}

.member-count {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 现代表格样式 */
.modern-table {
  --el-table-border-color: transparent;
  --el-table-header-bg-color: #f8fafc;
}

.modern-table :deep(.el-table__row) {
  transition: all 0.3s ease;
}

.modern-table :deep(.el-table__row:hover) {
  transform: translateY(-2px);
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

/* 对话框样式 */
.modern-dialog {
  border-radius: 12px;
  overflow: hidden;
}

.modern-dialog :deep(.el-dialog__header) {
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  margin-right: 0;
}

.modern-dialog :deep(.el-dialog__body) {
  padding: 20px;
}

/* 用户搜索结果项动画 */
.user-item {
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
}

.user-item:hover {
  transform: translateX(4px);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.12);
}

/* 返回按钮样式 */
.back-button {
  position: absolute;
  left: 30px;
  top: 30px;
  z-index: 1000;
  background: white;
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.back-button:hover {
  transform: translateX(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

/* 按钮样式增强 */
.action-button {
  padding: 10px 16px;
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
}

.chat-button {
  background: linear-gradient(135deg, #4ade80 0%, #22d3ee 100%);
  border: none;
  color: white;
  box-shadow: 0 2px 10px rgba(74, 222, 128, 0.3);
}

.chat-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(74, 222, 128, 0.4);
}

/* 按钮间距调整 */
.header-actions {
  display: flex;
  gap: 12px;
}

@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.05); }
  100% { transform: scale(1); }
}

.animate-pulse {
  animation: pulse 0.5s ease;
}


/* 完结按钮样式 */
.finish-button {
  background: linear-gradient(135deg, #f6ad55 0%, #f687b3 100%);
  border: none;
  color: white;
  box-shadow: 0 2px 10px rgba(246, 173, 85, 0.3);
}

.finish-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(246, 173, 85, 0.4);
}

/* 评价对话框样式 */
.evaluation-dialog :deep(.el-dialog__body) {
  padding: 20px 25px;
}

.evaluation-header {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.evaluation-user-info {
  margin-left: 15px;
}

.evaluation-user-info h3 {
  margin: 0;
  font-size: 1.2rem;
  color: #2d3748;
}

.user-role {
  margin-top: 5px;
}

/* 评价按钮样式 */
.evaluation-button {
  background: linear-gradient(135deg, #a0aec0 0%, #718096 100%);
  border: none;
  color: white;
}

.evaluation-button:hover {
  background: linear-gradient(135deg, #718096 0%, #4a5568 100%);
}

/* 项目完结状态提示 */
.project-status-tag {
  margin-left: 10px;
  vertical-align: middle;
}

/* 动画效果 */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.evaluation-content {
  animation: fadeInUp 0.4s ease;
}

.finish-project-content {
  animation: fadeInUp 0.4s ease;
}

.mb-4 {
  margin-bottom: 1rem;
}

/* 完结按钮样式 */
.finish-button {
  background: linear-gradient(135deg, #f6ad55 0%, #f687b3 100%);
  border: none;
  color: white;
  box-shadow: 0 2px 10px rgba(246, 173, 85, 0.3);
}

.finish-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(246, 173, 85, 0.4);
}

/* 评价提示框内容 */
.evaluation-prompt-content {
  text-align: center;
  padding: 20px;
}

.prompt-icon {
  font-size: 60px;
  color: #f6ad55;
  margin-bottom: 15px;
}

.evaluation-prompt-content h3 {
  margin: 10px 0;
  color: #2d3748;
}

.evaluation-prompt-content p {
  color: #718096;
  margin-bottom: 20px;
}

/* 立即前往按钮样式 */
.go-evaluate-btn {
  background: linear-gradient(135deg, #f6ad55 0%, #f687b3 100%);
  border: none;
  color: white;
  box-shadow: 0 2px 10px rgba(246, 173, 85, 0.3);
}

.go-evaluate-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(246, 173, 85, 0.4);
}

/* 评价提醒标签 */
.evaluation-reminder {
  position: fixed;
  bottom: 30px;
  right: 30px;
  background: linear-gradient(135deg, #f6ad55 0%, #f687b3 100%);
  color: white;
  padding: 12px 20px;
  border-radius: 30px;
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(246, 173, 85, 0.3);
  transition: all 0.3s ease;
  z-index: 1000;
  animation: pulse 2s infinite;
}

.evaluation-reminder:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 16px rgba(246, 173, 85, 0.4);
}

.evaluation-reminder .el-icon {
  font-size: 18px;
}

/* 脉冲动画 */
@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.05); }
  100% { transform: scale(1); }
}

/* 评价对话框增强样式 */
.evaluation-dialog :deep(.el-dialog__body) {
  padding: 20px 25px;
}

.evaluation-header {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
  padding: 10px;
  background: #f8fafc;
  border-radius: 8px;
}

.evaluation-user-info {
  margin-left: 15px;
}

.evaluation-user-info h3 {
  margin: 0;
  font-size: 1.2rem;
  color: #2d3748;
}

.user-role {
  margin-top: 5px;
}

/* 评分样式增强 */
.evaluation-form :deep(.el-rate) {
  margin-top: 8px;
}

.evaluation-form :deep(.el-rate__icon) {
  font-size: 28px;
}

/* 文本区域样式 */
.evaluation-form :deep(.el-textarea__inner) {
  min-height: 120px !important;
}
</style>
