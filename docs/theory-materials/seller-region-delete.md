# DELETE piirkond — algajatele

## Mis see on ja miks oluline?

See endpoint kustutab edasimüüja piirkonna seose (`seller_region` rea).
Ainult Admin saab kustutada — tavalised kasutajad saavad piirkondi ainult vaadata.

**Päriselust analoogia:** Kujuta ette et edasimüüjal on lepingus kirjas millistes piirkondades ta tegutseb. Kui ta lõpetab tegevuse mõnes piirkonnas, tõmbad selle rea lepingust maha — aga piirkond ise (nt "Harjumaa") jääb alles, sest teised edasimüüjad kasutavad seda ka.

---

## Mis on selles taskis uut võrreldes kontaktide kustutamisega?

See task on lihtsam kui kontaktide kustutamine — üks oluline erinevus:

| Asi | Kontaktide DELETE | Piirkondade DELETE |
|-----|------------------|-------------------|
| **FK constraint** | ✅ Oli probleem — pidi enne rollid kustutama | ❌ Pole probleemi — `seller_region` tabelil pole alamtabeleid |
| **Repository meetod** | Custom JPQL `@Modifying` + `@Transactional` | `deleteById()` — tuleb `JpaRepository`-st automaatselt |
| **Valideerimise muster** | Sama | Sama |
| **`@RequestParam userId`** | Sama | Sama |

**Põhiidee:** Kui kustutataval real pole teisi tabeleid mis sellele viitaksid, saab kustutada otse `deleteById()`-ga — pole vaja eelnevat puhastust.

---

## Andmevoog — suur pilt

```
BROWSER (Admin vajutab "Kustuta" nuppu piirkonna real)
    ↓
FRONTEND (saadab DELETE päringu)
    ↓
AXIOS → DELETE /api/seller/3/regions/5?userId=1
    ↓  ← siit algab backend
CONTROLLER (võtab userId, sellerId, regionId kätte)
    ↓
SERVICE (kontrollib rolli → seller olemas? → piirkond olemas? → kustutab)
    ↓
REPOSITORY (deleteById — üks DELETE lause)
    ↓
ANDMEBAAS (kustutab seller_region rea, region tabel jääb puutumata)
    ↓
CONTROLLER (tagastab 200 OK, tühi vastus)
    ↓
FRONTEND (eemaldab piirkonna nimekirjast)
```

---

## BACKEND

### 1. Miks ei pea siin midagi eelnevalt kustutama?

Kontaktide puhul tuli enne rollid kustutada, sest `seller_role` tabel viitas `seller_contact`-ile.
Piirkondade puhul sellist sõltuvust pole:

```
seller_region tabel:
id  | seller_id | region_id | sales_point_count
----+-----------+-----------+------------------
 1  |     1     |     1     |        2
 5  |     3     |     1     |        0
```

`seller_region` real ei viidata kusagilt mujalt → saab kustutada otse.

### 2. ErrorResponse — uus veakood

```java
// Region
SELLER_REGION_NOT_FOUND("Piirkonda ei leitud", 321),
```

Veakoodid on grupeeritud numbrite järgi (211 = seller, 311 = contact, 321 = region).
Uus kood lisatakse alati gruppi kus see loogiliselt kuulub.

### 3. SellerRegionService — kogu loogika

```java
@Transactional
public void deleteSellerRegion(Integer userId, Integer sellerId, Integer regionId) {
    validateUserIsAdmin(userId);          // 1. kas kasutajal on õigus?
    validateSellerExists(sellerId);       // 2. kas edasimüüja on olemas?
    validateSellerRegionExists(regionId); // 3. kas piirkonna seos on olemas?
    sellerRegionRepository.deleteById(regionId); // 4. kustuta
}
```

**Miks see järjekord?**
Kõigepealt kontrollime õigusi (403), siis andmete olemasolu (404).
Oleks imelik kui kasutaja saaks 404 vea asja kohta millele tal nagunii pole ligipääsu.

---

### 4. Kolm valideerimise abimeetodit

```java
private void validateUserIsAdmin(Integer userId) {
    // findById tagastab Optional — orElseThrow viskab erindi kui tühi
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new DataNotFoundException(
                USER_NOT_FOUND.getMessage(), USER_NOT_FOUND.getErrorCode()));
    // "A" = Admin, kõik muu = pole õigust
    if (!"A".equals(user.getUserRole())) {
        throw new ForbiddenException(ACCESS_DENIED.getMessage(), ACCESS_DENIED.getErrorCode());
    }
}

private void validateSellerExists(Integer sellerId) {
    // existsById = SELECT count(*) > 0 — kiirem kui findById, ei lae objekti mällu
    if (!sellerRepository.existsById(sellerId)) {
        throw new DataNotFoundException(SELLER_NOT_FOUND.getMessage(), SELLER_NOT_FOUND.getErrorCode());
    }
}

private void validateSellerRegionExists(Integer regionId) {
    // regionId on seller_region.id — mitte region.id!
    if (!sellerRegionRepository.existsById(regionId)) {
        throw new DataNotFoundException(SELLER_REGION_NOT_FOUND.getMessage(), SELLER_REGION_NOT_FOUND.getErrorCode());
    }
}
```

**`existsById` vs `findById` — millal kumba?**

| | `findById` | `existsById` |
|--|-----------|-------------|
| **Tagastab** | `Optional<Entity>` — kogu objekt | `boolean` — ainult true/false |
| **Kasuta kui** | vajad hiljem objekti andmeid | ainult kontrollid kas olemas |
| **Kiirus** | loeb kõik veerud | loeb ainult ID |

---

### 5. SellerRegionController — endpoint

```java
// DELETE meetod, URL sisaldab kahte muutujat
@DeleteMapping("/seller/{sellerId}/regions/{regionId}")
@Operation(summary = "Kustuta edasimüüja piirkond")
@ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "403", description = "Pole õigust",
                content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "404", description = "Edasimüüjat või piirkonda ei leitud",
                content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "500", description = "Serveri viga",
                content = @Content(schema = @Schema(implementation = ApiError.class)))})
// void — DELETE ei tagasta midagi
public void deleteSellerRegion(
        @RequestParam Integer userId,     // ?userId=1 — URL-i lõpus, pole ressursi osa
        @PathVariable Integer sellerId,   // /seller/{sellerId} — ressursi osa
        @PathVariable Integer regionId) { // /regions/{regionId} — ressursi osa
    sellerRegionService.deleteSellerRegion(userId, sellerId, regionId);
}
```

**Miks `userId` on `@RequestParam` mitte `@PathVariable`?**
`sellerId` ja `regionId` identifitseerivad ressursi mida kustutame — need kuuluvad URL-i teesse.
`userId` on ainult õiguste kontrollimiseks — ta ei ole ressursi osa, seega läheb päringuparameetriks.

---

## Kokkuvõte

| Samm | Fail | Mida tegime |
|------|------|-------------|
| 1 | `ErrorResponse.java` | Lisasime `SELLER_REGION_NOT_FOUND` veakoodi (321) |
| 2 | `SellerRegionService.java` | Lisasime `deleteSellerRegion()` + 3 valideerimise abimeetodit |
| 3 | `SellerRegionController.java` | Lisasime `@DeleteMapping` endpoint-i |

**Peamine õppetund:** Kui kustutataval tabelil pole alamtabeleid, kasuta `deleteById()` otse — pole vaja eelnevat puhastust. See erineb kontaktide kustutamisest kus tuli esmalt `seller_role` read eemaldada.

---

## Järgmised sammud

- `PUT /api/seller/{sellerId}/regions/{regionId}` — piirkonna müügipunktide arvu uuendamine
- `POST /api/seller/{sellerId}/regions` — piirkonna lisamine