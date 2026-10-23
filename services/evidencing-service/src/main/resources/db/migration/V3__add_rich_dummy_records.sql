-- ==============================================================================
-- V3__add_rich_dummy_records.sql
-- Rich dummy records for Evidencing Feed, Media Carousel, Video, and Notifications
-- ==============================================================================

-- 1. Upgrade V2 Seeded Media Assets to high-resolution educational Unsplash imagery
UPDATE media_assets 
SET storage_key = 'https://images.unsplash.com/photo-1532094349884-543bc11b234d?w=1200',
    thumbnail_storage_key = 'https://images.unsplash.com/photo-1532094349884-543bc11b234d?w=400'
WHERE id = 'de000001-0000-0000-0000-000000000001';

UPDATE media_assets 
SET storage_key = 'https://images.unsplash.com/photo-1434030216411-0b793f4b4173?w=1200',
    thumbnail_storage_key = 'https://images.unsplash.com/photo-1434030216411-0b793f4b4173?w=400'
WHERE id = 'de000002-0000-0000-0000-000000000002';

UPDATE media_assets 
SET storage_key = 'https://images.unsplash.com/photo-1565193566173-7a0ee3dbe261?w=1200',
    thumbnail_storage_key = 'https://images.unsplash.com/photo-1565193566173-7a0ee3dbe261?w=400'
WHERE id = 'de000003-0000-0000-0000-000000000003';

UPDATE media_assets 
SET storage_key = 'https://images.unsplash.com/photo-1461896836934-ffe607ba8211?w=1200',
    thumbnail_storage_key = 'https://images.unsplash.com/photo-1461896836934-ffe607ba8211?w=400'
WHERE id = 'de000004-0000-0000-0000-000000000004';


-- 2. Insert Additional Media Assets
INSERT INTO media_assets (id, uploader_id, media_type, original_filename, content_type, file_size_bytes, storage_key, thumbnail_storage_key, width, height, duration_seconds, status) VALUES
('de000005-0000-0000-0000-000000000005', '33333333-3333-3333-3333-333333333333', 'IMAGE', 'robotics_competition_grade5.jpg', 'image/jpeg', 3420000, 'https://images.unsplash.com/photo-1485827404703-89b55fcc595e?w=1200', 'https://images.unsplash.com/photo-1485827404703-89b55fcc595e?w=400', 1920, 1080, NULL, 'READY'),
('de000006-0000-0000-0000-000000000006', '11111111-1111-1111-1111-111111111111', 'IMAGE', 'planetarium_dome_visit.jpg', 'image/jpeg', 2890000, 'https://images.unsplash.com/photo-1518173946687-a4c8a383392e?w=1200', 'https://images.unsplash.com/photo-1518173946687-a4c8a383392e?w=400', 2048, 1365, NULL, 'READY'),
('de000007-0000-0000-0000-000000000007', '11111111-1111-1111-1111-111111111111', 'IMAGE', 'planetarium_telescope_demo.jpg', 'image/jpeg', 2540000, 'https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=1200', 'https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=400', 1920, 1280, NULL, 'READY'),
('de000008-0000-0000-0000-000000000008', '44444444-4444-4444-4444-444444444444', 'IMAGE', 'faculty_nominations_packet.jpg', 'image/jpeg', 1780000, 'https://images.unsplash.com/photo-1577896851231-70ef18881754?w=1200', 'https://images.unsplash.com/photo-1577896851231-70ef18881754?w=400', 1600, 1200, NULL, 'READY'),
('de000009-0000-0000-0000-000000000009', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'IMAGE', 'spring_garden_planting.jpg', 'image/jpeg', 3150000, 'https://images.unsplash.com/photo-1588072432836-e10032774350?w=1200', 'https://images.unsplash.com/photo-1588072432836-e10032774350?w=400', 1920, 1280, NULL, 'READY'),
('de000010-0000-0000-0000-000000000010', '55555555-5555-5555-5555-555555555555', 'VIDEO', 'choir_practice_rehearsal.mp4', 'video/mp4', 18450000, 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4', 'https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=400', 1920, 1080, 45, 'READY'),
('de000011-0000-0000-0000-000000000011', '22222222-2222-2222-2222-222222222222', 'IMAGE', 'rainbow_fruit_nutrition_day.jpg', 'image/jpeg', 2210000, 'https://images.unsplash.com/photo-1490818387583-1baba5e638af?w=1200', 'https://images.unsplash.com/photo-1490818387583-1baba5e638af?w=400', 1800, 1200, NULL, 'READY');


-- 3. Additional Posts

-- Post 5: Public Post by Mark Wilson (Grade 5B Innovators, Oakridge North Campus)
-- Domain: Science & Discovery (ca000006)
INSERT INTO posts (id, author_id, school_id, branch_id, class_id, category_id, caption, visibility, publication_status, idempotency_key, published_at, version) VALUES
('f0000005-0000-0000-0000-000000000005', '33333333-3333-3333-3333-333333333333', 'e0000001-0000-0000-0000-000000000001', 'b0000001-0000-0000-0000-000000000001', 'ce000002-0000-0000-0000-000000000001', 'ca000006-0000-0000-0000-000000000006', 'Grade 5 Robotics Club testing autonomous obstacle avoidance algorithms using ultrasonic sensors! 🤖⚡ Huge congratulations to Team Alpha for achieving zero collisions across the obstacle maze.', 'PUBLIC', 'PUBLISHED', 'seed-post-005', CURRENT_TIMESTAMP - INTERVAL '4' HOUR, 0);

INSERT INTO post_media (id, post_id, media_asset_id, display_order) VALUES
('fa000005-0000-0000-0000-000000000005', 'f0000005-0000-0000-0000-000000000005', 'de000005-0000-0000-0000-000000000005', 0);

-- Post 6: Multi-Media Carousel Public Post by Sarah Jenkins (Grade 4A, Oakridge North Campus)
-- Domain: Travel & Field Trips (ca000002) - 2 Images in Carousel!
INSERT INTO posts (id, author_id, school_id, branch_id, class_id, category_id, caption, visibility, publication_status, idempotency_key, published_at, version) VALUES
('f0000006-0000-0000-0000-000000000006', '11111111-1111-1111-1111-111111111111', 'e0000001-0000-0000-0000-000000000001', 'b0000001-0000-0000-0000-000000000001', 'ce000001-0000-0000-0000-000000000001', 'ca000002-0000-0000-0000-000000000002', 'Highlights from our Grade 4 experiential trip to the City Planetarium & Astronomy Pavilion! 🌌🔭 The students were captivated by the 360-degree celestial projection and real-time Mars rover telemetry simulations.', 'PUBLIC', 'PUBLISHED', 'seed-post-006', CURRENT_TIMESTAMP - INTERVAL '6' HOUR, 0);

INSERT INTO post_media (id, post_id, media_asset_id, display_order) VALUES
('fa000006-0000-0000-0000-000000000006', 'f0000006-0000-0000-0000-000000000006', 'de000006-0000-0000-0000-000000000006', 0),
('fa000007-0000-0000-0000-000000000007', 'f0000006-0000-0000-0000-000000000006', 'de000007-0000-0000-0000-000000000007', 1);

-- Post 7: Confidential Private Post by Robert Davis (Branch Admin)
-- Tagged ONLY to Jennifer Adams (School Admin) and Sarah Jenkins (Teacher)
-- Domain: Achievements & Milestones (ca000014)
INSERT INTO posts (id, author_id, school_id, branch_id, class_id, category_id, caption, visibility, publication_status, idempotency_key, published_at, version) VALUES
('f0000007-0000-0000-0000-000000000007', '44444444-4444-4444-4444-444444444444', 'e0000001-0000-0000-0000-000000000001', 'b0000001-0000-0000-0000-000000000001', NULL, 'ca000014-0000-0000-0000-000000000014', '🔒 Confidential Q3 Faculty Excellence Nominations: Attached is the review dossier for teacher commendations and pedagogical innovation grants. Please review before Friday''s leadership symposium.', 'PRIVATE', 'PUBLISHED', 'seed-post-007', CURRENT_TIMESTAMP - INTERVAL '8' HOUR, 0);

INSERT INTO post_media (id, post_id, media_asset_id, display_order) VALUES
('fa000008-0000-0000-0000-000000000008', 'f0000007-0000-0000-0000-000000000007', 'de000008-0000-0000-0000-000000000008', 0);

-- Tag Jennifer Adams and Sarah Jenkins into Post 7
INSERT INTO post_recipients (id, post_id, user_id) VALUES
('fb000002-0000-0000-0000-000000000002', 'f0000007-0000-0000-0000-000000000007', '55555555-5555-5555-5555-555555555555'),
('fb000003-0000-0000-0000-000000000003', 'f0000007-0000-0000-0000-000000000007', '11111111-1111-1111-1111-111111111111');

-- Post 8: Public Post by Lisa Martinez (Admission Coordinator)
-- Domain: Outdoor Play & Nature (ca000013)
INSERT INTO posts (id, author_id, school_id, branch_id, class_id, category_id, caption, visibility, publication_status, idempotency_key, published_at, version) VALUES
('f0000008-0000-0000-0000-000000000008', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'e0000001-0000-0000-0000-000000000001', 'b0000001-0000-0000-0000-000000000001', NULL, 'ca000013-0000-0000-0000-000000000013', 'Spring Campus Gardening & Ecology Initiative 🌱🌼! Our students planted organic sunflower seedlings and learned about soil aeration, composting, and pollinator habitats.', 'PUBLIC', 'PUBLISHED', 'seed-post-008', CURRENT_TIMESTAMP - INTERVAL '12' HOUR, 0);

INSERT INTO post_media (id, post_id, media_asset_id, display_order) VALUES
('fa000009-0000-0000-0000-000000000009', 'f0000008-0000-0000-0000-000000000008', 'de000009-0000-0000-0000-000000000009', 0);

-- Post 9: Public Video Post by Jennifer Adams (School Admin)
-- Domain: Music & Dance (ca000005)
INSERT INTO posts (id, author_id, school_id, branch_id, class_id, category_id, caption, visibility, publication_status, idempotency_key, published_at, version) VALUES
('f0000009-0000-0000-0000-000000000009', '55555555-5555-5555-5555-555555555555', 'e0000001-0000-0000-0000-000000000001', 'b0000001-0000-0000-0000-000000000001', NULL, 'ca000005-0000-0000-0000-000000000005', 'Rehearsal sneak peek! Oakridge Chamber Choir rehearsing Vivaldi''s Gloria ahead of next weekend''s Annual Arts Gala. Incredible vocal discipline and expression! 🎶🎻', 'PUBLIC', 'PUBLISHED', 'seed-post-009', CURRENT_TIMESTAMP - INTERVAL '18' HOUR, 0);

INSERT INTO post_media (id, post_id, media_asset_id, display_order) VALUES
('fa00000a-0000-0000-0000-00000000000a', 'f0000009-0000-0000-0000-000000000009', 'de000010-0000-0000-0000-000000000010', 0);

-- Post 10: Public Post by Emily Clark (Teacher)
-- Domain: Meals & Nutrition (ca000011)
INSERT INTO posts (id, author_id, school_id, branch_id, class_id, category_id, caption, visibility, publication_status, idempotency_key, published_at, version) VALUES
('f000000a-0000-0000-0000-00000000000a', '22222222-2222-2222-2222-222222222222', 'e0000001-0000-0000-0000-000000000001', 'b0000001-0000-0000-0000-000000000001', 'ce000001-0000-0000-0000-000000000001', 'ca000011-0000-0000-0000-000000000011', 'Rainbow Snack Day in Grade 4! 🍓🥝 Learning how different natural colors represent key antioxidants, dietary fiber, and healthy hydration habits.', 'PUBLIC', 'PUBLISHED', 'seed-post-010', CURRENT_TIMESTAMP - INTERVAL '1' DAY, 0);

INSERT INTO post_media (id, post_id, media_asset_id, display_order) VALUES
('fa00000b-0000-0000-0000-00000000000b', 'f000000a-0000-0000-0000-00000000000a', 'de000011-0000-0000-0000-000000000011', 0);


-- 4. Additional Likes
INSERT INTO post_likes (id, post_id, user_id) VALUES
('fc000003-0000-0000-0000-000000000001', 'f0000005-0000-0000-0000-000000000005', '11111111-1111-1111-1111-111111111111'),
('fc000004-0000-0000-0000-000000000001', 'f0000005-0000-0000-0000-000000000005', '44444444-4444-4444-4444-444444444444'),
('fc000005-0000-0000-0000-000000000001', 'f0000005-0000-0000-0000-000000000005', '55555555-5555-5555-5555-555555555555'),
('fc000006-0000-0000-0000-000000000001', 'f0000006-0000-0000-0000-000000000006', '22222222-2222-2222-2222-222222222222'),
('fc000007-0000-0000-0000-000000000001', 'f0000006-0000-0000-0000-000000000006', '33333333-3333-3333-3333-333333333333'),
('fc000008-0000-0000-0000-000000000001', 'f0000007-0000-0000-0000-000000000007', '55555555-5555-5555-5555-555555555555'),
('fc000009-0000-0000-0000-000000000001', 'f0000008-0000-0000-0000-000000000008', '11111111-1111-1111-1111-111111111111'),
('fc00000a-0000-0000-0000-000000000001', 'f0000009-0000-0000-0000-000000000009', '44444444-4444-4444-4444-444444444444');


-- 5. Additional Comments
INSERT INTO post_comments (id, post_id, user_id, comment_text) VALUES
('fd000003-0000-0000-0000-000000000001', 'f0000005-0000-0000-0000-000000000005', '11111111-1111-1111-1111-111111111111', 'The logic flow was impressive Mark! Loved how they debugged the corner turn issue.'),
('fd000004-0000-0000-0000-000000000001', 'f0000005-0000-0000-0000-000000000005', '44444444-4444-4444-4444-444444444444', 'Brilliant STEM demonstration. Let''s ensure this is presented at the district robotics fair.'),
('fd000005-0000-0000-0000-000000000001', 'f0000006-0000-0000-0000-000000000006', '22222222-2222-2222-2222-222222222222', 'The students were buzzing with excitement about the asteroid belt models all afternoon!'),
('fd000006-0000-0000-0000-000000000001', 'f0000007-0000-0000-0000-000000000007', '55555555-5555-5555-5555-555555555555', 'Received Robert. I have reviewed the teaching portfolio nominations and fully endorse the selections.'),
('fd000007-0000-0000-0000-000000000001', 'f0000009-0000-0000-0000-000000000009', '44444444-4444-4444-4444-444444444444', 'Superb acoustics and balance! Looking forward to the opening gala night.');


-- 6. Seed In-App Notifications
INSERT INTO evidencing_notifications (id, post_id, user_id, creator_id, creator_name, message, is_read, link) VALUES
('ee000001-0000-0000-0000-000000000001', 'f0000002-0000-0000-0000-000000000002', '22222222-2222-2222-2222-222222222222', '11111111-1111-1111-1111-111111111111', 'Sarah Jenkins', 'Sarah Jenkins tagged you in a private post: Confidential Semester 1 Student Milestone Evaluations', false, '/evidencing/f0000002-0000-0000-0000-000000000002'),
('ee000002-0000-0000-0000-000000000002', 'f0000007-0000-0000-0000-000000000007', '55555555-5555-5555-5555-555555555555', '44444444-4444-4444-4444-444444444444', 'Robert Davis', 'Robert Davis tagged you in a private post: Confidential Q3 Faculty Excellence Nominations', false, '/evidencing/f0000007-0000-0000-0000-000000000007'),
('ee000003-0000-0000-0000-000000000003', 'f0000007-0000-0000-0000-000000000007', '11111111-1111-1111-1111-111111111111', '44444444-4444-4444-4444-444444444444', 'Robert Davis', 'Robert Davis tagged you in a private post: Confidential Q3 Faculty Excellence Nominations', true, '/evidencing/f0000007-0000-0000-0000-000000000007'),
('ee000004-0000-0000-0000-000000000004', 'f0000005-0000-0000-0000-000000000005', '11111111-1111-1111-1111-111111111111', '33333333-3333-3333-3333-333333333333', 'Mark Wilson', 'Mark Wilson published a new post in Science & Discovery: Grade 5 Robotics Club', false, '/evidencing/f0000005-0000-0000-0000-000000000005');
