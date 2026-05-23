<template>
  <div class="container pt-4">
    <div class="row align-items-center mb-3">
      <div class="col">
        <h2>Edasimüüjad</h2>
      </div>
      <div v-if="isAdmin" class="col-auto">
        <button class="btn btn-success" @click="goToSellerFormView()">+ Lisa uus edasimüüja</button>
      </div>
    </div>

    <div class="row mb-3">
      <div class="col-4">
        <input
          v-model="searchQuery"
          type="text"
          class="form-control"
          placeholder="Otsi edasimüüjat ..."
        />
      </div>
    </div>
    <div v-if="successMessage" class="alert alert-success">{{ successMessage }}</div>
    <div v-if="errorMessage" class="alert alert-danger">{{ errorMessage }}</div>
    <table class="table table-bordered">
      <thead>
        <tr>
          <th>Ettevõtte nimi</th>
          <th>Ettevõtte ID</th>
          <th>Staatus</th>
          <th>Tegevused</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="seller in filteredSellers" :key="seller.sellerId">
          <td>
            {{ seller.companyName }}
            <span v-if="seller.notes" :title="seller.notes" style="cursor: help; color: #6c757d; font-size: 0.85em">ⓘ</span>
          </td>
          <td>{{ seller.orgId }}</td>
          <td>
            <span class="badge" :class="seller.status === 'ACTIVE' ? 'bg-success' : 'bg-secondary'">
              {{ seller.status === 'ACTIVE' ? 'Aktiivne' : 'Peatatud' }}
            </span>
          </td>
          <td>
            <button class="btn btn-primary btn-sm me-1" style="width: 80px" @click="goToSellerView(seller.sellerId)">Vaata</button>
            <button v-if="isAdmin" class="btn btn-primary btn-sm me-1" style="width: 80px" @click="goToSellerFormView(seller.sellerId)">Muuda</button>
            <button v-if="isAdmin" class="btn btn-secondary btn-sm" style="width: 80px" @click="goToSellerSettingsView(seller.sellerId)">Seaded</button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script>
import AuthService from '@/auth/AuthService.js'
import SellerService from '@/api-services/SellerService.js'
import NavigationService from '@/navigation/NavigationService.js'

export default {
  name: 'SellersView',
  data() {
    return {
      sellers: [],
      searchQuery: '',
      successMessage: '',
      errorMessage: '',
      isAdmin: AuthService.getRole() === 'A',
    }
  },

  computed: {
    filteredSellers() {
      return this.sellers.filter((seller) =>
        seller.companyName.toLowerCase().includes(this.searchQuery.toLowerCase()),
      )
    },
  },

  methods: {
    getSellers() {
      SellerService.sendGetSellers(AuthService.getUserId())
        .then((response) => {
          this.sellers = response.data
        })
        .catch(() => {
          this.errorMessage = 'Edasimüüjaid ei leitud'
        })
    },
    goToSellerView(sellerId) {
      NavigationService.navigateToSellerView(sellerId)
    },
    goToSellerFormView(sellerId = null) {
      NavigationService.navigateToSellerFormView(sellerId)
    },
    goToSellerSettingsView(sellerId) {
      NavigationService.navigateToSellerSettingsView(sellerId)
    },
  },
  beforeMount() {
    this.successMessage = this.$route.query.successMessage ?? ''
    this.getSellers()
  },
}
</script>
