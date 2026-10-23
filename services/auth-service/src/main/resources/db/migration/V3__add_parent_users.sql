-- ==============================================================================
-- V3__add_parent_users.sql
-- Seed Parent User Accounts for School Evidencing
-- Password for all parent accounts is: Password123!
-- ==============================================================================

INSERT INTO users (id, email, password_hash, full_name, role, enabled) VALUES
('bbbb0001-0000-0000-0000-000000000001', 'parent_michael@jobintel.com', '$2a$12$OVPVQcVnSfMWbW5r7IYBvuQkFBpYTy/XHro4t35NoSW4E4YrL6AUi', 'Michael Anderson', 'USER', true),
('bbbb0002-0000-0000-0000-000000000002', 'parent_sophia@jobintel.com', '$2a$12$OVPVQcVnSfMWbW5r7IYBvuQkFBpYTy/XHro4t35NoSW4E4YrL6AUi', 'Sophia Taylor', 'USER', true),
('bbbb0003-0000-0000-0000-000000000003', 'parent_lucas@jobintel.com', '$2a$12$OVPVQcVnSfMWbW5r7IYBvuQkFBpYTy/XHro4t35NoSW4E4YrL6AUi', 'Lucas Martinez', 'USER', true),
('bbbb0004-0000-0000-0000-000000000004', 'parent_elena@jobintel.com', '$2a$12$OVPVQcVnSfMWbW5r7IYBvuQkFBpYTy/XHro4t35NoSW4E4YrL6AUi', 'Elena Rostova', 'USER', true)
ON CONFLICT (email) DO NOTHING;
