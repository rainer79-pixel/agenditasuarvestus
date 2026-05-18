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
  navigateToSellersView(successMessage = null) {
    router.push({
      name: 'sellersRoute',
      query: successMessage ? { successMessage } : undefined,
    })
  },
  navigateToSellerView(sellerId) {
    router.push({ name: 'sellerRoute', params: { sellerId } })
  },
  navigateToSellerFormView(sellerId = null) {
    router.push({
      name: 'sellerFormRoute',
      query: sellerId ? { sellerId } : undefined,
    })
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

