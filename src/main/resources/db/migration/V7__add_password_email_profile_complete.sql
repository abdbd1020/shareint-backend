-- V7: Add password-based login, email, profile completion flag,
--     driver document storage, and uniqueness constraints.

-- 1. New columns on users table
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS password_hash       VARCHAR(255),
    ADD COLUMN IF NOT EXISTS email               VARCHAR(255),
    ADD COLUMN IF NOT EXISTS is_profile_complete BOOLEAN NOT NULL DEFAULT false;

-- Mark existing users with a full name as complete to avoid locking them out
UPDATE users
SET is_profile_complete = true
WHERE full_name IS NOT NULL AND full_name <> '';

-- 2. Unique constraint on email (partial: ignores NULLs natively in PostgreSQL)
CREATE UNIQUE INDEX IF NOT EXISTS idx_users_email_unique
    ON users (email)
    WHERE email IS NOT NULL AND deleted_at IS NULL;

-- 3. Driver documents table
--    Stores each submitted document for admin review.
--    One row per document type per user.
CREATE TABLE IF NOT EXISTS driver_documents (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    document_type   VARCHAR(50) NOT NULL,
    document_url    VARCHAR(500) NOT NULL,
    status          VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    reviewed_by     UUID REFERENCES users(id) ON DELETE SET NULL,
    reviewed_at     TIMESTAMPTZ,
    rejection_note  TEXT,
    created_at      TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, document_type)
);

CREATE INDEX IF NOT EXISTS idx_driver_documents_user_id   ON driver_documents(user_id);
CREATE INDEX IF NOT EXISTS idx_driver_documents_status    ON driver_documents(status) WHERE status = 'PENDING';
