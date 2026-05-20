# PUT /api/seller/{sellerId}/regions/{regionId}

**Kontroller:** `SellerRegionController.java`
**Tüüp:** Backend
**Staatus:** To Do

## Kontekst

`SellerSettingsView` kuvab edasimüüja piirkondade nimekirja kus iga real on muutmisnupp (pliiats). Nupp avab inline muutmisrežiimi kus saab uuendada piirkonna müügipunktide arvu (`sales_point_count`). Piirkond ise (`region_id`) ei muutu — ainult müügipunktide arv. Ainult Admin saab piirkonna andmeid muuta. Seotud endpointid samal lehel: `GET /api/seller/{sellerId}/regions`, `POST /api/seller/{sellerId}/regions`, `DELETE /api/seller/{sellerId}/regions/{regionId}`.

## Mocki vaade

![SellerSettingsView mock](../../balsamiq/views/SellerSettingsView.vue.png)

## API leping

| Väli | Väärtus |
|------|---------|
| Meetod | `PUT` |
| Tee | `/api/seller/{sellerId}/regions/{regionId}` |
| Auth | Ei (rollipõhine kontroll teenuse kihis) |

### Request Body — `SellerRegionDto.java`

> Schema: [`SellerRegionDto_schema.json`](../../dtos/schema/SellerRegionDto_schema.json)
> Näidis: [`SellerRegionDto_SellerSettingsView_example.json`](../../dtos/examples/SellerRegionDto_SellerSettingsView_example.json)

| Väli | Tüüp | Kirjeldus |
|------|------|-----------|
| `salesPointCount` | `Integer` | Müügipunktide arv — vähemalt 0 (`@Min(0)`) |

### Response Body

Puudub — HTTP 200 tühi vastus

## Veahaldus

| Olukord | Exception klass | ErrorResponse enum | HTTP staatus |
|---------|----------------|-------------------|--------------|
| Kasutajal pole õigust (pole Admin) | `ForbiddenException` | `ACCESS_DENIED` | 403 |
| Edasimüüjat ei leitud | `DataNotFoundException` | `SELLER_NOT_FOUND` | 404 |
| Piirkonda ei leitud | `DataNotFoundException` | `SELLER_REGION_NOT_FOUND` | 404 |

> **Märkus veahalduse kohta:**
> Veahalduse infrastruktuur asub: `backend/src/main/java/ee/valiit/etas/infrastructure/`
> Kontrolli, kas vajalikud `ErrorResponse` enum kirjed ja exception klassid juba eksisteerivad:
> - `backend/src/main/java/ee/valiit/etas/infrastructure/error/ErrorResponse.java`
> - `backend/src/main/java/ee/valiit/etas/infrastructure/exception/`
>
> **`SELLER_REGION_NOT_FOUND` puudub praegu `ErrorResponse` enumist — lisa see enne implementeerimist:**
> ```
> // Region
> SELLER_REGION_NOT_FOUND("Piirkonda ei leitud", 321),
> ```
> `ForbiddenException`, `DataNotFoundException` ja `ACCESS_DENIED`, `SELLER_NOT_FOUND` on juba olemas.

## Andmebaas

Seotud tabelid: `seller`, `seller_region`

`regionId` URL-is on `seller_region.id` (vahel-tabeli primaarvõti, mitte `region.id`).
Uuendatakse ainult `seller_region.sales_point_count` väli — `region_id` ja `seller_id` jäävad muutmata.

```
seller_region tabel enne:
id  | seller_id | region_id | sales_point_count
----+-----------+-----------+------------------
 5  |     3     |     1     |        2

seller_region tabel pärast PUT (regionId=5, salesPointCount=5):
id  | seller_id | region_id | sales_point_count
----+-----------+-----------+------------------
 5  |     3     |     1     |        5          ← ainult see väli muutus
```

## Vastuvõtu kriteeriumid

- [ ] `PUT /api/seller/{sellerId}/regions/{regionId}` uuendab müügipunktide arvu ja tagastab 200 OK
- [ ] `SELLER_REGION_NOT_FOUND` on lisatud `ErrorResponse` enumisse koodiga `321`
- [ ] `SellerRegionDto` on loodud Java klassina õigesse paketti (`salesPointCount` väli koos `@Min(0)` validatsiooniga)
- [ ] Kasutajal pole Admin rolli: tagastab 403 koos `ACCESS_DENIED` veaga
- [ ] Edasimüüjat ei leidu: tagastab 404 koos `SELLER_NOT_FOUND` veaga
- [ ] Piirkonda ei leidu: tagastab 404 koos `SELLER_REGION_NOT_FOUND` veaga
- [ ] Controller, Service, Repository kihid on eraldatud
- [ ] Kontrolleri meetodil on `@Operation` ja `@ApiResponses` annotatsioonid (sh veavastused `ApiError` skeemiga)
- [ ] Swagger UI kaudu on endpoint nähtav ja testitav