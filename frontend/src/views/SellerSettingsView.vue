<template>
  <div class="container mt-4">
    <h2 class="mb-3">{{ seller.companyName }}</h2>
    <!-- Üldandmed -->
    <div class="mb-4">
      <h5>Üldandmed</h5>
      <p>Reg.nr: {{ seller.orgId }}</p>
      <p>Staatus: {{ seller.status }}</p>
      <p>Lepingu algus: {{ seller.contractStart }}</p>
      <p>Lepingu lõpp: {{ seller.contractEnd ?? 'Tähtajatu' }}</p>
      <p>Märkused: {{ seller.notes }}</p>
    </div>

    <!-- Kontaktid -->
    <div class="mb-4">
      <h5>Kontaktid</h5>
      <!-- TODO: kontaktid -->
    </div>

    <!-- Piirkonnad -->
    <div class="mb-4">
      <h5>Piirkonnad</h5>
      <!-- TODO: piirkonnad -->
    </div>

    <!-- Teenustasud -->
    <div class="mb-4">
      <h5>Teenustasud</h5>
      <!-- TODO: teenustasud -->
    </div>
  </div>
</template>

<style scoped></style>

<script>
import SellerService from '@/api-services/SellerService.js'
import NavigationService from '@/navigation/NavigationService.js'

export default {
  name: 'SellerSettingsView',
  data() {
    return {
      sellerId: null,
      seller: {},
      contacts: [],
      regions: [],
      commissionRates: [],
      errorMessage: '',
    }
  },
  methods: {
    loadSeller() {
      SellerService.sendGetSeller(this.sellerId)
        .then((response) => {
          this.seller = response.data
        })
        .catch(() => NavigationService.navigateToSellersView())
    },
    loadContacts() {
      SellerService.sendGetSellerContacts(this.sellerId)
        .then((response) => {
          this.contacts = response.data
        })
        .catch(() => NavigationService.navigateToSellersView())
    },
    loadRegions() {
      SellerService.sendGetSellerRegions(this.sellerId)
        .then((response) => {
          this.regions = response.data
        })
        .catch(() => NavigationService.navigateToSellersView())
    },
  },
  beforeMount() {
    this.sellerId = this.$route.params.sellerId
    this.loadSeller()
    this.loadContacts()
    this.loadRegions()
  },
}
</script>
