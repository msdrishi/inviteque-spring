-- V14: Add Sunflower Fields template to master templates table
INSERT INTO templates (id, name, description, price, is_active) VALUES
('sunflower-fields', 'Sunflower Fields', 'A vibrant sunflower-themed wedding invitation.', 99900, TRUE),
('sunflowerfields', 'Sunflower Fields', 'A vibrant sunflower-themed wedding invitation.', 99900, TRUE)
ON CONFLICT (id) DO NOTHING;
