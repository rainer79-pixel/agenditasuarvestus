# Skill: ETAS Theory Materials

Sinu ülesanne on luua ETAS projekti taskile algajasõbralik õppematerjal eesti keeles.

Kasutaja annab task numbri argumendina (nt `03`). Kui argumenti ei anta, küsi kasutajalt.

---

## Sammud enne kirjutamist

1. **Loe** vastava taski dokument `docs/tasks/task-XX/task-XX.md` — mis tehti, millised endpointid, DTOd, ärireeglid
2. **Loe** loodud backend failid — Controller, Service, Repository, Mapper, DTOd (tegelik kood, mitte ainult plaan)
3. **Loe** loodud frontend failid — View, Service, muudetud NavigationService/Router
4. **Loe** olemasolevad materjalid `docs/theory-materials/` — mõista formaati ja taset, väldi kordamist
5. **Vaata** Balsamiq wireframe selle taski vaadete kohta — mõista mida kasutaja näeb

---

## Materjali põhimõtted

- **Algajasõbralik** — selgita nagu inimesele kes näeb Spring Boot / Vue koodi esimest korda
- **Andmevoog ees** — alusta alati browser → frontend → backend → frontend → browser diagrammiga
- **Päriselust analoogiad** — iga uue mõiste juures too analoogia päriselust
- **Koodinäited koos selgitusega** — näita koodi ja selgita iga osa eraldi, mitte korraga
- **Miks, mitte ainult mis** — selgita miks nii tehti, mitte ainult mida kood teeb
- **Seos eelnevaga** — viita eelmistele taskidele kui kasutame sama mustrit uuesti
- **Uued mõisted tabelis** — taski lõpus kokkuvõte uutest mõistetest

---

## Materjali struktuur

```markdown
# Task-XX: [Taski pealkiri] — algajatele

## Mis see on ja miks oluline?

[2-3 lausega: mida see task ehitab ja miks see on vajalik süsteemis]

**Päriselust analoogia:** [konkreetne analoogia]

---

## Andmevoog — suur pilt

[ASCII diagramm: browser → Vue komponent → api-service → Axios → Controller → Service → Repository → DB → Mapper → DTO → tagasi]

---

## BACKEND

### 1. [DTO nimi] — [sisend/vastus]

[koodinäide]

[selgitus: mis see on, miks nii, mida iga annotatsioon teeb]

---

### 2. [Repository]

[koodinäide]

[selgitus]

---

### 3. [Mapper]

[koodinäide]

[selgitus — rõhuasetus teisendustel mis on keerulised: staatuse teisendus, kuupäeva formaat, ignore]

---

### 4. [Service]

[koodinäide]

[selgitus — avalik vs privaatne meetod, @Transactional, validatsioon]

---

### 5. [Controller]

[koodinäide]

[selgitus — HTTP meetod, URL, @Valid, HTTP vastuskoodid]

---

### Veakoodid

| HTTP kood | Olukord | Sõnum |
|-----------|---------|-------|
| ... | ... | ... |

---

## FRONTEND

### [N]. [SellerService.js vms] — API päringud

[koodinäide]

[selgitus]

---

### [N]. [ViewName.vue] — script

[koodinäide — data(), beforeMount(), methods()]

[selgitus]

---

### [N]. [ViewName.vue] — template põhimõtted

[selgitus v-for, v-if, v-model kasutusest selles vaates — koodinäiteid ainult keerulisemate kohtade jaoks]

---

## Uued mõisted selles taskis

| Mõiste | Selgitus |
|--------|---------|
| ... | ... |

---

## Seos eelnevate taskidega

- **Task-01:** [mis on ühine / mis mustrit kordame]
- **Task-02:** [mis on ühine]

---

## Järgmised sammud

- **Task-XX:** [mis tuleb järgmiseks]
- [Mis jääb selles taskis veel tegemata / mis tuleb hiljem]
```

---

## Salvestamine

Salvesta fail:
```
docs/theory-materials/task-XX-[lühikirjeldus].md
```

Näiteks: `task-03-edasimyyjad.md`

---

## Pärast loomist

1. Näita kasutajale faili asukoht
2. Too välja 1-2 kohta kus materjal võiks olla ebaselge algajale — küsi kas selgitada rohkem
3. Küsi kas on mõisted mis vajavad lisaselgitust
