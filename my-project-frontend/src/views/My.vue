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
                <el-tag
                  class="activity-status"
                  :type="getStatusTagType(item.status)"
                  size="small"
                >
                  {{ getStatusText(item.status) }}
                </el-tag>
              </div>
              
              <div class="activity-content">
                <!-- 分类和时间放在原来状态标签的位置 -->
                <div class="activity-meta-top">
                  <div class="activity-type">{{ item.firstCategoryName }}</div>
                  <div class="activity-type">{{ item.secondCategoryName }}</div>
                  <div class="activity-time">{{ item.createdAt.slice(0, 10) }}</div>
                </div>

                <div class="activity-detail">{{ item.description }}</div>

                <!-- 如果是拒绝，展示理由 -->
                <div v-if="item.status === 5" class="activity-reason">
                  <strong>拒绝理由：</strong>{{ item.reason || '无' }}
                </div>

                <!-- 重新编辑按钮 -->
                <el-button
                  v-if="item.status === 5"
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
              <div class="activity-type">{{ item.firstCategoryName }}</div>
              <div class="activity-type">{{ item.secondCategoryName }}</div>
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
              <div class="activity-type">{{ item.firstCategoryName }}</div>
              <div class="activity-type">{{ item.secondCategoryName }}</div>
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
            <!-- 发起副业 - 使用火箭表示开始新事物，保持primary蓝色 -->
            <el-button type="primary" size="large" @click="goToCreateSidejob">
              🚀 发起副业
            </el-button>
            
            <!-- 找团队 - 使用握手符号表示合作，改为info天蓝色 -->
            <el-button type="info" size="large" @click="toggleIntentForm">
              👥 寻找团队
            </el-button>

            <!-- 审核 - 使用警徽表示审核权限，使用warning黄色 -->
            <el-button type="warning" size="large" @click="goToApplyList" style="margin-left: 0px;">
              🛡️ 我审核的（{{ stats.applyCount || 0 }}）
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
                    <span class="intent-value">{{ form.audienceDesc || '未填写' }}</span>
                  </div>
                  <div class="intent-item">
                    <span class="intent-label">可投入时间：</span>
                    <span class="intent-value">{{ form.time ? `${form.time}小时/天` : '未填写' }}</span>
                  </div>
                  <div class="intent-item">
                    <span class="intent-label">个人技能：</span>
                    <span class="intent-value">{{ form.skillNames || '未填写' }}</span>
                      <!-- {{ getSkillNames(form.skills) || '未填写' }} -->
                        <!-- <span class="intent-value">{{ getSkillNames(form.skills) }}</span> -->
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
                  <el-select
                    v-model="form.audience" 
                    placeholder="请选择身份"
                    clearable
                    style="width: 100%"
                  >
                    <el-option
                      v-for="type in userTypes"
                      :key="type.code"
                      :label="type.desc"
                      :value="type.code"
                    />
                  </el-select>
                </el-form-item>

                <el-form-item label="可投入时间">
                        <el-select
                          v-model="form.time" 
                          placeholder="请选择可投入时间"
                          clearable
                          style="max-width: 100%; width: 100%;"
                        >
                          <el-option
                            v-for="hour in availableHours"
                            :key="hour"
                            :label="`${hour}小时/天`"
                            :value="hour"
                          />
                        </el-select>
                  </el-form-item>
                  
               <el-form-item label="个人技能">
                <el-select
                  v-model="form.skills"
                  placeholder="选择技能分类"
                  multiple
                  filterable
                  collapse-tags
                  style="width: 100%"
                >
                  <el-option-group
                    v-for="group in skillCategories"
                    :key="group.code"
                    :label="group.desc"
                  >
                    <!-- 一级分类选项 -->
                    <el-option
                      :label="group.desc"
                      :value="group.code"
                    />
                    <!-- 二级分类选项 -->
                    <el-option
                      v-for="sub in group.subs"
                      :key="sub.code"
                      :label="sub.desc"
                      :value="sub.code"
                    />
                  </el-option-group>
                </el-select>
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

       <div class="sidebar-info-section">
          <div class="sidebar-follow-row">
            <div class="sidebar-section" @click="openFolloweeDrawer" style="cursor: pointer;">
              <h3 class="sidebar-title">我关注的</h3>
              <div class="sidebar-count">{{ followerCount }}</div>
            </div>
            <div class="sidebar-section" @click="openFollowerDrawer" style="cursor: pointer;">
              <h3 class="sidebar-title">关注我的</h3>
              <div class="sidebar-count">{{ followeeCount }}</div>
            </div>
          </div>

          <div class="sidebar-follow-row">
             <div class="sidebar-section" @click="openFollowedBarsDrawer" style="cursor: pointer;">
                <h3 class="sidebar-title">我关注的圈子</h3>
                <div class="sidebar-count">{{ followedBarsTotalCount }}</div>
              </div>
              <div class="sidebar-section" @click="openPointsDrawer" style="cursor: pointer;">
                <h3 class="sidebar-title">我的积分</h3>
                <div class="sidebar-count">{{ totalPoints }}</div>
              </div>
          </div>
         
        </div>

      </div>
  </div>

<!-- 我的积分 drawer -->
<el-drawer
  v-model="pointsDrawerVisible"
  title="我的积分"
  size="480px"
  direction="rtl"
  :with-header="true"
  class="custom-drawer"
>
  <el-scrollbar height="600px">
    <!-- 积分概览 - 固定不动 -->
    <div class="points-overview">
      <div class="points-total">
        <span class="points-value">{{ totalPoints }}</span>
        <span class="points-label">当前积分</span>
      </div>
      
      <div class="points-stats">
        <div class="points-stat-item">
          <span class="stat-label">今日获得</span>
          <span class="stat-value">{{ todayPoints }}</span>
        </div>
        <div class="points-stat-item">
          <span class="stat-label">本月获得</span>
          <span class="stat-value">{{ monthPoints }}</span>
        </div>
        <!-- 修改积分概览中的徽章展示部分 -->
        <div class="points-stat-item" v-if="badges.length > 0">
          <span class="stat-label">我的勋章</span>
          <div class="badges-container">
            <div 
              v-for="(badge, index) in badges" 
              :key="index"
              class="badge-item"
              :style="{ '--badge-color': badge.colorCode || '#FFD700' }"
            >
              <div class="badge-icon">
                <img 
                  v-if="badge.iconUrl" 
                  :src="badge.iconUrl" 
                  :alt="badge.name"
                  class="badge-image"
                />
                <el-icon v-else><Trophy /></el-icon>
              </div>
              <div class="badge-tooltip">
                <div class="tooltip-name">{{ badge.name }}</div>
                <div class="tooltip-desc">{{ badge.description }}</div>
              </div>
            </div>
          </div>
        </div>

        <div class="points-stat-item" v-else>
          <span class="stat-label">我的勋章</span>
          <span class="stat-value">暂无勋章</span>
        </div>
      </div>
    </div>

    <div class="points-actions">
      <el-button type="primary" plain @click="showPointsHistory">查看积分记录</el-button>
      <el-button type="success" plain @click="showPointsShop">积分兑换</el-button>
    </div>

    <!-- 积分任务 - 可滚动区域 -->
    <div class="points-tasks">
      <h3 class="tasks-title">积分任务</h3>
      
      <!-- 添加加载动画 -->
      <div v-if="pointsTaskLoading" class="task-loading">
        <el-skeleton :rows="3" animated />
      </div>
      
      <transition-group 
        name="task-list" 
        tag="div" 
        class="task-list"
        v-if="!pointsTaskLoading"
      >
        <div 
          class="task-item" 
          v-for="task in pointsTasks" 
          :key="task.id"
        >
          <div class="task-icon" :style="{ backgroundColor: task.color }">
            <el-icon :size="20"><component :is="getIconComponent(task.icon)" /></el-icon>
          </div>
          <div class="task-content">
            <div class="task-header">
              <h4 class="task-name">{{ task.name }}</h4>
              <span class="task-points">{{ task.points }}</span>
            </div>
            <p class="task-desc">{{ task.description }}</p>
            <el-progress 
              :percentage="calculateTaskProgress(task)" 
              :stroke-width="8"
              :color="task.color || '#409EFF'"
              :show-text="false"
            />
            

            <div class="task-meta">
              <span>{{ task.progress }}/{{ task.target }}</span>
              <el-button 
                :type="getTaskButtonType(task)"
                size="small" 
                :disabled="task.statusOfUserTask === 0 || task.statusOfUserTask === 1 || task.rewardClaimed === 1"
                @click="handleTaskReward(task)"
                :loading="task.loading"
              >
                {{ getTaskButtonText(task) }}
              </el-button>
            </div>


          </div>
        </div>
      </transition-group>

      <!-- 分页控件 -->
      <div class="pagination-container">
        <el-pagination
          small
          layout="prev, pager, next"
          :total="pointsTaskTotal"
          :page-size="pointsTaskSize"
          :current-page="pointsTaskPage"
          @current-change="handlePointsTaskPageChange"
          :disabled="pointsTaskLoading"
          class="custom-pagination"
        />
      </div>
    </div>
  </el-scrollbar>
</el-drawer>

  <!-- 我关注的圈子 drawer -->
<el-drawer
  v-model="followedBarsDrawerVisible"
  title="我关注的圈子"
  size="480px"
  direction="rtl"
  :with-header="true"
  class="custom-drawer"
>
  <el-scrollbar height="600px">
    <div
      class="followed-bar-card"
      v-for="bar in followedBarsList"
      :key="bar.id"
      @click="navigateToBar(bar.id)"
    >
      <el-avatar :src="bar.avatar" :size="48" shape="square" />
      <div class="followed-bar-info">
        <div class="followed-bar-name">{{ bar.name }}</div>
        <div class="followed-bar-stats">
          <span class="stat-item">
             <i class="stat-icon">👥</i>
            <span>{{ bar.followerCount | formatNumber }}</span>
          </span>
          <span class="stat-item">
            <i class="stat-icon">📝</i>
            <span>{{ bar.postCount | formatNumber }}</span>
          </span>
        </div>
        <div class="followed-bar-unread" v-if="bar.unread > 0">
          <el-badge :value="bar.unread" />
        </div>
      </div>
    </div>
    <div v-if="followedBarsLoading" class="followed-bar-loading">加载中...</div>
    <div v-if="!followedBarsLoading && followedBarsList.length === 0" class="no-more">
      暂无关注的圈子
    </div>
  </el-scrollbar>
  
  <!-- 分页控件 -->
  <div class="pagination-container">
    <el-pagination
      small
      layout="prev, pager, next"
      :total="followedBarsTotal"
      :page-size="followedBarsSize"
      v-model:current-page="followedBarsPage"
      @current-change="handleFollowedBarsPageChange"
      class="custom-pagination"
    />
  </div>
</el-drawer>

  <el-drawer
    v-model="followeeListVisible"
    title="我关注的用户"
    size="480px"
    direction="rtl"
    :with-header="true"
    class="custom-drawer"
  >
    <el-scrollbar height="600px">
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
            v-if="user.needFollow !== undefined && user.needFollow !== null"
              size="small"
              :type="user.needFollow ? 'success' : 'info'"
              @click="handleFollowAction(user)"
              plain
              :loading="user.loading"
            >
              {{ user.needFollow ? '关注他' : '已关注' }}
            </el-button>
          </div>
        </div>
      </div>
      <div v-if="followeeLoading" class="followee-loading">加载中...</div>
      <div v-if="!followeeLoading && followeeList.length === 0" class="no-more">暂无关注用户</div>
    </el-scrollbar>
    
    <!-- 分页控件 -->
    <div class="pagination-container">
      <el-pagination
        small
        layout="prev, pager, next"
        :total="followeeTotal"
        :page-size="followeeSize"
        v-model:current-page="followeePage"
        @current-change="handlePageChange"
      />
    </div>
  </el-drawer>

  <!-- 关注我的用户 drawer -->
  <el-drawer
    v-model="followerListVisible"
    title="关注我的用户"
    size="480px"
    direction="rtl"
    :with-header="true"
    class="custom-drawer"
  >
    <el-scrollbar height="600px">
      <div
        class="followee-user-card"
        v-for="user in followerList"
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
              v-if="user.needFollow !== undefined && user.needFollow !== null"
              size="small"
              :type="user.needFollow ? 'success' : 'info'"
              @click="handleFollowAction(user)"
              plain
              :loading="user.loading"
            >
              {{ user.needFollow ? '关注他' : '已关注' }}
            </el-button>
          </div>
        </div>
      </div>
      <div v-if="followerLoading" class="followee-loading">加载中...</div>
      <div v-if="!followerLoading && followerList.length === 0" class="no-more">暂无关注用户</div>
    </el-scrollbar>
    
    <div class="pagination-container">
      <el-pagination
        small
        layout="prev, pager, next"
        :total="followerTotal"
        :page-size="followerSize"
        v-model:current-page="followerPage"
        @current-change="handleFollowerPageChange"
        class="custom-pagination"
      />
    </div>
  </el-drawer>
</template>

<script setup>
import { ref, onMounted ,nextTick} from 'vue'
import { useRouter } from 'vue-router'
import * as icons from '@element-plus/icons-vue'
import { Suitcase, SuccessFilled, Edit,
  Calendar,
  Star,
  ChatDotRound,
  UserFilled,
  Notebook,
ChromeFilled,  
 User,
  Clock,
  Trophy,
  QuestionFilled } from '@element-plus/icons-vue'
import { post, get } from '@/net'
import { ElMessage } from 'element-plus'
import useUserInfo from '@/hooks/useUserInfo';
const { state: userInfo, loadUserInfo } = useUserInfo();



const availableHours = ref([1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12])



const router = useRouter()
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

// 关注弹窗相关状态
const followDialogVisible = ref(false)
const currentFollowTab = ref('following') // 当前显示的标签页
const followDialogTitle = ref('') // 对话框标题
const followingList = ref([]) // 我关注的列表
const followersList = ref([]) // 关注我的列表
const loadingFollowing = ref(false)
const loadingFollowers = ref(false)
const noMoreFollowing = ref(false)
const noMoreFollowers = ref(false)
const followingPage = ref(1)
const followersPage = ref(1)
const pageSize = ref(10)



// 我关注的用户相关状态
const followeeListVisible = ref(false)
const followeeList = ref([])
const followeePage = ref(1)
const followeeSize = ref(10)
const followeeTotal = ref(0)
const followeeLoading = ref(false)
const followeeFinished = ref(false)

// 关注我的用户相关状态
const followerListVisible = ref(false)
const followerList = ref([])
const followerPage = ref(1)
const followerSize = ref(10)
const followerTotal = ref(0)
const followerLoading = ref(false)
const followerFinished = ref(false)
const skillCategories = ref([])
const tagsOptions = ref([])
const userTypes = ref([])

// 我关注的圈子相关状态
const followedBars = ref([])
const followedBarsDrawerVisible = ref(false)
const followedBarsList = ref([])
const followedBarsPage = ref(1)
const followedBarsSize = ref(10)
const followedBarsTotal = ref(0)
const followedBarsLoading = ref(false)
const followedBarsTotalCount = ref(0)


// 积分相关状态
const pointsDrawerVisible = ref(false)
const pointsLoading = ref(false)
const pointsPage = ref(1)
const pointsSize = ref(10)
const pointsTotal = ref(0)

// 积分数据
const totalPoints = ref(0)
const todayPoints = ref(0)
const monthPoints = ref(0)
const historyPoints = ref(0)

// 积分任务相关状态
const pointsTasks = ref([])
const pointsTaskPage = ref(1)
const pointsTaskSize = ref(5)
const pointsTaskTotal = ref(0)
const pointsTaskLoading = ref(false)
const badges = ref([])



const fetchPointsInfo = async () => {
  try {
    const res = await get('/api/auth/achievement/getMyAchievementBaseInfo')
    if (res) {
      totalPoints.value = res.points || 0
      todayPoints.value = res.todayPoints || 0
      monthPoints.value = res.monthPoints || 0
      // Set badges if available
      badges.value = res.badges || []
    }
  } catch (error) {
    console.error('获取积分信息失败:', error)
  }
}


const getTaskButtonText = (task) => {
  if (task.statusOfUserTask === 0 || task.statusOfUserTask === 1) {
    return '进行中';
  } else if (task.statusOfUserTask === 2) {
    return task.rewardClaimed === 0 ? '领取' : '已完成';
  }
  return '去完成';
};

const handleTaskReward = async (task) => {
  try {
    if (task.statusOfUserTask !== 2 || task.rewardClaimed !== 0) return;
    
    task.loading = true;
    const res = await get(`/api/auth/achievement/receiverTaskReward?taskId=${task.id}`);
    console.log("task",task)
    if (res) {
      // 更新任务状态
      task.rewardClaimed = 1;
      ElMessage.success(
        `成功领取 ${task.points} ` 
      );
      
      // 如果是积分奖励，更新总积分
      if (task.rewardType === 1) {
        totalPoints.value += Number(task.rewardValue) || 0;
      }
      
      // 如果是勋章奖励，更新勋章列表
      if (task.rewardType === 2) {
        badges.value = [...badges.value, task.rewardValue];
      }
    }
  } catch (error) {
    console.error('领取奖励失败:', error);
    ElMessage.error('领取奖励失败');
  } finally {
    task.loading = false;
  }
};

const getTaskButtonType = (task) => {
  if (task.statusOfUserTask === 2 && task.rewardClaimed === 0) {
    return 'success'; // 可领取状态显示绿色
  } else if (task.rewardClaimed === 1) {
    return 'info';    // 已完成状态显示蓝色
  }
  return 'primary';   // 默认状态
};

// 获取积分任务数据
// 获取积分任务数据（带平滑滚动效果）
const fetchPointsTasks = async () => {
  try {
    pointsTaskLoading.value = true
    
    const res = await post('/api/auth/achievement/myAchievementPageList', {
      page: pointsTaskPage.value,
      size: pointsTaskSize.value
    })
    
    if (res && res.records) {
      // 清空当前数据，加载新页数据
      pointsTasks.value = res.records.map(task => ({
        id: task.taskId,
        name: task.name,
        description: task.description,
        points: task.rewardValue || 0,
        progress: task.myTargetValue || 0,
        target: task.targetValue || 1,
        completed: (task.myTargetValue || 0) >= (task.targetValue || 1),
        color: task.color || '#409EFF', // 添加默认颜色
        icon: task.icon,
        type: task.typeName,
        category: task.categoryName,
        rewardType: task.rewardTypeName,
        startTime: task.startTime  || '',
        endTime: task.endTime  || '',
        sortOrder: task.sortOrder || 0,
        statusOfUserTask: task.statusOfUserTask,
        rewardClaimed: task.rewardClaimed,
        rewardTypeName: task.rewardTypeName,
      }))
      
      pointsTaskTotal.value = Number(res.total) || 0
    }
  } catch (error) {
    console.error('获取积分任务失败:', error)
    ElMessage.error('获取积分任务失败')
  } finally {
    pointsTaskLoading.value = false
  }
}


// 添加这个方法
const getIconComponent = (iconName) => {
  const iconMap = {
    'Notebook': Notebook,
    'Calendar': Calendar,
    'Star': Star,
    'ChatDotRound': ChatDotRound,
    'UserFilled': UserFilled,
    'ChromeFilled': ChromeFilled,
    'User': User,
    'Clock': Clock,
    'Trophy': Trophy,
    'QuestionFilled': QuestionFilled,
    'Edit': Edit
    // 添加其他图标映射
  }
  return iconMap[iconName] || QuestionFilled // 默认图标
}

// 完成任务
const completeTask = async (task) => {
  try {
    if (task.completed) return
    
    // 调用完成任务API
    const res = await post('/api/auth/achievement/completeTask', {
      taskId: task.id
    })
    
    if (res) {
      task.progress++
      if (task.progress >= task.target) {
        task.completed = true
      }
      ElMessage.success(`完成任务，获得${task.points}积分`)
      
      // 更新积分总数
      totalPoints.value += task.points
    }
  } catch (error) {
    console.error('完成任务失败:', error)
    ElMessage.error('完成任务失败')
  }
}

// 分页变化
// 分页变化处理（带平滑滚动）
// 分页变化处理
const handlePointsTaskPageChange = async (page) => {
  pointsTaskPage.value = page
  await fetchPointsTasks()
  
  // 如果需要滚动回顶部，可以这样实现：
  nextTick(() => {
    const scrollContainer = document.querySelector('.el-scrollbar__wrap')
    if (scrollContainer) {
      scrollContainer.scrollTo({
        top: 0,
        behavior: 'smooth'
      })
    }
  })
}
//////

// 积分规则
const pointsRules = ref([
  { id: 1, name: '每日签到', points: 10 },
  { id: 2, name: '发布内容', points: 20 },
  { id: 3, name: '点赞内容', points: 5 },
  { id: 4, name: '发表评论', points: 15 },
  { id: 5, name: '内容被收藏', points: 10 },
  { id: 6, name: '完成成就', points: '50-200' }
])

// 打开积分抽屉
const openPointsDrawer = () => {
  pointsDrawerVisible.value = true
  pointsPage.value = 1
   fetchPointsInfo() // Replace fetchPointsTasks() with this
  fetchPointsTasks()
}


// 计算任务进度
const calculateTaskProgress = (task) => {
  return Math.min(100, (task.progress / task.target) * 100)
}

// 查看积分历史
const showPointsHistory = () => {
  router.push('/points/history')
}

// 查看积分商城
const showPointsShop = () => {
  router.push('/points/shop')
}


onMounted(() => {
  console.log(Object.keys(icons)) // 查看所有可用图标名称

  fetchSkillCategories().then(() =>{
    if (!userInfo.data.id) {
      loadUserInfo().then(() => {
        fetchPublishData()
        fetchFollowCount()
        fetchMyCount()
        loadIntentData()
        fetchUserTypes()
          fetchFollowedBars()
      });
    } else {
      fetchPublishData()
      fetchFollowCount()
      fetchMyCount()
      loadIntentData()
      fetchUserTypes()
        fetchFollowedBars()
    }
  })
  
});

const getStatusText = (status) => {
  const statusMap = {
    0: '待审核',
    5: '已退回', 
    10: '招募中',
    11: '已满员',
    12: '已关闭'
  }
  return statusMap[status] || '未知状态'
}

// 状态标签类型映射  
const getStatusTagType = (status) => {
  const typeMap = {
    0: 'warning',  // 待审核-黄色
    5: 'danger',   // 已退回-红色
    10: 'success', // 招募中-绿色
    11: 'info',    // 已满员-蓝色
    12: 'danger'   // 已关闭-红色
  }
  return typeMap[status] || ''
}


// 获取我关注的圈子
const fetchFollowedBars = async (page = 1, size = 5) => {
  try {
    if(!userInfo.data.id) {
      console.log("未登录，不获取我关注的圈子")
      return
    }
    
    followedBarsLoading.value = true
    const response = await post('/api/auth/quan/myFavoriteBar', {
      page: page,
      size: size
    })
    
    if (response && response.records) {
      if (size === 5) {
        // 主列表数据
        followedBars.value = response.records.map(bar => ({
          id: bar.id,
          name: bar.name,
          avatar: bar.avatar,
          followerCount: bar.followerCount,
          postCount: bar.postCount,
          unread: 0 // 初始未读消息数为0
        }))
      } else {
        // 弹窗数据
        followedBarsList.value = response.records.map(bar => ({
          id: bar.id,
          name: bar.name,
          avatar: bar.avatar,
          followerCount: bar.followerCount,
          postCount: bar.postCount,
          unread: 0 // 初始未读消息数为0
        }))
        followedBarsTotal.value = response.total || 0
      }
      // 更新总数
      followedBarsTotalCount.value = response?.total || response?.records?.length || 0
    }
  } catch (error) {
    console.error('获取关注的圈子失败:', error)
    ElMessage.error('获取关注的圈子失败，请稍后重试')
  } finally {
    followedBarsLoading.value = false
  }
}

// 打开我关注的圈子抽屉
const openFollowedBarsDrawer = () => {
  followedBarsDrawerVisible.value = true
  followedBarsPage.value = 1
  fetchFollowedBars(1, followedBarsSize.value)
}

// 分页变化
const handleFollowedBarsPageChange = (page) => {
  followedBarsPage.value = page
  fetchFollowedBars(page, followedBarsSize.value)
}

// 添加获取技能分类的方法
const fetchSkillCategories = async () => {
  try {
    const res = await get('/api/unauth/common/category')
    skillCategories.value = res
  } catch (error) {
    console.error('获取技能分类失败:', error)
    ElMessage.error('获取技能分类失败')
  }
}

const fetchUserTypes = async () => {
  try {
    const res = await get('/api/unauth/common/getUserType')
    userTypes.value = res
  } catch (error) {
    console.error('获取用户类型失败:', error)
    ElMessage.error('获取用户类型失败')
  }
}

// 添加获取技能名称的方法
const getSkillNames = (skillCodes) => {
  if (!skillCodes || (Array.isArray(skillCodes) && skillCodes.length === 0) || (typeof skillCodes === 'string' && skillCodes.trim() === '')) {
    return '未填写'
  }
  
  // 统一处理为数组格式
  const codes = Array.isArray(skillCodes) ? skillCodes : skillCodes.split(',').filter(Boolean)
  
  const names = []
  skillCategories.value.forEach(group => {
    // 检查一级分类
    if (codes.includes(String(group.code))) {
      names.push(group.desc)
    }
    
    // 检查二级分类
    if (group.subs) {
      group.subs.forEach(sub => {
        if (codes.includes(String(sub.code))) {
          names.push(sub.desc)
        }
      })
    }
  })
  
  return names.length ? names.join('、') : '未填写'
}


// 打开我关注的用户抽屉
const openFolloweeDrawer = () => {
  followeeListVisible.value = true
  loadFolloweeList()
}

// 打开关注我的用户抽屉
const openFollowerDrawer = () => {
  followerListVisible.value = true
  loadFollowerList()
}

// 我关注的用户分页变化
const handlePageChange = (page) => {
  followeePage.value = page
  loadFolloweeList()
}

const goToUserProfile = (secrecyId) => {
   window.open(`/index/user/${secrecyId}`, '_blank');
}

const handleFollowAction = async (user) => {
  user.loading = true
  try {
    if (user.needFollow) {
      // Follow action
      await post('/api/auth/project/concernPublisher', {
        followeeId: user.userId
      })
      ElMessage.success(`已关注 ${user.username}`)
    } else {
      // Unfollow action
      await post('/api/auth/project/concernPublisherCancel', {
        followeeId: user.userId
      })
      ElMessage.success(`已取消关注 ${user.username}`)
    }
    // Toggle the follow state
    user.needFollow = !user.needFollow
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败，请稍后重试')
  } finally {
    user.loading = false
  }
}

// 关注我的用户分页变化
const handleFollowerPageChange = (page) => {
  followerPage.value = page
  loadFollowerList()
}

const openFollowDialog = async (type) => {
  console.log("打开关注弹窗，type:",type)
  currentFollowTab.value = type
  followDialogTitle.value = type === 'followers' ? '关注我的' : '我关注的'
  
  // 重置状态
  if (type === 'following') {
    followingList.value = []
    followingPage.value = 1
    noMoreFollowing.value = false
  } else {
    followersList.value = []
    followersPage.value = 1
    noMoreFollowers.value = false
  }
  
  followDialogVisible.value = true
  
  // 加载数据
  if (type === 'following') {
    await loadFollowingList()
  } else {
    await loadFollowersList()
  }
}



// 加载关注我的用户列表
const loadFollowerList = async () => {
  followerLoading.value = true
  try {
    const res = await post('/api/auth/my/myFollowers', {
      page: followerPage.value,
      size: followerSize.value
    })
    const records = res.records || res
    followerList.value = records.map(user => ({
      ...user,
      loading: false
    }))
    followerTotal.value = res.total || records.length || 0
  } catch (err) {
    console.error('加载粉丝用户失败', err)
    ElMessage.error('加载粉丝用户失败')
  } finally {
    followerLoading.value = false
  }
}

const navigateToBar = (barId) => {
  window.open(`/index/quan/QuanDetail/${barId}`, '_blank')
}

// 加载我关注的用户列表
const loadFolloweeList = async () => {
  followeeLoading.value = true
  try {
    const res = await post('/api/auth/my/myFollowees', {
      page: followeePage.value,
      size: followeeSize.value
    })
    const records = res.records || res
    // Add loading state to each user
    followeeList.value = records.map(user => ({
      ...user,
      loading: false
    }))
    followeeTotal.value = res.total || records.length || 0
    // const records = res.records || res
    // followeeList.value = records
    // followeeTotal.value = res.total || records.length || 0
  } catch (err) {
    console.error('加载关注用户失败', err)
    ElMessage.error('加载关注用户失败')
  } finally {
    followeeLoading.value = false
  }
}

// 无限滚动加载更多
const loadMoreFollows = () => {
  console.log('无限滚动加载更多开始，当前页码:', followersPage.value)
  
  if (currentFollowTab.value === 'following') {
    if (!loadingFollowing.value && !noMoreFollowing.value) {
      console.log('加载更多我关注的列表，当前页码:', followingPage.value)
      loadFollowingList()
    }
  } else {
    if (!loadingFollowers.value && !noMoreFollowers.value) {
      console.log('加载更多关注我的列表，当前页码:', followersPage.value)
      loadFollowersList()
    } else {
      console.log('不满足加载条件:', {
        loading: loadingFollowers.value,
        noMore: noMoreFollowers.value
      })
    }
  }
}

// 关注用户
const followUser = async (userId, index, isFollowerTab) => {
  try {
    console.log("关注用户,userId:",userId)
    const res = await post('/api/auth/project/concernPublisher', {
        followeeId: userId
      });
      if (res) {
        // 回关成功后更新状态
         if (isFollowerTab) {
            followersList.value[index].isFollowing = true
          } else {
            // 回关后从列表中移除
            followersList.value.splice(index, 1)
          }
        ElMessage.success('回关成功');
      } else {
        ElMessage.error('关注失败')
      }
  } catch (error) {
    console.error('关注失败:', error)
    ElMessage.error('关注失败')
  }
}

// 取消关注
const unfollowUser = async (userId, index) => {
  try {
    console.log("取消关注userId",userId)
    const res = await post('/api/auth/project/concernPublisherCancel', {
        followeeId: userId
      })
      if (res) {
        ElMessage.success('已取消关注')
        
        if (followActiveTab.value === 'following') {
          followingList.value.splice(index, 1)
        } else {
          followersList.value[index].isFollowing = false
        }
      } else {
        ElMessage.error('取消关注失败')
      }
    
  } catch (error) {
    console.error('取消关注失败:', error)
    ElMessage.error('取消关注失败')
  }
}

// 查看用户主页
const viewUserProfile = (userId) => {
  // router.push(`/user/${userId}`)
  followDialogVisible.value = false
   window.open(`/index/user/${userId}`, '_blank');
}


const form = ref({
  id:'',
  audience: '',
  time: '',
  skills: '',
  skillNames:'',
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

// 修改 loadIntentData 方法，确保正确处理技能数据
const loadIntentData = async () => {
  try {
    const res = await get('/api/auth/project/getProjectOfMyShow')
    if (res) {
      // 将 skills 从字符串转换为数组
      const skills = res.skills ? 
        (Array.isArray(res.skills) ? 
          res.skills.map(Number) : 
          res.skills.split(',').filter(Boolean).map(Number)
        ) : []
      
      // 查找对应的身份描述
      const audienceDesc = userTypes.value.find(type => type.code === res.audience)?.desc || res.audienceName
      
      console.log("skills",skills)
      form.value = {
        id: res.id || '',
        audience: res.audience || '', // 使用code值
        audienceDesc: audienceDesc, // 保存描述文本用于显示
        time: res.timePerDay || '',
        skills: skills, // 确保是数组格式
        skillNames:res.skillNames,
        resources: res.resources || '',
        status: res.status ? 1 : 0
      }
      console.log("form.value after load", form.value)
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
    
    // 准备提交数据
    const submitData = {
      ...form.value,
      skills: Array.isArray(form.value.skills) ? form.value.skills.join(',') : form.value.skills,
      // 确保audience是code值
      audience: form.value.audience
    }

    await post('/api/auth/project/updateProjectOfMyShow', submitData)
    
    // 更新显示用的描述文本
    if (form.value.audience) {
      const selectedType = userTypes.value.find(type => type.code === form.value.audience)
      if (selectedType) {
        form.value.audienceDesc = selectedType.desc
      }
    }
    
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
    const res = await get('/api/auth/my/myFollowCount')
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

.follow-list-container {
  max-height: 500px;
  overflow-y: auto;
  padding: 8px;
  /* 确保容器有明确高度 */
  height: 500px;
}

.user-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  margin-bottom: 12px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.user-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  flex-shrink: 0;
}

.user-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.user-name {
  font-weight: 500;
  font-size: 15px;
  color: #1a1a1a;
}

.user-industry .el-tag {
  margin-right: 0;
}

.user-actions {
  display: flex;
  gap: 8px;
}

.empty-placeholder {
  text-align: center;
  padding: 40px 0;
  color: #8590a6;
  font-size: 14px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .user-card {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .user-actions {
    width: 100%;
    justify-content: flex-end;
  }
}

.loading-more {
  text-align: center;
  padding: 16px;
  color: #8590a6;
  font-size: 14px;
}

.loading-more .el-icon {
  margin-right: 8px;
  animation: rotating 2s linear infinite;
}

@keyframes rotating {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.no-more {
  text-align: center;
  padding: 16px;
  color: #8590a6;
  font-size: 14px;
}

/*1111111  */
/* Drawer 容器样式 */
.custom-drawer {
  --el-drawer-bg-color: #f8fafc; /* 更柔和的背景色 */
  --el-drawer-padding-primary: 20px;
  --el-drawer-box-shadow: 0 4px 24px rgba(0, 0, 0, 0.08); /* 更精致的阴影 */
}

/* Drawer 头部样式 */
.custom-drawer .el-drawer__header {
  padding: 18px 24px;
  margin-bottom: 0;
  border-bottom: 1px solid #e2e8f0; /* 更细腻的分隔线 */
  color: #1e293b; /* 深色文字 */
  font-weight: 600;
  background-color: #ffffff;
  border-radius: 8px 8px 0 0;
}

/* Drawer 内容区域样式 */
.custom-drawer .el-drawer__body {
  padding: 24px;
  background-color: #f8fafc; /* 与头部形成轻微对比 */
}

/* 用户卡片样式 */
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

/* 用户信息区域 */
.followee-user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

/* 用户名样式 */
.followee-username {
  font-weight: 600;
  font-size: 16px;
  color: #1e293b; /* 深色文字 */
}

/* 行业标签样式 */
.followee-industry {
  font-size: 13px;
  color: #64748b; /* 中灰色 */
  background-color: #f1f5f9;
  padding: 2px 8px;
  border-radius: 4px;
  display: inline-block;
  width: fit-content;
}

/* 操作按钮区域 */
.followee-actions {
  margin-top: 12px;
  display: flex;
  gap: 10px;
}

/* 加载中状态 */
.followee-loading {
  text-align: center;
  padding: 20px;
  font-size: 14px;
  color: #64748b;
  background-color: #f8fafc;
  border-radius: 8px;
  margin: 12px;
}

/* 分页容器 */
.pagination-container {
  position: absolute;
  bottom: 20px;
  right: 20px;
  padding: 10px;
  background: white;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* 自定义分页样式 */
.custom-pagination {
  --el-pagination-button-width: 40px; /* 增大分页按钮 */
  --el-pagination-button-height: 40px;
  --el-pagination-font-size: 15px;
  --el-pagination-bg-color: #ffffff;
  --el-pagination-button-color: #64748b;
  --el-pagination-button-disabled-bg-color: #f8fafc;
  --el-pagination-hover-color: #3b82f6;
}

.custom-pagination .btn-prev,
.custom-pagination .btn-next,
.custom-pagination .number {
  min-width: 40px;
  height: 40px;
  line-height: 40px;
  border-radius: 8px;
  margin: 0 4px;
  font-weight: 500;
}

.custom-pagination .number:hover,
.custom-pagination .number.is-active {
  background-color: #3b82f6;
  color: white;
}

/* 无更多内容提示 */
.no-more {
  text-align: center;
  padding: 16px;
  font-size: 14px;
  color: #94a3b8;
  background-color: #f8fafc;
  border-radius: 8px;
  margin: 12px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .custom-drawer {
    width: 90% !important;
  }
  
  .followee-user-card {
    flex-direction: column;
  }
  
  .followee-actions {
    flex-direction: column;
  }
  
  .followee-actions .el-button {
    width: 100%;
  }
}

@media (max-width: 576px) {
  .followee-user-card {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .followee-actions {
    flex-direction: column;
    width: 100%;
  }
  
  .followee-actions .el-button {
    width: 100%;
  }
  
  .custom-drawer {
    width: 100% !important;
  }
}

/* 我关注的圈子卡片样式 */
.followed-bar-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 16px;
  margin-bottom: 12px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  cursor: pointer;
  
  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    transform: translateY(-2px);
  }
}

.followed-bar-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.followed-bar-name {
  font-weight: 600;
  font-size: 16px;
  color: #1e293b;
}

.followed-bar-stats {
  display: flex;
  gap: 12px;
  
  .stat-item {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    font-size: 13px;
    color: #64748b;
    
    .el-icon {
      font-size: 14px;
    }
  }
}

.followed-bar-unread {
  position: absolute;
  right: 16px;
  
  .el-badge {
    :deep(.el-badge__content) {
      transform: scale(0.8);
      transform-origin: 100% 0;
    }
  }
}

.followed-bar-loading {
  text-align: center;
  padding: 20px;
  font-size: 14px;
  color: #64748b;
  background-color: #f8fafc;
  border-radius: 8px;
  margin: 12px;
}

/* 积分抽屉样式 */
.points-overview {
  background: linear-gradient(135deg, #409EFF, #79BBFF);
  border-radius: 12px;
  padding: 20px;
  color: white;
  margin-bottom: 20px;
}

.points-total {
  text-align: center;
  margin-bottom: 20px;
}

.points-value {
  font-size: 48px;
  font-weight: 700;
  line-height: 1;
  display: block;
}

.points-label {
  font-size: 16px;
  opacity: 0.9;
}

.points-stats {
  display: flex;
  justify-content: space-around;
}

.points-stat-item {
  text-align: center;
}

.stat-label {
  font-size: 14px;
  opacity: 0.8;
  display: block;
  margin-bottom: 5px;
}

.stat-value {
  font-size: 20px;
  font-weight: 600;
  display: block;
}

/* 积分任务样式 */
.points-tasks {
  margin-bottom: 20px;
}

.tasks-title {
  font-size: 16px;
  color: #303133;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f2f7;
}

.task-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.task-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.task-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.task-content {
  flex: 1;
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.task-name {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
}

.task-points {
  color: #E6A23C;
  font-weight: 600;
}

.task-desc {
  margin: 0 0 8px 0;
  font-size: 13px;
  color: #606266;
}

.task-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
  font-size: 13px;
  color: #909399;
}

/* 积分规则样式 */
.points-rules {
  margin-bottom: 20px;
}

.rules-title {
  font-size: 16px;
  color: #303133;
  margin-bottom: 12px;
}

.rules-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.rules-list li {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px dashed #eaeaea;
}

.rule-name {
  color: #606266;
}

.rule-points {
  color: #E6A23C;
  font-weight: 500;
}

/* 积分操作按钮 */
.points-actions {
  display: flex;
  gap: 12px;
  margin-top: 20px;
}

.points-loading {
  text-align: center;
  padding: 20px;
  color: #909399;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .points-stats {
    flex-direction: column;
    gap: 16px;
  }
  
  .points-actions {
    flex-direction: column;
  }
  
  .points-actions .el-button {
    width: 100%;
  }
}

.badges-container {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 6px;
}

.badge-tag {
  margin-right: 4px;
  margin-bottom: 4px;
}

/* Adjust points stats layout */
.points-stats {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.points-stat-item {
  background: rgba(255, 255, 255, 0.1);
  padding: 10px;
  border-radius: 8px;
  text-align: center;
}

.stat-value {
  font-size: 18px;
}

/* For the badges item specifically */
.points-stat-item:last-child {
  grid-column: span 2;
  text-align: center;
}

/* 积分任务加载动画 */
.task-loading {
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 8px;
  margin-bottom: 20px;
}

/* 任务列表过渡动画 */
.task-list-move, /* 对移动的元素应用过渡 */
.task-list-enter-active,
.task-list-leave-active {
  transition: all 0.5s ease;
}

.task-list-enter-from,
.task-list-leave-to {
  opacity: 0;
  transform: translateX(30px);
}

.task-list-leave-active {
  position: absolute;
}

/* 分页容器样式优化 */
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  padding: 10px 0;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

/* 任务项悬停效果增强 */
.task-item {
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
}

.task-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

/* 按钮加载效果 */
.el-button--primary.is-disabled {
  opacity: 0.7;
  transition: opacity 0.3s;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .points-stats {
    grid-template-columns: 1fr;
  }
  
  .task-item {
    flex-direction: column;
  }
  
  .task-icon {
    margin-bottom: 10px;
  }
}

/* 在style部分添加 */
.task-points {
  font-weight: 600;
  white-space: nowrap;
}

.task-meta .el-button {
  min-width: 80px;
  transition: all 0.3s ease;
}

/* 已完成按钮样式 */
.el-button--info.is-disabled {
  opacity: 1;
  background-color: #f0f9ff;
  border-color: #c6e2ff;
  color: #409eff;
}

/* 可领取按钮悬停效果 */
.el-button--success:not(.is-disabled):hover {
  background-color: #67c23a;
  border-color: #67c23a;
}

/* 徽章容器样式 */
.badges-container {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-top: 16px;
  justify-content: center;
  perspective: 1000px; /* 为3D效果添加透视 */
}

/* 单个徽章样式 */
.badge-item {
  position: relative;
  width: 80px;  /* 增大基础尺寸 */
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: radial-gradient(
    circle at center, 
    var(--badge-color) 0%, 
    color-mix(in srgb, var(--badge-color), #000 20%) 100%
  );
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
  transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
  cursor: pointer;
  z-index: 1;
  transform-style: preserve-3d; /* 启用3D变换 */
}

/* 徽章图标样式 */
.badge-icon {
  width: 70%;
  height: 70%;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background-color: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(3px);
  transition: all 0.4s ease;
  overflow: hidden;
}

.badge-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
  filter: drop-shadow(0 3px 6px rgba(0, 0, 0, 0.3));
  transform: scale(1);
  transition: transform 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.badge-icon .el-icon {
  font-size: 32px;
  color: white;
  transition: all 0.3s ease;
}

/* 徽章悬停效果 */
.badge-item:hover {
  transform: translateY(-6px) scale(1.05);
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.2);
}

.badge-item:hover .badge-image {
  transform: scale(1.2); /* 图片放大效果 */
}

.badge-item:hover .el-icon {
  transform: scale(1.2);
}

/* 工具提示样式 - 增强版 */
.badge-tooltip {
  position: absolute;
  bottom: calc(100% + 16px);
  left: 50%;
  transform: translateX(-50%) translateY(10px) scale(0.95);
  transform-origin: bottom center;
  opacity: 0;
  width: 220px;
  padding: 14px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.18);
  text-align: center;
  pointer-events: none;
  transition: all 0.4s cubic-bezier(0.68, -0.55, 0.265, 1.55);
  z-index: 10;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.badge-item:hover .badge-tooltip {
  opacity: 1;
  transform: translateX(-50%) translateY(0) scale(1);
}

.tooltip-name {
  font-weight: 700;
  font-size: 15px;
  color: #1a1a1a;
  margin-bottom: 6px;
  letter-spacing: 0.5px;
}

.tooltip-desc {
  font-size: 13px;
  color: #4a4a4a;
  line-height: 1.5;
}

/* 工具提示箭头 - 增强版 */
.badge-tooltip::after {
  content: '';
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translateX(-50%);
  border-width: 10px;
  border-style: solid;
  border-color: rgba(255, 255, 255, 0.95) transparent transparent transparent;
  filter: drop-shadow(0 2px 2px rgba(0, 0, 0, 0.1));
}

/* 添加徽章悬浮时的微光效果 */
.badge-item::before {
  content: '';
  position: absolute;
  top: -5px;
  left: -5px;
  right: -5px;
  bottom: -5px;
  border-radius: 50%;
  background: radial-gradient(
    circle at center, 
    rgba(255, 255, 255, 0.4) 0%, 
    transparent 70%
  );
  opacity: 0;
  transition: opacity 0.3s ease;
}

.badge-item:hover::before {
  opacity: 1;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .badges-container {
    gap: 16px;
  }
  
  .badge-item {
    width: 68px;
    height: 68px;
  }
  
  .badge-tooltip {
    width: 180px;
    padding: 12px;
  }
  
  .tooltip-name {
    font-size: 14px;
  }
  
  .tooltip-desc {
    font-size: 12px;
  }
}
</style>