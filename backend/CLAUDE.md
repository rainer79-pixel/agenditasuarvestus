# CLAUDE.md — Backend kodeerimistavad

> Kehtib koos juurkataloogis asuva `CLAUDE.md`-ga, kus on üldine projektiinfo.
> **Referentsprojekt:** `bank40back` (branch Rainer) — kasuta sama lähenemist kus võimalik. Kui ETAS vajab midagi uut, kasuta võimalikult lihtsat lahendust.

## Kodeerimistavad — Backend (Spring Boot)

### Pakettstruktuur
```
ee.valiit.etas/
  controller/{domain}/{Domain}Controller.java
  controller/{domain}/dto/{Domain}Dto.java
  service/{Domain}Service.java
  persistence/{domain}/{Domain}.java          ← JPA entity
  persistence/{domain}/{Domain}Repository.java
  persistence/{domain}/{Domain}Mapper.java
  infrastructure/RestExceptionHandler.java
  infrastructure/error/ApiError.java
  infrastructure/error/ErrorResponse.java     ← enum veakoodidega
  infrastructure/exception/{Type}Exception.java
  Status.java                                 ← enum (ACTIVE, SOFT_DELETED) — DB: "A"/"D", API: "ACTIVE"/"INACTIVE", UI: "Aktiivne"/"Peatatud"
```

### Entity tava
```java
@Getter @Setter @Entity @Table(name = "...", schema = "etas")
public class Foo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bar_id", nullable = false)
    private Bar bar;

    @Column(name = "status", nullable = false, length = Integer.MAX_VALUE)
    private String status;  // väärtus Status enum-ist
}
```
- Suhteväljad: `FetchType.LAZY`
- Status: `String` väli (mitte enum), väärtus `Status.ACTIVE.getCode()`
- Lombok: `@Getter @Setter` (mitte `@Data`)

### DTO tava
```java
@Data @NoArgsConstructor @AllArgsConstructor
public class FooResponseDto { ... }
```

### Mapper tava (MapStruct)
```java
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        imports = {Status.class})
public interface FooMapper {
    @Mapping(source = "id", target = "fooId")
    @Mapping(source = "bar.name", target = "barName")
    FooResponseDto toFooResponseDto(Foo foo);

    List<FooResponseDto> toFooResponseDtos(List<Foo> foos);

    @Mapping(ignore = true, target = "id")
    @Mapping(ignore = true, target = "bar")
    @Mapping(expression = "java(Status.ACTIVE.getCode())", target = "status")
    Foo toFoo(FooDto fooDto);

    // PUT jaoks — uuendab olemasolevat entity't, null välju ignoreerib
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(ignore = true, target = "id")
    @Mapping(ignore = true, target = "status")
    void updateFoo(FooDto fooDto, @MappingTarget Foo foo);
}
```
- `toFooResponseDtos(List<Foo>)` — list mapping automaatne
- `updateFoo(dto, @MappingTarget entity)` — PUT operatsiooni jaoks, null väljad jäetakse muutmata

### Repository tava
```java
public interface FooRepository extends JpaRepository<Foo, Integer> {
    @Query("select f from Foo f where f.bar.id = :barId and f.status = :status")
    List<Foo> findFoosBy(Integer barId, String status);

    @Query("select (count(f) > 0) from Foo f where f.name = :name")
    boolean fooExistsBy(String name);

    @Modifying
    @Transactional
    @Query("delete from Foo f where f.bar.id = :barId")
    void deleteAllByBarId(Integer barId);
}
```
- Meetodite nimed: `find[Mis]By()`, `[entity]ExistsBy()`
- Kirjutuspäringud: `@Modifying` + `@Transactional`
- DTO projektsioon: `select new ee.valiit...Dto(t.id, t.name) from ...`
- **`join fetch` seotud entity puhul** — kui mapper vajab seotud entity välju (nt `region.regionName`), lisa `join fetch` päringule, muidu `LazyInitializationException`: `select sr from SellerRegion sr join fetch sr.region where ...`

### Service tava
```java
@Service
@RequiredArgsConstructor
public class FooService {
    private final FooRepository fooRepository;
    private final FooMapper fooMapper;

    @Transactional
    public void addFoo(FooDto fooDto) {
        validateFooNameIsAvailable(fooDto.getName());
        createAndSaveFoo(fooDto);
    }

    public List<FooResponseDto> findFoos() {
        return fooMapper.toFooResponseDtos(fooRepository.findAll());
    }

    private void createAndSaveFoo(FooDto fooDto) {
        Foo foo = fooMapper.toFoo(fooDto);
        fooRepository.save(foo);
    }

    private void validateFooNameIsAvailable(String name) {
        if (fooRepository.fooExistsBy(name)) {
            throw new ConflictException(FOO_NAME_UNAVAILABLE.getMessage(), FOO_NAME_UNAVAILABLE.getErrorCode());
        }
    }
}
```
- `@Transactional` kõigil kirjutusoperatsioonidel (save, update, delete)
- Avalikud meetodid on lühikesed (3–7 rida), delegeerivad privaatsetele
- Privaatmeetodite nimed: `createAndSave*`, `handle*`, `validate*`, `is*`, `get*`, `addXxxToYyy`

### Controller tava
```java
@RestController @RequestMapping("/api") @RequiredArgsConstructor
public class FooController {
    // Kõigil endpoint-idel @Operation + @ApiResponses
    // Error response alati ApiError.class schemaga
    // URL: /api/{resource} (ilma versiooninumbrita)
    // Sisend DTO valideerimisel: @Valid @RequestBody FooDto fooDto
    // DTO väljadel: @NotBlank — ainult väljal, mitte klassi peal!
}
```

### Exception tava
- Custom exception laiendab `RuntimeException`, omab `message` ja `errorCode` välju
- `ErrorResponse` enum sisaldab veakoodid (numbrid: 111, 222, 333...)
- `RestExceptionHandler extends ResponseEntityExceptionHandler` käsitleb kõik

### Dependency injection
- Alati `@RequiredArgsConstructor` (mitte `@Autowired`)
- Controller süstib ainult Service-t
- Service süstib Repository-t ja Mapper-it
