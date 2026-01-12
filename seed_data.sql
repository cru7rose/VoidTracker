-- Insert Profiles
INSERT INTO planning_vehicle_profiles (id, name, max_capacity_weight, max_capacity_volume) VALUES
('PROFILE_VAN', 'Standard Van', 1000.0, 15.0),
('PROFILE_BIKE', 'Cargo Bike', 100.0, 2.0)
ON CONFLICT (id) DO NOTHING;

-- Insert Compliance Data
INSERT INTO planning_carrier_compliance (carrier_id, is_insured, compliance_status) VALUES
('CARRIER_GOOD', true, 'COMPLIANT'),
('CARRIER_BAD', false, 'SUSPENDED')
ON CONFLICT (carrier_id) DO NOTHING;

-- Insert/Update Vehicles
-- Vehicle 1: GOOD Carrier (Should be used)
INSERT INTO planning_fleet_vehicles (id, name, capacity_weight, capacity_volume, available, driver_id, profile_id, carrier_id) VALUES
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Van-Compliant', 500.0, 5.0, true, 'driver-1', 'PROFILE_VAN', 'CARRIER_GOOD')
ON CONFLICT (id) DO UPDATE SET carrier_id = 'CARRIER_GOOD', profile_id = 'PROFILE_VAN';

-- Vehicle 2: BAD Carrier (Should be ignored)
INSERT INTO planning_fleet_vehicles (id, name, capacity_weight, capacity_volume, available, driver_id, profile_id, carrier_id) VALUES
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'Van-NonCompliant', 500.0, 5.0, true, 'driver-2', 'PROFILE_VAN', 'CARRIER_BAD')
ON CONFLICT (id) DO UPDATE SET carrier_id = 'CARRIER_BAD', profile_id = 'PROFILE_VAN';
