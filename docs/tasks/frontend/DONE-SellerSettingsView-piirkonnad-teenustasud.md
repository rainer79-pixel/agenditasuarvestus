# SellerSettingsView — piirkonnad + teenustasud

**Vaade:** `SellerSettingsView.vue`
**Tüüp:** Frontend
**Staatus:** To Do

## Kontekst

See task lisab `SellerSettingsView`-sse **piirkondade** ja **teenustasude** sektsioonid.
Töö käib paralleelselt tiimikaaslase taskiga (`SellerSettingsView-raamistik-kontaktid`) — siin loodavad failid on peaaegu kõik eraldi uued failid, seega merge konflikt on minimaalne.
Ainult `SellerSettingsView.vue` lõpus on vaja sektsioonid sisse lisada (tiimikaaslasega kokku leppida).

## Mocki vaade

![SellerSettingsView mock](../../balsamiq/views/SellerSettingsView.vue.png)

---

## Kaasatud failid

| Fail | Tegevus |
|------|---------|
| `frontend/src/components/modals/SellerSettingsRegionModal.vue` | LOO — piirkonna lisamise modal |
| `frontend/src/components/modals/SellerSettingsProductModal.vue` | LOO — teenustasu lisamise modal |
| `frontend/src/api-services/SellerService.js` | UUENDA — lisa piirkondade + teenustasude meetodid |
| `frontend/src/api-services/RegionService.js` | LOO — GET /api/region (piirkondade dropdown jaoks) |
| `frontend/src/api-services/ProductTypeService.js` | LOO — GET /api/product-type (tootegruppide dropdown jaoks) |
| `frontend/src/views/SellerSettingsView.vue` | UUENDA — lisa piirkondade ja teenustasude sektsioonid (tiimikaaslasega kooskõlastatult) |

---

## API endpointid — piirkonnad

| Meetod | URL | Kirjeldus |
|--------|-----|-----------|
| GET | `/api/region` | Kõik piirkonnad (dropdown jaoks modalis) |
| GET | `/api/seller/{sellerId}/regions` | Edasimüüja piirkonnad |
| POST | `/api/seller/{sellerId}/regions?userId=` | Lisa piirkond |
| PUT | `/api/seller/{sellerId}/regions/{regionId}?userId=` | Uuenda müügipunktide arvu |
| DELETE | `/api/seller/{sellerId}/regions/{regionId}?userId=` | Kustuta piirkond |

### SellerRegionResponseDto väljaed (GET vastus)
```json
{
  "regionId": 0,
  "regionName": "string",
  "salesPointCount": 0
}
```

### SellerRegionDto väljaed (POST ja PUT body)
```json
{
  "salesPointCount": 0
}
```
> PUT puhul saadetakse ainult `salesPointCount` — piirkond ise ei muutu.
> POST puhul on vajalik lisaks `regionId` — valitakse dropdownist.

---

## API endpointid — teenustasud

| Meetod | URL | Kirjeldus |
|--------|-----|-----------|
| GET | `/api/product-type` | Kõik tootegrupid (dropdown jaoks modalis) |
| GET | `/api/seller/{sellerId}/commission-rates` | Edasimüüja teenustasud |
| POST | `/api/seller/{sellerId}/commission-rates?userId=` | Lisa teenustasu |
| PUT | `/api/seller/{sellerId}/commission-rates/{commissionRateId}?userId=` | Muuda teenustasu |
| DELETE | `/api/seller/{sellerId}/commission-rates/{commissionRateId}?userId=` | Kustuta teenustasu |

### CommissionRateResponseDto väljaed (GET vastus)
```json
{
  "commissionRateId": 0,
  "productTypeName": "string",
  "feePerTransaction": 0.0,
  "feePercent": 0.0,
  "includesVat": false,
  "validFrom": "pp.kk.aaaa",
  "validTo": "pp.kk.aaaa"
}
```

### CommissionRateDto väljaed (POST ja PUT body)
```json
{
  "productTypeId": 0,
  "feePerTransaction": 0.0,
  "feePercent": 0.0,
  "includesVat": false,
  "validFrom": "pp.kk.aaaa",
  "validTo": "pp.kk.aaaa"
}
```

---

## Funktsionaalsus

### Piirkonnad sektsiooni

- Kuva piirkondade tabel: **Piirkond** (regionName), **Müügipunktid** (salesPointCount inline muutmisväli), **Muuda** nupp (pliiats), **Kustuta** nupp
- "Lisa piirkond" roheline nupp — avab `SellerSettingsRegionModal`
- **Inline muutmine:** pliiats-nupp lülitab rea muutmisrežiimi — `salesPointCount` muutub `<input type="number">`-ks, pliiats asendub salvesta (✓) ja tühista (✗) nuppudega
- Salvesta — saadab PUT, värskendab nimekirja
- Kustuta nupp — saadab DELETE, värskendab nimekirja

### SellerSettingsRegionModal

- Väljad: **Piirkond** (dropdown — laetakse GET /api/region), **Müügipunktide arv** (number input, min 0)
- Salvestamine: POST → sulge modal → laadi piirkondade nimekiri uuesti
- Veakäsitlus: kuva `errorMessage` modali sees
- Sündmus: `event-modal-closed`, `event-region-saved`

### Teenustasud sektsiooni

- Kuva teenustasude tabel: **Tasu koguselt** (feePerTransaction), **Tasu %** (feePercent), **KM sees** (includesVat — kuva "Jah"/"Ei"), **Kehtib alates** (validFrom), **Kehtib kuni** (validTo — kui null → "Lõputu"), **Muuda** nupp, **Kustuta** nupp
- "Lisa teenustasu" roheline nupp — avab `SellerSettingsProductModal`
- **Inline muutmine:** sama muster nagu piirkondadel — pliiats avab muutmisrežiimi, kõik väljad muutuvad inputtideks
- Kustuta nupp: kui teenustasu on kasutuses (409 vastus) → kuva veateade "Teenustasu on kasutuses ja seda ei saa kustutada"

### SellerSettingsProductModal

- Väljad: **Tootegrupp** (dropdown — laetakse GET /api/product-type), **Tasu koguselt** (number), **Tasu %** (number), **KM sees** (checkbox), **Kehtib alates** (kuupäev pp.kk.aaaa), **Kehtib kuni** (kuupäev, valikuline)
- Salvestamine: POST → sulge modal → laadi teenustasude nimekiri uuesti
- Sündmus: `event-modal-closed`, `event-commission-rate-saved`

---

## SellerService.js — lisa meetodid

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

## RegionService.js — uus fail

```javascript
import axios from 'axios'

export default {
  sendGetRegions() {
    return axios.get('/api/region')
  },
}
```

## ProductTypeService.js — uus fail

```javascript
import axios from 'axios'

export default {
  sendGetProductTypes() {
    return axios.get('/api/product-type')
  },
}
```

---

## Vastuvõtu kriteeriumid

- [ ] Piirkondade nimekiri kuvab regionName ja salesPointCount
- [ ] Inline muutmine: pliiats → input → salvesta/tühista nupud töötavad
- [ ] PUT õnnestub — nimekiri uueneb
- [ ] "Lisa piirkond" modal avab koos piirkondade dropdowniga
- [ ] POST piirkond õnnestub — modal sulgub, nimekiri uueneb
- [ ] DELETE piirkond eemaldab rea
- [ ] Teenustasude nimekiri kuvab kõik väljad (KM sees "Jah"/"Ei", lõputu kehtivus)
- [ ] Inline muutmine töötab teenustasudel
- [ ] "Lisa teenustasu" modal avab koos tootegruppide dropdowniga
- [ ] POST teenustasu õnnestub — modal sulgub, nimekiri uueneb
- [ ] DELETE blokeerimise korral (409) kuvatakse veateade eesti keeles
- [ ] Sektsioonid on lisatud `SellerSettingsView.vue`-sse (kooskõlastatult tiimikaaslasega)

---

## Märkus paralleelse töö kohta

See task loob peaaegu ainult **uusi faile** — `SellerSettingsRegionModal.vue`, `SellerSettingsProductModal.vue`, `RegionService.js`, `ProductTypeService.js`.
`SellerService.js` ja `SellerSettingsView.vue` muutmiseks lepi tiimikaaslasega kokku kellal mida lisab — parim lähenemine on lisada oma meetodid `SellerService.js` faili lõppu (erinev piirkond, vähem konflikte).
