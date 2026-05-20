# DELETE teenustasu määr — algajatele

## Mis see on ja miks oluline?

See endpoint kustutab edasimüüja teenustasu määra — AGA ainult siis, kui see pole veel kasutuses.
Kui teenustasu alusel on juba arvutusi tehtud (`commission_calculation` tabelis on viitavad read),
kustutamine blokeeritakse — arvutuste ajalugu peab säilima auditeerimiseks.

**Päriselust analoogia:** Kujuta ette et oled palgaarvestaja. Sul on palgatabel kus on kirjas töötajate tunnitasud. Kui töötaja pole ühtegi tundi töötanud, saad tunnitasu kirje kustutada. Aga kui töötajale on juba palka makstud selle tunnitasu alusel, ei saa kirjet kustutada — muidu kaob ajaloo andmed ja auditi korral ei saa selgitada miks just nii palju maksti.

---

## Mis on selles taskis uut võrreldes eelmiste kustutamistega?

Oleme juba teinud kaks erinevat DELETE-i — siin tuleb kolmas, kõige erinev:

| Kustutamine | Lähenemine | Miks |
|-------------|-----------|------|
| **Kontaktid** | Kustuta laps enne vanemat (rollid → kontakt) | FK constraint — andmebaas ei luba kustutada kui teisest tabelist viidatakse |
| **Piirkonnad** | Kustuta otse `deleteById()` | `seller_region` tabelil pole alamtabeleid |
| **Teenustasud** ← uus | **Blokeeri kustutamine** kui kasutuses | Arvutuste ajalugu on ärikriitiline — ei tohi kustutada |

| Uus asi | Kus näed | Mida tähendab |
|---------|---------|---------------|
| **`ConflictException` (409)** | `SellerCommissionRateService` | Uus vealiik — ei ole 403 (õiguste) ega 404 (puudub), vaid "ei saa teha, sest konflikt andmetega" |
| **`select (count(c) > 0)` JPQL** | `CommissionCalculationRepository` | Kontrollib kas seotud tabelis on viitavad read — tagastab `boolean` |
| **Veakood 412** | `ErrorResponse` | Uus veakood `COMMISSION_RATE_IN_USE` — grupp 41x on teenustasude jaoks |

Kõik muu (`validateUserIsAdmin`, `validateSellerExists`, `@RequestParam userId`, `@DeleteMapping`, `@Transactional` servicel) on tuttav eelmistest kustutamistest.

---

## Andmevoog — suur pilt

```
BROWSER (Admin vajutab "Kustuta" nuppu teenustasu real)
    ↓
FRONTEND (saadab DELETE päringu)
    ↓
AXIOS → DELETE /api/seller/3/commission-rates/8?userId=1
    ↓  ← siit algab backend
CONTROLLER (võtab userId, sellerId, commissionRateId kätte)
    ↓
SERVICE (kontrollib rolli → seller olemas? → määr olemas? → määr kasutuses? → kustutab)
    ↓
REPOSITORY (kas commission_calculation viitab sellele määrale?)
    ↓
ANDMEBAAS: JA → viska 409 | EI → kustuta commission_rate rida
    ↓
CONTROLLER (tagastab 200 OK, tühi vastus)
    ↓
FRONTEND (eemaldab teenustasu nimekirjast)
```

---

## BACKEND

### 1. ErrorResponse — uus veakood

```java
// Commission rate
COMMISSION_RATE_NOT_FOUND("Teenustasu määra ei leitud", 411),  // oli juba olemas
COMMISSION_RATE_IN_USE("Teenustasu on kasutuses ja seda ei saa kustutada", 412),  // uus
```

Veakoodid on grupeeritud numbrite järgi:
- `211` = seller vead
- `311` = kontakti vead
- `321` = piirkonna vead
- `411` = teenustasu ei leitud
- `412` = teenustasu on kasutuses ← uus

**Miks 409 HTTP staatusega?**

| HTTP kood | Tähendus | Millal kasutad |
|-----------|---------|----------------|
| 403 | Forbidden — pole õigust | Vale roll |
| 404 | Not Found — ei leitud | Objekt andmebaasis puudub |
| **409** | **Conflict — konflikt** | **Saad teha, aga andmed ei luba — siin: määr on kasutuses** |

---

### 2. CommissionCalculationRepository — uus päring

```java
public interface CommissionCalculationRepository extends JpaRepository<CommissionCalculation, Integer> {

    @Query("select (count(c) > 0) from CommissionCalculation c where c.commissionRate.id = :commissionRateId")
    boolean commissionCalculationExistsBy(Integer commissionRateId);
}
```

**Mida see päring teeb?**

`select (count(c) > 0)` — loendab read ja tagastab `true` kui neid on rohkem kui 0.

SQL-s oleks see: `SELECT COUNT(*) > 0 FROM commission_calculation WHERE commission_rate_id = ?`

Andmebaasis:
```
commission_calculation tabel:
id  | commission_rate_id | calculated_fee | ...
----+--------------------+----------------+----
 1  |         8          |     15.00      | ...  ← viitab määrale 8
 2  |         8          |     22.50      | ...  ← viitab määrale 8
```

Kui `commissionRateId = 8` → loeb 2 rida → `count > 0` on `true` → tagastab `true` → kustutamine blokeeritakse.

**Miks mitte `existsById`?**

`existsById(8)` kontrollib kas `commission_calculation` tabelis on rida `id = 8`.
Me ei taha teada kas arvutus ID-ga 8 eksisteerib — me tahame teada kas **kustutatavale määrale** viitavaid arvutusi on.
Seega vajame oma päringut kus tingimus on `commission_rate_id = :commissionRateId`.

---

### 3. SellerCommissionRateService — kustutamise loogika

```java
@Transactional
public void deleteSellerCommissionRate(Integer userId, Integer sellerId, Integer commissionRateId) {
    validateUserIsAdmin(userId);
    validateSellerExists(sellerId);
    validateCommissionRateExists(commissionRateId);
    validateCommissionRateNotInUse(commissionRateId);
    commissionRateRepository.deleteById(commissionRateId);
}
```

Viis sammu — järjekord on oluline:

1. **Õiguste kontroll** (403) — vale roll annab kohe vea, andmeid ei vaadata
2. **Seller olemas?** (404) — üldisem ressurss enne spetsiifilisemat
3. **Määr olemas?** (404) — kas see teenustasu üldse eksisteerib
4. **Määr kasutuses?** (409) — kas saab kustutada
5. **Kustuta** — ainult siis kui kõik kontrollid läbitud

```java
private void validateCommissionRateExists(Integer commissionRateId) {
    if (!commissionRateRepository.existsById(commissionRateId)) {
        throw new DataNotFoundException(
            COMMISSION_RATE_NOT_FOUND.getMessage(),
            COMMISSION_RATE_NOT_FOUND.getErrorCode());
    }
}

private void validateCommissionRateNotInUse(Integer commissionRateId) {
    if (commissionCalculationRepository.commissionCalculationExistsBy(commissionRateId)) {
        throw new ConflictException(
            COMMISSION_RATE_IN_USE.getMessage(),
            COMMISSION_RATE_IN_USE.getErrorCode());
    }
}
```

**Kaks erinevat repository-t ühes service-s:**

`validateCommissionRateExists` kasutab `commissionRateRepository` — kontrollib kas määr ise on olemas.
`validateCommissionRateNotInUse` kasutab `commissionCalculationRepository` — kontrollib kas arvutusi on seotud.

---

### 4. SellerCommissionRateController — endpoint

```java
@DeleteMapping("/seller/{sellerId}/commission-rates/{commissionRateId}")
@Operation(summary = "Kustuta teenustasu määr")
@ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "403", description = "Pole õigust",
                content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "404", description = "Teenustasu määra ei leidu",
                content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "409", description = "Teenustasu on kasutuses",
                content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "500", description = "Serveri viga",
                content = @Content(schema = @Schema(implementation = ApiError.class)))})
public void deleteSellerCommissionRate(@RequestParam Integer userId,
                                       @PathVariable Integer sellerId,
                                       @PathVariable Integer commissionRateId) {
    sellerCommissionRateService.deleteSellerCommissionRate(userId, sellerId, commissionRateId);
}
```

Kontroller ise on lihtne — kogu loogika on service-s.
Kontrolleri ülesanne on ainult: võtta parameetrid kätte ja edasi anda.

---

### Veakoodid

| HTTP kood | Olukord | errorCode |
|-----------|---------|-----------|
| 200 | Kustutamine õnnestus | — |
| 403 | Kasutaja pole Admin | 115 |
| 404 | Edasimüüjat ei leitud | 211 |
| 404 | Teenustasu määra ei leitud | 411 |
| 409 | Teenustasu on arvutustes kasutuses | 412 |

---

## Kolme DELETE võrdlus — kokkuvõte

| | Kontakt | Piirkond | Teenustasu |
|--|---------|----------|-----------|
| **FK probleem** | Jah — kustuta rollid enne | Ei | Jah — aga blokeerime, ei kustuta |
| **Lahendus** | Cascade delete (laps enne vanem) | `deleteById()` otse | `ConflictException` kui kasutuses |
| **Repository** | Custom `@Modifying @Query` | Ainult `deleteById()` | Uus repository + custom kontrollpäring |
| **Vealiigid** | 403, 404 | 403, 404 | 403, 404, **409** |

---

## Uued mõisted selles taskis

| Mõiste | Selgitus |
|--------|---------|
| **`ConflictException` (409)** | Viga kui operatsioon on tehniliselt võimalik aga andmed ei luba — siin: määr on arvutustes kasutuses |
| **Audit trail (auditi jälg)** | Ajaloolised andmed mida peab säilitama selgitamaks mineviku otsuseid — arvutuste ajalugu ei tohi kustutada |
| **`select (count(c) > 0)` JPQL** | Päring mis kontrollib kas seotud tabelis on viitavaid ridu — tagastab `boolean` |
| **Blokeeri vs kustuta** | Kaks erinevat lähenemist FK conflictile: kas kustutada seotud andmed enne (kontaktid) või keelata kustutamine (teenustasud) |

---

## Seos eelnevate taskidega

- **DELETE kontakt:** `validateUserIsAdmin` muster, `@RequestParam userId`, `@DeleteMapping` — täpselt sama
- **DELETE piirkond:** `deleteById()` lõpus — sama; aga siin lisandub eelnevalt kasutuse kontroll
- **Task-01 (login):** `userRepository.findById()` — sama muster õiguste kontrolliks

---

## Järgmised sammud

- `POST /api/seller/{sellerId}/commission-rates` — teenustasu määra lisamine
- `PUT /api/seller/{sellerId}/commission-rates/{commissionRateId}` — teenustasu määra muutmine