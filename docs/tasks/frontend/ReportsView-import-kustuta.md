# Frontend task: ReportsView — import + kustuta sektsioon

**Tüüp:** Frontend
**Staatus:** To Do
**Eeldab:** Backend task `POST-api-import-user-userId.md` ja `DELETE-api-import-user-userId-period.md` peavad olema valmis

---

## Kontekst

Luuakse `ReportsView.vue` — projektis on praegu `reportsRoute` placeholder (suunab `DashboardView`-sse). See task loob päris vaate ülemise osa: import sektsioon (vasakul) + kustuta sektsioon (paremal). Aruande ülevaate tabel tuleb eraldi taskis.

---

## Õppejõud on juba teinud — ÄRA tee uuesti

- `ReportService.js` — `sendPostImportReport(userId, file)` meetod on olemas
- `NavigationService.js` — `navigateToReportsView()` on olemas
- `App.vue` — navbar "Aruanded" link `reportsRoute`-le on olemas
- `router/index.js` — `reportsRoute` on olemas (praegu suunab `DashboardView`-sse — muuta selles taskis)
- `DashboardView.vue` — import UI on ajutiselt dashboardil — **vii üle ja eemalda sealt**

---

## Muudetavad failid

| Fail | Tegevus |
|------|---------|
| `frontend/src/views/ReportsView.vue` | **Loo uus** |
| `frontend/src/router/index.js` | Muuda `reportsRoute` component `DashboardView` → `ReportsView` |
| `frontend/src/views/DashboardView.vue` | **Eemalda** import kaart (template + data väljad + meetodid) |
| `frontend/src/api-services/ReportService.js` | Lisa `sendDeleteImportReport(userId, period)` meetod |

---

## 1. router/index.js — muuda reportsRoute

```javascript
// ENNE (praegune placeholder):
{ path: '/reports', name: 'reportsRoute', component: DashboardView }

// PÄRAST:
import ReportsView from '@/views/ReportsView.vue'
{ path: '/reports', name: 'reportsRoute', component: ReportsView }
```

---

## 2. DashboardView.vue — eemalda import kaart

Eemalda kolm asja:

**Template** — kustuta kogu `<div class="card mt-5">` plokk (import kaart)

**data()** — eemalda väljad:
```javascript
selectedFile: null,
showSpinner: false,
errorMessage: '',
successMessage: '',
```

**methods** — eemalda meetodid:
```javascript
handleFileChange(event) { ... }
importReport() { ... }
```

Samuti eemalda `import ReportService from '@/api-services/ReportService.js'` kui seda enam ei kasutata.

---

## 3. ReportService.js — lisa kustutamise meetod

```javascript
sendDeleteImportReport(userId, period) {
  return axios.delete('/api/import/user/' + userId + '/' + period)
}
```

`period` formaadis `"2026-4"` (YYYY-M) — täpselt nii nagu salvestatud andmebaasis.

---

## 4. ReportsView.vue — loo uus vaade

### Vaate struktuur

```
<div class="container">
  <h1>Aruanded</h1>
  
  <div class="row">
    <div class="col-6">  ← Import sektsioon
    <div class="col-6">  ← Kustuta sektsioon
  </div>
  
  <!-- Aruande ülevaade tabel tuleb järgmises taskis -->
</div>
```

### data() väljad

```javascript
data() {
  return {
    // Import
    selectedFile: null,
    showImportSpinner: false,
    importErrorMessage: '',
    importSuccessMessage: '',

    // Kustuta
    deleteMonth: new Date().getMonth() + 1,  // praegune kuu (1-12)
    deleteYear: new Date().getFullYear(),
    showDeleteSpinner: false,
    deleteErrorMessage: '',

    // Abiväärtused
    years: [],   // täidetakse beforeMount-is, nt [2024, 2025, 2026]
  }
},
```

### beforeMount

```javascript
beforeMount() {
  // Loo aastate valik: 2 aastat tagasi kuni järgmine aasta
  const currentYear = new Date().getFullYear()
  this.years = [currentYear - 2, currentYear - 1, currentYear, currentYear + 1]
},
```

### Import sektsioon — template

Kopeeri DashboardView-st import kaardi sisu (mis sealt eemaldatakse). Muuda ainult:
- `errorMessage` → `importErrorMessage`
- `successMessage` → `importSuccessMessage`
- `showSpinner` → `showImportSpinner`

### Import meetodid

Kopeeri DashboardView-st:
- `handleFileChange(event)` — muuda `this.errorMessage` → `this.importErrorMessage` jne
- `importReport()` — sama loogika, muuda muutujate nimed

`importReport()` edu korral: `this.importSuccessMessage = 'Aruanne edukalt imporditud!'`

### Kustuta sektsioon — template

```html
<div class="card">
  <div class="card-header"><strong>Kustuta müügiandmete fail</strong></div>
  <div class="card-body">
    <AlertError :error-message="deleteErrorMessage" />
    <div class="d-flex gap-2 align-items-center">
      <select class="form-select w-auto" v-model="deleteMonth">
        <option v-for="m in 12" :key="m" :value="m">{{ m }}</option>
      </select>
      <select class="form-select w-auto" v-model="deleteYear">
        <option v-for="y in years" :key="y" :value="y">{{ y }}</option>
      </select>
      <button class="btn btn-warning" :disabled="showDeleteSpinner" @click="confirmAndDelete">
        <span v-if="showDeleteSpinner" class="spinner-border spinner-border-sm me-2"></span>
        Kustuta
      </button>
    </div>
  </div>
</div>
```

### Kustuta meetodid

Perioodi formaadi moodustamine: `deleteYear + '-' + deleteMonth` → nt `"2026-4"`

```javascript
confirmAndDelete() {
  if (!window.confirm('Kas oled kindel, et soovid perioodi ' + this.deleteMonth + '.' + this.deleteYear + ' andmed kustutada?')) return
  this.showDeleteSpinner = true
  this.deleteErrorMessage = ''
  ReportService.sendDeleteImportReport(AuthService.getUserId(), this.deleteYear + '-' + this.deleteMonth)
    .then(() => { /* eduteade või lehe refresh */ })
    .catch((error) => { this.deleteErrorMessage = error.response?.data?.message ?? 'Kustutamine ebaõnnestus' })
    .finally(() => { this.showDeleteSpinner = false })
},
```

### Impordid ReportsView.vue ülaosas

```javascript
import AuthService from '@/auth/AuthService.js'
import ReportService from '@/api-services/ReportService.js'
import AlertError from '@/components/common/AlertError.vue'
import AlertSuccess from '@/components/common/AlertSuccess.vue'
```

---

## Vastuvõtu kriteeriumid

- [ ] `reportsRoute` suunab `ReportsView.vue`-sse (mitte enam `DashboardView`-sse)
- [ ] Navbar "Aruanded" link viib õigele vaatele
- [ ] Import kaart on **eemaldatud** `DashboardView.vue`-st
- [ ] Import toimib `ReportsView`-s: fail valitakse, "Laadi üles" saadab, success/error kuvatakse
- [ ] Kustuta sektsioon: kuu + aasta dropdown, "Kustuta" nupp
- [ ] Enne kustutamist kuvatakse kinnitusdialooog (`window.confirm`)
- [ ] Kustutamise 403 viga kuvatakse kasutajale
- [ ] Kustutamise 404 viga (periood ei eksisteeri) kuvatakse kasutajale
- [ ] Spinner nähtav nii impordi kui kustutamise ajal
- [ ] `sendDeleteImportReport` on lisatud `ReportService.js`-i