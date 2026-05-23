# Juhend: GET /api/report/{sellerId}/{period}

**Taski fail:** `GET-api-report-sellerId-period.md`
**Kontroller:** `ReportController.java`
**Implementeerimise voog:** RestController → Service → Repository → Service → Mapper → RestController

---

## Sissejuhatus

See endpoint tagastab ühe edasimüüja aruande detailid ühe perioodi kohta — tootegruppide kaupa
(tehingute arv, müügisumma, teenustasud, KM). Andmed tulevad olemasolevast
`commission_calculation_view` vaatest, mida filtreeritakse `sellerId` ja `period` järgi.
Harjutuse käigus õpid lisama meetodit olemasolevale kontrollerile, kasutama repository filtrimeetodit
kahe parameetriga ning looma uue response DTO koos mapperiga.

---

## Enne alustamist — kontrolli ErrorResponse

Ava `backend/src/main/java/ee/valiit/etas/infrastructure/error/ErrorResponse.java`.

Otsi sealt üles `REPORT_NOT_FOUND`. Kas see on juba olemas?

> **Kui puudub** → lisa see enne edasi minemist. Vaata, millisesse vahemikku see peaks sobima
> (kommentaarid failis ütlevad, mis numbreid millise domeeni jaoks kasutatakse).
> Tüüpiline sõnum: eesti keeles, kirjeldab olukorda lühidalt.

---

## Samm 1 — RestController

### Mida teha?

`ReportController.java` on juba olemas — seal on `POST /api/import/user/{userId}` meetod.
Sinu ülesanne on lisada sellesse klassi **uus meetod** — GET endpoint kahe path parameetriga.

Ava fail: `backend/src/main/java/ee/valiit/etas/controller/ReportController.java`

### Meetodi loomine

Alusta meetodist **ilma mappingannotatsioonideta** — esmalt pane loogika paika:

```java
public void meetodiNimi(SisendTüüp param1, SisendTüüp param2) {
    // tühi meetod esialgu
}
```

> **Mõtle:** Mitu path parameetrit on sellel endpointil? Mis tüüpi on `sellerId` ja `period`?
> Vaata taskifailist API teed — see annab vihje.

Seejärel lisa:
1. **`@GetMapping`** — täpne tee taskifailist
2. **`@PathVariable`** — mõlemale parameetrile
3. **`@Operation` ja `@ApiResponses`** — sh 404 veaolukord

### Service klassi ettevalmistus

`ReportControllerService` on juba olemas. Lisa selle muutuja kontrollerisse (kui pole veel lisatud):

```java
private final TeenusKlass teenuseMuutuja;
```

Kutsu service meetod välja — kirjuta meetodinimi ise:

```java
public void meetodiNimi(SisendTüüp param1, SisendTüüp param2) {
    teenuseMuutuja.meetodiNimi(param1, param2);
}
```

> **IntelliJ vihje:** Kui meetodinimi on punane, vajuta **Alt+Enter** → "Create method in ReportControllerService".
> IntelliJ loob meetodi automaatselt service klassi!

---

## Samm 2 — Service

### Mida teha?

Mine `ReportControllerService.java` ja ava äsja loodud meetod. Sisse tulevad `sellerId` (Integer)
ja `period` (String). Eesmärk on leida `commission_calculation_view` andmed just selle
edasimüüja ja perioodi kohta.

### Kaks sammu andmete leidmiseks

**Samm A — leia SalesReport perioodi järgi**

Mõtle: kas `commission_calculation_view` entity-l on `period` väli? Ava
`CommissionCalculationView.java` ja vaata.

> **Kui väli puudub** → period on `sales_report` tabelis. Seega tuleb kõigepealt leida
> vastav `SalesReport` kirje, et saada tema `id`.

Kasuta `SalesReportRepository`-t. Kontrolli, kas see on juba service klassis väljas —
kui ei ole, lisa see (IntelliJ soovitab Tab-iga).

```java
public List<TagastatavDtoTüüp> meetodiNimi(Integer param1, String param2) {
    salesRep  // <- kirjuta algus, IntelliJ soovitab Tab-iga
}
```

> **Mõtle:** Mis meetod leiab `SalesReport` kirje `period` järgi? Vaata, kas see meetod on
> `SalesReportRepository`-s olemas. Kui ei, tuleb see lisada (Samm 3 käsitleb seda).

Kui `SalesReport`-i ei leita → viska `DataNotFoundException` koos `REPORT_NOT_FOUND` veakoodiga.

**Samm B — leia view andmed salesReportId + sellerId järgi**

Nüüd on `salesReport` olemas. Kasuta `CommissionCalculationViewRepository`-t —
see on juba service klassis olemas (`ReportControllerService` kasutab seda juba).

```java
public List<TagastatavDtoTüüp> meetodiNimi(Integer param1, String param2) {
    EntiteetTüüp salesReport = // ... leia perioodi järgi
    List<EntiteetTüüp> viewRows = commRep  // <- kirjuta algus
}
```

> `CommissionCalculationViewRepository`-s on praegu `findBySalesReportId(Integer)` meetod.
> Meile läheb vaja meetodit, mis filtreerib **nii** `salesReportId` **kui ka** `sellerId` järgi.
> Selle meetodi lisame Samm 3-s.

---

## Samm 3 — Repository

### Mida teha?

Sul on vaja kahte uut repository meetodit:

**3A — `SalesReportRepository`**: leia `SalesReport` perioodi järgi

**3B — `CommissionCalculationViewRepository`**: leia view read kahe filtriga

### 3A — SalesReportRepository

Ava `SalesReportRepository.java`. Mõtle: kas JPA derived query (`findByPeriod`) piisab,
või on vaja `@Query`?

> **Rusikareegel:** Kui päringusse läheb ainult see entiteet oma veerud — derived query piisab.
> Kui filtreerid mitu parameetrit — kaaluge JPA Buddy abi.

Tulemuse wrap-tüüp: kaaluge `Optional<SalesReport>` — nii saad kenasti kontrollida, kas kirje leidub.

### 3B — CommissionCalculationViewRepository

Ava `CommissionCalculationViewRepository.java`. Lisa uus meetod, mis filtreerib
**nii** `salesReportId` **kui ka** `sellerId` järgi.

JPA derived query nimekujuga — IntelliJ pakub automaattäidet:

```java
List<EntiteetTüüp> findBy...And...(Integer param1, Integer param2);
```

> **Vihje:** JPA derived query nimi koosneb väljanimedest. Vaata `CommissionCalculationView` välju
> ja kombineeri kaks välja `And`-iga. Väljad peavad täpselt vastama entity väljanimedele.

---

## Samm 4 — tagasi Service'i (andmete töötlemine)

### Mida teha?

Repository meetodid on nüüd olemas. Täienda service meetodit — kutsu repository meetodid välja ja
teisenda tulemus DTO-ks.

### DTO klass — ReportDetailResponseDto

`ReportDetailResponseDto.java` on vaja luua. Kaust: `controller/report/dto/`

Loo see käsitsi Java klassina (File → New → Java Class). Lisa väljad taskifailist:
`productTypeName`, `transactionCount`, `salesAmount`, `feePerTransaction`, `feePercent`,
`calculatedFee`, `vatAmount`, `totalFee`.

> **Tüübid:** Vaata taskifailist ja `CommissionCalculationView` entity-st, mis Java tüüp igale
> väljale sobib. Tähelepanu: `transactionCountSum` on view-s `Long` — kas DTO-s peaks kasutama
> sama tüüpi?

DTO klass peaks kasutama Lombok annotatsioone vastavalt projekti konventsioonile (vaata teisi DTO klasse).

### Mapper

Projekti `CommissionCalculationViewMapper.java` on juba olemas (asukoht: `persistence/view/`).
Ava see fail ja lisa uus mapper meetod, mis teisendab `CommissionCalculationView` →
`ReportDetailResponseDto`.

Mapper meetodi mall:

```java
@Mapping(source = "", target = "")
// ... kõigi väljade jaoks
TagastatavDtoTüüp toDtoNimi(EntiteetTüüp entiteet);

List<TagastatavDtoTüüp> toDtoNimid(List<EntiteetTüüp> entiteedid);
```

> **IntelliJ vihje:** Kliki `target = ""` jutumärkide vahele → vajuta **Ctrl+Space** — IntelliJ
> näitab DTO väljanimesid.

> **Tähelepanu:** Mõned väljanimede paarid ei kattu (nt `transactionCountSum` → `transactionCount`,
> `calculatedFeePlusVat` → `totalFee`, `salesAmountSum` → `salesAmount`).
> Projekti konventsioon: kõik väljad mappitakse eksplitsiitselt `@Mapping`-iga, ka kattuva nimega.

### Service meetodi lõpetamine

```java
public List<TagastatavDtoTüüp> meetodiNimi(Integer param1, String param2) {
    EntiteetTüüp salesReport = salesReportRepository.meetodiNimi(param2)
            .orElseThrow(() -> new DataNotFoundException(...));
    List<EntiteetTüüp> viewRows = viewRepository.meetodiNimi(salesReport.getId(), param1);
    return mapperMuutuja.toDtoNimid(viewRows);
}
```

> **IntelliJ vihje:** Tagastustüüp on praegu `void` — vajuta **Alt+Enter** punase joone peal
> → "Change return type to List<...>".

---

## Samm 5 — tagasi RestController'isse

### Mida teha?

Service meetod tagastab nüüd listi. Täienda kontrolleri meetodit:

```java
public TagastatavTüüp meetodiNimi(Integer param1, String param2) {
    return teenuseMuutuja.meetodiNimi(param1, param2);
}
```

> **IntelliJ vihje:** Muuda tagastustüüp `void`-lt `List<ReportDetailResponseDto>`-ks —
> vajuta **Alt+Enter** → "Change return type".

---

## Samm 6 — kood ilusaks (refactor)

### Make it work → Make it beautiful

Kui kood töötab, vaata service meetodit üle:

- Kas `orElseThrow` loogika saaks eraldada privaatmeetodiks?
- Meetodite järjekord: `public` enne, `private` pärast

**Extract Method IntelliJ'ga:**
Märgi koodilõik → paremklõps → Refactor → Extract Method.

> **Tähelepanu:** IntelliJ kasutab ekstraktimisel kogu objekti parameetrina — vaata üle,
> kas privaatmeetod vajab kogu objekti või ainult üht välja.

---

## Kokkuvõte ja kontrollnimekiri

Enne kui pead koodi valmis, kontrolli läbi:

- [ ] `REPORT_NOT_FOUND` on lisatud `ErrorResponse` enum-i
- [ ] `ReportDetailResponseDto` on loodud õigesse paketti (`controller/report/dto/`)
- [ ] `SalesReportRepository`-l on meetod perioodi järgi otsimiseks
- [ ] `CommissionCalculationViewRepository`-l on meetod kahe parameetriga filtrimiseks
- [ ] `CommissionCalculationViewMapper`-il on uus meetod `ReportDetailResponseDto` jaoks (list + üksik)
- [ ] Kõik `@Mapping` annotatsioonid täidetud — ka kattuva nimega väljad
- [ ] `ReportController`-il on `@GetMapping("/report/{sellerId}/{period}")` meetod
- [ ] Kontrolleri meetodil on `@Operation` ja `@ApiResponses` (sh 404)
- [ ] Meetodite järjekord: `public` enne, `private` pärast
- [ ] Kood kompileerub

---

> **Järgmine samm:** Testi Swagger UI kaudu (`http://localhost:8080/swagger-ui/index.html`).
> Kasuta testandmeid — vaata, kas tootegruppide nimekiri tuleb õige `sellerId` + `period` kombinatsiooniga.
