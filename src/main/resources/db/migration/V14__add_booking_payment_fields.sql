-- Add payment tracking fields to bookings
ALTER TABLE bookings
    ADD COLUMN payment_method   VARCHAR(20),
    ADD COLUMN commission_paid_at TIMESTAMP;
