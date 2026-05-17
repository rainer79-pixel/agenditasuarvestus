# Task-03: Edasimüüjad

## Mis selles taskis teeme?

Ehitame edasimüüjate halduse tuuma — kolm vaadet ja täielik backend. Kasutaja näeb edasimüüjate nimekirja, saab otsida, vaadata detaile ja (Admin rolliga) lisada ning muuta edasimüüjaid. Edasimüüjaid ei kustutata — ainult deaktiveeritakse.

See on task-03 keerukuse tõttu (3/5) mahukam kui eelmised. Backend sisaldab staatuse teisendust (DB `"A"`/`"D"` → API `"ACTIVE"`/`"INACTIVE"` → UI `"Aktiivne"`/`"Peatatud"`), rollipõhist ligipääsukontrolli ja unikaalsuse validatsiooni. Frontend sisaldab kolm eraldi vaadet millest ühes (SellerFormView) töötab nii lisamis- kui muutmisrežiim sama URL-iga.

**Backend:** `GET /api/seller/user/{userId}`, `GET /api/seller/{sellerId}`, `GET /api/seller/{sellerId}/contacts`, `GET /api/seller/{sellerId}/regions`, `GET /api/seller/{sellerId}/commission-rates`, `POST /api/seller/user/{userId}`, `PUT /api/seller/{sellerId}`, `PUT /api/seller/{sellerId}/status`

**Frontend:** `SellersView.vue` (nimekiri + otsing), `SellerView.vue` (detailvaade, read-only), `SellerFormView.vue` (lisamine + muutmine `?sellerId` parameetriga)

---

## Ülevaade

| Väli | Väärtus |
|------|---------|
| Branch | `task-03` |
| Raskus | 3/5 |
| Sõltuvused | task-01 (`userId` localStorage-is), task-02 (navbar olemas) |
| Seisund | — |

## Eesmärk

Võimaldada kasutajal näha edasimüüjate nimekirja ja detaile. Adminil lisaks lisada, muuta ja deaktiveerida edasimüüjaid.

---

## UI ülevaade

### SellersView.vue — `/sellers`

> Wireframe: `docs/balsamiq/views/SellersView.vue.png`

**Lehe struktuur:** Pealkiri "Edasimüüjad", otsingukast vasakul, "+ Lisa uus edasimüüja" nupp paremal (ainult Admin), all tabel.

**UI elemendid:**

| Element | Tüüp | Toiming |
|---------|------|---------|
| "Edasimüüjad" | Pealkiri | — |
| "Otsi edasimüüjat..." | Input | Filtreerib tabelit reaalajas (companyName järgi) |
| "+ Lisa uus edasimüüja" | Nupp (roheline, ainult Admin) | → `SellerFormView` (lisamise režiim) |
| Ettevõtte nimi | Veerg | — |
| Ettevõtte ID | Veerg | — |
| Staatus | Veerg | Badge: `"Aktiivne"` (roheline) / `"Peatatud"` (hall) |
| "Vaata" | Nupp (sinine) | → `SellerView` |
| "Muuda" | Nupp (sinine, ainult Admin) | → `SellerFormView` (muutmise režiim, `?sellerId=X`) |
| "Seaded" | Nupp (hall, ainult Admin) | → `SellerSettingsView` (task-05) |

**Rollipõhised erinevused:**
- Admin näeb: "+ Lisa uus edasimüüja", "Muuda", "Seaded"
- User näeb ainult: "Vaata"

**Navigatsioon sellest vaatest:**
- "Vaata" → `SellerView` (`/seller/{sellerId}`)
- "Muuda" → `SellerFormView` (`/seller/form?sellerId={sellerId}`)
- "+ Lisa uus edasimüüja" → `SellerFormView` (`/seller/form`)
- "Seaded" → `SellerSettingsView` (`/seller/{sellerId}/settings`) — task-05

---

### SellerView.vue — `/seller/:sellerId`

> Wireframe: `docs/balsamiq/views/SellerView.vue.png`

**Lehe struktuur:** "Tagasi nimekirja" nupp vasakul, "Seaded" nupp paremal (ainult Admin). Alla kolm sektsiooni: Üldandmed, Kontaktid, Teenustasud.

**UI elemendid:**

| Element | Tüüp | Toiming |
|---------|------|---------|
| "Tagasi nimekirja" | Nupp | → `SellersView` |
| "Seaded" | Nupp (hall, ainult Admin) | → `SellerSettingsView` (task-05) |
| Ettevõtte nimi | Tekst | — |
| Ettevõtte ID | Tekst | — |
| Staatus | Badge | `"Aktiivne"` / `"Peatatud"` |
| Lepingu algus | Tekst | `dd.MM.yyyy` |
| Piirkonnad ja müügipunktid | Tabel | Piirkond + müügipunktide arv |
| Märkused | Tekst | — |
| Kontaktid | Tabel | Nimi, Telefon, E-mail, Rollid |
| Teenustasud | Tabel | Tootegrupp, Tasu koguselt, Tasu % summalt, KM sees |

**Rollipõhised erinevused:**
- Admin näeb: "Seaded" nupp
- User: "Seaded" nupp peidetud

**Navigatsioon sellest vaatest:**
- "Tagasi nimekirja" → `SellersView`
- "Seaded" → `SellerSettingsView` (task-05)

---

### SellerFormView.vue — `/seller/form`

> Wireframe: `docs/balsamiq/views/SellerFormView.vue.png`

**Lehe struktuur:** Pealkiri muutub URL parameetri järgi — "Lisa uus edasimüüja" (ilma `?sellerId`) või "Muuda edasimüüja andmeid" (koos `?sellerId=X`). Kaks veergu põhiväljadele, all märkused, nupud.

**UI elemendid:**

| Element | Tüüp | Toiming |
|---------|------|---------|
| Ettevõtte nimi | Input (text, kohustuslik) | — |
| Ettevõtte ID | Input (number, kohustuslik) | `org_id` — peab olema unikaalne |
| Lepingu alguskuupäev | Input (date) | — |
| Lepingu lõppkuupäev | Input (date, valikuline) | Tühi = tähtajatu |
| Märkused | Textarea (valikuline) | — |
| "Tühista" | Nupp (hall) | → `SellersView` |
| "Lisa" / "Salvesta" | Nupp (sinine, spinner) | POST (lisa) või PUT (muuda) |
| Veateade | AlertError | Vale/puuduv väli, duplikaat org_id |

**Rollipõhised erinevused:**
- Ainult Admin — User ei pääse sellele vaatele

**Navigatsioon sellest vaatest:**
- "Tühista" → `SellersView`
- Edukas salvestamine → `SellersView` (koos `successMessage`)

---

## Backend

### Uued failid

| Fail | Kirjeldus |
|------|-----------|
| `controller/seller/SellerController.java` | Kõik seller endpointid |
| `controller/seller/dto/SellerDto.java` | Sisend: lisamine + muutmine |
| `controller/seller/dto/SellerStatusDto.java` | Sisend: staatuse muutmine |
| `controller/seller/dto/SellerResponseDto.java` | Vastus: nimekirja rida |
| `controller/seller/dto/SellerDetailResponseDto.java` | Vastus: detailvaade |
| `service/SellerService.java` | Äriloogika |
| `persistence/seller/Seller.java` | Entity (JPA) |
| `persistence/seller/SellerRepository.java` | Päringud |
| `persistence/seller/SellerMapper.java` | Entity ↔ DTO |

> **NB!** `Seller.java` entity võib olla juba olemas (task-00). Kontrolli enne loomist.

### API endpointid

| Meetod | Endpoint | Sisend DTO | Vastus DTO | HTTP kood |
|--------|----------|-----------|-----------|----------|
| GET | `/api/seller/user/{userId}` | — | `SellerResponseDto[]` | 200 OK |
| GET | `/api/seller/{sellerId}` | — | `SellerDetailResponseDto` | 200 OK |
| GET | `/api/seller/{sellerId}/contacts` | — | `SellerContactResponseDto[]` | 200 OK |
| GET | `/api/seller/{sellerId}/regions` | — | `SellerRegionResponseDto[]` | 200 OK |
| GET | `/api/seller/{sellerId}/commission-rates` | — | `CommissionRateResponseDto[]` | 200 OK |
| POST | `/api/seller/user/{userId}` | `SellerDto` | — | 201 Created |
| PUT | `/api/seller/{sellerId}` | `SellerDto` | — | 200 OK |
| PUT | `/api/seller/{sellerId}/status` | `SellerStatusDto` | — | 200 OK |

> **Märkus:** `contacts`, `regions`, `commission-rates` GET endpointid on task-03-s ainult lugemiseks (SellerView jaoks). POST/PUT/DELETE nendele tuleb task-05-s.

### DTO struktuurid

#### SellerResponseDto — nimekirja rida
```json
{
  "sellerId": 1,
  "companyName": "ETAS AS",
  "orgId": "10406134",
  "status": "ACTIVE"
}
```

#### SellerDetailResponseDto — detailvaade
```json
{
  "sellerId": 1,
  "companyName": "ETAS AS",
  "orgId": "10406134",
  "status": "ACTIVE",
  "contractStart": "01.01.2024",
  "contractEnd": null,
  "notes": "Märkused edasimüüja kohta"
}
```

#### SellerDto — sisend (lisamine + muutmine)
```json
{
  "companyName": "ETAS AS",
  "orgId": "10406134",
  "contractStart": "2024-01-01",
  "contractEnd": null,
  "notes": "Märkused"
}
```

#### SellerStatusDto — staatuse muutmine
```json
{ "status": "INACTIVE" }
```

#### SellerContactResponseDto — kontaktid (SellerView)
```json
[{
  "contactId": 1,
  "firstName": "Mari",
  "middleName": null,
  "lastName": "Maasikas",
  "phone": "55555555",
  "email": "mari@agent.ee",
  "roles": ["L", "A"]
}]
```

#### SellerRegionResponseDto — piirkonnad (SellerView)
```json
[{ "regionId": 1, "regionName": "Tallinn", "salesPointCount": 10 }]
```

#### CommissionRateResponseDto — teenustasud (SellerView)
```json
[{
  "commissionRateId": 1,
  "productTypeName": "isikustamine",
  "feePerTransaction": 0.01,
  "feePercent": null,
  "includesVat": false,
  "validFrom": "01.01.2024",
  "validTo": null
}]
```

### Veateated

| HTTP kood | Olukord | Kasutajale kuvatav sõnum |
|-----------|---------|--------------------------|
| 200 OK | Andmed laaditud / salvestatud | — |
| 201 Created | Uus edasimüüja lisatud | — |
| 400 Bad Request | Kohustuslik väli puudub | "Palun täitke kõik kohustuslikud väljad" |
| 404 Not Found | Edasimüüjat ei leitud | "Edasimüüjat ei leitud" |
| 409 Conflict | Sama org_id on juba olemas | "Selle ettevõtte ID-ga edasimüüja on juba olemas" |
| 500 Internal Server Error | Serveri viga | "Midagi läks valesti, proovi uuesti" |

### Olulised ärireeglid backend-is

**Staatuse teisendus (mapper):**
```
DB veerg    →  API JSON     →  UI kuvamine
"A"         →  "ACTIVE"     →  "Aktiivne"  (roheline badge)
"D"         →  "INACTIVE"   →  "Peatatud"  (hall badge)
```
Mapper kasutab `Status` enum-i — `ACTIVE.getCode()` tagastab `"A"`.

**`created_by`** — võetakse URL-i `{userId}` parameetrist, mitte request body-st.

**org_id unikaalsus** — enne salvestamist kontrollida kas sama `org_id` on juba olemas → `ConflictException` (409).

**Edasimüüjat ei kustutata** — `PUT /api/seller/{sellerId}/status` muudab staatuse `"D"`-ks (deaktiivne) või `"A"`-ks (aktiivne).

**Kuupäevad** — tagastatakse stringina formaadis `dd.MM.yyyy`, `null` = tähtajatu.

---

## Frontend

### Uued failid

| Fail | URL / asukoht | Kirjeldus |
|------|---------------|-----------|
| `views/SellersView.vue` | `/sellers` | Nimekiri + otsing |
| `views/SellerView.vue` | `/seller/:sellerId` | Detailvaade (read-only) |
| `views/SellerFormView.vue` | `/seller/form` | Lisamine + muutmine |
| `api-services/SellerService.js` | — | Kõik seller API kutsed |

### Muudetavad failid

| Fail | Muudatus |
|------|----------|
| `router/index.js` | Kolm uut route'i (asenda placeholderid) |
| `navigation/NavigationService.js` | Lisa `navigateToSellerView(sellerId)`, `navigateToSellerFormView(sellerId)` |

### Wireframe viited

- `docs/balsamiq/views/SellersView.vue.png`
- `docs/balsamiq/views/SellerView.vue.png`
- `docs/balsamiq/views/SellerFormView.vue.png`

### SellersView data() struktuur

```javascript
data() {
  return {
    sellers: [],          // API vastus
    searchQuery: '',      // otsingukast — filtreerib sellers[] reaalajas
    errorMessage: '',
  }
}
```

**Filtreerimine** — `computed` property `filteredSellers`:
```javascript
computed: {
  filteredSellers() {
    return this.sellers.filter(seller =>
      seller.companyName.toLowerCase().includes(this.searchQuery.toLowerCase())
    )
  }
}
```

### SellerView data() struktuur

```javascript
data() {
  return {
    seller: {
      sellerId: null,
      companyName: '',
      orgId: '',
      status: '',
      contractStart: null,
      contractEnd: null,
      notes: '',
    },
    contacts: [],
    regions: [],
    commissionRates: [],
    errorMessage: '',
  }
}
```

> **NB!** `beforeMount()` kutsub välja neli API päringut paralleelselt (seller detail + contacts + regions + commission-rates).

### SellerFormView data() struktuur

```javascript
data() {
  return {
    sellerId: null,         // $route.query.sellerId — null = lisamise režiim
    sellerData: {
      companyName: '',
      orgId: '',
      contractStart: '',
      contractEnd: '',
      notes: '',
    },
    errorMessage: '',
    showSpinner: false,
  }
}
```

**Lisamise vs muutmise režiim:**
- `sellerId === null` → pealkiri "Lisa uus edasimüüja", nupp "Lisa", kutsub `sendPostSeller()`
- `sellerId !== null` → pealkiri "Muuda edasimüüja andmeid", nupp "Salvesta", laadib olemasolevad andmed, kutsub `sendPutSeller()`

### API teenuse meetodid (SellerService.js)

| Meetodi nimi | HTTP kutse | Kirjeldus |
|-------------|-----------|-----------|
| `sendGetSellers(userId)` | `GET /api/seller/user/{userId}` | Kõik edasimüüjad |
| `sendGetSeller(sellerId)` | `GET /api/seller/{sellerId}` | Edasimüüja detail |
| `sendGetSellerContacts(sellerId)` | `GET /api/seller/{sellerId}/contacts` | Kontaktid |
| `sendGetSellerRegions(sellerId)` | `GET /api/seller/{sellerId}/regions` | Piirkonnad |
| `sendGetSellerCommissionRates(sellerId)` | `GET /api/seller/{sellerId}/commission-rates` | Teenustasud |
| `sendPostSeller(userId, sellerData)` | `POST /api/seller/user/{userId}` | Lisa edasimüüja |
| `sendPutSeller(sellerId, sellerData)` | `PUT /api/seller/{sellerId}` | Muuda edasimüüja |
| `sendPutSellerStatus(sellerId, statusData)` | `PUT /api/seller/{sellerId}/status` | Muuda staatus |

### NavigationService — uued meetodid

```javascript
navigateToSellerView(sellerId) {
  router.push({ name: 'sellerRoute', params: { sellerId } })
},
navigateToSellerFormView(sellerId = null) {
  router.push({
    name: 'sellerFormRoute',
    query: sellerId ? { sellerId } : undefined
  })
},
navigateToSellersView(successMessage = null) {
  router.push({
    name: 'sellersRoute',
    query: successMessage ? { successMessage } : undefined
  })
},
```

### Router — uued route'id

```javascript
{ path: '/sellers',             name: 'sellersRoute',    component: SellersView },
{ path: '/seller/:sellerId',    name: 'sellerRoute',     component: SellerView },
{ path: '/seller/form',         name: 'sellerFormRoute', component: SellerFormView },
```

### Rollipõhised erinevused UI-s

- `isAdmin` loetakse `AuthService.getRole() === 'A'` — salvestada `data()`-sse
- "+ Lisa uus edasimüüja": `v-if="isAdmin"`
- "Muuda" nupp: `v-if="isAdmin"`
- "Seaded" nupp: `v-if="isAdmin"`

---

## Valmiduse kriteeriumid

### Backend
- [ ] `GET /api/seller/user/{userId}` tagastab aktiivsete ja mitteaktiivsete edasimüüjate nimekirja
- [ ] `GET /api/seller/{sellerId}` tagastab ühe edasimüüja detailid
- [ ] `GET /api/seller/{sellerId}/contacts` tagastab kontaktide nimekirja
- [ ] `GET /api/seller/{sellerId}/regions` tagastab piirkondade nimekirja
- [ ] `GET /api/seller/{sellerId}/commission-rates` tagastab teenustasude nimekirja
- [ ] `POST /api/seller/user/{userId}` lisab uue edasimüüja (201 Created)
- [ ] Duplikaat `org_id` → 409 Conflict
- [ ] `PUT /api/seller/{sellerId}` uuendab edasimüüja andmed
- [ ] `PUT /api/seller/{sellerId}/status` muudab staatuse
- [ ] Staatus teisendub õigesti: DB `"A"`/`"D"` ↔ API `"ACTIVE"`/`"INACTIVE"`
- [ ] Kuupäevad tagastatakse formaadis `dd.MM.yyyy`

### Frontend
- [ ] `SellersView` laadib ja kuvab edasimüüjate nimekirja
- [ ] Otsingukast filtreerib nimekirja reaalajas
- [ ] Staatus kuvatakse badge-ina ("Aktiivne" / "Peatatud")
- [ ] Admin nupud peidetud User rolliga
- [ ] `SellerView` kuvab kõik sektsioonid (üldandmed, kontaktid, piirkonnad, teenustasud)
- [ ] `SellerFormView` töötab nii lisamis- kui muutmisrežiimis
- [ ] Edukas salvestamine suunab `SellersView`-sse koos `successMessage`-ga
- [ ] Duplikaat `org_id` kuvab veateate (409)
- [ ] Spinner nähtav päringu ajal

---

## Märkused

- **Staatuse teisendus mapperis** on selle taski üks keerulisemaid kohti — `Status` enum kasutamine + MapStruct `expression` või custom meetod
- **SellerFormView** töötab ühe URL-iga kahes režiimis — `?sellerId` puudumine = lisamise režiim
- **SellerView** teeb `beforeMount()`-is mitu API päringut — kõik kutsutakse välja korraga
- **Otsing** on client-side (filtreeritakse juba laaditud `sellers[]` massiivi) — server-side otsing lisatakse tulevikus kui andmemaht kasvab
- **`/seller/:sellerId` vs `/seller/form`** — Vue Router võib segi ajada `:sellerId` parameetri ja `form` staatilise segmendiga. Kui probleem tekib, pane `sellerFormRoute` `sellerRoute`-st ettepoole router-is
- **task-05 sõltuvus** — "Seaded" nupp navigeerib `SellerSettingsView`-le mis on task-05. Praegu navigatsioon lisatakse aga vaade on tühi kuni task-05 valmib
