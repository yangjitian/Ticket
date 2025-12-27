import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // 用户端路由
    {
      path: '/',
      component: () => import('@/views/user/UserLayout.vue'),
      children: [
        {
          path: '',
          name: 'Home',
          component: () => import('@/views/user/Home.vue'),
          meta: { requiresAuth: false }
        },
        {
          path: 'spots',
          name: 'SpotList',
          component: () => import('@/views/user/SpotList.vue'),
          meta: { requiresAuth: false }
        },
        {
          path: 'spot/:id',
          name: 'SpotDetail',
          component: () => import('@/views/user/SpotDetail.vue'),
          meta: { requiresAuth: false }
        },
        {
          path: 'checkout',
          name: 'Checkout',
          component: () => import('@/views/user/Checkout.vue'),
          meta: { requiresAuth: true }
        },
        {
          path: 'orders',
          name: 'UserOrders',
          component: () => import('@/views/user/UserOrders.vue'),
          meta: { requiresAuth: true }
        },
        {
          path: 'profile',
          name: 'UserProfile',
          component: () => import('@/views/user/UserProfile.vue'),
          meta: { requiresAuth: true }
        }
      ]
    },
    // 用户登录页
    {
      path: '/login',
      name: 'UserLogin',
      component: () => import('@/views/user/Login.vue'),
      meta: { requiresAuth: false }
    },
    // 管理员端路由
    {
      path: '/admin',
      redirect: '/admin/login'
    },
    {
      path: '/admin/login',
      name: 'AdminLogin',
      component: () => import('@/views/admin/Login.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/admin/dashboard',
      name: 'AdminDashboard',
      component: () => import('@/views/admin/DashboardLayout.vue'),
      meta: { requiresAuth: true, role: 'admin' }
    },
    {
      path: '/admin/user-management',
      name: 'UserManagement',
      component: () => import('@/views/admin/DashboardLayout.vue'),
      meta: { requiresAuth: true, role: 'admin' }
    },
    {
      path: '/admin/scenic-management',
      name: 'ScenicManagement',
      component: () => import('@/views/admin/DashboardLayout.vue'),
      meta: { requiresAuth: true, role: 'admin' }
    },
    {
      path: '/admin/distributor-management',
      name: 'DistributorManagement',
      component: () => import('@/views/admin/DashboardLayout.vue'),
      meta: { requiresAuth: true, role: 'admin' }
    },
    {
      path: '/admin/ticket-inventory',
      name: 'TicketInventory',
      component: () => import('@/views/admin/DashboardLayout.vue'),
      meta: { requiresAuth: true, role: 'admin' }
    }
  ],
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const userRole = localStorage.getItem('userRole')

  // 检查是否需要认证
  if (to.meta.requiresAuth) {
    if (!token) {
      // 如果是管理员路由，重定向到管理员登录页
      if (to.path.startsWith('/admin')) {
        next('/admin/login')
      } else {
        // 用户端路由重定向到用户登录页
        next('/login')
      }
      return
    }

    // 检查角色权限 - 管理员路由
    if (to.meta.role === 'admin' && userRole !== '0') {
      next('/admin/login')
      return
    }
  }

  // 已登录的管理员访问管理员登录页，跳转到管理页
  if (to.path === '/admin/login' && token && userRole === '0') {
    next('/admin/dashboard')
    return
  }

  // 已登录的用户访问用户登录页，跳转到首页
  if (to.path === '/login' && token && userRole !== '0') {
    next('/')
    return
  }

  next()
})

export default router
