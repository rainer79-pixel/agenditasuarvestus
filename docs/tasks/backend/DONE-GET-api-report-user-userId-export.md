# GET /api/report/user/{userId}/export

**Kontroller:** `ReportController.java`
**Tüüp:** Backend
**Staatus:** To Do

## Kontekst

`ReportsView` aruande ülevaate tabeli all on üks roheline "Ekspordi XLS" nupp — see ekspordib **kogu hetkel kuvatud tabeli** (kõik edasimüüjad, mitte ühe edasimüüja). Filtrid (`periodFrom`, `periodTo`, `sellerId`) on samad mis `GET /api/report/user/{userId}` nimekirja endpointil. Backend genereerib Apache POI-ga `.xlsx` faili ja tagastab selle binaarfailina.

> **Märkus:** Varasem taskifail `GET-api-report-sellerId-period-export.md` dokumenteeris vale endpointi (per edasimüüja). See fail on õige versioon — kasuta seda.

## Mocki vaade

![ReportsView mock](../../balsamiq/views/ReportsView.vue.png)

## API leping

| Väli | Väärtus |
|------|---------|
| Meetod | `GET` |
| Tee | `/api/report/user/{userId}/export` |
| Auth | Ei (mõlemad rollid saavad eksportida) |

### Query parameetrid (kõik valikulised — samad mis nimekirja endpointil)

| Parameeter | Tüüp | Kirjeldus |
|-----------|------|-----------|
| `periodFrom` | `String` | Perioodi algus formaadis `YYYY-M` (nt `2026-1`) — `@RequestParam(required = false)` |
| `periodTo` | `String` | Perioodi lõpp formaadis `YYYY-M` (nt `2026-4`) — `@RequestParam(required = false)` |
| `sellerId` | `Integer` | Edasimüüja filter — `@RequestParam(required = false)` |

### Request Body

Puudub — GET päring

### Response Body

Puudub — HTTP 200 koos `.xlsx` binaarfailiga

| HTTP päis | Väärtus |
|-----------|---------|
| `Content-Type` | `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet` |
| `Content-Disposition` | `attachment; filename="aruanne.xlsx"` |

Kontrolleri meetodi tagastustüüp: `ResponseEntity<byte[]>`

## Veahaldus

| Olukord | Exception klass | ErrorResponse enum | HTTP staatus |
|---------|----------------|-------------------|--------------|
| Eksporditavaid andmeid ei leitud | `DataNotFoundException` | `REPORT_NOT_FOUND` | 404 |

> `REPORT_NOT_FOUND(516)` lisatakse `ErrorResponse`-i `GET /api/report/user/{userId}` taskis — kontrolli, et on juba olemas.

## Andmebaas

Seotud tabelid: samad mis `GET /api/report/user/{userId}` — `sales_report`, `sales_report_detail`, `commission_calculation`, `commission_rate`, `product_type`, `seller`

> **Soovitus:** Kasuta `GET /api/report/user/{userId}` jaoks kirjutatud teenusemeetodit uuesti — päri `List<ReportResponseDto>` ja teisenda see Apache POI-ga Exceliks. Nii ei duplikeerita andmepäringuloogikat.

## Exceli fail — veergude struktuur

| Veerg | Sisu |
|-------|------|
| A | Edasimüüja |
| B | Tehinguid |
| C | Müügisumma (EUR) |
| D | Teenustasu (EUR) |
| E | KM (EUR) |
| F | Kokku (EUR) |

Viimane rida: KOKKU — kõigi edasimüüjate summad. Apache POI on juba `build.gradle`-s olemas (kasutati impordi jaoks).

## Muudetavad failid

- `ReportController.java` — lisa `@GetMapping("/report/user/{userId}/export")`, tagastustüüp `ResponseEntity<byte[]>`, Swagger annotatsioonid
- `ReportControllerService.java` — uus meetod `exportReports(Integer userId, String periodFrom, String periodTo, Integer sellerId)`, kasutab `XSSFWorkbook`-i

## Vastuvõtu kriteeriumid

- [ ] `GET /api/report/user/{userId}/export` tagastab 200 koos `.xlsx` failiga
- [ ] `Content-Disposition: attachment; filename="aruanne.xlsx"` päis on olemas
- [ ] Filtrid `periodFrom`, `periodTo`, `sellerId` töötavad (samad mis nimekirja endpointil)
- [ ] Exceli fail sisaldab päiserea + edasimüüjate read + KOKKU rida
- [ ] Kui andmeid ei leidu: tagastab 404 koos `REPORT_NOT_FOUND` veaga
- [ ] Kontrolleri meetodil on `@Operation` ja `@ApiResponses` annotatsioonid
- [ ] Swagger UI kaudu on endpoint nähtav ja faili allalaadimine testitav