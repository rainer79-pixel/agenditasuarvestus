# PUT /api/seller/{sellerId}/commission-rates/{commissionRateId}

**Kontroller:** `SellerCommissionRateController.java`
**Tüüp:** Backend
**Staatus:** To Do

## Kontekst

`SellerSettingsView` kuvab edasimüüja teenustasude nimekirja kus iga real on muutmisnupp (pliiats). Nupp avab modaali kus saab muuta teenustasu kõiki välju: tootegruppi, tasu koguselt, tasu protsenti, KM sisaldumist ja kehtivusperioodi. Ainult Admin saab teenustasusid muuta. Seotud endpointid samal lehel: `GET /api/seller/{sellerId}/commission-rates`, `POST /api/seller/{sellerId}/commission-rates`, `DELETE /api/seller/{sellerId}/commission-rates/{commissionRateId}`.

## Mocki vaade

![SellerSettingsView mock](../../balsamiq/views/SellerSettingsView.vue.png)

## API leping

| Väli | Väärtus |
|------|---------|
| Meetod | `PUT` |
| Tee | `/api/seller/{sellerId}/commission-rates/{commissionRateId}` |
| Auth | Ei (rollipõhine kontroll teenuse kihis) |

### Request Body — `CommissionRateDto.java`

> Schema: [`CommissionRateDto_schema.json`](../../dtos/schema/CommissionRateDto_schema.json)
> Näidis: [`CommissionRateDto_SellerSettingsView_example.json`](../../dtos/examples/CommissionRateDto_SellerSettingsView_example.json)

| Väli | Tüüp | Kirjeldus |
|------|------|-----------|
| `productTypeId` | `Integer` | Tootegrupi ID (`product_type.id`) |
| `feePerTransaction` | `BigDecimal` | Tasu ühe tehingu kohta eurodes — `null` kui kasutatakse protsenttasu |
| `feePercent` | `BigDecimal` | Teenustasu protsent — `null` kui kasutatakse tehingupõhist tasu |
| `includesVat` | `Boolean` | `true` = KM on juba tasus sees; `false` = KM lisatakse peale |
| `validFrom` | `String` | Kehtivuse algus formaadis `pp.kk.aaaa` |
| `validTo` | `String` | Kehtivuse lõpp formaadis `pp.kk.aaaa` — `null` = kehtib lõputult |

> **Märkus kuupäevade kohta:** Kuupäevad liiguvad API-s eesti formaadis `pp.kk.aaaa` (nt `14.05.2026`). Mapper teisendab stringi `LocalDate`-ks backendis. Sama lähenemine on kasutuses `SellerMapper`-is.

### Response Body

Puudub — HTTP 200 tühi vastus

## Veahaldus

| Olukord | Exception klass | ErrorResponse enum | HTTP staatus |
|---------|----------------|-------------------|--------------|
| Kasutajal pole õigust (pole Admin) | `ForbiddenException` | `ACCESS_DENIED` | 403 |
| Edasimüüjat ei leitud | `DataNotFoundException` | `SELLER_NOT_FOUND` | 404 |
| Teenustasu määra ei leitud | `DataNotFoundException` | `COMMISSION_RATE_NOT_FOUND` | 404 |

> **Märkus veahalduse kohta:**
> Veahalduse infrastruktuur asub: `backend/src/main/java/ee/valiit/etas/infrastructure/`
> Kontrolli, kas vajalikud `ErrorResponse` enum kirjed ja exception klassid juba eksisteerivad:
> - `backend/src/main/java/ee/valiit/etas/infrastructure/error/ErrorResponse.java`
> - `backend/src/main/java/ee/valiit/etas/infrastructure/exception/`
>
> Kõik vajalikud enum kirjed (`ACCESS_DENIED`, `SELLER_NOT_FOUND`, `COMMISSION_RATE_NOT_FOUND`) ja exception klassid (`ForbiddenException`, `DataNotFoundException`) on juba olemas.

## Andmebaas

Seotud tabelid: `seller`, `commission_rate`, `product_type`

Uuendatakse `commission_rate` rea kõik väljad peale `seller_id` — see tuleb URL-i `sellerId` path variable-ist ja ei muutu. `product_type_id` on FK `product_type(id)` vastu — mapper peab laadima `ProductType` entity andmebaasist enne salvestamist.

```
commission_rate tabel enne:
id | seller_id | product_type_id | fee_per_transaction | fee_percent | includes_vat | valid_from   | valid_to
---+-----------+-----------------+---------------------+-------------+--------------+--------------+---------
 8 |     3     |        2        |       0.0100        |    null     |     true     | 2026-05-14   |  null

commission_rate tabel pärast PUT (commissionRateId=8):
 8 |     3     |        2        |       0.0200        |    null     |     false    | 2026-05-14   |  null
                                         ↑                                ↑
                                    muutus                            muutus
```

> **Tähelepanu mapperis:** `CommissionRateDto` sisaldab `productTypeId` (Integer), aga entity vajab `ProductType` objekti. Service peab `ProductType` entity andmebaasist laadima ja seadma — mapper üksi seda teha ei saa. Sama muster on näha `SellerService.createAndSaveSeller()` kus `User` entity laetakse eraldi.

## Vastuvõtu kriteeriumid

- [ ] `PUT /api/seller/{sellerId}/commission-rates/{commissionRateId}` uuendab teenustasu ja tagastab 200 OK
- [ ] `CommissionRateDto` on loodud Java klassina õigesse paketti koos `@NotNull` väljadel `includesVat` ja `validFrom`
- [ ] Kasutajal pole Admin rolli: tagastab 403 koos `ACCESS_DENIED` veaga
- [ ] Edasimüüjat ei leidu: tagastab 404 koos `SELLER_NOT_FOUND` veaga
- [ ] Teenustasu määra ei leidu: tagastab 404 koos `COMMISSION_RATE_NOT_FOUND` veaga
- [ ] Kuupäevad teisendatakse õigesti `String` (`pp.kk.aaaa`) ↔ `LocalDate`
- [ ] `productTypeId` laetakse `ProductType` entity-ks enne salvestamist
- [ ] Controller, Service, Repository kihid on eraldatud
- [ ] Kontrolleri meetodil on `@Operation` ja `@ApiResponses` annotatsioonid (sh veavastused `ApiError` skeemiga)
- [ ] Swagger UI kaudu on endpoint nähtav ja testitav