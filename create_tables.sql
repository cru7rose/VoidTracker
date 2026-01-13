-- Create Vehicle Profiles Table
CREATE TABLE IF NOT EXISTS planning_vehicle_profiles (
    id VARCHAR(255) NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    max_capacity_weight DOUBLE PRECISION NOT NULL,
    max_capacity_volume DOUBLE PRECISION NOT NULL
);

-- Create Capabilities Collection Table
CREATE TABLE IF NOT EXISTS planning_vehicle_profiles_capabilities (
    vehicle_profile_entity_id VARCHAR(255) NOT NULL,
    capabilities VARCHAR(255),
    CONSTRAINT fk_profile_capabilities FOREIGN KEY (vehicle_profile_entity_id) REFERENCES planning_vehicle_profiles(id)
);

-- Create Carrier Compliance Table
CREATE TABLE IF NOT EXISTS planning_carrier_compliance (
    carrier_id VARCHAR(255) NOT NULL PRIMARY KEY,
    is_insured BOOLEAN NOT NULL,
    insurance_expiry_date DATE,
    compliance_status VARCHAR(255) NOT NULL
);

-- Alter Fleet Vehicles Table to add new columns
ALTER TABLE planning_fleet_vehicles ADD COLUMN IF NOT EXISTS profile_id VARCHAR(255);
ALTER TABLE planning_fleet_vehicles ADD COLUMN IF NOT EXISTS carrier_id VARCHAR(255);

-- Add available column (nullable with default)
ALTER TABLE planning_fleet_vehicles ADD COLUMN IF NOT EXISTS available BOOLEAN;
UPDATE planning_fleet_vehicles SET available = true WHERE available IS NULL;
ALTER TABLE planning_fleet_vehicles ALTER COLUMN available SET DEFAULT true;

-- Fix communication_logs columns (make nullable first, then set defaults)
ALTER TABLE communication_logs ADD COLUMN IF NOT EXISTS channel VARCHAR(255);
UPDATE communication_logs SET channel = 'UNKNOWN' WHERE channel IS NULL;
ALTER TABLE communication_logs ALTER COLUMN channel SET DEFAULT 'UNKNOWN';

ALTER TABLE communication_logs ADD COLUMN IF NOT EXISTS recipient VARCHAR(255);
UPDATE communication_logs SET recipient = 'UNKNOWN' WHERE recipient IS NULL;
ALTER TABLE communication_logs ALTER COLUMN recipient SET DEFAULT 'UNKNOWN';

-- Fix planning_solutions solution_data/solution_json column (check both names)
DO $$
BEGIN
    -- Try solution_data first (current entity name)
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'planning_solutions' AND column_name = 'solution_data') THEN
        ALTER TABLE planning_solutions ADD COLUMN solution_data JSONB;
        UPDATE planning_solutions SET solution_data = '{}'::jsonb WHERE solution_data IS NULL;
    END IF;
    -- Also handle solution_json if it exists
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'planning_solutions' AND column_name = 'solution_json') THEN
        UPDATE planning_solutions SET solution_json = '{}'::jsonb WHERE solution_json IS NULL;
    END IF;
END $$;
