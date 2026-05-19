<template>
  <div class="container pt-4">
    <div v-if="errorMessage" class="alert alert-danger">{{ errorMessage }}</div>

    <h2 class="mb-4">Edasimüüja Detailid</h2>

    <div class="row mt-3">
      <div class="col-4">
        <h5>Üldandmed</h5>
        <p><strong>Ettevõtte nimi:</strong> {{ seller.companyName }}</p>
        <p><strong>Ettevõtte ID:</strong> {{ seller.orgId }}</p>
        <p>
          <strong>Staatus:</strong>
          <span
            class="badge ms-1"
            :class="seller.status === 'ACTIVE' ? 'bg-success' : 'bg-secondary'"
          >
            {{ seller.status === 'ACTIVE' ? 'Aktiivne' : 'Peatatud' }}
          </span>
        </p>
        <p><strong>Lepingu algus:</strong> {{ seller.contractStart }}</p>
        <p><strong>Lepingu lõpp:</strong> {{ seller.contractEnd ?? 'Tähtajatu' }}</p>
      </div>

      <div class="col-4">
        <h5>Piirkonnad ja müügipunktid</h5>
        <ul class="list-unstyled">
          <li v-for="region in regions" :key="region.regionId">
            {{ region.regionName }} — {{ region.salesPointCount }} müügipunkti
          </li>
        </ul>
      </div>

      <div class="col-4">
        <h5>Märkused</h5>
        <p>{{ seller.notes }}</p>
      </div>
    </div>
    <h5 class="mt-4">Kontaktid</h5>
    <table class="table table-bordered">
      <thead>
        <tr>
          <th>Nimi</th>
          <th>Telefon</th>
          <th>E-mail</th>
          <th>Rollid</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="contact in contacts" :key="contact.contactId">
          <td>{{ contact.firstName }} {{ contact.middleName }} {{ contact.lastName }}</td>
          <td>{{ contact.phone }}</td>
          <td>{{ contact.email }}</td>
          <td>{{ contact.roles.join(', ') }}</td>
        </tr>
      </tbody>
    </table>
    <h5 class="mt-4">Teenustasud</h5>
    <table class="table table-bordered">
      <thead>
        <tr>
          <th>Tootegrupp</th>
          <th>Tasu koguselt (EUR)</th>
          <th>Tasu % summalt</th>
          <th>KM sees</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="rate in commissionRates" :key="rate.commissionRateId">
          <td>{{ rate.productTypeName }}</td>
          <td>{{ rate.feePerTransaction ?? '-' }}</td>
          <td>{{ rate.feePercent ?? '-' }}</td>
          <td>{{ rate.includesVat ? 'Jah' : 'Ei' }}</td>
        </tr>
      </tbody>
    </table>
    <div class="row mt-4">
      <div class="col">
        <button class="btn btn-secondary me-2" @click="goToSellersView()">Tagasi nimekirja</button>
        <button v-if="isAdmin" class="btn btn-secondary">Seaded</button>
      </div>
    </div>
  </div>
</template>

<script>
import AuthService from '@/auth/AuthService.js'
import SellerService from '@/api-services/SellerService.js'
import NavigationService from '@/navigation/NavigationService.js'

export default {
  name: 'SellerView',
  data() {
    return {
      sellerId: null,
      seller: {
        companyName: '',
        orgId: null,
        status: '',
        contractStart: null,
        contractEnd: null,
        notes: '',
      },
      contacts: [],
      regions: [],
      commissionRates: [],
      errorMessage: '',
      isAdmin: AuthService.getRole() === 'A',
    }
  },
  methods: {
    getSeller() {
      SellerService.sendGetSeller(this.sellerId)
        .then((response) => {
          this.seller = response.data
        })
        .catch(() => {
          this.errorMessage = 'Edasimüüjat ei leitud'
        })
    },
    getSellerContacts() {
      SellerService.sendGetSellerContacts(this.sellerId)
        .then((response) => {
          this.contacts = response.data
        })
        .catch(() => {
          this.errorMessage = 'Kontaktide laadimine ebaõnnestus'
        })
    },
    getSellerRegions() {
      SellerService.sendGetSellerRegions(this.sellerId)
        .then((response) => {
          this.regions = response.data
        })
        .catch(() => {
          this.errorMessage = 'Piirkondade laadimine ebaõnnestus'
        })
    },
    getSellerCommissionRates() {
      SellerService.sendGetSellerCommissionRates(this.sellerId)
        .then((response) => {
          this.commissionRates = response.data
        })
        .catch(() => {
          this.errorMessage = 'Teenustasude laadimine ebaõnnestus'
        })
    },
    goToSellersView() {
      NavigationService.navigateToSellersView()
    },
  },
  beforeMount() {
    this.sellerId = this.$route.params.sellerId
    this.getSeller()
    this.getSellerContacts()
    this.getSellerRegions()
    this.getSellerCommissionRates()
  },
}
</script>
