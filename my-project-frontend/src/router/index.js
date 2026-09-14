// WMS-only router (community routes removed)
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
            path: '/index/ckOutboundCreate/:id',
            name: 'ckOutboundCreateEdit',
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
            component: () => import('@/views/ck/CkOutboundManage.vue'),
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
            path: '/index/ckInventoryHistory/:id',
            name: 'ckInventoryHistory',
            component: () => import('@/views/ck/CkInventoryHistory.vue'),
            meta: { requiresAuth: false }
        },
        {
            path: '/index/ckSkuManage/',
            name: 'ckSkuManage',
            component: () => import('@/views/ck/CkSkuManage.vue'),
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
        
        // 采购入库
        {
            path: '/index/ckInboundPurchase',
            name: 'ckInboundPurchase',
            component: () => import('@/views/ck/inbound/CkInboundPurchase.vue'),
            meta: { requiresAuth: false }
        },
        {
            path: '/index/ckInboundPurchaseEdit/:id',
            name: 'ckInboundPurchaseEdit',
            component: () => import('@/views/ck/inbound/CkInboundPurchase.vue'),
            meta: { requiresAuth: false }
        },
        // 生产入库
        {
            path: '/index/ckInboundProduction',
            name: 'ckInboundProduction',
            component: () => import('@/views/ck/inbound/CkInboundProduction.vue'),
            meta: { requiresAuth: false }
        },
        {
            path: '/index/ckInboundProductionEdit/:id',
            name: 'ckInboundProductionEdit',
            component: () => import('@/views/ck/inbound/CkInboundProduction.vue'),
            meta: { requiresAuth: false }
        },
        // 退货入库
        {
            path: '/index/ckInboundReturn',
            name: 'ckInboundReturn',
            component: () => import('@/views/ck/inbound/CkInboundReturn.vue'),
            meta: { requiresAuth: false }
        },
        {
            path: '/index/ckInboundReturnEdit/:id',
            name: 'ckInboundReturnEdit',
            component: () => import('@/views/ck/inbound/CkInboundReturn.vue'),
            meta: { requiresAuth: false }
        },
        // 调拨入库
        {
            path: '/index/ckInboundTransfer',
            name: 'ckInboundTransfer',
            component: () => import('@/views/ck/inbound/CkInboundTransfer.vue'),
            meta: { requiresAuth: false }
        },
        {
            path: '/index/ckInboundTransferEdit/:id',
            name: 'ckInboundTransferEdit',
            component: () => import('@/views/ck/inbound/CkInboundTransfer.vue'),
            meta: { requiresAuth: false }
        },

        {
            path: '/index/salesOutboundCreate',
            name: 'SalesOutboundCreate',
            component: () => import('@/views/ck/outbound/SalesOutboundCreate.vue')
        },
        {
            path: '/index/salesOutboundCreate/:id',
            name: 'SalesOutboundCreateEdit',
            component: () => import('@/views/ck/outbound/SalesOutboundCreate.vue')
        },
        {
            path: '/index/productionPickingCreate',
            name: 'ProductionPickingCreate',
            component: () => import('@/views/ck/outbound/ProductionPickingCreate.vue')
        },
        {
            path: '/index/productionPickingCreate/:id',
            name: 'ProductionPickingCreateEdit',
            component: () => import('@/views/ck/outbound/ProductionPickingCreate.vue')
        },
        {
            path: '/index/ckUnitManage/',
            name: 'ckUnitManage',
            component: () => import('@/views/ck/CkUnitManage.vue'),
            meta: { requiresAuth: false }
        },
        {
            path: '/index/ckRecommendRuleManage/',
            name: 'ckRecommendRuleManage',
            component: () => import('@/views/ck/CkRecommendRuleManage.vue'),
            meta: { requiresAuth: false }
        },
        {
            path: '/index/ckStockManage/',
            name: 'ckStockManage',
            component: () => import('@/views/ck/stock/StockManage.vue'),
            meta: { requiresAuth: false }
        },
        {
            path: '/index/ckStockFullWareHouse/',
            name: 'ckStockFullWareHouse',
            component: () => import('@/views/ck/stock/StockFullWareHouse.vue'),
            meta: { requiresAuth: false }
        },
        {
            path: '/index/ckStockAreaWise/',
            name: 'ckStockAreaWise',
            component: () => import('@/views/ck/stock/StockAreaWise.vue'),
            meta: { requiresAuth: false }
        },
        {
            path: '/index/ckStockCategoryWise/',
            name: 'ckStockCategoryWise',
            component: () => import('@/views/ck/stock/StockCategoryWise.vue'),
            meta: { requiresAuth: false }
        },
        {
            path: '/index/ckStockCycleWise/',
            name: 'ckStockCycleWise',
            component: () => import('@/views/ck/stock/StockCycleWise.vue'),
            meta: { requiresAuth: false }
        },
        {
            path: '/index/ckStockRandomWise/',
            name: 'ckStockRandomWise',
            component: () => import('@/views/ck/stock/StockRandomWise.vue'),
            meta: { requiresAuth: false }
        },
        // 盘点操作
        {
            path: '/index/ckStockOperateItem/:id',
            name: 'ckStockOperateItem',
            component: () => import('@/views/ck/stock/StockOperateItem.vue'),
            meta: { requiresAuth: false }
        },
        // 盘点任务管理
        {
            path: '/index/ckStockTakeTaskManage/',
            name: 'ckStockTakeTaskManage',
            component: () => import('@/views/ck/stock/StockTakeTaskManage.vue'),
            meta: { requiresAuth: false }
        },
        // 调整详情
        {
            path: '/index/ckStockAdjustment/:id?',
            name: 'ckStockAdjustment',
            component: () => import('@/views/ck/stock/StockAdjustment.vue'),
            // props: (route) => ({
            //     id: route.params.id,
            //     createType: route.query.createType,
            //     warehouseId: route.query.warehouseId,
            //     warehouseName: route.query.warehouseName,
            //     viewMode: route.query.viewMode,
            //     editMode: route.query.editMode,
            //     stockTakeId: route.query.stockTakeId
            // }),
            meta: { requiresAuth: false }
        },
    ]
})

router.beforeEach((to, from, next) => {
    const publicNames = ['welcome', 'welcome-login', 'welcome-register', 'welcome-forget']
    if (publicNames.includes(to.name)) {
        next()
        return
    }
    // WMS pages: require login (ck routes previously bypassed; tighten to auth)
    if (unauthorized()) {
        next({ name: 'welcome-login' })
        return
    }
    next()
})

export default router
