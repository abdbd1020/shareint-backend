-- ==========================================
-- 1. ENUMS
-- ==========================================
CREATE TYPE role_type AS ENUM ('PASSENGER', 'DRIVER', 'ADMIN');
CREATE TYPE trip_status AS ENUM ('SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED');
CREATE TYPE booking_status AS ENUM ('PENDING_PAYMENT', 'CONFIRMED', 'CANCELLED', 'COMPLETED', 'REFUNDED');
CREATE TYPE payment_status AS ENUM ('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED');

-- ==========================================
-- 2. TABLES
-- ==========================================

-- LOCATIONS (Hierarchical: Division -> Zilla -> Upazilla)
CREATE TABLE locations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name_en VARCHAR(100) NOT NULL,
    name_bn VARCHAR(100),
    parent_id UUID REFERENCES locations(id) ON DELETE RESTRICT,
    is_distance_considered BOOLEAN DEFAULT true,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMPTZ
);

-- USERS (Unified Auth Table)
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    phone_number VARCHAR(15) UNIQUE NOT NULL,
    full_name VARCHAR(150),
    role role_type DEFAULT 'PASSENGER',
    is_verified BOOLEAN DEFAULT false,
    nid_number VARCHAR(50) UNIQUE,
    avatar_url VARCHAR(255),
    average_rating DECIMAL(3, 2) DEFAULT 0.00,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMPTZ
);

-- Audit Columns: Add foreign keys pointing to users after users is created.
ALTER TABLE locations ADD COLUMN creator_id UUID REFERENCES users(id) ON DELETE SET NULL;
ALTER TABLE locations ADD COLUMN updater_id UUID REFERENCES users(id) ON DELETE SET NULL;
ALTER TABLE users ADD COLUMN creator_id UUID REFERENCES users(id) ON DELETE SET NULL;
ALTER TABLE users ADD COLUMN updater_id UUID REFERENCES users(id) ON DELETE SET NULL;

-- VEHICLES
CREATE TABLE vehicles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    driver_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    make VARCHAR(50) NOT NULL,
    model VARCHAR(50) NOT NULL,
    year INTEGER,
    license_plate VARCHAR(30) UNIQUE NOT NULL,
    color VARCHAR(30),
    total_capacity INTEGER NOT NULL,
    is_approved BOOLEAN DEFAULT false,
    creator_id UUID REFERENCES users(id) ON DELETE SET NULL,
    updater_id UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMPTZ
);

-- TRIPS
CREATE TABLE trips (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    driver_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    vehicle_id UUID NOT NULL REFERENCES vehicles(id) ON DELETE RESTRICT,
    origin_location_id UUID NOT NULL REFERENCES locations(id) ON DELETE RESTRICT,
    destination_location_id UUID NOT NULL REFERENCES locations(id) ON DELETE RESTRICT,
    departure_time TIMESTAMPTZ NOT NULL,
    estimated_arrival_time TIMESTAMPTZ,
    price_per_seat DECIMAL(10, 2) NOT NULL CHECK (price_per_seat > 0),
    available_seats INTEGER NOT NULL CHECK (available_seats >= 0),
    status trip_status DEFAULT 'SCHEDULED',
    meeting_point VARCHAR(255), 
    drop_off_point VARCHAR(255),
    creator_id UUID REFERENCES users(id) ON DELETE SET NULL,
    updater_id UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMPTZ
);

-- BOOKINGS
CREATE TABLE bookings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    trip_id UUID NOT NULL REFERENCES trips(id) ON DELETE RESTRICT,
    passenger_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    total_seat_price DECIMAL(10, 2) NOT NULL CHECK (total_seat_price > 0),
    platform_fee DECIMAL(10, 2) NOT NULL CHECK (platform_fee >= 0),
    total_charged DECIMAL(10, 2) GENERATED ALWAYS AS (total_seat_price + platform_fee) STORED,
    booked_seats_count INTEGER NOT NULL CHECK (booked_seats_count > 0),
    status booking_status DEFAULT 'PENDING_PAYMENT',
    creator_id UUID REFERENCES users(id) ON DELETE SET NULL,
    updater_id UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMPTZ
);

-- TRIP_SEATS
CREATE TABLE trip_seats (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    trip_id UUID NOT NULL REFERENCES trips(id) ON DELETE CASCADE,
    booking_id UUID REFERENCES bookings(id) ON DELETE SET NULL,
    seat_identifier VARCHAR(20) NOT NULL,
    is_booked BOOLEAN DEFAULT false,
    creator_id UUID REFERENCES users(id) ON DELETE SET NULL,
    updater_id UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (trip_id, seat_identifier)
);

-- PAYMENTS
CREATE TABLE payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id UUID NOT NULL REFERENCES bookings(id) ON DELETE RESTRICT,
    passenger_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    amount DECIMAL(10, 2) NOT NULL,
    gateway_provider VARCHAR(50) NOT NULL,
    transaction_id VARCHAR(100) UNIQUE,
    gateway_response JSONB,
    status payment_status DEFAULT 'PENDING',
    creator_id UUID REFERENCES users(id) ON DELETE SET NULL,
    updater_id UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- RATINGS
CREATE TABLE ratings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    trip_id UUID NOT NULL REFERENCES trips(id) ON DELETE RESTRICT,
    reviewer_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    reviewee_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    score INTEGER NOT NULL CHECK (score >= 1 AND score <= 5),
    comment TEXT,
    creator_id UUID REFERENCES users(id) ON DELETE SET NULL,
    updater_id UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(trip_id, reviewer_id, reviewee_id)
);

-- ==========================================
-- 3. INDEXES
-- ==========================================
CREATE INDEX idx_trips_search 
    ON trips (origin_location_id, destination_location_id, departure_time)
    WHERE deleted_at IS NULL AND status = 'SCHEDULED';

CREATE INDEX idx_locations_parent_id ON locations(parent_id);
CREATE INDEX idx_trips_driver_id ON trips(driver_id);
CREATE INDEX idx_trips_vehicle_id ON trips(vehicle_id);
CREATE INDEX idx_bookings_trip_id ON bookings(trip_id);
CREATE INDEX idx_bookings_passenger_id ON bookings(passenger_id);
CREATE INDEX idx_trip_seats_trip_id ON trip_seats(trip_id);
CREATE INDEX idx_trip_seats_booking_id ON trip_seats(booking_id);
CREATE INDEX idx_payments_booking_id ON payments(booking_id);
CREATE INDEX idx_ratings_trip_id ON ratings(trip_id);
CREATE INDEX idx_ratings_reviewee_id ON ratings(reviewee_id);
CREATE INDEX idx_users_phone ON users(phone_number) WHERE deleted_at IS NULL;
