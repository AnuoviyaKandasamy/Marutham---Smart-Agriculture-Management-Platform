-- ============================================================
-- Marutham Enhancements V2 - Database Updates
-- Features: Image Upload, E-Marketplace, Soil Health
-- ============================================================

USE marutham_db;

-- 1. Feature 3: Image Upload for Consultations
-- Allow farmers to upload pictures of pests/diseases
ALTER TABLE consultations ADD COLUMN image_url VARCHAR(255) DEFAULT NULL;

-- 2. Feature 4: E-Marketplace (Farmers listing produce)
-- Marketplace for farmers to list their crops for sale
CREATE TABLE IF NOT EXISTS produces (
    produce_id INT PRIMARY KEY AUTO_INCREMENT,
    farmer_id INT NOT NULL,
    crop_name VARCHAR(100) NOT NULL,
    quantity VARCHAR(50) NOT NULL,
    price_expected DECIMAL(10,2) NOT NULL,
    location VARCHAR(150),
    description TEXT,
    contact_number VARCHAR(20),
    image_url VARCHAR(255),
    status ENUM('AVAILABLE', 'SOLD', 'RESERVED') DEFAULT 'AVAILABLE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (farmer_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Feature 6: Soil Health Cards
-- Store soil test results and automated recommendations
CREATE TABLE IF NOT EXISTS soil_reports (
    report_id INT PRIMARY KEY AUTO_INCREMENT,
    farmer_id INT NOT NULL,
    soil_type VARCHAR(50),
    ph_level DECIMAL(4,2),
    nitrogen_content VARCHAR(50),
    phosphorus_content VARCHAR(50),
    potassium_content VARCHAR(50),
    organic_carbon VARCHAR(50),
    recommendation TEXT,
    report_file_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (farmer_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
