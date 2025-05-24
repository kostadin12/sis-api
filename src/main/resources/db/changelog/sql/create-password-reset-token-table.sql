--changeset kostadin:create-password-reset-tokens-table
CREATE TABLE password_reset_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL,
    expiration_time TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    request_count INTEGER DEFAULT 1,
    last_request_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

--changeset kostadin:create-password-reset-tokens-indexes
CREATE INDEX idx_password_reset_tokens_token ON password_reset_tokens (token);
CREATE INDEX idx_password_reset_tokens_email ON password_reset_tokens (email);
CREATE INDEX idx_password_reset_tokens_expiration_time ON password_reset_tokens (expiration_time);
CREATE INDEX idx_password_reset_tokens_created_at ON password_reset_tokens (created_at);