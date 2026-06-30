-- ============================================================
-- Marutham Role-Based Access Control Updates
-- ============================================================

USE marutham_db;

-- 1. Add target_expert_id to consultations
-- This allows farmers to direct questions to specific experts
ALTER TABLE consultations ADD COLUMN target_expert_id INT DEFAULT NULL;
ALTER TABLE consultations ADD CONSTRAINT fk_consult_target_expert
FOREIGN KEY (target_expert_id) REFERENCES users(user_id) ON DELETE SET NULL;

-- 2. Indexing for faster role-based filtering
CREATE INDEX idx_consult_target_expert ON consultations(target_expert_id, status);
