// 修复后的路由配置结构
import { createRouter, createWebHistory } from 'vue-router'
import { unauthorized } from "@/net";

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/',
            name: 'welcome',
            component: () => import('@/views/WelcomeView.vue'),
            children: [
                {
                    path: '',
                    name: 'welcome-login',
                    component: () => import('@/views/welcome/LoginPage.vue')
                }, {
                    path: 'register',
                    name: 'welcome-register',
                    component: () => import('@/views/welcome/RegisterPage.vue')
                }, {
                    path: 'forget',
                    name: 'welcome-forget',
                    component: () => import('@/views/welcome/ForgetPage.vue')
                }
            ]
        }, {
            path: '/index',
            name: 'index',
            component: () => import('@/views/IndexView.vue'),
        }, {
            path: '/name-generator',
            name: 'name-generator',
            component: () => import('@/views/NameGenerator.vue'),
            //meta: { requiresAuth: true }
            meta: { layout: 'EmptyLayout' } 
        },{
            path: '/taohua',
            name: 'taohua',
            component: () => import('@/views/TaohuaView.vue'),
            meta: { layout: 'EmptyLayout' }
        },{
            path: '/index/detail/:id',
            name: 'project-detail',
            component: () => import('@/views/ProjectDetail.vue')
          },{
            path: '/index/my',
            name: 'my',
            component: () => import('@/views/My.vue'),
            meta: { layout: 'EmptyLayout' }
        },{
            path: '/index/my/CreateOfFindColleague',
            name: 'createOfFindColleague',
            component: () => import('@/views/CreateOfFindColleague.vue')
        },{
            path: '/index/my/create-find-job',
            name: 'createFindJob',
            component: () => import('@/views/create-find-job.vue')
        },{
            path: '/SkillMatch.vue',
            name: 'skillMatch',
            component: () => import('@/views/SkillMatch.vue')
        },{
            path: '/index/my/applyList',
            name: 'applyList',
            component: () => import('@/views/ApplyList.vue'),
            meta: { requiresAuth: true }
        },{
            path: '/index/my/applicationList',
            name: 'applicationList',
            component: () => import('@/views/ApplicationList.vue'),
            meta: { requiresAuth: true }
        },{
            path: '/index/my/updateUserInfo',
            name: 'updateUserInfo',
            component: () => import('@/views/UpdateUserInfo.vue'),
            meta: { requiresAuth: true }
        },{
            path: '/index/my/adminApplyList',
            name: 'adminApplyList',
            component: () => import('@/views/AdminApplyList.vue'),
            meta: { requiresAuth: true }
        },
    ]
})

// 导航守卫正确定义在路由实例之后
router.beforeEach((to, from, next) => {
  if (to.matched.some(record => record.meta.requiresAuth) && unauthorized()) {
    next({ name: 'welcome-login' })
  } else if (to.name === 'welcome' && !unauthorized()) {
    next('/index')
  } else {
    next()
  }
})

export default router
