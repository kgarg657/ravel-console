CREATE DATABASE IF NOT EXISTS ravel_db;
USE ravel_db;

CREATE TABLE IF NOT EXISTS entity (
    entity_id VARCHAR(32) PRIMARY KEY,
    entity_type VARCHAR(20) NOT NULL,
    name VARCHAR(150) NOT NULL,
    risk_score INT NOT NULL DEFAULT 0,
    department VARCHAR(100) NULL,
    role_title VARCHAR(100) NULL,
    category VARCHAR(100) NULL,
    gst_number VARCHAR(20) NULL,
    phone VARCHAR(20) NULL,
    bank_account VARCHAR(50) NULL,
    amount DOUBLE NULL,
    approver_id VARCHAR(32) NULL,
    vendor_id VARCHAR(32) NULL,
    invoice_date VARCHAR(32) NULL,
    CONSTRAINT chk_entity_type CHECK (entity_type IN ('EMPLOYEE', 'VENDOR', 'INVOICE')),
    CONSTRAINT chk_entity_risk CHECK (risk_score >= 0 AND risk_score <= 100),
    CONSTRAINT chk_entity_name_nonblank CHECK (CHAR_LENGTH(TRIM(name)) > 0),
    CONSTRAINT chk_invoice_amount CHECK (amount IS NULL OR amount > 0)
);

CREATE TABLE IF NOT EXISTS relationship (
    link_id VARCHAR(32) PRIMARY KEY,
    source_id VARCHAR(32) NOT NULL,
    target_id VARCHAR(32) NOT NULL,
    label VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    amount DOUBLE NULL,
    CONSTRAINT chk_rel_status CHECK (status IN ('SUGGESTED', 'APPROVED', 'REJECTED')),
    CONSTRAINT chk_rel_no_self_link CHECK (source_id <> target_id),
    CONSTRAINT chk_rel_amount CHECK (amount IS NULL OR amount > 0),
    CONSTRAINT fk_rel_source FOREIGN KEY (source_id) REFERENCES entity(entity_id),
    CONSTRAINT fk_rel_target FOREIGN KEY (target_id) REFERENCES entity(entity_id)
);

CREATE TABLE IF NOT EXISTS audit_case (
    case_id VARCHAR(32) PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    fraud_type VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    max_auditors INT NOT NULL DEFAULT 2,
    CONSTRAINT chk_case_status CHECK (status IN ('OPEN', 'UNDER_REVIEW', 'CLOSED')),
    CONSTRAINT chk_case_max_auditors CHECK (max_auditors > 0)
);

CREATE TABLE IF NOT EXISTS case_entity (
    case_id VARCHAR(32) NOT NULL,
    entity_id VARCHAR(32) NOT NULL,
    PRIMARY KEY (case_id, entity_id),
    CONSTRAINT fk_ce_case FOREIGN KEY (case_id) REFERENCES audit_case(case_id) ON DELETE CASCADE,
    CONSTRAINT fk_ce_entity FOREIGN KEY (entity_id) REFERENCES entity(entity_id)
);

CREATE TABLE IF NOT EXISTS audit_log (
    log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    action VARCHAR(100) NOT NULL,
    target_id VARCHAR(64) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);