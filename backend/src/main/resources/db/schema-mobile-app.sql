-- Layla mobile app schema (PostgreSQL)
-- Chạy sau khi đã có app_users, app_backgrounds (Hibernate / data.sql)
-- Tham chiếu: docs/MOBILE_API_AND_DATABASE.md

-- ========== Master: gói dịch vụ ==========
CREATE TABLE IF NOT EXISTS service_packages (
    code            VARCHAR(32) PRIMARY KEY,
    tier            VARCHAR(10) NOT NULL,           -- BASIC | PRO
    label           VARCHAR(120) NOT NULL,
    quota_posts     INT NOT NULL DEFAULT 0,
    quota_images    INT NOT NULL DEFAULT 0,
    quota_videos    INT NOT NULL DEFAULT 0,
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order      INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS service_definitions (
    id              VARCHAR(32) PRIMARY KEY,
    icon            VARCHAR(32),
    name            VARCHAR(120) NOT NULL,
    description     TEXT,
    tier_scope      VARCHAR(10) NOT NULL,           -- BASIC | PRO | BOTH
    sort_order      INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS package_service_items (
    package_code    VARCHAR(32) NOT NULL REFERENCES service_packages(code) ON DELETE CASCADE,
    service_id      VARCHAR(32) NOT NULL REFERENCES service_definitions(id) ON DELETE CASCADE,
    PRIMARY KEY (package_code, service_id)
);

-- ========== Gói khách đang dùng ==========
CREATE TABLE IF NOT EXISTS user_subscriptions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         VARCHAR(100) NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    package_code    VARCHAR(32) NOT NULL REFERENCES service_packages(code),
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    start_date      DATE NOT NULL,
    end_date        DATE NOT NULL,
    display_title   VARCHAR(200),
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_user_subscriptions_user ON user_subscriptions(user_id);
CREATE INDEX IF NOT EXISTS idx_user_subscriptions_active ON user_subscriptions(user_id, status);

CREATE TABLE IF NOT EXISTS subscription_progress (
    subscription_id UUID PRIMARY KEY REFERENCES user_subscriptions(id) ON DELETE CASCADE,
    completed_posts   INT NOT NULL DEFAULT 0,
    completed_images  INT NOT NULL DEFAULT 0,
    completed_videos  INT NOT NULL DEFAULT 0,
    updated_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ========== Dịch vụ / triển khai ==========
CREATE TABLE IF NOT EXISTS implementation_items (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    subscription_id   UUID NOT NULL REFERENCES user_subscriptions(id) ON DELETE CASCADE,
    code              VARCHAR(64) NOT NULL,
    category          VARCHAR(20) NOT NULL,
    title             VARCHAR(200) NOT NULL,
    current_count     INT NOT NULL DEFAULT 0,
    target_count      INT NOT NULL DEFAULT 0,
    status            VARCHAR(32) NOT NULL,
    updated_on        DATE,
    sort_order        INT NOT NULL DEFAULT 0,
    UNIQUE (subscription_id, code)
);

CREATE INDEX IF NOT EXISTS idx_impl_items_sub ON implementation_items(subscription_id);

-- ========== Bài gửi duyệt / đánh giá ==========
CREATE TABLE IF NOT EXISTS deliverables (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    implementation_item_id  UUID REFERENCES implementation_items(id) ON DELETE SET NULL,
    subscription_id         UUID NOT NULL REFERENCES user_subscriptions(id) ON DELETE CASCADE,
    post_number             VARCHAR(32) NOT NULL,
    thumbnail_url           TEXT,
    preview_url             TEXT,
    team_content_score      NUMERIC(3,1),
    team_design_score       NUMERIC(3,1),
    company_response_status   VARCHAR(20) NOT NULL DEFAULT 'pending',
    published_at            TIMESTAMP,
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS content_reviews (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    deliverable_id  UUID NOT NULL REFERENCES deliverables(id) ON DELETE CASCADE,
    user_id         VARCHAR(100) NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    quality_score   SMALLINT CHECK (quality_score BETWEEN 1 AND 10),
    comments        TEXT,
    suggestions     TEXT,
    status          VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    submitted_at    TIMESTAMP,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (deliverable_id, user_id, status)
);

-- Chỉ 1 bản SUBMITTED / deliverable / user (partial unique index)
CREATE UNIQUE INDEX IF NOT EXISTS uq_content_reviews_submitted
    ON content_reviews(deliverable_id, user_id)
    WHERE status = 'SUBMITTED';

-- ========== Lịch công việc (theo ngày) ==========
CREATE TABLE IF NOT EXISTS schedule_tasks (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    subscription_id UUID NOT NULL REFERENCES user_subscriptions(id) ON DELETE CASCADE,
    task_date       DATE NOT NULL,
    title           VARCHAR(200) NOT NULL,
    scheduled_time  VARCHAR(10),
    sort_order      INT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_schedule_sub_date ON schedule_tasks(subscription_id, task_date);

-- ========== Thông báo ==========
CREATE TABLE IF NOT EXISTS notifications (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         VARCHAR(100) NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    type            VARCHAR(32) NOT NULL,
    title           VARCHAR(255) NOT NULL,
    body            TEXT,
    reference_type  VARCHAR(32),
    reference_id    VARCHAR(100),
    read_at         TIMESTAMP,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_notifications_user ON notifications(user_id, created_at DESC);

-- ========== Tài khoản / nâng cấp / voucher ==========
CREATE TABLE IF NOT EXISTS user_profiles (
    user_id         VARCHAR(100) PRIMARY KEY REFERENCES app_users(id) ON DELETE CASCADE,
    full_name       VARCHAR(150),
    phone           VARCHAR(30),
    email           VARCHAR(150),
    avatar_url      TEXT,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS package_upgrade_requests (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             VARCHAR(100) NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    from_package_code   VARCHAR(32) NOT NULL,
    to_package_code     VARCHAR(32) NOT NULL,
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    note                TEXT,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS voucher_templates (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code_prefix     VARCHAR(20),
    title           VARCHAR(150) NOT NULL,
    description     TEXT,
    valid_days      INT NOT NULL DEFAULT 30
);

CREATE TABLE IF NOT EXISTS user_vouchers (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         VARCHAR(100) NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    template_id     UUID REFERENCES voucher_templates(id),
    code            VARCHAR(50) NOT NULL,
    title           VARCHAR(150) NOT NULL,
    expires_at      TIMESTAMP NOT NULL,
    used_at         TIMESTAMP,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ========== Seed master (idempotent) ==========
INSERT INTO service_packages (code, tier, label, quota_posts, quota_images, quota_videos, sort_order) VALUES
    ('BASIC_15', 'BASIC', 'Gói Basic 15 bài', 15, 15, 0, 1),
    ('PRO_15',   'PRO',   'Gói Pro 15 bài',   15, 10, 5,  2),
    ('BASIC_30', 'BASIC', 'Gói Basic 30 bài', 30, 30, 0, 3),
    ('PRO_30',   'PRO',   'Gói Pro 30 bài',   30, 20, 10, 4)
ON CONFLICT (code) DO NOTHING;

INSERT INTO service_definitions (id, icon, name, description, tier_scope, sort_order) VALUES
    ('posts',   'edit',  'Viết bài đăng', 'Viết bài theo kế hoạch nội dung.', 'BASIC', 1),
    ('design',  'image', 'Thiết kế hình ảnh', 'Thiết kế hình ảnh cho bài đăng.', 'BASIC', 2),
    ('fanpage', 'doc',   'Chăm sóc Fanpage', 'Quản trị và duy trì fanpage.', 'PRO', 3),
    ('content', 'edit',  'Sáng tạo nội dung', 'Lên ý tưởng và nội dung đăng bài.', 'PRO', 4),
    ('ads',     'ads',   'Quảng cáo', 'Tối ưu chiến dịch Facebook Ads.', 'PRO', 5),
    ('report',  'chart', 'Báo cáo', 'Báo cáo hiệu suất định kỳ.', 'PRO', 6),
    ('cover',   'image', 'Ảnh bìa / Avatar', 'Thiết kế ảnh bìa và avatar.', 'PRO', 7),
    ('like',    'heart', 'Like / Follow', 'Tăng tương tác có kiểm soát.', 'PRO', 8)
ON CONFLICT (id) DO NOTHING;

INSERT INTO package_service_items (package_code, service_id)
SELECT 'BASIC_15', id FROM service_definitions WHERE id IN ('posts', 'design')
ON CONFLICT DO NOTHING;
INSERT INTO package_service_items (package_code, service_id)
SELECT 'BASIC_30', id FROM service_definitions WHERE id IN ('posts', 'design')
ON CONFLICT DO NOTHING;
INSERT INTO package_service_items (package_code, service_id)
SELECT 'PRO_15', id FROM service_definitions WHERE id IN ('fanpage','content','ads','report','cover','like')
ON CONFLICT DO NOTHING;
INSERT INTO package_service_items (package_code, service_id)
SELECT 'PRO_30', id FROM service_definitions WHERE id IN ('fanpage','content','ads','report','cover','like')
ON CONFLICT DO NOTHING;
