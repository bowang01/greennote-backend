-- GreenNote system and infrastructure tables.
-- Tables: department, admin user, role, menu, dictionary, file, config, operation log, member user.
-- Seeds the admin account admin / admin123.
-- Run from GreenNote-backend:
--   mysql -uroot -p --default-character-set=utf8mb4 < sql/mysql/system.sql

CREATE DATABASE IF NOT EXISTS greennote
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE greennote;

CREATE TABLE IF NOT EXISTS gn_dept (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(64) NOT NULL,
    parent_id BIGINT NOT NULL DEFAULT 0,
    sort_no INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS gn_admin_user (
    id BIGINT NOT NULL AUTO_INCREMENT,
    dept_id BIGINT NULL,
    username VARCHAR(64) NOT NULL,
    password VARCHAR(100) NOT NULL,
    nickname VARCHAR(64) NOT NULL,
    status TINYINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_admin_username UNIQUE (username)
);

CREATE TABLE IF NOT EXISTS gn_role (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(64) NOT NULL,
    code VARCHAR(64) NOT NULL,
    status TINYINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_role_code UNIQUE (code)
);

CREATE TABLE IF NOT EXISTS gn_admin_user_role (
    admin_user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (admin_user_id, role_id)
);

CREATE TABLE IF NOT EXISTS gn_menu (
    id BIGINT NOT NULL AUTO_INCREMENT,
    parent_id BIGINT NOT NULL DEFAULT 0,
    name VARCHAR(64) NOT NULL,
    path VARCHAR(128) NOT NULL,
    sort_no INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS gn_role_menu (
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, menu_id)
);

CREATE TABLE IF NOT EXISTS gn_dict_type (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(64) NOT NULL,
    dict_type VARCHAR(64) NOT NULL,
    status TINYINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_dict_type UNIQUE (dict_type)
);

CREATE TABLE IF NOT EXISTS gn_dict_data (
    id BIGINT NOT NULL AUTO_INCREMENT,
    dict_type VARCHAR(64) NOT NULL,
    label VARCHAR(64) NOT NULL,
    dict_value VARCHAR(64) NOT NULL,
    sort_no INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS gn_file (
    id BIGINT NOT NULL AUTO_INCREMENT,
    original_name VARCHAR(255) NOT NULL,
    stored_name VARCHAR(255) NOT NULL,
    url VARCHAR(512) NOT NULL,
    size_bytes BIGINT NOT NULL,
    content_type VARCHAR(128) NOT NULL,
    uploader_kind VARCHAR(16) NOT NULL,
    uploader_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS gn_config (
    id BIGINT NOT NULL AUTO_INCREMENT,
    config_key VARCHAR(64) NOT NULL,
    config_value VARCHAR(1000) NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_config_key UNIQUE (config_key)
);

CREATE TABLE IF NOT EXISTS gn_operate_log (
    id BIGINT NOT NULL AUTO_INCREMENT,
    operator_id BIGINT NULL,
    operator_name VARCHAR(64) NOT NULL,
    action VARCHAR(64) NOT NULL,
    detail VARCHAR(500) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS gn_user (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL,
    password VARCHAR(100) NOT NULL,
    nickname VARCHAR(64) NOT NULL,
    avatar VARCHAR(512) NULL,
    bio VARCHAR(500) NOT NULL DEFAULT '',
    status TINYINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_user_username UNIQUE (username)
);


INSERT INTO gn_dept (name, parent_id, sort_no, status)
SELECT 'Headquarters', 0, 0, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM gn_dept WHERE name = 'Headquarters');

INSERT INTO gn_role (name, code, status)
SELECT 'Super Admin', 'super_admin', 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM gn_role WHERE code = 'super_admin');

INSERT INTO gn_admin_user (dept_id, username, password, nickname, status)
SELECT d.id, 'admin', '$2a$10$jXEjVvEB0bHOwysSYhIK9unVOItbPrvEhQilA5oapdd/2Ewz4yJHG', 'Administrator', 0
FROM gn_dept d
WHERE d.name = 'Headquarters'
  AND NOT EXISTS (SELECT 1 FROM gn_admin_user WHERE username = 'admin');

INSERT INTO gn_admin_user_role (admin_user_id, role_id)
SELECT u.id, r.id
FROM gn_admin_user u
JOIN gn_role r ON r.code = 'super_admin'
WHERE u.username = 'admin'
  AND NOT EXISTS (
      SELECT 1 FROM gn_admin_user_role ur WHERE ur.admin_user_id = u.id AND ur.role_id = r.id
  );

INSERT INTO gn_menu (parent_id, name, path, sort_no, status)
SELECT 0, 'Home', '/', 1, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM gn_menu WHERE path = '/');

INSERT INTO gn_menu (parent_id, name, path, sort_no, status)
SELECT 0, 'Members', '/members', 2, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM gn_menu WHERE path = '/members');

INSERT INTO gn_menu (parent_id, name, path, sort_no, status)
SELECT 0, 'Site', '/site', 3, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM gn_menu WHERE path = '/site');

INSERT INTO gn_menu (parent_id, name, path, sort_no, status)
SELECT 0, 'Departments', '/departments', 4, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM gn_menu WHERE path = '/departments');

INSERT INTO gn_menu (parent_id, name, path, sort_no, status)
SELECT 0, 'Dictionaries', '/dictionaries', 5, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM gn_menu WHERE path = '/dictionaries');

INSERT INTO gn_menu (parent_id, name, path, sort_no, status)
SELECT 0, 'Files', '/files', 6, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM gn_menu WHERE path = '/files');

INSERT INTO gn_menu (parent_id, name, path, sort_no, status)
SELECT 0, 'Operation logs', '/logs', 7, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM gn_menu WHERE path = '/logs');

INSERT INTO gn_role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM gn_role r
JOIN gn_menu m ON 1 = 1
WHERE r.code = 'super_admin'
  AND NOT EXISTS (
      SELECT 1 FROM gn_role_menu rm WHERE rm.role_id = r.id AND rm.menu_id = m.id
  );

INSERT INTO gn_dict_type (name, dict_type, status)
SELECT 'User status', 'user_status', 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM gn_dict_type WHERE dict_type = 'user_status');

INSERT INTO gn_dict_data (dict_type, label, dict_value, sort_no, status)
SELECT 'user_status', 'Active', '0', 1, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM gn_dict_data WHERE dict_type = 'user_status' AND dict_value = '0');

INSERT INTO gn_dict_data (dict_type, label, dict_value, sort_no, status)
SELECT 'user_status', 'Disabled', '1', 2, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM gn_dict_data WHERE dict_type = 'user_status' AND dict_value = '1');

INSERT INTO gn_config (config_key, config_value)
SELECT 'site.name', 'GreenNote' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM gn_config WHERE config_key = 'site.name');

INSERT INTO gn_config (config_key, config_value)
SELECT 'site.logo', '' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM gn_config WHERE config_key = 'site.logo');

INSERT INTO gn_config (config_key, config_value)
SELECT 'site.theme', '#1b6b45' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM gn_config WHERE config_key = 'site.theme');
