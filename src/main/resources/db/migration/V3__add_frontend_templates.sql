-- V3: Add frontend template IDs to the database
INSERT INTO templates (id, name, description, price, is_active) VALUES
('template-1', 'Aura of Elegance', 'A timeless masterpiece designed for those who appreciate the finer details of a grand celebration.', 99900, TRUE),
('template-2', 'Twilight Serenade', 'Capture the magic of a starlit evening with this warm, glowing invitation theme.', 99900, TRUE),
('template-3', 'Blossom Whisper', 'Delicate and romantic, this theme brings a soft, poetic touch to your wedding announcement.', 99900, TRUE),
('template-4', 'Everlasting Vows', 'Sophisticated and bold, focusing on the strength of your commitment through clean, modern design.', 99900, TRUE),
('template-5', 'Celestial Union', 'Inspired by the vastness of the horizon, perfect for a love that knows no bounds.', 99900, TRUE),
('template-6', 'Infinite Journey', 'A soulful, narrative-driven theme that celebrates the unique story of your togetherness.', 99900, TRUE),
('template-7', 'Template 7', 'Coming soon.', 99900, TRUE),
('template-8', 'Template 8', 'Coming soon.', 99900, TRUE),
('template-9', 'Template 9', 'Coming soon.', 99900, TRUE),
('template-10', 'Template 10', 'Coming soon.', 99900, TRUE),
('template-11', 'Template 11', 'Coming soon.', 99900, TRUE),
('template-12', 'Template 12', 'Coming soon.', 99900, TRUE)
ON CONFLICT (id) DO NOTHING;
