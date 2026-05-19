# DELETE /api/seller/{sellerId}/regions/{regionId}

**Kontroller:** `SellerRegionController.java`
**Tüüp:** Backend
**Staatus:** To Do

## Kontekst

`SellerSettingsView` kuvab edasimüüja piirkondade nimekirja koos "Kustuta" nupuga iga piirkonna real. See endpoint kustutab edasimüüja piirkonna seose (`seller_region` rea) — mitte piirkonda ennast, mis on üleüldine viiteandmestik. Ainult Admin saab piirkondi kustutada. Seotud endpointid samal lehel: `GET /api/seller/{sellerId}/regions`, `POST /api/seller/{sellerId}/regions`, `PUT /api/seller/{sellerId}/regions/{regionId}`.

## Mocki vaade

![SellerSettingsView mock](../../balsamiq/views/SellerSettingsView.vue.png)

## API leping

| Väli | Väärtus |
|------|---------|
| Meetod | `DELETE` |
| Tee | `/api/seller/{sellerId}/regions/{regionId}` |
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
| Piirkonda ei leitud | `DataNotFoundException` | `SELLER_REGION_NOT_FOUND` | 404 |

> **Märkus veahalduse kohta:**
> Veahalduse infrastruktuur asub: `backend/src/main/java/ee/valiit/etas/infrastructure/`
> Kontrolli, kas vajalikud `ErrorResponse` enum kirjed ja exception klassid juba eksisteerivad:
> - `backend/src/main/java/ee/valiit/etas/infrastructure/error/ErrorResponse.java`
> - `backend/src/main/java/ee/valiit/etas/infrastructure/exception/`
>
> **`SELLER_REGION_NOT_FOUND` puudub praegu `ErrorResponse` enumist — lisa see enne implementeerimist:**
> ```java
> // Region
> SELLER_REGION_NOT_FOUND("Piirkonda ei leitud", 321),
> ```
> `ForbiddenException`, `DataNotFoundException` ja `ACCESS_DENIED`, `SELLER_NOT_FOUND` on juba olemas.

## Andmebaas

Seotud tabelid: `seller`, `seller_region`

`regionId` URL-is on `seller_region.id` (vahel-tabeli primaarvõti, mitte `region.id`).
Kustutamisel eemaldatakse üks `seller_region` rida — `region` tabel jääb puutumata (see on üleüldine viiteandmestik, mida kasutavad kõik edasimüüjad).

`seller_region` tabelil pole alamtabeleid millele see viitaks, seega **FK constraint probleemi ei ole** — rida saab kustutada otse ilma eelneva puhastuseta. See erineb kontaktide kustutamisest kus tuli esmalt `seller_role` read kustutada.

```
seller_region tabel:
id  | seller_id | region_id | sales_point_count
----+-----------+-----------+------------------
 5  |     3     |     1     |        2          ← see rida kustutatakse (regionId=5)
```

## Vastuvõtu kriteeriumid

- [ ] `DELETE /api/seller/{sellerId}/regions/{regionId}` kustutab piirkonna seose ja tagastab 200 OK
- [ ] `SELLER_REGION_NOT_FOUND` on lisatud `ErrorResponse` enumisse koodiga `321`
- [ ] Kasutajal pole Admin rolli: tagastab 403 koos `ACCESS_DENIED` veaga
- [ ] Edasimüüjat ei leidu: tagastab 404 koos `SELLER_NOT_FOUND` veaga
- [ ] Piirkonda ei leidu: tagastab 404 koos `SELLER_REGION_NOT_FOUND` veaga
- [ ] Controller, Service, Repository kihid on eraldatud
- [ ] Kontrolleri meetodil on `@Operation` ja `@ApiResponses` annotatsioonid (sh veavastused `ApiError` skeemiga)
- [ ] Swagger UI kaudu on endpoint nähtav ja testitav