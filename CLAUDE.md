# CLAUDE.md — Projekti kontekst Claude Code jaoks

> Kehtib mõlemale alaprojektile: `backend` ja `frontend`

## Claude Code tööpõhimõtted

- **CLAUDE.md on jagatud teadmusbaas** — siin projektis töötab mitu kaasautorit. Kui keegi avab projekti, peaks tema suhtlus Claude Code-iga olema sama sujuv ilma eelmisi vestlusi lugemata. Seetõttu: iga sisuline muudatus kajastub CLAUDE.md-s kohe — paku seda aktiivselt, ilma et kasutaja peaks meelde tuletama
- **Järjepidevus üle kõige** — enne uue koodi kirjutamist vaata, kuidas sarnane asi on juba lahendatud (bank40back/bank40front on referentsprojektid). Ühesugused tegevused → ühesugune mõttelaad ja struktuur
- **Kasuta bank40 lahendusi kus võimalik** — kui ETAS-is on sarnane ülesanne, kasuta sama lähenemist mis bank40-s. Kui ETAS vajab midagi lisaks (nt Status enum kolme kihiga, ConflictException), kasuta võimalikult lihtsat lahendust — ära mine liialt tehniliseks. Kõik uued kontseptsioonid vs bank40 tuleb tuua välja taski õppematerjalides (`docs/theory-materials`) esiletõstetult alguses
- **Kõik kolm CLAUDE.md-d käivad kaasas** — muudatus ühes tähendab, et vaatad üle ka teised kaks (`CLAUDE.md`, `backend/CLAUDE.md`, `frontend/CLAUDE.md`) ja uuendad kõik, mis vajab muutmist
- **README.md käib kaasas** — kui CLAUDE.md muutub, kontrolli kas README vajab sama muudatust
- **Arendusjärjekord ja seis** sektsiooni uuenda kohe kui samm on tehtud — mitte hiljem
- **Õppematerjal taski lõpus** — kui task on valmis, paku alati võimalust luua õppematerjal `docs/theory-materials` kausta (`skill-loo-uus-oppematerjal`)

---

## Mis projekt see on?
**ETAS** — Edasimüüja Teenustasu Arvutussüsteem.
Ettevõttesisene veebirakendus edasimüüjate lepinguandmete haldamiseks,
teenustasude arvutamiseks Exceli müügiaruande põhjal ning edasimüüjate
arvete õigsuse kontrollimiseks. Süsteem võimaldab juhtkonnale esitada
müügi- ja teenustasude ülevaateid.

---

## Struktuur
```
etas/
  backend/   — Spring Boot backend
  frontend/  — Vue.js frontend
  database/                 — SQL skriptid (schema, seed-andmed)
  docs/
    specs/                              — Projektikirjeldus (PDF + MD)
    balsamiq/                           — Balsamiq failid ja wireframe'id (PDF, PNG, BMPR)
    datamodel/                          — Andmemudel (PDF, PNG, SQL)
    instructions/                       — Õppejõu juhendmaterjalid (PDF)
    tasks/
      tasks.md                          — Taskide ülevaade ja seis
      task-01/, task-02/, task-03/      — Vana süsteem: feature-põhised taski kirjeldused
      backend/                          — Uus süsteem: üks fail per backend endpoint (nt PUT-api-seller-sellerId-status.md)
    dtos/
      schema/                           — DTO välja nimed ja tüübid (NimiDto_schema.json)
      examples/                         — Realistlikud näidisandmed (NimiDto_VaateNimi_example.json)
    theory-materials/                   — Õppematerjalid taskide kaupa
  projekt-struktuur.md      — Kaustade struktuur visuaalselt (kood + tabel)
```

---

## Tehnoloogiad

| Kiht | Tehnoloogia | Komponendid |
|------|------------|-------------|
| Frontend | Vue.js 3 | Pinia, Vue Router 5, Axios, Bootstrap 5, @phosphor-icons/vue |
| Backend | Spring Boot 4.0.6 | Spring Security, JPA, MapStruct 1.6, Lombok, Springdoc OpenAPI 3.x, p6spy, Apache POI |
| Andmebaas | PostgreSQL 17 | Lokaalne arendus, schema-põhine |
| Build | Gradle (back), Vite (front) | — |
| Versioonihaldus | Git + GitHub | Feature branch workflow |
| Dokumentatsioon | Balsamiq Cloud | Wireframe'id koos API labelitega |
| Andmemudel | Redgate Data Modeler | ER diagramm, SQL skriptide genereerimine |

- Java 21, backend port **8080**, frontend port **8081**
- DB schema: `etas`, port 5432, kasutaja: postgres

---

## Rollid ja õigused

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

---

## Vaadete struktuur (10 vaadet)

| # | Failinimi | URL | Kirjeldus |
|---|-----------|-----|-----------|
| 1 | `HomeView.vue` | `/` | Avalehekülg — logo, kirjeldus, "Logi sisse" nupp |
| 2 | `LoginView.vue` | `/login` | Sisselogimine e-maili ja parooliga |
| 3 | `DashboardView.vue` | `/dashboard` | Töölaud — edasimüüjate arv ja viimane import |
| 4 | `SellersView.vue` | `/sellers` | Edasimüüjate nimekiri — otsing, vaata, muuda |
| 5 | `SellerView.vue` | `/seller/{sellerId}` | Detailvaade — üldandmed, kontaktid, teenustasud, piirkonnad (ainult lugemiseks) |
| 6 | `SellerFormView.vue` | `/seller/form` | Põhiandmete lisamine ja muutmine — URL-is `?sellerId={sellerId}` muutmisrežiimis |
| 7 | `SellerSettingsView.vue` | `/seller/{sellerId}/settings` | Haldusvaade — kontaktid, piirkonnad, teenustasud; kolm eraldi modali |
| 8 | `ReportsView.vue` | `/reports` | Aruanded — Excel import + laiendatava reaga aruande ülevaade |
| 9 | `InvoiceControlView.vue` | `/invoice-control` | Arvete kontroll — arve sisestamine ja võrdlus arvutusega |
| 10 | `SettingsView.vue` | `/settings` | Seadistused — kasutajad, KM määr, kontaktide rollid |

**Modaalid (SellerSettingsView avab):**
- `SellerSettingsContactModal.vue` — lisa kontakt (`POST /api/seller/{sellerId}/contacts`)
- `SellerSettingsRegionModal.vue` — lisa piirkond (`POST /api/seller/{sellerId}/regions`)
- `SellerSettingsProductModal.vue` — lisa teenustasu (`POST /api/seller/{sellerId}/commission-rates`)

**Navigatsiooni struktuur:**
Menüü: `Töölaud | Edasimüüjad | Aruanded | Arvete kontroll | Seaded`
Paremal: Kasutajanimi | `Logi välja`

**Navigatsiooni lingid:**
- HomeView "Logi sisse" → LoginView
- LoginView → DashboardView (peale edukat sisselogimist)
- Logi välja → LoginView
- "Lisa uus edasimüüja" → SellerFormView (lisa režiim)
- "Vaata" nupp → SellerView (read-only)
- "Muuda" nupp → SellerFormView (muuda režiim)
- "Seaded" nupp → SellerSettingsView
- Tagasi nimekirja → SellersView

---

## API endpoint-id

| Meetod | Endpoint | DTO | Kirjeldus |
|--------|----------|-----|-----------|
| POST | `/api/login` | `LoginDto` → `LoginResponseDto` (userId, firstName, middleName, lastName, role) | Sisselogimine |
| GET | `/api/dashboard` | `DashboardResponseDto` | Dashboard statistika |
| GET | `/api/seller/user/{userId}` | `SellerResponseDto[]` | Kõik edasimüüjad |
| GET | `/api/seller/{sellerId}` | `SellerDetailResponseDto` | Edasimüüja üldandmed |
| GET | `/api/seller/{sellerId}/contacts` | `SellerContactResponseDto[]` | Edasimüüja kontaktid |
| GET | `/api/seller/{sellerId}/regions` | `SellerRegionResponseDto[]` | Edasimüüja piirkonnad |
| GET | `/api/seller/{sellerId}/commission-rates` | `CommissionRateResponseDto[]` | Edasimüüja teenustasud |
| POST | `/api/seller/user/{userId}` | `SellerDto` | Lisa uus edasimüüja (`created_by` + rolli kontroll) |
| PUT | `/api/seller/{sellerId}` | `SellerDto` | Muuda edasimüüja andmeid |
| PUT | `/api/seller/{sellerId}/status` | `SellerStatusDto` | Aktiveeri või deaktiveeri edasimüüja (rolli kontroll) |
| POST | `/api/seller/{sellerId}/contacts` | `SellerContactDto` | Lisa kontakt |
| DELETE | `/api/seller/{sellerId}/contacts/{contactId}` | — | Kustuta kontakt |
| POST | `/api/seller/{sellerId}/regions` | `SellerRegionDto` | Lisa piirkond |
| PUT | `/api/seller/{sellerId}/regions/{regionId}` | `SellerRegionDto` | Uuenda piirkonna müügipunktide arvu |
| DELETE | `/api/seller/{sellerId}/regions/{regionId}` | — | Kustuta piirkond |
| POST | `/api/seller/{sellerId}/commission-rates` | `CommissionRateDto` | Lisa teenustasu |
| PUT | `/api/seller/{sellerId}/commission-rates/{commissionRateId}` | `CommissionRateDto` | Muuda teenustasu |
| DELETE | `/api/seller/{sellerId}/commission-rates/{commissionRateId}` | — | Kustuta teenustasu |
| POST | `/api/import/user/{userId}` | `multipart/form-data` | Laadi üles Excel müügiaruanne |
| DELETE | `/api/import/user/{userId}/{period}` | — | Kustuta impordi andmed perioodist |
| GET | `/api/report/user/{userId}` | `ReportResponseDto[]` | Aruannete nimekiri |
| GET | `/api/report/{sellerId}/{period}` | `ReportDetailResponseDto[]` | Aruande detailid |
| GET | `/api/report/{sellerId}/{period}/export` | `.xlsx` fail | Ekspordi aruanne Excelisse |
| GET | `/api/invoice/user/{userId}` | `InvoiceResponseDto[]` | Arvete nimekiri |
| POST | `/api/invoice/user/{userId}` | `InvoiceDto` | Sisesta arve |
| DELETE | `/api/invoice/number/{invoiceNumber}` | — | Kustuta arve |
| GET | `/api/settings/vat` | `VatSettingResponseDto` | KM määr |
| PUT | `/api/settings/vat/user/{userId}` | `VatSettingDto` | Uuenda KM määra |
| GET | `/api/user` | `UserResponseDto[]` | Kasutajate nimekiri |
| POST | `/api/user` | `UserDto` | Lisa uus kasutaja |
| PUT | `/api/user/{userId}` | `UserDto` | Muuda kasutajat |
| PUT | `/api/user/{userId}/status` | `UserStatusDto` | Deaktiveeri kasutaja |
| GET | `/api/region` | `RegionResponseDto[]` | Kõik piirkonnad (region tabelist) |
| GET | `/api/product-type` | `ProductTypeResponseDto[]` | Tootegruppide nimekiri |
| POST | `/api/product-type` | `ProductTypeDto` | Lisa uus tootegrupp |
| DELETE | `/api/product-type/{productTypeId}` | — | Kustuta tootegrupp |

---

## Andmemudel (tabelid)

**seller** — id, company_name, org_id (unikaalne), status, contract_start, contract_end (null=tähtajatu), notes, created_at, created_by (FK → app_user)

**seller_contact** — id, seller_id (FK), first_name, middle_name, last_name, phone, email

**seller_role** (vahel tabel) — id, seller_contact_id (FK), seller_role_id (FK) → Java: `SellerContactRole`

**role** — id, code (L/A/R/T), seller_role_name → Java: `ContactRole`

**region** — id, region_name, sequence_number

**seller_region** — id, seller_id (FK), region_id (FK → region), sales_point_count (default 0)

**product_type** — id, product_type_name

**commission_rate** — id, seller_id (FK), product_type_id (FK), fee_per_transaction, fee_percent, includes_vat, valid_from, valid_to (null=kehtib lõputult)

**sales_report** — id, created_by (FK → app_user), period, created_at — ühe impordi sessioon

**sales_report_detail** — id, sales_report_id (FK → sales_report), seller_id (FK), department_name, department_id, payment_channel, product_type (varchar — salvestatakse Exceli string otse), transaction_count, sales_amount, fee, period, region, created_at (timestamp)

**commission_calculation** — id, sales_report_id (FK → sales_report), commission_rate_id (FK), calculated_fee, vat_amount, calculated_fee_plus_vat, calculation_date

**invoice** — id, seller_id (FK), period, number, amount, issued_on, calculated_fee, notes — UNIQUE (seller_id, period)

**app_user** — id, first_name, middle_name, last_name, email (login, unikaalne), password (BCrypt), user_role char(1), user_status

**vat_setting** — id, vat_rate, updated_at, updated_by, valid_from_date, valid_to_date (null = kehtib lõputult), status — toetab mitut kirjet erinevate perioodidega; õige rida leitakse impordi perioodi järgi

---

## Enum-id

**product_type algandmed** (seed — lisatakse `database/` SQL skriptis):
`isikustamine`, `kaardimyyk`, `pilet`, `rahalaadimine`, `sooduskaardi isikustamine`, `kaardi tagasiost`, `raha valjamakse`

**region algandmed** (seed — kõik Eesti maakonnad + suuremad linnad, järjestatud `sequence_number` järgi)

**Kontaktide rollid** (fikseeritud, `role` tabel):
- `L` — Lepinguline kontakt
- `A` — Aruannete kontakt
- `R` — Raamatupidamise kontakt
- `T` — Tehniline kontakt

**Staatuse kolm kihti** (seller ja app_user):

| Kiht | Aktiivne | Mitteaktiivne |
|------|----------|---------------|
| DB (`status` veerg) | `"A"` | `"D"` |
| API JSON | `"ACTIVE"` | `"INACTIVE"` |
| UI kuvamine | `"Aktiivne"` | `"Peatatud"` |

Backend mapper teisendab DB koodi (`"A"`/`"D"`) ↔ API väärtuseks (`"ACTIVE"`/`"INACTIVE"`). Frontend kuvab eesti keeles.

---

## Teenustasu arvutuse loogika

```
1. Teenustasu koguselt:
   teenustasu = tehingute_arv × tasu_ühe_tehingu_kohta
   Näide: 100 tehingut × 0.01 EUR = 1.00 EUR

2. Teenustasu summalt:
   teenustasu = müügisumma × teenustasu_protsent
   Näide: 2500 EUR × 1% = 25.00 EUR

3. KM kontroll:
   KUI includes_vat = true  → lõplik_summa = arvutatud_teenustasu
   KUI includes_vat = false → lõplik_summa = arvutatud_teenustasu × (1 + vat_rate/100)
   KM määr võetakse alati andmebaasist vat_setting tabelist
```

**Excel impordi voog:**
1. Kasutaja valib periood + laadib Exceli faili
2. Backend loeb read sisse (Apache POI)
3. Kontrollib: edasimüüja olemas? tootegrupp olemas?
4. Leiab teenustasu määra andmebaasist
5. Arvutab teenustasu
6. Lisab KM vajadusel (võtab määra vat_setting tabelist)
7. Kuvab tulemused tabelis
8. Kasutaja kinnitab ja salvestab

**Excel faili formaat:** `docs/0.6_Issuer_sales_report.xlsx` (näidisfail)
**Reegel:** ühel perioodil saab olla ainult üks aruanne

| Veerg (Excel) | Kasutatakse | Vastendus DB / enum |
|---|---|---|
| `issuer_name` | ei | — (alati "Ridango AS") |
| `seller_name` | ei | — (edasimüüja leitakse org_id järgi) |
| `seller_org_id` | **jah** | `seller.org_id` — edasimüüja tuvastus |
| `dept_name` | **jah** | `sales_report_row.department_name` |
| `seller_dept_id` | **jah** | `sales_report_row.department_id` |
| `payment_channel` | **jah** | `sales_report_row.payment_channel` ("C" / "E") |
| `tyyp` | **jah** | `sales_report.product_type` (varchar) — salvestatakse string otse; arvutamisel otsitakse `product_type.product_type_name` järgi |
| `tehinguid` | **jah** | `sales_report_row.transaction_count` |
| `summas` | **jah** | `sales_report_row.sales_amount` |
| `price_add_sum` | ei | — |
| `fee_sum` | ei | — |
| `a_date` | **jah** | periood — Excel formaat `"2026-4"` (aasta-kuu), backend teisendab → `"04.2026"` (kk.aaaa) |
| `buyer_channel` | ei | — |
| `piirkond` | **jah** | `sales_report_row.region` |
| `liiniomanik` | ei | — |

**`tyyp` → product_type vastendus (backendis):**

Backend otsib `product_type` tabelist rea, kus `name` kattub Exceli `tyyp` väärtusega (väiketähelised, võivad sisaldada tühikuid). Kui vastet ei leita → viga.

| Exceli `tyyp` | product_type.name |
|---|---|
| `isikustamine` | `isikustamine` |
| `kaardimyyk` | `kaardimyyk` |
| `pilet` | `pilet` |
| `rahalaadimine` | `rahalaadimine` |
| `sooduskaardi isikustamine` | `sooduskaardi isikustamine` |
| `kaardi tagasiost` | `kaardi tagasiost` |
| `raha valjamakse` | `raha valjamakse` |

---

## Olulised arhitektuuri otsustused

- **Login emailiga** — email on unikaalne, kasutaja mäletab seda alati
- **Edasimüüjat ei kustutata, ainult deaktiveeritakse/aktiveeritakse** — `PUT /api/seller/{sellerId}/status` koos `SellerStatusDto {"status": "INACTIVE"/"ACTIVE"}`. Ajaloolised andmed säilivad.
- **Kasutajat ei kustutata, ainult deaktiveeritakse** — `PUT /api/user/{userId}/status` koos `UserStatusDto {"status": "INACTIVE"}`. Sama muster nagu edasimüüja puhul.
- **Kontakte saab kustutada** — need ei ole ajalooliselt kriitilised
- **SellerView on ainult lugemiseks** — muutmiseks eraldi SellerSettingsView (selge lahusus, turvaline)
- **Edasimüüja lisamine kahes sammus** — SellerFormView lisab põhiandmed, seejärel SellerSettingsView haldab kontakte/piirkondi/teenustasusid modaalidega
- **Modaalid, mitte inline** — SellerSettingsView avab kolm eraldi modali (kontakt, piirkond, teenustasu)
- **Inline vormid, mitte modaalid** — kontaktide ja kasutajate lisamine inline, aruande detailid laiendatava reana
- **KM määr seadistatav andmebaasist** — Admin saab muuta ilma arendajata; salvestatakse koos muutja ja kuupäevaga
- **Parool lihttekstina (task-01)** — BCrypt ja Spring Security lisatakse hiljem; praegu lihtne email+password+status päring
- **Autentimine: lihtne (task-01)** — Spring Security puudub, sessioon puudub; frontend salvestab vastuse localStorage-i

---

## Arenduse järjekord

Detailne taskide plaan: `docs/tasks/tasks.md`

**Taskid ja seis:**

| Task | Sisu | Seis |
|------|------|------|
| task-01 | Login ja autentimine | ✅ Valmis |
| task-02 | Dashboard ja navbar | ✅ Valmis |
| task-03 | Edasimüüjad (nimekiri, detailvaade, lisamine, muutmine, deaktiveerimine) | 🔄 Backend valmis, frontend pooleli |
| task-04 | Edasimüüja kontaktid (POST/DELETE + SellerSettingsView raamistik + ContactModal) | ⬜ |
| task-05 | Edasimüüja piirkonnad (GET /api/region + seller_region CRUD + RegionModal) | ⬜ |
| task-06 | Teenustasud (GET /api/product-type + commission_rate CRUD + CommissionModal) | ⬜ |
| task-07 | Excel import (Apache POI + POST/DELETE /api/import + ReportsView upload) | ⬜ |
| task-08 | Teenustasu arvutus + aruanded (calculation + GET /api/report + ReportsView tabel) | ⬜ |
| task-09 | Aruannete eksport (GET /api/report/export + ekspordi nupp) | ⬜ |
| task-10 | Arvete kontroll — **sõltumatu** (invoice CRUD + InvoiceControlView) | ⬜ |
| task-11 | Seadistused (product_type POST/DELETE + vat_setting + user CRUD + SettingsView) | ⬜ |

---

## MVP vs Nice to have

**MVP — kindlasti valmis:**
Sisselogimine, edasimüüjate CRUD, kontaktide haldus, teenustasude määrad, piirkonnad, Excel import, teenustasu arvutus, KM, aruannete ülevaade, arvete kontroll, kasutajate haldus, kaks rolli.

**Nice to have — tulevikus:**
Unustasin parooli, rollide tooltip, is_primary kontakt, PDF eksport, email saatmine, perioodipõhine statistika, graafikud, automaatne teavitus, Region/ContactRole enum refaktoreerimine, Exceli vormi dünaamiline seadistamine, piirkondlik teenustasu erisus (commission_rate tabelisse nullable region veerg — lahendus on selge, aga algtaseme projekti ajaraami ei mahu).

---

## Kodeerimistavad

Kodeerimistavad on eraldi alamfailides:
- **Backend:** `backend/CLAUDE.md`
- **Frontend:** `frontend/CLAUDE.md`

---

## Arendusjärjekord ja seis

### Tehtud
- [x] Projekti struktuur (monorepo — back + front + database + docs)
- [x] `database/` SQL skriptid (reset, create, import koos testandmetega)
- [x] `docs/` struktuur (balsamiq, datamodel, instructions)
- [x] Backend seadistus (`application.properties`, `build.gradle`, springdoc 3.0.3)
- [x] Frontend seadistus (`vite.config.js` port 8081, `main.js` Axios + Bootstrap, `assets/main.css`, `assets/style.css`)
- [x] Infrastructure error handling (`RestExceptionHandler`, `ApiError`, `ErrorResponse` enum ETAS veakoodidega, 4 exception klassi sh `ConflictException`)
- [x] `Status.java` enum (`ACTIVE`, `SOFT_DELETED`) — DB-sse salvestatakse `"A"` / `"D"`
- [x] p6spy seadistus (`build.gradle`, `application.properties`, `spy.properties`)

- [x] task-01: Login ja autentimine — `POST /api/login`, `HomeView.vue`, `LoginView.vue`, `AuthService.js`, `NavigationService.js`, `LoginService.js`, `router/index.js`
- [x] task-02: Dashboard ja navbar — `GET /api/dashboard`, `DashboardView.vue`, `DashboardService.js`, `App.vue` navbar, `NavigationService.js` täiendused, `router/index.js` placeholder route'id
- [x] task-03 backend: Seller CRUD — `SellerController`, `SellerService`, `SellerRepository`, `SellerMapper`, `SellerDto`, `SellerStatusDto`, `SellerDetailResponseDto`; `SellerRegionRepository`, `SellerRegionMapper`, `SellerRegionResponseDto`; `CommissionRateRepository`, `CommissionRateMapper`, `CommissionRateResponseDto`; `SellerContactRepository`, `SellerContactRoleRepository`, `SellerContactMapper`, `SellerContactResponseDto`; `Status.java` täiendused (`apiValue`, `toApiValue`, `fromApiValue`); `ErrorResponse` täiendused (`ACCESS_DENIED`, `SELLER_ORG_ID_ALREADY_EXISTS`, `SELLER_ALREADY_ACTIVE`)

### Järgmine
- [ ] task-03 frontend: `SellersView.vue`, `SellerView.vue`, `SellerFormView.vue`, `NavigationService.js` täiendused, `router/index.js` täiendused
- [ ] task-04: kontaktid — `POST /api/seller/{sellerId}/contacts`, `DELETE .../contacts/{contactId}`, `SellerSettingsView.vue` raamistik, `SellerSettingsContactModal.vue`
- [ ] task-05: piirkonnad — `GET /api/region`, seller_region POST/PUT/DELETE, `SellerSettingsRegionModal.vue`
- [ ] task-06: teenustasud — `GET /api/product-type`, commission_rate POST/PUT/DELETE, `SellerSettingsProductModal.vue`
- [ ] task-07: Excel import — Apache POI, `POST /api/import/user/{userId}`, `DELETE /api/import/user/{userId}/{period}`, `ReportsView.vue` upload
- [ ] task-08: arvutus + aruanded — commission_calculation loogika, `GET /api/report/user/{userId}`, `GET /api/report/{sellerId}/{period}`, `ReportsView.vue` tabel
- [ ] task-09: eksport — `GET /api/report/{sellerId}/{period}/export`, ekspordi nupp
- [ ] task-10: arvete kontroll — invoice CRUD, `InvoiceControlView.vue` *(sõltumatu, paralleelne)*
- [ ] task-11: seadistused — product_type POST/DELETE, vat_setting CRUD, user CRUD, `SettingsView.vue`

---

## Üldised keerukuse reeglid

1. **Esmalt lihtne** — for-each > stream, eksplitsiitne > lühike
2. **Meetodid väikesed** — avalik meetod 3–7 rida, delegeerib privaatsetele
3. **Ärge copy-pastege koodi**
4. **Kommentaarid** ainult kui "miks" pole koodist arusaadav — vaikimisi ei kirjuta
5. **Feature branch workflow** — iga teenus eraldi branch-il