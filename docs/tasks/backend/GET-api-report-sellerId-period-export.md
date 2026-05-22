# GET /api/report/{sellerId}/{period}/export

**Kontroller:** `ReportController.java`
**Tüüp:** Backend
**Staatus:** To Do

## Kontekst

`ReportsView` aruande ülevaate tabelis on iga edasimüüja real "Ekspordi XLS" nupp. Backend genereerib Apache POI-ga `.xlsx` faili, mis sisaldab sama tootegruppide jaotust mis `GET /api/report/{sellerId}/{period}`. Periood on URL-is formaadis `"2026-4"` (YYYY-M) — ehk sama andmekogum, lihtsalt Exceli formaadis allalaadimiseks. Frontend saab vastuse binaarfailina ja käivitab brauseri allalaadimisdialoogi.

## Mocki vaade

![ReportsView mock](../../balsamiq/views/ReportsView.vue.png)

## API leping

| Väli | Väärtus |
|------|---------|
| Meetod | `GET` |
| Tee | `/api/report/{sellerId}/{period}/export` |
| Auth | Ei (mõlemad rollid saavad eksportida) |

### Request Body

Puudub — path parameetrid `{sellerId}` ja `{period}`

### Response Body

Puudub — HTTP 200 koos `.xlsx` binaarfailiga

| HTTP päis | Väärtus |
|-----------|---------|
| `Content-Type` | `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet` |
| `Content-Disposition` | `attachment; filename="report_{sellerId}_{period}.xlsx"` |

Kontrolleri meetodi tagastustüüp: `ResponseEntity<byte[]>`

## Veahaldus

| Olukord | Exception klass | ErrorResponse enum | HTTP staatus |
|---------|----------------|-------------------|--------------|
| Aruannet ei leitud (sellerId + period kombinatsioon puudub) | `DataNotFoundException` | `REPORT_NOT_FOUND` | 404 |

> **Märkus veahalduse kohta:**
> `REPORT_NOT_FOUND(516)` lisatakse `ErrorResponse`-i `GET /api/report/user/{userId}` taskis —
> kontrolli, et see on juba olemas enne implementeerimist.

## Andmebaas

Seotud tabelid: `sales_report`, `commission_calculation`, `commission_rate`, `product_type`, `seller`

Eksporditavad andmed on samad mis `GET /api/report/{sellerId}/{period}` detailvaatel:
- Iga rida = üks tootegrupp
- Veerud: tootegrupp, tehinguid, müügisumma, tasu koguselt, tasu %, teenustasu, KM, kokku

> **Soovitus:** Kasuta `GET /api/report/{sellerId}/{period}` jaoks kirjutatud teenusemeetodit uuesti —
> päri `ReportDetailResponseDto` massiiv ja teisenda see Apache POI-ga Exceliks.
> Nii ei duplikeerita andmepäringulogika.

## Exceli fail — veergude struktuur

| Veerg | Sisu | Allikas |
|-------|------|---------|
| A | Tootegrupp | `productTypeName` |
| B | Tehinguid | `transactionCount` |
| C | Müügisumma (EUR) | `salesAmount` |
| D | Tasu koguselt (EUR) | `feePerTransaction` |
| E | Tasu % | `feePercent` |
| F | Teenustasu (EUR) | `calculatedFee` |
| G | KM (EUR) | `vatAmount` |
| H | Kokku (EUR) | `totalFee` |

Faili päiserea kujundus (soovituslik): bold, taustavärv.

## Muudetavad failid

- `ReportController.java` — lisa `@GetMapping("/report/{sellerId}/{period}/export")`, tagastustüüp `ResponseEntity<byte[]>`, Swagger annotatsioonid
- `ReportControllerService.java` — uus meetod `exportReport(Integer sellerId, String period)`, kasutab Apache POI `XSSFWorkbook`-i
- Apache POI on juba `build.gradle`-s olemas (kasutati impordi jaoks)

## Vastuvõtu kriteeriumid

- [ ] `GET /api/report/{sellerId}/{period}/export` tagastab 200 koos `.xlsx` failiga
- [ ] Vastus sisaldab `Content-Disposition: attachment; filename="..."` päist
- [ ] Exceli fail sisaldab päiserea ja andmeridu tootegruppide kaupa
- [ ] Kui sellerId + period kombinatsiooni ei leidu: tagastab 404 koos `REPORT_NOT_FOUND` veaga
- [ ] Kontrolleri meetodil on `@Operation` ja `@ApiResponses` annotatsioonid
- [ ] Swagger UI kaudu on endpoint nähtav ja allalaadimist saab testida