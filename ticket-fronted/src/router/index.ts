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
          path: 'spot/:id',
          name: 'SpotDetail',
          component: () => import('@/views/user/SpotDetail.vue'),
          meta: { requiresAuth: false }
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
        },
        {
          path: 'distributor',
          name: 'DistributorManagement',
          component: () => import('@/views/user/DistributorManagement.vue'),
          meta: { requiresAuth: true, requiresDistributor: true }
        },
        {
          path: 'passenger-selection',
          name: 'PassengerSelection',
          component: () => import('@/views/user/PassengerSelection.vue'),
          meta: { requiresAuth: true }
        },
        {
          path: 'order-confirmation',
          name: 'OrderConfirmation',
          component: () => import('@/views/user/OrderConfirmation.vue'),
          meta: { requiresAuth: true }
        },
        {
          path: 'payment',
          name: 'Payment',
          component: () => import('@/views/user/Payment.vue'),
          meta: { requiresAuth: true }
        },
        {
          path: 'e-ticket',
          name: 'ETicket',
          component: () => import('@/views/user/ETicket.vue'),
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
    // 用户注册页
    {
      path: '/register',
      name: 'UserRegister',
      component: () => import('@/views/user/Register.vue'),
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
      name: 'AdminDistributorManagement',
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
  // 判断是管理员路由还是用户路由
  const isAdminRoute = to.path.startsWith('/admin')

  let token, userRole, userAuditStatus

  if (isAdminRoute) {
    // 管理员路由：使用admin_前缀的localStorage键
    token = localStorage.getItem('admin_token')
    userRole = localStorage.getItem('admin_userRole')
  } else {
    // 用户路由：使用user_前缀的localStorage键
    token = localStorage.getItem('user_token')
    userRole = localStorage.getItem('user_userRole')
    userAuditStatus = localStorage.getItem('user_userAuditStatus')
  }

  // 检查是否需要认证
  if (to.meta.requiresAuth) {
    if (!token) {
      // 如果是管理员路由，重定向到管理员登录页
      if (isAdminRoute) {
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

    // 检查经销商权限
    if (to.meta.requiresDistributor) {
      console.log('检查经销商权限:', { userRole, userRoleType: typeof userRole, userAuditStatus, userAuditStatusType: typeof userAuditStatus, token, to: to.path })
      if (userRole !== '2') {
        console.log('用户不是经销商，重定向到首页')
        next('/')
        return
      }
      if (userAuditStatus !== '1') {
        console.log('经销商未审核通过，重定向到首页')
        next('/')
        return
      }
      console.log('用户是已审核的经销商，允许访问')
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
