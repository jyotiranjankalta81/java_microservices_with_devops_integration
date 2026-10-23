#!/usr/bin/env python3
"""
Seed Large-Scale Evidencing Data:
- 1,050+ Users across various roles (Teachers, Parents, Admins, Coordinators, Nannies, Peons, Drivers)
- 20+ Classes across branches
- 9,990 Posts (~10,000 posts) across 15 categories with realistic captions, timestamps, media assets, likes, comments, and private post tags
"""

import uuid
import random
from datetime import datetime, timedelta

# Fix seed for reproducibility
random.seed(42)

BCRYPT_PASSWORD_HASH = "$2a$12$OVPVQcVnSfMWbW5r7IYBvuQkFBpYTy/XHro4t35NoSW4E4YrL6AUi" # Password123!

SCHOOLS = [
    {"id": "e0000001-0000-0000-0000-000000000001", "name": "Oakridge International Academy"},
    {"id": "e0000002-0000-0000-0000-000000000002", "name": "St. Jude Grammar School"}
]

BRANCHES = [
    {"id": "b0000001-0000-0000-0000-000000000001", "school_id": "e0000001-0000-0000-0000-000000000001", "name": "Oakridge North Campus"},
    {"id": "b0000002-0000-0000-0000-000000000001", "school_id": "e0000001-0000-0000-0000-000000000001", "name": "Oakridge South Campus"},
    {"id": "b0000003-0000-0000-0000-000000000002", "school_id": "e0000002-0000-0000-0000-000000000002", "name": "St. Jude Downtown"}
]

# Existing classes
EXISTING_CLASSES = [
    {"id": "ce000001-0000-0000-0000-000000000001", "branch_id": "b0000001-0000-0000-0000-000000000001", "name": "Grade 4A - Explorers", "grade": "Grade 4", "sec": "A"},
    {"id": "ce000002-0000-0000-0000-000000000001", "branch_id": "b0000001-0000-0000-0000-000000000001", "name": "Grade 5B - Innovators", "grade": "Grade 5", "sec": "B"},
    {"id": "ce000003-0000-0000-0000-000000000002", "branch_id": "b0000002-0000-0000-0000-000000000001", "name": "Grade 3A - Pioneers", "grade": "Grade 3", "sec": "A"}
]

CATEGORIES = [
    {"id": "ca000001-0000-0000-0000-000000000001", "name": "Classroom Learning"},
    {"id": "ca000002-0000-0000-0000-000000000002", "name": "Travel & Field Trips"},
    {"id": "ca000003-0000-0000-0000-000000000003", "name": "Sports & Physical Activities"},
    {"id": "ca000004-0000-0000-0000-000000000004", "name": "Art & Craft"},
    {"id": "ca000005-0000-0000-0000-000000000005", "name": "Music & Dance"},
    {"id": "ca000006-0000-0000-0000-000000000006", "name": "Science & Discovery"},
    {"id": "ca000007-0000-0000-0000-000000000007", "name": "Language & Literacy"},
    {"id": "ca000008-0000-0000-0000-000000000008", "name": "Numeracy & Mathematics"},
    {"id": "ca000009-0000-0000-0000-000000000009", "name": "Social & Emotional Development"},
    {"id": "ca000010-0000-0000-0000-000000000010", "name": "Celebrations & Cultural Events"},
    {"id": "ca000011-0000-0000-0000-000000000011", "name": "Meals & Nutrition"},
    {"id": "ca000012-0000-0000-0000-000000000012", "name": "Health & Hygiene"},
    {"id": "ca000013-0000-0000-0000-000000000013", "name": "Outdoor Play & Nature"},
    {"id": "ca000014-0000-0000-0000-000000000014", "name": "Achievements & Milestones"},
    {"id": "ca000015-0000-0000-0000-000000000015", "name": "General School Activities"}
]

SAMPLE_IMAGES = [
    "https://images.unsplash.com/photo-1532094349884-543bc11b234d?w=1200", # science
    "https://images.unsplash.com/photo-1434030216411-0b793f4b4173?w=1200", # learning
    "https://images.unsplash.com/photo-1565193566173-7a0ee3dbe261?w=1200", # pottery
    "https://images.unsplash.com/photo-1461896836934-ffe607ba8211?w=1200", # sports
    "https://images.unsplash.com/photo-1485827404703-89b55fcc595e?w=1200", # robotics
    "https://images.unsplash.com/photo-1509062522246-3755977927d7?w=1200", # teacher classroom
    "https://images.unsplash.com/photo-1503676260728-1c00da094a0b?w=1200", # kids reading
    "https://images.unsplash.com/photo-1577896851231-70ef18881754?w=1200", # school library
    "https://images.unsplash.com/photo-1516627145497-ae6968895b74?w=1200", # kids art
    "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=1200", # music recital
    "https://images.unsplash.com/photo-1526976668912-1a811878dd37?w=1200", # outdoor play
    "https://images.unsplash.com/photo-1576267423445-b2e0074d68a4?w=1200", # mathematics
    "https://images.unsplash.com/photo-1529156069898-49953e39b3ac?w=1200", # celebration
    "https://images.unsplash.com/photo-1498837167922-ddd27525d352?w=1200", # healthy meals
    "https://images.unsplash.com/photo-1471286174890-9c112ffca56a?w=1200"  # athletics field
]

FIRST_NAMES = [
    "Liam", "Olivia", "Noah", "Emma", "Oliver", "Charlotte", "Elijah", "Amelia", "James", "Ava",
    "William", "Sophia", "Benjamin", "Isabella", "Lucas", "Mia", "Henry", "Evelyn", "Theodore", "Harper",
    "Jack", "Camila", "Levi", "Gianna", "Alexander", "Abigail", "Jackson", "Luna", "Mateo", "Ella",
    "Daniel", "Elizabeth", "Michael", "Sofia", "Mason", "Emily", "Sebastian", "Avery", "Ethan", "Mila",
    "Logan", "Arya", "Owen", "Chloe", "Samuel", "Layla", "Jacob", "Penelope", "Asher", "Grace",
    "Aiden", "Victoria", "John", "Riley", "Joseph", "Nora", "David", "Lily", "Leo", "Eleanor",
    "Luke", "Hannah", "Julian", "Lillian", "Hudson", "Addison", "Grayson", "Aubrey", "Matthew", "Ellie",
    "Ezra", "Stella", "Gabriel", "Natalie", "Carter", "Zoe", "Isaac", "Leah", "Jayden", "Hazel",
    "Anthony", "Violet", "Dylan", "Aurora", "Lincoln", "Savannah", "Thomas", "Audrey", "Maverick", "Brooklyn",
    "Elias", "Bella", "Josiah", "Claire", "Charles", "Skylar", "Caleb", "Lucy", "Christopher", "Paisley",
    "Ezekiel", "Everly", "Miles", "Anna", "Jaxon", "Caroline", "Isaiah", "Nova", "Andrew", "Genesis",
    "Joshua", "Emilia", "Nathan", "Kennedy", "Nolan", "Samantha", "Adrian", "Maya", "Cameron", "Willow",
    "Santiago", "Kinsley", "Eli", "Naomi", "Aaron", "Aaliyah", "Ryan", "Elena", "Angel", "Sarah"
]

LAST_NAMES = [
    "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis", "Rodriguez", "Martinez",
    "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson", "Thomas", "Taylor", "Moore", "Jackson", "Martin",
    "Lee", "Perez", "Thompson", "White", "Harris", "Sanchez", "Clark", "Ramirez", "Lewis", "Robinson",
    "Walker", "Young", "Allen", "King", "Wright", "Scott", "Torres", "Nguyen", "Hill", "Flores",
    "Green", "Adams", "Nelson", "Baker", "Hall", "Rivera", "Campbell", "Mitchell", "Carter", "Roberts",
    "Gomez", "Phillips", "Evans", "Turner", "Diaz", "Parker", "Cruz", "Edwards", "Collins", "Reyes",
    "Stewart", "Morris", "Morales", "Murphy", "Cook", "Rogers", "Gutierrez", "Ortiz", "Morgan", "Cooper",
    "Peterson", "Bailey", "Reed", "Kelly", "Howard", "Ramos", "Kim", "Cox", "Ward", "Richardson",
    "Watson", "Brooks", "Chavez", "Wood", "James", "Bennett", "Gray", "Mendoza", "Ruiz", "Hughes",
    "Price", "Alvarez", "Castillo", "Sanders", "Patel", "Myers", "Long", "Ross", "Foster", "Jimenez"
]

CAPTIONS_BY_CATEGORY = {
    "Classroom Learning": [
        "Interactive morning workshop exploring historical civilizations through peer presentations! The collaboration was inspiring.",
        "Students engaged in hands-on inquiry learning, drafting hypotheses and presenting evidence to their classmates today.",
        "Fostering independent inquiry and collaborative problem solving during our guided classroom seminar.",
        "Deep dive into world geography today! Our learners created relief maps and shared cultural insights with enthusiasm.",
        "Peer-to-peer reciprocal teaching session. Watching learners articulate concepts to their classmates reinforces genuine mastery."
    ],
    "Travel & Field Trips": [
        "Exciting heritage field trip to the National Science Museum! Exploring interactive flight and astronomy exhibits.",
        "Experiential learning expedition to the botanical conservatory. Students cataloged indigenous flora and ecosystem adaptations.",
        "Field visit to the historical observatory. An unforgettable starry session bridging classroom physics with real-world astronomy.",
        "Field trip to the city municipal archive and heritage press. Hands-on typography and historical document restoration!",
        "Annual ecological discovery tour at the nature reserve. Identifying water shed conservation techniques in person."
    ],
    "Sports & Physical Activities": [
        "Inter-house track & field qualifiers this morning! Outstanding athletic spirit, teamwork, and personal best records.",
        "Energetic football tournament semi-finals today! Hard work, discipline, and sportsmanship on full display.",
        "Morning gymnastics and balance beam agility training. Developing core coordination, poise, and confidence.",
        "Basketball dribbling drills and defensive set-pieces under sunny skies. Great camaraderie among the squads!",
        "Swimming gala preparations: relay heats and precision stroke refinement in the campus aquatic center."
    ],
    "Art & Craft": [
        "Pottery & clay modeling studio: students sculpted expressive terracotta figurines inspired by ancient folklore.",
        "Vibrant watercolor landscapes capturing seasonal flora changes. The creative brushwork and chromatic harmony were breathtaking.",
        "Recycled paper mache sculptures and sustainable design showcase! Blending environmental awareness with artistic ingenuity.",
        "Batik textile dyeing and geometric pattern experimentation in today's visual arts atelier.",
        "Abstract expressionism canvas session: exploring emotions, textures, and mixed media compositions."
    ],
    "Music & Dance": [
        "Harmonious choir rehearsal preparing for the annual spring symphony! Pitch accuracy and rhythm in perfect synchronization.",
        "Classical ensemble violin and cello sectional practice. Melodic progression and discipline shining through every piece.",
        "Traditional cultural dance choreography practice: colorful costumes, rhythmic footwork, and celebratory energy.",
        "Percussion workshop: exploring complex polyrhythms and Afro-Caribbean beat patterns with hand drums.",
        "Modern creative dance workshop exploring spatial movement, emotional expression, and group coordination."
    ],
    "Science & Discovery": [
        "Exciting exothermic reaction experiments in chemistry lab! Safety goggles on and curiosity at an all-time high.",
        "Cell biology microscopy lab: observing onion root mitosis and plant cell vacuole structures under 400x magnification.",
        "Robotics & automation lab: programming microcontrollers to navigate obstacle mazes using ultrasonic distance sensors.",
        "Hydroponic garden installation: monitoring nutrient pH balances and automated drip irrigation growth cycles.",
        "Solar system physics: measuring pendulum oscillations and calculating gravitational acceleration with precision."
    ],
    "Language & Literacy": [
        "Book club seminar discussing comparative themes in classic literature. Thoughtful perspectives and lively discourse!",
        "Creative creative writing showcase: original poetry reading and flash-fiction recitals under the library canopy.",
        "Spelling bee quarter-finals: extraordinary vocabulary retention, etymology analysis, and calm under pressure.",
        "Reader's theater adaptation: bringing historical dialogues to life with expressive cadence and dramatic flair.",
        "Journalistic writing workshop: drafting investigative editorial articles for the upcoming campus monthly gazette."
    ],
    "Numeracy & Mathematics": [
        "Geometry & architectural design challenge: constructing geodesic dome scale models using proportional trigonometry.",
        "Mental math speed Olympiad! Quick algebraic factorizations and clever modular arithmetic strategies.",
        "Applied statistics project: collecting empirical survey data on campus biodiversity and plotting bell-curve distributions.",
        "Fraction feast: hands-on culinary math calculating recipe scaling, ratios, and unit conversions.",
        "Logic puzzle matrices and algorithmic graph theory puzzles during today's advanced numeracy circle."
    ],
    "Social & Emotional Development": [
        "Morning circle time reflecting on empathy, active listening, and conflict resolution strategies.",
        "Gratitude journaling and mindfulness breathing exercise to cultivate emotional balance and focus.",
        "Team-building trust fall and cooperative bridge-building tasks reinforcing mutual respect across cohorts.",
        "Peer mentorship launch: older students guiding junior peers through study organization and friendship skills.",
        "Celebration of kindness week: students exchanging handwritten recognition notes for acts of consideration."
    ],
    "Celebrations & Cultural Events": [
        "Spectacular Annual Cultural Festival! Honoring global diversity with traditional garments, cuisine, and folklore.",
        "Independence & Constitution Day parade: flag hoisting, student patriotic songs, and historical tableaux presentations.",
        "Grandparents Day assembly: heartwarming musical tributes, shared family stories, and handmade keepsake cards.",
        "Earth Day carnival: student-led environmental awareness booths, sapling distribution, and zero-waste demonstrations.",
        "Founders' Day commemorative gala celebrating decades of academic excellence and community heritage."
    ],
    "Meals & Nutrition": [
        "Farm-to-fork nutrition workshop: understanding balanced macronutrients, dietary fiber, and fresh hydration.",
        "Campus kitchen garden harvest! Organic carrots, lettuce, and bell peppers incorporated into today's wholesome lunch.",
        "Culinary science: baking whole-grain sourdough while examining yeast fermentation kinetics.",
        "Hydration & wellness awareness day: designing infused fruit-water recipes and tracking daily water intake.",
        "Mindful eating circle: appreciating nutrient origins, portion awareness, and culinary traditions worldwide."
    ],
    "Health & Hygiene": [
        "Hands-on dental hygiene seminar led by guest pediatric orthodontists. Proper brushing technique demonstration!",
        "First aid & emergency readiness certification: learning basic CPR compression rhythms and bandage wrapping.",
        "Personal ergonomics workshop: posture alignment, healthy screen distance, and desk stretching routines.",
        "Campus-wide cleanliness and sanitization drive: reinforcing community hygiene and shared responsibility.",
        "Eye health checkup and vision wellness screening conducted in our campus healthcare pavilion."
    ],
    "Outdoor Play & Nature": [
        "Nature trail exploration: documenting seasonal bird migrations and leaf vein structures in the arboretum.",
        "Mud kitchen and sensory sandbox play: tactile exploration, water flow physics, and joyful laughter.",
        "Campus orchard tree-planting initiative! Each cohort adopted and watered their own indigenous sapling.",
        "Orienteering and compass navigation adventure through the campus woodland perimeter.",
        "Outdoor butterfly garden observation: witnessing caterpillar pupation and chrysalis metamorphosis up close."
    ],
    "Achievements & Milestones": [
        "Hearty congratulations to our Math Olympiad medalists! Recognized at today's honors convocation.",
        "Certificate of Merit ceremony for outstanding academic progress, punctuality, and community leadership.",
        "Young Authors Award: student published short-story anthologies displayed proudly in the main library showcase.",
        "STEM Innovation Challenge trophy winners! Celebrated for their solar-powered water filtration prototype.",
        "Graduation milestone preview: senior cohort cap-and-gown portrait session and retrospective memory wall."
    ],
    "General School Activities": [
        "Morning all-school assembly: inspiring address on perseverance, curiosity, and civic responsibility.",
        "Inter-house debate championship: articulate arguments on sustainable urbanization and clean energy transitions.",
        "Campus open house & student portfolio exhibition: welcoming prospective families and alumni to our learning spaces.",
        "Annual book fair kick-off! Excited readers discovering new literary worlds across hundreds of curated titles.",
        "Student council induction and pledge ceremony: congratulations to our newly elected school prefects and captains!"
    ]
}

COMMENTS_POOL = [
    "So proud of the dedication shown by all students in this project!",
    "Wonderful learning experience! Thank you for sharing these precious moments.",
    "The creativity and depth of thought here is truly remarkable.",
    "Inspiring to see such authentic inquiry and teamwork in action.",
    "Thank you teacher for organizing such an engaging and thoughtful activity!",
    "My child was talking enthusiastically about this all evening at dinner!",
    "Fantastic initiative! Keep encouraging these innovative minds.",
    "Great milestone achieved. Kudos to the entire class and educator team!",
    "Love the focus on hands-on practical application of classroom theories.",
    "Brilliant work! So glad our school champions holistic development."
]

def generate():
    print("Starting generation of large-scale evidencing data...")

    # 1. Generate additional classes (20 classes)
    classes = list(EXISTING_CLASSES)
    additional_classes_sql = []
    
    grade_levels = [
        ("Kindergarten", "K"),
        ("Grade 1", "1"),
        ("Grade 2", "2"),
        ("Grade 3", "3"),
        ("Grade 4", "4"),
        ("Grade 5", "5"),
        ("Grade 6", "6"),
        ("Grade 7", "7"),
        ("Grade 8", "8"),
        ("Grade 9", "9")
    ]
    
    class_id_idx = 10
    for branch in BRANCHES:
        for gl_name, gl_code in grade_levels[:4]:
            for sec in ["A", "B"]:
                cid = f"ce0000{class_id_idx:02d}-0000-0000-0000-000000000001"
                cname = f"{gl_name} {sec} - Section"
                classes.append({
                    "id": cid,
                    "branch_id": branch["id"],
                    "name": cname,
                    "grade": gl_name,
                    "sec": sec
                })
                additional_classes_sql.append(
                    f"INSERT INTO classes (id, branch_id, name, grade_level, section, created_at) "
                    f"VALUES ('{cid}', '{branch['id']}', '{cname}', '{gl_name}', '{sec}', now()) ON CONFLICT (id) DO NOTHING;"
                )
                class_id_idx += 1

    # 2. Generate 1,050 users
    # Breakdown:
    # 350 Teachers
    # 450 Parents
    # 30 Branch Admins
    # 20 School Admins
    # 60 Coordinators
    # 40 Admission Coordinators
    # 35 Nannies
    # 35 Peons
    # 30 Drivers
    # Total = 1,050

    role_quotas = [
        ("TEACHER", 350),
        ("PARENT", 450),
        ("BRANCH_ADMIN", 30),
        ("SCHOOL_ADMIN", 20),
        ("COORDINATOR", 60),
        ("ADMISSION_COORDINATOR", 40),
        ("NANNY", 35),
        ("PEON", 35),
        ("DRIVER", 30)
    ]

    generated_users = []
    auth_users_sql = []
    user_scopes_sql = []

    user_counter = 100
    for role, count in role_quotas:
        for i in range(count):
            user_counter += 1
            uid = f"u{user_counter:07d}-0000-0000-0000-{role[:8].lower():<12}"
            uid = str(uuid.uuid5(uuid.NAMESPACE_DNS, f"user-{user_counter}-{role}"))
            
            first = random.choice(FIRST_NAMES)
            last = random.choice(LAST_NAMES)
            full_name = f"{first} {last}"
            email = f"{first.lower()}.{last.lower()}.{user_counter}@oakridge.edu"
            if role == "PARENT":
                email = f"{first.lower()}.{last.lower()}.{user_counter}@gmail.com"

            # Auth db role
            auth_role = "ADMIN" if role in ["SCHOOL_ADMIN", "BRANCH_ADMIN"] else "USER"

            # Assign school & branch
            school = random.choice(SCHOOLS)
            school_branches = [b for b in BRANCHES if b["school_id"] == school["id"]]
            branch = random.choice(school_branches)

            # Assign class if teacher or parent
            assigned_class = None
            if role in ["TEACHER", "PARENT"]:
                branch_classes = [c for c in classes if c["branch_id"] == branch["id"]]
                if branch_classes:
                    assigned_class = random.choice(branch_classes)

            avatar_idx = random.randint(1, 70)
            gender = "men" if user_counter % 2 == 0 else "women"
            avatar_url = f"https://randomuser.me/api/portraits/{gender}/{avatar_idx}.jpg"

            generated_users.append({
                "id": uid,
                "email": email,
                "name": full_name,
                "role": role,
                "auth_role": auth_role,
                "school_id": school["id"],
                "branch_id": branch["id"],
                "class_id": assigned_class["id"] if assigned_class else None,
                "avatar_url": avatar_url
            })

            escaped_name = full_name.replace("'", "''")
            auth_users_sql.append(
                f"('{uid}', '{email}', '{BCRYPT_PASSWORD_HASH}', '{escaped_name}', '{auth_role}', true, now(), now())"
            )

            class_val = f"'{assigned_class['id']}'" if assigned_class else "NULL"
            user_scopes_sql.append(
                f"('{uid}', '{uid}', '{escaped_name}', '{role}', '{avatar_url}', '{school['id']}', '{branch['id']}', {class_val}, now())"
            )

    print(f"Generated {len(generated_users)} users.")

    # 3. Generate 9,990 Posts
    # Target: 9,990 posts
    # Authors: Teachers, School Admins, Branch Admins, Coordinators
    author_candidates = [u for u in generated_users if u["role"] in ["TEACHER", "SCHOOL_ADMIN", "BRANCH_ADMIN", "COORDINATOR"]]
    # Also include existing sample teachers/admins
    existing_author_id = "11111111-1111-1111-1111-111111111111" # Sarah Jenkins

    TOTAL_POSTS = 9990
    print(f"Generating {TOTAL_POSTS} posts and related media/interactions...")

    now = datetime.utcnow()
    
    media_assets_sql = []
    posts_sql = []
    post_media_sql = []
    post_recipients_sql = []
    post_likes_sql = []
    post_comments_sql = []

    # Pre-select parents for private post tagging
    parents_by_branch = {}
    for u in generated_users:
        if u["role"] == "PARENT":
            parents_by_branch.setdefault(u["branch_id"], []).append(u)

    for p_idx in range(1, TOTAL_POSTS + 1):
        post_id = str(uuid.uuid5(uuid.NAMESPACE_DNS, f"post-{p_idx}"))
        
        # 10% from existing Sarah Jenkins, 90% from generated staff
        if p_idx % 10 == 0:
            author_id = existing_author_id
            author_school = "e0000001-0000-0000-0000-000000000001"
            author_branch = "b0000001-0000-0000-0000-000000000001"
            author_class = "ce000001-0000-0000-0000-000000000001"
        else:
            author = random.choice(author_candidates)
            author_id = author["id"]
            author_school = author["school_id"]
            author_branch = author["branch_id"]
            author_class = author["class_id"]

        cat = random.choice(CATEGORIES)
        cat_captions = CAPTIONS_BY_CATEGORY[cat["name"]]
        base_caption = random.choice(cat_captions)
        caption = f"[{cat['name']}] #{p_idx} - {base_caption}"

        # Visibility: ~88% Public, ~12% Private
        is_private = (p_idx % 8 == 0)
        visibility = "PRIVATE" if is_private else "PUBLIC"

        # Timestamp: spread across past 180 days
        days_ago = (p_idx / TOTAL_POSTS) * 180.0
        post_time = now - timedelta(days=days_ago, minutes=random.randint(0, 1400))
        post_time_str = post_time.isoformat() + "Z"

        idempotency_key = f"seed-post-{p_idx}"

        class_val = f"'{author_class}'" if author_class else "NULL"
        escaped_caption = caption.replace("'", "''")
        posts_sql.append(
            f"('{post_id}', '{author_id}', '{author_school}', '{author_branch}', {class_val}, "
            f"'{cat['id']}', '{escaped_caption}', '{visibility}', 'PUBLISHED', "
            f"'{idempotency_key}', '{post_time_str}', '{post_time_str}', '{post_time_str}', 0)"
        )

        # Generate 1 or 2 media assets for this post
        num_media = 2 if (p_idx % 4 == 0) else 1
        for m_order in range(num_media):
            media_id = str(uuid.uuid5(uuid.NAMESPACE_DNS, f"media-{p_idx}-{m_order}"))
            pm_id = str(uuid.uuid5(uuid.NAMESPACE_DNS, f"postmedia-{p_idx}-{m_order}"))
            img_url = random.choice(SAMPLE_IMAGES)
            thumb_url = img_url.replace("w=1200", "w=400")

            media_assets_sql.append(
                f"('{media_id}', '{author_id}', 'IMAGE', 'activity_{p_idx}_{m_order}.jpg', 'image/jpeg', "
                f"1048576, '{img_url}', '{thumb_url}', 1200, 800, NULL, 'READY', NULL, '{post_time_str}', '{post_time_str}')"
            )

            post_media_sql.append(
                f"('{pm_id}', '{post_id}', '{media_id}', {m_order}, '{post_time_str}')"
            )

        # Tag recipients for PRIVATE posts (tag 1 to 3 parents from the same branch)
        if is_private:
            branch_parents = parents_by_branch.get(author_branch, [])
            if branch_parents:
                sample_count = min(random.randint(1, 3), len(branch_parents))
                tagged = random.sample(branch_parents, sample_count)
                for tag_user in tagged:
                    rec_id = str(uuid.uuid5(uuid.NAMESPACE_DNS, f"recip-{p_idx}-{tag_user['id']}"))
                    post_recipients_sql.append(
                        f"('{rec_id}', '{post_id}', '{tag_user['id']}', '{post_time_str}')"
                    )

        # Likes: 0 to 12 likes per post
        num_likes = random.randint(0, 10)
        if num_likes > 0 and len(generated_users) >= num_likes:
            likers = random.sample(generated_users[:100], num_likes)
            for liker in likers:
                like_id = str(uuid.uuid5(uuid.NAMESPACE_DNS, f"like-{p_idx}-{liker['id']}"))
                post_likes_sql.append(
                    f"('{like_id}', '{post_id}', '{liker['id']}', '{post_time_str}')"
                )

        # Comments: 0 to 3 comments per post
        if p_idx % 2 == 0:
            num_comments = random.randint(1, 3)
            commenters = random.sample(generated_users[:80], num_comments)
            for c_i, commenter in enumerate(commenters):
                cid = str(uuid.uuid5(uuid.NAMESPACE_DNS, f"comment-{p_idx}-{c_i}"))
                comm_text = random.choice(COMMENTS_POOL)
                escaped_comm = comm_text.replace("'", "''")
                post_comments_sql.append(
                    f"('{cid}', '{post_id}', '{commenter['id']}', '{escaped_comm}', '{post_time_str}', '{post_time_str}')"
                )

    print("Writing SQL files...")

    # Write auth_db seed SQL
    with open("/tmp/seed_auth_users.sql", "w") as f:
        f.write("-- Seed 1,050 users into auth_db\n")
        f.write("INSERT INTO users (id, email, password_hash, full_name, role, enabled, created_at, updated_at) VALUES\n")
        f.write(",\n".join(auth_users_sql))
        f.write("\nON CONFLICT (email) DO NOTHING;\n")

    print("Wrote /tmp/seed_auth_users.sql")

    # Write evidencing_db classes & user_scopes SQL
    with open("/tmp/seed_evidencing_users.sql", "w") as f:
        f.write("-- Additional classes\n")
        f.write("\n".join(additional_classes_sql))
        f.write("\n\n-- User scopes\n")
        # Split into batches of 500 for fast execution
        BATCH = 500
        for b_start in range(0, len(user_scopes_sql), BATCH):
            b_chunk = user_scopes_sql[b_start:b_start + BATCH]
            f.write("INSERT INTO user_scopes (id, user_id, display_name, role, avatar_url, school_id, branch_id, class_id, created_at) VALUES\n")
            f.write(",\n".join(b_chunk))
            f.write("\nON CONFLICT (id) DO NOTHING;\n\n")

    print("Wrote /tmp/seed_evidencing_users.sql")

    # Write evidencing_db posts & media in chunks
    with open("/tmp/seed_evidencing_posts.sql", "w") as f:
        BATCH = 1000

        # Media assets
        f.write("-- Media Assets\n")
        for b_start in range(0, len(media_assets_sql), BATCH):
            b_chunk = media_assets_sql[b_start:b_start + BATCH]
            f.write("INSERT INTO media_assets (id, uploader_id, media_type, original_filename, content_type, file_size_bytes, storage_key, thumbnail_storage_key, width, height, duration_seconds, status, error_message, created_at, updated_at) VALUES\n")
            f.write(",\n".join(b_chunk))
            f.write("\nON CONFLICT (id) DO NOTHING;\n\n")

        # Posts
        f.write("-- Posts\n")
        for b_start in range(0, len(posts_sql), BATCH):
            b_chunk = posts_sql[b_start:b_start + BATCH]
            f.write("INSERT INTO posts (id, author_id, school_id, branch_id, class_id, category_id, caption, visibility, publication_status, idempotency_key, published_at, created_at, updated_at, version) VALUES\n")
            f.write(",\n".join(b_chunk))
            f.write("\nON CONFLICT (id) DO NOTHING;\n\n")

        # Post media links
        f.write("-- Post Media\n")
        for b_start in range(0, len(post_media_sql), BATCH):
            b_chunk = post_media_sql[b_start:b_start + BATCH]
            f.write("INSERT INTO post_media (id, post_id, media_asset_id, display_order, created_at) VALUES\n")
            f.write(",\n".join(b_chunk))
            f.write("\nON CONFLICT (id) DO NOTHING;\n\n")

        # Recipients
        if post_recipients_sql:
            f.write("-- Post Recipients\n")
            for b_start in range(0, len(post_recipients_sql), BATCH):
                b_chunk = post_recipients_sql[b_start:b_start + BATCH]
                f.write("INSERT INTO post_recipients (id, post_id, user_id, created_at) VALUES\n")
                f.write(",\n".join(b_chunk))
                f.write("\nON CONFLICT (id) DO NOTHING;\n\n")

        # Likes
        if post_likes_sql:
            f.write("-- Post Likes\n")
            for b_start in range(0, len(post_likes_sql), 2000):
                b_chunk = post_likes_sql[b_start:b_start + 2000]
                f.write("INSERT INTO post_likes (id, post_id, user_id, created_at) VALUES\n")
                f.write(",\n".join(b_chunk))
                f.write("\nON CONFLICT (id) DO NOTHING;\n\n")

        # Comments
        if post_comments_sql:
            f.write("-- Post Comments\n")
            for b_start in range(0, len(post_comments_sql), 1000):
                b_chunk = post_comments_sql[b_start:b_start + 1000]
                f.write("INSERT INTO post_comments (id, post_id, user_id, comment_text, created_at, updated_at) VALUES\n")
                f.write(",\n".join(b_chunk))
                f.write("\nON CONFLICT (id) DO NOTHING;\n\n")

    print("Wrote /tmp/seed_evidencing_posts.sql")
    print("Generation complete!")

if __name__ == "__main__":
    generate()
