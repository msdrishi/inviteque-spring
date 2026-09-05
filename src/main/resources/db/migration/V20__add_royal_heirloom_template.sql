-- V20: Add Royal Heirloom template to master templates table
INSERT INTO templates (id, name, description, price, is_active) VALUES
('royal-heirloom', 'Royal Heirloom', 'A regal, heritage-inspired invitation with vintage floral motifs, cinematic cover animation, and elegant serif typography.', 99900, TRUE)
ON CONFLICT (id) DO NOTHING;
