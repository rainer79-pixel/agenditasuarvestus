# SellerSettingsView — piirkonnad ja teenustasud (frontend)

## Mis see on ja miks oluline?

See task lisab `SellerSettingsView`-sse piirkondade ja teenustasude halduse — kaks tabelit kus saab andmeid lisada, muuta ja kustutada. Lisaks parandatakse navigatsioon (`SellersView`, `SellerView`) ja lisatakse staatuse toggle koos muuda-nupuga.

**Päriselust analoogia:** Kujuta ette tabelarvutust kus saad ridu lisada, muuta ja kustutada — ilma lehte vahetamata. Täpselt seda inline muutmine teeb. Lisamiseks ilmub eraldi aken (modal) — nagu dialoogiaken desktoprakenduses.

---

## Mis on selles taskis uut võrreldes eelmise frontendi taskiga?

| Uus asi | Kus näed | Mida tähendab |
|---------|---------|---------------|
| **Modaalne komponent propide ja emitidega** | `SellerSettingsRegionModal.vue`, `SellerSettingsProductModal.vue` | Lapskomponent saab andmed `props` kaudu, saadab sündmused `emits`-iga välja — vanem-laps suhtlus |
| **Inline muutmine** | Piirkondade ja teenustasude tabelid | Ühe rea read/edit lülitus `v-if` abil — sama rea kohale kuvatakse kas tekst või sisendväli |
| **`editingId` muster** | `editingRegionId`, `editingCommissionRateId` | Null = ei muudeta midagi; väärtus = see rida on muutmisrežiimis |
| **productTypeId otsing nimest** | `startEditCommissionRate()` | GET vastus tagastab nime, PUT vajab ID-d — otsime ID üles `productTypes` listist nime järgi |
| **409 Conflict veakäsitlus** | `deleteCommissionRate()` | Kustutamine blokeeritakse kui teenustasu on kasutuses — käsitleme HTTP 409 spetsiaalselt |
| **`validTo \|\| null`** | `SellerSettingsProductModal` | Tühi string → `null` — backendi `@NotNull` puudub, aga tühi string on kehtetu kuupäev |
| **`visibility: hidden`** | Kontaktide tabeli ✏ nupp | Nähtamatu kohahoidja nuppude joondamiseks — element võtab ruumi aga pole nähtav |

---

## Andmevoog — suur pilt

Iga nupp käivitab ühe konkreetse API päringu:

```
Kasutaja klikib "+ Lisa piirkond"
        ↓
isRegionModalOpen = true  →  SellerSettingsRegionModal ilmub
        ↓
Kasutaja valib piirkonna + sisestab müügipunktide arvu + vajutab "Lisa"
        ↓
SellerService.sendPostSellerRegion(sellerId, userId, { regionId, salesPointCount })
        ↓
POST /api/seller/1/regions?userId=1  →  SellerController  →  SellerService  →  DB
        ↓
Õnnestus: $emit('event-region-saved')
        ↓
SellerSettingsView: isRegionModalOpen = false + loadRegions()
        ↓
GET /api/seller/1/regions  →  uus nimekiri  →  tabel uueneb ekraanil
```

---

## 1. Uued API teenused

### RegionService.js ja ProductTypeService.js

Mõlemad on lihtsad ühe meetodiga failid — laaditakse dropdownide jaoks modaalides.

```javascript
// RegionService.js
import axios from 'axios'
export default {
  sendGetRegions() {
    return axios.get('/api/region')
  },
}

// ProductTypeService.js
import axios from 'axios'
export default {
  sendGetProductTypes() {
    return axios.get('/api/product-type')
  },
}
```

**Miks eraldi failid, mitte SellerService-sse?**
`/api/region` ja `/api/product-type` on üldised register-loetelud — neid võib tulevikus kasutada ka teistes vaadetes. `SellerService` on spetsiifiliselt edasimüüjaga seotud.

---

### SellerService.js — 6 uut meetodit

```javascript
// Piirkonnad
sendPostSellerRegion(sellerId, userId, regionData) {
  return axios.post('/api/seller/' + sellerId + '/regions?userId=' + userId, regionData)
},
sendPutSellerRegion(sellerId, regionId, userId, regionData) {
  return axios.put('/api/seller/' + sellerId + '/regions/' + regionId + '?userId=' + userId, regionData)
},
sendDeleteSellerRegion(sellerId, regionId, userId) {
  return axios.delete('/api/seller/' + sellerId + '/regions/' + regionId + '?userId=' + userId)
},

// Teenustasud
sendPostSellerCommissionRate(sellerId, userId, commissionData) {
  return axios.post('/api/seller/' + sellerId + '/commission-rates?userId=' + userId, commissionData)
},
sendPutSellerCommissionRate(sellerId, commissionRateId, userId, commissionData) {
  return axios.put('/api/seller/' + sellerId + '/commission-rates/' + commissionRateId + '?userId=' + userId, commissionData)
},
sendDeleteSellerCommissionRate(sellerId, commissionRateId, userId) {
  return axios.delete('/api/seller/' + sellerId + '/commission-rates/' + commissionRateId + '?userId=' + userId)
},
```

**Miks `userId` URL-is query parameetrina (`?userId=`)?**
Backend logib iga muudatuse — kes tegi, millal. `userId` on audit-logi jaoks, mitte ressursi identifikaator. REST konventsioon: ressursid on teel (`/seller/1/regions/3`), metaandmed on parameetrites (`?userId=1`).

---

## 2. Modaalne komponent — vanem-laps suhtlus

### Mida modal teeb?

Modal on eraldi komponent mis:
1. Saab vanematelt andmed `props` kaudu (nt `sellerId`)
2. Kasutajal on vorm — valib, sisestab
3. Saadab sündmuse välja `emits`-iga (õnnestus / sulge)
4. Vanem kuulab sündmust `@event-...` kaudu

```
SellerSettingsView (vanem)
    │
    │  :seller-id="sellerId"     ← prop alla
    ↓
SellerSettingsRegionModal (laps)
    │
    │  $emit('event-region-saved')  ← sündmus üles
    ↓
SellerSettingsView
    └─ handleRegionSaved()  →  sulge modal, laadi nimekiri uuesti
```

### Bootstrap modali struktuur

Bootstrap modal nõuab kindlat pesastust:

```html
<div class="modal d-block">          ← nähtav (d-block asendab display:none)
  <div class="modal-dialog">         ← positsioneerib keskel
    <div class="modal-content">      ← valge kast
      <div class="modal-header">...</div>
      <div class="modal-body">...</div>
      <div class="modal-footer">...</div>
    </div>
  </div>
</div>
```

**Levinud viga:** `modal-body` ja `modal-footer` paigutamine `modal-content` VÄLJAPOOLE.
Tulemus: Vue multiple-root error + katkine visuaal.

### Props ja emits

```javascript
export default {
  name: 'SellerSettingsRegionModal',
  props: {
    sellerId: Number,   // lihtsale propile piisab tüübist
  },
  emits: ['event-modal-closed', 'event-region-saved'],
  // ...
}
```

**Miks `emits` deklareerida?**
Vue 3 nõuab emitide deklareerimist — see dokumenteerib komponendi "lepingu" (mida ta saadab välja) ja Vue hoiatab kui emitatakse deklareerimata sündmust.

### validTo — tühi string vs null

```javascript
commissionData = {
  validTo: this.validTo || null,  // '' || null → null
}
```

**Miks see oluline?**
HTML `<input>` ei saa tagastada `null` — kui kasutaja ei sisesta midagi, on väärtus `''` (tühi string).
Backendi kuupäevaparser ei oska tühja stringi tõlkida. `|| null` muudab tühja stringi `null`-iks → backend ignoreerib välja (tähtajatu kehtivus).

---

## 3. Inline muutmine — editingId muster

Inline muutmine tähendab: sama rea kohale kuvatakse kas **vaatamisrežiim** või **muutmisrežiim** — ilma eraldi modali avamata.

### Kuidas see töötab?

```javascript
data() {
  return {
    editingRegionId: null,      // null = ükski rida pole muutmisrežiimis
    editingSalesPointCount: null,
  }
},
methods: {
  startEditRegion(region) {
    this.editingRegionId = region.regionId          // märgi see rida aktiivseks
    this.editingSalesPointCount = region.salesPointCount  // kopeeri praegune väärtus
  },
  cancelEditRegion() {
    this.editingRegionId = null  // tagasi vaatamisrežiimi
    this.editingSalesPointCount = null
  },
}
```

### Template — v-if lülitab

```html
<tr v-for="region in regions" :key="region.regionId">
  <td>{{ region.regionName }}</td>
  <td>
    <!-- VAATAMINE: kuva tekst kui see rida EI OLE muutmisrežiimis -->
    <span v-if="editingRegionId !== region.regionId">{{ region.salesPointCount }}</span>
    <!-- MUUTMINE: kuva input kui see rida ON muutmisrežiimis -->
    <input v-else type="number" v-model="editingSalesPointCount" />
  </td>
  <td>
    <!-- VAATAMINE: pliiats + kustuta -->
    <template v-if="editingRegionId !== region.regionId">
      <button @click="startEditRegion(region)">✏</button>
      <button @click="deleteRegion(region.regionId)">Kustuta</button>
    </template>
    <!-- MUUTMINE: linnuke + X -->
    <template v-else>
      <button @click="saveRegionInline(region.regionId)">✓</button>
      <button @click="cancelEditRegion()">✗</button>
    </template>
  </td>
</tr>
```

**Miks `<template>` ja mitte `<div>`?**
`<template>` on nähtamatu konteiner — ei tekita lisaelemente DOM-i. `<div>` tekitaks lisarekki tabelis.

**Lülitusloogika:**
```
editingRegionId = null       → KÕIK read näitavad vaatamisrežiimi
editingRegionId = 3          → rida id=3 näitab muutmisrežiimi, ülejäänud vaatamisrežiimi
```

Korraga saab muutmisrežiimis olla ainult ÜKS rida — uue rea avamine seab `editingRegionId` uuele väärtusele, eelmine sulgub automaatselt.

---

## 4. productTypeId probleem — GET tagastab nime, PUT vajab ID-d

See on üks keerulisemaid kohti taskis.

**Probleem:**
```
GET /api/seller/1/commission-rates tagastab:
  { "productTypeName": "pilet", "feePerTransaction": 0.5, ... }
  ← ID-d EI OLE vastuses!

PUT /api/seller/1/commission-rates/7 vajab:
  { "productTypeId": 3, "feePerTransaction": 0.5, ... }
  ← ID-d VAJAME!
```

**Lahendus:** Laadime kõik tootegrupid eraldi (`GET /api/product-type`) ja otsime ID nime järgi:

```javascript
data() {
  return {
    productTypes: [],  // [{ productTypeId: 3, productTypeName: "pilet" }, ...]
  }
},
methods: {
  loadProductTypes() {
    ProductTypeService.sendGetProductTypes()
      .then((response) => { this.productTypes = response.data })
  },
  startEditCommissionRate(cr) {
    // Otsi productTypeId nime järgi productTypes listist
    const productType = this.productTypes.find(
      (pt) => pt.productTypeName === cr.productTypeName
    )
    this.editingCommissionRate = {
      productTypeId: productType ? productType.productTypeId : null,
      feePerTransaction: cr.feePerTransaction,
      // ...
    }
  },
}
```

**`Array.find()`** tagastab esimese elemendi mis vastab tingimusele — või `undefined` kui ei leita.
`productType ? productType.productTypeId : null` — kui ei leitud, kasuta `null` (ohutu fallback).

**Miks backend ei tagasta ID-d GET vastuses?**
`CommissionRateResponseDto` sisaldab `productTypeName` kuvamiseks — ID-d tavakasutajale ei näidata. PUT jaoks on vaja ID-d, aga DTO disain lähtub GET kasutusjuhtumist. Kompromiss: laadi tootegrupid eraldi.

---

## 5. 409 Conflict — kustutamise blokeerimine

Teenustasu kustutamine võib ebaõnnestuda kui see on juba arvutustes kasutuses:

```javascript
deleteCommissionRate(commissionRateId) {
  const userId = AuthService.getUserId()
  SellerService.sendDeleteSellerCommissionRate(this.sellerId, commissionRateId, userId)
    .then(() => this.loadCommissionRates())
    .catch((error) => {
      if (error.response.status === 409) {
        // Spetsiifiline veateade 409 korral
        this.commissionRateError = 'Teenustasu on kasutuses ja seda ei saa kustutada'
      } else {
        // Kõik muud vead — kuva serveri sõnum
        this.commissionRateError = error.response.data.message
      }
    })
},
```

**Miks 409 eraldi käsitleda?**
Backend tagastab 409 koos tehnilise veasõnumiga mis pole kasutajasõbralik. Frontend teab konteksti — "teenustasu on kasutuses" — ja kuvab parema sõnumi.

**HTTP 409 Conflict** — standardkood "loogilise konflikti" jaoks. Ei tähenda serveriviga (500) ega kasutaja viga sisendis (400) — tähendab et toiming pole lubatud praeguses olekus.

---

## 6. Nuppude joondamine — visuaalsed nõkse

### `flex-grow-1` — nupp täidab ülejäänud ruumi

```html
<div class="d-flex">
  <button class="btn btn-warning btn-sm me-1">✏</button>
  <button class="btn btn-danger btn-sm flex-grow-1">Kustuta</button>
</div>
```

`flex-grow-1` ütleb: "kasuta kõik ülejäänud ruum". Tulemus: Kustuta nupp venib, ✏ jääb fikseeritud laiusega — koos täidavad nad kogu lahtri laiuse.

### `visibility: hidden` — nähtamatu kohahoidja

Kontaktide tabelis pole muutmise funktsionaalsust veel — aga joondamiseks peab ✏ koht olema reserveeritud:

```html
<button class="btn btn-warning btn-sm me-1" style="visibility: hidden">✏</button>
<button class="btn btn-danger btn-sm flex-grow-1" @click="deleteContact(...)">Kustuta</button>
```

**`visibility: hidden` vs `display: none`:**
- `display: none` — element kaob, ülejäänud elemendid nihkuvad
- `visibility: hidden` — element on nähtamatu AGA võtab endiselt ruumi — joondus säilib

### Ühepikkused nupud veerus

Kõigil kolmel sektsiooni "Lisa" nupul ja tegevuste veeru `<th>`-l sama laius:

```html
<th style="width: 140px" class="text-end"></th>

<button style="width: 140px" @click="isRegionModalOpen = true">+ Lisa piirkond</button>
```

`width: 140px` kõikjal → visuaalne joondus kõigi sektsioonide vahel.

---

## 7. SellerView — ainult lugemiseks

`SellerView` on disainitud ainult andmete vaatamiseks — muutmised toimuvad `SellerSettingsView`-s:

```javascript
// SellerView.vue — EI OLE state toggle, EI OLE muuda nupp
// Ainult: Tagasi nimekirja + Seaded nupud all
methods: {
  goToSellerSettingsView() {
    NavigationService.navigateToSellerSettingsView(this.sellerId)
  },
  goToSellersView() {
    NavigationService.navigateToSellersView()
  },
}
```

**Miks selline lahusus?**
- `SellerView` on lugemiseks — selge, lihtne, kasutajad ei muuda kogemata
- `SellerSettingsView` on haldamiseks — kõik muudatused ühes kohas
- Admin saab otse "Seaded" nupust ligi ilma "Vaata" → "Muuda" → tagasi tsüklita

---

## 8. Staatuse toggle SellerSettingsView-s

Staatuse muutmine on ainult `SellerSettingsView`-s:

```javascript
toggleStatus() {
  const newStatus = this.seller.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
  SellerService.sendPutSellerStatus(this.sellerId, { status: newStatus })
    .then(() => this.loadSeller())
    .catch((error) => { this.errorMessage = error.response.data.message })
},
```

Ternary operaator (`? :`) valib uue staatuse:
- kui praegu `'ACTIVE'` → saada `'INACTIVE'`
- kui praegu midagi muud (st `'INACTIVE'`) → saada `'ACTIVE'`

Nupu värv muutub vastavalt:
```html
:class="seller.status === 'ACTIVE' ? 'btn-danger' : 'btn-success'"
```
Aktiivset saab ainult "punase nupuga" peatada — intuiitne.

---

## Kokkuvõte — võtmemõisted

| Mõiste | Selgitus |
|--------|---------|
| `props` | Andmed vanemkomponendist lapsele — ühepoolne allavoolu |
| `emits` | Sündmused lapsest vanemale — ühepoolne ülesvoolu |
| `event-` prefiks | Kõik projekti custom sündmused algavad sellega — nimekonventsioon |
| `editingId` muster | `null` = vaatamisrežiim; ID väärtus = see rida on muutmisrežiimis |
| `v-if` / `v-else` | Template lülitab vaatamis- ja muutmisrežiimi vahel |
| `<template>` | Nähtamatu Vue konteiner — ei tekita DOM elementi |
| `Array.find()` | Leiab massiivst esimese elemendi mis vastab tingimusele |
| `|| null` | Tühi string muudetakse `null`-iks — backendi jaoks |
| `visibility: hidden` | Element on nähtamatu aga võtab ruumi — joondus säilib |
| `flex-grow-1` | Flex element venib ja täidab kogu ülejäänud ruumi |
| HTTP 409 Conflict | "Loogiline konflikt" — toiming pole praeguses olekus lubatud |
| Bootstrap modal struktuur | `modal > modal-dialog > modal-content > header/body/footer` |

---

## Järgmised sammud

- **Task-07:** Excel import — `POST /api/import`, `ReportsView.vue` faili üleslaadimine
- **Task-08:** Teenustasu arvutus + aruanded — `commission_calculation` loogika, `ReportsView.vue` tabel
- **409 testimine:** Teenustasu kustutamise blokeerimine on kirjutatud — saab testida kui task-07 ja task-08 on valmis ja andmebaasis on arvutusi