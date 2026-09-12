-- Waitless demo data: one venue, five tables, a menu, three waitresses and a live queue.
-- This is the mobile app's MockRequestRepositoryImpl and the web app's MOCK_TABLES,
-- moved into Postgres.
--
--   psql "postgresql://postgres:postgres@localhost:5432/waitless" -f seed-demo-data.sql
--
-- Safe to re-run: everything below is keyed by fixed UUIDs and deleted first.
-- NOT a Flyway migration on purpose — it lives outside src/main/resources so it never
-- runs automatically against a real database.

BEGIN;

-- The account is attached to the built-in mock Firebase user, so `Bearer waitless-local-token`
-- works against it out of the box (see firebase.mock.* in application.yml).
-- To hand the venue to your real Firebase account instead, run this afterwards:
--   UPDATE store SET account_id = (SELECT id FROM account WHERE firebase_uid = '<your-uid>')
--   WHERE id = '5100...0001';

DELETE FROM service_request WHERE store_id = '51000000-0000-0000-0000-000000000001';
DELETE FROM staff_member    WHERE store_id = '51000000-0000-0000-0000-000000000001';
DELETE FROM menu_item       WHERE store_id = '51000000-0000-0000-0000-000000000001';
DELETE FROM store_table     WHERE store_id = '51000000-0000-0000-0000-000000000001';
DELETE FROM store           WHERE id       = '51000000-0000-0000-0000-000000000001';
DELETE FROM account         WHERE id       = '10000000-0000-0000-0000-000000000001';

INSERT INTO account (id, firebase_uid, full_name, email, phone_area, phone_number) VALUES
    ('10000000-0000-0000-0000-000000000001', 'mock-user-001', 'Demo Owner', 'owner@example.com', '+212', '600000001');

INSERT INTO store (id, account_id, name, created_at) VALUES
    ('51000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', 'Café Atlas', now());

-- qr_token is normally a random 32-char string from QrTokenGenerator. These are readable
-- on purpose so you can type them straight into the web app: http://localhost:5173/?t=demo-table-04
INSERT INTO store_table (id, store_id, table_number, zone, qr_token) VALUES
    ('7ab1e000-0000-0000-0000-000000000004', '51000000-0000-0000-0000-000000000001',  4, 'salon',   'demo-table-04'),
    ('7ab1e000-0000-0000-0000-000000000008', '51000000-0000-0000-0000-000000000001',  8, 'lounge',  'demo-table-08'),
    ('7ab1e000-0000-0000-0000-000000000012', '51000000-0000-0000-0000-000000000001', 12, 'terrace', 'demo-table-12'),
    ('7ab1e000-0000-0000-0000-000000000016', '51000000-0000-0000-0000-000000000001', 16, 'terrace', 'demo-table-16'),
    ('7ab1e000-0000-0000-0000-000000000021', '51000000-0000-0000-0000-000000000001', 21, 'patio',   'demo-table-21');

-- Prices are MAD, matching the web mock.
INSERT INTO menu_item (id, store_id, category, name, description, price) VALUES
    ('a0e00000-0000-0000-0000-000000000001', '51000000-0000-0000-0000-000000000001', 'Coffee',    'Espresso',           'Single origin, roasted in Casablanca',        18.00),
    ('a0e00000-0000-0000-0000-000000000002', '51000000-0000-0000-0000-000000000001', 'Coffee',    'Café crème',         'Double shot, steamed milk',                   28.00),
    ('a0e00000-0000-0000-0000-000000000003', '51000000-0000-0000-0000-000000000001', 'Coffee',    'Mint tea',           'Gunpowder, fresh nana mint',                  22.00),
    ('a0e00000-0000-0000-0000-000000000004', '51000000-0000-0000-0000-000000000001', 'Breakfast', 'Msmen & honey',      'Warm layered pancake, amlou, thyme honey',    45.00),
    ('a0e00000-0000-0000-0000-000000000005', '51000000-0000-0000-0000-000000000001', 'Breakfast', 'Atlas bowl',         'Yogurt, orange blossom, almonds, dates',      52.00),
    ('a0e00000-0000-0000-0000-000000000006', '51000000-0000-0000-0000-000000000001', 'Kitchen',   'Chicken pastilla',   'Crisp warqa, cinnamon, toasted almonds',      95.00),
    ('a0e00000-0000-0000-0000-000000000007', '51000000-0000-0000-0000-000000000001', 'Kitchen',   'Lamb tagine',        'Prunes, sesame, slow-cooked in clay',        120.00),
    ('a0e00000-0000-0000-0000-000000000008', '51000000-0000-0000-0000-000000000001', 'Kitchen',   'Sea bass chermoula', 'Charred vegetables, preserved lemon',        135.00);

-- bcrypt hashes generated with the app's own BCryptPasswordEncoder and verified against
-- the plaintext. Amina = 1234, Youssef = 2468, Sara = 1357.
INSERT INTO staff_member (id, store_id, full_name, pin_hash) VALUES
    ('57aff000-0000-0000-0000-000000000001', '51000000-0000-0000-0000-000000000001', 'Amina Benali',  '$2a$10$9uIi30pjqJBTIrcqNr7yRO6SdFg6eZpY7Ia4PmfESO/btEOQ1vedi'),
    ('57aff000-0000-0000-0000-000000000002', '51000000-0000-0000-0000-000000000001', 'Youssef Idrissi', '$2a$10$wQErAKiuAcJt/ZFekJITduRLOZvZo/eNek3TjDguCLLrua2c00BuC'),
    ('57aff000-0000-0000-0000-000000000003', '51000000-0000-0000-0000-000000000001', 'Sara El Amrani', '$2a$10$IHNnPh81Hpt2f.C3aoPfmuh1f.M1X6nX5zjvNmxwH6rEHtCphOZcW');

-- The four seeded requests from MockRequestRepositoryImpl, with timestamps relative to now
-- so the queue always shows sensible "2 min ago" labels. r2 is already acknowledged.
INSERT INTO service_request
    (id, store_id, table_id, type, status, payment_method, created_at, acknowledged_by_id, acknowledged_at, resolved_at) VALUES
    ('4e900000-0000-0000-0000-000000000001', '51000000-0000-0000-0000-000000000001', '7ab1e000-0000-0000-0000-000000000012',
     'CALL_WAITER',  'OPEN',         NULL,   now() - interval '18 seconds', NULL, NULL, NULL),
    ('4e900000-0000-0000-0000-000000000002', '51000000-0000-0000-0000-000000000001', '7ab1e000-0000-0000-0000-000000000004',
     'REQUEST_BILL', 'ACKNOWLEDGED', 'CARD', now() - interval '2 minutes',
     '57aff000-0000-0000-0000-000000000001', now() - interval '1 minute', NULL),
    ('4e900000-0000-0000-0000-000000000003', '51000000-0000-0000-0000-000000000001', '7ab1e000-0000-0000-0000-000000000021',
     'CALL_WAITER',  'OPEN',         NULL,   now() - interval '4 minutes', NULL, NULL, NULL),
    ('4e900000-0000-0000-0000-000000000004', '51000000-0000-0000-0000-000000000001', '7ab1e000-0000-0000-0000-000000000008',
     'REQUEST_BILL', 'OPEN',         'CASH', now() - interval '40 seconds', NULL, NULL, NULL),
    -- Resolved earlier today: proves the active-requests query filters correctly.
    ('4e900000-0000-0000-0000-000000000005', '51000000-0000-0000-0000-000000000001', '7ab1e000-0000-0000-0000-000000000016',
     'CALL_WAITER',  'RESOLVED',     NULL,   now() - interval '1 hour',
     '57aff000-0000-0000-0000-000000000002', now() - interval '58 minutes', now() - interval '55 minutes');

COMMIT;
