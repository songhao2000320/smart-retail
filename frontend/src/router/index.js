import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/Login.vue'),
    meta: { public: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/login/Register.vue'),
    meta: { public: true }
  },
  {
    path: '/',
    component: () => import('@/components/AppLayout.vue'),
    redirect: '/stores',
    children: [
      {
        path: 'stores',
        name: 'Stores',
        component: () => import('@/views/store/StoreList.vue'),
        meta: { title: '门店管理', icon: 'Shop', roles: ['admin', 'manager'] }
      },
      {
        path: 'stores/:id/zones',
        name: 'ZoneManage',
        component: () => import('@/views/store/ZoneManage.vue'),
        meta: { title: '区域管理', icon: 'Grid', roles: ['admin', 'manager'] }
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import('@/views/user/UserList.vue'),
        meta: { title: '用户管理', icon: 'UserFilled', roles: ['admin'] }
      },
      {
        path: 'behaviors',
        name: 'Behaviors',
        component: () => import('@/views/behavior/BehaviorList.vue'),
        meta: { title: '行为数据', icon: 'Document', roles: ['admin', 'manager', 'analyst'] }
      },
      {
        path: 'analytics/traffic',
        name: 'Traffic',
        component: () => import('@/views/analytics/Traffic.vue'),
        meta: { title: '客流统计', icon: 'TrendCharts' }
      },
      {
        path: 'analytics/heatmap',
        name: 'Heatmap',
        component: () => import('@/views/analytics/Heatmap.vue'),
        meta: { title: '热力图分析', icon: 'MapLocation' }
      },
      {
        path: 'analytics/duration',
        name: 'Duration',
        component: () => import('@/views/analytics/Duration.vue'),
        meta: { title: '停留时长分析', icon: 'Timer' }
      },
      {
        path: 'analytics/conversion',
        name: 'Conversion',
        component: () => import('@/views/analytics/Conversion.vue'),
        meta: { title: '购买转化分析', icon: 'DataLine' }
      },
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/Dashboard.vue'),
        meta: { title: '可视化仪表盘', icon: 'DataAnalysis' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫 - 认证 + 角色检查
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const userStr = localStorage.getItem('user')
  const user = userStr ? JSON.parse(userStr) : null

  // 公开页面直接放行
  if (to.meta.public) {
    if (token && (to.path === '/login' || to.path === '/register')) {
      return next('/dashboard')
    }
    return next()
  }

  // 未登录 → 跳转登录页
  if (!token || !user) {
    return next('/login')
  }

  // 角色检查
  if (to.meta.roles && Array.isArray(to.meta.roles)) {
    if (!to.meta.roles.includes(user.role)) {
      return next('/dashboard')
    }
  }

  next()
})

export default router
