<template>
  <div class="profile-container">
    <div class="profile-header">
      <div class="profile-info">
        <h1 class="username">用户名</h1>
        <div class="industry">行业</div>
        <div class="profile-actions">
          <el-button type="text" size="small">查看详细资料</el-button>
          <el-button type="text" size="small">编辑个人资料</el-button>
        </div>
      </div>
    </div>

    <div class="stats-container">
      <div class="stats-item">
        <div class="stats-count">0</div>
        <div class="stats-label">动态</div>
      </div>
      <div class="stats-item">
        <div class="stats-count">0</div>
        <div class="stats-label">回答</div>
      </div>
      <div class="stats-item">
        <div class="stats-count">0</div>
        <div class="stats-label">视频</div>
      </div>
      <div class="stats-item">
        <div class="stats-count">0</div>
        <div class="stats-label">提问</div>
      </div>
      <div class="stats-item">
        <div class="stats-count">0</div>
        <div class="stats-label">文章</div>
      </div>
      <div class="stats-item">
        <div class="stats-count">0</div>
        <div class="stats-label">专栏</div>
      </div>
      <div class="stats-item">
        <div class="stats-count">0</div>
        <div class="stats-label">想法</div>
      </div>
      <div class="stats-item">
        <div class="stats-count">0</div>
        <div class="stats-label">收藏</div>
      </div>
      <div class="stats-item">
        <div class="stats-count">0</div>
        <div class="stats-label">关注</div>
      </div>
    </div>

    <div class="content-tabs">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="我的动态" name="activities">
          <div class="activity-item">
            <div class="activity-type">赞同了回答</div>
            <div class="activity-time">2025-06-16 15:03</div>
            <div class="activity-content">
              <h3 class="activity-title">我35了，应该算是大龄女了吧，我应该妥协吗？</h3>
              <div class="activity-detail">
                <p>回答内容摘要...</p>
              </div>
              <div class="activity-meta">
                <span>已赞同 27</span>
                <span>6 条评论</span>
                <el-button type="text" size="small">分享</el-button>
                <el-button type="text" size="small">收藏</el-button>
                <el-button type="text" size="small">喜欢</el-button>
              </div>
            </div>
          </div>
        </el-tab-pane>
        <el-tab-pane label="个人成就" name="achievements">
          <div class="achievements-container">
            <div class="achievement-item">
              <div class="achievement-count">5</div>
              <div class="achievement-label">次赞同</div>
            </div>
            <div class="achievement-item">
              <div class="achievement-count">112</div>
              <div class="achievement-label">次喜欢</div>
            </div>
            <div class="achievement-item">
              <div class="achievement-count">15</div>
              <div class="achievement-label">次收藏</div>
            </div>
            <div class="achievement-item">
              <div class="achievement-count">15</div>
              <div class="achievement-label">次公共编辑</div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <div class="sidebar">
      <div class="sidebar-section">
        <h3 class="sidebar-title">关注了</h3>
        <div class="sidebar-count">48</div>
      </div>
      <div class="sidebar-section">
        <h3 class="sidebar-title">关注者</h3>
        <div class="sidebar-count">7</div>
      </div>
      <div class="sidebar-section">
        <h3 class="sidebar-title">赞助的 Live</h3>
        <div class="empty-placeholder">暂无内容</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const activeTab = ref('activities')
</script>

<style scoped>
.profile-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
  display: grid;
  grid-template-columns: 1fr 250px;
  gap: 20px;
}

.profile-header {
  grid-column: 1 / -1;
  margin-bottom: 20px;
}

.profile-info {
  padding: 20px 0;
}

.username {
  font-size: 24px;
  margin: 0 0 5px 0;
}

.industry {
  color: #8590a6;
  margin-bottom: 10px;
}

.profile-actions {
  margin-top: 10px;
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
  padding: 15px 0;
  border-bottom: 1px solid #f0f2f7;
}

.activity-type {
  color: #8590a6;
  font-size: 14px;
}

.activity-time {
  color: #8590a6;
  font-size: 14px;
  margin: 5px 0;
}

.activity-title {
  font-size: 16px;
  margin: 10px 0;
}

.activity-detail {
  color: #1a1a1a;
  margin: 10px 0;
}

.activity-meta {
  color: #8590a6;
  font-size: 14px;
}

.activity-meta > * {
  margin-right: 15px;
}

.achievements-container {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}

.achievement-item {
  width: calc(25% - 15px);
  text-align: center;
  padding: 15px;
  background: #f7f8fa;
  border-radius: 4px;
}

.achievement-count {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 5px;
}

.achievement-label {
  color: #8590a6;
  font-size: 14px;
}

.sidebar {
  grid-column: 2;
}

.sidebar-section {
  margin-bottom: 25px;
}

.sidebar-title {
  font-size: 16px;
  color: #444;
  margin-bottom: 10px;
}

.sidebar-count {
  font-size: 18px;
  font-weight: 600;
}

.empty-placeholder {
  color: #8590a6;
  font-size: 14px;
  padding: 10px 0;
}
</style>