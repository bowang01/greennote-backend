-- GreenNote schema for the current phase.
-- Tables: users, channels, topics, notes, note topics, likes, collects, comments, comment likes.
-- Drops and recreates these tables. Back up first if they already contain data.
--
-- Run from the GreenNote-backend directory:
--   mysql -uroot -p --default-character-set=utf8mb4 < sql/mysql/greennote.sql

CREATE DATABASE IF NOT EXISTS greennote
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE greennote;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS gn_note_comment_like;
DROP TABLE IF EXISTS gn_note_comment;
DROP TABLE IF EXISTS gn_note_collect;
DROP TABLE IF EXISTS gn_note_like;
DROP TABLE IF EXISTS gn_note_topic;
DROP TABLE IF EXISTS gn_note;
DROP TABLE IF EXISTS gn_topic;
DROP TABLE IF EXISTS gn_channel;
DROP TABLE IF EXISTS gn_user;

SET FOREIGN_KEY_CHECKS = 1;

-- Users. Login still uses the admin account in application.yml. This table is for later registration and profiles.
CREATE TABLE gn_user
(
    id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    username   VARCHAR(64)     NOT NULL COMMENT 'login name',
    password   VARCHAR(100)    NOT NULL COMMENT 'BCrypt password',
    nickname   VARCHAR(64)     NOT NULL COMMENT 'display name',
    avatar     VARCHAR(512)    NULL COMMENT 'avatar URL',
    bio        VARCHAR(500)    NOT NULL DEFAULT '' COMMENT 'bio',
    status     TINYINT         NOT NULL DEFAULT 0 COMMENT '0 active, 1 disabled',
    created_at DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_username (username)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='user';

-- Discover channels such as fashion and food. The recommended feed is not a channel row.
CREATE TABLE gn_channel
(
    id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    name       VARCHAR(32)     NOT NULL COMMENT 'channel name',
    sort       INT             NOT NULL DEFAULT 0 COMMENT 'lower values appear first',
    status     TINYINT         NOT NULL DEFAULT 0 COMMENT '0 enabled, 1 disabled',
    created_at DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_channel_name (name)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='channel';

CREATE TABLE gn_topic
(
    id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    name       VARCHAR(64)     NOT NULL COMMENT 'topic name, without #',
    intro      VARCHAR(255)    NOT NULL DEFAULT '' COMMENT 'topic intro',
    sort       INT             NOT NULL DEFAULT 0 COMMENT 'lower values appear first',
    status     TINYINT         NOT NULL DEFAULT 0 COMMENT '0 enabled, 1 disabled',
    created_at DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_topic_name (name)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='topic';

CREATE TABLE gn_note
(
    id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id       BIGINT UNSIGNED NOT NULL COMMENT 'author',
    channel_id    BIGINT UNSIGNED NULL COMMENT 'channel, optional for drafts',
    type          TINYINT         NOT NULL COMMENT '1 image, 2 video',
    title         VARCHAR(128)    NOT NULL DEFAULT '' COMMENT 'title',
    content       MEDIUMTEXT      NULL COMMENT 'body',
    cover_url     VARCHAR(512)    NULL COMMENT 'cover URL',
    media_json    JSON            NULL COMMENT 'image URL list',
    video_url     VARCHAR(512)    NULL COMMENT 'video URL',
    place_name    VARCHAR(128)    NULL COMMENT 'place name, map picker comes later',
    city_name     VARCHAR(64)     NULL COMMENT 'city, used by the nearby feed',
    longitude     DECIMAL(10, 6)  NULL COMMENT 'longitude',
    latitude      DECIMAL(10, 6)  NULL COMMENT 'latitude',
    status        TINYINT         NOT NULL DEFAULT 0 COMMENT '0 draft, 1 pending, 2 published, 3 offline',
    reject_reason VARCHAR(255)    NULL COMMENT 'reason for rejection or taking offline',
    like_count    INT             NOT NULL DEFAULT 0,
    collect_count INT             NOT NULL DEFAULT 0,
    comment_count INT             NOT NULL DEFAULT 0,
    published_at  DATETIME        NULL COMMENT 'time of first or latest publish',
    created_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted       TINYINT         NOT NULL DEFAULT 0 COMMENT '0 present, 1 deleted',
    PRIMARY KEY (id),
    KEY idx_note_user (user_id, deleted, created_at),
    KEY idx_note_feed (status, deleted, channel_id, published_at),
    KEY idx_note_city (status, deleted, city_name, published_at),
    CONSTRAINT fk_note_user FOREIGN KEY (user_id) REFERENCES gn_user (id),
    CONSTRAINT fk_note_channel FOREIGN KEY (channel_id) REFERENCES gn_channel (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='note';

CREATE TABLE gn_note_topic
(
    id       BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    note_id  BIGINT UNSIGNED NOT NULL,
    topic_id BIGINT UNSIGNED NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_note_topic (note_id, topic_id),
    KEY idx_note_topic_topic (topic_id),
    CONSTRAINT fk_note_topic_note FOREIGN KEY (note_id) REFERENCES gn_note (id),
    CONSTRAINT fk_note_topic_topic FOREIGN KEY (topic_id) REFERENCES gn_topic (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='note topic link';

CREATE TABLE gn_note_like
(
    id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id    BIGINT UNSIGNED NOT NULL,
    note_id    BIGINT UNSIGNED NOT NULL,
    created_at DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_note_like (user_id, note_id),
    KEY idx_note_like_note (note_id),
    CONSTRAINT fk_note_like_user FOREIGN KEY (user_id) REFERENCES gn_user (id),
    CONSTRAINT fk_note_like_note FOREIGN KEY (note_id) REFERENCES gn_note (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='note like';

CREATE TABLE gn_note_collect
(
    id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id    BIGINT UNSIGNED NOT NULL,
    note_id    BIGINT UNSIGNED NOT NULL,
    created_at DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_note_collect (user_id, note_id),
    KEY idx_note_collect_note (note_id),
    CONSTRAINT fk_note_collect_user FOREIGN KEY (user_id) REFERENCES gn_user (id),
    CONSTRAINT fk_note_collect_note FOREIGN KEY (note_id) REFERENCES gn_note (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='note collect';

-- parent_id = 0 is a top-level comment. Replies store the parent comment id and the replied-to user.
CREATE TABLE gn_note_comment
(
    id               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    note_id          BIGINT UNSIGNED NOT NULL,
    user_id          BIGINT UNSIGNED NOT NULL,
    parent_id        BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '0 means top-level comment',
    reply_to_user_id BIGINT UNSIGNED NULL COMMENT 'user being replied to',
    content          VARCHAR(1000)   NOT NULL,
    like_count       INT             NOT NULL DEFAULT 0,
    created_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted          TINYINT         NOT NULL DEFAULT 0 COMMENT '0 present, 1 deleted',
    PRIMARY KEY (id),
    KEY idx_comment_note (note_id, parent_id, id),
    KEY idx_comment_user (user_id, created_at),
    CONSTRAINT fk_comment_note FOREIGN KEY (note_id) REFERENCES gn_note (id),
    CONSTRAINT fk_comment_user FOREIGN KEY (user_id) REFERENCES gn_user (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='note comment';

CREATE TABLE gn_note_comment_like
(
    id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id    BIGINT UNSIGNED NOT NULL,
    comment_id BIGINT UNSIGNED NOT NULL,
    created_at DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_comment_like (user_id, comment_id),
    KEY idx_comment_like_comment (comment_id),
    CONSTRAINT fk_comment_like_user FOREIGN KEY (user_id) REFERENCES gn_user (id),
    CONSTRAINT fk_comment_like_comment FOREIGN KEY (comment_id) REFERENCES gn_note_comment (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='comment like';

INSERT INTO gn_channel (name, sort, status)
VALUES ('Fashion', 1, 0),
       ('Food', 2, 0),
       ('Travel', 3, 0),
       ('Beauty', 4, 0),
       ('Home', 5, 0),
       ('Fitness', 6, 0);

INSERT INTO gn_topic (name, intro, sort, status)
VALUES ('Outfit of the Day', 'One outfit a day', 1, 0),
       ('Home Cooking', 'Dishes you can cook at home', 2, 0),
       ('Weekend Trip', 'Short getaways', 3, 0),
       ('Budget Finds', 'Good and affordable', 4, 0);
