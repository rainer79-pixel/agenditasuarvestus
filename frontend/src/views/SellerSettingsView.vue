<template>
  <div class="container mt-4">
    <h2 class="mb-3">{{ seller.companyName }}</h2>

    <!-- Üldandmed -->
    <div class="mb-4">
      <h5>Üldandmed</h5>
      <p>Reg.nr: {{ seller.orgId }}</p>
      <div class="d-flex justify-content-between align-items-center">
        <p class="mb-0">
          Staatus:
          <span class="badge ms-1 py-2" :class="seller.status === 'ACTIVE' ? 'bg-success' : 'bg-secondary'">
            {{ seller.status === 'ACTIVE' ? 'Aktiivne' : 'Peatatud' }}
          </span>
          <button
            class="btn btn-sm ms-2"
            :class="seller.status === 'ACTIVE' ? 'btn-danger' : 'btn-success'"
            @click="toggleStatus()"
          >
            {{ seller.status === 'ACTIVE' ? 'Peata' : 'Aktiveeri' }}
          </button>
          <button class="btn btn-warning btn-sm ms-1" @click="goToSellerFormView()">✏</button>
        </p>
        <button class="btn btn-secondary" @click="goToSellersView()">Tagasi nimekirja</button>
      </div>
      <p>Lepingu algus: {{ seller.contractStart }}</p>
      <p>Lepingu lõpp: {{ seller.contractEnd ?? 'Tähtajatu' }}</p>
      <p>Märkused: {{ seller.notes }}</p>
    </div>

    <!-- Kontaktid -->
    <div class="mb-4">
      <div class="d-flex justify-content-between align-items-center mb-2">
        <h5>Kontaktid</h5>
        <button class="btn btn-success btn-sm" style="width: 140px" @click="isContactModalOpen = true">+ Lisa kontakt</button>
      </div>
      <table class="table table-bordered">
        <thead>
          <tr>
            <th>Nimi</th>
            <th>Telefon</th>
            <th>E-mail</th>
            <th>Rollid</th>
            <th style="width: 140px" class="text-end"></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="contact in contacts" :key="contact.contactId">
            <td>{{ contact.firstName }} {{ contact.middleName }} {{ contact.lastName }}</td>
            <td>{{ contact.phone }}</td>
            <td>{{ contact.email }}</td>
            <td>{{ contact.roles.join(', ') }}</td>
            <td>
              <div class="d-flex">
                <button class="btn btn-warning btn-sm me-1" style="visibility: hidden">✏</button>
                <button class="btn btn-danger btn-sm flex-grow-1" @click="deleteContact(contact.contactId)">Kustuta</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Piirkonnad -->
    <div class="mb-4">
      <div class="d-flex justify-content-between align-items-center mb-2">
        <h5>Piirkonnad</h5>
        <button class="btn btn-success btn-sm" style="width: 140px" @click="isRegionModalOpen = true">
          + Lisa piirkond
        </button>
      </div>
      <table class="table table-bordered">
        <thead>
          <tr>
            <th>Piirkond</th>
            <th>Müügipunkte</th>
            <th style="width: 140px" class="text-end"></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="region in regions" :key="region.regionId">
            <td>{{ region.regionName }}</td>
            <td>
              <span v-if="editingRegionId !== region.regionId">{{ region.salesPointCount }}</span>
              <input
                v-else
                type="number"
                class="form-control form-control-sm"
                v-model="editingSalesPointCount"
                min="0"
              />
            </td>
            <td>
              <template v-if="editingRegionId !== region.regionId">
                <div class="d-flex">
                  <button class="btn btn-warning btn-sm me-1" @click="startEditRegion(region)">✏</button>
                  <button class="btn btn-danger btn-sm flex-grow-1" @click="deleteRegion(region.regionId)">Kustuta</button>
                </div>
              </template>
              <template v-else>
                <div class="d-flex">
                  <button class="btn btn-success btn-sm me-1" @click="saveRegionInline(region.regionId)">✓</button>
                  <button class="btn btn-secondary btn-sm flex-grow-1" @click="cancelEditRegion()">✗</button>
                </div>
              </template>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Teenustasud -->
    <div class="mb-4">
      <div class="d-flex justify-content-between align-items-center mb-2">
        <h5>Teenustasud</h5>
        <button class="btn btn-success btn-sm" style="width: 140px" @click="isCommissionModalOpen = true">
          + Lisa teenustasu
        </button>
      </div>
      <div v-if="commissionRateError" class="alert alert-danger">{{ commissionRateError }}</div>
      <table class="table table-bordered">
        <thead>
          <tr>
            <th>Tootegrupp</th>
            <th>Tasu koguselt</th>
            <th>Tasu %</th>
            <th>KM sees</th>
            <th>Kehtib alates</th>
            <th>Kehtib kuni</th>
            <th style="width: 140px" class="text-end"></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="cr in commissionRates" :key="cr.commissionRateId">
            <td>{{ cr.productTypeName }}</td>
            <td>
              <span v-if="editingCommissionRateId !== cr.commissionRateId">{{ cr.feePerTransaction }}</span>
              <input v-else type="number" class="form-control form-control-sm" v-model="editingCommissionRate.feePerTransaction" />
            </td>
            <td>
              <span v-if="editingCommissionRateId !== cr.commissionRateId">{{ cr.feePercent }}</span>
              <input v-else type="number" class="form-control form-control-sm" v-model="editingCommissionRate.feePercent" />
            </td>
            <td>
              <span v-if="editingCommissionRateId !== cr.commissionRateId">{{ cr.includesVat ? 'Jah' : 'Ei' }}</span>
              <input v-else type="checkbox" class="form-check-input" v-model="editingCommissionRate.includesVat" />
            </td>
            <td>
              <span v-if="editingCommissionRateId !== cr.commissionRateId">{{ cr.validFrom }}</span>
              <input v-else type="text" class="form-control form-control-sm" v-model="editingCommissionRate.validFrom" />
            </td>
            <td>
              <span v-if="editingCommissionRateId !== cr.commissionRateId">{{ cr.validTo ?? 'Lõputu' }}</span>
              <input v-else type="text" class="form-control form-control-sm" v-model="editingCommissionRate.validTo" />
            </td>
            <td>
              <template v-if="editingCommissionRateId !== cr.commissionRateId">
                <div class="d-flex">
                  <button class="btn btn-warning btn-sm me-1" @click="startEditCommissionRate(cr)">✏</button>
                  <button class="btn btn-danger btn-sm flex-grow-1" @click="deleteCommissionRate(cr.commissionRateId)">Kustuta</button>
                </div>
              </template>
              <template v-else>
                <div class="d-flex">
                  <button class="btn btn-success btn-sm me-1" @click="saveCommissionRateInline(cr.commissionRateId)">✓</button>
                  <button class="btn btn-secondary btn-sm flex-grow-1" @click="cancelEditCommissionRate()">✗</button>
                </div>
              </template>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <SellerSettingsContactModal
      v-if="isContactModalOpen"
      :seller-id="sellerId"
      @event-modal-closed="isContactModalOpen = false"
      @event-contact-saved="handleContactSaved"
    />
    <SellerSettingsRegionModal
      v-if="isRegionModalOpen"
      :seller-id="sellerId"
      @event-modal-closed="isRegionModalOpen = false"
      @event-region-saved="handleRegionSaved"
    />
    <SellerSettingsProductModal
      v-if="isCommissionModalOpen"
      :seller-id="sellerId"
      @event-modal-closed="isCommissionModalOpen = false"
      @event-commission-rate-saved="handleCommissionRateSaved"
    />
  </div>
</template>

<style scoped></style>

<script>
import SellerService from '@/api-services/SellerService.js'
import ProductTypeService from '@/api-services/ProductTypeService.js'
import NavigationService from '@/navigation/NavigationService.js'
import AuthService from '@/auth/AuthService.js'
import SellerSettingsContactModal from '@/components/modals/SellerSettingsContactModal.vue'
import SellerSettingsRegionModal from '@/components/modals/SellerSettingsRegionModal.vue'
import SellerSettingsProductModal from '@/components/modals/SellerSettingsProductModal.vue'

export default {
  name: 'SellerSettingsView',
  components: { SellerSettingsContactModal, SellerSettingsRegionModal, SellerSettingsProductModal },
  data() {
    return {
      sellerId: null,
      seller: {},
      contacts: [],
      regions: [],
      commissionRates: [],
      productTypes: [],
      errorMessage: '',
      commissionRateError: '',
      isContactModalOpen: false,
      isRegionModalOpen: false,
      isCommissionModalOpen: false,
      editingRegionId: null,
      editingSalesPointCount: null,
      editingCommissionRateId: null,
      editingCommissionRate: {},
    }
  },
  methods: {
    loadSeller() {
      SellerService.sendGetSeller(this.sellerId)
        .then((response) => { this.seller = response.data })
        .catch(() => NavigationService.navigateToSellersView())
    },
    loadContacts() {
      SellerService.sendGetSellerContacts(this.sellerId)
        .then((response) => { this.contacts = response.data })
        .catch(() => NavigationService.navigateToSellersView())
    },
    loadRegions() {
      SellerService.sendGetSellerRegions(this.sellerId)
        .then((response) => { this.regions = response.data })
        .catch(() => NavigationService.navigateToSellersView())
    },
    loadCommissionRates() {
      SellerService.sendGetSellerCommissionRates(this.sellerId)
        .then((response) => { this.commissionRates = response.data })
        .catch(() => NavigationService.navigateToSellersView())
    },
    loadProductTypes() {
      ProductTypeService.sendGetProductTypes()
        .then((response) => { this.productTypes = response.data })
        .catch(() => NavigationService.navigateToSellersView())
    },
    deleteContact(contactId) {
      const userId = AuthService.getUserId()
      SellerService.sendDeleteSellerContact(this.sellerId, contactId, userId)
        .then(() => this.loadContacts())
        .catch((error) => { this.errorMessage = error.response.data.message })
    },
    goToSellersView() {
      NavigationService.navigateToSellersView()
    },
    goToSellerFormView() {
      NavigationService.navigateToSellerFormView(this.sellerId)
    },
    toggleStatus() {
      const newStatus = this.seller.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
      SellerService.sendPutSellerStatus(this.sellerId, AuthService.getUserId(), { status: newStatus })
        .then(() => this.loadSeller())
        .catch((error) => { this.errorMessage = error.response.data.message })
    },
    handleContactSaved() {
      this.isContactModalOpen = false
      this.loadContacts()
    },
    handleRegionSaved() {
      this.isRegionModalOpen = false
      this.loadRegions()
    },
    startEditRegion(region) {
      this.editingRegionId = region.regionId
      this.editingSalesPointCount = region.salesPointCount
    },
    cancelEditRegion() {
      this.editingRegionId = null
      this.editingSalesPointCount = null
    },
    saveRegionInline(regionId) {
      const userId = AuthService.getUserId()
      SellerService.sendPutSellerRegion(this.sellerId, regionId, userId, { regionId: regionId, salesPointCount: this.editingSalesPointCount })
        .then(() => {
          this.cancelEditRegion()
          this.loadRegions()
        })
        .catch((error) => { this.errorMessage = error.response.data.message })
    },
    deleteRegion(regionId) {
      const userId = AuthService.getUserId()
      SellerService.sendDeleteSellerRegion(this.sellerId, regionId, userId)
        .then(() => this.loadRegions())
        .catch((error) => { this.errorMessage = error.response.data.message })
    },
    handleCommissionRateSaved() {
      this.isCommissionModalOpen = false
      this.loadCommissionRates()
    },
    startEditCommissionRate(cr) {
      this.commissionRateError = ''
      this.editingCommissionRateId = cr.commissionRateId
      const productType = this.productTypes.find((pt) => pt.productTypeName === cr.productTypeName)
      this.editingCommissionRate = {
        productTypeId: productType ? productType.productTypeId : null,
        feePerTransaction: cr.feePerTransaction,
        feePercent: cr.feePercent,
        includesVat: cr.includesVat,
        validFrom: cr.validFrom,
        validTo: cr.validTo,
      }
    },
    cancelEditCommissionRate() {
      this.editingCommissionRateId = null
      this.editingCommissionRate = {}
    },
    saveCommissionRateInline(commissionRateId) {
      const userId = AuthService.getUserId()
      SellerService.sendPutSellerCommissionRate(this.sellerId, commissionRateId, userId, this.editingCommissionRate)
        .then(() => {
          this.cancelEditCommissionRate()
          this.loadCommissionRates()
        })
        .catch((error) => { this.commissionRateError = error.response.data.message })
    },
    deleteCommissionRate(commissionRateId) {
      const userId = AuthService.getUserId()
      SellerService.sendDeleteSellerCommissionRate(this.sellerId, commissionRateId, userId)
        .then(() => this.loadCommissionRates())
        .catch((error) => {
          if (error.response.status === 409) {
            this.commissionRateError = 'Teenustasu on kasutuses ja seda ei saa kustutada'
          } else {
            this.commissionRateError = error.response.data.message
          }
        })
    },
  },
  beforeMount() {
    this.sellerId = this.$route.params.sellerId
    this.loadSeller()
    this.loadContacts()
    this.loadRegions()
    this.loadCommissionRates()
    this.loadProductTypes()
  },
}
</script>