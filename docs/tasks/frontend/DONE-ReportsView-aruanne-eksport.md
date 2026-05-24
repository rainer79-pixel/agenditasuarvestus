# Frontend task: ReportsView — aruande ülevaade + eksport

**Tüüp:** Frontend
**Staatus:** To Do
**Eeldab:**
- Frontend task `ReportsView-import-kustuta.md` peab olema valmis (`ReportsView.vue` olemas)
- Backend taskid `GET-api-report-user-userId.md`, `GET-api-report-sellerId-period.md`, `GET-api-report-user-userId-export.md` peavad olema valmis

---

## Kontekst

Lisatakse `ReportsView.vue`-sse (loodud eelmises taskis) aruande ülevaade: filtrisektsioon, tulemuste tabel laiendatavate ridadega ja ekspordi nupp. Kasutaja valib perioodi vahemiku ja/või edasimüüja, vajutab "Otsi" — tabel täidetakse. "Vaata" nupuga laiendatakse üks rida tootegruppide detailvaateks. "Ekspordi XLS" ekspordib kogu hetkel kuvatud tabeli.

---

## Muudetavad failid

| Fail | Tegevus |
|------|---------|
| `frontend/src/views/ReportsView.vue` | Lisa filter + tabel + detail + eksport |
| `frontend/src/api-services/ReportService.js` | Lisa 3 uut meetodit |

---

## 1. ReportService.js — lisa meetodid

```javascript
sendGetReports(userId, periodFrom, periodTo, sellerId) {
  return axios.get('/api/report/user/' + userId, {
    params: { periodFrom, periodTo, sellerId },
  })
},

sendGetReportDetail(sellerId, period) {
  return axios.get('/api/report/' + sellerId + '/' + period)
},

sendGetReportExport(userId, periodFrom, periodTo, sellerId) {
  return axios.get('/api/report/user/' + userId + '/export', {
    params: { periodFrom, periodTo, sellerId },
    responseType: 'blob',   // ← OLULINE: failide allalaadimiseks
  })
},
```

> **Märkus `responseType: 'blob'`:** Tavalise JSON päringu puhul Axios teisendab vastuse automaatselt objektiks. Faili allalaadimiseks peab vastus jääma binaariks — selleks `responseType: 'blob'`.

---

## 2. data() — lisa filter + tabel + detail väljad

```javascript
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

// Laiendatav detail rida
expandedKey: null,        // 'sellerId-period' string, nt '1-2026-4'
reportDetails: [],
detailErrorMessage: '',
showDetailSpinner: false,

// Eksport
exportErrorMessage: '',
showExportSpinner: false,
```

---

## 3. beforeMount — lae eelmise kuu andmed vaikimisi

```javascript
beforeMount() {
  // Aastate valik (lisatud juba eelmises taskis — kontrolli, et olemas)
  const currentYear = new Date().getFullYear()
  this.years = [currentYear - 2, currentYear - 1, currentYear, currentYear + 1]

  // Vaikimisi: eelmine kuu
  this.setLastMonth()
  this.loadReports()
  this.loadSellers()
},
```

---

## 4. Meetodid

### Müüjate laadimine (edasimüüja filter dropdown jaoks)

```javascript
loadSellers() {
  SellerService.sendGetSellers(AuthService.getUserId())
    .then((response) => { this.sellers = response.data })
    .catch(() => {})  // dropdown tühjena OK, mitte kriitline
},
```

### Perioodivaliku abimeetodid

```javascript
setLastMonth() {
  const d = new Date()
  d.setMonth(d.getMonth() - 1)
  this.fromMonth = d.getMonth() + 1
  this.fromYear = d.getFullYear()
  this.toMonth = d.getMonth() + 1
  this.toYear = d.getFullYear()
},

setLastQuarter() {
  const d = new Date()
  const currentQuarter = Math.floor(d.getMonth() / 3)
  const prevQuarterStart = (currentQuarter - 1) * 3  // 0-indekseeritud kuu
  let startYear = d.getFullYear()
  let startMonth = prevQuarterStart  // 0-indekseeritud

  if (prevQuarterStart < 0) {
    // Kui praegune kvartal on Q1, siis eelmine kvartal on eelmise aasta Q4
    startYear = d.getFullYear() - 1
    startMonth = 9  // oktoober (0-indekseeritud)
  }

  this.fromMonth = startMonth + 1
  this.fromYear = startYear
  this.toMonth = startMonth + 3  // kvartali viimane kuu (1-indekseeritud)
  this.toYear = startYear
},
```

### Aruannete laadimine

```javascript
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
```

### Detail rea avamine/sulgemine

```javascript
toggleDetail(sellerId, period) {
  const key = sellerId + '-' + period
  if (this.expandedKey === key) {
    this.expandedKey = null  // sulge
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
```

### Eksport (faili allalaadimine brauseris)

```javascript
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
```

> **Selgitus:** `createObjectURL` loob ajutise URL-i mälus oleva failile → `link.click()` käivitab brauseri allalaadimisdialooogi → `revokeObjectURL` vabastab mälu.

---

## 5. computed — KOKKU rida

KOKKU rida arvutatakse frontendis massiivist (mitte backendist):

```javascript
computed: {
  totals() {
    return {
      transactionCount: this.reports.reduce((s, r) => s + r.transactionCount, 0),
      salesAmount:      this.reports.reduce((s, r) => s + r.salesAmount, 0),
      feeAmount:        this.reports.reduce((s, r) => s + r.feeAmount, 0),
      vatAmount:        this.reports.reduce((s, r) => s + r.vatAmount, 0),
      totalFee:         this.reports.reduce((s, r) => s + r.totalFee, 0),
    }
  },
},
```

---

## 6. Template struktuur

### Filtrisektsioon

```html
<div class="row mt-4 align-items-end">
  <div class="col-auto">
    <label>Kuu alates</label>
    <select class="form-select" v-model="fromMonth">
      <option :value="null">--</option>
      <option v-for="m in 12" :key="m" :value="m">{{ m }}</option>
    </select>
  </div>
  <div class="col-auto">
    <label>Aasta alates</label>
    <select class="form-select" v-model="fromYear">
      <option :value="null">--</option>
      <option v-for="y in years" :key="y" :value="y">{{ y }}</option>
    </select>
  </div>
  <!-- kuu kuni + aasta kuni samad -->
  <div class="col-auto">
    <label>Edasimüüja</label>
    <select class="form-select" v-model="selectedSellerId">
      <option :value="null">Kõik</option>
      <option v-for="s in sellers" :key="s.sellerId" :value="s.sellerId">{{ s.companyName }}</option>
    </select>
  </div>
  <div class="col-auto d-flex gap-2">
    <button class="btn btn-primary" @click="loadReports">Otsi</button>
    <button class="btn btn-outline-secondary" @click="setLastMonth(); loadReports()">Eelmine kuu</button>
    <button class="btn btn-outline-secondary" @click="setLastQuarter(); loadReports()">Eelmine kvartal</button>
  </div>
</div>
```

### Tabel

```html
<AlertError :error-message="reportsErrorMessage" />
<table class="table mt-3" v-if="reports.length > 0">
  <thead>
    <tr>
      <th>Edasimüüja</th>
      <th>Tehinguid</th>
      <th>Müügisumma</th>
      <th>Teenustasu</th>
      <th>KM</th>
      <th>Kokku</th>
      <th>Tegevused</th>
    </tr>
  </thead>
  <tbody>
    <template v-for="r in reports" :key="r.sellerId + '-' + r.period">
      <!-- Põhirida -->
      <tr>
        <td>{{ r.companyName }}</td>
        <td>{{ r.transactionCount }}</td>
        <td>{{ r.salesAmount }} EUR</td>
        <td>{{ r.feeAmount }} EUR</td>
        <td>{{ r.vatAmount }} EUR</td>
        <td>{{ r.totalFee }} EUR</td>
        <td>
          <button class="btn btn-sm btn-outline-primary"
                  @click="toggleDetail(r.sellerId, r.period)">
            {{ expandedKey === r.sellerId + '-' + r.period ? 'Sulge' : 'Vaata' }}
          </button>
        </td>
      </tr>
      <!-- Laiendatav detailrida -->
      <tr v-if="expandedKey === r.sellerId + '-' + r.period">
        <td colspan="7">
          <div v-if="showDetailSpinner" class="spinner-border spinner-border-sm"></div>
          <AlertError :error-message="detailErrorMessage" />
          <table class="table table-sm" v-if="reportDetails.length > 0">
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
    <!-- KOKKU rida -->
    <tr class="fw-bold" v-if="reports.length > 0">
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

<!-- Ekspordi nupp -->
<div class="mt-3" v-if="reports.length > 0">
  <AlertError :error-message="exportErrorMessage" />
  <button class="btn btn-success" :disabled="showExportSpinner" @click="exportReport">
    <span v-if="showExportSpinner" class="spinner-border spinner-border-sm me-2"></span>
    Ekspordi XLS
  </button>
</div>
```

### Impordid mis lisanduvad ReportsView.vue-sse

```javascript
import SellerService from '@/api-services/SellerService.js'
```

(ReportService ja AuthService on juba eelmisest taskist)

---

## Vastuvõtu kriteeriumid

- [ ] Filtrisektsioon: kuu alates, aasta alates, kuu kuni, aasta kuni, edasimüüja dropdown
- [ ] "Otsi" nupp laadib andmed backendist
- [ ] "Eelmine kuu" nupp seab filtri ja laadib automaatselt
- [ ] "Eelmine kvartal" nupp seab filtri ja laadib automaatselt
- [ ] Vaikimisi laaditakse eelmise kuu andmed `beforeMount`-is
- [ ] Tabel kuvab read: edasimüüja, tehinguid, müügisumma, teenustasu, KM, kokku
- [ ] KOKKU rida on tabel lõpus, arvutatud frontendis
- [ ] "Vaata" nupp laiendab rea — kuvatakse tootegruppide detailtabel
- [ ] Sama rea "Vaata" vajutamine uuesti sulgeb detailrea
- [ ] Erinevale reale "Vaata" vajutamine sulgeb eelmise ja avab uue
- [ ] 404 viga (andmed puuduvad) kuvatakse kasutajasõbraliku teatena (mitte navigeeri error vaatesse)
- [ ] "Ekspordi XLS" nupp on nähtav ainult siis kui tabelis on andmeid
- [ ] Eksport käivitab brauseri allalaadimisdialooogi `.xlsx` failiga
- [ ] `responseType: 'blob'` on `sendGetReportExport` meetodis
- [ ] Kõik spinnerid toimivad (import, kustuta, detail laadimine, eksport)