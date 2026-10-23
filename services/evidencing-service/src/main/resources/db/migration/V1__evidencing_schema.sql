-- Evidencing Module Schema

CREATE TABLE role_permissions (
    id UUID PRIMARY KEY,
    role VARCHAR(50) NOT NULL,
    permission VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_role_permission UNIQUE (role, permission)
);

CREATE TABLE schools (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE branches (
    id UUID PRIMARY KEY,
    school_id UUID NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE classes (
    id UUID PRIMARY KEY,
    branch_id UUID NOT NULL REFERENCES branches(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    grade_level VARCHAR(50),
    section VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_scopes (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    avatar_url VARCHAR(500),
    school_id UUID NOT NULL REFERENCES schools(id),
    branch_id UUID NOT NULL REFERENCES branches(id),
    class_id UUID REFERENCES classes(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_scopes_user_id ON user_scopes(user_id);
CREATE INDEX idx_user_scopes_branch_id ON user_scopes(branch_id);

CREATE TABLE post_categories (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    icon VARCHAR(100),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    display_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE media_assets (
    id UUID PRIMARY KEY,
    uploader_id UUID NOT NULL,
    media_type VARCHAR(20) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    file_size_bytes BIGINT NOT NULL,
    storage_key VARCHAR(500) NOT NULL,
    thumbnail_storage_key VARCHAR(500),
    width INT,
    height INT,
    duration_seconds DOUBLE PRECISION,
    status VARCHAR(30) NOT NULL,
    error_message TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_media_assets_uploader ON media_assets(uploader_id);
CREATE INDEX idx_media_assets_status ON media_assets(status);

CREATE TABLE posts (
    id UUID PRIMARY KEY,
    author_id UUID NOT NULL,
    school_id UUID NOT NULL REFERENCES schools(id),
    branch_id UUID NOT NULL REFERENCES branches(id),
    class_id UUID REFERENCES classes(id),
    category_id UUID NOT NULL REFERENCES post_categories(id),
    caption TEXT,
    visibility VARCHAR(20) NOT NULL,
    publication_status VARCHAR(30) NOT NULL,
    idempotency_key VARCHAR(100) UNIQUE,
    published_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_posts_school_branch ON posts(school_id, branch_id);
CREATE INDEX idx_posts_author ON posts(author_id);
CREATE INDEX idx_posts_visibility ON posts(visibility);
CREATE INDEX idx_posts_publication_status ON posts(publication_status);
CREATE INDEX idx_posts_category ON posts(category_id);
CREATE INDEX idx_posts_published_at ON posts(published_at);

CREATE TABLE post_media (
    id UUID PRIMARY KEY,
    post_id UUID NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    media_asset_id UUID NOT NULL REFERENCES media_assets(id) ON DELETE CASCADE,
    display_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_post_media UNIQUE (post_id, media_asset_id)
);

CREATE INDEX idx_post_media_post ON post_media(post_id);

CREATE TABLE post_recipients (
    id UUID PRIMARY KEY,
    post_id UUID NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    user_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_post_recipient UNIQUE (post_id, user_id)
);

CREATE INDEX idx_post_recipients_user ON post_recipients(user_id);
CREATE INDEX idx_post_recipients_post ON post_recipients(post_id);

CREATE TABLE post_likes (
    id UUID PRIMARY KEY,
    post_id UUID NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    user_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_post_like UNIQUE (post_id, user_id)
);

CREATE INDEX idx_post_likes_post ON post_likes(post_id);

CREATE TABLE post_comments (
    id UUID PRIMARY KEY,
    post_id UUID NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    user_id UUID NOT NULL,
    comment_text TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_post_comments_post ON post_comments(post_id);

CREATE TABLE post_views (
    id UUID PRIMARY KEY,
    post_id UUID NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    user_id UUID NOT NULL,
    viewed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_post_views_post ON post_views(post_id);

CREATE TABLE evidencing_notifications (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    post_id UUID NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    creator_id UUID NOT NULL,
    creator_name VARCHAR(255) NOT NULL,
    message VARCHAR(500) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    link VARCHAR(500) NOT NULL,
    idempotency_key VARCHAR(255) UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_notifications_user_read ON evidencing_notifications(user_id, is_read);

CREATE TABLE publication_dlq_entries (
    id UUID PRIMARY KEY,
    queue_name VARCHAR(100) NOT NULL,
    payload TEXT NOT NULL,
    error_reason TEXT NOT NULL,
    retry_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
