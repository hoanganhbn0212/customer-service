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
    sort_order      INT NOT NULL DEFAULT 0,
    progress_mode   VARCHAR(16) NOT NULL DEFAULT 'STATUS', -- QUANTITY | STATUS
    quota_key       VARCHAR(16)                          -- POSTS | IMAGES | VIDEOS (for QUANTITY)
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
    deployment_status VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
    start_date      DATE NOT NULL,
    end_date        DATE NOT NULL,
    display_title   VARCHAR(200),
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_user_subscriptions_user ON user_subscriptions(user_id);
CREATE INDEX IF NOT EXISTS idx_user_subscriptions_active ON user_subscriptions(user_id, status);
ALTER TABLE user_subscriptions
    ADD COLUMN IF NOT EXISTS deployment_status VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS';

CREATE TABLE IF NOT EXISTS subscription_progress (
    subscription_id UUID PRIMARY KEY REFERENCES user_subscriptions(id) ON DELETE CASCADE,
    completed_posts   INT NOT NULL DEFAULT 0,
    completed_images  INT NOT NULL DEFAULT 0,
    completed_videos  INT NOT NULL DEFAULT 0,
    updated_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS subscription_service_progress (
    subscription_id UUID NOT NULL REFERENCES user_subscriptions(id) ON DELETE CASCADE,
    service_id      VARCHAR(32) NOT NULL REFERENCES service_definitions(id) ON DELETE CASCADE,
    percent         INT NOT NULL DEFAULT 0,
    completed_count INT,
    target_count    INT,
    PRIMARY KEY (subscription_id, service_id)
);
ALTER TABLE subscription_service_progress
    ADD COLUMN IF NOT EXISTS completed_count INT;
ALTER TABLE subscription_service_progress
    ADD COLUMN IF NOT EXISTS target_count INT;

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
    planned_publish_date    DATE,
    topic                   VARCHAR(240),
    idea_frame              TEXT,
    post_content            TEXT,
    content_status          VARCHAR(32),
    attachment_url          TEXT,
    completed_on            DATE,
    media_name              VARCHAR(240),
    media_type              VARCHAR(16),
    design_customer_comment TEXT,
    design_improvement_suggestion TEXT,
    thumbnail_url           TEXT,
    preview_url             TEXT,
    team_content_score      NUMERIC(3,1),
    team_design_score       NUMERIC(3,1),
    company_response_status   VARCHAR(20) NOT NULL DEFAULT 'pending',
    published_at            TIMESTAMP,
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE deliverables ADD COLUMN IF NOT EXISTS planned_publish_date DATE;
ALTER TABLE deliverables ADD COLUMN IF NOT EXISTS topic VARCHAR(240);
ALTER TABLE deliverables ADD COLUMN IF NOT EXISTS idea_frame TEXT;
ALTER TABLE deliverables ADD COLUMN IF NOT EXISTS post_content TEXT;
ALTER TABLE deliverables ADD COLUMN IF NOT EXISTS content_status VARCHAR(32);
ALTER TABLE deliverables ADD COLUMN IF NOT EXISTS attachment_url TEXT;
ALTER TABLE deliverables ADD COLUMN IF NOT EXISTS completed_on DATE;
ALTER TABLE deliverables ADD COLUMN IF NOT EXISTS media_name VARCHAR(240);
ALTER TABLE deliverables ADD COLUMN IF NOT EXISTS media_type VARCHAR(16);
ALTER TABLE deliverables ADD COLUMN IF NOT EXISTS design_customer_comment TEXT;
ALTER TABLE deliverables ADD COLUMN IF NOT EXISTS design_improvement_suggestion TEXT;

CREATE TABLE IF NOT EXISTS content_reviews (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    deliverable_id  UUID NOT NULL REFERENCES deliverables(id) ON DELETE CASCADE,
    user_id         VARCHAR(100) NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    review_type     VARCHAR(20) NOT NULL DEFAULT 'CONTENT',
    quality_score   SMALLINT CHECK (quality_score BETWEEN 1 AND 10),
    comments        TEXT,
    suggestions     TEXT,
    status          VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    submitted_at    TIMESTAMP,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (deliverable_id, user_id, review_type, status)
);

ALTER TABLE content_reviews ADD COLUMN IF NOT EXISTS review_type VARCHAR(20) NOT NULL DEFAULT 'CONTENT';

-- Chỉ 1 bản SUBMITTED / deliverable / user (partial unique index)
CREATE UNIQUE INDEX IF NOT EXISTS uq_content_reviews_submitted
    ON content_reviews(deliverable_id, user_id, review_type)
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
    ('BASIC_15', 'BASIC', 'Gói 15 cơ bản', 15, 15, 0, 1),
    ('PRO_15',   'PRO',   'Gói 15 cao cấp', 15, 10, 5,  2),
    ('BASIC_30', 'BASIC', 'Gói 30 cơ bản', 30, 30, 0, 3),
    ('PRO_30',   'PRO',   'Gói 30 cao cấp', 30, 20, 10, 4)
ON CONFLICT (code) DO NOTHING;

INSERT INTO service_definitions (id, icon, name, description, tier_scope, sort_order, progress_mode, quota_key) VALUES
    ('posts',   'edit',  'Viết bài', 'Viết bài theo kế hoạch nội dung.', 'BASIC', 1, 'QUANTITY', 'POSTS'),
    ('design',  'image', 'Thiết kế hình ảnh', 'Thiết kế hình ảnh cho bài đăng.', 'BASIC', 2, 'QUANTITY', 'IMAGES'),
    ('video',   'video', 'Edit video', 'Chỉnh sửa video ngắn cho fanpage.', 'PRO', 3, 'QUANTITY', 'VIDEOS'),
    ('fanpage', 'doc',   'Quản trị Fanpage', 'Quản trị và duy trì fanpage.', 'PRO', 4, 'STATUS', NULL),
    ('content', 'edit',  'Sáng tạo nội dung', 'Lên ý tưởng và nội dung đăng bài.', 'PRO', 5, 'STATUS', NULL),
    ('ads',     'ads',   'Báo cáo chạy ads', 'Tối ưu và báo cáo chiến dịch Facebook Ads.', 'PRO', 6, 'STATUS', NULL),
    ('report',  'chart', 'Báo cáo hiệu suất', 'Báo cáo hiệu suất định kỳ.', 'PRO', 7, 'STATUS', NULL),
    ('cover',   'image', 'Ảnh bìa / Avatar', 'Thiết kế ảnh bìa và avatar.', 'PRO', 8, 'STATUS', NULL),
    ('like',    'heart', 'Like / Follow', 'Tăng tương tác có kiểm soát.', 'PRO', 9, 'STATUS', NULL)
ON CONFLICT (id) DO NOTHING;

INSERT INTO package_service_items (package_code, service_id)
SELECT 'BASIC_15', id FROM service_definitions WHERE id IN ('posts', 'design')
ON CONFLICT DO NOTHING;
INSERT INTO package_service_items (package_code, service_id)
SELECT 'BASIC_30', id FROM service_definitions WHERE id IN ('posts', 'design')
ON CONFLICT DO NOTHING;
INSERT INTO package_service_items (package_code, service_id)
SELECT 'PRO_15', id FROM service_definitions WHERE id IN ('fanpage','ads','report','cover','like')
ON CONFLICT DO NOTHING;
INSERT INTO package_service_items (package_code, service_id)
SELECT 'PRO_30', id FROM service_definitions WHERE id IN ('fanpage','ads','report','cover','like')
ON CONFLICT DO NOTHING;
INSERT INTO package_service_items (package_code, service_id)
SELECT 'PRO_15', id FROM service_definitions WHERE id IN ('posts','design','video')
ON CONFLICT DO NOTHING;
INSERT INTO package_service_items (package_code, service_id)
SELECT 'PRO_30', id FROM service_definitions WHERE id IN ('posts','design','video')
ON CONFLICT DO NOTHING;
DELETE FROM package_service_items
WHERE package_code IN ('PRO_15', 'PRO_30') AND service_id = 'content';
