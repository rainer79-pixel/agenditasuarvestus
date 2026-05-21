---
name: skill-intellij-backend-taski-implementeerimise-juhend
description: Koosta samm-sammuline IntelliJ-põhine juhend backend taski lahendamiseks — näitab kuidas läheneda implementeerimisele IDE abil, ilma äriloogika lahendusi ette andmata.
---

# IntelliJ Backend Taski Juhend

Koosta õpilasele samm-sammuline IntelliJ IDEA-põhine juhend valitud backend taski lahendamiseks.
Juhend annab suuniseid ja vihjeid, kuid mitte kunagi konkreetseid äriloogika lahendusi.

## Sammud

### 0. Tuleta meelde — branch

Enne juhendi koostamist küsi kasutajalt:

> **Kas oled masterist uue branchi võtnud?** Iga taski implementatsioon peaks minema eraldi branchi.

Oota kinnitust enne kui jätkad.

### 1. Kuva olemasolevad taskid

Loe kõik failid kaustast `docs/tasks/backend/` (mitte alamkaustadest).

Kuva kasutajale nimekiri leitud taskifailidest, näide:

```
Saadaval backend taskid:

1. GET-api-users-userId-transactions-history.md
2. POST-api-login.md
3. ...

Millist taski soovid lahendada? Kirjuta faili nimi (nt: GET-api-users-userId-transactions-history.md)
```

Oota kasutaja vastust enne kui jätkad.

### 2. Loe taski fail ja koguge kontekst

Paralleelselt:
- Loe kasutaja valitud taskifail (`docs/tasks/backend/<valitud-fail>.md`)
- Loe `database/2_create.sql` — andmebaasi skeemi mõistmiseks
- Vaata olemasolevaid kontrollereid: `backend/src/main/java/ee/valiit/etas/controller/`
- Vaata olemasolevaid service klasse: `backend/src/main/java/ee/valiit/etas/service/`
- Vaata olemasolevaid persisteerimise klasse: `backend/src/main/java/ee/valiit/etas/persistence/`

Parsi taskifailist välja:
- **HTTP meetod** (GET / POST / PUT / DELETE)
- **API tee** (nt `/api/users/{userId}/transactions-history`)
- **Kontrolleri nimi** (nt `TransactionController.java`)
- **RequestBody DTO** — nimi ja väljad (kui olemas)
- **ResponseBody DTO** — nimi ja väljad (kui olemas)
- **Veaolukorrad** — exception tüüp ja HTTP staatus
- **Seotud DB tabelid**

### 3. Määra implementeerimise voog

Vali HTTP meetodi põhjal õige implementeerimise järjekord:

**GET (lugemine):**
> RestController → Service → Repository → Service → Mapper → RestController

**POST (loomine):**
> RestController → Service → Mapper → Repository → Service → RestController

**PUT (uuendamine):**
> RestController → Service → Repository → Service → Mapper → Repository → Service → RestController

**DELETE (kustutamine):**
> RestController → Service → Repository → Service → RestController

### 4. Koosta juhend

Loo juhend vastavalt allpool toodud mallile ja reeglitele.

---

## Juhendi koostamise reeglid

### Üldreeglid

- Kõik selgitused kirjuta **eesti keeles**
- **Ära anna kunagi** konkreetseid äriloogika koodinäiteid (nt päringute JPQL sisu, valideerimisloogika, konkreetsed muutujate nimed domeenist)
- **Kasuta alati** generilist pseudokoodi — klassi- ja meetodinimed peavad olema väljamõeldud, mitte projekti pärisnimed
- Iga koodinäite ees peab olema **selgitav tekst**, mis kirjeldab, mida järgmisena tegema peaks
- Rõhuta IntelliJ IDE funktsionaalsust — **Alt+Enter**, **Tab**, **Ctrl+Space**, **JPA Buddy**
- Juhend peaks suunama õpilast **ise mõtlema**, mitte andma valmislahendust

### Pseudokoodi näide (ÕIGE — generiline)

```java
@GetMapping("/mingi/rada")
@Operation(summary = "Lühikokkuvõte")
@ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Kirjeldus, mida viga sisaldab",
                content = @Content(schema = @Schema(implementation = VeaKlass.class)))})
public TagastatavTüüp meetodiNimi(@RequestParam SisendTüüp parameetriNimi) {
    return teenuseMuutuja.meetodiNimi(parameetriNimi);
}
```

### Pseudokoodi näide (VALE — liiga konkreetne, äriloogikaga)

```java
@GetMapping("/users/{userId}/transactions")
public List<TransactionDto> getUserTransactions(@PathVariable Integer userId) {
    return transactionService.getTransactionsByUserId(userId);
}
```

---

## Juhendi mall

Kasuta järgmist struktuuri juhendi faili loomisel:

```markdown
# Juhend: <HTTP meetod> <API tee>

**Taski fail:** `<taskifaili nimi>`
**Kontroller:** `<KontrolleriNimi>.java`
**Implementeerimise voog:** <voo kirjeldus noolega>

---

## Sissejuhatus

<2–3 lauset: mis selle endpointi eesmärk on, millistest kihtidest see läbi käib ja mida õpilane selle harjutuse käigus õpib.>

---

## Samm 1 — RestController

### Mida teha?

<Kirjelda vabatekstina, mida selles sammus tegema peab. Maini ära, kas kontroller on juba olemas või tuleb uus luua.>

Kontrolli esmalt, kas vastav kontrolleri klass juba eksisteerib:
- Kaust: `backend/src/main/java/.../controller/`
- Kui **puudub** → loo uus klass IntelliJ'ga (File → New → Java Class)
- Kui **on olemas** → ava see klass ja lisa sinna uus meetod

Vajalikud klassiannotatsioonid (kui lood uue kontrolleri):

```java
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class KontrolleriKlass {
    // ...
}
```

### Meetodi loomine

Alusta meetodist **ilma mappingannotatsioonideta** — see aitab kõigepealt loogika paika saada:

```java
public void meetodiNimi(SisendTüüp parameetriNimi) {
    // tühi meetod esialgu
}
```

> **Mõtle:** Mis on selle meetodi hea nimi? Nimi peaks kirjeldama, mida meetod teeb.
> Vaata HTTP meetodit ja API teed taskifailist — need annavad vihje.

Seejärel lisa:
1. **Mappingannotatsioon** — `@GetMapping`, `@PostMapping`, `@PutMapping` või `@DeleteMapping`
2. **Parameetrite annotatsioonid** — `@PathVariable`, `@RequestParam` või `@RequestBody`
3. **Swagger annotatsioonid** — `@Operation` ja `@ApiResponses`

### Service klassi ettevalmistus

Enne kui kontrollerist service meetodit välja kutsud, kontrolli, kas service klass juba eksisteerib:
- Kaust: `backend/src/main/java/.../service/`
- Kui **puudub** → loo uus klass IntelliJ'ga (File → New → Java Class)

```java
@Service
@RequiredArgsConstructor
public class TeenusKlass {
    // ...
}
```

Kui service klass on olemas (või just loodud), lisa service muutuja kontrolleri klassi:

```java
private final TeenusKlass teenuseMuutuja;
```

Kutsu service meetodit välja (esialgne tühi väljakutse):

```java
public void meetodiNimi(SisendTüüp parameetriNimi) {
    teenuseMuutuja.meetodiNimi(parameetriNimi);
}
```

> **IntelliJ vihje:** Kui `teenuseMuutuja.meetodiNimi(...)` on punasega alla joonitud,
> vajuta **Alt+Enter** punasel joonel → vali **"Create method in TeenusKlass"**.
> IntelliJ loob automaatselt vastava meetodi service klassi!

---

## Samm 2 — Service

### Mida teha?

<Kirjelda vabatekstina, et nüüd liigutakse service klassi meetodisse. Maini, millised andmed sisse tulevad ja mis eesmärgil.>

Ava service klass (lõid või leidsid selle Samm 1 käigus) ja mine äsja loodud meetodisse.

### Repository ühenduse loomine

Mõtle: **millisest tabelist** on vaja andmeid pärida/kirjutada?
Vaata taskifailist sektsiooni "Andmebaas" — sealt leiad seotud tabelid.

Alusta kirjutama repositooriumi muutuja nime service meetodis:

```java
public void meetodiNimi(SisendTüüp parameetriNimi) {
    entiteetRep  // <- kirjuta algus siia
}
```

> **IntelliJ vihje:** Kirjuta muutuja nime algus (nt `entiteetRep`) ja IntelliJ pakub
> automaatselt vastavat repositooriumi. Vajuta **Tab** → repositoorium lisatakse klassiväljana!

Tulemus võiks välja näha nii:

```java
@Service
@RequiredArgsConstructor
public class TeenusKlass {

    private final EntiteetRepository entiteetRepository;

    public void meetodiNimi(SisendTüüp parameetriNimi) {
        entiteetRepository
    }
}
```

> **Kui repositooriumi interface pole olemas:** IntelliJ pakub punase pirniga (Alt+Enter)
> võimaluse luua uus interface. Vali **JpaRepository** ja kontrolli, et fail läheks
> õigesse paketti (sama pakett kui entiteet).

---

## Samm 3 — Repository

### Mida teha?

<Kirjelda, et nüüd liigutakse repository interface'i. Maini, et õpilane peab mõtlema, kas JPA pakub valmismeetodit või on vaja uus teha.>

**Küsi endalt:** Kas JPA pakub valmis `findById()` või muu sobiva meetodiga vastust?

> **Rusikareegel:** Kui päringusse läheb sisendina muu väärtus kui tabeli `id`,
> on tõenäoliselt vaja **uut meetodit** teha.

### Uue meetodi loomine JPA Buddy abil

Mine repository interface'i faili. Kasuta **JPA Buddy** funktsionaalsust:

1. Ava JPA Buddy paneel (paremklõps repository klassis → JPA Buddy)
2. Valikutes **Method** ja **Query** vali → **Query**
3. Vali meetodi tüüp:
   - **Find instance** — üksiku rea leidmiseks
   - **Find collection** — mitme rea leidmiseks
   - **Count** — loendamiseks
   - **Exists** — olemasolu kontrollimiseks
4. Määra **Wrap type**:
   - Üksiku rea puhul — kaaluda `Optional<EntiteetKlass>`
   - Mitme rea puhul — `List<EntiteetKlass>`
5. Lisa **query conditionid** — milliseid veerge filtreeritakse
6. **Advanced** sektsioonis: vali alati **Named parameters**
7. Mitme reaga tulemuse puhul mõtle läbi **Order By Attributes**

Peale meetodi loomist:
- Kontrolli parameetrite nimed — ebamäärane `id` asenda konkreetsemaga (nt `kasutajaId`)
- Tee vastav muudatus ka `@Query` annotatsiooni nimetud parameetris
- Eemalda ebavajalikud `@Param()` annotatsioonid meetodist, kui Named parameters on kasutusel

---

## Samm 4 — tagasi Service'i (andmete töötlemine)

### Mida teha?

<Kirjelda, et repository meetod tagastab entity objekti/lista ja see tuleb nüüd DTO-ks teisendada.>

Kasuta äsja loodud repository meetodit service meetodis.
Andmed saabuvad entity kujul — need tuleb teisendada DTO-ks.

### DTO klass

Mõtle: kas vastav DTO klass on juba olemas?
- Vaata kaustast: `backend/src/main/java/.../controller/.../dto/`

**Kui DTO puudub** → kasuta JPA Buddy abi:

1. Paremklõps entity klassil → New → DTO
2. Kontrolli valikud:
   - **Package** → controller alampakett (nt `controller.ressurss.dto`)
   - **DTO class name** → anna mõistlik nimi (nt `EntiteetResponseDto`)
   - **MapStruct Interface** → vali olemasolev mapper või loo uus plussmärgiga
   - **Mutable** → jäta märgituks
3. Vali väljad — kui seotud entiteet on foreign key objekt, vali **Flat** struktuur
4. Peale loomist kontrolli DTO klass üle ja tee käsitsi vajalikud korrektuurid

### Mapper

Ava mapper interface (nt `EntiteetMapper.java`).

Vaikimisi tekib JPA Buddy poolt kolm meetodit — eemalda mittevajalikud, jäta vaid need, mida tegelikult vajad.

Nimeta meetod ümber konventsiooni järgi:

```java
// Ühele DTO-le
TagastatavDtoTüüp toDtoKlassiNimi(EntiteetTüüp entiteet);

// Lista DTO listiks
List<TagastatavDtoTüüp> toDtoKlassiNimid(List<EntiteetTüüp> entiteedid);
```

Lisa `@Mapping` annotatsioonid:

```java
@Mapping(source = "", target = "")
TagastatavDtoTüüp toDtoKlassiNimi(EntiteetTüüp entiteet);
```

> **IntelliJ vihje:** Kliki `target = ""` jutumärkide vahele → vajuta **Ctrl+Space**.
> IntelliJ näitab, mitu välja DTO-l on — nii saad luua ettevalmistatud `@Mapping` malli.

Näiteks kui DTO-l on 3 välja, tekib selline mall:

```java
@Mapping(source = "", target = "")
@Mapping(source = "", target = "")
@Mapping(source = "", target = "")
TagastatavDtoTüüp toDtoKlassiNimi(EntiteetTüüp entiteet);
```

Täida kõik väljad. Mis ei sobi — kasuta `ignore = true`:

```java
@Mapping(source = "seotudObjekt.id", target = "seotudObjektiId")
@Mapping(source = "tavaveerg", target = "samaNimiDtos")
@Mapping(ignore = true, target = "väljaJuideiTahaSaata")
TagastatavDtoTüüp toDtoKlassiNimi(EntiteetTüüp entiteet);
```

### Service meetodi lõpetamine

Kutsu mapper meetod välja service meetodis:

```java
public void meetodiNimi(SisendTüüp parameetriNimi) {
    EntiteetTüüp entiteet = entiteetRepository.meetodiNimi(parameetriNimi);
    TagastatavDtoTüüp dto = mapperMuutuja.toDtoKlassiNimi(entiteet);
    return dto;
}
```

> **IntelliJ vihje:** Nüüd on meetodi tagastustüüp `void`, aga `return dto;` lause on sees.
> Vajuta **Alt+Enter** punase joone peal → IntelliJ parandab tagastustüübi automaatselt!

---

## Samm 4 — Mapper (sisendi teisendamine)

### Mida teha?

<Kirjelda, et POST/PUT puhul tuleb DTO teisendada entity-ks enne salvestamist.>

Loo mapper meetod, mis teisendab sisendi DTO entity-ks:

```java
@Mapping(ignore = true, target = "id")
@Mapping(source = "dtoVäli", target = "entiteetiVäli")
EntiteetTüüp toEntiteetKlassiNimi(SisendDtoTüüp dto);
```

> **Mõtle:** Millised väljad tuleb ignoreerida? `id` on alati `ignore = true` loomise puhul.
> Kas on välju, mida DTO ei sisalda, aga entity vajab (nt staatuseväli)?

---

## Samm <N> — tagasi RestController'isse

### Mida teha?

<Kirjelda, et service meetod on nüüd valmis ja tuleb naasta kontrolleri meetodisse.>

Täienda kontrolleri meetodit — lisa `return` lause:

```java
public void meetodiNimi(SisendTüüp parameetriNimi) {
    teenuseMuutuja.meetodiNimi(parameetriNimi);  // <- enne: tulemus kasutamata
}
```

> **IntelliJ vihje:** Lisa `return` lause ja muutuja, kuhu tulemus läheb.
> IntelliJ kurdab, et `void` ei saa midagi tagastada — vajuta **Alt+Enter** → "Change return type".

Tulemus:

```java
public TagastatavTüüp meetodiNimi(SisendTüüp parameetriNimi) {
    return teenuseMuutuja.meetodiNimi(parameetriNimi);
}
```

---

## Samm <N+1> — kood ilusaks (refactor)

### Make it work → Make it beautiful

Kui kood töötab, on aeg vaadata, kas saab koodi puhtamaks muuta.

**Extract Method IntelliJ'ga:**

Märgi service meetodis koodilõik, mida soovid eraldada helper meetodiks → paremklõps → Refactor → Extract Method.

> **Tähelepanu:** IntelliJ kasutab ekstraktimisel kogu objekti parameetrina.
> Vaata üle, kas helper meetod vajab tegelikult kogu objekti või ainult üht välja — ja tee vajadusel korrektuur.

Enne:
```java
kontrolliMidagiHelper(dtoObjekt);

private void kontrolliMidagiHelper(DtoTüüp dto) {
    boolean onProbleem = repositoorium.kontrollimeetod(dto.getMingiVäli());
    if (onProbleem) {
        throw new MingiException(...);
    }
}
```

Pärast (parem — anna edasi ainult vajalik):
```java
kontrolliMidagiHelper(dtoObjekt.getMingiVäli());

private void kontrolliMidagiHelper(VäljaTüüp väljaNimi) {
    boolean onProbleem = repositoorium.kontrollimeetod(väljaNimi);
    if (onProbleem) {
        throw new MingiException(...);
    }
}
```

### Meetodite järjekord

Kontrolli meetodite järjekorda vastavalt Java konventsioonile:
1. `public` meetodid enne
2. `private` meetodid pärast
3. Järjesta ka väljakutsumise hierarhia järgi — peameetod üleval, helper meetodid all

---

## Kokkuvõte ja kontrollnimekiri

Enne kui pead koodi valmis, kontrolli läbi:

- [ ] RestController klass on olemas vajaliku `@RestController`, `@RequestMapping`, `@RequiredArgsConstructor` annotatsiooniga
- [ ] Kontrolleri meetodil on `@Operation` ja `@ApiResponses` annotatsioonid
- [ ] Service klass on olemas vajaliku `@Service`, `@RequiredArgsConstructor` annotatsiooniga
- [ ] Repository interface on olemas ja laiendab `JpaRepository`-t
- [ ] Repository meetoditel on `@Query` annotatsioon Named parameters stiilis
- [ ] Mapper interface on olemas `@Mapper(componentModel = "spring")` annotatsiooniga
- [ ] Kõik `@Mapping` annotatsioonid on täidetud — ükski väli ei ole kaardistamata
- [ ] Meetodite järjekord: `public` enne, `private` pärast — järjesta ka väljakutsumise hierarhia järgi
- [ ] Kood kompileerub ja Swagger UI kaudu on endpoint nähtav

---

> **Järgmine samm:** Testi endpointi Swagger UI kaudu (`http://localhost:8080/swagger-ui/index.html`)
> ja kontrolli, et vastus vastab taskifailist leitud näidisandmetele.
```

### 5. Kontrolli mapper konventsioon

Mõlemas projektis (ETAS ja bank40back) esineb muster, kus **kõik väljad mappitakse eksplitsiitselt** — ka need, mille nimed kattuvad. See teeb kaardistuse ühes kohas täielikult nähtavaks ega jäta midagi "vaikimisi automaatseks".

```java
// EELISTATUD — kõik väljad nähtavad
@Mapping(source = "locationName", target = "name")
@Mapping(source = "numberOfAtms", target = "numberOfAtms")  // kattuv nimi, aga siiski kirjas
Location toLocation(LocationDto locationDto);
```

Kui kahtled, vaata olemasolevaid mappereid projektis ja järgi sama mustrit.

### 6. Loo juhendi fail

Koosta konkreetne juhend ülaltoodud malli põhjal, kohandades seda valitud taski spetsiifikaga:

- Asenda kõik `<...>` platsehoidjad taskifailist saadud infoga
- Kirjuta iga "Mida teha?" sektsiooni alla **konkreetne kontekst** valitud taskist (nt millisest tabelist andmeid pärida, milline DTO oodatakse), kuid **ilma lahendust ette andmata**
- Täienda veaolukordade sektsiooni taskifailist leitud veaolukordade põhjal

Loo fail: `docs/tasks/backend/instructions/<taskifailinimi-ilma-laiendita>-juhend.md`

Näide: task `GET-api-users-userId-transactions-history.md` → juhend `docs/tasks/backend/instructions/GET-api-users-userId-transactions-history-juhend.md`

### 7. Teavita kasutajat

Näita lühidalt:
- Loodud juhendi faili tee
- Implementeerimise voog mida juhend järgib
- Üks vihje, kust alustada

Näide:

```
Juhend on loodud: docs/tasks/backend/instructions/GET-api-...-juhend.md

Implementeerimise voog: RestController → Service → Repository → Service → Mapper → RestController

Alusta Samm 1-st — kontrolli esmalt, kas vastav kontrolleri klass juba eksisteerib.
```

### 8. Juhenda õpilast sammhaaval

Pärast juhendi loomist jätka interaktiivselt — **ära anna kogu sammu sisu korraga**. Juhend on raamistik, mitte skript.

**Põhireeglid:**

- **Üks küsimus / üks samm korraga** — anna järgmine samm alles pärast kinnitust ("tehtud", "ok", "jah")
- **Küsi enne edasiliikumist** — iga sammu lõpus: *"Kas on küsimusi, või liigume edasi?"*
- **Loe fail enne järgmise sammu andmist** — kontrolli, mis tegelikult kirjutatud on, mitte ära eelda
- **Kui õpilane küsib selgitust, mine väga lihtsaks:**
  - Murra süntaks visuaalselt osadeks (nooled/tulbad)
  - Kasuta analoogiaid (nt interface = tellimus restoranis, implementatsioon = köök)
  - Ära eelda eelteadmisi — seleta nii nagu oleks esimest korda
- **Ära anna koodilahendust ette** — anna vihje, oota katset, anna tagasisidet

**Näide heast vihjestiklist:**

```
// VALE — liiga palju korraga
Lisa service muutuja, kutsu getRegions() välja ja muuda tagastustüüp List<RegionResponseDto>-ks.

// ÕIGE — üks asi korraga
Lisa regionService väli kontrollerisse. Kus see peaks olema?
```

**Levinud vead mida jälgida:**
- Vale pakett (nt `controller.controller` asemel `controller.region`) — kontrolli kohe kui fail luuakse
- `@Operation` summary ei kirjelda endpointi täpselt (nt "Näita edasimüüja piirkondi" endpoint mis tagastab kõiki piirkondi)
- Lista mapper meetodi nimi ainsuses (nt `toRegionResponseDto`) — peaks olema mitmuses (`toRegionResponseDtos`)
- Repository meetodi nimi liiga pikk JPA konventsioonist (nt `findByOrderBySequenceNumberAsc`) — projekti tava on lühike `findAllRegions()`