-- Add NID photo URL and NID verification status for future OCR + API-based NID validation
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS nid_photo_url  VARCHAR(500),
    ADD COLUMN IF NOT EXISTS is_nid_verified BOOLEAN NOT NULL DEFAULT false;
