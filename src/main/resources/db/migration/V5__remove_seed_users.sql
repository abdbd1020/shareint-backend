-- Remove seed dummy data added in V4
-- Vehicles must be deleted first due to FK ON DELETE RESTRICT on driver_id

DELETE FROM vehicles
WHERE driver_id IN (
    SELECT id FROM users
    WHERE phone_number IN ('01711111111', '01722222222')
);

DELETE FROM users
WHERE phone_number IN (
    '01777777777',
    '01711111111',
    '01722222222',
    '01733333333',
    '01744444444'
);
