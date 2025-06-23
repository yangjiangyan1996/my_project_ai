<template>
    <div class="index-container">
      <el-button @click="userLogout" class="logout-button">退出登录</el-button>
  
      <el-card class="project-detail-card">
        <div class="header">
          <img :src="detail.coverImageUrl || '/images/default-project.png'" class="cover-image" />
          <div class="basic-info">
            <h2 class="title">{{ detail.name }}</h2>
            <div class="creator">发起人：{{ detail.creatorName || '匿名' }}</div>
  
            <div class="tags">
              <el-tag v-if="detail.isRemote" type="success">远程</el-tag>
              <el-tag v-if="detail.isFreeEntry" type="info">零门槛</el-tag>
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
        <div class="comment-box">
          <v-md-editor v-model="newComment" height="200px" :placeholder="replyToCommentId ? `回复 ${replyToUsername}...` : '写下你的评论...'" />
          <div class="comment-actions">
            <el-button type="primary" @click="submitComment">发表评论</el-button>
            <el-button v-if="replyToCommentId" @click="cancelReply">取消回复</el-button>
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
              <el-button text v-if="comment.replies?.length" @click="toggleExpand(comment.id)">
                {{ expandedComments[comment.id] ? '收起回复' : `展开回复 (${comment.replies.length})` }}
              </el-button>
            </div>
  
            <!-- 回复评论输入框 -->
            <div v-if="activeReplyBox === comment.id" class="reply-box">
              <v-md-editor v-model="replyContent" height="150px" :placeholder="`回复 ${replyToUsername}...`" />
              <div class="comment-actions">
                <el-button type="primary" size="small" @click="submitReply(comment.id)">回复</el-button>
                <el-button size="small" @click="cancelReply">取消</el-button>
              </div>
            </div>
  
            <div v-if="expandedComments[comment.id]" class="replies">
              <div v-for="reply in comment.replies" :key="reply.id" class="reply-item">
                <div class="comment-user">
                  <el-avatar :src="reply.avatar" size="small" class="user-avatar">{{ reply.username.charAt(0) }}</el-avatar>
                  <strong class="username">{{ reply.username }}</strong>
                  <span class="comment-time">{{ formatTime(reply.createdAt) }}</span>
                </div>
                <span v-if="reply.deleted" class="deleted">该评论已被删除</span>
                <span v-else>
                  <template v-if="reply.replyToName">回复 {{ reply.replyToName }}：</template>
                  <span v-html="renderMarkdown(reply.content)" class="md-content" />
                </span>
                <div class="comment-actions">
                  <el-button text @click="replyTo(reply.id, reply.username)">回复</el-button>
                  <el-button text @click="likeComment(reply.id)">👍 {{ reply.likes }}</el-button>
                  <el-button text v-if="reply.isMine" @click="deleteComment(reply.id)">删除</el-button>
                </div>
  
                <!-- 嵌套回复输入框 -->
                <div v-if="activeReplyBox === reply.id" class="reply-box">
                  <v-md-editor v-model="replyContent" height="150px" :placeholder="`回复 ${replyToUsername}...`" />
                  <div class="comment-actions">
                    <el-button type="primary" size="small" @click="submitReply(reply.id)">回复</el-button>
                    <el-button size="small" @click="cancelReply">取消</el-button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </el-card>
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { get, logout, post } from '@/net';
  import { ElMessage } from 'element-plus';
  import VMdEditor from '@kangc/v-md-editor';
  import createEmojiPlugin from '@kangc/v-md-editor/lib/plugins/emoji/index';
  import githubTheme from '@kangc/v-md-editor/lib/theme/github.js';
  import '@kangc/v-md-editor/lib/style/base-editor.css';
  import '@kangc/v-md-editor/lib/theme/style/github.css';
  import '@kangc/v-md-editor/lib/plugins/emoji/emoji.css';
  import markdownIt from 'markdown-it';
  import emoji from 'markdown-it-emoji';
  
  VMdEditor.use(githubTheme);
  VMdEditor.use(createEmojiPlugin());
  
  const route = useRoute();
  const router = useRouter();
  
  const detail = ref({});
  const parsedTags = ref([]);
  const liked = ref(false);
  const collected = ref(false);
  const likeCount = ref(0);
  const favoriteCount = ref(0);
  
  const comments = ref([]);
  const newComment = ref("");
  const replyContent = ref("");
  const replyToCommentId = ref(null);
  const replyToUsername = ref("");
  const activeReplyBox = ref(null);
  const expandedComments = ref({});
  
  // 配置 markdown-it 支持 emoji
  const mdParser = markdownIt().use(emoji);
  const projectId = route.params.id;
  
  onMounted(() => {
    fetchDetail();
    fetchComments();
  });
  
  // 格式化时间
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
    replyToCommentId.value = commentId;
    replyToUsername.value = username;
    activeReplyBox.value = commentId;
    replyContent.value = `@${username} `;
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
      await post('/api/auth/project/comment', {
        projectId,
        content: replyContent.value,
        replyTo: commentId,
        firstLevelCommonId: findFirstLevelCommentId(commentId)
      });
      replyContent.value = "";
      cancelReply();
      fetchComments();
      ElMessage.success('回复成功');
    } catch (err) {
      ElMessage.error('回复失败');
    }
  }
  
  function findFirstLevelCommentId(commentId) {
    const comment = findCommentById(commentId);
    if (!comment) return null;
    return comment.replyTo === -1 ? comment.id : comment.replyTo;
  }
  
  function findCommentById(commentId) {
    for (const comment of comments.value) {
      if (comment.id === commentId) return comment;
      if (comment.replies) {
        const found = findCommentInReplies(comment.replies, commentId);
        if (found) return found;
      }
    }
    return null;
  }
  
  function findCommentInReplies(replies, commentId) {
    for (const reply of replies) {
      if (reply.id === commentId) return reply;
      if (reply.replies) {
        const found = findCommentInReplies(reply.replies, commentId);
        if (found) return found;
      }
    }
    return null;
  }
  
  async function fetchComments() {
    try {
      const res = await get(`/api/auth/project/commentShow?projectId=${projectId}`);
      comments.value = res || [];
      // 默认展开有回复的评论
      comments.value.forEach(comment => {
        if (comment.replies?.length) {
          expandedComments.value[comment.id] = true;
        }
      });
    } catch (err) {
      ElMessage.error('加载评论失败');
    }
  }
  
  async function fetchDetail() {
    try {
      const res = await get(`/api/auth/project/detail?projectId=${projectId}`);
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
  
  .creator {
    font-size: 14px;
    color: #666;
    margin-bottom: 10px;
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
  
  /* 评论区域样式 */
  .comment-box {
    margin: 20px 0;
    border: 1px solid #eee;
    border-radius: 8px;
    padding: 15px;
    background: #fff;
  }
  
  .comment-actions {
    display: flex;
    gap: 10px;
    margin-top: 10px;
  }
  
  .comment-list {
    margin-top: 30px;
  }
  
  .comment-item {
    margin-bottom: 25px;
    padding-bottom: 15px;
    border-bottom: 1px solid #f0f0f0;
  }
  
  .comment-header {
    margin-bottom: 10px;
  }
  
  .comment-user {
    display: flex;
    align-items: center;
    margin-bottom: 8px;
  }
  
  .user-avatar {
    margin-right: 8px;
    background-color: #409EFF;
    color: white;
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
  
  .reply-box {
    margin: 10px 0 10px 30px;
    padding: 15px;
    background: #f9f9f9;
    border-radius: 6px;
  }
  
  .reply-item {
    margin-left: 30px;
    margin-top: 15px;
    padding: 10px;
    background: #f9f9f9;
    border-radius: 6px;
    position: relative;
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
  
    .title,
    .creator {
      text-align: center;
      width: 100%;
    }
  
    .actions {
      justify-content: center;
    }
    
    .reply-item {
      margin-left: 15px;
    }
  }
  </style>