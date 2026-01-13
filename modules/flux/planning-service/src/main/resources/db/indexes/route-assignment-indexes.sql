-- Performance indexes for route_assignment table
-- Designed for high-volume scenarios: thousands of orders per day, multiple clients
-- Supports filtering by status, driver, carrier, dates, and multi-tenant queries

-- Primary access patterns: Status filtering (most common)
CREATE INDEX IF NOT EXISTS idx_route_assignment_status 
ON route_assignment(status) 
WHERE status IN ('DRAFT', 'ASSIGNED', 'PUBLISHED', 'IN_PROGRESS');

-- Driver filtering (for Ghost PWA and driver-specific queries)
CREATE INDEX IF NOT EXISTS idx_route_assignment_driver_id 
ON route_assignment(driver_id) 
WHERE driver_id IS NOT NULL;

-- Composite index: Driver + Status (common query pattern for active routes)
CREATE INDEX IF NOT EXISTS idx_route_assignment_driver_status 
ON route_assignment(driver_id, status) 
WHERE driver_id IS NOT NULL AND status IN ('PUBLISHED', 'IN_PROGRESS');

-- Vehicle filtering
CREATE INDEX IF NOT EXISTS idx_route_assignment_vehicle_id 
ON route_assignment(vehicle_id) 
WHERE vehicle_id IS NOT NULL;

-- Carrier filtering (multi-tenant scenarios)
CREATE INDEX IF NOT EXISTS idx_route_assignment_carrier_id 
ON route_assignment(carrier_id) 
WHERE carrier_id IS NOT NULL;

-- Optimization solution filtering
CREATE INDEX IF NOT EXISTS idx_route_assignment_solution_id 
ON route_assignment(optimization_solution_id) 
WHERE optimization_solution_id IS NOT NULL;

-- Date range queries: Created date (for filtering by date range)
CREATE INDEX IF NOT EXISTS idx_route_assignment_created_at 
ON route_assignment(created_at DESC);

-- Date range queries: Updated date (for change tracking)
CREATE INDEX IF NOT EXISTS idx_route_assignment_updated_at 
ON route_assignment(updated_at DESC);

-- Composite: Status + Created date (for recent routes by status)
CREATE INDEX IF NOT EXISTS idx_route_assignment_status_created 
ON route_assignment(status, created_at DESC);

-- Route name search (partial match, case-insensitive queries)
-- Using GIN index for text search on route_name
CREATE INDEX IF NOT EXISTS idx_route_assignment_route_name_gin 
ON route_assignment USING gin(lower(route_name) gin_trgm_ops);

-- JSONB index for route_data queries (if needed for filtering by route content)
-- Uncomment if you need to query inside route_data JSONB
-- CREATE INDEX IF NOT EXISTS idx_route_assignment_route_data_gin 
-- ON route_assignment USING gin(route_data);

-- Partial index for active routes only (reduces index size)
CREATE INDEX IF NOT EXISTS idx_route_assignment_active 
ON route_assignment(driver_id, created_at DESC) 
WHERE status IN ('PUBLISHED', 'IN_PROGRESS');

-- Notes:
-- 1. Partial indexes (WHERE clauses) reduce index size and improve performance
-- 2. DESC ordering on dates supports "most recent first" queries
-- 3. GIN index for text search requires pg_trgm extension: CREATE EXTENSION IF NOT EXISTS pg_trgm;
-- 4. Monitor index usage with: SELECT * FROM pg_stat_user_indexes WHERE schemaname = 'public';
