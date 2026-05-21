# POST /api/seller/{sellerId}/contacts

**Kontroller:** `SellerContactController.java`
**Tüüp:** Backend
**Staatus:** To Do

## Kontekst

`SellerSettingsView` kuvab edasimüüja kontaktide nimekirja koos "Lisa kontakt" nupuga. Nupp avab modali kus Admin saab sisestada uue kontakti andmed ja valida rollid. Ainult Admin saab kontakte lisada. Seotud endpointid samal lehel: `GET /api/seller/{sellerId}/contacts`, `DELETE /api/seller/{sellerId}/contacts/{contactId}`.

## Mocki vaade

![SellerSettingsView mock](../../balsamiq/views/SellerSettingsView.vue.png)

## API leping

| Väli | Väärtus |
|------|---------|
| Meetod | `POST` |
| Tee | `/api/seller/{sellerId}/contacts` |
| Auth | Ei (rollipõhine kontroll teenuse kihis) |

### Request Body — `SellerContactDto.java`

> Schema: [`SellerContactDto_schema.json`](../../dtos/schema/SellerContactDto_schema.json)

| Väli | Tüüp | Kirjeldus |
|------|------|-----------|
| `firstName` | `String` | Eesnimi — kohustuslik (`@NotBlank`) |
| `middleName` | `String` | Keskmisnimi — valikuline |
| `lastName` | `String` | Perenimi — kohustuslik (`@NotBlank`) |
| `phone` | `String` | Telefon — valikuline |
| `email` | `String` | E-mail — kohustuslik (`@NotBlank`) |
| `roles` | `List<String>` | Rollide koodid — nt `["L", "A"]` |

### Response Body

Puudub — HTTP 201 tühi vastus

## Veahaldus

| Olukord | Exception klass | ErrorResponse enum | HTTP staatus |
|---------|----------------|-------------------|--------------|
| Kohustuslik väli puudub (`@NotBlank` ebaõnnestub) | — (Spring validatsioon) | — | 400 |
| Kasutajal pole õigust (pole Admin) | `ForbiddenException` | `ACCESS_DENIED` | 403 |
| Edasimüüjat ei leitud | `DataNotFoundException` | `SELLER_NOT_FOUND` | 404 |

> **Märkus veahalduse kohta:**
> `ForbiddenException`, `DataNotFoundException`, `ACCESS_DENIED`, `SELLER_NOT_FOUND` on juba olemas.

## Andmebaas

Seotud tabelid: `seller`, `seller_contact`, `seller_role`, `role`

Lisamisel luuakse:
1. Üks rida `seller_contact` tabelisse (firstName, middleName, lastName, phone, email, seller_id)
2. Iga rolli kohta üks rida `seller_role` tabelisse (seller_contact_id, seller_role_id)

`role` tabel sisaldab fikseeritud rollid (L, A, R, T) — otsitakse `code` veeru järgi.

```
seller_contact tabel pärast:
id  | seller_id | first_name | last_name | email
----+-----------+------------+-----------+------
 7  |     3     | Mari       | Tamm      | mari@email.ee

seller_role tabel pärast:
id  | seller_contact_id | seller_role_id
----+-------------------+---------------
 12 |         7         |      1        ← L roll
 13 |         7         |      3        ← R roll
```

## Uued failid

- `SellerContactDto.java` — uus DTO klass
- `ContactRoleRepository.java` — uus repository (`findByCode(String code)` meetodiga)
- `SellerContactDto_schema.json` — uus DTO schema fail

## Muudetavad failid

- `SellerContactMapper.java` — lisa `toSellerContact(SellerContactDto dto)` meetod
- `SellerContactService.java` — lisa `addSellerContact(Integer userId, Integer sellerId, SellerContactDto dto)`
- `SellerContactController.java` — lisa `@PostMapping`

## Vastuvõtu kriteeriumid

- [ ] `POST /api/seller/{sellerId}/contacts?userId=` lisab kontakti ja tagastab 201 Created
- [ ] Kontakt salvestatakse `seller_contact` tabelisse
- [ ] Iga roll salvestatakse `seller_role` tabelisse
- [ ] `SellerContactDto` on loodud Java klassina õigesse paketti
- [ ] `ContactRoleRepository` on loodud `findByCode` meetodiga
- [ ] Kasutajal pole Admin rolli: tagastab 403 koos `ACCESS_DENIED` veaga
- [ ] Edasimüüjat ei leidu: tagastab 404 koos `SELLER_NOT_FOUND` veaga
- [ ] Controller, Service, Repository kihid on eraldatud
- [ ] Kontrolleri meetodil on `@Operation` ja `@ApiResponses` annotatsioonid
- [ ] Swagger UI kaudu on endpoint nähtav ja testitav
