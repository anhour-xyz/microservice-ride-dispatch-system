CREATE TABLE credentials (
    user_id BINARY(16) NOT NULL,
    email VARCHAR(320) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (user_id),
    CONSTRAINT uk_credentials_email UNIQUE (email)
);

CREATE TABLE credential_roles (
    user_id BINARY(16) NOT NULL,
    role VARCHAR(20) NOT NULL,
    PRIMARY KEY (user_id, role),
    CONSTRAINT fk_credential_roles_user
        FOREIGN KEY (user_id)
        REFERENCES credentials (user_id)
        ON DELETE CASCADE
);

CREATE TABLE refresh_tokens (
    id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    revoked_at DATETIME(6) NULL,
    expires_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_refresh_tokens_hash UNIQUE (token_hash),
    INDEX idx_refresh_tokens_user_id (user_id),
    CONSTRAINT fk_refresh_tokens_user
        FOREIGN KEY (user_id)
        REFERENCES credentials (user_id)
        ON DELETE CASCADE
);