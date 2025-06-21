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
              <i :class="liked ? 'el-icon-star-on' : 'el-icon-star-off'"></i> {{ liked ? '已点赞' : '点赞' }}
            </el-button>
              <el-button :type="collected ? 'primary' : 'default'" size="small" @click="collected = !collected">
                <i :class="collected ? 'el-icon-folder-opened' : 'el-icon-folder-add'"></i> {{ collected ? '已收藏' : '收藏' }}
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
      </el-card>
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { get, logout } from '@/net';
  import { ElMessage } from 'element-plus';
  
  const route = useRoute();
  const router = useRouter();
  
  const detail = ref({});
  const parsedTags = ref([]);
  const liked = ref(false);

async function handleLike() {
  const tempState = !liked.value;
  try {
    const result = await get(`/api/auth/project/likeProject?projectId=${route.params.id}&liked=${tempState}`);
    console.log("/api/auth/project/likeProject-----》返回数据",result)
    if (result) {
      liked.value = tempState;
      if(tempState) {
        ElMessage.success('成功');
      } else {
        ElMessage.success('已取消');
      }
    } else {
      ElMessage.error('点赞失败');
    }
  } catch (err) {
    ElMessage.error('网络请求异常');
  }
}
  const collected = ref(false);
  
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
    } catch (err) {
      ElMessage.error('加载详情失败');
    }
  }
  
  function userLogout() {
    logout(() => router.push("/"));
  }
  
  onMounted(fetchDetail);
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
  
  /* 响应式支持 */
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
  </style>
  