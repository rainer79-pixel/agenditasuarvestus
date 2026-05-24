# POST /api/seller/{sellerId}/commission-rates — teenustasu lisamine

## Mis see on ja miks oluline?

See endpoint lisab edasimüüjale uue teenustasu määra. Admin valib `SellerSettingsProductModal`-is tootegruppi dropdownist ja täidab tasu, KM ja kehtivusaja väljad — "Lisa" nupp kutsub selle endpointi.

**Päriselust analoogia:** Kujuta ette lepingusse hinnakirja lisamist — "edasimüüja X maksab tootegrupist Y tasu Z, mis kehtib alates kuupäevast." Enne lisamist kontrollid: kas sul on õigus? Kas edasimüüja ja tootegrupp on olemas? Kas sama tootegrupil pole juba lõputu hinnakiri?

---

## Mis on selles taskis uut võrreldes varasema POST-iga?

| Asi | POST regions | POST commission-rates |
|-----|-------------|----------------------|
| FK objektid | `seller`, `region` käsitsi | `seller`, `productType` käsitsi |
| Kuupäevaväljad | Puuduvad | `validFrom`, `validTo` — `String` → `LocalDate` |
| 409 kontroll | Duplikaat (seller+region) | Lõputu teenustasu (seller+productType+validTo IS NULL) |
| `@ResponseStatus` | Puudus | `@ResponseStatus(HttpStatus.CREATED)` — tagastab 201 |

---

## Andmevoog — suur pilt

```
BROWSER (Admin vajutab "Lisa" nuppu modaalil)
    ↓
FRONTEND (saadab POST päringu koos JSON body-ga)
    ↓
AXIOS → POST /api/seller/1/commission-rates?userId=1
        Body: { "productTypeId": 2, "feePerTransaction": 0.01, ... }
    ↓  ← siit algab backend
SellerCommissionRateController.addSellerCommissionRate()
    ↓
SellerCommissionRateService.addSellerCommissionRate()
    ↓
  validateUserIsAdmin()           ← kas userId on Admin?
  validateSellerExists()          ← kas seller olemas?
  productTypeRepository.findById  ← kas tootegrupp olemas? tagastab objekti
  commissionRateExistsBy()        ← kas sama tootegrupil pole juba lõputut tasu?
  commissionRateMapper.toCommissionRate()  ← DTO → entity
  commissionRate.setSeller()      ← FK objekt käsitsi
  commissionRate.setProductType() ← FK objekt käsitsi
  commissionRateRepository.save() ← salvesta
    ↓
ANDMEBAAS (INSERT INTO commission_rate ...)
    ↓
CONTROLLER (tagastab 201 Created)
```

---

## BACKEND

### 1. ErrorResponse — uus veakood

```java
// Commission rate
COMMISSION_RATE_NOT_FOUND("Teenustasu määra ei leitud", 411),
COMMISSION_RATE_IN_USE("Teenustasu on kasutuses ja seda ei saa kustutada", 412),
COMMISSION_RATE_ALREADY_EXISTS("Sellel tootegrupil on juba kehtiv teenustasu", 413),  // ← uus
```

---

### 2. CommissionRateRepository — validTo IS NULL kontroll

```java
@Query("""
        select (count(c) > 0) from CommissionRate c
        where c.seller.id = :sellerId and c.productType.id = :productTypeId and c.validTo is null""")
boolean commissionRateExistsBy(Integer sellerId, Integer productTypeId);
```

**Miks `validTo is null`?**

`validTo = null` tähendab lõputu kehtivus — teenustasu kehtib igavesti. Kui sama tootegrupil on juba lõputu teenustasu olemas, ei saa uut lisada — need kattuksid.

```
validTo = null       → lõputu kehtivus → 409 kui sama tootegrupp
validTo = 31.12.2024 → lõplik kehtivus → lubatud lisada uus
```

**Miks `c.validTo is null` mitte `c.validTo = null`?**
SQL-is ei saa `= null` kasutada — null pole väärtus, see on "puudub". Selleks on spetsiaalne `IS NULL` operaator.

---

### 3. CommissionRateMapper — kuupäevade teisendus

```java
@Mapping(ignore = true, target = "id")
@Mapping(ignore = true, target = "seller")
@Mapping(ignore = true, target = "productType")
@Mapping(source = "feePerTransaction", target = "feePerTransaction")
@Mapping(source = "feePercent", target = "feePercent")
@Mapping(source = "includesVat", target = "includesVat")
@Mapping(expression = "java(commissionRateDto.getValidFrom() != null ? LocalDate.parse(commissionRateDto.getValidFrom(), DateTimeFormatter.ofPattern(\"dd.MM.yyyy\")) : null)",
        target = "validFrom")
@Mapping(expression = "java(commissionRateDto.getValidTo() != null ? LocalDate.parse(commissionRateDto.getValidTo(), DateTimeFormatter.ofPattern(\"dd.MM.yyyy\")) : null)",
        target = "validTo")
CommissionRate toCommissionRate(CommissionRateDto commissionRateDto);
```

**Kuupäevateisenduse lahtiseletus:**

```
commissionRateDto.getValidFrom() != null   ← kas väärtus on olemas?
    ? LocalDate.parse(                     ← kui jah: teisenda String → LocalDate
        commissionRateDto.getValidFrom(),  ← string väärtus DTO-st ("01.01.2024")
        DateTimeFormatter.ofPattern("dd.MM.yyyy")  ← mis formaadis string on
      )
    : null                                 ← kui ei: jäta null
```

**Miks `expression = "java(...)"` ja mitte lihtsalt `source`?**
Tavaline `source` kopeerib väärtuse otse. Siin on vaja teisendust — `String` → `LocalDate`. MapStruct ei oska seda automaatselt teha, seepärast kirjutame teisenduse käsitsi Java koodina.

**`@Mapper` annotatsioonis on `imports`:**
```java
@Mapper(..., imports = {LocalDate.class, DateTimeFormatter.class})
```
Need impordid on vajalikud, et MapStruct genereeritud klass teaks `LocalDate` ja `DateTimeFormatter` klasse.

---

### 4. SellerCommissionRateService — addSellerCommissionRate

```java
@Transactional
public void addSellerCommissionRate(Integer sellerId, Integer userId, CommissionRateDto commissionRateDto) {
    validateUserIsAdmin(userId);
    validateSellerExists(sellerId);
    Seller seller = sellerRepository.findById(sellerId)
            .orElseThrow(() -> new DataNotFoundException(SELLER_NOT_FOUND.getMessage(), SELLER_NOT_FOUND.getErrorCode()));
    ProductType productType = productTypeRepository.findById(commissionRateDto.getProductTypeId())
            .orElseThrow(() -> new DataNotFoundException(PRODUCT_TYPE_NOT_FOUND.getMessage(), PRODUCT_TYPE_NOT_FOUND.getErrorCode()));
    if (commissionRateRepository.commissionRateExistsBy(sellerId, commissionRateDto.getProductTypeId())) {
        throw new ConflictException(COMMISSION_RATE_ALREADY_EXISTS.getMessage(), COMMISSION_RATE_ALREADY_EXISTS.getErrorCode());
    }
    CommissionRate commissionRate = commissionRateMapper.toCommissionRate(commissionRateDto);
    commissionRate.setSeller(seller);
    commissionRate.setProductType(productType);
    commissionRateRepository.save(commissionRate);
}
```

**Miks `validateSellerExists` ja eraldi `findById` mõlemad?**
`validateSellerExists` kontrollib kas seller eksisteerib (`void`). Hiljem vajame `Seller` objekti — seepärast kutsume `findById` uuesti. Jah, see on kaks päringut, aga kood on selgem.

**FK objektide käsitsi seadmine:**
```
commissionRate.setSeller(seller);       ← mapper ignoreeris, seame käsitsi
commissionRate.setProductType(productType);  ← mapper ignoreeris, seame käsitsi
```
Mapper ei oska ID-st objekti teha — see nõuab andmebaasipäringut. Service teab mõlemat: nii DTO andmeid kui repositorytest laetud objekte.

---

### 5. SellerCommissionRateController — @PostMapping ja @ResponseStatus

```java
@PostMapping("/seller/{sellerId}/commission-rates")
@ResponseStatus(HttpStatus.CREATED)
@Operation(summary = "Lisa teenustasu")
@ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Teenustasu lisatud"),
        @ApiResponse(responseCode = "400", description = "Kohustuslik väli puudub", ...),
        @ApiResponse(responseCode = "403", description = "Kasutajal pole õigust", ...),
        @ApiResponse(responseCode = "404", description = "Edasimüüjat või tootegruppi ei leitud", ...),
        @ApiResponse(responseCode = "409", description = "Sellel tootegrupil on juba kehtiv teenustasu", ...),
        @ApiResponse(responseCode = "500", description = "Serveri viga", ...)})
public void addSellerCommissionRate(@PathVariable Integer sellerId,
                                    @RequestParam Integer userId,
                                    @Valid @RequestBody CommissionRateDto commissionRateDto) {
    sellerCommissionRateService.addSellerCommissionRate(sellerId, userId, commissionRateDto);
}
```

**Miks `@ResponseStatus(HttpStatus.CREATED)`?**

Spring tagastab vaikimisi `200 OK`. POST lisamisel on õige `201 Created` — see ütleb frontendile et uus ressurss loodi. Ilma selle annotatsioonita tuleb `200`, mitte `201`.

| Kood | Tähendus | Millal |
|------|----------|--------|
| `200 OK` | Päring õnnestus | GET, PUT, DELETE |
| `201 Created` | Uus ressurss loodud | POST lisamisel |

---

## Kokkuvõte

| Samm | Fail | Mida tegime |
|------|------|-------------|
| 1 | `ErrorResponse.java` | Lisasime `COMMISSION_RATE_ALREADY_EXISTS` (413) |
| 2 | `CommissionRateRepository.java` | Lisasime `commissionRateExistsBy()` — `validTo IS NULL` kontroll |
| 3 | `CommissionRateMapper.java` | Lisasime `toCommissionRate()` koos kuupäevateisendusega |
| 4 | `SellerCommissionRateService.java` | Lisasime `addSellerCommissionRate()` — 4 kontrollimist + salvestamine |
| 5 | `SellerCommissionRateController.java` | Lisasime `@PostMapping` koos `@ResponseStatus(HttpStatus.CREATED)` |

**Peamine õppetund:** Kuupäevaväljad vajavad mapperis käsitsi teisendust (`expression = "java(...)"`). FK objektid (`seller`, `productType`) seatakse alati käsitsi service'is — mapper teeb ainult primitiivväljad. `@ResponseStatus(HttpStatus.CREATED)` on vajalik, muidu Spring tagastab 200 mitte 201.

---

## Järgmised sammud

- Frontend: `SellerSettingsProductModal.vue` — teenustasu lisamise modal (kasutab seda endpointi)
- `PUT /api/seller/{sellerId}/commission-rates/{commissionRateId}` — teenustasu muutmine (sama muster)