<template>
  <el-dialog
    v-model="visible"
    title="私信对话"
    width="600px"
    :close-on-click-modal="false"
    custom-class="message-dialog"
    @closed="handleDialogClosed"
    :lock-scroll="true"
  >
    <div class="message-dialog-content">
      <!-- 消息历史区域 -->
      <div class="message-history-container" v-loading="loading">
        <el-scrollbar height="400px" @scroll="handleScroll">
          <div 
            v-for="(msg, index) in messageHistory" 
            :key="index"
            class="message-item"
            :class="{'message-sent': msg.isSelf, 'message-received': !msg.isSelf}"
            :ref="index === 0 ? 'firstMessageRef' : null"
          >
            <!-- 接收的消息头像在左边 -->
            <div class="message-avatar" v-if="!msg.isSelf">
              <el-avatar :size="40" :src="msg.senderAvatar" />
            </div>
            
            <!-- 消息内容 -->
            <div class="message-content-wrapper">
              <div class="message-meta">
                <span class="message-sender">{{ msg.isSelf ? '你' : msg.senderName }}</span>
                <span class="message-time">{{ formatMessageTime(msg.createdAt) }}</span>
                <!-- 添加已读未读标签 (仅显示在自己发送的消息上) -->
                <span 
                  v-if="msg.isSelf && Array.isArray(msg.readUserNames)" 
                  class="read-status"
                  :class="{'read': msg.readUserNames.length === 0, 'unread': msg.readUserNames.length > 0}"
                >
                  <el-tooltip 
                    v-if="msg.readUserNames.length > 0"
                    effect="light" 
                    placement="top"
                    :content="`未读: ${msg.readUserNames.join(', ')}`"
                  >
                    <span class="status-dot"></span>
                    <span class="status-text">未读</span>
                  </el-tooltip>
                  <el-tooltip 
                    v-else
                    effect="light" 
                    placement="top"
                    content="已读"
                  >
                    <span class="status-dot"></span>
                    <span class="status-text">已读</span>
                  </el-tooltip>
                </span>
              </div>
              <div class="message-bubble">
                {{ msg.content }}
              </div>
            </div>
            
            <!-- 发送的消息头像在右边 -->
            <div class="message-avatar" v-if="msg.isSelf">
              <el-avatar :size="40" :src="msg.senderAvatar" />
            </div>
          </div>
          
          <div v-if="messageHistory.length === 0 && !loading" class="no-messages">
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
          <el-button @click="visible = false">取消</el-button>
          <el-button 
            type="primary" 
            @click="sendMessage"
            :loading="sending"
            :disabled="!messageContent.trim()"
          >
            发送
          </el-button>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { post } from '@/net'
import { debounce } from 'lodash-es'


const props = defineProps({
  // 当前用户信息
  currentUser: {
    type: Object,
    required: true
  },
  // 目标用户信息
  targetUser: {
    type: Object,
    required: false
  },
  //会话ID
  chatConversationId: {  
    type: Number,
    default: null
  },
  //会话类型
  chatType: {  
    type: Number,
    default: null
  },
  // 是否显示对话框
  modelValue: {
    type: Boolean,
    default: false
  },
  
})

const emit = defineEmits(['update:modelValue'])

const visible = computed({
  get() {
    return props.modelValue
  },
  set(value) {
    emit('update:modelValue', value)
  }
})

const messageContent = ref('')
const sending = ref(false)
const messageHistory = ref([])
const loading = ref(false)
const chatHistory = ref([])
const chatPage = ref(1)
const chatSize = ref(10)
const chatTotal = ref(0)
const isNewConversation = ref(false)
const firstMessageRef = ref(null)
const pollInterval = ref(null)
const checkReadStatusInterval = ref(null)

// 格式化消息时间
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

// 打开对话框时加载聊天记录
const loadChatHistory = async () => {
  // 重置聊天状态
  chatHistory.value = []
  chatPage.value = 1
  isNewConversation.value = false
  
  try {
    loading.value = true

    const requestData = {
      page: chatPage.value,
      size: chatSize.value
    }

    if(props.chatConversationId) {
      requestData.chatId = props.chatConversationId
    }
    if(props.targetUser?.secrecyId) {
      requestData.secrecyId = props.targetUser.secrecyId
    }
    const res = await post('/api/auth/chat/getChatHistory', requestData)
    
    console.log('获取聊天历史:', res)
    if (res.records && res.records.length > 0) {
      chatHistory.value = res.records
      chatTotal.value = res.total
      
      formatChatHistory()
      // 确保DOM更新后滚动到底部
      nextTick(() => {
        scrollToBottom('auto')
      })
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
    loading.value = false
  }
}

// 格式化聊天历史
const formatChatHistory = () => {
  messageHistory.value = chatHistory.value.map(msg => {
    // 直接从API返回的数据中获取所有必要字段
    return {
      chatMessageId: msg.chatMessageId,
      chatConversationId: msg.chatConversationId,
      senderId: msg.senderId,
      senderName: msg.senderName,
      senderAvatar: msg.senderAvatar,
      content: msg.content,
      messageType: msg.messageType,
      createdAt: msg.createdTime,
      status: msg.status,
      readUserNames: Array.isArray(msg.readUserNames) ? msg.readUserNames : [],
      isSelf: msg.isSelf // 直接使用API返回的isSelf字段
    };
  });
};

// 开始轮询
const startPolling = () => {
  // 先清除已有定时器
  stopPolling()
  // 每2秒获取一次新消息
  pollInterval.value = setInterval(fetchNewMessages, 2000)
  // 每3秒专门检查一次已读状态
  checkReadStatusInterval.value = setInterval(checkReadStatus, 3000)
}

// 停止轮询
const stopPolling = () => {
  if (pollInterval.value) {
    clearInterval(pollInterval.value)
    pollInterval.value = null
  }
  if(checkReadStatusInterval.value) {
    clearInterval(checkReadStatusInterval.value)
    checkReadStatusInterval.value = null
  }
}

// 专门检查已读状态
const checkReadStatus = async () => {
  if (!props.chatConversationId) return
  
  try {
    const res = await post('/api/auth/chat/checkReadStatus', {
      conversationId: props.chatConversationId,
      userId: props.currentUser.id
    })

    if (res && Array.isArray(res.readStatus)) {
      // 创建消息ID到未读用户列表的映射
      const unreadStatusMap = new Map()
      res.readStatus.forEach(status => {
        unreadStatusMap.set(status.messageId, Array.isArray(status.unreadUsers) ? [...status.unreadUsers] : [])
      })
      
      let shouldUpdate = false
      const newMessageHistory = messageHistory.value.map(msg => {
        // 只处理自己发送的消息
        if (msg.isSelf) {
          const newReadUserNames = unreadStatusMap.has(msg.chatMessageId) 
            ? unreadStatusMap.get(msg.chatMessageId)
            : [] // 后端没返回的消息视为已读
            
          // 检查状态是否变化
          if (JSON.stringify(msg.readUserNames) !== JSON.stringify(newReadUserNames)) {
            shouldUpdate = true
            return {
              ...msg,
              readUserNames: newReadUserNames
            }
          }
        }
        return msg
      })
      
      if (shouldUpdate) {
        messageHistory.value = newMessageHistory
      }
    }
  } catch (error) {
    console.error('检查已读状态失败:', error)
  }
}

// 获取新消息
const fetchNewMessages = async () => {
  if (!props.chatConversationId || loading.value) return
  
  try {
    const res = await post('/api/auth/chat/getNewMessages', {
      chatConversationId: props.chatConversationId,
      lastMessageId: chatHistory.value.length > 0 
        ? chatHistory.value[chatHistory.value.length - 1].chatMessageId 
        : null
    })
    
    if (res && res.length > 0) {
      // 只添加新消息
      const newMessages = res.filter(newMsg => 
        !chatHistory.value.some(existingMsg => 
          existingMsg.chatMessageId === newMsg.chatMessageId
        )
      )
      
      if (newMessages.length > 0) {
        chatHistory.value.push(...newMessages)
        formatChatHistory()
        
        // 如果是我发送的消息被对方阅读了，更新状态
        updateReadStatusForMyMessages()
        
        // 平滑滚动到底部
        nextTick(() => {
          scrollToBottom('smooth')
        })
      }
    }
  } catch (error) {
    console.error('获取新消息失败:', error)
  }
}

// 更新我发送的消息的已读状态
const updateReadStatusForMyMessages = () => {
  chatHistory.value.forEach(msg => {
    if (msg.isSelf && msg.readUserNames) {
      // 如果这条消息的未读用户列表中不包含当前用户，则标记为已读
      const isRead = !msg.readUserNames.includes(props.targetUser?.username)
      if (isRead) {
        msg.readUserNames = []
      }
    }
  })
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

const handleScroll = debounce(({ scrollTop }) => {
  const scrollContainer = document.querySelector('.message-history-container .el-scrollbar__wrap')
  if (!scrollContainer) return
  
  if (scrollTop < 100 && !loading.value && chatPage.value * chatSize.value < chatTotal.value) {
    loadMoreMessages()
  }
}, 200)

const loadMoreMessages = async () => {
  if (loading.value || chatPage.value * chatSize.value >= chatTotal.value) return
  
  try {
    loading.value = true
    
    // 记录当前第一条消息的位置
    const scrollContainer = document.querySelector('.message-history-container .el-scrollbar__wrap')
    const firstMessage = document.querySelector('.message-item:first-child')
    const prevScrollHeight = scrollContainer.scrollHeight
    const prevFirstMessageOffset = firstMessage ? firstMessage.offsetTop : 0
    
    chatPage.value++
    const requestData = {
      page: chatPage.value,
      size: chatSize.value
    }
    if(props.targetUser) {
      requestData.secrecyId = props.targetUser.secrecyId
    }
    if(props.chatConversationId) {
      requestData.chatId = props.chatConversationId
    }

    const res = await post('/api/auth/chat/getChatHistory', requestData)
    
    if (res.records && res.records.length > 0) {
      // 将新数据插入到数组开头
      const newRecords = res.records.map(msg => ({
        chatMessageId: msg.chatMessageId,
        chatConversationId: msg.chatConversationId,
        senderId: msg.senderId,
        senderName: msg.senderName,
        senderAvatar: msg.senderAvatar,
        content: msg.content,
        messageType: msg.messageType,
        createdAt: msg.createdTime,
        status: msg.status,
        readUserNames: Array.isArray(msg.readUserNames) ? msg.readUserNames : [],
        isSelf: msg.isSelf // 直接使用API返回的isSelf字段
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
    loading.value = false
  }
}

// 发送消息
const sendMessage = async () => {
  if (!messageContent.value.trim()) return
  console.log("发送消息",props)
  sending.value = true
  try {
    const requestData = {
      content: messageContent.value,
    }
    if (props.chatConversationId) {
      requestData.chatConversationId = props.chatConversationId
    }
    if(props.chatType) {
      requestData.chatType = props.chatType
    }
    const res = await post('/api/auth/chat/sendMessage', requestData)
    if (res) {
      if (!props.chatConversationId && res) {
        // props.chatConversationId.value = res.conversationId
        isNewConversation.value = false
      }
      
      // 直接使用API返回的数据结构
      const newMsg = {
        chatMessageId: res.chatMessageId,
        chatConversationId: props.chatConversationId,
        senderId: props.currentUser.id,
        senderName: props.currentUser.username,
        senderAvatar: props.currentUser.avatarUrl,
        isSelf: true,
        content: messageContent.value,
        messageType: 1,
        createdTime: new Date().toISOString(),
        status: 1,
        readUserNames: res.readUserNames
      }
      
      chatHistory.value.push(newMsg)
      formatChatHistory()
      messageContent.value = ''
      
      // 发送消息时，将对方发送的所有消息标记为已读
      markOtherMessagesAsRead()
      
      scrollToBottom('smooth')
    }
  } catch (error) {
    console.error('发送消息失败:', error)
    ElMessage.error('发送消息失败')
  } finally {
    sending.value = false
  }
}

// 将对方发送的消息标记为已读
const markOtherMessagesAsRead = async () => {
  const unreadMessages = chatHistory.value.filter(
    msg => !msg.isSelf && msg.readUserNames?.includes(props.currentUser.username)
  )
  
  if (unreadMessages.length > 0) {
    try {
      await post('/api/auth/chat/markMessagesAsRead', {
        messageIds: unreadMessages.map(msg => msg.chatMessageId),
        readerId: props.currentUser.id
      })
      
      // 更新本地状态
      unreadMessages.forEach(msg => {
        msg.readUserNames = msg.readUserNames.filter(
          name => name !== props.currentUser.username
        )
      })
    } catch (error) {
      console.error('更新消息已读状态失败:', error)
    }
  }
}

// 对话框关闭时的处理
const handleDialogClosed = () => {
  stopPolling()
}

// 监听对话框显示状态
watch(visible, (newVal) => {
  if (newVal) {
    loadChatHistory()
  } else {
    stopPolling()
  }
})

// 组件卸载时停止轮询
onUnmounted(() => {
  stopPolling()
  handleScroll.cancel()

})
</script>

<style scoped>
/* 保留原有的样式，确保与之前一致 */
.message-dialog {
   position: fixed !important;
  top: 50% !important;
  left: 50% !important;
  transform: translate(-50%, -50%) !important;
  margin: 0 !important;
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
  overflow: hidden;
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
  flex-shrink: 0;
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

.read-status {
  margin-left: 8px;
  font-size: 12px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  transition: all 0.3s ease;
}

.read-status .status-dot {
  display: inline-block;
  width: 6px;
  height: 6px;
  border-radius: 50%;
}

.read-status.read {
  color: #67c23a;
}

.read-status.read .status-dot {
  background-color: #67c23a;
}

.read-status.unread {
  color: #e6a23c;
}

.read-status.unread .status-dot {
  background-color: #e6a23c;
}

.status-text {
  margin-left: 2px;
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
</style>