# POST /api/seller/{sellerId}/commission-rates

**Kontroller:** `SellerCommissionRateController.java`
**Tüüp:** Backend
**Staatus:** To Do

## Kontekst

`SellerSettingsProductModal` võimaldab Adminil lisada edasimüüjale uue teenustasu määra. Modal avab dropdowni tootegruppide jaoks (laetakse `GET /api/product-type`) ning sisendväljad tasu, KM ja kehtivusaja jaoks. "Lisa" nupp saadab selle endpointi. Seotud endpointid samal modaalil: `GET /api/product-type` (dropdown), `GET /api/seller/{sellerId}/commission-rates` (nimekirja uuendamiseks peale lisamist).

## Mocki vaade

![SellerSettingsProductModal mock](../../balsamiq/views/SellerSettingsProductModal.vue.png)

## API leping

| Väli | Väärtus |
|------|---------|
| Meetod | `POST` |
| Tee | `/api/seller/{sellerId}/commission-rates?userId=` |
| Auth | Ei (rollipõhine kontroll teenuse kihis) |

### Request Body — `CommissionRateDto.java`

> Schema: [`CommissionRateDto_schema.json`](../../dtos/schema/CommissionRateDto_schema.json)
> Näidis: [`CommissionRateDto_SellerSettingsProductModal_example.json`](../../dtos/examples/CommissionRateDto_SellerSettingsProductModal_example.json)

| Väli | Tüüp | Kirjeldus |
|------|------|-----------|
| `productTypeId` | `Integer` | Valitud tootegruppi ID dropdownist — kohustuslik |
| `feePerTransaction` | `BigDecimal` | Tasu koguselt (EUR) — valikuline |
| `feePercent` | `BigDecimal` | Tasu % summalt — valikuline |
| `includesVat` | `Boolean` | KM sees — kohustuslik |
| `validFrom` | `String` | Kehtib alates, formaat `dd.MM.yyyy` — kohustuslik |
| `validTo` | `String` | Kehtib kuni, formaat `dd.MM.yyyy` — valikuline (null = lõputu) |

### Response Body

Puudub — HTTP 201 tühi vastus

## Veahaldus

| Olukord | Exception klass | ErrorResponse enum | HTTP staatus |
|---------|----------------|-------------------|--------------|
| Kohustuslik väli puudub | — (Spring validatsioon) | — | 400 |
| Kasutajal pole õigust (pole Admin) | `ForbiddenException` | `ACCESS_DENIED` | 403 |
| Edasimüüjat ei leitud | `DataNotFoundException` | `SELLER_NOT_FOUND` | 404 |
| Tootegruppi ei leitud | `DataNotFoundException` | `PRODUCT_TYPE_NOT_FOUND` | 404 |
| Sellel tootegrupil on juba kehtiv teenustasu | `ConflictException` | `COMMISSION_RATE_ALREADY_EXISTS` | 409 |

> **Märkus veahalduse kohta:**
> `ForbiddenException`, `DataNotFoundException`, `ConflictException`, `ACCESS_DENIED`, `SELLER_NOT_FOUND`, `PRODUCT_TYPE_NOT_FOUND` on juba olemas.
> **Puudub:** `COMMISSION_RATE_ALREADY_EXISTS` — lisa `ErrorResponse.java`-sse (Commission rate sektsiooni, soovituslik kood 413):
> ```java
> COMMISSION_RATE_ALREADY_EXISTS("Sellel tootegrupil on juba kehtiv teenustasu", 413),
> ```
> **409 loogika:** Kontrollitakse, kas edasimüüjal on juba olemas teenustasu sama `product_type_id`-ga, mille `valid_to IS NULL` (lõputu kehtivus). Kui jah → 409.

## Andmebaas

Seotud tabelid: `seller`, `product_type`, `commission_rate`

Lisamisel luuakse üks rida `commission_rate` tabelisse (`seller_id`, `product_type_id`, `fee_per_transaction`, `fee_percent`, `includes_vat`, `valid_from`, `valid_to`). Kuupäevaväljad teisendatakse `String` (`dd.MM.yyyy`) → `LocalDate`. `valid_to` võib olla `null` (lõputu kehtivus).

## Uued failid

Puuduvad — kõik vajalikud klassid juba olemas (vt muudetavad failid).

## Muudetavad failid

- `CommissionRateMapper.java` — lisa `toCommissionRate(CommissionRateDto dto)` meetod
- `CommissionRateRepository.java` — lisa lõputu kehtivuse kontroll: `existsBySellerIdAndProductTypeIdAndValidToIsNull(sellerId, productTypeId)`
- `SellerCommissionRateService.java` — lisa `addSellerCommissionRate(Integer userId, Integer sellerId, CommissionRateDto dto)`
- `SellerCommissionRateController.java` — lisa `@PostMapping`
- `ErrorResponse.java` — lisa `COMMISSION_RATE_ALREADY_EXISTS`

## Vastuvõtu kriteeriumid

- [ ] `POST /api/seller/{sellerId}/commission-rates?userId=` lisab teenustasu ja tagastab 201 Created
- [ ] Teenustasu salvestatakse `commission_rate` tabelisse
- [ ] `validFrom` ja `validTo` teisendatakse `dd.MM.yyyy` → `LocalDate`
- [ ] `validTo` võib olla `null` (lõputu kehtivus)
- [ ] Kasutajal pole Admin rolli: tagastab 403 koos `ACCESS_DENIED` veaga
- [ ] Edasimüüjat ei leidu: tagastab 404 koos `SELLER_NOT_FOUND` veaga
- [ ] Tootegruppi ei leidu: tagastab 404 koos `PRODUCT_TYPE_NOT_FOUND` veaga
- [ ] Sama tootegrupil on juba lõputu teenustasu: tagastab 409 koos `COMMISSION_RATE_ALREADY_EXISTS` veaga
- [ ] Controller, Service, Repository kihid on eraldatud
- [ ] Kontrolleri meetodil on `@Operation` ja `@ApiResponses` annotatsioonid
- [ ] Swagger UI kaudu on endpoint nähtav ja testitav