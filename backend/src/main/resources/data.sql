-- Backward-compat for older DB schemas (missing columns).
-- NOTE: Spring splits SQL statements by ';', so avoid DO $$ blocks.
ALTER TABLE app_users ADD COLUMN IF NOT EXISTS role VARCHAR(20);
ALTER TABLE app_users ADD COLUMN IF NOT EXISTS created_at TIMESTAMP;
ALTER TABLE app_users ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;

-- If older rows have NULL role, default them to USER.
UPDATE app_users SET role = 'USER' WHERE role IS NULL;

-- Default admin: admin / password
INSERT INTO app_users (id, username, password_hash, enabled, role, created_at, updated_at)
VALUES (
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    'admin',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'ACTIVE',
    'ADMIN',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (id) DO UPDATE SET
    role = 'ADMIN',
    enabled = 'ACTIVE';

-- Default develop: develop / password (edit login theme & page assets)
INSERT INTO app_users (id, username, password_hash, enabled, role, created_at, updated_at)
VALUES (
    'b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a22',
    'develop',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'ACTIVE',
    'DEVELOP',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (id) DO UPDATE SET
    role = 'DEVELOP',
    enabled = 'ACTIVE';

INSERT INTO app_backgrounds (background_key, image_url, updated_at)
VALUES
    ('LOGIN_HEADER', '', CURRENT_TIMESTAMP),
    ('LOGIN_BODY', '', CURRENT_TIMESTAMP)
ON CONFLICT (background_key) DO NOTHING;
