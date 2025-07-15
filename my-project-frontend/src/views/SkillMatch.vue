<template>
  <div class="skill-match-wrapper">
    <div class="skill-match-container" ref="scrollContainer" @scroll="handleScroll">
      <el-row :gutter="20">
        <el-col
          v-for="skill in skillList"
          :key="skill.id"
          :xs="24" :sm="12" :md="8" :lg="6"
        >
          <el-card class="skill-card" shadow="hover">
            <div class="skill-header">
              <span>用户名称: {{ skill.userName }}</span>
              <el-tag :type="skill.status === '公开' ? 'success' : 'info'">{{ skill.status }}</el-tag>
            </div>
            <div class="skill-body">
              <p>🕒 每日投入: {{ skill.timePerDay }}小时/天</p>
              <p>👤 身份: {{ skill.audienceName }}</p>
              <p>🛠️ 技能:
                <el-tag
                  v-for="(s, index) in skill.skillNames"
                  :key="index"
                  type="primary"
                  size="small"
                  style="margin-right: 5px; margin-bottom: 5px;"
                >{{ s }}</el-tag>
              </p>
              <p>💼 可提供:
                <el-tag
                  v-for="(r, index) in skill.resources"
                  :key="index"
                  type="warning"
                  size="small"
                  style="margin-right: 5px; margin-bottom: 5px;"
                >{{ r }}</el-tag>
              </p>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <div class="loading-area" v-if="loading">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>加载中...</span>
      </div>
      <div class="loading-area" v-else-if="!hasMore">
        <span>没有更多了</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue';
import { Loading } from '@element-plus/icons-vue';
import { post } from '@/net';
import { ElMessage } from 'element-plus';

const scrollContainer = ref(null);
const skillList = ref([]);
const loading = ref(false);
const hasMore = ref(true);
const page = ref(1);
const size = ref(10);

const fetchSkillMatchData = async () => {
  if (loading.value || !hasMore.value) return;

  loading.value = true;
  try {
    const res = await post('/api/auth/project/projectShowList', {
      page: page.value,
      size: size.value
    });

    if (res?.records) {
      const newData = res.records.map(item => ({
        id: item.id,
        userId: item.userId,
        userName: item.userName || '匿名',
        timePerDay: item.timePerDay ? `${item.timePerDay}` : '未填写',
        audience: item.audience || '未填写',
        audienceName: item.audienceName || '未填写',
        skills: item.skills ? item.skills.split(',') : [],
        skillNames: item.skillNames ? item.skillNames.split(',') : [],
        resources: item.resources ? item.resources.split(',') : [],
        status: item.status === 1 ? '公开' : '未公开'
      }));

      console.log("newData",newData)
      // 确保响应式更新
      skillList.value = [...skillList.value, ...newData];
      console.log("skillList.value",skillList.value)
      page.value += 1;
      hasMore.value = res.total > skillList.value.length;
    }
  } catch (err) {
    ElMessage.error('数据加载失败');
    console.error(err);
  } finally {
    loading.value = false;
  }
};

const handleScroll = (e) => {
  const { scrollTop, scrollHeight, clientHeight } = e.target;
  if (scrollTop + clientHeight >= scrollHeight - 100) {
    fetchSkillMatchData();
  }
};

onMounted(async () => {
  await nextTick(); // 确保DOM已渲染
  fetchSkillMatchData();
});
</script>

<style scoped>
.skill-match-wrapper {
  height: calc(100vh - 120px);
  overflow: hidden;
  background: #f5f7fa;
}

.skill-match-container {
  height: 100%;
  overflow-y: auto;
  padding: 20px;
}

.skill-card {
  margin-bottom: 20px;
  height: 100%;
  transition: all 0.3s;
}

.skill-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.skill-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #eee;
}

.skill-body {
  padding: 8px 0;
}

.skill-body p {
  margin: 8px 0;
  line-height: 1.6;
}

.loading-area {
  text-align: center;
  padding: 20px;
  color: #909399;
}

.is-loading {
  animation: rotating 1.5s linear infinite;
  margin-right: 8px;
}

@keyframes rotating {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>