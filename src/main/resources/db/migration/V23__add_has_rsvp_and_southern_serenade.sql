-- V23: Add has_rsvp column to invites and register southern-serenade template

-- 1. Add persistent has_rsvp entitlement column to invites table
ALTER TABLE invites ADD COLUMN IF NOT EXISTS has_rsvp BOOLEAN NOT NULL DEFAULT FALSE;

-- 2. Performance index on has_rsvp
CREATE INDEX IF NOT EXISTS idx_invites_has_rsvp ON invites(has_rsvp);

-- 3. Backfill has_rsvp for existing paid invitations that purchased RSVP
UPDATE invites 
SET has_rsvp = TRUE 
WHERE status = 'PAID' 
  AND (
    (amount_paid IS NOT NULL AND amount_paid >= 2999) OR
    (rsvp_data IS NOT NULL AND (rsvp_data->>'isPaid' = 'true' OR rsvp_data->>'enabled' = 'true')) OR
    (invitation_data IS NOT NULL AND (invitation_data->>'hasRsvp' = 'true' OR invitation_data->>'isRsvpPaid' = 'true'))
  );

-- 4. Register southern-serenade in master templates table
INSERT INTO templates (id, name, description, price, is_active) VALUES
('southern-serenade', 'Southern Serenade', 'A divine South Indian wedding invitation inspired by sacred traditions, temple architecture, and timeless elegance.', 249900, TRUE)
ON CONFLICT (id) DO NOTHING;

