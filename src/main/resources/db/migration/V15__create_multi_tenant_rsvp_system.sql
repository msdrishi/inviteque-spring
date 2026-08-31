-- V15: Create Multi-Tenant RSVP System tables and indexes

-- 1. Table for Wedding Events (event-level tracking)
CREATE TABLE IF NOT EXISTS wedding_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    wedding_id UUID NOT NULL REFERENCES invites(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    event_date VARCHAR(100),
    event_time VARCHAR(100),
    venue VARCHAR(255),
    address TEXT,
    map_url TEXT,
    qr_code TEXT,
    sort_order INTEGER DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- 2. Table for Guest Groups (e.g., family, relatives, friends, day1, day2)
CREATE TABLE IF NOT EXISTS guest_groups (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    wedding_id UUID NOT NULL REFERENCES invites(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_wedding_group_slug UNIQUE (wedding_id, slug)
);

-- 3. Junction table: which events are visible to which guest group
CREATE TABLE IF NOT EXISTS guest_group_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    guest_group_id UUID NOT NULL REFERENCES guest_groups(id) ON DELETE CASCADE,
    event_id UUID NOT NULL REFERENCES wedding_events(id) ON DELETE CASCADE,
    CONSTRAINT uq_guest_group_event UNIQUE (guest_group_id, event_id)
);

-- 4. Table for Specific Invitation Links
CREATE TABLE IF NOT EXISTS invitation_links (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    wedding_id UUID NOT NULL REFERENCES invites(id) ON DELETE CASCADE,
    guest_group_id UUID REFERENCES guest_groups(id) ON DELETE SET NULL,
    slug VARCHAR(100) NOT NULL,
    label VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_wedding_link_slug UNIQUE (wedding_id, slug)
);

-- 5. Table for RSVPs
CREATE TABLE IF NOT EXISTS rsvps (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    wedding_id UUID NOT NULL REFERENCES invites(id) ON DELETE CASCADE,
    guest_group_id UUID REFERENCES guest_groups(id) ON DELETE SET NULL,
    invitation_link_id UUID REFERENCES invitation_links(id) ON DELETE SET NULL,
    guest_name VARCHAR(255) NOT NULL,
    attendance_status VARCHAR(20) NOT NULL DEFAULT 'yes', -- yes, no, maybe
    guest_count INTEGER NOT NULL DEFAULT 1,
    message TEXT,
    meal_preference VARCHAR(100),
    accommodation_needed BOOLEAN DEFAULT FALSE,
    dietary_notes TEXT,
    idempotency_key VARCHAR(100),
    custom_responses JSONB,
    submitted_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- 6. Table for Event-Level RSVP Responses
CREATE TABLE IF NOT EXISTS rsvp_event_responses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    rsvp_id UUID NOT NULL REFERENCES rsvps(id) ON DELETE CASCADE,
    event_id UUID NOT NULL REFERENCES wedding_events(id) ON DELETE CASCADE,
    response VARCHAR(20) NOT NULL DEFAULT 'yes', -- yes, no, maybe
    CONSTRAINT uq_rsvp_event UNIQUE (rsvp_id, event_id)
);

-- Performance Indexes
CREATE INDEX IF NOT EXISTS idx_wedding_events_wedding_id ON wedding_events(wedding_id);
CREATE INDEX IF NOT EXISTS idx_wedding_events_sort ON wedding_events(wedding_id, sort_order);

CREATE INDEX IF NOT EXISTS idx_guest_groups_wedding_id ON guest_groups(wedding_id);
CREATE INDEX IF NOT EXISTS idx_guest_groups_slug ON guest_groups(wedding_id, slug);

CREATE INDEX IF NOT EXISTS idx_guest_group_events_group ON guest_group_events(guest_group_id);
CREATE INDEX IF NOT EXISTS idx_guest_group_events_event ON guest_group_events(event_id);

CREATE INDEX IF NOT EXISTS idx_invitation_links_wedding ON invitation_links(wedding_id);
CREATE INDEX IF NOT EXISTS idx_invitation_links_slug ON invitation_links(wedding_id, slug);

CREATE INDEX IF NOT EXISTS idx_rsvps_wedding_id ON rsvps(wedding_id);
CREATE INDEX IF NOT EXISTS idx_rsvps_wedding_status ON rsvps(wedding_id, attendance_status);
CREATE INDEX IF NOT EXISTS idx_rsvps_wedding_group ON rsvps(wedding_id, guest_group_id);
CREATE INDEX IF NOT EXISTS idx_rsvps_submitted_at ON rsvps(wedding_id, submitted_at DESC);
CREATE INDEX IF NOT EXISTS idx_rsvps_idempotency ON rsvps(wedding_id, idempotency_key);

CREATE INDEX IF NOT EXISTS idx_rsvp_event_responses_rsvp ON rsvp_event_responses(rsvp_id);
CREATE INDEX IF NOT EXISTS idx_rsvp_event_responses_event ON rsvp_event_responses(event_id);
