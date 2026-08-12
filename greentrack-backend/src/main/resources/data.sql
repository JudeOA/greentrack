-- Run manually after first app start to insert seed data
-- psql "$DATABASE_URL" -f src/main/resources/schema.sql

INSERT INTO categories (name, icon, description) VALUES
('Overflowing Public Bin',  '🗑️',  'Public waste bins that are full and overflowing'),
('Illegal Dumping',         '🚯',  'Waste dumped illegally in unauthorised areas'),
('Missed Collection',       '🚛',  'Scheduled waste collection that did not occur'),
('Hazardous Waste',         '☣️',  'Dangerous or toxic waste requiring specialist removal'),
('Dead Animal',             '🐾',  'Dead animals requiring sanitation response'),
('Other',                   '📋',  'Other waste management issues')
ON CONFLICT (name) DO NOTHING;

-- Admin  password: Admin@2026
INSERT INTO users (name, email, password_hash, role, phone, is_active, created_at, updated_at) VALUES
('GreenTrack Admin', 'admin@greentrack.app',
 '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdFOzLjkF6bC5LS',
 'ADMIN', '+233200000001', true, NOW(), NOW())
ON CONFLICT (email) DO NOTHING;

-- Collector  password: Admin@2026 (change in production)
INSERT INTO users (name, email, password_hash, role, phone, badge_id, is_active, created_at, updated_at) VALUES
('Kofi Mensah', 'kofi@greentrack.app',
 '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdFOzLjkF6bC5LS',
 'COLLECTOR', '+233200000002', 'GT-COL-001', true, NOW(), NOW())
ON CONFLICT (email) DO NOTHING;

-- Citizen  password: Admin@2026 (change in production)
INSERT INTO users (name, email, password_hash, role, phone, is_active, created_at, updated_at) VALUES
('Ama Darko', 'ama@greentrack.app',
 '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdFOzLjkF6bC5LS',
 'CITIZEN', '+233200000003', true, NOW(), NOW())
ON CONFLICT (email) DO NOTHING;
