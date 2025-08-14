<template>
  <div class="profile-container">
    <div class="profile-header">
      <div class="profile-info">
        <div >
          <div class="user-profile-wrapper">
            <!-- 头像部分 -->
            <el-avatar 
              :size="80" 
              :src="userInfo.data?.avatarUrl" 
              class="user-avatar"
              :style="{
                backgroundColor: userInfo.data?.avatarUrl ? 'transparent' : '#409EFF',
                color: 'white',
                fontSize: '24px'
              }"
            >
              {{ userInfo.data?.nikeName ? userInfo.data.nikeName.charAt(0) : '用' }}
            </el-avatar>
            
            <!-- 用户信息部分 -->
            <div class="user-info-wrapper">
              <div class="user-info-main">
                <h1 class="username">{{ userInfo.data?.username || '用户名' }}</h1>
                <div class="industry-badge">
                  <el-tag 
                    v-if="userInfo.data?.industryName"
                    effect="dark"
                    type="info"
                    size="small"
                  >
                    {{ userInfo.data.industryName }}
                  </el-tag>
                  <el-tag 
                    v-else
                    effect="plain"
                    type="info"
                    size="small"
                  >
                    未设置行业
                  </el-tag>
                </div>
              </div>
              
              <!-- 添加关注按钮 -->
              <el-button 
                v-if="!isCurrentUser && userInfo.data?.id"
                :type="isFollowing ? 'info' : 'primary'"
                size="small"
                @click="toggleFollow"
                plain
                round
                :loading="followLoading"
              >
                <el-icon><Plus /></el-icon>
                <span>{{ isFollowing ? '已关注' : '关注他' }}</span>
              </el-button>

               <el-button 
                v-if="!isCurrentUser && userInfo.data?.id"
                type="success"
                size="small"
                @click="openMessageDialog"
                plain
                round
                class="message-btn"
              >
                <el-icon><Message /></el-icon>
                <span>发私信</span>
              </el-button>
              
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="content-tabs">
      <el-tabs v-model="activeTab" @tab-click="handleTabChange">
       
        <el-tab-pane label="我发布的" name="myPublish" v-loading="loading">
          <div class="infinite-list" v-infinite-scroll="loadMore" :infinite-scroll-disabled="noMorePublish">
            <div
              class="activity-item"
              v-for="(item, index) in publishList"
              :key="'publish-' + index"
              @click="goToDetail(item)"
            >
              <div class="activity-header">
                <h3 class="activity-title">{{ item.name }}</h3>
                <!-- 状态标签移到右上角 -->
                <el-tag
                  class="activity-status"
                  :type="item.status === 0 ? 'warning' : item.status === 1 ? 'success' : 'danger'"
                  size="small"
                >
                  {{ item.status === 0 ? '待审核' : item.status === 1 ? '已通过' : '已拒绝' }}
                </el-tag>
              </div>
              
              <div class="activity-content">
                <!-- 分类和时间放在原来状态标签的位置 -->
                <div class="activity-meta-top">
                  <div class="activity-type">{{ item.categoryName }}</div>
                  <div class="activity-time">{{ item.createdAt.slice(0, 10) }}</div>
                </div>

                <div class="activity-detail">{{ item.description }}</div>

                <!-- 如果是拒绝，展示理由 -->
                <!-- <div v-if="item.status === 2" class="activity-reason">
                  <strong>拒绝理由：</strong>{{ item.reason || '无' }}
                </div> -->
                <div class="activity-meta">
                  <span>👍 已赞同 {{ item.likeCount }}</span>
                  <span>💬 {{ item.commentCount }} 条评论</span>
                  <span>❤️ 收藏 {{ item.favoriteCount }}</span>
                </div>
              </div>
            </div>

            <div v-if="noMorePublish" class="no-more">没有更多内容了</div>
          </div>
        </el-tab-pane>

        
        <el-tab-pane label="我的团队" name="myTeams" v-loading="teamLoading">
          <div
            class="infinite-list"
            v-infinite-scroll="loadMoreTeam"
            :infinite-scroll-disabled="noMoreTeam"
          >
            <div
              class="activity-item"
              v-for="(item, index) in teamList"
              :key="'team-' + index"
              @click="goToMyMemberGroupDetail(item.projectId)"
            >
              <div class="activity-type">我的角色：{{ item.roleOfMemberGroup }}</div>
              <div class="activity-time">{{ item.createdAt?.slice(0,10) || '-' }}</div>
              <div class="activity-content">
                <h3 class="activity-title">{{ item.name }}</h3>
              </div>
            </div>
            <div v-if="noMoreTeam" class="no-more">没有更多内容了</div>
          </div>
        </el-tab-pane>


        <el-tab-pane label="我收藏的" name="myFavorites" v-loading="favoritesLoading">
          <div class="infinite-list" v-infinite-scroll="loadMoreFavorites" :infinite-scroll-disabled="noMoreFavorites">
            <div class="activity-item" v-for="(item, index) in favoritesList" :key="'favorites-'+index" @click="goToDetail(item)">
              <div class="activity-type">{{ item.categoryName }}</div>
              <div class="activity-time">{{ item.createdAt.slice(0,10) }}</div>
              <div class="activity-content">
                <h3 class="activity-title">{{ item.name }}</h3>
                <div class="activity-detail">{{ item.description }}</div>
                <div class="activity-meta">
                  <span>已赞同 {{ item.likeCount }}</span>
                  <span>{{ item.commentCount }} 条评论</span>
                  <span>收藏 {{ item.favoriteCount }}</span>
                </div>
              </div>
            </div>
            <div v-if="noMoreFavorites" class="no-more">没有更多内容了</div>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="我点赞的" name="myLike" v-loading="likeLoading">
          <div class="infinite-list" v-infinite-scroll="loadMoreLike" :infinite-scroll-disabled="noMoreLike">
            <div class="activity-item" v-for="(item, index) in likeList" :key="'like-'+index" @click="goToDetail(item)">
              <div class="activity-type">{{ item.categoryName }}</div>
              <div class="activity-time">{{ item.createdAt.slice(0,10) }}</div>
              <div class="activity-content">
                <h3 class="activity-title">{{ item.name }}</h3>
                <div class="activity-detail">{{ item.description }}</div>
                <div class="activity-meta">
                  <span>已赞同 {{ item.likeCount }}</span>
                  <span>{{ item.commentCount }} 条评论</span>
                  <span>收藏 {{ item.favoriteCount }}</span>
                </div>
              </div>
            </div>
            <div v-if="noMoreLike" class="no-more">没有更多内容了</div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <div class="sidebar">
       <div class="sidejob-entry-card">
          <div class="sidejob-header">
            <el-icon size="22"><Suitcase /></el-icon>
            <span class="sidejob-title">开启副业，赚外快</span>
          </div>
          <div class="sidejob-description">选择你的路径：发起副业项目，或加入有趣团队</div>
          <div class="sidejob-buttons">
            <!-- 找团队 - 使用握手符号表示合作，改为info天蓝色 -->
            <el-button type="info" size="large" @click="toggleIntentForm">
              👥 寻找团队
            </el-button>
          </div>

          <!-- 意向表单区域 -->
          <div class="intent-section" v-if="showIntentSection">
            <el-alert 
              v-if="!hasSubmitted"
              title="请先填写您的加入意向信息，让更多人了解您"
              type="info"
              show-icon
              :closable="false"
              class="intent-alert"
            />
            
            <el-card class="intent-card" shadow="always">
              <h2>🧍 我的加入意向</h2>
              
             <!-- 修改意向表单展示部分 -->
              <template v-if="hasSubmitted && !editMode">
                <!-- 已提交时的展示模式 -->
                <div class="intent-display">
                  <div class="intent-item">
                    <span class="intent-label">我的身份：</span>
                    <span class="intent-value">{{ form.audienceName || '未填写' }}</span>
                  </div>
                  <div class="intent-item">
                    <span class="intent-label">可投入时间：</span>
                    <span class="intent-value">{{ form.timePerDay ? `${form.timePerDay}小时/天` : '未填写' }}</span>
                  </div>
                  <div class="intent-item">
                    <span class="intent-label">个人技能：</span>
                    <span class="intent-value">{{ form.skillNames || '未填写' }}</span>
                  </div>
                  <div class="intent-item">
                    <span class="intent-label">我能提供：</span>
                    <span class="intent-value">{{ form.resources || '未填写' }}</span>
                  </div>
                </div>
              </template>
              
              <!-- 编辑模式 -->
              <el-form 
                v-if="!hasSubmitted || editMode"
                :model="form" 
                label-width="100px"
                class="intent-form"
              >
                <el-form-item label="我的身份">
                  <el-input 
                    v-model="form.audience" 
                    placeholder="如：上班族、大学生、宝妈等"
                    clearable
                  ></el-input>
                </el-form-item>
                <el-form-item label="可投入时间">
                  <el-input 
                    v-model="form.time" 
                    placeholder="如：每天2小时、每周末全天"
                    clearable
                  ></el-input>
                </el-form-item>
                <el-form-item label="个人技能">
                  <el-input 
                    v-model="form.skills" 
                    placeholder="如：剪辑、写作、编程、社群运营等"
                    clearable
                  ></el-input>
                </el-form-item>
                <el-form-item label="我能提供">
                  <el-input 
                    v-model="form.resources" 
                    placeholder="如：设备、人脉、账号资源等"
                    clearable
                  ></el-input>
                </el-form-item>
                <el-form-item label="是否发布">
                  <el-radio-group v-model="form.status">
                    <el-radio :label="1">发布</el-radio>
                    <el-radio :label="0">不发布</el-radio>
                  </el-radio-group>
                </el-form-item>
               
              </el-form>
            </el-card>
          </div>
        </div>

        <div class="sidebar-info-section">
          <div class="sidebar-follow-row">
            <div class="sidebar-section" @click="openFolloweeDrawer" style="cursor: pointer;">
              <h3 class="sidebar-title">我关注的</h3>
              <div class="sidebar-count">{{ followerCount }}</div>
            </div>
            <div class="sidebar-section" @click="openFollowerDrawer" style="cursor: pointer;">
              <h3 class="sidebar-title">关注我的</h3>
              <div class="sidebar-count">{{ followeeCount }}</div>
            </div>
          </div>
          

          <div class="sidebar-follow-row">
             <div class="sidebar-section" @click="openFollowedBarsDrawer" style="cursor: pointer;">
                <h3 class="sidebar-title">我关注的圈子</h3>
                <div class="sidebar-count">{{ followedBarsTotalCount }}</div>
              </div>
              <div class="sidebar-section"  style="cursor: pointer;">
                <h3 class="sidebar-title">待定</h3>
                <div class="sidebar-count"></div>
              </div>
          </div>

        </div>
      </div>
  </div>


  <!-- 私信弹窗 -->
<!-- In the template section - update the message dialog part -->
<el-dialog
  v-model="messageDialogVisible"
  title="私信对话"
  width="600px"
  :close-on-click-modal="false"
  custom-class="message-dialog"
>
  <div class="message-dialog-content">
    <!-- 消息历史区域 -->
    <div class="message-history-container" v-loading="chatLoading">
      <el-scrollbar height="400px" @scroll="handleScroll">
        <!-- 在template部分，修改消息项的结构 -->
        <div 
          v-for="(msg, index) in messageHistory" 
          :key="index"
          class="message-item"
          :class="{'message-sent': msg.isSelf, 'message-received': !msg.isSelf}"
          :ref="index === 0 ? 'firstMessageRef' : null"
        >
          <!-- 接收的消息头像在左边 -->
          <div class="message-avatar" v-if="!msg.isSelf">
            <el-avatar 
              :size="40" 
              :src="msg.isSelf ? currentUserInfo.data.avatarUrl : userInfo.data.avatarUrl"
            />
          </div>
          
          <!-- 消息内容 -->
          <div class="message-content-wrapper">
            <div class="message-meta">
              <span class="message-sender">{{ msg.isSelf ? '你' : msg.senderName }}</span>
              <span class="message-time">{{ formatMessageTime(msg.createdAt) }}</span>
            </div>
            <div class="message-bubble">
              {{ msg.content }}
            </div>
          </div>
          
          <!-- 发送的消息头像在右边 -->
          <div class="message-avatar" v-if="msg.isSelf">
            <el-avatar 
              :size="40" 
              :src="msg.isSelf ? currentUserInfo.data.avatarUrl : userInfo.data.avatarUrl"
            />
          </div>
        </div>
        
        <div v-if="messageHistory.length === 0 && !chatLoading" class="no-messages">
          <el-empty description="暂无聊天记录" />
        </div>
      </el-scrollbar>
    </div>

    <!-- 消息输入区域 -->
    <div class="message-input-area">
      <el-input
        v-model="messageContent"
        type="textarea"
        :rows="3"
        placeholder="输入你想发送的消息..."
        maxlength="500"
        show-word-limit
        resize="none"
        @keyup.enter="sendMessage"
      ></el-input>
      
      <div class="message-actions">
        <el-button @click="messageDialogVisible = false">取消</el-button>
        <el-button 
          type="primary" 
          @click="sendMessage"
          :loading="sendingMessage"
          :disabled="!messageContent.trim()"
        >
          发送
        </el-button>
      </div>
    </div>
  </div>
</el-dialog>


<el-drawer
  v-model="messageHistoryVisible"
  title="与TA的对话"
  size="480px"
  direction="rtl"
  :with-header="true"
  class="message-history-drawer"
>
  <el-scrollbar height="calc(100vh - 120px)">
    <div class="message-history-container">
      <div 
        v-for="(msg, index) in messageHistory" 
        :key="index"
        class="message-item"
        :class="{'message-sent': msg.senderId === currentUserInfo.data.id, 'message-received': msg.senderId !== currentUserInfo.data.id}"
      >
        <div class="message-avatar">
          <el-avatar 
            :size="36" 
            :src="msg.senderId === currentUserInfo.data.id ? currentUserInfo.data.avatarUrl : userInfo.data.avatarUrl"
          />
        </div>
        <div class="message-content">
          <div class="message-text">{{ msg.content }}</div>
          <div class="message-time">{{ formatMessageTime(msg.createdAt) }}</div>
        </div>
      </div>
      
      <div v-if="loadingMessages" class="message-loading">
        <el-icon class="is-loading"><Loading /></el-icon>
        加载中...
      </div>
      
      <div v-if="!loadingMessages && messageHistory.length === 0" class="no-messages">
        暂无历史消息
      </div>
    </div>
    
    <div class="message-input-container">
      <el-input
        v-model="newMessage"
        type="textarea"
        :rows="2"
        placeholder="输入新消息..."
        resize="none"
        @keyup.enter="sendNewMessage"
      ></el-input>
      <el-button 
        type="primary" 
        size="small" 
        @click="sendNewMessage"
        :disabled="!newMessage.trim()"
        class="send-btn"
      >
        发送
      </el-button>
    </div>
  </el-scrollbar>
</el-drawer>


  <!-- 我关注的圈子 drawer -->
<el-drawer
  v-model="followedBarsDrawerVisible"
  title="我关注的圈子"
  size="480px"
  direction="rtl"
  :with-header="true"
  class="custom-drawer"
>
  <el-scrollbar height="600px">
    <div
      class="followed-bar-card"
      v-for="bar in followedBarsList"
      :key="bar.id"
      @click="navigateToBar(bar.id)"
    >
      <el-avatar :src="bar.avatar" :size="48" shape="square" />
      <div class="followed-bar-info">
        <div class="followed-bar-name">{{ bar.name }}</div>
        <div class="followed-bar-stats">
          <span class="stat-item">
             <i class="stat-icon">👥</i>
            <span>{{ bar.followerCount | formatNumber }}</span>
          </span>
          <span class="stat-item">
            <i class="stat-icon">📝</i>
            <span>{{ bar.postCount | formatNumber }}</span>
          </span>
        </div>
        <div class="followed-bar-unread" v-if="bar.unread > 0">
          <el-badge :value="bar.unread" />
        </div>
      </div>
    </div>
    <div v-if="followedBarsLoading" class="followed-bar-loading">加载中...</div>
    <div v-if="!followedBarsLoading && followedBarsList.length === 0" class="no-more">
      暂无关注的圈子
    </div>
  </el-scrollbar>
  
  <!-- 分页控件 -->
  <div class="pagination-container">
    <el-pagination
      small
      layout="prev, pager, next"
      :total="followedBarsTotal"
      :page-size="followedBarsSize"
      v-model:current-page="followedBarsPage"
      @current-change="handleFollowedBarsPageChange"
      class="custom-pagination"
    />
  </div>
</el-drawer>


  <el-drawer
    v-model="followeeListVisible"
    title="我关注的用户"
    size="480px"
    direction="rtl"
    :with-header="true"
    class="custom-drawer"
  >
    <el-scrollbar height="600px">
      <div
        class="followee-user-card"
        v-for="user in followeeList"
        :key="user.id"
      >
        <el-avatar :src="user.avatarUrl" :size="48" />
        <div class="followee-user-info">
          <div class="followee-username">{{ user.username }}</div>
          <div class="followee-industry">{{ user.industryName || '未设置行业' }}</div>
          <div class="followee-actions">
            <el-button
              size="small"
              type="primary"
              @click="goToUserProfile(user.secrecyId)"
              plain
            >
              访问主页
            </el-button>
            <el-button
            v-if="user.needFollow !== undefined && user.needFollow !== null"
              size="small"
              :type="user.needFollow ? 'success' : 'info'"
              @click="handleFollowAction(user)"
              plain
              :loading="user.loading"
            >
              {{ user.needFollow ? '关注他' : '已关注' }}
            </el-button>
          </div>
        </div>
      </div>
      <div v-if="followeeLoading" class="followee-loading">加载中...</div>
      <div v-if="!followeeLoading && followeeList.length === 0" class="no-more">暂无关注用户</div>
    </el-scrollbar>
    
    <!-- 分页控件 -->
    <div class="pagination-container">
      <el-pagination
        small
        layout="prev, pager, next"
        :total="followeeTotal"
        :page-size="followeeSize"
        v-model:current-page="followeePage"
        @current-change="handlePageChange"
      />
    </div>
  </el-drawer>

  <!-- 关注我的用户 drawer -->
  <el-drawer
    v-model="followerListVisible"
    title="关注我的用户"
    size="480px"
    direction="rtl"
    :with-header="true"
    class="custom-drawer"
  >
    <el-scrollbar height="600px">
      <div
        class="followee-user-card"
        v-for="user in followerList"
        :key="user.id"
      >
        <el-avatar :src="user.avatarUrl" :size="48" />
        <div class="followee-user-info">
          <div class="followee-username">{{ user.username }}</div>
          <div class="followee-industry">{{ user.industryName || '未设置行业' }}</div>
          <div class="followee-actions">
            <el-button
              size="small"
              type="primary"
              @click="goToUserProfile(user.secrecyId)"
              plain
            >
              访问主页
            </el-button>
            <el-button
              v-if="user.needFollow !== undefined && user.needFollow !== null"
              size="small"
              :type="user.needFollow ? 'success' : 'info'"
              @click="handleFollowAction(user)"
              plain
              :loading="user.loading"
            >
              {{ user.needFollow ? '关注他' : '已关注' }}
            </el-button>
          </div>
        </div>
      </div>
      <div v-if="followerLoading" class="followee-loading">加载中...</div>
      <div v-if="!followerLoading && followerList.length === 0" class="no-more">暂无关注用户</div>
    </el-scrollbar>
    
    <div class="pagination-container">
      <el-pagination
        small
        layout="prev, pager, next"
        :total="followerTotal"
        :page-size="followerSize"
        v-model:current-page="followerPage"
        @current-change="handleFollowerPageChange"
        class="custom-pagination"
      />
    </div>
  </el-drawer>
</template>

<script setup>
import { ref, computed, onMounted, watch,nextTick,onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Suitcase, SuccessFilled, Plus ,Message, Loading} from '@element-plus/icons-vue'
import { post, get } from '@/net'
import { ElMessage } from 'element-plus'
import useUserInfo from '@/hooks/useUserInfo';


const { state: currentUserInfo, loadUserInfo } = useUserInfo();
const route = useRoute()
const router = useRouter()
const teamList = ref([])
const teamPage = ref(1)
const teamSize = ref(10)
const teamTotal = ref(0)
const teamLoading = ref(false)
const noMoreTeam = ref(false)
const userInfo = ref({
  data: null
})


// 意向表单相关状态
const showIntentSection = ref(false)
const editMode = ref(false)
const hasSubmitted = ref(false)
const submitting = ref(false)
const publishing = ref(false)

// 添加关注相关状态
const isFollowing = ref(false)
const followLoading = ref(false)

// 用户ID从路由参数获取
const secrecyId = ref(route.params.id)
const isCurrentUser =  ref(false)

// 我关注的用户相关状态
const followeeListVisible = ref(false)
const followeeList = ref([])
const followeePage = ref(1)
const followeeSize = ref(10)
const followeeTotal = ref(0)
const followeeLoading = ref(false)
const followeeFinished = ref(false)

// 关注我的用户相关状态
const followerListVisible = ref(false)
const followerList = ref([])
const followerPage = ref(1)
const followerSize = ref(10)
const followerTotal = ref(0)
const followerLoading = ref(false)
const followerFinished = ref(false)

// 我关注的圈子相关状态
const followedBars = ref([])
const followedBarsDrawerVisible = ref(false)
const followedBarsList = ref([])
const followedBarsPage = ref(1)
const followedBarsSize = ref(10)
const followedBarsTotal = ref(0)
const followedBarsLoading = ref(false)
const followedBarsTotalCount = ref(0)

// 在 setup 中添加状态
const messageDialogVisible = ref(false)
const messageContent = ref('')
const sendingMessage = ref(false)
const messageHistoryVisible = ref(false)
const messageHistory = ref([])
const loadingMessages = ref(false)
const newMessage = ref('')
const chatHistory = ref([])
const chatPage = ref(1)
const chatSize = ref(10)
const chatTotal = ref(0)
const chatLoading = ref(false)
const chatConversationId = ref(null)
const isNewConversation = ref(false)
// 添加一个 ref 来记录第一条消息的 DOM 元素
const firstMessageRef = ref(null)
// 添加轮询定时器引用
const pollInterval = ref(null)



onMounted(() => {
  if (!currentUserInfo.data.id) {
    loadUserInfo().then(() => {
      fetchUserInfo()
      isCurrentUser.value = String(currentUserInfo.data.secrecyId) === String(secrecyId.value)
      fetchPublishData()
      fetchFollowCount()
      loadIntentData()
      fetchFollowedBars();
    });
  } else {
     fetchUserInfo();
    isCurrentUser.value = String(currentUserInfo.data.secrecyId) === String(secrecyId.value)
    fetchPublishData()
    fetchFollowCount()
    loadIntentData()
    fetchFollowedBars();
  }
});

const formatMessageTime = (timeString) => {
  if (!timeString) return ''
  
  const now = new Date()
  const date = new Date(timeString)
  const diffInMinutes = Math.floor((now - date) / (1000 * 60))
  
  if (diffInMinutes < 1) {
    return '刚刚'
  } else if (diffInMinutes < 60) {
    return `${diffInMinutes}分钟前`
  } else if (date.toDateString() === now.toDateString()) {
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
  } else if (date.getFullYear() === now.getFullYear()) {
    return date.toLocaleDateString([], { month: 'short', day: 'numeric' })
  } else {
    return date.toLocaleDateString()
  }
}

const updateMessageStatus = async (messageIds, status) => {
  try {
    await post('/api/auth/chat/updateMessageStatus', {
      messageIds,
      status
    })
  } catch (error) {
    console.error('更新消息状态失败:', error)
  }
}

const openMessageDialog = async () => {
  console.log('openMessageDialog called')
  messageDialogVisible.value = true
  messageContent.value = ''
  
  // 重置聊天状态
  chatHistory.value = []
  chatPage.value = 1
  chatConversationId.value = null
  isNewConversation.value = false
  
  try {
    chatLoading.value = true
    const res = await post('/api/auth/chat/getChatHistory', {
      secrecyId: secrecyId.value,
      page: chatPage.value,
      size: chatSize.value
    })
    console.log("获取聊天历史",res)
    if (res.records && res.records.length > 0) {
      chatHistory.value = res.records
      chatTotal.value = res.total
      chatConversationId.value = res.records[0].chatConversationId
      
      formatChatHistory()
      scrollToBottom()
    } else {
      isNewConversation.value = true
      ElMessage.info('这是你们第一次对话，开始聊天吧！')
    }
    
    // 开启轮询获取新消息
    startPolling()
  } catch (error) {
    console.error('获取聊天历史失败:', error)
    ElMessage.error('获取聊天历史失败')
  } finally {
    chatLoading.value = false
  }
}

// 开始轮询
const startPolling = () => {
  // 先清除已有定时器
  stopPolling()
  // 每5秒获取一次新消息
  pollInterval.value = setInterval(fetchNewMessages, 5000)
}

// 停止轮询
const stopPolling = () => {
  if (pollInterval.value) {
    clearInterval(pollInterval.value)
    pollInterval.value = null
  }
}

// 获取新消息
const fetchNewMessages = async () => {
  if (!chatConversationId.value || chatLoading.value) return
  
  console.log("chatHistory:",chatHistory.value)
  try {
    const res = await post('/api/auth/chat/getNewMessages', {
      chatConversationId: chatConversationId.value,
      lastMessageId: chatHistory.value.length > 0 
        ? chatHistory.value[chatHistory.value.length - 1].chatMessageId 
        : null
    })
    
    if (res.records && res.records.length > 0) {
      // 只添加新消息
      const newMessages = res.records.filter(newMsg => 
        !chatHistory.value.some(existingMsg => 
          existingMsg.chatMessageId === newMsg.chatMessageId
        )
      )
      
      if (newMessages.length > 0) {
        chatHistory.value.push(...newMessages)
        formatChatHistory()
        
        // 平滑滚动到底部
        nextTick(() => {
          const container = document.querySelector('.message-history-container .el-scrollbar__wrap')
          if (container) {
            // 使用平滑滚动
            container.scrollTo({
              top: container.scrollHeight,
              behavior: 'smooth'
            })
          }
        })
      }
    }
  } catch (error) {
    console.error('获取新消息失败:', error)
  }
}

// 在组件卸载时停止轮询
onUnmounted(() => {
  stopPolling()
})

// 关闭对话框时停止轮询
watch(messageDialogVisible, (visible) => {
  if (!visible) {
    stopPolling()
  }
})


const formatChatHistory = () => {
  messageHistory.value = chatHistory.value.map(msg => ({
    ...msg,
    senderId: msg.senderId,
    content: msg.content,
    createdAt: msg.createdTime,
    isSelf: msg.senderId === currentUserInfo.data.id
  }))
}


const handleScroll = ({ scrollTop }) => {
  const scrollContainer = document.querySelector('.message-history-container .el-scrollbar__wrap')
  if (!scrollContainer) return
  
  // 如果滚动到顶部附近，加载更多消息
  if (scrollTop < 100 && !chatLoading.value && chatPage.value * chatSize.value < chatTotal.value) {
    loadMoreMessages()
  }
}

// 自动滚动到底部
const scrollToBottom = (behavior = 'smooth') => {
  nextTick(() => {
    const container = document.querySelector('.message-history-container .el-scrollbar__wrap')
    if (container) {
      container.scrollTo({
        top: container.scrollHeight,
        behavior: behavior
      })
    }
  })
}

const sendMessage = async () => {
  if (!messageContent.value.trim()) return
  
  sendingMessage.value = true
  try {
    const requestData = {
      receiverId: userInfo.value.data.id,
      content: messageContent.value,
      chatType: 1
    }
    
    if (chatConversationId.value) {
      requestData.chatConversationId = chatConversationId.value
    }
    
    const res = await post('/api/auth/chat/sendMessage', requestData)
    console.log("发送",res)
    
    if (!chatConversationId.value && res) {
      chatConversationId.value = res
      isNewConversation.value = false
    }
    
    ElMessage.success('消息发送成功')
    
    const newMsg = {
      chatMessageId: Date.now(), // 临时ID，实际应该从响应获取
      chatConversationId: chatConversationId.value,
      senderId: currentUserInfo.data.id,
      senderName: currentUserInfo.data.username,
      senderAvatar: currentUserInfo.data.avatarUrl,
      isSelf: true,
      content: messageContent.value,
      createdTime: new Date().toISOString(),
      status: 1
    }
    
    chatHistory.value.push(newMsg)
    formatChatHistory()
    messageContent.value = ''
    
    // 使用平滑滚动
    scrollToBottom('smooth')
  } catch (error) {
    console.error('发送消息失败:', error)
    ElMessage.error('发送消息失败')
  } finally {
    sendingMessage.value = false
  }
}


const loadMoreMessages = async () => {
  if (chatLoading.value || chatPage.value * chatSize.value >= chatTotal.value) return
  
  try {
    chatLoading.value = true
    
    // 记录当前第一条消息的位置
    const scrollContainer = document.querySelector('.message-history-container .el-scrollbar__wrap')
    const firstMessage = document.querySelector('.message-item:first-child')
    const prevScrollHeight = scrollContainer.scrollHeight
    const prevFirstMessageOffset = firstMessage ? firstMessage.offsetTop : 0
    
    chatPage.value++
    const res = await post('/api/auth/chat/getChatHistory', {
      secrecyId: secrecyId.value,
      page: chatPage.value,
      size: chatSize.value
    })
    
    if (res.records && res.records.length > 0) {
      // 将新数据插入到数组开头
      const newRecords = res.records.map(msg => ({
        ...msg,
        senderId: msg.senderId,
        content: msg.content,
        createdAt: msg.createdTime,
        isSelf: msg.senderId === currentUserInfo.data.id
      }))
      
      // 使用展开运算符创建新数组，确保响应式更新
      messageHistory.value = [...newRecords, ...messageHistory.value]
      
      // 等待 DOM 更新
      nextTick(() => {
        // 计算新内容增加的高度
        const newScrollHeight = scrollContainer.scrollHeight
        const heightDiff = newScrollHeight - prevScrollHeight
        
        // 保持用户之前的阅读位置
        scrollContainer.scrollTop = heightDiff + (firstMessage ? firstMessage.offsetTop - prevFirstMessageOffset : 0)
      })
    }
  } catch (error) {
    console.error('加载更多消息失败:', error)
    chatPage.value-- // 回退页码
  } finally {
    chatLoading.value = false
  }
}


const openMessageHistory = () => {
  messageHistoryVisible.value = true
  loadMessageHistory()
}

const loadMessageHistory = async () => {
  if (!userInfo.value.data?.id) return
  
  loadingMessages.value = true
  try {
    const res = await get(`/api/auth/message/history?userId=${userInfo.value.data.id}`)
    messageHistory.value = res.records || []
  } catch (error) {
    console.error('加载消息历史失败:', error)
    ElMessage.error('加载消息历史失败')
  } finally {
    loadingMessages.value = false
  }
}

const sendNewMessage = async () => {
  if (!newMessage.value.trim()) return
  
  try {
    await post('/api/auth/message/send', {
      receiverId: userInfo.value.data.id,
      content: newMessage.value
    })
    newMessage.value = ''
    loadMessageHistory()
  } catch (error) {
    console.error('发送消息失败:', error)
    ElMessage.error('发送消息失败')
  }
}


// 获取我关注的圈子
const fetchFollowedBars = async (page = 1, size = 5) => {
  try {
    followedBarsLoading.value = true
    const response = await post('/api/auth/quan/myFavoriteBar', {
      page: page,
      size: size,
      secrecyId: secrecyId.value,
    })
    console.log("response",response)
    if (response && response.records) {
      if (size === 5) {
        // 主列表数据
        followedBars.value = response.records.map(bar => ({
          id: bar.id,
          name: bar.name,
          avatar: bar.avatar,
          followerCount: bar.followerCount,
          postCount: bar.postCount,
          unread: 0 // 初始未读消息数为0
        }))
      } else {
        // 弹窗数据
        followedBarsList.value = response.records.map(bar => ({
          id: bar.id,
          name: bar.name,
          avatar: bar.avatar,
          followerCount: bar.followerCount,
          postCount: bar.postCount,
          unread: 0 // 初始未读消息数为0
        }))
        followedBarsTotal.value = response.total || 0
      }
      // 更新总数
      followedBarsTotalCount.value = response?.total || response?.records?.length || 0

    }
  } catch (error) {
    console.error('获取关注的圈子失败:', error)
    ElMessage.error('获取关注的圈子失败，请稍后重试')
  } finally {
    followedBarsLoading.value = false
  }
}

// 打开我关注的圈子抽屉
const openFollowedBarsDrawer = () => {
  followedBarsDrawerVisible.value = true
  followedBarsPage.value = 1
  fetchFollowedBars(1, followedBarsSize.value)
}

// 分页变化
const handleFollowedBarsPageChange = (page) => {
  followedBarsPage.value = page
  fetchFollowedBars(page, followedBarsSize.value)
}

const navigateToBar = (barId) => {
  window.open(`/index/quan/QuanDetail/${barId}`, '_blank')
}

// 打开我关注的用户抽屉
const openFolloweeDrawer = () => {
  followeeListVisible.value = true
  loadFolloweeList()
}

// 打开关注我的用户抽屉
const openFollowerDrawer = () => {
  followerListVisible.value = true
  loadFollowerList()
}

// 加载关注我的用户列表
const loadFollowerList = async () => {
  followerLoading.value = true
  try {
    const res = await post('/api/auth/my/otherFollowers', {
      secrecyId: secrecyId.value,
      page: followerPage.value,
      size: followerSize.value
    })
    const records = res.records || res
    followerList.value = records.map(user => ({
      ...user,
      loading: false
    }))
    followerTotal.value = res.total || records.length || 0
  } catch (err) {
    console.error('加载粉丝用户失败', err)
    ElMessage.error('加载粉丝用户失败')
  } finally {
    followerLoading.value = false
  }
}

// 加载我关注的用户列表
const loadFolloweeList = async () => {
  followeeLoading.value = true
  try {
    const res = await post('/api/auth/my/otherFollowees', {
      secrecyId: secrecyId.value,
      page: followeePage.value,
      size: followeeSize.value
    })
    const records = res.records || res
    // Add loading state to each user
    followeeList.value = records.map(user => ({
      ...user,
      loading: false
    }))
    followeeTotal.value = res.total || records.length || 0
    // const records = res.records || res
    // followeeList.value = records
    // followeeTotal.value = res.total || records.length || 0
  } catch (err) {
    console.error('加载关注用户失败', err)
    ElMessage.error('加载关注用户失败')
  } finally {
    followeeLoading.value = false
  }
}

const handleFollowAction = async (user) => {
  user.loading = true
  try {
    if (user.needFollow) {
      // Follow action
      await post('/api/auth/project/concernPublisher', {
        followeeId: user.userId
      })
      ElMessage.success(`已关注 ${user.username}`)
    } else {
      // Unfollow action
      await post('/api/auth/project/concernPublisherCancel', {
        followeeId: user.userId
      })
      ElMessage.success(`已取消关注 ${user.username}`)
    }
    // Toggle the follow state
    user.needFollow = !user.needFollow
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败，请稍后重试')
  } finally {
    user.loading = false
  }
}

// 我关注的用户分页变化
const handlePageChange = (page) => {
  followeePage.value = page
  loadFolloweeList()
}

// 关注我的用户分页变化
const handleFollowerPageChange = (page) => {
  followerPage.value = page
  loadFollowerList()
}

const goToUserProfile = (secrecyId) => {
   window.open(`/index/user/${secrecyId}`, '_blank');
}

const doFollowBack = async (user) => {
  try {
    await post('/api/auth/project/concernPublisher', {
      followeeId: user.userId
    })
    ElMessage.success(`已回关 ${user.username}`)
    user.needFollow = false
  } catch (error) {
    console.error('回关失败:', error)
    ElMessage.error('回关失败')
  }
}

const form = ref({
  id:'',
  audience: '',
  timePerDay: '',
  skills: '',
  resources: '',
  status: 1
})

// 检查关注状态
const checkFollowStatus = async () => {
  if (!userInfo.value.data?.id ) return
  try {
    const res = await get(`/api/auth/my/isFollewer?followeeId=${userInfo.value.data.id}`)
    isFollowing.value = !res
  } catch (error) {
    console.error('检查关注状态失败:', error)
  }
}

// 切换关注状态
const toggleFollow = async () => {
  if (!userInfo.value.data?.id || followLoading.value) return
  
  followLoading.value = true
  try {
    if (isFollowing.value) {
      await post('/api/auth/project/concernPublisherCancel', {
        followeeId: userInfo.value.data.id
      })
      ElMessage.success('已取消关注')
    } else {
      await post('/api/auth/project/concernPublisher', {
        followeeId: userInfo.value.data.id
      })
      ElMessage.success('关注成功')
    }
    isFollowing.value = !isFollowing.value
    await fetchFollowCount()
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败，请稍后重试')
  } finally {
    followLoading.value = false
  }
}

const loadMoreTeam = () => {
  if (!teamLoading.value && teamPage.value * teamSize.value < teamTotal.value) {
    teamPage.value++
    fetchTeamData()
  } else {
    noMoreTeam.value = true
  }
}

const fetchTeamData = async () => {
  try {
    teamLoading.value = true
    const res = await post('/api/auth/projectMember/myMemberGroups', {
        secrecyId:secrecyId.value,
      page: teamPage.value,
      size: teamSize.value
    })
    teamList.value.push(...res.records || res)
    teamTotal.value = res.total || res.length || 0
    noMoreTeam.value = teamPage.value * teamSize.value >= teamTotal.value
  } catch (err) {
    console.error('加载我的团队失败', err)
  } finally {
    teamLoading.value = false
  }
}

const goToDetail = (project) => {
  router.push({ name: 'project-detail', params: { id: project.id } })
}

const goToMyMemberGroupDetail = (projectId) => {
  router.push({ name: 'myMemberGroupDetail', params: { id: projectId } })
}

//加载用户名称和头像
const fetchUserInfo = async () => {
  try {
    const res = await get(`/api/auth/user/getSecrecyIdUserInfo?secrecyId=${secrecyId.value}`);
    userInfo.value = { data: res }
    checkFollowStatus();
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
}

// 统计数据
const stats = ref({
  dynamicCount: 0,
  answerCount: 0,
  videoCount: 0,
  questionCount: 0,
  articleCount: 0,
  columnCount: 0,
  ideaCount: 0,
  collectionCount: 0,
  followCount: 0
})

// 加载用户意向数据
const loadIntentData = async () => {
  try {
    const res = await get(`/api/auth/project/getProjectOfMyShow?secrecyId=${secrecyId.value}`);
    if (res) {
      form.value = {
         id: res.id || '',
        audience: res.audience || '',
        audienceName: res.audienceName || '',
        timePerDay: res.timePerDay || '',
        skills: res.skills || '',
        skillNames: res.skillNames || '',
        resources: res.resources || '',
        status: res.status ? 1 : 0
      }
      hasSubmitted.value = true
    }
  } catch (error) {
    console.error('加载意向数据失败:', error)
  }
}

// 切换表单显示
const toggleIntentForm = () => {
  showIntentSection.value = !showIntentSection.value
  if (showIntentSection.value && !hasSubmitted.value) {
    editMode.value = true
  }
}

// 其余原有代码
const activeTab = ref('myPublish')
const followerCount = ref(0)
const followeeCount = ref(0)
const publishList = ref([])
const publishPage = ref(1)
const publishSize = ref(10)
const publishTotal = ref(0)
const loading = ref(false)
const noMorePublish = ref(false)
const favoritesList = ref([])
const favoritesPage = ref(1)
const favoritesSize = ref(10)
const favoritesTotal = ref(0)
const favoritesLoading = ref(false)
const noMoreFavorites = ref(false)
const likeList = ref([])
const likePage = ref(1)
const likeSize = ref(10)
const likeTotal = ref(0)
const likeLoading = ref(false)
const noMoreLike = ref(false)

const fetchFollowCount = async () => {
  try {
    const res = await get(`/api/auth/my/myFollowCount?secrecyId=${secrecyId.value}`);
    followerCount.value = res.followerCount || 0
    followeeCount.value = res.followeeCount || 0
  } catch (error) {
    console.error('获取关注数失败:', error)
  }
}

const loadMore = () => {
  if (!loading.value && publishPage.value * publishSize.value < publishTotal.value) {
    publishPage.value++
    fetchPublishData()
  } else {
    noMorePublish.value = true
  }
}

const loadMoreFavorites = () => {
  if (!favoritesLoading.value && favoritesPage.value * favoritesSize.value < favoritesTotal.value) {
    favoritesPage.value++
    fetchFavoritesData()
  } else {
    noMoreFavorites.value = true
  }
}

const loadMoreLike = () => {
  if (!likeLoading.value && likePage.value * likeSize.value < likeTotal.value) {
    likePage.value++
    fetchLikeData()
  } else {
    noMoreLike.value = true
  }
}

const fetchPublishData = async () => {
  try {
    loading.value = true
    const res = await post('/api/auth/my/myPublished', {
      secrecyId:secrecyId.value,
      page: publishPage.value,
      size: publishSize.value
    })
    publishList.value.push(...res.records)
    publishTotal.value = res.total
    noMorePublish.value = publishPage.value * publishSize.value >= res.total
  } finally {
    loading.value = false
  }
}

const fetchFavoritesData = async () => {
  try {
    favoritesLoading.value = true
    const res = await post('/api/auth/my/myFavorites', {
        secrecyId:secrecyId.value,
      page: favoritesPage.value,
      size: favoritesSize.value
    })
    favoritesList.value.push(...res.records)
    favoritesTotal.value = res.total
    noMoreFavorites.value = favoritesPage.value * favoritesSize.value >= res.total
  } finally {
    favoritesLoading.value = false
  }
}

const fetchLikeData = async () => {
  try {
    likeLoading.value = true
    const res = await post('/api/auth/my/myLike', {
        secrecyId:secrecyId.value,
      page: likePage.value,
      size: likeSize.value
    })
    likeList.value.push(...res.records)
    likeTotal.value = res.total
    noMoreLike.value = likePage.value * likeSize.value >= res.total
  } finally {
    likeLoading.value = false
  }
}

const handleTabChange = (tab) => {
  if (tab.paneName === 'myFavorites' && favoritesList.value.length === 0) {
    fetchFavoritesData()
  } else if (tab.paneName === 'myLike' && likeList.value.length === 0) {
    fetchLikeData()
  } else if (tab.paneName === 'myTeams' && teamList.value.length === 0) {
    fetchTeamData()
  }
}
</script>

<style scoped>
.profile-container {
  width: 100%;
  margin: 0 auto;
  padding: 24px 40px;
  padding-right: 60px;
  display: grid;
  grid-template-columns: 1fr 400px;
  gap: 40px;
  background-color: #fff;
  font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;
}

.profile-header {
  grid-column: 1 / -1;
  border-bottom: 1px solid #ebebeb;
  margin-bottom: 6px;
}

.profile-info {
  padding: 20px 0;
}

.username {
  font-size: 26px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 4px;
}

.industry {
  color: #8590a6;
  margin-bottom: 10px;
}

.stats-container {
  grid-column: 1 / -1;
  display: flex;
  border-bottom: 1px solid #f0f2f7;
  padding-bottom: 15px;
  margin-bottom: 20px;
}

.stats-item {
  flex: 1;
  text-align: center;
  padding: 10px;
}

.stats-count {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 5px;
}

.stats-label {
  color: #8590a6;
  font-size: 14px;
}

.content-tabs {
  grid-column: 1;
}

.activity-item {
  padding: 14px 0;
  border-bottom: 1px solid #ebebeb;
  transition: all 0.3s ease;
  cursor: pointer;
  border-radius: 6px;
  margin: 4px 0 0 10px;
  padding: 16px;
}

.activity-item:hover {
  background-color: #e6e9ef;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.followee-actions {
  margin-top: 8px;
  display: flex;
  gap: 10px;
}

.activity-type {
  color: #8590a6;
  font-size: 14px;
  order: 1;
}

.activity-time {
   color: #8590a6;
  font-size: 14px;
  order: 2;
}

.activity-title {
 font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 10px 0;
  transition: color 0.2s ease;
}

.activity-detail {
  color: #808287;
  margin: 10px 0;
  font-size: 14px;
  line-height: 1.5;
}

.activity-meta {
   color: #8590a6;
  font-size: 14px;
  display: flex;
  gap: 16px;
  margin-top: 12px;
}

.activity-meta > * {
  margin-right: 15px;
}

.sidebar {
  grid-column: 2;
  padding-top: 12px;
  padding-right: 12px;
  margin-right: 10%;
}

.sidebar-section {
 padding: 12px;
  border-radius: 8px;
  background-color: #f8f9fa;
  transition: all 0.2s ease;
}

.sidebar-section:hover {
  background-color: #b2bfd2;
}

.sidebar-title {
  font-size: 16px;
  color: #444;
  margin-bottom: 10px;
}

.sidebar-count:hover {
  color: #409EFF;
}

.sidebar-count {
 font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  transition: color 0.2s ease;
}

.empty-placeholder {
  color: #8590a6;
  font-size: 14px;
  padding: 10px 0;
}

.no-more {
  text-align: center;
  padding: 16px;
  font-size: 14px;
  color: #94a3b8;
  background-color: #f8fafc;
  border-radius: 8px;
  margin: 12px;
}

.infinite-list {
  max-height: 600px;
  overflow-y: auto;
}

.sidejob-entry-card {
   background-color: #f6f6f6;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 16px;
  margin-top: 20px;
}

.sidebar-info-section {
  background-color: #f6f6f6;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 16px;
  margin-top: 20px;
}

.sidejob-header {
  display: flex;
  align-items: center;
  font-weight: 500;
  color: #121212;
  font-size: 16px;
  margin-bottom: 10px;
}

.sidejob-header .el-icon {
  margin-right: 8px;
}

.sidejob-title {
  font-size: 20px;
}

.sidejob-description {
  font-size: 13px;
  color: #8590a6;
  margin-bottom: 14px;
}

.sidejob-buttons {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-top: 12px;
  justify-items: start;
}

.sidejob-buttons .el-button {
  width: 80%;
  height: 48px;
  padding: 0 8px;
  font-size: 13px;
  white-space: normal;
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1.4;
}

@media (max-width: 768px) {
  .sidejob-buttons {
    grid-template-columns: 1fr;
  }
  
  .sidejob-buttons .el-button {
    height: 44px;
  }
}

.intent-section {
  margin-top: 20px;
  transition: all 0.3s ease;
}

.intent-alert {
  margin-bottom: 15px;
}

.intent-card {
  background: #f9f9f9;
  border: none;
  border-radius: 8px;
  padding: 16px;
}

.intent-card h2 {
  margin-bottom: 20px;
  color: #2c3e50;
  font-weight: 600;
  font-size: 18px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.intent-form
.intent-display {
  background: #fff;
  border: 1px solid #ebebeb;
  padding: 16px;
  border-radius: 6px;
}

.intent-item {
  margin-bottom: 12px;
  display: flex;
  align-items: flex-start;
  font-size: 14px;
}

.intent-label {
  font-weight: 500;
  color: #606266;
  min-width: 90px;
}

.intent-value {
  color: #303133;
  flex: 1;
}

.intent-actions {
  margin-top: 20px;
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.published-tag {
 background-color: #f0f9eb;
  border-color: #e1f3d8;
  color: #67c23a;
  font-weight: 500;
}

@media (max-width: 768px) {
  .profile-container {
    grid-template-columns: 1fr;
  }
  
  .sidebar {
    grid-column: 1;
  }
  
  .stats-container {
    flex-wrap: wrap;
  }
  
  .stats-item {
    flex: 0 0 33.33%;
  }
  
  .intent-item {
    flex-direction: column;
    gap: 4px;
  }
  
  .intent-actions {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .published-tag {
    margin-left: 0;
    margin-top: 10px;
  }
  
  .sidejob-buttons {
    grid-template-columns: 1fr;
  }
}

.user-profile-wrapper {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 8px 0;
}

.user-avatar {
  border: 2px solid #f0f2f5;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.user-avatar:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}

.sidejob-btn {
  display: flex;
  align-items: center;
  gap: 8px;
}

.activity-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.intent-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.activity-meta-top {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 4px;
}

.reject-reason {
  margin-left: 10px;
  color: #f56c6c;
  font-size: 14px;
}

.activity-content {
  transition: all 0.3s ease;
}

.activity-status {
  margin: 8px 0;
}

.activity-reason {
  color: #f56c6c;
  font-size: 14px;
  margin: 8px 0;
}
.user-info-wrapper {
  display: flex;
  flex-direction: column;
  flex-grow: 1;
  gap: 8px;
}
.user-info-main {
  display: flex;
  flex-direction: column;
}

.username-and-badge {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.username {
  font-size: 26px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
}

.industry-badge {
  margin-left: 8px;
}

.edit-profile-btn {
  align-self: flex-start;
  margin-top: 4px;
}

@media (max-width: 600px) {
  .username-and-badge {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
  
  .username {
    font-size: 22px;
  }
  
  .user-profile-wrapper {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }
  
  .edit-profile-btn {
    align-self: center;
  }
}
.sidebar-follow-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}

.sidebar-follow-row .sidebar-section {
  flex: 1;
  background-color: #f8f9fa;
  padding: 12px;
  border-radius: 8px;
  text-align: center;
}
.user-info-wrapper .el-button {
  margin-top: 8px;
  align-self: flex-start;
}

@media (max-width: 600px) {
  .user-info-wrapper .el-button {
    align-self: center;
  }
}

.followee-user-card {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 12px;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.2s ease;
}
.followee-user-card:hover {
  background-color: #f5f7fa;
}
.followee-user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.followee-username {
  font-weight: 600;
  font-size: 16px;
  color: #1e293b; /* 深色文字 */
}
.followee-industry {
 font-size: 13px;
  color: #64748b; /* 中灰色 */
  background-color: #f1f5f9;
  padding: 2px 8px;
  border-radius: 4px;
  display: inline-block;
  width: fit-content;
}
.followee-actions {
 margin-top: 12px;
  display: flex;
  gap: 10px;
}
.followee-loading {
 text-align: center;
  padding: 20px;
  font-size: 14px;
  color: #64748b;
  background-color: #f8fafc;
  border-radius: 8px;
  margin: 12px;
}

.pagination-container {
  position: absolute;
  bottom: 20px;
  right: 20px;
  padding: 10px;
  background: white;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}
.sidebar-follow-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}

.sidebar-follow-row .sidebar-section {
  flex: 1;
  background-color: #f8f9fa;
  padding: 12px;
  border-radius: 8px;
  text-align: center;
  cursor: pointer;
}

.sidebar-follow-row .sidebar-section:hover {
  background-color: #e6e9ef;
}
.custom-drawer {
  --el-drawer-bg-color: #f8fafc; /* 更柔和的背景色 */
  --el-drawer-padding-primary: 20px;
  --el-drawer-box-shadow: 0 4px 24px rgba(0, 0, 0, 0.08); /* 更精致的阴影 */
}

.custom-drawer .el-drawer__header {
 padding: 18px 24px;
  margin-bottom: 0;
  border-bottom: 1px solid #e2e8f0; /* 更细腻的分隔线 */
  color: #1e293b; /* 深色文字 */
  font-weight: 600;
  background-color: #ffffff;
  border-radius: 8px 8px 0 0;
}

.custom-drawer .el-drawer__body {
  padding: 24px;
  background-color: #f8fafc; /* 与头部形成轻微对比 */
}

.custom-pagination {
  --el-pagination-button-width: 40px; /* 增大分页按钮 */
  --el-pagination-button-height: 40px;
  --el-pagination-font-size: 15px;
  --el-pagination-bg-color: #ffffff;
  --el-pagination-button-color: #64748b;
  --el-pagination-button-disabled-bg-color: #f8fafc;
  --el-pagination-hover-color: #3b82f6;
}

.custom-pagination .btn-prev,
.custom-pagination .btn-next,
.custom-pagination .number {
  min-width: 40px;
  height: 40px;
  line-height: 40px;
  border-radius: 8px;
  margin: 0 4px;
  font-weight: 500;
}

.custom-pagination .number:hover,
.custom-pagination .number.is-active {
  background-color: #3b82f6;
  color: white;
}

.followee-user-card {
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 12px;
  background-color: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  border: 1px solid #f1f5f9; /* 更精致的边框 */
}

.followee-user-card:hover {
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
  border-color: #e2e8f0;
}

.pagination-container {
  padding: 16px;
  display: flex;
  justify-content: center;
  margin-top: 20px;
  background-color: white;
  border-top: 1px solid #f1f5f9;
  position: sticky;
  bottom: 0;
  z-index: 10;
}

/* 响应式调整 */
@media (max-width: 992px) {
  .profile-container {
    grid-template-columns: 1fr;
    padding: 16px;
    gap: 24px;
  }
  
  .sidebar {
    margin-right: 0;
    padding-right: 0;
  }
  
  .user-profile-wrapper {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .content-tabs {
    order: 2;
  }
  
  .sidebar {
    order: 1;
  }
  
  .sidejob-buttons {
    grid-template-columns: 1fr;
  }
  
  .sidejob-buttons .el-button {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .custom-drawer {
    width: 90% !important;
  }
  
  .followee-user-card {
    flex-direction: column;
  }
  
  .followee-actions {
    flex-direction: column;
  }
  
  .followee-actions .el-button {
    width: 100%;
  }
}

@media (max-width: 576px) {
  .profile-container {
    padding: 12px;
  }
  
  .username {
    font-size: 22px;
  }
  
  .followee-user-card {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .followee-actions {
    flex-direction: column;
    width: 100%;
  }
  
  .followee-actions .el-button {
    width: 100%;
  }
  
  .custom-drawer {
    width: 100% !important;
  }
  
  .activity-meta {
    flex-direction: column;
    gap: 8px;
  }
}

/* 我关注的圈子卡片样式 */
.followed-bar-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 16px;
  margin-bottom: 12px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  cursor: pointer;
  
  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    transform: translateY(-2px);
  }
}

.followed-bar-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.followed-bar-name {
  font-weight: 600;
  font-size: 16px;
  color: #1e293b;
}

.followed-bar-stats {
  display: flex;
  gap: 12px;
  
  .stat-item {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    font-size: 13px;
    color: #64748b;
    
    .el-icon {
      font-size: 14px;
    }
  }
}

.followed-bar-unread {
  position: absolute;
  right: 16px;
  
  .el-badge {
    :deep(.el-badge__content) {
      transform: scale(0.8);
      transform-origin: 100% 0;
    }
  }
}

.followed-bar-loading {
  text-align: center;
  padding: 20px;
  font-size: 14px;
  color: #64748b;
  background-color: #f8fafc;
  border-radius: 8px;
  margin: 12px;
}

/* 用户操作按钮组样式 */
.user-actions {
  display: flex;
  gap: 12px;
  margin-top: 12px;
}

.message-btn {
  transition: all 0.3s ease;
}

.message-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

/* 私信对话框样式 */
.message-dialog {
  border-radius: 12px;
  overflow: hidden;
}

.message-dialog .el-dialog__header {
  background-color: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
  margin-right: 0;
}

.message-dialog-content {
  padding: 20px;
}

.message-preview {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.message-bubble {
  max-width: 80%;
  padding: 12px 16px;
  background-color: #409EFF;
  color: white;
  border-radius: 18px 18px 0 18px;
  word-break: break-word;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.message-actions {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 消息历史抽屉样式 */
.message-history-drawer {
  --el-drawer-bg-color: #f8fafc;
}

.message-history-container {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message-item {
  display: flex;
  gap: 12px;
}

.message-sent {
  flex-direction: row-reverse;
}

.message-received {
  flex-direction: row;
}

.message-content {
  max-width: 70%;
}

.message-text {
  padding: 12px 16px;
  border-radius: 18px;
  word-break: break-word;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.message-sent .message-text {
  background-color: #409EFF;
  color: white;
  border-radius: 18px 18px 0 18px;
}

.message-received .message-text {
  background-color: white;
  color: #333;
  border-radius: 18px 18px 18px 0;
}

.message-time {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 4px;
  text-align: right;
}

.message-received .message-time {
  text-align: left;
}

.message-loading {
  text-align: center;
  padding: 20px;
  color: #94a3b8;
}

.no-messages {
  text-align: center;
  padding: 40px 20px;
  color: #94a3b8;
}

.message-input-container {
  position: sticky;
  bottom: 0;
  background-color: white;
  padding: 16px;
  border-top: 1px solid #e2e8f0;
}

.send-btn {
  margin-top: 10px;
  width: 100%;
}

/* 动画效果 */
.message-item {
  animation: fadeInUp 0.3s ease;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 消息对话框样式 */
.message-dialog {
  border-radius: 12px;
  overflow: hidden;
}

.message-dialog .el-dialog__header {
  padding: 16px 20px;
  background-color: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
  margin-right: 0;
}

.message-dialog .el-dialog__body {
  padding: 0;
}

.message-dialog-content {
  display: flex;
  flex-direction: column;
  height: 100%;
}

/* 消息历史容器 */
.message-history-container {
  flex: 1;
  padding: 20px;
  background-color: #f9f9f9;
  overflow-y: auto;
}

/* 消息项样式 */
.message-item {
  display: flex;
  margin-bottom: 16px;
  animation: fadeIn 0.3s ease;
}

.message-sent {
  justify-content: flex-end;
}

.message-received {
  justify-content: flex-start;
}

.message-avatar {
  margin-right: 12px;
  flex-shrink: 0;
}

.message-sent .message-avatar {
  order: 1;
  margin-right: 0;
  margin-left: 12px;
}

.message-content {
  max-width: 70%;
}

.message-meta {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.message-sent .message-meta {
  text-align: right;
}

.message-received .message-meta {
  text-align: left;
}

.message-sender {
  font-weight: 500;
  margin-right: 8px;
}

.message-text {
  padding: 12px 16px;
  border-radius: 18px;
  line-height: 1.5;
  word-break: break-word;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.message-sent .message-text {
  background-color: #409EFF;
  color: white;
  border-radius: 18px 18px 0 18px;
}

.message-received .message-text {
  background-color: white;
  color: #333;
  border-radius: 18px 18px 18px 0;
}

/* 消息输入区域 */
.message-input-area {
  padding: 16px;
  border-top: 1px solid #e4e7ed;
  background-color: white;
}

.message-actions {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 动画效果 */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 加载中样式 */
.message-loading {
  text-align: center;
  padding: 20px;
  color: #909399;
}

.no-messages {
  text-align: center;
  padding: 40px 20px;
  color: #909399;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .message-dialog {
    width: 90% !important;
  }
  
  .message-content {
    max-width: 80%;
  }
}

/* 消息对话框高级样式 */
.message-dialog {
  --el-drawer-bg-color: #f8fafc;
  border-radius: 12px;
  overflow: hidden;
}

.message-dialog .el-dialog__header {
  padding: 16px 24px;
  background-color: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
  margin-right: 0;
}

.message-dialog .el-dialog__body {
  padding: 0;
}

.message-dialog-content {
  display: flex;
  flex-direction: column;
  height: 100%;
}

/* 消息历史容器 */
.message-history-container {
  flex: 1;
  padding: 20px;
  background-color: #f9f9f9;
  overflow-y: auto;
}

/* 消息项基础样式 */
.message-item {
  display: flex;
  margin-bottom: 20px;
  max-width: 90%;
  animation: fadeIn 0.3s ease;
}

/* 发送的消息样式（靠右） */
.message-sent {
  margin-left: auto;
  flex-direction: row-reverse;
}

/* 接收的消息样式（靠左） */
.message-received {
  margin-right: auto;
  flex-direction: row;
}

/* 消息头像样式 */
.message-avatar {
  flex-shrink: 0;
  margin: 0 12px;
  align-self: flex-end;
}

/* 消息内容容器 */
.message-content-wrapper {
  display: flex;
  flex-direction: column;
  max-width: 80%;
}

/* 消息元信息（发送者和时间） */
.message-meta {
  font-size: 12px;
  color: #94a3b8;
  margin-bottom: 6px;
  display: flex;
  align-items: center;
}

.message-sent .message-meta {
  justify-content: flex-end;
}

.message-received .message-meta {
  justify-content: flex-start;
}

.message-sender {
  font-weight: 500;
  margin-right: 8px;
}

/* 消息气泡样式 */
.message-bubble {
  padding: 12px 16px;
  border-radius: 18px;
  line-height: 1.5;
  word-break: break-word;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  position: relative;
  animation: fadeInUp 0.3s ease;
}

/* 发送的消息气泡样式 */
.message-sent .message-bubble {
  background-color: #409EFF;
  color: white;
  border-radius: 18px 18px 0 18px;
  margin-left: 40px;
}

/* 接收的消息气泡样式 */
.message-received .message-bubble {
  background-color: white;
  color: #333;
  border-radius: 18px 18px 18px 0;
  margin-right: 40px;
}

/* 添加小三角指示器 */
.message-bubble:after {
  content: '';
  position: absolute;
  bottom: 0;
  width: 0;
  height: 0;
  border: 8px solid transparent;
}

.message-sent .message-bubble:after {
  right: -8px;
  border-left-color: #409EFF;
  border-right: 0;
  border-bottom: 0;
}

.message-received .message-bubble:after {
  left: -8px;
  border-right-color: white;
  border-left: 0;
  border-bottom: 0;
}

/* 消息输入区域 */
.message-input-area {
  padding: 16px;
  border-top: 1px solid #e4e7ed;
  background-color: white;
}

.message-actions {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 动画效果 */
@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 无消息提示 */
.no-messages {
  text-align: center;
  padding: 40px 20px;
  color: #94a3b8;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .message-dialog {
    width: 90% !important;
  }
  
  .message-content-wrapper {
    max-width: 75%;
  }
  
  .message-bubble {
    padding: 10px 14px;
  }
}

/* In the style section - update the message styling */
.message-dialog {
  --el-drawer-bg-color: #f8fafc;
  border-radius: 12px;
  overflow: hidden;
}

.message-dialog .el-dialog__header {
  padding: 16px 24px;
  background-color: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
  margin-right: 0;
}

.message-dialog .el-dialog__body {
  padding: 0;
}

.message-dialog-content {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.message-history-container {
  flex: 1;
  padding: 20px;
  background-color: #f5f7fa;
  overflow-y: auto;
}

.message-item {
  display: flex;
  margin-bottom: 20px;
  max-width: 100%;
  animation: fadeIn 0.3s ease;
  align-items: flex-end;
}

.message-sent {
  justify-content: flex-end;
  padding-right: 1px;
}

.message-received {
  justify-content: flex-start;
  padding-left: 1px;
}

.message-avatar {
  flex-shrink: 0;
  margin: 0 0 0 12px;
  align-self: flex-end;
}

.message-sent .message-avatar {
  order: 1;
  margin: 0 0 0 12px;
}

.message-received .message-avatar {
  order: -1;
  margin: 0 12px 0 0;
}

.message-content-wrapper {
  display: flex;
  flex-direction: column;
  max-width: 80%;
}

.message-meta {
  font-size: 12px;
  color: #94a3b8;
  margin-bottom: 6px;
  display: flex;
  align-items: center;
}

.message-sent .message-meta {
  justify-content: flex-end;
}

.message-received .message-meta {
  justify-content: flex-start;
}

.message-sender {
  font-weight: 500;
  margin-right: 8px;
  color: #333;
}

.message-time {
  font-size: 11px;
  color: #999;
}

.message-bubble {
  padding: 12px 16px;
  border-radius: 18px;
  line-height: 1.5;
  word-break: break-word;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  position: relative;
  animation: fadeInUp 0.3s ease;
}

.message-sent .message-bubble {
  background-color: #409EFF;
  color: white;
  border-radius: 18px 18px 0 18px;
}

.message-received .message-bubble {
  background-color: white;
  color: #333;
  border-radius: 18px 18px 18px 0;
  border: 1px solid #e4e7ed;
}

.message-bubble:after {
  content: '';
  position: absolute;
  bottom: 0;
  width: 0;
  height: 0;
  border: 8px solid transparent;
}

.message-sent .message-bubble:after {
  right: -8px;
  border-left-color: #409EFF;
  border-right: 0;
  border-bottom: 0;
}

.message-received .message-bubble:after {
  left: -8px;
  border-right-color: white;
  border-left: 0;
  border-bottom: 0;
}

.message-input-area {
  padding: 16px;
  border-top: 1px solid #e4e7ed;
  background-color: white;
}

.message-actions {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.no-messages {
  text-align: center;
  padding: 40px 20px;
  color: #94a3b8;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 在style部分，更新消息样式 */
.message-item {
  display: flex;
  margin-bottom: 16px;
  width: 100%;
}

.message-sent {
  justify-content: flex-end;
}

.message-received {
  justify-content: flex-start;
}

.message-content-wrapper {
  max-width: 70%;
}

.message-sent .message-content-wrapper {
  margin-left: auto;
  align-items: flex-end;
}

.message-received .message-content-wrapper {
  margin-right: auto;
  align-items: flex-start;
}

.message-meta {
  font-size: 12px;
  color: #94a3b8;
  margin-bottom: 6px;
  display: flex;
  align-items: center;
}

.message-sent .message-meta {
  justify-content: flex-end;
}

.message-received .message-meta {
  justify-content: flex-start;
}

.message-bubble {
  padding: 12px 16px;
  border-radius: 18px;
  line-height: 1.5;
  word-break: break-word;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.message-sent .message-bubble {
  background-color: #409EFF;
  color: white;
  border-radius: 18px 18px 0 18px;
}

.message-received .message-bubble {
  background-color: white;
  color: #333;
  border-radius: 18px 18px 18px 0;
}

.message-avatar {
  flex-shrink: 0;
  margin: 0 12px;
  align-self: flex-end;
}

.message-sent .message-avatar {
  order: -1;
  margin-left: 12px;
  margin-right: 0;
}

.message-received .message-avatar {
  order: -1;
  margin-right: 12px;
  margin-left: 0;
}
/* 添加平滑过渡效果 */
.message-item {
  transition: all 0.3s ease;
}

.message-history-container {
  transition: height 0.3s ease;
}

/* 添加加载指示器 */
.load-more-indicator {
  text-align: center;
  padding: 10px;
  color: #999;
  font-size: 14px;
}

.load-more-indicator .el-icon {
  animation: spin 1s linear infinite;
  margin-right: 5px;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 消息进入动画 */
@keyframes messageIn {
  0% {
    opacity: 0;
    transform: translateY(10px);
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
}

.message-item {
  animation: messageIn 0.3s ease-out forwards;
}

/* 新消息提示效果 */
.new-message-indicator {
  text-align: center;
  padding: 8px;
  font-size: 12px;
  color: #999;
  animation: fadeInOut 2s ease-in-out infinite;
}

@keyframes fadeInOut {
  0%, 100% { opacity: 0.5; }
  50% { opacity: 1; }
}
</style>