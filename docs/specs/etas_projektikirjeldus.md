# ETAS — Projektikirjeldus

## 1. Projekti eesmärk

ETAS (Edasimüüja Teenustasu Arvutussüsteem) on ettevõttesiseseks kasutuseks mõeldud veebirakendus mille eesmärk on:

- Hallata edasimüüjate lepinguandmeid ja kontaktisikuid
- Arvutada teenustasusid Exceli müügiaruande põhjal
- Kontrollida edasimüüjate esitatud arvete õigsust
- Esitada juhtkonnale müügi- ja teenustasude ülevaateid

## 2. Tehnoloogiad

| Kiht | Tehnoloogia | Komponendid |
|---|---|---|
| Frontend | Vue.js 3 | Pinia, Vue Router 5, Axios, Bootstrap 5, @phosphor-icons/vue |
| Backend | Spring Boot 4.0.6 | Spring Security, JPA, MapStruct 1.6, Lombok, Springdoc OpenAPI 3.x, p6spy, Apache POI |
| Andmebaas | PostgreSQL 17 | Schema-põhine (`etas`), lokaalne arendus |
| Build | Gradle (back), Vite (front) | — |
| Versioonihaldus | Git + GitHub | Feature branch workflow |
| Andmemudel | Redgate Data Modeler | ER diagramm, SQL skriptide genereerimine |
| Dokumentatsioon | Balsamiq Cloud | Wireframe'id koos API labelitega |

## 3. Kasutajarollid

Süsteemil on kaks rolli. **User** on igapäevane tööriista kasutaja — teeb arvutusi ja vaatab tulemusi. **Admin** haldab lisaks süsteemi seadistusi.

| Funktsioon | Admin | User |
|---|---|---|
| Edasimüüjate vaatamine | ✓ | ✓ |
| Edasimüüjate lisamine ja muutmine | ✓ | ✗ |
| Edasimüüja deaktiveerimine | ✓ | ✗ |
| Teenustasude seadistamine | ✓ | ✗ |
| Excel faili üleslaadimine | ✓ | ✓ |
| Aruannete vaatamine + XLS eksport | ✓ | ✓ |
| Arvete sisestamine ja kontroll | ✓ | ✓ |
| Kasutajate haldus | ✓ | ✗ |
| KM määra muutmine | ✓ | ✗ |
| Seadistuste vaatamine ja muutmine | ✓ | ✗ |

## 4. Vaadete struktuur (10 vaadet)

| # | Failinimi | URL | Kirjeldus |
|---|---|---|---|
| 1 | `HomeView.vue` | `/` | Avalehekülg — logo, kirjeldus, "Logi sisse" nupp |
| 2 | `LoginView.vue` | `/login` | Sisselogimine e-maili ja parooliga |
| 3 | `DashboardView.vue` | `/dashboard` | Töölaud — edasimüüjate arv ja viimane import |
| 4 | `SellersView.vue` | `/sellers` | Edasimüüjate nimekiri — otsing, vaata, muuda |
| 5 | `SellerView.vue` | `/seller/{sellerId}` | Detailvaade — üldandmed, kontaktid, teenustasud, piirkonnad (ainult lugemiseks) |
| 6 | `SellerFormView.vue` | `/seller/form` | Põhiandmete lisamine ja muutmine — URL-is `?sellerId={sellerId}` muutmisrežiimis |
| 7 | `SellerSettingsView.vue` | `/seller/{sellerId}/settings` | Haldusvaade — kontaktid, piirkonnad, teenustasud (kolm eraldi modali) |
| 8 | `ReportsView.vue` | `/reports` | Aruanded — Excel import + laiendatava reaga aruande ülevaade |
| 9 | `InvoiceControlView.vue` | `/invoice-control` | Arvete kontroll — arve sisestamine ja võrdlus arvutusega |
| 10 | `SettingsView.vue` | `/settings` | Seadistused — kasutajad, KM määr, kontaktide rollid (ainult Admin) |

## 5. API endpointid ja DTO-d

### 5.1 Login
| Meetod | Endpoint | Kirjeldus |
|---|---|---|
| POST | `/api/login` | Sisselogimine |

**Sisend:** `LoginDto.java`
```json
{
  "email": "mari@agent.ee",
  "password": "********"
}
```

**Vastus:** `LoginResponseDto.java`
```json
{
  "userId": 1,
  "firstName": "Mari",
  "middleName": null,
  "lastName": "Maasikas",
  "role": "A"
}
```

**Veateated:**
- `200 OK` — sisselogimine õnnestus
- `400 Bad Request` — email või parool puudub
- `403 Forbidden` — vale email või parool

---

### 5.2 Dashboard
| Meetod | Endpoint | Kirjeldus |
|---|---|---|
| GET | `/api/dashboard` | Dashboard statistika |

**Vastus:** `DashboardResponseDto.java`
```json
{
  "sellerCount": 5,
  "lastImport": "15.04.2026"
}
```

---

### 5.3 Edasimüüjad
| Meetod | Endpoint | Kirjeldus |
|---|---|---|
| GET | `/api/seller/user/{userId}` | Kõik edasimüüjad |
| GET | `/api/seller/{sellerId}` | Edasimüüja üldandmed |
| GET | `/api/seller/{sellerId}/contacts` | Edasimüüja kontaktid |
| GET | `/api/seller/{sellerId}/regions` | Edasimüüja piirkonnad |
| GET | `/api/seller/{sellerId}/commission-rates` | Edasimüüja teenustasud |
| POST | `/api/seller/user/{userId}` | Lisa uus edasimüüja |
| PUT | `/api/seller/{sellerId}` | Muuda edasimüüja andmeid |
| PUT | `/api/seller/{sellerId}/status` | Aktiveeri või deaktiveeri edasimüüja |
| POST | `/api/seller/{sellerId}/contacts` | Lisa kontakt |
| DELETE | `/api/seller/{sellerId}/contacts/{contactId}` | Kustuta kontakt |
| POST | `/api/seller/{sellerId}/regions` | Lisa piirkond |
| PUT | `/api/seller/{sellerId}/regions/{regionId}` | Uuenda piirkonna müügipunktide arvu |
| DELETE | `/api/seller/{sellerId}/regions/{regionId}` | Kustuta piirkond |
| POST | `/api/seller/{sellerId}/commission-rates` | Lisa teenustasu |
| PUT | `/api/seller/{sellerId}/commission-rates/{commissionRateId}` | Muuda teenustasu |
| DELETE | `/api/seller/{sellerId}/commission-rates/{commissionRateId}` | Kustuta teenustasu |

**Nimekiri ja detailvaade:** `SellerDetailResponseDto.java`
```json
[{ "sellerId": 1, "companyName": "ETAS AS", "orgId": 10406134, "status": "ACTIVE",
   "contractStart": "01.01.2024", "contractEnd": null, "notes": "Märkused" }]
```

**Piirkonnad:** `SellerRegionResponseDto.java`
```json
[{ "regionId": 1, "regionName": "Tallinn", "salesPointCount": 10 }]
```

**Kontaktid:** `SellerContactResponseDto.java`
```json
[{ "contactId": 1, "firstName": "Mari", "middleName": null, "lastName": "Maasikas",
   "phone": "55555555", "email": "mari@agent.ee", "roles": ["L", "A"] }]
```

**Teenustasud:** `CommissionRateResponseDto.java`
```json
[{ "commissionRateId": 1, "productTypeName": "Isikustamine", "feePerTransaction": 0.01,
   "feePercent": 0, "includesVat": true, "validFrom": "01.01.2024", "validTo": null }]
```

**Uus edasimüüja / muutmine:** `SellerDto.java`
```json
{ "companyName": "ETAS AS", "orgId": 10406134,
  "contractStart": "2024-01-01", "contractEnd": null, "notes": "Märkused" }
```

**Staatus:** `SellerStatusDto.java`
```json
{ "status": "INACTIVE" }
```

**Veateated:**
- `200 OK` — andmed laaditud / salvestatud
- `201 Created` — uus edasimüüja lisatud
- `400 Bad Request` — kohustuslik väli puudub
- `403 Forbidden` — kasutajal pole õigust (ainult Admin)
- `404 Not Found` — edasimüüjat ei leitud
- `409 Conflict` — selle org ID-ga edasimüüja on juba olemas

---

### 5.4 Piirkonnad ja tootegrupid
| Meetod | Endpoint | Kirjeldus |
|---|---|---|
| GET | `/api/region` | Kõik piirkonnad |
| GET | `/api/product-type` | Tootegruppide nimekiri |
| POST | `/api/product-type` | Lisa uus tootegrupp |
| DELETE | `/api/product-type/{productTypeId}` | Kustuta tootegrupp |

**Piirkond:** `RegionResponseDto.java`
```json
[{ "regionId": 1, "regionName": "Tallinn" }]
```

**Tootegrupp:** `ProductTypeResponseDto.java`
```json
[{ "productTypeId": 1, "productTypeName": "Isikustamine" }]
```

---

### 5.5 Aruanded
| Meetod | Endpoint | Kirjeldus |
|---|---|---|
| POST | `/api/import/user/{userId}` | Laadi üles Excel müügiaruanne |
| DELETE | `/api/import/user/{userId}/{period}` | Kustuta impordi andmed perioodist |
| GET | `/api/report/user/{userId}` | Aruannete nimekiri |
| GET | `/api/report/{sellerId}/{period}` | Aruande detailid |
| GET | `/api/report/user/{userId}/export` | Ekspordi aruanne Excelisse |

**Import:** `multipart/form-data`
```
file: (binary .xlsx)
```
> Periood loetakse automaatselt Exceli `a_date` veerust — eraldi `period` parameetrit ei saadeta.

**Nimekiri:** `ReportResponseDto.java`
```json
[{ "sellerId": 1, "companyName": "ETAS AS", "period": "04.2026",
   "transactionCount": 1640, "feeAmount": 380.00, "salesAmount": 12500.00,
   "vatAmount": 83.60, "totalFee": 463.60 }]
```

**Detailid:** `ReportDetailResponseDto.java`
```json
[{ "productTypeName": "Isikustamine", "salesAmount": 0, "transactionCount": 100,
   "feePerTransaction": 0.01, "feePercent": 0, "calculatedFee": 1.00,
   "vatAmount": 1.00, "totalFee": 1.22 }]
```

**Veateated:**
- `200 OK` — andmed laaditud
- `400 Bad Request` — vale failivorming
- `404 Not Found` — edasimüüjat ei leitud failist
- `409 Conflict` — sellel perioodil aruanne juba olemas

---

### 5.6 Arvete kontroll
| Meetod | Endpoint | Kirjeldus |
|---|---|---|
| GET | `/api/invoice/user/{userId}` | Arvete nimekiri |
| POST | `/api/invoice/user/{userId}` | Sisesta arve |
| DELETE | `/api/invoice/number/{invoiceNumber}` | Kustuta arve |

**Nimekiri:** `InvoiceResponseDto.java`
```json
[{ "invoiceId": 1, "sellerId": 1, "companyName": "ETAS AS", "period": "04.2026",
   "calculatedFee": 380.00, "invoiceAmount": 380.00, "difference": 0.00, "status": "KORRAS" }]
```

**Salvestamine:** `InvoiceDto.java`
```json
{ "sellerId": 1, "period": "04.2026", "invoiceNumber": "ETAS-2026-004",
  "invoiceAmount": 380.00, "invoiceDate": "05.05.2026", "notes": "Märkused" }
```

**Veateated:**
- `200 OK` — andmed laaditud / arve salvestatud
- `201 Created` — uus arve lisatud
- `404 Not Found` — edasimüüjat ei leitud
- `409 Conflict` — sellel perioodil arve juba olemas

---

### 5.7 Seadistused
| Meetod | Endpoint | Kirjeldus |
|---|---|---|
| GET | `/api/settings/vat` | KM määr |
| PUT | `/api/settings/vat/user/{userId}` | Uuenda KM määra |
| GET | `/api/user` | Kasutajate nimekiri |
| POST | `/api/user` | Lisa uus kasutaja |
| PUT | `/api/user/{userId}` | Muuda kasutajat |
| PUT | `/api/user/{userId}/status` | Deaktiveeri kasutaja |

**KM vastus:** `VatSettingResponseDto.java`
```json
{ "vatRate": 22, "updatedAt": "01.01.2024", "updatedBy": "Mari Maasikas" }
```

**KM salvestamine:** `VatSettingDto.java`
```json
{ "vatRate": 22 }
```

**Kasutajate nimekiri:** `UserResponseDto.java`
```json
[{ "userId": 1, "firstName": "Mari", "middleName": null, "lastName": "Maasikas",
   "email": "mari@agent.ee", "role": "A", "status": "ACTIVE" }]
```

**Kasutaja salvestamine:** `UserDto.java`
```json
{ "firstName": "Mari", "middleName": null, "lastName": "Maasikas",
  "email": "mari@agent.ee", "password": "********", "role": "A" }
```

**Kasutaja staatus:** `UserStatusDto.java`
```json
{ "status": "INACTIVE" }
```

**Veateated:**
- `200 OK` — andmed laaditud / salvestatud
- `201 Created` — uus kasutaja lisatud
- `403 Forbidden` — ainult Admin pääseb ligi
- `409 Conflict` — email juba kasutusel

---

## 6. Olulised otsustused

- **Login emailiga** — email on alati unikaalne, kasutaja mäletab seda alati
- **Autentimine lihtne (localStorage-põhine)** — Spring Security puudub, sessioon puudub; backend tagastab kasutajainfo JSON-ina, frontend salvestab `localStorage`-i. BCrypt ja Spring Security lisatakse hiljem.
- **Edasimüüjat ei kustutata** — ainult deaktiveeritakse/aktiveeritakse (`PUT /api/seller/{sellerId}/status`)
- **Kasutajat ei kustutata** — ainult deaktiveeritakse (`PUT /api/user/{userId}/status`)
- **Kontakte saab kustutada** — need ei ole ajalooliselt kriitilised
- **SellerView on ainult lugemiseks** — muutmiseks eraldi SellerSettingsView
- **Edasimüüja lisamine kahes sammus** — SellerFormView lisab põhiandmed, SellerSettingsView haldab kontakte/piirkondi/teenustasusid
- **Modaalid** — SellerSettingsView avab kolm eraldi modali (kontakt, piirkond, teenustasu)
- **Excel import fikseeritud formaadiga** — `docs/specs/0.6_Issuer_sales_report.xlsx`
- **Ühel perioodil saab olla ainult üks aruanne**
- **KM määr seadistatav** — praegu 24%, Admin saab muuta ilma arendajata; `vat_setting` toetab mitut kirjet `valid_from_date` / `valid_to_date` perioodidega — õige KM määr leitakse impordi perioodi järgi

## 7. Teenustasu arvutuse loogika

**Samm 1 — Teenustasu koguselt:**
```
teenustasu = tehingute_arv × tasu_ühe_tehingu_kohta
Näide: 100 tehingut × 0.01 EUR = 1.00 EUR
```

**Samm 2 — Teenustasu summalt:**
```
teenustasu = müügisumma × teenustasu_protsent
Näide: 2500 EUR × 1% = 25.00 EUR
```

**Samm 3 — KM kontroll:**
```
KUI includes_vat = true:  lõplik_summa = arvutatud_teenustasu
KUI includes_vat = false: lõplik_summa = arvutatud_teenustasu × (1 + vat_rate/100)
KM määr võetakse andmebaasist vat_setting tabelist
```

## 8. Andmemudel

![Andmemudel](../datamodeler/etas.dbmodel.png)

Tabelid: `app_user`, `vat_setting`, `region`, `role`, `product_type`, `seller`, `seller_contact`, `seller_role`, `seller_region`, `commission_rate`, `sales_report`, `sales_report_detail`, `commission_calculation`, `invoice`

Vaated: `commission_calculation_view`

**Muutunud tabelite struktuur:**

`vat_setting` — lisandusid `valid_from_date DATE`, `valid_to_date DATE`, `status VARCHAR(20)`. Toetab nüüd mitut kirjet erinevate kehtivusperioodidega.

`sales_report` — jagati kaheks tabeliks:
- `sales_report` (id, created_by, period, created_at) — ühe impordi sessioon
- `sales_report_detail` (id, sales_report_id FK, seller_id FK, department_name, department_id, payment_channel, product_type, transaction_count, sales_amount, fee, period, region, created_at) — kõik Exceli read

`commission_calculation` — veerg `total_fee` asemel on `calculated_fee_plus_vat`.

## 9. Arenduse järjekord

### Backend (Spring Boot):
1. User + login/autentimine (lihtne — email+password+status, ilma Spring Security ja BCrypt-ita)
2. Seller CRUD
3. Contact + contact_role CRUD
4. Region (seed-andmed) + Seller_region CRUD
5. ProductType CRUD
6. Commission_rate CRUD
7. Excel import + valideerimine (Apache POI)
8. Teenustasu arvutus + KM
9. Aruannete endpointid
10. Invoice CRUD
11. Dashboard endpoint

### Frontend (Vue.js 3):
1. HomeView.vue
2. LoginView.vue
3. DashboardView.vue
4. SellersView.vue
5. SellerView.vue
6. SellerFormView.vue
7. SellerSettingsView.vue (+ 3 modali)
8. ReportsView.vue
9. InvoiceControlView.vue
10. SettingsView.vue

## 10. MVP vs Nice to Have

### MVP — kindlasti valmis:
- Sisselogimine emailiga
- Edasimüüjate CRUD (deaktiveerimisega)
- Kontaktide haldus (mitu rolli ühel kontaktil)
- Teenustasude määrade seadistamine tootegruppide kaupa
- Piirkonnad + müügipunktide arv
- Excel import (fikseeritud formaat)
- Teenustasu arvutus (koguselt + summalt)
- KM automaatne lisamine vajadusel
- KM määr seadistatav Admin poolt
- Aruannete ülevaade laiendatava reaga
- Arvete kontroll
- Kasutajate haldus (Admin)
- Kaks rolli: Admin + User

### Nice to have — tulevikus:
- Unustasin parooli
- Rollide tooltip — kontaktide rollide selgitus UI-s
- is_primary kontakt — põhikontakti märkimine
- PDF eksport — aruannete eksport PDF formaadis
- Email saatmine — automaatne teavitus arve mittevastavuse korral
- Perioodipõhine statistika Dashboardil
- Graafikud — müük ja teenustasud visuaalselt
- Automaatne teavitus — arve ei klapi arvutusega
- Piirkondlik teenustasu erisus — commission_rate tabelisse nullable region veerg
- Region/ContactRole enum refaktoreerimine
- Exceli vormi dünaamiline seadistamine
- Aastavalik ReportsView perioodifiltreis dünaamiliselt esimese aruande aasta põhjal (praegu fikseeritud currentYear-5 kuni currentYear)
- Multi-select sellerid aruannete filtris (frontend filtreerimine, backi muudatust ei nõua)
