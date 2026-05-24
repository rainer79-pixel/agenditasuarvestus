# Teooria: BadRequestException ja Excel eksport

**Seotud taskid:** `POST /api/import/user/{userId}`, `GET /api/report/user/{userId}/export`

---

## Uus exception klass: BadRequestException

Projekti infrastruktuuris on mitut tüüpi exception-e — igaüks vastab eri HTTP staatusele:

| Exception klass | HTTP staatus |
|---|---|
| `ForbiddenException` | 403 |
| `DataNotFoundException` | 404 |
| `ConflictException` | 409 |
| `BadRequestException` | 400 ← uus |

Kõik laiendavad `RuntimeException`-i ja on registreeritud `RestExceptionHandler`-is:

```java
@ExceptionHandler
public ResponseEntity<ApiError> handleBadRequestException(BadRequestException exception) {
    ApiError apiError = new ApiError();
    apiError.setMessage(exception.getMessage());
    apiError.setErrorCode(exception.getErrorCode());
    return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
}
```

> **Miks 400, mitte 403?** 403 = "sul pole õigust". 400 = "su päring on vigane". Vigased Excel päised on kliendi viga sisendis, mitte õiguste probleem.

---

## @ResponseStatus(HttpStatus.CREATED)

Vaikimisi tagastab Spring Boot POST päringule `200 OK`. REST konventsiooni järgi peaks loomisoperatsioon tagastama `201 Created`:

```java
@PostMapping("/import/user/{userId}")
@ResponseStatus(HttpStatus.CREATED)
public void addReport(...) { ... }
```

---

## Teenuse kihi validatsioonide järjekord

`addReport` meetodis on validatsioonid kindlas järjekorras:

```java
public void addReport(Integer userId, MultipartFile file) {
    validateUserIsAdmin(userId);        // 1. rolli kontroll — 403
    // ... faili lugemine ...
    validateHeaders(headerMap);         // 2. päiste kontroll — 400
    validatePeriodIsAvailable(period);  // 3. duplikaadi kontroll — 409
    // ... salvestamine ...
}
```

Esimesena kontrollitakse õigusi — pole mõtet faili lugeda kui kasutajal pole õigust.

---

## Excel eksport Apache POI-ga

Ekspordi meetod kasutab sama `getReports()` meetodit mis nimekiri — ei duplikeerita andmepäringuloogikat:

```java
public byte[] exportReports(...) {
    List<ReportResponseDto> reports = getReports(...);  // taaskasutus
    try (XSSFWorkbook workbook = new XSSFWorkbook();
         ByteArrayOutputStream out = new ByteArrayOutputStream()) {
        // ... loo sheet, päiserida, andmeread, kokku rida ...
        workbook.write(out);
        return out.toByteArray();
    }
}
```

`ByteArrayOutputStream` — kirjutab Exceli faili mällu (mitte kettale), tagastab `byte[]`.

---

## ResponseEntity<byte[]> kontrolleris

Faili tagastamisel on vaja lisada HTTP päised, mis ütlevad brauserile, mis tüüpi fail on:

```java
public ResponseEntity<byte[]> exportReports(...) {
    byte[] file = reportControllerService.exportReports(...);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.parseMediaType(
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    headers.setContentDispositionFormData("attachment", "aruanne.xlsx");
    return new ResponseEntity<>(file, headers, HttpStatus.OK);
}
```

| Päis | Tähendus |
|---|---|
| `Content-Type` | Faili tüüp — brauser teab, et see on `.xlsx` |
| `Content-Disposition: attachment` | Brauser laeb alla, mitte ei kuva |
| `filename="aruanne.xlsx"` | Allalaaditud faili nimi |