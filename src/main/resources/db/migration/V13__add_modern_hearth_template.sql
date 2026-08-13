-- V13: Add Modern Hearth templates to the master templates table
INSERT INTO templates (id, name, description, price, is_active) VALUES
('modernhearth', 'Modern Hearth', 'A clean, modern housewarming invitation with elegant transitions.', 99900, TRUE),
('modern-hearth', 'Modern Hearth', 'A clean, modern housewarming invitation with elegant transitions.', 99900, TRUE)
ON CONFLICT (id) DO NOTHING;
