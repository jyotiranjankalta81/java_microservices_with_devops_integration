-- Seed Role Permissions (Deny by default: only explicitly inserted roles have permissions)

-- TEACHER
INSERT INTO role_permissions (id, role, permission) VALUES
('d0000001-0000-0000-0000-000000000001', 'TEACHER', 'EVIDENCING_VIEW'),
('d0000001-0000-0000-0000-000000000002', 'TEACHER', 'EVIDENCING_CREATE'),
('d0000001-0000-0000-0000-000000000003', 'TEACHER', 'EVIDENCING_EDIT_OWN'),
('d0000001-0000-0000-0000-000000000004', 'TEACHER', 'EVIDENCING_DELETE_OWN');

-- BRANCH_ADMIN
INSERT INTO role_permissions (id, role, permission) VALUES
('d0000002-0000-0000-0000-000000000001', 'BRANCH_ADMIN', 'EVIDENCING_VIEW'),
('d0000002-0000-0000-0000-000000000002', 'BRANCH_ADMIN', 'EVIDENCING_CREATE'),
('d0000002-0000-0000-0000-000000000003', 'BRANCH_ADMIN', 'EVIDENCING_EDIT_OWN'),
('d0000002-0000-0000-0000-000000000004', 'BRANCH_ADMIN', 'EVIDENCING_DELETE_OWN');

-- SCHOOL_ADMIN
INSERT INTO role_permissions (id, role, permission) VALUES
('d0000003-0000-0000-0000-000000000001', 'SCHOOL_ADMIN', 'EVIDENCING_VIEW'),
('d0000003-0000-0000-0000-000000000002', 'SCHOOL_ADMIN', 'EVIDENCING_CREATE'),
('d0000003-0000-0000-0000-000000000003', 'SCHOOL_ADMIN', 'EVIDENCING_EDIT_OWN'),
('d0000003-0000-0000-0000-000000000004', 'SCHOOL_ADMIN', 'EVIDENCING_DELETE_OWN');

-- SCHOOL_USER
INSERT INTO role_permissions (id, role, permission) VALUES
('d0000004-0000-0000-0000-000000000001', 'SCHOOL_USER', 'EVIDENCING_VIEW'),
('d0000004-0000-0000-0000-000000000002', 'SCHOOL_USER', 'EVIDENCING_CREATE'),
('d0000004-0000-0000-0000-000000000003', 'SCHOOL_USER', 'EVIDENCING_EDIT_OWN'),
('d0000004-0000-0000-0000-000000000004', 'SCHOOL_USER', 'EVIDENCING_DELETE_OWN');

-- COORDINATOR
INSERT INTO role_permissions (id, role, permission) VALUES
('d0000005-0000-0000-0000-000000000001', 'COORDINATOR', 'EVIDENCING_VIEW'),
('d0000005-0000-0000-0000-000000000002', 'COORDINATOR', 'EVIDENCING_CREATE'),
('d0000005-0000-0000-0000-000000000003', 'COORDINATOR', 'EVIDENCING_EDIT_OWN'),
('d0000005-0000-0000-0000-000000000004', 'COORDINATOR', 'EVIDENCING_DELETE_OWN');

-- ADMISSION_COORDINATOR
INSERT INTO role_permissions (id, role, permission) VALUES
('d0000006-0000-0000-0000-000000000001', 'ADMISSION_COORDINATOR', 'EVIDENCING_VIEW'),
('d0000006-0000-0000-0000-000000000002', 'ADMISSION_COORDINATOR', 'EVIDENCING_CREATE'),
('d0000006-0000-0000-0000-000000000003', 'ADMISSION_COORDINATOR', 'EVIDENCING_EDIT_OWN'),
('d0000006-0000-0000-0000-000000000004', 'ADMISSION_COORDINATOR', 'EVIDENCING_DELETE_OWN');

-- BRANCH_USER
INSERT INTO role_permissions (id, role, permission) VALUES
('d0000007-0000-0000-0000-000000000001', 'BRANCH_USER', 'EVIDENCING_VIEW'),
('d0000007-0000-0000-0000-000000000002', 'BRANCH_USER', 'EVIDENCING_CREATE'),
('d0000007-0000-0000-0000-000000000003', 'BRANCH_USER', 'EVIDENCING_EDIT_OWN'),
('d0000007-0000-0000-0000-000000000004', 'BRANCH_USER', 'EVIDENCING_DELETE_OWN');

-- NOTE: NANNY, PEON, DRIVER have no entries -> 100% Deny By Default!

-- Seed 15 Required Initial Categories
INSERT INTO post_categories (id, name, description, icon, is_active, display_order) VALUES
('ca000001-0000-0000-0000-000000000001', 'Classroom Learning', 'Daily academic lessons, experiments, group projects and classroom discussions', 'chalkboard', true, 1),
('ca000002-0000-0000-0000-000000000002', 'Travel & Field Trips', 'Excursions, educational tours, museum visits and experiential trips', 'bus', true, 2),
('ca000003-0000-0000-0000-000000000003', 'Sports & Physical Activities', 'Physical education, track events, sports matches and athletics', 'football', true, 3),
('ca000004-0000-0000-0000-000000000004', 'Art & Craft', 'Painting, sculpting, origami, pottery and craft projects', 'palette', true, 4),
('ca000005-0000-0000-0000-000000000005', 'Music & Dance', 'Choir, orchestra, instrumental practice and dance recitals', 'music', true, 5),
('ca000006-0000-0000-0000-000000000006', 'Science & Discovery', 'STEM labs, robotic experiments, nature explorations and inventions', 'flask', true, 6),
('ca000007-0000-0000-0000-000000000007', 'Language & Literacy', 'Reading circles, creative writing, spelling bees and storytelling', 'book-open', true, 7),
('ca000008-0000-0000-0000-000000000008', 'Numeracy & Mathematics', 'Math puzzles, problem solving, geometry and mental math challenges', 'calculator', true, 8),
('ca000009-0000-0000-0000-000000000009', 'Social & Emotional Development', 'Teamwork, peer mentorship, empathy exercises and mindfulness', 'heart', true, 9),
('ca000010-0000-0000-0000-000000000010', 'Celebrations & Cultural Events', 'Festival commemorations, heritage days, annual functions and drama', 'sparkles', true, 10),
('ca000011-0000-0000-0000-000000000011', 'Meals & Nutrition', 'Healthy snack times, cooking classes, lunch programs and nutrition guides', 'utensils', true, 11),
('ca000012-0000-0000-0000-000000000012', 'Health & Hygiene', 'Hand-washing drives, medical checkups, dental camps and wellness', 'shield-check', true, 12),
('ca000013-0000-0000-0000-000000000013', 'Outdoor Play & Nature', 'Recess games, gardening, environmental stewardship and nature walks', 'tree', true, 13),
('ca000014-0000-0000-0000-000000000014', 'Achievements & Milestones', 'Student awards, honors, competitions, certificates and milestones', 'trophy', true, 14),
('ca000015-0000-0000-0000-000000000015', 'General School Activities', 'Assemblies, campus announcements, parent interactions and notices', 'school', true, 15);

-- Seed Schools, Branches and Classes
INSERT INTO schools (id, name, code) VALUES
('e0000001-0000-0000-0000-000000000001', 'Oakridge International Academy', 'OAKRIDGE'),
('e0000002-0000-0000-0000-000000000002', 'St. Jude Grammar School', 'STJUDE');

INSERT INTO branches (id, school_id, name, code) VALUES
('b0000001-0000-0000-0000-000000000001', 'e0000001-0000-0000-0000-000000000001', 'Oakridge North Campus', 'OAK-NORTH'),
('b0000002-0000-0000-0000-000000000001', 'e0000001-0000-0000-0000-000000000001', 'Oakridge South Campus', 'OAK-SOUTH'),
('b0000003-0000-0000-0000-000000000002', 'e0000002-0000-0000-0000-000000000002', 'St. Jude Downtown', 'STJ-MAIN');

INSERT INTO classes (id, branch_id, name, grade_level, section) VALUES
('ce000001-0000-0000-0000-000000000001', 'b0000001-0000-0000-0000-000000000001', 'Grade 4A - Explorers', 'Grade 4', 'A'),
('ce000002-0000-0000-0000-000000000001', 'b0000001-0000-0000-0000-000000000001', 'Grade 5B - Innovators', 'Grade 5', 'B'),
('ce000003-0000-0000-0000-000000000002', 'b0000002-0000-0000-0000-000000000001', 'Grade 3A - Pioneers', 'Grade 3', 'A');

-- Seed Users and Scopes
INSERT INTO user_scopes (id, user_id, display_name, role, avatar_url, school_id, branch_id, class_id) VALUES
('ea000001-0000-0000-0000-000000000001', '11111111-1111-1111-1111-111111111111', 'Sarah Jenkins', 'TEACHER', 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150', 'e0000001-0000-0000-0000-000000000001', 'b0000001-0000-0000-0000-000000000001', 'ce000001-0000-0000-0000-000000000001'),
('ea000002-0000-0000-0000-000000000002', '22222222-2222-2222-2222-222222222222', 'Emily Clark', 'TEACHER', 'https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=150', 'e0000001-0000-0000-0000-000000000001', 'b0000001-0000-0000-0000-000000000001', 'ce000001-0000-0000-0000-000000000001'),
('ea000003-0000-0000-0000-000000000003', '33333333-3333-3333-3333-333333333333', 'Mark Wilson', 'TEACHER', 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150', 'e0000001-0000-0000-0000-000000000001', 'b0000001-0000-0000-0000-000000000001', 'ce000002-0000-0000-0000-000000000001'),
('ea000004-0000-0000-0000-000000000004', '44444444-4444-4444-4444-444444444444', 'Robert Davis', 'BRANCH_ADMIN', 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150', 'e0000001-0000-0000-0000-000000000001', 'b0000001-0000-0000-0000-000000000001', NULL),
('ea000005-0000-0000-0000-000000000005', '55555555-5555-5555-5555-555555555555', 'Jennifer Adams', 'SCHOOL_ADMIN', 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150', 'e0000001-0000-0000-0000-000000000001', 'b0000001-0000-0000-0000-000000000001', NULL),
('ea000006-0000-0000-0000-000000000006', '66666666-6666-6666-6666-666666666666', 'David Miller', 'TEACHER', 'https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?w=150', 'e0000001-0000-0000-0000-000000000001', 'b0000002-0000-0000-0000-000000000001', 'ce000003-0000-0000-0000-000000000002'),
('ea000007-0000-0000-0000-000000000007', '77777777-7777-7777-7777-777777777777', 'Mary Smith', 'NANNY', 'https://images.unsplash.com/photo-1580489944761-15a19d654956?w=150', 'e0000001-0000-0000-0000-000000000001', 'b0000001-0000-0000-0000-000000000001', NULL),
('ea000008-0000-0000-0000-000000000008', '88888888-8888-8888-8888-888888888888', 'John Brown', 'PEON', 'https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=150', 'e0000001-0000-0000-0000-000000000001', 'b0000001-0000-0000-0000-000000000001', NULL),
('ea000009-0000-0000-0000-000000000009', '99999999-9999-9999-9999-999999999999', 'James Garcia', 'DRIVER', 'https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=150', 'e0000001-0000-0000-0000-000000000001', 'b0000001-0000-0000-0000-000000000001', NULL),
('ea00000a-0000-0000-0000-00000000000a', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Lisa Martinez', 'ADMISSION_COORDINATOR', 'https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=150', 'e0000001-0000-0000-0000-000000000001', 'b0000001-0000-0000-0000-000000000001', NULL);

-- Seed Initial Media Assets
INSERT INTO media_assets (id, uploader_id, media_type, original_filename, content_type, file_size_bytes, storage_key, thumbnail_storage_key, width, height, duration_seconds, status) VALUES
('de000001-0000-0000-0000-000000000001', '11111111-1111-1111-1111-111111111111', 'IMAGE', 'science_lab_4a.jpg', 'image/jpeg', 2450000, 'media/science_lab_4a.jpg', 'media/thumb_science_lab_4a.jpg', 1920, 1080, NULL, 'READY'),
('de000002-0000-0000-0000-000000000002', '11111111-1111-1111-1111-111111111111', 'IMAGE', 'merit_awards_confidential.jpg', 'image/jpeg', 1890000, 'media/merit_awards_confidential.jpg', 'media/thumb_merit_awards_confidential.jpg', 1600, 1200, NULL, 'READY'),
('de000003-0000-0000-0000-000000000003', '22222222-2222-2222-2222-222222222222', 'IMAGE', 'pottery_exhibition_2026.jpg', 'image/jpeg', 3120000, 'media/pottery_exhibition_2026.jpg', 'media/thumb_pottery_exhibition_2026.jpg', 2048, 1536, NULL, 'READY'),
('de000004-0000-0000-0000-000000000004', '66666666-6666-6666-6666-666666666666', 'IMAGE', 'south_campus_sports.jpg', 'image/jpeg', 2800000, 'media/south_campus_sports.jpg', 'media/thumb_south_campus_sports.jpg', 1920, 1080, NULL, 'READY');

-- Seed Sample Posts
-- 1. Public Post by Sarah Jenkins (Grade 4A, Oakridge North)
INSERT INTO posts (id, author_id, school_id, branch_id, class_id, category_id, caption, visibility, publication_status, idempotency_key, published_at, version) VALUES
('f0000001-0000-0000-0000-000000000001', '11111111-1111-1111-1111-111111111111', 'e0000001-0000-0000-0000-000000000001', 'b0000001-0000-0000-0000-000000000001', 'ce000001-0000-0000-0000-000000000001', 'ca000001-0000-0000-0000-000000000001', 'Grade 4 students exploring density, buoyancy, and liquid displacement in our hands-on science lab today! 🌊🔬 Everybody had amazing insights and great teamwork!', 'PUBLIC', 'PUBLISHED', 'seed-post-001', CURRENT_TIMESTAMP - INTERVAL '2' HOUR, 0);

INSERT INTO post_media (id, post_id, media_asset_id, display_order) VALUES
('fa000001-0000-0000-0000-000000000001', 'f0000001-0000-0000-0000-000000000001', 'de000001-0000-0000-0000-000000000001', 0);

-- 2. Private Post by Sarah Jenkins (Tagged ONLY to Emily Clark; untagged users cannot view it!)
INSERT INTO posts (id, author_id, school_id, branch_id, class_id, category_id, caption, visibility, publication_status, idempotency_key, published_at, version) VALUES
('f0000002-0000-0000-0000-000000000002', '11111111-1111-1111-1111-111111111111', 'e0000001-0000-0000-0000-000000000001', 'b0000001-0000-0000-0000-000000000001', 'ce000001-0000-0000-0000-000000000001', 'ca000014-0000-0000-0000-000000000014', '🔒 Confidential Semester 1 Student Milestone Evaluations: Attached are the honor roll recommendations and customized learning plans for Grade 4A students.', 'PRIVATE', 'PUBLISHED', 'seed-post-002', CURRENT_TIMESTAMP - INTERVAL '1' HOUR, 0);

INSERT INTO post_media (id, post_id, media_asset_id, display_order) VALUES
('fa000002-0000-0000-0000-000000000002', 'f0000002-0000-0000-0000-000000000002', 'de000002-0000-0000-0000-000000000002', 0);

-- Emily Clark is tagged in Post 2
INSERT INTO post_recipients (id, post_id, user_id) VALUES
('fb000001-0000-0000-0000-000000000001', 'f0000002-0000-0000-0000-000000000002', '22222222-2222-2222-2222-222222222222');

-- 3. Public Post by Emily Clark (Art & Craft)
INSERT INTO posts (id, author_id, school_id, branch_id, class_id, category_id, caption, visibility, publication_status, idempotency_key, published_at, version) VALUES
('f0000003-0000-0000-0000-000000000003', '22222222-2222-2222-2222-222222222222', 'e0000001-0000-0000-0000-000000000001', 'b0000001-0000-0000-0000-000000000001', 'ce000001-0000-0000-0000-000000000001', 'ca000004-0000-0000-0000-000000000004', 'Annual Clay Sculptures & Modernist Pottery Showcase by Grade 4A students! Come by the west wing exhibition hall this Thursday to view their creations! 🎨🏺', 'PUBLIC', 'PUBLISHED', 'seed-post-003', CURRENT_TIMESTAMP - INTERVAL '30' MINUTE, 0);

INSERT INTO post_media (id, post_id, media_asset_id, display_order) VALUES
('fa000003-0000-0000-0000-000000000003', 'f0000003-0000-0000-0000-000000000003', 'de000003-0000-0000-0000-000000000003', 0);

-- 4. Public Post in South Campus (Branch 2) by David Miller
INSERT INTO posts (id, author_id, school_id, branch_id, class_id, category_id, caption, visibility, publication_status, idempotency_key, published_at, version) VALUES
('f0000004-0000-0000-0000-000000000004', '66666666-6666-6666-6666-666666666666', 'e0000001-0000-0000-0000-000000000001', 'b0000002-0000-0000-0000-000000000001', 'ce000003-0000-0000-0000-000000000002', 'ca000003-0000-0000-0000-000000000003', 'South Campus sports day athletics qualifiers underway! Inspiring energy and sportsmanship from our Grade 3 sprinters! 🏃‍♂️🥇', 'PUBLIC', 'PUBLISHED', 'seed-post-004', CURRENT_TIMESTAMP - INTERVAL '15' MINUTE, 0);

INSERT INTO post_media (id, post_id, media_asset_id, display_order) VALUES
('fa000004-0000-0000-0000-000000000004', 'f0000004-0000-0000-0000-000000000004', 'de000004-0000-0000-0000-000000000004', 0);

-- Seed Sample Likes and Comments
INSERT INTO post_likes (id, post_id, user_id) VALUES
('fc000001-0000-0000-0000-000000000001', 'f0000001-0000-0000-0000-000000000001', '22222222-2222-2222-2222-222222222222'),
('fc000002-0000-0000-0000-000000000001', 'f0000001-0000-0000-0000-000000000001', '44444444-4444-4444-4444-444444444444');

INSERT INTO post_comments (id, post_id, user_id, comment_text) VALUES
('fd000001-0000-0000-0000-000000000001', 'f0000001-0000-0000-0000-000000000001', '22222222-2222-2222-2222-222222222222', 'The students were so engaged! Fantastic practical demonstration Sarah.'),
('fd000002-0000-0000-0000-000000000001', 'f0000001-0000-0000-0000-000000000001', '44444444-4444-4444-4444-444444444444', 'Excellent pedagogical approach. Let us share this in the monthly branch newsletter.');
