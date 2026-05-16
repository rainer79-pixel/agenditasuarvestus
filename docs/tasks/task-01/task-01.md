# Task-01: Login ja autentimine

## Mis selles taskis teeme?

Ehitame süsteemi sissepääsu — kasutaja saab avada avalehe, klõpsata "Logi sisse" ja sisestada oma e-maili ning parooli. Backend otsib andmebaasist kasutaja email + parool + aktiivne staatus järgi — kui vastet ei leita, tagastatakse 403. Frontend salvestab kasutaja info localStorage-i ja suunab kasutaja töölauale.

See on esimene ja kõige kriitilisem task — ilma autentimiseta ei saa ühtegi teist funktsionaalsust kasutada. Kõik järgnevad taskid eeldavad et `userId` ja `role` on localStorage-is olemas.

**Backend:** `POST /api/login` endpoint — lihtne JPQL päring emaili, parooli ja staatuse järgi. Spring Security ega BCrypt puuduvad.

**Frontend:** `HomeView.vue` (avaleht "Logi sisse" nupuga) + `LoginView.vue` (loginvorm) + `AuthService.js` (localStorage) + `NavigationService.js` (navigatsioon) + Router algseadistus.

---

## Ülevaade

| Väli | Väärtus |
|------|---------|
| Branch | `task-01` |
| Raskus | 2/5 |
| Sõltuvused | task-00 (AppUser entity olemas) |
| Seisund | — |

## Eesmärk

Võimaldada kasutajal süsteemi sisse logida e-maili ja parooliga. Backend otsib kasutaja andmebaasist emaili, parooli ja aktiivse staatuse järgi, frontend salvestab kasutaja info localStorage-i ja suunab kasutaja edasi töölauale.

---

## UI ülevaade

### HomeView.vue — `/`

> Wireframe: `docs/balsamiq/views/HomeView.vue.png`

**Lehe struktuur:** Täisleht ilma navigatsiooniribata. Keskel logo, kirjeldus ja nupp. Allosas footer.

**UI elemendid:**

| Element | Tüüp | Toiming |
|---------|------|---------|
| "Edasimüüja Teenustasu Arvutussüsteem (ETAS)" | Pealkiri (ülaosas) | — |
| TeenustasuKalkulaator logo + ikoon | Pilt | — |
| "Sisekasutuseks mõeldud tööriist..." | Kirjeldus tekst | — |
| "Logi sisse" | Nupp (sinine) | Navigeerib → `LoginView` |
| "© 2026 ETAS" | Footer tekst | — |

**Rollipõhised erinevused:** Puuduvad — avaleht on avalik, navigatsioonibar puudub.

**Navigatsioon sellest vaatest:**
- "Logi sisse" nupp → `LoginView` (`/login`)

---

### LoginView.vue — `/login`

> Wireframe: `docs/balsamiq/views/LoginView.vue.png`

**Lehe struktuur:** Tsentreeritud kaart ilma navigatsiooniribata. Pealkiri "ETAS", alampealkiri "Palun logige sisse", kaks sisendvälja, nupp.

**UI elemendid:**

| Element | Tüüp | Toiming |
|---------|------|---------|
| "ETAS" | Pealkiri | — |
| "Palun logige sisse" | Alampealkiri | — |
| E-mail | Input (text) | Kasutaja e-mail |
| Parool | Input (password) | Kasutaja parool |
| "Logi sisse" | Nupp (sinine, täislaius) | Saadab `POST /api/login` |
| "Edasimüüja Teenustasu Arvutussüsteem" | Alltekst | — |
| Veateade | AlertError | Kuvatakse vale parooli / puuduva välja korral |

**Rollipõhised erinevused:** Puuduvad — loginvaade on kõigile sama.

**Navigatsioon sellest vaatest:**
- Edukas login → `DashboardView` (`/dashboard`)

---

## Backend

### Uued failid

| Fail | Kirjeldus |
|------|-----------|
| `controller/login/LoginController.java` | `POST /api/login` endpoint |
| `controller/login/dto/LoginDto.java` | Sisend: email + password |
| `controller/login/dto/LoginResponseDto.java` | Vastus: userId, firstName, middleName, lastName, role |
| `service/AppUserService.java` | Kasutaja otsimine — delegeerib repository-le |
| `persistence/appuser/AppUserRepository.java` | JPQL päring: email + password + status |
| `persistence/appuser/AppUserMapper.java` | `AppUser` → `LoginResponseDto` |

### API endpointid

| Meetod | Endpoint | Sisend DTO | Vastus DTO | Õnnestub |
|--------|----------|-----------|-----------|----------|
| POST | `/api/login` | `LoginDto` | `LoginResponseDto` | 200 OK |

### DTO struktuurid

#### LoginDto — sisend
```json
{
  "email": "mari@agent.ee",
  "password": "********"
}
```

#### LoginResponseDto — vastus
```json
{
  "userId": 1,
  "firstName": "Mari",
  "middleName": null,
  "lastName": "Maasikas",
  "role": "A"
}
```

### Veateated

| HTTP kood | Olukord | Kasutajale kuvatav sõnum |
|-----------|---------|--------------------------|
| 200 OK | Sisselogimine õnnestus | — |
| 400 Bad Request | Email või parool puudub | "Palun täitke kõik väljad" |
| 403 Forbidden | Vale email või parool | "Vale e-mail või parool" |
| 500 Internal Server Error | Serveri viga | "Midagi läks valesti, proovi uuesti" |

### Olulised ärireeglid backend-is

- **Lihtne autentimine** — Spring Security ja BCrypt puuduvad, parool salvestatakse lihttekstina
- Repository otsib otse: `email + password + userStatus = "A"` — kui vastet pole → 403
- Login käib **e-mailiga** — email on `app_user` tabelis unikaalne
- `user_role` veeru väärtus: `"A"` = Admin, `"U"` = User — tagastatakse otse `LoginResponseDto`-s
- Deaktiveeritud kasutaja (`user_status = "D"`) ei leita päringuga — tagastab automaatselt 403

**Repository JPQL näide (bank40back mustri järgi):**
```java
@Query("select u from AppUser u where u.email = :email and u.password = :password and u.userStatus = :status")
Optional<AppUser> findAppUserBy(String email, String password, String status);
```

---

## Frontend

### Uued failid

| Fail | URL / asukoht | Kirjeldus |
|------|---------------|-----------|
| `views/HomeView.vue` | `/` | Avaleht logo + "Logi sisse" nupuga |
| `views/LoginView.vue` | `/login` | Sisselogimise vorm |
| `auth/AuthService.js` | — | Kasutaja info salvestamine/lugemine localStorage-ist |
| `navigation/NavigationService.js` | — | Tsentraalne navigeerimine (algseadistus) |
| `router/index.js` | — | Route-ide definitsioon (algseadistus) |
| `api-services/LoginService.js` | — | `POST /api/login` kutse |

### Wireframe viited

- `docs/balsamiq/views/HomeView.vue.png`
- `docs/balsamiq/views/LoginView.vue.png`

### LoginView komponendi data() struktuur

```javascript
data() {
  return {
    loginData: {
      email: '',
      password: '',
    },
    errorMessage: '',
    showSpinner: false,
  }
}
```

### API teenuse meetodid (LoginService.js)

| Meetodi nimi | HTTP kutse | Kirjeldus |
|-------------|-----------|-----------|
| `sendPostLogin(loginData)` | `POST /api/login` | Saadab login andmed |

### AuthService.js — localStorage väljad

```javascript
saveUserInfo(data) {
  localStorage.setItem('userId', data.userId)
  localStorage.setItem('firstName', data.firstName)
  localStorage.setItem('middleName', data.middleName)
  localStorage.setItem('lastName', data.lastName)
  localStorage.setItem('role', data.role)
}
```

### NavigationService.js — algseis (vajalikud meetodid)

```javascript
navigateToHomeView()       // → /
navigateToLoginView()      // → /login
navigateToDashboardView()  // → /dashboard
```

### Router algseis

| Route nimi | Path | Komponent |
|-----------|------|-----------|
| `homeRoute` | `/` | `HomeView.vue` |
| `loginRoute` | `/login` | `LoginView.vue` |

### Navigatsioon

- `HomeView` "Logi sisse" nupp → `LoginView`
- Edukas login → `DashboardView`
- "Logi välja" (tulevased vaated) → `LoginView` + `AuthService.logOut()`

### Rollipõhised erinevused UI-s

Puuduvad selles taskis — HomeView ja LoginView on kõigile ühesugused.

---

## Valmiduse kriteeriumid

### Backend
- [ ] `POST /api/login` töötab Swagger UI-s
- [ ] Õige email + parooliga tagastab 200 + `LoginResponseDto`
- [ ] Vale email või parooliga tagastab 403
- [ ] Puuduva väljaga tagastab 400
- [ ] Deaktiveeritud kasutaja (`status = "D"`) tagastab 403

### Frontend
- [ ] `HomeView` avaneb URL-il `/` koos logo ja nupuga
- [ ] "Logi sisse" nupp viib `LoginView`-sse
- [ ] Edukas login salvestab info localStorage-i ja suunab `/dashboard`-ile
- [ ] Vale parool kuvab veateate (`errorMessage`)
- [ ] Tühi väli kuvab veateate
- [ ] Spinner nähtav päringu ajal, nupp disabled

---

## Märkused

- **Lihtne autentimine bank40back mustri järgi** — ei kasuta Spring Security ega BCrypt, parool lihttekstina DB-s
- `DashboardView` on task-02 — selle taski käigus piisab kui `/dashboard` route on olemas (tühi leht)
- `App.vue` navbar kuvatakse ainult sisselogitud kasutajale — kontrolliks `AuthService.isLoggedIn()`
- Paroolid testandmetes: `mari@agent.ee` / `123`, `jt@agent.ee` / `123` (vt `database/3_import.sql`)
