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
      
      <!-- 评论组件 -->
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
              <strong class="username">{{ comment.username }}</strong>
              <span class="comment-time">{{ formatTime(comment.createdAt) }}</span>
            </div>
            <span v-if="comment.deleted" class="deleted">该评论已被删除</span>
            <div v-else v-html="renderMarkdown(comment.content)" class="md-content" />
          </div>
          
          <div class="comment-actions">
            <el-button text @click="replyTo(comment.id, comment.username)">回复</el-button>
            <el-button text @click="likeComment(comment.id)">👍 {{ comment.likes }}</el-button>
            <el-button text v-if="comment.isMine" @click="deleteComment(comment.id)">删除</el-button>
            
            <el-button 
              text 
              v-if="comment.replyCount > previewReplyCount"
              @click="showReplyDialog(comment)"
            >
              查看全部 {{ comment.replyCount }} 条回复
              <el-icon><ArrowRight /></el-icon>
            </el-button>
          </div>

          <!-- 预览回复 (默认显示2条) -->
          <div v-if="comment.replies?.length > 0" class="preview-replies">
            <div v-for="reply in comment.replies.slice(0, previewReplyCount)" :key="reply.id" class="reply-item">
              <div class="comment-user">
                <el-avatar :src="reply.avatar" size="small">{{ reply.username.charAt(0) }}</el-avatar>
                <strong class="username">{{ reply.username }}</strong>
                <span class="comment-time">{{ formatTime(reply.createdAt) }}</span>
              </div>
              <span v-if="reply.deleted" class="deleted">该回复已被删除</span>
              <span v-else>
                <template v-if="reply.replyToName">@{{ reply.replyToName }}：</template>
                <span v-html="renderMarkdown(reply.content)" class="md-content" />
              </span>
              <div class="comment-actions">
                <el-button text @click="replyTo(reply.id, reply.username)">回复</el-button>
                <el-button text @click="likeComment(reply.id)">👍 {{ reply.likes }}</el-button>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 主评论分页 -->
        <el-pagination
          class="comment-pagination"
          v-model:current-page="commentPage"
          :page-size="commentPageSize"
          :total="commentTotal"
          @current-change="fetchComments"
        />
      </div>
    </el-card>

    <!-- 回复弹窗 -->
    <el-dialog
      v-model="replyDialogVisible"
      :title="`共 ${currentReplyCount} 条回复`"
      width="70%"
      top="5vh"
      custom-class="reply-dialog"
    >
      <div class="dialog-reply-list">
        <div v-for="reply in dialogReplies" :key="reply.id" class="reply-item">
          <div class="comment-user">
            <el-avatar :src="reply.avatar" size="small">{{ reply.username.charAt(0) }}</el-avatar>
            <strong class="username">{{ reply.username }}</strong>
            <span class="comment-time">{{ formatTime(reply.createdAt) }}</span>
          </div>
          <span v-if="reply.deleted" class="deleted">该回复已被删除</span>
          <span v-else>
            <template v-if="reply.replyToName">@{{ reply.replyToName }}：</template>
            <span v-html="renderMarkdown(reply.content)" class="md-content" />
          </span>
          <div class="comment-actions">
            <el-button text @click="replyTo(reply.id, reply.username)">回复</el-button>
            <el-button text @click="likeComment(reply.id)">👍 {{ reply.likes }}</el-button>
          </div>
        </div>
        
        <el-pagination
          small
          layout="prev, pager, next"
          :total="currentReplyCount"
          :page-size="replyPageSize"
          :current-page="dialogReplyPage"
          @current-change="handleDialogPageChange"
          class="dialog-pagination"
        />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, inject } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { get, post } from '@/net';
import { ElMessage } from 'element-plus';
import { View, ChatDotRound, Star, Share, ArrowRight } from '@element-plus/icons-vue';
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
const replyToCommentId = ref(null);
const replyToUsername = ref("");
const activeReplyBox = ref(null);

// 评论分页
const commentPage = ref(1);
const commentPageSize = ref(10);
const commentTotal = ref(0);
const replyPageSize = ref(10); // 每页回复数量
const previewReplyCount = 2; // 预览回复数量

// 点赞状态
const liked = ref(false);

// 弹窗相关数据
const replyDialogVisible = ref(false);
const dialogReplies = ref([]);
const dialogReplyPage = ref(1);
const currentReplyCount = ref(0);
const currentCommentId = ref(null);

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
      size: commentPageSize.value,
      withPreviewReplies: true,
      previewReplyCount: previewReplyCount
    });
    
    if (res) {
      comments.value = res.records;
      commentTotal.value = res.total;
    }
  } catch (error) {
    ElMessage.error('加载评论失败');
  }
};

// 显示回复弹窗
const showReplyDialog = async (comment) => {
  replyDialogVisible.value = true;
  currentCommentId.value = comment.id;
  currentReplyCount.value = comment.replyCount;
  dialogReplyPage.value = 1;
  await fetchDialogReplies(comment.id, 1);
};

// 获取弹窗回复数据
const fetchDialogReplies = async (commentId, page = 1) => {
  try {
    const res = await post(`/api/auth/quan/commentShow`, {
      tieId: tieId,
      page: page,
      size: replyPageSize.value,
      firstCommenId: commentId,
      isReply: true
    });
    
    if (res) {
      dialogReplies.value = res.records;
      currentReplyCount.value = res.total || currentReplyCount.value;
    }
  } catch (error) {
    ElMessage.error('加载回复失败');
  }
};

// 弹窗分页切换
const handleDialogPageChange = (page) => {
  dialogReplyPage.value = page;
  fetchDialogReplies(currentCommentId.value, page);
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

// 插入表情
function insertEmojiToNewComment(emoji) {
  newComment.value += emoji;
}

// 格式化时间
function formatTime(timeString) {
  if (!timeString) return '';
  const date = new Date(timeString);
  return date.toLocaleString();
}

// 点赞评论
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

// 删除评论
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

// 渲染Markdown
function renderMarkdown(text) {
  return mdParser.render(text || '');
}

// 回复评论
function replyTo(commentId, username) {
  replyToCommentId.value = commentId;
  replyToUsername.value = username;
  activeReplyBox.value = commentId;
  
  // 滚动到评论框
  nextTick(() => {
    const editor = document.querySelector('.zhihu-comment-editor');
    if (editor) {
      editor.scrollIntoView({ behavior: 'smooth' });
    }
  });
}

// 取消回复
function cancelReply() {
  replyToCommentId.value = null;
  replyToUsername.value = "";
  activeReplyBox.value = null;
}

// 提交评论
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

// 查找一级评论ID
function findFirstLevelCommentId(commentId) {
  const comment = comments.value.find(c => c.id === commentId);
  if (!comment) return -1;
  
  if (comment.firstLevelCommonId && comment.firstLevelCommonId !== -1) {
    return comment.firstLevelCommonId;
  }
  
  return commentId;
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

.preview-replies {
  margin-top: 10px;
  border-left: 2px solid #f0f0f0;
  padding-left: 12px;
}

.reply-item {
  padding: 10px 0;
  border-bottom: 1px dashed #f0f0f0;
}

.reply-item:last-child {
  border-bottom: none;
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

/* 弹窗样式 */
.reply-dialog {
  max-height: 80vh;
  display: flex;
  flex-direction: column;
  
  :deep(.el-dialog__body) {
    flex: 1;
    overflow: auto;
    padding: 20px;
  }
}

.dialog-reply-list {
  max-height: 60vh;
  overflow-y: auto;
  padding-right: 10px;
}

.dialog-reply-list .reply-item {
  padding: 15px 0;
  border-bottom: 1px solid #f0f0f0;
}

.dialog-reply-list .reply-item:last-child {
  border-bottom: none;
}

.dialog-pagination {
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
  
  .reply-dialog {
    width: 90% !important;
  }
}

/* 调整分页样式 */
:deep(.el-pagination.is-background .btn-prev),
:deep(.el-pagination.is-background .btn-next),
:deep(.el-pagination.is-background .el-pager li) {
  min-width: 28px;
  height: 28px;
  line-height: 28px;
  margin: 0 2px;
}
</style>