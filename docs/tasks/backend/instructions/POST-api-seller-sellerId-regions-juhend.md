# Juhend: POST /api/seller/{sellerId}/regions

**Taski fail:** `POST-api-seller-sellerId-regions.md`
**Kontroller:** `SellerRegionController.java`
**Implementeerimise voog:** RestController → Service → Repository (kontrollid) → Mapper → Repository (salvestus) → RestController

---

## Sissejuhatus

See endpoint lisab edasimüüjale uue piirkonna. Admin valib `SellerSettingsRegionModal`-is dropdownist piirkonna ja sisestab müügipunktide arvu — "Lisa" nupp kutsub selle endpointi. Kõik vajalikud klassid on juba olemas, vaja on lisada uusi meetodeid olemasolevatesse failidesse ja täiendada `SellerRegionDto`-t.

---

## Samm 1 — ErrorResponse.java (lisa puuduv veakood)

### Mida teha?

Enne koodi kirjutamist lisa puuduv veakood `ErrorResponse` enum-i. See on vajalik, et service saaks duplikaadi vea puhul õige vastuse tagastada.

Ava fail: `backend/src/main/java/ee/valiit/etas/infrastructure/error/ErrorResponse.java`

Leia `// Region` sektsioon. Praegu on seal ainult `SELLER_REGION_NOT_FOUND` (kood 321).

Lisa sinna järgmine rida:

```
SELLER_REGION_ALREADY_EXISTS("See piirkond on juba lisatud", 322),
```

> **Mõtle:** Miks on veakoodid grupeeritud 3xx, 4xx jne kaupa? See teeb veakoodide haldamise lihtsamaks — kõik piirkonnaga seotud vead on 32x all.

---

## Samm 2 — SellerRegionDto.java (lisa regionId väli)

### Mida teha?

`SellerRegionDto`-l on praegu ainult `salesPointCount`. POST päring vajab ka `regionId`-d, et teada, millisele piirkonnale viidatakse.

Ava fail: `backend/src/main/java/ee/valiit/etas/controller/seller/dto/SellerRegionDto.java`

Lisa uus väli `salesPointCount` ette (sest logiliselt: esmalt identifitseerime piirkonna, siis müügipunktide arv):

```java
@NotNull
private Integer regionId;
```

> **Mõtle:** Miks on `@NotNull` vajalik? Ilma selleta saaks frontend saata päringu ilma `regionId`-ta — backend aktsepteeriks seda ja tekiks `NullPointerException`.

---

## Samm 3 — SellerRegionRepository.java (lisa duplikaadi kontroll)

### Mida teha?

Enne salvestamist peab service kontrollima, kas sama `seller_id` + `region_id` kombinatsioon juba eksisteerib. Selleks on vaja uut repository meetodit.

Ava fail: `backend/src/main/java/ee/valiit/etas/persistence/sellerregion/SellerRegionRepository.java`

**Küsi endalt:** Mis tüüpi meetodit on vaja? Ei ole vaja tagastada ridu — vaid kontrollida, kas rida eksisteerib.

Kasuta JPA Buddy abi:
1. Paremklõps repository klassis → JPA Buddy → Query
2. Vali **Exists** (mitte Find)
3. Lisa kaks tingimust: `seller.id` ja `region.id`
4. Advanced → **Named parameters**

Tulemus peaks sarnanema:

```java
@Query("...")
boolean sellerRegionExistsBy(Integer sellerId, Integer regionId);
```

> **Mõtle:** Miks `seller.id` ja `region.id` (mitte lihtsalt `sellerId`)? Sest `SellerRegion` entity-l on `seller` objekt (mitte `Integer sellerId` väli otse). JPA navigeerib seose kaudu.

---

## Samm 4 — SellerRegionMapper.java (lisa entity loomise meetod)

### Mida teha?

POST puhul on vaja teisendada DTO → entity, et andmebaasi salvestada. Praegu on mapperil ainult lugemissuuna meetodid (entity → DTO).

Ava fail: `backend/src/main/java/ee/valiit/etas/persistence/sellerregion/SellerRegionMapper.java`

Lisa uus meetod pärast olemasolevaid:

```java
@Mapping(ignore = true, target = "id")
@Mapping(ignore = true, target = "seller")
@Mapping(ignore = true, target = "region")
@Mapping(source = "salesPointCount", target = "salesPointCount")
SellerRegion toSellerRegion(SellerRegionDto sellerRegionDto);
```

> **Miks `seller` ja `region` on `ignore = true`?**
> DTO-s on ainult `regionId` (Integer), aga entity vajab tervet `Region` objekti. Mapper ei suuda ID-st objekti teha — seda teeme service-s käsitsi (`sellerRegion.setRegion(region)`).

---

## Samm 5 — SellerRegionService.java (lisa addSellerRegion meetod)

### Mida teha?

Service on "aju" — siia läheb kogu loogika: rollide kontroll, olemasolu kontrollid, duplikaadi kontroll, entity loomine ja salvestamine.

Ava fail: `backend/src/main/java/ee/valiit/etas/service/SellerRegionService.java`

### 5a. Lisa RegionRepository süstimine

Service vajab `Region` objekti leidmiseks `RegionRepository`-t. Vaata olemasolevaid välju — praegu on `sellerRegionRepository`, `sellerRegionMapper`, `userRepository`, `sellerRepository`. Lisa juurde:

```java
private final RegionRepository regionRepository;
```

> **IntelliJ vihje:** Kirjuta `private final Reg` — IntelliJ pakub automaatselt `RegionRepository`. Vajuta Tab.

### 5b. Lisa avalik meetod

Lisa uus `@Transactional` avalik meetod. Vaata olemasolevate meetodite mustrit (nt `updateSellerRegion`) — struktuur on sarnane.

Avalik meetod peaks olema lühike (3–7 rida) ja delegeerima privaatsetele meetoditele:

```java
@Transactional
public void addSellerRegion(Integer userId, Integer sellerId, SellerRegionDto sellerRegionDto) {
    // 1. Kontrolli roll
    // 2. Hangi seller objekt (ja kontrolli olemasolu)
    // 3. Hangi region objekt (ja kontrolli olemasolu)
    // 4. Kontrolli duplikaat
    // 5. Loo ja salvesta entity
}
```

> **Mõtle:** Miks `getSellerById` asemel `validateSellerExists` ei sobi siin? Sest `validateSellerExists` kasutab ainult `existsById` — see ei tagasta `Seller` objekti, mida entity-le seadmiseks vajame.

### 5c. Lisa privaatmeetodid

**Selleri leidmine:**
Lisa privaatmeetod, mis otsib `Seller` objekti ID järgi ja viskab erandi kui ei leidu. Vaata `getSellerRegion(regionId)` meetodi mustrit — sama loogika, aga `SellerRepository`-ga.

```
private Seller getSellerById(Integer sellerId) { ... }
```

**Piirkonna leidmine:**
Sama muster `RegionRepository`-ga. Kasuta `SELLER_REGION_NOT_FOUND` veakoodi kui piirkond ei leidu.

```
private Region getRegionById(Integer regionId) { ... }
```

**Duplikaadi kontroll:**
Kasuta Samm 3-s loodud repository meetodit. Viska `ConflictException` koos `SELLER_REGION_ALREADY_EXISTS` veakoodiga kui kombinatsioon juba eksisteerib.

```
private void validateSellerRegionNotDuplicate(Integer sellerId, Integer regionId) { ... }
```

**Entity loomine ja salvestamine:**
Kasuta mapperit DTO → entity teisendamiseks. Seejärel sea käsitsi `seller` ja `region` objektid. Salvesta.

```
private void createAndSaveSellerRegion(Seller seller, Region region, SellerRegionDto dto) { ... }
```

> **Meetodite järjekord:** avalik meetod üleval, privaatsed all — järjesta ka väljakutsumise hierarhia järgi.

---

## Samm 6 — SellerRegionController.java (lisa @PostMapping)

### Mida teha?

Ava fail: `backend/src/main/java/ee/valiit/etas/controller/seller/SellerRegionController.java`

Vaata olemasolevaid `@PutMapping` ja `@DeleteMapping` meetodeid — `@PostMapping` on sarnane struktuuriga.

Uus meetod vajab:
- `@PostMapping("/seller/{sellerId}/regions")`
- `@Operation` summary
- `@ApiResponses` kõigi veaolukordade jaoks: 201, 400, 403, 404, 409, 500
- `@RequestParam Integer userId`
- `@PathVariable Integer sellerId`
- `@Valid @RequestBody SellerRegionDto sellerRegionDto`
- HTTP 201 tagastamine — kasuta `ResponseEntity.status(HttpStatus.CREATED).build()`

> **Mõtle:** Miks 201 Created (mitte 200 OK)? 200 tähendab "päring õnnestus", 201 tähendab "uus ressurss loodi". POST-i puhul on 201 semantiliselt õigem.

`@ApiResponses` näide 201 puhul:

```java
@ApiResponse(responseCode = "201", description = "Piirkond lisatud")
```

---

## Kokkuvõte ja kontrollnimekiri

Enne kui pead koodi valmis, kontrolli läbi:

- [ ] `ErrorResponse.java` — `SELLER_REGION_ALREADY_EXISTS` (kood 322) on olemas
- [ ] `SellerRegionDto.java` — `regionId` väli `@NotNull`-iga lisatud
- [ ] `SellerRegionMapper.java` — `toSellerRegion(SellerRegionDto)` meetod lisatud
- [ ] `SellerRegionRepository.java` — `sellerRegionExistsBy(sellerId, regionId)` meetod lisatud
- [ ] `SellerRegionService.java` — `RegionRepository` süstitud, `addSellerRegion` + 4 privaatmeetodit lisatud
- [ ] `SellerRegionController.java` — `@PostMapping` meetod koos kõigi annotatsioonidega lisatud
- [ ] Kood kompileerub ja Swagger UI kaudu on endpoint nähtav

---

> **Järgmine samm:** Testi Swagger UI kaudu (`http://localhost:8080/swagger-ui/index.html`)
> Kontrolli kõik neli veaolukorda: 403 (vale userId), 404 (vale sellerId), 404 (vale regionId), 409 (sama piirkond uuesti).