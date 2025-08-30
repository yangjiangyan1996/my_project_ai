<template>
  <div class="index-container">
    <!-- 新增私聊图标 -->
    <el-popover
      placement="bottom-end"
      trigger="click"
      width="350"
      v-model:visible="chatListVisible"
      popper-class="chat-popover"
      :teleported="false"
    >
      <template #reference>
        <div class="chat-icon" @mouseenter="showTooltip = true" @mouseleave="showTooltip = false">
          <el-badge :value="totalUnreadCount" :max="99" class="badge" :hidden="totalUnreadCount === 0">
            <el-icon :size="20"><Message /></el-icon>
          </el-badge>
          <el-tooltip 
            v-if="showTooltip"
            content="私聊消息"
            placement="bottom"
            effect="light"
          />
        </div>
      </template>
      
      <!-- 私聊列表内容 -->
      <div class="chat-list-container">
        <div class="chat-list-header">
          <h3>私聊消息</h3>
          <!-- <el-button 
            v-if="totalUnreadCount > 0"
            type="text" 
            size="small" 
            @click.stop="markAllChatsAsRead"
            class="mark-read-btn"
          >
            <el-icon><CircleCheck /></el-icon>
            全部已读
          </el-button> -->
        </div>
        
        <el-scrollbar height="400px" class="chat-scrollbar">
          <transition-group name="chat-list" tag="div">
            <div 
              v-for="chat in chatList" 
              :key="chat.chatId" 
              class="chat-item"
              :class="{ 'unread-chat': chat.unreadCount > 0 }"
              @click="openChatDialog(chat)"
            >
              <div class="chat-avatar">
                <el-avatar :size="40" :src="chat.avatar" />
                <el-badge 
                  :value="chat.unreadCount" 
                  :max="99" 
                  class="chat-badge" 
                  v-if="chat.unreadCount > 0"
                />
              </div>
              
              <div class="chat-content">
                <div class="chat-header">
                  <span class="chat-title">{{ chat.title }}</span>
                  <span class="chat-time">{{ formatChatTime(chat.lastActiveAt) }}</span>
                </div>
                <div class="chat-preview">
                  {{ chat.lastMessage || '暂无消息' }}
                </div>
              </div>
            </div>
          </transition-group>
          
          <div v-if="chatLoading" class="loading-more">
            <el-icon class="is-loading"><Loading /></el-icon>
            <span>加载中...</span>
          </div>
          <div v-else-if="!chatHasMore && chatList.length > 0" class="no-more">
            没有更多聊天了
          </div>
          <div v-else-if="chatList.length === 0" class="no-message">
            <el-empty description="暂无私聊消息" :image-size="80" />
          </div>
        </el-scrollbar>
      </div>
    </el-popover>

    <!-- 通知 -->
    <el-popover
      placement="bottom-end"
      trigger="click"
      width="350"
      v-model:visible="messageVisible"
    >
      <template #reference>
        <div class="message-bell" @click="handleMessageClick">
          <el-badge :value="unreadCount" :max="99" class="badge">
            <el-icon :size="20"><bell /></el-icon>
          </el-badge>
        </div>
      </template>
      
      <el-tabs v-model="activeMessageTab" class="message-tabs">
        <div class="mark-all-read-container">
          <el-button 
            type="text" 
            size="small" 
            @click="markAllAsRead(activeMessageTab)"
            :disabled="unreadCount === 0"
          >
            <el-icon><CircleCheck /></el-icon>
            一键已读
          </el-button>
        </div>
        <el-tab-pane label="评论回复" name="comment">
          <div class="message-list">
            <div 
              v-for="item in commentMessages" 
              :key="item.id" 
              class="message-item" 
              @click="showCommentDetail(item)"
              :class="{ 'unread-message': item.isRead === 0 }"
            >
              <div class="message-unread-dot" v-if="item.isRead === 0"></div>
              <div class="message-content">
                <div class="message-header">
                  <span class="message-time">{{ formatTime(item.createdAt) }}</span>
                </div>
                <div class="message-text">
                  <span class="sender-name">{{ item.senderName }}</span>
                  <span class="message-content-text">{{ item.content }}</span>
                  <span class="related-words">"{{ item.relatedWords }}"</span>
                </div>
              </div>
            </div>
          
            <div v-if="commentLoading" class="loading-more">
              <el-icon class="is-loading"><Loading /></el-icon>
            </div>
            <div v-else-if="commentHasMore" class="load-more" @click="loadMoreComments">
              加载更多
            </div>
            <div v-else-if="commentMessages.length === 0" class="no-message">
              暂无消息
            </div>
          </div>
        </el-tab-pane>

        <!-- 评论详情对话框 -->
        <el-dialog
          v-model="commentDialogVisible"
          title="评论详情"
          width="60%"
          top="5vh"
          @closed="handleDialogClosed"
          class="comment-detail-dialog"
        >
          <div class="comment-dialog-container">
            <div class="comment-list" ref="commentListRef">
              <div 
                v-for="comment in allComments" 
                :key="comment.id" 
                class="comment-item"
                :class="{ 'highlight-comment': comment.id === currentCommentId }"
                ref="commentItems"
              >
                <div class="comment-header">
                  <el-avatar :src="comment.avatar || '/images/default-avatar.png'" size="small" class="user-avatar">{{ comment.username?.charAt(0) || '匿' }}</el-avatar>
                  <strong class="username" @click.stop="goToUserProfile(comment.secrecyId)">{{ comment.username || '匿名用户' }}</strong>
                  <span class="comment-time">{{ formatTime(comment.createdAt) }}</span>
                </div>
                
                <div v-if="comment.deleted" class="deleted">该评论已被删除</div>
                <div v-else v-html="renderMarkdown(comment.content)" class="md-content" />
                
                <div class="comment-actions">
                  <el-button text @click="replyToDialog(comment.id, comment.username)">回复</el-button>
                  <el-button text @click="likeDialogComment(comment.id)">👍 {{ comment.likes || 0 }}</el-button>
                  <el-button text v-if="comment.isMine" @click="deleteDialogComment(comment.id)">删除</el-button>
                  <el-button text v-if="comment.replies?.length" @click="toggleExpandDialog(comment.id)">
                    {{ expandedDialogComments[comment.id] ? '收起回复' : `展开回复 (${comment.replies.length})` }}
                  </el-button>
                </div>

                <!-- 回复列表 -->
                <div v-if="expandedDialogComments[comment.id] && comment.replies?.length" class="replies">
                  <div v-for="reply in comment.replies" :key="reply.id" class="reply-item">
                    <div class="comment-header">
                      <el-avatar :src="reply.avatar || '/images/default-avatar.png'" size="small" class="user-avatar">{{ reply.username?.charAt(0) || '匿' }}</el-avatar>
                      <strong class="username" @click.stop="goToUserProfile(reply.secrecyId)">{{ reply.username || '匿名用户' }}</strong>
                      <span class="comment-time">{{ formatTime(reply.createdAt) }}</span>
                    </div>
                    
                    <div v-if="reply.deleted" class="deleted">该回复已被删除</div>
                    <div v-else class="reply-content">
                      <template v-if="reply.replyToName">@{{ reply.replyToName }}：</template>
                      <span v-html="renderMarkdown(reply.content)" class="md-content" />
                    </div>
                    
                    <div class="comment-actions">
                      <el-button text @click="replyToDialog(reply.id, reply.username)">回复</el-button>
                      <el-button text @click="likeDialogComment(reply.id)">👍 {{ reply.likes || 0 }}</el-button>
                      <el-button text v-if="reply.isMine" @click="deleteDialogComment(reply.id)">删除</el-button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            
            <!-- 回复输入框 -->
            <div class="zhihu-comment-editor" v-if="activeReplyCommentId">
              <div class="editor-header">
                <div class="avatar">{{ userInitial }}</div>
                <span>回复 {{ replyToUsername }}</span>
              </div>
              <el-input
                v-model="replyContent"
                type="textarea"
                :placeholder="`回复 ${replyToUsername}...`"
                :autosize="{ minRows: 4, maxRows: 8 }"
                class="markdown-textarea"
              />
              <div class="editor-footer">
                <el-popover placement="top" width="250" trigger="click">
                  <template #reference>
                    <el-button size="small" text type="primary">😊 表情</el-button>
                  </template>
                  <div class="emoji-list">
                    <span v-for="emoji in emojis" :key="emoji" class="emoji" @click="insertEmojiToReply(emoji)">
                      {{ emoji }}
                    </span>
                  </div>
                </el-popover>
                
                <button class="cancel-button" @click="cancelReply">取消</button>
                <button class="submit-button" @click="submitDialogReply">发布</button>
              </div>
            </div>
          </div>
          
          <template #footer>
            <el-button @click="commentDialogVisible = false">关闭</el-button>
          </template>
        </el-dialog>
        
        <el-tab-pane label="新增关注" name="follow">
          <div class="message-list">
            <div 
              v-for="item in followMessages" 
              :key="item.id" 
              class="message-item"
              :class="{ 'unread-message': item.isRead === 0 }"
              @click="goToUserProfile(item.secrecyId)" 
            >
              <div class="message-unread-dot" v-if="item.isRead === 0"></div>
              <div class="message-avatar">
                <el-avatar :src="item.senderAvatar || '/images/default-avatar.png'" />
              </div>
              <div class="message-content">
                <div class="message-header">
                  <span class="message-user">{{ item.senderName }}</span>
                  <span class="message-time">{{ formatTime(item.createTime) }}</span>
                </div>
                <div class="message-text">关注了你</div>
              </div>
              <div class="follow-back-container" v-if="item.needFollow">
                <el-button 
                  size="small" 
                  type="primary" 
                  plain 
                  @click.stop="followBack(item.senderId)"
                  :loading="item.followLoading"
                >
                  回关
                </el-button>
              </div>
            </div>
            <div v-if="followLoading" class="loading-more">
              <el-icon class="is-loading"><Loading /></el-icon>
            </div>
            <div v-else-if="followHasMore" class="load-more" @click="loadMoreFollows">
              加载更多
            </div>
            <div v-else-if="followMessages.length === 0" class="no-message">
              暂无消息
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="申请通知" name="apply">
          <div class="message-list">
            <div 
              v-for="item in applyMessages" 
              :key="item.id" 
              class="message-item"
              :class="{ 'unread-message': item.isRead === 0 }"
              @click="showApplyDetail(item)"
            >
              <div class="message-unread-dot" v-if="item.isRead === 0"></div>
              <div class="message-content">
                <div class="message-header">
                  <span class="message-time">{{ formatTime(item.createdAt) }}</span>
                </div>
                <div class="message-text">
                  <span class="sender-name">{{ item.senderName }}</span>
                  <span class="message-content-text">{{ item.content }}</span>
                  <span class="related-words">"{{ item.relatedWords }}"</span>
                </div>
              </div>
            </div>

            <div v-if="applyLoading" class="loading-more">
              <el-icon class="is-loading"><Loading /></el-icon>
            </div>
            <div v-else-if="applyHasMore" class="load-more" @click="loadMoreApplies">
              加载更多
            </div>
            <div v-else-if="applyMessages.length === 0" class="no-message">
              暂无消息
            </div>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="点赞通知" name="like">
          <div class="message-list">
            <div 
              v-for="item in likeMessages" 
              :key="item.id" 
              class="message-item"
              :class="{ 'unread-message': item.isRead === 0 }"
              @click="showCommentDetail(item)"
            >
              <div class="message-unread-dot" v-if="item.isRead === 0"></div>
              
              <div class="message-content">
                <div class="message-header">
                  <span class="message-time">{{ formatTime(item.createdAt) }}</span>
                </div>
                <div class="message-text">
                  <span class="sender-name">{{ item.senderName }}</span>
                  <span class="message-content-text">{{ item.content }}</span>
                  <span class="related-words">"{{ item.relatedWords }}"</span>
                </div>
              </div>
            </div>

            <div v-if="likeLoading" class="loading-more">
              <el-icon class="is-loading"><Loading /></el-icon>
            </div>
            <div v-else-if="likeHasMore" class="load-more" @click="loadMoreLikes">
              加载更多
            </div>
            <div v-else-if="likeMessages.length === 0" class="no-message">
              暂无消息
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-popover>

    <!-- 头像下拉菜单 -->
    <template v-if="state.data.id">
      <el-dropdown class="avatar-dropdown" trigger="click">
        <div class="avatar-wrapper">
          <el-avatar :src="state.data.avatarUrl || '/images/default-avatar.png'" />
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="changeDisplayMode('myInfo')">
              <i class="el-icon-user"></i>我的
            </el-dropdown-item>
            <el-dropdown-item divided @click="userLogout">
              <i class="el-icon-switch-button"></i>退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </template>
    <template v-else>
      <el-button 
        class="login-button" 
        type="primary" 
        size="small" 
        @click="goToLogin"
      >
        登录/注册
      </el-button>
    </template>

    <el-menu 
      mode="horizontal"
      background-color="#f8f9fa"
      text-color="#2c3e50"
      active-text-color="#409EFF"
      class="nav-menu"
    >
      <el-sub-menu index="1">
        <template #title><i class="el-icon-data-analysis"></i>项目展示</template>
        <el-menu-item index="fuye" @click="changeDisplayMode('project')">
          热门副业
        </el-menu-item>
        <el-menu-item index="ranking" @click="changeDisplayMode('rankingList')">
          副业榜单
        </el-menu-item>
      </el-sub-menu>

      <el-sub-menu index="2">
        <template #title><i class="el-icon-chat-dot-round"></i>社区互动</template>
        <el-menu-item index="forum" @click="changeDisplayMode('quanList')">圈子论坛</el-menu-item>
      </el-sub-menu>

      <el-sub-menu index="3">
        <template #title><i class="el-icon-user"></i>找人合作</template>
        <el-menu-item index="skill-match" @click="changeDisplayMode('skillMatch')">人才榜</el-menu-item>
        <el-menu-item index="skill-match" @click="changeDisplayMode('talentMatch')">人才匹配</el-menu-item>
      </el-sub-menu>

      <!-- <el-sub-menu index="4">
        <template #title><i class="el-icon-guide"></i>导航工具</template>
        <el-menu-item index="fuyeceping" @click="router.push({ name: 'survey' })">副业测评</el-menu-item>
        <el-menu-item index="gongjuxiang" @click="router.push({ name: 'toolbox' })">工具箱</el-menu-item>
        <el-menu-item index="ziyuandaohang" @click="router.push({ name: 'resources' })">资源导航</el-menu-item>
      </el-sub-menu> -->

      <!-- <el-sub-menu index="5">
        <template #title><i class="el-icon-s-custom"></i>AI推荐</template>
        <el-menu-item index="ai-assistant" @click="changeDisplayMode('growthCenter')">副业推荐助手</el-menu-item>
      </el-sub-menu> -->
    </el-menu>

    <!-- 搜索区域 -->
    <div v-if="displayMode === 'project'">
      <div class="search-bar">
        <el-input v-model="search.name" placeholder="搜索副业名称" style="width: 200px; margin-right: 10px" />
        <el-select
          v-model="search.categories"
          placeholder="选择分类"
          multiple
          filterable
          collapse-tags
          style="width: 300px; margin-right: 10px"
        >
          <el-option-group
            v-for="group in categories"
            :key="group.code"
            :label="group.desc"
          >
            <el-option
              :label="group.desc"
              :value="group.code"
            />
            <el-option
              v-for="sub in group.subs"
              :key="sub.code"
              :label="sub.desc"
              :value="sub.code"
            />
          </el-option-group>
        </el-select>
        <el-select
          v-model="search.difficulties"
          placeholder="选择难度"
          multiple
          style="width: 180px; margin-right: 10px"
        >
          <el-option
            v-for="item in difficulties"
            :key="item.code"
            :label="item.desc"
            :value="item.code"
          />
        </el-select>
        <el-button type="primary" @click="onSearch">搜索</el-button>
      </div>
      <!-- 项目展示区域 -->
     
      <div class="main-content">
        <div class="project-container">
          <el-row :gutter="20" class="project-list">
            <el-col 
              v-for="project in projectList" 
              :key="project.id" 
              :xs="24" :sm="12" :md="8" :lg="6"
            >
              <el-card class="project-card" shadow="hover" @click="goToDetail(project)">
                <img 
                  :src="project.imageUrl" 
                  class="project-image"
                  alt="项目封面"
                />
                <div class="project-content">
                  <h3 class="project-title">{{ project.name }}</h3>
                  <div class="project-meta">
                    <el-tag size="small">{{ project.firstCategoryName }}</el-tag>
                    <el-tag size="small">{{ project.secondCategoryName }}</el-tag>
                    <span class="project-difficulty">{{ project.difficulty }}</span>
                  </div>
                  <p class="project-description">{{ project.description }}</p>
                </div>
              </el-card>
            </el-col>
          </el-row>

          <!-- 分页控件 -->
          <div v-if="total > 0" class="pagination-container">
            <div class="pagination-info">
              共 {{ total }} 条记录，第 {{ currentPage }} / {{ totalPages }} 页
            </div>
            <div class="pagination-buttons">
              <el-button 
                :disabled="currentPage <= 1" 
                @click="prevPage"
                class="pagination-btn"
              >
                <el-icon><ArrowLeft /></el-icon>
                上一页
              </el-button>
              
              <!-- 页码按钮 -->
              <el-button 
                v-for="page in Math.min(5, totalPages)" 
                :key="page"
                :type="currentPage === page ? 'primary' : ''"
                @click="goToPage(page)"
                class="page-btn"
              >
                {{ page }}
              </el-button>
              
              <!-- 省略号 -->
              <span v-if="totalPages > 5" class="ellipsis">...</span>
              
              <el-button 
                :disabled="currentPage >= totalPages" 
                @click="nextPage"
                class="pagination-btn"
              >
                下一页
                <el-icon><ArrowRight /></el-icon>
              </el-button>
            </div>
          </div>

          <div v-if="!loading && projectList.length === 0" class="no-data">
            <el-empty description="暂无项目数据" :image-size="100" />
          </div>
        </div>
      </div>

    </div>

    <!-- 私信对话框 -->
    <ChatDialog 
      v-model="chatVisible"
      :current-user="currentUser"
      :target-user="targetUser"
      :chat-conversation-id="currentChatId"
      :chat-type="chatType"
    />

    <!-- 组件展示区域 -->
    <SkillMatch v-if="displayMode === 'skillMatch'" />
    <MyInfo v-if="displayMode === 'myInfo'" />
    <TalentMatch v-if="displayMode === 'talentMatch'" />
    <RankingList v-if="displayMode === 'rankingList'" />
    <QuanList v-if="displayMode === 'quanList'" />
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch, nextTick } from 'vue';
import { Loading, Bell, CircleCheck, Message } from '@element-plus/icons-vue';
import router from "@/router";
import { logout, post, get } from '@/net';
import { ElMessage, ElMessageBox } from 'element-plus';
import SkillMatch from '@/views/SkillMatch.vue';
import TalentMatch from '@/views/TalentMatch.vue';
import RankingList from '@/views/RankingList.vue';
import QuanList from '@/views/QuanList.vue';
import MyInfo from '@/views/My.vue';
import useUserInfo from '@/hooks/useUserInfo';
import ChatDialog from '@/components/ChatDialog.vue';
import markdownIt from 'markdown-it';
import emoji from 'markdown-it-emoji';

const { state, loadUserInfo } = useUserInfo();
const displayMode = ref('project')
const projectList = ref([]);
const loading = ref(false);
const currentPage = ref(1);
const pageSize = ref(8);
const totalPages = ref(0);
const total = ref(0);
const hasMore = ref(true);
const categories = ref([]);
const difficulties = ref([]);
const search = ref({ 
  name: '', 
  categories: [],
  difficulties: [] 
});

// 消息相关状态
const messageVisible = ref(false);
const activeMessageTab = ref('comment');
const unreadCount = ref(0);
const commentMessages = ref([]);
const commentPage = ref(1);
const commentSize = ref(10);
const commentHasMore = ref(true);
const commentLoading = ref(false);
const followMessages = ref([]);
const followPage = ref(1);
const followHasMore = ref(true);
const followLoading = ref(false);
const likeMessages = ref([]);
const likePage = ref(1);
const likeHasMore = ref(true);
const likeLoading = ref(false);
const applyMessages = ref([]);
const applyPage = ref(1);
const applySize = ref(10);
const applyHasMore = ref(true);
const applyLoading = ref(false);

// 评论对话框相关
const commentDialogVisible = ref(false);
const allComments = ref([]);
const currentCommentId = ref(null);
const commentListRef = ref(null);
const commentItems = ref([]);
const expandedDialogComments = ref({});
const activeReplyCommentId = ref(null);
const replyContent = ref('');
const replyToUsername = ref('');
const currentProjectId = ref(null);

const mdParser = markdownIt().use(emoji);
const emojis = ['😀', '😂', '😍', '👍', '👏', '🙏', '❤️', '🔥', '🎉', '🤔'];

// 私聊相关
const chatListVisible = ref(false);
const chatList = ref([]);
const chatPage = ref(1);
const chatSize = ref(10);
const chatLoading = ref(false);
const chatHasMore = ref(true);
const totalUnreadCount = ref(0);
const chatVisible = ref(false);
const currentChatId = ref(null);
const chatType = ref(null);
const currentUser = ref({});
const targetUser = ref({});
const showTooltip = ref(false);




const formatChatTime = (timeString) => {
  if (!timeString) return '';
  const date = new Date(timeString);
  return `${date.getFullYear()}-${padZero(date.getMonth() + 1)}-${padZero(date.getDate())} ${padZero(date.getHours())}:${padZero(date.getMinutes())}`;
};

const loadChatList = async () => {
  if(!state.data.id) {
     return;
  } 
  if (chatLoading.value) return;
  chatLoading.value = true;
  
  try {
    const res = await post('/api/auth/chat/getChats', {
      page: chatPage.value,
      size: chatSize.value
    });
    
    if (res?.records) {
      chatList.value = chatPage.value === 1 
        ? res.records 
        : [...chatList.value, ...res.records];
      
      chatHasMore.value = chatList.value.length < res.total;
      totalUnreadCount.value = chatList.value.reduce((sum, chat) => sum + (chat.unreadCount || 0), 0);
    }
  } catch (e) {
    // console.error('加载私聊列表失败:', e);
  } finally {
    chatLoading.value = false;
  }
};

const openChatDialog = async (chat) => {
  currentChatId.value = chat.chatId;
  chatType.value = chat.chatType;
  currentUser.value = state.data;
  targetUser.value = {};
  chatVisible.value = true;
};

const fetchUnreadCount = async () => {
  try {
    if(state.data.id) {
      const res = await get('/api/auth/msg/unreadMsgCount');
      unreadCount.value = res || 0;
    }
  } catch (e) {
    // console.error('获取未读消息数失败:', e);
  }
};

const goToLogin = () => {
  router.push({ name: 'welcome-login' });
};

const loadApplyMessages = async () => {
  if (applyLoading.value) return;
  applyLoading.value = true;
  try {
    const res = await post('/api/auth/msg/msgOfApply', {
      page: applyPage.value,
      size: applySize.value
    });
    applyMessages.value = [...applyMessages.value, ...(res.records || [])];
    applyHasMore.value = applyMessages.value.length < (res.total || 0);
  } catch (e) {
    // console.error('加载神奇通知失败:', e);
  } finally {
    applyLoading.value = false;
  }
};

const followBack = async (senderId) => {
  try {
    const item = followMessages.value.find(m => m.senderId === senderId);
    if (item) {
      item.followLoading = true;
      
      const res = await post('/api/auth/project/concernPublisher', {
        followeeId: senderId
      });
      
      if (res) {
        item.needFollow = false;
        ElMessage.success('回关成功');
      }
    }
  } catch (e) {
    // console.error('回关失败:', e);
    ElMessage.error('回关失败');
  } finally {
    const item = followMessages.value.find(m => m.senderId === senderId);
    if (item) {
      item.followLoading = false;
    }
  }
};

const showApplyDetail = async (item) => {
  try {
    await markMessagesAsRead(item.id);
    
    if (item.type === 103) {
      router.push('/index/my/applyList');
    } else if (item.type === 104) {
      router.push('/index/my/applicationList');
    } else if (item.type === 105) {
      router.push('/index/my/myMemberGroupDetail/' + item.projectId);
    }
  } catch (e) {
    // console.error('处理申请通知失败:', e);
    ElMessage.error('加载详情失败');
  }
};

const goToUserProfile = (userId) => {
  window.open(`/index/user/${userId}`, '_blank');
}

const markAllAsRead = async (tab) => {
  try {
    const res = await get(`/api/auth/msg/allMarkRead?type=${tab}`);
    if (res) {
      switch(tab) {
        case 'comment':
          commentMessages.value.forEach(msg => msg.isRead = 1);
          break;
        case 'follow':
          followMessages.value.forEach(msg => msg.isRead = 1);
          break;
        case 'like':
          likeMessages.value.forEach(msg => msg.isRead = 1);
          break;
        case 'apply':
          applyMessages.value.forEach(msg => msg.isRead = 1);
          break;
      }
      
      fetchUnreadCount();
      ElMessage.success('消息已标记为已读');
    }
  } catch (e) {
    // console.error('一键已读失败:', e);
    ElMessage.error('标记消息为已读失败');
  }
};

const showCommentDetail = async (item) => {
  try {
    await markMessagesAsRead(item.id);
    currentProjectId.value = item.projectId;

    const res = await get(`/api/unauth/project/commentShow?projectId=${item.projectId}`);
    allComments.value = res || [];
    currentCommentId.value = item.commentId;
    commentDialogVisible.value = true;
    
    allComments.value.forEach(comment => {
      if (comment.replies?.length) {
        expandedDialogComments.value[comment.id] = true;
      }
    });
    
    await nextTick();
    scrollToCurrentComment();
  } catch (e) {
    // console.error('获取评论详情失败:', e);
    ElMessage.error('加载评论详情失败');
  }
};

const scrollToCurrentComment = () => {
  if (commentItems.value && currentCommentId.value) {
    const index = allComments.value.findIndex(c => c.id === currentCommentId.value);
    if (index !== -1 && commentItems.value[index]) {
      const container = commentListRef.value;
      const element = commentItems.value[index];
      
      const containerRect = container.getBoundingClientRect();
      const elementRect = element.getBoundingClientRect();
      const scrollPosition = elementRect.top - containerRect.top + container.scrollTop - (containerRect.height / 2) + (elementRect.height / 2);
      
      container.scrollTo({
        top: scrollPosition,
        behavior: 'smooth'
      });
    }
  }
};

const handleDialogClosed = () => {
  allComments.value = [];
  currentCommentId.value = null;
};

const formatTime = (timeString) => {
  if (!timeString) return '';
  const date = new Date(timeString);
  return `${date.getFullYear()}-${padZero(date.getMonth() + 1)}-${padZero(date.getDate())} ${padZero(date.getHours())}:${padZero(date.getMinutes())}`;
};

const padZero = (num) => {
  return num < 10 ? `0${num}` : num;
};

const loadCommentMessages = async () => {
  if (commentLoading.value) return;
  commentLoading.value = true;
  try {
    const res = await post('/api/auth/msg/msgOfCommentList', {
      page: commentPage.value,
      size: commentSize.value
    });
    commentMessages.value = [...commentMessages.value, ...(res.records || [])];
    commentHasMore.value = commentMessages.value.length < (res.total || 0);
  } catch (e) {
    // console.error('加载评论消息失败:', e);
  } finally {
    commentLoading.value = false;
  }
};

const loadMoreComments = () => {
  commentPage.value += 1;
  loadCommentMessages();
};

const replyToDialog = (commentId, username) => {
  activeReplyCommentId.value = commentId;
  replyToUsername.value = username;
  replyContent.value = '';
  
  const firstLevelId = findFirstLevelCommentId(commentId);
  if (firstLevelId && firstLevelId !== commentId && !expandedDialogComments.value[firstLevelId]) {
    expandedDialogComments.value[firstLevelId] = true;
  }
  
  nextTick(() => {
    const container = commentListRef.value;
    container.scrollTo({
      top: container.scrollHeight,
      behavior: 'smooth'
    });
  });
};

const cancelReply = () => {
  activeReplyCommentId.value = null;
  replyToUsername.value = '';
  replyContent.value = '';
};

const insertEmojiToReply = (emoji) => {
  replyContent.value += emoji;
};

const submitDialogReply = async () => {
  if (!replyContent.value.trim()) {
    ElMessage.warning('回复内容不能为空');
    return;
  }
  try {
    const res = await post('/api/auth/project/comment', {
      projectId: currentProjectId.value,
      content: replyContent.value,
      replyTo: activeReplyCommentId.value,
      firstLevelCommonId: findFirstLevelCommentId(activeReplyCommentId.value)
    });
    if (res) {
      replyContent.value = '';
      cancelReply();
      await showCommentDetail({ projectId: currentProjectId.value, commentId: currentCommentId.value });
      ElMessage.success('回复成功');
    }
  } catch (err) {
    ElMessage.error('回复失败',err);
  }
};

const likeDialogComment = async (commentId) => {
  try {
    const res = await get(`/api/auth/project/commentLike?projectId=${currentProjectId.value}&commentId=${commentId}`);
    if (res) {
      await showCommentDetail({ projectId: currentProjectId.value, commentId: currentCommentId.value });
      ElMessage.success('点赞成功');
    }
  } catch (err) {
    ElMessage.error('点赞失败');
  }
};

const deleteDialogComment = async (commentId) => {
  try {
    const confirm = await ElMessageBox.confirm('确定要删除这条评论吗？', '删除评论', { type: 'warning' });
    if (confirm) {
      const res = await get(`/api/auth/project/commentDeleted?projectId=${currentProjectId.value}&commentId=${commentId}`);
      if (res) {
        await showCommentDetail({ projectId: currentProjectId.value, commentId: currentCommentId.value });
        ElMessage.success('删除成功');
      }
    }
  } catch (err) {
    if (err !== 'cancel') {
      ElMessage.error('删除失败');
    }
  }
};

const toggleExpandDialog = (commentId) => {
  expandedDialogComments.value[commentId] = !expandedDialogComments.value[commentId];
};

const renderMarkdown = (text) => {
  return mdParser.render(text || '');
};

const findFirstLevelCommentId = (commentId) => {
  const topLevelComment = allComments.value.find(c => c.id === commentId);
  if (topLevelComment) {
    return topLevelComment.id;
  }
  
  for (const comment of allComments.value) {
    if (comment.replies) {
      const foundReply = findCommentInReplies(comment.replies, commentId);
      if (foundReply) {
        if (foundReply.replyTo === comment.id) {
          return comment.id;
        }
        return findFirstLevelCommentId(foundReply.replyTo);
      }
    }
  }
  
  return -1;
};

const findCommentInReplies = (replies, commentId) => {
  for (const reply of replies) {
    if (reply.id === commentId) {
      return reply;
    }
    if (reply.replies) {
      const found = findCommentInReplies(reply.replies, commentId);
      if (found) {
        return found;
      }
    }
  }
  return null;
};

const loadFollowMessages = async () => {
  if (followLoading.value) return;
  followLoading.value = true;
  try {
    const res = await post('/api/auth/msg/msgOfFollowed', {
      page: followPage.value,
      size: commentSize.value
    });
    followMessages.value = [...followMessages.value, ...(res.records || [])];
    followHasMore.value = followMessages.value.length < (res.total || 0);
  } catch (e) {
    // console.error('加载关注消息失败:', e);
  } finally {
    followLoading.value = false;
  }
};

const loadMoreFollows = () => {
  followPage.value += 1;
  loadFollowMessages();
};

const loadLikeMessages = async () => {
  if (likeLoading.value) return;
  likeLoading.value = true;
  try {
    const res = await post('/api/auth/msg/msgOfLiked', {
      page: likePage.value,
      size: commentSize.value
    });
    likeMessages.value = [...likeMessages.value, ...(res.records || [])];
    likeHasMore.value = likeMessages.value.length < (res.total || 0);
  } catch (e) {
    // console.error('加载点赞消息失败:', e);
  } finally {
    likeLoading.value = false;
  }
};

const loadMoreLikes = () => {
  likePage.value += 1;
  loadLikeMessages();
};

const loadMoreApplies = () => {
  applyPage.value += 1;
  loadApplyMessages();
};

const handleMessageClick = () => {
  if (!messageVisible.value) {
    switch (activeMessageTab.value) {
      case 'comment':
        commentMessages.value = [];
        commentPage.value = 1;
        loadCommentMessages();
        break;
      case 'follow':
        followMessages.value = [];
        followPage.value = 1;
        loadFollowMessages();
        break;
      case 'like':
        likeMessages.value = [];
        likePage.value = 1;
        loadLikeMessages();
        break;
      case 'apply':
        applyMessages.value = [];
        applyPage.value = 1;
        loadApplyMessages();
        break;
    }
  }
};

const markMessagesAsRead = async (messageId) => {
  try {
    await post('/api/auth/msg/markMsgAsRead',{
      id:messageId
    });
    updateMessageReadStatus(messageId);
    fetchUnreadCount();
  } catch (e) {
    // console.error('标记消息为已读失败:', e);
  }
};

const updateMessageReadStatus = (messageId) => {
  const commentIndex = commentMessages.value.findIndex(m => m.id === messageId);
  if (commentIndex !== -1) {
    commentMessages.value[commentIndex].isRead = 1;
  }
  
  const followIndex = followMessages.value.findIndex(m => m.id === messageId);
  if (followIndex !== -1) {
    followMessages.value[followIndex].isRead = 1;
  }
  
  const likeIndex = likeMessages.value.findIndex(m => m.id === messageId);
  if (likeIndex !== -1) {
    likeMessages.value[likeIndex].isRead = 1;
  }

  const applyIndex = applyMessages.value.findIndex(m => m.id === messageId);
  if (applyIndex !== -1) {
    applyMessages.value[applyIndex].isRead = 1;
  }
};

watch(activeMessageTab, (newVal) => {
  if (messageVisible.value) {
    switch (newVal) {
      case 'comment':
        if (commentMessages.value.length === 0) {
          loadCommentMessages();
        }
        break;
      case 'follow':
        if (followMessages.value.length === 0) {
          loadFollowMessages();
        }
        break;
      case 'like':
        if (likeMessages.value.length === 0) {
          loadLikeMessages();
        }
        break;
      case 'apply':
        if (applyMessages.value.length === 0) {
          loadApplyMessages();
        }
        break;
    }
  }
});

function changeDisplayMode(mode) {
  displayMode.value = mode;
}

function goToDetail(project) {
  router.push({ name: 'project-detail', params: { id: project.id } });
}

const fetchOptions = async () => {
  try {
    const [catRes, diffRes] = await Promise.all([
      get('/api/unauth/common/category'),
      get('/api/unauth/common/difficulty')
    ]);
    categories.value = catRes || [];
    difficulties.value = diffRes || [];
  } catch (e) {
    ElMessage.error('加载搜索选项失败');
  }
};


const fetchProjectListData = async (params = {}, page = 1) => {
  if (loading.value) return;
  
  loading.value = true;
  try {
    const res = await post('/api/unauth/project/show', { 
      page: page,
      size: pageSize.value,
      categories: params?.categories,
      difficulty: params?.difficulties,
      projectName: params?.projectName
    });

    if (!res?.records) {
      console.warn("接口返回异常结构：", res);
      return;
    }

    // 处理数据
    projectList.value = res.records.map(item => ({
      id: item.id,
      name: item.name,
      firstCategoryName: item.firstCategoryName,
      secondCategoryName: item.secondCategoryName,
      description: item.description,
      difficulty: '★'.repeat(Number(item.difficulty || 1)),
      imageUrl: (item.imageUrl?.replace(/["]/g, '') || '/images/default-project.png')
    }));

    total.value = res.total || 0;
    totalPages.value = Math.ceil(total.value / pageSize.value);
    currentPage.value = page;
    
  } catch (error) {
    console.error("加载失败：", error);
    ElMessage.error('数据加载失败');
  } finally {
    loading.value = false;
  }
}

// 上一页
const prevPage = () => {
  if (currentPage.value > 1) {
    fetchProjectListData({
      categories: search.value.categories,
      difficulties: search.value.difficulties,
      projectName: search.value.name
    }, currentPage.value - 1);
  }
};

// 下一页
const nextPage = () => {
  if (currentPage.value < totalPages.value) {
    fetchProjectListData({
      categories: search.value.categories,
      difficulties: search.value.difficulties,
      projectName: search.value.name
    }, currentPage.value + 1);
  }
};

// 跳转到指定页
const goToPage = (page) => {
  if (page >= 1 && page <= totalPages.value && page !== currentPage.value) {
    fetchProjectListData({
      categories: search.value.categories,
      difficulties: search.value.difficulties,
      projectName: search.value.name
    }, page);
  }
};


const loadProjects = async () => {
 fetchOptions();
  fetchProjectListData({
    categories: search.value.categories,
    difficulties: search.value.difficulties,
    projectName: search.value.name
  }, 1);
};

const onSearch = () => {
  fetchProjectListData({
    categories: search.value.categories,
    difficulties: search.value.difficulties,
    projectName: search.value.name
  }, 1);
};
const handleScroll = (e) => {
  // const { scrollTop, scrollHeight, clientHeight } = e.target;
  // if (scrollHeight - scrollTop - clientHeight < 100 && !loading.value && hasMore.value) {
  //   fetchProjectListData({
  //     category: search.value.category,
  //     difficulty: search.value.difficulties,
  //     projectName: search.value.name
  //   });
  // }
};

onMounted(() => {
  loadProjects();
  fetchUnreadCount();

  if (!state.data.id) {
    loadUserInfo().then(() => {
      loadProjects();
      fetchUnreadCount();
      loadChatList()
    });
  } else {
    fetchUnreadCount();
    loadProjects();
    loadChatList();
  }
});

function userLogout() {
  logout(() => router.push("/"));
}
</script>

<style scoped>
.index-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
}

.nav-menu {
  position: sticky;
  top: 0;
  z-index: 1000;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.main-content {
  flex: 1;
  overflow: hidden;
  padding: 20px;
}

.project-container {
  height: 100%;
  overflow-y: auto;
  padding: 0 10px;
}

.project-list {
  margin-bottom: 20px;
}

.project-card {
  margin-bottom: 20px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.project-image {
  width: 100%;
  height: 180px;
  object-fit: cover;
  border-radius: 4px 4px 0 0;
}

.project-content {
  padding: 15px;
  flex: 1;
}

.project-title {
  margin: 0 0 10px;
  font-size: 16px;
  color: #333;
}

.project-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.project-difficulty {
  color: #ff9900;
  font-weight: bold;
}

.project-description {
  color: #666;
  font-size: 14px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.loading-more, .no-more {
  text-align: center;
  padding: 20px;
  color: #999;
}

.loading-more .el-icon {
  margin-right: 5px;
  animation: rotating 2s linear infinite;
}

@keyframes rotating {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.search-bar {
  margin-top: 20px;
  display: flex;
  align-items: center;
  padding: 10px 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.avatar-dropdown {
  position: absolute;
  right: 20px;
  top: 10px;
  z-index: 1001;
}

.avatar-wrapper {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.message-bell {
  position: absolute;
  right: 100px;
  top: 15px;
  z-index: 1001;
  cursor: pointer;
  padding: 8px;
  border-radius: 50%;
  transition: background-color 0.3s;
  color: #606266;
}

.message-bell:hover {
  background-color: #f0f0f0;
  color: #409EFF;
}

.badge {
  display: flex;
  align-items: center;
  justify-content: center;
}

.message-tabs {
  padding: 0 10px;
  background-color: #f8fafc;
}

.message-list {
  max-height: 400px;
  overflow-y: auto;
}

.message-item {
  display: flex;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
  position: relative;
  transition: all 0.3s;
}

.unread-message {
  background-color: #f8fafc;
}

.message-unread-dot {
  position: absolute;
  left: 8px;
  top: 50%;
  transform: translateY(-50%);
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #f56c6c;
}

.message-avatar {
  margin-right: 12px;
}

.message-content {
  flex: 1;
  margin-left: 15px;
}

.message-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 4px;
}

.message-user {
  font-weight: bold;
  color: #333;
}

.message-time {
  font-size: 12px;
  color: #999;
}

.message-text {
  font-size: 14px;
  color: #666;
  line-height: 1.5;
}

.load-more, .no-message {
  text-align: center;
  padding: 10px;
  color: #999;
  cursor: pointer;
}

.load-more:hover {
  color: #409EFF;
}

.no-message {
  cursor: default;
}

.message-text {
  font-size: 14px;
  line-height: 1.5;
}

.sender-name {
  color: #409EFF;
  font-weight: bold;
  margin-right: 5px;
}

.message-content-text {
  color: #666;
  margin-right: 5px;
}

.related-words {
  color: #67C23A;
  font-style: italic;
}

.comment-detail-dialog {
  border-radius: 12px;
}

.comment-dialog-container {
  max-height: 60vh;
  display: flex;
  flex-direction: column;
}

.comment-list {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
  margin-bottom: 20px;
}

.comment-item {
  padding: 15px;
  margin-bottom: 15px;
  border-radius: 8px;
  background-color: #f9f9f9;
  transition: all 0.3s;
}

.highlight-comment {
  background-color: #e6f7ff;
  border-left: 3px solid #1890ff;
  animation: highlight 2s ease-in-out;
}

@keyframes highlight {
  0% { background-color: #e6f7ff; }
  50% { background-color: #bae7ff; }
  100% { background-color: #e6f7ff; }
}

.comment-header {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  gap: 10px;
}

.user-avatar {
  background-color: #409EFF;
  color: white;
  font-weight: bold;
}

.username {
  font-weight: bold;
  color: #333;
  cursor: pointer;
}

.username:hover {
  color: #409EFF;
}

.comment-time {
  font-size: 12px;
  color: #999;
}

.md-content {
  color: #333;
  line-height: 1.6;
  word-break: break-word;
  font-size: 14px;
}

.md-content :deep(*) {
  margin: 0;
}

.md-content :deep(p) {
  margin-bottom: 8px;
}

.md-content :deep(a) {
  color: #409EFF;
  text-decoration: none;
}

.md-content :deep(a:hover) {
  text-decoration: underline;
}

.comment-actions {
  margin-top: 10px;
  display: flex;
  gap: 15px;
}

.replies {
  margin-top: 15px;
  padding-left: 30px;
  position: relative;
}

.replies::before {
  content: "";
  position: absolute;
  left: 15px;
  top: 0;
  bottom: 0;
  width: 1px;
  background: #eaeaea;
}

.reply-item {
  position: relative;
  padding: 10px;
  margin-bottom: 10px;
  background: #f5f5f5;
  border-radius: 6px;
}

.reply-item::before {
  content: "";
  position: absolute;
  left: -15px;
  top: 20px;
  width: 15px;
  height: 1px;
  background: #ddd;
}

.deleted {
  color: #bbb;
  font-style: italic;
}

.zhihu-comment-editor {
  border: 1px solid #f0f2f7;
  border-radius: 4px;
  padding: 12px;
  background: #fff;
}

.editor-header {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.editor-header .avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background-color: #409EFF;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 8px;
  font-weight: bold;
}

.markdown-textarea {
  width: 100%;
}

.editor-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
  gap: 8px;
}

.submit-button {
  background-color: #0084ff;
  color: white;
  border: none;
  padding: 6px 16px;
  border-radius: 4px;
  cursor: pointer;
}

.cancel-button {
  background: none;
  border: none;
  color: #8590a6;
  padding: 6px 12px;
  cursor: pointer;
}

.emoji-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.emoji {
  cursor: pointer;
  font-size: 20px;
  padding: 2px;
}

.mark-all-read-container {
  display: flex;
  justify-content: flex-end;
  padding: 8px 12px;
  border-bottom: 1px solid #f0f0f0;
}

.mark-all-read-container .el-button {
  color: #666;
}

.mark-all-read-container .el-button:hover {
  color: #409EFF;
}

.mark-all-read-container .el-button:disabled {
  color: #ccc;
  cursor: not-allowed;
}

.mark-all-read-container .el-icon {
  margin-right: 4px;
}

.follow-back-container {
  margin-left: auto;
  padding-right: 10px;
  display: flex;
  align-items: center;
}

.message-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
  position: relative;
  transition: all 0.3s;
}

.login-button {
  position: absolute;
  right: 20px;
  top: 10px;
  z-index: 1001;
}

.chat-icon {
  position: absolute;
  right: 140px;
  top: 15px;
  padding: 8px;
  cursor: pointer;
  border-radius: 50%;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background-color: #fff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  z-index: 1001;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chat-icon:hover {
  background-color: #f5f7fa;
  color: #409EFF;
  transform: scale(1.1);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

.chat-list-container {
  /* 修改为 */
  padding: 0;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.1);
  /* 新增 */
  position: relative;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
}

.chat-list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f2f7;
}

.chat-list-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #1f2d3d;
}


.chat-item {
  display: flex;
  padding: 12px;
  cursor: pointer;
  border-radius: 8px;
}

.chat-item:hover {
  background-color: #f5f7fa;
}

.unread-chat {
  background-color: #f8fafc;
}

.chat-avatar {
  position: relative;
  margin-right: 12px;
}

.chat-badge {
  position: absolute;
  top: -5px;
  right: -5px;
}

.chat-content {
  flex: 1;
  min-width: 0;
}

.chat-title {
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.chat-time {
  font-size: 12px;
  color: #999;
}

.chat-preview {
  font-size: 13px;
  color: #666;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}


.mark-read-btn {
  color: #909399;
  padding: 0;
  font-size: 12px;
}

.mark-read-btn:hover {
  color: #409EFF;
}

.chat-scrollbar {
  padding: 0 8px;
}

.chat-item {
  display: flex;
  padding: 12px;
  cursor: pointer;
  border-radius: 8px;
  margin: 4px 0;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.chat-item::after {
  content: '';
  position: absolute;
  left: 0;
  bottom: 0;
  width: 100%;
  height: 1px;
  background: linear-gradient(90deg, transparent, #f0f2f7, transparent);
}

.chat-item:hover {
  background-color: #f8fafc;
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.unread-chat {
  background-color: rgba(64, 158, 255, 0.05);
}

.unread-chat:hover {
  background-color: rgba(64, 158, 255, 0.08);
}

.chat-avatar {
  position: relative;
  margin-right: 12px;
  flex-shrink: 0;
}

.chat-badge {
  position: absolute;
  top: -5px;
  right: -5px;
  transform: scale(0.9);
  transition: all 0.3s;
}

.chat-item:hover .chat-badge {
  transform: scale(1);
}

.chat-content {
  flex: 1;
  min-width: 0;
  overflow: hidden;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.chat-title {
  font-weight: 600;
  font-size: 14px;
  color: #1f2d3d;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
}

.chat-time {
  font-size: 12px;
  color: #909399;
  margin-left: 8px;
  flex-shrink: 0;
}

.chat-preview {
  font-size: 13px;
  color: #909399;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.loading-more, .no-more {
  text-align: center;
  padding: 16px;
  color: #909399;
  font-size: 13px;
}

.no-message {
  padding: 40px 0;
}

/* 列表动画 */
.chat-list-enter-active,
.chat-list-leave-active {
  transition: all 0.5s ease;
}

.chat-list-enter-from,
.chat-list-leave-to {
  opacity: 0;
  transform: translateX(30px);
}

.chat-list-move {
  transition: transform 0.5s ease;
}

.chat-popover {
  padding: 0 !important;
  border: none !important;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.1) !important;
  border-radius: 8px !important;
  overflow: hidden;
  /* 新增以下样式 */
  position: fixed !important;
  inset: 0 !important;
  width: 350px !important;
  height: auto !important;
  max-height: 80vh !important;
  transform: none !important;
  margin: 0 !important;
}

.chat-popover .el-popper__arrow {
  display: none !important;
}

.chat-popover .el-scrollbar__wrap {
  overscroll-behavior: contain !important;
}

.pagination-container {
  position: fixed;
  right: 30px;
  bottom: 30px;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 10px;
  background: rgba(255, 255, 255, 0.95);
  padding: 15px 20px;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  backdrop-filter: blur(10px);
  border: 1px solid #ebeef5;
  z-index: 1000;
  min-width: 300px;
}

.pagination-info {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.pagination-buttons {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.pagination-btn {
  padding: 8px 16px;
  border-radius: 6px;
  font-weight: 500;
}

.pagination-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-btn {
  min-width: 40px;
  height: 32px;
  padding: 0;
  border-radius: 6px;
}

.ellipsis {
  padding: 0 8px;
  color: #c0c4cc;
  font-weight: bold;
}

.no-data {
  padding: 60px 0;
  text-align: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .pagination-container {
    right: 15px;
    bottom: 15px;
    left: 15px;
    align-items: center;
    min-width: auto;
  }
  
  .pagination-buttons {
    justify-content: center;
  }
  
  .pagination-info {
    text-align: center;
  }
}

/* 按钮悬停效果 */
.pagination-btn:not(:disabled):hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
}

.page-btn:not(.is-disabled):hover {
  border-color: #409EFF;
  color: #409EFF;
}
</style>