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
      <div class="comment-box">
        <v-md-editor v-model="newComment" height="200px" />
        <el-button class="comment-submit" type="primary" @click="submitComment">发表评论</el-button>

        <div class="comment-list">
          <div v-for="comment in comments" :key="comment.id" class="comment-item">
            <div class="comment-header">
              <strong>{{ comment.username }}</strong>
              <span v-if="comment.deleted" class="deleted">该评论已被删除</span>
              <div v-else v-html="renderMarkdown(comment.content)" class="md-content" />
            </div>
            <div class="comment-actions">
              <el-button text @click="replyTo(comment.id, comment.username)">回复</el-button>
              <el-button text @click="likeComment(comment.id)">👍 {{ comment.likes }}</el-button>
              <el-button text v-if="comment.isMine" @click="deleteComment(comment.id)">删除</el-button>
              <el-button text v-if="comment.replies?.length" @click="toggleExpand(comment.id)">
                {{ expandedComments[comment.id] ? '收起回复' : '展开回复 (' + comment.replies.length + ')' }}
              </el-button>
            </div>
            <div v-if="expandedComments[comment.id]" class="replies">
              <div v-for="reply in comment.replies" :key="reply.id" class="reply-item">
                <strong>{{ reply.username }}</strong>
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
  import { get, logout,post } from '@/net';
  import { ElMessage } from 'element-plus';

    import VMdEditor from '@kangc/v-md-editor';
    import createEmojiPlugin from '@kangc/v-md-editor/lib/plugins/emoji/index';
    import githubTheme from '@kangc/v-md-editor/lib/theme/github.js';
    import '@kangc/v-md-editor/lib/style/base-editor.css';
    import '@kangc/v-md-editor/lib/theme/style/github.css';
    import '@kangc/v-md-editor/lib/plugins/emoji/emoji.css';
    import markdownIt from 'markdown-it';
    
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
    const replyToCommentId = ref(null);
    const replyToUsername = ref("");
    const expandedComments = ref({});
    const mdParser = markdownIt();
    const projectId = route.params.id;

    onMounted(() => {
        fetchDetail();
        fetchComments();
    });
    
    async function likeComment(commentId) {
        try {
            await post('/api/auth/project/commentLike', { commentId });
            fetchComments();
        } catch (err) {
            ElMessage.error('点赞失败');
        }
    }

    async function deleteComment(commentId) {
        const id = route.params.id;
        try {
            const res = await get(`/api/auth/project/commentDeleted?projectId=${id}&commentId=${commentId}`);
            if(res){
                ElMessage.success('删除成功');
            }
            fetchComments();
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


    function replyTo(id, username) {
        replyToCommentId.value = id;
        replyToUsername.value = username;
        newComment.value = `@${username} `;
        console.log("回复评论",id,username ,newComment.value )
    }

    async function submitComment() {
        if (!newComment.value.trim()) return;
        try {
            await post('/api/auth/project/comment', {
            projectId,
            content: newComment.value,
            replyTo: replyToCommentId.value
            });
            newComment.value = "";
            replyToCommentId.value = null;
            replyToUsername.value = "";
            fetchComments();
        } catch (err) {
            ElMessage.error('评论失败');
        }
    }

    //加载评论
    async function fetchComments() {
        const id = route.params.id;
        try {
            const res = await get(`/api/auth/project/commentShow?projectId=${id}`);
            comments.value = res || [];
        } catch (err) {
            ElMessage.error('加载评论失败');
        }
    }
  
    //项目详情
  async function fetchDetail() {
    const id = route.params.id;
    try {
      const res = await get(`/api/auth/project/detail?projectId=${id}`);
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
      const result = await get(`/api/auth/project/likeProject?projectId=${route.params.id}&liked=${targetState}`);
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
      const result = await get(`/api/auth/project/favoriteProject?projectId=${route.params.id}&liked=${targetState}`);
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
  }


.comment-box {
  margin-top: 20px;
}
.comment-submit {
  margin-top: 10px;
}
.comment-list {
  margin-top: 20px;
}
.comment-item {
  margin-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
  padding-bottom: 10px;
}
.comment-header {
  font-size: 14px;
  margin-bottom: 6px;
}
.comment-actions {
  display: flex;
  gap: 10px;
  font-size: 12px;
  color: #999;
}
.reply-item {
  margin-left: 20px;
  margin-top: 5px;
  font-size: 13px;
}
.deleted {
  color: #bbb;
  font-style: italic;
  margin-left: 10px;
}
.md-content {
  line-height: 1.6;
  word-break: break-word;
}
  </style>
  