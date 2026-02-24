-- Seed 5 Users (1 Admin, 2 Drivers, 2 Passengers)
-- We insert some static random UUIDs or rely on default gen_random_uuid(). We will use gen_random_uuid() to keep it simple.

INSERT INTO users (phone_number, full_name, role, is_verified, nid_number, average_rating) VALUES
('01777777777', 'System Admin', 'ADMIN', true, 'ADMIN_NID_001', 5.0),
('01711111111', 'Driver One', 'DRIVER', true, 'DRIVER_NID_001', 4.8),
('01722222222', 'Driver Two', 'DRIVER', true, 'DRIVER_NID_002', 4.5),
('01733333333', 'Passenger One', 'PASSENGER', true, 'PAX_NID_001', 4.9),
('01744444444', 'Passenger Two', 'PASSENGER', true, 'PAX_NID_002', 4.7);

-- Note: In a real scenario, we might want to also add mock vehicles for the drivers.
-- We can add them using a subquery to find the newly inserted drivers.
DO $$ 
DECLARE
    driver_1_id UUID;
    driver_2_id UUID;
BEGIN
    SELECT id INTO driver_1_id FROM users WHERE phone_number = '01711111111';
    SELECT id INTO driver_2_id FROM users WHERE phone_number = '01722222222';

    INSERT INTO vehicles (driver_id, make, model, year, license_plate, color, total_capacity, is_approved) VALUES
    (driver_1_id, 'Toyota', 'Premio', 2018, 'Dhaka Metro Ga 12-3456', 'White', 4, true),
    (driver_2_id, 'Honda', 'Vezel', 2019, 'Dhaka Metro Ga 65-4321', 'Black', 4, true);
END $$;
