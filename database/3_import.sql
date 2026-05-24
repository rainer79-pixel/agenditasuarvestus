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
-- product_type_id: isikustamine=1, kaardimyyk=2, pilet=3, rahalaadimine=4,
--                  sooduskaardi isikustamine=5, kaardi tagasiost=6, raha valjamakse=7
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
    ('Jaan', 'Arvo', 'Tamm',  'jt@agent.ee',   '123', 'A', 'U');

-- vat_setting: KM määr (updated_by=1 on Mari Maasikas)
INSERT INTO vat_setting (vat_rate, updated_at, updated_by, valid_from_date, valid_to_date, status)
VALUES (24, now(), 1, '2024-01-01', NULL, 'A');

-- ----------------------------------------------------------------
-- seller — 10 näidisedasimüüjat
-- Arvopunkt OÜ (id=3) org_id=10406134 vastab demo Exceli seller_org_id veerul — teistel on näidisandmed
-- created_by=1 (Mari Maasikas)
-- ----------------------------------------------------------------
INSERT INTO seller (company_name, org_id, contract_start, contract_end, notes, status, created_at, created_by) VALUES
    ('Agent A OÜ',              1588, '2026-01-01', null, 'Eelistab suhtlust e-posti teel. Arved saata hiljemalt kuu 5. kuupäevaks.', 'A', now(), 1),  -- id=1
    ('Arvelduskeskus OÜ',       1222, '2026-01-01', null, null, 'A', now(), 1),  -- id=2
    ('Arvopunkt OÜ',        10406134, '2026-01-01', null, 'Leping lõpeb 31.12.2026. Uuendamisel kontrollida teenustasude määrasid.', 'A', now(), 1),  -- id=3
    ('Tasuvara OÜ',             9733, '2026-01-01', null, null, 'A', now(), 1),  -- id=4
    ('Piletivõrk OÜ',           8799, '2026-01-01', null, 'Kliendil kaks erinevat arvelduskontot — kontrollida enne makse saatmist.', 'A', now(), 1),  -- id=5
    ('Müügiruut OÜ',            3574, '2026-01-01', null, null, 'A', now(), 1),  -- id=6
    ('Kaardilahendus OÜ',       9681, '2026-01-01', null, null, 'A', now(), 1),  -- id=7
    ('Lõunakaardi Teenused OÜ', 2180, '2026-01-01', null, 'Uus kontaktisik alates 2026-03. Vana kontakt Jaan Tamm enam ei kehti.', 'A', now(), 1),  -- id=8
    ('Terminalipunkt OÜ',       7835, '2026-01-01', null, null, 'A', now(), 1),  -- id=9
    ('Sõidukaardi Keskus OÜ',   7474, '2026-01-01', null, null, 'A', now(), 1);  -- id=10

-- ----------------------------------------------------------------
-- seller_contact — üks kontakt iga edasimüüja kohta
-- seller_contact_id: 1–10 (vastab seller_id järjekorrale)
-- ----------------------------------------------------------------
INSERT INTO seller_contact (seller_id, first_name, middle_name, last_name, phone, email) VALUES
    (1,  'Toomas', null,   'Rebane', '55112233',       'toomas.rebane@agenta.ee'),        -- Agent A
    (2,  'Mari',   'Liis', 'Kask',   '+372 5234 5678', 'mari.kask@arvelduskeskus.ee'),    -- Arvelduskeskus
    (3,  'Kairi',  null,   'Kivi',   '56789012',       'kairi.kivi@arvopunkt.ee'),        -- Arvopunkt
    (4,  'Peeter', null,   'Pärn',   '53456789',       'peeter.parn@tasuvara.ee'),        -- Tasuvara
    (5,  'Siret',  null,   'Saar',   '51234567',       'siret.saar@piletivorke.ee'),      -- Piletivõrk
    (6,  'Andres', null,   'Allik',  '58901234',       'andres.allik@myygirut.ee'),       -- Müügiruut
    (7,  'Liisa',  null,   'Lumi',   '52345678',       'liisa.lumi@kaardilahendus.ee'),   -- Kaardilahendus
    (8,  'Maris',  null,   'Mets',   '57890123',       'maris.mets@lounakaart.ee'),       -- Lõunakaardi Teenused
    (9,  'Tanel',  null,   'Tamm',   '54567890',       'tanel.tamm@terminalipunkt.ee'),   -- Terminalipunkt
    (10, 'Külli',  null,   'Kärp',   '59012345',       'kulli.karp@soidukaart.ee');       -- Sõidukaardi Keskus

-- seller_role (kõigil Lepinguline kontakt = role_id 1)
INSERT INTO seller_role (seller_contact_id, seller_role_id) VALUES
    (1,  1),
    (2,  1),
    (3,  1),
    (4,  1),
    (5,  1),
    (6,  1),
    (7,  1),
    (8,  1),
    (9,  1),
    (10, 1);

-- ----------------------------------------------------------------
-- seller_region — piirkonnad ja müügipunktid
-- region_id: Tallinn=1, Harjumaa=2, Tartumaa=3, Pärnumaa=5, Ida-Virumaa=7, Põlvamaa=16
-- ----------------------------------------------------------------
INSERT INTO seller_region (seller_id, region_id, sales_point_count) VALUES
    (1,  1,  5),  -- Agent A: Tallinn
    (1,  2,  3),  -- Agent A: Harjumaa
    (2,  1,  8),  -- Arvelduskeskus: Tallinn
    (2,  3,  3),  -- Arvelduskeskus: Tartumaa
    (3,  1, 10),  -- Arvopunkt: Tallinn
    (3,  2,  7),  -- Arvopunkt: Harjumaa
    (3,  3,  5),  -- Arvopunkt: Tartumaa
    (3,  7,  4),  -- Arvopunkt: Ida-Virumaa
    (4,  1,  8),  -- Tasuvara: Tallinn
    (4,  2,  6),  -- Tasuvara: Harjumaa
    (4,  5,  4),  -- Tasuvara: Pärnumaa
    (5,  1,  6),  -- Piletivõrk: Tallinn
    (5,  2,  5),  -- Piletivõrk: Harjumaa
    (5,  3,  4),  -- Piletivõrk: Tartumaa
    (5,  5,  3),  -- Piletivõrk: Pärnumaa
    (6,  1,  4),  -- Müügiruut: Tallinn
    (6,  3,  3),  -- Müügiruut: Tartumaa
    (7,  1,  7),  -- Kaardilahendus: Tallinn
    (7,  2,  5),  -- Kaardilahendus: Harjumaa
    (7,  7,  3),  -- Kaardilahendus: Ida-Virumaa
    (8,  3,  6),  -- Lõunakaardi Teenused: Tartumaa
    (8,  5,  4),  -- Lõunakaardi Teenused: Pärnumaa
    (8, 16,  2),  -- Lõunakaardi Teenused: Põlvamaa
    (9,  1,  3),  -- Terminalipunkt: Tallinn
    (9,  2,  2),  -- Terminalipunkt: Harjumaa
    (10, 1,  9),  -- Sõidukaardi Keskus: Tallinn
    (10, 2,  7),  -- Sõidukaardi Keskus: Harjumaa
    (10, 3,  5),  -- Sõidukaardi Keskus: Tartumaa
    (10, 5,  4);  -- Sõidukaardi Keskus: Pärnumaa

-- ----------------------------------------------------------------
-- commission_rate — teenustasud Exceli tyyp tulba põhjal
-- product_type_id: isikustamine=1, kaardimyyk=2, pilet=3, rahalaadimine=4,
--                  sooduskaardi isikustamine=5, kaardi tagasiost=6, raha valjamakse=7
-- includes_vat=true kõigil | valid_from='2026-01-01' | valid_to=null
-- Koguselt (fee_per_transaction): isikustamine/soodus/tagasiost/raha — EUR tehingult
-- Summalt (fee_percent): pilet/rahalaadimine — % müügisummast
-- ----------------------------------------------------------------
INSERT INTO commission_rate (seller_id, product_type_id, fee_per_transaction, fee_percent, includes_vat, valid_from, valid_to) VALUES
    -- Agent A OÜ (id=1): kaardimyyk, rahalaadimine
    (1, 2, 0.1000, null,  true, '2026-01-01', null),
    (1, 4, null,   1.00,  true, '2026-01-01', null),
    -- Arvelduskeskus OÜ (id=2): pilet
    (2, 3, null,   1.50,  true, '2026-01-01', null),
    -- Arvopunkt OÜ (id=3): kõik 7 tootet
    (3, 1, 0.0500, null,  true, '2026-01-01', null),
    (3, 2, 0.1000, null,  true, '2026-01-01', null),
    (3, 3, null,   1.50,  true, '2026-01-01', null),
    (3, 4, null,   1.00,  true, '2026-01-01', null),
    (3, 5, 0.0500, null,  true, '2026-01-01', null),
    (3, 6, 0.1000, null,  true, '2026-01-01', null),
    (3, 7, 0.1000, null,  true, '2026-01-01', null),
    -- Tasuvara OÜ (id=4): isikustamine, kaardimyyk, pilet, rahalaadimine, sooduskaardi isikustamine
    (4, 1, 0.0500, null,  true, '2026-01-01', null),
    (4, 2, 0.1000, null,  true, '2026-01-01', null),
    (4, 3, null,   1.50,  true, '2026-01-01', null),
    (4, 4, null,   1.00,  true, '2026-01-01', null),
    (4, 5, 0.0500, null,  true, '2026-01-01', null),
    -- Piletivõrk OÜ (id=5): isikustamine, kaardimyyk, pilet, rahalaadimine, sooduskaardi isikustamine
    (5, 1, 0.0500, null,  true, '2026-01-01', null),
    (5, 2, 0.1000, null,  true, '2026-01-01', null),
    (5, 3, null,   1.50,  true, '2026-01-01', null),
    (5, 4, null,   1.00,  true, '2026-01-01', null),
    (5, 5, 0.0500, null,  true, '2026-01-01', null),
    -- Müügiruut OÜ (id=6): isikustamine, kaardimyyk, pilet, rahalaadimine
    (6, 1, 0.0500, null,  true, '2026-01-01', null),
    (6, 2, 0.1000, null,  true, '2026-01-01', null),
    (6, 3, null,   1.50,  true, '2026-01-01', null),
    (6, 4, null,   1.00,  true, '2026-01-01', null),
    -- Kaardilahendus OÜ (id=7): isikustamine, kaardimyyk, pilet, rahalaadimine
    (7, 1, 0.0500, null,  true, '2026-01-01', null),
    (7, 2, 0.1000, null,  true, '2026-01-01', null),
    (7, 3, null,   1.50,  true, '2026-01-01', null),
    (7, 4, null,   1.00,  true, '2026-01-01', null),
    -- Lõunakaardi Teenused OÜ (id=8): isikustamine, kaardimyyk, pilet, rahalaadimine, kaardi tagasiost, raha valjamakse
    (8, 1, 0.0500, null,  true, '2026-01-01', null),
    (8, 2, 0.1000, null,  true, '2026-01-01', null),
    (8, 3, null,   1.50,  true, '2026-01-01', null),
    (8, 4, null,   1.00,  true, '2026-01-01', null),
    (8, 6, 0.1000, null,  true, '2026-01-01', null),
    (8, 7, 0.1000, null,  true, '2026-01-01', null),
    -- Terminalipunkt OÜ (id=9): isikustamine, pilet, sooduskaardi isikustamine, raha valjamakse
    (9, 1, 0.0500, null,  true, '2026-01-01', null),
    (9, 3, null,   1.50,  true, '2026-01-01', null),
    (9, 5, 0.0500, null,  true, '2026-01-01', null),
    (9, 7, 0.1000, null,  true, '2026-01-01', null),
    -- Sõidukaardi Keskus OÜ (id=10): kõik 7 tootet
    (10, 1, 0.0500, null, true, '2026-01-01', null),
    (10, 2, 0.1000, null, true, '2026-01-01', null),
    (10, 3, null,   1.50, true, '2026-01-01', null),
    (10, 4, null,   1.00, true, '2026-01-01', null),
    (10, 5, 0.0500, null, true, '2026-01-01', null),
    (10, 6, 0.1000, null, true, '2026-01-01', null),
    (10, 7, 0.1000, null, true, '2026-01-01', null);