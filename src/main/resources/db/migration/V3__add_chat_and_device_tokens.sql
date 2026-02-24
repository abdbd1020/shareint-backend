-- ==========================================
-- 1. DEVICE TOKENS (For FCM Push Notifications)
-- ==========================================
CREATE TABLE device_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    fcm_token VARCHAR(255) UNIQUE NOT NULL,
    device_type VARCHAR(20),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_device_tokens_user_id ON device_tokens(user_id);

-- ==========================================
-- 2. CHAT ROOMS (For Firebase Chat tracking)
-- ==========================================
CREATE TABLE chat_rooms (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    trip_id UUID NOT NULL REFERENCES trips(id) ON DELETE CASCADE,
    passenger_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    driver_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    firebase_room_id VARCHAR(100) UNIQUE NOT NULL,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (trip_id, passenger_id, driver_id)
);

CREATE INDEX idx_chat_rooms_trip_id ON chat_rooms(trip_id);
CREATE INDEX idx_chat_rooms_passenger_id ON chat_rooms(passenger_id);
CREATE INDEX idx_chat_rooms_driver_id ON chat_rooms(driver_id);
