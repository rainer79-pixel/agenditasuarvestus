import router from '@/router/index.js'

export default {
  navigateToHomeView() {
    router.push({ name: 'homeRoute' })
  },
  navigateToLoginView() {
    router.push({ name: 'loginRoute' })
  },
  navigateToDashboardView() {
    window.location.href = '/dashboard'
  },
  navigateToSellersView() {
    router.push({ name: 'sellersRoute' })
  },
  navigateToReportsView() {
    router.push({ name: 'reportsRoute' })
  },
  navigateToInvoiceControlView() {
    router.push({ name: 'invoiceControlRoute' })
  },
  navigateToSettingsView() {
    router.push({ name: 'settingsRoute' })
  },
}
