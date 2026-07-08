-- V9: Alter users table and create otp_verifications table

-- 1. Make email and name nullable in the users table
ALTER TABLE users ALTER COLUMN email DROP NOT NULL;
ALTER TABLE users ALTER COLUMN name DROP NOT NULL;

-- 2. Create otp_verifications table
CREATE TABLE IF NOT EXISTS otp_verifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    phone_number VARCHAR(50) NOT NULL,
    code VARCHAR(10) NOT NULL,
    expiry_time TIMESTAMP NOT NULL,
    verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- 3. Create index for performance
CREATE INDEX IF NOT EXISTS idx_otp_phone ON otp_verifications(phone_number);
