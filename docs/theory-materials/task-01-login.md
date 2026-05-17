# Task-01: Login ja autentimine — algajatele

## Mis see on ja miks oluline?

Login on iga veebirakenduse esimene samm — ilma selleta ei pääse kasutaja rakendust kasutama.
Task-01 ehitab täieliku sisselogimise ahela: kasutaja sisestab e-maili ja parooli, backend kontrollib neid andmebaasist ja frontend salvestab vastuse brauseri mällu.

**Päriselust analoogia:** Login on nagu ukselukk — võti (parool) peab sobima lukuga (andmebaas). Kui sobib, uks avaneb (dashboard). Kui ei sobi, uks jääb kinni (403).

---

## Andmevoog — suur pilt

```
Kasutaja trükib email + parool
        ↓
    LoginView.vue
        ↓
    LoginService.js  →  POST /api/login  →  LoginController.java
                                                    ↓
                                            LoginService.java
                                                    ↓
                                            UserRepository.java
                                                    ↓
                                            Andmebaas (app_user tabel)
                                                    ↓
                                            UserMapper.java
                                                    ↓
                                        LoginResponseDto ← ← ← ←
                                                              ↓
                        AuthService.js  ←  axios vastus  ← ←
                              ↓
                        localStorage
                              ↓
                        NavigationService.js → /dashboard
```

---

## BACKEND

### 1. LoginDto — sisend

```java
@Data @NoArgsConstructor @AllArgsConstructor
public class LoginDto {
    @NotBlank(message = "Email on kohustuslik")
    private String email;

    @NotBlank(message = "Parool on kohustuslik")
    private String password;
}
```

**Mis see on?** DTO (Data Transfer Object) on andmekandja — lihtne klass mis hoiab andmeid.
`LoginDto` võtab vastu selle mida browser saadab:

```json
{ "email": "mari@agent.ee", "password": "123" }
```

Spring teisendab JSON automaatselt `LoginDto` objektiks.

**Miks `@NotBlank`?** Kontrollib et väli pole tühi — kui on tühi, tagastab 400 enne kui backend üldse midagi teeb.
**Oluline:** `@NotBlank` käib ainult välja peal, mitte klassi peal!

---

### 2. LoginResponseDto — vastus

```java
@Data @NoArgsConstructor @AllArgsConstructor
public class LoginResponseDto {
    private Integer userId;
    private String firstName;
    private String middleName;
    private String lastName;
    private String role;
}
```

**Mis see on?** See on mida backend tagastab — ainult need andmed mida frontend vajab.
Parool, email, staatus ei lähe tagasi — need pole frontendil vaja.

```json
{ "userId": 1, "firstName": "Mari", "middleName": null, "lastName": "Maasikas", "role": "A" }
```

---

### 3. UserRepository — andmebaasipäring

```java
public interface UserRepository extends JpaRepository<User, Integer> {

    @Query("""
            select u from User u
            where upper(u.email) = upper(:email)
            and u.password = :password
            and u.userStatus = :userStatus""")
    Optional<User> findUserBy(@Param("email") String email,
                               @Param("password") String password,
                               @Param("userStatus") String userStatus);
}
```

**Mis see on?** Repository räägib andmebaasiga. `JpaRepository` annab tasuta `findAll()`, `save()` jm meetodid.
Kohandatud `@Query` otsib kasutajat kolme tingimusega korraga.

**Miks `Optional`?** Kasutaja võib olla olemas või mitte — `Optional` on turvaline viis seda väljendada.
`orElseThrow()` viskab erandi kui kasutajat ei leita.

**Miks `upper()`?** E-mail on tõstutundetu — `Mari@agent.ee` ja `mari@agent.ee` on sama.

**Miks kolm tingimust?**
- `email` — tuvastab kasutaja
- `password` — kontrollib parooli
- `userStatus = 'A'` — deaktiveeritud kasutaja (`'D'`) ei leia vastet → automaatselt 403

---

### 4. UserMapper — teisendus

```java
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {
    @Mapping(source = "id", target = "userId")
    @Mapping(source = "userRole", target = "role")
    LoginResponseDto toLoginResponseDto(User user);
}
```

**Mis see on?** Mapper teisendab entity (andmebaasi objekt) → DTO (vastuse objekt).

**Miks vaja?** Entity `id` → DTO `userId` (nimi erineb), entity `userRole` → DTO `role` (nimi erineb).
Ülejäänud väljad (`firstName`, `middleName`, `lastName`) kattuvad nimepidi — MapStruct täidab automaatselt.

```
Entity                    DTO
  id         →→→→→→→→    userId
  userRole   →→→→→→→→    role
  firstName  →→→→→→→→    firstName   (automaatne)
  middleName →→→→→→→→    middleName  (automaatne)
  lastName   →→→→→→→→    lastName    (automaatne)
```

---

### 5. LoginService — äriloogika

```java
@Service
@RequiredArgsConstructor
public class LoginService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public LoginResponseDto getLoginResponse(String email, String password) {
        User user = findUserBy(email, password);
        return userMapper.toLoginResponseDto(user);
    }

    private User findUserBy(String email, String password) {
        return userRepository.findUserBy(email, password, ACTIVE.getCode())
                .orElseThrow(() -> new ForbiddenException(
                        INCORRECT_CREDENTIALS.getMessage(),
                        INCORRECT_CREDENTIALS.getErrorCode()));
    }
}
```

**Mis see on?** Service on äriloogika kiht — teeb töö ära.

**Muster:** avalik meetod on lühike (3-7 rida) ja delegeerib privaatsetele.
- `getLoginResponse()` — avalik, koordineerib
- `findUserBy()` — privaatne abiline, otsib kasutaja

**`ACTIVE.getCode()`** tagastab `"A"` — täpselt mida andmebaas ootab.
**`orElseThrow()`** — kui kasutajat ei leita, viskab `ForbiddenException` → 403.

---

### 6. LoginController — HTTP päring

```java
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LoginController {
    private final LoginService loginService;

    @PostMapping("/login")
    @Operation(summary = "Sisselogimine")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Vale e-mail või parool",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))})
    public LoginResponseDto login(@Valid @RequestBody LoginDto loginDto) {
        return loginService.getLoginResponse(loginDto.getEmail(), loginDto.getPassword());
    }
}
```

**Mis see on?** Controller on uks — võtab HTTP päringu vastu ja annab vastuse.

**Miks POST, mitte GET?**
- GET saadaks parooli URL-is: `/api/login?password=123` — nähtav logides, ebaturvaline
- POST saadab parooli JSON body-s — turvalisem

**`@Valid`** käivitab `LoginDto` validatsiooni — tühja välja korral 400.
**`@RequestBody`** ütleb Springile: "loe andmed HTTP body-st, mitte URL-ist".

---

### Veakoodid

| HTTP kood | Olukord | Sõnum |
|-----------|---------|-------|
| 200 OK | Sisselogimine õnnestus | — |
| 400 Bad Request | Email või parool puudub | "email: Email on kohustuslik" |
| 403 Forbidden | Vale email/parool või deaktiveeritud konto | "Sisselogimine ebaõnnestus, võtke ühendust administraatoriga" |

---

## FRONTEND

### 7. router/index.js — navigatsioonikaart

```javascript
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/',          name: 'homeRoute',      component: HomeView },
    { path: '/login',     name: 'loginRoute',     component: LoginView },
    { path: '/dashboard', name: 'dashboardRoute', component: DashboardView },
  ],
})
```

**Mis see on?** Router ütleb Vue'ile: "kui URL on `/login`, näita `LoginView` komponenti".

**Miks `name` väli?** Navigeerime nimega, mitte URL-iga:
```javascript
router.push({ name: 'loginRoute' })  // parem
router.push('/login')                 // töötab, aga URL muutusel läheb katki
```

---

### 8. AuthService.js — localStorage haldur

```javascript
export default {
  isLoggedIn() { return localStorage.getItem('userId') !== null },
  getUserId()  { return localStorage.getItem('userId') },
  getRole()    { return localStorage.getItem('role') },
  saveUserInfo(data) {
    localStorage.setItem('userId',     data.userId)
    localStorage.setItem('firstName',  data.firstName)
    localStorage.setItem('middleName', data.middleName)
    localStorage.setItem('lastName',   data.lastName)
    localStorage.setItem('role',       data.role)
  },
  logOut() { localStorage.clear() },
}
```

**Miks localStorage?** Vue "unustab" kõik kui leht uuesti laaditakse. localStorage püsib kuni `logOut()`.

**`isLoggedIn()`** — kasutatakse tulevikus route guard-is: kui `false` → suuna tagasi `/login`.

---

### 9. NavigationService.js — navigatsioon

```javascript
import router from '@/router/index.js'

export default {
  navigateToHomeView()      { router.push({ name: 'homeRoute' }) },
  navigateToLoginView()     { router.push({ name: 'loginRoute' }) },
  navigateToDashboardView() { router.push({ name: 'dashboardRoute' }) },
}
```

**Mis see on?** Tsentraalne navigatsioonihaldur — kõik navigeerimised ühes kohas.
Komponentid ei pea URL-e teadma, kutsuvad lihtsalt `navigateToLoginView()`.

---

### 10. LoginService.js — API päring

```javascript
import axios from 'axios'

export default {
  sendPostLogin(loginData) {
    return axios.post('/api/login', loginData)
  },
}
```

**Mis see on?** Axios saadab HTTP POST päringu backendile.
`loginData` objekt `{ email, password }` teisendatakse automaatselt JSON-iks.
`return` tagastab `Promise` — LoginView saab `.then().catch()`-ga vastust käsitleda.

---

### 11. LoginView.vue — script

```javascript
data() {
  return {
    loginData: { email: '', password: '' },  // seotud v-model-iga
    errorMessage: '',                         // veateade ekraanil
    showSpinner: false,                       // päringu ajal true
  }
},
methods: {
  login() {
    if (!this.loginData.email || !this.loginData.password) {
      this.errorMessage = 'Palun täitke kõik väljad'
      return
    }
    this.showSpinner = true
    LoginService.sendPostLogin(this.loginData)
      .then((response) => this.handleLoginResponse(response.data))
      .catch((error)   => this.handleLoginError(error))
      .finally(()      => { this.showSpinner = false })
  },
  handleLoginResponse(data) {
    AuthService.saveUserInfo(data)
    NavigationService.navigateToDashboardView()
  },
  handleLoginError(error) {
    this.errorMessage = error.response.data.message
  },
  navigateToHome() {
    NavigationService.navigateToHomeView()
  },
}
```

**`v-model`** — kahepoolne side: kasutaja trükib → `loginData.email` uueneb automaatselt.

**`Promise` ja `.then().catch().finally()`:**
- `.then()` — backend vastas 200 → salvesta localStorage, suuna `/dashboard`
- `.catch()` — backend vastas 403/400 → kuva veateade
- `.finally()` — alati → peida spinner

**Miks `loginData` on objekt, mitte kaks eraldi muutujat?**
Saame saata kogu objekti korraga: `sendPostLogin(this.loginData)` — ei pea kirjutama `sendPostLogin(this.email, this.password)`.

---

## Kokkuvõte — võtmemõisted

| Mõiste | Selgitus |
|--------|---------|
| DTO | Andmekandja klass — liigutab andmeid kihtide vahel |
| Entity | Andmebaasi tabeli peegeldus Java objektina |
| Mapper | Teisendab entity → DTO (MapStruct teeb automaatselt) |
| Repository | Räägib andmebaasiga — JPQL päringud |
| Service | Äriloogika — koordineerib repository ja mapper tööd |
| Controller | HTTP uks — võtab päringu vastu, tagastab vastuse |
| Optional | Turvaline viis "võib olla olemas või mitte" väljendamiseks |
| Promise | "Lubadus" — async vastus `.then().catch()` ahelaga |
| localStorage | Brauseri mälu — püsib kuni `clear()` kutsutakse |
| v-model | Kahepoolne side Vue komponendi ja HTML välja vahel |
| axios | HTTP päringute teek — `get`, `post`, `put`, `delete` |
| router | Navigatsioonikaart — URL → komponent |

---

## Järgmised sammud

- **Task-02:** DashboardView — esimene vaade pärast sisselogimist
- **Route guard:** `isLoggedIn()` rakendamine — kaitsta vaateid mitte-sisselogitud kasutaja eest
- **BCrypt:** paroolide krüpteerimine (praegu lihttekst)
