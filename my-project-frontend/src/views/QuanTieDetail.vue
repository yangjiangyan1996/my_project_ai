<template>
  <div class="tie-detail-container">
    <!-- 返回按钮 -->
    <el-button @click="goBack" class="back-button">返回</el-button>
    
    <el-card class="tie-detail-card">
      <!-- 帖子头部 -->
      <div class="tie-header">
        <div class="tie-meta">
          <el-avatar :size="48" :src="tieDetail.avatar" class="user-avatar"></el-avatar>
          <div class="user-info">
            <span class="username">{{ tieDetail.username || '匿名用户' }}</span>
            <span class="post-time">{{ formatTime(tieDetail.createdTime) }}</span>
          </div>
        </div>
        
        <h1 class="tie-title">{{ tieDetail.title }}</h1>
        
        <el-card class="section" v-html="tieDetail.content" />
        
        <div class="tie-footer">
          <div class="action-btn">
            <el-icon><View /></el-icon>
            <span>{{ tieDetail.views || 0 }}</span>
          </div>
          <div class="action-btn">
            <el-icon><ChatDotRound /></el-icon>
            <span>{{ tieDetail.comments || 0 }}</span>
          </div>
          <div class="action-btn" @click="handleLike">
            <el-icon :color="liked ? '#f56c6c' : ''"><Star /></el-icon>
            <span>{{ tieDetail.likes || 0 }}</span>
          </div>
          <div class="action-btn">
            <el-icon><Share /></el-icon>
          </div>
        </div>
      </div>
      
      <el-divider>评论区</el-divider>
      
      <!-- 评论组件 - 与ProjectDetail.vue保持一致 -->
      <div class="zhihu-comment-editor">
        <div class="editor-header">
          <div class="avatar">{{ userInitial }}</div>
          <span>{{ replyToCommentId ? `回复 ${replyToUsername}` : '写下你的评论...' }}</span>
        </div>
        <el-input
          v-model="newComment"
          type="textarea"
          placeholder="写下你的评论（支持 Markdown）"
          :autosize="{ minRows: 5, maxRows: 10 }"
          class="markdown-textarea"
        />
        <div class="editor-footer">
          <el-popover placement="top" width="250" trigger="click">
            <template #reference>
              <el-button size="small" text type="primary">😊 表情</el-button>
            </template>
            <div class="emoji-list">
              <span v-for="emoji in emojis" :key="emoji" class="emoji" @click="insertEmojiToNewComment(emoji)">
                {{ emoji }}
              </span>
            </div>
          </el-popover>
          
          <button class="cancel-button" v-if="replyToCommentId" @click="cancelReply">取消</button>
          <button class="submit-button" @click="submitComment">发布</button>
        </div>
      </div>

      <div class="comment-list">
        <div v-for="comment in comments" :key="comment.id" class="comment-item">
          <div class="comment-header">
            <div class="comment-user">
              <el-avatar :src="comment.avatar" size="small" class="user-avatar">{{ comment.username.charAt(0) }}</el-avatar>
              <strong class="username" @click.stop="goToUserProfile(comment.secrecyId)">{{ comment.username }}</strong>
              <span class="comment-time">{{ formatTime(comment.createdAt) }}</span>
            </div>
            <span v-if="comment.deleted" class="deleted">该评论已被删除</span>
            <div v-else v-html="renderMarkdown(comment.content)" class="md-content" />
          </div>
          <div class="comment-actions">
            <el-button text @click="replyTo(comment.id, comment.username)">回复</el-button>
            <el-button text @click="likeComment(comment.id)">👍 {{ comment.likes }}</el-button>
            <el-button text v-if="comment.isMine" @click="deleteComment(comment.id)">删除</el-button>
            <el-button text v-if="comment.replies?.length" @click="toggleExpand(comment.id)">
              {{ expandedComments[comment.id] ? '收起回复' : `展开回复 (${comment.replies.length})` }}
            </el-button>
          </div>

          <!-- 一级评论的回复框 -->
          <div v-if="activeReplyBox === comment.id" class="zhihu-reply-editor">
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
                  <span v-for="emoji in emojis" :key="emoji" class="emoji" @click="insertEmojiToNewComment(emoji)">
                    {{ emoji }}
                  </span>
                </div>
              </el-popover>
              
              <button class="cancel-button" @click="cancelReply">取消</button>
              <button class="submit-button" @click="submitReply(comment.id)">发布</button>
            </div>
          </div>

          <div v-if="expandedComments[comment.id]" class="replies">
            <div v-for="reply in comment.replies" :key="reply.id" class="reply-item">
              <div class="comment-user">
                <el-avatar :src="reply.avatar" size="small" class="user-avatar">{{ reply.username.charAt(0) }}</el-avatar>
                <strong class="username" @click.stop="goToUserProfile(reply.secrecyId)">{{ reply.username }}</strong>
                <span class="comment-time">{{ formatTime(reply.createdAt) }}</span>
              </div>
              <span v-if="reply.deleted" class="deleted">该评论已被删除</span>
              <span v-else>
                <template v-if="reply.replyToName">@{{ reply.replyToName }}：</template>
                <span v-html="renderMarkdown(reply.content)" class="md-content" />
              </span>
              <div class="comment-actions">
                <el-button text @click="replyTo(reply.id, reply.username)">回复</el-button>
                <el-button text @click="likeComment(reply.id)">👍 {{ reply.likes }}</el-button>
                <el-button text v-if="reply.isMine" @click="deleteComment(reply.id)">删除</el-button>
              </div>

              <!-- 二级评论的回复框 -->
              <div v-if="activeReplyBox === reply.id" class="zhihu-reply-editor">
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
                      <span v-for="emoji in emojis" :key="emoji" class="emoji" @click="insertEmojiToNewComment(emoji)">
                        {{ emoji }}
                      </span>
                    </div>
                  </el-popover>
                  
                  <button class="cancel-button" @click="cancelReply">取消</button>
                  <button class="submit-button" @click="submitReply(reply.id)">发布</button>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 评论分页 -->
        <el-pagination
          class="comment-pagination"
          v-model:current-page="commentPage"
          :page-size="commentPageSize"
          :pager-count="5"
          layout="prev, pager, next"
          :total="commentTotal"
          background
          hide-on-single-page
          @current-change="fetchComments"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, inject } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { get, post } from '@/net';
import { ElMessage } from 'element-plus';
import { View, ChatDotRound, Star, Share } from '@element-plus/icons-vue';
import markdownIt from 'markdown-it';
import emoji from 'markdown-it-emoji';
import useUserInfo from '@/hooks/useUserInfo';

const route = useRoute();
const router = useRouter();
const { state: userInfo, loadUserInfo } = useUserInfo();

// 帖子详情数据
const tieDetail = ref({
  id: 0,
  title: '',
  content: '',
  avatar: '',
  bar_id: 0,
  username: '',
  createdTime: '',
  views: 0,
  comments: 0,
  likes: 0
});

// 评论相关数据
const comments = ref([]);
const newComment = ref("");
const replyContent = ref("");
const replyToCommentId = ref(null);
const replyToUsername = ref("");
const activeReplyBox = ref(null);
const expandedComments = ref({});

// 评论分页
const commentPage = ref(1);
const commentPageSize = ref(10);
const commentTotal = ref(0);

// 点赞状态
const liked = ref(false);

// Markdown解析器
const mdParser = markdownIt().use(emoji);
const tieId = route.params.id;
const emojis = inject('emojis');

const userInitial = computed(() => {
  return userInfo.data?.username?.charAt(0) || '匿';
});

onMounted(async () => {
  await userInfo.loadUserInfo?.();
  fetchTieDetail();
  fetchComments();
});

// 获取帖子详情
const fetchTieDetail = async () => {
  try {
    const res = await get(`/api/unauth/quan/getTieBaseInfo?tieId=${tieId}`);
    if (res) {
      tieDetail.value = {
        ...res,
        username: res.createdName,
        createdTime: res.createdTime
      };
      liked.value = res.myLike || false;
    }
  } catch (error) {
    ElMessage.error('加载帖子详情失败');
  }
};

// 获取评论列表
const fetchComments = async (page = 1) => {
  try {
    const res = await post(`/api/auth/quan/commentShow`, {
        tieId: tieId,
        page: page,
        size: commentPageSize.value
    });
    
    if (res) {
      comments.value = res.records || [];
      commentTotal.value = res.total || 0;
      commentPage.value = page;
      
      // 初始化展开状态
      comments.value.forEach(comment => {
        if (comment.replies?.length) {
          expandedComments.value[comment.id] = true;
        }
      });
    }
  } catch (error) {
    ElMessage.error('加载评论失败');
  }
};

// 点赞帖子
const handleLike = async () => {
  try {
    const targetState = !liked.value;
    const result = await get(`/api/auth/quan/likeTie?tieId=${tieId}&liked=${targetState}`);
    
    if (result) {
      liked.value = targetState;
      tieDetail.value.likes += targetState ? 1 : -1;
      ElMessage.success(targetState ? '点赞成功' : '已取消点赞');
    }
  } catch (error) {
    ElMessage.error('操作失败');
  }
};

// 以下方法与ProjectDetail.vue中的评论功能保持一致
function insertEmojiToNewComment(emoji) {
  if (replyToCommentId.value) {
    replyContent.value += emoji;
  } else {
    newComment.value += emoji;
  }
}

function formatTime(timeString) {
  if (!timeString) return '';
  const date = new Date(timeString);
  return date.toLocaleString();
}

async function likeComment(commentId) {
  try {
    const res = await get(`/api/auth/quan/likeComment?tieId=${tieId}&commentId=${commentId}`);
    if (res) {
      ElMessage.success('点赞成功');
      fetchComments(commentPage.value);
    }
  } catch (err) {
    ElMessage.error('点赞失败');
  }
}

async function deleteComment(commentId) {
  try {
    const res = await get(`/api/auth/quan/deleteComment?tieId=${tieId}&commentId=${commentId}`);
    if (res) {
      ElMessage.success('删除成功');
      fetchComments(commentPage.value);
    }
  } catch (err) {
    ElMessage.error('删除失败');
  }
}

function toggleExpand(commentId) {
  expandedComments.value[commentId] = !expandedComments.value[commentId];
}

function renderMarkdown(text) {
  return mdParser.render(text || '');
}

function replyTo(commentId, username) {
  if (activeReplyBox.value === commentId) {
    cancelReply();
    return;
  }
  
  replyToCommentId.value = commentId;
  replyToUsername.value = username;
  activeReplyBox.value = commentId;
  replyContent.value = '';
  
  // 如果是回复二级评论，确保父级评论是展开的
  const firstLevelId = findFirstLevelCommentId(commentId);
  if (firstLevelId && firstLevelId !== commentId && !expandedComments.value[firstLevelId]) {
    expandedComments.value[firstLevelId] = true;
  }
}

function cancelReply() {
  replyToCommentId.value = null;
  replyToUsername.value = "";
  activeReplyBox.value = null;
  replyContent.value = "";
}

async function submitComment() {
  if (!newComment.value.trim()) {
    ElMessage.warning('评论内容不能为空');
    return;
  }

  try {
    await post('/api/auth/quan/comment', {
      tieId: tieId,
      content: newComment.value,
      replyTo: replyToCommentId.value,
      firstLevelCommonId: replyToCommentId.value ? findFirstLevelCommentId(replyToCommentId.value) : null
    });
    newComment.value = "";
    cancelReply();
    fetchComments(commentPage.value);
    ElMessage.success('评论成功');
  } catch (err) {
    ElMessage.error('评论失败');
  }
}

async function submitReply(commentId) {
  if (!replyContent.value.trim()) {
    ElMessage.warning('回复内容不能为空');
    return;
  }

  try {
    const res = await post('/api/auth/quan/comment', {
      tieId: tieId,
      content: replyContent.value,
      replyTo: commentId,
      firstLevelCommonId: findFirstLevelCommentId(commentId)
    });
    
    if (res) {
      replyContent.value = "";
      cancelReply();
      fetchComments(commentPage.value);
      ElMessage.success('回复成功');
    }
  } catch (err) {
    ElMessage.error('回复失败');
  }
}

function findFirstLevelCommentId(commentId) {
  const topLevelComment = comments.value.find(c => c.id === commentId);
  if (topLevelComment) {
    return topLevelComment.id;
  }
  
  for (const comment of comments.value) {
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
}

function findCommentInReplies(replies, commentId) {
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
}

// 跳转到用户主页
const goToUserProfile = (userId) => {
  window.open(`/index/user/${userId}`, '_blank');
};

// 返回上一页
const goBack = () => {
  router.go(-1);
};
</script>

<style scoped lang="scss">
.tie-detail-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 30px 20px;
  position: relative;
}

.back-button {
  position: absolute;
  left: 30px;
  top: 30px;
  z-index: 1000;
}

.tie-detail-card {
  background-color: #fff;
  padding: 30px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  border-radius: 16px;
}

.tie-header {
  margin-bottom: 30px;
}

.tie-meta {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.user-avatar {
  margin-right: 12px;
}

.user-info {
  display: flex;
  flex-direction: column;
}

.username {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.post-time {
  font-size: 12px;
  color: #999;
}

.tie-title {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 20px;
  color: #222;
}

.tie-content {
  line-height: 1.8;
  font-size: 15px;
  color: #333;
  margin-bottom: 24px;
  word-break: break-word;
}

.tie-footer {
  display: flex;
  align-items: center;
  gap: 20px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  
  &:hover {
    color: var(--el-color-primary);
  }
}

/* 以下样式与ProjectDetail.vue保持一致 */
.comment-list {
  margin-top: 30px;
}

.comment-item {
  margin-bottom: 16px;
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.03);
}

.comment-header {
  margin-bottom: 10px;
}

.comment-user {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.comment-user .user-avatar {
  background-color: #ff9800;
}

.username {
  font-size: 14px;
  color: #333;
  margin-right: 10px;
  cursor: pointer;
  
  &:hover {
    color: var(--el-color-primary);
  }
}

.comment-time {
  font-size: 12px;
  color: #999;
}

.md-content {
  line-height: 1.6;
  word-break: break-word;
  color: #333;
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

.replies {
  margin-top: 10px;
}

.reply-item {
  position: relative;
  margin-left: 30px;
  margin-top: 15px;
  padding: 10px;
  background: #f9f9f9;
  border-radius: 6px;
}

.reply-item::before {
  content: "";
  position: absolute;
  left: -15px;
  top: 15px;
  width: 15px;
  height: 1px;
  background: #ddd;
}

.deleted {
  color: #bbb;
  font-style: italic;
  margin-left: 10px;
}

.zhihu-comment-editor {
  margin-bottom: 20px;
  border: 1px solid #f0f2f7;
  border-radius: 4px;
  padding: 12px;
}

.zhihu-reply-editor {
  margin-top: 10px;
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

.comment-actions {
  display: flex;
  gap: 12px;
  margin-top: 10px;
}

.comment-pagination {
  margin-top: 20px;
  justify-content: center;
}

@media (max-width: 768px) {
  .tie-detail-container {
    padding: 15px;
  }
  
  .tie-detail-card {
    padding: 15px;
  }
  
  .tie-title {
    font-size: 20px;
  }
  
  .reply-item {
    margin-left: 15px;
  }
}
</style>