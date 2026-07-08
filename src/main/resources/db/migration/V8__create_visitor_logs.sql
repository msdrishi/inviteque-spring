-- V8: Create visitor_logs table for analytics
CREATE TABLE IF NOT EXISTS visitor_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    path VARCHAR(512) NOT NULL,
    template_id VARCHAR(100),
    invite_code VARCHAR(50),
    ip_address VARCHAR(100),
    visited_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_visitor_logs_template ON visitor_logs(template_id);
CREATE INDEX IF NOT EXISTS idx_visitor_logs_visited_at ON visitor_logs(visited_at);
