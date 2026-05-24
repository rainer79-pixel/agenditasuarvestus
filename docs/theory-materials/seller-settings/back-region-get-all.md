# GET /api/region — kõik piirkonnad

## Mis see on ja miks oluline?

See endpoint tagastab kõik piirkonnad andmebaasist järjestatult. Seda kasutatakse `SellerSettingsRegionModal`-is dropdowni laadimiseks — Admin näeb nimekirja piirkondadest ja valib, millise edasimüüjale lisada.

**Päriselust analoogia:** Kujuta ette postkontorit kus on seinale riputatud nimekiri kõigist postiindeksitest. Iga kord kui keegi tahab aadressi valida, vaatab ta sellest nimekirjast. Nimekiri ei muutu — lihtsalt loetakse seina pealt.

---

## Mis on selles taskis uut?

See on esimene kord, kus loome **täiesti uue kontrolleri** — kõik neli faili on uued:

| Asi | Varasemad taskid | See task |
|-----|-----------------|---------|
| **Kontroller** | Lisasime meetodeid olemasolevasse | ✅ Uus `RegionController` |
| **Service** | Lisasime meetodeid olemasolevasse | ✅ Uus `RegionService` |
| **Repository** | Lisasime meetodeid olemasolevasse | ✅ Uus `RegionRepository` |
| **Mapper** | Lisasime meetodeid olemasolevasse | ✅ Uus `RegionMapper` |
| **Veahaldus** | Oli vaja lisada veakoodid | ❌ Pole spetsiifilist veahaldust |

---

## Andmevoog — suur pilt

```
BROWSER (modal avab dropdown)
    ↓
FRONTEND (saadab GET päringu)
    ↓
AXIOS → GET /api/region
    ↓  ← siit algab backend
RegionController.getRegions()
    ↓
RegionService.getRegions()
    ↓
RegionRepository.findAllRegions()
    ↓
ANDMEBAAS (SELECT * FROM region ORDER BY sequence_number)
    ↓ List<Region> entity lista
RegionService
    ↓
RegionMapper.toRegionResponseDtos()
    ↓ List<RegionResponseDto>
RegionController
    ↓ JSON massiiv
BROWSER
```

---

## BACKEND

### 1. Pakettstruktuur — kus uued failid asuvad

```
controller/
  region/
    RegionController.java          ← HTTP endpoint
    dto/
      RegionResponseDto.java       ← mida frontendile tagastatakse

persistence/
  region/
    Region.java                    ← DB tabeli peegelpilt (oli juba olemas)
    RegionRepository.java          ← DB päringud
    RegionMapper.java              ← teisendused

service/
  RegionService.java               ← äriloogika
```

---

### 2. RegionResponseDto — mida frontendile tagastatakse

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegionResponseDto {
    private Integer regionId;
    private String regionName;
}
```

**Miks mitte tagastada `Region` entity-t otse?**
`Region` entity-l on ka `sequenceNumber` väli — frontend seda ei vaja. DTO filtreerib välja ainult vajaliku. See kaitseb ka juhul kui entity muutub — frontend ei saa sellest teada.

---

### 3. RegionRepository — DB päring järjestusega

```java
public interface RegionRepository extends JpaRepository<Region, Integer> {

    @Query("select r from Region r order by r.sequenceNumber")
    List<Region> findAllRegions();
}
```

**Miks custom `@Query` ja mitte `findAll()`?**
`findAll()` ei garanteeri järjekorda — piirkonnad võivad tulla suvalises järjekorras. `sequence_number` veerg on andmebaasis just selleks, et tagada loogiline järjekord (Harju maakond enne Ida-Viru maakonda jne).

---

### 4. RegionMapper — entity → DTO teisendus

```java
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING)
public interface RegionMapper {

    @Mapping(source = "id", target = "regionId")
    @Mapping(source = "regionName", target = "regionName")
    RegionResponseDto toRegionResponseDto(Region region);

    List<RegionResponseDto> toRegionResponseDtos(List<Region> regions);
}
```

**Kaks tähelepanekut:**

1. `id` → `regionId` — nimed erinevad, seega vajab eksplitsiitset `@Mapping`-ut
2. `regionName` → `regionName` — nimed kattuvad, aga kirjutame ikkagi välja — projekti tava on kõik väljad eksplitsiitselt nähtavaks teha

**`toRegionResponseDtos` list-meetod** — MapStruct genereerib selle automaatselt `toRegionResponseDto` põhjal. Sa kirjutad ainult signatuuri, implementatsiooni genereerib MapStruct.

---

### 5. RegionService — äriloogika (siin väga lihtne)

```java
@Service
@RequiredArgsConstructor
public class RegionService {
    private final RegionRepository regionRepository;
    private final RegionMapper regionMapper;

    public List<RegionResponseDto> getRegions() {
        List<Region> regions = regionRepository.findAllRegions();
        return regionMapper.toRegionResponseDtos(regions);
    }
}
```

Selle endpointi puhul pole keerulist äriloogikat — lihtsalt loe andmebaasist ja teisenda DTO-ks.

---

### 6. RegionController — HTTP endpoint

```java
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RegionController {
    private final RegionService regionService;

    @GetMapping("/region")
    @Operation(summary = "Kõik piirkonnad")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "500", description = "Serveri viga",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))})
    public List<RegionResponseDto> getRegions() {
        return regionService.getRegions();
    }
}
```

**Miks ainult 200 ja 500 — teistes endpointides on rohkem?**
See endpoint ei tee valideerimist — ei kontrolli kasutaja õigusi ega otsita konkreetset rida. Lihtsalt loeb kõik read. Ainus mis valesti minna saab on serveri sisemine viga (500).

---

## Kokkuvõte

| Samm | Fail | Mida tegime |
|------|------|-------------|
| 1 | `RegionResponseDto.java` | Lõime DTO kahe väljaga: `regionId`, `regionName` |
| 2 | `RegionRepository.java` | Lõime repository custom päringuga `ORDER BY sequence_number` |
| 3 | `RegionMapper.java` | Lõime mapperi entity → DTO teisenduseks |
| 4 | `RegionService.java` | Lõime service mis kutsub repository + mapper |
| 5 | `RegionController.java` | Lõime kontrolleri `GET /api/region` endpointiga |

**Peamine õppetund:** GET endpoint ilma parameetriteta on lihtsaim võimalik backend ülesanne — Controller → Service → Repository → Mapper → tagasi. Uus asi võrreldes varasemate taskidega: kõik neli kihti loodi nullist.

---

## Järgmised sammud

- `POST /api/seller/{sellerId}/regions` — piirkonna lisamine edasimüüjale (kasutab seda dropdowni)
- `GET /api/product-type` — sama muster, tootegruppide nimekiri