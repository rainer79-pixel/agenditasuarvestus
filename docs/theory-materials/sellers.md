# Task-03: Edasimüüjad — algajatele (backend)

## Mis see on ja miks oluline?

Task-03 ehitab edasimüüjate halduse — suurima ja keerukama osa kogu backendist.
Kasutaja saab näha edasimüüjate nimekirja, vaadata detaile (kontaktid, piirkonnad, teenustasud)
ning Admin saab lisada, muuta ja deaktiveerida edasimüüjaid.

**Päriselust analoogia:** Kujuta ette müügifirmat kellel on partnerid üle Eesti.
Keegi peab hoidma nimekirja: kes nad on, kuidas nendega ühendust võtta, mis tingimustel nad töötavad.
ETAS on see nimekiri — digitaalne ja ligipääsetav kõigile töötajatele.

---

## Mis on selles taskis uut võrreldes bank40 projektiga?

Bank40-s õppisime sama Controller → Service → Repository → Mapper → DTO mustrit. Task-03 kordab täpselt sama mustrit — aga edasimüüjate maailm on keerulisem, seetõttu lisandus kuus uut kontseptsiooni:

| Uus asi | Kus näed | Mida tähendab |
|---------|---------|---------------|
| **Status enum kolme kihiga** | `Status.java`, `SellerMapper`, `SellerService` | DB salvestab `"A"`/`"D"`, API tagastab `"ACTIVE"`/`"INACTIVE"`, UI näitab `"Aktiivne"`/`"Peatatud"` — üks enum teisendab kõik |
| **`@ResponseStatus(HttpStatus.CREATED)`** | `SellerController` POST meetodil | POST tagastab `201 Created`, mitte vaikimisi `200 OK` — see on HTTP standard uue ressursi loomisel |
| **`ConflictException` + HTTP 409** | `SellerService`, `RestExceptionHandler` | Uus veatüüp olukordadeks kus pole viga inputs, aga loogiline konflikt — nt orgId on juba olemas |
| **`LocalDate`/`LocalDateTime` + Mapper kuupäevateisendus** | `Seller` entity, `SellerMapper` | Kuupäevad liiguvad sisse ISO formaadis (`2024-01-01`), välja eesti formaadis (`01.01.2024`) — Mapper teisendab |
| **`@BeanMapping(NullValuePropertyMappingStrategy.IGNORE)`** | `SellerMapper` `updateSeller()` | PUT mapper — kui DTO väli on `null`, jätab entity olemasoleva väärtuse muutmata |
| **`join fetch` JPQL-is** | `SellerRegionRepository` | Lazy suhte korral peab seotud entity koos laadima — muidu `LazyInitializationException` |

Kõik muu (muster, `@Transactional`, `@Valid`, Swagger, `Optional`, `ForbiddenException`) on tuttav bank40-st või eelmistest taskidest.

---

## Kuidas kõik osad omavahel seotud on?

Enne kui detailidesse läheme, vaata suurt pilti. Üks HTTP päring läbib alati sama tee:

```
BROWSER (kasutaja vajutab nuppu)
    ↓
FRONTEND (Vue komponent kutsub SellerService.js)
    ↓
AXIOS (saadab HTTP päringu backendi)
    ↓  ← siit algab backend
CONTROLLER (võtab päringu vastu, kontrollib parameetrid)
    ↓
SERVICE (äriloogika — valideerib, otsustab)
    ↓
REPOSITORY (suhtleb andmebaasiga)
    ↓
ANDMEBAAS (tagastab andmed)
    ↓
MAPPER (teisendab Entity → DTO)
    ↓
CONTROLLER (tagastab JSON vastuse)
    ↓  ← backend lõpetab
AXIOS (saab vastuse)
    ↓
VUE KOMPONENT (kuvab andmed ekraanil)
    ↓
BROWSER (kasutaja näeb tulemust)
```

**Iga kihi roll lühidalt:**
- **Controller** — "uksehoidja": võtab päringu vastu, annab edasi, tagastab vastuse
- **Service** — "aju": otsustab mis on lubatud ja mis mitte
- **Repository** — "tõlk andmebaasiga": teab kuidas SQL päringuid teha
- **Mapper** — "tõlk andmete vahel": teisendab DB formaadi API formaadiks
- **DTO** — "andmelepping": täpselt mis info liigub sisse ja välja
- **Entity** — "DB peegeldus Java-s": täpselt sama struktuur mis andmebaasis

---

## Andmevoog samm-sammult — GET edasimüüjate nimekiri

Vaatame konkreetset näidet — kasutaja avab edasimüüjate nimekirja:

```
1. Kasutaja avab /sellers lehel
        ↓
2. SellersView.vue beforeMount() käivitub automaatselt
        ↓
3. SellerService.js sendGetSellers(userId) kutsutakse
        ↓
4. Axios saadab:  GET /api/seller/user/1
        ↓
5. SellerController.getSellers() võtab vastu
        ↓
6. sellerService.findSellers() kutsutakse
        ↓
7. sellerRepository.findAllSellers() pärib DB-st:
   SELECT * FROM etas.seller ORDER BY company_name
        ↓
8. DB tagastab List<Seller> (Java objektid)
        ↓
9. sellerMapper.toSellerDetailResponseDtos() teisendab:
   - seller.status "A" → "ACTIVE"
   - seller.contractStart LocalDate → "01.01.2024" string
        ↓
10. Controller tagastab List<SellerDetailResponseDto> JSON-ina
        ↓
11. Axios saab vastuse, Vue kuvab tabelis
```

---

## BACKEND

### 1. Status enum — miks on kolm erinevat formaati?

See on üks keerulisemaid kohti taskis. Vastus küsimusele "miks mitte lihtsalt kõikjal `ACTIVE`?" on ajalugu ja jõudlus.

**Päriselust analoogia:** Rahvusvahelises lennufirmas on lennujaamakood kolmetäheline (`TLL` = Tallinn). Infosüsteemides kasutatakse seda lühikoodi, aga klientidele näidatakse täisnime "Tallinn Ülemiste". Sama asi — üks väärtus, kolm esitusviisi.

```
DB tabel         → Java/API          → Kasutaja ekraan
"A"              → "ACTIVE"          → "Aktiivne"  (roheline)
"D"              → "INACTIVE"        → "Peatatud"  (hall)
```

```java
public enum Status {
    ACTIVE("A", "ACTIVE"),        // DB kood, API väärtus
    SOFT_DELETED("D", "INACTIVE");

    private final String code;     // salvestatakse DB-sse
    private final String apiValue; // kasutatakse JSON-is
}
```

**Kuidas teisendus toimib:**

Kui frontend saadab `PUT /status` koos `{"status": "INACTIVE"}`:
```
"INACTIVE" (API)
    → Status.fromApiValue("INACTIVE") → tagastab Status.SOFT_DELETED
    → .getCode() → tagastab "D"
    → seller.setStatus("D") → salvestatakse DB-sse
```

Kui backend tagastab GET vastuse:
```
"A" (DB-st)
    → Status.toApiValue("A") → tagastab "ACTIVE"
    → JSON-is kuvatakse "status": "ACTIVE"
    → Vue kuvab "Aktiivne"
```

---

### 2. DTO-d — mis info liigub kuhu?

**DTO** (Data Transfer Object) on nagu postipakk — täpselt see info mis vaja, ei midagi rohkem ega vähem.

**Miks DTO ja mitte otse Entity?**
Entity sisaldab kõiki DB välju — ka neid mida ei tohi väljastada (nt `createdBy`, `status` raw kood). DTO on "turvaline versioon" — ainult see mis kasutajal on vaja näha või saata.

```
Entity (DB peegeldus)          DTO (API lepping)
-------------------------------  -------------------------
id                          →    sellerId
company_name                →    companyName
status: "A"                 →    status: "ACTIVE"    ← mapper teisendab
contract_start: LocalDate   →    contractStart: "01.01.2024"  ← mapper teisendab
created_by (FK objekt)      →    (ei saadeta välja)
created_at                  →    (ei saadeta välja)
```

**Sisend vs vastus DTO:**

| DTO | Suund | Kelleks |
|-----|-------|---------|
| `SellerDto` | Frontend → Backend | POST lisamine + PUT muutmine |
| `SellerStatusDto` | Frontend → Backend | PUT staatuse muutmine |
| `SellerDetailResponseDto` | Backend → Frontend | GET nimekiri + GET detail |
| `SellerContactResponseDto` | Backend → Frontend | GET kontaktid |
| `SellerRegionResponseDto` | Backend → Frontend | GET piirkonnad |
| `CommissionRateResponseDto` | Backend → Frontend | GET teenustasud |

**Kuupäeva formaat — miks kaks erinevat?**

Sisendis (POST/PUT): `"2024-01-01"` — ISO formaat, `LocalDate.parse()` vaikimisi
Vastuses (GET): `"01.01.2024"` — eesti formaat, mapper teisendab

```
Frontend saadab:  "contractStart": "2024-01-01"
                                        ↓
                              LocalDate.parse("2024-01-01")
                                        ↓
                              DB-sse: 2024-01-01

DB-st tuleb:      contract_start: 2024-01-01
                                        ↓
                    .format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                                        ↓
Frontend saab:    "contractStart": "01.01.2024"
```

**Miks `BigDecimal` teenustasudele?**

```java
// double/float — VALE rahasummadele:
0.1 + 0.2 = 0.30000000000000004  ← ümardamisviga!

// BigDecimal — ÕIGE:
new BigDecimal("0.1").add(new BigDecimal("0.2")) = 0.3  ← täpne
```

---

### 3. Repository — kuidas andmebaasiga räägitakse?

Repository on vahekiht Java koodi ja andmebaasi vahel. Sa kirjutad Java meetodeid, Spring genereerib SQL-i automaatselt.

**Päriselust analoogia:** Repository on nagu raamatukoguhoidja — sa ütled "too mulle kõik raamatud autorist Tammsaare" ja ta teab kuidas seda otsida. Sa ei pea ise teadma kus riiulil need on.

**Kolm võimalust päringuid kirjutada:**

```java
// 1. Spring Data automaatne — nimi ütleb mida teha
boolean existsByOrgId(Integer orgId);
// Spring genereerib: SELECT COUNT(*) FROM seller WHERE org_id = ?

// 2. @Query JPQL — ise kirjutad, aga Java objekte kasutad
@Query("select s from Seller s order by s.companyName")
List<Seller> findAllSellers();
// Hibernate tõlgib: SELECT * FROM etas.seller ORDER BY company_name

// 3. @Query JPQL projektsiooni — tagastab otse stringid, mitte entity-sid
@Query("select scr.contactRole.code from SellerContactRole scr where scr.sellerContact.id = :contactId")
List<String> findRoleCodesBy(Integer contactId);
// Tagastab ["L", "A"] — mitte objekte
```

**`join fetch` — miks see nii oluline on?**

See on üks taskis esinenud vigadest. Mõistmine on oluline!

Kõik seosed entity-des on `FetchType.LAZY`:
```java
@ManyToOne(fetch = FetchType.LAZY)
private Region region;  // ei laeta kohe, laadetakse alles kui küsitakse
```

**Probleem:**
```
1. Repository laeb SellerRegion objektid DB-st
2. Hibernate sessioon suletakse (DB ühendus katkestatakse)
3. Mapper proovib lugeda sellerRegion.getRegion().getRegionName()
4. Hibernate üritab laadida Region-it — aga sessioon on juba kinni!
5. LazyInitializationException → 500 viga
```

**Lahendus `join fetch`-iga:**
```java
@Query("select sr from SellerRegion sr join fetch sr.region where ...")
// SQL: SELECT sr.*, r.* FROM seller_region sr JOIN region r ON r.id = sr.region_id
// Kõik andmed laetakse KORRAGA — mapper saab kõik kätte
```

```
Ilma join fetch:
  Päring 1: SELECT * FROM seller_region → saame seller_region read
  Mapper: region.getRegionName() → SESSIOON KINNI → VIGA!

join fetch-iga:
  Päring 1: SELECT sr.*, r.* FROM seller_region JOIN region → saame kõik korraga
  Mapper: region.getRegionName() → töötab!
```

---

### 4. Mapper — kuidas andmed teisendatakse?

Mapper on "tõlk" Entity ja DTO vahel. MapStruct genereerib automaatselt Java koodi mis teeb teisenduse.

**Päriselust analoogia:** Mapper on nagu tõlk kahte keelt kõneleva inimese vahel — sa ütled talle mis tõlkida ja ta teeb seda automaatselt.

**Lihtne teisendus** (sama nimi, sama tüüp):
```java
@Mapping(source = "companyName", target = "companyName")
// MapStruct genereerib: dto.setCompanyName(entity.getCompanyName());
```

**Seotud entity välja teisendus:**
```java
@Mapping(source = "region.regionName", target = "regionName")
// MapStruct genereerib: dto.setRegionName(entity.getRegion().getRegionName());
```

**Keeruline teisendus `expression`-iga:**
```java
@Mapping(
  expression = "java(Status.toApiValue(seller.getStatus()))",
  target = "status"
)
// MapStruct paneb otse: dto.setStatus(Status.toApiValue(seller.getStatus()));
```

**PUT muutmise mapper — miks teistsugune?**
```java
// POST jaoks — loob UUE objekti
Seller toSeller(SellerDto sellerDto);

// PUT jaoks — UUENDAB olemasolevat
@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
void updateSeller(SellerDto sellerDto, @MappingTarget Seller seller);
```

`@MappingTarget` tähendab: "ära loo uut, muuda seda mis siin juba on"
`NullValuePropertyMappingStrategy.IGNORE` tähendab: "kui DTO väli on null, jäta entity väärtus muutmata"

```
PUT /seller/1 keha: {"companyName": "Uus Nimi", "notes": null}

Ilma IGNORE:  seller.notes = null  ← kustutab olemasoleva!
IGNORE-ga:    seller.notes = "Vana märkus"  ← jätab alles
```

**Miks SellerContactMapper-il pole `roles` välja?**

Rollid tulevad eraldi tabelist `seller_role`. MapStruct oskab teisendada ainult ühe entity välju — ta ei tea kuidas teisest tabelist midagi juurde küsida. Seetõttu:
1. Mapper teisendab põhiväljad (nimi, telefon, email)
2. Service lisab rollid käsitsi eraldi päringuga

---

### 5. Service — äriloogika

Service on "aju" — otsustab mis on lubatud ja mis mitte. Controller küsib, Service vastab.

**Projekti konventsioon:** Avalikud meetodid ees, privaatsed lõpus. Privaatsed väljakutsumise järjekorras.

```java
// AVALIKUD — neid kutsub Controller
public void addSeller(...)          // 1. avalik
public SellerDetailResponseDto findSeller(...)  // 2. avalik

// PRIVAATSED — abimeetodid, väljakutsumise järjekorras
private void validateUserIsAdmin(...)      // esimesena kutsutud addSeller-ist
private void validateOrgIdIsAvailable(...)  // teisena kutsutud addSeller-ist
private void createAndSaveSeller(...)      // kolmandana kutsutud addSeller-ist
```

**Miks `@Transactional` kirjutusoperatsioonidel?**

```java
@Transactional
public void addSeller(Integer userId, SellerDto sellerDto) {
    validateUserIsAdmin(userId);
    validateOrgIdIsAvailable(sellerDto.getOrgId());
    createAndSaveSeller(userId, sellerDto);  // ← DB-sse kirjutamine
}
```

`@Transactional` tähendab: "kõik DB operatsioonid selles meetodis on üks tervik".
Kui midagi läheb valesti keskel — kõik muutused tühistatakse automaatselt.

**Päriselust analoogia:** Pangaülekanne — raha lahkub ühelt kontolt JA jõuab teisele. Kui teine samm ebaõnnestub, tuleb raha esimesele kontole tagasi. Ei saa olla olukorda kus raha lahkus aga ei jõudnud kohale.

**Rollipõhine kontroll:**
```java
private void validateUserIsAdmin(Integer userId) {
    User user = userRepository.findById(userId).orElseThrow(...);
    if (!"A".equals(user.getUserRole())) {
        throw new ForbiddenException(...);  // → 403 Forbidden
    }
}
```

**Unikaalsuse kontroll (orgId):**
```java
private void validateOrgIdIsAvailable(Integer orgId) {
    if (sellerRepository.existsByOrgId(orgId)) {
        throw new ConflictException(...);  // → 409 Conflict
    }
}
```

**Rollide lisamine kontaktidele — N+1 päring:**
```java
for (SellerContact contact : contacts) {
    // 1. Mapper teisendab põhiväljad
    SellerContactResponseDto dto = sellerContactMapper.toSellerContactResponseDto(contact);
    // 2. Eraldi päring rollide jaoks
    dto.setRoles(sellerContactRoleRepository.findRoleCodesBy(contact.getId()));
    result.add(dto);
}
```

Kui on 3 kontakti, tehakse 4 päringut (1 kontaktide list + 3 rollide päringut).
See on "N+1 päring" — üldiselt mitte parim praktika, aga kontaktide puhul (2-5 tk) on aktsepteeritav.

---

### 6. Controller — HTTP uksehing

Controller võtab HTTP päringu vastu, annab Service-le edasi, tagastab vastuse. Ise ta midagi ei arvuta.

```java
@GetMapping("/seller/{sellerId}")
@Operation(summary = "Edasimüüja detailvaade")   // ← Swagger UI pealkiri
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", ...)       // ← dokumentatsioon
})
public SellerDetailResponseDto getSeller(@PathVariable Integer sellerId) {
    return sellerService.findSeller(sellerId);    // ← ainult üks rida!
}
```

**Miks `@ResponseStatus(HttpStatus.CREATED)` POST-il?**
Vaikimisi tagastatakse kõigil meetoditel `200 OK`. Aga loomine peaks tagastama `201 Created` — see on HTTP standard. `@ResponseStatus` muudab vaikimisi koodi.

```
POST /seller/user/1  →  201 Created   ← õige
GET  /seller/1       →  200 OK        ← õige
PUT  /seller/1       →  200 OK        ← õige
```

---

### Veakoodid

| HTTP kood | Millal tekib | Mida tähendab |
|-----------|-------------|---------------|
| 200 OK | Kõik läks hästi | Andmed tagastatud / muutus salvestatud |
| 201 Created | POST õnnestus | Uus edasimüüja loodud |
| 400 Bad Request | Vigased sisendandmed | Kohustuslik väli puudub, vale formaat |
| 403 Forbidden | Pole õigust | Kasutaja pole Admin |
| 404 Not Found | Ei leitud | Vale ID, kirjet pole DB-s |
| 409 Conflict | Dubleering / loogiline konflikt | OrgId juba olemas, staatus juba sama |
| 500 Internal Server Error | Ootamatu viga | Käsitlemata erand serveris |

**Kuidas vead liiguvad:**
```
Service viskab: throw new ForbiddenException("Teil pole õigust", 115)
                                    ↓
RestExceptionHandler püüab kinni
                                    ↓
Tagastab: HTTP 403 + {"message": "Teil pole õigust", "errorCode": 115}
                                    ↓
Frontend näitab: errorMessage = "Teil pole õigust"
```

---

## Uued mõisted selles taskis

| Mõiste | Lihtsalt selgitatud |
|--------|---------------------|
| `FetchType.LAZY` | "Lae hiljem" — seotud andmeid ei laeta automaatselt, ainult kui küsitakse |
| `join fetch` | "Lae koos" — päri seotud andmed kohe sama SQL-iga, ära oota hilisema laadimiseni |
| `@BeanMapping(IGNORE)` | PUT mapper — kui väli on null, ära kustuta olemasolevat väärtust |
| `@MappingTarget` | "Muuda seda olemasolevat" — mapper ei loo uut objekti vaid uuendab |
| `BigDecimal` | Täpne kümnendaritmeetika — kohustuslik raha ja protsentide jaoks |
| `@Pattern` | Valideeri string mustri vastu — nt ainult `ACTIVE` või `INACTIVE` lubatud |
| `@Transactional` | Kõik-või-mitte-midagi — kui midagi ebaõnnestub, kõik muutused tühistatakse |
| N+1 päring | Üks nimekiri + N eraldi päringut lisainfo jaoks — sobib väikeste kogustega |
| DTO | Andmelepping — täpselt see info mis API-s liigub, ei midagi rohkem |
| `@ResponseStatus(CREATED)` | Muuda vaikimisi HTTP koodi 200 → 201 loomine operatsioonil |

---

## Seos eelnevate taskidega

- **Task-01** lõi `RestExceptionHandler` + `ErrorResponse` enum — task-03 lisas uued veakoodid (211-214, 115) samasse faili
- **Task-01** lõi `Status.java` enum — task-03 täiendas sellega (`apiValue` väli, `toApiValue`/`fromApiValue` meetodid)
- **Task-01/02** tutvustas mustrit: Controller → Service → Repository → Mapper → DTO — task-03 kordab sama mustrit 6 erineva andmetüübi jaoks

---

## Järgmised sammud

- **Task-03 frontend:** `SellersView.vue`, `SellerView.vue`, `SellerFormView.vue` — kasutajale nähtav osa
- **Task-05:** Kontaktide, piirkondade ja teenustasude POST/PUT/DELETE endpointid — praegu ainult GET (lugemine)
