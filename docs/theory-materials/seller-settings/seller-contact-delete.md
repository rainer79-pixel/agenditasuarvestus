# DELETE kontakt — algajatele

## Mis see on ja miks oluline?

See endpoint kustutab edasimüüja kontakti koos kõigi tema rollidega.
Ainult Admin saab kustutada — tavalised kasutajad saavad kontakte ainult vaadata.

**Päriselust analoogia:** Kujuta ette paberit kus on kaks külge — ühel pool on kontakti andmed (nimi, telefon, email), teisel pool on tema rollid (lepinguline, raamatupidaja jne). Kui tahad paberit hävitada, pead kõigepealt tagakülje tühjendama — muidu jääb tühi tagakülg hõljuma. Andmebaasis on täpselt sama loogika.

---

## Mis on selles taskis uut võrreldes bank40 projektiga?

Bank40-s õppisime GET, POST, PUT päringuid. See task lisab DELETE — ja toob kaks uut kontseptsiooni:

| Uus asi | Kus näed | Mida tähendab |
|---------|---------|---------------|
| **FK constraint — kustuta laps enne vanemat** | `SellerContactService.deleteContactWithRoles()` | Andmebaas ei luba kustutada rida millele teisest tabelist viidatakse — esmalt kustuta viitajad |
| **`@Modifying` + `@Transactional` repositorys** | `SellerContactRoleRepository` | JPQL DELETE päring vajab need mõlemad — ilma nendeta Spring ei luba andmeid muuta |
| **`@RequestParam` päringuparameetrina** | `SellerContactController.deleteSellerContact()` | DELETE päringul pole body — userId tuleb URL-i lõpus `?userId=1` kujul |

Kõik muu (`@PathVariable`, `DataNotFoundException`, `ForbiddenException`, `validateUserIsAdmin` muster, `@Transactional` servicel) on tuttav eelmistest taskidest.

---

## Andmevoog — suur pilt

```
BROWSER (Admin vajutab "Kustuta" nuppu)
    ↓
FRONTEND (SellerContactService.js saadab DELETE päringu)
    ↓
AXIOS → DELETE /api/seller/3/contacts/7?userId=1
    ↓  ← siit algab backend
CONTROLLER (võtab userId, sellerId, contactId kätte)
    ↓
SERVICE (kontrollib rolli → seller olemas? → kontakt olemas? → kustutab)
    ↓
REPOSITORY (kustutab kõigepealt seller_role read, siis seller_contact rea)
    ↓
ANDMEBAAS (2 DELETE lauset)
    ↓
CONTROLLER (tagastab 200 OK, tühi vastus)
    ↓
FRONTEND (eemaldab kontakti nimekirjast)
    ↓
BROWSER (kontakt kadus ekraanilt)
```

---

## BACKEND

### 1. Miks peab rollid enne kustutama — FK constraint

Kujuta ette et andmebaasis on praegu sellised andmed:

```
seller_contact tabel:
id  | first_name | last_name
----+------------+----------
 7  | Jaan       | Tamm        ← tahame selle kustutada

seller_role tabel:
id  | seller_contact_id | seller_role_id
----+-------------------+---------------
 12 |         7         |      1        ← viitab Jaan Tammele
 13 |         7         |      3        ← viitab Jaan Tammele
```

`seller_role.seller_contact_id` väärtus `7` on **foreign key** — see on viide `seller_contact.id = 7` reale.

Kui proovid kustutada `seller_contact` rea id=7, ütleb andmebaas:
> "Ei saa! `seller_role` tabelis on read (id=12 ja id=13) mis viitavad sellele reale. Kui kustutaksin, jääksid need read viitama olematule kontaktile — see rikuks andmete tervikluse."

See on **turvamehhanism** — andmebaas kaitseb sind enda eest, et sa ei jätaks "orvuks" andmeid.

**Lahendus — kustuta õiges järjekorras:**

```
1. Kustuta seller_role read kus seller_contact_id = 7
   → seller_role tabel on nüüd tühi (read 12 ja 13 on kadunud)

2. Kustuta seller_contact rida kus id = 7
   → nüüd on ok, keegi ei viita enam sellele reale
```

Koodis:
```java
private void deleteContactWithRoles(Integer contactId) {
    sellerContactRoleRepository.deleteAllBySellerContactId(contactId);  // 1. rollid
    sellerContactRepository.deleteById(contactId);                       // 2. kontakt
}
```

Kui vahetaksid read ära, saaksid kohe vea:
```
ERROR: update or delete on table "seller_contact" violates foreign key constraint
"seller_role_seller_contact_id_fkey" on table "seller_role"
```

---

### 2. `@Modifying` ja `@Transactional` — miks repositorys JA servicel?

Siin on koht mis tekitab segadust — `@Transactional` on koodis **kahes kohas**:

```java
// SellerContactRoleRepository — repositorys
@Modifying
@Transactional                          // ← siin
@Query("delete from SellerContactRole scr where scr.sellerContact.id = :contactId")
void deleteAllBySellerContactId(Integer contactId);
```

```java
// SellerContactService — servicel
@Transactional                          // ← ja siin
public void deleteSellerContact(...) { ... }
```

**Miks `@Transactional` repositorys?**

Spring Data JPA repositorys on vaikimisi ainult **lugemiseks** mõeldud — `findById`, `findAll` jne töötavad ilma lisaannotatsioonideta. Aga kui kirjutad oma JPQL DELETE päringu `@Query`-ga, pead kaks asja selgelt ütlema:

| Annotatsioon | Mida ütleb Spring'ile | Mis juhtub ilma selleta |
|---|---|---|
| `@Modifying` | "See päring **muudab** andmeid, mitte ei loe" | `InvalidDataAccessApiUsageException` viga kohe |
| `@Transactional` | "Käivita see tehingus" | `TransactionRequiredException` viga kohe |

**Miks `@Transactional` ka servicel?**

Servicel on `@Transactional` hoopis teistel põhjustel — see **hõlmab kogu operatsiooni**:

```java
@Transactional   // ← avab ühe ühise tehingu kogu meetodi jaoks
public void deleteSellerContact(...) {
    sellerContactRoleRepository.deleteAllBySellerContactId(contactId);  // DELETE 1
    sellerContactRepository.deleteById(contactId);                       // DELETE 2
}
```

Kui servicel on `@Transactional`, siis mõlemad DELETE laused käivad **ühe tehingu sees**. See tähendab: kui esimene DELETE õnnestub aga teine ebaõnnestub (näiteks andmebaas katkestab), siis **keeritatakse mõlemad tagasi** — andmebaas jääb täpselt selliseks nagu oli enne.

**Ühesõnaga:**
- Repositorys `@Transactional` — **kohustuslik** selleks, et JPQL DELETE üldse töötaks
- Servicel `@Transactional` — **turvavõrk** mitme operatsiooni atomaarsuse tagamiseks

**Kaks erinevat paketti — kas see on probleem?**

Repositorys kasutasime `jakarta.transaction.Transactional`, servicel saab kasutada mõlemat:
- `jakarta.transaction.Transactional` — Java standard
- `org.springframework.transaction.annotation.Transactional` — Spring'i oma versioon

Spring Boot projektis töötavad mõlemad. Konventsioonina kasutatakse servicel Spring'i versiooni (`org.springframework`), sest see pakub Spring-spetsiifilisi lisavõimalusi. Tulemus on sama — see on lihtsalt stiilieelistus.

---

### 3. `@RequestParam` vs `@PathVariable` — millal kumb

```java
public void deleteSellerContact(@RequestParam Integer userId,       // ?userId=1
                                @PathVariable Integer sellerId,     // /seller/3/
                                @PathVariable Integer contactId) {  // /contacts/7
```

**`@PathVariable`** — väärtus on URL-i teel sees:
```
/api/seller/3/contacts/7
             ↑         ↑
         sellerId   contactId
```

**`@RequestParam`** — väärtus tuleb URL-i lõpus `?` järel:
```
/api/seller/3/contacts/7?userId=1
                         ↑
                       userId
```

**Miks `userId` on `@RequestParam`, mitte `@PathVariable`?**
- URL `/api/seller/{sellerId}/contacts/{contactId}` kirjeldab *ressurssi* mida töödeldakse
- `userId` on *kontekst* (kes küsib) — see ei ole ressursi osa
- DELETE päringul pole request body — seega päringuparameeter on loogiline koht

---

### 4. Service — valideerimise järjekord

```java
@Transactional
public void deleteSellerContact(Integer userId, Integer sellerId, Integer contactId) {
    validateUserIsAdmin(userId);      // 1. kas õigus olemas?
    validateSellerExists(sellerId);   // 2. kas edasimüüja olemas?
    validateContactExists(contactId); // 3. kas kontakt olemas?
    deleteContactWithRoles(contactId); // 4. kustuta
}
```

Järjekord on oluline — kontrollid käivad üldisemast spetsiifilisemaks:
1. Esmalt õiguste kontroll — vale roll annab kohe 403, ei vaata andmeid üldse
2. Siis eksisteerimise kontrollid — üldisem (seller) enne spetsiifilisemat (contact)
3. Viimasena tegelik toiming

**`@Transactional` servicel** tähendab: kui `deleteAllBySellerContactId` õnnestub aga `deleteById` ebaõnnestub (näiteks voolukatkestus), siis keeritatakse kogu operatsioon tagasi. Andmebaas ei jää pooleldi kustutatusse seisu.

---

### 5. Controller

```java
@DeleteMapping("/seller/{sellerId}/contacts/{contactId}")
@Operation(summary = "Kustuta kontakt")
@ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "403", description = "Pole õigust", ...),
        @ApiResponse(responseCode = "404", description = "Edasimüüjat või kontakti ei leitud", ...),
        @ApiResponse(responseCode = "500", description = "Serveri viga", ...)})
public void deleteSellerContact(@RequestParam Integer userId,
                                @PathVariable Integer sellerId,
                                @PathVariable Integer contactId) {
    sellerContactService.deleteSellerContact(userId, sellerId, contactId);
}
```

- `@DeleteMapping` — HTTP DELETE meetod (GET loeb, POST loob, PUT muudab, DELETE kustutab)
- `void` tagastustüüp — vastuses pole keha, ainult HTTP 200 staatus
- `@Operation` + `@ApiResponses` — Swagger UI dokumentatsioon

### Veakoodid

| HTTP kood | Olukord | Sõnum | errorCode |
|-----------|---------|-------|-----------|
| 200 | Kustutamine õnnestus | — | — |
| 403 | Kasutaja pole Admin | "Teil pole selleks õigust" | 115 |
| 404 | Edasimüüjat ei leitud | "Edasimüüjat ei leitud" | 211 |
| 404 | Kontakti ei leitud | "Kontakti ei leitud" | 311 |

---

## Uued mõisted selles taskis

| Mõiste | Selgitus |
|--------|---------|
| **FK constraint (Foreign Key piirang)** | Andmebaasi reegel: ei saa kustutada rida millele teisest tabelist viidatakse — kaitseb andmete terviklust |
| **`@Modifying`** | Annotatsioon Spring Data repositorys mis ütleb: "See JPQL päring muudab andmeid" |
| **`@RequestParam`** | Kontrolleri parameeter mis loeb väärtuse URL-i lõpust (`?key=value`) |
| **`@DeleteMapping`** | Kontrolleri annotatsioon HTTP DELETE päringute käsitlemiseks |
| **Kustutamise järjekord** | FK constraint tõttu peab laps-tabelit kustutama enne vanema tabelit |

---

## Seos eelnevate taskidega

- **Task-01 (login):** `validateUserIsAdmin` kasutab sama `UserRepository.findById()` mustrit mis login
- **Task-03 (edasimüüjad):** `ForbiddenException`, `DataNotFoundException`, `ErrorResponse` enum — kõik juba tuttavad
- **Task-03 (edasimüüjad):** `@Transactional` servicel — sama muster, tagab atomaarsuse

---

## Järgmised sammud

- **POST /api/seller/{sellerId}/contacts** — kontakti lisamine (sama domeen, vastupidine suund)
- **Cascade delete** — hiljem saab FK constrainti lisada `ON DELETE CASCADE` mis kustutab rollid automaatselt; praegu on manuaalne kustutus õppeotstarbeliselt selgem