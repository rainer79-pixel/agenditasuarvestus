import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '@/views/HomeView.vue'
import LoginView from '@/views/LoginView.vue'
import DashboardView from '@/views/DashboardView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'homeRoute',
      component: HomeView,
    },
    {
      path: '/login',
      name: 'loginRoute',
      component: LoginView,
    },
    {
      path: '/dashboard',
      name: 'dashboardRoute',
      component: DashboardView,
    },

  ],
})

export default router
