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
**Branch:** `task-01`

| Kiht | Sisaldab | Raskus |
|------|----------|--------|
| Backend | `app_user` repo, mapper, service, controller; lihtne autentimine (email + parool + staatus JPQL päringuga); `POST /api/login` | 2 |
| Frontend | `HomeView`, `LoginView`, `AuthService` (localStorage), `NavigationService` algseis | 2 |

---

## 2. Dashboard
**Branch:** `task-02`

| Kiht | Sisaldab | Raskus |
|------|----------|--------|
| Backend | `GET /api/dashboard` — edasimüüjate arv + viimane import | 1 |
| Frontend | `DashboardView` — statistika kaardid | 1 |

---

## 3. Edasimüüjad
**Branch:** `task-03`

| Kiht | Sisaldab | Raskus |
|------|----------|--------|
| Backend | `seller` entity, repo, mapper, service, controller; `GET /api/seller/user/{userId}`, `GET /api/seller/{sellerId}`, `POST /api/seller/user/{userId}`, `PUT /api/seller/{sellerId}`, `PUT /api/seller/{sellerId}/status` | 3 |
| Frontend | `SellersView` (nimekiri + otsing), `SellerView` (detailvaade, read-only), `SellerFormView` (lisamine + muutmine `?sellerId` parameetriga) | 3 |

---

## 4. Tootegrupid
**Branch:** `task-04`

| Kiht | Sisaldab | Raskus |
|------|----------|--------|
| Backend | `product_type` entity, repo, mapper, service, controller; `GET /api/product-type`, `POST /api/product-type`, `DELETE /api/product-type/{productTypeId}` | 1 |
| Frontend | *(tuleb koos Seadistustega — vt task-10-settings)* | — |

---

## 5. Edasimüüja seaded
**Branch:** `task-05`

| Kiht | Sisaldab | Raskus |
|------|----------|--------|
| Backend | `seller_contact` + `seller_role` CRUD; `region` seed + `seller_region` CRUD; `commission_rate` CRUD; kõik GET/POST/PUT/DELETE endpointid | 4 |
| Frontend | `SellerSettingsView` + 3 modali: `SellerSettingsContactModal`, `SellerSettingsRegionModal`, `SellerSettingsProductModal` | 4 |

---

## 6. Excel import
**Branch:** `task-06`

| Kiht | Sisaldab | Raskus |
|------|----------|--------|
| Backend | Apache POI; `POST /api/import/user/{userId}` — faili lugemine, edasimüüja tuvastus `org_id` järgi, `sales_report` ridade salvestamine; `DELETE /api/import/user/{userId}/{period}` | 5 |
| Frontend | `ReportsView` — periood + faili üleslaadimise vorm | 2 |

---

## 7. Teenustasu arvutus
**Branch:** `task-07`

| Kiht | Sisaldab | Raskus |
|------|----------|--------|
| Backend | `commission_calculation` loogika — tasu koguselt + summalt, KM lisamine `vat_setting` järgi, tulemuste salvestamine; `GET /api/report/user/{userId}`, `GET /api/report/{sellerId}/{period}` | 5 |
| Frontend | `ReportsView` — aruande koondtabel + laiendatav detailirida | 3 |

---

## 8. Aruannete eksport
**Branch:** `task-08`

| Kiht | Sisaldab | Raskus |
|------|----------|--------|
| Backend | `GET /api/report/{sellerId}/{period}/export` — `.xlsx` faili genereerimine Apache POIga | 3 |
| Frontend | `ReportsView` — "Ekspordi" nupp, faili allalaadimine | 1 |

---

## 9. Arvete kontroll
**Branch:** `task-09`

| Kiht | Sisaldab | Raskus |
|------|----------|--------|
| Backend | `invoice` entity, repo, mapper, service, controller; `GET /api/invoice/user/{userId}`, `POST /api/invoice/user/{userId}`, `DELETE /api/invoice/number/{invoiceNumber}` | 2 |
| Frontend | `InvoiceControlView` — arvete nimekiri + arve sisestamise vorm | 2 |

---

## 10. Seadistused
**Branch:** `task-10`

| Kiht | Sisaldab | Raskus |
|------|----------|--------|
| Backend | `vat_setting` CRUD (`GET /api/settings/vat`, `PUT /api/settings/vat/user/{userId}`); kasutajate haldus (`GET /api/user`, `POST /api/user`, `PUT /api/user/{userId}`, `PUT /api/user/{userId}/status`) | 3 |
| Frontend | `SettingsView` — kasutajate tabel + lisamine, KM määra muutmine, tootegruppide haldus; `SettingsUserModal` — kasutaja muutmine | 3 |
