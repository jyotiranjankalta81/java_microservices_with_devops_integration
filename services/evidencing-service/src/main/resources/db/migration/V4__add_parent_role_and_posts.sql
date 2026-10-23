-- ==============================================================================
-- V4__add_parent_role_and_posts.sql
-- Seed Parent Role, Parent User Scopes, Posts with Parent Engagement & Notifications
-- ==============================================================================

-- 1. Grant EVIDENCING_VIEW permission to PARENT role
INSERT INTO role_permissions (id, role, permission) VALUES
('d0000008-0000-0000-0000-000000000001', 'PARENT', 'EVIDENCING_VIEW');

-- 2. Seed Parent User Scopes in Oakridge Academy
INSERT INTO user_scopes (id, user_id, display_name, role, avatar_url, school_id, branch_id, class_id) VALUES
('ea00000b-0000-0000-0000-000000000001', 'bbbb0001-0000-0000-0000-000000000001', 'Michael Anderson', 'PARENT', 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150', 'e0000001-0000-0000-0000-000000000001', 'b0000001-0000-0000-0000-000000000001', 'ce000001-0000-0000-0000-000000000001'),
('ea00000c-0000-0000-0000-000000000002', 'bbbb0002-0000-0000-0000-000000000002', 'Sophia Taylor', 'PARENT', 'https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=150', 'e0000001-0000-0000-0000-000000000001', 'b0000001-0000-0000-0000-000000000001', 'ce000002-0000-0000-0000-000000000001'),
('ea00000d-0000-0000-0000-000000000003', 'bbbb0003-0000-0000-0000-000000000003', 'Lucas Martinez', 'PARENT', 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150', 'e0000001-0000-0000-0000-000000000001', 'b0000001-0000-0000-0000-000000000001', 'ce000001-0000-0000-0000-000000000001'),
('ea00000e-0000-0000-0000-000000000004', 'bbbb0004-0000-0000-0000-000000000004', 'Elena Rostova', 'PARENT', 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150', 'e0000001-0000-0000-0000-000000000001', 'b0000002-0000-0000-0000-000000000001', 'ce000003-0000-0000-0000-000000000002');

-- 3. Media Assets for New Posts
INSERT INTO media_assets (id, uploader_id, media_type, original_filename, content_type, file_size_bytes, storage_key, thumbnail_storage_key, width, height, duration_seconds, status) VALUES
('de000012-0000-0000-0000-000000000012', '11111111-1111-1111-1111-111111111111', 'IMAGE', 'math_olympiad_grade4.jpg', 'image/jpeg', 2650000, 'https://images.unsplash.com/photo-1596495578065-6e0763fa1178?w=1200', 'https://images.unsplash.com/photo-1596495578065-6e0763fa1178?w=400', 1920, 1280, NULL, 'READY'),
('de000013-0000-0000-0000-000000000013', '11111111-1111-1111-1111-111111111111', 'IMAGE', 'parent_conference_portfolio.jpg', 'image/jpeg', 1980000, 'https://images.unsplash.com/photo-1454165804606-c3d57bc86b40?w=1200', 'https://images.unsplash.com/photo-1454165804606-c3d57bc86b40?w=400', 1600, 1200, NULL, 'READY'),
('de000014-0000-0000-0000-000000000014', '55555555-5555-5555-5555-555555555555', 'IMAGE', 'heritage_pageant_stage.jpg', 'image/jpeg', 3240000, 'https://images.unsplash.com/photo-1507676184212-d03ab07a01bf?w=1200', 'https://images.unsplash.com/photo-1507676184212-d03ab07a01bf?w=400', 1920, 1080, NULL, 'READY'),
('de000015-0000-0000-0000-000000000015', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'IMAGE', 'pediatric_screening_camp.jpg', 'image/jpeg', 2410000, 'https://images.unsplash.com/photo-1576091160399-112ba8d25d1d?w=1200', 'https://images.unsplash.com/photo-1576091160399-112ba8d25d1d?w=400', 1800, 1200, NULL, 'READY');

-- 4. Additional Posts

-- Post 11: Public Post by Sarah Jenkins (Grade 4A, Oakridge North Campus)
-- Domain: Numeracy & Mathematics (ca000008)
INSERT INTO posts (id, author_id, school_id, branch_id, class_id, category_id, caption, visibility, publication_status, idempotency_key, published_at, version) VALUES
('f000000b-0000-0000-0000-00000000000b', '11111111-1111-1111-1111-111111111111', 'e0000001-0000-0000-0000-000000000001', 'b0000001-0000-0000-0000-000000000001', 'ce000001-0000-0000-0000-000000000001', 'ca000008-0000-0000-0000-000000000008', 'Mental Math Sprint & Math Olympiad qualifiers! 🧮🏆 Our Grade 4 Explorers solved over 50 mental calculation cards in record time. Parents, please take a moment to see their colorful geometry tessellations exhibited in the central lobby!', 'PUBLIC', 'PUBLISHED', 'seed-post-011', CURRENT_TIMESTAMP - INTERVAL '2' DAY, 0);

INSERT INTO post_media (id, post_id, media_asset_id, display_order) VALUES
('fa00000c-0000-0000-0000-00000000000c', 'f000000b-0000-0000-0000-00000000000b', 'de000012-0000-0000-0000-000000000012', 0);

-- Post 12: Private Post by Sarah Jenkins
-- Tagged ONLY to Parents: Michael Anderson & Lucas Martinez
-- Domain: Social & Emotional Development (ca000009)
INSERT INTO posts (id, author_id, school_id, branch_id, class_id, category_id, caption, visibility, publication_status, idempotency_key, published_at, version) VALUES
('f000000c-0000-0000-0000-00000000000c', '11111111-1111-1111-1111-111111111111', 'e0000001-0000-0000-0000-000000000001', 'b0000001-0000-0000-0000-000000000001', 'ce000001-0000-0000-0000-000000000001', 'ca000009-0000-0000-0000-000000000009', '🔒 Confidential Term 1 Individual Learning Portfolio & Conference Notes: Attached are the peer collaboration observations and personalized milestone roadmaps ahead of tomorrow''s parent-teacher conference.', 'PRIVATE', 'PUBLISHED', 'seed-post-012', CURRENT_TIMESTAMP - INTERVAL '3' DAY, 0);

INSERT INTO post_media (id, post_id, media_asset_id, display_order) VALUES
('fa00000d-0000-0000-0000-00000000000d', 'f000000c-0000-0000-0000-00000000000c', 'de000013-0000-0000-0000-000000000013', 0);

-- Tag parents Michael Anderson and Lucas Martinez into Post 12
INSERT INTO post_recipients (id, post_id, user_id) VALUES
('fb000004-0000-0000-0000-000000000004', 'f000000c-0000-0000-0000-00000000000c', 'bbbb0001-0000-0000-0000-000000000001'),
('fb000005-0000-0000-0000-000000000005', 'f000000c-0000-0000-0000-00000000000c', 'bbbb0003-0000-0000-0000-000000000003');

-- Post 13: Public Post by Jennifer Adams (School Admin)
-- Domain: Celebrations & Cultural Events (ca000010)
INSERT INTO posts (id, author_id, school_id, branch_id, class_id, category_id, caption, visibility, publication_status, idempotency_key, published_at, version) VALUES
('f000000d-0000-0000-0000-00000000000d', '55555555-5555-5555-5555-555555555555', 'e0000001-0000-0000-0000-000000000001', 'b0000001-0000-0000-0000-000000000001', NULL, 'ca000010-0000-0000-0000-000000000010', 'Spectacular performances at the Oakridge Annual Cultural Pageant! 🎭✨ Students celebrated traditions from 12 countries through drama, traditional dance, and orchestra. Heartfelt gratitude to all parent volunteers who crafted the traditional attire!', 'PUBLIC', 'PUBLISHED', 'seed-post-013', CURRENT_TIMESTAMP - INTERVAL '4' DAY, 0);

INSERT INTO post_media (id, post_id, media_asset_id, display_order) VALUES
('fa00000e-0000-0000-0000-00000000000e', 'f000000d-0000-0000-0000-00000000000d', 'de000014-0000-0000-0000-000000000014', 0);

-- Post 14: Public Post by Lisa Martinez (Coordinator)
-- Domain: Health & Hygiene (ca000012)
INSERT INTO posts (id, author_id, school_id, branch_id, class_id, category_id, caption, visibility, publication_status, idempotency_key, published_at, version) VALUES
('f000000e-0000-0000-0000-00000000000e', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'e0000001-0000-0000-0000-000000000001', 'b0000001-0000-0000-0000-000000000001', NULL, 'ca000012-0000-0000-0000-000000000012', 'Pediatric Vision & Oral Health Screening Camp completed today with Dr. Reynolds and team 🩺🦷. Individual health report cards will be distributed in Friday home folders for parent records.', 'PUBLIC', 'PUBLISHED', 'seed-post-014', CURRENT_TIMESTAMP - INTERVAL '5' DAY, 0);

INSERT INTO post_media (id, post_id, media_asset_id, display_order) VALUES
('fa00000f-0000-0000-0000-00000000000f', 'f000000e-0000-0000-0000-00000000000e', 'de000015-0000-0000-0000-000000000015', 0);

-- 5. Likes from Parents & Teachers
INSERT INTO post_likes (id, post_id, user_id) VALUES
('fc00000b-0000-0000-0000-000000000001', 'f000000b-0000-0000-0000-00000000000b', 'bbbb0001-0000-0000-0000-000000000001'),
('fc00000c-0000-0000-0000-000000000001', 'f000000b-0000-0000-0000-00000000000b', 'bbbb0003-0000-0000-0000-000000000003'),
('fc00000d-0000-0000-0000-000000000001', 'f000000b-0000-0000-0000-00000000000b', '22222222-2222-2222-2222-222222222222'),
('fc00000e-0000-0000-0000-000000000001', 'f000000c-0000-0000-0000-00000000000c', 'bbbb0001-0000-0000-0000-000000000001'),
('fc00000f-0000-0000-0000-000000000001', 'f000000d-0000-0000-0000-00000000000d', 'bbbb0001-0000-0000-0000-000000000001'),
('fc000010-0000-0000-0000-000000000001', 'f000000d-0000-0000-0000-00000000000d', 'bbbb0002-0000-0000-0000-000000000002');

-- 6. Comments from Parents & Teachers
INSERT INTO post_comments (id, post_id, user_id, comment_text) VALUES
('fd000008-0000-0000-0000-000000000001', 'f000000b-0000-0000-0000-00000000000b', 'bbbb0001-0000-0000-0000-000000000001', 'Thank you Ms. Jenkins! Maya was so proud of solving the speed challenges and showed us all her geometry tessellations at home!'),
('fd000009-0000-0000-0000-000000000001', 'f000000b-0000-0000-0000-00000000000b', 'bbbb0003-0000-0000-0000-000000000003', 'Phenomenal dedication. The hands-on problem cards have made math so engaging and fun this year.'),
('fd00000a-0000-0000-0000-000000000001', 'f000000c-0000-0000-0000-00000000000c', 'bbbb0001-0000-0000-0000-000000000001', 'Received the portfolio observations, Ms. Jenkins. We will see you tomorrow morning at 10:00 AM for the conference.'),
('fd00000b-0000-0000-0000-000000000001', 'f000000d-0000-0000-0000-00000000000d', 'bbbb0002-0000-0000-0000-000000000002', 'The stage setups and musical performances were breathtaking! Huge round of applause to all educators and students!');

-- 7. In-App Notifications for Parents
INSERT INTO evidencing_notifications (id, post_id, user_id, creator_id, creator_name, message, is_read, link) VALUES
('ee000005-0000-0000-0000-000000000005', 'f000000c-0000-0000-0000-00000000000c', 'bbbb0001-0000-0000-0000-000000000001', '11111111-1111-1111-1111-111111111111', 'Sarah Jenkins', 'Sarah Jenkins tagged you in a private post: Confidential Term 1 Individual Learning Portfolio & Conference Notes', false, '/evidencing/f000000c-0000-0000-0000-00000000000c'),
('ee000006-0000-0000-0000-000000000006', 'f000000c-0000-0000-0000-00000000000c', 'bbbb0003-0000-0000-0000-000000000003', '11111111-1111-1111-1111-111111111111', 'Sarah Jenkins', 'Sarah Jenkins tagged you in a private post: Confidential Term 1 Individual Learning Portfolio & Conference Notes', false, '/evidencing/f000000c-0000-0000-0000-00000000000c'),
('ee000007-0000-0000-0000-000000000007', 'f000000d-0000-0000-0000-00000000000d', 'bbbb0002-0000-0000-0000-000000000002', '55555555-5555-5555-5555-555555555555', 'Jennifer Adams', 'Jennifer Adams published a new post in Celebrations & Cultural Events: Oakridge Cultural Pageant', false, '/evidencing/f000000d-0000-0000-0000-00000000000d');
