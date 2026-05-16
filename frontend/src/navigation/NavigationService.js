import router from '@/router/index.js'

export default {
  navigateToHomeView() {
    router.push({ name: 'homeRoute' })
  },
  navigateToLoginView() {
    router.push({ name: 'loginRoute' })
  },
  navigateToDashboardView() {
    router.push({ name: 'dashboardRoute' })
  },
}
