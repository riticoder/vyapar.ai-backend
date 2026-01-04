-- Database Schema for Vyapar.ai (Trivana-style)
-- Users table with OAuth2 support

-- Drop existing table if exists (for clean restart)
DROP TABLE IF EXISTS Vyapar_USERS;

-- Create Users table - H2 database compatible
CREATE TABLE Vyapar_USERS (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP DEFAULT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    name VARCHAR(255) DEFAULT NULL,
    password VARCHAR(255) NOT NULL,
    auth_provider VARCHAR(255) DEFAULT NULL,
    google_id VARCHAR(255) DEFAULT NULL
);

-- Initial data for Vyapar.ai development
-- Demo users with both LOCAL and GOOGLE providers

-- Insert demo local user (password is 'password' - BCrypt hash)
INSERT INTO Vyapar_USERS (email, password, name, auth_provider, google_id, enabled, created_at) 
VALUES ('demo@vyapar.ai', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Demo User', 'LOCAL', NULL, TRUE, CURRENT_TIMESTAMP);

-- Insert sample Google OAuth users for quick login testing
INSERT INTO Vyapar_USERS (email, password, name, auth_provider, google_id, enabled, created_at) 
VALUES ('john.doe@gmail.com', '$2a$10$OAUTH2_DUMMY_HASH', 'John Doe', 'GOOGLE', '1234567890', TRUE, CURRENT_TIMESTAMP);

INSERT INTO Vyapar_USERS (email, password, name, auth_provider, google_id, enabled, created_at) 
VALUES ('jane.smith@gmail.com', '$2a$10$OAUTH2_DUMMY_HASH', 'Jane Smith', 'GOOGLE', '0987654321', TRUE, CURRENT_TIMESTAMP);

INSERT INTO Vyapar_USERS (email, password, name, auth_provider, google_id, enabled, created_at) 
VALUES ('alice.wonder@gmail.com', '$2a$10$OAUTH2_DUMMY_HASH', 'Alice Wonder', 'GOOGLE', '1122334455', TRUE, CURRENT_TIMESTAMP);
