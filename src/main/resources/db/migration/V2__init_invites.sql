-- V2: Create templates and invites tables
CREATE TABLE templates (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price INTEGER NOT NULL,  -- in paise (INR)
    thumbnail_url VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Insert default templates
INSERT INTO templates (id, name, description, price, is_active) VALUES
('auraflower', 'Aura Flower', 'A romantic floral cinematic template with smooth animations', 79900, TRUE),
('royalclassic', 'Royal Classic', 'Elegant royal design with gold accents', 99900, TRUE),
('modernminimal', 'Modern Minimal', 'Clean and minimal design for contemporary couples', 49900, TRUE);

CREATE TABLE invites (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    template_id VARCHAR(50) NOT NULL REFERENCES templates(id),
    code VARCHAR(10) NOT NULL UNIQUE,  -- public unique code e.g. "43jl34"
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',  -- DRAFT, PAID
    -- Wedding details stored as JSONB for flexibility
    couple_data JSONB,
    hero_data JSONB,
    story_data JSONB,
    invitation_data JSONB,
    event_data JSONB,
    venue_data JSONB,
    schedule_data JSONB,
    rsvp_data JSONB,
    -- Payment info
    razorpay_order_id VARCHAR(255),
    razorpay_payment_id VARCHAR(255),
    amount_paid INTEGER,
    paid_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_invites_user_id ON invites(user_id);
CREATE INDEX idx_invites_code ON invites(code);
CREATE INDEX idx_invites_status ON invites(status);
