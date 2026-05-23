# DELETE /api/import/user/{userId}/{period}

**Kontroller:** `ReportController.java`
**Tüüp:** Backend
**Staatus:** To Do

## Kontekst

`ReportsView` võimaldab kasutajal kustutada juba imporditud perioodi andmeid. Kasutaja valib kuu ja aasta ning vajutab "Kustuta" — frontend kuvab enne päringu saatmist kinnitusdialoogi. Backend kustutab perioodi `sales_report`, kõik seotud `sales_report_detail` read ja `commission_calculation` kirjed. Seotud endpoint: `POST /api/import/user/{userId}` loob need andmed. Periood on URL-is formaadis `"2026-4"` (YYYY-M) — täpselt nii nagu see on salvestatud `sales_report.period` veergu.

## Mocki vaade

![ReportsView mock](../../balsamiq/views/ReportsView.vue.png)

## API leping

| Väli | Väärtus |
|------|---------|
| Meetod | `DELETE` |
| Tee | `/api/import/user/{userId}/{period}` |
| Auth | Ei (rollipõhine kontroll teenuse kihis) |

### Request Body

Puudub — path parameetrid `{userId}` ja `{period}`

### Response Body

Puudub — HTTP 200 tühi vastus

## Veahaldus

| Olukord | Exception klass | ErrorResponse enum | HTTP staatus |
|---------|----------------|-------------------|--------------|
| Kasutajal pole Admin rolli | `ForbiddenException` | `ACCESS_DENIED` | 403 |
| Perioodi andmeid ei leitud | `DataNotFoundException` | `IMPORT_PERIOD_NOT_FOUND` | 404 |

> **Märkus veahalduse kohta:**
> Veahalduse infrastruktuur asub: `backend/src/main/java/ee/valiit/etas/infrastructure/`
> Kontrolli, kas vajalikud `ErrorResponse` enum kirjed ja exception klassid juba eksisteerivad:
> - `backend/src/main/java/ee/valiit/etas/infrastructure/error/ErrorResponse.java`
> - `backend/src/main/java/ee/valiit/etas/infrastructure/exception/`
>
> **`IMPORT_PERIOD_NOT_FOUND` puudub** — lisa `ErrorResponse`-i kood `515`:
> `IMPORT_PERIOD_NOT_FOUND("Perioodi andmeid ei leitud", 515)`

## Andmebaas

Seotud tabelid: `sales_report`, `sales_report_detail`, `commission_calculation`

Kustutamise järjekord (foreign key piirangute tõttu):
1. Leia `sales_report.id` perioodi (`period` veerg) järgi → kui ei leidu, viska 404
2. Kustuta kõik `commission_calculation` read, kus `sales_report_id` = leitud id
3. Kustuta kõik `sales_report_detail` read, kus `sales_report_id` = leitud id
4. Kustuta `sales_report` rida

Kõik kolm kustutamist tuleb teha ühe `@Transactional` tehinguna.

## Muudetavad failid

- `ReportController.java` — lisa `@DeleteMapping`, Swagger annotatsioonid
- `ReportControllerService.java` — lisa rolli kontroll, leia periood, kustuta kolmest tabelist
- `SalesReportRepository.java` — lisa `findByPeriod(String period)` (või `existsByPeriod` täiendus)
- `SalesReportDetailRepository.java` — lisa `deleteAllBySalesReportId(Integer salesReportId)` (`@Modifying @Transactional`)
- `CommissionCalculationRepository.java` — lisa `deleteAllBySalesReportId(Integer salesReportId)` (`@Modifying @Transactional`)
- `ErrorResponse.java` — lisa `IMPORT_PERIOD_NOT_FOUND(515)`

## Vastuvõtu kriteeriumid

- [ ] `DELETE /api/import/user/{userId}/{period}` tagastab 200 OK õnnestumise korral
- [ ] Kasutajal pole Admin rolli: tagastab 403 koos `ACCESS_DENIED` veaga
- [ ] Perioodi ei leita: tagastab 404 koos `IMPORT_PERIOD_NOT_FOUND` veaga
- [ ] `IMPORT_PERIOD_NOT_FOUND` on lisatud `ErrorResponse`-i koodiga 515
- [ ] Kustutamine käib kolmest tabelist õiges järjekorras ühe tehinguna (`@Transactional`)
- [ ] Kontrolleri meetodil on `@Operation` ja `@ApiResponses` annotatsioonid
- [ ] Swagger UI kaudu on endpoint nähtav ja testitav