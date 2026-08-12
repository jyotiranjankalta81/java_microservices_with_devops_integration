import json
import random
import urllib.request
import urllib.error

BASE_URL = "http://localhost:8080"

FIRST_NAMES = [
    "Alex", "Jordan", "Taylor", "Morgan", "Casey", "Riley", "Avery", "Dakota", "Reese", "Quinn",
    "Chris", "Sam", "Pat", "Jamie", "Robin", "Jesse", "Skyler", "Cameron", "Drew", "Logan",
    "Adrian", "Rowan", "Hayden", "Kai", "Emerson", "Finley", "Harper", "Peyton", "Eden", "Sage",
    "Shiloh", "River", "Rory", "Milan", "Amari", "Sloan", "Lennon", "Remy", "Oakley", "Marlowe",
    "Ellis", "Soren", "Tatum", "August", "Linden", "Dallas", "Phoenix", "Zephyr", "Kieran", "Nico"
]

LAST_NAMES = [
    "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis", "Rodriguez", "Martinez",
    "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson", "Thomas", "Taylor", "Moore", "Jackson", "Martin",
    "Lee", "Perez", "Thompson", "White", "Harris", "Sanchez", "Clark", "Ramirez", "Lewis", "Robinson",
    "Walker", "Young", "Allen", "King", "Wright", "Scott", "Torres", "Nguyen", "Hill", "Flores",
    "Green", "Adams", "Nelson", "Baker", "Hall", "Rivera", "Campbell", "Mitchell", "Carter", "Roberts"
]

COMPANIES = [
    "Google", "Meta", "Amazon", "Apple", "Microsoft", "Netflix", "OpenAI", "Stripe",
    "Databricks", "Snowflake", "Palantir", "Uber", "Airbnb", "Spotify", "Shopify",
    "Salesforce", "Oracle", "IBM", "Intel", "Nvidia", "AMD", "Twilio", "Cloudflare", "GitHub"
]

ROLES = [
    "Senior Java Engineer", "Backend Architect", "Full Stack Developer", "ML Infra Engineer",
    "Cloud DevOps Specialist", "Data Engineer", "Cyber Security Analyst", "Frontend Architect",
    "AI Research Scientist", "Site Reliability Engineer", "Systems Software Engineer", "Product Manager"
]

SKILLS_POOL = [
    "Java", "Spring Boot", "Python", "TypeScript", "React", "Node.js", "Go", "Rust",
    "SQL", "Kafka", "Docker", "Kubernetes", "AWS", "GCP", "PostgreSQL", "Redis",
    "Microservices", "GraphQL", "PyTorch", "TensorFlow", "Spark", "CI/CD"
]

TECH_POOL = [
    "PostgreSQL", "Kafka", "Docker", "Kubernetes", "Prometheus", "Grafana", "Jaeger",
    "Spring Cloud", "React 18", "Vite", "Tailwind CSS", "Elasticsearch", "Cassandra", "MongoDB"
]

CITIES = [
    ("USA", "San Francisco"), ("USA", "New York"), ("USA", "Austin"), ("USA", "Seattle"),
    ("Canada", "Toronto"), ("Canada", "Vancouver"),
    ("UK", "London"), ("Germany", "Berlin"), ("Germany", "Munich"),
    ("India", "Bangalore"), ("India", "Hyderabad")
]

WORK_MODES = ["REMOTE", "HYBRID", "ONSITE"]
EMPLOYMENT_TYPES = ["FULL_TIME", "CONTRACT", "PART_TIME"]
SOURCES = ["LinkedIn", "Indeed", "Company Portal", "Glassdoor", "Direct Referral"]
TAGS_POOL = ["Urgent", "Hot Opportunity", "Remote Friendly", "High Pay", "Tier 1 Company", "AI Focused"]

def seed_users(count=50):
    users = []
    registered_count = 0
    print(f"Starting registration for {count} users...")
    
    for i in range(1, count + 1):
        fn = FIRST_NAMES[(i - 1) % len(FIRST_NAMES)]
        ln = LAST_NAMES[(i - 1) % len(LAST_NAMES)]
        full_name = f"{fn} {ln}"
        email = f"user_{i}@jobintel.com"
        password = "Password123!"

        reg_payload = {
            "email": email,
            "password": password,
            "fullName": full_name
        }

        req = urllib.request.Request(
            f"{BASE_URL}/api/auth/register",
            data=json.dumps(reg_payload).encode('utf-8'),
            headers={'Content-Type': 'application/json'}
        )

        try:
            with urllib.request.urlopen(req) as resp:
                data = json.loads(resp.read().decode('utf-8'))
                token = data["accessToken"]
                users.append({"email": email, "token": token, "name": full_name})
                registered_count += 1
        except urllib.error.HTTPError as e:
            # If already exists, login
            login_payload = {"email": email, "password": password}
            lreq = urllib.request.Request(
                f"{BASE_URL}/api/auth/login",
                data=json.dumps(login_payload).encode('utf-8'),
                headers={'Content-Type': 'application/json'}
            )
            try:
                with urllib.request.urlopen(lreq) as lresp:
                    ldata = json.loads(lresp.read().decode('utf-8'))
                    token = ldata["accessToken"]
                    users.append({"email": email, "token": token, "name": full_name})
                    registered_count += 1
            except Exception as lex:
                print(f"Failed to login user {email}: {lex}")
        except Exception as ex:
            print(f"Failed to register user {email}: {ex}")

        if i % 10 == 0 or i == count:
            print(f"[{registered_count}/{count}] Users registered/verified successfully.")

    return users

def seed_jobs(users, count=100):
    created_count = 0
    print(f"\nStarting job creation for {count} jobs distributed across {len(users)} users...")

    for i in range(1, count + 1):
        user = users[(i - 1) % len(users)]
        token = user["token"]

        company = random.choice(COMPANIES)
        role = random.choice(ROLES)
        title = f"{role} - {company}"
        country, city = random.choice(CITIES)
        work_mode = random.choice(WORK_MODES)
        emp_type = random.choice(EMPLOYMENT_TYPES)
        skills = random.sample(SKILLS_POOL, k=random.randint(3, 6))
        techs = random.sample(TECH_POOL, k=random.randint(2, 4))
        tags = random.sample(TAGS_POOL, k=random.randint(1, 3))
        
        sal_min = random.randint(90, 160) * 1000
        sal_max = sal_min + random.randint(30, 80) * 1000
        exp_min = random.randint(1, 4)
        exp_max = exp_min + random.randint(3, 6)

        job_data = {
            "title": title,
            "company": company,
            "role": role,
            "description": f"We are seeking an exceptional {role} to join {company} in building next-gen scalable systems.",
            "skills": skills,
            "technologies": techs,
            "country": country,
            "city": city,
            "workMode": work_mode,
            "employmentType": emp_type,
            "experienceMin": exp_min,
            "experienceMax": exp_max,
            "salaryMin": sal_min,
            "salaryMax": sal_max,
            "salaryCurrency": "USD",
            "jobSource": random.choice(SOURCES),
            "jobUrl": f"https://www.{company.lower().replace(' ', '')}.com/careers/{i}",
            "applyUrl": f"https://careers.{company.lower().replace(' ', '')}.com/apply/{i}",
            "status": "ACTIVE",
            "tags": tags
        }

        req = urllib.request.Request(
            f"{BASE_URL}/api/jobs",
            data=json.dumps(job_data).encode('utf-8'),
            headers={
                'Content-Type': 'application/json',
                'Authorization': f'Bearer {token}'
            }
        )

        try:
            with urllib.request.urlopen(req) as resp:
                if resp.status in (200, 201):
                    created_count += 1
        except Exception as ex:
            print(f"Failed to create job #{i} for user {user['email']}: {ex}")

        if i % 10 == 0 or i == count:
            print(f"[{created_count}/{count}] Jobs created successfully.")

    print(f"\nSuccessfully seeded {len(users)} users and {created_count} jobs!")

if __name__ == "__main__":
    user_list = seed_users(50)
    if user_list:
        seed_jobs(user_list, 100)
