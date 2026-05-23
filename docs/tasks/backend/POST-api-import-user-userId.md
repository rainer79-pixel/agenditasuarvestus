# POST /api/import/user/{userId}

**Kontroller:** `ReportController.java`
**Tüüp:** Backend
**Staatus:** To Do

## Kontekst

`ReportsView` võimaldab kasutajal laadida üles Exceli müügiaruande. Kasutaja valib faili ja vajutab "Laadi üles". Backend loeb faili Apache POI-ga, valideerib päised, leiab edasimüüjad `seller_org_id` järgi, salvestab read `sales_report_detail` tabelisse ja arvutab teenustasud `commission_calculation` tabelisse.

## Õppejõud on juba implementeerinud

> **Ära tee neid uuesti — ainult täienda**

- `ReportController.java` — `POST /api/import/user/{userId}` endpoint olemas, võtab vastu `file` parameetri
- `ReportControllerService.java` — täielik Apache POI impordi loogika: päiste kaardistamine, ridade lugemine, edasimüüjate leidmine, `sales_report` + `sales_report_detail` + `commission_calculation` salvestamine
- `ReportService.js` (frontend) — `sendPostImportReport(userId, file)` meetod olemas
- `DashboardView.vue` (frontend) — import UI on ajutiselt dashboardil; **frontend taskis viiakse see üle ReportsView-sse**

**Olemasolev kood vajab täiendamist:** puuduvad rolli kontroll, perioodi duplikaadi kontroll, korrektne 400 veakäsitlus, `@ResponseStatus(201)` ja Swagger annotatsioonid.

## Mocki vaade

![ReportsView mock](../../balsamiq/views/ReportsView.vue.png)

## API leping

| Väli | Väärtus |
|------|---------|
| Meetod | `POST` |
| Tee | `/api/import/user/{userId}` |
| Auth | Ei (rollipõhine kontroll teenuse kihis) |
| Content-Type | `multipart/form-data` |

### Request — form parameetrid (ei ole JSON body)

| Parameeter | Tüüp | Kirjeldus |
|-----------|------|-----------|
| `file` | `MultipartFile` | `.xlsx` fail — `@RequestParam("file")` |

### Response Body

Puudub — HTTP 201 tühi vastus

## Veahaldus

| Olukord | Exception klass | ErrorResponse enum | HTTP staatus |
|---------|----------------|-------------------|--------------|
| Vale failivorming / vigased päised | `BadRequestException` *(uus)* | `IMPORT_INVALID_HEADER` | 400 |
| Kasutajal pole Admin rolli | `ForbiddenException` | `ACCESS_DENIED` | 403 |
| Tundmatu edasimüüja (seller_org_id ei leitud) | `DataNotFoundException` | `IMPORT_SELLER_NOT_FOUND` | 404 — vt märkus allpool |
| Sellel perioodil on aruanne juba olemas | `ConflictException` | `IMPORT_PERIOD_ALREADY_EXISTS` | 409 |

> **Märkus veahalduse kohta:**
> - `BadRequestException` **puudub** — loo uus klass `exception/` paketti (järgi `ForbiddenException` mustrit) ja registreeri `RestExceptionHandler`-is (`HttpStatus.BAD_REQUEST`)
> - Praegune kood viskab `ForbiddenException` (→ 403) vigaste päiste korral — see on **vale**, tuleb asendada `BadRequestException`-iga (→ 400)
> - `IMPORT_INVALID_HEADER` (514), `IMPORT_SELLER_NOT_FOUND` (511), `IMPORT_PERIOD_ALREADY_EXISTS` (513) on juba `ErrorResponse.java`-s olemas
> - **Tundmatu seller_org_id käitumine:** praegune kood teeb `continue` (jätab rea lihtsalt vahele). Mock spetsifikatsioon ütleb 404. **Arutada õppejõuga** kumb käitumine on eelistatud enne implementeerimist.

## Periood formaadist

Periood loetakse Exceli `a_date` veerust — formaat `"2026-4"` (YYYY-M). See salvestatakse `sales_report.period` veergu kujul `"2026-4"`. `commission_calculation_view` kasutab täpselt seda formaati (`SPLIT_PART(sr.period, '-', ...)`).

Praegune kood `rows.get(0).getPeriod()` on **õige** — perioodi ei ole vaja `@RequestParam`-ina vastu võtta.

## Andmebaas

Seotud tabelid: `app_user`, `sales_report`, `sales_report_detail`, `commission_calculation`

Ühel impordi sessioonil salvestatakse:
1. Üks rida `sales_report` (`created_by`, `period`, `created_at`)
2. N rida `sales_report_detail` — üks Exceli rida = üks DB rida; tundmatu `seller_org_id`-ga read → 404
3. N rida `commission_calculation` — arvutused `commission_calculation_view` põhjal

Perioodi duplikaadi kontroll: `sales_report` tabelis pole `UNIQUE` constrainti — kontroll tehakse teenuse kihis (`existsByPeriod`).

## Muudetavad failid

- `ReportController.java` — lisa `@ResponseStatus(HttpStatus.CREATED)`, Swagger annotatsioonid
- `ReportControllerService.java` — lisa rolli kontroll, perioodi duplikaadi kontroll, paranda header validatsioon (`BadRequestException`)
- `SalesReportRepository.java` — lisa `existsByPeriod(String period)`
- `BadRequestException.java` *(uus)* — loo `exception/` paketti
- `RestExceptionHandler.java` — registreeri `BadRequestException` → `HttpStatus.BAD_REQUEST`

## Vastuvõtu kriteeriumid

- [ ] `POST /api/import/user/{userId}` tagastab 201 Created õnnestumise korral
- [ ] Periood loetakse Exceli `a_date` veerust formaadis `"2026-4"` ja salvestatakse `sales_report.period` veergu
- [ ] Vale failivorming / vigased päised: tagastab 400 koos `IMPORT_INVALID_HEADER` veaga
- [ ] Kasutajal pole Admin rolli: tagastab 403 koos `ACCESS_DENIED` veaga
- [ ] Tundmatu `seller_org_id`: tagastab 404 koos `IMPORT_SELLER_NOT_FOUND` veaga
- [ ] Periood juba olemas: tagastab 409 koos `IMPORT_PERIOD_ALREADY_EXISTS` veaga
- [ ] `BadRequestException` on loodud ja registreeritud `RestExceptionHandler`-is
- [ ] Kontrolleri meetodil on `@Operation` ja `@ApiResponses` annotatsioonid
- [ ] Swagger UI kaudu on endpoint nähtav ja testitav