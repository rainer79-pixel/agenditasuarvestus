# Excel faili import — Apache POI algajatele

## Mis see on ja miks oluline?

Kasutaja laadib brauseris üles Exceli faili (`.xlsx`) — backend loeb selle lahti, võtab andmed välja ja salvestab andmebaasi.

**Päriselust analoogia:** Kujuta ette postkontori töötajat. Keegi toob paki (Exceli fail). Töötaja:
1. Avab paki (loeb faili)
2. Kontrollib, kas saatja aadress on olemas (valideerib päised)
3. Sorteerib sisu lahtritesse (loeb read)
4. Kirjutab üles kõik mis laekus (salvestab andmebaasi)

---

## Failid mis selleks loodud sai

```
backend/
  build.gradle                          ← apache POI dependency lisatud
  src/main/java/.../
    controller/
      ReportController.java             ← võtab faili vastu HTTP-st
    controller/report/dto/
      SalesReportRowDto.java            ← ühe Exceli rea andmed
    service/
      ReportControllerService.java      ← kogu import loogika
    persistence/
      salesreport/
        SalesReport.java                ← impordi sessioon (periood + kasutaja)
        SalesReportRepository.java
      salesreportdetail/
        SalesReportDetail.java          ← üks Exceli rida andmebaasis
        SalesReportDetailMapper.java
        SalesReportDetailRepository.java
      view/
        CommissionCalculationView.java  ← VIEW entity (ainult lugemine)
        CommissionCalculationViewMapper.java
      CommissionCalculationViewRepository.java
```

---

## Andmevoog — suur pilt

```
BRAUSER
  ↓ POST /api/report/user/1  +  .xlsx fail
KONTROLLER  (ReportController)
  ↓ MultipartFile file
SERVICE     (ReportControllerService.addReport)
  ↓
  1. Ava fail → XSSFWorkbook
  2. Loe päiserida → headerMap {"seller_org_id": 2, "tyyp": 6, ...}
  3. Valideeri päised — kas kõik vajalikud veerud on olemas?
  4. Loe read → List<SalesReportRowDto>
  5. Salvesta sales_report (sessioon)
  6. Salvesta sales_report_detail read
  7. Loe commission_calculation_view
  8. Salvesta commission_calculation read
  ↓
ANDMEBAAS
```

---

## Apache POI — mis see on?

Apache POI on Java teek (library) Exceli failide lugemiseks ja kirjutamiseks.

```
build.gradle:
    implementation 'org.apache.poi:poi-ooxml:5.x'
```

Excel faili struktuur Apache POI-ga:

```
XSSFWorkbook  (kogu .xlsx fail)
  └── Sheet   (üks leht — nt "Sheet1")
        └── Row   (üks rida — nt rida nr 0 on päiserida)
              └── Cell  (üks lahter — nt "pilet", "13", "133.00")
```

```java
Workbook workbook = new XSSFWorkbook(file.getInputStream());
//                   ↑ avab faili                ↑ saab faili sisu voogu

Sheet sheet = workbook.getSheetAt(0);
//                              ↑ esimene leht (indeks 0)

Row headerRow = sheet.getRow(0);
//                          ↑ esimene rida (päiserida)

Row dataRow = sheet.getRow(1);
//                        ↑ teine rida (esimene andmerida)
```

---

## MultipartFile — mis see on?

`MultipartFile` on Spring Booti klass mis esindab brauserist saadetud faili.

```java
// Kontroller võtab faili vastu nii:
@PostMapping("/report/user/{userId}")
public void addReport(
    @PathVariable Integer userId,
    @RequestParam("file") MultipartFile file)  // ← see on Exceli fail
{
    reportControllerService.addReport(userId, file);
}
```

```
BRAUSER saadab:                    SPRING BOOT näeb:
┌─────────────────────┐            MultipartFile
│ Content-Type:       │               .getOriginalFilename() → "aruanne.xlsx"
│ multipart/form-data │               .getInputStream()      → faili sisu voog
│                     │               .getSize()             → faili suurus baitides
│ file: [aruanne.xlsx]│
└─────────────────────┘
```

**`try-with-resources` muster:**

```java
try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
    // tee midagi workbook'iga
}
// ← sulgub automaatselt peale try blokki — mälu vabastatakse
```

`try (...)` tagab, et fail suletakse alati — isegi kui viga tekib.

---

## Päiste kaart (headerMap) — miks see vajalik on

Excelis pole veerud fikseeritud positsioonidel. "seller_org_id" võib olla 3. või 5. veerus — oleneb failist.

Lahendus: loeme päiserida ja teeme sõnastiku `{veeru nimi → veeru number}`:

```java
private Map<String, Integer> buildHeaderMap(Row headerRow) {
    Map<String, Integer> map = new HashMap<>();
    for (Cell cell : headerRow) {
        map.put(cell.getStringCellValue().trim(), cell.getColumnIndex());
    }
    return map;
}
```

**Näide — Exceli päiserida:**

```
Veerg:    0           1            2              3         4
Väärtus:  issuer_name seller_name  seller_org_id  dept_name tyyp
```

**Tulemus (headerMap):**

```java
{
  "issuer_name":   0,
  "seller_name":   1,
  "seller_org_id": 2,
  "dept_name":     3,
  "tyyp":          4,
  ...
}
```

**Kasutamine rea lugemisel:**

```java
dto.setOrgId(getIntCell(row, headerMap.get("seller_org_id")));
//                                              ↑
//                              headerMap.get("seller_org_id") = 2
//                              → loe rea 2. lahter
```

---

## Rea lugemine — getStringCell / getIntCell

Excelis on lahtritel **tüüp** — string, number, kuupäev. Peame kontrollima:

```java
private String getStringCell(Row row, Integer colIndex) {
    Cell cell = row.getCell(colIndex);
    if (cell == null) return null;
    return switch (cell.getCellType()) {
        case STRING  -> cell.getStringCellValue();
        case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
        //              ↑ number lahter → teisendame stringiks
        default -> null;
    };
}
```

```java
private Integer getIntCell(Row row, Integer colIndex) {
    Cell cell = row.getCell(colIndex);
    if (cell == null) return null;
    return switch (cell.getCellType()) {
        case NUMERIC -> (int) cell.getNumericCellValue();
        case STRING  -> Integer.parseInt(cell.getStringCellValue().trim());
        default -> null;
    };
}
```

---

## Edasimüüja leidmine org_id järgi

Exceli fail ei sisalda `seller_id`-d (andmebaasi sisemist ID-d) — ainult `seller_org_id` (nt 1588 = "Agent A OÜ").

```java
Optional<Seller> seller = sellerRepository.findByOrgId(orgId);
if (seller.isEmpty()) continue;   // ← tundmatut edasimüüjat lihtsalt ei salvesta
```

```
Exceli rida:  seller_org_id = 1588
                    ↓
DB päring:    SELECT * FROM seller WHERE org_id = 1588
                    ↓
Tulemus:      Seller{id=1, companyName="Agent A OÜ"}
                    ↓
Salvesta:     salesReportDetail.setSeller(seller)
```

---

## Täielik impordi voog koodis

```java
@Transactional
public void addReport(Integer userId, MultipartFile file) {
    try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {

        Sheet sheet = workbook.getSheetAt(0);                  // 1. võta esimene leht
        Map<String, Integer> headerMap = buildHeaderMap(       // 2. kaardista päised
            sheet.getRow(0));
        validateHeaders(headerMap);                            // 3. kontrolli päised
        List<SalesReportRowDto> rows = parseRows(              // 4. loe kõik read
            sheet, headerMap);

        String period = rows.get(0).getPeriod();               // 5. periood esimesest reast
        User user = userRepository.findById(userId)...;

        SalesReport salesReport = createAndSaveSalesReport(    // 6. salvesta sessioon
            user, period);
        List<SalesReportDetail> details =
            salesReportDetailMapper.toSalesReportDetails(rows);
        List<SalesReportDetail> validDetails =
            attachSalesReportAndSeller(                        // 7. leia edasimüüjad
                details, rows, salesReport);
        salesReportDetailRepository.saveAll(validDetails);     // 8. salvesta read

        createCommissionCalculations(salesReport);             // 9. arvuta teenustasud

    } catch (IOException e) {
        throw new RuntimeException("Faili lugemine ebaõnnestus");
    }
}
```

---

## Kaks tabelit — miks on vaja nii `sales_report` kui `sales_report_detail`?

```
sales_report                         sales_report_detail
┌────────────────────────────┐        ┌──────────────────────────────────┐
│ id = 1                     │        │ id = 1                           │
│ period = "2026-4"          │◄───────│ sales_report_id = 1             │
│ created_by = user (Admin)  │        │ seller_id = 1                   │
│ created_at = 2026-05-22    │        │ product_type = "kaardimyyk"     │
└────────────────────────────┘        │ transaction_count = 13          │
         1 sessioon                   │ sales_amount = 133.00           │
                                      ├──────────────────────────────────┤
                                      │ id = 2                           │
                                      │ sales_report_id = 1             │
                                      │ seller_id = 1                   │
                                      │ product_type = "rahalaadimine"  │
                                      │ ...                              │
                                      └──────────────────────────────────┘
                                               N rida (üks Exceli rida = üks DB rida)
```

`sales_report` = üks import (kes, millal, mis periood)  
`sales_report_detail` = kõik Exceli read sellest impordist

---

## Kokkuvõte

| Mõiste | Selgitus |
|--------|---------|
| `MultipartFile` | Spring Booti objekt — esindab brauserist tulnud faili |
| `XSSFWorkbook` | Apache POI klass — avab `.xlsx` faili |
| `Sheet` | Exceli leht (esimene leht on indeksiga 0) |
| `Row` | Exceli rida |
| `Cell` | Exceli lahter (ühes reas) |
| `headerMap` | `Map<String, Integer>` — veeru nimi → veeru number |
| `try-with-resources` | Avab ja sulgeb faili automaatselt |
| `sales_report` | Impordi sessioon — üks rida perioodiga |
| `sales_report_detail` | Kõik Exceli read — üks DB rida = üks Exceli rida |

---

## Järgmised sammud

- SQL VIEW — kuidas `commission_calculation_view` neid ridu kasutab (`sql-view.md`)
- `commission_calculation` tabelisse kirjutamine — `createCommissionCalculations()` meetod