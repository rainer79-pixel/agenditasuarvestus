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
    id                SERIAL        PRIMARY KEY,
    seller_id         INTEGER       NOT NULL REFERENCES seller(id),
    department_name   VARCHAR(255)  NOT NULL,
    department_id     VARCHAR(50),
    payment_channel   VARCHAR(100),
    product_type      VARCHAR(100)  NOT NULL,
    transaction_count INTEGER       NOT NULL,
    sales_amount      NUMERIC(12,2),
    fee_sum           NUMERIC(12,2),
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
    total_fee           NUMERIC(12,2) NOT NULL,
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