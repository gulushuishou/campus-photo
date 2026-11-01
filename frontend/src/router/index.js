import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/',
    component: () => import('@/layout/Layout.vue'),
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('@/views/Home.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'demand',
        name: 'Demand',
        component: () => import('@/views/Demand.vue'),
        meta: { title: '约拍需求' }
      },
      {
        path: 'demand/:id',
        name: 'DemandDetail',
        component: () => import('@/views/DemandDetail.vue'),
        meta: { title: '需求详情' }
      },
      {
        path: 'works',
        name: 'Works',
        component: () => import('@/views/Works.vue'),
        meta: { title: '作品展示' }
      },
      {
        path: 'work/:id',
        name: 'WorkDetail',
        component: () => import('@/views/WorkDetail.vue'),
        meta: { title: '作品详情' }
      },
      {
        path: 'order',
        name: 'Order',
        component: () => import('@/views/Order.vue'),
        meta: { title: '我的订单' }
      },
      {
        path: 'message',
        name: 'Message',
        component: () => import('@/views/Message.vue'),
        meta: { title: '私信' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue'),
        meta: { title: '个人中心' }
      },
      {
        path: 'admin',
        name: 'Admin',
        component: () => import('@/views/Admin.vue'),
        meta: { title: '管理后台' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫：未登录跳转登录页
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
