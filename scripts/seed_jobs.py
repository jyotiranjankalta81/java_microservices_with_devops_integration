import json
import random
import urllib.request
import urllib.error

BASE_URL = "http://localhost:8080"

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

def register_and_login():
    reg_payload = {
        "email": "admin_seed@jobintel.com",
        "password": "Password123!",
        "fullName": "System Admin"
    }
    req = urllib.request.Request(
        f"{BASE_URL}/api/auth/register",
        data=json.dumps(reg_payload).encode('utf-8'),
        headers={'Content-Type': 'application/json'}
    )
    try:
        with urllib.request.urlopen(req) as resp:
            data = json.loads(resp.read().decode('utf-8'))
            print("Successfully registered admin_seed user!")
            return data["accessToken"]
    except urllib.error.HTTPError as e:
        # If already exists, login
        login_payload = {"email": "admin_seed@jobintel.com", "password": "Password123!"}
        lreq = urllib.request.Request(
            f"{BASE_URL}/api/auth/login",
            data=json.dumps(login_payload).encode('utf-8'),
            headers={'Content-Type': 'application/json'}
        )
        with urllib.request.urlopen(lreq) as lresp:
            ldata = json.loads(lresp.read().decode('utf-8'))
            print("Successfully logged in as admin_seed user!")
            return ldata["accessToken"]

def seed_jobs(token, count=100):
    created_count = 0
    for i in range(1, count + 1):
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
            "experienceMin": exp_min,
            "experienceMax": exp_max,
            "salaryMin": sal_min,
            "salaryMax": sal_max,
            "salaryCurrency": "USD",
            "employmentType": emp_type,
            "jobSource": random.choice(SOURCES),
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
                    if created_count % 10 == 0 or created_count == count:
                        print(f"[{created_count}/{count}] Dummy jobs created successfully.")
        except Exception as ex:
            print(f"Failed to create job #{i}: {ex}")

    print(f"\nCompleted seeding {created_count} jobs into the platform!")

if __name__ == "__main__":
    jwt_token = register_and_login()
    seed_jobs(jwt_token, 100)
