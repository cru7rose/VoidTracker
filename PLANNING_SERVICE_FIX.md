# Planning Service Schema Fix

## Problem
Planning service crashes on startup with errors:
- `ERROR: column fve1_0.available does not exist`
- `ERROR: column "channel" of relation "communication_logs" contains null values`
- `ERROR: column "recipient" of relation "communication_logs" contains null values`
- `ERROR: column "solution_json" of relation "planning_solutions" contains null values`

## Root Cause
Hibernate's `ddl-auto: update` is trying to add new columns as NOT NULL, but:
1. The columns don't exist yet
2. Existing rows would have NULL values

## Solution

### Option 1: Run SQL Migration Script (Recommended)

Run the SQL script to add missing columns:

```bash
# If you have psql installed:
psql -h localhost -p 5434 -U postgres -d voidtracker_planning -f fix_planning_schema.sql

# Or using the helper script:
./run_schema_fix.sh
```

The script will:
- Add `available` column to `planning_fleet_vehicles` (nullable, default true)
- Add `channel` column to `communication_logs` (nullable, default 'UNKNOWN')
- Add `recipient` column to `communication_logs` (nullable, default 'UNKNOWN')
- Fix `solution_data` column in `planning_solutions` (nullable, default '{}')

### Option 2: Temporary Workaround (If you can't run SQL)

Temporarily change Hibernate ddl-auto to `validate` or `none`:

```yaml
# In modules/flux/planning-service/src/main/resources/application.yml
spring:
  jpa:
    hibernate:
      ddl-auto: validate  # Change from 'update' to 'validate'
```

**Warning**: This will prevent Hibernate from creating/updating tables. You'll still need to run the SQL script eventually.

### Option 3: Manual SQL Execution

If you have database access, run these commands:

```sql
-- Fix planning_fleet_vehicles.available
ALTER TABLE planning_fleet_vehicles ADD COLUMN IF NOT EXISTS available BOOLEAN;
UPDATE planning_fleet_vehicles SET available = true WHERE available IS NULL;
ALTER TABLE planning_fleet_vehicles ALTER COLUMN available SET DEFAULT true;

-- Fix communication_logs.channel
ALTER TABLE communication_logs ADD COLUMN IF NOT EXISTS channel VARCHAR(255);
UPDATE communication_logs SET channel = 'UNKNOWN' WHERE channel IS NULL;
ALTER TABLE communication_logs ALTER COLUMN channel SET DEFAULT 'UNKNOWN';

-- Fix communication_logs.recipient
ALTER TABLE communication_logs ADD COLUMN IF NOT EXISTS recipient VARCHAR(255);
UPDATE communication_logs SET recipient = 'UNKNOWN' WHERE recipient IS NULL;
ALTER TABLE communication_logs ALTER COLUMN recipient SET DEFAULT 'UNKNOWN';

-- Fix planning_solutions.solution_data
ALTER TABLE planning_solutions ADD COLUMN IF NOT EXISTS solution_data JSONB;
UPDATE planning_solutions SET solution_data = '{}'::jsonb WHERE solution_data IS NULL;
```

## After Fix

1. Restart the planning service
2. Verify it starts without errors
3. Change `ddl-auto` back to `update` if you used Option 2

## Files Modified

- `fix_planning_schema.sql` - Complete migration script
- `run_schema_fix.sh` - Helper script to run migration
- `modules/flux/planning-service/src/main/java/com/example/planning_service/entity/FleetVehicleEntity.java` - Added explicit column name
