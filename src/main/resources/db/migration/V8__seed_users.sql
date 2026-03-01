-- V8: Seed dummy users as defined in dummy-credentials.md
--
-- All accounts use password: Pass@1234
-- BCrypt hash (cost 10): $2b$10$FlJ/QmkIb817HoW87/9D9uGuONRde24TVNlnRYk0is4LCqHPsSk4C

-- ── Passengers ───────────────────────────────────────────────────────────────
INSERT INTO users (
    phone_number, full_name, role,
    email, password_hash,
    is_verified, is_nid_verified, is_profile_complete,
    average_rating
) VALUES
(
    '+8801711111111', 'Rafi Ahmed', 'PASSENGER',
    'rafi.ahmed@test.shareint.com',
    '$2b$10$FlJ/QmkIb817HoW87/9D9uGuONRde24TVNlnRYk0is4LCqHPsSk4C',
    true, false, true,
    0.00
),
(
    '+8801722222222', 'Nadia Islam', 'PASSENGER',
    'nadia.islam@test.shareint.com',
    '$2b$10$FlJ/QmkIb817HoW87/9D9uGuONRde24TVNlnRYk0is4LCqHPsSk4C',
    true, false, true,
    0.00
);

-- ── Drivers ──────────────────────────────────────────────────────────────────
INSERT INTO users (
    phone_number, full_name, role,
    email, password_hash,
    is_verified, is_nid_verified, is_profile_complete,
    nid_number, average_rating
) VALUES
(
    '+8801733333333', 'Karim Uddin', 'DRIVER',
    'karim.uddin@test.shareint.com',
    '$2b$10$FlJ/QmkIb817HoW87/9D9uGuONRde24TVNlnRYk0is4LCqHPsSk4C',
    true, true, true,
    '1234567890123', 0.00
),
(
    '+8801744444444', 'Sultana Begum', 'DRIVER',
    'sultana.begum@test.shareint.com',
    '$2b$10$FlJ/QmkIb817HoW87/9D9uGuONRde24TVNlnRYk0is4LCqHPsSk4C',
    true, true, true,
    '9876543210987', 0.00
);

-- ── Vehicles for drivers ─────────────────────────────────────────────────────
DO $$
DECLARE
    karim_id   UUID;
    sultana_id UUID;
BEGIN
    SELECT id INTO karim_id   FROM users WHERE phone_number = '+8801733333333';
    SELECT id INTO sultana_id FROM users WHERE phone_number = '+8801744444444';

    INSERT INTO vehicles (driver_id, make, model, year, license_plate, color, total_capacity, is_approved) VALUES
    (karim_id,   'Toyota', 'Noah',  2017, 'Dhaka-Ka 11-1111', 'White',  6, true),
    (sultana_id, 'Honda',  'CR-V',  2020, 'Dhaka-Ga 22-2222', 'Silver', 4, true);
END $$;
