<template>
  <div class="container pt-5 mt-5">
    <h1 class="mb-5">
      Tere tulemast, {{ firstName }} <span v-if="middleName">{{ middleName }}&nbsp;</span
      >{{ lastName }}!
    </h1>
    <div class="row mt-3 align-items-stretch">
      <div class="col-6">
        <div class="card h-100" style="background-color: #e8eef4">
          <div class="card-body text-center">
            <p>Edasimüüjaid süsteemis</p>
            <h2 class="text-primary">{{ dashboard.sellerCount }}</h2>
          </div>
        </div>
      </div>
      <div class="col-6">
        <div class="card h-100" style="background-color: #e8eef4">
          <div class="card-body text-center">
            <p>Viimane import</p>
            <h2 class="text-primary">{{ dashboard.lastImport }}</h2>
          </div>
        </div>
      </div>
    </div>

    <div class="card mt-5">
      <div class="card-header">
        <strong>Impordi müügiaruanne</strong>
      </div>
      <div class="card-body">
        <div v-if="errorMessage" class="alert alert-danger">{{ errorMessage }}</div>
        <div v-if="successMessage" class="alert alert-success">{{ successMessage }}</div>
        <div class="d-flex align-items-center gap-3">
          <input
            type="file"
            accept=".xlsx"
            class="form-control w-auto"
            @change="handleFileChange"
          />
          <button
            class="btn btn-primary"
            :disabled="!selectedFile || showSpinner"
            @click="importReport"
          >
            <span v-if="showSpinner" class="spinner-border spinner-border-sm me-2"></span>
            Laadi üles
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import AuthService from '@/auth/AuthService.js'
import DashboardService from '@/api-services/DashboardService.js'
import ReportService from '@/api-services/ReportService.js'

export default {
  name: 'DashboardView',
  data() {
    return {
      firstName: AuthService.getFirstName(),
      middleName: AuthService.getMiddleName(),
      lastName: AuthService.getLastName(),
      dashboard: {
        sellerCount: 0,
        lastImport: null,
      },
      selectedFile: null,
      showSpinner: false,
      errorMessage: '',
      successMessage: '',
    }
  },
  methods: {
    getDashboard() {
      DashboardService.sendGetDashboard()
        .then((response) => {
          this.dashboard = response.data
        })
        .catch((error) => {
          console.log(error)
        })
    },
    handleFileChange(event) {
      this.selectedFile = event.target.files[0] ?? null
      this.errorMessage = ''
      this.successMessage = ''
    },
    importReport() {
      this.showSpinner = true
      this.errorMessage = ''
      this.successMessage = ''
      ReportService.sendPostImportReport(AuthService.getUserId(), this.selectedFile)
        .then(() => {
          this.successMessage = 'Aruanne edukalt imporditud!'
          this.selectedFile = null
          this.getDashboard()
        })
        .catch((error) => {
          this.errorMessage = error.response?.data?.message ?? 'Import ebaõnnestus'
        })
        .finally(() => {
          this.showSpinner = false
        })
    },
  },
  beforeMount() {
    this.getDashboard()
  },
}
</script>
