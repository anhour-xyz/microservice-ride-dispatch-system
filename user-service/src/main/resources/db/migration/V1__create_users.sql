CREATE TABLE users (
    user_id BINARY(16) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (user_id),
    CONSTRAINT uk_users_phone_number UNIQUE (phone_number)
);

CREATE TABLE user_roles (
    user_id BINARY(16) NOT NULL,
    role VARCHAR(20) NOT NULL,
    PRIMARY KEY (user_id, role),
    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id)
        REFERENCES users (user_id)
        ON DELETE CASCADE
);