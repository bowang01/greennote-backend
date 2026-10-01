CREATE TABLE IF NOT EXISTS gn_dept (
    id CHAR(36) NOT NULL COMMENT 'Primary key',
    name VARCHAR(64) NOT NULL COMMENT 'Department name',
    parent_id CHAR(36) NULL COMMENT 'Parent department id, null for a root department',
    sort_no INT NOT NULL DEFAULT 0 COMMENT 'Display order, smaller first',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0 enabled, 1 disabled',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Updated time',
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS gn_admin_user (
    id CHAR(36) NOT NULL COMMENT 'Primary key',
    dept_id CHAR(36) NULL COMMENT 'Department id',
    username VARCHAR(64) NOT NULL COMMENT 'Login name',
    password VARCHAR(100) NOT NULL COMMENT 'BCrypt password',
    nickname VARCHAR(64) NOT NULL COMMENT 'Display name',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0 enabled, 1 disabled',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Updated time',
    PRIMARY KEY (id),
    CONSTRAINT uk_admin_username UNIQUE (username)
);

CREATE TABLE IF NOT EXISTS gn_role (
    id CHAR(36) NOT NULL COMMENT 'Primary key',
    name VARCHAR(64) NOT NULL COMMENT 'Role name',
    code VARCHAR(64) NOT NULL COMMENT 'Role code',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0 enabled, 1 disabled',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Updated time',
    PRIMARY KEY (id),
    CONSTRAINT uk_role_code UNIQUE (code)
);

CREATE TABLE IF NOT EXISTS gn_admin_user_role (
    admin_user_id CHAR(36) NOT NULL COMMENT 'Admin user id',
    role_id CHAR(36) NOT NULL COMMENT 'Role id',
    PRIMARY KEY (admin_user_id, role_id)
);

CREATE TABLE IF NOT EXISTS gn_menu (
    id CHAR(36) NOT NULL COMMENT 'Primary key',
    parent_id CHAR(36) NULL COMMENT 'Parent menu id, null for a root menu',
    name VARCHAR(64) NOT NULL COMMENT 'Menu label',
    path VARCHAR(128) NOT NULL COMMENT 'Frontend route',
    sort_no INT NOT NULL DEFAULT 0 COMMENT 'Display order, smaller first',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0 enabled, 1 disabled',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Updated time',
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS gn_role_menu (
    role_id CHAR(36) NOT NULL COMMENT 'Role id',
    menu_id CHAR(36) NOT NULL COMMENT 'Menu id',
    PRIMARY KEY (role_id, menu_id)
);

CREATE TABLE IF NOT EXISTS gn_dict_type (
    id CHAR(36) NOT NULL COMMENT 'Primary key',
    name VARCHAR(64) NOT NULL COMMENT 'Dictionary name',
    dict_type VARCHAR(64) NOT NULL COMMENT 'Dictionary code',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0 enabled, 1 disabled',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Updated time',
    PRIMARY KEY (id),
    CONSTRAINT uk_dict_type UNIQUE (dict_type)
);

CREATE TABLE IF NOT EXISTS gn_dict_data (
    id CHAR(36) NOT NULL COMMENT 'Primary key',
    dict_type VARCHAR(64) NOT NULL COMMENT 'Dictionary code',
    label VARCHAR(64) NOT NULL COMMENT 'Display label',
    dict_value VARCHAR(64) NOT NULL COMMENT 'Stored value',
    sort_no INT NOT NULL DEFAULT 0 COMMENT 'Display order, smaller first',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0 enabled, 1 disabled',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Updated time',
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS gn_file (
    id CHAR(36) NOT NULL COMMENT 'Primary key',
    original_name VARCHAR(255) NOT NULL COMMENT 'Original file name',
    stored_name VARCHAR(255) NOT NULL COMMENT 'Stored file name',
    url VARCHAR(512) NOT NULL COMMENT 'Public file URL',
    size_bytes BIGINT NOT NULL COMMENT 'File size in bytes',
    content_type VARCHAR(128) NOT NULL COMMENT 'MIME type',
    uploader_kind VARCHAR(16) NOT NULL COMMENT 'admin or member',
    uploader_id CHAR(36) NOT NULL COMMENT 'Uploader user id',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS gn_config (
    id CHAR(36) NOT NULL COMMENT 'Primary key',
    config_key VARCHAR(64) NOT NULL COMMENT 'Config key',
    config_value VARCHAR(1000) NOT NULL COMMENT 'Config value',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Updated time',
    PRIMARY KEY (id),
    CONSTRAINT uk_config_key UNIQUE (config_key)
);

CREATE TABLE IF NOT EXISTS gn_operate_log (
    id CHAR(36) NOT NULL COMMENT 'Primary key',
    operator_id CHAR(36) NULL COMMENT 'Operator user id',
    operator_name VARCHAR(64) NOT NULL COMMENT 'Operator name',
    action VARCHAR(64) NOT NULL COMMENT 'Action code',
    detail VARCHAR(500) NOT NULL COMMENT 'Action detail',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS gn_user (
    id CHAR(36) NOT NULL COMMENT 'Primary key',
    username VARCHAR(64) NOT NULL COMMENT 'Login name',
    password VARCHAR(100) NOT NULL COMMENT 'BCrypt password',
    nickname VARCHAR(64) NOT NULL COMMENT 'Display name',
    avatar VARCHAR(512) NULL COMMENT 'Avatar URL',
    bio VARCHAR(500) NOT NULL DEFAULT '' COMMENT 'Profile bio',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0 enabled, 1 disabled',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Updated time',
    PRIMARY KEY (id),
    CONSTRAINT uk_user_username UNIQUE (username)
);

CREATE TABLE IF NOT EXISTS gn_channel (
    id CHAR(36) NOT NULL COMMENT 'Primary key',
    name VARCHAR(32) NOT NULL COMMENT 'Channel name',
    sort_no INT NOT NULL DEFAULT 0 COMMENT 'Display order, smaller first',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0 enabled, 1 disabled',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Updated time',
    PRIMARY KEY (id),
    CONSTRAINT uk_channel_name UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS gn_topic (
    id CHAR(36) NOT NULL COMMENT 'Primary key',
    name VARCHAR(64) NOT NULL COMMENT 'Topic name',
    intro VARCHAR(255) NOT NULL DEFAULT '' COMMENT 'Short introduction',
    sort_no INT NOT NULL DEFAULT 0 COMMENT 'Display order, smaller first',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0 enabled, 1 disabled',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Updated time',
    PRIMARY KEY (id),
    CONSTRAINT uk_topic_name UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS gn_note (
    id CHAR(36) NOT NULL COMMENT 'Primary key',
    user_id CHAR(36) NOT NULL COMMENT 'Author member id',
    channel_id CHAR(36) NULL COMMENT 'Channel id',
    type TINYINT NOT NULL COMMENT '1 image, 2 video',
    title VARCHAR(128) NOT NULL DEFAULT '' COMMENT 'Note title',
    content LONGTEXT NULL COMMENT 'Note body',
    cover_url VARCHAR(512) NULL COMMENT 'Cover image URL',
    media_json VARCHAR(4000) NULL COMMENT 'JSON array of image URLs',
    video_url VARCHAR(512) NULL COMMENT 'Video URL',
    place_name VARCHAR(128) NULL COMMENT 'Place name',
    city_name VARCHAR(64) NULL COMMENT 'City name',
    longitude DECIMAL(10, 6) NULL COMMENT 'Longitude',
    latitude DECIMAL(10, 6) NULL COMMENT 'Latitude',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0 draft, 1 pending, 2 published, 3 offline',
    reject_reason VARCHAR(255) NULL COMMENT 'Reject or offline reason',
    like_count INT NOT NULL DEFAULT 0 COMMENT 'Like count',
    collect_count INT NOT NULL DEFAULT 0 COMMENT 'Collect count',
    comment_count INT NOT NULL DEFAULT 0 COMMENT 'Comment count',
    published_at TIMESTAMP NULL COMMENT 'Published time',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Updated time',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '0 kept, 1 deleted',
    PRIMARY KEY (id),
    INDEX idx_note_user (user_id, deleted, created_at),
    INDEX idx_note_feed (status, deleted, channel_id, published_at)
);

CREATE TABLE IF NOT EXISTS gn_note_topic (
    id CHAR(36) NOT NULL COMMENT 'Primary key',
    note_id CHAR(36) NOT NULL COMMENT 'Note id',
    topic_id CHAR(36) NOT NULL COMMENT 'Topic id',
    PRIMARY KEY (id),
    CONSTRAINT uk_note_topic UNIQUE (note_id, topic_id)
);

CREATE TABLE IF NOT EXISTS gn_note_like (
    id CHAR(36) NOT NULL COMMENT 'Primary key',
    user_id CHAR(36) NOT NULL COMMENT 'Member id',
    note_id CHAR(36) NOT NULL COMMENT 'Note id',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    PRIMARY KEY (id),
    CONSTRAINT uk_note_like UNIQUE (user_id, note_id)
);

CREATE TABLE IF NOT EXISTS gn_note_collect (
    id CHAR(36) NOT NULL COMMENT 'Primary key',
    user_id CHAR(36) NOT NULL COMMENT 'Member id',
    note_id CHAR(36) NOT NULL COMMENT 'Note id',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    PRIMARY KEY (id),
    CONSTRAINT uk_note_collect UNIQUE (user_id, note_id)
);

CREATE TABLE IF NOT EXISTS gn_note_comment (
    id CHAR(36) NOT NULL COMMENT 'Primary key',
    note_id CHAR(36) NOT NULL COMMENT 'Note id',
    user_id CHAR(36) NOT NULL COMMENT 'Author member id',
    parent_id CHAR(36) NULL COMMENT 'Parent comment id, null for a top-level comment',
    reply_to_user_id CHAR(36) NULL COMMENT 'Member id being replied to',
    content VARCHAR(1000) NOT NULL COMMENT 'Comment text',
    like_count INT NOT NULL DEFAULT 0 COMMENT 'Like count',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Updated time',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '0 kept, 1 deleted',
    PRIMARY KEY (id),
    INDEX idx_comment_note (note_id, parent_id, id)
);

CREATE TABLE IF NOT EXISTS gn_note_comment_like (
    id CHAR(36) NOT NULL COMMENT 'Primary key',
    user_id CHAR(36) NOT NULL COMMENT 'Member id',
    comment_id CHAR(36) NOT NULL COMMENT 'Comment id',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    PRIMARY KEY (id),
    CONSTRAINT uk_comment_like UNIQUE (user_id, comment_id)
);
