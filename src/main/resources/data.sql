-- ============================================================
-- DATA SEED FILE — runs on startup (spring.sql.init.mode=always)
-- Admin password = 'admin123' (stored as plain text — no hashing)
-- ============================================================

-- Insert default admin only if not already exists
INSERT INTO admins (username, password_hash)
SELECT 'admin', 'admin123'
WHERE NOT EXISTS (
    SELECT 1 FROM admins WHERE username = 'admin'
);
