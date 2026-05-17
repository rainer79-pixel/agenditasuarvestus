# Task-02: Dashboard ja navigatsioonibaar

## Mis selles taskis teeme?

Ehitame töölaua — esimese lehe mida kasutaja näeb pärast sisselogimist. Dashboard kuvab kaks statistikakaart: edasimüüjate arv süsteemis ja viimase impordi kuupäev. Lisaks ehitame navigatsiooniriba (`App.vue`) mis kuvatakse kõigil lehtedel peale `HomeView` ja `LoginView`.

**Backend:** `GET /api/dashboard` endpoint — kaks päringut: aktiivsete edasimüüjate arv ja viimase impordi kuupäev.

**Frontend:** `DashboardView.vue` (kaks statistikakaarti + tervitus) + `App.vue` navbar (menüü + kasutajanimi + "Logi välja") + `DashboardService.js`.

---

## Ülevaade

| Väli | Väärtus |
|------|---------|
| Branch | `task-02` |
| Raskus | 2/5 |
| Sõltuvused | task-01 (login valmis, `userId` localStorage-is) |
| Seisund | — |

## Eesmärk

Kuvada kasutajale pärast sisselogimist töölaud kahe statistikakaardiga. Lisada navigatsioonibaar mis on nähtav kõigil sisselogitud vaadetel.

---

## UI ülevaade

### DashboardView.vue — `/dashboard`

> Wireframe: `docs/balsamiq/views/DashboardView.vue.png`

**Lehe struktuur:** Navigatsioonibaar üleval, all tervitus ja kaks statistikakaarti kõrvuti.

**UI elemendid:**

| Element | Tüüp | Toiming |
|---------|------|---------|
| "Tere tulemast, [firstName] [lastName]!" | Pealkiri | Kasutaja nimi localStorage-ist |
| "Edasimüüjaid süsteemis" | Kaart | Arv backendist |
| "Viimane import" | Kaart | Kuupäev backendist |

**Navigatsioon sellest vaatest:**
- Navbar menüülingid → teised vaated

---

### App.vue — Navigatsioonibaar

**Navbar elemendid:**

| Element | Tüüp | Toiming |
|---------|------|---------|
| Töölaud | Link | → `/dashboard` |
| Edasimüüjad | Link | → `/sellers` |
| Aruanded | Link | → `/reports` |
| Arvete kontroll | Link | → `/invoice-control` |
| Seaded | Link | → `/settings` |
| `[firstName] [lastName]` | Tekst | Kasutaja nimi localStorage-ist |
| "Logi välja" | Nupp (punane) | `AuthService.logOut()` → `HomeView` |

**Rollipõhised erinevused:**
- `Seaded` link kuvatakse ainult Admin rollile (`role === 'A'`)

---

## Backend

### Uued failid

| Fail | Kirjeldus |
|------|-----------|
| `controller/dashboard/DashboardController.java` | `GET /api/dashboard` endpoint |
| `controller/dashboard/dto/DashboardResponseDto.java` | Vastus: sellerCount, lastImport |
| `service/DashboardService.java` | Statistika kogumine |
| `persistence/seller/SellerRepository.java` | Aktiivsete edasimüüjate arv |
| `persistence/salesreport/SalesReportRepository.java` | Viimase impordi kuupäev |

### API endpointid

| Meetod | Endpoint | Vastus DTO | Õnnestub |
|--------|----------|-----------|----------|
| GET | `/api/dashboard` | `DashboardResponseDto` | 200 OK |

### DTO struktuurid

#### DashboardResponseDto — vastus
```json
{
  "sellerCount": 2,
  "lastImport": "15.04.2026"
}
```

### Veateated

| HTTP kood | Olukord | Kasutajale kuvatav sõnum |
|-----------|---------|--------------------------|
| 200 OK | Andmed laaditud | — |
| 500 Internal Server Error | Serveri viga | "Midagi läks valesti, proovi uuesti" |

### Olulised ärireeglid backend-is

- `sellerCount` — ainult aktiivsed edasimüüjad (`status = 'A'`)
- `lastImport` — viimase `sales_report` rea `created_at` kuupäev, formaat `dd.MM.yyyy`
- Kui importi pole tehtud → `lastImport` on `null`

---

## Frontend

### Uued failid

| Fail | URL / asukoht | Kirjeldus |
|------|---------------|-----------|
| `views/DashboardView.vue` | `/dashboard` | Töölaud kahe statistikakaardiga |
| `api-services/DashboardService.js` | — | `GET /api/dashboard` kutse |

### Muudetavad failid

| Fail | Muudatus |
|------|----------|
| `App.vue` | Navbar lisamine — kuvatakse kui `AuthService.isLoggedIn()` |
| `navigation/NavigationService.js` | Uued meetodid kõigi vaadete jaoks |

### Wireframe viide

- `docs/balsamiq/views/DashboardView.vue.png`

### DashboardView komponendi data() struktuur

```javascript
data() {
  return {
    dashboard: {
      sellerCount: 0,
      lastImport: null,
    },
    errorMessage: '',
  }
}
```

### API teenuse meetodid (DashboardService.js)

| Meetodi nimi | HTTP kutse | Kirjeldus |
|-------------|-----------|-----------|
| `sendGetDashboard()` | `GET /api/dashboard` | Laadib dashboard statistika |

### App.vue — navbar loogika

```javascript
// Navbar kuvatakse ainult sisselogitud kasutajale
// AuthService.isLoggedIn() → true/false
// AuthService.getRole() === 'A' → Seaded link nähtav
// AuthService.getFirstName() + getLastName() → kasutajanimi navbar-is
```

### NavigationService.js — uued meetodid

```javascript
navigateToSellersView()
navigateToReportsView()
navigateToInvoiceControlView()
navigateToSettingsView()
```

---

## Valmiduse kriteeriumid

### Backend
- [ ] `GET /api/dashboard` töötab Swagger UI-s
- [ ] `sellerCount` tagastab aktiivsete edasimüüjate arvu
- [ ] `lastImport` tagastab viimase impordi kuupäeva formaadis `dd.MM.yyyy`
- [ ] Kui importi pole → `lastImport` on `null`

### Frontend
- [ ] `DashboardView` avaneb URL-il `/dashboard`
- [ ] Tervitus kuvab kasutaja nime localStorage-ist
- [ ] Mõlemad statistikakaardid kuvavad backendi andmeid
- [ ] Navbar kuvatakse `DashboardView`-l
- [ ] "Logi välja" nupp logib välja ja suunab `HomeView`-sse
- [ ] `Seaded` link on nähtav ainult Admin rollile

---

## Märkused

- Navbar kuvatakse ainult sisselogitud vaadetel — `App.vue`-s kontrollitakse `AuthService.isLoggedIn()`
- `HomeView` ja `LoginView` ei näita navbar'i
- `DashboardView` laadib andmed `beforeMount()` hook-is
