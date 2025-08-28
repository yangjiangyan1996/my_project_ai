<template>
  <div class="tie-detail-container">
    <!-- 返回按钮 -->
    <!-- <el-button @click="goBack" class="back-button">返回</el-button> -->
    
    <el-card class="tie-detail-card">
      <!-- 帖子头部部分保持不变 -->
      <div class="tie-header">
          <div class="user-meta" @click="goToUserProfile(tieDetail.secrecyId)">
        <el-avatar :size="48" :src="tieDetail.createdAvatar" class="user-avatar"></el-avatar>
        <div class="user-info">
          <div class="username-line">
            <span class="username">{{ tieDetail.username || '匿名用户' }}</span>
            <span class="post-time">{{ formatTime(tieDetail.createdTime) }}</span>
          </div>
          <div class="user-badge" v-if="tieDetail.isAuth">作者</div>
        </div>
      </div>


        <div class="tie-meta">
          
          
            <div class="image-gallery" v-if="tieDetail.avatar && tieDetail.avatar.length">
              <el-image 
                v-for="(img, index) in tieDetail.avatar" 
                :key="index" 
                :src="img" 
                :preview-src-list="tieDetail.avatar"
                :initial-index="index"
                fit="cover"
                class="gallery-image"
                :style="{ width: calculateImageWidth(tieDetail.avatar.length) }"
                :zoom-rate="1.2"
                :max-scale="7"
                :min-scale="0.2"
                preview-teleported
                hide-on-click-modal
              />
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
            <el-icon :color="favorited ? '#f56c6c' : ''"><Star /></el-icon>
            <span>{{ tieDetail.likes || 0 }}</span>
          </div>
         
          <div class="action-btn" @click="handleShare">
            <el-icon><Share /></el-icon>
            <span>分享</span>
            <!-- 分享菜单 -->
            <el-popover
              v-model:visible="shareMenuVisible"
              placement="bottom"
              trigger="click"
              :width="200"
            >
              <template #reference>
                <span></span>
              </template>
              <div class="share-menu">
                <div class="share-item" @click="copyLink">
                  <el-icon><DocumentCopy /></el-icon>
                  <span>复制链接</span>
                </div>
                <div class="share-item" @click="shareToWechat">
                  <el-icon><ChatLineRound /></el-icon>
                  <span>分享到微信</span>
                </div>
                <div class="share-item" @click="shareToWeibo">
                  <el-icon><Promotion /></el-icon>
                  <span>分享到微博</span>
                </div>
                <div class="share-item" @click="shareToQQ">
                  <el-icon><ChatLineSquare /></el-icon>
                  <span>分享到QQ</span>
                </div>
              </div>
            </el-popover>
          </div>

        </div>
      </div>
      
      <el-divider>评论区</el-divider>
      
      <!-- 主评论输入框 (固定位置) -->
      <div class="zhihu-comment-editor">
        <div class="editor-header">
          <div class="avatar">{{ userInitial }}</div>
          <span>写下你的评论...</span>
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
          <button class="submit-button" @click="submitComment">发布</button>
        </div>
      </div>

      <div class="comment-list">
        <div v-for="comment in comments" :key="comment.id" class="comment-item">
          <div class="comment-header">
            <div class="comment-user" @click="goToUserProfile(comment.secrecyId)">
              <el-avatar :src="comment.avatar" size="small" class="user-avatar">{{ comment.username.charAt(0) }}</el-avatar>
              <strong class="username">{{ comment.username }}</strong>
              <span v-if="comment.isAuth" class="author-tag">作者</span>
              <span class="comment-time">{{ formatTime(comment.createdAt) }}</span>
            </div>
            <span v-if="comment.deleted" class="deleted">该评论已被删除</span>
            <div v-else v-html="renderMarkdown(comment.content)" class="md-content" />
          </div>
          
          <div class="comment-actions">
            <el-button text @click="toggleReplyBox(comment.id, comment.username)">回复</el-button>
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
                  <span v-for="emoji in emojis" :key="emoji" class="emoji" @click="insertEmojiToReply(emoji)">
                    {{ emoji }}
                  </span>
                </div>
              </el-popover>
              
              <button class="cancel-button" @click="cancelReply">取消</button>
              <button class="submit-button" @click="submitReply(comment.id)">发布</button>
            </div>
          </div>

          <!-- 预览回复 (默认显示2条) -->
          <div v-if="comment.replies?.length > 0" class="preview-replies">
            <div v-for="reply in comment.replies.slice(0, previewReplyCount)" :key="reply.id" class="reply-item">
              <div class="comment-user">
                <el-avatar :src="reply.avatar" size="small">{{ reply.username.charAt(0) }}</el-avatar>
                <strong class="username">{{ reply.username }}</strong>
                <!-- 添加作者标签 -->
                <span v-if="reply.isAuth" class="author-tag">作者</span>
                <span class="comment-time">{{ formatTime(reply.createdAt) }}</span>
              </div>
              <span v-if="reply.deleted" class="deleted">该回复已被删除</span>
              <span v-else>
                <template v-if="reply.replyToName">@{{ reply.replyToName }}：</template>
                <span v-html="renderMarkdown(reply.content)" class="md-content" />
              </span>
              <div class="comment-actions">
                <el-button text @click="toggleReplyBox(reply.id, reply.username)">回复</el-button>
                <el-button text @click="likeComment(reply.id)">👍 {{ reply.likes }}</el-button>
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
                      <span v-for="emoji in emojis" :key="emoji" class="emoji" @click="insertEmojiToReply(emoji)">
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
            <!-- 添加作者标签 -->
            <span v-if="reply.isAuth" class="author-tag">作者</span>
            <span class="comment-time">{{ formatTime(reply.createdAt) }}</span>
          </div>
          <span v-if="reply.deleted" class="deleted">该回复已被删除</span>
          <span v-else>
            <template v-if="reply.replyToName">@{{ reply.replyToName }}：</template>
            <span v-html="renderMarkdown(reply.content)" class="md-content" />
          </span>
          <div class="comment-actions">
            <el-button text @click="toggleDialogReplyBox(reply.id, reply.username)">回复</el-button>
            <el-button text @click="likeComment(reply.id)">👍 {{ reply.likes }}</el-button>
          </div>

          <!-- 弹窗中的回复框 -->
          <div v-if="dialogActiveReplyBox === reply.id" class="zhihu-reply-editor">
            <div class="editor-header">
              <div class="avatar">{{ userInitial }}</div>
              <span>回复 {{ dialogReplyToUsername }}</span>
            </div>
            <el-input
              v-model="dialogReplyContent"
              type="textarea"
              :placeholder="`回复 ${dialogReplyToUsername}...`"
              :autosize="{ minRows: 4, maxRows: 8 }"
              class="markdown-textarea"
            />
            <div class="editor-footer">
              <el-popover placement="top" width="250" trigger="click">
                <template #reference>
                  <el-button size="small" text type="primary">😊 表情</el-button>
                </template>
                <div class="emoji-list">
                  <span v-for="emoji in emojis" :key="emoji" class="emoji" @click="insertEmojiToDialogReply(emoji)">
                    {{ emoji }}
                  </span>
                </div>
              </el-popover>
              
              <button class="cancel-button" @click="cancelDialogReply">取消</button>
              <button class="submit-button" @click="submitDialogReply(reply.id)">发布</button>
            </div>
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
import { ref, computed, onMounted, inject, nextTick } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { get, post } from '@/net';
import { ElMessage } from 'element-plus';
import { View, ChatDotRound, Star, Share, ArrowRight } from '@element-plus/icons-vue';
import markdownIt from 'markdown-it';
import emoji from 'markdown-it-emoji';
import useUserInfo from '@/hooks/useUserInfo';
import 'element-plus/dist/index.css'
// 导入需要的图标
import { DocumentCopy, ChatLineRound, Promotion, ChatLineSquare } from '@element-plus/icons-vue';

const route = useRoute();
const router = useRouter();
const { state: userInfo, loadUserInfo } = useUserInfo();

// 帖子详情数据
const tieDetail = ref({
  id: 0,
  title: '',
  content: '',
  avatar:  [],
  bar_id: 0,
  username: '',
  createdTime: '',
  views: 0,
  comments: 0,
  likes: 0,
  createdAvatar: '',
});

// 评论相关数据
const comments = ref([]);
const newComment = ref("");
const replyContent = ref("");
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
const favorited = ref(false);

// 弹窗相关数据
const replyDialogVisible = ref(false);
const dialogReplies = ref([]);
const dialogReplyPage = ref(1);
const currentReplyCount = ref(0);
const currentCommentId = ref(null);
const dialogActiveReplyBox = ref(null);
const dialogReplyToUsername = ref("");
const dialogReplyContent = ref("");

// Markdown解析器
const mdParser = markdownIt().use(emoji);
const tieId = route.params.id;
const barId = route.query.barId;
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
        createdTime: res.createdTime,
        createdAvatar: res.createdAvatar,
        avatar: Array.isArray(res.avatar) ? res.avatar : []
      };
      favorited.value = res.myLike || false;
    }
  } catch (error) {
    ElMessage.error('加载帖子详情失败');
  }
};

// 获取评论列表
const fetchComments = async (page = 1) => {
  try {
    const res = await post(`/api/unauth/quan/commentShow`, {
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

// 切换回复框显示状态
const toggleReplyBox = (commentId, username) => {
  // 如果点击的是已经打开的回复框，则关闭它
  if (activeReplyBox.value === commentId) {
    cancelReply();
    return;
  }
  
  replyToCommentId.value = commentId;
  replyToUsername.value = username;
  activeReplyBox.value = commentId;
  replyContent.value = '';
  
  // 滚动到回复框位置
  nextTick(() => {
    const editor = document.querySelector(`.comment-item[data-id="${commentId}"] .zhihu-reply-editor`);
    if (editor) {
      editor.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    }
  });
};


// 计算图片宽度，根据图片数量决定
const calculateImageWidth = (count) => {
  if (count === 1) return '100%';
  if (count === 2) return '49%';
  if (count === 3) return '32%';
  if (count === 4) return '49%'; // 4张图时两行两列
  return '32%'; // 默认3列布局
};

// 取消回复
const cancelReply = () => {
  replyToCommentId.value = null;
  replyToUsername.value = "";
  activeReplyBox.value = null;
  replyContent.value = "";
};

// 提交主评论
const submitComment = async () => {
  if (!newComment.value.trim()) {
    ElMessage.warning('评论内容不能为空');
    return;
  }

  try {
    await post('/api/auth/quan/comment', {
      tieId: tieId,
      content: newComment.value,
      replyTo: null,
      firstLevelCommonId: null
    });
    newComment.value = "";
    fetchComments(commentPage.value);
    ElMessage.success('评论成功');
  } catch (err) {
    ElMessage.error('评论失败');
  }
};


// 分享相关状态
const shareMenuVisible = ref(false);

// 处理分享按钮点击
const handleShare = () => {
  shareMenuVisible.value = true;
};

// 生成分享链接
const generateShareLink = () => {
  // console.log("生成分享链接",tieId,barId)
  return `${window.location.origin}/index/quan/QuanTieDetail/${tieId}?barId=${barId}`;
};

// 复制链接
const copyLink = async () => {
  try {
    const shareLink = generateShareLink();
    await navigator.clipboard.writeText(shareLink);
    ElMessage.success('链接已复制到剪贴板');
    shareMenuVisible.value = false;
  } catch (err) {
    ElMessage.error('复制失败，请手动复制');
  }
};

const goToUserProfile = (userId) => {
  // console.log("访问用户详情页",userId)
  // router.push(`/index/user/${userId}`)
  window.open(`/index/user/${userId}`, '_blank');
}


// 分享到微信
const shareToWechat = () => {
  const shareLink = generateShareLink();
  // 这里可以使用微信JS-SDK或生成二维码
  ElMessage.warning('请手动打开微信分享');
  shareMenuVisible.value = false;
  // 实际项目中可以集成微信分享SDK
};

// 分享到微博
const shareToWeibo = () => {
  const shareLink = generateShareLink();
  const title = tieDetail.value.title || '有趣的帖子';
  const url = `http://service.weibo.com/share/share.php?url=${encodeURIComponent(shareLink)}&title=${encodeURIComponent(title)}`;
  window.open(url, '_blank', 'width=550,height=400');
  shareMenuVisible.value = false;
};

// 分享到QQ
const shareToQQ = () => {
  const shareLink = generateShareLink();
  const title = tieDetail.value.title || '有趣的帖子';
  const url = `https://connect.qq.com/widget/shareqq/index.html?url=${encodeURIComponent(shareLink)}&title=${encodeURIComponent(title)}`;
  window.open(url, '_blank', 'width=550,height=400');
  shareMenuVisible.value = false;
};


// 提交回复
const submitReply = async (commentId) => {
  if (!replyContent.value.trim()) {
    ElMessage.warning('回复内容不能为空');
    return;
  }

  // console.log("提交回复commentId",commentId)
  try {
    await post('/api/auth/quan/comment', {
      tieId: tieId,
      content: replyContent.value,
      replyTo: commentId,
      firstLevelCommonId: findFirstLevelCommentId(commentId)
    });
    
    replyContent.value = "";
    cancelReply();
    fetchComments(commentPage.value);
    ElMessage.success('回复成功');
  } catch (err) {
    ElMessage.error('回复失败');
  }
};

// 显示回复弹窗
const showReplyDialog = async (comment) => {
  replyDialogVisible.value = true;
  currentCommentId.value = comment.id;
  currentReplyCount.value = comment.replyCount;
  dialogReplyPage.value = 1;
  dialogActiveReplyBox.value = null;
  dialogReplyContent.value = "";
  await fetchDialogReplies(comment.id, 1);
};

// 获取弹窗回复数据
const fetchDialogReplies = async (commentId, page = 1) => {
  try {
    const res = await post(`/api/unauth/quan/commentShow`, {
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

// 弹窗中切换回复框
const toggleDialogReplyBox = (replyId, username) => {
  // 如果点击的是已经打开的回复框，则关闭它
  if (dialogActiveReplyBox.value === replyId) {
    cancelDialogReply();
    return;
  }
  
  dialogActiveReplyBox.value = replyId;
  dialogReplyToUsername.value = username;
  dialogReplyContent.value = "";
  
  // 滚动到回复框位置
  nextTick(() => {
    const editor = document.querySelector(`.reply-item[data-id="${replyId}"] .zhihu-reply-editor`);
    if (editor) {
      editor.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    }
  });
};

// 取消弹窗中的回复
const cancelDialogReply = () => {
  dialogActiveReplyBox.value = null;
  dialogReplyToUsername.value = "";
  dialogReplyContent.value = "";
};

// 提交弹窗中的回复
const submitDialogReply = async (replyId) => {
  if (!dialogReplyContent.value.trim()) {
    ElMessage.warning('回复内容不能为空');
    return;
  }

  // console.log("提交弹窗中的回复currentCommentId",currentCommentId.value)
  try {
    await post('/api/auth/quan/comment', {
      tieId: tieId,
      content: dialogReplyContent.value,
      replyTo: replyId,
      firstLevelCommonId: currentCommentId.value
    });
    
    dialogReplyContent.value = "";
    cancelDialogReply();
    fetchDialogReplies(currentCommentId.value, dialogReplyPage.value);
    ElMessage.success('回复成功');
  } catch (err) {
    ElMessage.error('回复失败');
  }
};

// 查找一级评论ID
// function findFirstLevelCommentId(commentId) {
//   const comment = comments.value.find(c => c.id === commentId);
//   if (!comment) return -1;
  
//   if (comment.firstLevelCommonId && comment.firstLevelCommonId !== -1) {
//     return comment.firstLevelCommonId;
//   }
  
//   return commentId;
// }

function findFirstLevelCommentId(commentId) {
  const topLevelComment = comments.value.find(c => c.id === commentId);
  if (topLevelComment) {
    return topLevelComment.id;
  }
  
  for (const comment of comments.value) {
    // console.log("comment.replies",comment.replies)
    if (comment.replies && comment.replies.length > 0) {
      const foundReply = findCommentInReplies(comment.replies, commentId);
      if (foundReply) {
        return foundReply.firstLevelCommonId;
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

// 点赞帖子
const handleLike = async () => {
  try {
    const targetState = !favorited.value;
    const result = await get(`/api/auth/quan/favoriteTie?tieId=${tieId}&favorited=${targetState}`);
    
    if (result) {
      favorited.value = targetState;
      tieDetail.value.likes += targetState ? 1 : -1;
      ElMessage.success(targetState ? '点赞成功' : '已取消点赞');
    }
  } catch (error) {
    ElMessage.error('操作失败');
  }
};

// 插入表情到主评论
function insertEmojiToNewComment(emoji) {
  newComment.value += emoji;
}

// 插入表情到回复
function insertEmojiToReply(emoji) {
  replyContent.value += emoji;
}

// 插入表情到弹窗回复
function insertEmojiToDialogReply(emoji) {
  dialogReplyContent.value += emoji;
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
    const res = await get(`/api/auth/quan/commentLike?tieId=${tieId}&commentId=${commentId}`);
    if (res) {
      ElMessage.success('点赞成功');
      fetchComments(commentPage.value);
      if (replyDialogVisible.value) {
        fetchDialogReplies(currentCommentId.value, dialogReplyPage.value);
      }
    }
  } catch (err) {
    ElMessage.error('点赞失败');
  }
}

// 删除评论
async function deleteComment(commentId) {
  try {
    const res = await get(`/api/auth/quan/commentDeleted?tieId=${tieId}&commentId=${commentId}`);
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

// 返回上一页
const goBack = () => {
    // console.log("返回上一页",barId)
  router.push('/index/quan/QuanDetail/' + barId);
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
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(5px);
  border: none;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;

  &:hover {
    transform: translateX(-3px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
  }
}

.tie-detail-card {
  background-color: #fff;
  padding: 30px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  border-radius: 16px;
  border: 1px solid rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
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
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

  &:hover {
    transform: scale(1.05);
  }
}

.user-info {
  display: flex;
  flex-direction: column;
}

.username {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  cursor: pointer;
  transition: all 0.2s ease;
  
  &:hover {
    color: var(--el-color-primary);
  }
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
  line-height: 1.4;
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
  border-top: 1px solid rgba(0, 0, 0, 0.05);
}

.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 20px;
  transition: all 0.2s ease;
  
  &:hover {
    color: var(--el-color-primary);
    background: rgba(64, 158, 255, 0.1);
    transform: translateY(-1px);
  }
}

.comment-list {
  margin-top: 30px;
}

.comment-item {
  margin-bottom: 20px;
  padding: 20px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(0, 0, 0, 0.03);
  transition: all 0.3s ease;

  &:hover {
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
    transform: translateY(-2px);
  }
}

.comment-header {
  margin-bottom: 12px;
}

.comment-user {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.comment-user .user-avatar {
  background-color: #ff9800;
  margin-right: 10px;
}

.username {
  font-size: 15px;
  color: #333;
  margin-right: 10px;
  font-weight: 600;
}

.comment-time {
  font-size: 12px;
  color: #999;
}

.md-content {
  line-height: 1.7;
  word-break: break-word;
  color: #333;
  font-size: 15px;
  padding-left: 42px;
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
  margin-top: 15px;
  border-left: 2px solid rgba(0, 0, 0, 0.05);
  padding-left: 20px;
  position: relative;
}

.preview-replies::before {
  content: '';
  position: absolute;
  left: -1px;
  top: 0;
  bottom: 0;
  width: 2px;
  background: linear-gradient(to bottom, transparent, rgba(64, 158, 255, 0.3), transparent);
}

.reply-item {
  padding: 15px 0;
  position: relative;
  transition: all 0.2s ease;

  &:not(:last-child)::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    height: 1px;
    background: linear-gradient(to right, transparent, rgba(0, 0, 0, 0.05), transparent);
  }

  &:hover {
    transform: translateX(3px);
  }
}

.deleted {
  color: #bbb;
  font-style: italic;
  margin-left: 10px;
}

.view-all-replies {
  margin-top: 10px;
  display: flex;
  justify-content: center;
}


.author-tag {
  display: inline-block;
  padding: 2px 6px;
  margin-left: 8px;
  font-size: 12px;
  color: #fff;
  background: linear-gradient(to right, #ff9500, #ff5e3a);
  border-radius: 10px;
  transform: scale(0.9);
  line-height: 1;
  box-shadow: 0 1px 3px rgba(255, 94, 58, 0.3);
}

.view-all-replies-btn {
  background: rgba(64, 158, 255, 0.1);
  color: #409EFF;
  border: none;
  padding: 6px 16px;
  border-radius: 20px;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.2s ease;
  display: inline-flex;
  align-items: center;
  gap: 4px;

  &:hover {
    background: rgba(64, 158, 255, 0.2);
    transform: translateY(-1px);
  }
}

.zhihu-comment-editor {
  margin-bottom: 30px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  border-radius: 12px;
  padding: 16px;
  background: #fff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;

  &:hover {
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  }
}

.zhihu-reply-editor {
  margin-top: 15px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  border-radius: 12px;
  padding: 16px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  }
}

.editor-header {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.editor-header .avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #409EFF, #79bbff);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  font-weight: bold;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

.markdown-textarea {
  width: 100%;
  border-radius: 8px;
  transition: all 0.3s ease;

  &:deep(.el-textarea__inner) {
    min-height: 100px !important;
    padding: 12px;
    border-radius: 8px;
    border: 1px solid rgba(0, 0, 0, 0.08);
    transition: all 0.3s ease;

    &:focus {
      border-color: #409EFF;
      box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
    }
  }
}

.editor-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
}


.share-menu {
  padding: 8px 0;
  
  .share-item {
    display: flex;
    align-items: center;
    padding: 8px 16px;
    cursor: pointer;
    transition: all 0.2s ease;
    
    &:hover {
      background-color: #f5f7fa;
      color: var(--el-color-primary);
    }
    
    .el-icon {
      margin-right: 8px;
      font-size: 16px;
    }
    
    span {
      font-size: 14px;
    }
  }
}

.action-btn {
  position: relative;
  
  .el-popover__reference {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    opacity: 0;
  }
}


.submit-button {
  background: linear-gradient(to right, #409EFF, #64b5ff);
  color: white;
  border: none;
  padding: 8px 20px;
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s ease;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);

  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
  }
}

.cancel-button {
  background: none;
  border: none;
  color: #8590a6;
  padding: 8px 16px;
  cursor: pointer;
  border-radius: 20px;
  transition: all 0.2s ease;

  &:hover {
    background: rgba(0, 0, 0, 0.03);
    color: #666;
  }
}

.emoji-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  max-height: 200px;
  overflow-y: auto;
  padding: 8px;
}

.emoji {
  cursor: pointer;
  font-size: 22px;
  padding: 4px;
  transition: all 0.2s ease;
  border-radius: 4px;

  &:hover {
    transform: scale(1.2);
    background: rgba(0, 0, 0, 0.05);
  }
}

.comment-actions {
  display: flex;
  gap: 12px;
  margin-top: 12px;
  padding-left: 42px;

  .el-button {
    font-size: 13px;
    color: #666;
    transition: all 0.2s ease;

    &:hover {
      color: #409EFF;
      transform: translateY(-1px);
    }
  }
}

.comment-pagination {
  margin-top: 30px;
  justify-content: center;
}

/* 弹窗样式 */
.reply-dialog {
  max-height: 80vh;
  display: flex;
  flex-direction: column;
  border-radius: 16px;
  overflow: hidden;
  background: #f8fafc;
  
  :deep(.el-dialog__header) {
    background: linear-gradient(to right, #409EFF, #64b5ff);
    margin: 0;
    padding: 16px 24px;
    
    .el-dialog__title {
      color: white;
    }
    
    .el-dialog__headerbtn {
      color: white;
      
      &:hover {
        color: rgba(255, 255, 255, 0.8);
      }
    }
  }
  
  :deep(.el-dialog__body) {
    flex: 1;
    overflow: auto;
    padding: 20px;
    background: #f8fafc;
  }
}

.dialog-reply-list {
  max-height: 60vh;
  overflow-y: auto;
  padding-right: 10px;
}

.dialog-reply-list .reply-item {
  padding: 18px;
  margin-bottom: 12px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  }
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
  
  .md-content {
    padding-left: 0;
  }
  
  .comment-actions {
    padding-left: 0;
  }
}

/* 调整分页样式 */
:deep(.el-pagination.is-background .btn-prev),
:deep(.el-pagination.is-background .btn-next),
:deep(.el-pagination.is-background .el-pager li) {
  min-width: 32px;
  height: 32px;
  line-height: 32px;
  margin: 0 4px;
  border-radius: 8px;
  transition: all 0.2s ease;
  
  &:hover {
    transform: translateY(-1px);
  }
}

:deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
  background: linear-gradient(to right, #409EFF, #64b5ff);
  box-shadow: 0 2px 6px rgba(64, 158, 255, 0.3);
}

/* 动画效果 */
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.comment-item {
  animation: fadeIn 0.3s ease forwards;
}

.reply-item {
  animation: fadeIn 0.3s ease forwards;
}

/* 滚动条样式 */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.03);
  border-radius: 3px;
}

::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.1);
  border-radius: 3px;
  
  &:hover {
    background: rgba(0, 0, 0, 0.2);
  }
}

.user-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  
  &:hover {
    .username {
      color: var(--el-color-primary);
    }
    .user-avatar {
      transform: scale(1.05);
    }
  }
}

.image-gallery {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin: 16px 0;
  
  .gallery-image {
    border-radius: 8px;
    overflow: hidden;
    cursor: pointer;
    transition: all 0.3s ease;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    aspect-ratio: 16/9;
    background-color: #f5f5f5;
    
    &:hover {
      transform: scale(1.02);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    }
    
    :deep(.el-image__inner) {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.3s ease;
    }
    
    :deep(.el-image__error) {
      display: flex;
      align-items: center;
      justify-content: center;
      background-color: #f5f5f5;
      color: #999;
      font-size: 14px;
    }
    
    :deep(.el-image__placeholder) {
      background-color: #f5f5f5;
    }
  }
  
  // 单张图片特殊处理
  .gallery-image:only-child {
    max-height: 400px;
    width: 100% !important;
  }
  
  // 两张图片特殊处理
  .gallery-image:nth-child(1):nth-last-child(2),
  .gallery-image:nth-child(2):nth-last-child(1) {
    flex: 1 1 calc(50% - 8px);
  }
  
  // 三张图片特殊处理
  .gallery-image:nth-child(1):nth-last-child(3),
  .gallery-image:nth-child(2):nth-last-child(2),
  .gallery-image:nth-child(3):nth-last-child(1) {
    flex: 1 1 calc(33.333% - 8px);
  }
  
  // 四张及以上图片特殊处理
  .gallery-image:nth-child(n+4) {
    flex: 1 1 calc(25% - 8px);
  }
}
</style>