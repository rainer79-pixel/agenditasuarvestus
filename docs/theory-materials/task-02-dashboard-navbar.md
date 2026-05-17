# Task-02: Dashboard ja navigatsioonibaar — algajatele

## Mis see on ja miks oluline?

Task-02 ehitab töölaua — esimese lehe mida kasutaja näeb pärast sisselogimist.
Dashboard kuvab kaks statistikakaarti: edasimüüjate arv ja viimase impordi kuupäev.
Lisaks ehitame navigatsiooniriba mis on nähtav kõigil sisselogitud lehtedel.

**Päriselust analoogia:** Dashboard on nagu auto armatuurlaud — korraga näed kõige olulisemad numbrid (kiirus, kütus). Navbar on nagu teeviidad — ütleb kuhu edasi minna.

---

## Andmevoog — suur pilt

```
Browser avab /dashboard
        ↓
    DashboardView.vue (beforeMount)
        ↓
    DashboardService.js  →  GET /api/dashboard  →  DashboardController.java
                                                            ↓
                                                    DashboardService.java
                                                      ↓             ↓
                                              SellerRepository  SalesReportRepository
                                                      ↓             ↓
                                                  Andmebaas (seller + sales_report tabelid)
                                                      ↓             ↓
                                              sellerCount    lastImportDate
                                                            ↓
                                            DashboardResponseDto ← ← ←
                                                          ↓
                        DashboardView.vue  ←  axios vastus
                              ↓
                        dashboard.sellerCount, dashboard.lastImport kuvatud ekraanil
```

---

## BACKEND

### 1. DashboardResponseDto — vastus

```java
@Data @NoArgsConstructor @AllArgsConstructor
public class DashboardResponseDto {
    private Integer sellerCount;
    private String lastImport;
}
```

Tagastatakse JSON-ina:
```json
{ "sellerCount": 2, "lastImport": "16.05.2026" }
```

**Miks `String lastImport`, mitte `LocalDate`?**
Formaadime kuupäeva backendis (`dd.MM.yyyy`) — frontend saab otse kuvada, ei pea ise teisendama.

---

### 2. SellerRepository — aktiivsete müüjate arv

```java
public interface SellerRepository extends JpaRepository<Seller, Integer> {

    @Query("select count(s) from Seller s where s.status = :status")
    int countSellersBy(@Param("status") String status);
}
```

**`count(s)`** — loendab read, mitte ei tagasta objekte.
**Tagastab `int`** — arv on alati täisarv, `long` oleks üleliigne.

Kutsumine service-st:
```java
int sellerCount = sellerRepository.countSellersBy(ACTIVE.getCode());
// ACTIVE.getCode() tagastab "A" — järjekindlus kõikjal koodis
```

**Miks `ACTIVE.getCode()` ja mitte `"A"` otse?**
Kui kunagi muutub staatuse kood, piisab muutusest ühes kohas (enum-is), mitte kogu koodis.

---

### 3. SalesReportRepository — viimase impordi kuupäev

```java
public interface SalesReportRepository extends JpaRepository<SalesReport, Integer> {

    @Query("select max(s.createdAt) from SalesReport s")
    Optional<LocalDateTime> findLastImportDate();
}
```

**`max(s.createdAt)`** — leiab kõige suurema (hiliseima) kuupäeva kõigi ridade hulgast.

**Miks `Optional<LocalDateTime>`?**
Kui `sales_report` tabel on tühi (importi pole tehtud), `max()` tagastab `null`.
`Optional` ütleb selgelt: "see väärtus võib puududa" — käsitleme seda service-s.

**Miks JPA Buddy seda ei genereerinud?**
JPA Buddy oskab genereerida `WHERE` tingimusi, aga aggregate funktsioonid (`max`, `min`, `sum`, `count`) tuleb käsitsi kirjutada.

---

### 4. DashboardService — kõik kokku

```java
@Service
@RequiredArgsConstructor
public class DashboardService {
    private final SellerRepository sellerRepository;
    private final SalesReportRepository salesReportRepository;

    public DashboardResponseDto getDashboardResponse() {
        int sellerCount = sellerRepository.countSellersBy(ACTIVE.getCode());
        Optional<LocalDateTime> lastImportDate = salesReportRepository.findLastImportDate();
        String lastImport = lastImportDate
                .map(date -> date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))
                .orElse(null);
        return new DashboardResponseDto(sellerCount, lastImport);
    }
}
```

**Samm-sammult:**
1. `countSellersBy("A")` → küsib DB-st aktiivsete müüjate arvu
2. `findLastImportDate()` → küsib kõige hilisema `created_at` kuupäeva
3. `.map(date -> date.format(...))` → kui kuupäev on olemas, formaadib stringiks `"dd.MM.yyyy"`
4. `.orElse(null)` → kui importi pole tehtud, tagastab `null`
5. `new DashboardResponseDto(sellerCount, lastImport)` → pakib vastuse DTO-sse (inline)

**Optional ahel selgitatud:**
```
Optional<LocalDateTime>
    .map(date -> formaadi string)   ← käivitub ainult kui väärtus olemas
    .orElse(null)                   ← käivitub kui väärtus puudub
```

---

### 5. DashboardController — HTTP endpoint

```java
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    @Operation(summary = "Dashboard statistika")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK")})
    public DashboardResponseDto getDashboard() {
        return dashboardService.getDashboardResponse();
    }
}
```

**Miks `GET`, mitte `POST`?**
Dashboard ainult loeb andmeid, ei muuda midagi. `GET` on lugemiseks, `POST` on loomiseks.
Reegel: kui saadad andmeid serverisse → `POST`. Kui küsid andmeid → `GET`.

---

## FRONTEND

### 6. DashboardService.js — API päring

```javascript
import axios from 'axios'

export default {
  sendGetDashboard() {
    return axios.get('/api/dashboard')
  },
}
```

Lihtne — üks meetod, `GET` päring. `return` tagastab Promise mida `DashboardView` kasutab `.then()` ja `.catch()`-ga.

---

### 7. DashboardView.vue — template

```html
<template>
  <div class="container pt-5 mt-5">
    <h1 class="mb-5">
      Tere tulemast, {{ firstName }}
      <span v-if="middleName">{{ middleName }} </span>
      {{ lastName }}!
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
  </div>
</template>
```

**Bootstrap layout selgitatud:**
```
container          ← keskel, fikseeritud laius
  row              ← horisontaalne rida
  align-items-stretch ← kõik kaardid sama kõrgusega
    col-6          ← pool laiusest (6 + 6 = 12 = täislaius)
      card h-100   ← kaart, täidab kogu veeru kõrguse
        card-body text-center ← sisu, kõik keskel
```

**`v-if="middleName"`** — `<span>` kuvatakse ainult kui `middleName` pole tühi string.
Kui `middleName` on `''` (tühi string), on see falsy → element ei ilmu.

---

### 8. DashboardView.vue — script

```javascript
import AuthService from '@/auth/AuthService.js'
import DashboardService from '@/api-services/DashboardService.js'

export default {
  name: 'DashboardView',
  data() {
    return {
      firstName: AuthService.getFirstName(),   // localStorage-ist kohe
      middleName: AuthService.getMiddleName(), // localStorage-ist kohe
      lastName: AuthService.getLastName(),     // localStorage-ist kohe
      dashboard: {
        sellerCount: 0,      // algväärtus enne API vastust
        lastImport: null,    // algväärtus enne API vastust
      },
    }
  },
  methods: {
    getDashboard() {
      DashboardService.sendGetDashboard()
        .then((response) => {
          this.dashboard = response.data  // Vue uuendab ekraani automaatselt
        })
        .catch((error) => {
          console.log(error)
        })
    },
  },
  beforeMount() {
    this.getDashboard()  // kutsutakse enne lehe kuvamist
  },
}
```

**Miks `dashboard` on objekt algväärtustega?**
Vue peab teadma milliseid väljasid jälgida. Kui kirjutaksid `dashboard: {}`, ei pruugi Vue `sellerCount` ja `lastImport` muutusi märgata.

**`beforeMount()` vs `mounted()`:**
- `beforeMount` — enne HTML renderdamist → kasutaja ei näe tühja kaarti
- `mounted` — pärast renderdamist → kasutaja näeks hetkeks tühja kaarti

---

### 9. App.vue — navbar

```html
<nav v-if="showNavbar" class="navbar navbar-dark bg-dark px-3 d-flex justify-content-between">
  <div class="d-flex gap-4">
    <router-link class="nav-link text-white" :to="{ name: 'dashboardRoute' }">Töölaud</router-link>
    <router-link class="nav-link text-white" :to="{ name: 'sellersRoute' }">Edasimüüjad</router-link>
    <router-link class="nav-link text-white" v-if="isAdmin" :to="{ name: 'settingsRoute' }">Seaded</router-link>
  </div>
  <div class="d-flex align-items-center gap-3">
    <span class="text-white">{{ firstName }} {{ lastName }}</span>
    <button class="btn btn-danger" @click="logOut">Logi välja</button>
  </div>
</nav>
<RouterView />
```

**`<router-link>` vs `<a>`:**

| | `<a @click="navigate">` | `<router-link :to="...">` |
|---|---|---|
| Navigatsioon | Käsitsi meetod | Vue Router automaatselt |
| Aktiivne klass | Käsitsi | `router-link-exact-active` automaatselt |
| Kood | Rohkem | Vähem |

**`router-link-exact-active`** — Vue Router lisab selle klassi automaatselt aktiivsele lingile.
CSS-is:
```css
.router-link-exact-active {
  border-bottom: 2px solid white;
}
```

**`v-if="isAdmin"`** — `Seaded` link kuvatakse ainult kui `role === 'A'`.

---

### 10. App.vue — computed ja logOut

```javascript
data() {
  return {
    isLoggedIn: AuthService.isLoggedIn(),  // loetakse lehe laadimisel
    isAdmin: AuthService.getRole() === 'A',
    firstName: AuthService.getFirstName(),
    lastName: AuthService.getLastName(),
  }
},
computed: {
  showNavbar() {
    return (
      this.isLoggedIn &&
      this.$route.name !== 'loginRoute' &&
      this.$route.name !== 'homeRoute'
    )
  },
},
methods: {
  logOut() {
    AuthService.logOut()       // tühjendab localStorage
    this.isLoggedIn = false    // navbar kaob kohe
    NavigationService.navigateToHomeView()
  },
}
```

**`computed` vs `methods`:**

| | `computed` | `methods` |
|---|---|---|
| Millal käivitub | Automaatselt kui sõltuvused muutuvad | Ainult kui kutsud välja |
| Kasutuskoht | Arvutatud väärtused (`showNavbar`) | Tegevused (`logOut`, `getDashboard`) |

**`showNavbar`** sõltub kahest asjast:
1. `this.isLoggedIn` — kas kasutaja on sisse loginud?
2. `this.$route.name` — millisel lehel oleme?

Kui kumbki muutub, Vue arvutab `showNavbar` automaatselt uuesti.

**Miks `this.isLoggedIn = false` logOut-is?**
`localStorage` ei ole Vue-s reaktiivne — Vue ei märka kui localStorage muutub.
Seepärast uuendame `isLoggedIn` käsitsi → `showNavbar` arvutatakse uuesti → navbar kaob.

---

### 11. NavigationService.js — oluline erand

```javascript
navigateToDashboardView() {
  window.location.href = '/dashboard'  // ← täisleht laaditakse uuesti!
},
// kõik teised:
navigateToSellersView() {
  router.push({ name: 'sellersRoute' })  // ← ainult vaade vahetub
},
```

**Miks `window.location.href` pärast sisselogimist?**

`App.vue` loeb `isLoggedIn` üks kord lehe laadimisel. Sisselogimisel `App.vue` ei laadita uuesti — `isLoggedIn` jääb `false`-ks ja navbar ei ilmu.

`window.location.href` lammutab lehe täielikult ja ehitab uuesti — siis loetakse `isLoggedIn` uuesti localStorage-ist → `true` → navbar ilmub.

```
router.push()         → ainult RouterView komponent vahetub, App.vue jääb
window.location.href  → kogu brauser laadib uuesti, App.vue loetakse nullist
```

---

### 12. AuthService.js — middleName parandus

```javascript
saveUserInfo(data) {
  localStorage.setItem('middleName', data.middleName || '')
  //                                               ↑
  //         kui middleName on null → salvesta '' (tühi string)
  //         muidu localStorage salvestaks stringi "null"
  //         ja v-if="middleName" ei töötaks (string "null" on truthy!)
}
```

**Probleem:** `localStorage.setItem('key', null)` salvestab sõna-sõnalt `"null"` (string), mitte päris `null`.

**Lahendus:** `|| ''` tagab et `null` → tühi string `''` → `v-if="middleName"` töötab õigesti.

---

## Kokkuvõte — võtmemõisted

| Mõiste | Selgitus |
|--------|---------|
| `@GetMapping` | HTTP GET endpoint — andmete lugemiseks |
| `count()` | JPQL aggregate — loendab read |
| `max()` | JPQL aggregate — leiab suurima väärtuse |
| `Optional` | Turvaline viis null-i käsitlemiseks — `.map()` ja `.orElse()` |
| `computed` | Vue automaatselt uuenev väärtus — sõltub reaktiivsetest andmetest |
| `router-link` | Vue Router-i navigatsiooni komponent — aktiivne klass automaatselt |
| `router-link-exact-active` | CSS klass mille Vue Router lisab aktiivsele lingile |
| `window.location.href` | Brauser laadib lehe täielikult uuesti |
| `router.push()` | Vue Router navigeerib — ainult vaade vahetub, leht ei laadita uuesti |
| `beforeMount()` | Vue lifecycle hook — käivitub enne HTML renderdamist |
| `align-items-stretch` | Bootstrap — kõik veerus olevad elemendid sama kõrgusega |
| `h-100` | Bootstrap — element täidab kogu vanemelemendi kõrguse |
| `v-if` | Vue direktiiv — kuvab elemendi ainult kui tingimus on tõene |

---

## Järgmised sammud

- **Task-03:** SellersView — edasimüüjate nimekiri otsimise ja filtreerimisega
- **Route guard:** kaitsa vaateid — `isLoggedIn()` kontroll enne iga lehe avamist
- **Error handling:** `errorMessage` kuvamine dashboard-il serveri vea korral
