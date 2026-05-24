-- V6: Create coupons table and populate 50 unique random coupons
CREATE TABLE coupons (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL UNIQUE,
    discount_percentage INT NOT NULL DEFAULT 50,
    is_available BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_coupons_code ON coupons(code);

-- Generate 50 unique random coupons starting with 'IQ50-'
DO $$
DECLARE
    i INT;
    chars TEXT := 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789';
    code_val TEXT;
    char_len INT := 6;
    j INT;
BEGIN
    FOR i IN 1..50 LOOP
        LOOP
            code_val := 'IQ50-';
            FOR j IN 1..char_len LOOP
                code_val := code_val || substr(chars, floor(random() * length(chars) + 1)::int, 1);
            END LOOP;
            
            IF NOT EXISTS (SELECT 1 FROM coupons WHERE code = code_val) THEN
                INSERT INTO coupons (code, discount_percentage, is_available) 
                VALUES (code_val, 50, TRUE);
                EXIT;
            END IF;
        END LOOP;
    END LOOP;
END $$;
