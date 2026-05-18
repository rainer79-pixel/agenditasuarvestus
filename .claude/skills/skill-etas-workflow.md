# Skill: ETAS Task planeerimisdokument

Sinu ülesanne on koostada ETAS projekti task-ile täielik planeerimisdokument.

Kasutaja annab task numbri argumendina (nt `01`, `02`, `03`). Kui argumenti ei anta, küsi kasutajalt.

---

## Arendusstiil ja juhendamine

**See projekt on õppeprojekt — arendame koos samm-sammult.**

- **Õpilane teeb ise, Claude juhendab** — ära kirjuta koodi ette valmis. Ütle mida teha ja miks, oota et õpilane teeb, siis liigu edasi.
- **Üks samm korraga** — anna üks konkreetne ülesanne, oota kinnitust, alles siis järgmine.
- **Selgita "miks"** — iga sammu juures selgita lühidalt miks see nii tehakse. Õpilane peaks aru saama loogikast, mitte ainult kopeerima.
- **Andmevoo suund** — liigu alati andmete liikumise suunas: **browser → frontend → backend → frontend → browser**. Näide: alusta sellest mida kasutaja näeb (UI), siis mis API kutse tehakse, siis backend loogika, siis vastus tagasi.
- **Kontrolli enne järgmist sammu** — kui õpilane näitab koodi, kontrolli et on õige enne jätkamist.

---

## Sammud enne kirjutamist

1. **Loe** `docs/tasks/tasks.md` — leia vastav task numbri järgi (branch nimi, raskus, kirjeldus)
2. **Loe** `CLAUDE.md` — API endpointid, rollid, vaadete struktuur, arhitektuuriotsused
3. **Vaata** `docs/balsamiq/project/edasimüüjatasuarvestus.pdf` — kõik 14 lehte, et saada ülevaade tervikust: vaadete struktuur, DTO-d, JSON näited, veateated, seosed vaadete vahel. Balsamiq sildid on autoritatiivne spetsifikatsioon.
4. **Loe** eelnevate taskide dokumendid `docs/tasks/task-XX/` — mõista mis on juba tehtud, mis mustrid on kasutusel, mida saab taaskasutada (nt `AuthService.js`, `NavigationService.js`, olemasolevad DTOd, entityd).
5. **Loe** olemasolev kood — ära piirdu ainult CLAUDE.md dokumentatsiooniga. Loe tegelikud Java ja Vue failid (nt `LoginController.java`, `LoginService.java`, `DashboardView.vue`) et näha reaalseid mustreid — dokumentatsioon kirjeldab reeglit, kood näitab rakendust.
6. **Vaata** referentsprojektid `bank40back` ja `bank40front` — need on ETAS-i kõrval `IdeaProjects/` kaustas. Kui ETAS-is pole sarnast lahendust veel olemas, vaata kuidas bank40 seda teeb. Ühesugused ülesanded → ühesugune mõttelaad ja struktuur.
7. **Loe** `backend/CLAUDE.md` — entity, mapper, service, controller, repository konventsioonid
8. **Loe** `frontend/CLAUDE.md` — Vue Options API muster, api-services, AuthService, NavigationService
9. **Loe** `database/2_create.sql` — vajadusel tabelite struktuur

## Valmiduse kontroll enne arendust

Enne kui arendama hakkad, kontrolli kolm asja:

**1. Sõltuvused on päriselt olemas — mitte ainult plaanitud:**
- Kontrolli task dokumendis loetletud sõltuvused (nt task-03 vajab `AuthService.js` task-01-st)
- Kontrolli et vajalikud failid on olemas: `find` või `ls` käsuga
- Kui sõltuvus puudub, teavita kasutajat enne jätkamist

**2. Infrastruktuur on valmis:**
- Kontrolli et vajalikud `ErrorResponse` enum kirjed on olemas — kui puuduvad, lisa enne service kirjutamist
- Kontrolli et vajalikud exception klassid on olemas `infrastructure/exception/` kaustas

**3. API leping on selge enne esimese faili loomist:**
- Iga DTO väli, tüüp ja andmeallikas (DB tabel.veerg) peab olema selge
- Kui Balsamiqis või task dokumendis on ebaselgus (nt välja nimi erineb, tüüp pole selge), **küsi kasutajalt enne kui hakkad faile looma**
- Näide mida kontrollida: `sellerId → Integer → seller.id`, `status → String → "ACTIVE"/"INACTIVE"`

## Branch loomine

Enne planeerimisdokumendi kirjutamist tuleta kasutajale meelde, et kogu taski töö — nii arendus kui dokumentatsioon — käib eraldi branch-is. Masterisse ei commitita midagi enne kui task on täielikult valmis.

Paluda kasutajal käivitada:
```
git checkout master
git pull
git checkout -b task-XX
```
kus `XX` on tasknumber, nt `task-01`, `task-02`.

Kinnita kasutajalt et branch on loodud enne kui jätkad dokumendi genereerimisega.

---

## Dokumendi loomine

Loo kataloog `docs/tasks/task-XX-nimi/` ning sinna fail `task-XX-nimi.md`.

Kasuta alljärgnevat täpset struktuuri. Täida **kõik** osad konkreetse task info põhjal — ära jäta tühje sektsioone.

---

## Dokumendi struktuur

```markdown
# Task-XX: [Taski pealkiri]

## Mis selles taskis teeme?

[3–5 lausega vabas vormis: mida arendatakse, miks see on vajalik, kuidas see süsteemi tervikusse sobib. Kirjelda nii backend kui frontend poolt — mida kasutaja näeb ja mis toimub taustal. Sobib ka loetelu kui on mitu selget osa.]

**Backend:** [1 lause — millised endpointid / loogika]
**Frontend:** [1 lause — milline vaade / milliseid toiminguid kasutaja teeb]

---

## Ülevaade

| Väli | Väärtus |
|------|---------|
| Branch | `task-XX-nimi` |
| Raskus | X/5 |
| Sõltuvused | [loetelu eelnevatest taskidest millele tugineb] |
| Seisund | Planeeritud |

## Eesmärk

[1–2 lausega: mida see task teeb ja miks see on vajalik süsteemi tervikus]

---

## UI ülevaade

> Põhineb Balsamiq wireframe-il: `docs/balsamiq/views/XxxView.vue.png`

### [XxxView.vue] — `/xxx`

**Lehe struktuur:**
[Kirjelda lehe ülesehitust — pealkiri, sektsioonid, paigutus]

**UI elemendid:**

| Element | Tüüp | Toiming |
|---------|------|---------|
| "Lisa uus X" | Nupp (roheline) | Avab vormi / navigeerib lisamisvaatesse |
| "Salvesta" | Nupp (sinine) | Saadab POST/PUT päringu |
| "Tühista" | Nupp (hall) | Tühistab ja navigeerib tagasi |
| Otsingukast | Input | Filtreerib nimekirja reaalajas |
| Nimekiri | Tabel | Kuvab kirjeid koos tegevusnuppudega |

**Rollipõhised erinevused:**
- Admin näeb: [loetelu]
- User näeb: [loetelu, mis on peidetud]

**Navigatsioon sellest vaatest:**
- [Nupp / link] → [Sihtvaade]

---

## Backend

### Uued failid

| Fail | Kirjeldus |
|------|-----------|
| `controller/xxx/XxxController.java` | REST endpointid |
| `controller/xxx/dto/XxxDto.java` | Sisend DTO |
| `controller/xxx/dto/XxxResponseDto.java` | Vastus DTO |
| `service/XxxService.java` | Äriloogika |
| `persistence/xxx/XxxRepository.java` | Andmebaasi päringud |
| `persistence/xxx/XxxMapper.java` | Entity ↔ DTO teisendus |

### API endpointid

| Meetod | Endpoint | Sisend DTO | Vastus DTO | Õnnestub |
|--------|----------|-----------|-----------|----------|
| POST | `/api/xxx` | `XxxDto` | — | 201 Created |
| GET | `/api/xxx/{id}` | — | `XxxResponseDto` | 200 OK |

### DTO struktuurid

#### [XxxDto] — sisend
```json
{
  "field1": "väärtus",
  "field2": 0
}
```

#### [XxxResponseDto] — vastus
```json
{
  "xxxId": 1,
  "field1": "väärtus"
}
```

### Veateated

| HTTP kood | Olukord | Kasutajale kuvatav sõnum |
|-----------|---------|--------------------------|
| 400 Bad Request | Kohustuslik väli puudub | "Palun täitke kõik kohustuslikud väljad" |
| 403 Forbidden | Puudub roll/õigus | "Teil pole selleks õigust" |
| 404 Not Found | Kirjet ei leitud | "Xxx ei leitud" |
| 409 Conflict | Duplikaat | "Xxx on juba olemas" |

### Olulised ärireeglid backend-is

- [konkreetsed reeglid mis backend peab jõustama]
- [rollide kontroll vajadusel: ainult Admin / kõik kasutajad]

---

## Frontend

### Uued failid

| Fail | URL / asukoht | Kirjeldus |
|------|---------------|-----------|
| `views/XxxView.vue` | `/xxx` | [kirjeldus] |
| `api-services/XxxService.js` | — | API kutsed |

### Wireframe viide

`docs/balsamiq/views/XxxView.vue.png`

[Lühike kirjeldus mida wireframe näitab — põhilised UI elemendid, nupud, tabelid, vormid]

### Komponendi data() struktuur

```javascript
data() {
  return {
    errorMessage: '',
    successMessage: '',
    // konkreetsed andmeväljad selle vaate jaoks
  }
}
```

### API teenuse meetodid (XxxService.js)

| Meetodi nimi | HTTP kutse | Kirjeldus |
|-------------|-----------|-----------|
| `sendGetXxxs()` | `GET /api/xxx` | Laeb nimekirja |
| `sendPostXxx(xxx)` | `POST /api/xxx` | Lisab uue |

### Navigatsioon

- [Kuhu navigeeritakse pärast õnnestumist]
- [Kuhu navigeeritakse tagasi nupul]
- [Millised nupud/lingid viivad siia vaatesse]

### Rollipõhised erinevused UI-s

[Kirjelda mis on nähtav/peidetud Admin vs User rolliga — ainult kui erinev]

---

## Valmiduse kriteeriumid

### Backend
- [ ] Kõik endpointid vastavad Swagger UI-s dokumenteeritud spetsifikatsioonile
- [ ] Mapper teisendab entity ↔ DTO õigesti (kõik väljad kaardistatud)
- [ ] Õiged HTTP vastuskoodid kõigil juhtudel (200/201/400/403/404/409)
- [ ] Rollide kontroll toimib (Admin / User eristus)
- [ ] `@Transactional` kirjutusoperatsioonidel

### Frontend
- [ ] Andmed laadivad korrektselt (`beforeMount`)
- [ ] Vead kuvatakse kasutajale (`errorMessage`)
- [ ] Spinner nähtav päringu ajal
- [ ] Navigatsioon toimib (tagasi, edasi, pärast salvestamist)
- [ ] Rollipõhised nupud peidetud/nähtavad õigesti

---

## Andmevoo ülevaade

Kirjelda andmete liikumist selles taskis browser → frontend → backend → frontend → browser suunas:

```
Kasutaja tegevus (browser)
  → Vue komponent kutsub SellerService.js meetodit (frontend)
    → Axios saadab HTTP päringu backendi (frontend → backend)
      → Controller võtab vastu, delegeerib Service-le
        → Service valideerib, kutsub Repository
          → Repository pärib andmebaasist
        → Mapper teisendab Entity → DTO
      → Controller tagastab JSON vastuse
    → Axios saab vastuse, Vue komponent uuendab data() (backend → frontend)
  → Kasutaja näeb uuendatud UI-d (browser)
```

[Täpsusta selle taski konkreetse andmevooga]

## Märkused

[Erijuhud, arhitektuuriotsused, teadaolevad keerukused, sõltuvused teistest taskidest]
```

---

## Pärast faili loomist

1. Näita kasutajale loodud faili asukoht
2. Too välja peamised küsimused või kohad mis vajavad täpsustamist
3. Küsi kas dokument on heaks kiidetud enne kui arendusega alustada
4. Tuleta meelde: kogu arendus käib selles branch-is — masterisse läheb kõik koos alles siis kui task on täielikult valmis (backend + frontend + dokumentatsioon)

**NB!** Kui `docs/tasks/task-XX-nimi/` kataloog või fail juba eksisteerib, küsi kasutajalt enne ülekirjutamist.

---

## Taski lõpetamine

Kui task on valmis (backend + frontend + testitud), tee järgmist:

1. **Uuenda task dokument** — muuda `Seisund: Planeeritud` → `Seisund: Valmis`
2. **Uuenda CLAUDE.md** — lisa valmis taskis loodud endpointid, failid ja otsused "Arendusjärjekord ja seis" sektsiooni
3. **Commit ja PR** — kasuta `skill-git-pr-full-merge` skilli: branch → commit → push → PR → squash merge → cleanup
4. **Kontrolli** et master on ajakohane enne järgmise taski alustamist
