-- ============================================================
-- DATA SEED FILE — runs on startup (spring.sql.init.mode=always)
-- Admin password = 'admin123' (BCrypt hashed)
-- ============================================================

-- Insert default admin only if not already exists
INSERT INTO admins (username, password_hash)
SELECT 'admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh3y'
WHERE NOT EXISTS (
    SELECT 1 FROM admins WHERE username = 'admin'
);
