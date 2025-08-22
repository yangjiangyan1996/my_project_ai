<template>
  <div class="welcome-container">
    <!-- 左侧背景图片区域 -->
    <div class="background-section">
      <el-image 
        class="background-image"
        fit="cover"
        :src="shouyePng"
      />
      <div class="overlay"></div>
      
      <!-- 欢迎标题 -->
      <div class="welcome-title">
        <h1>欢迎来到平耀聚业通</h1>
        <p>在这里，你可以发现有趣的副业项目，结识志同道合的伙伴，一起合作共创。</p>
        <p>无论你是想学习新技能、寻找兼职机会，还是想把创意变成现实，都能在这里找到舞台。</p>
      </div>
    </div>
    
    <!-- 右侧卡片区域 -->
    <div class="card-section">
      <!-- 路由视图 -->
      <router-view v-slot="{ Component }">
        <transition name="el-fade-in-linear" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </div>
    
    <!-- 底部备案信息 -->
    <div class="footer">
      <a 
        href="https://beian.miit.gov.cn/#/Integrated/index" 
        target="_blank" 
        rel="noopener noreferrer"
        class="beian-link"
      >
        浙ICP备2025192607号-1
      </a>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import shouyePng from '@/assets/shouye.png'

// 图片导入
// const shouyePng = ref('https://images.unsplash.com/photo-1522202176988-66273c2fd55f?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1000&q=80')
</script>

<style scoped>
.welcome-container {
  width: 100vw;
  height: 100vh;
  display: flex;
  position: relative;
  overflow: hidden;
}

.background-section {
  flex: 1;
  position: relative;
  overflow: hidden;
}

.background-image {
  width: 100%;
  height: 100%;
}

.overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, rgba(30, 41, 59, 0.7) 0%, rgba(15, 23, 42, 0.9) 100%);
}

.welcome-title {
  position: absolute;
  bottom: 100px;
  left: 50px;
  color: white;
  max-width: 600px;
  z-index: 2;
}

.welcome-title h1 {
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: 20px;
  background: linear-gradient(90deg, #3494E6, #EC6EAD);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.welcome-title p {
  font-size: 1.1rem;
  line-height: 1.6;
  margin-bottom: 12px;
  color: #e2e8f0;
}

.card-section {
  width: 450px;
  background-color: var(--el-bg-color);
  z-index: 10;
  display: flex;
  flex-direction: column;
  box-shadow: -5px 0 15px rgba(0, 0, 0, 0.05);
}

.footer {
  position: absolute;
  bottom: 20px;
  left: 0;
  right: 0;
  text-align: center;
  z-index: 10;
}

.beian-link {
  color: #909399;
  text-decoration: none;
  font-size: 0.9rem;
  transition: color 0.3s ease;
}

.beian-link:hover {
  color: #409EFF;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .welcome-container {
    flex-direction: column;
  }
  
  .background-section {
    height: 40%;
  }
  
  .card-section {
    width: 100%;
    height: 60%;
  }
  
  .welcome-title {
    bottom: 30px;
    left: 30px;
    right: 30px;
    max-width: none;
  }
  
  .welcome-title h1 {
    font-size: 1.8rem;
  }
  
  .welcome-title p {
    font-size: 1rem;
  }
}
</style>