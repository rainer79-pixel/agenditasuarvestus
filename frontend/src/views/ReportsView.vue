<template>
  <div class="container pt-4">
    <h2>Aruanded</h2>

    <div class="row mt-4">
      <div class="col-6">
        <div class="card">
          <div class="card-header">
            <strong>Impordi müügiaruanne</strong>
          </div>
          <div class="card-body">
            <div v-if="importErrorMessage" class="alert alert-danger">{{ importErrorMessage }}</div>
            <div v-if="importSuccessMessage" class="alert alert-success">{{ importSuccessMessage }}</div>
            <div class="d-flex align-items-center gap-3">
              <input
                type="file"
                accept=".xlsx"
                class="form-control w-auto"
                @change="handleFileChange"
              />
              <button
                class="btn btn-primary"
                :disabled="!selectedFile || showImportSpinner"
                @click="importReport"
              >
                <span v-if="showImportSpinner" class="spinner-border spinner-border-sm me-2"></span>
                Laadi üles
              </button>
            </div>
          </div>
        </div>
      </div>
      <div class="col-6">
        <div class="card">
          <div class="card-header"><strong>Kustuta müügiandmete fail</strong></div>
          <div class="card-body">
            <div v-if="deleteSuccessMessage" class="alert alert-success">{{ deleteSuccessMessage }}</div>
            <div v-if="deleteErrorMessage" class="alert alert-danger">{{ deleteErrorMessage }}</div>
            <div class="d-flex gap-2 align-items-center">
              <select class="form-select w-auto" v-model="deleteMonth">
                <option value="" disabled>-- kuu --</option>
                <option v-for="m in 12" :key="m" :value="m">{{ m }}</option>
              </select>
              <select class="form-select w-auto" v-model="deleteYear">
                <option value="" disabled>-- aasta --</option>
                <option v-for="y in years" :key="y" :value="y">{{ y }}</option>
              </select>
              <button class="btn btn-danger" :disabled="showDeleteSpinner || !deleteMonth || !deleteYear" @click="confirmAndDelete">
                <span v-if="showDeleteSpinner" class="spinner-border spinner-border-sm me-2"></span>
                Kustuta
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Aruande ülevaade + eksport — task: ReportsView-aruanne-eksport -->
  </div>
</template>

<script>
import AuthService from '@/auth/AuthService.js'
import ReportService from '@/api-services/ReportService.js'

export default {
  name: 'ReportsView',
  data() {
    return {
      // Import
      selectedFile: null,
      showImportSpinner: false,
      importErrorMessage: '',
      importSuccessMessage: '',

      // Kustuta
      deleteMonth: '',
      deleteYear: '',
      showDeleteSpinner: false,
      deleteErrorMessage: '',
      deleteSuccessMessage: '',

      // Abiväärtused
      years: [],
    }
  },
  methods: {
    handleFileChange(event) {
      this.selectedFile = event.target.files[0] ?? null
      this.importErrorMessage = ''
      this.importSuccessMessage = ''
    },
    importReport() {
      this.showImportSpinner = true
      this.importErrorMessage = ''
      this.importSuccessMessage = ''
      ReportService.sendPostImportReport(AuthService.getUserId(), this.selectedFile)
        .then(() => {
          this.importSuccessMessage = 'Aruanne edukalt imporditud!'
          this.selectedFile = null
        })
        .catch((error) => {
          this.importErrorMessage = error.response?.data?.message ?? 'Import ebaõnnestus'
        })
        .finally(() => {
          this.showImportSpinner = false
        })
    },
    confirmAndDelete() {
      if (!window.confirm('Kas oled kindel, et soovid perioodi ' + this.deleteMonth + '.' + this.deleteYear + ' andmed kustutada?')) return
      this.showDeleteSpinner = true
      this.deleteErrorMessage = ''
      this.deleteSuccessMessage = ''
      ReportService.sendDeleteImportReport(AuthService.getUserId(), this.deleteYear + '-' + this.deleteMonth)
        .then(() => {
          this.deleteSuccessMessage = 'Perioodi andmed kustutatud.'
        })
        .catch((error) => {
          this.deleteErrorMessage = error.response?.data?.message ?? 'Kustutamine ebaõnnestus'
        })
        .finally(() => {
          this.showDeleteSpinner = false
        })
    },
  },
  beforeMount() {
    const currentYear = new Date().getFullYear()
    this.years = Array.from({ length: 6 }, (_, i) => currentYear - i)
  },
}
</script>