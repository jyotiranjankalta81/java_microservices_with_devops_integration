-- V5: Add apply_url column to jobs table
ALTER TABLE jobs ADD COLUMN IF NOT EXISTS apply_url VARCHAR(1000);
