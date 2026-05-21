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
      <div class="d-flex justify-content-between align-items-center mb-2">
        <h5>Kontaktid</h5>
        <button class="btn btn-success btn-sm" @click="isContactModalOpen = true">
          + Lisa kontakt
        </button>
      </div>
      <table class="table table-bordered">
        <thead>
          <tr>
            <th>Nimi</th>
            <th>Telefon</th>
            <th>E-mail</th>
            <th>Rollid</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="contact in contacts" :key="contact.contactId">
            <td>{{ contact.firstName }} {{ contact.middleName }} {{ contact.lastName }}</td>
            <td>{{ contact.phone }}</td>
            <td>{{ contact.email }}</td>
            <td>{{ contact.roles.join(', ') }}</td>
            <td>
              <button class="btn btn-danger btn-sm" @click="deleteContact(contact.contactId)">
                Kustuta
              </button>
            </td>
          </tr>
        </tbody>
      </table>
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
    <SellerSettingsContactModal
      v-if="isContactModalOpen"
      :seller-id="sellerId"
      @event-modal-closed="isContactModalOpen = false"
      @event-contact-saved="handleContactSaved"
    />
  </div>
</template>

<style scoped></style>

<script>
import SellerService from '@/api-services/SellerService.js'
import NavigationService from '@/navigation/NavigationService.js'
import AuthService from '@/auth/AuthService.js'
import SellerSettingsContactModal from '@/components/modals/SellerSettingsContactModal.vue'

export default {
  name: 'SellerSettingsView',
  components: { SellerSettingsContactModal },
  data() {
    return {
      sellerId: null,
      seller: {},
      contacts: [],
      regions: [],
      commissionRates: [],
      errorMessage: '',
      isContactModalOpen: false,
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
    deleteContact(contactId) {
      const userId = AuthService.getUserId()
      SellerService.sendDeleteSellerContact(this.sellerId, contactId, userId)
        .then(() => this.loadContacts())
        .catch((error) => {
          this.errorMessage = error.response.data.message
        })
    },
    handleContactSaved() {
      this.isContactModalOpen = false
      this.loadContacts()
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
