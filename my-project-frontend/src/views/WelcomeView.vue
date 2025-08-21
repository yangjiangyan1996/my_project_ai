<template>
    <div style="width: 100vw;height: 100vh;overflow: hidden;display: flex">
        <div style="flex: 1">
             <el-image 
                style="width: 100%; height: 100%" 
                fit="cover"
                :src="shouyePng"
                />
                      
        </div>
        <div class="welcome-title">
            <div style="font-size: 30px;font-weight: bold">欢迎来到副业协作平台</div>
            <div style="margin-top: 10px">在这里，你可以发现有趣的副业项目，结识志同道合的伙伴，一起合作共创。</div>
            <div style="margin-top: 5px">无论你是想学习新技能、寻找兼职机会，还是想把创意变成现实，都能在这里找到舞台。</div>
        </div>
        <div class="right-card">
            <router-view v-slot="{ Component }">
                <transition name="el-fade-in-linear" mode="out-in">
                    <component :is="Component" style="height: 100%"/>
                </transition>
            </router-view>
        </div>
    </div>
</template>

<script setup>
import shouyePng from '@/assets/shouye.png'

</script>

<style scoped>
.right-card {
  width: 400px;
  z-index: 1;
  background-color: var(--el-bg-color);
}

.welcome-title {
    position: absolute;
    bottom: 30px;
    left: 30px;
    color: white;
    text-shadow: 0 0 10px black;
}
</style>
