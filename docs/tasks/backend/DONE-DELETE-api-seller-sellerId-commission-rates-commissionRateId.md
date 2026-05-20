# DELETE /api/seller/{sellerId}/commission-rates/{commissionRateId}

**Kontroller:** `SellerCommissionRateController.java`
**Tüüp:** Backend
**Staatus:** To Do

## Kontekst

`SellerSettingsView` kuvab edasimüüja teenustasude nimekirja koos "Kustuta" nupuga iga rea juures. See endpoint kustutab ühe teenustasu määra. Ainult Admin saab teenustasusid kustutada. Kui teenustasu on juba kasutatud arvutustes (`commission_calculation` tabelis on viitavad read), ei tohi seda kustutada — arvutuste ajalugu peab säilima. Seotud endpointid samal lehel: `GET /api/seller/{sellerId}/commission-rates`, `POST /api/seller/{sellerId}/commission-rates`, `PUT /api/seller/{sellerId}/commission-rates/{commissionRateId}`.

## Mocki vaade

![SellerSettingsView mock](../../balsamiq/views/SellerSettingsView.vue.png)

## API leping

| Väli | Väärtus |
|------|---------|
| Meetod | `DELETE` |
| Tee | `/api/seller/{sellerId}/commission-rates/{commissionRateId}` |
| Auth | Ei (rollipõhine kontroll teenuse kihis) |

### Request Body

Puudub — DELETE päring

### Response Body

Puudub — HTTP 200 tühi vastus

## Veahaldus

| Olukord | Exception klass | ErrorResponse enum | HTTP staatus |
|---------|----------------|-------------------|--------------|
| Kasutajal pole õigust (pole Admin) | `ForbiddenException` | `ACCESS_DENIED` | 403 |
| Edasimüüjat ei leitud | `DataNotFoundException` | `SELLER_NOT_FOUND` | 404 |
| Teenustasu määra ei leitud | `DataNotFoundException` | `COMMISSION_RATE_NOT_FOUND` | 404 |
| Teenustasu on kasutuses arvutustes | `ConflictException` | `COMMISSION_RATE_IN_USE` | 409 |

> **Märkus veahalduse kohta:**
> Veahalduse infrastruktuur asub: `backend/src/main/java/ee/valiit/etas/infrastructure/`
> Kontrolli, kas vajalikud `ErrorResponse` enum kirjed ja exception klassid juba eksisteerivad:
> - `backend/src/main/java/ee/valiit/etas/infrastructure/error/ErrorResponse.java`
> - `backend/src/main/java/ee/valiit/etas/infrastructure/exception/`
>
> **`COMMISSION_RATE_IN_USE` puudub praegu `ErrorResponse` enumist — lisa see enne implementeerimist:**
> ```java
> // Commission rate
> COMMISSION_RATE_NOT_FOUND("Teenustasu määra ei leitud", 411),  // juba olemas
> COMMISSION_RATE_IN_USE("Teenustasu on kasutuses ja seda ei saa kustutada", 412),  // lisa
> ```
> `ForbiddenException`, `DataNotFoundException`, `ConflictException` ja `ACCESS_DENIED`, `SELLER_NOT_FOUND`, `COMMISSION_RATE_NOT_FOUND` on juba olemas.

## Andmebaas

Seotud tabelid: `seller`, `commission_rate`, `commission_calculation`

`commission_calculation` tabelil on FK `commission_rate_id → commission_rate(id)` ilma `ON DELETE CASCADE`-ta. See tähendab: kui `commission_calculation` tabelis on ridu mis viitavad sellele teenustasule, **ei tohi** rida kustutada — arvutuste ajalugu peab säilima auditeerimiseks.

**Kustutamise loogika:**
```
1. Kontrolli: kas commission_calculation tabelis on ridu kus commission_rate_id = commissionRateId?
   JA → viska ConflictException (COMMISSION_RATE_IN_USE)
   EI → kustuta commission_rate rida otse (deleteById)
```

Erinevus kontaktide kustutamisest: kontaktide puhul kustutasime rollid enne kontakti (cascade). Siin me arvutusi ei kustuta — hoiame need ja blokeerime kustutamise.

```
commission_rate tabel:
id  | seller_id | product_type_id | fee_per_transaction | ...
----+-----------+-----------------+---------------------+----
 8  |     3     |        2        |       0.0100        | ...  ← tahame kustutada

commission_calculation tabel:
id  | commission_rate_id | calculated_fee | ...
----+--------------------+----------------+----
 1  |         8          |     15.00      | ...  ← viitab meie reale → EI LUBA kustutada
```

## Vastuvõtu kriteeriumid

- [ ] `DELETE /api/seller/{sellerId}/commission-rates/{commissionRateId}` kustutab teenustasu ja tagastab 200 OK
- [ ] `COMMISSION_RATE_IN_USE` on lisatud `ErrorResponse` enumisse koodiga `412`
- [ ] Teenustasu on kasutuses arvutustes: tagastab 409 koos `COMMISSION_RATE_IN_USE` veaga
- [ ] Kasutajal pole Admin rolli: tagastab 403 koos `ACCESS_DENIED` veaga
- [ ] Edasimüüjat ei leidu: tagastab 404 koos `SELLER_NOT_FOUND` veaga
- [ ] Teenustasu määra ei leidu: tagastab 404 koos `COMMISSION_RATE_NOT_FOUND` veaga
- [ ] Controller, Service, Repository kihid on eraldatud
- [ ] Kontrolleri meetodil on `@Operation` ja `@ApiResponses` annotatsioonid (sh veavastused `ApiError` skeemiga)
- [ ] Swagger UI kaudu on endpoint nähtav ja testitav