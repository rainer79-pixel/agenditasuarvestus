# Frontend: Vue.js — Andmevoog ja mustrid

## Mis see on ja miks oluline?

Vue.js on JavaScript raamistik, millega ehitatakse kasutajaliidest. ETAS-i frontend koosneb 10 vaatest (`.vue` failid) mis suhtlevad backendiga läbi API teenuste.

**Päriselust analoogia:** Vue komponent on nagu televiisor — ekraan (template) näitab pilti, pult (methods) muudab kanalit, mälu (data) hoiab seadistused.

---

## 1. Vue komponendi struktuur

Iga `.vue` fail koosneb kahest osast:

```
┌─────────────────────────────────────┐
│  <template>                         │
│    HTML + Vue direktiivid           │
│    (see mis kasutaja näeb)          │
│  </template>                        │
│                                     │
│  <script>                           │
│    export default {                 │
│      data()      → muutujad         │
│      computed()  → arvutused        │
│      methods()   → funktsioonid     │
│      beforeMount → käivitub laadimisel │
│    }                                │
│  </script>                          │
└─────────────────────────────────────┘
```

### Andmevoog brauserist backendi ja tagasi

```
BRAUSER (kasutaja klikib)
  ↓
TEMPLATE (.vue — HTML + Vue direktiivid)
  ↓ käivitab meetodi (@click)
METHODS (Vue komponent)
  ↓ kutsub
API SERVICE (nt LoginService.js — ainult HTTP kutsed)
  ↓ axios saadab HTTP päringu
BACKEND (Spring Boot — port 8080)
  ↓ vastab JSON-iga
API SERVICE → METHODS (.then())
  ↓ salvestab
DATA (Vue komponent — reaktiivne olek)
  ↓ uuendab automaatselt
TEMPLATE (kasutaja näeb uut infot)
```

---

## 2. `data()` — Vue mälu

`data()` on nagu tahvel — kõik muutujad mida Vue jälgib ja ekraanil automaatselt uuendab.

```javascript
data() {
  return {
    loginData: {        // ← objekt (saadetakse JSON-ina backendile)
      email: '',
      password: '',
    },
    sellers: [],        // ← tühi list (täidetakse API vastusega)
    errorMessage: '',   // ← tühi = peidetud, täis = nähtav
    showSpinner: false, // ← false = ei keerle, true = keerleb
    isAdmin: AuthService.getRole() === 'A',  // ← arvutatakse kohe
  }
},
```

**Miks objekt `{email, password}` mitte kaks eraldi muutujat?**
Objekt läheb otse backendile — struktuur vastab Java DTO-le. Axios teisendab selle automaatselt JSON-iks.

---

## 3. Vue direktiivid template-s

| Direktiiv | Näide | Tähendus |
|---|---|---|
| `v-model` | `v-model="loginData.email"` | Seob inputi muutujaga — mõlemas suunas |
| `v-if` | `v-if="errorMessage"` | Kuvab elementi ainult kui tingimus on tõene |
| `v-for` | `v-for="seller in sellers"` | Kordab elementi iga listi elemendi jaoks |
| `:class` | `:class="isAdmin ? 'btn-danger' : 'btn-success'"` | Dünaamiline CSS klass |
| `@click` | `@click="login"` | Käivitab meetodi klikimisel |
| `@change` | `@change="activePreset = null"` | Käivitab kui väärtus muutub |

### `v-model` — kahepoolne seos

```javascript
// Kasutaja kirjutab inputi → loginData.email uueneb
// loginData.email muutub koodis → input uueneb
<input v-model="loginData.email" />

// Päriselust analoogia: peeglid mõlemal pool —
// üks liigub, teine liigub kaasa
```

### `v-if` ja tühi string

```javascript
''        → false  (tühi = pole midagi = peidetud)
'Viga!'   → true   (midagi on sees = nähtav)

<p v-if="errorMessage">{{ errorMessage }}</p>
// errorMessage = ''       → lõik on PEIDETUD
// errorMessage = 'Viga!' → lõik on NÄHTAV punaselt
```

---

## 4. `.then()` / `.catch()` / `.finally()` — API vastuste käsitlus

```javascript
login() {
  this.showSpinner = true         // spinner sisse

  LoginService.sendPostLogin(this.loginData)
    .then((response) => {         // ← backend vastas 200 OK
      AuthService.saveUserInfo(response.data)
      NavigationService.navigateToDashboardView()
    })
    .catch((error) => {           // ← backend vastas veaga
      this.errorMessage = error.response.data.message
    })
    .finally(() => {              // ← käivitub ALATI (nii õnnestumisel kui veal)
      this.showSpinner = false    // spinner välja
    })
}
```

**Miks `.finally()` ja mitte `.then()`-s spinner välja?**
Kui spinner oleks ainult `.then()`-s, jääks vea korral spinner igavesti keerlema.

---

## 5. AuthService — localStorage

Pärast sisselogimist salvestab backend `userId`, `firstName`, `role` jne.
Need salvestatakse **localStorage**-i — brauser hoiab neid ka pärast lehe uuendamist (F5).

```javascript
// auth/AuthService.js
saveUserInfo(data) {
  localStorage.setItem('userId', data.userId)    // → '1'
  localStorage.setItem('role',   data.role)      // → 'A'
},
getUserId() { return localStorage.getItem('userId') },  // → '1'
getRole()   { return localStorage.getItem('role') },    // → 'A'
logOut()    { localStorage.clear() },
```

```
Vue data()      → kaob kui leht uuendatakse (F5) ❌
localStorage    → püsib ka pärast F5, kuni logout ✓
```

**Päriselust analoogia:** Vue `data()` on post-it töölaual — kaob akna kinni pannes. localStorage on märkmik sahtlis — jääb alles.

---

## 6. NavigationService — lehtede vahel liikumine

```javascript
// navigation/NavigationService.js
navigateToSellerView(sellerId) {
  router.push({ name: 'sellerRoute', params: { sellerId } })
  // URL muutub: /seller/3  (ilma lehe uuesti laadimiseta)
},

navigateToDashboardView() {
  window.location.href = '/dashboard'
  // Laadib KOGU lehe uuesti (nagu F5)
  // Vajalik pärast login — et App.vue arvutaks isLoggedIn uuesti
},
```

**Miks login kasutab `window.location.href` aga teised `router.push()`?**
Navbar-i `isLoggedIn` arvutatakse ainult lehe laadimisel. `router.push()` ei laadi lehte uuesti → navbar ei uuene. `window.location.href` laadib kõik uuesti → navbar näeb uut sisselogimist.

---

## 7. `computed` — automaatsed arvutused

`computed` jälgib muutujaid automaatselt — kui muutuja muutub, arvutatakse tulemus uuesti.

```javascript
// SellersView — otsing
computed: {
  filteredSellers() {
    return this.sellers.filter((seller) =>
      seller.companyName.toLowerCase().includes(this.searchQuery.toLowerCase())
    )
  }
}
// searchQuery muutub → filteredSellers arvutatakse uuesti → tabel uueneb
```

```javascript
// SellerFormView — kaks režiimi ühes vormis
computed: {
  isAddMode()        { return this.sellerId === null },
  pageTitle()        { return this.isAddMode ? 'Lisa uus edasimüüja' : 'Muuda' },
  submitButtonLabel(){ return this.isAddMode ? 'Lisa' : 'Salvesta' },
}
```

**`computed` vs `methods`:**
```
computed → käivitub AUTOMAATSELT kui sõltuv muutuja muutub
method   → käivitub ainult kui käsitsi kutsutakse
```

**Päriselust analoogia:** `computed` on automaatne uks — liigud ligi, avaneb ise. `method` on tavaline uks — pead ise käega avama.

---

## 8. URL query params — SellerFormView

Sama vorm teeb kahte erinevat asja sõltuvalt URL-ist:

```
/seller/form              → lisa režiim  (POST — uus kirje)
/seller/form?sellerId=3   → muuda režiim (PUT  — uuenda olemasolevat)
```

```javascript
beforeMount() {
  this.sellerId = this.$route.query.sellerId ?? null
  //                    ↑              ↑           ↑
  //              Vue Router       URL-ist      kui puudub
  //              pärib URL-i      ?sellerId=3  → null
  //              query parameetrid → '3'

  if (!this.isAddMode) {
    this.getSeller()  // laadib olemasolevad andmed vormi täitmiseks
  }
},
```

---

## 9. Props ja emits — parent↔child suhtlus

Kui vaade kasutab eraldi komponenti (nt modaali), peavad nad omavahel suhtlema.

```
SellerSettingsView (VANEM)
  ↓ :seller-id="sellerId"     → props (andmed alla lapsele)
  ↑ @event-contact-saved      ← emits (signaal üles vanemale)
SellerSettingsContactModal (LAPS)
```

**Vanem avab modaali ja annab andmeid:**

```html
<SellerSettingsContactModal
  v-if="isContactModalOpen"
  :seller-id="sellerId"
  @event-modal-closed="isContactModalOpen = false"
  @event-contact-saved="handleContactSaved"
/>
```

**Laps võtab props vastu ja saadab signaale:**

```javascript
// ContactModal
props: {
  sellerId: Number,   // ← vanemalt saadud (nt 3)
},
emits: ['event-modal-closed', 'event-contact-saved'],

// X nupp → sulge modaal
@click="$emit('event-modal-closed')"

// Salvestamine õnnestus → teavita vanemat
.then(() => this.$emit('event-contact-saved'))
```

**Miks laps ei loe `sellerId` ise URL-ist?**
Modaal on eraldi komponent — ta ei tea mis lehel ta asub. Vanem teab ja annab kaasa.

---

## 10. FormData — faili saatmine backendile

Tavaline JSON päring ei suuda faili saata. Selleks on `FormData`:

```javascript
sendPostImportReport(userId, file) {
  const formData = new FormData()
  formData.append('file', file)
  //               ↑       ↑
  //          välja nimi  .xlsx fail brauserist

  return axios.post('/api/import/user/' + userId, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
    //                               ↑
    //           ütleb backendile: "see pole JSON, see on fail"
  })
},
```

```
Tavaline JSON:  axios.post('/api/login', { email: 'mari@...' })
Faili saatmine: axios.post('/api/import/...', formData, { headers: { multipart } })
```

---

## 11. Blob — faili allalaadimine backendist

Kui backend saadab faili (nt `.xlsx`), tuleb see käsitleda `blob`-ina:

```javascript
// 1. Ütle Axios-ile et vastus on fail, mitte JSON
sendGetReportExport(...) {
  return axios.get('/api/report/.../export', {
    responseType: 'blob'
  })
},

// 2. ReportsView-s — loo allalaadimislink ja "kliki" seda
.then((response) => {
  const blob = new Blob([response.data], { type: '...xlsx MIME type...' })
  const url = window.URL.createObjectURL(blob)  // ajutine link blob-ile
  const link = document.createElement('a')
  link.href = url
  link.setAttribute('download', 'aruanne.xlsx')
  link.click()                            // brauser laadib alla
  window.URL.revokeObjectURL(url)         // koristab lingi ära
})
```

**Päriselust analoogia:** Backend saadab faili baitidena → `Blob` paneb need kokku → loome ajutise lingi → "klikime" → brauser laadib alla → koristame ära.

---

## Kokkuvõte

| Mõiste | Fail/koht | Mida teeb |
|---|---|---|
| `data()` | `.vue` | Vue reaktiivne mälu — muutub → ekraan uueneb |
| `computed` | `.vue` | Automaatne arvutus — sõltub data muutujatest |
| `methods` | `.vue` | Funktsioonid — käivituvad @click jms sündmustel |
| `beforeMount` | `.vue` | Käivitub enne lehe kuvamist — laeb andmed |
| `v-model` | template | Kahepoolne seos inputi ja muutuja vahel |
| `v-if` | template | Kuvab elementi kui tingimus tõene |
| `v-for` | template | Kordab elementi listi iga elemendi jaoks |
| `AuthService` | `auth/` | Salvestab/loeb kasutajainfo localStorage-ist |
| `NavigationService` | `navigation/` | Liigub lehtede vahel |
| `XxxService` | `api-services/` | Ainult HTTP kutsed (axios) |
| `props` | komponent | Vanem annab andmeid lapsele |
| `emits` | komponent | Laps saadab signaali vanemale |
| `FormData` | `api-services/` | Faili saatmine backendile |
| `Blob` | `.vue` | Faili allalaadimine backendist |

---

## Järgmised sammud

- **task-10** — arvete kontroll (`InvoiceControlView.vue`)
- **task-11** — seadistused (`SettingsView.vue`)