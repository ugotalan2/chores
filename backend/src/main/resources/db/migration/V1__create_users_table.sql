-- V1__create_users_table.sql
-- Users table — stores both parent and kid profiles
-- See DATA_MODEL.md for full schema documentation

CREATE TABLE users (
                       user_id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       display_name     VARCHAR(100) NOT NULL,
                       role             VARCHAR(20)  NOT NULL
                           CHECK (role IN ('PARENT', 'CHILD')),
                       avatar_url       VARCHAR(500),
                       is_active        BOOLEAN NOT NULL DEFAULT TRUE,

    -- CHILD only (nullable for parents)
                       age                     INTEGER,
                       assigned_bathroom_id    UUID,
                       laundry_day_of_week     INTEGER
                           CHECK (laundry_day_of_week BETWEEN 0 AND 6),

    -- PARENT only (nullable for children)
                       email            VARCHAR(255) UNIQUE,
                       clerk_user_id    VARCHAR(255) UNIQUE,
                       fcm_token        VARCHAR(500),
                       is_primary_admin BOOLEAN DEFAULT FALSE,

                       created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
                       updated_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
                       created_by       UUID
);

CREATE INDEX idx_users_role
    ON users(role);

CREATE INDEX idx_users_clerk_id
    ON users(clerk_user_id);

CREATE INDEX idx_users_is_active
    ON users(is_active);