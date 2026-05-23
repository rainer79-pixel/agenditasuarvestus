# POST /api/seller/{sellerId}/regions — piirkonna lisamine

## Mis see on ja miks oluline?

See endpoint lisab edasimüüjale uue piirkonna. Admin valib `SellerSettingsRegionModal`-is dropdownist piirkonna (laetakse `GET /api/region`-iga) ja sisestab müügipunktide arvu — "Lisa" nupp kutsub selle endpointi.

**Päriselust analoogia:** Kujuta ette lepingut — edasimüüja leping sisaldab nimekirja piirkondadest kus ta tegutseb. Selle endpointiga lisad lepingusse uue rea: "edasimüüja X tegutseb piirkonnas Y, tal on Z müügipunkti." Enne lisamist kontrollid: kas sul on õigus seda teha? Kas piirkond on tegelikkuses olemas? Kas see rida pole juba lepingus?

---

## Mis on selles taskis uut võrreldes DELETE-ga?

| Asi | DELETE | POST |
|-----|--------|------|
| **Suund** | Kustutab olemasoleva rea | Loob uue rea |
| **DTO** | Puudub — ainult URL parameetrid | `SellerRegionDto` request body-s |
| **Mapper** | Pole vaja | Vaja `toSellerRegion` (DTO → entity) |
| **FK objektid** | Pole vaja | Vaja `Seller` ja `Region` objektid käsitsi seada |
| **Duplikaadi kontroll** | Pole vaja | Vaja — sama kombinatsioon ei tohi kaks korda olla |
| **Exception tüüp** | `DataNotFoundException` (404) | Lisaks `ConflictException` (409) |

---

## Andmevoog — suur pilt

```
BROWSER (Admin vajutab "Lisa" nuppu modaalil)
    ↓
FRONTEND (saadab POST päringu koos JSON body-ga)
    ↓
AXIOS → POST /api/seller/1/regions?userId=1
        Body: { "regionId": 5, "salesPointCount": 12 }
    ↓  ← siit algab backend
SellerRegionController.addSellerRegion()
    ↓
SellerRegionService.addSellerRegion()
    ↓
  validateUserIsAdmin()      ← kas userId on Admin?
  getSellerById()            ← kas seller olemas? tagastab Seller objekti
  getRegionById()            ← kas region olemas? tagastab Region objekti
  validateSellerRegionNotDuplicate()  ← kas kombinatsioon juba eksisteerib?
  createAndSaveSellerRegion()         ← loo entity ja salvesta
    ↓
ANDMEBAAS (INSERT INTO seller_region ...)
    ↓
CONTROLLER (tagastab 201 Created)
    ↓
FRONTEND (uuendab piirkondade nimekirja)
```

---

## Andmed tulevad KOLMEST kohast korraga

```
POST /api/seller/{sellerId}/regions?userId=1
              ↑                      ↑
         @PathVariable           @RequestParam
         sellerId = 7            userId = 1

Body: { "regionId": 5, "salesPointCount": 12 }
       ↑                ↑
       @RequestBody SellerRegionDto
```

| Andmeosa | Kust tuleb | Annotatsiooon |
|----------|-----------|---------------|
| `sellerId` | URL tee `/seller/7/regions` | `@PathVariable` |
| `userId` | URL parameeter `?userId=1` | `@RequestParam` |
| `regionId` | JSON body | `@RequestBody` |
| `salesPointCount` | JSON body | `@RequestBody` |

---

## BACKEND

### 1. ErrorResponse — uus veakood

```java
// Region
SELLER_REGION_NOT_FOUND("Piirkonda ei leitud", 321),
SELLER_REGION_ALREADY_EXISTS("See piirkond on juba lisatud", 322),  // ← uus
```

**Miks 409 Conflict (mitte 404)?**
- 404 = "ei leidnud" — otsid midagi mis pole olemas
- 409 = "konflikt" — proovid teha midagi mis läheb vastuollu olemasoleva andmega

Duplikaat on konflikt — andmebaasis ON juba selline rida, seega 409 on semantiliselt õige.

---

### 2. SellerRegionDto — lisasime regionId välja

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SellerRegionDto {
    @NotNull
    private Integer regionId;       // ← uus väli

    @NotNull
    @Min(0)
    private Integer salesPointCount;
}
```

**Miks `regionId` on DTO-s aga `sellerId` mitte?**
`sellerId` tuleb URL-ist (`@PathVariable`) — ta identifitseerib ressurssi. `regionId` on "lisatav andmeosa" — mis piirkond valiti dropdownist. Semantiliselt kuulub ta body-sse.

---

### 3. SellerRegionRepository — duplikaadi kontroll

```java
@Query("select (count(sr) > 0) from SellerRegion sr where sr.seller.id = :sellerId and sr.region.id = :regionId")
boolean sellerRegionExistsBy(Integer sellerId, Integer regionId);
```

**Miks `sr.seller.id` ja mitte `sr.sellerId`?**
`SellerRegion` entity-l pole `sellerId` välja — on `Seller seller` objekt. JPA navigeerib läbi objektisuhte:

```
SellerRegion → seller (Seller objekt) → id (Integer)
              sr.seller.id
```

---

### 4. SellerRegionMapper — lisasime DTO → entity meetodi

```java
// Lugemine (oli juba olemas)
@Mapping(source = "id", target = "regionId")
@Mapping(source = "region.regionName", target = "regionName")
@Mapping(source = "salesPointCount", target = "salesPointCount")
SellerRegionResponseDto toSellerRegionResponseDto(SellerRegion sellerRegion);

List<SellerRegionResponseDto> toSellerRegionResponseDtos(List<SellerRegion> sellerRegions);

// Kirjutamine (uus)
@Mapping(ignore = true, target = "id")
@Mapping(ignore = true, target = "seller")
@Mapping(ignore = true, target = "region")
@Mapping(source = "salesPointCount", target = "salesPointCount")
SellerRegion toSellerRegion(SellerRegionDto sellerRegionDto);
```

**Miks `seller` ja `region` on `ignore = true`?**

```
DTO-s:     regionId = 5 (ainult number)
Entity-l:  region = Region { id=5, regionName="Tallinn", ... } (terve objekt)
```

Mapper ei suuda numbrist objekti teha — selleks on vaja andmebaasipäringut. See on service töö. Mapper teeb ainult `salesPointCount` ülekande, ülejäänu seame käsitsi.

---

### 5. SellerRegionService — addSellerRegion ja abimeetodid

```java
@Transactional
public void addSellerRegion(Integer userId, Integer sellerId, SellerRegionDto sellerRegionDto) {
    validateUserIsAdmin(userId);                                    // 1. õigused
    Seller seller = getSellerById(sellerId);                        // 2. seller olemas?
    Region region = getRegionById(sellerRegionDto.getRegionId());   // 3. region olemas?
    validateSellerRegionNotDuplicate(sellerId, region.getId());     // 4. duplikaat?
    createAndSaveSellerRegion(seller, region, sellerRegionDto);     // 5. salvesta
}
```

**Miks `getSellerById` mitte `validateSellerExists`?**

```java
// validateSellerExists — ainult kontrollib, ei tagasta midagi
private void validateSellerExists(Integer sellerId) {
    if (!sellerRepository.existsById(sellerId)) { throw ... }
}

// getSellerById — kontrollib JA tagastab objekti
private Seller getSellerById(Integer sellerId) {
    return sellerRepository.findById(sellerId)
            .orElseThrow(() -> new DataNotFoundException(...));
}
```

POST puhul vajame hiljem `seller` objekti (`sellerRegion.setSeller(seller)`), seega kasutame `getSellerById`.

**Privaatmeetodid:**

```java
private void validateSellerRegionNotDuplicate(Integer sellerId, Integer regionId) {
    if (sellerRegionRepository.sellerRegionExistsBy(sellerId, regionId)) {
        throw new ConflictException(SELLER_REGION_ALREADY_EXISTS.getMessage(),
                                    SELLER_REGION_ALREADY_EXISTS.getErrorCode());
    }
}

private void createAndSaveSellerRegion(Seller seller, Region region, SellerRegionDto dto) {
    SellerRegion sellerRegion = sellerRegionMapper.toSellerRegion(dto); // salesPointCount
    sellerRegion.setSeller(seller);   // ← FK objekt käsitsi
    sellerRegion.setRegion(region);   // ← FK objekt käsitsi
    sellerRegionRepository.save(sellerRegion);
}
```

**`setSeller` ja `setRegion` — miks käsitsi?**
Mapper ei saa ID-st objekti teha (pole andmebaasile ligipääsu). Service on see kes teab mõlemat — nii DTO andmeid kui repositorytest laetud objekte.

---

### 6. SellerRegionController — @PostMapping

```java
@PostMapping("/seller/{sellerId}/regions")
@Operation(summary = "Lisa edasimüüjale piirkond")
@ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Piirkond lisatud"),
        @ApiResponse(responseCode = "400", description = "Vigased andmed", ...),
        @ApiResponse(responseCode = "403", description = "Kasutajal pole õigust", ...),
        @ApiResponse(responseCode = "404", description = "Edasimüüjat või piirkonda ei leitud", ...),
        @ApiResponse(responseCode = "409", description = "Piirkond on juba lisatud", ...),
        @ApiResponse(responseCode = "500", description = "Serveri viga", ...)})
public void addSellerRegion(@RequestParam Integer userId,
                            @PathVariable Integer sellerId,
                            @Valid @RequestBody SellerRegionDto sellerRegionDto) {
    sellerRegionService.addSellerRegion(userId, sellerId, sellerRegionDto);
}
```

**Meetodite järjekord kontrolleris:** GET → POST → PUT → DELETE. See on REST konventsioon — loeb enne kui muudab.

---

## Kokkuvõte

| Samm | Fail | Mida tegime |
|------|------|-------------|
| 1 | `ErrorResponse.java` | Lisasime `SELLER_REGION_ALREADY_EXISTS` (322) |
| 2 | `SellerRegionDto.java` | Lisasime `regionId` välja |
| 3 | `SellerRegionRepository.java` | Lisasime `sellerRegionExistsBy()` duplikaadi kontrolliks |
| 4 | `SellerRegionMapper.java` | Lisasime `toSellerRegion()` (DTO → entity) |
| 5 | `SellerRegionService.java` | Lisasime `addSellerRegion()` + 4 abimeetodit |
| 6 | `SellerRegionController.java` | Lisasime `@PostMapping` |

**Peamine õppetund:** POST endpoint on keerulisem kui DELETE — andmed tulevad mitmest kohast, FK objektid tuleb käsitsi seada, duplikaati tuleb kontrollida. Mapper teeb lihtsa osa (primitiivväljad), service teeb keerulise osa (objektisuhted + valideerimine).

---

## Järgmised sammud

- `POST /api/seller/{sellerId}/contacts` — kontakti lisamine (sama muster)
- `POST /api/seller/{sellerId}/commission-rates` — teenustasu lisamine (sama muster, keerulisem DTO)