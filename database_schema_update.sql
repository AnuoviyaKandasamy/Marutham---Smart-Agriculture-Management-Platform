-- Update users table to include location and language preference
ALTER TABLE users ADD COLUMN location VARCHAR(150) DEFAULT 'Central Market' AFTER phone;
ALTER TABLE users ADD COLUMN language_preference ENUM('EN', 'TA') DEFAULT 'EN' AFTER role;
ALTER TABLE users ADD COLUMN is_active BOOLEAN DEFAULT TRUE AFTER created_at;

-- Update existing demo users with some locations
UPDATE users SET location = 'Thanjavur' WHERE username = 'farmer_murugan';
UPDATE users SET location = 'Trichy' WHERE username = 'farmer_lakshmi';
UPDATE users SET location = 'Madurai' WHERE username = 'expert_senthil';
UPDATE users SET location = 'Chennai' WHERE username = 'admin_marutham';

-- Add soft delete support
ALTER TABLE posts ADD COLUMN is_deleted BOOLEAN DEFAULT FALSE;
ALTER TABLE comments ADD COLUMN is_deleted BOOLEAN DEFAULT FALSE;
ALTER TABLE equipment ADD COLUMN is_deleted BOOLEAN DEFAULT FALSE;

-- Create activity_logs table for audit trail
CREATE TABLE IF NOT EXISTS activity_logs (
    log_id      INT           NOT NULL AUTO_INCREMENT,
    user_id     INT           DEFAULT NULL,
    action      VARCHAR(100)  NOT NULL,
    description TEXT,
    ip_address  VARCHAR(45),
    created_at  TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (log_id),
    CONSTRAINT fk_logs_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create indexes for performance
CREATE INDEX idx_posts_deleted ON posts(is_deleted);
CREATE INDEX idx_equipment_deleted ON equipment(is_deleted);
CREATE INDEX idx_logs_user ON activity_logs(user_id);
CREATE INDEX idx_logs_created ON activity_logs(created_at DESC);
