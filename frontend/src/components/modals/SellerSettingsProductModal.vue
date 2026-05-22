<template>
  <div class="modal d-block">
    <div class="modal-dialog">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">Lisa teenustasu</h5>
          <button type="button" class="btn-close" @click="$emit('event-modal-closed')"></button>
        </div>
        <div class="modal-body">
          <div v-if="errorMessage" class="alert alert-danger">{{ errorMessage }}</div>
          <div class="mb-3">
            <label class="form-label">Tootegrupp</label>
            <select class="form-select" v-model="selectedProductTypeId">
              <option :value="null" disabled>Vali tootegrupp</option>
              <option v-for="pt in productTypes" :key="pt.productTypeId" :value="pt.productTypeId">
                {{ pt.productTypeName }}
              </option>
            </select>
          </div>
          <div class="mb-3">
            <label class="form-label">Tasu koguselt (EUR)</label>
            <input type="number" class="form-control" v-model="feePerTransaction" />
          </div>
          <div class="mb-3">
            <label class="form-label">Tasu % summalt</label>
            <input type="number" class="form-control" v-model="feePercent" />
          </div>
          <div class="mb-3 form-check">
            <input type="checkbox" class="form-check-input" id="includesVat" v-model="includesVat" />
            <label class="form-check-label" for="includesVat">KM sees</label>
          </div>
          <div class="mb-3">
            <label class="form-label">Kehtib alates</label>
            <input type="text" class="form-control" v-model="validFrom" placeholder="pp.kk.aaaa" />
          </div>
          <div class="mb-3">
            <label class="form-label">Kehtib kuni</label>
            <input type="text" class="form-control" v-model="validTo" placeholder="pp.kk.aaaa" />
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" @click="$emit('event-modal-closed')">Tühista</button>
          <button class="btn btn-success" @click="saveCommissionRate()">Lisa</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped></style>

<script>
import ProductTypeService from '@/api-services/ProductTypeService.js'
import SellerService from '@/api-services/SellerService.js'
import AuthService from '@/auth/AuthService.js'

export default {
  name: 'SellerSettingsProductModal',
  props: {
    sellerId: Number,
  },
  emits: ['event-modal-closed', 'event-commission-rate-saved'],
  data() {
    return {
      productTypes: [],
      selectedProductTypeId: null,
      feePerTransaction: null,
      feePercent: null,
      includesVat: false,
      validFrom: '',
      validTo: '',
      errorMessage: '',
    }
  },
  methods: {
    loadProductTypes() {
      ProductTypeService.sendGetProductTypes()
        .then((response) => {
          this.productTypes = response.data
        })
        .catch(() => {
          this.errorMessage = 'Midagi läks valesti, proovi uuesti'
        })
    },
    saveCommissionRate() {
      const userId = AuthService.getUserId()
      const commissionData = {
        productTypeId: this.selectedProductTypeId,
        feePerTransaction: this.feePerTransaction,
        feePercent: this.feePercent,
        includesVat: this.includesVat,
        validFrom: this.validFrom,
        validTo: this.validTo || null,
      }
      SellerService.sendPostSellerCommissionRate(this.sellerId, userId, commissionData)
        .then(() => this.$emit('event-commission-rate-saved'))
        .catch((error) => {
          this.errorMessage = error.response.data.message
        })
    },
  },
  beforeMount() {
    this.loadProductTypes()
  },
}
</script>