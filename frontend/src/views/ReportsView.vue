<template>
  <div class="container pt-4" @click="clearAllMessages">
    <h2>Aruanded</h2>

    <div class="row mt-4">
      <div class="col-6">
        <div class="card">
          <div class="card-header">
            <strong>Laadi üles müügiaruanne</strong>
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

    <!-- Filter -->
    <div class="row mt-4 align-items-end g-2">
      <div class="col-auto">
        <label class="form-label">Kuu alates</label>
        <select class="form-select" v-model="fromMonth" @change="activePreset = null">
          <option :value="null">--</option>
          <option v-for="m in 12" :key="m" :value="m">{{ m }}</option>
        </select>
      </div>
      <div class="col-auto">
        <label class="form-label">Aasta alates</label>
        <select class="form-select" v-model="fromYear" @change="activePreset = null">
          <option :value="null">--</option>
          <option v-for="y in years" :key="y" :value="y">{{ y }}</option>
        </select>
      </div>
      <div class="col-auto">
        <label class="form-label">Kuu kuni</label>
        <select class="form-select" v-model="toMonth" @change="activePreset = null">
          <option :value="null">--</option>
          <option v-for="m in 12" :key="m" :value="m">{{ m }}</option>
        </select>
      </div>
      <div class="col-auto">
        <label class="form-label">Aasta kuni</label>
        <select class="form-select" v-model="toYear" @change="activePreset = null">
          <option :value="null">--</option>
          <option v-for="y in years" :key="y" :value="y">{{ y }}</option>
        </select>
      </div>
      <div class="col-auto">
        <label class="form-label">Edasimüüja</label>
        <select class="form-select" v-model="selectedSellerId" @change="activePreset = null">
          <option :value="null">Kõik</option>
          <option v-for="s in sellers" :key="s.sellerId" :value="s.sellerId">{{ s.companyName }}</option>
        </select>
      </div>
      <div class="col-auto d-flex gap-2">
        <button class="btn btn-primary" @click="loadReports">Otsi</button>
        <button class="btn" :class="activePreset === 'lastMonth' ? 'btn-secondary' : 'btn-outline-secondary'" @click="setLastMonth(); loadReports()">Eelmine kuu</button>
        <button class="btn" :class="activePreset === 'lastThreeMonths' ? 'btn-secondary' : 'btn-outline-secondary'" @click="setLastThreeMonths(); loadReports()">Viimased 3 kuud</button>
      </div>
    </div>

    <!-- Tabel -->
    <div v-if="reportsErrorMessage" class="alert alert-danger mt-3">{{ reportsErrorMessage }}</div>
    <table class="table mt-3" v-if="reports.length > 0">
      <thead>
        <tr>
          <th>Edasimüüja</th>
          <th>Tehinguid</th>
          <th>Müügisumma</th>
          <th>Teenustasu</th>
          <th>KM</th>
          <th>Kokku</th>
          <th></th>
        </tr>
      </thead>
      <tbody>
        <template v-for="r in reports" :key="r.sellerId + '-' + r.period">
          <tr>
            <td>{{ r.companyName }}</td>
            <td>{{ r.transactionCount }}</td>
            <td>{{ r.salesAmount }} EUR</td>
            <td>{{ r.feeAmount }} EUR</td>
            <td>{{ r.vatAmount }} EUR</td>
            <td>{{ r.totalFee }} EUR</td>
            <td>
              <button class="btn btn-sm btn-outline-primary" @click="toggleDetail(r.sellerId, r.period)">
                {{ expandedKey === r.sellerId + '-' + r.period ? 'Sulge' : 'Vaata' }}
              </button>
            </td>
          </tr>
          <tr v-if="expandedKey === r.sellerId + '-' + r.period">
            <td colspan="7" class="bg-light">
              <div v-if="showDetailSpinner" class="spinner-border spinner-border-sm my-2"></div>
              <div v-if="detailErrorMessage" class="alert alert-danger">{{ detailErrorMessage }}</div>
              <table class="table table-sm mb-0" v-if="reportDetails.length > 0">
                <thead>
                  <tr>
                    <th>Tootegrupp</th>
                    <th>Tehinguid</th>
                    <th>Müügisumma</th>
                    <th>Tasu %</th>
                    <th>Tasu koguselt</th>
                    <th>Teenustasu</th>
                    <th>KM</th>
                    <th>Kokku</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="d in reportDetails" :key="d.productTypeName">
                    <td>{{ d.productTypeName }}</td>
                    <td>{{ d.transactionCount }}</td>
                    <td>{{ d.salesAmount }}</td>
                    <td>{{ d.feePercent ?? '-' }}</td>
                    <td>{{ d.feePerTransaction ?? '-' }}</td>
                    <td>{{ d.calculatedFee }}</td>
                    <td>{{ d.vatAmount }}</td>
                    <td>{{ d.totalFee }}</td>
                  </tr>
                </tbody>
              </table>
            </td>
          </tr>
        </template>
        <tr class="fw-bold">
          <td>KOKKU</td>
          <td>{{ totals.transactionCount }}</td>
          <td>{{ totals.salesAmount }} EUR</td>
          <td>{{ totals.feeAmount }} EUR</td>
          <td>{{ totals.vatAmount }} EUR</td>
          <td>{{ totals.totalFee }} EUR</td>
          <td></td>
        </tr>
      </tbody>
    </table>

    <!-- Eksport -->
    <div class="mt-3" v-if="reports.length > 0">
      <div v-if="exportErrorMessage" class="alert alert-danger">{{ exportErrorMessage }}</div>
      <button class="btn btn-success" :disabled="showExportSpinner" @click="exportReport">
        <span v-if="showExportSpinner" class="spinner-border spinner-border-sm me-2"></span>
        Ekspordi XLS
      </button>
    </div>
  </div>
</template>

<script>
import AuthService from '@/auth/AuthService.js'
import ReportService from '@/api-services/ReportService.js'
import SellerService from '@/api-services/SellerService.js'

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

      // Filter
      fromMonth: null,
      fromYear: null,
      toMonth: null,
      toYear: null,
      selectedSellerId: null,
      sellers: [],

      // Tabel
      reports: [],
      reportsErrorMessage: '',

      // Detail rida
      expandedKey: null,
      reportDetails: [],
      detailErrorMessage: '',
      showDetailSpinner: false,

      // Eksport
      exportErrorMessage: '',
      showExportSpinner: false,

      // Abiväärtused
      years: [],
      activePreset: null,
    }
  },
  computed: {
    totals() {
      return {
        transactionCount: this.reports.reduce((s, r) => s + r.transactionCount, 0),
        salesAmount: this.reports.reduce((s, r) => s + r.salesAmount, 0).toFixed(2),
        feeAmount: this.reports.reduce((s, r) => s + r.feeAmount, 0).toFixed(2),
        vatAmount: this.reports.reduce((s, r) => s + r.vatAmount, 0).toFixed(2),
        totalFee: this.reports.reduce((s, r) => s + r.totalFee, 0).toFixed(2),
      }
    },
  },
  methods: {
    clearAllMessages() {
      this.importErrorMessage = ''
      this.importSuccessMessage = ''
      this.deleteErrorMessage = ''
      this.deleteSuccessMessage = ''
      this.reportsErrorMessage = ''
      this.exportErrorMessage = ''
      this.detailErrorMessage = ''
    },
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
    loadSellers() {
      SellerService.sendGetSellers(AuthService.getUserId())
        .then((response) => { this.sellers = response.data })
        .catch(() => {})
    },
    setLastMonth() {
      const d = new Date()
      d.setMonth(d.getMonth() - 1)
      this.fromMonth = d.getMonth() + 1
      this.fromYear = d.getFullYear()
      this.toMonth = null
      this.toYear = null
      this.activePreset = 'lastMonth'
    },
    setLastThreeMonths() {
      const to = new Date()
      to.setMonth(to.getMonth() - 1)
      const from = new Date(to.getFullYear(), to.getMonth() - 2, 1)
      this.fromMonth = from.getMonth() + 1
      this.fromYear = from.getFullYear()
      this.toMonth = to.getMonth() + 1
      this.toYear = to.getFullYear()
      this.activePreset = 'lastThreeMonths'
    },
    loadReports() {
      this.reportsErrorMessage = ''
      this.reports = []
      this.expandedKey = null

      const periodFrom = this.fromYear && this.fromMonth ? this.fromYear + '-' + this.fromMonth : null
      const periodTo = this.toYear && this.toMonth ? this.toYear + '-' + this.toMonth : null

      ReportService.sendGetReports(AuthService.getUserId(), periodFrom, periodTo, this.selectedSellerId)
        .then((response) => { this.reports = response.data })
        .catch((error) => {
          if (error.response?.status === 404) {
            this.reportsErrorMessage = 'Valitud perioodil andmed puuduvad'
          } else {
            this.reportsErrorMessage = 'Andmete laadimine ebaõnnestus'
          }
        })
    },
    toggleDetail(sellerId, period) {
      const key = sellerId + '-' + period
      if (this.expandedKey === key) {
        this.expandedKey = null
        return
      }
      this.expandedKey = key
      this.reportDetails = []
      this.detailErrorMessage = ''
      this.showDetailSpinner = true

      ReportService.sendGetReportDetail(sellerId, period)
        .then((response) => { this.reportDetails = response.data })
        .catch(() => { this.detailErrorMessage = 'Detailide laadimine ebaõnnestus' })
        .finally(() => { this.showDetailSpinner = false })
    },
    exportReport() {
      this.showExportSpinner = true
      this.exportErrorMessage = ''

      const periodFrom = this.fromYear && this.fromMonth ? this.fromYear + '-' + this.fromMonth : null
      const periodTo = this.toYear && this.toMonth ? this.toYear + '-' + this.toMonth : null

      ReportService.sendGetReportExport(AuthService.getUserId(), periodFrom, periodTo, this.selectedSellerId)
        .then((response) => {
          const blob = new Blob([response.data], {
            type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
          })
          const url = window.URL.createObjectURL(blob)
          const link = document.createElement('a')
          link.href = url
          link.setAttribute('download', 'aruanne.xlsx')
          document.body.appendChild(link)
          link.click()
          document.body.removeChild(link)
          window.URL.revokeObjectURL(url)
        })
        .catch(() => { this.exportErrorMessage = 'Eksport ebaõnnestus' })
        .finally(() => { this.showExportSpinner = false })
    },
  },
  beforeMount() {
    const currentYear = new Date().getFullYear()
    this.years = Array.from({ length: 6 }, (_, i) => currentYear - i)
    this.setLastMonth()
    this.loadReports()
    this.loadSellers()
  },
}
</script>
