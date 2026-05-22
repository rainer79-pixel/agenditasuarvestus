SET search_path TO etas;

-- ----------------------------------------------------------------
-- Põhiandmed (seed — alati vajalikud)
-- ----------------------------------------------------------------

-- role: kontaktide rollid (fikseeritud)
INSERT INTO role (code, seller_role_name) VALUES
    ('L', 'Lepinguline kontakt'),
    ('A', 'Aruannete kontakt'),
    ('R', 'Raamatupidamise kontakt'),
    ('T', 'Tehniline kontakt');

-- region: Eesti maakonnad ja suuremad linnad (järjestatud sequence_number järgi)
INSERT INTO region (region_name, sequence_number) VALUES
    ('Tallinn',        1),
    ('Harjumaa',       2),
    ('Tartumaa',       3),
    ('Tartu',          4),
    ('Pärnumaa',       5),
    ('Pärnu',          6),
    ('Ida-Virumaa',    7),
    ('Narva',          8),
    ('Lääne-Virumaa',  9),
    ('Saaremaa',      10),
    ('Viljandimaa',   11),
    ('Läänemaa',      12),
    ('Raplamaa',      13),
    ('Järvamaa',      14),
    ('Jõgevamaa',     15),
    ('Põlvamaa',      16),
    ('Valgamaa',      17),
    ('Võrumaa',       18),
    ('Hiiumaa',       19);

-- product_type: tootegrupid (fikseeritud, vastab Exceli 'tyyp' väärtustele)
INSERT INTO product_type (product_type_name) VALUES
    ('isikustamine'),
    ('kaardimyyk'),
    ('pilet'),
    ('rahalaadimine'),
    ('sooduskaardi isikustamine'),
    ('kaardi tagasiost'),
    ('raha valjamakse');

-- ----------------------------------------------------------------
-- Testandmed (CRUD valideerimiseks)
-- ----------------------------------------------------------------

-- app_user — parool: '123' (lihttekst — BCrypt puudub selles projektis)
-- user_role: 'A'=Admin, 'U'=User | user_status: 'A'=aktiivne, 'D'=deaktiveeritud
INSERT INTO app_user (first_name, middle_name, last_name, email, password, user_status, user_role) VALUES
    ('Mari', NULL, 'Maasikas', 'mari@agent.ee', '123', 'A', 'A'),
    ('Jaan', 'Arvo', 'Tamm',   'jt@agent.ee',   '123', 'A', 'U');

-- vat_setting: KM määr (updated_by=1 on Mari Maasikas)
INSERT INTO vat_setting (vat_rate, updated_at, updated_by, valid_from_date, valid_to_date, status)
VALUES (24, now(), 1, '2024-01-01', NULL, 'A');

-- seller
-- org_id 10406134 vastab näidis-Exceli seller_org_id-le
-- created_by=1 (Mari Maasikas)
-- status: 'A'=aktiivne, 'D'=deaktiveeritud
-- INSERT INTO seller (company_name, org_id, contract_start, contract_end, notes, status, created_by) VALUES
--     ('ETAS AS',   10406134, '2024-01-01', NULL,         'Näidisedasimüüja — vastab Exceli näidisfailile', 'A', 1),
--     ('Ühistu OÜ', 20506789, '2022-06-01', '2025-12-31', NULL,                                            'A', 1);

INSERT INTO etas.seller (id, company_name, org_id, contract_start, contract_end, notes, status, created_at, created_by) VALUES (default, 'Agent A OÜ ', 1588, '2024-01-01', null, 'Näidisedasimüüja — vastab Exceli näidisfailile', 'A', '2026-05-22 06:47:15.861101', 1);
INSERT INTO etas.seller (id, company_name, org_id, contract_start, contract_end, notes, status, created_at, created_by) VALUES (default, 'Arvelduskeskus OÜ', 1222, '2022-06-01', null, null, 'A', '2026-05-22 06:47:15.861101', 1);


-- seller_contact (seller_id: ETAS AS=1, Ühistu OÜ=2)
INSERT INTO seller_contact (seller_id, first_name, middle_name, last_name, phone, email) VALUES
    (1, 'Toomas', NULL,    'Rebane',  '55112233',       'toomas.rebane@etasas.ee'),
    (2, 'Mari',   'Liis', 'Kask',    '+372 5234 5678', 'mari.kask@agentb.ee');

-- seller_role (seller_contact_id: Toomas Rebane=1, Mari Liis Kask=2 | seller_role_id: L=1, A=2, R=3, T=4)
INSERT INTO seller_role (seller_contact_id, seller_role_id) VALUES
    (1, 1),  -- Toomas Rebane: Lepinguline kontakt
    (1, 2),  -- Toomas Rebane: Aruannete kontakt
    (2, 1),  -- Mari Liis Kask: Lepinguline kontakt
    (2, 4);  -- Mari Liis Kask: Tehniline kontakt

-- seller_region (region_id: Tallinn=1, Harjumaa=2, Tartumaa=3, Pärnumaa=5)
INSERT INTO seller_region (seller_id, region_id, sales_point_count) VALUES
    (1, 1, 10),  -- ETAS AS: Tallinn, 10 müügipunkti
    (1, 2,  5),  -- ETAS AS: Harjumaa, 5 müügipunkti
    (1, 5,  3),  -- ETAS AS: Pärnumaa, 3 müügipunkti
    (2, 1,  8),  -- Ühistu OÜ: Tallinn, 8 müügipunkti
    (2, 3,  3);  -- Ühistu OÜ: Tartumaa, 3 müügipunkti

-- commission_rate
-- product_type_id: isikustamine=1, kaardimyyk=2, pilet=3, rahalaadimine=4,
-- --                  sooduskaardi isikustamine=5, kaardi tagasiost=6, raha valjamakse=7
-- INSERT INTO commission_rate (seller_id, product_type_id, fee_per_transaction, fee_percent, includes_vat, valid_from, valid_to) VALUES
--     (1, 1, 0.0100, NULL, false, '2024-01-01', NULL),  -- ETAS AS: isikustamine
--     (1, 2, 0.0100, NULL, false, '2024-01-01', NULL),  -- ETAS AS: kaardimyyk
--     (1, 3, NULL,   1.00, false, '2024-01-01', NULL),  -- ETAS AS: pilet
--     (1, 4, NULL,   1.00, false, '2024-01-01', NULL),  -- ETAS AS: rahalaadimine
--     (1, 5, 0.0100, NULL, false, '2024-01-01', NULL),  -- ETAS AS: sooduskaardi isikustamine
--     (1, 6, 0.0100, NULL, false, '2024-01-01', NULL),  -- ETAS AS: kaardi tagasiost
--     (1, 7, 0.0100, NULL, false, '2024-01-01', NULL),  -- ETAS AS: raha valjamakse
--     (2, 2, NULL,   1.00, false, '2022-06-01', NULL),  -- Ühistu OÜ: kaardimyyk
--     (2, 4, 0.0500, NULL, false, '2022-06-01', NULL);  -- Ühistu OÜ: rahalaadimine
INSERT INTO etas.commission_rate (id, seller_id, product_type_id, fee_per_transaction, fee_percent, includes_vat, valid_from, valid_to) VALUES (default, 1, 2, 1.0000, null, true, '2026-02-01', null);
INSERT INTO etas.commission_rate (id, seller_id, product_type_id, fee_per_transaction, fee_percent, includes_vat, valid_from, valid_to) VALUES (default, 1, 4, null, 1.00, true, '2026-02-01', null);
INSERT INTO etas.commission_rate (id, seller_id, product_type_id, fee_per_transaction, fee_percent, includes_vat, valid_from, valid_to) VALUES (default, 2, 3, 1.0000, null, true, '2026-02-01', null);



-- sales_report
-- INSERT INTO sales_report (created_by, period, created_at)
-- VALUES (1, '2026-4', '2026-05-01');

-- sales_report_detail (periood '04.2026' — vastab näidis-Exceli andmetele)
-- product_type salvestatakse Exceli 'tyyp' väärtusena (lowercase string)
-- INSERT INTO sales_report_detail (seller_id, department_name, department_id, payment_channel, product_type, transaction_count, sales_amount, period, region) VALUES
--     (1, 'Pood 1', '2894', 'C', 'isikustamine',              100,    NULL, '04.2026', 'Tallinn'),
--     (1, 'Pood 2', '2543', 'C', 'kaardimyyk',                100,    NULL, '04.2026', 'Tartumaa'),
--     (1, 'Pood 1', '2894', 'E', 'pilet',                     300, 2500.00, '04.2026', 'Pärnumaa'),
--     (1, 'Pood 2', '2543', 'C', 'rahalaadimine',             100,  500.00, '04.2026', 'Harjumaa'),
--     (1, 'Pood 1', '2894', 'C', 'sooduskaardi isikustamine', 100,    NULL, '04.2026', 'Tallinn'),
--     (1, 'Pood 3', '2541', 'E', 'kaardi tagasiost',          100,    NULL, '04.2026', 'Harjumaa'),
--     (1, 'Pood 2', '2543', 'C', 'raha valjamakse',           100,    NULL, '04.2026', 'Tartumaa');

-- INSERT INTO etas.sales_report_detail (id, sales_report_id, seller_id, department_name, department_id, payment_channel, product_type, transaction_count, sales_amount, fee, period, region, created_at) VALUES (default, 1, 1, 'Agent A OÜ ', '1588', 'C', 'kaardimyyk', 13, 133.00, 0.00, '2026-4', 'Piirkond 11', '2026-05-22 06:51:09.435965');
-- INSERT INTO etas.sales_report_detail (id, sales_report_id, seller_id, department_name, department_id, payment_channel, product_type, transaction_count, sales_amount, fee, period, region, created_at) VALUES (default, 1, 1, 'Agent A OÜ ', '1588', 'C', 'rahalaadimine', 31, 310.00, 0.00, '2026-4', 'Piirkond 11', '2026-05-22 06:51:09.435965');
-- INSERT INTO etas.sales_report_detail (id, sales_report_id, seller_id, department_name, department_id, payment_channel, product_type, transaction_count, sales_amount, fee, period, region, created_at) VALUES (default, 1, 2, 'Arvelduskeskus OÜ', '1222', 'PP', 'pilet', 1, 13.20, 0.00, '2026-4', 'Piirkond 14', '2026-05-22 06:51:09.435965');
-- INSERT INTO etas.sales_report_detail (id, sales_report_id, seller_id, department_name, department_id, payment_channel, product_type, transaction_count, sales_amount, fee, period, region, created_at) VALUES (default, 1, 2, 'Arvelduskeskus OÜ', '1222', 'PPE', 'pilet', 2102, 232001.62, 0.00, '2026-4', 'Piirkond 14', '2026-05-22 06:51:09.435965');
--

-- commission_calculation (KM 22%, includes_vat=false → KM lisatakse peale)
-- isikustamine:              100 × 0.0100  =  1.00 | vat= 0.22 | total=  1.22
-- kaardimyyk:                100 × 0.0100  =  1.00 | vat= 0.22 | total=  1.22
-- pilet:                    2500 × 1.00%   = 25.00 | vat= 5.50 | total= 30.50
-- rahalaadimine:             500 × 1.00%   =  5.00 | vat= 1.10 | total=  6.10
-- sooduskaardi isikustamine: 100 × 0.0100  =  1.00 | vat= 0.22 | total=  1.22
-- kaardi tagasiost:          100 × 0.0100  =  1.00 | vat= 0.22 | total=  1.22
-- raha valjamakse:           100 × 0.0100  =  1.00 | vat= 0.22 | total=  1.22
-- commission_rate_id IDs järjekorras: isikust=1, kaardimyyk=2, pilet=3, rahalaadimine=4, soodus=5, tagasiost=6, raha=7
-- INSERT INTO commission_calculation (sales_report_id, commission_rate_id, calculated_fee, vat_amount, total_fee, calculation_date) VALUES
--     (1, 1,  1.00, 0.22,  1.22, '2026-05-01'),
--     (2, 2,  1.00, 0.22,  1.22, '2026-05-01'),
--     (3, 3, 25.00, 5.50, 30.50, '2026-05-01'),
--     (4, 4,  5.00, 1.10,  6.10, '2026-05-01'),
--     (5, 5,  1.00, 0.22,  1.22, '2026-05-01'),
--     (6, 6,  1.00, 0.22,  1.22, '2026-05-01'),
--     (7, 7,  1.00, 0.22,  1.22, '2026-05-01');
--
-- invoice (ETAS AS, periood 04.2026)
-- Arvutatud kogusumma total_fee: 1.22+1.22+30.50+6.10+1.22+1.22+1.22 = 42.70
INSERT INTO invoice (seller_id, period, number, amount, issued_on, calculated_fee, notes) VALUES
    (1, '04.2026', 'ATAS-2026-004', 42.70, '2026-05-05', 42.70, 'Arve vastab arvutusele');