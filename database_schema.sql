-- ============================================================
-- Marutham Professional Farmer Platform
-- Complete Database Setup – MySQL 8.0+
--
-- Usage:
--   mysql -u root -p < database_schema.sql
--
-- What this file does (in order):
--   1. Create & select database
--   2. Drop tables in safe reverse-FK order
--   3. Create all 12 tables with proper constraints & indexes
--   4. Insert seed data (market prices, crops)
--   5. Insert demo users (password = "Password@123" for all)
--   6. Insert demo posts, comments, likes, equipment,
--      equipment_requests, consultations, notifications,
--      crop_recommendations, price_requests
--   7. Useful admin queries at the bottom (commented out)
-- ============================================================

-- ------------------------------------------------------------
-- SECTION 1 – Database
-- ------------------------------------------------------------
CREATE DATABASE IF NOT EXISTS marutham_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE marutham_db;

-- Keep FK checks off while we rebuild
SET FOREIGN_KEY_CHECKS = 0;

-- ------------------------------------------------------------
-- SECTION 2 – Drop tables (reverse FK order)
-- ------------------------------------------------------------
DROP TABLE IF EXISTS price_requests;
DROP TABLE IF EXISTS crop_recommendations;
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS consultations;
DROP TABLE IF EXISTS equipment_requests;
DROP TABLE IF EXISTS likes;
DROP TABLE IF EXISTS comments;
DROP TABLE IF EXISTS posts;
DROP TABLE IF EXISTS equipment;
DROP TABLE IF EXISTS market_prices;
DROP TABLE IF EXISTS crops;
DROP TABLE IF EXISTS users;

SET FOREIGN_KEY_CHECKS = 1;

-- ------------------------------------------------------------
-- SECTION 3 – Create Tables
-- ------------------------------------------------------------

-- 3.1  users
-- ---------------------------------------------------------------
CREATE TABLE users (
    user_id         INT           NOT NULL AUTO_INCREMENT,
    username        VARCHAR(50)   NOT NULL,
    password        VARCHAR(255)  NOT NULL,          -- BCrypt hash
    email           VARCHAR(100)  NOT NULL,
    full_name       VARCHAR(100)  NOT NULL,
    phone           VARCHAR(20)   DEFAULT NULL,
    role            ENUM('FARMER','EXPERT','ADMIN') NOT NULL DEFAULT 'FARMER',
    profile_picture VARCHAR(255)  DEFAULT 'default_dp.png',
    created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (user_id),
    UNIQUE KEY uq_username (username),
    UNIQUE KEY uq_email    (email),
    INDEX idx_role (role)                            -- fast role-based lookups
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- 3.2  posts
-- ---------------------------------------------------------------
CREATE TABLE posts (
    post_id    INT          NOT NULL AUTO_INCREMENT,
    user_id    INT          NOT NULL,
    title      VARCHAR(255) NOT NULL,
    content    TEXT         NOT NULL,
    category   VARCHAR(100) NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (post_id),
    INDEX idx_user_id  (user_id),
    INDEX idx_category (category),
    INDEX idx_created  (created_at DESC),            -- ORDER BY created_at DESC
    CONSTRAINT fk_posts_user
        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- 3.3  likes  (unique per post+user to prevent double-likes)
-- ---------------------------------------------------------------
CREATE TABLE likes (
    like_id    INT       NOT NULL AUTO_INCREMENT,
    post_id    INT       NOT NULL,
    user_id    INT       NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (like_id),
    UNIQUE KEY uq_post_user (post_id, user_id),      -- one like per user per post
    INDEX idx_likes_user (user_id),
    CONSTRAINT fk_likes_post
        FOREIGN KEY (post_id) REFERENCES posts(post_id) ON DELETE CASCADE,
    CONSTRAINT fk_likes_user
        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- 3.4  comments
-- ---------------------------------------------------------------
CREATE TABLE comments (
    comment_id INT       NOT NULL AUTO_INCREMENT,
    post_id    INT       NOT NULL,
    user_id    INT       NOT NULL,
    content    TEXT      NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (comment_id),
    INDEX idx_comments_post (post_id),               -- fast fetch by post
    INDEX idx_comments_user (user_id),
    CONSTRAINT fk_comments_post
        FOREIGN KEY (post_id) REFERENCES posts(post_id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_user
        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- 3.5  equipment
-- ---------------------------------------------------------------
CREATE TABLE equipment (
    equipment_id        INT            NOT NULL AUTO_INCREMENT,
    owner_id            INT            NOT NULL,
    name                VARCHAR(150)   NOT NULL,
    description         TEXT           DEFAULT NULL,
    price_per_day       DECIMAL(10,2)  NOT NULL DEFAULT 0.00,
    availability_status BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (equipment_id),
    INDEX idx_eq_owner     (owner_id),
    INDEX idx_eq_available (availability_status),    -- getAllAvailable() filter
    CONSTRAINT fk_equipment_owner
        FOREIGN KEY (owner_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- 3.6  equipment_requests
-- ---------------------------------------------------------------
CREATE TABLE equipment_requests (
    request_id   INT  NOT NULL AUTO_INCREMENT,
    equipment_id INT  NOT NULL,
    requester_id INT  NOT NULL,
    start_date   DATE NOT NULL,
    end_date     DATE NOT NULL,
    status       ENUM('PENDING','APPROVED','REJECTED') NOT NULL DEFAULT 'PENDING',
    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (request_id),
    INDEX idx_req_equipment  (equipment_id),
    INDEX idx_req_requester  (requester_id),
    INDEX idx_req_status     (status),
    CONSTRAINT fk_req_equipment
        FOREIGN KEY (equipment_id) REFERENCES equipment(equipment_id) ON DELETE CASCADE,
    CONSTRAINT fk_req_requester
        FOREIGN KEY (requester_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- 3.7  consultations
-- ---------------------------------------------------------------
CREATE TABLE consultations (
    consultation_id INT       NOT NULL AUTO_INCREMENT,
    farmer_id       INT       NOT NULL,
    expert_id       INT       DEFAULT NULL,          -- NULL until answered
    question        TEXT      NOT NULL,
    answer          TEXT      DEFAULT NULL,
    status          ENUM('PENDING','ANSWERED') NOT NULL DEFAULT 'PENDING',
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (consultation_id),
    INDEX idx_consult_farmer (farmer_id),
    INDEX idx_consult_status (status),               -- getPendingForExpert() filter
    CONSTRAINT fk_consult_farmer
        FOREIGN KEY (farmer_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_consult_expert
        FOREIGN KEY (expert_id) REFERENCES users(user_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- 3.8  notifications
-- ---------------------------------------------------------------
CREATE TABLE notifications (
    notification_id INT          NOT NULL AUTO_INCREMENT,
    user_id         INT          NOT NULL,
    message         VARCHAR(500) NOT NULL,
    is_read         BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (notification_id),
    INDEX idx_notif_user   (user_id),
    INDEX idx_notif_unread (user_id, is_read),       -- fast unread count
    CONSTRAINT fk_notif_user
        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- 3.9  market_prices
--      updated_at is queried by MarketDAO: ORDER BY updated_at DESC
-- ---------------------------------------------------------------
CREATE TABLE market_prices (
    price_id     INT            NOT NULL AUTO_INCREMENT,
    crop_name    VARCHAR(100)   NOT NULL,
    price_per_kg DECIMAL(10,2)  NOT NULL,
    market_name  VARCHAR(150)   NOT NULL,
    state        VARCHAR(100)   NOT NULL,
    updated_at   TIMESTAMP      NOT NULL
                 DEFAULT CURRENT_TIMESTAMP
                 ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (price_id),
    INDEX idx_mp_crop  (crop_name),
    INDEX idx_mp_state (state),
    INDEX idx_mp_updated (updated_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- 3.10  crops  (recommendation engine lookup table)
--       Queried by: WHERE soil_type=? AND season=? AND water_requirement=?
-- ---------------------------------------------------------------
CREATE TABLE crops (
    crop_id           INT         NOT NULL AUTO_INCREMENT,
    crop_name         VARCHAR(100) NOT NULL,
    soil_type         VARCHAR(50)  NOT NULL,
    season            VARCHAR(50)  NOT NULL,
    water_requirement VARCHAR(50)  NOT NULL,

    PRIMARY KEY (crop_id),
    -- Composite index exactly matches the DAO query columns
    INDEX idx_crop_lookup (soil_type, season, water_requirement)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- 3.11  crop_recommendations  (per-user history)
-- ---------------------------------------------------------------
CREATE TABLE crop_recommendations (
    recommendation_id  INT          NOT NULL AUTO_INCREMENT,
    user_id            INT          NOT NULL,
    soil_type          VARCHAR(50)  NOT NULL,
    season             VARCHAR(50)  NOT NULL,
    water_availability VARCHAR(50)  NOT NULL,
    region             VARCHAR(150) DEFAULT NULL,
    recommended_crops  TEXT         DEFAULT NULL,
    created_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (recommendation_id),
    INDEX idx_cr_user    (user_id),
    INDEX idx_cr_created (created_at DESC),
    CONSTRAINT fk_cr_user
        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3.12 price_requests
-- ---------------------------------------------------------------
CREATE TABLE price_requests (
    request_id      INT            NOT NULL AUTO_INCREMENT,
    farmer_id       INT            NOT NULL,
    crop_name       VARCHAR(100)   NOT NULL,
    requested_price DECIMAL(10,2)  NOT NULL,
    quantity        VARCHAR(100)   NOT NULL,
    status          ENUM('PENDING','PROCESSED') NOT NULL DEFAULT 'PENDING',
    admin_price     DECIMAL(10,2)  DEFAULT NULL,
    created_at      TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (request_id),
    CONSTRAINT fk_price_req_farmer FOREIGN KEY (farmer_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ============================================================
-- SECTION 4 – Seed Data: market_prices
-- ============================================================
INSERT INTO market_prices (crop_name, price_per_kg, market_name, state) VALUES
-- Tamil Nadu
('Rice',        28.50, 'Chennai Koyambedu',   'Tamil Nadu'),
('Tomato',      18.00, 'Madurai Mattuthavani', 'Tamil Nadu'),
('Cotton',      62.50, 'Coimbatore APMC',      'Tamil Nadu'),
('Sugarcane',    3.50, 'Thanjavur Mandi',      'Tamil Nadu'),
('Turmeric',   120.00, 'Erode Market',         'Tamil Nadu'),
('Groundnut',   55.00, 'Tirunelveli Market',   'Tamil Nadu'),
('Banana',      22.00, 'Trichy Market',        'Tamil Nadu'),
('Coconut',     14.00, 'Pollachi APMC',        'Tamil Nadu'),
('Chilli',      95.00, 'Guntur Tamil Market',  'Tamil Nadu'),
('Brinjal',     12.00, 'Salem APMC',           'Tamil Nadu'),

-- Other States
('Wheat',       22.00, 'Delhi Azadpur',        'Delhi'),
('Onion',       25.00, 'Nashik APMC',          'Maharashtra'),
('Soybean',     42.00, 'Indore Mandi',         'Madhya Pradesh'),
('Maize',       19.00, 'Mysuru APMC',          'Karnataka'),
('Jowar',       21.00, 'Solapur APMC',         'Maharashtra'),
('Bajra',       18.50, 'Jaipur Mandi',         'Rajasthan'),
('Sunflower',   50.00, 'Bellary APMC',         'Karnataka'),
('Potato',      14.00, 'Agra Mandi',           'Uttar Pradesh'),
('Garlic',      60.00, 'Kota Mandi',           'Rajasthan'),
('Ginger',      45.00, 'Cochin Market',        'Kerala');


-- ============================================================
-- SECTION 5 – Seed Data: crops (recommendation lookup)
-- All combinations of soil_type × season × water_requirement
-- Values must exactly match frontend dropdown options
-- ============================================================
INSERT INTO crops (crop_name, soil_type, season, water_requirement) VALUES
-- Clay soil
('Rice',       'Clay',  'Kharif', 'High'),
('Sugarcane',  'Clay',  'Annual', 'High'),
('Banana',     'Clay',  'Annual', 'High'),
('Jute',       'Clay',  'Kharif', 'High'),
('Taro',       'Clay',  'Kharif', 'Medium'),
('Groundnut',  'Clay',  'Rabi',   'Low'),

-- Sandy soil
('Groundnut',  'Sandy', 'Kharif', 'Low'),
('Bajra',      'Sandy', 'Kharif', 'Low'),
('Watermelon', 'Sandy', 'Summer', 'Low'),
('Carrot',     'Sandy', 'Rabi',   'Medium'),
('Chilli',     'Sandy', 'Kharif', 'Medium'),
('Maize',      'Sandy', 'Kharif', 'Medium'),
('Cassava',    'Sandy', 'Annual', 'Low'),

-- Loamy soil
('Wheat',      'Loamy', 'Rabi',   'Medium'),
('Tomato',     'Loamy', 'Rabi',   'Medium'),
('Onion',      'Loamy', 'Rabi',   'Low'),
('Turmeric',   'Loamy', 'Annual', 'High'),
('Sunflower',  'Loamy', 'Rabi',   'Medium'),
('Potato',     'Loamy', 'Rabi',   'Medium'),
('Ginger',     'Loamy', 'Kharif', 'High'),
('Garlic',     'Loamy', 'Rabi',   'Low'),

-- Black (Regur) soil
('Cotton',     'Black', 'Kharif', 'Low'),
('Soybean',    'Black', 'Kharif', 'Medium'),
('Jowar',      'Black', 'Kharif', 'Low'),
('Chickpea',   'Black', 'Rabi',   'Low'),
('Linseed',    'Black', 'Rabi',   'Low'),
('Sunflower',  'Black', 'Kharif', 'Medium'),

-- Red soil
('Groundnut',  'Red',   'Kharif', 'Low'),
('Ragi',       'Red',   'Kharif', 'Low'),
('Castor',     'Red',   'Kharif', 'Low'),
('Mango',      'Red',   'Annual', 'Medium'),
('Tapioca',    'Red',   'Annual', 'Medium');


-- ============================================================
-- SECTION 6 – Demo Users
-- All passwords are BCrypt hash of: Password@123
-- (generated with BCrypt cost=10)
-- ============================================================
INSERT INTO users (username, password, email, full_name, phone, role) VALUES
(
  'admin_marutham',
  '$2a$10$aCEGbzMHWE/axMXHjfwhaunxGn/OwAUKIh22SbNS6JzP7xUntDwBO',
  'admin@marutham.in',
  'Marutham Admin',
  '9876543210',
  'ADMIN'
),
(
  'expert_senthil',
  '$2a$10$aCEGbzMHWE/axMXHjfwhaunxGn/OwAUKIh22SbNS6JzP7xUntDwBO',
  'senthil.expert@marutham.in',
  'Dr. Senthilkumar A',
  '9876543211',
  'EXPERT'
),
(
  'farmer_murugan',
  '$2a$10$aCEGbzMHWE/axMXHjfwhaunxGn/OwAUKIh22SbNS6JzP7xUntDwBO',
  'murugan.farmer@marutham.in',
  'Murugan Rajan',
  '9876543212',
  'FARMER'
),
(
  'farmer_lakshmi',
  '$2a$10$aCEGbzMHWE/axMXHjfwhaunxGn/OwAUKIh22SbNS6JzP7xUntDwBO',
  'lakshmi.farmer@marutham.in',
  'Lakshmi Devi',
  '9876543213',
  'FARMER'
),
(
  'expert_priya',
  '$2a$10$aCEGbzMHWE/axMXHjfwhaunxGn/OwAUKIh22SbNS6JzP7xUntDwBO',
  'priya.expert@marutham.in',
  'Dr. Priya Krishnan',
  '9876543214',
  'EXPERT'
);


-- ============================================================
-- SECTION 7 – Demo Posts
-- ============================================================
INSERT INTO posts (user_id, title, content, category) VALUES
(
  3,  -- farmer_murugan
  'Best organic fertilizer for paddy?',
  'I have been using urea for the past 5 years on my paddy fields in Thanjavur. This season I want to switch to organic. Can anyone suggest a good organic fertilizer and the correct application schedule? Soil type is clay.',
  'Soil & Fertilizers'
),
(
  4,  -- farmer_lakshmi
  'Drip irrigation setup cost for 2 acres',
  'I am planning to install drip irrigation for my banana plantation in Trichy. Currently watering manually which wastes a lot. What is the expected cost for 2 acres and which brand is reliable in Tamil Nadu?',
  'Irrigation'
),
(
  3,  -- farmer_murugan
  'Pest attack on tomato – yellow leaves and white spots',
  'My tomato crop in Madurai is showing yellow leaves with white powdery spots. Started 10 days ago and spreading fast. Already lost about 20% of plants. What pesticide or organic remedy should I use immediately?',
  'Pest & Disease'
),
(
  4,  -- farmer_lakshmi
  'Government subsidy for solar pump – how to apply?',
  'I heard that Tamil Nadu government is giving 90% subsidy on solar pumps for small farmers. Can anyone who has already applied share the process? Which office do I go to in Tirunelveli district?',
  'Government Schemes'
),
(
  3,  -- farmer_murugan
  'Selling turmeric directly to exporters – contacts needed',
  'I have 500 kg of dry turmeric from Erode variety ready for sale. Mandi price is very low this season. Looking for direct buyers or exporters who pay better price. Any contacts or online platforms to try?',
  'Market & Selling'
);


-- ============================================================
-- SECTION 8 – Demo Likes
-- ============================================================
INSERT INTO likes (post_id, user_id) VALUES
(1, 4), (1, 2), (1, 5),   -- post 1: 3 likes
(2, 3), (2, 2),            -- post 2: 2 likes
(3, 4), (3, 5), (3, 2),   -- post 3: 3 likes
(4, 3), (4, 2),            -- post 4: 2 likes
(5, 4), (5, 5);            -- post 5: 2 likes


-- ============================================================
-- SECTION 9 – Demo Comments
-- ============================================================
INSERT INTO comments (post_id, user_id, content) VALUES
(1, 2, 'Use vermicompost at 2 tonnes per acre before transplanting, then jeevamrutham spray once every 15 days. Very effective for clay soil paddy.'),
(1, 5, 'Panchagavya is another excellent option – 3% solution spray during tillering and panicle initiation stage gives great results.'),
(2, 2, 'Netafim and Jain Irrigation are the most reliable brands available in TN. For 2 acres banana, budget around ₹35,000–45,000 including installation.'),
(3, 5, 'This looks like powdery mildew (Oidium neolycopersici). Spray wettable sulphur 3g/litre immediately. Remove heavily affected leaves before spraying.'),
(3, 2, 'Neem oil 5ml + liquid soap 1ml per litre water also works well as organic option. Spray in the early morning for best results.'),
(4, 3, 'I applied last year from Tirunelveli Agriculture Department office on Palayamkottai Road. Needed Patta, Aadhaar, and bank passbook copies. Processing took about 3 months.');


-- ============================================================
-- SECTION 10 – Demo Equipment
-- ============================================================
INSERT INTO equipment (owner_id, name, description, price_per_day, availability_status) VALUES
(3, 'Power Tiller – 7HP',       'Kirloskar 7HP power tiller, well maintained, suitable for small plots up to 3 acres. Diesel consumption approx 3L/day.',          800.00, TRUE),
(3, 'Sprayer Pump – 16L',       'Battery operated 16L sprayer with telescopic wand. 5 hours battery life per charge. Includes 2 nozzle types.',                    150.00, TRUE),
(4, 'Rotavator – 35HP',         'Mahindra Jivo compatible rotavator, 5-feet width. Excellent for land preparation. Includes operator for ₹200 extra per day.',  1200.00, TRUE),
(4, 'Paddy Thresher',           'Electric paddy thresher, capacity 800kg/hour. 3-phase power connection required. Transportation by owner at extra cost.',         600.00, FALSE),
(3, 'Drip Irrigation Kit',      'Complete drip kit for 1 acre – mainline, sub-main, laterals, drippers (4L/hr). Setup assistance provided free of charge.',        300.00, TRUE);


-- ============================================================
-- SECTION 11 – Demo Equipment Requests
-- ============================================================
INSERT INTO equipment_requests (equipment_id, requester_id, start_date, end_date, status) VALUES
(1, 4, '2026-07-01', '2026-07-03', 'APPROVED'),
(2, 4, '2026-07-10', '2026-07-10', 'PENDING'),
(3, 3, '2026-07-05', '2026-07-06', 'REJECTED'),
(5, 3, '2026-07-15', '2026-07-16', 'PENDING');


-- ============================================================
-- SECTION 12 – Demo Consultations
-- ============================================================
INSERT INTO consultations (farmer_id, expert_id, question, answer, status) VALUES
(
  3, 2,
  'My rice crop is showing brown spots on leaves 3 weeks after transplanting. Soil is clay, water is adequate. Is this blast disease or brown spot? What should I spray?',
  'Based on your description this is most likely Rice Brown Spot (Helminthosporium oryzae), not blast. Key difference: brown spot has oval lesions with yellow halo; blast has diamond-shaped grey lesions. Treatment: Spray Mancozeb 75% WP at 2g/litre. Repeat after 10 days. Also apply potassium fertilizer (MOP) to strengthen plant resistance.',
  'ANSWERED'
),
(
  4, NULL,
  'I want to grow turmeric as intercrop in my coconut garden. Coconut spacing is 7.5m x 7.5m, 8 years old trees. Is turmeric suitable? What variety should I choose and when to plant?',
  NULL,
  'PENDING'
),
(
  3, 5,
  'What is the ideal spacing and seed rate for groundnut in sandy soil? And how many days from sowing to harvest for TMV-7 variety?',
  'For TMV-7 groundnut in sandy soil: Row spacing 30cm, plant spacing 10cm. Seed rate: 100kg per acre (bold seeds). Sow seeds after removing testa (seed coat). TMV-7 matures in 105–110 days. Apply gypsum 200kg/acre at pegging stage for better pod filling.',
  'ANSWERED'
),
(
  4, NULL,
  'Is it safe to apply glyphosate weedicide between banana rows? My banana plants are 4 months old. I want to control the grass weeds without damaging roots.',
  NULL,
  'PENDING'
);


-- ============================================================
-- SECTION 13 – Demo Notifications
-- ============================================================
INSERT INTO notifications (user_id, message, is_read) VALUES
(3, 'Your booking request for ''Power Tiller – 7HP'' was approved.',         TRUE),
(3, 'An expert has answered your question about rice brown spot.',            TRUE),
(4, 'New equipment booking request received.',                                FALSE),
(3, 'An expert has answered your question about groundnut spacing.',          FALSE),
(4, 'Your booking request for ''Rotavator – 35HP'' was rejected.',           FALSE);


-- ============================================================
-- SECTION 14 – Demo Crop Recommendations History
-- ============================================================
INSERT INTO crop_recommendations
    (user_id, soil_type, season, water_availability, region, recommended_crops) VALUES
(3, 'Clay',  'Kharif', 'High',   'Thanjavur, Tamil Nadu', 'Rice, Sugarcane, Banana, Jute'),
(4, 'Loamy', 'Rabi',   'Medium', 'Trichy, Tamil Nadu',    'Wheat, Tomato, Sunflower, Potato'),
(3, 'Sandy', 'Kharif', 'Low',    'Madurai, Tamil Nadu',   'Groundnut, Bajra, Chilli'),
(4, 'Clay',  'Annual', 'High',   'Trichy, Tamil Nadu',    'Sugarcane, Banana');


-- ============================================================
-- SECTION 15 – Verification Queries
-- Run these after import to confirm everything is loaded.
-- ============================================================

SELECT 'TABLE ROW COUNTS' AS check_label;
SELECT 'users' AS tbl, COUNT(*) AS rows FROM users
UNION ALL SELECT 'posts',  COUNT(*) FROM posts
UNION ALL SELECT 'likes', COUNT(*) FROM likes
UNION ALL SELECT 'comments', COUNT(*) FROM comments
UNION ALL SELECT 'equipment', COUNT(*) FROM equipment
UNION ALL SELECT 'equipment_requests', COUNT(*) FROM equipment_requests
UNION ALL SELECT 'consultations',COUNT(*) FROM consultations
UNION ALL SELECT 'notifications',COUNT(*) FROM notifications
UNION ALL SELECT 'market_prices',COUNT(*) FROM market_prices
UNION ALL SELECT 'crops', COUNT(*) FROM crops
UNION ALL SELECT 'crop_recommendations',COUNT(*) FROM crop_recommendations
UNION ALL SELECT 'price_requests',COUNT(*) FROM price_requests;


-- ============================================================
-- SECTION 16 – Admin Helper Queries (uncomment when needed)
-- ============================================================

-- Promote a user to ADMIN:
-- UPDATE users SET role = 'ADMIN' WHERE username = 'your_username';

-- Reset a user password (BCrypt of 'Password@123'):
-- UPDATE users SET password = '$2a$10$aCEGbzMHWE/axMXHjfwhaunxGn/OwAUKIh22SbNS6JzP7xUntDwBO'
--   WHERE username = 'your_username';

-- See all pending consultations:
-- SELECT c.consultation_id, u.full_name AS farmer, c.question, c.created_at
--   FROM consultations c JOIN users u ON c.farmer_id = u.user_id
--  WHERE c.status = 'PENDING'
--  ORDER BY c.created_at ASC;

-- See platform summary stats:
-- SELECT
--   (SELECT COUNT(*) FROM users)              AS total_users,
--   (SELECT COUNT(*) FROM posts)              AS total_posts,
--   (SELECT COUNT(*) FROM equipment)          AS total_equipment,
--   (SELECT COUNT(*) FROM consultations)      AS total_consultations,
--   (SELECT COUNT(*) FROM equipment_requests) AS total_bookings;

-- See unread notification count per user:
-- SELECT u.username, COUNT(*) AS unread_count
--   FROM notifications n JOIN users u ON n.user_id = u.user_id
--  WHERE n.is_read = FALSE
--  GROUP BY u.username
--  ORDER BY unread_count DESC;