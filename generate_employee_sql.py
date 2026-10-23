import random

first_names = ["James", "Mary", "John", "Patricia", "Robert", "Jennifer", "Michael", "Linda", "William", "Elizabeth",
               "David", "Barbara", "Richard", "Susan", "Joseph", "Jessica", "Thomas", "Sarah", "Charles", "Karen",
               "Christopher", "Nancy", "Daniel", "Lisa", "Matthew", "Betty", "Anthony", "Margaret", "Donald", "Sandra",
               "Mark", "Ashley", "Paul", "Kimberly", "Steven", "Emily", "Andrew", "Donna", "Kenneth", "Michelle",
               "Joshua", "Carol", "Kevin", "Amanda", "Brian", "Dorothy", "George", "Melissa", "Edward", "Deborah",
               "Jyoti", "Amit", "Priya", "Rahul", "Ananya", "Rajesh", "Sunita", "Vikram", "Neha", "Sanjay"]

last_names = ["Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis", "Rodriguez", "Martinez",
              "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson", "Thomas", "Taylor", "Moore", "Jackson", "Martin",
              "Lee", "Perez", "Thompson", "White", "Harris", "Sanchez", "Clark", "Ramirez", "Lewis", "Robinson",
              "Walker", "Young", "Allen", "King", "Wright", "Scott", "Torres", "Nguyen", "Hill", "Flores",
              "Kalta", "Sharma", "Verma", "Patel", "Singh", "Gupta", "Mehta", "Rao", "Joshi", "Nair"]

cities_states = [
    ("New York", "NY", "USA", "10001"),
    ("San Francisco", "CA", "USA", "94105"),
    ("Seattle", "WA", "USA", "98101"),
    ("Austin", "TX", "USA", "78701"),
    ("Chicago", "IL", "USA", "60601"),
    ("Boston", "MA", "USA", "02108"),
    ("Los Angeles", "CA", "USA", "90001"),
    ("Denver", "CO", "USA", "80202"),
    ("Miami", "FL", "USA", "33101"),
    ("Atlanta", "GA", "USA", "30301"),
    ("Toronto", "ON", "Canada", "M5V 2T6"),
    ("London", "ENG", "UK", "EC1A 1BB"),
    ("Bangalore", "KA", "India", "560001"),
    ("Mumbai", "MH", "India", "400001")
]

roles_depts = [
    ("Software Engineer", "Engineering", 75000, 115000),
    ("Senior Software Engineer", "Engineering", 120000, 160000),
    ("Staff Software Engineer", "Engineering", 165000, 210000),
    ("DevOps Engineer", "DevOps", 85000, 140000),
    ("QA Automation Engineer", "Quality Assurance", 65000, 105000),
    ("Product Manager", "Product", 95000, 150000),
    ("UI/UX Designer", "Design", 70000, 120000),
    ("Data Scientist", "Data & AI", 90000, 155000),
    ("HR Specialist", "Human Resources", 55000, 85000),
    ("HR Manager", "Human Resources", 85000, 125000),
    ("Financial Analyst", "Finance", 68000, 110000),
    ("System Architect", "Architecture", 145000, 195000)
]

statuses = ["ACTIVE", "ACTIVE", "ACTIVE", "ACTIVE", "ACTIVE", "ON_LEAVE", "ONBOARDING"]

lines = []
lines.append("-- Flyway Migration V2__create_employee_table.sql")
lines.append("CREATE TABLE IF NOT EXISTS employee (")
lines.append("    id SERIAL PRIMARY KEY,")
lines.append("    f_name VARCHAR(100) NOT NULL,")
lines.append("    last_name VARCHAR(100) NOT NULL,")
lines.append("    email VARCHAR(150) NOT NULL UNIQUE,")
lines.append("    phone VARCHAR(30),")
lines.append("    address VARCHAR(255),")
lines.append("    pic VARCHAR(255),")
lines.append("    city VARCHAR(100),")
lines.append("    state VARCHAR(100),")
lines.append("    country VARCHAR(100),")
lines.append("    pin VARCHAR(20),")
lines.append("    salary NUMERIC(12, 2) NOT NULL,")
lines.append("    role VARCHAR(50) NOT NULL,")
lines.append("    department VARCHAR(100) NOT NULL,")
lines.append("    hire_date DATE NOT NULL,")
lines.append("    status VARCHAR(20) DEFAULT 'ACTIVE',")
lines.append("    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,")
lines.append("    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP")
lines.append(");")
lines.append("")
lines.append("CREATE INDEX IF NOT EXISTS idx_employee_email ON employee (email);")
lines.append("CREATE INDEX IF NOT EXISTS idx_employee_role ON employee (role);")
lines.append("CREATE INDEX IF NOT EXISTS idx_employee_department ON employee (department);")
lines.append("")

random.seed(42)

inserts = []
for i in range(1, 201):
    fn = random.choice(first_names).replace("'", "''")
    ln = random.choice(last_names).replace("'", "''")
    
    email_candidate = f"{fn.lower()}.{ln.lower()}{i}@jobintel.com"
    phone = f"+1-{random.randint(201, 999)}-{random.randint(100, 999)}-{random.randint(1000, 9999)}"
    street_num = random.randint(100, 9999)
    street_name = random.choice(["Main St", "Market St", "Tech Pkwy", "Broadway", "5th Ave", "Oak St", "Pine St", "Cedar Ave"])
    address = f"{street_num} {street_name}, Suite {random.randint(100, 900)}".replace("'", "''")
    pic = f"https://api.dicebear.com/7.x/avataaars/svg?seed={fn}{ln}{i}"
    city, state, country, pin = random.choice(cities_states)
    
    role, dept, min_sal, max_sal = random.choice(roles_depts)
    salary = round(random.uniform(min_sal, max_sal), 2)
    
    year = random.randint(2020, 2026)
    month = random.randint(1, 12)
    day = random.randint(1, 28)
    hire_date = f"{year}-{month:02d}-{day:02d}"
    status = random.choice(statuses)
    
    row = f"INSERT INTO employee (f_name, last_name, email, phone, address, pic, city, state, country, pin, salary, role, department, hire_date, status) VALUES ('{fn}', '{ln}', '{email_candidate}', '{phone}', '{address}', '{pic}', '{city}', '{state}', '{country}', '{pin}', {salary}, '{role}', '{dept}', '{hire_date}', '{status}') ON CONFLICT (email) DO NOTHING;"
    inserts.append(row)

sql_content = "\n".join(lines) + "\n" + "\n".join(inserts)
target_path = "/Users/jyotiranjankalta/Projects/job-intelligence-platform/services/auth-service/src/main/resources/db/migration/V2__create_employee_table.sql"
with open(target_path, "w") as f:
    f.write(sql_content)

print(f"Generated clean Flyway SQL migration file at {target_path} 🟢")
