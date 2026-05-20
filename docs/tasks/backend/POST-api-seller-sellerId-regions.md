# POST /api/seller/{sellerId}/regions

**Kontroller:** `SellerRegionController.java`
**Tüüp:** Backend
**Staatus:** To Do

## Kontekst

`SellerSettingsRegionModal` võimaldab Adminil lisada edasimüüjale uue piirkonna. Modal avab dropdowni (laetakse `GET /api/region`) kust valitakse piirkond, ning number input müügipunktide arvu jaoks. "Lisa" nupp saadab selle endpointi. Seotud endpointid samal modaalil: `GET /api/region` (dropdown), `GET /api/seller/{sellerId}/regions` (nimekirja uuendamiseks peale lisamist).

## Mocki vaade

![SellerSettingsRegionModal mock](../../balsamiq/views/SellerSettingRegionModal.vue.png)

## API leping

| Väli | Väärtus |
|------|---------|
| Meetod | `POST` |
| Tee | `/api/seller/{sellerId}/regions?userId=` |
| Auth | Ei (rollipõhine kontroll teenuse kihis) |

### Request Body — `SellerRegionDto.java`

> Schema: [`SellerRegionDto_schema.json`](../../dtos/schema/SellerRegionDto_schema.json)
> Näidis: [`SellerRegionDto_SellerSettingsRegionModal_example.json`](../../dtos/examples/SellerRegionDto_SellerSettingsRegionModal_example.json)

| Väli | Tüüp | Kirjeldus |
|------|------|-----------|
| `regionId` | `Integer` | Valitud piirkonna ID dropdownist — kohustuslik |
| `salesPointCount` | `Integer` | Müügipunktide arv — kohustuslik, min 0 |

> **Märkus:** `SellerRegionDto`-sse tuleb lisada `regionId` väli (praegu on ainult `salesPointCount`).

### Response Body

Puudub — HTTP 201 tühi vastus

## Veahaldus

| Olukord | Exception klass | ErrorResponse enum | HTTP staatus |
|---------|----------------|-------------------|--------------|
| Kasutajal pole õigust (pole Admin) | `ForbiddenException` | `ACCESS_DENIED` | 403 |
| Piirkond on juba lisatud | `ConflictException` | `SELLER_REGION_ALREADY_EXISTS` | 409 |
| Edasimüüjat ei leitud | `DataNotFoundException` | `SELLER_NOT_FOUND` | 404 |
| Piirkonda ei leitud | `DataNotFoundException` | `SELLER_REGION_NOT_FOUND` | 404 |

> **Märkus veahalduse kohta:**
> Veahalduse infrastruktuur asub: `backend/src/main/java/ee/valiit/etas/infrastructure/`
> `ForbiddenException`, `DataNotFoundException`, `ConflictException`, `ACCESS_DENIED`, `SELLER_NOT_FOUND` on juba olemas.
> **Puudub:** `SELLER_REGION_ALREADY_EXISTS` — lisa `ErrorResponse.java`-sse (vt. Region sektsiooni, soovituslik kood 322):
> ```java
> SELLER_REGION_ALREADY_EXISTS("See piirkond on juba lisatud", 322),
> ```

## Andmebaas

Seotud tabelid: `seller`, `region`, `seller_region`

Lisamisel luuakse üks rida `seller_region` tabelisse (`seller_id`, `region_id`, `sales_point_count`). Enne lisamist kontrollida, et sama `seller_id` + `region_id` kombinatsioon ei eksisteeri — 409 viga kui juba olemas. DB-s puudub unikaalsuspiirang, seega kontroll tehakse teenuse kihis.

## Uued failid

Puuduvad — kõik vajalikud klassid juba olemas (vt muudetavad failid).

## Muudetavad failid

- `SellerRegionDto.java` — lisa `regionId` väli (`@NotNull`)
- `SellerRegionRepository.java` — lisa duplikaadi kontroll: `existsBySellerIdAndRegionId(sellerId, regionId)`
- `SellerRegionService.java` — lisa `addSellerRegion(Integer userId, Integer sellerId, SellerRegionDto dto)`
- `SellerRegionController.java` — lisa `@PostMapping`
- `ErrorResponse.java` — lisa `SELLER_REGION_ALREADY_EXISTS`

## Vastuvõtu kriteeriumid

- [ ] `POST /api/seller/{sellerId}/regions?userId=` lisab piirkonna ja tagastab 201 Created
- [ ] Piirkond salvestatakse `seller_region` tabelisse
- [ ] Kasutajal pole Admin rolli: tagastab 403 koos `ACCESS_DENIED` veaga
- [ ] Piirkond on juba lisatud: tagastab 409 koos `SELLER_REGION_ALREADY_EXISTS` veaga
- [ ] Edasimüüjat ei leidu: tagastab 404 koos `SELLER_NOT_FOUND` veaga
- [ ] Controller, Service, Repository kihid on eraldatud
- [ ] Kontrolleri meetodil on `@Operation` ja `@ApiResponses` annotatsioonid
- [ ] Swagger UI kaudu on endpoint nähtav ja testitav