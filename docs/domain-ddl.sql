-- ============================================================
-- License Registry — DDL for PostgreSQL 18
-- ============================================================

-- ------------------------------------------------------------
-- users
-- ------------------------------------------------------------
CREATE TABLE users
(
    id            UUID         NOT NULL DEFAULT uuidv7(),
    email         VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    last_name     VARCHAR(100) NOT NULL,
    first_name    VARCHAR(100) NOT NULL,
    middle_name   VARCHAR(100) NULL,
    global_role   VARCHAR(20)  NOT NULL DEFAULT 'USER',
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    --------------------------------------------------------
    CONSTRAINT pk_users
        PRIMARY KEY (id),
    CONSTRAINT uq_users_email
        UNIQUE (email),
    CONSTRAINT ck_users_global_role
        CHECK (global_role IN ('SUPERADMIN', 'USER'))
);

-- ------------------------------------------------------------
-- organizations
-- ------------------------------------------------------------
CREATE TABLE organizations
(
    id         UUID         NOT NULL DEFAULT uuidv7(),
    name       VARCHAR(255) NOT NULL,
    join_code  VARCHAR(16)  NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    --------------------------------------------------------
    CONSTRAINT pk_organizations
        PRIMARY KEY (id),
    CONSTRAINT uq_organizations_join_code
        UNIQUE (join_code)
);

-- ------------------------------------------------------------
-- organization_member
-- ------------------------------------------------------------
CREATE TABLE organization_member
(
    org_id      UUID        NOT NULL,
    user_id     UUID        NOT NULL,
    member_role VARCHAR(20) NOT NULL,
    joined_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    --------------------------------------------------------
    CONSTRAINT pk_organization_member
        PRIMARY KEY (org_id, user_id),
    CONSTRAINT fk_organization_member_org
        FOREIGN KEY (org_id)
            REFERENCES organizations (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_organization_member_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE,
    CONSTRAINT ck_organization_member_role
        CHECK (member_role IN ('ADMIN', 'MANAGER'))
);

-- ------------------------------------------------------------
-- license_types
-- ------------------------------------------------------------
CREATE TABLE license_types
(
    id   UUID         NOT NULL DEFAULT uuidv7(),
    code VARCHAR(50)  NOT NULL,
    name VARCHAR(255) NOT NULL,
    --------------------------------------------------------
    CONSTRAINT pk_license_types
        PRIMARY KEY (id),
    CONSTRAINT uq_license_types_code
        UNIQUE (code)
);

-- ------------------------------------------------------------
-- licenses
-- ------------------------------------------------------------
CREATE TABLE licenses
(
    id         UUID         NOT NULL DEFAULT uuidv7(),
    org_id     UUID         NOT NULL,
    title      VARCHAR(255) NOT NULL,
    issued_at  DATE         NOT NULL,
    expires_at DATE         NOT NULL,
    status     VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    notes      TEXT         NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    --------------------------------------------------------
    CONSTRAINT pk_licenses
        PRIMARY KEY (id),
    CONSTRAINT fk_licenses_org
        FOREIGN KEY (org_id)
            REFERENCES organizations (id)
            ON DELETE CASCADE,
    CONSTRAINT ck_licenses_status
        CHECK (status IN ('ACTIVE', 'EXPIRING', 'EXPIRED', 'CANCELLED')),
    CONSTRAINT ck_licenses_dates
        CHECK (issued_at <= expires_at)
);

CREATE INDEX idx_licenses_org_id ON licenses (org_id);
CREATE INDEX idx_licenses_expires_at ON licenses (expires_at);
CREATE INDEX idx_licenses_status ON licenses (status);

-- ------------------------------------------------------------
-- license_type_link
-- ------------------------------------------------------------
CREATE TABLE license_type_link
(
    license_id UUID NOT NULL,
    type_id    UUID NOT NULL,
    --------------------------------------------------------
    CONSTRAINT pk_license_type_link
        PRIMARY KEY (license_id, type_id),
    CONSTRAINT fk_license_type_link_license
        FOREIGN KEY (license_id)
            REFERENCES licenses (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_license_type_link_type
        FOREIGN KEY (type_id)
            REFERENCES license_types (id)
            ON DELETE RESTRICT
);

-- ------------------------------------------------------------
-- tags
-- ------------------------------------------------------------
CREATE TABLE tags
(
    id     UUID        NOT NULL DEFAULT uuidv7(),
    org_id UUID        NOT NULL,
    name   VARCHAR(50) NOT NULL,
    --------------------------------------------------------
    CONSTRAINT pk_tags
        PRIMARY KEY (id),
    CONSTRAINT fk_tags_org
        FOREIGN KEY (org_id)
            REFERENCES organizations (id)
            ON DELETE CASCADE,
    CONSTRAINT uq_tags_org_name
        UNIQUE (org_id, name)
);

-- ------------------------------------------------------------
-- license_tag
-- ------------------------------------------------------------
CREATE TABLE license_tag
(
    license_id UUID NOT NULL,
    tag_id     UUID NOT NULL,
    --------------------------------------------------------
    CONSTRAINT pk_license_tag
        PRIMARY KEY (license_id, tag_id),
    CONSTRAINT fk_license_tag_license
        FOREIGN KEY (license_id)
            REFERENCES licenses (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_license_tag_tag
        FOREIGN KEY (tag_id)
            REFERENCES tags (id)
            ON DELETE CASCADE
);
