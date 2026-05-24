# Frontend: ReportsView — UI parandused ja Vue mustrid

## Mis see on ja miks oluline?

See materjal katab kolm Vue.js mustrit, mida kasutasime ReportsView aruannete vaates:
1. Numbrite kokkuarvutamine ja vormindamine (`computed` + `toFixed`)
2. Kõigi teadete korraga puhastamine (`clearAllMessages` + event bubbling)
3. Aktiivse nupu seisundi jälgimine (`activePreset`)

**Päriselust analoogia:** Need on nagu armatuurlaua liikuvad osad — igaüks lahendab ühe konkreetse kasutajakogemuse probleemi.

---

## 1. `computed` + `reduce` + `toFixed(2)` — summade arvutamine

### Probleem

Aruannete tabelis on KOKKU rida — kõigi sellerite summad kokku. Numbrid tulevad backendist `BigDecimal`-ina, JavaScript lisab need `+` operaatoriga — aga JavaScript kasutab ujukomaarvutust, mis võib anda:

```
1.1 + 2.2 = 3.3000000000000003  ← vale, kaks kohta pärast koma piisab
```

### Lahendus: `computed` + `reduce` + `toFixed(2)`

```javascript
computed: {
  totals() {
    return {
      transactionCount: this.reports.reduce((s, r) => s + r.transactionCount, 0),
      //                                     ↑         ↑              ↑         ↑
      //                               jooksev summa  lisa järgmine  0 = algväärtus

      salesAmount: this.reports.reduce((s, r) => s + r.salesAmount, 0).toFixed(2),
      //                                                               ↑
      //                                        ümardab 2 kohale: 3.3000000003 → "3.30"
      feeAmount:   this.reports.reduce((s, r) => s + r.feeAmount,   0).toFixed(2),
      vatAmount:   this.reports.reduce((s, r) => s + r.vatAmount,   0).toFixed(2),
      totalFee:    this.reports.reduce((s, r) => s + r.totalFee,    0).toFixed(2),
    }
  },
},
```

### `reduce` lahti kirjutatult

```javascript
// reduce teeb sama mis see for-tsükkel:
let summa = 0
for (const r of this.reports) {
  summa = summa + r.salesAmount
}

// reduce lühiversioon:
this.reports.reduce((s, r) => s + r.salesAmount, 0)
//                   ↑  ↑                         ↑
//           jooksev  üks    algväärtus (0 = tühi)
//           summa    rida
```

### `toFixed(2)` — mida see tagastab?

```javascript
(3.3000000003).toFixed(2)   // → "3.30"  (string, mitte number!)
(100).toFixed(2)            // → "100.00"
(0).toFixed(2)              // → "0.00"
```

`toFixed()` tagastab **stringi** — see sobib kuvamiseks, aga mitte edasisteks arvutusteks.

---

## 2. `clearAllMessages` + event bubbling — kõikide teadete puhastamine

### Probleem

ReportsView-l on mitu kaarti ja igaühel oma teated (import, kustutamine, aruanded, eksport). Kasutaja võib näha korraga mitut teadet — see on segadusttekitav.

**Eesmärk:** iga klikk lehel puhastab kõik vanad teated.

### Lahendus: üks meetod + `@click` konteineril

```javascript
methods: {
  clearAllMessages() {
    this.importErrorMessage = ''     // ← import kaart
    this.importSuccessMessage = ''
    this.deleteErrorMessage = ''     // ← kustutamise kaart
    this.deleteSuccessMessage = ''
    this.reportsErrorMessage = ''    // ← aruannete tabel
    this.exportErrorMessage = ''     // ← ekspordi nupp
    this.detailErrorMessage = ''     // ← detailrida
  },
}
```

```html
<!-- @click konteineril püüab KÕIK klõpsud lehel -->
<div class="container pt-4" @click="clearAllMessages">
  ...kõik sisu...
</div>
```

### Miks see töötab — event bubbling

```
Kasutaja klikib nupul
        ↓
  nupu @click käivitub   (nt importReport())
        ↓
  sündmus "tõuseb üles" (bubbling)
        ↓
  konteineri @click käivitub   (clearAllMessages())
```

**Klikisündmused liiguvad DOM-puus ülespoole** — algusest lapselt vanemale. Konteineri `@click` saab kõik klõpsud kätte, mis toimuvad selle sees.

### Miks API vastused ei kao?

```javascript
importReport() {
  // 1. nupu click → importReport() käivitub (SÜNKROONNE)
  this.showImportSpinner = true

  ReportService.sendPostImportReport(...)
    .then(() => {
      this.importSuccessMessage = 'Aruanne edukalt imporditud!'
      // ↑ käivitub HILJEM (async) — pärast clearAllMessages()
    })

  // 2. sündmus tõuseb üles → clearAllMessages() käivitub (SÜNKROONNE)
  //    sel hetkel on teated juba tühjad (samm 1 tegi seda)
  //    .then() pole veel käivitunud — toimub alles hiljem
}
```

Järjekord: `importReport()` → `clearAllMessages()` → ... (async) → `.then()` seab uue teate

---

## 3. `activePreset` — aktiivse nupu jälgimine

### Probleem

Kasutajal on kaks kiirvalikut: "Eelmine kuu" ja "Viimased 3 kuud". Tahame näidata, milline on hetkel aktiivne — visuaalselt erinev nupp.

### Lahendus: `activePreset` andmemuutuja

```javascript
data() {
  return {
    activePreset: null,  // ← null = ükski pole aktiivne
    //                          'lastMonth' = eelmine kuu valitud
    //                          'lastThreeMonths' = 3 kuud valitud
  }
},
methods: {
  setLastMonth() {
    // ... seab fromMonth, fromYear, toMonth, toYear ...
    this.activePreset = 'lastMonth'      // ← märgib aktiivseks
  },
  setLastThreeMonths() {
    // ... seab kuupäevad ...
    this.activePreset = 'lastThreeMonths'
  },
}
```

### Template — nupu klass muutub dünaamiliselt

```html
<button
  class="btn"
  :class="activePreset === 'lastMonth' ? 'btn-secondary' : 'btn-outline-secondary'"
  @click="setLastMonth(); loadReports()"
>
  Eelmine kuu
</button>
```

```
activePreset === 'lastMonth'  →  nupp saab klassi 'btn-secondary'      (täidetud)
activePreset !== 'lastMonth'  →  nupp saab klassi 'btn-outline-secondary' (tühi)
```

### `@change` — eemaldab aktiivsuse filtri muutmisel

```html
<!-- Kui kasutaja valib käsitsi teise kuu, preset ei kehti enam -->
<select v-model="fromMonth" @change="activePreset = null">
```

| Sündmus | Miks `@change`, mitte `@click`? |
|---------|--------------------------------|
| `@click` | Käivitub iga klikiga (ka dropdown avamisel) |
| `@change` | Käivitub ainult siis, kui väärtus muutus |

---

## Kokkuvõte

| Muster | Kood | Mida lahendab |
|--------|------|---------------|
| `reduce` + `toFixed(2)` | `computed: { totals() }` | Summade arvutamine ja õige vormindamine |
| `@click` konteineril | `<div @click="clearAllMessages">` | Kõik klõpsud lehel puhastab teated |
| `activePreset` + `:class` | `activePreset === 'x' ? 'btn-secondary' : 'btn-outline-secondary'` | Aktiivse nupu visuaalne märgistamine |
| `@change` selectil | `@change="activePreset = null"` | Käsitsi muutmisel eemaldab preset |

---

## Järgmised sammud

- **Task-10** — arvete kontroll (`InvoiceControlView.vue`)
- **Task-11** — seadistused (`SettingsView.vue`)