# GET /api/report/user/{userId}

**Kontroller:** `ReportController.java`
**Tüüp:** Backend
**Staatus:** To Do

## Kontekst

`ReportsView` kuvab "Aruande ülevaade" tabelit — kõik edasimüüjad ja nende perioodi summad (tehingud, müügisumma, teenustasud, KM, kokku). Kasutaja saab filtreerida perioodi vahemiku ja edasimüüja järgi. Vaikimisi laaditakse eelmise kuu andmed; lisaks on "Eelmine kuu" ja "Eelmine kvartal" kiirvaliku nupud. KOKKU rea arvutab frontend ise vastuste massiivist. Seotud endpointid: `POST /api/import/user/{userId}` (andmete allikas), `GET /api/report/{sellerId}/{period}` (detailvaade).

## Mocki vaade

![ReportsView mock](../../balsamiq/views/ReportsView.vue.png)

## API leping

| Väli | Väärtus |
|------|---------|
| Meetod | `GET` |
| Tee | `/api/report/user/{userId}` |
| Auth | Ei (mõlemad rollid näevad aruandeid) |
| Content-Type | — |

### Query parameetrid (kõik valikulised)

| Parameeter | Tüüp | Kirjeldus |
|-----------|------|-----------|
| `periodFrom` | `String` | Perioodi algus formaadis `YYYY-M` (nt `2026-1`) — `@RequestParam(required = false)` |
| `periodTo` | `String` | Perioodi lõpp formaadis `YYYY-M` (nt `2026-4`) — `@RequestParam(required = false)` |
| `sellerId` | `Integer` | Edasimüüja filter — `@RequestParam(required = false)` |

### Request Body

Puudub — GET päring

### Response Body — `ReportResponseDto.java`

> Schema: [`ReportResponseDto_schema.json`](../../dtos/schema/ReportResponseDto_schema.json)
> Näidis: [`ReportResponseDto_ReportsView_Array_example.json`](../../dtos/examples/ReportResponseDto_ReportsView_Array_example.json)

| Väli | Tüüp | Allikas |
|------|------|---------|
| `sellerId` | `Integer` | `seller.id` |
| `companyName` | `String` | `seller.company_name` |
| `period` | `String` | `sales_report.period` |
| `transactionCount` | `Integer` | `SUM(sales_report_detail.transaction_count)` grupeerimisel seller + period |
| `salesAmount` | `BigDecimal` | `SUM(sales_report_detail.sales_amount)` grupeerimisel seller + period |
| `feeAmount` | `BigDecimal` | `SUM(commission_calculation.calculated_fee)` grupeerimisel seller + period |
| `vatAmount` | `BigDecimal` | `SUM(commission_calculation.vat_amount)` grupeerimisel seller + period |
| `totalFee` | `BigDecimal` | `SUM(commission_calculation.calculated_fee_plus_vat)` grupeerimisel seller + period |

## Veahaldus

| Olukord | Exception klass | ErrorResponse enum | HTTP staatus |
|---------|----------------|-------------------|--------------|
| Aruandeid ei leitud | `DataNotFoundException` | `REPORT_NOT_FOUND` | 404 |

> **Märkus veahalduse kohta:**
> Veahalduse infrastruktuur asub: `backend/src/main/java/ee/valiit/etas/infrastructure/`
> Kontrolli, kas vajalikud `ErrorResponse` enum kirjed ja exception klassid juba eksisteerivad:
> - `backend/src/main/java/ee/valiit/etas/infrastructure/error/ErrorResponse.java`
> - `backend/src/main/java/ee/valiit/etas/infrastructure/exception/`
>
> **`REPORT_NOT_FOUND` puudub** — lisa `ErrorResponse`-i (Excel ploki lõppu):
> `REPORT_NOT_FOUND("Aruannet ei leitud", 516)`

## Andmebaas

Seotud tabelid: `sales_report`, `sales_report_detail`, `commission_calculation`, `seller`

Vastus on **agregeeritud** — üks DTO rida = üks edasimüüja ühel perioodil. Päring peab:
1. Liitma `sales_report` + `seller` + `sales_report_detail` + `commission_calculation`
2. Grupeerima `seller.id` ja `sales_report.period` järgi
3. Sumeerima tehingud, müügisummad, teenustasud, KM

Filtrid (kõik valikulised):
- `periodFrom` / `periodTo` — võrdlus `sales_report.period` veeru vastu (string, formaadis `YYYY-M`, nt `2026-1`)
- `sellerId` — `sales_report_detail.seller_id = :sellerId`

> **Soovitus:** Kaaluda eraldi SQL VIEW loomist (sarnaselt `commission_calculation_view`-le), mis koondab agregatsioonipäringu ühte kohta. Alternatiivina saab kasutada JPQL `select new ... ` projektsiooni.

Perioodivõrdlus `YYYY-M` stringidena: `"2026-1" < "2026-4"` — stringivõrdlus toimib aasta tasandil, kuid kuu tasandil `"2026-9" > "2026-10"` (string järjestus). Turvaline lahendus: teisendada periood `YYYY-MM` formaati võrdluse ajaks või kasutada `MAKE_DATE` kuupäevaks.

## Muudetavad failid

- `ReportController.java` — lisa `@GetMapping`, query parameetrid, Swagger annotatsioonid
- `ReportControllerService.java` — uus meetod aruannete nimekirja pärimiseks filtritega
- Uus repository meetod või SQL VIEW — agregeeritud andmete pärimiseks
- `ReportResponseDto.java` *(uus)* — loo `controller/report/dto/` paketti
- `ErrorResponse.java` — lisa `REPORT_NOT_FOUND(516)`

## Vastuvõtu kriteeriumid

- [ ] `GET /api/report/user/{userId}` tagastab 200 ja aruannete nimekirja
- [ ] Filtreerimine `periodFrom` + `periodTo` järgi töötab
- [ ] Filtreerimine `sellerId` järgi töötab
- [ ] Kui filtrile vastavaid andmeid pole: tagastab 404 koos `REPORT_NOT_FOUND` veaga
- [ ] `REPORT_NOT_FOUND` on lisatud `ErrorResponse`-i koodiga 516
- [ ] `ReportResponseDto` klass on loodud Java klassina õigesse paketti
- [ ] Vastuse väljad vastavad skeemile: `sellerId`, `companyName`, `period`, `transactionCount`, `salesAmount`, `feeAmount`, `vatAmount`, `totalFee`
- [ ] Kontrolleri meetodil on `@Operation` ja `@ApiResponses` annotatsioonid
- [ ] Swagger UI kaudu on endpoint nähtav ja testitav