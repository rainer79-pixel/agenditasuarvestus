# PUT teenustasu — algajatele

## Mis see on ja miks oluline?

See endpoint uuendab edasimüüja teenustasu kõiki välju: tootegruppi, tasu koguselt, tasu protsenti, KM sisaldumist ja kehtivusperioodi.
Kasutaja näeb `SellerSettingsView`-s teenustasude nimekirja — iga real on pliiatsikoon mis avab muutmisvormi.

**Päriselust analoogia:** Kujuta ette tabelit kus on kirjas kõik lepingutingimused. Kui avad ühe rea ja muudad mitut väärtust korraga — kuupäevi, summasid, tootegruppi — kirjutatakse kõik uued väärtused üle. Rida ise (selle ID) jääb samaks.

---

## Mis on selles taskis uut võrreldes eelmiste PUT taskidega?

Eelmine PUT task (`seller-region-put.md`) uuendas ainult **ühte välja** käsitsi (`setSalesPointCount`). Siin on **6 välja** ja kasutame mapperit automaatseks uuendamiseks. Lisaks tuleb tegeleda kuupäevadega ja seotud entity laadimisega.

| Uus asi | Kus näed | Mida tähendab |
|---------|---------|---------------|
| **`@MappingTarget`** | `CommissionRateMapper` | Mapper uuendab olemasolevat entity't, ei loo uut |
| **`@BeanMapping(IGNORE)`** | `CommissionRateMapper` | Null väärtused jäetakse muutmata |
| **String → LocalDate** | `CommissionRateMapper` | Kuupäev tuleb brauserist stringina, DB vajab `LocalDate` objekti |
| **Seotud entity laadimine** | `SellerCommissionRateService` | `productTypeId` (Integer) → `ProductType` objekt — mapper ei suuda seda teha, service teeb |

---

## Andmevoog — suur pilt

```
BROWSER (Admin muudab teenustasu andmeid ja vajutab Salvesta)
    ↓
FRONTEND (saadab PUT päringu)
    ↓
AXIOS → PUT /api/seller/1/commission-rates/2
        Request body: {"productTypeId": 2, "feePercent": 2.00, "includesVat": true, "validFrom": "01.06.2022", "validTo": null}
    ↓  ← siit algab backend
CONTROLLER (@Valid kontrollib kohustuslikud väljad)
    ↓
SERVICE (seller olemas? → commissionRate olemas? → productType olemas? → uuenda)
    ↓
MAPPER (kirjutab DTO väljad entity peale, teisendab kuupäevad)
    ↓
REPOSITORY (save → UPDATE commission_rate SET ... WHERE id=2)
    ↓
CONTROLLER (tagastab 200 OK, tühi vastus)
    ↓
BROWSER (kasutaja näeb uuendatud andmeid)
```

---

## BACKEND

### 1. `CommissionRateDto.java` — sisend (request body)

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommissionRateDto {
    private Integer productTypeId;
    private BigDecimal feePerTransaction;
    private BigDecimal feePercent;
    @NotNull
    private Boolean includesVat;
    @NotNull
    private String validFrom;
    private String validTo;
}
```

**Miks `String` ja mitte `LocalDate` kuupäeva jaoks?**
Brauser saadab kuupäeva eesti formaadis `"01.06.2022"`. See ei ole Java vaikimisi `LocalDate` formaat (`2022-06-01`). Mapper teisendab stringi `LocalDate`-ks backendis.

**Miks `feePerTransaction` ja `feePercent` on ilma `@NotNull`?**
Üks neist on alati `null` — kas kasutatakse kogusepõhist tasu või protsenti, mitte mõlemat korraga.

---

### 2. `CommissionRateMapper` — `@MappingTarget` muster

```java
@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
@Mapping(ignore = true, target = "id")
@Mapping(ignore = true, target = "productType")
@Mapping(expression = "java(commissionRateDto.getValidFrom() != null ? LocalDate.parse(commissionRateDto.getValidFrom(), DateTimeFormatter.ofPattern(\"dd.MM.yyyy\")) : null)", target = "validFrom")
@Mapping(expression = "java(commissionRateDto.getValidTo() != null ? LocalDate.parse(commissionRateDto.getValidTo(), DateTimeFormatter.ofPattern(\"dd.MM.yyyy\")) : null)", target = "validTo")
void updateCommissionRate(CommissionRateDto commissionRateDto, @MappingTarget CommissionRate commissionRate);
```

**`@MappingTarget` — mida see tähendab?**

Tavaline mapper loob **uue** objekti:
```
DTO → Mapper → uus Entity objekt
```

`@MappingTarget` mapper **uuendab olemasolevat** objekti:
```
DTO + olemasolev Entity → Mapper → sama Entity, uute väärtustega
```

See on oluline PUT puhul — tahame muuta olemasolevat andmebaasireal, mitte luua uut.

**`@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)` — mida see teeb?**

Kui DTO-s on mõni väli `null`, **ärge kirjutage seda entity peale** — jätke vana väärtus alles.

```
DTO: {feePerTransaction: null, feePercent: 2.00}
Entity enne: {feePerTransaction: 0.01, feePercent: null}
Entity pärast: {feePerTransaction: 0.01, feePercent: 2.00}  ← feePerTransaction ei muutunud!
```

**Miks `productType` on `ignore = true`?**
Mapper saab mappida lihtsaid välju (Integer, String, BigDecimal). Aga `productType` on **objekt** (`ProductType` entity) — mapper ei suuda `Integer` (productTypeId) → `ProductType` (entity andmebaasist) teisendust teha. Service laadib selle käsitsi.

**Kuupäeva teisendus stringist LocalDate-ks:**
```java
LocalDate.parse("01.06.2022", DateTimeFormatter.ofPattern("dd.MM.yyyy"))
// tulemus: LocalDate.of(2022, 6, 1)
```
Null kontroll on vajalik — `validTo` võib olla `null` (kehtib lõputult).

---

### 3. Service — kolm kontrollimist + ProductType laadimine

```java
@Transactional
public void updateCommissionRate(Integer sellerId, Integer commissionRateId, CommissionRateDto commissionRateDto) {
    Seller seller = sellerRepository.findById(sellerId)
            .orElseThrow(() -> new DataNotFoundException(SELLER_NOT_FOUND.getMessage(), SELLER_NOT_FOUND.getErrorCode()));

    CommissionRate commissionRate = commissionRateRepository.findById(commissionRateId)
            .orElseThrow(() -> new DataNotFoundException(COMMISSION_RATE_NOT_FOUND.getMessage(), COMMISSION_RATE_NOT_FOUND.getErrorCode()));

    ProductType productType = productTypeRepository.findById(commissionRateDto.getProductTypeId())
            .orElseThrow(() -> new DataNotFoundException(PRODUCT_TYPE_NOT_FOUND.getMessage(), PRODUCT_TYPE_NOT_FOUND.getErrorCode()));

    commissionRate.setProductType(productType);
    commissionRateMapper.updateCommissionRate(commissionRateDto, commissionRate);
    commissionRateRepository.save(commissionRate);
}
```

**Miks laadime `seller` aga ei kasuta teda?**
Kontrollime et edasimüüja üldse olemas on — kui `sellerId` on vale, saab kasutaja kohe `404 SELLER_NOT_FOUND`, mitte segast viga.

**Miks `productType` laadime service-s, mitte mapperis?**
Mapper töötab ainult objektide vahel — ta ei pääse andmebaasile ligi. Service teab kõiki repositooriume, seega laadib `ProductType` ise ja seab selle entity peale enne mapperit.

**`commissionRate.setProductType(productType)` enne mapperit — miks järjekord loeb?**

```
1. Leia commissionRate andmebaasist
2. Sea productType käsitsi          ← enne mapperit!
3. Mapper uuendab ülejäänud väljad
4. Salvesta
```

Mapper ignoreerib `productType` välja (`ignore = true`) — seega kui seaksime selle pärast mapperit, oleks järjekord sama. Aga konventsiooni järgi seame kõik seotud entity-d enne mapperit — kood on selgem.

---

### 4. Controller

```java
@PutMapping("/seller/{sellerId}/commission-rates/{commissionRateId}")
@Operation(summary = "Uuenda edasimüüja teenustasu")
@ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "403", description = "Kasutajal pole õigust",
                content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "404", description = "Edasimüüjat ei leitud",
                content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "500", description = "Serveri viga",
                content = @Content(schema = @Schema(implementation = ApiError.class)))})
public void updateCommissionRate(@PathVariable Integer sellerId,
                                  @PathVariable Integer commissionRateId,
                                  @Valid @RequestBody CommissionRateDto commissionRateDto) {
    sellerCommissionRateService.updateCommissionRate(sellerId, commissionRateId, commissionRateDto);
}
```

---

### Veakoodid

| HTTP kood | Olukord | Sõnum | errorCode |
|-----------|---------|-------|-----------|
| 200 | Uuendamine õnnestus | — | — |
| 403 | Kasutaja pole Admin | "Teil pole selleks õigust" | 115 |
| 404 | Edasimüüjat ei leitud | "Edasimüüjat ei leitud" | 211 |
| 404 | Teenustasu ei leitud | "Teenustasu määra ei leitud" | 411 |
| 404 | Tootegruppi ei leitud | "Tootegruppi ei leitud" | 911 |

---

## Uued mõisted selles taskis

| Mõiste | Selgitus |
|--------|---------|
| **`@MappingTarget`** | Mapper uuendab olemasolevat entity't asemel et luua uus |
| **`@BeanMapping(IGNORE)`** | Null väärtused DTO-s jäetakse entity peale kirjutamata |
| **`String → LocalDate`** | Kuupäeva teisendus: `LocalDate.parse(string, DateTimeFormatter.ofPattern("dd.MM.yyyy"))` |
| **`LocalDate → String`** | Kuupäeva teisendus: `localDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))` |
| **Seotud entity laadimine** | Kui DTO sisaldab FK id-d (nt `productTypeId`), peab service laadima vastava entity andmebaasist |
| **`Optional.orElseThrow()`** | `findById()` tagastab `Optional` — kui tulemus puudub, visatakse exception automaatselt |

---

## Seos eelnevate taskidega

- **`seller-region-put.md`** — eelmine PUT task: üks väli, käsitsi `set`; siin: mitu välja, mapper teeb töö ära
- **DELETE kontakt / DELETE piirkond** — `DataNotFoundException` ja `orElseThrow()` muster — täpselt sama
- **`seller-region-put.md`** — `@Transactional`, `@Valid`, `void` tagastustüüp — sama muster

---

## Järgmised sammud

- **DELETE /api/seller/{sellerId}/commission-rates/{commissionRateId}** — teenustasu kustutamine (tiimikaaslane teeb)
- **POST /api/seller/{sellerId}/commission-rates** — teenustasu lisamine (tuleb hiljem)
