# GET /api/product-type

**Kontroller:** `ProductTypeController.java`
**Tüüp:** Backend
**Staatus:** Done ✅

## Kontekst

`SellerSettingsProductModal` avab dropdowni kus Admin saab valida tootegruppi teenustasu lisamiseks. Dropdown laetakse selle endpointiga — tagastatakse kõik tootegrupid. Seotud endpointid samal modaalil: `POST /api/seller/{sellerId}/commission-rates` (teenustasu lisamine).

## Mocki vaade

![SellerSettingsProductModal mock](../../balsamiq/views/SellerSettingsProductModal.vue.png)

## API leping

| Väli | Väärtus |
|------|---------|
| Meetod | `GET` |
| Tee | `/api/product-type` |
| Auth | Ei (rollipõhine kontroll puudub) |

### Request Body

Puudub — GET päring

### Response Body — `ProductTypeResponseDto.java`

> Schema: [`ProductTypeResponseDto_schema.json`](../../dtos/schema/ProductTypeResponseDto_schema.json)
> Näidis: [`ProductTypeResponseDto_SellerSettingsProductModal_Array_example.json`](../../dtos/examples/ProductTypeResponseDto_SellerSettingsProductModal_Array_example.json)

| Väli | Tüüp | Allikas (DB tabel.veerg) |
|------|------|--------------------------|
| `productTypeId` | `Integer` | `product_type.id` |
| `productTypeName` | `String` | `product_type.product_type_name` |

## Veahaldus

| Olukord | Exception klass | ErrorResponse enum | HTTP staatus |
|---------|----------------|-------------------|--------------|
| Serveri viga | — | — | 500 |

> **Märkus veahalduse kohta:**
> See endpoint ei oma spetsiifilist veahaldust — JPA vead käsitleb `RestExceptionHandler` automaatselt.

## Andmebaas

Seotud tabelid: `product_type`

Loetakse kõik read `product_type` tabelist. Tabel sisaldab fikseeritud seed-andmed: `isikustamine`, `kaardimyyk`, `pilet`, `rahalaadimine`, `sooduskaardi isikustamine`, `kaardi tagasiost`, `raha valjamakse`.

## Uued failid

- `ProductTypeResponseDto.java` — uus DTO klass
- `ProductTypeMapper.java` — uus mapper
- `ProductTypeService.java` — uus service
- `ProductTypeController.java` — uus kontroller

## Vastuvõtu kriteeriumid

- [x] `GET /api/product-type` tagastab 200 ja tootegruppide massiivi
- [x] Kõik tootegrupid tagastatakse
- [x] `ProductTypeResponseDto` on loodud Java klassina õigesse paketti
- [x] Controller, Service, Mapper kihid on eraldatud
- [x] Kontrolleri meetodil on `@Operation` ja `@ApiResponses` annotatsioonid
- [x] Swagger UI kaudu on endpoint nähtav ja testitav