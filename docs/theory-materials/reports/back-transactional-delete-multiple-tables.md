# Teooria: @Transactional kustutamine mitmest tabelist

**Seotud task:** `DELETE /api/import/user/{userId}/{period}`

---

## Miks see keeruline on?

Andmebaasis on foreign key piirangud — laps-tabelit ei saa kustutada enne vanem-tabelit, ja vanem-tabelit ei saa kustutada enne kui kõik lapsed on kustutatud.

Meie näites:
```
sales_report (vanem)
  ├── sales_report_detail (laps)
  └── commission_calculation (laps)
```

Kui proovida kustutada `sales_report` rida enne lapsi → andmebaas annab vea.

---

## Õige kustutamise järjekord

Kustuta alati **lapsest vanemani**:

```java
commissionCalculationRepository.deleteAllBy(salesReport.getId());  // 1. laps
salesReportDetailRepository.deleteAllBy(salesReport.getId());      // 2. laps
salesReportRepository.delete(salesReport);                          // 3. vanem
```

---

## @Modifying + @Transactional repositooriumis

Kirjutuspäringud (DELETE, UPDATE) vajavad kahte annotatsiooni:

```java
@Modifying
@Transactional
@Query("delete from SalesReportDetail s where s.salesReport.id = :salesReportId")
void deleteAllBy(Integer salesReportId);
```

| Annotatsioon | Miks vajalik |
|---|---|
| `@Modifying` | Ütleb Spring Data-le, et see pole SELECT päring |
| `@Transactional` | Tagab, et muudatus on ühe tehinguna — kui midagi läheb valesti, kõik tühistatakse |

> **Märkus:** Repositooriumi `@Transactional` on `jakarta.transaction.Transactional`. Service kihis kasutatakse `org.springframework.transaction.annotation.Transactional` — need on erinevad paketid, aga mõlemad töötavad.

---

## @Transactional service meetodis

Service meetodil on samuti `@Transactional` — see tagab, et **kõik kolm kustutamist käivad ühe tehinguna**:

```java
@Transactional
public void deleteReport(Integer userId, String period) {
    validateUserIsAdmin(userId);
    SalesReport salesReport = salesReportRepository.findByPeriod(period)
            .orElseThrow(...);
    commissionCalculationRepository.deleteAllBy(salesReport.getId());
    salesReportDetailRepository.deleteAllBy(salesReport.getId());
    salesReportRepository.delete(salesReport);
}
```

Kui kolmas kustutamine ebaõnnestub — kaks esimest **tühistatakse automaatselt**. Andmebaas jääb puhtasse olekusse.

---

## Veakoodi valik

`getSalesReportByPeriod()` on jagatud abimeetod — ta kasutab üldist `REPORT_NOT_FOUND` viga.

`deleteReport()` vajab spetsiifilisemat viga `IMPORT_PERIOD_NOT_FOUND` — seetõttu ei kasuta ta jagatud abimeetodit, vaid teeb päringu otse:

```java
// VALE — kasutab vale veakoodi
SalesReport salesReport = getSalesReportByPeriod(period);  // tagastab REPORT_NOT_FOUND

// ÕIGE — spetsiifiline veakood
SalesReport salesReport = salesReportRepository.findByPeriod(period)
        .orElseThrow(() -> new DataNotFoundException(
                IMPORT_PERIOD_NOT_FOUND.getMessage(),
                IMPORT_PERIOD_NOT_FOUND.getErrorCode()));
```

---

## Kokkuvõte

| Kontseptsioon | Reegel |
|---|---|
| Kustutamise järjekord | Lapsest vanemani (foreign key järgi) |
| `@Modifying` | Kohustuslik kõigil mitte-SELECT päringutes |
| `@Transactional` service-s | Kõik operatsioonid ühe tehinguna — vea korral kõik tühistatakse |
| Veakoodi valik | Jagatud abimeetod vs. inline päring — sõltub sellest, millist veakoodi vajad |