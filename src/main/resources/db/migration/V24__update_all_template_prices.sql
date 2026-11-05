-- V24: Update all template prices and aliases in templates table to match current catalog configuration

-- 1. Ensure southern-serenade and its common slug variations exist
INSERT INTO templates (id, name, description, price, is_active) VALUES
('southern-serenade', 'Southern Serenade', 'A divine South Indian wedding invitation inspired by sacred traditions, temple architecture, and timeless elegance.', 249900, TRUE)
ON CONFLICT (id) DO UPDATE SET price = 249900, is_active = TRUE;

INSERT INTO templates (id, name, description, price, is_active) VALUES
('southern_serenade', 'Southern Serenade', 'A divine South Indian wedding invitation inspired by sacred traditions, temple architecture, and timeless elegance.', 249900, TRUE)
ON CONFLICT (id) DO UPDATE SET price = 249900, is_active = TRUE;

INSERT INTO templates (id, name, description, price, is_active) VALUES
('southernserenade', 'Southern Serenade', 'A divine South Indian wedding invitation inspired by sacred traditions, temple architecture, and timeless elegance.', 249900, TRUE)
ON CONFLICT (id) DO UPDATE SET price = 249900, is_active = TRUE;

-- 2. Ensure Rajasthani Vows aliases exist
INSERT INTO templates (id, name, description, price, is_active) VALUES
('rajasthani-vows', 'Rajasthani Vows', 'A majestic royal Rajasthani invitation with authentic cultural motifs.', 249900, TRUE)
ON CONFLICT (id) DO UPDATE SET price = 249900, is_active = TRUE;

INSERT INTO templates (id, name, description, price, is_active) VALUES
('rajestani-vows', 'Rajasthani Vows', 'A majestic royal Rajasthani invitation with authentic cultural motifs.', 249900, TRUE)
ON CONFLICT (id) DO UPDATE SET price = 249900, is_active = TRUE;

-- 3. Premium tier (₹2,499 = 249900 paise)
UPDATE templates SET price = 249900 WHERE LOWER(id) IN (
    'southern-serenade',
    'southern_serenade',
    'southernserenade',
    'rajasthani-vows',
    'rajestani-vows',
    'royal-heritage',
    'pink-blossom',
    'midnight-waltz',
    'royal-heirloom'
);

-- 4. Standard tier (₹1,999 = 199900 paise)
UPDATE templates SET price = 199900 WHERE LOWER(id) IN (
    'aura-of-elegance',
    'template-1',
    'twilight-serenade',
    'template-2',
    'sunflower-fields',
    'sunflowerfields',
    'template-3',
    'blossom-whisper',
    'everlastingvows',
    'everlasting-vows',
    'template-4',
    'modernhearth',
    'modern-hearth',
    'house-warming-1',
    'house-warming-2',
    'house-warming-3',
    'enchanted-forest',
    'template-8',
    'modern-muse',
    'template-10',
    'earthy-whispers',
    'template-11',
    'coastal-serenity',
    'template-12',
    'template-5',
    'celestial-union',
    'template-6',
    'infinite-journey',
    'template-7',
    'template-9',
    'royal-palace'
);
