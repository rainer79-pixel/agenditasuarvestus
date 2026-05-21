# GET /api/product-type — kõik tootegrupid

## Mis see on ja miks oluline?

See endpoint tagastab kõik tootegrupid andmebaasist. Seda kasutatakse `SellerSettingsProductModal`-is dropdowni laadimiseks — Admin valib tootegruppi teenustasu lisamiseks.

**Päriselust analoogia:** Kujuta ette kohvikut kus on seinale kirjutatud kõik joogi tüübid (kohv, tee, kakao). Iga kord kui keegi tahab tellida, vaatab ta seina. Nimekiri ei muutu — lihtsalt loetakse seina pealt.

---

## Mis on selles taskis uut vs varasemad taskid?

See task on **sama muster mis `GET /api/region`** — uut kontseptsiooni pole. Eesmärk on kinnistada sama voog teist korda läbi tehes.

| Asi | GET /api/region | GET /api/product-type |
|-----|----------------|----------------------|
| Kõik 4 kihti uued | ✅ | ✅ |
| Spetsiifiline veahaldus | ❌ | ❌ |
| Custom repository päring | ✅ (ORDER BY vajas) | ❌ (findAll() piisab) |
| Parameetreid päringus | ❌ | ❌ |

**Üks oluline erinevus:** `product_type` tabel ei vaja järjestamist — `findAll()` on piisav. `region` tabel vajas `ORDER BY sequence_number` — seetõttu oli seal custom `@Query`.

---

## Andmevoog — suur pilt

```
BROWSER (modal avab dropdown)
    ↓
FRONTEND (saadab GET päringu)
    ↓
AXIOS → GET /api/product-type
    ↓  ← siit algab backend
ProductTypeController.getProductTypes()
    ↓
ProductTypeService.getProductTypes()
    ↓
ProductTypeRepository.findAll()
    ↓
ANDMEBAAS (SELECT * FROM product_type)
    ↓ List<ProductType> entity lista
ProductTypeService
    ↓
ProductTypeMapper.toProductTypeResponseDtos()
    ↓ List<ProductTypeResponseDto>
ProductTypeController
    ↓ JSON massiiv
BROWSER
```

---

## BACKEND

### 1. Pakettstruktuur — kus uued failid asuvad

```
controller/
  producttype/
    ProductTypeController.java          ← HTTP endpoint
    dto/
      ProductTypeResponseDto.java       ← mida frontendile tagastatakse

persistence/
  producttype/
    ProductType.java                    ← DB tabeli peegelpilt (oli juba olemas)
    ProductTypeRepository.java          ← DB päringud
    ProductTypeMapper.java              ← teisendused

service/
  ProductTypeService.java               ← äriloogika
```

---

### 2. ProductTypeResponseDto — mida frontendile tagastatakse

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductTypeResponseDto {
    private Integer productTypeId;
    private String productTypeName;
}
```

Kaks välja: `productTypeId` (tabeli `id`) ja `productTypeName` (tabeli `product_type_name`).

---

### 3. ProductTypeRepository — miks findAll() piisab

```java
public interface ProductTypeRepository extends JpaRepository<ProductType, Integer> {
}
```

**Tühi repository — pole custom meetodeid.** JPA `findAll()` tagastab kõik read ilma järjestuseta. Tootegruppide puhul pole järjestus kriitiline — dropdown näitab kõiki ja kasutaja valib ise.

**Võrdle regioniga:**

| | RegionRepository | ProductTypeRepository |
|--|------------------|-----------------------|
| Kood | `@Query("select r from Region r order by r.sequenceNumber")` | tühi — `findAll()` piisab |
| Miks | Piirkonnad peavad olema kindlas järjekorras | Tootegrupid — järjestus pole oluline |

---

### 4. ProductTypeMapper — entity → DTO teisendus

```java
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductTypeMapper {

    @Mapping(source = "id", target = "productTypeId")
    @Mapping(source = "productTypeName", target = "productTypeName")
    ProductTypeResponseDto toProductTypeResponseDto(ProductType productType);

    List<ProductTypeResponseDto> toProductTypeResponseDtos(List<ProductType> productTypes);
}
```

**Kaks tähelepanekut:**

1. `id` → `productTypeId` — nimed erinevad, seega vajab eksplitsiitset `@Mapping`-ut
2. `productTypeName` → `productTypeName` — nimed kattuvad, aga kirjutame ikkagi välja — projekti tava on kõik väljad eksplitsiitselt nähtavaks teha

---

### 5. ProductTypeService — äriloogika

```java
@Service
@RequiredArgsConstructor
public class ProductTypeService {
    private final ProductTypeRepository productTypeRepository;
    private final ProductTypeMapper productTypeMapper;

    public List<ProductTypeResponseDto> getProductTypes() {
        List<ProductType> productTypes = productTypeRepository.findAll();
        return productTypeMapper.toProductTypeResponseDtos(productTypes);
    }
}
```

**Kahe- vs üherealine stiil:**

Mõlemad variandid on õiged — projekti koodis kasutatakse mõlemat:

```java
// Kaherealine (loetavam, muutuja nimega)
public List<ProductTypeResponseDto> getProductTypes() {
    List<ProductType> productTypes = productTypeRepository.findAll();
    return productTypeMapper.toProductTypeResponseDtos(productTypes);
}

// Üherealine (kompaktsem)
public List<ProductTypeResponseDto> getProductTypes() {
    return productTypeMapper.toProductTypeResponseDtos(productTypeRepository.findAll());
}
```

Vali see mis tundub loetavam — mõlemad kompileeruvad ja töötavad täpselt samamoodi.

---

### 6. ProductTypeController — HTTP endpoint

```java
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProductTypeController {
    private final ProductTypeService productTypeService;

    @GetMapping("/product-type")
    @Operation(summary = "Kõik tootegrupid")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tootegrupid tagastatud"),
            @ApiResponse(responseCode = "500", description = "Serveri viga",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))})
    public List<ProductTypeResponseDto> getProductTypes() {
        return productTypeService.getProductTypes();
    }
}
```

---

### Levinud viga — 200 vs 201

Selles taskis tehti viga, mis on väga tüüpiline:

```java
// VALE — 201 tähendab "loodud" (POST päringutel)
@ApiResponse(responseCode = "201", description = "Teenustasu lisatud")

// ÕIGE — 200 tähendab "päring õnnestus" (GET päringutel)
@ApiResponse(responseCode = "200", description = "Tootegrupid tagastatud")
```

**Rusikareegel HTTP staatusekoodidega:**

| Kood | Tähendus | Millal kasutada |
|------|----------|----------------|
| `200` | OK | GET — andmed tagastatud |
| `201` | Created | POST — uus ressurss loodud |
| `400` | Bad Request | Vigane sisend |
| `404` | Not Found | Ressurssi ei leitud |
| `409` | Conflict | Konflikt (nt duplikaat) |
| `500` | Server Error | Ootamatu viga serveris |

---

## Kokkuvõte

| Samm | Fail | Mida tegime |
|------|------|-------------|
| 1 | `ProductTypeResponseDto.java` | Lõime DTO kahe väljaga: `productTypeId`, `productTypeName` |
| 2 | `ProductTypeRepository.java` | Tühi repository — `findAll()` piisab, järjestust ei vajata |
| 3 | `ProductTypeMapper.java` | Lõime mapperi entity → DTO teisenduseks |
| 4 | `ProductTypeService.java` | Lõime service mis kutsub repository + mapper |
| 5 | `ProductTypeController.java` | Lõime kontrolleri `GET /api/product-type` endpointiga |

**Peamine õppetund:** Sama voog mis `GET /api/region` — Controller → Service → Repository → Mapper → tagasi. Erinevus: `findAll()` vs custom `@Query` sõltub sellest, kas järjestust on vaja või mitte.

---

## Järgmised sammud

- `POST /api/seller/{sellerId}/commission-rates` — teenustasu lisamine edasimüüjale (kasutab seda tootegruppide dropdowni)