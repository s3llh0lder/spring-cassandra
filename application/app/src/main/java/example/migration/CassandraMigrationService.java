package example.migration;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CassandraMigrationService {
    private static final Logger log = LoggerFactory.getLogger(CassandraMigrationService.class);

    @Autowired
    private CqlSession cqlSession;

    @Value("${spring.cassandra.keyspace-name}")
    private String keyspace;

    @Value("${app.migration.enabled:true}")
    private boolean migrationEnabled;

    @Value("${app.migration.validate-on-startup:false}")
    private boolean validateOnStartup;

    @Value("${app.migration.reset-schema:false}")
    private boolean resetSchema;

    private final List<CassandraMigration> migrations = new ArrayList<>();

    @EventListener(ApplicationReadyEvent.class)
    @Order(1)
    public void runMigrationsOnStartup() {
        if (!migrationEnabled) {
            log.info("Database migrations are disabled");
            return;
        }

        log.info("Starting database migration process...");

        try {
            // Create keyspace first if it doesn't exist
            createKeyspaceIfNeeded();

            // Switch to the keyspace
            cqlSession.execute("USE " + keyspace);
            log.debug("Switched to keyspace: {}", keyspace);

            // Reset schema if requested (for development)
            if (resetSchema) {
                log.warn("RESETTING DATABASE SCHEMA - This should only be used in development!");
                resetDatabaseSchema();
            }

            initializeMigrationHistory();
            loadMigrations();

            // Debug: Show what migrations we loaded
            log.info("Loaded migrations:");
            migrations.forEach(migration ->
                    log.info("  - {} : {}", migration.getVersion(), migration.getDescription()));

            runPendingMigrations();

            if (validateOnStartup) {
                validateMigrations();
            }

            log.info("Database migration process completed successfully");
        } catch (Exception e) {
            log.error("Database migration failed", e);

            // Debug: Show current migration status
            try {
                log.error("Current migration status:");
                Set<String> appliedVersions = getAppliedMigrations();
                log.error("Applied migrations: {}", appliedVersions);

                Set<String> expectedVersions = migrations.stream()
                        .map(CassandraMigration::getVersion)
                        .collect(Collectors.toSet());
                log.error("Expected migrations: {}", expectedVersions);

            } catch (Exception debugException) {
                log.error("Failed to get debug info", debugException);
            }

            throw new RuntimeException("Database migration failed", e);
        }
    }

    private void resetDatabaseSchema() {
        log.warn("Dropping all tables in keyspace: {}", keyspace);

        try {
            // Get all tables in the keyspace
            String getTablesQuery = """
                    SELECT table_name FROM system_schema.tables 
                    WHERE keyspace_name = ?
                    """;

            ResultSet tablesResult = cqlSession.execute(getTablesQuery, keyspace);
            List<String> tableNames = new ArrayList<>();

            for (Row row : tablesResult) {
                tableNames.add(row.getString("table_name"));
            }

            // Drop all tables
            for (String tableName : tableNames) {
                try {
                    log.info("Dropping table: {}", tableName);
                    cqlSession.execute("DROP TABLE IF EXISTS " + tableName);
                } catch (Exception e) {
                    log.warn("Failed to drop table {}: {}", tableName, e.getMessage());
                }
            }

            log.warn("Schema reset completed. All tables dropped.");

        } catch (Exception e) {
            log.error("Failed to reset schema", e);
            throw new RuntimeException("Schema reset failed", e);
        }
    }

    private void createKeyspaceIfNeeded() {
        log.info("Ensuring keyspace '{}' exists...", keyspace);

        try {
            // Check if keyspace exists
            String checkKeyspaceQuery = "SELECT keyspace_name FROM system_schema.keyspaces WHERE keyspace_name = ?";
            ResultSet resultSet = cqlSession.execute(checkKeyspaceQuery, keyspace);

            if (!resultSet.iterator().hasNext()) {
                // Keyspace doesn't exist, create it
                log.info("Creating keyspace '{}'...", keyspace);
                String createKeyspaceQuery = String.format("""
                        CREATE KEYSPACE %s
                        WITH REPLICATION = {
                            'class': 'SimpleStrategy',
                            'replication_factor': 1
                        }
                        """, keyspace);

                cqlSession.execute(createKeyspaceQuery);
                log.info("Keyspace '{}' created successfully", keyspace);
            } else {
                log.debug("Keyspace '{}' already exists", keyspace);
            }
        } catch (Exception e) {
            log.error("Failed to create keyspace '{}'", keyspace, e);
            throw new RuntimeException("Failed to create keyspace: " + keyspace, e);
        }
    }

    private void initializeMigrationHistory() {
        log.debug("Initializing migration history table...");

        String createTableCql = """
                CREATE TABLE IF NOT EXISTS migration_history (
                    version TEXT PRIMARY KEY,
                    description TEXT,
                    applied_at TIMESTAMP,
                    applied_by TEXT,
                    success BOOLEAN,
                    error_message TEXT,
                    execution_time_ms BIGINT
                )
                """;

        cqlSession.execute(createTableCql);
        log.debug("Migration history table initialized");
    }

    private void loadMigrations() {
        migrations.clear();

        try {
            // Add your migration classes here as they're created
            log.debug("Loading migration classes...");

            migrations.add(new V001_CreateInitialSchema());
            log.debug("Loaded V001_CreateInitialSchema");

            migrations.add(new V002_CreateUserStatsTable());
            log.debug("Loaded V002_CreateUserStatsTable");

            migrations.add(new V003_CreatePostTables());
            log.debug("Loaded V003_CreatePostTables");

            migrations.add(new V004_CreateIndexes());
            log.debug("Loaded V004_CreateIndexes");

            // Sort by version to ensure proper order
            migrations.sort(Comparator.comparing(CassandraMigration::getVersion));

            log.info("Successfully loaded {} migrations", migrations.size());

        } catch (Exception e) {
            log.error("Failed to load migration classes", e);
            throw new RuntimeException("Failed to load migrations", e);
        }
    }

    private void runPendingMigrations() {
        log.debug("Checking for pending migrations...");

        Set<String> appliedVersions = getAppliedMigrations();
        log.debug("Found {} already applied migrations: {}", appliedVersions.size(), appliedVersions);

        List<CassandraMigration> pendingMigrations = migrations.stream()
                .filter(migration -> !appliedVersions.contains(migration.getVersion()))
                .collect(Collectors.toList());

        if (pendingMigrations.isEmpty()) {
            log.info("No pending migrations found");
            return;
        }

        log.info("Found {} pending migrations: {}",
                pendingMigrations.size(),
                pendingMigrations.stream().map(CassandraMigration::getVersion).collect(Collectors.toList()));

        MigrationContext context = new MigrationContext(cqlSession, keyspace);

        for (CassandraMigration migration : pendingMigrations) {
            runMigration(migration, context);
        }
    }

    private void runMigration(CassandraMigration migration, MigrationContext context) {
        log.info("Applying migration: {} - {}", migration.getVersion(), migration.getDescription());

        MigrationRecord record = new MigrationRecord(migration.getVersion(), migration.getDescription());
        long startTime = System.currentTimeMillis();

        try {
            migration.migrate(context);

            record.setSuccess(true);
            record.setExecutionTimeMs(System.currentTimeMillis() - startTime);

            saveMigrationRecord(record);

            log.info("Successfully applied migration: {} in {}ms",
                    migration.getVersion(), record.getExecutionTimeMs());

        } catch (Exception e) {
            record.setSuccess(false);
            record.setErrorMessage(e.getMessage());
            record.setExecutionTimeMs(System.currentTimeMillis() - startTime);

            saveMigrationRecord(record);

            log.error("Failed to apply migration: {} - Error: {}", migration.getVersion(), e.getMessage(), e);
            throw new RuntimeException("Migration failed: " + migration.getVersion(), e);
        }
    }

    private Set<String> getAppliedMigrations() {
        try {
            // Query all migration records and filter in Java instead of Cassandra
            ResultSet resultSet = cqlSession.execute("SELECT version, success FROM migration_history");
            Set<String> appliedVersions = new HashSet<>();

            for (Row row : resultSet) {
                // Only include successful migrations
                if (row.getBoolean("success")) {
                    appliedVersions.add(row.getString("version"));
                }
            }

            log.debug("Found {} applied migrations", appliedVersions.size());
            return appliedVersions;
        } catch (Exception e) {
            log.debug("Migration history table doesn't exist or is empty, error: {}", e.getMessage());
            return new HashSet<>();
        }
    }

    private void saveMigrationRecord(MigrationRecord record) {
        String insertCql = """
                INSERT INTO migration_history (version, description, applied_at, applied_by, success, error_message, execution_time_ms)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        // Convert OffsetDateTime to Instant for storage
        Instant appliedAtInstant = record.getAppliedAt().toInstant();

        cqlSession.execute(insertCql,
                record.getVersion(),
                record.getDescription(),
                appliedAtInstant, // Store as Instant in Cassandra
                record.getAppliedBy(),
                record.isSuccess(),
                record.getErrorMessage(),
                record.getExecutionTimeMs()
        );

        log.debug("Saved migration record for version: {} (success: {})",
                record.getVersion(), record.isSuccess());
    }

    private void validateMigrations() {
        log.debug("Validating migration integrity...");

        // Check that all expected migrations have been applied
        Set<String> appliedVersions = getAppliedMigrations();
        Set<String> expectedVersions = migrations.stream()
                .map(CassandraMigration::getVersion)
                .collect(Collectors.toSet());

        Set<String> missingMigrations = new HashSet<>(expectedVersions);
        missingMigrations.removeAll(appliedVersions);

        if (!missingMigrations.isEmpty()) {
            log.error("Validation failed - Missing migrations: {}", missingMigrations);
            log.error("Applied migrations: {}", appliedVersions);
            log.error("Expected migrations: {}", expectedVersions);
            throw new RuntimeException("Missing migrations: " + missingMigrations);
        }

        Set<String> unexpectedMigrations = new HashSet<>(appliedVersions);
        unexpectedMigrations.removeAll(expectedVersions);

        if (!unexpectedMigrations.isEmpty()) {
            log.warn("Found unexpected migrations in history: {}", unexpectedMigrations);
        }

        log.debug("Migration validation completed successfully");
    }

    // Public methods for manual migration management
    public void runMigrations() {
        runPendingMigrations();
    }

    public List<MigrationInfo> getMigrationStatus() {
        Set<String> appliedVersions = getAppliedMigrations();

        return migrations.stream()
                .map(migration -> new MigrationInfo(
                        migration.getVersion(),
                        migration.getDescription(),
                        appliedVersions.contains(migration.getVersion())
                ))
                .collect(Collectors.toList());
    }

    public static class MigrationInfo {
        private final String version;
        private final String description;
        private final boolean applied;

        public MigrationInfo(String version, String description, boolean applied) {
            this.version = version;
            this.description = description;
            this.applied = applied;
        }

        // Getters
        public String getVersion() {
            return version;
        }

        public String getDescription() {
            return description;
        }

        public boolean isApplied() {
            return applied;
        }
    }
}