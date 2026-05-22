# Juhend: POST /api/seller/{sellerId}/commission-rates

**Taski fail:** `POST-api-seller-sellerId-commission-rates.md`
**Kontroller:** `SellerCommissionRateController.java`
**Implementeerimise voog:** RestController → Service → Repository (kontrollid) → Mapper → Repository (save) → RestController

---

## Sissejuhatus

See endpoint lisab edasimüüjale uue teenustasu määra. Päring käib läbi kontrolleri, service valideerib kasutaja õigused ja andmete olemasolu, seejärel mapper teisendab DTO entity-ks ja repository salvestab andmebaasi. Selle harjutuse käigus õpid POST endpointi lisama olemasolevasse kontrollerisse, töötama kuupäevade teisendusega ja lisama mitu erinevat veakontrolli.

**Kõik failid on juba olemas — midagi uut ei looda.** Lisatakse meetodeid olemasolevatesse failidesse.

---

## Enne alustamist — ErrorResponse täiendamine

Taskifailist: `COMMISSION_RATE_ALREADY_EXISTS` veakood puudub `ErrorResponse.java`-st.

Ava `backend/src/main/java/ee/valiit/etas/infrastructure/error/ErrorResponse.java` ja lisa Commission rate sektsiooni uus rida (soovituslik kood 413):

```
VEAKOOD("Veateade eesti keeles", 413),
```

> **Vaata** taskifailist täpsed andmed — sealt leiad nii veateate teksti kui koodinumbri.

---

## Samm 1 — RestController

### Mida teha?

`SellerCommissionRateController` on juba olemas — sellesse lisatakse uus `@PostMapping` meetod. Kontrolleris on juba `GET` ja `PUT` ja `DELETE` meetodid — vaata neid kui eeskuju.

Ava fail: `backend/src/main/java/ee/valiit/etas/controller/seller/SellerCommissionRateController.java`

### Meetodi loomine

Alusta meetodist **ilma mappingannotatsioonideta**:

```java
public void meetodiNimi(Integer paramA, Integer paramB, DtoTüüp dto) {
    // tühi meetod esialgu
}
```

> **Mõtle:** Millised parameetrid on vajalikud? Vaata taskifailist API teed ja request body kirjeldust:
> - API tee sisaldab `{sellerId}` → `@PathVariable`
> - API tee lõpus on `?userId=` → `@RequestParam`
> - Request body on `CommissionRateDto` → `@RequestBody`

Seejärel lisa annotatsioonid:
1. **`@PostMapping`** — õige URL lõpp (vaata taskifailist API teed)
2. **Parameetrite annotatsioonid** — `@PathVariable`, `@RequestParam`, `@Valid @RequestBody`
3. **Swagger annotatsioonid** — `@Operation` ja `@ApiResponses`

### Swagger annotatsioonid

Sellel endpointil on **5 erinevat vastust** — vaata taskifailist veahalduse tabel:
- `201` — teenustasu lisatud (edu korral tagastatakse tühi keha)
- `400` — kohustuslik väli puudub
- `403` — kasutajal pole õigust
- `404` — edasimüüjat/tootegruppi ei leitud
- `409` — sama tootegrupil on juba kehtiv teenustasu

> **Märkus:** `201 Created` — mitte `200 OK`. POST loob uue ressursi, seepärast `201`.

Vaata olemasolevaid meetodeid kontrolleris — `@ApiResponse` annotatsioonide stiil on sama.

### Service meetodi väljakutse

Kutsu service meetodit välja (esialgne tühi väljakutse):

```java
public void meetodiNimi(...) {
    teenuseMuutuja.meetodiNimi(...);
}
```

> **IntelliJ vihje:** Kui `sellerCommissionRateService.meetodiNimi(...)` on punasega alla joonitud,
> vajuta **Alt+Enter** → **"Create method in SellerCommissionRateService"**.

---

## Samm 2 — Service

### Mida teha?

Ava `SellerCommissionRateService.java` ja mine äsja loodud meetodisse. Service peab tegema **4 kontrollimist** enne salvestamist — vaata taskifailist veahalduse tabel.

Vaata olemasolevat `deleteSellerCommissionRate` meetodit — see on hea eeskuju, kuidas kontrollid on struktureeritud privaatmeetoditeks.

### Kontrollide järjekord

```java
@Transactional
public void meetodiNimi(Integer kasutajaId, Integer myyjaId, DtoTüüp dto) {
    // 1. Kas kasutaja on Admin?
    // 2. Kas edasimüüja eksisteerib?
    // 3. Kas tootegrupp eksisteerib? (vajad ProductType objekti hiljem)
    // 4. Kas sellel tootegrupil pole juba lõputut teenustasu?
    // 5. Loo entity ja salvesta
}
```

> **Mõtle:** Kontrollid 1, 2, 4 on sarnased olemasolevatele privaatmeetoditele.
> Kontrolli 3 puhul vajad `ProductType` objekti tagasi — miks?
> (Vihje: `CommissionRate` entity-l on `productType` väli, mis on FK objekt)

### Entity loomine ja salvestamine

Peale kontrollide on kaks sammu:
1. **Mapper** teisendab `CommissionRateDto` → `CommissionRate` entity
2. Seejärel sead käsitsi `seller` ja `productType` väljad (mapper ei saa neid seada — need on FK objektid)
3. `repository.save(entity)`

> **Miks mapper ei saa `seller` ja `productType` seada?**
> DTO sisaldab ainult `sellerId` ja `productTypeId` (numbrid), aga entity vajab tervet objekti.
> Objekte otsid repository-st — seejärel sead käsitsi `entity.setSeller(seller)`.

---

## Samm 3 — Repository (konfliktikontroll)

### Mida teha?

`CommissionRateRepository`-sse on vaja lisada uus meetod 409 kontrolliks:
kas edasimüüjal on juba sama tootegrupiga teenustasu, mille `valid_to IS NULL` (lõputu kehtivus).

Ava `CommissionRateRepository.java`. Kasuta **JPA Buddy**:

1. Paremklõps repository klassis → JPA Buddy
2. **Method** ja **Query** → **Query**
3. Meetodi tüüp: **Exists**
4. **Query conditions** — lisa kolm tingimust:
   - `seller.id` equals
   - `productType.id` equals
   - `validTo` isNull
5. **Advanced** → **Named parameters**

Peale loomist kontrolli meetodi nimi — projekti tava on lühike ja kirjeldav nimi.

> **Näide (VALE — liiga pikk JPA konventsioon):**
> `existsBySellerIdAndProductTypeIdAndValidToIsNull`
>
> **Parem — lühike projekti tava:**
> `commissionRateExistsBy(Integer sellerId, Integer productTypeId)`

---

## Samm 4 — Mapper (DTO → Entity)

### Mida teha?

`CommissionRateMapper`-sse lisatakse uus meetod, mis teisendab `CommissionRateDto` → `CommissionRate` entity.

Ava `CommissionRateMapper.java`. Vaata olemasolevat `updateCommissionRate` meetodit — sealt leiad kuupäevade teisenduse mustri (`String` → `LocalDate`).

Lisa uus meetod:

```java
@Mapping(ignore = true, target = "")
@Mapping(ignore = true, target = "")
@Mapping(ignore = true, target = "")
@Mapping(expression = "java(...)", target = "validFrom")
@Mapping(expression = "java(...)", target = "validTo")
EntiteetTüüp toEntiteetNimi(DtoTüüp dto);
```

> **Mõtle — millised väljad tuleb ignoreerida?**
> - `id` — uus rida, ID genereerib andmebaas
> - `seller` — seatakse käsitsi service'is
> - `productType` — seatakse käsitsi service'is
>
> **Kuupäevade teisendus** — vaata `updateCommissionRate` meetodit, seal on täpselt sama muster:
> `String` (`dd.MM.yyyy`) → `LocalDate.parse(...)`. Kopeeri sama loogika.

---

## Samm 5 — tagasi RestController'isse

### Mida teha?

Service meetod on `void` — tagastab midagi? Ei, POST lisamisel tagastatakse tühi keha.

Kontrolli, et `@PostMapping` meetodil oleks õige tagastustüüp — `void` on õige.

Swagger annotatsiooni `responseCode = "201"` — kas oled selle lisanud?

---

## Samm 6 — kood ilusaks (refactor)

### Make it work → Make it beautiful

Kui kood töötab, vaata service meetodit üle:

- Kas avalik meetod on lühike (3–7 rida)?
- Kas kontrollid on eraldatud privaatmeetoditeks?
- Kas privaatmeetodite nimed kirjeldavad mida kontrollitakse?

**Meetodite järjekord service klassis:**
1. `public` meetodid üleval
2. `private` meetodid all, väljakutsumise järjekorras

---

## Kokkuvõte ja kontrollnimekiri

- [ ] `ErrorResponse.java` — `COMMISSION_RATE_ALREADY_EXISTS` on lisatud
- [ ] `SellerCommissionRateController.java` — `@PostMapping` meetod on lisatud koos `@Operation` ja `@ApiResponses` annotatsioonidega
- [ ] `SellerCommissionRateService.java` — `addSellerCommissionRate()` meetod on lisatud koos 4 kontrollimisega
- [ ] `CommissionRateRepository.java` — konfliktikontrolli meetod on lisatud
- [ ] `CommissionRateMapper.java` — `toCommissionRate()` meetod on lisatud koos kuupäevade teisendusega
- [ ] `seller` ja `productType` seatakse service'is käsitsi peale mappimist
- [ ] Kood kompileerub ja Swagger UI kaudu on `POST /api/seller/{sellerId}/commission-rates` nähtav

---

> **Järgmine samm:** Testi Swagger UI kaudu — loo teenustasu, seejärel proovi sama tootegrupiga uuesti (peaks tagastama 409).