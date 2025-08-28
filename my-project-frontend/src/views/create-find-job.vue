<template>
  <div class="join-sidejob-container">
    <el-button @click="goBack" class="back-button">返回</el-button>
    <!-- 🣍 我的加入意向卡 -->
    <el-card class="intent-card" shadow="always">
      <h2>🧍 我的加入意向</h2>
      <el-form :model="form" label-width="100px">
        <el-form-item label="我的身份">
          <el-input v-model="form.audience" placeholder="如：上班族、大学生、宝妈等"></el-input>
        </el-form-item>
        <el-form-item label="可投入时间">
          <el-input v-model="form.time" placeholder="如：每天2小时、每周末全天"></el-input>
        </el-form-item>
        <el-form-item label="个人技能">
          <el-input v-model="form.skills" placeholder="如：剪辑、写作、编程、社群运营等"></el-input>
        </el-form-item>
        <el-form-item label="我能提供">
          <el-input v-model="form.resources" placeholder="如：设备、人脉、账号资源等"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submitIntent(false)">更新显示卡</el-button>
          <el-button type="success" @click="submitIntent(true)">发布到广场</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 📨 已申请的副业 -->
    <el-card class="applied-projects" shadow="never">
      <h3>📌 我申请过的副业</h3>
      <el-empty v-if="appliedList.length === 0" description="你还没有申请任何副业项目" />
      <div v-else>
        <div class="project-card" v-for="item in appliedList" :key="item.id">
          <div class="project-title">{{ item.name }}</div>
          <div class="project-status">申请状态：<span>{{ item.status }}</span></div>
        </div>
      </div>
    </el-card>

    <!-- 💼 推荐副业列表 -->
    <el-card class="recommended-projects" shadow="never">
      <h3>🔥 推荐副业项目</h3>
      <div v-if="projects.length === 0">
        <el-empty description="暂无推荐项目，稍后再来看看吧~" />
      </div>
      <el-row :gutter="20">
        <el-col :span="8" v-for="item in projects" :key="item.id">
          <el-card class="project-card" shadow="hover">
            <div class="project-name">{{ item.name }}</div>
            <div class="project-desc">{{ item.description }}</div>
            <div class="project-tags">
              <el-tag v-for="tag in item.tags.split(',')" :key="tag" type="info" size="small">{{ tag }}</el-tag>
            </div>
            <el-button type="success" size="small" @click="applyToProject(item.id)">申请加入</el-button>
          </el-card>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { logout, post, get } from '@/net';
import { useRouter } from 'vue-router'

const router = useRouter()

const form = reactive({
  time: '',
  skills: '',
  resources: ''
})

const appliedList = ref([
  { id: 1, name: 'AI 文案创作组', status: '已通过' },
  { id: 2, name: '短视频分销团队', status: '已申请' }
])

const projects = ref([
  {
    id: 101,
    name: '短视频剪辑合作',
    description: '每周产出3个分享视频，需要剪辑和BGM编辑投入',
    tags: '短视频,剪辑,BGM'
  },
  {
    id: 102,
    name: '文案帮写+自媒体合作',
    description: '日更微信公众号文案、想法写作周括',
    tags: '写作,文案,创意'
  }
])
// 返回
const goBack = () => {
  router.go(-1)
}

const submitIntent = async (publish) => {
  try {
    const res = await post('/api/auth/project/updateProjectOfMyShow', {
      ...form,
      status: publish ? 1 : 0
    });
    // console.log("result",res)
    ElMessage.success(res || '操作成功');
    goBack()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '请求失败');
  }
}

const applyToProject = (id) => {
  ElMessage.success('已申请加入项目 ID: ' + id)
  appliedList.value.push({ id, name: projects.value.find(p => p.id === id).name, status: '已申请' })
}
</script>

<style scoped>
.join-sidejob-container {
  padding: 30px;
  max-width: 1000px;
  margin: 0 auto;
}

.intent-card {
  margin-bottom: 30px;
  background: #f0f9ff;
  border: 1px solid #a0d8ef;
  border-radius: 12px;
}

.project-card {
  margin-bottom: 20px;
}

.project-name {
  font-weight: bold;
  font-size: 16px;
  margin-bottom: 5px;
}

.project-desc {
  color: #666;
  font-size: 14px;
  margin-bottom: 10px;
}

.project-tags {
  margin-bottom: 10px;
}
.back-button {
  position: absolute;
  left: 30px;
  top: 30px;
  z-index: 1000;
}

</style>
