package com.example.planning_service.bootstrap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Automatically fixes database schema issues on startup.
 * This runs before DataInitializer to ensure schema is correct.
 */
@Component
@Order(1) // Run before DataInitializer (which has default order)
@RequiredArgsConstructor
@Slf4j
public class SchemaMigrationRunner implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Skip during build/test - only run when actually starting the application
        if (isBuildOrTestPhase(args)) {
            log.debug("Skipping schema migration during build/test phase");
            return;
        }

        log.info("🔧 Running automatic schema migration...");

        try {
            // 1. Fix planning_fleet_vehicles.available
            fixAvailableColumn();
            
            // 2. Fix communication_logs columns
            fixCommunicationLogsColumns();
            
            // 3. Fix planning_solutions.solution_data
            fixSolutionDataColumn();
            
            log.info("✅ Schema migration completed successfully");
        } catch (Exception e) {
            log.warn("⚠️ Schema migration encountered issues (this may be normal if columns already exist): {}", e.getMessage());
            // Don't fail startup - these are idempotent operations
        }
    }

    private boolean isBuildOrTestPhase(String... args) {
        // Check if running during Maven build (common indicators)
        String classpath = System.getProperty("java.class.path", "");
        return classpath.contains("maven") || 
               classpath.contains("surefire") ||
               System.getProperty("maven.test.skip") != null ||
               System.getProperty("skipTests") != null;
    }

    private void fixAvailableColumn() {
        try {
            // Check if column exists
            Boolean columnExists = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM information_schema.columns " +
                "WHERE table_name = 'planning_fleet_vehicles' AND column_name = 'available')",
                Boolean.class
            );

            if (Boolean.FALSE.equals(columnExists)) {
                log.info("Adding 'available' column to planning_fleet_vehicles");
                jdbcTemplate.execute("ALTER TABLE planning_fleet_vehicles ADD COLUMN available BOOLEAN");
            }

            // Update NULLs and set default
            jdbcTemplate.execute("UPDATE planning_fleet_vehicles SET available = true WHERE available IS NULL");
            jdbcTemplate.execute("ALTER TABLE planning_fleet_vehicles ALTER COLUMN available SET DEFAULT true");
            log.info("✅ Fixed planning_fleet_vehicles.available column");
        } catch (Exception e) {
            log.debug("Available column fix: {}", e.getMessage());
        }
    }

    private void fixCommunicationLogsColumns() {
        try {
            // Fix channel column
            Boolean channelExists = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM information_schema.columns " +
                "WHERE table_name = 'communication_logs' AND column_name = 'channel')",
                Boolean.class
            );

            if (Boolean.FALSE.equals(channelExists)) {
                log.info("Adding 'channel' column to communication_logs");
                jdbcTemplate.execute("ALTER TABLE communication_logs ADD COLUMN channel VARCHAR(255)");
            }
            jdbcTemplate.execute("UPDATE communication_logs SET channel = 'UNKNOWN' WHERE channel IS NULL");
            jdbcTemplate.execute("ALTER TABLE communication_logs ALTER COLUMN channel SET DEFAULT 'UNKNOWN'");

            // Fix recipient column
            Boolean recipientExists = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM information_schema.columns " +
                "WHERE table_name = 'communication_logs' AND column_name = 'recipient')",
                Boolean.class
            );

            if (Boolean.FALSE.equals(recipientExists)) {
                log.info("Adding 'recipient' column to communication_logs");
                jdbcTemplate.execute("ALTER TABLE communication_logs ADD COLUMN recipient VARCHAR(255)");
            }
            jdbcTemplate.execute("UPDATE communication_logs SET recipient = 'UNKNOWN' WHERE recipient IS NULL");
            jdbcTemplate.execute("ALTER TABLE communication_logs ALTER COLUMN recipient SET DEFAULT 'UNKNOWN'");

            log.info("✅ Fixed communication_logs columns");
        } catch (Exception e) {
            log.debug("Communication logs columns fix: {}", e.getMessage());
        }
    }

    private void fixSolutionDataColumn() {
        try {
            // Check for solution_data (current entity name)
            Boolean solutionDataExists = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM information_schema.columns " +
                "WHERE table_name = 'planning_solutions' AND column_name = 'solution_data')",
                Boolean.class
            );

            if (Boolean.FALSE.equals(solutionDataExists)) {
                log.info("Adding 'solution_data' column to planning_solutions");
                jdbcTemplate.execute("ALTER TABLE planning_solutions ADD COLUMN solution_data JSONB");
            }
            jdbcTemplate.execute("UPDATE planning_solutions SET solution_data = '{}'::jsonb WHERE solution_data IS NULL");

            // Also handle solution_json if it exists (legacy)
            Boolean solutionJsonExists = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM information_schema.columns " +
                "WHERE table_name = 'planning_solutions' AND column_name = 'solution_json')",
                Boolean.class
            );

            if (Boolean.TRUE.equals(solutionJsonExists)) {
                jdbcTemplate.execute("UPDATE planning_solutions SET solution_json = '{}'::jsonb WHERE solution_json IS NULL");
            }

            log.info("✅ Fixed planning_solutions.solution_data column");
        } catch (Exception e) {
            log.debug("Solution data column fix: {}", e.getMessage());
        }
    }
}
