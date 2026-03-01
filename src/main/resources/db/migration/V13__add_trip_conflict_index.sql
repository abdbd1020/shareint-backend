-- Index for fast trip conflict lookups (scheduling validation)
-- Used when a driver publishes a new trip or attempts to start an existing one
CREATE INDEX idx_trips_driver_vehicle_active
    ON trips (driver_id, vehicle_id, status, departure_time)
    WHERE deleted_at IS NULL AND status IN ('SCHEDULED', 'IN_PROGRESS');
