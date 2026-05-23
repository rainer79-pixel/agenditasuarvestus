# PUT piirkond — algajatele

## Mis see on ja miks oluline?

See endpoint uuendab edasimüüja piirkonna müügipunktide arvu.
Kasutaja näeb `SellerSettingsView`-s piirkondade nimekirja — iga real on pliiatsikoon mis avab muutmisrežiimi.
Ainult üks väli muutub: `salesPointCount`. Piirkond ise (`region_id`) jääb samaks.

**Päriselust analoogia:** Kujuta ette tabelit kus on kirjas mitu müügipunkti sul igas linnas on. Kui avad Tallinna rea ja muudad arvu 2-lt 5-le — muutub ainult see number. Tallinn jääb Tallinnaks.

---

## Mis on selles taskis uut võrreldes bank40 projektiga?

Bank40-s tegime samuti PUT päringuid — aga seal kasutas mapper `@BeanMapping` entity uuendamiseks. Siin on üks väli ja kasutame lihtsamat lähenemist:

| Uus asi | Kus näed | Mida tähendab |
|---------|---------|---------------|
| **`@Min(0)`** | `SellerRegionDto` | Bean Validation annotatsioon — keelab negatiivse arvu, annab 400 automaatselt |
| **`getEntity` vs `validateExists`** | `SellerRegionService` | Kui pead entity't edasi kasutama → tagasta see; kui ainult kontrollid olemasolu → `void` |
| **`save()` uuendab, mitte ainult ei lisa** | `SellerRegionService` | JPA `save()` on universaalne — kui entity'l on `id` olemas, teeb UPDATE; ilma `id`-ta teeb INSERT |

Kõik muu (`@Transactional`, `@Valid`, `@RequestParam` userId, `ForbiddenException`, `DataNotFoundException`) on tuttav eelmistest taskidest.

---

## Andmevoog — suur pilt

```
BROWSER (Admin muudab müügipunktide arvu ja vajutab Salvesta)
    ↓
FRONTEND (SellerRegionService.js saadab PUT päringu)
    ↓
AXIOS → PUT /api/seller/3/regions/5?userId=1
        Request body: {"salesPointCount": 5}
    ↓  ← siit algab backend
CONTROLLER (@Valid kontrollib et salesPointCount >= 0)
    ↓
SERVICE (Admin? → seller olemas? → sellerRegion olemas? → uuenda)
    ↓
REPOSITORY (findById → save)
    ↓
ANDMEBAAS (UPDATE seller_region SET sales_point_count=5 WHERE id=5)
    ↓
CONTROLLER (tagastab 200 OK, tühi vastus)
    ↓
FRONTEND (uuendab arvu ekraanil)
    ↓
BROWSER (kasutaja näeb uut väärtust)
```

---

## BACKEND

### 1. `SellerRegionDto.java` — sisend (request body)

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SellerRegionDto {

    @NotNull
    @Min(0)
    private Integer salesPointCount;
}
```

**`@NotNull`** — väli ei tohi olla `null`. Kui frontend saadab `{}` ilma `salesPointCount`-ita, annab 400.

**`@Min(0)`** — väärtus peab olema vähemalt 0. Kui saadad `-1`, annab 400:
```json
{"message": "salesPointCount: must be greater than or equal to 0", "errorCode": 777}
```

**Miks `@Min(0)` DTO-l, mitte servicel?** Bean Validation kontrollib sisendi piirid automaatselt enne kui päring jõuab serviceini. Servicel pole vaja seda käsitsi kontrollida — vähem koodi, sama tulemus.

**Miks `@Valid` on controlleris vajalik?**

```java
public void updateSellerRegion(..., @Valid @RequestBody SellerRegionDto sellerRegionDto)
```

`@Valid` on "käiviti" — ilma selleta ignoreerib Spring `@NotNull` ja `@Min(0)` täielikult. `@Valid` ütleb: "Enne kui annad selle DTO servicele, kontrolli kõik validatsiooniannotatsioonid läbi."

---

### 2. Service — `getSellerRegion` vs `validateSellerExists`

```java
private void validateSellerExists(Integer sellerId) {
    if (!sellerRepository.existsById(sellerId)) {
        throw new DataNotFoundException(...);
    }
}

private SellerRegion getSellerRegion(Integer regionId) {
    return sellerRegionRepository.findById(regionId)
            .orElseThrow(() -> new DataNotFoundException(...));
}
```

**Miks kaks erinevat mustrit?**

| | `validateSellerExists` | `getSellerRegion` |
|---|---|---|
| **Tagastab** | `void` | `SellerRegion` objekt |
| **Kasutab** | `existsById()` — ainult kontrollib | `findById()` — laadib terve objekti |
| **Miks** | Seller andmeid pole vaja — tahame ainult teada "kas on olemas?" | SellerRegion objekt on vaja kätte saada, et selle `salesPointCount` välja muuta |

**Päriselust analoogia:** `validateSellerExists` on nagu küsida uksel "Kas see isik on nimekirjas?" — jah/ei piisab. `getSellerRegion` on nagu paluda "Too mulle see dokument" — sul on dokument füüsiliselt käes ja saad seda muuta.

---

### 3. Service — `save()` teeb UPDATE, mitte INSERT

```java
@Transactional
public void updateSellerRegion(Integer userId, Integer sellerId, Integer regionId, SellerRegionDto sellerRegionDto) {
    validateUserIsAdmin(userId);
    validateSellerExists(sellerId);
    SellerRegion sellerRegion = getSellerRegion(regionId);   // lae entity andmebaasist
    sellerRegion.setSalesPointCount(sellerRegionDto.getSalesPointCount());  // muuda väli
    sellerRegionRepository.save(sellerRegion);               // salvesta muudatus
}
```

**Miks `save()` ei loo uut rida?**

JPA `save()` käitub erinevalt sõltuvalt sellest kas entity'l on `id` olemas:

```
save(entity kus id = null)  → INSERT — loo uus rida
save(entity kus id = 5)     → UPDATE — uuenda olemasolevat rida
```

`getSellerRegion` laadis entity andmebaasist — sellel on `id = 5` juba olemas. Seega `save()` teeb UPDATE.

**`@Transactional` roll siin:** Tagab et `findById` ja `save` käivad ühe andmebaasitehingu sees. Kui `save` ebaõnnestub, keeratakse kogu operatsioon tagasi.

---

### 4. Controller

```java
@PutMapping("/seller/{sellerId}/regions/{regionId}")
@Operation(summary = "Uuenda piirkonna müügipunktide arv")
@ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Vigased andmed", ...),
        @ApiResponse(responseCode = "403", description = "Pole õigust", ...),
        @ApiResponse(responseCode = "404", description = "Edasimüüjat või piirkonda ei leitud", ...),
        @ApiResponse(responseCode = "500", description = "Serveri viga", ...)})
public void updateSellerRegion(@RequestParam Integer userId,
                               @PathVariable Integer sellerId,
                               @PathVariable Integer regionId,
                               @Valid @RequestBody SellerRegionDto sellerRegionDto) {
    sellerRegionService.updateSellerRegion(userId, sellerId, regionId, sellerRegionDto);
}
```

- `@PutMapping` — HTTP PUT meetod, kasutatakse olemasoleva ressursi uuendamiseks
- `@Valid @RequestBody` — loe JSON body + käivita validatsioon
- `void` tagastustüüp — vastuses pole keha, ainult HTTP 200

---

### Veakoodid

| HTTP kood | Olukord | Sõnum | errorCode |
|-----------|---------|-------|-----------|
| 200 | Uuendamine õnnestus | — | — |
| 400 | `salesPointCount` on negatiivne või puudub | "salesPointCount: must be ≥ 0" | 777 |
| 403 | Kasutaja pole Admin | "Teil pole selleks õigust" | 115 |
| 404 | Edasimüüjat ei leitud | "Edasimüüjat ei leitud" | 211 |
| 404 | Piirkonda ei leitud | "Piirkonda ei leitud" | 321 |

---

## Uued mõisted selles taskis

| Mõiste | Selgitus |
|--------|---------|
| **`@Min(0)`** | Bean Validation annotatsioon — keelab väärtuse mis on väiksem kui 0; annab automaatselt 400 |
| **`@Valid`** | Controlleri annotatsioon mis käivitab DTO validatsiooniannotatsioonide kontrollimise |
| **Bean Validation** | Java standard (annotatsioonid `@NotNull`, `@Min`, `@Max`, `@Size` jne) sisendi automaatseks kontrollimiseks |
| **`save()` UPDATE vs INSERT** | JPA `save()` teeb UPDATE kui entity'l on `id` olemas, INSERT kui `id` on `null` |
| **`existsById()` vs `findById()`** | `existsById` — odavam, tagastab ainult `boolean`; `findById` — laadib terve objekti mällu |

---

## Seos eelnevate taskidega

- **Task-03 (edasimüüjad):** `@Valid @RequestBody` — sama muster POST ja PUT päringutes
- **Task-03 (edasimüüjad):** `@BeanMapping(IGNORE)` mapper PUT jaoks — alternatiivne lähenemine kui on palju välju; siin kasutasime lihtsamat otseteed kuna väli on ainult üks
- **DELETE kontakt:** `validateUserIsAdmin`, `DataNotFoundException` — täpselt sama muster

---

## Järgmised sammud

- **POST /api/seller/{sellerId}/regions** — piirkonna lisamine (sama domeen, POST suund)
- **DELETE /api/seller/{sellerId}/regions/{regionId}** — piirkonna kustutamine (tiimikaaslane teeb)