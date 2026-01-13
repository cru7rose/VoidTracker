-- Fix Planning Service Database Schema Issues
-- Run this script to fix missing columns and nullable constraints

-- 1. Fix planning_fleet_vehicles.available column
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'planning_fleet_vehicles' AND column_name = 'available') THEN
        ALTER TABLE planning_fleet_vehicles ADD COLUMN available BOOLEAN;
        UPDATE planning_fleet_vehicles SET available = true WHERE available IS NULL;
        ALTER TABLE planning_fleet_vehicles ALTER COLUMN available SET DEFAULT true;
        RAISE NOTICE 'Added available column to planning_fleet_vehicles';
    ELSE
        -- Column exists, just update NULLs
        UPDATE planning_fleet_vehicles SET available = true WHERE available IS NULL;
        ALTER TABLE planning_fleet_vehicles ALTER COLUMN available SET DEFAULT true;
        RAISE NOTICE 'Updated existing available column in planning_fleet_vehicles';
    END IF;
END $$;

-- 2. Fix communication_logs.channel column
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'communication_logs' AND column_name = 'channel') THEN
        ALTER TABLE communication_logs ADD COLUMN channel VARCHAR(255);
        UPDATE communication_logs SET channel = 'UNKNOWN' WHERE channel IS NULL;
        ALTER TABLE communication_logs ALTER COLUMN channel SET DEFAULT 'UNKNOWN';
        RAISE NOTICE 'Added channel column to communication_logs';
    ELSE
        UPDATE communication_logs SET channel = 'UNKNOWN' WHERE channel IS NULL;
        ALTER TABLE communication_logs ALTER COLUMN channel SET DEFAULT 'UNKNOWN';
        RAISE NOTICE 'Updated existing channel column in communication_logs';
    END IF;
END $$;

-- 3. Fix communication_logs.recipient column
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'communication_logs' AND column_name = 'recipient') THEN
        ALTER TABLE communication_logs ADD COLUMN recipient VARCHAR(255);
        UPDATE communication_logs SET recipient = 'UNKNOWN' WHERE recipient IS NULL;
        ALTER TABLE communication_logs ALTER COLUMN recipient SET DEFAULT 'UNKNOWN';
        RAISE NOTICE 'Added recipient column to communication_logs';
    ELSE
        UPDATE communication_logs SET recipient = 'UNKNOWN' WHERE recipient IS NULL;
        ALTER TABLE communication_logs ALTER COLUMN recipient SET DEFAULT 'UNKNOWN';
        RAISE NOTICE 'Updated existing recipient column in communication_logs';
    END IF;
END $$;

-- 4. Fix planning_solutions.solution_data column (entity uses solution_data, not solution_json)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'planning_solutions' AND column_name = 'solution_data') THEN
        ALTER TABLE planning_solutions ADD COLUMN solution_data JSONB;
        UPDATE planning_solutions SET solution_data = '{}'::jsonb WHERE solution_data IS NULL;
        RAISE NOTICE 'Added solution_data column to planning_solutions';
    ELSE
        UPDATE planning_solutions SET solution_data = '{}'::jsonb WHERE solution_data IS NULL;
        RAISE NOTICE 'Updated existing solution_data column in planning_solutions';
    END IF;
    
    -- Also handle solution_json if it exists (legacy column name)
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_name = 'planning_solutions' AND column_name = 'solution_json') THEN
        UPDATE planning_solutions SET solution_json = '{}'::jsonb WHERE solution_json IS NULL;
        RAISE NOTICE 'Updated existing solution_json column in planning_solutions';
    END IF;
END $$;

-- Summary
SELECT 'Schema fix completed successfully!' AS status;
