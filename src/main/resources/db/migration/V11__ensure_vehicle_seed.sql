-- V11: Idempotent vehicle seed for dummy drivers
-- Uses INSERT ... SELECT to avoid DO $$ PL/pgSQL blocks.
-- Safe to run on any DB state: inserts vehicles if missing, sets is_approved=true if present.

INSERT INTO vehicles (driver_id, make, model, year, license_plate, color, total_capacity, is_approved)
SELECT id, 'Toyota', 'Noah', 2017, 'Dhaka-Ka 11-1111', 'White', 6, true
FROM users
WHERE phone_number = '+8801733333333'
ON CONFLICT (license_plate) DO UPDATE SET is_approved = true;

INSERT INTO vehicles (driver_id, make, model, year, license_plate, color, total_capacity, is_approved)
SELECT id, 'Honda', 'CR-V', 2020, 'Dhaka-Ga 22-2222', 'Silver', 4, true
FROM users
WHERE phone_number = '+8801744444444'
ON CONFLICT (license_plate) DO UPDATE SET is_approved = true;
