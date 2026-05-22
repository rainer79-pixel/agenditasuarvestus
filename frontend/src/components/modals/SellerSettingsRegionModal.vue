<template>
  <div class="modal d-block">
    <div class="modal-dialog">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">Lisa piirkond</h5>
          <button type="button" class="btn-close" @click="$emit('event-modal-closed')"></button>
        </div>
        <div class="modal-body">
          <div v-if="errorMessage" class="alert alert-danger">{{ errorMessage }}</div>
          <div class="mb-3">
            <label class="form-label">Piirkond</label>
            <select class="form-select" v-model="selectedRegionId">
              <option :value="null" disabled>Vali piirkond</option>
              <option v-for="region in regions" :key="region.regionId" :value="region.regionId">
                {{ region.regionName }}
              </option>
            </select>
          </div>
          <div class="mb-3">
            <label class="form-label">Müügipunkte</label>
            <input type="number" class="form-control" v-model="salesPointCount" min="0" />
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" @click="$emit('event-modal-closed')">Tühista</button>
          <button class="btn btn-success" @click="saveRegion()">Lisa</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped></style>

<script>
import RegionService from '@/api-services/RegionService.js'
import SellerService from '@/api-services/SellerService.js'
import AuthService from '@/auth/AuthService.js'

export default {
  name: 'SellerSettingsRegionModal',
  props: {
    sellerId: Number,
  },
  emits: ['event-modal-closed', 'event-region-saved'],

  data() {
    return {
      regions: [],
      selectedRegionId: null,
      salesPointCount: 1,
      errorMessage: '',
    }
  },
  methods: {
    loadRegions() {
      RegionService.sendGetRegions()
        .then((response) => {
          this.regions = response.data
        })
        .catch(() => {
          this.errorMessage = 'Midagi läks valesti, proovi uuesti'
        })
    },
    saveRegion() {
      const userId = AuthService.getUserId()
      const regionData = {
        regionId: this.selectedRegionId,
        salesPointCount: this.salesPointCount,
      }
      SellerService.sendPostSellerRegion(this.sellerId, userId, regionData)
        .then(() => this.$emit('event-region-saved'))
        .catch((error) => {
          this.errorMessage = error.response.data.message
        })
    },
  },
  beforeMount() {
    this.loadRegions()
  },
}
</script>
