-- V11: Add device_type column to visitor_logs table and alter amount_paid column in invites table
ALTER TABLE visitor_logs ADD COLUMN IF NOT EXISTS device_type VARCHAR(50);
ALTER TABLE invites ALTER COLUMN amount_paid TYPE DOUBLE PRECISION;
