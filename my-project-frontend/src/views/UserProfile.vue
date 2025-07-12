<template>
  <div class="profile-container">
    <div class="profile-header">
      <div class="profile-info">
        <div >
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
                <h1 class="username">{{ userInfo.data?.username || '用户名' }}</h1>
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
              
              <!-- 添加关注按钮 -->
              <el-button 
                v-if="!isCurrentUser && userInfo.data?.id"
                :type="isFollowing ? 'info' : 'primary'"
                size="small"
                @click="toggleFollow"
                plain
                round
                :loading="followLoading"
              >
                <el-icon><Plus /></el-icon>
                <span>{{ isFollowing ? '已关注' : '关注他' }}</span>
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="content-tabs">
      <el-tabs v-model="activeTab" @tab-click="handleTabChange">
       
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
                <!-- <div v-if="item.status === 2" class="activity-reason">
                  <strong>拒绝理由：</strong>{{ item.reason || '无' }}
                </div> -->
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
       <div class="sidejob-entry-card">
          <div class="sidejob-header">
            <el-icon size="22"><Suitcase /></el-icon>
            <span class="sidejob-title">开启副业，赚外快</span>
          </div>
          <div class="sidejob-description">选择你的路径：发起副业项目，或加入有趣团队</div>
          <div class="sidejob-buttons">
            <!-- 找团队 - 使用握手符号表示合作，改为info天蓝色 -->
            <el-button type="info" size="large" @click="toggleIntentForm">
              👥 寻找团队
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
                    <span class="intent-value">{{ form.timePerDay || '未填写' }}</span>
                  </div>
                  <div class="intent-item">
                    <span class="intent-label">个人技能：</span>
                    <span class="intent-value">{{ form.skills || '未填写' }}</span>
                  </div>
                  <div class="intent-item">
                    <span class="intent-label">我能提供：</span>
                    <span class="intent-value">{{ form.resources || '未填写' }}</span>
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
               
              </el-form>
            </el-card>
          </div>
        </div>

        <div class="sidebar-info-section">
          <div class="sidebar-follow-row">
            <div class="sidebar-section" @click="openFolloweeDrawer" style="cursor: pointer;">
              <h3 class="sidebar-title">我关注的</h3>
              <div class="sidebar-count">{{ followerCount }}</div>
            </div>
            <div class="sidebar-section">
              <h3 class="sidebar-title">关注我的</h3>
              <div class="sidebar-count">{{ followeeCount }}</div>
            </div>
          </div>
          <div class="sidebar-section">
            <h3 class="sidebar-title">赞助的 Live</h3>
            <div class="empty-placeholder">暂无内容</div>
          </div>
        </div>
      </div>
  </div>

  <el-drawer
  v-model="followeeListVisible"
  title="我关注的用户"
  size="480px"
  direction="rtl"
  :with-header="true"
>
  <el-scrollbar height="600px" @scroll="handleFolloweeScroll">
    <div
      class="followee-user-card"
      v-for="user in followeeList"
      :key="user.id"
    >
      <el-avatar :src="user.avatarUrl" :size="48" />
      <div class="followee-user-info">
        <div class="followee-username">{{ user.username }}</div>
        <div class="followee-industry">{{ user.industryName || '未设置行业' }}</div>
        <div class="followee-actions">
          <el-button
            size="small"
            type="primary"
            @click="goToUserProfile(user.secrecyId)"
            plain
          >
            访问主页
          </el-button>
          <el-button
            v-if="user.needFollow"
            size="small"
            type="success"
            @click="doFollowBack(user)"
            plain
          >
            回关
          </el-button>
        </div>
      </div>
    </div>
    <div v-if="followeeLoading" class="followee-loading">加载中...</div>
    <div v-if="followeeFinished" class="no-more">没有更多了</div>
  </el-scrollbar>
</el-drawer>
</template>

<script setup>




import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Suitcase, SuccessFilled, Plus } from '@element-plus/icons-vue'
import { post, get } from '@/net'
import { ElMessage } from 'element-plus'
import useUserInfo from '@/hooks/useUserInfo';


const { state: currentUserInfo, loadUserInfo } = useUserInfo();
const route = useRoute()
const router = useRouter()
const teamList = ref([])
const teamPage = ref(1)
const teamSize = ref(10)
const teamTotal = ref(0)
const teamLoading = ref(false)
const noMoreTeam = ref(false)
const userInfo = ref({
  data: null
})


// 意向表单相关状态
const showIntentSection = ref(false)
const editMode = ref(false)
const hasSubmitted = ref(false)
const submitting = ref(false)
const publishing = ref(false)

// 添加关注相关状态
const isFollowing = ref(false)
const followLoading = ref(false)

// 用户ID从路由参数获取
const secrecyId = ref(route.params.id)
const isCurrentUser =  ref(false)

const followeeListVisible = ref(false)
const followeeList = ref([])
const followeePage = ref(1)
const followeeSize = ref(10)
const followeeTotal = ref(0)
const followeeLoading = ref(false)
const followeeFinished = ref(false)


onMounted(() => {
  // fetchUserInfo()
  if (!currentUserInfo.data.id) {
    loadUserInfo().then(() => {
      fetchUserInfo()
      isCurrentUser.value = String(currentUserInfo.data.secrecyId) === String(secrecyId.value)
      fetchPublishData()
      fetchFollowCount()
      loadIntentData()
      // checkFollowStatus()
    });
  } else {
     fetchUserInfo();
    isCurrentUser.value = String(currentUserInfo.data.secrecyId) === String(secrecyId.value)
    fetchPublishData()
    fetchFollowCount()
    loadIntentData()
    // checkFollowStatus()
  }

});

const openFolloweeDrawer = () => {
  followeeListVisible.value = true
  if (followeeList.value.length === 0) {
    loadFolloweeList()
  }
}

const loadFolloweeList = async () => {
  if (followeeLoading.value || followeeFinished.value) return
  followeeLoading.value = true
  try {
    const res = await post('/api/auth/my/myFollowees', {
      secrecyId: secrecyId.value,
      page: followeePage.value,
      size: followeeSize.value
    })
    const records = res.records || res
    followeeList.value.push(...records)
    followeeTotal.value = res.total || records.length || 0
    followeeFinished.value = followeePage.value * followeeSize.value >= followeeTotal.value
    followeePage.value++
  } catch (err) {
    console.error('加载关注用户失败', err)
  } finally {
    followeeLoading.value = false
  }
}

const handleFolloweeScroll = (e) => {
  // const { scrollTop, scrollHeight, clientHeight } = e.target
  // if (scrollTop + clientHeight >= scrollHeight - 50) {
  //   loadFolloweeList()
  // }
  loadFolloweeList()
}

const goToUserProfile = (secrecyId) => {
  router.push({ name: 'user-profile', params: { id: secrecyId } })
}

const doFollowBack = async (user) => {
  try {
    await post('/api/auth/project/concernPublisher', {
      followeeId: user.userId
    })
    ElMessage.success(`已回关 ${user.username}`)
    user.needFollow = false
  } catch (error) {
    console.error('回关失败:', error)
    ElMessage.error('回关失败')
  }
}

const form = ref({
  id:'',
  audience: '',
  timePerDay: '',
  skills: '',
  resources: '',
  
  status: 1
})

// 检查关注状态
const checkFollowStatus = async () => {
  console.log("关注状态",userInfo)
  if (!userInfo.value.data?.id ) return
  try {
    const res = await get(`/api/auth/my/isFollewer?followeeId=${userInfo.value.data.id}`)
    console.log("关注状态",res)
    isFollowing.value = !res // 接口返回true表示未关注，false表示已关注
  } catch (error) {
    console.error('检查关注状态失败:', error)
  }
}

// 切换关注状态
const toggleFollow = async () => {
  if (!userInfo.value.data?.id || followLoading.value) return
  
  followLoading.value = true
  try {
    if (isFollowing.value) {
      // 取消关注
      await post('/api/auth/project/concernPublisherCancel', {
        followeeId: userInfo.value.data.id
      })
      ElMessage.success('已取消关注')
    } else {
      // 关注
      await post('/api/auth/project/concernPublisher', {
        followeeId: userInfo.value.data.id
      })
      ElMessage.success('关注成功')
    }
    isFollowing.value = !isFollowing.value
    // 更新关注数
    await fetchFollowCount()
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败，请稍后重试')
  } finally {
    followLoading.value = false
  }
}

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
        secrecyId:secrecyId.value,
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
  router.push({ name: 'myMemberGroupDetail', params: { id: projectId } })
}



//加载用户名称和头像
const fetchUserInfo = async () => {
  try {
    const res = await get(`/api/auth/user/getSecrecyIdUserInfo?secrecyId=${secrecyId.value}`);
    userInfo.value = { data: res }

    checkFollowStatus();
  } catch (error) {
    console.error('获取用户信息失败:', error)
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
    const res = await get(`/api/auth/project/getProjectOfMyShow?secrecyId=${secrecyId.value}`);
    if (res) {
      form.value = {
        id:res.id||'',
        audience: res.audience || '',
        timePerDay: res.timePerDay || '',
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



// 切换表单显示
const toggleIntentForm = () => {
  showIntentSection.value = !showIntentSection.value
  if (showIntentSection.value && !hasSubmitted.value) {
    editMode.value = true
  }
}

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
    const res = await get(`/api/auth/my/myFollowCount?secrecyId=${secrecyId.value}`);
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
      secrecyId:secrecyId.value,
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
        secrecyId:secrecyId.value,
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
        secrecyId:secrecyId.value,
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




</script>

<style scoped>
.profile-container {
  width: 100%;
  margin: 0 auto;
  padding: 24px 40px;
  /* padding-left: 60px; */
  padding-right: 60px;
  display: grid;
  grid-template-columns: 1fr 400px;
  gap: 40px;
  background-color: #fff;
  font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;
}

.profile-header {
  grid-column: 1 / -1;
  border-bottom: 1px solid #ebebeb;
  margin-bottom: 6px;
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
  padding: 14px 0;
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
  padding-top: 12px;
  padding-right: 12px;
  margin-right: 10%;
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
  border: 1px solid #e0e0e0; /* 增加边界线 */
  border-radius: 8px;
  padding: 16px;
  margin-top: 20px;
}

.sidebar-info-section {
  background-color: #f6f6f6;
  border: 1px solid #e0e0e0; /* 增加边界线 */
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
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-top: 12px;
  justify-items: start; /* 新增：让网格项左对齐 */
}

.sidejob-buttons .el-button {
  width: 80%; /* 宽度填满网格单元格 */
  height: 48px; /* 固定高度 */
  padding: 0 8px; /* 内边距 */
  font-size: 13px; /* 统一字体大小 */
  white-space: normal; /* 允许文字换行 */
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1.4; /* 行高 */
}

/* 移动端适配 */
@media (max-width: 768px) {
  .sidejob-buttons {
    grid-template-columns: 1fr;
  }
  
  .sidejob-buttons .el-button {
    height: 44px; /* 移动端稍小一点 */
  }
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
    grid-template-columns: 1fr;
  }
}

/* 添加这些样式 */
.user-profile-wrapper {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 8px 0;
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
.sidebar-follow-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}

.sidebar-follow-row .sidebar-section {
  flex: 1;
  background-color: #f8f9fa;
  padding: 12px;
  border-radius: 8px;
  text-align: center;
}
.user-info-wrapper .el-button {
  margin-top: 8px;
  align-self: flex-start;
}

/* 响应式调整 */
@media (max-width: 600px) {
  .user-info-wrapper .el-button {
    align-self: center;
  }
}


.followee-user-card {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 12px;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.2s ease;
}
.followee-user-card:hover {
  background-color: #f5f7fa;
}
.followee-user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.followee-username {
  font-weight: 600;
  font-size: 16px;
  color: #303133;
}
.followee-industry {
  font-size: 13px;
  color: #909399;
}
.followee-actions {
  margin-top: 8px;
  display: flex;
  gap: 10px;
}
.followee-loading {
  text-align: center;
  padding: 10px;
  font-size: 14px;
  color: #999;
}
</style>