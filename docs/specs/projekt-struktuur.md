# Projekti kaustade struktuur

```
etas/
├── CLAUDE.md                           # Üldised juhised Claude Code'ile (projekti tase)
├── README.md                           # Projekti kirjeldus ja käivitusjuhend
├── backend/                            # Serveri lähtekood (Spring Boot)
│   ├── CLAUDE.md                       # Backendi juhised Claude Code'ile (Spring Boot, Java)
│   ├── gradle/                         # Gradle wrapper failid
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── ee.valiit.etas/             # Baaspakett
│   │   │   │       ├── controller/             # REST kontrollerid
│   │   │   │       │   └── {domain}/           # Kontrolleri alampakett (nt seller)
│   │   │   │       │       ├── dto/            # Andmeedastuse objektid (DTO-d)
│   │   │   │       │       └── {Domain}Controller.java
│   │   │   │       ├── infrastructure/         # Ühine infrastruktuur
│   │   │   │       │   ├── error/              # Veavastuse mudel
│   │   │   │       │   └── exception/          # Kohandatud erindiklassid
│   │   │   │       ├── persistence/            # Andmebaasi entiteedid ja repositooriumid
│   │   │   │       │   └── {domain}/           # Entiteedi alampakett (nt seller)
│   │   │   │       │       ├── {Domain}.java
│   │   │   │       │       ├── {Domain}Mapper.java
│   │   │   │       │       └── {Domain}Repository.java
│   │   │   │       └── service/                # Äriloogika teenused
│   │   │   └── resources/                      # Rakenduse konfiguratsioon
│   │   └── test/                               # Ühik- ja integratsioonitestid
│   └── [build.gradle, settings.gradle, gradlew jms]
│
├── frontend/                           # Kliendipoolne rakendus (Vue 3)
│   ├── CLAUDE.md                       # Frontendi juhised Claude Code'ile (Vue 3, Vite)
│   ├── public/                         # Avalikud staatilised failid (kopeeritakse buildi)
│   └── src/                            # Rakenduse lähtekood
│       ├── api-services/               # Axios API päringute teenused
│       ├── assets/                     # Staatilised ressursid (pildid, fondid jms)
│       ├── auth/                       # Kasutaja info haldus (localStorage)
│       ├── components/                 # Korduvkasutatavad Vue komponendid
│       │   ├── common/                 # Üldkasutatavad elemendid (AlertError jms)
│       │   ├── forms/                  # Vormi komponendid (sisendid, validatsioon)
│       │   ├── modals/                 # Modaalakende komponendid
│       │   └── tables/                 # Tabelite komponendid
│       ├── navigation/                 # Tsentraalne navigatsiooniloogika
│       ├── router/                     # Vue Router marsruutide konfiguratsioon
│       └── views/                      # Lehekülgede komponendid (marsruutidega seotud)
│
├── database/                           # SQL skriptid
│   ├── 1_reset_database.sql            # Skeemi kustutamine ja taasloomine
│   ├── 2_create.sql                    # Tabelite ja seoste loomine
│   └── 3_import.sql                    # Seed- ja testandmete import
│
└── docs/                               # Dokumentatsioon
    ├── balsamiq/                       # Balsamiq wireframe'id (PDF, BMPR, PNG)
    ├── datamodeler/                    # Andmemudel (PDF, PNG)
    ├── dtos/                           # DTO schema ja näidisandmed (schema/, examples/)
    ├── specs/                          # Projektikirjeldus, projekt-struktuur, juhendid (projectinstructions/)
    ├── tasks/                          # Ülesannete kirjeldused õpilastele (backend/, frontend/)
    └── theory-materials/               # Õppematerjalid taskide kaupa
```

## Lühikirjeldused

| Kaust | Eesmärk |
|-------|---------|
| `CLAUDE.md` | Üldised juhised Claude Code'ile — projekti struktuur, reeglid, käsud |
| `README.md` | Projekti kirjeldus, tehnoloogiad, käivitusjuhend, testandmed |
| `backend/` | Spring Boot rakendus — REST API, äriloogika, andmebaas |
| `backend/CLAUDE.md` | Backendi juhised Claude Code'ile — Spring Boot, Java konventsioonid |
| `backend/src/main/java/.../controller/` | REST kontrollerid — võtavad päringud vastu ja tagastavad vastused |
| `backend/src/main/java/.../controller/.../dto/` | DTO klassid — andmekuju päringute ja vastuste jaoks |
| `backend/src/main/java/.../infrastructure/` | Globaalne veahaldus, erindid, API veavormingud |
| `backend/src/main/java/.../persistence/` | Entiteedid, mapperid ja repositooriumid andmebaasiga suhtlemiseks |
| `backend/src/main/java/.../service/` | Äriloogika — töötleb andmeid kontrolleri ja andmebaasi vahel |
| `backend/src/main/resources/` | Rakenduse seadistused (port, andmebaas, logimine) |
| `backend/src/test/` | Ühik- ja integratsioonitestid |
| `frontend/` | Vue 3 rakendus, mida kasutaja brauseris näeb |
| `frontend/CLAUDE.md` | Frontendi juhised Claude Code'ile — Vue 3, Vite, koodistiil |
| `frontend/src/api-services/` | Kõik HTTP päringud backendiga — üks fail ressursi kohta |
| `frontend/src/auth/` | Kasutaja info (userId, firstName, lastName, role) localStorage'is |
| `frontend/src/components/` | Korduvkasutatavad Vue komponendid, jaotatud alltüüpide kaupa |
| `frontend/src/components/common/` | Üldised UI-elemendid mida kasutatakse kogu rakenduses |
| `frontend/src/components/forms/` | Vormi sisend- ja validatsiooniloogikaga seotud komponendid |
| `frontend/src/components/modals/` | Modaalakende komponendid |
| `frontend/src/components/tables/` | Tabelite kuvamiseks mõeldud komponendid |
| `frontend/src/navigation/` | Tsentraalne navigatsioon — navigateTo{View}() meetodid |
| `frontend/src/router/` | URL-ide ja vaadete vahelised seosed |
| `frontend/src/views/` | Täislehed, mida router kuvab — kasutavad komponente |
| `database/` | SQL skriptid skeemi loomiseks ja andmete importimiseks |
| `docs/balsamiq/` | Balsamiq wireframe'id koos API labelitega |
| `docs/datamodeler/` | Redgate Data Modeler — ER diagramm (PDF, PNG) |
| `docs/dtos/` | DTO schema ja näidisandmed (`schema/`, `examples/`) |
| `docs/specs/` | Projektikirjeldus, projekt-struktuur, juhendmaterjalid |
| `docs/tasks/` | Ülesannete kirjeldused õpilastele (backend/, frontend/) |
| `docs/theory-materials/` | Õppematerjalid taskide kaupa |
