# `commission_calculation_view` — loogika selgitus

> Eesmärk: enne SQL kirjutamist veenduda, et loogikast on üheselt arusaadud.

---

## Mis see vaade teeb?

Vaade koondab ühe `sales_report` (impordisessiooni) kõik read, grupeerib need
**edasimüüja + tootegrupi** kaupa kokku ja **arvutab igale grupile teenustasu**.

Tulemus on üks rida iga *(sales_report, seller, product_type)* kombinatsiooni kohta —
valmis selleks, et Spring Boot saaks need kanded `commission_calculation` tabelisse kirjutada.

---

## Andmevoog samm-sammult

```
sales_report (id, period)
      │
      │  period = '2026-4'
      ▼
[ Periood → kuupäev ]
      │  MAKE_DATE(2026, 4, 1) = 2026-04-01
      │
      ├──────────────────────────────────────────────────────────────────────────┐
      │                                                                          │
      ▼                                                                          ▼
vat_setting                                                      sales_report_detail (read)
Leia rida, kus 2026-04-01 jääb                                         │
valid_from_date .. valid_to_date vahele                                 │
→ vat_rate = 24.00                                                      │
                                                             ┌──────────┴──────────┐
                                                             │                     │
                                                             ▼                     ▼
                                                        product_type           seller
                                                 (string → id leidmine)   (company_name)
                                                             │
                                                             ▼
                                                   Grupeeri kokku:
                                              (sales_report_id, seller_id, product_type_id)
                                              SUM(transaction_count) → transaction_count_sum
                                              SUM(sales_amount)      → sales_amount_sum
                                              SUM(fee)               → fee_sum
                                                             │
                                                             ▼
                                                      commission_rate
                                              (seller_id + product_type_id + periood)
                                              → fee_per_transaction, fee_percent, includes_vat
                                                             │
                                                             ▼
                                                    [ Arvutused ]
                                              calculated_fee, vat_amount,
                                              calculated_fee_plus_vat
```

---

## Tabelite seosed (mis millega joindub)

```
sales_report_detail
  ├── sales_report_id  →  sales_report.id          (periood)
  ├── seller_id        →  seller.id                (company_name)
  ├── product_type     →  product_type.product_type_name  (string match → id)
  └── (seller_id + product_type_id + periood) → commission_rate

commission_rate
  ├── fee_per_transaction
  ├── fee_percent
  └── includes_vat

vat_setting
  └── vat_rate  (perioodiga leitud õige rida)
```

---

## Perioodianalüüs — kuidas '2026-4' → kuupäev

`sales_report.period` on string kujul `'2026-4'` (aasta-kuu).

Võrdlemiseks teisendame selle kuu **esimeseks päevaks**:

```sql
MAKE_DATE(
    SPLIT_PART(sr.period, '-', 1)::int,   -- 2026
    SPLIT_PART(sr.period, '-', 2)::int,   -- 4
    1                                      -- 01 → 2026-04-01
) AS period_date
```

Seejärel kasutame seda kuupäeva nii `vat_setting` kui `commission_rate` filtreerimiseks:

```
valid_from_date <= 2026-04-01
AND (valid_to_date IS NULL OR valid_to_date >= 2026-04-01)
```

**Miks esimene päev?**
Kui periood on aprill 2026, siis kehtiv KM määr või komisjonitariif on see,
mis kehtis kuu alguses. See on kõige lihtsam ja üheselt mõistetav lähenemine.

---

## Grupeerimise loogika

`sales_report_detail` sisaldab ühe edasimüüja kohta mitu rida (erinevad osakondad,
piirkonnad, kanalid). Aga `commission_rate` on defineeritud edasimüüja + tootegrupi
tasemel — seega peame read enne arvutust kokku summeerima.

**Näide andmetest:**

| seller_id | product_type   | transaction_count | sales_amount |
|-----------|----------------|-------------------|--------------|
| 2         | pilet          | 1                 | 13.20        |
| 2         | pilet          | 2102              | 232001.62    |

→ pärast grupeerimist:

| seller_id | product_type_id | transaction_count_sum | sales_amount_sum |
|-----------|-----------------|-----------------------|------------------|
| 2         | 3 (pilet)       | 2103                  | 232014.82        |

---

## Arvutuste loogika

### Samm 1 — toortasu (raw fee)

```
raw_fee = (transaction_count_sum × fee_per_transaction)
        + (sales_amount_sum × fee_percent / 100)
```

> **Märkus:** `fee_percent` on salvestatud protsendina (nt `1.00` tähendab 1%).
> Seepärast jagame 100-ga enne korrutamist.
> Kui üks neist on NULL (nt ainult tehingupõhine tasu), siis NULL arvutuses = 0.
> SQL-s: `COALESCE(fee_per_transaction, 0)` ja `COALESCE(fee_percent, 0)`.

### Samm 2 — calculated_fee (sõltub includes_vat väärtusest)

```
includes_vat = true  →  calculated_fee = raw_fee / (1 + vat_rate / 100)
includes_vat = false →  calculated_fee = raw_fee
```

> **Selgitus `includes_vat = true` kohta:**
> Kui `includes_vat = true`, tähendab see, et edasimüüjale kokkulepitud tariif **sisaldab**
> juba käibemaksu. Seega on `raw_fee` summa, milles KM on sees.
> Et saada käibemaksuvaba summa (neto), jagame KM koefitsiendiga: `/ 1.24` (kui KM=24%).
>
> Valem üldiselt: `raw_fee / (1 + vat_rate/100)`

### Samm 3 — vat_amount

```
vat_amount = calculated_fee × (vat_rate / 100)
```

### Samm 4 — calculated_fee_plus_vat

```
calculated_fee_plus_vat = calculated_fee + vat_amount
```

> **Kontroll:** kui `includes_vat = true`, siis `calculated_fee_plus_vat ≈ raw_fee`
> (väike ümardamiserinevus võib esineda).

---

## Näide — arvutus läbi andmetega

### Agent A OÜ — kaardimyyk

| Väli                  | Väärtus                         |
|-----------------------|---------------------------------|
| transaction_count_sum | 13                              |
| sales_amount_sum      | 133.00                          |
| fee_per_transaction   | 1.0000                          |
| fee_percent           | NULL                            |
| includes_vat          | true                            |
| vat_rate              | 24.00                           |

```
raw_fee         = (13 × 1.0000) + (133.00 × 0/100) = 13.00
calculated_fee  = 13.00 / (1 + 24/100) = 13.00 / 1.24 = 10.48
vat_amount      = 10.48 × 0.24 = 2.52
calculated_fee_plus_vat = 10.48 + 2.52 = 13.00  ✓ (= raw_fee)
```

### Arvelduskeskus OÜ — pilet

| Väli                  | Väärtus                         |
|-----------------------|---------------------------------|
| transaction_count_sum | 2103                            |
| sales_amount_sum      | 232014.82                       |
| fee_per_transaction   | 1.0000                          |
| fee_percent           | NULL                            |
| includes_vat          | true                            |
| vat_rate              | 24.00                           |

```
raw_fee         = (2103 × 1.0000) + 0 = 2103.00
calculated_fee  = 2103.00 / 1.24 = 1696.77
vat_amount      = 1696.77 × 0.24 = 407.22
calculated_fee_plus_vat = 1696.77 + 407.22 = 2103.99  (≈ 2103.00, ümardus)
```

---

## Küsimus — kinnita enne SQL kirjutamist

**Punkt 1 — `includes_vat = true` valem:**

Sinu kirjutatud valem oli: `raw_fee / vat_rate` (kus vat_rate=24).

See annaks kaardimyük jaoks: `13.00 / 24 = 0.54 EUR` — mis ei tundu õige.

Mina mõistsin loogilisema valemina: `raw_fee / (1 + vat_rate / 100)` ehk `13.00 / 1.24 = 10.48 EUR`.

Palun kinnita, kumba valemit kasutada?

**Punkt 2 — `fee_percent` on protsendina (1.00 = 1%):**

Kasutan arvutuses `fee_percent / 100`. Kas see on õige?

---

## Vaate väljundi kokkuvõte

| Veerg                   | Allikas                                     |
|-------------------------|---------------------------------------------|
| `sales_report_id`       | `sales_report_detail.sales_report_id`       |
| `seller_id`             | `sales_report_detail.seller_id`             |
| `product_type_id`       | `product_type.id` (string match järgi)      |
| `company_name`          | `seller.company_name`                       |
| `product_type_name`     | `product_type.product_type_name`            |
| `transaction_count_sum` | `SUM(sales_report_detail.transaction_count)`|
| `sales_amount_sum`      | `SUM(sales_report_detail.sales_amount)`     |
| `fee_sum`               | `SUM(sales_report_detail.fee)`              |
| `fee_per_transaction`   | `commission_rate.fee_per_transaction`       |
| `fee_percent`           | `commission_rate.fee_percent`               |
| `includes_vat`          | `commission_rate.includes_vat`              |
| `calculated_fee`        | arvutus (vt ülal)                           |
| `vat_rate`              | `vat_setting.vat_rate`                      |
| `vat_amount`            | `calculated_fee × vat_rate / 100`           |
| `calculated_fee_plus_vat` | `calculated_fee + vat_amount`             |
