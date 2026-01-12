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
