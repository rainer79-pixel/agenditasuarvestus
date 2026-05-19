# PUT /api/seller/{sellerId}/status

**Kontroller:** `SellerController.java`
**Tüüp:** Backend
**Staatus:** To Do

## Kontekst

`SellerSettingsView` kuvab edasimüüja seadete lehel staatuse koos kahe nupuga: "Deaktiveeri" (kui aktiivne) ja "Aktiveeri" (kui mitteaktiivne). See endpoint muudab edasimüüja staatuse `ACTIVE` ↔ `INACTIVE` — edasimüüjat ei kustutata, ainult deaktiveeritakse. Ainult Admin saab staatust muuta. Seotud endpointid samal lehel: `GET /api/seller/{sellerId}`, `GET /api/seller/{sellerId}/contacts`, `GET /api/seller/{sellerId}/regions`, `GET /api/seller/{sellerId}/commission-rates`.

## Mocki vaade

![SellerSettingsView mock](../../balsamiq/views/SellerSettingsView.vue.png)

## API leping

| Väli | Väärtus |
|------|---------|
| Meetod | `PUT` |
| Tee | `/api/seller/{sellerId}/status` |
| Auth | Ei (rollipõhine kontroll teenuse kihis) |

### Request Body — `SellerStatusDto.java`

> Schema: [`SellerStatusDto_schema.json`](../../dtos/schema/SellerStatusDto_schema.json)
> Näidis: [`SellerStatusDto_SellerSettingsView_example.json`](../../dtos/examples/SellerStatusDto_SellerSettingsView_example.json)

| Väli | Tüüp | Kirjeldus |
|------|------|-----------|
| `status` | `String` | Uus staatus — `"ACTIVE"` või `"INACTIVE"` |

### Response Body

Puudub — HTTP 200 tühi vastus

## Veahaldus

| Olukord | Exception klass | ErrorResponse enum | HTTP staatus |
|---------|----------------|-------------------|--------------|
| Kasutajal pole õigust (pole Admin) | `ForbiddenException` | `ACCESS_DENIED` | 403 |
| Edasimüüjat ei leitud | `DataNotFoundException` | `SELLER_NOT_FOUND` | 404 |
| Edasimüüja on juba deaktiveeritud | `ConflictException` | `SELLER_ALREADY_INACTIVE` | 409 |
| Edasimüüja on juba aktiivne | `ConflictException` | `SELLER_ALREADY_ACTIVE` | 409 |

> **Märkus veahalduse kohta:**
> Veahalduse infrastruktuur asub: `backend/src/main/java/ee/valiit/etas/infrastructure/`
> Kontrolli, kas vajalikud `ErrorResponse` enum kirjed ja exception klassid juba eksisteerivad:
> - `backend/src/main/java/ee/valiit/etas/infrastructure/error/ErrorResponse.java`
> - `backend/src/main/java/ee/valiit/etas/infrastructure/exception/`
>
> Kõik vajalikud enum kirjed (`ACCESS_DENIED`, `SELLER_NOT_FOUND`, `SELLER_ALREADY_INACTIVE`, `SELLER_ALREADY_ACTIVE`) ja exception klassid (`ForbiddenException`, `DataNotFoundException`, `ConflictException`) on juba olemas.

## Andmebaas

Seotud tabelid: `seller`

`seller.status` veerg (VARCHAR(10)) salvestatakse DB koodina `"A"` (aktiivne) või `"D"` (deaktiveeritud). API tasandil kasutatakse `"ACTIVE"` / `"INACTIVE"` — teisendus toimub `Status` enum-i kaudu mapperis. Enne uuendamist kontrollida, et edasimüüja oleks olemas ja et uus staatus erineks praegusest.

## Vastuvõtu kriteeriumid

- [ ] `PUT /api/seller/{sellerId}/status` koos `{"status": "INACTIVE"}` muudab staatuse ja tagastab 200 OK
- [ ] `PUT /api/seller/{sellerId}/status` koos `{"status": "ACTIVE"}` aktiveerib edasimüüja ja tagastab 200 OK
- [ ] Kasutajal pole Admin rolli: tagastab 403 koos `ACCESS_DENIED` veaga
- [ ] Edasimüüjat ei leidu: tagastab 404 koos `SELLER_NOT_FOUND` veaga
- [ ] Edasimüüja on juba INACTIVE: tagastab 409 koos `SELLER_ALREADY_INACTIVE` veaga
- [ ] Edasimüüja on juba ACTIVE: tagastab 409 koos `SELLER_ALREADY_ACTIVE` veaga
- [ ] Kõik DTO klassid on loodud Java klassidena õigesse paketti
- [ ] Controller, Service, Repository kihid on eraldatud
- [ ] Kontrolleri meetodil on `@Operation` ja `@ApiResponses` annotatsioonid (sh veavastused `ApiError` skeemiga)
- [ ] Swagger UI kaudu on endpoint nähtav ja testitav
