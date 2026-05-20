# DELETE /api/seller/{sellerId}/contacts/{contactId}

**Kontroller:** `SellerContactController.java`
**Tüüp:** Backend
**Staatus:** To Do

## Kontekst

`SellerSettingsView` kuvab edasimüüja kontaktide nimekirja koos "Kustuta" nupuga iga kontakti real. See endpoint kustutab ühe kontakti ja kõik tema rollid. Ainult Admin saab kontakte kustutada. Seotud endpointid samal lehel: `GET /api/seller/{sellerId}/contacts`, `POST /api/seller/{sellerId}/contacts`.

## Mocki vaade

![SellerSettingsView mock](../../balsamiq/views/SellerSettingsView.vue.png)

## API leping

| Väli | Väärtus |
|------|---------|
| Meetod | `DELETE` |
| Tee | `/api/seller/{sellerId}/contacts/{contactId}` |
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
| Kontakti ei leitud | `DataNotFoundException` | `CONTACT_NOT_FOUND` | 404 |

> **Märkus veahalduse kohta:**
> Veahalduse infrastruktuur asub: `backend/src/main/java/ee/valiit/etas/infrastructure/`
> Kontrolli, kas vajalikud `ErrorResponse` enum kirjed ja exception klassid juba eksisteerivad:
> - `backend/src/main/java/ee/valiit/etas/infrastructure/error/ErrorResponse.java`
> - `backend/src/main/java/ee/valiit/etas/infrastructure/exception/`
>
> Kõik vajalikud enum kirjed (`ACCESS_DENIED`, `SELLER_NOT_FOUND`, `CONTACT_NOT_FOUND`) ja exception klassid (`ForbiddenException`, `DataNotFoundException`) on juba olemas.

## Andmebaas

Seotud tabelid: `seller_contact`, `seller_role`

`seller_role` tabelil on foreign key `seller_contact_id → seller_contact(id)` ilma `ON DELETE CASCADE`-ta. Service peab kustutama **kõigepealt** kontakti rollid (`seller_role` ridad), seejärel kontakti enda (`seller_contact` rida). Vastupidises järjekorras tekib FK constraint rikkumine.

## Vastuvõtu kriteeriumid

- [ ] `DELETE /api/seller/{sellerId}/contacts/{contactId}` kustutab kontakti ja tagastab 200 OK
- [ ] Kontakti rollid (`seller_role`) kustutatakse enne kontakti kustutamist
- [ ] Kasutajal pole Admin rolli: tagastab 403 koos `ACCESS_DENIED` veaga
- [ ] Edasimüüjat ei leidu: tagastab 404 koos `SELLER_NOT_FOUND` veaga
- [ ] Kontakti ei leidu: tagastab 404 koos `CONTACT_NOT_FOUND` veaga
- [ ] Controller, Service, Repository kihid on eraldatud
- [ ] Kontrolleri meetodil on `@Operation` ja `@ApiResponses` annotatsioonid (sh veavastused `ApiError` skeemiga)
- [ ] Swagger UI kaudu on endpoint nähtav ja testitav
