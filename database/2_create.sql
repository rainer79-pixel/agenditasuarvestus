SET search_path TO etas;

CREATE TABLE app_user (
    id          SERIAL        PRIMARY KEY,
    first_name  VARCHAR(100)  NOT NULL,
    middle_name VARCHAR(100),
    last_name   VARCHAR(100)  NOT NULL,
    email       VARCHAR(255)  NOT NULL UNIQUE,
    password    VARCHAR(255)  NOT NULL,
    user_status VARCHAR(20)   NOT NULL,
    user_role   CHAR(1)       NOT NULL
);

CREATE TABLE vat_setting (
    id              SERIAL        PRIMARY KEY,
    vat_rate        NUMERIC(5,2)  NOT NULL,
    updated_at      TIMESTAMP     NOT NULL DEFAULT now(),
    updated_by      INTEGER       NOT NULL REFERENCES app_user(id),
    valid_from_date DATE          NOT NULL,
    valid_to_date   DATE,
    status          VARCHAR(20)   NOT NULL
);

CREATE TABLE region (
    id              SERIAL       PRIMARY KEY,
    region_name     VARCHAR(20)  NOT NULL,
    sequence_number INTEGER      NOT NULL
);

CREATE TABLE role (
    id               SERIAL        PRIMARY KEY,
    code             VARCHAR(5)    NOT NULL UNIQUE,
    seller_role_name VARCHAR(100)  NOT NULL
);

CREATE TABLE product_type (
    id                SERIAL        PRIMARY KEY,
    product_type_name VARCHAR(100)  NOT NULL
);

CREATE TABLE seller (
    id              SERIAL        PRIMARY KEY,
    company_name    VARCHAR(255)  NOT NULL,
    org_id          INTEGER       NOT NULL UNIQUE,
    contract_start  DATE,
    contract_end    DATE,
    notes           VARCHAR(2000),
    status          VARCHAR(10)   NOT NULL,
    created_at      TIMESTAMP     NOT NULL DEFAULT now(),
    created_by      INTEGER       NOT NULL REFERENCES app_user(id)
);

CREATE TABLE seller_contact (
    id          SERIAL        PRIMARY KEY,
    seller_id   INTEGER       NOT NULL REFERENCES seller(id),
    first_name  VARCHAR(100)  NOT NULL,
    middle_name VARCHAR(100),
    last_name   VARCHAR(100)  NOT NULL,
    phone       VARCHAR(50),
    email       VARCHAR(255)  NOT NULL
);

CREATE TABLE seller_role (
    id                SERIAL   PRIMARY KEY,
    seller_contact_id INTEGER  NOT NULL REFERENCES seller_contact(id),
    seller_role_id    INTEGER  NOT NULL REFERENCES role(id)
);

CREATE TABLE seller_region (
    id                SERIAL   PRIMARY KEY,
    seller_id         INTEGER  NOT NULL REFERENCES seller(id),
    region_id         INTEGER  NOT NULL REFERENCES region(id),
    sales_point_count INTEGER  NOT NULL DEFAULT 0
);

CREATE TABLE commission_rate (
    id                  SERIAL        PRIMARY KEY,
    seller_id           INTEGER       NOT NULL REFERENCES seller(id),
    product_type_id     INTEGER       NOT NULL REFERENCES product_type(id),
    fee_per_transaction NUMERIC(10,4),
    fee_percent         NUMERIC(5,2),
    includes_vat        BOOLEAN       NOT NULL DEFAULT true,
    valid_from          DATE          NOT NULL,
    valid_to            DATE
);

CREATE TABLE sales_report (
    id         SERIAL       PRIMARY KEY,
    created_by INTEGER      NOT NULL REFERENCES app_user(id),
    period     VARCHAR(10)  NOT NULL,
    created_at DATE         NOT NULL DEFAULT CURRENT_DATE
);

CREATE TABLE sales_report_detail (
    id                SERIAL        PRIMARY KEY,
    sales_report_id   INTEGER       NOT NULL REFERENCES sales_report(id),
    seller_id         INTEGER       NOT NULL REFERENCES seller(id),
    department_name   VARCHAR(255)  NOT NULL,
    department_id     VARCHAR(50),
    payment_channel   VARCHAR(100),
    product_type      VARCHAR(100)  NOT NULL,
    transaction_count INTEGER       NOT NULL,
    sales_amount      NUMERIC(12,2),
    fee               NUMERIC(12,2),
    period            VARCHAR(20)   NOT NULL,
    region            VARCHAR(100),
    created_at        TIMESTAMP     NOT NULL DEFAULT now()
);

CREATE TABLE commission_calculation (
    id                  SERIAL        PRIMARY KEY,
    sales_report_id     INTEGER       NOT NULL REFERENCES sales_report(id),
    commission_rate_id  INTEGER       NOT NULL REFERENCES commission_rate(id),
    calculated_fee      NUMERIC(12,2) NOT NULL,
    vat_amount          NUMERIC(12,2) NOT NULL,
    calculated_fee_plus_vat           NUMERIC(12,2) NOT NULL,
    calculation_date    DATE          NOT NULL DEFAULT CURRENT_DATE
);

CREATE TABLE invoice (
    id             SERIAL        PRIMARY KEY,
    seller_id      INTEGER       NOT NULL REFERENCES seller(id),
    period         VARCHAR(20)   NOT NULL,
    number         VARCHAR(100),
    amount         NUMERIC(12,2) NOT NULL,
    issued_on      DATE,
    calculated_fee NUMERIC(12,2) NOT NULL,
    notes          VARCHAR(2000),
    UNIQUE (seller_id, period)
);

-- ----------------------------------------------------------------
-- commission_calculation_view
-- ----------------------------------------------------------------
-- Eesmärk: koondab sales_report_detail read (seller + product_type kaupa),
-- leiab kehtivad tariifid ja KM määra ning arvutab teenustasud.
-- Spring Boot kasutab seda vaateid commission_calculation tabelisse
-- kandeid tegemisel (järgmises etapis programmiliselt).
-- ----------------------------------------------------------------
CREATE OR REPLACE VIEW commission_calculation_view AS

WITH grouped AS (
    SELECT
        srd.sales_report_id,
        srd.seller_id,
        pt.id                           AS product_type_id,
        s.company_name,
        pt.product_type_name,
        SUM(srd.transaction_count)      AS transaction_count_sum,
        SUM(srd.sales_amount)           AS sales_amount_sum,
        SUM(srd.fee)                    AS fee_sum,
        MAKE_DATE(
            SPLIT_PART(sr.period, '-', 1)::int,
            SPLIT_PART(sr.period, '-', 2)::int,
            1
        )                               AS period_date
    FROM sales_report_detail srd
    JOIN  sales_report   sr ON sr.id                  = srd.sales_report_id
    JOIN  seller          s ON s.id                   = srd.seller_id
    LEFT JOIN product_type pt ON pt.product_type_name = srd.product_type
    GROUP BY
        srd.sales_report_id,
        srd.seller_id,
        pt.id,
        s.company_name,
        pt.product_type_name,
        sr.period
),

with_rates AS (
    SELECT
        g.sales_report_id,
        g.seller_id,
        g.product_type_id,
        g.company_name,
        g.product_type_name,
        g.transaction_count_sum,
        g.sales_amount_sum,
        g.fee_sum,
        cr.id                           AS commission_rate_id,
        cr.fee_per_transaction,
        cr.fee_percent,
        cr.includes_vat,
        vs.vat_rate,
          COALESCE(g.transaction_count_sum, 0) * COALESCE(cr.fee_per_transaction, 0)
        + COALESCE(g.sales_amount_sum,      0) * COALESCE(cr.fee_percent,         0) / 100
            AS raw_fee
    FROM grouped g
    LEFT JOIN commission_rate cr
           ON  cr.seller_id       = g.seller_id
           AND cr.product_type_id = g.product_type_id
           AND cr.valid_from     <= g.period_date
           AND (cr.valid_to   IS NULL OR cr.valid_to   >= g.period_date)
    LEFT JOIN vat_setting vs
           ON  vs.valid_from_date <= g.period_date
           AND (vs.valid_to_date IS NULL OR vs.valid_to_date >= g.period_date)
           AND vs.status = 'A'
),

with_calculated AS (
    SELECT
        *,
        ROUND(
            CASE
                WHEN includes_vat = true THEN raw_fee / (1 + vat_rate / 100)
                ELSE raw_fee
            END,
            2
        ) AS calculated_fee
    FROM with_rates
)

SELECT
    ROW_NUMBER() OVER ()                                                            AS id,
    sales_report_id,
    seller_id,
    product_type_id,
    company_name,
    product_type_name,
    transaction_count_sum,
    sales_amount_sum,
    fee_sum,
    commission_rate_id,
    fee_per_transaction,
    fee_percent,
    includes_vat,
    calculated_fee,
    vat_rate,
    ROUND(calculated_fee * vat_rate / 100, 2)                                       AS vat_amount,
    ROUND(calculated_fee + ROUND(calculated_fee * vat_rate / 100, 2), 2)            AS calculated_fee_plus_vat
FROM with_calculated;