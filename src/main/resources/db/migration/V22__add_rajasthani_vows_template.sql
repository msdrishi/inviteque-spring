-- V22: Add Rajasthani Vows template to master templates table
INSERT INTO templates (id, name, description, price, is_active) VALUES
('rajasthani-vows', 'Rajasthani Vows', 'A majestic royal Rajasthani invitation with authentic cultural motifs, regal heritage artwork, and traditional celebrations.', 249900, TRUE),
('rajestani-vows', 'Rajasthani Vows', 'A majestic royal Rajasthani invitation with authentic cultural motifs, regal heritage artwork, and traditional celebrations.', 249900, TRUE)
ON CONFLICT (id) DO NOTHING;
