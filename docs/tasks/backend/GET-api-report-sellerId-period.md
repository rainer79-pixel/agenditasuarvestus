# GET /api/report/{sellerId}/{period}

**Kontroller:** `ReportController.java`
**Tüüp:** Backend
**Staatus:** To Do

## Kontekst

`ReportsView` aruande ülevaate tabelis on iga edasimüüja real "Vaata" nupp — see laadib laiendatava rea, mis näitab tootegruppide kaupa jaotust (tehingute arv, müügisumma, teenustasu määrad, arvutatud teenustasu, KM). Üks DTO rida = üks tootegrupp ühe edasimüüja perioodi aruandes. Periood on formaadis `"2026-4"` (YYYY-M) — täpselt nii nagu salvestatud `sales_report.period` veergu. Seotud endpoint: `GET /api/report/user/{userId}` annab ülevaate taseme, see endpoint annab detaili taseme.

## Mocki vaade

![ReportsView mock](../../balsamiq/views/ReportsView.vue.png)

## API leping

| Väli | Väärtus |
|------|---------|
| Meetod | `GET` |
| Tee | `/api/report/{sellerId}/{period}` |
| Auth | Ei (mõlemad rollid näevad aruandeid) |

### Request Body

Puudub — path parameetrid `{sellerId}` ja `{period}`

### Response Body — `ReportDetailResponseDto.java`

> Schema: [`ReportDetailResponseDto_schema.json`](../../dtos/schema/ReportDetailResponseDto_schema.json)
> Näidis: [`ReportDetailResponseDto_ReportsView_Array_example.json`](../../dtos/examples/ReportDetailResponseDto_ReportsView_Array_example.json)

| Väli | Tüüp | Allikas |
|------|------|---------|
| `productTypeName` | `String` | `product_type.product_type_name` (läbi `commission_rate`) |
| `transactionCount` | `Integer` | `commission_calculation_view.transaction_count_sum` |
| `salesAmount` | `BigDecimal` | `commission_calculation_view.sales_amount_sum` |
| `feePerTransaction` | `BigDecimal` | `commission_rate.fee_per_transaction` (null kui protsendipõhine) |
| `feePercent` | `BigDecimal` | `commission_rate.fee_percent` (null kui tehingupõhine) |
| `calculatedFee` | `BigDecimal` | `commission_calculation.calculated_fee` |
| `vatAmount` | `BigDecimal` | `commission_calculation.vat_amount` |
| `totalFee` | `BigDecimal` | `commission_calculation.calculated_fee_plus_vat` |

> **Märkus:** `feePerTransaction` ja `feePercent` on vastastikku eksklusiivsed — ühel tootegrupil on alati üks null ja teine väärtus.

## Veahaldus

| Olukord | Exception klass | ErrorResponse enum | HTTP staatus |
|---------|----------------|-------------------|--------------|
| Aruannet ei leitud (sellerId + period kombinatsioon puudub) | `DataNotFoundException` | `REPORT_NOT_FOUND` | 404 |

> **Märkus veahalduse kohta:**
> Veahalduse infrastruktuur asub: `backend/src/main/java/ee/valiit/etas/infrastructure/`
> `REPORT_NOT_FOUND(516)` lisatakse `ErrorResponse`-i `GET /api/report/user/{userId}` taskis —
> kontrolli, kas see on juba lisatud enne implementeerimist.

## Andmebaas

Seotud tabelid: `sales_report`, `sales_report_detail`, `commission_calculation`, `commission_rate`, `product_type`

Päring peab:
1. Leidma `sales_report` rea, kus `period = :period` ja sellele vastavad `sales_report_detail` read, kus `seller_id = :sellerId`
2. Leidma kõik `commission_calculation` read, mis on seotud selle `sales_report_id`-ga ja selle `sellerId`-ga (läbi `commission_rate.seller_id`)
3. Liitma `commission_rate` (teenustasumäärad) ja `product_type` (tootegrupinimed)

> **Soovitus:** `commission_calculation_view` sisaldab juba koondandmed (`transaction_count_sum`, `sales_amount_sum`, `calculated_fee`, `vat_amount`, `calculated_fee_plus_vat`, `fee_per_transaction`, `fee_percent`, `product_type_name`) grupeerimisel seller + product_type + sales_report tasemel — seda saab kasutada otse filtreeritud päringus (`seller_id = :sellerId` ja `sales_report.period = :period`). Vaata olemasolevat `CommissionCalculationViewRepository`-t.

## Muudetavad failid

- `ReportController.java` — lisa `@GetMapping("/report/{sellerId}/{period}")`, Swagger annotatsioonid
- `ReportControllerService.java` — uus meetod detailide pärimiseks `sellerId` + `period` järgi
- `CommissionCalculationViewRepository.java` — lisa meetod `findBySalesReportPeriodAndSellerId(String period, Integer sellerId)` (või sarnane)
- `ReportDetailResponseDto.java` *(uus)* — loo `controller/report/dto/` paketti

## Vastuvõtu kriteeriumid

- [ ] `GET /api/report/{sellerId}/{period}` tagastab 200 ja tootegruppide nimekirja
- [ ] Iga DTO rida vastab ühele tootegrupile (productTypeName täidetud)
- [ ] `feePerTransaction` ja `feePercent` on vastavalt null või väärtusega (mitte mõlemad täidetud)
- [ ] Kui sellerId + period kombinatsiooni ei leidu: tagastab 404 koos `REPORT_NOT_FOUND` veaga
- [ ] `ReportDetailResponseDto` klass on loodud Java klassina õigesse paketti
- [ ] Kontrolleri meetodil on `@Operation` ja `@ApiResponses` annotatsioonid
- [ ] Swagger UI kaudu on endpoint nähtav ja testitav