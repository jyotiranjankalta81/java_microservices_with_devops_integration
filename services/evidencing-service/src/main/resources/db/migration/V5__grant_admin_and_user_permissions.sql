-- ==============================================================================
-- V5__grant_admin_and_user_permissions.sql
-- Grant Evidencing permissions to ADMIN and general USER roles
-- Seed admin_seed user into user_scopes as SCHOOL_ADMIN
-- ==============================================================================

-- 1. ADMIN (Full module access)
INSERT INTO role_permissions (id, role, permission) VALUES
('d0000009-0000-0000-0000-000000000001', 'ADMIN', 'EVIDENCING_VIEW'),
('d0000009-0000-0000-0000-000000000002', 'ADMIN', 'EVIDENCING_CREATE'),
('d0000009-0000-0000-0000-000000000003', 'ADMIN', 'EVIDENCING_EDIT_OWN'),
('d0000009-0000-0000-0000-000000000004', 'ADMIN', 'EVIDENCING_DELETE_OWN');

-- 2. USER (General logged-in platform user can view school public activity posts)
INSERT INTO role_permissions (id, role, permission) VALUES
('d000000a-0000-0000-0000-000000000001', 'USER', 'EVIDENCING_VIEW');

-- 3. Seed admin_seed user into user_scopes as SCHOOL_ADMIN of Oakridge Academy
INSERT INTO user_scopes (id, user_id, display_name, role, avatar_url, school_id, branch_id, class_id) VALUES
('ea00000f-0000-0000-0000-000000000001', '6204d9c5-cb7f-4abd-8b3d-58596fea7fe1', 'System Administrator', 'SCHOOL_ADMIN', 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150', 'e0000001-0000-0000-0000-000000000001', 'b0000001-0000-0000-0000-000000000001', NULL);
