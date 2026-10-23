CREATE USER auth_user WITH PASSWORD 'auth_pass';
CREATE DATABASE auth_db OWNER auth_user;

CREATE USER job_user WITH PASSWORD 'job_pass';
CREATE DATABASE job_db OWNER job_user;

CREATE USER dashboard_user WITH PASSWORD 'dashboard_pass';
CREATE DATABASE dashboard_db OWNER dashboard_user;

CREATE USER export_user WITH PASSWORD 'export_pass';
CREATE DATABASE export_db OWNER export_user;

CREATE USER audit_user WITH PASSWORD 'audit_pass';
CREATE DATABASE audit_db OWNER audit_user;

CREATE USER evidencing_user WITH PASSWORD 'evidencing_pass';
CREATE DATABASE evidencing_db OWNER evidencing_user;
