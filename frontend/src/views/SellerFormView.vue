<template>
  <div class="container pt-4">
    <h2 class="mb-4">{{ pageTitle }}</h2>

    <div v-if="errorMessage" class="alert alert-danger">{{ errorMessage }}</div>
    <div class="row">
      <div class="col-6">
        <div class="mb-3">
          <label class="form-label">Ettevõtte nimi</label>
          <input v-model="sellerData.companyName" type="text" class="form-control" />
        </div>
        <div class="mb-3">
          <label class="form-label">Edasimüüja ID</label>
          <input v-model="sellerData.orgId" type="number" class="form-control" />
        </div>
      </div>
      <div class="col-6">
        <div class="mb-3">
          <label class="form-label">Lepingu alguskuupäev</label>
          <input
            v-model="sellerData.contractStart"
            type="text"
            class="form-control"
            placeholder="pp.kk.aaaa"
          />
        </div>
        <div class="mb-3">
          <label class="form-label">Lepingu lõppkuupäev</label>
          <input
            v-model="sellerData.contractEnd"
            type="text"
            class="form-control"
            placeholder="pp.kk.aaaa"
          />
        </div>
      </div>
    </div>

    <div class="mb-3">
      <label class="form-label">Märkused</label>
      <textarea v-model="sellerData.notes" class="form-control" rows="3"></textarea>
      <div class="mt-3">
        <button class="btn btn-secondary me-2" @click="goToSellersView()">Tühista</button>
        <button class="btn btn-primary" @click="saveSeller()" :disabled="showSpinner">
          <span v-if="showSpinner" class="spinner-border spinner-border-sm me-1"></span>
          {{ submitButtonLabel }}
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import AuthService from '@/auth/AuthService.js'
import NavigationService from '@/navigation/NavigationService.js'
import SellerService from '@/api-services/SellerService.js'

export default {
  name: 'SellerFormView',
  data() {
    return {
      sellerId: null,
      sellerData: {
        companyName: '',
        orgId: null,
        contractStart: '',
        contractEnd: '',
        notes: '',
      },
      errorMessage: '',
      showSpinner: false,
    }
  },
  computed: {
    isAddMode() {
      return this.sellerId === null
    },
    pageTitle() {
      return this.isAddMode ? 'Lisa uus edasimüüja' : 'Muuda edasimüüja andmeid'
    },
    submitButtonLabel() {
      return this.isAddMode ? 'Lisa' : 'Salvesta'
    },
  },
  methods: {
    getSeller() {
      SellerService.sendGetSeller(this.sellerId)
        .then((response) => {
          this.sellerData = response.data
        })
        .catch(() => {
          this.errorMessage = 'Edasimüüjat ei leitud'
        })
    },
    saveSeller() {
      this.showSpinner = true
      this.errorMessage = ''
      const dataToSend = {
        ...this.sellerData,
        contractStart: this.toBackendDate(this.sellerData.contractStart),
        contractEnd: this.toBackendDate(this.sellerData.contractEnd),
      }
      if (this.isAddMode) {
        SellerService.sendPostSeller(AuthService.getUserId(), dataToSend)
          .then(() => NavigationService.navigateToSellersView('Edasimüüja lisatud'))
          .catch((error) => {
            this.errorMessage = error.response.data.message
          })
          .finally(() => {
            this.showSpinner = false
          })
      } else {
        SellerService.sendPutSeller(this.sellerId, AuthService.getUserId(), dataToSend)
          .then(() => NavigationService.navigateToSellersView('Edasimüüja andmed uuendatud'))
          .catch((error) => {
            this.errorMessage = error.response.data.message
          })
          .finally(() => {
            this.showSpinner = false
          })
      }
    },

    goToSellersView() {
      NavigationService.navigateToSellersView()
    },
    toBackendDate(dateStr) {
      if (!dateStr) return null
      const parts = dateStr.split('.')
      return `${parts[2]}-${parts[1]}-${parts[0]}`
    },
  },
  beforeMount() {
    this.sellerId = this.$route.query.sellerId ?? null
    if (!this.isAddMode) {
      this.getSeller()
    }
  },
}
</script>
