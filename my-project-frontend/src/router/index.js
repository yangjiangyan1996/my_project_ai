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
            meta: { layout: 'EmptyLayout' } // 使用空白布局

        }
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
