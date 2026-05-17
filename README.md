# ETAS

> Kehtib mõlemale alaprojektile: `backend` ja `frontend`

**Edasimüüja Teenustasu Arvutussüsteem** — ettevõttesisene veebirakendus edasimüüjate lepinguandmete
haldamiseks, teenustasude arvutamiseks Exceli müügiaruande põhjal ning edasimüüjate arvete
õigsuse kontrollimiseks. Süsteem võimaldab juhtkonnale esitada müügi- ja teenustasude ülevaateid.

## Projektid

- `backend` — Spring Boot backend (Java 21, Spring Boot 4.0.6)
- `frontend` — Vue.js frontend (Vue 3, Bootstrap 5, Axios)
- `database/` — SQL skriptid (schema loomine, seed-andmed)

## Tehnoloogiad

### Backend
- Java 21
- Spring Boot 4.0.6
- PostgreSQL 17
- Hibernate / Spring Data JPA
- MapStruct 1.6
- Lombok
- Swagger (springdoc-openapi)
- p6spy (SQL logimine arenduses)
- Apache POI (Excel import)
- Spring Security (sessioonipõhine autentimine)

### Frontend
- Vue.js 3 (Options API — `data()`, `methods`, `beforeMount()`)
- Vue Router 5
- Pinia 3
- Bootstrap 5
- Axios
- @phosphor-icons/vue
- Vite

**Frontend arhitektuur:**
- `api-services/` — kõik API kutsed koondatud teenusefailidesse (`SellerService.js` jne)
- `auth/AuthService.js` — kasutaja info localStorage'is (userId, firstName, lastName, role)
- `navigation/NavigationService.js` — tsentraalne navigeerimine
- `components/common/` — üldised UI-elemendid (AlertError, AlertSuccess jms)
- `components/forms/` — vormikomponendid
- `components/modals/` — modaalaknad
- `components/tables/` — tabelikomponendid

## Käivitamine

### Backend
```shell
cd backend
./gradlew bootRun
```

### Frontend
```shell
cd frontend
npm install
npm run dev
```

Backend käivitub pordil: http://localhost:8080

Frontend käivitub pordil: http://localhost:8081

Swagger UI: http://localhost:8080/swagger-ui/index.html

### Andmebaas
- PostgreSQL 17, port 5432
- Schema: `etas`
- Kasutaja: `postgres`

## Kasutajarollid

- **Admin** — haldab edasimüüjaid, kasutajaid, teenustasusid ja süsteemi seadistusi
- **User** — kasutab tööriista igapäevaselt (import, arvutus, aruanded)

| Funktsioon | Admin | User |
|-----------|-------|------|
| Edasimüüjate vaatamine | ✓ | ✓ |
| Edasimüüjate lisamine ja muutmine | ✓ | ✗ |
| Edasimüüja deaktiveerimine | ✓ | ✗ |
| Teenustasude seadistamine | ✓ | ✗ |
| Excel faili üleslaadimine | ✓ | ✓ |
| Aruannete vaatamine + XLS eksport | ✓ | ✓ |
| Arvete sisestamine ja kontroll | ✓ | ✓ |
| Kasutajate haldus | ✓ | ✗ |
| KM määra muutmine | ✓ | ✗ |
| Seadistuste vaatamine ja muutmine | ✓ | ✗ |

## Vaated (10)

| Vaade | URL | Kirjeldus |
|-------|-----|-----------|
| `HomeView.vue` | `/` | Avalehekülg — logo, kirjeldus, "Logi sisse" nupp |
| `LoginView.vue` | `/login` | Sisselogimine e-maili ja parooliga |
| `DashboardView.vue` | `/dashboard` | Töölaud — edasimüüjate arv ja viimane import |
| `SellersView.vue` | `/sellers` | Edasimüüjate nimekiri — otsing, vaata, muuda |
| `SellerView.vue` | `/seller/{sellerId}` | Edasimüüja detailvaade (ainult lugemiseks) |
| `SellerFormView.vue` | `/seller/form` | Põhiandmete lisamine ja muutmine — URL-is `?sellerId={sellerId}` muutmisrežiimis |
| `SellerSettingsView.vue` | `/seller/{sellerId}/settings` | Kontaktid, piirkonnad, teenustasud |
| `ReportsView.vue` | `/reports` | Aruanded ja Excel import |
| `InvoiceControlView.vue` | `/invoice-control` | Arvete kontroll |
| `SettingsView.vue` | `/settings` | Seadistused — kasutajad, KM määr, tootegrupid |

## Olulised äriloogika reeglid

- Edasimüüjaid ei kustutata — deaktiveeritakse/aktiveeritakse (`PUT /api/seller/{sellerId}/status`)
- Kasutajaid ei kustutata — deaktiveeritakse (`PUT /api/user/{userId}/status`)
- Kontakte saab kustutada
- Excel import on fikseeritud formaadiga — näidisfail: `docs/0.6_Issuer_sales_report.xlsx`
- Ühel perioodil saab olla ainult üks aruanne
- KM määr (22%) on seadistatav andmebaasist — Admin saab muuta ilma arendajata
- Parool on praegu lihttekstina (BCrypt ja Spring Security lisatakse hiljem)

## Andmemudel

<img src="docs/datamodel/edasimüüjatasuarvestus data modeler.png" alt="Andmemudel">

## Testandmed

Pärast `database/3_import.sql` käivitamist on andmebaasis:

| Kasutaja | Parool | Roll |
|----------|--------|------|
| mari@agent.ee | 123 | Admin |
| jt@agent.ee | 123 | User |

| Edasimüüja | org_id | Seis |
|------------|-----------------|------|
| ETAS AS | 10406134 | ACTIVE |
| Ühistu OÜ | 20506789 | ACTIVE |

## Dokumentatsioon

- `projekt-struktuur.md` — Kaustade struktuur visuaalselt (kood + tabel)
- `docs/specs/` — Projekti kirjeldus
- `docs/balsamiq/` — Balsamiq wireframe'id (PDF, BMPR)
- `docs/datamodel/` — Andmemudel (PDF, HTML, SQL)