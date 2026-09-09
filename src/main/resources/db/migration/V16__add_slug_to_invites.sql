-- Add slug column to invites table for human-readable URL-based lookup
ALTER TABLE invites ADD COLUMN IF NOT EXISTS slug VARCHAR(100) UNIQUE;
