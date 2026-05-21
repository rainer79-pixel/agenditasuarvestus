# Juhend: GET /api/region

**Taski fail:** `GET-api-region.md`
**Kontroller:** `RegionController.java`
**Implementeerimise voog:** RestController → Service → Repository → Service → Mapper → RestController

---

## Sissejuhatus

See endpoint tagastab kõik piirkonnad järjestatuna, et frontend saaks täita `SellerSettingsRegionModal` dropdowni "Piirkond". Vaade näeb välja nii:

- Modal "Lisa piirkond" avab dropdowni nimega **Vali piirkond**
- Dropdown laetakse selle endpointiga
- Piirkonnad peavad tulema kindlas järjestuses (Tallinn, Harju maakond, Tartu maakond jne)

Selle harjutuse käigus õpid, kuidas luua uus ressursi-põhine kontrolleriklass koos kõigi kihtidega — ja kuidas ära kasutada juba olemasolevat entity klassi.

> **Oluline tähelepanu enne alustamist:** Kõik teised ressursid (nt `SellerController`, `SellerRegionController`) on `controller/seller/` kaustas, sest need kõik puudutavad edasimüüjat. `region` on iseseisev ressurss — piirkondade nimekiri ei kuulu edasimüüja alla. Mõtle: kuhu peaks `RegionController.java` minema?

---

## Samm 1 — RestController

### Mida teha?

`RegionController.java` klassi ei ole veel olemas — see tuleb luua. Uue kontrolleri pakettide struktuur peaks järgima sama loogikat, mis `SellerController` puhul — kontroller kuulub ressursi enda paketti.

Loo IntelliJ'ga uus Java klass:
- **File → New → Java Class**
- Paki nimi: `ee.valiit.etas.controller.region`
- Klassi nimi: `RegionController`

Lisa klassile vajalikud annotatsioonid:

```java
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class KontrolleriKlass {
    // ...
}
```

### Meetodi loomine

Alusta meetodist ilma mappingannotatsioonita — kujunda esmalt struktuur:

```java
public void meetodiNimi() {
    // tühi meetod esialgu
}
```

> **Mõtle:** Milline on hea meetodi nimi? Vaata teiste kontrollerite näiteid (nt `getSellerRegions`) — seal on muster selge.

Seejärel lisa:
1. **Mappingannotatsioon** — `@GetMapping` koos õige teega
2. **Swagger annotatsioonid** — `@Operation` ja `@ApiResponses`

Sellel endpointil puuduvad `@PathVariable` ja `@RequestParam` — GET /api/region ei vaja ühtegi sisendparameetrit.

Swagger `@ApiResponses` jaoks vaata taskifailist "Veahaldus" sektsiooni — seal on kirjas, millised HTTP vastusekoodid on oodatud. Kuna spetsiifilist veahaldust ei ole, piisab lihtsast 200 + 500 kirjeldusest.

### Service klassi ettevalmistus

`RegionService.java` ei ole veel olemas — loo uus klass:
- Pakk: `ee.valiit.etas.service`
- Klass: `RegionService`

```java
@Service
@RequiredArgsConstructor
public class TeenusKlass {
    // ...
}
```

Lisa service muutuja kontrollerisse:

```java
private final TeenusKlass teenuseMuutuja;
```

Kutsu service meetodit kontrollerist välja (esialgu tühi):

```java
public void meetodiNimi() {
    teenuseMuutuja.meetodiNimi();
}
```

> **IntelliJ vihje:** Kui `teenuseMuutuja.meetodiNimi()` on punasega alla joonitud,
> vajuta **Alt+Enter** → vali **"Create method in TeenusKlass"**.
> IntelliJ loob automaatselt vastava meetodi service klassi!

---

## Samm 2 — Service

### Mida teha?

Ava `RegionService.java` (just loodud) ja mine äsja loodud meetodisse. Service meetod ei vaja ühtegi sisendit — ta pärib lihtsalt kõik piirkonnad andmebaasist ja tagastab need DTO listina.

### Repository ühenduse loomine

Mõtle: **millisest tabelist** on vaja andmeid lugeda? Vaata taskifailist "Andmebaas" sektsiooni.

> **Oluline:** Projekti kaustas `persistence/region/` on juba olemas `Region.java` entity — seda EI pea looma! Pead ainult looma vastava **Repository** interface'i.

Alusta kirjutama repositooriumi muutuja nime service meetodis:

```java
public void meetodiNimi() {
    regionRep  // <- kirjuta algus siia
}
```

> **IntelliJ vihje:** Kirjuta `regionRep` ja IntelliJ otsib sobivat repositooriumi.
> Kuna `RegionRepository` ei ole veel olemas, pakub IntelliJ **Alt+Enter** kaudu
> võimaluse luua uus interface. Vali **JpaRepository** laiendus ja kontrolli,
> et fail läheks paketti `ee.valiit.etas.persistence.region`.

Tulemus service klassis:

```java
@Service
@RequiredArgsConstructor
public class TeenusKlass {

    private final EntiteetRepository entiteetRepository;

    public void meetodiNimi() {
        entiteetRepository
    }
}
```

---

## Samm 3 — Repository

### Mida teha?

Mine äsja loodud `RegionRepository` interface'i. Pead lisama meetodi, mis tagastab **kõik** piirkonnad teatud järjestuses.

**Küsi endalt:** Kas JPA vaikimisi `findAll()` meetod sobib siinkohal? Milles on probleem?

> **Vihje:** Vaata taskifailist "Andmebaas" sektsiooni — seal on kirjas, millise veeru järgi peab tulemused järjestama. `findAll()` ei garanteeri järjestust.

### Uue meetodi loomine JPA Buddy abil

Kasuta **JPA Buddy** funktsionaalsust uue meetodi loomiseks:

1. Paremklõps repository klassis → JPA Buddy
2. Valikutes **Method** ja **Query** → vali **Query**
3. Vali meetodi tüüp:
   - **Find collection** — kuna tagastatakse mitu piirkonda
4. **Wrap type:** `List<EntiteetKlass>`
5. Query conditions: sel korral **ei ole** filtreerimist — kõik read tagastatakse
6. **Advanced** sektsioonis: vali **Named parameters**
7. **Order By Attributes** — lisa siia õige veerg kasvavalt

Peale meetodi loomist:
- Kontrolli, et meetodi nimi on arusaadav (nt `findAllBy...`)
- Kontrolli `@Query` JPQL sisu — kas järjestus on seal olemas?

---

## Samm 4 — tagasi Service'i (andmete teisendamine)

### Mida teha?

Repository meetod tagastab `List<Region>` — entity objektide lista. Need tuleb teisendada `RegionResponseDto` listiks.

### DTO klass

`RegionResponseDto.java` ei ole veel olemas. Vaata taskifailist "Response Body" sektsiooni — seal on kirjas täpselt, millised väljad DTO-l peavad olema ja millised on nende tüübid.

Loo DTO käsitsi (mitte JPA Buddy kaudu, kuna see on lihtne klass):
- Pakk: `ee.valiit.etas.controller.region.dto`
- Klass: `RegionResponseDto`

Vaata, kuidas teised DTO klassid on loodud (nt `SellerRegionResponseDto.java`) — kasuta sama struktuuri ja annotatsioone (`@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`).

> **Oluline tähelepanu:** DTO-l on ainult **kaks** välja. Vaata taskifailist, millised — ja pane tähele, et `sequenceNumber` **ei kuulu** DTO-sse. See on ainult andmebaasi järjestamiseks.

### Mapper

Loo `RegionMapper.java` mapper interface:
- Pakk: `ee.valiit.etas.persistence.region` (koos `Region.java` entity-ga)

Vaata näitena `SellerRegionMapper.java` — sama struktuur, sama `@Mapper` annotatsioon:

```java
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING)
public interface MapperiKlass {
    // ...
}
```

Lisa mapper meetodid. Vaja on **kahte** meetodit:
1. Üks `Region` → `RegionResponseDto` (üksik objekt)
2. Teine `List<Region>` → `List<RegionResponseDto>` (lista)

Lisa `@Mapping` annotatsioonid seal, kus välja nimed entity-s ja DTO-s erinevad:

```java
@Mapping(source = "", target = "")
TagastatavDtoTüüp toDtoKlassiNimi(EntiteetTüüp entiteet);
```

> **IntelliJ vihje:** Kliki `target = ""` jutumärkide vahele → vajuta **Ctrl+Space**.
> IntelliJ näitab, millised väljad DTO-l on — nii saad luua ettevalmistatud `@Mapping` malli.

Mõtle: milline on `Region` entity välja nimi ID jaoks? Ja milline on `RegionResponseDto` vastava välja nimi? Kas need kattuvad?

### Service meetodi lõpetamine

Kutsu mapper meetod välja service meetodis:

```java
public void meetodiNimi() {
    List<EntiteetTüüp> entiteedid = entiteetRepository.meetodiNimi();
    List<TagastatavDtoTüüp> dtos = mapperMuutuja.toDtoListiNimi(entiteedid);
    return dtos;
}
```

> **IntelliJ vihje:** `return dtos;` tekitab vea, sest meetodi tagastustüüp on `void`.
> Vajuta **Alt+Enter** → IntelliJ parandab tagastustüübi automaatselt!

Lisa mapper muutuja service klassi väljaviitena (kui pole veel lisatud) — IntelliJ pakub seda automaatselt **Alt+Enter** kaudu.

---

## Samm 5 — tagasi RestController'isse

### Mida teha?

Service meetod on nüüd valmis. Naase `RegionController.java` ja täienda kontrolleri meetodit.

Lisa `return` lause:

```java
public void meetodiNimi() {
    teenuseMuutuja.meetodiNimi();  // <- tulemus on praegu kasutamata
}
```

Muuda see:

```java
public TagastatavTüüp meetodiNimi() {
    return teenuseMuutuja.meetodiNimi();
}
```

> **IntelliJ vihje:** Lisa `return` ja muuta meetodi tagastustüüp `void`-ilt õigeks.
> Vajuta **Alt+Enter** punase joone peal → "Change return type".

---

## Samm 6 — kood ilusaks (refactor)

### Make it work → Make it beautiful

Kui kood kompileerub ja töötab, vaata üle:

**Meetodite järjekord:**
- `public` meetodid enne, `private` meetodid pärast
- Sel taskil service meetodeid on vähe, nii et järjestus on lihtne

**Mapper meetodite kontroll:**
- Kas kõik `@Mapping` annotatsioonid on täidetud?
- Kas on mõni väli, mis on kaardistamata ja tekitab hoiatuse?

**Kontrolli, et `unmappedTargetPolicy = ReportingPolicy.IGNORE`** on mapperil olemas — see väldib kompileerimisvigu kaardistamata väljade puhul.

---

## Kokkuvõte ja kontrollnimekiri

Enne kui pead koodi valmis, kontrolli läbi:

- [ ] `RegionController.java` on loodud paketti `ee.valiit.etas.controller.region`
- [ ] Kontrolleri meetodil on `@GetMapping("/region")` annotatsioon
- [ ] Kontrolleri meetodil on `@Operation` ja `@ApiResponses` annotatsioonid (200 + 500)
- [ ] `RegionService.java` on loodud paketti `ee.valiit.etas.service`
- [ ] `RegionRepository.java` on loodud paketti `ee.valiit.etas.persistence.region`
- [ ] Repository interface laiendab `JpaRepository<Region, Integer>`-t
- [ ] Repository meetodil on `@Query` annotatsioon koos `order by` klausliga
- [ ] `RegionResponseDto.java` on loodud paketti `ee.valiit.etas.controller.region.dto`
- [ ] DTO-l on täpselt kaks välja: `regionId` ja `regionName` (mitte `sequenceNumber`)
- [ ] `RegionMapper.java` on loodud paketti `ee.valiit.etas.persistence.region`
- [ ] Mapperil on `@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)`
- [ ] Kõik `@Mapping` annotatsioonid on täidetud
- [ ] Kood kompileerub vigadeta

---

> **Järgmine samm:** Testi endpointi Swagger UI kaudu (`http://localhost:8080/swagger-ui/index.html`)
> — vali `GET /api/region`, vajuta **Execute** ja kontrolli:
> 1. Vastus on HTTP 200
> 2. Vastuses on piirkondade massiiv (Tallinn, Harju maakond, Tartu maakond jne)
> 3. Piirkonnad on järjestatud `sequenceNumber` järgi kasvavalt
> 4. Iga piirkonna juures on `regionId` ja `regionName` — mitte `sequenceNumber`