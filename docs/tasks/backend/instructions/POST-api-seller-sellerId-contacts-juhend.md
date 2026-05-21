# Juhend: POST /api/seller/{sellerId}/contacts

**Taski fail:** `POST-api-seller-sellerId-contacts.md`
**Kontroller:** `SellerContactController.java`
**Implementeerimise voog:** RestController → Service → Mapper → Repository → Service → RestController

---

## Sissejuhatus

See endpoint lisab edasimüüjale uue kontakti koos tema rollidega. Andmed liiguvad läbi nelja kihi: Controller võtab päringu vastu, Service teostab äriloogika, Mapper teisendab DTO entity-ks, Repository salvestab andmebaasi. Harjutuse käigus õpid kuidas POST puhul käib kahe tabeli samaaegne lisamine (kontakt + rollid eraldi tabelis) ja kuidas mapper teisendab DTO entity-ks.

---

## Olemasolev vs uus

Enne alustamist — vaata, mis on juba tehtud ja mis vajab lisandust:

| Fail | Seis | Mida vaja teha |
|------|------|----------------|
| `SellerContactController.java` | Olemas | Lisa uus `@PostMapping` meetod |
| `SellerContactService.java` | Olemas | Lisa `addSellerContact(...)` meetod |
| `SellerContactMapper.java` | Olemas | Lisa `toSellerContact(SellerContactDto dto)` meetod |
| `ContactRoleRepository.java` | Loodud, aga tühi | Täienda — laienda `JpaRepository`-t + lisa `findByCode` |
| `SellerContactDto.java` | ✅ Valmis | Ei vaja muuta |

---

## Samm 1 — RestController

### Mida teha?

`SellerContactController` on juba olemas. Ava see fail ja lisa sinna uus meetod POST päringu jaoks.

Kontrolli enne lisamist, mis on kontrolleri klassis juba sees — näed olemasolevaid `@GetMapping` ja `@DeleteMapping` meetodeid. Uus meetod läheb samasse klassi.

### Meetodi loomine

Alusta meetodist **ilma annotatsioonideta** — see aitab kõigepealt parameetrid paika saada.

POST kontakti lisamiseks läheb vaja kolme parameetrit:
- `userId` — kes teeb päringu (URL query param)
- `sellerId` — millisele edasimüüjale (URL tee muutuja)
- DTO — uue kontakti andmed (päringu keha)

> **Mõtle:** Millist annotatsiooni kasutad iga parameetri jaoks?  
> `@PathVariable`, `@RequestParam` või `@RequestBody`?  
> Vaata olemasolevat `deleteSellerContact` meetodit — seal on sama muster.

> **Mõtle:** POST endpoint, mis loob uue ressursi, peaks tagastama HTTP 201 (mitte 200).  
> Lisatava annotatsiooni leiad: `@ResponseStatus(HttpStatus.CREATED)`

```java
public void meetodiNimi(SisendTüüp parameeter1, SisendTüüp parameeter2, SisendTüüp parameeter3) {
    teenuseMuutuja.meetodiNimi(parameeter1, parameeter2, parameeter3);
}
```

> **IntelliJ vihje:** Kui `teenuseMuutuja.meetodiNimi(...)` on punasega alla joonitud,
> vajuta **Alt+Enter** → vali **"Create method in SellerContactService"**.
> IntelliJ loob automaatselt meetodi signatuuri service klassi!

Seejärel lisa annotatsioonid:
1. `@PostMapping` — õige URL teega (vaata taskifailist)
2. `@ResponseStatus(HttpStatus.CREATED)` — HTTP 201 vastus
3. `@Operation(summary = "...")` — Swagger kirjeldus
4. `@ApiResponses(...)` — vastuskoodid: 201, 400, 403, 404, 500

> **DTO valideerimise annotatsiooni kohta:**  
> Kui soovid, et Spring kontrolliks DTO kohustuslikke välju automaatselt (`@NotBlank`),  
> lisa `@RequestBody` ette `@Valid`.

---

## Samm 2 — Service (skeleton)

### Mida teha?

IntelliJ lõi eelmises sammus `addSellerContact` meetodi skeleti `SellerContactService`-sse. Ava see meetod.

Vaata, mis `SellerContactService`-s juba on — `deleteSellerContact` meetodis on kaks `private` abimeetodit, mida saad **uuesti kasutada**:
- `validateUserIsAdmin(userId)` — kontrollib, kas kasutaja on Admin
- `validateSellerExists(sellerId)` — kontrollib, kas edasimüüja eksisteerib

Uues meetodis kutsud mõlemad välja, nagu `deleteSellerContact`-is.

```java
@Transactional
public void meetodiNimi(Tüüp userId, Tüüp sellerId, DtoTüüp dto) {
    // 1. Valideeri kasutaja roll
    // 2. Valideeri edasimüüja olemasolu
    // 3. ... (lisad hiljem)
}
```

> **Miks `@Transactional`?**  
> See endpoint kirjutab andmebaasi mitmele tabelile (`seller_contact` + `seller_role`).  
> `@Transactional` tagab, et kui midagi läheb valesti, tühistatakse **kõik** muudatused korraga.

---

## Samm 3 — Mapper

### Mida teha?

`SellerContactMapper`-il on praegu ainult üks meetod (`toSellerContactResponseDto`), mis teisendab entity DTO-ks. Lisa teine meetod, mis teeb vastupidist — teisendab `SellerContactDto` **entity-ks** (`SellerContact`).

Ava `SellerContactMapper.java` ja vaata selle struktuuri.

### Uue meetodi lisamine

Mapper meetodi signatuur entity loomiseks:

```java
@Mapping(ignore = true, target = "id")
@Mapping(ignore = true, target = "seotudObjektiVäli")
EntiteetTüüp toEntiteetKlassiNimi(DtoTüüp dto);
```

> **Mõtle:** Milliseid välju pead ignoreerima?
> - `id` ignoreeritakse alati loomise puhul (andmebaas genereerib selle ise)
> - `seller` väli `SellerContact` entity-s — miks seda mapper ei sea? (Vihje: sellerId tuleb URL-ist, mitte DTO-st — selle seab service käsitsi)

> **Mõtle:** Milliseid välju mapper automaatselt kaardistab (sama nimi mõlemas)?  
> Vaata `SellerContactDto` välju ja `SellerContact` entity välju — kus nimed kattuvad?

---

## Samm 4 — ContactRoleRepository parandamine

### Mida teha?

`ContactRoleRepository.java` on loodud, aga praegu tühi — see ei laiene `JpaRepository`-lt ega sisalda ühtegi meetodit. Lisa mõlemad.

Ava `ContactRoleRepository.java`. See peaks välja nägema praegu nii:

```java
public interface ContactRoleRepository {
}
```

### Vajalikud muudatused

1. **Laienda `JpaRepository`-t** — samamoodi nagu teised repositooriumid projektis  
   Vaata näiteks `SellerContactRoleRepository.java` — kuidas see laiendab?

2. **Lisa `findByCode` meetod** — leiab `ContactRole` entity tema `code` välja järgi (nt "L", "A", "R", "T")

> **Mõtle:** Kas see meetod tagastab ühe või mitu rida?  
> `code` veerg on unikaalne — seega tuleb üks tulemus.

> **Kas JPQL `@Query` on vajalik?**  
> Spring Data JPA oskab lihtsa ühe veeru järgi otsingut automaatselt genereerida meetodi nime põhjal.  
> Meetodi nimi `findBy[VäljaNimi]` töötab ilma `@Query`-ta!

Tulemus peaks välja nägema umbes nii:

```java
public interface KlassiNimi extends JpaRepository<EntiteetTüüp, IdTüüp> {
    Optional<EntiteetTüüp> findByKodeVäli(String kodeVäärtus);
}
```

> **Vihje miks `Optional`?** Kui koodi ei leita andmebaasist, tagastab `Optional.empty()` asemel `null`.  
> Service-s saad siis kasutada `.orElseThrow(...)` — sama muster nagu mujal projektis.

---

## Samm 5 — tagasi Service'i (täielik loogika)

### Mida teha?

Nüüd on Mapper ja Repository valmis. Täienda `addSellerContact` meetodit lõpuni.

Peale valideerimist pead tegema kolm asja:
1. Luua `SellerContact` entity ja salvestada see andmebaasi
2. Iga rolli kohta DTOs leida vastav `ContactRole` entity ning salvestada `SellerContactRole` kirje

### Seller entity hankimine

Mapper vajab `Seller` objekti `SellerContact.seller` välja seadmiseks, aga mapper ise ei tea `sellerId`-st midagi — seega service peab Seller ise leidma ja entity-le käsitsi seadma.

Vaata, kuidas `SellerContactService`-s on `validateSellerExists` tehtud — see kontrollib ainult `existsById`. Kontakti lisamiseks on vaja kogu `Seller` objekti.

```java
Entiteet entiteet = mingiRepository.findById(id)
        .orElseThrow(() -> new DataNotFoundException(VIGA.getMessage(), VIGA.getErrorCode()));
```

### Kontakti salvestamine

Peale Seller entity leidmist:
1. Kutsu mapper meetodit — saa `SellerContact` entity
2. Sea `seller` väli entity-le käsitsi (`contact.setSeller(seller)`)
3. Salvesta `sellerContactRepository.save(contact)` abil

> **Tähelepanu:** `save()` tagastab salvestatud entity koos genereeritud `id`-ga.  
> Seda `id`-d läheb vaja järgmises sammus rollide salvestamiseks!

```java
EntiteetTüüp salvestatud = repositoorium.save(entiteet);
// salvestatud.getId() on nüüd saadaval
```

### Rollide salvestamine

DTO sisaldab `roles` välja — see on `List<String>` rolli koodidega (nt `["L", "A"]`).

Iga rolli kohta pead:
1. Leidma `ContactRole` entity `ContactRoleRepository.findByCode(kood)` abil
2. Looma uue `SellerContactRole` objekti
3. Seadma sellele nii salvestatud `SellerContact` kui ka leitud `ContactRole`
4. Salvestama `sellerContactRoleRepository.save(...)` abil

```java
for (String rollKood : dto.getRoles()) {
    EntiteetTüüp seotudEntiteet = seotudRepository.findByVäli(rollKood)
            .orElseThrow(() -> new DataNotFoundException(...));
    VaheTabelEntiteet vaheTabelRida = new VaheTabelEntiteet();
    vaheTabelRida.setPeaEntiteet(salvestatud);
    vaheTabelRida.setSeotudEntiteet(seotudEntiteet);
    vaheTabelRepository.save(vaheTabelRida);
}
```

> **Mõtle:** Mis viga tuleb, kui rolli kood ei leidu `role` tabelist?  
> Vaata taskifailist veaolukordade sektsiooni — kas see olukord on seal kirjas?  
> Mis exception klassi ja ErrorResponse enum-i kasutad?

> **IntelliJ vihje:** Kui `contactRoleRepository` on punasega alla joonitud (puudub service väljas),  
> vajuta **Alt+Enter** → "Add field" → IntelliJ lisab väljaDeklaratsiooni automaatselt!

### Service meetodi struktuur kokkuvõttes

Peale täielikku implementatsiooni peaks meetod delegeerima privaatsetele meetoditele:

```java
@Transactional
public void meetodiNimi(Integer userId, Integer sellerId, DtoTüüp dto) {
    validateUserIsAdmin(userId);
    validateSellerExists(sellerId);
    looAndSalvestaKontakt(sellerId, dto);
}
```

> **Miks eraldada `private` meetodiks?**  
> Avalik meetod peaks olema 3–7 rida — see on projektis kasutatav konventsioon.  
> Vaata `deleteSellerContact` — sama muster: avalik meetod delegeerib privaatsetele.

---

## Samm 6 — tagasi RestController'isse

### Mida teha?

Controller on juba `@ResponseStatus(HttpStatus.CREATED)` annotatsiooniga — HTTP 201 tuleb automaatselt.

Kontrolli, et `@ApiResponses` annotatsioonil on kõik vastuskoodid taskifailist:
- 201 — OK (kontakt lisatud)
- 400 — `@NotBlank` valideerimine ebaõnnestus
- 403 — kasutajal pole Admin rolli
- 404 — edasimüüjat ei leitud
- 500 — serveri viga

400 vastuskoodi puhul ei pea `content = @Content(...)` täpsustama — Spring validatsioon käsitleb seda automaatselt.

---

## Samm 7 — kood ilusaks (refactor)

### Make it work → Make it beautiful

Kui kood töötab, vaata `addSellerContact` meetodit üle.

**Kontrolli meetodite järjekorda:**
- `public` meetodid enne, `private` meetodid pärast
- Privaatmeetodite järjekord: väljakutsumise hierarhia järgi (peameetod üleval, helper meetodid all)

**Nimede kontroll:**
- Kas privaatmeetodite nimed järgivad konventsiooni? (`createAndSave*`, `validate*`, `addXxxToYyy`)
- Kas muutujate nimed on selged?

---

## Kokkuvõte ja kontrollnimekiri

Enne kui pead koodi valmis, kontrolli läbi:

- [ ] `SellerContactController` — `@PostMapping` meetod on olemas, `@ResponseStatus(HttpStatus.CREATED)`, `@Valid @RequestBody`, `@Operation` + `@ApiResponses`
- [ ] `SellerContactService` — `addSellerContact` meetod on `@Transactional`, delegeerib privaatsetele meetoditele
- [ ] `SellerContactMapper` — `toSellerContact(SellerContactDto dto)` on olemas, `id` ja `seller` on ignoreeritud
- [ ] `ContactRoleRepository` — laiendab `JpaRepository<ContactRole, Integer>`, sisaldab `findByCode` meetodit
- [ ] Rollide salvestamine: iga rolli kohta luuakse `SellerContactRole` kirje `seller_role` tabelisse
- [ ] `@Transactional` on service meetodil — rollide salvestamine käib sama tehinguga
- [ ] Meetodite järjekord: `public` enne, `private` pärast

---

> **Järgmine samm:** Testi endpointi Swagger UI kaudu (`http://localhost:8080/swagger-ui/index.html`)  
> Saada POST päring koos DTO näidisandmetega ja kontrolli, et:
> - HTTP 201 tuleb tagasi
> - `seller_contact` tabelis on uus rida
> - `seller_role` tabelis on üks rida iga rolli kohta
