import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '@/views/HomeView.vue'
import LoginView from '@/views/LoginView.vue'
import DashboardView from '@/views/DashboardView.vue'
import SellersView from '@/views/SellersView.vue'
import SellerView from '@/views/SellerView.vue'
import SellerFormView from '@/views/SellerFormView.vue'
import SellerSettingsView from '@/views/SellerSettingsView.vue'
import UnderDevelopmentView from '@/views/UnderDevelopmentView.vue'

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
    {
      path: '/sellers',
      name: 'sellersRoute',
      component: SellersView,
    },
    {
      path: '/seller/form',
      name: 'sellerFormRoute',
      component: SellerFormView,
    },
    {
      path: '/seller/:sellerId',
      name: 'sellerRoute',
      component: SellerView,
    },
    {
      path: '/seller/:sellerId/settings',
      name: 'sellerSettingsRoute',
      component: SellerSettingsView,
    },

    { path: '/reports', name: 'reportsRoute', component: UnderDevelopmentView },
    { path: '/invoice-control', name: 'invoiceControlRoute', component: UnderDevelopmentView },
    { path: '/settings', name: 'settingsRoute', component: UnderDevelopmentView },
  ],
})

export default router
