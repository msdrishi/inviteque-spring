-- Add slug column to invites table for human-readable URL-based lookup (e.g. "Pavitra-Sri")
ALTER TABLE invites ADD COLUMN IF NOT EXISTS slug VARCHAR(100) UNIQUE;

-- Seed the Pavitra-Sri invite if it doesn't exist yet
-- This is a no-op if the invite already exists with a slug
