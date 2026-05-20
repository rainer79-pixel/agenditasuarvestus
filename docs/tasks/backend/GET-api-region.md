# GET /api/region

**Kontroller:** `RegionController.java`
**Tüüp:** Backend
**Staatus:** To Do

## Kontekst

`SellerSettingsRegionModal` avab dropdowni kus Admin saab valida piirkonna edasimüüjale lisamiseks. Dropdown laetakse selle endpointiga — tagastatakse kõik piirkonnad järjestatud `sequence_number` järgi. Seotud endpointid samal modaalil: `POST /api/seller/{sellerId}/regions`.

## Mocki vaade

![SellerSettingsRegionModal mock](../../balsamiq/views/SellerSettingRegionModal.vue.png)

## API leping

| Väli | Väärtus |
|------|---------|
| Meetod | `GET` |
| Tee | `/api/region` |
| Auth | Ei (rollipõhine kontroll puudub) |

### Request Body

Puudub — GET päring

### Response Body — `RegionResponseDto.java`

> Schema: [`RegionResponseDto_schema.json`](../../dtos/schema/RegionResponseDto_schema.json)
> Näidis: [`RegionResponseDto_SellerSettingsRegionModal_Array_example.json`](../../dtos/examples/RegionResponseDto_SellerSettingsRegionModal_Array_example.json)

| Väli | Tüüp | Allikas (DB tabel.veerg) |
|------|------|--------------------------|
| `regionId` | `Integer` | `region.id` |
| `regionName` | `String` | `region.region_name` |

## Veahaldus

| Olukord | Exception klass | ErrorResponse enum | HTTP staatus |
|---------|----------------|-------------------|--------------|
| Serveri viga | — | — | 500 |

> **Märkus veahalduse kohta:**
> Veahalduse infrastruktuur asub: `backend/src/main/java/ee/valiit/etas/infrastructure/`
> See endpoint ei oma spetsiifilist veahaldust — JPA vead käsitleb `RestExceptionHandler` automaatselt.

## Andmebaas

Seotud tabelid: `region`

Loetakse kõik read `region` tabelist, järjestatakse `sequence_number` veeru järgi kasvavalt. `sequence_number` tagab piirkondade loogilise järjestuse (Eesti maakonnad + suuremad linnad).

## Uued failid

- `RegionResponseDto.java` — uus DTO klass
- `RegionMapper.java` — uus mapper
- `RegionService.java` — uus service
- `RegionController.java` — uus kontroller

## Vastuvõtu kriteeriumid

- [ ] `GET /api/region` tagastab 200 ja piirkondade massiivi
- [ ] Piirkonnad on järjestatud `sequence_number` järgi
- [ ] `RegionResponseDto` on loodud Java klassina õigesse paketti
- [ ] Controller, Service, Repository, Mapper kihid on eraldatud
- [ ] Kontrolleri meetodil on `@Operation` ja `@ApiResponses` annotatsioonid
- [ ] Swagger UI kaudu on endpoint nähtav ja testitav