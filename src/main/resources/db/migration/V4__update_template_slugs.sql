-- V4: Update template IDs to readable slugs safely
-- 1. Insert new template records with slugs
INSERT INTO templates (id, name, description, price, is_active) VALUES
('aura-of-elegance', 'Aura of Elegance', 'A timeless masterpiece designed for those who appreciate the finer details of a grand celebration.', 99900, TRUE),
('twilight-serenade', 'Twilight Serenade', 'Capture the magic of a starlit evening with this warm, glowing invitation theme.', 99900, TRUE),
('blossom-whisper', 'Blossom Whisper', 'Delicate and romantic, this theme brings a soft, poetic touch to your wedding announcement.', 99900, TRUE),
('everlasting-vows', 'Everlasting Vows', 'Sophisticated and bold, focusing on the strength of your commitment through clean, modern design.', 99900, TRUE),
('celestial-union', 'Celestial Union', 'Inspired by the vastness of the horizon, perfect for a love that knows no bounds.', 99900, TRUE),
('infinite-journey', 'Infinite Journey', 'A soulful, narrative-driven theme that celebrates the unique story of your togetherness.', 99900, TRUE)
ON CONFLICT (id) DO NOTHING;

-- 2. Update the references in the invites table
UPDATE invites SET template_id = 'aura-of-elegance' WHERE template_id = 'template-1';
UPDATE invites SET template_id = 'twilight-serenade' WHERE template_id = 'template-2';
UPDATE invites SET template_id = 'blossom-whisper' WHERE template_id = 'template-3';
UPDATE invites SET template_id = 'everlasting-vows' WHERE template_id = 'template-4';
UPDATE invites SET template_id = 'celestial-union' WHERE template_id = 'template-5';
UPDATE invites SET template_id = 'infinite-journey' WHERE template_id = 'template-6';

-- 3. Delete the old template records that are no longer used
DELETE FROM templates WHERE id IN ('template-1', 'template-2', 'template-3', 'template-4', 'template-5', 'template-6');
