<template>
  <div class="index-container">
    <el-button @click="goBack" class="back-button">返回</el-button>
    <el-button @click="userLogout" class="logout-button">退出登录</el-button>

    <el-card class="project-detail-card">
      <!-- 项目详情头部部分保持不变 -->
      <div class="header">
        <img :src="detail.imageUrl || '/images/default-project.png'" class="cover-image" />
        <div class="basic-info">
          <h2 class="title">{{ detail.name }}</h2>
          
          <div class="creator">
            <div class="user-info" @click="goToUserProfile(detail.secrecyId)">
              <el-avatar :size="32" class="user-avatar" >{{ detail.creatorName?.charAt(0) || '匿' }}</el-avatar>
              <div class="user-detail">
                <div class="username">{{ detail.creatorName || '匿名用户' }}</div>
              </div>
            </div>
            <el-button 
              v-if="userInfo.data?.id !== detail.createdBy"
              :type="isConcerned ? 'success' : 'default'"
              :loading="concernLoading"
              size="small"
              @click="handleConcernPublisher(detail.createdBy)"
              class="concern-button">
              {{ isConcerned ? '已关注' : '关注' }}
            </el-button>

            <el-button 
              v-if="detail.memberNum > 0"
              :type="getApplyButtonType(applyStatus)"
              size="small"
              @click="handleApply"
              :loading="applyLoading"
              :disabled="applyStatus === 3">
              <i :class="getApplyButtonIcon(applyStatus)" />
              <span style="margin-left: 4px">
                {{ getApplyButtonText(applyStatus) }}
              </span>
            </el-button>
          </div>

          <div class="tags">
            <el-tag v-for="tag in parsedTags" :key="tag" type="warning">{{ tag }}</el-tag>
          </div>

          <div class="meta">
            <span>每日投入：{{ detail.timePerDay }}</span>
            <span>月收益：{{ detail.incomeEstimate }}</span>
            <span>适合人群：{{ detail.targetAudience }}</span>
          </div>

          <div class="actions">
            <el-button :type="liked ? 'danger' : 'default'" size="small" @click="handleLike">
              <i :class="liked ? 'el-icon-star-on' : 'el-icon-star-off'" />
              <span style="margin-left: 4px">{{ liked ? '已点赞' : '点赞' }}（{{ likeCount }}）</span>
            </el-button>

            <el-button :type="collected ? 'primary' : 'default'" size="small" @click="handleFavorite">
              <i :class="collected ? 'el-icon-folder-opened' : 'el-icon-folder-add'" />
              <span style="margin-left: 4px">{{ collected ? '已收藏' : '收藏' }}（{{ favoriteCount }}）</span>
            </el-button>
          </div>
        </div>
      </div>

      <el-divider>操作步骤</el-divider>
      <el-card class="section" v-html="detail.steps" />

      <el-divider>推荐工具</el-divider>
      <el-card class="section" v-html="detail.tools" />

      <el-divider>风险提示</el-divider>
      <el-alert :title="detail.riskWarning" type="warning" show-icon :closable="false" />

      <el-divider>评论区</el-divider>
      
      <!-- 主评论输入框 -->
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
              <strong class="username"  @click.stop="goToUserProfile(comment.secrecyId)">{{ comment.username }}</strong>
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
                <strong class="username"  @click.stop="goToUserProfile(reply.secrecyId)">{{ reply.username }}</strong>
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
      </div>
    </el-card>

    <!-- 申请加入对话框 -->
    <el-dialog v-model="applyDialogVisible" title="申请加入" width="500px">
      <el-form :model="applyForm" label-width="80px">
        <el-form-item label="申请留言">
          <el-input 
            v-model="applyForm.message" 
            type="textarea" 
            placeholder="请简单介绍一下自己或申请理由（选填）"
            :autosize="{ minRows: 4, maxRows: 8 }"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="applyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitApply">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, inject, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { get, logout, post } from '@/net';
import { ElMessage, ElMessageBox } from 'element-plus';
import markdownIt from 'markdown-it';
import emoji from 'markdown-it-emoji';

const route = useRoute();
const router = useRouter();

const detail = ref({});
const parsedTags = ref([]);
const liked = ref(false);
const collected = ref(false);
const likeCount = ref(0);
const favoriteCount = ref(0);
const isConcerned = ref(false);
const concernCount = ref(0);
const concernLoading = ref(false);

const comments = ref([]);
const newComment = ref("");
const replyContent = ref("");
const replyToCommentId = ref(null);
const replyToUsername = ref("");
const activeReplyBox = ref(null);
const expandedComments = ref({});

const mdParser = markdownIt().use(emoji);
const projectId = route.params.id;
const userInfo = inject('userInfo');
const emojis = inject('emojis');

const applyStatus = ref(-1); // -1-未申请 0=待审核 1-已通过 2-已拒绝 3-已撤销
const applyLoading = ref(false);
const applyDialogVisible = ref(false);
const applyForm = ref({
  message: ''
});

const userInitial = computed(() => {
  return userInfo.data?.username?.charAt(0) || '匿';
});

onMounted(async () => {
  await userInfo.loadUserInfo?.();
  fetchDetail();
  fetchComments();
});

const goToUserProfile = (userId) => {
  console.log("访问用户详情页",userId)
  // router.push(`/index/user/${userId}`)
  window.open(`/index/user/${userId}`, '_blank');
}

// 获取申请按钮文本
const getApplyButtonText = (status) => {
  switch(status) {
    case -1: return '申请加入';
    case 0: return '审核中';
    case 1: return '已加入';
    case 2: return '已拒绝';
    case 3: return '重新申请';
    default: return '申请加入';
  }
};

// 获取申请按钮类型
const getApplyButtonType = (status) => {
  switch(status) {
    case -1: return 'primary';
    case 0: return 'info';
    case 1: return 'success';
    case 2: return 'danger';
    case 3: return 'warning';
    default: return 'primary';
  }
};

// 获取申请按钮图标
const getApplyButtonIcon = (status) => {
  switch(status) {
    case -1: return 'el-icon-circle-plus-outline';
    case 0: return 'el-icon-loading';
    case 1: return 'el-icon-success';
    case 2: return 'el-icon-error';
    case 3: return 'el-icon-refresh-left';
    default: return 'el-icon-circle-plus-outline';
  }
};

// 返回
const goBack = () => {
  router.go(-1)
}

function insertEmojiToNewComment(emoji) {
  newComment.value += emoji;
}

function formatTime(timeString) {
  if (!timeString) return '';
  const date = new Date(timeString);
  return date.toLocaleString();
}

async function likeComment(commentId) {
  try {
    const res = await get(`/api/auth/project/commentLike?projectId=${projectId}&commentId=${commentId}`);
    if (res) {
      ElMessage.success('点赞成功');
      fetchComments();
    }
  } catch (err) {
    ElMessage.error('点赞失败');
  }
}

async function deleteComment(commentId) {
  try {
    const res = await get(`/api/auth/project/commentDeleted?projectId=${projectId}&commentId=${commentId}`);
    if (res) {
      ElMessage.success('删除成功');
      fetchComments();
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
  // 如果点击的是已经打开的回复框，则关闭它
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
    await post('/api/auth/project/comment', {
      projectId,
      content: newComment.value,
      replyTo: replyToCommentId.value,
      firstLevelCommonId: replyToCommentId.value ? findFirstLevelCommentId(replyToCommentId.value) : null
    });
    newComment.value = "";
    cancelReply();
    fetchComments();
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
    const res = await post('/api/auth/project/comment', {
      projectId,
      content: replyContent.value,
      replyTo: commentId,
      firstLevelCommonId: findFirstLevelCommentId(commentId)
    });
    
    if (res) {
      replyContent.value = "";
      cancelReply();
      fetchComments();
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

async function fetchComments() {
  try {
    const res = await get(`/api/unauth/project/commentShow?projectId=${projectId}`);
    comments.value = res || [];
    comments.value.forEach(comment => {
      if (comment.replies?.length) {
        expandedComments.value[comment.id] = true;
      }
    });
  } catch (err) {
    ElMessage.error('加载评论失败');
  }
}

const handleConcernPublisher = async (userId) => {
  if (concernLoading.value) return;
  if (!userInfo.data?.id) {
    return router.push('/login');
  }
  
  try {
    concernLoading.value = true;
    const res = await post(`/api/auth/project/concernPublisher${isConcerned.value ? 'Cancel' : ''}`, { 
      followeeId: userId
    });
    
    isConcerned.value = !isConcerned.value;
    concernCount.value += isConcerned.value ? 1 : -1;
    ElMessage.success(isConcerned.value ? '关注成功' : '已取消关注');
    
    if (detail.value) {
      detail.value.followed = isConcerned.value;
    }
  } catch (error) {
    ElMessage.error('操作失败');
  } finally {
    concernLoading.value = false;
  }
};

// 处理申请加入逻辑
const handleApply = async () => {
  if (applyLoading.value) return;
  
  try {
    applyLoading.value = true;
    
    if (applyStatus.value === -1 || applyStatus.value === 3) {
      // 显示申请留言对话框
      applyDialogVisible.value = true;
      applyForm.value.message = '';
    } else if (applyStatus.value === 1) {
      // 已加入状态，点击跳转到项目详情
      // router.push(`/project/${projectId}/workspace`);
      //TODO yang 这里跳转到组
    } else if (applyStatus.value === 0) {
      // 审核中状态，可以撤销申请
      const confirm = await ElMessageBox.confirm(
        '确定要撤销申请吗？', 
        '撤销申请', 
        { type: 'warning' }
      );
      if (confirm) {
        const res = await get(`/api/auth/project/cancelApplyJoinProject?projectId=${projectId}`);
        if (res) {
          applyStatus.value = 3;
          ElMessage.success('申请已撤销');
        }
      }
    }
  } catch (err) {
    ElMessage.error('操作失败');
  } finally {
    applyLoading.value = false;
  }
};

// 提交申请
const submitApply = async () => {
  try {
    applyLoading.value = true;
    const res = await post('/api/auth/project/applyJoinProject', {
      projectId: projectId,
      message: applyForm.value.message
    });
    
    if (res) {
      applyStatus.value = 0;
      applyDialogVisible.value = false;
      ElMessage.success('申请已提交，请等待审核');
    }
  } catch (err) {
    // ElMessage.error('提交申请失败');
  } finally {
    applyLoading.value = false;
  }
};

async function fetchDetail() {
  try {
    const res = await get(`/api/unauth/project/detail?projectId=${projectId}`);
    isConcerned.value = res.followed || false;
    concernCount.value = res.concernCount || 0;
    if (!res) {
      ElMessage.error('项目不存在或已下架');
      router.push('/');
      return;
    }
    detail.value = res;
    parsedTags.value = res.tags ? res.tags.split(',') : [];
    liked.value = !!res.myLike;
    collected.value = !!res.myFavorite;
    likeCount.value = res.likeCount || 0;
    favoriteCount.value = res.favoriteCount || 0;
    applyStatus.value = res.applyStatus ?? -1; 
  } catch (err) {
    ElMessage.error('加载详情失败');
  }
}

async function handleLike() {
  const targetState = !liked.value;
  try {
    const result = await get(`/api/auth/project/likeProject?projectId=${projectId}&liked=${targetState}`);
    if (result) {
      liked.value = targetState;
      likeCount.value += targetState ? 1 : -1;
      ElMessage.success(targetState ? '点赞成功' : '已取消点赞');
    } else {
      ElMessage.error('操作失败');
    }
  } catch (err) {
    ElMessage.error('请求异常');
  }
}

async function handleFavorite() {
  const targetState = !collected.value;
  try {
    const result = await get(`/api/auth/project/favoriteProject?projectId=${projectId}&liked=${targetState}`);
    if (result) {
      collected.value = targetState;
      favoriteCount.value += targetState ? 1 : -1;
      ElMessage.success(targetState ? '收藏成功' : '已取消收藏');
    } else {
      ElMessage.error('操作失败');
    }
  } catch (err) {
    ElMessage.error('请求异常');
  }
}

function userLogout() {
  logout(() => router.push("/"));
}
</script>

<style scoped>
.index-container {
  padding: 30px;
  max-width: 1100px;
  margin: 0 auto;
  background: #f7f9fc;
}

.logout-button {
  position: absolute;
  right: 30px;
  top: 30px;
  z-index: 1000;
}

.project-detail-card {
  background-color: #fff;
  padding: 30px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  border-radius: 16px;
}

.header {
  display: flex;
  flex-wrap: wrap;
  gap: 24px;
}

.cover-image {
  width: 320px;
  height: 200px;
  object-fit: cover;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.basic-info {
  flex: 1;
  min-width: 300px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.title {
  font-size: 26px;
  font-weight: 700;
  margin-bottom: 6px;
  color: #222;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-avatar {
  background-color: #409EFF;
  color: white;
  font-weight: bold;
}

.user-detail {
  display: flex;
  align-items: center;
  gap: 6px;
}

.username {
  font-size: 14px;
  font-weight: 500;
  color: #222;
}

.creator {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.concern-button {
  padding: 5px 12px;
  border-radius: 16px;
}

.concern-count {
  color: #8590a6;
  font-size: 12px;
}

.tags {
  margin-bottom: 12px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.meta {
  font-size: 14px;
  color: #555;
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-top: 10px;
}

.actions {
  margin-top: 20px;
  display: flex;
  gap: 12px;
}

.section {
  margin-bottom: 24px;
  padding: 20px;
  line-height: 1.7;
  color: #444;
  background: #fcfcfc;
  border-radius: 10px;
  word-break: break-word;
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

@media screen and (max-width: 768px) {
  .header {
    flex-direction: column;
    align-items: center;
  }

  .cover-image {
    width: 100%;
    height: auto;
  }

  .basic-info {
    align-items: flex-start;
    width: 100%;
  }

  .title, .creator {
    text-align: center;
    width: 100%;
  }

  .actions {
    justify-content: center;
  }
  
  .reply-item {
    margin-left: 15px;
  }
  
  .creator {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .concern-button {
    align-self: flex-end;
  }
}

.back-button {
  position: absolute;
  left: 30px;
  top: 30px;
  z-index: 1000;
}
</style>