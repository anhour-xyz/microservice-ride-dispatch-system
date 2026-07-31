ALTER TABLE credentials
    ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE email_verification_tokens (
    id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    used_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_email_verification_token_hash UNIQUE (token_hash),
    INDEX idx_email_verification_user_id (user_id),
    CONSTRAINT fk_email_verification_user
        FOREIGN KEY (user_id)
        REFERENCES credentials (user_id)
        ON DELETE CASCADE
);