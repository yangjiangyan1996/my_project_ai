// 修复后的路由配置结构
import { createRouter, createWebHistory } from 'vue-router'
import { unauthorized } from "@/net";

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/welcome',
            name: 'welcome',
            component: () => import('@/views/WelcomeView.vue'),
            children: [
                {
                    path: '/login',
                    name: 'welcome-login',
                    component: () => import('@/views/welcome/LoginPage.vue')
                }, {
                    path: '/welcome/register',
                    name: 'welcome-register',
                    component: () => import('@/views/welcome/RegisterPage.vue')
                }, {
                    path: '/welcome/forget',
                    name: 'welcome-forget',
                    component: () => import('@/views/welcome/ForgetPage.vue')
                }
            ]
        }, {
            path: '/views/IndexView',
            name: 'index',
            component: () => import('@/views/IndexView.vue'),
            meta: { requiresAuth: false },
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
        },{
            path: '/index/my/myMemberGroupDetail/:id',
            name: 'myMemberGroupDetail',
            component: () => import('@/views/MyMemberGroupDetail.vue'),
            meta: { requiresAuth: true }
        },{
            path: '/index/user/:id',
            name: 'userProfile',
            component: () => import('@/views/UserProfile.vue'),
            meta: { requiresAuth: true }
        },{
            path: '/index/talentMatch',
            name: 'talentMatch',
            component: () => import('@/views/TalentMatch.vue'),
            meta: { requiresAuth: true }
        },{
            path: '/index/rankingList',
            name: 'rankingList',
            component: () => import('@/views/RankingList.vue'),
            meta: { requiresAuth: true }
        },{
            path: '/index/QuanList',
            name: 'quanList',
            component: () => import('@/views/QuanList.vue'),
            meta: { requiresAuth: true }
        },{
            path: '/index/quan/QuanDetail/:id',
            name: 'quanDetail',
            component: () => import('@/views/QuanDetail.vue'),
            meta: { requiresAuth: true }
        },{
            path: '/index/quan/QuanTieDetail/:id',
            name: 'quanTieDetail',
            component: () => import('@/views/QuanTieDetail.vue'),
            meta: { requiresAuth: true }
        },
        {
            path: '/index/Toolbox',
            name: 'toolbox',
            component: () => import('@/views/Toolbox.vue'),
            meta: { requiresAuth: true }
        },
        {
            path: '/',
            name: 'ckIndex',
            component: () => import('@/views/ck/CkIndex.vue'),
            meta: { requiresAuth: false } //TODO yang 这里后面都要改成true
        },
        {
            path: '/index/ckInboundCreate',
            name: 'ckInboundCreate',
            component: () => import('@/views/ck/CkInboundCreate.vue'),
            meta: { requiresAuth: false }
        },
        {
            path: '/index/ckInboundCreate/:id',
            name: 'ckInboundCreateEdit',
            component: () => import('@/views/ck/CkInboundCreate.vue'),
            meta: { requiresAuth: false }
        },
        {
            path: '/index/ckOutboundCreate',
            name: 'ckOutboundCreate',
            component: () => import('@/views/ck/CkOutboundCreate.vue'),
            meta: { requiresAuth: false }
        },
        {
            path: '/index/ckTransferCreate',
            name: 'ckTransferCreate',
            component: () => import('@/views/ck/CkTransferCreate.vue'),
            meta: { requiresAuth: false }
        },
        {
            path: '/index/ckStockTakeCreate',
            name: 'ckStockTakeCreate',
            component: () => import('@/views/ck/CkStockTakeCreate.vue'),
            meta: { requiresAuth: false }
        },
        {
            path: '/index/ckApprovalTask',
            name: 'ckApprovalTask',
            component: () => import('@/views/ck/CkApprovalTask.vue'),
            meta: { requiresAuth: false }
        },
        {
            path: '/index/ckWarehouseManage',
            name: 'ckWarehouseManage',
            component: () => import('@/views/ck/CkWarehouseManage.vue'),
            meta: { requiresAuth: false }
        },
        {
            path: '/index/ckApproveManager',
            name: 'ckApproveManager',
            component: () => import('@/views/ck/CkApproveManager.vue'),
            meta: { requiresAuth: false }
        },
        {
            path: '/index/ckSupplierManage',
            name: 'ckSupplierManage',
            component: () => import('@/views/ck/CkSupplierManage.vue'),
            meta: { requiresAuth: false }
        },
        {
            path: '/index/ckCustomerManage',
            name: 'ckCustomerManage',
            component: () => import('@/views/ck/CkCustomerManage.vue'),
            meta: { requiresAuth: false }
        },
        {
            path: '/index/ckProductManage',
            name: 'ckProductManage',
            component: () => import('@/views/ck/CkProductManage.vue'),
            meta: { requiresAuth: false }
        },
        {
            path: '/index/ckInboundManage',
            name: 'ckInboundManage',
            component: () => import('@/views/ck/CkInboundManage.vue'),
            meta: { requiresAuth: false }
        },
        {
            path: '/index/inboundDetail/',
            name: 'inboundDetail',
            component: () => import('@/views/ck/InboundDetail.vue'),
            meta: { requiresAuth: false }
        },
        {
            path: '/index/CkOutboundManage/',
            name: 'ckOutboundManage',
            component: () => import('@/views/ck/ckOutboundManage.vue'),
            meta: { requiresAuth: false }
        },
        {
            path: '/index/ckTransferManage/',
            name: 'ckTransferManage',
            component: () => import('@/views/ck/CkTransferManage.vue'),
            meta: { requiresAuth: false }
                
        },
        {
            path: '/index/ckInventoryList/',
            name: 'ckInventoryList',
            component: () => import('@/views/ck/CkInventoryList.vue'),
            meta: { requiresAuth: false }
        },
        {
            path: '/index/ckInventoryTransaction/',
            name: 'ckInventoryTransaction',
            component: () => import('@/views/ck/CkInventoryTransaction.vue'),
            meta: { requiresAuth: false }
        },
        {
            path: '/index/ckStockTake/',
            name: 'ckStockTake',
            component: () => import('@/views/ck/CkStockTake.vue'),
            meta: { requiresAuth: false }
        },
        {
            path: '/index/ckShelfManage/',
            name: 'ckShelfManage',
            component: () => import('@/views/ck/CkShelfManage.vue'),
            meta: { requiresAuth: false }
        },
        {
            path: '/index/ckUser/',
            name: 'ckUser',
            component: () => import('@/views/ck/CkUser.vue'),
            meta: { requiresAuth: false }
        },


    ]
})

// 导航守卫正确定义在路由实例之后
router.beforeEach((to, from, next) => {
    const isUnauthRoute = to.matched.some(record => {
    // console.log("=======")
    // console.log("path:",record.path)
    console.log("name:",record.name)
    return record.path.startsWith('/api/unauth') || 
        record.name === 'welcome-login' || 
        record.name === 'welcome-register' || 
        record.name === 'welcome-forget' ||
        record.name === 'quanDetail' ||
        record.name === 'quanTieDetail' ||
        // record.name === 'ckIndex' ||
        record.name.startsWith('ck')
});

    // console.log("isUnauthRoute",isUnauthRoute)
    if (to.matched.some(record => record.meta.requiresAuth) && unauthorized() && !isUnauthRoute) {
        console.log("去登录3")
        next({ name: 'welcome-login' });
    } else if (to.name === 'welcome' && !unauthorized()) {
        next('/');
    } else {
        next();
    }
});

export default router
