-- V21: Add newly created templates to the master templates table
INSERT INTO templates (id, name, description, price, is_active) VALUES
('pink-blossom', 'Pink Blossom', 'An elegant invitation with a hot pink, light green, and ivory theme.', 249900, TRUE),
('aura-of-elegance', 'Aura of Elegance', 'A luxurious green-themed cinematic invite.', 199900, TRUE),
('twilight-serenade', 'Twilight Serenade', 'A beautiful twilight themed animated invitation.', 199900, TRUE),
('house-warming-2', 'House Warming 2', 'Elegant modern house warming invitation.', 199900, TRUE),
('house-warming-3', 'House Warming 3', 'Warm and inviting house warming template.', 199900, TRUE)
ON CONFLICT (id) DO NOTHING;
