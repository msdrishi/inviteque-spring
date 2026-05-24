-- V7: Add coupon purchase fields to coupons and invites tables
ALTER TABLE coupons 
ADD COLUMN purchased_date TIMESTAMP DEFAULT NULL,
ADD COLUMN invite_id UUID DEFAULT NULL REFERENCES invites(id) ON DELETE SET NULL;

ALTER TABLE invites
ADD COLUMN coupon_code VARCHAR(50) DEFAULT NULL;
