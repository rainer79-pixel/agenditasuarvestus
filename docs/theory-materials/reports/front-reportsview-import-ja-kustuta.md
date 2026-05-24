# Task-07 Frontend: ReportsView — import ja kustutamine

## Mis see on ja miks oluline?

See task loob `ReportsView.vue` — vaate kus kasutaja saab laadida üles Excel müügiaruande ja kustutada konkreetse perioodi andmed.

**Päriselust analoogia:** Import on nagu dokumendi skännimine arhiivi — saadad faili serverisse. Kustutamine on nagu arhiivist ühe kuu kausta eemaldamine — ütled mis kuu, server kustutab.

---

## Mis on selles taskis uut võrreldes eelmiste taskidega?

| Uus asi | Kus näed | Mida tähendab |
|---------|----------|---------------|
| **`FormData`** | `ReportService.js` | Viis faili (Excel) saatmiseks HTTP päringuga — tavaline JSON selleks ei sobi |
| **`multipart/form-data`** | `ReportService.js` headers | Ütleb backendile et saadame faili, mitte JSON-i |
| **`window.confirm()`** | `ReportsView.vue` | Brauser avab hüpikakna kinnituseks enne ohtlikku tegevust |
| **`deleteSuccessMessage`** | `ReportsView.vue` | Eraldi eduteade kustutamisele — sama muster mis importimisel |

---

## Andmevoog — suur pilt

```
BRAUSER
  ↓ kasutaja valib .xlsx faili
  ↓ klikib "Laadi üles"
ReportsView.vue  →  ReportService.js  →  POST /api/import/user/3
                                               ↓
                                         Backend salvestab
                                         sales_report + sales_report_detail
                                               ↓
                                         200 OK / veateade
  ↓ kasutaja valib kuu=4, aasta=2026
  ↓ klikib "Kustuta" → window.confirm()
ReportsView.vue  →  ReportService.js  →  DELETE /api/import/user/3/2026-4
                                               ↓
                                         Backend kustutab read
                                         kus period='2026-4'
                                               ↓
                                         200 OK / veateade
```

---

## ReportService.js — kaks meetodit

```javascript
// Import — saadame faili FormData ümbrikus
sendPostImportReport(userId, file) {
  const formData = new FormData()
  formData.append('file', file)         // ← Excel fail pakitakse ümbrikusse

  return axios.post('/api/import/user/' + userId, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    //                          ↑ ütleb backendile: saadetakse fail, mitte JSON
  })
},

// Kustutamine — saadame ainult URL-is userId ja period
sendDeleteImportReport(userId, period) {
  return axios.delete('/api/import/user/' + userId + '/' + period)
  //                                                        ↑
  //                                                 nt '2026-4'
},
```

**Miks `FormData`, mitte JSON?**
Excel fail on binaarne objekt — suur hulk nulle ja ühtesid. JSON on tekstipõhine ja ei sobi suurte failide saatmiseks. `FormData` on selleks loodud konteiner.

**Miks perioodi formaat `"2026-4"` mitte `"04.2026"`?**
Backend salvestab perioodi täpselt sellises formaadis `sales_report_detail` tabelisse. Kustutamisel peame saatma täpselt sama formaadi, et backend leiaks õiged read üles.

---

## ReportsView.vue — import meetod

```javascript
importReport() {
  this.showImportSpinner = true    // ← nupp läheb disabled (ei saa topelt vajutada)
  this.importErrorMessage = ''     // ← tühjendame vana vea

  ReportService.sendPostImportReport(AuthService.getUserId(), this.selectedFile)

    .then(() => {
      this.importSuccessMessage = 'Aruanne edukalt imporditud!'
      this.selectedFile = null     // ← tühjendame faili valiku
    })
    .catch((error) => {
      this.importErrorMessage = error.response?.data?.message ?? 'Import ebaõnnestus'
      //                                      ↑
      //                        backendi veateade (nt "Periood juba olemas")
    })
    .finally(() => {
      this.showImportSpinner = false  // ← käivitub ALATI — nii õnnestumisel kui veal
    })
},
```

**Miks `.finally()` eraldi?**
`.finally()` käivitub alati — nii `.then()` kui `.catch()` järel. Kui paneksime `showImportSpinner = false` mõlemasse eraldi, peaksime seda kahes kohas hoidma. Kui ühe unustame, jääb spinner igavesti lahti.

---

## ReportsView.vue — kustutamise meetod

```javascript
confirmAndDelete() {
  // Enne kustutamist küsib kasutajalt kinnitust
  if (!window.confirm('Kas oled kindel, et soovid perioodi 4.2026 andmed kustutada?')) return
  //                   ↑ brauser avab hüpikakna
  //   "Tühista" → return (meetod lõpeb siia, midagi ei kustutata)
  //   "OK"      → jätkame

  this.showDeleteSpinner = true
  this.deleteErrorMessage = ''
  this.deleteSuccessMessage = ''

  ReportService.sendDeleteImportReport(AuthService.getUserId(), this.deleteYear + '-' + this.deleteMonth)
  //                                                             ↑ nt '2026' + '-' + '4' = '2026-4'

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
```

---

## Nupu disabled loogika

```html
<!-- Import nupp -->
<button :disabled="!selectedFile || showImportSpinner" @click="importReport">

<!-- Kustuta nupp -->
<button :disabled="showDeleteSpinner || !deleteMonth || !deleteYear" @click="confirmAndDelete">
```

| Tingimus | Miks |
|----------|------|
| `!selectedFile` | Faili pole valitud — pole mida saata |
| `showImportSpinner` | Päring käib — ei saa topelt saata |
| `!deleteMonth` | Kuu pole valitud |
| `!deleteYear` | Aasta pole valitud |

---

## Vea kuvamise muster

Veateade jääb ekraanile — kasutaja näeb mis läks valesti, parandab valiku ja proovib uuesti. Järgmisel katsel tühjendatakse vana veateade meetodi alguses (`deleteErrorMessage = ''`).

---

## Kokkuvõte

| Mõiste | Selgitus |
|--------|----------|
| `FormData` | Konteiner faili saatmiseks HTTP päringuga |
| `multipart/form-data` | Content-Type mis ütleb backendile et tuleb fail |
| `window.confirm()` | Brauser küsib kasutajalt kinnitust hüpikaknas |
| `.finally()` | Käivitub alati — kasutatakse spinneri sulgemiseks |
| `period = '2026-4'` | Perioodi formaat — täpselt nii nagu andmebaasis |

---

## Järgmised sammud

- **Task-08 frontend** — aruannete tabel `ReportsView.vue`-s (GET `/api/report/user/{userId}`)
- **Task-09 frontend** — ekspordi nupp (GET `/api/report/user/{userId}/export`)