# ETAS — Arenduse taskid

Arenduse järjekord: feature kaupa, **backend enne frontend**.
Raskusskaalal 1–5: **1** = triviaalne → **5** = väga keeruline.
Branchid võetakse masterist: `git checkout -b task-XX`

---


## 0. Kõik JPA entiteedid
**Otse masterisse — branch pole vaja**

Kõik entiteedid luuakse korraga enne taske. Entiteedid on puhta andmestruktuuriga (väljad + JPA suhted), äriloogikat pole. Vundament millest kõik järgnevad branchid lähtuvad.

| Kiht | Sisaldab | Raskus |
|------|----------|--------|
| Backend | Kõik entity klassid: `AppUser`, `Seller`, `SellerContact`, `SellerRole`, `Role`, `SellerRegion`, `Region`, `ProductType`, `CommissionRate`, `SalesReport`, `CommissionCalculation`, `Invoice`, `VatSetting` | 2 |

---

## 1. Login ja autentimine
**Branch:** `task-01` ✅ Valmis

| Kiht | Sisaldab | Raskus |
|------|----------|--------|
| Backend | `app_user` repo, mapper, service, controller; lihtne autentimine (email + parool + staatus JPQL päringuga); `POST /api/login` | 2 |
| Frontend | `HomeView`, `LoginView`, `AuthService` (localStorage), `NavigationService` algseis | 2 |

---

## 2. Dashboard
**Branch:** `task-02` ✅ Valmis

| Kiht | Sisaldab | Raskus |
|------|----------|--------|
| Backend | `GET /api/dashboard` — edasimüüjate arv + viimane import | 1 |
| Frontend | `DashboardView` — statistika kaardid, navbar (`App.vue`) | 1 |

---

## 3. Edasimüüjad
**Branch:** `task-03` 🔄 Backend valmis, frontend pooleli

| Kiht | Sisaldab | Raskus |
|------|----------|--------|
| Backend | `seller` repo, mapper, service, controller; `GET /api/seller/user/{userId}`, `GET /api/seller/{sellerId}`, `POST /api/seller/user/{userId}`, `PUT /api/seller/{sellerId}`, `PUT /api/seller/{sellerId}/status` | 3 |
| Frontend | `SellersView` (nimekiri + otsing), `SellerView` (detailvaade, read-only), `SellerFormView` (lisamine + muutmine `?sellerId` parameetriga) | 3 |

---

## 4. Edasimüüja kontaktid
**Branch:** `task-04`
> Task-05 ja task-06 saavad alata alles pärast selle merge-i — loob `SellerSettingsView.vue` mida mõlemad vajavad.

Esimene samm SellerSettingsView ehitamisel — loob lehe raamistiku ja kontaktide halduse.

| Kiht | Sisaldab | Raskus |
|------|----------|--------|
| Backend | `POST /api/seller/{sellerId}/contacts`, `DELETE /api/seller/{sellerId}/contacts/{contactId}` | 2 |
| Frontend | `SellerSettingsView` (lehe raamistik + kontaktide sektsioon), `SellerSettingsContactModal` | 3 |

---

## 5. Edasimüüja piirkonnad
**Branch:** `task-05`
> Vajab task-04 merge-i. Saab korraga käia task-06-ga — väike merge konflikt `SellerSettingsView.vue`-s (erinevad read, lahendatav).

Lisab SellerSettingsView-sse piirkondade halduse. Sisaldab ka `GET /api/region` — vajalik regionide dropdown-i jaoks modalis.

| Kiht | Sisaldab | Raskus |
|------|----------|--------|
| Backend | `GET /api/region` (seed-andmed), `POST /api/seller/{sellerId}/regions`, `PUT /api/seller/{sellerId}/regions/{regionId}`, `DELETE /api/seller/{sellerId}/regions/{regionId}` | 2 |
| Frontend | `SellerSettingsRegionModal` | 2 |

---

## 6. Edasimüüja teenustasud
**Branch:** `task-06`
> Vajab task-04 merge-i. Saab korraga käia task-05-ga. Task-11 saab alata alles pärast selle merge-i (vajab `GET /api/product-type`).

Lisab SellerSettingsView-sse teenustasude halduse. Sisaldab `GET /api/product-type` — vajalik tootegruppide dropdown-i jaoks modalis. Täielik tootegruppide haldus (POST/DELETE) tuleb task-11-s.

| Kiht | Sisaldab | Raskus |
|------|----------|--------|
| Backend | `GET /api/product-type` (seed-andmed), `POST /api/seller/{sellerId}/commission-rates`, `PUT /api/seller/{sellerId}/commission-rates/{commissionRateId}`, `DELETE /api/seller/{sellerId}/commission-rates/{commissionRateId}` | 2 |
| Frontend | `SellerSettingsProductModal` | 2 |

---

## 7. Excel import
**Branch:** `task-07`
> Vajab task-06 merge-i. Task-08 saab alata alles pärast selle merge-i.

| Kiht | Sisaldab | Raskus |
|------|----------|--------|
| Backend | Apache POI; `POST /api/import/user/{userId}` — faili lugemine, edasimüüja tuvastus `org_id` järgi, `sales_report` ridade salvestamine; `DELETE /api/import/user/{userId}/{period}` | 5 |
| Frontend | `ReportsView` — periood + faili üleslaadimise vorm | 2 |

---

## 8. Teenustasu arvutus ja aruanded
**Branch:** `task-08`
> Vajab task-07 merge-i. Task-09 saab alata alles pärast selle merge-i.

| Kiht | Sisaldab | Raskus |
|------|----------|--------|
| Backend | `commission_calculation` loogika — tasu koguselt + summalt, KM lisamine `vat_setting` järgi, tulemuste salvestamine; `GET /api/report/user/{userId}`, `GET /api/report/{sellerId}/{period}` | 5 |
| Frontend | `ReportsView` — aruande koondtabel + laiendatav detailirida | 3 |

---

## 9. Aruannete eksport
**Branch:** `task-09`

| Kiht | Sisaldab | Raskus |
|------|----------|--------|
| Backend | `GET /api/report/{sellerId}/{period}/export` — `.xlsx` faili genereerimine Apache POIga | 2 |
| Frontend | `ReportsView` — "Ekspordi" nupp, faili allalaadimine | 1 |

---

## 10. Arvete kontroll
**Branch:** `task-10`
> Sõltumatu — ei sõltu ühelegi teisele taskile. Saab alustada igal hetkel paralleelselt.

| Kiht | Sisaldab | Raskus |
|------|----------|--------|
| Backend | `invoice` repo, mapper, service, controller; `GET /api/invoice/user/{userId}`, `POST /api/invoice/user/{userId}`, `DELETE /api/invoice/number/{invoiceNumber}` | 2 |
| Frontend | `InvoiceControlView` — arvete nimekiri + arve sisestamise vorm | 2 |

---

## 11. Seadistused
**Branch:** `task-11`
> Vajab task-06 merge-i (`GET /api/product-type` peab olemas olema tootegruppide dropdown-i jaoks).

| Kiht | Sisaldab | Raskus |
|------|----------|--------|
| Backend | `product_type` POST + DELETE (täiendab task-06 GET-i); `GET /api/settings/vat`, `PUT /api/settings/vat/user/{userId}`; `GET /api/user`, `POST /api/user`, `PUT /api/user/{userId}`, `PUT /api/user/{userId}/status` | 3 |
| Frontend | `SettingsView` — tootegruppide haldus, KM määra muutmine, kasutajate tabel + lisamine; `SettingsUserModal` | 3 |
