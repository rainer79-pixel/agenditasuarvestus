# SQL VIEW — andmebaasi vaade algajatele

## Mis see on ja miks oluline?

**SQL VIEW** on nagu salvestatud küsimus — kirjutad keerulise `SELECT` päringu üks kord, annad sellele nime, ja edaspidi kasutad seda nagu tavalist tabelit.

**Päriselust analoogia:** Kujuta ette, et sul on kaks tabelit — üks töötajatega ja teine palkadega. Iga kuu pead kirjutama sama pika päringu: "lisa kaks tabelit kokku, arvuta maksud, grupeeri osakonniti..." VIEW on nagu see, et keegi kirjutas selle päringu kord ühte "retseptikaarti" — edaspidi ütled lihtsalt "anna mulle kuupalgad" ja saad tulemuse kohe kätte.

```
TAVALINE TABEL                    VIEW (vaade)
┌─────────────┐                   ┌─────────────────────┐
│ Pärisandmed │                   │ Salvestatud päring  │
│ (kirjutatav)│                   │ (ainult lugemiseks) │
└─────────────┘                   └─────────────────────┘
     ↑                                      ↑
     │                              Kasutab päris tabeleid
  INSERT/UPDATE/DELETE              aga ise ei salvesta midagi
  töötab                            — arvutab iga päring uuesti
```

---

## VIEW vs päris tabel — erinevus

| | Päris tabel | VIEW |
|--|-------------|------|
| Andmed salvestatud? | ✅ Jah, kettale | ❌ Ei — arvutatakse hetkel |
| Kirjutamine (INSERT/UPDATE) | ✅ Saab | ❌ Ei saa (enamasti) |
| Lugemine (SELECT) | ✅ Saab | ✅ Saab — täpselt nagu tabel |
| Millal kasutada? | Põhiandmed | Keerulised arvutused / kokkuvõtted |

---

## Miks ETAS-is VIEW-d kasutame?

`commission_calculation_view` arvutab teenustasud. Selleks peab:
1. Võtma müügiaruande read (`sales_report_detail`)
2. Grupeerima need edasimüüja + tootegrupi kaupa
3. Leidma õige teenustasu määra (`commission_rate`)
4. Leidma kehtiva KM määra (`vat_setting`)
5. Arvutama lõpliku summa

See on **väga keeruline SQL** — 60+ rida. VIEW-na kirjutatakse see üks kord, edaspidi küsib Spring Boot lihtsalt:

```sql
SELECT * FROM commission_calculation_view WHERE sales_report_id = 1
```

---

## SQL VIEW struktuur — samm-sammult

ETAS-i VIEW kasutab **CTE-sid** (`WITH` plokid) — see on nagu vahesammude nimekiri:

```sql
-- Analoogia: retsept kolme sammuga
-- Samm 1: valmista kaste
-- Samm 2: lisa koostisosad
-- Samm 3: pane ahjju

CREATE OR REPLACE VIEW commission_calculation_view AS

WITH grouped AS (
--  ↑ "grouped" on vahesammu nimi — kasutame all uuesti

    SELECT
        srd.sales_report_id,
        srd.seller_id,
        pt.id AS product_type_id,
        SUM(srd.transaction_count) AS transaction_count_sum,  -- liidame kõik read kokku
        SUM(srd.sales_amount)      AS sales_amount_sum
        -- ...
    FROM sales_report_detail srd
    JOIN sales_report sr ON sr.id = srd.sales_report_id
    JOIN seller s        ON s.id  = srd.seller_id
    LEFT JOIN product_type pt ON pt.product_type_name = srd.product_type
    GROUP BY srd.sales_report_id, srd.seller_id, pt.id, ...
    --       ↑ grupeerime — üks rida iga seller+product_type kombinatsiooni kohta
),

with_rates AS (
--  ↑ teine vahesamm — lisame tariifid

    SELECT
        g.*,                        -- kõik eelmise sammu veerud
        cr.fee_per_transaction,
        cr.fee_percent,
        cr.includes_vat,
        vs.vat_rate,
        -- raw_fee = tehingute arv × tasu + summa × protsent
          COALESCE(g.transaction_count_sum, 0) * COALESCE(cr.fee_per_transaction, 0)
        + COALESCE(g.sales_amount_sum, 0)      * COALESCE(cr.fee_percent, 0) / 100
            AS raw_fee
    FROM grouped g
    LEFT JOIN commission_rate cr ON cr.seller_id = g.seller_id
                                AND cr.product_type_id = g.product_type_id
                                AND cr.valid_from <= g.period_date
                                AND (cr.valid_to IS NULL OR cr.valid_to >= g.period_date)
    LEFT JOIN vat_setting vs ON vs.valid_from_date <= g.period_date
                             AND (vs.valid_to_date IS NULL OR vs.valid_to_date >= g.period_date)
)

SELECT
    ROW_NUMBER() OVER () AS id,    -- ← vajalik Spring Bootile (vt allpool)
    sales_report_id,
    seller_id,
    -- ... kõik väljad ...
    CASE
        WHEN includes_vat = true THEN raw_fee / (1 + vat_rate / 100)
        ELSE raw_fee
    END AS calculated_fee
    -- ...
FROM with_rates;
```

---

## Miks `ROW_NUMBER() OVER () AS id`?

VIEW-l pole päris `id` veergu — andmed arvutatakse iga kord uuesti, andmebaas ei salvesta ridu.

Aga Spring Boot nõuab `@Entity` puhul alati **unikaalset ID-d**.

`ROW_NUMBER() OVER ()` annab igale reale järjekorranumbri (1, 2, 3, ...) — see toimib ID-na.

```
Tulemus:
id | sales_report_id | seller_id | product_type_id | calculated_fee
1  |        1        |     1     |        2        |     10.48
2  |        1        |     1     |        4        |     2.50
3  |        1        |     2     |        3        |     1696.77
```

---

## Kuidas Spring Boot VIEW-d loeb — `@Immutable`

Tavaline entity:

```java
@Entity
@Table(name = "seller", schema = "etas")
public class Seller { ... }
// Spring Boot saab: SELECT, INSERT, UPDATE, DELETE
```

VIEW entity:

```java
@Getter
@Entity
@Immutable                                          // ← see on võtmeannotatsioon
@Table(name = "commission_calculation_view", schema = "etas")
public class CommissionCalculationView {

    @Id
    @Column(name = "id")
    private Long id;                                // ← ROW_NUMBER() tulemus

    @Column(name = "sales_report_id")
    private Integer salesReportId;

    @Column(name = "calculated_fee")
    private BigDecimal calculatedFee;

    // ... ülejäänud väljad ...
}
```

**`@Immutable` tähendab:** Hibernate (JPA) ei ürita seda kirjutada — ainult loeb.

```
@Immutable puudub:  Hibernate kontrollib muutusi → üritab UPDATE → viga
@Immutable olemas:  Hibernate loeb ainult → töötab
```

---

## Kuidas COALESCE töötab?

`COALESCE(väärtus, 0)` tähendab: **"kui väärtus on NULL, kasuta 0"**

```sql
COALESCE(cr.fee_per_transaction, 0)
-- cr.fee_per_transaction = NULL  →  tulemus: 0
-- cr.fee_per_transaction = 1.50  →  tulemus: 1.50
```

Miks vaja? Teenustasul võib olla ainult `fee_per_transaction` (koguselt) VÕI ainult `fee_percent` (summalt) — teine on `NULL`. NULL × arv = NULL, aga meie tahame 0.

---

## KM arvutuse loogika

```
includes_vat = false  →  calculated_fee = raw_fee
                          (tariif ilma KM-ta, lisame KM juurde)

includes_vat = true   →  calculated_fee = raw_fee / (1 + vat_rate / 100)
                          (tariif sisaldab KM-i, võtame KM välja)
```

**Näide includes_vat = true, KM = 24%:**

```
raw_fee = 13.00 EUR   (sisaldab juba KM-i)

calculated_fee      = 13.00 / 1.24 = 10.48 EUR  (neto)
vat_amount          = 10.48 × 0.24 = 2.52 EUR
calculated_fee_plus_vat = 10.48 + 2.52 = 13.00 EUR  ✓ (= raw_fee)
```

---

## Kokkuvõte

| Mõiste | Selgitus |
|--------|---------|
| `VIEW` | Salvestatud SQL päring — loetav nagu tabel, ise ei salvesta andmeid |
| `WITH grouped AS (...)` | CTE — vahesamm, millele annad nime ja kasutad all uuesti |
| `GROUP BY` | Grupeerib read kokku — summeerib samad seller+tootegrupp read |
| `ROW_NUMBER() OVER ()` | Annab igale reale järjekorranumbri — Spring Booti ID jaoks |
| `@Immutable` | Spring Boot annotatioon — ainult lugemine, mitte kirjutamine |
| `COALESCE(x, 0)` | Kui x on NULL, kasuta 0 — kaitseb NULL-arvutuste eest |
| `CASE WHEN ... END` | SQL-i if-else — erinev valem sõltuvalt includes_vat väärtusest |

---

## Järgmised sammud

- Excel impordi voog — kuidas `sales_report_detail` read tekivad (`excel-import.md`)
- `commission_calculation` tabelisse kirjutamine — Spring Boot kasutab seda VIEW-d kandeid tehes (`ReportControllerService.createCommissionCalculations()`)
