# ETAS — Kordamismaterjal

> Spring Boot + Vue.js projekti kõikide seniste taskide koondülevaade.
> Iga kontseptsioon on selgitatud üks kord ja katab kõik selle mustri kasutuskohad projektis.

---

## Sisukord

1. [Projekti ülevaade](#1-projekti-ülevaade)
2. [Üldine arhitektuurimuster](#2-üldine-arhitektuurimuster)
3. [Entity ja andmemudel](#3-entity-ja-andmemudel)
4. [DTO — andmelepping](#4-dto--andmelepping)
5. [Mapper (MapStruct)](#5-mapper-mapstruct)
6. [Repository — andmebaasiga suhtlemine](#6-repository--andmebaasiga-suhtlemine)
7. [Service — äriloogika](#7-service--äriloogika)
8. [Controller — HTTP uks](#8-controller--http-uks)
9. [CRUD mustrid](#9-crud-mustrid)
   - [GET — lugemine](#91-get--lugemine)
   - [POST — loomine](#92-post--loomine)
   - [PUT — muutmine](#93-put--muutmine)
   - [DELETE — kustutamine](#94-delete--kustutamine)
10. [Veahaldus](#10-veahaldus)
11. [Erisused ja täiendavad lahendused](#11-erisused-ja-täiendavad-lahendused)
12. [ReportsView — frontend erimustrid](#12-reportsview--frontend-erimustrid)
13. [Mis erineb bank40 projektist](#13-mis-erineb-bank40-projektist)

---

## 1. Projekti ülevaade

**ETAS** — Edasimüüja Teenustasu Arvutussüsteem.

| Kiht | Tehnoloogia |
|------|-------------|
| Frontend | Vue.js 3 (Options API), Pinia, Vue Router 5, Axios, Bootstrap 5 |
| Backend | Spring Boot 4, Java 21, Spring Data JPA, MapStruct 1.6, Lombok |
| Andmebaas | PostgreSQL 17, schema `etas` |
| API dokument | Springdoc OpenAPI (Swagger UI `/swagger-ui/index.html`) |
| SQL logimine | p6spy (arenduses) |
| Excel | Apache POI (import + eksport) |

**Käivitamine:**
```
backend:  ./gradlew bootRun  → port 8080
frontend: npm run dev        → port 8081
```

**Pakettstruktuur (backend):**
```
ee.valiit.etas/
  controller/{domain}/      ← HTTP kiht
  controller/{domain}/dto/  ← andmekandja klassid
  service/                  ← äriloogika
  persistence/{domain}/     ← entity + repository + mapper
  infrastructure/           ← veahaldus (RestExceptionHandler, ApiError, ErrorResponse)
  Status.java               ← ühine staatuse enum
```

**Kaks kasutajarolli:**
- `A` (Admin) — haldab edasimüüjaid, kasutajaid, teenustasusid, KM määra
- `U` (User) — vaatab andmeid, impordib Exceli, vaatab aruandeid

---

## 2. Üldine arhitektuurimuster

Iga HTTP päring läbib sama ahela:

```
BROWSER → Vue komponent → axios → Controller → Service → Repository → DB
                                                                   ↓
BROWSER ← Vue komponent ← axios ← Controller ← Service ← Mapper ← DB
```

**Iga kihi roll:**

| Kiht | Vastutus | Näide |
|------|----------|-------|
| Controller | Võtab HTTP päringu vastu, tagastab vastuse | `@GetMapping`, `@PathVariable` |
| Service | Äriloogika — valideerib, otsustab, koordineerib | `validateUserIsAdmin()`, `createAndSave*()` |
| Repository | Räägib andmebaasiga — JPQL päringud | `findAllSellers()`, `deleteById()` |
| Mapper | Teisendab Entity ↔ DTO | `toSellerDto()`, `toSeller()` |
| DTO | Andmelepping — täpselt see info mis API-s liigub | `SellerDto`, `SellerDetailResponseDto` |
| Entity | Andmebaasi tabeli peegeldus Java objektina | `Seller`, `SellerRegion` |

**Dependency injection:**
```java
@Service
@RequiredArgsConstructor          // ← Lombok loob konstruktori automaatselt
public class SellerService {
    private final SellerRepository sellerRepository;   // Spring süstib automaatselt
    private final SellerMapper sellerMapper;
    private final ValidationService validationService;
}
```

---

## 3. Entity ja andmemudel

```java
@Getter @Setter
@Entity
@Table(name = "seller", schema = "etas")
public class Seller {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "status", nullable = false)
    private String status;          // "A" või "D" — mitte enum, vaid String

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;         // FK seos — objekt, mitte Integer
}
```

**Reeglid:**
- Lombok: `@Getter @Setter` (mitte `@Data` — Entity-l on identiteediprobleemid `equals/hashCode`-ga)
- Status: `String` väli (väärtused `"A"`/`"D"`), mitte Java enum — DB ühilduvuse tõttu
- FK seosed: `FetchType.LAZY` — laetakse ainult siis kui küsitakse (jõudlus)

### Status enum — kolm kihti

Edasimüüja ja kasutaja staatusel on kolm erinevat esitusviisi:

```
DB tabel  →  API JSON   →  UI eesti keeles
"A"       →  "ACTIVE"   →  "Aktiivne"
"D"       →  "INACTIVE" →  "Peatatud"
```

```java
public enum Status {
    ACTIVE("A", "ACTIVE"),
    SOFT_DELETED("D", "INACTIVE");

    private final String code;      // salvestatakse DB-sse
    private final String apiValue;  // saadetakse JSON-is

    // DB kood → API väärtus (GET vastustes)
    public static String toApiValue(String code) { ... }

    // API väärtus → Status enum (PUT staatuse muutmisel)
    public static Status fromApiValue(String apiValue) { ... }
}
```

Mapper kasutab `expression` annotatsiooni teisendamiseks:
```java
@Mapping(expression = "java(Status.toApiValue(seller.getStatus()))", target = "status")
```

---

## 4. DTO — andmelepping

**Miks DTO, mitte otse Entity?**
Entity sisaldab kõiki DB välju (sh `createdBy`, `status` raw kood, `createdAt`). DTO on "turvaline versioon" — ainult see, mida API vajab.

**Kahte tüüpi DTO-sid:**

| Tüüp | Suund | Näide |
|------|-------|-------|
| Sisend (Request) | Frontend → Backend | `SellerDto`, `SellerRegionDto` |
| Vastus (Response) | Backend → Frontend | `SellerDetailResponseDto`, `ReportResponseDto` |

```java
@Data @NoArgsConstructor @AllArgsConstructor
public class SellerDto {
    @NotBlank                    // ← validatsioon — tühi väli → 400 kohe
    private String companyName;

    @NotNull
    private Integer orgId;

    private String contractStart; // ISO formaat "2024-01-01" sisendis
    private String contractEnd;   // null = tähtajatu leping
    private String notes;
}
```

**Kuupäeva formaat:**
- Sisend (POST/PUT): `"2024-01-01"` — ISO, `LocalDate.parse()` vaikimisi
- Vastus (GET): `"01.01.2024"` — eesti formaat, mapper teisendab

**Rahasummad:**
```java
// double — VALE: 0.1 + 0.2 = 0.30000000000000004
// BigDecimal — ÕIGE: täpne kümnendaritmeetika
private BigDecimal feePerTransaction;
```

---

## 5. Mapper (MapStruct)

Mapper teisendab automaatselt Entity ↔ DTO. MapStruct genereerib koodi kompileerimisel.

```java
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        imports = {Status.class})
public interface SellerMapper {

    // Entity → DTO (GET vastus)
    @Mapping(source = "id", target = "sellerId")
    @Mapping(source = "companyName", target = "companyName")
    @Mapping(expression = "java(Status.toApiValue(seller.getStatus()))", target = "status")
    SellerDetailResponseDto toSellerDetailResponseDto(Seller seller);

    // Lista teisendus — automaatne, kasutab ülemist meetodit
    List<SellerDetailResponseDto> toSellerDetailResponseDtos(List<Seller> sellers);

    // DTO → Entity (POST loomine)
    @Mapping(ignore = true, target = "id")
    @Mapping(ignore = true, target = "createdBy")     // seame service-s käsitsi
    @Mapping(expression = "java(Status.ACTIVE.getCode())", target = "status")
    Seller toSeller(SellerDto sellerDto);

    // DTO → Entity (PUT muutmine) — null välju ignoreerib
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(ignore = true, target = "id")
    @Mapping(ignore = true, target = "status")
    void updateSeller(SellerDto sellerDto, @MappingTarget Seller seller);
}
```

**Olulised annotatsioonid:**

| Annotatsioon | Tähendus |
|---|---|
| `@Mapping(source, target)` | Kaardistab välja ühest nimest teise |
| `@Mapping(ignore = true)` | Jätab välja vahele — seame käsitsi |
| `@Mapping(expression = "java(...)")` | Täiskood teisenduseks |
| `@MappingTarget` | PUT: ära loo uut objekti, muuda olemasolevat |
| `NullValuePropertyMappingStrategy.IGNORE` | PUT: kui DTO väli on null, jäta entity väärtus |

**FK objekt mapperis:**
```java
// Seotud entity (nt region) peab olema eraldi laetud
@Mapping(source = "region.regionName", target = "regionName")
// MapStruct genereerib: dto.setRegionName(entity.getRegion().getRegionName())
// → eeldab, et region on mälus (vt join fetch)
```

---

## 6. Repository — andmebaasiga suhtlemine

```java
public interface SellerRepository extends JpaRepository<Seller, Integer> {

    // Kohandatud päring — JPQL (Java objekte, mitte SQL tabeleid)
    @Query("select s from Seller s order by s.companyName")
    List<Seller> findAllSellers();

    // Olemasolu kontroll — kiirem kui findById (loeb ainult ID)
    @Query("select (count(s) > 0) from Seller s where s.orgId = :orgId")
    boolean existsByOrgId(Integer orgId);

    // Kirjutuspäring — @Modifying kohustuslik
    @Modifying
    @Transactional
    @Query("delete from SalesReportDetail s where s.salesReport.id = :salesReportId")
    void deleteAllBy(Integer salesReportId);
}
```

**`existsById` vs `findById`:**

| | `existsById` | `findById` |
|--|---|---|
| Tagastab | `boolean` | `Optional<Entity>` |
| Kasuta kui | ainult kontrollid kas olemas | vajad hiljem objekti andmeid |
| SQL | `SELECT COUNT(*) ... LIMIT 1` | `SELECT * FROM ...` |

**`join fetch` — miks oluline:**

Lazy seosed (`FetchType.LAZY`) laaditakse ainult siis kui küsitakse. Kui Hibernate sessioon on suletud ja mapper proovib seotud objekti lugeda → `LazyInitializationException`.

```java
// PROBLEEM: mapper proovib region.regionName lugeda, aga sessioon kinni
@Query("select sr from SellerRegion sr where sr.seller.id = :sellerId")

// LAHENDUS: lae region koos seller_region-iga
@Query("select sr from SellerRegion sr join fetch sr.region where sr.seller.id = :sellerId")
// SQL: SELECT sr.*, r.* FROM seller_region sr JOIN region r ON r.id = sr.region_id
```

---

## 7. Service — äriloogika

```java
@Service
@RequiredArgsConstructor
public class SellerRegionService {
    private final SellerRegionRepository sellerRegionRepository;
    private final SellerRegionMapper sellerRegionMapper;
    private final ValidationService validationService;  // jagatud valideerimisservice

    // Avalikud meetodid ees — neid kutsub Controller
    @Transactional
    public void addSellerRegion(Integer userId, Integer sellerId, SellerRegionDto dto) {
        validationService.validateUserIsAdmin(userId);
        Seller seller = getSellerById(sellerId);
        Region region = getRegionById(dto.getRegionId());
        validateSellerRegionNotDuplicate(sellerId, region.getId());
        createAndSaveSellerRegion(seller, region, dto);
    }

    // Privaatsed meetodid pärast — väljakutsumise järjekorras
    private Seller getSellerById(Integer sellerId) { ... }
    private Region getRegionById(Integer regionId) { ... }
    private void validateSellerRegionNotDuplicate(...) { ... }
    private void createAndSaveSellerRegion(...) { ... }
}
```

**Meetodite järjekord:** avalikud ees, privaatsed pärast, privaatsed väljakutsumise hierarhia järjekorras.

**Valideerimise järjekord (alati sama):**
1. Rolli kontroll (`403 Forbidden`) — pole mõtet edasi minna kui kasutajal pole õigust
2. Andmete olemasolu (`404 Not Found`) — kas viidatavad kirjed on olemas
3. Loogiline kontroll (`409 Conflict`) — duplikaat, juba sama staatus vms

**`@Transactional` kirjutusoperatsioonidel:**
Kõik DB operatsioonid meetodis on üks tervik. Vea korral kõik tühistatakse — andmebaas jääb puhtasse olekusse.

**ValidationService — jagatud valideerimisloogika:**

`validateUserIsAdmin` ja `validateSellerExists` olid korratud igas service klassis. Eraldati eraldi teenusesse:

```java
@Service
@RequiredArgsConstructor
public class ValidationService {
    private final UserRepository userRepository;
    private final SellerRepository sellerRepository;

    public void validateUserIsAdmin(Integer userId) {
        User user = userRepository.findById(userId).orElseThrow(...);
        if (!"A".equals(user.getUserRole())) {
            throw new ForbiddenException(ACCESS_DENIED.getMessage(), ACCESS_DENIED.getErrorCode());
        }
    }

    public void validateSellerExists(Integer sellerId) {
        if (!sellerRepository.existsById(sellerId)) {
            throw new DataNotFoundException(SELLER_NOT_FOUND.getMessage(), SELLER_NOT_FOUND.getErrorCode());
        }
    }
}
```

---

## 8. Controller — HTTP uks

```java
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SellerRegionController {
    private final SellerRegionService sellerRegionService;

    @GetMapping("/seller/{sellerId}/regions")
    @Operation(summary = "Edasimüüja piirkonnad")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Edasimüüjat ei leitud",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public List<SellerRegionResponseDto> getSellerRegions(@PathVariable Integer sellerId) {
        return sellerRegionService.findSellerRegions(sellerId);
    }
}
```

**URL parameetrite tüübid:**

| Annotatsiooon | Näide URL | Kasutuskoht |
|---|---|---|
| `@PathVariable` | `/seller/{sellerId}` | ressursi identifitseerimine |
| `@RequestParam` | `?userId=1` | lisaparameeter (nt õiguste kontroll) |
| `@RequestBody` | JSON body | POST/PUT andmed |

**Meetodite järjekord kontrolleris:** GET → POST → PUT → DELETE (REST konventsioon).

**Kõik veavastused `ApiError` skeemiga:**
```java
content = @Content(schema = @Schema(implementation = ApiError.class))
```

---

## 9. CRUD mustrid

### 9.1 GET — lugemine

**Nimekiri (kõik kirjed):**
```
Controller.getAll() → Service.findAll() → Repository.findAll() → Mapper.toDtos()
```

**Üksik kirje (ID järgi):**
```
Controller.getOne() → Service.findOne() → Repository.findById().orElseThrow() → Mapper.toDto()
```

**Filtritega nimekiri (query parameetrid):**
```java
@GetMapping("/report/user/{userId}")
public List<ReportResponseDto> getReports(
        @PathVariable Integer userId,
        @RequestParam(required = false) String periodFrom,
        @RequestParam(required = false) String periodTo,
        @RequestParam(required = false) Integer sellerId) { ... }
```

**N+1 päring (aktsepteeritav väikeste koguste puhul):**
```java
// Kontaktide rollid tulevad eraldi tabelist — 1 päring kontaktidele + N päringut rollidele
for (SellerContact contact : contacts) {
    SellerContactResponseDto dto = sellerContactMapper.toSellerContactResponseDto(contact);
    dto.setRoles(sellerContactRoleRepository.findRoleCodesBy(contact.getId()));  // eraldi päring
    result.add(dto);
}
// 3 kontakti → 4 päringut kokku. Aktsepteeritav kui N on väike.
```

---

### 9.2 POST — loomine

```
Controller → Service (valideeri) → Mapper (DTO→Entity) → Service (FK käsitsi) → Repository.save()
```

**Service loogika:**
```java
@Transactional
public void addSellerRegion(Integer userId, Integer sellerId, SellerRegionDto dto) {
    validationService.validateUserIsAdmin(userId);            // 1. õigused → 403
    Seller seller = getSellerById(sellerId);                  // 2. seller olemas? → 404
    Region region = getRegionById(dto.getRegionId());         // 3. region olemas? → 404
    validateSellerRegionNotDuplicate(sellerId, region.getId()); // 4. duplikaat? → 409
    createAndSaveSellerRegion(seller, region, dto);           // 5. salvesta
}

private void createAndSaveSellerRegion(Seller seller, Region region, SellerRegionDto dto) {
    SellerRegion sellerRegion = sellerRegionMapper.toSellerRegion(dto); // salesPointCount
    sellerRegion.setSeller(seller);   // FK objekt käsitsi — mapper ei saa ID-st objekti teha
    sellerRegion.setRegion(region);   // FK objekt käsitsi
    sellerRegionRepository.save(sellerRegion);
}
```

**Miks FK objektid käsitsi?**
Mapper teisendab ainult primitiivseid väärtusi. `regionId` (Integer) → `Region` (objekt) nõuab andmebaasipäringut — see on service töö.

**Controller:**
```java
@PostMapping("/seller/{sellerId}/regions")
@ResponseStatus(HttpStatus.CREATED)   // ← 201, mitte vaikimisi 200
public void addSellerRegion(...) { ... }
```

---

### 9.3 PUT — muutmine

```
Controller → Service (valideeri) → Repository.findById() → Mapper.update(@MappingTarget) → Repository.save()
```

**Mapper PUT jaoks:**
```java
@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
@Mapping(ignore = true, target = "id")
@Mapping(ignore = true, target = "status")
void updateSeller(SellerDto dto, @MappingTarget Seller seller);
```

`@MappingTarget` — ära loo uut objekti, muuda olemasolevat.
`IGNORE` — kui DTO väli on `null`, jäta entity olemasolev väärtus.

**Service loogika:**
```java
@Transactional
public void updateSeller(Integer userId, Integer sellerId, SellerDto dto) {
    validationService.validateUserIsAdmin(userId);
    Seller seller = sellerRepository.findById(sellerId).orElseThrow(...);  // laeme muutmiseks
    sellerMapper.updateSeller(dto, seller);  // muudame in-place
    sellerRepository.save(seller);
}
```

**Staatuse muutmine eraldi endpoint:**
```java
// PUT /seller/{sellerId}/status — ainult status muutub
@Transactional
public void updateSellerStatus(Integer userId, Integer sellerId, SellerStatusDto dto) {
    validationService.validateUserIsAdmin(userId);
    Seller seller = sellerRepository.findById(sellerId).orElseThrow(...);
    String newStatus = Status.fromApiValue(dto.getStatus()).getCode(); // "INACTIVE" → "D"
    validateStatusChange(seller.getStatus(), newStatus);               // juba sama? → 409
    seller.setStatus(newStatus);
    sellerRepository.save(seller);
}
```

---

### 9.4 DELETE — kustutamine

**Ühe tabeli kustutamine (lihtne — pole FK sõltuvusi):**
```java
@Transactional
public void deleteSellerRegion(Integer userId, Integer sellerId, Integer regionId) {
    validationService.validateUserIsAdmin(userId);
    validationService.validateSellerExists(sellerId);
    validateSellerRegionExists(regionId);
    sellerRegionRepository.deleteById(regionId);  // JpaRepository valmismeetod
}
```

`deleteById()` tuleb `JpaRepository`-st automaatselt — pole JPQL kirjutamist vaja.

**Mitme tabeli kustutamine (FK piirangud):**

Kui kustutataval tabelil on alamtabeleid, tuleb järjekorda järgida — **lapsest vanemani**:

```
sales_report (vanem)
  ├── commission_calculation (laps)
  └── sales_report_detail (laps)
```

```java
@Transactional
public void deleteReport(Integer userId, String period) {
    validationService.validateUserIsAdmin(userId);
    SalesReport salesReport = salesReportRepository.findByPeriod(period).orElseThrow(...);
    commissionCalculationRepository.deleteAllBy(salesReport.getId());  // 1. laps
    salesReportDetailRepository.deleteAllBy(salesReport.getId());      // 2. laps
    salesReportRepository.delete(salesReport);                          // 3. vanem
}
```

**Repositoorium kirjutuspäringuks:**
```java
@Modifying           // ütleb Spring Data-le: pole SELECT
@Transactional       // jakarta.transaction — repositooriumis OK
@Query("delete from SalesReportDetail s where s.salesReport.id = :salesReportId")
void deleteAllBy(Integer salesReportId);
```

**Enne kustutamist on kontakti puhul vaja ka rollid kustutada:**
```java
// seller_contact_role viitab seller_contact-ile → esmalt rollid
sellerContactRoleRepository.deleteAllBySellerContactId(contactId);
sellerContactRepository.deleteById(contactId);
```

---

## 10. Veahaldus

### Exception tüübid

| Exception klass | HTTP staatus | Millal |
|---|---|---|
| `BadRequestException` | 400 Bad Request | Vigased sisendandmed (nt valed Excel päised) |
| `ForbiddenException` | 403 Forbidden | Kasutajal pole õigust (pole Admin) |
| `DataNotFoundException` | 404 Not Found | Kirjet andmebaasis ei leitud |
| `ConflictException` | 409 Conflict | Loogiline konflikt (duplikaat, juba sama staatus) |

Kõik laiendavad `RuntimeException`-i ja registreeritakse `RestExceptionHandler`-is:
```java
@ExceptionHandler
public ResponseEntity<ApiError> handleConflictException(ConflictException ex) {
    ApiError error = new ApiError();
    error.setMessage(ex.getMessage());
    error.setErrorCode(ex.getErrorCode());
    return new ResponseEntity<>(error, HttpStatus.CONFLICT);
}
```

### ErrorResponse enum

Kõik veakoodid on koondatud ühte enum-i:
```java
public enum ErrorResponse {
    USER_NOT_FOUND("Kasutajat ei leitud", 111),
    ACCESS_DENIED("Juurdepääs keelatud", 115),
    SELLER_NOT_FOUND("Edasimüüjat ei leitud", 211),
    SELLER_ORG_ID_ALREADY_EXISTS("Selle org ID-ga edasimüüja on juba olemas", 212),
    SELLER_REGION_NOT_FOUND("Piirkonda ei leitud", 321),
    SELLER_REGION_ALREADY_EXISTS("See piirkond on juba lisatud", 322),
    IMPORT_INVALID_HEADER("Faili päised ei vasta nõutud formaadile", 511),
    IMPORT_PERIOD_ALREADY_EXISTS("Sellel perioodil on aruanne juba olemas", 512),
    IMPORT_PERIOD_NOT_FOUND("Perioodi andmeid ei leitud", 515);
    // ...
}
```

Veakoodid on grupeeritud (111 = user, 211 = seller, 321 = region, 511 = import).

### Kuidas veavastus liigub

```
Service: throw new ConflictException("See piirkond on juba lisatud", 322)
                        ↓
RestExceptionHandler püüab → moodustab ApiError
                        ↓
HTTP 409 + { "message": "See piirkond on juba lisatud", "errorCode": 322 }
                        ↓
Frontend: error.response.data.message → kuvatakse kasutajale
```

---

## 11. Erisused ja täiendavad lahendused

### 11.1 SQL VIEW kui entity

Teenustasude arvutuseks on loodud andmebaasi vaade `commission_calculation_view` mis ühendab mitme tabeli andmeid ja teeb arvutused SQL-is.

Backendis on vaade kaardistatud nagu tavaline entity — `@Entity` + `@Table`:

```java
@Getter
@Entity
@Table(name = "commission_calculation_view", schema = "etas")
@Immutable       // ← kirjutamine pole lubatud — ainult lugemine
public class CommissionCalculationView {
    @Id
    private Integer id;
    private BigDecimal calculatedFee;
    private BigDecimal vatAmount;
    private BigDecimal calculatedFeePlusVat;
    // ...
}
```

Repository töötab täpselt samamoodi kui tavaliste entiteetidega.

**Miks SQL VIEW, mitte Java arvutus?**
Arvutus sisaldab keerulisi JOIN-e mitme tabeli vahel ja agregaate. SQL-is on see efektiivsem — andmebaas teeb arvutuse enne kui Java üldse andmeid näeb.

### 11.2 Excel import (Apache POI)

`MultipartFile` — Spring võtab üleslaetava faili vastu:

```java
@PostMapping("/import/user/{userId}")
@ResponseStatus(HttpStatus.CREATED)
public void addReport(@PathVariable Integer userId,
                      @RequestParam("file") MultipartFile file) { ... }
```

Faili lugemine ja päiste valideerimine:
```java
try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
    Sheet sheet = workbook.getSheetAt(0);
    Map<String, Integer> headerMap = buildHeaderMap(sheet.getRow(0));
    validateHeaders(headerMap);   // BadRequestException kui päised valed → 400
    List<SalesReportRowDto> rows = parseRows(sheet, headerMap);
    // ...
}
```

Valideerimiste järjekord service-s:
```java
public void addReport(Integer userId, MultipartFile file) {
    validationService.validateUserIsAdmin(userId);  // 1. õigused → 403
    // ... faili avamine ...
    validateHeaders(headerMap);                     // 2. formaadi kontroll → 400
    validatePeriodIsAvailable(period);              // 3. duplikaat → 409
}
```

### 11.3 Excel eksport

Eksport genereerib faili mälus (`ByteArrayOutputStream`) ja tagastab `byte[]`:

```java
public byte[] exportReports(...) {
    List<ReportResponseDto> reports = getReports(...);  // taaskasutus — pole duplikaati
    try (XSSFWorkbook workbook = new XSSFWorkbook();
         ByteArrayOutputStream out = new ByteArrayOutputStream()) {
        Sheet sheet = workbook.createSheet("Aruanne");
        createExportHeaderRow(sheet);
        for (int i = 0; i < reports.size(); i++) {
            createExportDataRow(sheet, i + 1, reports.get(i));
        }
        createExportTotalsRow(sheet, reports);
        workbook.write(out);
        return out.toByteArray();
    }
}
```

Kontrolleris `ResponseEntity<byte[]>` koos HTTP päistega:
```java
public ResponseEntity<byte[]> exportReports(...) {
    byte[] file = reportControllerService.exportReports(...);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.parseMediaType(
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    headers.setContentDispositionFormData("attachment", "aruanne.xlsx");
    return new ResponseEntity<>(file, headers, HttpStatus.OK);
}
```

| HTTP päis | Tähendus |
|---|---|
| `Content-Type` | Faili tüüp — brauser teab, et on `.xlsx` |
| `Content-Disposition: attachment` | Brauser laeb alla, ei kuva |
| `filename="aruanne.xlsx"` | Faili nimi allalaadimisel |

### 11.4 Frontend põhimuster

```javascript
// api-services/SellerService.js — kõik API päringud ühes kohas
export default {
  sendGetSellers(userId)        { return axios.get(`/api/seller/user/${userId}`) },
  sendPostSeller(userId, data)  { return axios.post(`/api/seller/user/${userId}`, data) },
  sendPutSeller(sellerId, data) { return axios.put(`/api/seller/${sellerId}`, data) },
  sendDeleteRegion(sellerId, regionId, userId) {
    return axios.delete(`/api/seller/${sellerId}/regions/${regionId}`, { params: { userId } })
  }
}
```

```javascript
// Vue komponent — beforeMount laeb andmed lehe avamisel
beforeMount() {
  this.loadSellers()
},
methods: {
  loadSellers() {
    SellerService.sendGetSellers(AuthService.getUserId())
      .then(response => { this.sellers = response.data })
      .catch(error   => { this.errorMessage = error.response.data.message })
  }
}
```

---

## 12. ReportsView — frontend erimustrid

ReportsView on kõige keerukam vaade projektis — ühel lehel on mitu iseseisvat sektsiooni (import, kustuta, filter, tabel, eksport). Siin kasutatakse mitmeid mustreid mida mujal pole vaja olnud.

### 12.1 Mitu iseseisvat spinnerit ja veateadet

Varasemalt oli vaates üks `errorMessage` ja üks `showSpinner`. ReportsView-s on igal sektsioonil omad:

```javascript
data() {
  return {
    showImportSpinner: false,    importErrorMessage: '',  importSuccessMessage: '',
    showDeleteSpinner: false,    deleteErrorMessage: '',
    showDetailSpinner: false,    detailErrorMessage: '',
    showExportSpinner: false,    exportErrorMessage: '',
    reportsErrorMessage: '',
  }
}
```

**Miks eraldi?** Import spinner ei tohi mõjutada ekspordi spinner olekut. Iga sektsiooni vea- ja laadimisseisund on sõltumatu.

### 12.2 Perioodi formaadi moodustamine

Backend ootab perioodi kujul `"2026-4"` (YYYY-M). Frontend hoiab kuu ja aasta eraldi `data()` väljades ning moodustab stringi vahetult enne päringu saatmist:

```javascript
const periodFrom = this.fromYear && this.fromMonth
  ? this.fromYear + '-' + this.fromMonth
  : null

// Kustutamisel:
const period = this.deleteYear + '-' + this.deleteMonth   // nt "2026-4"
```

### 12.3 window.confirm — kustutamise kinnitusdialoog

Enne pöördumatut kustutamist küsitakse kasutajalt kinnitus brauseri natiivsest dialoogist:

```javascript
confirmAndDelete() {
  if (!window.confirm('Kas oled kindel, et soovid perioodi '
      + this.deleteMonth + '.' + this.deleteYear + ' andmed kustutada?')) return
  // kasutaja vajutas "OK" → jätka kustutamisega
  this.showDeleteSpinner = true
  ReportService.sendDeleteImportReport(userId, period)
    .then(...)
    .catch(error => { this.deleteErrorMessage = error.response?.data?.message ?? 'Kustutamine ebaõnnestus' })
    .finally(() => { this.showDeleteSpinner = false })
}
```

`?? 'Kustutamine ebaõnnestus'` — nullish coalescing: kui `error.response?.data?.message` on `null`/`undefined`, kasuta vaikeväärtust.

### 12.4 404 sõbraliku teatena (mitte navigeeri errori vaatesse)

Tavaliselt catch plokis näidatakse veateadet. Aga 404 aruannete puhul pole see "viga" — lihtsalt andmeid ei ole:

```javascript
.catch((error) => {
  if (error.response?.status === 404) {
    this.reportsErrorMessage = 'Valitud perioodil andmed puuduvad'  // sõbralik teade
  } else {
    this.reportsErrorMessage = 'Andmete laadimine ebaõnnestus'      // tehniline viga
  }
})
```

### 12.5 Laiendatav rida — expandedKey muster

Tabelis saab iga rea "Vaata" nupuga avada tootegruppide detailtabeli. Korraga on avatud maksimaalselt üks rida.

```javascript
data() {
  return {
    expandedKey: null,    // null = kõik suletud; '1-2026-4' = see rida avatud
    reportDetails: [],
  }
},
methods: {
  toggleDetail(sellerId, period) {
    const key = sellerId + '-' + period
    if (this.expandedKey === key) {
      this.expandedKey = null  // sama rida — sulge
      return
    }
    this.expandedKey = key     // uus rida — ava ja lae
    this.reportDetails = []
    this.showDetailSpinner = true
    ReportService.sendGetReportDetail(sellerId, period)
      .then(response => { this.reportDetails = response.data })
      .finally(() => { this.showDetailSpinner = false })
  }
}
```

Templateis kontrollitakse, kas rida on avatud:
```html
<template v-for="r in reports" :key="r.sellerId + '-' + r.period">
  <tr>
    <td>...</td>
    <td>
      <button @click="toggleDetail(r.sellerId, r.period)">
        {{ expandedKey === r.sellerId + '-' + r.period ? 'Sulge' : 'Vaata' }}
      </button>
    </td>
  </tr>
  <!-- Detailrida — nähtav ainult kui see rida on avatud -->
  <tr v-if="expandedKey === r.sellerId + '-' + r.period">
    <td colspan="7">
      <table>...</table>
    </td>
  </tr>
</template>
```

`<template v-for>` — kasutame `<template>` ümbrisena (mitte `<tr>`), sest ühe iteratsiooni kohta on vaja kahte `<tr>` elementi.

### 12.6 computed — KOKKU rida

KOKKU rida arvutatakse frontendis, mitte backendist:

```javascript
computed: {
  totals() {
    return {
      transactionCount: this.reports.reduce((sum, r) => sum + r.transactionCount, 0),
      salesAmount:      this.reports.reduce((sum, r) => sum + r.salesAmount, 0),
      feeAmount:        this.reports.reduce((sum, r) => sum + r.feeAmount, 0),
      vatAmount:        this.reports.reduce((sum, r) => sum + r.vatAmount, 0),
      totalFee:         this.reports.reduce((sum, r) => sum + r.totalFee, 0),
    }
  }
}
```

`computed` — Vue arvutab väärtuse automaatselt ümber iga kord kui `this.reports` muutub. Erinevus `methods`-ist: `computed` tulemust hoitakse cache-is, `methods` käivitatakse iga kord uuesti.

### 12.7 Faili allalaadimine — blob muster

Ekspordi päring erineb tavalisest JSON päringust — Axios peab saama binaari, mitte JSONi:

```javascript
// ReportService.js
sendGetReportExport(userId, periodFrom, periodTo, sellerId) {
  return axios.get('/api/report/user/' + userId + '/export', {
    params: { periodFrom, periodTo, sellerId },
    responseType: 'blob',   // ← kohustuslik, muidu Axios teisendab binaar JSONiks
  })
}
```

Vastuse käsitlemine — brauseri allalaadimise käivitamine programmiliselt:

```javascript
.then((response) => {
  const blob = new Blob([response.data], {
    type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
  })
  const url = window.URL.createObjectURL(blob)  // ajutine URL mälus olevale failile
  const link = document.createElement('a')
  link.href = url
  link.setAttribute('download', 'aruanne.xlsx') // faili nimi allalaadimisel
  document.body.appendChild(link)
  link.click()                                   // käivitab allalaadimise
  document.body.removeChild(link)
  window.URL.revokeObjectURL(url)                // vabasta mälu
})
```

| Samm | Miks |
|---|---|
| `new Blob(...)` | Binaarne andmeplokk koos MIME tüübiga |
| `createObjectURL` | Loob ajutise `blob:` URL-i mälus olevale failile |
| `link.click()` | Simuleerib kasutaja klikki — brauser alustab allalaadimist |
| `revokeObjectURL` | Vabastab mälu — URL ei ole enam vajalik |

---

## 13. Mis erineb bank40 projektist

Bank40 õpetas põhimustri: Controller → Service → Repository → Mapper → DTO. ETAS kordab sama mustrit, kuid lisab:

| Uus asi | Kus näed ETAS-is | Miks lisandus |
|---------|-----------------|---------------|
| **Status enum 3 kihiga** | `Status.java`, `SellerMapper`, `SellerService` | DB `"A"`/`"D"` ↔ API `"ACTIVE"`/`"INACTIVE"` ↔ UI "Aktiivne"/"Peatatud" — üks enum teisendab kõik |
| **ConflictException (409)** | `RestExceptionHandler`, `SellerService`, `SellerRegionService` | Unikaalsuse rikumine (orgId olemas, piirkond juba lisatud) — pole 400 ega 404, on loogiline konflikt |
| **BadRequestException (400)** | `RestExceptionHandler`, `ReportControllerService` | Vigased sisendandmed — erineb 403-st (pole õiguste probleem, on kliendi viga) |
| **DELETE mitmest tabelist** | `ReportControllerService.deleteReport()` | FK piirangud nõuavad lapsi enne vanemaid kustutada; `@Modifying` + `@Transactional` repositooriumis |
| **`@BeanMapping(IGNORE)`** | `SellerMapper.updateSeller()` | PUT mapper — null välja korral säilita olemasolev väärtus, ära kustuta |
| **`join fetch` JPQL-is** | `SellerRegionRepository` | Lazy seos — mapper vajab seotud entity andmeid pärast sessiooni sulgemist |
| **ValidationService** | `service/ValidationService.java` | `validateUserIsAdmin` + `validateSellerExists` olid korratud 5 service-s — DRY põhimõte |
| **SQL VIEW kui `@Entity`** | `CommissionCalculationView.java` | Keerulise arvutuse SQL-is hoidmine; `@Immutable` — ainult lugemine |
| **Excel import (Apache POI)** | `ReportControllerService.addReport()` | `MultipartFile` → `XSSFWorkbook` → read → DB; päiste valideerimine |
| **Excel eksport (`byte[]`)** | `ReportControllerService.exportReports()` | `ByteArrayOutputStream` + `ResponseEntity<byte[]>` + HTTP päised |
| **`@ResponseStatus(CREATED)`** | POST meetodid | POST peaks tagastama 201, mitte vaikimisi 200 — HTTP standard |
| **Veakoodide grupeeringud** | `ErrorResponse.java` | 111 = user, 211 = seller, 321 = region, 511 = import — hoiab koode organiseeritult |