-- ============================================================
-- Plastic Waste Audit & Reduction Tracker
-- MySQL Schema — 5 Normalised Tables with FK Constraints
-- SDG 12 (Responsible Consumption) | SDG 14 (Life Below Water)
-- ============================================================

-- Table 1: roles
CREATE TABLE IF NOT EXISTS roles (
    id   BIGINT       NOT NULL AUTO_INCREMENT,
    name VARCHAR(50)  NOT NULL UNIQUE,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table 2: industries
CREATE TABLE IF NOT EXISTS industries (
    id                        BIGINT        NOT NULL AUTO_INCREMENT,
    name                      VARCHAR(150)  NOT NULL,
    sector                    VARCHAR(100),
    location                  VARCHAR(200),
    registration_no           VARCHAR(60)   UNIQUE,
    contact_email             VARCHAR(100),
    contact_phone             VARCHAR(20),
    annual_plastic_target_kg  DOUBLE,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table 3: users
CREATE TABLE IF NOT EXISTS users (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    username    VARCHAR(60)  NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    email       VARCHAR(100) NOT NULL UNIQUE,
    full_name   VARCHAR(100),
    enabled     TINYINT(1)   NOT NULL DEFAULT 1,
    industry_id BIGINT       NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_users_industry FOREIGN KEY (industry_id)
        REFERENCES industries (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table 4: user_roles (join table — @ManyToMany)
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_ur_user FOREIGN KEY (user_id) REFERENCES users     (id) ON DELETE CASCADE,
    CONSTRAINT fk_ur_role FOREIGN KEY (role_id) REFERENCES roles     (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table 5: waste_entries
CREATE TABLE IF NOT EXISTS waste_entries (
    id                    BIGINT       NOT NULL AUTO_INCREMENT,
    industry_id           BIGINT       NOT NULL,
    entry_date            DATE         NOT NULL,
    plastic_generated_kg  DOUBLE       NOT NULL DEFAULT 0,
    plastic_recycled_kg   DOUBLE       NOT NULL DEFAULT 0,
    plastic_eliminated_kg DOUBLE       NOT NULL DEFAULT 0,
    entry_type            VARCHAR(30)  NOT NULL DEFAULT 'MONTHLY',
    notes                 VARCHAR(500),
    verified              TINYINT(1)   NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_we_industry FOREIGN KEY (industry_id)
        REFERENCES industries (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table 6: audit_reports
CREATE TABLE IF NOT EXISTS audit_reports (
    id                       BIGINT        NOT NULL AUTO_INCREMENT,
    industry_id              BIGINT        NOT NULL,
    generated_by_user_id     BIGINT,
    period_start             DATE          NOT NULL,
    period_end               DATE          NOT NULL,
    total_generated_kg       DOUBLE        NOT NULL DEFAULT 0,
    total_recycled_kg        DOUBLE        NOT NULL DEFAULT 0,
    total_eliminated_kg      DOUBLE        NOT NULL DEFAULT 0,
    reduction_rate_percent   DOUBLE,
    recycling_ratio_percent  DOUBLE,
    status                   VARCHAR(20)   NOT NULL DEFAULT 'DRAFT',
    generated_at             DATETIME,
    remarks                  VARCHAR(1000),
    PRIMARY KEY (id),
    CONSTRAINT fk_ar_industry  FOREIGN KEY (industry_id)          REFERENCES industries (id) ON DELETE CASCADE,
    CONSTRAINT fk_ar_user      FOREIGN KEY (generated_by_user_id) REFERENCES users      (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_we_industry_date ON waste_entries  (industry_id, entry_date);
CREATE INDEX IF NOT EXISTS idx_ar_industry       ON audit_reports  (industry_id);
CREATE INDEX IF NOT EXISTS idx_ar_status         ON audit_reports  (status);
