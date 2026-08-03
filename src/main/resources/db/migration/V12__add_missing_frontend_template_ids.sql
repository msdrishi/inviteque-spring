-- V12: Add missing frontend-compatible template IDs to the templates table
INSERT INTO templates (id, name, description, price, is_active) VALUES
('template-3', 'Royal Palace', 'A majestic, heritage-rich invitation.', 99900, TRUE),
('royal-palace', 'Royal Palace', 'A majestic, heritage-rich invitation.', 99900, TRUE),
('everlastingvows', 'Everlasting Vows', 'Sophisticated and bold modern invitation.', 99900, TRUE),
('template-5', 'Celestial Union', 'Inspired by the vastness of the horizon.', 99900, TRUE),
('template-6', 'Infinite Journey', 'A soulful narrative-driven theme.', 99900, TRUE),
('royal-heritage', 'Royal Heritage', 'A majestic heritage-rich invitation.', 99900, TRUE),
('enchanted-forest', 'Enchanted Forest', 'A whimsical nature-inspired theme.', 99900, TRUE),
('midnight-waltz', 'Midnight Waltz', 'Deep navy and metallic gold romance.', 99900, TRUE),
('modern-muse', 'Modern Muse', 'Sleek editorial typography.', 99900, TRUE),
('earthy-whispers', 'Earthy Whispers', 'A bohemain earthy tone theme.', 99900, TRUE),
('coastal-serenity', 'Coastal Serenity', 'Breezy coastal and warm sand tones.', 99900, TRUE)
ON CONFLICT (id) DO NOTHING;
