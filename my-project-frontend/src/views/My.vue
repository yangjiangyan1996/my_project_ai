<template>
  <div class="profile-container">
    <div class="profile-header">
      <div class="profile-info">
        <div class="profile-header">
          <div class="user-profile-wrapper">
            <!-- 头像部分 -->
            <el-avatar 
              :size="80" 
              :src="userInfo.data?.avatarUrl" 
              class="user-avatar"
              :style="{
                backgroundColor: userInfo.data?.avatarUrl ? 'transparent' : '#409EFF',
                color: 'white',
                fontSize: '24px'
              }"
            >
              {{ userInfo.data?.nikeName ? userInfo.data.nikeName.charAt(0) : '用' }}
            </el-avatar>
            
            <!-- 用户信息部分 -->
            <div class="user-info-wrapper">
              <div class="user-info-main">
                <h1 class="username">{{ userInfo.data?.nikeName || '用户名' }}</h1>
                <div class="industry-badge">
                  <el-tag 
                    v-if="userInfo.data?.industryName"
                    effect="dark"
                    type="info"
                    size="small"
                  >
                    {{ userInfo.data.industryName }}
                  </el-tag>
                  <el-tag 
                    v-else
                    effect="plain"
                    type="info"
                    size="small"
                  >
                    未设置行业
                  </el-tag>
                </div>
              </div>
              
              <!-- 编辑按钮 -->
              <el-button 
                type="primary" 
                size="small" 
                @click="goToUpdateUserInfo"
                class="edit-profile-btn"
                plain
                round
              >
                <el-icon><Edit /></el-icon>
                <span>编辑资料</span>
              </el-button>
            </div>
          </div>
        </div>


        <div class="sidejob-entry-card">
          <div class="sidejob-header">
            <el-icon size="22"><Suitcase /></el-icon>
            <span class="sidejob-title">开启副业，赚外快</span>
          </div>
          <div class="sidejob-description">选择你的路径：发起副业项目，或加入有趣团队</div>
          <div class="sidejob-buttons">
            <!-- 发起副业 - 使用火箭表示开始新事物，保持primary蓝色 -->
            <el-button type="primary" size="large" @click="goToCreateSidejob">
              🚀 我要发起副业
            </el-button>
            
            <!-- 找团队 - 使用握手符号表示合作，改为info天蓝色 -->
            <el-button type="info" size="large" @click="toggleIntentForm">
              👥 我想找团队
            </el-button>

            <!-- 审核 - 使用警徽表示审核权限，使用warning黄色 -->
            <el-button type="warning" size="large" @click="goToApplyList">
              🛡️ 去审核（{{ stats.applyCount || 0 }}）
            </el-button>

            <!-- 我申请的 - 使用文档符号表示申请记录，使用success绿色 -->
            <el-button type="success" size="large" @click="goToApplicationList">
              📄 我申请的
            </el-button>

            <!-- 管理员审核 - 使用星标表示管理员权限，使用danger红色 -->
            <el-button 
              type="danger" 
              size="large" 
              @click="goToAdminApplyList"
              v-if="userInfo.data?.role === 'ADMIN'"
            >
              ⭐ 去审核用户发布的帖子
            </el-button>
          </div>

          <!-- 意向表单区域 -->
          <div class="intent-section" v-if="showIntentSection">
            <el-alert 
              v-if="!hasSubmitted"
              title="请先填写您的加入意向信息，让更多人了解您"
              type="info"
              show-icon
              :closable="false"
              class="intent-alert"
            />
            
            <el-card class="intent-card" shadow="always">
              <h2>🧍 我的加入意向</h2>
              
              <template v-if="hasSubmitted && !editMode">
                <!-- 已提交时的展示模式 -->
                <div class="intent-display">
                  <div class="intent-item">
                    <span class="intent-label">我的身份：</span>
                    <span class="intent-value">{{ form.audience || '未填写' }}</span>
                  </div>
                  <div class="intent-item">
                    <span class="intent-label">可投入时间：</span>
                    <span class="intent-value">{{ form.time || '未填写' }}</span>
                  </div>
                  <div class="intent-item">
                    <span class="intent-label">个人技能：</span>
                    <span class="intent-value">{{ form.skills || '未填写' }}</span>
                  </div>
                  <div class="intent-item">
                    <span class="intent-label">我能提供：</span>
                    <span class="intent-value">{{ form.resources || '未填写' }}</span>
                  </div>
                  
                  <div class="intent-actions">
                    <el-button 
                      v-if="form.status === 0"
                      type="success" 
                      @click="changeShowStatus"
                      :loading="publishing"
                    >
                      发布到广场
                    </el-button>
                    <el-button 
                      type="primary" 
                      @click="editMode = true"
                    >
                      编辑意向
                    </el-button>
                    <el-tag v-if="form.published" type="success" class="published-tag">
                      <el-icon><SuccessFilled /></el-icon> 已发布
                    </el-tag>
                  </div>
                </div>
              </template>
              
              <!-- 编辑模式 -->
              <el-form 
                v-if="!hasSubmitted || editMode"
                :model="form" 
                label-width="100px"
                class="intent-form"
              >
                <el-form-item label="我的身份">
                  <el-input 
                    v-model="form.audience" 
                    placeholder="如：上班族、大学生、宝妈等"
                    clearable
                  ></el-input>
                </el-form-item>
                <el-form-item label="可投入时间">
                  <el-input 
                    v-model="form.time" 
                    placeholder="如：每天2小时、每周末全天"
                    clearable
                  ></el-input>
                </el-form-item>
                <el-form-item label="个人技能">
                  <el-input 
                    v-model="form.skills" 
                    placeholder="如：剪辑、写作、编程、社群运营等"
                    clearable
                  ></el-input>
                </el-form-item>
                <el-form-item label="我能提供">
                  <el-input 
                    v-model="form.resources" 
                    placeholder="如：设备、人脉、账号资源等"
                    clearable
                  ></el-input>
                </el-form-item>
                <el-form-item label="是否发布">
                  <el-radio-group v-model="form.status">
                    <el-radio :label="1">发布</el-radio>
                    <el-radio :label="0">不发布</el-radio>
                  </el-radio-group>
                </el-form-item>
                <el-form-item>
                  <el-button 
                    type="primary" 
                    @click="submitIntent(false)"
                    :loading="submitting"
                  >
                    {{ hasSubmitted ? '更新信息' : '保存信息' }}
                  </el-button>
                  <el-button 
                    @click="cancelEdit"
                  >
                    取消
                  </el-button>
                </el-form-item>
              </el-form>
            </el-card>
          </div>
        </div>
      </div>
    </div>

    <div class="content-tabs">
      <el-tabs v-model="activeTab" @tab-click="handleTabChange">
        <!-- <el-tab-pane label="我发布的" name="myPublish" v-loading="loading">
          <div class="infinite-list" v-infinite-scroll="loadMore" :infinite-scroll-disabled="noMorePublish">
            <div class="activity-item" v-for="(item, index) in publishList" :key="'publish-'+index" @click="goToDetail(item)">
              <div class="activity-type">{{ item.categoryName }}</div>
              <div class="activity-time">{{ item.createdAt.slice(0,10) }}</div>
              <div class="activity-content">
                <h3 class="activity-title">{{ item.name }}</h3>
                <div class="activity-detail">{{ item.description }}</div>
                <div class="activity-meta">
                  <span>已赞同 {{ item.likeCount }}</span>
                  <span>{{ item.commentCount }} 条评论</span>
                  <span>收藏 {{ item.favoriteCount }}</span>
                </div>
              </div>
            </div>
            <div v-if="noMorePublish" class="no-more">没有更多内容了</div>
          </div>
        </el-tab-pane> -->
        <el-tab-pane label="我发布的" name="myPublish" v-loading="loading">
          <div class="infinite-list" v-infinite-scroll="loadMore" :infinite-scroll-disabled="noMorePublish">
            <div
              class="activity-item"
              v-for="(item, index) in publishList"
              :key="'publish-' + index"
              @click="goToDetail(item)"
            >
              <div class="activity-header">
                <h3 class="activity-title">{{ item.name }}</h3>
                <!-- 状态标签移到右上角 -->
                <el-tag
                  class="activity-status"
                  :type="item.status === 0 ? 'warning' : item.status === 1 ? 'success' : 'danger'"
                  size="small"
                >
                  {{ item.status === 0 ? '待审核' : item.status === 1 ? '已通过' : '已拒绝' }}
                </el-tag>
              </div>
              
              <div class="activity-content">
                <!-- 分类和时间放在原来状态标签的位置 -->
                <div class="activity-meta-top">
                  <div class="activity-type">{{ item.categoryName }}</div>
                  <div class="activity-time">{{ item.createdAt.slice(0, 10) }}</div>
                </div>

                <div class="activity-detail">{{ item.description }}</div>

                <!-- 如果是拒绝，展示理由 -->
                <div v-if="item.status === 2" class="activity-reason">
                  <strong>拒绝理由：</strong>{{ item.reason || '无' }}
                </div>

                <!-- 重新编辑按钮 -->
                <el-button
                  v-if="item.status === 2"
                  type="primary"
                  size="small"
                  @click.stop="goToCreateSidejob(item.id)"
                  style="margin-top: 8px"
                >
                  重新编辑
                </el-button>

                <div class="activity-meta">
                  <span>👍 已赞同 {{ item.likeCount }}</span>
                  <span>💬 {{ item.commentCount }} 条评论</span>
                  <span>❤️ 收藏 {{ item.favoriteCount }}</span>
                </div>
              </div>
            </div>

            <div v-if="noMorePublish" class="no-more">没有更多内容了</div>
          </div>
        </el-tab-pane>

        
        <el-tab-pane label="我的团队" name="myTeams" v-loading="teamLoading">
          <div
            class="infinite-list"
            v-infinite-scroll="loadMoreTeam"
            :infinite-scroll-disabled="noMoreTeam"
          >
            <div
              class="activity-item"
              v-for="(item, index) in teamList"
              :key="'team-' + index"
              @click="goToMyMemberGroupDetail(item.projectId)"
            >
              <div class="activity-type">我的角色：{{ item.roleOfMemberGroup }}</div>
              <div class="activity-time">{{ item.createdAt?.slice(0,10) || '-' }}</div>
              <div class="activity-content">
                <h3 class="activity-title">{{ item.name }}</h3>
              </div>
            </div>
            <div v-if="noMoreTeam" class="no-more">没有更多内容了</div>
          </div>
        </el-tab-pane>


        <el-tab-pane label="我收藏的" name="myFavorites" v-loading="favoritesLoading">
          <div class="infinite-list" v-infinite-scroll="loadMoreFavorites" :infinite-scroll-disabled="noMoreFavorites">
            <div class="activity-item" v-for="(item, index) in favoritesList" :key="'favorites-'+index" @click="goToDetail(item)">
              <div class="activity-type">{{ item.categoryName }}</div>
              <div class="activity-time">{{ item.createdAt.slice(0,10) }}</div>
              <div class="activity-content">
                <h3 class="activity-title">{{ item.name }}</h3>
                <div class="activity-detail">{{ item.description }}</div>
                <div class="activity-meta">
                  <span>已赞同 {{ item.likeCount }}</span>
                  <span>{{ item.commentCount }} 条评论</span>
                  <span>收藏 {{ item.favoriteCount }}</span>
                </div>
              </div>
            </div>
            <div v-if="noMoreFavorites" class="no-more">没有更多内容了</div>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="我点赞的" name="myLike" v-loading="likeLoading">
          <div class="infinite-list" v-infinite-scroll="loadMoreLike" :infinite-scroll-disabled="noMoreLike">
            <div class="activity-item" v-for="(item, index) in likeList" :key="'like-'+index" @click="goToDetail(item)">
              <div class="activity-type">{{ item.categoryName }}</div>
              <div class="activity-time">{{ item.createdAt.slice(0,10) }}</div>
              <div class="activity-content">
                <h3 class="activity-title">{{ item.name }}</h3>
                <div class="activity-detail">{{ item.description }}</div>
                <div class="activity-meta">
                  <span>已赞同 {{ item.likeCount }}</span>
                  <span>{{ item.commentCount }} 条评论</span>
                  <span>收藏 {{ item.favoriteCount }}</span>
                </div>
              </div>
            </div>
            <div v-if="noMoreLike" class="no-more">没有更多内容了</div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <div class="sidebar">
      <div class="sidebar-section">
        <h3 class="sidebar-title">关注了</h3>
        <div class="sidebar-count">{{ followeeCount }}</div>
      </div>
      <div class="sidebar-section">
        <h3 class="sidebar-title">关注者</h3>
        <div class="sidebar-count">{{ followerCount }}</div>
      </div>
      <div class="sidebar-section">
        <h3 class="sidebar-title">赞助的 Live</h3>
        <div class="empty-placeholder">暂无内容</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, inject } from 'vue'
import { useRouter } from 'vue-router'
import { Suitcase, SuccessFilled } from '@element-plus/icons-vue'
import { post, get } from '@/net'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userInfo = inject('userInfo')

const teamList = ref([])
const teamPage = ref(1)
const teamSize = ref(10)
const teamTotal = ref(0)
const teamLoading = ref(false)
const noMoreTeam = ref(false)


// 意向表单相关状态
const showIntentSection = ref(false)
const editMode = ref(false)
const hasSubmitted = ref(false)
const submitting = ref(false)
const publishing = ref(false)


const form = ref({
  id:'',
  audience: '',
  time: '',
  skills: '',
  resources: '',
  
  status: 1
})

const loadMoreTeam = () => {
  if (!teamLoading.value && teamPage.value * teamSize.value < teamTotal.value) {
    teamPage.value++
    fetchTeamData()
  } else {
    noMoreTeam.value = true
  }
}

const fetchTeamData = async () => {
  try {
    teamLoading.value = true
    const res = await post('/api/auth/projectMember/myMemberGroups', {
      page: teamPage.value,
      size: teamSize.value
    })
    teamList.value.push(...res.records || res)  // 有些接口可能不分页
    teamTotal.value = res.total || res.length || 0
    noMoreTeam.value = teamPage.value * teamSize.value >= teamTotal.value
  } catch (err) {
    console.error('加载我的团队失败', err)
  } finally {
    teamLoading.value = false
  }
}


const goToDetail = (project) => {
  router.push({ name: 'project-detail', params: { id: project.id } })
}

const goToMyMemberGroupDetail = (projectId) => {
  console.log("project.id",projectId)
  router.push({ name: 'myMemberGroupDetail', params: { id: projectId } })
}

const goToCreateSidejob = (itemId) => {
  console.log("itemId------",itemId)
  router.push({
    name: 'createOfFindColleague',
    query: { id: itemId }
  })
}


const goToUpdateUserInfo = () => {
  router.push('/index/my/updateUserInfo')
}


const goToApplyList = () => {
  router.push('/index/my/applyList')
}

const goToAdminApplyList = () => {
  router.push('/index/my/adminApplyList')
}

const goToApplicationList = () => {
  router.push('/index/my/applicationList')
}

// 新增获取统计数据方法
const fetchMyCount = async () => {
  try {
    const res = await get('/api/auth/project/getMyCount')
    stats.value.applyCount = res.applyCount || 0
    stats.value.applicationCount = res.applicationCount || 0
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

// 加载用户意向数据
const changeShowStatus = async () => {
  try {
    const res = await get(`/api/auth/project/changeShowStatus?projectShowId=${form.value.id}&status=1`);
    if (res) {
      ElMessage.success(res || '操作成功');
      form.value.status = 1
    }
  } catch (error) {
    console.error('加载意向数据失败:', error)
  }
}

// 统计数据
const stats = ref({
  dynamicCount: 0,
  answerCount: 0,
  videoCount: 0,
  questionCount: 0,
  articleCount: 0,
  columnCount: 0,
  ideaCount: 0,
  collectionCount: 0,
  followCount: 0
})

// 加载用户意向数据
const loadIntentData = async () => {
  try {
    const res = await get('/api/auth/project/getProjectOfMyShow')
    if (res) {
      form.value = {
        id:res.id||'',
        audience: res.audience || '',
        time: res.time || '',
        skills: res.skills || '',
        resources: res.resources || '',
        status: res.status ? 1 : 0
      }
      hasSubmitted.value = true
    }
  } catch (error) {
    console.error('加载意向数据失败:', error)
  }
}

// 提交意向表单
const submitIntent = async () => {
  try {
    submitting.value = true
    
    await post('/api/auth/project/updateProjectOfMyShow', form.value)
    
    // form.value.status = 1
    hasSubmitted.value = true
    editMode.value = false
    
    ElMessage.success(form.value.status === 1 ? '已发布到广场' : '信息已保存')
  } catch (error) {
    ElMessage.error('操作失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

// 切换表单显示
const toggleIntentForm = () => {
  showIntentSection.value = !showIntentSection.value
  if (showIntentSection.value && !hasSubmitted.value) {
    editMode.value = true
  }
}

// 取消编辑
const cancelEdit = () => {
  if (hasSubmitted.value) {
    editMode.value = false
  } else {
    showIntentSection.value = false
  }
}

// 加载统计数据
// const loadStats = async () => {
//   try {
//     const res = await get('/api/auth/my/stats')
//     stats.value = res || {}
//   } catch (error) {
//     console.error('加载统计数据失败:', error)
//   }
// }

// 其余原有代码
const activeTab = ref('myPublish')
const followerCount = ref(0)
const followeeCount = ref(0)
const publishList = ref([])
const publishPage = ref(1)
const publishSize = ref(10)
const publishTotal = ref(0)
const loading = ref(false)
const noMorePublish = ref(false)
const favoritesList = ref([])
const favoritesPage = ref(1)
const favoritesSize = ref(10)
const favoritesTotal = ref(0)
const favoritesLoading = ref(false)
const noMoreFavorites = ref(false)
const likeList = ref([])
const likePage = ref(1)
const likeSize = ref(10)
const likeTotal = ref(0)
const likeLoading = ref(false)
const noMoreLike = ref(false)



const fetchFollowCount = async () => {
  try {
    const res = await post('/api/auth/my/myFollowCount')
    followerCount.value = res.followerCount || 0
    followeeCount.value = res.followeeCount || 0
  } catch (error) {
    console.error('获取关注数失败:', error)
  }
}

const loadMore = () => {
  if (!loading.value && publishPage.value * publishSize.value < publishTotal.value) {
    publishPage.value++
    fetchPublishData()
  } else {
    noMorePublish.value = true
  }
}

const loadMoreFavorites = () => {
  if (!favoritesLoading.value && favoritesPage.value * favoritesSize.value < favoritesTotal.value) {
    favoritesPage.value++
    fetchFavoritesData()
  } else {
    noMoreFavorites.value = true
  }
}

const loadMoreLike = () => {
  if (!likeLoading.value && likePage.value * likeSize.value < likeTotal.value) {
    likePage.value++
    fetchLikeData()
  } else {
    noMoreLike.value = true
  }
}

const fetchPublishData = async () => {
  try {
    loading.value = true
    const res = await post('/api/auth/my/myPublished', {
      page: publishPage.value,
      size: publishSize.value
    })
    publishList.value.push(...res.records)
    publishTotal.value = res.total
    noMorePublish.value = publishPage.value * publishSize.value >= res.total
  } finally {
    loading.value = false
  }
}

const fetchFavoritesData = async () => {
  try {
    favoritesLoading.value = true
    const res = await post('/api/auth/my/myFavorites', {
      page: favoritesPage.value,
      size: favoritesSize.value
    })
    favoritesList.value.push(...res.records)
    favoritesTotal.value = res.total
    noMoreFavorites.value = favoritesPage.value * favoritesSize.value >= res.total
  } finally {
    favoritesLoading.value = false
  }
}

const fetchLikeData = async () => {
  try {
    likeLoading.value = true
    const res = await post('/api/auth/my/myLike', {
      page: likePage.value,
      size: likeSize.value
    })
    likeList.value.push(...res.records)
    likeTotal.value = res.total
    noMoreLike.value = likePage.value * likeSize.value >= res.total
  } finally {
    likeLoading.value = false
  }
}

const handleTabChange = (tab) => {
  if (tab.paneName === 'myFavorites' && favoritesList.value.length === 0) {
    fetchFavoritesData()
  } else if (tab.paneName === 'myLike' && likeList.value.length === 0) {
    fetchLikeData()
  } else if (tab.paneName === 'myTeams' && teamList.value.length === 0) {
    fetchTeamData()
  }
}

// 初始化加载数据
onMounted(() => {
  console.log('userInfo',userInfo)
  fetchPublishData()
  fetchFollowCount()
  fetchMyCount() // 新增调用
  loadIntentData()
})
</script>

<style scoped>
.profile-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 24px;
  display: grid;
  grid-template-columns: 1fr 260px;
  gap: 32px;
  background-color: #fff;
  font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;
}

.profile-header {
  grid-column: 1 / -1;
  border-bottom: 1px solid #ebebeb;
  padding-bottom: 20px;
  margin-bottom: 24px;
}

.profile-info {
  padding: 20px 0;
}

.username {
  font-size: 26px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 4px;
}

.industry {
  color: #8590a6;
  margin-bottom: 10px;
}

.stats-container {
  grid-column: 1 / -1;
  display: flex;
  border-bottom: 1px solid #f0f2f7;
  padding-bottom: 15px;
  margin-bottom: 20px;
}

.stats-item {
  flex: 1;
  text-align: center;
  padding: 10px;
}

.stats-count {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 5px;
}

.stats-label {
  color: #8590a6;
  font-size: 14px;
}

.content-tabs {
  grid-column: 1;
}

.activity-item {
  padding: 18px 0;
  border-bottom: 1px solid #ebebeb;
  transition: all 0.3s ease; /* 添加过渡效果 */
  cursor: pointer;
  border-radius: 6px; /* 添加圆角 */
  margin: 4px 0 0 10px; /* 添加一点外边距 */
  padding: 16px; /* 调整内边距 */
}

.activity-item:hover {
  background-color: #e6e9ef;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.activity-type {
  color: #8590a6;
  font-size: 14px;
  order: 1;
}

.activity-time {
   color: #8590a6;
  font-size: 14px;
  order: 2;
}

.activity-title {
 font-size: 16px;
  font-weight: 600; /* 增加字体粗细 */
  color: #303133; /* 使用更深的颜色 */
  margin: 10px 0;
  transition: color 0.2s ease; /* 添加颜色过渡效果 */
}

.activity-detail {
  color: #808287;
  margin: 10px 0;
  font-size: 14px;
  line-height: 1.5;
}

.activity-meta {
   color: #8590a6;
  font-size: 14px;
  display: flex;
  gap: 16px;
  margin-top: 12px;
}

.activity-meta > * {
  margin-right: 15px;
}

.sidebar {
  grid-column: 2;
}

.sidebar-section {
 padding: 12px;
  border-radius: 8px;
  background-color: #f8f9fa; /* 浅灰色背景 */
  transition: all 0.2s ease;
}

.sidebar-section:hover {
  background-color: #b2bfd2; /* 悬停时背景色加深 */
}

.sidebar-title {
  font-size: 16px;
  color: #444;
  margin-bottom: 10px;
}

.sidebar-count:hover {
  color: #409EFF; /* 悬停时变为主题蓝色 */
}

.sidebar-count {
 font-size: 18px;
  font-weight: 600;
  color: #1a1a1a; /* 添加这行，使用深色字体 */
  transition: color 0.2s ease; /* 添加过渡效果 */
}

.empty-placeholder {
  color: #8590a6;
  font-size: 14px;
  padding: 10px 0;
}

.no-more {
  text-align: center;
  color: #8590a6;
  padding: 10px 0;
  font-size: 14px;
}

.infinite-list {
  max-height: 600px;
  overflow-y: auto;
}

.sidejob-entry-card {
   background-color: #f6f6f6;
  border: none;
  border-radius: 8px;
  padding: 16px;
  margin-top: 20px;
}

.sidejob-header {
  display: flex;
  align-items: center;
  font-weight: 500;
  color: #121212;
  font-size: 16px;
  margin-bottom: 10px;
}

.sidejob-header .el-icon {
  margin-right: 8px;
}

.sidejob-title {
  font-size: 20px;
}

.sidejob-description {
  font-size: 13px;
  color: #8590a6;
  margin-bottom: 14px;
}

.sidejob-buttons {
 display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

/* 新增意向表单样式 */
.intent-section {
  margin-top: 20px;
  transition: all 0.3s ease;
}

.intent-alert {
  margin-bottom: 15px;
}

.intent-card {
  background: #f9f9f9;
  border: none;
  border-radius: 8px;
  padding: 16px;
}

.intent-card h2 {
  margin-bottom: 20px;
  color: #2c3e50; /* 更深的字体色 */
  font-weight: 600;
  font-size: 18px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.intent-form
.intent-display {
  background: #fff;
  border: 1px solid #ebebeb;
  padding: 16px;
  border-radius: 6px;
}

.intent-item {
  margin-bottom: 12px;
  display: flex;
  align-items: flex-start;
  font-size: 14px;
}

.intent-label {
  font-weight: 500;
  color: #606266; /* 深灰更清晰 */
  min-width: 90px;
}

.intent-value {
  color: #303133; 
  /* color: #1a1a1a; */
  flex: 1;
}

.intent-actions {
  margin-top: 20px;
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.published-tag {
 background-color: #f0f9eb;
  border-color: #e1f3d8;
  color: #67c23a;
  font-weight: 500;
}

/* .intent-form { */
  /* padding: 10px;/ */
/* } */

/* 响应式调整 */
@media (max-width: 768px) {
  .profile-container {
    grid-template-columns: 1fr;
  }
  
  .sidebar {
    grid-column: 1;
  }
  
  .stats-container {
    flex-wrap: wrap;
  }
  
  .stats-item {
    flex: 0 0 33.33%;
  }
  
  .intent-item {
    flex-direction: column;
    gap: 4px;
  }
  
  .intent-actions {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .published-tag {
    margin-left: 0;
    margin-top: 10px;
  }
  
  .sidejob-buttons {
    flex-direction: column;
  }
}

/* 添加这些样式 */
.user-profile-wrapper {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 16px 0;
}

.user-avatar {
  border: 2px solid #f0f2f5;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.user-avatar:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}

.sidejob-btn {
  display: flex;
  align-items: center;
  gap: 8px;
}

.activity-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.intent-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.activity-meta-top {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 4px;
}


.reject-reason {
  margin-left: 10px;
  color: #f56c6c;
  font-size: 14px;
}

/* 活动项内部元素的样式调整 */
.activity-content {
  transition: all 0.3s ease;
}

.activity-status {
  margin: 8px 0;
}

.activity-reason {
  color: #f56c6c;
  font-size: 14px;
  margin: 8px 0;
}
.user-info-wrapper {
  display: flex;
  flex-direction: column;
  flex-grow: 1;
  gap: 8px;
}
.user-info-main {
  display: flex;
  flex-direction: column;
}

.username-and-badge {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.username {
  font-size: 26px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0; /* 移除默认margin */
}

.industry-badge {
  margin-left: 8px;
}

.edit-profile-btn {
  align-self: flex-start; /* 使按钮左对齐 */
  margin-top: 4px;
}

/* 响应式调整 */
@media (max-width: 600px) {
  .username-and-badge {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
  
  .username {
    font-size: 22px;
  }
  
  .user-profile-wrapper {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }
  
  .edit-profile-btn {
    align-self: center;
  }
}
</style>